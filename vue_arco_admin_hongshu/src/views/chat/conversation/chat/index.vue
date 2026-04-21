<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb
      :items="[
        $t('menu.chat'),
        $t('menu.chat.conversation'),
        $t('menu.chat.conversation.chat'),
      ]"
    ></Breadcrumb>
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
                <a-form-item field="userName" :label="$t('biz.userName')">
                  <a-input
                    v-model="state.queryParams.userName"
                    :placeholder="$t('biz.pleaseInputUserName')"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="title" :label="$t('biz.chatTitle')">
                  <a-input
                    v-model="state.queryParams.title"
                    :placeholder="$t('biz.pleaseInputChatTitle')"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item
                  field="assistantTitle"
                  :label="$t('biz.assistantName')"
                >
                  <a-input
                    v-model="state.queryParams.assistantTitle"
                    :placeholder="$t('biz.pleaseInputAssistantName')"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="dateRange" :label="$t('biz.createTime')">
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
              {{ $t('biz.query') }}
            </a-button>
            <a-button @click="resetQueryForm">
              <template #icon>
                <IconRefresh />
              </template>
              {{ $t('common.reset') }}
            </a-button>
          </a-space>
        </a-col>
        <a-divider style="margin-top: 0" />

        <!-- 工具栏 -->
        <a-row style="margin-bottom: 16px">
          <a-col :span="12">
            <a-space>
              <a-button
                v-permission="['chat:conversation:remove']"
                status="danger"
                :disabled="selectedKeys.length === 0"
                @click="handleBatchDelete"
              >
                <template #icon>
                  <IconDelete />
                </template>
                {{ $t('biz.batchDelete') }}
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-row>

      <!-- 表格数据-->
      <a-table
        row-key="id"
        :loading="loading"
        :columns="columns"
        :data="state.list"
        :pagination="pagination"
        :bordered="false"
        :row-selection="rowSelection"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
      >
        <template #userName="{ record }">
          <span>{{ record.userName || '-' }}</span>
        </template>
        <template #assistantTitle="{ record }">
          <span>{{
            record.assistantTitle || $t('biz.defaultAssistant')
          }}</span>
        </template>
        <template #title="{ record }">
          <a-tooltip :content="record.title">
            <div class="text-ellipsis">{{ record.title || '-' }}</div>
          </a-tooltip>
        </template>
        <template #chatNumber="{ record }">
          <span>{{ record.chatNumber || '-' }}</span>
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
                {{ $t('biz.view') }}
              </a-button>
              <a-button
                v-permission="['chat:conversation:remove']"
                type="text"
                size="mini"
                @click="handleDelete(record)"
              >
                <template #icon>
                  <IconDelete />
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
import { ref, reactive, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconSearch,
  IconRefresh,
  IconDelete,
  IconEye,
} from '@arco-design/web-vue/es/icon';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import { pageChat, delChat } from '@/api/chat/chat';

const router = useRouter();
const { t } = useI18n();
const loading = ref(false);
const selectedKeys = ref<(string | number)[]>([]);

// 表格行选择配置
const rowSelection = reactive({
  type: 'checkbox' as const,
  showCheckedAll: true,
  onlyCurrent: false,
});

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 响应式状态
const state = reactive({
  queryParams: {
    userName: '',
    title: '',
    assistantTitle: '',
    dateRange: [] as string[],
  },
  list: [] as any[],
});

const columns = computed(() => [
  {
    title: t('biz.id'),
    dataIndex: 'id',
    width: 80,
    align: 'center',
  },
  {
    title: t('biz.userName'),
    dataIndex: 'userName',
    slotName: 'userName',
    width: 120,
    align: 'center',
  },
  {
    title: t('biz.assistantName'),
    dataIndex: 'assistantTitle',
    slotName: 'assistantTitle',
    width: 150,
    align: 'center',
  },
  {
    title: t('biz.chatTitle'),
    dataIndex: 'title',
    slotName: 'title',
    width: 300,
    align: 'center',
  },
  {
    title: t('biz.createTime'),
    dataIndex: 'createTime',
    slotName: 'createTime',
    width: 160,
    align: 'center',
  },
  {
    title: t('biz.operation'),
    dataIndex: 'optional',
    slotName: 'optional',
    fixed: 'right',
    width: 150,
    align: 'center',
  },
]);

// 获取数据
function fetchData() {
  loading.value = true;
  const params = {
    current: pagination.current,
    size: pagination.pageSize,
    ...state.queryParams,
  };

  // 处理日期范围
  if (state.queryParams.dateRange && state.queryParams.dateRange.length === 2) {
    const [beginTime, endTime] = state.queryParams.dateRange;
    params.beginTime = beginTime;
    params.endTime = endTime;
    delete params.dateRange;
  } else {
    delete params.dateRange;
  }

  // 移除空值
  Object.keys(params).forEach((key) => {
    if (
      params[key] === '' ||
      params[key] === null ||
      params[key] === undefined
    ) {
      delete params[key];
    }
  });

  pageChat(params)
    .then((res: any) => {
      // 兼容多种数据结构
      const data = res?.data?.data || res?.data || res || {};
      const records = data.records || data.list || data.rows || [];
      const total = data.total || records.length;

      state.list = records;
      pagination.total = total;
    })
    .catch((error) => {
      console.error('获取聊天列表失败:', error);
      Message.error(t('biz.fetchDataFailed'));
    })
    .finally(() => {
      loading.value = false;
    });
}

// 查询
function handleQuery() {
  pagination.current = 1;
  fetchData();
}

// 重置
function resetQueryForm() {
  state.queryParams = {
    userName: '',
    title: '',
    assistantTitle: '',
    dateRange: [],
  };
  handleQuery();
}

// 分页变化
function onPageChange(page: number) {
  pagination.current = page;
  fetchData();
}

function onPageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize;
  pagination.current = 1;
  fetchData();
}

// 查看详情
function handleView(record: any) {
  router.push({
    path: '/chat/conversation/message',
    query: { chatId: record.id },
  });
}

// 删除单条记录
function handleDelete(record: any) {
  Message.info({
    content: t('biz.confirmDeleteChat'),
    showCancel: true,
    onOk: () => {
      delChat(record.id)
        .then(() => {
          Message.success(t('biz.deleteSuccess'));
          fetchData();
        })
        .catch(() => {
          Message.error(t('biz.deleteFailed'));
        });
    },
  });
}

// 批量删除
function handleBatchDelete() {
  if (selectedKeys.value.length === 0) {
    Message.warning(t('biz.pleaseSelectRecordsToDelete'));
    return;
  }

  Message.info({
    content: t('biz.confirmDeleteSelected', {
      count: selectedKeys.value.length,
    }),
    showCancel: true,
    onOk: () => {
      Promise.all(selectedKeys.value.map((id) => delChat(id)))
        .then(() => {
          Message.success(t('biz.batchDeleteSuccess'));
          selectedKeys.value = [];
          fetchData();
        })
        .catch(() => {
          Message.error(t('biz.batchDeleteFailed'));
        });
    },
  });
}

// 格式化时间
function formatTime(time: string | number) {
  if (!time) return '-';

  let timestamp: number;

  if (typeof time === 'string') {
    // 如果是字符串，尝试解析
    // 检查是否是纯数字字符串（时间戳）
    if (/^\d+$/.test(time)) {
      timestamp = parseInt(time, 10);
      // 判断是否是秒级时间戳（10位数字，范围大约在 2001-01-01 到 2286-11-20 之间）
      // 秒级时间戳通常在 1000000000 到 9999999999 之间
      if (timestamp >= 1000000000 && timestamp <= 9999999999) {
        timestamp *= 1000;
      }
    } else {
      // 尝试直接解析日期字符串
      const parsed = Date.parse(time);
      if (!Number.isNaN(parsed)) {
        timestamp = parsed;
      } else {
        // 如果无法解析，直接返回原字符串
        return time;
      }
    }
  } else {
    // 如果是数字，检查是否是秒级时间戳（10位）
    timestamp = time;
    // 判断是否是秒级时间戳（10位数字）
    if (timestamp >= 1000000000 && timestamp <= 9999999999) {
      timestamp *= 1000;
    }
  }

  // 转换为日期对象
  const date = new Date(timestamp);

  // 检查日期是否有效
  if (Number.isNaN(date.getTime())) {
    return '-';
  }

  // 格式化为 YYYY-MM-DD HH:mm:ss
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

// 页面挂载时获取数据
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.text-ellipsis {
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}
</style>