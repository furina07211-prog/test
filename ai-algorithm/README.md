# AI Algorithm

本目录提供水果批发仓库管理系统的算法落地脚本。

## 脚本说明
- `forecast_simple.py`：简化版销量预测，基于加权移动平均。
- `forecast_prophet.py`：完整版 Prophet 预测，若 Prophet 不可用会自动回退到简化版。
- `generate_purchase_suggestion.py`：基于预测销量、安全库存、当前库存生成采购建议。
- `predict_cli.py`：Java `ProcessBuilder` 统一入口，从标准输入读取 JSON。

## 输入约定
### forecast
```json
{
  "fruitId": 1,
  "fruitName": "苹果",
  "days": 7,
  "model": "simple",
  "history": [
    {"date": "2026-03-20", "qty": 32},
    {"date": "2026-03-21", "qty": 35}
  ]
}
```

### suggest
```json
{
  "predictedDailyQty": 28,
  "leadTimeDays": 2,
  "safeStockQty": 80,
  "currentStockQty": 20,
  "inTransitQty": 0
}
```
