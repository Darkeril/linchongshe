import { baseUrl } from '@/.env.js'

let mediaRecorder = null
let chunks = []
let stream = null
let startTimeMs = 0

function getOrigin() {
  try {
    return (typeof window !== 'undefined' && window.location && window.location.origin) ? window.location.origin : ''
  } catch (e) {
    return ''
  }
}

function checkIsEnable() {
  const origin = getOrigin()
  // H5 录音要求 https（localhost/127.0.0.1 允许）
  if (origin && origin.indexOf('https') === -1 && origin.indexOf('localhost') === -1 && origin.indexOf('127.0.0.1') === -1) {
    uni.showToast({ title: '请在https环境中使用录音功能', icon: 'none' })
    return false
  }
  if (!navigator || !navigator.mediaDevices || typeof navigator.mediaDevices.getUserMedia !== 'function' || typeof window === 'undefined' || !window.MediaRecorder) {
    uni.showToast({ title: '当前浏览器不支持录音', icon: 'none' })
    return false
  }
  return true
}

function start() {
  chunks = []
  startTimeMs = Date.now()
  return navigator.mediaDevices.getUserMedia({ audio: true }).then((audioStream) => {
    stream = audioStream
    mediaRecorder = new MediaRecorder(stream)
    mediaRecorder.ondataavailable = (e) => {
      if (e && e.data) chunks.push(e.data)
    }
    mediaRecorder.start()
  })
}

function close() {
  try {
    if (stream && typeof stream.getTracks === 'function') {
      stream.getTracks().forEach((track) => track.stop())
    }
  } catch (e) {}
  try {
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
      mediaRecorder.stop()
    }
  } catch (e) {}
}

function upload() {
  return new Promise((resolve, reject) => {
    if (!mediaRecorder) {
      reject('录音未初始化')
      return
    }
    mediaRecorder.onstop = () => {
      try {
        if (!chunks.length || !chunks[0] || !chunks[0].size) {
          chunks = []
          reject('录音为空')
          return
        }
        const durationSec = Math.round((Date.now() - startTimeMs) / 1000)
        const blob = new Blob(chunks, { type: 'audio/mpeg' })
        const name = `${Date.now()}.mp3`
        const file = new File([blob], name, { type: 'audio/mpeg' })
        chunks = []
        uni.uploadFile({
          url: baseUrl + '/web/app/third/uploadAudio',
          header: {
            accessToken: uni.getStorageSync('uniapp_token') || '',
            token: uni.getStorageSync('uniapp_token') || ''
          },
          file: file,
          name: 'file',
          // 兼容部分 HBuilderX 版本：filePath 为空会直接 fail
          filePath: 'x',
          success: (res) => {
            let data = {}
            try {
              data = JSON.parse(res.data)
            } catch (e) {
              reject('语音上传失败')
              return
            }
            const ok = data && data.code === 200
            if (!ok) {
              reject((data && (data.msg || data.message)) || '语音上传失败')
              return
            }
            resolve({
              duration: Math.max(durationSec, 1),
              url: data.data
            })
          },
          fail: (e) => reject(e)
        })
      } catch (e) {
        reject(e)
      }
    }
  })
}

export { checkIsEnable, start, close, upload }

