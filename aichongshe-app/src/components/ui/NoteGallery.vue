<!--
  NoteGallery — 笔记详情图文轮播
  设计稿来源：design/screens-part2.jsx L214-235
  - swiper 横滑，height 360（iPhone 13 Pro 视口近似）
  - 右下页码 `1 / N` 胶囊：rgba(0,0,0,0.45) 背景 + 白字 11/600 / font-family num
  - 底部居中指示点：active 16×5 圆角（白色）/ inactive 5×5 圆（白 55%）
  - 每张图用 PetPlaceholder 占位（复用 Phase 4 组件）
  - gotchas #16：swiper 必须显式高度
  - class 加 acs- 前缀（gotchas #12）
-->
<template>
  <view class="acs-gallery">
    <swiper
      class="acs-gallery__swiper"
      :current="currentIndex"
      :duration="260"
      :indicator-dots="false"
      @change="onSwiperChange"
    >
      <swiper-item
        v-for="(img, idx) in images"
        :key="`${img.seed}-${idx}`"
        class="acs-gallery__item"
      >
        <image
          v-if="img.url"
          class="acs-gallery__image"
          :src="img.url"
          mode="aspectFill"
        />
        <PetPlaceholder
          v-else
          :variant="img.variant"
          :seed="img.seed"
          :w="galleryW"
          :h="galleryH"
        />
      </swiper-item>
    </swiper>

    <!-- 右下页码胶囊 -->
    <view v-if="images.length > 1" class="acs-gallery__pager">
      <text class="acs-gallery__pager-text">{{ currentIndex + 1 }} / {{ images.length }}</text>
    </view>

    <!-- 底部指示点 -->
    <view v-if="images.length > 1" class="acs-gallery__dots">
      <view
        v-for="(_, i) in images"
        :key="i"
        class="acs-gallery__dot"
        :class="{ 'acs-gallery__dot--active': i === currentIndex }"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import PetPlaceholder from '@/components/ui/PetPlaceholder.vue';
import type { NoteImage } from '@/types/note';

interface Props {
  images: NoteImage[];
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'change', index: number): void;
}>();

/** 内部维护当前索引 */
const currentIndex = ref<number>(0);

// 画布宽高：设计稿 390×360（iPhone 13 Pro 近似视口）
// PetPlaceholder 接收 px 值，SVG 内部自适应；宽度 100% 通过容器拉伸
// 这里给一个固定像素值即可（PetPlaceholder 内部按 viewBox 渲染，再被父容器 width:100% 拉伸到屏宽）
const galleryW = 390;
const galleryH = 360;

function onSwiperChange(e: { detail: { current: number } }): void {
  const idx = e.detail?.current ?? 0;
  if (idx >= 0 && idx < props.images.length) {
    currentIndex.value = idx;
    emit('change', idx);
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-gallery {
  position: relative;
  width: 100%;
  // 设计稿 360px → 720rpx（1px = 2rpx）
  height: 720rpx;
  background: $color-surface-dim;
  overflow: hidden;

  // gotchas #16：swiper 必须显式高度
  &__swiper {
    width: 100%;
    height: 100%;
  }

  &__item {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    // 让 PetPlaceholder 铺满
    :deep(.acs-pet-placeholder) {
      width: 100% !important;
      height: 100% !important;
    }
    :deep(.acs-pet-placeholder svg) {
      width: 100% !important;
      height: 100% !important;
    }
  }

  &__image {
    width: 100%;
    height: 100%;
    display: block;
  }

  // 右下页码：设计稿 bottom 12 / right 12 → 24rpx / 24rpx；padding 3/10 → 6/20rpx；radius 10 → 20rpx
  &__pager {
    position: absolute;
    right: 24rpx;
    bottom: 24rpx;
    padding: 6rpx 20rpx;
    border-radius: 20rpx;
    background: rgba(0, 0, 0, 0.45);
    line-height: 1;
  }

  &__pager-text {
    font-size: $font-size-sm; // 11px
    font-weight: $font-weight-semibold;
    color: #ffffff;
    font-family: $font-family-num;
    line-height: 1;
  }

  // 底部指示点：居中，bottom 12 → 24rpx，gap 4 → 8rpx
  &__dots {
    position: absolute;
    left: 50%;
    bottom: 24rpx;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  &__dot {
    // inactive: 5×5 → 10rpx
    width: 10rpx;
    height: 10rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.55);
    transition: all 0.2s;

    &--active {
      // active: 16×5 → 32rpx × 10rpx，圆角 6rpx
      width: 32rpx;
      height: 10rpx;
      border-radius: 6rpx;
      background: #ffffff;
    }
  }
}
</style>
