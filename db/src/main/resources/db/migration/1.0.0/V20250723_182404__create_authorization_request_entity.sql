 --migrations here

CREATE TABLE authorization_request_entity (
    state VARCHAR(100) PRIMARY KEY,
    request_json TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL
);
--rollback  -- Rollbacks here