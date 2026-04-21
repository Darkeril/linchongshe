<template>
  <div class="container">
    <Breadcrumb :items="['IM管理', '用户管理']" />
    <a-card
      class="general-card r-nav-card"
      :bordered="false"
      :body-style="{ padding: '15px' }"
      :header-style="{ padding: '15px' }"
    >
      <a-row style="margin-bottom: 16px">
        <a-col :flex="1">
          <a-form :model="queryParams" layout="inline">
            <a-form-item label="用户名">
              <a-input
                v-model="queryParams.userName"
                placeholder="请输入用户名"
                allow-clear
                style="width: 160px"
              />
            </a-form-item>
            <a-form-item label="用户昵称">
              <a-input
                v-model="queryParams.nickName"
                placeholder="请输入用户昵称"
                allow-clear
                style="width: 160px"
              />
            </a-form-item>
            <a-form-item label="活跃范围">
              <a-select
                v-model="queryParams.activeRange"
                placeholder="全部"
                allow-clear
                style="width: 140px"
              >
                <a-option value="daily">日活(1天)</a-option>
                <a-option value="monthly">月活(30天)</a-option>
              </a-select>
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
                <!-- <a-button @click="handleExport">
                  <template #icon><icon-download /></template>
                  导出
                </a-button> -->
              </a-space>
            </a-form-item>
          </a-form>
        </a-col>
      </a-row>
      <a-table
        :loading="loading"
        :data="userList"
        :columns="columns"
        v-model:selected-keys="selectedKeys"
        :row-selection="rowSelection"
        :pagination="pagination"
        row-key="id"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
        @selection-change="onSelectionChange"
      >
        <template #avatar="{ record }">
          <a-avatar
            :size="36"
            :image-url="record.headImageThumb || record.headImage"
          >
            {{ (record.nickName || record.userName || '?').slice(0, 1) }}
          </a-avatar>
        </template>
        <template #phone="{ record }">
          <span :title="record.phone || ''">{{
            record.phone && String(record.phone).trim() ? record.phone : '—'
          }}</span>
        </template>
        <template #sex="{ record }">
          <a-tag v-if="isMaleSex(record.sex)" color="arcoblue">男</a-tag>
          <a-tag v-else-if="isFemaleSex(record.sex)" color="magenta">女</a-tag>
          <a-tag v-else color="gray">未知</a-tag>
        </template>
        <template #isBanned="{ record }">
          <a-tag v-if="record.isBanned" color="red">已封禁</a-tag>
          <a-tag v-else color="green">正常</a-tag>
        </template>
        <template #createdTime="{ record }">
          {{ formatTime(record.createdTime) }}
        </template>
        <template #action="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="handleDetail(record)"
              >详情</a-button
            >
            <a-button
              v-if="record.isBanned"
              type="text"
              size="small"
              status="danger"
              @click="handleUnban(record)"
              >解封</a-button
            >
            <a-button
              v-else
              type="text"
              size="small"
              status="danger"
              @click="handleBan(record)"
              >封禁</a-button
            >
          </a-space>
        </template>
      </a-table>
    </a-card>
    <!-- 详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="用户详情"
      :footer="false"
      width="600px"
    >
      <a-descriptions v-if="detailUser" :column="1" bordered>
        <a-descriptions-item label="头像">
          <a-avatar
            :size="48"
            :image-url="detailUser.headImageThumb || detailUser.headImage"
          >
            {{
              (detailUser.nickName || detailUser.userName || '?').slice(0, 1)
            }}
          </a-avatar>
        </a-descriptions-item>
        <a-descriptions-item label="用户名">{{
          detailUser.userName
        }}</a-descriptions-item>
        <a-descriptions-item label="用户昵称">{{
          detailUser.nickName
        }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{
          detailUser.phone && String(detailUser.phone).trim()
            ? detailUser.phone
            : '—'
        }}</a-descriptions-item>
        <a-descriptions-item label="因讯号">{{
          detailUser.hsId ?? '-'
        }}</a-descriptions-item>
        <a-descriptions-item label="性别">
          <a-tag v-if="isMaleSex(detailUser.sex)" color="arcoblue">男</a-tag>
          <a-tag v-else-if="isFemaleSex(detailUser.sex)" color="magenta"
            >女</a-tag
          >
          <a-tag v-else color="gray">未知</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="个性签名">{{
          detailUser.signature || '-'
        }}</a-descriptions-item>
        <a-descriptions-item label="是否封禁">{{
          detailUser.isBanned ? '是' : '否'
        }}</a-descriptions-item>
        <a-descriptions-item v-if="detailUser.isBanned" label="封禁原因">{{
          detailUser.reason || '-'
        }}</a-descriptions-item>
        <a-descriptions-item label="注册时间">{{
          formatTime(detailUser.createdTime)
        }}</a-descriptions-item>
        <a-descriptions-item label="最后登录">{{
          formatTime(detailUser.lastLoginTime)
        }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
    <!-- 封禁原因弹窗 -->
    <a-modal
      v-model:visible="banReasonVisible"
      title="封禁原因"
      @ok="confirmBan"
      @cancel="banReasonVisible = false"
    >
      <a-input
        v-model="banReason"
        placeholder="请输入封禁原因（选填，最多128字）"
        :max-length="128"
        show-word-limit
      />
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Message, Modal } from '@arco-design/web-vue';
import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import useLoading from '@/hooks/loading';
import {
  getImUserList,
  getImUserInfo,
  banImUser,
  unbanImUser,
  exportImUserList,
} from '@/api/im/user';
import type { ImUserVo } from '@/api/im/user';

const { loading, setLoading } = useLoading(false);
const route = useRoute();
const router = useRouter();

const rowSelection = reactive({
  type: 'checkbox' as const,
  showCheckedAll: true,
});

const selectedKeys = ref<(string | number)[]>([]);

const columns: TableColumnData[] = [
  {
    title: '编号',
    dataIndex: 'id',
    slotName: 'id',
    width: 60,
    align: 'center',
  },
  {
    title: '头像',
    dataIndex: 'avatar',
    slotName: 'avatar',
    align: 'center',
    width: 100,
  },
  { title: '用户名', dataIndex: 'userName', align: 'center', width: 100 },
  { title: '用户昵称', dataIndex: 'nickName', align: 'center', width: 100 },
  {
    title: '手机号',
    dataIndex: 'phone',
    slotName: 'phone',
    align: 'center',
    width: 128,
    ellipsis: true,
    tooltip: true,
  },
  { title: '因讯号', dataIndex: 'hsId', align: 'center', width: 100 },
  {
    title: '性别',
    dataIndex: 'sex',
    slotName: 'sex',
    align: 'center',
    width: 100,
  },
  {
    title: '是否封禁',
    dataIndex: 'isBanned',
    slotName: 'isBanned',
    align: 'center',
    width: 100,
  },
  {
    title: '注册时间',
    dataIndex: 'createdTime',
    slotName: 'createdTime',
    align: 'center',
    width: 100,
  },
  {
    title: '操作',
    slotName: 'action',
    align: 'center',
    width: 100,
    fixed: 'right',
  },
];

const queryParams = reactive({
  userName: '',
  nickName: '',
  activeRange: undefined as 'daily' | 'monthly' | undefined,
  pageNum: 1,
  pageSize: 10,
});

const userList = ref<ImUserVo[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
});

const detailVisible = ref(false);
const detailUser = ref<ImUserVo | null>(null);
const banReasonVisible = ref(false);
const banReason = ref('');
const banTargetId = ref<number | null>(null);

function formatTime(t: string | undefined) {
  if (!t) return '-';
  const d = new Date(t);
  return d.toLocaleString('zh-CN', { hour12: false });
}

function isMaleSex(sex: unknown) {
  if (sex === 0 || sex === '0' || sex === false) return true;
  if (typeof sex === 'string' && sex.trim() === '男') return true;
  if (typeof sex === 'number' && sex === 0) return true;
  return false;
}

function isFemaleSex(sex: unknown) {
  if (sex === 1 || sex === '1') return true;
  if (typeof sex === 'string' && sex.trim() === '女') return true;
  if (typeof sex === 'number' && sex === 1) return true;
  return false;
}

function onSelectionChange(keys: (string | number)[]) {
  selectedKeys.value = keys;
}

const fetchList = async () => {
  setLoading(true);
  try {
    const res = (await getImUserList({
      userName: queryParams.userName || undefined,
      nickName: queryParams.nickName || undefined,
      activeRange: queryParams.activeRange,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
    })) as {
      rows?: ImUserVo[];
      total?: number;
      data?: { rows?: ImUserVo[]; total?: number };
    };
    const payload = res?.rows ? res : res?.data ?? {};
    const rows = payload?.rows ?? [];
    const total = payload?.total ?? 0;
    userList.value = Array.isArray(rows) ? rows : [];
    pagination.total = total;
    selectedKeys.value = [];
  } catch (e) {
    Message.error('获取用户列表失败');
    userList.value = [];
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
  queryParams.userName = '';
  queryParams.nickName = '';
  queryParams.activeRange = undefined;
  router.replace({ path: '/im/user' });
  handleQuery();
};

const handleExport = async () => {
  try {
    const res = await exportImUserList({
      userName: queryParams.userName || undefined,
      nickName: queryParams.nickName || undefined,
      activeRange: queryParams.activeRange,
    });
    const blob = res instanceof Blob ? res : (res as any).data;
    if (!blob) {
      Message.error('导出失败');
      return;
    }
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `IM用户列表_${new Date().toISOString().slice(0, 10)}.xlsx`;
    a.click();
    window.URL.revokeObjectURL(url);
    Message.success('导出成功');
  } catch (e) {
    Message.error('导出失败');
  }
};

const onPageChange = (page: number) => {
  queryParams.pageNum = page;
  pagination.current = page;
  queryParams.pageSize = pagination.pageSize;
  fetchList();
};

const onPageSizeChange = (pageSize: number) => {
  queryParams.pageNum = 1;
  queryParams.pageSize = pageSize;
  pagination.current = 1;
  pagination.pageSize = pageSize;
  fetchList();
};

const handleDetail = async (record: ImUserVo) => {
  try {
    const res = (await getImUserInfo(record.id)) as any;
    detailUser.value = res.data ?? res;
    detailVisible.value = true;
  } catch (e) {
    Message.error('获取详情失败');
  }
};

const handleBan = (record: ImUserVo) => {
  banTargetId.value = record.id;
  banReason.value = '';
  banReasonVisible.value = true;
};

const confirmBan = async () => {
  if (banTargetId.value == null) return;
  try {
    await banImUser({
      id: banTargetId.value,
      reason: banReason.value || undefined,
    });
    Message.success('封禁成功');
    banReasonVisible.value = false;
    fetchList();
  } catch (e) {
    Message.error('封禁失败');
  }
};

const handleUnban = (record: ImUserVo) => {
  Modal.confirm({
    title: '确认解封',
    content: `确定要解封用户「${record.nickName || record.userName}」吗？`,
    onOk: async () => {
      try {
        await unbanImUser({ id: record.id });
        Message.success('解封成功');
        fetchList();
      } catch (e) {
        Message.error('解封失败');
      }
    },
  });
};

onMounted(() => {
  const { activeRange } = route.query;
  if (activeRange === 'daily' || activeRange === 'monthly') {
    queryParams.activeRange = activeRange;
  }
  fetchList();
});

watch(
  () => route.query.activeRange,
  (activeRange) => {
    if (activeRange === 'daily' || activeRange === 'monthly') {
      queryParams.activeRange = activeRange;
    } else {
      queryParams.activeRange = undefined;
    }
    queryParams.pageNum = 1;
    pagination.current = 1;
    fetchList();
  }
);
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
