const { fetchLatestSensors, fetchHistory } = require('../../utils/api')
const { formatDateTime, formatNumber, toStartOfDay, toEndOfDay, sensorPickerOptions } = require('../../utils/format')

const DEFAULT_SENSOR_OPTIONS = [{ label: '全部点位', value: '' }]

Page({
  data: {
    loading: false,
    sensorOptions: DEFAULT_SENSOR_OPTIONS,
    sensorIndex: 0,
    startDate: '',
    endDate: '',
    rows: [],
  },

  onShow() {
    this.initialize()
  },

  async initialize() {
    try {
      const sensors = await fetchLatestSensors()
      this.setData({
        sensorOptions: sensorPickerOptions(sensors),
      })
    } catch (error) {
      this.setData({
        sensorOptions: DEFAULT_SENSOR_OPTIONS,
        sensorIndex: 0,
      })
      wx.showToast({ title: error.message || '点位列表加载失败', icon: 'none' })
    }

    await this.loadHistory()
  },

  normalizeSensorIndex() {
    const maxIndex = Math.max(this.data.sensorOptions.length - 1, 0)
    if (this.data.sensorIndex > maxIndex) {
      this.setData({ sensorIndex: 0 })
    }
  },

  bindSensorChange(event) {
    this.setData({ sensorIndex: Number(event.detail.value) })
  },

  bindStartDate(event) {
    this.setData({ startDate: event.detail.value })
  },

  bindEndDate(event) {
    this.setData({ endDate: event.detail.value })
  },

  async loadHistory() {
    this.normalizeSensorIndex()
    this.setData({ loading: true })
    try {
      const selectedSensor = this.data.sensorOptions[this.data.sensorIndex]
      const rows = await fetchHistory({
        sensorCode: selectedSensor?.value || '',
        startTime: toStartOfDay(this.data.startDate),
        endTime: toEndOfDay(this.data.endDate),
      })
      this.setData({
        rows: rows.map((row) => ({
          ...row,
          temperatureText: formatNumber(row.temperature, '℃'),
          humidityText: formatNumber(row.humidity, '%'),
          collectTimeText: formatDateTime(row.collectTime),
        })),
      })
    } catch (error) {
      wx.showToast({ title: error.message || '查询失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
})
