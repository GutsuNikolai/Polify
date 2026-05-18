-- Mark seed_admin as moderator for local MVP demo.
UPDATE users
SET role = 'MODERATOR'
WHERE login = 'seed_admin';

