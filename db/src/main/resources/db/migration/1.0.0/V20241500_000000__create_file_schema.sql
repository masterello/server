CREATE SEQUENCE IF NOT EXISTS hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

SET search_path = public, pg_catalog;

CREATE TABLE IF NOT EXISTS files(
                        uuid uuid NOT NULL PRIMARY KEY,
                        user_uuid uuid NOT NULL,
                        file_name character varying(255),
                        file_extension character varying(255),
                        file_type integer,
                        is_public boolean,
                        created_date timestamp with time zone DEFAULT now(),
                        updated_date timestamp with time zone DEFAULT now()
);