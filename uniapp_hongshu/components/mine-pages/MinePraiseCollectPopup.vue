<template>
	<view @touchmove.stop.prevent="moveHandle">
		<u-popup :show="show" mode="center" @close="handleClose" :round="10">
			<view style="display: flex;flex-direction: column;align-items: center;width: 600rpx;">
				<view
					style="font-size: 40rpx;letter-spacing: 2rpx;padding: 40rpx;text-align: center;border-bottom-style: solid;border-width: 1rpx;border-color: #e7e7eb;">
					获赞与收藏</view>
				<view
					style="padding: 30rpx;display: flex;flex-direction: column;justify-content: space-between;font-size: 30rpx;color: #95949a;">
					<view style="padding: 20rpx;">当前发布笔记数<text style="color: #595857;">{{' '+count.notesCount}}</text>
					</view>
					<view style="padding: 20rpx;">当前获得点赞数<text style="color: #595857;">{{' '+count.praiseCount}}</text>
					</view>
					<view style="padding: 20rpx;">当前获得收藏数<text style="color: #595857;">{{' '+count.collectCount}}</text>
					</view>
				</view>
				<view @click="handleClose"
					style="margin: 20rpx 10rpx 60rpx 10rpx;padding: 15rpx 40rpx;background-color: #3d8af5;color: #ffffff;width: 400rpx;box-sizing: border-box;text-align: center;border-radius: 80rpx;">
					我知道了</view>
			</view>
		</u-popup>
	</view>
</template>

<script>
	export default {
		name: 'MinePraiseCollectPopup',
		props: {
			show: {
				type: Boolean,
				default: false
			},
			count: {
				type: Object,
				default: () => ({
					notesCount: 0,
					praiseCount: 0,
					collectCount: 0
				})
			}
		},
		methods: {
			moveHandle(e) {},
			handleClose() {
				this.$emit('close');
			}
		}
	}
</script>

<style lang="scss" scoped>






















