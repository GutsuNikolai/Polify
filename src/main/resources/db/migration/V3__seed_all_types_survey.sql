-- Seed survey to validate end-to-end flow across all question types.
-- 2 questions per type: TEXT, RADIO, CHECKBOX, SELECT, PRIORITY

-- Create a dedicated seed owner if missing.
INSERT INTO users (login, password_hash, email, phone_number, full_name, gender, birth_date, country, city, is_verified, last_active_at, created_at)
SELECT
  'seed_owner_all_types',
  'seed',
  NULL,
  '+37370000001',
  'Seed Owner',
  'MALE',
  NULL,
  'MD',
  'Chisinau',
  true,
  now(),
  now()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'seed_owner_all_types');

-- Create survey if missing.
INSERT INTO surveys (title, description, reward_amount_bani, target_completions, created_by_user_id, created_at)
SELECT
  'E2E All Types',
  'Survey for automated/manual E2E validation (all question types).',
  900,
  999,
  u.id,
  now()
FROM users u
WHERE u.login = 'seed_owner_all_types'
  AND NOT EXISTS (SELECT 1 FROM surveys s WHERE s.title = 'E2E All Types');

-- Create 10 required questions (positions 1..10).
WITH s AS (
  SELECT id AS survey_id FROM surveys WHERE title = 'E2E All Types' ORDER BY id DESC LIMIT 1
)
INSERT INTO questions (survey_id, type, text, position, is_required, created_at)
SELECT s.survey_id, q.type, q.text, q.position, true, now()
FROM s
JOIN (VALUES
  ('TEXT',     'E2E TEXT 1: Enter a short text', 1),
  ('TEXT',     'E2E TEXT 2: Enter another short text', 2),

  ('RADIO',    'E2E RADIO 1: Pick one', 3),
  ('RADIO',    'E2E RADIO 2: Pick one', 4),

  ('CHECKBOX', 'E2E CHECKBOX 1: Pick one or more', 5),
  ('CHECKBOX', 'E2E CHECKBOX 2: Pick one or more', 6),

  ('SELECT',   'E2E SELECT 1: Choose one', 7),
  ('SELECT',   'E2E SELECT 2: Choose one', 8),

  ('PRIORITY', 'E2E PRIORITY 1: Rank all options', 9),
  ('PRIORITY', 'E2E PRIORITY 2: Rank all options', 10)
) AS q(type, text, position) ON true
ON CONFLICT DO NOTHING;

-- Options: RADIO 1
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'E2E RADIO 1: Pick one'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('Radio 1 - A', 'A', 1),
  ('Radio 1 - B', 'B', 2),
  ('Radio 1 - C', 'C', 3)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

-- Options: RADIO 2
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'E2E RADIO 2: Pick one'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('Radio 2 - A', 'A', 1),
  ('Radio 2 - B', 'B', 2),
  ('Radio 2 - C', 'C', 3)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

-- Options: CHECKBOX 1
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'E2E CHECKBOX 1: Pick one or more'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('Checkbox 1 - A', 'A', 1),
  ('Checkbox 1 - B', 'B', 2),
  ('Checkbox 1 - C', 'C', 3)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

-- Options: CHECKBOX 2
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'E2E CHECKBOX 2: Pick one or more'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('Checkbox 2 - A', 'A', 1),
  ('Checkbox 2 - B', 'B', 2),
  ('Checkbox 2 - C', 'C', 3)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

-- Options: SELECT 1
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'E2E SELECT 1: Choose one'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('Select 1 - A', 'A', 1),
  ('Select 1 - B', 'B', 2),
  ('Select 1 - C', 'C', 3)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

-- Options: SELECT 2
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'E2E SELECT 2: Choose one'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('Select 2 - A', 'A', 1),
  ('Select 2 - B', 'B', 2),
  ('Select 2 - C', 'C', 3)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

-- Options: PRIORITY 1
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'E2E PRIORITY 1: Rank all options'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('Priority 1 - A', 'A', 1),
  ('Priority 1 - B', 'B', 2),
  ('Priority 1 - C', 'C', 3)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

-- Options: PRIORITY 2
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'E2E PRIORITY 2: Rank all options'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('Priority 2 - A', 'A', 1),
  ('Priority 2 - B', 'B', 2),
  ('Priority 2 - C', 'C', 3)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

