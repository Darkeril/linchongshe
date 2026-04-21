/**
 * 微信小程序性能优化工具类
 * 解决H5流畅但小程序卡顿的问题
 */

class PerformanceOptimizer {
  constructor() {
    this.updateQueue = new Map() // 更新队列
    this.updateTimer = null // 更新定时器
    this.batchSize = 5 // 批处理大小
    this.updateDelay = 16 // 更新延迟（约60fps）
  }

  /**
   * 批量setData更新
   * @param {Object} context - 页面上下文
   * @param {Object} data - 要更新的数据
   * @param {Function} callback - 回调函数
   */
  batchSetData(context, data, callback) {
    const pageId = this.getPageId(context)
    
    // 合并到更新队列
    if (!this.updateQueue.has(pageId)) {
      this.updateQueue.set(pageId, {})
    }
    
    const queueData = this.updateQueue.get(pageId)
    Object.assign(queueData, data)
    
    // 清除之前的定时器
    if (this.updateTimer) {
      clearTimeout(this.updateTimer)
    }
    
    // 延迟执行更新
    this.updateTimer = setTimeout(() => {
      this.flushUpdates(callback)
    }, this.updateDelay)
  }

  /**
   * 执行批量更新
   */
  flushUpdates(callback) {
    this.updateQueue.forEach((data, pageId) => {
      const context = this.getContextByPageId(pageId)
      if (context) {
        // 一次性更新所有数据
        Object.keys(data).forEach(key => {
          context[key] = data[key]
        })
        
        // 触发setData
        if (context.$set) {
          Object.keys(data).forEach(key => {
            context.$set(context, key, data[key])
          })
        }
      }
    })
    
    // 清空队列
    this.updateQueue.clear()
    this.updateTimer = null
    
    // 执行回调
    if (callback) {
      callback()
    }
  }

  /**
   * 分批处理数组数据
   * @param {Array} items - 数据数组
   * @param {Function} processor - 处理函数
   * @param {Number} batchSize - 批处理大小
   */
  async batchProcess(items, processor, batchSize = this.batchSize) {
    const results = []
    
    for (let i = 0; i < items.length; i += batchSize) {
      const batch = items.slice(i, i + batchSize)
      const batchResults = await Promise.all(
        batch.map(item => processor(item))
      )
      results.push(...batchResults)
      
      // 让出主线程，避免阻塞
      await this.yieldToMain()
    }
    
    return results
  }

  /**
   * 让出主线程
   */
  yieldToMain() {
    return new Promise(resolve => {
      setTimeout(resolve, 0)
    })
  }

  /**
   * 图片懒加载优化
   * @param {Array} imageUrls - 图片URL数组
   * @param {Number} maxConcurrent - 最大并发数
   */
  async optimizedImageLoad(imageUrls, maxConcurrent = 3) {
    const results = []
    const executing = []
    
    for (const url of imageUrls) {
      const promise = this.loadImage(url)
      results.push(promise)
      
      if (imageUrls.length >= maxConcurrent) {
        executing.push(promise)
        
        if (executing.length >= maxConcurrent) {
          await Promise.race(executing)
          executing.splice(executing.findIndex(p => p === promise), 1)
        }
      }
    }
    
    return Promise.all(results)
  }

  /**
   * 加载单张图片
   */
  loadImage(url) {
    return new Promise((resolve, reject) => {
      uni.getImageInfo({
        src: url,
        success: resolve,
        fail: reject
      })
    })
  }

  /**
   * 内存管理
   */
  cleanup() {
    if (this.updateTimer) {
      clearTimeout(this.updateTimer)
      this.updateTimer = null
    }
    this.updateQueue.clear()
  }

  /**
   * 获取页面ID
   */
  getPageId(context) {
    return context.$route?.path || context.$options.name || 'default'
  }

  /**
   * 根据页面ID获取上下文
   */
  getContextByPageId(pageId) {
    // 这里需要根据实际情况实现
    return null
  }
}

// 创建全局实例
const performanceOptimizer = new PerformanceOptimizer()

export default performanceOptimizer

/**
 * 使用示例：
 * 
 * // 批量更新数据
 * performanceOptimizer.batchSetData(this, {
 *   'notesList[0].leftList': newLeftList,
 *   'notesList[0].rightList': newRightList
 * })
 * 
 * // 分批处理图片
 * const results = await performanceOptimizer.batchProcess(
 *   imageUrls, 
 *   url => performanceOptimizer.loadImage(url),
 *   3
 * )
 */
