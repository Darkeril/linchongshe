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
			<view class="header-title">评论</view>
			<view class="header-right"></view>
		</view>
		<!-- #endif -->
		<!-- 头部占位（微信端不预留） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		<view style="padding: 30rpx; margin-top: -30rpx;">
			<block v-if="commentList.length > 0">
				<block v-for="(item,index) in commentList" :key="index">
					<view style="display: flex;padding: 20rpx 0;">
						<!-- 左侧头像 -->
						<image :src="item.avatarUrl" style="height: 80rpx;width: 80rpx;border-radius: 50%;"
							@click="goToOtherMine(item.userId)" mode="aspectFill">
						</image>

						<!-- 右侧内容区 -->
						<view style="flex: 1;margin-left: 20rpx;">
							<!-- 用户名 -->
							<view style="font-size: 28rpx;color: #333;">
								{{item.userName}}
							</view>

							<!-- 评论类型和时间 -->
							<view style="display: flex;align-items: center;margin-top: 4rpx;">
								<view style="font-size: 26rpx;color: #999;display: flex;align-items: center;">
									评论了你的
									<view
										:class="['type-tag', item.commentType === 'product' ? 'product-tag' : 'note-tag']">
										{{item.commentType === 'product' ? '商品' : '笔记'}}
									</view>
								</view>
								<text style="font-size: 26rpx;color: #999;margin-left: auto;">
									{{timeFormat(item.time)}}
								</text>
							</view>

							<!-- 评论内容 -->
							<view style="font-size: 30rpx;color: #333;margin-top: 20rpx;">
								{{item.content}}
							</view>

							<!-- 回复按钮 -->
							<view style="margin-top: 20rpx;">
								<view class="reply-btn" @click="onReply(item)">
									<text style="color: #999;">回复评论</text>
								</view>
							</view>
						</view>

						<!-- 右侧图片 -->
						<image :src="item.notesCoverPicture"
							style="height: 120rpx;width: 120rpx;border-radius: 8rpx;margin-left: 20rpx;"
							@click="goToNotesDetail(item.notesId, item.notesType)" mode="aspectFill">
						</image>
					</view>
					<!-- 分割线 -->
					<view style="height: 1rpx;background-color: #f5f5f5;margin: 10rpx 0;"></view>
				</block>
			</block>

			<!-- 无数据时显示提示 -->
			<view v-else-if="!isLoading" style="text-align: center;color: #999;padding: 30rpx;">
				暂无评论
			</view>

			<!-- 加载更多 -->
			<view style="margin-top: 30rpx;" v-if="commentList.length > 0">
				<u-loadmore :status="status" loadingIcon="spinner" line></u-loadmore>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		getNoticeComment
	} from '@/apis/mesasage_apis.js'
	import weixinNavBar from '@/utils/weixinNavBar.js'

	export default {
		mixins: [weixinNavBar],
		data() {
			return {
				statusBarHeight: 0,
				commentList: [],
				page: 1,
				pageSize: 10,
				status: "loading",
				isLoading: false
			}
		},
		methods: {
			goBack() {
				uni.navigateBack()
			},
			timeFormat(time) {
				/// 两个月内为 '01-03'的形式
				/// 两个月前为 '2020-01-03'的形式
				const currentDate = new Date();
				const inputDate = new Date(time);

				// 计算时间差（毫秒）
				const timeDiff = currentDate - inputDate;

				// 两个月的毫秒数
				const twoMonthsInMillis = 2 * 30 * 24 * 60 * 60 * 1000;

				if (timeDiff < twoMonthsInMillis) {
					// 两个月内
					const month = (inputDate.getMonth() + 1).toString().padStart(2, '0');
					const day = inputDate.getDate().toString().padStart(2, '0');
					return `${month}-${day}`;
				} else {
					// 两个月前
					const year = inputDate.getFullYear();
					const month = (inputDate.getMonth() + 1).toString().padStart(2, '0');
					const day = inputDate.getDate().toString().padStart(2, '0');
					return `${year}-${month}-${day}`;
				}
			},

			goToOtherMine(userId) {
				const currentUserId = uni.getStorageSync('userInfo').id;
				// 如果点击的是当前登录用户的头像，跳转到mine页面
				if (userId == currentUserId) {
					uni.navigateTo({
						url: '/pkg-main/pages/mine/mine'
					})
				} else {
					// 否则跳转到otherMine页面（分包路径）
					uni.navigateTo({
						url: `/pkg-mine/pages/mine/otherMine?userId=${userId}`
					})
				}
			},

			goToNotesDetail(notesId, notesType) {
				let url = ''
				if (notesType === 'product') {
					url = `/pkg-detail/pages/idleDetail/idleDetail?productId=${notesId}`
				} else {
					url = notesType === '1' ?
						`/pkg-detail/pages/notesDetail/noteVideoD?notesId=${notesId}` :
						`/pkg-detail/pages/notesDetail/notesDetail?notesId=${notesId}`
				}
				uni.navigateTo({
					url
				})
			},

			onReply(item) {
				const url = item.commentType === 'product' ?
					`/pkg-detail/pages/idleDetail/idleDetail?productId=${item.notesId}&focus=true&replyTo=${item.userName}` :
					`/pkg-detail/pages/notesDetail/notesDetail?notesId=${item.notesId}&focus=true&replyTo=${item.userName}`
				uni.navigateTo({
					url
				})
			},

			async getCommentList() {
				if (this.isLoading || this.status === "nomore") return

				this.isLoading = true
				try {
					const res = await getNoticeComment({
						page: this.page,
						pageSize: this.pageSize
					})

					if (res.code === 200) {
						const list = res.data.records.map(item => ({
							id: item.id,
							userId: item.uid,
							userName: item.username,
							avatarUrl: item.avatar,
							content: item.content,
							time: item.time,
							notesId: item.commentType === 'product' ? item.productId : item.nid,
							notesCoverPicture: item.commentType === 'product' ? item.productCover : item
								.noteCover,
							notesType: item.commentType === 'product' ? 'product' : '0',
							commentType: item.commentType
						}))

						this.commentList = this.page === 1 ?
							list : [...this.commentList, ...list]

						if (list.length === 0 || res.data.current >= res.data.pages) {
							this.status = "nomore"
						} else {
							this.status = "loading"
							this.page++
						}
					} else {
						throw new Error(res.message || '请求失败')
					}
				} catch (e) {
					console.error('获取评论列表失败', e)
					uni.showToast({
						title: '加载失败',
						icon: 'none'
					})
				} finally {
					this.isLoading = false
				}
			},

			resetList() {
				this.page = 1
				this.commentList = []
				this.status = "loading"
				this.isLoading = false
			}
		},

		onLoad() {
			uni.getSystemInfo({
				success: (res) => {
					this.applyWeixinNavBar(res, 44)
				}
			})
			this.getCommentList()
		},

		onReachBottom() {
			if (!this.isLoading && this.status !== "nomore") {
				this.getCommentList()
			}
		},

		async onPullDownRefresh() {
			try {
				this.resetList()
				await this.getCommentList()
			} catch (e) {
				console.error('下拉刷新失败', e)
				uni.showToast({
					title: '刷新失败',
					icon: 'none'
				})
			} finally {
				uni.stopPullDownRefresh()
			}
		}
	}
</script>

<style lang="scss">
	.reply-btn {
		display: inline-block;
		padding: 4rpx 20rpx;
		background-color: #f5f5f5;
		border-radius: 30rpx;
		font-size: 24rpx;
	}

	.type-tag {
		display: inline-block;
		padding: 2rpx 12rpx;
		border-radius: 30rpx;
		font-size: 24rpx;
		margin-left: 6rpx;
	}

	.note-tag {
		color: #007AFF;
		background-color: rgba(0, 122, 255, 0.1);
	}

	.product-tag {
		color: #4fc08d;
		background-color: rgba(79, 192, 141, 0.1);
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

