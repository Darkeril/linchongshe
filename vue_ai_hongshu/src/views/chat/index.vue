<script setup lang='ts'>
import type { Ref } from 'vue'
import {
  computed,
  nextTick,
  onMounted,
  onUnmounted,
  ref,
  watch,
} from 'vue'
import { NButton, useDialog, useMessage } from 'naive-ui'
// import html2canvas from 'html2canvas'
import { useScroll } from './hooks/useScroll'
import { useChat } from './hooks/useChat'
import { Message, Top, WaterfallFlow } from './components'
import { t } from '@/locales'
import { useBasicLayout } from '@/hooks/useBasicLayout'
import { useAppStore, useChatStore } from '@/store'
import {
  deleteChatMessageById,
  fetchChatAPI,
  fetchChatAPIProcess,
  fetchChatMessageAPI,
  fetchChatMessageById,
  listAssistantType,
} from '@/api'
import { SvgIcon } from '@/components/common'

// debugger
let controller = new AbortController()
// const openLongReply = import.meta.env.VITE_GLOB_OPEN_LONG_REPLY === 'true'
const dialog = useDialog()
const ms = useMessage()

const appStore = useAppStore()
const chatStore = useChatStore()
const { isMobile } = useBasicLayout()
const { scrollRef, scrollToBottom, scrollToBottomIfAtBottom } = useScroll()
const { addChat, updateChat, updateChatLoading, getChatByIndex } = useChat()

const dataSources = ref<Chat.Chat[]>([])
const loading = ref<boolean>(false)
const inputRef = ref<Ref | null>(null)
const ideaRef = ref<Ref | null>(null)
// 提示词
const prompt = ref<string>('')
// 是否展示提示词（右侧万花筒）默认不展示
const showPrompt = ref<boolean>(false)
// 计算灵感大全-提示词高度
const computedHeight = ref<string>('')

// 监听选中信息刷新对话内容
watch(
  () => chatStore.active,
  () => {
    chatStore.getChatByChatNumber(chatStore.active).then((res: any[]) => {
      dataSources.value = res
      scrollToBottom()
    })
  },
)

// 更新聊天数据loading状态
dataSources.value.forEach((item, index) => {
  if (item.loading)
    updateChatLoading(chatStore.active, index, { loading: false })
})

// 输入框提示
const placeholder = computed(() => {
  if (isMobile.value)
    return t('chat.placeholderMobile')
  return t('chat.placeholder')
})

// 打开万花筒时自动折叠左侧聊天记录
const isPromptOpen = computed(() => showPrompt.value || appStore.promptPanelOpen)
watch(isPromptOpen, (open) => {
  if (open)
    appStore.setSiderCollapsed(true)
  else
    appStore.setSiderCollapsed(false)
})

// 空页：用于将底部输入框置于页面中间显示
const isEmpty = computed(() => dataSources.value.length === 0)

// 发送按钮是否禁用
const buttonDisabled = computed(() => {
  return loading.value || !prompt.value || prompt.value.trim() === ''
})

// 内容样式
const indexClass = computed(() => {
  let classes = ['w-[1040px]']
  if (isMobile.value)
    classes = ['w-full', 'overflow-hidden']
  return classes
})

// 输入框高度监听
const handleInput = () => {
  inputRef.value.style.height = 'auto'
  inputRef.value.style.height = `${inputRef.value.scrollHeight - 13}px`
}

// 监听浏览器高度自动
window.addEventListener('resize', () => {
  const ideaHeight = ideaRef.value?.clientHeight || 0
  computedHeight.value = `${window.innerHeight - ideaHeight - 2}px`
})

// 禁止代码调试
// (() => {
//   function block() {
//     if (window.outerHeight - window.innerHeight > 200 || window.outerWidth - window.innerWidth > 200)
//       document.body.innerHTML = '检测到非法调试,请关闭后刷新重试!'

//     setInterval(() => {
//       (function () {
//         return false
//       }
//         .constructor('debugger')
//         .call())
//     }, 50)
//   }
//   try {
//     block()
//   }
//   catch (err) { }
// })()

onMounted(() => {
  if (inputRef.value && !isMobile.value)
    inputRef.value?.focus()

  const ideaHeight = ideaRef.value?.clientHeight || 0
  computedHeight.value = `${window.innerHeight - ideaHeight - 74}px`
  inputRef.value.addEventListener('input', handleInput)
  assistantType()
  scrollToBottom()
})

onUnmounted(() => {
  if (loading.value)
    controller.abort()
})

// 助手分类
interface AssistantType {
  id: number
  name: string
  icon: string
}

const assistantTypeId = ref<number>(-1)
const assistantTypeValue = ref<Array<AssistantType>>([])
const fixedAssistantTypeValue = ref<Array<AssistantType>>([])

// 获取助手分类
function assistantType() {
  listAssistantType<Array<AssistantType>>().then(
    (res: { data: { id: number; name: string; icon: string }[] }) => {
      const assistantType: AssistantType = {
        id: -1,
        name: '全部',
        icon: 'ri:message-line',
      }
      assistantTypeValue.value = res.data
      assistantTypeValue.value.splice(0, 0, assistantType)
      fixedAssistantTypeValue.value = assistantTypeValue.value.slice(0, 5)
    },
  )
}

// 助手分类选择
function chooseType(typeId: number) {
  assistantTypeId.value = typeId
}

// 助手分类是否选中
function isActive(typeId: number) {
  return assistantTypeId.value === typeId
}

// 选择右侧固定分类
function chooseFixedType(typeId: number | null) {
  if (typeId)
    assistantTypeId.value = typeId

  showPrompt.value = true
}

// 触发选择助手
function handAssistant(assistantId: number) {
  if (loading.value) {
    ms.warning('您的对话尚未结束，请等待对话结束后再切换～')
    return
  }
  chatStore.setActive('')
  handleSubmit(assistantId)
}

// 编辑助手
function editAssistant(systemPrompt: string) {
  prompt.value = systemPrompt
  setTimeout(() => {
    inputRef.value.style.height = 'auto'
    inputRef.value.style.height = `${inputRef.value.scrollHeight}px`
  }, 50)
}

// 发送消息
function handleEnter(event: KeyboardEvent) {
  if (!isMobile.value) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      handleSubmit()
    }
  }
  else {
    if (event.key === 'Enter' && event.ctrlKey) {
      event.preventDefault()
      handleSubmit()
    }
  }
}

// 停止输出
function handleStop() {
  if (loading.value) {
    controller.abort()
    loading.value = false
    // ms.warning('已停止响应～')
  }
}

// 发送
async function handleSubmit(assistantId?: number) {
  let message = prompt.value
  let chatNumber: string = chatStore.active
  if (loading.value) {
    ms.warning('您的对话尚未结束，请等待对话结束后再尝试～')
    return
  }
  // 没有选择助手的时候判断文字
  if (!assistantId) {
    if (!message || message.trim() === '')
      return
  }

  const chatData: Chat.ChatRequest = {
    assistantId,
    systemPrompt: message,
    prompt: message,
  }
  // 创建会话
  if (!chatNumber) {
    const chatRes = await fetchChatAPI(chatData)
    chatNumber = chatRes.data.chatNumber
    message = chatRes.data.prompt
    if (assistantId)
      prompt.value = chatRes.data.prompt

    chatStore.setActive(chatNumber)
    await nextTick()
  }
  const messageData: Chat.MessageRequest = {
    chatNumber,
    model: chatStore.model,
    systemPrompt: message,
    prompt: message,
  }
  // 发送内容
  const messageRes = await fetchChatMessageAPI(messageData)
  // 流式响应
  onConversation(chatNumber, messageRes.data)
}

/**
 * sse对话
 */
async function onConversation(chatNumber: string, conversationId: string) {
  const message = prompt.value

  controller = new AbortController()

  addChat(chatNumber, {
    conversationId,
    dateTime: new Date().toLocaleString(),
    contentType: 'text',
    text: message,
    model: chatStore.model,
    inversion: true,
    error: false,
  })

  scrollToBottom()

  loading.value = true
  prompt.value = ''

  setTimeout(() => {
    inputRef.value.style.height = 'auto'
    inputRef.value.style.height = `${inputRef.value.scrollHeight - 13}px`
  }, 50)

  addChat(chatNumber, {
    parentMessageId: conversationId,
    dateTime: new Date().toLocaleString(),
    contentType: 'text',
    text: '',
    images: [],
    model: chatStore.model,
    loading: true,
    inversion: false,
    error: false,
  })

  scrollToBottom()

  try {
    const lastText = ''
    const fetchChatAPIOnce = async () => {
      await fetchChatAPIProcess<Chat.ConversationResponse>({
        conversationId,
        signal: controller.signal,
        onDownloadProgress: ({ event }) => {
          const xhr = event.target
          const { responseText } = xhr
          const lastIndex = responseText.lastIndexOf(
            '\n',
            responseText.length - 2,
          )
          let chunk = responseText
          if (lastIndex !== -1)
            chunk = responseText.substring(lastIndex)

          try {
            const data = JSON.parse(chunk)
            updateChat(chatNumber, dataSources.value.length - 1, {
              conversationId: data.conversationId,
              parentMessageId: conversationId,
              dateTime: new Date().toLocaleString(),
              contentType: data.contentType,
              text:
                data.contentType === 'text'
                  ? lastText + (data.content ?? '')
                  : '',
              images:
                (data.contentType === 'image' && data.content)
                  ? data.content.map((d: { b64Image: any }) => {
                    return d.b64Image
                  })
                  : [],
              model: chatStore.model,
              inversion: false,
              error: false,
              loading: true,
            })
            nextTick(() => {
              scrollToBottom()
              scrollToBottomIfAtBottom()
            })
          }
          catch (error) {
            //
          }
        },
      })
      updateChatLoading(chatNumber, dataSources.value.length - 1, {
        loading: false,
      })
    }
    await fetchChatAPIOnce()
  }
  catch (error: any) {
    const errorMessage = error?.message ?? t('common.wrong')
    if (error?.message === 'canceled') {
      updateChatLoading(chatNumber, dataSources.value.length - 1, {
        loading: false,
      })
      scrollToBottomIfAtBottom()
      return
    }

    const currentChat = getChatByIndex(
      chatNumber,
      dataSources.value.length - 1,
    )

    if (currentChat?.text && currentChat.text !== '') {
      updateChatLoading(chatNumber, dataSources.value.length - 1, {
        text: `${currentChat.text}`,
        error: false,
        loading: false,
      })
      return
    }

    updateChat(chatNumber, dataSources.value.length - 1, {
      dateTime: new Date().toLocaleString(),
      contentType: 'text',
      text: errorMessage,
      inversion: false,
      error: true,
      loading: false,
    })
    scrollToBottomIfAtBottom()
  }
  finally {
    loading.value = false
  }
}

/** 重新回复 */
async function onRegenerate(messageId?: string | null) {
  if (!messageId)
    return
  fetchChatMessageById(messageId).then((res: { data: { content: string } }) => {
    prompt.value = res.data.content
    handleSubmit()
  })
}

// 删除消息
function handleDelete(index: number, messageId?: string | null) {
  if (loading.value)
    return
  if (!messageId)
    return
  dialog.warning({
    title: t('chat.deleteMessage'),
    content: t('chat.deleteMessageConfirm'),
    positiveText: t('common.yes'),
    negativeText: t('common.no'),
    onPositiveClick: () => {
      chatStore.deleteChatByUuid(chatStore.active, index)
      chatStore.deleteChatByUuid(chatStore.active, index - 1)
      deleteChatMessageById(messageId)
    },
  })
}

// 跳转页面
// function jumpToXieyi(type: number) {
//   const { href } = router.resolve({
//     path: '/agreement',
//     query: { type },
//   })
//   // 打开新窗口
//   window.open(href)
// }
</script>

<template>
  <div class="flex w-full h-full bg-slate-50 dark:bg-[#18181c]">
    <div class="flex flex-col h-full m-auto" :class="indexClass">
      <main v-show="!isEmpty" class="flex-1 w-full pt-2 mb-4 overflow-hidden">
        <div
          id="scrollRef"
          ref="scrollRef"
          class="h-full overflow-hidden overflow-y-auto no-scrollbar"
        >
          <div id="image-wrapper" class="max-w-screen-xl dark:bg-[#18181c]">
            <template v-if="dataSources.length">
              <div>
                <Message
                  v-for="(item, index) of dataSources"
                  :key="index"
                  :date-time="item.dateTime"
                  :content-type="item.contentType"
                  :text="item.text"
                  :images="item.images"
                  :model="item.model"
                  :inversion="item.inversion"
                  :error="item.error"
                  :loading="item.loading"
                  @regenerate="onRegenerate(item.parentMessageId)"
                  @delete="handleDelete(index, item.conversationId)"
                />
                <div class="h-10" />
                <div class="sticky bottom-0 left-0 flex justify-center">
                  <NButton v-if="loading" @click="handleStop">
                    <template #icon>
                      <SvgIcon icon="ri:stop-circle-line" />
                    </template>
                    {{ t('chat.stopResponding') }}
                  </NButton>
                </div>
              </div>
            </template>
          </div>
        </div>
      </main>
      <div v-if="isEmpty" class="flex flex-col items-center justify-center flex-1 space-y-1">
        <Top @hand-assistant="handAssistant" />
        <div
          class="flex items-center justify-between w-[720px] max-w-[88%] h-16 px-5 bg-white dark:bg-[#24272e] border border-slate-200/60 dark:border-neutral-700 rounded-[26px] shadow-[0_6px_22px_rgba(0,0,0,0.06)]"
        >
          <textarea
            ref="inputRef"
            v-model="prompt"
            class="flex-1 max-h-[120px] pr-3 outline-none no-scrollbar resize-none bg-transparent text-[16px] dark:text-white text-left"
            :class="prompt ? ['h-12','leading-7'] : ['h-16','leading-[64px]']"
            :placeholder="placeholder"
            @keypress="handleEnter"
          />
          <div class="flex flex-col justify-end">
            <div
              class="flex items-center justify-center w-10 h-10 rounded-full bg-indigo-500 hover:bg-indigo-600 text-white cursor-pointer"
              :class="buttonDisabled ? ['opacity-60','pointer-events-none'] : []"
              @click="handleSubmit()"
            >
              <SvgIcon icon="ri:send-plane-fill" class="text-xl" />
            </div>
          </div>
        </div>
      </div>
      <footer v-show="!isEmpty" class="w-full">
        <div
          class="flex justify-between max-w-screen-xl max-h-[282px] mb-2 p-3 bg-white dark:bg-[#24272e] dark:border-none border border-slate-300 rounded-xl"
        >
          <textarea
            ref="inputRef"
            v-model="prompt"
            class="w-[920px] h-[32px] max-h-[244px] outline-none no-scrollbar resize-none dark:bg-[#24272e]"
            :placeholder="placeholder"
            @keypress="handleEnter"
          />
          <div class="flex flex-col justify-end">
            <div
              class="flex items-center justify-center w-[32px] h-[32px] bg-blue-50 dark:bg-[#24272e] rounded-2xl cursor-pointer"
              :class="buttonDisabled ? ['pointer-events-none'] : ['hover:bg-blue-100']"
              @click="handleSubmit()"
            >
              <SvgIcon icon="ri:send-plane-fill" class="text-2xl text-blue-600 mr-[3px]" />
            </div>
          </div>
        </div>
        <div v-show="!isEmpty" class="mb-2 text-xs text-center text-slate-400">
          {{ appStore.baseInfo.copyright }}
          <!-- <span
            class="text-blue-600 cursor-pointer"
            @click="jumpToXieyi(1)"
          >{{ t('common.userXieyi') }}</span> |
          <span
            class="text-blue-600 cursor-pointer"
            @click="jumpToXieyi(2)"
          >{{ t('common.privacyZhengce') }}</span> -->
        </div>
      </footer>
    </div>
    <div v-if="!isMobile" class="flex flex-col items-center h-full">
      <div v-if="showPrompt || appStore.promptPanelOpen" class="w-[510px] bg-[#fff] dark:bg-[#24272e] pb-0">
        <div ref="ideaRef" class="h-auto p-4 pb-2">
          <div class="flex justify-between mb-3">
            <div class="flex items-center">
              <SvgIcon icon="icons8:idea" class="mr-2 text-xl font-bold text-blue-600" />
              <div class="text-sm font-medium">
                {{ t('chat.idea') }}
              </div>
            </div>
            <div class="cursor-pointer" @click="showPrompt = false; appStore.setPromptPanelOpen(false)">
              <SvgIcon icon="ic:round-close" class="mr-2 text-xl font-bold" />
            </div>
          </div>
          <div class="flex flex-wrap">
            <div
              v-for="item in assistantTypeValue"
              :key="item.id"
              class="px-3 py-1 mb-2 mr-2 text-sm rounded-md cursor-pointer"
              :class="isActive(item.id) ? ['text-white', 'bg-blue-600', 'hover:bg-blue-700'] : ['bg-slate-100', 'dark:bg-[#24272e]', 'hover:bg-blue-50', 'hover:text-blue-500']"
              @click="chooseType(item.id)"
            >
              {{ item.name }}
            </div>
          </div>
        </div>
        <div :style="{ height: computedHeight, maxHeight: computedHeight }">
          <WaterfallFlow
            :assistant-type-id="assistantTypeId"
            @hand-assistant="handAssistant"
            @edit-assistant="editAssistant"
          />
        </div>
      </div>
      <div v-else class="flex flex-col items-center justify-center w-12 h-full">
        <div class="flex flex-col items-center justify-center w-12 bg-white dark:bg-[#24272e] rounded-l-lg p-2">
          <div class="flex flex-col items-center p-1 rounded-lg cursor-pointer w-10 hover:text-blue-600" @click="chooseFixedType(null)">
            <div class="flex items-center justify-center w-5 h-5 bg-slate-100 dark:bg-[#24272e] rounded-xl">
              <SvgIcon icon="fa6-solid:angles-left" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
