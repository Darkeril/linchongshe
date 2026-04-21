<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['闲宝管理', '订单管理']"></Breadcrumb>
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
                <a-form-item field="orderNumber" :label="'订单号'">
                  <a-input
                    v-model="formModel.orderNumber"
                    :placeholder="'请输入订单号'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="dealStatus" :label="'交易状态'">
                  <a-select
                    v-model="formModel.dealStatus"
                    placeholder="请选择交易状态"
                    allow-clear
                  >
                    <a-option
                      v-for="dict in dealStatusOptions"
                      :key="dict.dictValue"
                      :value="dict.dictValue"
                      :label="dict.dictLabel"
                      >{{ dict.dictLabel }}
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="orderStatus" :label="'支付状态'">
                  <a-select
                    v-model="formModel.orderStatus"
                    placeholder="请选择支付状态"
                    allow-clear
                  >
                    <a-option
                      v-for="dict in orderStatusOptions"
                      :key="dict.dictValue"
                      :value="dict.dictValue"
                      :label="dict.dictLabel"
                      >{{ dict.dictLabel }}
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="payType" :label="'支付方式'">
                  <a-select
                    v-model="formModel.payType"
                    placeholder="请选择支付方式"
                    allow-clear
                  >
                    <a-option value="1" label="微信支付">微信支付</a-option>
                    <a-option value="2" label="支付宝">支付宝</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="buyerName" :label="'买家姓名'">
                  <a-input
                    v-model="formModel.buyerName"
                    :placeholder="'请输入买家姓名'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="sellerName" :label="'卖家姓名'">
                  <a-input
                    v-model="formModel.sellerName"
                    :placeholder="'请输入卖家姓名'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="dateRange" :label="'创建时间'">
                  <a-range-picker v-model="formModel.dateRange" style="width: 100%">
                  </a-range-picker>
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
              :disabled="selectedKeys.length === 0"
              @click="handleBatchDelete"
            >
              <template #icon>
                <icon-delete />
              </template>
              批量删除
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <!-- 表格 -->
      <a-table
        ref="tableRef"
        v-model:selected-keys="selectedKeys"
        row-key="id"
        :loading="loading"
        :columns="columns"
        :data="renderData"
        :pagination="pagination"
        :bordered="false"
        :row-selection="rowSelection"
        :size="'medium'"
        @select="handleSelected"
        @sorter-change="handleSorterChange"
        @page-change="onPageChange"
        @row-click="handleRowClick"
      >

        <template #actualBuyMoney="{ record }">
          <span v-if="record.actualBuyMoney" style="color: #f53f3f; font-weight: bold;">
            ¥{{ (parseFloat(record.actualBuyMoney) / 100).toFixed(2) }}
          </span>
          <span v-else style="color: #999">-</span>
        </template>

        <template #dealStatus="{ record }">
          <dict-tag
            :options="dealStatusOptions"
            :value="record.dealStatus"
          />
        </template>

        <template #orderStatus="{ record }">
          <dict-tag
            :options="orderStatusOptions"
            :value="record.orderStatus"
          />
        </template>

        <template #payType="{ record }">
          <a-tag v-if="record.payTypeName" :color="record.payType === '1' ? 'green' : 'blue'">
            {{ record.payTypeName }}
          </a-tag>
          <span v-else>-</span>
        </template>

        <template #createTime="{ record }">
          <span>{{ formatDate(record.createTime) }}</span>
        </template>

        <template #updateTime="{ record }">
          <span>{{ formatDate(record.updateTime) }}</span>
        </template>

        <template #operations="{ record }">
          <a-space>
            <a-button-group>
              <a-button type="text" size="mini" @click="handleView(record)">
                <template #icon>
                  <icon-eye />
                </template>
                查看
              </a-button>
              <a-button type="text" size="mini" @click="handleDelete(record)">
                <template #icon>
                  <icon-delete />
                </template>
                删除
              </a-button>
            </a-button-group>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 订单详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="订单详情"
      width="80%"
      :footer="false"
      :mask-closable="false"
    >
      <div v-if="currentOrder" class="order-detail">
        <a-row :gutter="24">
          <a-col :span="12">
            <div class="detail-image-container">
              <img
                v-if="currentImageSrc"
                :src="currentImageSrc"
                class="detail-image"
                @error="handleImageError"
              />
              <div v-else class="no-image-placeholder">
                <icon-image />
                <p>暂无图片</p>
                <p style="font-size: 12px; color: #ccc"
                  >调试信息: {{ getDebugInfo(currentOrder) }}</p
                >
              </div>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="detail-info">
              <h2 class="detail-title">{{ currentOrder.productTitle }}</h2>
              <a-descriptions :column="2" bordered>
                <a-descriptions-item label="订单号">
                  {{ currentOrder.orderNumber }}
                </a-descriptions-item>
                <a-descriptions-item label="商品ID">
                  {{ currentOrder.productId }}
                </a-descriptions-item>
                <a-descriptions-item label="买家姓名">
                  {{ currentOrder.buyerName }}
                </a-descriptions-item>
                <a-descriptions-item label="卖家姓名">
                  {{ currentOrder.sellerName }}
                </a-descriptions-item>
                <a-descriptions-item label="支付金额">
                  {{
                    currentOrder.actualBuyMoney
                      ? (parseFloat(currentOrder.actualBuyMoney) / 100).toFixed(2) +
                        ' 元'
                      : '-'
                  }}
                </a-descriptions-item>
                <a-descriptions-item label="原价">
                  {{
                    currentOrder.originalPrice
                      ? (parseFloat(currentOrder.originalPrice) / 100).toFixed(2) +
                        ' 元'
                      : '-'
                  }}
                </a-descriptions-item>
                <a-descriptions-item label="交易状态">
                  <dict-tag
                    :options="dealStatusOptions"
                    :value="currentOrder.dealStatus"
                  />
                </a-descriptions-item>
                <a-descriptions-item label="支付状态">
                  <dict-tag
                    :options="orderStatusOptions"
                    :value="currentOrder.orderStatus"
                  />
                </a-descriptions-item>
                <a-descriptions-item label="支付方式">
                  <a-tag v-if="currentOrder.payTypeName" :color="currentOrder.payType === '1' ? 'green' : 'blue'">
                    {{ currentOrder.payTypeName }}
                  </a-tag>
                  <span v-else>-</span>
                </a-descriptions-item>
                <a-descriptions-item label="收货地址">
                  {{ currentOrder.address || '未填写' }}
                </a-descriptions-item>
                <a-descriptions-item label="联系电话">
                  {{ currentOrder.phone || '未填写' }}
                </a-descriptions-item>
                <a-descriptions-item label="备注" :span="2">
                  {{ currentOrder.remark || '无' }}
                </a-descriptions-item>
                <a-descriptions-item label="创建时间" :span="2">
                  {{ formatDate(currentOrder.createTime) }}
                </a-descriptions-item>
                <a-descriptions-item label="更新时间" :span="2">
                  {{ formatDate(currentOrder.updateTime) }}
                </a-descriptions-item>
              </a-descriptions>
            </div>
          </a-col>
        </a-row>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, reactive, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { Message, Modal } from '@arco-design/web-vue';
import { Pagination } from '@/types/global';
import {
  IconSearch,
  IconRefresh,
  IconDelete,
  IconEye,
  IconImage,
} from '@arco-design/web-vue/es/icon';
import {
  getOrderList,
  deleteOrder,
  deleteBatchOrder,
  getOrder,
} from '@/api/idle/order';
import { getDicts } from '@/api/system/dict-data';
import addDateRange, { currentLocale } from '@/utils/common';
import { getTodayRange } from '@/utils/workbench-nav';
import DictTag from '@/components/dict-tag/index.vue';
import AImage from '@arco-design/web-vue/es/image';
import { h } from 'vue';

const route = useRoute();

const generateFormModel = () => {
  return {
    orderNumber: '',
    dealStatus: undefined,
    orderStatus: undefined,
    payType: undefined,
    buyerName: '',
    sellerName: '',
    dateRange: [],
    orderByColumn: undefined as string | undefined,
    isAsc: undefined as string | undefined,
  };
};
const formModel = ref(generateFormModel());
const loading = ref(false);
const basePagination: Pagination = {
  current: 1,
  pageSize: 10,
};
const pagination = reactive({
  ...basePagination,
});

// 详情相关状态
const detailVisible = ref(false);
const currentOrder = ref<any>(null);
const currentImageSrc = ref('');

// 字典数据
const dealStatusOptions = ref<any[]>([]);
const orderStatusOptions = ref<any[]>([]);

// 选中行
const selectedKeys = ref<number[]>([]);

const tableRef = ref();
const rowSelection = reactive({
  type: 'checkbox',
  showCheckedAll: true,
  onlyCurrent: false,
});

const columns = computed(() => [
  {
    title: '序号',
    dataIndex: 'id',
    width: 80,
    align: 'center',
  },
  {
    title: '订单号',
    dataIndex: 'orderNumber',
    width: 200,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '商品标题',
    dataIndex: 'productTitle',
    width: 200,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '商品封面',
    dataIndex: 'productCover',
    slotName: 'productCover',
    width: 100,
    align: 'center',
    render: ({ record }) =>
      h(AImage, {
        src: record.productCover,
        width: '60px',
        height: '60px',
        preview: true, // 支持图片预览
        alt: '封面',
        onClick: (e: Event) => e.stopPropagation(),
      }),
  },
  {
    title: '买家姓名',
    dataIndex: 'buyerName',
    width: 120,
    align: 'center',
  },
  {
    title: '卖家姓名',
    dataIndex: 'sellerName',
    width: 120,
    align: 'center',
  },
  {
    title: '支付金额',
    dataIndex: 'actualBuyMoney',
    slotName: 'actualBuyMoney',
    width: 120,
    align: 'center',
  },
  {
    title: '交易状态',
    dataIndex: 'dealStatus',
    slotName: 'dealStatus',
    width: 120,
    align: 'center',
  },
  {
    title: '支付状态',
    dataIndex: 'orderStatus',
    slotName: 'orderStatus',
    width: 120,
    align: 'center',
  },
  {
    title: '支付方式',
    dataIndex: 'payTypeName',
    slotName: 'payType',
    width: 120,
    align: 'center',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    slotName: 'createTime',
    sortable: {
      sortDirections: ['ascend', 'descend'],
    },
    width: 200,
    align: 'center',
    render: ({ record }) => {
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
    title: '更新时间',
    dataIndex: 'updateTime',
    slotName: 'updateTime',
    sortable: {
      sortDirections: ['ascend', 'descend'],
    },
    width: 200,
    align: 'center',
    render: ({ record }) => {
      const date = new Date(record.updateTime || record.createTime);
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
    title: '操作',
    dataIndex: 'operations',
    slotName: 'operations',
    fixed: 'right',
    width: 200,
    align: 'center',
  },
]);

const renderData = ref([]);

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '';
  return new Date(date).toLocaleString(currentLocale());
};

// 获取标签颜色
const getTagColor = (value: any) => {
  const colorMap: Record<string, string> = {
    '0': 'orange',
    '1': 'green',
    '2': 'red',
    '3': 'blue',
    '4': 'purple',
  };
  return colorMap[value] || 'gray';
};

// 获取过滤后的字典选项
const getDealStatusOptions = (value: any) => {
  if (!dealStatusOptions.value || !Array.isArray(dealStatusOptions.value)) {
    return [];
  }
  const result = dealStatusOptions.value.filter(
    (dict) =>
      dict.dictValue === value ||
      dict.dictValue === String(value) ||
      dict.dictValue === Number(value)
  );
  return result;
};

const getOrderStatusOptions = (value: any) => {
  if (!orderStatusOptions.value || !Array.isArray(orderStatusOptions.value)) {
    return [];
  }
  const result = orderStatusOptions.value.filter(
    (dict) =>
      dict.dictValue === value ||
      dict.dictValue === String(value) ||
      dict.dictValue === Number(value)
  );
  return result;
};

const fetchData = async (params: any = { current: 1, pageSize: 10 }) => {
  loading.value = true;
  try {
    // 将前端的 current 转换为后端期望的 pageNum
    const requestParams: any = {
      pageNum: params.current || 1,
      pageSize: params.pageSize || pagination.pageSize,
      orderNumber: params.orderNumber,
      dealStatus: params.dealStatus,
      orderStatus: params.orderStatus,
      payType: params.payType,
      buyerName: params.buyerName,
      sellerName: params.sellerName,
      orderByColumn: params.orderByColumn,
      isAsc: params.isAsc,
    };
    // 处理日期范围参数
    if (params.params) {
      requestParams.params = params.params;
    }
    const response = await getOrderList(requestParams);
    // 后端返回的数据结构：{ code: 200, total: 94, rows: [...], msg: "查询成功" }
    if (response.rows) {
      renderData.value = response.rows;
      pagination.total = (response as any).total || 0;
    } else if (response.data) {
      if (response.data.rows) {
        renderData.value = response.data.rows;
        pagination.total = response.data.total || 0;
      } else if (Array.isArray(response.data)) {
        renderData.value = response.data;
        pagination.total = response.data.length;
      } else {
        renderData.value = [];
        pagination.total = 0;
      }
    } else if (Array.isArray(response)) {
      renderData.value = response;
      pagination.total = response.length;
    } else {
      renderData.value = [];
      pagination.total = 0;
    }
    pagination.current = params.current || 1;
  } catch (err) {
    console.error('获取订单列表失败:', err);
    Message.error('获取数据失败');
    renderData.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

// 获取字典数据
const getDictOptions = async () => {
  try {
    const [dealStatusRes, orderStatusRes] = await Promise.all([
      getDicts('deal_status'),
      getDicts('pay_status'),
    ]);
    dealStatusOptions.value = dealStatusRes.data || dealStatusRes;
    orderStatusOptions.value = orderStatusRes.data || orderStatusRes;

    // 如果字典数据为空，使用硬编码数据（与后端枚举匹配）
    if (!dealStatusOptions.value || dealStatusOptions.value.length === 0) {
      dealStatusOptions.value = [
        { dictValue: '0', dictLabel: '临时' },
        { dictValue: '1', dictLabel: '付款中' },
        { dictValue: '2', dictLabel: '交易完成' },
        { dictValue: '3', dictLabel: '交易失败' },
        { dictValue: '4', dictLabel: '待发货' },
        { dictValue: '5', dictLabel: '待确认' },
        { dictValue: '6', dictLabel: '退货中' },
        { dictValue: '7', dictLabel: '待评价' },
        { dictValue: '8', dictLabel: '评价完成' },
      ];
    }

    if (!orderStatusOptions.value || orderStatusOptions.value.length === 0) {
      orderStatusOptions.value = [
        { dictValue: '0', dictLabel: '未支付' },
        { dictValue: '1', dictLabel: '待支付' },
        { dictValue: '2', dictLabel: '支付中' },
        { dictValue: '3', dictLabel: '已支付' },
        { dictValue: '4', dictLabel: '交易完成' },
      ];
    }
  } catch (error) {
    console.error('获取字典数据失败:', error);
    // 使用硬编码数据作为备用（与后端枚举匹配）
    dealStatusOptions.value = [
      { dictValue: '0', dictLabel: '临时' },
      { dictValue: '1', dictLabel: '付款中' },
      { dictValue: '2', dictLabel: '交易完成' },
      { dictValue: '3', dictLabel: '交易失败' },
      { dictValue: '4', dictLabel: '待发货' },
      { dictValue: '5', dictLabel: '待确认' },
      { dictValue: '6', dictLabel: '退货中' },
      { dictValue: '7', dictLabel: '待评价' },
      { dictValue: '8', dictLabel: '评价完成' },
    ];
    orderStatusOptions.value = [
      { dictValue: '0', dictLabel: '未支付' },
      { dictValue: '1', dictLabel: '待支付' },
      { dictValue: '2', dictLabel: '支付中' },
      { dictValue: '3', dictLabel: '已支付' },
      { dictValue: '4', dictLabel: '交易完成' },
    ];
  }
};

const search = () => {
  pagination.current = 1;
  const params: any = {
    current: 1,
    pageSize: pagination.pageSize,
    orderNumber: formModel.value.orderNumber,
    dealStatus: formModel.value.dealStatus,
    orderStatus: formModel.value.orderStatus,
    payType: formModel.value.payType,
    buyerName: formModel.value.buyerName,
    sellerName: formModel.value.sellerName,
    orderByColumn: formModel.value.orderByColumn,
    isAsc: formModel.value.isAsc,
  };
  if (formModel.value.dateRange && formModel.value.dateRange.length === 2) {
    const dateRangeParams = addDateRange({}, formModel.value.dateRange);
    if (dateRangeParams.params) {
      params.params = dateRangeParams.params;
    }
  }
  fetchData(params);
};

const onPageChange = (current: number) => {
  const params: any = {
    current,
    pageSize: pagination.pageSize,
    orderNumber: formModel.value.orderNumber,
    dealStatus: formModel.value.dealStatus,
    orderStatus: formModel.value.orderStatus,
    payType: formModel.value.payType,
    buyerName: formModel.value.buyerName,
    sellerName: formModel.value.sellerName,
    orderByColumn: formModel.value.orderByColumn,
    isAsc: formModel.value.isAsc,
  };
  if (formModel.value.dateRange && formModel.value.dateRange.length === 2) {
    const dateRangeParams = addDateRange({}, formModel.value.dateRange);
    if (dateRangeParams.params) {
      params.params = dateRangeParams.params;
    }
  }
  fetchData(params);
};

const onPageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize;
  const params: any = {
    current: 1,
    pageSize,
    orderNumber: formModel.value.orderNumber,
    dealStatus: formModel.value.dealStatus,
    orderStatus: formModel.value.orderStatus,
    payType: formModel.value.payType,
    buyerName: formModel.value.buyerName,
    sellerName: formModel.value.sellerName,
    orderByColumn: formModel.value.orderByColumn,
    isAsc: formModel.value.isAsc,
  };
  if (formModel.value.dateRange && formModel.value.dateRange.length === 2) {
    const dateRangeParams = addDateRange({}, formModel.value.dateRange);
    if (dateRangeParams.params) {
      params.params = dateRangeParams.params;
    }
  }
  fetchData(params);
};

const reset = () => {
  formModel.value = generateFormModel();
  pagination.current = 1;
  search();
};

// 排序触发事件
const handleSorterChange = (
  dataIndex: string,
  direction: string | undefined
) => {
  if (!direction) {
    // 如果没有排序方向，清除排序参数
    formModel.value.orderByColumn = undefined;
    formModel.value.isAsc = undefined;
  } else {
    // 设置排序字段和排序方向
    if (dataIndex === 'createTime') {
      // 创建时间排序
      formModel.value.orderByColumn = 'createTime';
    } else if (dataIndex === 'updateTime') {
      // 更新时间排序
      formModel.value.orderByColumn = 'updateTime';
    }

    // 设置排序方向：将 Arco Design 的排序方向转换为后端需要的 asc/desc
    const directionStr = String(direction || '')
      .toLowerCase()
      .trim();

    // 使用 startsWith 判断，可以处理 ascend, ascending, asc, ascing 等所有以 asc 开头的值
    if (directionStr.startsWith('asc')) {
      formModel.value.isAsc = 'asc';
    }
    // 使用 startsWith 判断，可以处理 descend, descending, desc 等所有以 desc 开头的值
    else if (directionStr.startsWith('desc')) {
      formModel.value.isAsc = 'desc';
    } else {
      // 默认降序
      formModel.value.isAsc = 'desc';
    }
  }
  search();
};

// 选中行事件
const handleSelected = (rowKeys: (string | number)[]) => {
  selectedKeys.value = rowKeys as number[];
};

// 查看订单详情
const handleView = async (record: any) => {
  try {
    const response = await getOrder(record.id);
    console.log('订单详情响应:', response);
    currentOrder.value = response.data || response;
    console.log('当前订单数据:', currentOrder.value);

    // 直接使用 productCover 字段
    if (currentOrder.value.productCover) {
      currentImageSrc.value = currentOrder.value.productCover;
      console.log('订单图片:', currentOrder.value.productCover);
    } else {
      currentImageSrc.value = '';
      console.log('订单无图片');
    }

    detailVisible.value = true;
  } catch (error) {
    console.error('获取订单详情失败:', error);
    Message.error('获取订单详情失败');
  }
};

const handleRowClick = (record: any) => {
  handleView(record);
};

// 删除订单
const handleDelete = (record: any) => {
  const ids = record.id;
  Modal.confirm({
    title: '系统提示',
    content: `是否确认删除订单编号为"${ids}"的数据项？`,
    onOk: async () => {
      try {
        await deleteOrder(record.id);
        Message.success('删除成功');
        search();
      } catch (error) {
        Message.error('删除失败');
      }
    },
  });
};

// 批量删除
const handleBatchDelete = () => {
  if (selectedKeys.value.length === 0) {
    Message.warning('请选择要删除的订单');
    return;
  }
  const ids = selectedKeys.value.join(',');
  Modal.confirm({
    title: '系统提示',
    content: `是否确认删除订单编号为"${ids}"的数据项？`,
    onOk: async () => {
      try {
        await deleteBatchOrder(selectedKeys.value);
        Message.success('批量删除成功');
        selectedKeys.value = [];
        search();
      } catch (error) {
        Message.error('批量删除失败');
      }
    },
  });
};

// 处理图片加载错误
const handleImageError = (event: any) => {
  console.error('图片加载失败:', event);
  Message.warning('图片加载失败，请检查图片路径');
};

// 获取调试信息
const getDebugInfo = (order: any) => {
  if (!order) return '订单数据为空';

  const imageFields = [
    'productCover',
    'cover',
    'image',
    'picture',
    'img',
    'productImage',
    'productPicture',
  ];
  const foundFields = imageFields.filter((field) => order[field]);

  return foundFields.length > 0
    ? `找到字段: ${foundFields.join(', ')}`
    : '未找到图片字段';
};

onMounted(() => {
  if (route.query.today === '1') {
    formModel.value.dateRange = getTodayRange();
  }
  search();
  getDictOptions();
});
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}
.general-card {
  min-height: 395px;
}

.order-detail {
  .detail-image-container {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 300px;
    border-radius: 8px;
    background: #f5f5f5;
  }

  .detail-image {
    max-width: 100%;
    max-height: 100%;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    cursor: pointer;
  }

  .no-image-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    color: #999;
    font-size: 14px;

    .arco-icon {
      font-size: 48px;
      margin-bottom: 8px;
    }

    p {
      margin: 0;
      color: #999;
    }
  }

  .detail-info {
    .detail-title {
      margin: 0 0 16px 0;
      font-size: 24px;
      font-weight: 600;
      color: #1a1a1a;
    }
  }
}

:deep(.arco-table-tbody .arco-table-tr) {
  cursor: pointer;
}
</style>