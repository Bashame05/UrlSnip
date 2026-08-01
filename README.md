# UrlSnip

A fast, lightweight URL shortener REST API built with Spring Boot and MySQL.

> **Version:** v2.0.0

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
- [Future Improvements](#future-improvements)

---

## Overview

UrlSnip takes long URLs and converts them into compact, shareable short links. When a short link is visited, the user is seamlessly redirected (HTTP 302) to the original URL. Every redirect is tracked, giving you basic click analytics out of the box. You can also set custom aliases, configure expiration dates, and generate QR codes for any short link.

---

## Features

- **Shorten URLs** — Generate a unique 7-character short code for any long URL using NanoID.
- **Custom Aliases** — Choose your own short code (3–7 alphanumeric characters, e.g., `/mylink`) instead of a randomly generated one. Prohibited system aliases are blocked automatically.
- **URL Expiration** — Set an optional expiry duration (up to 183 days). Expired links return `410 Gone`.
- **QR Code Generation** — Generate a 300×300 PNG QR code for any short URL on the fly.
- **Instant Redirect** — Visit a short URL (by short code or custom alias) and get redirected to the original destination via HTTP 302.
- **Click Analytics** — Track total click count, creation time, expiry time, and last accessed time for each short URL.
- **Duplicate Detection** — Submitting the same long URL returns the existing short URL instead of creating a duplicate.
- **Request Validation** — Input payloads are validated using Jakarta Bean Validation (e.g., expiry range constraints).
- **Structured Error Responses** — All errors return a consistent JSON format with `timestamp`, `status`, `error`, and `message`.
- **Global Exception Handling** — Clean error responses for invalid aliases, missing URLs, and expired links.
- **Dockerised Deployment** — Ship the app and MySQL together with a single `docker-compose up`.

---

## Tech Stack

| Layer        | Technology                     |
|--------------|--------------------------------|
| Language     | Java 21                        |
| Framework    | Spring Boot 4.1.0              |
| Web          | Spring WebMVC                  |
| Persistence  | Spring Data JPA + Hibernate    |
| Database     | MySQL                          |
| Validation   | Jakarta Bean Validation        |
| QR Codes     | ZXing (core + javase) 3.5.3    |
| ID Generator | JNanoID 2.0.0                  |
| Boilerplate  | Lombok                         |
| Build Tool   | Maven                          |
| Container    | Docker + Docker Compose        |

---

## Architecture

The project follows a clean **layered architecture**:

```
Controller  →  Service  →  Repository  →  Database
     ↑             ↑
    DTOs        Mappers
                   ↑
                 Utils
```

| Package        | Responsibility                                                           |
|----------------|--------------------------------------------------------------------------|
| `controller`   | REST endpoints — receives requests and returns responses                 |
| `service`      | Business logic — URL shortening, redirection, analytics, QR generation   |
| `repository`   | Data access — Spring Data JPA interface for `UrlMapping` entity          |
| `model`        | JPA entity — `UrlMapping` table mapping                                  |
| `dto`          | Data Transfer Objects — request/response/error records                   |
| `mapper`       | Entity-to-DTO converters with base URL prefixing                         |
| `utils`        | Utility components — `ShortUrlBuilder` for constructing full short URLs   |
| `exception`    | Custom exceptions and global exception handler                           |

---

## API Endpoints

### 1. Shorten a URL

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
  "createdAt": "2026-07-29T19:30:00",
  "expiresIn": "2026-08-28T19:30:00"
}
```

---

### 2. Redirect to Original URL

```
GET /{identifier}
```

The `identifier` can be either the generated short code or a custom alias.

**Response** `302 Found` — redirects to the original long URL.

**Example:**
```
GET /mylink  →  302 redirect to https://www.example.com/some/very/long/path
```

If the identifier does not exist, returns `404 Not Found`. If the URL has expired, returns `410 Gone`.

---

### 3. Get Analytics

```
GET /api/urls/{identifier}/analytics
```

**Response** `200 OK`:
```json
{
  "longUrl": "https://www.example.com/some/very/long/path",
  "shortUrl": "http://localhost:7000/mylink",
  "createdAt": "2026-07-29T19:30:00",
  "lastAccessed": "2026-07-29T19:45:12",
  "expiresAt": "2026-08-28T19:30:00",
  "clickCount": 42
}
```

---

### 4. Generate QR Code

```
GET /api/urls/{identifier}/qrcode
```

**Response** `200 OK` — returns a **300×300 PNG image** of a QR code that encodes the original long URL.

If the URL has expired, returns `410 Gone`.

---

### Error Response Format

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-07-29T14:15:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "No such URL or alias found"
}
```

| Status | Condition                     |
|--------|-------------------------------|
| `404`  | Short code or alias not found |
| `409`  | Custom alias is unavailable   |
| `410`  | URL has expired               |

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
   ```

   > `ABSOLUTE_URL` is the base URL prefix used when returning short URLs in API responses.

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

---

## Project Structure

```
url-shortner/
├── src/
│   ├── main/
│   │   ├── java/com/makar/UrlSnip/
│   │   │   ├── UrlSnip.java                     # Application entry point
│   │   │   ├── controller/
│   │   │   │   └── UrlController.java           # REST API endpoints
│   │   │   ├── service/
│   │   │   │   └── UrlService.java              # Core business logic
│   │   │   ├── repository/
│   │   │   │   └── UrlRepository.java           # JPA data access
│   │   │   ├── model/
│   │   │   │   └── UrlMapping.java              # JPA entity
│   │   │   ├── dto/
│   │   │   │   ├── UrlRequestDto.java           # Incoming request payload
│   │   │   │   ├── UrlResponseDto.java          # Shorten response payload
│   │   │   │   ├── UrlAnalyticsDto.java         # Analytics response payload
│   │   │   │   └── ErrorResponseDto.java        # Structured error payload
│   │   │   ├── mapper/
│   │   │   │   ├── UrlResponseMapper.java       # Entity → Response DTO
│   │   │   │   └── UrlAnalyticsMapper.java      # Entity → Analytics DTO
│   │   │   ├── utils/
│   │   │   │   └── ShortUrlBuilder.java         # Builds full short URL (alias-aware)
│   │   │   └── exception/
│   │   │       ├── NoSuchUrlException.java      # Custom 404 exception
│   │   │       ├── AliasNotAllowedException.java# Custom 409 exception
│   │   │       ├── UrlExpiredException.java     # Custom 410 exception
│   │   │       └── GlobalExceptionHandler.java  # Centralized error handling
│   │   └── resources/
│   │       └── application.yaml                 # App configuration
│   └── test/
├── .env                                          # Environment variables (git-ignored)
├── .gitignore
├── Dockerfile                                    # Multi-stage Docker build
├── docker-compose.yaml                           # App + MySQL orchestration
├── pom.xml                                       # Maven build config
├── mvnw / mvnw.cmd                               # Maven Wrapper
└── README.md
```

---

## Future Improvements

| Feature                                    | Description                                                                                                           |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| **Authentication & Authorization**         | Integrate Spring Security and JWT for user registration, login, and token-based access control.                       |
| **User-Owned URLs**                        | Associate shortened URLs with authenticated users, enabling secure access control over their own links.               |
| **Personal Analytics Dashboard**           | Provide per-user analytics — URLs created, total redirects, QR code generations, and active/expired URL counts.       |
| **Favorite URLs**                          | Allow users to mark URLs as favorites for quick access and management.                                                |
| **Rate Limiting**                          | Implement rate limiting to prevent API abuse and ensure fair usage across all consumers.                              |
