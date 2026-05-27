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
  const date = new Date(localDateTime)
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mi = String(date.getMinutes()).padStart(2, '0')
  const ss = String(date.getSeconds()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}T${hh}:${mi}:${ss}`
}
