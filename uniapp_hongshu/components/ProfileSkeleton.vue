<template>
  <view class="profile-skeleton-container">
    <!-- 背景图片骨架 -->
    <view class="background-skeleton"></view>
    
    <!-- 状态栏骨架 -->
    <view class="status-bar-skeleton"></view>
    
    <!-- 导航栏骨架 -->
    <view class="navigation-bar-skeleton">
      <view class="nav-left-skeleton"></view>
      <view class="nav-avatar-skeleton"></view>
      <view class="nav-right-skeleton"></view>
    </view>
    
    <!-- 用户信息区域骨架 -->
    <view class="user-info-skeleton">
      <!-- 用户头像和基本信息 -->
      <view class="user-main-skeleton">
        <view class="user-avatar-skeleton"></view>
        <view class="user-details-skeleton">
          <view class="username-skeleton"></view>
          <view class="user-id-skeleton"></view>
          <view class="ip-location-skeleton"></view>
        </view>
      </view>
      
      <!-- 个人简介骨架 -->
      <view class="introduction-skeleton">
        <view class="intro-line-skeleton" v-for="i in 2" :key="i"></view>
      </view>
      
      <!-- 标签骨架 -->
      <view class="tags-skeleton">
        <view class="tag-skeleton" v-for="i in 3" :key="i"></view>
      </view>
      
      <!-- 统计数据骨架 -->
      <view class="stats-skeleton">
        <view class="stat-item-skeleton" v-for="i in 3" :key="i">
          <view class="stat-number-skeleton"></view>
          <view class="stat-label-skeleton"></view>
        </view>
        <view class="action-buttons-skeleton">
          <view class="edit-button-skeleton"></view>
          <view class="settings-button-skeleton"></view>
        </view>
      </view>
      
      <!-- 功能按钮骨架 -->
      <view class="feature-buttons-skeleton">
        <view class="feature-button-skeleton" v-for="i in 3" :key="i">
          <view class="feature-icon-skeleton"></view>
          <view class="feature-text-skeleton"></view>
          <view class="feature-desc-skeleton"></view>
        </view>
      </view>
    </view>
    
    <!-- 标签页骨架 -->
    <view class="tabs-skeleton">
      <view class="tab-item-skeleton" v-for="i in 3" :key="i"></view>
      <view class="search-icon-skeleton"></view>
    </view>
    
    <!-- 内容区域骨架 -->
    <view class="content-skeleton">
      <!-- 笔记网格骨架 -->
      <view class="notes-grid-skeleton">
        <view class="note-card-skeleton" v-for="n in count" :key="n">
          <!-- 置顶标签 -->
          <view class="pinned-tag-skeleton"></view>
          
          <!-- 笔记图片 -->
          <view class="note-image-skeleton"></view>
          
          <!-- 浏览量 -->
          <view class="view-count-skeleton"></view>
          
          <!-- 视频播放按钮 -->
          <view class="video-play-skeleton"></view>
          
          <!-- 笔记标题 -->
          <view class="note-title-skeleton"></view>
          
          <!-- 用户信息 -->
          <view class="note-user-skeleton">
            <view class="note-avatar-skeleton"></view>
            <view class="note-username-skeleton"></view>
            <view class="note-likes-skeleton"></view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'ProfileSkeleton',
  props: {
    // 笔记卡片数量
    count: {
      type: Number,
      default: 6
    },
    // 是否显示置顶标签
    showPinned: {
      type: Boolean,
      default: true
    },
    // 是否显示视频播放按钮
    showVideoPlay: {
      type: Boolean,
      default: true
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-skeleton-container {
  position: relative;
  min-height: 100vh;
  background: #f5f5f5;
}

/* 背景图片骨架 */
.background-skeleton {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 500rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  filter: brightness(65%);
}

/* 状态栏骨架 */
.status-bar-skeleton {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 44rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  z-index: 10;
}

/* 导航栏骨架 */
.navigation-bar-skeleton {
  position: fixed;
  top: 44rpx;
  left: 0;
  width: 100%;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  z-index: 10;
  
  .nav-left-skeleton {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
  }
  
  .nav-avatar-skeleton {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
  }
  
  .nav-right-skeleton {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
  }
}

/* 用户信息区域骨架 */
.user-info-skeleton {
  position: relative;
  padding: 0 40rpx 40rpx 40rpx;
  margin-top: 200rpx;
  
  /* 用户头像和基本信息 */
  .user-main-skeleton {
    display: flex;
    margin-top: 60rpx;
    
    .user-avatar-skeleton {
      width: 160rpx;
      height: 160rpx;
      border-radius: 50%;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: skeleton-loading 1.5s infinite;
      margin-right: 40rpx;
    }
    
    .user-details-skeleton {
      flex: 1;
      margin-top: 50rpx;
      
      .username-skeleton {
        width: 120rpx;
        height: 35rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 17rpx;
        margin-bottom: 20rpx;
      }
      
      .user-id-skeleton {
        width: 200rpx;
        height: 23rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 11rpx;
        margin-bottom: 10rpx;
      }
      
      .ip-location-skeleton {
        width: 100rpx;
        height: 23rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 11rpx;
      }
    }
  }
  
  /* 个人简介骨架 */
  .introduction-skeleton {
    margin: 20rpx 0;
    
    .intro-line-skeleton {
      height: 30rpx;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: skeleton-loading 1.5s infinite;
      border-radius: 15rpx;
      margin-bottom: 10rpx;
      
      &:nth-child(1) { width: 100%; }
      &:nth-child(2) { width: 70%; }
    }
  }
  
  /* 标签骨架 */
  .tags-skeleton {
    display: flex;
    gap: 16rpx;
    margin: 20rpx 0;
    
    .tag-skeleton {
      width: 80rpx;
      height: 38rpx;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: skeleton-loading 1.5s infinite;
      border-radius: 30rpx;
      
      &:nth-child(1) { width: 100rpx; }
      &:nth-child(2) { width: 90rpx; }
      &:nth-child(3) { width: 110rpx; }
    }
  }
  
  /* 统计数据骨架 */
  .stats-skeleton {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 20rpx 0;
    
    .stat-item-skeleton {
      text-align: center;
      
      .stat-number-skeleton {
        width: 40rpx;
        height: 24rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 12rpx;
        margin-bottom: 8rpx;
      }
      
      .stat-label-skeleton {
        width: 60rpx;
        height: 20rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 10rpx;
      }
    }
    
    .action-buttons-skeleton {
      display: flex;
      gap: 20rpx;
      
      .edit-button-skeleton {
        width: 120rpx;
        height: 60rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 30rpx;
      }
      
      .settings-button-skeleton {
        width: 60rpx;
        height: 60rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 30rpx;
      }
    }
  }
  
  /* 功能按钮骨架 */
  .feature-buttons-skeleton {
    display: flex;
    justify-content: space-between;
    margin: 40rpx 0;
    
    .feature-button-skeleton {
      flex: 1;
      padding: 20rpx;
      background: rgba(89, 88, 87, 0.6);
      border-radius: 20rpx;
      margin: 0 10rpx;
      
      .feature-icon-skeleton {
        width: 36rpx;
        height: 36rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 50%;
        margin-bottom: 10rpx;
      }
      
      .feature-text-skeleton {
        width: 80rpx;
        height: 27rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 13rpx;
        margin-bottom: 8rpx;
      }
      
      .feature-desc-skeleton {
        width: 120rpx;
        height: 20rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 10rpx;
      }
    }
  }
}

/* 标签页骨架 */
.tabs-skeleton {
  position: sticky;
  top: 200rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100rpx;
  border-top-left-radius: 20rpx;
  border-top-right-radius: 20rpx;
  padding: 0 40rpx;
  
  .tab-item-skeleton {
    width: 80rpx;
    height: 40rpx;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: 20rpx;
    margin: 0 30rpx;
    
    &:nth-child(1) { width: 100rpx; }
    &:nth-child(2) { width: 90rpx; }
    &:nth-child(3) { width: 80rpx; }
  }
  
  .search-icon-skeleton {
    position: absolute;
    right: 40rpx;
    width: 50rpx;
    height: 50rpx;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: 50%;
  }
}

/* 内容区域骨架 */
.content-skeleton {
  background: #fff;
  min-height: 400rpx;
  
  .notes-grid-skeleton {
    display: flex;
    flex-wrap: wrap;
    padding: 20rpx;
    gap: 20rpx;
    
    .note-card-skeleton {
      width: calc(50% - 10rpx);
      background: #fff;
      border-radius: 20rpx;
      overflow: hidden;
      position: relative;
      box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.08);
      
      /* 置顶标签 */
      .pinned-tag-skeleton {
        position: absolute;
        top: 10rpx;
        left: 10rpx;
        width: 60rpx;
        height: 30rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 15rpx;
        z-index: 2;
      }
      
      /* 笔记图片 */
      .note-image-skeleton {
        width: 100%;
        height: 300rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
      }
      
      /* 浏览量 */
      .view-count-skeleton {
        position: absolute;
        bottom: 100rpx;
        left: 10rpx;
        width: 60rpx;
        height: 30rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 15rpx;
        z-index: 2;
      }
      
      /* 视频播放按钮 */
      .video-play-skeleton {
        position: absolute;
        top: 10rpx;
        right: 10rpx;
        width: 60rpx;
        height: 60rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 50%;
        z-index: 2;
      }
      
      /* 笔记标题 */
      .note-title-skeleton {
        padding: 20rpx;
        height: 24rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 12rpx;
        margin-bottom: 10rpx;
      }
      
      /* 用户信息 */
      .note-user-skeleton {
        display: flex;
        align-items: center;
        padding: 0 20rpx 20rpx 20rpx;
        
        .note-avatar-skeleton {
          width: 44rpx;
          height: 44rpx;
          border-radius: 50%;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          margin-right: 20rpx;
        }
        
        .note-username-skeleton {
          flex: 1;
          height: 20rpx;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 10rpx;
        }
        
        .note-likes-skeleton {
          width: 60rpx;
          height: 20rpx;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 10rpx;
        }
      }
    }
  }
}

/* 骨架屏动画 */
@keyframes skeleton-loading {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* 响应式调整 */
@media screen and (max-width: 750rpx) {
  .user-info-skeleton {
    padding: 0 20rpx 40rpx 20rpx;
  }
  
  .notes-grid-skeleton {
    padding: 10rpx;
    gap: 10rpx;
  }
}
</style>
