-- =========================
-- SURVEY ARCHIVE (MVP moderator lifecycle)
-- =========================

ALTER TABLE surveys
  ADD COLUMN is_archived BOOLEAN NOT NULL DEFAULT false,
  ADD COLUMN archived_at TIMESTAMPTZ,
  ADD COLUMN archived_by_user_id BIGINT REFERENCES users(id);

-- Consistency: if archived => archived_at must be set, and archived_by_user_id should be set as well.
ALTER TABLE surveys
  ADD CONSTRAINT ck_surveys_archived_meta
  CHECK (
    (is_archived = false AND archived_at IS NULL AND archived_by_user_id IS NULL)
    OR
    (is_archived = true  AND archived_at IS NOT NULL AND archived_by_user_id IS NOT NULL)
  );

CREATE INDEX ix_surveys_is_archived ON surveys(is_archived);

