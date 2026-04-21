<script setup lang='ts'>
import { computed, onMounted, ref, watch } from 'vue'
import { NPopconfirm, NScrollbar, useMessage } from 'naive-ui'
import { SvgIcon } from '@/components/common'
import { useAppStore, useChatStore } from '@/store'
import { useBasicLayout } from '@/hooks/useBasicLayout'
import { debounce } from '@/utils/functions/debounce'
import { listChat, removeChat } from '@/api'

const ms = useMessage()
const { isMobile } = useBasicLayout()
const appStore = useAppStore()
const chatStore = useChatStore()

// 获取会话列表内容
const dataSources = ref<Array<Chat.History>>()

// 工具：安全解析时间为 Date
function parseDate(dateStr: string): Date {
  if (!dateStr)
    return new Date(0)
  const raw = dateStr.trim()
  // 纯数字（时间戳）
  if (/^\d{10,}$/.test(raw)) {
    const ms = raw.length === 10 ? Number(raw) * 1000 : Number(raw)
    return new Date(ms)
  }
  // 优先尝试标准或常见格式
  const try1 = new Date(raw)
  if (!isNaN(try1.getTime()))
    return try1
  const try2 = new Date(raw.replace(/-/g, '/'))
  if (!isNaN(try2.getTime()))
    return try2
  // 提取 年月日 数字
  const nums = raw.match(/\d+/g) || []
  if (nums.length >= 3) {
    const [y, m, d] = [Number(nums[0]), Number(nums[1] || 1), Number(nums[2] || 1)]
    return new Date(y, Math.max(0, m - 1), Math.max(1, d))
  }
  if (nums.length >= 2) {
    const [y, m] = [Number(nums[0]), Number(nums[1] || 1)]
    return new Date(y, Math.max(0, m - 1), 1)
  }
  return new Date(0)
}

function startOfDay(date: Date): Date {
  const d = new Date(date)
  d.setHours(0, 0, 0, 0)
  return d
}

function formatYearMonth(date: Date): string {
  const y = date.getFullYear()
  const m = `${date.getMonth() + 1}`.padStart(2, '0')
  return `${y}年${m}月`
}

// 计算：将会话列表分组
const groupedList = computed(() => {
  const list = dataSources.value || []
  const todayStart = startOfDay(new Date())

  const fixedBuckets: Record<string, Chat.History[]> = {
    '今天': [],
    '昨天': [],
    '3天内': [],
    '7天内': [],
    '30天内': [],
  }
  const monthBuckets: Record<string, Chat.History[]> = {}

  for (const item of list) {
    const d = startOfDay(parseDate(item.createTime))
    const diffDays = Math.floor((todayStart.getTime() - d.getTime()) / (24 * 60 * 60 * 1000))

    if (diffDays === 0) {
      fixedBuckets['今天'].push(item)
    }
    else if (diffDays === 1) {
      fixedBuckets['昨天'].push(item)
    }
    else if (diffDays <= 3) {
      fixedBuckets['3天内'].push(item)
    }
    else if (diffDays <= 7) {
      fixedBuckets['7天内'].push(item)
    }
    else if (diffDays <= 30) {
      fixedBuckets['30天内'].push(item)
    }
    else {
      const ym = formatYearMonth(d)
      if (!monthBuckets[ym])
        monthBuckets[ym] = []
      monthBuckets[ym].push(item)
    }
  }

  // 固定组的顺序
  const order = ['今天', '昨天', '3天内', '7天内', '30天内']
  const groups: { key: string; label: string; items: Chat.History[] }[] = []
  for (const k of order) {
    const arr = fixedBuckets[k]
    if (arr && arr.length)
      groups.push({ key: k, label: k, items: arr })
  }

  // 按年月分组：按时间倒序展示（最近的年月在前）
  const monthKeys = Object.keys(monthBuckets).sort((a, b) => {
    // 比较年月，转换为数字 2025-09 形式再比较
    const toNum = (ym: string) => parseInt(ym.replace('年', '').replace('月', '').padStart(6, '0'))
    return toNum(b) - toNum(a)
  })
  for (const key of monthKeys)
    groups.push({ key, label: key, items: monthBuckets[key] })

  return groups
})

// 分组内 sticky 标题版本

onMounted(() => {
  listChatApi()
})

// 监听选中信息
watch(
  () => chatStore.active,
  () => {
    if (!chatStore.active)
      return

    const index = dataSources.value ? dataSources.value.findIndex(item => item.chatNumber === chatStore.active) : -1
    if (index === -1)
      listChatApi()
  },
)

function listChatApi() {
  listChat<Array<Chat.History>>().then((res) => {
    dataSources.value = res.data
  })
}

async function handleSelect({ chatNumber }: Chat.History) {
  if (isActive(chatNumber))
    return
  await chatStore.setActive(chatNumber)
  if (isMobile.value)
    appStore.setSiderCollapsed(true)
}

// 是否选中当前会话
function isActive(chatNumber: string) {
  return chatStore.active === chatNumber
}

/** 删除会话 */
const handleDeleteDebounce = debounce(handleDelete, 600)

function handleDelete(chatNumber: string, event?: MouseEvent | TouchEvent) {
  event?.stopPropagation()
  removeChat(chatNumber).then((res) => {
    if (res.code === 200) {
      listChatApi()
      chatStore.setActive('')
      ms.success('删除成功')
    }
    else {
      ms.error(res.msg)
    }
  })
  if (isMobile.value)
    appStore.setSiderCollapsed(true)
}
</script>

<template>
  <NScrollbar>
    <div class="flex flex-col pr-3">
      <template v-if="!dataSources || dataSources.length === 0">
        <div class="flex flex-col items-center mt-4 text-center text-neutral-300">
          <span>{{ $t('common.noData') }}</span>
        </div>
      </template>
      <template v-else>
        <div v-for="group in groupedList" :key="group.key" class="mb-0.5">
          <div class="sticky top-0 left-0 z-10 block px-3 py-0.5 text-xs text-neutral-500 bg-white dark:bg-[#24272e] pointer-events-none">
            {{ group.label }}
          </div>
          <div v-for="item in group.items" :key="item.chatNumber">
            <a
              class="flex flex-1 items-center justify-between h-10 p-2 cursor-pointer hover:bg-neutral-100 hover:text-blue-500 hover:border-l-2 hover:border-blue-600
              dark:hover:bg-[#24272e] dark:hover:text-white dark:hover:border-none hover:transition-all"
              :class="isActive(item.chatNumber) && ['border-blue-600', 'dark:border-none', 'bg-neutral-100', 'dark:bg-[#24272e]', 'text-blue-400', 'dark:text-white', 'border-l-2', 'shadow']"
              @click="handleSelect(item)"
            >
              <div class="flex flex-col w-[208px]">
                <div class="overflow-hidden text-ellipsis whitespace-nowrap">{{ item.title }}</div>
              </div>
              <div v-if="isActive(item.chatNumber)">
                <NPopconfirm placement="bottom" @positive-click="handleDeleteDebounce(item.chatNumber, $event)">
                  <template #trigger>
                    <button class="p-1">
                      <SvgIcon icon="ri:delete-bin-line" />
                    </button>
                  </template>
                  {{ $t('chat.deleteHistoryConfirm') }}
                </NPopconfirm>
              </div>
            </a>
          </div>
        </div>
      </template>
    </div>
  </NScrollbar>
</template>
