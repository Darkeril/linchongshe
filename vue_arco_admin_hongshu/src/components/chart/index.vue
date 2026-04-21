<template>
  <VCharts
      v-if="renderChart"
      :option="options"
      :update-options="updateOptions"
      :autoresize="autoResize"
      :style="{ width, height }"
  />
</template>

<script lang="ts" setup>
import {nextTick, ref} from 'vue';
import VCharts from 'vue-echarts';
// import { useAppStore } from '@/store';

defineProps({
  options: {
    type: Object,
    default() {
      return {};
    },
  },
  /** 传入 vue-echarts 的 setOption 第二参数，避免 xAxis 等合并残留导致切换区间后横轴不更新 */
  updateOptions: {
    type: Object,
    default: undefined,
  },
  autoResize: {
    type: Boolean,
    default: true,
  },
  width: {
    type: String,
    default: '100%',
  },
  height: {
    type: String,
    default: '100%',
  },
});
// const appStore = useAppStore();
// const theme = computed(() => {
//   if (appStore.theme === 'dark') return 'dark';
//   return '';
// });
const renderChart = ref(false);
// wait container expand
nextTick(() => {
  renderChart.value = true;
});
</script>

<style scoped lang="less"></style>
