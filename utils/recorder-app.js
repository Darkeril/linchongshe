import { baseUrl } from '@/.env.js'

const rc = uni.getRecorderManager()
let startTime = 0

function checkIsEnable() {
  return true
}

function start() {
  return new Promise((resolve, reject) => {
    try {
      rc.onStart(() => {
        startTime = Date.now()
        resolve()
      })
      rc.onError((e) => {
        reject(e)
      })
      rc.start({ format: 'mp3' })
    } catch (e) {
      reject(e)
    }
  })
}

function close() {
  try {
    rc.stop()
  } catch (e) {}
}

function upload() {
  return new Promise((resolve, reject) => {
    rc.onStop((res) => {
      const filePath = res && res.tempFilePath
      if (!filePath) {
        reject('录音文件不存在')
        return
      }
      uni.uploadFile({
        url: baseUrl + '/web/app/third/uploadAudio',
        filePath,
        name: 'file',
        header: {
          accessToken: uni.getStorageSync('uniapp_token') || '',
          token: uni.getStorageSync('uniapp_token') || ''
        },
        success: (uploadRes) => {
          let data = {}
          try {
            data = JSON.parse(uploadRes.data)
          } catch (e) {
            reject('语音上传失败')
            return
          }
          const ok = data && data.code === 200
          if (!ok) {
            reject((data && (data.msg || data.message)) || '语音上传失败')
            return
          }
          const duration = Math.max(Math.round((Date.now() - startTime) / 1000), 1)
          resolve({ duration, url: data.data })
        },
        fail: (e) => reject(e)
      })
    })
  })
}

export { checkIsEnable, start, close, upload }

