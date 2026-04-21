<template>
  <view class="virtual-list-container" :style="{ height: containerHeight + 'px' }">
    <!-- 虚拟滚动容器 -->
    <scroll-view
      scroll-y
      :style="{ height: containerHeight + 'px' }"
      @scroll="onScroll"
      :scroll-top="scrollTop"
      :scroll-with-animation="false"
      :enable-back-to-top="false"
      :refresher-enabled="refresherEnabled"
      @refresherrefresh="onRefresh"
      :refresher-triggered="refreshing"
      :refresher-threshold="100"
      @scrolltolower="onReachBottom"
    >
      <!-- 占位元素，用于撑开滚动区域 -->
      <view :style="{ height: totalHeight + 'px' }">
        <!-- 可见区域的内容 -->
        <view 
          class="visible-content"
          :style="{ 
            transform: `translateY(${offsetY}px)`,
            position: 'relative'
          }"
        >
          <view
            v-for="(item, index) in visibleItems"
            :key="getItemKey(item, startIndex + index)"
            :style="{ height: itemHeight + 'px' }"
            class="list-item"
          >
            <slot 
              :item="item" 
              :index="startIndex + index"
              :isVisible="true"
            ></slot>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-indicator">
      <u-loading-icon mode="circle" size="24" color="#999"></u-loading-icon>
      <text class="loading-text">加载中...</text>
    </view>
    
    <!-- 空状态 -->
    <view v-if="!loading && dataList.length === 0" class="empty-state">
      <text class="empty-text">暂无数据</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'VirtualList',
  props: {
    // 数据列表
    dataList: {
      type: Array,
      default: () => []
    },
    // 容器高度
    containerHeight: {
      type: Number,
      default: 600
    },
    // 每项高度
    itemHeight: {
      type: Number,
      default: 100
    },
    // 缓冲区大小（可见区域外的额外渲染项数）
    bufferSize: {
      type: Number,
      default: 5
    },
    // 是否启用下拉刷新
    refresherEnabled: {
      type: Boolean,
      default: true
    },
    // 是否正在刷新
    refreshing: {
      type: Boolean,
      default: false
    },
    // 是否正在加载
    loading: {
      type: Boolean,
      default: false
    },
    // 获取项目唯一键的函数
    getItemKey: {
      type: Function,
      default: (item, index) => index
    }
  },
  data() {
    return {
      scrollTop: 0,
      startIndex: 0,
      endIndex: 0,
      visibleCount: 0,
      offsetY: 0
    }
  },
  computed: {
    // 总高度
    totalHeight() {
      return this.dataList.length * this.itemHeight
    },
    
    // 可见项目列表
    visibleItems() {
      return this.dataList.slice(this.startIndex, this.endIndex + 1)
    }
  },
  watch: {
    dataList: {
      handler() {
        this.updateVisibleRange()
      },
      immediate: true
    },
    
    containerHeight() {
      this.updateVisibleRange()
    },
    
    itemHeight() {
      this.updateVisibleRange()
    }
  },
  mounted() {
    this.updateVisibleRange()
  },
  methods: {
    /**
     * 更新可见范围
     */
    updateVisibleRange() {
      if (this.dataList.length === 0) {
        this.startIndex = 0
        this.endIndex = 0
        this.visibleCount = 0
        this.offsetY = 0
        return
      }
      
      // 计算可见区域能显示多少个项目
      this.visibleCount = Math.ceil(this.containerHeight / this.itemHeight)
      
      // 计算当前滚动位置对应的开始索引
      const scrollIndex = Math.floor(this.scrollTop / this.itemHeight)
      
      // 添加缓冲区
      this.startIndex = Math.max(0, scrollIndex - this.bufferSize)
      this.endIndex = Math.min(
        this.dataList.length - 1,
        scrollIndex + this.visibleCount + this.bufferSize
      )
      
      // 计算偏移量
      this.offsetY = this.startIndex * this.itemHeight
    },
    
    /**
     * 滚动事件处理
     */
    onScroll(e) {
      this.scrollTop = e.detail.scrollTop
      this.updateVisibleRange()
    },
    
    /**
     * 下拉刷新
     */
    onRefresh() {
      this.$emit('refresh')
    },
    
    /**
     * 触底加载更多
     */
    onReachBottom() {
      this.$emit('loadmore')
    },
    
    /**
     * 滚动到指定位置
     */
    scrollToIndex(index) {
      if (index < 0 || index >= this.dataList.length) {
        return
      }
      
      const scrollTop = index * this.itemHeight
      this.scrollTop = scrollTop
      this.updateVisibleRange()
    },
    
    /**
     * 滚动到顶部
     */
    scrollToTop() {
      this.scrollToIndex(0)
    },
    
    /**
     * 滚动到底部
     */
    scrollToBottom() {
      this.scrollToIndex(this.dataList.length - 1)
    },
    
    /**
     * 获取当前可见的项目索引范围
     */
    getVisibleRange() {
      return {
        start: this.startIndex,
        end: this.endIndex,
        count: this.endIndex - this.startIndex + 1
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.virtual-list-container {
  position: relative;
  overflow: hidden;
  
  .visible-content {
    will-change: transform;
  }
  
  .list-item {
    display: flex;
    align-items: center;
    box-sizing: border-box;
  }
  
  .loading-indicator {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    z-index: 10;
    
    .loading-text {
      margin-top: 8px;
      font-size: 14px;
      color: #999;
    }
  }
  
  .empty-state {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 10;
    
    .empty-text {
      font-size: 16px;
      color: #999;
    }
  }
}
</style>
