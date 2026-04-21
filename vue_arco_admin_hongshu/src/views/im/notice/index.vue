<template>
  <div class="container">
    <Breadcrumb :items="['IM管理', '通知管理']" />
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
                allow-clear
                placeholder="请输入通知内容关键字"
                style="width: 220px"
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
        <a-col>
          <a-button
            style="margin-top: 5px"
            type="primary"
            @click="noticeDialog.visible = true"
          >
            <template #icon><icon-plus /></template>
            发送系统通知
          </a-button>
        </a-col>
      </a-row>

      <a-table
        :loading="loading"
        :data="tableData"
        :columns="columns"
        :pagination="pagination"
        row-key="id"
        @page-change="onPageChange"
      >
        <template #scopeType="{ record }">
          {{ record.scopeType === 1 ? '指定用户' : '全员' }}
        </template>
        <template #sendTime="{ record }">
          {{ formatTime(record.sendTime) }}
        </template>
        <template #action="{ record }">
          <a-button type="text" size="small" @click="openDetail(record)"
            >详情</a-button
          >
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="noticeDialog.visible"
      title="发送系统通知"
      :ok-loading="noticeDialog.sending"
      @ok="submitSystemNotice"
      @cancel="resetNoticeDialog"
    >
      <a-form :model="noticeDialog.form" layout="vertical">
        <a-form-item label="发送范围">
          <a-radio-group v-model="noticeDialog.form.sendAll">
            <a-radio :value="true">全员</a-radio>
            <a-radio :value="false">指定用户</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item v-if="!noticeDialog.form.sendAll" label="选择用户">
          <a-select
            v-model="noticeDialog.form.recvIds"
            :loading="noticeDialog.userSearching"
            placeholder="请输入用户名或ID搜索并选择用户"
            allow-search
            allow-clear
            multiple
            :filter-option="false"
            @search="handleUserSearch"
          >
            <template #empty>
              <div
                v-if="noticeDialog.userSearching"
                style="text-align: center; padding: 8px"
              >
                搜索中...
              </div>
              <div
                v-else
                style="text-align: center; padding: 8px; color: #86909c"
              >
                请输入关键词搜索用户
              </div>
            </template>
            <a-option
              v-for="option in noticeDialog.userOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            >
              <div style="display: flex; align-items: center; gap: 8px">
                <a-avatar
                  v-if="option.avatar"
                  :size="24"
                  :image-url="option.avatar"
                />
                <span>{{ option.username }}</span>
                <span style="color: #86909c; font-size: 12px"
                  >(ID: {{ option.id }})</span
                >
              </div>
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="通知标题">
          <a-input
            v-model="noticeDialog.form.title"
            :max-length="60"
            placeholder="请输入通知标题"
            allow-clear
            show-word-limit
          />
        </a-form-item>
        <a-form-item label="副标题">
          <a-input
            v-model="noticeDialog.form.subtitle"
            :max-length="120"
            placeholder="用于列表摘要展示"
            allow-clear
            show-word-limit
          />
        </a-form-item>
        <a-form-item label="封面图URL">
          <div class="cover-url-wrap">
            <a-upload
              v-if="!noticeDialog.form.cover"
              :show-file-list="false"
              :auto-upload="false"
              accept="image/*"
              @before-upload="beforeCoverUpload"
            >
              <template #upload-button>
                <a-button
                  type="outline"
                  :loading="noticeDialog.coverUploading"
                  class="cover-upload-btn"
                >
                  <template #icon><icon-upload /></template>
                  上传
                </a-button>
              </template>
            </a-upload>
            <a-input
              v-model="noticeDialog.form.cover"
              placeholder="输入封面图链接，或点击左侧上传"
              allow-clear
              class="cover-url-input"
            />
          </div>
          <div v-if="noticeDialog.form.cover" class="cover-preview-wrap">
            <a-image
              :src="noticeDialog.form.cover"
              width="220"
              height="120"
              fit="cover"
              class="cover-preview"
            />
            <div class="cover-remove" @click="removeCover">×</div>
          </div>
        </a-form-item>
        <a-form-item label="详情链接（可选）">
          <a-input
            v-model="noticeDialog.form.detailUrl"
            placeholder="https://example.com/detail"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="通知正文">
          <a-textarea
            v-model="noticeDialog.form.content"
            :max-length="1000"
            :auto-size="{ minRows: 4, maxRows: 8 }"
            placeholder="请输入通知正文内容"
            show-word-limit
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="detailDialog.visible"
      :title="`通知详情 #${detailDialog.noticeId || ''}`"
      width="860px"
      :footer="false"
      @cancel="closeDetail"
    >
      <a-table
        :loading="detailDialog.loading"
        :data="detailDialog.rows"
        :columns="detailColumns"
        :pagination="detailDialog.pagination"
        row-key="userId"
        @page-change="onDetailPageChange"
      >
        <template #avatar="{ record }">
          <a-avatar
            :size="32"
            shape="circle"
            :image-url="record.headImage || undefined"
          >
            {{ (record.nickName || record.userName || '?').slice(0, 1) }}
          </a-avatar>
        </template>
        <template #readFlag="{ record }">
          <a-tag :color="record.readFlag === 1 ? 'green' : 'orange'">
            {{ record.readFlag === 1 ? '已读' : '未读' }}
          </a-tag>
        </template>
        <template #readTime="{ record }">
          {{ record.readFlag === 1 ? formatTime(record.readTime) : '-' }}
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import useLoading from '@/hooks/loading';
import {
  getImSystemNoticeList,
  getImSystemNoticeDetailList,
  sendImSystemNotice,
  type ImSystemNoticeRecord,
  type ImSystemNoticeDetailRecord,
} from '@/api/im/systemNotice';
import { searchImUserByName, type ImUserVo } from '@/api/im/user';
import { uploadFile } from '@/api/web/xiaohongshu';

const { loading, setLoading } = useLoading(false);

const columns: TableColumnData[] = [
  { title: 'ID', dataIndex: 'id', align: 'center', width: 90 },
  {
    title: '通知内容',
    dataIndex: 'content',
    align: 'center',
    width: 150,
    ellipsis: true,
    tooltip: true,
  },
  {
    title: '范围',
    dataIndex: 'scopeType',
    slotName: 'scopeType',
    align: 'center',
    width: 100,
  },
  {
    title: '接收人数',
    dataIndex: 'totalRecvCount',
    align: 'center',
    width: 100,
  },
  { title: '已读人数', dataIndex: 'readCount', align: 'center', width: 100 },
  { title: '未读人数', dataIndex: 'unreadCount', align: 'center', width: 100 },
  {
    title: '发送时间',
    dataIndex: 'sendTime',
    slotName: 'sendTime',
    align: 'center',
    width: 100,
  },
  {
    title: '操作',
    dataIndex: 'action',
    slotName: 'action',
    align: 'center',
    width: 100,
  },
];

const detailColumns: TableColumnData[] = [
  { title: '用户ID', dataIndex: 'userId', align: 'center', width: 90 },
  {
    title: '头像',
    dataIndex: 'headImage',
    slotName: 'avatar',
    align: 'center',
    width: 72,
  },
  { title: '用户名', dataIndex: 'userName', align: 'center' },
  { title: '昵称', dataIndex: 'nickName', align: 'center' },
  {
    title: '状态',
    dataIndex: 'readFlag',
    slotName: 'readFlag',
    align: 'center',
    width: 90,
  },
  {
    title: '已读时间',
    dataIndex: 'readTime',
    slotName: 'readTime',
    align: 'center',
    width: 180,
  },
];

const queryParams = reactive({
  content: '',
  pageNum: 1,
  pageSize: 10,
});

const tableData = ref<ImSystemNoticeRecord[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
});

const noticeDialog = reactive({
  visible: false,
  sending: false,
  userSearching: false,
  coverUploading: false,
  userOptions: [] as Array<{
    value: number;
    label: string;
    username: string;
    id: number;
    avatar?: string;
  }>,
  form: {
    sendAll: true,
    recvIds: [] as number[],
    title: '',
    subtitle: '',
    cover: '',
    detailUrl: '',
    content: '',
  },
});

const detailDialog = reactive({
  visible: false,
  noticeId: 0,
  loading: false,
  rows: [] as ImSystemNoticeDetailRecord[],
  pagination: {
    current: 1,
    pageSize: 10,
    total: 0,
    showTotal: true,
  },
});

const fetchList = async () => {
  setLoading(true);
  try {
    const res = (await getImSystemNoticeList({
      content: queryParams.content || undefined,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
    })) as {
      rows?: ImSystemNoticeRecord[];
      total?: number;
      data?: { rows?: ImSystemNoticeRecord[]; total?: number };
    };
    const payload = res?.rows ? res : res?.data ?? {};
    tableData.value = Array.isArray(payload?.rows) ? payload.rows : [];
    pagination.total = payload?.total ?? 0;
  } catch {
    tableData.value = [];
    pagination.total = 0;
    Message.error('获取通知列表失败');
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

const onPageChange = (current: number) => {
  pagination.current = current;
  queryParams.pageNum = current;
  fetchList();
};

const fetchDetailList = async () => {
  if (!detailDialog.noticeId) return;
  detailDialog.loading = true;
  try {
    const res = (await getImSystemNoticeDetailList(detailDialog.noticeId, {
      pageNum: detailDialog.pagination.current,
      pageSize: detailDialog.pagination.pageSize,
    })) as {
      rows?: ImSystemNoticeDetailRecord[];
      total?: number;
      data?: { rows?: ImSystemNoticeDetailRecord[]; total?: number };
    };
    const payload = res?.rows ? res : res?.data ?? {};
    detailDialog.rows = Array.isArray(payload?.rows) ? payload.rows : [];
    detailDialog.pagination.total = payload?.total ?? 0;
  } catch {
    detailDialog.rows = [];
    detailDialog.pagination.total = 0;
    Message.error('获取通知详情失败');
  } finally {
    detailDialog.loading = false;
  }
};

const openDetail = (record: ImSystemNoticeRecord) => {
  detailDialog.noticeId = Number(record.id || 0);
  detailDialog.pagination.current = 1;
  detailDialog.visible = true;
  fetchDetailList();
};

const closeDetail = () => {
  detailDialog.visible = false;
  detailDialog.noticeId = 0;
  detailDialog.rows = [];
};

const onDetailPageChange = (current: number) => {
  detailDialog.pagination.current = current;
  fetchDetailList();
};

const resetNoticeDialog = () => {
  noticeDialog.form.sendAll = true;
  noticeDialog.form.recvIds = [];
  noticeDialog.form.title = '';
  noticeDialog.form.subtitle = '';
  noticeDialog.form.cover = '';
  noticeDialog.form.detailUrl = '';
  noticeDialog.form.content = '';
  noticeDialog.userOptions = [];
};

const beforeCoverUpload = async (file: any) => {
  const rawFile: File = file?.file || file;
  const fileName = String(rawFile?.name || '').toLowerCase();
  const fileType = String(rawFile?.type || '').toLowerCase();
  const isImage =
    fileType.startsWith('image/') ||
    /\.(jpg|jpeg|png|gif|webp|bmp)$/.test(fileName);
  if (!isImage) {
    Message.error('只能上传图片文件');
    return false;
  }
  noticeDialog.coverUploading = true;
  try {
    const res = await uploadFile(rawFile);
    const coverUrl = res?.data?.url;
    if (!coverUrl) {
      Message.error('上传失败，未返回图片地址');
      return false;
    }
    noticeDialog.form.cover = coverUrl;
    Message.success('封面上传成功');
  } catch (e: any) {
    Message.error(e?.message || '封面上传失败');
  } finally {
    noticeDialog.coverUploading = false;
  }
  return false;
};

const removeCover = () => {
  noticeDialog.form.cover = '';
};

const submitSystemNotice = async () => {
  const title = (noticeDialog.form.title || '').trim();
  const subtitle = (noticeDialog.form.subtitle || '').trim();
  const content = (noticeDialog.form.content || '').trim();
  const cover = (noticeDialog.form.cover || '').trim();
  const detailUrl = (noticeDialog.form.detailUrl || '').trim();
  if (!title) {
    Message.warning('请输入通知标题');
    return false;
  }
  if (!content) {
    Message.warning('请输入通知正文');
    return false;
  }
  let recvIds: number[] | undefined;
  if (!noticeDialog.form.sendAll) {
    const arr = (noticeDialog.form.recvIds || [])
      .map((n) => Number(n))
      .filter((n) => Number.isFinite(n) && n > 0);
    if (arr.length === 0) {
      Message.warning('请至少选择一个用户');
      return false;
    }
    recvIds = Array.from(new Set(arr));
  }
  noticeDialog.sending = true;
  try {
    const payloadContent = JSON.stringify({
      title,
      subtitle,
      cover,
      content,
      detailUrl,
    });
    await sendImSystemNotice({ content: payloadContent, recvIds });
    Message.success('系统通知发送成功');
    noticeDialog.visible = false;
    resetNoticeDialog();
    fetchList();
    return true;
  } catch (e: any) {
    Message.error(e?.message || '系统通知发送失败');
    return false;
  } finally {
    noticeDialog.sending = false;
  }
};

const handleUserSearch = async (value: string) => {
  if (!value || !value.trim()) {
    noticeDialog.userOptions = [];
    return;
  }
  noticeDialog.userSearching = true;
  try {
    const response = await searchImUserByName(value.trim());
    const userList: ImUserVo[] = Array.isArray(response?.data)
      ? response.data
      : [];
    noticeDialog.userOptions = userList
      .map((user: ImUserVo) => {
        const userId = Number(user.id);
        if (!Number.isFinite(userId) || userId <= 0) return null;
        const displayName = user.nickName || user.userName || `用户${userId}`;
        return {
          value: userId,
          label: `${displayName} (ID: ${userId})`,
          username: displayName,
          id: userId,
          avatar: user.headImageThumb || user.headImage,
        };
      })
      .filter((item): item is NonNullable<typeof item> => Boolean(item));
  } catch {
    noticeDialog.userOptions = [];
    Message.error('搜索用户失败');
  } finally {
    noticeDialog.userSearching = false;
  }
};

function formatTime(t: string | number | undefined) {
  if (!t) return '-';
  return new Date(t).toLocaleString('zh-CN', { hour12: false });
}

onMounted(() => {
  fetchList();
});
</script>

<style scoped>
.cover-url-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cover-url-input {
  flex: 1;
}

.cover-upload-btn {
  white-space: nowrap;
}

.cover-preview {
  margin-top: 10px;
}

.cover-preview-wrap {
  margin-top: 10px;
  position: relative;
  width: 220px;
  height: 120px;
}

.cover-remove {
  position: absolute;
  right: -8px;
  top: -8px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.65);
  color: #fff;
  line-height: 20px;
  text-align: center;
  font-size: 14px;
  cursor: pointer;
  user-select: none;
}
</style>

