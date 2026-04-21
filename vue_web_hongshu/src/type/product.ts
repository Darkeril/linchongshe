export interface ProductSearch {
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
  isCollection: boolean; 
  likeCount: number;
  time: number | string;
}

export interface ProductInfo {
  id: string;
  uid: string;
  title: string;
  description: string;
  type: number;
  productType: number;
  image: string;
  cover: string;
  urls: string;
  imgList: string[];
  price: number;
  originalPrice: number;
  postType: number;
  province: string;
  city: string;
  district: string;
  address: string;
  status: number;
  username: string;
  avatar: string;
  time: string;
  likeCount: number;
  collectionCount: number;
  commentCount: number;
  isCollection: boolean;
  isFollow: boolean;   
  isLike: boolean;  
  pinned: string;
  auditStatus: string   
}

export interface EsProductDTO {
  keyword: string;
  type: number;
  cid: string;
  cpid: string;
}
