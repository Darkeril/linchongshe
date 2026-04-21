<template>
  <div>
    <template v-for="(item, index) in options" :key="item.dictValue">
      <template v-if="values.includes(String(item.dictValue))">
        <a-tag
          :index="index"
          :color="resolveTagColor(item.listClass)"
          :class="item.cssClass"
        >
          {{ item.dictLabel }}
        </a-tag>
      </template>
    </template>
  </div>
</template>

<script lang="ts" setup>
  import { computed, reactive } from 'vue';

  interface Option {
    dictValue: string | number;
    dictLabel: string;
    listClass?: string; // 可选属性
    cssClass?: string; // 可选属性
  }

  const props = defineProps({
    options: {
      type: Array as () => Option[], // 指定数组元素的类型
      default: () => [],
    },
    value: {
      type: [Number, String, Array],
      default: undefined,
    },
  });

  const state = reactive({
    listClassOptions: {
      default: { color: 'default' },
      primary: { color: 'blue' },
      success: { color: 'green' },
      info: { color: 'gray' },
      warning: { color: 'orangered' },
      danger: { color: 'red' },
    } as Record<string, { color: string }>, // 添加索引签名
  });

  const values = computed(() => {
    const { value } = props;
    if (value === undefined || value === null) return [];
    return Array.isArray(value) ? value.map(String) : [String(value)];
  });

  const resolveTagColor = (listClass?: string) => {
    if (!listClass || listClass === 'default') return 'gray';
    return state.listClassOptions[listClass]?.color || 'gray';
  };
</script>

<style scoped lang="less">
  .el-tag + .el-tag {
    margin-left: 10px;
  }
</style>
