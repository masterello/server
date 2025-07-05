 --migrations here

ALTER TABLE users
    ADD created_at timestamp with time zone DEFAULT now(),
    ADD updated_at timestamp with time zone;

--rollback  -- Rollbacks here