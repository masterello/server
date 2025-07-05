 --migrations here

ALTER TABLE worker_info
    ADD verified boolean default true;

--rollback  -- Rollbacks here