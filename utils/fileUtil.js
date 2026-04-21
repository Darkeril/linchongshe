// 将文件网址下载到本地保存，返回本地保存的临时文件路径
function downloadFile(url) {
	return new Promise((resolve, reject) => {
		// 验证 URL 是否有效
		if (!url || typeof url !== 'string') {
			reject(new Error('downloadFile: parameter.url should be String instead of ' + typeof url));
			return;
		}
		
		uni.downloadFile({
			url: url,
			success: (res) => {
				if (res.statusCode === 200) {
					resolve(res.tempFilePath)
				} else {
					reject(res)
				}
			},
			fail: (err) => {
				reject(err)
			}
		})
	})
}

// 根据url保存永久保存到本地，返回本地保存的文件路径
export const saveFile = (url, type = 'image') => {
	return new Promise((resolve, reject) => {
		if (!url || typeof url !== 'string') {
			reject(new Error('saveFile: parameter.url should be String instead of ' + typeof url));
			return;
		}
		// H5 端直接选择文件
		if (process.env.UNI_PLATFORM === 'h5') {
			if (type === 'image') {
				chooseImageAndGetTempPaths(1).then(paths => {
					resolve(paths[0]);
				}).catch(reject);
			} else if (type === 'video') {
				chooseVideoAndGetTempPath().then(path => {
					resolve(path);
				}).catch(reject);
			} else {
				reject(new Error('不支持的文件类型'));
			}
		} else {
			// 非 H5 端，走原有逻辑
			downloadFile(url).then((filePath) => {
				uni.saveFile({
					tempFilePath: filePath,
					success: (res) => {
						resolve(res.savedFilePath)
					},
					fail: (err) => {
						reject(err)
					}
				})
			}).catch((err) => {
				reject(err)
			})
		}
	});
}

export const zoomOutImage = (src) => {
	return new Promise((resolve, reject) => {
		// 获取屏幕宽高
		const screenWidth = uni.getSystemInfoSync().screenWidth;
		const screenHeight = uni.getSystemInfoSync().screenHeight;

		// 获取本地图片信息
		uni.getImageInfo({
			src: src,
			success: (imageInfo) => {
				// 图片原始宽高
				const imgWidth = imageInfo.width;
				const imgHeight = imageInfo.height;

				// 计算缩放比例
				const scale = Math.min(screenWidth / (2 * imgWidth), screenHeight / (2 *
					imgHeight));

				// 计算缩小后的宽高
				const actualWidth = Math.floor(imgWidth * scale);
				const actualHeight = Math.floor(imgHeight * scale);

				// 执行成功，将结果传递给 Promise 的 resolve
				resolve({
					width: actualWidth,
					height: actualHeight,
					src: src
				});
			},
			fail: (error) => {
				// 执行失败，将错误信息传递给 Promise 的 reject
				reject(error);
			},
		});
	});
}

export const uploadFile = (url, filePath, name) => {
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url,
			filePath,
			name,
			header: {
				'token': uni.getStorageSync('uniapp_token')
			},
			success: (res) => {
				let data = JSON.parse(res.data);
				if (data.code === 200) {
					console.log(data);
					resolve(data.data);
				} else {
					reject(new Error('File upload failed.'));
				}
			},
			fail: (error) => {
				reject(error);
			}
		});
	});
}

export function chooseImageAndGetTempPaths(count = 9) {
	return new Promise((resolve, reject) => {
		uni.chooseImage({
			count,
			sizeType: ['original', 'compressed'],
			sourceType: ['album', 'camera'],
			success: function(res) {
				// res.tempFilePaths 是临时文件路径数组
				resolve(res.tempFilePaths);
			},
			fail: reject
		});
	});
}

export function chooseVideoAndGetTempPath() {
	return new Promise((resolve, reject) => {
		uni.chooseVideo({
			sourceType: ['album', 'camera'],
			maxDuration: 60,
			camera: 'back',
			success: function(res) {
				// res.tempFilePath 是临时文件路径
				resolve(res.tempFilePath);
			},
			fail: reject
		});
	});
}