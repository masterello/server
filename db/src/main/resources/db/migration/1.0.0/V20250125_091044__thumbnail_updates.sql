 --migrations here
ALTER TABLE files

    DROP COLUMN IF EXISTS avatar_thumbnail,
    ADD parent_image UUID,
    ADD task_uuid UUID

--rollback  -- Rollbacks here