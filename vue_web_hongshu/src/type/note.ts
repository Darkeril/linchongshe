export interface NoteSearch {
  id: string;
  title: string;
  src: string;
  uid: string;
  username: string;
  avatar: string;
  cid: string;
  cpid: string;
  urls: string;
  count: number;
  type: number;
  isLike: boolean; 
  likeCount: number;
  time: number | string;
}

export interface NoteInfo {
  id: string;
  title: string;
  content: string;
  noteCover: string;
  noteType: string;
  uid: string;
  username: string;
  avatar: string;
  imgList: Array<string>;
  type: number;
  likeCount: number;
  collectionCount: number;
  commentCount: number;
  tagList: Array<any>;
  time: string;
  isFollow: boolean,
  isLike: boolean,
  isCollection: boolean,
  pinned: string,
  province: string,
  city: string,
  district: string,
  address: string,
  relatedProducts: RelatedProduct[];
  auditStatus: string;
}

export interface NoteDTO {
  keyword: string;
  type: number;
  cid: string;
  cpid: string;
}

export interface EsRecordDTO {
  keyword: string;
  uid: string;
}

export interface RelatedProduct {
  id: string;
  cover: string;
  title: string;
  price: number;
  originalPrice?: number;
}