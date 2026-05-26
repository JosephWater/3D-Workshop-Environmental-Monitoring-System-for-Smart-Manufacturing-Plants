<script setup>
import { defineAsyncComponent } from 'vue'

const WorkshopScene = defineAsyncComponent(() => import('../workshop/WorkshopScene.vue'))

defineProps({
  sensors: {
    type: Array,
    required: true,
  },
  selectedSensorCode: {
    type: String,
    required: true,
  },
  uploading: {
    type: Boolean,
    required: true,
  },
})

const emit = defineEmits(['select-sensor', 'import-file'])

function handleFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    return
  }
  emit('import-file', file)
  event.target.value = ''
}
</script>

<template>
  <section class="card card--scene">
    <div class="card__header">
      <div>
        <span class="section-kicker">3D Workshop</span>
        <h2>三维车间总览</h2>
      </div>

      <label class="button">
        <input type="file" accept=".csv,.xlsx,.xls" hidden @change="handleFileChange" />
        {{ uploading ? '导入中...' : '上传 CSV / Excel' }}
      </label>
    </div>

    <WorkshopScene
      :sensors="sensors"
      :selected-sensor-code="selectedSensorCode"
      @select="emit('select-sensor', $event)"
    />
  </section>
</template>
