import axios from 'axios'
import request from "@/utils/request";

/**
 * 当前时间
 * @prama time 13位时间戳
 */
export const formatDate = (t: number): string => {
  t = t || Date.now();
  const time = new Date(t);
  let str = time.getFullYear() + "-";
  str += (time.getMonth() < 9 ? "0" : "") + (time.getMonth() + 1) + "-"; // 修正月份
  str += (time.getDate() < 10 ? "0" : "") + time.getDate() + " ";
  str += (time.getHours() < 10 ? "0" : "") + time.getHours() + ":"; // 添加小时的前导零
  str += (time.getMinutes() < 10 ? "0" : "") + time.getMinutes(); // 添加分钟的前导零
  return str;
};

/**
 * 距当前时间点的时长
 * @prama time 13位时间戳
 * @return str x秒 / x分钟 / x小时
 */
export const formateTime = (time: number): string => {
  const now = Date.now();
  const diffValue = now - time;
  
  // 使用常量对象整理时间单位
  const TIME_UNITS = {
    SECOND: 1000,
    MINUTE: 1000 * 60,
    HOUR: 1000 * 60 * 60,
    DAY: 1000 * 60 * 60 * 24,
    MONTH: 1000 * 60 * 60 * 24 * 30,
    YEAR: 1000 * 60 * 60 * 24 * 365
  } as const;

  // 使用数组配置来简化判断逻辑
  const timeConfigs = [
    { unit: TIME_UNITS.YEAR, threshold: 2, format: () => formatDate(time) },  // 超过2年显示原始时间
    { unit: TIME_UNITS.YEAR, threshold: 1, format: (val: number) => `${Math.floor(val)}年前` },
    { unit: TIME_UNITS.MONTH, threshold: 1, format: (val: number) => `${Math.floor(val)}个月前` },
    { unit: TIME_UNITS.DAY, threshold: 1, format: (val: number) => `${Math.floor(val)}天前` },
    { unit: TIME_UNITS.HOUR, threshold: 1, format: (val: number) => `${Math.floor(val)}小时前` },
    { unit: TIME_UNITS.MINUTE, threshold: 1, format: (val: number) => `${Math.floor(val)}分钟前` },
    { unit: TIME_UNITS.SECOND, threshold: 1, format: (val: number) => `${Math.floor(val)}秒前` }
  ];

  // 遍历配置数组找到匹配的时间格式
  for (const { unit, threshold, format } of timeConfigs) {
    const value = diffValue / unit;
    if (value >= threshold) {
      return format(value);
    }
  }
  return "刚刚";
};

/**
 * 随机生成字符串
 * @param len 指定生成字符串长度
 */
export const getRandomString = (len: number) => {
  const _charStr = "abacdefghjklmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ0123456789",
    min = 0,
    max = _charStr.length - 1;
  let _str = ""; //定义随机字符串 变量
  //判断是否指定长度，否则默认长度为15
  len = len || 15;
  //循环生成字符串
  for (let i = 0, index; i < len; i++) {
    index = (function (randomIndexFunc, i) {
      return randomIndexFunc(min, max, i, randomIndexFunc);
    })(function (min: any, max: any, i: any, _self: any) {
      let indexTemp = Math.floor(Math.random() * (max - min + 1) + min);
      const numStart = _charStr.length - 10;
      if (i == 0 && indexTemp >= numStart) {
        indexTemp = _self(min, max, i, _self);
      }
      return indexTemp;
    }, i);
    _str += _charStr[index];
  }
  return _str;
}



export const getFileFromUrl = async (url: string, fileName: string) => {
  try {
    // 第一步：使用axios获取网络图片数据
    const response = await axios.get(url, { responseType: 'arraybuffer' })
    // 第二步：将图片数据转换为Blob对象
    const blob = new Blob([response.data], {
      type: response.headers['content-type']
    })
    // 第三步：创建一个新的File对象
    const file = new File([blob], fileName, {
      type: response.headers['content-type']
    })
    return file
  } catch (error) {
    console.error('将图片转换为File对象时发生错误:', error)
    throw error
  }
}

/**
 * 得到html标签中的内容
 * @param content 
 */
export const getHtmlContent = (html: string) => {
  const pattern = /<[a-z]+[1-6]?\b[^>]*>(.*?)<\/[a-z]+[1-6]?>/g;
  const res = [];
  let match;
  while ((match = pattern.exec(html)) !== null) {
    const content = match[1].trim();
    res.push(content);
  }
  return res;
}


/**
 * 滚动平滑导航栏
 */
export const refreshTab = (f: any) => {
  let scrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop;
  const clientHeight =
    window.innerHeight || Math.min(document.documentElement.clientHeight, document.body.clientHeight);

  if (scrollTop <= clientHeight * 2) {
    const timeTop = setInterval(() => {
      document.documentElement.scrollTop = document.body.scrollTop = scrollTop -= 100;
      if (scrollTop <= 0) {
        clearInterval(timeTop);
        f();
      }
    }, 10); //定时调用函数使其更顺滑
  } else {
    document.documentElement.scrollTop = 0;
    f();
  }
};

/**
 * 将图片转成base64格式
 *
 * @param imageFile 图片文件
 * @param callback 转成成功函数回调（这里是接收转换成功结果的函数）
 * @param errorCallback 转成失败函数回调（这里是接收转换失败结果的函数）
 */
export const convertImgToBase64 = (imageFile: any, callback: any, errorCallback: any) => {
  try {
    const reader = new FileReader();
    reader.readAsDataURL(imageFile);
    reader.onload = function (e) {
      if (callback) {
        const base64Str = e.target!.result;
        callback(base64Str);
      }
    };
  } catch (error) {
    if (errorCallback) {
      errorCallback(error);
    }
  }
}

// 上传文件到file模块（推荐使用）
export const uploadFileToFileService = (file: File): Promise<{ code: number; data?: { url: string }; msg?: string }> => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/file/upload`,
    method: 'post',
    data: formData,
    // 不手动设置Content-Type，让axios自动设置（包含boundary）
    timeout: 300000 // 5分钟超时，支持大文件上传
  }) as Promise<{ code: number; data?: { url: string }; msg?: string }>
}

// 批量上传文件到file模块
export const uploadBatchFilesToFileService = (files: File[]): Promise<{ code: number; data?: Array<{ url: string; name?: string }>; msg?: string }> => {
  if (files.length === 0) {
    return Promise.resolve({ code: 200, data: [] })
  }
  
  // 如果只有一张图片，使用单文件上传
  if (files.length === 1) {
    return uploadFileToFileService(files[0]).then(res => {
      if (res.code === 200 && res.data?.url) {
        return { code: 200, data: [{ url: res.data.url }] }
      }
      return { code: res.code, msg: res.msg }
    })
  }
  
  // 多文件时使用批量上传接口
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })
  
  return request({
    url: `/file/batch/upload`,
    method: 'post',
    data: formData,
    // 不手动设置Content-Type，让axios自动设置（包含boundary）
    timeout: 300000 // 5分钟超时，支持大文件上传
  }) as Promise<{ code: number; data?: Array<{ url: string; name?: string }>; msg?: string }>
}

// 上传图片接口（已废弃，保留用于兼容）
export const uploadFileMethod = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/web/oss/upload`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 处理图片URL（兼容旧数据）
 * 现在所有新数据都是OSS路径，此函数主要用于兼容可能存在的旧数据
 * 
 * @param imageUrl 图片URL
 * @returns 处理后的URL（OSS URL直接返回，小红书CDN URL转换为代理路径）
 */
export const getNginxProxyImageUrl = (imageUrl: string | undefined): string => {
  if (!imageUrl || !imageUrl.trim()) {
    return '';
  }
  
  // 如果是相对路径（以 / 开头但不是 http://），直接返回
  if (imageUrl.startsWith('/') && !imageUrl.startsWith('//')) {
    return imageUrl;
  }
  
  // 如果已经是 base64 数据，直接返回
  if (imageUrl.startsWith('data:')) {
    return imageUrl;
  }
  
  // 如果已经是OSS URL（包含OSS域名），直接返回（新数据都是OSS路径）
  if (imageUrl.includes('oss') || imageUrl.includes('aliyuncs.com') || imageUrl.includes('qcloud.com') || imageUrl.includes('mayongjian.cn')) {
    return imageUrl;
  }
  
  // 如果是本地开发服务器的URL（localhost、127.0.0.1），直接返回
  if (imageUrl.includes('localhost') || imageUrl.includes('127.0.0.1')) {
    return imageUrl;
  }
  
  // 兼容旧数据：如果是小红书图片URL，转换为Nginx代理路径
  if (imageUrl.includes('sns-webpic-qc.xhscdn.com') || 
      imageUrl.includes('xhscdn.com') ||
      imageUrl.includes('xiaohongshu.com')) {
    try {
      const url = new URL(imageUrl);
      // 提取路径部分，转换为 /xhspic/ 开头的路径
      return `/xhspic${url.pathname}${url.search}`;
    } catch (e) {
      // 如果URL解析失败，尝试简单替换
      const match = imageUrl.match(/https?:\/\/[^/]+(\/.*)/);
      if (match) {
        return `/xhspic${match[1]}`;
      }
      // 如果都失败了，返回原始URL
      return imageUrl;
    }
  }
  
  // 其他情况，直接返回原始URL
  return imageUrl;
};

/**
 * 处理视频URL（兼容旧数据）
 * 现在所有新数据都是OSS路径，此函数主要用于兼容可能存在的旧数据
 * 
 * @param videoUrl 视频URL
 * @returns 处理后的URL（OSS URL直接返回，小红书CDN URL转换为代理路径）
 */
export const getNginxProxyVideoUrl = (videoUrl: string | undefined): string => {
  if (!videoUrl || !videoUrl.trim()) {
    return '';
  }
  
  // 如果是相对路径（以 / 开头但不是 http://），直接返回
  if (videoUrl.startsWith('/') && !videoUrl.startsWith('//')) {
    return videoUrl;
  }
  
  // 如果已经是 blob URL，直接返回
  if (videoUrl.startsWith('blob:')) {
    return videoUrl;
  }
  
  // 如果已经是OSS URL（包含OSS域名），直接返回（新数据都是OSS路径）
  if (videoUrl.includes('oss') || videoUrl.includes('aliyuncs.com') || videoUrl.includes('qcloud.com') || videoUrl.includes('mayongjian.cn')) {
    return videoUrl;
  }
  
  // 如果是本地开发服务器的URL（localhost、127.0.0.1），直接返回
  if (videoUrl.includes('localhost') || videoUrl.includes('127.0.0.1')) {
    return videoUrl;
  }
  
  // 兼容旧数据：如果是小红书视频URL，转换为Nginx代理路径
  if (videoUrl.includes('sns-video-bd.xhscdn.com') || 
      videoUrl.includes('xhscdn.com') ||
      videoUrl.includes('xiaohongshu.com')) {
    try {
      const url = new URL(videoUrl);
      // 提取路径部分，转换为 /xhsvideo/ 开头的路径
      return `/xhsvideo${url.pathname}${url.search}`;
    } catch (e) {
      // 如果URL解析失败，尝试简单替换
      const match = videoUrl.match(/https?:\/\/[^/]+(\/.*)/);
      if (match) {
        return `/xhsvideo${match[1]}`;
      }
      // 如果都失败了，返回原始URL
      return videoUrl;
    }
  }
  
  // 其他情况，直接返回原始URL
  return videoUrl;
};

/** 富文本转纯文本（系统通知列表预览、推送摘要等） */
export function htmlToPlainText(html: string): string {
  if (!html || typeof html !== "string") return "";
  if (typeof document === "undefined") {
    return html.replace(/<[^>]+>/g, " ").replace(/\s+/g, " ").trim();
  }
  const d = document.createElement("div");
  d.innerHTML = html;
  return (d.textContent || d.innerText || "").replace(/\s+/g, " ").trim();
}
