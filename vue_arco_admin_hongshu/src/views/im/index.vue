<template>
  <div class="im-dashboard">
    <Breadcrumb :items="['IM管理', '数据管理']" />
    <div class="im-dashboard__content">
      <div class="im-dashboard__actions">
        <span class="im-dashboard__refresh-time">最近刷新：{{ lastRefreshTimeText }}</span>
        <a-space>
          <a-button @click="goNoticeManager">
            <template #icon><icon-notification /></template>
            前往通知管理
          </a-button>
          <a-button type="primary" :loading="stat.loading" @click="refreshAll">
            <template #icon><icon-refresh /></template>
            刷新数据
          </a-button>
        </a-space>
      </div>
      <!-- 统计卡片：4 列网格，与下方图表同构 -->
      <div class="im-dashboard__grid">
        <div class="stat-card stat-card--user stat-card--clickable" @click="goImUser">
          <div class="stat-card__icon">
            <icon-user :size="28" />
          </div>
          <div class="stat-card__body">
            <div class="stat-card__title">总用户量</div>
            <div class="stat-card__value">
              <a-statistic
                :value="stat.totalUserCount"
                :value-from="0"
                animation
                show-group-separator
              />
            </div>
          </div>
        </div>
        <div class="stat-card stat-card--group stat-card--clickable" @click="goImGroup">
          <div class="stat-card__icon">
            <icon-apps :size="28" />
          </div>
          <div class="stat-card__body">
            <div class="stat-card__title">总群组数量</div>
            <div class="stat-card__value">
              <a-statistic
                :value="stat.totalGroupCount"
                :value-from="0"
                animation
                show-group-separator
              />
            </div>
          </div>
        </div>
        <div class="stat-card stat-card--daily stat-card--clickable" @click="goImUserDaily">
          <div class="stat-card__icon">
            <icon-clock-circle :size="28" />
          </div>
          <div class="stat-card__body">
            <div class="stat-card__title">日活用户</div>
            <div class="stat-card__value">
              <a-statistic
                :value="stat.dailyActiveCount"
                :value-from="0"
                animation
                show-group-separator
              />
            </div>
          </div>
        </div>
        <div class="stat-card stat-card--monthly stat-card--clickable" @click="goImUserMonthly">
          <div class="stat-card__icon">
            <icon-calendar :size="28" />
          </div>
          <div class="stat-card__body">
            <div class="stat-card__title">月活用户</div>
            <div class="stat-card__value">
              <a-statistic
                :value="stat.monthlyActiveCount"
                :value-from="0"
                animation
                show-group-separator
              />
            </div>
          </div>
        </div>
        <!-- 两个并排的图表：占满整行，与上方 4 卡片右边缘对齐 -->
        <div class="im-dashboard__charts">
          <a-spin :loading="stat.loading" class="im-dashboard__chart-spin">
            <template v-if="stat.loaded">
              <div class="chart-box">
                <div class="chart-box__header">
                  <span class="chart-box__title">用户注册</span>
                  <a-select
                    v-model="userChartDays"
                    size="small"
                    class="chart-box__select"
                    style="width: 150px"
                    @change="fetchUserChart"
                  >
                    <a-option :value="7">7天</a-option>
                    <a-option :value="15">15天</a-option>
                    <a-option :value="30">30天</a-option>
                    <a-option :value="90">90天</a-option>
                  </a-select>
                </div>
                <div class="chart-box__subtitle">每日用户注册趋势</div>
                <div class="chart-box__body">
                  <Chart class="chart-inner" :options="userChartOption" />
                  <div
                    v-if="userChartData.length === 0"
                    class="chart-box__empty-hint"
                  >
                    暂无注册数据
                  </div>
                </div>
              </div>
              <div class="chart-box">
                <div class="chart-box__header">
                  <span class="chart-box__title">消息统计</span>
                  <a-select
                    v-model="messageChartDays"
                    size="small"
                    class="chart-box__select"
                    style="width: 150px"
                    @change="fetchMessageChart"
                  >
                    <a-option :value="7">7天</a-option>
                    <a-option :value="15">15天</a-option>
                    <a-option :value="30">30天</a-option>
                    <a-option :value="90">90天</a-option>
                  </a-select>
                </div>
                <div class="chart-box__subtitle">每日消息量趋势对比</div>
                <div class="chart-box__body">
                  <Chart class="chart-inner" :options="messageChartOption" />
                  <div
                    v-if="messageChartData.dates.length === 0"
                    class="chart-box__empty-hint"
                  >
                    暂无消息数据
                  </div>
                </div>
              </div>
            </template>
            <a-empty
              v-else-if="!stat.loading && !stat.loaded"
              description="暂无数据或接口未就绪"
              class="chart-empty"
            />
          </a-spin>
        </div>
      </div>
    </div>
  </div>
</template>
  
<script lang="ts" setup>
import { reactive, ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import useChartOption from '@/hooks/chart-option';
import {
  getImUserTotalCount,
  getImUserActiveStats,
  getImUserDailyRegistrationCount,
} from '@/api/im/user';
import { getImGroupTotalCount } from '@/api/im/group';
import { getImPrivateMessageDailyCount } from '@/api/im/privateMessage';
import { getImGroupMessageDailyCount } from '@/api/im/groupMessage';

const stat = reactive({
  loading: false,
  loaded: false,
  totalUserCount: 0,
  totalGroupCount: 0,
  dailyActiveCount: 0,
  monthlyActiveCount: 0,
});

const router = useRouter();
const lastRefreshAt = ref<number>(0);

const userChartDays = ref(7);
const messageChartDays = ref(7);

const userChartData = ref<{ date: string; count: number }[]>([]);
const messageChartData = ref<{
  dates: string[];
  privateData: number[];
  groupData: number[];
}>({ dates: [], privateData: [], groupData: [] });

function parseDateKey(d: string): number {
  if (!d || !d.trim()) return 0;
  let str = d.trim();
  if (str.length === 5 && /^\d{2}-\d{2}$/.test(str))
    str = `${new Date().getFullYear()}-${str}`;
  const t = new Date(str).getTime();
  return Number.isNaN(t) ? 0 : t;
}

const fetchStats = async () => {
  stat.loading = true;
  stat.loaded = false;
  try {
    const [userCountRes, groupCountRes, activeRes] = await Promise.all([
      getImUserTotalCount().catch(() => ({ data: 0 })),
      getImGroupTotalCount().catch(() => ({ data: 0 })),
      getImUserActiveStats().catch(() => ({
        data: { dailyActive: 0, weeklyActive: 0, monthlyActive: 0 },
      })),
    ]);
    stat.totalUserCount = (userCountRes as any).data ?? 0;
    stat.totalGroupCount = (groupCountRes as any).data ?? 0;
    const active = (activeRes as any).data || {};
    stat.dailyActiveCount = active.dailyActive ?? 0;
    stat.monthlyActiveCount = active.monthlyActive ?? 0;
    stat.loaded = true;
    lastRefreshAt.value = Date.now();
  } finally {
    stat.loading = false;
  }
};

const fetchUserChart = async () => {
  try {
    const res = await getImUserDailyRegistrationCount(userChartDays.value);
    const list = (res as any).data || [];
    const arr = Array.isArray(list) ? list : [];
    const withDate = arr.map((it: { date?: string; count?: number }) => ({
      date: it.date ?? '',
      count: typeof it.count === 'number' ? it.count : 0,
    }));
    withDate.sort(
      (a: { date: string }, b: { date: string }) =>
        parseDateKey(a.date) - parseDateKey(b.date)
    );
    userChartData.value = withDate;
  } catch {
    userChartData.value = [];
  }
};

const fetchMessageChart = async () => {
  try {
    const days = messageChartDays.value;
    const [privateRes, groupRes] = await Promise.all([
      getImPrivateMessageDailyCount(days).catch(() => ({ data: [] })),
      getImGroupMessageDailyCount(days).catch(() => ({ data: [] })),
    ]);
    const privateList = (privateRes as any).data || [];
    const groupList = (groupRes as any).data || [];
    const privateArr = Array.isArray(privateList) ? privateList : [];
    const groupArr = Array.isArray(groupList) ? groupList : [];
    const dateSet = new Set<string>();
    privateArr.forEach(
      (it: { date?: string }) => it.date && dateSet.add(it.date)
    );
    groupArr.forEach(
      (it: { date?: string }) => it.date && dateSet.add(it.date)
    );
    const dates = Array.from(dateSet).sort(
      (a, b) => parseDateKey(a) - parseDateKey(b)
    );
    const privateMap = new Map(
      privateArr
        .filter((it): it is { date: string; count?: number } =>
          Boolean(it.date)
        )
        .map((it) => [it.date, it.count ?? 0])
    );
    const groupMap = new Map(
      groupArr
        .filter((it): it is { date: string; count?: number } =>
          Boolean(it.date)
        )
        .map((it) => [it.date, it.count ?? 0])
    );
    messageChartData.value = {
      dates,
      privateData: dates.map((d) => privateMap.get(d) ?? 0),
      groupData: dates.map((d) => groupMap.get(d) ?? 0),
    };
  } catch {
    messageChartData.value = { dates: [], privateData: [], groupData: [] };
  }
};

/** 生成最近 N 天的日期列表，格式 MM-DD，用于 X 轴完整展示 */
function getFullDateRange(days: number): string[] {
  const arr: string[] = [];
  const today = new Date();
  for (let i = days - 1; i >= 0; i -= 1) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    arr.push(
      `${String(d.getMonth() + 1).padStart(2, '0')}-${String(
        d.getDate()
      ).padStart(2, '0')}`
    );
  }
  return arr;
}

/** 将 API 返回的日期转为 MM-DD 便于与完整日期轴对齐 */
function toMMDD(dateStr: string): string {
  if (!dateStr || !dateStr.trim()) return '';
  const str = dateStr.trim();
  if (str.length >= 10) {
    return str.slice(5, 10);
  }
  if (str.length === 5 && /^\d{1,2}-\d{1,2}$/.test(str)) {
    const [m, d] = str.split('-');
    return `${String(Number(m)).padStart(2, '0')}-${String(Number(d)).padStart(
      2,
      '0'
    )}`;
  }
  return str;
}

const { chartOption: userChartOption } = useChartOption(() => {
  const days = userChartDays.value;
  const fullDates = getFullDateRange(days);
  const dataMap = new Map<string, number>();
  userChartData.value.forEach((it) => {
    const key = toMMDD(it.date);
    if (key) dataMap.set(key, it.count ?? 0);
  });
  const counts = fullDates.map((d) => dataMap.get(d) ?? 0);
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.9)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#666', fontSize: 12 },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: fullDates,
      axisLine: { lineStyle: { color: '#999' } },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
      splitLine: { show: false },
    },
    yAxis: {
      type: 'value',
      name: '注册用户数',
      nameTextStyle: { color: '#666', fontSize: 12 },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
      splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
    },
    series: [
      {
        name: '注册用户数',
        type: 'line',
        data: counts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#f7ba2a', borderWidth: 2, borderColor: '#fff' },
        lineStyle: { width: 2, color: '#f7ba2a' },
      },
    ],
  };
});

const { chartOption: messageChartOption } = useChartOption(() => {
  const days = messageChartDays.value;
  const fullDates = getFullDateRange(days);
  const privateMap = new Map<string, number>();
  const groupMap = new Map<string, number>();
  const { dates: apiDates, privateData, groupData } = messageChartData.value;
  apiDates.forEach((d, i) => {
    const key = toMMDD(d);
    if (key) {
      privateMap.set(key, privateData[i] ?? 0);
      groupMap.set(key, groupData[i] ?? 0);
    }
  });
  const privateCounts = fullDates.map((d) => privateMap.get(d) ?? 0);
  const groupCounts = fullDates.map((d) => groupMap.get(d) ?? 0);
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.9)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#666', fontSize: 12 },
    },
    legend: {
      data: ['私聊消息', '群聊消息'],
      top: 5,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#666', fontSize: 12 },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '18%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: fullDates,
      axisLine: { lineStyle: { color: '#999' } },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
      splitLine: { show: false },
    },
    yAxis: {
      type: 'value',
      name: '消息量',
      nameTextStyle: { color: '#666', fontSize: 12 },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
      splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
    },
    series: [
      {
        name: '私聊消息',
        type: 'line',
        data: privateCounts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#165dff', borderWidth: 2, borderColor: '#fff' },
        lineStyle: { width: 2, color: '#165dff' },
      },
      {
        name: '群聊消息',
        type: 'line',
        data: groupCounts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#00b42a', borderWidth: 2, borderColor: '#fff' },
        lineStyle: { width: 2, color: '#00b42a' },
      },
    ],
  };
});

const refreshAll = async () => {
  await Promise.all([fetchStats(), fetchUserChart(), fetchMessageChart()]);
};

const goNoticeManager = () => {
  router.push('/im/notice');
};

const goImUser = () => {
  router.push('/im/user');
};

const goImGroup = () => {
  router.push('/im/group');
};

const goImUserDaily = () => {
  router.push({ path: '/im/user', query: { activeRange: 'daily' } });
};

const goImUserMonthly = () => {
  router.push({ path: '/im/user', query: { activeRange: 'monthly' } });
};

const lastRefreshTimeText = computed(() => {
  if (!lastRefreshAt.value) return '-';
  return new Date(lastRefreshAt.value).toLocaleString('zh-CN', { hour12: false });
});

onMounted(() => {
  refreshAll();
});
</script>
  
  <style scoped lang="less">
.im-dashboard {
  padding: 0 24px 24px;
  padding-right: 32px;
  width: 100%;
  max-width: 100%;
  background: var(--color-fill-2);
  min-height: 100%;
  box-sizing: border-box;
}

.im-dashboard__content {
  width: 100%;
  box-sizing: border-box;
}

.im-dashboard__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.im-dashboard__refresh-time {
  font-size: 12px;
  color: var(--color-text-3);
}

.im-dashboard__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  width: 100%;
  box-sizing: border-box;
}

.im-dashboard__grid .stat-card {
  min-width: 0;
}

.im-dashboard__charts {
  grid-column: 1 / -1;
  width: 100%;
  box-sizing: border-box;
}

.im-dashboard__chart-spin {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  min-height: 420px;
}

.im-dashboard__chart-spin .chart-box {
  min-width: 0;
}

.im-dashboard__chart-spin .chart-empty {
  grid-column: 1 / -1;
  padding: 48px 0;
}

@media (max-width: 992px) {
  .im-dashboard__grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .im-dashboard__chart-spin {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  .im-dashboard__grid {
    grid-template-columns: 1fr;
  }
  .im-dashboard__chart-spin {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 8px;
  background: var(--color-bg-2);
  border: 1px solid var(--color-border-2);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: box-shadow 0.2s, border-color 0.2s;

  &:hover {
    border-color: var(--color-border-3);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.06);
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 52px;
    height: 52px;
    border-radius: 10px;
    flex-shrink: 0;
  }

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__title {
    font-size: 13px;
    color: var(--color-text-3);
    margin-bottom: 4px;
  }

  &__value {
    font-size: 22px;
    font-weight: 600;
    color: var(--color-text-1);
    line-height: 1.2;

    :deep(.arco-statistic-value) {
      font-size: 22px;
      font-weight: 600;
    }
  }

  &--user .stat-card__icon {
    background: rgba(var(--primary-6), 0.15);
    color: rgb(var(--primary-6));
  }

  &--group .stat-card__icon {
    background: rgba(var(--green-6), 0.15);
    color: rgb(var(--green-6));
  }

  &--daily .stat-card__icon {
    background: rgba(var(--orange-6), 0.15);
    color: rgb(var(--orange-6));
  }

  &--monthly .stat-card__icon {
    background: rgba(var(--arcoblue-6), 0.15);
    color: rgb(var(--arcoblue-6));
  }

  &--clickable {
    cursor: pointer;
  }
}

.chart-spin {
  display: block;
  width: 100%;
  min-height: 420px;
}

.chart-empty {
  padding: 48px 0;
}

.chart-row {
  margin-top: 0;
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  align-items: stretch;
}

.chart-col {
  min-width: 0;
  display: flex;
  margin-bottom: 0;
}

/* 图中两个框：左右各一半，等高、带边框、填满区域 */
.chart-box {
  width: 100%;
  flex: 1;
  min-height: 420px;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-2);
  border: 1px solid var(--color-border-2);
  border-radius: 8px;
  box-sizing: border-box;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: nowrap;
    gap: 12px;
    padding: 16px 20px 0;
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text-1);
    flex-shrink: 0;
  }

  &__select {
    width: 64px !important;
    min-width: 64px !important;
    flex-shrink: 0;
    margin-left: auto;

    :deep(.arco-select-view) {
      width: 64px !important;
      min-width: 64px !important;
    }
  }

  &__subtitle {
    font-size: 13px;
    color: var(--color-text-3);
    padding: 10px 20px 0;
    margin-bottom: 12px;
    text-align: center;
  }

  &__body {
    flex: 1;
    min-height: 320px;
    width: 100%;
    padding: 0 20px 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    position: relative;
  }

  &__empty-hint {
    position: absolute;
    bottom: 28px;
    left: 50%;
    transform: translateX(-50%);
    font-size: 12px;
    color: var(--color-text-3);
    pointer-events: none;
  }
}

.chart-inner {
  width: 100% !important;
  height: 100% !important;
  min-height: 300px;
}
</style>
  