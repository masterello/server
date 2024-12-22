ALTER TABLE worker_info
    ADD active boolean default true;

UPDATE worker_info
SET active = CASE
                WHEN users.status = 0 THEN TRUE
                ELSE FALSE
            END
FROM users
WHERE worker_info.worker_id = users.uuid;