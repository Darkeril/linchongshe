<!--
  Publish 发布页（Phase 7）
  设计稿来源：design/screens-part3.jsx PublishScreen

  布局：
    - 顶部栏：取消 / 发布笔记 / 存草稿
    - scroll-view：图片 3×3 网格、标题、正文、话题、选项列表
    - 底部固定发布区：快捷入口 + 立即发布
    - LoginSheet：直达本页后点击发布也能触发登录面板

  约束：
    - #4 scroll-view 显式 flex 高度
    - #5 页面根不加 transform，LoginSheet 依赖 fixed
    - #11 触摸事件统一 @tap
    - #12 新 class 使用 acs- 前缀
    - #13 导航走 navigateSafe / switchTab
    - #18 发布中状态 in-place 改 ref，不替换复杂 store
-->
<template>
  <view class="acs-publish">
    <view class="acs-publish__header">
      <view class="acs-publish__header-action" @tap="onCancel">
        <text class="acs-publish__cancel-text">取消</text>
      </view>
      <text class="acs-publish__header-title">发布笔记</text>
      <view class="acs-publish__header-action acs-publish__header-action--right" @tap="onSaveDraft">
        <text class="acs-publish__draft-text">存草稿</text>
      </view>
    </view>

    <scroll-view
      class="acs-publish__scroll"
      scroll-y
      :enhanced="true"
      :show-scrollbar="false"
    >
      <view class="acs-publish__image-grid">
        <view
          v-for="(img, index) in images"
          :key="`${img.seed}-${index}`"
          class="acs-publish__image-tile"
          @tap="onPreviewImage(index)"
        >
          <image
            class="acs-publish__image"
            :src="img.url"
            mode="aspectFill"
          />
          <view v-if="index === 0" class="acs-publish__cover-badge">
            <text class="acs-publish__cover-text">封面</text>
          </view>
          <view class="acs-publish__remove" @tap.stop="onRemoveImage(index)">
            <CloseIcon :size="14" color="#FFFFFF" />
          </view>
        </view>

        <view
          v-if="images.length < MAX_IMAGES"
          class="acs-publish__add-tile"
          @tap="onChooseImage"
        >
          <view class="acs-publish__add-icon">
            <PlusIcon :size="24" :color="primaryColor" />
          </view>
          <text class="acs-publish__add-text">添加 {{ images.length }}/{{ MAX_IMAGES }}</text>
        </view>
      </view>

      <view class="acs-publish__field acs-publish__field--title">
        <input
          v-model="title"
          class="acs-publish__title-input"
          type="text"
          maxlength="40"
          placeholder="填个有趣的标题，更容易被看见"
          :placeholder-style="placeholderStyle"
        />
      </view>

      <view class="acs-publish__field acs-publish__field--content">
        <textarea
          v-model="content"
          class="acs-publish__content-input"
          maxlength="500"
          auto-height
          placeholder="和大家分享今天发生的趣事..."
          :placeholder-style="placeholderStyle"
        />
      </view>

      <view class="acs-publish__topics">
        <view
          v-for="topic in topics"
          :key="topic"
          class="acs-publish__topic-chip"
          @tap="onRemoveTopic(topic)"
        >
          <text class="acs-publish__topic-text">#{{ topic }} ×</text>
        </view>
        <view class="acs-publish__topic-add" @tap="onAddTopic">
          <text class="acs-publish__topic-add-text">+ {{ topicAddLabel }}</text>
        </view>
      </view>

      <view class="acs-publish__options">
        <view
          v-for="(row, index) in optionRows"
          :key="row.action"
          class="acs-publish__option-row"
          :class="{ 'acs-publish__option-row--last': index === optionRows.length - 1 }"
          @tap="onOptionTap(row.action)"
        >
          <text class="acs-publish__option-icon">{{ row.icon }}</text>
          <text class="acs-publish__option-label">{{ row.label }}</text>
          <text
            class="acs-publish__option-value"
            :class="{ 'acs-publish__option-value--selected': row.value }"
          >
            {{ row.value || '未选择' }}
          </text>
          <text class="acs-publish__option-arrow">›</text>
        </view>
      </view>

      <view class="acs-publish__scroll-gap" />
    </scroll-view>

    <view class="acs-publish__bottom">
      <view class="acs-publish__toolbar">
        <view
          v-for="tool in toolbarItems"
          :key="tool.action"
          class="acs-publish__tool"
          @tap="onToolbarTap(tool.action)"
        >
          <text class="acs-publish__tool-text">{{ tool.label }}</text>
        </view>
      </view>
      <button
        class="acs-publish__submit"
        :class="{ 'acs-publish__submit--disabled': !canPublish || publishing }"
        :disabled="publishing"
        hover-class="acs-publish__submit--hover"
        :hover-stay-time="120"
        @tap="onPublish"
      >
        {{ publishing ? '发布中...' : '立即发布 🐾' }}
      </button>
    </view>

    <LoginSheet />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import LoginSheet from '@/components/ui/LoginSheet.vue';
import CloseIcon from '@/components/ui/icons/CloseIcon.vue';
import PlusIcon from '@/components/ui/icons/PlusIcon.vue';
import { publishNote } from '@/api/services/note';
import { useAuthGuard } from '@/composables/useAuthGuard';
import { navigateSafe, switchTab } from '@/utils/navigate';
import { COLOR } from '@/styles/tokens';
import type { PetVariant, PublishImageItem, PublishNotePayload } from '@/types/note';

const MAX_IMAGES = 9;
const DRAFT_KEY = 'acs_publish_draft_v1';
const DEFAULT_TOPICS = ['金毛日记', '洗澡日常'];
const TOPIC_POOL = ['金毛日记', '洗澡日常', '毛绒绒', '宠物食谱', '出行攻略', '新手养宠'];
const VARIANTS: PetVariant[] = ['dog', 'cat', 'mochi', 'puppy'];

type OptionAction = 'location' | 'pet' | 'friend' | 'product';
type ToolbarAction = 'emoji' | 'topic' | 'friend' | 'location';

interface PublishDraft {
  title: string;
  content: string;
  topics: string[];
  images: PublishImageItem[];
}

const { requireAuth } = useAuthGuard();
const primaryColor = COLOR.primary;
const placeholderStyle = `color: ${COLOR.inkFaint}; font-size: 14px;`;

const images = ref<PublishImageItem[]>([]);
const title = ref<string>('');
const content = ref<string>('');
const topics = ref<string[]>([...DEFAULT_TOPICS]);
const publishing = ref<boolean>(false);
const selectedLocation = ref<string>('上海 · 徐汇区');
const selectedPet = ref<string>('汤圆 · 金毛');
const selectedFriend = ref<string>('');
const selectedProduct = ref<string>('');

const optionRows = computed<Array<{ action: OptionAction; icon: string; label: string; value: string }>>(() => [
  { action: 'location', icon: '📍', label: '所在位置', value: selectedLocation.value },
  { action: 'pet', icon: '🐕', label: '添加我的宠物', value: selectedPet.value },
  { action: 'friend', icon: '@', label: '@好友提醒', value: selectedFriend.value },
  { action: 'product', icon: '🛍', label: '关联商品', value: selectedProduct.value },
]);

const toolbarItems: Array<{ action: ToolbarAction; label: string }> = [
  { action: 'emoji', label: '😊 表情' },
  { action: 'topic', label: '# 话题' },
  { action: 'friend', label: '@ 好友' },
  { action: 'location', label: '📍 位置' },
];

const topicAddLabel = computed<string>(() => {
  const next = TOPIC_POOL.find((t) => !topics.value.includes(t));
  return next ? '添加话题' : '话题已满';
});

const canPublish = computed<boolean>(() =>
  images.value.length > 0 &&
  title.value.trim().length > 0 &&
  content.value.trim().length > 0,
);

onLoad(() => {
  restoreDraft();
});

function variantFor(index: number): PetVariant {
  return VARIANTS[index % VARIANTS.length]!;
}

function makeDraft(): PublishDraft {
  return {
    title: title.value,
    content: content.value,
    topics: [...topics.value],
    images: images.value.map((img) => ({ ...img })),
  };
}

function hasDraftContent(): boolean {
  return (
    images.value.length > 0 ||
    title.value.trim().length > 0 ||
    content.value.trim().length > 0
  );
}

function restoreDraft(): void {
  try {
    const raw = uni.getStorageSync(DRAFT_KEY) as Partial<PublishDraft> | '';
    if (!raw || typeof raw !== 'object') return;
    title.value = typeof raw.title === 'string' ? raw.title : '';
    content.value = typeof raw.content === 'string' ? raw.content : '';
    topics.value = Array.isArray(raw.topics) && raw.topics.length > 0
      ? raw.topics.filter((t): t is string => typeof t === 'string')
      : [...DEFAULT_TOPICS];
    images.value = Array.isArray(raw.images)
      ? raw.images.filter((img): img is PublishImageItem =>
        typeof img?.url === 'string' &&
        typeof img?.variant === 'string' &&
        (typeof img?.seed === 'string' || typeof img?.seed === 'number'),
      ).slice(0, MAX_IMAGES)
      : [];
  } catch (err) {
    console.warn('[publish] restore draft failed:', err);
  }
}

function clearDraft(): void {
  try {
    uni.removeStorageSync(DRAFT_KEY);
  } catch (err) {
    console.warn('[publish] clear draft failed:', err);
  }
}

function onChooseImage(): void {
  const remain = MAX_IMAGES - images.value.length;
  if (remain <= 0) {
    uni.showToast({ title: '最多添加 9 张图片', icon: 'none' });
    return;
  }
  uni.chooseImage({
    count: remain,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const paths = Array.isArray(res.tempFilePaths) ? res.tempFilePaths : [];
      const start = images.value.length;
      paths.slice(0, remain).forEach((path, i) => {
        images.value.push({
          url: path,
          variant: variantFor(start + i),
          seed: `publish-${Date.now()}-${start + i}`,
        });
      });
    },
    fail: (err) => {
      const message = (err as { errMsg?: string })?.errMsg ?? '';
      if (message.toLowerCase().includes('cancel')) return;
      uni.showToast({ title: '选择图片失败', icon: 'none' });
    },
  });
}

function onPreviewImage(index: number): void {
  const urls = images.value.map((img) => img.url);
  if (!urls[index]) return;
  uni.previewImage({ current: urls[index], urls });
}

function onRemoveImage(index: number): void {
  images.value.splice(index, 1);
}

function onAddTopic(): void {
  const next = TOPIC_POOL.find((t) => !topics.value.includes(t));
  if (!next) {
    uni.showToast({ title: '话题已满', icon: 'none' });
    return;
  }
  topics.value.push(next);
}

function onRemoveTopic(topic: string): void {
  const idx = topics.value.indexOf(topic);
  if (idx >= 0) topics.value.splice(idx, 1);
}

function onOptionTap(action: OptionAction): void {
  if (action === 'location') {
    selectedLocation.value = selectedLocation.value ? '' : '上海 · 徐汇区';
    return;
  }
  if (action === 'pet') {
    selectedPet.value = selectedPet.value ? '' : '汤圆 · 金毛';
    return;
  }
  if (action === 'friend') {
    selectedFriend.value = selectedFriend.value ? '' : '奶油小糕';
    return;
  }
  selectedProduct.value = selectedProduct.value ? '' : '低敏沐浴露';
}

function onToolbarTap(action: ToolbarAction): void {
  if (action === 'topic') {
    onAddTopic();
    return;
  }
  if (action === 'location') {
    onOptionTap('location');
    return;
  }
  if (action === 'friend') {
    onOptionTap('friend');
    return;
  }
  uni.showToast({ title: '表情面板待接入', icon: 'none' });
}

function onSaveDraft(): void {
  if (!hasDraftContent()) {
    uni.showToast({ title: '还没有可保存的内容', icon: 'none' });
    return;
  }
  try {
    uni.setStorageSync(DRAFT_KEY, makeDraft());
    uni.showToast({ title: '已保存草稿', icon: 'success' });
  } catch (err) {
    uni.showToast({
      title: err instanceof Error ? err.message : '保存草稿失败',
      icon: 'none',
    });
  }
}

function validatePublish(): string {
  if (images.value.length === 0) return '请先添加图片';
  if (!title.value.trim()) return '请填写标题';
  if (!content.value.trim()) return '请填写正文';
  return '';
}

function buildPayload(): PublishNotePayload {
  return {
    title: title.value.trim(),
    content: content.value.trim(),
    topics: topics.value,
    images: images.value,
    location: selectedLocation.value,
    petName: selectedPet.value,
    mentionedUsers: selectedFriend.value ? [selectedFriend.value] : [],
    relatedProducts: selectedProduct.value ? [selectedProduct.value] : [],
  };
}

async function wait(ms: number): Promise<void> {
  await new Promise<void>((resolve) => {
    setTimeout(resolve, ms);
  });
}

async function onPublish(): Promise<void> {
  if (publishing.value) return;
  const message = validatePublish();
  if (message) {
    uni.showToast({ title: message, icon: 'none' });
    return;
  }

  try {
    await requireAuth(async () => {
      publishing.value = true;
      try {
        const result = await publishNote(buildPayload());
        clearDraft();
        uni.showToast({ title: '发布成功', icon: 'success' });
        await wait(450);
        await navigateSafe({
          url: `/pages/note-detail/note-detail?id=${encodeURIComponent(result.id)}`,
          replace: true,
        });
      } finally {
        publishing.value = false;
      }
    });
  } catch (err) {
    publishing.value = false;
    const code = (err as { code?: string })?.code;
    if (code === 'AUTH_CANCELLED' || code === 'AUTH_REPLACED') return;
    uni.showToast({
      title: err instanceof Error ? err.message : '发布失败',
      icon: 'none',
    });
  }
}

function leavePage(): void {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
  } else {
    void switchTab('/pages/home/home');
  }
}

function onCancel(): void {
  if (!hasDraftContent()) {
    leavePage();
    return;
  }
  uni.showModal({
    title: '放弃编辑？',
    content: '当前内容还没有发布，离开后可先存草稿。',
    confirmText: '放弃',
    cancelText: '继续编辑',
    confirmColor: COLOR.warn,
    success: (res) => {
      if (res.confirm) leavePage();
    },
  });
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-publish {
  position: relative;
  width: 100%;
  height: 100vh;
  background: $color-bg;
  font-family: $font-family-body;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &__header {
    flex-shrink: 0;
    height: 100rpx;
    padding: calc(36rpx + env(safe-area-inset-top)) 32rpx 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-sizing: content-box;
  }

  &__header-action {
    min-width: 112rpx;
    height: 72rpx;
    display: flex;
    align-items: center;

    &--right {
      justify-content: flex-end;
    }

    &:active {
      opacity: 0.68;
    }
  }

  &__cancel-text {
    font-size: $font-size-xl;
    color: $color-ink-soft;
    line-height: 1;
  }

  &__draft-text {
    font-size: $font-size-md;
    color: $color-primary;
    font-weight: $font-weight-semibold;
    line-height: 1;
  }

  &__header-title {
    font-size: $font-size-xl;
    color: $color-ink;
    font-weight: $font-weight-bold;
    line-height: 1;
  }

  &__scroll {
    flex: 1;
    height: 0;
    min-height: 0;
  }

  &__image-grid {
    padding: 4rpx 32rpx 0;
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
  }

  &__image-tile,
  &__add-tile {
    position: relative;
    width: calc((100vw - 88rpx) / 3);
    height: calc((100vw - 88rpx) / 3);
    border-radius: 22rpx;
    overflow: hidden;
    box-sizing: border-box;
  }

  &__image {
    width: 100%;
    height: 100%;
    display: block;
  }

  &__cover-badge {
    position: absolute;
    top: 8rpx;
    left: 8rpx;
    padding: 4rpx 12rpx;
    border-radius: 12rpx;
    background: $color-primary;
    line-height: 1;
  }

  &__cover-text {
    font-size: 18rpx;
    color: #ffffff;
    font-weight: $font-weight-bold;
    line-height: 1;
  }

  &__remove {
    position: absolute;
    top: 8rpx;
    right: 8rpx;
    width: 36rpx;
    height: 36rpx;
    border-radius: 18rpx;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;

    &:active {
      opacity: 0.75;
    }
  }

  &__add-tile {
    background: $color-surface;
    border: 3rpx dashed $color-primary-soft;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10rpx;

    &:active {
      opacity: 0.72;
    }
  }

  &__add-icon {
    width: 56rpx;
    height: 56rpx;
    border-radius: 28rpx;
    background: rgba($color-primary-soft, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__add-text {
    font-size: $font-size-xs;
    color: $color-primary;
    font-weight: $font-weight-semibold;
    line-height: 1;
  }

  &__field {
    padding-left: 36rpx;
    padding-right: 36rpx;

    &--title {
      padding-top: 36rpx;
    }

    &--content {
      padding-top: 20rpx;
    }
  }

  &__title-input {
    width: 100%;
    height: 58rpx;
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    color: $color-ink;
    line-height: $line-height-tight;
  }

  &__content-input {
    width: 100%;
    min-height: 150rpx;
    font-size: $font-size-lg;
    color: $color-ink;
    line-height: $line-height-relaxed;
  }

  &__topics {
    padding: 16rpx 36rpx 0;
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
  }

  &__topic-chip,
  &__topic-add {
    padding: 8rpx 20rpx;
    border-radius: 24rpx;
    line-height: 1;
  }

  &__topic-chip {
    background: $color-primary-soft;

    &:active {
      opacity: 0.72;
    }
  }

  &__topic-text {
    font-size: $font-size-sm;
    color: $color-primary-ink;
    font-weight: $font-weight-semibold;
    line-height: 1;
  }

  &__topic-add {
    background: $color-surface;
    border: 2rpx dashed $color-ink-faint;
    box-sizing: border-box;

    &:active {
      opacity: 0.72;
    }
  }

  &__topic-add-text {
    font-size: $font-size-sm;
    color: $color-ink-soft;
    font-weight: $font-weight-medium;
    line-height: 1;
  }

  &__options {
    margin: 36rpx 28rpx 0;
    background: $color-surface;
    border-radius: $radius-md;
    overflow: hidden;
    box-shadow: $shadow-sm;
  }

  &__option-row {
    height: 92rpx;
    padding: 0 28rpx;
    display: flex;
    align-items: center;
    border-bottom: 1rpx solid $color-divider;

    &--last {
      border-bottom: none;
    }

    &:active {
      background: rgba($color-primary-soft, 0.22);
    }
  }

  &__option-icon {
    width: 42rpx;
    font-size: $font-size-xl;
    color: $color-primary;
    line-height: 1;
  }

  &__option-label {
    font-size: $font-size-md;
    color: $color-ink;
    font-weight: $font-weight-medium;
    line-height: 1;
  }

  &__option-value {
    margin-left: auto;
    max-width: 240rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: $font-size-base;
    color: $color-ink-faint;
    line-height: 1;

    &--selected {
      color: $color-primary;
    }
  }

  &__option-arrow {
    margin-left: 12rpx;
    font-size: 40rpx;
    color: $color-ink-faint;
    line-height: 1;
  }

  &__scroll-gap {
    height: 34rpx;
  }

  &__bottom {
    flex-shrink: 0;
    padding: 24rpx 32rpx calc(44rpx + env(safe-area-inset-bottom));
    background: $color-surface;
    border-top: 1rpx solid $color-divider;
    box-shadow: 0 -8rpx 28rpx rgba(80, 50, 30, 0.06);
  }

  &__toolbar {
    display: flex;
    gap: 20rpx;
    margin-bottom: 20rpx;
  }

  &__tool {
    flex: 1;
    height: 64rpx;
    border-radius: 20rpx;
    background: $color-surface-dim;
    display: flex;
    align-items: center;
    justify-content: center;

    &:active {
      opacity: 0.72;
    }
  }

  &__tool-text {
    font-size: $font-size-sm;
    color: $color-ink-soft;
    line-height: 1;
    white-space: nowrap;
  }

  &__submit {
    width: 100%;
    height: 92rpx;
    border: none;
    border-radius: $radius-lg;
    background: linear-gradient(135deg, $color-primary, $color-primary-ink);
    color: #ffffff;
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    line-height: 92rpx;
    box-shadow: 0 12rpx 36rpx rgba($color-primary, 0.33);
    font-family: $font-family-body;
    padding: 0;

    &::after {
      border: none;
    }

    &--hover {
      opacity: 0.92;
      transform: scale(0.98);
    }

    &--disabled {
      opacity: 0.52;
      box-shadow: none;
    }
  }
}
</style>
