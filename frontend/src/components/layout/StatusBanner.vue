<script setup>
defineProps({
  message: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <div class="status-banner" :class="`status-banner--${message.type}`">
    <span class="status-banner__dot" />
    <p>{{ message.text }}</p>
  </div>
</template>
