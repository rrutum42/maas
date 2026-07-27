# Period Tracker (MaaS)

A full-stack period tracking application with cycle prediction, symptom logging, and a visual calendar dashboard. The backend provides a REST API with rolling-average-based prediction; the frontend is a single-page vanilla JS app.

---

## Technologies

**Backend**

| | |
|---|---|
| **Runtime** | Java 17, Spring Boot 3.4.1 |
| **Database** | SQLite 3 via HikariCP + Hibernate 6 |
| **Migrations** | Flyway 10 (SQL files in `db/migration/`) |
| **Validation** | Jakarta Bean Validation (`@NotNull`, `@Min`, `@Max`) |
| **Build** | Maven (Spring Boot parent POM) |

**Frontend**
- Vanilla JavaScript (ES modules-free, no framework)
- Plain HTML + CSS (custom properties, responsive grid, no build step)
- Served directly from the filesystem or any static server

---

## Database Schema

Six tables managed by Flyway migrations.

### `users`
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK | Auto-increment |
| display_name | TEXT | NOT NULL |
| created_at | TEXT | ISO-8601 timestamp |

### `user_profiles`
| Column | Type | Notes |
|---|---|---|
| user_id | INTEGER PK | FK → users.id |
| typical_cycle_length_days | INTEGER | NOT NULL |
| typical_period_duration_days | INTEGER | NOT NULL |
| last_period_start_date | TEXT | ISO date |
| onboarding_completed_at | TEXT | Nullable timestamp |
| updated_at | TEXT | Timestamp |

### `period_logs`
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK | Auto-increment |
| user_id | INTEGER | FK → users.id, NOT NULL |
| start_date | TEXT | ISO date, NOT NULL |
| end_date | TEXT | Nullable ISO date |
| flow_intensity | INTEGER | 1–5 scale, nullable |
| notes | TEXT | Free text |
| created_at / updated_at | TEXT | Timestamps |
| *Unique* | (user_id, start_date) | One log per start date |

### `symptom_catalog`
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK | Auto-increment |
| code | TEXT | Unique, e.g. `CRAMPS` |
| label | TEXT | Display name |
| category | TEXT | e.g. `physical`, `mood`, `energy` |
| active | INTEGER | Boolean flag |

### `symptom_entries`
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK | Auto-increment |
| user_id | INTEGER | FK → users.id |
| log_date | TEXT | ISO date |
| symptom_id | INTEGER | FK → symptom_catalog.id |
| severity | INTEGER | 1–5 scale |
| period_log_id | INTEGER | Nullable FK → period_logs.id |
| notes | TEXT | Free text |
| *Unique* | (user_id, log_date, symptom_id) | One entry per symptom per day |

### `user_cycle_stats`
| Column | Type | Notes |
|---|---|---|
| user_id | INTEGER PK | FK → users.id |
| sample_size | INTEGER | Number of cycles observed |
| avg_cycle_length | REAL | Mean days between period starts |
| cycle_length_stddev | REAL | Population std dev |
| avg_period_duration | REAL | Mean days from start to end |
| period_duration_stddev | REAL | Population std dev |
| last_period_start | TEXT | ISO date |
| computed_at | TEXT | Timestamp |

**Migrations**
- `V1__init.sql` — All six tables + indexes
- `V2__seed.sql` — Sample users (Ava, Maya, Priya), period logs, symptoms
- `V3__seed_pre_onboarding_user.sql` — Additional onboarding user

---

## API Reference

All endpoints are prefixed with `/v1`. CORS is enabled for all origins on `/v1/**`.

### `GET /v1/users`

List all users with their onboarding status.

Response `200`:
```json
[
  { "id": 1, "displayName": "Ava Sharma", "onboardingCompleted": true },
  { "id": 2, "displayName": "Maya Iyer",  "onboardingCompleted": true }
]
```

### `PUT /v1/profile`

Create or update a user's profile (onboarding).

Request body:
```json
{
  "userId": 1,
  "typicalCycleLengthDays": 28,
  "typicalPeriodDurationDays": 5,
  "lastPeriodStartDate": "2026-07-13"
}
```

Response `200`:
```json
{
  "userId": 1,
  "typicalCycleLengthDays": 28,
  "typicalPeriodDurationDays": 5,
  "lastPeriodStartDate": "2026-07-13",
  "onboardingCompleted": true,
  "onboardingCompletedAt": "2026-07-27T14:30:00Z"
}
```

### `GET /v1/profile?userId={id}`

Get a user's profile.

Response `200` — same shape as PUT. Returns `404` with `NOT_FOUND` error if profile does not exist.

### `POST /v1/periods`

Log a period entry. Idempotent — logging the same `userId` + `startDate` again acts as an update and returns `200` instead of `201`.

Request body:
```json
{
  "userId": 1,
  "startDate": "2026-07-27",
  "endDate": "2026-07-31",
  "flowIntensity": 3,
  "notes": "cramping day 1"
}
```

Response `201` (created) / `200` (updated):
```json
{
  "id": 7,
  "startDate": "2026-07-27",
  "endDate": "2026-07-31",
  "flowIntensity": 3,
  "notes": "cramping day 1",
  "cycleLengthDays": 28
}
```

### `GET /v1/periods?userId={id}&from=&to=&cursor=&size=`

List period logs with cursor-based or date-range pagination.

| Param | Type | Description |
|---|---|---|
| userId | Long | Required |
| from / to | ISO date | Optional date range filter |
| cursor | ISO date | Optional cursor (returns logs before this date) |
| size | int | Page size (default 50, max 200) |

Response `200`:
```json
{
  "data": [
    {
      "id": 6,
      "startDate": "2026-06-29",
      "endDate": "2026-07-03",
      "flowIntensity": 3,
      "notes": null,
      "cycleLengthDays": 28
    }
  ],
  "pagination": { "nextCursor": "2026-06-29", "hasMore": false }
}
```

### `GET /v1/predictions?userId={id}`

Get cycle prediction for a user. Requires onboarding to be completed.

Response `200`:
```json
{
  "nextPeriod": {
    "predictedStartDate": "2026-08-24",
    "confidenceBand": { "earliest": "2026-08-20", "latest": "2026-08-28" },
    "confidenceLevel": 0.68
  },
  "ovulation": {
    "predictedDate": "2026-08-10",
    "fertileWindow": { "start": "2026-08-08", "end": "2026-08-11" }
  },
  "explanation": {
    "method": "rolling_average_with_variance_band",
    "sampleSize": 6,
    "avgCycleLengthDays": 28.3,
    "cycleLengthStdDev": 0.82,
    "onboardingBaselineDays": 28,
    "onboardingPeriodDurationDays": 5,
    "dataSource": "observed",
    "confidenceNote": "Based on your last 6 observed cycles.",
    "lutealPhaseDays": 14,
    "lastPeriodStart": "2026-06-29"
  },
  "computedAt": "2026-07-27T14:30:00Z"
}
```

Errors:
- `404 NOT_FOUND` — user does not exist
- `422 UNPROCESSABLE_ENTITY` — onboarding not completed

### Error response format

All errors return:
```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "User not found with id: 99",
    "details": null
  }
}
```

| HTTP Status | Code | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Invalid input |
| 404 | `NOT_FOUND` | Resource not found |
| 422 | `ONBOARDING_REQUIRED` | Predictions requested before onboarding |

---

## Cycle Prediction Algorithm

The `CyclePredictor` uses a rolling average with variance band. All math is computed from the dates a user logs. Here is exactly how each statistic works, with a worked example.

### Step 1: Compute raw cycle lengths

Every time a user logs a period, the gap between its start date and the *previous* period's start date is one cycle length.

**Example — Ava's logs from seed data:**

| Start date | End date | Days from previous start |
|---|---|---|
| Jan 10 | Jan 14 | — (first entry, no previous) |
| Feb 7 | Feb 11 | 28 days |
| Mar 6 | Mar 10 | 27 days |
| Apr 2 | Apr 6 | 29 days |
| May 1 | May 5 | 28 days |
| May 29 | Jun 2 | 28 days |

Raw cycle lengths: `[28, 27, 29, 28, 28]`

### Step 2: Choose data source

If there are **fewer than 2 observed cycles**, the algorithm uses the onboarding baseline. Otherwise it uses the observed data:

| Condition | Cycle length (`μ`) | Std dev (`σ`) | Source |
|---|---|---|---|
| 0 cycles | User's onboarding value | `onboardingSigma` (4) | `onboarding_baseline` |
| 1 cycle | User's onboarding value | `onboardingSigma` (4) | `onboarding_baseline` |
| ≥ 2 cycles | Rolling mean of recent cycles | Rolling σ (floor 2.0) | `observed` |

Ava has 5 cycles, so the **observed phase** applies.

### Step 3: Rolling window

Only the most recent `windowSize` (default **6**) cycles are used. This keeps predictions responsive to recent changes.

Ava has 5 cycles and `windowSize` is 6, so all 5 are within the window. The effective window is `[28, 27, 29, 28, 28]`.

If Ava had 10 cycles, only the most recent 6 would be used.

### Step 4: Compute the mean (average)

The **mean** is the sum divided by the count:

```
μ = (x₁ + x₂ + ... + xₙ) / n
```

Ava's example:

```
μ = (28 + 27 + 29 + 28 + 28) / 5
μ = 140 / 5
μ = 28.0
```

Ava's average cycle length is exactly **28.0 days**.

### Step 5: Compute the standard deviation

The **population standard deviation** measures how spread out the cycle lengths are:

```
σ = √( ((x₁ - μ)² + (x₂ - μ)² + ... + (xₙ - μ)²) / n )
```

Each term `(x - μ)²` is the **squared deviation** from the mean. A cycle exactly on the mean contributes 0; a cycle far from the mean contributes a lot.

Ava's example:

| Cycle | Value | Deviation from μ=28 | Squared deviation |
|---|---|---|---|
| 1 | 28 | 0 | 0 |
| 2 | 27 | -1 | 1 |
| 3 | 29 | +1 | 1 |
| 4 | 28 | 0 | 0 |
| 5 | 28 | 0 | 0 |

```
σ = √( (0 + 1 + 1 + 0 + 0) / 5 )
σ = √( 2 / 5 )
σ = √0.4
σ ≈ 0.63
```

However, the algorithm enforces a minimum floor of **2.0** to avoid overconfidence. Since 0.63 < 2.0, the reported σ is **2.0**.

### Step 6: Predicted next period

The prediction date is the **anchor** (the last recorded period start) plus the average cycle length:

```
predictedNextStart = anchorDate + round(μ) days
```

Ava's anchor is her last start date (May 29). The rounded mean is 28.

```
predictedNextStart = May 29 + 28 days = June 26
```

### Step 7: Confidence band (1-sigma range)

Instead of a single date, the API returns a **confidence band** from `μ - σ` to `μ + σ`:

```
earliest = anchorDate + round(μ - σ) days
latest   = anchorDate + round(μ + σ) days
```

With σ = 2.0:

```
earliest = May 29 + round(28 - 2) = May 29 + 26 = June 24
latest   = May 29 + round(28 + 2) = May 29 + 30 = June 28
```

The `confidenceLevel` returned is always **0.68** (68%). This comes from the **empirical rule** for normal distributions: ≈68% of values fall within ±1 standard deviation of the mean.

**What this means:** Ava's next period is most likely June 26, and there's roughly a 68% chance it will arrive between June 24 and June 28.

### Step 8: Ovulation estimate

Ovulation is estimated by counting backwards from the predicted next period by the **luteal phase length** (default **14 days**). This is based on the well-established medical finding that the luteal phase (ovulation to next period) is relatively stable at 14 days for most people.

```
predictedOvulation = predictedNextStart - lutealPhaseDays
```

```
predictedOvulation = June 26 - 14 = June 12
```

### Step 9: Fertile window

The fertile window widens when the user's cycle is irregular. The base is 4 days (ovulation day ± 2), which increases when σ exceeds 2:

```
fertileSpread = max(0, ceil(σ) - 1)
fertileStart  = predictedOvulation - (2 + fertileSpread)
fertileEnd    = predictedOvulation + (1 + fertileSpread)
```

**Why the asymmetry:** ovulation itself is imprecise, but sperm can survive up to 5 days in the reproductive tract, so the window extends further before ovulation than after.

- **Regular cycles (σ ≤ 2):** ovulation ± 2 days (4-day window). Ava with σ = 2.0 → `ceil(2.0) - 1 = 1`, so fertile = ovulation − 3 to ovulation + 2 = **6 days**.
- **Irregular example (σ = 3.5):** `ceil(3.5) - 1 = 3`, so fertile = ovulation − 5 to ovulation + 4 = **10 days**.

### Full walkthrough: onboarding phase

Before Ava logs any periods, she completes onboarding with:
- Typical cycle length: 28 days
- Typical period duration: 5 days

The algorithm uses only the onboarding defaults:

```
μ = 28 (from onboarding)
σ = 4 (onboardingSigma)
predictedNextStart = lastPeriodStart + 28 days
earliest = lastPeriodStart + 24 days
latest   = lastPeriodStart + 32 days
confidenceLevel = 0.68
```

The confidence band is much wider (24–32 vs 26–28 in the observed phase) because no real data exists yet.

### Full walkthrough: irregular cycle example

Suppose a user named Sam has these start dates:
- Jan 1 → Feb 5 (35 days)
- Feb 5 → Mar 3 (26 days)
- Mar 3 → Apr 15 (43 days)
- Apr 15 → May 13 (28 days)
- May 13 → Jun 10 (28 days)

```
μ = (35 + 26 + 43 + 28 + 28) / 5 = 32.0
```

Deviations: 35−32=3, 26−32=−6, 43−32=11, 28−32=−4, 28−32=−4

Squared: 9, 36, 121, 16, 16

Variance: (9 + 36 + 121 + 16 + 16) / 5 = 198/5 = 39.6

```
σ = √39.6 ≈ 6.3
```

Since 6.3 > 2.0, the floor does not apply. σ = 6.3.

If Sam's last period started June 10:

```
predictedNext = June 10 + 32 = July 12
earliest = June 10 + round(32 - 6.3) = June 10 + 26 = July 6
latest   = June 10 + round(32 + 6.3) = June 10 + 38 = July 18
```

The band is 12 days wide — reflecting Sam's irregular cycles.

Fertile window:

```
predictedOvulation = July 12 - 14 = June 28
fertileSpread = ceil(6.3) - 1 = 7 - 1 = 6
fertileStart = June 28 - (2 + 6) = June 20
fertileEnd   = June 28 + (1 + 6) = July 5
```

A 16-day fertile window — wider because the irregularity makes ovulation timing less certain.

### Stats stored on every period log

Every time `POST /v1/periods` is called, `recomputeStats()` recalculates:

| Stat | Formula | Meaning |
|---|---|---|
| `avgCycleLength` | Mean of all cycle lengths | Typical gap between period starts |
| `cycleLengthStddev` | Population σ of all cycle lengths | How irregular cycles are |
| `avgPeriodDuration` | Mean of (end date − start date) for all logs | Typical period length in days |
| `periodDurationStddev` | Population σ of all durations | How much period length varies |
| `sampleSize` | Count of cycle lengths used | Number of observed cycles |

These stats are persisted in `user_cycle_stats` and updated incrementally. The prediction algorithm does not use these stored stats directly — it re-computes from the raw dates using the rolling window. The stored stats serve as a quick-reference summary for the user.

### Configuration reference

```yaml
maas:
  prediction:
    window-size: 6           # How many recent cycles to average
    observed-min-sample: 2    # Minimum cycles before leaving onboarding
    onboarding-sigma: 4       # Std dev used during onboarding (higher = wider band)
    luteal-phase-days: 14     # Days from ovulation to next period
```

---

## Frontend

A single-page app in `frontend/` (`index.html`, `style.css`, `app.js`) — no build tools. It connects to `http://localhost:8080/v1` by default (overridable via `localStorage.api_base`).

Features:
- User switcher (seed data includes Ava, Maya, Priya)
- Onboarding form (cycle length, period duration, last period)
- Calendar view with period days, predicted days, and fertile window
- Period logging form with flow intensity selector
- Recent period history list
- Prediction cards showing next period date, ovulation date, and confidence
- Expandable explanation of how predictions are calculated
- Responsive layout (3-column grid, collapses to 1-column on mobile)

---

## Running Locally

```bash
# Backend
cd backend
mvn spring-boot:run
# Starts on http://localhost:8080

# Frontend — open directly in a browser
open frontend/index.html
# or serve with any static server
python3 -m http.server 3000 --directory frontend
```
