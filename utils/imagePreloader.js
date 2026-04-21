import { imageConfig, getOptimizedImageUrl } from '@/config/imageConfig.js'

// 图片预加载工具
class ImagePreloader {
  constructor() {
    this.preloadQueue = []
    this.maxConcurrent = imageConfig.preload.maxConcurrent
    this.isPreloading = false
  }

  // 添加图片到预加载队列
  addToQueue(urls) {
    if (Array.isArray(urls)) {
      this.preloadQueue.push(...urls)
    } else {
      this.preloadQueue.push(urls)
    }
  }

  // 预加载图片
  async preloadImages() {
    if (this.isPreloading || this.preloadQueue.length === 0) {
      return
    }

    this.isPreloading = true
    const urls = this.preloadQueue.splice(0, this.maxConcurrent)
    
    try {
      await Promise.all(urls.map(url => this.loadImage(url)))
      
      // 继续预加载剩余的图片
      if (this.preloadQueue.length > 0) {
        setTimeout(() => {
          this.preloadImages()
        }, imageConfig.preload.delay)
      }
    } catch (error) {
      console.warn('图片预加载失败:', error)
    } finally {
      this.isPreloading = false
    }
  }

  // 加载单张图片
  loadImage(url) {
    return new Promise((resolve, reject) => {
      if (!url) {
        resolve()
        return
      }

      // 检查是否已经加载过
      if (this.isImageLoaded(url)) {
        resolve()
        return
      }

      // #ifdef MP-WEIXIN
      // 微信小程序环境下，禁用预加载，避免大量网络请求
      console.log('⚡ 微信小程序环境，跳过图片预加载:', url);
      resolve();
      return;
      // #endif

      // #ifndef MP-WEIXIN
      // H5环境下正常预加载
      uni.getImageInfo({
        src: url,
        success: (res) => {
          // 标记为已加载
          this.markImageAsLoaded(url)
          resolve(res)
        },
        fail: (error) => {
          console.warn('图片加载失败:', url, error)
          resolve() // 即使失败也继续
        }
      })
      // #endif
    })
  }

  // 检查图片是否已加载
  isImageLoaded(url) {
    try {
      return uni.getStorageSync(`img_loaded_${url}`) === true
    } catch (e) {
      return false
    }
  }

  // 标记图片为已加载
  markImageAsLoaded(url) {
    try {
      uni.setStorageSync(`img_loaded_${url}`, true)
    } catch (e) {
      // 忽略存储错误
    }
  }

  // 清理预加载队列
  clearQueue() {
    this.preloadQueue = []
  }

  // 获取图片压缩URL（如果支持）
  getCompressedUrl(url, type = 'cover') {
    return getOptimizedImageUrl(url, type)
  }

  // 批量预加载关键图片
  async preloadCriticalImages(imageUrls) {
    if (!Array.isArray(imageUrls) || imageUrls.length === 0) {
      return
    }

    // 优先预加载关键图片
    const criticalUrls = imageUrls.slice(0, imageConfig.preload.criticalCount)
    this.addToQueue(criticalUrls)
    await this.preloadImages()
  }
}

// 创建全局实例
export const imagePreloader = new ImagePreloader()

// 导出工具函数
export const preloadImages = (urls) => {
  imagePreloader.addToQueue(urls)
  imagePreloader.preloadImages()
}

export const preloadCriticalImages = (urls) => {
  return imagePreloader.preloadCriticalImages(urls)
}

export const getCompressedUrl = (url, quality) => {
  return imagePreloader.getCompressedUrl(url, quality)
}
