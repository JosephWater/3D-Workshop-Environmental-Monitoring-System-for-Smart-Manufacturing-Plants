const app = getApp()
const {
  fetchSummary,
  fetchLatestSensors,
  fetchAlarmRule,
  updateAlarmRule,
  fetchHistory,
  fetchAlarms,
  uploadFile,
} = require('../../utils/api')
const { formatDateTime, formatNumber } = require('../../utils/format')
const { playAlarmTone } = require('../../utils/alarm')

function safePercent(value, total) {
  if (!total) {
    return 0
  }
  return Math.max(8, Math.round((value / total) * 100))
}

Page({
  data: {
    loading: false,
    uploading: false,
    saving: false,
    summaryCards: [],
    latestCollectTimeText: '暂无数据',
    lastImport: null,
    ruleForm: {
      tempMin: '18',
      tempMax: '30',
      humidityMin: '40',
      humidityMax: '70',
      enabled: true,
    },
    avgTempText: '--',
    avgHumidityText: '--',
    sensorBars: [],
    alarmBars: [],
    sourceBars: [],
  },

  onShow() {
    this.loadPageData()
  },

  async loadPageData(showToast = false) {
    this.setData({ loading: true })
    try {
      const [summary, sensors, rule, historyRows, alarms] = await Promise.all([
        fetchSummary(),
        fetchLatestSensors(),
        fetchAlarmRule(),
        fetchHistory(),
        fetchAlarms(),
      ])

      if ((app.globalData.lastAlarmCount || 0) < Number(summary.activeAlarmCount || 0)) {
        playAlarmTone()
      }
      app.globalData.lastAlarmCount = Number(summary.activeAlarmCount || 0)
      app.globalData.summary = summary

      const summaryCards = [
        { label: '在线点位', value: summary.totalSensors, hint: '九宫格监测点在线数量' },
        { label: '数据总量', value: summary.totalDataRows, hint: '已导入温湿度记录总数' },
        { label: '当前告警', value: summary.activeAlarmCount, hint: '活动告警数量' },
        { label: '已恢复告警', value: summary.resolvedAlarmCount, hint: '已恢复告警数量' },
      ]

      const groupedSensors = {}
      let totalTemp = 0
      let totalHumidity = 0
      historyRows.forEach((row) => {
        totalTemp += Number(row.temperature || 0)
        totalHumidity += Number(row.humidity || 0)
        if (!groupedSensors[row.sensorCode]) {
          groupedSensors[row.sensorCode] = {
            sensorCode: row.sensorCode,
            sensorName: row.sensorName,
            temp: 0,
            humidity: 0,
            count: 0,
          }
        }
        groupedSensors[row.sensorCode].temp += Number(row.temperature || 0)
        groupedSensors[row.sensorCode].humidity += Number(row.humidity || 0)
        groupedSensors[row.sensorCode].count += 1
      })

      const sensorBars = Object.values(groupedSensors)
        .map((item) => {
          const avgTemp = item.temp / item.count
          return {
            label: item.sensorCode,
            desc: `${item.sensorName} · ${formatNumber(avgTemp, '℃')}`,
            valueText: formatNumber(avgTemp, '℃'),
            percent: Math.min(100, Math.round((avgTemp / 40) * 100)),
          }
        })
        .sort((left, right) => left.label.localeCompare(right.label))

      const alarmCounter = {}
      alarms.forEach((alarm) => {
        alarmCounter[alarm.alarmType] = (alarmCounter[alarm.alarmType] || 0) + 1
      })
      const totalAlarm = alarms.length
      const alarmBars = Object.keys(alarmCounter).map((key) => ({
        label: key,
        valueText: `${alarmCounter[key]} 条`,
        percent: safePercent(alarmCounter[key], totalAlarm),
      }))

      const sourceCounter = {}
      historyRows.forEach((row) => {
        const fileName = row.sourceFile || '未记录'
        sourceCounter[fileName] = (sourceCounter[fileName] || 0) + 1
      })
      const topSource = Object.entries(sourceCounter)
        .sort((left, right) => right[1] - left[1])
        .slice(0, 5)
      const maxSource = topSource[0]?.[1] || 0
      const sourceBars = topSource.map(([label, value]) => ({
        label,
        valueText: `${value} 条`,
        percent: safePercent(value, maxSource),
      }))

      this.setData({
        summaryCards,
        latestCollectTimeText: formatDateTime(summary.latestCollectTime),
        ruleForm: {
          tempMin: String(rule.tempMin),
          tempMax: String(rule.tempMax),
          humidityMin: String(rule.humidityMin),
          humidityMax: String(rule.humidityMax),
          enabled: !!rule.enabled,
        },
        avgTempText: historyRows.length ? formatNumber(totalTemp / historyRows.length, '℃') : '--',
        avgHumidityText: historyRows.length ? formatNumber(totalHumidity / historyRows.length, '%') : '--',
        sensorBars,
        alarmBars,
        sourceBars,
      })

      if (showToast) {
        wx.showToast({ title: '控制台已刷新', icon: 'success' })
      }
    } catch (error) {
      wx.showToast({ title: error.message || '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  chooseAndUploadFile() {
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['csv', 'xlsx', 'xls'],
      success: async ({ tempFiles }) => {
        const file = tempFiles?.[0]
        if (!file) {
          return
        }
        this.setData({ uploading: true })
        try {
          const result = await uploadFile(file.path, file.name)
          this.setData({ lastImport: result })
          wx.showToast({ title: '导入成功', icon: 'success' })
          await this.loadPageData()
        } catch (error) {
          wx.showToast({ title: error.message || '导入失败', icon: 'none' })
        } finally {
          this.setData({ uploading: false })
        }
      },
    })
  },

  bindInput(event) {
    const field = event.currentTarget.dataset.field
    const { value } = event.detail
    this.setData({
      [`ruleForm.${field}`]: value,
    })
  },

  toggleRule(event) {
    this.setData({
      'ruleForm.enabled': event.detail.value,
    })
  },

  async saveRule() {
    const ruleForm = this.data.ruleForm
    const payload = {
      tempMin: Number(ruleForm.tempMin),
      tempMax: Number(ruleForm.tempMax),
      humidityMin: Number(ruleForm.humidityMin),
      humidityMax: Number(ruleForm.humidityMax),
      enabled: !!ruleForm.enabled,
    }

    if (Object.values(payload).some((item) => typeof item === 'number' && Number.isNaN(item))) {
      wx.showToast({ title: '请填写完整阈值', icon: 'none' })
      return
    }
    if (payload.tempMin > payload.tempMax) {
      wx.showToast({ title: '温度下限不能大于上限', icon: 'none' })
      return
    }
    if (payload.humidityMin > payload.humidityMax) {
      wx.showToast({ title: '湿度下限不能大于上限', icon: 'none' })
      return
    }

    this.setData({ saving: true })
    try {
      await updateAlarmRule(payload)
      wx.showToast({ title: '阈值已保存', icon: 'success' })
      await this.loadPageData()
    } catch (error) {
      wx.showToast({ title: error.message || '保存失败', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  },
})
