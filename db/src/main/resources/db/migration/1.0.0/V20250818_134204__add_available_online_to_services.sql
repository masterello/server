 --migrations here
 ALTER TABLE category
     ADD available_online boolean default false;

--rollback  -- Rollbacks here