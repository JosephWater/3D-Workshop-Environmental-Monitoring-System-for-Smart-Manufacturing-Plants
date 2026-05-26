<script setup>
defineProps({
  filters: {
    type: Object,
    required: true,
  },
  sensorOptions: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    required: true,
  },
})

const emit = defineEmits(['search'])
</script>

<template>
  <section class="card">
    <div class="card__header">
      <div>
        <span class="section-kicker">Alarm Query</span>
        <h2>告警条件筛选</h2>
      </div>
      <button class="button button--ghost" type="button" @click="emit('search')">
        {{ loading ? '查询中...' : '执行查询' }}
      </button>
    </div>

    <div class="filter-grid filter-grid--wide">
      <label>
        <span>传感器</span>
        <select v-model="filters.sensorCode">
          <option value="">全部点位</option>
          <option v-for="option in sensorOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </label>
      <label>
        <span>告警类型</span>
        <select v-model="filters.alarmType">
          <option value="">全部类型</option>
          <option value="TEMP_HIGH">TEMP_HIGH</option>
          <option value="TEMP_LOW">TEMP_LOW</option>
          <option value="HUM_HIGH">HUM_HIGH</option>
          <option value="HUM_LOW">HUM_LOW</option>
        </select>
      </label>
      <label>
        <span>状态</span>
        <select v-model="filters.status">
          <option value="">全部状态</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="RESOLVED">RESOLVED</option>
        </select>
      </label>
      <label>
        <span>开始时间</span>
        <input v-model="filters.startTime" type="datetime-local" />
      </label>
      <label>
        <span>结束时间</span>
        <input v-model="filters.endTime" type="datetime-local" />
      </label>
    </div>
  </section>
</template>
