UPDATE cart_items SET count = 1 WHERE count = 0;

ALTER TABLE cart_items
ADD CONSTRAINT chk_cart_items_count_positive CHECK (count > 0);

DROP INDEX IF EXISTS idx_items_title_description;