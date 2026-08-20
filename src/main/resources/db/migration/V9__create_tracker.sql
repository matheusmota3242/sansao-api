-- Quantitative daily-goal trackers (water, protein, ...) and their logged entries.
-- A tracker is identified in chat by a unique keyword (@agua 250, @prot 30).
CREATE TABLE tracker (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    keyword VARCHAR(50) NOT NULL UNIQUE,
    unit VARCHAR(20) NOT NULL,
    daily_goal NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE tracker_entry (
    id BIGSERIAL PRIMARY KEY,
    tracker_id BIGINT NOT NULL REFERENCES tracker(id),
    amount NUMERIC(10,2) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Daily progress is a SUM over (tracker_id, recorded_at range), so index that pair.
CREATE INDEX idx_tracker_entry_tracker_recorded ON tracker_entry (tracker_id, recorded_at);
