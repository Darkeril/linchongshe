<template>
  <div class="popular-content-page">
    <Breadcrumb :items="['数据中心', '内容分析']" />
    <a-card
      class="general-card"
      :bordered="false"
      :body-style="{ padding: '20px' }"
    >
      <!-- <a-tabs v-model:active-key="activeTab" type="text" class="content-tabs">
        <a-tab-pane key="note" title="笔记数据" />
        <a-tab-pane key="live" title="商品数据" />
      </a-tabs> -->

      <!-- 笔记数据 -->
      <template v-if="activeTab === 'note'">
        <div class="filter-row">
          <a-form layout="inline" :model="queryParams" class="filter-form">
            <a-form-item label="选择用户">
              <a-select
                v-model="queryParams.uid"
                placeholder="全部用户（可输入检索）"
                allow-clear
                allow-search
                :filter-option="filterCreatorOption"
                style="width: 200px"
              >
                <a-option
                  v-for="opt in creatorOptions"
                  :key="opt.value"
                  :value="opt.value"
                  :label="opt.label"
                />
              </a-select>
            </a-form-item>
            <a-form-item label="笔记类型">
              <a-select
                v-model="queryParams.noteType"
                placeholder="全部"
                allow-clear
                style="width: 140px"
              >
                <a-option value="">全部</a-option>
                <a-option value="1">图文</a-option>
                <a-option value="3">live图</a-option>
                <a-option value="2">视频</a-option>
              </a-select>
            </a-form-item>
            <a-form-item label="笔记首发时间">
              <a-range-picker
                v-model="queryParams.dateRange"
                show-time
                format="YYYY-MM-DD HH:mm"
                style="width: 360px"
                :disabled-date="disabledFutureDate"
              />
            </a-form-item>
          </a-form>
          <a-space>
            <a-button type="primary" @click="handleQuery">
              <template #icon><icon-search /></template>
              查询
            </a-button>
            <!-- <a-button @click="handleExport">
              <template #icon><icon-download /></template>
              导出数据
            </a-button> -->
          </a-space>
        </div>

        <a-table
          :loading="loading"
          :data="noteList"
          :columns="noteColumns"
          :pagination="pagination"
          row-key="id"
          @page-change="onPageChange"
          @page-size-change="onPageSizeChange"
        >
          <template #noteInfo="{ record }">
            <div class="note-info-cell">
              <div v-if="record.notApproved" class="note-status">
                <a-tag color="red">未通过</a-tag>
                <a-link size="small">查看修改建议</a-link>
              </div>
              <div class="note-info-main">
                <a-image
                  v-if="record.cover"
                  :src="record.cover"
                  width="80"
                  height="60"
                  fit="cover"
                  class="note-cover"
                />
                <div v-else class="note-placeholder" />
                <div class="note-meta">
                  <div class="note-title">{{ record.title }}</div>
                  <div class="note-time">发布于 {{ record.publishTime }}</div>
                </div>
              </div>
            </div>
          </template>
          <template #action="{ record }">
            <a-button type="text" size="small" @click="goDetail(record)"
              >详情数据</a-button
            >
          </template>
        </a-table>
      </template>

      <!-- 商品数据 -->
      <template v-if="activeTab === 'live'">
        <a-empty
          description="商品数据，敬请期待"
          :image-style="{ height: '100px' }"
        />
      </template>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import dayjs from 'dayjs';
import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import useLoading from '@/hooks/loading';
import { getNoteDataCenterList } from '@/api/web/note';
import { getCreatorOptions } from '@/api/web/dataCenter';
import { useRouter } from 'vue-router';

const router = useRouter();

const { loading, setLoading } = useLoading(false);
const activeTab = ref('note');

const queryParams = reactive({
  uid: '' as string,
  noteType: '' as string,
  dateRange: [] as (string | Date)[],
});
const creatorOptions = ref<{ value: string; label: string }[]>([]);

const noteColumns: TableColumnData[] = [
  {
    title: '笔记基础信息',
    dataIndex: 'noteInfo',
    slotName: 'noteInfo',
    width: 300,
    align: 'center',
  },
  {
    title: '曝光',
    dataIndex: 'exposure',
    width: 100,
    align: 'center',
    sortable: { sortDirections: ['ascend', 'descend'] },
    sorter: (a: any, b: any) =>
      Number(a.exposure || 0) - Number(b.exposure || 0),
  },
  {
    title: '观看',
    dataIndex: 'views',
    width: 100,
    align: 'center',
    sortable: { sortDirections: ['ascend', 'descend'] },
    sorter: (a: any, b: any) => Number(a.views || 0) - Number(b.views || 0),
  },
  {
    title: '封面点击率',
    dataIndex: 'coverCtr',
    width: 150,
    align: 'center',
    sortable: { sortDirections: ['ascend', 'descend'] },
    sorter: (a: any, b: any) => {
      const parseRate = (v: unknown) =>
        typeof v === 'string'
          ? parseFloat(v.replace('%', '')) || 0
          : Number(v || 0);
      return parseRate(a.coverCtr) - parseRate(b.coverCtr);
    },
  },
  {
    title: '点赞',
    dataIndex: 'likes',
    width: 100,
    align: 'center',
    sortable: { sortDirections: ['ascend', 'descend'] },
    sorter: (a: any, b: any) => Number(a.likes || 0) - Number(b.likes || 0),
  },
  {
    title: '评论',
    dataIndex: 'comments',
    width: 100,
    align: 'center',
    sortable: { sortDirections: ['ascend', 'descend'] },
    sorter: (a: any, b: any) =>
      Number(a.comments || 0) - Number(b.comments || 0),
  },
  {
    title: '收藏',
    dataIndex: 'favorites',
    width: 100,
    align: 'center',
    sortable: { sortDirections: ['ascend', 'descend'] },
    sorter: (a: any, b: any) =>
      Number(a.favorites || 0) - Number(b.favorites || 0),
  },
  {
    title: '涨粉',
    dataIndex: 'follows',
    width: 100,
    align: 'center',
    sortable: { sortDirections: ['ascend', 'descend'] },
    sorter: (a: any, b: any) => Number(a.follows || 0) - Number(b.follows || 0),
  },
  {
    title: '分享',
    dataIndex: 'shares',
    width: 100,
    align: 'center',
    sortable: { sortDirections: ['ascend', 'descend'] },
    sorter: (a: any, b: any) => Number(a.shares || 0) - Number(b.shares || 0),
  },
  {
    title: '人均观看时长',
    dataIndex: 'avgDuration',
    width: 150,
    align: 'center',
    sortable: { sortDirections: ['ascend', 'descend'] },
    sorter: (a: any, b: any) => {
      const parseSec = (v: unknown) =>
        typeof v === 'string'
          ? parseFloat(v.replace('s', '')) || 0
          : Number(v || 0);
      return parseSec(a.avgDuration) - parseSec(b.avgDuration);
    },
  },
  // {
  //   title: '弹幕',
  //   dataIndex: 'danmaku',
  //   width: 80,
  //   align: 'right',
  //   sortable: { sortDirections: ['ascend', 'descend'] },
  //   sorter: (a: any, b: any) => Number(a.danmaku || 0) - Number(b.danmaku || 0),
  // },
  {
    title: '操作',
    slotName: 'action',
    width: 100,
    align: 'center',
    fixed: 'right',
  },
];

const noteList = ref<any[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
  simple: false, // 完整分页：显示左右箭头和 1、2、3… 页码
  hideOnSinglePage: false, // 只有一页时也显示分页栏，与其他页面一致
});

/** 禁用当前日期之后的日期（只能选今天及以前） */
function disabledFutureDate(current: Date) {
  return current && dayjs(current).isAfter(dayjs(), 'day');
}

function formatDateRange(arr: (string | Date)[] | unknown): {
  startTime?: string;
  endTime?: string;
} {
  if (!Array.isArray(arr) || arr.length !== 2) return {};
  const [s, e] = arr;
  let start = s ? dayjs(s).format('YYYY-MM-DD HH:mm') : undefined;
  let end = e ? dayjs(e).format('YYYY-MM-DD HH:mm') : undefined;
  const now = dayjs();
  if (end && dayjs(end).isAfter(now)) end = now.format('YYYY-MM-DD HH:mm');
  if (start && dayjs(start).isAfter(now))
    start = now.format('YYYY-MM-DD HH:mm');
  return { startTime: start, endTime: end };
}

async function fetchNoteList() {
  setLoading(true);
  try {
    const { startTime, endTime } = formatDateRange(queryParams.dateRange);
    const params: Record<string, unknown> = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    };
    if (queryParams.noteType) params.noteType = Number(queryParams.noteType);
    if (queryParams.uid) params.uid = queryParams.uid;
    if (startTime) params.startTime = startTime;
    if (endTime) params.endTime = endTime;
    const res: any = await getNoteDataCenterList(params);
    const data = res?.data ?? res;
    const rows = data?.rows ?? data?.list;
    noteList.value = Array.isArray(rows) ? rows : [];
    pagination.total = Number(data?.total ?? res?.total ?? 0);
  } catch (e) {
    noteList.value = [];
    pagination.total = 0;
    console.error('获取笔记数据失败', e);
  } finally {
    setLoading(false);
  }
}

function onPageChange(page: number) {
  pagination.current = page;
  fetchNoteList();
}

function onPageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize;
  pagination.current = 1;
  fetchNoteList();
}

function handleQuery() {
  pagination.current = 1;
  fetchNoteList();
}

function handleExport() {
  // TODO: 导出数据
}

/** 选择用户下拉：按输入内容匹配昵称或用户ID */
function filterCreatorOption(
  inputValue: string,
  option: { value: string; label: string }
) {
  if (!inputValue || !inputValue.trim()) return true;
  const kw = inputValue.trim().toLowerCase();
  const label = String(option?.label ?? '').toLowerCase();
  const value = String(option?.value ?? '').toLowerCase();
  return label.includes(kw) || value.includes(kw);
}

async function loadCreatorOptions() {
  try {
    const res: any = await getCreatorOptions();
    const data = res?.data ?? res;
    const rows = data?.rows ?? data?.list ?? [];
    creatorOptions.value = (Array.isArray(rows) ? rows : [])
      .map((r: any) => ({
        value: String(r.id ?? r.uid ?? ''),
        label: r.username ?? r.nickname ?? String(r.id ?? r.uid ?? ''),
      }))
      .filter((o: { value: string }) => o.value);
  } catch (e) {
    console.error('加载用户列表失败', e);
  }
}

function goDetail(record: any) {
  router.push({
    name: 'DataCenterNoteDetail',
    params: { id: String(record.id) },
    query: { days: 30 },
  });
}

onMounted(() => {
  loadCreatorOptions();
  fetchNoteList();
});
</script>

<script lang="ts">
export default {
  name: 'DataCenterPopularContent',
};
</script>

<style lang="less" scoped>
.popular-content-page {
  padding: 0 20px 20px;
  background-color: var(--color-fill-2);
  min-height: 100%;
}

.content-tabs {
  margin-bottom: 16px;
  :deep(.arco-tabs-header) {
    margin-bottom: 0;
  }
}

.filter-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.note-info-cell {
  .note-status {
    margin-bottom: 6px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .note-info-main {
    display: flex;
    gap: 12px;
    align-items: flex-start;
  }
  .note-cover {
    border-radius: 4px;
    flex-shrink: 0;
  }
  .note-placeholder {
    width: 80px;
    height: 60px;
    background: var(--color-fill-2);
    border-radius: 4px;
    flex-shrink: 0;
  }
  .note-meta {
    min-width: 0;
  }
  .note-title {
    font-size: 13px;
    color: var(--color-text-1);
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
  .note-time {
    font-size: 12px;
    color: var(--color-text-3);
  }
}
</style>
