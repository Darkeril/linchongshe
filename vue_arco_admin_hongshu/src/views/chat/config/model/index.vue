<template>
  <div class="container">
    <Breadcrumb
      :items="[
        $t('menu.chat'),
        $t('menu.chat.config'),
        $t('menu.chat.config.model'),
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
                <a-form-item field="name" :label="$t('biz.modelName')">
                  <a-input
                    v-model="query.name"
                    :placeholder="$t('biz.modelNameInputPlaceholder')"
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
        <template #iconUrl="{ record }">
          <img
            v-if="tableIconSrc(record)"
            class="table-logo-img"
            :src="tableIconSrc(record)"
            alt=""
          />
          <span v-else class="text-muted">-</span>
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
      width="520px"
      :ok-loading="submitting"
      @ok="submitForm"
      @cancel="closeForm"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
      >
        <a-form-item :label="$t('biz.modelName')" field="name">
          <a-input v-model="formData.name" :placeholder="$t('biz.displayName')" allow-clear />
        </a-form-item>
        <a-form-item :label="$t('biz.modelCode')" field="model">
          <a-input
            v-model="formData.model"
            :placeholder="$t('biz.modelCodeHint')"
            allow-clear
          />
        </a-form-item>
        <a-form-item :label="$t('biz.logoIcon')" field="icon">
          <a-select
            v-model="iconSelectModel"
            allow-clear
            :placeholder="$t('biz.selectSvgIcon')"
            :style="{ width: '100%' }"
          >
            <a-option
              v-for="opt in iconSelectOptions"
              :key="opt.value"
              :value="opt.value"
            >
              <div class="icon-option-row">
                <img
                  class="icon-option-img"
                  :src="opt.url"
                  alt=""
                />
                <span>{{ opt.label }}</span>
              </div>
            </a-option>
          </a-select>
          <div v-if="resolveModelIconUrl(formData.icon)" class="logo-preview-row">
            <span class="logo-preview-label">{{ $t('biz.preview') }}</span>
            <img
              :src="resolveModelIconUrl(formData.icon)"
              alt=""
              class="logo-preview-img"
            />
          </div>
        </a-form-item>
        <a-form-item :label="$t('biz.version')" field="version">
          <a-input v-model="formData.version" :placeholder="$t('biz.optional')" allow-clear />
        </a-form-item>
        <a-form-item :label="$t('biz.sort')" field="sort">
          <a-input-number
            v-model="formData.sort"
            :min="0"
            :max="999999"
            :placeholder="$t('biz.sortPlaceholder')"
            style="width: 100%"
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
import { ref, reactive, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Message } from '@arco-design/web-vue';
import type { FormInstance } from '@arco-design/web-vue';
import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
import {
  pageModel,
  getModel,
  saveModel,
  updateModel,
  delModel,
  type ChatGptModelPayload,
} from '@/api/chat/model';

const { t } = useI18n();

/** 扫描 @/assets/svgs；入库保存文件名（如 ChatGPT.svg），列表用 resolveModelIconUrl 解析 */
const builtInSvgModules = import.meta.glob('@/assets/svgs/*.svg', {
  eager: true,
  as: 'url',
}) as Record<string, string>;

const svgUrlByFileName: Record<string, string> = {};
Object.entries(builtInSvgModules).forEach(([path, url]) => {
  const fileName = path.replace(/^.*\//, '');
  svgUrlByFileName[fileName] = url;
});

/**
 * 网关/统一返回可能套两层 { code, data: { code, data: 业务 } }，拦截器只剥一层。
 * 分页：data 为 { records, total, ... }；详情：最终落到 VO。
 */
function unwrapChatPayload(res: any): any {
  if (!res || typeof res !== 'object') return res;
  let x: any = res.data !== undefined ? res.data : res;
  while (
    x &&
    typeof x === 'object' &&
    'data' in x &&
    x.data != null &&
    typeof x.data === 'object'
  ) {
    if (Array.isArray(x.records)) break;
    x = x.data;
  }
  return x;
}

/** 将接口返回的 icon 规范成与 assets 文件名一致（大小写），便于下拉回显 */
function canonicalSvgFileName(stored: string | undefined | null): string {
  if (stored === undefined || stored === null) return '';
  const s = String(stored).trim();
  if (!s) return '';
  if (svgUrlByFileName[s]) return s;
  const base =
    s.split('/').pop()?.split('?')[0]?.split('#')[0] || s;
  const lower = base.toLowerCase();
  const matched = Object.keys(svgUrlByFileName).find(
    (fn) => fn.toLowerCase() === lower
  );
  return matched || s;
}

const iconSelectOptions = Object.keys(svgUrlByFileName)
  .sort((a, b) => {
    const la = a.replace(/\.svg$/i, '');
    const lb = b.replace(/\.svg$/i, '');
    return la.localeCompare(lb, 'en');
  })
  .map((fileName) => ({
    value: fileName,
    label: fileName.replace(/\.svg$/i, ''),
    url: svgUrlByFileName[fileName],
  }));

function resolveModelIconUrl(stored: string | undefined | null): string {
  if (stored === undefined || stored === null) return '';
  const s = String(stored).trim();
  if (!s) return '';
  const fileKey = canonicalSvgFileName(s);
  if (fileKey && svgUrlByFileName[fileKey]) {
    return svgUrlByFileName[fileKey];
  }
  if (
    /^https?:\/\//i.test(s) ||
    s.startsWith('//') ||
    s.startsWith('/assets/') ||
    s.startsWith('data:')
  ) {
    return s;
  }
  if (svgUrlByFileName[s]) return svgUrlByFileName[s];
  return s;
}

function tableIconSrc(record: { icon?: string }) {
  return resolveModelIconUrl(record?.icon);
}

const loading = ref(false);
const records = ref<any[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const selectedRowKeys = ref<(number | string)[]>([]);

const query = reactive({ current: 1, size: 10, name: '' });

/** 与会员管理页一致：勾选由表格 v-model:selected-keys 管理，勿在 rowSelection 里再绑 selectedRowKeys */
const rowSelection = {
  type: 'checkbox' as const,
  showCheckedAll: true,
  onlyCurrent: false,
};

const formVisible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const formData = reactive<ChatGptModelPayload>({
  name: '',
  model: '',
  icon: '',
  version: '',
  status: 1,
  sort: 0,
});

/** 下拉绑定内置文件名；清空时同步清空 formData.icon（含改为仅下拉场景） */
const iconSelectModel = computed({
  get() {
    const v = canonicalSvgFileName(formData.icon);
    return v && svgUrlByFileName[v] ? v : undefined;
  },
  set(val: string | undefined) {
    formData.icon = val ?? '';
  },
});

const formTitle = computed(() =>
  formData.id ? t('biz.editModel') : t('biz.addModel')
);

const formRules = computed(() => ({
  name: [{ required: true, message: t('biz.pleaseInputModelName') }],
  model: [{ required: true, message: t('biz.pleaseInputModelCode') }],
}));

const columns = computed<TableColumnData[]>(() => [
  {
    title: t('biz.serialNo'),
    dataIndex: 'id',
    width: 90,
    align: 'center',
  },
  {
    title: 'Logo',
    dataIndex: 'icon',
    slotName: 'iconUrl',
    align: 'center',
    width: 100,
  },
  { title: t('biz.modelName'), dataIndex: 'name', width: 100, align: 'center' },
  { title: t('biz.modelCode'), dataIndex: 'model', width: 100, align: 'center' },
  { title: t('biz.version'), dataIndex: 'version', align: 'center', width: 100 },
  { title: t('biz.sort'), dataIndex: 'sort', align: 'center', width: 80 },
  {
    title: t('biz.status'),
    dataIndex: 'status',
    slotName: 'status',
    align: 'center',
    width: 90,
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
    width: 160,
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
  pageModel(params)
    .then((res: any) => {
      const payload = unwrapChatPayload(res) || {};
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
  Object.assign(formData, {
    id: undefined,
    name: '',
    model: '',
    icon: '',
    version: '',
    status: 1,
    sort: 0,
  });
  formRef.value?.clearValidate();
}

function openForm(record?: any) {
  resetFormModel();
  if (record?.id) {
    getModel(record.id)
      .then((res: any) => {
        const row = unwrapChatPayload(res);
        if (row && typeof row === 'object') {
          formData.id = row.id;
          formData.name = row.name ?? '';
          formData.model = row.model ?? '';
          formData.icon = canonicalSvgFileName(row.icon ?? '');
          formData.version = row.version ?? '';
          formData.status =
            Number(row.status) === 0 ? 0 : 1;
          formData.sort =
            row.sort !== undefined && row.sort !== null ? Number(row.sort) : 0;
        }
        formVisible.value = true;
      })
      .catch(() => {
        Message.error(t('biz.getModelDetailFailed'));
      });
  } else {
    formVisible.value = true;
  }
}

function closeForm() {
  formVisible.value = false;
}

function submitForm() {
  // Arco：回调参数为 errors，通过时为 undefined，失败时为错误对象（勿当成 Element 的 valid boolean）
  formRef.value?.validate((errors) => {
    if (errors) return;
    submitting.value = true;
    const rawIcon = String(formData.icon ?? '').trim();
    const iconToSave = rawIcon
      ? canonicalSvgFileName(rawIcon) || rawIcon
      : undefined;
    const payload: ChatGptModelPayload = {
      name: formData.name?.trim() || '',
      model: formData.model?.trim() || '',
      icon: iconToSave,
      version: formData.version?.trim() || undefined,
      status: formData.status,
      sort: formData.sort,
    };
    if (formData.id) {
      payload.id = formData.id;
    }
    const req = formData.id ? updateModel(payload) : saveModel(payload);
    req
      .then(() => {
        Message.success(formData.id ? t('biz.editSuccess') : t('biz.addSuccess'));
        closeForm();
        fetchList();
      })
      .catch(() => {
        Message.error(formData.id ? t('biz.editFailed') : t('biz.addFailed'));
      })
      .finally(() => {
        submitting.value = false;
      });
  });
}

function handleDelete(record: any) {
  delModel(record.id).then(() => {
    Message.success(t('biz.deleteSuccess'));
    fetchList();
  });
}

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) return;
  Promise.all(selectedRowKeys.value.map((id) => delModel(id))).then(() => {
    Message.success(t('biz.deleteSuccess'));
    selectedRowKeys.value = [];
    fetchList();
  });
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

fetchList();
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px;
  box-sizing: border-box;
}

.text-muted {
  color: var(--color-text-3);
}

.table-logo-img {
  width: 36px;
  height: 36px;
  object-fit: contain;
  vertical-align: middle;
  border-radius: 4px;
  background: var(--color-fill-2);
}

.icon-option-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-option-img {
  width: 22px;
  height: 22px;
  object-fit: contain;
  flex-shrink: 0;
}

.logo-preview-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.logo-preview-label {
  font-size: 12px;
  color: var(--color-text-3);
}

.logo-preview-img {
  width: 48px;
  height: 48px;
  object-fit: contain;
  border-radius: 6px;
  background: var(--color-fill-2);
  display: block;
}
</style>
