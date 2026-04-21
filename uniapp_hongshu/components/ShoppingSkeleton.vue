<template>
  <view class="shopping-skeleton-container">
    <!-- Banner骨架屏 -->
    <view class="banner-skeleton">
      <view class="banner-item-skeleton"></view>
    </view>
    
    <!-- 分类tabs骨架屏 -->
    <view class="tabs-skeleton">
      <view class="tab-item-skeleton" v-for="n in 5" :key="n"></view>
    </view>
    
    <!-- 商品列表骨架屏 -->
    <view class="products-skeleton">
      <view class="waterfall-skeleton">
        <view class="water-left">
          <view class="product-card-skeleton" v-for="n in Math.ceil(count/2)" :key="n">
            <!-- 商品图片骨架 -->
            <view class="product-image-skeleton"></view>
            
            <!-- 位置标签骨架 -->
            <view class="location-tag-skeleton"></view>
            
            <!-- 视频播放按钮骨架 -->
            <view class="video-play-skeleton"></view>
            
            <!-- 商品标题骨架 -->
            <view class="product-title-skeleton"></view>
            
            <!-- 价格区域骨架 -->
            <view class="price-row-skeleton">
              <view class="current-price-skeleton"></view>
              <view class="original-price-skeleton"></view>
              <view class="post-type-skeleton"></view>
            </view>
            
            <!-- 用户信息骨架 -->
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
        
        <view class="water-right">
          <view class="product-card-skeleton" v-for="n in Math.floor(count/2)" :key="n">
            <!-- 商品图片骨架 -->
            <view class="product-image-skeleton"></view>
            
            <!-- 位置标签骨架 -->
            <view class="location-tag-skeleton"></view>
            
            <!-- 视频播放按钮骨架 -->
            <view class="video-play-skeleton"></view>
            
            <!-- 商品标题骨架 -->
            <view class="product-title-skeleton"></view>
            
            <!-- 价格区域骨架 -->
            <view class="price-row-skeleton">
              <view class="current-price-skeleton"></view>
              <view class="original-price-skeleton"></view>
              <view class="post-type-skeleton"></view>
            </view>
            
            <!-- 用户信息骨架 -->
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
    </view>
  </view>
</template>

<script>
export default {
  name: 'ShoppingSkeleton',
  props: {
    // 商品骨架屏数量
    count: {
      type: Number,
      default: 8
    },
    // 是否显示Banner骨架
    showBanner: {
      type: Boolean,
      default: true
    },
    // 是否显示分类tabs骨架
    showTabs: {
      type: Boolean,
      default: true
    }
  }
}
</script>

<style lang="scss" scoped>
.shopping-skeleton-container {
  background: #f5f5f5;
  min-height: 100vh;
}

/* Banner骨架屏 */
.banner-skeleton {
  width: 100vw;
  height: 260rpx;
  margin: 0 auto;
  border-radius: 15rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  
  .banner-item-skeleton {
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
  }
}

/* 分类tabs骨架屏 */
.tabs-skeleton {
  width: 100%;
  height: 40px;
  background-color: #fff;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  margin-bottom: 20rpx;
  
  .tab-item-skeleton {
    width: 120rpx;
    height: 60rpx;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: 30rpx;
    margin-right: 20rpx;
    
    &:nth-child(1) { width: 100rpx; }
    &:nth-child(2) { width: 140rpx; }
    &:nth-child(3) { width: 120rpx; }
    &:nth-child(4) { width: 160rpx; }
    &:nth-child(5) { width: 110rpx; }
  }
}

/* 商品列表骨架屏 */
.products-skeleton {
  padding: 0 20rpx;
  
  .waterfall-skeleton {
    display: flex;
    gap: 16rpx;
    
    .water-left,
    .water-right {
      flex: 1;
    }
    
    .product-card-skeleton {
      background: #fff;
      border-radius: 20rpx;
      margin-bottom: 24rpx;
      overflow: hidden;
      box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.08);
      border: 1rpx solid #f0f0f0;
      position: relative;
      
      /* 商品图片骨架 */
      .product-image-skeleton {
        width: 100%;
        height: 300rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
      }
      
      /* 位置标签骨架 */
      .location-tag-skeleton {
        position: absolute;
        bottom: 180rpx;
        left: 8rpx;
        width: 80rpx;
        height: 40rpx;
        background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s infinite;
        border-radius: 50rpx;
      }
      
      /* 视频播放按钮骨架 */
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
      
      /* 商品标题骨架 */
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
      
      /* 价格区域骨架 */
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
      
      /* 用户信息骨架 */
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
  .product-card-skeleton {
    margin-bottom: 16rpx;
  }
  
  .tabs-skeleton {
    padding: 0 10rpx;
    
    .tab-item-skeleton {
      margin-right: 10rpx;
    }
  }
}
</style>
