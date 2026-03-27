USE fruit_warehouse_manage;

-- Idempotent patch: add shipped_qty to sales_order_item when missing
SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sales_order_item'
      AND COLUMN_NAME = 'shipped_qty'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE sales_order_item ADD COLUMN shipped_qty DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER quantity',
    'SELECT ''patch_add_shipped_qty: column already exists'' AS message'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Safety backfill for historical nullable data
UPDATE sales_order_item
SET shipped_qty = 0.00
WHERE shipped_qty IS NULL;