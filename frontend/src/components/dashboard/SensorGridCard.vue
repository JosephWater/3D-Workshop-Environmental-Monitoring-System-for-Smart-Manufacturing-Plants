<script setup>
import { formatNumber } from '../../utils/format'

defineProps({
  sensors: {
    type: Array,
    required: true,
  },
  selectedSensorCode: {
    type: String,
    required: true,
  },
  activeAlarmCount: {
    type: Number,
    required: true,
  },
})

const emit = defineEmits(['select'])
</script>

<template>
  <section class="card">
    <div class="card__header">
      <div>
        <span class="section-kicker">Sensor Matrix</span>
        <h2>九宫格点位状态</h2>
      </div>
      <span class="section-note">{{ activeAlarmCount }} 个点位处于告警状态</span>
    </div>

    <div class="sensor-grid">
      <button
        v-for="sensor in sensors"
        :key="sensor.sensorCode"
        class="sensor-tile"
        :class="{
          'sensor-tile--active': sensor.sensorCode === selectedSensorCode,
          'sensor-tile--danger': sensor.alarmActive,
        }"
        type="button"
        @click="emit('select', sensor.sensorCode)"
      >
        <div class="sensor-tile__head">
          <strong>{{ sensor.sensorCode }}</strong>
          <span>{{ sensor.gridPosition }}</span>
        </div>
        <p>{{ sensor.sensorName }}</p>
        <div class="sensor-tile__metrics">
          <span>{{ formatNumber(sensor.temperature, '℃') }}</span>
          <span>{{ formatNumber(sensor.humidity, '%') }}</span>
        </div>
      </button>
    </div>
  </section>
</template>
