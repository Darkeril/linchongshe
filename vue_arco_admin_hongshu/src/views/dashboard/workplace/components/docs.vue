<template>
  <a-card
    class="general-card"
    :title="$t('dashboard.helpDocs')"
    :header-style="{ paddingBottom: 0 }"
    :body-style="{ paddingTop: 0 }"
    style="height: 166px"
  >
    <template #extra>
      <!-- <a-link>{{ '查看更多' }}</a-link> -->
    </template>
    <a-row>
      <a-col :span="12">
        <a-link @click="handleLinkClick(docs[0])">
          {{ docs[0].title }}
        </a-link>
      </a-col>
      <a-col :span="12">
        <a-link @click="handleLinkClick(docs[1])">
          {{ docs[1].title }}
        </a-link>
      </a-col>
      <a-col :span="12">
        <a-link @click="handleLinkClick(docs[2])">
          {{ docs[2].title }}
        </a-link>
      </a-col>
      <a-col :span="12">
        <a-link @click="handleLinkClick(docs[3])">
          {{ docs[3].title }}
        </a-link>
      </a-col>
    </a-row>
  </a-card>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

interface DocItem {
  title: string;
  url: string;
}

const { t } = useI18n();

const docs = computed<DocItem[]>(() => [
  {
    title: t('dashboard.proSource'),
    url: 'https://www.yuque.com/xiaomage-ippj7/kaoqwn/rqslw69egcbgx7v7?singleDoc# 《Pro版源码》',
  },
  {
    title: t('dashboard.projectIntro'),
    url: 'https://www.yuque.com/xiaomage-ippj7/kaoqwn/ul26y3yxwl70gssf?singleDoc# 《项目简介》',
  },
  {
    title: t('dashboard.coreHighlights'),
    url: 'https://www.yuque.com/xiaomage-ippj7/kaoqwn/chm8w6thvrmtnmbk?singleDoc# 《项目文档-核心亮点》',
  },
  {
    title: t('dashboard.techArchitecture'),
    url: 'https://www.yuque.com/xiaomage-ippj7/kaoqwn/nlxhpqk33hc79ab1?singleDoc# 《技术架构与创新方案》',
  }, // 链接地址待补充
]);

const handleLinkClick = (item: DocItem) => {
  if (item.url) {
    window.open(item.url, '_blank');
  } else {
    console.warn(`链接地址未配置: ${item.title}`);
  }
};
</script>

<style lang="less" scoped>
.arco-card-body .arco-link {
  margin: 10px 0;
  color: rgb(var(--gray-8));
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: rgb(var(--arcoblue-6));
  }
}
</style>
