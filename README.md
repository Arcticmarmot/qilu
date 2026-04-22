# Qilu

Qilu is a community-oriented backend project built with **Spring Boot 3**, **MyBatis-Plus**, **MySQL**, **Redis**, and **Kafka**.

The project is currently focused on a content-community scenario: users can register and log in, publish posts, like posts, comment on posts, reply to comments, and receive asynchronous interaction notifications.

This repository is mainly a **backend practice project** with a clear modular structure, SQL migrations, and basic infrastructure setup for local development.

## Status

Qilu is under active development.

At the current stage, the repository already contains modules for:

- authentication and JWT-based login
- user management
- post publishing and querying
- post likes
- post comments
- comment replies
- like notifications
- comment notifications
- Kafka producers and consumers for asynchronous interaction events
- Flyway database migrations
- Docker Compose based local infrastructure for MySQL, Redis, and Kafka

The current source tree shows dedicated modules for `auth`, `user`, `post`, `like`, `comment`, `reply`, and `notification`, plus common infrastructure such as JWT, OpenAPI config, global exception handling, Kafka topic definitions, and Flyway migrations. It also includes local Docker deployment files under `deploy/docker` for Kafka, MySQL, and Redis, along with mapper XML files and versioned SQL migrations under `src/main/resources`.

## Tech Stack

- **Java 17**
- **Spring Boot 3**
- **MyBatis-Plus**
- **MySQL**
- **Redis**
- **Kafka**
- **JWT**
- **OpenAPI / Swagger**
- **Docker Compose**

## Project Goals

Qilu is designed as a small but realistic backend system for a content-community product. The project emphasizes:

- modular backend design
- clean resource-oriented APIs
- interaction flows such as like, comment, and reply
- asynchronous notification delivery with Kafka
- maintainable database evolution through Flyway
- local reproducible infrastructure using Docker Compose

Instead of pursuing a large monolith of features all at once, Qilu is being built incrementally around a practical V1 scope.

## Main Features

### 1. Authentication

- login endpoint
- JWT generation and parsing
- request authentication through interceptor

### 2. User Module

- user creation
- user query APIs

### 3. Post Module

- create post
- update post
- query post detail
- query post page/list

### 4. Like Module

- like posts
- maintain interaction state
- trigger asynchronous notification events

### 5. Comment Module

- create root comments for posts
- list post comments

### 6. Reply Module

- create second-level replies under comments
- reply either to a root comment or to another reply while keeping a two-level discussion structure

### 7. Notification Module

- like notification consumer and query APIs
- comment notification consumer and query APIs
- unread notification count support can be extended from here

## Infrastructure

The repository already includes local infrastructure definitions:

- `deploy/docker/mysql/docker-compose.yml`
- `deploy/docker/redis/docker-compose.yml`
- `deploy/docker/kafka/docker-compose.yml`

These files are intended to help run the backend locally with the required middleware.

## API Style

Qilu follows a modular REST-style backend design. For interaction resources such as comments and replies, the APIs are organized around clear ownership relationships between posts, comments, and replies.

The project also keeps business validation close to the domain flow, for example:

- a reply must belong to a valid root comment
- a comment must belong to an interactable post
- notifications are produced asynchronously after successful write operations

## Local Development

Typical local development flow:

1. Start MySQL, Redis, and Kafka with the provided Docker Compose files.
2. Configure `application.yaml` for local environment.
3. Run Flyway migrations automatically on startup.
4. Start the Spring Boot application.

Common commands usually include:
```bash
make up
```


```bash
./mvnw spring-boot:run
```

or

```bash
./mvnw clean test
./mvnw clean package
```

## What This Repository Is Good For

Qilu is a good fit for people who want to study or discuss:

- Spring Boot modular backend design
- MyBatis-Plus based persistence
- SQL schema evolution with Flyway
- Redis / Kafka integration in a community product scenario
- interaction and notification modeling for posts, comments, and replies

## Notes

This project is still evolving, so some modules may be incomplete or subject to refactoring. The current repository structure already shows the intended architecture and the main business direction.

If you are interested in backend engineering, community products, or interaction-driven system design, Qilu is intended to be a clear and readable practice project rather than an over-engineered demo.