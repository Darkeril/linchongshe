<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['笔记管理', '群聊管理']"></Breadcrumb>
    <a-card
      class="general-card r-nav-card"
      :body-style="{ padding: '15px' }"
      :header-style="{ padding: '15px' }"
    >
      <a-row>
        <!-- 搜索区块 -->
        <a-col :flex="1">
          <a-form
            :model="formModel"
            :label-col-props="{ span: 6 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="left"
            auto-label-width
          >
            <a-row :gutter="16">
              <a-col :span="6">
                <a-form-item field="groupName" label="群名称">
                  <a-input
                    v-model="formModel.groupName"
                    placeholder="请输入群名称"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="ownerId" label="群主ID">
                  <a-input
                    v-model="formModel.ownerId"
                    placeholder="请输入群主ID"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="status" label="状态">
                  <a-select
                    v-model="formModel.status"
                    placeholder="请选择状态"
                    allow-clear
                  >
                    <a-option :value="0" label="正常">正常</a-option>
                    <a-option :value="1" label="已解散">已解散</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 42px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space :size="18">
            <a-button type="primary" @click="search">
              <template #icon>
                <icon-search />
              </template>
              查询
            </a-button>
            <a-button @click="reset">
              <template #icon>
                <icon-refresh />
              </template>
              重置
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-divider style="margin-top: 0" />
      <!-- 工具栏 -->
      <a-row style="margin-bottom: 16px">
        <a-col :span="12">
          <a-space>
            <a-button
              status="danger"
              :disabled="selectedKeys.length <= 0"
              @click="handleBatchDelete"
            >
              <template #icon>
                <icon-delete />
              </template>
              批量解散
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <!-- 表格数据-->
      <a-table
        row-key="id"
        :loading="loading"
        :columns="columns"
        :data="renderData"
        v-model:selected-keys="selectedKeys"
        :pagination="pagination"
        :bordered="false"
        :scrollbar="true"
        :row-selection="{ type: 'checkbox', showCheckedAll: true }"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
      >
        <template #groupAvatar="{ record }">
          <a-avatar v-if="record.groupAvatar" :size="50" shape="square">
            <img :src="record.groupAvatar" alt="群头像" />
          </a-avatar>
          <a-avatar v-else :size="50" shape="square">
            <icon-user />
          </a-avatar>
        </template>
        <template #groupName="{ record }">
          <span>{{ record.groupName || '未命名群聊' }}</span>
        </template>
        <template #ownerName="{ record }">
          <div style="display: flex; align-items: center; justify-content: flex-start;">
            <a-avatar v-if="record.ownerAvatar" :size="30" style="flex-shrink: 0;">
              <img :src="record.ownerAvatar" alt="头像" />
            </a-avatar>
            <a-avatar v-else :size="30" style="flex-shrink: 0;">
              <icon-user />
            </a-avatar>
            <span style="margin-left: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ record.ownerName || '未知' }}</span>
          </div>
        </template>
        <template #status="{ record }">
          <a-tag :color="record.status === 0 ? 'green' : 'red'">
            {{ record.status === 0 ? '正常' : '已解散' }}
          </a-tag>
        </template>
        <template #memberCount="{ record }">
          {{ record.memberCount || 0 }} / {{ record.maxMemberCount || 500 }}
        </template>
        <template #createTime="{ record }">
          {{ formatTime(record.createTime) }}
        </template>
        <template #operations="{ record }">
          <a-space>
            <a-button
              type="text"
              size="small"
              @click="handleView(record)"
            >
              <template #icon>
                <icon-eye />
              </template>
              查看
            </a-button>
            <a-button
              type="text"
              size="small"
              status="danger"
              @click="handleDelete(record)"
            >
              <template #icon>
                <icon-delete />
              </template>
              解散
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 群聊详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="群聊详情"
      width="80%"
      :footer="false"
      :mask-closable="false"
      @cancel="handleDetailClose"
    >
      <div v-if="currentGroup" class="group-detail">
        <a-row :gutter="24">
          <a-col :span="8">
            <div class="detail-info">
              <a-descriptions :column="1" bordered>
                <a-descriptions-item label="群聊ID">
                  {{ currentGroup.id }}
                </a-descriptions-item>
                <a-descriptions-item label="群名称">
                  {{ currentGroup.groupName }}
                </a-descriptions-item>
                <a-descriptions-item label="群主">
                  {{ currentGroup.ownerName }}
                </a-descriptions-item>
                <a-descriptions-item label="成员数量">
                  {{ currentGroup.memberCount || 0 }} / {{ currentGroup.maxMemberCount || 500 }}
                </a-descriptions-item>
                <a-descriptions-item label="状态">
                  <a-tag :color="currentGroup.status === 0 ? 'green' : 'red'">
                    {{ currentGroup.status === 0 ? '正常' : '已解散' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="创建时间">
                  {{ formatTime(currentGroup.createTime) }}
                </a-descriptions-item>
              </a-descriptions>
              <div v-if="currentGroup.groupDescription" class="detail-description" style="margin-top: 16px;">
                <h4>群简介</h4>
                <p>{{ currentGroup.groupDescription }}</p>
              </div>
            </div>
          </a-col>
          <a-col :span="16">
            <div class="member-list">
              <h4>群成员列表（{{ memberList.length }}人）</h4>
              <a-table
                :columns="memberColumns"
                :data="memberList"
                :pagination="false"
                :bordered="false"
                size="small"
              >
                <template #avatar="{ record }">
                  <a-avatar v-if="record.avatar" :size="35">
                    <img :src="record.avatar" alt="头像" />
                  </a-avatar>
                  <a-avatar v-else :size="35">
                    <icon-user />
                  </a-avatar>
                </template>
                <template #role="{ record }">
                  <a-tag v-if="record.role === 0" color="red">群主</a-tag>
                  <a-tag v-else-if="record.role === 1" color="blue">管理员</a-tag>
                  <a-tag v-else color="gray">成员</a-tag>
                </template>
                <template #joinTime="{ record }">
                  {{ formatTime(record.joinTime) }}
                </template>
                <template #memberOperations="{ record }">
                  <a-button
                    v-if="record.role !== 0"
                    type="text"
                    size="small"
                    status="danger"
                    @click="handleRemoveMember(record)"
                  >
                    移除
                  </a-button>
                </template>
              </a-table>
            </div>
          </a-col>
        </a-row>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, reactive, onMounted } from 'vue';
import { Message, Modal } from '@arco-design/web-vue';
import { Pagination } from '@/types/global';
import {
  IconSearch,
  IconRefresh,
  IconUser,
  IconDelete,
  IconEye,
} from '@arco-design/web-vue/es/icon';
import {
  listGroupChat,
  getGroupChat,
  getGroupMembers,
  delGroupChat,
  removeMember,
  type GroupChatRecord,
  type GroupChatMemberRecord,
} from '@/api/web/groupChat';
import Breadcrumb from '@/components/breadcrumb/index.vue';

const generateFormModel = () => {
  return {
    groupName: '',
    ownerId: '',
    status: undefined as number | undefined,
  };
};

const formModel = ref(generateFormModel());
const loading = ref(false);
const selectedKeys = ref([]);
const detailVisible = ref(false);
const currentGroup = ref<GroupChatRecord | null>(null);
const memberList = ref<GroupChatMemberRecord[]>([]);

const basePagination: Pagination = {
  current: 1,
  pageSize: 20,
};

const pagination = reactive({
  ...basePagination,
});

const columns = computed(() => [
  {
    title: '群头像',
    dataIndex: 'groupAvatar',
    slotName: 'groupAvatar',
    width: 100,
    align: 'center',
  },
  {
    title: '群名称',
    dataIndex: 'groupName',
    slotName: 'groupName',
    width: 180,
    align: 'left',
  },
  {
    title: '群主',
    dataIndex: 'ownerName',
    slotName: 'ownerName',
    width: 150,
    align: 'left',
  },
  {
    title: '成员数',
    dataIndex: 'memberCount',
    slotName: 'memberCount',
    width: 120,
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
    title: '创建时间',
    dataIndex: 'createTime',
    slotName: 'createTime',
    width: 180,
    align: 'center',
  },
  {
    title: '操作',
    dataIndex: 'operations',
    slotName: 'operations',
    width: 150,
    align: 'center',
    fixed: 'right',
  },
]);

const memberColumns = computed(() => [
  {
    title: '头像',
    dataIndex: 'avatar',
    slotName: 'avatar',
    width: 80,
    align: 'center',
  },
  {
    title: '用户名',
    dataIndex: 'username',
    width: 150,
    align: 'center',
  },
  {
    title: '昵称',
    dataIndex: 'nickname',
    width: 150,
    align: 'center',
  },
  {
    title: '角色',
    dataIndex: 'role',
    slotName: 'role',
    width: 100,
    align: 'center',
  },
  {
    title: '加入时间',
    dataIndex: 'joinTime',
    slotName: 'joinTime',
    width: 180,
    align: 'center',
  },
  {
    title: '操作',
    dataIndex: 'memberOperations',
    slotName: 'memberOperations',
    width: 100,
    align: 'center',
  },
]);

const renderData = ref<GroupChatRecord[]>([]);

const fetchData = async (params: any = { current: 1, pageSize: 20 }) => {
  loading.value = true;
  try {
    const requestParams: any = {
      pageNum: params.current || 1,
      pageSize: params.pageSize || 20,
    };
    
    if (params.groupName) {
      requestParams.groupName = params.groupName;
    }
    if (params.ownerId) {
      requestParams.ownerId = params.ownerId;
    }
    if (params.status !== undefined && params.status !== null) {
      requestParams.status = params.status;
    }

    const res = await listGroupChat(requestParams);

    let list;
    let total;
    if (res?.data?.list) {
      list = res.data.list;
      total = res.data.total;
    } else if (res?.data?.rows) {
      list = res.data.rows;
      total = res.data.total;
    } else if (res?.rows) {
      list = res.rows;
      total = res.total;
    } else if (Array.isArray(res?.data)) {
      list = res.data;
      total = res.data.length;
    } else if (res?.data?.records) {
      list = res.data.records;
      total = res.data.total;
    } else {
      list = [];
      total = 0;
    }

    renderData.value = Array.isArray(list) ? list : [];
    pagination.current = params.current;
    pagination.total = typeof total === 'number' ? total : renderData.value.length;
  } catch (err) {
    console.error('获取群聊列表失败', err);
    renderData.value = [];
    pagination.total = 0;
    Message.error('获取群聊列表失败');
  } finally {
    loading.value = false;
  }
};

const search = () => {
  pagination.current = 1;
  const params: any = {
    current: 1,
    pageSize: pagination.pageSize,
    groupName: formModel.value.groupName,
    ownerId: formModel.value.ownerId,
    status: formModel.value.status,
  };
  fetchData(params);
};

const reset = () => {
  formModel.value = generateFormModel();
  pagination.current = 1;
  fetchData(basePagination);
};

const onPageChange = (current: number) => {
  const params: any = {
    current,
    pageSize: pagination.pageSize,
    groupName: formModel.value.groupName,
    ownerId: formModel.value.ownerId,
    status: formModel.value.status,
  };
  fetchData(params);
};

const onPageSizeChange = (pageSize: number) => {
  const params: any = {
    current: pagination.current,
    pageSize,
    groupName: formModel.value.groupName,
    ownerId: formModel.value.ownerId,
    status: formModel.value.status,
  };
  fetchData(params);
};

const handleView = async (record: GroupChatRecord) => {
  try {
    // 获取群聊详情
    const res = await getGroupChat(record.id!);
    currentGroup.value = res.data || record;
    
    // 获取成员列表
    const memberRes = await getGroupMembers(record.id!);
    memberList.value = memberRes.data || [];
    
    detailVisible.value = true;
  } catch (err) {
    console.error('获取群聊详情失败', err);
    Message.error('获取群聊详情失败');
  }
};

const handleDetailClose = () => {
  detailVisible.value = false;
  currentGroup.value = null;
  memberList.value = [];
};

const handleDelete = (record: GroupChatRecord) => {
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: `是否确认解散群聊"${record.groupName}"？解散后所有成员将被移除。`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await delGroupChat([record.id!]);
        Message.success('解散成功');
        fetchData({
          current: pagination.current,
          pageSize: pagination.pageSize,
          groupName: formModel.value.groupName,
          ownerId: formModel.value.ownerId,
          status: formModel.value.status,
        });
      } catch (error) {
        Message.error('解散失败');
      }
    },
  });
};

const handleBatchDelete = () => {
  if (selectedKeys.value.length === 0) {
    Message.warning('请选择要解散的群聊');
    return;
  }

  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: `是否确认解散选中的 ${selectedKeys.value.length} 个群聊？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await delGroupChat(selectedKeys.value as string[]);
        Message.success(`成功解散 ${selectedKeys.value.length} 个群聊`);
        selectedKeys.value = [];
        fetchData({
          current: pagination.current,
          pageSize: pagination.pageSize,
          groupName: formModel.value.groupName,
          ownerId: formModel.value.ownerId,
          status: formModel.value.status,
        });
      } catch (error) {
        Message.error('批量解散失败');
      }
    },
  });
};

const handleRemoveMember = (member: GroupChatMemberRecord) => {
  if (!currentGroup.value) return;
  
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: `是否确认将"${member.username || member.nickname}"移出群聊？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await removeMember(currentGroup.value!.id!, member.userId!);
        Message.success('移除成功');
        // 刷新成员列表
        const memberRes = await getGroupMembers(currentGroup.value!.id!);
        memberList.value = memberRes.data || [];
        // 刷新群聊列表
        fetchData({
          current: pagination.current,
          pageSize: pagination.pageSize,
          groupName: formModel.value.groupName,
          ownerId: formModel.value.ownerId,
          status: formModel.value.status,
        });
      } catch (error) {
        Message.error('移除失败');
      }
    },
  });
};

const formatTime = (time: string | undefined) => {
  if (!time) return '-';
  const date = new Date(time);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

onMounted(() => {
  fetchData(basePagination);
});
</script>

<style lang="less" scoped>
.container {
  padding: 0 20px 20px 20px;
}

.group-detail {
  .detail-info {
    margin-bottom: 20px;
  }
  
  .detail-description {
    h4 {
      margin-bottom: 8px;
      font-size: 14px;
      font-weight: 600;
    }
    
    p {
      margin: 0;
      color: #666;
      line-height: 1.6;
    }
  }
  
  .member-list {
    h4 {
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 600;
    }
  }
}
</style>

