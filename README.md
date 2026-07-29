# UrlSnip

A fast, lightweight URL shortener REST API built with Spring Boot and MySQL.

> **Version:** v1.0.0

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
- [Environment Variables](#environment-variables)
- [Project Structure](#project-structure)
- [Future Improvements](#future-improvements-(v2))

---

## Overview

UrlSnip takes long URLs and converts them into compact, shareable short links. When a short link is visited, the user is seamlessly redirected (HTTP 302) to the original URL. Every redirect is tracked, giving you basic click analytics out of the box.

---

## Features

- **Shorten URLs** — Generate a unique 7-character short code for any long URL using NanoID.
- **Instant Redirect** — Visit a short URL and get redirected to the original destination via HTTP 302.
- **Click Analytics** — Track total click count, creation time, and last accessed time for each short URL.
- **Duplicate Detection** — Submitting the same long URL returns the existing short URL instead of creating a duplicate.
- **Global Exception Handling** — Clean error responses for invalid or missing short URLs.

---

## Tech Stack

| Layer        | Technology                     |
|--------------|--------------------------------|
| Language     | Java 21                        |
| Framework    | Spring Boot 4.1.0              |
| Web          | Spring WebMVC                  |
| Persistence  | Spring Data JPA + Hibernate    |
| Database     | MySQL                          |
| ID Generator | JNanoID 2.0.0                  |
| Boilerplate  | Lombok                         |
| Build Tool   | Maven                          |

---

## Architecture

The project follows a clean **layered architecture**:

```
Controller  →  Service  →  Repository  →  Database
     ↑             ↑
    DTOs        Mappers
```

| Package        | Responsibility                                                     |
|----------------|--------------------------------------------------------------------|
| `controller`   | REST endpoints — receives requests and returns responses           |
| `service`      | Business logic — URL shortening, redirection, analytics            |
| `repository`   | Data access — Spring Data JPA interface for `UrlMapping` entity    |
| `model`        | JPA entity — `UrlMapping` table mapping                            |
| `dto`          | Data Transfer Objects — request/response records                   |
| `mapper`       | Entity-to-DTO converters with base URL prefixing                   |
| `exception`    | Custom exceptions and global exception handler                     |

---

## API Endpoints

### 1. Shorten a URL

```
POST /api/urls
```

**Request Body:**
```json
{
  "longUrl": "https://www.example.com/some/very/long/path"
}
```

**Response** `200 OK`:
```json
{
  "longUrl": "https://www.example.com/some/very/long/path",
  "shortUrl": "http://localhost:7000/aB3xK9q",
  "createdAt": "2026-07-29T19:30:00"
}
```

---

### 2. Redirect to Original URL

```
GET /{shortCode}
```

**Response** `302 Found` — redirects to the original long URL.

**Example:**
```
GET /aB3xK9q  →  302 redirect to https://www.example.com/some/very/long/path
```

If the short code does not exist, returns `404 Not Found` with an error message.

---

### 3. Get Analytics

```
GET /api/urls/{shortCode}/analytics
```

**Response** `200 OK`:
```json
{
  "longUrl": "https://www.example.com/some/very/long/path",
  "shortUrl": "http://localhost:7000/aB3xK9q",
  "createdAt": "2026-07-29T19:30:00",
  "lastAccessed": "2026-07-29T19:45:12",
  "clickCount": 42
}
```

---

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+** (or use the included Maven Wrapper)
- **MySQL 8.0+**

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
   DB_USER=root
   DB_PASSWORD=your_password
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

---

## Environment Variables

| Variable       | Description                                      | Example                              |
|----------------|--------------------------------------------------|--------------------------------------|
| `DB_URL`       | JDBC connection string for the MySQL database    | `jdbc:mysql://localhost:3306/urls`    |
| `DB_USER`      | MySQL username                                   | `root`                               |
| `DB_PASSWORD`  | MySQL password                                   | `your_password`                      |
| `ABSOLUTE_URL` | Base URL prefix for generated short links        | `localhost:7000/`                     |

---

## Project Structure

```
url-shortner/
├── src/
│   ├── main/
│   │   ├── java/com/makar/urlshortner/
│   │   │   ├── UrlShortnerApplication.java      # Application entry point
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
│   │   │   │   └── UrlAnalyticsDto.java         # Analytics response payload
│   │   │   ├── mapper/
│   │   │   │   ├── UrlResponseMapper.java       # Entity → Response DTO
│   │   │   │   └── UrlAnalyticsMapper.java      # Entity → Analytics DTO
│   │   │   └── exception/
│   │   │       ├── NoSuchUrlException.java      # Custom 404 exception
│   │   │       └── GlobalExceptionHandler.java  # Centralized error handling
│   │   └── resources/
│   │       └── application.yaml                 # App configuration
│   └── test/
├── .env                                          # Environment variables (git-ignored)
├── .gitignore
├── pom.xml                                       # Maven build config
├── mvnw / mvnw.cmd                               # Maven Wrapper
└── README.md
```

---

## Future Improvements (v2)

| Feature                      | Description                                                                                                  |
|------------------------------|--------------------------------------------------------------------------------------------------------------|
| **Structured Error Responses** | Replace plain-text error bodies with a consistent JSON error format (status, message, timestamp, path).     |
| **Custom Aliases**             | Allow users to choose their own short code (e.g., `/my-link`) instead of a randomly generated one.         |
| **URL Expiration**             | Support optional TTL / expiry dates so short links automatically become invalid after a set duration.       |
| **Dockerisation**              | Provide a `Dockerfile` and `docker-compose.yml` to spin up the app and MySQL with a single command.        |


