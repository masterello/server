CREATE TABLE IF NOT EXISTS password_reset(
                        uuid uuid NOT NULL PRIMARY KEY,
                        user_uuid uuid NOT NULL,
                        token character varying(255),
                        creation_date timestamp with time zone,
                        expires_at timestamp with time zone
);