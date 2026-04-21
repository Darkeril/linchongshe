<script setup lang='ts'>
import { computed, onMounted, ref } from 'vue'
import type {
  FormInst,
  FormItemRule,
} from 'naive-ui'
import {
  NButton,
  NCheckbox,
  NForm,
  NFormItem,
  NInput,
  useMessage,
} from 'naive-ui'
import { fetchChatConfig, fetchVerify } from '@/api'
import { useAppStore, useAuthStore } from '@/store'
import logo from '@/assets/logo.jpg'
import indexBack from '@/assets/media/index-back.mp4'
import { router } from '@/router'
import { useRoute } from 'vue-router'

const authStore = useAuthStore()
const appStore = useAppStore()
const ms = useMessage()
const route = useRoute()

const visible = ref(false)
const loading = ref(false)
const autoLogging = ref(false)
const formRef = ref<FormInst | null>(null)
const baseInfo = ref<any>(null)
const hasTriedAutoLogin = ref(false)

interface Token {
  token: string
  refreshToken: string
  expiresIn: number
}

interface Login {
  tel: string
  password: string
  checked: false
  loginType: number
}
const loginValue = ref<Login>({
  tel: '',
  password: '',
  checked: false,
  loginType: 3,
})

const disabled = computed(() => {
  return !(
    loginValue.value?.tel.trim()
    && loginValue.value?.password.trim()
    && !loading.value
    && loginValue.value?.checked
  )
})

const rules = {
  tel: {
    required: true,
    pattern: /^[1][3456789]\d{9}$/,
    message: '请输入正确的手机号码',
    trigger: ['input', 'blur'],
  },
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: ['input', 'blur'],
    },
    {
      trigger: ['blur', 'change'],
      pattern: /^(?=^.*?[a-z])(?=^.*?\d).{6,32}$/,
      message: '请输入6-32位包含字母和数字的密码',
    },
  ],
  checked: {
    required: true,
    message: '请勾选“用户协议与隐私政策”',
    trigger: ['input', 'blur', 'change'],
    validator(rule: FormItemRule, value: boolean) {
      if (!value)
        return new Error('请勾选“用户协议与隐私政策”')

      return true
    },
  },
}

onMounted(() => {
  // 先检查是否需要自动登录，尽早隐藏落地页
  checkAutoLoginParams()

  fetchChatConfig<any>().then((res) => {
    if (res.code === 200) {
      baseInfo.value = res.data.baseInfo
      appStore.setBaseInfo(res.data.baseInfo)
    }
  })
})

/** 检查URL参数中的自动登录信息 */
function checkAutoLoginParams() {
  try {
    const phone = route.query.phone as string | undefined
    const accessToken = route.query.accessToken as string | undefined

    if (phone && !hasTriedAutoLogin.value) {
      loginValue.value.tel = phone
      hasTriedAutoLogin.value = true
      autoLogging.value = true
      handleAutoLogin(phone, accessToken || undefined)
    }
  }
  catch (error) {
    console.error('解析自动登录参数失败:', error)
  }
}

/** 立即体验/快速开始 */
function handlePress() {
  const phone = route.query.phone as string | undefined
  const accessToken = route.query.accessToken as string | undefined

  if (phone) {
    autoLogging.value = true
    handleAutoLogin(phone, accessToken || undefined)
  }
  else {
    visible.value = !visible.value
  }
}

/** 自动登录 - 使用手机号调用后端接口 */
async function handleAutoLogin(phone: string, accessToken?: string) {
  try {
    loading.value = true

    const autoLoginData: any = {
      tel: phone,
      password: '',
      loginType: 0,
    }
    if (accessToken)
      autoLoginData.accessToken = accessToken

    const res = await fetchVerify<Token>(autoLoginData)

    if (res.code === 200) {
      authStore.setToken(res.data.token)
      window.history.replaceState({}, '', window.location.pathname)
      router.push('/')
      return
    }

    ms.error(res.msg ?? '自动登录失败，请手动登录')
    autoLogging.value = false
    visible.value = true
  }
  catch {
    ms.error('自动登录失败，请手动登录')
    autoLogging.value = false
    visible.value = true
  }
  finally {
    loading.value = false
  }
}

/** 登录 */
async function handleVerify(e: MouseEvent) {
  e.preventDefault()
  formRef.value?.validate(async (errors) => {
    if (errors)
      return

    try {
      loading.value = true
      fetchVerify<Token>(loginValue.value).then((res) => {
        if (res.code === 200) {
          authStore.setToken(res.data.token)
          ms.success('登录成功')
          router.push('/')
        }
        else {
          ms.error(res.msg ?? '登录失败')
        }
      })
    }
    finally {
      loading.value = false
    }
  })
}

// 跳转页面
function jumpToXieyi(type: number) {
  const { href } = router.resolve({
    path: '/agreement',
    query: { type },
  })
  // 打开新窗口
  window.open(href)
}
</script>

<template>
  <!-- 自动登录中：纯净加载画面，不显示 GPTHUB 落地页 -->
  <div v-if="autoLogging" class="auto-login-overlay">
    <div class="auto-login-content">
      <img :src="baseInfo && baseInfo.siteLogo ? baseInfo.siteLogo : logo" class="auto-login-logo">
      <div class="auto-login-spinner" />
      <span class="auto-login-text">正在登录，请稍候...</span>
    </div>
  </div>

  <!-- 常规登录落地页 -->
  <div v-else class="bg-[#ebf3fe] w-full h-full overflow-auto">
    <div class="box-border relative w-full h-full p-2">
      <video autoplay loop muted class="absolute top-0 left-0 object-cover w-full h-full">
        <source :src="indexBack" type="video/mp4">
      </video>
      <div class="fixed w-full h-full z-1">
        <div class="flex items-center w-full logo-container">
          <img
            :src="baseInfo && baseInfo.siteLogo ? baseInfo.siteLogo : logo"
            class="w-[122px] h-[122px]"
          >
          <span
            class="text-[50px] font-medium"
          >{{ baseInfo && baseInfo.siteTitle ? baseInfo.siteTitle : $t('common.siteTitle') }}</span>
        </div>
        <div class="w-full h-full flex mt-[100px]">
          <div class="w-[800px] ml-[350px]">
            <div class="text-[60px] font-bold ml-[30px]">
              {{ $t('login.frontTitle') }}
            </div>
            <div class="text-[60px] font-bold ml-[30px]">
              {{ $t('login.title') }}
            </div>
            <div class="text-[20px] font-light ml-[30px]">
              {{ $t('login.subTitle') }}
            </div>
            <div
              class="w-[196px] h-[60px] mt-[80px] ml-[30px] bg-[#2454ff] text-white text-[22px] flex items-center justify-center rounded-lg cursor-pointer hover:bg-blue-700"
              @click="handlePress"
            >
              {{ $t('login.quickStart') }}
            </div>
          </div>
          <div v-if="visible">
            <div class="w-[500px] h-[450px] py-6 px-10 ml-[150px] bg-white rounded-xl slide-left">
              <NForm ref="formRef" :model="loginValue" :rules="rules">
                <div class="flex justify-center items-center mb-[18px] h-[65px]">
                  <img
                    :src="baseInfo && baseInfo.siteLogo ? baseInfo.siteLogo : logo"
                    class="w-[80px] h-[80px]"
                  >
                  <span
                    class="text-[40px] font-medium"
                  >{{ baseInfo && baseInfo.siteTitle ? baseInfo.siteTitle : $t('common.siteTitle') }}</span>
                </div>
                <div>
                  <NFormItem label="手机号" path="tel">
                    <NInput v-model:value="loginValue.tel" class="login-form" placeholder="请输入手机号" />
                  </NFormItem>
                </div>
                <div>
                  <NFormItem label="密码" path="password">
                    <NInput
                      v-model:value="loginValue.password"
                      class="login-form"
                      type="password"
                      placeholder="请输入密码"
                    />
                  </NFormItem>
                </div>
                <div class="mb-1">
                  <NFormItem :show-label="false" path="checked">
                    <NCheckbox v-model:checked="loginValue.checked" />
                    <span class="ml-2 text-slate-700">
                      {{ $t('login.checkTips') }}
                      <span
                        class="text-blue-600 cursor-pointer"
                        @click="jumpToXieyi(1)"
                      >{{ $t('common.userXieyi') }}</span> 与
                      <span
                        class="text-blue-600 cursor-pointer"
                        @click="jumpToXieyi(2)"
                      >{{ $t('common.privacyZhengce') }}</span>
                    </span>
                  </NFormItem>
                </div>
                <NButton
                  class="login-form"
                  block
                  type="primary"
                  :disabled="disabled"
                  :loading="loading"
                  @click="handleVerify"
                >
                  {{ $t('login.login') }}
                </NButton>
              </NForm>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auto-login-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ebf3fe 0%, #dbeafe 100%);
}

.auto-login-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.auto-login-logo {
  width: 72px;
  height: 72px;
  border-radius: 16px;
}

.auto-login-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(36, 84, 255, 0.2);
  border-top-color: #2454ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.auto-login-text {
  font-size: 15px;
  color: #4b5563;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.logo-container {
  margin-left: 60px;
  margin-top: 10px;
}

.login-form {
  height: 45px;
  border-radius: 8px;
  display: flex;
  align-items: center;
}

.slide-left {
  animation: slide-left 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

@keyframes slide-left {
  0% { transform: translateX(0); }
  100% { transform: translateX(-100px); }
}
</style>
