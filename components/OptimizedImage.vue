<template>
  <view class="optimized-image-container">
    <!-- 加载状态 -->
    <view v-if="loading" class="image-loading">
      <view class="loading-spinner"></view>
    </view>
    
    <!-- 错误状态 -->
    <view v-else-if="error" class="image-error" @click="retryLoad">
      <view class="error-icon">📷</view>
      <text class="error-text">图片加载失败</text>
      <text class="retry-text">点击重试</text>
    </view>
    
    <!-- 正常图片 -->
    <image 
      v-else
      :src="currentSrc" 
      :mode="mode"
      :lazy-load="lazyLoad"
      :fade-show="fadeShow"
      :webp="webp"
      :show-menu-by-longpress="showMenuByLongpress"
      :class="imageClass"
      :style="imageStyle"
      @load="onImageLoad"
      @error="onImageError"
      @click="onImageClick"
    />
  </view>
</template>

<script>
export default {
  name: 'OptimizedImage',
  props: {
    // 图片源地址
    src: {
      type: String,
      required: true
    },
    // 图片显示模式
    mode: {
      type: String,
      default: 'aspectFill'
    },
    // 是否启用懒加载
    lazyLoad: {
      type: Boolean,
      default: true
    },
    // 是否启用淡入效果
    fadeShow: {
      type: Boolean,
      default: true
    },
    // 是否启用webp格式
    webp: {
      type: Boolean,
      default: true
    },
    // 是否支持长按菜单
    showMenuByLongpress: {
      type: Boolean,
      default: false
    },
    // 自定义样式类
    imageClass: {
      type: String,
      default: ''
    },
    // 自定义样式
    imageStyle: {
      type: Object,
      default: () => ({})
    },
    // 占位图
    placeholder: {
      type: String,
      default: '/static/image/placeholder.png'
    },
    // 错误图
    errorImage: {
      type: String,
      default: '/static/image/error.png'
    }
  },
  data() {
    return {
      loading: true,
      error: false,
      currentSrc: this.src,
      retryCount: 0,
      maxRetry: 3
    }
  },
  watch: {
    src: {
      handler(newSrc) {
        if (newSrc !== this.currentSrc) {
          this.resetState()
          this.currentSrc = newSrc
        }
      },
      immediate: true
    }
  },
  methods: {
    /**
     * 图片加载成功
     */
    onImageLoad() {
      this.loading = false
      this.error = false
      this.$emit('load', this.currentSrc)
    },
    
    /**
     * 图片加载失败
     */
    onImageError(e) {
      this.loading = false
      
      if (this.retryCount < this.maxRetry) {
        // 重试加载
        this.retryCount++
        this.$logger.warn(`图片加载失败，第${this.retryCount}次重试:`, this.currentSrc)
        setTimeout(() => {
          this.currentSrc = this.currentSrc + (this.currentSrc.includes('?') ? '&' : '?') + `retry=${this.retryCount}`
        }, 1000 * this.retryCount)
      } else {
        // 显示错误状态
        this.error = true
        this.$logger.error('图片加载失败，已达到最大重试次数:', this.currentSrc)
        this.$emit('error', {
          src: this.currentSrc,
          error: e,
          retryCount: this.retryCount
        })
      }
    },
    
    /**
     * 重试加载
     */
    retryLoad() {
      this.resetState()
      this.currentSrc = this.src
    },
    
    /**
     * 重置状态
     */
    resetState() {
      this.loading = true
      this.error = false
      this.retryCount = 0
    },
    
    /**
     * 图片点击事件
     */
    onImageClick() {
      this.$emit('click', this.currentSrc)
    }
  }
}
</script>

<style lang="scss" scoped>
.optimized-image-container {
  position: relative;
  display: inline-block;
  
  .image-loading {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f5f5;
    border-radius: 4px;
    min-height: 100px;
    
    .loading-spinner {
      width: 24px;
      height: 24px;
      border: 2px solid #f3f3f3;
      border-top: 2px solid #999;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
  }
  
  .image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: #f5f5f5;
    border-radius: 4px;
    min-height: 100px;
    cursor: pointer;
    
    .error-icon {
      font-size: 24px;
      margin-bottom: 8px;
    }
    
    .error-text {
      font-size: 12px;
      color: #999;
      margin-top: 8px;
    }
    
    .retry-text {
      font-size: 10px;
      color: #ccc;
      margin-top: 4px;
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
