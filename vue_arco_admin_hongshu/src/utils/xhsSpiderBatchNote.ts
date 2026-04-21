import { addNote } from '@/api/web/note';
import {
  downloadXiaohongshuImage,
  downloadXiaohongshuVideo,
  type XiaohongshuItem,
} from '@/api/web/xiaohongshu';

function isOssUrl(url: string | undefined): boolean {
  if (!url) return false;
  return !url.includes('xhscdn.com') && !url.includes('xiaohongshu.com');
}

function isXiaohongshuUrl(url: string | undefined): boolean {
  if (!url) return false;
  return url.includes('xhscdn.com') || url.includes('xiaohongshu.com');
}

function parseOssUrlFromDownloadResponse(response: {
  code: string | number;
  data?: unknown;
  message?: string;
}): string {
  if (response.code !== 'success' && response.code !== 200) {
    throw new Error((response as { message?: string }).message || '下载失败');
  }
  const responseData = response.data;
  if (typeof responseData === 'string') return responseData;
  if (responseData && typeof responseData === 'object' && 'url' in responseData) {
    return (responseData as { url: string }).url;
  }
  if (responseData && typeof responseData === 'object' && 'data' in responseData) {
    return (responseData as { data: string }).data;
  }
  throw new Error('下载响应数据格式错误');
}

/** 与爬虫页单条「发布」映射一致，供批量提交复用 */
export function mapXhsSpiderItemToDraft(item: XiaohongshuItem): {
  title: string;
  content: string;
  noteType: string;
  urls: string[];
  noteCover: string;
} {
  let noteType = '0';
  if (item.type === 'video') {
    noteType = '2';
  } else if (item.type === 'image') {
    noteType = '1';
  }

  const title = item.title || '无标题';
  const content = item.content || '';
  const noteCover =
    item.cover || (item.images && item.images.length > 0 ? item.images[0] : '') || '';

  let urls: string[] = [];
  if (item.type === 'video' && item.video) {
    noteType = '2';
    urls = [item.video];
  } else if (item.images && item.images.length > 0) {
    const maxImages = 9;
    urls = item.images.slice(0, maxImages);
    // 业务约定：单张、多张图片均记为「图片」1；无图纯文为 0
    noteType = '1';
  }

  return { title, content, noteType, urls, noteCover };
}

/**
 * 将小红书外链转为 OSS（与 edit-note 确定逻辑一致，可选 cookie）
 */
export async function resolveXhsMediaToOssForNote(params: {
  noteType: string;
  urls: string[];
  noteCover: string;
  cookie?: string;
}): Promise<{ finalUrls: string[]; finalNoteCover: string | undefined }> {
  let finalUrls = [...params.urls];
  let finalNoteCover = params.noteCover || undefined;

  if (params.noteType === '2' && finalUrls.length > 0) {
    const videoUrl = finalUrls[0];
    if (isXiaohongshuUrl(videoUrl) && !isOssUrl(videoUrl)) {
      const response = await downloadXiaohongshuVideo(videoUrl, params.cookie);
      finalUrls = [parseOssUrlFromDownloadResponse(response)];
    }
  } else if (finalUrls.length > 0) {
    const urlsToDownload = finalUrls.filter(
      (url) => isXiaohongshuUrl(url) && !isOssUrl(url)
    );
    if (urlsToDownload.length > 0) {
      const downloadPromises = finalUrls.map(async (url, index) => {
        if (isOssUrl(url)) {
          return { success: true as const, url, index };
        }
        if (isXiaohongshuUrl(url)) {
          try {
            const response = await downloadXiaohongshuImage(url, params.cookie);
            const ossUrl = parseOssUrlFromDownloadResponse(response);
            return { success: true as const, url: ossUrl, index };
          } catch (error: unknown) {
            const msg = error instanceof Error ? error.message : '下载失败';
            return { success: false as const, url: null, index, error: msg };
          }
        }
        return { success: true as const, url, index };
      });

      const results = await Promise.all(downloadPromises);
      const successful = results.filter((r) => r.success && r.url);
      if (successful.length === 0) {
        throw new Error('所有图片下载失败，无法保存');
      }

      finalUrls = successful.sort((a, b) => a.index - b.index).map((r) => r.url as string);

      if (finalUrls.length > 0 && isXiaohongshuUrl(finalNoteCover || '')) {
        const [firstImageUrl] = finalUrls;
        finalNoteCover = firstImageUrl;
      }
    }
  }

  if (finalNoteCover && isXiaohongshuUrl(finalNoteCover) && !isOssUrl(finalNoteCover)) {
    try {
      const response = await downloadXiaohongshuImage(finalNoteCover, params.cookie);
      finalNoteCover = parseOssUrlFromDownloadResponse(response);
    } catch {
      // 与 edit-note 一致：封面失败保留原 URL
    }
  }

  return { finalUrls, finalNoteCover };
}

export async function submitSpiderItemAsNote(params: {
  item: XiaohongshuItem;
  uid: string;
  author: string;
  cpid: number;
  cookie?: string;
}): Promise<void> {
  const draft = mapXhsSpiderItemToDraft(params.item);
  if (draft.noteType === '2' && draft.urls.length === 0) {
    throw new Error('视频笔记缺少视频地址');
  }
  if (draft.noteType !== '2' && draft.urls.length === 0 && !draft.noteCover) {
    throw new Error('缺少图片或封面');
  }

  const { finalUrls, finalNoteCover } = await resolveXhsMediaToOssForNote({
    noteType: draft.noteType,
    urls: draft.urls,
    noteCover: draft.noteCover,
    cookie: params.cookie,
  });

  const noteData: Record<string, unknown> = {
    title: draft.title,
    cpid: params.cpid,
    noteType: draft.noteType,
    auditStatus: '1',
    fromType: '2',
    noteCover: finalNoteCover,
    content: draft.content,
    address: undefined,
    province: undefined,
    city: undefined,
    district: undefined,
    uid: params.uid,
    author: params.author,
    urls: finalUrls.length > 0 ? JSON.stringify(finalUrls) : undefined,
    count: finalUrls.length > 0 ? finalUrls.length : 0,
  };

  const formData = new FormData();
  formData.append('note', JSON.stringify(noteData));

  await addNote(formData as any);
}
