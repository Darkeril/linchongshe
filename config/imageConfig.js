// 图片加载优化配置
export const imageConfig = {
  // 图片压缩质量
  quality: {
    avatar: 60,      // 头像压缩质量
    cover: 70,       // 封面图压缩质量
    content: 80,     // 内容图压缩质量
    thumbnail: 50    // 缩略图压缩质量
  },

  // 图片尺寸限制
  maxSize: {
    avatar: 100,     // 头像最大尺寸
    cover: 500,      // 封面图最大尺寸
    content: 800     // 内容图最大尺寸
  },

  // 懒加载配置
  lazyLoad: {
    enabled: true,           // 启用懒加载
    threshold: 100,          // 触发距离（px）
    placeholder: '/static/placeholder.png' // 占位图
  },

  // 预加载配置
  preload: {
    enabled: true,           // 启用预加载
    maxConcurrent: 3,        // 最大并发数
    criticalCount: 3,        // 关键图片数量
    delay: 100               // 预加载延迟（ms）
  },

  // 缓存配置
  cache: {
    enabled: true,           // 启用缓存
    maxSize: 200,           // 最大缓存数量
    ttl: 30 * 60 * 1000,    // 缓存过期时间（30分钟）
    cleanupRatio: 0.2       // 清理比例（20%）
  },

  // 图片格式优化
  format: {
    preferWebp: true,        // 优先使用WebP
    fallbackJpg: true,       // 回退到JPG
    qualityMap: {
      'image/jpeg': 85,
      'image/png': 90,
      'image/webp': 80
    }
  },

  // 错误处理
  error: {
    retryCount: 2,           // 重试次数
    retryDelay: 1000,        // 重试延迟（ms）
    fallbackUrl: '/static/error.png' // 错误占位图
  }
}

// 获取压缩后的图片URL
export const getOptimizedImageUrl = (url, type = 'cover') => {
  if (!url) return url
  
  const quality = imageConfig.quality[type] || imageConfig.quality.cover
  
  // 如果是OSS图片，添加压缩参数
  if (url.includes('oss') || url.includes('aliyuncs')) {
    const separator = url.includes('?') ? '&' : '?'
    return `${url}${separator}x-oss-process=image/quality,q_${quality}/format,webp`
  }
  
  // 如果是其他CDN，尝试添加压缩参数
  if (url.includes('cdn') || url.includes('static')) {
    const separator = url.includes('?') ? '&' : '?'
    return `${url}${separator}q=${quality}&f=webp`
  }
  
  return url
}

// 获取图片加载配置
export const getImageLoadConfig = (type = 'cover') => {
  return {
    lazyLoad: imageConfig.lazyLoad.enabled,
    webp: imageConfig.format.preferWebp,
    showMenuByLongpress: false,
    fadeShow: true,
    quality: imageConfig.quality[type] || imageConfig.quality.cover
  }
}



