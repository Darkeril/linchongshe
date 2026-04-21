<template>
  <div class="analytics-page">
    <Breadcrumb :items="['数据中心', '数据分析']" />
    <a-spin :loading="loading" style="width: 100%">
      <div class="analytics-header">
        <a-tabs
          v-model:active-key="activeTab"
          type="text"
          class="analytics-tabs"
        >
          <a-tab-pane key="view" title="观看数据" />
          <a-tab-pane key="interact" title="互动数据" />
          <a-tab-pane key="fans" title="涨粉数据" />
          <a-tab-pane key="publish" title="发布数据" />
        </a-tabs>
        <div class="analytics-toolbar">
          <a-select
            v-model="selectedUid"
            placeholder="选择用户（可输入检索）"
            allow-clear
            allow-search
            :filter-option="filterCreatorOption"
            style="width: 200px"
            size="small"
            @change="handleTimeChange"
          >
            <a-option
              v-for="opt in creatorOptions"
              :key="opt.value"
              :value="opt.value"
              :label="opt.label"
            />
          </a-select>
          <a-radio-group
            v-model="timeRange"
            type="button"
            size="small"
            @change="handleTimeChange"
          >
            <a-radio value="7">近7日</a-radio>
            <a-radio value="30">近30日</a-radio>
          </a-radio-group>
          <!-- <a-button type="primary" size="small">
            <template #icon><icon-download /></template>
            导出数据
          </a-button> -->
        </div>
      </div>

      <!-- 观看数据 -->
      <div v-show="activeTab === 'view'" class="analytics-content">
        <div class="data-overview">
          <div class="overview-title">
            <span>数据总览</span>
            <a-tooltip content="统计周期内的核心指标">
              <icon-question-circle
                style="margin-left: 4px; color: var(--color-text-3)"
              />
            </a-tooltip>
            <span class="overview-period">统计周期 {{ dateRangeText }}</span>
          </div>
          <div class="overview-cards">
            <div
              v-for="item in viewData.overview"
              :key="item.label"
              class="stat-card"
            >
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div
                class="stat-ring"
                :class="{ up: item.ring > 0, down: item.ring < 0 }"
              >
                {{ item.ringText }}
              </div>
            </div>
          </div>
        </div>
        <div class="chart-section">
          <div class="chart-box">
            <div class="chart-title">观看数据趋势</div>
            <div class="chart-body">
              <Chart class="chart-inner" :options="viewTrendOption" />
            </div>
          </div>
        </div>
        <div class="chart-row">
          <div class="chart-box half">
            <div class="chart-title">观看来源</div>
            <div class="chart-subtitle">统计周期 {{ dateRangeText }}</div>
            <div class="chart-body">
              <Chart class="chart-inner" :options="viewSourceOption" />
            </div>
          </div>
          <div class="chart-box half">
            <div class="chart-title">观看时段</div>
            <div class="chart-subtitle">统计周期 {{ dateRangeText }}</div>
            <div class="chart-body">
              <Chart class="chart-inner" :options="viewTimeOption" />
            </div>
          </div>
        </div>
      </div>

      <!-- 互动数据 -->
      <div v-show="activeTab === 'interact'" class="analytics-content">
        <div class="data-overview">
          <div class="overview-title">
            <span>数据总览</span>
            <a-tooltip content="统计周期内的核心指标">
              <icon-question-circle
                style="margin-left: 4px; color: var(--color-text-3)"
              />
            </a-tooltip>
            <span class="overview-period">统计周期 {{ dateRangeText }}</span>
          </div>
          <div class="overview-cards">
            <div
              v-for="item in interactData.overview"
              :key="item.label"
              class="stat-card"
            >
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div
                class="stat-ring"
                :class="{ up: item.ring > 0, down: item.ring < 0 }"
              >
                {{ item.ringText }}
              </div>
            </div>
          </div>
        </div>
        <div class="chart-section">
          <div class="chart-box">
            <div class="chart-title">互动数据趋势</div>
            <div class="chart-body">
              <Chart class="chart-inner" :options="interactTrendOption" />
            </div>
          </div>
        </div>
      </div>

      <!-- 涨粉数据 -->
      <div v-show="activeTab === 'fans'" class="analytics-content">
        <div class="data-overview">
          <div class="overview-title">
            <span>数据总览</span>
            <a-tooltip content="统计周期内的核心指标">
              <icon-question-circle
                style="margin-left: 4px; color: var(--color-text-3)"
              />
            </a-tooltip>
            <span class="overview-period">统计周期 {{ dateRangeText }}</span>
          </div>
          <div class="overview-cards">
            <div
              v-for="item in fansData.overview"
              :key="item.label"
              class="stat-card"
            >
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div
                class="stat-ring"
                :class="{ up: item.ring > 0, down: item.ring < 0 }"
              >
                {{ item.ringText }}
              </div>
            </div>
          </div>
        </div>
        <div class="chart-section">
          <div class="chart-box">
            <div class="chart-title">涨粉数据趋势</div>
            <div class="chart-body">
              <Chart class="chart-inner" :options="fansTrendOption" />
            </div>
          </div>
        </div>
      </div>

      <!-- 发布数据 -->
      <div v-show="activeTab === 'publish'" class="analytics-content">
        <div class="data-overview">
          <div class="overview-title">
            <span>数据总览</span>
            <a-tooltip content="统计周期内的核心指标">
              <icon-question-circle
                style="margin-left: 4px; color: var(--color-text-3)"
              />
            </a-tooltip>
            <span class="overview-period">统计周期 {{ dateRangeText }}</span>
          </div>
          <div class="overview-cards">
            <div
              v-for="item in publishData.overview"
              :key="item.label"
              class="stat-card"
            >
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div
                class="stat-ring"
                :class="{ up: item.ring > 0, down: item.ring < 0 }"
              >
                {{ item.ringText }}
              </div>
            </div>
          </div>
        </div>
        <div class="chart-section">
          <div class="chart-box">
            <div class="chart-title">发布数据趋势</div>
            <div class="chart-body">
              <Chart class="chart-inner" :options="publishTrendOption" />
            </div>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import useChartOption from '@/hooks/chart-option';
import { getAnalytics, getCreatorOptions } from '@/api/web/dataCenter';

const activeTab = ref('view');
const timeRange = ref('7');
const loading = ref(false);
const selectedUid = ref<string | undefined>(undefined);
const creatorOptions = ref<{ value: string; label: string }[]>([]);

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

function getDateRangeText(days: number): string {
  const dates = getFullDateRange(days);
  if (dates.length === 0) return '';
  return `${dates[0]} 至 ${dates[dates.length - 1]}`;
}

const dateRangeText = computed(() => getDateRangeText(Number(timeRange.value)));

const daysNum = computed(() => Number(timeRange.value));
const fullDates = computed(() => getFullDateRange(daysNum.value));

const viewData = ref({
  overview: [] as {
    label: string;
    value: number | string;
    ring: number;
    ringText: string;
  }[],
  trend: [] as number[],
  source: [] as { name: string; value: number }[],
  timeSlot: [] as number[],
});

const interactData = ref({
  overview: [] as {
    label: string;
    value: number | string;
    ring: number;
    ringText: string;
  }[],
  trend: [] as number[],
});

const fansData = ref({
  overview: [] as {
    label: string;
    value: number | string;
    ring: number;
    ringText: string;
  }[],
  trend: [] as number[],
});

const publishData = ref({
  overview: [] as {
    label: string;
    value: number | string;
    ring: number;
    ringText: string;
  }[],
  trend: [] as number[],
});

// 观看来源名称映射：后端英文 key -> 前端中文展示
const viewSourceNameMap: Record<string, string> = {
  other: '其它',
  search: '搜索',
  profile: '个人主页',
  recommend: '推荐',
};

const viewSourceData = computed(() => {
  const source = viewData.value.source || [];
  if (!Array.isArray(source) || source.length === 0) {
    return [];
  }
  return source.map((item) => ({
    ...item,
    name: viewSourceNameMap[item.name] || item.name,
  }));
});

async function fetchAnalytics() {
  loading.value = true;
  try {
    const res: any = await getAnalytics({
      days: Number(timeRange.value),
      uid: selectedUid.value || undefined,
    });
    const data = res?.data ?? res;
    if (!data) return;
    if (data.view) {
      viewData.value.overview = data.view.overview ?? [];
      viewData.value.trend = (data.view.trend ?? []).map((v: number) =>
        Number(v)
      );
      viewData.value.source = (data.view.source ?? []).map(
        (s: { name: string; value: number }) => ({
          name: s.name,
          value: Number(s.value ?? 0),
        })
      );
      viewData.value.timeSlot = (data.view.timeSlot ?? []).map((v: number) =>
        Number(v)
      );
      if (viewData.value.timeSlot.length !== 24) {
        viewData.value.timeSlot = Array.from(
          { length: 24 },
          (_, i) => viewData.value.timeSlot[i] ?? 0
        );
      }
    }
    if (data.interact) {
      interactData.value.overview = data.interact.overview ?? [];
      interactData.value.trend = (data.interact.trend ?? []).map((v: number) =>
        Number(v)
      );
    }
    if (data.fans) {
      fansData.value.overview = data.fans.overview ?? [];
      fansData.value.trend = (data.fans.trend ?? []).map((v: number) =>
        Number(v)
      );
    }
    if (data.publish) {
      publishData.value.overview = data.publish.overview ?? [];
      publishData.value.trend = (data.publish.trend ?? []).map((v: number) =>
        Number(v)
      );
    }
  } catch (e) {
    console.error('数据分析请求失败', e);
  } finally {
    loading.value = false;
  }
}

const handleTimeChange = () => {
  fetchAnalytics();
};

/** 选择用户下拉：按输入内容匹配昵称或用户ID */
function filterCreatorOption(
  inputValue: string,
  option: { value: string; label: string }
) {
  if (!inputValue || !inputValue.trim()) return true;
  const kw = inputValue.trim().toLowerCase();
  const label = String(option?.label ?? '').toLowerCase();
  const value = String(option?.value ?? '').toLowerCase();
  return label.includes(kw) || value.includes(kw);
}

async function loadCreatorOptions() {
  try {
    const res: any = await getCreatorOptions();
    const data = res?.data ?? res;
    const rows = data?.rows ?? data?.list ?? [];
    creatorOptions.value = (Array.isArray(rows) ? rows : [])
      .map((r: any) => ({
        value: String(r.id ?? r.uid ?? ''),
        label: r.username ?? r.nickname ?? String(r.id ?? r.uid ?? ''),
      }))
      .filter((o: { value: string }) => o.value);
  } catch (e) {
    console.error('加载用户列表失败', e);
  }
}

onMounted(() => {
  loadCreatorOptions();
  fetchAnalytics();
});

const baseLineChartOption = () => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255,255,255,0.9)',
    borderColor: '#eee',
    borderWidth: 1,
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
    data: fullDates.value,
    axisLine: { lineStyle: { color: '#999' } },
    axisTick: { show: false },
    axisLabel: { color: '#666', fontSize: 12 },
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#666', fontSize: 12 },
    splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
  },
});

const { chartOption: viewTrendOption } = useChartOption(() => {
  const opt = baseLineChartOption() as any;
  opt.series = [
    {
      name: '观看数',
      type: 'line',
      data: viewData.value.trend,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: { color: 'rgba(22, 93, 255, 0.15)' },
      itemStyle: { color: '#165dff', borderWidth: 2, borderColor: '#fff' },
      lineStyle: { width: 2, color: '#165dff' },
    },
  ];
  return opt;
});

const { chartOption: viewSourceOption } = useChartOption(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 10, left: 'center' },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2,
      },
      label: { show: true, formatter: '{b} {d}%' },
      data: viewSourceData.value.length
        ? viewSourceData.value
        : [{ name: '暂无数据', value: 1 }],
      color: ['#f53f3f', '#ff7d00', '#ffb400', '#00b42a'],
    },
  ],
}));

const { chartOption: viewTimeOption } = useChartOption(() => ({
  tooltip: { trigger: 'axis' },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '10%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: Array.from(
      { length: 24 },
      (_, i) => `${String(i).padStart(2, '0')}:00`
    ),
    axisLabel: { fontSize: 10, interval: 1 },
  },
  yAxis: { type: 'value' },
  series: [
    {
      type: 'bar',
      data: viewData.value.timeSlot.length
        ? viewData.value.timeSlot
        : Array(24).fill(0),
      itemStyle: { color: '#165dff' },
      barWidth: '60%',
    },
  ],
}));

const { chartOption: interactTrendOption } = useChartOption(() => {
  const opt = baseLineChartOption() as any;
  opt.series = [
    {
      name: '互动数',
      type: 'line',
      data: interactData.value.trend,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: { color: 'rgba(0, 180, 42, 0.15)' },
      itemStyle: { color: '#00b42a', borderWidth: 2, borderColor: '#fff' },
      lineStyle: { width: 2, color: '#00b42a' },
    },
  ];
  return opt;
});

const { chartOption: fansTrendOption } = useChartOption(() => {
  const opt = baseLineChartOption() as any;
  opt.series = [
    {
      name: '净涨粉',
      type: 'line',
      data: fansData.value.trend,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: { color: 'rgba(114, 46, 209, 0.15)' },
      itemStyle: { color: '#722ed1', borderWidth: 2, borderColor: '#fff' },
      lineStyle: { width: 2, color: '#722ed1' },
    },
  ];
  return opt;
});

const { chartOption: publishTrendOption } = useChartOption(() => {
  const opt = baseLineChartOption() as any;
  opt.series = [
    {
      name: '发布数',
      type: 'line',
      data: publishData.value.trend,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: { color: 'rgba(245, 63, 63, 0.15)' },
      itemStyle: { color: '#f53f3f', borderWidth: 2, borderColor: '#fff' },
      lineStyle: { width: 2, color: '#f53f3f' },
    },
  ];
  return opt;
});

onMounted(() => {
  // 可在此处调用 API 获取真实数据
});
</script>

<script lang="ts">
export default {
  name: 'DataCenterAnalytics',
};
</script>

<style lang="less" scoped>
.analytics-page {
  padding: 0 20px 20px;
  background-color: var(--color-fill-2);
  min-height: 100%;
}

.analytics-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: var(--color-bg-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
}

.analytics-tabs {
  flex: 1;
  display: flex;
  align-items: center;
  :deep(.arco-tabs-header) {
    margin-bottom: 0;
    align-items: center;
  }
  :deep(.arco-tabs-pane) {
    padding: 0;
  }
}

.analytics-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.analytics-content {
  background: var(--color-bg-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
  padding: 20px;
}

.data-overview {
  margin-bottom: 24px;
}

.overview-title {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-1);
}

.overview-period {
  margin-left: auto;
  font-size: 12px;
  color: var(--color-text-3);
  font-weight: normal;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 16px;
  background: var(--color-fill-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-3);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-1);
}

.stat-ring {
  font-size: 12px;
  color: var(--color-text-3);
  margin-top: 4px;
  &.up {
    color: rgb(var(--red-6));
  }
  &.down {
    color: rgb(var(--green-6));
  }
}

.chart-section {
  margin-bottom: 24px;
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-box {
  background: var(--color-fill-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
  padding: 16px;
  margin-bottom: 16px;

  &.half {
    margin-bottom: 0;
  }
}

.chart-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-1);
  margin-bottom: 4px;
}

.chart-subtitle {
  font-size: 12px;
  color: var(--color-text-3);
  margin-bottom: 12px;
}

.chart-body {
  height: 320px;
  min-height: 280px;
}

.chart-inner {
  width: 100% !important;
  height: 100% !important;
}

@media (max-width: 768px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
}
</style>
