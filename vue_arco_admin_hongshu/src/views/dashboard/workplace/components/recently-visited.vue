<template>
  <a-card
    class="general-card"
    :title="$t('dashboard.recentlyVisited')"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ padding: '4px 20px 0 20px' }"
  >
    <template #extra>
      <!-- <a-link>{{ '管理' }}</a-link> -->
    </template>
    <a-empty
      v-if="links.length === 0"
      :description="$t('dashboard.recentlyVisitedEmpty')"
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
        <div class="text" :title="link.text">{{ link.text }}</div>
      </a-col>
    </a-row>
    <a-divider class="split-line" style="margin: 0" />
  </a-card>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router';
import { useRecentVisitLinks } from '@/hooks/workbench-visit-links';

const router = useRouter();
const { links } = useRecentVisitLinks(9);

const handleClick = (link: { path: string }) => {
  router.push(link.path);
};
</script>

<style scoped lang="less">
.wrapper {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 8px;
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
    box-sizing: border-box;
    width: 100%;
    max-width: 100%;
    margin: 0;
    padding: 0 2px;
    font-size: 12px;
    color: var(--color-text-1);
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
