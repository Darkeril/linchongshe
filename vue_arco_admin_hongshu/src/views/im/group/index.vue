<template>
  <div class="container">
    <Breadcrumb :items="['IM管理', '群聊管理']" />
    <a-card
      class="general-card r-nav-card"
      :bordered="false"
      :body-style="{ padding: '15px' }"
      :header-style="{ padding: '15px' }"
    >
      <a-row style="margin-bottom: 16px">
        <a-col :flex="1">
          <a-form :model="queryParams" layout="inline">
            <a-form-item label="群名">
              <a-input
                v-model="queryParams.name"
                placeholder="请输入群名"
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
        :data="groupList"
        :columns="columns"
        :pagination="pagination"
        row-key="id"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
      >
        <template #headImage="{ record }">
          <a-avatar
            v-if="record.headImage"
            :size="40"
            shape="circle"
            :image-url="record.headImage"
          >
            {{ (record.name || '').slice(0, 1) || '群' }}
          </a-avatar>
          <a-avatar v-else :size="40" shape="circle">
            {{ (record.name || '').slice(0, 1) || '群' }}
          </a-avatar>
        </template>
        <template #createdTime="{ record }">
          {{ formatTime(record.createdTime) }}
        </template>
        <template #dissolve="{ record }">
          <a-tag v-if="record.dissolve" color="red">已解散</a-tag>
          <a-tag v-else color="green">正常</a-tag>
        </template>
        <template #isBanned="{ record }">
          <a-tag v-if="record.isBanned" color="red">已封禁</a-tag>
          <a-tag v-else color="green">正常</a-tag>
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
            <a-button type="text" size="small" @click="handleMembers(record)"
              >查看成员</a-button
            >
          </a-space>
        </template>
      </a-table>
    </a-card>
    <!-- 详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="群聊详情"
      :footer="false"
      width="600px"
    >
      <a-descriptions :column="1" bordered v-if="detailGroup">
        <a-descriptions-item label="群名">{{
          detailGroup.name
        }}</a-descriptions-item>
        <a-descriptions-item label="群主">{{
          detailGroup.ownerUserName
        }}</a-descriptions-item>
        <a-descriptions-item label="成员数量">{{
          detailGroup.memberCount
        }}</a-descriptions-item>
        <a-descriptions-item label="群公告">{{
          detailGroup.notice || '-'
        }}</a-descriptions-item>
        <a-descriptions-item label="是否解散">{{
          detailGroup.dissolve ? '是' : '否'
        }}</a-descriptions-item>
        <a-descriptions-item label="是否封禁">{{
          detailGroup.isBanned ? '是' : '否'
        }}</a-descriptions-item>
        <a-descriptions-item label="封禁原因" v-if="detailGroup.isBanned">{{
          detailGroup.reason || '-'
        }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{
          formatTime(detailGroup.createdTime)
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
        placeholder="请输入封禁原因（选填）"
        :max-length="128"
        show-word-limit
      />
    </a-modal>
    <!-- 成员列表弹窗 -->
    <a-modal
      v-model:visible="memberModalVisible"
      :title="memberModalTitle"
      width="860px"
      :footer="false"
      @cancel="closeMemberModal"
    >
      <a-row style="margin-bottom: 12px">
        <a-col :flex="1">
          <a-form :model="memberQueryParams" layout="inline">
            <a-form-item label="昵称">
              <a-input
                v-model="memberQueryParams.keyword"
                placeholder="请输入用户昵称/群内昵称"
                allow-clear
                style="width: 220px"
              />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleMemberQuery">
                  <template #icon><icon-search /></template>
                  查询
                </a-button>
                <a-button @click="resetMemberQuery">
                  <template #icon><icon-refresh /></template>
                  重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-col>
      </a-row>
      <a-table
        :data="memberList"
        :columns="memberColumns"
        :loading="memberLoading"
        :pagination="memberPagination"
        row-key="id"
        @page-change="onMemberPageChange"
        @page-size-change="onMemberPageSizeChange"
      >
        <template #avatar="{ record }">
          <a-avatar :size="32" :image-url="record.headImage">
            {{ (record.userNickName || record.userName || '?').slice(0, 1) }}
          </a-avatar>
        </template>
        <template #role="{ record }">
          <a-tag :color="getMemberRole(record).color">
            {{ getMemberRole(record).label }}
          </a-tag>
        </template>
        <template #memberCreatedTime="{ record }">
          {{ formatTime(record.createdTime) }}
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { Message, Modal } from '@arco-design/web-vue';
import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import useLoading from '@/hooks/loading';
import {
  getImGroupList,
  getImGroupInfo,
  banImGroup,
  unbanImGroup,
  getImGroupMemberList,
} from '@/api/im/group';
import type { ImGroupVo } from '@/api/im/group';

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
    title: '群头像',
    dataIndex: 'headImage',
    slotName: 'headImage',
    width: 100,
    align: 'center',
  },
  { title: '群名', dataIndex: 'name', width: 100, align: 'center' },
  { title: '群主', dataIndex: 'ownerUserName', width: 100, align: 'center' },
  { title: '成员数量', dataIndex: 'memberCount', align: 'center', width: 100 },
  {
    title: '创建时间',
    dataIndex: 'createdTime',
    slotName: 'createdTime',
    align: 'center',
    width: 100,
  },
  {
    title: '是否解散',
    dataIndex: 'dissolve',
    slotName: 'dissolve',
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
  { title: '操作', slotName: 'action', align: 'center', width: 100 },
];

const memberColumns: TableColumnData[] = [
  {
    title: '头像',
    dataIndex: 'avatar',
    slotName: 'avatar',
    align: 'center',
    width: 76,
  },
  { title: '用户名', dataIndex: 'userName', align: 'center' },
  { title: '昵称', dataIndex: 'userNickName', align: 'center' },
  { title: '群内昵称', dataIndex: 'showNickName', align: 'center' },
  {
    title: '角色',
    dataIndex: 'role',
    slotName: 'role',
    align: 'center',
    width: 100,
  },
  {
    title: '加入时间',
    dataIndex: 'createdTime',
    slotName: 'memberCreatedTime',
    align: 'center',
    width: 180,
  },
];

const queryParams = reactive({
  name: '',
  pageNum: 1,
  pageSize: 10,
});

const groupList = ref<ImGroupVo[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
});

const detailVisible = ref(false);
const detailGroup = ref<ImGroupVo | null>(null);
const banReasonVisible = ref(false);
const banReason = ref('');
const banTargetId = ref<number | null>(null);

const memberModalVisible = ref(false);
const memberModalTitle = ref('群成员列表');
const memberList = ref<any[]>([]);
const memberLoading = ref(false);
const currentGroupId = ref<number | null>(null);
const currentGroupOwnerId = ref<number | null>(null);
const memberQueryParams = reactive({
  keyword: '',
});
const memberPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
});

function formatTime(t: string | undefined) {
  if (!t) return '-';
  return new Date(t).toLocaleString('zh-CN', { hour12: false });
}

const fetchList = async () => {
  setLoading(true);
  try {
    const res = (await getImGroupList({
      name: queryParams.name || undefined,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
    })) as {
      rows?: ImGroupVo[];
      total?: number;
      data?: { rows?: ImGroupVo[]; total?: number };
    };
    const payload = res?.rows ? res : res?.data ?? {};
    const rows = payload?.rows ?? [];
    const total = payload?.total ?? 0;
    groupList.value = Array.isArray(rows) ? rows : [];
    pagination.total = total;
  } catch (e) {
    Message.error('获取群聊列表失败');
    groupList.value = [];
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
  queryParams.name = '';
  handleQuery();
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

const handleDetail = async (record: ImGroupVo) => {
  try {
    const res = (await getImGroupInfo(record.id)) as { data?: ImGroupVo };
    detailGroup.value = res?.data ?? res;
    detailVisible.value = true;
  } catch (e) {
    Message.error('获取详情失败');
  }
};

const handleBan = (record: ImGroupVo) => {
  banTargetId.value = record.id;
  banReason.value = '';
  banReasonVisible.value = true;
};

const confirmBan = async () => {
  if (banTargetId.value == null) return;
  try {
    await banImGroup({
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

const handleUnban = (record: ImGroupVo) => {
  Modal.confirm({
    title: '确认解封',
    content: `确定要解封群聊「${record.name}」吗？`,
    onOk: async () => {
      try {
        await unbanImGroup({ id: record.id });
        Message.success('解封成功');
        fetchList();
      } catch (e) {
        Message.error('解封失败');
      }
    },
  });
};

const fetchMemberList = async () => {
  if (currentGroupId.value == null) return;
  memberLoading.value = true;
  try {
    const res = (await getImGroupMemberList({
      groupId: currentGroupId.value,
      userNickName: memberQueryParams.keyword || undefined,
      remarkNickName: memberQueryParams.keyword || undefined,
      pageNum: memberPagination.current,
      pageSize: memberPagination.pageSize,
    })) as {
      rows?: any[];
      total?: number;
      data?: { rows?: any[]; total?: number };
    };
    const payload = res?.rows ? res : res?.data ?? {};
    const rows = payload?.rows ?? [];
    memberList.value = Array.isArray(rows) ? rows : [];
    memberPagination.total = payload?.total ?? 0;
  } catch (e) {
    Message.error('获取成员列表失败');
    memberList.value = [];
    memberPagination.total = 0;
  } finally {
    memberLoading.value = false;
  }
};

const handleMembers = (record: ImGroupVo) => {
  currentGroupId.value = record.id;
  currentGroupOwnerId.value = record.ownerId ?? null;
  memberModalTitle.value = `群成员列表 - ${record.name || record.id}`;
  memberQueryParams.keyword = '';
  memberPagination.current = 1;
  memberPagination.pageSize = 10;
  memberPagination.total = 0;
  memberModalVisible.value = true;
  fetchMemberList();
};

const onMemberPageChange = (page: number) => {
  memberPagination.current = page;
  fetchMemberList();
};

const onMemberPageSizeChange = (pageSize: number) => {
  memberPagination.current = 1;
  memberPagination.pageSize = pageSize;
  fetchMemberList();
};

const handleMemberQuery = () => {
  memberPagination.current = 1;
  fetchMemberList();
};

const resetMemberQuery = () => {
  memberQueryParams.keyword = '';
  handleMemberQuery();
};

const getMemberRole = (record: any) => {
  if (Number(record?.userId) === Number(currentGroupOwnerId.value)) {
    return { label: '群主', color: 'red' };
  }
  const isAdmin =
    record?.role === 'admin' ||
    record?.memberRole === 'admin' ||
    record?.isAdmin === true ||
    Number(record?.isAdmin) === 1;
  if (isAdmin) {
    return { label: '管理员', color: 'arcoblue' };
  }
  return { label: '成员', color: 'green' };
};

const closeMemberModal = () => {
  memberModalVisible.value = false;
  memberList.value = [];
  currentGroupId.value = null;
  currentGroupOwnerId.value = null;
  memberQueryParams.keyword = '';
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
