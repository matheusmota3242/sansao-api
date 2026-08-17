CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Named print_order because ORDER is a reserved SQL keyword: a table literally
-- named "order" would have to be quoted in every statement Hibernate generates.
CREATE TABLE print_order (
    id BIGSERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    print_time_minutes INTEGER,
    -- Position in the printing queue, 1-based and contiguous across the orders
    -- still in it (WAITING/RUNNING). NULL once an order leaves the queue
    -- (COMPLETED/CANCELLED), so finished work does not occupy a position.
    priority INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    production_cost NUMERIC(12, 2),
    sale_price NUMERIC(12, 2),
    -- Set when the order moves to RUNNING, cleared if it goes back to WAITING.
    started_at TIMESTAMP,
    observations TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_print_order_queue ON print_order (status, priority);
CREATE INDEX idx_print_order_customer ON print_order (customer_id);
