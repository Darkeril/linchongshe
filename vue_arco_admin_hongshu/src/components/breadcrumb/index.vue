<template>
  <a-breadcrumb class="container-breadcrumb">
    <a-breadcrumb-item>
      <icon-apps/>
    </a-breadcrumb-item>
    <a-breadcrumb-item v-for="item in resolvedItems" :key="item">
      {{ item }}
    </a-breadcrumb-item>
  </a-breadcrumb>
</template>

<script lang="ts" setup>
import { computed, PropType } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  items: {
    type: Array as PropType<string[]>,
    default() {
      return [];
    },
  },
});

const { messages, locale } = useI18n();

// vue-i18n 默认将点号作为嵌套路径分隔符，但 locale 中 menu 的子项
// 使用平铺 key（如 'web.tag'），导致 $t('menu.web.tag') 解析失败。
// 这里对未解析的 key 做 flat key 回退查找。
const resolvedItems = computed(() =>
  props.items.map((item) => {
    if (item.startsWith('menu.')) {
      const flatKey = item.slice(5);
      const menuObj = (messages.value[locale.value] as Record<string, any>)
        ?.menu;
      if (menuObj && flatKey in menuObj) {
        return menuObj[flatKey] as string;
      }
    }
    return item;
  })
);
</script>

<style scoped lang="less">
.container-breadcrumb {
  margin: 16px 0;

  :deep(.arco-breadcrumb-item) {
    color: rgb(var(--gray-6));

    &:last-child {
      color: rgb(var(--gray-8));
    }
  }
}
</style>
