ALTER TABLE followers
    ADD COLUMN IF NOT EXISTS follower_id uuid;

ALTER TABLE followers
    ADD COLUMN IF NOT EXISTS following_id uuid;

UPDATE followers
SET follower_id = COALESCE(follower_id, follower, user_id)
WHERE follower_id IS NULL;

UPDATE followers
SET following_id = COALESCE(following_id, following)
WHERE following_id IS NULL;

ALTER TABLE followers
    ALTER COLUMN follower_id SET NOT NULL;

ALTER TABLE followers
    ALTER COLUMN following_id SET NOT NULL;

ALTER TABLE followers
    DROP COLUMN IF EXISTS follower_map;

ALTER TABLE followers
    DROP COLUMN IF EXISTS follower;

ALTER TABLE followers
    DROP COLUMN IF EXISTS following;

ALTER TABLE followers
    DROP COLUMN IF EXISTS user_id;

ALTER TABLE followers
    DROP CONSTRAINT IF EXISTS uq_follower_following;

ALTER TABLE followers
    ADD CONSTRAINT uq_follower_following UNIQUE (follower_id, following_id);

CREATE INDEX IF NOT EXISTS ix_followers_follower ON followers (follower_id);

CREATE INDEX IF NOT EXISTS ix_followers_following ON followers (following_id);
