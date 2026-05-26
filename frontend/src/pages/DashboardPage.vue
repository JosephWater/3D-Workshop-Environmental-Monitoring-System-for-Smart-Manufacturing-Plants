<script setup>
import ScenePanel from '../components/dashboard/ScenePanel.vue'
import SensorDetailCard from '../components/dashboard/SensorDetailCard.vue'
import SensorGridCard from '../components/dashboard/SensorGridCard.vue'
import { useMonitoringStore } from '../composables/useMonitoringStore'

const { state, selectedSensor, activeAlarms, handleImport, selectSensor } = useMonitoringStore()
</script>

<template>
  <div class="page-stack">
    <div class="dashboard-grid dashboard-grid--wide">
      <div class="dashboard-grid__main">
        <ScenePanel
          :sensors="state.sensors"
          :selected-sensor-code="state.selectedSensorCode"
          :uploading="state.loading.upload"
          @select-sensor="selectSensor"
          @import-file="handleImport"
        />
      </div>

      <div class="dashboard-grid__side">
        <SensorDetailCard :sensor="selectedSensor" />
        <SensorGridCard
          :sensors="state.sensors"
          :selected-sensor-code="state.selectedSensorCode"
          :active-alarm-count="activeAlarms.length"
          @select="selectSensor"
        />
      </div>
    </div>
  </div>
</template>
