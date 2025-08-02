 --migrations here

-- V2__make_token_unique.sql

ALTER TABLE confirmation_link
ADD CONSTRAINT uq_confirmation_link_token UNIQUE (token);

--rollback  -- Rollbacks here