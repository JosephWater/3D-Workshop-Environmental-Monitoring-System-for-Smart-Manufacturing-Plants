<script setup>
import { formatDateTime, formatNumber } from '../../utils/format'

defineProps({
  sensor: {
    type: Object,
    default: null,
  },
})
</script>

<template>
  <section class="card">
    <div class="card__header">
      <div>
        <span class="section-kicker">Selected Sensor</span>
        <h2>点位详情</h2>
      </div>
    </div>

    <div v-if="sensor" class="detail-stack">
      <div class="detail-head">
        <div>
          <strong class="detail-head__code">{{ sensor.sensorCode }}</strong>
          <p>{{ sensor.sensorName }}</p>
        </div>
        <span class="pill" :class="sensor.alarmActive ? 'pill--danger' : 'pill--ok'">
          {{ sensor.alarmActive ? '告警中' : '正常' }}
        </span>
      </div>

      <div class="detail-grid">
        <div>
          <span>温度</span>
          <strong>{{ formatNumber(sensor.temperature, '℃') }}</strong>
        </div>
        <div>
          <span>湿度</span>
          <strong>{{ formatNumber(sensor.humidity, '%') }}</strong>
        </div>
        <div>
          <span>点位编号</span>
          <strong>{{ sensor.gridPosition }}</strong>
        </div>
        <div>
          <span>更新时间</span>
          <strong>{{ formatDateTime(sensor.collectTime) }}</strong>
        </div>
      </div>

      <div class="tag-list">
        <span v-for="alarmType in sensor.alarmTypes" :key="alarmType" class="tag tag--danger">
          {{ alarmType }}
        </span>
        <span v-if="sensor.alarmTypes.length === 0" class="tag">无告警</span>
      </div>
    </div>

    <p v-else class="empty-copy">当前没有可展示的传感器数据。</p>
  </section>
</template>
