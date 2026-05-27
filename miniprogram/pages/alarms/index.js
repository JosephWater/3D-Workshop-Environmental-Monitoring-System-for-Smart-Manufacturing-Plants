const { fetchLatestSensors, fetchAlarms } = require('../../utils/api')
const { formatDateTime, toStartOfDay, toEndOfDay, sensorPickerOptions } = require('../../utils/format')

const ALARM_TYPES = [
  { label: '全部类型', value: '' },
  { label: 'TEMP_HIGH', value: 'TEMP_HIGH' },
  { label: 'TEMP_LOW', value: 'TEMP_LOW' },
  { label: 'HUM_HIGH', value: 'HUM_HIGH' },
  { label: 'HUM_LOW', value: 'HUM_LOW' },
]

const ALARM_STATUS = [
  { label: '全部状态', value: '' },
  { label: 'ACTIVE', value: 'ACTIVE' },
  { label: 'RESOLVED', value: 'RESOLVED' },
]

Page({
  data: {
    loading: false,
    sensorOptions: [{ label: '全部点位', value: '' }],
    sensorIndex: 0,
    typeIndex: 0,
    statusIndex: 0,
    startDate: '',
    endDate: '',
    typeOptions: ALARM_TYPES,
    statusOptions: ALARM_STATUS,
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
      await this.loadAlarms()
    } catch (error) {
      wx.showToast({ title: error.message || '初始化失败', icon: 'none' })
    }
  },

  bindPicker(event) {
    const field = event.currentTarget.dataset.field
    this.setData({ [field]: Number(event.detail.value) })
  },

  bindDate(event) {
    const field = event.currentTarget.dataset.field
    this.setData({ [field]: event.detail.value })
  },

  async loadAlarms() {
    this.setData({ loading: true })
    try {
      const rows = await fetchAlarms({
        sensorCode: this.data.sensorOptions[this.data.sensorIndex]?.value || '',
        alarmType: this.data.typeOptions[this.data.typeIndex]?.value || '',
        status: this.data.statusOptions[this.data.statusIndex]?.value || '',
        startTime: toStartOfDay(this.data.startDate),
        endTime: toEndOfDay(this.data.endDate),
      })
      this.setData({
        rows: rows.map((row) => ({
          ...row,
          alarmTimeText: formatDateTime(row.alarmTime),
          resolvedAtText: formatDateTime(row.resolvedAt),
        })),
      })
    } catch (error) {
      wx.showToast({ title: error.message || '查询失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
})
