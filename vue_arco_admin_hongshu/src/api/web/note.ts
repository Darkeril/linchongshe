import axios from 'axios';

export interface NoteRecord {
  searchValue?: any;
  createBy?: any;
  createTime?: any;
  updateBy?: any;
  updateTime?: any;
  remark?: any;
  id: number | undefined;
  pid?: any;
  ancestors?: any;
  title?: string;
  sort?: any;
  normalCover?: string;
  phone?: any;
  email?: any;
  status?: any;
  delFlag?: any;
  parentName?: any;
  children?: any[];
}

// 查询导航栏列表
export function listNote(query: any) {
  return axios({
    url: '/web/note/list',
    method: 'get',
    params: query
  })
}

// 查询导航栏列表
export function unAuditList(query: any) {
  return axios({
    url: '/web/note/unAuditList',
    method: 'get',
    params: query
  })
}

// 查询导航栏详细
export function getNote(id: number) {
  return axios({
    url: `/web/note/${id}`,
    method: 'get'
  })
}

// 新增导航栏
export function addNote(data: NoteRecord) {
  return axios({
    url: '/web/note',
    method: 'post',
    data,
    headers: { "Content-Type": "multipart/form-data;boundary=----WebKitFormBoundaryk4ZvuPo6pkphe7Pl" },
  })
}

// 修改导航栏
export function updateNote(data: NoteRecord) {
  return axios({
    url: '/web/note',
    method: 'put',
    data,
    headers: { "Content-Type": "multipart/form-data;boundary=----WebKitFormBoundaryk4ZvuPo6pkphe7Pl" },
  })
}

// 删除导航栏
export function delNote(id: number) {
  return axios({
    url: `/web/note/${id}`,
    method: 'delete'
  })
}

// 审核笔记
export function auditNote(data: { noteId: string | number; auditType: string; }) {
  return axios({
    url: '/web/note/auditNote',
    method: 'put',
    data
  })
}

// 批量审核笔记
export function batchAuditNote(data: { noteIds: string; auditType: string; }) {
  return axios({
    url: '/web/note/batchAuditNote',
    method: 'put',
    data,
  })
}

/** 数据中心-内容分析-笔记数据列表（分页） */
export interface NoteDataCenterParams {
  uid?: string;  // 按用户筛选（数据中心-内容分析）
  noteType?: number;  // 笔记题材：1=图文 2=视频 3=live图
  startTime?: string;
  endTime?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface NoteDataCenterRecord {
  id: string;
  title: string;
  cover: string;
  publishTime: string;
  notApproved: boolean;
  exposure: number;
  views: number;
  coverCtr: string;
  likes: number;
  comments: number;
  favorites: number;
  follows: number;
  shares: number;
  avgDuration: string;
  danmaku: number;
}

export function getNoteDataCenterList(params: NoteDataCenterParams) {
  return axios({
    url: '/web/note/dataCenterList',
    method: 'get',
    params,
  })
}

/** 观众画像：性别/年龄/城市/兴趣（value 为占比或人数） */
export interface NoteAudienceProfile {
  gender?: { name: string; value: number }[];
  age?: { name: string; value: number }[];
  region?: { name: string; value: number }[];
  interest?: { name: string; value: number }[];
}

/** 笔记数据详情（单条笔记的曝光、观看、来源、时段等） */
export interface NoteDataCenterDetailVo {
  noteInfo?: {
    id: string;
    title: string;
    cover: string;
    author: string;
    publishTime: string;
    tags: string;
  };
  overview?: { label: string; value: unknown; ring: number; ringText: string }[];
  exposureTrend?: { date: string; cnt: number }[];
  viewTrend?: { date: string; cnt: number }[];
  viewSource?: { name: string; value: number }[];
  viewTimeSlot?: number[];
  avgDurationSec?: number;
  completionRate?: number;
  twoSecExitRate?: number;
  shares?: number;
  likes?: number;
  comments?: number;
  favorites?: number;
  audienceProfile?: NoteAudienceProfile;
}

export function getNoteDataCenterDetail(notesId: string, days?: number) {
  return axios({
    url: '/web/note/dataCenterDetail',
    method: 'get',
    params: { notesId, days: days ?? 30 },
  })
}

// ES重置
export function refreshNoteDate() {
  return axios({
    url: '/web/es/note/refreshNoteData',
    method: 'post',
  })
}

// ES重置
export function clearAllRecordDate() {
  return axios({
    url: '/web/es/record/clearAllRecord',
    method: 'post',
  })
}
