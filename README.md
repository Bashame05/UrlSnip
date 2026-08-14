# UrlSnip

A fast, lightweight URL shortener REST API built with Spring Boot and MySQL — now with authentication, user-owned URLs, and rate limiting.

> **Version:** v3.0.0

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Setup](#setup)
  - [Run](#run)
  - [Docker](#docker)
- [Environment Variables](#environment-variables)
- [Project Structure](#project-structure)

---

## Overview

UrlSnip takes long URLs and converts them into compact, shareable short links. When a short link is visited, the user is seamlessly redirected (HTTP 302) to the original URL. Every redirect is tracked, giving you basic click analytics out of the box. You can also set custom aliases, configure expiration dates, and generate QR codes for any short link.

Users register and log in to receive a JWT token. All URL operations are scoped to the authenticated user — each shortened URL is owned by the user who created it, and only the owner can view analytics, generate QR codes, or mark URLs as favourites. Public redirect endpoints are rate-limited per IP to prevent abuse.

---

## Features

- **Authentication & Authorization** — User registration and login powered by Spring Security with stateless JWT-based access control. Passwords are hashed with BCrypt.
- **User-Owned URLs** — Every shortened URL is associated with the authenticated user. Only the owner can access analytics, generate QR codes, or manage their links.
- **Personal Analytics Dashboard** — Per-user analytics endpoint returning total URLs created and total redirects across all owned URLs.
- **Favourite URLs** — Mark any owned URL as a favourite and retrieve all favourites for quick access.
- **Rate Limiting** — IP-based rate limiting on the redirect endpoint using Bucket4j (10 requests per minute per IP) to prevent API abuse.
- **Shorten URLs** — Generate a unique 7-character short code for any long URL using NanoID.
- **Custom Aliases** — Choose your own short code (3–7 alphanumeric characters, e.g., `/mylink`) instead of a randomly generated one. Prohibited system aliases are blocked automatically.
- **URL Expiration** — Set an optional expiry duration (up to 183 days). Expired links return `410 Gone`.
- **QR Code Generation** — Generate a 300×300 PNG QR code for any short URL on the fly.
- **Instant Redirect** — Visit a short URL (by short code or custom alias) and get redirected to the original destination via HTTP 302.
- **Click Analytics** — Track total click count, creation time, expiry time, and last accessed time for each short URL.
- **Duplicate Detection** — Submitting the same long URL returns the existing short URL instead of creating a duplicate (scoped per user).
- **Request Validation** — Input payloads are validated using Jakarta Bean Validation (e.g., expiry range constraints, email format, password length).
- **Structured Error Responses** — All errors return a consistent JSON format with `timestamp`, `status`, `error`, and `message`.
- **Global Exception Handling** — Clean error responses for invalid aliases, missing URLs, expired links, bad credentials, duplicate users, and ownership violations.
- **Dockerised Deployment** — Ship the app and MySQL together with a single `docker-compose up`.

---

## Tech Stack

| Layer          | Technology                     |
|----------------|--------------------------------|
| Language       | Java 21                        |
| Framework      | Spring Boot 4.1.0              |
| Web            | Spring WebMVC                  |
| Security       | Spring Security + JJWT 0.12.6  |
| Persistence    | Spring Data JPA + Hibernate    |
| Database       | MySQL                          |
| Validation     | Jakarta Bean Validation        |
| Rate Limiting  | Bucket4j 8.14.0                |
| QR Codes       | ZXing (core + javase) 3.5.3    |
| ID Generator   | JNanoID 2.0.0                  |
| Boilerplate    | Lombok                         |
| Build Tool     | Maven                          |
| Container      | Docker + Docker Compose        |

---

## Architecture

The project follows a clean **layered architecture**:

```
Controller  →  Service  →  Repository  →  Database
     ↑             ↑
    DTOs        Mappers
                   ↑
                 Utils

Security Filter Chain (JWT)  →  Controller
Rate Limiter (Bucket4j)      →  Redirect Endpoint
```

| Package        | Responsibility                                                                  |
|----------------|---------------------------------------------------------------------------------|
| `controller`   | REST endpoints — receives requests and returns responses                        |
| `service`      | Business logic — URL shortening, redirection, analytics, QR generation, auth    |
| `repository`   | Data access — Spring Data JPA interfaces for `UrlMapping` and `User` entities   |
| `model`        | JPA entities — `UrlMapping` and `User` table mappings                           |
| `dto`          | Data Transfer Objects — organised into `auth/` and `url/` sub-packages          |
| `mapper`       | Entity-to-DTO converters with base URL prefixing                                |
| `utils`        | Utility components — `ShortUrlBuilder`, `ROLES` enum                            |
| `config`       | Spring Security configuration — filter chain, authentication, password encoding |
| `security`     | JWT filter, JWT service, `UserPrincipal`, custom `UserDetailsService`           |
| `ratelimit`    | IP-based rate limiting configuration using Bucket4j                             |
| `exception`    | Custom exceptions and global exception handler                                  |

---

## API Endpoints

### Authentication

#### 1. Register

```
POST /api/auth/register
```

**Request Body:**
```json
{
  "userName": "johndoe",
  "userEmail": "john@example.com",
  "userPassword": "securepassword"
}
```

> Username must be 3–20 characters. Email must be valid. Password must be at least 7 characters.

**Response** `200 OK`:
```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "userName": "johndoe",
  "userEmail": "john@example.com",
  "userRole": "USER",
  "createdAt": "2026-08-14T14:00:00"
}
```

---

#### 2. Login

```
POST /api/auth/login
```

**Request Body:**
```json
{
  "userName": "johndoe",
  "userPassword": "securepassword"
}
```

**Response** `200 OK` — returns a JWT token string:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

> Include this token in subsequent requests as: `Authorization: Bearer <token>`

---

### URL Operations

> All URL endpoints (except redirect) require a valid JWT token in the `Authorization` header.

#### 3. Shorten a URL

```
POST /api/urls
```

**Request Body:**
```json
{
  "longUrl": "https://www.example.com/some/very/long/path",
  "customAlias": "mylink",
  "expiresInDays": 30
}
```

> `customAlias` and `expiresInDays` are optional. Alias must be 3–7 alphanumeric characters. Expiry must be between 0 and 183 days.

**Response** `200 OK`:
```json
{
  "longUrl": "https://www.example.com/some/very/long/path",
  "shortUrl": "http://localhost:7000/mylink",
  "createdAt": "2026-08-14T14:00:00",
  "expiresIn": "2026-09-13T14:00:00"
}
```

---

#### 4. Redirect to Original URL

```
GET /{identifier}
```

The `identifier` can be either the generated short code or a custom alias. This endpoint is **public** (no authentication required) but is **rate-limited** to 10 requests per minute per IP.

**Response** `302 Found` — redirects to the original long URL.

**Example:**
```
GET /mylink  →  302 redirect to https://www.example.com/some/very/long/path
```

If the identifier does not exist, returns `404 Not Found`. If the URL has expired, returns `410 Gone`. If the rate limit is exceeded, returns `429 Too Many Requests`.

---

#### 5. Get Analytics

```
GET /api/urls/{identifier}/analytics
```

> Only the URL owner can access this endpoint.

**Response** `200 OK`:
```json
{
  "longUrl": "https://www.example.com/some/very/long/path",
  "shortUrl": "http://localhost:7000/mylink",
  "createdAt": "2026-08-14T14:00:00",
  "lastAccessed": "2026-08-14T14:45:12",
  "expiresAt": "2026-09-13T14:00:00",
  "clickCount": 42
}
```

---

#### 6. Generate QR Code

```
GET /api/urls/{identifier}/qrcode
```

> Only the URL owner can access this endpoint.

**Response** `200 OK` — returns a **300×300 PNG image** of a QR code that encodes the original long URL.

If the URL has expired, returns `410 Gone`.

---

#### 7. Mark URL as Favourite

```
POST /api/url/{identifier}/favourites
```

> Only the URL owner can access this endpoint.

**Response** `200 OK`:
```
Url marked as favourite
```

---

#### 8. Get Favourite URLs

```
GET /api/url/favourites
```

**Response** `200 OK`:
```json
[
  {
    "longUrl": "https://www.example.com/some/very/long/path",
    "shortUrl": "http://localhost:7000/mylink"
  }
]
```

---

#### 9. Get Personal Analytics

```
GET /api/user/analytics
```

**Response** `200 OK`:
```json
{
  "urlsCreated": 12,
  "totalRedirects": 347
}
```

---

### Error Response Format

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-08-14T14:15:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "No such URL or alias found"
}
```

| Status | Condition                                       |
|--------|-------------------------------------------------|
| `400`  | Validation error (invalid input)                |
| `401`  | Missing, invalid, or expired JWT token          |
| `403`  | Accessing a URL not owned by the authenticated user |
| `404`  | Short code or alias not found                   |
| `409`  | Custom alias unavailable or user already exists |
| `410`  | URL has expired                                 |
| `429`  | Rate limit exceeded on redirect endpoint        |

---

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+** (or use the included Maven Wrapper)
- **MySQL 8.0+**
- **Docker & Docker Compose** (optional — for containerised deployment)

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Bashame05/UrlSnip.git
   cd UrlSnip
   ```

2. **Create the MySQL database:**
   ```sql
   CREATE DATABASE urls;
   ```

3. **Configure environment variables:**

   Create a `.env` file in the project root (this file is git-ignored):
   ```env
   DB_URL=jdbc:mysql://localhost:3306/urls
   DB_USER=your_db_username
   DB_PASSWORD=your_db_password
   DB_DATABASE=urls
   ABSOLUTE_URL=localhost:7000/
   JWT_SECRET=your_base64_encoded_secret_key
   ```

   > `ABSOLUTE_URL` is the base URL prefix used when returning short URLs in API responses.
   >
   > `JWT_SECRET` must be a Base64-encoded key (at least 256 bits) used to sign and verify JWT tokens.

### Run

Using the Maven Wrapper:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The application starts on **port 7000** by default.

### Docker

Build the Docker image and start both the app and MySQL with a single command:

```bash
# Build the application image
docker build -t url-snip:latest .

# Start all services
docker-compose up -d
```

> The `docker-compose.yaml` reads variables from the `.env` file. MySQL data is persisted in a named volume (`mysql_data`).

---

## Environment Variables

| Variable       | Description                                      | Example                              |
|----------------|--------------------------------------------------|--------------------------------------|
| `DB_URL`       | JDBC connection string for the MySQL database    | `jdbc:mysql://localhost:3306/urls`    |
| `DB_USER`      | MySQL username                                   | `root`                               |
| `DB_PASSWORD`  | MySQL password                                   | `your_password`                      |
| `DB_DATABASE`  | MySQL database name (used by Docker Compose)     | `urls`                               |
| `ABSOLUTE_URL` | Base URL prefix for generated short links        | `localhost:7000/`                     |
| `JWT_SECRET`   | Base64-encoded secret key for signing JWT tokens | `poKGJvMJUqdfP8R74mNJAN...`          |

---

## Project Structure

```
url-shortner/
├── src/
│   ├── main/
│   │   ├── java/com/makar/UrlSnip/
│   │   │   ├── UrlSnip.java                         # Application entry point
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java               # Spring Security filter chain & beans
│   │   │   ├── security/
│   │   │   │   ├── JwtFilter.java                    # JWT authentication filter
│   │   │   │   ├── JwtService.java                   # JWT token generation & validation
│   │   │   │   ├── MyUserDetailsService.java         # Custom UserDetailsService
│   │   │   │   └── UserPrincipal.java                # UserDetails implementation
│   │   │   ├── ratelimit/
│   │   │   │   └── RateLimitConfig.java              # IP-based rate limiting (Bucket4j)
│   │   │   ├── controller/
│   │   │   │   ├── UserController.java               # Auth endpoints (register, login)
│   │   │   │   └── UrlController.java                # URL endpoints (CRUD, analytics, QR)
│   │   │   ├── service/
│   │   │   │   ├── UserService.java                  # User registration & login logic
│   │   │   │   └── UrlService.java                   # Core URL business logic
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java               # JPA data access for User
│   │   │   │   └── UrlRepository.java                # JPA data access for UrlMapping
│   │   │   ├── model/
│   │   │   │   ├── User.java                         # User JPA entity (UUID primary key)
│   │   │   │   └── UrlMapping.java                   # UrlMapping JPA entity
│   │   │   ├── dto/
│   │   │   │   ├── ErrorResponseDto.java             # Structured error payload
│   │   │   │   ├── auth/
│   │   │   │   │   ├── UserRegisterDto.java          # Registration request payload
│   │   │   │   │   ├── UserLoginDto.java             # Login request payload
│   │   │   │   │   └── UserResponseDto.java          # Registration response payload
│   │   │   │   └── url/
│   │   │   │       ├── UrlRequestDto.java            # Shorten request payload
│   │   │   │       ├── UrlResponseDto.java           # Shorten response payload
│   │   │   │       ├── UrlAnalyticsDto.java          # URL analytics response payload
│   │   │   │       ├── UserAnalyticsDto.java         # Personal analytics response payload
│   │   │   │       └── UserFavouriteUrlDto.java      # Favourite URL response payload
│   │   │   ├── mapper/
│   │   │   │   ├── UrlResponseMapper.java            # Entity → Response DTO
│   │   │   │   ├── UrlAnalyticsMapper.java           # Entity → Analytics DTO
│   │   │   │   ├── UserResponseMapper.java           # User → UserResponse DTO
│   │   │   │   └── UserFavouriteUrlMapper.java       # Entity → Favourite DTO
│   │   │   ├── utils/
│   │   │   │   ├── ShortUrlBuilder.java              # Builds full short URL (alias-aware)
│   │   │   │   └── ROLES.java                        # User role enum (ADMIN, USER)
│   │   │   └── exception/
│   │   │       ├── NoSuchUrlException.java           # Custom 404 exception
│   │   │       ├── AliasNotAllowedException.java     # Custom 409 exception
│   │   │       ├── UrlExpiredException.java          # Custom 410 exception
│   │   │       ├── UserAlreadyExistsException.java   # Custom 409 exception
│   │   │       ├── UrlNotOwnedException.java         # Custom 403 exception
│   │   │       └── GlobalExceptionHandler.java       # Centralized error handling
│   │   └── resources/
│   │       └── application.yaml                      # App configuration
│   └── test/
├── .env                                               # Environment variables (git-ignored)
├── .gitignore
├── Dockerfile                                         # Multi-stage Docker build
├── docker-compose.yaml                                # App + MySQL orchestration
├── pom.xml                                            # Maven build config
├── mvnw / mvnw.cmd                                    # Maven Wrapper
└── README.md
```
