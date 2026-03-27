USE fruit_warehouse_manage;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    last_login_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    KEY idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    menu_name VARCHAR(50) NOT NULL,
    menu_type VARCHAR(20) NOT NULL,
    route_path VARCHAR(100) DEFAULT NULL,
    component_path VARCHAR(150) DEFAULT NULL,
    permission_code VARCHAR(100) DEFAULT NULL,
    icon VARCHAR(50) DEFAULT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    visible TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_menu_parent (parent_id),
    KEY idx_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    KEY idx_ur_role (role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, menu_id),
    KEY idx_rm_menu (menu_id),
    CONSTRAINT fk_rm_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
    CONSTRAINT fk_rm_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    credit_level VARCHAR(20) DEFAULT 'B',
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_supplier_code (supplier_code),
    KEY idx_supplier_name (supplier_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_code VARCHAR(50) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    customer_level VARCHAR(20) DEFAULT 'NORMAL',
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customer_code (customer_code),
    KEY idx_customer_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fruit_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_code VARCHAR(50) NOT NULL,
    warehouse_name VARCHAR(100) NOT NULL,
    location VARCHAR(255) DEFAULT NULL,
    manager_name VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_warehouse_code (warehouse_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fruit_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fruit_code VARCHAR(50) NOT NULL,
    fruit_name VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL,
    unit VARCHAR(20) NOT NULL DEFAULT 'kg',
    origin_place VARCHAR(100) DEFAULT NULL,
    shelf_life_days INT NOT NULL DEFAULT 7,
    warning_days INT NOT NULL DEFAULT 2,
    safe_stock_qty DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    suggested_purchase_price DECIMAL(10,2) DEFAULT 0.00,
    suggested_sale_price DECIMAL(10,2) DEFAULT 0.00,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fruit_code (fruit_code),
    KEY idx_fruit_name (fruit_name),
    KEY idx_fruit_category (category_id),
    CONSTRAINT fk_fruit_category FOREIGN KEY (category_id) REFERENCES fruit_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_no VARCHAR(50) NOT NULL,
    supplier_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    order_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    order_date DATE NOT NULL,
    expected_arrival_date DATE DEFAULT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_by BIGINT DEFAULT NULL,
    received_by BIGINT DEFAULT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_purchase_no (purchase_no),
    KEY idx_po_status_date (order_status, order_date),
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id),
    CONSTRAINT fk_po_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_order_id BIGINT NOT NULL,
    fruit_id BIGINT NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    production_date DATE DEFAULT NULL,
    expiration_date DATE NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    received_qty DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_po_batch (purchase_order_id, fruit_id, batch_no),
    KEY idx_poi_fruit (fruit_id),
    CONSTRAINT fk_poi_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(id),
    CONSTRAINT fk_poi_fruit FOREIGN KEY (fruit_id) REFERENCES fruit_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory_batch (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fruit_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    source_id BIGINT DEFAULT NULL,
    production_date DATE DEFAULT NULL,
    expiration_date DATE NOT NULL,
    total_qty DECIMAL(10,2) NOT NULL,
    available_qty DECIMAL(10,2) NOT NULL,
    locked_qty DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    unit_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_STOCK',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_inv_batch (fruit_id, warehouse_id, batch_no),
    KEY idx_inv_expire (expiration_date),
    CONSTRAINT fk_inv_fruit FOREIGN KEY (fruit_id) REFERENCES fruit_info(id),
    CONSTRAINT fk_inv_wh FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sales_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sales_no VARCHAR(50) NOT NULL,
    customer_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    order_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    order_time DATETIME NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_by BIGINT DEFAULT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sales_no (sales_no),
    KEY idx_so_status_date (order_status, order_time),
    CONSTRAINT fk_so_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_so_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sales_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sales_order_id BIGINT NOT NULL,
    fruit_id BIGINT NOT NULL,
    batch_id BIGINT DEFAULT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_soi_order (sales_order_id),
    KEY idx_soi_fruit (fruit_id),
    KEY idx_soi_batch (batch_id),
    CONSTRAINT fk_soi_order FOREIGN KEY (sales_order_id) REFERENCES sales_order(id),
    CONSTRAINT fk_soi_fruit FOREIGN KEY (fruit_id) REFERENCES fruit_info(id),
    CONSTRAINT fk_soi_batch FOREIGN KEY (batch_id) REFERENCES inventory_batch(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory_txn (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_type VARCHAR(30) NOT NULL,
    biz_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    fruit_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    change_qty DECIMAL(10,2) NOT NULL,
    balance_qty DECIMAL(10,2) NOT NULL,
    operator_id BIGINT DEFAULT NULL,
    txn_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(255) DEFAULT NULL,
    KEY idx_txn_biz (biz_type, biz_id),
    KEY idx_txn_fruit_time (fruit_id, txn_time),
    CONSTRAINT fk_txn_batch FOREIGN KEY (batch_id) REFERENCES inventory_batch(id),
    CONSTRAINT fk_txn_fruit FOREIGN KEY (fruit_id) REFERENCES fruit_info(id),
    CONSTRAINT fk_txn_wh FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS stock_check_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    check_no VARCHAR(50) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    check_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    check_date DATE NOT NULL,
    created_by BIGINT DEFAULT NULL,
    approved_by BIGINT DEFAULT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_check_no (check_no),
    CONSTRAINT fk_sco_wh FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS stock_check_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    check_order_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    fruit_id BIGINT NOT NULL,
    system_qty DECIMAL(10,2) NOT NULL,
    actual_qty DECIMAL(10,2) NOT NULL,
    diff_qty DECIMAL(10,2) NOT NULL,
    reason VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_check_batch (check_order_id, batch_id),
    CONSTRAINT fk_sci_order FOREIGN KEY (check_order_id) REFERENCES stock_check_order(id),
    CONSTRAINT fk_sci_batch FOREIGN KEY (batch_id) REFERENCES inventory_batch(id),
    CONSTRAINT fk_sci_fruit FOREIGN KEY (fruit_id) REFERENCES fruit_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alert_type VARCHAR(20) NOT NULL,
    fruit_id BIGINT NOT NULL,
    batch_id BIGINT DEFAULT NULL,
    warehouse_id BIGINT NOT NULL,
    alert_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    alert_msg VARCHAR(255) NOT NULL,
    threshold_value DECIMAL(10,2) DEFAULT NULL,
    current_value DECIMAL(10,2) DEFAULT NULL,
    alert_status VARCHAR(20) NOT NULL DEFAULT 'UNHANDLED',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handled_time DATETIME DEFAULT NULL,
    handled_by BIGINT DEFAULT NULL,
    KEY idx_alert_status (alert_status),
    KEY idx_alert_type_time (alert_type, created_time),
    CONSTRAINT fk_alert_fruit FOREIGN KEY (fruit_id) REFERENCES fruit_info(id),
    CONSTRAINT fk_alert_batch FOREIGN KEY (batch_id) REFERENCES inventory_batch(id),
    CONSTRAINT fk_alert_wh FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_forecast_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fruit_id BIGINT NOT NULL,
    forecast_date DATE NOT NULL,
    target_date DATE NOT NULL,
    model_name VARCHAR(50) NOT NULL,
    version_no VARCHAR(50) NOT NULL DEFAULT 'v1',
    predict_qty DECIMAL(10,2) NOT NULL,
    confidence_lower DECIMAL(10,2) DEFAULT NULL,
    confidence_upper DECIMAL(10,2) DEFAULT NULL,
    data_window_days INT NOT NULL DEFAULT 30,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_forecast_key (fruit_id, target_date, model_name, version_no),
    CONSTRAINT fk_forecast_fruit FOREIGN KEY (fruit_id) REFERENCES fruit_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_purchase_suggestion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fruit_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    suggestion_date DATE NOT NULL,
    predicted_daily_qty DECIMAL(10,2) NOT NULL,
    lead_time_days INT NOT NULL DEFAULT 1,
    safety_stock_qty DECIMAL(10,2) NOT NULL,
    current_stock_qty DECIMAL(10,2) NOT NULL,
    in_transit_qty DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    recommended_purchase_qty DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    reason VARCHAR(255) DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_suggestion_key (fruit_id, warehouse_id, suggestion_date),
    CONSTRAINT fk_suggestion_fruit FOREIGN KEY (fruit_id) REFERENCES fruit_info(id),
    CONSTRAINT fk_suggestion_wh FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_chat_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    provider_name VARCHAR(50) DEFAULT NULL,
    model_name VARCHAR(100) DEFAULT NULL,
    intent_code VARCHAR(50) DEFAULT NULL,
    tool_name VARCHAR(50) DEFAULT NULL,
    content LONGTEXT NOT NULL,
    token_count INT NOT NULL DEFAULT 0,
    stream_flag TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chat_session (session_id),
    KEY idx_chat_user_time (user_id, create_time),
    CONSTRAINT fk_chat_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
