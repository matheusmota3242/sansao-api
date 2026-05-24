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

CREATE TABLE task (
	id BIGSERIAL PRIMARY KEY,
	description VARCHAR(255),
	scheduled_at TIMESTAMP,
	completed BOOLEAN NOT NULL DEFAULT FALSE,
	active BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMP NOT NULL,
	updated_at TIMESTAMP NOT NULL
);

CREATE TABLE chat_record (
	id BIGSERIAL PRIMARY KEY,
	interaction JSONB,
	completed BOOLEAN,
	active BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMP NOT NULL,
	updated_at TIMESTAMP
);

--GRANT ALL PRIVILEGES ON DATABASE simao_db TO matheus;
--GRANT ALL ON SCHEMA public TO matheus;
--GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO matheus;
--GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO matheus;
