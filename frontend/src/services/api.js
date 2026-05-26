const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      Accept: 'application/json',
      ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
      ...options.headers,
    },
    ...options,
  })

  if (!response.ok) {
    let message = `请求失败: ${response.status}`
    try {
      const body = await response.json()
      if (body?.message) {
        message = body.message
      }
    } catch {
      // ignore invalid JSON response
    }
    throw new Error(message)
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}

function buildQuery(params) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })
  return search.toString() ? `?${search.toString()}` : ''
}

export function fetchSummary() {
  return request('/api/dashboard/summary')
}

export function fetchLatestSensors() {
  return request('/api/sensors/latest')
}

export function fetchAlarmRule() {
  return request('/api/alarm-rule')
}

export function updateAlarmRule(payload) {
  return request('/api/alarm-rule', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function fetchSensorHistory(filters) {
  return request(`/api/sensors/history${buildQuery(filters)}`)
}

export function fetchAlarms(filters) {
  return request(`/api/alarms${buildQuery(filters)}`)
}

export function importExcel(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request('/api/excel/import', {
    method: 'POST',
    body: formData,
  })
}
