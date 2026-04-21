import { weChatTimeFormat } from '@/utils/util.js'

export function mapNoteItem(item = {}) {
  // 优先使用 nid，如果没有则使用 id，确保至少有一个ID字段
  const noteId = item.nid || item.id || ''
  return {
    ...item,
    id: noteId,
    nid: noteId, // 确保 nid 字段存在，用于跳转（后端返回的是 nid）
    coverPicture: item.noteCover || item.coverPicture || '',
    avatarUrl: item.avatar || item.avatarUrl || '',
    nickname: item.username || item.nickname || '',
    notesLikeNum: parseInt(item.likeCount, 10) || 0,
    commentCount: parseInt(item.commentCount, 10) || 0,
    notesCollectNum: parseInt(item.collectCount, 10) || 0,
    isLike: !!item.isLike,
    isCollect: !!item.isCollect,
    notesType: parseInt(item.noteType, 10) || 0,
    content: item.content || '',
    title: item.title || '',
    time: item.time || item.time, // 保留原始时间戳
    updateTime: item.time ? weChatTimeFormat(item.time) : '',
    createTime: item.time ? weChatTimeFormat(item.time) : '',
    belongUserId: item.uid || item.belongUserId || '',
    noteCoverHeight: parseInt(item.noteCoverHeight, 10) || 0,
    views: parseInt(item.viewCount, 10) || 0,
    imgList: item.imgUrls || item.imgList || [],
    location: item.location,
    // 审核状态：'0'-审核中, '1'-审核通过, '2'-审核未通过
    auditStatus: item.auditStatus || '1', // 默认为审核通过
    // 地址相关字段（保持原始值，不设置默认空字符串）
    province: item.province,
    city: item.city,
    district: item.district,
    address: item.address,
    // 兼容后端不同字段：视频地址
    videoUrl: item.videoUrl || item.video || item.videoPath || '',
    // 其它需要兼容的字段可继续补充
  }
}