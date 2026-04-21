/**
 * 安全存储工具
 * 提供敏感信息的加密存储和解密读取功能
 */

import CryptoJS from 'crypto-js'

class SecureStorage {
  constructor() {
    // 生成或获取设备唯一标识作为加密密钥的一部分
    this.deviceId = this.getDeviceId()
    this.baseKey = 'uniapp_secure_'
  }

  /**
   * 获取设备唯一标识
   */
  getDeviceId() {
    let deviceId = uni.getStorageSync('device_id')
    if (!deviceId) {
      // 生成设备ID
      deviceId = this.generateDeviceId()
      uni.setStorageSync('device_id', deviceId)
    }
    return deviceId
  }

  /**
   * 生成设备ID
   */
  generateDeviceId() {
    const timestamp = Date.now().toString()
    const random = Math.random().toString(36).substring(2)
    return CryptoJS.MD5(timestamp + random).toString()
  }

  /**
   * 生成加密密钥
   * @param {string} key 存储键名
   */
  generateEncryptionKey(key) {
    return CryptoJS.MD5(this.deviceId + key + 'uniapp_salt').toString()
  }

  /**
   * 加密数据
   * @param {string} data 要加密的数据
   * @param {string} key 存储键名
   */
  encrypt(data, key) {
    try {
      const encryptionKey = this.generateEncryptionKey(key)
      const encrypted = CryptoJS.AES.encrypt(JSON.stringify(data), encryptionKey).toString()
      return encrypted
    } catch (error) {
      console.error('数据加密失败:', error)
      return null
    }
  }

  /**
   * 解密数据
   * @param {string} encryptedData 加密的数据
   * @param {string} key 存储键名
   */
  decrypt(encryptedData, key) {
    try {
      const encryptionKey = this.generateEncryptionKey(key)
      const decrypted = CryptoJS.AES.decrypt(encryptedData, encryptionKey)
      const data = decrypted.toString(CryptoJS.enc.Utf8)
      return JSON.parse(data)
    } catch (error) {
      console.error('数据解密失败:', error)
      return null
    }
  }

  /**
   * 安全存储数据
   * @param {string} key 存储键名
   * @param {any} data 要存储的数据
   * @param {boolean} encrypt 是否加密存储
   */
  setItem(key, data, encrypt = true) {
    try {
      const storageKey = this.baseKey + key
      
      if (encrypt) {
        const encryptedData = this.encrypt(data, key)
        if (encryptedData) {
          uni.setStorageSync(storageKey, encryptedData)
          // 标记为加密数据
          uni.setStorageSync(storageKey + '_encrypted', true)
        } else {
          throw new Error('数据加密失败')
        }
      } else {
        uni.setStorageSync(storageKey, data)
        uni.removeStorageSync(storageKey + '_encrypted')
      }
      
      return true
    } catch (error) {
      console.error('安全存储失败:', error)
      return false
    }
  }

  /**
   * 安全读取数据
   * @param {string} key 存储键名
   * @param {any} defaultValue 默认值
   */
  getItem(key, defaultValue = null) {
    try {
      const storageKey = this.baseKey + key
      const isEncrypted = uni.getStorageSync(storageKey + '_encrypted')
      
      if (isEncrypted) {
        const encryptedData = uni.getStorageSync(storageKey)
        if (encryptedData) {
          const decryptedData = this.decrypt(encryptedData, key)
          return decryptedData !== null ? decryptedData : defaultValue
        }
      } else {
        return uni.getStorageSync(storageKey) || defaultValue
      }
      
      return defaultValue
    } catch (error) {
      console.error('安全读取失败:', error)
      return defaultValue
    }
  }

  /**
   * 删除安全存储的数据
   * @param {string} key 存储键名
   */
  removeItem(key) {
    try {
      const storageKey = this.baseKey + key
      uni.removeStorageSync(storageKey)
      uni.removeStorageSync(storageKey + '_encrypted')
      return true
    } catch (error) {
      console.error('安全删除失败:', error)
      return false
    }
  }

  /**
   * 清空所有安全存储的数据
   */
  clear() {
    try {
      const storageInfo = uni.getStorageInfoSync()
      storageInfo.keys.forEach(key => {
        if (key.startsWith(this.baseKey)) {
          uni.removeStorageSync(key)
        }
      })
      return true
    } catch (error) {
      console.error('清空安全存储失败:', error)
      return false
    }
  }

  /**
   * 检查数据是否存在
   * @param {string} key 存储键名
   */
  hasItem(key) {
    const storageKey = this.baseKey + key
    return uni.getStorageSync(storageKey) !== ''
  }

  /**
   * 获取所有安全存储的键名
   */
  getAllKeys() {
    try {
      const storageInfo = uni.getStorageInfoSync()
      return storageInfo.keys
        .filter(key => key.startsWith(this.baseKey))
        .map(key => key.replace(this.baseKey, ''))
    } catch (error) {
      console.error('获取存储键名失败:', error)
      return []
    }
  }

  /**
   * 存储用户敏感信息
   * @param {Object} userInfo 用户信息
   */
  setUserInfo(userInfo) {
    return this.setItem('userInfo', userInfo, true)
  }

  /**
   * 获取用户敏感信息
   */
  getUserInfo() {
    return this.getItem('userInfo', {})
  }

  /**
   * 存储访问令牌
   * @param {string} token 访问令牌
   */
  setToken(token) {
    return this.setItem('uniapp_token', token, true)
  }

  /**
   * 获取访问令牌
   */
  getToken() {
    return this.getItem('uniapp_token', '')
  }

  /**
   * 存储用户密码（加密）
   * @param {string} password 密码
   */
  setPassword(password) {
    return this.setItem('password', password, true)
  }

  /**
   * 获取用户密码
   */
  getPassword() {
    return this.getItem('password', '')
  }

  /**
   * 存储支付信息
   * @param {Object} paymentInfo 支付信息
   */
  setPaymentInfo(paymentInfo) {
    return this.setItem('paymentInfo', paymentInfo, true)
  }

  /**
   * 获取支付信息
   */
  getPaymentInfo() {
    return this.getItem('paymentInfo', {})
  }

  /**
   * 清除用户敏感信息
   */
  clearUserData() {
    const keys = ['userInfo', 'uniapp_token', 'password', 'paymentInfo']
    keys.forEach(key => this.removeItem(key))
  }
}

// 创建单例实例
const secureStorage = new SecureStorage()

export default secureStorage
