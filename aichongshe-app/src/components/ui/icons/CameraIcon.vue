<!-- CameraIcon — 发布页添加图片图标，对齐 design/design-system.jsx CameraIcon -->
<template>
  <view class="acs-camera-icon" :style="{ width: size + 'px', height: size + 'px', lineHeight: 0 }">
    <svg
      :width="size"
      :height="size"
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      :style="{ display: 'block' }"
    >
      <path
        d="M3 8.5h3l2-3h8l2 3h3v11H3v-11z"
        :stroke="color"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <circle
        cx="12"
        cy="13.5"
        r="3.5"
        :stroke="color"
        stroke-width="1.8"
      />
    </svg>
  </view>
</template>

<script setup lang="ts">
interface Props {
  size?: number;
  color?: string;
}
withDefaults(defineProps<Props>(), {
  size: 22,
  color: '#C97B4A',
});
</script>

<style lang="scss" scoped>
.acs-camera-icon {
  display: inline-block;
}
</style>
