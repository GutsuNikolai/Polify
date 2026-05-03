-- Seed data for local development / MVP demo.
-- Creates a seed user (cannot necessarily login) and 2 example surveys.

INSERT INTO users (login, password_hash, email, phone_number, full_name, gender, birth_date, country, city, is_verified, last_active_at, created_at)
VALUES (
  'seed_admin',
  'seed',
  NULL,
  '+37370000000',
  'Seed Admin',
  'MALE',
  NULL,
  'MD',
  'Chisinau',
  true,
  now(),
  now()
)
ON CONFLICT (login) DO NOTHING;

-- Survey 1
INSERT INTO surveys (title, description, reward_amount_bani, target_completions, created_by_user_id, created_at)
SELECT
  'Daily habits',
  'Quick survey about your daily routine.',
  500,
  100,
  u.id,
  now()
FROM users u
WHERE u.login = 'seed_admin'
ON CONFLICT DO NOTHING;

-- Questions for Survey 1
WITH s AS (
  SELECT id AS survey_id FROM surveys WHERE title = 'Daily habits' ORDER BY id DESC LIMIT 1
)
INSERT INTO questions (survey_id, type, text, position, is_required, created_at)
SELECT s.survey_id, q.type, q.text, q.position, q.is_required, now()
FROM s
JOIN (VALUES
  ('TEXT',     'What is your favorite morning drink?', 1, true),
  ('RADIO',    'How many hours do you sleep on average?', 2, true),
  ('CHECKBOX', 'Which activities do you do daily?', 3, false)
) AS q(type, text, position, is_required) ON true
ON CONFLICT DO NOTHING;

-- Options for Survey 1 RADIO question (position 2)
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'How many hours do you sleep on average?'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('< 6',  'LT6', 1),
  ('6-7',  '6_7', 2),
  ('7-8',  '7_8', 3),
  ('> 8',  'GT8', 4)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

-- Options for Survey 1 CHECKBOX question (position 3)
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'Which activities do you do daily?'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('Sport',     'SPORT', 1),
  ('Reading',   'READ',  2),
  ('Meditation','MED',   3),
  ('Gaming',    'GAME',  4)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

-- Survey 2
INSERT INTO surveys (title, description, reward_amount_bani, target_completions, created_by_user_id, created_at)
SELECT
  'Product feedback',
  'Help us improve: choose preferences and priorities.',
  800,
  50,
  u.id,
  now()
FROM users u
WHERE u.login = 'seed_admin'
ON CONFLICT DO NOTHING;

-- Questions for Survey 2
WITH s AS (
  SELECT id AS survey_id FROM surveys WHERE title = 'Product feedback' ORDER BY id DESC LIMIT 1
)
INSERT INTO questions (survey_id, type, text, position, is_required, created_at)
SELECT s.survey_id, q.type, q.text, q.position, q.is_required, now()
FROM s
JOIN (VALUES
  ('SELECT',   'Which platform do you use most?', 1, true),
  ('PRIORITY', 'Rank the features by importance (1 = most important).', 2, true)
) AS q(type, text, position, is_required) ON true
ON CONFLICT DO NOTHING;

-- Options for Survey 2 SELECT question
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'Which platform do you use most?'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('Android', 'ANDROID', 1),
  ('iOS',     'IOS',     2),
  ('Web',     'WEB',     3)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

-- Options for Survey 2 PRIORITY question
WITH q AS (
  SELECT id AS question_id
  FROM questions
  WHERE text = 'Rank the features by importance (1 = most important).'
  ORDER BY id DESC
  LIMIT 1
)
INSERT INTO question_options (question_id, label, value, position, is_active, media_url, created_at)
SELECT q.question_id, o.label, o.value, o.position, true, NULL, now()
FROM q
JOIN (VALUES
  ('Reward size',     'REWARD', 1),
  ('Survey speed',    'SPEED',  2),
  ('Survey variety',  'VAR',    3),
  ('UI convenience',  'UI',     4)
) AS o(label, value, position) ON true
ON CONFLICT DO NOTHING;

