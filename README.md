# spring-jpa
Setting up Java Persistence API (JPA) using Spring Boot 4 and Hibernate.

# Overview
JPA allows us to map Java objects to a relational database, whats called ORM (Object-Relational Mapping) framework.
Getting this right creates a solid persistence layer for Java applications utilising a relational database.
At its core it allows us to effectively represent @OneToOne, @OneToMany, and @ManyToMany relationships at the DB level.

This repo represents the Java code to show how each of these relationships work on a practical level.

A large part of confidence in releasing something to production is to have the right tests with good code coverage.
So I have included a realistic set of tests.

Plan is to create the entity/repository layer here, then build other layers over it.

# The Domain Model
I have chosen a simple Domain model for demonstration purposes:

domain model ER diagram here.

Here a Customer has a @OneToMany relationship with Order (Customers make Orders).
Orders have a @ManyToMany relationship with Products (an Order can contain many Products, and a Product can be in many Orders).
And a Customer has a @OneToOne relationship with ContactInfo (each Customer has a single ContactInfo).

## highlights

# The Repositories

## highlights

# Repository tests

## highlights


