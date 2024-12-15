CREATE TABLE worker_languages (
    worker_id uuid,
    language VARCHAR(255),
    PRIMARY KEY (worker_id, language),
    FOREIGN KEY (worker_id) REFERENCES worker_info(worker_id)
);

-- Migrate data from user_languages to worker_languages
INSERT INTO worker_languages (worker_id, language)
SELECT ul.user_id AS worker_id, ul.language
FROM user_languages ul
JOIN worker_info wi
  ON ul.user_id = wi.worker_id
-- Ensure only users with existing worker_info are migrated;
-- otherwise, these rows will be excluded.