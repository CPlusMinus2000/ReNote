CREATE TABLE IF NOT EXISTS NOTESF (
    name VARCHAR NOT NULL,
    contents VARCHAR NOT NULL,
    creation_time BIGINT NOT NULL,
    last_edited BIGINT NOT NULL,
    custom_order INT NOT NULL,
    notebook_name VARCHAR NOT NULL,
    recorder VARCHAR,
    server_id VARCHAR(60) DEFAULT RANDOM_UUID() PRIMARY KEY
);