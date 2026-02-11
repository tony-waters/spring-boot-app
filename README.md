# spring-jpa
Setting up Java Persistence API (JPA) using Spring Boot 4 and Hibernate.

# Overview
JPA allows us to map Java objects to a relational database, whats called Object-Relational Mapping (ORM).
Getting this right creates a solid persistence layer for Java applications backed by a relational database.
At its core it allows us to effectively represent @OneToOne, @OneToMany, and @ManyToMany relationships at the DB level.

This repo represents the Java code to show how each of these relationships work on a practical level.

A large part of confidence in releasing something to production is to have the right tests.
So I have included a realistic set of tests.

Plan is to create the entity/repository layer here, then build other layers over it.

# The Entities
I have chosen a simple Domain model for demonstration purposes:

domain model ER diagram here.

Here a Customer has a @OneToMany relationship with Order (Customers make Orders).
Orders have a @ManyToMany relationship with Products (an Order can contain many Products, and a Product can be in many Orders).
And a Customer has a @OneToOne relationship with ContactInfo (each Customer has a single ContactInfo).

Of particular note is the Order -> Product relationship.
This is quite an oversimplification from a business perspective.
In the real world an Order -> Product relationship would need information attached to it, 
for example, the Price when the Order was made.
So its not really a @ManyToMany relationship.

## highlights

### inheriting from a BaseEntity

## testing

What I’ll cover (and only what’s worth covering for entities):

Bidirectional consistency
Customer.addOrder/removeOrder/clearOrders
Order.addProduct/removeProduct/clearProducts
Customer.setContactInfo(...) with @MapsId

Orphan removal
removing orders deletes rows
removing contact info deletes row
clearing collections removes join rows where appropriate

Immutability / guard rails
unmodifiable collections can’t be mutated through getters
ID/audit fields not settable

Equals/hashCode “Set safety”
transient entities don’t collapse; persisted ones behave as expected

We will avoid CRUD.

### add / remove / clear Consistency Tests

### orphan removal Tests

### lock in equals / hashcode correctness Tests

### pressure Tests

While its not always straightforward what one should be testing where Entites are concerned,
we can begin with the tenent that we should test any code/logic we have added.
In our case this mainly consists of the add, remove, and clear methods we have included in the @ToMany relationships to maintain the realtionship integrity at the DB level.


In a Production system you do not want to be dealing with bugs from the JPA layer.
If you are working on a already-in-production system these bugs have already implicated the client layer,
and they may be very difficult to trace.
All the while your database is potentially persisting these bugs at a data level.
So let us try and lock down the JPA Entity layer so we feel confident, and can sleep well at night.

At the same time realise that re-testing what has already been tested is a code smell.
It creates unnecessary complexity (more code to read and understand) and diverts attention from the important bits.

With JPA Entities we are operating within a framework where a lot of testing is already taking place.
So for the sake of brevity and sanity we should try and avoid testing the framework itself too much.
At the same time we are configuring the framework with Annotations.
So one question is should we test our use of the JPA annotations is correct?
We can break this down further as the annotations refer to different facets of the ORM.


# The Repositories

## highlights

### using projections

## testing

# The Service layer