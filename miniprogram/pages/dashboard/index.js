const app = getApp()
const { fetchLatestSensors, fetchSummary } = require('../../utils/api')
const { formatDateTime, formatNumber } = require('../../utils/format')
const { playAlarmTone } = require('../../utils/alarm')

const DEFAULT_SENSORS = [
  { sensorCode: 'S1', sensorName: '一区一号传感器', gridPosition: '1-1', x: 12, y: 0, z: 12 },
  { sensorCode: 'S2', sensorName: '一区二号传感器', gridPosition: '1-2', x: 36, y: 0, z: 12 },
  { sensorCode: 'S3', sensorName: '一区三号传感器', gridPosition: '1-3', x: 60, y: 0, z: 12 },
  { sensorCode: 'S4', sensorName: '二区一号传感器', gridPosition: '2-1', x: 12, y: 0, z: 36 },
  { sensorCode: 'S5', sensorName: '二区二号传感器', gridPosition: '2-2', x: 36, y: 0, z: 36 },
  { sensorCode: 'S6', sensorName: '二区三号传感器', gridPosition: '2-3', x: 60, y: 0, z: 36 },
  { sensorCode: 'S7', sensorName: '三区一号传感器', gridPosition: '3-1', x: 12, y: 0, z: 60 },
  { sensorCode: 'S8', sensorName: '三区二号传感器', gridPosition: '3-2', x: 36, y: 0, z: 60 },
  { sensorCode: 'S9', sensorName: '三区三号传感器', gridPosition: '3-3', x: 60, y: 0, z: 60 },
].map((sensor) => ({
  ...sensor,
  temperature: null,
  humidity: null,
  collectTime: null,
  alarmActive: false,
  alarmTypes: [],
  temperatureText: '--',
  humidityText: '--',
  collectTimeText: '等待数据',
}))

Page({
  data: {
    loading: false,
    sensors: DEFAULT_SENSORS,
    selectedSensorCode: 'S5',
    selectedSensor: DEFAULT_SENSORS[4],
    latestCollectTimeText: '暂无数据',
    activeAlarmCount: 0,
  },

  onShow() {
    this.loadOverview()
    this.startPolling()
  },

  onHide() {
    this.stopPolling()
  },

  onUnload() {
    this.stopPolling()
  },

  async loadOverview() {
    this.setData({ loading: true })
    try {
      const [sensors, summary] = await Promise.all([fetchLatestSensors(), fetchSummary()])
      const sensorMap = new Map(sensors.map((sensor) => [sensor.sensorCode, sensor]))
      const sensorViews = DEFAULT_SENSORS.map((sensor) => {
        const latest = sensorMap.get(sensor.sensorCode)
        const merged = latest ? { ...sensor, ...latest } : sensor
        return {
          ...merged,
          alarmTypes: Array.isArray(merged.alarmTypes) ? merged.alarmTypes : [],
          temperatureText: formatNumber(merged.temperature, '℃'),
          humidityText: formatNumber(merged.humidity, '%'),
          collectTimeText: formatDateTime(merged.collectTime),
        }
      })
      if ((app.globalData.lastAlarmCount || 0) < Number(summary.activeAlarmCount || 0)) {
        playAlarmTone()
      }
      app.globalData.lastAlarmCount = Number(summary.activeAlarmCount || 0)

      const selectedSensorCode = this.data.selectedSensorCode || sensorViews[4]?.sensorCode || ''
      const selectedSensor = sensorViews.find((item) => item.sensorCode === selectedSensorCode) || sensorViews[4] || null

      this.setData({
        sensors: sensorViews,
        selectedSensorCode: selectedSensor?.sensorCode || '',
        selectedSensor,
        latestCollectTimeText: formatDateTime(summary.latestCollectTime),
        activeAlarmCount: Number(summary.activeAlarmCount || 0),
      })
    } catch (error) {
      const fallbackSensors = this.data.sensors.length ? this.data.sensors : DEFAULT_SENSORS
      this.setData({
        sensors: fallbackSensors,
        selectedSensorCode: this.data.selectedSensorCode || fallbackSensors[4]?.sensorCode || '',
        selectedSensor:
          fallbackSensors.find((item) => item.sensorCode === this.data.selectedSensorCode) || fallbackSensors[4] || null,
      })
      wx.showToast({ title: error.message || '总览加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  startPolling() {
    this.stopPolling()
    this.pollTimer = setInterval(() => {
      this.loadOverview()
    }, 20000)
  },

  stopPolling() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer)
      this.pollTimer = null
    }
  },

  selectSensor(event) {
    const sensorCode = event.currentTarget.dataset.code
    const selectedSensor = this.data.sensors.find((item) => item.sensorCode === sensorCode) || null
    this.setData({
      selectedSensorCode: sensorCode,
      selectedSensor,
    })
  },
})
