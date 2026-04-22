<template>
	<view>
		<!-- 侧边栏菜单 -->
		<mine-sidebar :show="moreShow" :screenHeight="screenHeight" @close="closeMore"
			@edit-data="goToEditData" @drafts="goToDrafts" @my-comments="goToMyComments" @browse-record="goToBrowseRecord"
			@good-product="goToGoodProduct" @orders="goToOrders" @shopping-cart="goToShoppingCart"
			@address-management="goToAddressManagement" @ai-assistant="goToAIAssistant" @chat-history="goToChatHistory"
			@help-service="goToCustomerServiceFromIndex"
			@about="goToAbout" @scan="handleScan" @setting="goToSetting"></mine-sidebar>
		
		<view class="msg-divider"></view>
		<!-- 状态栏背景 - 白色（非微信端需要占位；微信端用系统栏，无需占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #F4EDE2;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 顶部 Tab + 搜索 (design: padding 6px 16px 10px, flex, gap 12) -->
		<view :style="{ top: navBarTop }"
			class="nav-bar">
			<!-- 左侧菜单 -->
			<view class="nav-menu-btn" @click="openMore">
				<u-icon name="list" size="22" color="#231710"></u-icon>
			</view>
			<!-- 中间 Tabs -->
			<view class="nav-tabs">
				<view v-for="(tab, idx) in tabsList" :key="idx"
					class="nav-tab-item"
					:class="{ active: actTab === idx }"
					@click="changetabs({ index: idx })">
					<text>{{ tab.name }}</text>
					<view v-if="actTab === idx" class="nav-tab-line"></view>
				</view>
			</view>
			<!-- 右侧搜索：圆形白底按钮 (design: 36px circle, surface bg, shadow) -->
			<view class="nav-search-btn" @click="goToSearch">
				<u-icon name="search" color="#231710" size="18"></u-icon>
			</view>
		</view>
	<view :style="{ height: navPlaceholderHeight }" style="width: 100%;background-color:#F4EDE2;"></view>

	<!-- 主swiper：支持左右滑动切换 -->
		<!-- #ifdef MP-WEIXIN -->
		<view :style="{position: 'relative', height: notesHeight + 'px', marginTop: '5px'}">
			<swiper :current="mainSwiperCurrent" @change="onMainSwiperChange" @transition="onMainSwiperTransition"
				@animationfinish="onMainSwiperAnimationFinish" :style="{height: '100%', position: 'relative'}" :duration="300"
				:disable-touch="shouldDisableMainSwiper || (actTab === 0 && selectedUserId)">
			<!-- #endif -->
				<!-- #ifndef MP-WEIXIN -->
				<swiper :current="actTab" @change="onMainSwiperChange"
					:style="{height: notesHeight + 'px', marginTop: '5px'}" :duration="300"
					:disable-touch="shouldDisableMainSwiper">
				<!-- #endif -->
		<!-- 关注页 -->
		<swiper-item>
						<follow-page :notes-height="notesHeight" :enablerefresh="enablerefresh"
							@selectedUserIdChange="handleSelectedUserIdChange"
							@goToUser="goToUser"
							@goToDetail="goToDetail"></follow-page>
		</swiper-item>

		<!-- 发现页 -->
		<swiper-item>
						<discover-page :notes-height="notesHeight" :enablerefresh="enablerefresh"
							@switchToPrevMainTab="switchToPrevMainTab" @switchToNextMainTab="switchToNextMainTab"
							@goToDetail="goToDetail" @goToUser="goToUser"></discover-page>
		</swiper-item>

		<!-- 附近页 -->
		<swiper-item>
						<nearby-page :notes-height="notesHeight" :enablerefresh="enablerefresh"
							:loading-text="loadingText" :loadmore-text="loadmoreText" :nomore-text="nomoreText"
							@cityNameChange="handleCityNameChange" @goToDetail="goToDetail"
							@goToUser="goToUser"></nearby-page>
	</swiper-item>
				<!-- #ifdef MP-WEIXIN -->
	</swiper>
		</view>
		<!-- #endif -->
		<!-- #ifndef MP-WEIXIN -->
		</swiper>
		<!-- #endif -->
	
    <custom-tabbar ref="tabbar" :current="0" @update:current="updateCurrentTab"></custom-tabbar>
	</view>
</template>

<script>
// import CustomTabbar from '@/components/custom-tabbar/CustomTabBar.vue'
import FollowPage from '@/components/index-pages/FollowPage.vue';
import DiscoverPage from '@/components/index-pages/DiscoverPage.vue';
import NearbyPage from '@/components/index-pages/NearbyPage.vue';
import MineSidebar from '@/components/mine-pages/MineSidebar.vue';
import {
  getLastNotesByPage,
  getNotesByCategoryId,
  getVideoNote,
  praiseOrCancelNotes,
  collectOrCancelNotes,
  getFindList
} from '@/apis/notes_service.js';
import { searchNotesNearby } from '@/apis/search_service.js';
import { pxToRpx, weChatTimeFormat } from '@/utils/util.js';
// mapNoteItem、ImageCacheUtil、preloadCriticalImages、getCompressedUrl 已移至子组件
export default {
  components: {
    FollowPage,
    DiscoverPage,
    NearbyPage,
    MineSidebar
  },
  data() {
    return {
      statusBarHeight: 0,
      notesHeight: 0,
      navBarTop: '0px',
      navPlaceholderHeight: '0px',
      screenHeight: 0,
      moreShow: false, // 侧边栏显示状态
      actTab: 1,
      cityName: '附近',
      tabsList: [
        {
          name: '关注'
        },
        {
          name: '发现'
        },
        {
          name: '附近'
        }
      ],
      // 当前选中的用户ID（用于筛选，从 FollowPage 组件接收）
      selectedUserId: null,
      // 微信小程序：触摸事件相关
      blockTouchStartX: 0,
      blockTouchStartY: 0,
      blockTouchStartTime: 0,
      // 发现页数据已移至 DiscoverPage 组件
      notesList: [
        {
          // 关注页数据已移至 FollowPage 组件，这里保留空结构以兼容其他逻辑
          notesList: [],
          leftList: [],
          rightList: [],
          leftHeight: 0,
          rightHeight: 0,
          status: 'loadmore',
          page: 1,
          pageSize: 6,
          initialLoadSize: 3
        },
        {
          // 发现页数据已移至 DiscoverPage 组件，这里保留空结构以兼容其他逻辑
          notesList: [],
          leftList: [],
          rightList: [],
          leftHeight: 0,
          rightHeight: 0,
          status: 'loadmore',
          page: 1,
          pageSize: 6,
          initialLoadSize: 3
        }
      ],
      // 附近页数据已移至 NearbyPage 组件
      // 发现页数据已移至 DiscoverPage 组件
      loadingText: '加载中...',
      loadmoreText: '加载更多',
      nomoreText: '到底了',
      refreshing: false,
      enablerefresh: true,
      // 性能优化相关
      tabSwitchTimer: null
    };
  },
  computed: {
    // 智能控制主swiper滑动
    shouldDisableMainSwiper() {
      // ✅ 在关注页且选中用户时禁用主swiper，让左右滑动只用于切换关注用户
      if (this.actTab === 0 && this.selectedUserId) {
        console.log('🎛️ 主swiper禁用状态: true (关注页且选中用户)');
        return true;
      }

      // 发现页的主swiper控制已移至 DiscoverPage 组件
      // 主页面不再需要控制发现页的swiper禁用

      console.log('🎛️ 主swiper禁用状态: false');
      return false;
    },
    // currentCategoryData 已移至 DiscoverPage 组件

    // #ifdef MP-WEIXIN
    // 微信小程序：强制控制主swiper的current值，选中用户时始终为0
    mainSwiperCurrent() {
      // 如果选中了用户，强制返回0，不允许切换到其他页面
      if (this.selectedUserId) {
        // 如果 actTab 不是 0，立即纠正
        if (this.actTab !== 0) {
          console.log('⚠️ 微信小程序：强制设置swiper current为0 (actTab:', this.actTab, ')');
          // 使用 setTimeout 避免在计算属性中直接修改数据
          setTimeout(() => {
            if (this.actTab !== 0) {
              this.$set(this, 'actTab', 0);
              this.$forceUpdate();
            }
          }, 0);
        }
        return 0;
      }
      return this.actTab;
    }
    // #endif
  },
  // #ifdef MP-WEIXIN
  watch: {
    // 监听 actTab，如果选中用户且切换到非关注页，强制恢复
    actTab(newVal, oldVal) {
      // 如果选中了用户，且尝试切换到非关注页，立即阻止
      if (this.selectedUserId && newVal !== 0) {
        console.log(
          '⚠️ 微信小程序：watch检测到actTab被改变，强制恢复 (oldVal:',
          oldVal,
          'newVal:',
          newVal,
          ')'
        );
        // 立即同步更新，不使用 $nextTick
        this.$set(this, 'actTab', 0);
        this.$forceUpdate();
        // 再次确认，防止异步更新
        setTimeout(() => {
          if (this.actTab !== 0) {
            this.$set(this, 'actTab', 0);
            this.$forceUpdate();
          }
        }, 0);
      }
    }
  },
  // #endif
  methods: {
    // 处理关注页 selectedUserId 变化
    handleSelectedUserIdChange(userId) {
      this.selectedUserId = userId;
      console.log('🔄 关注页 selectedUserId 变化:', userId);
      // #ifdef MP-WEIXIN
      // 微信小程序：如果选中了用户但当前不在关注页，强制切换到关注页
      if (this.actTab !== 0) {
        console.log('⚠️ 微信小程序：选中用户时不在关注页，强制切换');
        this.$set(this, 'actTab', 0);
        this.$forceUpdate();
      }
      // #endif
    },
    // 格式化地址显示方法（只显示市+详细地址）
    formatAddress(item) {
      if (!item) return '';
      const parts = [];
      if (item.city) parts.push(item.city);
      if (item.address) parts.push(item.address);
      // 兼容旧数据：如果没有新字段但有location字段
      if (parts.length === 0 && item.location) {
        const location = item.location;
        return location.length <= 12 ? location : location.substring(0, 12) + '...';
      }
      const fullAddress = parts.join(' ');
      return fullAddress.length > 12 ? fullAddress.substring(0, 12) + '...' : fullAddress;
    },
    // 地址缩短显示方法（兼容旧代码）
    shortenLocation() {
      return location => {
        if (!location) return '';
        if (location.length <= 8) return location;
        return location.substring(0, 8) + '...';
      };
    },
    // 获取城市信息并更新附近标签
    async getCityInfo() {
      try {
        // 检查缓存，避免重复请求
        const cachedCity = uni.getStorageSync('nearby_city');
        if (cachedCity) {
          this.$set(this.tabsList, 2, {
            ...this.tabsList[2],
            name: cachedCity
          });
          console.log('🏙️ 使用缓存的城市名:', cachedCity);
          return;
        }

        console.log('🏙️ 开始获取城市信息');
        const res = await searchNotesNearby({
          page: 1,
          pageSize: 1 // 只获取1条数据，主要是为了获取城市信息
        });

        if (res.code === 200 && res.data && res.data.cityName) {
          const formattedCityName = this.formatCityName(res.data.cityName);
          if (formattedCityName) {
            this.$set(this.tabsList, 2, {
              ...this.tabsList[2],
              name: formattedCityName
            });
            // 缓存城市名
            uni.setStorageSync('nearby_city', formattedCityName);
            console.log('🏙️ 首页加载时更新附近页面城市名:', formattedCityName);
          }
        } else {
          console.log('🏙️ 未获取到城市信息，保持默认"附近"');
        }
      } catch (error) {
        console.error('🏙️ 获取城市信息失败:', error);
        // 失败时保持默认"附近"标签
      }
    },
    // 规范城市名显示：去除"市/省/自治区/特别行政区"等后缀
    formatCityName(name) {
      if (!name) return '';
      const map = {
        广西壮族自治区: '广西',
        内蒙古自治区: '内蒙古',
        新疆维吾尔自治区: '新疆',
        宁夏回族自治区: '宁夏',
        西藏自治区: '西藏',
        香港特别行政区: '香港',
        澳门特别行政区: '澳门'
      };
      if (map[name]) return map[name];
      // 一般性去后缀
      return name.replace(/(特别行政区|自治区|自治州|地区|盟|省|市)$/u, '');
    },
    // 处理附近页城市名变化事件
    handleCityNameChange(cityName) {
      console.log('🏙️ 接收附近页城市名变化:', cityName);
      if (cityName) {
        this.$set(this.tabsList, 2, {
          ...this.tabsList[2],
          name: cityName
        });
      }
    },
    // 通用：通过服务端或高德地图WebService进行反地理解析，返回城市名
    async fetchCityByServer(lat, lng) {
      try {
        // 方案A（推荐）：调用你方后端接口做反地理解析（避免前端暴露key）
        // const { data } = await uni.request({ url: 'https://your.api/reverseGeocode', data: { lat, lng } });
        // if (data?.code === 200 && data?.data?.city) return data.data.city;

        // 方案B：直接使用高德地图 WebService（H5/小程序可用）。
        // 优先从配置文件/manifest 读取
        let AMAP_KEY = '';
        try {
          // 从根目录 .env.js 导出读取
          const conf = require('@/.env.js');
          AMAP_KEY = conf.AMAP_KEY || '';
        } catch (e) {}
        // 兜底：从全局配置读取（例如在 App.vue 的 onLaunch 里注入 getApp().$amapKey = 'xxxx'）
        if (!AMAP_KEY && typeof getApp === 'function') {
          try {
            AMAP_KEY = getApp().globalData?.AMAP_KEY || '';
          } catch (e) {}
        }
        // 再兜底：环境变量（如 uniCloud 或自定义 env 文件）
        if (!AMAP_KEY) {
          try {
            AMAP_KEY = process?.env?.VUE_APP_AMAP_KEY || '';
          } catch (e) {}
        }
        // 最后兜底：若仍为空，尽量不发起请求
        if (!AMAP_KEY) return '';
        const { data } = await uni.request({
          url: 'https://restapi.amap.com/v3/geocode/regeo',
          method: 'GET',
          data: {
            location: `${lng},${lat}`,
            key: AMAP_KEY,
            extensions: 'base'
          }
        });
        if (data?.status === '1') {
          const comp = data?.regeocode?.addressComponent || {};
          // 高德直辖市下 city 可能为空数组，做兜底
          const city =
            (Array.isArray(comp.city) ? comp.city[0] : comp.city) ||
            comp.province ||
            comp.district ||
            '';
          return city;
        }
        return '';
      } catch (e) {
        return '';
      }
    },
    // 反查城市并替换“附近”标签
    updateNearbyTabTitleByReverseGeocode(lat, lng) {
      if (!lat || !lng) return;
      // 使用腾讯地图/H5内置反地理或你已有的后端接口。这里先尝试 uni.reverseGeocoder（App/H5 支持），失败则忽略。
      // #ifdef APP-PLUS
      try {
        uni.reverseGeocoder({
          location: {
            latitude: lat,
            longitude: lng
          },
          success: res => {
            let city =
              res?.result?.addressComponent?.city || res?.result?.addressComponent?.province;
            city = this.formatCityName(city);
            if (city) {
              // 持久化，避免来回切换频繁请求
              try {
                uni.setStorageSync('nearby_city', city);
              } catch (e) {}
              // 确保触发响应式更新
              this.$set(this.tabsList, 2, {
                ...this.tabsList[2],
                name: city
              });
            }
          }
        });
      } catch (e) {}
      // #endif
      // #ifndef APP-PLUS
      // H5/小程序：调用服务端或腾讯地图WebService，成功后替换“附近”为真实城市
      this.fetchCityByServer(lat, lng).then(city => {
        city = this.formatCityName(city);
        if (city) {
          try {
            uni.setStorageSync('nearby_city', city);
          } catch (e) {}
          this.$set(this.tabsList, 2, {
            ...this.tabsList[2],
            name: city
          });
        }
      });
      // #endif
    },
    // 定位失败时的兜底：先用高德IP定位拿城市，再取城市中心点作为经纬度，并更新“附近”标题
    async ipFallbackAndUpdateTitle() {
      try {
        const conf = require('@/.env.js');
        const AMAP_KEY = conf.AMAP_KEY || '';
        if (!AMAP_KEY) return;
        // 1) IP -> 城市
        const { data: ipRes } = await uni.request({
          url: 'https://restapi.amap.com/v3/ip',
          method: 'GET',
          data: {
            key: AMAP_KEY
          }
        });
        let city = '';
        if (ipRes?.status === '1') {
          city = ipRes.city || ipRes.province || '';
        }
        // IPv6 / 部分网络场景再试一次
        if (!city) {
          try {
            const { data: ipResV6 } = await uni.request({
              url: 'https://restapi.amap.com/v3/ip',
              method: 'GET',
              data: {
                key: AMAP_KEY,
                type: 4
              }
            });
            if (ipResV6?.status === '1') {
              city = ipResV6.city || ipResV6.province || '';
            }
          } catch (e) {}
        }
        if (city) {
          if (city) {
            // 2) city -> 中心经纬度
            const { data: geoRes } = await uni.request({
              url: 'https://restapi.amap.com/v3/geocode/geo',
              method: 'GET',
              data: {
                key: AMAP_KEY,
                address: city
              }
            });
            const loc = geoRes?.status === '1' && geoRes?.geocodes?.[0]?.location;
            if (loc) {
              const [lng, lat] = String(loc).split(',');
              this.longitude = Number(lng);
              this.latitude = Number(lat);
            }
            const showCity = this.formatCityName(city);
            try {
              uni.setStorageSync('nearby_city', showCity);
            } catch (e) {}
            this.$set(this.tabsList, 2, {
              ...this.tabsList[2],
              name: showCity
            });
          }
        } else {
          // 使用历史缓存或默认城市
          let cached = '';
          try {
            cached = uni.getStorageSync('nearby_city') || '';
          } catch (e) {}
          const fallbackCity = this.formatCityName(cached) || '北京';
          this.$set(this.tabsList, 2, {
            ...this.tabsList[2],
            name: fallbackCity
          });
        }
      } catch (e) {
        // ignore
      }
    },
    updateCurrentTab(index) {
      console.log('切换到 tab:', index);
    },
    // 打开侧边栏菜单
    openMore() {
      this.moreShow = true;
      // 尝试隐藏TabBar，如果失败则忽略错误（自定义TabBar可能不支持此API）
      uni.hideTabBar({
        animation: true,
        fail: err => {
          console.log('hideTabBar failed:', err);
        }
      });
    },
    // 关闭侧边栏菜单
    closeMore() {
      this.moreShow = false;
      // 尝试显示TabBar，如果失败则忽略错误（自定义TabBar可能不支持此API）
      uni.showTabBar({
        animation: true,
        fail: err => {
          console.log('showTabBar failed:', err);
        }
      });
    },
    // 跳转到编辑资料页面
    goToEditData() {
      this.closeMore();
      uni.navigateTo({
        url: '/pkg-profile/pages/editData/editData'
      });
    },
    // 跳转到草稿页面
    goToDrafts() {
      this.closeMore();
      uni.navigateTo({
        url: '/pkg-mine/pages/drafts/drafts'
      });
    },
    // 跳转到收到的评论页面（消息页面的评论和@）
    goToMyComments() {
      this.closeMore();
      uni.navigateTo({
        url: '/pkg-msg/pages/comment/comment'
      });
    },
    // 跳转到浏览记录页面
    goToBrowseRecord() {
      this.closeMore();
      uni.navigateTo({
        url: '/pkg-mine/pages/browseRecord/browseRecord'
      });
    },
    // 跳转到闲置好物页面（市集页面）
    goToGoodProduct() {
      this.closeMore();
      uni
        .navigateTo({
          url: '/pkg-main/pages/idle/idle'
        })
        .catch(err => {
          console.log('navigateTo 失败，尝试 reLaunch:', err);
          uni.reLaunch({
            url: '/pkg-main/pages/idle/idle'
          });
        });
    },
    // 跳转到订单页面
    goToOrders() {
      this.closeMore();
      uni.navigateTo({
        url: '/pkg-mine/pages/orders/orders'
      });
    },
    // 跳转到心愿单页面
    goToShoppingCart() {
      this.closeMore();
      uni.navigateTo({
        url: '/pkg-mine/pages/shoppingCart/shoppingCart'
      });
    },
    // 跳转到收货地址页面
    goToAddressManagement() {
      this.closeMore();
      uni.navigateTo({
        url: '/pkg-mine/pages/addressManagement/addressManagement'
      });
    },
    // 跳转到关于页面
    goToAbout() {
      this.closeMore();
      // uni.showToast({
      // 	title: '功能开发中',
      // 	icon: 'none'
      // })
    },
    // 跳转到设置页面
    goToSetting() {
      this.closeMore();
      uni.navigateTo({
        url: '/pkg-mine/pages/setting/setting'
      });
    },
    // 跳转到AI助手页面
    goToAIAssistant() {
      this.closeMore();
      uni.navigateTo({
        url: '/pkg-ai/pages/model-select/model-select'
      });
    },
    // 跳转到对话记录页面
    goToChatHistory() {
      this.closeMore();
      uni.navigateTo({
        url: '/pkg-ai/pages/chat-history/chat-history'
      });
    },
    // 侧边栏 - 帮助与客服：跳转到客服聊天页面
    goToCustomerServiceFromIndex() {
      this.closeMore();
      const customerServiceId = '0';
      const customerServiceName = '客服';
      const customerServiceAvatar = '/static/customer-service-avatar.png'; // 可根据需要替换为实际客服头像

      uni.navigateTo({
        url: `/pkg-msg/pages/chat/chat?userId=${customerServiceId}&userName=${customerServiceName}&avatarUrl=${customerServiceAvatar}&chatType=0`,
        fail: err => {
          console.error('跳转客服失败:', err);
          uni.showToast({
            title: '页面加载失败',
            icon: 'none'
          });
        }
      });
    },
    // 处理扫码功能
    handleScan() {
      this.closeMore();
      // 检查平台是否支持扫码功能
      // #ifdef H5
      uni.showToast({
        title: 'H5平台暂不支持扫码功能',
        icon: 'none',
        duration: 2000
      });
      return;
      // #endif

      // 调用扫码功能
      uni.scanCode({
        success: res => {
          console.log('扫码结果:', res);
          uni.showToast({
            title: '扫码成功',
            icon: 'success'
          });
        },
        fail: err => {
          console.error('扫码失败:', err);
          // 检查是否是用户主动取消
          // 用户取消时，err.errMsg 通常包含 "cancel" 或 "fail cancel"
          const errMsg = err.errMsg || err.message || '';
          if (
            errMsg.includes('cancel') ||
            errMsg.includes('取消') ||
            errMsg.includes('user cancel')
          ) {
            // 用户主动取消，不显示错误提示
            console.log('用户主动取消扫码');
            return;
          }
          // 其他错误才显示提示
          uni.showToast({
            title: '扫码失败',
            icon: 'none'
          });
        }
      });
    },
    goToSearch() {
      uni.navigateTo({
        url: '/pkg-search/pages/searchPage/searchPage'
      });
    },
    goToUser(id) {
      uni.navigateTo({
        url: '/pkg-mine/pages/mine/otherMine?userId=' + id
      });
    },
    async getImageHeight(url) {
      // 先检查缓存
      const cachedImage = ImageCacheUtil.getCachedImage(url);
      if (cachedImage) {
        return cachedImage;
      }

      // 检查是否为微信小程序环境
      // #ifdef MP-WEIXIN
      // 在微信小程序中，直接使用默认高度，避免网络请求
      console.warn('⚡ 微信小程序环境，直接使用默认高度，避免网络请求:', url);
      const defaultInfo = {
        height: 200, // 默认高度
        path: url
      };
      // 存入缓存，避免重复检查
      ImageCacheUtil.cacheImage(url, defaultInfo);
      return defaultInfo;
      // #endif

      // #ifndef MP-WEIXIN
      // H5环境：正常获取图片高度
      return new Promise(resolve => {
        uni.getImageInfo({
          src: url,
          success: res => {
            const imageInfo = {
              height: pxToRpx(res.height),
              path: res.path
            };
            // 存入缓存
            ImageCacheUtil.cacheImage(url, imageInfo);
            resolve(imageInfo);
          },
          fail: error => {
            console.warn('⚠️ getImageInfo 失败，使用默认高度:', error, url);
            // 加载失败时的默认值
            const defaultInfo = {
              height: 200,
              path: url
            };
            // 存入缓存，避免重复请求
            ImageCacheUtil.cacheImage(url, defaultInfo);
            resolve(defaultInfo);
          }
        });
      });
      // #endif
    },
    // praiseNotes、updateNotesListLikeStatus 和 collectNotes 已移至 NearbyPage、DiscoverPage 和 FollowPage 组件
    // 发现页相关方法已移至 DiscoverPage 组件

    // 切换到上一个主标签
    switchToPrevMainTab() {
      if (this.actTab > 0) {
        console.log('🔄 边界滑动：切换到关注页');
        // 添加触觉反馈（安全调用）
        this.safeVibrate();
        // 发现页的边界记录已移至 DiscoverPage 组件
        // 直接修改actTab并触发相关逻辑
        const newTab = this.actTab - 1;
        this.actTab = newTab;
        this.$nextTick(() => {
          this.handleTabSwitch(newTab);
        });
      }
    },

    // 切换到下一个主标签
    switchToNextMainTab() {
      if (this.actTab < this.tabsList.length - 1) {
        console.log('🔄 边界滑动：切换到附近页');
        // 添加触觉反馈（安全调用）
        this.safeVibrate();
        // 发现页的边界记录已移至 DiscoverPage 组件
        // 直接修改actTab并触发相关逻辑
        const newTab = this.actTab + 1;
        this.actTab = newTab;
        this.$nextTick(() => {
          this.handleTabSwitch(newTab);
        });
      }
    },

    // 监听分类页滚动，控制二级分类显示/隐藏
    onCategoryScroll(e) {
      // ✅ 固定分类栏：禁用滚动隐藏功能
      return;

      // 以下代码已禁用
      // if (this.actTab !== 1) return; // 只在发现页生效
      //
      // const scrollTop = e.detail.scrollTop;
      // const delta = scrollTop - this.lastScrollTop;
      //
      // // 向下滚动且超过阈值 -> 隐藏
      // if (delta > this.scrollThreshold && scrollTop > 100) {
      // 	if (this.showCategoryTabs) {
      // 		console.log('🔽 隐藏二级分类，启用主swiper滑动');
      // 		this.showCategoryTabs = false;
      // 	}
      // }
      // // 向上滚动 -> 显示
      // else if (delta < -10) {
      // 	if (!this.showCategoryTabs) {
      // 		console.log('🔼 显示二级分类，禁用主swiper滑动');
      // 		this.showCategoryTabs = true;
      // 	}
      // }
      //
      // this.lastScrollTop = scrollTop;
    },

    // #ifdef MP-WEIXIN
    // 微信小程序：透明层触摸开始 - 记录初始位置
    onBlockSwiperTouchStart(e) {
      if (e && e.touches && e.touches.length > 0) {
        this.blockTouchStartX = e.touches[0].clientX;
        this.blockTouchStartY = e.touches[0].clientY;
        this.blockTouchStartTime = Date.now();
      }
    },
    // 微信小程序：透明层触摸移动 - 判断滑动方向
    onBlockSwiperTouchMove(e) {
      if (!e || !e.touches || e.touches.length === 0) {
        return;
      }

      const touch = e.touches[0];
      const deltaX = touch.clientX - this.blockTouchStartX;
      const deltaY = touch.clientY - this.blockTouchStartY;
      const absDeltaX = Math.abs(deltaX);
      const absDeltaY = Math.abs(deltaY);

      // 如果横向滑动距离大于纵向滑动距离，且是向左滑动（从关注页切换到发现页），则拦截
      // 如果横向滑动距离小于50px，可能是FollowPage内部的左右滑动切换用户，允许通过
      if (absDeltaX > absDeltaY && absDeltaX > 50 && deltaX < 0) {
        // 向左滑动且距离较大，这是要切换到发现页，必须拦截
        console.log(
          '⚠️ 拦截横向向左滑动，防止切换到发现页 (deltaX:',
          deltaX,
          'deltaY:',
          deltaY,
          ')'
        );
        // 阻止事件继续传播到 swiper
        if (e && typeof e.stopPropagation === 'function') {
          e.stopPropagation();
        }
        if (e && typeof e.preventDefault === 'function') {
          e.preventDefault();
        }
        // 确保 actTab 保持为 0
        if (this.actTab !== 0) {
          this.$set(this, 'actTab', 0);
          this.$forceUpdate();
        }
        return false;
      }
      // 其他情况（纵向滑动、向右滑动、或横向滑动距离很小）允许事件传递到子组件
      // 不阻止事件传播，让子组件可以正常处理
    },
    // 微信小程序：透明层触摸结束 - 最终检查
    onBlockSwiperTouchEnd(e) {
      if (!e || !e.changedTouches || e.changedTouches.length === 0) {
        return;
      }

      const touch = e.changedTouches[0];
      const deltaX = touch.clientX - this.blockTouchStartX;
      const deltaY = touch.clientY - this.blockTouchStartY;
      const absDeltaX = Math.abs(deltaX);
      const absDeltaY = Math.abs(deltaY);
      const timeDelta = Date.now() - this.blockTouchStartTime;

      // 快速横向向左滑动（可能是滑动切换主页面的手势），需要拦截
      if (absDeltaX > absDeltaY && absDeltaX > 50 && deltaX < 0 && timeDelta < 300) {
        console.log('⚠️ 触摸结束：检测到快速向左滑动，确保不切换到发现页');
        if (e && typeof e.stopPropagation === 'function') {
          e.stopPropagation();
        }
        if (e && typeof e.preventDefault === 'function') {
          e.preventDefault();
        }
        // 确保 actTab 保持为 0
        if (this.actTab !== 0) {
          this.$set(this, 'actTab', 0);
          this.$forceUpdate();
        }
        return false;
      }
    },

    // 微信小程序：主swiper过渡事件，用于提前拦截
    onMainSwiperTransition(e) {
      const dx = e.detail.dx;
      // 如果选中了用户且当前在关注页（actTab === 0），阻止任何方向的滑动
      if (this.selectedUserId && this.actTab === 0) {
        // 如果向左滑动（dx < 0），这是要切换到发现页，必须阻止
        if (dx < 0) {
          console.log('⚠️ 微信小程序：阻止向左滑动切换到发现页 (dx:', dx, ')');
          // 立即强制设置 actTab 为 0，阻止切换
          this.$set(this, 'actTab', 0);
          // 同时使用 $forceUpdate 强制更新视图
          this.$forceUpdate();
          // 阻止事件继续传播
          if (e && typeof e.stopPropagation === 'function') {
            e.stopPropagation();
          }
          if (e && typeof e.preventDefault === 'function') {
            e.preventDefault();
          }
        }
        // 无论什么方向，只要选中了用户且当前在关注页，都强制保持 actTab 为 0
        this.$set(this, 'actTab', 0);
        this.$forceUpdate();
      }
    },

    // 微信小程序：主swiper动画完成事件，用于最终拦截
    onMainSwiperAnimationFinish(e) {
      const current = e.detail.current;
      // 如果选中了用户且当前在关注页（actTab === 0），但swiper已经切换到了其他页面，强制回退
      if (this.selectedUserId && this.actTab === 0 && current !== 0) {
        console.log(
          '⚠️ 微信小程序：动画完成后检测到错误切换，强制回退 (current:',
          current,
          'actTab:',
          this.actTab,
          ')'
        );
        // 立即强制回退
        this.$set(this, 'actTab', 0);
        this.$forceUpdate();
        // 使用多个延迟确保回退成功
        setTimeout(() => {
          if (this.actTab !== 0 || current !== 0) {
            console.log('⚠️ 延迟1：强制回退');
            this.$set(this, 'actTab', 0);
            this.$forceUpdate();
          }
        }, 10);
        setTimeout(() => {
          if (this.actTab !== 0 || current !== 0) {
            console.log('⚠️ 延迟2：强制回退');
            this.$set(this, 'actTab', 0);
            this.$forceUpdate();
          }
        }, 50);
        setTimeout(() => {
          if (this.actTab !== 0 || current !== 0) {
            console.log('⚠️ 延迟3：强制回退');
            this.$set(this, 'actTab', 0);
            this.$forceUpdate();
          }
        }, 100);
      }
    },
    // #endif

    // 主swiper滑动事件，同步actTab
    onMainSwiperChange(e) {
      const current = e.detail.current;
      console.log(
        '🔄 主swiper滑动:',
        current,
        '当前actTab:',
        this.actTab,
        '选中用户:',
        this.selectedUserId
      );

      if (this.actTab === current) {
        console.log('⚠️ actTab相同，跳过切换');
        return;
      }

      // ✅ 如果选中了用户且当前在关注页（actTab === 0），则阻止切换到任何其他页面（current !== 0）
      if (this.selectedUserId && this.actTab === 0 && current !== 0) {
        console.log(
          '⚠️ 关注页选中用户时，阻止切换到其他页面 (actTab:',
          this.actTab,
          'current:',
          current,
          ')'
        );
        // #ifdef MP-WEIXIN
        // 微信小程序：立即强制恢复actTab，使用 $set 确保响应式更新
        this.$set(this, 'actTab', 0);
        this.$forceUpdate();
        // 使用 setTimeout 确保 swiper 也回退（因为 swiper 的 current 是双向绑定的）
        setTimeout(() => {
          // 再次确认，确保同步
          if (this.actTab !== 0) {
            console.log('⚠️ 强制恢复actTab为0');
            this.$set(this, 'actTab', 0);
            this.$forceUpdate();
          }
        }, 10);
        // 再次延迟确认，防止异步更新
        setTimeout(() => {
          if (this.actTab !== 0) {
            console.log('⚠️ 二次强制恢复actTab为0');
            this.$set(this, 'actTab', 0);
            this.$forceUpdate();
          }
        }, 100);
        // #endif

        // #ifndef MP-WEIXIN
        // H5和其他平台：不更新 actTab，保持当前状态
        // #endif

        return;
      }

      this.actTab = current;

      // 发现页逻辑已移至 DiscoverPage 组件

      // 触发标签切换逻辑
      this.handleTabSwitch(current);
    },
    // fetchFindList, refreshFindNotes 已移至 DiscoverPage 组件
    goToDetail(id, type, fromVideoCategory) {
      console.log(type);
      if (type == 1) {
        // 图文
        uni.navigateTo({
          url: '/pkg-detail/pages/notesDetail/notesDetail?notesId=' + id
        });
      } else if (type == 2) {
        // 视频
        console.log('视频');
        // 判断是否从视频分类进入
        let url = '/pkg-detail/pages/notesDetail/noteVideoD?notesId=' + id;
        // 如果是从视频分类进入，添加标识
        if (fromVideoCategory) {
          url += '&fromVideoCategory=1';
        }
        uni.navigateTo({
          url: url
        });
      }
    },
    animationFinish(e) {
      this.enablerefresh = true;
    },
    transition(e) {
      if (e.target.dx == 0) {
        this.enablerefresh = true;
      } else {
        this.enablerefresh = false;
      }
    },
    // loadMoreFindNotes 已移至 DiscoverPage 组件
    // assignToWaterfall 和 getApiConfig 已移至 NearbyPage 和 DiscoverPage 组件
    // getMoreNotes 已移至 NearbyPage 和 DiscoverPage 组件
    // mapNoteItem 已移至 NearbyPage 和 DiscoverPage 组件
    // refreshList 已移至 NearbyPage 和 DiscoverPage 组件
    changetabs(e) {
      let index = e.index;
      if (this.actTab === index) return; // 避免重复切换

      this.actTab = index;

      // 发现页逻辑已移至 DiscoverPage 组件

      // 优化：使用防抖避免快速切换
      if (this.tabSwitchTimer) {
        clearTimeout(this.tabSwitchTimer);
      }

      this.tabSwitchTimer = setTimeout(() => {
        this.handleTabSwitch(index);
      }, 100);
    },

    // 解析内容，将话题标签分离出来
    parseContentWithTags(content) {
      if (!content)
        return [
          {
            text: content,
            isTag: false
          }
        ];

      console.log('🔍 处理内容:', content);

      // 首先去除HTML标签，只保留纯文本
      let cleanContent = content.replace(/<[^>]+>/g, '');

      // 解码HTML实体（如 &lt; &gt; &amp; 等）
      // #ifdef H5
      if (typeof document !== 'undefined') {
        try {
          const textarea = document.createElement('textarea');
          textarea.innerHTML = cleanContent;
          cleanContent = textarea.value;
        } catch (e) {
          console.warn('HTML实体解码失败，使用备用方案:', e);
        }
      }
      // #endif

      // 通用的HTML实体解码（适用于所有平台）
      cleanContent = cleanContent
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&amp;/g, '&')
        .replace(/&quot;/g, '"')
        .replace(/&#39;/g, "'")
        .replace(/&#x27;/g, "'")
        .replace(/&nbsp;/g, ' ')
        .replace(/&#x2F;/g, '/');

      console.log('✨ 清理后的内容:', cleanContent);

      // 匹配话题标签的正则表达式：以#开头，后跟非空格字符
      const tagRegex = /#([^\s#]+)/g;

      // 将内容分割成文本和标签部分
      const parts = [];
      let lastIndex = 0;
      let match;

      while ((match = tagRegex.exec(cleanContent)) !== null) {
        console.log('🎯 找到话题标签:', match[0]);

        // 添加标签前的普通文本
        if (match.index > lastIndex) {
          parts.push({
            text: cleanContent.substring(lastIndex, match.index),
            isTag: false
          });
        }

        // 添加话题标签
        parts.push({
          text: match[0], // 包含#的完整标签
          isTag: true
        });

        lastIndex = match.index + match[0].length;
      }

      // 添加最后剩余的文本
      if (lastIndex < cleanContent.length) {
        parts.push({
          text: cleanContent.substring(lastIndex),
          isTag: false
        });
      }

      // 如果没有找到话题标签，直接返回清理后的内容
      if (parts.length === 0) {
        parts.push({
          text: cleanContent,
          isTag: false
        });
      }

      console.log('📝 解析结果:', parts);
      return parts;
    },
    // 新增：处理标签切换逻辑
    handleTabSwitch(index) {
      console.log('🔄 handleTabSwitch 被调用:', {
        index,
        actTab: this.actTab
      });

      // 附近页逻辑已移至 NearbyPage 组件
      // 发现页逻辑已移至 DiscoverPage 组件
      // 关注页逻辑已移至 FollowPage 组件
    },
    // onRefresh 已移至 NearbyPage、DiscoverPage 和 FollowPage 组件
    swipeIndex(e) {
      let index = e.detail.current;
      this.actTab = index;
      // 简化逻辑：直接调用 handleTabSwitch
      this.handleTabSwitch(index);
    },
    // typeTabChange 已移至 DiscoverPage 组件
    // 发现页相关方法（changeType, handleTypeSwitch, setCategoryCache, getCategoryCache, preloadNextCategory, loadCategoryData, processCategoryData, preloadCategoryData, handleReach, loadMoreCategoryData, preloadNextPage, appendCategoryData）已移至 DiscoverPage 组件
    // 切换内容展开/收起
    // toggleExpand 已移至 FollowPage 组件

    // showCacheStatus 已移至 DiscoverPage 组件
    // 添加防抖函数
    debounce(fn, delay = 200) {
      let timer = null;
      return function (...args) {
        if (timer) clearTimeout(timer);
        timer = setTimeout(() => {
          fn.apply(this, args);
        }, delay);
      };
    },

    // preloadImages 和 handleImageLoadComplete 已移至 NearbyPage 和 DiscoverPage 组件

    // 安全振动方法
    safeVibrate() {
      try {
        // 检查是否支持振动
        if (typeof uni.vibrateShort === 'function') {
          uni.vibrateShort({
            success: () => {
              console.log('✅ 振动成功');
            },
            fail: err => {
              console.warn('⚠️ 振动失败:', err);
            }
          });
        }
      } catch (error) {
        console.warn('⚠️ 振动调用异常:', error);
      }
    }

    // handleReach 已移至 NearbyPage 和 DiscoverPage 组件
  },
  created() {
    // onReach 已移至 NearbyPage 和 DiscoverPage 组件
  },
  onLoad() {
    // 设置状态栏样式为白色背景
    // #ifdef MP-WEIXIN
    uni.setNavigationBarColor({
      frontColor: '#000000', // 状态栏文字颜色为黑色
      backgroundColor: '#F4EDE2' // 状态栏背景色
    });
    // #endif

    uni.getSystemInfo({
      success: res => {
        console.log(res);
        this.statusBarHeight = res.statusBarHeight || 0;
        this.screenHeight = res.screenHeight;
        // #ifdef MP-WEIXIN
        // 微信端使用系统导航栏「爱宠社」，内容区已在系统栏下方，只需为本站自定义栏留 44px
        this.navBarTop = '0px';
        this.navPlaceholderHeight = '44px';
        this.notesHeight = res.windowHeight - 44;
        // #endif
        // #ifndef MP-WEIXIN
        this.navBarTop = this.statusBarHeight + 'px';
        this.navPlaceholderHeight = this.statusBarHeight + 40 + 'px';
        this.notesHeight = res.windowHeight - this.statusBarHeight - 40;
        // #endif
        console.log(this.statusBarHeight);
      }
    });

    // 首次加载：先检查缓存，避免重复请求
    console.log('🚀 页面加载，检查缓存状态');

    // 关注页初始化已移至 FollowPage 组件
    // 发现页初始化已移至 DiscoverPage 组件
    this.getCityInfo(); // 获取城市信息并更新附近标签

    // 接收全局徽标更新事件
    uni.$on('im:setTabBadge', count => {
      const tabbar = this.$refs.tabbar;
      if (tabbar && tabbar.setMessageBadge) tabbar.setMessageBadge(count);
    });
    // 首次进入时读取本地缓存的未读数，避免空白
    try {
      const cached = uni.getStorageSync('im_total_unread') || 0;
      const tabbar = this.$refs.tabbar;
      if (tabbar && tabbar.setMessageBadge) tabbar.setMessageBadge(cached);
    } catch (e) {}
  },

  onUnload() {
    // 清理定时器
    if (this.tabSwitchTimer) {
      clearTimeout(this.tabSwitchTimer);
    }
    // 关注页和发现页定时器清理已移至各自组件
  }
};
</script>
<style lang="scss">
.msg-divider {
  width: 100%;
  height: 1rpx;
  background: rgba(80, 50, 30, 0.1);
}

/* 关注页样式已移至 FollowPage 组件 */
/* 发现页和附近页样式已移至各自的组件 */

page,
.page-bg {
  background: #F4EDE2;
}

/* ── 顶部导航栏 (design: padding 6px 16px 10px, flex, gap 12) ── */
.nav-bar {
  position: fixed;
  width: 100%;
  height: 44px;
  background-color: #F4EDE2;
  z-index: 9999;
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  box-sizing: border-box;
}

.nav-menu-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* ── 自定义 Tabs (design: gap 18, 激活 18px/700, 非激活 15px/500) ── */
.nav-tabs {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 36rpx;
}

.nav-tab-item {
  position: relative;
  padding: 8rpx 4rpx;
  font-size: 30rpx;
  font-weight: 500;
  color: #8F7260;
  transition: all 0.2s ease;
}

.nav-tab-item.active {
  font-size: 36rpx;
  font-weight: 700;
  color: #231710;
}

/* Tab 下划线 (design: width 16, height 3, borderRadius 2, centered) */
.nav-tab-line {
  position: absolute;
  bottom: -4rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 32rpx;
  height: 6rpx;
  border-radius: 4rpx;
  background: #C97B4A;
}

/* ── 搜索按钮：圆形白底 (design: 36px circle, surface bg, shadow) ── */
.nav-search-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

// 优化"到底了"提示样式
::v-deep .u-loadmore__nomore-text,
::v-deep .u-loadmore-text {
  font-size: 20rpx !important;
  color: #C4AC95 !important;
}

::v-deep .u-loadmore__nomore-line {
  border-color: rgba(80, 50, 30, 0.1) !important;
}
</style>