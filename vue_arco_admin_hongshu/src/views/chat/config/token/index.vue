<template>
  <div class="container">
    <Breadcrumb
      :items="[
        $t('menu.chat'),
        $t('menu.chat.config'),
        $t('menu.chat.config.token'),
      ]"
    ></Breadcrumb>
    <a-card
      class="general-card r-nav-card"
      :bordered="false"
      :body-style="{ padding: '15px' }"
    >
      <a-row>
        <a-col :flex="1">
          <a-form
            :model="query"
            :label-col-props="{ span: 6 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="left"
            auto-label-width
            @submit.prevent
          >
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="name" :label="$t('biz.keyword')">
                  <a-input
                    v-model="query.name"
                    :placeholder="$t('biz.tokenKeywordPlaceholder')"
                    allow-clear
                    @press-enter="handleQuery"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 42px" direction="vertical" />
        <a-col :flex="'none'" style="text-align: right">
          <a-space :size="18" wrap>
            <a-button type="primary" @click="handleQuery">{{ $t('common.search') }}</a-button>
            <a-button @click="resetQuery">{{ $t('common.reset') }}</a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-divider style="margin: 0 0 12px" />

      <a-row style="margin-bottom: 8px">
        <a-col :span="12">
          <a-space>
            <a-button type="primary" @click="openForm()">
              <template #icon><icon-plus /></template>
              {{ $t('common.add') }}
            </a-button>
            <a-button
              status="danger"
              :disabled="selectedRowKeys.length <= 0"
              @click="handleBatchDelete"
            >
              {{ $t('common.delete') }}
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-table
        ref="tableRef"
        v-model:selected-keys="selectedRowKeys"
        row-key="id"
        :loading="loading"
        :data="records || []"
        :columns="columns"
        :pagination="pagination"
        :row-selection="rowSelection"
        :bordered="false"
        :size="'medium'"
        :stripe="true"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
      >
        <template #appKey="{ record }">
          <a-tooltip :content="record.appKey || '-'">
            <div class="text-ellipsis" style="max-width: 200px">
              {{ formatKey(record.appKey) }}
            </div>
          </a-tooltip>
        </template>
        <template #modelName="{ record }">
          <a-tooltip :content="record.modelName || '-'">
            <span
              class="text-ellipsis"
              style="max-width: 140px; display: inline-block"
              >{{ record.modelName || '—' }}</span
            >
          </a-tooltip>
        </template>
        <template #status="{ record }">
          <a-tag v-if="Number(record.status) === 1" color="green">{{ $t('biz.enable') }}</a-tag>
          <a-tag v-else color="red">{{ $t('biz.disable') }}</a-tag>
        </template>
        <template #createTime="{ record }">
          <span>{{ formatTime(record.createTime) }}</span>
        </template>
        <template #operations="{ record }">
          <a-space>
            <a-button type="text" size="mini" @click="openForm(record)">
              <template #icon><icon-edit /></template>
              {{ $t('common.edit') }}
            </a-button>
            <a-button
              type="text"
              size="mini"
              status="danger"
              @click="handleDelete(record)"
            >
              <template #icon><icon-delete /></template>
              {{ $t('common.delete') }}
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="formVisible"
      :title="formTitle"
      width="560px"
      :ok-loading="submitting"
      @ok="submitForm"
      @cancel="closeForm"
    >
      <a-alert v-if="isEdit" type="info" style="margin-bottom: 12px">
        {{ $t('biz.tokenKeyMaskedHint') }}
      </a-alert>
      <a-form
        ref="formRef"
        :model="formData"
        :rules="dynamicRules"
        layout="vertical"
      >
        <a-form-item :label="$t('biz.remark')" field="remark">
          <a-input
            v-model="formData.remark"
            :placeholder="$t('biz.remarkDescPlaceholder')"
            allow-clear
          />
        </a-form-item>
        <a-form-item :label="$t('biz.appId')" field="appId">
          <a-input v-model="formData.appId" :placeholder="$t('biz.optional')" allow-clear />
        </a-form-item>
        <a-form-item :label="$t('biz.appKey')" field="appKey">
          <a-input-password
            v-model="formData.appKey"
            :placeholder="isEdit ? $t('biz.leaveBlankNoChange') : $t('biz.pleaseInputAppKey')"
            allow-clear
          />
        </a-form-item>
        <a-form-item :label="$t('biz.appSecret')" field="appSecret">
          <a-input-password
            v-model="formData.appSecret"
            :placeholder="isEdit ? $t('biz.leaveBlankNoChange') : $t('biz.pleaseInputAppSecret')"
            allow-clear
          />
        </a-form-item>
        <a-form-item :label="$t('biz.relatedModel')" field="model">
          <a-select
            v-model="formData.model"
            :placeholder="$t('biz.selectBigModel')"
            allow-clear
            allow-search
            :loading="modelListLoading"
            :filter-option="filterModelOption"
          >
            <a-option
              v-for="m in modelList"
              :key="String(m.id ?? m.model)"
              :value="m.model"
              :label="`${m.name || ''} ${m.model || ''}`"
            >
              {{ m.name || m.model }} ({{ m.model }})
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="$t('biz.totalTokenQuota')" field="totalTokens">
          <a-input-number
            v-model="formData.totalTokens"
            :min="0"
            :max="999999999999"
            style="width: 100%"
            :placeholder="$t('biz.totalTokenHint')"
          />
        </a-form-item>
        <a-form-item :label="$t('biz.status')" field="status">
          <a-radio-group v-model="formData.status" type="button">
            <a-radio :value="1">{{ $t('biz.enable') }}</a-radio>
            <a-radio :value="0">{{ $t('biz.disable') }}</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Message } from '@arco-design/web-vue';
import type { FormInstance } from '@arco-design/web-vue';
import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
import { listModel } from '@/api/chat/model';
import {
  pageOpenkey,
  getOpenkey,
  saveOpenkey,
  updateOpenkey,
  delOpenkey,
  type ChatOpenkeyPayload,
} from '@/api/chat/openkey';

const { t } = useI18n();

const loading = ref(false);
const records = ref<any[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const selectedRowKeys = ref<(number | string)[]>([]);

const query = reactive({ current: 1, size: 10, name: '' });

const rowSelection = {
  type: 'checkbox' as const,
  showCheckedAll: true,
  onlyCurrent: false,
};

const formVisible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const isEdit = ref(false);

const formData = reactive<
  ChatOpenkeyPayload & { appKey?: string; appSecret?: string }
>({
  remark: '',
  appId: '',
  appKey: '',
  appSecret: '',
  model: '',
  totalTokens: 0,
  status: 1,
});

const formTitle = computed(() =>
  isEdit.value ? t('biz.editTokenConfig') : t('biz.addTokenConfig')
);

const baseRules = computed(() => ({
  model: [{ required: true, message: t('biz.pleaseSelectRelatedModel') }],
}));

const createKeyRules = computed(() => ({
  appKey: [{ required: true, message: t('biz.pleaseInputAppKey') }],
  appSecret: [{ required: true, message: t('biz.pleaseInputAppSecret') }],
}));

const dynamicRules = computed(() =>
  isEdit.value
    ? { ...baseRules.value }
    : { ...baseRules.value, ...createKeyRules.value }
);

const modelList = ref<any[]>([]);
const modelListLoading = ref(false);

function unwrapModelList(res: any): any[] {
  const d = res?.data;
  if (Array.isArray(d)) return d;
  return [];
}

function loadModelList() {
  modelListLoading.value = true;
  listModel()
    .then((res: any) => {
      modelList.value = unwrapModelList(res);
    })
    .finally(() => {
      modelListLoading.value = false;
    });
}

/** 历史数据模型编码在 gpt_model 中已删除时，仍能在下拉里选中 */
function ensureOrphanModelInList(modelCode: string) {
  const code = (modelCode || '').trim();
  if (!code) return;
  if (modelList.value.some((m) => m.model === code)) return;
  modelList.value = [
    ...modelList.value,
    { id: `orphan-${code}`, name: t('biz.modelNotInLibrary'), model: code },
  ];
}

function filterModelOption(inputValue: string, option: { label?: string }) {
  return (option?.label ?? '')
    .toLowerCase()
    .includes(String(inputValue ?? '').toLowerCase());
}

const columns = computed<TableColumnData[]>(() => [
  {
    title: t('biz.serialNo'),
    dataIndex: 'id',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.modelName'),
    dataIndex: 'modelName',
    slotName: 'modelName',
    align: 'center',
    width: 140,
  },
  {
    title: t('biz.modelCode'),
    dataIndex: 'model',
    align: 'center',
    width: 120,
    ellipsis: true,
    tooltip: true,
  },
  {
    title: t('biz.appId'),
    dataIndex: 'appId',
    align: 'center',
    width: 100,
    ellipsis: true,
    tooltip: true,
  },
  {
    title: t('biz.keyColumn'),
    dataIndex: 'appKey',
    slotName: 'appKey',
    align: 'center',
    width: 200,
  },
  { title: t('biz.totalToken'), dataIndex: 'totalTokens', align: 'center', width: 100 },
  { title: t('biz.usedToken'), dataIndex: 'usedTokens', align: 'center', width: 100 },
  {
    title: t('biz.surplusToken'),
    dataIndex: 'surplusTokens',
    align: 'center',
    width: 100,
  },
  {
    title: t('biz.status'),
    dataIndex: 'status',
    slotName: 'status',
    align: 'center',
    width: 90,
  },
  {
    title: t('biz.remark'),
    dataIndex: 'remark',
    width: 100,
    align: 'center',
    ellipsis: true,
    tooltip: true,
  },
  {
    title: t('biz.createTime'),
    dataIndex: 'createTime',
    slotName: 'createTime',
    align: 'center',
    width: 170,
  },
  {
    title: t('biz.operation'),
    dataIndex: 'operations',
    slotName: 'operations',
    align: 'center',
    width: 140,
    fixed: 'right',
  },
]);

function fetchList() {
  loading.value = true;
  const params: any = {
    current: pagination.current,
    size: pagination.pageSize,
    ...query,
  };
  pageOpenkey(params)
    .then((res: any) => {
      const root = res && (res.data !== undefined ? res.data : res);
      const payload =
        (root && (root.data !== undefined ? root.data : root)) || {};
      const list = Array.isArray((payload as any).records)
        ? (payload as any).records
        : [];
      records.value = list;
      const pageTotal =
        typeof (payload as any).total === 'number'
          ? (payload as any).total
          : list.length;
      pagination.total = pageTotal;
    })
    .finally(() => {
      loading.value = false;
    });
}

function handleQuery() {
  query.current = 1;
  pagination.current = 1;
  fetchList();
}

function resetQuery() {
  Object.assign(query, { name: '' });
  handleQuery();
}

function onPageChange(page: number) {
  pagination.current = page;
  selectedRowKeys.value = [];
  fetchList();
}

function onPageSizeChange(size: number) {
  pagination.pageSize = size;
  pagination.current = 1;
  selectedRowKeys.value = [];
  fetchList();
}

function resetFormModel() {
  isEdit.value = false;
  Object.assign(formData, {
    id: undefined,
    remark: '',
    appId: '',
    appKey: '',
    appSecret: '',
    model: '',
    totalTokens: 0,
    status: 1,
  });
  formRef.value?.clearValidate();
}

function openForm(record?: any) {
  resetFormModel();
  if (record?.id) {
    isEdit.value = true;
    getOpenkey(record.id)
      .then((res: any) => {
        const row = res?.data;
        if (row && typeof row === 'object') {
          formData.id = row.id;
          formData.remark = row.remark ?? '';
          formData.appId = row.appId ?? '';
          formData.appKey = '';
          formData.appSecret = '';
          formData.model = row.model ?? '';
          formData.totalTokens =
            row.totalTokens !== undefined && row.totalTokens !== null
              ? Number(row.totalTokens)
              : 0;
          formData.status = Number(row.status) === 0 ? 0 : 1;
          ensureOrphanModelInList(formData.model);
        }
        formVisible.value = true;
      })
      .catch(() => {
        Message.error(t('biz.getConfigDetailFailed'));
      });
  } else {
    if (modelList.value.length === 0) {
      loadModelList();
    }
    formVisible.value = true;
  }
}

function closeForm() {
  formVisible.value = false;
}

function buildPayload(): ChatOpenkeyPayload {
  const p: ChatOpenkeyPayload = {
    remark: formData.remark?.trim() || undefined,
    appId: formData.appId?.trim() || undefined,
    model: formData.model?.trim() || '',
    totalTokens: formData.totalTokens ?? 0,
    status: formData.status,
  };
  if (formData.id) {
    p.id = formData.id;
  }
  const k = formData.appKey?.trim();
  const s = formData.appSecret?.trim();
  if (k) p.appKey = k;
  if (s) p.appSecret = s;
  return p;
}

function submitForm() {
  formRef.value?.validate((errors) => {
    if (errors) return;
    submitting.value = true;
    const payload = buildPayload();
    const req = isEdit.value ? updateOpenkey(payload) : saveOpenkey(payload);
    req
      .then(() => {
        Message.success(isEdit.value ? t('biz.editSuccess') : t('biz.addSuccess'));
        closeForm();
        fetchList();
      })
      .catch(() => {
        Message.error(isEdit.value ? t('biz.editFailed') : t('biz.addFailed'));
      })
      .finally(() => {
        submitting.value = false;
      });
  });
}

function handleDelete(record: any) {
  delOpenkey(record.id).then(() => {
    Message.success(t('biz.deleteSuccess'));
    fetchList();
  });
}

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) return;
  Promise.all(selectedRowKeys.value.map((id) => delOpenkey(id))).then(() => {
    Message.success(t('biz.deleteSuccess'));
    selectedRowKeys.value = [];
    fetchList();
  });
}

function formatKey(key: string) {
  if (!key) return '-';
  if (key.length > 30) {
    return `${key.substring(0, 15)}...${key.substring(key.length - 10)}`;
  }
  return key;
}

function formatTime(time: string | number) {
  if (!time) return '-';
  let timestamp: number;
  if (typeof time === 'string') {
    if (/^\d+$/.test(time)) {
      timestamp = parseInt(time, 10);
      if (timestamp >= 1000000000 && timestamp <= 9999999999) {
        timestamp *= 1000;
      }
    } else {
      const parsed = Date.parse(time);
      if (!Number.isNaN(parsed)) {
        timestamp = parsed;
      } else {
        return time;
      }
    }
  } else {
    timestamp = time;
    if (timestamp >= 1000000000 && timestamp <= 9999999999) {
      timestamp *= 1000;
    }
  }
  const date = new Date(timestamp);
  if (Number.isNaN(date.getTime())) return '-';
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const h = String(date.getHours()).padStart(2, '0');
  const min = String(date.getMinutes()).padStart(2, '0');
  const s = String(date.getSeconds()).padStart(2, '0');
  return `${y}-${m}-${d} ${h}:${min}:${s}`;
}

onMounted(() => {
  loadModelList();
});

fetchList();
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px;
  box-sizing: border-box;
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}
</style>
