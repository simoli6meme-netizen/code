<template>
  <div class="page data-page">
    <!-- 概览数据 -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-card__icon" style="background: #fff1f1; color: #f64848">💰</div>
        <div>
          <div class="stat-card__label">销售总额</div>
          <div class="stat-card__value">
            <small>￥</small>{{ formatMoney(data.count.total) }}
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__icon" style="background: #eef1ff; color: #5b6cff">📈</div>
        <div>
          <div class="stat-card__label">今日销售额</div>
          <div class="stat-card__value">
            <small>￥</small>{{ formatMoney(data.count.today) }}
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__icon" style="background: #e8f6ff; color: #2f9be0">📦</div>
        <div>
          <div class="stat-card__label">商品总数</div>
          <div class="stat-card__value">
            {{ data.count.goods ?? '-' }}<small>件</small>
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__icon" style="background: #f2edff; color: #8b6cf0">👥</div>
        <div>
          <div class="stat-card__label">注册用户</div>
          <div class="stat-card__value">
            {{ data.count.user ?? '-' }}<small>人</small>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表 -->
    <div class="chart-grid">
      <div class="chart-card panel">
        <div class="chart-card__head">
          <span class="chart-card__title">近一周订单销售趋势</span>
          <span class="chart-card__hint">单位：元</span>
        </div>
        <div id="line" class="chart-card__body"></div>
      </div>

      <div class="chart-card panel">
        <div class="chart-card__head">
          <span class="chart-card__title">分类商品销售额占比</span>
          <span class="chart-card__hint">单位：元</span>
        </div>
        <div id="pie" class="chart-card__body"></div>
      </div>
    </div>
  </div>
</template>
<script setup>
import {reactive , onMounted, onBeforeUnmount} from "vue";
import request from "@/utils/request";
import * as echarts from 'echarts'

const data = reactive({
  count: {}
})

const formatMoney = (value) => {
  const num = Number(value || 0)
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const lineOption = {
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(46,49,67,.92)',
    borderWidth: 0,
    textStyle: { color: '#fff', fontSize: 12 },
    axisPointer: { type: 'line', lineStyle: { color: '#c8ccdb', type: 'dashed' } }
  },
  grid: {
    top: 30,
    left: 14,
    right: 56,
    bottom: 26,
    containLabel: true
  },
  xAxis: {
    name: '日期',
    type: 'category',
    boundaryGap: false,
    data: [],
    axisLine: { lineStyle: { color: '#e6e8f0' } },
    axisTick: { show: false },
    axisLabel: { color: '#8c93a8', fontSize: 11 }
  },
  yAxis: {
    name: '销售额（元）',
    type: 'value',
    nameTextStyle: { color: '#8c93a8', fontSize: 11 },
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#8c93a8', fontSize: 11 },
    splitLine: { lineStyle: { color: '#f0f2f8', type: 'dashed' } }
  },
  series: [
    {
      name: '销售额',
      data: [],
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 7,
      showSymbol: false,
      lineStyle: { width: 3, color: '#5b6cff' },
      itemStyle: { color: '#5b6cff', borderColor: '#fff', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(91,108,255,.26)' },
          { offset: 1, color: 'rgba(91,108,255,.02)' }
        ])
      },
      markPoint: {
        symbol: 'pin',
        symbolSize: 42,
        data: [
          { type: 'max', name: 'Max' }
        ],
        itemStyle: { color: '#f64848' },
        label: { color: '#fff', fontSize: 10 }
      },
      markLine: {
        symbol: 'none',
        data: [{ type: 'average', name: 'Avg' }],
        lineStyle: { color: '#f9b44c', type: 'dashed' },
        label: { color: '#b07a12', fontSize: 11, formatter: '均值 {c}' }
      }
    },
  ]
}

const pieOption = {
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(46,49,67,.92)',
    borderWidth: 0,
    textStyle: { color: '#fff', fontSize: 12 },
    formatter: '{b}<br/>{c} 元（{d}%）'
  },
  legend: {
    bottom: 0,
    icon: 'circle',
    itemWidth: 8,
    itemHeight: 8,
    itemGap: 14,
    textStyle: { color: '#59607a', fontSize: 12 }
  },
  color: ['#5b6cff', '#34c38f', '#f9b44c', '#f64848', '#8b6cf0', '#2f9be0', '#e46ba8'],
  series: [
    {
      name: '销售额',
      type: 'pie',
      center: ['50%', '46%'],
      radius: ['46%', '68%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 3,
        borderRadius: 6
      },
      label: {
        show: false
      },
      emphasis: {
        scale: true,
        scaleSize: 6,
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 600,
          formatter: '{b}\n{d}%'
        }
      },
      labelLine: { show: false },
      data: []
    }
  ]
}

request.get('/count').then(res => {
  data.count = res.data || {}
})

let lineChart = null
let pieChart = null

const resizeCharts = () => {
  lineChart && lineChart.resize()
  pieChart && pieChart.resize()
}

// 等页面所有元素加载完成后再设置 echarts图表
onMounted(() => {
  //折线图
  let lineDom = document.getElementById('line')
  lineChart = echarts.init(lineDom)
  // 请求数据  初始化图表
  request.get('/selectLine').then(res => {
    lineOption.xAxis.data = res.data.date
    lineOption.series[0].data = res.data.count
    lineChart.setOption(lineOption)
  })
//饼图
  let pieDom = document.getElementById('pie')
  pieChart = echarts.init(pieDom)
  // 请求数据  初始化图表
  request.get('/selectPie').then(res => {
    pieOption.series[0].data = res.data
    pieChart.setOption(pieOption)
  })

  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  lineChart && lineChart.dispose()
  pieChart && pieChart.dispose()
  lineChart = null
  pieChart = null
})
</script>

<style scoped>
.data-page {
  gap: 16px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.chart-card {
  border-radius: var(--radius, 12px);
  background: #fff;
  border: 1px solid var(--line, #eceef3);
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
  padding: 18px 18px 12px;
  height: 460px;
  display: flex;
  flex-direction: column;
}

.chart-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  flex-shrink: 0;
}

.chart-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text, #1f2430);
  position: relative;
  padding-left: 11px;
}

.chart-card__title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: var(--brand, #2e3143);
}

.chart-card__hint {
  font-size: 11.5px;
  color: var(--text-3, #8c93a8);
}

.chart-card__body {
  flex: 1;
  min-height: 0;
  width: 100%;
}

@media (max-width: 1100px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
  .chart-card {
    height: 400px;
  }
}

@media (max-width: 640px) {
  .chart-card {
    height: 340px;
    padding: 14px 10px 8px;
  }
}
</style>
