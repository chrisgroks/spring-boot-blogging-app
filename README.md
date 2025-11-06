# ![RealWorld Example App using Kotlin and Spring](example-logo.png)

[![Actions](https://github.com/gothinkster/spring-boot-realworld-example-app/workflows/Java%20CI/badge.svg)](https://github.com/gothinkster/spring-boot-realworld-example-app/actions)

> ### A Spring Boot + MyBatis implementation of the RealWorld spec - a Medium.com clone backend demonstrating real-world application patterns including CRUD, authentication, authorization, advanced architectural patterns (DDD, CQRS), and dual API support (REST + GraphQL).

This codebase serves as a comprehensive example of a production-ready backend application built with Spring Boot and MyBatis. It implements the complete [RealWorld API specification](https://github.com/gothinkster/realworld-example-apps), providing a fully-featured blogging platform backend with user management, article publishing, social features (following users, favoriting articles), and content discovery.

**Key Highlights:**
- 🏗️ **Domain-Driven Design (DDD)** with clean separation of business logic and infrastructure
- 🔄 **CQRS Pattern** for optimized read and write operations
- 🌐 **Dual API Support** - Both RESTful and GraphQL interfaces
- 🔐 **JWT Authentication** with Spring Security integration
- 📊 **MyBatis** for clean data mapping and SQL control
- 🎯 **Comprehensive test coverage** with 68+ tests

For more information on how this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.

## Features

This application provides a complete set of features for a modern blogging platform:

### User Management
- **Registration & Authentication** - Secure user signup with JWT token-based authentication
- **Profile Management** - Update user profile including bio, image, email, and password
- **User Following** - Follow/unfollow other users to curate your content feed

### Article Management
- **CRUD Operations** - Create, read, update, and delete articles with markdown support
- **Rich Content** - Support for title, description, body, and tags
- **Slug Generation** - Automatic URL-friendly slug creation from article titles
- **Author Attribution** - Articles linked to author profiles with metadata

### Social Features
- **Favoriting** - Mark articles as favorites and track favorite counts
- **Comments** - Add, view, and delete comments on articles
- **Feed System** - Personalized article feed based on followed users
- **Tag-Based Discovery** - Browse and filter articles by tags

### API & Integration
- **RESTful API** - Complete REST endpoints following RealWorld spec
- **GraphQL API** - Full GraphQL schema with queries and mutations
- **Pagination** - Support for both offset-based and cursor-based pagination
- **CORS Support** - Configured for cross-origin requests from frontend applications

# *NEW* GraphQL Support  

Following some DDD principles. REST or GraphQL is just a kind of adapter. And the domain layer will be consistent all the time. So this repository implement GraphQL and REST at the same time.

The GraphQL schema is https://github.com/gothinkster/spring-boot-realworld-example-app/blob/master/src/main/resources/schema/schema.graphqls and the visualization looks like below.

![](graphql-schema.png)

And this implementation is using [dgs-framework](https://github.com/Netflix/dgs-framework) which is a quite new java graphql server framework.

# How it works

## Technology Stack

The application is built with the following technologies:

- **Spring Boot 2.6.3** - Framework foundation with Web, Security, Validation, and HATEOAS modules
- **Java 17** - Modern Java with enhanced performance and language features
- **MyBatis 2.2.2** - SQL mapping framework for clean data access
- **Netflix DGS 4.9.21** - GraphQL server framework for robust GraphQL API
- **JJWT 0.11.2** - JSON Web Token implementation for authentication
- **SQLite 3.36.0.3** - Lightweight database for development (easily swappable for production databases)
- **Flyway** - Database migration management
- **Gradle 7.4** - Build automation and dependency management
- **Lombok** - Reduce boilerplate code
- **JUnit 5 & Rest Assured** - Comprehensive testing framework

## Architecture & Design Patterns

The application follows **Domain-Driven Design (DDD)** principles to maintain a clean separation between business logic and technical implementation:

* **Domain-Driven Design** - Separates business terminology and rules from infrastructure concerns, keeping the domain model pure and focused
* **Data Mapper Pattern** - MyBatis implements this pattern to map between database records and domain objects without polluting the domain with persistence logic
* **CQRS (Command Query Responsibility Segregation)** - Separates read operations (queries) from write operations (commands) for optimized performance and clearer code organization

### Four-Layer Architecture

The code is organized into four distinct layers, each with clear responsibilities:

1. **`api`** - Web/Adapter Layer
   - REST controllers using Spring MVC
   - GraphQL data fetchers using Netflix DGS
   - Request/response DTOs and validation
   - Security filters (JWT token processing)

2. **`core`** - Domain Layer
   - Pure business entities (User, Article, Comment)
   - Domain services and business rules
   - Repository interfaces (technology-agnostic)
   - No dependencies on infrastructure

3. **`application`** - Application Service Layer
   - High-level orchestration services
   - Query services for read operations (CQRS reads)
   - Command services for write operations (CQRS writes)
   - DTO transformations for API responses

4. **`infrastructure`** - Infrastructure Layer
   - MyBatis repository implementations
   - MyBatis XML mappers with SQL
   - JWT service implementation
   - Database configurations and migrations

# API Documentation

The application exposes two complete API interfaces - REST and GraphQL - both implementing the full RealWorld specification.

## REST API

**Base URL:** `http://localhost:8080`

### Key Endpoints

**User & Authentication**
- `POST /users` - Register a new user
- `POST /users/login` - Login and receive JWT token
- `GET /user` - Get current user profile (authenticated)
- `PUT /user` - Update current user profile (authenticated)

**Profiles**
- `GET /profiles/:username` - Get user profile
- `POST /profiles/:username/follow` - Follow a user (authenticated)
- `DELETE /profiles/:username/follow` - Unfollow a user (authenticated)

**Articles**
- `GET /articles` - List articles (supports filtering by tag, author, favorited by)
- `GET /articles/feed` - Get personalized feed (authenticated)
- `GET /articles/:slug` - Get single article
- `POST /articles` - Create article (authenticated)
- `PUT /articles/:slug` - Update article (authenticated)
- `DELETE /articles/:slug` - Delete article (authenticated)
- `POST /articles/:slug/favorite` - Favorite article (authenticated)
- `DELETE /articles/:slug/favorite` - Unfavorite article (authenticated)

**Comments**
- `GET /articles/:slug/comments` - Get article comments
- `POST /articles/:slug/comments` - Add comment (authenticated)
- `DELETE /articles/:slug/comments/:id` - Delete comment (authenticated)

**Tags**
- `GET /tags` - Get all tags

## GraphQL API

**Endpoint:** `http://localhost:8080/graphql`  
**GraphiQL Interface:** `http://localhost:8080/graphiql` (interactive API explorer)

### Key Queries
- `article(slug: String!)` - Fetch single article
- `articles(...)` - List articles with filtering and cursor-based pagination
- `feed(...)` - Personalized article feed
- `profile(username: String!)` - User profile
- `tags` - All available tags
- `me` - Current authenticated user

### Key Mutations
- `createUser`, `login`, `updateUser` - User operations
- `createArticle`, `updateArticle`, `deleteArticle` - Article operations
- `favoriteArticle`, `unfavoriteArticle` - Favorite operations
- `followUser`, `unfollowUser` - Follow operations
- `addComment`, `deleteComment` - Comment operations

The complete GraphQL schema is available in [`src/main/resources/schema/schema.graphqls`](src/main/resources/schema/schema.graphqls).

# Security

Integration with Spring Security and add other filter for jwt token process.

The secret key is stored in `application.properties`.

# Database

It uses a ~~H2 in-memory database~~ sqlite database (for easy local test without losing test data after every restart), can be changed easily in the `application.properties` for any other database.

# Getting started

## Prerequisites

- **Java 17** - Required for building and running the application
- **Gradle 7.4+** - Included via Gradle wrapper (`./gradlew`), no separate installation needed

## Running the Application

Start the application using Gradle:

    ./gradlew bootRun

The application will start on **http://localhost:8080**

### Verify it's working

Open a browser tab at http://localhost:8080/tags or use curl:

    curl http://localhost:8080/tags

You should see a JSON response with available tags.

### Explore the APIs

- **REST API**: Try http://localhost:8080/articles
- **GraphQL Playground**: Open http://localhost:8080/graphiql in your browser for an interactive GraphQL explorer

# Try it out with [Docker](https://www.docker.com/)

You'll need Docker installed.
	
    ./gradlew bootBuildImage --imageName spring-boot-realworld-example-app
    docker run -p 8081:8080 spring-boot-realworld-example-app

# Try it out with a RealWorld frontend

The entry point address of the backend API is at http://localhost:8080, **not** http://localhost:8080/api as some of the frontend documentation suggests.

# Run test

The repository contains a lot of test cases to cover both api test and repository test.

    ./gradlew test

# Code format

Use spotless for code format.

    ./gradlew spotlessJavaApply

# Help

Please fork and PR to improve the project.
