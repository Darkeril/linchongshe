<template>
	<view>
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{ height: statusBarHeight + 'px' }"
			style="position: fixed; top: 0; width: 100%; z-index: 9999; background-color: #fff"></view>
		<!-- #endif -->
		<view :style="{ top: navBarTop }" style="
        position: fixed;
        width: 100%;
        height: 44px;
        background-color: #fff;
        z-index: 9999;
        display: flex;
        align-items: center;
        padding: 0 10rpx;
        box-sizing: border-box;
      ">
			<!-- #ifndef MP-WEIXIN -->
			<u-icon name="arrow-left" color="#2b2b2b" size="25" @click="goBack"></u-icon>
			<!-- #endif -->
			<view style="margin-left: 20rpx">
				<image :src="notesDetail.avatarUrl" style="height: 35px; width: 35px; border-radius: 50%"
					mode="aspectFill" @click="goToOtherMine(notesDetail.belongUserId)"></image>
			</view>
			<view class="authorName">{{ notesDetail.nickname }}</view>
			<view v-if="notesDetail.belongUserId != userInfo.id"
				style="display: flex; justify-content: space-around; flex: 1; padding: 0 20rpx">
				<u-tag v-if="notesDetail.isFollow" text="已关注" shape="circle" color="#3d8af5" bgColor="#e6f2ff"
					borderColor="#3d8af5" @click="attention"></u-tag>
				<u-tag v-else text="关注" plain shape="circle" color="#3d8af5" borderColor="#3d8af5"
					@click="attention"></u-tag>
				<u-icon @click="showShare = true" name="share" color="#2b2b2b" size="30"></u-icon>
			</view>
			<view v-else style="display: flex; margin-left: auto; margin-right: 15rpx">
				<u-icon @click="showShare = true" name="more-dot-fill" color="#2b2b2b" size="26"></u-icon>
			</view>
		</view>
		<view :style="{ height: navPlaceholderHeight }" style="width: 100%"></view>
		<view style="position: relative">
			<swiper :style="{ height: swipperHeight + 70 + 'rpx' }" :indicator-dots="true" :circular="true"
				current="swiperCurrent" @change="swiperChange" indicator-active-color="#3d8af5"
				indicator-color="#9ea1a3">
				<swiper-item v-for="(item, index) in notesDetail.notesResources" :key="index">
					<view :style="{ height: swipperHeight + 'rpx' }"
						style="display: flex; justify-content: center; align-items: center">
						<!-- 图片类型 -->
						<image v-if="!item.isVideo" :src="item.url" mode="widthFix" style="max-width: 750rpx"
							:style="{ width: item.width + 'rpx', transition: 'all 0.3s ease' }"
							:show-menu-by-longpress="true"></image>
						<!-- 视频类型 -->
						<video v-else :src="item.url" :poster="item.poster || notesDetail.coverPicture || ''"
							:controls="true" :show-center-play-btn="true" :enable-progress-gesture="true"
							:show-mute-btn="true" playsinline webkit-playsinline x5-playsinline
							style="width: 100%; max-height: 100%"></video>
					</view>
				</swiper-item>
			</swiper>
			<view style="
          position: absolute;
          top: 30rpx;
          right: 30rpx;
          background-color: #383c3c;
          border-radius: 20px;
          font-size: 28rpx;
          color: #f5f5f5;
          padding: 8rpx 18rpx;
        " v-if="notesDetail != null">
				{{ swiperCurrent + 1 }}/{{ swipperCount }}
			</view>
		</view>
		<view style="padding: 10rpx 40rpx">
			<view style="
          font-size: 37rpx;
          color: #474a4d;
          margin-bottom: 15rpx;
          word-wrap: break-word;
          word-break: break-all;
          letter-spacing: 2rpx;
        ">
				{{ notesDetail.title }}
			</view>

			<!-- 商品内容 -->
			<view v-if="notesDetail.content" style="
          font-size: 32rpx;
          letter-spacing: 0.1rem;
          word-wrap: break-word;
          word-break: break-all;
          line-height: 1.8;
          margin-bottom: 20rpx;
        ">
				<rich-text :nodes="notesDetail.content" @itemclick="clickTopic"></rich-text>
			</view>

			<!-- 商品信息区域：发货方式在卡片右上角，仅显示包邮/自提 -->
			<view style="position: relative; background-color: #f8f9fa; border-radius: 16rpx; padding: 30rpx; margin: 20rpx 0">
				<view v-if="notesDetail.postType === 0" style="position: absolute; top: 38rpx; right: 24rpx; background-color: #e8f5e8; color: #52c41a; padding: 8rpx 16rpx; border-radius: 20rpx; font-size: 25rpx;">包邮</view>
				<view v-else-if="notesDetail.postType === 1" style="position: absolute; top: 38rpx; right: 24rpx; background-color: #fff7e6; color: #fa8c16; padding: 8rpx 16rpx; border-radius: 20rpx; font-size: 25rpx;">自提</view>
				<!-- 价格信息 -->
				<view style="display: flex; align-items: center; margin-bottom: 10rpx">
					<view style="font-size: 48rpx; color: #ff2442; font-weight: bold">
						¥{{ notesDetail.price }}
					</view>
					<view v-if="notesDetail.originalPrice && notesDetail.originalPrice > notesDetail.price"
						style="font-size: 28rpx; color: #999; text-decoration: line-through; margin-left: 20rpx">
						原价 ¥{{ notesDetail.originalPrice }}
					</view>
				</view>

				<!-- 发布位置 -->
				<!-- <view style="display: flex;align-items: center;">
					<view style="font-size: 28rpx;color: #666;margin-right: 20rpx;">发布位置:</view>
					<view style="font-size: 28rpx;color: #333;">
						{{notesDetail.province}}{{notesDetail.city}}{{notesDetail.district}}
					</view>
				</view> -->
			</view>
			<view style="display: flex; margin-top: 15rpx; font-size: 24rpx; color: #afafb0">
				<view>
					<text v-if="notesDetail.updateTime != notesDetail.createTime">
						更新于{{ notesDetail.updateTime }}
					</text>
					<text v-else>{{ notesDetail.createTime }}</text>
				</view>
				<view style="margin-left: 20rpx">{{ notesDetail.province }}</view>
			</view>
		</view>
		<u-divider style="padding: 0 30rpx" :hairline="true"></u-divider>
		<view style="padding: 30rpx">
			<view v-if="commentCount > 0" style="font-size: 25rpx; color: #474a4d">
				共{{ commentCount }}条评论
			</view>
			<view v-else style="font-size: 25rpx; color: #474a4d">暂无评论</view>
			<view style="display: flex; margin-top: 20rpx; height: 90rpx; align-items: center">
				<image style="height: 70rpx; width: 70rpx; border-radius: 50%" mode="aspectFill"
					:src="userInfo.avatarUrl"></image>
				<view @click="replyNotes" style="
            margin-left: 20rpx;
            flex: 1;
            background-color: #f3f3f2;
            padding: 20rpx;
            border-radius: 70rpx;
            height: 100%;
            box-sizing: border-box;
            display: flex;
            align-items: center;
          ">
					<view style="font-size: 30rpx; color: #afafb0; margin-left: 10rpx">
						爱评论的人运气都不差
					</view>
					<view style="display: flex; justify-content: space-around; flex: 1; padding: 0 10rpx 0 50rpx">
						<u-icon name="/static/aite.png" size="25"></u-icon>
						<u-icon name="/static/emoji.png" size="25"></u-icon>
						<u-icon name="/static/photo.png" size="25"></u-icon>
					</view>
				</view>
			</view>
		</view>
		<view style="padding: 30rpx">
			<block v-for="(item, index) in commentList" v-bind:key="item.id">
				<view style="display: flex; align-items: flex-start">
					<image :src="item.commentUserAvatar" style="height: 70rpx; width: 70rpx; border-radius: 50%"
						mode="aspectFill" @click="goToOtherMine(item.commentUserId)"></image>
					<view style="flex: 1; padding: 0 10px; word-break: break-all; text-overflow: ellipsis">
						<view style="display: flex; font-size: 26rpx; color: #afafb0">
							<view style="
                  word-break: break-all;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  max-width: 300rpx;
                " @click="goToOtherMine(item.commentUserId)">
								{{ item.commentUserName }}
							</view>
							<view v-if="item.commentUserId == notesDetail.belongUserId" style="
                  margin-left: 10rpx;
                  padding: 4rpx 10rpx;
                  background-color: #f3f3f2;
                  color: #7d7d7d;
                  border-radius: 50rpx;
                  white-space: nowrap;
                  width: 50rpx;
                  text-align: center;
                ">
								作者
							</view>
							<view v-else-if="item.commentUserId == userInfo.id" style="
                  margin-left: 10rpx;
                  padding: 4rpx 10rpx;
                  background-color: #f3f3f2;
                  color: #7d7d7d;
                  border-radius: 50rpx;
                  white-space: nowrap;
                  width: 30rpx;
                  text-align: center;
                ">
								我
							</view>
						</view>
						<view style="margin-top: 5rpx">
							<view style="display: flex; align-items: baseline; flex-wrap: wrap">
								<rich-text style="font-size: 14px; letter-spacing: 0.05rem; color: #383c3c"
									:nodes="item.content" @longpress="openCommentSetting(item, 0)" @touchend="touchend"
									@click="replyFirstComment(item)" @itemclick="clickUser"></rich-text>
								<text style="font-size: 20rpx; color: #afafb0; margin-left: 10rpx; white-space: nowrap"
									@longpress="openCommentSetting(item, 0)" @touchend="touchend"
									@click="replyFirstComment(item)">
									{{ item.createTime }}
								</text>
								<text style="font-size: 20rpx; color: #afafb0; margin-left: 20rpx; white-space: nowrap"
									@longpress="openCommentSetting(item, 0)" @touchend="touchend"
									@click="replyFirstComment(item)">
									{{ item.province }}
								</text>
								<text style="font-size: 20rpx; color: #7d7d7d; margin-left: 20rpx; white-space: nowrap"
									@longpress="openCommentSetting(item, 0)" @touchend="touchend"
									@click="replyFirstComment(item)">
									回复
								</text>
							</view>
						</view>
						<view v-if="item.pictureUrl != null && item.pictureUrl != ''" style="margin-top: 10rpx"
							@click="previewImage(item.picture.url)">
							<image :src="item.picture.url"
								:style="{ height: item.picture.height + 'rpx', width: item.picture.width + 'rpx' }"
								style="border-radius: 20rpx" mode="aspectFill"></image>
						</view>
						<view v-if="item.isTop" style="
                background-color: #fdeff2;
                padding: 5rpx 10rpx;
                border-radius: 50rpx;
                font-size: 22rpx;
                color: #3d8af5;
                display: inline-block;
              ">
							置顶评论
						</view>
						<view v-if="item.commentReplyNum > 0" style="margin-top: 10rpx">
							<block v-for="(item2, index2) in item.children.list" v-bind:key="item2.id">
								<view style="
                    display: flex;
                    align-items: flex-start;
                    margin-top: 10rpx;
                    position: relative;
                  ">
									<image :src="item2.commentUserAvatar"
										style="height: 50rpx; width: 50rpx; border-radius: 50%" mode="aspectFill"
										@click="goToOtherMine(item2.commentUserId)"></image>
									<view style="
                      flex: 1;
                      padding: 0 20rpx;
                      word-break: break-all;
                      overflow: hidden;
                      text-overflow: ellipsis;
                    ">
										<view style="display: flex; font-size: 26rpx; color: #afafb0; width: 350rpx">
											<view style="
                          word-break: break-all;
                          overflow: hidden;
                          text-overflow: ellipsis;
                          white-space: nowrap;
                          max-width: 300rpx;
                        " @click="goToOtherMine(item2.commentUserId)">
												{{ item2.commentUserName }}
											</view>
											<view v-if="item2.commentUserId == notesDetail.belongUserId" style="
                          margin-left: 10rpx;
                          padding: 4rpx 10rpx;
                          background-color: #f3f3f2;
                          color: #7d7d7d;
                          border-radius: 50rpx;
                          white-space: nowrap;
                          width: 50rpx;
                          text-align: center;
                        ">
												作者
											</view>
											<view v-else-if="item2.commentUserId == userInfo.id" style="
                          margin-left: 10rpx;
                          padding: 4rpx 10rpx;
                          background-color: #f3f3f2;
                          color: #7d7d7d;
                          border-radius: 50rpx;
                          white-space: nowrap;
                          width: 30rpx;
                          text-align: center;
                        ">
												我
											</view>
										</view>
										<view style="margin-top: 5rpx">
											<view style="display: flex; align-items: baseline; flex-wrap: wrap">
												<rich-text
													style="font-size: 14px; letter-spacing: 0.05rem; color: #383c3c"
													@longpress="openCommentSetting(item2, item)" @touchend="touchend"
													:nodes="item2.content" @click="replySecondComment(item, item2)"
													@itemclick="clickUser"></rich-text>
												<text style="
                            font-size: 20rpx;
                            color: #afafb0;
                            margin-left: 10rpx;
                            white-space: nowrap;
                          " @longpress="openCommentSetting(item2, item)" @touchend="touchend"
													@click="replySecondComment(item, item2)">
													{{ item2.createTime }}
												</text>
												<text style="
                            font-size: 20rpx;
                            color: #afafb0;
                            margin-left: 20rpx;
                            white-space: nowrap;
                          " @longpress="openCommentSetting(item2, item)" @touchend="touchend"
													@click="replySecondComment(item, item2)">
													{{ item2.province }}
												</text>
												<text style="
                            font-size: 20rpx;
                            color: #7d7d7d;
                            margin-left: 20rpx;
                            white-space: nowrap;
                          " @longpress="openCommentSetting(item2, item)" @touchend="touchend"
													@click="replySecondComment(item, item2)">
													回复
												</text>
											</view>
										</view>
										<view v-if="item2.pictureUrl != null && item2.pictureUrl != ''"
											style="margin-top: 10rpx" @click="previewImage(item2.picture.url)">
											<image :src="item2.picture.url" :style="{
                          height: item2.picture.height + 'rpx',
                          width: item2.picture.width + 'rpx'
                        }" style="border-radius: 20rpx" mode="aspectFill"></image>
										</view>
									</view>
									<view style="
                      width: 30px;
                      display: flex;
                      flex-direction: column;
                      justify-content: center;
                      text-align: center;
                      padding: 5rpx;
                      box-sizing: border-box;
                      position: absolute;
                      right: -40px;
                      top: 0;
                    ">
										<u-transition :show="!item2.isLike" mode="fade" duration="2000">
											<u-icon v-if="!item2.isLike" name="/static/praise.png" size="24"
												@click="praiseComment(item2.id, item2.commentUserId, 2, [index, index2])"></u-icon>
										</u-transition>
										<u-transition :show="item2.isLike" mode="fade" duration="2000">
											<u-icon v-if="item2.isLike" name="/static/praise_select.png" size="24"
												@click="praiseComment(item2.id, item2.commentUserId, 2, [index, index2])"></u-icon>
										</u-transition>
										<view v-if="item2.commentLikeNum > 0" style="font-size: 12px; color: #7d7d7d">
											{{ item2.commentLikeNum }}
										</view>
									</view>
								</view>
							</block>
						</view>
						<u-loadmore v-if="item.commentReplyNum > 0 && item.children.list.length > 0" :fontSize="13"
							color="#5b7e91" style="width: 350rpx; letter-spacing: 0.05rem"
							:status="item.children.status" :loading-text="loadingText"
							:loadmore-text="item.children.loadmoreText" nomore-text=" "
							@loadmore="getReplyList(item.id)" />
					</view>
					<view style="
              width: 30px;
              display: flex;
              flex-direction: column;
              justify-content: center;
              text-align: center;
              padding: 5rpx;
              box-sizing: border-box;
            ">
						<u-transition :show="!item.isLike" mode="fade" duration="2000">
							<u-icon v-if="!item.isLike" name="/static/praise.png" size="24"
								@click="praiseComment(item.id, item.commentUserId, 1, [index])"></u-icon>
						</u-transition>
						<u-transition :show="item.isLike" mode="fade" duration="2000">
							<u-icon v-if="item.isLike" name="/static/praise_select.png" size="24"
								@click="praiseComment(item.id, item.commentUserId, 1, [index])"></u-icon>
						</u-transition>
						<view v-if="item.commentLikeNum > 0" style="font-size: 12px; color: #7d7d7d">
							{{ item.commentLikeNum }}
						</view>
					</view>
				</view>
				<u-divider style="padding-left: 100rpx" :hairline="true"></u-divider>
			</block>
			<u-loadmore v-if="commentList.length > 0" line :status="status" :loading-text="loadingText"
				loadingIcon="semicircle"></u-loadmore>
		</view>
		<u-popup :show="inputField" mode="bottom" @close="closeEditor">
			<view style="
          width: 100%;
          box-sizing: border-box;
          position: fixed;
          bottom: 0;
          background-color: #fff;
        ">
				<view @click="onEditClick">
					<lsj-edit class="lsjComment" style="height: auto" ref="lsjComment" :placeholder="editPlaceholder"
						:maxCount="1000" @onReady="editReady"></lsj-edit>
				</view>
				<view style="
            display: flex;
            width: 100%;
            padding: 0 10rpx;
            box-sizing: border-box;
            height: 90rpx;
          ">
					<view style="display: flex; justify-content: space-around; width: 50%; align-items: center">
						<view @touchend.prevent="chooseAite">
							<u-icon name="/static/aite.png" size="25"></u-icon>
						</view>
						<view @touchend.prevent="openEmoji">
							<u-icon name="/static/emoji.png" size="25" :color="showEmoji ? '#ff4757' : '#666'"></u-icon>
						</view>
						<view @touchend.prevent="chooseImage">
							<u-icon name="/static/photo.png" size="25"></u-icon>
						</view>
					</view>
					<view style="display: flex; margin-left: auto; margin-right: 20rpx; align-items: center"
						@touchend.prevent="sendComment">
						<u-button style="height: 70rpx" size="mini" type="primary" shape="circle">
							发送
						</u-button>
					</view>
				</view>
				<view v-if="commentImagesurl != ''" style="padding: 30rpx; display: flex">
					<view style="position: relative">
						<image :src="commentImagesurl" style="height: 100rpx; width: 100rpx" mode="aspectFill"></image>
						<view style="
                position: absolute;
                top: 0;
                right: 0;
                background-color: #7d7d7d;
                padding: 5rpx;
                border-bottom-left-radius: 50%;
                border-top-right-radius: 5rpx;
              ">
							<u-icon name="close" size="9" color="#f3f3f2" @click="deleteImage"></u-icon>
						</view>
					</view>
				</view>
				<scroll-view :scroll-x="true"
					style="background-color: #fff; display: flex; align-items: center; white-space: nowrap"
					v-if="bottomEmoji.length > 0 && !showEmoji">
					<view style="display: inline-flex; align-items: center; padding: 0 15rpx">
						<block v-for="(em, index) in bottomEmoji" v-bind:key="index">
							<view @click="addEmoji(em)"
								style="padding: 15rpx; font-size: 48rpx; line-height: 70rpx; height: 70rpx">
								{{ em }}
							</view>
						</block>
					</view>
				</scroll-view>
				<!-- 统一使用与聊天相同的 Unicode 表情 -->
				<scroll-view v-if="showEmoji" :style="{ height: (keyboardHeight > 0 ? keyboardHeight : 300) + 'px' }"
					scroll-y style="background-color: aliceblue">
					<view style="
              display: grid;
              padding: 20rpx;
              grid-template-columns: repeat(8, 1fr);
              gap: 16rpx;
              text-align: center;
            ">
						<block v-for="(em, index) in emojiList" v-bind:key="index">
							<view @click="addEmoji(em)" style="font-size: 48rpx; line-height: 100rpx; height: 100rpx">
								{{ em }}
							</view>
						</block>
					</view>
				</scroll-view>
				<view v-if="showAite" style="background-color: #f3f3f2">
					<scroll-view style="height: 500rpx" @scrolltolower="getAttentionUser">
						<view v-if="attentionUser.list.length > 0" style="padding: 30rpx">
							<block v-for="(item, index) in attentionUser.list" v-bind:key="item.id">
								<view style="display: flex; padding: 10rpx 0; align-items: center">
									<image :src="item.avatarUrl"
										style="height: 90rpx; width: 90rpx; border-radius: 50%"></image>
									<view style="
                      display: flex;
                      flex-direction: column;
                      justify-content: space-between;
                      margin-left: 20rpx;
                      flex: 1;
                    ">
										<view style="font-size: 30rpx; color: #2b2b2b">{{ item.nickname }}</view>
									</view>
									<view style="margin-left: auto">
										<view style="
                        width: 150rpx;
                        height: 60rpx;
                        line-height: 60rpx;
                        text-align: center;
                        border-radius: 30rpx;
                        background-color: #3d8af5;
                        color: #ffffff;
                        font-size: 25rpx;
                      " @click="addUser(item)">
											@Ta
										</view>
									</view>
								</view>
								<u-divider></u-divider>
							</block>
							<view style="margin-top: 70rpx">
								<u-loadmore :status="attentionUser.status" loadingIcon="spinner" line></u-loadmore>
							</view>
						</view>
						<view v-else
							style="display: flex; justify-content: center; align-items: center; height: 500rpx">
							<view style="font-size: 25rpx; color: #afafb0">暂无数据</view>
						</view>
					</scroll-view>
				</view>
			</view>
		</u-popup>
		<!-- 非自己的商品：显示购买按钮 -->
		<view v-if="!inputField && notesDetail.belongUserId != userInfo.id" style="
        position: fixed;
        bottom: 0;
        display: flex;
        padding: 20rpx 30rpx;
        box-sizing: border-box;
        height: 100rpx;
        width: 100%;
        background-color: #fff;
        align-items: center;
        justify-content: space-between;
        border-top: 1px solid #eee;
      ">
			<!-- 左侧收藏图标区 -->
			<view style="
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          min-width: 80rpx;
        ">
				<u-transition :show="!notesDetail.isCollect" mode="fade" duration="2000">
					<u-icon v-if="!notesDetail.isCollect" name="star" size="32" color="#bbb"
						@click="handleWant"></u-icon>
				</u-transition>
				<u-transition :show="notesDetail.isCollect" mode="fade" duration="2000">
					<u-icon v-if="notesDetail.isCollect" name="star-fill" size="32" color="#f7b500"
						@click="handleWant"></u-icon>
				</u-transition>
				<view v-if="notesDetail.notesCollectNum > 0" style="color: #666; font-size: 22rpx; margin-top: 6rpx">
					{{ notesDetail.notesCollectNum }}
				</view>
				<view style="font-size: 22rpx; color: #666; margin-top: 6rpx" v-else>收藏</view>
			</view>

			<!-- 右侧按钮区：「我想要」固定为去聊天，与左侧收藏状态无关 -->
			<view style="flex: 1; display: flex; gap: 24rpx; margin-left: 40rpx">
				<view @click="goToProductChat"
					style="flex: 1; height: 80rpx; background-color: #fff; border: 1px solid #3d8af5; border-radius: 40rpx; display: flex; align-items: center; justify-content: center; color: #3d8af5; font-size: 30rpx; font-weight: 500;">
					我想要
				</view>
				<view @click="buyNow" style="
            flex: 1.5;
            height: 80rpx;
            background-color: #3d8af5;
            border-radius: 40rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
            font-size: 30rpx;
            font-weight: 500;
          ">
					立即购买
				</view>
			</view>
		</view>

		<!-- 自己的商品：显示评论、点赞等按钮 -->
		<view v-else-if="!inputField" style="
        position: fixed;
        bottom: 0;
        display: flex;
        padding: 20rpx;
        box-sizing: border-box;
        height: 60px;
        width: 100%;
        background-color: #fff;
      ">
			<view class="bottom-edit" @click="replyNotes">
				<u-icon name="/static/icons_edit.png" size="21"></u-icon>
				<view style="font-size: 32rpx; color: #afafb0; margin-left: 10rpx">说点什么...</view>
			</view>
			<view class="bottom-icon">
				<view style="display: flex; align-items: center; margin: 0 10rpx">
					<u-transition :show="!notesDetail.isLike" mode="fade" duration="2000">
						<u-icon v-if="!notesDetail.isLike" name="/static/praise.png" size="28"
							@click="praiseNotes(notesDetail.id)"></u-icon>
					</u-transition>
					<u-transition :show="notesDetail.isLike" mode="fade" duration="2000">
						<u-icon v-if="notesDetail.isLike" name="/static/praise_select.png" size="28"
							@click="praiseNotes(notesDetail.id)"></u-icon>
					</u-transition>
					<view v-if="notesDetail.notesLikeNum > 0">{{ notesDetail.notesLikeNum }}</view>
					<view style="font-size: 30rpx" v-else>点赞</view>
				</view>
				<view style="display: flex; align-items: center; margin: 0 10rpx">
					<u-transition :show="!notesDetail.isCollect" mode="fade" duration="2000">
						<u-icon v-if="!notesDetail.isCollect" name="star" size="28" color="#bbb"
							@click="collectNotes(notesDetail.id)"></u-icon>
					</u-transition>
					<u-transition :show="notesDetail.isCollect" mode="fade" duration="2000">
						<u-icon v-if="notesDetail.isCollect" name="star-fill" size="28" color="#f7b500"
							@click="collectNotes(notesDetail.id)"></u-icon>
					</u-transition>
					<view v-if="notesDetail.notesCollectNum > 0"
						style="color: gray; font-size: 24rpx; margin-left: 6rpx">
						{{ notesDetail.notesCollectNum }}
					</view>
					<view style="font-size: 24rpx; color: gray; margin-left: 6rpx" v-else>想要</view>
				</view>
				<view style="display: flex; align-items: center; margin: 0 10rpx">
					<u-icon name="/static/comment.png" size="28"></u-icon>
					<view v-if="commentCount > 0">{{ commentCount }}</view>
					<view style="font-size: 30rpx" v-else>评论</view>
				</view>
			</view>
		</view>
		<!-- 底部占位：非自己的商品80px，自己的60px -->
		<view :style="{ height: notesDetail.belongUserId != userInfo.id ? '80px' : '60px' }"></view>
		<u-popup :show="showShare" mode="bottom" @close="showShare = false" round="10" bgColor="#f5f5f5">
			<view :style="{ height: windowHeight * (1 / 6) + 'px' }"
				style="width: 750rpx; display: flex; flex-direction: column">
				<view style="
            display: flex;
            justify-content: center;
            text-align: center;
            width: 100%;
            padding: 20rpx;
            box-sizing: border-box;
            position: relative;
            flex: 1;
          ">
					<view>分享至</view>
					<u-icon style="position: absolute; right: 20rpx" name="close" size="20"
						@click="showShare = false"></u-icon>
				</view>
				<scroll-view scroll-x style="
            white-space: nowrap;
            display: flex;
            width: 100%;
            border-top-style: solid;
            border-top-width: 1rpx;
            border-color: #e5e4e6;
            flex: 3;
            align-items: center;
          " enable-flex>
					<!-- #ifdef MP-WEIXIN -->
					<button
            open-type="share"
            class="share-item"
            @click="showShare = false"
          >
						<u-icon name="weixin-circle-fill" size="50" color="#07c160"></u-icon>
						<view style="font-size: 30rpx; color: #727171">微信好友</view>
					</button>
					<!-- 朋友圈只能通过右上角菜单分享，按钮仅做引导 -->
					<view
            class="share-item"
            @click="handleShareTimeline"
          >
						<u-icon name="qzone-circle-fill" size="50" color="#07c160"></u-icon>
						<view style="font-size: 30rpx; color: #727171">朋友圈</view>
					</view>
					<!-- #endif -->
					<!-- #ifndef MP-WEIXIN -->
					<view @click="handleShareClick" style="
              display: inline-flex;
              flex-direction: column;
              text-align: center;
              padding: 20rpx;
              justify-content: center;
              align-items: center;
            ">
						<u-icon name="weixin-circle-fill" size="50" color="#07c160"></u-icon>
						<view style="font-size: 30rpx; color: #727171">微信好友</view>
					</view>
					<view @click="handleShareTimelineClick" style="
              display: inline-flex;
              flex-direction: column;
              text-align: center;
              padding: 20rpx;
              justify-content: center;
              align-items: center;
            ">
						<u-icon name="qzone-circle-fill" size="50" color="#07c160"></u-icon>
						<view style="font-size: 30rpx; color: #727171">朋友圈</view>
					</view>
					<!-- #endif -->
					<!-- </scroll-view>
        <scroll-view
          scroll-x
          v-if="userInfo.id == notesDetail.belongUserId"
          style="
            white-space: nowrap;
            display: flex;
            width: 100%;
            border-top-style: solid;
            border-top-width: 1rpx;
            border-top-color: #e5e4e6;
            flex: 3;
          "
          enable-flex
        >
          <view
            @click="editNotes"
            style="
              display: inline-flex;
              flex-direction: column;
              text-align: center;
              padding: 20rpx;
              justify-content: center;
            "
          >
            <view style="padding: 20rpx; border-radius: 50%; background-color: #ffffff">
              <u-icon name="/static/image/editNotes.png" size="35"></u-icon>
            </view>
            <view style="font-size: 30rpx; color: #727171">编辑</view>
          </view>
          <view
            style="
              display: inline-flex;
              flex-direction: column;
              text-align: center;
              padding: 20rpx;
              justify-content: center;
            "
          >
            <u-icon name="account" size="50" color="#cdcdcd"></u-icon>
            <view style="font-size: 30rpx; color: #727171">权限设置</view>
          </view>
          <view
            @click="deleteNotes"
            style="
              display: inline-flex;
              flex-direction: column;
              text-align: center;
              padding: 20rpx;
              justify-content: center;
            "
          >
            <view style="padding: 20rpx; border-radius: 50%; background-color: #ffffff">
              <u-icon name="/static/image/delete.png" size="35"></u-icon>
            </view>
            <view style="font-size: 30rpx; color: #727171">删除</view>
          </view> -->
				</scroll-view>
			</view>
		</u-popup>
	</view>
</template>

<script>
	import {
		emojiList
	} from '@/utils/emojiUtil.js';
	import {
		getProductById,
		praiseOrCancelNotes,
		praiseOrCancelIdles,
		deleteNotes
	} from '@/apis/idles_service.js';
	import {
		weChatTimeFormat,
		replaceHTMLTags
	} from '@/utils/util.js';
	import {
		getAttentionList,
		updateAttention
	} from '@/apis/user_service';
	import {
		addComment,
		addCommentSync,
		getCommentWithCommentByProductId,
		getCommentSecondListByNotesId,
		praiseOrCancelComment,
		setNotesTopComment,
		deleteNotesComment
	} from '@/apis/comment_service';
	import {
		getPaymentGlobalConfig
	} from '@/apis/order_service.js';
	import {
		addBrowseRecord
	} from '@/apis/browse_record_service';
	import {
		baseUrl
	} from '@/.env.js';
	import {
		getWeChatJSSDKConfig
	} from '@/apis/third_service.js';
	import {
		initWeChatShare
	} from '@/utils/wechatJSSDK.js';
	import weixinNavBar from '@/utils/weixinNavBar.js';
	export default {
		mixins: [weixinNavBar],
		// 微信小程序分享到好友
		onShareAppMessage() {
			return {
				title: this.notesDetail.title || '来自爱宠社的闲置好物',
				path: `/pkg-detail/pages/idleDetail/idleDetail?productId=${this.notesDetail.id || ''}`,
				imageUrl: this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0 ?
					this.notesDetail.notesResources[0].url :
					'/static/logoImage.png'
			};
		},
		// 微信小程序分享到朋友圈
		onShareTimeline() {
			return {
				title: this.notesDetail.title || '来自爱宠社的闲置好物',
				query: `productId=${this.notesDetail.id || ''}`,
				imageUrl: this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0 ?
					this.notesDetail.notesResources[0].url :
					'/static/logoImage.png'
			};
		},
		data() {
			return {
				showShare: false,
				windowHeight: 0,
				editPlaceholder: '爱评论的人运气都不差',
				statusBarHeight: 0,
				notesDetail: {},
				commentCount: 0,
				commentList: [],
				page: 1,
				pageSize: 10,
				status: 'loadmore',
				swipperHeight: 0,
				swiperCurrent: 0,
				swipperCount: 0,
				userInfo: {},
				edit: null,
				keyboardHeight: 0,
				inputField: false,
				showEmoji: false,
				showAite: false,
				attentionUser: {
					list: [],
					page: 1,
					pageSize: 10,
					status: 'loading',
					isLoading: false,
					isNoMore: false
				},
				emojiList: [],
				bottomEmoji: [],
				commentImagesurl: '',
				content: '',
				revealcontent: '',
				replyCommentUserId: 0,
				replyCommentUserName: '',
				parentId: 0,
				loadingText: '加载中...',
				showBubble: false,
				isLongPress: false
			};
		},
		methods: {
			// 分享到朋友圈（微信小程序：只能通过右上角菜单分享，此处仅提示）
			handleShareTimeline() {
				// #ifdef MP-WEIXIN
				uni.showToast({
					title: '请点击右上角 ··· 选择「分享到朋友圈」',
					icon: 'none',
					duration: 2500
				});
				this.showShare = false;
				// #endif
				// #ifndef MP-WEIXIN
				this.showShare = false;
				// #endif
			},
			async handleShareClick() {
				// #ifdef H5
				try {
					const ua = typeof navigator !== 'undefined' ? navigator.userAgent : '';
					const isWeChat = /micromessenger/i.test(ua);
					if (!isWeChat) {
						uni.showToast({
							title: '请在微信中打开',
							icon: 'none',
							duration: 2000
						});
						this.showShare = false;
						return;
					}
					let shareLink = '';
					let shareImgUrl = '';
					if (typeof window !== 'undefined' && window.location) {
						shareLink = window.location.href.split('#')[0] + `?productId=${this.notesDetail.id || ''}`;
						shareImgUrl =
							this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0 ?
							this.notesDetail.notesResources[0].url :
							`${window.location.origin}/static/logoImage.png`;
					} else {
						shareLink =
							`${baseUrl.replace('/prod-api', '').replace('/dev-api', '')}/pkg-detail/pages/idleDetail/idleDetail?productId=${this.notesDetail.id || ''}`;
						shareImgUrl =
							this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0 ?
							this.notesDetail.notesResources[0].url :
							`${baseUrl.replace('/prod-api', '').replace('/dev-api', '')}/static/logoImage.png`;
					}
					const shareData = {
						title: this.notesDetail.title || '来自爱宠社的闲置好物',
						desc: (this.notesDetail.content || '').replace(/<[^>]+>/g, '').substring(0, 100) ||
							'来自爱宠社的闲置好物',
						link: shareLink,
						imgUrl: shareImgUrl
					};
					await initWeChatShare(params => getWeChatJSSDKConfig(params), shareData);
					uni.showToast({
						title: '请点击右上角菜单分享',
						icon: 'none',
						duration: 2000
					});
					this.showShare = false;
				} catch (error) {
					console.error('微信分享初始化失败:', error);
					uni.showToast({
						title: error.message || '分享功能初始化失败',
						icon: 'none',
						duration: 2000
					});
					this.showShare = false;
				}
				// #endif
				// #ifndef H5
				uni.showToast({
					title: '请使用微信打开后分享',
					icon: 'none',
					duration: 2000
				});
				this.showShare = false;
				// #endif
			},
			async handleShareTimelineClick() {
				// #ifdef H5
				try {
					const ua = typeof navigator !== 'undefined' ? navigator.userAgent : '';
					const isWeChat = /micromessenger/i.test(ua);
					if (!isWeChat) {
						uni.showToast({
							title: '请在微信中打开',
							icon: 'none',
							duration: 2000
						});
						this.showShare = false;
						return;
					}
					let shareLink = '';
					let shareImgUrl = '';
					if (typeof window !== 'undefined' && window.location) {
						shareLink = window.location.href.split('#')[0] + `?productId=${this.notesDetail.id || ''}`;
						shareImgUrl =
							this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0 ?
							this.notesDetail.notesResources[0].url :
							`${window.location.origin}/static/logoImage.png`;
					} else {
						shareLink =
							`${baseUrl.replace('/prod-api', '').replace('/dev-api', '')}/pkg-detail/pages/idleDetail/idleDetail?productId=${this.notesDetail.id || ''}`;
						shareImgUrl =
							this.notesDetail.notesResources && this.notesDetail.notesResources.length > 0 ?
							this.notesDetail.notesResources[0].url :
							`${baseUrl.replace('/prod-api', '').replace('/dev-api', '')}/static/logoImage.png`;
					}
					const shareData = {
						title: this.notesDetail.title || '来自爱宠社的闲置好物',
						link: shareLink,
						imgUrl: shareImgUrl
					};
					await initWeChatShare(params => getWeChatJSSDKConfig(params), shareData);
					uni.showToast({
						title: '请点击右上角菜单分享到朋友圈',
						icon: 'none',
						duration: 2000
					});
					this.showShare = false;
				} catch (error) {
					console.error('微信分享初始化失败:', error);
					uni.showToast({
						title: error.message || '分享功能初始化失败',
						icon: 'none',
						duration: 2000
					});
					this.showShare = false;
				}
				// #endif
				// #ifndef H5
				uni.showToast({
					title: '请使用微信打开后分享到朋友圈',
					icon: 'none',
					duration: 2000
				});
				this.showShare = false;
				// #endif
			},
			deleteNotes() {
				this.$showModal({
					title: '提示',
					content: '是否删除笔记?',
					cancelText: '取消', // 取消按钮的文字
					cancelColor: '#949495', // 取消按钮的文字颜色
					confirmText: '确定', // 确认按钮文字
					confirmColor: '#3d8af5', // 确认按钮颜色
					showCancel: true // 是否显示取消按钮，默认为 true
				}).then(res => {
					deleteNotes({
						notesId: this.notesDetail.id
					}).then(res => {
						console.log(res);
						if (res.code == 20040) {
							uni.showToast({
								title: '删除成功',
								icon: 'none'
							});
							uni.navigateBack({
								delta: 1
							});
						} else {
							uni.showToast({
								title: res.msg == '' ? '删除失败' : res.msg,
								icon: 'none'
							});
						}
					});
				});
			},
			editNotes() {
				// uni.navigateTo({
				// 	url: '/pages/publishNotes/publishNotes?update=2&notesId=' + this.notesDetail.id
				// })
			},
			/**
			 * 关注用户
			 */
			attention() {
				if (!this.userInfo || !this.userInfo.id) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					});
					return;
				}
				updateAttention({
					userId: this.userInfo.id,
					targetUserId: this.notesDetail.belongUserId
				}).then(res => {
					if (res.code === 200) {
						this.notesDetail.isFollow = !this.notesDetail.isFollow;
						uni.showToast({
							title: this.notesDetail.isFollow ? '关注成功' : '取消关注成功',
							icon: 'none'
						});
					}
				});
			},
			openCommentSetting(e, e1) {
				this.isLongPress = true;
				let itemList = ['置顶', '回复', '复制', '删除'];
				if (this.notesDetail.belongUserId != this.userInfo.id || e.parentId != 0) {
					itemList.shift();
				} else {
					if (e.isTop) {
						itemList[0] = '取消置顶';
					}
				}
				if (
					this.notesDetail.belongUserId != this.userInfo.id &&
					e.commentUserId != this.userInfo.id
				) {
					itemList.pop();
				}
				this.$showActionSheet({
					itemList: itemList,
					success: data => {
						let text = itemList[data.tapIndex];
						console.log(text);
						if (text === '置顶') {
							this.topComment(e.id, 0);
						}
						if (text === '取消置顶') {
							this.topComment(e.id, 1);
						}
						if (text === '回复') {
							if (e1 == 0) {
								this.replyFirstComment(e);
							} else {
								this.replySecondComment(e1, e);
							}
						}
						if (text === '复制') {
							let a = replaceHTMLTags(e.content);
							uni.setClipboardData({
								data: a,
								success: function() {
									uni.showToast({
										title: '复制成功',
										icon: 'none'
									});
								}
							});
						}
						if (text === '删除') {
							this.deleteComment(e);
						}
					}
				});
			},
			/** 置顶评论
			 * @param {Object} id
			 */
			topComment(id, type) {
				this.$showModal({
					title: '提示',
					content: type == 0 ? '是否置顶评论?' : '是否取消置顶评论?',
					cancelText: '取消', // 取消按钮的文字
					cancelColor: '#949495', // 取消按钮的文字颜色
					confirmText: '确定', // 确认按钮文字
					confirmColor: '#3d8af5', // 确认按钮颜色
					showCancel: true // 是否显示取消按钮，默认为 true
				}).then(res => {
					setNotesTopComment({
						commentId: id
					}).then(res => {
						console.log(res);
						if (res.code == 200) {
							uni.showToast({
								title: type == 0 ? '置顶成功' : '取消置顶成功',
								icon: 'none'
							});
							// 将置顶的评论放到第一位
							this.commentList.forEach((item, index) => {
								if (item.id == id) {
									if (type == 0) {
										item.isTop = true;
										this.commentList.splice(index, 1);
										this.commentList.unshift(item);
									} else {
										item.isTop = false;
									}
								} else {
									item.isTop = false;
								}
							});
						} else {
							uni.showToast({
								title: res.msg == null ? (type == 0 ? '置顶失败' : '取消置顶失败') : res.msg,
								icon: 'none'
							});
						}
					});
				});
			},
			/** 删除评论
			 * @param {Object} e
			 */
			deleteComment(e) {
				this.$showModal({
					title: '提示',
					content: '是否删除评论?',
					cancelText: '取消', // 取消按钮的文字
					cancelColor: '#949495', // 取消按钮的文字颜色
					confirmText: '确定', // 确认按钮文字
					confirmColor: '#3d8af5', // 确认按钮颜色
					showCancel: true // 是否显示取消按钮，默认为 true
				}).then(res => {
					deleteNotesComment({
						commentId: e.id
					}).then(res => {
						console.log(res);
						if (res.code == 200) {
							uni.showToast({
								title: '删除成功',
								icon: 'none'
							});
							// 删除评论
							if (e.parentId == '0') {
								this.commentList.forEach((item, index) => {
									if (item.id == e.id) {
										this.commentList.splice(index, 1);
									}
								});
							} else {
								this.commentList.forEach((item, index) => {
									if (item.id == e.parentId) {
										item.children.list.forEach((item2, index2) => {
											if (item2.id == e.id) {
												item.children.list.splice(index2, 1);
											}
										});
									}
								});
							}
						} else {
							uni.showToast({
								title: res.msg == null ? '删除失败' : res.msg,
								icon: 'none'
							});
						}
					});
				});
			},
			touchend() {
				setTimeout(() => {
					this.isLongPress = false;
				}, 200);
			},
			/**
			 * 点赞或取消点赞笔记
			 * @param {Object} id
			 */
			praiseNotes(id) {
				praiseOrCancelNotes({
					notesId: id,
					userId: this.userInfo.id,
					targetUserId: this.notesDetail.belongUserId
				}).then(res => {
					console.log(res);
					if (res.code == 200) {
						if (this.notesDetail.isLike) {
							this.notesDetail.notesLikeNum = this.notesDetail.notesLikeNum - 1;
							this.notesDetail.isLike = false;
						} else {
							this.notesDetail.notesLikeNum = this.notesDetail.notesLikeNum + 1;
							this.notesDetail.isLike = true;
						}
					}
				});
			},
			/**
			 * 收藏或取消收藏笔记
			 * @param {Object} id
			 */
			collectNotes(id) {
				praiseOrCancelIdles({
						notesId: id,
						userId: this.userInfo.id,
						targetUserId: this.notesDetail.belongUserId
					})
					.then(res => {
						console.log('收藏操作响应:', res);
						if (res.code == 200) {
							// 切换收藏状态
							this.notesDetail.isCollect = !this.notesDetail.isCollect;

							// 更新收藏数量
							if (this.notesDetail.isCollect) {
								this.notesDetail.notesCollectNum = (this.notesDetail.notesCollectNum || 0) + 1;
							} else {
								this.notesDetail.notesCollectNum = Math.max(
									0,
									(this.notesDetail.notesCollectNum || 0) - 1
								);
							}

							// 静默更新状态，不显示提示

							// 如果是从购物车页面跳转过来的，通知购物车页面刷新
							const pages = getCurrentPages();
							if (pages.length > 1) {
								const prevPage = pages[pages.length - 2];
								if (prevPage && prevPage.route && prevPage.route.includes('shoppingCart')) {
									// 设置购物车页面需要刷新
									prevPage.$vm.needRefresh = true;
								}
							}
						} else {
							uni.showToast({
								title: res.msg || '操作失败',
								icon: 'none'
							});
						}
					})
					.catch(error => {
						console.error('收藏操作失败:', error);
						uni.showToast({
							title: '操作失败',
							icon: 'none'
						});
					});
			},

			/**
			 * 加入购物车
			 */
			addToCart() {
				console.log('加入购物车:', this.notesDetail);

				// 检查用户是否登录
				if (!this.userInfo || !this.userInfo.id) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					});
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/login/login'
						});
					}, 1500);
					return;
				}

				// TODO: 调用加入购物车API
				uni.showToast({
					title: '已加入购物车',
					icon: 'success'
				});

				// 可以跳转到购物车页面
				// uni.navigateTo({
				// 	url: '/pkg-mine/pages/shoppingCart/shoppingCart'
				// });
			},

			/**
			 * 我想要按钮 - 进入商品聊天对话页与卖家沟通
			 */
			goToProductChat() {
				if (!this.userInfo || !this.userInfo.id) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					});
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/login/login'
						});
					}, 1500);
					return;
				}
				const uid = this.notesDetail.belongUserId != null ? String(this.notesDetail.belongUserId) : ''
				const productId = this.notesDetail.id != null ? String(this.notesDetail.id) : ''
				const userName = this.notesDetail.nickname || this.notesDetail.username || '卖家'
				const avatarUrl = this.notesDetail.avatarUrl || ''
				if (!uid) {
					uni.showToast({
						title: '无法发起对话',
						icon: 'none'
					})
					return
				}
				const params = [
					`userId=${uid}`,
					`productId=${productId}`,
					`chatType=0`,
					`userName=${encodeURIComponent(userName)}`,
					`avatarUrl=${encodeURIComponent(avatarUrl)}`
				]
				uni.navigateTo({
					url: `/pkg-msg/pages/chat/chat?${params.join('&')}`
				})
			},

			/**
			 * 收藏区域点击 - 收藏/取消收藏
			 */
			handleWant() {
				if (!this.userInfo || !this.userInfo.id) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					});
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/login/login'
						});
					}, 1500);
					return;
				}
				this.collectNotes(this.notesDetail.id);
			},

			/**
			 * 立即购买
			 */
			async buyNow() {
				console.log('立即购买:', this.notesDetail);

				// 检查用户是否登录
				if (!this.userInfo || !this.userInfo.id) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					});
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/login/login'
						});
					}, 1500);
					return;
				}

				// 检查支付功能是否可用
				try {
					const res = await getPaymentGlobalConfig();
					if (res.code === 200 && res.data) {
						// 检查全局支付开关
						if (!res.data.paymentEnabled) {
							uni.showToast({
								title: '支付功能暂时关闭，暂时无法购买商品',
								icon: 'none',
								duration: 2500
							});
							return;
						}

						// 检查是否有可用的支付方式
						const hasAvailablePayment = res.data.wechatQrcodeEnabled ||
							res.data.alipaySandboxEnabled ||
							res.data.alipayQrcodeEnabled;
						if (!hasAvailablePayment) {
							uni.showToast({
								title: '当前没有可用的支付方式，暂时无法购买商品',
								icon: 'none',
								duration: 2500
							});
							return;
						}
					}
				} catch (error) {
					console.error('检查支付配置失败:', error);
					// 检查失败时仍允许进入，由确认订单页面再次检查
				}

				// 跳转到订单确认页面
				uni.navigateTo({
					url: `/pkg-mine/pages/confirmOrder/confirmOrder?productId=${this.notesDetail.id}&quantity=1`
				});
			},

			/** 点赞或取消点赞评论
			 * @param {Object} id 评论id
			 * @param {Object} userId 评论人id
			 * @param {Object} type 评论类型 1:一级评论 2:二级评论
			 * @param {Object} index 评论下标，数组，[一级评论下标，二级评论下标]
			 */
			praiseComment(id, userId, type, index) {
				console.log(id, userId, type, index);
				praiseOrCancelComment({
					commentId: id,
					userId: this.userInfo.id,
					targetUserId: userId
				}).then(res => {
					console.log(res);
					if (res.code == 200) {
						if (type == 1) {
							if (this.commentList[index].isLike) {
								this.commentList[index].isLike = false;
								this.commentList[index].commentLikeNum--;
							} else {
								this.commentList[index].isLike = true;
								this.commentList[index].commentLikeNum++;
							}
						} else {
							if (this.commentList[index[0]].children.list[index[1]].isLike) {
								this.commentList[index[0]].children.list[index[1]].isLike = false;
								this.commentList[index[0]].children.list[index[1]].commentLikeNum--;
							} else {
								this.commentList[index[0]].children.list[index[1]].isLike = true;
								this.commentList[index[0]].children.list[index[1]].commentLikeNum++;
							}
						}
					}
				});
			},
			/** 点击用户头像跳转到用户主页
			 * @param {Object} e
			 */
			clickUser(e) {
				console.log(e);
				if (e.detail.node.name != 'a') {
					return;
				}
				let href = e.detail.node.attrs.href || '';
				// 检查是否是用户链接（新格式，使用userName）
				if (href.includes('/pkg-mine/pages/mine/otherMine')) {
					const userNameMatch = href.match(/userName=([^&]+)/);
					if (userNameMatch) {
						const userName = decodeURIComponent(userNameMatch[1]);
						uni.navigateTo({
							url: '/pkg-mine/pages/mine/otherMine?userName=' + encodeURIComponent(userName)
						});
						return;
					}
					const userIdMatch = href.match(/userId=([^&]+)/);
					if (userIdMatch) {
						const userId = decodeURIComponent(userIdMatch[1]);
						uni.navigateTo({
							url: '/pkg-mine/pages/mine/otherMine?userId=' + userId
						});
						return;
					}
				}
				// 兼容旧格式
				let userId = this.findUserId(href);
				if (userId) {
					this.goToOtherMine(userId);
				}
			},
			/** 获取一级评论的子评论
			 * @param {Object} e
			 */
			getReplyList(id) {
				console.log(id);
				this.commentList.forEach(item => {
					if (item.id == id) {
						if (item.children.status == 'loading' || item.children.status == 'nomore') {
							return;
						}
						item.children.status = 'loading';
						getCommentSecondListByNotesId({
								productId: this.notesDetail.id,
								parentId: id,
								page: item.children.page,
								pageSize: item.children.pageSize
							})
							.then(res => {
								console.log(res);
								if (res.code == 200) {
									item.children.page++;
									item.children.loadmoreText = '展开更多回复';
									res.data.forEach(item => {
										item.createTime = weChatTimeFormat(Number(item.createTime));
										if (item.pictureUrl != null && item.pictureUrl != '') {
											uni.getImageInfo({
												src: item.pictureUrl,
												success: image => {
													item.picture = {
														url: item.pictureUrl
													};
													// 图片长度最长为350rpx,高度最高为350rpx
													if (image.width >= image.height) {
														if (image.width > 350) {
															item.picture.width = 350;
															item.picture.height = (350 *
																	image.height) / image
																.width;
														} else {
															item.picture.width = image
																.width;
															item.picture.height = image
																.height;
														}
													} else {
														if (image.height > 350) {
															item.picture.height = 350;
															item.picture.width = (350 *
																	image.width) / image
																.height;
														} else {
															item.picture.width = image
																.width;
															item.picture.height = image
																.height;
														}
													}
													console.log(item.picture);
												},
												fail: err => {
													console.log(err);
												}
											});
										} else {
											item.picture = null;
										}
									});
									setTimeout(() => {
										if (res.data.length < item.children.pageSize) {
											item.children.status = 'nomore';
										} else {
											item.children.status = 'loadmore';
										}
										item.children.list.push(...res.data);
									}, 1000);
								} else {
									item.children.status = 'nomore';
								}
							})
							.catch(err => {
								item.children.status = 'nomore';
							})
							.finally(() => {
								console.log(this.commentList);
								return;
							});
					}
				});
			},
			/**
			 * 发送评论
			 */
			sendComment() {
				uni.showLoading({
					title: '发送中...',
					mask: true
				});
				this.edit.getContents().then(res => {
					console.log(res);
					// 如果res.text里面只有换行符或者空格，res.html也没有<img>标签，就不允许发布
					const regex = /^[\n\s]+$/;
					const regex2 = /<img/g;
					if (regex.test(res.text) && this.commentImagesurl == '' && !regex2.test(res.html)) {
						uni.hideLoading();
						uni.showToast({
							title: '请输入内容',
							icon: 'none'
						});
						return;
					}
					this.content = res.html;
					if (this.commentImagesurl == '') {
						addComment({
								content: this.content,
								replyUserId: this.replyCommentUserId,
								replyUserName: this.replyCommentUserName,
								productId: this.notesDetail.id,
								commentUserId: this.userInfo.id,
								parentId: this.parentId,
								pictureUrl: this.commentImagesurl
							})
							.then(res => {
								uni.hideLoading();
								console.log(res);
								if (res.code == 200) {
									// 同步评论
									const id = res.data.id;
									addCommentSync([id]); // 用数组包裹

									uni.showToast({
										title: '评论已发布',
										icon: 'success'
									});
									this.content = '';
									this.inputField = false;
									this.commentShow = false;
									this.commentCount++;
									// 确保输入框关闭
									setTimeout(() => {
										this.inputField = false;
									}, 100);
									res.data.createTime = weChatTimeFormat(Number(res.data.createTime));
									// 映射字段名，确保头像能正确显示
									res.data.commentUserAvatar = res.data.avatar;
									res.data.commentUserName = res.data.username;
									res.data.commentUserId = res.data.uid;
									console.log(res.data);
									if (this.parentId == 0) {
										res.data.children = {
											list: [],
											page: 1,
											pageSize: 10,
											status: 'loadmore',
											loadmoreText: '—— 展开更多回复 ——'
										};
										console.log(res.data);
										this.commentList.unshift(res.data);
									} else {
										this.commentList.forEach(item => {
											if (item.id == this.parentId) {
												console.log(item);
												if (item.children.page == 1) {
													item.commentReplyNum++;
													item.children.loadmoreText = '—— 展开' + item
														.commentReplyNum + '条回复 ——';
												} else {
													item.children.list.unshift(res.data);
												}
											}
										});
									}
								} else {
									uni.showToast({
										title: errorMsg,
										icon: 'none',
										duration: 2500
									});
								}
							})
							.catch(err => {
								uni.hideLoading();
								const errorMsg = err?.msg || err?.message || '评论失败';
								uni.showToast({
									title: errorMsg,
									icon: 'none',
									duration: 2500
								});
							});
						return;
					} else {
						uni.uploadFile({
							url: baseUrl + '/web/oss/upload',
							filePath: this.commentImagesurl,
							name: 'file',
							header: {
								accessToken: uni.getStorageSync('uniapp_token') || ''
							},
							success: res => {
								let data = JSON.parse(res.data);
								if (data.code === 200) {
									console.log('图片上传成功:', data.data);
									// 直接传递图片URL作为content
									addComment({
											content: data.data,
											replyUserId: this.replyCommentUserId,
											replyUserName: this.replyCommentUserName,
											productId: this.notesDetail.id,
											commentUserId: this.userInfo.id,
											parentId: this.parentId,
											pictureUrl: data.data
										})
										.then(res => {
											console.log(res);
											if (res.code == 200) {
												// 同步评论
												const id = res.data.id;
												addCommentSync([id]); // 用数组包裹

												uni.getImageInfo({
													src: res.data.pictureUrl,
													success: image => {
														res.data.picture = {
															url: res.data
																.pictureUrl
														};
														// 图片长度最长为350rpx,高度最高为350rpx
														if (image.width >= image
															.height) {
															if (image.width > 350) {
																res.data.picture
																	.width = 350;
																res.data.picture
																	.height = (350 *
																		image.height) /
																	image.width;
															} else {
																res.data.picture
																	.width = image
																	.width;
																res.data.picture
																	.height = image
																	.height;
															}
														} else {
															if (image.height > 350) {
																res.data.picture
																	.height = 350;
																res.data.picture
																	.width = (350 *
																		image.width) /
																	image.height;
															} else {
																res.data.picture
																	.width = image
																	.width;
																res.data.picture
																	.height = image
																	.height;
															}
														}
														uni.hideLoading();
														console.log(res.data.picture);
														this.content = '';
														this.revealcontent = '';
														this.commentImagesurl = '';
														this.inputField = false;
														this.commentShow = false;
														uni.showToast({
															title: '评论已发布',
															icon: 'success'
														});
														this.commentCount++;
														res.data.createTime =
															weChatTimeFormat(Number(res
																.data.createTime));
														// 映射字段名，确保头像能正确显示
														res.data.commentUserAvatar =
															res.data.avatar;
														res.data.commentUserName = res
															.data.username;
														res.data.commentUserId = res
															.data.uid;
														if (this.parentId == 0) {
															res.data.children = {
																list: [],
																page: 1,
																pageSize: 10,
																status: 'loadmore',
																loadmoreText: '—— 展开更多回复 ——'
															};
															this.commentList.unshift(
																res.data);
														} else {
															this.commentList.forEach(
																item => {
																	if (item.id ==
																		this
																		.parentId
																		) {
																		console
																			.log(
																				item
																				);
																		if (item
																			.children
																			.page ==
																			1) {
																			item
																			.commentReplyNum++;
																			item.children
																				.loadmoreText =
																				'—— 展开' +
																				item
																				.commentReplyNum +
																				'条回复 ——';
																		} else {
																			item.children
																				.list
																				.unshift(
																					res
																					.data
																					);
																		}
																	}
																});
														}
													},
													fail: err => {
														uni.hideLoading();
														console.log(err);
													}
												});
											} else {
												uni.hideLoading();
												const errorMsg = res?.msg || '评论失败';
												uni.showToast({
													title: errorMsg,
													icon: 'none',
													duration: 2500
												});
											}
										})
										.catch(err => {
											uni.hideLoading();
											const errorMsg = err?.msg || err?.message || '评论失败';
											uni.showToast({
												title: errorMsg,
												icon: 'none',
												duration: 2500
											});
										});
								} else {
									uni.hideLoading();
									uni.showToast({
										title: '图片上传失败',
										icon: 'none'
									});
								}
							},
							fail: err => {
								uni.hideLoading();
								uni.showToast({
									title: '图片上传失败',
									icon: 'none'
								});
							}
						});
					}
				});
			},
			/**
			 * 回复一级评论
			 * @param {Object} item 一级评论
			 */
			replyFirstComment(item) {
				if (this.isLongPress) {
					return;
				}
				this.editPlaceholder = '回复 @' + item.commentUserName;
				if (this.replyCommentUserId != item.commentUserId) {
					this.content = '';
					this.revealcontent = '';
				}
				this.replyCommentUserId = 0;
				this.replyCommentUserName = '';
				this.parentId = item.id;
				this.openEditor();
			},
			/**
			 * 回复二级评论
			 * @param {Object} fitem 一级评论
			 * @param {Object} item 二级评论
			 */
			replySecondComment(fitem, item) {
				if (this.isLongPress) {
					return;
				}
				this.editPlaceholder = '回复 @' + item.commentUserName;
				this.content = '';
				this.revealcontent = '';
				this.replyCommentUserId = item.commentUserId;
				this.replyCommentUserName = item.commentUserName;
				this.parentId = fitem.id;
				this.openEditor();
			},
			/**
			 * 回复笔记，即发布一级评论
			 */
			replyNotes() {
				this.editPlaceholder = '爱评论的人运气都不差';
				if (this.replyCommentUserId != 0) {
					this.content = '';
					this.revealcontent = '';
				}
				this.replyCommentUserId = 0;
				this.replyCommentUserName = '';
				this.parentId = 0;
				this.openEditor();
			},
			/**
			 * 弹出输入框
			 */
			openEditor() {
				this.inputField = true;
				this.showEmoji = false;
				this.showAite = false;
				setTimeout(() => {
					this.$refs.lsjComment.keyboardShow();
				}, 700);
			},

			/**
			 * 点击输入框
			 */
			onEditClick() {
				this.showEmoji = false;
				this.showAite = false;
			},
			/**
			 * 关闭输入框
			 */
			closeEditor() {
				this.inputField = false;
				this.showEmoji = false;
				this.showAite = false;
				this.edit.getContents().then(res => {
					console.log(res);
					this.content = res.html;
					this.revealcontent = res.text;
				});
				uni.hideKeyboard();
			},
			/**
			 * 预览图片
			 */
			previewImage(url) {
				uni.previewImage({
					urls: [url]
				});
			},
			/**
			 * 前往对方用户主页
			 * @param {Object} userId
			 */
			goToOtherMine(userId) {
				const currentUserId = uni.getStorageSync('userInfo').id;
				// 如果点击的是当前登录用户的头像，跳转到mine页面
				if (userId == currentUserId) {
					uni.navigateTo({
						url: '/pkg-main/pkg-main/pages/mine/mine'
					});
				} else {
					// 否则跳转到otherMine页面
					uni.navigateTo({
						url: '/pkg-mine/pages/mine/otherMine?userId=' + userId
					});
				}
			},
			/**
			 * 格式化主内容，处理#标签高亮和@用户高亮
			 * @param {String} content 主内容
			 */
			formatContentWithHashtags(content) {
				if (!content) return content;

				// 如果内容已经是HTML格式（包含<span>标签），说明已经格式化过，直接返回
				// 避免重复格式化导致HTML被破坏
				if (typeof content === 'string' && (content.includes('<span') || content.includes('<a '))) {
					return content;
				}

				let processedContent = content;

				// 处理@用户高亮（先处理@用户，避免与#标签冲突）
				// 匹配格式：@用户名（用户名不包含空格、@、#），但不在HTML标签内
				const mentionRegex = /@([^\s@#<>]+)/g;
				processedContent = processedContent.replace(
					mentionRegex,
					(match, userName, offset, string) => {
						// 检查是否在HTML标签内部
						const beforeText = string.substring(Math.max(0, offset - 50), offset);
						// 如果前后有未闭合的<a标签，说明已经在标签内，跳过
						const beforeOpenTags = (beforeText.match(/<a[^>]*>/g) || []).length;
						const beforeCloseTags = (beforeText.match(/<\/a>/g) || []).length;
						if (beforeOpenTags > beforeCloseTags) {
							return match;
						}
						// 只显示蓝色样式，不添加链接（只显示不跳转）
						return '<span style="color: #1890ff;">' + match + '</span>';
					}
				);

				// 处理#标签高亮（如果还没有被格式化）
				// 注意：需要避免匹配HTML标签内的#（如style="color: #1890ff;"）
				// 使用更精确的方法：先找到所有可能的#标签位置，然后检查是否在HTML标签属性内
				let lastIndex = 0;
				let result = '';
				const hashtagRegex = /#([^\s#<>]+)/g;
				let match;

				while ((match = hashtagRegex.exec(processedContent)) !== null) {
					const matchStart = match.index;
					const matchEnd = matchStart + match[0].length;

					// 添加匹配前的文本
					const beforeText = processedContent.substring(lastIndex, matchStart);
					result += beforeText;

					// 检查是否在HTML标签属性内
					const beforeMatch = processedContent.substring(0, matchStart);
					const lastTagStart = beforeMatch.lastIndexOf('<');
					const lastTagEnd = beforeMatch.lastIndexOf('>');

					let shouldSkip = false;

					// 如果在未闭合的标签内
					if (lastTagStart > lastTagEnd) {
						const tagContent = processedContent.substring(lastTagStart, matchEnd);
						// 检查是否在引号内（属性值中）
						const beforeInTag = tagContent.substring(0, matchStart - lastTagStart);
						const singleQuotes = (beforeInTag.match(/'/g) || []).length;
						const doubleQuotes = (beforeInTag.match(/"/g) || []).length;
						// 如果引号数量是奇数，说明在属性值内
						if (singleQuotes % 2 === 1 || doubleQuotes % 2 === 1) {
							shouldSkip = true;
						}
					}

					// 检查是否是十六进制颜色代码
					if (!shouldSkip && /^[0-9a-fA-F]{6}$/i.test(match[1])) {
						shouldSkip = true;
					}

					if (shouldSkip) {
						result += match[0]; // 保持原样
					} else {
						// 检查#标签前面是否有空格，如果没有就添加一个空格
						const lastChar =
							beforeText.length > 0 ?
							beforeText[beforeText.length - 1] :
							lastIndex > 0 ?
							processedContent[lastIndex - 1] :
							' ';
						const needSpace =
							lastChar !== ' ' && lastChar !== '\n' && lastChar !== '\t' && lastIndex !== 0;
						const space = needSpace ? ' ' : '';
						// 只显示蓝色样式，不添加链接（只显示不跳转）
						result += `${space}<span style="color: #1890ff;">${match[0]}</span>`;
					}

					lastIndex = matchEnd;
				}

				// 添加剩余的文本
				result += processedContent.substring(lastIndex);
				processedContent = result;

				return processedContent;
			},
			/**
			 * 格式化评论内容，将图片URL转换为图片标签，并处理#标签高亮和@用户高亮
			 * @param {String} content 评论内容
			 */
			formatCommentContent(content) {
				if (!content) return content;

				// 检测是否为图片URL
				const imageUrlPattern = /^https?:\/\/.*\.(jpg|jpeg|png|gif|webp)(\?.*)?$/i;
				if (imageUrlPattern.test(content.trim())) {
					// 如果是图片URL，转换为img标签，设置固定大小
					return `<div style="width: 120px; height: 120px; overflow: hidden; border-radius: 6px; margin: 5px 0;"><img src="${content}" style="width: 100%; height: 100%; object-fit: cover;" /></div>`;
				}

				// 如果内容已经是HTML格式（包含<span>标签），说明已经格式化过，直接返回
				// 避免重复格式化导致HTML被破坏
				if (typeof content === 'string' && (content.includes('<span') || content.includes('<a '))) {
					return content;
				}

				let processedContent = content;

				// 处理@用户高亮（先处理@用户，避免与#标签冲突）
				// 匹配格式：@用户名（用户名不包含空格、@、#），但不在HTML标签内
				const mentionRegex = /@([^\s@#<>]+)/g;
				processedContent = processedContent.replace(
					mentionRegex,
					(match, userName, offset, string) => {
						// 检查是否在HTML标签内部
						const beforeText = string.substring(Math.max(0, offset - 50), offset);
						// 如果前后有未闭合的<a标签，说明已经在标签内，跳过
						const beforeOpenTags = (beforeText.match(/<a[^>]*>/g) || []).length;
						const beforeCloseTags = (beforeText.match(/<\/a>/g) || []).length;
						if (beforeOpenTags > beforeCloseTags) {
							return match;
						}
						// 只显示蓝色样式，不添加链接（只显示不跳转）
						return '<span style="color: #1890ff;">' + match + '</span>';
					}
				);

				// 处理#标签高亮（如果还没有被格式化）
				// 注意：需要避免匹配HTML标签内的#（如style="color: #1890ff;"）
				// 使用更精确的方法：先找到所有可能的#标签位置，然后检查是否在HTML标签属性内
				let lastIndex = 0;
				let result = '';
				const hashtagRegex = /#([^\s#<>]+)/g;
				let match;

				while ((match = hashtagRegex.exec(processedContent)) !== null) {
					const matchStart = match.index;
					const matchEnd = matchStart + match[0].length;

					// 添加匹配前的文本
					const beforeText = processedContent.substring(lastIndex, matchStart);
					result += beforeText;

					// 检查是否在HTML标签属性内
					const beforeMatch = processedContent.substring(0, matchStart);
					const lastTagStart = beforeMatch.lastIndexOf('<');
					const lastTagEnd = beforeMatch.lastIndexOf('>');

					let shouldSkip = false;

					// 如果在未闭合的标签内
					if (lastTagStart > lastTagEnd) {
						const tagContent = processedContent.substring(lastTagStart, matchEnd);
						// 检查是否在引号内（属性值中）
						const beforeInTag = tagContent.substring(0, matchStart - lastTagStart);
						const singleQuotes = (beforeInTag.match(/'/g) || []).length;
						const doubleQuotes = (beforeInTag.match(/"/g) || []).length;
						// 如果引号数量是奇数，说明在属性值内
						if (singleQuotes % 2 === 1 || doubleQuotes % 2 === 1) {
							shouldSkip = true;
						}
					}

					// 检查是否是十六进制颜色代码
					if (!shouldSkip && /^[0-9a-fA-F]{6}$/i.test(match[1])) {
						shouldSkip = true;
					}

					if (shouldSkip) {
						result += match[0]; // 保持原样
					} else {
						// 检查#标签前面是否有空格，如果没有就添加一个空格
						const lastChar =
							beforeText.length > 0 ?
							beforeText[beforeText.length - 1] :
							lastIndex > 0 ?
							processedContent[lastIndex - 1] :
							' ';
						const needSpace =
							lastChar !== ' ' && lastChar !== '\n' && lastChar !== '\t' && lastIndex !== 0;
						const space = needSpace ? ' ' : '';
						// 只显示蓝色样式，不添加链接（只显示不跳转）
						result += `${space}<span style="color: #1890ff;">${match[0]}</span>`;
					}

					lastIndex = matchEnd;
				}

				// 添加剩余的文本
				result += processedContent.substring(lastIndex);
				processedContent = result;

				return processedContent;
			},
			/**
			 * 获取一级评论列表
			 * @param {Object} notesId 笔记id
			 */
			async getFirstComment(notesId) {
				if (this.status == 'loading' || this.status == 'nomore') {
					return;
				}
				this.status = 'loading';
				getCommentWithCommentByProductId({
						productId: notesId,
						page: this.page,
						pageSize: this.pageSize
					})
					.then(async res => {
						console.log(res);
						const commentArr = Array.isArray(res.data.records) ? res.data.records : [];
						const mappedArr = commentArr.map(item => {
							// 处理二级评论数据
							let childrenList = [];
							if (item.children && Array.isArray(item.children) && item.children.length >
								0) {
								childrenList = item.children.map(child => ({
									id: child.id,
									parentId: child.pid,
									notesId: child.nid,
									commentUserId: child.uid,
									commentUserName: child.username,
									commentUserAvatar: child.avatar,
									content: this.formatCommentContent(child.content),
									isLike: child.isLike,
									commentLikeNum: child.likeCount,
									commentReplyNum: child.twoCommentCount,
									createTime: weChatTimeFormat(Number(child.createTime))
								}));
							}

							return {
								id: item.id,
								parentId: item.pid,
								notesId: item.nid,
								commentUserId: item.uid,
								commentUserName: item.username,
								commentUserAvatar: item.avatar,
								content: this.formatCommentContent(item.content),
								isLike: item.isLike,
								commentLikeNum: item.likeCount,
								commentReplyNum: item.twoCommentCount,
								createTime: weChatTimeFormat(Number(item.createTime)),
								children: {
									list: childrenList,
									page: 1,
									pageSize: 10,
									status: childrenList.length > 0 ? 'loadmore' : 'nomore',
									loadmoreText: '—— 展开' + (item.twoCommentCount || 0) + '条回复 ——'
								}
							};
						});

						// 处理图片信息
						const processedArr = await Promise.all(
							mappedArr.map(item => {
								return new Promise(resolve => {
									if (item.pictureUrl != null && item.pictureUrl != '') {
										uni.getImageInfo({
											src: item.pictureUrl,
											success: image => {
												item.picture = {
													url: item.pictureUrl
												};
												// 图片长度最长为350rpx,高度最高为350rpx
												if (image.width >= image.height) {
													if (image.width > 350) {
														item.picture.width = 350;
														item.picture.height = (
																350 * image.height
																) / image.width;
													} else {
														item.picture.width = image
															.width;
														item.picture.height = image
															.height;
													}
												} else {
													if (image.height > 350) {
														item.picture.height = 350;
														item.picture.width = (350 *
																image.width) /
															image.height;
													} else {
														item.picture.width = image
															.width;
														item.picture.height = image
															.height;
													}
												}
												resolve(item);
											},
											fail: err => {
												console.log(err);
												item.picture = null;
												resolve(item);
											}
										});
									} else {
										item.picture = null;
										resolve(item);
									}
								});
							})
						);

						setTimeout(() => {
							this.page++;
							if (processedArr.length < this.pageSize) {
								this.status = 'nomore';
							} else {
								this.status = 'loadmore';
							}
							this.commentList = this.commentList.concat(processedArr);
						}, 700);
					})
					.catch(err => {
						console.log(err);
					});
			},

			/**
			 * 评论输入框添加@用户
			 * @param {Object} item
			 */
			addUser(item) {
				this.showAite = false;
				this.$refs.lsjComment.keyboardShow();
				this.edit.addLink({
					prefix: '@',
					name: item.nickname,
					data: {
						userId: item.userId
					}
				});
			},
			/**
			 * 评论输入框添加emoji
			 * @param {Object} name
			 */
			addEmoji(em) {
				// 统一使用 Unicode 文本插入，避免依赖图片资源
				if (this.edit && typeof this.edit.emoji === 'function') {
					this.edit.emoji(em);
				} else if (this.edit && typeof this.edit.insertText === 'function') {
					this.edit.insertText(em);
				} else if (this.edit && typeof this.edit.insertCustomEmoji === 'function') {
					// 兜底：不再依赖图片，直接插入文本
					this.edit.insertText(em);
				}
				this.showEmoji = false;
				this.$refs.lsjComment.keyboardShow();
			},
			/**
			 * 点击 @ 图标：直接在输入框插入一个 @ 符号
			 */
			chooseAite() {
				this.showAite = false;
				this.showEmoji = false;

				if (this.edit && typeof this.edit.insertText === 'function') {
					this.edit.insertText('@');
				} else if (this.edit && typeof this.edit.addLink === 'function') {
					this.edit.addLink({
						prefix: '',
						name: '@',
						data: {}
					});
				} else {
					this.content = (this.content || '') + '@';
				}

				if (this.$refs.lsjComment && typeof this.$refs.lsjComment.keyboardShow === 'function') {
					this.$refs.lsjComment.keyboardShow();
				}
			},
			/**
			 * 获取关注用户列表
			 */
			getAttentionUser() {
				if (this.attentionUser.isNoMore) {
					return;
				}
				this.attentionUser.isLoading = true;
				setTimeout(() => {
					getAttentionList({
							userId: this.userInfo.id,
							pageNum: this.attentionUser.page,
							pageSize: this.attentionUser.pageSize
						})
						.then(res => {
							if (res.code == 20010) {
								console.log(res);
								if (res.data.length < this.attentionUser.pageSize) {
									this.attentionUser.isNoMore = true;
									this.attentionUser.status = 'nomore';
								}
								this.attentionUser.list = this.attentionUser.list.concat(res.data);
								this.attentionUser.page++;
							}
						})
						.finally(() => {
							this.attentionUser.isLoading = false;
						});
				}, 500);
			},
			/**
			 * 打开emoji弹出层
			 */
			openEmoji() {
				if (this.showEmoji) {
					this.showEmoji = false;
					this.$refs.lsjComment.keyboardShow();
				} else {
					this.showAite = false;
					this.showEmoji = true;
					uni.hideKeyboard();
				}
			},
			/**
			 * 选择发布评论的图片
			 */
			chooseImage() {
				uni.chooseImage({
					count: 1,
					sizeType: ['original', 'compressed'],
					sourceType: ['album', 'camera'],
					success: res => {
						console.log(res);
						this.commentImagesurl = res.tempFilePaths[0];
					},
					complete: () => {
						this.$refs.lsjComment.keyboardShow();
					}
				});
			},
			/**
			 * 删除发布评论的图片
			 */
			deleteImage() {
				this.commentImagesurl = '';
				this.$refs.lsjComment.keyboardShow();
			},
			/**
			 * 编辑器初始化
			 * @param {Object} edit
			 */
			editReady(edit) {
				this.edit = edit;
				const reg = /<img/g;
				if (!(this.revealcontent == '\n' || this.revealcontent == '') || reg.test(this.content)) {
					this.edit.ready(this.content);
				}
			},
			/**
			 * swiper切换
			 * @param {Object} e
			 */
			swiperChange(e) {
				this.swiperCurrent = e.detail.current;
			},
			/**
			 * 提取话题名
			 * @param {Object} str
			 */
			findTopicName(str) {
				// #{&quot;topicname&quot;:&quot;蛋仔派对&quot;}
				let reg = /#{&quot;topicname&quot;:&quot;(.+?)&quot;}/g;
				let result = str.match(reg);
				if (result) {
					return result[0].replace(/#{&quot;topicname&quot;:&quot;/g, '').replace(/&quot;}/g, '');
				} else {
					return '';
				}
			},
			/**
			 * 提取用户id
			 * @param {Object} str
			 */
			findUserId(str) {
				// 兼容两种格式：#{"userId":"xxx"} 和 #{&quot;userId&quot;:&quot;xxx&quot;}
				let reg = /#{"userId":"(.+?)"}/g;
				let result = str.match(reg);
				if (result) {
					return result[0].replace(/#{"userId":"/g, '').replace(/"}/g, '');
				} else {
					// 兼容旧格式
					reg = /#{&quot;userId&quot;:&quot;(.+?)&quot;}/g;
					result = str.match(reg);
					if (result) {
						return result[0].replace(/#{&quot;userId&quot;:&quot;/g, '').replace(/&quot;}/g, '');
					}
				}
				return '';
			},
			/**
			 * 点击话题或者用户
			 * @param {Object} e
			 */
			clickTopic(e) {
				if (e.detail.node.name != 'a') {
					return;
				}
				let href = e.detail.node.attrs.href || '';
				// 检查是否是话题链接
				if (href.includes('/pkg-search/pages/topicIndex/topicIndex')) {
					const topicNameMatch = href.match(/topicName=([^&]+)/);
					if (topicNameMatch) {
						const topicName = decodeURIComponent(topicNameMatch[1]);
						uni.navigateTo({
							url: '/pkg-search/pages/topicIndex/topicIndex?topicName=' + encodeURIComponent(
								topicName)
						});
						return;
					}
				}
				// 检查是否是用户链接（新格式，使用userName）
				if (href.includes('/pkg-mine/pages/mine/otherMine')) {
					const userNameMatch = href.match(/userName=([^&]+)/);
					if (userNameMatch) {
						const userName = decodeURIComponent(userNameMatch[1]);
						uni.navigateTo({
							url: '/pkg-mine/pages/mine/otherMine?userName=' + encodeURIComponent(userName)
						});
						return;
					}
					const userIdMatch = href.match(/userId=([^&]+)/);
					if (userIdMatch) {
						const userId = decodeURIComponent(userIdMatch[1]);
						uni.navigateTo({
							url: '/pkg-mine/pages/mine/otherMine?userId=' + userId
						});
						return;
					}
				}
				// 兼容旧格式
				let topicName = this.findTopicName(href);
				let userId = this.findUserId(href);
				if (topicName != '') {
					uni.navigateTo({
						url: '/pkg-search/pages/topicIndex/topicIndex?topicName=' + topicName
					});
				} else if (userId != '') {
					uni.navigateTo({
						url: '/pkg-mine/pages/mine/otherMine?userId=' + userId
					});
				}
			},
			/**
			 * 返回上一页
			 */
			goBack() {
				uni.navigateBack();
			}
		},
		onLoad(options) {
			console.log('idleDetail onLoad options:', options);
			// 🔧 修复：加强参数验证，防止传递 undefined 或无效值
			const productId = options.productId;
			if (!productId || productId === 'undefined' || productId === 'null' || productId.trim() === '') {
				console.error('❌ 商品ID参数无效:', productId);
				uni.showToast({
					title: '商品参数错误',
					icon: 'none',
					duration: 2000
				});
				setTimeout(() => {
					uni.navigateBack();
				}, 2000);
				return;
			}
			// 设置初始高度，防止真机图片显示不全
			this.swipperHeight = 750;
			uni.getSystemInfo({
				success: res => {
					this.applyWeixinNavBar(res, 44);
					this.windowHeight = res.windowHeight;
				}
			});
			// 统一使用内置 Unicode 表情，去掉本地 sqlite 依赖
			this.emojiList = emojiList;
			this.bottomEmoji = emojiList.slice(0, 9);
			uni.onKeyboardHeightChange(res => {
				if (res.height > 0 && this.keyboardHeight == 0) {
					this.keyboardHeight = res.height;
				}
			});
			this.userInfo = uni.getStorageSync('userInfo');
			// 🔧 修复：使用验证后的 productId
			getProductById({
				productId: productId
			}).then(res => {
				const data = res.data;
				// 处理媒体资源：urls[0] 可能是视频
				let notesResources = [];
				if (data.urls) {
					const list = Array.isArray(data.urls) ? data.urls : JSON.parse(data.urls);
					const videoExtReg = /\.(mp4|mov|m4v|webm|ogg|m3u8)(\?.*)?$/i;
					notesResources = list.map((url, idx) => ({
						url,
						width: 750,
						height: 750,
						isVideo: videoExtReg.test(url)
					}));
				}
				// 处理时间
				const createTime = data.time ? weChatTimeFormat(data.time) : '';
				const updateTime = data.time ? weChatTimeFormat(data.time) : '';
				// 统一映射
				this.notesDetail = {
					id: data.id,
					title: data.title,
					content: this.formatContentWithHashtags(data.description || data.content || ''),
					coverPicture: data.cover || data.noteCover || '',
					noteType: Number(data.noteType),
					belongUserId: Number(data.uid),
					nickname: data.username,
					avatarUrl: data.avatar,
					imgList: data.urls ? (Array.isArray(data.urls) ? data.urls : JSON.parse(data.urls)) : [],
					notesResources, // 你可以用 notesResources 替换原本的 notesResources 字段
					likeCount: Number(data.likeCount),
					viewCount: Number(data.viewCount),
					collectionCount: Number(data.collectionCount),
					commentCount: Number(data.commentCount),
					isFollow: data.isFollow,
					isLike: data.isLike,
					isCollect: data.isCollection,
					createTime,
					updateTime,
					// 商品信息字段
					price: data.price || 0,
					originalPrice: data.originalPrice || 0,
					postType: data.postType || 0, // 0: 包邮, 1: 自提
					province: data.province || '',
					city: data.city || '',
					district: data.district || ''
					// 其它字段按需添加
				};
				this.swipperCount = notesResources.length;
				// 添加浏览记录（与笔记详情、闲置视频详情一致）
				if (this.userInfo && this.userInfo.id) {
					addBrowseRecord({
						nid: String(data.id),
						uid: this.userInfo.id
					}).catch(err => {
						console.log('添加浏览记录失败:', err);
					});
				}
				notesResources.forEach(item => {
					if (item.isVideo) {
						// 视频不需要计算图片尺寸，但确保 swiper 高度合理
						if (this.swipperHeight < 750) {
							this.swipperHeight = 750;
						}
						return;
					}
					uni.getImageInfo({
						src: item.url,
						success: image => {
							if (image.width >= image.height) {
								item.width = 750;
								item.height = (750 * image.height) / image.width;
							} else {
								if (image.height > 1000) {
									item.height = 1000;
									item.width = (1000 * image.width) / image.height;
									if (item.width > 750) {
										item.width = 750;
										item.height = (750 * image.height) / image.width;
									}
								} else {
									if (image.width > 750) {
										item.width = 750;
										item.height = (750 * image.height) / image.width;
									} else {
										item.width = image.width;
										item.height = image.height;
									}
								}
							}
							if (item.height > this.swipperHeight) {
								this.swipperHeight = item.height;
							}
						},
						fail: () => {
							// 小程序 getImageInfo 可能因域名未配置或非 https 失败，使用兜底尺寸避免空白
							item.width = 750;
							item.height = 750;
							if (item.height > this.swipperHeight) {
								this.swipperHeight = item.height;
							}
						}
					});
				});
				// res.data.createTime = weChatTimeFormat(res.data.createTime)
				// res.data.updateTime = weChatTimeFormat(res.data.updateTime)
				// setTimeout(() => {
				// 	this.notesDetail = res.data
				// 	this.swipperCount = res.data.notesResources.length
				// }, 500)
			});
			// 评论数量已从 getNotesByNotesId 接口获取，无需单独调用
			this.commentCount = this.notesDetail.commentCount;
			this.getFirstComment(options.productId);
		},
		onReachBottom() {
			this.getFirstComment(this.notesDetail.id);
		},
		onBackPress() {
			if (this.inputField) {
				this.inputField = false;
				return true;
			}
			if (this.showEmoji) {
				this.showEmoji = false;
				return true;
			}
			if (this.showAite) {
				this.showAite = false;
				return true;
			}
			return false;
		}
	};
</script>

<style scoped>
	.authorName {
		color: #2b2b2b;
		font-size: 15px;
		margin-left: 20rpx;
		width: 350rpx;
		word-break: break-all;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.bottom-icon {
		display: flex;
		justify-content: space-around;
		font-size: 35rpx;
		color: #2b2b2b;
		justify-content: space-around;
		padding-right: 10rpx;
	}

	.bottom-edit {
		padding: 0 15rpx;
		background-color: #f3f3f2;
		border-radius: 50px;
		margin: 10rpx 18rpx;
		box-sizing: border-box;
		display: flex;
		align-items: center;
		flex: 1;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	::v-deep .uni-swiper-dots {
		bottom: 30rpx;
	}

	::v-deep .lsjComment .lsj-edit-edit-container {
		background-color: #f3f3f2 !important;
		min-height: 40px !important;
		max-height: 140px !important;
		height: auto !important;
		font-size: 30rpx;
		padding: 30rpx;
		border-radius: 35rpx;
	}

	::v-deep .ql-editor.ql-blank:before {
		font-size: 30rpx;
	}

	::v-deep .editot-pd {
		padding: 15rpx 20rpx !important;
	}

	/* 根据实际需要调整样式 */
	.animate-praise {
		animation: scaleAnimation 0.8s ease;
		/* 使用动画 */
	}

	@keyframes scaleAnimation {
		0% {
			transform: scale(1, 1);
		}

		50% {
			transform: scale(1.2, 1.2);
		}

		100% {
			transform: scale(1, 1);
		}
	}

	.share-item {
		display: inline-flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		padding: 20rpx;
		margin: 0;
		background-color: transparent;
		border: none;
		box-sizing: border-box;
		/* 微信小程序：缩小图标与文字间距 */
		gap: 8rpx;
	}

	/* 微信小程序：让 button 与右侧 view 视觉完全一致，消除突兀感 */
	button.share-item {
		border-radius: 0;
		background-color: transparent;
		font-size: 30rpx;
		font-weight: normal;
		line-height: normal;
		height: auto;
		min-height: 0;
		padding: 20rpx;
		text-align: center;
	}
	button.share-item::after {
		border: none;
	}
</style>