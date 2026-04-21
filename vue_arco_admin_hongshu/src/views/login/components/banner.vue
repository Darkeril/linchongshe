<template>
  <div class="banner">
    <div class="banner-inner">
      <CreatorPromo />
    </div>
  </div>
</template>

<script lang="ts" setup>
import CreatorPromo from '@/components/CreatorPromo.vue';
</script>

<style lang="less" scoped>
.banner {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;

  &-inner {
    flex: 1;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
