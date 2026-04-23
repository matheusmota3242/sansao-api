CREATE TABLE automation (
    id BIGSERIAL PRIMARY KEY,
    name TEXT,
    action_type VARCHAR(50),
    metadata JSONB,
    schedule_config JSONB,
    next_execution_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    active BOOLEAN DEFAULT TRUE
);