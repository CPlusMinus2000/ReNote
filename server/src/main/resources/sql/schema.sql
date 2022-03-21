CREATE TABLE IF NOT EXISTS NOTESB (
    id VARCHAR(60) DEFAULT RANDOM_UUID() PRIMARY KEY,
    name VARCHAR NOT NULL,
    contents VARCHAR NOT NULL,
    creation_time INT NOT NULL,
    last_edited INT NOT NULL,
    custom_order INT UNIQUE NOT NULL,
    notebook_name VARCHAR NOT NULL
);