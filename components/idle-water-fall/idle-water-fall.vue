<template>
  <view style="width: 100%; display: flex; flex-wrap: wrap; background: #f5f5f5">
    <view class="water-left">
      <!-- 使用 id + index 组合 key，既保证唯一性，又尽量稳定，避免重复 key 警告 -->
      <block
        v-for="(item, index) in leftList"
        :key="getLeftItemKey(item, index)"
      >
        <view @click="goToDetail(getItemId(item), item.productType)" class="product-card">
          <view class="image-container">
            <u--image
              v-if="item.coverPicture && String(item.coverPicture).trim()"
              :src="item.coverPicture"
              :fade="false"
              lazyLoad
              width="100%"
              height="auto"
              mode="widthFix"
              style="max-height: 500rpx; overflow: hidden"
            >
              <template v-slot:loading>
                <view style="height: 200rpx; text-align: center; padding: 20rpx">
                  <u-loading-icon color="#e83929"></u-loading-icon>
                  <view style="font-size: 30rpx">loading......</view>
                </view>
              </template>
            </u--image>
            <!-- 审核中状态 -->
            <view v-if="item.auditStatus === '0'" class="audit-overlay">
              <view class="audit-eye">
                <u-icon name="eye" color="#ffffff" size="48"></u-icon>
                <view style="color: #ffffff; font-size: 24rpx; margin-top: 8rpx">审核中...</view>
              </view>
            </view>
            <!-- 未通过状态 -->
            <view v-if="item.auditStatus === '2'" class="audit-overlay not-passed">
              <view class="audit-eye">
                <view style="color: #ff2442; font-size: 24rpx">未通过⚠️</view>
                <u-button
                  v-if="item.uid === userId"
                  type="primary"
                  size="mini"
                  @click.stop="editProduct(getItemId(item))"
                  style="margin-top: 10rpx; font-size: 20rpx"
                >
                  重新编辑
                </u-button>
              </view>
            </view>
            <!-- 地址信息 -->
            <view class="look-views" v-if="formatAddress(item)">
              <u-icon name="map" color="#ffffff" size="25rpx"></u-icon>
              <text style="margin-left: 5rpx">{{ formatAddress(item) }}</text>
            </view>
            <!-- 视频标识 -->
            <view v-if="item.productType == 2" class="video-play">
              <image src="@/static/svgs/video.svg" style="width: 45rpx" mode="widthFix"></image>
            </view>
            <!-- Live 图标识 -->
            <view v-if="item.productType == 3" class="live-badge">
              <image src="@/static/svgs/live.svg" style="width: 45rpx" mode="widthFix"></image>
            </view>
          </view>
          <!-- 商品标题 -->
          <view class="title">{{ item.title }}</view>
          <!-- 价格区 -->
          <view class="goods-price-row">
            <text class="goods-price">¥{{ item.price }}</text>
            <text class="goods-origin-price">¥{{ item.originalPrice || item.originPrice }}</text>
            <view v-if="item.postType === 0" class="post-type-tag post-type-shipping">包邮</view>
            <view v-else-if="item.postType === 1" class="post-type-tag post-type-pickup">自提</view>
          </view>
          <!-- 用户信息 -->
          <view style="display: flex; position: relative; padding: 20rpx">
            <image
              v-if="item.avatarUrl && String(item.avatarUrl).trim()"
              style="height: 22px; width: 22px; border-radius: 50%"
              mode="aspectFill"
              :src="item.avatarUrl"
              @click.stop="handleUserClick(item.belongUserId || item.uid)"
            ></image>
            <view
              class="product-username"
              @click.stop="handleUserClick(item.belongUserId || item.uid)"
            >
              {{ item.nickname || '用户' }}
            </view>
            <view style="display: flex; position: absolute; right: 10rpx">
              <!-- 市集页面：显示可点击的收藏按钮 -->
              <template v-if="showCollection">
                <u-transition :show="!item.isCollection" mode="fade" duration="2000">
                  <view
                    @click.stop="handleCollectionClickLeft(item, index)"
                    style="display: inline-block"
                  >
                    <u-icon v-if="!item.isCollection" name="star" size="18" color="#bbb"></u-icon>
                  </view>
                </u-transition>
                <u-transition :show="item.isCollection" mode="fade" duration="2000">
                  <view
                    @click.stop="handleCollectionClickLeft(item, index)"
                    style="display: inline-block"
                  >
                    <u-icon
                      v-if="item.isCollection"
                      name="star-fill"
                      size="18"
                      color="#f7b500"
                    ></u-icon>
                  </view>
                </u-transition>
                <view
                  v-if="item.notesLikeNum > 0"
                  style="color: gray; font-size: 12px; line-height: 18px; margin-left: 3rpx"
                >
                  {{ item.notesLikeNum }}
                </view>
                <view
                  v-else
                  style="color: gray; font-size: 12px; line-height: 18px; margin-left: 3rpx"
                >
                  想要
                </view>
              </template>
              <!-- mine页面：只显示"想要"文本 -->
              <template v-else>
                <u-icon name="star" size="18" color="#bbb"></u-icon>
                <view style="color: gray; font-size: 12px; line-height: 18px; margin-left: 3rpx">
                  想要
                </view>
              </template>
            </view>
          </view>
        </view>
      </block>
    </view>
    <view class="water-right">
      <!-- 使用 id + index 组合 key，既保证唯一性，又尽量稳定，避免重复 key 警告 -->
      <block
        v-for="(item, index) in rightList"
        :key="getRightItemKey(item, index)"
      >
        <view @click="goToDetail(getItemId(item), item.productType)" class="product-card">
          <view class="image-container">
            <u--image
              v-if="item.coverPicture && String(item.coverPicture).trim()"
              :src="item.coverPicture"
              width="100%"
              height="auto"
              mode="widthFix"
              :fade="false"
              lazyLoad
              style="max-height: 500rpx; overflow: hidden"
            >
              <template v-slot:loading>
                <view
                  style="height: 200rpx; text-align: center; padding: 20rpx; margin-bottom: 30rpx"
                >
                  <u-loading-icon color="#e83929"></u-loading-icon>
                  <view style="font-size: 30rpx">loading......</view>
                </view>
              </template>
            </u--image>
            <!-- 审核中状态 -->
            <view v-if="item.auditStatus === '0'" class="audit-overlay">
              <view class="audit-eye">
                <u-icon name="eye" color="#ffffff" size="48"></u-icon>
                <view style="color: #ffffff; font-size: 24rpx; margin-top: 8rpx">审核中...</view>
              </view>
            </view>
            <!-- 未通过状态 -->
            <view v-if="item.auditStatus === '2'" class="audit-overlay not-passed">
              <view class="audit-eye">
                <view style="color: #ff2442; font-size: 24rpx">未通过⚠️</view>
                <u-button
                  v-if="item.uid === userId"
                  type="primary"
                  size="mini"
                  @click.stop="editProduct(getItemId(item))"
                  style="margin-top: 10rpx; font-size: 20rpx"
                >
                  重新编辑
                </u-button>
              </view>
            </view>
            <!-- 地址信息 -->
            <view class="look-views" v-if="formatAddress(item)">
              <u-icon name="map" color="#ffffff" size="25rpx"></u-icon>
              <text style="margin-left: 5rpx">{{ formatAddress(item) }}</text>
            </view>
            <!-- 视频标识 -->
            <view v-if="item.productType == 2" class="video-play">
              <image src="@/static/svgs/video.svg" style="width: 45rpx" mode="widthFix"></image>
            </view>
            <!-- Live 图标识 -->
            <view v-if="item.productType == 3" class="live-badge">
              <image src="@/static/svgs/live.svg" style="width: 45rpx" mode="widthFix"></image>
            </view>
          </view>
          <!-- 商品标题 -->
          <view class="title">{{ item.title }}</view>
          <!-- 价格区 -->
          <view class="goods-price-row">
            <text class="goods-price">¥{{ item.price }}</text>
            <text class="goods-origin-price">¥{{ item.originalPrice || item.originPrice }}</text>
            <view v-if="item.postType === 0" class="post-type-tag post-type-shipping">包邮</view>
            <view v-else-if="item.postType === 1" class="post-type-tag post-type-pickup">自提</view>
          </view>
          <!-- 用户信息 -->
          <view style="display: flex; position: relative; padding: 20rpx">
            <image
              v-if="item.avatarUrl && String(item.avatarUrl).trim()"
              style="height: 22px; width: 22px; border-radius: 50%"
              mode="aspectFill"
              :src="item.avatarUrl"
              @click.stop="handleUserClick(item.belongUserId || item.uid)"
            ></image>
            <view
              class="product-username"
              @click.stop="handleUserClick(item.belongUserId || item.uid)"
            >
              {{ item.nickname || '用户' }}
            </view>
            <view style="display: flex; position: absolute; right: 10rpx">
              <!-- 市集页面：显示可点击的收藏按钮 -->
              <template v-if="showCollection">
                <u-transition :show="!item.isCollection" mode="fade" duration="2000">
                  <view
                    @click.stop="handleCollectionClickRight(item, index)"
                    style="display: inline-block"
                  >
                    <u-icon v-if="!item.isCollection" name="star" size="18" color="#bbb"></u-icon>
                  </view>
                </u-transition>
                <u-transition :show="item.isCollection" mode="fade" duration="2000">
                  <view
                    @click.stop="handleCollectionClickRight(item, index)"
                    style="display: inline-block"
                  >
                    <u-icon
                      v-if="item.isCollection"
                      name="star-fill"
                      size="18"
                      color="#f7b500"
                    ></u-icon>
                  </view>
                </u-transition>
                <view
                  v-if="item.notesLikeNum > 0"
                  style="color: gray; font-size: 12px; line-height: 18px; margin-left: 3rpx"
                >
                  {{ item.notesLikeNum }}
                </view>
                <view
                  v-else
                  style="color: gray; font-size: 12px; line-height: 18px; margin-left: 3rpx"
                >
                  想要
                </view>
              </template>
              <!-- mine页面：只显示"想要"文本 -->
              <template v-else>
                <u-icon name="star" size="18" color="#bbb"></u-icon>
                <view style="color: gray; font-size: 12px; line-height: 18px; margin-left: 3rpx">
                  想要
                </view>
              </template>
            </view>
          </view>
        </view>
      </block>
    </view>
  </view>
</template>

<script>
export default {
  name: 'IdleWaterFall',
  props: {
    list: {
      type: Array,
      default: () => []
    },
    userId: {
      type: [String, Number],
      default: ''
    },
    // 是否显示收藏按钮（市集页面需要，mine页面不需要）
    showCollection: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      leftList: [],
      rightList: [],
      leftHeight: 0,
      rightHeight: 0,
      isInitializing: false,
      // 初始化期间如果 list 发生追加，把新增项先暂存，避免被跳过导致左右列缺数据
      pendingAppendItems: []
    };
  },
  mounted() {
    this.init();
  },
  watch: {
    list: {
      handler(newList, oldList) {
        // 🔧 修复：微信小程序端优化，添加更详细的日志
        console.log('商品瀑布流list变化:', {
          oldLength: oldList?.length,
          newLength: newList?.length,
          platform: process.env.UNI_PLATFORM
        });

        // 如果新列表为空，清空所有数据
        if (!newList || newList.length === 0) {
          //   console.log('新列表为空，清空商品瀑布流');
          this.clear();
          return;
        }

        // 如果旧列表为空，直接初始化
        if (!oldList || oldList.length === 0) {
          console.log('商品瀑布流：旧列表为空，初始化新数据', {
            newLength: newList.length
          });
          this.$nextTick(() => {
            if (!this.isInitializing) {
              this.init();
            }
          });
          return;
        }

        // 🔧 修复：更准确地检测数据追加（而不是替换）
        // 先检查旧列表的所有ID是否都在新列表中，如果是且新列表更长，说明是追加
        const getItemId = (item) => {
          const id = item.id || item.productId || item.noteId;
          return id !== undefined && id !== null ? String(id) : null;
        };
        
        const oldIds = new Set();
        oldList.forEach(item => {
          const id = getItemId(item);
          if (id) oldIds.add(id);
        });
        
        const newIds = new Set();
        newList.forEach(item => {
          const id = getItemId(item);
          if (id) newIds.add(id);
        });
        
        // 检查旧列表的所有ID是否都在新列表中
        let allOldIdsInNew = true;
        for (let id of oldIds) {
          if (!newIds.has(id)) {
            allOldIdsInNew = false;
            break;
          }
        }
        
        // 如果旧列表的所有ID都在新列表中，且新列表更长，说明是追加
        const isAppended = allOldIdsInNew && newList.length > oldList.length;
        
        if (isAppended) {
          // 数据追加：只添加新数据，不清空重建，避免页面闪烁
          const newItems = newList.filter(item => {
            const id = getItemId(item);
            if (!id) return false; // 过滤掉无效ID的数据
            return !oldIds.has(id);
          });
          
          if (newItems.length > 0) {
            console.log('商品瀑布流：检测到数据追加，只添加新数据', {
              oldLength: oldList.length,
              newLength: newList.length,
              appendCount: newItems.length,
              oldIdsCount: oldIds.size,
              newItemsIds: newItems.map(item => getItemId(item)).slice(0, 10)
            });
            
            // 使用 addList 方法追加新数据，不会清空现有数据
            this.addList(newItems);
          } else {
            console.warn('⚠️ 商品瀑布流：数据追加但所有数据都已存在', {
              oldLength: oldList.length,
              newLength: newList.length,
              oldIdsCount: oldIds.size
            });
          }
        } else if (!allOldIdsInNew || (oldList.length === newList.length && oldList.length > 0)) {
          // 🔧 修复：只有在确认不是追加时才重新初始化
          // 条件：旧列表有ID不在新列表中（数据替换），或长度相同但长度>0（可能是数据更新）
          // 但如果是长度相同且所有ID都匹配，说明只是状态更新，不需要重新初始化
          if (oldList.length === newList.length && allOldIdsInNew && oldIds.size === newIds.size) {
            // 长度相同且所有ID都匹配，说明只是状态更新，不需要重新初始化
            console.log('商品瀑布流：数据长度相同且ID匹配，只同步状态更新，不重新初始化');
            // 同步更新内部列表中的对应项（如果需要）
            // 这里可以添加状态同步逻辑，但为了性能，暂时跳过
            return;
          }
          
          // 数据替换：重建瀑布流
          console.log('商品瀑布流：检测到数据替换，重新初始化', {
            oldLength: oldList.length,
            newLength: newList.length,
            allOldIdsInNew,
            oldIdsCount: oldIds.size,
            newIdsCount: newIds.size
          });

          // 清空现有数据
          this.leftList = [];
          this.rightList = [];
          this.leftHeight = 0;
          this.rightHeight = 0;

          // 使用 nextTick 确保清空完成后再初始化
          this.$nextTick(() => {
            if (!this.isInitializing) {
              this.init();
            }
          });
        } else {
          // 长度相等：只同步状态变化（比如收藏/点赞数量），不动布局
          // 数据长度相同但内容可能变化（如收藏状态更新）
          // 检查是否有数据变化
          let hasChanges = false;
          newList.forEach(newItem => {
            const oldItem = oldList.find(item => item.id === newItem.id);
            if (
              oldItem &&
              (oldItem.isCollection !== newItem.isCollection ||
                oldItem.notesLikeNum !== newItem.notesLikeNum)
            ) {
              hasChanges = true;
              // 同步更新内部列表中的对应项
              const leftIndex = this.leftList.findIndex(item => item.id === newItem.id);
              if (leftIndex !== -1) {
                this.$set(this.leftList[leftIndex], 'isCollection', newItem.isCollection);
                this.$set(this.leftList[leftIndex], 'notesLikeNum', newItem.notesLikeNum);
              }

              const rightIndex = this.rightList.findIndex(item => item.id === newItem.id);
              if (rightIndex !== -1) {
                this.$set(this.rightList[rightIndex], 'isCollection', newItem.isCollection);
                this.$set(this.rightList[rightIndex], 'notesLikeNum', newItem.notesLikeNum);
              }
            }
          });

          if (hasChanges) {
            console.log('商品瀑布流数据内容变化，已同步更新内部列表');
            // 强制更新UI（微信小程序需要）
            // #ifdef MP-WEIXIN
            this.$forceUpdate();
            // #endif
          }
        }
      },
      immediate: false
    }
  },
  methods: {
    formatAddress(item) {
      if (!item) return '';
      const parts = [];
      if (item.city) parts.push(item.city);
      if (item.address) parts.push(item.address);
      // 兼容旧数据
      if (parts.length === 0 && item.location) {
        const location = item.location;
        return location.length <= 12 ? location : location.substring(0, 12) + '...';
      }
      const fullAddress = parts.join(' ');
      return fullAddress.length > 12 ? fullAddress.substring(0, 12) + '...' : fullAddress;
    },
    async init() {
      //   console.log('商品瀑布流init开始，当前list长度:', this.list?.length);

      if (this.isInitializing) {
        console.log('商品瀑布流正在初始化中，跳过重复初始化');
        return;
      }

      this.isInitializing = true;
      // 每次 init 都重置待追加队列，避免跨刷新污染
      this.pendingAppendItems = [];

      if (!this.list || this.list.length === 0) {
        // console.log('商品瀑布流init：没有数据，直接返回');
        this.isInitializing = false;
        return;
      }

      try {
        // 🔧 修复：初始化时使用快照，避免 init 过程中 list 变长导致漏处理/重复处理
        const snapshot = this.list.slice();
        const batchSize = 5;
        for (let i = 0; i < snapshot.length; i += batchSize) {
          const batch = snapshot.slice(i, i + batchSize);
          await this.processBatch(batch);
          await this.yieldToMain();
        }

        console.log(
          '商品瀑布流init完成，左列:',
          this.leftList.length,
          '右列:',
          this.rightList.length
        );
      } catch (error) {
        console.error('商品瀑布流初始化失败:', error);
      } finally {
        this.isInitializing = false;
        // init 完成后，如果期间有追加数据，补一次 addList，确保左右列与外部 list 一致
        if (this.pendingAppendItems && this.pendingAppendItems.length > 0) {
          const pending = this.pendingAppendItems;
          this.pendingAppendItems = [];
          console.log('商品瀑布流init完成后补处理追加数据:', pending.length, '条');
          this.addList(pending);
        }
      }
    },

    async processBatch(batch) {
      const imagePromises = batch.map(item =>
        this.getImageHeight(item.coverPicture).then(res => ({
          ...item,
          coverPicture: res.path,
          imageHeight: res.height
        }))
      );

      const items = await Promise.all(imagePromises);

      items.forEach(item => {
        this.addItem(item);
      });
    },

    addItem(item) {
      const itemHeight = item.imageHeight || 300;

      if (this.leftHeight <= this.rightHeight) {
        this.leftList.push(item);
        this.leftHeight += itemHeight;
      } else {
        this.rightList.push(item);
        this.rightHeight += itemHeight;
      }
    },

    getImageHeight(url) {
      return new Promise((resolve, reject) => {
        if (!url) {
          resolve({ path: url, height: 300 });
          return;
        }

        uni.getImageInfo({
          src: url,
          success: res => {
            const aspectRatio = res.width / res.height;
            const displayWidth = 375; // 假设每列宽度为375rpx
            const displayHeight = displayWidth / aspectRatio;
            resolve({ path: url, height: displayHeight });
          },
          fail: err => {
            console.warn('获取图片信息失败:', err);
            resolve({ path: url, height: 300 });
          }
        });
      });
    },

    yieldToMain() {
      return new Promise(resolve => {
        setTimeout(resolve, 0);
      });
    },

    clear() {
      this.leftList = [];
      this.rightList = [];
      this.leftHeight = 0;
      this.rightHeight = 0;
      this.isInitializing = false;
      // console.log('商品瀑布流clear执行')
    },

    addList(newItems) {
      if (!newItems || newItems.length === 0) {
        console.log('商品瀑布流addList: 没有新数据');
        return;
      }

      // 🔧 修复：去重逻辑优化，确保ID正确
      // 收集已有的ID（包括可能的 productId 和 noteId）
      const existingIds = new Set();
      [...this.leftList, ...this.rightList].forEach(item => {
        const id = item.id || item.productId || item.noteId;
        if (id !== undefined && id !== null) {
          existingIds.add(String(id));
        }
      });

      // 🔧 修复：过滤新数据，确保ID正确且不存在
      const uniqueItems = newItems.filter(item => {
        const id = item.id || item.productId || item.noteId;
        if (id === undefined || id === null) {
          console.warn('⚠️ 商品瀑布流addList: 发现无效ID的数据', item);
          return false; // 过滤掉无效ID的数据
        }
        return !existingIds.has(String(id));
      });

      if (uniqueItems.length === 0) {
        console.log('商品瀑布流addList: 所有数据都已存在，跳过', {
          newItemsCount: newItems.length,
          existingIdsCount: existingIds.size,
          existingIds: Array.from(existingIds).slice(0, 10) // 只显示前10个ID
        });
        return;
      }

      console.log(
        `商品瀑布流addList: 添加${uniqueItems.length}条新数据（原始${newItems.length}条）`,
        {
          newItemsIds: newItems.map(item => item.id || item.productId || item.noteId).slice(0, 10),
          existingIdsCount: existingIds.size,
          uniqueItemsIds: uniqueItems.map(item => item.id || item.productId || item.noteId).slice(0, 10)
        }
      );

      // 批量处理
      this.processBatch(uniqueItems).then(() => {
        console.log('商品瀑布流addList完成', {
          leftListLength: this.leftList.length,
          rightListLength: this.rightList.length
        });
      });
    },
    // 🔧 新增：检测数据是否是追加（而不是替换）
    isDataAppended(oldList, newList) {
      if (!oldList || oldList.length === 0) {
        return false;
      }
      
      // 🔧 修复：确保ID正确，兼容 productId 和 noteId
      const getItemId = (item) => {
        const id = item.id || item.productId || item.noteId;
        return id !== undefined && id !== null ? String(id) : null;
      };
      
      // 🔧 修复：如果数据长度相同，检查是否是相同的数据（可能是状态更新）
      if (oldList.length === newList.length) {
        // 数据长度相同，检查ID是否完全一致
        const oldIds = new Set();
        oldList.forEach(item => {
          const id = getItemId(item);
          if (id) oldIds.add(id);
        });
        
        const newIds = new Set();
        newList.forEach(item => {
          const id = getItemId(item);
          if (id) newIds.add(id);
        });
        
        // 如果ID集合相同，说明是相同数据（可能是状态更新），不是替换也不是追加
        if (oldIds.size === newIds.size && oldIds.size > 0) {
          let allMatch = true;
          for (let id of oldIds) {
            if (!newIds.has(id)) {
              allMatch = false;
              break;
            }
          }
          if (allMatch) {
            // ID完全一致，说明是相同数据，不是替换
            return true; // 返回 true，避免被误判为替换
          }
        }
        // ID不完全一致，说明是替换
        return false;
      }
      
      // 如果新列表包含所有旧列表的数据，且新列表更长，说明是追加
      const oldIds = new Set();
      oldList.forEach(item => {
        const id = getItemId(item);
        if (id) oldIds.add(id);
      });
      
      const newIds = new Set();
      newList.forEach(item => {
        const id = getItemId(item);
        if (id) newIds.add(id);
      });
      
      // 检查旧列表的所有ID是否都在新列表中
      for (let id of oldIds) {
        if (!newIds.has(id)) {
          return false; // 有旧数据不在新列表中，说明是替换
        }
      }
      // 如果新列表比旧列表长，说明是追加
      return newList.length > oldList.length;
    },
    goToDetail(id, type) {
      // 🔧 修复：添加参数验证，防止传递 undefined
      if (!id || id === 'undefined' || id === 'null') {
        console.error('❌ goToDetail: 商品ID无效', id, {
          type: typeof id,
          value: id,
          stack: new Error().stack
        });
        uni.showToast({
          title: '商品ID无效',
          icon: 'none'
        });
        return;
      }
      this.$emit('detail', id, type);
    },

    editProduct(productId) {
      this.$emit('edit', productId);
    },

    handleCollection(item, index, side) {
      // @click.stop 修饰符已经自动阻止了事件冒泡，无需手动处理
      this.$emit('collection', item, index, side);
    },

    handleCollectionClick(item, index, side) {
      // 包装函数，避免 u-icon 组件内部事件处理问题
      this.handleCollection(item, index, side);
    },

    /**
     * 处理左侧列表的收藏点击（包装函数，避免小程序编译错误）
     */
    handleCollectionClickLeft(item, index) {
      this.handleCollection(item, index, 'left');
    },

    /**
     * 处理右侧列表的收藏点击（包装函数，避免小程序编译错误）
     */
    handleCollectionClickRight(item, index) {
      this.handleCollection(item, index, 'right');
    },

    handleUserClick(userId) {
      // @click.stop 修饰符已经自动阻止了事件冒泡，无需手动处理
      this.$emit('user-click', userId);
    },

    /**
     * 获取商品ID（兼容 id、productId、noteId）
     * @param {Object} item - 列表项
     * @returns {String|Number|null} ID 值
     */
    getItemId(item) {
      if (!item) {
        console.warn('⚠️ getItemId: item 为空');
        return null;
      }
      const id = item.id || item.productId || item.noteId || null;
      if (!id) {
        console.warn('⚠️ getItemId: 无法获取商品ID', {
          item: item,
          hasId: !!item.id,
          hasProductId: !!item.productId,
          hasNoteId: !!item.noteId
        });
      }
      return id;
    },
    /**
     * 生成左侧列表项的 key，避免在模板中使用字符串参数导致小程序编译错误
     * @param {Object} item - 列表项
     * @param {Number} index - 索引
     * @returns {String} key 值
     */
    getLeftItemKey(item, index) {
      if (!item) {
        return `l_noitem_${index}`;
      }
      const id = this.getItemId(item);
      if (id) {
        return `l_${id}_${index}`;
      }
      return `l_noid_${index}`;
    },

    /**
     * 生成右侧列表项的 key，避免在模板中使用字符串参数导致小程序编译错误
     * @param {Object} item - 列表项
     * @param {Number} index - 索引
     * @returns {String} key 值
     */
    getRightItemKey(item, index) {
      if (!item) {
        return `r_noitem_${index}`;
      }
      const id = this.getItemId(item);
      if (id) {
        return `r_${id}_${index}`;
      }
      return `r_noid_${index}`;
    },

    // 更新指定商品的收藏状态
    updateCollectionStatus(productId, isCollection, notesLikeNum) {
      console.log('🔄 更新商品收藏状态:', { productId, isCollection, notesLikeNum });

      // 更新 leftList 中的对应项
      const leftIndex = this.leftList.findIndex(item => item.id === productId);
      if (leftIndex !== -1) {
        this.$set(this.leftList[leftIndex], 'isCollection', isCollection);
        this.$set(this.leftList[leftIndex], 'notesLikeNum', notesLikeNum);
        console.log('✅ 已更新左列商品:', this.leftList[leftIndex]);
      }

      // 更新 rightList 中的对应项
      const rightIndex = this.rightList.findIndex(item => item.id === productId);
      if (rightIndex !== -1) {
        this.$set(this.rightList[rightIndex], 'isCollection', isCollection);
        this.$set(this.rightList[rightIndex], 'notesLikeNum', notesLikeNum);
        console.log('✅ 已更新右列商品:', this.rightList[rightIndex]);
      }

      // 强制更新UI（所有平台都需要）
      this.$forceUpdate();
    }
  }
};
</script>

<style lang="scss" scoped>
.water-left,
.water-right {
  width: 48%;
  margin: 20rpx auto;
}

.product-card {
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.08);
  margin-bottom: 24rpx;
  margin-left: 8rpx;
  margin-right: 8rpx;
  overflow: hidden;
  position: relative;
  border: 1rpx solid #f0f0f0;
}

.image-container {
  position: relative;
  display: inline-block;
  width: 100%;
}

.audit-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
}

.audit-overlay.not-passed {
  background: rgba(0, 0, 0, 0.7);
}

.audit-eye {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.look-views {
  display: flex;
  position: absolute;
  bottom: 5rpx;
  left: 8rpx;
  color: #ffffff;
  background-color: rgba(123, 124, 125, 0.6);
  padding: 3rpx 10rpx;
  border-radius: 50rpx;
  font-size: 22rpx;
  z-index: 2;
}

.video-play {
  position: absolute;
  top: 5rpx;
  right: 10rpx;
  // background-color: rgba(123, 124, 125, 0.6);
  padding: 10rpx;
  border-radius: 50%;
}

.live-badge {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  // background-color: rgba(123, 124, 125, 0.6);
  padding: 10rpx;
  border-radius: 50%;
}

.title {
  font-size: 30rpx;
  padding: 10rpx;
  margin-bottom: -5px;
  color: #000000;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-height: 1.4em;
  max-height: 2.4em;
}

.goods-price-row {
  display: flex;
  align-items: center;
  padding: 10rpx;
  gap: 10rpx;
  margin-left: 5rpx;
}

.goods-price {
  color: #ff2442;
  font-size: 32rpx;
  font-weight: bold;
}

.goods-origin-price {
  color: #999;
  font-size: 24rpx;
  text-decoration: line-through;
}

.post-type-tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  margin-left: auto;
  font-weight: 500;
  white-space: nowrap;
}

.post-type-shipping {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: #ffffff;
  border: 1rpx solid #52c41a;
}

.post-type-pickup {
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
  color: #ffffff;
  border: 1rpx solid #ff9800;
}

.product-username {
  margin-left: 10rpx;
  color: #16160e;
  font-size: 23rpx;
  line-height: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: calc(100% - 70px);
}
</style>
