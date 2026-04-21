/**
 * 市集页面 - 收藏相关 Mixin
 * 负责收藏功能的处理
 */
import { praiseOrCancelIdles } from '@/apis/idles_service.js'

export default {
	methods: {
		/**
		 * 处理收藏操作
		 * @param {Object} item - 商品对象
		 * @param {Number} index - 索引
		 * @param {String} side - 列类型（left/right）
		 */
		handleCollection(item, index, side) {
			// 组件传递的参数是 item, index, side，直接调用原有的收藏方法
			// 注意：这里不需要处理事件对象，组件内部已经使用了 @click.stop
			if (!item || !item.id) {
				console.warn('⚠️ handleCollection: 无效的 item 参数', item);
				return;
			}
			this.praiseUserIdles(item.id, item.belongUserId || item.uid, index, side);
		},

		/**
		 * 收藏/取消收藏商品
		 * @param {String|Number} id - 商品ID
		 * @param {String|Number} targetUserId - 目标用户ID
		 * @param {Number} index - 索引
		 * @param {String} colType - 列类型（left/right）
		 */
		praiseUserIdles(id, targetUserId, index, colType) {
			console.log('⭐ 市集收藏操作:', {
				id,
				targetUserId,
				index,
				colType,
				typeTabIndex: this.typeTabIndex
			});

			praiseOrCancelIdles({
				notesId: id,
				userId: uni.getStorageSync('userInfo').id,
				targetUserId: targetUserId
			}).then(res => {
				console.log('⭐ 市集收藏响应:', res);
				if (res.code == 200) {
					// 更新分类数据
					this.updateCategoryDataCollectionStatus(id, index, colType);
				}
			}).catch(error => {
				console.error('⭐ 市集收藏失败:', error);
				uni.showToast({
					title: '收藏失败',
					icon: 'none'
				});
			});
		},

		/**
		 * 更新分类数据的收藏状态
		 * @param {String|Number} id - 商品ID
		 * @param {Number} index - 索引
		 * @param {String} colType - 列类型（left/right）
		 */
		updateCategoryDataCollectionStatus(id, index, colType) {
			const categoryKey = `category_${this.typeTabIndex}`;
			const categoryData = this.categoryData[categoryKey];

			if (!categoryData) {
				console.warn('⚠️ 市集分类数据不存在:', categoryKey);
				return;
			}

			// 优先使用统一的 list 数据结构
			if (categoryData.list && categoryData.list.length > 0) {
				const itemIndex = categoryData.list.findIndex(item => item.id === id);
				if (itemIndex !== -1) {
					const item = categoryData.list[itemIndex];
					const oldIsCollection = item.isCollection;
					const oldNotesLikeNum = item.notesLikeNum || 0;

					// 使用 $set 确保响应式更新
					if (oldIsCollection) {
						this.$set(categoryData.list[itemIndex], 'notesLikeNum', Math.max(0, oldNotesLikeNum - 1));
						this.$set(categoryData.list[itemIndex], 'isCollection', false);
					} else {
						this.$set(categoryData.list[itemIndex], 'notesLikeNum', oldNotesLikeNum + 1);
						this.$set(categoryData.list[itemIndex], 'isCollection', true);
					}

					console.log('✅ 市集收藏状态已更新 (list):', categoryData.list[itemIndex]);

					// 同时更新 leftList 和 rightList（如果存在）以保持一致性
					if (categoryData.leftList) {
						const leftItemIndex = categoryData.leftList.findIndex(item => item.id === id);
						if (leftItemIndex !== -1) {
							this.$set(categoryData.leftList[leftItemIndex], 'isCollection', categoryData.list[itemIndex].isCollection);
							this.$set(categoryData.leftList[leftItemIndex], 'notesLikeNum', categoryData.list[itemIndex].notesLikeNum);
						}
					}
					if (categoryData.rightList) {
						const rightItemIndex = categoryData.rightList.findIndex(item => item.id === id);
						if (rightItemIndex !== -1) {
							this.$set(categoryData.rightList[rightItemIndex], 'isCollection', categoryData.list[itemIndex].isCollection);
							this.$set(categoryData.rightList[rightItemIndex], 'notesLikeNum', categoryData.list[itemIndex].notesLikeNum);
						}
					}

					// 通过 ref 直接更新组件内部的列表
					this.$nextTick(() => {
						const refName = `idleWaterfall_${this.typeTabIndex}`;
						let component = this.$refs[refName];

						// v-for 中的 ref 可能是数组，取对应索引的元素
						if (Array.isArray(component)) {
							component = component[this.typeTabIndex] || component[0];
						}

						if (component && typeof component.updateCollectionStatus === 'function') {
							console.log('🔄 调用组件更新方法:', {
								id,
								isCollection: categoryData.list[itemIndex].isCollection,
								notesLikeNum: categoryData.list[itemIndex].notesLikeNum,
								refName
							});
							component.updateCollectionStatus(
								id,
								categoryData.list[itemIndex].isCollection,
								categoryData.list[itemIndex].notesLikeNum
							);
						} else {
							console.warn('⚠️ 组件或方法不存在:', {
								refName,
								component: !!component,
								hasMethod: component && typeof component.updateCollectionStatus === 'function'
							});
						}
					});

					// 强制更新组件（确保UI刷新）
					this.$forceUpdate();
					return;
				}
			}

			// 兼容旧数据结构：根据列类型更新对应的数据
			let updatedItem = null;
			if (colType === 'left') {
				// 左列
				if (categoryData.leftList && categoryData.leftList[index]) {
					const item = categoryData.leftList[index];
					const oldIsCollection = item.isCollection;
					const oldNotesLikeNum = item.notesLikeNum || 0;

					if (oldIsCollection) {
						this.$set(categoryData.leftList[index], 'notesLikeNum', Math.max(0, oldNotesLikeNum - 1));
						this.$set(categoryData.leftList[index], 'isCollection', false);
					} else {
						this.$set(categoryData.leftList[index], 'notesLikeNum', oldNotesLikeNum + 1);
						this.$set(categoryData.leftList[index], 'isCollection', true);
					}
					updatedItem = categoryData.leftList[index];
					console.log('✅ 市集左列收藏状态已更新:', updatedItem);
				}
			} else if (colType === 'right') {
				// 右列
				if (categoryData.rightList && categoryData.rightList[index]) {
					const item = categoryData.rightList[index];
					const oldIsCollection = item.isCollection;
					const oldNotesLikeNum = item.notesLikeNum || 0;

					if (oldIsCollection) {
						this.$set(categoryData.rightList[index], 'notesLikeNum', Math.max(0, oldNotesLikeNum - 1));
						this.$set(categoryData.rightList[index], 'isCollection', false);
					} else {
						this.$set(categoryData.rightList[index], 'notesLikeNum', oldNotesLikeNum + 1);
						this.$set(categoryData.rightList[index], 'isCollection', true);
					}
					updatedItem = categoryData.rightList[index];
					console.log('✅ 市集右列收藏状态已更新:', updatedItem);
				}
			}

			// 强制更新组件（确保UI刷新）
			if (updatedItem) {
				this.$forceUpdate();
			}
		},
	}
}

