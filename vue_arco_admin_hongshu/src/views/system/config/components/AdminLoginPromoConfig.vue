<template>
  <div class="admin-login-promo">
    <div class="page-head">
      <div class="page-head-text">
        <h4 class="page-title">管理端登录页圆形展示区</h4>
        <p class="form-tip">
          配置 Arco 登录页左侧 7
          个圆框；视频、图片可留空（使用内置素材）。保存后刷新登录页生效。
        </p>
      </div>
      <a-button
        type="primary"
        status="success"
        :loading="saving"
        @click="handleSave"
      >
        保存配置
      </a-button>
    </div>

    <div class="promo-grid">
      <div v-for="(item, index) in list" :key="index" class="promo-item-block">
        <a-card class="promo-card" :bordered="true">
          <template #title>
            <span class="promo-card__title">
              <span class="promo-card__badge">{{ index + 1 }}</span>
              圆框
            </span>
          </template>
          <template #extra>
            <span class="promo-card__switch-label">展示</span>
            <a-switch v-model="item.enabled" size="small" />
          </template>

          <div class="promo-card__body">
            <div class="promo-card__preview-col">
              <div
                class="preview-circle"
                :class="{ 'preview-circle--disabled': !item.enabled }"
                :style="{
                  background: item.background || 'var(--color-fill-2)',
                }"
              >
                <video
                  v-if="previewVideoUrl(item)"
                  :key="`v-${index}-${previewVideoUrl(item)}`"
                  class="preview-media"
                  muted
                  loop
                  playsinline
                  autoplay
                  preload="metadata"
                >
                  <source :src="previewVideoUrl(item)" type="video/mp4" />
                </video>
                <img
                  v-else-if="previewImageUrl(item)"
                  :key="`i-${index}-${previewImageUrl(item)}`"
                  :src="previewImageUrl(item)"
                  alt=""
                  class="preview-media"
                  @error="onPreviewImgError"
                />
                <div v-else class="preview-placeholder">
                  <span class="preview-placeholder-text">默认</span>
                </div>
              </div>
              <p class="preview-one-line" :title="previewStatusText(item)">
                {{ previewStatusText(item) }}
              </p>
            </div>

            <div class="promo-card__fields">
              <div class="field-row">
                <span class="field-label">视频</span>
                <div class="field-input-row">
                  <a-input
                    v-model="item.videoUrl"
                    placeholder="URL 或上传"
                    allow-clear
                    size="mini"
                  />
                  <a-upload
                    :show-file-list="false"
                    accept="video/mp4,video/webm"
                    :custom-request="(opt: any) => handleVideoUpload(opt, index)"
                  >
                    <template #upload-button>
                      <a-button
                        type="outline"
                        size="mini"
                        :loading="uploadingVideoIndex === index"
                        class="field-upload-btn"
                      >
                        <icon-upload />
                      </a-button>
                    </template>
                  </a-upload>
                </div>
              </div>
              <div class="field-row">
                <span class="field-label">图片</span>
                <div class="field-input-row">
                  <a-input
                    v-model="item.imageUrl"
                    placeholder="无视频时显示"
                    allow-clear
                    size="mini"
                  />
                  <a-upload
                    :show-file-list="false"
                    accept="image/jpeg,image/png,image/jpg,image/gif,image/webp"
                    :custom-request="(opt: any) => handleImageUpload(opt, index)"
                  >
                    <template #upload-button>
                      <a-button
                        type="outline"
                        size="mini"
                        :loading="uploadingImageIndex === index"
                        class="field-upload-btn"
                      >
                        <icon-upload />
                      </a-button>
                    </template>
                  </a-upload>
                </div>
              </div>

              <a-collapse
                class="promo-collapse promo-collapse--inline-head"
                :bordered="false"
              >
                <a-collapse-item :key="`layout-${index}`" header="布局微调">
                  <div class="layout-panel">
                    <div class="layout-nums">
                      <div class="layout-num-cell">
                        <span class="layout-num-label">宽</span>
                        <a-input-number
                          v-model="item.width"
                          :min="40"
                          size="mini"
                          class="layout-num-input"
                          hide-button
                        />
                      </div>
                      <div class="layout-num-cell">
                        <span class="layout-num-label">高</span>
                        <a-input-number
                          v-model="item.height"
                          :min="40"
                          size="mini"
                          class="layout-num-input"
                          hide-button
                        />
                      </div>
                      <div class="layout-num-cell">
                        <span class="layout-num-label">上距</span>
                        <a-input-number
                          v-model="item.marginTop"
                          size="mini"
                          class="layout-num-input"
                          hide-button
                        />
                      </div>
                      <div class="layout-num-cell">
                        <span class="layout-num-label">左距</span>
                        <a-input-number
                          v-model="item.marginLeft"
                          size="mini"
                          class="layout-num-input"
                          hide-button
                        />
                      </div>
                    </div>
                    <div class="layout-panel-divider" />
                    <div class="layout-line">
                      <span class="layout-line-label">背景</span>
                      <a-input
                        v-model="item.background"
                        size="mini"
                        placeholder="rgb(...)"
                      />
                    </div>
                    <div class="layout-line">
                      <span class="layout-line-label">位移</span>
                      <a-input
                        v-model="item.transform"
                        size="mini"
                        placeholder="translate(0, 0)"
                      />
                    </div>
                  </div>
                </a-collapse-item>
              </a-collapse>
            </div>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconUpload } from '@arco-design/web-vue/es/icon';
import {
  buildDefaultAdminLoginPromoDtoList,
  ADMIN_LOGIN_PROMO_SLOT_COUNT,
} from '@/constants/admin-login-promo-defaults';
import {
  editAdminLoginPromoConfig,
  uploadBatchFiles,
} from '@/api/system/config';

export interface AdminLoginPromoItem {
  enabled: boolean;
  videoUrl: string;
  imageUrl: string;
  width: number;
  height: number;
  marginTop: number;
  marginLeft: number;
  background: string;
  transform: string;
}

const props = defineProps<{
  initialData?: AdminLoginPromoItem[];
}>();

const emit = defineEmits<{
  (e: 'update'): void;
}>();

function normalizeItem(
  raw: any,
  fallback: AdminLoginPromoItem
): AdminLoginPromoItem {
  return {
    enabled: raw?.enabled !== false,
    videoUrl: raw?.videoUrl ?? '',
    imageUrl: raw?.imageUrl ?? '',
    width: typeof raw?.width === 'number' ? raw.width : fallback.width,
    height: typeof raw?.height === 'number' ? raw.height : fallback.height,
    marginTop:
      typeof raw?.marginTop === 'number' ? raw.marginTop : fallback.marginTop,
    marginLeft:
      typeof raw?.marginLeft === 'number'
        ? raw.marginLeft
        : fallback.marginLeft,
    background: raw?.background ?? fallback.background,
    transform: raw?.transform ?? fallback.transform,
  };
}

const defaults = buildDefaultAdminLoginPromoDtoList() as AdminLoginPromoItem[];

function buildListFromInitial(
  data?: AdminLoginPromoItem[]
): AdminLoginPromoItem[] {
  const base = defaults.map((d, i) => ({ ...d }));
  if (!data || !Array.isArray(data)) {
    return base;
  }
  return base.map((fallback, i) => normalizeItem(data[i], fallback));
}

const list = ref<AdminLoginPromoItem[]>(
  buildListFromInitial(props.initialData)
);
const saving = ref(false);
const uploadingVideoIndex = ref<number | null>(null);
const uploadingImageIndex = ref<number | null>(null);

function previewVideoUrl(item: AdminLoginPromoItem): string {
  return item.videoUrl?.trim() || '';
}

function previewImageUrl(item: AdminLoginPromoItem): string {
  return item.imageUrl?.trim() || '';
}

function previewStatusText(item: AdminLoginPromoItem): string {
  if (!item.enabled) {
    return '已关闭';
  }
  if (previewVideoUrl(item)) {
    return '优先视频';
  }
  if (previewImageUrl(item)) {
    return '仅图片';
  }
  return '内置视频';
}

function onPreviewImgError(e: Event) {
  const el = e.target as HTMLImageElement;
  if (el) {
    el.style.visibility = 'hidden';
  }
}

watch(
  () => props.initialData,
  (data) => {
    if (!data || !Array.isArray(data) || data.length === 0) return;
    list.value = buildListFromInitial(data);
  },
  { deep: true }
);

async function doUpload(
  file: File,
  index: number,
  field: 'videoUrl' | 'imageUrl',
  loadingRef: typeof uploadingVideoIndex
) {
  try {
    loadingRef.value = index;
    const res = await uploadBatchFiles([file]);
    const url = res?.data?.[0];
    let finalUrl = '';
    if (typeof url === 'string') {
      finalUrl = url;
    } else if (url && typeof url === 'object' && (url as any).url) {
      finalUrl = (url as any).url;
    }
    if (finalUrl) {
      list.value = list.value.map((it, i) =>
        i === index ? { ...it, [field]: finalUrl } : it
      );
      Message.success('上传成功');
    } else {
      Message.error('上传失败，未返回地址');
    }
  } catch {
    Message.error('上传失败');
  } finally {
    loadingRef.value = null;
  }
}

function handleVideoUpload(
  options: {
    fileItem: { file?: File };
    onSuccess?: () => void;
    onError?: (e: Error) => void;
  },
  index: number
) {
  const file = options.fileItem?.file;
  if (!file || !(file instanceof File)) {
    options.onError?.(new Error('未选择文件'));
    return;
  }
  const isVideo = file.type.startsWith('video/');
  if (!isVideo) {
    Message.error('请选择视频文件');
    options.onError?.(new Error('非视频'));
    return;
  }
  const isLt30M = file.size / 1024 / 1024 < 30;
  if (!isLt30M) {
    Message.error('视频建议不超过 30MB');
    options.onError?.(new Error('过大'));
    return;
  }
  doUpload(file, index, 'videoUrl', uploadingVideoIndex).then(() =>
    options.onSuccess?.()
  );
}

function handleImageUpload(
  options: {
    fileItem: { file?: File };
    onSuccess?: () => void;
    onError?: (e: Error) => void;
  },
  index: number
) {
  const file = options.fileItem?.file;
  if (!file || !(file instanceof File)) {
    options.onError?.(new Error('未选择文件'));
    return;
  }
  if (!file.type.startsWith('image/')) {
    Message.error('只能上传图片');
    options.onError?.(new Error('非图片'));
    return;
  }
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    Message.error('图片不能超过 5MB');
    options.onError?.(new Error('过大'));
    return;
  }
  doUpload(file, index, 'imageUrl', uploadingImageIndex).then(() =>
    options.onSuccess?.()
  );
}

async function handleSave() {
  const payload = list.value
    .slice(0, ADMIN_LOGIN_PROMO_SLOT_COUNT)
    .map((item) => ({
      enabled: item.enabled !== false,
      videoUrl: item.videoUrl?.trim() || '',
      imageUrl: item.imageUrl?.trim() || '',
      width: item.width,
      height: item.height,
      marginTop: item.marginTop,
      marginLeft: item.marginLeft,
      background: item.background || '',
      transform: item.transform || '',
    }));
  try {
    saving.value = true;
    const res = await editAdminLoginPromoConfig(payload);
    if (res.code === 200) {
      Message.success('登录页展示区配置已保存');
      emit('update');
    } else {
      Message.error((res as any).message || '保存失败');
    }
  } catch {
    Message.error('保存失败');
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.admin-login-promo {
  padding: 0 4px 8px;
}

.page-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin: 0 16px 20px;
}

.page-title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
}

.form-tip {
  margin: 0;
  color: var(--color-text-3);
  font-size: 12px;
  line-height: 1.5;
  max-width: 720px;
}

/* 大屏 4 列，中屏 3 列，小屏 2 列，手机 1 列 */
.promo-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin: 0 16px;
}

@media (max-width: 1680px) {
  .promo-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .promo-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .promo-grid {
    grid-template-columns: 1fr;
  }
}

.promo-item-block {
  min-width: 0;
}

.promo-card {
  height: 100%;
  border-radius: 10px;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
  overflow: hidden;
}

.promo-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border-color: var(--color-border-3);
}

.promo-card :deep(.arco-card-header) {
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border-2);
  min-height: 44px;
}

.promo-card :deep(.arco-card-header-title) {
  font-size: 13px;
  font-weight: 600;
}

.promo-card :deep(.arco-card-body) {
  padding: 12px;
}

.promo-card__title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.promo-card__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: rgb(var(--primary-6));
  background: rgba(var(--primary-6), 0.12);
}

.promo-card__switch-label {
  font-size: 12px;
  color: var(--color-text-3);
  margin-right: 6px;
}

.promo-card__body {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.promo-card__preview-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.preview-circle {
  width: 90px;
  height: 90px;
  margin-top: 10px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  border: 2px solid var(--color-border-2);
}

.preview-circle--disabled {
  opacity: 0.5;
  filter: grayscale(0.35);
}

.preview-media {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.preview-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-placeholder-text {
  font-size: 11px;
  color: var(--color-text-3);
}

.preview-one-line {
  margin: 0;
  font-size: 11px;
  color: var(--color-text-3);
  text-align: center;
  line-height: 1.3;
  max-width: 76px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.promo-card__fields {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-label {
  font-size: 12px;
  color: var(--color-text-2);
  font-weight: 500;
}

.field-input-row {
  display: flex;
  gap: 6px;
  align-items: center;
}

.field-input-row :deep(.arco-input-wrapper) {
  flex: 1;
  min-width: 0;
}

.field-upload-btn {
  flex-shrink: 0;
  padding: 0 8px;
}

.promo-collapse {
  margin-top: 2px;
}

.promo-collapse :deep(.arco-collapse-item-header) {
  padding: 6px 0 4px;
  font-size: 12px;
  color: var(--color-text-3);
  line-height: 1.2;
}

.promo-collapse :deep(.arco-collapse-item-content) {
  padding: 0;
}

.promo-collapse :deep(.arco-collapse-item-content-box) {
  padding: 0 0 4px;
}

/*
 * 默认 expand-icon-position=right 时 Arco 用「向左」的 caret 并绝对定位到行最右侧，观感差。
 * 使用默认 left（向右 caret），再通过 flex order 做成「布局微调 ▶」紧挨排列。
 */
.promo-collapse--inline-head :deep(.arco-collapse-item-header) {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  padding-left: 0 !important;
  padding-right: 0 !important;
}

.promo-collapse--inline-head :deep(.arco-collapse-item-header-left) {
  padding-left: 0 !important;
}

.promo-collapse--inline-head :deep(.arco-collapse-item-header-title) {
  order: 1;
  flex: 0 1 auto;
}

.promo-collapse--inline-head :deep(.arco-collapse-item-icon-hover) {
  position: static !important;
  left: auto !important;
  right: auto !important;
  top: auto !important;
  transform: none !important;
  order: 2;
  margin-left: 6px;
  flex-shrink: 0;
}

.promo-collapse--inline-head :deep(.arco-collapse-item-expand-icon) {
  transform: none;
}

.promo-collapse--inline-head
  :deep(.arco-collapse-item-active .arco-collapse-item-expand-icon) {
  transform: rotate(90deg);
}

.promo-collapse--inline-head :deep(.arco-collapse-item-content) {
  padding-left: 0 !important;
}

/* 布局微调：统一浅底面板，避免 Row/Col 与下方全宽行风格割裂 */
.layout-panel {
  padding: 8px 10px 8px;
  border-radius: 8px;
  background: var(--color-fill-2);
  border: 1px solid var(--color-border-2);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.layout-nums {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 6px 8px;
}

.layout-num-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.layout-num-label {
  font-size: 11px;
  color: var(--color-text-3);
  line-height: 1;
  white-space: nowrap;
}

.layout-num-input {
  width: 100%;
}

.layout-num-input :deep(.arco-input) {
  text-align: center;
}

.layout-panel-divider {
  height: 1px;
  background: var(--color-border-2);
  margin: 0;
  opacity: 0.85;
  flex-shrink: 0;
}

.layout-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.layout-line-label {
  flex: 0 0 34px;
  font-size: 11px;
  color: var(--color-text-3);
  line-height: 1.2;
}

.layout-line :deep(.arco-input-wrapper) {
  flex: 1;
  min-width: 0;
}
</style>
