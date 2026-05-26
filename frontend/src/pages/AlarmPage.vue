<script setup>
import AlarmFilterCard from '../components/alarms/AlarmFilterCard.vue'
import AlarmListCard from '../components/alarms/AlarmListCard.vue'
import { useMonitoringStore } from '../composables/useMonitoringStore'

const { state, sensorOptions, loadAlarms } = useMonitoringStore()
</script>

<template>
  <div class="page-stack">
    <AlarmFilterCard
      :filters="state.alarmFilters"
      :sensor-options="sensorOptions"
      :loading="state.loading.alarms"
      @search="loadAlarms"
    />
    <AlarmListCard :alarms="state.alarms" />
  </div>
</template>
