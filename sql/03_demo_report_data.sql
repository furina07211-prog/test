USE fruit_warehouse_manage;

INSERT INTO purchase_order (id, purchase_no, supplier_id, warehouse_id, order_status, order_date, expected_arrival_date, total_amount, created_by, received_by, remark) VALUES
(1, 'PO_DEMO_001', 1, 1, 'RECEIVED', CURDATE() - INTERVAL 2 DAY, CURDATE() - INTERVAL 1 DAY, 650.00, 1, 2, '演示采购单'),
(2, 'PO_DEMO_002', 2, 1, 'DRAFT', CURDATE(), CURDATE() + INTERVAL 1 DAY, 540.00, 1, NULL, '在途采购单')
ON DUPLICATE KEY UPDATE order_status = VALUES(order_status), total_amount = VALUES(total_amount);

INSERT INTO purchase_order_item (id, purchase_order_id, fruit_id, batch_no, production_date, expiration_date, quantity, received_qty, unit_price, subtotal, remark) VALUES
(1, 1, 1, 'APL2401', CURDATE() - INTERVAL 5 DAY, CURDATE() + INTERVAL 12 DAY, 100.00, 100.00, 6.50, 650.00, '苹果批次'),
(2, 2, 2, 'BAN2401', CURDATE(), CURDATE() + INTERVAL 6 DAY, 120.00, 0.00, 4.50, 540.00, '香蕉在途')
ON DUPLICATE KEY UPDATE received_qty = VALUES(received_qty), subtotal = VALUES(subtotal);

INSERT INTO inventory_batch (id, fruit_id, warehouse_id, batch_no, source_type, source_id, production_date, expiration_date, total_qty, available_qty, locked_qty, unit_cost, status) VALUES
(1, 1, 1, 'APL2401', 'PURCHASE_IN', 1, CURDATE() - INTERVAL 5 DAY, CURDATE() + INTERVAL 12 DAY, 100.00, 68.00, 0.00, 6.50, 'IN_STOCK'),
(2, 3, 1, 'ORG2401', 'PURCHASE_IN', 1, CURDATE() - INTERVAL 4 DAY, CURDATE() + INTERVAL 5 DAY, 60.00, 20.00, 0.00, 5.20, 'IN_STOCK')
ON DUPLICATE KEY UPDATE available_qty = VALUES(available_qty), status = VALUES(status);

INSERT INTO sales_order (id, sales_no, customer_id, warehouse_id, order_status, order_time, total_amount, created_by, remark) VALUES
(1, 'SO_DEMO_001', 1, 1, 'CONFIRMED', NOW() - INTERVAL 1 DAY, 384.00, 3, '苹果销售单')
ON DUPLICATE KEY UPDATE order_status = VALUES(order_status), total_amount = VALUES(total_amount);

INSERT INTO sales_order_item (id, sales_order_id, fruit_id, batch_id, quantity, unit_price, subtotal, remark) VALUES
(1, 1, 1, 1, 32.00, 12.00, 384.00, '苹果出库')
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), subtotal = VALUES(subtotal);

INSERT INTO inventory_txn (id, biz_type, biz_id, batch_id, fruit_id, warehouse_id, change_qty, balance_qty, operator_id, remark) VALUES
(1, 'PURCHASE_IN', 1, 1, 1, 1, 100.00, 100.00, 2, '采购入库'),
(2, 'SALES_OUT', 1, 1, 1, 1, -32.00, 68.00, 3, '销售出库')
ON DUPLICATE KEY UPDATE change_qty = VALUES(change_qty), balance_qty = VALUES(balance_qty);

INSERT INTO inventory_alert (id, alert_type, fruit_id, batch_id, warehouse_id, alert_level, alert_msg, threshold_value, current_value, alert_status) VALUES
(1, 'LOW_STOCK', 3, NULL, 1, 'MEDIUM', '橙子库存低于安全库存', 80.00, 20.00, 'UNHANDLED'),
(2, 'EXPIRING', 3, 2, 1, 'MEDIUM', '橙子批次即将到期', 3.00, 2.00, 'UNHANDLED')
ON DUPLICATE KEY UPDATE alert_msg = VALUES(alert_msg), current_value = VALUES(current_value), alert_status = VALUES(alert_status);

INSERT INTO ai_forecast_result (id, fruit_id, forecast_date, target_date, model_name, version_no, predict_qty, confidence_lower, confidence_upper, data_window_days) VALUES
(1, 1, CURDATE(), CURDATE() + INTERVAL 1 DAY, 'simple', 'v1', 35.00, 31.50, 38.50, 30),
(2, 1, CURDATE(), CURDATE() + INTERVAL 2 DAY, 'simple', 'v1', 37.00, 33.30, 40.70, 30),
(3, 1, CURDATE(), CURDATE() + INTERVAL 3 DAY, 'simple', 'v1', 36.00, 32.40, 39.60, 30)
ON DUPLICATE KEY UPDATE predict_qty = VALUES(predict_qty), confidence_lower = VALUES(confidence_lower), confidence_upper = VALUES(confidence_upper);

INSERT INTO ai_purchase_suggestion (id, fruit_id, warehouse_id, suggestion_date, predicted_daily_qty, lead_time_days, safety_stock_qty, current_stock_qty, in_transit_qty, recommended_purchase_qty, status, reason) VALUES
(1, 3, 1, CURDATE(), 28.00, 2, 80.00, 20.00, 0.00, 116.00, 'NEW', '橙子低库存且存在临期风险，建议尽快补货')
ON DUPLICATE KEY UPDATE recommended_purchase_qty = VALUES(recommended_purchase_qty), reason = VALUES(reason);
