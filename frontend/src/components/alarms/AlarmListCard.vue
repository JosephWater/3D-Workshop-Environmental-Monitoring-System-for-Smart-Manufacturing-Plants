<script setup>
import { formatDateTime } from '../../utils/format'

defineProps({
  alarms: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <section class="card">
    <div class="card__header">
      <div>
        <span class="section-kicker">Alarm Result</span>
        <h2>告警记录列表</h2>
      </div>
    </div>

    <div class="alarm-list">
      <article v-for="alarm in alarms" :key="alarm.id" class="alarm-card">
        <div class="alarm-card__head">
          <div>
            <span class="section-note">{{ alarm.sensorCode }} 路 {{ alarm.sensorName }}</span>
            <h3>{{ alarm.alarmType }}</h3>
          </div>
          <span class="pill" :class="alarm.status === 'ACTIVE' ? 'pill--danger' : 'pill--muted'">
            {{ alarm.status }}
          </span>
        </div>

        <div class="alarm-card__grid">
          <p><span>当前值</span><strong>{{ alarm.currentValue }}</strong></p>
          <p><span>阈值规则</span><strong>{{ alarm.thresholdDesc }}</strong></p>
          <p><span>等级</span><strong>{{ alarm.alarmLevel }}</strong></p>
          <p><span>告警时间</span><strong>{{ formatDateTime(alarm.alarmTime) }}</strong></p>
        </div>
      </article>

      <div v-if="alarms.length === 0" class="table-empty table-empty--card">暂无符合条件的告警记录。</div>
    </div>
  </section>
</template>
