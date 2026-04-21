<template>
	<u-popup :show="show" mode="bottom" :overlayStyle="{'touch-action':'none'}" @close="handleClose">
		<view :style="{height: screenHeight*2+'px'}" style="background-color: #f3f3f2;">
			<view :style="{height: statusBarHeight+'px'}"></view>
			<view
				style="display: flex;justify-content: space-between;height: 50rpx;padding: 20rpx;margin-bottom: 45rpx;">
				<view style="color: #727171;" @click="handleClose">取消</view>
				<view style="31rpx">编辑简介</view>
				<view v-if="selfIntroduction==''" style="color: #f09199;" @click="handleSave">保存</view>
				<view v-else style="color: #d9333f;" @click="handleSave">保存</view>
			</view>
			<text style="margin-left: 50rpx;color: #727171;font-size: 30rpx;">可以自定义简介哦</text>
			<view style="margin: 30rpx;background-color: #ffffff;border-radius: 30rpx;padding: 35rpx;">
				<u--textarea v-model="selfIntroduction" count maxlength="100" placeholder="点击填写兴趣爱好,生活方式等个人简介"
					height="200rpx" border="none" placeholderStyle="color: #727171"
					style="color: #000000;"></u--textarea>
			</view>
		</view>
	</u-popup>
</template>

<script>
	export default {
		name: 'MineEditIntroductionPopup',
		props: {
			show: {
				type: Boolean,
				default: false
			},
			screenHeight: {
				type: [Number, String],
				default: 0
			},
			statusBarHeight: {
				type: [Number, String],
				default: 0
			},
			value: {
				type: String,
				default: ''
			}
		},
		data() {
			return {
				selfIntroduction: ''
			}
		},
		watch: {
			value: {
				immediate: true,
				handler(newVal) {
					this.selfIntroduction = newVal || '';
				}
			}
		},
		methods: {
			handleClose() {
				this.$emit('close');
			},
			handleSave() {
				this.$emit('save', this.selfIntroduction);
			}
		}
	}
</script>

<style lang="scss" scoped>
</style>






















