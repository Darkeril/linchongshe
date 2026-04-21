import { defineStore } from "pinia";
import { ref } from "vue";
import { storage } from "@/utils/storage";
import { refreshToken } from "@/api/user";
import { invalidateRecommendCaches } from "@/utils/apiCache";
import { store } from "@/store";
import type { User } from "@/type/user";

// 使用setup模式定义
export const userStore = defineStore("userStore", () => {
  const token = ref("");

  const getToken = () => {
    return storage.get("accessToken");
  };

  const getUserInfo = (): User => {
    return storage.get("userInfo") as User;
  };

  const setUserInfo = (data: User) => {
    storage.set("userInfo", data);
  };

  const isLogin = () => {
    const user = storage.get("userInfo") as User;
    return user != null && user != undefined;
  };

  const getNewToken = (token: string) => {
    return new Promise<any>((resolve, reject) => {
      refreshToken(token)
        .then((res) => {
          resolve(res);
        })
        .catch((error) => {
          reject(error);
        });
    });
  };

  const loginOut = () => {
    invalidateRecommendCaches();
    // 只清除web端自己的存储数据，避免影响其他项目（admin、uniapp）
    // storage 已经使用 'web_' 前缀，不会与 uniapp 的 'uniapp_token' 和 admin 的 'token' 冲突
    storage.remove("accessToken");
    storage.remove("refreshToken");
    storage.remove("userInfo");
    // 清除web端可能使用的其他localStorage数据（不带前缀的，如theme等）
    const otherWebKeys = ['theme', 'ai-btn-position'];
    otherWebKeys.forEach(key => {
      try {
        window.localStorage.removeItem(key);
      } catch (e) {
        // 忽略错误
      }
    });
    // 清除web端可能使用的sessionStorage数据
    // 注意：不调用 window.sessionStorage.clear()，避免影响其他项目
  };

  return { token, getToken, getNewToken, getUserInfo, setUserInfo, loginOut, isLogin };
});

export function useUserStore() {
  return userStore(store);
}
