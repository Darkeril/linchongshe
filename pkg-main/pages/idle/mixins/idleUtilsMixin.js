/**
 * 市集页面 - 工具方法 Mixin
 * 负责数据映射、格式化等工具方法
 */
import { pxToRpx, weChatTimeFormat } from '@/utils/util.js'

export default {
	methods: {
		/**
		 * 格式化地址显示（只显示市+详细地址）
		 * @param {Object} note - 商品对象
		 * @returns {String} 格式化后的地址
		 */
		formatAddress(note) {
			if (!note) return '';
			const parts = [];
			if (note.city) parts.push(note.city);
			if (note.address) parts.push(note.address);
			// 兼容旧数据：如果没有新字段但有location字段
			if (parts.length === 0 && note.location) {
				const location = note.location;
				return location.length <= 12 ? location : location.substring(0, 12) + '...';
			}
			const fullAddress = parts.join(' ');
			return fullAddress.length > 12 ? fullAddress.substring(0, 12) + '...' : fullAddress;
		},

		/**
		 * 获取图片高度
		 * @param {String} s - 图片URL
		 * @returns {Promise<Object>} 包含 height 和 path 的对象
		 */
		getImageHeight(s) {
			return new Promise((resolve) => {
				// 检查是否为微信小程序环境
				// #ifdef MP-WEIXIN
				// 在微信小程序中，直接使用默认高度，避免网络请求
				console.warn('⚡ 微信小程序环境，直接使用默认高度，避免网络请求:', s);
				resolve({
					height: 200, // 默认高度
					path: s
				});
				return;
				// #endif

				// #ifndef MP-WEIXIN
				// H5环境：正常获取图片高度
				uni.getImageInfo({
					src: s,
					success: (res) => {
						let imageHeight = pxToRpx(res.height);
						let imageWidth = pxToRpx(res.width);
						const width = 360;
						const maxHeight = 500;
						let height = width * imageHeight / imageWidth;
						if (height > maxHeight) {
							height = maxHeight;
						}
						let obj = {
							height: height,
							path: res.path
						}
						resolve(obj)
					},
					fail: (error) => {
						console.warn('⚠️ getImageInfo 失败，使用默认高度:', error, s);
						// 图片加载失败也要resolve，给个默认高度和原图
						resolve({
							height: 200,
							path: s
						});
					}
				})
				// #endif
			})
		},

		/**
		 * 映射闲置商品数据
		 * @param {Object} item - 原始商品数据
		 * @returns {Object} 映射后的商品数据
		 */
		mapIdleItem(item) {
			if (!item) return null;
			
			// 统一主键字段（兼容不同接口字段命名）
			const itemId = item.id || item.productId || item.noteId;
			
			return {
				...item,
				id: itemId,
				coverPicture: item.cover,
				avatarUrl: item.avatar,
				nickname: item.username,
				productType: parseInt(item.productType, 10) || 0,
				notesLikeNum: parseInt(item.likeCount, 10) || 0,
				notesCollectNum: parseInt(item.collectCount, 10) || 0,
				isCollection: item.isCollection || false,
				isCollect: item.isCollection || false,
				coverHeight: parseInt(item.noteCoverHeight, 10) || 0,
				noteCoverHeight: parseInt(item.noteCoverHeight, 10) || 0,
				content: item.description || '',
				title: item.title,
				updateTime: item.updateTime ? weChatTimeFormat(item.updateTime) : '',
				createTime: item.createTime ? weChatTimeFormat(item.createTime) : '',
				belongUserId: item.uid,
				imgList: item.urls ? JSON.parse(item.urls) : [],
				province: item.province,
				city: item.city,
				address: item.address,
				price: item.price,
				originPrice: item.originalPrice,
				postType: item.postType // 0=邮寄，1=自提
			};
		},
	}
}

