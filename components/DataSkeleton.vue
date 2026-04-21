<template>
  <view class="data-skeleton-container">
    <!-- 笔记网格骨架屏 -->
    <view v-if="type === 'notes'" class="notes-skeleton">
      <view class="notes-grid-skeleton">
        <view class="note-card-skeleton" v-for="n in count" :key="n">
          <!-- 置顶标签 -->
          <view v-if="showPinned" class="pinned-tag-skeleton"></view>
          
          <!-- 笔记图片 -->
          <view class="note-image-skeleton"></view>
          
          <!-- 浏览量 -->
          <view v-if="showViews" class="view-count-skeleton"></view>
          
          <!-- 视频播放按钮 -->
          <view v-if="showVideoPlay" class="video-play-skeleton"></view>
          
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
    
    <!-- 商品网格骨架屏 -->
    <view v-else-if="type === 'products'" class="products-skeleton">
      <view class="products-grid-skeleton">
        <view class="product-card-skeleton" v-for="n in count" :key="n">
          <!-- 商品图片 -->
          <view class="product-image-skeleton"></view>
          
          <!-- 位置标签 -->
          <view class="location-tag-skeleton"></view>
          
          <!-- 视频播放按钮 -->
          <view class="video-play-skeleton"></view>
          
          <!-- 商品标题 -->
          <view class="product-title-skeleton"></view>
          
          <!-- 价格区域 -->
          <view class="price-row-skeleton">
            <view class="current-price-skeleton"></view>
            <view class="original-price-skeleton"></view>
            <view class="post-type-skeleton"></view>
          </view>
          
          <!-- 用户信息 -->
          <view class="user-info-skeleton">
            <view class="user-avatar-skeleton"></view>
            <view class="user-name-skeleton"></view>
            <view class="action-area-skeleton">
              <view class="star-skeleton"></view>
              <view class="like-count-skeleton"></view>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 关注页列表骨架屏 -->
    <view v-else-if="type === 'follow'" class="follow-skeleton">
      <view class="follow-list-skeleton">
        <view class="follow-card-skeleton" v-for="n in count" :key="n">
          <!-- 用户信息 -->
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
          <view v-if="showImages" class="image-skeleton">
            <view v-if="imageType === 'single'" class="single-image-skeleton"></view>
            <view v-else class="multi-image-skeleton">
              <view class="image-item-skeleton" v-for="i in 3" :key="i"></view>
            </view>
          </view>
          
          <!-- 操作栏骨架 -->
          <view class="action-bar-skeleton">
            <view class="action-item-skeleton" v-for="i in 3" :key="i"></view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'DataSkeleton',
  props: {
    // 骨架屏类型：notes(笔记), products(商品), follow(关注)
    type: {
      type: String,
      default: 'notes',
      validator: value => ['notes', 'products', 'follow'].includes(value)
    },
    // 骨架屏数量
    count: {
      type: Number,
      default: 6
    },
    // 是否显示置顶标签
    showPinned: {
      type: Boolean,
      default: true
    },
    // 是否显示浏览量
    showViews: {
      type: Boolean,
      default: true
    },
    // 是否显示视频播放按钮
    showVideoPlay: {
      type: Boolean,
      default: true
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
.data-skeleton-container {
  padding: 20rpx;
}

/* 笔记网格骨架屏 */
.notes-skeleton {
  .notes-grid-skeleton {
    display: flex;
    flex-wrap: wrap;
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

/* 商品网格骨架屏 */
.products-skeleton {
  .products-grid-skeleton {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    
    .product-card-skeleton {
      width: calc(50% - 10rpx);
      background: #fff;
      border-radius: 20rpx;
      overflow: hidden;
      position: relative;
      box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.08);
      
      /* 商品图片 */
      .product-image-skeleton {
        width: 100%;
        height: 300rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
      }
      
      /* 位置标签 */
      .location-tag-skeleton {
        position: absolute;
        bottom: 180rpx;
        left: 10rpx;
        width: 80rpx;
        height: 40rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 50rpx;
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
      }
      
      /* 商品标题 */
      .product-title-skeleton {
        margin: 20rpx;
        height: 24rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 12rpx;
        
        &::after {
          content: '';
          display: block;
          margin-top: 8rpx;
          height: 24rpx;
          width: 70%;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 12rpx;
        }
      }
      
      /* 价格区域 */
      .price-row-skeleton {
        display: flex;
        align-items: center;
        margin: 0 20rpx 8rpx 20rpx;
        
        .current-price-skeleton {
          width: 80rpx;
          height: 32rpx;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 16rpx;
          margin-right: 12rpx;
        }
        
        .original-price-skeleton {
          width: 60rpx;
          height: 24rpx;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 12rpx;
          margin-right: 20rpx;
        }
        
        .post-type-skeleton {
          width: 50rpx;
          height: 30rpx;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 14rpx;
        }
      }
      
      /* 用户信息 */
      .user-info-skeleton {
        display: flex;
        align-items: center;
        padding: 20rpx;
        position: relative;
        
        .user-avatar-skeleton {
          width: 44rpx;
          height: 44rpx;
          border-radius: 50%;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          margin-right: 20rpx;
        }
        
        .user-name-skeleton {
          flex: 1;
          height: 20rpx;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 10rpx;
          max-width: 120rpx;
        }
        
        .action-area-skeleton {
          position: absolute;
          right: 20rpx;
          display: flex;
          align-items: center;
          
          .star-skeleton {
            width: 36rpx;
            height: 36rpx;
            background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
            background-size: 200% 100%;
            animation: skeleton-loading 1.5s infinite;
            border-radius: 50%;
            margin-right: 8rpx;
          }
          
          .like-count-skeleton {
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
}

/* 关注页列表骨架屏 */
.follow-skeleton {
  .follow-list-skeleton {
    .follow-card-skeleton {
      background: #fff;
      border-radius: 16rpx;
      padding: 24rpx;
      margin-bottom: 20rpx;
      box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.04);
      
      /* 用户信息 */
      .user-info-skeleton {
        display: flex;
        align-items: center;
        margin-bottom: 20rpx;
        
        .avatar-skeleton {
          width: 80rpx;
          height: 80rpx;
          border-radius: 40rpx;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          margin-right: 24rpx;
        }
        
        .user-details-skeleton {
          flex: 1;
          
          .username-skeleton {
            width: 120rpx;
            height: 32rpx;
            background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
            background-size: 200% 100%;
            animation: skeleton-loading 1.5s infinite;
            border-radius: 16rpx;
            margin-bottom: 12rpx;
          }
          
          .time-skeleton {
            width: 80rpx;
            height: 24rpx;
            background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
            background-size: 200% 100%;
            animation: skeleton-loading 1.5s infinite;
            border-radius: 12rpx;
          }
        }
      }
      
      /* 内容 */
      .content-skeleton {
        margin-bottom: 20rpx;
        
        .text-line-skeleton {
          height: 28rpx;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 14rpx;
          margin-bottom: 12rpx;
          
          &:nth-child(1) { width: 100%; }
          &:nth-child(2) { width: 85%; }
          &:nth-child(3) { width: 60%; }
        }
      }
      
      /* 图片 */
      .image-skeleton {
        margin-bottom: 20rpx;
        
        .single-image-skeleton {
          width: 100%;
          height: 400rpx;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
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
            background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
            background-size: 200% 100%;
            animation: skeleton-loading 1.5s infinite;
            border-radius: 12rpx;
          }
        }
      }
      
      /* 操作栏 */
      .action-bar-skeleton {
        display: flex;
        justify-content: space-around;
        padding-top: 20rpx;
        border-top: 1rpx solid #f0f0f0;
        
        .action-item-skeleton {
          width: 80rpx;
          height: 32rpx;
          background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
          background-size: 200% 100%;
          animation: skeleton-loading 1.5s infinite;
          border-radius: 16rpx;
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
  .data-skeleton-container {
    padding: 10rpx;
  }
  
  .notes-grid-skeleton,
  .products-grid-skeleton {
    gap: 10rpx;
  }
}
</style>
