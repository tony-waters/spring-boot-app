# Spring Boot Demo

A focused Spring Boot application demonstrating:

* @OneToOne, @OneToMany, and @ManyToMany relationships
* Aggregate-oriented domain modelling (DDD-lite)
* Clear separation of **Command vs Query** with CQRS
* Projection-based read model (no entity leakage)
* Query Pagination and Filtering
* REST API with validation and proper status codes
* Layered testing strategy (domain → JPA → service → web)

---

## Tech Stack

- Java 21
- Spring Boot 4.x
- Spring Data JPA
- Hibernate 7
- H2 (test database)
- Postgres (for docker-compose)
- AssertJ

---

## 🧠 What this project demonstrates

### 1. Rich Domain Model

* `Customer` is the **aggregate root**
* `Ticket` and `Profile` are internal entities
* No public setters — behaviour-driven methods only
* Invariants enforced inside the domain

Example:

```java
customer.raiseTicket("This is a valid ticket");
customer.resolveTicket(ticketId);
customer.addTagToTicket(ticketId, tag);
```

---

### 2. Aggregate Boundaries

* All mutations go through `Customer`
* Child entities (`Ticket`, `Profile`) are not directly exposed
* Relationships are managed internally

This prevents:

* inconsistent state
* orphan logic in services/controllers

---

### 3. Command / Query Separation

**Command side**

* Loads full aggregate
* Applies business rules
* Persists changes

```java
Customer customer = customerRepository.findAggregateById(id);
customer.resolveTicket(ticketId);
```

**Query side**

* Uses DTO projections
* No entity loading
* Optimised for read performance

```java
select new CustomerSummaryView(...)
from Customer c
```

---

### 4. JPA Usage Beyond Basics

* `@OneToOne`, `@OneToMany`, `@ManyToMany`
* `cascade` + `orphanRemoval`
* lazy loading
* `@EntityGraph` for aggregate loading
* projection queries for read side

---

### 5. REST API (Thin Controllers)

* Controllers map:

  * HTTP → Commands (write)
  * HTTP → Query DTOs (read)
* No business logic in controllers
* Validation via `jakarta.validation`
* Consistent error handling

---

### 6. Filtering + Pagination

Examples:

```http
GET /api/customers?page=0&size=10
GET /api/customers?name=tony
GET /api/customers/1/tickets?status=OPEN
GET /api/customers/1/tickets?tag=bug
```

---

### 7. Testing Strategy

Layered tests:

| Layer           | Approach                        |
| --------------- | ------------------------------- |
| Domain          | Plain unit tests                |
| JPA             | `@DataJpaTest`                  |
| Command Service | Transactional integration tests |
| Query           | Projection + filtering tests    |
| Web             | `@WebMvcTest` (slice tests)     |

---

## 🏗️ Architecture Overview

```
web (REST controllers)
   ↓
application
   ├── command (write use-cases)
   └── query (read use-cases)
   ↓
domain (aggregate + invariants)
   ↓
persistence (Spring Data JPA)
```

---

## 📦 Key Packages

```
domain/
  customer/
  tag/

application/
  customer/
    command/
    query/

web/
  customer/

```

---

## 🚀 Running the application with Maven

### Requirements

* Java 21
* Maven

### Run

```bash
mvn spring-boot:run
```

App will start on:

```
http://localhost:8080
```

---

## 🔍 Example API Usage

### Create a customer

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{ "displayName": "Tony" }'
```

---

### Raise a ticket

```bash
curl -X POST http://localhost:8080/api/customers/1/tickets \
  -H "Content-Type: application/json" \
  -d '{ "description": "This is a valid ticket" }'
```

---

### Resolve a ticket

```bash
curl -X POST http://localhost:8080/api/customers/1/tickets/10/resolve
```

---

### Query customers

```bash
curl "http://localhost:8080/api/customers?page=0&size=5"
```

---

### Filter tickets

```bash
curl "http://localhost:8080/api/customers/1/tickets?status=OPEN"
curl "http://localhost:8080/api/customers/1/tickets?tag=bug"
```

---

## 🐳 Running the Application with Docker Compose

This project can be run locally using Docker Compose, with:

* Spring Boot application
* PostgreSQL database
* Environment-based configuration (no local setup required)

---

### 📦 Prerequisites

* Docker
* Docker Compose (v2+)

---

### 🚀 Start the application

```bash
docker compose up --build
```

This will:

1. Build the Spring Boot application image
2. Start a PostgreSQL container
3. Start the application container
4. Wire them together via Docker networking

---

### 🌐 Access the application

* API: http://localhost:8080
* Health: http://localhost:8080/actuator/health

---

### 🔍 Verify it’s working

Check health:

```bash
curl http://localhost:8080/actuator/health
```

Create a customer:

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"displayName":"Tony"}'
```

Query customers:

```bash
curl "http://localhost:8080/api/customers?page=0&size=5"
```

---

### 🗄️ Database details

The application connects to PostgreSQL using:

```
jdbc:postgresql://postgres:5432/spring_jpa
```

Credentials (from `docker-compose.yml`):

* database: `spring_jpa`
* username: `spring_user`
* password: `spring_pass`

Data is persisted in a Docker volume:

```
postgres_data
```

---

### ⚙️ Configuration

All configuration is provided via environment variables:

| Variable                        | Purpose             |
| ------------------------------- | ------------------- |
| `SPRING_DATASOURCE_URL`         | Database connection |
| `SPRING_DATASOURCE_USERNAME`    | DB username         |
| `SPRING_DATASOURCE_PASSWORD`    | DB password         |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Schema generation   |
| `SERVER_PORT`                   | App port            |
| `JAVA_TOOL_OPTIONS`             | JVM memory settings |

---

### 🧠 Notes

* Uses `ddl-auto=update` for convenience (not for production)
* PostgreSQL runs with a health check before the app starts
* Application exposes `/actuator/health` for readiness/liveness

---

### 🛑 Stop the application

```bash
docker compose down
```

To remove the database volume as well:

```bash
docker compose down -v
```

---

### 📈 Why this matters

This setup demonstrates:

* Containerised Spring Boot application
* Externalised configuration (12-factor style)
* Service-to-service networking
* Real database integration

It forms the foundation for the Kubernetes deployment shown later.


## ⚠️ What this project deliberately avoids

* Generic CRUD services
* Entity exposure in controllers
* “God” service classes
* Overuse of DTO mappers (MapStruct not required on query side)
* Premature abstraction (no specifications/query DSL yet)

---

## 📈 Possible next steps

If extending this demo:

* Add containerisation (Docker + Postgres)
* Add load testing (k6/Gatling)
* Add authentication
* Introduce domain events (e.g. TicketResolvedEvent)
* Develop Tag as a separate aggregate route

---

## 🧩 Key Takeaway

This project demonstrates that:

> You can build a Spring Boot + JPA application that is
> **not CRUD-driven**,
> **not anemic**,
> and **still simple to reason about and test**.

---

## 📄 License

MIT (or your choice)
