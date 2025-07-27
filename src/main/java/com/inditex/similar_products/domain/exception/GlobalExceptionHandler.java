package com.inditex.similar_products.domain.exception;

import com.inditex.similar_products.domain.exception.custom.ProductNotFoundException;
import com.inditex.similar_products.domain.exception.custom.TooManyRequestsException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ProductNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<Object> handleCircuitBreaker(CallNotPermittedException ex, HttpServletRequest request) {
        log.warn("Circuit breaker is OPEN for method: {}", ex.getLocalizedMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex, request,
                "Service temporarily unavailable due to high failure rate");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.error("Illegal state (possible cache/config issue): {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Object> handleRateLimiter(RequestNotPermitted ex, HttpServletRequest request) {
        log.warn("Rate limiter triggered for URI: {}", request.getRequestURI());
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex, request,
                "Too many requests - please try again later");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, Exception ex, HttpServletRequest request) {
        return buildResponse(status, ex, request, ex.getMessage());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, Exception ex, HttpServletRequest request, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", request.getRequestURI(),
                "exception", ex.getClass().getSimpleName()
        ));
    }
}