import axios from '@/api/interceptor';

export interface AlbumRecord {
  id?: number;
  title: string;
  sort?: number;
  albumCover?: string;
  /** 所属 web 用户主键，与 web_user.id 一致（字符串） */
  uid?: string;
  type?: number;
  imgCount?: number;
}

// 获取专辑详情
export function getAlbumById(id: number | string) {
  return axios({
    url: `/web/album/${id}`,
    method: 'get',
  });
}

// 专辑列表
export function getAlbumList(query: any) {
  return axios({
    url: '/web/album/list',
    method: 'get',
    params: query,
  });
}

// 新增专辑
export function addAlbum(data: AlbumRecord) {
  return axios({
    url: '/web/album',
    method: 'post',
    data,
  });
}

// 编辑专辑
export function editAlbum(data: AlbumRecord) {
  return axios({
    url: '/web/album',
    method: 'put',
    data,
  });
}

/** 管理端：专辑内笔记分页（需 web:album:query） */
export function getAlbumNotes(
  albumId: string | number,
  params?: { current?: number; size?: number }
) {
  return axios({
    url: `/web/album/${albumId}/notes`,
    method: 'get',
    params: {
      current: params?.current ?? 1,
      size: params?.size ?? 10,
    },
  });
}

// 批量删除专辑
export function deleteBatchAlbum(ids: number[]) {
  // 后端接口是 @DeleteMapping("/{ids}")，Spring会自动将逗号分隔的字符串解析为数组
  return axios({
    url: `/web/album/${ids.join(',')}`,
    method: 'delete',
  });
}









