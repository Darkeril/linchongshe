<template>
	<view class="category-container">
		<view v-for="rowIndex in Math.ceil(iconList.length / 3)" :key="rowIndex" class="category-row">
			<view
				v-for="colIndex in 3"
				:key="colIndex"
				class="category-card"
				v-if="(rowIndex - 1) * 3 + colIndex - 1 < iconList.length"
				@click="goToCategoryDetail(iconList[(rowIndex - 1) * 3 + colIndex - 1].id)"
			>
				<u-icon
					:name="iconList[(rowIndex - 1) * 3 + colIndex - 1].icon"
					size="50"
					color="#4e83fd"
				></u-icon>
				<text class="category-name">{{ iconList[(rowIndex - 1) * 3 + colIndex - 1].categoryName }}</text>
			</view>
		</view>
	</view>
</template>

<script>
import { getNotesCategoryList } from '@/apis/notes_service.js'
export default {
	data() {
		return {
			iconList: []
		}
	},
	methods: {
		goToCategoryDetail(id) {
			uni.navigateTo({
				url: `/pkg-others/pkg-main/pages/category/categoryDetail?id=${id}&categoryName=${this.iconList.find(item => item.id === id).categoryName}`
			})
		}
	},
	onLoad() {
		getNotesCategoryList().then(res => {
			const iconMap = {
				"头像": "account",
				"壁纸": "photo",
				"风景": "map",
				"动漫": "star",
				"美食": "order",
				"艺术": "star",
				"影视": "play-right-fill",
				"动物": "star",
				"绘画": "edit-pen",
				"美女": "woman"
			};
			this.iconList = res.data.map(item => ({
				id: item.id,
				icon: iconMap[item.title] || "star",
				categoryName: item.title
			}));
		})
	}
}
</script>

<style lang="scss">
.category-container {
	padding: 30rpx 20rpx;
	background: #f5f6fa;
}
.category-row {
	display: flex;
	justify-content: space-between;
	margin-bottom: 40rpx;
}
.category-card {
	width: 32%;
	background: #fff;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx 0 rgba(78,131,253,0.08);
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 40rpx 0 30rpx 0;
	transition: transform 0.1s;
	cursor: pointer;
	&:active {
		transform: scale(0.96);
		box-shadow: 0 2rpx 8rpx 0 rgba(78,131,253,0.12);
	}
}
.category-name {
	margin-top: 24rpx;
	font-size: 30rpx;
	color: #333;
	font-weight: 500;
	letter-spacing: 2rpx;
}
</style>