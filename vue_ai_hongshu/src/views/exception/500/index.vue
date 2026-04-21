<script lang="ts" setup>
import { NButton, NResult } from 'naive-ui'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 刷新页面返回首页
function goHome() {
  // 如果路由中有自动登录参数，跳转到登录页并保留参数
  if (route.query.phone && route.query.accessToken) {
    console.log('🔴 500页面：检测到自动登录参数，跳转到登录页')
    router.push({
      path: '/login',
      query: route.query
    })
  } else {
    router.push('/')
  }
}
</script>

<template>
  <div class="flex h-full dark:bg-neutral-800">
    <div class="px-4 m-auto space-y-4 text-center max-[400px]">
      <NResult status="500" title="服务器连接中，请稍后再试～" description="服务器出错可能说明该雇更多程序员了">
        <template #footer>
          <NButton type="primary" style="cursor: pointer;" @click="goHome">
            刷新
          </NButton>
        </template>
      </NResult>
    </div>
  </div>
</template>
