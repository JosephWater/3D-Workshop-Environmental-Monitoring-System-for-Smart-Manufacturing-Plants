<script setup>
const props = defineProps({
  ruleForm: {
    type: Object,
    required: true,
  },
  saving: {
    type: Boolean,
    required: true,
  },
})

const emit = defineEmits(['save'])
</script>

<template>
  <section class="card">
    <div class="card__header">
      <div>
        <span class="section-kicker">Alarm Rule</span>
        <h2>阈值配置</h2>
      </div>

      <button class="button button--ghost" type="button" @click="emit('save')">
        {{ props.saving ? '保存中...' : '保存阈值' }}
      </button>
    </div>

    <div class="form-grid">
      <label>
        <span>温度下限</span>
        <input v-model="props.ruleForm.tempMin" type="number" step="0.1" />
      </label>
      <label>
        <span>温度上限</span>
        <input v-model="props.ruleForm.tempMax" type="number" step="0.1" />
      </label>
      <label>
        <span>湿度下限</span>
        <input v-model="props.ruleForm.humidityMin" type="number" step="0.1" />
      </label>
      <label>
        <span>湿度上限</span>
        <input v-model="props.ruleForm.humidityMax" type="number" step="0.1" />
      </label>
    </div>

    <label class="toggle">
      <input v-model="props.ruleForm.enabled" type="checkbox" />
      <span>启用实时告警规则</span>
    </label>
  </section>
</template>
