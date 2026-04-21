/**
 * 缓存管理工具
 * 提供内存缓存和本地存储缓存功能
 */

class CacheManager {
  constructor() {
    this.memoryCache = new Map()
    this.maxMemorySize = 100 // 最大内存缓存数量
    this.defaultTTL = 5 * 60 * 1000 // 默认5分钟过期
  }

  /**
   * 生成缓存键
   * @param {string} url 请求URL
   * @param {Object} params 请求参数
   * @param {string} method 请求方法
   */
  generateCacheKey(url, params = {}, method = 'GET') {
    const sortedParams = Object.keys(params)
      .sort()
      .reduce((result, key) => {
        result[key] = params[key]
        return result
      }, {})
    
    return `${method}:${url}:${JSON.stringify(sortedParams)}`
  }

  /**
   * 设置缓存
   * @param {string} key 缓存键
   * @param {any} data 缓存数据
   * @param {number} ttl 过期时间（毫秒）
   */
  set(key, data, ttl = this.defaultTTL) {
    const cacheItem = {
      data,
      timestamp: Date.now(),
      ttl
    }

    // 内存缓存
    this.memoryCache.set(key, cacheItem)
    
    // 如果内存缓存超过限制，删除最旧的
    if (this.memoryCache.size > this.maxMemorySize) {
      const firstKey = this.memoryCache.keys().next().value
      this.memoryCache.delete(firstKey)
    }

    // 本地存储缓存（仅对重要数据）
    if (this.shouldPersist(key)) {
      try {
        uni.setStorageSync(`cache_${key}`, cacheItem)
      } catch (error) {
        console.warn('缓存存储失败:', error)
      }
    }
  }

  /**
   * 获取缓存
   * @param {string} key 缓存键
   */
  get(key) {
    // 先检查内存缓存
    let cacheItem = this.memoryCache.get(key)
    
    if (!cacheItem) {
      // 检查本地存储缓存
      try {
        cacheItem = uni.getStorageSync(`cache_${key}`)
        if (cacheItem) {
          // 恢复到内存缓存
          this.memoryCache.set(key, cacheItem)
        }
      } catch (error) {
        console.warn('缓存读取失败:', error)
      }
    }

    if (!cacheItem) {
      return null
    }

    // 检查是否过期
    if (Date.now() - cacheItem.timestamp > cacheItem.ttl) {
      this.delete(key)
      return null
    }

    return cacheItem.data
  }

  /**
   * 删除缓存
   * @param {string} key 缓存键
   */
  delete(key) {
    this.memoryCache.delete(key)
    try {
      uni.removeStorageSync(`cache_${key}`)
    } catch (error) {
      console.warn('缓存删除失败:', error)
    }
  }

  /**
   * 清空所有缓存
   */
  clear() {
    this.memoryCache.clear()
    
    // 清空本地存储中的缓存
    try {
      const storageInfo = uni.getStorageInfoSync()
      storageInfo.keys.forEach(key => {
        if (key.startsWith('cache_')) {
          uni.removeStorageSync(key)
        }
      })
    } catch (error) {
      console.warn('清空缓存失败:', error)
    }
  }

  /**
   * 判断是否应该持久化存储
   * @param {string} key 缓存键
   */
  shouldPersist(key) {
    // 只对用户信息、配置等重要数据进行持久化
    const persistKeys = [
      'userInfo',
      'config',
      'category',
      'settings'
    ]
    
    return persistKeys.some(persistKey => key.includes(persistKey))
  }

  /**
   * 获取缓存统计信息
   */
  getStats() {
    return {
      memorySize: this.memoryCache.size,
      maxMemorySize: this.maxMemorySize,
      keys: Array.from(this.memoryCache.keys())
    }
  }

  /**
   * 清理过期缓存
   */
  cleanExpired() {
    const now = Date.now()
    const expiredKeys = []
    
    this.memoryCache.forEach((item, key) => {
      if (now - item.timestamp > item.ttl) {
        expiredKeys.push(key)
      }
    })
    
    expiredKeys.forEach(key => {
      this.delete(key)
    })
    
    return expiredKeys.length
  }
}

// 创建单例实例
const cacheManager = new CacheManager()

// 定期清理过期缓存（每10分钟）
setInterval(() => {
  const cleaned = cacheManager.cleanExpired()
  if (cleaned > 0) {
    console.log(`清理了 ${cleaned} 个过期缓存`)
  }
}, 10 * 60 * 1000)

export default cacheManager
