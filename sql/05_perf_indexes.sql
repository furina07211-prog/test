-- 高频查询优化索引（MySQL 8+ 支持 IF NOT EXISTS）
CREATE INDEX IF NOT EXISTS idx_so_order_time ON sales_order(order_time);
CREATE INDEX IF NOT EXISTS idx_soi_fruit_order ON sales_order_item(fruit_id, sales_order_id);
CREATE INDEX IF NOT EXISTS idx_inv_batch_fruit_wh_status ON inventory_batch(fruit_id, warehouse_id, status);
CREATE INDEX IF NOT EXISTS idx_alert_fruit_type_status ON inventory_alert(fruit_id, alert_type, alert_status);