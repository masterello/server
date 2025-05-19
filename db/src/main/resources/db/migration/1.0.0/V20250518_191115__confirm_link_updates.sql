 --migrations here

ALTER TABLE confirmation_link
    ADD creation_date timestamp with time zone

--rollback  -- ALTER TABLE confirmation_link DROP COLUMN IF EXISTS creation_date