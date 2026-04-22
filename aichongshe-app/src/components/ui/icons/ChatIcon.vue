<!--
  ChatIcon — 对话框（评论）
  设计稿来源：screens-part2.jsx L314
  - 仅 outline，24×24 viewBox
  - class 加 acs- 前缀（gotchas #12）
-->
<template>
  <view
    class="acs-chat-icon"
    :style="{ width: size + 'px', height: size + 'px', lineHeight: 0 }"
  >
    <svg
      :width="size"
      :height="size"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
      :style="{ display: 'block' }"
    >
      <!-- 圆角对话框 + 左下小尾巴 -->
      <path
        d="M4 5.5 A2 2 0 0 1 6 3.5 L18 3.5 A2 2 0 0 1 20 5.5 L20 15.5 A2 2 0 0 1 18 17.5 L10 17.5 L6 21 L6 17.5 A2 2 0 0 1 4 15.5 Z"
        fill="none"
        :stroke="color"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </svg>
  </view>
</template>

<script setup lang="ts">
interface Props {
  /** 图标尺寸（px） */
  size?: number;
  /** 描边色 */
  color: string;
}

withDefaults(defineProps<Props>(), {
  size: 22,
});
</script>

<style lang="scss" scoped>
.acs-chat-icon {
  display: inline-block;
}
</style>
