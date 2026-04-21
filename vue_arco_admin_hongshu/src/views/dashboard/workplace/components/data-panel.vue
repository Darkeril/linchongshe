<template>
  <div class="panel">
    <div class="stat-grid">
      <div class="stat-cell" @click="btnClick('1')">
        <a-space>
          <a-avatar
            :size="55"
            class="col-avatar stat-avatar stat-avatar--visit"
          >
            <icon-eye :size="30" />
          </a-avatar>
          <a-statistic
            class="stat-block"
            :title="$t('dashboard.ipCount')"
            :value="visitAddTotal"
            :value-from="0"
            animation
            show-group-separator
          >
            <template #suffix>
              <span class="unit">{{ $t('common.unit') }}</span>
            </template>
          </a-statistic>
        </a-space>
      </div>
      <div class="stat-cell" @click="btnClick('2')">
        <a-space>
          <a-avatar
            :size="55"
            class="col-avatar stat-avatar stat-avatar--member"
          >
            <icon-user-group :size="30" />
          </a-avatar>
          <a-statistic
            class="stat-block"
            :title="$t('dashboard.memberCount')"
            :value="userTotal"
            :value-from="0"
            animation
            show-group-separator
          >
            <template #suffix>
              <a-badge
                v-if="todayNewUsers > 0"
                :offset="[12, -15]"
                :count="todayNewUsers"
              >
                <span class="unit">{{ $t('common.unit') }}</span>
              </a-badge>
              <span v-else class="unit">{{ $t('common.unit') }}</span>
            </template>
          </a-statistic>
        </a-space>
      </div>
      <div class="stat-cell" @click="btnClick('3')">
        <a-space>
          <a-avatar :size="55" class="col-avatar stat-avatar stat-avatar--note">
            <icon-pen :size="30" />
          </a-avatar>
          <a-statistic
            class="stat-block"
            :title="$t('dashboard.noteCount')"
            :value="blogTotal"
            :value-from="0"
            animation
            show-group-separator
          >
            <template #suffix>
              <a-badge
                v-if="todayNewNotes > 0"
                :offset="[12, -15]"
                :count="todayNewNotes"
              >
                <span class="unit">{{ $t('common.unit') }}</span>
              </a-badge>
              <span v-else class="unit">{{ $t('common.unit') }}</span>
            </template>
          </a-statistic>
        </a-space>
      </div>
      <div class="stat-cell" @click="btnClick('4')">
        <a-space>
          <a-avatar
            :size="55"
            class="col-avatar stat-avatar stat-avatar--product"
          >
            <icon-shake :size="30" />
          </a-avatar>
          <a-statistic
            class="stat-block"
            :title="$t('dashboard.productCount')"
            :value="productTotal"
            :value-from="0"
            animation
          >
            <template #suffix>
              <a-badge
                v-if="todayNewProducts > 0"
                :offset="[12, -15]"
                :count="todayNewProducts"
              >
                <span class="unit">{{ $t('common.unit') }}</span>
              </a-badge>
              <span v-else class="unit">{{ $t('common.unit') }}</span>
            </template>
          </a-statistic>
        </a-space>
      </div>
      <div class="stat-cell" @click="btnClick('5')">
        <a-space>
          <a-avatar
            :size="55"
            class="col-avatar stat-avatar stat-avatar--order"
          >
            <icon-file :size="30" />
          </a-avatar>
          <a-statistic
            class="stat-block"
            :title="$t('dashboard.orderCount')"
            :value="orderTotal"
            :value-from="0"
            animation
            show-group-separator
          >
            <template #suffix>
              <a-badge
                v-if="todayNewOrders > 0"
                :offset="[12, -15]"
                :count="todayNewOrders"
              >
                <span class="unit">{{ $t('common.unit') }}</span>
              </a-badge>
              <span v-else class="unit">{{ $t('common.unit') }}</span>
            </template>
          </a-statistic>
        </a-space>
      </div>
      <!-- 大模型数暂时隐藏
      <div class="stat-cell" @click="btnClick('6')">
        <a-space>
          <a-avatar
            :size="54"
            class="col-avatar stat-avatar stat-avatar--model"
          >
            <icon-robot :size="30" />
          </a-avatar>
          <a-statistic
            class="stat-block"
            :title="$t('dashboard.modelCount')"
            :value="modelTotal"
            :value-from="0"
            animation
          >
            <template #suffix>
              <span class="unit">{{ $t('common.unit') }}</span>
            </template>
          </a-statistic>
        </a-space>
      </div>
      -->
    </div>
    <a-divider class="panel-border" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import {
  IconEye,
  IconFile,
  IconPen,
  IconRobot,
  IconShake,
  IconUserGroup,
} from '@arco-design/web-vue/es/icon';
import {
  init,
  getVisitByWeek,
  getBlogCountByTag,
  getBlogCountByBlogSort,
} from '@/api/dashboard';
import { useRouter } from 'vue-router';

const router = useRouter();

const blogTotal = ref(0);
const productTotal = ref(0);
const userTotal = ref(0);
const visitAddTotal = ref(0);
const orderTotal = ref(0);
const modelTotal = ref(0);
const todayNewUsers = ref(0);
const todayNewNotes = ref(0);
const todayNewProducts = ref(0);
const todayNewOrders = ref(0);

const showLineChart = ref(false);
const showPieChart = ref(false);
const showPieBlogSortChart = ref(false);

const lineChartData = reactive({
  date: [],
  expectedData: [],
  actualData: [],
});

const tagChartData = ref([]);
const blogSortChartData = ref([]);

const btnClick = (id: string) => {
  const routes: Record<number, string> = {
    1: '/system/log/loginLog',
    2: '/member/member',
    3: '/note/note',
    4: '/idle/product',
    5: '/idle/order',
    6: '/chat/config/model',
  };

  const hasNewMap: Record<number, boolean> = {
    1: true,
    2: todayNewUsers.value > 0,
    3: todayNewNotes.value > 0,
    4: todayNewProducts.value > 0,
    5: todayNewOrders.value > 0,
    6: false,
  };

  const num = Number(id);
  const path = routes[num];
  if (!path) {
    console.error('Invalid route id:', id);
    return;
  }

  const query = hasNewMap[num] ? { today: '1' } : undefined;
  router.push({ path, query });
};

// 处理响应函数
function handleInitResponse(data: {
  blogCount: number;
  productCount: number;
  userCount: number;
  visitCount: number;
  orderCount?: number;
  gptModelCount: number;
}) {
  blogTotal.value = data.blogCount;
  productTotal.value = data.productCount;
  userTotal.value = data.userCount;
  visitAddTotal.value = data.visitCount;
  orderTotal.value = Number(data.orderCount) || 0;
  modelTotal.value = data.gptModelCount;
}

function handleVisitResponse(data: {
  date: never[];
  pv: never[];
  uv: never[];
}) {
  lineChartData.date = data.date;
  lineChartData.expectedData = data.pv;
  lineChartData.actualData = data.uv;
  showLineChart.value = true;
}

function handleTagResponse(data: {
  map: (arg0: (tag: any) => { name: any; value: any }) => never[];
}) {
  tagChartData.value = data.map((tag: { name: any; count: any }) => ({
    name: tag.name,
    value: tag.count,
  }));
  showPieChart.value = true;
}

function handleBlogSortResponse(data: {
  map: (arg0: (blogSort: any) => { name: any; value: any }) => never[];
}) {
  blogSortChartData.value = data.map((blogSort: { name: any; count: any }) => ({
    name: blogSort.name,
    value: blogSort.count,
  }));
  showPieBlogSortChart.value = true;
}

// 主数据获取函数
async function fetchData() {
  try {
    const [
      initResponse,
      visitResponse,
      tagResponse,
      blogSortResponse,
      dailyNewRes,
    ] = await Promise.all([
      init(),
      getVisitByWeek(),
      getBlogCountByTag(),
      getBlogCountByBlogSort(),
      // 每日新增数据（用于徽标）
      import('@/api/dashboard').then((m) => m.getDailyNewData()),
    ]);

    if (initResponse.code === 200) {
      handleInitResponse(initResponse.data);
    } else {
      console.error('Failed to fetch init data');
    }

    if (visitResponse.code === 200) {
      handleVisitResponse(visitResponse.data);
    } else {
      console.error('Failed to fetch visit data');
    }

    if (tagResponse.code === 200) {
      handleTagResponse(tagResponse.data);
    } else {
      console.error('Failed to fetch blog count by tag');
    }

    if (blogSortResponse.code === 200) {
      handleBlogSortResponse(blogSortResponse.data);
    } else {
      console.error('Failed to fetch blog count by blog sort');
    }

    if (dailyNewRes.code === 200) {
      todayNewUsers.value = dailyNewRes.data.todayNewUsers || 0;
      todayNewNotes.value = dailyNewRes.data.todayNewNotes || 0;
      todayNewProducts.value = dailyNewRes.data.todayNewProducts || 0;
      todayNewOrders.value = dailyNewRes.data.todayNewOrders || 0;
    }
  } catch (error) {
    console.error('An error occurred while fetching data:', error);
  }
}

// 在挂载时获取数据
onMounted(fetchData);
</script>

<style lang="less" scoped>
.panel {
  margin-bottom: 0;
}

.stat-grid {
  display: flex;
  justify-content: space-evenly;
  padding: 10px 0 0;
  align-items: stretch;
}

@media (max-width: 1024px) {
  .stat-grid {
    flex-wrap: wrap;
    gap: 12px 0;
  }
}

.stat-cell {
  flex: 1;
  min-width: 0;
  display: flex;
  justify-content: center;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.15s ease;
  border-right: 1px solid rgb(var(--gray-2));

  &:last-child {
    border-right: none;
  }

  &:hover {
    background-color: rgb(var(--gray-1));
  }
}

.stat-block {
  min-width: 0;
}

.stat-cell :deep(.arco-space) {
  width: auto;
}

/* 标题单行展示，避免与其它项高度不齐；极窄时省略号 */
.stat-cell :deep(.arco-statistic-title) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.col-avatar {
  margin-right: 10px;
  margin-left: -10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Arco 图标 + 浅色底，六项语义与色系区分 */
.stat-avatar {
  flex-shrink: 0;

  :deep(svg) {
    display: block;
  }

  &--visit {
    background-color: rgb(var(--arcoblue-1));
    color: rgb(var(--arcoblue-6));
  }

  &--member {
    background-color: rgb(var(--purple-1));
    color: rgb(var(--purple-6));
  }

  &--note {
    background-color: rgb(var(--green-1));
    color: rgb(var(--green-6));
  }

  &--product {
    background-color: rgb(var(--orange-1));
    color: rgb(var(--orange-6));
  }

  &--order {
    background-color: rgb(var(--cyan-1));
    color: rgb(var(--cyan-6));
  }

  &--model {
    background-color: rgb(var(--magenta-1));
    color: rgb(var(--magenta-6));
  }
}

.up-icon {
  color: rgb(var(--red-6));
}

.unit {
  margin-left: 5px;
  color: rgb(var(--gray-8));
  font-size: 15px;
}

:deep(.panel-border) {
  margin: 10px 0 0 0;
}
</style>
