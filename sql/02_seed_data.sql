USE fruit_warehouse_manage;

INSERT INTO sys_role (id, role_code, role_name, remark, status) VALUES
(1, 'ADMIN', '系统管理员', '系统全权限', 1),
(2, 'WAREHOUSE', '仓库管理员', '入库、盘点、预警', 1),
(3, 'SALES', '销售员', '销售出库、客户维护、智能问答', 1)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), remark = VALUES(remark), status = VALUES(status);

INSERT INTO sys_user (id, username, password, real_name, phone, email, status) VALUES
(1, 'admin', '123456', '系统管理员', '13800000001', 'admin@fruit.com', 1),
(2, 'warehouse', '123456', '仓库主管', '13800000002', 'warehouse@fruit.com', 1),
(3, 'sales', '123456', '销售主管', '13800000003', 'sales@fruit.com', 1)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), status = VALUES(status);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO fruit_category (id, category_name, sort_no, status) VALUES
(1, '柑橘类', 1, 1),
(2, '浆果类', 2, 1),
(3, '核果类', 3, 1)
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name), sort_no = VALUES(sort_no);

INSERT INTO warehouse (id, warehouse_code, warehouse_name, location, manager_name, phone, status) VALUES
(1, 'WH001', '一号冷库', '华南分拨中心', '李仓', '13911110001', 1),
(2, 'WH002', '二号常温库', '华南分拨中心', '王仓', '13911110002', 1)
ON DUPLICATE KEY UPDATE warehouse_name = VALUES(warehouse_name), location = VALUES(location);

INSERT INTO supplier (id, supplier_code, supplier_name, contact_person, phone, status) VALUES
(1, 'SUP001', '岭南果业', '陈老板', '13600000001', 1),
(2, 'SUP002', '南方鲜果供应链', '黄老板', '13600000002', 1)
ON DUPLICATE KEY UPDATE supplier_name = VALUES(supplier_name), contact_person = VALUES(contact_person);

INSERT INTO customer (id, customer_code, customer_name, contact_person, phone, status) VALUES
(1, 'CUS001', '城市生鲜市场', '赵采购', '13700000001', 1),
(2, 'CUS002', '优选商超', '孙经理', '13700000002', 1)
ON DUPLICATE KEY UPDATE customer_name = VALUES(customer_name), contact_person = VALUES(contact_person);

INSERT INTO fruit_info (id, fruit_code, fruit_name, category_id, unit, origin_place, shelf_life_days, warning_days, safe_stock_qty, suggested_purchase_price, suggested_sale_price, status) VALUES
(1, 'FR001', '苹果', 3, 'kg', '山东烟台', 20, 3, 120.00, 6.50, 10.00, 1),
(2, 'FR002', '香蕉', 3, 'kg', '广西南宁', 7, 2, 100.00, 4.50, 7.50, 1),
(3, 'FR003', '橙子', 1, 'kg', '江西赣州', 15, 3, 80.00, 5.20, 8.80, 1)
ON DUPLICATE KEY UPDATE fruit_name = VALUES(fruit_name), category_id = VALUES(category_id), safe_stock_qty = VALUES(safe_stock_qty);
