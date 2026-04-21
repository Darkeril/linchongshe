/**
 * 微信JS-SDK工具类
 * 用于H5环境下的微信分享功能
 */

// 动态加载微信JS-SDK
function loadWeChatJSSDK() {
	return new Promise((resolve, reject) => {
		// #ifdef H5
		// 检查是否已经加载
		if (typeof window !== 'undefined' && window.wx) {
			resolve(window.wx);
			return;
		}

		// 动态创建script标签加载微信JS-SDK
		if (typeof document !== 'undefined') {
			const script = document.createElement('script');
			script.src = 'https://res.wx.qq.com/open/js/jweixin-1.6.0.js';
			script.onload = () => {
				if (window.wx) {
					resolve(window.wx);
				} else {
					reject(new Error('微信JS-SDK加载失败'));
				}
			};
			script.onerror = () => {
				reject(new Error('微信JS-SDK加载失败'));
			};
			document.head.appendChild(script);
		} else {
			reject(new Error('当前环境不支持微信JS-SDK'));
		}
		// #endif
		// #ifndef H5
		reject(new Error('当前环境不支持微信JS-SDK'));
		// #endif
	});
}

/**
 * 初始化微信JS-SDK
 * @param {Object} config - 微信JS-SDK配置（从后端获取）
 * @param {string} config.appId - 公众号AppID
 * @param {string} config.timestamp - 时间戳
 * @param {string} config.nonceStr - 随机字符串
 * @param {string} config.signature - 签名
 * @returns {Promise} 返回Promise，成功时resolve，失败时reject
 */
export function initWeChatJSSDK(config) {
	return new Promise((resolve, reject) => {
		loadWeChatJSSDK()
			.then((wx) => {
				wx.config({
					debug: false, // 生产环境设为false
					appId: config.appId,
					timestamp: config.timestamp,
					nonceStr: config.nonceStr,
					signature: config.signature,
					jsApiList: [
						'updateAppMessageShareData', // 分享给朋友
						'updateTimelineShareData' // 分享到朋友圈
					]
				});

				wx.ready(() => {
					console.log('微信JS-SDK初始化成功');
					resolve(wx);
				});

				wx.error((res) => {
					console.error('微信JS-SDK初始化失败:', res);
					reject(new Error('微信JS-SDK初始化失败: ' + JSON.stringify(res)));
				});
			})
			.catch((error) => {
				reject(error);
			});
	});
}

/**
 * 配置分享给朋友
 * @param {Object} wx - 微信JS-SDK对象
 * @param {Object} shareData - 分享数据
 * @param {string} shareData.title - 分享标题
 * @param {string} shareData.desc - 分享描述
 * @param {string} shareData.link - 分享链接
 * @param {string} shareData.imgUrl - 分享图标
 */
export function configShareToFriend(wx, shareData) {
	wx.updateAppMessageShareData({
		title: shareData.title,
		desc: shareData.desc || shareData.title,
		link: shareData.link,
		imgUrl: shareData.imgUrl,
		success: () => {
			console.log('分享给朋友配置成功');
		},
		cancel: () => {
			console.log('用户取消分享给朋友');
		}
	});
}

/**
 * 配置分享到朋友圈
 * @param {Object} wx - 微信JS-SDK对象
 * @param {Object} shareData - 分享数据
 * @param {string} shareData.title - 分享标题
 * @param {string} shareData.link - 分享链接
 * @param {string} shareData.imgUrl - 分享图标
 */
export function configShareToTimeline(wx, shareData) {
	wx.updateTimelineShareData({
		title: shareData.title,
		link: shareData.link,
		imgUrl: shareData.imgUrl,
		success: () => {
			console.log('分享到朋友圈配置成功');
		},
		cancel: () => {
			console.log('用户取消分享到朋友圈');
		}
	});
}

/**
 * 初始化并配置微信分享
 * @param {Function} getConfig - 获取配置的函数，返回Promise，resolve时返回配置对象
 * @param {Object} shareData - 分享数据
 * @returns {Promise} 返回Promise
 */
export async function initWeChatShare(getConfig, shareData) {
	try {
		// #ifdef H5
		// 获取当前页面URL
		let currentUrl = '';
		if (typeof window !== 'undefined' && window.location) {
			currentUrl = window.location.href.split('#')[0]; // 去掉hash部分
		} else {
			// UniApp H5环境，使用pages.json中的路径
			const pages = getCurrentPages();
			if (pages && pages.length > 0) {
				const currentPage = pages[pages.length - 1];
				const route = currentPage.route || '';
				const options = currentPage.options || {};
				// 构建完整URL（需要根据实际域名调整）
				const query = Object.keys(options).map(key => `${key}=${encodeURIComponent(options[key])}`).join('&');
				currentUrl = `${window.location.origin}/${route}${query ? '?' + query : ''}`;
			}
		}

		// 获取微信JS-SDK配置
		const configRes = await getConfig({ url: currentUrl });
		if (configRes.code !== 200) {
			throw new Error(configRes.msg || '获取微信配置失败');
		}

		const config = configRes.data;
		if (!config.appId || !config.signature) {
			throw new Error('微信配置不完整');
		}

		// 初始化微信JS-SDK
		const wx = await initWeChatJSSDK(config);

		// 配置分享给朋友
		configShareToFriend(wx, shareData);

		// 配置分享到朋友圈
		configShareToTimeline(wx, shareData);

		return wx;
		// #endif
		// #ifndef H5
		throw new Error('当前环境不支持微信JS-SDK');
		// #endif
	} catch (error) {
		console.error('初始化微信分享失败:', error);
		throw error;
	}
}

