const { buildWebUrl } = require('../../utils/config')

const TITLES = {
  console: '控制台',
  dashboard: '车间总览',
  history: '历史数据',
  alarms: '告警查询',
}

Page({
  data: {
    route: 'console',
    src: '',
    title: '控制台',
    tabs: [
      { key: 'console', label: '控制台' },
      { key: 'dashboard', label: '总览' },
      { key: 'history', label: '历史' },
      { key: 'alarms', label: '告警' },
    ],
  },

  onLoad(options) {
    const route = options.route || 'console'
    this.setRoute(route)
  },

  switchTab(event) {
    const route = event.currentTarget.dataset.route
    this.setRoute(route)
  },

  setRoute(route) {
    const title = TITLES[route] || TITLES.console
    wx.setNavigationBarTitle({ title })
    this.setData({
      route,
      title,
      src: buildWebUrl(route),
    })
  },
})
