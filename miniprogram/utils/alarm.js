let audioContext
let alarmFilePath

function createAlarmWavBase64() {
  const sampleRate = 22050
  const durationSeconds = 0.32
  const sampleCount = Math.floor(sampleRate * durationSeconds)
  const dataSize = sampleCount * 2
  const buffer = new ArrayBuffer(44 + dataSize)
  const view = new DataView(buffer)

  function writeString(offset, text) {
    for (let index = 0; index < text.length; index += 1) {
      view.setUint8(offset + index, text.charCodeAt(index))
    }
  }

  writeString(0, 'RIFF')
  view.setUint32(4, 36 + dataSize, true)
  writeString(8, 'WAVE')
  writeString(12, 'fmt ')
  view.setUint32(16, 16, true)
  view.setUint16(20, 1, true)
  view.setUint16(22, 1, true)
  view.setUint32(24, sampleRate, true)
  view.setUint32(28, sampleRate * 2, true)
  view.setUint16(32, 2, true)
  view.setUint16(34, 16, true)
  writeString(36, 'data')
  view.setUint32(40, dataSize, true)

  const frequency = 698
  for (let i = 0; i < sampleCount; i += 1) {
    const t = i / sampleRate
    const envelope = Math.exp(-7 * t)
    const sample = Math.sin(2 * Math.PI * frequency * t) * envelope
    view.setInt16(44 + i * 2, sample * 32767, true)
  }

  const bytes = new Uint8Array(buffer)
  let binary = ''
  bytes.forEach((item) => {
    binary += String.fromCharCode(item)
  })
  return wx.arrayBufferToBase64(buffer)
}

function ensureAlarmFile() {
  if (alarmFilePath) {
    return Promise.resolve(alarmFilePath)
  }

  return new Promise((resolve, reject) => {
    const filePath = `${wx.env.USER_DATA_PATH}/alarm-tone.wav`
    wx.getFileSystemManager().writeFile({
      filePath,
      data: createAlarmWavBase64(),
      encoding: 'base64',
      success() {
        alarmFilePath = filePath
        resolve(filePath)
      },
      fail(error) {
        reject(error)
      },
    })
  })
}

async function playAlarmTone() {
  try {
    const filePath = await ensureAlarmFile()
    if (!audioContext) {
      audioContext = wx.createInnerAudioContext()
      audioContext.obeyMuteSwitch = false
    }
    audioContext.stop()
    audioContext.src = filePath
    audioContext.play()
  } catch {
    wx.vibrateShort({ type: 'medium' })
  }
}

module.exports = {
  playAlarmTone,
}
