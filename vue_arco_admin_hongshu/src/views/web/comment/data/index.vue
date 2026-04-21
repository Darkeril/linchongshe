<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['笔记管理', '评论管理', '子评论']"></Breadcrumb>
    <a-card
      class="general-card r-nav-card"
      :body-style="{ padding: '15px' }"
      :header-style="{ padding: '15px' }"
    >
      <!-- 类型提示 -->
      <a-alert
        :message="`当前查看: ${commentTypeText}评论`"
        type="info"
        style="margin-bottom: 20px"
      />

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
                <a-form-item field="username" label="评论人">
                  <a-input
                    v-model="formModel.username"
                    placeholder="请输入评论人"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="content" label="评论内容">
                  <a-input
                    v-model="formModel.content"
                    placeholder="请输入评论内容"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="dateRange" label="评论时间">
                  <a-range-picker
                    v-model="formModel.dateRange"
                    style="width: 100%"
                    format="YYYY-MM-DD"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="4">
                <a-space>
                  <a-button type="primary" @click="handleQuery">
                    <template #icon>
                      <icon-search />
                    </template>
                    查询
                  </a-button>
                  <a-button @click="resetQuery">
                    <template #icon>
                      <icon-refresh />
                    </template>
                    重置
                  </a-button>
                </a-space>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
      </a-row>

      <a-row style="margin-bottom: 16px">
        <a-col :span="24">
          <a-space>
            <a-button
              type="primary"
              status="danger"
              @click="handleBatchDelete"
              :disabled="selectedKeys.length === 0"
            >
              <template #icon>
                <icon-delete />
              </template>
              批量删除
            </a-button>
            <a-button type="secondary" @click="handleClose">
              <template #icon>
                <icon-close />
              </template>
              关闭
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
      >
        <template #username="{ record }">
          <div
            style="display: flex; align-items: center; justify-content: center"
          >
            <a-avatar v-if="record.avatar" :size="35">
              <img :src="record.avatar" alt="头像" />
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
              删除
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, reactive, onMounted } from 'vue';
import { Message, Modal } from '@arco-design/web-vue';
import { useRouter, useRoute } from 'vue-router';
import { Pagination } from '@/types/global';
import {
  IconSearch,
  IconRefresh,
  IconUser,
  IconDelete,
  IconClose,
} from '@arco-design/web-vue/es/icon';
import { getCommentTreeList, delComment } from '@/api/web/comment';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import addDateRange from '@/utils/common';

const router = useRouter();
const route = useRoute();

const generateFormModel = () => {
  return {
    username: '',
    content: '',
    dateRange: [] as string[],
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

// 从路由参数获取 targetId 和 type
const targetId = computed(() => route.params.id as string);
const commentType = computed(() => (route.query.type as string) || 'note');
const commentTypeText = computed(() =>
  commentType.value === 'product' ? '商品' : '笔记'
);

const columns = computed(() => [
  {
    title: '序号',
    dataIndex: 'id',
    width: 80,
    align: 'center',
  },
  {
    title: '评论人',
    dataIndex: 'username',
    slotName: 'username',
    width: 150,
    align: 'center',
  },
  {
    title: '评论内容',
    dataIndex: 'content',
    slotName: 'content',
    width: 300,
    align: 'center',
  },
  {
    title: '评论等级',
    dataIndex: 'level',
    slotName: 'level',
    width: 120,
    align: 'center',
  },
  {
    title: '评论时间',
    dataIndex: 'createTime',
    slotName: 'createTime',
    width: 180,
    align: 'center',
  },
  {
    title: '操作',
    slotName: 'operations',
    width: 100,
    align: 'center',
    fixed: 'right',
  },
]);

const renderData = ref([]);

// 评论等级映射
const levelMap = {
  1: '一级评论',
  2: '二级评论',
  3: '三级评论',
  4: '四级评论',
  5: '五级评论',
};

// 根据 level 返回对应的颜色
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
  return levelMap[level] || '其他评论';
};

// 时间格式化函数
const formatTime = (timestamp: number | string) => {
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

// 获取列表数据
const fetchData = async () => {
  loading.value = true;
  try {
    const params: any = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      targetId: targetId.value,
      type: commentType.value,
    };

    // 添加搜索条件
    if (formModel.value.username) {
      params.username = formModel.value.username;
    }
    if (formModel.value.content) {
      params.content = formModel.value.content;
    }

    // 添加日期范围
    const dateRangeParams = addDateRange(
      params,
      formModel.value.dateRange
    );

    const res = await getCommentTreeList(dateRangeParams);
    if (res.code === 200) {
      renderData.value = res.rows || [];
      pagination.total = res.total || 0;
    } else {
      Message.error(res.msg || '获取数据失败');
    }
  } catch (error) {
    console.error('获取数据失败:', error);
    Message.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

// 查询
const handleQuery = () => {
  pagination.current = 1;
  fetchData();
};

// 重置
const resetQuery = () => {
  formModel.value = generateFormModel();
  pagination.current = 1;
  fetchData();
};

// 分页变化
const onPageChange = (current: number) => {
  pagination.current = current;
  fetchData();
};

// 每页数量变化
const onPageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize;
  pagination.current = 1;
  fetchData();
};

// 删除
const handleDelete = (record: any) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除这条评论吗？`,
    onOk: async () => {
      try {
        const res = await delComment(record.id);
        if (res.code === 200) {
          Message.success('删除成功');
          fetchData();
        } else {
          Message.error(res.msg || '删除失败');
        }
      } catch (error) {
        console.error('删除失败:', error);
        Message.error('删除失败');
      }
    },
  });
};

// 批量删除
const handleBatchDelete = () => {
  if (selectedKeys.value.length === 0) {
    Message.warning('请选择要删除的评论');
    return;
  }
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedKeys.value.length} 条评论吗？`,
    onOk: async () => {
      try {
        const ids = selectedKeys.value.join(',');
        const res = await delComment(ids);
        if (res.code === 200) {
          Message.success('删除成功');
          selectedKeys.value = [];
          fetchData();
        } else {
          Message.error(res.msg || '删除失败');
        }
      } catch (error) {
        console.error('删除失败:', error);
        Message.error('删除失败');
      }
    },
  });
};

// 关闭页面
const handleClose = () => {
  router.back();
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}
</style>

