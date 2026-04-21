<template>
  <div class="workbench-carousel-root">
    <a-spin :loading="loading" class="workbench-carousel-spin">
      <a-carousel
        v-if="displayItems.length > 0"
        indicator-type="slider"
        show-arrow="hover"
        auto-play
        class="workbench-carousel"
      >
        <a-carousel-item
          v-for="(item, idx) in displayItems"
          :key="`${idx}-${item.url}`"
        >
          <div
            class="workbench-carousel-slide"
            :class="{ 'is-clickable': !!item.link?.trim() }"
            role="img"
            :aria-label="item.title || $t('dashboard.carousel')"
            @click="onSlideClick(item)"
          >
            <img :src="absUrl(item.url)" :alt="item.title || ''" />
          </div>
        </a-carousel-item>
      </a-carousel>
      <div v-else-if="!loading" class="workbench-carousel-empty">
        <a-empty :description="$t('dashboard.carouselEmpty')" />
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { getWorkbenchCarouselConfig } from '@/api/system/config';

interface CarouselRow {
  url?: string;
  link?: string;
  title?: string;
  sortOrder?: number;
}

const items = ref<CarouselRow[]>([]);
const loading = ref(true);

const displayItems = computed(() =>
  [...items.value]
    .filter((i) => (i.url || '').trim())
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
);

function absUrl(u: string | undefined): string {
  const s = (u || '').trim();
  if (!s) return '';
  if (s.startsWith('//')) {
    if (typeof window !== 'undefined') {
      return `${window.location.protocol}${s}`;
    }
    return `https:${s}`;
  }
  return s;
}

function onSlideClick(item: CarouselRow) {
  const link = item.link?.trim();
  if (!link) return;
  window.open(link, '_blank', 'noopener,noreferrer');
}

onMounted(async () => {
  loading.value = true;
  try {
    const res = await getWorkbenchCarouselConfig();
    if (res.code === 200 && Array.isArray(res.data)) {
      items.value = res.data;
    }
  } catch {
    items.value = [];
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped lang="less">
.workbench-carousel-root {
  width: 100%;
}

.workbench-carousel-spin {
  display: block;
  width: 100%;
}

.workbench-carousel {
  width: 100%;
  height: 170px;
  border-radius: 4px;
  overflow: hidden;
}

.workbench-carousel-slide {
  width: 100%;
  height: 170px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-fill-2);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  &.is-clickable {
    cursor: pointer;
  }
}

.workbench-carousel-empty {
  height: 170px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  background: var(--color-bg-1);
  border: 1px dashed var(--color-border-2);

  :deep(.arco-empty-description) {
    font-size: 12px;
    padding: 0 8px;
    text-align: center;
  }
}
</style>
