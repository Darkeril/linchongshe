<template>
  <div class="container">
    <Breadcrumb
      :items="[$t('menu.chat'), $t('menu.chat.order.list')]"
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
              <a-col :span="6">
                <a-form-item field="tradeNo" :label="$t('biz.orderNumber')">
                  <a-input
                    v-model="query.tradeNo"
                    :placeholder="$t('biz.pleaseInputOrderNumber')"
                    allow-clear
                    @press-enter="handleQuery"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="transactionId" :label="$t('biz.channelTradeNo')">
                  <a-input
                    v-model="query.transactionId"
                    :placeholder="$t('biz.pleaseInputChannelTradeNo')"
                    allow-clear
                    @press-enter="handleQuery"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="memberId" :label="$t('biz.orderUser')">
                  <a-input
                    v-model="query.memberId"
                    :placeholder="$t('biz.pleaseInputOrderUser')"
                    allow-clear
                    @press-enter="handleQuery"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="combId" :label="$t('biz.purchasePlan')">
                  <a-input
                    v-model="query.combId"
                    :placeholder="$t('biz.pleaseInputPurchasePlan')"
                    allow-clear
                    @press-enter="handleQuery"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="chanel" :label="$t('biz.payChannel')">
                  <a-input
                    v-model="query.chanel"
                    :placeholder="$t('biz.pleaseInputPayChannel')"
                    allow-clear
                    @press-enter="handleQuery"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="status" :label="$t('biz.orderStatus')">
                  <a-input
                    v-model="query.status"
                    :placeholder="$t('biz.pleaseSelectOrderStatus')"
                    allow-clear
                    @press-enter="handleQuery"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="$t('biz.createTime')">
                  <a-range-picker
                    v-model="dateRange"
                    style="width: 100%"
                    format="YYYY-MM-DD"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 84px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space direction="vertical" :size="18">
            <a-button type="primary" @click="handleQuery">{{ $t('common.search') }}</a-button>
            <a-button @click="resetQuery">{{ $t('common.reset') }}</a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-divider style="margin: 0 0 12px" />

      <a-row style="margin-bottom: 8px">
        <a-col :span="12">
          <a-space>
            <a-button status="danger" :disabled="selectedRowKeys.length===0" @click="handleBatchDelete">{{ $t('common.delete') }}</a-button>
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
      />
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, h, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Message } from '@arco-design/web-vue';
import { pageOrder, delOrder } from '@/api/chat/order';

const { t } = useI18n();

const loading = ref(false);
const total = ref(0);
const records = ref<any[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const dateRange = ref<string[] | []>([]);
const selectedRowKeys = ref<(number | string)[]>([]);

const query = reactive({
  current: 1,
  size: 10,
  tradeNo: '',
  transactionId: '',
  memberId: '',
  combId: '',
  chanel: '',
  status: '',
});

const rowSelection = reactive({
  type: 'checkbox' as const,
  selectedRowKeys,
  onChange: (keys: (number | string)[]) => {
    selectedRowKeys.value = keys;
  },
});

function fetchList() {
  loading.value = true;
  const params: any = { ...query };
  if (Array.isArray(dateRange.value) && dateRange.value.length === 2) {
    const [beginTime, endTime] = dateRange.value as string[];
    params.beginTime = beginTime;
    params.endTime = endTime;
  }
  pageOrder(params)
    .then((res: any) => {
      const root = res && (res.data !== undefined ? res.data : res);
      const payload = root && (root.data !== undefined ? root.data : root) || {};
      const list = Array.isArray((payload as any).records) ? (payload as any).records : [];
      records.value = list;
      const pageTotal = typeof (payload as any).total === 'number' ? (payload as any).total : list.length;
      total.value = pageTotal;
      pagination.total = pageTotal;
    })
    .finally(() => {
      loading.value = false;
    });
}

function handleQuery() {
  query.current = 1;
  fetchList();
}

function resetQuery() {
  Object.assign(query, {
    tradeNo: '',
    transactionId: '',
    memberId: '',
    combId: '',
    chanel: '',
    status: '',
  });
  dateRange.value = [];
  handleQuery();
}

function onPageChange(page: number) {
  pagination.current = page;
  fetchList();
}

function onPageSizeChange(size: number) {
  pagination.pageSize = size;
  pagination.current = 1;
  fetchList();
}

function handleDelete(record: any) {
  const { id } = record;
  delOrder(id).then(() => {
    Message.success(t('biz.deleteSuccess'));
    fetchList();
  });
}

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) return;
  Promise.all(selectedRowKeys.value.map(id => delOrder(id))).then(() => {
    Message.success(t('biz.deleteSuccess'));
    selectedRowKeys.value = [];
    fetchList();
  });
}

const columns = computed(() => [
  { title: t('biz.id'), dataIndex: 'id', align: 'center', width: 100 },
  { title: t('biz.orderNumber'), dataIndex: 'tradeNo', align: 'center' },
  { title: t('biz.orderUser'), dataIndex: 'memberName', align: 'center' },
  { title: t('biz.purchasePlan'), dataIndex: 'combName', align: 'center' },
  { title: t('biz.planPrice'), dataIndex: 'price', align: 'center' },
  { title: t('biz.payChannel'), dataIndex: 'chanel', align: 'center' },
  { title: t('biz.orderStatus'), dataIndex: 'status', align: 'center' },
  { title: t('biz.orderCreateTime'), dataIndex: 'createTime', align: 'center' },
  { title: t('biz.paySuccessTime'), dataIndex: 'successTime', align: 'center' },
  {
    title: t('biz.operation'),
    align: 'center',
    width: 120,
    render: ({ record }: any) =>
      h(
        'a-popconfirm',
        {
          content: t('biz.confirmDeleteRecord'),
          onOk: () => handleDelete(record),
        },
        () => h('a-link', { status: 'danger' }, t('common.delete'))
      ),
  },
]);

fetchList();
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px;
  box-sizing: border-box;
}
</style>

