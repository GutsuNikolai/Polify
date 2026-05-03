  -- =========================
  -- USERS
  -- =========================
  CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,

    login           TEXT NOT NULL UNIQUE,
    password_hash   TEXT NOT NULL,

    email           TEXT UNIQUE,
    phone_number    TEXT NOT NULL UNIQUE,

    full_name       TEXT,
    gender          TEXT CHECK (gender IN ('MALE','FEMALE')),
    birth_date      DATE,
    country         TEXT,
    city            TEXT,

    is_verified     BOOLEAN NOT NULL DEFAULT false,
    last_active_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_users_phone
      CHECK (phone_number ~ '^\+[1-9]\d{9,14}$')
  );

  -- =========================
  -- SURVEYS (IMMUTABLE)
  -- =========================
  CREATE TABLE surveys (
    id                   BIGSERIAL PRIMARY KEY,
    title                TEXT NOT NULL,
    description          TEXT,

    reward_amount_bani   SMALLINT NOT NULL
      CHECK (reward_amount_bani BETWEEN 0 AND 9900),

    target_completions   INT NOT NULL
      CHECK (target_completions > 0),

    created_by_user_id   BIGINT NOT NULL REFERENCES users(id),

    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
  );

  -- =========================
  -- QUESTIONS
  -- =========================
  CREATE TABLE questions (
    id          BIGSERIAL PRIMARY KEY,
    survey_id   BIGINT NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,

    type        TEXT NOT NULL
      CHECK (type IN ('RADIO','CHECKBOX','SELECT','TEXT','PRIORITY')),

    text        TEXT NOT NULL,
    position    INT NOT NULL CHECK (position > 0),

    is_required BOOLEAN NOT NULL DEFAULT false,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE (survey_id, position),
    -- Support composite foreign key answers(question_id, survey_id) -> questions(id, survey_id).
    UNIQUE (id, survey_id)
  );

  -- =========================
  -- QUESTION OPTIONS
  -- =========================
  CREATE TABLE question_options (
    id          BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,

    label       TEXT,
    value       TEXT,
    position    INT NOT NULL CHECK (position > 0),

    is_active   BOOLEAN NOT NULL DEFAULT true,

    media_url   TEXT,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE (question_id, position),
    UNIQUE (question_id, value),
    -- Support composite foreign keys referencing (id, question_id).
    UNIQUE (id, question_id)
  );

  -- =========================
  -- ATTEMPTS
  -- =========================
  CREATE TABLE attempts (
    id           BIGSERIAL PRIMARY KEY,
    survey_id    BIGINT NOT NULL REFERENCES surveys(id),
    user_id      BIGINT NOT NULL REFERENCES users(id),

    status       TEXT NOT NULL
      CHECK (status IN ('IN_PROGRESS','COMPLETED','ABANDONED')),

    started_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,

    CONSTRAINT ck_attempts_completed_at
      CHECK (
        (status = 'COMPLETED' AND completed_at IS NOT NULL)
        OR (status <> 'COMPLETED' AND completed_at IS NULL)
      ),

    -- Support composite foreign keys (PostgreSQL requires referenced columns be UNIQUE/PK).
    UNIQUE (id, survey_id),
    UNIQUE (id, user_id)
  );

  -- Allow multiple attempts per user+survey, but:
  -- 1) only one in-progress attempt at a time
  -- 2) only one completed attempt ever (after that, they can't redo)
  CREATE UNIQUE INDEX ux_attempts_user_survey_in_progress
    ON attempts(user_id, survey_id)
    WHERE status = 'IN_PROGRESS';

  CREATE UNIQUE INDEX ux_attempts_user_survey_completed
    ON attempts(user_id, survey_id)
    WHERE status = 'COMPLETED';

  -- =========================
  -- ANSWERS (HEADER)
  -- =========================
  CREATE TABLE answers (
    id           BIGSERIAL PRIMARY KEY,

    attempt_id   BIGINT NOT NULL,
    survey_id    BIGINT NOT NULL,
    question_id  BIGINT NOT NULL,

    answered_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE (attempt_id, question_id),
    UNIQUE (id, question_id),

    FOREIGN KEY (attempt_id, survey_id)
      REFERENCES attempts(id, survey_id),

    FOREIGN KEY (question_id, survey_id)
      REFERENCES questions(id, survey_id)
  );

  -- =========================
  -- ANSWER TEXT
  -- =========================
  CREATE TABLE answer_text (
    answer_id   BIGINT PRIMARY KEY
      REFERENCES answers(id) ON DELETE CASCADE,

    value_text  VARCHAR(500) NOT NULL
  );

  -- =========================
  -- ANSWER OPTIONS
  -- =========================
  CREATE TABLE answer_options (
    answer_id   BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    option_id   BIGINT NOT NULL,

    PRIMARY KEY (answer_id, option_id),

    FOREIGN KEY (answer_id, question_id)
      REFERENCES answers(id, question_id),

    FOREIGN KEY (option_id, question_id)
      REFERENCES question_options(id, question_id)
  );

  -- =========================
  -- ANSWER PRIORITY
  -- =========================
  CREATE TABLE answer_priority (
    answer_id   BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    option_id   BIGINT NOT NULL,
    rank        INT NOT NULL CHECK (rank > 0),

    PRIMARY KEY (answer_id, option_id),
    UNIQUE (answer_id, rank),

    FOREIGN KEY (answer_id, question_id)
      REFERENCES answers(id, question_id),

    FOREIGN KEY (option_id, question_id)
      REFERENCES question_options(id, question_id)
  );

  -- =========================
  -- LEDGER
  -- =========================
  CREATE TABLE ledger_entries (
    id          BIGSERIAL PRIMARY KEY,

    attempt_id  BIGINT NOT NULL UNIQUE,
    user_id     BIGINT NOT NULL REFERENCES users(id),

    amount_bani SMALLINT NOT NULL
      CHECK (amount_bani BETWEEN 0 AND 9900),

    currency    CHAR(3) NOT NULL DEFAULT 'MDL'
      CHECK (currency = 'MDL'),

    status      TEXT NOT NULL DEFAULT 'CREATED'
      CHECK (status IN ('CREATED','CONFIRMED','FAILED')),

    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    FOREIGN KEY (attempt_id, user_id)
      REFERENCES attempts(id, user_id)
  );
