<template>
  <div class="container">
    <!-- 免责声明 -->
    <a-alert type="warning" style="margin-bottom: 20px" :closable="true">
      <template #title>使用须知</template>
      <div>
        <p>1. 本功能仅用于学习研究，请勿用于商业用途</p>
        <p>2. 爬取的内容可能涉及版权问题，请谨慎使用</p>
        <p>3. 请遵守相关法律法规和平台规则</p>
        <p>4. 使用本功能产生的任何法律后果由使用者自行承担</p>
      </div>
    </a-alert>

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
            label-align="left"
            auto-label-width
            :label-col-style="{ paddingRight: '4px' }"
          >
            <a-row :gutter="[32, 16]" align="end">
              <a-col :span="7">
                <a-form-item field="keyword" label="关键词">
                  <a-input
                    v-model="state.queryParams.keyword"
                    placeholder="输入搜索关键词"
                    allow-clear
                    @keyup.enter="handleSearch"
                  >
                    <template #prefix>
                      <icon-search />
                    </template>
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="4">
                <a-form-item field="type" label="类型">
                  <a-select
                    v-model="state.queryParams.type"
                    placeholder="选择类型"
                  >
                    <a-option value="note">笔记</a-option>
                    <a-option value="image">图片</a-option>
                    <a-option value="video">视频</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="4">
                <a-form-item field="sortType" label="排序方式">
                  <a-select
                    v-model="state.queryParams.sortType"
                    placeholder="选择排序方式"
                  >
                    <a-option :value="0">综合排序</a-option>
                    <a-option :value="1">最新</a-option>
                    <a-option :value="2">最多点赞</a-option>
                    <a-option :value="3">最多评论</a-option>
                    <a-option :value="4">最多收藏</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="4">
                <a-form-item field="noteTime" label="笔记时间">
                  <a-select
                    v-model="state.queryParams.noteTime"
                    placeholder="选择笔记时间"
                  >
                    <a-option :value="0">不限</a-option>
                    <a-option :value="1">一天内</a-option>
                    <a-option :value="2">一周内</a-option>
                    <a-option :value="3">半年内</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="5">
                <a-form-item>
                  <a-button
                    type="primary"
                    :loading="state.searching"
                    style="width: 100%; height: 32px"
                    size="medium"
                    @click="() => handleSearch(false)"
                  >
                    <template #icon>
                      <icon-search />
                    </template>
                    开始爬取
                  </a-button>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16" style="margin-top: 16px">
              <a-col :span="24">
                <a-form-item field="cookie" label="Cookie（可选）">
                  <a-textarea
                    v-model="state.queryParams.cookie"
                    placeholder="输入Cookie值（格式：key1=value1; key2=value2; ...）&#10;提示：登录小红书后，在浏览器开发者工具中复制Cookie值，可提高爬取成功率&#10;Cookie必须包含a1字段，用于生成签名"
                    :allow-clear="true"
                    :auto-size="{ minRows: 3, maxRows: 6 }"
                    :max-length="5000"
                    show-word-limit
                  />
                  <template #extra>
                    <span style="color: #999; font-size: 12px">
                      💡 提示：Cookie必须包含
                      <code
                        style="
                          background: #f5f5f5;
                          padding: 2px 4px;
                          border-radius: 2px;
                        "
                        >a1</code
                      >
                      字段才能正常生成签名
                    </span>
                  </template>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
      </a-row>

      <!-- 加载状态 -->
      <div
        v-if="state.searching"
        class="loading-container"
        style="margin-top: 40px"
      >
        <a-spin
          :loading="true"
          tip="正在爬取数据，请稍候..."
          style="width: 100%"
        >
          <div class="loading-content">
            <div
              style="
                min-height: 300px;
                width: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
              "
            ></div>
          </div>
        </a-spin>
        <p
          style="
            margin-top: 16px;
            color: #86909c;
            font-size: 14px;
            text-align: center;
          "
        >
          视频数据爬取可能需要较长时间，请耐心等待...
        </p>
      </div>

      <!-- 结果列表 -->
      <div v-else-if="state.items.length > 0" style="margin-top: 20px">
        <div class="batch-toolbar">
          <a-space wrap>
            <span class="batch-toolbar-text"
              >已选 <strong>{{ selectedIds.length }}</strong> 条</span
            >
            <a-button size="small" @click="selectAllOnCurrentPage">
              全选本页
            </a-button>
            <a-button size="small" @click="clearSelectionOnCurrentPage">
              取消本页
            </a-button>
            <a-button size="small" @click="clearAllSelection">清空选择</a-button>
            <a-button
              type="primary"
              size="small"
              :disabled="selectedIds.length === 0"
              @click="openBatchPublishModal"
            >
              <template #icon>
                <icon-upload />
              </template>
              批量发布
            </a-button>
          </a-space>
        </div>
        <div class="notes-grid">
          <div
            v-for="(item, index) in displayItems"
            :key="item.id || index"
            class="note-item-wrapper"
          >
            <a-card
              :body-style="{ padding: '0' }"
              :bordered="false"
              class="note-card"
              hoverable
            >
              <!-- 封面/图片 -->
              <div
                v-if="item.cover || (item.images && item.images.length > 0)"
                class="note-cover"
              >
                <a-image
                  :src="getNginxProxyImageUrl(item.cover || (item.images && item.images.length > 0 ? item.images[0] : ''))"
                  :preview="true"
                  fit="cover"
                  class="cover-image"
                />
                <!-- 视频标识 -->
                <div
                  v-if="item.video || item.type === 'video'"
                  class="video-badge"
                >
                  <icon-play-circle-fill style="font-size: 28px; color: #fff" />
                </div>
                <!-- 图片数量标识 -->
                <div
                  v-else-if="
                    (item.type === 'image' || item.type === 'note') &&
                    item.images &&
                    item.images.length > 1
                  "
                  class="image-count-badge"
                >
                  <icon-image
                    style="font-size: 16px; color: #fff; margin-right: 4px"
                  />
                  <span>{{ item.images.length }}</span>
                </div>
              </div>

              <!-- 内容区域 -->
              <div class="note-content">
                <!-- 标题 -->
                <div class="note-title" :title="item.title">
                  {{ item.title || '无标题' }}
                </div>

                <!-- 作者信息 -->
                <div v-if="item.author" class="note-author">
                  <icon-user style="color: #86909c" />
                  <span>{{ item.author }}</span>
                </div>

                <!-- 统计数据 -->
                <div
                  v-if="
                    item.likeCount || item.collectionCount || item.commentCount
                  "
                  class="note-stats"
                >
                  <a-space size="small" split>
                    <span v-if="item.likeCount" class="stat-item stat-like">
                      <icon-heart style="color: #f53f3f" />
                      <span>{{ formatCount(item.likeCount) }}</span>
                    </span>
                    <span
                      v-if="item.collectionCount"
                      class="stat-item stat-collect"
                    >
                      <icon-star style="color: #ff7d00" />
                      <span>{{ formatCount(item.collectionCount) }}</span>
                    </span>
                    <span
                      v-if="item.commentCount"
                      class="stat-item stat-comment"
                    >
                      <icon-message style="color: #165dff" />
                      <span>{{ formatCount(item.commentCount) }}</span>
                    </span>
                  </a-space>
                </div>

                <!-- 操作按钮 -->
                <div class="note-actions">
                  <a-checkbox
                    v-if="item.id"
                    class="note-action-checkbox"
                    :model-value="isItemSelected(item)"
                    @click.stop
                    @update:model-value="(v) => onCardCheckChange(item, !!v)"
                  />
                  <a-button
                    type="primary"
                    size="small"
                    style="flex: 1; margin-right: 8px"
                    @click="handleViewDetail(item)"
                  >
                    <template #icon>
                      <icon-eye />
                    </template>
                    查看
                  </a-button>
                  <a-button
                    type="primary"
                    size="small"
                    style="flex: 1"
                    @click="handlePublish(item)"
                  >
                    <template #icon>
                      <icon-upload />
                    </template>
                    发布
                  </a-button>
                </div>
              </div>
            </a-card>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-container" style="margin-top: 20px">
          <a-pagination
            v-model:current="state.currentPage"
            v-model:page-size="state.pageSize"
            :total="state.total"
            :show-total="true"
            :show-page-size="true"
            @change="handlePageChange"
            @page-size-change="handleSpiderPageSizeChange"
          >
            <!-- Arco 的 showTotal 仅为 boolean，自定义文案必须用 #total 插槽；否则会用 props.total 渲染「共 N 条」而 total 含 +1 占位 -->
            <template #total>
              {{ spiderPaginationTotalText }}
            </template>
          </a-pagination>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty
        v-else
        description="暂无数据，请输入关键词开始爬取"
        style="margin-top: 40px"
      />
    </a-card>

    <!-- 详情对话框 -->
    <a-modal
      v-model:visible="state.detailVisible"
      title="笔记详情"
      :footer="false"
      width="1200px"
      class="note-detail-modal"
      :body-style="{
        height: '700px',
        maxHeight: '700px',
        overflowY: 'auto',
        overflowX: 'hidden',
        padding: '16px 20px',
      }"
    >
      <div v-if="state.currentItem" class="detail-content">
        <!-- 基本信息卡片 - 紧凑布局 -->
        <a-card :bordered="false" class="info-card">
          <div class="detail-header-compact">
            <div class="detail-title-compact">
              <h3>{{ state.currentItem.title || '无标题' }}</h3>
            </div>
            <div class="detail-meta-compact">
              <div class="detail-author-compact">
                <icon-user style="color: #86909c; font-size: 14px" />
                <span>{{ state.currentItem.author || '未知作者' }}</span>
              </div>
              <!-- 统计数据 - 紧凑展示 -->
              <div class="detail-stats-compact">
                <span class="stat-item-compact">
                  <icon-heart style="color: #f53f3f; font-size: 14px" />
                  <span>{{
                    formatCount(state.currentItem.likeCount || 0)
                  }}</span>
                </span>
                <span class="stat-item-compact">
                  <icon-star style="color: #ff7d00; font-size: 14px" />
                  <span>{{
                    formatCount(state.currentItem.collectionCount || 0)
                  }}</span>
                </span>
                <span class="stat-item-compact">
                  <icon-message style="color: #165dff; font-size: 14px" />
                  <span>{{
                    formatCount(state.currentItem.commentCount || 0)
                  }}</span>
                </span>
              </div>
            </div>
          </div>
        </a-card>

        <!-- 内容卡片 - 紧凑 -->
        <a-card
          v-if="state.currentItem.content"
          :bordered="false"
          class="content-card-compact"
          title="内容"
        >
          <div class="note-content-text-compact">
            {{ state.currentItem.content }}
          </div>
        </a-card>

        <!-- 视频展示 - 优先显示 -->
        <a-card
          v-if="hasVideo"
          :bordered="false"
          class="video-card-compact"
          title="视频"
        >
          <div class="video-wrapper-compact">
            <!-- 视频下载中提示 -->
            <div
              v-if="state.videoDownloading"
              class="video-downloading-container"
            >
              <div class="video-downloading-content">
                <div class="loading-spinner-wrapper">
                  <a-spin :loading="true" />
                </div>
                <p class="loading-text-primary"> 正在下载视频，请稍候... </p>
                <p class="loading-text-secondary">
                  视频下载可能需要较长时间，请耐心等待...
                </p>
              </div>
            </div>
            <!-- 视频加载中提示 -->
            <div v-else-if="state.videoLoading" class="video-loading-tip">
              <a-spin tip="正在加载视频，请稍候...">
                <div style="min-height: 200px"></div>
              </a-spin>
            </div>
            <!-- 视频播放器 - 优先使用OSS URL，如果没有OSS URL则使用原视频URL（可能是已下载的OSS URL） -->
            <!-- 如果视频加载失败，不显示播放器，而是显示下载按钮 -->
            <video
              v-else-if="
                state.currentItem &&
                state.currentItem.video &&
                state.currentItem.video.trim() &&
                !state.videoLoadError
              "
              :src="state.videoOssUrl || getNginxProxyVideoUrl(state.currentItem.video)"
              controls
              preload="auto"
              class="detail-video-compact"
              playsinline
              @error="handleVideoError"
              @loadstart="handleVideoLoadStart"
              @loadedmetadata="handleVideoLoadedMetadata"
              @canplay="handleVideoCanPlay"
            >
              您的浏览器不支持视频播放
            </video>
            <!-- 未加载视频时的占位符和加载按钮 -->
            <!-- 如果视频加载失败，也显示这个占位符 -->
            <div v-else class="video-placeholder">
              <div style="text-align: center; padding: 40px 20px">
                <p
                  v-if="state.videoLoadError"
                  style="color: #f53f3f; margin-bottom: 20px"
                >
                  ⚠️ 小红书视频限制播放
                </p>
                <p
                  v-else-if="
                    state.currentItem.video && isOssUrl(state.currentItem.video)
                  "
                  style="color: #86909c; margin-bottom: 20px"
                >
                  视频已下载，正在加载...
                </p>
                <p v-else style="color: #86909c; margin-bottom: 20px"
                  >视频未加载</p
                >

                <a-button
                  v-if="
                    !state.currentItem.video ||
                    !isOssUrl(state.currentItem.video)
                  "
                  type="primary"
                  size="large"
                  :loading="state.videoDownloading"
                  @click="handleDownloadVideo"
                >
                  <template #icon><icon-download /></template>
                  下载视频
                </a-button>

                <p
                  v-if="
                    !state.currentItem.video ||
                    !isOssUrl(state.currentItem.video)
                  "
                  style="font-size: 12px; color: #86909c; margin-top: 16px"
                >
                  点击按钮将视频下载到OSS，然后即可播放
                </p>
                <p style="font-size: 12px; color: #86909c; margin-top: 8px">
                  笔记类型: {{ state.currentItem.type }}, 笔记ID:
                  {{ state.currentItem.id }}
                </p>
              </div>
            </div>
          </div>
        </a-card>

        <!-- 视频封面图（如果有视频但没有图片列表） -->
        <a-card
          v-if="
            state.currentItem.video &&
            state.currentItem.video.trim() &&
            state.currentItem.cover &&
            (!state.currentItem.images || state.currentItem.images.length === 0)
          "
          :bordered="false"
          class="video-cover-card"
          title="封面"
        >
          <div class="video-cover-wrapper">
            <a-image
              :src="getNginxProxyImageUrl(state.currentItem.cover)"
              :preview="true"
              fit="cover"
              class="video-cover-image"
            />
          </div>
        </a-card>

        <!-- 图片展示（视频类型时标题显示"视频封面"，非视频类型时显示"图片列表"） -->
        <a-card
          v-if="state.currentItem.images && state.currentItem.images.length > 0"
          :bordered="false"
          class="images-card-compact"
          :title="
            state.currentItem.type === 'video'
              ? '视频封面'
              : `图片列表 (${state.currentItem.images.length}张)`
          "
        >
          <a-row :gutter="[6, 6]">
            <a-col
              v-for="(img, idx) in state.currentItem.images"
              :key="idx"
              :xs="12"
              :sm="8"
              :md="4"
              :lg="4"
              :xl="4"
              :xxl="4"
            >
              <div class="image-wrapper-compact">
                <a-image
                  :src="getNginxProxyImageUrl(img)"
                  :preview="true"
                  fit="cover"
                  class="detail-image-compact"
                />
              </div>
            </a-col>
          </a-row>
        </a-card>

        <!-- 其他信息 - 移到图片列表下边 -->
        <a-card :bordered="false" class="meta-card-compact" title="其他信息">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="笔记ID">
              <span class="meta-value-compact">{{ state.currentItem.id }}</span>
            </a-descriptions-item>
            <a-descriptions-item
              v-if="state.currentItem.publishTime"
              label="发布时间"
            >
              <span class="meta-value-compact">{{
                state.currentItem.publishTime
              }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="链接" :span="2">
              <a
                :href="state.currentItem.url"
                target="_blank"
                class="link-text-compact"
              >
                {{ state.currentItem.url }}
                <icon-link style="margin-left: 4px; font-size: 12px" />
              </a>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 操作按钮 -->
        <a-card :bordered="false" class="action-card-compact">
          <div style="text-align: right; padding: 10px 0">
            <a-button type="primary" size="large" @click="handlePublishFromDetail">
              <template #icon>
                <icon-upload />
              </template>
              发布笔记
            </a-button>
          </div>
        </a-card>
      </div>
    </a-modal>

    <!-- 笔记发布弹窗 -->
    <edit-note
      v-model:id="state.publishNoteId"
      v-model:visible="state.publishVisible"
      :initial-data="state.publishInitialData"
      @refresh-data-list="() => {}"
    >
    </edit-note>

    <!-- 批量发布：统一用户与分类，直接入库（审核通过 + 管理端来源） -->
    <a-modal
      v-model:visible="batchState.visible"
      title="批量发布笔记"
      width="520px"
      ok-text="确定"
      cancel-text="取消"
      :mask-closable="false"
      :ok-loading="batchState.submitting"
      :ok-button-props="{ disabled: batchState.submitting }"
      :cancel-button-props="{ disabled: batchState.submitting }"
      unmount-on-close
      @cancel="resetBatchModal"
      @before-ok="runBatchPublish"
    >
      <a-alert type="info" style="margin-bottom: 16px">
        将为已选 {{ selectedIds.length }} 条爬取结果依次下载资源并新增笔记（审核状态：已通过）。请指定发布用户与分类。
      </a-alert>
      <div
        v-if="batchState.submitting && batchState.progressTotal > 0"
        class="batch-publish-progress"
      >
        <div class="batch-publish-progress-text">
          正在发布 {{ batchState.progressCurrent }} /
          {{ batchState.progressTotal }}
        </div>
        <a-progress
          :percent="batchPublishProgressPercent"
          :animation="true"
        />
      </div>
      <a-form
        ref="batchFormRef"
        :model="batchState.form"
        :rules="batchRules"
        layout="vertical"
      >
        <a-form-item label="用户" field="userId" required>
          <a-select
            v-model="batchState.form.userId"
            :loading="batchState.userSearching"
            placeholder="请输入用户名或 ID 搜索"
            allow-search
            allow-clear
            :filter-option="false"
            @search="handleBatchUserSearch"
            @change="handleBatchUserChange"
          >
            <template #empty>
              <div
                v-if="batchState.userSearching"
                style="text-align: center; padding: 8px"
              >
                搜索中...
              </div>
              <div
                v-else
                style="text-align: center; padding: 8px; color: #86909c"
              >
                请输入关键词搜索用户
              </div>
            </template>
            <a-option
              v-for="option in batchState.userOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            >
              <div style="display: flex; align-items: center; gap: 8px">
                <a-avatar
                  v-if="option.avatar"
                  :size="24"
                  :image-url="option.avatar"
                />
                <span>{{ option.username }}</span>
                <span style="color: #86909c; font-size: 12px"
                  >(ID: {{ option.id }})</span
                >
              </div>
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="分类" field="cpid" required>
          <a-tree-select
            v-model="batchState.form.cpid"
            :field-names="{
              key: 'id',
              title: 'title',
              children: 'children',
            }"
            :data="batchState.navbarOptions"
            :show-count="false"
            placeholder="请选择分类"
            allow-clear
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { reactive, computed, onBeforeUnmount, onMounted, watch, ref, nextTick } from 'vue';
import { Message } from '@arco-design/web-vue';
import type { FormInstance } from '@arco-design/web-vue';
import {
  xiaohongshuSpider,
  downloadXiaohongshuVideo,
  type XiaohongshuItem,
} from '@/api/web/xiaohongshu';
import { listNavbar } from '@/api/web/navbar';
import { getUserByKeyword, unwrapUserKeywordPage } from '@/api/web/user';
import { submitSpiderItemAsNote } from '@/utils/xhsSpiderBatchNote';
import EditNote from '@/views/web/note/components/edit-note.vue';
import cache from '@/plugins/cache';

const state = reactive({
  queryParams: {
    keyword: '',
    type: 'note' as 'image' | 'video' | 'note',
    sortType: 0, // 排序方式：0-综合排序, 1-最新, 2-最多点赞, 3-最多评论, 4-最多收藏
    noteTime: 0, // 笔记时间：0-不限, 1-一天内, 2-一周内, 3-半年内
    cookie: '', // Cookie值（可选）
  },
  items: [] as XiaohongshuItem[],
  currentPage: 1,
  pageSize: 10,
  total: 0,
  /** 接口本页满页时认为可能还有下一页，用于分页器 total 仅 +1，避免「共 N 条」虚高 */
  hasMore: false,
  searching: false,
  detailVisible: false,
  currentItem: null as XiaohongshuItem | null,
  videoLoadError: false, // 视频加载错误标志
  videoBlobUrl: '', // Blob URL，用于播放视频
  videoLoading: false, // 视频加载中标志
  videoOssUrl: '', // OSS URL，下载后的视频链接
  videoDownloading: false, // 视频下载中标志
  publishVisible: false, // 发布弹窗显示状态
  publishNoteId: undefined as number | undefined, // 发布笔记ID（新增时为undefined）
  publishInitialData: undefined as any, // 发布初始数据
  loadedPages: new Set<number>(), // 已加载的页面集合，用于去重
  lastSearchParams: '', // 上次搜索的参数（用于判断搜索条件是否变化）
});

// 当前页展示的数据（分页切片，需早于「全选本页」等逻辑定义）
const displayItems = computed(() => {
  const start = (state.currentPage - 1) * state.pageSize;
  const end = start + state.pageSize;
  return state.items.slice(start, end);
});

/** 底部文案：只按已加载 items 计数；与 :total 的 +1 占位解耦，避免「共 N 条」多算 1 */
const spiderPaginationTotalText = computed(() => {
  const loaded = state.items.length;
  if (loaded === 0) return '暂无数据';
  const startIdx = (state.currentPage - 1) * state.pageSize;
  const endIdx = Math.min(startIdx + state.pageSize, loaded);
  const from = startIdx + 1;
  const to = endIdx;
  if (from > to) {
    return `已加载 ${loaded} 条（请稍候或返回上一页）`;
  }
  if (state.hasMore) {
    return `第 ${from}-${to} 条 · 已加载 ${loaded} 条（可继续翻页加载）`;
  }
  return `第 ${from}-${to} 条，共 ${loaded} 条`;
});

const selectedIds = ref<string[]>([]);

const batchFormRef = ref<FormInstance>();

const batchState = reactive({
  visible: false,
  submitting: false,
  /** 批量发布进度：已完成条数 / 总条数 */
  progressCurrent: 0,
  progressTotal: 0,
  userSearching: false,
  userOptions: [] as Array<{
    value: string;
    label: string;
    username: string;
    id: string;
    avatar?: string;
  }>,
  navbarOptions: [] as any[],
  form: {
    userId: undefined as string | undefined,
    author: undefined as string | undefined,
    cpid: undefined as number | string | undefined,
  },
});

const batchRules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  cpid: [{ required: true, message: '请选择分类', trigger: 'change' }],
};

/** Arco Progress 的 percent 为 0～1，组件内部再 ×100 渲染文案与宽度；勿传 0～100 否则会显示成 6000% 这类值 */
const batchPublishProgressPercent = computed(() => {
  const t = batchState.progressTotal;
  if (!t) return 0;
  return Math.min(1, batchState.progressCurrent / t);
});

function listNavbarToTree(list: Array<any>) {
  const map: Record<string, number> = {};
  const treeData: any[] = [];
  const nodes = list.map((item) => ({
    id: item.id,
    pid: item.pid,
    title: item.title,
    children: [] as any[],
  }));
  for (let i = 0; i < nodes.length; i += 1) {
    map[String(nodes[i].id)] = i;
  }
  for (let i = 0; i < nodes.length; i += 1) {
    const node = nodes[i];
    if (node.pid != null && node.pid !== '' && nodes[map[String(node.pid)]]) {
      nodes[map[String(node.pid)]].children.push(node);
    } else {
      treeData.push(node);
    }
  }
  return treeData;
}

const loadBatchNavbarTree = async () => {
  if (batchState.navbarOptions.length > 0) return;
  try {
    const res: any = await listNavbar({});
    const body = res?.data !== undefined ? res.data : res;
    const rows = Array.isArray(body) ? body : body?.data ?? body?.rows ?? [];
    batchState.navbarOptions = listNavbarToTree(Array.isArray(rows) ? rows : []);
  } catch (e) {
    console.warn('加载分类失败', e);
    Message.error('分类数据加载失败');
  }
};

const isItemSelected = (item: XiaohongshuItem) =>
  !!item.id && selectedIds.value.includes(item.id);

const onCardCheckChange = (item: XiaohongshuItem, checked: boolean) => {
  if (!item.id) return;
  if (checked) {
    if (!selectedIds.value.includes(item.id)) {
      selectedIds.value = [...selectedIds.value, item.id];
    }
  } else {
    selectedIds.value = selectedIds.value.filter((id) => id !== item.id);
  }
};

const selectAllOnCurrentPage = () => {
  const next = new Set(selectedIds.value);
  displayItems.value.forEach((it) => {
    if (it.id) next.add(it.id);
  });
  selectedIds.value = Array.from(next);
};

const clearSelectionOnCurrentPage = () => {
  const pageIds = new Set(
    displayItems.value.map((it) => it.id).filter(Boolean) as string[]
  );
  selectedIds.value = selectedIds.value.filter((id) => !pageIds.has(id));
};

const clearAllSelection = () => {
  selectedIds.value = [];
};

const openBatchPublishModal = async () => {
  if (selectedIds.value.length === 0) {
    Message.warning('请先勾选要发布的笔记');
    return;
  }
  await loadBatchNavbarTree();
  batchState.visible = true;
};

const resetBatchModal = () => {
  batchState.visible = false;
  batchState.submitting = false;
  batchState.progressCurrent = 0;
  batchState.progressTotal = 0;
  batchFormRef.value?.resetFields();
  batchState.form.userId = undefined;
  batchState.form.author = undefined;
  batchState.form.cpid = undefined;
  batchState.userOptions = [];
};

let batchUserSearchTimer: ReturnType<typeof setTimeout> | null = null;
const handleBatchUserSearch = (keyword: string) => {
  if (batchUserSearchTimer) clearTimeout(batchUserSearchTimer);
  if (!keyword || !keyword.trim()) {
    batchState.userOptions = [];
    return;
  }
  batchUserSearchTimer = setTimeout(async () => {
    batchState.userSearching = true;
    try {
      const res = await getUserByKeyword(1, 20, keyword.trim());
      const users = unwrapUserKeywordPage(res);
      batchState.userOptions = users.map((u) => ({
        value: String(u.id),
        label: `${u.username} (ID: ${u.id})`,
        username: u.username,
        id: String(u.id),
        avatar: u.avatar,
      }));
    } catch (e) {
      console.error(e);
      batchState.userOptions = [];
      Message.error('搜索用户失败');
    } finally {
      batchState.userSearching = false;
    }
  }, 300);
};

const handleBatchUserChange = (value: string) => {
  if (!value) {
    batchState.form.userId = undefined;
    batchState.form.author = undefined;
    return;
  }
  const opt = batchState.userOptions.find((o) => o.value === value);
  if (opt) {
    batchState.form.userId = opt.id;
    batchState.form.author = opt.username;
  }
};

/** 供批量弹窗 @before-ok 使用：返回 false 时不关闭弹窗（校验失败或全部失败） */
const runBatchPublish = async (): Promise<boolean> => {
  const vr = await batchFormRef.value?.validate();
  if (vr) return false;

  const cpidRaw = batchState.form.cpid;
  const cpid =
    typeof cpidRaw === 'string' && cpidRaw !== ''
      ? Number(cpidRaw)
      : Number(cpidRaw);
  if (cpidRaw === undefined || cpidRaw === '' || Number.isNaN(cpid)) {
    Message.error('请选择有效分类');
    return false;
  }

  const uid = batchState.form.userId;
  const author = batchState.form.author || '';
  if (!uid) {
    Message.error('请选择用户');
    return false;
  }

  const items = state.items.filter((i) => selectedIds.value.includes(i.id));
  if (items.length === 0) {
    Message.warning('所选条目已不存在，请重新选择');
    return false;
  }

  const cookie =
    state.queryParams.cookie && state.queryParams.cookie.trim()
      ? state.queryParams.cookie.trim()
      : undefined;

  batchState.submitting = true;
  batchState.progressTotal = items.length;
  batchState.progressCurrent = 0;
  let ok = 0;
  let fail = 0;
  const failedIds: string[] = [];

  try {
    await items.reduce<Promise<void>>((prev, item) => {
      return prev.then(async () => {
        try {
          await submitSpiderItemAsNote({
            item,
            uid,
            author,
            cpid,
            cookie,
          });
          ok += 1;
        } catch (err: unknown) {
          fail += 1;
          if (item.id) failedIds.push(item.id);
          console.error('批量发布单条失败', item.id, err);
        } finally {
          batchState.progressCurrent += 1;
        }
      });
    }, Promise.resolve());

    if (ok > 0) {
      Message.success(
        `批量发布完成：成功 ${ok} 条${fail > 0 ? `，失败 ${fail} 条` : ''}`
      );
    }
    if (fail > 0 && ok === 0) {
      Message.error(`全部失败，共 ${fail} 条，请检查网络或资源链接`);
    } else if (fail > 0) {
      Message.warning(`${fail} 条发布失败，已保留失败项在勾选列表中便于重试`);
    }

    selectedIds.value = failedIds;
    if (ok > 0) {
      resetBatchModal();
      return true;
    }
    return false;
  } finally {
    batchState.submitting = false;
  }
};

// Cookie持久化：从localStorage加载
const COOKIE_STORAGE_KEY = 'xiaohongshu_cookie';
onMounted(() => {
  const savedCookie = cache.local.get(COOKIE_STORAGE_KEY);
  if (savedCookie) {
    state.queryParams.cookie = savedCookie;
  }
});

// Cookie持久化：保存到localStorage
watch(
  () => state.queryParams.cookie,
  (newCookie) => {
    if (newCookie && newCookie.trim()) {
      cache.local.set(COOKIE_STORAGE_KEY, newCookie.trim());
    } else {
      cache.local.remove(COOKIE_STORAGE_KEY);
    }
  }
);

// 生成搜索参数的唯一标识（用于判断搜索条件是否变化）
const getSearchParamsKey = (): string => {
  return JSON.stringify({
    keyword: state.queryParams.keyword,
    type: state.queryParams.type,
    sortType: state.queryParams.sortType,
    noteTime: state.queryParams.noteTime,
    cookie: state.queryParams.cookie,
  });
};

// 去重函数：根据 item.id 去重
const deduplicateItems = (newItems: XiaohongshuItem[], existingItems: XiaohongshuItem[]): XiaohongshuItem[] => {
  const existingIds = new Set(existingItems.map(item => item.id));
  const uniqueNewItems = newItems.filter(item => !existingIds.has(item.id));
  return [...existingItems, ...uniqueNewItems];
};

// 搜索
const handleSearch = async (isPageChange = false) => {
  if (!state.queryParams.keyword.trim()) {
    Message.error('请输入搜索关键词');
    return;
  }

  // 检查搜索条件是否变化
  const currentSearchParams = getSearchParamsKey();
  const isSearchParamsChanged = currentSearchParams !== state.lastSearchParams;

  // 如果搜索条件变化，重置状态
  if (isSearchParamsChanged && !isPageChange) {
    state.items = [];
    state.loadedPages.clear();
    state.currentPage = 1;
    state.lastSearchParams = currentSearchParams;
    state.hasMore = false;
    state.total = 0;
    selectedIds.value = [];
  }

  // 如果当前页已加载，且是分页变化，直接返回
  if (isPageChange && state.loadedPages.has(state.currentPage)) {
    return;
  }

  state.searching = true;
  try {
    const params: any = {
      keyword: state.queryParams.keyword,
      type: state.queryParams.type,
      sortType: state.queryParams.sortType,
      noteTime: state.queryParams.noteTime,
      page: state.currentPage,
      pageSize: state.pageSize,
    };

    // 如果提供了Cookie，添加到参数中
    if (state.queryParams.cookie && state.queryParams.cookie.trim()) {
      params.cookie = state.queryParams.cookie.trim();
    }

    const response = await xiaohongshuSpider(params);
    const responseCode = String(response.code);
    if (responseCode === 'success' || responseCode === '200') {
      // 处理响应数据，确保是数组类型
      let responseData: XiaohongshuItem[] | any = response.data;
      if (!Array.isArray(responseData)) {
        // 如果响应数据不是数组，尝试从 data 字段获取
        if (responseData && typeof responseData === 'object' && 'data' in responseData) {
          responseData = (responseData as any).data;
        } else {
          responseData = [] as XiaohongshuItem[];
        }
      }
      const newItems: XiaohongshuItem[] = Array.isArray(responseData) ? (responseData as XiaohongshuItem[]) : [];
      
      if (isPageChange) {
        // 分页加载：追加数据并去重
        const beforeCount = state.items.length;
        state.items = deduplicateItems(newItems, state.items);
        const addedCount = state.items.length - beforeCount;
        Message.success(`成功加载第 ${state.currentPage} 页，新增 ${addedCount} 条数据（共 ${state.items.length} 条）`);
      } else {
        // 首次搜索：直接设置数据
        state.items = newItems;
        Message.success(`成功爬取 ${newItems.length} 条数据`);
      }
      
      // 标记当前页已加载
      state.loadedPages.add(state.currentPage);

      state.hasMore = newItems.length >= state.pageSize;
      // 与本地拼接列表一致：total 仅为「已加载 + 是否可能还有下一页」，避免原公式 currentPage*pageSize+pageSize 导致「共 30 条」实际只有 20 条
      state.total = state.items.length + (state.hasMore ? 1 : 0);
    } else {
      const errorMsg = (response as any).message || '爬取失败';
      Message.error(errorMsg);
      if (!isPageChange) {
        state.items = [];
        state.total = 0;
        state.hasMore = false;
      }
    }
  } catch (error: any) {
    console.error('爬取失败:', error);
    Message.error(error.message || '爬取失败，请检查网络连接或稍后重试');
    if (!isPageChange) {
      state.items = [];
      state.total = 0;
      state.hasMore = false;
    }
  } finally {
    state.searching = false;
  }
};

// 分页变化
const handlePageChange = (page: number) => {
  state.currentPage = page;
  // 检查当前页是否已加载，如果已加载则不需要重新爬取
  if (!state.loadedPages.has(page)) {
    handleSearch(true); // 传递 true 表示是分页变化
  }
};

// 每页条数变化：与后端按 page/pageSize 分页一致，需清空已加载缓存并重新从第 1 页爬取
const handleSpiderPageSizeChange = () => {
  state.items = [];
  state.loadedPages.clear();
  state.currentPage = 1;
  state.total = 0;
  state.hasMore = false;
  selectedIds.value = [];
  handleSearch(false);
};

// 判断是否有视频（用于显示视频卡片）
const hasVideo = computed(() => {
  if (!state.currentItem) return false;
  // 如果笔记类型是video，即使没有video URL也显示视频卡片
  if (state.currentItem.type === 'video') {
    return true;
  }
  // 否则检查是否有video URL
  return !!(state.currentItem.video && state.currentItem.video.trim());
});

// 将小红书图片URL转换为Nginx代理路径
// 例如: http://sns-webpic-qc.xhscdn.com/202512310138/xxx.jpg
// 转换为: /xhspic/202512310138/xxx.jpg
const getNginxProxyImageUrl = (imageUrl: string | undefined): string => {
  if (!imageUrl || !imageUrl.trim()) {
    return '';
  }
  
  // 如果是小红书图片URL，转换为Nginx代理路径
  if (imageUrl.includes('sns-webpic-qc.xhscdn.com') || 
      imageUrl.includes('xhscdn.com') ||
      imageUrl.includes('xiaohongshu.com')) {
    try {
      const url = new URL(imageUrl);
      // 提取路径部分，转换为 /xhspic/ 开头的路径
      return `/xhspic${url.pathname}${url.search}`;
    } catch (e) {
      // 如果URL解析失败，尝试简单替换
      const match = imageUrl.match(/https?:\/\/[^/]+(\/.*)/);
      if (match) {
        return `/xhspic${match[1]}`;
      }
      // 如果都失败了，返回原始URL
      return imageUrl;
    }
  }
  
  // 非小红书图片，直接返回原始URL
  return imageUrl;
};

// 将小红书视频URL转换为Nginx代理路径
// 例如: https://sns-video-bd.xhscdn.com/xxx.mp4
// 转换为: /xhsvideo/xxx.mp4
const getNginxProxyVideoUrl = (videoUrl: string | undefined): string => {
  if (!videoUrl || !videoUrl.trim()) {
    return '';
  }
  
  // 如果已经是OSS URL（包含 OSS 域名），直接返回
  // 这里先简单判断，避免循环依赖
  if (videoUrl.includes('oss') || videoUrl.includes('aliyuncs.com') || videoUrl.includes('qcloud.com')) {
    return videoUrl;
  }
  
  // 如果是小红书视频URL，转换为Nginx代理路径
  if (videoUrl.includes('sns-video-bd.xhscdn.com') || 
      videoUrl.includes('xhscdn.com') ||
      videoUrl.includes('xiaohongshu.com')) {
    try {
      const url = new URL(videoUrl);
      // 提取路径部分，转换为 /xhsvideo/ 开头的路径
      return `/xhsvideo${url.pathname}${url.search}`;
    } catch (e) {
      // 如果URL解析失败，尝试简单替换
      const match = videoUrl.match(/https?:\/\/[^/]+(\/.*)/);
      if (match) {
        return `/xhsvideo${match[1]}`;
      }
      // 如果都失败了，返回原始URL
      return videoUrl;
    }
  }
  
  // 非小红书视频，直接返回原始URL
  return videoUrl;
};

// 检查URL是否是OSS URL（已下载的视频）
const isOssUrl = (url: string | undefined): boolean => {
  if (!url) return false;
  // 检查是否是OSS URL（通常包含OSS域名或特定路径）
  // 可以根据实际OSS域名调整判断逻辑
  return !url.includes('xhscdn.com') && !url.includes('xiaohongshu.com');
};

// 查看详情
const handleViewDetail = (item: XiaohongshuItem) => {
  state.currentItem = item;
  state.detailVisible = true;
  state.videoLoadError = false; // 重置视频加载错误状态
  state.videoDownloading = false; // 重置下载状态

  // 检查视频URL是否是OSS URL（已下载的）
  if (item.video && item.video.trim() && isOssUrl(item.video)) {
    // 如果已经是OSS URL，直接使用
    state.videoOssUrl = item.video;
    console.log('检测到OSS URL，直接使用:', item.video);
  } else {
    // 如果是原始URL，重置OSS URL，等待下载
    state.videoOssUrl = '';
  }

  // 清理之前的Blob URL
  if (state.videoBlobUrl) {
    URL.revokeObjectURL(state.videoBlobUrl);
    state.videoBlobUrl = '';
  }

  // 调试信息：检查视频字段
  console.log('=== 笔记详情调试信息 ===');
  console.log('笔记ID:', item.id);
  console.log('笔记类型:', item.type);
  console.log('视频URL:', item.video);
  console.log('是否OSS URL:', isOssUrl(item.video));
  console.log('hasVideo计算值:', hasVideo.value);
  console.log('========================');
};

// 下载视频到服务器
const handleDownloadVideo = async () => {
  if (!state.currentItem?.video || !state.currentItem.video.trim()) {
    Message.error('视频URL为空，无法下载');
    return;
  }

  // 如果已经是OSS URL，不需要重新下载
  if (isOssUrl(state.currentItem.video)) {
    state.videoOssUrl = state.currentItem.video;
    Message.info('视频已下载，直接使用OSS链接');
    return;
  }

  state.videoDownloading = true;
  state.videoLoadError = false;

  try {
    console.log('开始下载视频到服务器:', state.currentItem.video);

    // 保存原始URL，用于下载
    const originalVideoUrl = state.currentItem.video;

    const response = await downloadXiaohongshuVideo(
      originalVideoUrl,
      state.queryParams.cookie && state.queryParams.cookie.trim()
        ? state.queryParams.cookie.trim()
        : undefined
    );

    const responseCode = String(response.code);
    if (responseCode === 'success' || responseCode === '200') {
      const ossUrl = typeof response.data === 'string' ? response.data : String(response.data);
      console.log('视频下载成功，OSS URL:', ossUrl);

      // 替换原视频URL为OSS URL，这样下次打开就不需要重新下载
      if (state.currentItem && ossUrl) {
        state.currentItem.video = ossUrl;
        console.log('已更新当前item的视频URL:', originalVideoUrl, '->', ossUrl);

        // 同时更新列表中的对应item
        const itemInList = state.items.find(
          (item) => item.id === state.currentItem?.id
        );
        if (itemInList) {
          itemInList.video = ossUrl;
          console.log('已更新列表中的视频URL');
        }

        // 更新OSS URL状态
        state.videoOssUrl = ossUrl;
      }

      // 重置错误状态，确保视频播放器能显示
      state.videoLoadError = false;

      Message.success('视频下载成功，正在加载...');

      // 自动播放视频
      setTimeout(() => {
        const videoElement = document.querySelector(
          '.detail-video-compact'
        ) as HTMLVideoElement;
        if (videoElement) {
          videoElement.load();
        }
      }, 100);
    } else {
      const errorMsg = (response as any).message || '下载失败';
      throw new Error(errorMsg);
    }
  } catch (error: any) {
    console.error('下载视频失败:', error);
    state.videoLoadError = true;
    Message.error(`下载视频失败: ${error.message || '未知错误'}`);
  } finally {
    state.videoDownloading = false;
  }
};

// 处理视频加载开始
const handleVideoLoadStart = () => {
  state.videoLoadError = false;
  console.log('视频开始加载，Blob URL:', state.videoBlobUrl);
};

// 处理视频元数据加载完成
const handleVideoLoadedMetadata = () => {
  console.log('视频元数据加载完成');
  const videoElement = document.querySelector(
    '.detail-video-compact'
  ) as HTMLVideoElement;
  if (videoElement) {
    console.log('视频时长:', videoElement.duration, '秒');
    console.log(
      '视频尺寸:',
      videoElement.videoWidth,
      'x',
      videoElement.videoHeight
    );
  }
};

// 处理视频可以播放
const handleVideoCanPlay = () => {
  console.log('视频可以播放');
  state.videoLoadError = false;
};

// 处理视频加载错误
const handleVideoError = (event: Event) => {
  console.error('视频加载失败:', event);
  const videoElement = event.target as HTMLVideoElement;
  console.error('视频URL:', videoElement?.src);
  console.error('原始视频URL:', state.currentItem?.video);

  // 检查错误类型
  const error = videoElement?.error;
  if (error) {
    console.error('视频错误代码:', error.code);
    console.error('视频错误消息:', error.message);

    // 错误代码说明：
    // 1 = MEDIA_ERR_ABORTED - 用户中止
    // 2 = MEDIA_ERR_NETWORK - 网络错误
    // 3 = MEDIA_ERR_DECODE - 解码错误
    // 4 = MEDIA_ERR_SRC_NOT_SUPPORTED - 格式不支持

    // 根据错误代码显示相应的错误信息
    if (error.code === 2) {
      Message.error('视频加载失败：网络错误');
    } else if (error.code === 3) {
      Message.error('视频加载失败：视频格式错误或损坏');
    }
    // 错误代码4（格式不支持）和其他错误不显示提示
  } else {
    Message.warning('视频无法播放');
  }

  state.videoLoadError = true;
};

// 在新窗口打开视频URL
const handleOpenVideoUrl = () => {
  if (state.currentItem?.video) {
    window.open(state.currentItem.video, '_blank');
  }
};

// 重试播放视频
const handleRetryVideo = () => {
  state.videoLoadError = false;
  // 强制重新加载视频
  const videoElement = document.querySelector(
    '.detail-video-compact'
  ) as HTMLVideoElement;
  if (videoElement) {
    videoElement.load();
  }
};

// 复制链接
const handleCopyUrl = async (url: string) => {
  try {
    await navigator.clipboard.writeText(url);
    Message.success('链接已复制到剪贴板');
  } catch (error) {
    Message.error('复制失败');
  }
};

// 发布笔记
const handlePublish = async (item: XiaohongshuItem) => {
  try {
    // 根据类型确定笔记类型
    let noteType = '0'; // 默认图文
    if (item.type === 'video') {
      noteType = '2'; // 视频
    } else if (item.type === 'image') {
      noteType = '1'; // 图片
    }

    // 将小红书数据映射到笔记表单数据（不下载资源，直接使用原始URL）
    const initialData: any = {
      title: item.title || '无标题',
      content: item.content || '',
      noteCover:
        item.cover ||
        (item.images && item.images.length > 0 ? item.images[0] : ''),
      fromType: '2', // 管理端（默认）
      auditStatus: '1', // 已通过（默认）
      noteType, // 0-图文, 1-图片, 2-视频
    };

    // 处理图片/视频URL（直接使用原始URL，不下载）
    if (item.type === 'video' && item.video) {
      // 视频类型，使用原始URL（将在新增页面点击确定时下载）
      initialData.urls = [item.video];
      initialData.noteType = '2';
    } else if (item.images && item.images.length > 0) {
      // 图片类型：直接使用原始URL（将在新增页面点击确定时下载）
      const maxImages = 9;
      const imagesToUse = item.images.slice(0, maxImages);
      initialData.urls = imagesToUse;
      // 单张、多张图均记为图片类型 1
      initialData.noteType = '1';
      
      // 如果图片数量超过限制，提示用户
      if (item.images.length > maxImages) {
        Message.warning(`图片数量超过限制，已自动截取前${maxImages}张图片`);
      }
    }

    // 先设置发布初始数据
    state.publishInitialData = initialData;

    // 使用 nextTick 确保数据设置完成后再打开弹窗
    await nextTick();

    // 打开发布弹窗
    state.publishNoteId = undefined; // 新增模式
    state.publishVisible = true;

    Message.info('已填充笔记数据，请检查并完善后发布');
  } catch (error) {
    console.error('发布失败:', error);
    Message.error('打开发布页面失败');
  }
};

// 从详情页发布笔记
const handlePublishFromDetail = () => {
  if (state.currentItem) {
    // 关闭详情弹窗
    state.detailVisible = false;
    // 调用发布函数
    handlePublish(state.currentItem);
  }
};

// 格式化数量
const formatCount = (count: number): string => {
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}万`;
  }
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k`;
  }
  return count.toString();
};

// 监听详情页关闭，停止视频播放
watch(
  () => state.detailVisible,
  (newVal) => {
    if (!newVal) {
      // 详情页关闭时，停止视频播放
      const videoElement = document.querySelector(
        '.detail-video-compact'
      ) as HTMLVideoElement;
      if (videoElement) {
        videoElement.pause();
        videoElement.currentTime = 0; // 重置播放位置
      }
    }
  }
);

// 组件卸载时释放Blob URL，避免内存泄漏
onBeforeUnmount(() => {
  if (state.videoBlobUrl) {
    URL.revokeObjectURL(state.videoBlobUrl);
    state.videoBlobUrl = '';
  }
});
</script>

<style scoped lang="less">
.container {
  padding: 0;
}

.batch-toolbar {
  margin-bottom: 16px;
  padding: 12px 16px;
  background: var(--color-fill-1);
  border-radius: 8px;
}

.batch-toolbar-text {
  color: var(--color-text-2);
  font-size: 14px;
}

.batch-publish-progress {
  margin-bottom: 16px;
  padding: 12px 14px;
  background: var(--color-fill-1);
  border-radius: 8px;
}

.batch-publish-progress-text {
  margin-bottom: 10px;
  color: var(--color-text-2);
  font-size: 13px;
}

.note-action-checkbox {
  flex-shrink: 0;
  margin-right: 4px;
}

// 视频下载中提示
.video-downloading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  border-radius: 8px;
  padding: 60px 40px;
}

.video-downloading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.loading-spinner-wrapper {
  margin-bottom: 32px;

  :deep(.arco-spin) {
    display: block;
  }

  :deep(.arco-spin-icon) {
    font-size: 48px;
    color: #165dff;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-text-primary {
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 12px 0;
  letter-spacing: 0.5px;
}

.loading-text-secondary {
  color: #86909c;
  font-size: 13px;
  margin: 0;
  line-height: 1.6;
}

// 视频加载中提示（保留原有样式用于其他场景）
.video-loading-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  padding: 40px;
}

// 加载状态容器
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  min-height: 300px;

  .loading-content {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  :deep(.arco-spin) {
    width: 100%;
    display: block;

    .arco-spin-children {
      width: 100%;
    }

    .arco-spin-mask {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
    }

    .arco-spin-tip {
      color: #165dff;
      font-size: 14px;
      font-weight: 500;
      margin-top: 12px;
    }

    .arco-spin-icon {
      font-size: 32px;
      color: #165dff;
    }
  }
}

// 5列网格布局
.notes-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin: -8px 0; // 只抵消上下边距
  padding: 0 32px; // 增加左右边距

  .note-item-wrapper {
    flex: 0 0 calc(20% - 13px); // 5列布局，每列20%，减去间距
    min-width: 0; // 防止内容溢出
    max-width: calc(20% - 13px);

    // 响应式布局
    @media (max-width: 1920px) {
      flex: 0 0 calc(20% - 13px);
      max-width: calc(20% - 13px);
    }

    @media (max-width: 1600px) {
      flex: 0 0 calc(25% - 12px); // 4列
      max-width: calc(25% - 12px);
    }

    @media (max-width: 1200px) {
      flex: 0 0 calc(33.333% - 11px); // 3列
      max-width: calc(33.333% - 11px);
    }

    @media (max-width: 768px) {
      flex: 0 0 calc(50% - 8px); // 2列
      max-width: calc(50% - 8px);
    }

    @media (max-width: 576px) {
      flex: 0 0 100%; // 1列
      max-width: 100%;
    }
  }
}

.note-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

.note-cover {
  position: relative;
  width: 100%;
  overflow: hidden;
  background: #f5f5f5;
  aspect-ratio: 3 / 4;
  border-radius: 12px 12px 0 0;
  min-height: 240px;
  max-height: 360px;

  .cover-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  :deep(.arco-image) {
    width: 100%;
    height: 100%;
    display: block;
  }

  :deep(.arco-image-img) {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 12px 12px 0 0;
  }

  :deep(.arco-image-wrapper) {
    width: 100%;
    height: 100%;
  }

  .video-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1;
    transition: all 0.3s ease;

    &:hover {
      transform: scale(1.1);
    }
  }

  .image-count-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(4px);
    border-radius: 16px;
    padding: 6px 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1;
    transition: all 0.3s ease;

    span {
      color: #fff;
      font-size: 13px;
      font-weight: 500;
      line-height: 1;
    }

    &:hover {
      background: rgba(0, 0, 0, 0.75);
      transform: scale(1.05);
    }
  }
}

.note-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.note-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 12px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 45px;
  transition: color 0.3s ease;

  .note-card:hover & {
    color: #165dff;
  }
}

.note-author {
  font-size: 13px;
  color: #86909c;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.note-stats {
  font-size: 13px;
  margin-bottom: 16px;
  flex: 1;

  .stat-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    border-radius: 4px;
    transition: background-color 0.3s ease;
    font-weight: 500;

    span {
      color: #4e5969;
    }

    &.stat-like:hover {
      background-color: rgba(245, 63, 63, 0.1);
    }

    &.stat-collect:hover {
      background-color: rgba(255, 125, 0, 0.1);
    }

    &.stat-comment:hover {
      background-color: rgba(22, 93, 255, 0.1);
    }
  }
}

.note-actions {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f2f3f5;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

// 详情弹窗样式 - 强制设置高度
:deep(.note-detail-modal) {
  // 设置整个Modal容器的高度
  .arco-modal-container {
    height: 820px !important;
    max-height: 820px !important;
  }

  .arco-modal {
    height: 820px !important;
    max-height: 820px !important;
    margin-top: calc((100vh - 820px) / 2) !important;
    display: flex !important;
    flex-direction: column !important;
    overflow: hidden !important;
  }

  .arco-modal-header {
    flex-shrink: 0 !important;
    padding: 16px 20px !important;
    border-bottom: 1px solid #f2f3f5 !important;
    height: auto !important;
  }

  .arco-modal-body {
    flex: 1 !important;
    height: 700px !important;
    max-height: 700px !important;
    min-height: 0 !important;
    overflow-y: auto !important;
    overflow-x: hidden !important;
    padding: 16px 20px !important;
    position: relative !important;
  }

  // 自定义滚动条样式 - 确保滚动条可见
  .arco-modal-body::-webkit-scrollbar {
    width: 10px;
  }

  .arco-modal-body::-webkit-scrollbar-track {
    background: #f5f5f5;
    border-radius: 5px;
    margin: 4px 0;
  }

  .arco-modal-body::-webkit-scrollbar-thumb {
    background: #bfbfbf;
    border-radius: 5px;
    border: 2px solid #f5f5f5;

    &:hover {
      background: #999;
    }
  }

  // 确保滚动条始终可见（Firefox）
  .arco-modal-body {
    scrollbar-width: thin;
    scrollbar-color: #bfbfbf #f5f5f5;
  }
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  // 确保内容可以超出容器，触发滚动
  min-height: min-content;
}

// 紧凑布局样式
.info-card {
  :deep(.arco-card-body) {
    padding: 12px 16px;
  }

  :deep(.arco-card-header) {
    padding: 0;
    border: none;
  }
}

.detail-header-compact {
  .detail-title-compact {
    margin-bottom: 8px;

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #1d2129;
      margin: 0;
      line-height: 1.4;
    }
  }

  .detail-meta-compact {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 12px;
  }

  .detail-author-compact {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #86909c;
  }

  .detail-stats-compact {
    display: flex;
    gap: 16px;
    align-items: center;

    .stat-item-compact {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #4e5969;
      font-weight: 500;
    }
  }
}

.content-card-compact,
.images-card-compact,
.video-card-compact,
.meta-card-compact {
  :deep(.arco-card-body) {
    padding: 12px 16px;
  }

  :deep(.arco-card-header) {
    padding: 12px 16px 8px;
    font-size: 14px;
  }
}

.note-content-text-compact {
  font-size: 14px;
  line-height: 1.6;
  color: #1d2129;
  white-space: pre-wrap;
  word-break: break-word;
}

.image-wrapper-compact {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 4px;
  overflow: hidden;
  background: #f5f5f5;
  cursor: pointer;
  transition: transform 0.3s ease;

  &:hover {
    transform: scale(1.05);
  }

  .detail-image-compact {
    width: 100%;
    height: 100%;

    :deep(.arco-image-img) {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

// 确保每行显示6张图片（24/4 = 6列）
.images-card-compact {
  :deep(.arco-row) {
    margin: 0 -3px;
  }

  :deep(.arco-col) {
    padding: 0 3px;
  }

  // 在大屏幕上每行6张（16.666% = 1/6）
  @media (min-width: 992px) {
    :deep(.arco-col) {
      flex: 0 0 16.666% !important;
      max-width: 16.666% !important;
    }
  }
}

.video-wrapper-compact {
  width: 100%;
  display: flex;
  justify-content: center;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  position: relative;

  .detail-video-compact {
    width: 100%;
    max-width: 100%;
    max-height: 500px;
    min-height: 300px;
    outline: none;
    display: block;
    background: #000;
  }

  .video-error-tip {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: rgba(0, 0, 0, 0.8);
    color: #fff;
    padding: 20px;
    border-radius: 8px;
    text-align: center;
    z-index: 10;
    max-width: 80%;
  }
}

.video-cover-card {
  :deep(.arco-card-body) {
    padding: 12px 16px;
  }

  :deep(.arco-card-header) {
    padding: 12px 16px 8px;
    font-size: 14px;
  }
}

.video-cover-wrapper {
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;

  .video-cover-image {
    width: 100%;
    height: 100%;

    :deep(.arco-image-img) {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.meta-card-compact {
  :deep(.arco-descriptions-item-label) {
    font-weight: 600;
    color: #4e5969;
    width: 80px;
    font-size: 13px;
    padding: 8px 12px;
  }

  :deep(.arco-descriptions-item-value) {
    padding: 8px 12px;
    font-size: 13px;
  }

  .meta-value-compact {
    color: #1d2129;
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 12px;
  }

  .link-text-compact {
    color: #165dff;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    word-break: break-all;
    font-size: 12px;

    &:hover {
      text-decoration: underline;
    }
  }
}

// 响应式优化
@media (max-width: 768px) {
  .note-card {
    .note-content {
      padding: 12px;
    }

    .note-title {
      font-size: 14px;
      min-height: 40px;
    }

    .note-stats {
      font-size: 12px;
    }
  }

  :deep(.note-detail-modal) {
    .arco-modal-container {
      height: 820px !important;
      max-height: 820px !important;
    }

    .arco-modal {
      width: 95% !important;
      height: 820px !important;
      max-height: 820px !important;
      margin-top: calc((100vh - 820px) / 2) !important;
      display: flex !important;
      flex-direction: column !important;
      overflow: hidden !important;
    }

    .arco-modal-body {
      flex: 1 !important;
      height: 700px !important;
      max-height: 700px !important;
      min-height: 0 !important;
      overflow-y: auto !important;
      overflow-x: hidden !important;
    }
  }

  .detail-stats {
    .stat-item {
      min-width: 100px;
      padding: 10px 12px;
    }
  }
}
</style>
