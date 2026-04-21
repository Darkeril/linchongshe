<template>
  <view>
    <!-- 顶部状态栏占位：微信小程序端使用系统导航栏，不需要额外占位，避免出现大块空白 -->
    <!-- #ifndef MP-WEIXIN -->
    <view
      :style="{ height: statusBarHeight + 'px' }"
      style="position: fixed; top: 0; width: 100%; background-color: #fff; z-index: 9999"
    ></view>
    <view :style="{ height: statusBarHeight + 'px' }"></view>
    <!-- #endif -->
    <view class="header-section">
      <view
        style="padding: 10rpx 30rpx 10rpx 15rpx; display: flex; height: 44px; align-items: center"
      >
        <!-- #ifndef MP-WEIXIN -->
        <u-icon @click="goToBack" name="arrow-left" size="25"></u-icon>
        <!-- #endif -->
        <view
          @click="goToSearchValue"
          style="
            background-color: #f2f2f2;
            padding: 10rpx 20rpx;
            color: #606266;
            display: flex;
            border-radius: 50rpx;
            flex: 1;
            margin-left: 20rpx;
            height: 64rpx;
            align-items: center;
            text-align: center;
            box-sizing: border-box;
          "
        >
          <u-icon name="search" size="22" color="#909399"></u-icon>
          <view style="font-size: 30rpx; margin-left: 15rpx">{{ searchValue }}</view>
        </view>
      </view>
      <view
        style="
          display: flex;
          height: 35px;
          width: 100%;
          align-items: flex-start;
          border-bottom-style: solid;
          border-bottom-color: #f3f3f2;
          border-bottom-width: 1rpx;
        "
      >
        <view
          style="display: flex; align-items: center; width: 33%; justify-content: center"
          @click="clickAll"
          :style="{
            color: current === 0 ? '#16160e' : '#9fa0a0',
            fontWeight: current === 0 ? '600' : 'normal'
          }"
        >
          <view>{{ radiovalue1 }}</view>
          <u-icon
            v-if="!show"
            name="arrow-down"
            size="18"
            :color="current === 0 ? '#16160e' : '#9fa0a0'"
          ></u-icon>
          <u-icon
            v-else
            name="arrow-up"
            size="18"
            :color="current === 0 ? '#16160e' : '#9fa0a0'"
          ></u-icon>
        </view>
        <view
          style="
            display: flex;
            align-items: center;
            width: 33%;
            justify-content: center;
            font-weight: ;
          "
          @click="clickUser"
          :style="{
            color: current === 1 ? '#16160e' : '#9fa0a0',
            fontWeight: current === 1 ? '600' : 'normal'
          }"
        >
          <view>用户</view>
        </view>
        <u-transition :show="current == 0" mode="slide-right">
          <view style="display: flex; align-items: center; width: 250rpx; justify-content: center">
            <view style="color: #9fa0a0; font-size: 30rpx">|</view>
            <view
              style="margin-left: 20rpx"
              :style="{
                color: screening ? '#16160e' : '#9fa0a0',
                fontWeight: screening ? '600' : 'normal'
              }"
              @click="openScreening"
            >
              筛选
            </view>
            <u-icon
              name="list"
              size="18"
              :color="screening ? '#16160e' : '#9fa0a0'"
              @click="openScreening"
            ></u-icon>
          </view>
        </u-transition>
      </view>
    </view>
    <view v-if="current == 0" class="component">
      <view style="width: 100%; display: flex; flex-wrap: wrap; background: #f5f5f5">
        <view class="water-left">
          <block v-for="(item, index) in notesList.leftList || []" :key="index">
            <view style="position: relative">
              <view class="note-card">
                <u--image
                  @load="onSearchCoverLoad(item)"
                  @click="goToDetail(item.id, item.notesType)"
                  :src="item.coverPicture"
                  width="100%"
                  height="auto"
                  mode="widthFix"
                  style="max-height: 500rpx; overflow: hidden"
                >
                  <template v-slot:loading>
                    <view style="height: 200rpx; text-align: center; padding: 20rpx">
                      <u-loading-icon color="#e83929"></u-loading-icon>
                      <view style="font-size: 30rpx">loading......</view>
                    </view>
                  </template>
                </u--image>
                <view class="look-views" v-if="item.views != null && item.location">
                  <u-icon name="map" color="#ffffff" size="25rpx"></u-icon>
                  <view style="margin-left: 5rpx">{{ item.location }}</view>
                </view>
                <view v-if="item.notesType == 2" class="video-play">
                  <u-icon name="play-right-fill" color="#ffffff" size="25rpx"></u-icon>
                </view>
                <view class="title" @click="goToDetail(item.id, item.notesType)">
                  {{ item.title }}
                </view>
                <view style="display: flex; position: relative; padding: 20rpx">
                  <image
                    style="height: 22px; width: 22px; border-radius: 50%"
                    mode="aspectFill"
                    :src="item.avatarUrl"
                  ></image>
                  <view class="note-username">
                    {{ item.nickname }}
                  </view>
                  <view style="display: flex; position: absolute; right: 10rpx">
                    <u-transition :show="!item.isLike" mode="fade" duration="2000">
                      <u-icon
                        v-if="!item.isLike"
                        name="/static/praise.png"
                        size="18"
                        @click="praiseNotes(item.id, item.belongUserId, 1, index)"
                      ></u-icon>
                    </u-transition>
                    <u-transition :show="item.isLike" mode="fade" duration="2000">
                      <u-icon
                        v-if="item.isLike"
                        name="/static/praise_select.png"
                        size="18"
                        @click="praiseNotes(item.id, item.belongUserId, 1, index)"
                      ></u-icon>
                    </u-transition>
                    <view
                      v-if="item.notesLikeNum > 0"
                      style="color: gray; font-size: 15px; line-height: 18px; margin-left: 3rpx"
                    >
                      {{ item.notesLikeNum }}
                    </view>
                    <view
                      v-else
                      style="color: gray; font-size: 12px; line-height: 18px; margin-left: 3rpx"
                    >
                      {{ '赞' }}
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </block>
        </view>
        <view class="water-right">
          <block v-for="(item, index) in notesList.rightList || []" :key="index">
            <view style="position: relative">
              <view class="note-card">
                <u--image
                  @load="onSearchCoverLoad(item)"
                  @click="goToDetail(item.id, item.notesType)"
                  :src="item.coverPicture"
                  width="100%"
                  height="auto"
                  mode="widthFix"
                  style="max-height: 500rpx; overflow: hidden"
                >
                  <template v-slot:loading>
                    <view
                      style="
                        height: 200rpx;
                        text-align: center;
                        padding: 20rpx;
                        margin-bottom: 30rpx;
                      "
                    >
                      <u-loading-icon color="#e83929"></u-loading-icon>
                      <view style="font-size: 30rpx">loading......</view>
                    </view>
                  </template>
                </u--image>
                <view class="look-views" v-if="item.views != null && item.location">
                  <u-icon name="map" color="#ffffff" size="25rpx"></u-icon>
                  <view style="margin-left: 5rpx">{{ item.location }}</view>
                </view>
                <view v-if="item.notesType == 2" class="video-play">
                  <u-icon name="play-right-fill" color="#ffffff" size="25rpx"></u-icon>
                </view>
                <view class="title" @click="goToDetail(item.id, item.notesType)">
                  {{ item.title }}
                </view>
                <view style="display: flex; position: relative; padding: 20rpx">
                  <image
                    style="height: 20px; width: 20px; border-radius: 50%"
                    mode="aspectFill"
                    :src="item.avatarUrl"
                  ></image>
                  <view class="note-username">
                    {{ item.nickname }}
                  </view>
                  <view style="display: flex; position: absolute; right: 10rpx">
                    <u-transition :show="!item.isLike" mode="fade" duration="2000">
                      <u-icon
                        v-if="!item.isLike"
                        name="/static/praise.png"
                        size="18"
                        @click="praiseNotes(item.id, item.belongUserId, 2, index)"
                      ></u-icon>
                    </u-transition>
                    <u-transition :show="item.isLike" mode="fade" duration="2000">
                      <u-icon
                        v-if="item.isLike"
                        name="/static/praise_select.png"
                        size="18"
                        @click="praiseNotes(item.id, item.belongUserId, 2, index)"
                      ></u-icon>
                    </u-transition>
                    <view
                      v-if="item.notesLikeNum > 0"
                      style="color: gray; font-size: 15px; line-height: 18px; margin-left: 3rpx"
                    >
                      {{ item.notesLikeNum }}
                    </view>
                    <view
                      v-else
                      style="color: gray; font-size: 12px; line-height: 18px; margin-left: 3rpx"
                    >
                      {{ '赞' }}
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </block>
        </view>
      </view>
      <u-loadmore
        v-if="
          notesList.leftList &&
          notesList.rightList &&
          notesList.leftList.length + notesList.rightList.length > 0
        "
        margin-top="20"
        line
        :status="notesList.status"
        :loading-text="loadingText"
        :loadmore-text="loadmoreText"
        :nomore-text="nomoreText"
      />
    </view>
    <view v-if="current == 1">
      <view style="padding: 0 30rpx">
        <block v-for="(item, index) in userList.list || []" :key="index">
          <view
            style="
              display: flex;
              padding: 20rpx 0;
              align-items: center;
              border-bottom-style: solid;
              border-width: 1rpx;
              border-color: #f3f3f2;
              height: 140rpx;
            "
          >
            <image
              :src="item.avatarUrl"
              style="height: 80rpx; width: 80rpx; border-radius: 50%"
              @click="goToUserMine(item.id)"
              mode="aspectFill"
            ></image>
            <view
              @click="goToUserMine(item.id)"
              style="
                display: flex;
                flex-direction: column;
                justify-content: space-between;
                margin-left: 20rpx;
                flex: 1;
              "
            >
              <view style="font-size: 30rpx; color: #2b2b2b">{{ item.nickname }}</view>
              <view style="height: 10rpx"></view>
              <view style="font-size: 23rpx; color: #afafb0">爱宠社号：{{ item.uid }}</view>
            </view>
            <u-tag
              style="margin-left: auto"
              text="去私信"
              plain
              shape="circle"
              borderColor="#afafb0"
              color="#2b2b2b"
              @click="goToChat(item)"
            ></u-tag>
          </view>
        </block>
        <view style="margin-top: 70rpx">
          <u-loadmore :status="userList.status" loadingIcon="spinner" line></u-loadmore>
        </view>
      </view>
    </view>
    <!-- 排序筛选弹窗 -->
    <view v-if="show" class="dropdown-overlay" @click="show = false">
      <view class="dropdown-menu" @click.stop>
        <view
          v-for="(item, index) in radiolist1"
          :key="index"
          class="dropdown-item"
          :class="{ active: radiovalue1 === item.name }"
          @click="radioChange(item.name)"
        >
          <text class="item-text">{{ item.name }}</text>
          <view class="check-icon" v-if="radiovalue1 === item.name">
            <text class="check-mark">✓</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 类型筛选弹窗 -->
    <view v-if="screening" class="filter-overlay" @click="screening = false">
      <view class="filter-menu" @click.stop>
        <view class="filter-title">笔记类型</view>
        <view class="type-options">
          <view class="type-option" :class="{ active: noteType === 0 }" @click="chooseNoteType(0)">
            <text class="type-text">全部</text>
          </view>
          <view class="type-option" :class="{ active: noteType === 1 }" @click="chooseNoteType(1)">
            <text class="type-text">图文</text>
          </view>
          <view class="type-option" :class="{ active: noteType === 2 }" @click="chooseNoteType(2)">
            <text class="type-text">视频</text>
          </view>
          <view class="type-option" :class="{ active: noteType === 3 }" @click="chooseNoteType(3)">
            <text class="type-text">Live图</text>
          </view>
        </view>
        <view class="filter-actions">
          <view class="reset-btn" @click="resetFilter">重置</view>
          <view class="confirm-btn" @click="screening = false">完成</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { praiseOrCancelNotes } from '@/apis/notes_service.js';
import { reportNoteBehaviorBatch } from '@/apis/note_behavior_service.js';
import { pxToRpx } from '@/utils/util.js';
import { searchNotesByKeyword, searchUserByKeyword } from '@/apis/search_service.js';
export default {
  data() {
    return {
      statusBarHeight: 0,
      searchValue: '',
      title: 'hello',
      show: false,
      current: 0,
      screening: false,
      radiolist1: [
        {
          name: '最新',
          disabled: false
        },
        {
          name: '最热',
          disabled: false
        }
      ],
      radiovalue1: '最新',
      noteType: 0,
      notesList: {
        leftList: [],
        rightList: [],
        leftHeight: 0,
        rightHeight: 0,
        status: 'loadmore',
        page: 1,
        pageSize: 10
      },
      userList: {
        list: [],
        page: 1,
        pageSize: 10,
        status: 'loadmore'
      },
      loadingText: '加载中...',
      loadmoreText: '加载更多',
      nomoreText: '到底了',
      exposureReportedMap: {},
      exposurePendingQueue: [],
      exposureTimer: null
    };
  },
  methods: {
    onSearchCoverLoad(item) {
      const noteId = item && (item.id || item.noteId || item.nid);
      if (!noteId) return;
      const key = String(noteId);
      if (this.exposureReportedMap[key]) return;
      this.$set(this.exposureReportedMap, key, true);
      this.exposurePendingQueue.push({
        eventType: 'exposure',
        nid: key,
        scene: 'search'
      });
      if (this.exposureTimer) {
        clearTimeout(this.exposureTimer);
      }
      this.exposureTimer = setTimeout(() => {
        this.exposureTimer = null;
        if (!this.exposurePendingQueue.length) return;
        const payload = this.exposurePendingQueue.slice();
        this.exposurePendingQueue = [];
        reportNoteBehaviorBatch(payload).catch(() => {});
      }, 400);
    },
    moveHandle(e) {},
    goToChat(item) {
      uni.navigateTo({
        url: `/pkg-msg/pages/chat/chat?userId=${item.id}&userName=${item.nickname}&avatarUrl=${item.avatarUrl}`
      });
    },
    goToUserMine(id) {
      uni.navigateTo({
        url: `/pkg-mine/pages/mine/otherMine?userId=${id}`
      });
    },
    getImageHeight(s) {
      return new Promise((resolve, reject) => {
        uni.getImageInfo({
          src: s,
          success: res => {
            let imageHeight = pxToRpx(res.height);
            let imageWidth = pxToRpx(res.width);
            const width = 360;
            const maxHeight = 500;
            let height = (width * imageHeight) / imageWidth;
            if (height > maxHeight) {
              height = maxHeight;
            }
            let obj = {
              height: height,
              path: res.path
            };
            resolve(obj);
          },
          fail: err => {
            console.error('获取图片信息失败:', err);
            // 提供一个默认高度，防止渲染错误
            resolve({ height: pxToRpx(200), path: s });
          }
        });
      });
    },
    praiseNotes(id, targetUserId, type, index) {
      console.log(type);
      praiseOrCancelNotes({
        notesId: id,
        userId: uni.getStorageSync('userInfo').id,
        targetUserId: targetUserId
      }).then(res => {
        console.log(res);
        if (res.code == 200) {
          if (type === 1) {
            // 确保 leftList 存在且索引有效
            if (this.notesList && this.notesList.leftList && this.notesList.leftList[index]) {
              console.log(this.notesList.leftList[index]);
              if (this.notesList.leftList[index].isLike) {
                this.notesList.leftList[index].notesLikeNum =
                  this.notesList.leftList[index].notesLikeNum - 1;
                this.notesList.leftList[index].isLike = false;
              } else {
                this.notesList.leftList[index].notesLikeNum =
                  this.notesList.leftList[index].notesLikeNum + 1;
                this.notesList.leftList[index].isLike = true;
              }
            }
          } else {
            // 确保 rightList 存在且索引有效
            if (this.notesList && this.notesList.rightList && this.notesList.rightList[index]) {
              if (this.notesList.rightList[index].isLike) {
                this.notesList.rightList[index].notesLikeNum =
                  this.notesList.rightList[index].notesLikeNum - 1;
                this.notesList.rightList[index].isLike = false;
              } else {
                this.notesList.rightList[index].notesLikeNum =
                  this.notesList.rightList[index].notesLikeNum + 1;
                this.notesList.rightList[index].isLike = true;
              }
            }
          }
        }
      });
    },
    getMoreUser() {
      if (this.userList.status == 'nomore' || this.userList.status == 'loading') {
        return;
      }
      this.userList.status = 'loading';
      searchUserByKeyword({
        keyword: this.searchValue,
        page: this.userList.page,
        pageSize: this.userList.pageSize
      }).then(res => {
        if (res.code == 200) {
          const records = (res.data.records || []).map(item => ({
            ...item,
            avatarUrl: item.avatar,
            nickname: item.username,
            uid: item.hsId
          }));
          this.userList.page += 1;
          this.userList.list = this.userList.list.concat(records);
          if (records.length < this.userList.pageSize) {
            this.userList.status = 'nomore';
          } else {
            this.userList.status = 'loadmore';
          }
        } else {
          this.userList.status = 'nomore';
          uni.showToast({
            title: res.msg == '' ? '获取用户失败' : res.msg,
            icon: 'none'
          });
        }
      });
    },
    goToDetail(id, type) {
      if (!id) {
        uni.showToast({
          title: '笔记ID不存在，无法打开详情',
          icon: 'none'
        });
        return;
      }

      const scene = 'search';
      if (Number(type) === 2) {
        // 视频笔记
        uni.navigateTo({
          url: `/pkg-detail/pages/notesDetail/noteVideoD?notesId=${id}&scene=${scene}`
        });
      } else {
        // 默认图文笔记
        uni.navigateTo({
          url: `/pkg-detail/pages/notesDetail/notesDetail?notesId=${id}&scene=${scene}`
        });
      }
    },
    getMoreNotes() {
      if (this.notesList.status == 'nomore' || this.notesList.status == 'loading') {
        return;
      }
      this.notesList.status = 'loading';
      searchNotesByKeyword({
        keyword: this.searchValue,
        page: this.notesList.page,
        pageSize: this.notesList.pageSize,
        notesType: this.noteType,
        hot: this.radiovalue1 === '最新' ? 0 : 1
      }).then(res => {
        if (res.code == 200) {
          console.log(res.data.records);
          this.notesList.page += 1;
          res.data.records.forEach(item => {
            // 统一笔记主键字段：后端可能返回 id / noteId / nid，确保点击跳转时有值
            item.id = item.id || item.noteId || item.nid;
            item.coverPicture = item.noteCover; // 封面图
            item.avatarUrl = item.avatar; // 头像
            item.nickname = item.username; // 昵称
            item.notesLikeNum = parseInt(item.likeCount, 10) || 0; // 点赞数
            item.notesCollectNum = parseInt(item.collectCount, 10) || 0; // 收藏数（如果有）
            item.isLike = item.isLike || false; // 是否点赞
            item.isCollect = item.isCollect || false; // 是否收藏（如果有）
            item.notesType = parseInt(item.noteType, 10) || 0; // 类型
            item.content = item.content || ''; // 内容
            item.title = item.title; // 标题
            item.belongUserId = item.uid; // 用户id
            item.noteCoverHeight = parseInt(item.noteCoverHeight, 10) || 0;
            item.views = parseInt(item.viewCount, 10) || 0;
            item.location = item.location;

            this.getImageHeight(item.coverPicture)
              .then(res => {
                item.coverPicture = res.path;
                // 确保 notesList 和其属性存在
                if (this.notesList && this.notesList.leftList && this.notesList.rightList) {
                  if (this.notesList.leftHeight <= this.notesList.rightHeight) {
                    this.notesList.leftList.push(item);
                    this.notesList.leftHeight += res.height + pxToRpx(50);
                  } else {
                    this.notesList.rightList.push(item);
                    this.notesList.rightHeight += res.height + pxToRpx(50);
                  }
                }
              })
              .catch(err => {
                console.error('获取图片信息失败:', err);
                // 即使图片获取失败，也要确保数据能正常显示
                if (this.notesList && this.notesList.leftList && this.notesList.rightList) {
                  if (this.notesList.leftHeight <= this.notesList.rightHeight) {
                    this.notesList.leftList.push(item);
                    this.notesList.leftHeight += pxToRpx(200); // 使用默认高度
                  } else {
                    this.notesList.rightList.push(item);
                    this.notesList.rightHeight += pxToRpx(200);
                  }
                }
              });
          });
          if (res.data.records.length < this.notesList.pageSize) {
            this.notesList.status = 'nomore';
          } else {
            this.notesList.status = 'loadmore';
          }
        } else {
          this.notesList.status = 'nomore';
          uni.showToast({
            title: res.msg == '' ? '获取笔记失败' : res.msg,
            icon: 'none'
          });
        }
      });
    },
    chooseNoteType(n) {
      this.noteType = n;
      this.screening = false;
      this.notesList = {
        leftList: [],
        rightList: [],
        leftHeight: 0,
        rightHeight: 0,
        status: 'loadmore',
        page: 1,
        pageSize: 10
      };
      this.getMoreNotes();
    },
    radioChange(n) {
      this.radiovalue1 = n;
      this.show = false;
      this.notesList = {
        leftList: [],
        rightList: [],
        leftHeight: 0,
        rightHeight: 0,
        status: 'loadmore',
        page: 1,
        pageSize: 10
      };
      this.getMoreNotes();
    },
    clickAll() {
      // 切换到最新标签时，关闭筛选弹窗
      this.screening = false;

      if (this.current === 0) {
        this.show = !this.show;
      } else {
        this.current = 0;
        this.show = false;
        this.notesList = {
          leftList: [],
          rightList: [],
          leftHeight: 0,
          rightHeight: 0,
          status: 'loadmore',
          page: 1,
          pageSize: 10
        };
        this.getMoreNotes();
      }
    },
    clickUser() {
      // 切换到用户标签时，关闭所有弹窗
      this.show = false;
      this.screening = false;

      if (this.current != 1 && this.userList.list && this.userList.list.length == 0) {
        this.current = 1;
        this.getMoreUser();
      } else if (this.current != 1) {
        this.current = 1;
      } else if (this.current == 1) {
        this.userList = {
          list: [],
          page: 1,
          pageSize: 10,
          status: 'loadmore'
        };
        this.getMoreUser();
      }
    },
    openScreening() {
      if (this.show) {
        this.show = false;
        return;
      }
      if (this.current != 0) {
        return;
      }
      this.screening = !this.screening;
    },
    resetFilter() {
      this.noteType = 0;
      this.screening = false;
      this.notesList = {
        leftList: [],
        rightList: [],
        leftHeight: 0,
        rightHeight: 0,
        status: 'loadmore',
        page: 1,
        pageSize: 10
      };
      this.getMoreNotes();
    },
    goToSearchValue() {
      console.log(this.searchValue);
      uni.navigateTo({
        url: `/pages/searchPage/searchPage?keyword=${this.searchValue}`
      });
    },
    goToBack() {
      uni.navigateBack();
    }
  },
  onLoad(options) {
    this.searchValue = options.keyword;
    uni.getSystemInfo({
      success: res => {
        // 微信小程序端使用系统导航栏，不额外占位
        // #ifdef MP-WEIXIN
        this.statusBarHeight = 0;
        // #endif
        // #ifndef MP-WEIXIN
        this.statusBarHeight = res.statusBarHeight;
        // #endif
      }
    });
    this.getMoreNotes();
  },
  onShow() {
    uni.$once('updateSearch', keyword => {
      console.log(keyword);
      this.searchValue = keyword;
      if (this.current == 0) {
        this.notesList = {
          leftList: [],
          rightList: [],
          leftHeight: 0,
          rightHeight: 0,
          status: 'loadmore',
          page: 1,
          pageSize: 10
        };
        this.getMoreNotes();
      } else if (this.current == 1) {
        this.userList = {
          list: [],
          page: 1,
          pageSize: 10,
          status: 'loadmore'
        };
        this.getMoreUser();
      }
    });
  },
  onReachBottom() {
    if (this.current == 0) {
      this.getMoreNotes();
    } else if (this.current == 1) {
      this.getMoreUser();
    }
  },
  onPullDownRefresh() {
    if (this.current == 0) {
      this.notesList = {
        leftList: [],
        rightList: [],
        leftHeight: 0,
        rightHeight: 0,
        status: 'loadmore',
        page: 1,
        pageSize: 10
      };
      this.getMoreNotes();
    } else if (this.current == 1) {
      this.userList = {
        list: [],
        page: 1,
        pageSize: 10,
        status: 'loadmore'
      };
      this.getMoreUser();
    }
    setTimeout(() => {
      uni.stopPullDownRefresh();
    }, 800);
  },
  onUnload() {
    if (this.exposurePendingQueue.length) {
      const payload = this.exposurePendingQueue.slice();
      this.exposurePendingQueue = [];
      reportNoteBehaviorBatch(payload).catch(() => {});
    }
    if (this.exposureTimer) {
      clearTimeout(this.exposureTimer);
      this.exposureTimer = null;
    }
  }
};
</script>

<style lang="scss">
.water-left,
.water-right {
  width: 48%;
  margin: 20rpx auto;
  margin-top: 5px;
}

.title {
  font-size: 30rpx;
  padding: 10rpx;
  margin-bottom: -5px;
  color: #000000;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-height: 1.4em;
  max-height: 2.4em;
}

.note-username {
  margin-left: 10rpx;
  color: #16160e;
  font-size: 24rpx;
  line-height: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: calc(100% - 70px);
}

.look-views {
  display: flex;
  position: absolute;
  bottom: 135rpx;
  left: 8rpx;
  color: #ffffff;
  background-color: rgba(123, 124, 125, 0.6);
  // filter: brightness(65%);
  padding: 3rpx 10rpx;
  border-radius: 50rpx;
  font-size: 22rpx;
}

.video-play {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  // background-color: rgba(123, 124, 125, 0.6);
  // filter: brightness(65%);
  padding: 10rpx;
  border-radius: 50%;
}

.note-card {
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.08);
  margin-top: 5rpx;
  margin-bottom: 24rpx;
  margin-left: 8rpx;
  margin-right: 8rpx;
  overflow: hidden;
  position: relative;
  border: 1rpx solid #f0f0f0;
}

// 头部区域样式
.header-section {
  // 保持原有样式，无变暗效果
}

// 下拉菜单样式
.dropdown-overlay {
  position: fixed;
  top: 170rpx;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1000;
}

.dropdown-menu {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  z-index: 1001;
}

.dropdown-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &.active {
    .item-text {
      color: #ff4757;
      font-weight: 500;
    }
  }

  &:active {
    background: #f8f9fa;
  }
}

.item-text {
  font-size: 30rpx;
  color: #333;
}

.check-icon {
  width: 24rpx;
  height: 24rpx;
  background: #ff4757;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-mark {
  font-size: 16rpx;
  color: #fff;
  font-weight: bold;
}

// 筛选弹窗样式
.filter-overlay {
  position: fixed;
  top: 170rpx;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.filter-menu {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.15);
  z-index: 1001;
  padding: 30rpx;
}

.filter-title {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 24rpx;
}

// 类型选项样式
.type-options {
  display: flex;
  gap: 16rpx;
  margin-bottom: 40rpx;
}

.type-option {
  flex: 1;
  padding: 20rpx 16rpx;
  background: #f5f5f5;
  border-radius: 20rpx;
  border: 1rpx solid #e0e0e0;
  text-align: center;
  transition: all 0.3s ease;

  &.active {
    background: #ff4757;
    border-color: #ff4757;

    .type-text {
      color: #fff;
      font-weight: 500;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.type-text {
  font-size: 26rpx;
  color: #333;
  transition: all 0.3s ease;
}

// 操作按钮样式
.filter-actions {
  display: flex;
  gap: 20rpx;
}

.reset-btn {
  flex: 1;
  padding: 24rpx;
  background: #f5f5f5;
  border: 1rpx solid #e0e0e0;
  border-radius: 20rpx;
  text-align: center;
  font-size: 30rpx;
  color: #333;
  transition: all 0.3s ease;

  &:active {
    background: #e8e8e8;
  }
}

.confirm-btn {
  flex: 1;
  padding: 24rpx;
  background: #ff4757;
  border-radius: 20rpx;
  text-align: center;
  font-size: 30rpx;
  color: #fff;
  font-weight: 500;
  transition: all 0.3s ease;

  &:active {
    background: #e63946;
  }
}
</style>