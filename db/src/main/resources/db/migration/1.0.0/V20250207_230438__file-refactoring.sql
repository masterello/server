 --migrations here

ALTER TABLE files
    DROP COLUMN IF EXISTS file_extension,
    DROP COLUMN IF EXISTS parent_image,
    DROP COLUMN IF EXISTS thumbnail_size,
    ADD file_status integer;


--rollback  -- Rollbacks here