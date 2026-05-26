<script setup>
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()

const items = [
  { to: '/console', label: '控制台', desc: '统计概览、阈值配置与图表分析' },
  { to: '/dashboard', label: '车间总览', desc: '三维车间、点位状态与实时联动' },
  { to: '/history', label: '历史数据', desc: '温湿度记录检索与明细表格' },
  { to: '/alarms', label: '告警查询', desc: '告警记录、状态与阈值规则回溯' },
]
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar__brand">
      <span class="sidebar__kicker">Factory Monitor</span>
      <strong>智能制造车间</strong>
      <p>以桌面端浏览体验为核心，覆盖导入、监控、告警和多维分析的完整演示链路。</p>
    </div>

    <nav class="sidebar__nav">
      <RouterLink
        v-for="item in items"
        :key="item.to"
        :to="item.to"
        class="sidebar__link"
        :class="{ 'sidebar__link--active': route.path === item.to }"
      >
        <strong>{{ item.label }}</strong>
        <span>{{ item.desc }}</span>
      </RouterLink>
    </nav>

    <div class="sidebar__foot">
      <p>支持导入 CSV、XLSX、XLS 三种数据文件格式。</p>
      <p>若存在 `public/models/workshop.glb`，系统会优先加载真实三维模型。</p>
    </div>
  </aside>
</template>
