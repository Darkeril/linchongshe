<template>
  <div class="carousel-manage">
    <div class="page-head">
      <div class="page-head-text">
        <h4 class="page-title">{{ pageTitle }}</h4>
        <p class="form-tip">
          {{ pageTip }}
        </p>
      </div>
      <div class="page-head-actions">
        <a-button type="primary" @click="handleAdd">
          <icon-plus />
          新增
        </a-button>
        <a-button type="primary" status="success" :loading="saving" @click="handleSave">
          保存配置
        </a-button>
      </div>
    </div>

    <div class="carousel-grid">
      <div v-for="(item, index) in list" :key="index" class="carousel-item-block">
        <a-card class="carousel-card" :bordered="true">
          <template #title>
            <span class="carousel-card__title">
              <span class="carousel-card__badge">{{ index + 1 }}</span>
              轮播图
            </span>
          </template>
          <template #extra>
            <a-button type="text" size="small" status="danger" @click="handleDelete(index)">
              删除
            </a-button>
          </template>

          <div class="carousel-card__body">
            <div
              class="carousel-card__preview"
              :class="{ 'carousel-card__preview--empty': !item.url?.trim() }"
            >
              <img
                v-if="item.url?.trim()"
                :key="item.url"
                :src="item.url"
                alt=""
                class="carousel-card__preview-img"
                @error="onPreviewError"
              />
              <div v-else class="carousel-card__preview-placeholder">
                <icon-image class="carousel-card__preview-icon" />
                <span>上传或填写图片地址</span>
              </div>
            </div>

            <div class="carousel-card__fields">
              <div class="field-row">
                <span class="field-label">图片地址</span>
                <div class="field-input-row">
                  <a-input
                    v-model="item.url"
                    placeholder="https://"
                    allow-clear
                    size="small"
                  />
                  <a-upload
                    :show-file-list="false"
                    accept="image/jpeg,image/png,image/jpg,image/gif,image/webp"
                    :custom-request="(opt: any) => handleImageUpload(opt, index)"
                  >
                    <template #upload-button>
                      <a-button
                        type="outline"
                        size="small"
                        :loading="uploadingIndex === index"
                        class="field-upload-btn"
                      >
                        <icon-upload />
                      </a-button>
                    </template>
                  </a-upload>
                </div>
              </div>

              <div class="field-row field-row--pair">
                <div class="field-col">
                  <span class="field-label">排序</span>
                  <a-input-number
                    v-model="item.sortOrder"
                    :min="0"
                    :step="1"
                    size="small"
                    placeholder="0"
                    class="field-sort"
                  />
                </div>
                <div class="field-col field-col--grow">
                  <span class="field-label">标题（选填）</span>
                  <a-input
                    v-model="item.title"
                    placeholder="展示用标题"
                    allow-clear
                    size="small"
                  />
                </div>
              </div>

              <div class="field-row">
                <span class="field-label">跳转链接</span>
                <a-input
                  v-model="item.link"
                  placeholder="选填，如活动页或商品页"
                  allow-clear
                  size="small"
                />
              </div>
            </div>
          </div>
        </a-card>
      </div>
    </div>

    <div v-if="list.length === 0 && !loading" class="carousel-empty">
      <a-empty description="暂无轮播项，点击「新增」添加" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus, IconUpload, IconImage } from '@arco-design/web-vue/es/icon';
import {
  editCarouselConfig,
  editWorkbenchCarouselConfig,
  uploadBatchFiles,
} from '@/api/system/config';

interface CarouselItem {
  url: string;
  link?: string;
  title?: string;
  sortOrder?: number;
}

const props = withDefaults(
  defineProps<{
    initialData?: CarouselItem[];
    /** mall：APP 市集；workbench：管理端工作台右侧 */
    configScope?: 'mall' | 'workbench';
  }>(),
  { configScope: 'mall' }
);

const pageTitle = computed(() =>
  props.configScope === 'workbench' ? '工作台轮播图' : '市集页轮播图'
);

const pageTip = computed(() =>
  props.configScope === 'workbench'
    ? '用于管理端「工作台」页面右侧轮播区域；支持图片地址、跳转链接与排序。保存后刷新工作台即可生效。'
    : '用于 uniapp 市集（商城）页顶部轮播；支持图片地址、跳转链接与排序。保存后客户端拉取配置生效。'
);

const emit = defineEmits<{
  (e: 'update'): void;
}>();

const list = ref<CarouselItem[]>([]);
const loading = ref(false);
const saving = ref(false);

function normalizeItem(item: any, index: number): CarouselItem {
  return {
    url: item?.url ?? '',
    link: item?.link ?? '',
    title: item?.title ?? '',
    sortOrder: item?.sortOrder ?? index,
  };
}

if (props.initialData && Array.isArray(props.initialData) && props.initialData.length > 0) {
  list.value = props.initialData.map((item, i) => normalizeItem(item, i));
}

watch(
  () => props.initialData,
  (data) => {
    if (!data || !Array.isArray(data)) return;
    const next = data.map((item, i) => normalizeItem(item, i));
    if (next.length > 0 || list.value.length === 0) {
      list.value = next;
    }
  },
  { deep: true }
);

function handleAdd() {
  list.value = [
    ...list.value,
    {
      url: '',
      link: '',
      title: '',
      sortOrder: list.value.length,
    },
  ];
}

function handleDelete(index: number) {
  list.value = list.value.filter((_, i) => i !== index);
}

function onPreviewError(e: Event) {
  const el = e.target as HTMLImageElement;
  if (el) {
    el.classList.add('carousel-card__preview-img--broken');
  }
}

const uploadingIndex = ref<number | null>(null);

async function handleImageUpload(
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
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    Message.error('只能上传图片文件（JPG、PNG、GIF、WebP）');
    options.onError?.(new Error('非图片类型'));
    return;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    Message.error('图片大小不能超过 2MB');
    options.onError?.(new Error('文件过大'));
    return;
  }
  try {
    uploadingIndex.value = index;
    const res = await uploadBatchFiles([file]);
    const url = res?.data?.[0];
    let finalUrl = '';
    if (typeof url === 'string') {
      finalUrl = url;
    } else if (url && typeof url === 'object' && (url as any).url) {
      finalUrl = (url as any).url;
    }
    if (finalUrl) {
      list.value = list.value.map((it, i) => (i === index ? { ...it, url: finalUrl } : it));
      Message.success('图片上传成功');
      options.onSuccess?.();
    } else {
      Message.error('上传失败，未返回图片地址');
      options.onError?.(new Error('No URL returned'));
    }
  } catch (e) {
    Message.error('上传失败');
    options.onError?.(e instanceof Error ? e : new Error('Upload failed'));
  } finally {
    uploadingIndex.value = null;
  }
}

async function handleSave() {
  const sorted = [...list.value]
    .filter((item) => item.url && item.url.trim())
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
    .map((item) => ({
      url: item.url.trim(),
      link: item.link?.trim() || '',
      title: item.title?.trim() || '',
      sortOrder: item.sortOrder ?? 0,
    }));
  if (sorted.length === 0 && list.value.length > 0) {
    Message.warning('请至少填写一项有效的图片地址');
    return;
  }
  try {
    saving.value = true;
    const saveFn =
      props.configScope === 'workbench'
        ? editWorkbenchCarouselConfig
        : editCarouselConfig;
    const res = await saveFn(sorted);
    if (res.code === 200) {
      Message.success(
        props.configScope === 'workbench'
          ? '工作台轮播图已保存'
          : '轮播图配置已保存'
      );
      list.value = sorted;
      emit('update');
    } else {
      Message.error((res as any).message || '保存失败');
    }
  } catch (e) {
    Message.error('保存失败');
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.carousel-manage {
  padding: 0 4px 16px;
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

.page-head-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex-shrink: 0;
}

.carousel-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin: 0 16px;
}

@media (max-width: 1400px) {
  .carousel-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .carousel-grid {
    grid-template-columns: 1fr;
  }
}

.carousel-item-block {
  min-width: 0;
}

.carousel-card {
  height: 100%;
  border-radius: 10px;
  overflow: hidden;
  transition: box-shadow 0.2s ease;
}

.carousel-card:hover {
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
}

.carousel-card :deep(.arco-card-header) {
  border-bottom: 1px solid var(--color-border-2);
  padding: 10px 14px;
  min-height: 44px;
}

.carousel-card :deep(.arco-card-body) {
  padding: 0;
}

.carousel-card__title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-1);
}

.carousel-card__badge {
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
  background: color-mix(in srgb, rgb(var(--primary-6)) 12%, transparent);
}

.carousel-card__body {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.carousel-card__preview {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: var(--color-fill-2);
  border-bottom: 1px solid var(--color-border-2);
  overflow: hidden;
}

.carousel-card__preview--empty {
  background: linear-gradient(
    145deg,
    var(--color-fill-2) 0%,
    var(--color-fill-3) 100%
  );
}

.carousel-card__preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.carousel-card__preview-img--broken {
  object-fit: contain;
  opacity: 0.4;
  padding: 12px;
}

.carousel-card__preview-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--color-text-3);
  font-size: 12px;
  padding: 16px;
  text-align: center;
}

.carousel-card__preview-icon {
  font-size: 28px;
  opacity: 0.45;
}

.carousel-card__fields {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.field-row--pair {
  flex-direction: row;
  gap: 12px;
  align-items: flex-end;
}

.field-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.field-col--grow {
  flex: 1;
}

.field-label {
  font-size: 12px;
  color: var(--color-text-2);
  font-weight: 500;
}

.field-input-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.field-input-row :deep(.arco-input-wrapper) {
  flex: 1;
  min-width: 0;
}

.field-upload-btn {
  flex-shrink: 0;
  padding: 0 10px;
}

.field-sort {
  width: 100%;
  max-width: 120px;
}

.carousel-empty {
  margin: 32px 16px;
  padding: 24px;
  border-radius: 10px;
  background: var(--color-fill-1);
  border: 1px dashed var(--color-border-2);
}
</style>
