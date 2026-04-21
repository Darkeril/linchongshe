import {
	$request
} from '../utils/request.js'
import {
	baseUrl
} from '@/.env.js'

// === New upload helpers for Scheme A (upload first, then save JSON) ===
export const uploadImage = (filePath) => {
    return new Promise((resolve, reject) => {
        // H5/App/MP: uni.uploadFile 单文件
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
    
    // 如果只有一张图片，使用单文件上传
    if (paths.length === 1) {
        return uploadImage(paths[0]).then(url => [url])
    }
    
    // 多文件时，尝试使用批量上传接口（H5端可以使用FormData）
    // #ifdef H5
    return new Promise((resolve, reject) => {
        // H5端可以使用FormData批量上传
        const formData = new FormData()
        const uploadPromises = paths.map(filePath => {
            // 处理不同的文件路径格式
            let fetchUrl = filePath
            if (filePath.startsWith('blob:')) {
                // blob URL 直接使用
                fetchUrl = filePath
            } else if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
                // HTTP URL 直接使用
                fetchUrl = filePath
            } else {
                // 本地文件路径，在 H5 端通常不存在，这种情况应该使用 uni.uploadFile
                // 但为了兼容，尝试作为 blob URL 处理
                reject(new Error('H5端不支持本地文件路径，请使用blob URL或HTTP URL'))
                return
            }
            
            return fetch(fetchUrl)
                .then(res => {
                    if (!res.ok) {
                        throw new Error(`Failed to fetch file: ${res.statusText}`)
                    }
                    return res.blob()
                })
                .then(blob => {
                    const fileName = filePath.split('/').pop() || filePath.split('\\').pop() || 'image.jpg'
                    // 如果文件名包含查询参数，去掉
                    const cleanFileName = fileName.split('?')[0]
                    formData.append('files', blob, cleanFileName)
                    return { filePath, fileName: cleanFileName }
                })
                .catch(err => {
                    console.error('处理文件失败:', filePath, err)
                    throw new Error(`处理文件失败: ${filePath}, ${err.message}`)
                })
        })
        
        Promise.all(uploadPromises).then(() => {
            fetch(baseUrl + '/file/batch/upload', {
                method: 'POST',
                headers: {
                    'accessToken': uni.getStorageSync('uniapp_token') || '',
                    'Authorization': 'Bearer ' + (uni.getStorageSync('uniapp_token') || '')
                },
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                if (data && data.code === 200 && data.data) {
                    // 处理返回的数据格式：可能是数组，每个元素可能是对象（有url属性）或直接是URL字符串
                    const urls = data.data.map(file => {
                        if (typeof file === 'string') {
                            return file
                        } else if (file && file.url) {
                            return file.url
                        } else if (file && typeof file === 'object') {
                            // 尝试从对象中提取URL
                            return file.url || file.data?.url || JSON.stringify(file)
                        }
                        return file
                    }).filter(url => url && (url.startsWith('http://') || url.startsWith('https://')))
                    
                    console.log('批量上传成功，获取到的URLs:', urls)
                    if (urls.length === 0) {
                        reject(new Error('批量上传成功但未获取到有效的URL'))
                        return
                    }
                    resolve(urls)
                } else {
                    reject(new Error(data?.msg || '批量上传失败'))
                }
            })
            .catch(reject)
        }).catch(reject)
    })
    // #endif
    
    // #ifndef H5
    // 非H5端（小程序/APP），uni.uploadFile 不支持多文件，逐个上传
    return Promise.all(paths.map(p => uploadImage(p)))
    // #endif
}

export const saveNote = ({ requestId, noteDTO }) => {
    const rid = requestId || `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
    return $request({
        url: `/web/app/note/saveNote?requestId=${encodeURIComponent(rid)}`,
        method: 'POST',
        data: noteDTO
    })
}

// export const addNote = (params = {}) => {
// 	return $request({
// 		url: '/notes/publish',
// 		method: 'POST',
// 		data: params.notesVO
// 	})
// }
export const addNote = ({
    noteData,
    coverFile,
    uploadFiles,
    requestId,
    uploadedUrls, // 已上传的文件URL（可选，用于避免重复上传）
    uploadedCoverUrl, // 已上传的封面URL（可选）
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
                // 如果已经提供了上传后的URL，直接使用，避免重复上传
                let coverUrl = uploadedCoverUrl || ''
                let urls = uploadedUrls || []
                
                // 只有在没有已上传URL的情况下才上传文件
                if (!coverUrl && coverFile) {
                    coverUrl = await uploadImage(coverFile)
                }
                
                if (urls.length === 0 && uploadFiles && uploadFiles.length > 0) {
                    urls = await uploadImages(uploadFiles)
                }
                
                // 如果没有封面但有图片，使用第一张作为封面
                if (!coverUrl && urls.length > 0) {
                    coverUrl = urls[0]
                }
                
                // 更新noteData中的URL
                noteData.noteCover = coverUrl
                noteData.urls = urls
                
                // 调用saveNote接口
                const result = await saveNote({
                    requestId: rid,
                    noteDTO: noteData
                })
                resolve(result)
            } catch (err) {
                reject(err)
            }
        })()
	})
}

// export const getLastNotesByPage = (params = {}) => {
// 	return $request({
// 		url: '/notes/getLastNotesByPage?page=' + params.page + '&pageSize=' + params.pageSize,
// 		method: 'GET'
// 	})
// }
export const getLastNotesByPage = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	return $request({
		url: `/web/app/es/note/getRecommendNote/${page}/${limit}`,
		method: 'GET',
		showLoading: false, // 发现页推荐列表不显示 loading
		cache: false // 🔧 禁用缓存，确保每次获取最新数据
	})
}

export const getFindList = (params = {}) => {
	return $request({
		url: `/web/app/category/getCategoryTreeData`,
		method: 'GET'
	})
}

// export const getAttentionUserNotes = (params = {}) => {
// 	return $request({
// 		url: '/notes/getAttentionUserNotes?page=' + params.page + '&pageSize=' + params.pageSize,
// 		method: 'GET',
// 	})
// }
export const getAttentionUserNotes = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	const userId = params.userId
	
	// 构建查询参数
	let url = `/web/app/follower/getFollowList/${page}/${limit}`
	if (userId) {
		url += `?userId=${userId}`
	}
	
	console.log('🌐 API请求URL:', url)
	console.log('🌐 API请求参数:', { page, limit, userId })
	
	return $request({
		url: url,
		method: 'GET',
		showLoading: false, // 关注页列表不显示 loading
		cache: false // 禁用缓存，确保用户筛选时能获取最新数据
	})
}

// export const getNotesByUserId = (params = {}) => {
// 	return $request({
// 		url: '/notes/getNotesByUserId?page=' + params.page + '&pageSize=' + params.pageSize +
// 			'&authority=' + params.authority + '&type=' + params.type,
// 		method: 'GET'
// 	})
// }
export const getNotesByUserId = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	const userId = params.userId
	const type = params.type || 1
	const status = params.status || -1
	// 🔧 修复：如果传入了时间戳_t，添加到URL参数中，防止微信小程序HTTP缓存
	const url = params._t 
		? `/web/app/user/getTrendByUser/${page}/${limit}?_t=${params._t}`
		: `/web/app/user/getTrendByUser/${page}/${limit}`
	return $request({
		url: url,
		method: 'GET',
		data: {
			userId,
			type,
			status
		},
		cache: params._t ? false : undefined // 🔧 如果有时间戳，禁用缓存
	})
}

// export const getNotesByView = (params = {}) => {
// 	return $request({
// 		url: '/notes/getNotesByView?page=' + params.page + '&pageSize=' + params.pageSize + '&type=' +
// 			params.type + '&userId=' + params.userId,
// 		method: 'GET',
// 	})
// }
export const getNotesByView = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	const userId = params.userId
	const type = params.type || 1
	const status = params.status || -1
	return $request({
		url: `/web/app/user/getTrendByUser/${page}/${limit}`,
		method: 'GET',
		data: {
			userId,
			type,
			status
		}
	})
}

// export const getNotesByNotesId = (params = {}) => {
// 	return $request({
// 		url: '/notes/getNotesByNotesId?notesId=' + params.notesId,
// 		method: 'GET'
// 	})
// }
export const getNotesByNotesId = (params = {}) => {
	const noteId = params.notesId
	return $request({
		url: `/web/app/note/getNoteById`,
		method: 'GET',
		data: {
			noteId
		}
	})
}

// export const praiseOrCancelNotes = (params = {}) => {
// 	return $request({
// 		url: '/notes/praiseNotes?notesId=' + params.notesId + '&userId=' + params.userId +
// 			'&targetUserId=' + params.targetUserId,
// 		method: 'POST'
// 	})
// }
export const praiseOrCancelNotes = (params = {}) => {
	const likeOrCollectionId = params.notesId
	const publishUid = params.targetUserId
	const type = 1
	return $request({
		url: `/web/app/likeOrCollection/likeOrCollectionByDTO`,
		method: 'POST',
		data: {
			likeOrCollectionId,
			publishUid,
			type
		},
		showLoading: false
	}).catch(error => {
		// 防止未捕获的Promise导致控制台报错，同时给出友好提示
		console.warn('点赞失败:', error)
		uni.showToast({ title: '点赞失败，请稍后重试', icon: 'none' })
		return { code: 500, msg: 'like failed', error }
	})
}

// export const collectOrCancelNotes = (params = {}) => {
// 	return $request({
// 		url: '/notes/collectNotes?notesId=' + params.notesId + '&userId=' + params.userId +
// 			'&targetUserId=' + params.targetUserId,
// 		method: 'POST'
// 	})
// }
export const collectOrCancelNotes = (params = {}) => {
	const likeOrCollectionId = params.notesId
	const publishUid = params.targetUserId
	const type = 3
	return $request({
		url: `/web/app/likeOrCollection/likeOrCollectionByDTO`,
		method: 'POST',
		data: {
			likeOrCollectionId,
			publishUid,
			type
		},
		showLoading: false
	}).catch(error => {
		console.warn('收藏失败:', error)
		uni.showToast({ title: '收藏失败，请稍后重试', icon: 'none' })
		return { code: 500, msg: 'collect failed', error }
	})
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

// 个人主页统计（发布笔记数、获赞数等）
// 新接口：GET /note/noteCount?userId=xxx
// 返回：{ rejectedCount, likeCount, allCount, pendingCount, approvedCount }
export const getNotesCountByUserId = (params = {}) => {
    const userId = params.userId
    return $request({
        url: '/web/note/noteCount',
        method: 'GET',
        data: { userId }
    })
}

// export const getNotesCategoryList=()=>{
// 	return $request({
// 		url:'/notes/category/getNotesCategoryList',
// 		method:'GET'
// 	})
// }
export const getNotesCategoryList = () => {
	return $request({
		url: `/web/app/category/getCategoryTreeData`,
		method: 'GET'
	})
}


// export const getNotesByCategoryId=(params={})=>{
// 	return $request({
// 		url:'/notes/getNotesByCategoryId?categoryId='+params.categoryId+'&page='+params.page+'&pageSize='+params.pageSize+'&type='+params.type+'&notesType='+params.notesType,
// 		method:'GET'
// 	})
// }
export const getNotesByCategoryId = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	const body = {
		cid: params.cid || '',
		cpid: params.cpid || '',
		keyword: params.keyword || '',
		type: params.type || 0
	}
	return $request({
		url: `/web/app/es/note/getNoteByDTO/${page}/${limit}`,
		method: 'POST',
		data: body,
		showLoading: false, // 发现页分类列表不显示 loading
		cache: false // 🔧 禁用缓存，确保每次获取最新数据
	})
}

/**
 * 获取视频笔记
 * @param {Object} params 参数对象
 * @param {Number} params.page 页码，默认1
 * @param {Number} params.pageSize 每页数量，默认10
 * @returns {Promise} 返回Promise对象
 */
export const getVideoNote = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	return $request({
		url: `/web/app/es/note/getVideoNote/${page}/${limit}`,
		method: 'GET',
		showLoading: false, // 发现页视频列表不显示 loading
		cache: false // 🔧 禁用缓存，确保每次获取最新数据
	})
}

export const deleteNotes = (params = {}) => {
	return $request({
		url: '/web/app/note/deleteNotes?notesId=' + params.notesId,
		method: 'DELETE'
	})
}