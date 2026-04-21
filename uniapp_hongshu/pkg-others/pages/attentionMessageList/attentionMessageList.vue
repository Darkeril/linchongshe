<template>
		<view>
			<!-- 状态栏背景（微信端由系统栏「爱宠社」占位） -->
			<!-- #ifndef MP-WEIXIN -->
			<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
			<!-- #endif -->
			<!-- 头部导航（H5 显示标题和返回按钮，微信端由系统栏展示） -->
			<!-- #ifndef MP-WEIXIN -->
			<view class="header" :style="{top: navBarTop, position: 'fixed', width: '100%', zIndex: 9999}">
				<view class="header-left" @click="goBack">
					<u-icon name="arrow-left" size="20" color="#333"></u-icon>
				</view>
				<view class="header-title">新增关注</view>
				<view class="header-right"></view>
			</view>
			<!-- #endif -->
			<!-- 头部占位（微信端不预留） -->
			<!-- #ifndef MP-WEIXIN -->
			<view :style="{height: navPlaceholderHeight}"></view>
			<!-- #endif -->
			<view style="padding: 30rpx;">
				<!-- 有数据时显示列表 -->
				<block v-if="attentionMessageList.length > 0">
					<!-- 显示的数据：如果未展开则只显示前7条，展开后显示全部 -->
					<block v-for="(item,index) in displayedList" :key="index">
						<view style="display: flex;padding: 10rpx 0;align-items: center;">
							<image :src="item.avatar" 
	                            style="height: 90rpx;width: 90rpx;border-radius: 50%;"
								@click="goToOtherMine(item.uid)" 
	                            mode="aspectFill" />
							<view style="display: flex;flex-direction: column;justify-content: space-between;margin-left: 20rpx;flex: 1;"
								@click="goToOtherMine(item.uid)">
								<view style="font-size: 30rpx;color: #2b2b2b;">{{item.username}}</view>
								<view style="font-size: 24rpx;color: #afafb0;margin-top: 8rpx;">
									{{item.content || '开始关注你了'}}
									<text style="margin-left: 60rpx;font-size: 22rpx;">{{formatTime(item.createTime || item.time)}}</text>
								</view>
							</view>
							<u-tag style="margin-left: auto;" 
	                            text="去私信" 
	                            plain 
	                            shape="circle" 
	                            borderColor="#3d8af5"
								color="#3d8af5" 
	                            @click="goToChat(item)" />
						</view>
						<u-divider></u-divider>
					</block>
					
					<!-- 查看全部按钮 -->
					<view v-if="!isExpanded && attentionMessageList.length > 7" 
						style="text-align: center; padding: 20rpx; color: #999; font-size: 26rpx; display: flex; align-items: center; justify-content: center;"
						@click="expandList">
						<text>查看全部</text>
						<u-icon name="arrow-down" color="#999" size="14" style="margin-left: 8rpx;"></u-icon>
					</view>
				</block>

			<!-- 无数据时显示提示 -->
			<view v-else-if="!isLoading" style="text-align: center;color: #999;padding: 30rpx;">
				暂无新增关注
			</view>

			<!-- 加载更多 -->
			<!-- <view style="margin-top: 70rpx;">
				<u-loadmore :status="status" loadingIcon="spinner" line></u-loadmore>
			</view> -->
			
			<!-- 发现好友模块 -->
			<view class="discover-section" v-if="!hideDiscover && recommendUsers.length">
				<view class="discover-header">
					<view class="discover-title">
						发现好友
						<u-icon class="discover-tip-icon" name="question-circle" color="#999" size="18"
							@click="openDiscoverTip"></u-icon>
					</view>
					<view class="discover-close" @click="hideDiscover = true">关闭</view>
				</view>
				<view class="discover-item" v-for="(user, index) in recommendUsers" :key="getUserKey(user, index)">
					<image 
						class="discover-avatar" 
						mode="aspectFill" 
						:src="getUserAvatar(user)" 
						@click="goToUserProfile(user, index)"
					></image>
					<view class="discover-info">
						<view class="discover-name">{{ getUserName(user) }}</view>
						<view class="discover-meta">
							<text class="discover-badge">粉丝 {{ getUserFans(user) }}</text>
							<text class="discover-desc" v-if="user && user.likeCount">被点赞超过{{ user.likeCount }}次</text>
						</view>
					</view>
					<view>
						<u-button 
							type="primary" 
							size="small" 
							shape="circle" 
							v-if="!getUserFollowed(user)" 
							@click="followUser(user, index)"
							text="关注"
						></u-button>
						<u-button 
							type="info" 
							size="small" 
							shape="circle" 
							v-else 
							text="已关注"
							disabled
						></u-button>
					</view>
				</view>
				<view class="discover-loadmore" v-if="recommendUsers.length >= recommendSize" @click="loadMoreRecommend">
					<text>查看更多推荐</text>
					<u-icon name="arrow-right" color="#999" size="14"></u-icon>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getNoticeAttention } from '@/apis/mesasage_apis.js'
import weixinNavBar from '@/utils/weixinNavBar.js'

export default {
	mixins: [weixinNavBar],
    data() {
        return {
            statusBarHeight: 0,
            attentionMessageList: [],
            page: 1,
            pageSize: 10,
            status: "loadmore",
            isLoading: false,
            // 发现好友相关数据
            recommendUsers: [],
            recommendPage: 1,
            recommendSize: 5,
            hideDiscover: false,
            // 折叠显示相关数据
            isExpanded: false,
            maxDisplayCount: 7
        }
    },
    computed: {
        // 计算要显示的数据列表
        displayedList() {
            if (this.isExpanded || this.attentionMessageList.length <= this.maxDisplayCount) {
                return this.attentionMessageList
            }
            return this.attentionMessageList.slice(0, this.maxDisplayCount)
        }
    },
    methods: {
		goBack() {
			uni.navigateBack()
		},
        // 格式化时间显示
        formatTime(time) {
            if (!time) return ''
            
            const now = new Date()
            const targetTime = new Date(time)
            const diff = now - targetTime
            
            // 小于1分钟
            if (diff < 60 * 1000) {
                return '刚刚'
            }
            
            // 小于1小时
            if (diff < 60 * 60 * 1000) {
                const minutes = Math.floor(diff / (60 * 1000))
                return `${minutes}分钟前`
            }
            
            // 小于24小时
            if (diff < 24 * 60 * 60 * 1000) {
                const hours = Math.floor(diff / (60 * 60 * 1000))
                return `${hours}小时前`
            }
            
            // 小于7天
            if (diff < 7 * 24 * 60 * 60 * 1000) {
                const days = Math.floor(diff / (24 * 60 * 60 * 1000))
                return `${days}天前`
            }
            
            // 超过7天，显示具体日期
            const month = targetTime.getMonth() + 1
            const day = targetTime.getDate()
            const year = targetTime.getFullYear()
            
            // 如果是今年，只显示月-日
            if (year === now.getFullYear()) {
                return `${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`
            }
            
            // 跨年显示完整日期
            return `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`
        },
        
        goToOtherMine(userId) {
            const currentUserId = uni.getStorageSync('userInfo').id;
            // 如果点击的是当前登录用户的头像，跳转到mine页面
            if (userId == currentUserId) {
                uni.navigateTo({
                    url: '/pkg-main/pages/mine/mine'
                })
            } else {
                // 否则跳转到otherMine页面
                uni.navigateTo({
                    url: `/pkg-mine/pages/mine/otherMine?userId=${userId}`
                })
            }
        },
        
        async getAttentionMessageList() {
            if (this.isLoading || this.status === 'nomore') return

            try {
                this.isLoading = true
                this.status = 'loading'

                const res = await getNoticeAttention({
                    page: this.page,
                    pageSize: this.pageSize
                })

                if (res.code === 200) {
                    const list = res.data.records || []

                    // 首次加载或下拉刷新
                    if (this.page === 1) {
                        this.attentionMessageList = list
                    } else {
                        this.attentionMessageList = [...this.attentionMessageList, ...list]
                    }

                    // 更新分页状态
                    if (list.length === 0 || res.data.current >= res.data.pages) {
                        this.status = 'nomore'
                    } else {
                        this.status = 'loadmore'
                        this.page++
                    }
                } else {
                    uni.showToast({
                        title: res.message || '获取数据失败',
                        icon: 'none'
                    })
                }
            } catch (e) {
                console.error('获取关注列表失败:', e)
                uni.showToast({
                    title: '获取数据失败',
                    icon: 'none'
                })
            } finally {
                this.isLoading = false
            }
        },

        goToChat(item) {
            // 确保所有必要参数都存在
            if (!item.uid || !item.username || !item.avatar) {
                uni.showToast({
                    title: '用户信息不完整',
                    icon: 'none'
                })
                return
            }

            uni.navigateTo({
				url: `/pkg-msg/pages/chat/chat?userId=${item.uid}&userName=${item.username}&avatarUrl=${item.avatar}`
            })
        },

        // 展开列表
        expandList() {
            this.isExpanded = true
        },

        // 发现好友相关方法
        getUserKey(user, index) {
            if (!user) return `user_${index}`
            return user.uid || user.userId || user.id || `user_${index}`
        },
        
        getUserAvatar(user) {
            if (!user) return '/static/default.jpg'
            const avatar = user.avatar || user.avatarUrl || ''
            if (!avatar || avatar === 'undefined' || avatar === 'null') {
                return '/static/default.jpg'
            }
            if (avatar.startsWith('http')) {
                return avatar
            } else if (avatar.startsWith('/')) {
                return avatar
            } else {
                return '/static/default.jpg'
            }
        },
        
        getUserName(user) {
            if (!user) return '未知用户'
            return user.username || user.nickname || user.name || '未知用户'
        },
        
        getUserFans(user) {
            if (!user) return 0
            return user.fansNum || user.fansCount || user.followers || 0
        },
        
        getUserFollowed(user) {
            if (!user) return false
            return user.followed || false
        },
        
        async fetchRecommendUsers(reset = false) {
            try {
                const {
                    getRecommendUser
                } = require('@/apis/search_service.js')
                if (reset) this.recommendPage = 1
                const res = await getRecommendUser(this.recommendPage, this.recommendSize)
                console.log('推荐用户API响应:', res)
                
                const rows = res?.data?.records || res?.data || []
                console.log('推荐用户数据:', rows)
                
                // 验证数据完整性
                const validUsers = rows.filter(user => {
                    if (!user || typeof user !== 'object') {
                        console.warn('发现无效用户数据（非对象）:', user)
                        return false
                    }
                    
                    const hasId = user.uid || user.userId || user.id
                    if (!hasId) {
                        console.warn('发现无效用户数据（缺少ID）:', user)
                        return false
                    }
                    
                    const avatar = user.avatar || user.avatarUrl
                    if (avatar && (avatar === 'undefined' || avatar === 'null')) {
                        console.warn('发现无效用户数据（头像字段为undefined/null）:', user)
                        user.avatar = ''
                        user.avatarUrl = ''
                    }
                    return true
                })
                
                console.log('有效用户数据:', validUsers.length, '条')
                
                if (reset) {
                    this.recommendUsers = validUsers
                } else {
                    this.recommendUsers = [...this.recommendUsers, ...validUsers]
                }
                
                console.log('当前推荐用户总数:', this.recommendUsers.length)
            } catch (error) {
                console.error('获取推荐用户失败:', error)
            }
        },
        
        loadMoreRecommend() {
            this.recommendPage += 1
            this.fetchRecommendUsers()
        },
        
        async followUser(user, index) {
            console.log('followUser 接收到的用户数据:', user)
            console.log('用户索引:', index)
            
            if (!user) {
                console.error('用户数据为空')
                if (index !== undefined && this.recommendUsers[index]) {
                    console.log('从列表中获取到用户数据:', this.recommendUsers[index])
                    user = this.recommendUsers[index]
                } else {
                    uni.showToast({ title: '用户信息获取失败', icon: 'none' })
                    return
                }
            }
            
            const uid = user.uid || user.userId || user.id
            if (!uid) {
                console.error('用户ID为空')
                uni.showToast({ title: '用户信息不完整', icon: 'none' })
                return
            }
            
            try {
                const { updateAttention } = require('@/apis/user_service.js')
                const res = await updateAttention({ targetUserId: uid })
                
                if (res && res.code === 200) {
                    // 更新本地状态
                    if (index !== undefined && this.recommendUsers[index]) {
                        this.$set(this.recommendUsers[index], 'followed', true)
                    }
                    
                    uni.showToast({
                        title: '关注成功',
                        icon: 'success'
                    })
                } else {
                    uni.showToast({
                        title: res?.msg || '关注失败',
                        icon: 'none'
                    })
                }
            } catch (error) {
                console.error('关注用户失败:', error)
                uni.showToast({
                    title: '网络错误，请重试',
                    icon: 'none'
                })
            }
        },
        
        goToUserProfile(user, index) {
            console.log('goToUserProfile 接收到的用户数据:', user)
            console.log('用户索引:', index)
            
            if (!user) {
                console.error('用户数据为空')
                if (index !== undefined && this.recommendUsers[index]) {
                    console.log('从列表中获取到用户数据:', this.recommendUsers[index])
                    user = this.recommendUsers[index]
                } else {
                    uni.showToast({ title: '用户信息获取失败', icon: 'none' })
                    return
                }
            }
            
            const uid = user.uid || user.userId || user.id
            if (!uid) {
                console.error('用户ID为空')
                uni.showToast({ title: '用户信息不完整', icon: 'none' })
                return
            }
            
            console.log('准备跳转到用户主页，用户ID:', uid)
            
            uni.navigateTo({
                url: `/pkg-mine/pages/mine/otherMine?userId=${uid}`,
                success: () => {
                    console.log('成功跳转到用户主页')
                },
                fail: (error) => {
                    console.error('跳转用户主页失败:', error)
                    // 平台特定的错误处理
                    if (process.env.UNI_PLATFORM === 'mp-weixin') {
                        uni.showModal({
                            title: '提示',
                            content: '跳转失败，请重试',
                            showCancel: false
                        })
                    } else {
                        uni.showToast({
                            title: '跳转失败',
                            icon: 'none'
                        })
                    }
                }
            })
        },
        
        openDiscoverTip() {
            uni.showModal({
                title: '发现好友',
                content: '系统会根据你的兴趣和活跃度，为你推荐可能感兴趣的用户',
                showCancel: false
            })
        },

        resetData() {
            this.page = 1
            this.attentionMessageList = []
            this.status = 'loadmore'
            this.isLoading = false
            this.isExpanded = false  // 重置展开状态
        }
    },

    onLoad() {
		uni.getSystemInfo({
			success: (res) => {
				this.applyWeixinNavBar(res, 44)
			}
		})
        this.getAttentionMessageList()
        this.fetchRecommendUsers(true)
    },

    onReachBottom() {
        if (!this.isLoading && this.status !== 'nomore') {
            this.getAttentionMessageList()
        }
    },

    async onPullDownRefresh() {
        try {
            this.resetData()
            await this.getAttentionMessageList()
            this.fetchRecommendUsers(true)
        } catch (e) {
            console.error('下拉刷新失败', e)
        } finally {
            uni.stopPullDownRefresh()
        }
    }
}
</script>

<style lang="scss" scoped>
/* 发现好友样式 */
.discover-section {
    // margin: 20rpx;
    // border-top: 1rpx solid #f0f0f0;
    padding-top: 10rpx;
}

.discover-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
}

.discover-title {
    font-size: 32rpx;
    font-weight: 600;
    display: flex;
    align-items: center;
}

.discover-tip-icon {
    margin-left: 10rpx;
}

.discover-close {
    color: #999;
    font-size: 26rpx;
}

.discover-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
}

.discover-item:last-child {
    border-bottom: none;
}

.discover-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    margin-right: 20rpx;
}

.discover-info {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.discover-name {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 8rpx;
}

.discover-meta {
    display: flex;
    flex-direction: column;
    gap: 4rpx;
}

.discover-badge {
    font-size: 24rpx;
    color: #666;
}

.discover-desc {
    font-size: 22rpx;
    color: #999;
}

.discover-loadmore {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx;
    color: #999;
    font-size: 26rpx;
    gap: 8rpx;
}

/* H5 头部导航（与「收到的赞和收藏」一致） */
.header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 44px;
    padding: 0 15px;
    background-color: #fff;
    border-bottom: 1px solid #eee;
}
.header-left {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: flex-start;
}
.header-title {
    flex: 1;
    text-align: center;
    font-size: 18px;
    font-weight: 500;
    color: #333;
}
.header-right {
    width: 40px;
}
</style>