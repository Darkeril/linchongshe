import {defineStore} from 'pinia';
import {getUserInfo, login as userLogin, LoginData, LoginRes, logout as userLogout,} from '@/api/user';
import {clearToken, setToken, setAccessToken, setExpiresIn} from '@/utils/auth';
import {removeRouteListener} from '@/utils/route-listener';
import {clearRouteVisitHistory} from '@/utils/route-visit-history';
import {UserAction} from './types';
import useAppStore from '../app';

const env = import.meta.env.VITE_APP_BASE_API;

const useUserStore = defineStore('user', {
    state: (): UserAction => ({
        permissions: [],
        roles: [],
        user: {
            avatar: '',
            dept: {
                deptName: '',
            },
        },
    }),

    getters: {
        userInfo(state: UserAction): UserAction {
            return {...state};
        },
    },

    actions: {
        // Set user's information
        // setInfo(partial: Partial<UserAction>) {
        //     if (partial.user?.avatar) {
        //         partial.user.avatar = env + partial.user.avatar;
        //     } else if (partial.user?.avatar === '') {
        //         partial.user.avatar = '//lf1-xgcdn-tos.pstatp.com/obj/vcloud/vadmin/start.8e0e4855ee346a46ccff8ff3e24db27b.png';
        //     }
        //     this.$patch(partial);
        // },
        setInfo(partial: Partial<UserAction>) {
            const user = partial.user as any;
            if (user) {
                const avatar: string = user.avatar || '';
                if (!avatar) {
                    user.avatar = '//lf1-xgcdn-tos.pstatp.com/obj/vcloud/vadmin/start.8e0e4855ee346a46ccff8ff3e24db27b.png';
                }
            }
            this.$patch(partial);
        },

        // Reset user's information
        resetInfo() {
            this.$reset();
        },

        // Get user's information
        async info() {
            const res = await getUserInfo();
            // @ts-ignore
            this.setInfo(res);
        },

    // Login
    async login(loginForm: LoginData) {
      try {
          const res = await userLogin(loginForm) as LoginRes;
          if (res.code === 200 && res.data && res.data.access_token) {
              const token = res.data.access_token;
              setToken(token);
              // 后端可能同时返回 access_token 与 accessToken（用于 Accesstoken 头）
              const passthroughToken = (res.data as any).accessToken || token;
              setAccessToken(passthroughToken);
              if (res.data.expires_in !== undefined && res.data.expires_in !== null) {
                  setExpiresIn(String(res.data.expires_in));
              }
          } else {
              throw new Error(res.msg || 'Login failed, token not found.');
          }
      } catch (err: any) {
          clearToken();
          console.error('Login failed:', err.message || err);
          throw err;
      }
    },
    logoutCallBack() {
      const appStore = useAppStore();
      this.resetInfo();
      clearToken();
      removeRouteListener();
      clearRouteVisitHistory();
      appStore.clearServerMenu();
    },
    // Logout
    async logout() {
      try {
        await userLogout();
      } finally {
        this.logoutCallBack();
      }
    },
  },
});

export default useUserStore;
