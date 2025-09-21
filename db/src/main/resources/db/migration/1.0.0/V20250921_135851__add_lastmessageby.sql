 --migrations here
ALTER TABLE chat ADD COLUMN IF NOT EXISTS last_message_by UUID NULL;

--rollback  -- Rollbacks here