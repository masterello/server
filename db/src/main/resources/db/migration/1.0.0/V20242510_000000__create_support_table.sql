CREATE TABLE IF NOT EXISTS support(
                        uuid uuid NOT NULL PRIMARY KEY,
                        title character varying(255),
                        email character varying(255),
                        phone character varying(255),
                        message character varying(500),
                        processed boolean not null default false,
                        creation_date timestamp with time zone DEFAULT now()
);