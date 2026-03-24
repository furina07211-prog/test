-- =============================================
-- Fruit Wholesale Warehouse Management System
-- Database Initialization Script
-- =============================================

CREATE DATABASE IF NOT EXISTS fruit_warehouse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE fruit_warehouse;

-- ----------------------------
-- 1. System User Table
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    username VARCHAR(50) NOT NULL COMMENT 'Username',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt Password',
    real_name VARCHAR(50) NOT NULL COMMENT 'Real Name',
    phone VARCHAR(20) DEFAULT NULL COMMENT 'Phone',
    email VARCHAR(100) DEFAULT NULL COMMENT 'Email',
    avatar VARCHAR(255) DEFAULT NULL COMMENT 'Avatar URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=active, 0=disabled',
    last_login_time DATETIME DEFAULT NULL COMMENT 'Last Login',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical Delete',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System User';

-- ----------------------------
-- 2. System Role Table
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    role_name VARCHAR(50) NOT NULL COMMENT 'Role Name',
    role_code VARCHAR(50) NOT NULL COMMENT 'Role Code',
    description VARCHAR(200) DEFAULT NULL COMMENT 'Description',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=active',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical Delete',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Role';

-- ----------------------------
-- 3. User-Role Mapping
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    create_time DATETIME NOT NULL COMMENT 'Created',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User-Role Mapping';

-- ----------------------------
-- 4. System Permission Table
-- ----------------------------
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    permission_name VARCHAR(100) NOT NULL COMMENT 'Permission Name',
    permission_code VARCHAR(100) NOT NULL COMMENT 'Permission Code',
    module VARCHAR(50) NOT NULL COMMENT 'Module',
    description VARCHAR(200) DEFAULT NULL COMMENT 'Description',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical Delete',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Permission';

-- ----------------------------
-- 5. Role-Permission Mapping
-- ----------------------------
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    permission_id BIGINT NOT NULL COMMENT 'Permission ID',
    create_time DATETIME NOT NULL COMMENT 'Created',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Role-Permission Mapping';

-- ----------------------------
-- 6. Category Table
-- ----------------------------
DROP TABLE IF EXISTS category;
CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    category_name VARCHAR(50) NOT NULL COMMENT 'Category Name',
    sort_order INT DEFAULT 0 COMMENT 'Sort Order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=active',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical Delete',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product Category';

-- ----------------------------
-- 7. Product Table
-- ----------------------------
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    product_code VARCHAR(50) NOT NULL COMMENT 'Product Code',
    product_name VARCHAR(100) NOT NULL COMMENT 'Product Name',
    category_id BIGINT NOT NULL COMMENT 'Category ID',
    unit VARCHAR(20) NOT NULL COMMENT 'Unit (kg/box/crate)',
    spec VARCHAR(100) DEFAULT NULL COMMENT 'Specification',
    shelf_life_days INT NOT NULL COMMENT 'Shelf Life in Days',
    low_stock_threshold INT NOT NULL DEFAULT 100 COMMENT 'Low Stock Threshold',
    image_url VARCHAR(255) DEFAULT NULL COMMENT 'Image',
    description VARCHAR(500) DEFAULT NULL COMMENT 'Description',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=active',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical Delete',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_code (product_code),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product';

-- ----------------------------
-- 8. Supplier Table
-- ----------------------------
DROP TABLE IF EXISTS supplier;
CREATE TABLE supplier (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    supplier_code VARCHAR(50) NOT NULL COMMENT 'Supplier Code',
    supplier_name VARCHAR(100) NOT NULL COMMENT 'Supplier Name',
    contact_person VARCHAR(50) DEFAULT NULL COMMENT 'Contact Person',
    contact_phone VARCHAR(20) DEFAULT NULL COMMENT 'Contact Phone',
    email VARCHAR(100) DEFAULT NULL COMMENT 'Email',
    address VARCHAR(200) DEFAULT NULL COMMENT 'Address',
    bank_account VARCHAR(50) DEFAULT NULL COMMENT 'Bank Account',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=active',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical Delete',
    PRIMARY KEY (id),
    UNIQUE KEY uk_supplier_code (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Supplier';

-- ----------------------------
-- 9. Warehouse Table
-- ----------------------------
DROP TABLE IF EXISTS warehouse;
CREATE TABLE warehouse (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    warehouse_code VARCHAR(50) NOT NULL COMMENT 'Warehouse Code',
    warehouse_name VARCHAR(100) NOT NULL COMMENT 'Warehouse Name',
    warehouse_type TINYINT NOT NULL DEFAULT 1 COMMENT '1=normal, 2=cold-chain, 3=frozen',
    address VARCHAR(200) DEFAULT NULL COMMENT 'Address',
    capacity DECIMAL(12,2) DEFAULT NULL COMMENT 'Total Capacity',
    manager_id BIGINT DEFAULT NULL COMMENT 'Manager User ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=active',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical Delete',
    PRIMARY KEY (id),
    UNIQUE KEY uk_warehouse_code (warehouse_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Warehouse';

-- ----------------------------
-- 10. Inventory Table
-- ----------------------------
DROP TABLE IF EXISTS inventory;
CREATE TABLE inventory (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    product_id BIGINT NOT NULL COMMENT 'Product ID',
    warehouse_id BIGINT NOT NULL COMMENT 'Warehouse ID',
    total_quantity DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'Total Stock',
    locked_quantity DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'Locked Quantity',
    available_quantity DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'Available Quantity',
    version INT NOT NULL DEFAULT 0 COMMENT 'Optimistic Lock Version',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_warehouse (product_id, warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory';

-- ----------------------------
-- 11. Inventory Batch Table
-- ----------------------------
DROP TABLE IF EXISTS inventory_batch;
CREATE TABLE inventory_batch (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    batch_code VARCHAR(50) NOT NULL COMMENT 'Batch Code',
    product_id BIGINT NOT NULL COMMENT 'Product ID',
    warehouse_id BIGINT NOT NULL COMMENT 'Warehouse ID',
    supplier_id BIGINT DEFAULT NULL COMMENT 'Supplier ID',
    inbound_order_id BIGINT DEFAULT NULL COMMENT 'Source Inbound Order',
    quantity DECIMAL(12,2) NOT NULL COMMENT 'Initial Quantity',
    remaining_quantity DECIMAL(12,2) NOT NULL COMMENT 'Remaining Quantity',
    unit_cost DECIMAL(10,2) DEFAULT NULL COMMENT 'Unit Cost',
    production_date DATE DEFAULT NULL COMMENT 'Production Date',
    expiry_date DATE NOT NULL COMMENT 'Expiry Date',
    batch_status TINYINT NOT NULL DEFAULT 1 COMMENT '1=normal, 2=expiring, 3=expired, 4=depleted',
    version INT NOT NULL DEFAULT 0 COMMENT 'Optimistic Lock Version',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical Delete',
    PRIMARY KEY (id),
    UNIQUE KEY uk_batch_code (batch_code),
    KEY idx_product_warehouse_status (product_id, warehouse_id, batch_status),
    KEY idx_expiry_date (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory Batch';

-- ----------------------------
-- 12. Inbound Order Table
-- ----------------------------
DROP TABLE IF EXISTS inbound_order;
CREATE TABLE inbound_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    order_code VARCHAR(50) NOT NULL COMMENT 'Order Code',
    supplier_id BIGINT NOT NULL COMMENT 'Supplier ID',
    warehouse_id BIGINT NOT NULL COMMENT 'Warehouse ID',
    order_status TINYINT NOT NULL DEFAULT 0 COMMENT '0=draft,1=pending,2=approved,3=receiving,4=completed,5=cancelled',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT 'Total Amount',
    expected_date DATE DEFAULT NULL COMMENT 'Expected Date',
    actual_date DATE DEFAULT NULL COMMENT 'Actual Complete Date',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    creator_id BIGINT NOT NULL COMMENT 'Creator',
    reviewer_id BIGINT DEFAULT NULL COMMENT 'Reviewer',
    review_time DATETIME DEFAULT NULL COMMENT 'Review Time',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical Delete',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_code (order_code),
    KEY idx_supplier_id (supplier_id),
    KEY idx_warehouse_id (warehouse_id),
    KEY idx_order_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inbound Order';

-- ----------------------------
-- 13. Inbound Order Detail Table
-- ----------------------------
DROP TABLE IF EXISTS inbound_order_detail;
CREATE TABLE inbound_order_detail (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    inbound_order_id BIGINT NOT NULL COMMENT 'Inbound Order ID',
    product_id BIGINT NOT NULL COMMENT 'Product ID',
    expected_quantity DECIMAL(12,2) NOT NULL COMMENT 'Expected Quantity',
    actual_quantity DECIMAL(12,2) DEFAULT 0 COMMENT 'Actual Quantity',
    unit_cost DECIMAL(10,2) NOT NULL COMMENT 'Unit Cost',
    amount DECIMAL(12,2) DEFAULT NULL COMMENT 'Amount',
    production_date DATE DEFAULT NULL COMMENT 'Production Date',
    expiry_date DATE DEFAULT NULL COMMENT 'Expiry Date',
    batch_id BIGINT DEFAULT NULL COMMENT 'Generated Batch ID',
    remark VARCHAR(200) DEFAULT NULL COMMENT 'Remark',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    PRIMARY KEY (id),
    KEY idx_inbound_order_id (inbound_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inbound Order Detail';

-- ----------------------------
-- 14. Inbound Record Table
-- ----------------------------
DROP TABLE IF EXISTS inbound_record;
CREATE TABLE inbound_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    inbound_order_id BIGINT NOT NULL COMMENT 'Inbound Order ID',
    inbound_detail_id BIGINT NOT NULL COMMENT 'Inbound Detail ID',
    product_id BIGINT NOT NULL COMMENT 'Product ID',
    warehouse_id BIGINT NOT NULL COMMENT 'Warehouse ID',
    batch_id BIGINT NOT NULL COMMENT 'Batch ID',
    quantity DECIMAL(12,2) NOT NULL COMMENT 'Quantity',
    operator_id BIGINT NOT NULL COMMENT 'Operator',
    operate_time DATETIME NOT NULL COMMENT 'Operate Time',
    remark VARCHAR(200) DEFAULT NULL COMMENT 'Remark',
    create_time DATETIME NOT NULL COMMENT 'Created',
    PRIMARY KEY (id),
    KEY idx_inbound_order_id (inbound_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inbound Record';

-- ----------------------------
-- 15. Outbound Order Table
-- ----------------------------
DROP TABLE IF EXISTS outbound_order;
CREATE TABLE outbound_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    order_code VARCHAR(50) NOT NULL COMMENT 'Order Code',
    warehouse_id BIGINT NOT NULL COMMENT 'Warehouse ID',
    order_status TINYINT NOT NULL DEFAULT 0 COMMENT '0=draft,1=pending,2=approved,3=picking,4=completed,5=cancelled',
    order_type TINYINT NOT NULL DEFAULT 1 COMMENT '1=sales,2=transfer,3=loss,4=sample',
    customer_name VARCHAR(100) DEFAULT NULL COMMENT 'Customer',
    customer_phone VARCHAR(20) DEFAULT NULL COMMENT 'Customer Phone',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT 'Total Amount',
    expected_date DATE DEFAULT NULL COMMENT 'Expected Date',
    actual_date DATE DEFAULT NULL COMMENT 'Actual Date',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    creator_id BIGINT NOT NULL COMMENT 'Creator',
    reviewer_id BIGINT DEFAULT NULL COMMENT 'Reviewer',
    review_time DATETIME DEFAULT NULL COMMENT 'Review Time',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical Delete',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_code (order_code),
    KEY idx_warehouse_id (warehouse_id),
    KEY idx_order_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Outbound Order';

-- ----------------------------
-- 16. Outbound Order Detail Table
-- ----------------------------
DROP TABLE IF EXISTS outbound_order_detail;
CREATE TABLE outbound_order_detail (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    outbound_order_id BIGINT NOT NULL COMMENT 'Outbound Order ID',
    product_id BIGINT NOT NULL COMMENT 'Product ID',
    quantity DECIMAL(12,2) NOT NULL COMMENT 'Requested Quantity',
    actual_quantity DECIMAL(12,2) DEFAULT 0 COMMENT 'Actual Picked',
    unit_price DECIMAL(10,2) NOT NULL COMMENT 'Unit Price',
    amount DECIMAL(12,2) DEFAULT NULL COMMENT 'Amount',
    remark VARCHAR(200) DEFAULT NULL COMMENT 'Remark',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    PRIMARY KEY (id),
    KEY idx_outbound_order_id (outbound_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Outbound Order Detail';

-- ----------------------------
-- 17. Outbound Record Table
-- ----------------------------
DROP TABLE IF EXISTS outbound_record;
CREATE TABLE outbound_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    outbound_order_id BIGINT NOT NULL COMMENT 'Outbound Order ID',
    outbound_detail_id BIGINT NOT NULL COMMENT 'Outbound Detail ID',
    product_id BIGINT NOT NULL COMMENT 'Product ID',
    warehouse_id BIGINT NOT NULL COMMENT 'Warehouse ID',
    batch_id BIGINT NOT NULL COMMENT 'Batch ID',
    quantity DECIMAL(12,2) NOT NULL COMMENT 'Quantity',
    operator_id BIGINT NOT NULL COMMENT 'Operator',
    operate_time DATETIME NOT NULL COMMENT 'Operate Time',
    remark VARCHAR(200) DEFAULT NULL COMMENT 'Remark',
    create_time DATETIME NOT NULL COMMENT 'Created',
    PRIMARY KEY (id),
    KEY idx_outbound_order_id (outbound_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Outbound Record';

-- ----------------------------
-- 18. Alert Table
-- ----------------------------
DROP TABLE IF EXISTS alert;
CREATE TABLE alert (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    alert_type TINYINT NOT NULL COMMENT '1=low_stock, 2=expiring_soon, 3=expired',
    product_id BIGINT NOT NULL COMMENT 'Product ID',
    warehouse_id BIGINT DEFAULT NULL COMMENT 'Warehouse ID',
    batch_id BIGINT DEFAULT NULL COMMENT 'Batch ID',
    alert_content VARCHAR(500) NOT NULL COMMENT 'Alert Content',
    threshold_value DECIMAL(12,2) DEFAULT NULL COMMENT 'Threshold',
    current_value DECIMAL(12,2) DEFAULT NULL COMMENT 'Current Value',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=unread, 1=read, 2=handled',
    handler_id BIGINT DEFAULT NULL COMMENT 'Handler',
    handle_time DATETIME DEFAULT NULL COMMENT 'Handle Time',
    handle_remark VARCHAR(200) DEFAULT NULL COMMENT 'Handle Remark',
    create_time DATETIME NOT NULL COMMENT 'Created',
    update_time DATETIME NOT NULL COMMENT 'Updated',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical Delete',
    PRIMARY KEY (id),
    KEY idx_alert_type (alert_type),
    KEY idx_product_id (product_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Alert';

-- ----------------------------
-- 19. Inventory Snapshot Table
-- ----------------------------
DROP TABLE IF EXISTS inventory_snapshot;
CREATE TABLE inventory_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    snapshot_date DATE NOT NULL COMMENT 'Snapshot Date',
    product_id BIGINT NOT NULL COMMENT 'Product ID',
    warehouse_id BIGINT NOT NULL COMMENT 'Warehouse ID',
    total_quantity DECIMAL(12,2) NOT NULL COMMENT 'Total Quantity',
    create_time DATETIME NOT NULL COMMENT 'Created',
    PRIMARY KEY (id),
    UNIQUE KEY uk_snapshot (snapshot_date, product_id, warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory Snapshot';

-- ----------------------------
-- 20. Operation Log Table
-- ----------------------------
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    username VARCHAR(50) NOT NULL COMMENT 'Username',
    operation VARCHAR(50) NOT NULL COMMENT 'Operation',
    method VARCHAR(200) DEFAULT NULL COMMENT 'Method',
    params TEXT DEFAULT NULL COMMENT 'Params (JSON)',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP',
    result_status TINYINT DEFAULT NULL COMMENT '1=success, 0=fail',
    error_msg VARCHAR(500) DEFAULT NULL COMMENT 'Error Message',
    duration BIGINT DEFAULT NULL COMMENT 'Duration (ms)',
    create_time DATETIME NOT NULL COMMENT 'Created',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Operation Log';

-- =============================================
-- Seed Data
-- =============================================

-- Roles
INSERT INTO sys_role (id, role_name, role_code, description, status, create_time, update_time) VALUES
(1, '系统管理员', 'ADMIN', '拥有系统最高权限', 1, NOW(), NOW()),
(2, '仓库管理员', 'WAREHOUSE_MANAGER', '负责日常仓储的核心调度', 1, NOW(), NOW()),
(3, '仓库工作人员', 'WAREHOUSE_WORKER', '执行具体的入库、出库操作', 1, NOW(), NOW()),
(4, '采购员', 'PURCHASER', '查看库存、制定采购计划', 1, NOW(), NOW());

-- Permissions
INSERT INTO sys_permission (id, permission_name, permission_code, module, description, create_time, update_time) VALUES
-- User Management
(1, '用户查看', 'user:list', 'system', '查看用户列表', NOW(), NOW()),
(2, '用户新增', 'user:create', 'system', '新增用户', NOW(), NOW()),
(3, '用户编辑', 'user:update', 'system', '编辑用户', NOW(), NOW()),
(4, '用户删除', 'user:delete', 'system', '删除用户', NOW(), NOW()),
-- Role Management
(5, '角色查看', 'role:list', 'system', '查看角色列表', NOW(), NOW()),
(6, '角色新增', 'role:create', 'system', '新增角色', NOW(), NOW()),
(7, '角色编辑', 'role:update', 'system', '编辑角色', NOW(), NOW()),
(8, '角色删除', 'role:delete', 'system', '删除角色', NOW(), NOW()),
-- Category Management
(9, '分类查看', 'category:list', 'base', '查看分类', NOW(), NOW()),
(10, '分类新增', 'category:create', 'base', '新增分类', NOW(), NOW()),
(11, '分类编辑', 'category:update', 'base', '编辑分类', NOW(), NOW()),
(12, '分类删除', 'category:delete', 'base', '删除分类', NOW(), NOW()),
-- Product Management
(13, '商品查看', 'product:list', 'base', '查看商品', NOW(), NOW()),
(14, '商品新增', 'product:create', 'base', '新增商品', NOW(), NOW()),
(15, '商品编辑', 'product:update', 'base', '编辑商品', NOW(), NOW()),
(16, '商品删除', 'product:delete', 'base', '删除商品', NOW(), NOW()),
-- Supplier Management
(17, '供应商查看', 'supplier:list', 'base', '查看供应商', NOW(), NOW()),
(18, '供应商新增', 'supplier:create', 'base', '新增供应商', NOW(), NOW()),
(19, '供应商编辑', 'supplier:update', 'base', '编辑供应商', NOW(), NOW()),
(20, '供应商删除', 'supplier:delete', 'base', '删除供应商', NOW(), NOW()),
-- Warehouse Management
(21, '仓库查看', 'warehouse:list', 'base', '查看仓库', NOW(), NOW()),
(22, '仓库新增', 'warehouse:create', 'base', '新增仓库', NOW(), NOW()),
(23, '仓库编辑', 'warehouse:update', 'base', '编辑仓库', NOW(), NOW()),
(24, '仓库删除', 'warehouse:delete', 'base', '删除仓库', NOW(), NOW()),
-- Inbound Management
(25, '入库单查看', 'inbound:list', 'inbound', '查看入库单', NOW(), NOW()),
(26, '入库单新增', 'inbound:create', 'inbound', '新增入库单', NOW(), NOW()),
(27, '入库单编辑', 'inbound:update', 'inbound', '编辑入库单', NOW(), NOW()),
(28, '入库审核', 'inbound:review', 'inbound', '审核入库单', NOW(), NOW()),
(29, '入库收货', 'inbound:receive', 'inbound', '执行入库收货', NOW(), NOW()),
-- Outbound Management
(30, '出库单查看', 'outbound:list', 'outbound', '查看出库单', NOW(), NOW()),
(31, '出库单新增', 'outbound:create', 'outbound', '新增出库单', NOW(), NOW()),
(32, '出库单编辑', 'outbound:update', 'outbound', '编辑出库单', NOW(), NOW()),
(33, '出库审核', 'outbound:review', 'outbound', '审核出库单', NOW(), NOW()),
(34, '出库拣货', 'outbound:pick', 'outbound', '执行出库拣货', NOW(), NOW()),
-- Inventory & Alerts
(35, '库存查看', 'inventory:list', 'inventory', '查看库存', NOW(), NOW()),
(36, '批次查看', 'batch:list', 'inventory', '查看批次', NOW(), NOW()),
(37, '预警查看', 'alert:list', 'alert', '查看预警', NOW(), NOW()),
(38, '预警处理', 'alert:handle', 'alert', '处理预警', NOW(), NOW()),
-- Statistics
(39, '统计查看', 'stats:view', 'stats', '查看统计', NOW(), NOW()),
-- Procurement
(40, '采购查看', 'procurement:view', 'procurement', '查看采购建议', NOW(), NOW());

-- Admin gets all permissions (role_id=1)
INSERT INTO sys_role_permission (role_id, permission_id, create_time)
SELECT 1, id, NOW() FROM sys_permission;

-- Warehouse Manager permissions (role_id=2)
INSERT INTO sys_role_permission (role_id, permission_id, create_time) VALUES
(2, 9, NOW()), (2, 13, NOW()), (2, 17, NOW()), (2, 21, NOW()),
(2, 25, NOW()), (2, 26, NOW()), (2, 27, NOW()), (2, 28, NOW()), (2, 29, NOW()),
(2, 30, NOW()), (2, 31, NOW()), (2, 32, NOW()), (2, 33, NOW()), (2, 34, NOW()),
(2, 35, NOW()), (2, 36, NOW()), (2, 37, NOW()), (2, 38, NOW()), (2, 39, NOW());

-- Warehouse Worker permissions (role_id=3)
INSERT INTO sys_role_permission (role_id, permission_id, create_time) VALUES
(3, 13, NOW()), (3, 25, NOW()), (3, 29, NOW()), (3, 30, NOW()), (3, 34, NOW()), (3, 35, NOW());

-- Purchaser permissions (role_id=4)
INSERT INTO sys_role_permission (role_id, permission_id, create_time) VALUES
(4, 13, NOW()), (4, 17, NOW()), (4, 18, NOW()), (4, 19, NOW()), (4, 20, NOW()),
(4, 25, NOW()), (4, 26, NOW()), (4, 35, NOW()), (4, 39, NOW()), (4, 40, NOW());

-- Default Admin User (password: admin123 -> BCrypt)
INSERT INTO sys_user (id, username, password, real_name, phone, status, create_time, update_time) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000000', 1, NOW(), NOW());

INSERT INTO sys_user_role (user_id, role_id, create_time) VALUES (1, 1, NOW());

-- Seed categories
INSERT INTO category (id, category_name, sort_order, status, create_time, update_time) VALUES
(1, '热带水果', 1, 1, NOW(), NOW()),
(2, '柑橘类', 2, 1, NOW(), NOW()),
(3, '浆果类', 3, 1, NOW(), NOW()),
(4, '核果类', 4, 1, NOW(), NOW()),
(5, '瓜果类', 5, 1, NOW(), NOW());
