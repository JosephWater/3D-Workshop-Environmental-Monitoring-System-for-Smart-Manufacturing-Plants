<script setup>
import { computed } from 'vue'
import { useMonitoringStore } from '../../composables/useMonitoringStore'
import { formatDateTime } from '../../utils/format'

const props = defineProps({
  title: {
    type: String,
    required: true,
  },
})

const { state, loadOverview } = useMonitoringStore()

const latestCollectTime = computed(() => formatDateTime(state.summary.latestCollectTime))
</script>

<template>
  <header class="topbar">
    <div>
      <span class="section-kicker">Intelligent Manufacturing Workshop</span>
      <h1>{{ props.title }}</h1>
      <p class="topbar__subtitle">
        通过数据导入、三维总览、告警联动和图表分析，形成完整的环境监控与展示闭环。
      </p>
    </div>

    <div class="topbar__meta">
      <div class="topbar__chip">
        <span>最近采集时间</span>
        <strong>{{ latestCollectTime }}</strong>
      </div>
      <button class="button button--ghost" type="button" @click="loadOverview(true)">
        {{ state.loading.overview ? '刷新中...' : '刷新总览' }}
      </button>
    </div>
  </header>
</template>
