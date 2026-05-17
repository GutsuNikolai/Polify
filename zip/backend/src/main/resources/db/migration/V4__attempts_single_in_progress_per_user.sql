-- Enforce: a user can have only one IN_PROGRESS attempt across all surveys.
CREATE UNIQUE INDEX IF NOT EXISTS ux_attempts_user_in_progress_global
  ON attempts(user_id)
  WHERE status = 'IN_PROGRESS';

