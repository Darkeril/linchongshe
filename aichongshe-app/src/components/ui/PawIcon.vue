<!--
  PawIcon — 小肉垫装饰图标
  设计稿来源：design/design-system.jsx 中 PawIcon 实现
  组件分层：ui/（设计稿特色装饰图标，uview-plus 未覆盖）

  说明：
  - 按 24×24 viewBox 复刻 5 个形状（4 椭圆 + 1 掌垫 path），与设计稿完全一致
  - 只接受 size / color 两个 props，旋转/定位/透明度由父组件通过 style 控制
  - 颜色不硬编码，必须由外部通过 color prop 传入 token 颜色
-->
<template>
  <view
    class="paw-icon"
    :style="{
      width: size + 'px',
      height: size + 'px',
      lineHeight: 0,
    }"
  >
    <!-- H5 / 小程序均支持 inline SVG（通过 image 或直接 svg 均可，这里走 svg 确保形状精准） -->
    <svg
      :width="size"
      :height="size"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
      :style="{ display: 'block', fill: color }"
    >
      <ellipse cx="6" cy="9" rx="2.2" ry="3" />
      <ellipse cx="10.5" cy="6" rx="2" ry="2.8" />
      <ellipse cx="15" cy="6" rx="2" ry="2.8" />
      <ellipse cx="19" cy="9" rx="2.2" ry="3" />
      <path
        d="M12.5 10c-3.2 0-5.8 2.6-5.8 5.4 0 2 1.6 3.6 3.6 3.6 1.1 0 2-0.5 2.2-0.5 0.2 0 1.1 0.5 2.2 0.5 2 0 3.6-1.6 3.6-3.6 0-2.8-2.6-5.4-5.8-5.4z"
      />
    </svg>
  </view>
</template>

<script setup lang="ts">
interface Props {
  /** 图标尺寸，单位 px（会同步映射到 svg width/height） */
  size?: number;
  /** 图标填充色，必须从外部 token 传入，不接受默认硬编码色值 */
  color: string;
}

withDefaults(defineProps<Props>(), {
  size: 16,
});
</script>

<style lang="scss" scoped>
.paw-icon {
  display: inline-block;
}
</style>
