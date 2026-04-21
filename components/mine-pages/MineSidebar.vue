<template>
  <u-popup
    :show="show"
    mode="left"
    customStyle="width:525rpx;background-color: #f5f5f5;"
    @close="handleClose"
    :safeAreaInsetTop="safeAreaInsetTop"
    :safeAreaInsetBottom="false"
    :overlayStyle="{ 'touch-action': 'none' }"
  >
    <scroll-view
      scroll-y="true"
      enable-flex
      :style="{ height: screenHeight * 2 + 'px' }"
      style="font-size: 28rpx; background-color: #f5f5f5"
    >
      <view style="height: 60rpx"></view>
      <!-- 顶部卡片：用户资料和内容管理 -->
      <view class="menu-card">
        <!-- <view class="menu-item" @click="handleEditData">
					<u-icon name="edit-pen" size="28"></u-icon>
					<view class="menu-text">编辑资料</view>
				</view> -->
        <view class="menu-item" @click="handleDrafts">
          <u-icon name="folder" size="28"></u-icon>
          <view class="menu-text">我的草稿</view>
        </view>
        <view class="menu-item" @click="handleMyComments">
          <u-icon name="chat" size="28"></u-icon>
          <view class="menu-text">我的评论</view>
        </view>
        <view class="menu-item" @click="handleBrowseRecord">
          <u-icon name="clock" size="28"></u-icon>
          <view class="menu-text">浏览记录</view>
        </view>
      </view>

      <!-- 中间卡片：购物和交易 -->
      <view class="menu-card">
        <view class="menu-item" @click="handleGoodProduct">
          <u-icon name="gift" size="28"></u-icon>
          <view class="menu-text">闲置好物</view>
        </view>
        <view class="menu-item" @click="handleOrders">
          <u-icon name="order" size="28"></u-icon>
          <view class="menu-text">我的订单</view>
        </view>
        <view class="menu-item" @click="handleShoppingCart">
          <u-icon name="bag" size="28"></u-icon>
          <view class="menu-text">心愿单</view>
        </view>
        <view class="menu-item" @click="handleAddressManagement">
          <u-icon name="map" size="28"></u-icon>
          <view class="menu-text">收货地址</view>
        </view>
      </view>

      <!-- AI 功能卡片 -->
      <view class="menu-card">
        <view class="menu-item" @click="handleAIAssistant">
          <u-icon name="chat" size="28"></u-icon>
          <view class="menu-text">AI助手</view>
        </view>
        <view class="menu-item" @click="handleChatHistory">
          <u-icon name="file-text" size="28"></u-icon>
          <view class="menu-text">对话记录</view>
        </view>
      </view>

      <!-- 底部卡片：关于信息 -->
      <view class="menu-card">
        <view class="menu-item" @click="handleAbout">
          <u-icon name="info-circle" size="28"></u-icon>
          <view class="menu-text">爱宠社</view>
        </view>
      </view>

      <view style="height: 200rpx; background-color: #f5f5f5"></view>
    </scroll-view>
    <view
      style="
        position: fixed;
        bottom: 30rpx;
        left: 0;
        right: 0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 525rpx;
        background-color: #f5f5f5;
        padding: 20rpx 60rpx;
        box-sizing: border-box;
        font-size: 25rpx;
        color: #727171;
        z-index: 10;
      "
    >
      <view style="text-align: -webkit-center" @click="handleScan">
        <view
          style="
            display: flex;
            justify-content: center;
            align-items: center;
            width: 70rpx;
            height: 70rpx;
            background-color: #e8e8e8;
            border-radius: 50%;
            text-align: -webkit-center;
          "
        >
          <u-icon name="scan" size="24" color="#666"></u-icon>
        </view>
        <view style="text-align: center; margin-top: 5rpx">扫一扫</view>
      </view>
      <view style="text-align: -webkit-center" @click="handleHelpService">
        <view
          style="
            display: flex;
            justify-content: center;
            align-items: center;
            width: 70rpx;
            height: 70rpx;
            background-color: #e8e8e8;
            border-radius: 50%;
            text-align: -webkit-center;
          "
        >
          <u-icon name="chat" size="24" color="#666"></u-icon>
        </view>
        <view style="text-align: center; margin-top: 5rpx">帮助与客服</view>
      </view>
      <view style="text-align: -webkit-center" @click="handleSetting">
        <view
          style="
            display: flex;
            justify-content: center;
            align-items: center;
            width: 70rpx;
            height: 70rpx;
            background-color: #e8e8e8;
            border-radius: 50%;
          "
        >
          <u-icon name="setting" size="24" color="#666"></u-icon>
        </view>
        <view style="text-align: center; margin-top: 5rpx">设置</view>
      </view>
    </view>
  </u-popup>
</template>

<script>
export default {
  name: 'MineSidebar',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    screenHeight: {
      type: [Number, String],
      default: 0
    }
  },
  computed: {
    // 微信小程序顶部已是系统栏「首页/爱宠社」，不再为安全区预留，避免侧边栏顶部大块空白
    safeAreaInsetTop() {
      // #ifdef MP-WEIXIN
      return false
      // #endif
      // #ifndef MP-WEIXIN
      return true
      // #endif
    }
  },
  methods: {
    handleClose() {
      this.$emit('close');
    },
    handleEditData() {
      this.$emit('edit-data');
    },
    handleDrafts() {
      this.$emit('drafts');
    },
    handleMyComments() {
      this.$emit('my-comments');
    },
    handleBrowseRecord() {
      this.$emit('browse-record');
    },
    handleGoodProduct() {
      this.$emit('good-product');
    },
    handleOrders() {
      this.$emit('orders');
    },
    handleShoppingCart() {
      this.$emit('shopping-cart');
    },
    handleAddressManagement() {
      this.$emit('address-management');
    },
    handleAbout() {
      this.$emit('about');
    },
    handleScan() {
      this.$emit('scan');
    },
    handleSetting() {
      this.$emit('setting');
    },
    handleAIAssistant() {
      this.$emit('ai-assistant');
    },
    handleChatHistory() {
      this.$emit('chat-history');
    },
    handleHelpService() {
      this.$emit('help-service');
    }
  }
};
</script>

<style lang="scss" scoped>
/* 侧边栏菜单卡片样式 */
.menu-card {
  background-color: #ffffff;
  border-radius: 20rpx;
  margin: 0 30rpx 20rpx 30rpx;
  box-shadow: 0 2rpx 8rpx 0 rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 20rpx 40rpx;
}

.menu-item:active {
  background-color: #f5f5f5;
}

.menu-text {
  margin-left: 20rpx;
  line-height: 60rpx;
  color: #333;
  font-size: 28rpx;
}
</style>











