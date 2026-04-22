<template>
  <view class="skeleton-container">
    <!-- 关注页骨架屏 -->
    <view v-if="type === 'follow'" class="follow-skeleton">
      <view v-for="n in count" :key="n" class="follow-skeleton-item">
        <!-- 用户信息骨架 -->
        <view class="user-info-skeleton">
          <view class="avatar-skeleton"></view>
          <view class="user-details-skeleton">
            <view class="username-skeleton"></view>
            <view class="time-skeleton"></view>
          </view>
        </view>
        
        <!-- 内容骨架 -->
        <view class="content-skeleton">
          <view class="text-line-skeleton" v-for="i in 3" :key="i"></view>
        </view>
        
        <!-- 图片骨架 -->
        <view class="image-skeleton" v-if="showImages">
          <view class="single-image-skeleton" v-if="imageType === 'single'"></view>
          <view class="multi-image-skeleton" v-else>
            <view class="image-item-skeleton" v-for="i in 3" :key="i"></view>
          </view>
        </view>
        
        <!-- 操作栏骨架 -->
        <view class="action-bar-skeleton">
          <view class="action-item-skeleton" v-for="i in 3" :key="i"></view>
        </view>
      </view>
    </view>
    
    <!-- 发现页骨架屏 -->
    <view v-else-if="type === 'discover'" class="discover-skeleton">
      <view class="waterfall-skeleton">
        <view class="water-left">
          <view class="note-card-skeleton" v-for="n in Math.ceil(count/2)" :key="n">
            <view class="image-skeleton"></view>
            <view class="note-info-skeleton">
              <view class="note-title-skeleton"></view>
              <view class="note-meta-skeleton">
                <view class="username-skeleton"></view>
                <view class="stats-skeleton"></view>
              </view>
            </view>
          </view>
        </view>
        <view class="water-right">
          <view class="note-card-skeleton" v-for="n in Math.floor(count/2)" :key="n">
            <view class="image-skeleton"></view>
            <view class="note-info-skeleton">
              <view class="note-title-skeleton"></view>
              <view class="note-meta-skeleton">
                <view class="username-skeleton"></view>
                <view class="stats-skeleton"></view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 附近页骨架屏 -->
    <view v-else-if="type === 'nearby'" class="nearby-skeleton">
      <view class="waterfall-skeleton">
        <view class="water-left">
          <view class="note-card-skeleton" v-for="n in Math.ceil(count/2)" :key="n">
            <view class="image-skeleton"></view>
            <view class="note-info-skeleton">
              <view class="note-title-skeleton"></view>
              <view class="note-meta-skeleton">
                <view class="username-skeleton"></view>
                <view class="stats-skeleton"></view>
              </view>
            </view>
          </view>
        </view>
        <view class="water-right">
          <view class="note-card-skeleton" v-for="n in Math.floor(count/2)" :key="n">
            <view class="image-skeleton"></view>
            <view class="note-info-skeleton">
              <view class="note-title-skeleton"></view>
              <view class="note-meta-skeleton">
                <view class="username-skeleton"></view>
                <view class="stats-skeleton"></view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'SkeletonScreen',
  props: {
    // 骨架屏类型：follow(关注), discover(发现), nearby(附近)
    type: {
      type: String,
      default: 'follow',
      validator: value => ['follow', 'discover', 'nearby'].includes(value)
    },
    // 骨架屏数量
    count: {
      type: Number,
      default: 5
    },
    // 是否显示图片骨架
    showImages: {
      type: Boolean,
      default: true
    },
    // 图片类型：single(单图), multi(多图)
    imageType: {
      type: String,
      default: 'single',
      validator: value => ['single', 'multi'].includes(value)
    }
  }
}
</script>

<style lang="scss" scoped>
.skeleton-container {
  padding: 20rpx;
}

/* 关注页骨架屏样式 */
.follow-skeleton {
  .follow-skeleton-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.04);
    
    .user-info-skeleton {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;
      
      .avatar-skeleton {
        width: 80rpx;
        height: 80rpx;
        border-radius: 40rpx;
        background: linear-gradient(90deg, #F4EDE2 25%, #EADFCB 50%, #F4EDE2 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        margin-right: 24rpx;
      }
      
      .user-details-skeleton {
        flex: 1;
        
        .username-skeleton {
          width: 120rpx;
          height: 32rpx;
          background: linear-gradient(90deg, #F4EDE2 25%, #EADFCB 50%, #F4EDE2 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 16rpx;
          margin-bottom: 12rpx;
        }
        
        .time-skeleton {
          width: 80rpx;
          height: 24rpx;
          background: linear-gradient(90deg, #F4EDE2 25%, #EADFCB 50%, #F4EDE2 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 12rpx;
        }
      }
    }
    
    .content-skeleton {
      margin-bottom: 20rpx;
      
      .text-line-skeleton {
        height: 28rpx;
        background: linear-gradient(90deg, #F4EDE2 25%, #EADFCB 50%, #F4EDE2 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 14rpx;
        margin-bottom: 12rpx;
        
        &:nth-child(1) { width: 100%; }
        &:nth-child(2) { width: 85%; }
        &:nth-child(3) { width: 60%; }
      }
    }
    
    .image-skeleton {
      margin-bottom: 20rpx;
      
      .single-image-skeleton {
        width: 100%;
        height: 400rpx;
        background: linear-gradient(90deg, #F4EDE2 25%, #EADFCB 50%, #F4EDE2 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 12rpx;
      }
      
      .multi-image-skeleton {
        display: flex;
        gap: 12rpx;
        
        .image-item-skeleton {
          flex: 1;
          height: 200rpx;
          background: linear-gradient(90deg, #F4EDE2 25%, #EADFCB 50%, #F4EDE2 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 12rpx;
        }
      }
    }
    
    .action-bar-skeleton {
      display: flex;
      justify-content: space-around;
      padding-top: 20rpx;
      border-top: 1rpx solid rgba(80,50,30,0.1);
      
      .action-item-skeleton {
        width: 80rpx;
        height: 32rpx;
        background: linear-gradient(90deg, #F4EDE2 25%, #EADFCB 50%, #F4EDE2 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 16rpx;
      }
    }
  }
}

/* 发现页和附近页骨架屏样式 */
.discover-skeleton,
.nearby-skeleton {
  .waterfall-skeleton {
    display: flex;
    gap: 16rpx;
    
    .water-left,
    .water-right {
      flex: 1;
    }
    
    .note-card-skeleton {
      background: #fff;
      border-radius: 16rpx;
      margin-bottom: 16rpx;
      overflow: hidden;
      box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.08);
      
      .image-skeleton {
        width: 100%;
        height: 300rpx;
        background: linear-gradient(90deg, #F4EDE2 25%, #EADFCB 50%, #F4EDE2 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
      }
      
      .note-info-skeleton {
        padding: 20rpx;
        
        .note-title-skeleton {
          width: 100%;
          height: 24rpx;
          background: linear-gradient(90deg, #F4EDE2 25%, #EADFCB 50%, #F4EDE2 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 12rpx;
          margin-bottom: 16rpx;
        }
        
        .note-meta-skeleton {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .username-skeleton {
            width: 80rpx;
            height: 20rpx;
            background: linear-gradient(90deg, #F4EDE2 25%, #EADFCB 50%, #F4EDE2 75%);
            background-size: 200% 100%;
            animation: skeleton-loading 1.5s infinite;
            border-radius: 10rpx;
          }
          
          .stats-skeleton {
            width: 60rpx;
            height: 20rpx;
            background: linear-gradient(90deg, #F4EDE2 25%, #EADFCB 50%, #F4EDE2 75%);
            background-size: 200% 100%;
            animation: skeleton-loading 1.5s infinite;
            border-radius: 10rpx;
          }
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
  .follow-skeleton-item {
    padding: 20rpx;
  }
  
  .note-card-skeleton {
    margin-bottom: 12rpx;
  }
}
</style>
