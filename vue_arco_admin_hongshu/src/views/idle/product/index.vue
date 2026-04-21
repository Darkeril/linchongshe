<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb
      :items="[$t('menu.idle'), $t('menu.idle.product')]"
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
            :model="formModel"
            :label-col-props="{ span: 6 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="left"
            auto-label-width
          >
            <a-row :gutter="16">
              <a-col :span="6">
                <a-form-item field="cpid" :label="$t('biz.category')">
                  <a-tree-select
                    v-model="formModel.cpid"
                    :field-names="{
                      key: 'id',
                      title: 'title',
                      children: 'children',
                    }"
                    :data="navbarOptions"
                    :show-count="false"
                    :placeholder="$t('biz.selectCategory')"
                    allow-clear
                  >
                  </a-tree-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="title" :label="$t('biz.title')">
                  <a-input
                    v-model="formModel.title"
                    :placeholder="$t('biz.pleaseInputTitle')"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="author" :label="$t('biz.author')">
                  <a-input
                    v-model="formModel.author"
                    :placeholder="$t('biz.pleaseInputAuthor')"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="productType" :label="$t('biz.type')">
                  <a-select
                    v-model="formModel.productType"
                    :placeholder="$t('biz.productTypeSelect')"
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
                <a-form-item field="auditStatus" :label="$t('biz.status')">
                  <a-select
                    v-model="formModel.auditStatus"
                    :placeholder="$t('biz.productStatusSelect')"
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
                <a-form-item field="fromType" :label="$t('biz.productSource')">
                  <a-select
                    v-model="formModel.fromType"
                    :placeholder="$t('biz.productSourceSelect')"
                    allow-clear
                  >
                    <a-option :value="0" :label="$t('biz.fromWeb')">{{
                      $t('biz.fromWeb')
                    }}</a-option>
                    <a-option :value="1" :label="$t('biz.fromApp')">{{
                      $t('biz.fromApp')
                    }}</a-option>
                    <a-option :value="2" :label="$t('biz.fromAdmin')">{{
                      $t('biz.fromAdmin')
                    }}</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item
                  field="dateRange"
                  :label="$t('biz.createTime')"
                >
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

      <!-- 工具栏 -->
      <a-row style="margin-bottom: 16px">
        <a-col :span="12">
          <a-space>
            <a-button
              v-permission="['shop:product:add']"
              type="primary"
              @click="handleAdd"
            >
              <template #icon>
                <icon-plus />
              </template>
              {{ $t('common.add') }}
            </a-button>
            <a-button
              v-permission="['shop:product:remove']"
              status="danger"
              :disabled="selectedKeys.length === 0"
              @click="handleDelete"
            >
              <template #icon>
                <icon-delete />
              </template>
              {{ $t('common.delete') }}
            </a-button>
            <a-button status="warning" @click="handleRefreshProduct">
              <template #icon>
                <icon-refresh />
              </template>
              {{ $t('biz.esReset') }}
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
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
        @row-click="handleRowClick"
        @sorter-change="handleSorterChange"
      >
        <template #cover="{ record }">
          <a-image
            v-if="record.cover"
            :src="record.cover"
            :width="60"
            :height="60"
            fit="cover"
            :preview="true"
            @click.stop
          />
          <span v-else style="color: #999">{{ $t('biz.noCover') }}</span>
        </template>

        <template #cpid="{ record }">
          <a-tag color="blue">{{ getCategoryTitle(record.cpid) }}</a-tag>
        </template>
        <template #fromType="{ record }">
          <a-tag :color="record.fromType === 0 ? 'blue' : record.fromType === 1 ? 'green' : 'orange'">
            {{
              record.fromType === 0
                ? $t('biz.fromWeb')
                : record.fromType === 1
                  ? $t('biz.fromApp')
                  : $t('biz.fromAdmin')
            }}
          </a-tag>
        </template>

        <template #auditStatus="{ record }">
          <a-tag
            v-for="dict in getAuditStatusOptions(record.auditStatus)"
            :key="dict.dictValue"
            :color="getTagColor(dict.dictValue)"
          >
            {{ dict.dictLabel }}
          </a-tag>
        </template>

        <template #price="{ record }">
          <span v-if="record.price" style="color: #f53f3f; font-weight: bold;">
            ¥{{ record.price }}
          </span>
          <span v-else style="color: #999">-</span>
        </template>

        <template #originalPrice="{ record }">
          <span v-if="record.originalPrice" style="color: #999; text-decoration: line-through;">
            ¥{{ record.originalPrice }}
          </span>
          <span v-else style="color: #999">-</span>
        </template>

        <template #productType="{ record }">
          <dict-tag
            :options="productTypeOptions"
            :value="record.productType"
          />
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
              <a-button
                type="text"
                size="mini"
                @click.stop="handleView(record)"
              >
                <template #icon>
                  <icon-eye />
                </template>
                {{ $t('biz.view') }}
              </a-button>
              <a-button
                v-permission="['shop:product:edit']"
                type="text"
                size="mini"
                @click.stop="handleUpdate(record)"
              >
                <template #icon>
                  <icon-edit />
                </template>
                {{ $t('biz.modify') }}
              </a-button>
              <a-button
                v-permission="['shop:product:remove']"
                type="text"
                size="mini"
                @click.stop="handleDelete(record)"
              >
                <template #icon>
                  <icon-delete />
                </template>
                {{ $t('common.delete') }}
              </a-button>
            </a-button-group>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 商品详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      :title="$t('biz.productDetail')"
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
                <a-descriptions-item :label="$t('biz.author')">
                  {{ currentProduct.author }}
                </a-descriptions-item>
                <a-descriptions-item :label="$t('biz.category')">
                  {{ getCategoryTitle(currentProduct.cpid) }}
                </a-descriptions-item>
                <a-descriptions-item :label="$t('biz.type')">
                  <dict-tag
                    :options="productTypeOptions"
                    :value="currentProduct.productType"
                  />
                </a-descriptions-item>
                <a-descriptions-item :label="$t('biz.status')">
                  <a-tag
                    v-for="dict in getAuditStatusOptions(
                      currentProduct.auditStatus
                    )"
                    :key="dict.dictValue"
                    :color="getTagColor(dict.dictValue)"
                  >
                    {{ dict.dictLabel }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item :label="$t('biz.productSource')">
                  <a-tag
                    v-for="dict in getFromTypeOptions(currentProduct.fromType)"
                    :key="dict.dictValue"
                    :color="getTagColor(dict.dictValue)"
                  >
                    {{ dict.dictLabel }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item :label="$t('biz.publishLocation')">
                  {{ currentProduct.address || $t('biz.unknown') }}
                </a-descriptions-item>
                <a-descriptions-item :label="$t('biz.imageCount')">
                  {{ currentProduct.count || 0 }} {{ $t('biz.imageUnit') }}
                </a-descriptions-item>
                <a-descriptions-item :label="$t('biz.viewCountCol')">
                  {{ currentProduct.viewCount || 0 }}
                </a-descriptions-item>
                <a-descriptions-item :label="$t('biz.collectionCountCol')">
                  {{ currentProduct.collectionCount || 0 }}
                </a-descriptions-item>
                <a-descriptions-item :label="$t('biz.commentCountCol')">
                  {{ currentProduct.commentCount || 0 }}
                </a-descriptions-item>
                <a-descriptions-item :label="$t('biz.createTime')" :span="2">
                  {{ formatDate(currentProduct.createTime) }}
                </a-descriptions-item>
              </a-descriptions>
              <div class="detail-description">
                <h4>{{ $t('biz.productDescriptionSection') }}</h4>
                <p>
                  {{
                    stripHtml(currentProduct.description) ||
                    $t('biz.noDescription')
                  }}
                </p>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>
    </a-modal>

    <!-- 添加或修改商品对话框 -->
    <edit-product
      :id="currentProductId"
      :visible="editVisible"
      @update:visible="editVisible = $event"
      @update:id="currentProductId = $event"
      @refresh-data-list="fetchData"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, reactive, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { Message, Modal } from '@arco-design/web-vue';
import { Pagination } from '@/types/global';
import {
  IconSearch,
  IconRefresh,
  IconPlus,
  IconEdit,
  IconDelete,
  IconLeft,
  IconRight,
  IconEye,
} from '@arco-design/web-vue/es/icon';
import {
  getProductList,
  deleteProduct,
  refreshProductData,
  createProduct,
  updateProduct,
  getProduct,
} from '@/api/idle/product';
import { getIdleNavbarList } from '@/api/idle/navbar';
import { getDicts } from '@/api/system/dict-data';
import addDateRange, { currentLocale } from '@/utils/common';
import { getTodayRange } from '@/utils/workbench-nav';
import DictTag from '@/components/dict-tag/index.vue';
import EditProduct from './components/edit-product.vue';

const { t } = useI18n();

const generateFormModel = () => {
  return {
    title: '',
    author: '',
    uid: undefined,
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
const route = useRoute();
const loading = ref(false);
const tableRef = ref();
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

// 编辑相关状态
const editVisible = ref(false);
const currentProductId = ref<string | undefined>(undefined);

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
  return categoriesMap.value[pid] || t('biz.unknownCategory');
};

const rowSelection = reactive({
  type: 'checkbox',
  showCheckedAll: true,
  onlyCurrent: false,
});

const columns = computed(() => [
  {
    title: t('biz.serialNo'),
    dataIndex: 'id',
    width: 80,
    align: 'center',
  },
  {
    title: t('biz.category'),
    dataIndex: 'cpid',
    slotName: 'cpid',
    width: 120,
    align: 'center',
  },
  {
    title: t('biz.title'),
    dataIndex: 'title',
    width: 200,
    ellipsis: true,
    align: 'center',
  },
  {
    title: t('biz.author'),
    dataIndex: 'author',
    width: 120,
    align: 'center',
  },
  {
    title: t('biz.productCover'),
    dataIndex: 'cover',
    slotName: 'cover',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.productContent'),
    dataIndex: 'description',
    width: 200,
    ellipsis: true,
    align: 'center',
  },
  {
    title: t('biz.publishLocation'),
    dataIndex: 'address',
    width: 120,
    ellipsis: true,
    align: 'center',
  },
  {
    title: t('biz.productPrice'),
    dataIndex: 'price',
    slotName: 'price',
    width: 120,
    align: 'center',
  },
  {
    title: t('biz.originalPriceCol'),
    dataIndex: 'originalPrice',
    slotName: 'originalPrice',
    width: 120,
    align: 'center',
  },
  {
    title: t('biz.imageCount'),
    dataIndex: 'count',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.type'),
    dataIndex: 'productType',
    slotName: 'productType',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.productSource'),
    dataIndex: 'fromType',
    slotName: 'fromType',
    width: 120,
    align: 'center',
  },
  {
    title: t('biz.status'),
    dataIndex: 'auditStatus',
    slotName: 'auditStatus',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.viewCountCol'),
    dataIndex: 'viewCount',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.collectionCountCol'),
    dataIndex: 'collectionCount',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.commentCountCol'),
    dataIndex: 'commentCount',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.createTime'),
    dataIndex: 'createTime',
    slotName: 'createTime',
    width: 180,
    sortable: {
      sortDirections: ['ascend', 'descend'],
    },
    align: 'center',
  },
  {
    title: t('biz.updateTime'),
    dataIndex: 'updateTime',
    slotName: 'updateTime',
    width: 180,
    sortable: {
      sortDirections: ['ascend', 'descend'],
    },
    align: 'center',
  },
  {
    title: t('biz.operation'),
    dataIndex: 'operations',
    slotName: 'operations',
    fixed: 'right',
    width: 200,
    align: 'center',
  },
]);

const renderData = ref([]);

const rules = computed(() => ({
  title: [{ required: true, message: t('biz.pleaseInputProductTitle') }],
  productType: [{ required: true, message: t('biz.productTypeSelect') }],
}));

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '';
  return new Date(date).toLocaleString(currentLocale());
};

// 去除HTML标签，只显示纯文本
const stripHtml = (html: string): string => {
  if (!html) return '';
  // 去除HTML标签
  let text = html.replace(/<[^>]+>/g, '');
  // 解码HTML实体
  text = text
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&nbsp;/g, ' ')
    .replace(/&#x2F;/g, '/');
  return text.trim();
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
  // 尝试字符串和数字匹配
  const result = fromTypeOptions.value.filter(
    (dict) =>
      dict.dictValue === value ||
      dict.dictValue === String(value) ||
      dict.dictValue === Number(value)
  );
  if (result.length === 0 && value !== undefined) {
    console.log(
      '未找到fromType字典:',
      value,
      '类型:',
      typeof value,
      '可用选项:',
      fromTypeOptions.value
    );
  }
  return result;
};

const getAuditStatusOptions = (value: any) => {
  if (!auditStatusOptions.value || !Array.isArray(auditStatusOptions.value)) {
    return [];
  }
  // 尝试字符串和数字匹配
  const result = auditStatusOptions.value.filter(
    (dict) =>
      dict.dictValue === value ||
      dict.dictValue === String(value) ||
      dict.dictValue === Number(value)
  );
  if (result.length === 0 && value !== undefined) {
    console.log(
      '未找到auditStatus字典:',
      value,
      '类型:',
      typeof value,
      '可用选项:',
      auditStatusOptions.value
    );
  }
  return result;
};

const getProductTypeOptions = (value: any) => {
  if (!productTypeOptions.value || !Array.isArray(productTypeOptions.value)) {
    return [];
  }
  // 尝试字符串和数字匹配
  const result = productTypeOptions.value.filter(
    (dict) =>
      dict.dictValue === value ||
      dict.dictValue === String(value) ||
      dict.dictValue === Number(value)
  );
  if (result.length === 0 && value !== undefined) {
    console.log(
      '未找到productType字典:',
      value,
      '类型:',
      typeof value,
      '可用选项:',
      productTypeOptions.value
    );
  }
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
    const response = await getProductList(requestParams);
    console.log('接口返回数据:', response);
    // 后端返回的数据结构：{ code: 200, total: 94, rows: [...], msg: "查询成功" }
    // 或者经过拦截器处理后：{ code: 200, data: { total: 94, rows: [...] }, msg: "查询成功" }
    if (response.rows) {
      // 如果响应顶层有 rows（拦截器可能将 TableDataInfo 的 rows 提取到顶层）
      renderData.value = response.rows;
      pagination.total = (response as any).total || 0;
    } else if (response.data) {
      if (response.data.rows) {
        // 如果 data 中有 rows
        renderData.value = response.data.rows;
        pagination.total = response.data.total || 0;
      } else if (Array.isArray(response.data)) {
        renderData.value = response.data;
        pagination.total = response.data.length;
      } else if (response.data.list) {
        renderData.value = response.data.list;
        pagination.total = response.data.total || 0;
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
    console.log('解析后的数据:', renderData.value);
    console.log('总数:', pagination.total);
    pagination.current = params.current || 1;
  } catch (err) {
    console.error('获取商品列表失败:', err);
    Message.error(t('biz.fetchDataFailed'));
    renderData.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

// 数组转树结构集合
const listToTree: any = (list: Array<any>) => {
  const map: any = {};
  const treeData: Array<any> = [];
  list = list.map((item) => {
    return {
      id: item.id,
      pid: item.pid,
      title: item.title,
    };
  });
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
    const list = Array.isArray(data)
      ? data
      : data?.list || data?.rows || data?.records || [];
    if (list && list.length > 0) {
      // 创建分类映射
      categoriesMap.value = {};
      list.forEach((item: any) => {
        if (item.id && item.title) {
          categoriesMap.value[item.id] = item.title;
        }
      });
      // 将列表数据转换为树结构
      const treeData = listToTree(list);
      // 添加"主类目"作为父级节点
      navbarOptions.value = [
        {
          id: 0,
          title: t('biz.mainCategory'),
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
        { dictValue: '0', dictLabel: t('biz.auditPending') },
        { dictValue: '1', dictLabel: t('biz.auditApproved') },
        { dictValue: '2', dictLabel: t('biz.auditRejected') },
      ];
    }

    if (!productTypeOptions.value || productTypeOptions.value.length === 0) {
      productTypeOptions.value = [
        { dictValue: '1', dictLabel: t('dashboard.imageText') },
        { dictValue: '2', dictLabel: t('dashboard.video') },
      ];
    }

    if (!fromTypeOptions.value || fromTypeOptions.value.length === 0) {
      fromTypeOptions.value = [
        { dictValue: '0', dictLabel: t('biz.fromWeb') },
        { dictValue: '1', dictLabel: t('biz.fromApp') },
        { dictValue: '2', dictLabel: t('biz.fromAdmin') },
      ];
    }

    console.log('字典数据加载完成:', {
      auditStatus: auditStatusOptions.value,
      productType: productTypeOptions.value,
      fromType: fromTypeOptions.value,
    });
  } catch (error) {
    console.error('获取字典数据失败:', error);
    // 使用硬编码数据作为备用
    auditStatusOptions.value = [
      { dictValue: '0', dictLabel: t('biz.auditPending') },
      { dictValue: '1', dictLabel: t('biz.auditApproved') },
      { dictValue: '2', dictLabel: t('biz.auditRejected') },
    ];
    productTypeOptions.value = [
      { dictValue: '1', dictLabel: t('dashboard.imageText') },
      { dictValue: '2', dictLabel: t('dashboard.video') },
    ];
    fromTypeOptions.value = [
      { dictValue: '0', dictLabel: t('biz.fromWeb') },
      { dictValue: '1', dictLabel: t('biz.fromApp') },
      { dictValue: '2', dictLabel: t('biz.fromAdmin') },
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
  pagination.current = current;
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
  pagination.current = 1;
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

// 查看详情
const handleView = (record: any) => {
  currentProduct.value = record;
  initImageList(record);
  detailVisible.value = true;
};

// 行点击事件
const handleRowClick = (record: any) => {
  currentProduct.value = record;
  initImageList(record);
  detailVisible.value = true;
};

const handleAdd = () => {
  currentProductId.value = undefined;
  editVisible.value = true;
};

const handleUpdate = (record?: any) => {
  const productId = record?.id || selectedKeys.value[0];
  if (!productId) {
    Message.warning(t('biz.pleaseSelectProductToEdit'));
    return;
  }

  currentProductId.value = String(productId);
  editVisible.value = true;
};

const handleDelete = (record?: any) => {
  const productIds = record?.id ? [record.id] : selectedKeys.value;
  if (productIds.length === 0) {
    Message.warning(t('biz.pleaseSelectProductToDelete'));
    return;
  }

  const ids = record && record.id ? record.id : selectedKeys.value.join(',');
  Modal.confirm({
    titleAlign: 'start',
    title: t('biz.deleteConfirmTitle'),
    content: t('biz.deleteProductConfirm', { ids }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        // 目前只支持单个删除，取第一个ID
        await deleteProduct(productIds[0]);
        Message.success(t('biz.deleteSuccess'));
        fetchData();
      } catch (error) {
        Message.error(t('biz.deleteFailed'));
      }
    },
  });
};


const handleRefreshProduct = () => {
  Modal.confirm({
    title: t('biz.confirmEsResetTitle'),
    content: t('biz.confirmEsResetContent'),
    onOk: async () => {
      try {
        await refreshProductData();
        Message.success(t('biz.resetSuccess'));
        fetchData();
      } catch (error) {
        Message.error(t('biz.resetFailed'));
      }
    },
  });
};

// 这些函数已移到 edit-product 组件中

onMounted(() => {
  if (route.query.uid) {
    formModel.value.uid = route.query.uid as string;
  }
  if (route.query.cpid) {
    formModel.value.cpid = Number(route.query.cpid);
  }
  if (route.query.today === '1') {
    formModel.value.dateRange = getTodayRange();
  }
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
:deep(.arco-table-th) {
  &:last-child {
    .arco-table-th-item-title {
      margin-left: 16px;
    }
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

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: border-color 0.3s;

  &:hover {
    border-color: #165dff;
  }

  .upload-text {
    margin-top: 8px;
    font-size: 12px;
    color: #666;
  }
}

.preview-image {
  margin-top: 8px;
}
</style>