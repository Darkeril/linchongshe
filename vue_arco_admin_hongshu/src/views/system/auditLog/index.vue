<template>
  <div class="container">
    <a-card class="general-card" :title="$t('biz.auditLogTitle')">
      <!-- 查询表单 -->
      <a-row>
        <a-col :flex="1">
          <a-form
            :model="formModel"
            :label-col-props="{ span: 6 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="left"
          >
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="contentId" :label="$t('biz.contentId')">
                  <a-input
                    v-model="formModel.contentId"
                    :placeholder="$t('biz.contentIdPlaceholder')"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="contentType" :label="$t('biz.contentType')">
                  <a-select
                    v-model="formModel.contentType"
                    :placeholder="$t('biz.contentTypePlaceholder')"
                    allow-clear
                  >
                    <a-option value="note">{{
                      $t('biz.contentTypeNote')
                    }}</a-option>
                    <a-option value="product">{{
                      $t('biz.contentTypeProduct')
                    }}</a-option>
                    <a-option value="comment">{{
                      $t('biz.contentTypeComment')
                    }}</a-option>
                    <a-option value="user">{{
                      $t('biz.contentTypeUser')
                    }}</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="auditResult" :label="$t('biz.auditResult')">
                  <a-select
                    v-model="formModel.auditResult"
                    :placeholder="$t('biz.auditResultPlaceholder')"
                    allow-clear
                  >
                    <a-option value="PASS">{{ $t('biz.auditPass') }}</a-option>
                    <a-option value="MANUAL">{{
                      $t('biz.auditManual')
                    }}</a-option>
                    <a-option value="REJECT">{{
                      $t('biz.auditReject')
                    }}</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item
                  field="auditProvider"
                  :label="$t('biz.auditProvider')"
                >
                  <a-input
                    v-model="formModel.auditProvider"
                    :placeholder="$t('biz.auditProviderPlaceholder')"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="startTime" :label="$t('biz.startTime')">
                  <a-date-picker
                    v-model="formModel.startTime"
                    style="width: 100%"
                    show-time
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="endTime" :label="$t('biz.endTime')">
                  <a-date-picker
                    v-model="formModel.endTime"
                    style="width: 100%"
                    show-time
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 84px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space direction="vertical" :size="18">
            <a-button type="primary" @click="search">
              <template #icon>
                <icon-search />
              </template>
              {{ $t('biz.query') }}
            </a-button>
            <a-button @click="reset">
              <template #icon>
                <icon-refresh />
              </template>
              {{ $t('common.reset') }}
            </a-button>
          </a-space>
        </a-col>
      </a-row>
      <a-divider style="margin-top: 0" />

      <!-- 数据表格 -->
      <a-table
        row-key="id"
        :loading="loading"
        :pagination="pagination"
        :columns="columns"
        :data="renderData"
        :bordered="false"
        :size="size"
        @page-change="onPageChange"
      >
        <template #contentType="{ record }">
          <a-tag v-if="record.contentType === 'note'" color="blue">{{
            $t('biz.contentTypeNote')
          }}</a-tag>
          <a-tag v-else-if="record.contentType === 'product'" color="green">{{
            $t('biz.contentTypeProduct')
          }}</a-tag>
          <a-tag v-else-if="record.contentType === 'comment'" color="purple">{{
            $t('biz.contentTypeComment')
          }}</a-tag>
          <a-tag v-else-if="record.contentType === 'user'" color="arcoblue">{{
            $t('biz.contentTypeUser')
          }}</a-tag>
          <a-tag v-else color="gray">{{ record.contentType }}</a-tag>
        </template>

        <template #auditResult="{ record }">
          <a-tag v-if="record.auditResult === 'PASS'" color="green">{{
            $t('biz.auditPass')
          }}</a-tag>
          <a-tag v-else-if="record.auditResult === 'MANUAL'" color="orange">{{
            $t('biz.auditManual')
          }}</a-tag>
          <a-tag v-else-if="record.auditResult === 'REJECT'" color="red">{{
            $t('biz.auditReject')
          }}</a-tag>
          <a-tag v-else color="gray">{{ record.auditResult }}</a-tag>
        </template>

        <template #auditScore="{ record }">
          <span v-if="record.auditScore">{{
            record.auditScore.toFixed(2)
          }}</span>
          <span v-else>-</span>
        </template>

        <template #auditTime="{ record }">
          {{ formatDateTime(record.auditTime) }}
        </template>

        <template #operations="{ record }">
          <a-button type="text" size="small" @click="viewDetail(record)">
            {{ $t('biz.viewDetail') }}
          </a-button>
        </template>
      </a-table>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      :title="$t('biz.auditLogDetailTitle')"
      width="800px"
      :footer="false"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item :label="$t('biz.logId')">
          {{ detailData.id }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.contentId')">
          {{ detailData.contentId }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.contentType')">
          <a-tag v-if="detailData.contentType === 'note'" color="blue">{{
            $t('biz.contentTypeNote')
          }}</a-tag>
          <a-tag
            v-else-if="detailData.contentType === 'product'"
            color="green"
            >{{ $t('biz.contentTypeProduct') }}</a-tag
          >
          <a-tag
            v-else-if="detailData.contentType === 'comment'"
            color="purple"
            >{{ $t('biz.contentTypeComment') }}</a-tag
          >
          <a-tag
            v-else-if="detailData.contentType === 'user'"
            color="arcoblue"
            >{{ $t('biz.contentTypeUser') }}</a-tag
          >
          <a-tag v-else color="gray">{{ detailData.contentType }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.auditResult')">
          <a-tag v-if="detailData.auditResult === 'PASS'" color="green">{{
            $t('biz.auditPass')
          }}</a-tag>
          <a-tag
            v-else-if="detailData.auditResult === 'MANUAL'"
            color="orange"
            >{{ $t('biz.auditManual') }}</a-tag
          >
          <a-tag v-else-if="detailData.auditResult === 'REJECT'" color="red">{{
            $t('biz.auditReject')
          }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.auditScore')">
          {{ detailData.auditScore ? detailData.auditScore.toFixed(2) : '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.auditProvider')">
          {{ formatAuditProvider(detailData.auditProvider) }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.auditTime')" :span="2">
          {{ formatDateTime(detailData.auditTime) }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.createTime')" :span="2">
          {{ formatDateTime(detailData.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('biz.auditReason')" :span="2">
          {{ detailData.auditReason || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, reactive } from 'vue';
import { useI18n } from 'vue-i18n';
import { Message } from '@arco-design/web-vue';
import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
import { queryAuditLogList } from '@/api/system/auditLog';
import dayjs from 'dayjs';

const { t } = useI18n();

const formModel = ref({
  contentId: '',
  contentType: '',
  auditResult: '',
  auditProvider: '',
  startTime: '',
  endTime: '',
});

const loading = ref(false);
const renderData = ref([]);
const size = ref<'medium' | 'small' | 'mini' | 'large'>('medium');
const detailVisible = ref(false);
const detailData = ref<any>({});

// 审核提供商显示格式化：common_audit / baidu_qianfan 显示为 百度千帆，其它原样
function formatAuditProvider(provider?: string) {
  if (!provider) return '-';
  if (provider === 'common_audit' || provider === 'baidu_qianfan') {
    return t('biz.baiduQianfan');
  }
  return provider;
}

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
});

const columns = computed<TableColumnData[]>(() => [
  {
    title: t('biz.logId'),
    dataIndex: 'id',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.contentType'),
    dataIndex: 'contentType',
    slotName: 'contentType',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.auditResult'),
    dataIndex: 'auditResult',
    slotName: 'auditResult',
    width: 100,
  },
  {
    title: t('biz.auditScore'),
    dataIndex: 'auditScore',
    slotName: 'auditScore',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.auditProvider'),
    dataIndex: 'auditProvider',
    width: 150,
    render: ({ record }) => formatAuditProvider((record as any).auditProvider),
    align: 'center',
  },
  {
    title: t('biz.auditTime'),
    dataIndex: 'auditTime',
    slotName: 'auditTime',
    width: 180,
    align: 'center',
  },
  {
    title: t('biz.operation'),
    slotName: 'operations',
    width: 120,
    fixed: 'right',
    align: 'center',
  },
]);

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      ...formModel.value,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    };
    const response = await queryAuditLogList(params);
    console.log('API Response:', response);
    const data = response.data || response;
    renderData.value = data.rows || data.list || [];
    pagination.total = data.total || 0;
  } catch (err: any) {
    console.error('查询失败:', err);
    Message.error(
      `${t('biz.queryFailedPrefix')}: ${err.message || t('biz.unknownError')}`
    );
  } finally {
    loading.value = false;
  }
};

const search = () => {
  pagination.current = 1;
  fetchData();
};

const reset = () => {
  formModel.value = {
    contentId: '',
    contentType: '',
    auditResult: '',
    auditProvider: '',
    startTime: '',
    endTime: '',
  };
  search();
};

const onPageChange = (current: number) => {
  pagination.current = current;
  fetchData();
};

const viewDetail = (record: any) => {
  detailData.value = record;
  detailVisible.value = true;
};

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-';
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
};

// 初始加载
fetchData();
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}
</style>
