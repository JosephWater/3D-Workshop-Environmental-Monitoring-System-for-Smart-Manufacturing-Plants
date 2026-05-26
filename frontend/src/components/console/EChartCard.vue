<script setup>
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  subtitle: {
    type: String,
    default: '',
  },
  option: {
    type: Object,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const chartEl = ref(null)
let chart
let resizeObserver

function renderChart() {
  if (!chartEl.value) {
    return
  }
  if (!chart) {
    chart = echarts.init(chartEl.value)
  }
  chart.setOption(props.option, true)
  chart.resize()
}

onMounted(async () => {
  await nextTick()
  renderChart()
  resizeObserver = new ResizeObserver(() => {
    chart?.resize()
  })
  if (chartEl.value) {
    resizeObserver.observe(chartEl.value)
  }
  window.addEventListener('resize', renderChart)
})

watch(
  () => props.option,
  async () => {
    await nextTick()
    renderChart()
  },
  { deep: true },
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', renderChart)
  resizeObserver?.disconnect()
  chart?.dispose()
})
</script>

<template>
  <section class="card chart-card">
    <div class="card__header">
      <div>
        <span class="section-kicker">Data Visualization</span>
        <h2>{{ title }}</h2>
        <p v-if="subtitle" class="chart-card__subtitle">{{ subtitle }}</p>
      </div>
      <span v-if="loading" class="section-note">加载中...</span>
    </div>

    <div ref="chartEl" class="chart-card__canvas" />
  </section>
</template>
