USE fruit_warehouse_manage;

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, route_path, component_path, permission_code, icon, sort_no, visible) VALUES
(1, 0, '经营总览', 'MENU', '/dashboard', 'views/dashboard/DashboardView.vue', 'dashboard:view', 'DataAnalysis', 1, 1),
(2, 0, '系统管理', 'MENU', '/system', 'views/system/SystemManageView.vue', 'system:view', 'Setting', 2, 1),
(3, 0, '基础资料', 'MENU', '/basic', 'views/basic/BasicDataView.vue', 'basic:view', 'Files', 3, 1),
(4, 0, '采购入库', 'MENU', '/purchase', 'views/purchase/PurchaseView.vue', 'purchase:view', 'ShoppingCart', 4, 1),
(5, 0, '销售出库', 'MENU', '/sales', 'views/sales/SalesView.vue', 'sales:view', 'Sell', 5, 1),
(6, 0, '库存盘点', 'MENU', '/inventory', 'views/inventory/InventoryView.vue', 'inventory:view', 'Box', 6, 1),
(7, 0, 'AI 工作台', 'MENU', '/ai', 'views/ai/AiWorkbenchView.vue', 'ai:view', 'MagicStick', 7, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), route_path = VALUES(route_path), component_path = VALUES(component_path);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1),(1, 2),(1, 3),(1, 4),(1, 5),(1, 6),(1, 7),
(2, 1),(2, 3),(2, 4),(2, 6),(2, 7),
(3, 1),(3, 3),(3, 5),(3, 7)
ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);
