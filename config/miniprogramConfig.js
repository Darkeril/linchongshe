/**
 * 微信小程序性能优化配置
 * 解决H5流畅但小程序卡顿的问题
 */

export const miniprogramConfig = {
  // 渲染优化
  render: {
    // 减少setData频率
    batchUpdateDelay: 16, // 16ms，约60fps
    maxBatchSize: 10, // 最大批处理数量
    
    // 虚拟列表配置
    virtualList: {
      itemHeight: 200, // 预估项目高度
      bufferSize: 5, // 缓冲区大小
      threshold: 100 // 触发阈值
    },
    
    // 图片懒加载
    lazyLoad: {
      threshold: 200, // 触发距离
      rootMargin: '50px', // 根边距
      placeholder: '/static/placeholder.png'
    }
  },
  
  // 内存管理
  memory: {
    // 最大缓存数量
    maxCacheSize: 100,
    
    // 缓存过期时间
    cacheExpireTime: 30 * 60 * 1000, // 30分钟
    
    // 清理策略
    cleanupStrategy: {
      // 内存使用率超过80%时清理
      memoryThreshold: 0.8,
      
      // 清理比例
      cleanupRatio: 0.3,
      
      // 清理间隔
      cleanupInterval: 5 * 60 * 1000 // 5分钟
    }
  },
  
  // 网络优化
  network: {
    // 请求超时时间
    timeout: 10000, // 10秒
    
    // 重试配置
    retry: {
      maxRetries: 3,
      retryDelay: 1000, // 1秒
      backoffFactor: 2 // 退避因子
    },
    
    // 并发控制
    concurrency: {
      maxConcurrent: 5, // 最大并发请求数
      queueSize: 20 // 队列大小
    }
  },
  
  // 图片优化
  image: {
    // 压缩质量
    quality: {
      avatar: 60,
      cover: 70,
      content: 80,
      thumbnail: 50
    },
    
    // 尺寸限制
    maxSize: {
      avatar: 200,
      cover: 500,
      content: 800
    },
    
    // 格式优化
    format: {
      preferWebp: true,
      fallbackJpg: true
    },
    
    // 预加载配置
    preload: {
      enabled: true,
      maxConcurrent: 3,
      criticalCount: 5
    }
  },
  
  // 动画优化
  animation: {
    // 使用transform代替position
    useTransform: true,
    
    // 启用硬件加速
    enableHardwareAcceleration: true,
    
    // 动画时长
    duration: 300,
    
    // 缓动函数
    easing: 'ease-out'
  },
  
  // 事件优化
  event: {
    // 防抖延迟
    debounceDelay: 200,
    
    // 节流间隔
    throttleInterval: 100,
    
    // 长按延迟
    longPressDelay: 500
  },
  
  // 调试配置
  debug: {
    // 是否启用性能监控
    enablePerformanceMonitor: true,
    
    // 是否启用内存监控
    enableMemoryMonitor: true,
    
    // 是否启用网络监控
    enableNetworkMonitor: true,
    
    // 日志级别
    logLevel: 'warn' // 'debug', 'info', 'warn', 'error'
  }
}

/**
 * 获取优化后的配置
 * @param {string} platform - 平台类型
 * @returns {Object} 优化后的配置
 */
export function getOptimizedConfig(platform = 'mp-weixin') {
  const config = { ...miniprogramConfig }
  
  // 根据平台调整配置
  if (platform === 'mp-weixin') {
    // 微信小程序特殊优化
    config.render.batchUpdateDelay = 16
    config.render.maxBatchSize = 8
    config.image.quality.cover = 60 // 更激进的压缩
    config.network.concurrency.maxConcurrent = 3 // 降低并发
  } else if (platform === 'h5') {
    // H5平台优化
    config.render.batchUpdateDelay = 8
    config.render.maxBatchSize = 20
    config.image.quality.cover = 80 // 更高质量的图片
    config.network.concurrency.maxConcurrent = 10 // 更高的并发
  }
  
  return config
}

/**
 * 性能监控工具
 */
export class PerformanceMonitor {
  constructor() {
    this.metrics = {
      renderTime: [],
      memoryUsage: [],
      networkTime: [],
      setDataCount: 0,
      setDataSize: 0
    }
    
    this.startTime = Date.now()
  }
  
  /**
   * 记录渲染时间
   */
  recordRenderTime(time) {
    this.metrics.renderTime.push(time)
    
    // 保持最近100条记录
    if (this.metrics.renderTime.length > 100) {
      this.metrics.renderTime.shift()
    }
  }
  
  /**
   * 记录setData调用
   */
  recordSetData(size) {
    this.metrics.setDataCount++
    this.metrics.setDataSize += size
  }
  
  /**
   * 记录内存使用
   */
  recordMemoryUsage() {
    // #ifdef MP-WEIXIN
    const performance = wx.getPerformance()
    if (performance && performance.memory) {
      this.metrics.memoryUsage.push({
        timestamp: Date.now(),
        used: performance.memory.usedJSHeapSize,
        total: performance.memory.totalJSHeapSize
      })
    }
    // #endif
  }
  
  /**
   * 获取性能报告
   */
  getReport() {
    const avgRenderTime = this.metrics.renderTime.length > 0 
      ? this.metrics.renderTime.reduce((a, b) => a + b, 0) / this.metrics.renderTime.length 
      : 0
    
    const avgSetDataSize = this.metrics.setDataCount > 0 
      ? this.metrics.setDataSize / this.metrics.setDataCount 
      : 0
    
    return {
      avgRenderTime: avgRenderTime.toFixed(2),
      setDataCount: this.metrics.setDataCount,
      avgSetDataSize: avgSetDataSize.toFixed(2),
      totalSetDataSize: this.metrics.setDataSize,
      uptime: Date.now() - this.startTime,
      memoryUsage: this.metrics.memoryUsage.slice(-10) // 最近10次内存记录
    }
  }
  
  /**
   * 重置监控数据
   */
  reset() {
    this.metrics = {
      renderTime: [],
      memoryUsage: [],
      networkTime: [],
      setDataCount: 0,
      setDataSize: 0
    }
    this.startTime = Date.now()
  }
}

// 创建全局性能监控实例
export const performanceMonitor = new PerformanceMonitor()

export default miniprogramConfig
