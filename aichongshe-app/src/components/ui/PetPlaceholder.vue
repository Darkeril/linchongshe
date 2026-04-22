<!--
  PetPlaceholder — 瀑布流卡片封面占位图
  设计稿来源：design/design-system.jsx 行 255-298 的 PetPlaceholder + PetAvatarShape
  - 柔和渐变背景 + 隐约宠物轮廓 + 小爪印
  - 色板固定用 latte 主题（design-system.jsx 行 49-67）
  - seed 决定渐变组合（5 种），variant 仅用于和 PetAvatar 头像保持语义一致
    （设计稿的 PetAvatarShape 只是简化形状，4 variant 视觉相似度高）
  - inline SVG：H5 / App-vue 直接支持；小程序理论上也能（基础编译期会转，不走 gotcha #10）。
    rendered 在 vue 页面，非 nvue，能容纳 SVG（不像 nvue 只有 flex）
  - 绝不用 `currentColor`（小程序不识别），都预编译成具体 fill
-->
<template>
  <view
    class="acs-pet-placeholder"
    :style="{ width: w + 'px', height: h + 'px', lineHeight: 0 }"
  >
    <svg
      :width="w"
      :height="h"
      :viewBox="`0 0 ${w} ${h}`"
      preserveAspectRatio="xMidYMid slice"
      xmlns="http://www.w3.org/2000/svg"
      :style="{ display: 'block' }"
    >
      <defs>
        <linearGradient :id="gid" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" :stop-color="c1" />
          <stop offset="100%" :stop-color="c2" />
        </linearGradient>
      </defs>
      <rect :width="w" :height="h" :fill="`url(#${gid})`" />

      <!-- 隐约宠物轮廓（PetAvatarShape 设计稿同款简化形状） -->
      <g
        opacity="0.55"
        :transform="`translate(${w / 2 - 40 * shapeScale}, ${h / 2 - 30 * shapeScale}) scale(${1.4 * shapeScale})`"
      >
        <!-- 脸 -->
        <ellipse cx="30" cy="34" rx="22" ry="20" fill="rgba(255,255,255,0.55)" />
        <!-- 左耳 -->
        <ellipse
          cx="14"
          cy="20"
          rx="6"
          ry="9"
          fill="rgba(255,255,255,0.5)"
          transform="rotate(-18 14 20)"
        />
        <!-- 右耳 -->
        <ellipse
          cx="46"
          cy="20"
          rx="6"
          ry="9"
          fill="rgba(255,255,255,0.5)"
          transform="rotate(18 46 20)"
        />
        <!-- 眼 / 鼻 / 腮红（区分 variant；颜色统一白色半透，形状微差） -->
        <!-- dog: 椭圆眼 -->
        <template v-if="variant === 'dog'">
          <ellipse cx="24" cy="32" rx="1.8" ry="2.4" fill="rgba(90,55,40,0.5)" />
          <ellipse cx="36" cy="32" rx="1.8" ry="2.4" fill="rgba(90,55,40,0.5)" />
        </template>
        <!-- cat: 立起眼（竖长） -->
        <template v-else-if="variant === 'cat'">
          <ellipse cx="24" cy="32" rx="1.4" ry="2.8" fill="rgba(90,55,40,0.5)" />
          <ellipse cx="36" cy="32" rx="1.4" ry="2.8" fill="rgba(90,55,40,0.5)" />
        </template>
        <!-- mochi: 圆点眼 -->
        <template v-else-if="variant === 'mochi'">
          <circle cx="24" cy="32" r="2" fill="rgba(90,55,40,0.5)" />
          <circle cx="36" cy="32" r="2" fill="rgba(90,55,40,0.5)" />
        </template>
        <!-- puppy: 半月眯眼 -->
        <template v-else>
          <path
            d="M22 32 Q24 34 26 32"
            stroke="rgba(90,55,40,0.5)"
            stroke-width="1.2"
            fill="none"
            stroke-linecap="round"
          />
          <path
            d="M34 32 Q36 34 38 32"
            stroke="rgba(90,55,40,0.5)"
            stroke-width="1.2"
            fill="none"
            stroke-linecap="round"
          />
        </template>
      </g>

      <!-- 小爪印（设计稿 design-system.jsx 行 280-283） -->
      <g :fill="pawColor" opacity="0.35">
        <circle :cx="w * 0.15" :cy="h * 0.85" r="2" />
        <circle :cx="w * 0.18" :cy="h * 0.82" r="1.5" />
        <circle :cx="w * 0.2" :cy="h * 0.86" r="1.5" />
      </g>
    </svg>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { PetVariant } from '@/types/note';
import { COLOR } from '@/styles/tokens';

interface Props {
  /** 宠物变体，决定眼睛形状 */
  variant?: PetVariant;
  /** 渐变 seed（通常传 note.id 的 hash 或 index） */
  seed?: string | number;
  /** 宽度（px） */
  w: number;
  /** 高度（px） */
  h: number;
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'dog',
  seed: 0,
});

/** 将 seed 转成数字（字符串 id 如 'discover-3' 走字符串哈希） */
const seedNum = computed<number>(() => {
  if (typeof props.seed === 'number') return props.seed;
  let h = 0;
  for (let i = 0; i < props.seed.length; i += 1) {
    h = (h * 31 + props.seed.charCodeAt(i)) & 0xffffffff;
  }
  return Math.abs(h);
});

/** 渐变色对（5 种，设计稿行 257-263） */
const GRADIENTS: Array<[string, string]> = [
  [COLOR.primarySoft, COLOR.surfaceDim],
  [COLOR.surfaceDim, COLOR.primarySoft],
  [COLOR.primarySoft, '#FFFFFF'],
  ['#F5E1D3', COLOR.primarySoft],
  [COLOR.surfaceDim, '#FFF4EE'],
];

const c1 = computed<string>(() => GRADIENTS[seedNum.value % GRADIENTS.length]![0]);
const c2 = computed<string>(() => GRADIENTS[seedNum.value % GRADIENTS.length]![1]);

/** gradient id 必须每实例唯一，否则同页面多卡片会互相抢 gradient */
const gid = computed<string>(() => `acs-pg-${props.variant}-${seedNum.value}`);

/** 爪印颜色 */
const pawColor = COLOR.primary;

/** 设计稿轮廓原尺寸基于 150×200，自适应到 w×h 时轻微缩放（不超过 2 倍） */
const shapeScale = computed<number>(() => {
  const base = Math.min(props.w / 150, props.h / 200);
  // 夹在 0.8-1.6 之间，保持形状辨识度
  return Math.max(0.8, Math.min(1.6, base));
});
</script>

<style lang="scss" scoped>
.acs-pet-placeholder {
  display: block;
  overflow: hidden;
}
</style>
