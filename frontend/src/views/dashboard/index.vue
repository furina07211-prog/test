<template>
  <div class="dashboard-container">
    <!-- Stats Cards -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409EFF;">
            <i class="el-icon-goods"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ dashboard.totalProducts || 0 }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67C23A;">
            <i class="el-icon-box"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ dashboard.totalInventory || 0 }}</div>
            <div class="stat-label">库存总量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #E6A23C;">
            <i class="el-icon-download"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ dashboard.todayInboundCount || 0 }}</div>
            <div class="stat-label">今日入库</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #F56C6C;">
            <i class="el-icon-warning"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ dashboard.activeAlerts || 0 }}</div>
            <div class="stat-label">待处理预警</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card>
          <div slot="header">近7天出入库统计</div>
          <div ref="inOutChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div slot="header">商品出库排行</div>
          <div ref="rankingChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getDashboard, getInOutStats, getProductRanking } from '@/api/statistics'
import dayjs from 'dayjs'

export default {
  name: 'Dashboard',
  data() {
    return {
      dashboard: {}
    }
  },
  mounted() {
    this.fetchData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.inOutChart) this.inOutChart.dispose()
    if (this.rankingChart) this.rankingChart.dispose()
  },
  methods: {
    async fetchData() {
      try {
        this.dashboard = await getDashboard()
        this.initInOutChart()
        this.initRankingChart()
      } catch (e) {
        // Error handled
      }
    },
    async initInOutChart() {
      const endDate = dayjs().format('YYYY-MM-DD')
      const startDate = dayjs().subtract(6, 'day').format('YYYY-MM-DD')
      
      try {
        const data = await getInOutStats({ startDate, endDate })
        
        this.inOutChart = echarts.init(this.$refs.inOutChart)
        this.inOutChart.setOption({
          tooltip: { trigger: 'axis' },
          legend: { data: ['入库', '出库'] },
          xAxis: {
            type: 'category',
            data: data.map(d => d.date)
          },
          yAxis: { type: 'value' },
          series: [
            {
              name: '入库',
              type: 'bar',
              data: data.map(d => d.inQuantity),
              itemStyle: { color: '#409EFF' }
            },
            {
              name: '出库',
              type: 'bar',
              data: data.map(d => d.outQuantity),
              itemStyle: { color: '#E6A23C' }
            }
          ]
        })
      } catch (e) {
        // Error handled
      }
    },
    async initRankingChart() {
      const endDate = dayjs().format('YYYY-MM-DD')
      const startDate = dayjs().subtract(29, 'day').format('YYYY-MM-DD')
      
      try {
        const data = await getProductRanking({ startDate, endDate, limit: 5 })
        
        this.rankingChart = echarts.init(this.$refs.rankingChart)
        this.rankingChart.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'value' },
          yAxis: {
            type: 'category',
            data: data.map(d => d.productName).reverse()
          },
          series: [{
            type: 'bar',
            data: data.map(d => d.quantity).reverse(),
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#67C23A' },
                { offset: 1, color: '#409EFF' }
              ])
            }
          }]
        })
      } catch (e) {
        // Error handled
      }
    },
    handleResize() {
      if (this.inOutChart) this.inOutChart.resize()
      if (this.rankingChart) this.rankingChart.resize()
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      padding: 20px;
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 15px;

      i {
        font-size: 28px;
        color: #fff;
      }
    }

    .stat-content {
      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 5px;
      }
    }
  }

  .chart-row {
    .chart {
      height: 350px;
    }
  }
}
</style>
