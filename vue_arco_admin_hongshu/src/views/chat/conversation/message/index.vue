<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['AI管理', '对话管理', '消息管理']"></Breadcrumb>
    <a-card
      class="general-card r-nav-card"
      :body-style="{ padding: '15px' }"
      :header-style="{ padding: '15px' }"
    >
      <a-row>
        <!-- 搜索区块 -->
        <a-col :flex="1">
          <a-form
            :model="state.queryParams"
            :label-col-props="{ span: 6 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="left"
            auto-label-width
          >
            <a-row :gutter="16">
              <a-col :span="6">
                <a-form-item field="role" :label="'对话角色'">
                  <a-select
                    v-model="state.queryParams.role"
                    placeholder="请选择对话角色"
                    allow-clear
                  >
                    <a-option value="user">用户</a-option>
                    <a-option value="assistant">助手</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="status" :label="'消息状态'">
                  <a-select
                    v-model="state.queryParams.status"
                    placeholder="请选择消息状态"
                    allow-clear
                  >
                    <a-option :value="2">成功</a-option>
                    <a-option :value="3">异常</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="model" :label="'使用模型'">
                  <a-input
                    v-model="state.queryParams.model"
                    :placeholder="'请输入模型名称'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="dateRange" :label="'创建时间'">
                  <a-range-picker
                    v-model="state.queryParams.dateRange"
                    style="width: 100%"
                    format="YYYY-MM-DD"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 84px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space direction="vertical" :size="18">
            <a-button type="primary" @click="handleQuery">
              <template #icon>
                <IconSearch />
              </template>
              查询
            </a-button>
            <a-button @click="resetQueryForm">
              <template #icon>
                <IconRefresh />
              </template>
              重置
            </a-button>
          </a-space>
        </a-col>
        <a-divider style="margin-top: 0" />
        
        <!-- 工具栏 -->
        <a-row style="margin-bottom: 16px">
          <a-col :span="12">
            <a-space>
              <a-button
                v-permission="['chat:message:remove']"
                status="danger"
                :disabled="selectedKeys.length === 0"
                @click="handleBatchDelete"
              >
                <template #icon>
                  <IconDelete />
                </template>
                批量删除
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-row>

      <!-- 表格数据-->
      <a-table
        row-key="id"
        :loading="loading"
        :columns="state.columns"
        :data="state.list"
        :pagination="pagination"
        :bordered="false"
        :row-selection="rowSelection"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
      >
        <template #role="{ record }">
          <a-tag v-if="record.role === 'assistant'" color="blue">
            <template #icon>
              <IconRobot />
            </template>
            助手
          </a-tag>
          <a-tag v-else-if="record.role === 'user'" color="orange">
            <template #icon>
              <IconUser />
            </template>
            用户
          </a-tag>
          <span v-else>{{ record.role || '-' }}</span>
        </template>
        <template #content="{ record }">
          <a-tooltip :content="record.content">
            <div class="text-ellipsis">{{ record.content || '-' }}</div>
          </a-tooltip>
        </template>
        <template #status="{ record }">
          <a-tag v-if="record.status === 2" color="green">
            <template #icon>
              <IconCheck />
            </template>
            成功
          </a-tag>
          <a-tag v-else-if="record.status === 3" color="red">
            <template #icon>
              <IconClose />
            </template>
            异常
          </a-tag>
          <a-tag v-else color="gray">-</a-tag>
        </template>
        <template #model="{ record }">
          <span>{{ record.model || '-' }}</span>
        </template>
        <template #modelVersion="{ record }">
          <span>{{ record.modelVersion || '-' }}</span>
        </template>
        <template #usedTokens="{ record }">
          <a-tag v-if="record.usedTokens" color="purple">
            {{ record.usedTokens }}
          </a-tag>
          <span v-else>-</span>
        </template>
        <template #createTime="{ record }">
          <span>{{ formatTime(record.createTime) }}</span>
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button-group>
              <a-button
                v-permission="['chat:message:remove']"
                type="text"
                size="mini"
                @click="handleDelete(record)"
              >
                <template #icon>
                  <IconDelete />
                </template>
                删除
              </a-button>
            </a-button-group>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconSearch,
  IconRefresh,
  IconDelete,
  IconRobot,
  IconUser,
  IconCheck,
  IconClose,
} from '@arco-design/web-vue/es/icon'
import Breadcrumb from '@/components/breadcrumb/index.vue'
import { pageChatMessage, delChatMessage } from '@/api/chat/message'

const route = useRoute()
const loading = ref(false)
const selectedKeys = ref<(string | number)[]>([])

// 表格行选择配置
const rowSelection = reactive({
  type: 'checkbox' as const,
  showCheckedAll: true,
  onlyCurrent: false,
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 响应式状态
const state = reactive({
  queryParams: {
    chatId: '',
    role: '',
    status: undefined as number | undefined,
    model: '',
    dateRange: [] as string[],
  },
  list: [] as any[],
  columns: [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
      align: 'center',
    },
    {
      title: '对话角色',
      dataIndex: 'role',
      slotName: 'role',
      width: 120,
      align: 'center',
    },
    {
      title: '消息内容',
      dataIndex: 'content',
      slotName: 'content',
      width: 400,
      align: 'center',
    },
    {
      title: '状态',
      dataIndex: 'status',
      slotName: 'status',
      width: 100,
      align: 'center',
    },
    {
      title: '使用模型',
      dataIndex: 'model',
      slotName: 'model',
      width: 120,
      align: 'center',
    },
    {
      title: '模型版本',
      dataIndex: 'modelVersion',
      slotName: 'modelVersion',
      width: 120,
      align: 'center',
    },
    {
      title: '消耗Token',
      dataIndex: 'usedTokens',
      slotName: 'usedTokens',
      width: 120,
      align: 'center',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      slotName: 'createTime',
      width: 160,
      align: 'center',
    },
    {
      title: '操作',
      dataIndex: 'optional',
      slotName: 'optional',
      fixed: 'right',
      width: 100,
      align: 'center',
    },
  ],
})

// 获取数据
function fetchData() {
  loading.value = true
  const params = {
    current: pagination.current,
    size: pagination.pageSize,
    ...state.queryParams,
  }

  // 处理日期范围
  if (state.queryParams.dateRange && state.queryParams.dateRange.length === 2) {
    const [beginTime, endTime] = state.queryParams.dateRange
    params.beginTime = beginTime
    params.endTime = endTime
    delete params.dateRange
  } else {
    delete params.dateRange
  }

  // 移除空值
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || params[key] === undefined) {
      delete params[key]
    }
  })

  pageChatMessage(params)
    .then((res: any) => {
      // 兼容多种数据结构
      const data = res?.data?.data || res?.data || res || {}
      const records = data.records || data.list || data.rows || []
      const total = data.total || records.length

      state.list = records
      pagination.total = total
    })
    .catch((error) => {
      console.error('获取消息列表失败:', error)
      Message.error('获取数据失败')
    })
    .finally(() => {
      loading.value = false
    })
}

// 查询
function handleQuery() {
  pagination.current = 1
  fetchData()
}

// 重置
function resetQueryForm() {
  state.queryParams = {
    chatId: route.query.chatId ? String(route.query.chatId) : '',
    role: '',
    status: undefined,
    model: '',
    dateRange: [],
  }
  handleQuery()
}

// 分页变化
function onPageChange(page: number) {
  pagination.current = page
  fetchData()
}

function onPageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.current = 1
  fetchData()
}

// 删除单条记录
function handleDelete(record: any) {
  Message.info({
    content: '确认删除该消息记录？',
    showCancel: true,
    onOk: () => {
      delChatMessage(record.id)
        .then(() => {
          Message.success('删除成功')
          fetchData()
        })
        .catch(() => {
          Message.error('删除失败')
        })
    },
  })
}

// 批量删除
function handleBatchDelete() {
  if (selectedKeys.value.length === 0) {
    Message.warning('请选择要删除的记录')
    return
  }

  Message.info({
    content: `确认删除选中的 ${selectedKeys.value.length} 条记录？`,
    showCancel: true,
    onOk: () => {
      Promise.all(selectedKeys.value.map((id) => delChatMessage(id)))
        .then(() => {
          Message.success('批量删除成功')
          selectedKeys.value = []
          fetchData()
        })
        .catch(() => {
          Message.error('批量删除失败')
        })
    },
  })
}

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

// 页面挂载时获取数据
onMounted(() => {
  // 从路由参数获取chatId
  if (route.query.chatId) {
    state.queryParams.chatId = String(route.query.chatId)
  }
  fetchData()
})
</script>

<style scoped>
.text-ellipsis {
  max-width: 360px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}
</style>