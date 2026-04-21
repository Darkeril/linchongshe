<template>
  <a-spin :loading="loading" class="note-interaction-spin-root">
    <a-card
      class="general-card note-interaction-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="noteCardBodyStyle"
    >
      <template #title>{{ $t('dashboard.noteInteraction') }}</template>
      <template #extra>
        <a-radio-group v-model="days" type="button" size="small">
          <a-radio :value="7">{{ $t('dashboard.days7') }}</a-radio>
          <a-radio :value="30">{{ $t('dashboard.days30') }}</a-radio>
        </a-radio-group>
      </template>

      <a-row :gutter="10" class="stat-row">
        <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6" :xxl="6">
          <div class="stat-tile">
            <div class="stat-label">{{ $t('dashboard.likeCount') }}</div>
            <div class="stat-value">{{ summary.likeCount }}</div>
            <div class="stat-mom">{{ formatMom(summary.likeMomPct) }}</div>
          </div>
        </a-col>
        <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6" :xxl="6">
          <div class="stat-tile">
            <div class="stat-label">{{ $t('dashboard.commentCount') }}</div>
            <div class="stat-value">{{ summary.commentCount }}</div>
            <div class="stat-mom">{{ formatMom(summary.commentMomPct) }}</div>
          </div>
        </a-col>
        <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6" :xxl="6">
          <div class="stat-tile">
            <div class="stat-label">{{ $t('dashboard.collectCount') }}</div>
            <div class="stat-value">{{ summary.collectionCount }}</div>
            <div class="stat-mom">{{
              formatMom(summary.collectionMomPct)
            }}</div>
          </div>
        </a-col>
        <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6" :xxl="6">
          <div class="stat-tile">
            <div class="stat-label">{{ $t('dashboard.interactionRate') }}</div>
            <div class="stat-value"
              >{{ Number(summary.interactionRatePct || 0).toFixed(2) }}%</div
            >
            <!-- <div class="stat-sub">互动/浏览</div> -->
            <div class="stat-mom">{{ formatMom(summary.rateMomPct) }}</div>
          </div>
        </a-col>
      </a-row>

      <div class="note-interaction-chart-wrap">
        <Chart height="100%" :options="chartOption" />
      </div>
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import useLoading from '@/hooks/loading';
import useChartOption from '@/hooks/chart-option';
import { getNoteInteractionEfficiency } from '@/api/dashboard';
import { percentValueYAxisPartial } from '@/utils/chart-line-axis';
import Chart from '@/components/chart/index.vue';
import trendChartHeight from '../dashboard-chart-sizes';

const { t } = useI18n();

const noteCardBodyStyle = computed(() => ({
  padding: '16px 20px 16px',
  display: 'flex',
  flexDirection: 'column' as const,
  flex: 1,
  minHeight: 0,
}));

const { loading, setLoading } = useLoading(true);
const days = ref<7 | 30>(7);

const summary = ref({
  likeCount: 0,
  commentCount: 0,
  collectionCount: 0,
  interactionRatePct: 0,
  likeMomPct: null as number | null,
  commentMomPct: null as number | null,
  collectionMomPct: null as number | null,
  rateMomPct: null as number | null,
});

const trendLabels = ref<string[]>([]);
const trendRates = ref<number[]>([]);

function formatMom(v: number | null | undefined) {
  if (v === null || v === undefined) return t('dashboard.chainRatioDash');
  const sign = v > 0 ? '+' : '';
  return `${t('dashboard.chainRatio')} ${sign}${v}%`;
}

const { chartOption } = useChartOption(() => {
  const purple = '#722ED1';
  const purpleLight = '#b37feb';
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#eee',
      textStyle: { color: '#666', fontSize: 12 },
      formatter: (params: any) => {
        const p = Array.isArray(params) ? params[0] : params;
        if (!p) return '';
        return `${p.axisValue}<br/>互动率：${p.data}%`;
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: trendLabels.value,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#ddd' } },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      name: t('dashboard.interactionRatePercent'),
      nameTextStyle: { color: '#666', fontSize: 11, padding: [0, 0, 0, 24] },
      ...percentValueYAxisPartial(trendRates.value),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 11 },
      splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
    },
    series: [
      {
        name: t('dashboard.interactionRate'),
        type: 'line',
        data: trendRates.value,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: purple, borderWidth: 2, borderColor: '#fff' },
        lineStyle: { width: 2, color: purpleLight },
      },
    ],
  };
});

async function fetchData() {
  try {
    setLoading(true);
    const res: any = await getNoteInteractionEfficiency(days.value);
    if (res.code !== 200 || !res.data) return;
    const d = res.data;
    const s = d.summary || {};
    summary.value = {
      likeCount: Number(s.likeCount) || 0,
      commentCount: Number(s.commentCount) || 0,
      collectionCount: Number(s.collectionCount) || 0,
      interactionRatePct: Number(s.interactionRatePct) || 0,
      likeMomPct: s.likeMomPct ?? null,
      commentMomPct: s.commentMomPct ?? null,
      collectionMomPct: s.collectionMomPct ?? null,
      rateMomPct: s.rateMomPct ?? null,
    };
    trendLabels.value = Array.isArray(d.trendLabels) ? d.trendLabels : [];
    trendRates.value = Array.isArray(d.trendRates)
      ? d.trendRates.map((n: number) => Number(n) || 0)
      : [];
  } catch (e) {
    console.error(e);
  } finally {
    setLoading(false);
  }
}

watch(days, () => {
  fetchData();
});

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="less">
.note-interaction-spin-root {
  display: flex;
  width: 100%;
  min-width: 0;
  height: 100%;
  min-height: 0;
  flex-direction: column;

  :deep(.arco-spin) {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-height: 0;
    width: 100%;
  }
}

.note-interaction-card {
  display: flex;
  flex: 1;
  width: 100%;
  min-height: 0;
  height: 100%;
  flex-direction: column;

  :deep(.arco-card-body) {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-height: 0;
  }
}

.stat-row {
  flex-shrink: 0;
  margin-bottom: 8px;
}

/* 与左侧一致：占满 body 剩余高度，至少 trendChartHeight */
.note-interaction-chart-wrap {
  flex: 1;
  width: 100%;
  min-width: 0;
  min-height: v-bind(trendChartHeight);
}

.stat-tile {
  background: var(--color-fill-2);
  border-radius: 4px;
  padding: 4px 4px 5px;
  text-align: center;
}

.stat-label {
  font-size: 10px;
  color: var(--color-text-3);
  margin-bottom: 1px;
  line-height: 1.15;
}

.stat-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-1);
  line-height: 1.2;
}

.stat-sub {
  font-size: 10px;
  color: var(--color-text-3);
  margin-top: 1px;
  line-height: 1.2;
}

.stat-mom {
  font-size: 9px;
  color: var(--color-text-3);
  margin-top: 1px;
  line-height: 1.15;
}
</style>
