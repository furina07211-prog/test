# Fruit Warehouse Manage

基于 `Spring Boot 3 + MyBatis-Plus + MySQL 8 + Vue 3 + Element Plus + ECharts + Python` 的水果批发仓库管理系统。

## 项目目录
- `backend`：Spring Boot 后端
- `frontend`：Vue 3 前端
- `ai-algorithm`：AI预测与库存优化脚本
- `sql`：数据库初始化与演示种子数据

## 默认运行配置（已固化）
- MySQL：`localhost:3306`
- 数据库：`fruit_warehouse_manage`
- 用户名/密码：`root / 123456`
- 后端端口：`8080`
- 前端端口：`5173`
- 前端代理：`/api -> http://localhost:8080`

## 快速启动
1. 初始化数据库脚本（按顺序）：
   - `sql/_sanitized_schema.sql`
   - `sql/_seed_ascii.sql`
2. 启动后端：
   - `cd backend`
   - `mvn spring-boot:run`
3. 启动前端：
   - `cd frontend`
   - `npm install --cache .npm-cache`
   - `npm run dev`
4. 访问：`http://localhost:5173`

## 默认账号
- `admin / 123456`
- `warehouse / 123456`
- `sales / 123456`

## 说明
- AI Key 为空时不影响系统基础功能启动。
- 详细一键步骤见《一键启动说明.md》。
