 --migrations here

ALTER TABLE user_rating
   ADD user_uuid uuid NOT NULL,
   ADD worker_uuid uuid NOT NULL;

ALTER TABLE worker_rating
   ADD user_uuid uuid NOT NULL,
   ADD worker_uuid uuid NOT NULL;

CREATE INDEX IF NOT EXISTS user_rating_user_uuid_idx ON user_rating USING btree(user_uuid);
CREATE INDEX IF NOT EXISTS worker_rating_worker_uuid_idx ON worker_rating USING btree(worker_uuid);

--rollback  ALTER TABLE user_rating DROP COLUMN IF EXISTS user_uuid;
--rollback  ALTER TABLE user_rating DROP COLUMN IF EXISTS worker_uuid;
--rollback  ALTER TABLE worker_rating DROP COLUMN IF EXISTS user_uuid;
--rollback  ALTER TABLE worker_rating DROP COLUMN IF EXISTS worker_uuid;