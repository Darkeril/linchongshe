/**
 * 封装操作localstorage本地存储的方法
 * 使用前缀 'web_' 避免与 uniapp 和 admin 端冲突
 */
const STORAGE_PREFIX = 'web_';

export const storage = {
  //存储
  set(key: string, value: any) {
    const prefixedKey = STORAGE_PREFIX + key;
    localStorage.setItem(prefixedKey, JSON.stringify(value));
  },
  //取出数据
  get<T>(key: string) {
    const prefixedKey = STORAGE_PREFIX + key;
    const value = localStorage.getItem(prefixedKey);
    if (value && value != "undefined" && value != "null") {
      return <T>JSON.parse(value);
    }
    // 兼容旧数据（没有前缀的key）
    const oldValue = localStorage.getItem(key);
    if (oldValue && oldValue != "undefined" && oldValue != "null") {
      const parsed = <T>JSON.parse(oldValue);
      // 迁移到新key
      this.set(key, parsed);
      localStorage.removeItem(key);
      return parsed;
    }
  },
  // 删除数据
  remove(key: string) {
    const prefixedKey = STORAGE_PREFIX + key;
    localStorage.removeItem(prefixedKey);
    // 同时删除旧key（兼容处理）
    localStorage.removeItem(key);
  },
};

/**
 * 封装操作sessionStorage本地存储的方法
 */
export const sessionStorage = {
  //存储
  set(key: string, value: any) {
    window.sessionStorage.setItem(key, JSON.stringify(value));
  },
  //取出数据
  get(key: string) {
    const value = window.sessionStorage.getItem(key);
    if (value && value != "undefined" && value != "null") {
      return JSON.parse(value);
    }
    return null;
  },
  // 删除数据
  remove(key: string) {
    window.sessionStorage.removeItem(key);
  },
};
