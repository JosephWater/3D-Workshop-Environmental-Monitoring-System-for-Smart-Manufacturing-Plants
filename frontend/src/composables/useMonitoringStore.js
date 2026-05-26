import { computed, reactive } from 'vue'
import {
  fetchAlarmRule,
  fetchAlarms,
  fetchLatestSensors,
  fetchSensorHistory,
  fetchSummary,
  importExcel,
  updateAlarmRule,
} from '../services/api'
import { toIso } from '../utils/format'

const state = reactive({
  summary: {
    totalSensors: 0,
    totalDataRows: 0,
    activeAlarmCount: 0,
    resolvedAlarmCount: 0,
    latestCollectTime: null,
  },
  sensors: [],
  historyRows: [],
  alarms: [],
  consoleHistoryRows: [],
  consoleAlarmRows: [],
  selectedSensorCode: '',
  lastImport: null,
  message: {
    type: 'info',
    text: '系统已就绪，等待导入环境监测数据。',
  },
  loading: {
    overview: false,
    history: false,
    alarms: false,
    upload: false,
    rule: false,
    console: false,
  },
  ruleForm: {
    tempMin: '18',
    tempMax: '30',
    humidityMin: '40',
    humidityMax: '70',
    enabled: true,
  },
  historyFilters: {
    sensorCode: '',
    startTime: '',
    endTime: '',
  },
  alarmFilters: {
    sensorCode: '',
    alarmType: '',
    status: '',
    startTime: '',
    endTime: '',
  },
})

let refreshTimer
let audioContext
let lastAlarmCount = 0
let initialized = false
let consoleLoaded = false

function setMessage(type, text) {
  state.message.type = type
  state.message.text = text
}

function ensureAudioContext() {
  if (!audioContext && typeof window !== 'undefined' && window.AudioContext) {
    audioContext = new window.AudioContext()
  }
}

function playAlarmTone() {
  ensureAudioContext()
  if (!audioContext) {
    return
  }
  if (audioContext.state === 'suspended') {
    audioContext.resume()
  }

  const oscillator = audioContext.createOscillator()
  const gainNode = audioContext.createGain()
  oscillator.type = 'square'
  oscillator.frequency.setValueAtTime(698, audioContext.currentTime)
  gainNode.gain.setValueAtTime(0.0001, audioContext.currentTime)
  gainNode.gain.exponentialRampToValueAtTime(0.05, audioContext.currentTime + 0.04)
  gainNode.gain.exponentialRampToValueAtTime(0.0001, audioContext.currentTime + 0.34)
  oscillator.connect(gainNode)
  gainNode.connect(audioContext.destination)
  oscillator.start()
  oscillator.stop(audioContext.currentTime + 0.36)
}

async function loadOverview(showStatus = false) {
  state.loading.overview = true
  try {
    const [summaryData, latestSensors, rule] = await Promise.all([
      fetchSummary(),
      fetchLatestSensors(),
      fetchAlarmRule(),
    ])

    state.summary = summaryData
    state.sensors = latestSensors

    if (!state.selectedSensorCode && latestSensors.length > 0) {
      state.selectedSensorCode = latestSensors[0].sensorCode
    }
    if (state.selectedSensorCode && !latestSensors.some((item) => item.sensorCode === state.selectedSensorCode)) {
      state.selectedSensorCode = latestSensors[0]?.sensorCode ?? ''
    }

    state.ruleForm.tempMin = String(rule.tempMin)
    state.ruleForm.tempMax = String(rule.tempMax)
    state.ruleForm.humidityMin = String(rule.humidityMin)
    state.ruleForm.humidityMax = String(rule.humidityMax)
    state.ruleForm.enabled = Boolean(rule.enabled)

    if (summaryData.activeAlarmCount > lastAlarmCount) {
      playAlarmTone()
    }
    lastAlarmCount = summaryData.activeAlarmCount

    if (showStatus) {
      setMessage('success', '总览数据已刷新。')
    }
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    state.loading.overview = false
  }
}

async function loadHistory(showStatus = true) {
  state.loading.history = true
  try {
    state.historyRows = await fetchSensorHistory({
      sensorCode: state.historyFilters.sensorCode,
      startTime: toIso(state.historyFilters.startTime),
      endTime: toIso(state.historyFilters.endTime),
    })
    if (showStatus) {
      setMessage('info', `历史数据查询完成，共返回 ${state.historyRows.length} 条记录。`)
    }
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    state.loading.history = false
  }
}

async function loadAlarms(showStatus = true) {
  state.loading.alarms = true
  try {
    state.alarms = await fetchAlarms({
      sensorCode: state.alarmFilters.sensorCode,
      alarmType: state.alarmFilters.alarmType,
      status: state.alarmFilters.status,
      startTime: toIso(state.alarmFilters.startTime),
      endTime: toIso(state.alarmFilters.endTime),
    })
    if (showStatus) {
      setMessage('info', `告警查询完成，共返回 ${state.alarms.length} 条记录。`)
    }
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    state.loading.alarms = false
  }
}

async function loadConsoleData(showStatus = false) {
  state.loading.console = true
  try {
    const [historyRows, alarmRows] = await Promise.all([fetchSensorHistory({}), fetchAlarms({})])
    state.consoleHistoryRows = historyRows
    state.consoleAlarmRows = alarmRows
    consoleLoaded = true
    if (showStatus) {
      setMessage('success', '控制台数据已刷新。')
    }
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    state.loading.console = false
  }
}

async function ensureConsoleData() {
  if (!consoleLoaded) {
    await loadConsoleData()
  }
}

async function handleImport(file) {
  state.loading.upload = true
  try {
    state.lastImport = await importExcel(file)
    setMessage(
      'success',
      `导入成功，共写入 ${state.lastImport.importedRows} 条数据，新增 ${state.lastImport.newAlarmCount} 条告警。`,
    )
    await Promise.all([loadOverview(), loadHistory(false), loadAlarms(false), loadConsoleData(false)])
  } catch (error) {
    setMessage('error', error.message)
    throw error
  } finally {
    state.loading.upload = false
  }
}

function validateRuleForm() {
  const payload = {
    tempMin: Number(state.ruleForm.tempMin),
    tempMax: Number(state.ruleForm.tempMax),
    humidityMin: Number(state.ruleForm.humidityMin),
    humidityMax: Number(state.ruleForm.humidityMax),
    enabled: Boolean(state.ruleForm.enabled),
  }

  if (Object.values(payload).some((value) => typeof value === 'number' && Number.isNaN(value))) {
    throw new Error('请填写完整的阈值数值。')
  }
  if (payload.tempMin > payload.tempMax) {
    throw new Error('温度下限不能大于温度上限。')
  }
  if (payload.humidityMin > payload.humidityMax) {
    throw new Error('湿度下限不能大于湿度上限。')
  }
  return payload
}

async function saveRule() {
  state.loading.rule = true
  try {
    await updateAlarmRule(validateRuleForm())
    setMessage('success', '告警阈值已更新。')
    await loadOverview()
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    state.loading.rule = false
  }
}

function selectSensor(code) {
  state.selectedSensorCode = code
}

function startPolling() {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
  }
  refreshTimer = window.setInterval(() => {
    loadOverview()
  }, 20000)
}

async function initialize() {
  if (initialized) {
    return
  }
  initialized = true
  ensureAudioContext()
  if (typeof window !== 'undefined') {
    window.addEventListener('pointerdown', ensureAudioContext, { once: true })
  }
  await Promise.all([loadOverview(), loadHistory(false), loadAlarms(false)])
  startPolling()
}

const sensorOptions = computed(() =>
  state.sensors.map((sensor) => ({
    value: sensor.sensorCode,
    label: `${sensor.sensorCode} 路 ${sensor.sensorName}`,
  })),
)

const selectedSensor = computed(
  () => state.sensors.find((sensor) => sensor.sensorCode === state.selectedSensorCode) ?? null,
)

const activeAlarms = computed(() => state.sensors.filter((sensor) => sensor.alarmActive))

const statCards = computed(() => [
  { label: '在线点位', value: state.summary.totalSensors, hint: '车间内传感器在线数量' },
  { label: '数据总量', value: state.summary.totalDataRows, hint: '已导入的温湿度记录总数' },
  { label: '当前告警', value: state.summary.activeAlarmCount, hint: '处于活动状态的告警' },
  { label: '已恢复告警', value: state.summary.resolvedAlarmCount, hint: '已恢复并归档的告警' },
])

export function useMonitoringStore() {
  return {
    state,
    sensorOptions,
    selectedSensor,
    activeAlarms,
    statCards,
    initialize,
    loadOverview,
    loadHistory,
    loadAlarms,
    loadConsoleData,
    ensureConsoleData,
    handleImport,
    saveRule,
    selectSensor,
    setMessage,
  }
}
