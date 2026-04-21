<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['闲宝管理', '商品审核管理']"></Breadcrumb>
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
                <a-form-item field="cpid" :label="'分类'">
                  <a-tree-select
                    v-model="formModel.cpid"
                    :field-names="{
                      key: 'id',
                      title: 'title',
                      children: 'children',
                    }"
                    :data="navbarOptions"
                    :show-count="false"
                    placeholder="请选择分类"
                    allow-clear
                  >
                  </a-tree-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="title" :label="'标题'">
                  <a-input
                    v-model="formModel.title"
                    :placeholder="'请输入标题'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="author" :label="'作者'">
                  <a-input
                    v-model="formModel.author"
                    :placeholder="'请输入作者'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="productType" :label="'类型'">
                  <a-select
                    v-model="formModel.productType"
                    placeholder="请选择商品类型"
                    allow-clear
                  >
                    <a-option
                      v-for="dict in productTypeOptions"
                      :key="dict.dictValue"
                      :value="dict.dictValue"
                      :label="dict.dictLabel"
                      >{{ dict.dictLabel }}
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="auditStatus" :label="'状态'">
                  <a-select
                    v-model="formModel.auditStatus"
                    placeholder="请选择审核状态"
                    allow-clear
                  >
                    <a-option
                      v-for="dict in auditStatusOptions"
                      :key="dict.dictValue"
                      :value="dict.dictValue"
                      :label="dict.dictLabel"
                      >{{ dict.dictLabel }}
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="fromType" :label="'商品来源'">
                  <a-select
                    v-model="formModel.fromType"
                    placeholder="请选择商品来源"
                    allow-clear
                  >
                    <a-option :value="0" label="web端">web端</a-option>
                    <a-option :value="1" label="app端">app端</a-option>
                  </a-select>
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
              type="primary"
              :disabled="selectedKeys.length <= 0"
              @click="openBatchAudit('pass')"
              >批量通过</a-button
            >
            <a-button
              status="danger"
              :disabled="selectedKeys.length <= 0"
              @click="openBatchAudit('reject')"
              >批量驳回</a-button
            >
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
        <template #cpid="{ record }">
          <a-tag color="gray">{{ getCategoryTitle(record.cpid) }}</a-tag>
        </template>

        <template #fromType="{ record }">
          <a-tag :color="record.fromType === 0 ? 'blue' : 'green'">
            {{ record.fromType === 0 ? 'web端' : 'app端' }}
          </a-tag>
        </template>

        <template #auditStatus="{ record }">
          <dict-tag
            :options="auditStatusOptions"
            :value="record.auditStatus"
          />
        </template>

        <template #productType="{ record }">
          <dict-tag
            :options="productTypeOptions"
            :value="record.productType"
          />
        </template>

        <template #content="{ record }">
          <a-tooltip :content="record.description">
            <div style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 130px;">
              {{ record.description || '暂无内容' }}
            </div>
          </a-tooltip>
        </template>
        <template #count="{ record }">
          {{ record.count || 0 }}
        </template>

        <template #operations="{ record }">
          <a-space>
            <a-button-group>
              <a-button type="text" size="mini" @click.stop="handleView(record.id)">
                <template #icon>
                  <IconEye />
                </template>
                查看
              </a-button>
              <a-button
                type="text"
                size="mini"
                @click.stop="handleAudit(record.id, 'pass')"
              >
                通过
              </a-button>
              <a-button
                type="text"
                size="mini"
                @click.stop="handleAudit(record.id, 'reject')"
              >
                驳回
              </a-button>
            </a-button-group>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 商品详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="商品详情"
      width="80%"
      :footer="false"
      :mask-closable="false"
    >
      <div v-if="currentProduct" class="product-detail">
        <a-row :gutter="24">
          <a-col :span="12">
            <div class="detail-image-container">
              <img
                v-if="!isVideoProduct"
                :src="currentImageSrc"
                class="detail-image"
                @click="() => {}"
              />
              <video
                v-else
                :src="currentImageSrc"
                :poster="currentProduct.cover"
                controls
                class="detail-video"
              />
              <div
                v-if="!isVideoProduct && imageList.length > 1"
                class="image-navigation"
              >
                <a-button
                  type="primary"
                  shape="circle"
                  size="small"
                  :disabled="currentImageIndex === 0"
                  @click="prevImage"
                >
                  <template #icon>
                    <icon-left />
                  </template>
                </a-button>
                <a-button
                  type="primary"
                  shape="circle"
                  size="small"
                  :disabled="currentImageIndex === imageList.length - 1"
                  @click="nextImage"
                >
                  <template #icon>
                    <icon-right />
                  </template>
                </a-button>
              </div>
              <div
                v-if="!isVideoProduct && imageList.length > 1"
                class="image-counter"
              >
                {{ currentImageIndex + 1 }} / {{ imageList.length }}
              </div>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="detail-info">
              <h2 class="detail-title">{{ currentProduct.title }}</h2>
              <a-descriptions :column="2" bordered>
                <a-descriptions-item label="作者">
                  {{ currentProduct.author }}
                </a-descriptions-item>
                <a-descriptions-item label="分类">
                  {{ getCategoryTitle(currentProduct.cpid) }}
                </a-descriptions-item>
                <a-descriptions-item label="类型">
                  <dict-tag
                    :options="productTypeOptions"
                    :value="currentProduct.productType"
                  />
                </a-descriptions-item>
                <a-descriptions-item label="状态">
                  <dict-tag
                    :options="auditStatusOptions"
                    :value="currentProduct.auditStatus"
                  />
                </a-descriptions-item>
                <a-descriptions-item label="商品来源">
                  <a-tag :color="currentProduct.fromType === 0 ? 'blue' : 'green'">
                    {{ currentProduct.fromType === 0 ? 'web端' : 'app端' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="发布位置">
                  {{ currentProduct.address || '未知' }}
                </a-descriptions-item>
                <a-descriptions-item label="图片数量">
                  {{ currentProduct.count || 0 }} 张
                </a-descriptions-item>
                <a-descriptions-item label="浏览次数">
                  {{ currentProduct.viewCount || 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="收藏次数">
                  {{ currentProduct.collectionCount || 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="评论次数">
                  {{ currentProduct.commentCount || 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="创建时间" :span="2">
                  {{ formatDate(currentProduct.createTime) }}
                </a-descriptions-item>
              </a-descriptions>
              <div class="detail-description">
                <h4>商品描述</h4>
                <p>{{ currentProduct.description || '暂无描述' }}</p>
              </div>
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
  IconCheck,
  IconClose,
  IconUser,
  IconImage,
  IconLeft,
  IconRight,
  IconEye,
} from '@arco-design/web-vue/es/icon';
import {
  getProductList,
  unAuditListProduct,
  auditProduct,
  batchAuditProduct,
  getProduct,
} from '@/api/idle/product';
import { getIdleNavbarList } from '@/api/idle/navbar';
import { getDicts } from '@/api/system/dict-data';
import addDateRange, { currentLocale } from '@/utils/common';
import DictTag from '@/components/dict-tag/index.vue';
import AImage from '@arco-design/web-vue/es/image';
import { h } from 'vue';

const generateFormModel = () => {
  return {
    title: '',
    author: '',
    auditStatus: undefined,
    productType: undefined,
    fromType: undefined,
    cpid: undefined,
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
const currentProduct = ref<any>(null);
const currentImageIndex = ref(0);
const imageList = ref<string[]>([]);
const currentImageSrc = ref('');
const navbarOptions = ref<any[]>([]);
const categoriesMap = ref<Record<string, string>>({});


// 字典数据
const auditStatusOptions = ref<any[]>([]);
const productTypeOptions = ref<any[]>([]);
const fromTypeOptions = ref<any[]>([]);

// 选中行
const selectedKeys = ref<number[]>([]);

const isVideoProduct = computed(() => {
  return currentProduct.value && Number(currentProduct.value.productType) === 2;
});

// 获取分类名称
const getCategoryTitle = (pid: string) => {
  return categoriesMap.value[pid] || '未知分类';
};

const tableRef = ref();
const rowSelection = reactive({
  type: 'checkbox',
  showCheckedAll: true,
  onlyCurrent: false,
});

const columns = computed(() => [
  {
    title: '编号',
    dataIndex: 'id',
    slotName: 'id',
    width: 60,
    align: 'center',
  },
  {
    title: '分类',
    dataIndex: 'cpid',
    slotName: 'cpid',
    width: 100,
    align: 'center',
  },
  {
    title: '标题',
    dataIndex: 'title',
    slotName: 'title',
    width: 120,
    align: 'center',
  },
  {
    title: '作者',
    dataIndex: 'author',
    slotName: 'author',
    width: 100,
    align: 'center',
  },
  {
    title: '商品封面',
    dataIndex: 'cover',
    slotName: 'cover',
    width: 100,
    align: 'center',
    render: ({ record }) =>
      h(AImage, {
        src: record.cover,
        width: '60px',
        height: '60px',
        preview: true, // 支持图片预览
        alt: '封面',
        onClick: (e: Event) => e.stopPropagation(),
      }),
  },
  {
    title: '内容',
    dataIndex: 'description',
    slotName: 'content',
    width: 130,
    align: 'center',
  },
  {
    title: '图片数量',
    dataIndex: 'count',
    slotName: 'count',
    width: 100,
    align: 'center',
  },
  {
    title: '类型',
    dataIndex: 'productType',
    slotName: 'productType',
    width: 100,
    align: 'center',
  },
  {
    title: '来源',
    dataIndex: 'fromType',
    slotName: 'fromType',
    width: 100,
    align: 'center',
  },
  {
    title: '状态',
    dataIndex: 'auditStatus',
    slotName: 'auditStatus',
    width: 100,
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
    dataIndex: 'optional',
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
const getFromTypeOptions = (value: any) => {
  if (!fromTypeOptions.value || !Array.isArray(fromTypeOptions.value)) {
    return [];
  }
  const result = fromTypeOptions.value.filter(
    (dict) =>
      dict.dictValue === value ||
      dict.dictValue === String(value) ||
      dict.dictValue === Number(value)
  );
  return result;
};

const getAuditStatusOptions = (value: any) => {
  if (!auditStatusOptions.value || !Array.isArray(auditStatusOptions.value)) {
    return [];
  }
  const result = auditStatusOptions.value.filter(
    (dict) =>
      dict.dictValue === value ||
      dict.dictValue === String(value) ||
      dict.dictValue === Number(value)
  );
  return result;
};

const getProductTypeOptions = (value: any) => {
  if (!productTypeOptions.value || !Array.isArray(productTypeOptions.value)) {
    return [];
  }
  const result = productTypeOptions.value.filter(
    (dict) =>
      dict.dictValue === value ||
      dict.dictValue === String(value) ||
      dict.dictValue === Number(value)
  );
  return result;
};

// 初始化图片列表
const initImageList = (product: any) => {
  let images: string[] = [];
  if (product.urls) {
    try {
      const urlsArray = JSON.parse(product.urls);
      if (Array.isArray(urlsArray) && urlsArray.length > 0) {
        images = urlsArray;
      }
    } catch (error) {
      if (Array.isArray(product.urls) && product.urls.length > 0) {
        images = product.urls;
      }
    }
  } else if (
    product.images &&
    Array.isArray(product.images) &&
    product.images.length > 0
  ) {
    images = product.images;
  } else if (
    product.imageList &&
    Array.isArray(product.imageList) &&
    product.imageList.length > 0
  ) {
    images = product.imageList;
  } else if (
    product.imageUrls &&
    Array.isArray(product.imageUrls) &&
    product.imageUrls.length > 0
  ) {
    images = product.imageUrls;
  } else if (product.image1 || product.image2 || product.image3) {
    images = [product.image1, product.image2, product.image3].filter(
      (img) => img
    );
  } else if (
    product.pictures &&
    Array.isArray(product.pictures) &&
    product.pictures.length > 0
  ) {
    images = product.pictures;
  } else if (
    product.photoList &&
    Array.isArray(product.photoList) &&
    product.photoList.length > 0
  ) {
    images = product.photoList;
  } else {
    images = [product.cover].filter((img) => img);
  }

  imageList.value = images;
  currentImageIndex.value = 0;
  currentImageSrc.value = imageList.value[0] || '';
};

// 上一张图片
const prevImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value -= 1;
    currentImageSrc.value = imageList.value[currentImageIndex.value];
  }
};

// 下一张图片
const nextImage = () => {
  if (currentImageIndex.value < imageList.value.length - 1) {
    currentImageIndex.value += 1;
    currentImageSrc.value = imageList.value[currentImageIndex.value];
  }
};

const fetchData = async (params: any = { current: 1, pageSize: 10 }) => {
  loading.value = true;
  try {
    // 将前端的 current 转换为后端期望的 pageNum
    const requestParams: any = {
      pageNum: params.current || 1,
      pageSize: params.pageSize || pagination.pageSize,
      title: params.title,
      author: params.author,
      auditStatus: params.auditStatus,
      productType: params.productType,
      fromType: params.fromType,
      cpid: params.cpid,
      orderByColumn: params.orderByColumn,
      isAsc: params.isAsc,
    };
    // 处理日期范围参数
    if (params.params) {
      requestParams.params = params.params;
    }
    const response = await unAuditListProduct(requestParams);
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
    console.error('获取商品列表失败:', err);
    Message.error('获取数据失败');
    renderData.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

// 将列表数据转换为树结构
const listToTree = (list: any[]) => {
  const treeData: any[] = [];
  const map: Record<number, number> = {};
  for (let i = 0; i < list.length; i += 1) {
    map[list[i].id] = i;
    list[i].children = [];
  }
  for (let i = 0; i < list.length; i += 1) {
    const node = list[i];
    if (node.pid && list[map[node.pid]]) {
      list[map[node.pid]].children.push(node);
    } else {
      treeData.push(node);
    }
  }
  return treeData;
};

// 获取导航栏分类
const getNavbarOptions = async () => {
  try {
    const response = await getIdleNavbarList({});
    const data = response.data || response;
    if (data) {
      // 构建分类映射表
      categoriesMap.value = {};
      data.forEach((item: any) => {
        if (item.id && item.title) {
          categoriesMap.value[item.id] = item.title;
        }
      });
      // 将列表数据转换为树结构
      const treeData = listToTree(data);
      // 添加"主类目"作为父级节点
      navbarOptions.value = [
        {
          id: 0,
          title: '主类目',
          children: treeData,
        },
      ];
    }
  } catch (error) {
    console.error('获取分类失败:', error);
  }
};

// 获取字典数据
const getDictOptions = async () => {
  try {
    const [auditStatusRes, productTypeRes, fromTypeRes] = await Promise.all([
      getDicts('audit_status'),
      getDicts('product_type'),
      getDicts('from_type'),
    ]);
    auditStatusOptions.value = auditStatusRes.data || auditStatusRes;
    productTypeOptions.value = productTypeRes.data || productTypeRes;
    fromTypeOptions.value = fromTypeRes.data || fromTypeRes;

    // 如果字典数据为空，使用硬编码数据
    if (!auditStatusOptions.value || auditStatusOptions.value.length === 0) {
      auditStatusOptions.value = [
        { dictValue: '0', dictLabel: '未审核' },
        { dictValue: '1', dictLabel: '已通过' },
        { dictValue: '2', dictLabel: '已驳回' },
      ];
    }

    if (!productTypeOptions.value || productTypeOptions.value.length === 0) {
      productTypeOptions.value = [
        { dictValue: '1', dictLabel: '图文' },
        { dictValue: '2', dictLabel: '视频' },
      ];
    }

    if (!fromTypeOptions.value || fromTypeOptions.value.length === 0) {
      fromTypeOptions.value = [
        { dictValue: '0', dictLabel: 'web端' },
        { dictValue: '1', dictLabel: 'app端' },
      ];
    }
  } catch (error) {
    console.error('获取字典数据失败:', error);
    // 使用硬编码数据作为备用
    auditStatusOptions.value = [
      { dictValue: '0', dictLabel: '未审核' },
      { dictValue: '1', dictLabel: '已通过' },
      { dictValue: '2', dictLabel: '已驳回' },
    ];
    productTypeOptions.value = [
      { dictValue: '1', dictLabel: '图文' },
      { dictValue: '2', dictLabel: '视频' },
    ];
    fromTypeOptions.value = [
      { dictValue: '0', dictLabel: 'web端' },
      { dictValue: '1', dictLabel: 'app端' },
    ];
  }
};

const search = () => {
  pagination.current = 1;
  const params: any = {
    current: 1,
    pageSize: pagination.pageSize,
    title: formModel.value.title,
    author: formModel.value.author,
    auditStatus: formModel.value.auditStatus,
    productType: formModel.value.productType,
    fromType: formModel.value.fromType,
    cpid: formModel.value.cpid,
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
    title: formModel.value.title,
    author: formModel.value.author,
    auditStatus: formModel.value.auditStatus,
    productType: formModel.value.productType,
    fromType: formModel.value.fromType,
    cpid: formModel.value.cpid,
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
    title: formModel.value.title,
    author: formModel.value.author,
    auditStatus: formModel.value.auditStatus,
    productType: formModel.value.productType,
    fromType: formModel.value.fromType,
    cpid: formModel.value.cpid,
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

// 打开商品详情
const openProductDetail = (product: any) => {
  currentProduct.value = product;
  initImageList(product);
  detailVisible.value = true;
};

const handleRowClick = (record: any) => {
  openProductDetail(record);
};

// 查看详情
const handleView = async (id: number) => {
  try {
    const { data } = await getProduct(id);
    currentProduct.value = data;
    initImageList(data);
    detailVisible.value = true;
  } catch (e) {
    Message.error('获取商品详情失败');
  }
};

// 单个审核
const handleAudit = (
  productId: string | number,
  auditType: 'pass' | 'reject'
) => {
  Modal.confirm({
    title: `是否确认${auditType === 'pass' ? '通过' : '驳回'}该商品？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await auditProduct({ productId, auditType });
        Message.success(`${auditType === 'pass' ? '通过' : '驳回'}成功`);
        search();
      } catch (error) {
        Message.error('审核失败');
      }
    },
    onCancel: () => {
      Message.info('已取消审核');
    },
    content: '',
  });
};

// 批量审核
const openBatchAudit = (auditType: 'pass' | 'reject') => {
  if (!selectedKeys.value.length) {
    Message.error('请选择需要审核的记录');
    return;
  }
  Modal.confirm({
    title: `是否确认批量${auditType === 'pass' ? '通过' : '驳回'}所选商品？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        // 将选中的ID数组转换为逗号分隔的字符串
        const productIds = (selectedKeys.value as Array<string | number>).join(',');
        await batchAuditProduct({
          productIds,
          auditType,
        });
        Message.success(`批量${auditType === 'pass' ? '通过' : '驳回'}成功`);
        // 清空选中项
        selectedKeys.value = [];
        search();
      } catch (e) {
        Message.error('批量审核失败');
      }
    },
  });
};

onMounted(() => {
  search();
  getNavbarOptions();
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

.product-info-cell {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 0;

  .product-cover-cell {
    margin-right: 12px;
  }

  .product-details-cell {
    flex: 1;

    .product-title-cell {
      font-weight: 500;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 200px;
    }

    .product-meta-cell {
      .author-info {
        display: flex;
        align-items: center;
        color: #666;
        font-size: 12px;

        .arco-icon {
          margin-right: 4px;
        }
      }
    }
  }
}

.content-preview-cell {
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
  color: #666;
}

.image-count {
  display: flex;
  align-items: center;
  justify-content: center;

  .arco-icon {
    margin-right: 4px;
  }
}

.product-detail {
  .detail-image-container {
    position: relative;
    width: 100%;
    height: 400px;
    border-radius: 8px;
    overflow: hidden;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;

    .detail-image {
      max-width: 100%;
      max-height: 100%;
      width: auto;
      height: auto;
      object-fit: contain;
      display: block;
    }

    .detail-video {
      width: 100%;
      height: 100%;
      object-fit: contain;
      background: #000;
    }

    .image-navigation {
      position: absolute;
      top: 50%;
      left: 0;
      right: 0;
      display: flex;
      justify-content: space-between;
      padding: 0 16px;
      transform: translateY(-50%);
      pointer-events: none;

      .arco-btn {
        pointer-events: auto;
        background: rgba(0, 0, 0, 0.6);
        border: none;
        color: #fff;

        &:disabled {
          background: rgba(0, 0, 0, 0.3);
        }
      }
    }

    .image-counter {
      position: absolute;
      top: 16px;
      right: 16px;
      background: rgba(0, 0, 0, 0.6);
      color: #fff;
      padding: 4px 8px;
      border-radius: 12px;
      font-size: 12px;
    }
  }

  .detail-info {
    .detail-title {
      margin: 0 0 16px 0;
      font-size: 24px;
      font-weight: 600;
      color: #1a1a1a;
    }

    .detail-description {
      margin-top: 16px;
      padding: 16px;
      background: #f8f9fa;
      border-radius: 8px;
      border-left: 3px solid #1890ff;

      h4 {
        margin: 0 0 8px 0;
        color: #1890ff;
      }

      p {
        margin: 0;
        color: #666;
        line-height: 1.6;
        white-space: pre-wrap;
      }
    }
  }
}

:deep(.arco-table-tbody .arco-table-tr) {
  cursor: pointer;
}
</style>