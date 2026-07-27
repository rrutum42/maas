# MaaS Backend

Menstrual health as a Service — a Spring Boot 3.4 microservice with period tracking, cycle predictions, and an extensible prediction engine.

## Prerequisites

- **Java 21** or later
- **Maven 3.9+** (or use the wrapper)
- **Node.js 18+** (for the frontend at `../frontend/`)

## Quick Start

### 1. Configure Maven (if not already in PATH)

Set env vars pointing to Maven 3.9.16:

```shell
setx MAVEN_HOME "C:\tools\apache-maven-3.9.16"
setx PATH "%PATH%;C:\tools\apache-maven-3.9.16\bin"
```

Restart your terminal, then verify:

```shell
mvn --version
```

### 2. Start the Backend

```shell
cd backend
mvn spring-boot:run
```

The server starts on `http://localhost:8080`. On first launch it:
1. Creates `./data/` directory
2. Runs Flyway migrations (creates tables, seeds demo data)
3. Creates an SQLite database at `./data/maas.db`

Demo users seeded automatically (passwords not required — auth is bypassed):

| ID | Name | Periods Logged |
|----|------|----------------|
| 1 | Priya Sharma | 6 (regular, with symptoms) |
| 2 | Ananya Patel | 3 (sparse data) |
| 3 | Maria Lopez | 0 (needs onboarding) |
| 4 | Riya Menon | 0 (pre-onboarding) |

### 3. Start the Frontend (optional)

```shell
cd frontend
python -m http.server 3000
```

Then open `http://localhost:3000`.

> **Note:** The frontend connects to `http://localhost:8080` by default. No proxy or CORS plugin needed — the backend already permits all origins on `/v1/**`.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/v1/users` | List all users |
| `PUT` | `/v1/profile` | Create or update user profile (onboarding) |
| `GET` | `/v1/profile` | Get profile for a user (`?userId=N`) |
| `POST` | `/v1/periods` | Log a period |
| `GET` | `/v1/periods` | List periods (`?userId=N&page=0&size=20`) |
| `GET` | `/v1/predictions` | Get predictions (`?userId=N`) |

### Example: Log a Period

```shell
curl -X POST http://localhost:8080/v1/periods \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "startDate": "2026-07-15",
    "endDate": "2026-07-20",
    "flowIntensity": 3,
    "notes": "Cramps on day 1"
  }'
```

## Running Tests

```shell
cd backend
mvn test
```

Tests cover:
- `CyclePredictorTest` — zero logs, single log, regular cycles, irregular cycles, ovulation offset, excluded cycles

## Build & Package

```shell
mvn clean package -DskipTests
java -jar target/maas.jar
```
