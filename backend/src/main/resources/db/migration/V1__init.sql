CREATE TABLE users (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    display_name    TEXT NOT NULL,
    created_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE user_profiles (
    user_id                         INTEGER PRIMARY KEY,
    typical_cycle_length_days       INTEGER NOT NULL,
    typical_period_duration_days    INTEGER NOT NULL,
    last_period_start_date          TEXT NOT NULL,
    onboarding_completed_at         TEXT,
    updated_at                      TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE period_logs (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL,
    start_date      TEXT NOT NULL,
    end_date        TEXT,
    flow_intensity  INTEGER,
    notes           TEXT,
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE (user_id, start_date)
);

CREATE INDEX idx_period_logs_user_start ON period_logs (user_id, start_date DESC);

CREATE TABLE symptom_catalog (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    code        TEXT NOT NULL UNIQUE,
    label       TEXT NOT NULL,
    category    TEXT NOT NULL,
    active      INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE symptom_entries (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL,
    log_date        TEXT NOT NULL,
    symptom_id      INTEGER NOT NULL,
    severity        INTEGER NOT NULL,
    period_log_id   INTEGER,
    notes           TEXT,
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (symptom_id) REFERENCES symptom_catalog(id),
    FOREIGN KEY (period_log_id) REFERENCES period_logs(id),
    UNIQUE (user_id, log_date, symptom_id)
);

CREATE INDEX idx_symptom_entries_user_date ON symptom_entries (user_id, log_date DESC);

CREATE TABLE user_cycle_stats (
    user_id                 INTEGER PRIMARY KEY,
    sample_size             INTEGER NOT NULL DEFAULT 0,
    avg_cycle_length        REAL,
    cycle_length_stddev     REAL,
    last_period_start       TEXT,
    avg_period_duration     REAL,
    period_duration_stddev  REAL,
    computed_at             TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
