# ![RealWorld Example App using Kotlin and Spring](example-logo.png)

[![Actions](https://github.com/gothinkster/spring-boot-realworld-example-app/workflows/Java%20CI/badge.svg)](https://github.com/gothinkster/spring-boot-realworld-example-app/actions)
![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.3-brightgreen.svg)

> ### Spring boot + MyBatis codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld-example-apps) spec and API.

This codebase was created to demonstrate a fully fledged full-stack application built with Spring boot + Mybatis including CRUD operations, authentication, routing, pagination, and more.

For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.

## Table of Contents

- [GraphQL Support](#graphql-support)
- [How it Works](#how-it-works)
- [Technology Stack](#technology-stack)
- [Security](#security)
- [Database](#database)
- [API Reference](#api-reference)
  - [REST API Endpoints](#rest-api-endpoints)
  - [GraphQL API](#graphql-api)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Docker](#docker)
- [Code Format](#code-format)
- [Additional Documentation](#additional-documentation)
- [Help](#help)

## GraphQL Support

Following DDD principles, REST and GraphQL are just different adapters. The domain layer remains consistent, allowing this repository to implement both GraphQL and REST APIs simultaneously.

**GraphQL Endpoints:**
- GraphQL API: `http://localhost:8080/graphql`
- GraphiQL Interactive UI: `http://localhost:8080/graphiql`

**Available Queries:**
- `article(slug)` - Get a single article
- `articles(...)` - List articles with filters
- `me` - Get current user
- `feed(...)` - Get personalized feed
- `profile(username)` - Get user profile
- `tags` - Get all tags

**Available Mutations:**
- User: `createUser`, `login`, `updateUser`, `followUser`, `unfollowUser`
- Article: `createArticle`, `updateArticle`, `deleteArticle`, `favoriteArticle`, `unfavoriteArticle`
- Comment: `addComment`, `deleteComment`

The complete GraphQL schema is available at [schema.graphqls](https://github.com/gothinkster/spring-boot-realworld-example-app/blob/master/src/main/resources/schema/schema.graphqls) and the visualization looks like below.

![](graphql-schema.png)

This implementation uses [Netflix DGS Framework](https://github.com/Netflix/dgs-framework), a modern Java GraphQL server framework.
## How it Works

The application is built with **Spring Boot 2.6.3** and uses **MyBatis** for database persistence, implementing a clean architecture based on Domain-Driven Design principles.

### Architecture Patterns

**Domain-Driven Design (DDD):**
- Separates business logic from infrastructure concerns
- Core domain remains independent of frameworks and adapters

**Data Mapper Pattern:**
- MyBatis implements the [Data Mapper](https://martinfowler.com/eaaCatalog/dataMapper.html) pattern
- Keeps domain objects free from persistence logic

**CQRS (Command Query Responsibility Segregation):**
- Separates read operations (queries) from write operations (commands)
- Read models (`ArticleQueryService`) are optimized for queries
- Write models (`ArticleCommandService`) handle business logic and validation

### Code Organization

The codebase follows a **four-layer architecture**:

1. **`api`** - Web layer with REST controllers and GraphQL resolvers
   - REST: `UsersApi`, `ArticlesApi`, `ProfileApi`, etc.
   - GraphQL: Mutations and data fetchers using Netflix DGS

2. **`core`** - Domain model with business entities and logic
   - Entities: `User`, `Article`, `Comment`
   - Repository interfaces
   - Domain services

3. **`application`** - High-level application services
   - Query services for read operations
   - Command services for write operations
   - DTOs for data transfer

4. **`infrastructure`** - Technical implementation details
   - MyBatis mappers and repositories
   - JWT service implementation
   - Database configuration

## Technology Stack

- **Java 17** - Language version
- **Spring Boot 2.6.3** - Application framework
- **MyBatis 2.2.2** - SQL mapping framework
- **Netflix DGS 4.9.21** - GraphQL server framework
- **SQLite 3.36.0.3** - Embedded database
- **Flyway** - Database migrations
- **JJWT 0.11.2** - JSON Web Token implementation
- **Spring Security** - Authentication and authorization
- **Gradle 7.4** - Build tool

## Security

The application uses **Spring Security** with **JWT (JSON Web Token)** authentication.

**Authentication Flow:**
1. User registers or logs in via `/users` or `/users/login`
2. Server returns a JWT token valid for 24 hours
3. Client includes token in subsequent requests: `Authorization: Token {jwt}`
4. `JwtTokenFilter` validates token and loads user context
5. Protected endpoints verify authentication and ownership

**Security Configuration:**
- **Password Encryption:** BCrypt hashing algorithm
- **JWT Session Time:** 86400 seconds (24 hours)
- **Token Format:** `Authorization: Token {jwt-token-here}`
- **Secret Key:** Stored in `application.properties` (change for production!)

**Protected Resources:**
- Article/comment modifications require ownership validation
- User profile updates require authentication
- Follow/favorite actions require authentication

## Database

The application uses **SQLite** for local development and testing.

**Configuration:**
- Database file: `dev.db` in the project root
- Flyway manages database migrations
- Database is automatically cleaned during `./gradlew clean` tasks

**Switching Databases:**
You can easily switch to other databases (PostgreSQL, MySQL, etc.) by updating the connection settings in `application.properties`.

## API Reference

### REST API Endpoints

All REST endpoints follow the [RealWorld API spec](https://realworld-docs.netlify.app/docs/specs/backend-specs/endpoints). Base URL: `http://localhost:8080`

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/users` | Register new user | No |
| POST | `/users/login` | Login user | No |
| GET | `/user` | Get current user | Yes |
| PUT | `/user` | Update current user | Yes |
| POST | `/articles` | Create article | Yes |
| GET | `/articles` | List articles (supports `tag`, `author`, `favorited`, `limit`, `offset`) | Optional |
| GET | `/articles/feed` | Get personalized feed | Yes |
| GET | `/articles/{slug}` | Get single article by slug | Optional |
| PUT | `/articles/{slug}` | Update article | Yes (owner only) |
| DELETE | `/articles/{slug}` | Delete article | Yes (owner only) |
| POST | `/articles/{slug}/favorite` | Favorite an article | Yes |
| DELETE | `/articles/{slug}/favorite` | Unfavorite an article | Yes |
| POST | `/articles/{slug}/comments` | Add comment to article | Yes |
| GET | `/articles/{slug}/comments` | Get article comments | Optional |
| DELETE | `/articles/{slug}/comments/{id}` | Delete comment | Yes (owner only) |
| GET | `/profiles/{username}` | Get user profile | Optional |
| POST | `/profiles/{username}/follow` | Follow user | Yes |
| DELETE | `/profiles/{username}/follow` | Unfollow user | Yes |
| GET | `/tags` | Get all tags | No |

**Authentication Header Format:**
```
Authorization: Token {your-jwt-token}
```

### GraphQL API

**Endpoints:**
- API Endpoint: `http://localhost:8080/graphql`
- Interactive UI: `http://localhost:8080/graphiql`

**Features:**
- Type-safe queries and mutations
- Cursor-based pagination for articles and comments
- Inline fragments for flexible data fetching
- Full schema introspection

**Schema:**
The complete GraphQL schema is available in [`src/main/resources/schema/schema.graphqls`](src/main/resources/schema/schema.graphqls). Use GraphiQL at `http://localhost:8080/graphiql` for interactive exploration and auto-completion.

## Getting Started

### Prerequisites

- **Java 17** (required)
- **Gradle** (wrapper included, no separate installation needed)

### Running the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

### Verify Installation

Test that it works by accessing the tags endpoint:

**Browser:**
Open http://localhost:8080/tags

**Command Line:**
```bash
curl http://localhost:8080/tags
```

**Expected Response:**
```json
{
  "tags": ["tag1", "tag2", "..."]
}
```

### Explore the API

- **REST API:** See the [API Reference](#api-reference) section above
- **GraphQL:** Open http://localhost:8080/graphiql for interactive GraphQL exploration

## Testing

### Run All Tests

```bash
./gradlew test
```

### Run Lint Checks

```bash
./gradlew checkstyleMain checkstyleTest
```

## Docker

### Build Docker Image

```bash
./gradlew bootBuildImage --imageName spring-boot-realworld-example-app
```

### Run Container

```bash
docker run -p 8081:8080 spring-boot-realworld-example-app
```

The application will be available at http://localhost:8081

### Try with a RealWorld Frontend

The backend API is available at `http://localhost:8080` (**not** `http://localhost:8080/api` as some frontend documentation suggests).

Compatible frontends can be found at [RealWorld Frontends](https://codebase.show/projects/realworld).

## Code Format

This project uses Spotless for code formatting and Checkstyle for linting.

### Apply Code Formatting

```bash
./gradlew spotlessJavaApply
```

### Check Code Style

```bash
./gradlew checkstyleMain checkstyleTest
```

## Additional Documentation

- **[SONAR_QUICKSTART.md](SONAR_QUICKSTART.md)** - SonarCloud setup and code quality analysis
- **[jenkins-demo/](jenkins-demo/)** - Jenkins pipeline configuration and CI/CD setup
- **[jenkins-demo/SONAR_SETUP.md](jenkins-demo/SONAR_SETUP.md)** - Detailed SonarCloud integration guide
- **[jenkins-demo/TESTING.md](jenkins-demo/TESTING.md)** - Testing documentation

## Help

Please fork and PR to improve the project.

**Contributing:**
- Follow the existing code style
- Add tests for new features
- Update documentation as needed
- Run lint and tests before submitting PR
