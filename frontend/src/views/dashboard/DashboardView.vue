<template>
  <div class="page-shell">
    <header class="page-header">
      <div class="page-title">
        <h2>经营总览</h2>
        <p>把采购、销售、库存预警和预测趋势放进一个连续操作面板，适合作为答辩时的主展示页。</p>
      </div>
      <el-button type="warning" @click="refreshAll">刷新看板</el-button>
    </header>

    <section class="data-grid">
      <StatMetric class="metric-cell" label="水果档案数" :value="overview.fruitCount || 0" hint="基础资料" />
      <StatMetric class="metric-cell" label="库存批次数" :value="overview.batchCount || 0" hint="批次管理" />
      <StatMetric class="metric-cell" label="未处理预警" :value="overview.pendingAlerts || 0" hint="低库存 / 临期" />
      <StatMetric class="metric-cell" label="今日销售额" :value="overview.todaySales || 0" hint="实时经营" />
    </section>

    <section class="data-grid">
      <div class="surface section-block chart-card wide">
        <div class="inner-header">
          <div>
            <h3>销量预测趋势</h3>
            <p>展示未来多天预测值与置信区间。</p>
          </div>
          <el-input-number v-model="fruitId" :min="1" controls-position="right" @change="loadTrend" />
        </div>
        <div ref="trendRef" class="chart-box"></div>
      </div>
      <div class="surface section-block chart-card side">
        <div class="inner-header">
          <div>
            <h3>库存预警热度</h3>
            <p>用于答辩展示临期与低库存聚集情况。</p>
          </div>
        </div>
        <div ref="heatRef" class="chart-box"></div>
      </div>
    </section>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { nextTick, onMounted, ref } from 'vue'
import { dashboardApi } from '@/api/modules'
import StatMetric from '@/components/StatMetric.vue'

const overview = ref({})
const fruitId = ref(1)
const trendRef = ref()
const heatRef = ref()
let trendChart
let heatChart

async function refreshAll() {
  overview.value = await dashboardApi.overview()
  await loadTrend()
  await loadHeatmap()
}

async function loadTrend() {
  const data = await dashboardApi.forecastTrend({ fruitId: fruitId.value })
  await nextTick()
  trendChart ||= echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 20, right: 20, bottom: 20, top: 36, containLabel: true },
    xAxis: { type: 'category', data: data.map((item) => item.targetDate) },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'line',
        smooth: true,
        areaStyle: { color: 'rgba(240, 140, 43, 0.16)' },
        lineStyle: { width: 3, color: '#f08c2b' },
        symbolSize: 8,
        data: data.map((item) => item.predictQty)
      }
    ]
  })
}

async function loadHeatmap() {
  const data = await dashboardApi.alertHeatmap()
  await nextTick()
  heatChart ||= echarts.init(heatRef.value)
  heatChart.setOption({
    tooltip: {},
    grid: { left: 20, right: 20, bottom: 24, top: 36, containLabel: true },
    xAxis: { type: 'category', data: ['低库存', '临期'] },
    yAxis: { type: 'category', data: ['仓库维度'] },
    visualMap: {
      min: 0,
      max: Math.max(data.length, 1),
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: { color: ['#fdf0d5', '#f08c2b', '#c8433e'] }
    },
    series: [
      {
        type: 'heatmap',
        data: [
          [0, 0, data.filter((item) => item.alertType === 'LOW_STOCK').length],
          [1, 0, data.filter((item) => item.alertType === 'EXPIRING').length]
        ],
        label: { show: true }
      }
    ]
  })
}

onMounted(async () => {
  await refreshAll()
  window.addEventListener('resize', () => {
    trendChart?.resize()
    heatChart?.resize()
  })
})
</script>

<style scoped>
.metric-cell {
  grid-column: span 3;
}

.chart-card.wide {
  grid-column: span 8;
}

.chart-card.side {
  grid-column: span 4;
}

.inner-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}

.inner-header p {
  margin: 6px 0 0;
  color: var(--text-soft);
}

.chart-box {
  height: 320px;
}

@media (max-width: 960px) {
  .metric-cell,
  .chart-card.wide,
  .chart-card.side {
    grid-column: span 1;
  }
}
</style>
