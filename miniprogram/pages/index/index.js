const { H5_BASE_URL } = require('../../utils/config')

Page({
  data: {
    h5BaseUrl: H5_BASE_URL,
    cards: [
      {
        title: '控制台',
        desc: '查看统计概览、图表分析、导入结果和阈值配置。',
        route: 'console',
      },
      {
        title: '车间总览',
        desc: '打开三维车间总览，查看九宫格传感器与实时联动。',
        route: 'dashboard',
      },
      {
        title: '历史数据',
        desc: '查询温度、湿度历史记录与来源文件。',
        route: 'history',
      },
      {
        title: '告警查询',
        desc: '按时间、状态和类型筛选告警记录。',
        route: 'alarms',
      },
    ],
  },

  openFeature(event) {
    const route = event.currentTarget.dataset.route
    wx.navigateTo({
      url: `/pages/webview/index?route=${route}`,
    })
  },
})
