<template>
  <div class="container">
    <Breadcrumb :items="['IM管理', '敏感词管理']" />
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
                placeholder="请输入敏感词内容"
                allow-clear
                style="width: 180px"
              />
            </a-form-item>
            <a-form-item label="状态">
              <a-select
                v-model="queryParams.enabled"
                placeholder="全部"
                allow-clear
                style="width: 100px"
              >
                <a-option :value="true">开启</a-option>
                <a-option :value="false">关闭</a-option>
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
              </a-space>
            </a-form-item>
          </a-form>
        </a-col>
        <a-col style="text-align: right">
          <a-button type="primary" @click="handleAdd">
            <template #icon><icon-plus /></template>
            新增
          </a-button>
        </a-col>
      </a-row>
      <a-table
        :loading="loading"
        :data="sensitiveWordList"
        :columns="columns"
        :pagination="pagination"
        v-model:selected-keys="selectedKeys"
        :row-selection="rowSelection"
        row-key="id"
        @page-change="onPageChange"
        @selection-change="onSelectionChange"
      >
        <template #enabled="{ record }">
          <a-switch
            :model-value="record.enabled"
            @change="(v: boolean) => handleSwitch(record, v)"
          />
        </template>
        <template #createTime="{ record }">
          {{ formatTime(record.createTime) }}
        </template>
        <template #action="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="handleUpdate(record)"
              >修改</a-button
            >
            <a-button
              type="text"
              size="small"
              status="danger"
              @click="handleDelete(record)"
              >删除</a-button
            >
          </a-space>
        </template>
      </a-table>
    </a-card>
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      @ok="submitForm"
      @cancel="modalVisible = false"
    >
      <a-form ref="formRef" :model="form" layout="vertical">
        <a-form-item label="内容" required>
          <a-textarea
            v-model="form.content"
            placeholder="请输入敏感词内容"
            :auto-size="{ minRows: 3 }"
          />
        </a-form-item>
        <a-form-item label="是否启用">
          <a-switch v-model="form.enabled" />
        </a-form-item>
      </a-form>
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
  getImSensitiveWordList,
  addImSensitiveWord,
  updateImSensitiveWord,
  deleteImSensitiveWord,
  enableImSensitiveWord,
} from '@/api/im/sensitiveWord';
import type {
  ImSensitiveWordVo,
  ImSensitiveWordBo,
} from '@/api/im/sensitiveWord';

const { loading, setLoading } = useLoading(false);

const columns: TableColumnData[] = [
  { type: 'checkbox', width: 48 },
  { title: '内容', dataIndex: 'content', align: 'center' },
  {
    title: '状态',
    dataIndex: 'enabled',
    slotName: 'enabled',
    align: 'center',
    width: 100,
  },
  { title: '创建者', dataIndex: 'creatorName', align: 'center' },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    slotName: 'createTime',
    align: 'center',
    width: 180,
  },
  { title: '操作', slotName: 'action', align: 'center', width: 150 },
];

const rowSelection = reactive({
  type: 'checkbox',
  showCheckedAll: true,
});

const queryParams = reactive({
  content: '',
  enabled: undefined as boolean | undefined,
  pageNum: 1,
  pageSize: 10,
});

const sensitiveWordList = ref<ImSensitiveWordVo[]>([]);
const selectedKeys = ref<(string | number)[]>([]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
});

const modalVisible = ref(false);
const modalTitle = ref('新增敏感词');
const formRef = ref();
const form = reactive<ImSensitiveWordBo & { id?: number }>({
  id: undefined,
  content: '',
  enabled: true,
});

function formatTime(t: string | undefined) {
  if (!t) return '-';
  return new Date(t).toLocaleString('zh-CN', { hour12: false });
}

const fetchList = async () => {
  setLoading(true);
  try {
    const res = (await getImSensitiveWordList({
      content: queryParams.content || undefined,
      enabled: queryParams.enabled,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
    })) as {
      rows?: ImSensitiveWordVo[];
      total?: number;
      data?: { rows?: ImSensitiveWordVo[]; total?: number };
    };
    const payload = res?.rows ? res : res?.data ?? {};
    const rows = payload?.rows ?? [];
    sensitiveWordList.value = Array.isArray(rows) ? rows : [];
    pagination.total = payload?.total ?? 0;
  } catch (e) {
    Message.error('获取敏感词列表失败');
    sensitiveWordList.value = [];
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
  queryParams.enabled = undefined;
  handleQuery();
};

const onPageChange = (page: number, pageSize: number) => {
  queryParams.pageNum = page;
  queryParams.pageSize = pageSize;
  pagination.current = page;
  pagination.pageSize = pageSize;
  fetchList();
};

const onSelectionChange = (keys: (string | number)[]) => {
  selectedKeys.value = keys;
};

const handleAdd = () => {
  form.id = undefined;
  form.content = '';
  form.enabled = true;
  modalTitle.value = '新增敏感词';
  modalVisible.value = true;
};

const handleUpdate = (record: ImSensitiveWordVo) => {
  form.id = record.id;
  form.content = record.content ?? '';
  form.enabled = record.enabled ?? true;
  modalTitle.value = '修改敏感词';
  modalVisible.value = true;
};

const handleDelete = (record: ImSensitiveWordVo) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除敏感词「${record.content}」吗？`,
    onOk: async () => {
      try {
        await deleteImSensitiveWord([record.id]);
        Message.success('删除成功');
        fetchList();
      } catch (e) {
        Message.error('删除失败');
      }
    },
  });
};

const handleSwitch = async (record: ImSensitiveWordVo, value: boolean) => {
  try {
    await enableImSensitiveWord({ id: record.id, enabled: value });
    Message.success(value ? '已开启' : '已关闭');
    record.enabled = value;
  } catch (e) {
    Message.error('操作失败');
    record.enabled = !value;
  }
};

const submitForm = async () => {
  if (!form.content?.trim()) {
    Message.warning('请输入敏感词内容');
    return;
  }
  try {
    if (form.id) {
      await updateImSensitiveWord({
        id: form.id,
        content: form.content,
        enabled: form.enabled,
      });
      Message.success('修改成功');
    } else {
      await addImSensitiveWord({
        content: form.content,
        enabled: form.enabled,
      });
      Message.success('新增成功');
    }
    modalVisible.value = false;
    fetchList();
  } catch (e) {
    Message.error(form.id ? '修改失败' : '新增失败');
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
