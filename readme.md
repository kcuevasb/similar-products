# Similar Products API - Hexagonal Architecture

This application implements a **Hexagonal Architecture** (Ports and Adapters) to ensure modularity, testability, and maintainability. It leverages **Resilience4j** for resilience patterns (Circuit Breaker, Retry, Rate Limiter), **Caffeine** for caching, and **Spring's RestClient** for HTTP communication.

---

## Architecture Overview

The application separates concerns clearly across hexagonal boundaries:

### 🔸 External World
- REST clients (e.g., browsers, Postman)
- External APIs (mock services via Docker)

### 🔸 Inbound Adapter (Controller)
- `ProductController`
- Exposes: `GET /product/{productId}/similar`

### 🔸 Application Layer (Use Case)
- `ProductServiceImpl`
- Orchestrates domain operations

### 🔸 Outbound Adapter (External API Client)
- `ExternalProductApiClient` using `RestClient`
- Fetches similar product IDs and full product details

### 🔸 Domain Layer
- `ProductService` (port)
- `Product` (model)
- `ProductNotFoundException`, `TooManyRequestsException`, and global error handling

---

## Package Structure

| Package                             | Description                                           |
|-------------------------------------|-------------------------------------------------------|
| `domain.model`                      | Core domain entities (e.g., `Product`)               |
| `domain.port`                       | Port interfaces (e.g., `ProductService`)             |
| `domain.exception`                 | Business exceptions and error handling               |
| `application`                       | Use case implementation (`ProductServiceImpl`)       |
| `infrastructure.controller`         | REST controller exposing the API                     |
| `infrastructure.external`           | RestClient-based outbound adapter                    |
| `infrastructure.client`             | Product loader service + caching logic               |
| `infrastructure.client.config`      | Caffeine cache and RestClient config                 |
| `shared`                            | General-purpose configuration                        |

---

## Runtime Flow

1. Client sends `GET /product/{id}/similar`.
2. `ProductController` delegates to `ProductService`.
3. `ProductServiceImpl` orchestrates:
    - Calls external API for similar product IDs.
    - For each ID, fetches full product info (cached).
4. Circuit breaker, retry, rate limiter, and fallback are applied transparently.
5. A list of `Product` objects is returned.

> If a product ID is not found: `404 Not Found`  
> If rate limit is exceeded: `429 Too Many Requests`  
> If circuit breaker is open: `503 Service Unavailable`

---

## Why This Topology

- Clean separation of responsibilities
- Pluggable/adaptable to new APIs or clients
- Easy to test and mock
- Built-in resilience (CB, Retry, Rate Limiter)
- Efficient caching with **Caffeine**
- Simple and effective HTTP client using **Spring RestClient**

---

## Running the App

### Requirements
- Java 17+
- Maven
- Docker & Docker Compose

---

### 1️⃣ Clone the Repo

```bash
git clone https://github.com/your-org/similar-products-api.git
cd similar-products-api
```

---

### 2️⃣ Start Mock Services (via Docker)

```bash
docker-compose up -d simulado influxdb grafana
```

Mock API URL:  
http://localhost:3001

Grafana dashboard (for performance test):  
http://localhost:3000

---

### 3️⃣ Build and Run the App

```bash
mvn clean install
java -jar target/similar-products-1.0.0-SNAPSHOT.jar
```

API will be available at:  
http://localhost:5000

---

### 4️⃣ Try the API

```bash
curl http://localhost:5000/product/1/similar
```

Sample response:

```json
[
  {
    "id": "2",
    "name": "Product 2",
    "price": 19.99,
    "availability": true
  },
  {
    "id": "3",
    "name": "Product 3",
    "price": 24.99,
    "availability": true
  }
]
```

---

### 5️⃣ Performance Test with K6

```bash
docker-compose run --rm k6 run scripts/test.js
```

View real-time metrics in Grafana:  
http://localhost:3000/d/Le2Ku9NMk/k6-performance-test

---

## Configuration

Application uses these defaults:

```properties
server.port=5000
external.api.base-url=http://localhost:3001
```

Resilience4j (Circuit Breaker, Retry, Rate Limiter) and cache settings are defined in `application.yml`.