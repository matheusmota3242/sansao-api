CREATE TABLE chat_record (
	id BIGSERIAL PRIMARY KEY,
	interaction JSONB,
	completed BOOLEAN,
	active BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMP NOT NULL,
	updated_at TIMESTAMP
);