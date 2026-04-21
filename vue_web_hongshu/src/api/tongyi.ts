import request from "@/utils/request";

/**
 * 通义千问内容生成API
 */

/**
 * 根据图片和视频URL生成文案
 */
export function generateContentFromUrls(data: {
  imageUrls?: string[];
  videoUrl?: string;
  category?: string;
  productName?: string;
  productDescription?: string;
  style?: string;
  contentType?: string; // note-笔记，idle-闲置商品
  titleOnly?: boolean;
}) {
  return request({
    url: "/web/tongyi/content/generate",
    method: "post",
    data,
    timeout: 60000, // 增加超时时间到60秒
  });
}

/**
 * 根据上传的图片文件生成文案
 */
export function generateContentFromFiles(
  files: File[],
  options?: {
    category?: string;
    productName?: string;
    productDescription?: string;
    style?: string;
  }
) {
  const formData = new FormData();
  files.forEach((file) => {
    formData.append("imageFiles", file);
  });

  if (options?.category) {
    formData.append("category", options.category);
  }
  if (options?.productName) {
    formData.append("productName", options.productName);
  }
  if (options?.productDescription) {
    formData.append("productDescription", options.productDescription);
  }
  if (options?.style) {
    formData.append("style", options.style);
  }

  return request({
    url: "/web/tongyi/content/generate-from-images",
    method: "post",
    data: formData,
    timeout: 60000, // 增加超时时间到60秒
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

/**
 * 根据上传的视频文件生成文案
 */
export function generateContentFromVideo(
  file: File,
  options?: {
    category?: string;
    productName?: string;
    productDescription?: string;
    style?: string;
  }
) {
  const formData = new FormData();
  formData.append("videoFile", file);

  if (options?.category) {
    formData.append("category", options.category);
  }
  if (options?.productName) {
    formData.append("productName", options.productName);
  }
  if (options?.productDescription) {
    formData.append("productDescription", options.productDescription);
  }
  if (options?.style) {
    formData.append("style", options.style);
  }

  return request({
    url: "/web/tongyi/content/generate-from-video",
    method: "post",
    data: formData,
    timeout: 60000, // 增加超时时间到60秒
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

/**
 * 只生成标题
 */
export function generateTitlesOnly(data: {
  imageUrls?: string[];
  videoUrl?: string;
  category?: string;
  productName?: string;
  productDescription?: string;
  style?: string;
}) {
  return request({
    url: "/web/tongyi/content/generate-titles",
    method: "post",
    data: { ...data, titleOnly: true },
    timeout: 60000,
  });
}


