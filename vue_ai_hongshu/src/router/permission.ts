import type { Router } from 'vue-router'
import { useAuthStoreWithout } from '@/store/modules/auth'
import { useChatStore } from '@/store'

export function setupPageGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const chatStore = useChatStore()
    const authStore = useAuthStoreWithout()

    if (to.path === '/login') {
      // 带自动登录参数时不清 token，让 login.vue 自行处理
      if (!to.query.phone)
        authStore.removeToken()
      next()
      return
    }
    if (to.path === '/agreement') {
      next()
      return
    }

    // 携带自动登录参数时跳过 getSession，直接进 /login 走自动登录
    if (to.query.phone) {
      next({ path: '/login', query: to.query })
      return
    }

    try {
      const res = await authStore.getSession()
      chatStore.setActive('')
      if (!res || res.code !== 200) {
        next({ path: '/login', query: to.query })
        return
      }
      next()
    }
    catch {
      if (to.path !== '/500')
        next({ name: '500' })
      else
        next()
    }
  })
}
