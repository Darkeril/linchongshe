<script lang="ts" setup>
import { ref } from 'vue'
import { NAvatar, NButton, NInput, useMessage } from 'naive-ui'
import { useAuthStoreWithout } from '@/store/modules/auth'
import defaultAvatar from '@/assets/avatar.png'
import { router } from '@/router'
import { t } from '@/locales'

const ms = useMessage()
const authStore = useAuthStoreWithout()
const avatar = ref(authStore.session?.avatar ? authStore.session?.avatar : defaultAvatar)
const name = ref(authStore.session?.name ?? '')
const tel = ref(authStore.session?.tel ?? '')
const password = ref(authStore.session?.password ?? '')

/** 退出登录 */
function handleLogout() {
  authStore.removeToken()
  ms.success(t('退出成功'))
  router.push('/login')
}
</script>

<template>
  <div class="p-3 space-y-5 min-h-[200px]">
    <div class="space-y-6">
      <div class="flex items-center space-x-4">
        <span class="flex-shrink-0 w-[100px]">{{ $t('setting.avatarLink') }}</span>
        <div class="flex-1">
          <NAvatar round size="large" :src="avatar" />
        </div>
      </div>
      <div class="flex items-center space-x-4">
        <span class="flex-shrink-0 w-[100px]">{{ $t('setting.name') }}</span>
        <div class="w-[200px]">
          <NInput v-model:value="name" placeholder="" />
        </div>
        <!-- <NButton size="tiny" text type="primary" @click="updateUserInfo({ name })">
          {{ $t('common.save') }}
        </NButton> -->
      </div>
      <div class="flex items-center space-x-4">
        <span class="flex-shrink-0 w-[100px]">{{ $t('setting.tel') }}</span>
        <div class="w-[200px]">
          <NInput v-model:value="tel" placeholder="" />
        </div>
        <!-- <NButton size="tiny" text type="primary" @click="updateUserInfo({ name })">
          {{ $t('common.save') }}
        </NButton> -->
      </div>
      <div class="flex items-center space-x-4">
        <span class="flex-shrink-0 w-[100px]">{{ $t('setting.password') }}</span>
        <div class="w-[200px]">
          <NInput v-model:value="password" type="password" placeholder="" />
        </div>
        <!-- <NButton size="tiny" text type="primary" @click="updateUserInfo({ name })">
          {{ $t('common.save') }}
        </NButton> -->
      </div>
      <div class="flex items-center space-x-4">
        <span class="flex-shrink-0 w-[100px]">{{ $t('setting.monthlyUsage') }}</span>
        <div class="w-[200px]">
          <NInput v-model:value="name" placeholder="" />
        </div>
      </div>
      <div class="flex items-center justify-center space-x-4">
        <NButton type="primary" class="logout-form" @click="handleLogout">
          {{ $t('setting.resetUserInfo') }}
        </NButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.logout-form {
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
}
</style>
