TRUNCATE oauth2_authorization;

-- 1. Create the new table for storing refresh token data with history
CREATE TABLE oauth2_token_pair (
    id UUID PRIMARY KEY,                           -- Unique identifier for each token pair
    authorization_id VARCHAR(100) NOT NULL,        -- Foreign key linking to the authorization
    access_token_value TEXT NOT NULL,              -- Access token value
    access_token_type varchar(100) DEFAULT NULL,
    access_token_expires_at TIMESTAMP,             -- Expiration date of the access token
    access_token_metadata TEXT,                    -- Metadata for the access token
    access_token_scopes VARCHAR(1000),             -- Scopes of the access token
    refresh_token_value TEXT NOT NULL,             -- Refresh token value
    refresh_token_expires_at TIMESTAMP,            -- Expiration date of the refresh token
    refresh_token_metadata TEXT,                   -- Metadata for the refresh token
    is_revoked BOOLEAN DEFAULT FALSE,              -- Flag to mark if the pair is revoked
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp for historical tracking
    CONSTRAINT fk_authorization_token_pair
        FOREIGN KEY (authorization_id) REFERENCES oauth2_authorization(id)
        ON DELETE CASCADE                          -- Ensure tokens are deleted if the authorization is removed
);

-- 2. Remove the refresh token fields from oauth2_authorization
ALTER TABLE oauth2_authorization
    DROP COLUMN IF EXISTS refresh_token_value,
    DROP COLUMN IF EXISTS refresh_token_issued_at,
    DROP COLUMN IF EXISTS refresh_token_expires_at,
    DROP COLUMN IF EXISTS refresh_token_metadata,
    DROP COLUMN IF EXISTS access_token_value,
    DROP COLUMN IF EXISTS access_token_issued_at,
    DROP COLUMN IF EXISTS access_token_expires_at,
    DROP COLUMN IF EXISTS access_token_metadata,
    DROP COLUMN IF EXISTS access_token_scopes,
    DROP COLUMN IF EXISTS access_token_type;
