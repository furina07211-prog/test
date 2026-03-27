<template>
  <div class="dashboard-page" v-loading="refreshing" element-loading-text="加载中">
    <div class="dashboard-header card">
      <div>
        <h2>经营数据看板</h2>
        <p>实时展示采购、销售、库存与预警核心指标</p>
      </div>
      <el-button type="primary" :loading="refreshing" @click="handleRefresh">刷新数据</el-button>
    </div>

    <div v-if="loading" class="loading-wrap card">
      <el-skeleton animated :rows="10" />
    </div>

    <template v-else>
      <section class="kpi-grid">
        <div class="kpi-card card">
          <div class="kpi-label">库存品种总数</div>
          <div class="kpi-value">{{ overview.totalStockSku }}</div>
        </div>
        <div class="kpi-card card">
          <div class="kpi-label">今日采购金额</div>
          <div class="kpi-value">{{ formatCurrency(overview.todayPurchaseAmount) }}</div>
        </div>
        <div class="kpi-card card">
          <div class="kpi-label">今日销售金额</div>
          <div class="kpi-value">{{ formatCurrency(overview.todaySalesAmount) }}</div>
        </div>
        <div class="kpi-card card warning">
          <div class="kpi-label">库存预警商品数</div>
          <div class="kpi-value">{{ overview.warningFruitCount }}</div>
        </div>
      </section>

      <section class="chart-grid">
        <div class="card chart-card trend-card">
          <div class="chart-title">近7天采购/销售金额趋势</div>
          <div v-if="hasTrendData" ref="trendRef" class="chart"></div>
          <el-empty v-else description="暂无数据" />
        </div>

        <div class="card chart-card ratio-card">
          <div class="chart-title">水果品类库存占比</div>
          <div v-if="hasRatioData" ref="ratioRef" class="chart"></div>
          <el-empty v-else description="暂无数据" />
        </div>

        <div class="card chart-card top-card">
          <div class="chart-title">近30天销量前5水果</div>
          <div v-if="hasTopData" ref="topRef" class="chart"></div>
          <el-empty v-else description="暂无数据" />
        </div>

        <div class="card warning-card">
          <div class="chart-title">库存预警商品列表</div>
          <el-table :data="warningRows" class="app-table" stripe empty-text="暂无预警数据">
            <el-table-column prop="fruitName" label="水果" min-width="110" />
            <el-table-column prop="warehouseName" label="仓库" min-width="110" />
            <el-table-column prop="batchNo" label="批次号" min-width="130" />
            <el-table-column label="预警类型" width="120">
              <template #default="scope">{{ formatAlertType(scope.row.alertType) }}</template>
            </el-table-column>
            <el-table-column prop="alertLevel" label="预警等级" width="110" />
            <el-table-column prop="currentValue" label="当前值" width="100" />
            <el-table-column prop="thresholdValue" label="阈值" width="100" />
            <el-table-column label="创建时间" min-width="160">
              <template #default="scope">{{ formatDateTime(scope.row.createdTime) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { dashboardApi } from '@/api/modules'

const loading = ref(true)
const refreshing = ref(false)

const overview = reactive({
  totalStockSku: 0,
  todayPurchaseAmount: 0,
  todaySalesAmount: 0,
  warningFruitCount: 0
})

const trendRows = ref([])
const ratioRows = ref([])
const topRows = ref([])
const warningRows = ref([])

const trendRef = ref(null)
const ratioRef = ref(null)
const topRef = ref(null)

let trendChart = null
let ratioChart = null
let topChart = null
let refreshTimer = null

const ALERT_TYPE_LABEL_MAP = {
  LOW_STOCK: '低库存',
  NEAR_EXPIRATION: '临期',
  EXPIRING: '临期'
}

const hasTrendData = computed(() => trendRows.value.some((row) => Number(row.purchaseAmount || 0) > 0 || Number(row.salesAmount || 0) > 0))
const hasRatioData = computed(() => ratioRows.value.length > 0)
const hasTopData = computed(() => topRows.value.length > 0)

const formatCurrency = (value) => `${Number(value || 0).toFixed(2)} 元`
const formatAlertType = (type) => ALERT_TYPE_LABEL_MAP[type] || type || '-'
const formatDateTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

const loadDashboard = async (initial = false) => {
  if (!initial && refreshing.value) return

  if (initial) {
    loading.value = true
  } else {
    refreshing.value = true
  }

  try {
    const [overviewRes, trendRes, ratioRes, topRes, warningRes] = await Promise.all([
      dashboardApi.overview(),
      dashboardApi.amountTrend({ days: 7 }),
      dashboardApi.categoryRatio(),
      dashboardApi.salesTop({ days: 30, limit: 5 }),
      dashboardApi.warnings({ limit: 20 })
    ])

    overview.totalStockSku = Number(overviewRes?.totalStockSku || 0)
    overview.todayPurchaseAmount = Number(overviewRes?.todayPurchaseAmount || 0)
    overview.todaySalesAmount = Number(overviewRes?.todaySalesAmount || 0)
    overview.warningFruitCount = Number(overviewRes?.warningFruitCount || 0)

    trendRows.value = Array.isArray(trendRes) ? trendRes : []
    ratioRows.value = Array.isArray(ratioRes) ? ratioRes : []
    topRows.value = Array.isArray(topRes) ? topRes : []
    warningRows.value = Array.isArray(warningRes) ? warningRes : []

    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const renderCharts = () => {
  renderTrendChart()
  renderRatioChart()
  renderTopChart()
}

const renderTrendChart = () => {
  if (!trendRef.value || !hasTrendData.value) {
    trendChart?.clear()
    return
  }
  trendChart ||= echarts.init(trendRef.value)

  const xAxis = trendRows.value.map((item) => item.date)
  const purchaseSeries = trendRows.value.map((item) => Number(item.purchaseAmount || 0))
  const salesSeries = trendRows.value.map((item) => Number(item.salesAmount || 0))

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['采购金额', '销售金额'] },
    grid: { left: 50, right: 20, top: 36, bottom: 30 },
    xAxis: { type: 'category', data: xAxis },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [
      { name: '采购金额', type: 'line', smooth: true, data: purchaseSeries },
      { name: '销售金额', type: 'line', smooth: true, data: salesSeries }
    ]
  })
}

const renderRatioChart = () => {
  if (!ratioRef.value || !hasRatioData.value) {
    ratioChart?.clear()
    return
  }
  ratioChart ||= echarts.init(ratioRef.value)

  const pieData = ratioRows.value.map((item) => ({
    name: item.categoryName || '未分类',
    value: Number(item.stockQty || 0)
  }))

  ratioChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 0, top: 'middle' },
    series: [
      {
        name: '库存占比',
        type: 'pie',
        radius: ['35%', '62%'],
        center: ['62%', '52%'],
        data: pieData,
        label: { formatter: '{b}\n{d}%' }
      }
    ]
  })
}

const renderTopChart = () => {
  if (!topRef.value || !hasTopData.value) {
    topChart?.clear()
    return
  }
  topChart ||= echarts.init(topRef.value)

  const xAxis = topRows.value.map((item) => item.fruitName || `水果-${item.fruitId}`)
  const series = topRows.value.map((item) => Number(item.salesQty || 0))

  topChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 50 },
    xAxis: { type: 'category', data: xAxis, axisLabel: { rotate: 20 } },
    yAxis: { type: 'value', name: '销量' },
    series: [{ type: 'bar', data: series, itemStyle: { color: '#409eff' } }]
  })
}

const resizeCharts = () => {
  trendChart?.resize()
  ratioChart?.resize()
  topChart?.resize()
}

const handleRefresh = async () => {
  await loadDashboard(false)
}

onMounted(async () => {
  await loadDashboard(true)
  window.addEventListener('resize', resizeCharts)
  refreshTimer = window.setInterval(() => {
    loadDashboard(false).catch(() => {})
  }, 60000)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
    refreshTimer = null
  }
  trendChart?.dispose()
  ratioChart?.dispose()
  topChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 14px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard-header h2 {
  margin: 0;
  font-size: 20px;
}

.dashboard-header p {
  margin: 4px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.loading-wrap {
  min-height: 420px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.kpi-card {
  min-height: 106px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.kpi-card.warning {
  border-color: #f6d8a8;
  background: #fffaf1;
}

.kpi-label {
  color: var(--text-secondary);
  font-size: 13px;
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-main);
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.chart-card {
  min-height: 360px;
}

.trend-card {
  grid-column: span 2;
}

.warning-card {
  grid-column: span 3;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 10px;
}

.chart {
  width: 100%;
  height: 300px;
}

@media (max-width: 1280px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .trend-card,
  .warning-card {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .kpi-grid,
  .chart-grid {
    grid-template-columns: 1fr;
  }

  .trend-card,
  .warning-card {
    grid-column: span 1;
  }

  .chart {
    height: 280px;
  }
}
</style>
