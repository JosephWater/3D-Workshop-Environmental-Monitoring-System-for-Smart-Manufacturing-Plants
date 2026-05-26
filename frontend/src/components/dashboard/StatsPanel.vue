<script setup>
defineProps({
  cards: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <section class="stats-grid">
    <article
      v-for="card in cards"
      :key="card.label"
      class="stat-card"
      :class="{ 'stat-card--alert': card.label === '当前告警' && Number(card.value) > 0 }"
    >
      <span>{{ card.label }}</span>
      <strong>{{ card.value }}</strong>
      <small>{{ card.hint }}</small>
    </article>
  </section>
</template>
