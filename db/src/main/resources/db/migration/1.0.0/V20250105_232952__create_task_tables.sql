 --migrations here

CREATE TABLE IF NOT EXISTS task(
                        uuid uuid NOT NULL PRIMARY KEY,
                        user_uuid uuid NOT NULL,
                        worker_uuid uuid,
                        name character varying(255),
                        description character varying(755),
                        category_uuid uuid NOT NULL,
                        status integer NOT NULL,
                        created_date timestamp with time zone DEFAULT now(),
                        updated_date timestamp with time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_task_review(
                        uuid uuid NOT NULL PRIMARY KEY,
                        task_uuid uuid NOT NULL,
                        user_uuid uuid NOT NULL,
                        review character varying(755),
                        created_date timestamp with time zone DEFAULT now(),
                        updated_date timestamp with time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS worker_task_review(
                        uuid uuid NOT NULL PRIMARY KEY,
                        task_uuid uuid NOT NULL,
                        worker_uuid uuid NOT NULL,
                        review character varying(755),
                        created_date timestamp with time zone DEFAULT now(),
                        updated_date timestamp with time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS worker_rating(
                        uuid uuid NOT NULL PRIMARY KEY,
                        task_uuid uuid NOT NULL,
                        rating integer NOT NULL,
                        created_date timestamp with time zone DEFAULT now(),
                        updated_date timestamp with time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_rating(
                        uuid uuid NOT NULL PRIMARY KEY,
                        task_uuid uuid NOT NULL,
                        rating integer NOT NULL,
                        created_date timestamp with time zone DEFAULT now(),
                        updated_date timestamp with time zone DEFAULT now()
);

CREATE INDEX IF NOT EXISTS task_user_idx ON task USING btree(user_uuid);
CREATE INDEX IF NOT EXISTS task_worker_idx ON task USING btree(worker_uuid) WHERE worker_uuid IS NOT NULL;

CREATE INDEX IF NOT EXISTS worker_task_review_task_uuid_idx ON worker_task_review USING btree(task_uuid);
CREATE INDEX IF NOT EXISTS user_task_review_task_uuid_idx ON user_task_review USING btree(task_uuid);

CREATE INDEX IF NOT EXISTS worker_rating_task_uuid_idx ON worker_rating USING btree(task_uuid);
CREATE INDEX IF NOT EXISTS user_rating_task_uuid_idx ON user_rating USING btree(task_uuid);

--rollback  DROP TABLE IF EXISTS tasks CASCADE;
--rollback  DROP TABLE IF EXISTS task_review CASCADE;
--rollback  DROP TABLE IF EXISTS worker_rating CASCADE;
--rollback  DROP TABLE IF EXISTS user_rating CASCADE;