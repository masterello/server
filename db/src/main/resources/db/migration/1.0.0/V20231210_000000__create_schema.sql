CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

SET search_path = public, pg_catalog;

CREATE TABLE users (
                        uuid uuid NOT NULL PRIMARY KEY,
                        email character varying(255) NOT NULL,
                        password character varying(255),
                        name character varying(255),
                        lastname character varying(255),
                        title character varying(10),
                        phone character varying(50),
                        city character varying(50),
                        status integer,
                        email_verified boolean
);

CREATE TABLE user_roles (
    user_id uuid,
    role VARCHAR(255),
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(uuid)
);

CREATE TABLE confirmation_link(
                      uuid uuid NOT NULL PRIMARY KEY,
                      user_uuid uuid NOT NULL,
                      token character varying(255),
                      expires_at timestamp with time zone
);

CREATE TABLE clients (
    id VARCHAR(255) PRIMARY KEY,
    client_id VARCHAR(255),
    client_id_issued_at timestamp with time zone,
    client_secret VARCHAR(255),
    client_secret_expires_at timestamp with time zone,
    client_name VARCHAR(255),
    client_authentication_methods VARCHAR(255),
    authorization_grant_types VARCHAR(255),
    redirect_uris VARCHAR(255),
    post_logout_redirect_uris VARCHAR(255),
    scopes VARCHAR(255)
);

CREATE TABLE oauth2_authorization (
    id varchar(100) NOT NULL,
    registered_client_id varchar(100) NOT NULL,
    principal_name varchar(200) NOT NULL,
    authorization_grant_type varchar(100) NOT NULL,
    authorized_scopes varchar(1000) DEFAULT NULL,
    principal text DEFAULT NULL,
    access_token_value text DEFAULT NULL,
    access_token_issued_at timestamp DEFAULT NULL,
    access_token_expires_at timestamp DEFAULT NULL,
    access_token_metadata text DEFAULT NULL,
    access_token_type varchar(100) DEFAULT NULL,
    access_token_scopes varchar(1000) DEFAULT NULL,
    refresh_token_value text DEFAULT NULL,
    refresh_token_issued_at timestamp DEFAULT NULL,
    refresh_token_expires_at timestamp DEFAULT NULL,
    refresh_token_metadata text DEFAULT NULL,
    PRIMARY KEY (id)
);

