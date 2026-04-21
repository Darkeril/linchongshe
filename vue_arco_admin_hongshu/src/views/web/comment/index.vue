<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb
      :items="[$t('menu.web.note'), $t('menu.web.comment')]"
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
                <a-form-item field="username" :label="$t('biz.commenter')">
                  <a-input
                    v-model="formModel.username"
                    :placeholder="$t('biz.enterCommenter')"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="content" :label="$t('biz.commentContent')">
                  <a-input
                    v-model="formModel.content"
                    :placeholder="$t('biz.enterCommentContent')"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="dateRange" :label="$t('biz.commentTimeLabel')">
                  <a-range-picker
                    v-model="formModel.dateRange"
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
        <!-- 前置工具栏部分 -->
        <a-col :span="12">
          <a-space>
            <a-button
              status="danger"
              :disabled="selectedKeys.length <= 0"
              @click="handleBatchDelete"
            >
              <template #icon>
                <icon-delete />
              </template>
              {{ $t('common.delete') }}
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <!-- 表格数据-->
        <a-table
          row-key="id"
          :loading="loading"
          :columns="columns"
          :data="renderData"
          v-model:selected-keys="selectedKeys"
          :pagination="pagination"
        :bordered="false"
        :row-selection="{ type: 'checkbox', showCheckedAll: true }"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
        @sorter-change="handleSorterChange"
      >
        <template #username="{ record }">
          <div
            style="display: flex; align-items: center; justify-content: center"
          >
            <a-avatar v-if="record.avatar" :size="35">
              <img :src="record.avatar" :alt="$t('biz.avatar')" />
            </a-avatar>
            <a-avatar v-else :size="35">
              <icon-user />
            </a-avatar>
            <a-tooltip :content="record.username" placement="top">
              <span
                style="
                  margin-left: 10px;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  max-width: 100px;
                "
              >
                {{ record.username }}
              </span>
            </a-tooltip>
          </div>
        </template>
        <template #pushUsername="{ record }">
          <div
            style="display: flex; align-items: center; justify-content: center"
          >
            <a-avatar v-if="record.replyAvatar" :size="35">
              <img :src="record.replyAvatar" :alt="$t('biz.avatar')" />
            </a-avatar>
            <a-avatar v-else :size="35">
              <icon-user />
            </a-avatar>
            <a-tooltip :content="record.pushUsername" placement="top">
              <span
                style="
                  margin-left: 10px;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  max-width: 100px;
                "
              >
                {{ record.pushUsername }}
              </span>
            </a-tooltip>
          </div>
        </template>
        <template #commentType="{ record }">
          <a-tag :color="(record.commentType === 'product' || record.productId) ? 'orange' : 'green'">
            {{
              (record.commentType === 'product' || record.productId)
                ? $t('biz.productTypeShort')
                : $t('biz.noteTypeShort')
            }}
          </a-tag>
        </template>
        <template #title="{ record }">
          <a-typography-text
            :ellipsis="{ tooltip: true, rows: 1 }"
            style="max-width: 150px; color: #1890ff; cursor: pointer"
            @click="handleTitleClick(record)"
          >
            {{ record.title }}
          </a-typography-text>
        </template>
        <template #noteCover="{ record }">
          <a-avatar v-if="record.noteCover" :size="60" shape="square">
            <img :src="record.noteCover" :alt="$t('biz.cover')" />
          </a-avatar>
          <span v-else style="color: #999">{{ $t('biz.noCover') }}</span>
        </template>
        <template #content="{ record }">
          <a-typography-text
            :ellipsis="{ tooltip: true, rows: 2 }"
            style="max-width: 200px"
          >
            {{ record.content }}
          </a-typography-text>
        </template>
        <template #level="{ record }">
          <a-tag :color="getLevelColor(record.level)">
            {{ getLevelText(record.level) }}
          </a-tag>
        </template>
        <template #createTime="{ record }">
          {{ formatTime(record.createTime) }}
        </template>
        <template #operations="{ record }">
          <a-space>
            <a-button
              type="text"
              size="small"
              status="danger"
              @click="handleDelete(record)"
            >
              <template #icon>
                <icon-delete />
              </template>
              {{ $t('common.delete') }}
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, reactive, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Message, Modal } from '@arco-design/web-vue';
import { useRouter } from 'vue-router';
import { Pagination } from '@/types/global';
import {
  IconSearch,
  IconRefresh,
  IconUser,
  IconDelete,
} from '@arco-design/web-vue/es/icon';
import { getCommentList, deleteComment } from '@/api/web/comment';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import addDateRange from '@/utils/common';

const router = useRouter();
const { t } = useI18n();

const generateFormModel = () => {
  return {
    username: '',
    content: '',
    dateRange: [] as string[],
    orderByColumn: undefined as string | undefined,
    isAsc: undefined as string | undefined,
  };
};
const formModel = ref(generateFormModel());
const loading = ref(false);
const selectedKeys = ref([]);
const basePagination: Pagination = {
  current: 1,
  pageSize: 20,
};
const pagination = reactive({
  ...basePagination,
});
const columns = computed(() => [
  {
    title: t('biz.serialNo'),
    dataIndex: 'id',
    width: 80,
    align: 'center',
  },
  {
    title: t('biz.commenter'),
    dataIndex: 'username',
    slotName: 'username',
    width: 150,
    align: 'center',
  },
  {
    title: t('biz.commentedUser'),
    dataIndex: 'pushUsername',
    slotName: 'pushUsername',
    width: 150,
    align: 'center',
  },
  {
    title: t('biz.commentTarget'),
    dataIndex: 'commentType',
    slotName: 'commentType',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.title'),
    dataIndex: 'title',
    slotName: 'title',
    width: 150,
    align: 'center',
  },
  {
    title: t('biz.cover'),
    dataIndex: 'noteCover',
    slotName: 'noteCover',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.authorColumn'),
    dataIndex: 'pushUsername',
    width: 120,
    align: 'center',
  },
  {
    title: t('biz.commentContent'),
    dataIndex: 'content',
    slotName: 'content',
    width: 200,
    align: 'center',
  },
  {
    title: t('biz.commentLevel'),
    dataIndex: 'level',
    slotName: 'level',
    width: 100,
    align: 'center',
  },
  {
    title: t('biz.commentTimeLabel'),
    dataIndex: 'createTime',
    slotName: 'createTime',
    sortable: {
      sortDirections: ['ascend', 'descend'],
    },
    width: 180,
    align: 'center',
  },
  {
    title: t('biz.operation'),
    dataIndex: 'operations',
    slotName: 'operations',
    width: 200,
    align: 'center',
    fixed: 'right',
  },
]);
const renderData = ref([]);
const fetchData = async (params: any = { current: 1, pageSize: 20 }) => {
  loading.value = true;
  try {
    // 转换分页参数：前端使用 current，后端使用 pageNum
    const requestParams: any = {
      pageNum: params.current || 1,
      pageSize: params.pageSize || 20,
    };
    // 添加筛选条件
    if (params.username) {
      requestParams.username = params.username;
    }
    if (params.content) {
      requestParams.content = params.content;
    }
    // 添加排序参数
    if (params.orderByColumn) {
      requestParams.orderByColumn = params.orderByColumn;
    }
    if (params.isAsc) {
      requestParams.isAsc = params.isAsc;
    }
    // 添加日期范围参数
    if (params.params) {
      requestParams.params = params.params;
    }
    const res = await getCommentList(requestParams);

    // 处理不同的数据结构
    let list;
    let total;
    if (res?.data?.list) {
      // 格式: { data: { list: [...], total: 20 } }
      list = res.data.list;
      total = res.data.total;
    } else if (res?.data?.rows) {
      // 格式: { data: { rows: [...], total: 20 } }
      list = res.data.rows;
      total = res.data.total;
    } else if (res?.rows) {
      // 格式: { rows: [...], total: 20 }
      list = res.rows;
      total = res.total;
    } else if (Array.isArray(res?.data)) {
      // 格式: { data: [...] }
      list = res.data;
      total = res.data.length;
    } else if (res?.data?.records) {
      // 格式: { data: { records: [...], total: 20 } }
      list = res.data.records;
      total = res.data.total;
    } else {
      // 其他格式
      list = [];
      total = 0;
    }

    renderData.value = Array.isArray(list) ? list : [];
    pagination.current = params.current;
    pagination.total =
      typeof total === 'number' ? total : renderData.value.length;
  } catch (err) {
    // Handle error silently
    renderData.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

const search = () => {
  pagination.current = 1;
  const params: any = {
    current: 1,
    pageSize: pagination.pageSize,
    username: formModel.value.username,
    content: formModel.value.content,
    orderByColumn: formModel.value.orderByColumn,
    isAsc: formModel.value.isAsc,
  };
  // 处理日期范围
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
    username: formModel.value.username,
    content: formModel.value.content,
    orderByColumn: formModel.value.orderByColumn,
    isAsc: formModel.value.isAsc,
  };
  // 处理日期范围
  if (formModel.value.dateRange && formModel.value.dateRange.length === 2) {
    const dateRangeParams = addDateRange({}, formModel.value.dateRange);
    if (dateRangeParams.params) {
      params.params = dateRangeParams.params;
    }
  }
  fetchData(params);
};
const onPageSizeChange = (pageSize: number) => {
  const params: any = {
    current: pagination.current,
    pageSize,
    username: formModel.value.username,
    content: formModel.value.content,
    orderByColumn: formModel.value.orderByColumn,
    isAsc: formModel.value.isAsc,
  };
  // 处理日期范围
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
  fetchData(basePagination);
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
      // 评论时间排序
      formModel.value.orderByColumn = 'createTime';
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
  // 重置到第一页并重新查询
  pagination.current = 1;
  const params: any = {
    current: 1,
    pageSize: pagination.pageSize,
    username: formModel.value.username,
    content: formModel.value.content,
    orderByColumn: formModel.value.orderByColumn,
    isAsc: formModel.value.isAsc,
  };
  // 处理日期范围
  if (formModel.value.dateRange && formModel.value.dateRange.length === 2) {
    const dateRangeParams = addDateRange({}, formModel.value.dateRange);
    if (dateRangeParams.params) {
      params.params = dateRangeParams.params;
    }
  }
  fetchData(params);
};
const handleDelete = (record: any) => {
  Modal.confirm({
    titleAlign: 'start',
    title: t('biz.systemPrompt'),
    content: t('biz.confirmDeleteThisComment'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        await deleteComment(record.id);
        Message.success(t('biz.deleteSuccess'));
        fetchData();
      } catch (error) {
        Message.error(t('biz.deleteCommentFailed'));
      }
    },
  });
};

// 批量删除
const handleBatchDelete = () => {
  if (selectedKeys.value.length === 0) {
    Message.warning(t('biz.noSelection'));
    return;
  }

  Modal.confirm({
    titleAlign: 'start',
    title: t('biz.systemPrompt'),
    content: t('biz.confirmDeleteNComments', {
      n: selectedKeys.value.length,
    }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        // 批量删除所有选中的评论
        await Promise.all(selectedKeys.value.map((id) => deleteComment(id)));
        Message.success(
          t('biz.deletedNComments', { n: selectedKeys.value.length })
        );
        selectedKeys.value = [];
        fetchData();
      } catch (error) {
        Message.error(t('biz.batchDeleteCommentFailed'));
      }
    },
  });
};

// 点击标题跳转到子评论页面
const handleTitleClick = (record: any) => {
  // 根据是否有 productId 判断是商品还是笔记
  const targetId = record.productId || record.nid;
  const type = record.productId ? 'product' : 'note';
  
  if (!targetId) {
    Message.warning(t('biz.cannotGetCommentTargetId'));
    return;
  }
  
  // 跳转到子评论页面
  router.push({
    name: 'CommentData',
    params: {
      id: String(targetId)
    },
    query: {
      type
    }
  });
};

// 根据 level 值返回对应的颜色
const getLevelColor = (level: number) => {
  switch (level) {
    case 1:
      return 'green'; // 一级评论，绿色
    case 2:
      return 'blue'; // 二级评论，蓝色
    case 3:
      return 'orange'; // 三级评论，橙色
    case 4:
      return 'red'; // 四级评论，红色
    case 5:
      return 'purple'; // 五级评论，紫色
    default:
      return 'gray'; // 默认颜色
  }
};

// 根据 level 返回对应的文本
const getLevelText = (level: number) => {
  const map: Record<number, string> = {
    1: t('biz.levelComment1'),
    2: t('biz.levelComment2'),
    3: t('biz.levelComment3'),
    4: t('biz.levelComment4'),
    5: t('biz.levelComment5'),
  };
  return map[level] || t('biz.otherComment');
};

// 时间格式化函数
const formatTime = (timestamp: number) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
};

onMounted(() => {
  fetchData();
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
</style>

