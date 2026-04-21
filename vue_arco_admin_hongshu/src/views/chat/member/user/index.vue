<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['AI管理', '会员管理', '会员列表']"></Breadcrumb>
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
                <a-form-item field="nickName" :label="'用户昵称'">
                  <a-input
                    v-model="state.queryParams.nickName"
                    :placeholder="'请输入用户昵称'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="tel" :label="'手机号码'">
                  <a-input
                    v-model="state.queryParams.tel"
                    :placeholder="'请输入手机号码'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="shareUserName" :label="'邀请人'">
                  <a-input
                    v-model="state.queryParams.shareUserName"
                    :placeholder="'请输入邀请人'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="type" :label="'注册类型'">
                  <a-select
                    v-model="state.queryParams.type"
                    placeholder="请选择注册类型"
                    allow-clear
                  >
                    <a-option value="1">微信小程序</a-option>
                    <a-option value="2">公众号</a-option>
                    <a-option value="3">手机号</a-option>
                    <a-option value="5">分销账号</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="vipType" :label="'会员类型'">
                  <a-select
                    v-model="state.queryParams.vipType"
                    placeholder="请选择会员类型"
                    allow-clear
                  >
                    <a-option value="0">普通用户</a-option>
                    <a-option value="1">普通会员</a-option>
                    <a-option value="2">尊享会员</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="status" :label="'会员状态'">
                  <a-select
                    v-model="state.queryParams.status"
                    placeholder="请选择会员状态"
                    allow-clear
                  >
                    <a-option value="0">禁用</a-option>
                    <a-option value="1">启用</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="dateRange" :label="'注册时间'">
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
        <a-divider style="height: 126px" direction="vertical" />
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
                v-permission="['chat:member:edit']"
                type="outline"
                :disabled="selectedKeys.length !== 1"
                @click="handleBatchEdit"
              >
                <template #icon>
                  <IconEdit />
                </template>
                编辑
              </a-button>
              <a-button
                v-permission="['chat:member:export']"
                type="outline"
                @click="handleExport"
              >
                <template #icon>
                  <IconDownload />
                </template>
                导出
              </a-button>
              <a-button
                v-permission="['chat:member:edit']"
                status="danger"
                :disabled="selectedKeys.length === 0"
                @click="handleBatchDisable"
              >
                <template #icon>
                  <IconStop />
                </template>
                禁用
              </a-button>
              <a-button
                v-permission="['chat:member:remove']"
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
        <template #avatar="{ record }">
          <a-avatar v-if="!record.avatar" :size="40">
            {{ (record.nickName && record.nickName[0]) || 'U' }}
          </a-avatar>
          <a-image
            v-else
            :src="buildAvatarSrc(record.avatar)"
            :width="40"
            :height="40"
            :preview="true"
            fit="cover"
            style="border-radius: 50%"
          />
        </template>
        <template #nickName="{ record }">
          <a-tooltip :content="record.nickName">
            <div class="text-ellipsis">{{ record.nickName || '-' }}</div>
          </a-tooltip>
        </template>
        <template #tel="{ record }">
          <span>{{ record.tel || '-' }}</span>
        </template>
        <template #openid="{ record }">
          <a-tooltip v-if="record.openid" :content="record.openid">
            <div class="text-ellipsis-short">{{ record.openid }}</div>
          </a-tooltip>
          <span v-else>-</span>
        </template>
        <template #ip="{ record }">
          <span>{{ record.ip || '-' }}</span>
        </template>
        <template #address="{ record }">
          <a-tooltip v-if="record.address" :content="record.address">
            <div class="text-ellipsis">{{ record.address }}</div>
          </a-tooltip>
          <span v-else>-</span>
        </template>
        <template #num="{ record }">
          <a-tag v-if="record.num" color="blue">
            {{ record.num }} 额度
          </a-tag>
          <span v-else>-</span>
        </template>
        <template #resetCount="{ record }">
          <a-tag v-if="record.resetCount" color="green">
            {{ record.resetCount }} 次
          </a-tag>
          <span v-else>-</span>
        </template>
        <template #shareName="{ record }">
          <span>{{ record.shareName || '-' }}</span>
        </template>
        <template #type="{ record }">
          <a-tag v-if="record.type === '1'" color="green">微信小程序</a-tag>
          <a-tag v-else-if="record.type === '2'" color="blue">公众号</a-tag>
          <a-tag v-else-if="record.type === '3'" color="orange">手机号</a-tag>
          <a-tag v-else-if="record.type === '5'" color="purple">分销账号</a-tag>
          <span v-else>{{ record.type || '-' }}</span>
        </template>
        <template #vipType="{ record }">
          <a-tag v-if="record.vipType === '0'" color="gray">普通用户</a-tag>
          <a-tag v-else-if="record.vipType === '1'" color="blue">普通会员</a-tag>
          <a-tag v-else-if="record.vipType === '2'" color="gold">尊享会员</a-tag>
          <span v-else>{{ record.vipType || '-' }}</span>
        </template>
        <template #status="{ record }">
          <a-tag v-if="record.status === '1'" color="green">
            <template #icon>
              <IconCheck />
            </template>
            启用
          </a-tag>
          <a-tag v-else color="red">
            <template #icon>
              <IconStop />
            </template>
            禁用
          </a-tag>
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
                v-permission="['chat:member:edit']"
                type="text"
                size="mini"
                @click="handleUpdate(record)"
              >
                <template #icon>
                  <IconEdit />
                </template>
                编辑
              </a-button>
              <a-button
                v-permission="['chat:member:remove']"
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

    <!-- 会员详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="会员详情"
      width="800px"
      :footer="false"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="头像">
          <a-avatar v-if="!currentRecord.avatar" :size="40">
            {{ (currentRecord.nickName && currentRecord.nickName[0]) || 'U' }}
          </a-avatar>
          <a-image
            v-else
            :src="buildAvatarSrc(currentRecord.avatar)"
            :width="40"
            :height="40"
            :preview="true"
            fit="cover"
            style="border-radius: 50%"
          />
        </a-descriptions-item>
        <a-descriptions-item label="昵称">
          {{ currentRecord.nickName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="手机号">
          {{ currentRecord.tel || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="微信标识">
          {{ currentRecord.openid || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="登录IP">
          {{ currentRecord.ip || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="登录地址">
          {{ currentRecord.address || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="总额度">
          <a-tag v-if="currentRecord.num" color="blue">
            {{ currentRecord.num }} 额度
          </a-tag>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="剩余赠送次数">
          <a-tag v-if="currentRecord.resetCount" color="green">
            {{ currentRecord.resetCount }} 次
          </a-tag>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="邀请人">
          {{ currentRecord.shareName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="注册类型">
          <a-tag v-if="currentRecord.type === '1'" color="green">微信小程序</a-tag>
          <a-tag v-else-if="currentRecord.type === '2'" color="blue">公众号</a-tag>
          <a-tag v-else-if="currentRecord.type === '3'" color="orange">手机号</a-tag>
          <a-tag v-else-if="currentRecord.type === '5'" color="purple">分销账号</a-tag>
          <span v-else>{{ currentRecord.type || '-' }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="会员类型">
          <a-tag v-if="currentRecord.vipType === '0'" color="gray">普通用户</a-tag>
          <a-tag v-else-if="currentRecord.vipType === '1'" color="blue">普通会员</a-tag>
          <a-tag v-else-if="currentRecord.vipType === '2'" color="gold">尊享会员</a-tag>
          <span v-else>{{ currentRecord.vipType || '-' }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="会员状态">
          <a-tag v-if="currentRecord.status === '1'" color="green">
            <template #icon><IconCheck /></template>
            启用
          </a-tag>
          <a-tag v-else color="red">
            <template #icon><IconStop /></template>
            禁用
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="注册时间">
          {{ formatTime(currentRecord.createTime) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 编辑会员弹窗 -->
    <a-modal
      v-model:visible="editVisible"
      title="编辑会员"
      width="600px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
      >
        <a-form-item label="昵称" field="nickName">
          <a-input v-model="formData.nickName" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item label="手机号" field="tel">
          <a-input v-model="formData.tel" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="会员类型" field="vipType">
          <a-select v-model="formData.vipType" placeholder="请选择会员类型">
            <a-option value="0">普通用户</a-option>
            <a-option value="1">普通会员</a-option>
            <a-option value="2">尊享会员</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="会员状态" field="status">
          <a-radio-group v-model="formData.status">
            <a-radio value="1">启用</a-radio>
            <a-radio value="0">禁用</a-radio>
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
  IconEdit,
  IconEye,
  IconDownload,
  IconStop,
  IconCheck,
} from '@arco-design/web-vue/es/icon'
import Breadcrumb from '@/components/breadcrumb/index.vue'
import { pageMember, delMember, updateMemberStatus, delAllMember } from '@/api/chat/member'

const loading = ref(false)
const selectedKeys = ref<(string | number)[]>([])
const detailVisible = ref(false)
const editVisible = ref(false)
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
  nickName: '',
  tel: '',
  vipType: '',
  status: '',
})

// 表单验证规则
const formRules = {
  nickName: [
    { required: true, message: '请输入昵称' },
  ],
  tel: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' },
  ],
}

// 响应式状态
const state = reactive({
  queryParams: {
    nickName: '',
    tel: '',
    shareUserName: '',
    type: '',
    vipType: '',
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
      title: '头像',
      dataIndex: 'avatar',
      slotName: 'avatar',
      width: 80,
      align: 'center',
    },
    {
      title: '昵称',
      dataIndex: 'nickName',
      slotName: 'nickName',
      width: 120,
      align: 'center',
    },
    {
      title: '手机号',
      dataIndex: 'tel',
      slotName: 'tel',
      width: 120,
      align: 'center',
    },
    {
      title: '微信标识',
      dataIndex: 'openid',
      slotName: 'openid',
      width: 150,
      align: 'center',
    },
    {
      title: '登录IP',
      dataIndex: 'ip',
      slotName: 'ip',
      width: 120,
      align: 'center',
    },
    {
      title: '登录地址',
      dataIndex: 'address',
      slotName: 'address',
      width: 150,
      align: 'center',
    },
    {
      title: '总额度',
      dataIndex: 'num',
      slotName: 'num',
      width: 100,
      align: 'center',
    },
    {
      title: '赠送次数',
      dataIndex: 'resetCount',
      slotName: 'resetCount',
      width: 100,
      align: 'center',
    },
    {
      title: '邀请人',
      dataIndex: 'shareName',
      slotName: 'shareName',
      width: 100,
      align: 'center',
    },
    {
      title: '注册类型',
      dataIndex: 'type',
      slotName: 'type',
      width: 120,
      align: 'center',
    },
    {
      title: '会员类型',
      dataIndex: 'vipType',
      slotName: 'vipType',
      width: 120,
      align: 'center',
    },
    {
      title: '状态',
      dataIndex: 'status',
      slotName: 'status',
      width: 80,
      align: 'center',
    },
    {
      title: '注册时间',
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

  pageMember(params)
    .then((res: any) => {
      // 兼容多种数据结构
      const data = res?.data?.data || res?.data || res || {}
      const records = data.records || data.list || data.rows || []
      const total = data.total || records.length

      state.list = records
      pagination.total = total
    })
    .catch((error) => {
      console.error('获取会员列表失败:', error)
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
    nickName: '',
    tel: '',
    shareUserName: '',
    type: '',
    vipType: '',
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

// 编辑
function handleUpdate(record: any) {
  Object.assign(formData, {
    id: record.id,
    nickName: record.nickName || '',
    tel: record.tel || '',
    vipType: record.vipType || '',
    status: record.status || '',
  })
  editVisible.value = true
}

// 批量编辑
function handleBatchEdit() {
  if (selectedKeys.value.length !== 1) {
    Message.warning('请选择一条记录进行编辑')
    return
  }
  const record = state.list.find(item => item.id === selectedKeys.value[0])
  if (record) {
    handleUpdate(record)
  }
}

// 导出
function handleExport() {
  Message.info('导出功能开发中...')
}

// 批量禁用
function handleBatchDisable() {
  if (selectedKeys.value.length === 0) {
    Message.warning('请选择要禁用的记录')
    return
  }

  Message.info({
    content: `确认禁用选中的 ${selectedKeys.value.length} 个用户？`,
    showCancel: true,
    onOk: () => {
      Promise.all(selectedKeys.value.map((id) => updateMemberStatus(id, 0)))
        .then(() => {
          Message.success('批量禁用成功')
          selectedKeys.value = []
          fetchData()
        })
        .catch(() => {
          Message.error('批量禁用失败')
        })
    },
  })
}

// 删除单条记录
function handleDelete(record: any) {
  Message.info({
    content: '确认删除该会员？',
    showCancel: true,
    onOk: () => {
      delMember(record.id)
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
      Promise.all(selectedKeys.value.map((id) => delMember(id)))
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
      // 这里应该调用更新接口，暂时只做状态更新
      updateMemberStatus(formData.id, formData.status)
        .then(() => {
          Message.success('修改成功')
          editVisible.value = false
          fetchData()
        })
        .catch(() => {
          Message.error('修改失败')
        })
    }
  })
}

// 取消表单
function handleCancel() {
  editVisible.value = false
  formRef.value?.resetFields()
}

// 构建头像URL
function buildAvatarSrc(url?: string) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  if (url.startsWith('//')) return `${window.location.protocol}${url}`
  if (url.startsWith('/')) return `${window.location.origin}${url}`
  return url
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
.text-ellipsis {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}

.text-ellipsis-short {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}
</style>