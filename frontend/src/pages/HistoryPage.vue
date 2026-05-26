<script setup>
import HistoryFilterCard from '../components/history/HistoryFilterCard.vue'
import HistoryTableCard from '../components/history/HistoryTableCard.vue'
import { useMonitoringStore } from '../composables/useMonitoringStore'

const { state, sensorOptions, loadHistory } = useMonitoringStore()
</script>

<template>
  <div class="page-stack">
    <HistoryFilterCard
      :filters="state.historyFilters"
      :sensor-options="sensorOptions"
      :loading="state.loading.history"
      @search="loadHistory"
    />
    <HistoryTableCard :rows="state.historyRows" />
  </div>
</template>
