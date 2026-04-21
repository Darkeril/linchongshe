// 创建全局的图片缓存实例
const imageCache = new Map();

// 限制缓存大小
const MAX_CACHE_SIZE = 200; // 增加缓存大小

// 图片缓存工具类
export const ImageCacheUtil = {
	// 获取缓存的图片
	getCachedImage(url) {
		if (imageCache.has(url)) {
			const cached = imageCache.get(url);
			// 更新访问时间
			cached.lastAccess = Date.now();
			return cached;
		}
		return null;
	},

	// 存储图片到缓存
	cacheImage(url, imageInfo) {
		// 如果缓存达到上限，删除最久未访问的条目
		if (imageCache.size >= MAX_CACHE_SIZE) {
			this.cleanupCache();
		}
		
		imageCache.set(url, {
			...imageInfo,
			lastAccess: Date.now(),
			url: url
		});
	},

	// 清理缓存（删除最久未访问的条目）
	cleanupCache() {
		if (imageCache.size === 0) return;
		
		// 按最后访问时间排序，删除最久未访问的
		const entries = Array.from(imageCache.entries());
		entries.sort((a, b) => a[1].lastAccess - b[1].lastAccess);
		
		// 删除前20%的条目
		const deleteCount = Math.floor(entries.length * 0.2);
		for (let i = 0; i < deleteCount; i++) {
			imageCache.delete(entries[i][0]);
		}
	},

	// 清除缓存
	clearCache() {
		imageCache.clear();
	},

	// 获取缓存统计信息
	getCacheStats() {
		return {
			size: imageCache.size,
			maxSize: MAX_CACHE_SIZE,
			usage: `${imageCache.size}/${MAX_CACHE_SIZE}`
		};
	},

	// 预加载图片
	async preloadImage(url) {
		if (this.getCachedImage(url)) {
			return this.getCachedImage(url);
		}

		return new Promise((resolve) => {
			uni.getImageInfo({
				src: url,
				success: (res) => {
					const imageInfo = {
						height: res.height,
						width: res.width,
						path: res.path,
						url: url
					};
					this.cacheImage(url, imageInfo);
					resolve(imageInfo);
				},
				fail: () => {
					// 加载失败时的默认值
					const defaultInfo = {
						height: 200,
						width: 200,
						path: url,
						url: url
					};
					resolve(defaultInfo);
				}
			});
		});
	}
};