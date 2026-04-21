<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['AI管理', '订单管理', '兑换码管理']"></Breadcrumb>
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
                <a-form-item field="code" :label="'兑换码'">
                  <a-input
                    v-model="state.queryParams.code"
                    :placeholder="'请输入兑换码'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="userName" :label="'兑换人'">
                  <a-input
                    v-model="state.queryParams.userName"
                    :placeholder="'请输入兑换人'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="status" :label="'兑换状态'">
                  <a-select
                    v-model="state.queryParams.status"
                    placeholder="请选择兑换状态"
                    allow-clear
                  >
                    <a-option value="0">未兑换</a-option>
                    <a-option value="1">已兑换</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="dateRange" :label="'兑换时间'">
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
                v-permission="['chat:redemption:add']"
                type="primary"
                @click="handleAdd"
              >
                <template #icon>
                  <IconPlus />
                </template>
                新建
              </a-button>
              <a-button
                v-permission="['chat:redemption:remove']"
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
        <template #code="{ record }">
          <a-typography-text copyable>{{ record.code || '-' }}</a-typography-text>
        </template>
        <template #num="{ record }">
          <a-tag v-if="record.num" color="purple">
            {{ record.num }} 电量
          </a-tag>
          <span v-else>-</span>
        </template>
        <template #userName="{ record }">
          <span>{{ record.userName || '-' }}</span>
        </template>
        <template #status="{ record }">
          <a-tag v-if="record.status === 1" color="green">
            <template #icon>
              <IconCheck />
            </template>
            已兑换
          </a-tag>
          <a-tag v-else color="orange">
            <template #icon>
              <IconClockCircle />
            </template>
            未兑换
          </a-tag>
        </template>
        <template #receiveTime="{ record }">
          <span>{{ formatTime(record.receiveTime) }}</span>
        </template>
        <template #createTime="{ record }">
          <span>{{ formatTime(record.createTime) }}</span>
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button-group>
              <a-button type="text" size="mini" @click="handleView(record)">
                <template #icon>
                  <IconEye />
                </template>
                查看
              </a-button>
              <a-button
                v-permission="['chat:redemption:edit']"
                type="text"
                size="mini"
                @click="handleUpdate(record)"
              >
                <template #icon>
                  <IconEdit />
                </template>
                修改
              </a-button>
              <a-button
                v-permission="['chat:redemption:remove']"
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

    <!-- 兑换码详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="兑换码详情"
      width="600px"
      :footer="false"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="兑换码">
          <a-typography-text copyable>{{ currentRecord.code }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="兑换电量">
          <a-tag color="purple">{{ currentRecord.num }} 电量</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="兑换人">
          {{ currentRecord.userName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="兑换状态">
          <a-tag v-if="currentRecord.status === 1" color="green">
            <template #icon><IconCheck /></template>
            已兑换
          </a-tag>
          <a-tag v-else color="orange">
            <template #icon><IconClockCircle /></template>
            未兑换
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="兑换时间">
          {{ formatTime(currentRecord.receiveTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ formatTime(currentRecord.createTime) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 新增/编辑兑换码弹窗 -->
    <a-modal
      v-model:visible="formVisible"
      :title="formTitle"
      width="500px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
      >
        <a-form-item label="兑换码" field="code">
          <a-input v-model="formData.code" placeholder="请输入兑换码" />
        </a-form-item>
        <a-form-item label="兑换电量" field="num">
          <a-input-number
            v-model="formData.num"
            :min="1"
            :max="999999"
            placeholder="请输入兑换电量"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="兑换人" field="userName">
          <a-input v-model="formData.userName" placeholder="请输入兑换人" />
        </a-form-item>
        <a-form-item label="兑换状态" field="status">
          <a-radio-group v-model="formData.status">
            <a-radio :value="0">未兑换</a-radio>
            <a-radio :value="1">已兑换</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import {
  IconSearch,
  IconRefresh,
  IconDelete,
  IconPlus,
  IconEye,
  IconEdit,
  IconCheck,
  IconClockCircle,
} from '@arco-design/web-vue/es/icon'
import Breadcrumb from '@/components/breadcrumb/index.vue'
import { pageRedemption, delRedemption, addRedemption, updateRedemption, getRedemption } from '@/api/chat/redemption'

const loading = ref(false)
const selectedKeys = ref<(string | number)[]>([])
const detailVisible = ref(false)
const formVisible = ref(false)
const formTitle = ref('')
const currentRecord = ref<any>({})
const formRef = ref()

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

// 表单数据
const formData = reactive({
  id: null,
  code: '',
  num: null,
  userName: '',
  status: 0,
})

// 表单验证规则
const formRules = {
  code: [
    { required: true, message: '请输入兑换码' },
    { min: 6, max: 32, message: '兑换码长度为6-32位' },
  ],
  num: [
    { required: true, message: '请输入兑换电量' },
    { type: 'number', min: 1, message: '兑换电量至少为1' },
  ],
}

// 响应式状态
const state = reactive({
  queryParams: {
    code: '',
    userName: '',
    status: '',
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
      title: '兑换码',
      dataIndex: 'code',
      slotName: 'code',
      width: 200,
      align: 'center',
    },
    {
      title: '兑换电量',
      dataIndex: 'num',
      slotName: 'num',
      width: 120,
      align: 'center',
    },
    {
      title: '兑换人',
      dataIndex: 'userName',
      slotName: 'userName',
      width: 120,
      align: 'center',
    },
    {
      title: '兑换状态',
      dataIndex: 'status',
      slotName: 'status',
      width: 120,
      align: 'center',
    },
    {
      title: '兑换时间',
      dataIndex: 'receiveTime',
      slotName: 'receiveTime',
      width: 160,
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
      width: 180,
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

  pageRedemption(params)
    .then((res: any) => {
      // 兼容多种数据结构
      const data = res?.data?.data || res?.data || res || {}
      const records = data.records || data.list || data.rows || []
      const total = data.total || records.length

      state.list = records
      pagination.total = total
    })
    .catch((error) => {
      console.error('获取兑换码列表失败:', error)
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
    code: '',
    userName: '',
    status: '',
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

// 查看详情
function handleView(record: any) {
  currentRecord.value = { ...record }
  detailVisible.value = true
}

// 新增
function handleAdd() {
  Object.assign(formData, {
    id: null,
    code: '',
    num: null,
    userName: '',
    status: 0,
  })
  formTitle.value = '新增兑换码'
  formVisible.value = true
}

// 编辑
function handleUpdate(record: any) {
  if (record.id) {
    getRedemption(record.id)
      .then((res: any) => {
        const data = res?.data || res || {}
        Object.assign(formData, {
          id: data.id,
          code: data.code || '',
          num: data.num,
          userName: data.userName || '',
          status: data.status || 0,
        })
        formTitle.value = '编辑兑换码'
        formVisible.value = true
      })
      .catch(() => {
        Message.error('获取兑换码详情失败')
      })
  }
}

// 删除单条记录
function handleDelete(record: any) {
  Message.info({
    content: '确认删除该兑换码？',
    showCancel: true,
    onOk: () => {
      delRedemption(record.id)
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
      Promise.all(selectedKeys.value.map((id) => delRedemption(id)))
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

// 提交表单
function handleSubmit() {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      const submitData = { ...formData }
      const apiCall = submitData.id ? updateRedemption : addRedemption
      
      apiCall(submitData)
        .then(() => {
          Message.success(submitData.id ? '修改成功' : '新增成功')
          formVisible.value = false
          fetchData()
        })
        .catch(() => {
          Message.error(submitData.id ? '修改失败' : '新增失败')
        })
    }
  })
}

// 取消表单
function handleCancel() {
  formVisible.value = false
  formRef.value?.resetFields()
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
  fetchData()
})
</script>

<style scoped>
</style>