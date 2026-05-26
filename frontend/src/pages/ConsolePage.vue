<script setup>
import { computed, onMounted } from 'vue'
import ImportSummaryCard from '../components/dashboard/ImportSummaryCard.vue'
import RuleFormCard from '../components/dashboard/RuleFormCard.vue'
import StatsPanel from '../components/dashboard/StatsPanel.vue'
import EChartCard from '../components/console/EChartCard.vue'
import { useMonitoringStore } from '../composables/useMonitoringStore'
import { formatNumber } from '../utils/format'

const { state, statCards, ensureConsoleData, loadConsoleData, saveRule } = useMonitoringStore()

onMounted(() => {
  ensureConsoleData()
})

const historyRowsAsc = computed(() =>
  [...state.consoleHistoryRows].sort((left, right) => new Date(left.collectTime) - new Date(right.collectTime)),
)

const timelineMetrics = computed(() => {
  const grouped = new Map()
  historyRowsAsc.value.forEach((row) => {
    const key = row.collectTime
    const item = grouped.get(key) ?? { temp: 0, hum: 0, count: 0 }
    item.temp += Number(row.temperature)
    item.hum += Number(row.humidity)
    item.count += 1
    grouped.set(key, item)
  })

  return [...grouped.entries()].map(([time, value]) => ({
    time,
    avgTemp: Number((value.temp / value.count).toFixed(2)),
    avgHum: Number((value.hum / value.count).toFixed(2)),
  }))
})

const sensorAverages = computed(() => {
  const grouped = new Map()
  historyRowsAsc.value.forEach((row) => {
    const item = grouped.get(row.sensorCode) ?? {
      sensorCode: row.sensorCode,
      sensorName: row.sensorName,
      temp: 0,
      hum: 0,
      count: 0,
    }
    item.temp += Number(row.temperature)
    item.hum += Number(row.humidity)
    item.count += 1
    grouped.set(row.sensorCode, item)
  })

  return [...grouped.values()]
    .map((item) => ({
      sensorCode: item.sensorCode,
      sensorName: item.sensorName,
      avgTemp: Number((item.temp / item.count).toFixed(2)),
      avgHum: Number((item.hum / item.count).toFixed(2)),
    }))
    .sort((left, right) => left.sensorCode.localeCompare(right.sensorCode))
})

const alarmTypeCounts = computed(() => {
  const counts = new Map()
  state.consoleAlarmRows.forEach((alarm) => {
    counts.set(alarm.alarmType, (counts.get(alarm.alarmType) ?? 0) + 1)
  })
  return [...counts.entries()].map(([name, value]) => ({ name, value }))
})

const alarmStatusCounts = computed(() => {
  const counts = new Map()
  state.consoleAlarmRows.forEach((alarm) => {
    counts.set(alarm.status, (counts.get(alarm.status) ?? 0) + 1)
  })
  return [...counts.entries()].map(([name, value]) => ({ name, value }))
})

const sourceFileCounts = computed(() => {
  const counts = new Map()
  historyRowsAsc.value.forEach((row) => {
    const source = row.sourceFile || '未记录'
    counts.set(source, (counts.get(source) ?? 0) + 1)
  })
  return [...counts.entries()]
    .map(([name, value]) => ({ name, value }))
    .sort((left, right) => right.value - left.value)
})

const topAlarmSensors = computed(() => {
  const counts = new Map()
  state.consoleAlarmRows.forEach((alarm) => {
    const item = counts.get(alarm.sensorCode) ?? {
      sensorCode: alarm.sensorCode,
      sensorName: alarm.sensorName,
      count: 0,
    }
    item.count += 1
    counts.set(alarm.sensorCode, item)
  })

  return [...counts.values()].sort((left, right) => right.count - left.count).slice(0, 5)
})

const averageTemp = computed(() => {
  if (historyRowsAsc.value.length === 0) {
    return null
  }
  const sum = historyRowsAsc.value.reduce((total, row) => total + Number(row.temperature), 0)
  return sum / historyRowsAsc.value.length
})

const averageHumidity = computed(() => {
  if (historyRowsAsc.value.length === 0) {
    return null
  }
  const sum = historyRowsAsc.value.reduce((total, row) => total + Number(row.humidity), 0)
  return sum / historyRowsAsc.value.length
})

const trendOption = computed(() => ({
  color: ['#5b6779', '#7fb7ff'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0, textStyle: { color: '#5f6b7a' } },
  grid: { left: 36, right: 18, top: 42, bottom: 26 },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    axisLabel: { color: '#6b7787', formatter: (value) => value.slice(11, 16) },
    data: timelineMetrics.value.slice(-36).map((item) => item.time),
  },
  yAxis: [
    {
      type: 'value',
      name: '温度',
      axisLabel: { color: '#6b7787' },
      splitLine: { lineStyle: { color: '#d9e0e8' } },
    },
    {
      type: 'value',
      name: '湿度',
      axisLabel: { color: '#6b7787' },
      splitLine: { show: false },
    },
  ],
  series: [
    {
      name: '平均温度',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: { color: 'rgba(91, 103, 121, 0.12)' },
      data: timelineMetrics.value.slice(-36).map((item) => item.avgTemp),
    },
    {
      name: '平均湿度',
      type: 'line',
      smooth: true,
      yAxisIndex: 1,
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: { color: 'rgba(127, 183, 255, 0.12)' },
      data: timelineMetrics.value.slice(-36).map((item) => item.avgHum),
    },
  ],
}))

const sensorAverageOption = computed(() => ({
  color: ['#677282', '#8ad2d6'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0, textStyle: { color: '#5f6b7a' } },
  grid: { left: 40, right: 18, top: 42, bottom: 26 },
  xAxis: {
    type: 'category',
    axisLabel: { color: '#6b7787' },
    data: sensorAverages.value.map((item) => item.sensorCode),
  },
  yAxis: {
    type: 'value',
    axisLabel: { color: '#6b7787' },
    splitLine: { lineStyle: { color: '#d9e0e8' } },
  },
  series: [
    {
      name: '平均温度',
      type: 'bar',
      borderRadius: [10, 10, 0, 0],
      data: sensorAverages.value.map((item) => item.avgTemp),
    },
    {
      name: '平均湿度',
      type: 'bar',
      borderRadius: [10, 10, 0, 0],
      data: sensorAverages.value.map((item) => item.avgHum),
    },
  ],
}))

const alarmTypeOption = computed(() => ({
  color: ['#f0b54d', '#7fb7ff', '#8ad2d6', '#9098a4'],
  tooltip: { trigger: 'item' },
  legend: {
    orient: 'vertical',
    right: 8,
    top: 'middle',
    textStyle: { color: '#5f6b7a' },
  },
  series: [
    {
      name: '告警类型',
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['38%', '52%'],
      label: { formatter: '{b}\n{c} 条' },
      data: alarmTypeCounts.value,
    },
  ],
}))

const sourceFileOption = computed(() => ({
  color: ['#7a838f'],
  tooltip: { trigger: 'axis' },
  grid: { left: 40, right: 18, top: 22, bottom: 56 },
  xAxis: {
    type: 'category',
    axisLabel: { color: '#6b7787', interval: 0, rotate: 18 },
    data: sourceFileCounts.value.map((item) => item.name),
  },
  yAxis: {
    type: 'value',
    axisLabel: { color: '#6b7787' },
    splitLine: { lineStyle: { color: '#d9e0e8' } },
  },
  series: [
    {
      type: 'bar',
      data: sourceFileCounts.value.map((item) => item.value),
      borderRadius: [10, 10, 0, 0],
      barMaxWidth: 56,
    },
  ],
}))
</script>

<template>
  <div class="page-stack">
    <StatsPanel :cards="statCards" />

    <div class="console-layout">
      <div class="console-layout__main">
        <div class="chart-grid">
          <EChartCard
            title="温湿度趋势"
            subtitle="按采集时间聚合的平均温度与平均湿度"
            :loading="state.loading.console"
            :option="trendOption"
          />
          <EChartCard
            title="各传感器平均值"
            subtitle="按传感器统计平均温度与平均湿度"
            :loading="state.loading.console"
            :option="sensorAverageOption"
          />
          <EChartCard
            title="告警类型占比"
            subtitle="统计 TEMP_HIGH / TEMP_LOW / HUM_HIGH / HUM_LOW 的分布"
            :loading="state.loading.console"
            :option="alarmTypeOption"
          />
          <EChartCard
            title="来源文件数据量"
            subtitle="用于查看不同导入文件的记录贡献"
            :loading="state.loading.console"
            :option="sourceFileOption"
          />
        </div>
      </div>

      <div class="console-layout__side">
        <section class="card">
          <div class="card__header">
            <div>
              <span class="section-kicker">Console Summary</span>
              <h2>控制台摘要</h2>
            </div>
            <button class="button button--ghost button--tiny" type="button" @click="loadConsoleData(true)">
              {{ state.loading.console ? '刷新中...' : '刷新图表' }}
            </button>
          </div>

          <div class="meta-list">
            <p><span>平均温度</span><strong>{{ formatNumber(averageTemp, '℃') }}</strong></p>
            <p><span>平均湿度</span><strong>{{ formatNumber(averageHumidity, '%') }}</strong></p>
            <p>
              <span>告警状态分布</span>
              <strong>{{ alarmStatusCounts.map((item) => `${item.name}: ${item.value}`).join(' / ') || '--' }}</strong>
            </p>
            <p>
              <span>数据文件数量</span>
              <strong>{{ sourceFileCounts.length }}</strong>
            </p>
          </div>
        </section>

        <section class="card">
          <div class="card__header">
            <div>
              <span class="section-kicker">Top Sensors</span>
              <h2>高频告警点位</h2>
            </div>
          </div>

          <div class="meta-list">
            <p v-for="item in topAlarmSensors" :key="item.sensorCode">
              <span>{{ item.sensorCode }} · {{ item.sensorName }}</span>
              <strong>{{ item.count }} 次</strong>
            </p>
            <p v-if="topAlarmSensors.length === 0" class="empty-copy">当前没有可统计的告警数据。</p>
          </div>
        </section>

        <RuleFormCard :rule-form="state.ruleForm" :saving="state.loading.rule" @save="saveRule" />
        <ImportSummaryCard :last-import="state.lastImport" />
      </div>
    </div>
  </div>
</template>
