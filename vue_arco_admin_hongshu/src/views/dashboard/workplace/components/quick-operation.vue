<template>
  <a-card
    class="general-card"
    :title="$t('dashboard.quickOperation')"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ padding: '24px 20px 0 20px' }"
  >
    <template #extra>
      <!-- <a-link>{{ '管理' }}</a-link> -->
    </template>
    <a-empty
      v-if="links.length === 0"
      :description="$t('dashboard.quickOperationEmpty')"
    />
    <a-row v-else :gutter="8">
      <a-col
        v-for="link in links"
        :key="link.path"
        :span="8"
        class="wrapper"
        @click="handleClick(link)"
      >
        <div class="icon">
          <component :is="link.icon" />
        </div>
        <a-typography-paragraph class="text">
          {{ link.text }}
        </a-typography-paragraph>
      </a-col>
    </a-row>
    <a-divider class="split-line" style="margin: 0" />
  </a-card>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router';
import { useFrequentVisitLinks } from '@/hooks/workbench-visit-links';

const router = useRouter();
const { links } = useFrequentVisitLinks(6);

const handleClick = (link: { path: string }) => {
  router.push(link.path);
};
</script>

<style scoped lang="less">
.wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  margin-bottom: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background-color: var(--color-fill-2);
  }

  .icon {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--color-fill-1);
    border-radius: 4px;
    margin-bottom: 8px;
    font-size: 20px;
    color: var(--color-text-2);
  }

  .text {
    margin: 0;
    font-size: 12px;
    color: var(--color-text-1);
    text-align: center;
  }
}
</style>
