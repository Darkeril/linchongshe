<!--
  NoteCard — 瀑布流笔记卡片
  设计稿来源：design/screens-part2.jsx 行 31-68
  结构：
    - 封面 PetPlaceholder
      - 可选标签（毛玻璃白 85% + blur 8px，左上角）
    - 信息区（padding 8 10 10 → 16 20 20 rpx）
      - 标题：2 行截断 + $font-size-base (12px) + ink
      - 底部行：左作者 PetAvatar 18 + 昵称；右 HeartIcon 12 + 点赞数
        - 点赞数 > 999 显示 N.Nk，字体 Baloo 2（$font-family-num）
  - 所有颜色 / 字号 / 阴影走 tokens
  - class 加 acs- 前缀（gotchas #12）
  - @tap + :active CSS 按压反馈（gotchas #11，不用 hover-class）
-->
<template>
  <view class="acs-note-card" @tap="onCardTap">
    <!-- 封面 -->
    <view class="acs-note-card__cover">
      <PetPlaceholder
        :variant="note.variant"
        :seed="note.id"
        :w="coverW"
        :h="note.h"
      />
      <!-- 标签（可选） -->
      <view v-if="note.tag" class="acs-note-card__tag">
        <text class="acs-note-card__tag-text">#{{ note.tag }}</text>
      </view>
    </view>

    <!-- 信息区 -->
    <view class="acs-note-card__body">
      <text class="acs-note-card__title">{{ note.title }}</text>

      <view class="acs-note-card__bottom">
        <!-- 左：作者 -->
        <view class="acs-note-card__user">
          <PetAvatar :variant="note.avatarVariant" :size="18" />
          <text class="acs-note-card__user-name">{{ note.user }}</text>
        </view>
        <!-- 右：点赞 -->
        <view class="acs-note-card__like" @tap.stop="onLikeTap">
          <HeartIcon
            :size="12"
            :color="note.liked ? warnColor : inkMutedColor"
            :filled="note.liked"
          />
          <text
            class="acs-note-card__like-count"
            :class="{ 'acs-note-card__like-count--liked': note.liked }"
          >
            {{ displayLikes }}
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import PetPlaceholder from '@/components/ui/PetPlaceholder.vue';
import PetAvatar from '@/components/ui/PetAvatar.vue';
import HeartIcon from '@/components/ui/icons/HeartIcon.vue';
import { COLOR } from '@/styles/tokens';
import type { NoteListItem } from '@/types/note';

interface Props {
  note: NoteListItem;
  /** 卡片宽度（px），WaterFall 根据容器宽和列数算出后传入 */
  coverW?: number;
}

const props = withDefaults(defineProps<Props>(), {
  coverW: 180,
});

const emit = defineEmits<{
  (e: 'tap', note: NoteListItem): void;
  (e: 'like', note: NoteListItem): void;
}>();

const warnColor = COLOR.warn;
const inkMutedColor = COLOR.inkMuted;

/** 点赞数 > 999 显示 N.Nk，其他原样 */
const displayLikes = computed<string>(() => {
  const v = props.note.likes;
  if (v > 999) {
    return (v / 1000).toFixed(1) + 'k';
  }
  return String(v);
});

function onCardTap(): void {
  emit('tap', props.note);
}

function onLikeTap(): void {
  emit('like', props.note);
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-note-card {
  background: $color-surface;
  border-radius: $radius-md; // 16px
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(80, 50, 30, 0.06);
  // 设计稿 margin-bottom: 10 → 20rpx；但瀑布流绝对定位，marginBottom 不起作用
  // 所以位移由 WaterFall 的 top 计算负责
  transition: transform 0.12s ease;

  // Phase 4 视觉反馈修正：
  // 整卡去掉 :active 缩放 —— 否则点心形时 pointer 冒泡到父 .acs-note-card 也会触发整卡 scale。
  // @tap.stop 只阻止 JS 事件冒泡，无法阻止 CSS :active 伪类冒泡（浏览器原生行为）。
  // 心形自己有 :active opacity 反馈（见 &__like），足够让用户感知点击。

  // ── 封面 ────────────────────────────────────────────
  &__cover {
    position: relative;
    width: 100%;
    line-height: 0;
    font-size: 0; // 避免 inline svg 下有微小空隙
  }

  // 标签：毛玻璃白 85% + blur 8px
  // 设计稿 top/left 8 → 16rpx，padding 3 8 → 6 16 rpx，radius 8 → 16rpx
  &__tag {
    position: absolute;
    top: 16rpx;
    left: 16rpx;
    padding: 6rpx 16rpx;
    border-radius: 16rpx;
    background: rgba(255, 255, 255, 0.85);
    // 小程序端 backdrop-filter 支持差，保底用半透明白色；H5 / App-vue 生效
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    line-height: 1;
  }

  &__tag-text {
    font-size: $font-size-xs; // 10px
    font-weight: $font-weight-semibold;
    color: $color-primary-ink;
    line-height: 1;
    white-space: nowrap;
  }

  // ── 信息区 ──────────────────────────────────────────
  // 设计稿 padding 8 10 10 → 16 20 20 rpx
  &__body {
    padding: 16rpx 20rpx 20rpx;
  }

  // 标题：$font-size-base / 500 / ink / line-height 1.4 / 2 行截断
  &__title {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: $font-size-base; // 12px
    font-weight: $font-weight-medium;
    color: $color-ink;
    line-height: $line-height-normal; // 1.4
    margin-bottom: 16rpx;
    // 允许文字换行
    white-space: normal;
    word-break: break-all;
  }

  &__bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8rpx;
  }

  // 作者
  &__user {
    display: flex;
    align-items: center;
    gap: 10rpx;
    flex: 1;
    min-width: 0;
  }

  &__user-name {
    font-size: $font-size-xs; // 10px
    color: $color-ink-muted;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
    min-width: 0;
  }

  // 点赞
  &__like {
    display: flex;
    align-items: center;
    gap: 6rpx;
    flex-shrink: 0;
    padding: 4rpx 2rpx; // 扩大点击热区
    // 点击反馈
    &:active {
      opacity: 0.65;
    }
  }

  &__like-count {
    font-size: $font-size-xs; // 10px
    font-weight: $font-weight-medium;
    color: $color-ink-muted;
    font-family: $font-family-num; // Baloo 2
    line-height: 1;

    &--liked {
      color: $color-warn;
    }
  }
}
</style>
