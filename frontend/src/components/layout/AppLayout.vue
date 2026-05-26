<script setup>
import { computed, onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import AppSidebar from './AppSidebar.vue'
import AppTopbar from './AppTopbar.vue'
import StatusBanner from './StatusBanner.vue'
import { useMonitoringStore } from '../../composables/useMonitoringStore'

const route = useRoute()
const { state, initialize } = useMonitoringStore()

const pageTitle = computed(() => route.meta.title ?? '环境监控系统')

onMounted(() => {
  initialize()
})
</script>

<template>
  <div class="shell">
    <AppSidebar />

    <div class="shell__main">
      <AppTopbar :title="pageTitle" />
      <StatusBanner :message="state.message" />

      <div class="shell__content">
        <RouterView />
      </div>
    </div>
  </div>
</template>
