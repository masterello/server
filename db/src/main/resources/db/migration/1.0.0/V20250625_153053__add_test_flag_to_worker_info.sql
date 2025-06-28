 --migrations here

ALTER TABLE worker_info
    ADD test boolean default false;
--rollback  -- Rollbacks here