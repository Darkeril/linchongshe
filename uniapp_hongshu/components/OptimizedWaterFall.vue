/**
 * 优化版瀑布流组件 - 专门针对微信小程序性能优化
 * 解决H5流畅但小程序卡顿的问题
 */

<template>
  <view class="waterfall-container">
    <!-- 左侧列 -->
    <view class="water-left">
      <block v-for="(item, index) in leftList" :key="getItemKey(item, index)">
        <view class="note-card" :style="{ height: getItemHeight(item) + 'rpx' }">
          <!-- 图片容器 -->
          <view class="image-container" @click="goToDetail(item.id, item.notesType)">
            <image 
              :src="getOptimizedImageUrl(item.coverPicture)" 
              :mode="getImageMode(item)"
              :lazy-load="true"
              :fade-show="false"
              :webp="true"
              :show-menu-by-longpress="false"
              class="note-image"
              @load="onImageLoad(item, 'left', index)"
              @error="onImageError(item, 'left', index)"
            />
            
            <!-- 加载状态 -->
            <view v-if="item._loading" class="image-loading">
              <u-loading-icon color="#C97B4A" size="24"></u-loading-icon>
            </view>
            
            <!-- 审核状态覆盖层 -->
            <view v-if="item.auditStatus === '0'" class="audit-overlay">
              <view class="audit-eye">
                <u-icon name="eye" color="#ffffff" size="48"></u-icon>
                <view class="audit-text">审核中...</view>
              </view>
            </view>
            
            <!-- 标签 / 置顶 -->
            <view v-if="item.pinned == 1 && type == 1" class="card-tag">置顶</view>
            <view v-else-if="item.categoryName" class="card-tag">
              #{{ item.categoryName }}
            </view>
            
            <!-- 浏览数 -->
            <view v-if="showViews && item.notesViewNum != null" class="look-views">
              <u-icon name="eye" color="#ffffff" size="25rpx"></u-icon>
              <view class="view-count">{{ item.notesViewNum }}</view>
            </view>
            
            <!-- 视频播放标识 -->
            <view v-if="item.notesType == 2" class="video-play">
              <u-icon name="play-right-fill" color="#ffffff" size="25rpx"></u-icon>
            </view>
          </view>
          
          <!-- 标题 -->
          <view class="title" @click="goToDetail(item.id, item.notesType)">
            {{ item.title }}
          </view>
          
          <!-- 底部信息：头像+用户名 | 点赞 -->
          <view v-if="slot_bottom" class="bottom-info">
            <view class="user-row">
              <image
                class="avatar"
                mode="aspectFill"
                :src="getOptimizedImageUrl(item.avatarUrl, 'avatar')"
              />
              <text class="username">{{ item.username }}</text>
            </view>
            <view class="like-row">
              <u-icon name="heart" :color="item.liked ? '#D66A5E' : '#8F7260'" size="24rpx"></u-icon>
              <text class="like-count" :class="{ 'liked': item.liked }">{{ formatLikes(item.notesLikeNum || item.likeCount || 0) }}</text>
            </view>
          </view>
        </view>
      </block>
    </view>
    
    <!-- 右侧列 -->
    <view class="water-right">
      <block v-for="(item, index) in rightList" :key="getItemKey(item, index)">
        <view class="note-card" :style="{ height: getItemHeight(item) + 'rpx' }">
          <!-- 图片容器 -->
          <view class="image-container" @click="goToDetail(item.id, item.notesType)">
            <image 
              :src="getOptimizedImageUrl(item.coverPicture)" 
              :mode="getImageMode(item)"
              :lazy-load="true"
              :fade-show="false"
              :webp="true"
              :show-menu-by-longpress="false"
              class="note-image"
              @load="onImageLoad(item, 'right', index)"
              @error="onImageError(item, 'right', index)"
            />
            
            <!-- 加载状态 -->
            <view v-if="item._loading" class="image-loading">
              <u-loading-icon color="#C97B4A" size="24"></u-loading-icon>
            </view>
            
            <!-- 审核状态覆盖层 -->
            <view v-if="item.auditStatus === '0'" class="audit-overlay">
              <view class="audit-eye">
                <u-icon name="eye" color="#ffffff" size="48"></u-icon>
                <view class="audit-text">审核中...</view>
              </view>
            </view>
            
            <!-- 标签 / 置顶 -->
            <view v-if="item.pinned == 1 && type == 1" class="card-tag">置顶</view>
            <view v-else-if="item.categoryName" class="card-tag">
              #{{ item.categoryName }}
            </view>
            
            <!-- 浏览数 -->
            <view v-if="showViews && item.notesViewNum != null" class="look-views">
              <u-icon name="eye" color="#ffffff" size="25rpx"></u-icon>
              <view class="view-count">{{ item.notesViewNum }}</view>
            </view>
            
            <!-- 视频播放标识 -->
            <view v-if="item.notesType == 2" class="video-play">
              <u-icon name="play-right-fill" color="#ffffff" size="25rpx"></u-icon>
            </view>
          </view>
          
          <!-- 标题 -->
          <view class="title" @click="goToDetail(item.id, item.notesType)">
            {{ item.title }}
          </view>
          
          <!-- 底部信息：头像+用户名 | 点赞 -->
          <view v-if="slot_bottom" class="bottom-info">
            <view class="user-row">
              <image
                class="avatar"
                mode="aspectFill"
                :src="getOptimizedImageUrl(item.avatarUrl, 'avatar')"
              />
              <text class="username">{{ item.username }}</text>
            </view>
            <view class="like-row">
              <u-icon name="heart" :color="item.liked ? '#D66A5E' : '#8F7260'" size="24rpx"></u-icon>
              <text class="like-count" :class="{ 'liked': item.liked }">{{ formatLikes(item.notesLikeNum || item.likeCount || 0) }}</text>
            </view>
          </view>
        </view>
      </block>
    </view>
  </view>
</template>

<script>
import performanceOptimizer from '@/utils/performanceOptimizer.js'

export default {
  name: 'OptimizedWaterFall',
  props: {
    list: {
      type: Array,
      default: () => []
    },
    showViews: {
      type: Boolean,
      default: false
    },
    slot_bottom: {
      type: Boolean,
      default: false
    },
    type: {
      type: Number,
      default: 1
    }
  },
  data() {
    return {
      leftList: [],
      rightList: [],
      leftHeight: 0,
      rightHeight: 0,
      itemHeights: new Map(), // 缓存项目高度
      imageLoadQueue: [], // 图片加载队列
      maxConcurrentLoads: 3, // 最大并发加载数
      loadingCount: 0 // 当前加载数量
    }
  },
  watch: {
    list: {
      handler(newList, oldList) {
        console.log('瀑布流list变化:', 'old:', oldList?.length, 'new:', newList?.length)
        
        if (!newList || newList.length === 0) {
          this.clear()
          return
        }
        
        // 如果旧列表为空且新列表有数据，才重新初始化
        if ((!oldList || oldList.length === 0) && newList.length > 0) {
          console.log('从空列表到有数据，重新初始化')
          this.clear()
          this.init()
        }
      },
      immediate: false
    }
  },
  methods: {
    /**
     * 获取项目唯一标识
     */
    getItemKey(item, index) {
      return item.id || item.nid || `item-${index}`
    },
    
    /**
     * 获取优化后的图片URL
     */
    getOptimizedImageUrl(url, type = 'cover') {
      if (!url) return '/static/default.jpg'
      
      // 如果是微信小程序，添加压缩参数
      // #ifdef MP-WEIXIN
      const separator = url.includes('?') ? '&' : '?'
      const quality = type === 'avatar' ? 60 : 70
      return `${url}${separator}q=${quality}&f=webp`
      // #endif
      
      // #ifndef MP-WEIXIN
      return url
      // #endif
    },
    
    /**
     * 获取图片显示模式
     */
    getImageMode(item) {
      return item.notesType == 2 ? 'aspectFill' : 'widthFix'
    },
    
    /**
     * 获取项目高度
     */
    getItemHeight(item) {
      const cachedHeight = this.itemHeights.get(item.id || item.nid)
      if (cachedHeight) {
        return cachedHeight
      }
      
      // 使用后端返回的高度或默认高度
      const baseHeight = item.noteCoverHeight ? this.pxToRpx(item.noteCoverHeight) : 200
      const titleHeight = item.title ? 60 : 0
      const bottomHeight = this.slot_bottom ? 80 : 0
      
      return baseHeight + titleHeight + bottomHeight
    },
    
    /**
     * 像素转rpx
     */
    pxToRpx(px) {
      const systemInfo = uni.getSystemInfoSync()
      return (px * 750) / systemInfo.windowWidth
    },
    
    /**
     * 初始化瀑布流
     */
    async init() {
      console.log('初始化瀑布流，数据量:', this.list.length)
      
      // 清空现有数据
      this.leftList = []
      this.rightList = []
      this.leftHeight = 0
      this.rightHeight = 0
      this.itemHeights.clear()
      
      if (!this.list || this.list.length === 0) {
        return
      }
      
      // 分批处理数据，避免阻塞
      await this.batchProcessItems(this.list)
    },
    
    /**
     * 分批处理项目
     */
    async batchProcessItems(items) {
      const batchSize = 5 // 每批处理5个项目
      
      for (let i = 0; i < items.length; i += batchSize) {
        const batch = items.slice(i, i + batchSize)
        await this.processBatch(batch)
        
        // 让出主线程，避免阻塞
        await this.yieldToMain()
      }
    },
    
    /**
     * 处理一批项目
     */
    async processBatch(batch) {
      const processedItems = []
      
      for (const item of batch) {
        const processedItem = await this.processItem(item)
        processedItems.push(processedItem)
      }
      
      // 批量分配到左右列
      this.assignToColumns(processedItems)
    },
    
    /**
     * 处理单个项目
     */
    async processItem(item) {
      const processedItem = { ...item }
      
      // 如果有后端返回的高度，直接使用
      if (item.noteCoverHeight && item.noteCoverHeight > 0) {
        processedItem._height = this.pxToRpx(item.noteCoverHeight)
        this.itemHeights.set(item.id || item.nid, processedItem._height)
        return processedItem
      }
      
      // 否则异步获取图片高度
      try {
        const imageInfo = await this.getImageHeight(item.coverPicture)
        processedItem._height = imageInfo.height
        processedItem.coverPicture = imageInfo.path
        this.itemHeights.set(item.id || item.nid, processedItem._height)
      } catch (error) {
        console.warn('获取图片高度失败:', error)
        processedItem._height = 200 // 默认高度
        this.itemHeights.set(item.id || item.nid, processedItem._height)
      }
      
      return processedItem
    },
    
    /**
     * 分配到左右列
     */
    assignToColumns(items) {
      const newLeftList = [...this.leftList]
      const newRightList = [...this.rightList]
      let leftHeight = this.leftHeight
      let rightHeight = this.rightHeight
      
      items.forEach(item => {
        const height = item._height + 50 // 加上间距
        
        if (leftHeight <= rightHeight) {
          newLeftList.push(item)
          leftHeight += height
        } else {
          newRightList.push(item)
          rightHeight += height
        }
      })
      
      // 使用性能优化器批量更新
      performanceOptimizer.batchSetData(this, {
        leftList: newLeftList,
        rightList: newRightList,
        leftHeight: leftHeight,
        rightHeight: rightHeight
      })
    },
    
    /**
     * 添加新数据
     */
    async addList(newItems) {
      if (!newItems || newItems.length === 0) return
      
      console.log('添加新数据到瀑布流:', newItems.length)
      
      // 去重处理
      const existingIds = new Set([
        ...this.leftList.map(item => item.id || item.nid),
        ...this.rightList.map(item => item.id || item.nid)
      ])
      
      const uniqueItems = newItems.filter(item => {
        const id = item.id || item.nid
        return id && !existingIds.has(id)
      })
      
      if (uniqueItems.length === 0) {
        console.log('没有新数据需要添加')
        return
      }
      
      console.log('去重后新数据量:', uniqueItems.length)
      
      // 分批处理新数据
      await this.batchProcessItems(uniqueItems)
    },
    
    /**
     * 获取图片高度
     */
    getImageHeight(url) {
      return new Promise((resolve, reject) => {
        if (!url) {
          reject(new Error('图片URL为空'))
          return
        }
        
        uni.getImageInfo({
          src: url,
          success: (res) => {
            const height = this.pxToRpx(res.height)
            resolve({
              path: url,
              height: height
            })
          },
          fail: (error) => {
            console.warn('获取图片信息失败:', error)
            reject(error)
          }
        })
      })
    },
    
    /**
     * 图片加载成功
     */
    onImageLoad(item, column, index) {
      item._loading = false
      console.log('图片加载成功:', item.id, column, index)
    },
    
    /**
     * 图片加载失败
     */
    onImageError(item, column, index) {
      item._loading = false
      console.warn('图片加载失败:', item.id, column, index)
    },
    
    /**
     * 清空数据
     */
    clear() {
      this.leftList = []
      this.rightList = []
      this.leftHeight = 0
      this.rightHeight = 0
      this.itemHeights.clear()
      this.imageLoadQueue = []
      this.loadingCount = 0
    },
    
    /**
     * 让出主线程
     */
    yieldToMain() {
      return new Promise(resolve => {
        setTimeout(resolve, 0)
      })
    },
    
    /**
     * 格式化点赞数（>999 显示 x.xk）
     */
    formatLikes(num) {
      if (!num) return '0'
      if (num > 9999) return (num / 10000).toFixed(1) + 'w'
      if (num > 999) return (num / 1000).toFixed(1) + 'k'
      return String(num)
    },

    /**
     * 跳转到详情页
     */
    goToDetail(id, notesType) {
      this.$emit('goToDetail', { id, notesType })
    }
  },
  
  onUnload() {
    // 清理资源
    performanceOptimizer.cleanup()
    this.clear()
  }
}
</script>

<style scoped>
/* ── 容器：两列 flex，列间距 16rpx (design: gap 8px) ── */
.waterfall-container {
  display: flex;
  gap: 16rpx;
  width: 100%;
  padding: 0 20rpx;
  box-sizing: border-box;
  background: #F4EDE2;
}

.water-left,
.water-right {
  flex: 1;
  min-width: 0;
}

/* ── 卡片：圆角 32rpx，极淡阴影 (design: radius 16, shadow 0 2px 8px) ── */
.note-card {
  margin-bottom: 20rpx;
  background: #FFFFFF;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

/* ── 图片区 ── */
.image-container {
  position: relative;
  width: 100%;
}

.note-image {
  width: 100%;
  display: block;
}

.image-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16rpx;
  padding: 20rpx;
}

.audit-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
}

.audit-eye {
  text-align: center;
  color: #ffffff;
}

.audit-text {
  font-size: 24rpx;
  margin-top: 8rpx;
}

/* ── 卡片标签：毛玻璃白底 (design: rgba(255,255,255,0.85) + blur + #8A4A1F) ── */
.card-tag {
  position: absolute;
  top: 16rpx;
  left: 16rpx;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  font-size: 20rpx;
  font-weight: 600;
  color: #8A4A1F;
}

/* ── 浏览数 ── */
.look-views {
  position: absolute;
  bottom: 12rpx;
  right: 12rpx;
  background: rgba(0, 0, 0, 0.45);
  color: #ffffff;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  font-size: 20rpx;
}

.view-count {
  margin-left: 6rpx;
}

/* ── 视频播放 ── */
.video-play {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ── 标题：24rpx/500 (design: 12px, fontWeight 500) ── */
.title {
  padding: 16rpx 20rpx 0;
  font-size: 24rpx;
  font-weight: 500;
  color: #231710;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* ── 底部：头像+用户名 左 / 点赞 右 (design: space-between) ── */
.bottom-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 20rpx 20rpx;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  min-width: 0;
  flex: 1;
  overflow: hidden;
}

.avatar {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.username {
  font-size: 20rpx;
  color: #8F7260;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.like-row {
  display: flex;
  align-items: center;
  gap: 6rpx;
  flex-shrink: 0;
}

.like-count {
  font-size: 20rpx;
  font-weight: 500;
  color: #8F7260;
}

.like-count.liked {
  color: #D66A5E;
}
</style>
