<script setup>
import { formatDateTime, formatNumber } from '../../utils/format'

defineProps({
  rows: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <section class="card">
    <div class="card__header">
      <div>
        <span class="section-kicker">History Result</span>
        <h2>查询结果</h2>
      </div>
    </div>

    <div class="table-shell">
      <table>
        <thead>
          <tr>
            <th>传感器</th>
            <th>名称</th>
            <th>温度</th>
            <th>湿度</th>
            <th>采集时间</th>
            <th>来源文件</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.id">
            <td>{{ row.sensorCode }}</td>
            <td>{{ row.sensorName }}</td>
            <td>{{ formatNumber(row.temperature, '℃') }}</td>
            <td>{{ formatNumber(row.humidity, '%') }}</td>
            <td>{{ formatDateTime(row.collectTime) }}</td>
            <td>{{ row.sourceFile }}</td>
          </tr>
          <tr v-if="rows.length === 0">
            <td colspan="6" class="table-empty">暂无符合条件的数据。</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
