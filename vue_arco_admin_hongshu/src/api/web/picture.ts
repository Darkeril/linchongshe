import { request } from '@/utils/request';

export interface PictureRecord {
  uid?: string;
  fileUid: string;
  pictureSortUid: string;
  picName: string;
}

export interface UploadPicsByUrlParams {
  source: string;
  userUid: string;
  adminUid: string;
  projectName: string;
  sortName: string;
  token: string;
  urlList: string[];
}

export interface UploadPicsByUrlResponse {
  code: string;
  data: Array<{
    uid: string;
    picName: string;
  }>;
  message?: string;
}

export interface AddPictureResponse {
  code: string;
  message?: string;
}

// 通过URL上传图片
export function uploadPicsByUrl(data: UploadPicsByUrlParams) {
  return request<UploadPicsByUrlResponse>({
    url: '/web/picture/uploadByUrl',
    method: 'post',
    data,
  });
}

// 添加图片到图片管理
export function addPicture(data: PictureRecord[]) {
  return request<AddPictureResponse>({
    url: '/web/picture/add',
    method: 'post',
    data,
  });
}








