# Order Management Service

A Spring Boot REST API for managing customer orders, built to demonstrate secure API development, data consistency, caching, and scalable backend design.

## Technology Stack

- Java 21
- Spring Boot 4
- Spring Security and JWT
- Spring Data JPA and Hibernate
- PostgreSQL
- Redis
- Maven
- JUnit and Mockito
- Docker

## Features

- **Order Management:** Create, retrieve, update, and delete orders.
- **Validation and Exception Handling:** Request validation and meaningful HTTP error responses.
- **Pagination:** Retrieve orders in manageable pages.
- **JWT Authentication:** Secure API endpoints using JWT validation.
- **Role-Based Authorization:** Restrict update and delete operations to administrators.
- **Idempotency:** Prevent duplicate order creation when a request is retried with the same idempotency key.
- **Optimistic Locking:** Use Hibernate's `@Version` mechanism to help prevent conflicting database updates.
- **Redis Caching:** Cache order retrieval responses and invalidate cached data after updates.
- **Correlation IDs:** Trace requests through application logs using `X-Correlation-Id`.
- **Automated Testing:** Unit and web-layer tests using JUnit and Mockito.
- **LocalJwtConfig and LocalJwtTokenGenerator are for local testing, not production authentication.

## Architecture

```text
Client / Postman
       |
       v
Correlation ID Filter
       |
       v
Spring Security
(JWT + Role Authorization)
       |
       v
Order Controller
       |
       v
Order Service
(Business Logic)
       |
       +------> Redis Cache
       |
       v
Order Repository
(Spring Data JPA)
       |
       v
PostgreSQL
```

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/orders` | Create an order |
| GET | `/api/orders` | Retrieve all orders |
| GET | `/api/orders/{id}` | Retrieve an order by ID |
| GET | `/api/orders/page` | Retrieve paginated orders |
| GET | `/api/orders/status/{status}` | Filter orders by status |
| PUT | `/api/orders/{id}` | Update an order |
| DELETE | `/api/orders/{id}` | Delete an order |

Order creation requires an `Idempotency-Key` header. Protected endpoints require a valid JWT.

## Running Locally

### Prerequisites

- Java 21
- Maven
- PostgreSQL
- Redis

Configure the database connection and Redis settings in `application.properties` before starting the application.

Run the tests:

```bash
mvn test
```

Run the application:

```bash
mvn spring-boot:run
```

## Scalability and Reliability

- Pagination helps avoid loading large numbers of records in a single request.
- Redis caching reduces repeated database reads.
- Idempotency supports safe retries for order creation.
- Optimistic locking helps maintain data consistency during concurrent updates.
- Stateless JWT authentication supports horizontally scaled application instances.
- Correlation IDs help trace requests during troubleshooting.

## Future Enhancements

- OAuth2 integration with Keycloak
- Kafka-based asynchronous order processing
- Centralized observability and distributed tracing
- Docker Compose for local infrastructure
- Kubernetes deployment and health monitoring
- Integration tests for concurrent database updates
