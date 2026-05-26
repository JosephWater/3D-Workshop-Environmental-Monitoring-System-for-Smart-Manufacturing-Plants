export function formatDateTime(value) {
  if (!value) {
    return '暂无数据'
  }
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(new Date(value))
}

export function formatNumber(value, unit = '') {
  if (value === null || value === undefined || value === '') {
    return '--'
  }
  return `${Number(value).toFixed(1)}${unit}`
}

export function toIso(localDateTime) {
  if (!localDateTime) {
    return ''
  }
  return new Date(localDateTime).toISOString()
}
