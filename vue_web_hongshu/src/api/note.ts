import request from "@/utils/request";

/**
 * 根据笔记id获取笔记
 * @param noteId 笔记id
 * @returns 笔记
 */
export const getNoteById = (noteId: string) => {
  return request<any>({
    url: "/web/note/getNoteById", // mock接口
    method: "get",
    params: {
      noteId,
    },
  });
};

/**
 * 保存笔记（使用文件URL）
 * @param requestId 请求ID
 * @param noteDTO 笔记实体（包含文件URL）
 * @returns 笔记id
 */
export const saveNote = (requestId: string, noteDTO: any) => {
  return request<any>({
    url: "/web/note/saveNote",
    method: "post",
    params: { requestId },
    data: noteDTO,
  });
};

/**
 * 保存笔记（使用文件，已废弃，保留用于兼容）
 * @param data 笔记实体
 * @returns 笔记id
 */
export const saveNoteByDTO = (data: any) => {
  return request<any>({
    url: "/web/note/saveNoteByDTO", // mock接口
    method: "post",
    data: data,
    headers: { "Content-Type": "multipart/form-data;boundary=----WebKitFormBoundaryk4ZvuPo6pkphe7Pl" },
  });
};

/**
 * 更新笔记
 * @param data 笔记实体
 * @returns 笔记id
 */
export const updateNoteByDTO = (data: any) => {
  return request<any>({
    url: "/web/note/updateNoteByDTO", // mock接口
    method: "post",
    data: data,
    headers: { "Content-Type": "multipart/form-data;boundary=----WebKitFormBoundaryk4ZvuPo6pkphe7Pl" },
  });
};

/**
 * 置顶笔记
 * @param noteId 
 * @returns 
 */
export const pinnedNote = (noteId: string) => {
  return request<any>({
    url: "/web/note/pinnedNote", // mock接口
    method: "get",
    params: {
      noteId,
    },
  });
};

/**
 * 删除笔记
 * @param data 
 * @returns 
 */
export const deleteNoteByIds = (data: any) => {
  return request<any>({
    url: "/web/note/deleteNoteByIds", // mock接口
    method: "post",
    data: data,
  });
};

export const getNoteCount = (userId:string) => {
  return request<any>({
    url: `/web/note/noteCount`,
    method: 'get',
    params: {
      userId
    }
  });
}

export const getLikeCount = (userId:string) => {
  return request<any>({
    url: `/web/note/likeCount`,
    method: 'get',
    params: {
      userId
    }
  });
}
