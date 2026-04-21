<template>
	<view>
		<!-- 顶部栏与状态栏一体：整条固定，避免留白和滑动穿模（与闲宝消息等页一致） -->
		<view
			class="publish-nav-wrap"
			:style="{ height: (statusBarHeight + 44) + 'px' }"
		>
			<view class="publish-nav-row">
				<view class="publish-nav-left" @click="goBack">
					<u-icon name="arrow-left" size="22" color="#333"></u-icon>
				</view>
				<text class="publish-nav-title">发布笔记</text>
			</view>
		</view>
		<view :style="{ height: (statusBarHeight + 44) + 'px' }" class="publish-nav-placeholder"></view>
    <view style="padding: 30rpx">
			<view>
        <scroll-view v-if="Number(type) === 1" scroll-x enable-flex="true">
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
        <scroll-view v-else-if="Number(type) === 2" scroll-x enable-flex="true">
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
          <!-- 横向滑动选择关注用户 -->
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
                categoryId == cat.id ? 'border-color:#3d8af5;color:#3d8af5;background:#ffffff' : ''
              "
            >
							{{ cat.categoryName }}
						</view>
					</view>
				</scroll-view>
			</view>
		</u-popup>
		<!-- 专辑选择弹窗 -->
    <u-popup
      :show="showAlbum"
      :round="10"
      mode="bottom"
      @close="showAlbum = false"
      :safeAreaInsetBottom="true"
    >
      <view style="height: 580rpx; background-color: #ffffff">
        <view style="padding: 12rpx 28rpx 6rpx 28rpx; text-align: center">
          <view style="font-size: 32rpx; font-weight: 600; color: #2b2b2b">选择专辑</view>
				</view>
        <scroll-view scroll-y style="height: 500rpx">
          <view style="padding: 20rpx 28rpx 28rpx 28rpx">
            <view
              v-for="(album, idx) in albumList"
              :key="album.id"
							@click="selectAlbum(album)"
              style="
                height: 84rpx;
                border-radius: 14rpx;
                display: flex;
                justify-content: space-between;
                align-items: center;
                background: #ffffff;
                border: 2rpx solid #e6e6e6;
                box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.03);
                font-size: 26rpx;
                color: #2b2b2b;
                margin-bottom: 12rpx;
                padding: 0 20rpx;
              "
              :style="
                albumId == album.id ? 'border-color:#3d8af5;color:#3d8af5;background:#FFF5F6' : ''
              "
            >
              <view style="display: flex; align-items: center">
								<text>{{ album.title }}</text>
                <text
                  v-if="album.imgCount"
                  style="margin-left: 12rpx; font-size: 22rpx; color: #999"
                >
                  {{ album.imgCount }}篇
                </text>
							</view>
              <u-icon
                v-if="albumId == album.id"
                name="checkmark"
                color="#3d8af5"
                size="20"
              ></u-icon>
						</view>
						<!-- 创建新专辑按钮 -->
            <view
              @click="showCreateAlbum = true"
              style="
                height: 104rpx;
                border-radius: 54rpx;
                display: flex;
                justify-content: center;
                align-items: center;
                background: #f5f6f7;
                border: 1rpx dashed #e6e6e6;
                font-size: 26rpx;
                color: #3d8af5;
                margin-top: 32rpx;
              "
            >
              <u-icon name="plus" color="#3d8af5" size="20" style="margin-right: 8rpx"></u-icon>
							<text>创建新专辑</text>
						</view>
					</view>
				</scroll-view>
			</view>
		</u-popup>
		<!-- 创建专辑弹窗 -->
    <u-popup
      :show="showCreateAlbum"
      :round="10"
      mode="center"
      @close="showCreateAlbum = false"
      :overlayOpacity="0.3"
      :safeAreaInsetBottom="true"
    >
      <view style="width: 570rpx">
        <view style="padding: 20rpx; text-align: center">
          <view style="font-size: 35rpx">创建专辑</view>
          <view style="font-size: 25rpx; color: #949495; margin-top: 10rpx">请输入专辑名称</view>
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
            placeholder="请输入专辑名称"
            v-model="newAlbumName"
            :focus="showCreateAlbum"
            border="none"
            clearable
            maxlength="20"
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
            <text style="font-size: 35rpx; color: #949495" @click="cancelCreateAlbum">取消</text>
					</view>
          <view style="flex: 1; text-align: center">
            <text style="font-size: 35rpx; color: #3d8af5" @click="confirmCreateAlbum">创建</text>
					</view>
				</view>
			</view>
		</u-popup>
		<!-- 商品选择弹窗 -->
    <u-popup
      :show="showProducts"
      :round="10"
      mode="bottom"
      @close="showProducts = false"
      :safeAreaInsetBottom="true"
    >
      <view style="height: 80vh; background-color: #ffffff; display: flex; flex-direction: column">
				<!-- 标题栏 -->
        <view
          style="
            padding: 12rpx 28rpx 6rpx 28rpx;
            text-align: center;
            position: relative;
            flex-shrink: 0;
          "
        >
          <view style="font-size: 32rpx; font-weight: 600; color: #2b2b2b">选择关联商品</view>
          <view
            style="position: absolute; right: 28rpx; top: 12rpx; font-size: 28rpx; color: #3d8af5"
            @click="showProducts = false"
          >
            取消
          </view>
				</view>
				<!-- 搜索栏 -->
        <view style="padding: 20rpx 28rpx; flex-shrink: 0">
          <u-input
            v-model="productSearchKeyword"
            placeholder="搜索商品标题"
            clearable
            border="surround"
            @input="handleProductSearch"
            style="background: #f7f7f8; border-radius: 12rpx"
          >
						<template #prefix>
							<u-icon name="search" size="18" color="#999"></u-icon>
						</template>
					</u-input>
				</view>
				<!-- 已选商品提示 -->
        <view
          v-if="tempSelectedProducts.length > 0"
          style="padding: 0 28rpx 10rpx; font-size: 24rpx; color: #3d8af5; flex-shrink: 0"
        >
					已选择 {{ tempSelectedProducts.length }}/3 个商品
				</view>
				<!-- 商品列表 - 使用flex:1占据剩余空间 -->
        <scroll-view scroll-y style="flex: 1; overflow-y: auto" @scrolltolower="loadMoreProducts">
          <view style="padding: 20rpx 28rpx">
            <view
              v-for="(product, idx) in productList"
              :key="product.id"
							@click="toggleProductSelection(product)"
              style="
                height: 160rpx;
                border-radius: 14rpx;
                display: flex;
                align-items: center;
                background: #ffffff;
                border: 2rpx solid #e6e6e6;
                box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.03);
                margin-bottom: 12rpx;
                padding: 0 20rpx;
                position: relative;
              "
              :style="isProductSelected(product) ? 'border-color:#3d8af5;background:#F0F7FF' : ''"
            >
              <image
                :src="product.cover || product.image"
                mode="aspectFill"
                style="width: 120rpx; height: 120rpx; border-radius: 10rpx; margin-right: 20rpx"
              ></image>
              <view
                style="
                  flex: 1;
                  display: flex;
                  flex-direction: column;
                  justify-content: space-between;
                "
              >
                <view
                  style="
                    font-size: 28rpx;
                    color: #2b2b2b;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                  "
                >
									{{ product.title }}
								</view>
                <view style="display: flex; align-items: center">
                  <text style="font-size: 32rpx; color: #ff3b30; font-weight: 600">
                    ￥{{ formatPrice(product.price) }}
                  </text>
                  <text
                    v-if="product.originalPrice && product.originalPrice > product.price"
                    style="
                      font-size: 24rpx;
                      color: #999;
                      text-decoration: line-through;
                      margin-left: 12rpx;
                    "
                  >
										￥{{ formatPrice(product.originalPrice) }}
									</text>
								</view>
							</view>
              <view
                v-if="isProductSelected(product)"
                style="
                  width: 48rpx;
                  height: 48rpx;
                  border-radius: 50%;
                  background: #3d8af5;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                "
              >
								<u-icon name="checkmark" color="#fff" size="20"></u-icon>
							</view>
						</view>
            <view
              v-if="productList.length === 0 && !productSearchKeyword"
              style="text-align: center; padding: 100rpx 0; color: #999; font-size: 28rpx"
            >
							暂无商品
						</view>
            <view
              v-if="productList.length === 0 && productSearchKeyword"
              style="text-align: center; padding: 100rpx 0; color: #999; font-size: 28rpx"
            >
							未找到相关商品
						</view>
					</view>
				</scroll-view>
				<!-- 底部确认按钮 - 固定在底部 -->
        <view
          style="
            padding: 20rpx 28rpx;
            border-top: 1rpx solid #e6e6e6;
            flex-shrink: 0;
            background-color: #ffffff;
          "
        >
          <view
            @click="confirmProductSelection"
            style="
              height: 88rpx;
              border-radius: 44rpx;
              background: #3d8af5;
              color: #fff;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 32rpx;
            "
          >
						确定（{{ tempSelectedProducts.length }}/3）
					</view>
				</view>
			</view>
		</u-popup>
    <u-cell
      v-if="tags.length === 0"
      icon="tags"
      title="添加标签"
      @click="openTags"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    ></u-cell>
    <u-cell
      v-else
      icon="tags"
      iconStyle="color: #3d8af5"
      titleStyle="color: #3d8af5"
      :title="tags.join('、')"
      @click="openTags"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    ></u-cell>
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
      v-if="albumName == ''"
      icon="folder"
      title="选择专辑"
      @click="chooseAlbum"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    ></u-cell>
    <u-cell
      v-else
      icon="folder"
      iconStyle="color: #3d8af5"
      titleStyle="color: #3d8af5"
      :title="albumName"
      @click="chooseAlbum"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    ></u-cell>
    <u-cell
      v-if="relatedProducts.length === 0"
      icon="shopping-cart"
      title="关联商品"
      @click="chooseProducts"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    ></u-cell>
    <u-cell
      v-else
      icon="shopping-cart"
      iconStyle="color: #3d8af5"
      titleStyle="color: #3d8af5"
      :title="`已关联${relatedProducts.length}个商品`"
      @click="chooseProducts"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    ></u-cell>
    <u-cell
      v-if="address == ''"
      icon="map"
      title="添加地点"
      @click="chooseAddress"
      :isLink="true"
      arrow-direction="right"
      :border="false"
      size="large"
    ></u-cell>
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
        @click="publishNotes"
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
        <view style="color: #ffffff; font-size: 37rpx; letter-spacing: 1px">发布笔记</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getAttentionList } from '@/apis/user_service';
import editBtns from '@/uni_modules/lsj-edit/components/lsj-edit/edit-btns/edit-btns.vue';
import { emojiList } from '@/utils/emojiUtil.js';
import { addNote, getNotesCategoryList, uploadImages } from '@/apis/notes_service.js';
import { getAlbumList, createAlbum } from '@/apis/album_service.js';
import { getUserProducts } from '@/apis/idles_service.js';
import { baseUrl, AMAP_KEY } from '@/.env.js';
import { uploadFile } from '@/utils/fileUtil.js';
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
				draftId: '', // 草稿ID
				createTime: null, // 创建时间
				autoSaveTimer: null, // 自动保存定时器
				isSaving: false, // 是否正在保存草稿
				edit: null,
      type: 0, // 改为数字0，避免字符串比较问题
				tempFilePaths: [],
				uploadedFileUrls: [], // 已上传的文件URL（用于避免重复上传）
				uploadedCoverUrl: '', // 已上传的封面URL
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
				albumId: '',
				albumName: '',
				albumList: [],
				showAlbum: false,
				showCreateAlbum: false,
				newAlbumName: '',
				parentCategoryId: '',
				// 关联商品相关
				relatedProducts: [], // 已关联的商品列表
				showProducts: false, // 商品选择弹窗
				productList: [], // 商品列表
				productPage: 1, // 商品分页
				productPageSize: 10, // 每页数量
				totalProducts: 0, // 商品总数
				productSearchKeyword: '', // 商品搜索关键词
				tempSelectedProducts: [], // 临时选中的商品（用于弹窗确认）
      productSearchTimer: null, // 搜索防抖定时器
      aiGenerating: false // AI生成中状态
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
      getNotesCategoryList()
        .then(res => {
					if (res.code == 200) {
						// 字段转换：把 title 转成 categoryName
						this.categoryList = res.data.map(item => ({
							...item,
							categoryName: item.title
            }));
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
			// 选择专辑
			chooseAlbum() {
				if (this.albumList.length != 0) {
        this.showAlbum = true;
        return;
				}
      this.getAlbumListData();
			},
			// 获取专辑列表
			getAlbumListData() {
      const userInfo = uni.getStorageSync('userInfo');
				if (!userInfo || !userInfo.id) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
        });
        return;
				}
				getAlbumList({
					userId: userInfo.id
      })
        .then(res => {
					if (res.code == 200) {
						// getAlbumList 返回的是数组，不是分页数据
            const data = res.data;
						if (Array.isArray(data)) {
              this.albumList = data;
						} else {
              this.albumList = [];
						}
            this.showAlbum = true;
					} else {
						uni.showToast({
							title: res.msg || '获取专辑列表失败',
							icon: 'none'
            });
					}
        })
        .catch(err => {
          console.error('获取专辑列表失败:', err);
					uni.showToast({
						title: '获取专辑列表失败',
						icon: 'none'
          });
        });
			},
			// 选择专辑项
			selectAlbum(album) {
      this.albumId = album.id;
      this.albumName = album.title;
      this.showAlbum = false;
			},
			// 取消创建专辑
			cancelCreateAlbum() {
      this.newAlbumName = '';
      this.showCreateAlbum = false;
			},
			// 确认创建专辑
			confirmCreateAlbum() {
      const name = (this.newAlbumName || '').trim();
				if (!name) {
					uni.showToast({
						title: '请输入专辑名称',
						icon: 'none'
        });
        return;
				}
      const userInfo = uni.getStorageSync('userInfo');
				if (!userInfo || !userInfo.id) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
        });
        return;
				}
				uni.showLoading({
					title: '创建中...',
					mask: true
      });
				createAlbum({
					title: name,
					uid: userInfo.id,
					sort: 0
      })
        .then(res => {
          uni.hideLoading();
					if (res.code == 200) {
						uni.showToast({
							title: '创建成功',
							icon: 'success'
            });
            this.newAlbumName = '';
            this.showCreateAlbum = false;
						// 刷新专辑列表
            this.getAlbumListData();
					} else {
						uni.showToast({
							title: res.msg || '创建失败',
							icon: 'none'
            });
          }
        })
        .catch(err => {
          uni.hideLoading();
          console.error('创建专辑失败:', err);
					uni.showToast({
						title: '创建失败',
						icon: 'none'
          });
        });
			},
			// 选择商品
			chooseProducts() {
				if (this.productList.length === 0) {
        this.loadProducts();
				}
      this.tempSelectedProducts = [...this.relatedProducts];
      this.showProducts = true;
			},
			// 加载商品列表
			loadProducts() {
      const userInfo = uni.getStorageSync('userInfo');
				if (!userInfo || !userInfo.id) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
        });
        return;
				}
				// 移除加载中提示，直接加载数据
				getUserProducts({
					page: this.productPage || 1,
					pageSize: this.productPageSize || 10,
					keyword: this.productSearchKeyword || '', // 传递空字符串而不是 undefined
					userId: userInfo.id || ''
      })
        .then(res => {
          uni.hideLoading();
					if (res.code == 200) {
            const records = res.data?.records || res.data?.list || [];
						if (this.productPage === 1) {
              this.productList = records;
						} else {
              this.productList = this.productList.concat(records);
						}
            this.totalProducts = res.data?.total || 0;
					} else {
						uni.showToast({
							title: res.msg || '获取商品列表失败',
							icon: 'none'
            });
          }
        })
        .catch(err => {
          uni.hideLoading();
          console.error('获取商品列表失败:', err);
					uni.showToast({
						title: '获取商品列表失败',
						icon: 'none'
          });
        });
			},
			// 加载更多商品
			loadMoreProducts() {
				if (this.productList.length >= this.totalProducts) {
        return;
				}
      this.productPage++;
      this.loadProducts();
			},
			// 搜索商品
			handleProductSearch() {
				// 延迟搜索，避免频繁请求
      clearTimeout(this.productSearchTimer);
				this.productSearchTimer = setTimeout(() => {
        this.productPage = 1;
        this.productList = [];
        this.loadProducts();
      }, 300);
			},
			// 检查商品是否被选中
			isProductSelected(product) {
      return this.tempSelectedProducts.some(p => p.id === product.id);
			},
			// 切换商品选择状态
			toggleProductSelection(product) {
      const index = this.tempSelectedProducts.findIndex(p => p.id === product.id);
				if (index > -1) {
        this.tempSelectedProducts.splice(index, 1);
				} else {
					if (this.tempSelectedProducts.length >= 3) {
						uni.showToast({
							title: '最多只能关联3个商品',
							icon: 'none'
          });
          return;
					}
        this.tempSelectedProducts.push(product);
				}
			},
			// 确认选择商品
			confirmProductSelection() {
      this.relatedProducts = [...this.tempSelectedProducts];
      this.showProducts = false;
			},
			// 格式化价格
			formatPrice(price) {
      if (!price) return '0.00';
      return (price / 100).toFixed(2);
			},
			publishNotes() {
				// 1. 校验标题不能为空
				if (this.title == '') {
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
					
					// 3. 图片类型自动使用第一张作为封面（如果未设置）
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
					
					// 校验视频类型必须有封面图
					if (!this.coverPicture) {
						uni.showToast({
							title: '请设置视频封面图',
							icon: 'none'
						});
          return;
					}
				}
				
				// 5. 校验分类不能为空
				if (!this.categoryId || this.categoryId === '') {
					uni.showToast({
						title: '请选择分类',
						icon: 'none'
					});
        return;
      }
      uni
        .showModal({
          title: '提示',
          content: '确认发布笔记吗？',
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
							let noteVO = {
								// id: this.id, // 编辑时传
								title: this.title,
								content: contentText, // 纯文本内容
								noteCover: '',
								noteCoverHeight: this.noteCoverHeight || null,
								// 注意：按最新约定，categoryId 赋值给 cpid；父级给 cid（如有）
								cpid: this.categoryId,
								cid: this.parentCategoryId,
								urls: [],
								tags: this.tags || [],
								count: this.tempFilePaths.length,
								noteType: String(this.type), // 类型
								province: this.province || '',
								city: this.city || '',
								district: this.district || '',
								address: this.address || '',
								albumId: this.albumId || '',
								relatedProducts: this.relatedProducts || []
              };
							console.log('📝 发布笔记 - 省市区信息:', {
								province: noteVO.province,
								city: noteVO.city,
								district: noteVO.district,
								address: noteVO.address
              });
              const rid = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
						(async () => {
							try {
								// 直接调用addNote方法，一步完成文件上传和笔记保存
								// 如果已经有上传后的URL（从AI配文获取），直接使用，避免重复上传
								const resData = await addNote({
									requestId: rid,
									noteData: noteVO,
									coverFile: this.uploadedCoverUrl ? null : (this.coverPicture || null), // 如果已有URL，不传文件
									uploadFiles: this.uploadedFileUrls.length > 0 ? [] : (this.tempFilePaths || []), // 如果已有URL，不传文件
									uploadedUrls: this.uploadedFileUrls.length > 0 ? this.uploadedFileUrls : undefined, // 传入已上传的URL
									uploadedCoverUrl: this.uploadedCoverUrl || undefined // 传入已上传的封面URL
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
										// 使用 reLaunch 兼容微信小程序自定义 TabBar（无原生 tabBar）
										uni.reLaunch({
											url: '/pages/index/index'
                      });
                    }, 1500);
								} else {
									uni.showToast({
                      title: resData.msg == '' ? '发布失败' : resData.msg,
										icon: 'none',
										duration: 2000
									});
								}
							} catch (err) {
                  uni.hideLoading();
								uni.showToast({
									title: '发布失败',
									icon: 'none',
									duration: 2000
                  });
							}
              })();
            });
					}
        });
			},
			saveDraftTip() {
				// SQLite已禁用，草稿功能统一走后端接口
				// 查询所有草稿
				// this.$sqliteUtil.SqlSelect(`select * from draft_notes`).then(res => {
				// 	console.log(res)
				// })
				uni.showModal({
        title: '提示',
        content: '确认保存笔记至草稿箱吗？',
        cancelText: '取消',
        confirmText: '确定',
        success: res => {
						if (res.confirm) {
							// 将页面数据存入数据库
            this.saveDraft();
						}
					}
      });
			},
			saveDraft(isSilent = false) {
				// 防止重复保存
				if (this.isSaving) {
        return;
				}
      this.isSaving = true;
				
				// 保存草稿到本地存储
      this.edit
        .getContents()
        .then(res => {
          console.log('获取编辑内容:', res);
          this.content = res.html;
					
					// 如果没有设置封面且是图片类型，使用第一张图片作为封面
					if (this.coverPicture == '' && this.type == 1 && this.tempFilePaths.length > 0) {
            this.coverPicture = this.tempFilePaths[0];
					}
					
					try {
						// 准备草稿数据
						const draftData = {
							id: this.draftId || Date.now().toString(),
							title: this.title || '',
							content: this.content || '',
							coverPicture: this.coverPicture || '',
							type: this.type || 1,
							tempFilePaths: this.tempFilePaths || [],
							address: this.address || '',
							province: this.province || '',
							city: this.city || '',
							district: this.district || '',
							latitude: this.latitude || '',
							longitude: this.longitude || '',
							authority: this.authority || 0,
							tags: this.tags || [],
							category: this.category || '',
							categoryId: this.categoryId || '',
							categoryName: this.categoryName || '',
							parentCategoryId: this.parentCategoryId || '',
							albumId: this.albumId || '',
							albumName: this.albumName || '',
							relatedProducts: this.relatedProducts || [],
							createTime: this.createTime || Date.now(),
							updateTime: Date.now(),
							draftType: 'notes'
            };
						
						// 获取现有草稿列表
            const existingDrafts = uni.getStorageSync('drafts') || [];
						
						// 检查是否是更新现有草稿
            const draftIndex = existingDrafts.findIndex(d => d.id === draftData.id);
						
						if (draftIndex >= 0) {
							// 更新现有草稿
              existingDrafts[draftIndex] = draftData;
              console.log('更新草稿:', draftData.id);
						} else {
							// 新增草稿
              existingDrafts.unshift(draftData);
              console.log('新增草稿:', draftData.id);
						}
						
						// 保存到本地存储
            uni.setStorageSync('drafts', existingDrafts);
						
						// 用户反馈（非静默模式）
						if (!isSilent) {
							uni.showToast({
								title: draftIndex >= 0 ? '草稿已更新' : '草稿已保存',
								icon: 'success',
								duration: 1500
              });
							
							// 延迟跳转到首页
							setTimeout(() => {
								uni.reLaunch({
									url: '/pages/index/index'
                });
              }, 1500);
						}
					} catch (error) {
            console.error('保存草稿失败:', error);
						uni.showToast({
							title: '保存失败',
							icon: 'none'
            });
					}
        })
        .catch(error => {
          console.error('获取编辑内容失败:', error);
					uni.showToast({
						title: '获取内容失败',
						icon: 'none'
          });
        });
			},
			/**
			 * 自动保存草稿（已禁用）
			 */
			// autoSave() {
			// 	// 清除之前的定时器
			// 	if (this.autoSaveTimer) {
			// 		clearTimeout(this.autoSaveTimer)
			// 	}
			// 	
			// 	// 设置新的定时器
			// 	this.autoSaveTimer = setTimeout(() => {
			// 		if (this.title.trim() || this.content.trim()) {
			// 			this.silentSaveDraft() // 静默保存
			// 		}
			// 	}, 3000) // 3秒后自动保存
			// },
			/**
			 * 静默保存草稿（已禁用）
			 */
			// silentSaveDraft() {
			// 	this.edit.getContents().then(res => {
			// 		this.content = res.html
			// 		
			// 		if (this.coverPicture == '' && this.type == 1 && this.tempFilePaths.length > 0) {
			// 			this.coverPicture = this.tempFilePaths[0]
			// 		}
			// 		
			// 		try {
			// 			const draftData = {
			// 				id: this.draftId || Date.now().toString(),
			// 				title: this.title || '',
			// 				content: this.content || '',
			// 				coverPicture: this.coverPicture || '',
			// 				type: this.type || 1,
			// 				tempFilePaths: this.tempFilePaths || [],
			// 				address: this.address || '',
			// 				latitude: this.latitude || '',
			// 				longitude: this.longitude || '',
			// 				authority: this.authority || 0,
			// 				tags: this.tags || [],
			// 				category: this.category || '',
			// 				location: this.location || '',
			// 				createTime: this.createTime || Date.now(),
			// 				updateTime: Date.now(),
			// 				draftType: 'notes'
			// 			}
			// 		
			// 			const existingDrafts = uni.getStorageSync('drafts') || []
			// 			const draftIndex = existingDrafts.findIndex(d => d.id === draftData.id)
			// 		
			// 			if (draftIndex >= 0) {
			// 				existingDrafts[draftIndex] = draftData
			// 			} else {
			// 				existingDrafts.unshift(draftData)
			// 			}
			// 		
			// 			uni.setStorageSync('drafts', existingDrafts)
			// 			console.log('自动保存草稿成功')
			// 		
			// 		} catch (error) {
			// 			console.error('自动保存草稿失败:', error)
			// 		}
			// 	}).catch(error => {
			// 		console.error('获取编辑内容失败:', error)
			// 	})
			// },
			/**
			 * 从本地存储加载草稿
			 */
			loadDraftFromStorage(draftId) {
				try {
        const drafts = uni.getStorageSync('drafts') || [];
        const draft = drafts.find(d => d.id === draftId);
					
					if (draft) {
          this.draftId = draft.id;
          this.title = draft.title || '';
          this.content = draft.content || '';
          this.coverPicture = draft.coverPicture || '';
          this.type = draft.type || 1;
          this.tempFilePaths = draft.tempFilePaths || [];
          this.address = draft.address || '';
          this.province = draft.province || '';
          this.city = draft.city || '';
          this.district = draft.district || '';
          this.latitude = draft.latitude || '';
          this.longitude = draft.longitude || '';
          this.authority = draft.authority || 0;
          this.tags = draft.tags || [];
          this.category = draft.category || '';
          this.categoryId = draft.categoryId || '';
          this.categoryName = draft.categoryName || '';
          this.parentCategoryId = draft.parentCategoryId || '';
          this.albumId = draft.albumId || '';
          this.albumName = draft.albumName || '';
          this.relatedProducts = draft.relatedProducts || [];
          this.createTime = draft.createTime;

          console.log('加载草稿成功:', draft);
					} else {
          console.error('草稿不存在:', draftId);
						uni.showToast({
							title: '草稿不存在',
							icon: 'none'
          });
					}
				} catch (error) {
        console.error('加载草稿失败:', error);
					uni.showToast({
						title: '加载草稿失败',
						icon: 'none'
        });
				}
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
				// 兼容微信小程序的授权流程
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
								location: { latitude: res.latitude, longitude: res.longitude },
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
        uni.chooseLocation({ success: afterChoose });
      };
				// 微信小程序端先检查并申请权限
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
											if (m.confirm) {
                      uni.openSetting({});
											}
										}
                });
								}
            });
						}
					}
      });
				// #endif
				// 其他端直接打开
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
				// 最后兜底：无法解析省市区时，使用地点名称作为详细地址
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
        let uploadedUrls = []; // 保存上传后的URL
        let uploadedCoverUrl = '';

        if (this.type === 2) {
          // 视频模式：优先使用已上传的URL，避免重复上传
          if (this.uploadedFileUrls.length > 0 && this.uploadedFileUrls[0]) {
            const videoUrl = this.uploadedFileUrls[0];
            console.log('✅ 使用之前已上传的视频URL，跳过上传:', videoUrl);
            
            // 使用已上传的URL调用AI生成接口
            response = await generateContentFromUrls({
              videoUrl: videoUrl,
              style: '小红书笔记',
              contentType: 'note',
              category: this.categoryName || undefined
            });
          } else {
            // 视频模式：先上传视频获取URL
            try {
              const videoUrl = await new Promise((resolve, reject) => {
                uni.uploadFile({
                  url: baseUrl + '/file/upload',
                  filePath: this.tempFilePaths[0],
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
              
              // 保存上传后的URL，避免发布时重复上传
              this.uploadedFileUrls = [videoUrl];
            
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
            }
            
            // 使用上传后的URL调用AI生成接口
            response = await generateContentFromUrls({
              videoUrl: videoUrl,
              style: '小红书笔记',
              contentType: 'note',
              category: this.categoryName || undefined
            });
          } catch (uploadError) {
            // 如果上传失败，回退到原来的方式
            console.warn('视频上传失败，使用原方式:', uploadError);
            response = await generateContentFromVideo(this.tempFilePaths[0], {
              style: '小红书笔记',
              contentType: 'note',
              category: this.categoryName || undefined
            });
            }
          }
        } else {
          // 图片模式：优先使用已上传的URL，避免重复上传
          if (this.uploadedFileUrls.length > 0 && this.uploadedFileUrls.length === this.tempFilePaths.length) {
            // 如果之前已经上传过，且数量匹配，直接使用已上传的URL
            uploadedUrls = this.uploadedFileUrls;
            console.log('✅ 使用之前已上传的图片URL，跳过上传:', uploadedUrls);
          } else {
            // 图片模式：使用批量上传接口（如果支持）或逐个上传
            uploadedUrls = await uploadImages(this.tempFilePaths);
            
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
          }
          
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
          } else if (uploadedUrls.length > 0) {
            // 没有封面但有图片，使用第一张作为封面
            this.uploadedCoverUrl = uploadedUrls[0];
          }

          // 使用上传后的URL调用AI生成接口
          response = await generateContentFromUrls({
            imageUrls: uploadedUrls,
            style: '小红书笔记',
            contentType: 'note',
            category: this.categoryName || undefined
          });
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
            
            // 使用nextTick确保在DOM更新后再设置内容
            // 在设置内容前保持loading显示，设置完成后再隐藏
            this.$nextTick(() => {
              try {
                // 使用tool方法直接调用setContents，避免ready方法可能触发的加载状态
                // 通过tool('setContents')设置内容，不会触发ready的初始化逻辑
                if (this.edit.tool && this.edit.ctx) {
                  // 先失焦，避免设置内容后自动聚焦并触发键盘
                  this.edit.tool('blur');
                  
                  // 使用tool方法设置内容
                  this.edit.tool('setContents', {
                    html: contentText
                  });
                  
                  // 设置内容后，延迟将光标移动到内容末尾，并确保不聚焦
                  // 同时在这个时机隐藏loading，确保内容已设置完成
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
                    } finally {
                      // 确保在设置内容完成后才隐藏loading
                      uni.hideLoading();
                    }
                  }, 200);
                } else if (this.edit.ready) {
                  // 如果tool方法不存在，使用ready方法作为备选
                  // 先失焦
                  if (this.edit.tool) {
                    this.edit.tool('blur');
                  }
                  this.edit.ready(contentText);
                  
                  // 延迟失焦，避免触发键盘，同时隐藏loading
                  setTimeout(() => {
                    try {
                      if (this.edit && this.edit.ctx) {
                        this.edit.ctx.blur();
                      }
                    } catch (e) {
                      console.warn('失焦失败:', e);
                    } finally {
                      // 确保在设置内容完成后才隐藏loading
                      uni.hideLoading();
                    }
                  }, 200);
                } else {
                  // 如果都不存在，直接隐藏loading
                  uni.hideLoading();
                }
              } catch (e) {
                console.error('设置内容失败:', e);
                // 如果设置失败，使用ready方法作为备选
                if (this.edit.ready) {
                  this.edit.ready(contentText);
                }
                // 确保在错误情况下也隐藏loading
                uni.hideLoading();
              }
            });
          } else {
            // 如果没有内容需要设置，直接隐藏loading
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
			insertCoverPicture() {
				uni.chooseImage({
					count: 1,
					sizeType: ['original', 'compressed'],
					sourceType: ['album', 'camera'],
        success: res => {
						let tempFilePaths = res.tempFilePaths;
						if (process.env.UNI_PLATFORM === 'h5') {
							// H5 端直接用临时路径
							this.coverPicture = tempFilePaths[0];
						} else {
							// App 端保存到本地
							uni.saveFile({
								tempFilePath: tempFilePaths[0],
              success: res => {
									this.coverPicture = res.savedFilePath;
								}
							});
						}
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
						if (process.env.UNI_PLATFORM === 'h5') {
							// H5 端直接拼接临时路径
							this.tempFilePaths = this.tempFilePaths.concat(tempFilePaths);
							// 首次选择时默认第一张为封面
							if (!this.coverPicture && this.tempFilePaths.length > 0) {
              this.coverPicture = this.tempFilePaths[0];
							}
						} else {
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
						}
					}
				});
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
			}
		},
		watch: {
			// 监听标题变化，触发自动保存
			// title() {
			// 	this.autoSave()
			// },
			// 监听内容变化，触发自动保存
			// content() {
			// 	this.autoSave()
			// }
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
    // #ifdef MP-WEIXIN
    console.log('🔍 发布笔记页面 onLoad，接收到的参数:', options);
    // #endif
    
    // 不在这里设置，让后面的逻辑设置
    // this.type = this.type || 0;
    
			if (options.update == 0) {
				// 从发布笔记进入
      this.type = Number(options.type);
      console.log('📝 设置 type 为:', this.type, '(类型:', typeof this.type, ')');
      console.log('📝 options.type 原始值:', options.type, '(类型:', typeof options.type, ')');
      
      // 确保 type 是数字类型
      if (isNaN(this.type)) {
        console.error('❌ type 转换失败，使用默认值 1');
        this.type = 1;
      }
      
      // #ifdef MP-WEIXIN
      // 微信小程序端：优先从全局存储读取文件路径
      const storedFilePaths = uni.getStorageSync('publish_tempFilePaths');
      console.log('🔍 从全局存储读取 publish_tempFilePaths:', storedFilePaths);
      console.log('🔍 storedFilePaths 类型:', typeof storedFilePaths, '是否为数组:', Array.isArray(storedFilePaths));
      
      if (storedFilePaths && Array.isArray(storedFilePaths) && storedFilePaths.length > 0) {
        this.tempFilePaths = storedFilePaths;
        console.log('✅ 从全局存储读取文件路径成功，数量:', this.tempFilePaths.length);
        console.log('✅ 文件路径详情:', this.tempFilePaths);
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
        console.log('⚠️ 全局存储没有，尝试从URL参数读取');
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
        console.error('❌ storedFilePaths:', storedFilePaths);
        console.error('❌ options.tempFilePaths:', options.tempFilePaths);
        // 不设置空数组，保持初始值，让页面至少能显示
        // this.tempFilePaths = []; // 注释掉，避免覆盖初始值
        // 不立即返回，让页面至少能显示，在 onShow 中再尝试读取
        console.log('⚠️ 将在 onShow 中再次尝试读取全局存储');
        console.log('⚠️ 当前 tempFilePaths:', this.tempFilePaths);
      }
      // #endif
      
      // #ifndef MP-WEIXIN
      // 非微信小程序端：从URL参数读取
      if (options.tempFilePaths) {
        try {
          this.tempFilePaths = JSON.parse(decodeURIComponent(options.tempFilePaths));
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
      
				// 处理 Live Photo 的封面图参数
				if (options.coverPicture) {
					this.coverPicture = decodeURIComponent(options.coverPicture);
					console.log('✅ 接收到 Live Photo 封面:', this.coverPicture);
					console.log('Live Photo 模式:', {
						type: this.type,
						coverPicture: this.coverPicture,
						videoPath: this.tempFilePaths[0]
					});
				} else if (this.type === 2 && this.tempFilePaths && this.tempFilePaths[0]) {
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
			} else if (options.draftId) {
				// 从草稿页面进入编辑模式
      this.loadDraftFromStorage(options.draftId);
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
    
    // 调试：输出最终状态（所有平台）
    const tempFilePathsLength = this.tempFilePaths ? this.tempFilePaths.length : 0;
    console.log('========== onLoad 完成，最终状态 ==========');
    console.log('📊 type:', this.type, '(类型:', typeof this.type, ', 值:', this.type, ')');
    console.log('📊 tempFilePaths:', JSON.stringify(this.tempFilePaths), '(长度:', tempFilePathsLength, ')');
    console.log('📊 coverPicture:', this.coverPicture || '(空)');
    console.log('📊 type === 1 判断结果:', this.type === 1);
    console.log('📊 type === "1" 判断结果:', this.type === "1");
    console.log('📊 Number(type) === 1 判断结果:', Number(this.type) === 1);
    console.log('📊 页面应该显示:', this.type === 1 || this.type === '1' ? '图片模式' : this.type === 2 || this.type === '2' ? '视频模式' : '未知模式');
    console.log('========== onLoad 结束 ==========');
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
  mounted() {
    // #ifdef MP-WEIXIN
    const tempFilePathsLength = this.tempFilePaths ? this.tempFilePaths.length : 0;
    console.log('📊 页面 mounted - type:', this.type, 'tempFilePaths.length:', tempFilePathsLength);
    // #endif
  },
		onShow() {
			// 页面显示时确保底部按钮状态正确
			this.showBottom = true;
    
    // #ifdef MP-WEIXIN
    // 微信小程序端：如果 onLoad 时没有读取到文件路径，在 onShow 中再次尝试
    if (this.type && (!this.tempFilePaths || this.tempFilePaths.length === 0)) {
      console.log('🔍 onShow: 检测到 tempFilePaths 为空，尝试从全局存储读取');
      const storedFilePaths = uni.getStorageSync('publish_tempFilePaths');
      console.log('🔍 onShow: 从全局存储读取 publish_tempFilePaths:', storedFilePaths);
      console.log('🔍 onShow: storedFilePaths 类型:', typeof storedFilePaths, '是否为数组:', Array.isArray(storedFilePaths));
      
      if (storedFilePaths && Array.isArray(storedFilePaths) && storedFilePaths.length > 0) {
        this.tempFilePaths = storedFilePaths;
        console.log('✅ onShow: 从全局存储读取文件路径成功，数量:', this.tempFilePaths.length);
        console.log('✅ onShow: 文件路径详情:', this.tempFilePaths);
        // 读取后清除存储
        uni.removeStorageSync('publish_tempFilePaths');
        
        // 读取封面图
        const storedCoverPicture = uni.getStorageSync('publish_coverPicture');
        if (storedCoverPicture) {
          this.coverPicture = storedCoverPicture;
          console.log('✅ onShow: 从全局存储读取封面图:', this.coverPicture);
          uni.removeStorageSync('publish_coverPicture');
        }
        
        // 强制更新视图
        this.$forceUpdate();
      } else {
        console.error('❌ onShow: 仍然未找到文件路径');
      }
    }
    
    // 调试：输出当前状态
    const tempFilePathsLength = this.tempFilePaths ? this.tempFilePaths.length : 0;
    console.log('🔍 onShow: 当前状态 - type:', this.type, 'tempFilePaths.length:', tempFilePathsLength);
    // #endif
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