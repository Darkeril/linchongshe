<template>
  <div class="container">
    <Breadcrumb :items="['会员中心', '用户审核']"></Breadcrumb>
    <a-card
      class="general-card r-nav-card"
      :body-style="{ padding: '15px' }"
      :header-style="{ padding: '15px' }"
    >
      <a-row style="margin-bottom: 16px">
        <a-col :span="24">
          <a-space>
            <a-input
              v-model="queryParams.phone"
              placeholder="手机号"
              allow-clear
              style="width: 200px"
              @press-enter="fetchList"
            />
            <a-input
              v-model="queryParams.username"
              placeholder="会员名称"
              allow-clear
              style="width: 200px"
              @press-enter="fetchList"
            />
            <a-button type="primary" @click="fetchList">
              <template #icon><icon-search /></template>
              查询
            </a-button>
            <a-button @click="resetQuery">
              <template #icon><icon-refresh /></template>
              重置
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-table
        row-key="id"
        :loading="loading"
        :pagination="pagination"
        :columns="columns"
        :data="list"
        :bordered="false"
        :scroll="{ x: 1100 }"
        size="medium"
        @page-change="onPageChange"
      >
        <template #fromType="{ record }">
          <a-tag :color="getFromTypeColor(record.fromType)">
            {{ getFromTypeLabel(record.fromType) || '未知来源' }}
          </a-tag>
        </template>
        <template #status="{ record }">
          <dict-tag :options="statusOptions" :value="record.status" />
        </template>
        <template #createTime="{ record }">
          {{ formatDateTime(record.createTime) }}
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button
              v-permission="['web:user:audit']"
              type="primary"
              size="small"
              @click="handleApprove(record)"
            >
              通过
            </a-button>
            <a-button
              v-permission="['web:user:audit']"
              status="danger"
              size="small"
              @click="handleReject(record)"
            >
              拒绝
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted } from 'vue';
import { Message, Modal } from '@arco-design/web-vue';
import {
  listMember,
  approveMemberAudit,
  rejectMemberAudit,
} from '@/api/web/member';
import { DictDataRecord, getDicts } from '@/api/system/dict-data';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import DictTag from '@/components/dict-tag/index.vue';

const loading = ref(false);
const list = ref<any[]>([]);
const statusOptions = ref<DictDataRecord[]>([]);
const fromTypeOptions = ref<DictDataRecord[]>([]);

const queryParams = reactive({
  phone: undefined as string | undefined,
  username: undefined as string | undefined,
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
});

const columns = [
  {
    title: '主键ID',
    dataIndex: 'id',
    ellipsis: true,
    tooltip: true,
    width: 100,
    align: 'center',
  },

  {
    title: '会员名称',
    dataIndex: 'username',
    ellipsis: true,
    tooltip: true,
    width: 100,
    align: 'center',
  },
  {
    title: '来因号',
    dataIndex: 'hsId',
    width: 100,
    ellipsis: true,
    tooltip: true,
    align: 'center',
  },
  { title: '手机号', dataIndex: 'phone', width: 100, align: 'center' },
  {
    title: '会员来源',
    dataIndex: 'fromType',
    slotName: 'fromType',
    width: 100,
    align: 'center' as const,
  },
  {
    title: '注册IP',
    dataIndex: 'loginIp',
    width: 100,
    ellipsis: true,
    tooltip: true,
    align: 'center',
  },
  {
    title: '登录地点',
    dataIndex: 'address',
    width: 100,
    ellipsis: true,
    tooltip: true,
    align: 'center',
  },
  {
    title: '状态',
    dataIndex: 'status',
    slotName: 'status',
    width: 100,
    align: 'center' as const,
  },
  {
    title: '注册时间',
    dataIndex: 'createTime',
    slotName: 'createTime',
    width: 140,
    align: 'center' as const,
  },
  {
    title: '操作',
    slotName: 'optional',
    width: 140,
    fixed: 'right' as const,
    align: 'center' as const,
  },
];

/** 后端可能返回时间戳数字、ISO 字符串或 Date */
const formatDateTime = (val: unknown) => {
  if (val === undefined || val === null || val === '') return '-';
  const date = new Date(val as string | number | Date);
  if (Number.isNaN(date.getTime())) return '-';
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
};

const getFromTypeLabel = (fromType: string | number | undefined | null) => {
  if (fromType === undefined || fromType === null) return '';
  const option = fromTypeOptions.value.find(
    (item) => String(item.dictValue) === String(fromType)
  );
  return option ? option.dictLabel : '';
};

const getFromTypeColor = (fromType: string | number | undefined | null) => {
  if (fromType === undefined || fromType === null) return 'gray';
  if (fromType === 0 || fromType === '0') return 'blue';
  if (fromType === 1 || fromType === '1') return 'green';
  if (fromType === 2 || fromType === '2') return 'orange';
  return 'gray';
};

const fetchList = async () => {
  loading.value = true;
  try {
    const res: any = await listMember({
      status: '2',
      phone: queryParams.phone,
      username: queryParams.username,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    });
    const rows = res.rows ?? res.data?.rows;
    const total = res.total ?? res.data?.total;
    list.value = rows || [];
    pagination.total = total ?? 0;
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  queryParams.phone = undefined;
  queryParams.username = undefined;
  pagination.current = 1;
  fetchList();
};

const onPageChange = (current: number) => {
  pagination.current = current;
  fetchList();
};

const handleApprove = (record: any) => {
  Modal.confirm({
    title: '确认通过',
    content: `确定通过用户「${record.username || record.phone}」的注册审核吗？`,
    onOk: async () => {
      await approveMemberAudit(record.id);
      Message.success('已通过');
      fetchList();
    },
  });
};

const handleReject = (record: any) => {
  Modal.confirm({
    title: '确认拒绝',
    content: `拒绝后该账号将被禁用，确定拒绝「${
      record.username || record.phone
    }」吗？`,
    onOk: async () => {
      await rejectMemberAudit(record.id);
      Message.success('已拒绝');
      fetchList();
    },
  });
};

onMounted(async () => {
  try {
    const [statusResp, fromTypeResp] = await Promise.all([
      getDicts('web_member_status').catch(() => getDicts('sys_normal_disable')),
      getDicts('from_type').catch(() => ({ data: [] as DictDataRecord[] })),
    ]);
    statusOptions.value = statusResp.data || [];
    fromTypeOptions.value = fromTypeResp.data || [];
  } catch {
    const { data } = await getDicts('sys_normal_disable');
    statusOptions.value = data;
  }
  fetchList();
});
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px;
}
</style>
