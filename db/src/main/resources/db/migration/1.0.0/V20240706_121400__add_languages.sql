CREATE TABLE user_languages (
    user_id uuid,
    language VARCHAR(255),
    PRIMARY KEY (user_id, language),
    FOREIGN KEY (user_id) REFERENCES users(uuid)
);