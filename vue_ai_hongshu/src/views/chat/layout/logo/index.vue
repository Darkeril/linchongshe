<script setup lang='ts'>
import { computed, defineAsyncComponent, onMounted, ref } from 'vue'
import { NAvatar, NDropdown, useMessage } from 'naive-ui'
import logo from '@/assets/logo.jpg'
import { SvgIcon } from '@/components/common'
import { useAppStore, useChatStore } from '@/store'
import { fetchModel } from '@/api'
import type { Theme } from '@/store/modules/app/helper'
import { useAuthStoreWithout } from '@/store/modules/auth'
import { resolveAvatarUrl } from '@/utils/functions'

const ms = useMessage()
const appStore = useAppStore()
const chatStore = useChatStore()
const Setting = defineAsyncComponent(() => import('@/components/common/Setting/index.vue'))
const theme = computed(() => appStore.theme)
const model = computed(() => chatStore.model)
const isPromptOpen = computed(() => appStore.promptPanelOpen)
const show = ref(false)
const models = ref<Chat.Model[]>()
const options = ref<any>()

const authStore = useAuthStoreWithout()
const avatar = computed(() => authStore.session && authStore.session.avatar)
const avatarSrc = computed(() => resolveAvatarUrl((avatar.value as unknown as string) || ''))

// 初始化加载
onMounted(() => {
  // 获取用户模型
  fetchModel<Chat.Model[]>().then((res) => {
    models.value = res.data
    options.value = models.value.map((model) => {
      const option = {
        label: model.name,
        key: model.model,
        props: {},
      }
      if (option.key === chatStore.model) {
        option.props = {
          style: { backgroundColor: theme.value === 'light' ? '#F3F3F5' : '#767C82' },
        }
      }
      return option
    })
    if (!chatStore.model)
      chatStore.setModel(options.value[0].key)
  })
})

function changeTheme() {
  const value: Theme = theme.value === 'light' ? 'dark' : 'light'
  appStore.setTheme(value)
  updateOptions()
}

async function handleSelect(key: string) {
  if (key === chatStore.model)
    return
  await chatStore.setModel(key)
  updateOptions()
  ms.success('模型切换成功')
}

function updateOptions() {
  options.value = options.value.map((v: { label: string; key: string; props: object }) => {
    const option = {
      label: v.label,
      key: v.key,
      props: {},
    }
    if (option.key === chatStore.model) {
      option.props = {
        style: { backgroundColor: theme.value === 'light' ? '#F3F3F5' : '#767C82' },
      }
    }
    return option
  })
}

function openPromptPanelFromLeft() {
  appStore.setPromptPanelOpen(!appStore.promptPanelOpen)
}

function openChatFromLeft() {
  if (appStore.promptPanelOpen)
    appStore.setPromptPanelOpen(false)
}
</script>

<template>
  <div>
    <!-- 侧边 -->
    <div class="flex h-full bg-white dark:bg-[#18181c] absolute left-0 z-50">
      <div class="flex flex-col items-center justify-between w-20 border-r dark:border-[#2d2d30]">
        <!-- logo -->
        <div class="flex flex-col items-center">
          <img
            :src="appStore.baseInfo && appStore.baseInfo.siteLogo ? appStore.baseInfo.siteLogo : logo"
            class="mt-3 w-[70px] h-[70px] cursor-pointer"
          >
          <!-- 对话 -->
          <div
            class="flex flex-col items-center mt-5 w-14 p-2 rounded-lg cursor-pointer hover:bg-slate-100 hover:dark:bg-[#24272e]"
            :class="isPromptOpen ? 'dark:bg-[#18181c]' : 'bg-slate-100 dark:bg-[#24272e]'"
            @click="openChatFromLeft"
          >
            <SvgIcon icon="ri:message-line" class="mb-2 text-xl" />
            <div class="text-xs font-semibold">
              {{ $t('chat.duiHua') }}
            </div>
          </div>
          <!-- 作图 -->
          <!-- <div
            class="flex flex-col items-center mt-5 w-14 dark:bg-[#18181c] p-2 rounded-lg cursor-pointer hover:bg-slate-100 hover:dark:bg-[#24272e]"
            @click="changePicture()"
          >
            <SvgIcon icon="icon-park-outline:picture" class="mb-2 text-xl" />
            <div class="text-xs font-semibold">
              {{ $t('chat.picture') }}
            </div>
          </div> -->
          <!-- 万花筒入口（最左侧） -->
          <div
            class="flex flex-col items-center mt-5 w-14 p-2 rounded-lg cursor-pointer hover:bg-slate-100 hover:dark:bg-[#24272e]"
            :class="isPromptOpen ? 'bg-slate-100 dark:bg-[#24272e]' : 'dark:bg-[#18181c]'"
            @click="openPromptPanelFromLeft"
          >
            <SvgIcon icon="icons8:idea" class="mb-2 text-xl" />
            <div class="text-xs font-semibold">
              万花筒
            </div>
          </div>
        </div>
        <div class="flex flex-col items-center">
          <!-- 切换大模型 -->
          <div
            class="flex justify-center items-center rounded-3xl cursor-pointer mb-6 dark:bg-[#24272e] w-9 h-9 hover:bg-slate-200"
          >
            <NDropdown
              trigger="hover"
              placement="right"
              :options="options"
              style="border: none;"
              @select="handleSelect"
            >
              <SvgIcon :name="model" width="32" height="32" />
            </NDropdown>
          </div>
          <!-- 会员 -->
          <!-- <div class="flex flex-col items-center mb-6 cursor-pointer">
            <div class="flex justify-center items-center rounded-3xl bg-[#ecd2ac] w-9 h-9 mb-1">
              <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" aria-hidden="true" role="img" class="text-2xl text-[#793706] iconify iconify--mingcute" width="1em" height="1em" viewBox="0 0 24 24">
                <g fill="none" fill-rule="evenodd">
                  <path d="m12.593 23.258l-.011.002l-.071.035l-.02.004l-.014-.004l-.071-.035c-.01-.004-.019-.001-.024.005l-.004.01l-.017.428l.005.02l.01.013l.104.074l.015.004l.012-.004l.104-.074l.012-.016l.004-.017l-.017-.427c-.002-.01-.009-.017-.017-.018m.265-.113l-.013.002l-.185.093l-.01.01l-.003.011l.018.43l.005.012l.008.007l.201.093c.012.004.023 0 .029-.008l.004-.014l-.034-.614c-.003-.012-.01-.02-.02-.022m-.715.002a.023.023 0 0 0-.027.006l-.006.014l-.034.614c0 .012.007.02.017.024l.015-.002l.201-.093l.01-.008l.004-.011l.017-.43l-.003-.012l-.01-.01z" />
                  <path fill="currentColor" d="M17.42 3a2 2 0 0 1 1.649.868l.087.14L22.49 9.84a2 2 0 0 1-.208 2.283l-.114.123l-9.283 9.283a1.25 1.25 0 0 1-1.666.091l-.102-.09l-9.283-9.284a2 2 0 0 1-.4-2.257l.078-.15l3.333-5.832a2 2 0 0 1 1.572-1.001L6.58 3zM7.293 9.293a1 1 0 0 0 0 1.414l3.823 3.823a1.25 1.25 0 0 0 1.768 0l3.823-3.823a1 1 0 1 0-1.414-1.414L12 12.586L8.707 9.293a1 1 0 0 0-1.414 0" />
                </g>
              </svg>
            </div>
            <div class="text-[11px] text-[#793706] dark:text-[#fff]">
              会员
            </div>
          </div> -->
          <!-- 微信 -->
          <!-- <div
            class="flex justify-center items-center rounded-3xl cursor-pointer mb-6 bg-slate-100 dark:bg-[#24272e] w-9 h-9 hover:bg-slate-200"
          >
            <NPopover
              trigger="hover"
              raw
              :show-arrow="false"
              placement="right"
              style="margin-left: 17px;"
            >
              <template #trigger>
                <div class="flex items-center">
                  <NAvatar round :src="vx" />
                </div>
              </template>
              <div
                class="bg-[#fff] dark:bg-[#24272e]"
                style="width: 220px; height: 220px; padding: 20px 20px; display: flex; justify-content: center; align-items: center; flex-direction: column;"
              >
                <div style="font-weight: 500; margin-bottom: 10px;">
                  {{ $t('common.wechat') }}
                </div>
                <img :src="wechat" style="width: 120px; height: 150px;">
              </div>
            </NPopover>
          </div> -->
          <!-- 改变主题 -->
          <div
            class="flex justify-center items-center rounded-3xl cursor-pointer mb-6 bg-slate-100 dark:bg-[#24272e] w-9 h-9 hover:bg-slate-200"
            @click="changeTheme()"
          >
            <SvgIcon
              :icon="theme === 'light' ? 'ri:sun-foggy-line' : 'ri:moon-foggy-line'"
              class="text-2xl"
            />
          </div>
          <!-- 头像 -->
          <div
            class="flex justify-center items-center rounded-3xl cursor-pointer mb-6 bg-slate-100 dark:bg-[#24272e] w-9 h-9 hover:bg-slate-200"
            @click="show = true"
          >
            <!-- <SvgIcon icon="lucide:user" class="text-2xl" /> -->
            <NAvatar round :src="avatarSrc" />
          </div>
          <!-- APP 端 -->
          <!-- <div
            class="flex justify-center items-center w-14 mb-4 bg-[#18181c] dark:bg-[#24272e] rounded-md h-7 cursor-pointer hover:bg-blue-600 hover:dark:bg-[#24272e]"
          >
            <NPopover
              trigger="hover"
              raw
              :show-arrow="false"
              placement="right"
              style="margin-left: 17px;"
            >
              <template #trigger>
                <div class="flex items-center">
                  <SvgIcon icon="fluent:phone-16-regular" class="text-lg text-white" />
                  <div class="flex text-sm text-white">
                    {{ $t('common.app') }}
                  </div>
                </div>
              </template>
              <div
                class="bg-[#fff] dark:bg-[#24272e]"
                style="width: 220px; height: 220px; padding: 20px 20px; display: flex; justify-content: center; align-items: center; flex-direction: column;"
              >
                <div style="font-weight: 500; margin-bottom: 10px;">
                  {{ $t('common.appTip') }}
                </div>
                <img :src="wxapp" style="width: 150px; height: 150px;">
              </div>
            </NPopover>
          </div> -->
        </div>
      </div>
    </div>
    <Setting v-if="show" v-model:visible="show" />
  </div>
</template>

<style lang="less">
.n-dropdown-menu {
  max-height: 212px;
  overflow-y: auto;
}
</style>