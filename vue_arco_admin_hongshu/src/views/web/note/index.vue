<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb
      :items="[$t('biz.adminNoteMenuTitle'), $t('biz.adminNoteMenuTitle')]"
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
                <a-form-item field="cpid" :label="$t('biz.category')">
                  <a-tree-select
                    v-model="state.queryParams.cpid"
                    :field-names="{
                      key: 'id',
                      title: 'title',
                      children: 'children',
                    }"
                    :data="state.navbarOptions"
                    :show-count="false"
                    :placeholder="$t('biz.selectCategory')"
                    allow-clear
                  >
                  </a-tree-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="title" :label="$t('biz.noteTitle')">
                  <a-input
                    v-model="state.queryParams.title"
                    :placeholder="$t('biz.pleaseInputTitle')"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="author" :label="$t('biz.author')">
                  <a-input
                    v-model="state.queryParams.author"
                    :placeholder="$t('biz.pleaseInputAuthor')"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="noteType" :label="$t('biz.noteType')">
                  <a-select
                    v-model="state.queryParams.noteType"
                    :placeholder="$t('biz.selectNoteTypePlaceholder')"
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
                <a-form-item field="auditStatus" :label="$t('biz.auditStatus')">
                  <a-select
                    v-model="state.queryParams.auditStatus"
                    :placeholder="$t('biz.selectNoteAuditPlaceholder')"
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
                <a-form-item field="fromType" :label="$t('biz.columnNoteSource')">
                  <a-select
                    v-model="state.queryParams.fromType"
                    :placeholder="$t('biz.selectNoteFromPlaceholder')"
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
                <a-form-item field="dateRange" :label="$t('biz.createTime')">
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
              {{ $t('biz.query') }}
            </a-button>
            <a-button @click="resetQueryForm">
              <template #icon>
                <icon-refresh />
              </template>
              {{ $t('common.reset') }}
            </a-button>
          </a-space>
        </a-col>

        <a-divider style="margin-top: 0" />
        <!-- 工具栏 -->
        <a-row style="margin-bottom: 16px">
          <!-- 前置工具栏部分 -->
          <a-col :span="12">
            <a-space>
              <a-space>
                <a-button
                  v-permission="['web:category:add']"
                  type="primary"
                  @click="handleAdd(0)"
                >
                  <template #icon>
                    <IconPlus />
                  </template>
                  {{ $t('common.add') }}
                </a-button>
                <a-button
                  v-permission="['web:member:remove']"
                  status="danger"
                  :disabled="state.selectedKeys.length <= 0"
                  @click="handleDelete"
                >
                  <template #icon>
                    <IconDelete />
                  </template>
                  {{ $t('common.delete') }}
                </a-button>
                <!-- <a-button
                  v-permission="['monitor:logininfor:export']"
                  status="warning"
                  @click="handleExport"
                >
                  <template #icon>
                    <IconDownload />
                  </template>
                  导出
                </a-button> -->
                <a-button status="warning" @click="refreshNote">
                  <template #icon>
                    <IconRefresh />
                  </template>
                  {{ $t('biz.esReset') }}
                </a-button>
                <a-button status="warning" @click="clearAllRecord">
                  <template #icon>
                    <IconRefresh />
                  </template>
                  {{ $t('biz.esClearAction') }}
                </a-button>
              </a-space>
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
        <template #status="{ record }">
          <dict-tag
            :options="state.dictOptions.status"
            :value="record.status"
          />
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
        <template #title="{ record }">
          <a-tooltip :content="record.title">
            <div class="text-ellipsis">{{ record.title }}</div>
          </a-tooltip>
        </template>
        <template #content="{ record }">
          <a-tooltip :content="stripHtml(record.content)">
            <div class="text-ellipsis">{{ stripHtml(record.content) }}</div>
          </a-tooltip>
        </template>
        <template #location="{ record }">
          <a-tooltip :content="formatAddress(record)">
            <div class="text-ellipsis">{{ formatAddress(record) }}</div>
          </a-tooltip>
        </template>
        <template #cpid="{ record }">
          <a-tag color="blue">{{ getCategoryTitle(record.cpid) }}</a-tag>
        </template>
        <template #albumId="{ record }">
          <a-tag v-if="record.albumId" color="blue">{{ record.albumTitle || record.albumId }}</a-tag>
          <span v-else style="color: #999;">-</span>
        </template>
        <template #pinned="{ record }">
          <dict-tag
            :options="state.dictOptions.pinned"
            :value="record.pinned"
          />
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button-group>
              <a-button
                type="text"
                size="mini"
                @click.stop="handleView(record)"
              >
                <template #icon>
                  <IconEye />
                </template>
                {{ $t('biz.view') }}
              </a-button>
              <a-button
                v-permission="['web:category:edit']"
                type="text"
                size="mini"
                @click.stop="handleUpdate(record.id)"
              >
                <template #icon>
                  <IconEdit />
                </template>
                {{ $t('biz.modify') }}
              </a-button>
              <a-button
                v-permission="['web:category:remove']"
                type="text"
                size="mini"
                @click.stop="handleDelete(record)"
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
  </div>
  <edit-note
    v-model:id="state.currentId"
    v-model:visible="state.editVisible"
    @refresh-data-list="fetchListData"
  >
  </edit-note>

  <!-- 笔记详情弹窗 -->
  <a-modal
    v-model:visible="state.detailVisible"
    :title="$t('biz.noteDetailModalTitle')"
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
              v-if="!isVideoNote"
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
            />
            <div
              v-if="!isVideoNote && state.imageList.length > 1"
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
                :disabled="state.currentImageIndex === state.imageList.length - 1"
                @click="nextImage"
              >
                <template #icon>
                  <icon-right />
                </template>
              </a-button>
            </div>
            <div v-if="!isVideoNote && state.imageList.length > 1" class="image-counter">
              {{ state.currentImageIndex + 1 }} / {{ state.imageList.length }}
            </div>
          </div>
        </a-col>
        <a-col :span="12">
          <div class="detail-info">
            <h2 class="detail-title">{{ state.currentNote.title }}</h2>
            <a-descriptions :column="2" bordered>
              <a-descriptions-item :label="$t('biz.author')">
                {{ state.currentNote.author }}
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.category')">
                {{ getCategoryTitle(state.currentNote.cpid) }}
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.noteType')">
                <dict-tag
                  :options="state.dictOptions.noteType"
                  :value="state.currentNote.noteType"
                />
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.auditStatus')">
                <dict-tag
                  :options="state.dictOptions.auditStatus"
                  :value="state.currentNote.auditStatus"
                />
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.pinned')">
                <dict-tag
                  :options="state.dictOptions.pinned"
                  :value="state.currentNote.pinned"
                />
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.publishLocation')">
                {{ formatAddress(state.currentNote) || $t('biz.unknown') }}
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.columnNoteSource')">
                <dict-tag
                  :options="state.dictOptions.fromType"
                  :value="state.currentNote.fromType"
                />
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.imageCount')">
                {{ state.currentNote.count || 0 }} {{ $t('biz.imageUnit') }}
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.viewCountCol')">
                {{ state.currentNote.viewCount || 0 }}
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.likeCount')">
                {{ state.currentNote.likeCount || 0 }}
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.collectionCountCol')">
                {{ state.currentNote.collectionCount || 0 }}
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.commentCountCol')">
                {{ state.currentNote.commentCount || 0 }}
              </a-descriptions-item>
              <a-descriptions-item :label="$t('biz.createTime')" :span="2">
                {{ formatDate(state.currentNote.createTime) }}
              </a-descriptions-item>
            </a-descriptions>
            <div class="detail-description">
              <h4>{{ $t('biz.noteBodyHeading') }}</h4>
              <p>
                {{ stripHtml(state.currentNote.content) || $t('biz.emptyNoteBody') }}
              </p>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import useLoading from '@/hooks/loading';
import { computed, reactive, h, ref, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import AImage from '@arco-design/web-vue/es/image';
import { DictDataRecord, getDicts } from '@/api/system/dict-data';
import { Message, Modal } from '@arco-design/web-vue';
import { Pagination } from '@/types/global';
import addDateRange, { currentLocale } from '@/utils/common';
import { getTodayRange } from '@/utils/workbench-nav';
import {
  listNote,
  addNote,
  delNote,
  getNote,
  updateNote,
  refreshNoteDate,
  clearAllRecordDate,
} from '@/api/web/note';
import { download } from '@/api/download';
import {
  TableColumnData,
  TableData,
} from '@arco-design/web-vue/es/table/interface';
import { IconEdit } from '@arco-design/web-vue/es/icon';
import { listNavbar } from '@/api/web/navbar';
import { getAlbumList } from '@/api/web/album';
import DictTag from '@/components/dict-tag/index.vue';
import EditNote from './components/edit-note.vue';

const { loading, setLoading } = useLoading(true);
const route = useRoute();
const { t } = useI18n();

const basePagination: Pagination = {
  current: 1,
  pageSize: 10,
};
const pagination = reactive({
  ...basePagination,
});

// 将小红书图片URL转换为Nginx代理路径
// 根据Nginx配置: proxy_pass https://sns-webpic-qc.xhscdn.com/;
// 请求 /xhspic/xxx 会被转发到 https://sns-webpic-qc.xhscdn.com/xxx
const getNginxProxyImageUrl = (imageUrl: string | undefined): string => {
  if (!imageUrl || !imageUrl.trim()) {
    return '';
  }
  
  // 如果已经是相对路径（以 /xhspic/ 开头），直接返回
  if (imageUrl.startsWith('/xhspic/')) {
    return imageUrl;
  }
  
  // 如果已经是 base64 数据，直接返回
  if (imageUrl.startsWith('data:')) {
    return imageUrl;
  }
  
  // 如果已经是OSS URL（包含OSS域名），直接返回
  if (imageUrl.includes('oss') || imageUrl.includes('aliyuncs.com') || imageUrl.includes('qcloud.com')) {
    return imageUrl;
  }
  
  // 如果是本地开发服务器的URL（localhost、127.0.0.1），直接返回
  if (imageUrl.includes('localhost') || imageUrl.includes('127.0.0.1')) {
    return imageUrl;
  }
  
  // 如果是小红书图片URL，转换为Nginx代理路径
  if (imageUrl.includes('sns-webpic-qc.xhscdn.com') || 
      imageUrl.includes('xhscdn.com') ||
      imageUrl.includes('xiaohongshu.com')) {
    try {
      const url = new URL(imageUrl);
      // 提取路径部分，转换为 /xhspic/ 开头的路径
      // 例如: http://sns-webpic-qc.xhscdn.com/202512301548/xxx.jpg
      // 转换为: /xhspic/202512301548/xxx.jpg
      const path = url.pathname.startsWith('/') ? url.pathname : `/${url.pathname}`;
      return `/xhspic${path}${url.search}`;
    } catch (e) {
      // 如果URL解析失败，尝试简单替换
      const match = imageUrl.match(/https?:\/\/[^/]+(\/.*)/);
      if (match) {
        const path = match[1].startsWith('/') ? match[1] : `/${match[1]}`;
        return `/xhspic${path}`;
      }
      // 如果都失败了，返回原始URL
      return imageUrl;
    }
  }
  
  // 非小红书图片，直接返回原始URL
  return imageUrl;
};

// 将小红书视频URL转换为Nginx代理路径
// 根据Nginx配置: proxy_pass https://sns-video-bd.xhscdn.com/;
// 请求 /xhsvideo/xxx 会被转发到 https://sns-video-bd.xhscdn.com/xxx
const getNginxProxyVideoUrl = (videoUrl: string | undefined): string => {
  if (!videoUrl || !videoUrl.trim()) {
    return '';
  }
  
  // 如果已经是相对路径（以 /xhsvideo/ 开头），直接返回
  if (videoUrl.startsWith('/xhsvideo/')) {
    return videoUrl;
  }
  
  // 如果已经是OSS URL（包含 OSS 域名），直接返回
  if (videoUrl.includes('oss') || videoUrl.includes('aliyuncs.com') || videoUrl.includes('qcloud.com')) {
    return videoUrl;
  }
  
  // 如果是本地开发服务器的URL（localhost、127.0.0.1），直接返回
  if (videoUrl.includes('localhost') || videoUrl.includes('127.0.0.1')) {
    return videoUrl;
  }
  
  // 如果是小红书视频URL，转换为Nginx代理路径
  if (videoUrl.includes('sns-video-bd.xhscdn.com') || 
      videoUrl.includes('xhscdn.com') ||
      videoUrl.includes('xiaohongshu.com')) {
    try {
      const url = new URL(videoUrl);
      // 提取路径部分，转换为 /xhsvideo/ 开头的路径
      // 例如: https://sns-video-bd.xhscdn.com/1040g2so31o126tdkl2805n1g4gf1phsh0qcvug0
      // 转换为: /xhsvideo/1040g2so31o126tdkl2805n1g4gf1phsh0qcvug0
      const path = url.pathname.startsWith('/') ? url.pathname : `/${url.pathname}`;
      return `/xhsvideo${path}${url.search}`;
    } catch (e) {
      // 如果URL解析失败，尝试简单替换
      const match = videoUrl.match(/https?:\/\/[^/]+(\/.*)/);
      if (match) {
        const path = match[1].startsWith('/') ? match[1] : `/${match[1]}`;
        return `/xhsvideo${path}`;
      }
      // 如果都失败了，返回原始URL
      console.warn('无法解析视频URL，返回原始URL:', videoUrl);
      return videoUrl;
    }
  }
  
  // 非小红书视频，直接返回原始URL
  return videoUrl;
};

const state = reactive({
  queryParams: {
    hsId: '',
    username: undefined,
    phone: undefined,
    auditStatus: undefined,
    cpid: undefined as number | undefined,
    title: undefined,
    author: undefined,
    uid: undefined,
    noteType: undefined,
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
    status: [] as DictDataRecord[],
    auditStatus: [] as DictDataRecord[],
    noteType: [] as DictDataRecord[],
    pinned: [] as DictDataRecord[],
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
  navbarOptions: [] as any[],
  categoriesMap: {} as Record<string, string>,
  albumsMap: {} as Record<string, string>,
  // 列表显示列
  columns: computed<TableColumnData[]>(() => {
    return [
    {
      title: t('biz.columnRecordNo'),
      dataIndex: 'id',
      slotName: 'id',
      width: 60,
      align: 'center',
    },
    {
      title: t('biz.category'),
      dataIndex: 'cpid',
      slotName: 'cpid',
      width: 100,
      align: 'center',
    },
    {
      title: t('biz.noteTitle'),
      dataIndex: 'title',
      slotName: 'title',
      width: 120,
      align: 'center',
    },
    {
      title: t('biz.author'),
      dataIndex: 'author',
      slotName: 'author',
      width: 100,
      align: 'center',
    },
    {
      title: t('biz.columnNoteCover'),
      dataIndex: 'noteCover',
      slotName: 'noteCover',
      width: 100,
      align: 'center',
      render: ({ record }) => {
        // 优先使用 noteCover，如果没有则尝试从 urls 字段提取第一张图片
        let coverUrl = record.noteCover;
        if (!coverUrl && record.urls) {
          // 尝试解析 urls 字段（可能是 JSON 字符串或数组）
          try {
            let urls: string[] = [];
            if (typeof record.urls === 'string') {
              const trimmed = record.urls.trim();
              if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
                urls = JSON.parse(trimmed);
              } else {
                urls = trimmed.split(/[,，\s]/).filter(Boolean);
              }
            } else if (Array.isArray(record.urls)) {
              urls = record.urls;
            }
            // 过滤出图片URL（排除视频）
            const imageUrls = urls.filter((url: string) => {
              if (!url || typeof url !== 'string') return false;
              const normalized = url.toLowerCase();
              return !normalized.includes('.mp4') && 
                     !normalized.includes('.mov') && 
                     !normalized.includes('.m4v') &&
                     !normalized.includes('video');
            });
            const [firstImageUrl] = imageUrls;
            if (firstImageUrl) {
              coverUrl = firstImageUrl;
            }
          } catch (e) {
            // 解析失败，忽略
            console.warn('解析 urls 字段失败:', e, record.urls);
          }
        }
        const proxyUrl = getNginxProxyImageUrl(coverUrl);
        return h(AImage, {
          src: proxyUrl,
          width: '60px',
          height: '60px',
          preview: true,
          alt: t('biz.cover'),
          style: { borderRadius: '4px' },
          onClick: (e: Event) => e.stopPropagation(),
        });
      },
    },
    {
      title: t('biz.columnNoteContent'),
      dataIndex: 'content',
      slotName: 'content',
      width: 130,
      align: 'center',
    },
    {
      title: t('biz.publishLocation'),
      dataIndex: 'location',
      slotName: 'location',
      width: 120,
      align: 'center',
    },
    {
      title: t('biz.imageCount'),
      dataIndex: 'count',
      slotName: 'count',
      width: 100,
      align: 'center',
    },
    {
      title: t('biz.noteType'),
      dataIndex: 'noteType',
      slotName: 'noteType',
      width: 100,
      align: 'center',
    },
    {
      title: t('biz.columnNoteSource'),
      dataIndex: 'fromType',
      slotName: 'fromType',
      width: 120,
      align: 'center',
    },
    {
      title: t('biz.columnAlbum'),
      dataIndex: 'albumId',
      slotName: 'albumId',
      width: 120,
      align: 'center',
    },
    {
      title: t('biz.auditStatus'),
      dataIndex: 'auditStatus',
      slotName: 'auditStatus',
      width: 100,
      align: 'center',
    },
    {
      title: t('biz.pinned'),
      dataIndex: 'pinned',
      slotName: 'pinned',
      width: 100,
      align: 'center',
    },
    {
      title: t('biz.viewCountCol'),
      dataIndex: 'viewCount',
      slotName: 'viewCount',
      width: 100,
      align: 'center',
    },
    {
      title: t('biz.likeCount'),
      dataIndex: 'likeCount',
      slotName: 'likeCount',
      width: 100,
      align: 'center',
    },
    {
      title: t('biz.collectionCountCol'),
      dataIndex: 'collectionCount',
      slotName: 'collectionCount',
      width: 100,
      align: 'center',
    },
    {
      title: t('biz.commentCountCol'),
      dataIndex: 'commentCount',
      slotName: 'commentCount',
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
      title: t('biz.updateTime'),
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
      title: t('biz.operation'),
      dataIndex: 'optional',
      slotName: 'optional',
      fixed: 'right',
      width: 200,
      align: 'center',
    },
  ];
  }),
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
      console.warn('停止笔记详情视频失败', error);
    }
  }
  state.currentImageSrc = '';
};

const resetDetailState = () => {
  state.currentNote = null;
  state.imageList = [];
  state.currentImageIndex = 0;
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

// 初始化字典（逐一获取，防止其中一个返回异常导致整体崩溃）
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

  const [status, auditStatus, noteType] = await Promise.all([
    safeGetDict('sys_normal_disable'),
    safeGetDict('audit_status'),
    safeGetDict('note_type'),
  ]);

  // 定义笔记来源字典（0代表web端，1代表app端，2代表管理端）
  const fromType = [
    { dictValue: '0', dictLabel: t('biz.fromWeb'), listClass: 'primary' },
    { dictValue: '1', dictLabel: t('biz.fromApp'), listClass: 'success' },
    { dictValue: '2', dictLabel: t('biz.fromAdmin'), listClass: 'warning' },
  ];

  // 定义是否置顶字典（0代表否，1代表是）
  const pinned = [
    { dictValue: '0', dictLabel: t('biz.no'), listClass: 'info' },
    { dictValue: '1', dictLabel: t('biz.yes'), listClass: 'success' },
  ];

  state.dictOptions = {
    status,
    auditStatus,
    noteType,
    pinned,
    fromType,
  } as any;

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
        title: t('biz.mainCategory'),
        children: treeData,
      },
    ];
  } catch (e) {
    console.warn('[category] load failed', e);
  }

  // 加载专辑数据
  try {
    const albumRes = await getAlbumList({});
    const albums = albumRes.data?.rows || albumRes.data || [];
    // 构建专辑映射表
    const albumsMap: Record<string, string> = {};
    albums.forEach((album: any) => {
      if (album.id && album.title) {
        albumsMap[String(album.id)] = album.title;
      }
    });
    state.albumsMap = albumsMap;
  } catch (e) {
    console.warn('[album] load failed', e);
  }
};
initData();

/** ES清空 */
const clearAllRecord = async () => {
  Modal.confirm({
    title: t('biz.confirmEsClearTitle'),
    content: t('biz.confirmEsClearBody'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        await clearAllRecordDate();
        Message.success(t('biz.clearEsRecordsOk'));
        // eslint-disable-next-line no-use-before-define
        fetchListData();
      } catch (error) {
        Message.error(t('biz.clearEsRecordsFail'));
      }
    },
    onCancel: () => {
      Message.info(t('biz.clearEsRecordsCancelledMsg'));
    },
  });
};

/** ES重置 */
const refreshNote = () => {
  Modal.confirm({
    title: t('biz.confirmEsResetContent'),
    content: t('biz.esResetModalBody'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        await refreshNoteDate();
        Message.success(t('biz.resetSuccess'));
        // eslint-disable-next-line no-use-before-define
        fetchListData();
      } catch (error) {
        Message.error(t('biz.resetFailed'));
      }
    },
    onCancel: () => {
      Message.info(t('biz.resetEsCancelledMsg'));
    },
  });
};

// 重置查询表单
const resetQueryForm = () => {
  state.queryParams = {
    hsId: '',
    username: undefined,
    phone: undefined,
    auditStatus: undefined,
    cpid: undefined,
    title: undefined,
    author: undefined,
    uid: undefined,
    noteType: undefined,
    fromType: undefined,
    orderByColumn: undefined,
    isAsc: undefined,
  };
  state.selectName = '';
  state.dateRange = [];
  // eslint-disable-next-line no-use-before-define
  fetchListData();
};

// 获取列表数据
const fetchListData = async () => {
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
  const { rows, total } = await listNote(requestParams);
  state.list = rows;
  pagination.total = total;
  setLoading(false);
};

// 组件挂载时检查路由参数
onMounted(() => {
  if (route.query.uid) {
    state.queryParams.uid = route.query.uid as string;
  }
  if (route.query.pid) {
    state.queryParams.cpid = Number(route.query.pid);
  } else if (route.query.cpid) {
    state.queryParams.cpid = Number(route.query.cpid);
  }
  if (route.query.today === '1') {
    state.dateRange = getTodayRange() as unknown as never[];
  }
  fetchListData();
});

// 分页
const onPageChange = (current: number) => {
  pagination.current = current;
  fetchListData();
};

// 格式化地址显示
const formatAddress = (record: any): string => {
  if (!record) return '';
  const parts = [];
  if (record.province) parts.push(record.province);
  if (record.city && record.city !== record.province) parts.push(record.city);
  if (record.district) parts.push(record.district);
  if (record.address) parts.push(record.address);
  // 兼容旧数据：如果没有新字段但有location字段
  if (parts.length === 0 && record.location) {
    return record.location;
  }
  return parts.length > 0 ? parts.join(' ') : '';
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

// 修改
const handleUpdate = (id: number) => {
  state.currentId = id;
  state.editVisible = true;
};

// 删除
const handleDelete = (record: any) => {
  const ids = record && record.id ? record.id : state.selectedKeys.join(',');
  Modal.confirm({
    titleAlign: 'start',
    title: t('biz.deleteConfirmTitle'),
    content: t('biz.deleteNoteIdsConfirm', { ids }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: () => {
      delNote(ids).then(() => {
        Message.success(t('biz.deleteSuccess'));
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

// 获取分类标题
const getCategoryTitle = (cpid: string) => {
  return state.categoriesMap[cpid] || cpid;
};

// 获取专辑标题
const getAlbumTitle = (albumId: string) => {
  return state.albumsMap[String(albumId)] || '';
};

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

// 初始化图片列表
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

    // 优先尝试解析 JSON 字符串（数据库中的 urls 字段通常是 JSON 字符串）
    if (
      (trimmed.startsWith('[') && trimmed.endsWith(']')) ||
      (trimmed.startsWith('{') && trimmed.endsWith('}'))
    ) {
      try {
        const parsed = JSON.parse(trimmed);
        // 如果解析成功，递归处理解析后的结果
        const result = extractUrlArray(parsed);
        if (result.length > 0) {
          return result;
        }
      } catch (error) {
        // JSON 解析失败，继续使用分隔符处理
      }
    }

    // 如果不是 JSON 格式，尝试按分隔符分割
    const segments = trimmed.split(/[,，\s]/).filter(Boolean);
    if (segments.length > 1) {
      return segments
        .map((item) => normalizeUrlString(item))
        .filter((item) => !!item);
    }

    // 单个 URL
    const normalized = normalizeUrlString(trimmed);
    return normalized ? [normalized] : [];
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

  if (typeof value === 'object' && value !== null) {
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

const initImageList = (note: any) => {
  // 优先从 urls 字段提取（这是数据库存储的主要字段）
  // urls 可能是 JSON 字符串格式，如 ["url1", "url2"] 或 "url1,url2"
  const orderedSources = [
    note.urls, // 优先使用 urls 字段（数据库主字段）
    note.imageList,
    note.images,
    note.imageUrls,
    note.pictures,
    note.photoList,
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

  // 如果以上都没有，尝试从封面提取
  const fallbackImages = extractImageUrls([
    note.noteCover,
    note.cover,
    note.thumbnail,
  ]);

  if (fallbackImages.length > 0) {
    return [fallbackImages[0]];
  }

  return [];
};

const extractFirstUrl = (value: any): string => {
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
        // ignore parse error, fallback to separator handling
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
};

// 上一张图片
const prevImage = () => {
  if (state.currentImageIndex > 0) {
    state.currentImageIndex -= 1;
    state.currentImageSrc = getNginxProxyImageUrl(state.imageList[state.currentImageIndex]);
  }
};

// 下一张图片
const nextImage = () => {
  if (state.currentImageIndex < state.imageList.length - 1) {
    state.currentImageIndex += 1;
    state.currentImageSrc = getNginxProxyImageUrl(state.imageList[state.currentImageIndex]);
  }
};

const resolveVideoUrl = (note: any, fallback?: any) => {
  if (!note && !fallback) return '';
  const source = note || {};
  const fallbackSource = fallback || {};
  const type = Number(
    source.noteType !== undefined ? source.noteType : fallbackSource.noteType
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
    extractFirstUrl(fallbackSource.videoUrl),
    extractFirstUrl(fallbackSource.video),
    extractFirstUrl(fallbackSource.videoPath),
    extractFirstUrl(fallbackSource.videoUrls),
    extractFirstUrl(fallbackSource.videoList),
    extractFirstUrl(fallbackSource.videoSrc),
    extractFirstUrl(fallbackSource.urls),
    extractFirstUrl(fallbackSource.videos),
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

const isVideoNote = computed(() => {
  if (!state.currentNote) return false;
  const videoSrc = resolveVideoUrl(state.currentNote);
  return !!videoSrc;
});

const loadNoteDetail = async (record: any) => {
  stopDetailPlayback();
  const noteId = record?.id;
  try {
    const { data } = noteId ? await getNote(noteId) : { data: record };
    const fullNote = { ...record, ...data };
    const videoSrc = resolveVideoUrl(fullNote, record);
    const hasVideo = !!videoSrc;

    if (videoSrc) {
      fullNote.videoUrl = videoSrc;
    }

    state.currentNote = fullNote;
    state.imageList = hasVideo ? [] : initImageList(fullNote);
    state.currentImageIndex = 0;
    
    // 使用代理函数处理图片和视频URL
    if (hasVideo) {
      state.currentImageSrc = getNginxProxyVideoUrl(videoSrc || fullNote?.noteCover || '');
    } else {
      // 优先使用图片列表的第一张，如果没有则使用封面
      const rawImageSrc = state.imageList[0] || fullNote?.noteCover || '';
      state.currentImageSrc = getNginxProxyImageUrl(rawImageSrc);
      
      // 如果图片列表为空但封面存在，将封面添加到图片列表
      if (state.imageList.length === 0 && fullNote?.noteCover) {
        state.imageList = [fullNote.noteCover];
        // 更新当前图片源
        state.currentImageSrc = getNginxProxyImageUrl(fullNote.noteCover);
      }
    }
    
    state.detailVisible = true;
  } catch (error) {
    console.error('获取笔记详情失败:', error);
    Message.error(t('biz.loadNoteDetailFailed'));
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

    .detail-image {
      max-width: 100%;
      max-height: 100%;
      width: auto;
      height: auto;
      object-fit: contain;
      display: block;
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
  }
}

.note-detail-modal {
  .detail-content {
    max-height: 70vh;
    overflow-y: auto;
  }

  .note-info {
    margin-bottom: 20px;

    .info-item {
      margin-bottom: 10px;

      .label {
        font-weight: bold;
        color: #333;
        margin-right: 10px;
      }

      .value {
        color: #666;
      }
    }
  }

  .image-preview {
    position: relative;
    text-align: center;
    margin: 20px 0;

    .image-container {
      position: relative;
      display: inline-block;
      max-width: 100%;

      .preview-image {
        max-width: 100%;
        max-height: 500px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .nav-button {
        position: absolute;
        top: 50%;
        transform: translateY(-50%);
        background: rgba(0, 0, 0, 0.5);
        color: white;
        border: none;
        border-radius: 50%;
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background: rgba(0, 0, 0, 0.7);
        }

        &.prev {
          left: -20px;
        }

        &.next {
          right: -20px;
        }
      }
    }

    .image-counter {
      margin-top: 10px;
      color: #666;
      font-size: 14px;
    }
  }

  .video-preview {
    text-align: center;
    margin: 20px 0;

    video {
      max-width: 100%;
      max-height: 400px;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
  }

  .no-media {
    text-align: center;
    color: #999;
    font-style: italic;
    margin: 40px 0;
  }
}

.text-ellipsis {
  max-width: 200px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
