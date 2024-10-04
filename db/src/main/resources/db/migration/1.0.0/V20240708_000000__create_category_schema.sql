--CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SEQUENCE IF NOT EXISTS hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS category_code_seq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;

SET search_path = public, pg_catalog;

CREATE TABLE IF NOT EXISTS category(
                        uuid uuid NOT NULL PRIMARY KEY,
                        name character varying(255),
                        original_name character varying(255),
                        description character varying(255),
                        category_code integer not null DEFAULT nextval('category_code_seq'),
                        parent_code integer,
                        is_service boolean,
                        created_date timestamp with time zone DEFAULT now(),
                        updated_date timestamp with time zone DEFAULT now(),
                        active boolean default false
);

CREATE INDEX IF NOT EXISTS category_name_idx ON category USING btree(name);
CREATE INDEX IF NOT EXISTS category_code_idx ON category USING btree(category_code);