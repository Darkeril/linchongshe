<template>
  <view class="notes-list-item" @click="handleClick">
    <!-- 用户信息 -->
    <view class="user-info">
      <image 
        :src="item.userAvatar" 
        class="avatar"
        mode="aspectFill"
        lazy-load
        @error="handleImageError"
      />
      <view class="user-details">
        <text class="username">{{ item.username }}</text>
        <text class="publish-time">{{ formatTime(item.publishTime) }}</text>
      </view>
    </view>
    
    <!-- 笔记内容 -->
    <view class="content">
      <text class="content-text">{{ item.content }}</text>
    </view>
    
    <!-- 图片列表 -->
    <view v-if="item.imageList && item.imageList.length > 0" class="image-list">
      <view 
        v-for="(image, index) in item.imageList" 
        :key="index"
        class="image-item"
        @click.stop="previewImage(index)"
      >
        <image 
          :src="image" 
          class="content-image"
          mode="aspectFill"
          lazy-load
          @error="handleImageError"
        />
      </view>
    </view>
    
    <!-- 操作栏 -->
    <view class="action-bar">
      <view class="action-item" @click.stop="handleLike">
        <u-icon 
          :name="item.isLiked ? 'thumb-up-fill' : 'thumb-up'" 
          :color="item.isLiked ? '#ff4757' : '#999'"
          size="20"
        />
        <text class="action-text">{{ item.likeCount || 0 }}</text>
      </view>
      
      <view class="action-item" @click.stop="handleComment">
        <u-icon name="chat" color="#999" size="20" />
        <text class="action-text">{{ item.commentCount || 0 }}</text>
      </view>
      
      <view class="action-item" @click.stop="handleShare">
        <u-icon name="share" color="#999" size="20" />
        <text class="action-text">分享</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'NotesListItem',
  props: {
    item: {
      type: Object,
      required: true
    },
    index: {
      type: Number,
      required: true
    }
  },
  methods: {
    /**
     * 处理点击事件
     */
    handleClick() {
      this.$emit('item-click', this.item, this.index)
    },
    
    /**
     * 预览图片
     */
    previewImage(current) {
      const urls = this.item.imageList.map(img => img)
      uni.previewImage({
        current,
        urls
      })
    },
    
    /**
     * 处理点赞
     */
    handleLike() {
      this.$emit('like', this.item, this.index)
    },
    
    /**
     * 处理评论
     */
    handleComment() {
      this.$emit('comment', this.item, this.index)
    },
    
    /**
     * 处理分享
     */
    handleShare() {
      this.$emit('share', this.item, this.index)
    },
    
    /**
     * 处理图片加载错误
     */
    handleImageError(e) {
      console.warn('图片加载失败:', e)
    },
    
    /**
     * 格式化时间
     */
    formatTime(timestamp) {
      if (!timestamp) return ''
      
      const now = Date.now()
      const diff = now - timestamp
      
      if (diff < 60000) { // 1分钟内
        return '刚刚'
      } else if (diff < 3600000) { // 1小时内
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) { // 1天内
        return Math.floor(diff / 3600000) + '小时前'
      } else {
        const date = new Date(timestamp)
        return `${date.getMonth() + 1}月${date.getDate()}日`
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.notes-list-item {
  background: #fff;
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    
    .avatar {
      width: 40px;
      height: 40px;
      border-radius: 20px;
      margin-right: 12px;
    }
    
    .user-details {
      flex: 1;
      
      .username {
        display: block;
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin-bottom: 4px;
      }
      
      .publish-time {
        font-size: 12px;
        color: #999;
      }
    }
  }
  
  .content {
    margin-bottom: 12px;
    
    .content-text {
      font-size: 15px;
      line-height: 1.6;
      color: #333;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 3;
      overflow: hidden;
    }
  }
  
  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
    
    .image-item {
      flex: 1;
      min-width: 0;
      
      .content-image {
        width: 100%;
        height: 120px;
        border-radius: 6px;
      }
    }
  }
  
  .action-bar {
    display: flex;
    justify-content: space-around;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
    
    .action-item {
      display: flex;
      align-items: center;
      padding: 8px 16px;
      border-radius: 20px;
      transition: background-color 0.2s;
      
      &:active {
        background-color: #f5f5f5;
      }
      
      .action-text {
        margin-left: 6px;
        font-size: 14px;
        color: #666;
      }
    }
  }
}
</style>
