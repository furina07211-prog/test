<template>
  <div class="dashboard-page">
    <div class="card toolbar">
      <el-select v-model="query.fruitId" placeholder="Select fruit" filterable style="width: 220px">
        <el-option v-for="item in fruits" :key="item.id" :label="item.fruitName" :value="item.id" />
      </el-select>
      <el-input-number v-model="query.historyDays" :min="7" :max="90" :step="7" />
      <el-select v-model="query.model" style="width: 160px">
        <el-option label="Prophet" value="prophet" />
        <el-option label="Simple" value="simple" />
      </el-select>
      <el-button type="primary" :loading="runningForecast" @click="runForecast">Refresh Forecast</el-button>
      <el-button type="success" :loading="runningOptimize" @click="runOptimize">Refresh Optimize</el-button>
    </div>

    <div class="grid">
      <div class="card chart-card wide">
        <div class="chart-title">Sales Trend vs Next 7 Days Forecast</div>
        <div ref="trendRef" class="chart"></div>
      </div>

      <div class="card chart-card">
        <div class="chart-title">Inventory Alert Heatmap</div>
        <div ref="heatRef" class="chart"></div>
      </div>

      <div class="card chart-card">
        <div class="chart-title">Purchase Suggestion Top 10</div>
        <div ref="barRef" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { basicApi, dashboardApi } from '@/api/modules'

const query = reactive({
  fruitId: null,
  historyDays: 30,
  model: 'prophet'
})

const fruits = ref([])
const runningForecast = ref(false)
const runningOptimize = ref(false)

const trendRef = ref(null)
const heatRef = ref(null)
const barRef = ref(null)
let trendChart = null
let heatChart = null
let barChart = null

function fruitNameById(id) {
  const row = fruits.value.find((f) => f.id === id)
  return row?.fruitName || `Fruit-${id}`
}

async function initData() {
  const fruitRes = await basicApi.fruits({ current: 1, size: 300 })
  fruits.value = fruitRes?.records || []
  if (!query.fruitId && fruits.value.length > 0) {
    query.fruitId = fruits.value[0].id
  }
  await loadAllCharts()
}

async function runForecast() {
  if (!query.fruitId) {
    ElMessage.warning('Please select one fruit first')
    return
  }
  runningForecast.value = true
  try {
    await dashboardApi.runForecast({ fruitId: query.fruitId, days: 7, model: query.model })
    ElMessage.success('Forecast refreshed')
    await loadTrendChart()
  } catch (error) {
    ElMessage.error(error?.message || 'Failed to refresh forecast')
  } finally {
    runningForecast.value = false
  }
}

async function runOptimize() {
  runningOptimize.value = true
  try {
    await dashboardApi.runOptimize({ fruitId: query.fruitId, warehouseId: 1, leadTimeDays: 1, safetyDays: 3 })
    ElMessage.success('Inventory optimization refreshed')
    await Promise.all([loadBarChart(), loadHeatChart()])
  } catch (error) {
    ElMessage.error(error?.message || 'Failed to refresh optimization')
  } finally {
    runningOptimize.value = false
  }
}

async function loadAllCharts() {
  await Promise.all([loadTrendChart(), loadHeatChart(), loadBarChart()])
}

async function loadTrendChart() {
  if (!query.fruitId) {
    return
  }
  const res = await dashboardApi.forecastTrend({ fruitId: query.fruitId, historyDays: query.historyDays })
  await nextTick()

  trendChart ||= echarts.init(trendRef.value)

  const history = res?.history || []
  const forecast = res?.forecast || []
  const historyData = history.map((row) => ({ date: row.date, qty: Number(row.qty || 0) }))
  const forecastData = forecast.map((row) => ({ date: row.targetDate, qty: Number(row.predictQty || 0) }))

  const xAxis = [...historyData.map((x) => x.date), ...forecastData.map((x) => x.date)]
  const historySeries = [...historyData.map((x) => x.qty), ...new Array(forecastData.length).fill(null)]
  const forecastSeries = [...new Array(historyData.length).fill(null), ...forecastData.map((x) => x.qty)]

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['History', 'Forecast'] },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: xAxis },
    yAxis: { type: 'value', name: 'Qty' },
    series: [
      { name: 'History', type: 'line', smooth: true, data: historySeries },
      { name: 'Forecast', type: 'line', smooth: true, lineStyle: { type: 'dashed' }, data: forecastSeries }
    ]
  })
}

async function loadHeatChart() {
  const rows = await dashboardApi.alertHeatmap()
  await nextTick()
  heatChart ||= echarts.init(heatRef.value)

  const types = [...new Set(rows.map((x) => x.alertType).filter(Boolean))]
  const xTypes = types.length > 0 ? types : ['LOW_STOCK', 'NEAR_EXPIRATION']
  const fruitsAxis = [...new Set(rows.map((x) => x.fruitName || 'Unknown'))]
  const matrix = []

  for (let y = 0; y < fruitsAxis.length; y += 1) {
    for (let x = 0; x < xTypes.length; x += 1) {
      const matched = rows.find((row) => (row.fruitName || 'Unknown') === fruitsAxis[y] && row.alertType === xTypes[x])
      matrix.push([x, y, Number(matched?.alertCount || 0)])
    }
  }

  const maxValue = matrix.length > 0 ? Math.max(...matrix.map((item) => item[2])) : 1
  heatChart.setOption({
    tooltip: { position: 'top' },
    grid: { left: 110, right: 20, top: 20, bottom: 50 },
    xAxis: { type: 'category', data: xTypes },
    yAxis: { type: 'category', data: fruitsAxis },
    visualMap: {
      min: 0,
      max: maxValue || 1,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 0
    },
    series: [{ type: 'heatmap', data: matrix, label: { show: true } }]
  })
}

async function loadBarChart() {
  const rows = await dashboardApi.optimizeList({ warehouseId: 1, limit: 10 })
  await nextTick()
  barChart ||= echarts.init(barRef.value)

  const x = rows.map((row) => fruitNameById(row.fruitId))
  const y = rows.map((row) => Number(row.recommendedPurchaseQty || 0))

  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 60 },
    xAxis: { type: 'category', data: x, axisLabel: { rotate: 25 } },
    yAxis: { type: 'value', name: 'Recommended Qty' },
    series: [{ type: 'bar', data: y, itemStyle: { color: '#5c7cfa' } }]
  })
}

function resizeAll() {
  trendChart?.resize()
  heatChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  await initData()
  window.addEventListener('resize', resizeAll)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeAll)
  trendChart?.dispose()
  heatChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 14px;
}

.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.chart-card.wide {
  grid-column: span 2;
}

.chart-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.chart {
  width: 100%;
  height: 320px;
}

@media (max-width: 1100px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .chart-card.wide {
    grid-column: span 1;
  }
}
</style>