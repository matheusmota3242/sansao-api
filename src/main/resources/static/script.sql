CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(55),
    description TEXT,
    priority VARCHAR(55)
    execute_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);