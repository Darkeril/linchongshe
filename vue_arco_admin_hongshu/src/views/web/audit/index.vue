<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['笔记管理', '审核管理']"></Breadcrumb>
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
                <a-form-item field="cpid" :label="'分类'">
                  <a-tree-select
                    v-model="state.queryParams.cpid"
                    :field-names="{
                      key: 'id',
                      title: 'title',
                      children: 'children',
                    }"
                    :data="state.navbarOptions"
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
                    v-model="state.queryParams.title"
                    :placeholder="'请输入标题'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="author" :label="'作者'">
                  <a-input
                    v-model="state.queryParams.author"
                    :placeholder="'请输入作者'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="noteType" :label="'类型'">
                  <a-select
                    v-model="state.queryParams.noteType"
                    placeholder="请选择笔记类型"
                    allow-clear
                  >
                    <a-option
                      v-for="dict in state.dictOptions.noteType"
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
                    v-model="state.queryParams.auditStatus"
                    placeholder="请选择审核状态"
                    allow-clear
                  >
                    <a-option
                      v-for="dict in state.dictOptions.auditStatus"
                      :key="dict.dictValue"
                      :value="dict.dictValue"
                      :label="dict.dictLabel"
                      >{{ dict.dictLabel }}
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="fromType" :label="'笔记来源'">
                  <a-select
                    v-model="state.queryParams.fromType"
                    placeholder="请选择笔记来源"
                    allow-clear
                  >
                    <a-option
                      v-for="dict in state.dictOptions.fromType"
                      :key="dict.dictValue"
                      :value="dict.dictValue"
                      :label="dict.dictLabel"
                      >{{ dict.dictLabel }}
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="dateRange" :label="'创建时间'">
                  <a-range-picker v-model="state.dateRange" style="width: 100%">
                  </a-range-picker>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 84px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space direction="vertical" :size="18">
            <a-button type="primary" @click="fetchListData">
              <template #icon>
                <icon-search />
              </template>
              查询
            </a-button>
            <a-button @click="resetQueryForm">
              <template #icon>
                <icon-refresh />
              </template>
              重置
            </a-button>
          </a-space>
        </a-col>

        <a-divider style="margin-top: 0" />
        <!-- 工具栏 -->
        <a-row style="margin-bottom: 16px">
          <!-- 前置工具栏部分 -->
          <a-col :span="12">
            <a-space>
              <a-button
                type="primary"
                :disabled="state.selectedKeys.length <= 0"
                @click="openBatchAudit('pass')"
                >批量通过</a-button
              >
              <a-button
                status="danger"
                :disabled="state.selectedKeys.length <= 0"
                @click="openBatchAudit('reject')"
                >批量驳回</a-button
              >
            </a-space>
          </a-col>
        </a-row>
      </a-row>

      <!-- 表格数据-->
      <a-table
        ref="tableRef"
        v-model:selected-keys="state.selectedKeys"
        row-key="id"
        :loading="loading"
        :pagination="pagination"
        :columns="state.columns"
        :data="state.list"
        :row-selection="state.rowSelection"
        :bordered="false"
        :size="'medium'"
        @select="handleSelected"
        @sorter-change="handleSorterChange"
        @page-change="onPageChange"
        @row-click="handleRowClick"
      >
        <template #cpid="{ record }">
          <a-tag color="gray">{{ getCategoryTitle(record.cpid) }}</a-tag>
        </template>
        <template #auditStatus="{ record }">
          <dict-tag
            :options="state.dictOptions.auditStatus"
            :value="record.auditStatus"
          />
        </template>
        <template #noteType="{ record }">
          <dict-tag
            :options="state.dictOptions.noteType"
            :value="record.noteType"
          />
        </template>
        <template #fromType="{ record }">
          <dict-tag
            :options="state.dictOptions.fromType"
            :value="record.fromType"
          />
        </template>
        <template #content="{ record }">
          <a-tooltip :content="stripHtml(record.content)">
            <div class="text-ellipsis">{{ stripHtml(record.content) }}</div>
          </a-tooltip>
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button-group>
              <a-button type="text" size="mini" @click.stop="handleView(record)">
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
  </div>

  <!-- 笔记详情弹窗 -->
  <a-modal
    v-model:visible="state.detailVisible"
    title="笔记详情"
    width="80%"
    :footer="false"
    :mask-closable="false"
    @cancel="handleDetailClose"
    @close="handleDetailClose"
  >
    <div v-if="state.currentNote" class="note-detail">
      <a-row :gutter="24">
        <a-col :span="12">
          <div class="detail-image-container">
            <img
              v-if="!state.isVideoNote"
              :src="state.currentImageSrc"
              class="detail-image"
              @click="() => {}"
            />
            <video
              v-else
              ref="detailVideoRef"
              :src="state.currentImageSrc"
              :poster="state.currentNote.noteCover"
              controls
              class="detail-video"
              style="width: 100%; height: 100%; object-fit: contain"
            />
            <div
              v-if="!state.isVideoNote && state.imageList.length > 1"
              class="image-navigation"
            >
              <a-button
                type="primary"
                shape="circle"
                size="small"
                :disabled="state.currentImageIndex === 0"
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
                :disabled="
                  state.currentImageIndex === state.imageList.length - 1
                "
                @click="nextImage"
              >
                <template #icon>
                  <icon-right />
                </template>
              </a-button>
            </div>
            <div
              v-if="!state.isVideoNote && state.imageList.length > 1"
              class="image-counter"
            >
              {{ state.currentImageIndex + 1 }} / {{ state.imageList.length }}
            </div>
          </div>
        </a-col>
        <a-col :span="12">
          <div class="detail-info">
            <h2 class="detail-title">{{ state.currentNote.title }}</h2>
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="作者">
                {{ state.currentNote.author }}
              </a-descriptions-item>
              <a-descriptions-item label="分类">
                {{ getCategoryTitle(state.currentNote.cpid) }}
              </a-descriptions-item>
              <a-descriptions-item label="类型">
                <dict-tag
                  :options="state.dictOptions.noteType"
                  :value="state.currentNote.noteType"
                />
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <dict-tag
                  :options="state.dictOptions.auditStatus"
                  :value="state.currentNote.auditStatus"
                />
              </a-descriptions-item>
              <a-descriptions-item label="来源">
                <dict-tag
                  :options="state.dictOptions.fromType"
                  :value="state.currentNote.fromType"
                />
              </a-descriptions-item>
              <a-descriptions-item label="图片数量">
                {{ state.currentNote.count || 0 }} 张
              </a-descriptions-item>
              <a-descriptions-item label="浏览次数">
                {{ state.currentNote.viewCount || 0 }}
              </a-descriptions-item>
              <a-descriptions-item label="创建时间" :span="2">
                {{ formatDate(state.currentNote.createTime) }}
              </a-descriptions-item>
            </a-descriptions>
            <div class="detail-description">
              <h4>笔记内容</h4>
              <p>{{ stripHtml(state.currentNote.content) || '暂无内容' }}</p>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import useLoading from '@/hooks/loading';
import { computed, reactive, h, ref, watch } from 'vue';
import AImage from '@arco-design/web-vue/es/image';
import { DictDataRecord, getDicts } from '@/api/system/dict-data';
import { Message, Modal } from '@arco-design/web-vue';
import { Pagination } from '@/types/global';
import addDateRange from '@/utils/common';
import {
  unAuditList,
  addNote,
  delNote,
  getNote,
  updateNote,
  auditNote,
  batchAuditNote,
} from '@/api/web/note';
import { download } from '@/api/download';
import {
  TableColumnData,
  TableData,
} from '@arco-design/web-vue/es/table/interface';
import { listNavbar } from '@/api/web/navbar';
import DictTag from '@/components/dict-tag/index.vue';

const { loading, setLoading } = useLoading(true);

const basePagination: Pagination = {
  current: 1,
  pageSize: 10,
};
const pagination = reactive({
  ...basePagination,
});

const state = reactive({
  queryParams: {
    cpid: undefined as number | undefined,
    title: undefined,
    author: undefined,
    noteType: undefined,
    auditStatus: undefined,
    fromType: undefined,
    orderByColumn: undefined as string | undefined,
    isAsc: undefined as string | undefined,
  },
  rowSelection: {
    type: 'checkbox',
    showCheckedAll: true,
    onlyCurrent: false,
  } as unknown,
  // 表格选中值
  selectedKeys: [],
  selectName: '',
  // 时间选择
  dateRange: [],
  // 显示编辑页面
  editVisible: false,
  // 列表数据
  list: [],
  // 字典
  dictOptions: {
    auditStatus: [] as DictDataRecord[],
    noteType: [] as DictDataRecord[],
    fromType: [] as DictDataRecord[],
  },
  // 选中行ID
  currentId: 0,
  // 详情相关状态
  detailVisible: false,
  currentNote: null as any,
  currentImageIndex: 0,
  imageList: [] as string[],
  currentImageSrc: '',
  isVideoNote: false,
  categoriesMap: {} as Record<string, string>,
  navbarOptions: [] as any[],
  // 列表显示列
  columns: computed<TableColumnData[]>(() => [
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
      title: '笔记封面',
      dataIndex: 'noteCover',
      slotName: 'noteCover',
      width: 100,
      align: 'center',
      render: ({ record }) =>
        h(AImage, {
          src: record.noteCover,
          width: '60px',
          height: '60px',
          preview: true, // 支持图片预览
          alt: '头像',
          onClick: (e: Event) => e.stopPropagation(),
        }),
    },
    {
      title: '内容',
      dataIndex: 'content',
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
      dataIndex: 'noteType',
      slotName: 'noteType',
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
      slotName: 'optional',
      fixed: 'right',
      width: 200,
      align: 'center',
    },
  ]),
});

const detailVideoRef = ref<HTMLVideoElement | null>(null);

const stopDetailPlayback = () => {
  const videoEl = detailVideoRef.value;
  if (videoEl) {
    try {
      videoEl.pause();
      videoEl.currentTime = 0;
      videoEl.removeAttribute('src');
      videoEl.load();
    } catch (error) {
      console.warn('停止审核详情视频失败', error);
    }
  }
  state.currentImageSrc = '';
};

const resetDetailState = () => {
  state.currentNote = null;
  state.imageList = [];
  state.currentImageIndex = 0;
  state.isVideoNote = false;
};

watch(
  () => state.detailVisible,
  (visible) => {
    if (!visible) {
      stopDetailPlayback();
      resetDetailState();
      detailVideoRef.value = null;
    }
  }
);

const handleDetailClose = () => {
  state.detailVisible = false;
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

// 初始化字典
const initData = async () => {
  function parseToArray(input: any) {
    // 已是数组
    if (Array.isArray(input)) return input;
    // 字符串：尝试 JSON.parse
    if (typeof input === 'string') {
      const str = input.trim();
      try {
        const parsed = JSON.parse(str);
        if (Array.isArray(parsed)) return parsed;
      } catch (_) {
        // 非 JSON 数组：尝试逗号/换行分隔，元素支持 "值:名称" 或 "值|名称" 或单值
        const parts = str.split(/[,\n]\s*/).filter(Boolean);
        if (parts.length) {
          return parts.map((p) => {
            const seg = p.split(/[:|，：]/);
            const value = (seg[0] ?? '').trim();
            const label = (seg[1] ?? value).trim();
            return { dictValue: value, dictLabel: label } as any;
          });
        }
      }
    }
    // 其它：返回空数组
    return [];
  }

  async function safeGetDict(key: string) {
    try {
      const res: any = await getDicts(key);
      const data = res && (res.data !== undefined ? res.data : res);
      return parseToArray(data);
    } catch (e) {
      console.warn(`[dict] load failed: ${key}`, e);
      return [];
    }
  }

  const [auditStatusResponse, noteTypeResponse, fromTypeResponse] =
    await Promise.all([
      safeGetDict('audit_status'),
      safeGetDict('note_type'),
      safeGetDict('from_type'),
    ]);

  // 定义笔记来源字典（0代表web端，1代表app端）
  const fromType = [
    { dictValue: '0', dictLabel: 'web端', listClass: 'primary' },
    { dictValue: '1', dictLabel: 'app端', listClass: 'success' },
  ];

  state.dictOptions = {
    auditStatus: auditStatusResponse,
    noteType: noteTypeResponse,
    fromType: fromTypeResponse.length > 0 ? fromTypeResponse : fromType,
  };

  // 加载分类数据
  try {
    const categoryRes = await listNavbar({});
    const categories = categoryRes.data || [];
    // 构建分类映射表
    const categoriesMap: Record<string, string> = {};
    categories.forEach((category: any) => {
      if (category.id && category.title) {
        categoriesMap[category.id] = category.title;
      }
    });
    state.categoriesMap = categoriesMap;

    // 将列表数据转换为树结构
    const treeData = listToTree(categories);
    // 添加"主类目"作为父级节点
    state.navbarOptions = [
      {
        id: 0,
        title: '主类目',
        children: treeData,
      },
    ];
  } catch (e) {
    console.warn('[category] load failed', e);
  }
};
initData();

// 获取分类标题
const getCategoryTitle = (cpid: string) => {
  return state.categoriesMap[cpid] || cpid;
};

// 重置查询表单
const resetQueryForm = () => {
  state.queryParams = {
    cpid: undefined,
    title: undefined,
    author: undefined,
    noteType: undefined,
    auditStatus: undefined,
    fromType: undefined,
    orderByColumn: undefined,
    isAsc: undefined,
  };
  state.selectName = '';
  state.dateRange = [];
  pagination.current = 1;
  // eslint-disable-next-line no-use-before-define
  fetchListData();
};

// 获取列表数据（前置声明，避免 no-use-before-define）
async function fetchListData() {
  setLoading(true);
  // 构建请求参数，将 cpid 转换为 pid（后端API使用 pid 参数）
  const requestParams: any = {
    ...pagination,
    ...addDateRange(state.queryParams, state.dateRange),
  };
  // 如果查询参数中有 cpid，需要转换为 pid 传递给后端
  if (state.queryParams.cpid) {
    requestParams.pid = state.queryParams.cpid;
    // 删除 cpid，避免传递错误的参数
    delete requestParams.cpid;
  }
  const { rows, total } = await unAuditList(requestParams);
  state.list = rows;
  pagination.total = total;
  setLoading(false);
}
fetchListData();

/** 审核按钮操作 */
const handleAudit = (noteId: string | number, auditType: string) => {
  const data = {
    noteId,
    auditType,
  };
  // 显示确认框
  Modal.confirm({
    title: `是否确认${auditType === 'pass' ? '通过' : '拒绝'}该笔记？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await auditNote(data);
        Message.success(`${auditType === 'pass' ? '通过' : '拒绝'}成功`);
        // eslint-disable-next-line no-use-before-define
        fetchListData();
      } catch (err) {
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
  if (!state.selectedKeys.length) {
    Message.error('请选择需要审核的记录');
    return;
  }
  Modal.confirm({
    title: `是否确认批量${auditType === 'pass' ? '通过' : '驳回'}所选笔记？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        // 将选中的ID数组转换为逗号分隔的字符串
        const noteIds = (state.selectedKeys as Array<string | number>).join(',');
        await batchAuditNote({
          noteIds,
          auditType,
        });
        Message.success(`批量${auditType === 'pass' ? '通过' : '驳回'}成功`);
        // 清空选中项
        state.selectedKeys = [];
        fetchListData();
      } catch (e) {
        Message.error('批量审核失败');
      }
    },
  });
};

function formatDate(time?: string | number) {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
}

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

function normalizeUrlString(value: string) {
  if (typeof value !== 'string') return '';
  const trimmed = value.trim();
  if (!trimmed) return '';
  return trimmed.replace(/^"(.*)"$/, '$1');
}

function isProbablyVideoUrl(url: string) {
  if (typeof url !== 'string') return false;
  const normalized = url.toLowerCase();
  const videoExtensions = [
    '.mp4',
    '.mov',
    '.m4v',
    '.avi',
    '.wmv',
    '.flv',
    '.f4v',
    '.rmvb',
    '.rm',
    '.3gp',
    '.ts',
    '.mkv',
    '.webm',
    '.m3u8',
  ];
  return videoExtensions.some((ext) => normalized.includes(ext));
}

function extractUrlArray(value: any): string[] {
  if (!value) return [];

  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (!trimmed) return [];

    if (
      (trimmed.startsWith('[') && trimmed.endsWith(']')) ||
      (trimmed.startsWith('{') && trimmed.endsWith('}'))
    ) {
      try {
        const parsed = JSON.parse(trimmed);
        return extractUrlArray(parsed);
      } catch (error) {
        // ignore parse error
      }
    }

    const segments = trimmed.split(/[,，\s]/).filter(Boolean);
    if (segments.length > 1) {
      return segments
        .map((item) => normalizeUrlString(item))
        .filter((item) => !!item);
    }

    return [normalizeUrlString(trimmed)].filter((item) => !!item);
  }

  if (Array.isArray(value)) {
    const result: string[] = [];
    value.forEach((item) => {
      const urls = extractUrlArray(item);
      if (urls.length) {
        result.push(...urls);
      }
    });
    return result;
  }

  if (typeof value === 'object') {
    const candidates = [
      value.url,
      value.src,
      value.value,
      value.path,
      value.link,
    ];
    const result: string[] = [];
    candidates.forEach((item) => {
      const urls = extractUrlArray(item);
      if (urls.length) {
        result.push(...urls);
      }
    });
    return result;
  }

  return [];
}

function extractImageUrls(value: any): string[] {
  const urls = extractUrlArray(value);
  const result: string[] = [];
  urls.forEach((item) => {
    const normalized = normalizeUrlString(item);
    if (
      normalized &&
      !isProbablyVideoUrl(normalized) &&
      !result.includes(normalized)
    ) {
      result.push(normalized);
    }
  });
  return result;
}

function initImageList(note: any): string[] {
  const orderedSources = [
    note.imageList,
    note.images,
    note.imageUrls,
    note.pictures,
    note.photoList,
    note.urls,
    note.imageListStr,
    note.covers,
    note.albumImages,
  ];

  for (let i = 0; i < orderedSources.length; i += 1) {
    const candidates = extractImageUrls(orderedSources[i]);
    if (candidates.length > 0) {
      return Array.from(new Set(candidates));
    }
  }

  const fallbackImages = extractImageUrls([
    note.noteCover,
    note.cover,
    note.thumbnail,
  ]);

  return fallbackImages.length > 0 ? [fallbackImages[0]] : [];
}

function extractFirstUrl(value: any): string {
  if (!value) return '';

  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (!trimmed) return '';

    if (
      (trimmed.startsWith('[') && trimmed.endsWith(']')) ||
      (trimmed.startsWith('{') && trimmed.endsWith('}'))
    ) {
      try {
        const parsed = JSON.parse(trimmed);
        if (Array.isArray(parsed)) {
          for (let i = 0; i < parsed.length; i += 1) {
            const url = extractFirstUrl(parsed[i]);
            if (url) {
              return url;
            }
          }
          return '';
        }
        if (parsed && typeof parsed === 'object') {
          return extractFirstUrl(parsed.url || parsed.src || parsed.value);
        }
      } catch (error) {
        // ignore parse error
      }
    }

    const segments = trimmed.split(/[,，\s]/).filter(Boolean);
    if (segments.length > 1) {
      for (let i = 0; i < segments.length; i += 1) {
        const normalized = normalizeUrlString(segments[i]);
        if (normalized) {
          return normalized;
        }
      }
      return '';
    }

    return normalizeUrlString(trimmed);
  }

  if (Array.isArray(value)) {
    for (let i = 0; i < value.length; i += 1) {
      const url = extractFirstUrl(value[i]);
      if (url) {
        return url;
      }
    }
    return '';
  }

  if (typeof value === 'object') {
    if (value.url) return extractFirstUrl(value.url);
    if (value.src) return extractFirstUrl(value.src);
    if (value.value) return extractFirstUrl(value.value);
  }

  return '';
}

const resolveVideoUrl = (note: any, fallback?: any) => {
  if (!note && !fallback) return '';
  const source = note || {};
  const fallbackSource = fallback || {};
  const type = Number(
    source.noteType !== undefined ? source.noteType : fallbackSource?.noteType
  );
  const candidates = [
    extractFirstUrl(source.videoUrl),
    extractFirstUrl(source.video),
    extractFirstUrl(source.videoPath),
    extractFirstUrl(source.videoUrls),
    extractFirstUrl(source.videoList),
    extractFirstUrl(source.videoSrc),
    extractFirstUrl(source.urls),
    extractFirstUrl(source.videos),
    extractFirstUrl(fallbackSource?.videoUrl),
    extractFirstUrl(fallbackSource?.video),
    extractFirstUrl(fallbackSource?.videoPath),
    extractFirstUrl(fallbackSource?.videoUrls),
    extractFirstUrl(fallbackSource?.videoList),
    extractFirstUrl(fallbackSource?.videoSrc),
    extractFirstUrl(fallbackSource?.urls),
    extractFirstUrl(fallbackSource?.videos),
  ];
  const normalizedCandidates = candidates.filter(
    (item) => typeof item === 'string' && item.trim().length > 0
  ) as string[];

  if (normalizedCandidates.length === 0) {
    return '';
  }

  if (type === 2) {
    return (
      normalizedCandidates.find((candidate) => isProbablyVideoUrl(candidate)) ||
      normalizedCandidates[0]
    );
  }

  return (
    normalizedCandidates.find((candidate) => isProbablyVideoUrl(candidate)) || ''
  );
};

const loadNoteDetail = async (record: any) => {
  stopDetailPlayback();
  const noteId =
    typeof record === 'object' && record !== null ? record.id : record;
  try {
    const { data } =
      noteId !== undefined && noteId !== null
        ? await getNote(noteId)
        : { data: record };
    const fullNote =
      typeof record === 'object' && record !== null
        ? { ...record, ...data }
        : data;
    const videoSrc = resolveVideoUrl(fullNote, record);
    const hasVideo = !!videoSrc;

    if (videoSrc) {
      fullNote.videoUrl = videoSrc;
    }

    state.currentNote = fullNote;
    state.isVideoNote = hasVideo;
    state.imageList = hasVideo ? [] : initImageList(fullNote);
    state.currentImageIndex = 0;
    state.currentImageSrc = hasVideo
      ? videoSrc || fullNote?.noteCover || ''
      : state.imageList[0] || fullNote?.noteCover || '';
    state.detailVisible = true;
  } catch (e) {
    Message.error('获取笔记详情失败');
  }
};

// 查看详情
const handleView = (record: any) => {
  loadNoteDetail(record);
};

// 行点击事件
const handleRowClick = (record: any) => {
  loadNoteDetail(record);
};

const prevImage = () => {
  if (state.currentImageIndex > 0) {
    state.currentImageIndex -= 1;
    state.currentImageSrc = state.imageList[state.currentImageIndex];
  }
};

const nextImage = () => {
  if (state.currentImageIndex < state.imageList.length - 1) {
    state.currentImageIndex += 1;
    state.currentImageSrc = state.imageList[state.currentImageIndex];
  }
};

// 分页
const onPageChange = (current: number) => {
  pagination.current = current;
  fetchListData();
};

// 多选框选中数据
const handleSelected = (
  rowKeys: Array<number>,
  rowKey: string | number,
  record: TableData
) => {
  state.selectName = record.userName;
};

// 排序触发事件
const handleSorterChange = (
  dataIndex: string,
  direction: string | undefined
) => {
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
  fetchListData();
};

// 打开新增
const handleAdd = (id: number) => {
  state.currentId = 0;
  state.editVisible = true;
};

// 删除
const handleDelete = () => {
  const ids = state.selectedKeys.join(',');
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: `是否确认删除访问编号为"${ids}"的数据项？`,
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      delNote(ids).then(() => {
        Message.success('删除成功');
        fetchListData();
      });
    },
  });
};

// 清空按钮操作
const handleClean = () => {
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: '是否确认清空所有登录日志数据项？',
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      delNote().then(() => {
        Message.success('清空成功');
        fetchListData();
      });
    },
  });
};

// 导出按钮操作
const handleExport = () => {
  download(
    'web/member/export',
    state.queryParams,
    `logininfor_${new Date().getTime()}.xlsx`
  );
};
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}

.note-detail {
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
  }

  .detail-image {
    max-width: 100%;
    max-height: 100%;
    width: auto;
    height: auto;
    object-fit: contain;
  }

  .detail-video {
    max-width: 100%;
    max-height: 100%;
    width: auto;
    height: auto;
    object-fit: contain;
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

  .detail-info {
    .detail-title {
      margin: 0 0 12px 0;
    }
    .detail-description {
      margin-top: 12px;
    }
  }
}

.text-ellipsis {
  max-width: 130px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  word-break: break-all;
}
</style>
