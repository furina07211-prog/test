USE fruit_warehouse_manage;

INSERT INTO sys_role (id, role_code, role_name, remark, status) VALUES
(1,'ADMIN','Administrator','system admin',1),
(2,'WAREHOUSE','Warehouse','warehouse operator',1),
(3,'SALES','Sales','sales operator',1)
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name), remark=VALUES(remark), status=VALUES(status);

INSERT INTO sys_user (id, username, password, real_name, phone, email, status) VALUES
(1,'admin','123456','Admin User','13800000001','admin@fruit.local',1),
(2,'warehouse','123456','Warehouse User','13800000002','warehouse@fruit.local',1),
(3,'sales','123456','Sales User','13800000003','sales@fruit.local',1)
ON DUPLICATE KEY UPDATE password=VALUES(password), real_name=VALUES(real_name), status=VALUES(status);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1,1),(2,2),(3,3)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

INSERT INTO fruit_category (id, category_name, sort_no, status, remark) VALUES
(1,'Citrus',1,1,'demo'),
(2,'Berry',2,1,'demo'),
(3,'Tropical',3,1,'demo')
ON DUPLICATE KEY UPDATE category_name=VALUES(category_name), sort_no=VALUES(sort_no), status=VALUES(status);

INSERT INTO warehouse (id, warehouse_code, warehouse_name, location, manager_name, phone, status) VALUES
(1,'WH001','Main Cold Storage','South Hub','Li','13911110001',1),
(2,'WH002','Ambient Storage','South Hub','Wang','13911110002',1)
ON DUPLICATE KEY UPDATE warehouse_name=VALUES(warehouse_name), location=VALUES(location), status=VALUES(status);

INSERT INTO supplier (id, supplier_code, supplier_name, contact_person, phone, address, credit_level, status, remark) VALUES
(1,'SUP001','Fresh Source Ltd','Chen','13600000001','Market A','A',1,'demo supplier'),
(2,'SUP002','Golden Fruit Co','Huang','13600000002','Market B','B',1,'demo supplier')
ON DUPLICATE KEY UPDATE supplier_name=VALUES(supplier_name), status=VALUES(status);

INSERT INTO customer (id, customer_code, customer_name, contact_person, phone, address, customer_level, status, remark) VALUES
(1,'CUS001','City Fresh Market','Zhao','13700000001','Block A','NORMAL',1,'demo customer'),
(2,'CUS002','Preferred Shop','Sun','13700000002','Block B','A',1,'demo customer')
ON DUPLICATE KEY UPDATE customer_name=VALUES(customer_name), status=VALUES(status);

INSERT INTO fruit_info (id, fruit_code, fruit_name, category_id, unit, origin_place, shelf_life_days, warning_days, safe_stock_qty, suggested_purchase_price, suggested_sale_price, status, remark) VALUES
(1,'FR001','Apple',1,'kg','Shandong',20,3,120.00,6.50,10.00,1,'demo fruit'),
(2,'FR002','Banana',3,'kg','Guangxi',7,2,100.00,4.50,7.50,1,'demo fruit'),
(3,'FR003','Orange',1,'kg','Jiangxi',15,3,80.00,5.20,8.80,1,'demo fruit')
ON DUPLICATE KEY UPDATE fruit_name=VALUES(fruit_name), category_id=VALUES(category_id), status=VALUES(status);

INSERT INTO inventory_batch (id, fruit_id, warehouse_id, batch_no, source_type, source_id, production_date, expiration_date, total_qty, available_qty, locked_qty, unit_cost, status) VALUES
(1,1,1,'BATCH-APPLE-001','PURCHASE_IN',1,'2026-03-20','2026-04-15',300.00,300.00,0.00,6.20,'IN_STOCK'),
(2,2,1,'BATCH-BANANA-001','PURCHASE_IN',1,'2026-03-22','2026-03-31',180.00,180.00,0.00,4.20,'IN_STOCK')
ON DUPLICATE KEY UPDATE available_qty=VALUES(available_qty), total_qty=VALUES(total_qty), status=VALUES(status);

INSERT INTO purchase_order (id,purchase_no,supplier_id,warehouse_id,order_status,order_date,expected_arrival_date,total_amount,created_by,remark) VALUES
(1,'PO_DEMO_0001',1,1,'DRAFT','2026-03-25','2026-03-28',620.00,1,'demo purchase')
ON DUPLICATE KEY UPDATE order_status=VALUES(order_status), total_amount=VALUES(total_amount);

INSERT INTO purchase_order_item (id,purchase_order_id,fruit_id,batch_no,production_date,expiration_date,quantity,received_qty,unit_price,subtotal,remark) VALUES
(1,1,1,'PO-APPLE-001','2026-03-24','2026-04-20',100.00,0.00,6.20,620.00,'demo purchase item')
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity), unit_price=VALUES(unit_price), subtotal=VALUES(subtotal);

INSERT INTO sales_order (id,sales_no,customer_id,warehouse_id,order_status,order_time,total_amount,created_by,remark) VALUES
(1,'SO_DEMO_0001',1,1,'CONFIRMED','2026-03-20 10:00:00',300.00,3,'demo sales'),
(2,'SO_DEMO_0002',2,1,'CONFIRMED','2026-03-21 10:00:00',330.00,3,'demo sales'),
(3,'SO_DEMO_0003',1,1,'CONFIRMED','2026-03-22 10:00:00',360.00,3,'demo sales')
ON DUPLICATE KEY UPDATE total_amount=VALUES(total_amount), order_status=VALUES(order_status);

INSERT INTO sales_order_item (id,sales_order_id,fruit_id,batch_id,quantity,unit_price,subtotal,remark) VALUES
(1,1,1,1,30.00,10.00,300.00,'demo item'),
(2,2,1,1,33.00,10.00,330.00,'demo item'),
(3,3,1,1,36.00,10.00,360.00,'demo item')
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity), subtotal=VALUES(subtotal);

INSERT INTO inventory_alert (id,alert_type,fruit_id,batch_id,warehouse_id,alert_level,alert_msg,threshold_value,current_value,alert_status,created_time) VALUES
(1,'LOW_STOCK',1,1,1,'MEDIUM','stock warning',120.00,80.00,'UNHANDLED',NOW()),
(2,'NEAR_EXPIRATION',2,2,1,'HIGH','near expiry',3.00,2.00,'UNHANDLED',NOW())
ON DUPLICATE KEY UPDATE alert_status=VALUES(alert_status), current_value=VALUES(current_value);