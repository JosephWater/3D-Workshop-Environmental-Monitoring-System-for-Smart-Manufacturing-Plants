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
        <span class="section-kicker">History Query</span>
        <h2>温湿度历史数据</h2>
      </div>
      <button class="button button--ghost" type="button" @click="emit('search')">
        {{ loading ? '查询中...' : '执行查询' }}
      </button>
    </div>

    <div class="filter-grid">
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
