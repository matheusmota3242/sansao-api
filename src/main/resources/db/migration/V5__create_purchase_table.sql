CREATE TABLE purchase (
    id BIGSERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    amount INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    source TEXT,
    purchased_at DATE NOT NULL,
    observations TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
