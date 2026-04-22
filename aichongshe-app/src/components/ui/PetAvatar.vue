
<!--
  PetAvatar — 萌宠头像（SVG 还原）
  设计稿来源：design/design-system.jsx 行 220-252 的 PetAvatar
  组件分层：ui/（设计稿特色装饰组件，uview-plus 未覆盖）

  变体：dog / cat / mochi / puppy（Phase 2 Login 场景用到这 4 种）
  说明：
  - viewBox 严格用 60×60，形状 path / ellipse 与设计稿一字不差
  - 每个变体的 bg / face / ear / nose 色板严格从设计稿 palettes 还原
  - bg 可通过 prop 覆盖
  - 不依赖 tokens（这是插画级固定配色，设计稿自身给定），符合"像素级还原"
-->
<template>
  <view
    class="pet-avatar"
    :style="{ width: size + 'px', height: size + 'px', lineHeight: 0 }"
  >
    <svg
      :width="size"
      :height="size"
      viewBox="0 0 60 60"
      xmlns="http://www.w3.org/2000/svg"
      :style="{ display: 'block', borderRadius: '50%' }"
    >
      <!-- 底色 -->
      <rect width="60" height="60" :fill="palette.bg" rx="30" />
      <!-- 耳朵 -->
      <ellipse
        cx="17"
        cy="22"
        rx="7"
        ry="10"
        :fill="palette.ear"
        transform="rotate(-18 17 22)"
      />
      <ellipse
        cx="43"
        cy="22"
        rx="7"
        ry="10"
        :fill="palette.ear"
        transform="rotate(18 43 22)"
      />
      <!-- 脸 -->
      <ellipse cx="30" cy="34" rx="16" ry="15" :fill="palette.face" />
      <!-- 眼 -->
      <ellipse cx="24" cy="32" rx="1.8" ry="2.3" :fill="palette.nose" />
      <ellipse cx="36" cy="32" rx="1.8" ry="2.3" :fill="palette.nose" />
      <!-- 眼高光 -->
      <circle cx="24.5" cy="31.2" r="0.6" fill="#fff" />
      <circle cx="36.5" cy="31.2" r="0.6" fill="#fff" />
      <!-- 鼻 -->
      <ellipse cx="30" cy="38" rx="1.6" ry="1.2" :fill="palette.nose" />
      <!-- 嘴 -->
      <path
        d="M28 40 Q30 42 32 40"
        :stroke="palette.nose"
        stroke-width="1"
        fill="none"
        stroke-linecap="round"
      />
      <!-- 腮红 -->
      <circle cx="21" cy="38" r="2.2" fill="#FFB4A0" opacity="0.5" />
      <circle cx="39" cy="38" r="2.2" fill="#FFB4A0" opacity="0.5" />
    </svg>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

/** 变体（与设计稿保持一致） */
export type PetAvatarVariant = 'dog' | 'cat' | 'mochi' | 'puppy';

interface PetAvatarPalette {
  bg: string;
  face: string;
  ear: string;
  nose: string;
}

interface Props {
  /** 变体 */
  variant?: PetAvatarVariant;
  /** 尺寸（px），SVG 会等比缩放 */
  size?: number;
  /** 覆盖背景色；未传则用变体默认 bg */
  bg?: string;
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'dog',
  size: 40,
  bg: '',
});

/** 与 design-system.jsx 的 palettes 严格一致 */
const PALETTES: Record<PetAvatarVariant, PetAvatarPalette> = {
  dog: { bg: '#FFD8BF', face: '#F4C9A4', ear: '#C97B4A', nose: '#3a2a20' },
  cat: { bg: '#FFE6D5', face: '#FBE6CE', ear: '#E8846E', nose: '#3a2a20' },
  mochi: { bg: '#F5EEE2', face: '#FFFFFF', ear: '#C4AC95', nose: '#3a2a20' },
  puppy: { bg: '#FFCCB8', face: '#FBDAC4', ear: '#6FB89A', nose: '#3a2a20' },
};

const palette = computed<PetAvatarPalette>(() => {
  const base = PALETTES[props.variant] ?? PALETTES.dog;
  return props.bg ? { ...base, bg: props.bg } : base;
});
</script>

<style lang="scss" scoped>
.pet-avatar {
  display: inline-block;
}
</style>
