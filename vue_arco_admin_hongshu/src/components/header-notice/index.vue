<template>
  <a-popover
    v-model:popup-visible="popoverVisible"
    position="br"
    trigger="click"
    :arrow-style="{ display: 'none' }"
    :content-style="{ padding: 0, width: '320px' }"
    content-class="header-notice-popover"
    @popup-visible-change="onPopupVisibleChange"
  >
    <a-tooltip content="通知公告">
      <span class="notice-trigger">
        <a-badge :count="unreadCount" :max-count="99" :offset="[2, -2]">
          <a-button class="nav-btn" type="outline" shape="circle">
            <template #icon>
              <icon-notification />
            </template>
          </a-button>
        </a-badge>
      </span>
    </a-tooltip>
    <template #content>
      <div class="notice-header">
        <span class="notice-title">通知公告</span>
        <a-button type="text" size="mini" class="mark-all" @click="markAllRead">
          全部已读
        </a-button>
      </div>
      <a-spin
        :loading="noticeLoading"
        style="display: block; min-height: 120px"
      >
        <div
          v-if="!noticeLoading && noticeList.length === 0"
          class="notice-empty"
        >
          <icon-message class="empty-ico" />
          <div>暂无公告</div>
        </div>
        <div v-else class="notice-list">
          <div
            v-for="item in noticeList"
            :key="item.noticeId"
            class="notice-item"
            :class="{ 'is-read': item.isRead }"
            @click="previewNotice(item)"
          >
            <a-tag size="small" :color="noticeTypeColor(item.noticeType)">
              {{ noticeTypeLabel(item.noticeType) }}
            </a-tag>
            <span class="notice-item-title">{{ item.noticeTitle }}</span>
            <span class="notice-item-date">{{ item.createTime }}</span>
          </div>
        </div>
      </a-spin>
    </template>
  </a-popover>

  <a-modal
    v-model:visible="previewVisible"
    :title="previewTitle"
    width="680px"
    :footer="false"
    unmount-on-close
    body-class="notice-preview-body"
  >
    <div class="notice-preview-meta">
      <a-tag :color="noticeTypeColor(previewNoticeType)">
        {{ noticeTypeLabel(previewNoticeType) }}
      </a-tag>
      <span class="notice-preview-info">
        <icon-user /> {{ previewCreateBy }}
      </span>
      <span class="notice-preview-info">
        <icon-clock-circle /> {{ previewCreateTime }}
      </span>
    </div>
    <div class="notice-preview-divider" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div class="notice-preview-content" v-html="previewContent" />
  </a-modal>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import {
  IconClockCircle,
  IconMessage,
  IconNotification,
  IconUser,
} from '@arco-design/web-vue/es/icon';
import {
  getNotice,
  listNoticeTop,
  markNoticeRead,
  markNoticeReadAll,
  type NoticeTopRecord,
} from '@/api/system/notice';

/** 与后端 HashMap 序列化一致：0/false 为未读 */
function normalizeNoticeRead(v: unknown): boolean {
  if (v === true || v === 1 || v === '1') return true;
  if (v === false || v === 0 || v === '0') return false;
  if (typeof v === 'number') return v !== 0;
  return false;
}

function noticeTypeLabel(type: string) {
  if (type === '1') return '通知';
  if (type === '3') return '告警';
  return '公告';
}
function noticeTypeColor(type: string) {
  if (type === '1') return 'orangered';
  if (type === '3') return 'red';
  return 'green';
}

const popoverVisible = ref(false);
const noticeList = ref<NoticeTopRecord[]>([]);
const unreadCount = ref(0);
const noticeLoading = ref(false);
const previewVisible = ref(false);
const previewTitle = ref('');
const previewContent = ref('');
const previewNoticeType = ref('');
const previewCreateBy = ref('');
const previewCreateTime = ref('');

async function loadNoticeTop() {
  noticeLoading.value = true;
  try {
    const res = (await listNoticeTop()) as any;
    const payload = res?.data || res;
    const rawList = payload?.data || [];
       noticeList.value = rawList.map((n: any) => ({
      ...n,
      isRead: normalizeNoticeRead(n.isRead),
    }));
    const listUnread = noticeList.value.filter((n) => !n.isRead).length;
    const serverUnread = Number(payload?.unreadCount);
    // 后端曾用 Integer 与 Long 比较导致 unreadCount 恒为 0；与列表推导取较大值，避免徽标不显示
    unreadCount.value = Math.max(
      Number.isFinite(serverUnread) ? serverUnread : 0,
      listUnread
    );
  } finally {
    noticeLoading.value = false;
  }
}

onMounted(() => {
  loadNoticeTop();
});

function onPopupVisibleChange(visible: boolean) {
  if (visible) {
    loadNoticeTop();
  }
}

async function previewNotice(item: NoticeTopRecord) {
  const nid = item.noticeId;
  if (nid == null) return;
  if (!item.isRead) {
    markNoticeRead(nid).catch(() => {});
    const idx = noticeList.value.findIndex((n) => n.noticeId === nid);
    if (idx !== -1) {
      noticeList.value[idx] = { ...item, isRead: true };
    }
    unreadCount.value = Math.max(0, unreadCount.value - 1);
  }
  if (item.noticeContent) {
    previewTitle.value = item.noticeTitle || '';
    previewContent.value = item.noticeContent;
    previewNoticeType.value = String(item.noticeType ?? '');
    previewCreateBy.value = item.createBy || '';
    previewCreateTime.value = item.createTime || '';
    previewVisible.value = true;
    return;
  }
  try {
    const res = (await getNotice(nid)) as any;
    const notice = res?.data || res;
    previewTitle.value = notice.noticeTitle || '';
    previewContent.value = notice.noticeContent || '';
    previewNoticeType.value = String(notice.noticeType ?? '');
    previewCreateBy.value = notice.createBy || '';
    previewCreateTime.value = notice.createTime || '';
    previewVisible.value = true;
  } catch {
    /* ignore */
  }
}

function markAllRead() {
  const ids = noticeList.value
    .map((n) => n.noticeId)
    .filter(Boolean) as number[];
  if (!ids.length) return;
  markNoticeReadAll(ids.join(',')).catch(() => {});
  noticeList.value = noticeList.value.map((n) => ({ ...n, isRead: true }));
  unreadCount.value = 0;
}
</script>

<style scoped lang="less">
.notice-trigger {
  display: inline-flex;
  vertical-align: middle;
}

.notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: var(--color-fill-1);
  border-bottom: 1px solid var(--color-border-2);
  font-size: 13px;
  font-weight: 600;
}

.mark-all {
  font-weight: normal;
  padding: 0 4px;
}

.notice-empty {
  padding: 28px 16px;
  text-align: center;
  color: var(--color-text-3);
  font-size: 12px;

  .empty-ico {
    font-size: 28px;
    display: block;
    margin: 0 auto 8px;
    opacity: 0.45;
  }
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--color-border-1);
  cursor: pointer;
  transition: background 0.15s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--color-fill-1);
  }

  &.is-read {
    opacity: 0.55;
  }
}

.notice-item-title {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-item-date {
  flex-shrink: 0;
  font-size: 11px;
  color: var(--color-text-3);
}
</style>

<style lang="less">
.header-notice-popover {
  padding: 0 !important;
}

.notice-preview-body {
  .notice-preview-meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
    padding: 8px 0 12px;
    font-size: 12px;
    color: var(--color-text-3);
  }

  .notice-preview-info {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  .notice-preview-divider {
    height: 1px;
    background: var(--color-border-2);
    margin-bottom: 16px;
  }

  .notice-preview-content {
    font-size: 14px;
    line-height: 1.85;
    color: var(--color-text-1);
    word-break: break-word;

    img {
      max-width: 100%;
      border-radius: 4px;
    }
  }
}
</style>
