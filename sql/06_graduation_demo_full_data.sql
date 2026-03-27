USE fruit_warehouse_manage;

-- =============================================
-- 毕设演示全量数据（幂等增量版）
-- 约定：演示专用ID区间 860000~899999
-- 可重复执行，不影响非演示数据
-- =============================================

-- 1) 兼容性补丁：补齐 shipped_qty 字段（若缺失）
ALTER TABLE sales_order_item
    ADD COLUMN IF NOT EXISTS shipped_qty DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER quantity;

-- 2) 幂等清理（仅清理演示范围）
DELETE FROM ai_purchase_suggestion WHERE id BETWEEN 898000 AND 898999;
DELETE FROM ai_forecast_result WHERE id BETWEEN 897000 AND 897999;
DELETE FROM inventory_alert WHERE id BETWEEN 896000 AND 896999;

DELETE FROM sales_order_item WHERE id BETWEEN 881000 AND 882999;
DELETE FROM sales_order WHERE id BETWEEN 880000 AND 880999;

DELETE FROM purchase_order_item WHERE id BETWEEN 871000 AND 872999;
DELETE FROM purchase_order WHERE id BETWEEN 870000 AND 870999;

DELETE FROM inventory_batch WHERE id BETWEEN 862000 AND 862099;

DELETE FROM fruit_info WHERE id BETWEEN 861100 AND 861199;
DELETE FROM supplier WHERE id BETWEEN 861300 AND 861399;
DELETE FROM customer WHERE id BETWEEN 861400 AND 861499;
DELETE FROM warehouse WHERE id BETWEEN 861200 AND 861299;
DELETE FROM fruit_category WHERE id BETWEEN 861000 AND 861099;

-- 3) 角色、账号、绑定关系（兼容明文123456登录后自动升级）
INSERT INTO sys_role (id, role_code, role_name, remark, status) VALUES
(1, 'ADMIN', '系统管理员', '系统全权限', 1),
(2, 'WAREHOUSE', '仓库管理员', '库存、盘点、预警处理', 1),
(3, 'SALES', '销售员', '销售出库与客户维护', 1)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    remark = VALUES(remark),
    status = VALUES(status);

INSERT INTO sys_user (id, username, password, real_name, phone, email, status) VALUES
(1, 'admin', '123456', '系统管理员', '13800000001', 'admin@demo.local', 1),
(2, 'warehouse', '123456', '仓库管理员', '13800000002', 'warehouse@demo.local', 1),
(3, 'sales', '123456', '销售员', '13800000003', 'sales@demo.local', 1)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    real_name = VALUES(real_name),
    phone = VALUES(phone),
    email = VALUES(email),
    status = VALUES(status);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 4) 主数据：分类、仓库、供应商、客户、水果
INSERT INTO fruit_category (id, category_name, sort_no, status, remark) VALUES
(861001, '演示柑橘类', 1, 1, '毕设演示分类'),
(861002, '演示热带类', 2, 1, '毕设演示分类'),
(861003, '演示浆果类', 3, 1, '毕设演示分类'),
(861004, '演示仁果类', 4, 1, '毕设演示分类'),
(861005, '演示瓜果类', 5, 1, '毕设演示分类')
ON DUPLICATE KEY UPDATE
    category_name = VALUES(category_name),
    sort_no = VALUES(sort_no),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO warehouse (id, warehouse_code, warehouse_name, location, manager_name, phone, status) VALUES
(861201, 'WH_DEMO_A', '华南冷库A仓', '广州白云分拨中心', '李仓', '13910000001', 1),
(861202, 'WH_DEMO_B', '华南常温B仓', '广州白云分拨中心', '王仓', '13910000002', 1),
(861203, 'WH_DEMO_C', '深圳前置仓C', '深圳龙华前置中心', '赵仓', '13910000003', 1)
ON DUPLICATE KEY UPDATE
    warehouse_name = VALUES(warehouse_name),
    location = VALUES(location),
    manager_name = VALUES(manager_name),
    phone = VALUES(phone),
    status = VALUES(status);

INSERT INTO supplier (id, supplier_code, supplier_name, contact_person, phone, address, credit_level, status, remark) VALUES
(861301, 'SUP_DEMO_001', '华南果业合作社', '陈建国', '13610000001', '广东增城果园基地', 'A', 1, '演示核心供应商'),
(861302, 'SUP_DEMO_002', '赣南柑橘供应中心', '刘志强', '13610000002', '江西赣州原产地', 'A', 1, '演示核心供应商'),
(861303, 'SUP_DEMO_003', '海南热带水果联盟', '吴明', '13610000003', '海南海口冷链园', 'B', 1, '演示核心供应商'),
(861304, 'SUP_DEMO_004', '山东苹果直采基地', '赵云峰', '13610000004', '山东烟台产区', 'A', 1, '演示核心供应商'),
(861305, 'SUP_DEMO_005', '云南高原水果商行', '孙立', '13610000005', '云南昆明集散中心', 'B', 1, '演示供应商'),
(861306, 'SUP_DEMO_006', '广西香蕉集采平台', '周海', '13610000006', '广西南宁批发市场', 'A', 1, '演示供应商')
ON DUPLICATE KEY UPDATE
    supplier_name = VALUES(supplier_name),
    contact_person = VALUES(contact_person),
    phone = VALUES(phone),
    address = VALUES(address),
    credit_level = VALUES(credit_level),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO customer (id, customer_code, customer_name, contact_person, phone, address, customer_level, status, remark) VALUES
(861401, 'CUS_DEMO_001', '广州市海珠生鲜市场', '杨采购', '13710000001', '广州海珠农产品市场', 'VIP', 1, '演示核心客户'),
(861402, 'CUS_DEMO_002', '深圳南山连锁门店', '彭店长', '13710000002', '深圳南山社区商超', 'A', 1, '演示核心客户'),
(861403, 'CUS_DEMO_003', '佛山顺德批发档口', '何老板', '13710000003', '佛山顺德果蔬档口', 'A', 1, '演示客户'),
(861404, 'CUS_DEMO_004', '东莞社区团购中心', '罗运营', '13710000004', '东莞南城配送站', 'NORMAL', 1, '演示客户'),
(861405, 'CUS_DEMO_005', '珠海商超采购中心', '郑经理', '13710000005', '珠海香洲商超中心', 'VIP', 1, '演示客户'),
(861406, 'CUS_DEMO_006', '中山水果批发市场', '梁采购', '13710000006', '中山火炬开发区', 'NORMAL', 1, '演示客户')
ON DUPLICATE KEY UPDATE
    customer_name = VALUES(customer_name),
    contact_person = VALUES(contact_person),
    phone = VALUES(phone),
    address = VALUES(address),
    customer_level = VALUES(customer_level),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO fruit_info (id, fruit_code, fruit_name, category_id, unit, origin_place, shelf_life_days, warning_days, safe_stock_qty, suggested_purchase_price, suggested_sale_price, status, remark) VALUES
(861101, 'FR_DEMO_001', '苹果', 861004, 'kg', '山东烟台', 30, 5, 220.00, 6.80, 10.80, 1, '演示水果主数据'),
(861102, 'FR_DEMO_002', '香蕉', 861002, 'kg', '广西南宁', 10, 3, 200.00, 4.20, 7.20, 1, '演示水果主数据'),
(861103, 'FR_DEMO_003', '橙子', 861001, 'kg', '江西赣州', 20, 4, 180.00, 5.30, 8.60, 1, '演示水果主数据'),
(861104, 'FR_DEMO_004', '葡萄', 861003, 'kg', '新疆吐鲁番', 12, 3, 120.00, 9.80, 14.80, 1, '演示水果主数据'),
(861105, 'FR_DEMO_005', '草莓', 861003, 'kg', '辽宁丹东', 6, 2, 90.00, 12.50, 18.50, 1, '演示水果主数据'),
(861106, 'FR_DEMO_006', '西瓜', 861005, 'kg', '海南三亚', 10, 3, 260.00, 2.90, 4.90, 1, '演示水果主数据'),
(861107, 'FR_DEMO_007', '哈密瓜', 861005, 'kg', '新疆哈密', 14, 4, 150.00, 5.10, 8.20, 1, '演示水果主数据'),
(861108, 'FR_DEMO_008', '芒果', 861002, 'kg', '海南海口', 9, 3, 140.00, 7.20, 11.20, 1, '演示水果主数据'),
(861109, 'FR_DEMO_009', '菠萝', 861002, 'kg', '广东湛江', 12, 3, 130.00, 4.00, 6.90, 1, '演示水果主数据'),
(861110, 'FR_DEMO_010', '梨', 861004, 'kg', '河北赵县', 25, 5, 160.00, 5.90, 9.00, 1, '演示水果主数据'),
(861111, 'FR_DEMO_011', '桃子', 861004, 'kg', '山东蒙阴', 8, 2, 110.00, 6.80, 10.60, 1, '演示水果主数据'),
(861112, 'FR_DEMO_012', '蓝莓', 861003, 'kg', '云南红河', 7, 2, 80.00, 22.00, 32.00, 1, '演示水果主数据')
ON DUPLICATE KEY UPDATE
    fruit_name = VALUES(fruit_name),
    category_id = VALUES(category_id),
    unit = VALUES(unit),
    origin_place = VALUES(origin_place),
    shelf_life_days = VALUES(shelf_life_days),
    warning_days = VALUES(warning_days),
    safe_stock_qty = VALUES(safe_stock_qty),
    suggested_purchase_price = VALUES(suggested_purchase_price),
    suggested_sale_price = VALUES(suggested_sale_price),
    status = VALUES(status),
    remark = VALUES(remark);

-- 5) 库存批次（24条：正常+临期+低库存+失效）
INSERT INTO inventory_batch (
    id, fruit_id, warehouse_id, batch_no, source_type, source_id,
    production_date, expiration_date, total_qty, available_qty, locked_qty, unit_cost, status
) VALUES
(862000, 861101, 861201, 'DEMO_APL_A1', 'PURCHASE_IN', 870001, DATE_SUB(CURDATE(), INTERVAL 12 DAY), DATE_ADD(CURDATE(), INTERVAL 18 DAY), 180.00, 130.00, 0.00, 6.80, 'IN_STOCK'),
(862001, 861101, 861202, 'DEMO_APL_B1', 'PURCHASE_IN', 870002, DATE_SUB(CURDATE(), INTERVAL 8 DAY), DATE_ADD(CURDATE(), INTERVAL 16 DAY), 140.00, 95.00, 0.00, 6.90, 'IN_STOCK'),
(862002, 861102, 861201, 'DEMO_BAN_A1', 'PURCHASE_IN', 870003, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY), 150.00, 90.00, 0.00, 4.20, 'IN_STOCK'),
(862003, 861102, 861203, 'DEMO_BAN_C1', 'PURCHASE_IN', 870004, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 120.00, 65.00, 0.00, 4.30, 'IN_STOCK'),
(862004, 861103, 861201, 'DEMO_ORG_A1', 'PURCHASE_IN', 870005, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 10 DAY), 130.00, 88.00, 0.00, 5.20, 'IN_STOCK'),
(862005, 861103, 861202, 'DEMO_ORG_B1', 'PURCHASE_IN', 870006, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 100.00, 45.00, 0.00, 5.40, 'IN_STOCK'),
(862006, 861104, 861201, 'DEMO_GRP_A1', 'PURCHASE_IN', 870007, DATE_SUB(CURDATE(), INTERVAL 6 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 90.00, 22.00, 0.00, 9.80, 'IN_STOCK'),
(862007, 861104, 861203, 'DEMO_GRP_C1', 'PURCHASE_IN', 870008, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), 80.00, 35.00, 0.00, 9.70, 'IN_STOCK'),
(862008, 861105, 861201, 'DEMO_STR_A1', 'PURCHASE_IN', 870009, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 70.00, 18.00, 0.00, 12.50, 'IN_STOCK'),
(862009, 861105, 861202, 'DEMO_STR_B1', 'PURCHASE_IN', 870010, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 65.00, 25.00, 0.00, 12.60, 'IN_STOCK'),
(862010, 861106, 861201, 'DEMO_WTM_A1', 'PURCHASE_IN', 870011, DATE_SUB(CURDATE(), INTERVAL 6 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), 260.00, 210.00, 0.00, 2.90, 'IN_STOCK'),
(862011, 861106, 861203, 'DEMO_WTM_C1', 'PURCHASE_IN', 870012, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 230.00, 170.00, 0.00, 3.00, 'IN_STOCK'),
(862012, 861107, 861201, 'DEMO_HMG_A1', 'PURCHASE_IN', 870013, DATE_SUB(CURDATE(), INTERVAL 9 DAY), DATE_ADD(CURDATE(), INTERVAL 8 DAY), 160.00, 105.00, 0.00, 5.20, 'IN_STOCK'),
(862013, 861107, 861202, 'DEMO_HMG_B1', 'PURCHASE_IN', 870014, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 140.00, 85.00, 0.00, 5.10, 'IN_STOCK'),
(862014, 861108, 861201, 'DEMO_MNG_A1', 'PURCHASE_IN', 870015, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY), 150.00, 92.00, 0.00, 7.20, 'IN_STOCK'),
(862015, 861108, 861203, 'DEMO_MNG_C1', 'PURCHASE_IN', 870016, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 120.00, 52.00, 0.00, 7.30, 'IN_STOCK'),
(862016, 861109, 861201, 'DEMO_PNA_A1', 'PURCHASE_IN', 870017, DATE_SUB(CURDATE(), INTERVAL 6 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 110.00, 26.00, 0.00, 4.00, 'IN_STOCK'),
(862017, 861109, 861202, 'DEMO_PNA_B1', 'PURCHASE_IN', 870018, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 100.00, 42.00, 0.00, 4.10, 'IN_STOCK'),
(862018, 861110, 861201, 'DEMO_PER_A1', 'PURCHASE_IN', 870019, DATE_SUB(CURDATE(), INTERVAL 11 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 170.00, 123.00, 0.00, 5.90, 'IN_STOCK'),
(862019, 861110, 861203, 'DEMO_PER_C1', 'PURCHASE_IN', 870020, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), 140.00, 96.00, 0.00, 6.00, 'IN_STOCK'),
(862020, 861111, 861202, 'DEMO_PCH_B1', 'PURCHASE_IN', 870021, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 85.00, 38.00, 0.00, 6.80, 'IN_STOCK'),
(862021, 861111, 861203, 'DEMO_PCH_C1', 'PURCHASE_IN', 870022, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 70.00, 28.00, 0.00, 6.90, 'IN_STOCK'),
(862022, 861112, 861201, 'DEMO_BLB_A1', 'PURCHASE_IN', 870023, DATE_SUB(CURDATE(), INTERVAL 9 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), 60.00, 0.00, 0.00, 22.00, 'EXPIRED'),
(862023, 861112, 861202, 'DEMO_BLB_B1', 'PURCHASE_IN', 870024, DATE_SUB(CURDATE(), INTERVAL 8 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 50.00, 0.00, 0.00, 22.10, 'EXPIRED')
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

-- 6) 采购单（近30天，36单，覆盖 DRAFT/SUBMITTED/APPROVED/RECEIVED）
WITH RECURSIVE seq AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 35
)
INSERT INTO purchase_order (
    id, purchase_no, supplier_id, warehouse_id, order_status,
    order_date, expected_arrival_date, total_amount, created_by, received_by, remark
)
SELECT
    870000 + n,
    CONCAT('PO_DEMO_', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL n DAY), '%Y%m%d'), '_', LPAD(n, 2, '0')),
    861301 + MOD(n, 6),
    861201 + MOD(n, 3),
    CASE MOD(n, 4)
        WHEN 0 THEN 'DRAFT'
        WHEN 1 THEN 'SUBMITTED'
        WHEN 2 THEN 'APPROVED'
        ELSE 'RECEIVED'
    END,
    DATE_SUB(CURDATE(), INTERVAL n DAY),
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL n DAY), INTERVAL 2 DAY),
    0.00,
    1,
    CASE WHEN MOD(n, 4) = 3 THEN 2 ELSE NULL END,
    '毕设演示采购单'
FROM seq
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
    id, purchase_order_id, fruit_id, batch_no, production_date, expiration_date,
    quantity, received_qty, unit_price, subtotal, remark
)
SELECT
    871000 + (po.id - 870000),
    po.id,
    fi.id,
    CONCAT('POD', po.id, 'A'),
    DATE_SUB(po.order_date, INTERVAL 2 DAY),
    DATE_ADD(po.order_date, INTERVAL fi.shelf_life_days DAY),
    CAST(40 + MOD(po.id, 5) * 10 AS DECIMAL(10,2)) AS quantity,
    CASE po.order_status
        WHEN 'RECEIVED' THEN CAST(40 + MOD(po.id, 5) * 10 AS DECIMAL(10,2))
        WHEN 'APPROVED' THEN CAST((40 + MOD(po.id, 5) * 10) * 0.40 AS DECIMAL(10,2))
        ELSE 0.00
    END AS received_qty,
    fi.suggested_purchase_price,
    CAST((40 + MOD(po.id, 5) * 10) * fi.suggested_purchase_price AS DECIMAL(12,2)) AS subtotal,
    '演示采购明细A'
FROM purchase_order po
JOIN fruit_info fi ON fi.id = (861101 + MOD(po.id, 12))
WHERE po.id BETWEEN 870000 AND 870035;

INSERT INTO purchase_order_item (
    id, purchase_order_id, fruit_id, batch_no, production_date, expiration_date,
    quantity, received_qty, unit_price, subtotal, remark
)
SELECT
    872000 + (po.id - 870000),
    po.id,
    fi.id,
    CONCAT('POD', po.id, 'B'),
    DATE_SUB(po.order_date, INTERVAL 1 DAY),
    DATE_ADD(po.order_date, INTERVAL fi.shelf_life_days DAY),
    CAST(20 + MOD(po.id, 4) * 5 AS DECIMAL(10,2)) AS quantity,
    CASE po.order_status
        WHEN 'RECEIVED' THEN CAST(20 + MOD(po.id, 4) * 5 AS DECIMAL(10,2))
        WHEN 'APPROVED' THEN CAST((20 + MOD(po.id, 4) * 5) * 0.50 AS DECIMAL(10,2))
        ELSE 0.00
    END AS received_qty,
    fi.suggested_purchase_price,
    CAST((20 + MOD(po.id, 4) * 5) * fi.suggested_purchase_price AS DECIMAL(12,2)) AS subtotal,
    '演示采购明细B'
FROM purchase_order po
JOIN fruit_info fi ON fi.id = (861101 + MOD(po.id + 5, 12))
WHERE po.id BETWEEN 870000 AND 870035
  AND MOD(po.id, 3) = 0;

UPDATE purchase_order po
JOIN (
    SELECT purchase_order_id, SUM(subtotal) AS sum_amount
    FROM purchase_order_item
    WHERE purchase_order_id BETWEEN 870000 AND 870035
    GROUP BY purchase_order_id
) t ON po.id = t.purchase_order_id
SET po.total_amount = t.sum_amount
WHERE po.id BETWEEN 870000 AND 870035;

-- 7) 销售单（近3个月，90单；近30天覆盖多状态）
WITH RECURSIVE seq2 AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM seq2 WHERE n < 89
)
INSERT INTO sales_order (
    id, sales_no, customer_id, warehouse_id, order_status,
    order_time, total_amount, created_by, remark
)
SELECT
    880000 + n,
    CONCAT('SO_DEMO_', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL n DAY), '%Y%m%d'), '_', LPAD(n, 3, '0')),
    861401 + MOD(n, 6),
    861201 + MOD(n, 3),
    CASE
        WHEN n < 30 THEN
            CASE MOD(n, 5)
                WHEN 0 THEN 'DRAFT'
                WHEN 1 THEN 'SUBMITTED'
                WHEN 2 THEN 'APPROVED'
                WHEN 3 THEN 'SHIPPED'
                ELSE 'CONFIRMED'
            END
        ELSE 'CONFIRMED'
    END,
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL n DAY), INTERVAL (8 + MOD(n, 10)) HOUR),
    0.00,
    3,
    '毕设演示销售单'
FROM seq2
ON DUPLICATE KEY UPDATE
    customer_id = VALUES(customer_id),
    warehouse_id = VALUES(warehouse_id),
    order_status = VALUES(order_status),
    order_time = VALUES(order_time),
    total_amount = VALUES(total_amount),
    created_by = VALUES(created_by),
    remark = VALUES(remark);

INSERT INTO sales_order_item (
    id, sales_order_id, fruit_id, batch_id,
    quantity, shipped_qty, unit_price, subtotal, remark
)
SELECT
    881000 + (so.id - 880000),
    so.id,
    CASE MOD(so.id, 5)
        WHEN 0 THEN 861101
        WHEN 1 THEN 861102
        WHEN 2 THEN 861103
        WHEN 3 THEN 861108
        ELSE 861110
    END AS fruit_id,
    NULL,
    CAST(18 + MOD(so.id, 7) * 3 AS DECIMAL(10,2)) AS quantity,
    CASE so.order_status
        WHEN 'SHIPPED' THEN CAST(18 + MOD(so.id, 7) * 3 AS DECIMAL(10,2))
        WHEN 'CONFIRMED' THEN CAST(18 + MOD(so.id, 7) * 3 AS DECIMAL(10,2))
        WHEN 'APPROVED' THEN CAST((18 + MOD(so.id, 7) * 3) * 0.50 AS DECIMAL(10,2))
        ELSE 0.00
    END AS shipped_qty,
    fi.suggested_sale_price,
    CAST((18 + MOD(so.id, 7) * 3) * fi.suggested_sale_price AS DECIMAL(12,2)) AS subtotal,
    '演示销售明细A'
FROM sales_order so
JOIN fruit_info fi ON fi.id = CASE MOD(so.id, 5)
    WHEN 0 THEN 861101
    WHEN 1 THEN 861102
    WHEN 2 THEN 861103
    WHEN 3 THEN 861108
    ELSE 861110
END
WHERE so.id BETWEEN 880000 AND 880089;

INSERT INTO sales_order_item (
    id, sales_order_id, fruit_id, batch_id,
    quantity, shipped_qty, unit_price, subtotal, remark
)
SELECT
    882000 + (so.id - 880000),
    so.id,
    CASE MOD(so.id + 2, 5)
        WHEN 0 THEN 861101
        WHEN 1 THEN 861102
        WHEN 2 THEN 861103
        WHEN 3 THEN 861108
        ELSE 861110
    END AS fruit_id,
    NULL,
    CAST(10 + MOD(so.id, 6) * 2 AS DECIMAL(10,2)) AS quantity,
    CASE so.order_status
        WHEN 'SHIPPED' THEN CAST(10 + MOD(so.id, 6) * 2 AS DECIMAL(10,2))
        WHEN 'CONFIRMED' THEN CAST(10 + MOD(so.id, 6) * 2 AS DECIMAL(10,2))
        WHEN 'APPROVED' THEN CAST((10 + MOD(so.id, 6) * 2) * 0.50 AS DECIMAL(10,2))
        ELSE 0.00
    END AS shipped_qty,
    fi.suggested_sale_price,
    CAST((10 + MOD(so.id, 6) * 2) * fi.suggested_sale_price AS DECIMAL(12,2)) AS subtotal,
    '演示销售明细B'
FROM sales_order so
JOIN fruit_info fi ON fi.id = CASE MOD(so.id + 2, 5)
    WHEN 0 THEN 861101
    WHEN 1 THEN 861102
    WHEN 2 THEN 861103
    WHEN 3 THEN 861108
    ELSE 861110
END
WHERE so.id BETWEEN 880000 AND 880089
  AND MOD(so.id, 2) = 0;

UPDATE sales_order so
JOIN (
    SELECT sales_order_id, SUM(subtotal) AS sum_amount
    FROM sales_order_item
    WHERE sales_order_id BETWEEN 880000 AND 880089
    GROUP BY sales_order_id
) t ON so.id = t.sales_order_id
SET so.total_amount = t.sum_amount
WHERE so.id BETWEEN 880000 AND 880089;

-- 8) 库存预警（未处理）
INSERT INTO inventory_alert (
    id, alert_type, fruit_id, batch_id, warehouse_id, alert_level,
    alert_msg, threshold_value, current_value, alert_status, created_time
) VALUES
(896000, 'LOW_STOCK', 861104, 862006, 861201, 'HIGH', '葡萄库存低于安全库存，请及时补货', 120.00, 22.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(896001, 'LOW_STOCK', 861105, 862008, 861201, 'HIGH', '草莓库存低于安全库存，请及时补货', 90.00, 18.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(896002, 'LOW_STOCK', 861109, 862016, 861201, 'MEDIUM', '菠萝库存偏低，建议跟进采购', 130.00, 26.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(896003, 'EXPIRING', 861102, 862003, 861203, 'MEDIUM', '香蕉批次2天内到期，请优先销售', 3.00, 2.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(896004, 'EXPIRING', 861105, 862008, 861201, 'HIGH', '草莓批次1天内到期，请尽快处理', 2.00, 1.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(896005, 'EXPIRING', 861111, 862021, 861203, 'MEDIUM', '桃子批次临期，请安排促销出库', 2.00, 1.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 1 HOUR))
ON DUPLICATE KEY UPDATE
    alert_type = VALUES(alert_type),
    fruit_id = VALUES(fruit_id),
    batch_id = VALUES(batch_id),
    warehouse_id = VALUES(warehouse_id),
    alert_level = VALUES(alert_level),
    alert_msg = VALUES(alert_msg),
    threshold_value = VALUES(threshold_value),
    current_value = VALUES(current_value),
    alert_status = VALUES(alert_status),
    created_time = VALUES(created_time);

-- 9) AI演示数据：销量预测结果（7天*5水果）
WITH RECURSIVE d AS (
    SELECT 1 AS day_idx
    UNION ALL
    SELECT day_idx + 1 FROM d WHERE day_idx < 7
), f AS (
    SELECT 0 AS k, 861101 AS fruit_id UNION ALL
    SELECT 1, 861102 UNION ALL
    SELECT 2, 861103 UNION ALL
    SELECT 3, 861108 UNION ALL
    SELECT 4, 861110
)
INSERT INTO ai_forecast_result (
    id, fruit_id, forecast_date, target_date, model_name, version_no,
    predict_qty, confidence_lower, confidence_upper, data_window_days, created_time
)
SELECT
    897000 + ((day_idx - 1) * 5) + k,
    fruit_id,
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL day_idx DAY),
    'demo-model',
    'v1',
    CAST(45 + k * 6 + day_idx * 2 AS DECIMAL(10,2)) AS predict_qty,
    CAST((45 + k * 6 + day_idx * 2) * 0.90 AS DECIMAL(10,2)) AS confidence_lower,
    CAST((45 + k * 6 + day_idx * 2) * 1.10 AS DECIMAL(10,2)) AS confidence_upper,
    90,
    NOW()
FROM d
CROSS JOIN f
ON DUPLICATE KEY UPDATE
    forecast_date = VALUES(forecast_date),
    predict_qty = VALUES(predict_qty),
    confidence_lower = VALUES(confidence_lower),
    confidence_upper = VALUES(confidence_upper),
    data_window_days = VALUES(data_window_days),
    created_time = VALUES(created_time);

-- 10) AI演示数据：采购建议
INSERT INTO ai_purchase_suggestion (
    id, fruit_id, warehouse_id, suggestion_date,
    predicted_daily_qty, lead_time_days, safety_stock_qty, current_stock_qty,
    in_transit_qty, recommended_purchase_qty, status, reason, created_time
) VALUES
(898000, 861104, 861201, CURDATE(), 42.00, 2, 126.00, 22.00, 10.00, 114.00, 'NEW', '演示建议：葡萄低库存且销量上升，建议及时补货', NOW()),
(898001, 861105, 861201, CURDATE(), 36.00, 1, 72.00, 18.00, 0.00, 54.00, 'NEW', '演示建议：草莓临期与低库存并存，建议小批量高频补货', NOW()),
(898002, 861109, 861201, CURDATE(), 30.00, 2, 90.00, 26.00, 8.00, 56.00, 'NEW', '演示建议：菠萝库存偏低，建议补货保持周转', NOW()),
(898003, 861102, 861203, CURDATE(), 52.00, 1, 104.00, 65.00, 20.00, 19.00, 'NEW', '演示建议：香蕉需求稳定，建议按日补货', NOW()),
(898004, 861108, 861203, CURDATE(), 40.00, 2, 120.00, 52.00, 12.00, 56.00, 'NEW', '演示建议：芒果需求高峰，建议提前备货', NOW()),
(898005, 861103, 861202, CURDATE(), 34.00, 2, 102.00, 45.00, 15.00, 42.00, 'NEW', '演示建议：橙子进入旺销期，建议适度补货', NOW())
ON DUPLICATE KEY UPDATE
    predicted_daily_qty = VALUES(predicted_daily_qty),
    lead_time_days = VALUES(lead_time_days),
    safety_stock_qty = VALUES(safety_stock_qty),
    current_stock_qty = VALUES(current_stock_qty),
    in_transit_qty = VALUES(in_transit_qty),
    recommended_purchase_qty = VALUES(recommended_purchase_qty),
    status = VALUES(status),
    reason = VALUES(reason),
    created_time = VALUES(created_time);

-- 11) 导入后快速检查（可选）
-- SELECT COUNT(*) AS fruit_count FROM fruit_info WHERE id BETWEEN 861100 AND 861199;
-- SELECT COUNT(*) AS supplier_count FROM supplier WHERE id BETWEEN 861300 AND 861399;
-- SELECT COUNT(*) AS customer_count FROM customer WHERE id BETWEEN 861400 AND 861499;
-- SELECT COUNT(*) AS warehouse_count FROM warehouse WHERE id BETWEEN 861200 AND 861299;
-- SELECT COUNT(*) AS inventory_batch_count FROM inventory_batch WHERE id BETWEEN 862000 AND 862099;
-- SELECT order_status, COUNT(*) FROM purchase_order WHERE id BETWEEN 870000 AND 870999 GROUP BY order_status;
-- SELECT order_status, COUNT(*) FROM sales_order WHERE id BETWEEN 880000 AND 880999 GROUP BY order_status;
-- SELECT COUNT(*) AS unhandled_alerts FROM inventory_alert WHERE id BETWEEN 896000 AND 896999 AND alert_status = 'UNHANDLED';
