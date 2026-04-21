<template>
  <view>
    <!-- 顶部栏与状态栏一体：整条固定，避免留白和滑动穿模 -->
    <view
      class="publish-nav-wrap"
      :style="{ height: (statusBarHeight + 44) + 'px' }"
    >
      <view class="publish-nav-row">
        <view class="publish-nav-left" @click="goBack">
          <u-icon name="arrow-left" size="22" color="#333"></u-icon>
        </view>
        <text class="publish-nav-title">发布闲置</text>
      </view>
    </view>
    <view :style="{ height: (statusBarHeight + 44) + 'px' }" class="publish-nav-placeholder"></view>
    <view style="padding: 30rpx">
      <view>
        <scroll-view v-if="type === 1" scroll-x enable-flex="true">
          <view style="display: flex">
            <block v-for="(item, index) in tempFilePaths" v-bind:key="index">
              <view style="position: relative">
                <u--image
                  :showLoading="true"
                  :src="item"
                  mode="aspectFill"
                  height="240rpx"
                  width="240rpx"
                  :radius="10"
                  style="margin: 0 10rpx"
                ></u--image>
                <!-- 删除按钮 -->
                <view
                  style="
                    position: absolute;
                    top: 0;
                    right: 10rpx;
                    background-color: #7d7d7d;
                    padding: 5rpx;
                    border-bottom-left-radius: 50%;
                    border-top-right-radius: 5rpx;
                  "
                >
                  <u-icon
                    name="close"
                    size="12"
                    color="#f3f3f2"
                    @click="deleteImage(index)"
                  ></u-icon>
                </view>
                <!-- 封面标记/清除；当已存在封面时隐藏其它图片的“设为封面” -->
                <view
                  v-if="coverPicture === item"
                  @click="clearCover"
                  style="
                    position: absolute;
                    left: 14rpx;
                    top: 14rpx;
                    background: #3d8af5;
                    color: #fff;
                    font-size: 20rpx;
                    border-radius: 20rpx;
                    padding: 4rpx 12rpx;
                  "
                >
                  封面
                </view>
                <view
                  v-else-if="!coverPicture"
                  @click="setCover(item)"
                  style="
                    position: absolute;
                    left: 14rpx;
                    top: 14rpx;
                    background: rgba(0, 0, 0, 0.45);
                    color: #fff;
                    font-size: 20rpx;
                    border-radius: 20rpx;
                    padding: 4rpx 12rpx;
                  "
                >
                  设为封面
                </view>
              </view>
            </block>
            <view v-if="tempFilePaths.length < 9">
              <view
                @click="insertImage"
                style="
                  width: 240rpx;
                  height: 240rpx;
                  border-radius: 10px;
                  display: flex;
                  justify-content: center;
                  align-items: center;
                  background-color: #f3f3f2;
                  margin: 0 10rpx;
                "
              >
                <u-icon name="plus" size="28"></u-icon>
              </view>
            </view>
          </view>
        </scroll-view>
        <scroll-view v-else-if="type === 2" scroll-x enable-flex="true">
          <!-- #ifndef MP-WEIXIN -->
          <!-- 隐藏的video和canvas用于提取视频帧（仅H5和APP端使用） -->
          <view
            style="
              position: fixed;
              left: -9999px;
              top: -9999px;
              width: 400px;
              height: 300px;
              overflow: hidden;
            "
          >
            <video
              id="videoFrameVideo"
              :src="tempFilePaths[0]"
              style="width: 400px; height: 300px"
              @loadedmetadata="onVideoMetadataLoaded"
              @seeked="onVideoSeeked"
              @error="onVideoError"
            ></video>
            <canvas
              id="videoFrameCanvas"
              canvas-id="videoFrameCanvas"
              style="width: 400px; height: 300px"
            ></canvas>
          </view>
          <view style="display: none">
            <video
              id="video"
              :src="tempFilePaths[0]"
              controls
              style="width: 100%"
              @fullscreenchange="fullscreenchange"
            ></video>
          </view>
          <!-- #endif -->
          <view style="display: flex">
            <view @click="openVideoFullScreen" style="position: relative">
              <video
                :src="tempFilePaths[0]"
                :poster="coverPicture || ''"
                :controls="false"
                :show-center-play-btn="false"
                :show-fullscreen-btn="false"
                :show-mute-btn="false"
                :show-play-btn="false"
                :enable-progress-gesture="false"
                muted
                autoplay
                loop
                style="
                  width: 240rpx;
                  height: 240rpx;
                  border-radius: 10px;
                  background-color: #000;
                  margin: 0 10rpx;
                  object-fit: cover;
                "
              ></video>
              <!-- 替换视频按钮（与图片删除角标风格一致） -->
              <view
                @click.stop="replaceVideo"
                style="
                  position: absolute;
                  top: 0;
                  right: 10rpx;
                  background-color: #7d7d7d;
                  padding: 5rpx;
                  border-bottom-left-radius: 50%;
                  border-top-right-radius: 5rpx;
                "
              >
                <u-icon name="close" size="12" color="#f3f3f2"></u-icon>
              </view>
            </view>
            <view>
              <view v-if="coverPicture != ''" style="position: relative">
                <u--image
                  :src="coverPicture"
                  mode="aspectFill"
                  height="240rpx"
                  width="240rpx"
                  :radius="10"
                  style="margin: 0 10rpx"
                ></u--image>
                <!-- 封面标记，与图片类型一致 -->
                <view
                  style="
                    position: absolute;
                    left: 14rpx;
                    top: 14rpx;
                    background: #3d8af5;
                    color: #fff;
                    font-size: 20rpx;
                    border-radius: 20rpx;
                    padding: 4rpx 12rpx;
                  "
                >
                  封面
                </view>
                <!-- 删除按钮风格与图片类型一致 -->
                <view
                  @click="deleteCoverPicture"
                  style="
                    position: absolute;
                    top: 0;
                    right: 10rpx;
                    background-color: #7d7d7d;
                    padding: 5rpx;
                    border-bottom-left-radius: 50%;
                    border-top-right-radius: 5rpx;
                  "
                >
                  <u-icon name="close" size="12" color="#f3f3f2"></u-icon>
                </view>
              </view>
              <view v-else>
                <view
                  @click="insertCoverPicture"
                  style="
                    width: 240rpx;
                    height: 240rpx;
                    border-radius: 10px;
                    background-color: #f3f3f2;
                    margin: 0 10rpx;
                    display: flex;
                    flex-direction: column;
                    justify-content: center;
                    align-items: center;
                  "
                >
                  <u-icon name="plus-circle" size="28"></u-icon>
                  <view style="font-size: 23rpx; color: #2b2b2b">添加封面</view>
                </view>
              </view>
            </view>
          </view>
          <!-- 预览视频 -->
          <view
            style="
              background-color: #f3f3f2;
              margin-top: 20rpx;
              border-radius: 10px;
              padding: 10rpx 20rpx;
              display: inline-flex;
              margin-left: 10rpx;
            "
            @click="openVideoFullScreen"
          >
            <u-icon name="play-right-fill" color="#2b2b2b"></u-icon>
            <view style="font-size: 23rpx; color: #2b2b2b">预览视频</view>
          </view>
        </scroll-view>
        <!-- 全屏视频预览弹层 -->
        <u-popup
          :show="showVideoFull"
          mode="bottom"
          :overlayOpacity="1"
          :safeAreaInsetBottom="true"
          :round="0"
          @close="showVideoFull = false"
        >
          <view
            style="
              width: 750rpx;
              height: 100vh;
              background: #000;
              display: flex;
              flex-direction: column;
            "
          >
            <view
              style="
                height: 88rpx;
                display: flex;
                align-items: center;
                padding: 0 24rpx;
                color: #fff;
              "
            >
              <u-icon
                name="arrow-left"
                color="#fff"
                size="22"
                @click="showVideoFull = false"
              ></u-icon>
              <view style="margin-left: 12rpx; font-size: 32rpx">预览视频</view>
            </view>
            <view style="flex: 1; display: flex; align-items: center; justify-content: center">
              <video
                :src="tempFilePaths[0]"
                controls
                autoplay
                style="width: 100%; height: 100%; object-fit: contain; background: #000"
              ></video>
            </view>
          </view>
        </u-popup>
        <u-popup
          :show="showAddTopic"
          :round="10"
          mode="center"
          @close="showAddTopic = false"
          :overlayOpacity="0.3"
          :safeAreaInsetBottom="true"
        >
          <view style="width: 570rpx">
            <view style="padding: 20rpx; text-align: center">
              <view style="font-size: 35rpx">添加话题</view>
              <view style="font-size: 25rpx; color: #949495; margin-top: 10rpx">
                最多不超过12字
              </view>
            </view>
            <view
              style="
                margin: 30rpx;
                margin-top: 15rpx;
                padding: 20rpx;
                background-color: #f3f3f2;
                border-radius: 20rpx;
              "
            >
              <u-input
                placeholder="请输入话题"
                v-model="topicname"
                :focus="showAddTopic"
                border="none"
                clearable
                maxlength="12"
                color="#2b2b2b"
              ></u-input>
            </view>
            <u-line></u-line>
            <view style="padding: 30rpx; display: flex">
              <view
                style="
                  flex: 1;
                  text-align: center;
                  border-right-style: solid;
                  border-right-color: #e5e4e6;
                  border-right-width: 0.5rpx;
                "
              >
                <text style="font-size: 35rpx; color: #949495" @click="cancelAddTopic">取消</text>
              </view>
              <view style="flex: 1; text-align: center">
                <text style="font-size: 35rpx; color: #3d8af5" @click="addTopic">添加</text>
              </view>
            </view>
          </view>
        </u-popup>
        <u-popup :show="showAttenUser" :round="10" mode="bottom" @close="showAttenUser = false">
          <!-- 横向滑动选择关注用户，样式与图文发布页保持一致 -->
          <view style="padding: 30rpx 30rpx 40rpx 30rpx">
            <view v-if="attentionUser.list.length > 0">
              <scroll-view
                scroll-x
                style="white-space: nowrap; width: 100%;"
                @scrolltolower="getAttentionUser"
              >
                <view
                  v-for="(item, index) in attentionUser.list"
                  :key="item.userId || index"
                  style="
                    display: inline-flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    width: 140rpx;
                    margin-right: 32rpx;
                  "
                  @click="addUser(item)"
                >
                  <image
                    :src="item.avatarUrl"
                    style="
                      height: 96rpx;
                      width: 96rpx;
                      border-radius: 50%;
                      margin-bottom: 12rpx;
                    "
                  ></image>
                  <text
                    style="
                      font-size: 26rpx;
                      color: #2b2b2b;
                      max-width: 140rpx;
                      text-align: center;
                      overflow: hidden;
                      text-overflow: ellipsis;
                      white-space: nowrap;
                    "
                  >
                    {{ item.nickname }}
                  </text>
                </view>
              </scroll-view>
            </view>
            <view
              v-else
              style="display: flex; justify-content: center; align-items: center; height: 200rpx"
            >
              <view style="font-size: 25rpx; color: #afafb0">暂无关注的用户</view>
            </view>
          </view>
        </u-popup>
        <u-popup :show="showEmoji" :round="10" mode="bottom" @close="showEmoji = false">
          <scroll-view :style="{ height: keyboardHeight + 'px' }" scroll-y>
            <view
              style="
                display: grid;
                padding: 20rpx;
                grid-template-columns: repeat(8, 1fr);
                gap: 16rpx;
                text-align: center;
              "
            >
              <block v-for="(em, index) in emojiList" v-bind:key="index">
                <view
                  @click="addEmoji(em)"
                  style="font-size: 48rpx; line-height: 100rpx; height: 100rpx"
                >
                  {{ em }}
                </view>
              </block>
            </view>
          </scroll-view>
        </u-popup>
        <!-- 标签编辑弹窗 -->
        <u-popup
          :show="showTags"
          :round="12"
          mode="bottom"
          @close="showTags = false"
          :safeAreaInsetBottom="true"
        >
          <view style="padding: 28rpx 32rpx 40rpx; background: #ffffff">
            <view style="text-align: center; font-size: 34rpx; font-weight: 600; color: #2b2b2b">
              添加标签
            </view>
            <view style="display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 24rpx">
              <view
                v-for="(t, i) in tags"
                :key="i"
                style="
                  padding: 10rpx 18rpx;
                  border-radius: 30rpx;
                  background: #3d8af5;
                  font-size: 24rpx;
                  color: #ffffff;
                  display: flex;
                  align-items: center;
                "
              >
                {{ t }}
                <u-icon
                  name="close"
                  size="14"
                  color="#ffffff"
                  style="margin-left: 8rpx"
                  @click="removeTag(i)"
                ></u-icon>
              </view>
            </view>
            <view style="display: flex; margin-top: 20rpx">
              <u--input
                v-model="tagInput"
                placeholder="搜索标签或输入自定义标签"
                border="surround"
                clearable
                style="
                  flex: 1;
                  background: #f7f7f8;
                  border-radius: 12rpx;
                  padding: 8rpx 12rpx;
                  font-size: 26rpx;
                "
                @confirm="addTag"
              ></u--input>
              <view
                @click="addTag"
                style="
                  margin-left: 12rpx;
                  padding: 0 24rpx;
                  border-radius: 12rpx;
                  background: #3d8af5;
                  color: #fff;
                  display: flex;
                  align-items: center;
                  font-size: 26rpx;
                "
              >
                添加
              </view>
            </view>
            <view style="margin-top: 26rpx">
              <view style="font-size: 28rpx; color: #2b2b2b; margin-bottom: 16rpx">热门标签</view>
              <view style="display: flex; flex-wrap: wrap; gap: 16rpx">
                <view
                  v-for="(ht, hi) in hotTags"
                  :key="hi"
                  @click="toggleHotTag(ht)"
                  :style="
                    (tags || []).includes(ht)
                      ? 'padding: 10rpx 18rpx;border-radius: 30rpx;background:#3d8af5;color:#ffffff;font-size: 24rpx;'
                      : 'padding: 10rpx 18rpx;border-radius: 30rpx;background:#f1f2f5;color:#6e6e73;font-size: 24rpx;border:1rpx solid #e6e7eb;'
                  "
                >
                  {{ ht }}
                </view>
              </view>
              <view style="text-align: center; color: #8e8e93; font-size: 22rpx; margin-top: 20rpx">
                最多可选择5个标签，点击标签可添加或移除
              </view>
            </view>
          </view>
        </u-popup>
        <view>
          <view style="margin-top: 20rpx; padding: 10rpx; display: flex; align-items: center">
            <text style="color: #ff3b30; font-size: 32rpx; margin-right: 8rpx">*</text>
            <u--input
              v-model="title"
              :fontSize="18"
              caret-color:
              #3d8af5;
              placeholder="请输入标题"
              style="font-size: 30rpx; letter-spacing: 1px; flex: 1"
              :maxlength="20"
              showWordLimit
              clearable
              border="none"
            ></u--input>
            <!-- AI生成按钮 -->
            <view
              @click="handleAIGenerate"
              :class="['ai-generate-btn', { loading: aiGenerating }]"
              style="
                margin-left: 20rpx;
                padding: 12rpx 24rpx;
                background: #3d8af5;
                border-radius: 40rpx;
                display: flex;
                align-items: center;
                box-shadow: 0 4rpx 12rpx rgba(61, 138, 245, 0.3);
              "
              :style="{ opacity: aiGenerating ? 0.6 : 1 }"
            >
              <text v-if="!aiGenerating" style="font-size: 24rpx; color: #ffffff">✨</text>
              <u-loading-icon v-else mode="spinner" color="#ffffff" size="20"></u-loading-icon>
              <text style="font-size: 24rpx; color: #ffffff; margin-left: 8rpx">AI 配文</text>
            </view>
          </view>
          <u-divider></u-divider>
          <lsj-edit
            ref="lsjEdit"
            placeholder="请输入正文"
            :maxCount="1000"
            @onReady="editReady"
          ></lsj-edit>
          <view style="display: flex; margin-top: 10rpx">
            <view
              @click="showAddTopic = true"
              style="
                padding: 12rpx 20rpx;
                background-color: #f5f6f7;
                border: 1rpx solid #eee;
                border-radius: 40rpx;
                display: flex;
                align-items: center;
                box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
              "
            >
              <text style="font-size: 28rpx; color: #3d8af5">#</text>
              <view style="font-size: 26rpx; color: #2b2b2b; margin-left: 12rpx">添加话题</view>
            </view>
            <view
              @click="openAttenUser"
              style="
                padding: 12rpx 20rpx;
                background-color: #f5f6f7;
                border: 1rpx solid #eee;
                border-radius: 40rpx;
                display: flex;
                align-items: center;
                margin-left: 20rpx;
                box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
              "
            >
              <text style="font-size: 28rpx; color: #3d8af5">@</text>
              <view style="font-size: 26rpx; color: #2b2b2b; margin-left: 12rpx">用户</view>
            </view>
            <view
              @click="showEmoji = true"
              style="
                padding: 12rpx 20rpx;
                background-color: #f5f6f7;
                border: 1rpx solid #eee;
                border-radius: 40rpx;
                display: flex;
                align-items: center;
                margin-left: 20rpx;
                box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
              "
            >
              <text style="font-size: 28rpx">😊</text>
              <view style="font-size: 26rpx; color: #2b2b2b; margin-left: 12rpx">表情</view>
            </view>
          </view>
        </view>
      </view>
    </view>
    <u-popup
      :show="showCategory"
      :round="10"
      mode="bottom"
      @close="showCategory = false"
      :safeAreaInsetBottom="true"
    >
      <view style="height: 580rpx; background-color: #ffffff">
        <view style="padding: 12rpx 28rpx 6rpx 28rpx; text-align: center">
          <view style="font-size: 32rpx; font-weight: 600; color: #2b2b2b">选择分类</view>
        </view>
        <view
          style="
            margin: 12rpx 28rpx 0 28rpx;
            background: #fff7dc;
            border-radius: 10rpx;
            padding: 12rpx 14rpx;
            display: flex;
            align-items: center;
          "
        >
          <u-icon name="info-circle" color="#C28B00" size="14"></u-icon>
          <text style="margin-left: 8rpx; font-size: 20rpx; color: #8a6d00">标注</text>
          <text style="color: #ff3b30; font-size: 20rpx">*</text>
          <text style="font-size: 20rpx; color: #8a6d00">为必填项</text>
        </view>
        <scroll-view scroll-y style="height: 500rpx">
          <view
            style="
              display: grid;
              grid-template-columns: repeat(3, 1fr);
              gap: 12rpx;
              padding: 20rpx 28rpx 28rpx 28rpx;
            "
          >
            <view
              v-for="(cat, idx) in categoryList"
              :key="cat.id"
              @click="chooseCategoryItem(Math.floor(idx / 3) + 1, (idx % 3) + 1)"
              style="
                height: 84rpx;
                border-radius: 14rpx;
                display: flex;
                justify-content: center;
                align-items: center;
                background: #ffffff;
                border: 2rpx solid #e6e6e6;
                box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.03);
                font-size: 26rpx;
                color: #2b2b2b;
              "
              :style="
                categoryId == cat.id ? 'border-color:#3d8af5;color:#3d8af5;background:#FFF5F6' : ''
              "
            >
              {{ cat.categoryName }}
            </view>
          </view>
        </scroll-view>
      </view>
    </u-popup>
    <!-- 价格与发货方式（移动至标签前） -->
    <view
      style="margin-top: 10rpx; padding: 10rpx; width: 50%; min-width: 300rpx; margin-left: 20rpx"
    >
      <view style="font-size: 28rpx; color: #2b2b2b; margin-bottom: 12rpx">
        <text style="color: #ff3b30; margin-right: 4rpx">*</text>
        <text>商品售价</text>
      </view>
      <u-number-box
        v-model="sellPrice"
        :integer="false"
        :step="1"
        :min="0"
        :max="999999"
        :inputWidth="120"
        :decimalLength="2"
      ></u-number-box>
    </view>
    <view
      style="margin-top: 10rpx; padding: 10rpx; width: 50%; min-width: 300rpx; margin-left: 20rpx"
    >
      <view style="font-size: 28rpx; color: #2b2b2b; margin-bottom: 12rpx">
        <text style="color: #ff3b30; margin-right: 4rpx">*</text>
        <text>商品原价</text>
      </view>
      <u-number-box
        v-model="originalPrice"
        :integer="false"
        :step="1"
        :min="0"
        :max="999999"
        :inputWidth="120"
        :decimalLength="2"
      ></u-number-box>
    </view>
    <u-cell
      v-if="postType === null"
      icon="car"
      @click="showPostTypeSheet = true"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    >
      <template #title>
        <view style="display: inline-flex; align-items: flex-start">
          <text style="color: #ff3b30; margin-right: 4rpx">*</text>
          <text>发货方式</text>
        </view>
      </template>
    </u-cell>
    <u-cell
      v-else
      icon="car"
      iconStyle="color: #3d8af5"
      titleStyle="color: #3d8af5"
      :title="postTypeLabel()"
      @click="showPostTypeSheet = true"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    ></u-cell>
    <u-action-sheet
      :show="showPostTypeSheet"
      :actions="postTypeActions"
      @select="onChoosePostType"
      @close="showPostTypeSheet = false"
    ></u-action-sheet>
    <!-- <u-cell v-if="tags.length===0" icon="tags" title="添加标签" @click="openTags" :isLink="true" arrow-direction="right"
			:border="false" size="large"></u-cell>
		<u-cell v-else icon="tags" iconStyle="color: #3d8af5" titleStyle="color: #3d8af5" :title="tags.join('、')"
			@click="openTags" :isLink="true" arrow-direction="right" :border="false" size="large"></u-cell> -->
    <u-cell
      v-if="categoryName == ''"
      icon="grid"
      @click="chooseCategory"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    >
      <template #title>
        <view style="display: inline-flex; align-items: flex-start">
          <text style="color: #ff3b30; margin-right: 4rpx">*</text>
          <text>选择分类</text>
        </view>
      </template>
    </u-cell>
    <u-cell
      v-else
      icon="grid"
      iconStyle="color: #3d8af5"
      titleStyle="color: #3d8af5"
      :title="categoryName"
      @click="chooseCategory"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    ></u-cell>
    <u-cell
      v-if="address == ''"
      icon="map"
      @click="chooseAddress"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    >
      <template #title>
        <view style="display: inline-flex; align-items: flex-start">
          <text style="color: #ff3b30; margin-right: 4rpx">*</text>
          <text>添加地点</text>
        </view>
      </template>
    </u-cell>
    <u-cell
      v-else
      icon="map"
      iconStyle="color: #2ca9e1"
      titleStyle="color: #2ca9e1"
      :title="address"
      @click="chooseAddress"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    ></u-cell>
    <u-cell
      v-if="authority === 0"
      icon="lock-open"
      title="公开可见"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
      @click="chooseAuth"
    ></u-cell>
    <u-cell
      v-if="authority === 1"
      icon="lock"
      title="仅自己可见"
      iconStyle="color: #3d8af5"
      titleStyle="color: #3d8af5"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
      @click="chooseAuth"
    ></u-cell>
    <view style="height: 180rpx"></view>
    <view
      v-if="showBottom"
      style="
        display: flex;
        position: fixed;
        bottom: 0;
        width: 750rpx;
        height: 160rpx;
        align-items: center;
        z-index: 99;
        background-color: #ffffff;
      "
    >
      <view
        @click="saveDraftTip"
        style="margin-left: 30rpx; flex: 1; display: flex; justify-content: flex-start"
      >
        <view
          style="
            width: 100rpx;
            height: 100rpx;
            border-radius: 20rpx;
            background: #f5f6f7;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.06);
          "
        >
          <u-icon name="bookmark" size="36" color="#6e6e73"></u-icon>
          <view
            style="
              margin-top: 4rpx;
              padding: 2rpx 10rpx;
              border-radius: 16rpx;
              background: #ffffff;
              border: 1rpx solid #e6e7eb;
              font-size: 18rpx;
              color: #6e6e73;
            "
          >
            存草稿
          </view>
        </view>
      </view>
      <view
        @click="publishProduct"
        style="
          margin-left: 30rpx;
          margin-right: 30rpx;
          border-radius: 50px;
          flex: 5;
          background-color: #3d8af5;
          height: 100rpx;
          display: flex;
          align-items: center;
          justify-content: center;
        "
      >
        <view style="color: #ffffff; font-size: 37rpx; letter-spacing: 1px">发布商品</view>
      </view>
    </view>
  </view>
</template>

<script>
import { getAttentionList } from '@/apis/user_service';
import editBtns from '@/uni_modules/lsj-edit/components/lsj-edit/edit-btns/edit-btns.vue';
import { emojiList } from '@/utils/emojiUtil.js';
import { addNote, getNotesCategoryList, uploadImages } from '@/apis/notes_service.js';
import { getIdleCategoryList } from '@/apis/idles_service';
import { baseUrl, AMAP_KEY } from '@/.env.js';
import { uploadFile } from '@/utils/fileUtil.js';
import { addProduct } from '@/apis/idles_service.js';
import { generateContentFromFiles, generateContentFromVideo, generateContentFromUrls } from '@/apis/ai_service.js';
import weixinNavBar from '@/utils/weixinNavBar.js';
export default {
  mixins: [weixinNavBar],
  components: {
    editBtns
  },
  data() {
    return {
      statusBarHeight: 0,
      isUpdate: false,
      tableId: '',
      edit: null,
      type: '',
      tempFilePaths: [],
      coverPicture: '',
      videoB: '',
      title: '',
      content: '',
      topicname: '',
      categoryName: '',
      categoryId: '',
      categoryList: [],
      showCategory: false,
      showAddTopic: false,
      showTags: false,
      showAttenUser: false,
      attentionUser: {
        list: [],
        page: 1,
        pageSize: 10,
        status: 'loading',
        isLoading: false,
        isNoMore: false
      },
      keyboardHeight: 302,
      showEmoji: false,
      emojiList: [],
      tags: [],
      tagInput: '',
      address: '',
      province: '',
      city: '',
      district: '',
      latitude: '',
      longitude: '',
      authority: 0,
      showBottom: true,
      hotTags: [],
      showVideoFull: false,
      sellPrice: 0,
      originalPrice: 0,
      postType: null,
      showPostTypeSheet: false,
      postTypeActions: [
        {
          name: '邮寄',
          value: 0
        },
        {
          name: '自提',
          value: 1
        }
      ],
      aiGenerating: false, // AI生成中状态
      uploadedFileUrls: [], // 已上传的文件URL（用于避免重复上传）
      uploadedCoverUrl: '', // 已上传的封面URL
      uploadedVideoUrl: '' // 已上传的视频URL
    };
  },
  methods: {
    goBack() {
      uni.showActionSheet({
        alertText: '是否保存草稿',
        itemList: ['保存草稿', '不保存'],
        success: res => {
          if (res.tapIndex === 0) {
            this.saveDraft();
          } else {
            uni.navigateTo({ url: '/pages/index/index' });
          }
        }
      });
    },
    postTypeLabel() {
      if (this.postType === null || this.postType === undefined) {
        return '';
      }
      return this.postType === 1 ? '自提' : '邮寄';
    },
    onChoosePostType(action) {
      this.postType = action.value;
      this.showPostTypeSheet = false;
    },
    replaceVideo() {
      uni.showActionSheet({
        itemList: ['替换视频', '取消'],
        success: res => {
          if (res.tapIndex === 0) {
            uni.chooseVideo({
              sourceType: ['album', 'camera'],
              maxDuration: 60, // 最长60秒
              success: r => {
                // 检查视频大小，不能超过20M
                const maxSize = 20 * 1024 * 1024; // 20M in bytes
                if (r.size > maxSize) {
                  uni.showToast({
                    title: '视频大小不能超过20M',
                    icon: 'none',
                    duration: 2000
                  });
                  return;
                }

                const path = r.tempFilePath;
                // tempFilePaths[0] 作为视频路径
                this.$set(this.tempFilePaths, 0, path);

                // #ifdef MP-WEIXIN
                // 微信小程序：使用系统返回的缩略图
                if (r.thumbTempFilePath) {
                  this.coverPicture = r.thumbTempFilePath;
                  console.log('✅ 使用系统缩略图作为封面:', r.thumbTempFilePath);
                } else {
                  // 如果没有缩略图，清空封面让用户手动添加
                  this.coverPicture = '';
                  uni.showToast({
                    title: '请手动添加封面',
                    icon: 'none'
                  });
                }
                // #endif

                // #ifndef MP-WEIXIN
                // 其他平台：替换视频时清除旧封面，重新提取新视频的封面
                this.coverPicture = '';
                // 自动提取第一帧作为封面
                this.extractVideoCover(path, true);
                // #endif
              }
            });
          }
        }
      });
    },
    openVideoFullScreen() {
      const src = this.tempFilePaths && this.tempFilePaths[0];
      if (!src) {
        uni.showToast({
          title: '请先选择视频',
          icon: 'none'
        });
        return;
      }
      this.showVideoFull = true;
    },
    setCover(img) {
      this.coverPicture = img;
      const idx = this.tempFilePaths.indexOf(img);
      if (idx > 0) {
        const arr = this.tempFilePaths.slice();
        arr.splice(idx, 1);
        arr.unshift(img);
        this.tempFilePaths = arr;
      }
    },
    clearCover() {
      this.coverPicture = '';
    },
    openTags() {
      this.showTags = true;
      this.fetchHotTags();
    },
    addTag() {
      const val = (this.tagInput || '').trim();
      if (!val) {
        return;
      }
      if (this.tags.length >= 5) {
        uni.showToast({
          title: '最多只能选择5个标签',
          icon: 'none'
        });
        return;
      }
      if (this.tags.includes(val)) {
        this.tagInput = '';
        return;
      }
      this.tags.push(val);
      this.tagInput = '';
    },
    removeTag(index) {
      this.tags.splice(index, 1);
    },
    toggleHotTag(name) {
      const idx = this.tags.indexOf(name);
      if (idx > -1) {
        this.tags.splice(idx, 1);
        return;
      }
      if (this.tags.length >= 5) {
        uni.showToast({
          title: '最多只能选择5个标签',
          icon: 'none'
        });
        return;
      }
      this.tags.push(name);
    },
    fetchHotTags() {
      if (this.hotTags && this.hotTags.length) {
        return;
      }
      import('@/apis/tag_service.js').then(m => {
        m.getHotTagList({
          page: 1,
          pageSize: 30
        }).then(res => {
          if (res.code === 200) {
            const list = Array.isArray(res.data?.records)
              ? res.data.records
              : Array.isArray(res.data)
              ? res.data
              : [];
            this.hotTags = list.map(it => it.name || it.title || it.tagName || it);
          }
        });
      });
    },
    // 选择分类
    chooseCategory() {
      if (this.categoryList.length != 0) {
        this.showCategory = true;
        return;
      }
      getIdleCategoryList()
        .then(res => {
          if (res.code == 200) {
            // 兼容后端类目树：name -> categoryName
            const normalize = nodes => {
              return (nodes || []).map(n => ({
                ...n,
                categoryName: n.name || n.title || n.categoryName
              }));
            };
            this.categoryList = normalize(res.data);
            console.log(this.categoryList);
            this.showCategory = true;
          } else {
            uni.showToast({
              title: '获取分类失败',
              icon: 'none'
            });
          }
        })
        .catch(err => {
          uni.showToast({
            title: '获取分类失败',
            icon: 'none'
          });
        });
    },
    chooseCategoryItem(rowIndex, colIndex) {
      const item = this.categoryList[(rowIndex - 1) * 3 + colIndex - 1];
      this.categoryName = item.categoryName;
      this.categoryId = item.id; // 二级分类
      this.parentCategoryId = item.parentId; // 一级分类
      this.showCategory = false;
    },
    publishProduct() {
      // 1. 校验标题不能为空
      if (!this.title || this.title.trim() === '') {
        uni.showToast({
          title: '请输入标题',
          icon: 'none'
        });
        return;
      }

      // 2. 校验图文类型必须有图片
      if (this.type === 1) {
        if (!this.tempFilePaths || this.tempFilePaths.length === 0) {
          uni.showToast({
            title: '请至少添加一张图片',
            icon: 'none'
          });
          return;
        }

        // 3. 如果没有设置封面图，自动使用第一张图片作为封面
        if (!this.coverPicture && this.tempFilePaths.length > 0) {
          this.coverPicture = this.tempFilePaths[0];
        }
      }

      // 4. 校验视频类型必须有视频和封面图
      if (this.type === 2) {
        if (!this.tempFilePaths || this.tempFilePaths.length === 0) {
          uni.showToast({
            title: '请选择视频',
            icon: 'none'
          });
          return;
        }

        if (!this.coverPicture) {
          uni.showToast({
            title: '请设置视频封面图',
            icon: 'none'
          });
          return;
        }
      }

      // 5. 校验商品售价不能为空
      if (!this.sellPrice || Number(this.sellPrice) <= 0) {
        uni.showToast({
          title: '请输入商品售价',
          icon: 'none'
        });
        return;
      }

      // 6. 校验商品原价不能为空
      if (!this.originalPrice || Number(this.originalPrice) <= 0) {
        uni.showToast({
          title: '请输入商品原价',
          icon: 'none'
        });
        return;
      }

      // 7. 校验发货方式不能为空
      if (this.postType === null || this.postType === undefined) {
        uni.showToast({
          title: '请选择发货方式',
          icon: 'none'
        });
        return;
      }

      // 8. 校验分类不能为空
      if (!this.categoryId || this.categoryId === '') {
        uni.showToast({
          title: '请选择分类',
          icon: 'none'
        });
        return;
      }

      // 9. 校验地点不能为空
      if (!this.address || this.address.trim() === '') {
        uni.showToast({
          title: '请添加地点',
          icon: 'none'
        });
        return;
      }

      // 10. 校验价格不能为负数
      if (Number(this.sellPrice) < 0 || Number(this.originalPrice) < 0) {
        uni.showToast({
          title: '价格不能为负数',
          icon: 'none'
        });
        return;
      }
      uni
        .showModal({
          title: '提示',
          content: '确认发布商品吗？',
          confirmText: '确定',
          cancelText: '取消'
        })
        .then(res => {
          if (res.confirm) {
            uni.showLoading({
              title: '发布中...',
              mask: true
            });
            this.edit.getContents().then(editRes => {
              // 图片笔记未手动设置封面时，默认取第一张作为封面，不占用9张数量
              if (
                String(this.type) === '1' &&
                !this.coverPicture &&
                this.tempFilePaths.length > 0
              ) {
                this.coverPicture = this.tempFilePaths[0];
              }
              // 将HTML内容转换为纯文本（去除HTML标签，保留纯文本内容）
              let contentText = this.stripHtmlToText(editRes.html || '');
              let productDTO = {
                title: this.title,
                content: contentText,
                cover: '',
                noteCoverHeight: this.noteCoverHeight || null,
                // 分类：选中的分类赋给 cpid（父级），二级为 cid
                cpid: this.categoryId,
                cid: this.parentCategoryId,
                urls: [],
                count: this.tempFilePaths.length,
                productType: String(this.type),
                postType: this.postType,
                province: this.province || '',
                city: this.city || '',
                district: this.district || '',
                address: this.address || '',
                // 价格按后端要求为字符串，保留两位小数
                price: Number(this.sellPrice || 0).toFixed(2),
                originalPrice: Number(this.originalPrice || 0).toFixed(2)
              };
              console.log('🛍️ 发布商品 - 省市区信息:', {
                province: productDTO.province,
                city: productDTO.city,
                district: productDTO.district,
                address: productDTO.address
              });
              const rid = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
              (async () => {
                try {
                  // 直接调用addProduct方法，一步完成文件上传和商品保存
                  const resData = await addProduct({
                    requestId: rid,
                    noteData: productDTO,
                    coverFile: this.coverPicture || null,
                    uploadFiles: this.tempFilePaths || []
                  });
                  uni.hideLoading();
                  if (resData.code == 200) {
                    uni.showToast({
                      title: '发布成功',
                      icon: 'none',
                      duration: 1500
                    });

                    // 延迟跳转，让用户看到成功提示
                    setTimeout(() => {
                      // 自定义 TabBar 场景使用 reLaunch 以确保跳转
                      uni.reLaunch({
                        url: '/pages/index/index'
                      });
                    }, 1500);
                  } else {
                    uni.showToast({
                      title: resData.msg == '' ? '发布失败' : resData.msg,
                      icon: 'none'
                    });
                  }
                } catch (err) {
                  uni.hideLoading();
                  uni.showToast({
                    title: '发布失败',
                    icon: 'none'
                  });
                }
              })();
            });
          }
        });
    },
    saveDraftTip() {
      // 查询所有草稿
      this.$sqliteUtil.SqlSelect(`select * from draft_notes`).then(res => {
        console.log(res);
      });
      this.$showModal({
        title: '提示',
        content: '确认保存笔记至草稿箱吗？',
        align: 'left', // 对齐方式 left/center/right
        cancelText: '取消', // 取消按钮的文字
        cancelColor: '#3d8af5', // 取消按钮颜色
        confirmText: '确定', // 确认按钮文字
        confirmColor: '#3d8af5', // 确认按钮颜色
        showCancel: true // 是否显示取消按钮，默认为 true
      }).then(res => {
        // 将页面数据存入数据库
        this.saveDraft();
      });
    },
    saveDraft() {
      // 保存草稿
      this.edit.getContents().then(res => {
        console.log(res);
        this.content = res.html;
        if (this.coverPicture == '' && this.type == 1) {
          this.coverPicture = this.tempFilePaths[0];
        }
        if (this.isUpdate) {
          let sql1 = `delete from draft_notes where id = ${this.tableId}`;
          this.$sqliteUtil.SqlExecute(sql1).then(res => {
            console.log(res);
          });
        }
        setTimeout(() => {
          let sql2 = `insert into draft_notes (title,content,type,coverPicture,address,latitude,longitude,authority,tempFilePaths,updateTime) values ('${
            this.title
          }','${this.content}','${this.type}','${this.coverPicture}','${this.address}','${
            this.latitude
          }','${this.longitude}','${this.authority}','${JSON.stringify(
            this.tempFilePaths
          )}',datetime('now','localtime'))`;
          this.$sqliteUtil
            .SqlExecute(sql2)
            .then(res => {
              uni.showToast({
                title: '保存成功',
                icon: 'none'
              });
              // 自定义 TabBar 场景使用 reLaunch 以确保跳转
              uni.reLaunch({
                url: '/pages/index/index'
              });
            })
            .catch(err => {
              uni.showToast({
                title: '保存失败',
                icon: 'none'
              });
            });
        }, 500);
      });
    },
    chooseAuth() {
      uni.showActionSheet({
        itemList: ['公开可见', '仅自己可见'],
        success: res => {
          if (res.tapIndex == 0) {
            this.authority = 0;
          } else {
            this.authority = 1;
          }
        }
      });
    },
    chooseAddress() {
      const afterChoose = res => {
        console.log('📍 选择地址返回数据:', res);
        // 在H5环境下，res.address通常包含完整地址信息（省市区+详细地址）
        // res.name通常是地点名称
        const fullAddress = res.address || '';
        const placeName = res.name || '';

        this.latitude = res.latitude;
        this.longitude = res.longitude;

        // 反向地理解析，获取省/市/区
        if (typeof uni.reverseGeocoder === 'function') {
          try {
            uni.reverseGeocoder({
              location: {
                latitude: res.latitude,
                longitude: res.longitude
              },
              success: r => {
                const comp = (r && r.result && r.result.addressComponent) || {};
                this.province = comp.province || '';
                this.city = comp.city || comp.province || '';
                this.district = comp.district || '';
                // 从详细地址中提取详细地址部分（去掉省市区）
                if (fullAddress && this.province) {
                  // 如果地址中包含了省市区信息，则从详细地址中去除
                  let detailedAddr = fullAddress;
                  if (this.province && detailedAddr.startsWith(this.province)) {
                    detailedAddr = detailedAddr.substring(this.province.length).trim();
                  }
                  if (this.city && detailedAddr.startsWith(this.city)) {
                    detailedAddr = detailedAddr.substring(this.city.length).trim();
                  }
                  if (this.district && detailedAddr.startsWith(this.district)) {
                    detailedAddr = detailedAddr.substring(this.district.length).trim();
                  }
                  this.address = detailedAddr || placeName;
                } else {
                  this.address = placeName;
                }
                console.log('✅ 获取到省市区信息:', {
                  province: this.province,
                  city: this.city,
                  district: this.district,
                  address: this.address
                });
              },
              fail: () => {
                console.warn('⚠️ 反向地理解析失败，使用字符串解析');
                // 使用完整地址进行解析
                this.parseAddressToPCD(fullAddress || placeName, placeName);
              }
            });
          } catch (e) {
            console.warn('⚠️ 反向地理解析异常，使用字符串解析:', e);
            // 使用完整地址进行解析
            this.parseAddressToPCD(fullAddress || placeName, placeName);
          }
        } else {
          // 无反地理接口时（如H5环境），使用高德地图API获取省市区
          console.warn('⚠️ 无反地理接口，尝试使用高德地图API');
          this.getLocationByAMap(res.latitude, res.longitude, fullAddress, placeName);
        }
      };
      const openChooser = () => {
        uni.chooseLocation({
          success: afterChoose
        });
      };
      // #ifdef MP-WEIXIN
      uni.getSetting({
        success: st => {
          if (st.authSetting && st.authSetting['scope.userLocation']) {
            openChooser();
          } else {
            uni.authorize({
              scope: 'scope.userLocation',
              success: openChooser,
              fail: () => {
                uni.showModal({
                  title: '需要位置权限',
                  content: '请在设置中开启位置权限以选择地点',
                  confirmText: '去设置',
                  success: m => {
                    if (m.confirm) uni.openSetting({});
                  }
                });
              }
            });
          }
        }
      });
      // #endif
      // #ifndef MP-WEIXIN
      openChooser();
      // #endif
    },
    // 使用高德地图API获取省市区信息（H5环境）
    async getLocationByAMap(latitude, longitude, fullAddress, placeName) {
      try {
        // 使用导入的AMAP_KEY
        if (!AMAP_KEY) {
          console.warn('⚠️ 高德地图Key未配置，使用字符串解析');
          this.parseAddressToPCD(fullAddress || placeName, placeName);
          return;
        }

        // 由于WebService API的Key权限问题，在H5环境下使用JSONP方式调用
        // 或者使用JavaScript API（需要加载高德地图JS SDK）
        // 这里先尝试JSONP方式（仅在H5环境下）
        // #ifdef H5
        if (typeof window !== 'undefined' && typeof document !== 'undefined') {
          try {
            // JSONP方式调用高德地图逆地理编码API
            const callbackName =
              'amap_regeo_callback_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
            const jsonpUrl = `https://restapi.amap.com/v3/geocode/regeo?location=${longitude},${latitude}&key=${AMAP_KEY}&extensions=base&output=json&callback=${callbackName}`;

            const responseData = await new Promise((resolve, reject) => {
              // 创建回调函数
              window[callbackName] = data => {
                delete window[callbackName];
                // 清理script标签
                const script = document.querySelector(`script[src*="${callbackName}"]`);
                if (script && script.parentNode) {
                  script.parentNode.removeChild(script);
                }
                resolve(data);
              };

              // 创建script标签
              const script = document.createElement('script');
              script.src = jsonpUrl;
              script.onerror = () => {
                delete window[callbackName];
                document.head.removeChild(script);
                reject(new Error('JSONP请求失败'));
              };
              document.head.appendChild(script);

              // 设置超时
              setTimeout(() => {
                if (window[callbackName]) {
                  delete window[callbackName];
                  const timeoutScript = document.querySelector(`script[src*="${callbackName}"]`);
                  if (timeoutScript && timeoutScript.parentNode) {
                    timeoutScript.parentNode.removeChild(timeoutScript);
                  }
                  reject(new Error('JSONP请求超时'));
                }
              }, 5000);
            });

            console.log('📍 高德地图API返回数据:', responseData);

            // 检查响应状态
            if (responseData && responseData.status === '1') {
              const comp = responseData.regeocode?.addressComponent || {};
              this.province = comp.province || '';
              this.city = Array.isArray(comp.city)
                ? comp.city[0]
                : comp.city || comp.province || '';
              this.district = comp.district || '';

              // 提取详细地址
              // 优先使用地点名称（placeName），如果没有地点名称，则使用完整地址去掉省市区后的部分
              if (placeName && placeName.trim()) {
                // 如果有地点名称（如"李家下庄安置区"），直接使用
                this.address = placeName.trim();
              } else if (fullAddress) {
                // 如果没有地点名称，使用完整地址，去掉省市区后的剩余部分
                let detailedAddr = fullAddress;
                if (this.province && detailedAddr.startsWith(this.province)) {
                  detailedAddr = detailedAddr.substring(this.province.length).trim();
                }
                if (this.city && detailedAddr.startsWith(this.city)) {
                  detailedAddr = detailedAddr.substring(this.city.length).trim();
                }
                if (this.district && detailedAddr.startsWith(this.district)) {
                  detailedAddr = detailedAddr.substring(this.district.length).trim();
                }
                this.address = detailedAddr || '';
              } else {
                this.address = '';
              }

              console.log('✅ 通过高德地图API获取到省市区信息:', {
                province: this.province,
                city: this.city,
                district: this.district,
                address: this.address
              });
              return;
            } else {
              console.warn('⚠️ 高德地图API返回失败:', {
                status: responseData?.status,
                info: responseData?.info
              });
            }
          } catch (jsonpError) {
            console.warn('⚠️ 高德地图JSONP调用失败，尝试使用字符串解析:', jsonpError);
          }
        }
        // #endif

        // 如果JSONP也失败或非H5环境，回退到字符串解析
        this.parseAddressToPCD(fullAddress || placeName, placeName);
      } catch (e) {
        console.warn('⚠️ 高德地图API调用异常，使用字符串解析:', e);
        this.parseAddressToPCD(fullAddress || placeName, placeName);
      }
    },
    // 简单从中文地址中切分省/市/区（直辖市/自治区兼容）
    parseAddressToPCD(addr, placeName) {
      if (!addr || typeof addr !== 'string') {
        this.province = '';
        this.city = '';
        this.district = '';
        this.address = placeName || '';
        return;
      }
      const text = addr.replace(/\s+/g, '');
      // 直辖市/特别行政区
      const direct = /(北京市|天津市|上海市|重庆市|香港特别行政区|澳门特别行政区)/;
      const mDirect = text.match(direct);
      if (mDirect) {
        this.province = mDirect[1].replace('特别行政区', '');
        this.city = this.province;
        const rest = text.slice(text.indexOf(mDirect[1]) + mDirect[1].length);
        const dist = rest.match(/([^省市]+?(?:区|县))/);
        this.district = dist ? dist[1] : '';
        // 提取详细地址（去掉省市区后的剩余部分）
        let detailedAddr = rest;
        if (this.district && detailedAddr.startsWith(this.district)) {
          detailedAddr = detailedAddr.substring(this.district.length).trim();
        }
        this.address = detailedAddr || placeName || '';
        return;
      }
      // 省/自治区
      const provMatch = text.match(/(.+?省|.+?自治区)/);
      if (provMatch) {
        this.province = provMatch[1];
        const rest = text.slice(provMatch[0].length);
        const cityMatch = rest.match(/(.+?(?:市|州|盟))/);
        this.city = cityMatch ? cityMatch[1] : '';
        const afterCity = cityMatch ? rest.slice(cityMatch[0].length) : rest;
        const distMatch = afterCity.match(/(.+?(?:区|县))/);
        this.district = distMatch ? distMatch[1] : '';
        // 提取详细地址（去掉省市区后的剩余部分）
        let detailedAddr = afterCity;
        if (this.district && detailedAddr.startsWith(this.district)) {
          detailedAddr = detailedAddr.substring(this.district.length).trim();
        }
        this.address = detailedAddr || placeName || '';
        return;
      }
      // 兜底：只含城市
      const cityOnly = text.match(/(.+?(?:市|州|盟))/);
      if (cityOnly) {
        this.city = cityOnly[1];
        const after = text.slice(cityOnly[0].length);
        const dist = after.match(/(.+?(?:区|县))/);
        this.district = dist ? dist[1] : '';
        this.province = '';
        // 提取详细地址（去掉市区后的剩余部分）
        let detailedAddr = after;
        if (this.district && detailedAddr.startsWith(this.district)) {
          detailedAddr = detailedAddr.substring(this.district.length).trim();
        }
        this.address = detailedAddr || placeName || '';
        return;
      }
      // 实在无法解析省市区时，使用地点名称作为详细地址
      this.province = '';
      this.city = '';
      this.district = '';
      this.address = placeName || addr || '';
    },
    cancelAddTopic() {
      this.topicname = '';
      this.showAddTopic = false;
    },
    openAttenUser() {
      this.showAttenUser = true;
      this.getAttentionUser();
    },
    getAttentionUser() {
      if (this.attentionUser.isNoMore) {
        return;
      }
      this.attentionUser.isLoading = true;
      setTimeout(() => {
        getAttentionList({
          userId: uni.getStorageSync('userInfo').id,
          pageNum: this.attentionUser.page,
          pageSize: this.attentionUser.pageSize
        })
          .then(res => {
            if (res.code == 200) {
              const records = res.data.records || [];
              if (records.length < this.attentionUser.pageSize) {
                this.attentionUser.isNoMore = true;
                this.attentionUser.status = 'nomore';
              }
              // 字段映射
              const mappedList = records.map(item => ({
                userId: item.uid, // 接口是uid，页面用userId
                nickname: item.username, // 接口是username，页面用nickname
                avatarUrl: item.avatar, // 接口是avatar，页面用avatarUrl
                selfIntroduction: item.description || '', // 你接口里没有，后端加上或前端补空
                isAttention: true, // 关注列表默认已关注
                bidirectional: !!item.bidirectional // 互相关注，后端有就用，没有就补false
                // 其它字段按需补充
              }));
              this.attentionUser.list = this.attentionUser.list.concat(mappedList);
              this.attentionUser.page++;
            }
          })
          .finally(() => {
            this.attentionUser.isLoading = false;
          });
      }, 500);
    },
    // 演示发布
    async save() {
      // 获取插入的图片列表
      let imgs = await this.edit.getImages();
      // 判断是否允许提交
      if (!this.edit.textCount && !imgs.length) {
        uni.showToast({
          title: '请录入内容'
        });
      }
      let con = await this.edit.getContents();
      console.log(con);
    },
    // 插入话题
    addTopic() {
      // this.edit.insertCustomHtml(`<span style="color: #e2041b;">#${this.topicname}#</span>`)
      this.edit.addLink({
        prefix: '#',
        suffix: '', // 去掉后缀的#号，只保留前面的#
        name: this.topicname,
        data: {
          topicname: this.topicname
        }
      });
      this.topicname = '';
      this.showAddTopic = false;
    },
    // @某人示例
    addUser(item) {
      this.showAttenUser = false;
      this.edit.addLink({
        prefix: '@',
        name: item.nickname,
        data: {
          userId: item.userId
        }
      });
    },
    addEmoji(emojiChar) {
      // 直接插入 Unicode 表情
      this.edit.emoji(emojiChar);
      this.showEmoji = false;
    },
    // AI生成内容
    async handleAIGenerate() {
      // 检查是否有图片或视频
      if (!this.tempFilePaths || this.tempFilePaths.length === 0) {
        uni.showToast({
          title: '请先上传图片或视频',
          icon: 'none'
        });
        return;
      }

      if (this.aiGenerating) {
        return;
      }

      this.aiGenerating = true;
      // 使用mask: true确保loading在最上层，避免被其他组件覆盖
      // 只显示"AI生成中"，不显示其他加载状态
      uni.showLoading({
        title: 'AI生成中...',
        mask: true
      });

      try {
        let response;

        if (this.type === 2) {
          // 视频模式：优先使用已上传的URL，避免重复上传
          if (this.uploadedFileUrls.length > 0 && this.uploadedFileUrls[0]) {
            const videoUrl = this.uploadedFileUrls[0];
            console.log('✅ 使用之前已上传的视频URL，跳过上传:', videoUrl);
            
            // 使用已上传的URL调用AI生成接口
            response = await generateContentFromUrls({
              videoUrl: videoUrl,
              style: '小红书笔记',
              contentType: 'idle', // 闲置商品类型
              category: this.categoryName || undefined
            });
          } else {
            // 视频模式：先上传视频获取URL
            response = await generateContentFromVideo(this.tempFilePaths[0], {
              style: '小红书笔记',
              contentType: 'idle', // 闲置商品类型
              category: this.categoryName || undefined
            });
            
            // 保存上传后的URL（generateContentFromVideo内部会上传并返回URL）
            // 注意：generateContentFromVideo返回的response.data可能包含videoUrl
            // 这里需要根据实际返回结构来保存URL
            if (response && response.code === 200 && response.data) {
              // 如果API返回了videoUrl，保存它
              // 注意：generateContentFromVideo内部已经上传了，但我们需要从响应中获取URL
              // 或者从generateContentFromVideo的实现中获取
            }
          }
        } else {
          // 图片模式：优先使用已上传的URL，避免重复上传
          if (this.uploadedFileUrls.length > 0 && this.uploadedFileUrls.length === this.tempFilePaths.length) {
            // 如果之前已经上传过，且数量匹配，直接使用已上传的URL
            console.log('✅ 使用之前已上传的图片URL，跳过上传:', this.uploadedFileUrls);
            
            // 使用已上传的URL调用AI生成接口
            response = await generateContentFromUrls({
              imageUrls: this.uploadedFileUrls,
              style: '小红书笔记',
              contentType: 'idle', // 闲置商品类型
              category: this.categoryName || undefined
            });
          } else {
            // 图片模式：先上传图片获取URL
            const uploadedUrls = await uploadImages(this.tempFilePaths);
            
            // 验证URL格式（必须是http://或https://开头）
            const validUrls = uploadedUrls.filter(url => url && (url.startsWith('http://') || url.startsWith('https://')))
            if (validUrls.length === 0) {
              throw new Error('上传成功但未获取到有效的图片URL，请检查上传服务配置')
            }
            if (validUrls.length !== uploadedUrls.length) {
              console.warn('部分URL格式无效，已过滤:', uploadedUrls.filter(url => !url || (!url.startsWith('http://') && !url.startsWith('https://'))))
            }
            
            console.log('✅ 图片上传成功，有效URL数量:', validUrls.length, 'URLs:', validUrls)
            
            // 保存上传后的URL，避免发布时重复上传
            this.uploadedFileUrls = validUrls;
            
            // 如果有封面，也上传封面
            if (this.coverPicture) {
              try {
                const uploadedCover = await new Promise((resolve, reject) => {
                  uni.uploadFile({
                    url: baseUrl + '/file/upload',
                    filePath: this.coverPicture,
                    name: 'file',
                    header: {
                      'accessToken': uni.getStorageSync('uniapp_token') || '',
                      'Authorization': 'Bearer ' + (uni.getStorageSync('uniapp_token') || '')
                    },
                    success: (res) => {
                      try {
                        const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
                        if (data && data.code === 200) {
                          resolve(data.data?.url || data.data);
                        } else {
                          reject(new Error(data?.msg || '上传失败'));
                        }
                      } catch (e) {
                        reject(e);
                      }
                    },
                    fail: reject
                  });
                });
                this.uploadedCoverUrl = uploadedCover;
              } catch (e) {
                console.warn('封面上传失败，将在发布时上传:', e);
              }
            } else if (validUrls.length > 0) {
              // 没有封面但有图片，使用第一张作为封面
              this.uploadedCoverUrl = validUrls[0];
            }
            
            // 使用上传后的URL调用AI生成接口
            response = await generateContentFromUrls({
              imageUrls: validUrls,
              style: '小红书笔记',
              contentType: 'idle', // 闲置商品类型
              category: this.categoryName || undefined
            });
          }
        }

        if (response && response.code === 200 && response.data) {
          const data = response.data;

          // 填充标题（取第一个标题，如果标题列表存在）
          if (data.titles && data.titles.length > 0) {
            const title = data.titles[0];
            this.title = title.length > 20 ? title.substring(0, 20) : title;
          }

          // 填充正文内容
          if (data.content && this.edit) {
            // 将内容设置为纯文本，lsj-edit会自动处理
            const contentText = data.content;
            
            // 先隐藏loading，避免与editor组件内部状态冲突
            uni.hideLoading();
            
            // 使用nextTick确保在DOM更新后再设置内容
            this.$nextTick(() => {
              try {
                // 使用tool方法直接调用setContents，避免ready方法可能触发的加载状态
                // 通过tool('setContents')设置内容，不会触发ready的初始化逻辑
                if (this.edit.tool && this.edit.ctx) {
                  // 先失焦，避免设置内容后自动聚焦并触发键盘
                  this.edit.tool('blur');
                  
                  // 使用tool方法设置内容（静默设置，不触发任何加载状态）
                  this.edit.tool('setContents', {
                    html: contentText
                  });
                  
                  // 设置内容后，延迟将光标移动到内容末尾，并确保不聚焦
                  setTimeout(() => {
                    try {
                      // 将光标移动到末尾，但不聚焦（避免触发键盘）
                      if (this.edit && this.edit.ctx) {
                        const length = contentText.length;
                        // 使用setSelectionRange将光标移动到末尾
                        this.edit.ctx.setSelectionRange({
                          start: length,
                          end: length
                        });
                        // 立即失焦，避免触发键盘
                        this.edit.ctx.blur();
                      }
                    } catch (e) {
                      // 如果设置光标位置失败，忽略错误
                      console.warn('设置光标位置失败:', e);
                    }
                  }, 150);
                } else if (this.edit.ready) {
                  // 如果tool方法不存在，使用ready方法作为备选
                  // 先失焦
                  if (this.edit.tool) {
                    this.edit.tool('blur');
                  }
                  this.edit.ready(contentText);
                  
                  // 延迟失焦，避免触发键盘
                  setTimeout(() => {
                    try {
                      if (this.edit && this.edit.ctx) {
                        this.edit.ctx.blur();
                      }
                    } catch (e) {
                      console.warn('失焦失败:', e);
                    }
                  }, 150);
                }
              } catch (e) {
                console.error('设置内容失败:', e);
                // 如果设置失败，使用ready方法作为备选
                if (this.edit.ready) {
                  this.edit.ready(contentText);
                }
              }
            });
          } else {
            // 如果没有内容需要设置，隐藏loading
            uni.hideLoading();
          }

          uni.showToast({
            title: 'AI配文已生成',
            icon: 'success'
          });
        } else {
          // 如果响应失败，隐藏loading
          uni.hideLoading();
          uni.showToast({
            title: response?.msg || '生成失败，请稍后重试',
            icon: 'none'
          });
        }
      } catch (error) {
        uni.hideLoading();
        console.error('AI生成失败:', error);
        uni.showToast({
          title: error?.message || '生成失败，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.aiGenerating = false;
      }
    },
    editReady(edit) {
      // 将富文本对象存放到当前页面，便于后续直接操作
      this.edit = edit;
      this.edit.ready(this.content);
    },
    /**
     * 将HTML内容转换为纯文本
     * 去除所有HTML标签，保留纯文本内容，同时处理话题标签
     */
    stripHtmlToText(html) {
      if (!html) return '';

      // 先处理话题标签：将链接标签转换为纯文本话题标签
      // 匹配 <a href="#{"topicname":"xxx"}">#xxx#</a> 或类似格式
      html = html.replace(/<a[^>]*href=["']#\{[^}]+\}[^>]*>([^<]+)<\/a>/gi, '$1');

      // 去除所有HTML标签
      let text = html.replace(/<[^>]+>/g, '');

      // 解码HTML实体
      text = text
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&amp;/g, '&')
        .replace(/&quot;/g, '"')
        .replace(/&#39;/g, "'")
        .replace(/&#x27;/g, "'")
        .replace(/&nbsp;/g, ' ')
        .replace(/&#x2F;/g, '/');

      // 清理多余的空白字符
      text = text.replace(/\s+/g, ' ').trim();

      return text;
    },
    previewVideo() {
      // 使用系统预览，更统一的全屏体验（H5、App 均可）
      const src = this.tempFilePaths && this.tempFilePaths[0];
      if (!src) {
        uni.showToast({
          title: '请先选择视频',
          icon: 'none'
        });
        return;
      }
      if (uni.previewMedia) {
        uni.previewMedia({
          sources: [
            {
              url: src,
              type: 'video'
            }
          ],
          current: 0
        });
      } else {
        // 低版本降级为全屏播放
        this.videoB = uni.createVideoContext('video', this);
        this.videoB.requestFullScreen({
          direction: 0
        });
        this.videoB.play();
      }
    },
    fullscreenchange(e) {
      if (!e.detail.fullScreen) {
        this.videoB.pause();
      }
    },
    deleteCoverPicture() {
      this.coverPicture = '';
    },
    // 提取视频封面
    extractVideoCover(videoPath, force = false) {
      if (!videoPath) return;

      // 如果已有封面且不是强制提取，不自动提取
      if (this.coverPicture && !force) {
        return;
      }

      // #ifdef H5
      // H5端使用工具类
      import('@/utils/videoUtil.js').then(module => {
        module
          .extractVideoFrame(videoPath, 0)
          .then(coverPath => {
            this.coverPicture = coverPath;
            console.log('✅ H5端自动提取封面成功');
          })
          .catch(err => {
            console.warn('⚠️ H5端自动提取封面失败，请手动添加:', err);
            // 提取失败不影响使用，用户可以手动添加
          });
      });
      // #endif

      // #ifdef APP-PLUS
      // APP端使用页面组件方式
      this.$nextTick(() => {
        setTimeout(() => {
          import('@/utils/videoUtil.js').then(module => {
            module
              .extractVideoFrameInPage('videoFrameVideo', 'videoFrameCanvas', 0)
              .then(coverPath => {
                this.coverPicture = coverPath;
                console.log('✅ APP端自动提取封面成功');
              })
              .catch(err => {
                console.warn('⚠️ APP端自动提取封面失败，请手动添加:', err);
                // 提取失败不影响使用，用户可以手动添加
              });
          });
        }, 500);
      });
      // #endif

      // #ifdef MP-WEIXIN
      // 微信小程序端不支持canvas截取video帧
      // 应该在chooseVideo时使用thumbTempFilePath作为封面
      console.log('ℹ️ 微信小程序端请使用系统缩略图或手动添加封面');
      // #endif
    },
    // 视频元数据加载完成
    onVideoMetadataLoaded() {
      // 视频加载完成后，跳转到第一帧
      // #ifdef APP-PLUS || MP-WEIXIN
      const videoContext = uni.createVideoContext('videoFrameVideo');
      videoContext.seek(0);
      // #endif
    },
    // 视频跳转完成
    onVideoSeeked() {
      // 视频跳转到第一帧后，开始截图
      if (!this.coverPicture && this.tempFilePaths && this.tempFilePaths[0]) {
        // #ifdef APP-PLUS || MP-WEIXIN
        this.captureVideoFrame();
        // #endif
      }
    },
    // 视频加载错误
    onVideoError(err) {
      console.warn('视频加载失败:', err);
    },
    // 捕获视频帧（APP和小程序端）
    captureVideoFrame() {
      // #ifdef APP-PLUS || MP-WEIXIN
      const videoContext = uni.createVideoContext('videoFrameVideo');
      const canvasContext = uni.createCanvasContext('videoFrameCanvas');

      // 获取视频信息
      const query = uni.createSelectorQuery().in(this);
      query
        .select('#videoFrameVideo')
        .boundingClientRect(rect => {
          if (rect) {
            // 使用canvas截图
            canvasContext.drawImage('#videoFrameVideo', 0, 0, rect.width, rect.height);
            canvasContext.draw(false, () => {
              // 导出canvas为图片
              uni.canvasToTempFilePath(
                {
                  canvasId: 'videoFrameCanvas',
                  success: res => {
                    this.coverPicture = res.tempFilePath;
                  },
                  fail: err => {
                    console.warn('截图失败:', err);
                  }
                },
                this
              );
            });
          }
        })
        .exec();
      // #endif
    },
    insertCoverPicture() {
      uni.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        sourceType: ['album', 'camera'],
        success: res => {
          let tempFilePaths = res.tempFilePaths;
          // #ifdef H5
          // H5 端直接用临时路径
          this.coverPicture = tempFilePaths[0];
          // #endif

          // #ifdef MP-WEIXIN
          // 微信小程序端直接使用临时路径
          this.coverPicture = tempFilePaths[0];
          console.log('✅ 微信小程序选择封面图成功:', this.coverPicture);
          // #endif

          // #ifdef APP-PLUS
          // App 端保存到本地
          uni.saveFile({
            tempFilePath: tempFilePaths[0],
            success: res => {
              this.coverPicture = res.savedFilePath;
              console.log('✅ APP端保存封面图成功:', res.savedFilePath);
            }
          });
          // #endif
        }
      });
    },
    deleteImage(index) {
      // 去除下标为index的图片
      const removed = this.tempFilePaths.splice(index, 1)[0];
      if (this.coverPicture === removed) {
        this.coverPicture = this.tempFilePaths[0] || '';
      }
    },
    insertImage() {
      // 最多插入9个图片（不含封面，封面单独选择）
      if (this.tempFilePaths.length >= 9) {
        uni.showToast({
          title: '最多选择9张图片',
          icon: 'none'
        });
        return;
      }
      uni.chooseImage({
        count: Math.min(9 - this.tempFilePaths.length, 9),
        sizeType: ['original', 'compressed'],
        sourceType: ['album', 'camera'],
        success: res => {
          let tempFilePaths = res.tempFilePaths;
          // #ifdef H5
          // H5 端直接拼接临时路径
          this.tempFilePaths = this.tempFilePaths.concat(tempFilePaths);
          // 首次选择时默认第一张为封面
          if (!this.coverPicture && this.tempFilePaths.length > 0) {
            this.coverPicture = this.tempFilePaths[0];
          }
          // #endif

          // #ifdef MP-WEIXIN
          // 微信小程序端直接使用临时路径（在小程序生命周期内有效）
          this.tempFilePaths = this.tempFilePaths.concat(tempFilePaths);
          // 首次选择时默认第一张为封面
          if (!this.coverPicture && this.tempFilePaths.length > 0) {
            this.coverPicture = this.tempFilePaths[0];
          }
          console.log('✅ 微信小程序选择图片成功:', tempFilePaths.length, '张');
          // #endif

          // #ifdef APP-PLUS
          // App 端保存到本地
          let filePaths = [];
          tempFilePaths.forEach(item => {
            uni.saveFile({
              tempFilePath: item,
              success: res => {
                filePaths.push(res.savedFilePath);
                if (filePaths.length == tempFilePaths.length) {
                  this.tempFilePaths = this.tempFilePaths.concat(filePaths);
                  // 首次选择时默认第一张为封面
                  if (!this.coverPicture && this.tempFilePaths.length > 0) {
                    this.coverPicture = this.tempFilePaths[0];
                  }
                }
              }
            });
          });
          // #endif
        }
      });
    }
  },
  onLoad(options) {
    uni.getSystemInfo({
      success: (res) => {
        this.applyWeixinNavBar(res, 44);
        this.statusBarHeight = res.statusBarHeight ?? 0;
        // #ifdef MP-WEIXIN
        this.navBarTop = this.statusBarHeight + 'px';
        this.navPlaceholderHeight = (this.statusBarHeight + 44) + 'px';
        // #endif
      }
    });
    console.log('🔍 发布闲置页面 onLoad，接收到的参数:', options);
    if (options.update == 0) {
      // 从发布笔记进入
      this.type = Number(options.type);
      console.log('📝 设置类型:', this.type, '(1=图片, 2=视频)');
      
      // #ifdef MP-WEIXIN
      // 微信小程序端：优先从全局存储读取文件路径
      const storedFilePaths = uni.getStorageSync('publish_tempFilePaths');
      if (storedFilePaths && Array.isArray(storedFilePaths) && storedFilePaths.length > 0) {
        this.tempFilePaths = storedFilePaths;
        console.log('✅ 从全局存储读取文件路径:', this.tempFilePaths);
        // 读取后清除存储
        uni.removeStorageSync('publish_tempFilePaths');
        
        // 读取封面图
        const storedCoverPicture = uni.getStorageSync('publish_coverPicture');
        if (storedCoverPicture) {
          this.coverPicture = storedCoverPicture;
          console.log('✅ 从全局存储读取封面图:', this.coverPicture);
          uni.removeStorageSync('publish_coverPicture');
        }
      } else if (options.tempFilePaths) {
        // 如果全局存储没有，尝试从URL参数读取（兼容旧逻辑）
        try {
          this.tempFilePaths = JSON.parse(decodeURIComponent(options.tempFilePaths));
          console.log('✅ 从URL参数读取文件路径:', this.tempFilePaths);
        } catch (e) {
          console.error('解析 tempFilePaths 失败:', e);
          try {
            this.tempFilePaths = JSON.parse(options.tempFilePaths);
          } catch (e2) {
            console.error('直接解析 tempFilePaths 也失败:', e2);
            this.tempFilePaths = [];
            uni.showToast({
              title: '文件路径解析失败',
              icon: 'none'
            });
            return;
          }
        }
      } else {
        console.error('❌ 未找到文件路径（全局存储和URL参数都没有）');
        this.tempFilePaths = [];
        uni.showToast({
          title: '未找到选中的文件',
          icon: 'none'
        });
        return;
      }
      // #endif
      
      // #ifndef MP-WEIXIN
      // 非微信小程序端：从URL参数读取
      if (options.tempFilePaths) {
        try {
          this.tempFilePaths = JSON.parse(decodeURIComponent(options.tempFilePaths));
          console.log('✅ 成功解析 tempFilePaths:', this.tempFilePaths);
        } catch (e) {
          console.error('解析 tempFilePaths 失败:', e);
          try {
            this.tempFilePaths = JSON.parse(options.tempFilePaths);
          } catch (e2) {
            console.error('直接解析 tempFilePaths 也失败:', e2);
            this.tempFilePaths = [];
            uni.showToast({
              title: '文件路径解析失败',
              icon: 'none'
            });
            return;
          }
        }
      } else {
        this.tempFilePaths = [];
      }
      // #endif
      
      // 处理封面图参数（Live Photo 或微信小程序的系统缩略图）
      if (options.coverPicture) {
        this.coverPicture = decodeURIComponent(options.coverPicture);
        console.log('✅ 接收到封面图:', this.coverPicture);
      } else if (this.type === 2 && this.tempFilePaths && this.tempFilePaths[0]) {
        console.log('ℹ️ 视频类型但没有封面图，准备自动提取或提示手动添加');
        // 视频类型且没有封面，自动提取第一帧
        // #ifndef MP-WEIXIN
        // 视频类型且没有封面，自动提取第一帧（仅H5和APP端）
        this.$nextTick(() => {
          setTimeout(() => {
            this.extractVideoCover(this.tempFilePaths[0]);
          }, 500);
        });
        // #endif

        // #ifdef MP-WEIXIN
        // 微信小程序端不自动提取，让用户手动添加或使用系统缩略图
        console.log('ℹ️ 微信小程序端，请手动添加封面或使用系统缩略图');
        // #endif
      }
    } else if (options.update == 1) {
      // 从草稿箱进入
      this.isUpdate = true;
      this.tableId = options.tableId;
      let id = options.tableId;
      let sql = `select * from draft_notes where id=${id}`;
      this.$sqliteUtil.SqlSelect(sql).then(res => {
        console.log(res);
        this.title = res[0].title;
        this.content = res[0].content;
        this.type = Number(res[0].type);
        this.coverPicture = res[0].coverPicture;
        this.videoB = res[0].videoB;
        this.topicname = res[0].topicname;
        this.address = res[0].address;
        this.latitude = res[0].latitude;
        this.longitude = res[0].longitude;
        this.authority = Number(res[0].authority);
        this.tempFilePaths = JSON.parse(res[0].tempFilePaths);
      });
    } else if (options.update == 2) {
      // 从编辑笔记进入
      this.isUpdate = true;
      this.tableId = options.tableId;
      let id = options.tableId;
    }
    // 获取表情列表
    this.emojiList = emojiList;
    // let sql = `select * from emoji_list`
    // this.$sqliteUtil.SqlSelect(sql).then(res => {
    // 	if (res.length == 0) {
    // 		this.emojiList = emojiList
    // 	} else {
    // 		this.emojiList = res
    // 	}
    // })
    uni.getLocation({
      type: 'gcj02',
      success: res => {
        this.latitude = res.latitude;
        this.longitude = res.longitude;
      },
      fail: err => {
        // H5需https或localhost，若失败则忽略并由用户手动选择地点
        console.warn('getLocation failed, fallback to manual chooseAddress', err && err.errMsg);
      },
      complete: () => {}
    });
    uni.onKeyboardHeightChange(res => {
      console.log('键盘高度变化:', res.height);
      if (res.height != 0) {
        this.keyboardHeight = res.height;
        this.showBottom = false;
      } else {
        // 键盘收起时，延迟显示底部按钮，确保状态稳定
        setTimeout(() => {
          this.showBottom = true;
        }, 100);
      }
    });
  },
  onShow() {
    // 页面显示时确保底部按钮状态正确
    this.showBottom = true;
  },
  onBackPress() {
    if (this.showAddTopic) {
      this.showAddTopic = false;
      return true;
    }
    if (this.showAttenUser) {
      this.showAttenUser = false;
      return true;
    }
    if (this.showEmoji) {
      this.showEmoji = false;
      return true;
    }
    uni.showActionSheet({
      alertText: '是否保存草稿',
      itemList: ['保存草稿', '不保存'],
      success: res => {
        if (res.tapIndex == 0) {
          this.saveDraft();
        } else {
          uni.navigateTo({
            url: '/pages/index/index'
          });
        }
      }
    });
    return true;
  }
};
</script>

<style lang="scss">
/* 与状态栏一体：整条固定顶栏，无留白，滑动不穿模 */
.publish-nav-wrap {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 100;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  box-sizing: border-box;
}
.publish-nav-row {
  width: 100%;
  height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #eee;
  box-sizing: border-box;
  flex-shrink: 0;
}
.publish-nav-left {
  padding: 8px;
  margin-left: -8px;
}
.publish-nav-title {
  flex: 1;
  text-align: center;
  font-size: 34rpx;
  font-weight: 500;
  color: #333;
}
.publish-nav-placeholder {
  width: 100%;
  background: #fff;
  flex-shrink: 0;
}
editor {
  caret-color: #3d8af5;
}

input {
  caret-color: #3d8af5;
}

// 调整正文编辑器的初始高度更紧凑
::v-deep .lsj-edit-edit-container {
  min-height: 160px !important;
  max-height: 480px !important;
  height: auto !important;
  background-color: #ffffff;
}

// 缩小编辑器外层 padding，减少视觉高度
::v-deep .editot-pd {
  padding: 20rpx 20rpx 40rpx !important;
}
</style>