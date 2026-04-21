/**
 * 视频工具类 - 提取视频第一帧作为封面
 */

/**
 * 提取视频第一帧作为封面图
 * @param {String} videoPath 视频路径
 * @param {Number} time 截取时间点（秒），默认0（第一帧）
 * @returns {Promise<String>} 返回封面图路径（base64或文件路径）
 */
export function extractVideoFrame(videoPath, time = 0) {
	return new Promise((resolve, reject) => {
		// #ifdef H5
		extractVideoFrameH5(videoPath, time).then(resolve).catch(reject);
		// #endif
		
		// #ifdef APP-PLUS
		extractVideoFrameApp(videoPath, time).then(resolve).catch(reject);
		// #endif
		
		// #ifdef MP-WEIXIN
		extractVideoFrameMP(videoPath, time).then(resolve).catch(reject);
		// #endif
		
		// #ifndef H5 || APP-PLUS || MP-WEIXIN
		// 其他平台降级处理
		reject(new Error('当前平台不支持视频截图功能'));
		// #endif
	});
}

// H5端实现
// #ifdef H5
function extractVideoFrameH5(videoPath, time) {
	return new Promise((resolve, reject) => {
		try {
			const video = document.createElement('video');
			video.crossOrigin = 'anonymous';
			video.preload = 'metadata';
			video.src = videoPath;
			
			video.addEventListener('loadedmetadata', () => {
				video.currentTime = time;
			});
			
			video.addEventListener('seeked', () => {
				try {
					const canvas = document.createElement('canvas');
					canvas.width = video.videoWidth;
					canvas.height = video.videoHeight;
					
					const ctx = canvas.getContext('2d');
					ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
					
					// 转换为base64
					const base64 = canvas.toDataURL('image/jpeg', 0.8);
					resolve(base64);
				} catch (error) {
					reject(error);
				}
			});
			
			video.addEventListener('error', (e) => {
				reject(new Error('视频加载失败'));
			});
			
			// 如果视频已经加载，直接触发
			if (video.readyState >= 2) {
				video.currentTime = time;
			}
		} catch (error) {
			reject(error);
		}
	});
}
// #endif

// APP端实现
// #ifdef APP-PLUS
function extractVideoFrameApp(videoPath, time) {
	return new Promise((resolve, reject) => {
		try {
			// 使用uni-app的canvas API
			const ctx = uni.createCanvasContext('videoFrameCanvas');
			const video = uni.createVideoContext('videoFrameVideo');
			
			// 创建隐藏的video组件来加载视频
			// 注意：这里需要在页面中有一个隐藏的video组件
			// 或者使用plus.video API
			
			// 使用plus.video更可靠
			if (plus && plus.video) {
				const videoElement = document.createElement('video');
				videoElement.src = videoPath;
				videoElement.crossOrigin = 'anonymous';
				videoElement.preload = 'metadata';
				
				videoElement.addEventListener('loadedmetadata', () => {
					videoElement.currentTime = time;
				});
				
				videoElement.addEventListener('seeked', () => {
					try {
						const canvas = document.createElement('canvas');
						canvas.width = videoElement.videoWidth;
						canvas.height = videoElement.videoHeight;
						
						const ctx = canvas.getContext('2d');
						ctx.drawImage(videoElement, 0, 0, canvas.width, canvas.height);
						
						const base64 = canvas.toDataURL('image/jpeg', 0.8);
						
						// 保存为临时文件
						const fileName = `video_cover_${Date.now()}.jpg`;
						plus.io.resolveLocalFileSystemURL('_doc/', (entry) => {
							entry.getFile(fileName, { create: true, exclusive: false }, (fileEntry) => {
								fileEntry.createWriter((writer) => {
									// 将base64转换为blob
									const base64Data = base64.split(',')[1];
									const binaryString = atob(base64Data);
									const bytes = new Uint8Array(binaryString.length);
									for (let i = 0; i < binaryString.length; i++) {
										bytes[i] = binaryString.charCodeAt(i);
									}
									const blob = new Blob([bytes], { type: 'image/jpeg' });
									
									writer.write(blob);
									writer.onwriteend = () => {
										resolve(fileEntry.toLocalURL());
									};
								});
							});
						});
					} catch (error) {
						reject(error);
					}
				});
				
				videoElement.addEventListener('error', () => {
					reject(new Error('视频加载失败'));
				});
			} else {
				// 降级方案：使用canvas + video组件
				reject(new Error('APP端需要plus.video支持'));
			}
		} catch (error) {
			reject(error);
		}
	});
}
// #endif

// 微信小程序端实现
// #ifdef MP-WEIXIN
function extractVideoFrameMP(videoPath, time) {
	return new Promise((resolve, reject) => {
		// 微信小程序需要使用页面中的video和canvas组件
		// 这里返回一个标记，让调用方知道需要使用页面组件方式
		reject(new Error('微信小程序需要使用页面组件方式'));
	});
}
// #endif

/**
 * 在页面中提取视频帧（需要页面中有video和canvas组件）
 * @param {String} videoId video组件的id
 * @param {String} canvasId canvas组件的id
 * @param {Number} time 截取时间点（秒）
 * @returns {Promise<String>} 返回封面图路径
 */
export function extractVideoFrameInPage(videoId, canvasId, time = 0) {
	return new Promise((resolve, reject) => {
		// #ifdef H5
		const video = document.getElementById(videoId);
		const canvas = document.getElementById(canvasId);
		
		if (!video || !canvas) {
			reject(new Error('找不到video或canvas元素'));
			return;
		}
		
		video.currentTime = time;
		
		video.addEventListener('seeked', () => {
			try {
				const ctx = canvas.getContext('2d');
				canvas.width = video.videoWidth;
				canvas.height = video.videoHeight;
				ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
				const base64 = canvas.toDataURL('image/jpeg', 0.8);
				resolve(base64);
			} catch (error) {
				reject(error);
			}
		}, { once: true });
		
		video.addEventListener('error', () => {
			reject(new Error('视频加载失败'));
		}, { once: true });
		// #endif
		
		// #ifdef APP-PLUS
		// APP端使用video和canvas组件
		const videoContext = uni.createVideoContext(videoId);
		const canvasContext = uni.createCanvasContext(canvasId);
		
		// 先获取视频信息
		videoContext.seek(time);
		
		// 等待视频跳转完成后再截图
		setTimeout(() => {
			try {
				// 获取视频尺寸（需要先播放一下才能获取）
				videoContext.play();
				setTimeout(() => {
					videoContext.pause();
					
					// 使用canvas截图
					// 注意：需要先获取video的实际尺寸
					// 这里使用固定尺寸，实际应该从video获取
					const query = uni.createSelectorQuery();
					query.select(`#${videoId}`).boundingClientRect((rect) => {
						if (rect) {
							canvasContext.drawImage(`#${videoId}`, 0, 0, rect.width, rect.height);
							canvasContext.draw(false, () => {
								// 导出canvas为图片
								uni.canvasToTempFilePath({
									canvasId: canvasId,
									success: (res) => {
										resolve(res.tempFilePath);
									},
									fail: (err) => {
										reject(err);
									}
								});
							});
						} else {
							reject(new Error('无法获取视频尺寸'));
						}
					}).exec();
				}, 500);
			} catch (error) {
				reject(error);
			}
		}, 1000);
		// #endif
		
		// #ifdef MP-WEIXIN
		// 微信小程序端不支持canvas.drawImage绘制video元素
		// 应该在选择视频时就获取缩略图，而不是在这里截取
		reject(new Error('微信小程序不支持从video截图，请使用chooseMedia返回的缩略图'));
		// #endif
	});
}

