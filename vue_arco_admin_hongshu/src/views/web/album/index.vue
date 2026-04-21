<template>
  <div class="album-page">
    <Breadcrumb
      :items="[$t('biz.adminNoteMenuTitle'), $t('biz.adminAlbumMenuTitle')]"
    />

    <a-card class="album-shell general-card r-nav-card" :bordered="false">
      <!-- 筛选 -->
      <div class="album-filter">
        <a-form
          :model="state.queryParams"
          layout="inline"
          class="album-filter-form"
          auto-label-width
        >
          <a-form-item field="title" :label="$t('biz.albumName')">
            <a-input
              v-model="state.queryParams.title"
              allow-clear
              :placeholder="$t('biz.albumNameInputPh')"
              style="width: 200px"
            />
          </a-form-item>
          <a-form-item field="dateRange" :label="$t('biz.createTime')">
            <a-range-picker
              v-model="state.queryParams.dateRange"
              style="width: 260px"
              format="YYYY-MM-DD"
            />
          </a-form-item>
        </a-form>
        <div class="album-filter-actions">
          <a-button type="primary" @click="handleQuery">
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
        </div>
      </div>
      <a-divider style="margin-top: 0" />

      <!-- 工具栏 -->
      <div class="album-toolbar">
        <a-space>
          <a-button
            v-permission="['web:album:add']"
            type="primary"
            @click="handleAdd"
          >
            <template #icon>
              <IconPlus />
            </template>
            {{ $t('biz.newAlbumToolbar') }}
          </a-button>
          <a-button
            v-permission="['web:album:remove']"
            status="danger"
            :disabled="state.selectedKeys.length <= 0"
            @click="handleDelete"
          >
            <template #icon>
              <IconDelete />
            </template>
            {{ $t('biz.batchDelete') }}
          </a-button>
        </a-space>
        <span v-if="state.selectedKeys.length" class="album-toolbar-hint">
          {{ $t('biz.selectedItemsCount', { n: state.selectedKeys.length }) }}
        </span>
      </div>

      <a-spin :loading="loading" class="album-spin">
        <div v-if="state.list.length" class="album-grid">
          <div
            v-for="item in state.list"
            :key="item.id"
            class="album-grid-cell"
          >
            <a-card class="album-item-card" :bordered="true" hoverable>
              <div class="album-item-cover-wrap">
                <div
                  class="album-item-cover"
                  role="button"
                  tabindex="0"
                  :title="
                    $t('biz.viewNotesInAlbumCoverTitle', {
                      count: albumNoteCount(item),
                    })
                  "
                  @click="openAlbumNotes(item)"
                  @keydown.enter="openAlbumNotes(item)"
                >
                  <img
                    v-if="item.albumCover"
                    :src="item.albumCover"
                    class="album-item-cover-img"
                    alt=""
                    @error="onCoverError"
                  />
                  <div v-else class="album-item-cover-placeholder">
                    <icon-image class="album-item-cover-icon" />
                    <span>{{ $t('biz.noCover') }}</span>
                  </div>
                  <div class="album-item-count-badge">
                    {{ $t('biz.notesCountUnit', { count: albumNoteCount(item) }) }}
                  </div>
                </div>
                <div class="album-item-check" @click.stop>
                  <a-checkbox
                    :model-value="state.selectedKeys.includes(item.id)"
                    @change="(v) => onAlbumCheckChange(item.id, v)"
                  />
                </div>
              </div>

              <div class="album-item-footer">
                <div class="album-item-footer-left">
                  <div class="album-item-title-row">
                    <span
                      class="album-item-title"
                      :title="item.title || $t('biz.unnamedAlbum')"
                    >
                      {{ item.title || $t('biz.unnamedAlbum') }}
                    </span>
                  </div>
                  <div
                    v-if="item.uid"
                    class="album-item-meta"
                    :title="albumOwnerTitle(item)"
                  >
                    <span class="album-item-meta-label">{{ $t('biz.ownerUserPrefix') }}</span>
                    <span class="album-item-owner-name">{{
                      item.ownerUsername || '—'
                    }}</span>
                    <span class="album-item-owner-sep">·</span>
                    <span class="album-item-owner-id">{{ $t('biz.idDisplayPrefix') }}{{ item.uid }}</span>
                  </div>
                  <!-- <div class="album-item-note-count-line">
                    专辑内笔记：<strong>{{ albumNoteCount(item) }}</strong> 篇
                  </div> -->
                </div>
                <div class="album-item-actions">
                  <a-tooltip :content="$t('biz.viewNotesInAlbum')">
                    <a-button
                      type="text"
                      size="small"
                      @click="openAlbumNotes(item)"
                    >
                      <template #icon>
                        <icon-file />
                      </template>
                    </a-button>
                  </a-tooltip>
                  <a-tooltip :content="$t('common.edit')">
                    <a-button type="text" size="small" @click="openEdit(item)">
                      <template #icon>
                        <icon-edit />
                      </template>
                    </a-button>
                  </a-tooltip>
                  <a-tooltip :content="$t('common.delete')">
                    <a-button
                      type="text"
                      size="small"
                      status="danger"
                      @click="handleDeleteSingle(item)"
                    >
                      <template #icon>
                        <icon-delete />
                      </template>
                    </a-button>
                  </a-tooltip>
                </div>
              </div>
            </a-card>
          </div>
        </div>

        <a-empty
          v-else
          class="album-empty"
          :description="$t('biz.albumEmptyDesc')"
        />
      </a-spin>

      <div v-if="state.list.length" class="album-pagination">
        <a-pagination
          :total="pagination.total"
          :current="pagination.current"
          :page-size="pagination.pageSize"
          show-total
          show-page-size
          @change="onPageChange"
          @page-size-change="onPageSizeChange"
        />
      </div>
    </a-card>

    <EditAlbum
      v-if="state.editVisible"
      :id="state.currentId"
      :open="state.editVisible"
      @cancel="handleCancel"
      @success="handleSuccess"
    />

    <a-drawer
      v-model:visible="notesDrawerVisible"
      :width="720"
      unmount-on-close
      :title="notesDrawerTitle"
      @cancel="closeAlbumNotes"
    >
      <a-table
        row-key="id"
        :columns="albumNoteColumns"
        :data="albumNotesList"
        :loading="notesLoading"
        :pagination="notesPagination"
        @page-change="onAlbumNotesPageChange"
        @page-size-change="onAlbumNotesPageSizeChange"
      >
        <template #cover="{ record }">
          <div class="album-note-cover-cell">
            <img
              v-if="record.noteCover"
              :src="record.noteCover"
              alt=""
              class="album-note-thumb"
            />
            <span v-else class="album-note-thumb-ph">—</span>
          </div>
        </template>
        <template #statusText="{ record }">
          {{ formatNoteStatus(record.status) }}
        </template>
      </a-table>
    </a-drawer>
  </div>
</template>

<script lang="ts" setup>
import useLoading from '@/hooks/loading';
import { reactive, onMounted, ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Message, Modal } from '@arco-design/web-vue';
import type { TableColumnData } from '@arco-design/web-vue';
import { Pagination } from '@/types/global';
import { getAlbumList, deleteBatchAlbum, getAlbumNotes } from '@/api/web/album';
import EditAlbum from './components/edit-album.vue';

const { t } = useI18n();
const { loading, setLoading } = useLoading(true);
const { loading: notesLoading, setLoading: setNotesLoading } =
  useLoading(false);

const notesDrawerVisible = ref(false);
const currentAlbumForNotes = ref<{ id: string; title: string } | null>(null);
const albumNotesList = ref<Record<string, unknown>[]>([]);

const notesPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
});

const notesDrawerTitle = computed(() => {
  const title =
    currentAlbumForNotes.value?.title || t('biz.unnamedAlbum');
  return t('biz.albumNotesInDrawerTitle', { title });
});

const albumNoteColumns = computed<TableColumnData[]>(() => {
  return [
    {
      title: t('biz.cover'),
      dataIndex: 'noteCover',
      slotName: 'cover',
      width: 76,
    },
    {
      title: t('biz.noteTitle'),
      dataIndex: 'title',
      ellipsis: true,
      tooltip: true,
      width: 220,
    },
    {
      title: t('biz.author'),
      dataIndex: 'username',
      width: 110,
      ellipsis: true,
      tooltip: true,
    },
    {
      title: t('biz.shelfColumn'),
      dataIndex: 'status',
      slotName: 'statusText',
      width: 88,
    },
    {
      title: t('biz.auditColumnShort'),
      dataIndex: 'auditStatus',
      width: 88,
      ellipsis: true,
      tooltip: true,
    },
  ];
});

const basePagination: Pagination = {
  current: 1,
  pageSize: 10,
};
const pagination = reactive({
  ...basePagination,
});

const state = reactive({
  queryParams: {
    title: undefined as string | undefined,
    dateRange: [] as string[],
    orderByColumn: 'createTime' as string | undefined,
    isAsc: 'desc' as string | undefined,
  },
  selectedKeys: [] as number[],
  editVisible: false,
  currentId: 0,
  list: [] as any[],
});

const fetchListData = async () => {
  setLoading(true);
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.pageSize,
      ...state.queryParams,
    };

    if (
      state.queryParams.dateRange &&
      state.queryParams.dateRange.length === 2
    ) {
      const [beginTime, endTime] = state.queryParams.dateRange;
      params['params[beginTime]'] = beginTime;
      params['params[endTime]'] = endTime;
      delete params.dateRange;
    } else {
      delete params.dateRange;
    }

    if (state.queryParams.orderByColumn) {
      params.orderByColumn = state.queryParams.orderByColumn;
      params.isAsc = state.queryParams.isAsc;
    }

    Object.keys(params).forEach((key) => {
      if (
        params[key] === '' ||
        params[key] === null ||
        params[key] === undefined
      ) {
        delete params[key];
      }
    });

    const res = await getAlbumList(params);
    const data = res?.data || res || {};
    const rows = data.rows || data.records || data.list || [];
    const total = data.total ?? rows.length;

    state.list = rows;
    pagination.total = total;
  } catch (error) {
    console.error('获取专辑列表失败:', error);
    Message.error(t('biz.fetchListFail'));
  } finally {
    setLoading(false);
  }
};

onMounted(() => {
  fetchListData();
});

const onPageChange = (current: number) => {
  pagination.current = current;
  fetchListData();
};

const onPageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize;
  pagination.current = 1;
  fetchListData();
};

const handleQuery = () => {
  pagination.current = 1;
  fetchListData();
};

const resetQueryForm = () => {
  state.queryParams.title = undefined;
  state.queryParams.dateRange = [];
  pagination.current = 1;
  fetchListData();
};

function albumNoteCount(row: { imgCount?: number | string | null }) {
  const n = row?.imgCount;
  if (n === null || n === undefined || n === '') return 0;
  const v = Number(n);
  return Number.isFinite(v) ? v : 0;
}

function formatNoteStatus(status: unknown) {
  const s = String(status ?? '');
  if (s === '1') return t('biz.listedStatus');
  return t('biz.unlistedStatus');
}

function parseAlbumNotesResponse(res: unknown) {
  const r = res as Record<string, unknown>;
  const page = (r?.data ?? r) as Record<string, unknown>;
  const records = (page?.records ?? []) as Record<string, unknown>[];
  return {
    records,
    total: Number(page?.total) || 0,
    current: Number(page?.current) || 1,
    size: Number(page?.size) || 10,
  };
}

async function loadAlbumNotes() {
  const album = currentAlbumForNotes.value;
  if (!album?.id) return;
  setNotesLoading(true);
  try {
    const res = await getAlbumNotes(album.id, {
      current: notesPagination.current,
      size: notesPagination.pageSize,
    });
    const { records, total, current, size } = parseAlbumNotesResponse(res);
    albumNotesList.value = records;
    notesPagination.total = total;
    notesPagination.current = current;
    notesPagination.pageSize = size;
  } catch (e) {
    console.error(e);
    Message.error(t('biz.loadAlbumNotesError'));
    albumNotesList.value = [];
    notesPagination.total = 0;
  } finally {
    setNotesLoading(false);
  }
}

function openAlbumNotes(row: { id?: string | number; title?: string }) {
  const id = row?.id != null ? String(row.id) : '';
  if (!id) {
    Message.warning(t('biz.invalidAlbumId'));
    return;
  }
  currentAlbumForNotes.value = {
    id,
    title: (row.title as string) || t('biz.unnamedAlbum'),
  };
  notesPagination.current = 1;
  notesDrawerVisible.value = true;
  loadAlbumNotes();
}

function closeAlbumNotes() {
  notesDrawerVisible.value = false;
  currentAlbumForNotes.value = null;
  albumNotesList.value = [];
}

function onAlbumNotesPageChange(current: number) {
  notesPagination.current = current;
  loadAlbumNotes();
}

function onAlbumNotesPageSizeChange(pageSize: number) {
  notesPagination.pageSize = pageSize;
  notesPagination.current = 1;
  loadAlbumNotes();
}

const openEdit = (row: any) => {
  state.currentId = row.id;
  state.editVisible = true;
};

function setAlbumSelected(id: number, checked: boolean) {
  const idx = state.selectedKeys.indexOf(id);
  if (checked) {
    if (idx < 0) state.selectedKeys.push(id);
  } else if (idx >= 0) {
    state.selectedKeys.splice(idx, 1);
  }
}

function onAlbumCheckChange(id: number, checked: boolean) {
  setAlbumSelected(id, !!checked);
}

function onCoverError(e: Event) {
  const el = e.target as HTMLImageElement;
  if (el) el.style.display = 'none';
}

function albumOwnerTitle(item: { ownerUsername?: string; uid?: string }) {
  const name = item.ownerUsername || '—';
  return t('biz.albumOwnerTooltip', { name, id: item.uid ?? '' });
}

const handleAdd = () => {
  state.currentId = 0;
  state.editVisible = true;
};

const handleCancel = () => {
  state.editVisible = false;
  state.currentId = 0;
};

const handleSuccess = () => {
  state.editVisible = false;
  state.currentId = 0;
  fetchListData();
};

const handleDelete = () => {
  const ids: number[] = state.selectedKeys as unknown as number[];
  if (!ids.length) {
    Message.error(t('biz.selectAlbumsFirst'));
    return;
  }
  Modal.confirm({
    titleAlign: 'start',
    title: t('biz.albumBatchDeleteTitle'),
    content: t('biz.albumBatchDeleteBody', { count: ids.length }),
    okText: t('common.delete'),
    cancelText: t('common.cancel'),
    okButtonProps: { status: 'danger' },
    onOk: async () => {
      try {
        const res = await deleteBatchAlbum(ids);
        if (res?.code === 200 || res?.status === 200) {
          Message.success(t('biz.deleteSuccess'));
          state.selectedKeys = [];
          fetchListData();
        } else {
          Message.error(
            (res as any)?.msg ||
              (res as any)?.message ||
              t('biz.deleteFailGeneric')
          );
        }
      } catch (error: any) {
        Message.error(
          error?.response?.data?.msg ||
            error?.response?.data?.message ||
            error?.message ||
            t('biz.deleteFailGeneric')
        );
      }
    },
  });
};

const handleDeleteSingle = (row: any) => {
  Modal.confirm({
    titleAlign: 'start',
    title: t('biz.albumBatchDeleteTitle'),
    content: t('biz.albumSingleDeleteBody', {
      name: row.title || t('biz.albumUnnamedShort'),
    }),
    okText: t('common.delete'),
    cancelText: t('common.cancel'),
    okButtonProps: { status: 'danger' },
    onOk: async () => {
      try {
        const res = await deleteBatchAlbum([row.id]);
        if (res?.code === 200 || res?.status === 200) {
          Message.success(t('biz.deleteSuccess'));
          fetchListData();
        } else {
          Message.error(
            (res as any)?.msg ||
              (res as any)?.message ||
              t('biz.deleteFailGeneric')
          );
        }
      } catch (error: any) {
        Message.error(
          error?.response?.data?.msg ||
            error?.response?.data?.message ||
            error?.message ||
            t('biz.deleteFailGeneric')
        );
      }
    },
  });
};
</script>

<style scoped lang="less">
.album-page {
  padding: 0 20px 24px;
}

.album-shell {
  border-radius: 8px;
}

.album-shell :deep(.arco-card-body) {
  padding: 16px 20px 20px;
}

.album-filter {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  // margin-bottom: 16px;
  // border-radius: 8px;
  // background: var(--color-fill-2);
  // border: 1px solid var(--color-border-2);
}

.album-filter-form {
  flex: 1;
  min-width: 0;
}

.album-filter-form :deep(.arco-form-item) {
  margin-bottom: 8px;
  margin-right: 16px;
}

.album-filter-actions {
  display: flex;
  flex-shrink: 0;
  gap: 10px;
  padding-top: 2px;
}

.album-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.album-toolbar-hint {
  font-size: 13px;
  color: var(--color-text-3);
}

.album-spin {
  display: block;
  min-height: 200px;
}

.album-spin :deep(.arco-spin) {
  width: 100%;
}

.album-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

@media (min-width: 1600px) {
  .album-grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

.album-grid-cell {
  min-width: 0;
}

.album-item-card {
  border-radius: 10px;
  overflow: hidden;
  transition: box-shadow 0.2s ease;
}

.album-item-card :deep(.arco-card-body) {
  padding: 0;
}

.album-item-cover-wrap {
  position: relative;
}

.album-item-cover {
  position: relative;
  aspect-ratio: 4 / 3;
  background: var(--color-fill-2);
  cursor: pointer;
  overflow: hidden;
  outline: none;
}

.album-item-count-badge {
  position: absolute;
  right: 8px;
  bottom: 8px;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 10px;
  pointer-events: none;
  line-height: 1.3;
}

.album-item-cover:focus-visible {
  box-shadow: inset 0 0 0 2px rgb(var(--primary-6));
}

.album-item-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.25s ease;
}

.album-item-cover:hover .album-item-cover-img {
  transform: scale(1.04);
}

.album-item-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--color-text-3);
  font-size: 12px;
  background: linear-gradient(
    160deg,
    var(--color-fill-2) 0%,
    var(--color-fill-3) 100%
  );
}

.album-item-cover-icon {
  font-size: 32px;
  opacity: 0.4;
}

.album-item-check {
  position: absolute;
  left: 10px;
  top: 10px;
  z-index: 2;
  padding: 5px 6px 2px 1px;
  border-radius: 5px;
  background: color-mix(in srgb, var(--color-bg-2) 88%, transparent);
  backdrop-filter: blur(6px);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.album-item-check :deep(.arco-checkbox) {
  margin: 0;
}

.album-item-footer {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 12px 10px;
  border-top: 1px solid var(--color-border-2);
}

.album-item-footer-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.album-item-title-row {
  min-height: 22px;
}

.album-item-meta {
  font-size: 12px;
  color: var(--color-text-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-item-meta-label {
  margin-right: 2px;
}

.album-item-owner-name {
  font-weight: 500;
  color: var(--color-text-2);
}

.album-item-owner-sep {
  margin: 0 4px;
  opacity: 0.55;
}

.album-item-owner-id {
  font-variant-numeric: tabular-nums;
}

.album-item-title {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-1);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-item-note-count-line {
  font-size: 12px;
  color: var(--color-text-3);
  margin-top: 2px;
  line-height: 1.35;
}

.album-note-cover-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.album-note-thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
  display: block;
  vertical-align: middle;
}

.album-note-thumb-ph {
  color: var(--color-text-4);
  font-size: 12px;
}

.album-item-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 0;
  margin: 0 -4px;
  align-self: flex-end;
}

.album-item-actions :deep(.arco-btn) {
  color: var(--color-text-2);
}

.album-item-actions :deep(.arco-btn-status-danger) {
  color: rgb(var(--danger-6));
}

.album-empty {
  padding: 48px 16px;
  background: var(--color-fill-1);
  border-radius: 8px;
  border: 1px dashed var(--color-border-2);
}

.album-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 4px;
}
</style>
