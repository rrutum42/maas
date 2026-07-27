INSERT INTO users (id, display_name, created_at) VALUES
    (1, 'Ava Sharma', strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (2, 'Maya Iyer',  strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (3, 'Priya Nair', strftime('%Y-%m-%dT%H:%M:%SZ', 'now'));

INSERT INTO user_profiles (
    user_id, typical_cycle_length_days, typical_period_duration_days,
    last_period_start_date, onboarding_completed_at, updated_at
) VALUES
    (1, 28, 5, date('now', '-28 days'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (2, 31, 6, date('now', '-35 days'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (3, 28, 4, date('now', '-12 days'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now'));

-- Ava: cycle lengths 28, 27, 29, 28, 28
INSERT INTO period_logs (user_id, start_date, end_date, flow_intensity, notes, created_at, updated_at) VALUES
    (1, date('now', '-168 days'), date('now', '-164 days'), 3, NULL,              strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (1, date('now', '-140 days'), date('now', '-136 days'), 4, NULL,              strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (1, date('now', '-113 days'), date('now', '-109 days'), 3, NULL,              strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (1, date('now',  '-84 days'), date('now',  '-80 days'), 2, 'lighter flow',    strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (1, date('now',  '-56 days'), date('now',  '-51 days'), 4, NULL,              strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (1, date('now',  '-28 days'), date('now',  '-24 days'), 3, NULL,              strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now'));

-- Maya: cycle lengths 35, 25, 32, 33
INSERT INTO period_logs (user_id, start_date, end_date, flow_intensity, notes, created_at, updated_at) VALUES
    (2, date('now', '-160 days'), date('now', '-154 days'), 4, NULL, strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (2, date('now', '-125 days'), date('now', '-120 days'), 3, NULL, strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (2, date('now', '-100 days'), date('now',  '-95 days'), 5, NULL, strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (2, date('now',  '-68 days'), date('now',  '-62 days'), 3, NULL, strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    (2, date('now',  '-35 days'), date('now',  '-30 days'), 4, NULL, strftime('%Y-%m-%dT%H:%M:%SZ', 'now'), strftime('%Y-%m-%dT%H:%M:%SZ', 'now'));

INSERT INTO symptom_catalog (code, label, category, active) VALUES
    ('CRAMPS',      'Cramps',      'physical', 1),
    ('HEADACHE',    'Headache',    'physical', 1),
    ('BLOATING',    'Bloating',    'physical', 1),
    ('BACK_PAIN',   'Back pain',   'physical', 1),
    ('NAUSEA',      'Nausea',      'physical', 1),
    ('ACNE',        'Acne',        'physical', 1),
    ('FATIGUE',     'Fatigue',     'energy',   1),
    ('MOOD_SWINGS', 'Mood swings', 'mood',     1),
    ('ANXIETY',     'Anxiety',     'mood',     1);

INSERT INTO symptom_entries (user_id, log_date, symptom_id, severity, period_log_id, notes, created_at)
SELECT 1, date('now', '-27 days'), (SELECT id FROM symptom_catalog WHERE code = 'CRAMPS'), 3,
       (SELECT id FROM period_logs WHERE user_id = 1 AND start_date = date('now', '-28 days')),
       NULL, strftime('%Y-%m-%dT%H:%M:%SZ', 'now');

INSERT INTO symptom_entries (user_id, log_date, symptom_id, severity, period_log_id, notes, created_at)
SELECT 1, date('now', '-27 days'), (SELECT id FROM symptom_catalog WHERE code = 'FATIGUE'), 2,
       (SELECT id FROM period_logs WHERE user_id = 1 AND start_date = date('now', '-28 days')),
       NULL, strftime('%Y-%m-%dT%H:%M:%SZ', 'now');

INSERT INTO symptom_entries (user_id, log_date, symptom_id, severity, period_log_id, notes, created_at)
SELECT 1, date('now', '-14 days'), (SELECT id FROM symptom_catalog WHERE code = 'HEADACHE'), 2,
       NULL, 'mid-cycle', strftime('%Y-%m-%dT%H:%M:%SZ', 'now');
