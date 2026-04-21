<template>
  <div class="container">
    <Breadcrumb :items="['IM管理', '消息管理', '私聊消息']" />
    <a-card
      class="general-card r-nav-card"
      :bordered="false"
      :body-style="{ padding: '15px' }"
      :header-style="{ padding: '15px' }"
    >
      <a-row style="margin-bottom: 16px">
        <a-col :flex="1">
          <a-form :model="queryParams" layout="inline">
            <a-form-item label="内容">
              <a-input
                v-model="queryParams.content"
                placeholder="消息内容"
                allow-clear
                style="width: 160px"
              />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleQuery">
                  <template #icon><icon-search /></template>
                  查询
                </a-button>
                <a-button @click="resetQuery">
                  <template #icon><icon-refresh /></template>
                  重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-col>
      </a-row>
      <a-table
        :loading="loading"
        :data="messageList"
        :columns="columns"
        :pagination="pagination"
        row-key="id"
        @page-change="onPageChange"
      >
        <template #sendUser="{ record }">
          <a-space :size="8">
            <a-avatar
              :size="32"
              shape="circle"
              :image-url="
                record.sendHeadImageThumb || record.sendHeadImage || undefined
              "
            >
              {{ (record.sendUserName || '?').slice(0, 1) }}
            </a-avatar>
            <span>{{ record.sendUserName || '—' }}</span>
          </a-space>
        </template>
        <template #recvUser="{ record }">
          <a-space :size="8">
            <a-avatar
              :size="32"
              shape="circle"
              :image-url="
                record.recvHeadImageThumb || record.recvHeadImage || undefined
              "
            >
              {{ (record.recvUserName || '?').slice(0, 1) }}
            </a-avatar>
            <span>{{ record.recvUserName || '—' }}</span>
          </a-space>
        </template>
        <template #type="{ record }">
          {{ messageTypeText(record.type) }}
        </template>
        <template #sendTime="{ record }">
          {{ formatTime(record.sendTime) }}
        </template>
        <template #action="{ record }">
          <a-button type="text" size="small" @click="handleDetail(record)"
            >详情</a-button
          >
        </template>
      </a-table>
    </a-card>
    <a-modal
      v-model:visible="detailVisible"
      title="消息详情"
      :footer="false"
      width="500px"
    >
      <a-descriptions :column="1" bordered v-if="detailRecord">
        <a-descriptions-item label="发送用户">
          <a-space :size="8">
            <a-avatar
              :size="36"
              shape="circle"
              :image-url="
                detailRecord.sendHeadImageThumb ||
                detailRecord.sendHeadImage ||
                undefined
              "
            >
              {{ (detailRecord.sendUserName || '?').slice(0, 1) }}
            </a-avatar>
            <span>{{ detailRecord.sendUserName || '—' }}</span>
          </a-space>
        </a-descriptions-item>
        <a-descriptions-item label="接收用户">
          <a-space :size="8">
            <a-avatar
              :size="36"
              shape="circle"
              :image-url="
                detailRecord.recvHeadImageThumb ||
                detailRecord.recvHeadImage ||
                undefined
              "
            >
              {{ (detailRecord.recvUserName || '?').slice(0, 1) }}
            </a-avatar>
            <span>{{ detailRecord.recvUserName || '—' }}</span>
          </a-space>
        </a-descriptions-item>
        <a-descriptions-item label="内容">{{
          detailRecord.content || '-'
        }}</a-descriptions-item>
        <a-descriptions-item label="消息类型">{{
          messageTypeText(detailRecord.type)
        }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{
          detailRecord.status === 1 ? '已读' : '未读'
        }}</a-descriptions-item>
        <a-descriptions-item label="发送时间">{{
          formatTime(detailRecord.sendTime)
        }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import useLoading from '@/hooks/loading';
import {
  getImPrivateMessageList,
  getImPrivateMessageInfo,
} from '@/api/im/privateMessage';
import type { ImPrivateMessageVo } from '@/api/im/privateMessage';

const { loading, setLoading } = useLoading(false);

const columns: TableColumnData[] = [
  {
    title: '编号',
    dataIndex: 'id',
    slotName: 'id',
    width: 60,
    align: 'center',
  },
  {
    title: '发送用户',
    dataIndex: 'sendUserName',
    slotName: 'sendUser',
    width: 160,
    align: 'center',
  },
  {
    title: '接收用户',
    dataIndex: 'recvUserName',
    slotName: 'recvUser',
    width: 160,
    align: 'center',
  },
  {
    title: '发送内容',
    dataIndex: 'content',
    align: 'center',
    ellipsis: true,
    tooltip: true,
    width: 200,
  },
  {
    title: '消息类型',
    dataIndex: 'type',
    slotName: 'type',
    align: 'center',
    width: 100,
  },
  {
    title: '发送时间',
    dataIndex: 'sendTime',
    slotName: 'sendTime',
    align: 'center',
    width: 100,
  },
  { title: '操作', slotName: 'action', align: 'center', width: 100 },
];

const queryParams = reactive({
  content: '',
  pageNum: 1,
  pageSize: 10,
});

const messageList = ref<ImPrivateMessageVo[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
});

const detailVisible = ref(false);
const detailRecord = ref<ImPrivateMessageVo | null>(null);

function formatTime(t: string | undefined) {
  if (!t) return '-';
  return new Date(t).toLocaleString('zh-CN', { hour12: false });
}

function messageTypeText(type: number | undefined) {
  const map: Record<number, string> = { 0: '文字', 1: '图片', 2: '文件' };
  return type !== undefined && type !== null ? map[type] ?? `类型${type}` : '-';
}

const fetchList = async () => {
  setLoading(true);
  try {
    const res = (await getImPrivateMessageList({
      content: queryParams.content || undefined,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
    })) as {
      rows?: ImPrivateMessageVo[];
      total?: number;
      data?: { rows?: ImPrivateMessageVo[]; total?: number };
    };
    const payload = res?.rows ? res : res?.data ?? {};
    const rows = payload?.rows ?? [];
    messageList.value = Array.isArray(rows) ? rows : [];
    pagination.total = payload?.total ?? 0;
  } catch (e) {
    Message.error('获取私聊消息列表失败');
    messageList.value = [];
    pagination.total = 0;
  } finally {
    setLoading(false);
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  pagination.current = 1;
  fetchList();
};

const resetQuery = () => {
  queryParams.content = '';
  handleQuery();
};

const onPageChange = (page: number, pageSize?: number) => {
  queryParams.pageNum = page;
  pagination.current = page;
  if (pageSize !== undefined) {
    queryParams.pageSize = pageSize;
    pagination.pageSize = pageSize;
  }
  fetchList();
};

const handleDetail = async (record: ImPrivateMessageVo) => {
  try {
    const res = (await getImPrivateMessageInfo(record.id)) as {
      data?: ImPrivateMessageVo;
    };
    detailRecord.value = res?.data ?? res;
    detailVisible.value = true;
  } catch (e) {
    Message.error('获取详情失败');
  }
};

onMounted(() => {
  fetchList();
});
</script>

<style scoped lang="less">
.container {
  padding: 16px 20px 20px;
  box-sizing: border-box;
}

.general-card.r-nav-card {
  margin-top: 16px;
}
</style>
