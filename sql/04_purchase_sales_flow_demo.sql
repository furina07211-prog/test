USE fruit_warehouse_manage;

ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS received_qty DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER quantity;

ALTER TABLE sales_order_item
    ADD COLUMN IF NOT EXISTS shipped_qty DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER quantity;

-- 采购演示单（全流程状态）
INSERT INTO purchase_order (
    id, purchase_no, supplier_id, warehouse_id, order_status, order_date, expected_arrival_date, total_amount, created_by, received_by, remark
) VALUES
(910001, 'PO_DEMO_FLOW_001', 1, 1, 'DRAFT', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 480.00, 1, NULL, '演示-采购草稿单'),
(910002, 'PO_DEMO_FLOW_002', 1, 1, 'SUBMITTED', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 525.00, 1, NULL, '演示-采购已提交单'),
(910003, 'PO_DEMO_FLOW_003', 2, 1, 'APPROVED', DATE_SUB(CURDATE(), INTERVAL 2 DAY), CURDATE(), 430.00, 1, NULL, '演示-采购已审核待收货单（分批）'),
(910004, 'PO_DEMO_FLOW_004', 2, 1, 'RECEIVED', DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), 520.00, 1, 2, '演示-采购已入库单')
ON DUPLICATE KEY UPDATE
    supplier_id = VALUES(supplier_id),
    warehouse_id = VALUES(warehouse_id),
    order_status = VALUES(order_status),
    order_date = VALUES(order_date),
    expected_arrival_date = VALUES(expected_arrival_date),
    total_amount = VALUES(total_amount),
    created_by = VALUES(created_by),
    received_by = VALUES(received_by),
    remark = VALUES(remark);

INSERT INTO purchase_order_item (
    id, purchase_order_id, fruit_id, batch_no, production_date, expiration_date, quantity, received_qty, unit_price, subtotal, remark
) VALUES
(910101, 910001, 1, 'POF-APPLE-001', DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 18 DAY), 40.00, 0.00, 6.00, 240.00, '草稿单-苹果'),
(910102, 910001, 2, 'POF-BANANA-001', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 40.00, 0.00, 6.00, 240.00, '草稿单-香蕉'),
(910103, 910002, 3, 'POF-ORANGE-002', DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 16 DAY), 50.00, 0.00, 5.00, 250.00, '已提交单-橙子'),
(910104, 910002, 1, 'POF-APPLE-002', DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 25.00, 0.00, 11.00, 275.00, '已提交单-苹果'),
(910105, 910003, 2, 'POF-BANANA-003', DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), 60.00, 30.00, 4.00, 240.00, '已审核单-香蕉分批到货'),
(910106, 910003, 3, 'POF-ORANGE-003', DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 20.00, 0.00, 9.50, 190.00, '已审核单-橙子待收'),
(910107, 910004, 1, 'POF-APPLE-004', DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 22 DAY), 50.00, 50.00, 6.40, 320.00, '已收货单-苹果'),
(910108, 910004, 3, 'POF-ORANGE-004', DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 25.00, 25.00, 8.00, 200.00, '已收货单-橙子')
ON DUPLICATE KEY UPDATE
    purchase_order_id = VALUES(purchase_order_id),
    fruit_id = VALUES(fruit_id),
    batch_no = VALUES(batch_no),
    production_date = VALUES(production_date),
    expiration_date = VALUES(expiration_date),
    quantity = VALUES(quantity),
    received_qty = VALUES(received_qty),
    unit_price = VALUES(unit_price),
    subtotal = VALUES(subtotal),
    remark = VALUES(remark);

-- 销售演示单（全流程状态）
INSERT INTO sales_order (
    id, sales_no, customer_id, warehouse_id, order_status, order_time, total_amount, created_by, remark
) VALUES
(920001, 'SO_DEMO_FLOW_001', 1, 1, 'DRAFT', DATE_SUB(NOW(), INTERVAL 1 HOUR), 360.00, 3, '演示-销售草稿单'),
(920002, 'SO_DEMO_FLOW_002', 2, 1, 'SUBMITTED', DATE_SUB(NOW(), INTERVAL 3 HOUR), 243.00, 3, '演示-销售已提交单'),
(920003, 'SO_DEMO_FLOW_003', 1, 1, 'APPROVED', DATE_SUB(NOW(), INTERVAL 5 HOUR), 450.00, 3, '演示-销售已审核待出库单（分批）'),
(920004, 'SO_DEMO_FLOW_004', 2, 1, 'SHIPPED', DATE_SUB(NOW(), INTERVAL 9 HOUR), 280.00, 3, '演示-销售已出库单')
ON DUPLICATE KEY UPDATE
    customer_id = VALUES(customer_id),
    warehouse_id = VALUES(warehouse_id),
    order_status = VALUES(order_status),
    order_time = VALUES(order_time),
    total_amount = VALUES(total_amount),
    created_by = VALUES(created_by),
    remark = VALUES(remark);

INSERT INTO sales_order_item (
    id, sales_order_id, fruit_id, batch_id, quantity, shipped_qty, unit_price, subtotal, remark
) VALUES
(920101, 920001, 1, NULL, 30.00, 0.00, 12.00, 360.00, '草稿单-苹果'),
(920102, 920002, 2, NULL, 27.00, 0.00, 9.00, 243.00, '已提交单-香蕉'),
(920103, 920003, 1, NULL, 20.00, 10.00, 12.00, 240.00, '已审核单-苹果分批出库'),
(920104, 920003, 3, NULL, 25.00, 0.00, 8.40, 210.00, '已审核单-橙子待出'),
(920105, 920004, 2, NULL, 20.00, 20.00, 7.00, 140.00, '已出库单-香蕉'),
(920106, 920004, 3, NULL, 20.00, 20.00, 7.00, 140.00, '已出库单-橙子')
ON DUPLICATE KEY UPDATE
    sales_order_id = VALUES(sales_order_id),
    fruit_id = VALUES(fruit_id),
    batch_id = VALUES(batch_id),
    quantity = VALUES(quantity),
    shipped_qty = VALUES(shipped_qty),
    unit_price = VALUES(unit_price),
    subtotal = VALUES(subtotal),
    remark = VALUES(remark);

-- 演示库存批次，保证销售出库可执行且可触发“库存不足拦截”验证
INSERT INTO inventory_batch (
    id, fruit_id, warehouse_id, batch_no, source_type, source_id, production_date, expiration_date, total_qty, available_qty, locked_qty, unit_cost, status
) VALUES
(930001, 1, 1, 'INV-DEMO-APPLE-001', 'PURCHASE_IN', 910004, DATE_SUB(CURDATE(), INTERVAL 6 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 300.00, 220.00, 0.00, 6.50, 'IN_STOCK'),
(930002, 2, 1, 'INV-DEMO-BANANA-001', 'PURCHASE_IN', 910003, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), 260.00, 200.00, 0.00, 4.20, 'IN_STOCK'),
(930003, 3, 1, 'INV-DEMO-ORANGE-001', 'PURCHASE_IN', 910004, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 180.00, 120.00, 0.00, 5.30, 'IN_STOCK'),
(930010, 2, 1, 'POF-BANANA-003', 'PURCHASE_IN', 910003, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), 30.00, 30.00, 0.00, 4.00, 'IN_STOCK'),
(930011, 1, 1, 'POF-APPLE-004', 'PURCHASE_IN', 910004, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 22 DAY), 50.00, 50.00, 0.00, 6.40, 'IN_STOCK'),
(930012, 3, 1, 'POF-ORANGE-004', 'PURCHASE_IN', 910004, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 25.00, 25.00, 0.00, 8.00, 'IN_STOCK')
ON DUPLICATE KEY UPDATE
    fruit_id = VALUES(fruit_id),
    warehouse_id = VALUES(warehouse_id),
    batch_no = VALUES(batch_no),
    source_type = VALUES(source_type),
    source_id = VALUES(source_id),
    production_date = VALUES(production_date),
    expiration_date = VALUES(expiration_date),
    total_qty = VALUES(total_qty),
    available_qty = VALUES(available_qty),
    locked_qty = VALUES(locked_qty),
    unit_cost = VALUES(unit_cost),
    status = VALUES(status);
