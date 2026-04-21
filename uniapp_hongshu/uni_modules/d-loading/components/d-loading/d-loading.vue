<template>
	<view class="dengLoadingVue">
		<view class="maskVueHiddenShow" v-if="statusLoading" @click="onCheckHiddenApi"
			@touchmove.stop.prevent="moveHandle">
			<view class="maskVueHiddenShowWrapper">
				<view class="loading-spinner">
					<view class="loading-spinner-circle"></view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		props: {
			status: false,
		},
		created() {
			if (this.statusLoading == true) {
				var that = this
				setTimeout(function() {
					that.statusLoading = false
				}, 1500)
			}
		},
		data() {
			return {
				statusLoading: true,
			};
		},
		watch: {
			status(e) {
				this.statusLoading = e

			}
		},
		methods: {
			moveHandle() {},
			onCheckHiddenApi() {},
			reset() {
				this.statusLoading = true
				var that = this
				setTimeout(function() {
					that.statusLoading = false
				}, 1500)
			}
		}
	}
</script>

<style lang="scss">
	.dengLoadingVue {
		.maskVueHiddenShow {
			z-index: 1000;
			position: fixed;
			top: 0;
			left: 0;
			right: 0;
			bottom: 0;
			background-color: #ffffff;
			opacity: 0.6;
			//
			display: flex;
			align-items: center;
			justify-content: center;

			.maskVueHiddenShowWrapper {
				width: 80%;
				height: 300rpx;
				display: flex;
				align-items: center;
				justify-content: center;

				.maskVueHiddenShowWrapperGif {
					width: 400rpx;
				}

				/* CSS Spinner to replace heavy GIF */
				.loading-spinner {
					width: 120rpx;
					height: 120rpx;
					display: flex;
					align-items: center;
					justify-content: center;
				}

				.loading-spinner-circle {
					width: 100rpx;
					height: 100rpx;
					border: 8rpx solid rgba(0, 0, 0, 0.1);
					border-top-color: #333333;
					border-radius: 50%;
					animation: loading-spin 1s linear infinite;
				}

				@keyframes loading-spin {
					0% { transform: rotate(0deg); }
					100% { transform: rotate(360deg); }
				}
			}
		}
	}
</style>