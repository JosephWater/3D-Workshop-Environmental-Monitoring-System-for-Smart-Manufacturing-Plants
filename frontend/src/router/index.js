import { createRouter, createWebHistory } from 'vue-router'

const ConsolePage = () => import('../pages/ConsolePage.vue')
const DashboardPage = () => import('../pages/DashboardPage.vue')
const HistoryPage = () => import('../pages/HistoryPage.vue')
const AlarmPage = () => import('../pages/AlarmPage.vue')

const routes = [
  {
    path: '/',
    redirect: '/console',
  },
  {
    path: '/console',
    name: 'console',
    component: ConsolePage,
    meta: {
      title: '控制台',
    },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: DashboardPage,
    meta: {
      title: '车间总览',
    },
  },
  {
    path: '/history',
    name: 'history',
    component: HistoryPage,
    meta: {
      title: '历史数据',
    },
  },
  {
    path: '/alarms',
    name: 'alarms',
    component: AlarmPage,
    meta: {
      title: '告警查询',
    },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.afterEach((to) => {
  document.title = `${to.meta.title ?? '环境监控系统'} - 智能制造工厂三维车间环境监控系统`
})

export default router
