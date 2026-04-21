import {
	$request
} from '../utils/request.js'

export const addComment = (params = {}) => {
	const body = {
		content: params.content,
		level: params.level || 1,
		pid: params.parentId,
		replyId: params.replyUserId || '0',
		replyUid: params.replyUserId,
	}
	
	// 添加图片URL字段
	if (params.pictureUrl) {
		body.pictureUrl = params.pictureUrl;
	}
	
	// 支持笔记评论和商品评论
	if (params.notesId) {
		// 笔记评论
		body.nid = params.notesId;
		body.noteUid = params.commentUserId;
	} else if (params.productId) {
		// 商品评论
		body.productId = params.productId;
		body.productUid = params.commentUserId;
	}
	
	return $request({
		url: `/web/app/comment/saveCommentByDTO`,
		method: 'POST',
		data: body
	})
}

export const addCommentSync = (commentIds = []) => {
	return $request({
		url: `/web/app/comment/syncCommentByIds`,
		method: 'POST',
		data: commentIds
	})
}

export const getCommentCountByNotesId = (params = {}) => {
	return $request({
		url: '/web/app/comment/getCommentCount?notesId=' + params.notesId,
		method: 'GET'
	})
}

// export const getCommentFirstListByNotesId=(params={})=>{
// 	return $request({
// 		url:'/comment/getCommentFirstList?notesId='+params.notesId+'&page='+params.page+'&pageSize='+params.pageSize,
// 		method:'GET'
// 	})
// }
export const getCommentFirstListByNotesId = (params = {}) => {
	const noteId = params.notesId
	const page = params.page || 1
	const limit = params.pageSize || 10
	return $request({
		url: `/web/app/comment/getCommentWithCommentByNoteId/${page}/${limit}?noteId=${noteId}`,
		method: 'GET'
	})
}

export const getCommentWithCommentByProductId = (params = {}) => {
	const productId = params.productId
	const page = params.page || 1
	const limit = params.pageSize || 10
	return $request({
		url: `/web/app/comment/getCommentWithCommentByProductId/${page}/${limit}?productId=${productId}`,
		method: 'GET'
	})
}

// export const getCommentSecondListByNotesId = (params = {}) => {
// 	return $request({
// 		url: '/comment/getCommentSecondList?notesId=' + params.notesId + '&parentId=' + params.parentId +
// 			'&page=' + params.page + '&pageSize=' + params.pageSize,
// 		method: 'GET'
// 	})
// }
export const getCommentSecondListByNotesId = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	const oneCommentId = params.parentId
	
	// 支持笔记评论和商品评论
	if (params.notesId) {
		// 笔记评论
		return $request({
			url: `/web/app/comment/getTwoCommentByOneCommentId/${page}/${limit}?noteId=${params.notesId}&oneCommentId=${oneCommentId}`,
			method: 'GET'
		})
	} else if (params.productId) {
		// 商品评论
		return $request({
			url: `/web/app/comment/getTwoCommentByOneCommentId/${page}/${limit}?productId=${params.productId}&oneCommentId=${oneCommentId}`,
			method: 'GET'
		})
	}
}

// export const praiseOrCancelComment = (params = {}) => {
// 	return $request({
// 		url: '/comment/praiseComment?commentId=' + params.commentId + '&userId=' + params.userId +
// 			'&targetUserId=' + params.targetUserId,
// 		method: 'POST'
// 	})
// }
export const praiseOrCancelComment = (params = {}) => {
	const likeOrCollectionId = params.commentId
	const publishUid = params.targetUserId
	const type = 2
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
		console.warn('评论点赞失败:', error)
		uni.showToast({ title: '操作失败，请稍后重试', icon: 'none' })
		return { code: 500, msg: 'comment like failed', error }
	})
}

export const setNotesTopComment = (params = {}) => {
	return $request({
		url: '/web/app/comment/setTopComment?commentId=' + params.commentId,
		method: 'POST'
	})
}

export const deleteNotesComment = (params = {}) => {
	return $request({
		url: '/web/app/comment/deleteComment?commentId=' + params.commentId,
		method: 'DELETE'
	})
}
