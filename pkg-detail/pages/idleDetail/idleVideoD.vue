<template>
	<view>
		<cover-view :style="{height: statusBarHeight + 'px'}"
			style="position: fixed;top: 0;width: 100%;z-index: 9999;background-color: #000000;">
		</cover-view>
		<view :style="{height: statusBarHeight + 'px'}"></view>
		<!-- 视频分类下的滑动切换模式 -->
		<swiper v-if="fromVideoCategory && videoList.length > 0"
			:style="{height: screenHeight-statusBarHeight-50 + 'px',width: screenWidth + 'px'}"
			:current="currentVideoIndex"
			@change="onVideoSwiperChange"
			:vertical="true"
			:circular="false"
			:duration="300"
			:indicator-dots="false"
			:autoplay="false"
			style="background: #000;"
		>
			<swiper-item v-for="(videoItem, index) in videoList" :key="videoItem.id">
				<view style="position: relative; width: 100vw; height: 100vh; background: #000; overflow: hidden;"
					:style="{height: screenHeight-statusBarHeight-50 + 'px',width: screenWidth + 'px'}" @tap="handleTap">
					<view
						style="display: flex;position: absolute;top: 0;width: 750rpx;height: 44px;align-items: center;padding: 20rpx;justify-content: space-between;box-sizing: border-box;z-index: 99;background-color: transparent;">
						<!-- #ifndef MP-WEIXIN -->
						<u-icon @click="goBack" name="arrow-left" color="#f5f5f5" size="25"></u-icon>
						<!-- #endif -->
						<view style="flex: 1;"></view>
						<u-icon v-if="videoItem.belongUserId!=userInfo.id" name="share" color="#f5f5f5"
							size="27"></u-icon>
						<u-icon v-else name="more-dot-fill" color="#f5f5f5" size="27"></u-icon>
					</view>
					<view style="position: relative;">
						<video v-if="videoItem.videoUrl" :id="'video_' + index" :src="videoItem.videoUrl" :controls="false" :loop="true"
							:muted="muted" :show-play-btn="false" :show-center-play-btn="false" :enable-progress-gesture="false"
							:initial-time="0" :poster="videoItem.coverPicture" @play="onVideoPlay" @pause="onVideoPause"
							@error="onVideoError" @loadedmetadata="onLoadedmetadata" @timeupdate="onTimeUpdate"
							style="width: 100vw; height: 100vh; object-fit: cover; position: absolute; left: 0; top: 0; z-index: 1;">
						</video>

						<!-- 自定义进度条 -->
						<view class="custom-progress custom-progress-in-swiper" @touchstart.stop="onProgressTouchStart"
							@touchmove.stop="onProgressTouchMove" @touchend.stop="onProgressTouchEnd">
							<view class="progress-bar" id="progressBar">
								<view class="progress-inner" :style="{width: progress + '%'}"></view>
								<view class="progress-dot" :style="{left: progress + '%'}" v-show="isDragging"></view>
							</view>
							<text class="time-text">{{currentTime}}/{{totalTime}}</text>
						</view>

						<!-- 加载状态 -->
						<view v-if="isLoading && currentVideoIndex === index"
							style="position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); z-index: 3;">
							<u-loading-icon mode="circle" size="40" color="#ffffff"></u-loading-icon>
						</view>

						<!-- 暂停图标 -->
						<view v-if="showPauseIcon && currentVideoIndex === index"
							style="position: absolute; left: 50%; top: 400px; transform: translate(-50%, -50%); z-index: 3; background-color: rgba(0, 0, 0, 0.3); border-radius: 50%; padding: 20rpx;"
							@click.stop="togglePlay">
							<u-icon name="pause" color="#ffffff" size="60"></u-icon>
						</view>

						<!-- 点击层 -->
						<view style="position: absolute; left: 0; top: 0; width: 100vw; height: 100vh; z-index: 2;"
							@click.stop="togglePlay"></view>
					</view>
					<!-- 右侧操作栏 -->
					<view
						style="position: fixed; right: 20rpx; bottom: 220rpx; z-index: 99; display: flex; flex-direction: column; align-items: center;">
						<!-- 用户头像 -->
						<view style="margin-bottom: 50rpx; position: relative;">
							<u--image :src="videoItem.avatarUrl" shape="circle" width="90rpx" height="90rpx"
								@click="goToOtherMine(videoItem.belongUserId)">
							</u--image>
							<view v-if="videoItem.belongUserId!=userInfo.id&&!videoItem.isFollow" @click="attention"
								style="width: 40rpx; height: 40rpx; background: #e60033; border-radius: 50%; position: absolute; bottom: -20rpx; left: 50%; transform: translateX(-50%); display: flex; align-items: center; justify-content: center;">
								<text style="color: #fff; font-size: 30rpx;">+</text>
							</view>
						</view>

						<!-- 收藏按钮 -->
						<view style="margin-bottom: 40rpx; text-align: center;">
							<u-transition :show="!videoItem.isCollect" mode="fade" duration="2000">
								<u-icon v-if="!videoItem.isCollect" name="/static/collect_white.png" size="50"
									@click="collectNotes(videoItem.id)">
								</u-icon>
							</u-transition>
							<u-transition :show="videoItem.isCollect" mode="fade" duration="2000">
								<u-icon v-if="videoItem.isCollect" name="/static/collect_select.png" size="50"
									@click="collectNotes(videoItem.id)">
								</u-icon>
							</u-transition>
							<view style="font-size: 26rpx; color: #fff; margin-top: 5rpx;">
								{{videoItem.collectionCount || '收藏'}}
							</view>
						</view>

						<!-- 评论按钮 -->
						<view style="margin-bottom: 40rpx; text-align: center;" @click="openComment">
							<u-icon name="/static/comment_white.png" size="50"></u-icon>
							<view style="font-size: 26rpx; color: #fff; margin-top: 5rpx;">
								{{videoItem.commentCount > 0 ? videoItem.commentCount : '评论'}}
							</view>
						</view>

						<!-- 去购买按钮（仅非自己的商品显示） -->
						<view v-if="videoItem.belongUserId != userInfo.id" style="text-align: center;" @click="buyNow">
							<u-icon name="shopping-cart" size="50" color="#fff"></u-icon>
							<view style="font-size: 26rpx; color: #fff; margin-top: 5rpx;">
								去购买
							</view>
						</view>
					</view>

					<!-- 底部视频信息 -->
					<view style="position: absolute; bottom: 180rpx; left: 20rpx; right: 120rpx; z-index: 98;">
						<view style="color: #fff; font-size: 35rpx; font-weight: bold; margin-bottom: 20rpx;">
							@{{videoItem.nickname}}
						</view>
						<rich-text :nodes="videoItem.content || videoItem.description" style="font-size: 28rpx; color: #fff; line-height: 1.5;"
							@itemclick="clickTopic">
						</rich-text>
					</view>
				</view>
			</swiper-item>
		</swiper>
		<!-- 非视频分类下的普通模式 -->
		<view v-else-if="!commentShow"
			style="position: relative; width: 100vw; height: 100vh; background: #000; overflow: hidden;"
			:style="{height: screenHeight-statusBarHeight-50 + 'px',width: screenWidth + 'px'}" @tap="handleTap">
			<view
				style="display: flex;position: absolute;top: 0;width: 750rpx;height: 44px;align-items: center;padding: 20rpx;justify-content: space-between;box-sizing: border-box;z-index: 99;">
				<!-- #ifndef MP-WEIXIN -->
				<u-icon @click="goBack" name="arrow-left" color="#f5f5f5" size="25"></u-icon>
				<!-- #endif -->
				<!-- #ifdef MP-WEIXIN -->
				<view style="flex: 1;"></view>
				<!-- #endif -->
				<u-icon v-if="notesDetail.belongUserId!=userInfo.id" name="share" color="#f5f5f5"
					size="27"></u-icon>
				<u-icon v-else name="more-dot-fill" color="#f5f5f5" size="27"></u-icon>
			</view>
			<!-- <video :src="notesDetail.videoUrl" autoplay controls object-fit="contain" style="width: 750rpx;" loop
				:poster="notesDetail.coverPicture"></video> -->
			<view style="position: relative;">
				<video v-if="notesDetail.videoUrl" ref="myVideo" id="myVideo" :src="notesDetail.videoUrl" :controls="false" :loop="true"
					:muted="muted" :show-play-btn="false" :show-center-play-btn="false" :enable-progress-gesture="false"
					:initial-time="0" :poster="notesDetail.coverPicture" @play="onVideoPlay" @pause="onVideoPause"
					@error="onVideoError" @loadedmetadata="onLoadedmetadata" @timeupdate="onTimeUpdate"
					style="width: 100vw; height: 100vh; object-fit: cover; position: absolute; left: 0; top: 0; z-index: 1;">
				</video>

				<!-- 自定义进度条 -->
				<view class="custom-progress" @touchstart.stop="onProgressTouchStart"
					@touchmove.stop="onProgressTouchMove" @touchend.stop="onProgressTouchEnd">
					<view class="progress-bar" id="progressBar">
						<view class="progress-inner" :style="{width: progress + '%'}"></view>
						<!-- 添加一个拖动点 -->
						<view class="progress-dot" :style="{left: progress + '%'}" v-show="isDragging"></view>
					</view>
					<text class="time-text">{{currentTime}}/{{totalTime}}</text>
				</view>

				<!-- 加载状态 -->
				<view v-if="isLoading" style="position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); z-index: 3; pointer-events: none;">
					<u-loading-icon mode="circle" size="40" color="#ffffff"></u-loading-icon>
				</view>

				<!-- 暂停图标 -->
				<view v-if="showPauseIcon"
					style="position: absolute; left: 50%; top: 400px; transform: translate(-50%, -50%); z-index: 3; background-color: rgba(0, 0, 0, 0.3); border-radius: 50%; padding: 20rpx;"
					@click.stop="togglePlay">
					<u-icon name="pause" color="#ffffff" size="60"></u-icon>
				</view>

				<!-- 点击层 -->
				<view style="position: absolute; left: 0; top: 0; width: 100vw; height: 100vh; z-index: 2;"
					@click.stop="togglePlay"></view>
			</view>
			<!-- 右侧操作栏 -->
			<view
				style="position: fixed; right: 20rpx; bottom: 220rpx; z-index: 99; display: flex; flex-direction: column; align-items: center;">
				<!-- 用户头像 -->
				<view style="margin-bottom: 50rpx; position: relative;">
					<u--image :src="notesDetail.avatarUrl" shape="circle" width="90rpx" height="90rpx"
						@click="goToOtherMine(notesDetail.belongUserId)">
					</u--image>
					<view v-if="notesDetail.belongUserId!=userInfo.id&&!notesDetail.isFollow" @click="attention"
						style="width: 40rpx; height: 40rpx; background: #e60033; border-radius: 50%; position: absolute; bottom: -20rpx; left: 50%; transform: translateX(-50%); display: flex; align-items: center; justify-content: center;">
						<text style="color: #fff; font-size: 30rpx;">+</text>
					</view>
				</view>

				<!-- 收藏按钮 -->
				<view style="margin-bottom: 40rpx; text-align: center;">
					<u-transition :show="!notesDetail.isCollect" mode="fade" duration="2000">
						<u-icon v-if="!notesDetail.isCollect" name="/static/collect_white.png" size="50"
							@click="collectNotes(notesDetail.id)">
						</u-icon>
					</u-transition>
					<u-transition :show="notesDetail.isCollect" mode="fade" duration="2000">
						<u-icon v-if="notesDetail.isCollect" name="/static/collect_select.png" size="50"
							@click="collectNotes(notesDetail.id)">
						</u-icon>
					</u-transition>
					<view style="font-size: 26rpx; color: #fff; margin-top: 5rpx;">
						{{notesDetail.collectionCount || '收藏'}}
					</view>
				</view>

				<!-- 评论按钮 -->
				<view style="margin-bottom: 40rpx; text-align: center;" @click="openComment">
					<u-icon name="/static/comment_white.png" size="50"></u-icon>
					<view style="font-size: 26rpx; color: #fff; margin-top: 5rpx;">
						{{commentCount > 0 ? commentCount : '评论'}}
					</view>
				</view>

				<!-- 去购买按钮（仅非自己的商品显示） -->
				<view v-if="notesDetail.belongUserId != userInfo.id" style="text-align: center;" @click="buyNow">
					<u-icon name="shopping-cart" size="50" color="#fff"></u-icon>
					<view style="font-size: 26rpx; color: #fff; margin-top: 5rpx;">
						去购买
					</view>
				</view>
			</view>

			<!-- 底部视频信息 -->
			<view style="position: absolute; bottom: 100rpx; left: 20rpx; right: 120rpx; z-index: 99;">
				<view style="color: #fff; font-size: 35rpx; font-weight: bold; margin-bottom: 20rpx;">
					@{{notesDetail.nickname}}
				</view>
				<rich-text :nodes="notesDetail.content" style="font-size: 28rpx; color: #fff; line-height: 1.5;"
					@itemclick="clickTopic">
				</rich-text>
			</view>

			<!-- 底部评论区弹出层 -->
			<u-popup :show="commentShow" mode="bottom" @close="closeComment" :round="20"
				style="background: #fff;">
				<!-- 评论区内容保持不变 -->
			</u-popup>
			<!-- <view style="position: absolute; bottom: 0; left: 0; width: 100vw; z-index: 10; padding: 32rpx;">
				<view style="display: flex;align-items: center;">
					<u--image :src="notesDetail.avatarUrl" shape="circle" height="70rpx" width="70rpx"></u--image>
					<text style="color: #f5f5f5;font-size: 33rpx;margin-left: 20rpx;">{{notesDetail.nickname}}</text>
					<view v-if="notesDetail.belongUserId!=userInfo.id&&!notesDetail.isFollow" @click="attention"
						style="background-color: #e60033;border-radius: 50rpx;padding: 10rpx 25rpx;margin-left: 20rpx;text-align: center;color: #f5f5f5;font-size: 28rpx;">
						关注
					</view>
					<view v-else-if="notesDetail.belongUserId!=userInfo.id&&notesDetail.isFollow" @click="attention"
						style="background-color: rgba(0, 0, 0, 0);border-radius: 50rpx;padding: 10rpx 25rpx;margin-left: 20rpx;border-style: solid;border-color: #f5f5f5;border-width: 1rpx;text-align: center;color: #f5f5f5;font-size: 28rpx;">
						已关注
					</view>
				</view>
				<view style="margin-top: 20rpx;padding: 10rpx;box-sizing: border-box;">
					<rich-text :nodes="notesDetail.content" style="font-size: 15px;color: #f5f5f5;"
						@itemclick="clickTopic"></rich-text>
				</view>
			</view> -->
		</view>
		<view v-if="commentShow" :style="{height: (screenHeight-statusBarHeight)*(1/3)-20+'px'}"
			style="display: flex;align-items: center;justify-content: center;background-color: #000000;z-index: 99999;width: 750rpx;">
			<!-- <video :src="notesDetail.notesResources[0].url" autoplay controls object-fit="contain" loop
				:style="{height: (screenHeight-statusBarHeight)*(1/3)-20+'px',width: 100+'%'}"
				:poster="notesDetail.coverPicture">
			</video> -->
			<video :src="notesDetail.url" autoplay controls object-fit="contain" loop
				:style="{height: (screenHeight-statusBarHeight)*(1/3)-20+'px',width: 100+'%'}"
				:poster="notesDetail.coverPicture">
			</video>
		</view>
		<view v-if="!commentShow"
			style="display: flex;padding: 20rpx;width: 750rpx;box-sizing: border-box;height: 50px;background-color: #000000;align-items: center;">
			<!-- <view
				style="padding: 15rpx 40rpx;background-color: #241a08;border-radius: 50rpx;color: #949495;font-size: 30rpx;text-align: center;">
				发条弹幕吧
			</view>
			<view style="display: flex;flex: 1;justify-content: space-around;text-align: center;align-items: center;">
				<view style="display: flex;align-items: center;">
					<u-transition :show="!notesDetail.isLike" mode="fade" duration="2000">
						<u-icon v-if="!notesDetail.isLike" name="/static/praise_white.png" size="28"
							@click="praiseNotes(notesDetail.id)"></u-icon>
					</u-transition>
					<u-transition :show="notesDetail.isLike" mode="fade" duration="2000">
						<u-icon v-if="notesDetail.isLike" name="/static/praise_select.png" size="28"
							@click="praiseNotes(notesDetail.id)"></u-icon>
					</u-transition>
					<text v-if="notesDetail.notesLikeNum>0"
						style="color: #f5f5f5;font-size: 30rpx;margin-left: 10rpx;">{{notesDetail.notesLikeNum}}</text>
					<text v-else style="color: #f5f5f5;font-size: 30rpx;margin-left: 10rpx;">点赞</text>
				</view>
				<view style="display: flex;align-items: center;">
					<u-transition :show="!notesDetail.isCollect" mode="fade" duration="2000">
						<u-icon v-if="!notesDetail.isCollect" name="/static/collect_white.png" size="28"
							@click="collectNotes(notesDetail.id)"></u-icon>
					</u-transition>
					<u-transition :show="notesDetail.isCollect" mode="fade" duration="2000">
						<u-icon v-if="notesDetail.isCollect" name="/static/collect_select.png" size="28"
							@click="collectNotes(notesDetail.id)"></u-icon>
					</u-transition>
					<text v-if="notesDetail.notesCollectNum>0"
						style="color: #f5f5f5;font-size: 30rpx;margin-left: 10rpx;">{{notesDetail.notesCollectNum}}</text>
					<text v-else style="color: #f5f5f5;font-size: 30rpx;margin-left: 10rpx;">收藏</text>
				</view>
				<view style="display: flex;align-items: center;" @click="openComment">
					<u-icon name="/static/comment_white.png" size="28"></u-icon>
					<text v-if="commentCount>0"
						style="color: #f5f5f5;font-size: 30rpx;margin-left: 10rpx;">{{commentCount}}</text>
					<text v-else style="color: #f5f5f5;font-size: 30rpx;margin-left: 10rpx;">评论</text>
				</view>
			</view> -->
		</view>
		<u-popup :show="commentShow" mode="bottom" @close="closeComment">
			<u-popup :show="inputField" mode="bottom" @close="closeEditor" zIndex="11000">
				<view style="width: 100%;box-sizing: border-box;position: fixed;bottom: 0;background-color: #fff;">
					<view @click="onEditClick">
						<lsj-edit class="lsjComment" style="height: auto" ref="lsjComment"
							:placeholder="editPlaceholder" :maxCount="1000" @onReady="editReady">
						</lsj-edit>
					</view>
					<view style="display: flex;width: 100%;padding: 0 10rpx;box-sizing: border-box;height: 34px;">
						<view style="display: flex;justify-content: space-around;width: 50%;align-items: center;">
							<view @touchend.prevent="chooseAite">
								<u-icon name="/static/aite.png" size="25"></u-icon>
							</view>
							<view @touchend.prevent="openEmoji">
								<u-icon name="/static/emoji.png" size="25" :color="showEmoji ? '#3d8af5' : '#666'"></u-icon>
							</view>
							<view @touchend.prevent="chooseImage">
								<u-icon name="/static/photo.png" size="25"></u-icon>
							</view>
						</view>
						<view style="display: flex;margin-left: auto;margin-right: 20rpx;align-items: center;"
							@touchend.prevent="sendComment">
							<u-button style="height: 28px;" size="mini" type="primary" shape="circle">发送</u-button>
						</view>
					</view>
					<view v-if="commentImagesurl!=''" style="padding: 30rpx;display: flex;">
						<view style="position: relative;">
							<image :src="commentImagesurl" style="height: 100rpx;width: 100rpx;" mode="aspectFill">
							</image>
							<view
								style="position: absolute;top: 0;right: 0;background-color: #7d7d7d;padding: 5rpx;border-bottom-left-radius: 50%;border-top-right-radius: 5rpx;">
								<u-icon name="close" size="9" color="#f3f3f2" @click="deleteImage"></u-icon>
							</view>
						</view>
					</view>
					<scroll-view :scroll-x="true"
						style="background-color: #fff;display: flex;align-items: center;white-space: nowrap;"
						v-if="bottomEmoji.length>0 && !showEmoji">
						<view style="display: inline-flex;align-items: center;padding: 0 15rpx;">
							<block v-for="(item,index) in bottomEmoji" v-bind:key="index">
								<view @click="addEmoji(item.name)" style="padding: 15rpx;">
									<image :src="item.url" style="width: 70rpx;height: 70rpx;" mode="widthFix"
										lazy-load>
									</image>
								</view>
							</block>
						</view>
					</scroll-view>
					<view v-if="showEmoji">
						<scroll-view :style="{height: keyboardHeight+'px'}" scroll-y
							style="background-color: aliceblue;">
							<view
								style="display: grid;padding: 20rpx;;grid-template-columns: repeat(5,1fr);gap: 20rpx;text-align: center;">
								<block v-for="(item,index) in emojiList" v-bind:key="index">
									<view @click="addEmoji(item.name)">
										<image :src="item.url" style="width: 100rpx;height: 100rpx;" mode="widthFix"
											lazy-load>
										</image>
									</view>
								</block>
							</view>
						</scroll-view>
					</view>
					<view v-if="showAite" style="background-color: #f3f3f2;">
						<scroll-view style="height: 500rpx;" @scrolltolower="getAttentionUser">
							<view v-if="attentionUser.list.length>0" style="padding: 30rpx;">
								<block v-for="(item,index) in attentionUser.list" v-bind:key="item.id">
									<view style="display: flex;padding: 10rpx 0;align-items: center;">
										<image :src="item.avatarUrl"
											style="height: 90rpx;width: 90rpx;border-radius: 50%;">
										</image>
										<view
											style="display: flex;flex-direction: column;justify-content: space-between;margin-left: 20rpx;flex: 1;">
											<view style="font-size: 30rpx;color: #2b2b2b;">{{item.nickname}}</view>
										</view>
										<view style="margin-left: auto;">
											<view
												style="width: 150rpx;height: 60rpx;line-height: 60rpx;text-align: center;border-radius: 30rpx;background-color: #3d8af5;color: #ffffff;font-size: 25rpx;"
												@click="addUser(item)">@Ta</view>
										</view>
									</view>
									<u-divider></u-divider>
								</block>
								<view style="margin-top: 70rpx;">
									<u-loadmore :status="attentionUser.status" loadingIcon="spinner" line></u-loadmore>
								</view>
							</view>
							<view v-else
								style="display: flex;justify-content: center;align-items: center;height: 500rpx;">
								<view style="font-size: 25rpx;color: #afafb0;">暂无数据</view>
							</view>
						</scroll-view>
					</view>
				</view>
			</u-popup>
			<view style="background-color: #ffffff;" :style="{height: (screenHeight-statusBarHeight)*(2/3)+20 + 'px'}">
				<view v-if="commentShow"
					style="display: flex;align-items: center;position: fixed;bottom: 0;background-color: #ffffff;padding: 20rpx;box-sizing: border-box;width: 750rpx;z-index: 9999;">
					<view @click="replyNotes"
						style="width: 100%;background-color: #f3f3f2;padding: 20rpx;border-radius: 70rpx;height: 100%;box-sizing: border-box;display: flex;align-items: center;">
						<view style="font-size: 30rpx;color: #afafb0;margin-left: 10rpx;">爱评论的人运气都不差</view>
						<view style="display: flex;justify-content: space-around;flex: 1;padding: 0 10rpx 0 50rpx;">
							<u-icon name="/static/aite.png" size="25"></u-icon>
							<u-icon name="/static/emoji.png" size="25"></u-icon>
							<u-icon name="/static/photo.png" size="25"></u-icon>
						</view>
					</view>
				</view>
				<view
					style="display: flex;justify-content: center;position: relative;height: 35px;padding: 15rpx;text-align: center;align-items: center;">
					<view style="font-size: 34rpx;letter-spacing: 2rpx;">共{{commentCount || 0}}条评论</view>
					<u-icon style="position: absolute;right: 20rpx;" name="close" size="21"
						@click="closeComment"></u-icon>
				</view>
				<scroll-view scroll-y @scrolltolower="getMoreComment"
					:style="{height: (screenHeight-statusBarHeight)*(2/3)-130 + 'px'}">
					<view style="padding: 30rpx;">
						<block v-for="(item,index) in commentList" v-bind:key="item.id">
							<view style="display: flex;align-items: flex-start;">
								<image :src="item.commentUserAvatar"
									style="height: 70rpx;width: 70rpx;border-radius: 50%;" mode="aspectFill"
									@click="goToOtherMine(item.commentUserId)"></image>
								<view style="flex: 1;padding: 0 10px;word-break: break-all;text-overflow: ellipsis;">
									<view style="display: flex;font-size: 26rpx;color: #afafb0;">
										<view
											style="word-break: break-all;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;max-width: 300rpx;"
											@click="goToOtherMine(item.commentUserId)">
											{{item.commentUserName}}
										</view>
										<view v-if="item.commentUserId==notesDetail.belongUserId"
											style="margin-left: 10rpx;padding: 4rpx 10rpx;background-color: #f3f3f2;color: #7d7d7d;border-radius: 50rpx;white-space: nowrap;width: 50rpx;text-align: center;">
											作者</view>
										<view v-else-if="item.commentUserId==userInfo.id"
											style="margin-left: 10rpx;padding: 4rpx 10rpx;background-color: #f3f3f2;color: #7d7d7d;border-radius: 50rpx;white-space: nowrap;width: 30rpx;text-align: center;">
											我
										</view>
									</view>
									<rich-text style="font-size: 14px;letter-spacing: 0.05rem;color: #383c3c;"
										:nodes="item.content" @longpress="openCommentSetting(item,0)"
										@touchend="touchend" @click="replyFirstComment(item)"
										@itemclick="clickUser"></rich-text>
									<view v-if="item.pictureUrl!=null&&item.pictureUrl!=''" style="margin-top: 10rpx;"
										@click="previewImage(item.picture.url)">
										<image :src="item.picture.url"
											:style="{height: item.picture.height + 'rpx',width: item.picture.width + 'rpx'}"
											style="border-radius: 20rpx;" mode="aspectFill"></image>
									</view>
									<view style="display: flex;align-items: center;margin-top: 10rpx;"
										@longpress="openCommentSetting(item,0)" @touchend="touchend"
										@click="replyFirstComment(item)">
										<view style="font-size: 24rpx;color: #afafb0;">{{item.createTime}}</view>
										<view style="margin-left: 20rpx;font-size: 24rpx;color: #afafb0;">
											{{item.province}}
										</view>
										<view style="margin-left: 20rpx;font-size: 24rpx;color: #7d7d7d;">回复</view>
									</view>
									<view v-if="item.isTop"
										style="background-color: #fdeff2;padding: 5rpx 10rpx;border-radius: 50rpx;font-size: 22rpx;color: #3d8af5;display: inline-block">
										置顶评论
									</view>
									<view v-if="item.commentReplyNum>0" style="margin-top: 10rpx;">
										<block v-for="(item2,index2) in item.children.list" v-bind:key="item2.id">
											<view
												style="display: flex;align-items: flex-start;margin-top: 10rpx;position: relative;">
												<image :src="item2.commentUserAvatar"
													style="height: 50rpx;width: 50rpx;border-radius: 50%;"
													mode="aspectFill" @click="goToOtherMine(item2.commentUserId)">
												</image>
												<view
													style="flex: 1;padding: 0 20rpx;word-break: break-all;overflow: hidden;text-overflow: ellipsis;">
													<view
														style="display: flex;font-size: 26rpx;color: #afafb0;width: 350rpx;">
														<view
															style="word-break: break-all;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;max-width: 300rpx;"
															@click="goToOtherMine(item2.commentUserId)">
															{{item2.commentUserName}}
														</view>
														<view v-if="item2.commentUserId==notesDetail.belongUserId"
															style="margin-left: 10rpx;padding: 4rpx 10rpx;background-color: #f3f3f2;color: #7d7d7d;border-radius: 50rpx;white-space: nowrap;width: 50rpx;text-align: center;">
															作者</view>
														<view v-else-if="item2.commentUserId==userInfo.id"
															style="margin-left: 10rpx;padding: 4rpx 10rpx;background-color: #f3f3f2;color: #7d7d7d;border-radius: 50rpx;white-space: nowrap;width: 30rpx;text-align: center;">
															我
														</view>
													</view>
													<rich-text
														style="font-size: 14px;letter-spacing: 0.05rem;color: #383c3c;"
														@longpress="openCommentSetting(item2,item)" @touchend="touchend"
														:nodes="item2.content" @click="replySecondComment(item,item2)"
														@itemclick="clickUser"></rich-text>
													<view v-if="item2.pictureUrl!=null&&item2.pictureUrl!=''"
														style="margin-top: 10rpx;"
														@click="previewImage(item2.picture.url)">
														<image :src="item2.picture.url"
															:style="{height: item2.picture.height + 'rpx',width: item2.picture.width + 'rpx'}"
															style="border-radius: 20rpx;" mode="aspectFill"></image>
													</view>
													<view style="display: flex;align-items: center;margin-top: 10rpx;"
														@longpress="openCommentSetting(item2,item)" @touchend="touchend"
														@click="replySecondComment(item,item2)">
														<view style="font-size: 24rpx;color: #afafb0;">
															{{item2.createTime}}
														</view>
														<view
															style="margin-left: 20rpx;font-size: 24rpx;color: #afafb0;">
															{{item2.province}}
														</view>
														<view
															style="margin-left: 20rpx;font-size: 24rpx;color: #7d7d7d;">
															回复</view>
													</view>
												</view>
												<view
													style="width: 30px;display: flex;flex-direction: column;justify-content: center;text-align: center;padding: 5rpx;box-sizing: border-box;position: absolute;right: -40px;top: 0;">
													<u-transition :show="!item2.isLike" mode="fade" duration="2000">
														<u-icon v-if="!item2.isLike" name="/static/praise.png" size="24"
															@click="praiseComment(item2.id,item2.commentUserId,2,[index,index2])"></u-icon>
													</u-transition>
													<u-transition :show="item2.isLike" mode="fade" duration="2000">
														<u-icon v-if="item2.isLike" name="/static/praise_select.png"
															size="24"
															@click="praiseComment(item2.id, item2.commentUserId, 2, [index,index2])"></u-icon>
													</u-transition>
													<view v-if="item2.commentLikeNum>0"
														style="font-size: 12px;color: #7d7d7d;">
														{{item2.commentLikeNum}}
													</view>
												</view>
											</view>
										</block>
									</view>
									<u-loadmore v-if="item.commentReplyNum>0" :fontSize="13" color="#5b7e91"
										style="width: 350rpx;letter-spacing: 0.05rem;" :status="item.children.status"
										:loading-text="loadingText" :loadmore-text="item.children.loadmoreText"
										nomore-text=" " @loadmore="getReplyList(item.id)" />
								</view>
								<view
									style="width: 30px; display: flex; flex-direction: column; justify-content: center; text-align: center; padding: 5rpx; box-sizing: border-box;">
									<u-transition :show="!item.isLike" mode="fade" duration="2000">
										<u-icon v-if="!item.isLike" name="/static/praise.png" size="24"
											@click="praiseComment(item.id,item.commentUserId,1,[index])"></u-icon>
									</u-transition>
									<u-transition :show="item.isLike" mode="fade" duration="2000">
										<u-icon v-if="item.isLike" name="/static/praise_select.png" size="24"
											@click="praiseComment(item.id, item.commentUserId, 1, [index])"></u-icon>
									</u-transition>
									<view v-if="item.commentLikeNum > 0" style="font-size: 12px; color: #7d7d7d;">
										{{ item.commentLikeNum }}
									</view>
								</view>
							</view>
							<u-divider style="padding-left: 100rpx;" :hairline="true"></u-divider>
						</block>
						<u-loadmore line :status="status" :loading-text="loadingText"
							loadingIcon="semicircle"></u-loadmore>
					</view>
				</scroll-view>
			</view>
		</u-popup>
	</view>
</template>

<script>
	import {
		emojiList
	} from '@/utils/emojiUtil.js'
	import parseHtml from '@/utils/html-parse.js'
	import {
		getProductById,
		praiseOrCancelIdles,
		getVideoProduct
	} from '@/apis/idles_service.js'
	import {
		weChatTimeFormat,
		replaceHTMLTags,
	} from '@/utils/util.js'
	import {
		getAttentionList,
		updateAttention
	} from '@/apis/user_service'
	import {
		addComment,
		getCommentWithCommentByProductId,
		getCommentSecondListByNotesId,
		praiseOrCancelComment,
		setNotesTopComment,
		deleteNotesComment
	} from '@/apis/comment_service'
	import {
		addBrowseRecord
	} from '@/apis/browse_record_service'
	import {
		getPaymentGlobalConfig
	} from '@/apis/order_service.js'
	import {
		baseUrl
	} from '@/.env.js'
	export default {
		data() {
			return {
				screenHeight: 0,
				screenWidth: 0,
				statusBarHeight: 0,
				commentShow: false,
				editPlaceholder: '爱评论的人运气都不差',
				notesDetail: {},
				commentCount: 0,
				commentList: [],
				page: 1,
				pageSize: 10,
				status: 'loadmore',
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
				isLongPress: false,
				isPlaying: false, // 记录当前播放状态
				showPauseIcon: false, // 新增
				videoContext: null, // 小程序用
				isLoading: false, // 添加加载状态（初始为false，避免显示圆圈）
				lastTapTime: 0,
				progress: 0,
				currentTime: '00:00',
				totalTime: '00:00',
				showLikeAnimation: false,
				duration: 0,
				isDragging: false,
				progressBarRect: null,
				autoplayAttempted: false,
				muted: false, // 默认静音
				fromVideoCategory: false, // 是否从视频分类进入
				videoList: [], // 视频列表
				currentVideoIndex: 0, // 当前视频索引
				videoPage: 1, // 视频列表页码
				videoPageSize: 20, // 视频列表每页数量
				loadingMoreVideo: false, // 是否正在加载更多视频
				hasMoreVideo: true, // 是否还有更多视频
			}
		},
		onReady() {
			// 如果是视频分类模式，等待视频列表加载完成后再初始化
			if (this.fromVideoCategory && this.videoList.length > 0) {
				return;
			}
			this.$nextTick(() => {
				this.videoContext = uni.createVideoContext('myVideo', this);
				setTimeout(() => {
					if (this.notesDetail?.videoUrl) {
						this.initVideoPlay();
					}
				}, 300);
			});
		},
		mounted() {
			// 如果是视频分类模式，等待视频列表加载完成后再初始化
			if (this.fromVideoCategory && this.videoList.length > 0) {
				return;
			}
			this.videoContext = uni.createVideoContext('myVideo', this);
			setTimeout(() => {
				if (this.notesDetail?.videoUrl) {
					this.initVideoPlay();
				}
			}, 300);
		},
		methods: {
			// 初始化视频播放
			initVideoPlay() {
				if (this.autoplayAttempted) return;
				this.autoplayAttempted = true;
				
				// 如果是视频分类模式，不使用这个方法
				if (this.fromVideoCategory) {
					this.isLoading = false;
					return;
				}

				// 统一使用 videoContext
				if (!this.videoContext) {
					this.videoContext = uni.createVideoContext('myVideo', this);
				}

				try {
					this.videoContext.play();
					this.isPlaying = true;
					this.showPauseIcon = false;
					this.isLoading = false;
				} catch (err) {
					console.log('播放失败', err);
					this.handlePlayFailed();
				}
			},
			// 处理播放失败
			handlePlayFailed() {
				this.isPlaying = false;
				this.showPauseIcon = true;
				this.isLoading = false;

				uni.showToast({
					title: '点击屏幕播放视频',
					icon: 'none',
					duration: 2000
				});
			},
			// 切换播放状态
			togglePlay() {
				let videoContext = this.videoContext;
				
				if (this.fromVideoCategory && this.videoList.length > 0) {
					const videoId = `video_${this.currentVideoIndex}`;
					videoContext = uni.createVideoContext(videoId, this);
				} else if (!videoContext) {
					videoContext = uni.createVideoContext('myVideo', this);
					this.videoContext = videoContext;
				}

				if (this.isPlaying) {
					videoContext.pause();
					this.isPlaying = false;
					this.showPauseIcon = true;
				} else {
					// 用户交互时取消静音
					if (this.muted) {
						this.muted = false;
						videoContext.muted = false;
					}
					videoContext.play();
					this.isPlaying = true;
					this.showPauseIcon = false;
				}
			},
			// 处理播放错误
			handlePlayError() {
				// 关闭可能存在的loading
				try {
					uni.hideLoading();
				} catch (e) {
					// 忽略hideLoading错误
				}
				
				uni.showToast({
					title: '视频加载失败',
					icon: 'none',
					duration: 2000
				});
			},
			// 视频播放状态回调
			onVideoPlay() {
				console.log('视频开始播放');
				this.isPlaying = true;
				this.showPauseIcon = false;
				this.isLoading = false;
			},

			onVideoPause() {
				console.log('视频暂停');
				this.isPlaying = false;
				this.showPauseIcon = true;
			},

			onVideoError(e) {
				console.log('视频加载失败', e);
				console.log('视频URL:', this.notesDetail?.videoUrl);
				// 如果 videoUrl 为空，说明数据还没加载，忽略此错误
				if (!this.notesDetail?.videoUrl) {
					console.log('视频URL为空，忽略此错误');
					return;
				}
				this.handlePlayError();
			},
			beforeDestroy() {
				if (this.videoContext) {
					this.videoContext.pause();
				}
			},
			// 视频加载完成
			onLoadedmetadata(e) {
				console.log('视频加载完成');
				this.isLoading = false;
			},
			// 视频播放出错
			onPlayError(e) {
				console.log('视频播放失败', e);
				this.isPlaying = false;
				this.showPauseIcon = true;
				uni.showToast({
					title: '视频播放失败，请重试',
					icon: 'none'
				});
			},
			// 处理双击事件
			handleTap(e) {
				const now = Date.now();
				if (now - this.lastTapTime < 300) {
					// 双击事件
					this.showLikeAnimation = true;
					this.praiseNotes(this.notesDetail.id);
					setTimeout(() => {
						this.showLikeAnimation = false;
					}, 500);
				}
				this.lastTapTime = now;
			},
			// 视频进度更新
			onTimeUpdate(e) {
				// 如果正在拖动，不更新进度
				if (this.isDragging) return;
				
				// 在视频分类模式下，只更新当前视频的进度
				if (this.fromVideoCategory && this.videoList.length > 0) {
					const videoId = e.target.id;
					const currentVideoId = `video_${this.currentVideoIndex}`;
					if (videoId !== currentVideoId) {
						return; // 不是当前视频的进度更新，忽略
					}
				}

				const currentTime = e.detail.currentTime;
				const duration = e.detail.duration;

				this.duration = duration;
				this.progress = (currentTime / duration) * 100;
				this.currentTime = this.formatTime(currentTime);
				this.totalTime = this.formatTime(duration);
			},

			// 格式化时间
			formatTime(seconds) {
				const minutes = Math.floor(seconds / 60);
				const remainingSeconds = Math.floor(seconds % 60);
				return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
			},

			// 点击进度条
			onProgressClick(e) {
				if (!this.videoContext) return;

				// 获取进度条的位置信息
				const query = uni.createSelectorQuery();
				query.select('#progressBar').boundingClientRect(data => {
					if (!data) return;

					// 计算点击位置相对于进度条的偏移
					const touch = e.touches[0];
					const clickX = touch.clientX - data.left;
					const percentage = (clickX / data.width) * 100;

					// 设置视频进度
					if (this.duration) {
						this.videoContext.seek((percentage / 100) * this.duration);
					}
				}).exec();
			},
			// 开始拖动
			onProgressTouchStart(e) {
				this.isDragging = true;
				// 获取进度条位置信息
				const query = uni.createSelectorQuery();
				query.select('#progressBar').boundingClientRect(data => {
					this.progressBarRect = data;
					this.updateProgressByTouch(e.touches[0].clientX);
				}).exec();
			},

			// 拖动中
			onProgressTouchMove(e) {
				if (!this.isDragging) return;
				this.updateProgressByTouch(e.touches[0].clientX);
			},

			// 结束拖动
			onProgressTouchEnd(e) {
				if (!this.isDragging) return;
				// 使用 changedTouches 来获取最后的触摸位置
				if (e.changedTouches && e.changedTouches[0]) {
					this.updateProgressByTouch(e.changedTouches[0].clientX, true);
				}
				this.isDragging = false;
			},
			// 根据触摸位置更新进度
			updateProgressByTouch(clientX, isEnd = false) {
				if (!this.progressBarRect || !this.duration) return;

				let clickX = clientX - this.progressBarRect.left;

				// 限制范围
				clickX = Math.max(0, Math.min(clickX, this.progressBarRect.width));

				// 计算百分比
				const percentage = (clickX / this.progressBarRect.width) * 100;

				// 更新进度条显示
				this.progress = percentage;

				// 更新视频时间
				const newTime = (percentage / 100) * this.duration;
				this.currentTime = this.formatTime(newTime);

				// 如果是触摸结束，则设置视频进度
				if (isEnd) {
					let videoContext = this.videoContext;
					if (this.fromVideoCategory && this.videoList.length > 0) {
						const videoId = `video_${this.currentVideoIndex}`;
						videoContext = uni.createVideoContext(videoId, this);
					}
					if (videoContext) {
						videoContext.seek(newTime);
					}
				}
			},
			/**
			 * 关注用户
			 */
			attention() {
				if (!this.userInfo || !this.userInfo.id) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					return
				}
				
				const targetVideo = this.fromVideoCategory && this.videoList.length > 0
					? this.videoList[this.currentVideoIndex]
					: this.notesDetail;
				
				if (!targetVideo) return;
				
				const isCurrentlyFollowing = targetVideo.isFollow;
				
				updateAttention({
					userId: this.userInfo.id,
					targetUserId: targetVideo.belongUserId
				}).then(res => {
					console.log('关注接口返回:', res)
					// 兼容不同的返回码
					if (res.code === 200 || res.data) {
						if (this.fromVideoCategory && this.videoList.length > 0) {
							const index = this.currentVideoIndex;
							this.videoList[index].isFollow = !this.videoList[index].isFollow;
							// 同步更新当前显示的详情
							this.notesDetail.isFollow = this.videoList[index].isFollow;
						} else {
							this.notesDetail.isFollow = !this.notesDetail.isFollow;
						}
						// 强制视图更新
						this.$forceUpdate();
						uni.showToast({
							title: isCurrentlyFollowing ? '取消关注成功' : '关注成功',
							icon: 'none'
						})
					} else {
						console.error('关注失败，返回码:', res.code)
						uni.showToast({
							title: res.msg || '操作失败',
							icon: 'none'
						})
					}
				}).catch(err => {
					console.error('关注接口错误:', err)
					uni.showToast({
						title: '网络错误',
						icon: 'none'
					})
				})
			},
			openCommentSetting(e, e1) {
				this.isLongPress = true
				let itemList = ['置顶', '回复', '复制', '删除']
				if ((this.notesDetail.belongUserId != this.userInfo.id) || e.parentId != 0) {
					itemList.shift()
				} else {
					if (e.isTop) {
						itemList[0] = '取消置顶'
					}
				}
				if (this.notesDetail.belongUserId != this.userInfo.id && e.commentUserId != this.userInfo.id) {
					itemList.pop()
				}
				uni.showActionSheet({
					itemList: itemList,
					success: (data) => {
						let text = itemList[data.tapIndex]
						console.log(text)
						if (text === '置顶') {
							this.topComment(e.id, 0)
						}
						if (text === '取消置顶') {
							this.topComment(e.id, 1)
						}
						if (text === '回复') {
							if (e1 == 0) {
								this.replyFirstComment(e)
							} else {
								this.replySecondComment(e1, e)
							}
						}
						if (text === '复制') {
							let a = replaceHTMLTags(e.content)
							uni.setClipboardData({
								data: a,
								success: function() {
									uni.showToast({
										title: '复制成功',
										icon: 'none'
									})
								}
							})
						}
						if (text === '删除') {
							this.deleteComment(e)
						}
					}
				})
			},
			/** 置顶评论
			 * @param {Object} id
			 */
			topComment(id, type) {
				uni.showModal({
					title: '提示',
					content: type == 0 ? '是否置顶评论?' : '是否取消置顶评论?',
					confirmText: "确定",
					cancelText: "取消",
					success: (res) => {
						if (res.confirm) {
							setNotesTopComment({
								commentId: id,
							}).then(res => {
								console.log(res)
								if (res.code == 200) {
									uni.showToast({
										title: type == 0 ? '置顶成功' : '取消置顶成功',
										icon: 'none'
									})
									// 将置顶的评论放到第一位
									this.commentList.forEach((item, index) => {
										if (item.id == id) {
											if (type == 0) {
												item.isTop = true
												this.commentList.splice(index, 1)
												this.commentList.unshift(item)
											} else {
												item.isTop = false
											}
										} else {
											item.isTop = false
										}
									})
								} else {
									uni.showToast({
										title: res.msg == null ? type == 0 ? '置顶失败' : '取消置顶失败' : res.msg,
										icon: 'none'
									})
								}
							})
						}
					}
				})
			},
			/** 删除评论
			 * @param {Object} e
			 */
			deleteComment(e) {
				uni.showModal({
					title: '提示',
					content: '是否删除评论?',
					confirmText: "确定",
					cancelText: "取消",
					success: (res) => {
						if (res.confirm) {
							deleteNotesComment({
								commentId: e.id,
							}).then(res => {
								console.log(res)
								if (res.code == 200) {
									uni.showToast({
										title: '删除成功',
										icon: 'none'
									})
									// 删除评论
									if (e.parentId == '0') {
										this.commentList.forEach((item, index) => {
											if (item.id == e.id) {
												this.commentList.splice(index, 1)
											}
										})
									} else {
										this.commentList.forEach((item, index) => {
											if (item.id == e.parentId) {
												item.children.list.forEach((item2, index2) => {
													if (item2.id == e.id) {
														item.children.list.splice(index2, 1)
													}
												})
											}
										})
									}
								} else {
									uni.showToast({
										title: res.msg == null ? '删除失败' : res.msg,
										icon: 'none'
									})
								}
							})
						}
					}
				})
			},
			touchend() {
				setTimeout(() => {
					this.isLongPress = false
				}, 200)
			},
			/**
			 * 点赞或取消点赞商品
			 * @param {Object} id
			 */
			praiseNotes(id) {
				const targetVideo = this.fromVideoCategory && this.videoList.length > 0
					? this.videoList.find(v => v.id === id) || this.notesDetail
					: this.notesDetail;
				
				praiseOrCancelIdles({
					notesId: id,
					userId: this.userInfo.id,
					targetUserId: targetVideo.belongUserId
				}).then(res => {
					console.log(res)
					if (res.code == 200) {
						if (this.fromVideoCategory && this.videoList.length > 0) {
							// 更新视频列表中对应项
							const index = this.videoList.findIndex(v => v.id === id);
							if (index !== -1) {
								const item = this.videoList[index];
								if (item.isLike) {
									item.likeCount = Math.max(0, item.likeCount - 1);
									item.isLike = false;
								} else {
									item.likeCount = (item.likeCount || 0) + 1;
									item.isLike = true;
								}
								// 同步更新当前显示的详情
								if (this.notesDetail.id === id) {
									this.notesDetail.isLike = item.isLike;
									this.notesDetail.likeCount = item.likeCount;
								}
							}
						} else {
							if (this.notesDetail.isLike) {
								this.notesDetail.likeCount = this.notesDetail.likeCount - 1
								this.notesDetail.isLike = false
							} else {
								this.notesDetail.likeCount = this.notesDetail.likeCount + 1
								this.notesDetail.isLike = true
							}
						}
					}
				})
			},
			/**
			 * 收藏或取消收藏商品
			 * @param {Object} id
			 */
			collectNotes(id) {
				const targetVideo = this.fromVideoCategory && this.videoList.length > 0
					? this.videoList.find(v => v.id === id) || this.notesDetail
					: this.notesDetail;
				
				praiseOrCancelIdles({
					notesId: id,
					userId: this.userInfo.id,
					targetUserId: targetVideo.belongUserId
				}).then(res => {
					console.log(res)
					if (res.code == 200) {
						if (this.fromVideoCategory && this.videoList.length > 0) {
							// 更新视频列表中对应项
							const index = this.videoList.findIndex(v => v.id === id);
							if (index !== -1) {
								const item = this.videoList[index];
								if (item.isCollect) {
									item.collectionCount = Math.max(0, item.collectionCount - 1);
									item.isCollect = false;
								} else {
									item.collectionCount = (item.collectionCount || 0) + 1;
									item.isCollect = true;
								}
								// 同步更新当前显示的详情
								if (this.notesDetail.id === id) {
									this.notesDetail.isCollect = item.isCollect;
									this.notesDetail.collectionCount = item.collectionCount;
								}
							}
						} else {
							if (this.notesDetail.isCollect) {
								this.notesDetail.collectionCount = this.notesDetail.collectionCount - 1
								this.notesDetail.isCollect = false
							} else {
								this.notesDetail.collectionCount = this.notesDetail.collectionCount + 1
								this.notesDetail.isCollect = true
							}
						}
					}
				})
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
							url: '/pkg-auth/pages/login/login'
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
				
				// 获取当前商品ID（可能是视频列表中的当前项或单个商品）
				const productId = this.fromVideoCategory && this.videoList.length > 0
					? this.videoList[this.currentVideoIndex]?.id || this.notesDetail.id
					: this.notesDetail.id;
				
				// 跳转到订单确认页面
				uni.navigateTo({
					url: `/pkg-mine/pages/confirmOrder/confirmOrder?productId=${productId}&quantity=1`
				});
			},
			/** 点赞或取消点赞评论
			 * @param {Object} id 评论id
			 * @param {Object} userId 评论人id
			 * @param {Object} type 评论类型 1:一级评论 2:二级评论
			 * @param {Object} index 评论下标，数组，[一级评论下标，二级评论下标]
			 */
			praiseComment(id, userId, type, index) {
				console.log(id, userId, type, index)
				praiseOrCancelComment({
					commentId: id,
					userId: this.userInfo.id,
					targetUserId: userId
				}).then(res => {
					console.log(res)
					if (res.code == 200) {
						if (type == 1) {
							if (this.commentList[index].isLike) {
								this.commentList[index].isLike = false
								this.commentList[index].commentLikeNum--
							} else {
								this.commentList[index].isLike = true
								this.commentList[index].commentLikeNum++
							}
						} else {
							if (this.commentList[index[0]].children.list[index[1]].isLike) {
								this.commentList[index[0]].children.list[index[1]].isLike = false
								this.commentList[index[0]].children.list[index[1]].commentLikeNum--
							} else {
								this.commentList[index[0]].children.list[index[1]].isLike = true
								this.commentList[index[0]].children.list[index[1]].commentLikeNum++
							}
						}
					}
				})
			},
			/** 点击用户头像跳转到用户主页
			 * @param {Object} e
			 */
			clickUser(e) {
				console.log(e)
				if (e.detail.node.name != 'a') {
					return
				}
				let userId = this.findUserId(e.detail.node.attrs.href)
				this.goToOtherMine(userId)
			},
			/** 获取一级评论的子评论
			 * @param {Object} e
			 */
			getReplyList(id) {
				console.log(id)
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
						}).then(res => {
							console.log(res)
							if (res.code == 20010) {
								item.children.page++
								item.children.loadmoreText = '展开更多回复'
								res.data.forEach(item => {
									item.createTime = weChatTimeFormat(Number(item.createTime))
									if (item.pictureUrl != null && item.pictureUrl != '') {
										uni.getImageInfo({
											src: item.pictureUrl,
											success: (image) => {
												item.picture = {
													url: item.pictureUrl,
												}
												// 图片长度最长为350rpx,高度最高为350rpx
												if (image.width >= image.height) {
													if (image.width > 350) {
														item.picture.width = 350
														item.picture.height = 350 *
															image.height / image.width
													} else {
														item.picture.width = image
															.width
														item.picture.height = image
															.height
													}
												} else {
													if (image.height > 350) {
														item.picture.height = 350
														item.picture.width = 350 *
															image.width / image.height
													} else {
														item.picture.width = image
															.width
														item.picture.height = image
															.height
													}
												}
												console.log(item.picture)
											},
											fail: (err) => {
												console.log(err)
											}
										})
									} else {
										item.picture = null
									}
								})
								setTimeout(() => {
									if (res.data.length < item.children.pageSize) {
										item.children.status = 'nomore';
									} else {
										item.children.status = 'loadmore';
									}
									item.children.list.push(...res.data)
								}, 1000)
							} else {
								item.children.status = 'nomore';
							}
						}).catch(err => {
							item.children.status = 'nomore';
						}).finally(() => {
							console.log(this.commentList)
							return;
						})
					}
				})
			},
			/**
			 * 发送评论
			 */
			sendComment() {
				this.edit.getContents().then(res => {
					console.log(res)
					// 如果res.text里面只有换行符或者空格，res.html也没有<img>标签，就不允许发布	
					const regex = /^[\n\s]+$/;
					const regex2 = /<img/g;
					if (regex.test(res.text) && this.commentImagesurl == '' && !regex2.test(res.html)) {
						uni.showToast({
							title: '请输入内容',
							icon: 'none'
						})
						return;
					}
					this.content = res.html
					if (this.commentImagesurl == '') {
						let commentVO = {
							content: this.content,
							replyUserId: this.replyCommentUserId,
							replyUserName: this.replyCommentUserName,
							productId: this.notesDetail.id,
							commentUserId: this.userInfo.id,
							parentId: this.parentId
						}
						console.log(commentVO)
						addComment({
							commentVO
						}).then(res => {
							console.log(res)
							if (res.code == 200) {
								uni.showToast({
									title: '评论已发布',
									icon: 'success'
								})
								this.commentCount++
								res.data.createTime = weChatTimeFormat(Number(res.data.createTime))
								console.log(res.data)
								if (this.parentId == 0) {
									res.data.children = {
										list: [],
										page: 1,
										pageSize: 10,
										status: 'loadmore',
										loadmoreText: '—— 展开更多回复 ——'
									}
									console.log(res.data)
									this.commentList.unshift(res.data)
								} else {
									this.commentList.forEach(item => {
										if (item.id == this.parentId) {
											console.log(item)
											if (item.children.page == 1) {
												item.commentReplyNum++
												item.children.loadmoreText = '—— 展开' + item
													.commentReplyNum + '条回复 ——'
											} else {
												item.children.list.unshift(res.data)
											}
										}
									})
								}
								this.content = ''
								this.inputField = false
								this.commentShow = false
								// 确保输入框关闭
								setTimeout(() => {
									this.inputField = false
								}, 100)
							} else {
								uni.showToast({
									title: res.msg == null ? '评论失败' : res.msg,
									icon: 'none'
								})
							}
						}).catch(err => {
							const errorMsg = err?.msg || err?.message || '评论失败'
							uni.showToast({
								title: errorMsg,
								icon: 'none',
								duration: 2500
							})
						})
						return
					}
					uni.uploadFile({
						url: baseUrl + '/web/oss/upload',
						filePath: this.commentImagesurl,
						name: 'file',
						header: {
							'accessToken': uni.getStorageSync('uniapp_token') || ''
						},
						success: (res) => {
							let data = JSON.parse(res.data)
							if (data.code === 200) {
								console.log('图片上传成功:', data.data)
								// 直接传递图片URL作为content
								addComment({
									content: data.data,
									replyUserId: this.replyCommentUserId,
									replyUserName: this.replyCommentUserName,
									productId: this.notesDetail.id,
									commentUserId: this.userInfo.id,
									parentId: this.parentId,
									pictureUrl: data.data
								}).then(res => {
									console.log(res)
									if (res.code == 200) {
										// 同步评论
										const id = res.data.id;
										addCommentSync([id]) // 用数组包裹
										
										uni.getImageInfo({
											src: res.data.pictureUrl,
											success: (image) => {
												res.data.picture = {
													url: res.data
														.pictureUrl,
												}
												// 图片长度最长为350rpx,高度最高为350rpx
												if (image.width >= image
													.height) {
													if (image.width > 350) {
														res.data.picture
															.width = 350
														res.data.picture
															.height = 350 *
															image.height /
															image
															.width
													} else {
														res.data.picture
															.width = image
															.width
														res.data.picture
															.height = image
															.height
													}
												} else {
													if (image.height > 350) {
														res.data.picture
															.height = 350
														res.data.picture
															.width = 350 *
															image.width / image
															.height
													} else {
														res.data.picture
															.width = image
															.width
														res.data.picture
															.height = image
															.height
													}
												}
												uni.hideLoading()
												console.log(res.data.picture)
												this.content = ''
												this.revealcontent = ''
												this.commentImagesurl = ''
												this.inputField = false
												this.commentShow = false
												uni.showToast({
													title: '评论已发布',
													icon: 'success'
												})
												this.commentCount++
												res.data.createTime =
													weChatTimeFormat(Number(res
														.data
														.createTime))
												if (this.parentId == 0) {
													res.data.children = {
														list: [],
														page: 1,
														pageSize: 10,
														status: 'loadmore',
														loadmoreText: '—— 展开更多回复 ——'
													}
													this.commentList.unshift(res
														.data)
												} else {
													this.commentList.forEach(
														item => {
															if (item.id == this
																.parentId) {
																console.log(
																	item)
																if (item
																	.children
																	.page == 1
																) {
																	item.commentReplyNum++
																	item.children
																		.loadmoreText =
																		'—— 展开' +
																		item
																		.commentReplyNum +
																		'条回复 ——'
																} else {
																	item.children
																		.list
																		.unshift(
																			res
																			.data
																		)
																}
															}
														})
												}
											},
											fail: (err) => {
												uni.hideLoading()
												console.log(err)
											}
										})
									} else {
										uni.hideLoading()
										uni.showToast({
											title: res.msg == null ? '评论失败' : res
												.msg,
											icon: 'none'
										})
									}
								}).catch(err => {
									const errorMsg = err?.msg || err?.message || '评论失败'
									uni.showToast({
										title: errorMsg,
										icon: 'none',
										duration: 2500
									})
								})
							} else {
								uni.showToast({
									title: '图片上传失败',
									icon: 'none'
								})
							}
						},
						fail: (err) => {
							uni.showToast({
								title: '图片上传失败',
								icon: 'none'
							})
						}
					})
				})
			},
			/**
			 * 回复一级评论
			 * @param {Object} item 一级评论
			 */
			replyFirstComment(item) {
				if (this.isLongPress) {
					return
				}
				this.editPlaceholder = '回复 @' + item.commentUserName
				if (this.replyCommentUserId != item.commentUserId) {
					this.content = ''
					this.revealcontent = ''
				}
				this.replyCommentUserId = 0
				this.replyCommentUserName = ''
				this.parentId = item.id
				this.openEditor()
			},
			/**
			 * 回复二级评论
			 * @param {Object} fitem 一级评论
			 * @param {Object} item 二级评论
			 */
			replySecondComment(fitem, item) {
				if (this.isLongPress) {
					return
				}
				this.editPlaceholder = '回复 @' + item.commentUserName
				this.content = ''
				this.revealcontent = ''
				this.replyCommentUserId = item.commentUserId
				this.replyCommentUserName = item.commentUserName
				this.parentId = fitem.id
				this.openEditor()
			},
			/**
			 * 回复笔记，即发布一级评论
			 */
			replyNotes() {
				this.editPlaceholder = '爱评论的人运气都不差'
				if (this.replyCommentUserId != 0) {
					this.content = ''
					this.revealcontent = ''
				}
				this.replyCommentUserId = 0
				this.replyCommentUserName = ''
				this.parentId = 0
				this.openEditor()
			},
			/**
			 * 弹出输入框
			 */
			openEditor() {
				this.inputField = true
				this.showEmoji = false
				this.showAite = false
				setTimeout(() => {
					this.$refs.lsjComment.keyboardShow()
				}, 700)
			},

			/**
			 * 点击输入框
			 */
			onEditClick() {
				this.showEmoji = false
				this.showAite = false
			},
			/**
			 * 关闭输入框
			 */
			closeEditor() {
				this.inputField = false
				this.showEmoji = false
				this.showAite = false
				this.edit.getContents().then(res => {
					console.log(res)
					this.content = res.html
					this.revealcontent = res.text
				})
				uni.hideKeyboard()
			},
			/**
			 * 预览图片
			 */
			previewImage(url) {
				uni.previewImage({
					urls: [url]
				})
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
						url: '/pkg-main/pages/mine/mine'
					})
				} else {
					// 否则跳转到otherMine页面
					uni.navigateTo({
						url: '/pkg-mine/pages/mine/otherMine?userId=' + userId
					})
				}
			},
			/**
			 * 格式化评论内容，将图片URL转换为图片标签
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
				
				return content;
			},
			/**
			 * 获取商品评论列表
			 * @param {Object} productId 商品id
			 */
			getFirstComment(productId) {
				if (this.status == 'loading' || this.status == 'nomore') {
					return;
				}
				this.status = 'loading';
				getCommentWithCommentByProductId({
					productId: productId,
					page: this.page,
					pageSize: this.pageSize
				}).then(res => {
					console.log(res)
					res.data.records.forEach(item => {
						item.createTime = weChatTimeFormat(Number(item.createTime))
						item.children = {
							list: [],
							page: 1,
							pageSize: 10,
							status: 'loadmore',
							loadmoreText: '—— 展开' + item.commentReplyNum + '条回复 ——'
						}
						if (item.pictureUrl != null && item.pictureUrl != '') {
							uni.getImageInfo({
								src: item.pictureUrl,
								success: (image) => {
									item.picture = {
										url: item.pictureUrl,
									}
									// 图片长度最长为350rpx,高度最高为350rpx
									if (image.width >= image.height) {
										if (image.width > 350) {
											item.picture.width = 350
											item.picture.height = 350 * image.height / image
												.width
										} else {
											item.picture.width = image.width
											item.picture.height = image.height
										}
									} else {
										if (image.height > 350) {
											item.picture.height = 350
											item.picture.width = 350 * image.width / image
												.height
										} else {
											item.picture.width = image.width
											item.picture.height = image.height
										}
									}
									console.log(item.picture)
								},
								fail: (err) => {
									console.log(err)
								}
							})
						} else {
							item.picture = null
						}
					})
					setTimeout(() => {
						this.page++;
						if (res.data.records < this.pageSize) {
							this.status = 'nomore';
						} else {
							this.status = 'loadmore';
						}
						this.commentList = this.commentList.concat(res.data.records)
					}, 700)
				}).catch(err => {
					console.log(err)
				})
			},
			/**
			 * 评论输入框添加@用户
			 * @param {Object} item 
			 */
			addUser(item) {
				this.showAite = false
				this.$refs.lsjComment.keyboardShow()
				this.edit.addLink({
					prefix: '@',
					name: item.nickname,
					data: {
						userId: item.userId,
					}
				})
			},
			/**
			 * 评论输入框添加emoji
			 * @param {Object} name 
			 */
			addEmoji(name) {
				let sql = `update emoji_list set updateTime=datetime('now','localtime') where name='${name}'`
				this.$sqliteUtil.SqlExecute(sql)
				let emojiUrl = '';
				emojiList.forEach(item => {
					if (item.name == name) {
						emojiUrl = item.url;
					}
				})
				// this.edit.insertEmoji(emojiUrl, name)
				this.edit.insertCustomEmoji(emojiUrl, name, '15px', '15px')
				this.showEmoji = false
				this.$refs.lsjComment.keyboardShow()
			},
			/**
			 * 点击 @ 图标：直接在输入框插入一个 @ 符号
			 */
			chooseAite() {
				this.showAite = false
				this.showEmoji = false

				if (this.edit && typeof this.edit.insertText === 'function') {
					this.edit.insertText('@')
				} else if (this.edit && typeof this.edit.addLink === 'function') {
					this.edit.addLink({
						prefix: '',
						name: '@',
						data: {}
					})
				} else {
					this.content = (this.content || '') + '@'
				}

				if (this.$refs.lsjComment && typeof this.$refs.lsjComment.keyboardShow === 'function') {
					this.$refs.lsjComment.keyboardShow()
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
						pageSize: this.attentionUser.pageSize,
					}).then(res => {
						if (res.code == 20010) {
							console.log(res)
							if (res.data.length < this.attentionUser.pageSize) {
								this.attentionUser.isNoMore = true;
								this.attentionUser.status = 'nomore';
							}
							this.attentionUser.list = this.attentionUser.list.concat(res.data);
							this.attentionUser.page++;
						}
					}).finally(() => {
						this.attentionUser.isLoading = false;
					})
				}, 500)
			},
			/**
			 * 打开emoji弹出层
			 */
			openEmoji() {
				if (this.showEmoji) {
					this.showEmoji = false
					this.$refs.lsjComment.keyboardShow()
				} else {
					this.showAite = false
					this.showEmoji = true
					uni.hideKeyboard()
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
					success: (res) => {
						console.log(res)
						this.commentImagesurl = res.tempFilePaths[0]
					},
					complete: () => {
						this.$refs.lsjComment.keyboardShow()
					}
				})
			},
			/**
			 * 删除发布评论的图片
			 */
			deleteImage() {
				this.commentImagesurl = ''
				this.$refs.lsjComment.keyboardShow()
			},
			/**
			 * 编辑器初始化
			 * @param {Object} edit 
			 */
			editReady(edit) {
				this.edit = edit
				const reg = /<img/g;
				if (!(this.revealcontent == '\n' || this.revealcontent == '') || reg.test(this.content)) {
					this.edit.ready(this.content)
				}
			},
			/**
			 * swiper切换
			 * @param {Object} e 
			 */
			swiperChange(e) {
				this.swiperCurrent = e.detail.current
			},
			/**
			 * 提取话题名
			 * @param {Object} str 
			 */
			findTopicName(str) {
				// #{&quot;topicname&quot;:&quot;蛋仔派对&quot;}
				let reg = /#{&quot;topicname&quot;:&quot;(.+?)&quot;}/g
				let result = str.match(reg)
				if (result) {
					return result[0].replace(/#{&quot;topicname&quot;:&quot;/g, '').replace(/&quot;}/g, '')
				} else {
					return ''
				}
			},
			/**
			 * 提取用户id
			 * @param {Object} str 
			 */
			findUserId(str) {
				// #{&quot;userId&quot;:&quot;1675532564583455936&quot;}
				let reg = /#{&quot;userId&quot;:&quot;(.+?)&quot;}/g
				let result = str.match(reg)
				if (result) {
					return result[0].replace(/#{&quot;userId&quot;:&quot;/g, '').replace(/&quot;}/g, '')
				} else {
					return ''
				}
			},
			/**
			 * 点击话题或者用户
			 * @param {Object} e 
			 */
			clickTopic(e) {
				if (e.detail.node.name != 'a') {
					return
				}
				// 判断是话题还是用户
				let topicName = this.findTopicName(e.detail.node.attrs.href)
				let userId = this.findUserId(e.detail.node.attrs.href)
				if (topicName != '') {
					uni.navigateTo({
						url: '/pkg-search/pages/topicIndex/topicIndex?topicName=' + topicName
					})
				} else if (userId != '') {
					uni.navigateTo({
						url: '/pkg-mine/pages/mine/otherMine?userId=' + userId
					})
				} else {
					console.log('不是话题也不是用户')
				}
			},
			/**
			 * 返回上一页
			 */
			goBack() {
				uni.navigateBack()
			},
			openComment() {
				// 打开评论区时暂停视频
				let videoContext = this.videoContext;
				if (this.fromVideoCategory && this.videoList.length > 0) {
					const videoId = `video_${this.currentVideoIndex}`;
					videoContext = uni.createVideoContext(videoId, this);
					// 确保使用当前视频的评论数量
					const currentVideo = this.videoList[this.currentVideoIndex];
					if (currentVideo) {
						this.commentCount = Number(currentVideo.commentCount) || 0;
					}
				} else if (this.notesDetail) {
					// 单个视频模式，确保评论数量正确
					this.commentCount = Number(this.notesDetail.commentCount) || 0;
				}
				
				if (videoContext && this.isPlaying) {
					videoContext.pause()
					this.isPlaying = false
					this.showPauseIcon = true
				}
				this.commentShow = true
			},
			closeComment() {
				// 关闭评论区时恢复视频播放
				this.commentShow = false
				
				let videoContext = this.videoContext;
				if (this.fromVideoCategory && this.videoList.length > 0) {
					const videoId = `video_${this.currentVideoIndex}`;
					videoContext = uni.createVideoContext(videoId, this);
				}
				
				if (videoContext && !this.isPlaying) {
					// 延迟一点时间再播放，确保界面切换完成
					setTimeout(() => {
						videoContext.play()
						this.isPlaying = true
						this.showPauseIcon = false
					}, 300)
				}
			},
			getMoreComment() {
				this.getFirstComment(this.notesDetail.id)
			},
			/**
			 * 处理视频swiper切换
			 */
			onVideoSwiperChange(e) {
				const newIndex = e.detail.current;
				console.log('🔄 视频swiper切换:', newIndex);
				
				// 暂停上一个视频
				if (this.currentVideoIndex !== newIndex && this.videoList[this.currentVideoIndex]) {
					const prevVideoId = `video_${this.currentVideoIndex}`;
					const prevVideoContext = uni.createVideoContext(prevVideoId, this);
					if (prevVideoContext) {
						prevVideoContext.pause();
					}
				}
				
				// 更新当前索引
				this.currentVideoIndex = newIndex;
				
				// 播放新视频
				this.isLoading = false; // 切换时关闭加载状态
				this.$nextTick(() => {
					const newVideoId = `video_${newIndex}`;
					const newVideoContext = uni.createVideoContext(newVideoId, this);
					if (newVideoContext) {
						setTimeout(() => {
							newVideoContext.play();
							this.isPlaying = true;
							this.showPauseIcon = false;
							this.isLoading = false; // 确保加载状态关闭
						}, 300);
					}
				});
				
				// 更新当前视频详情（用于评论等功能）
				if (this.videoList[newIndex]) {
					this.loadCurrentVideoDetail(this.videoList[newIndex].id);
				}
				
				// 如果接近列表末尾，加载更多视频
				if (newIndex >= this.videoList.length - 3 && this.hasMoreVideo && !this.loadingMoreVideo) {
					this.loadMoreVideoList();
				}
			},
			/**
			 * 加载视频列表
			 */
			async loadVideoList(currentProductId) {
				try {
					this.loadingMoreVideo = true;
					const res = await getVideoProduct({
						page: this.videoPage,
						pageSize: this.videoPageSize
					});
					
					if (res.code === 200 && res.data.records) {
						// 处理视频数据
						const mappedVideos = res.data.records.map(item => {
							// 处理视频url
							let videoUrl = '';
							if (item.urls) {
								const urlsArr = typeof item.urls === 'string' ? JSON.parse(item.urls) : item.urls;
								if (urlsArr.length > 0) {
									videoUrl = urlsArr[0];
								}
							}
							
							// 处理描述内容
							let description = '';
							if (item.description) {
								// 如果description是HTML格式，去掉<p>标签
								const descText = item.description.replace(/^<p>|<\/p>$/g, '');
								description = descText || '';
							} else if (item.content) {
								const contentText = item.content.replace(/^<p>|<\/p>$/g, '');
								description = contentText || '';
							}
							
							return {
								id: item.id || '',
								title: item.title || '',
								content: parseHtml('<p>' + item.title + '    ' + (description || '')),
								coverPicture: item.cover || '',
								belongUserId: Number(item.uid) || 0,
								nickname: item.username || '',
								avatarUrl: item.avatar || '',
								videoUrl: videoUrl,
								likeCount: Number(item.likeCount) || 0,
								viewCount: Number(item.viewCount) || 0,
								collectionCount: Number(item.collectionCount) || 0,
								commentCount: Number(item.commentCount) || 0,
								isFollow: item.isFollow || false,
								isLike: item.isLike || false,
								isCollect: item.isCollection || false,
								createTime: item.time ? weChatTimeFormat(item.time) : '',
								updateTime: item.time ? weChatTimeFormat(item.time) : '',
								// 商品信息字段
								price: item.price || 0,
								originalPrice: item.originalPrice || 0,
								postType: item.postType || 0,
								province: item.province || '',
								city: item.city || '',
								district: item.district || '',
							};
						});
						
						// 追加到视频列表
						this.videoList = [...this.videoList, ...mappedVideos];
						
						// 找到当前视频的索引
						if (currentProductId && this.currentVideoIndex === 0) {
							const index = this.videoList.findIndex(v => String(v.id) === String(currentProductId));
							if (index !== -1) {
								this.currentVideoIndex = index;
								console.log('✅ 找到当前视频索引:', index, '视频ID:', currentProductId);
							}
						}
						
						console.log('📹 视频列表加载完成，总数:', this.videoList.length, '当前索引:', this.currentVideoIndex);
						
						// 更新分页信息
						if (res.data.records.length < this.videoPageSize) {
							this.hasMoreVideo = false;
						} else {
							this.videoPage++;
						}
					} else {
						this.hasMoreVideo = false;
					}
				} catch (error) {
					console.error('加载视频列表失败:', error);
					this.hasMoreVideo = false;
				} finally {
					this.loadingMoreVideo = false;
				}
			},
			/**
			 * 加载更多视频
			 */
			loadMoreVideoList() {
				if (!this.hasMoreVideo || this.loadingMoreVideo) {
					return;
				}
				this.loadVideoList();
			},
			/**
			 * 加载当前视频详情
			 */
			loadCurrentVideoDetail(productId) {
				// 添加浏览记录
				if (this.userInfo && this.userInfo.id) {
					addBrowseRecord({
						nid: productId,
						uid: this.userInfo.id
					}).catch(err => {
						console.log('添加浏览记录失败:', err)
					})
				}
				
				getProductById({
					productId: productId
				}).then(res => {
					const data = res.data;
					// 处理视频url
					let videoUrl = '';
					if (data.urls) {
						// urls 是字符串，需要先 JSON.parse
						const urlsArr = typeof data.urls === 'string' ? JSON.parse(data.urls) : data.urls;
						// 只取第一个视频地址
						if (urlsArr.length > 0) {
							videoUrl = urlsArr[0];
						}
					}
					// 处理时间
					const createTime = data.time ? weChatTimeFormat(data.time) : '';
					const updateTime = data.time ? weChatTimeFormat(data.time) : '';
					// 统一映射
					const videoDetail = {
						id: data.id,
						title: data.title,
						content: parseHtml('<p>' + data.title + '    ' + (data.content ? data.content.substring(3) : '')),
						coverPicture: data.cover || data.noteCover || '',
						noteType: Number(data.noteType),
						belongUserId: Number(data.uid),
						nickname: data.username,
						avatarUrl: data.avatar,
						videoUrl: videoUrl,
						likeCount: Number(data.likeCount),
						viewCount: Number(data.viewCount),
						collectionCount: Number(data.collectionCount),
						commentCount: Number(data.commentCount) || 0,
						isFollow: data.isFollow,
						isLike: data.isLike,
						isCollect: data.isCollection,
						createTime,
						updateTime,
						// 商品信息字段
						price: data.price || 0,
						originalPrice: data.originalPrice || 0,
						postType: data.postType || 0,
						province: data.province || '',
						city: data.city || '',
						district: data.district || '',
					};
					
					// 如果是视频分类模式，需要同步更新videoList中对应项
					if (this.fromVideoCategory && this.videoList.length > 0) {
						const index = this.videoList.findIndex(v => v.id === videoDetail.id);
						if (index !== -1) {
							this.$set(this.videoList, index, videoDetail);
						}
						// 设置当前索引对应的视频详情
						if (this.currentVideoIndex < this.videoList.length) {
							this.notesDetail = this.videoList[this.currentVideoIndex];
						}
					} else {
						this.notesDetail = videoDetail;
					}
					
					// 评论数量已从 getProductById 接口获取，无需单独调用
					this.commentCount = Number(videoDetail.commentCount) || 0
					console.log('商品视频详情页 - 评论数量:', this.commentCount, '原始数据:', data.commentCount)
					
					// 加载评论
					this.commentList = [];
					this.page = 1;
					this.status = 'loadmore';
					this.getFirstComment(productId);
					
					// 如果不是视频分类模式，初始化视频播放
					if (!this.fromVideoCategory) {
						this.$nextTick(() => {
							this.videoContext = uni.createVideoContext('myVideo', this);
							setTimeout(() => {
								this.initVideoPlay();
							}, 300);
						});
					} else {
						// 视频分类模式，播放当前索引的视频
						this.$nextTick(() => {
							const videoId = `video_${this.currentVideoIndex}`;
							this.videoContext = uni.createVideoContext(videoId, this);
							setTimeout(() => {
								if (this.videoContext) {
									this.videoContext.play();
									this.isPlaying = true;
									this.showPauseIcon = false;
									this.isLoading = false; // 确保加载状态关闭
								}
							}, 300);
						});
					}
				});
			},
		},
		onLoad(options) {
			console.log(options)
			// 参数验证
			if (!options.productId) {
				uni.showToast({
					title: '商品参数错误',
					icon: 'none',
					duration: 2000
				})
				setTimeout(() => {
					uni.navigateBack()
				}, 2000)
				return
			}
			uni.getSystemInfo({
				success: (res) => {
					this.screenHeight = res.screenHeight;
					this.screenWidth = res.screenWidth;
					this.statusBarHeight = res.statusBarHeight;
				}
			})
			// 统一使用内置 Unicode 表情，去掉本地 sqlite 依赖
			this.emojiList = emojiList
			this.bottomEmoji = emojiList.slice(0, 9)
			uni.onKeyboardHeightChange((res) => {
				if (res.height > 0 && this.keyboardHeight == 0) {
					this.keyboardHeight = res.height
				}
			})
			this.userInfo = uni.getStorageSync('userInfo');
			
			// 判断是否从视频分类进入
			if (options.fromVideoCategory === '1') {
				this.fromVideoCategory = true;
				// 先加载视频列表
				this.loadVideoList(options.productId).then(() => {
					// 视频列表加载完成后，再加载当前视频详情
					this.loadCurrentVideoDetail(options.productId);
				});
			} else {
				// 非视频分类模式，使用原有逻辑
				getProductById({
					productId: options.productId
				}).then(res => {
					const data = res.data;
					// 处理视频url
					let videoUrl = '';
					if (data.urls) {
						// urls 是字符串，需要先 JSON.parse
						const urlsArr = typeof data.urls === 'string' ? JSON.parse(data.urls) : data.urls;
						// 只取第一个视频地址
						if (urlsArr.length > 0) {
							videoUrl = urlsArr[0];
						}
					}
					// 处理时间
					const createTime = data.time ? weChatTimeFormat(data.time) : '';
					const updateTime = data.time ? weChatTimeFormat(data.time) : '';
					// 统一映射
					this.notesDetail = {
						id: data.id,
						title: data.title,
						content: parseHtml('<p>' + data.title + '    ' + (data.content ? data.content.substring(
							3) : '')),
						coverPicture: data.cover || data.noteCover || '',
						noteType: Number(data.noteType),
						belongUserId: Number(data.uid),
						nickname: data.username,
						avatarUrl: data.avatar,
						videoUrl: videoUrl,
						likeCount: Number(data.likeCount),
						viewCount: Number(data.viewCount),
						collectionCount: Number(data.collectionCount),
						commentCount: Number(data.commentCount) || 0,
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
						district: data.district || '',
						// 其它字段按需添加
					};
					// 评论数量已从 getProductById 接口获取，无需单独调用
					this.commentCount = Number(this.notesDetail.commentCount) || 0
					console.log('商品视频详情页 - 评论数量:', this.commentCount, '原始数据:', data.commentCount)
				});
				this.getFirstComment(options.productId)
			}
		},
		onBackPress() {
			if (this.showEmoji) {
				this.showEmoji = false
				return true
			}
			if (this.showAite) {
				this.showAite = false
				return true
			}
			if (this.inputField) {
				this.inputField = false
				return true
			}
			if (this.commentShow) {
				this.commentShow = false
				return true
			}
			return false
		}
	}
</script>

<style lang="scss">
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
		max-height: 100px !important;
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

	.video-container {
		position: relative;
		width: 100vw;
		height: 100vh;
		background: #000;
	}

	/* 隐藏视频默认控件 */
	video {
		&::-webkit-media-controls {
			display: none !important;
		}

		&::-webkit-media-controls-start-playback-button {
			display: none !important;
		}
	}

	/* #ifdef MP-WEIXIN */
	video {
		margin-top: -50rpx;
	}

	/* #endif */

	// 添加新样式
	.right-action-bar {
		.action-item {
			margin-bottom: 40rpx;
			text-align: center;

			.count {
				font-size: 26rpx;
				color: #fff;
				margin-top: 5rpx;
			}
		}
	}

	.bottom-info {
		.nickname {
			font-size: 30rpx;
			color: #fff;
			font-weight: bold;
			margin-bottom: 10rpx;
		}

		.content {
			font-size: 28rpx;
			color: #fff;
			line-height: 1.5;
		}
	}

	// 弹出层样式
	.comment-popup {
		border-top-left-radius: 20rpx;
		border-top-right-radius: 20rpx;
		overflow: hidden;
	}

	.custom-progress {
		position: fixed; // 改为 fixed 定位
		bottom: 120rpx; // 调整底部距离
		left: 0;
		right: 0;
		z-index: 99; // 提高层级
		padding: 0 40rpx;
		display: flex;
		align-items: center;

		.progress-bar {
			position: relative;
			flex: 1;
			height: 4px;
			background: rgba(255, 255, 255, 0.3);
			border-radius: 2px;
			margin-right: 20rpx;

			.progress-inner {
				position: absolute;
				left: 0;
				top: 0;
				height: 100%;
				background: #3d8af5;
				border-radius: 2px;
			}

			.progress-dot {
				position: absolute;
				top: 50%;
				transform: translate(-50%, -50%);
				width: 12px;
				height: 12px;
				background: #3d8af5;
				border-radius: 50%;
				border: 2px solid #fff;
			}
		}

		.time-text {
			color: #fff;
			font-size: 24rpx;
			min-width: 120rpx;
			text-align: right;
		}
	}

	// 针对不同平台的适配
	/* #ifdef MP-WEIXIN */
	.custom-progress {
		bottom: 110rpx; // 小程序端底部距离调整
	}

	/* #endif */

	/* #ifdef H5 */
	.custom-progress {
		bottom: 150rpx; // H5端底部距离
	}

	/* #endif */

	/* 双击点赞动画 */
	.like-animation {
		position: absolute;
		left: 50%;
		top: 50%;
		transform: translate(-50%, -50%);
		animation: likeScale 0.5s ease-out;
		z-index: 99;
	}

	@keyframes likeScale {
		0% {
			transform: translate(-50%, -50%) scale(0.5);
			opacity: 0;
		}

		50% {
			transform: translate(-50%, -50%) scale(1.5);
			opacity: 1;
		}

		100% {
			transform: translate(-50%, -50%) scale(1);
			opacity: 0;
		}
	}
</style>