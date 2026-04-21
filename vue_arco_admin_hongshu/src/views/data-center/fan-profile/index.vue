<template>
  <div class="fan-profile-page">
    <Breadcrumb :items="['数据中心', '粉丝画像']" />
    <a-spin :loading="loading" style="width: 100%">
      <div class="fan-header">
        <div class="overview-cards">
          <div class="stat-card">
            <div class="stat-label">总粉丝数</div>
            <div class="stat-value">{{ overview.totalFans }}</div>
          </div>
          <div class="stat-card stat-card--up">
            <div class="stat-label">新增粉丝数</div>
            <div class="stat-value-row">
              <span class="stat-value">{{ overview.newFans }}</span>
              <IconArrowRise class="stat-icon up" />
            </div>
          </div>
          <div v-if="hasLostFansData" class="stat-card stat-card--down">
            <div class="stat-label">流失粉丝数</div>
            <div class="stat-value-row">
              <span class="stat-value">{{ overview.lostFans ?? '-' }}</span>
              <IconArrowFall class="stat-icon down" />
            </div>
          </div>
        </div>
        <div class="header-toolbar">
          <a-select
            v-model="creatorUid"
            placeholder="选择用户（可输入检索）"
            allow-clear
            allow-search
            :filter-option="filterCreatorOption"
            style="width: 200px; margin-right: 12px"
            size="small"
            @change="onCreatorChange"
          >
            <a-option
              v-for="opt in creatorOptions"
              :key="opt.value"
              :value="opt.value"
              :label="opt.label"
            />
          </a-select>
          <a-radio-group v-model="timeRange" type="button" size="small">
            <a-radio value="7">近7天</a-radio>
            <a-radio value="30">近30天</a-radio>
          </a-radio-group>
        </div>
      </div>

      <!-- 粉丝数据趋势 -->
      <div class="chart-section">
        <div class="chart-box">
          <div class="chart-title">粉丝数据趋势</div>
          <div class="chart-body">
            <Chart class="chart-inner" :options="fanTrendOption" />
          </div>
        </div>
      </div>

      <!-- 粉丝画像 -->
      <div class="profile-section">
        <div class="section-title">粉丝画像</div>
        <div class="profile-grid">
          <div class="chart-box">
            <div class="chart-title">性别分布</div>
            <div class="chart-body chart-body--pie">
              <Chart class="chart-inner" :options="genderOption" />
            </div>
          </div>
          <div class="chart-box">
            <div class="chart-title">年龄分布</div>
            <div class="chart-body">
              <Chart class="chart-inner" :options="ageOption" />
            </div>
          </div>
          <div class="chart-box">
            <div class="chart-title">兴趣分布</div>
            <template v-if="interestTags.length">
              <div class="interest-tags">
                <span
                  v-for="tag in interestTags"
                  :key="tag.name"
                  class="interest-tag"
                >
                  {{ tag.name }}
                </span>
              </div>
            </template>
            <template v-else>
              <div class="interest-empty">暂无数据</div>
            </template>
          </div>
          <div class="chart-box">
            <div class="chart-title">地域分布</div>
            <div class="chart-body">
              <Chart class="chart-inner" :options="regionOption" />
            </div>
          </div>
        </div>
      </div>

      <!-- 粉丝来源 -->
      <div class="source-section">
        <div class="section-title">粉丝来源</div>
        <a-empty description="暂无数据" :image-style="{ height: '80px' }" />
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, watch } from 'vue';
import { IconArrowRise, IconArrowFall } from '@arco-design/web-vue/es/icon';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import useChartOption from '@/hooks/chart-option';
import {
  getFanProfileOverview,
  getFanProfileDistribution,
  getCreatorOptions,
} from '@/api/web/dataCenter';

const timeRange = ref('7');
const loading = ref(false);
const creatorUid = ref<string | undefined>(undefined);
const creatorOptions = ref<{ value: string; label: string }[]>([]);

const overview = ref({
  totalFans: 0,
  newFans: 0,
  lostFans: null as number | null,
});
const hasLostFansData = computed(() => overview.value.lostFans !== null);

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

// 粉丝趋势（由接口填充）
const fanTrendData = ref<{
  dates: string[];
  total: number[];
  newFans: number[];
  lostFans: number[];
}>({
  dates: [],
  total: [],
  newFans: [],
  lostFans: [],
});
const fullDates = computed(() =>
  fanTrendData.value.dates?.length
    ? fanTrendData.value.dates
    : getFullDateRange(Number(timeRange.value))
);

async function fetchOverview() {
  loading.value = true;
  try {
    const res: any = await getFanProfileOverview({
      uid: creatorUid.value,
      days: Number(timeRange.value),
    });
    const data = res?.data ?? res;
    if (!data) return;
    overview.value = {
      totalFans: data.totalFans ?? 0,
      newFans: data.newFans ?? 0,
      lostFans:
        typeof data.lostFans === 'number' ? data.lostFans : null,
    };
    fanTrendData.value = {
      dates: data.dates ?? [],
      total: data.total ?? [],
      newFans: data.newFansTrend ?? [],
      lostFans: data.lostFansTrend ?? [],
    };
  } catch (e) {
    console.error('粉丝画像概览请求失败', e);
  } finally {
    loading.value = false;
  }
}

async function fetchDistribution() {
  try {
    const res: any = await getFanProfileDistribution({ uid: creatorUid.value });
    const data = res?.data ?? res;
    if (!data) return;
    if (Array.isArray(data.gender)) genderData.value = data.gender;
    if (Array.isArray(data.age)) ageData.value = data.age;
    if (Array.isArray(data.region)) regionData.value = data.region;
    if (Array.isArray(data.interest)) {
      // 随机打乱兴趣列表，生成标签云（不区分样式，保持统一颜色）
      const src = (data.interest as { name: string; value: number }[]).slice();
      src.sort(() => Math.random() - 0.5);
      interestTags.value = src.map((item) => ({
        name: item.name,
      }));
    }
  } catch (e) {
    console.error('粉丝画像分布请求失败', e);
  }
}

function onCreatorChange() {
  fetchOverview();
  fetchDistribution();
}

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
  fetchOverview();
  fetchDistribution();
});
watch(timeRange, () => {
  fetchOverview();
});

const { chartOption: fanTrendOption } = useChartOption(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255,255,255,0.9)',
    borderColor: '#eee',
    borderWidth: 1,
  },
  legend: {
    data: hasLostFansData.value
      ? ['总粉丝数', '新增粉丝数', '流失粉丝数']
      : ['总粉丝数', '新增粉丝数'],
    bottom: 0,
    itemWidth: 12,
    itemHeight: 12,
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '40px',
    top: '10%',
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
    min: (value: { min: number }) => Math.max(0, value.min - 5),
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#666', fontSize: 12 },
    splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
  },
  series: [
    {
      name: '总粉丝数',
      type: 'line',
      data: fanTrendData.value.total,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: { color: '#165dff', borderWidth: 2, borderColor: '#fff' },
      lineStyle: { width: 2, color: '#165dff' },
    },
    {
      name: '新增粉丝数',
      type: 'line',
      data: fanTrendData.value.newFans,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: { color: '#ff7d00', borderWidth: 2, borderColor: '#fff' },
      lineStyle: { width: 2, color: '#ff7d00' },
    },
    ...(hasLostFansData.value
      ? [
          {
            name: '流失粉丝数',
            type: 'line',
            data: fanTrendData.value.lostFans,
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            itemStyle: { color: '#86909c', borderWidth: 2, borderColor: '#fff' },
            lineStyle: { width: 2, color: '#86909c' },
          },
        ]
      : []),
  ],
}));

// 性别分布（由接口填充）
const genderData = ref<{ name: string; value: number }[]>([]);

const { chartOption: genderOption } = useChartOption(() => {
  const raw = genderData.value || [];

  // 参考内容分析-观众画像：固定三类并放在左侧
  const buckets = [
    { name: '男性', color: '#165dff' },
    { name: '女性', color: '#f53f3f' },
    { name: '未知', color: '#86909c' },
  ];

  if (!raw.length) {
    return {
      tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
      color: ['#165dff'],
      legend: {
        orient: 'vertical',
        left: 10,
        top: 'middle',
        data: ['暂无数据'],
        itemGap: 12,
        itemWidth: 12,
        itemHeight: 12,
        textStyle: { fontSize: 12 },
      },
      series: [
        {
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['55%', '50%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 4,
            borderColor: '#fff',
            borderWidth: 2,
          },
          label: {
            show: true,
            position: 'center',
            formatter: '暂无数据',
            fontSize: 12,
          },
          labelLine: { show: false },
          data: [{ name: '暂无数据', value: 1 }],
        },
      ],
    };
  }

  const map = raw.reduce<Record<string, number>>((acc, cur) => {
    const key = cur.name;
    if (!key) return acc;
    acc[key] = (acc[key] || 0) + (cur.value || 0);
    return acc;
  }, {});

  const data = buckets.map((b) => ({
    name: b.name,
    value: map[b.name] ?? 0,
    itemStyle: { color: b.color },
  }));

  const hasRealData = data.some((d) => d.value > 0);

  return {
    tooltip: { trigger: 'item' },
    color: ['#165dff', '#f53f3f', '#86909c'],
    legend: {
      orient: 'vertical',
      left: 10,
      top: 'middle',
      data: buckets.map((b) => b.name),
      itemGap: 12,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 12 },
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['55%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: true,
          position: hasRealData ? 'outside' : 'center',
          formatter: hasRealData ? '{b}: {d}%' : '暂无数据',
          fontSize: 12,
          distanceToLabelLine: 4,
        },
        labelLine: {
          show: hasRealData,
          length: 8,
          length2: 6,
          smooth: false,
        },
        data,
      },
    ],
  };
});

// 年龄分布（由接口填充，为人数值）
const ageData = ref<{ name: string; value: number }[]>([]);

const { chartOption: ageOption } = useChartOption(() => {
  // 期望固定的年龄段顺序，缺失的区间补 0，展示百分比
  const buckets = ['<18', '18-24', '25-34', '35-44', '>44'];

  if (!ageData.value.length) {
    return {
      tooltip: { trigger: 'axis' },
      grid: {
        left: '6%',
        right: '4%',
        bottom: '8%',
        top: '10%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: ['暂无数据'],
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
      series: [
        {
          type: 'bar',
          data: [0],
          barWidth: 28,
          itemStyle: { color: '#165dff' },
        },
      ],
    };
  }

  const map = ageData.value.reduce<Record<string, number>>((acc, cur) => {
    acc[cur.name] = (acc[cur.name] || 0) + (cur.value || 0);
    return acc;
  }, {});
  const total = Object.values(map).reduce((s, v) => s + v, 0) || 1;

  const categories: string[] = [];
  const values: number[] = [];
  buckets.forEach((b) => {
    categories.push(b);
    const raw = map[b] || 0;
    const pct = (raw / total) * 100;
    values.push(Number(pct.toFixed(2)));
  });

  return {
    tooltip: {
      trigger: 'axis',
      formatter(params: any) {
        const first = Array.isArray(params) ? params[0] : params;
        return `${first.axisValue}<br/>${first.seriesName}: ${first.data}%`;
      },
    },
    grid: {
      left: '6%',
      right: '4%',
      bottom: '8%',
      top: '10%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: { lineStyle: { color: '#999' } },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: '#666',
        fontSize: 12,
        formatter: '{value}%',
      },
      splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
    },
    series: [
      {
        name: '占比',
        type: 'bar',
        data: values,
        barWidth: 28,
        itemStyle: { color: '#165dff' },
      },
    ],
  };
});

// 地域分布（由接口填充，为人数值）
const regionData = ref<{ name: string; value: number }[]>([]);

const { chartOption: regionOption } = useChartOption(() => ({
  tooltip: { trigger: 'axis' },
  grid: {
    left: '15%',
    right: '10%',
    bottom: '3%',
    top: '5%',
    containLabel: true,
  },
  xAxis: {
    type: 'value',
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: {},
    splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
  },
  yAxis: {
    type: 'category',
    data: regionData.value.length
      ? regionData.value.map((i) => i.name)
      : ['暂无数据'],
    axisLine: { lineStyle: { color: '#999' } },
    axisTick: { show: false },
    axisLabel: { color: '#666', fontSize: 12 },
  },
  series: [
    {
      type: 'bar',
      data: regionData.value.length
        ? regionData.value.map((i) => i.value)
        : [0],
      barWidth: '60%',
      itemStyle: { color: '#165dff' },
    },
  ],
}));

// 兴趣分布（由接口填充）
const interestTags = ref<{ name: string; active: boolean }[]>([]);
</script>

<script lang="ts">
export default {
  name: 'DataCenterFanProfile',
};
</script>

<style lang="less" scoped>
.fan-profile-page {
  padding: 0 20px 20px;
  background-color: var(--color-fill-2);
  min-height: 100%;
}

.fan-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
  padding: 20px;
  background: var(--color-bg-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
}

.overview-cards {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.stat-card {
  padding: 16px 24px;
  background: var(--color-fill-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
  min-width: 120px;

  &--up .stat-icon.up {
    color: rgb(var(--orange-6));
  }

  &--down .stat-icon.down {
    color: var(--color-text-3);
  }
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-3);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-1);
}

.stat-value-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-icon {
  font-size: 18px;
}

.header-toolbar {
  flex-shrink: 0;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-box {
  background: var(--color-bg-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
  padding: 20px;
  margin-bottom: 16px;

  &.full-width {
    grid-column: 1 / -1;
  }
}

.chart-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-1);
  margin-bottom: 12px;
}

.chart-hint {
  font-size: 12px;
  color: var(--color-text-3);
  margin-bottom: 8px;
}

.chart-body {
  height: 260px;
  min-height: 220px;

  &--pie {
    height: 240px;
    min-height: 200px;
  }
}

.chart-inner {
  width: 100% !important;
  height: 100% !important;
}

.profile-section {
  background: var(--color-bg-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
  padding: 20px;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-1);
  margin-bottom: 16px;
}

.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.interest-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 18px 22px;
  justify-content: center;
  align-items: center;
  min-height: 230px; // 在卡片中垂直居中一些
}

.interest-tag {
  padding: 8px 20px;
  font-size: 22px;
  color: #3d8af5;
  font-weight: 600;
  background: rgba(22, 93, 255, 0.06);
  border-radius: 999px;
  border: none;
  line-height: 1;
  white-space: nowrap;
  box-shadow: 0 0 0 1px rgba(22, 93, 255, 0.08);
}

/* 大小不一 + 上下错位，营造漂浮感，但颜色样式保持一致 */
.interest-tag:nth-child(3n + 1) {
  transform: translateY(-6px);
  padding: 10px 26px;
  font-size: 16px;
}

.interest-tag:nth-child(3n + 2) {
  transform: translateY(4px);
}

.interest-tag:nth-child(5n) {
  transform: translateY(8px);
  padding-inline: 22px;
}

.interest-empty {
  min-height: 230px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--color-text-3);
}

.source-section {
  background: var(--color-bg-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
  padding: 40px 20px;
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .fan-header {
    flex-direction: column;
  }
}
</style>
