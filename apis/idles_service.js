import {
	$request
} from '../utils/request.js'
import {
	baseUrl
} from '@/.env.js'

// 从 notes_service 导入上传方法
export const uploadImage = (filePath) => {
    return new Promise((resolve, reject) => {
        // 统一上传到 file 模块，而不是 web 模块
        uni.uploadFile({
            url: baseUrl + `/file/upload`,
            filePath: filePath,
            name: 'file',
            header: {
                'accessToken': uni.getStorageSync('uniapp_token') || '',
                'Authorization': 'Bearer ' + (uni.getStorageSync('uniapp_token') || '')
            },
            success: (res) => {
                try {
                    const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
                    if (data && data.code === 200) {
                        // file 模块返回的是 { url, name } 格式
                        resolve(data.data?.url || data.data)
                    } else {
                        reject(new Error(data?.msg || '上传失败'))
                    }
                } catch (e) {
                    reject(e)
                }
            },
            fail: reject
        })
    })
}

export const uploadImages = (filePaths = []) => {
    const paths = Array.isArray(filePaths) ? filePaths : []
    if (paths.length === 0) return Promise.resolve([])
    return Promise.all(paths.map(p => uploadImage(p)))
}

// export const updateNotesViewCount = (params = {}) => {
// 	return $request({
// 		url: '/notes/viewNotes?notesId=' + params.notesId,
// 		method: 'POST'
// 	})
// }
export const updateNotesViewCount = (params = {}) => {
	return $request({
		url: '/web/app/note/viewNotes?notesId=' + params.notesId,
		method: 'POST',
		showErrorToast: false, // 不显示错误提示
		showLoading: false // 不显示加载状态
	}).catch(error => {
		// 静默处理404错误，不影响用户体验
		console.warn('更新查看数量失败:', error);
		return Promise.resolve({ code: 200, msg: 'success' });
	})
}

export const getIdleCategoryList = (params = {}) => {
	return $request({
		url: `/idle/app/category/getCategoryTreeData`,
		method: 'GET',
		cache: false // 刷新时拉取最新分类（含图标等）
	})
}

export const getLastIdleByPage = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	return $request({
		url: `/idle/app/es/product/recommendProduct/${page}/${limit}`,
		method: 'GET',
		showLoading: false, // 市集页推荐列表不显示 loading
		cache: false // 🔧 禁用缓存，确保每次获取最新数据
	})
}

/**
 * 获取视频商品
 * @param {Object} params 
 * @param {Number} params.page 页码
 * @param {Number} params.pageSize 每页数量
 */
export const getVideoProduct = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	return $request({
		url: `/idle/app/es/product/getVideoProduct/${page}/${limit}`,
		method: 'GET',
		showLoading: false, // 市集页视频列表不显示 loading
		cache: false // 🔧 禁用缓存，确保每次获取最新数据
	})
}

export const getIdlesByCategoryId = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	const body = {
		cid: params.cid || '',
		cpid: params.cpid || '',
		keyword: params.keyword || '',
		type: params.type || 1
	}
	return $request({
		url: `/idle/app/es/product/getProductByDTO/${page}/${limit}`,
		method: 'POST',
		data: body,
		showLoading: false, // 市集页列表不显示 loading
		cache: false // 🔧 禁用缓存，确保每次获取最新数据
	})
}

export const getProductById = (params = {}) => {
	const productId = params.productId
	// 🔧 修复：添加参数验证，防止传递 undefined
	if (!productId || productId === 'undefined' || productId === 'null') {
		console.error('❌ getProductById: productId 参数无效', productId)
		return Promise.reject({
			code: 400,
			message: '商品ID参数无效',
			data: null
		})
	}
	// 🔧 修复：微信小程序端 GET 请求参数需要通过 URL 查询字符串传递
	// 将 productId 拼接到 URL 中，确保微信小程序端能正确传递参数
	const url = `/idle/app/product/getProductById?productId=${encodeURIComponent(productId)}`
	return $request({
		url: url,
		method: 'GET'
	})
}

// 根据用户获取闲置商品
export const getUserIdleProducts = ({ currentPage = 1, pageSize = 10, userId, type, status } = {}) => {
    // 构建查询参数（兼容微信小程序）
    const queryParams = []
    if (userId) queryParams.push(`userId=${encodeURIComponent(userId)}`)
    if (type !== undefined) queryParams.push(`type=${encodeURIComponent(type)}`)
    if (status) queryParams.push(`status=${encodeURIComponent(status)}`)
    
    const queryString = queryParams.length > 0 ? queryParams.join('&') : ''
    const url = `/web/user/getProductByUser/${currentPage}/${pageSize}${queryString ? '?' + queryString : ''}`
    
    console.log('getUserIdleProducts 请求URL:', url)
    console.log('getUserIdleProducts 请求参数:', { currentPage, pageSize, userId, type, status })
    
    return $request({
        url: url,
        method: 'GET',
        cache: false, // 禁用缓存，确保每次都是真实请求
        showLoading: false // 不显示loading，由页面控制
    })
}

// 获取用户商品列表（支持搜索，用于笔记关联商品）
// 注意：后端代码要求所有参数都必须传递，即使值为空字符串，否则会调用 null.toString() 报错
// 与web端保持一致：web端使用axios，undefined参数会被自动过滤，但后端需要所有参数
export const getUserProducts = ({ page = 1, pageSize = 10, keyword = '', userId } = {}) => {
    // 构建查询参数（必须传递所有参数，即使是空字符串）
    const queryParams = []
    
    // 必须参数：pageNum 和 pageSize
    queryParams.push(`pageNum=${encodeURIComponent(page || 1)}`)
    queryParams.push(`pageSize=${encodeURIComponent(pageSize || 10)}`)
    
    // 必须参数：uid（userId），即使为空也要传递空字符串
    queryParams.push(`uid=${encodeURIComponent(userId || '')}`)
    
    // 必须参数：title（keyword），即使为空也要传递空字符串
    // 后端代码会调用 map.get("title").toString()，如果为null会报错
    // 所以必须传递，即使是空字符串
    queryParams.push(`title=${encodeURIComponent(keyword ? keyword.trim() : '')}`)
    
    const queryString = queryParams.join('&')
    const url = `/web/user/getUserProducts?${queryString}`
    
    console.log('getUserProducts 请求URL:', url)
    console.log('getUserProducts 请求参数:', { page, pageSize, keyword, userId })
    
    return $request({
        url: url,
        method: 'GET',
        cache: false,
        showLoading: false
    })
}

// 保存商品（上传完成后，提交 JSON）
export const saveProduct = ({ requestId, productDTO }) => {
    const rid = requestId || `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
    return $request({
        url: `/idle/app/product/saveProduct?requestId=${encodeURIComponent(rid)}`,
        method: 'POST',
        data: productDTO,
        showLoading: true,
        showErrorToast: true,
    })
}

// 新增商品（一次性提交文件和数据）
export const addProduct = ({
    noteData,
    coverFile,
    uploadFiles,
    requestId,
}) => {
    // 兼容后端新增的 requestId 要求；如未传则本地生成
    const rid = requestId || `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
	let files = []
	if (coverFile) {
		files.push({
			name: 'coverFile',
			uri: coverFile
		})
	}
	if (uploadFiles && uploadFiles.length > 0) {
		uploadFiles.forEach(item => {
			files.push({
				name: 'uploadFiles',
				uri: item
			})
		})
	}
	return new Promise((resolve, reject) => {
        // 统一处理：H5 和 App/小程序端都使用先上传文件再保存的方式
        (async () => {
            try {
                // 先将所有文件上传到 file 模块获取URL
                let coverUrl = ''
                let urls = []
                
                if (coverFile) {
                    coverUrl = await uploadImage(coverFile)
                }
                
                if (uploadFiles && uploadFiles.length > 0) {
                    urls = await uploadImages(uploadFiles)
                }
                
                // 如果没有封面但有图片，使用第一张作为封面
                if (!coverUrl && urls.length > 0) {
                    coverUrl = urls[0]
                }
                
                // 更新noteData中的URL
                noteData.cover = coverUrl
                noteData.urls = urls
                
                // 调用saveProduct接口
                const result = await saveProduct({
                    requestId: rid,
                    productDTO: noteData
                })
                resolve(result)
            } catch (err) {
                reject(err)
            }
        })()
	})
}

export const praiseOrCancelIdles = (params = {}) => {
	const collectionId = params.notesId
	const publishUid = params.targetUserId
	const type = 1
	return $request({
		url: `/idle/app/collection/collectionByDTO`,
		method: 'POST',
		data: {
			collectionId,
			publishUid,
			type
		}
	})
}

// 根据关键词搜索商品
export const getProductByKeyword = (params = {}) => {
	const page = params.currentPage || params.page || 1
	const limit = params.pageSize || 10
	const keyword = params.keyword || ''
	
	return $request({
		url: `/idle/app/product/getProductByKeyword/${page}/${limit}`,
		method: 'GET',
		data: {
			keyword: keyword
		},
		showLoading: false // 商品搜索不显示 loading
	})
}