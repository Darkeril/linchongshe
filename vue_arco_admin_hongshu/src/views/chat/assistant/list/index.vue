<template>
  <div class="container">
    <Breadcrumb
      :items="[$t('menu.chat'), $t('menu.chat.assistant.list')]"
    ></Breadcrumb>
    <a-card
      class="general-card r-nav-card"
      :bordered="false"
      :body-style="{ padding: '15px' }"
    >
      <a-row>
        <a-col :flex="1">
          <a-form
            :model="query"
            :label-col-props="{ span: 6 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="left"
            auto-label-width
            @submit.prevent
          >
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="title" :label="$t('biz.assistantName')">
                  <a-input v-model="query.title" :placeholder="$t('biz.pleaseInputAssistantName')" allow-clear @press-enter="handleQuery" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="typeId" :label="$t('biz.categoryId')">
                  <a-input v-model="query.typeId" :placeholder="$t('biz.pleaseInputCategoryId')" allow-clear @press-enter="handleQuery" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 42px" direction="vertical" />
        <a-col :flex="'none'" style="text-align: right">
          <a-space :size="18" wrap>
            <a-button type="primary" @click="handleQuery">{{ $t('common.search') }}</a-button>
            <a-button @click="resetQuery">{{ $t('common.reset') }}</a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-divider style="margin: 0 0 12px" />

      <a-row style="margin-bottom: 8px">
        <a-col :span="12">
          <a-space>
            <a-button status="danger" :disabled="selectedRowKeys.length===0" @click="handleBatchDelete">{{ $t('common.delete') }}</a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-table
        ref="tableRef"
        v-model:selected-keys="selectedRowKeys"
        row-key="id"
        :loading="loading"
        :data="records || []"
        :columns="columns"
        :pagination="pagination"
        :row-selection="rowSelection"
        :bordered="false"
        :size="'medium'"
        :stripe="true"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
      >
        <template #createTime="{ record }">
          <span>{{ formatTime(record.createTime) }}</span>
        </template>
        <template #operations="{ record }">
          <a-space>
            <a-button-group>
              <a-button
                type="text"
                size="mini"
                @click="handleDelete(record)"
              >
                <template #icon>
                  <icon-delete />
                </template>
                {{ $t('common.delete') }}
              </a-button>
            </a-button-group>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Message } from '@arco-design/web-vue'
import { pageAssistant, delAssistant } from '@/api/chat/assistant'

const { t } = useI18n()

const loading = ref(false)
const total = ref(0)
const records = ref<any[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const selectedRowKeys = ref<(number|string)[]>([])

const query = reactive({ current: 1, size: 10, title: '', typeId: '' })

const rowSelection = reactive({
  type: 'checkbox' as const,
  selectedRowKeys,
  onChange: (keys: (number|string)[]) => { selectedRowKeys.value = keys },
})

function fetchList() {
  loading.value = true
  const params: any = { current: pagination.current, size: pagination.pageSize, ...query }
  pageAssistant(params).then((res: any) => {
    const root = res && (res.data !== undefined ? res.data : res)
    const payload = root && (root.data !== undefined ? root.data : root) || {}
    const list = Array.isArray((payload as any).records) ? (payload as any).records : []
    records.value = list
    const pageTotal = typeof (payload as any).total === 'number' ? (payload as any).total : list.length
    total.value = pageTotal
    pagination.total = pageTotal
  }).finally(() => { loading.value = false })
}

function handleQuery() {
  query.current = 1
  fetchList()
}

function resetQuery() {
  Object.assign(query, { title: '', typeId: '' })
  handleQuery()
}

function onPageChange(page: number) { pagination.current = page; fetchList() }
function onPageSizeChange(size: number) { pagination.pageSize = size; pagination.current = 1; fetchList() }

function handleDelete(record: any) {
  const { id } = record
  delAssistant(id).then(() => {
    Message.success(t('biz.deleteSuccess'))
    fetchList()
  })
}

fetchList()

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) return
  Promise.all(selectedRowKeys.value.map(id => delAssistant(id))).then(() => {
    Message.success(t('biz.deleteSuccess'))
    selectedRowKeys.value = []
    fetchList()
  })
}

const columns = computed(() => [
  { title: t('biz.id'), dataIndex: 'id', align: 'center', width: 100 },
  { title: t('biz.name'), dataIndex: 'title', align: 'center' },
  { title: t('biz.icon'), dataIndex: 'icon', align: 'center' },
  { title: t('biz.description'), dataIndex: 'description', align: 'center' },
  { title: t('biz.sort'), dataIndex: 'sort', align: 'center' },
  { title: t('biz.createTime'), dataIndex: 'createTime', slotName: 'createTime', align: 'center' },
  {
    title: t('biz.operation'),
    dataIndex: 'operations',
    slotName: 'operations',
    align: 'center',
    width: 160,
  },
])

// 格式化时间
function formatTime(time: string | number) {
  if (!time) return '-'
  
  let timestamp: number
  
  if (typeof time === 'string') {
    // 如果是字符串，尝试解析
    // 检查是否是纯数字字符串（时间戳）
    if (/^\d+$/.test(time)) {
      timestamp = parseInt(time, 10)
      // 判断是否是秒级时间戳（10位数字，范围大约在 2001-01-01 到 2286-11-20 之间）
      // 秒级时间戳通常在 1000000000 到 9999999999 之间
      if (timestamp >= 1000000000 && timestamp <= 9999999999) {
        timestamp *= 1000
      }
    } else {
      // 尝试直接解析日期字符串
      const parsed = Date.parse(time)
      if (!Number.isNaN(parsed)) {
        timestamp = parsed
      } else {
        // 如果无法解析，直接返回原字符串
        return time
      }
    }
  } else {
    // 如果是数字，检查是否是秒级时间戳（10位）
    timestamp = time
    // 判断是否是秒级时间戳（10位数字）
    if (timestamp >= 1000000000 && timestamp <= 9999999999) {
      timestamp *= 1000
    }
  }
  
  // 转换为日期对象
  const date = new Date(timestamp)
  
  // 检查日期是否有效
  if (Number.isNaN(date.getTime())) {
    return '-'
  }
  
  // 格式化为 YYYY-MM-DD HH:mm:ss
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px;
  box-sizing: border-box;
}
</style>





