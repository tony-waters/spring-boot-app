# spring-jpa
Setting up Java Persistence API (JPA) using Spring Boot 4 and Hibernate.

# Overview
JPA allows us to map Java objects to a relational database, whats called Object-Relational Mapping (ORM).
Getting this right creates a solid persistence layer for Java applications backed by a relational database.
At its core it allows us to effectively represent @OneToOne, @OneToMany, and @ManyToMany relationships at the DB level.

This repo represents the Java code to show how each of these relationships work on a practical level.

A large part of confidence in releasing something to production is to have the right tests with good code coverage.
So I have included a realistic set of tests.

Plan is to create the entity/repository layer here, then build other layers over it.

# The Entities
I have chosen a simple Domain model for demonstration purposes:

domain model ER diagram here.

Here a Customer has a @OneToMany relationship with Order (Customers make Orders).
Orders have a @ManyToMany relationship with Products (an Order can contain many Products, and a Product can be in many Orders).
And a Customer has a @OneToOne relationship with ContactInfo (each Customer has a single ContactInfo).

Note that in terms of the provided code, particularly the tests, I have focused mostly on the Customer side of things.
“Many-to-many is convenient for demos; real order systems usually use an OrderLine join entity for quantity/price.”

Of particular note is the Order -> Product relationship.
This is quite an oversimplification from a business perspective.
In the real world an Order -> Product relationship would need information attached to it, 
for example, the Price when the Order was made.
So its not really a @ManyToMany relationship.

## highlights

## testing
You do not want to be dealing with bugs from the JPA layer.
If you are working on a already-in-production system these bugs have already seeped into the client layer,
and they may be very difficult to trace.
So let us try and lock down the JPA Entity layer so we feel confident, and can sleep well at night.

At the same time realise that re-testing what has already been tested is a code smell.
It creates unnecessary complexity (more code to read and understand) and diverts attention from the important bits.

With JPA Entities we are operating within a framework where a lot of testing is already taking place.
So for the sake of brevity we should try and avoid testing the framework itself.
At the same time we are configuring the framework with Annotations.
So one question is should we test our use of the JPA annotations is correct?
We can break this down further as the annotations refer to different facets of the ORM.
...
Its not straightforward what one should be testing where Entites are concerned.

# The Repositories

## highlights

## testing

