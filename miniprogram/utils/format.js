function formatDateTime(value) {
  if (!value) {
    return '暂无数据'
  }
  const date = new Date(value)
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mi = String(date.getMinutes()).padStart(2, '0')
  const ss = String(date.getSeconds()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`
}

function formatNumber(value, unit = '') {
  if (value === null || value === undefined || value === '') {
    return '--'
  }
  return `${Number(value).toFixed(1)}${unit}`
}

function toStartOfDay(dateText) {
  return dateText ? `${dateText}T00:00:00` : ''
}

function toEndOfDay(dateText) {
  return dateText ? `${dateText}T23:59:59` : ''
}

function sensorPickerOptions(sensors = []) {
  return [{ label: '全部点位', value: '' }].concat(
    sensors.map((sensor) => ({
      label: `${sensor.sensorCode} ${sensor.sensorName}`,
      value: sensor.sensorCode,
    })),
  )
}

module.exports = {
  formatDateTime,
  formatNumber,
  toStartOfDay,
  toEndOfDay,
  sensorPickerOptions,
}
