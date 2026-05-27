const { API_BASE_URL, API_BASE_CANDIDATES } = require('./config')

let resolvedBaseUrl = ''

function getBaseCandidates() {
  const candidates = []
  if (resolvedBaseUrl) {
    candidates.push(resolvedBaseUrl)
  }
  API_BASE_CANDIDATES.forEach((item) => {
    if (item && !candidates.includes(item)) {
      candidates.push(item)
    }
  })
  if (API_BASE_URL && !candidates.includes(API_BASE_URL)) {
    candidates.push(API_BASE_URL)
  }
  const saved = wx.getStorageSync('factoryMonitorApiBase')
  if (saved && !candidates.includes(saved)) {
    candidates.push(saved)
  }
  return candidates
}

function rememberBaseUrl(baseUrl) {
  resolvedBaseUrl = baseUrl
  wx.setStorageSync('factoryMonitorApiBase', baseUrl)
}

function rawRequest(baseUrl, path, options = {}) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${baseUrl}${path}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'content-type': 'application/json',
        ...(options.header || {}),
      },
      timeout: options.timeout || 5000,
      success: ({ statusCode, data }) => {
        if (statusCode >= 200 && statusCode < 300) {
          rememberBaseUrl(baseUrl)
          resolve(data)
          return
        }
        reject(new Error(data?.message || `请求失败: ${statusCode}`))
      },
      fail: (error) => {
        reject(new Error(error.errMsg || `网络请求失败: ${path}`))
      },
    })
  })
}

function request(path, options = {}) {
  const candidates = getBaseCandidates()
  let lastError
  return candidates
    .reduce((promise, baseUrl) => {
      return promise.catch((error) => {
        lastError = error
        return rawRequest(baseUrl, path, options)
      })
    }, Promise.reject(new Error('尚未开始请求')))
    .catch(() => {
      throw lastError || new Error(`网络请求失败: ${path}`)
    })
}

function uploadFile(filePath, name) {
  const candidates = getBaseCandidates()
  let lastError
  return candidates
    .reduce((promise, baseUrl) => {
      return promise.catch((error) => {
        lastError = error
        return new Promise((resolve, reject) => {
          wx.uploadFile({
            url: `${baseUrl}/api/excel/import`,
            filePath,
            name: 'file',
            formData: {},
            timeout: 8000,
            success: ({ statusCode, data }) => {
              let parsed
              try {
                parsed = JSON.parse(data)
              } catch {
                parsed = null
              }
              if (statusCode >= 200 && statusCode < 300 && parsed) {
                rememberBaseUrl(baseUrl)
                resolve(parsed)
                return
              }
              reject(new Error(parsed?.message || `${name || '文件'} 上传失败`))
            },
            fail: (uploadError) => {
              reject(new Error(uploadError.errMsg || '文件上传失败'))
            },
          })
        })
      })
    }, Promise.reject(new Error('尚未开始上传')))
    .catch(() => {
      throw lastError || new Error(`${name || '文件'} 上传失败`)
    })
}

function buildQuery(params) {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')
  return query ? `?${query}` : ''
}

function fetchSummary() {
  return request('/api/dashboard/summary')
}

function fetchLatestSensors() {
  return request('/api/sensors/latest')
}

function fetchAlarmRule() {
  return request('/api/alarm-rule')
}

function updateAlarmRule(payload) {
  return request('/api/alarm-rule', {
    method: 'PUT',
    data: payload,
  })
}

function fetchHistory(filters = {}) {
  return request(`/api/sensors/history${buildQuery(filters)}`)
}

function fetchAlarms(filters = {}) {
  return request(`/api/alarms${buildQuery(filters)}`)
}

module.exports = {
  request,
  uploadFile,
  fetchSummary,
  fetchLatestSensors,
  fetchAlarmRule,
  updateAlarmRule,
  fetchHistory,
  fetchAlarms,
}
