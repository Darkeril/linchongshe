<template>
  <div class="container">
    <div class="left-side">
      <div class="panel">
        <Banner />
        <DataPanel />
      </div>
      <div class="panel" style="margin-top: 16px">
        <Calendar />
      </div>
      <a-grid :cols="24" :col-gap="16" :row-gap="16" style="margin-top: 16px">
        <a-grid-item :span="{ xs: 24, sm: 24, md: 24, lg: 8, xl: 8, xxl: 8 }">
          <CategoriesPercent />
        </a-grid-item>
        <a-grid-item :span="{ xs: 24, sm: 24, md: 24, lg: 8, xl: 8, xxl: 8 }">
          <ProductTypesPercent />
        </a-grid-item>
        <a-grid-item :span="{ xs: 24, sm: 24, md: 24, lg: 8, xl: 8, xxl: 8 }">
          <UsagePercent />
        </a-grid-item>
        <a-grid-item :span="24">
          <div class="workbench-trend-pair-row">
            <DailyNewTrend />
            <NoteInteractionEfficiency />
          </div>
        </a-grid-item>
        <a-grid-item
          :span="{ xs: 24, sm: 24, md: 24, lg: 12, xl: 12, xxl: 12 }"
        >
          <OrderSalesTrend />
        </a-grid-item>
        <a-grid-item
          :span="{ xs: 24, sm: 24, md: 24, lg: 12, xl: 12, xxl: 12 }"
        >
          <ChatBusinessTrend />
        </a-grid-item>
      </a-grid>
    </div>
    <div class="right-side">
      <a-grid :cols="24" :row-gap="16">
        <a-grid-item :span="24">
          <div class="panel moduler-wrap">
            <!-- <QuickOperation /> -->
            <RecentlyVisited />
          </div>
        </a-grid-item>
        <a-grid-item class="panel" :span="24">
          <Carousel />
        </a-grid-item>
        <a-grid-item class="panel" :span="24">
          <WorkbenchSidebarBrief />
        </a-grid-item>
        <a-grid-item class="panel" :span="24">
          <LoginLocationStats />
        </a-grid-item>
        <!-- <a-grid-item class="panel" :span="24">
          <Announcement />
        </a-grid-item> -->
        <a-grid-item class="panel" :span="24">
          <Docs />
        </a-grid-item>
      </a-grid>
    </div>
  </div>
</template>

<script lang="ts" setup>
import Banner from './components/banner.vue';
import DataPanel from './components/data-panel.vue';
import Calendar from './components/calendar.vue';
import PopularContent from './components/popular-content.vue';
import CategoriesPercent from './components/categories-percent.vue';
import RecentlyVisited from './components/recently-visited.vue';
import QuickOperation from './components/quick-operation.vue';
import Announcement from './components/announcement.vue';
import Carousel from './components/carousel.vue';
import Docs from './components/docs.vue';
import LoginLocationStats from './components/login-location-stats.vue';
import WorkbenchSidebarBrief from './components/workbench-sidebar-brief.vue';
import ProductTypesPercent from './components/product-types-percent.vue';
import UsagePercent from './components/usage-percent.vue';
import DailyNewTrend from './components/daily-new-trend.vue';
import NoteInteractionEfficiency from './components/note-interaction-efficiency.vue';
import OrderSalesTrend from './components/order-sales-trend.vue';
import ChatBusinessTrend from './components/chat-business-trend.vue';
</script>

<script lang="ts">
export default {
  name: 'Dashboard', // If you want the include property of keep-alive to take effect, you must name the component
};
</script>

<style lang="less" scoped>
.container {
  background-color: var(--color-fill-2);
  padding: 16px 20px;
  padding-bottom: 0;
  display: flex;
}

.left-side {
  flex: 1;
  overflow: auto;
}

.right-side {
  width: 280px;
  flex-shrink: 0;
  margin-left: 16px;
}

.panel {
  background-color: var(--color-bg-2);
  border-radius: 4px;
  overflow: auto;
}

/* 并排趋势：子列等高拉伸，图表区 flex 填满，避免一侧隐藏 Top 后高低不齐 */
.workbench-trend-pair-row {
  display: grid;
  gap: 16px;
  grid-template-columns: 1fr;
  align-items: stretch;
}

@media screen and (min-width: 992px) {
  .workbench-trend-pair-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

:deep(.panel-border) {
  margin-bottom: 0;
  border-bottom: 1px solid rgb(var(--gray-2));
}

.moduler-wrap {
  border-radius: 4px;
  background-color: var(--color-bg-2);

  :deep(.text) {
    font-size: 12px;
    text-align: center;
    color: rgb(var(--gray-8));
  }

  :deep(.wrapper) {
    margin-bottom: 8px;
    text-align: center;
    cursor: pointer;

    &:last-child {
      .text {
        margin-bottom: 0;
      }
    }

    &:hover {
      .icon {
        color: rgb(var(--arcoblue-6));
        background-color: #e8f3ff;
      }

      .text {
        color: rgb(var(--arcoblue-6));
      }
    }
  }

  :deep(.icon) {
    display: inline-block;
    width: 32px;
    height: 32px;
    margin-bottom: 4px;
    color: rgb(var(--dark-gray-1));
    line-height: 32px;
    font-size: 16px;
    text-align: center;
    background-color: rgb(var(--gray-1));
    border-radius: 4px;
  }
}
</style>

<style lang="less" scoped>
// responsive
.mobile {
  .container {
    display: block;
  }

  .right-side {
    // display: none;
    width: 100%;
    margin-left: 0;
    margin-top: 16px;
  }
}
</style>
