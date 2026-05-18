-- Add user role for moderator/admin capabilities.
ALTER TABLE users
  ADD COLUMN role TEXT NOT NULL DEFAULT 'USER'
  CHECK (role IN ('USER','MODERATOR','ADMIN'));

