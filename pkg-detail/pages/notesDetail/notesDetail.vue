<template>
  <view>
    <!-- #ifndef MP-WEIXIN -->
    <view
      :style="{ height: statusBarHeight + 'px' }"
      style="position: fixed; top: 0; width: 100%; z-index: 9999; background-color: #fff"
    ></view>
    <!-- #endif -->
    <view
      :style="{ top: navBarTop }"
      style="
        position: fixed;
        width: 100%;
        height: 44px;
        background-color: #fff;
        z-index: 9999;
        display: flex;
        align-items: center;
        padding: 0 10rpx;
        box-sizing: border-box;
      "
    >
      <!-- #ifndef MP-WEIXIN -->
      <u-icon name="arrow-left" color="#2b2b2b" size="25" @click="goBack"></u-icon>
      <!-- #endif -->
      <view style="margin-left: 20rpx">
        <image
          :src="notesDetail.avatarUrl"
          style="height: 33px; width: 33px; border-radius: 50%; margin-top: 5px"
          mode="aspectFill"
          @click="goToOtherMine(notesDetail.belongUserId)"
        ></image>
      </view>
      <view class="authorName">{{ notesDetail.nickname }}</view>
      <view
        v-if="notesDetail.belongUserId != userInfo.id"
        style="display: flex; justify-content: space-around; flex: 1; padding: 0 20rpx"
      >
        <u-tag
          v-if="notesDetail.isFollow"
          text="已关注"
          shape="circle"
          color="#3d8af5"
          bgColor="#e6f2ff"
          borderColor="#3d8af5"
          @click="attention"
        ></u-tag>
        <u-tag
          v-else
          text="关注"
          plain
          shape="circle"
          color="#3d8af5"
          borderColor="#3d8af5"
          @click="attention"
        ></u-tag>
        <u-icon @click="showShare = true" name="share" color="#2b2b2b" size="30"></u-icon>
      </view>
      <view v-else style="display: flex; margin-left: auto; margin-right: 15rpx">
        <u-icon @click="showShare = true" name="more-dot-fill" color="#2b2b2b" size="26"></u-icon>
      </view>
    </view>
    <view :style="{ height: navPlaceholderHeight }" style="width: 100%"></view>
    <view style="position: relative">
      <swiper
        :style="{ height: swipperHeight + 70 + 'rpx' }"
        :indicator-dots="true"
        :circular="true"
        current="swiperCurrent"
        @change="swiperChange"
        indicator-active-color="#3d8af5"
        indicator-color="#9ea1a3"
      >
        <swiper-item v-for="(item, index) in notesDetail.notesResources" :key="index">
          <view
            :style="{ height: swipperHeight + 'rpx' }"
            style="display: flex; justify-content: center; align-items: center"
          >
            <image
              :src="item.url"
              :style="{
                height: item.height + 'rpx',
                width: item.width + 'rpx',
                transition: 'all 0.3s ease'
              }"
              mode="widthFix"
              :show-menu-by-longpress="true"
            ></image>
          </view>
        </swiper-item>
      </swiper>
      <view
        style="
          position: absolute;
          top: 30rpx;
          right: 30rpx;
          background-color: #383c3c;
          border-radius: 20px;
          font-size: 28rpx;
          color: #f5f5f5;
          padding: 8rpx 18rpx;
        "
        v-if="notesDetail != null"
      >
        {{ swiperCurrent + 1 }}/{{ swipperCount }}
      </view>
    </view>
    <view style="padding: 10rpx 40rpx">
      <view
        style="
          font-size: 37rpx;
          color: #474a4d;
          margin-bottom: 15rpx;
          word-wrap: break-word;
          word-break: break-all;
          letter-spacing: 2rpx;
        "
      >
        {{ notesDetail.title }}
      </view>
      <view class="note-content">
        <rich-text
          v-if="notesDetail.content"
          :nodes="notesDetail.content"
          @itemclick="clickTopic"
        ></rich-text>
      </view>
      <!-- 关联商品展示 -->
      <view
        v-if="notesDetail.relatedProducts && notesDetail.relatedProducts.length > 0"
        style="margin-top: 20rpx"
      >
        <!-- <view style="font-size: 26rpx;color: #2b2b2b;margin-bottom: 16rpx;font-weight: 600;">关联商品</view> -->
        <view style="display: flex; flex-direction: column; gap: 12rpx">
          <view
            v-for="product in notesDetail.relatedProducts"
            :key="product.id"
            @click="goToProduct(product.id)"
            style="
              display: flex;
              align-items: center;
              background: #ffffff;
              border-radius: 10rpx;
              border: 1rpx solid #e6e6e6;
              box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.02);
              padding: 12rpx;
              gap: 12rpx;
            "
          >
            <image
              :src="product.cover"
              mode="aspectFill"
              style="width: 100rpx; height: 100rpx; border-radius: 8rpx; flex-shrink: 0"
            ></image>
            <view
              style="
                flex: 1;
                display: flex;
                flex-direction: column;
                justify-content: space-between;
                min-width: 0;
              "
            >
              <view
                style="
                  font-size: 26rpx;
                  color: #2b2b2b;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  margin-bottom: 8rpx;
                "
              >
                {{ product.title }}
              </view>
              <view style="display: flex; align-items: center; gap: 8rpx">
                <text style="font-size: 28rpx; color: #ff3b30; font-weight: 600">
                  ￥{{ formatProductPrice(product.price) }}
                </text>
                <text
                  v-if="
                    product.originalPrice &&
                    parseFloat(product.originalPrice || 0) > parseFloat(product.price || 0)
                  "
                  style="font-size: 22rpx; color: #999; text-decoration: line-through"
                >
                  ￥{{ formatProductPrice(product.originalPrice) }}
                </text>
              </view>
            </view>
            <u-icon name="arrow-right" color="#999" size="18" style="flex-shrink: 0"></u-icon>
          </view>
        </view>
      </view>
      <view style="display: flex; margin-top: 15rpx; font-size: 24rpx; color: #afafb0">
        <view>
          <text v-if="notesDetail.updateTime != notesDetail.createTime">
            更新于{{ notesDetail.updateTime }}
          </text>
          <text v-else>{{ notesDetail.createTime }}</text>
        </view>
        <view v-if="notesDetail.city || notesDetail.address" style="margin-left: 20rpx">
          {{ notesDetail.city }}{{ notesDetail.address ? ' ' + notesDetail.address : '' }}
        </view>
      </view>
    </view>
    <u-divider style="padding: 0 30rpx" :hairline="true"></u-divider>
    <view style="padding: 30rpx">
      <view v-if="commentCount > 0" style="font-size: 25rpx; color: #474a4d">
        共{{ commentCount }}条评论
      </view>
      <view v-else style="font-size: 25rpx; color: #474a4d">暂无评论</view>
      <view style="display: flex; margin-top: 20rpx; height: 90rpx; align-items: center">
        <image
          style="height: 70rpx; width: 70rpx; border-radius: 50%"
          mode="aspectFill"
          :src="userInfo.avatarUrl"
        ></image>
        <view
          @click="replyNotes"
          style="
            margin-left: 20rpx;
            flex: 1;
            background-color: #f3f3f2;
            padding: 20rpx;
            border-radius: 70rpx;
            height: 100%;
            box-sizing: border-box;
            display: flex;
            align-items: center;
          "
        >
          <view style="font-size: 30rpx; color: #afafb0; margin-left: 10rpx">
            爱评论的人运气都不差
          </view>
          <view
            style="display: flex; justify-content: space-around; flex: 1; padding: 0 10rpx 0 50rpx"
          >
            <u-icon name="/static/aite.png" size="25"></u-icon>
            <u-icon name="/static/emoji.png" size="25"></u-icon>
            <u-icon name="/static/photo.png" size="25"></u-icon>
          </view>
        </view>
      </view>
    </view>
    <view style="padding: 30rpx">
      <block v-for="(item, index) in commentList" v-bind:key="item.id">
        <view style="display: flex; align-items: flex-start; margin-bottom: 20rpx">
          <image
            :src="item.commentUserAvatar"
            style="height: 70rpx; width: 70rpx; border-radius: 50%"
            mode="aspectFill"
            @click="goToOtherMine(item.commentUserId)"
          ></image>
          <view style="flex: 1; padding: 0 10px; word-break: break-all; text-overflow: ellipsis">
            <view style="display: flex; font-size: 26rpx; color: #afafb0">
              <view
                style="
                  word-break: break-all;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  max-width: 300rpx;
                "
                @click="goToOtherMine(item.commentUserId)"
              >
                {{ item.commentUserName }}
              </view>
              <view
                v-if="item.commentUserId == notesDetail.belongUserId"
                style="
                  margin-left: 10rpx;
                  padding: 4rpx 10rpx;
                  background-color: #f3f3f2;
                  color: #7d7d7d;
                  border-radius: 50rpx;
                  white-space: nowrap;
                  width: 50rpx;
                  text-align: center;
                "
              >
                作者
              </view>
              <view
                v-else-if="item.commentUserId == userInfo.id"
                style="
                  margin-left: 10rpx;
                  padding: 4rpx 10rpx;
                  background-color: #f3f3f2;
                  color: #7d7d7d;
                  border-radius: 50rpx;
                  white-space: nowrap;
                  width: 30rpx;
                  text-align: center;
                "
              >
                我
              </view>
            </view>
            <view style="margin-top: 5rpx">
              <view style="display: flex; align-items: baseline; flex-wrap: wrap">
                <rich-text
                  v-if="item.content"
                  style="font-size: 14px; letter-spacing: 0.05rem; color: #383c3c"
                  :nodes="item.content"
                  @longpress="openCommentSetting(item, 0)"
                  @touchend="touchend"
                  @click="replyFirstComment(item)"
                  @itemclick="clickUser"
                ></rich-text>
                <text
                  style="font-size: 20rpx; color: #afafb0; margin-left: 10rpx; white-space: nowrap"
                  @longpress="openCommentSetting(item, 0)"
                  @touchend="touchend"
                  @click="replyFirstComment(item)"
                >
                  {{ item.createTime }}
                </text>
                <text
                  style="font-size: 20rpx; color: #afafb0; margin-left: 20rpx; white-space: nowrap"
                  @longpress="openCommentSetting(item, 0)"
                  @touchend="touchend"
                  @click="replyFirstComment(item)"
                >
                  {{ item.province }}
                </text>
                <text
                  style="font-size: 20rpx; color: #7d7d7d; margin-left: 20rpx; white-space: nowrap"
                  @longpress="openCommentSetting(item, 0)"
                  @touchend="touchend"
                  @click="replyFirstComment(item)"
                >
                  回复
                </text>
              </view>
            </view>
            <view
              v-if="item.pictureUrl != null && item.pictureUrl != ''"
              style="margin-top: 10rpx"
              @click="previewImage(item.picture.url)"
            >
              <image
                :src="item.picture.url"
                :style="{ height: item.picture.height + 'rpx', width: item.picture.width + 'rpx' }"
                style="border-radius: 20rpx"
                mode="aspectFill"
              ></image>
            </view>
            <view
              v-if="item.isTop"
              style="
                background-color: #fdeff2;
                padding: 5rpx 10rpx;
                border-radius: 50rpx;
                font-size: 22rpx;
                color: #3d8af5;
                display: inline-block;
              "
            >
              置顶评论
            </view>
            <view v-if="item.commentReplyNum > 0" style="margin-top: 10rpx">
              <block v-for="(item2, index2) in item.children.list" :key="index2">
                <view
                  style="
                    display: flex;
                    align-items: flex-start;
                    margin-top: 10rpx;
                    position: relative;
                  "
                >
                  <image
                    :src="item2.commentUserAvatar"
                    style="height: 50rpx; width: 50rpx; border-radius: 50%"
                    mode="aspectFill"
                    @click="goToOtherMine(item2.commentUserId)"
                  ></image>
                  <view
                    style="
                      flex: 1;
                      padding: 0 20rpx;
                      word-break: break-all;
                      overflow: hidden;
                      text-overflow: ellipsis;
                    "
                  >
                    <view style="display: flex; font-size: 26rpx; color: #afafb0; width: 350rpx">
                      <view
                        style="
                          word-break: break-all;
                          overflow: hidden;
                          text-overflow: ellipsis;
                          white-space: nowrap;
                          max-width: 300rpx;
                        "
                        @click="goToOtherMine(item2.commentUserId)"
                      >
                        {{ item2.commentUserName }}
                      </view>
                      <view
                        v-if="item2.commentUserId == notesDetail.belongUserId"
                        style="
                          margin-left: 10rpx;
                          padding: 4rpx 10rpx;
                          background-color: #f3f3f2;
                          color: #7d7d7d;
                          border-radius: 50rpx;
                          white-space: nowrap;
                          width: 50rpx;
                          text-align: center;
                        "
                      >
                        作者
                      </view>
                      <view
                        v-else-if="item2.commentUserId == userInfo.id"
                        style="
                          margin-left: 10rpx;
                          padding: 4rpx 10rpx;
                          background-color: #f3f3f2;
                          color: #7d7d7d;
                          border-radius: 50rpx;
                          white-space: nowrap;
                          width: 30rpx;
                          text-align: center;
                        "
                      >
                        我
                      </view>
                    </view>
                    <view style="margin-top: 5rpx">
                      <view style="display: flex; align-items: baseline; flex-wrap: wrap">
                        <rich-text
                          v-if="item2.content"
                          style="font-size: 14px; letter-spacing: 0.05rem; color: #383c3c"
                          @longpress="openCommentSetting(item2, item)"
                          @touchend="touchend"
                          :nodes="item2.content"
                          @click="replySecondComment(item, item2)"
                          @itemclick="clickUser"
                        ></rich-text>
                        <text
                          style="
                            font-size: 20rpx;
                            color: #afafb0;
                            margin-left: 10rpx;
                            white-space: nowrap;
                          "
                          @longpress="openCommentSetting(item2, item)"
                          @touchend="touchend"
                          @click="replySecondComment(item, item2)"
                        >
                          {{ item2.createTime }}
                        </text>
                        <text
                          style="
                            font-size: 20rpx;
                            color: #afafb0;
                            margin-left: 20rpx;
                            white-space: nowrap;
                          "
                          @longpress="openCommentSetting(item2, item)"
                          @touchend="touchend"
                          @click="replySecondComment(item, item2)"
                        >
                          {{ item2.province }}
                        </text>
                        <text
                          style="
                            font-size: 20rpx;
                            color: #7d7d7d;
                            margin-left: 20rpx;
                            white-space: nowrap;
                          "
                          @longpress="openCommentSetting(item2, item)"
                          @touchend="touchend"
                          @click="replySecondComment(item, item2)"
                        >
                          回复
                        </text>
                      </view>
                    </view>
                    <view
                      v-if="item2.pictureUrl != null && item2.pictureUrl != ''"
                      style="margin-top: 10rpx"
                      @click="previewImage(item2.picture.url)"
                    >
                      <image
                        :src="item2.picture.url"
                        :style="{
                          height: item2.picture.height + 'rpx',
                          width: item2.picture.width + 'rpx'
                        }"
                        style="border-radius: 20rpx"
                        mode="aspectFill"
                      ></image>
                    </view>
                  </view>
                  <view
                    style="
                      width: 30px;
                      display: flex;
                      flex-direction: column;
                      justify-content: center;
                      text-align: center;
                      padding: 5rpx;
                      box-sizing: border-box;
                      position: absolute;
                      right: -40px;
                      top: 0;
                    "
                  >
                    <u-transition :show="!item2.isLike" mode="fade" duration="2000">
                      <u-icon
                        v-if="!item2.isLike"
                        name="/static/praise.png"
                        size="24"
                        @click="praiseComment(item2.id, item2.commentUserId, 2, [index, index2])"
                      ></u-icon>
                    </u-transition>
                    <u-transition :show="item2.isLike" mode="fade" duration="2000">
                      <u-icon
                        v-if="item2.isLike"
                        name="/static/praise_select.png"
                        size="24"
                        @click="praiseComment(item2.id, item2.commentUserId, 2, [index, index2])"
                      ></u-icon>
                    </u-transition>
                    <view v-if="item2.commentLikeNum > 0" style="font-size: 12px; color: #7d7d7d">
                      {{ item2.commentLikeNum }}
                    </view>
                  </view>
                </view>
              </block>
            </view>
            <u-loadmore
              v-if="item.commentReplyNum > 0 && item.children.list.length > 0"
              :fontSize="13"
              color="#5b7e91"
              style="width: 350rpx; letter-spacing: 0.05rem"
              :status="item.children.status"
              :loading-text="loadingText"
              :loadmore-text="item.children.loadmoreText"
              nomore-text=" "
              @loadmore="getReplyList(item.id)"
            />
          </view>
          <view
            style="
              width: 30px;
              display: flex;
              flex-direction: column;
              justify-content: center;
              text-align: center;
              padding: 5rpx;
              box-sizing: border-box;
            "
          >
            <u-transition :show="!item.isLike" mode="fade" duration="2000">
              <u-icon
                v-if="!item.isLike"
                name="/static/praise.png"
                size="24"
                @click="praiseComment(item.id, item.commentUserId, 1, [index])"
              ></u-icon>
            </u-transition>
            <u-transition :show="item.isLike" mode="fade" duration="2000">
              <u-icon
                v-if="item.isLike"
                name="/static/praise_select.png"
                size="24"
                @click="praiseComment(item.id, item.commentUserId, 1, [index])"
              ></u-icon>
            </u-transition>
            <view v-if="item.commentLikeNum > 0" style="font-size: 12px; color: #7d7d7d">
              {{ item.commentLikeNum }}
            </view>
          </view>
        </view>
        <!-- <u-divider style="padding-left: 100rpx;" :hairline="true"></u-divider> -->
      </block>
      <u-loadmore
        v-if="commentList.length > 0"
        line
        :status="status"
        :loading-text="loadingText"
        loadingIcon="semicircle"
      ></u-loadmore>
    </view>
    <u-popup :show="inputField" mode="bottom" @close="closeEditor">
      <view
        style="
          width: 100%;
          box-sizing: border-box;
          position: fixed;
          bottom: 0;
          background-color: #fff;
        "
      >
        <view @click="onEditClick">
          <lsj-edit
            class="lsjComment"
            style="height: auto"
            ref="lsjComment"
            :placeholder="editPlaceholder"
            :maxCount="1000"
            @onReady="editReady"
          ></lsj-edit>
        </view>
        <view
          style="
            display: flex;
            width: 100%;
            padding: 0 10rpx;
            box-sizing: border-box;
            height: 90rpx;
          "
        >
          <view
            style="display: flex; justify-content: space-around; width: 50%; align-items: center"
          >
            <view @touchend.prevent="chooseAite">
              <u-icon name="/static/aite.png" size="25"></u-icon>
            </view>
            <view @touchend.prevent="openEmoji">
              <u-icon
                name="/static/emoji.png"
                size="25"
                :color="showEmoji ? '#3d8af5' : '#666'"
              ></u-icon>
            </view>
            <view @touchend.prevent="chooseImage">
              <u-icon name="/static/photo.png" size="25"></u-icon>
            </view>
          </view>
          <view
            style="display: flex; margin-left: auto; margin-right: 20rpx; align-items: center"
            @touchend.prevent="sendComment"
          >
            <u-button style="height: 70rpx" size="mini" type="primary" shape="circle">
              发送
            </u-button>
          </view>
        </view>
        <view v-if="commentImagesurl != ''" style="padding: 30rpx; display: flex">
          <view style="position: relative">
            <image
              :src="commentImagesurl"
              style="height: 100rpx; width: 100rpx"
              mode="aspectFill"
            ></image>
            <view
              style="
                position: absolute;
                top: 0;
                right: 0;
                background-color: #7d7d7d;
                padding: 5rpx;
                border-bottom-left-radius: 50%;
                border-top-right-radius: 5rpx;
              "
            >
              <u-icon name="close" size="9" color="#f3f3f2" @click="deleteImage"></u-icon>
            </view>
          </view>
        </view>
        <scroll-view
          :scroll-x="true"
          style="background-color: #fff; display: flex; align-items: center; white-space: nowrap"
          v-if="bottomEmoji.length > 0 && !showEmoji"
        >
          <view style="display: inline-flex; align-items: center; padding: 0 15rpx">
            <!-- <block v-for="(em, index) in bottomEmoji" v-bind:key="index">
              <view
                @click="addEmoji(em)"
                style="padding: 15rpx; font-size: 48rpx; line-height: 70rpx; height: 70rpx"
              >
                {{ em }}
              </view>
            </block> -->
          </view>
        </scroll-view>
        <view v-if="showEmoji">
          <scroll-view
            :style="{ height: (keyboardHeight > 0 ? keyboardHeight : 300) + 'px' }"
            scroll-y
            style="background-color: aliceblue"
          >
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
        </view>
        <!-- <view v-if="showAite" style="background-color: #f3f3f2">
          <scroll-view style="height: 500rpx" @scrolltolower="getAttentionUser">
            <view v-if="attentionUser.list.length > 0" style="padding: 30rpx">
              <block v-for="(item, index) in attentionUser.list" v-bind:key="item.id">
                <view style="display: flex; padding: 10rpx 0; align-items: center">
                  <image
                    :src="item.avatarUrl"
                    style="height: 90rpx; width: 90rpx; border-radius: 50%"
                  ></image>
                  <view
                    style="
                      display: flex;
                      flex-direction: column;
                      justify-content: space-between;
                      margin-left: 20rpx;
                      flex: 1;
                    "
                  >
                    <view style="font-size: 30rpx; color: #2b2b2b">{{ item.nickname }}</view>
                  </view>
                  <view style="margin-left: auto">
                    <view
                      style="
                        width: 150rpx;
                        height: 60rpx;
                        line-height: 60rpx;
                        text-align: center;
                        border-radius: 30rpx;
                        background-color: #3d8af5;
                        color: #ffffff;
                        font-size: 25rpx;
                      "
                      @click="addUser(item)"
                    >
                      @Ta
                    </view>
                  </view>
                </view>
                <u-divider></u-divider>
              </block>
              <view style="margin-top: 70rpx">
                <u-loadmore :status="attentionUser.status" loadingIcon="spinner" line></u-loadmore>
              </view>
            </view>
            <view
              v-else
              style="display: flex; justify-content: center; align-items: center; height: 500rpx"
            >
              <view style="font-size: 25rpx; color: #afafb0">暂无数据</view>
            </view>
          </scroll-view>
        </view> -->
      </view>
    </u-popup>
    <view
      v-if="!inputField"
      style="
        position: fixed;
        bottom: 0;
        display: flex;
        padding: 20rpx;
        box-sizing: border-box;
        height: 60px;
        width: 100%;
        background-color: #fff;
      "
    >
      <view class="bottom-edit" @click="replyNotes">
        <u-icon name="/static/icons_edit.png" size="22"></u-icon>
        <view style="font-size: 26rpx; color: #afafb0; margin-left: 10rpx">说点什么...</view>
      </view>
      <view class="bottom-icon">
        <view style="display: flex; align-items: center; margin: 0 10rpx">
          <u-transition :show="!notesDetail.isLike" mode="fade" duration="2000">
            <u-icon
              v-if="!notesDetail.isLike"
              name="/static/praise.png"
              size="28"
              @click="praiseNotes(notesDetail.id)"
            ></u-icon>
          </u-transition>
          <u-transition :show="notesDetail.isLike" mode="fade" duration="2000">
            <u-icon
              v-if="notesDetail.isLike"
              name="/static/praise_select.png"
              size="28"
              @click="praiseNotes(notesDetail.id)"
            ></u-icon>
          </u-transition>
          <view v-if="notesDetail.notesLikeNum > 0">{{ notesDetail.notesLikeNum }}</view>
          <view style="font-size: 30rpx" v-else>点赞</view>
        </view>
        <view style="display: flex; align-items: center; margin: 0 10rpx">
          <u-transition :show="!notesDetail.isCollect" mode="fade" duration="2000">
            <u-icon
              v-if="!notesDetail.isCollect"
              name="/static/collect.png"
              size="28"
              @click="collectNotes(notesDetail.id)"
            ></u-icon>
          </u-transition>
          <u-transition :show="notesDetail.isCollect" mode="fade" duration="2000">
            <u-icon
              v-if="notesDetail.isCollect"
              name="/static/collect_select.png"
              size="28"
              @click="collectNotes(notesDetail.id)"
            ></u-icon>
          </u-transition>
          <view v-if="notesDetail.notesCollectNum > 0">{{ notesDetail.notesCollectNum }}</view>
          <view style="font-size: 30rpx" v-else>收藏</view>
        </view>
        <view style="display: flex; align-items: center; margin: 0 10rpx">
          <u-icon name="/static/comment.png" size="28"></u-icon>
          <view v-if="commentCount > 0">{{ commentCount }}</view>
          <view style="font-size: 30rpx" v-else>评论</view>
        </view>
      </view>
    </view>
    <view style="height: 60px"></view>
    <u-popup
      :show="showShare"
      mode="bottom"
      @close="showShare = false"
      round="10"
      bgColor="#f5f5f5"
    >
      <view
        :style="{ height: windowHeight * (1 / 6) + 'px' }"
        style="width: 750rpx; display: flex; flex-direction: column"
      >
        <view
          style="
            display: flex;
            justify-content: center;
            text-align: center;
            width: 100%;
            padding: 30rpx;
            box-sizing: border-box;
            position: relative;
            flex: 1;
          "
        >
          <view>分享至</view>
          <!-- <u-icon
            style="position: absolute; right: 20rpx"
            name="close"
            size="20"
            @click="showShare = false"
          ></u-icon> -->
        </view>
        <scroll-view
          scroll-x
          style="
            white-space: nowrap;
            display: flex;
            width: 100%;
            border-top-style: solid;
            border-top-width: 1rpx;
            border-color: #e5e4e6;
            flex: 3;
            align-items: center;
          "
          enable-flex
        >
          <!-- #ifdef MP-WEIXIN -->
          <button
            open-type="share"
            class="share-item"
            @click="showShare = false"
          >
            <u-icon name="weixin-circle-fill" size="50" color="#07c160"></u-icon>
            <view style="font-size: 30rpx; color: #727171">微信好友</view>
          </button>
          <!-- 朋友圈只能通过右上角菜单分享，按钮仅做引导 -->
          <view
            class="share-item"
            @click="handleShareTimeline"
          >
            <u-icon name="qzone-circle-fill" size="50" color="#07c160"></u-icon>
            <view style="font-size: 30rpx; color: #727171">朋友圈</view>
          </view>
          <!-- #endif -->
          <!-- #ifndef MP-WEIXIN -->
          <view
            @click="handleShareClick"
            style="
              display: inline-flex;
              flex-direction: column;
              text-align: center;
              padding: 20rpx;
              justify-content: center;
              align-items: center;
            "
          >
            <u-icon name="weixin-circle-fill" size="50" color="#07c160"></u-icon>
            <view style="font-size: 30rpx; color: #727171">微信好友</view>
          </view>
          <view
            @click="handleShareTimelineClick"
            style="
              display: inline-flex;
              flex-direction: column;
              text-align: center;
              padding: 20rpx;
              justify-content: center;
              align-items: center;
            "
          >
            <u-icon name="qzone-circle-fill" size="50" color="#07c160"></u-icon>
            <view style="font-size: 30rpx; color: #727171">朋友圈</view>
          </view>
          <!-- #endif -->
          <!-- <view
						style="display: inline-flex;flex-direction: column;text-align: center;padding: 20rpx;justify-content: center;align-items: center;">
						<u-icon name="weixin-circle-fill" size="50" color="#49e679"></u-icon>
						<view style="font-size: 30rpx;color: #727171;">微信好友</view>
					</view> -->
          <!-- <view
						style="display: inline-flex;flex-direction: column;text-align: center;padding: 20rpx;justify-content: center;align-items: center;">
						<u-icon name="qq-circle-fill" size="50" color="#008dff"></u-icon>
						<view style="font-size: 30rpx;color: #727171;">QQ</view>
					</view>
					<view
						style="display: inline-flex;flex-direction: column;text-align: center;padding: 20rpx;justify-content: center;align-items: center;">
						<u-icon name="weibo-circle-fill" size="50" color="#c52f2b"></u-icon>
						<view style="font-size: 30rpx;color: #727171;">微博</view>
					</view> -->
        </scroll-view>
        <!-- <scroll-view scroll-x v-if="userInfo.id==notesDetail.belongUserId"
					style="white-space: nowrap;display: flex;width: 100%;border-top-style:solid;border-top-width: 1rpx;border-top-color: #e5e4e6;flex: 3;"
					enable-flex>
					<view @click="editNotes"
						style="display: inline-flex;flex-direction: column;text-align: center;padding: 20rpx;justify-content: center;">
						<view style="padding: 20rpx;border-radius: 50%;background-color: #ffffff;">
							<u-icon name="/static/image/editNotes.png" size="35"></u-icon>
						</view>
						<view style="font-size: 30rpx;color: #727171;">编辑</view>
					</view>
					<view
						style="display: inline-flex;flex-direction: column;text-align: center;padding: 20rpx;justify-content: center;">
						<u-icon name="account" size="50" color="#cdcdcd"></u-icon>
						<view style="font-size: 30rpx;color: #727171;">权限设置</view>
					</view>
					<view @click="deleteNotes"
						style="display: inline-flex;flex-direction: column;text-align: center;padding: 20rpx;justify-content: center;">
						<view style="padding: 20rpx;border-radius: 50%;background-color: #ffffff;">
							<u-icon name="/static/image/delete.png" size="35"></u-icon>
						</view>
						<view style="font-size: 30rpx;color: #727171;">删除</view>
					</view>
				</scroll-view> -->
      </view>
    </u-popup>
  </view>
</template>

<script>
import { emojiList } from '@/utils/emojiUtil.js';
import {
  getNotesByNotesId,
  praiseOrCancelNotes,
  collectOrCancelNotes,
  deleteNotes
} from '@/apis/notes_service.js';
import { weChatTimeFormat, replaceHTMLTags } from '@/utils/util.js';
import { getAttentionList, updateAttention } from '@/apis/user_service';
import {
  addComment,
  addCommentSync,
  getCommentFirstListByNotesId,
  getCommentSecondListByNotesId,
  praiseOrCancelComment,
  setNotesTopComment,
  deleteNotesComment
} from '@/apis/comment_service';
import { addBrowseRecord } from '@/apis/browse_record_service';
import { reportNoteBehavior } from '@/apis/note_behavior_service.js';
import { baseUrl } from '@/.env.js';
import { getWeChatJSSDKConfig } from '@/apis/third_service.js';
import { initWeChatShare } from '@/utils/wechatJSSDK.js';
import weixinNavBar from '@/utils/weixinNavBar.js';
export default {
  mixins: [weixinNavBar],
  // 微信小程序分享到好友
  onShareAppMessage(res) {
    console.log('触发分享', res);
    this.reportShareBehavior();
    return {
      title: this.notesDetail.title || '来自爱宠社的精彩内容',
      path: `/pkg-detail/pages/notesDetail/notesDetail?notesId=${this.notesDetail.id}`,
      imageUrl:
        this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0
          ? this.notesDetail.notesResources[0].url
          : '/static/logoImage.png'
    };
  },
  // 微信小程序分享到朋友圈
  onShareTimeline() {
    this.reportShareBehavior();
    return {
      title: this.notesDetail.title || '来自爱宠社的精彩内容',
      query: `notesId=${this.notesDetail.id}`,
      imageUrl:
        this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0
          ? this.notesDetail.notesResources[0].url
          : '/static/logoImage.png'
    };
  },
  data() {
    return {
      showShare: false,
      windowHeight: 0,
      editPlaceholder: '爱评论的人运气都不差',
      statusBarHeight: 0,
      notesDetail: {
        content: null,
        title: '',
        nickname: '',
        avatarUrl: '',
        belongUserId: 0,
        notesResources: [],
        isFollow: false,
        isLike: false,
        isCollect: false,
        notesLikeNum: 0,
        notesCollectNum: 0,
        createTime: '',
        updateTime: '',
        province: '',
        city: '',
        district: '',
        address: ''
      },
      commentCount: 0,
      commentList: [],
      page: 1,
      pageSize: 10,
      status: 'loadmore',
      swipperHeight: 0,
      swiperCurrent: 0,
      swipperCount: 0,
      userInfo: {},
      edit: null,
      keyboardHeight: 0,
      inputField: false,
      showEmoji: false,
      showAite: false,
      attentionUser: {
        list: [],
        page: 1,
        pageSize: 10,
        status: 'loading',
        isLoading: false,
        isNoMore: false
      },
      emojiList: [],
      bottomEmoji: [],
      commentImagesurl: '',
      content: '',
      revealcontent: '',
      replyCommentUserId: 0,
      replyCommentUserName: '',
      parentId: 0,
      loadingText: '加载中...',
      showBubble: false,
      isLongPress: false,
      detailOpenTime: 0,
      shareScene: 'other'
    };
  },
  onUnload() {
    if (this.detailOpenTime > 0 && this.notesDetail && this.notesDetail.id) {
      const durationSec = Math.floor((Date.now() - this.detailOpenTime) / 1000);
      const isVideo = this.notesDetail.noteType === 2;
      reportNoteBehavior({
        eventType: 'watch',
        nid: String(this.notesDetail.id),
        durationSec,
        completed: isVideo ? 0 : undefined
      });
    }
  },
  methods: {
    reportShareBehavior() {
      if (!this.notesDetail || !this.notesDetail.id) return;
      reportNoteBehavior({
        eventType: 'share',
        nid: String(this.notesDetail.id),
        scene: this.shareScene || 'other'
      }).catch(() => {});
    },
    // H5环境点击分享提示
    async handleShareClick() {
      // #ifdef H5
      try {
        // 检查是否在微信浏览器中
        // #ifdef H5
        const ua = typeof navigator !== 'undefined' ? navigator.userAgent : '';
        const isWeChat = /micromessenger/i.test(ua);
        if (!isWeChat) {
          uni.showToast({
            title: '请在微信中打开',
            icon: 'none',
            duration: 2000
          });
          this.showShare = false;
          return;
        }
        // #endif

        // 准备分享数据
        let shareLink = '';
        let shareImgUrl = '';
        // #ifdef H5
        if (typeof window !== 'undefined' && window.location) {
          shareLink = window.location.href.split('#')[0] + `?notesId=${this.notesDetail.id}`;
          shareImgUrl =
            this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0
              ? this.notesDetail.notesResources[0].url
              : `${window.location.origin}/static/logoImage.png`;
        } else {
          // 备用方案：使用baseUrl
          shareLink = `${baseUrl
            .replace('/prod-api', '')
            .replace('/dev-api', '')}/pkg-detail/pages/notesDetail/notesDetail?notesId=${
            this.notesDetail.id
          }`;
          shareImgUrl =
            this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0
              ? this.notesDetail.notesResources[0].url
              : `${baseUrl.replace('/prod-api', '').replace('/dev-api', '')}/static/logoImage.png`;
        }
        // #endif

        const shareData = {
          title: this.notesDetail.title || '来自爱宠社的精彩内容',
          desc: this.notesDetail.content
            ? this.notesDetail.content.replace(/<[^>]+>/g, '').substring(0, 100)
            : '来自爱宠社的精彩内容',
          link: shareLink,
          imgUrl: shareImgUrl
        };

        // 初始化微信分享（分享给朋友）
        await initWeChatShare(params => getWeChatJSSDKConfig(params), shareData);
        this.reportShareBehavior();

        // 提示用户使用右上角菜单分享
        uni.showToast({
          title: '请点击右上角菜单分享',
          icon: 'none',
          duration: 2000
        });
        this.showShare = false;
      } catch (error) {
        console.error('微信分享初始化失败:', error);
        uni.showToast({
          title: error.message || '分享功能初始化失败',
          icon: 'none',
          duration: 2000
        });
        this.showShare = false;
      }
      // #endif
      // #ifndef H5
      uni.showToast({
        title: '请使用微信打开后分享',
        icon: 'none',
        duration: 2000
      });
      this.showShare = false;
      // #endif
    },
    // 处理分享到朋友圈（微信小程序：只能通过右上角菜单分享，此处仅提示）
    handleShareTimeline() {
      // #ifdef MP-WEIXIN
      uni.showToast({
        title: '请点击右上角 ··· 选择「分享到朋友圈」',
        icon: 'none',
        duration: 2500
      });
      this.showShare = false;
      // #endif
      // #ifndef MP-WEIXIN
      this.showShare = false;
      // #endif
    },
    // H5环境点击分享到朋友圈提示
    async handleShareTimelineClick() {
      // #ifdef H5
      try {
        // 检查是否在微信浏览器中
        // #ifdef H5
        const ua = typeof navigator !== 'undefined' ? navigator.userAgent : '';
        const isWeChat = /micromessenger/i.test(ua);
        if (!isWeChat) {
          uni.showToast({
            title: '请在微信中打开',
            icon: 'none',
            duration: 2000
          });
          this.showShare = false;
          return;
        }
        // #endif

        // 准备分享数据
        let shareLink = '';
        let shareImgUrl = '';
        // #ifdef H5
        if (typeof window !== 'undefined' && window.location) {
          shareLink = window.location.href.split('#')[0] + `?notesId=${this.notesDetail.id}`;
          shareImgUrl =
            this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0
              ? this.notesDetail.notesResources[0].url
              : `${window.location.origin}/static/logoImage.png`;
        } else {
          // 备用方案：使用baseUrl
          shareLink = `${baseUrl
            .replace('/prod-api', '')
            .replace('/dev-api', '')}/pkg-detail/pages/notesDetail/notesDetail?notesId=${
            this.notesDetail.id
          }`;
          shareImgUrl =
            this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0
              ? this.notesDetail.notesResources[0].url
              : `${baseUrl.replace('/prod-api', '').replace('/dev-api', '')}/static/logoImage.png`;
        }
        // #endif

        const shareData = {
          title: this.notesDetail.title || '来自爱宠社的精彩内容',
          link: shareLink,
          imgUrl: shareImgUrl
        };

        // 初始化微信分享（分享到朋友圈）
        await initWeChatShare(params => getWeChatJSSDKConfig(params), shareData);
        this.reportShareBehavior();

        // 提示用户使用右上角菜单分享
        uni.showToast({
          title: '请点击右上角菜单分享到朋友圈',
          icon: 'none',
          duration: 2000
        });
        this.showShare = false;
      } catch (error) {
        console.error('微信分享初始化失败:', error);
        uni.showToast({
          title: error.message || '分享功能初始化失败',
          icon: 'none',
          duration: 2000
        });
        this.showShare = false;
      }
      // #endif
      // #ifndef H5
      uni.showToast({
        title: '请使用微信打开后分享到朋友圈',
        icon: 'none',
        duration: 2000
      });
      this.showShare = false;
      // #endif
    },
    deleteNotes() {
      this.$showModal({
        title: '提示',
        content: '是否删除笔记?',
        cancelText: '取消', // 取消按钮的文字
        cancelColor: '#949495', // 取消按钮的文字颜色
        confirmText: '确定', // 确认按钮文字
        confirmColor: '#3d8af5', // 确认按钮颜色
        showCancel: true // 是否显示取消按钮，默认为 true
      }).then(res => {
        deleteNotes({
          notesId: this.notesDetail.id
        }).then(res => {
          console.log(res);
          if (res.code == 20040) {
            uni.showToast({
              title: '删除成功',
              icon: 'none'
            });
            uni.navigateBack({
              delta: 1
            });
          } else {
            uni.showToast({
              title: res.msg == '' ? '删除失败' : res.msg,
              icon: 'none'
            });
          }
        });
      });
    },
    editNotes() {
      // uni.navigateTo({
      // 	url: '/pages/publishNotes/publishNotes?update=2&notesId=' + this.notesDetail.id
      // })
    },
    /**
     * 关注用户
     */
    attention() {
      // uni.showModal({
      // 	title: '提示',
      // 	content: this.notesDetail.isFollow ? '是否取消关注?' : '是否关注?',
      // 	cancelText: "取消",
      // 	cancelColor: "#949495",
      // 	confirmText: "确定",
      // 	confirmColor: "#3d8af5",
      // 	showCancel: true,
      // 	success: (res) => {
      // 		if (res.confirm) {
      updateAttention({
        userId: this.userInfo.id,
        targetUserId: this.notesDetail.belongUserId
      }).then(res => {
        if (res.code == 200) {
          this.notesDetail.isFollow = !this.notesDetail.isFollow;
          uni.showToast({
            title: this.notesDetail.isFollow ? '关注成功' : '取消关注成功',
            icon: 'none'
          });
        }
      });
      // 		}
      // 	}
      // })
    },
    openCommentSetting(e, e1) {
      this.isLongPress = true;
      let itemList = ['置顶', '回复', '复制', '删除'];
      if (this.notesDetail.belongUserId != this.userInfo.id || e.parentId != 0) {
        itemList.shift();
      } else {
        if (e.isTop) {
          itemList[0] = '取消置顶';
        }
      }
      if (
        this.notesDetail.belongUserId != this.userInfo.id &&
        e.commentUserId != this.userInfo.id
      ) {
        itemList.pop();
      }
      this.$showActionSheet({
        itemList: itemList,
        success: data => {
          let text = itemList[data.tapIndex];
          console.log(text);
          if (text === '置顶') {
            this.topComment(e.id, 0);
          }
          if (text === '取消置顶') {
            this.topComment(e.id, 1);
          }
          if (text === '回复') {
            if (e1 == 0) {
              this.replyFirstComment(e);
            } else {
              this.replySecondComment(e1, e);
            }
          }
          if (text === '复制') {
            let a = replaceHTMLTags(e.content);
            uni.setClipboardData({
              data: a,
              success: function () {
                uni.showToast({
                  title: '复制成功',
                  icon: 'none'
                });
              }
            });
          }
          if (text === '删除') {
            this.deleteComment(e);
          }
        }
      });
    },
    /** 置顶评论
     * @param {Object} id
     */
    topComment(id, type) {
      this.$showModal({
        title: '提示',
        content: type == 0 ? '是否置顶评论?' : '是否取消置顶评论?',
        cancelText: '取消', // 取消按钮的文字
        cancelColor: '#949495', // 取消按钮的文字颜色
        confirmText: '确定', // 确认按钮文字
        confirmColor: '#3d8af5', // 确认按钮颜色
        showCancel: true // 是否显示取消按钮，默认为 true
      }).then(res => {
        setNotesTopComment({
          commentId: id
        }).then(res => {
          console.log(res);
          if (res.code == 200) {
            uni.showToast({
              title: type == 0 ? '置顶成功' : '取消置顶成功',
              icon: 'none'
            });
            // 将置顶的评论放到第一位
            this.commentList.forEach((item, index) => {
              if (item.id == id) {
                if (type == 0) {
                  item.isTop = true;
                  this.commentList.splice(index, 1);
                  this.commentList.unshift(item);
                } else {
                  item.isTop = false;
                }
              } else {
                item.isTop = false;
              }
            });
          } else {
            uni.showToast({
              title: res.msg == null ? (type == 0 ? '置顶失败' : '取消置顶失败') : res.msg,
              icon: 'none'
            });
          }
        });
      });
    },
    /** 删除评论
     * @param {Object} e
     */
    deleteComment(e) {
      this.$showModal({
        title: '提示',
        content: '是否删除评论?',
        cancelText: '取消', // 取消按钮的文字
        cancelColor: '#949495', // 取消按钮的文字颜色
        confirmText: '确定', // 确认按钮文字
        confirmColor: '#3d8af5', // 确认按钮颜色
        showCancel: true // 是否显示取消按钮，默认为 true
      }).then(res => {
        deleteNotesComment({
          commentId: e.id
        }).then(res => {
          console.log(res);
          if (res.code == 200) {
            uni.showToast({
              title: '删除成功',
              icon: 'none'
            });
            // 删除评论
            if (e.parentId == '0') {
              this.commentList.forEach((item, index) => {
                if (item.id == e.id) {
                  this.commentList.splice(index, 1);
                }
              });
            } else {
              this.commentList.forEach((item, index) => {
                if (item.id == e.parentId) {
                  item.children.list.forEach((item2, index2) => {
                    if (item2.id == e.id) {
                      item.children.list.splice(index2, 1);
                    }
                  });
                }
              });
            }
          } else {
            uni.showToast({
              title: res.msg == null ? '删除失败' : res.msg,
              icon: 'none'
            });
          }
        });
      });
    },
    touchend() {
      setTimeout(() => {
        this.isLongPress = false;
      }, 200);
    },
    /**
     * 点赞或取消点赞笔记
     * @param {Object} id
     */
    praiseNotes(id) {
      praiseOrCancelNotes({
        notesId: id,
        userId: this.userInfo.id,
        targetUserId: this.notesDetail.belongUserId
      }).then(res => {
        console.log(res);
        if (res.code == 200) {
          if (this.notesDetail.isLike) {
            this.notesDetail.notesLikeNum = this.notesDetail.notesLikeNum - 1;
            this.notesDetail.isLike = false;
          } else {
            this.notesDetail.notesLikeNum = this.notesDetail.notesLikeNum + 1;
            this.notesDetail.isLike = true;
          }
        }
      });
    },
    /**
     * 收藏或取消收藏笔记
     * @param {Object} id
     */
    collectNotes(id) {
      collectOrCancelNotes({
        notesId: id,
        userId: this.userInfo.id,
        targetUserId: this.notesDetail.belongUserId
      }).then(res => {
        console.log(res);
        if (res.code == 200) {
          if (this.notesDetail.isCollect) {
            this.notesDetail.notesCollectNum = this.notesDetail.notesCollectNum - 1;
            this.notesDetail.isCollect = false;
          } else {
            this.notesDetail.notesCollectNum = this.notesDetail.notesCollectNum + 1;
            this.notesDetail.isCollect = true;
          }
        }
      });
    },
    /** 点赞或取消点赞评论
     * @param {Object} id 评论id
     * @param {Object} userId 评论人id
     * @param {Object} type 评论类型 1:一级评论 2:二级评论
     * @param {Object} index 评论下标，数组，[一级评论下标，二级评论下标]
     */
    praiseComment(id, userId, type, indexPath) {
      console.log(id, userId, type, indexPath);
      praiseOrCancelComment({
        commentId: id,
        userId: this.userInfo.id,
        targetUserId: userId
      }).then(res => {
        console.log(res);
        if (res.code == 200) {
          const path = Array.isArray(indexPath) ? indexPath : [];
          const i0 = typeof path[0] === 'number' ? path[0] : Number(path[0]);
          const i1 = typeof path[1] === 'number' ? path[1] : Number(path[1]);

          if (!Number.isFinite(i0) || !Array.isArray(this.commentList) || !this.commentList[i0]) return;

          if (type == 1) {
            const item = this.commentList[i0];
            item.isLike = !item.isLike;
            item.commentLikeNum = (Number(item.commentLikeNum) || 0) + (item.isLike ? 1 : -1);
            if (item.commentLikeNum < 0) item.commentLikeNum = 0;
            return;
          }

          const child = this.commentList[i0]?.children?.list;
          if (!Number.isFinite(i1) || !Array.isArray(child) || !child[i1]) return;
          const item2 = child[i1];
          item2.isLike = !item2.isLike;
          item2.commentLikeNum = (Number(item2.commentLikeNum) || 0) + (item2.isLike ? 1 : -1);
          if (item2.commentLikeNum < 0) item2.commentLikeNum = 0;
        }
      });
    },
    /** 点击用户头像跳转到用户主页
     * @param {Object} e
     */
    clickUser(e) {
      console.log(e);
      if (e.detail.node.name != 'a') {
        return;
      }
      let href = e.detail.node.attrs.href || '';
      // 检查是否是用户链接（新格式）
      if (href.includes('/pkg-mine/pages/mine/otherMine')) {
        const userNameMatch = href.match(/userName=([^&]+)/);
        if (userNameMatch) {
          const userName = decodeURIComponent(userNameMatch[1]);
          uni.navigateTo({
            url: '/pkg-mine/pages/mine/otherMine?userName=' + encodeURIComponent(userName)
          });
          return;
        }
        const userIdMatch = href.match(/userId=([^&]+)/);
        if (userIdMatch) {
          const userId = decodeURIComponent(userIdMatch[1]);
          uni.navigateTo({
            url: '/pkg-mine/pages/mine/otherMine?userId=' + userId
          });
          return;
        }
      }
      // 兼容旧格式
      let userId = this.findUserId(href);
      if (userId) {
        this.goToOtherMine(userId);
      }
    },
    /** 获取一级评论的子评论
     * @param {Object} e
     */
    getReplyList(id) {
      this.commentList.forEach(item => {
        if (item.id == id) {
          if (item.children.status == 'loading' || item.children.status == 'nomore') {
            return;
          }
          item.children.status = 'loading';
          getCommentSecondListByNotesId({
            notesId: this.notesDetail.id,
            parentId: id,
            page: item.children.page,
            pageSize: item.children.pageSize
          })
            .then(res => {
              if (res.code == 200) {
                const records = Array.isArray(res.data.records) ? res.data.records : [];
                const mappedArr = records.map(child => ({
                  id: child.id,
                  parentId: child.pid,
                  notesId: child.nid,
                  commentUserId: child.uid,
                  commentUserName: child.username,
                  commentUserAvatar: child.avatar,
                  content: this.formatCommentContent(child.content),
                  isLike: child.isLike,
                  commentLikeNum: child.likeCount,
                  commentReplyNum: child.twoCommentCount,
                  createTime: weChatTimeFormat(Number(child.createTime))
                }));
                if (mappedArr.length < item.children.pageSize) {
                  item.children.status = 'nomore';
                } else {
                  item.children.status = 'loadmore';
                }
                item.children.list.push(...mappedArr);
                item.children.page++;
              } else {
                item.children.status = 'nomore';
              }
            })
            .catch(() => {
              item.children.status = 'nomore';
            });
        }
      });
    },
    /**
     * 发送评论
     */
    sendComment() {
      uni.showLoading({
        title: '发送中...',
        mask: true
      });
      this.edit.getContents().then(res => {
        console.log(res);
        // 如果res.text里面只有换行符或者空格，res.html也没有<img>标签，就不允许发布
        const regex = /^[\n\s]+$/;
        const regex2 = /<img/g;
        if (regex.test(res.text) && this.commentImagesurl == '' && !regex2.test(res.html)) {
          uni.hideLoading();
          uni.showToast({
            title: '请输入内容',
            icon: 'none'
          });
          return;
        }
        this.content = res.html;
        if (this.commentImagesurl == '') {
          addComment({
            content: this.content,
            replyUserId: this.replyCommentUserId,
            replyUserName: this.replyCommentUserName,
            notesId: this.notesDetail.id,
            commentUserId: this.userInfo.id,
            parentId: this.parentId,
            pictureUrl: this.commentImagesurl
          })
            .then(res => {
              uni.hideLoading();
              console.log(res);
              if (res.code == 200) {
                // 同步评论
                const id = res.data.id;
                addCommentSync([id]); // 用数组包裹

                uni.showToast({
                  title: '评论已发布',
                  icon: 'success'
                });
                this.content = '';
                this.inputField = false;
                this.commentShow = false;
                this.commentCount++;
                console.log('评论发送成功，关闭输入框，inputField:', this.inputField);
                // 确保输入框关闭
                setTimeout(() => {
                  this.inputField = false;
                  console.log('延迟确认关闭输入框，inputField:', this.inputField);
                }, 100);
                res.data.createTime = weChatTimeFormat(Number(res.data.createTime));
                // 映射字段名，确保头像能正确显示
                res.data.commentUserAvatar = res.data.avatar;
                res.data.commentUserName = res.data.username;
                res.data.commentUserId = res.data.uid;
                console.log(res.data);
                if (this.parentId == 0) {
                  res.data.children = {
                    list: [],
                    page: 1,
                    pageSize: 10,
                    status: 'loadmore',
                    loadmoreText: '—— 展开更多回复 ——'
                  };
                  console.log(res.data);
                  this.commentList.unshift(res.data);
                } else {
                  this.commentList.forEach(item => {
                    if (item.id == this.parentId) {
                      console.log(item);
                      if (item.children.page == 1) {
                        item.commentReplyNum++;
                        item.children.loadmoreText = '—— 展开' + item.commentReplyNum + '条回复 ——';
                      } else {
                        item.children.list.unshift(res.data);
                      }
                    }
                  });
                }
              } else {
                uni.showToast({
                  title: res.msg || '评论失败',
                  icon: 'none'
                });
              }
            })
            .catch(err => {
              uni.hideLoading();
              // 显示后端返回的具体错误消息
              const errorMsg = err?.msg || err?.message || '评论失败';
              uni.showToast({
                title: errorMsg,
                icon: 'none',
                duration: 2500
              });
            });
          return;
        } else {
          uni.uploadFile({
            url: baseUrl + '/web/oss/upload',
            filePath: this.commentImagesurl,
            name: 'file',
            header: {
              accessToken: uni.getStorageSync('uniapp_token') || ''
            },
            success: res => {
              let data = JSON.parse(res.data);
              if (data.code === 200) {
                console.log('图片上传成功:', data.data);
                // 直接传递图片URL作为content
                addComment({
                  content: data.data,
                  replyUserId: this.replyCommentUserId,
                  replyUserName: this.replyCommentUserName,
                  notesId: this.notesDetail.id,
                  commentUserId: this.userInfo.id,
                  parentId: this.parentId,
                  pictureUrl: data.data
                })
                  .then(res => {
                    console.log(res);
                    if (res.code == 200) {
                      // 同步评论
                      const id = res.data.id;
                      addCommentSync([id]); // 用数组包裹

                      // 立即关闭评论框和输入框
                      this.content = '';
                      this.revealcontent = '';
                      this.commentImagesurl = '';
                      this.inputField = false;
                      this.commentShow = false;

                      uni.hideLoading();
                      uni.showToast({
                        title: '评论已发布',
                        icon: 'success'
                      });
                      this.commentCount++;

                      // 格式化评论内容
                      res.data.content = this.formatCommentContent(res.data.content);
                      res.data.createTime = weChatTimeFormat(Number(res.data.createTime));
                      // 映射字段名，确保头像能正确显示
                      res.data.commentUserAvatar = res.data.avatar;
                      res.data.commentUserName = res.data.username;
                      res.data.commentUserId = res.data.uid;

                      uni.getImageInfo({
                        src: res.data.pictureUrl,
                        success: image => {
                          res.data.picture = {
                            url: res.data.pictureUrl
                          };
                          // 图片长度最长为350rpx,高度最高为350rpx
                          if (image.width >= image.height) {
                            if (image.width > 350) {
                              res.data.picture.width = 350;
                              res.data.picture.height = (350 * image.height) / image.width;
                            } else {
                              res.data.picture.width = image.width;
                              res.data.picture.height = image.height;
                            }
                          } else {
                            if (image.height > 350) {
                              res.data.picture.height = 350;
                              res.data.picture.width = (350 * image.width) / image.height;
                            } else {
                              res.data.picture.width = image.width;
                              res.data.picture.height = image.height;
                            }
                          }
                          console.log(res.data.picture);

                          if (this.parentId == 0) {
                            res.data.children = {
                              list: [],
                              page: 1,
                              pageSize: 10,
                              status: 'loadmore',
                              loadmoreText: '—— 展开更多回复 ——'
                            };
                            this.commentList.unshift(res.data);
                          } else {
                            this.commentList.forEach(item => {
                              if (item.id == this.parentId) {
                                console.log(item);
                                if (item.children.page == 1) {
                                  item.commentReplyNum++;
                                  item.children.loadmoreText =
                                    '—— 展开' + item.commentReplyNum + '条回复 ——';
                                } else {
                                  item.children.list.unshift(res.data);
                                }
                              }
                            });
                          }
                        },
                        fail: err => {
                          console.log(err);
                          // 即使获取图片信息失败，也要添加评论到列表
                          if (this.parentId == 0) {
                            res.data.children = {
                              list: [],
                              page: 1,
                              pageSize: 10,
                              status: 'loadmore',
                              loadmoreText: '—— 展开更多回复 ——'
                            };
                            this.commentList.unshift(res.data);
                          } else {
                            this.commentList.forEach(item => {
                              if (item.id == this.parentId) {
                                if (item.children.page == 1) {
                                  item.commentReplyNum++;
                                  item.children.loadmoreText =
                                    '—— 展开' + item.commentReplyNum + '条回复 ——';
                                } else {
                                  item.children.list.unshift(res.data);
                                }
                              }
                            });
                          }
                        }
                      });
                    } else {
                      uni.hideLoading();
                      uni.showToast({
                        title: res.msg == null ? '评论失败' : res.msg,
                        icon: 'none'
                      });
                      this.inputField = false;
                      this.commentShow = false;
                    }
                  })
                  .catch(err => {
                    uni.hideLoading();
                    const errorMsg = err?.msg || err?.message || '评论失败';
                    uni.showToast({
                      title: errorMsg,
                      icon: 'none',
                      duration: 2500
                    });
                    this.inputField = false;
                    this.commentShow = false;
                  });
              } else {
                uni.hideLoading();
                uni.showToast({
                  title: '图片上传失败',
                  icon: 'none'
                });
                this.inputField = false;
              }
            },
            fail: err => {
              uni.hideLoading();
              uni.showToast({
                title: '图片上传失败',
                icon: 'none'
              });
              this.inputField = false;
            }
          });
        }
      });
    },
    /**
     * 回复一级评论
     * @param {Object} item 一级评论
     */
    replyFirstComment(item) {
      if (this.isLongPress) {
        return;
      }
      this.editPlaceholder = '回复 @' + item.commentUserName;
      if (this.replyCommentUserId != item.commentUserId) {
        this.content = '';
        this.revealcontent = '';
      }
      this.replyCommentUserId = 0;
      this.replyCommentUserName = '';
      this.parentId = item.id;
      this.openEditor();
    },
    /**
     * 回复二级评论
     * @param {Object} fitem 一级评论
     * @param {Object} item 二级评论
     */
    replySecondComment(fitem, item) {
      if (this.isLongPress) {
        return;
      }
      this.editPlaceholder = '回复 @' + item.commentUserName;
      this.content = '';
      this.revealcontent = '';
      this.replyCommentUserId = item.commentUserId;
      this.replyCommentUserName = item.commentUserName;
      this.parentId = fitem.id;
      this.openEditor();
    },
    /**
     * 回复笔记，即发布一级评论
     */
    replyNotes() {
      this.editPlaceholder = '爱评论的人运气都不差';
      if (this.replyCommentUserId != 0) {
        this.content = '';
        this.revealcontent = '';
      }
      this.replyCommentUserId = 0;
      this.replyCommentUserName = '';
      this.parentId = 0;
      this.openEditor();
    },
    /**
     * 弹出输入框
     */
    openEditor() {
      this.inputField = true;
      this.showEmoji = false;
      this.showAite = false;
      setTimeout(() => {
        this.$refs.lsjComment.keyboardShow();
      }, 700);
    },

    /**
     * 点击输入框
     */
    onEditClick() {
      this.showEmoji = false;
      this.showAite = false;
    },
    /**
     * 关闭输入框
     */
    closeEditor() {
      this.inputField = false;
      this.showEmoji = false;
      this.showAite = false;
      this.edit.getContents().then(res => {
        console.log(res);
        this.content = res.html;
        this.revealcontent = res.text;
      });
      uni.hideKeyboard();
    },
    /**
     * 预览图片
     */
    previewImage(url) {
      uni.previewImage({
        urls: [url]
      });
    },
    /**
     * 前往对方用户主页
     * @param {Object} userId
     */
    goToOtherMine(userId) {
      const currentUserId = uni.getStorageSync('userInfo').id;
      // 如果点击的是当前登录用户的头像，跳转到mine页面
      if (userId == currentUserId) {
        uni.navigateTo({
          url: '/pkg-main/pages/mine/mine'
        });
      } else {
        // 否则跳转到otherMine页面
        uni.navigateTo({
          url: '/pkg-mine/pages/mine/otherMine?userId=' + userId
        });
      }
    },
    /**
     * 格式化主内容，处理#标签高亮和@用户高亮
     * @param {String} content 主内容
     */
    formatContentWithHashtags(content) {
      if (!content) return content;

      // 如果内容已经是HTML格式（包含<span>标签），说明已经格式化过，直接返回
      // 避免重复格式化导致HTML被破坏
      if (typeof content === 'string' && (content.includes('<span') || content.includes('<a '))) {
        return content;
      }

      let processedContent = content;

      // 处理@用户高亮（先处理@用户，避免与#标签冲突）
      // 匹配格式：@用户名（用户名不包含空格、@、#），但不在HTML标签内
      const mentionRegex = /@([^\s@#<>]+)/g;
      processedContent = processedContent.replace(
        mentionRegex,
        (match, userName, offset, string) => {
          // 检查是否在HTML标签内部
          const beforeText = string.substring(Math.max(0, offset - 50), offset);
          // 如果前后有未闭合的<a标签，说明已经在标签内，跳过
          const beforeOpenTags = (beforeText.match(/<a[^>]*>/g) || []).length;
          const beforeCloseTags = (beforeText.match(/<\/a>/g) || []).length;
          if (beforeOpenTags > beforeCloseTags) {
            return match;
          }
          // 只显示蓝色样式，不添加链接（只显示不跳转）
          return '<span style="color: #1890ff;">' + match + '</span>';
        }
      );

      // 处理#标签高亮（如果还没有被格式化）
      // 注意：需要避免匹配HTML标签内的#（如style="color: #1890ff;"）
      // 使用更精确的方法：先找到所有可能的#标签位置，然后检查是否在HTML标签属性内
      let lastIndex = 0;
      let result = '';
      const hashtagRegex = /#([^\s#<>]+)/g;
      let match;

      while ((match = hashtagRegex.exec(processedContent)) !== null) {
        const matchStart = match.index;
        const matchEnd = matchStart + match[0].length;

        // 添加匹配前的文本
        const beforeText = processedContent.substring(lastIndex, matchStart);
        result += beforeText;

        // 检查是否在HTML标签属性内
        const beforeMatch = processedContent.substring(0, matchStart);
        const lastTagStart = beforeMatch.lastIndexOf('<');
        const lastTagEnd = beforeMatch.lastIndexOf('>');

        let shouldSkip = false;

        // 如果在未闭合的标签内
        if (lastTagStart > lastTagEnd) {
          const tagContent = processedContent.substring(lastTagStart, matchEnd);
          // 检查是否在引号内（属性值中）
          const beforeInTag = tagContent.substring(0, matchStart - lastTagStart);
          const singleQuotes = (beforeInTag.match(/'/g) || []).length;
          const doubleQuotes = (beforeInTag.match(/"/g) || []).length;
          // 如果引号数量是奇数，说明在属性值内
          if (singleQuotes % 2 === 1 || doubleQuotes % 2 === 1) {
            shouldSkip = true;
          }
        }

        // 检查是否是十六进制颜色代码
        if (!shouldSkip && /^[0-9a-fA-F]{6}$/i.test(match[1])) {
          shouldSkip = true;
        }

        if (shouldSkip) {
          result += match[0]; // 保持原样
        } else {
          // 检查#标签前面是否有空格，如果没有就添加一个空格
          const lastChar =
            beforeText.length > 0
              ? beforeText[beforeText.length - 1]
              : lastIndex > 0
              ? processedContent[lastIndex - 1]
              : ' ';
          const needSpace =
            lastChar !== ' ' && lastChar !== '\n' && lastChar !== '\t' && lastIndex !== 0;
          const space = needSpace ? ' ' : '';
          // 只显示蓝色样式，不添加链接（只显示不跳转）
          result += `${space}<span style="color: #1890ff;">${match[0]}</span>`;
        }

        lastIndex = matchEnd;
      }

      // 添加剩余的文本
      result += processedContent.substring(lastIndex);
      processedContent = result;

      return processedContent;
    },
    /**
     * 格式化评论内容，将图片URL转换为图片标签，并处理#标签高亮和@用户高亮
     * @param {String} content 评论内容
     */
    formatCommentContent(content) {
      if (!content) return content;

      // 检测是否为图片URL
      const imageUrlPattern = /^https?:\/\/.*\.(jpg|jpeg|png|gif|webp)(\?.*)?$/i;
      if (imageUrlPattern.test(content.trim())) {
        // 如果是图片URL，转换为img标签，设置固定大小
        return `<div style="width: 120px; height: 120px; overflow: hidden; border-radius: 6px; margin: 5px 0;"><img src="${content}" style="width: 100%; height: 100%; object-fit: cover;" /></div>`;
      }

      // 如果内容已经是HTML格式（包含<span>标签），说明已经格式化过，直接返回
      // 避免重复格式化导致HTML被破坏
      if (typeof content === 'string' && (content.includes('<span') || content.includes('<a '))) {
        return content;
      }

      let processedContent = content;

      // 处理@用户高亮（先处理@用户，避免与#标签冲突）
      // 匹配格式：@用户名（用户名不包含空格、@、#），但不在HTML标签内
      const mentionRegex = /@([^\s@#<>]+)/g;
      processedContent = processedContent.replace(
        mentionRegex,
        (match, userName, offset, string) => {
          // 检查是否在HTML标签内部
          const beforeText = string.substring(Math.max(0, offset - 50), offset);
          // 如果前后有未闭合的<a标签，说明已经在标签内，跳过
          const beforeOpenTags = (beforeText.match(/<a[^>]*>/g) || []).length;
          const beforeCloseTags = (beforeText.match(/<\/a>/g) || []).length;
          if (beforeOpenTags > beforeCloseTags) {
            return match;
          }
          // 只显示蓝色样式，不添加链接（只显示不跳转）
          return '<span style="color: #1890ff;">' + match + '</span>';
        }
      );

      // 处理#标签高亮（如果还没有被格式化）
      // 注意：需要避免匹配HTML标签内的#（如style="color: #1890ff;"）
      // 使用更精确的方法：先找到所有可能的#标签位置，然后检查是否在HTML标签属性内
      let lastIndex = 0;
      let result = '';
      const hashtagRegex = /#([^\s#<>]+)/g;
      let match;

      while ((match = hashtagRegex.exec(processedContent)) !== null) {
        const matchStart = match.index;
        const matchEnd = matchStart + match[0].length;

        // 添加匹配前的文本
        const beforeText = processedContent.substring(lastIndex, matchStart);
        result += beforeText;

        // 检查是否在HTML标签属性内
        const beforeMatch = processedContent.substring(0, matchStart);
        const lastTagStart = beforeMatch.lastIndexOf('<');
        const lastTagEnd = beforeMatch.lastIndexOf('>');

        let shouldSkip = false;

        // 如果在未闭合的标签内
        if (lastTagStart > lastTagEnd) {
          const tagContent = processedContent.substring(lastTagStart, matchEnd);
          // 检查是否在引号内（属性值中）
          const beforeInTag = tagContent.substring(0, matchStart - lastTagStart);
          const singleQuotes = (beforeInTag.match(/'/g) || []).length;
          const doubleQuotes = (beforeInTag.match(/"/g) || []).length;
          // 如果引号数量是奇数，说明在属性值内
          if (singleQuotes % 2 === 1 || doubleQuotes % 2 === 1) {
            shouldSkip = true;
          }
        }

        // 检查是否是十六进制颜色代码
        if (!shouldSkip && /^[0-9a-fA-F]{6}$/i.test(match[1])) {
          shouldSkip = true;
        }

        if (shouldSkip) {
          result += match[0]; // 保持原样
        } else {
          // 检查#标签前面是否有空格，如果没有就添加一个空格
          const lastChar =
            beforeText.length > 0
              ? beforeText[beforeText.length - 1]
              : lastIndex > 0
              ? processedContent[lastIndex - 1]
              : ' ';
          const needSpace =
            lastChar !== ' ' && lastChar !== '\n' && lastChar !== '\t' && lastIndex !== 0;
          const space = needSpace ? ' ' : '';
          // 只显示蓝色样式，不添加链接（只显示不跳转）
          result += `${space}<span style="color: #1890ff;">${match[0]}</span>`;
        }

        lastIndex = matchEnd;
      }

      // 添加剩余的文本
      result += processedContent.substring(lastIndex);
      processedContent = result;

      return processedContent;
    },
    /**
     * 获取一级评论列表
     * @param {Object} notesId 笔记id
     */
    getFirstComment(notesId) {
      if (this.status == 'loading' || this.status == 'nomore') {
        return;
      }
      this.status = 'loading';
      getCommentFirstListByNotesId({
        notesId: notesId,
        page: this.page,
        pageSize: this.pageSize
      })
        .then(res => {
          const commentArr = Array.isArray(res.data.records) ? res.data.records : [];
          const mappedArr = commentArr.map(item => {
            // 处理二级评论数据
            let childrenList = [];
            let childrenPage = 1;
            let childrenStatus = 'nomore';

            if (item.children && Array.isArray(item.children) && item.children.length > 0) {
              childrenList = item.children.map(child => ({
                id: child.id,
                parentId: child.pid,
                notesId: child.nid,
                commentUserId: child.uid,
                commentUserName: child.username,
                commentUserAvatar: child.avatar,
                content: this.formatCommentContent(child.content),
                isLike: child.isLike,
                commentLikeNum: child.likeCount,
                commentReplyNum: child.twoCommentCount,
                createTime: weChatTimeFormat(Number(child.createTime))
              }));
              // 如果API返回了二级评论，说明已经展开了，page应该设置为2
              childrenPage = 2;
              // 如果返回的二级评论数量等于pageSize，说明可能还有更多
              childrenStatus = childrenList.length >= 10 ? 'loadmore' : 'nomore';
            }

            return {
              id: item.id,
              parentId: item.pid,
              notesId: item.nid,
              commentUserId: item.uid,
              commentUserName: item.username,
              commentUserAvatar: item.avatar,
              content: this.formatCommentContent(item.content),
              isLike: item.isLike,
              commentLikeNum: item.likeCount,
              commentReplyNum: item.twoCommentCount,
              createTime: weChatTimeFormat(Number(item.createTime)),
              children: {
                list: childrenList,
                page: childrenPage,
                pageSize: 10,
                status: childrenStatus,
                loadmoreText: '—— 展开' + (item.twoCommentCount || 0) + '条回复 ——'
              }
            };
          });
          setTimeout(() => {
            this.page++;
            if (mappedArr.length < this.pageSize) {
              this.status = 'nomore';
            } else {
              this.status = 'loadmore';
            }
            this.commentList = this.commentList.concat(mappedArr);
          }, 700);
        })
        .catch(err => {
          console.log(err);
        });
    },
    /**
     * 评论输入框添加@用户
     * @param {Object} item
     */
    addUser(item) {
      this.showAite = false;
      this.$refs.lsjComment.keyboardShow();
      this.edit.addLink({
        prefix: '@',
        name: item.nickname,
        data: {
          userId: item.userId
        }
      });
    },
    /**
     * 评论输入框添加emoji
     * @param {Object} name
     */
    addEmoji(em) {
      // 优先使用编辑器的emoji插入；不支持则降级为插入文本
      if (this.edit && typeof this.edit.emoji === 'function') {
        this.edit.emoji(em);
      } else if (this.edit && typeof this.edit.insertText === 'function') {
        this.edit.insertText(em);
      } else if (this.edit && typeof this.edit.insertCustomEmoji === 'function') {
        // 兜底：不再依赖图片资源，直接插入文本表情
        this.edit.insertText(em);
      }
      this.showEmoji = false;
      this.$refs.lsjComment.keyboardShow();
    },
    /**
     * 点击 @ 图标：直接在输入框插入一个 @ 符号
     */
    chooseAite() {
      // 关闭可能的弹层
      this.showAite = false;
      this.showEmoji = false;

      // 优先使用编辑器提供的插入文本能力
      if (this.edit && typeof this.edit.insertText === 'function') {
        this.edit.insertText('@');
      } else if (this.edit && typeof this.edit.addLink === 'function') {
        // 兜底：用 addLink 插入一个 @
        this.edit.addLink({
          prefix: '',
          name: '@',
          data: {}
        });
      } else {
        // 最后兜底直接拼接到 content 文本
        this.content = (this.content || '') + '@';
      }

      // 保持键盘弹出，方便继续输入
      if (this.$refs.lsjComment && typeof this.$refs.lsjComment.keyboardShow === 'function') {
        this.$refs.lsjComment.keyboardShow();
      }
    },
    /**
     * 获取关注用户列表
     */
    getAttentionUser() {
      if (this.attentionUser.isNoMore) {
        return;
      }
      this.attentionUser.isLoading = true;
      setTimeout(() => {
        getAttentionList({
          userId: this.userInfo.id,
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
    /**
     * 打开emoji弹出层
     */
    openEmoji() {
      if (this.showEmoji) {
        this.showEmoji = false;
        this.$refs.lsjComment.keyboardShow();
      } else {
        this.showAite = false;
        this.showEmoji = true;
        uni.hideKeyboard();
      }
    },
    /**
     * 选择发布评论的图片
     */
    chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        sourceType: ['album', 'camera'],
        success: res => {
          console.log(res);
          this.commentImagesurl = res.tempFilePaths[0];
        },
        complete: () => {
          this.$refs.lsjComment.keyboardShow();
        }
      });
    },
    /**
     * 删除发布评论的图片
     */
    deleteImage() {
      this.commentImagesurl = '';
      this.$refs.lsjComment.keyboardShow();
    },
    /**
     * 编辑器初始化
     * @param {Object} edit
     */
    editReady(edit) {
      this.edit = edit;
      const reg = /<img/g;
      if (!(this.revealcontent == '\n' || this.revealcontent == '') || reg.test(this.content)) {
        this.edit.ready(this.content);
      }
    },
    /**
     * swiper切换
     * @param {Object} e
     */
    swiperChange(e) {
      this.swiperCurrent = e.detail.current;
    },
    /**
     * 提取话题名
     * @param {Object} str
     */
    findTopicName(str) {
      // #{&quot;topicname&quot;:&quot;蛋仔派对&quot;}
      let reg = /#{&quot;topicname&quot;:&quot;(.+?)&quot;}/g;
      let result = str.match(reg);
      if (result) {
        return result[0].replace(/#{&quot;topicname&quot;:&quot;/g, '').replace(/&quot;}/g, '');
      } else {
        return '';
      }
    },
    /**
     * 提取用户id
     * @param {Object} str
     */
    findUserId(str) {
      // 兼容多种格式：
      // 1. href='#{"userId":"xxx"}' (单引号包裹，JSON内部双引号)
      // 2. href="#{\"userId\":\"xxx\"}" (双引号包裹，JSON内部转义双引号)
      // 3. href="#{&quot;userId&quot;:&quot;xxx&quot;}" (旧格式，HTML实体)
      let reg = /href=['"]#\{["']userId["']:\s*["'](.+?)["']\}/g;
      let result = str.match(reg);
      if (result) {
        // 提取userId值
        let userIdMatch = result[0].match(/["']userId["']:\s*["'](.+?)["']/);
        if (userIdMatch && userIdMatch[1]) {
          return userIdMatch[1];
        }
      }
      // 兼容旧格式
      reg = /#{&quot;userId&quot;:&quot;(.+?)&quot;}/g;
      result = str.match(reg);
      if (result) {
        return result[0].replace(/#{&quot;userId&quot;:&quot;/g, '').replace(/&quot;}/g, '');
      }
      return '';
    },
    /**
     * 跳转到商品详情页
     * @param {String} productId 商品ID
     */
    goToProduct(productId) {
      if (!productId) {
        uni.showToast({
          title: '商品ID不存在',
          icon: 'none'
        });
        return;
      }
      // 跳转到普通商品详情页（关联商品默认是普通商品）
      uni.navigateTo({
        url: `/pkg-detail/pages/idleDetail/idleDetail?productId=${productId}`
      });
    },
    /**
     * 格式化商品价格
     * @param {Number|String} price 价格（后端返回的已经是元）
     * @returns {String} 格式化后的价格（元）
     */
    formatProductPrice(price) {
      if (!price && price !== 0) return '0.00';
      // 转换为数字
      const priceNum = typeof price === 'string' ? parseFloat(price) : Number(price);
      if (isNaN(priceNum)) return '0.00';
      // 后端返回的价格已经是元（字符串格式），直接格式化显示
      // 保留两位小数
      return priceNum.toFixed(2);
    },
    /**
     * 点击话题或者用户
     * @param {Object} e
     */
    clickTopic(e) {
      if (e.detail.node.name != 'a') {
        return;
      }
      let href = e.detail.node.attrs.href || '';
      // 检查是否是话题链接
      if (href.includes('/pkg-search/pages/topicIndex/topicIndex')) {
        const topicNameMatch = href.match(/topicName=([^&]+)/);
        if (topicNameMatch) {
          const topicName = decodeURIComponent(topicNameMatch[1]);
          uni.navigateTo({
            url:
              '/pkg-search/pages/topicIndex/topicIndex?topicName=' + encodeURIComponent(topicName)
          });
          return;
        }
      }
      // 检查是否是用户链接（新格式，使用userName）
      if (href.includes('/pkg-mine/pages/mine/otherMine')) {
        const userNameMatch = href.match(/userName=([^&]+)/);
        if (userNameMatch) {
          const userName = decodeURIComponent(userNameMatch[1]);
          uni.navigateTo({
            url: '/pkg-mine/pages/mine/otherMine?userName=' + encodeURIComponent(userName)
          });
          return;
        }
        const userIdMatch = href.match(/userId=([^&]+)/);
        if (userIdMatch) {
          const userId = decodeURIComponent(userIdMatch[1]);
          uni.navigateTo({
            url: '/pkg-mine/pages/mine/otherMine?userId=' + userId
          });
          return;
        }
      }
      // 兼容旧格式
      let topicName = this.findTopicName(href);
      let userId = this.findUserId(href);
      if (topicName != '') {
        uni.navigateTo({
          url: '/pkg-search/pages/topicIndex/topicIndex?topicName=' + topicName
        });
      } else if (userId != '') {
        uni.navigateTo({
          url: '/pkg-mine/pages/mine/otherMine?userId=' + userId
        });
      }
    },
    /**
     * 返回上一页
     */
    goBack() {
      uni.navigateBack();
    }
  },
  onLoad(options) {
    console.log(options);
    if (!options || !options.notesId) {
      uni.showToast({
        title: '笔记ID缺失，无法打开详情',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 600);
      return;
    }
    // 设置初始高度，防止真机图片显示不全
    this.swipperHeight = 750;
    uni.getSystemInfo({
      success: res => {
        this.applyWeixinNavBar(res, 44);
        this.windowHeight = res.windowHeight;
      }
    });
    // 统一为 Unicode 表情列表
    this.emojiList = emojiList;
    this.bottomEmoji = emojiList.slice(0, 9);

    // let sql1 = `select * from emoji_list order by updateTime desc limit 9`
    // this.$sqliteUtil.SqlSelect(sql1).then(res => {
    // 	if (res.length == 0) {
    // 		this.bottomEmoji = emojiList
    // 		console.log(this.bottomEmoji)
    // 	} else {
    // 		this.bottomEmoji = res
    // 	}
    // })
    uni.onKeyboardHeightChange(res => {
      if (res.height > 0 && this.keyboardHeight == 0) {
        this.keyboardHeight = res.height;
      }
    });
    this.userInfo = uni.getStorageSync('userInfo');

    // 添加浏览记录
    if (this.userInfo && this.userInfo.id) {
      addBrowseRecord({
        nid: options.notesId,
        uid: this.userInfo.id
      }).catch(err => {
        console.log('添加浏览记录失败:', err);
      });
    }
    const scene = options.scene || 'other';
    this.shareScene = scene;
    reportNoteBehavior({ eventType: 'view', nid: options.notesId, scene }).catch(() => {});

    getNotesByNotesId({
      notesId: options.notesId
    }).then(res => {
      // 兼容两种返回结构：
      // 1) $request resolve 的结构：{ code, data, msg }
      // 2) 其它封装可能返回：{ data: { code, data, msg } }
      const data =
        res && res.data && Object.prototype.hasOwnProperty.call(res.data, 'data') ? res.data.data : res.data;
      if (!data) {
        uni.showToast({
          title: '笔记不存在或已删除',
          icon: 'none'
        });
        setTimeout(() => {
          uni.navigateBack();
        }, 600);
        return;
      }

      const imgList = (() => {
        const urls = data.urls;
        if (!urls) return [];
        if (Array.isArray(urls)) return urls;
        if (typeof urls === 'string') {
          try {
            const parsed = JSON.parse(urls);
            return Array.isArray(parsed) ? parsed : [];
          } catch (e) {
            // 兜底：如果后端返回的是逗号分隔字符串，也尽量解析成数组
            return urls
              .split(',')
              .map(s => (s || '').trim())
              .filter(Boolean);
          }
        }
        return [];
      })();
      // urls 为空但仅有封面时（如管理端/OSS 只写了 noteCover），仍展示轮播，与域名无关
      const effectiveImgList =
        imgList.length > 0
          ? imgList
          : data.noteCover && String(data.noteCover).trim()
            ? [String(data.noteCover).trim()]
            : [];
      // 处理图片资源
      let notesResources = [];
      if (effectiveImgList.length > 0) {
        notesResources = effectiveImgList.map(url => ({
          url,
          width: 750, // 可以后续用 getImageInfo 动态赋值
          height: 750
        }));
      }
      // 处理时间
      const createTime = data.time ? weChatTimeFormat(data.time) : '';
      const updateTime = data.time ? weChatTimeFormat(data.time) : '';
      // 统一映射
      this.notesDetail = {
        id: data.id,
        title: data.title,
        content: this.formatContentWithHashtags(data.content),
        coverPicture: data.noteCover,
        noteType: Number(data.noteType),
        belongUserId: Number(data.uid),
        nickname: data.username,
        avatarUrl: data.avatar,
        imgList: effectiveImgList,
        notesResources, // 你可以用 notesResources 替换原本的 notesResources 字段
        notesLikeNum: Number(data.likeCount),
        viewCount: Number(data.viewCount),
        notesCollectNum: Number(data.collectionCount),
        commentCount: Number(data.commentCount),
        isFollow: data.isFollow,
        isLike: data.isLike,
        isCollect: data.isCollection,
        createTime,
        updateTime,
        province: data.province || '',
        city: data.city || '',
        district: data.district || '',
        address: data.address || '', // 只保留详细地址
        relatedProducts: data.relatedProducts || [] // 关联商品列表
        // 其它字段按需添加
      };
      this.detailOpenTime = Date.now();
      this.commentCount = this.notesDetail.commentCount;
      this.swipperCount = notesResources.length;
      notesResources.forEach(item => {
        uni.getImageInfo({
          src: item.url,
          success: image => {
            if (image.width >= image.height) {
              item.width = 750;
              item.height = (750 * image.height) / image.width;
            } else {
              if (image.height > 1000) {
                item.height = 1000;
                item.width = (1000 * image.width) / image.height;
                if (item.width > 750) {
                  item.width = 750;
                  item.height = (750 * image.height) / image.width;
                }
              } else {
                if (image.width > 750) {
                  item.width = 750;
                  item.height = (750 * image.height) / image.width;
                } else {
                  item.width = image.width;
                  item.height = image.height;
                }
              }
            }
            if (item.height > this.swipperHeight) {
              this.swipperHeight = item.height;
            }
          },
          fail: err => {
            console.log(err);
          }
        });
      });
      // res.data.createTime = weChatTimeFormat(res.data.createTime)
      // res.data.updateTime = weChatTimeFormat(res.data.updateTime)
      // setTimeout(() => {
      // 	this.notesDetail = res.data
      // 	this.swipperCount = res.data.notesResources.length
      // }, 500)
    });
    this.getFirstComment(options.notesId);
  },
  onReachBottom() {
    this.getFirstComment(this.notesDetail.id);
  },
  onBackPress() {
    if (this.inputField) {
      this.inputField = false;
      return true;
    }
    if (this.showEmoji) {
      this.showEmoji = false;
      return true;
    }
    if (this.showAite) {
      this.showAite = false;
      return true;
    }
    return false;
  }
};
</script>

<style scoped>
.authorName {
  color: #2b2b2b;
  font-size: 15px;
  margin-left: 15rpx;
  width: 350rpx;
  word-break: break-all;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bottom-icon {
  display: flex;
  justify-content: space-around;
  font-size: 35rpx;
  color: #2b2b2b;
  justify-content: space-around;
  padding-right: 10rpx;
}

.bottom-edit {
  padding: 0 15rpx;
  background-color: #f3f3f2;
  border-radius: 50px;
  margin: 5rpx 18rpx;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.share-item {
  display: inline-flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20rpx;
  margin: 0;
  background-color: transparent;
  border: none;
  box-sizing: border-box;
  /* 微信小程序：缩小图标与文字间距 */
  gap: 8rpx;
}

/* 微信小程序：让 button 与右侧 view 视觉完全一致，消除突兀感 */
button.share-item {
  border-radius: 0;
  background-color: transparent;
  font-size: 30rpx;
  font-weight: normal;
  line-height: normal;
  height: auto;
  min-height: 0;
  padding: 20rpx;
  text-align: center;
}
button.share-item::after {
  border: none;
}

::v-deep .uni-swiper-dots {
  bottom: 30rpx;
}

::v-deep .lsjComment .lsj-edit-edit-container {
  background-color: #f3f3f2 !important;
  min-height: 40px !important;
  max-height: 140px !important;
  height: auto !important;
  font-size: 30rpx;
  padding: 30rpx;
  border-radius: 35rpx;
}

::v-deep .ql-editor.ql-blank:before {
  font-size: 30rpx;
}

::v-deep .editot-pd {
  padding: 15rpx 20rpx !important;
}

/* 根据实际需要调整样式 */
.animate-praise {
  animation: scaleAnimation 0.8s ease;
  /* 使用动画 */
}

@keyframes scaleAnimation {
  0% {
    transform: scale(1, 1);
  }

  50% {
    transform: scale(1.2, 1.2);
  }

  100% {
    transform: scale(1, 1);
  }
}

/* 笔记内容样式优化 */
.note-content {
  font-size: 32rpx;
  color: #333;
  line-height: 1.8;
  letter-spacing: 0.5rpx;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
  margin-top: 10rpx;
}

.note-content ::v-deep rich-text {
  line-height: 1.8 !important;
}
</style>