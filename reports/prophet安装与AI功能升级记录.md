# Prophet安装与AI功能升级记录

## 1. 背景与目标
- 项目：`fruit_warehouse_manage`
- 目标：解决 Prophet 因镜像/代理/环境权限导致的安装失败，并完成“脚本 -> 后端接口 -> 前端看板”全链路验证。
- 执行日期：2026-03-27

## 2. 环境诊断结论（Step 1）
- Python: `3.8.0`
- pip: `25.0.1`
- OS: Windows
- pip全局配置存在源污染：固定阿里源 + trusted-host（已在安装命令统一使用 `--isolated` 绕过）
- 依赖状态（诊断时）：
  - `prophet/cmdstanpy/pystan` 未安装
  - `pandas/numpy/pymysql` 已安装
- 诊断日志：`reports/prophet_diagnosis.log`

## 3. 多方案安装执行结果（Step 2）

### 方案一：pip国内镜像安装（失败）
- 执行：清华源下载 Prophet wheel 成功。
- 失败点：安装阶段写入用户site-packages失败，报错 `WinError 183`，并伴随 PowerShell profile 执行策略拦截。
- 结论：不是 Prophet 包不可用，属于当前系统用户目录写入/策略问题。

### 方案二：Conda独立环境（失败）
- 按计划执行 `conda --no-plugins` 路径。
- 失败点：当前机器对 `C:\Users\Furina\AppData\Local\conda` 无写权限，出现 `NotWritableError` 与 cache/notices 目录创建失败。
- 结论：Conda在该权限约束下不可作为可行主路径。

### 方案三：离线安装（成功，已落地）
- 成功策略：本地构建 wheel 仓库 + 项目内独立虚拟环境离线安装。
- 关键命令：
  - `python -m pip --isolated download --dest .wheels --index-url https://pypi.tuna.tsinghua.edu.cn/simple --trusted-host pypi.tuna.tsinghua.edu.cn prophet==1.1.5`
  - `python -m pip --isolated download --dest .wheels --index-url https://pypi.tuna.tsinghua.edu.cn/simple --trusted-host pypi.tuna.tsinghua.edu.cn pymysql==1.1.1`
  - `python -m venv .venv-ai`
  - `.\.venv-ai\Scripts\python.exe -m pip install --no-index --find-links=.\.wheels prophet==1.1.5 pandas numpy pymysql`
- 安装验收：
  - `.\.venv-ai\Scripts\python.exe -c "from prophet import Prophet; import cmdstanpy, pandas, numpy, pymysql; print('ok')"`
  - 输出 `ok`

### 方案四：替代算法（未触发）
- 因方案三已成功，未进入替代实现。

## 4. 配置与代码变更（Step 2/4）

### 配置变更
- 文件：`backend/src/main/resources/application.yml`
- 变更项：
  - `fruit.algorithm.python-command` 从 `python` 改为 `../.venv-ai/Scripts/python.exe`
- 目的：让后端固定调用项目内已安装 Prophet 的解释器，避免系统Python权限问题。

### 算法稳定性升级
- 文件：`ai-algorithm/scripts/forecast_prophet.py`
- 变更项：对 `yhat/yhat_lower/yhat_upper` 增加非负裁剪，并保证 `confidenceUpper >= confidenceLower`。
- 目的：避免 Prophet 在小样本下出现负销量，保证业务可解释性与图表稳定。
- 接口协议保持不变：`targetDate/predictQty/confidenceLower/confidenceUpper`。

## 5. 联调验证结果（Step 3）

### 脚本层验证
- 命令：
  - `$payload | .\.venv-ai\Scripts\python.exe .\ai-algorithm\scripts\predict_cli.py forecast`
- 结果：返回非空 `predictions`，日期连续，`fallback=false`。

### 后端接口验证
- 登录：`POST /api/auth/login` 成功，获取 token。
- 预测执行：`POST /api/dashboard/forecast/run`（`fruitId=1, model=prophet, days=7`）返回 `code=200`，`savedForecastRows=7`。
- 趋势查询：`GET /api/dashboard/forecast/trend?fruitId=1&historyDays=30` 返回历史+预测序列。

### 数据库落库验证
- 查询显示 `model_name='prophet'` 当日预测已写入（7条）。
- 稳定性升级后再次验证：预测值无负数。

### 前端联调验证（通过Vite代理）
- `http://127.0.0.1:5173/api/...` 代理到后端正常。
- 关键检查：`prophetPoints=7; hasNaN=False; hasNegative=False`
- 结论：ECharts趋势图数据满足渲染条件（无空图/NaN/负销量异常点）。

## 6. 失败根因归档
- pip方案失败根因：用户目录 `site-packages` 写入失败（`WinError 183`）+ profile 执行策略噪音。
- conda方案失败根因：`AppData\Local\conda` 权限拒绝，无法创建 env/cache/notices。
- 网络镜像本身可达：wheel 下载正常，安装阻塞主要来自权限而非网络连通。

## 7. 验收结论
- `prophet` 已可用并参与真实预测执行。
- 后端 `/forecast/run`、`/forecast/trend` 验证通过。
- 前端代理链路与图表数据校验通过。
- 本次升级未改动 Java/Python 协议，仅调整解释器路径与 Prophet 输出稳定性。

## 8. 交付与后续建议
- 已交付文件：
  - `reports/prophet_diagnosis.log`
  - `reports/prophet安装与AI功能升级记录.md`
- 后续建议：
  - 若需完全回归“独立Conda策略”，需先解决当前Windows账号对 `AppData\Local\conda` 的写权限问题。
  - 可在后续版本增加 Prophet 结果平滑或分位数约束，减少小样本波动。