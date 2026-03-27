# Fruit Warehouse Manage

基于 `Spring Boot 3 + MyBatis-Plus + MySQL 8 + Vue 3 + Element Plus + ECharts + Python` 的水果批发仓库管理系统。

## 目录
- `backend`：Spring Boot 后端
- `frontend`：Vue 3 前端
- `ai-algorithm`：销量预测与采购建议脚本
- `sql`：数据库初始化与演示数据

## 启动顺序
1. 执行 SQL 脚本：`00_init_db.sql -> 01_schema.sql -> 02_seed_data.sql -> 03_demo_report_data.sql -> 04_menu_init.sql`
2. 修改 `backend/src/main/resources/application-dev.yml` 中的 MySQL 连接。
3. 在 `backend` 目录运行：`mvn spring-boot:run`
4. 在 `frontend` 目录运行：`npm install --cache .npm-cache && npm run dev`
5. 如需算法依赖：在 `ai-algorithm` 目录创建虚拟环境并执行 `pip install -r requirements.txt`

## 默认账号
- `admin / 123456`
- `warehouse / 123456`
- `sales / 123456`

## AI 说明
- 后端 `AiService.java` 同时支持规则模式和 OpenAI 兼容接口模式。
- 如果 `application.yml` 中仍是示例 Key，则系统走本地规则回复，便于无 Key 演示。
- Python 预测统一入口为 `ai-algorithm/scripts/predict_cli.py`。
