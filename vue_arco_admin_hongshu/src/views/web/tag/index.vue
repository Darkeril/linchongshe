<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb
      :items="[$t('menu.web.note'), $t('menu.web.tag')]"
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
                <a-form-item field="title" :label="$t('biz.tagName')">
                  <a-input
                    v-model="state.queryParams.title"
                    :placeholder="$t('biz.enterTagName')"
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
        <a-divider style="height: 42px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space :size="18">
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
                v-permission="['web:tag:add']"
                type="primary"
                @click="handleAdd"
              >
                <template #icon>
                  <IconPlus />
                </template>
                {{ $t('common.add') }}
              </a-button>
              <!-- <a-button
                v-permission="['web:tag:export']"
                type="outline"
                @click="handleExport"
              >
                <template #icon>
                  <IconDownload />
                </template>
                导出
              </a-button> -->
              <a-button
                v-permission="['web:tag:remove']"
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
        @sorter-change="handleSorterChange"
      >
        <template #tagName="{ record }">
          <div class="tag-display">
            <a-tag :color="getTagColor(record.title)" class="tag-preview">
              <template #icon>
                <IconTag />
              </template>
              {{ record.title || '-' }}
            </a-tag>
            <span class="tag-text">{{ record.title || '-' }}</span>
          </div>
        </template>
        <template #useCount="{ record }">
          <a-statistic
            :value="record.likeCount || 0"
            :value-style="{ color: '#1890ff' }"
          >
            <template #suffix>
              <span style="font-size: 12px; color: #999">{{
                $t('common.times')
              }}</span>
            </template>
          </a-statistic>
        </template>
        <template #creator="{ record }">
          <a-tag v-if="record.creator === 'system'" color="blue">
            <template #icon>
              <IconRobot />
            </template>
            {{ $t('biz.systemBuiltin') }}
          </a-tag>
          <span v-else>{{ record.creator || '-' }}</span>
        </template>
        <template #sort="{ record }">
          <a-input-number
            v-model="record.sort"
            :min="0"
            :max="999"
            size="small"
            style="width: 80px"
            @change="handleSortChange(record)"
          />
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
                v-permission="['web:tag:edit']"
                type="text"
                size="mini"
                @click="handleUpdate(record)"
              >
                <template #icon>
                  <IconEdit />
                </template>
                {{ $t('biz.modify') }}
              </a-button>
              <a-button
                v-permission="['web:tag:remove']"
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

    <!-- 标签详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      :title="$t('biz.tagDetailTitle')"
      width="600px"
      :footer="false"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item :label="$t('biz.tagName')">
          <a-tag :color="getTagColor(currentRecord.title)" size="large">
            <template #icon><IconTag /></template>
            {{ currentRecord.title }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.tagUseCount')">
          <a-statistic
            :value="currentRecord.likeCount || 0"
            :value-style="{ color: '#1890ff' }"
          >
            <template #suffix>
              <span style="font-size: 12px; color: #999">{{
                $t('common.times')
              }}</span>
            </template>
          </a-statistic>
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.sort')">
          {{ currentRecord.sort || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.creator')">
          <a-tag v-if="currentRecord.creator === 'system'" color="blue">
            <template #icon><IconRobot /></template>
            {{ $t('biz.systemBuiltin') }}
          </a-tag>
          <span v-else>{{ currentRecord.creator || '-' }}</span>
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.createTime')">
          {{
            currentRecord.createTime
              ? new Date(currentRecord.createTime).toLocaleDateString('zh-CN', {
                  year: 'numeric',
                  month: '2-digit',
                  day: '2-digit',
                  hour: '2-digit',
                  minute: '2-digit',
                  second: '2-digit',
                  hour12: false,
                })
              : '-'
          }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.updateTimeColumn')">
          {{
            currentRecord.updateTime
              ? new Date(currentRecord.updateTime).toLocaleDateString('zh-CN', {
                  year: 'numeric',
                  month: '2-digit',
                  day: '2-digit',
                  hour: '2-digit',
                  minute: '2-digit',
                  second: '2-digit',
                  hour12: false,
                })
              : '-'
          }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 新增/编辑标签弹窗 -->
    <a-modal
      v-model:visible="formVisible"
      :title="formTitle"
      width="600px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
      >
        <a-form-item :label="$t('biz.tagName')" field="title">
          <a-input
            v-model="formData.title"
            :placeholder="$t('biz.enterTagName')"
            @input="updateTagPreview"
          />
          <div class="tag-preview-container">
            <span class="preview-label">{{ $t('biz.previewEffect') }}</span>
            <a-tag
              :color="getTagColor(formData.title)"
              class="tag-preview-large"
            >
              <template #icon><IconTag /></template>
              {{ formData.title || $t('biz.tagName') }}
            </a-tag>
          </div>
        </a-form-item>
        <a-form-item :label="$t('biz.sort')" field="sort">
          <a-input-number
            v-model="formData.sort"
            :min="0"
            :max="999"
            :placeholder="$t('biz.enterSortValue')"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Message, Modal } from '@arco-design/web-vue';
import {
  IconSearch,
  IconRefresh,
  IconDelete,
  IconPlus,
  IconEye,
  IconEdit,
  IconDownload,
  IconTag,
  IconCheck,
  IconStop,
  IconRobot,
} from '@arco-design/web-vue/es/icon';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import { tagList, addTag, delTag, getTag, updateTag } from '@/api/web/tag';

const { t } = useI18n();
const loading = ref(false);
const selectedKeys = ref<(string | number)[]>([]);
const detailVisible = ref(false);
const formVisible = ref(false);
const formTitle = ref('');
const currentRecord = ref<any>({});
const formRef = ref();

// 表格行选择配置
const rowSelection = reactive({
  type: 'checkbox' as const,
  showCheckedAll: true,
  onlyCurrent: false,
  selectedRowKeys: selectedKeys,
  onChange: (keys: (string | number)[]) => {
    selectedKeys.value = keys;
  },
});

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 表单数据
const formData = reactive({
  id: null,
  title: '',
  sort: 0,
});

// 表单验证规则
const formRules = computed(() => ({
  title: [
    { required: true, message: t('biz.enterTagName') },
    { min: 1, max: 20, message: t('biz.tagNameLengthRule') },
  ],
  sort: [
    { type: 'number', min: 0, max: 999, message: t('biz.sortRangeRule') },
  ],
}));

// 响应式状态
const state = reactive({
  queryParams: {
    title: '',
    dateRange: [] as string[],
    orderByColumn: 'createTime' as string | undefined,
    isAsc: 'desc' as string | undefined,
  },
  list: [] as any[],
});

const columns = computed(() => [
  {
    title: t('biz.idColumn'),
    dataIndex: 'id',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.tagName'),
    dataIndex: 'title',
    slotName: 'tagName',
    width: 120,
    align: 'center',
  },
  {
    title: t('biz.tagUseCount'),
    dataIndex: 'likeCount',
    slotName: 'useCount',
    width: 120,
    align: 'center',
  },
  {
    title: t('biz.sort'),
    dataIndex: 'sort',
    slotName: 'sort',
    width: 120,
    align: 'center',
  },
  {
    title: t('biz.creator'),
    dataIndex: 'creator',
    slotName: 'creator',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.createTime'),
    dataIndex: 'createTime',
    slotName: 'createTime',
    sortable: {
      sortDirections: ['ascend', 'descend'],
    },
    width: 200,
    align: 'center' as const,
    render: ({ record }: { record: any }) => {
      if (!record.createTime) return '-';
      const date = new Date(record.createTime);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false, // 24小时制
      });
    },
  },
  {
    title: t('biz.updateTimeColumn'),
    dataIndex: 'updateTime',
    slotName: 'updateTime',
    sortable: {
      sortDirections: ['ascend', 'descend'],
    },
    width: 200,
    align: 'center' as const,
    render: ({ record }: { record: any }) => {
      if (!record.updateTime) return '-';
      const date = new Date(record.updateTime);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false, // 24小时制
      });
    },
  },
  {
    title: t('biz.operation'),
    dataIndex: 'optional',
    slotName: 'optional',
    fixed: 'right',
    width: 180,
    align: 'center',
  },
]);

// 获取标签颜色
function getTagColor(tagName: string) {
  if (!tagName) return 'blue';

  const colors = [
    'red',
    'orange',
    'gold',
    'lime',
    'green',
    'cyan',
    'blue',
    'purple',
    'magenta',
    'volcano',
    'geekblue',
    'arcoblue',
  ];

  // 根据标签名称生成固定颜色
  let hash = 0;
  for (let i = 0; i < tagName.length; i += 1) {
    hash = tagName.charCodeAt(i) + (hash * 32 - hash);
  }
  return colors[Math.abs(hash) % colors.length];
}

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

  tagList(params)
    .then((res: any) => {
      // 兼容多种数据结构
      const data = res?.data || res || {};
      const records = data.rows || data.records || data.list || [];
      const total = data.total || records.length;

      state.list = records;
      pagination.total = total;
    })
    .catch((error) => {
      console.error('获取标签列表失败:', error);
      Message.error(t('biz.fetchListFailed'));
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
    title: '',
    dateRange: [],
    orderByColumn: undefined,
    isAsc: undefined,
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

// 排序触发事件
function handleSorterChange(dataIndex: string, direction: string | undefined) {
  if (!direction) {
    // 如果没有排序方向，清除排序参数
    state.queryParams.orderByColumn = undefined;
    state.queryParams.isAsc = undefined;
  } else {
    // 设置排序字段和排序方向
    if (dataIndex === 'createTime') {
      // 创建时间排序
      state.queryParams.orderByColumn = 'createTime';
    } else if (dataIndex === 'updateTime') {
      // 更新时间排序
      state.queryParams.orderByColumn = 'updateTime';
    }

    // 设置排序方向：将 Arco Design 的排序方向转换为后端需要的 asc/desc
    const directionStr = String(direction || '')
      .toLowerCase()
      .trim();

    // 使用 startsWith 判断，可以处理 ascend, ascending, asc, ascing 等所有以 asc 开头的值
    if (directionStr.startsWith('asc')) {
      state.queryParams.isAsc = 'asc';
    }
    // 使用 startsWith 判断，可以处理 descend, descending, desc 等所有以 desc 开头的值
    else if (directionStr.startsWith('desc')) {
      state.queryParams.isAsc = 'desc';
    } else {
      // 默认降序
      state.queryParams.isAsc = 'desc';
    }
  }
  fetchData();
}

// 查看详情
function handleView(record: any) {
  currentRecord.value = { ...record };
  detailVisible.value = true;
}

// 新增
function handleAdd() {
  Object.assign(formData, {
    id: null,
    title: '',
    sort: 0,
  });
  formTitle.value = t('biz.addTagTitle');
  formVisible.value = true;
}

// 编辑
function handleUpdate(record: any) {
  if (record.id) {
    getTag(record.id)
      .then((res: any) => {
        const data = res?.data || res || {};
        Object.assign(formData, {
          id: data.id,
          title: data.title || '',
          sort: data.sort || 0,
        });
        formTitle.value = t('biz.editTagTitle');
        formVisible.value = true;
      })
      .catch((error: any) => {
        console.error('获取标签详情失败:', error);
        Message.error(
          error?.response?.data?.msg ||
            error?.message ||
            t('biz.fetchTagDetailFailed')
        );
      });
  }
}

// 删除单条记录
function handleDelete(record: any) {
  Modal.confirm({
    title: t('biz.deleteConfirmTitle'),
    content: t('biz.confirmDeleteThisTag'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: () => {
      return delTag(record.id)
        .then((res: any) => {
          if (res?.code === 200 || res?.status === 200) {
            Message.success(t('biz.deleteSuccess'));
            fetchData();
            return res;
          }
          Message.error(res?.msg || res?.message || t('biz.deleteFailed'));
          return Promise.reject(
            new Error(res?.msg || res?.message || t('biz.deleteFailed'))
          );
        })
        .catch((error: any) => {
          console.error('删除失败:', error);
          Message.error(
            error?.response?.data?.msg ||
              error?.message ||
              t('biz.deleteFailed')
          );
          return Promise.reject(error);
        });
    },
  });
}

// 批量删除
function handleBatchDelete() {
  if (selectedKeys.value.length === 0) {
    Message.warning(t('biz.noSelection'));
    return;
  }

  Modal.confirm({
    title: t('biz.deleteConfirmTitle'),
    content: t('biz.confirmDeleteSelectedCount', {
      count: selectedKeys.value.length,
    }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: () => {
      // 将ID数组转换为逗号分隔的字符串，Spring MVC路径参数会自动解析为数组
      const ids = selectedKeys.value.map((id) => String(id)).join(',');
      return delTag(ids)
        .then((res: any) => {
          if (res?.code === 200 || res?.status === 200) {
            Message.success(t('biz.batchDeleteSuccess'));
            selectedKeys.value = [];
            fetchData();
            return res;
          }
          Message.error(
            res?.msg || res?.message || t('biz.batchDeleteFailed')
          );
          return Promise.reject(
            new Error(
              res?.msg || res?.message || t('biz.batchDeleteFailed')
            )
          );
        })
        .catch((error: any) => {
          console.error('批量删除失败:', error);
          Message.error(
            error?.response?.data?.msg ||
              error?.message ||
              t('biz.batchDeleteFailed')
          );
          return Promise.reject(error);
        });
    },
  });
}

// 导出
function handleExport() {
  Message.info(t('biz.exportDeveloping'));
}

// 排序变化
function handleSortChange(record: any) {
  updateTag(record)
    .then((res: any) => {
      if (res?.code === 200 || res?.status === 200) {
        Message.success(t('biz.sortUpdatedSuccess'));
      } else {
        Message.error(res?.msg || res?.message || t('biz.sortUpdateFailed'));
        fetchData(); // 恢复原数据
      }
    })
    .catch((error: any) => {
      console.error('排序更新失败:', error);
      Message.error(
        error?.response?.data?.msg ||
          error?.message ||
          t('biz.sortUpdateFailed')
      );
      fetchData(); // 恢复原数据
    });
}

// 提交表单
function handleSubmit() {
  return formRef.value?.validate().then((validateError: any) => {
    if (validateError) {
      // 验证失败，阻止关闭弹窗
      return Promise.reject(new Error(t('biz.formValidationFailed')));
    }

    const submitData = { ...formData };
    const apiCall = submitData.id ? updateTag : addTag;

    return apiCall(submitData)
      .then((res: any) => {
        if (res?.code === 200 || res?.status === 200) {
          Message.success(
            submitData.id ? t('biz.editSuccess') : t('biz.addSuccess')
          );
          formVisible.value = false;
          fetchData();
          return res;
        }
        Message.error(
          res?.msg ||
            res?.message ||
            (submitData.id ? t('biz.modifyFailed') : t('biz.addFailed'))
        );
        return Promise.reject(
          new Error(res?.msg || res?.message || t('biz.operationFailed'))
        );
      })
      .catch((apiError: any) => {
        console.error('提交失败:', apiError);
        Message.error(
          apiError?.response?.data?.msg ||
            apiError?.message ||
            (submitData.id ? t('biz.modifyFailed') : t('biz.addFailed'))
        );
        return Promise.reject(apiError);
      });
  });
}

// 取消表单
function handleCancel() {
  formVisible.value = false;
  formRef.value?.resetFields();
}

// 更新标签预览
function updateTagPreview() {
  // 实时更新预览效果
}

// 页面挂载时获取数据
onMounted(() => {
  // 默认排序：按创建时间倒序（已在 queryParams 中设置）
  fetchData();
});
</script>

<style scoped>
.container {
  padding: 0 20px 20px 20px;
}

.tag-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-preview {
  font-size: 12px;
  border-radius: 4px;
  padding: 2px 8px;
}

.tag-text {
  color: #666;
  font-size: 13px;
}

.tag-preview-container {
  margin-top: 8px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-label {
  color: #999;
  font-size: 12px;
}

.tag-preview-large {
  font-size: 14px;
  padding: 4px 22px;
}
</style>