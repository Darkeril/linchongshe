<script>
import { checkToken, getTrtcUserSig } from './apis/auth_apis.js';
import { getUserInfo } from './apis/user_service.js';
import { saveFile } from './utils/fileUtil.js';
import list from './uni_modules/uview-ui/libs/config/props/list.js';
import genTestUserSig from './debug/GenerateTestUserSig.js';
import logger from './utils/logger.js';
import errorHandler from './utils/errorHandler.js';
// // 原生插件引入提供如下接口：1V1音视频，群聊音视频。
// const TUICallKit = uni.requireNativePlugin('TencentCloud-TUICallKit');
// // 监听通话的状态，例如：异常、通话开始、结束等。
// const TUICallingEvent = uni.requireNativePlugin('globalEvent');
// // 目前仅提供如下接口：结束通话，设置用户视频画面的渲染模式
// const TUICallEngine = uni.requireNativePlugin('TencentCloud-TUICallKit-TUICallEngine');
let TUICallKit, TUICallingEvent, TUICallEngine;
if (process.env.UNI_PLATFORM === 'app-plus') {
  TUICallKit = uni.requireNativePlugin('TencentCloud-TUICallKit');
  TUICallingEvent = uni.requireNativePlugin('globalEvent');
  TUICallEngine = uni.requireNativePlugin('TencentCloud-TUICallKit-TUICallEngine');
}
export default {
  onLaunch: function () {
    logger.info('App Launch');
    uni.$TUICallKit = TUICallKit;
    uni.$TUICallingEvent = TUICallingEvent;
    uni.$TUICallEngine = TUICallEngine;
    this.$sqliteUtil.init();
    // 监听中间按钮点击事件
    uni.onTabBarMidButtonTap(() => {
      logger.info('点击了中间按钮');
      uni.showActionSheet({
        itemList: ['发布图文', '发布视频'],
        success: function (res) {
          logger.debug('ActionSheet选择:', res.tapIndex);
          if (res.tapIndex == 0) {
            // 发布图文
            uni.chooseImage({
              // ...
              success: function (res) {
                let tempFilePaths = res.tempFilePaths;
                let filePaths = [];
                if (process.env.UNI_PLATFORM === 'h5') {
                  uni.navigateTo({
                    url:
                      '/pkg-publish/pages/publishNotes/publishNotes?update=0&type=1&tempFilePaths=' +
                      JSON.stringify(tempFilePaths)
                  });
                } else {
                  tempFilePaths.forEach(item => {
                    uni.saveFile({
                      tempFilePath: item,
                      success: function (res) {
                        filePaths.push(res.savedFilePath);
                        if (filePaths.length == tempFilePaths.length) {
                          uni.navigateTo({
                            url:
                              '/pkg-publish/pages/publishNotes/publishNotes?update=0&type=1&tempFilePaths=' +
                              JSON.stringify(filePaths)
                          });
                        }
                      }
                    });
                  });
                }
              }
            });
          } else if (res.tapIndex == 1) {
            // 发布视频
            uni.chooseVideo({
              // ...
              success: function (res) {
                let tempFilePath = res.tempFilePath;
                if (process.env.UNI_PLATFORM === 'h5') {
                  uni.navigateTo({
                    url:
                      '/pkg-publish/pages/publishNotes/publishNotes?update=0&type=2&tempFilePaths=' +
                      JSON.stringify([tempFilePath])
                  });
                } else {
                  uni.saveFile({
                    tempFilePath: tempFilePath,
                    success: function (res) {
                      uni.navigateTo({
                        url:
                          '/pkg-publish/pages/publishNotes/publishNotes?update=0&type=2&tempFilePaths=' +
                          JSON.stringify([res.savedFilePath])
                      });
                    }
                  });
                }
              }
            });
          }
        },
        fail: function (res) {
          logger.error('ActionSheet失败:', res.errMsg);
        }
      });
    });
    const token = uni.getStorageSync('uniapp_token');
    // 检查 token 是否存在且有效（不为 null、undefined 或空字符串）
    if (!token || token === '' || token === 'undefined' || token === 'null') {
      logger.info('未检测到有效Token，跳转到登录相关页面');
      this.$ws.completeClose();
      // 清理可能存在的无效数据
      uni.removeStorageSync('uniapp_token');
      uni.removeStorageSync('uniapp_refresh_token');
      uni.removeStorageSync('userInfo');

      // 检查是否已经看过启动页（只在第一次启动时显示）
      const hasSeenSplash = uni.getStorageSync('hasSeenSplash');
      if (!hasSeenSplash) {
        // 第一次打开，跳转到启动页
        uni.reLaunch({
          url: '/pages/splash/splash'
        });
        uni.setStorageSync('hasSeenSplash', true);
      } else {
        // 已经看过启动页，直接跳转到登录方式选择页
        uni.reLaunch({
          url: '/pkg-auth/pages/login/loginMethod'
        });
      }
      return;
    }

    logger.info('检测到Token，开始验证...');
    checkToken()
      .then(res => {
        if (res.code === 200) {
          // 检查userInfo和userId是否存在
          const storedUserInfo = uni.getStorageSync('userInfo');
          const userId = storedUserInfo?.id;

          // 如果userId不存在，直接跳转到登录页
          if (!userId) {
            logger.warn('用户ID不存在，跳转到登录页');
            uni.removeStorageSync('uniapp_token');
            uni.removeStorageSync('uniapp_refresh_token');
            uni.removeStorageSync('userInfo');
            if (uni.$ws) {
              uni.$ws.completeClose();
            }
            setTimeout(() => {
              uni.reLaunch({
                url: '/pkg-auth/pages/login/login'
              });
            }, 500);
            return;
          }

          getUserInfo({
            userId: userId
          })
            .then(res => {
              logger.debug('获取用户信息成功:', res);
              const data = res.data;
              if (res.code === 200) {
                const userInfo = {
                  avatarUrl: data.avatar, // 头像
                  nickname: data.username, // 昵称
                  id: parseInt(data.id, 10) || 0, // 用户id（注意这里用id，和个人中心页一致）
                  hsId: data.hsId, // 爱宠社id
                  ipAddr: data.address, // IP属地
                  selfIntroduction: data.description, // 简介
                  age: data.age || null, // 年龄
                  sex: data.gender !== undefined ? parseInt(data.gender, 10) : null, // 性别
                  attentionNum: parseInt(data.followerCount) || 0, // 关注数
                  fansNum: parseInt(data.fanCount) || 0, // 粉丝数
                  homePageBackground: data.userCover, // 主页背景
                  area: data.address, // 地区
                  phoneNumber: data.phone,
                  tags: data.tags ? JSON.parse(data.tags) : []
                };
                this.userInfo = userInfo;
                uni.setStorageSync('userInfo', userInfo);
                this.$ws.init();
                // this.$callUtils.login(res.data.id)
              }
            })
            .catch(err => {
              // 如果是500错误且包含userId=undefined，或者错误信息表明用户信息获取失败，跳转到登录页
              const errorCode = err?.code || err?.statusCode;
              const errorMsg = err?.msg || err?.message || '';
              const errorUrl = err?.response?.config?.url || '';

              if (
                errorCode === 500 &&
                (errorUrl.includes('userId=undefined') ||
                  errorMsg.includes('userId') ||
                  errorMsg.includes('用户') ||
                  errorMsg === '失败')
              ) {
                logger.warn('获取用户信息失败，跳转到登录页', err);
                uni.removeStorageSync('uniapp_token');
                uni.removeStorageSync('uniapp_refresh_token');
                uni.removeStorageSync('userInfo');
                if (uni.$ws) {
                  uni.$ws.completeClose();
                }
                uni.showToast({
                  title: '登录已过期，请重新登录',
                  icon: 'none',
                  duration: 2000
                });
                setTimeout(() => {
                  uni.reLaunch({
                    url: '/pkg-auth/pages/login/login'
                  });
                }, 1000);
              } else {
                // 其他错误使用统一错误处理
                errorHandler.handle(err, '获取用户信息');
              }
            });
        } else {
          // Token验证失败，清除登录信息并跳转到登录页
          logger.warn('Token验证失败，跳转到登录页', res);
          uni.removeStorageSync('uniapp_token');
          uni.removeStorageSync('uniapp_refresh_token');
          uni.removeStorageSync('userInfo');
          if (uni.$ws) {
            uni.$ws.completeClose();
          }
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none',
            duration: 2000
          });
          setTimeout(() => {
            uni.reLaunch({
              url: '/pkg-auth/pages/login/login'
            });
          }, 1000);
        }
      })
      .catch(err => {
        // Token验证请求失败，清除登录信息并跳转到登录页
        logger.error('Token验证请求失败，跳转到登录页', err);
        uni.removeStorageSync('uniapp_token');
        uni.removeStorageSync('uniapp_refresh_token');
        uni.removeStorageSync('userInfo');
        if (uni.$ws) {
          uni.$ws.completeClose();
        }
        uni.showToast({
          title: '登录已过期，请重新登录',
          icon: 'none',
          duration: 2000
        });
        setTimeout(() => {
          uni.reLaunch({
            url: '/pkg-auth/pages/login/login'
          });
        }, 1000);
      });
  },
  onShow: function () {
    logger.info('App Show');
    this.$ws.setCornerMark();
    // 全局监听新消息，更新底部消息徽标
    uni.$off('im:updateTabBadge');
    uni.$on('im:updateTabBadge', count => {
      try {
        // 在各页面都可能存在的 tabbar ref 不可直接访问，改为广播给当前页
        uni.$emit('im:setTabBadge', count);
      } catch (e) {}
    });
    // 统一处理请求刷新徽标事件：当有新消息/聚合时，在任意页面触发一次从消息列表拉取总未读并广播
    uni.$off('im:requestRefreshBadge');
    uni.$on('im:requestRefreshBadge', async () => {
      try {
        const apis = await import('./apis/mesasage_apis.js');
        const [resList, resCount] = await Promise.all([
          apis.getChatUserList(),
          apis.getCountMessage()
        ]);
        const list = (resList && resList.data) || [];
        const chatUnread = list.reduce((sum, it) => sum + (parseInt(it.count || 0, 10) || 0), 0);
        const meta = (resCount && resCount.data) || {};
        const likeOrCollectionCount = parseInt(meta.likeOrCollectionCount || 0, 10) || 0;
        const commentCount = parseInt(meta.commentCount || 0, 10) || 0;
        const followCount = parseInt(meta.followCount || 0, 10) || 0;
        const totalUnread = chatUnread + likeOrCollectionCount + commentCount + followCount;
        uni.setStorageSync('im_total_unread', totalUnread);
        uni.$emit('im:setTabBadge', totalUnread);
      } catch (e) {}
    });
    // 刷三模块元数据（点赞收藏/评论/关注）并广播给页面
    uni.$off('im:requestRefreshMeta');
    uni.$on('im:requestRefreshMeta', async () => {
      try {
        const apis = await import('./apis/mesasage_apis.js');
        const resCount = await apis.getCountMessage();
        const meta = (resCount && resCount.data) || {};
        uni.$emit('im:setMetaCounts', {
          likeOrCollectionCount: parseInt(meta.likeOrCollectionCount || 0, 10) || 0,
          commentCount: parseInt(meta.commentCount || 0, 10) || 0,
          followCount: parseInt(meta.followCount || 0, 10) || 0
        });
        // 刷新总徽标
        uni.$emit('im:requestRefreshBadge');
      } catch (e) {}
    });
    uni.onNetworkStatusChange(res => {
      if (res.isConnected) {
        uni.showToast({
          title: '网络已恢复',
          icon: 'none',
          duration: 1000,
          mask: true
        });
        if (this.$ws.socketTask == null && uni.getStorageSync('userInfo') != null) {
          this.$ws.init();
        }
      } else {
        uni.showToast({
          title: '网络已断开',
          icon: 'none',
          duration: 1000,
          mask: true
        });
        this.$ws.completeClose();
      }
    });
  },
  onHide: function () {
    logger.info('App Hide');
  }
};
</script>
<style lang="scss">
/* 注意要写在第一行，同时给style标签加入lang="scss"属性 */
@import '@/uni_modules/uview-ui/index.scss';

/* 全局优化"到底了"提示样式 */
.u-loadmore__nomore-text,
.u-loadmore-text {
  font-size: 20rpx !important;
  color: #d8d8d8 !important;
  font-weight: normal !important;
}

.u-loadmore__nomore-line {
  border-color: #ececec !important;
}
</style>