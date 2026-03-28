# Spring Boot JPA – Aggregate + CQRS Demo

A focused Spring Boot application demonstrating:

* Aggregate-oriented domain modelling (DDD-lite)
* Clear separation of **Command vs Query**
* Projection-based read model (no entity leakage)
* REST API with validation and proper status codes
* Layered testing strategy (domain → JPA → service → web)

This is intentionally **not** a CRUD demo.

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

Important detail:

> Tests use **flush + clear** to avoid false positives from persistence context caching.

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

## 🚀 Running the application

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
