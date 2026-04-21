<script setup lang='ts'>
import { computed, onMounted, ref } from 'vue'
import type { FormInst, UploadFileInfo } from 'naive-ui'
import { NAvatar, NButton, NForm, NFormItem, NIcon, NInput, NInputNumber, NModal, NPopconfirm, NSwitch, NTooltip, NUpload, useMessage } from 'naive-ui'
import { Person } from '@vicons/ionicons5'
import { redemptionActivation, removeAllChat, shareAccount, updateContext, updatePassword, updateUser } from '@/api'
import { useAuthStoreWithout } from '@/store/modules/auth'
import { useAuthStore } from '@/store'
import { router } from '@/router'
import { SvgIcon } from '@/components/common'
import { t } from '@/locales'
import { copyToClip } from '@/utils/copy'
import defaultAvatar from '@/assets/avatar.png'
import { resolveApiUrl, resolveAvatarUrl } from '@/utils/functions'

const props = defineProps<Props>()
const emit = defineEmits<Emit>()
const ms = useMessage()
const authStore = useAuthStoreWithout()

const active = ref<string>('base')
const loading = ref(false)
const formRef = ref<FormInst | null>(null)
const token = useAuthStore().token

interface Props {
  visible: boolean
}

interface Emit {
  (e: 'update:visible', visible: boolean): void
}

const show = computed({
  get() {
    return props.visible
  },
  set(visible: boolean) {
    emit('update:visible', visible)
  },
})

interface User {
  avatar: string
  name: string
  nickName: string
  tel: string
  context: boolean
  num: number
  oldPassword: string
  newPassword: string
  account: string
  password: string
  limit: number
  code: string
}
const userValue = ref<User>({
  avatar: '',
  name: '',
  nickName: '',
  tel: '',
  context: false,
  num: 0,
  oldPassword: '',
  newPassword: '',
  account: '',
  password: '',
  limit: 1,
  code: '',
})

const rules = {
  name: {
    required: true,
    message: '请输入昵称',
    trigger: ['input', 'blur'],
  },
  tel: {
    required: true,
    pattern: /^[1][3456789]\d{9}$/,
    message: '请输入正确的手机号码',
    trigger: ['input', 'blur'],
  },
  oldPassword: [
    {
      required: true,
      message: '请输入旧密码',
      trigger: ['input', 'blur'],
    },
    {
      trigger: ['blur', 'change'],
      pattern: /^(?=^.*?[a-z])(?=^.*?\d).{6,32}$/,
      message: '请输入6-32位包含字母和数字的密码',
    },
  ],
  newPassword: [
    {
      required: true,
      message: '请输入新密码',
      trigger: ['input', 'blur'],
    },
    {
      trigger: ['blur', 'change'],
      pattern: /^(?=^.*?[a-z])(?=^.*?\d).{6,32}$/,
      message: '请输入6-32位包含字母和数字的密码',
    },
  ],
  account: {
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
  limit: [
    {
      required: true,
      trigger: ['blur', 'change'],
      pattern: /^[1-9]\d*$/,
      message: '电量值有误，请输入正确电量值',
    },
  ],
}

onMounted(async () => {
  if (!authStore.session) {
    router.push('/login')
    return
  }
  await authStore.getSession()
  userValue.value = {
    avatar: authStore.session.avatar ? authStore.session.avatar : defaultAvatar,
    name: authStore.session.name,
    nickName: authStore.session.nickName,
    tel: authStore.session.tel,
    context: authStore.session.context,
    num: authStore.session.num,
    oldPassword: '',
    newPassword: '',
    account: '',
    password: '',
    limit: 1,
    code: '',
  }
})

/** 页面跳转 */
function handleJump(value: string) {
  active.value = value
}

/** 关闭弹窗 */
function handleClose() {
  show.value = false
}

/** 开启/关闭上下文 */
async function handleContext(value: boolean) {
  userValue.value.context = value
  updateContext(userValue.value).then((res) => {
    if (value)
      ms.success('开启成功')
    else
      ms.warning('关闭成功')

    authStore.getSession()
  })
}

/** 清除所有聊天记录 */
async function handleDelAll() {
  removeAllChat().then((res) => {
    if (res.code === 200) {
      ms.success('删除成功')
      setTimeout(() => {
        location.reload()
      }, 500)
    }
    else { ms.error(res.msg) }
  })
}

/** 分享GPTHUB */
async function handleShare() {
  const text = 'https://gpthub.life'
  await copyToClip(text)
  ms.success('复制成功')
}

/** 账号分销 */
async function handleShareAccount() {
  formRef.value?.validate(async (errors: any) => {
    if (errors)
      return
    if (userValue.value.limit >= userValue.value.num) {
      ms.error('请确认当前账号电量是否充足，分享电量值需小于当前总电量')
      return
    }
    try {
      loading.value = true
      shareAccount(userValue.value).then((res) => {
        if (res.code === 200) {
          ms.success('账户分销成功')
          show.value = false
          authStore.getSession()
        }
        else {
          ms.error(res.msg ?? '账户分销失败')
        }
      })
    }
    finally {
      loading.value = false
    }
  })
}

/** 兑换码激活 */
async function handleRedemptionActivation() {
  formRef.value?.validate(async (errors: any) => {
    if (errors)
      return
    try {
      loading.value = true
      redemptionActivation(userValue.value).then((res) => {
        if (res.code === 200) {
          ms.success(' 兑换码激活成功')
          show.value = false
          authStore.getSession()
        }
        else {
          ms.error(res.msg ?? '兑换码激活失败，请重试')
        }
      })
    }
    finally {
      loading.value = false
    }
  })
}

/** 修改个人信息 */
async function handleUpdateUser() {
  formRef.value?.validate(async (errors: any) => {
    if (errors)
      return

    try {
      loading.value = true
      updateUser(userValue.value).then((res) => {
        if (res.code === 200) {
          ms.success('修改成功')
          show.value = false
          authStore.getSession()
        }
        else {
          ms.error(res.msg ?? '修改失败')
        }
      })
    }
    finally {
      loading.value = false
    }
  })
}

/** 上传头像成功回调 */
const handleUploadFinish = ({
  file,
  event,
}: {
  file: UploadFileInfo
  event?: ProgressEvent
}) => {
  try {
    const raw = (event?.target as XMLHttpRequest)?.response || ''
    const res = raw ? JSON.parse(raw) : {}
    if (!res || res.code !== 200) {
      ms.error(res?.msg ?? '头像修改失败')
      return
    }

    const data = res.data
    const fileUrl = typeof data === 'string'
      ? data
      : (data?.fileUrl || data?.url || data?.path || data?.location || '')

    if (!fileUrl) {
      ms.error('头像上传成功但返回地址为空')
      return
    }

    userValue.value.avatar = fileUrl
    authStore.getSession()
    ms.success('头像修改成功')
  }
  catch (e) {
    ms.error('头像上传响应解析失败')
  }
}

/** 修改密码 */
async function handleUpdatePassword() {
  formRef.value?.validate(async (errors: any) => {
    if (errors)
      return

    if (userValue.value.oldPassword === userValue.value.newPassword) {
      ms.error('新密码与旧密码相同')
      return
    }
    try {
      loading.value = true
      updatePassword(userValue.value).then((res) => {
        if (res.code === 200) {
          ms.success('修改成功')
          show.value = false
        }
        else {
          ms.error(res.msg ?? '修改失败')
        }
      })
    }
    finally {
      loading.value = false
    }
  })
}

/** 退出登录 */
function handleLogout() {
  authStore.removeToken()
  ms.success(t('退出成功'))
  router.push('/login')
}
</script>

<template>
  <NModal v-model:show="show" :auto-focus="false">
    <div class="w-4/12 bg-slate-50 dark:bg-[#18181c] p-6">
      <div v-if="active === 'base'" class="flex flex-col items-center justify-center">
        <div class="self-end cursor-pointer" @click="handleClose">
          <SvgIcon icon="ant-design:close-outlined" class="text-xl" />
        </div>
        <div class="flex items-center justify-between">
          <div class="flex flex-col items-center justify-center">
            <NAvatar
              v-if="userValue.avatar !== ''" round object-fit="fill" :size="100"
              :src="resolveAvatarUrl(userValue.avatar)"
            />
            <NAvatar v-else round :size="100">
              <NIcon size="40">
                <Person />
              </NIcon>
            </NAvatar>
            <div class="mt-3 font-medium">
              {{ userValue.nickName }}
            </div>
            <div v-if="userValue.tel" class="mt-2 text-xs">
              手机号：{{ userValue.tel }}
            </div>
          </div>
          <div class="flex flex-col items-center justify-center ml-28" style="margin-left: 40px;">
            <div class="flex items-center">
              <div class="font-medium text-[15px]">
                剩余电量
              </div>
              <NTooltip trigger="hover">
                <template #trigger>
                  <SvgIcon icon="octicon:info-16" class="ml-1" />
                </template>
                分享好友即可获得电量，开通会员可享更多权益
              </NTooltip>
            </div>
            <div class="flex items-center mt-2" style="margin-left: 80px;">
              <span class="text-[15px]">{{ userValue.num }}</span>
              <SvgIcon icon="mingcute:lightning-fill" class="ml-1 mr-3 text-2xl text-yellow-400" />
              <NButton type="success" class="text-[15px]" @click="handleJump('redemption')">
                兑换
              </NButton>
            </div>
            <!-- <div class="mt-4">
              <NButton block type="primary" @click="handleOpenMember">
                开通会员
              </NButton>
            </div> -->
          </div>
        </div>
        <div class="w-full mt-4 bg-white dark:bg-[#18181c] border dark:border-[#24272e] rounded-xl">
          <!-- 个人资料 -->
          <div
            class="w-full h-12 px-4 flex items-center border-b dark:border-[#24272e] cursor-pointer hover:bg-slate-200 hover:dark:bg-[#24272e]"
            @click="handleJump('general')"
          >
            <div class="flex items-center justify-center bg-blue-600 rounded-lg w-7 h-7">
              <SvgIcon icon="basil:user-solid" class="text-xl text-white" />
            </div>
            <div class="ml-3 text-[17px] text-blue-600">
              设置个人资料
            </div>
            <div class="flex justify-end flex-1">
              <SvgIcon icon="formkit:right" class="text-xl" />
            </div>
          </div>
          <!-- 上下文开关 -->
          <div
            class="w-full h-12 px-4 flex items-center border-b dark:border-[#24272e] cursor-pointer hover:bg-slate-200 hover:dark:bg-[#24272e]"
          >
            <div class="flex items-center justify-center bg-indigo-600 rounded-lg w-7 h-7">
              <SvgIcon icon="bitcoin-icons:message-filled" class="text-xl text-white" />
            </div>
            <div class="ml-3 text-[17px]">
              开启上下文
            </div>
            <div class="flex justify-end flex-1">
              <NSwitch v-model:value="userValue.context" @update:value="handleContext" />
            </div>
          </div>
          <!-- 清除所有聊天记录 -->
          <NPopconfirm placement="right" @positive-click="handleDelAll">
            <template #trigger>
              <div class="w-full h-12 px-4 flex items-center border-b dark:border-[#24272e] cursor-pointer hover:bg-slate-200 hover:dark:bg-[#24272e]">
                <div class="flex items-center justify-center bg-red-600 rounded-lg w-7 h-7">
                  <SvgIcon icon="basil:trash-solid" class="text-xl text-white" />
                </div>
                <div class="ml-3 text-[17px]">
                  清除所有聊天记录
                </div>
                <div class="flex justify-end flex-1">
                  <SvgIcon icon="basil:trash-solid" class="text-xl" />
                </div>
              </div>
            </template>
            {{ $t('chat.clearHistoryConfirm') }}
          </NPopconfirm>
          <!-- 账号分销 -->
          <div
            class="w-full h-12 px-4 flex items-center border-b dark:border-[#24272e] cursor-pointer hover:bg-slate-200 hover:dark:bg-[#24272e]"
            @click="handleJump('account')"
          >
            <div class="flex items-center justify-center bg-green-600 rounded-lg w-7 h-7">
              <SvgIcon icon="basil:share-solid" class="text-xl text-white" />
            </div>
            <div class="ml-3 text-[17px]">
              电量共享
            </div>
            <div class="flex justify-end flex-1">
              <SvgIcon icon="formkit:right" class="text-xl" />
            </div>
          </div>
          <!-- 分享 -->
          <div
            class="w-full h-12 px-4 flex items-center cursor-pointer hover:bg-slate-200 hover:dark:bg-[#24272e]"
            @click="handleShare"
          >
            <div class="flex items-center justify-center bg-green-600 rounded-lg w-7 h-7">
              <SvgIcon icon="majesticons:share" class="text-xl text-white" />
            </div>
            <div class="ml-3 text-[17px]">
              分享GPTHUB给好友
            </div>
            <div class="flex justify-end flex-1">
              <SvgIcon icon="basil:forward-solid" class="text-xl" />
            </div>
          </div>
        </div>

        <!-- 修改密码 -->
        <div class="w-full mt-4 bg-white dark:bg-[#18181c] border dark:border-[#24272e] rounded-xl">
          <div
            class="w-full h-12 px-4 flex items-center border-b dark:border-[#24272e] cursor-pointer hover:bg-slate-200 hover:dark:bg-[#24272e]"
            @click="handleJump('password')"
          >
            <div class="flex items-center justify-center bg-red-500 rounded-lg w-7 h-7">
              <SvgIcon icon="solar:lock-password-bold" class="text-xl text-white" />
            </div>
            <div class="ml-3 text-[17px]">
              修改密码
            </div>
            <div class="flex justify-end flex-1">
              <SvgIcon icon="formkit:right" class="text-xl" />
            </div>
          </div>
          <!-- 退出登录 -->
          <div
            class="w-full h-12 px-4 flex items-center cursor-pointer hover:bg-slate-200 hover:dark:bg-[#24272e]"
            @click="handleLogout"
          >
            <div class="flex items-center justify-center bg-purple-600 rounded-lg w-7 h-7">
              <SvgIcon icon="line-md:logout" class="text-xl text-white" />
            </div>
            <div class="ml-3 text-[17px]">
              退出登录
            </div>
          </div>
        </div>
      </div>

      <!-- 个人信息修改页面 -->
      <div v-if="active === 'general'" class="flex flex-col items-center justify-center">
        <div class="self-start cursor-pointer" @click="handleJump('base')">
          <SvgIcon icon="icon-park-solid:back" class="text-xl text-blue-600" />
        </div>
        <div class="relative cursor-pointer">
          <NUpload
            :show-file-list="false" :action="resolveApiUrl('/app/user/avatar')" :headers="{
              Authorization: token,
            }" @finish="handleUploadFinish"
          >
            <NAvatar v-if="userValue.avatar != ''" round :size="100" :src="resolveAvatarUrl(userValue.avatar)" />
            <NAvatar v-else round :size="100">
              <NIcon size="40">
                <Person />
              </NIcon>
            </NAvatar>
            <SvgIcon
              icon="raphael:photo"
              class="text-4xl text-blue-600 absolute right-[-8px] bottom-[4px] z-10"
            />
          </NUpload>
        </div>
        <NForm ref="formRef" :model="userValue" :rules="rules">
          <NFormItem label="昵称" path="name">
            <NInput v-model:value="userValue.nickName" class="user-form" placeholder="请输入昵称" />
          </NFormItem>
          <NFormItem label="手机号">
            <NInput v-model:value="userValue.tel" class="user-form" :disabled="true" placeholder="请输入手机号" />
          </NFormItem>
          <NButton class="user-form" block type="primary" @click="handleUpdateUser">
            确 认
          </NButton>
        </NForm>
      </div>
      <!-- 密码修改页面 -->
      <div v-if="active === 'password'" class="flex flex-col items-center justify-center">
        <div class="self-start cursor-pointer" @click="handleJump('base')">
          <SvgIcon icon="icon-park-solid:back" class="text-xl text-blue-600" />
        </div>
        <div class="relative cursor-pointer">
          <NAvatar v-if="userValue.avatar != ''" round :size="100" :src="resolveAvatarUrl(userValue.avatar)" />
          <NAvatar v-else round :size="100">
            <NIcon size="40">
              <Person />
            </NIcon>
          </NAvatar>
        </div>
        <NForm ref="formRef" :model="userValue" :rules="rules">
          <NFormItem label="旧密码" path="oldPassword">
            <NInput
              v-model:value="userValue.oldPassword" class="user-form" type="password"
              placeholder="请输入旧密码"
            />
          </NFormItem>
          <NFormItem label="新密码" path="newPassword">
            <NInput
              v-model:value="userValue.newPassword" class="user-form" type="password"
              placeholder="请输入新密码"
            />
          </NFormItem>
          <NButton class="user-form" block type="primary" @click="handleUpdatePassword">
            确 认
          </NButton>
        </NForm>
      </div>
      <!-- 账号分销页面 -->
      <div v-if="active === 'account'" class="flex flex-col items-center justify-center">
        <div class="self-start cursor-pointer" @click="handleJump('base')">
          <SvgIcon icon="icon-park-solid:back" class="text-xl text-blue-600" />
        </div>
        <div class="font-medium text-[15px]">
          🔋剩余电量
        </div>
        <div class="flex items-center mt-2">
          <span class="text-[15px]">{{ userValue.num }}</span>
          <SvgIcon icon="mingcute:lightning-fill" class="ml-1 text-2xl text-yellow-400" />
        </div>
        <div class="mt-2 text-xs">
          <p class="subtitle ">
            分销说明*
          </p>
          <p class="description">
            将当前账号的部分电量共享给一个新账号（创建一个新账号）<br>
            赠送电量由当前分销人设置，不能超过当前账号的总电量。
          </p>
        </div>
        <br>
        <NForm ref="formRef" :model="userValue" :rules="rules">
          <NFormItem label="手机号" path="account">
            <NInput v-model:value="userValue.account" class="user-form" placeholder="请输入账号" />
          </NFormItem>
          <NFormItem label="密码" path="password">
            <NInput v-model:value="userValue.password" class="user-form" placeholder="请输入密码" />
          </NFormItem>
          <NFormItem label="赠送电量" path="limit">
            <NInputNumber v-model:value="userValue.limit" :min="1" :max="userValue.num" class="user-form" placeholder="请输入电量" />
          </NFormItem>
          <NButton class="user-form" block type="primary" @click="handleShareAccount">
            确 认
          </NButton>
        </NForm>
      </div>
      <!-- 兑换码 -->
      <div v-if="active === 'redemption'" class="flex flex-col items-center justify-center">
        <div class="self-start cursor-pointer" @click="handleJump('base')">
          <SvgIcon icon="icon-park-solid:back" class="text-xl text-blue-600" />
        </div>
        <div class="font-medium text-[25px]">
          兑换码激活
        </div>
        <NForm ref="formRef" :model="userValue" :rules="rules">
          <NFormItem>
            <NInput v-model:value="userValue.code" class="user-form" placeholder="请输入兑换码" />
          </NFormItem>
          <NButton class="user-form" block type="primary" @click="handleRedemptionActivation">
            激 活
          </NButton>
        </NForm>
      </div>
    </div>
  </NModal>
</template>

<style scoped>
.user-form {
    height: 45px;
    width: 380px;
    border-radius: 8px;
    display: flex;
    align-items: center;
}
.subtitle {
  font-weight: bold;
  margin-left: -10px;
  color: #d71f1f;
}

.description {
  font-weight: bold;
  margin-left: 15px;
  color: #7b7b7b;
}
</style>
