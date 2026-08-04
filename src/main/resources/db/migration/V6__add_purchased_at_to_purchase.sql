ALTER TABLE purchase ADD COLUMN purchased_at DATE;
UPDATE purchase SET purchased_at = created_at::date WHERE purchased_at IS NULL;
ALTER TABLE purchase ALTER COLUMN purchased_at SET NOT NULL;
