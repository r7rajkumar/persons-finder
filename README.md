# Persons Finder — Backend Challenge (AI-Augmented Edition)

A Spring Boot REST API that helps users find people nearby. Built as a senior backend engineering assessment demonstrating DDD architecture, AI bio generation with prompt injection protection, Haversine distance calculation, and production-ready engineering practices.

---

## Technology Stack

| Component | Version |
|---|---|
| Java | 21 LTS (Eclipse Temurin) |
| Spring Boot | 3.5.3 |
| Kotlin | 2.1.21 |
| Gradle | 8.13 |
| Database | H2 (in-memory) |
| ORM | Spring Data JPA / Hibernate 6.x |
| API Docs | springdoc-openapi 2.8.9 (OpenAPI 3) |
| Testing | JUnit 5 / Spring Boot Test / Mockito |

---

## API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/persons` | Create a person — triggers AI bio generation |
| `PUT` | `/api/v1/persons/{id}/location` | Update a person's current location |
| `GET` | `/api/v1/persons/nearby` | Find people within a radius (km), sorted by distance |

---

## Running the Application

### Prerequisites

- Java 21 installed ([Eclipse Temurin](https://adoptium.net/))
- No other dependencies — H2 runs in-memory, no Docker required for local dev

### Build & Run

```bash
# Build and run all tests
./gradlew clean build

# Start the application
./gradlew bootRun
```

The application starts on **http://localhost:8080**.

### Verify startup

```bash
curl http://localhost:8080/api/v1/persons
# → 200 OK
```

### Swagger UI

Open **http://localhost:8080/swagger-ui.html** in a browser to explore and test all endpoints interactively.

### H2 Console (development)

Open **http://localhost:8080/h2-console** — use JDBC URL `jdbc:h2:mem:testdb`, username `sa`, password `password`.

---

## Docker

```bash
# Build image
docker build -t persons-finder .

# Run container
docker run -p 8080:8080 persons-finder
```

---

## Project Structure

```
src/main/kotlin/com/persons/finder/
├── ApplicationStarter.kt
├── presentation/          # Controllers, DTOs, exception handlers
├── application/           # Use-case orchestration (PersonFacade)
├── domain/
│   ├── services/          # PersonsService, LocationsService interfaces + impls
│   ├── ai/                # AiBioService interface
│   ├── util/              # HaversineCalculator
│   └── exception/         # Domain exceptions
└── infrastructure/
    ├── persistence/       # JPA entities, Spring Data repositories
    ├── ai/                # MockAiBioService, PromptSanitiser
    └── config/            # OpenAPI config
```

---

## Security

See [SECURITY.md](SECURITY.md) for details on:
- Prompt injection protection strategy
- PII and LLM privacy risks
- Architecture recommendations for high-security environments

---

## AI Usage Log

See [AI_LOG.md](AI_LOG.md) for documentation of AI-assisted development interactions during this project.

---

## Assessment Requirements

### Core

- [x] `POST /persons` — create person with AI-generated bio
- [x] `PUT /persons/{id}/location` — update location
- [x] `GET /persons/nearby` — proximity search with Haversine, sorted by distance

### AI & Security

- [x] AI bio generation service with mock implementation (no external API required)
- [x] Prompt injection safeguard before sending user input to AI
- [x] `AI_LOG.md`
- [x] `SECURITY.md`

### Engineering Quality

- [x] Controller / Service / Repository layered architecture
- [x] DDD principles
- [x] Input validation (Bean Validation)
- [x] Global exception handling
- [x] OpenAPI / Swagger documentation
- [x] Unit tests
- [x] Docker support
- [x] GitHub Actions CI/CD pipeline

### Bonus

- [ ] 1M record seed + `nearby` benchmark
