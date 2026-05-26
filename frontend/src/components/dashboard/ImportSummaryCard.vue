<script setup>
import { formatDateTime } from '../../utils/format'

defineProps({
  lastImport: {
    type: Object,
    default: null,
  },
})
</script>

<template>
  <section class="card">
    <div class="card__header">
      <div>
        <span class="section-kicker">Latest Import</span>
        <h2>导入结果</h2>
      </div>
    </div>

    <div v-if="lastImport" class="meta-list">
      <p><span>来源文件</span><strong>{{ lastImport.sourceFile }}</strong></p>
      <p><span>导入条数</span><strong>{{ lastImport.importedRows }}</strong></p>
      <p><span>新增告警</span><strong>{{ lastImport.newAlarmCount }}</strong></p>
      <p><span>导入时间</span><strong>{{ formatDateTime(lastImport.importedAt) }}</strong></p>
    </div>

    <p v-else class="empty-copy">当前还没有导入记录。</p>
  </section>
</template>
