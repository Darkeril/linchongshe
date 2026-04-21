<template>
  <a-card
    class="general-card login-location-stats-card"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ padding: '12px 12px 12px' }"
  >
    <template #title>{{ $t('dashboard.loginLocationStats') }}</template>
    <template #extra>
      <a-radio-group
        v-model="days"
        type="button"
        size="small"
        class="login-loc-range"
      >
        <a-radio :value="1">{{ $t('dashboard.today') }}</a-radio>
        <a-radio :value="7">{{ $t('dashboard.last7Days') }}</a-radio>
        <!-- <a-radio :value="30">30日</a-radio> -->
      </a-radio-group>
    </template>
    <a-spin :loading="loading" style="width: 100%">
      <div class="section-label">{{ $t('dashboard.webClient') }}</div>
      <Chart
        v-if="webRows.length > 0"
        height="200px"
        :options="webChartOption"
        :update-options="chartUpdateOpts"
      />
      <a-empty v-else class="mini-empty" :description="$t('dashboard.noRecords')" />

      <div class="section-label section-label--mobile"
        >{{ $t('dashboard.mobileClient') }}</div
      >
      <Chart
        v-if="mobileRows.length > 0"
        height="200px"
        :options="mobileChartOption"
        :update-options="chartUpdateOpts"
      />
      <a-empty v-else class="mini-empty" :description="$t('dashboard.noRecords')" />

      <div class="section-label section-label--admin">{{ $t('dashboard.adminClient') }}</div>
      <Chart
        v-if="adminRows.length > 0"
        height="200px"
        :options="adminChartOption"
        :update-options="chartUpdateOpts"
      />
      <a-empty v-else class="mini-empty" :description="$t('dashboard.noRecords')" />
    </a-spin>
  </a-card>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import type { EChartsOption } from 'echarts';
import Chart from '@/components/chart/index.vue';
import useLoading from '@/hooks/loading';
import {
  getAdminLoginLocationStats,
  getMobileLoginLocationStats,
  getWebLoginLocationStats,
  type LoginLocationStatRow,
} from '@/api/dashboard';

const { t } = useI18n();
const { loading, setLoading } = useLoading(true);
const chartUpdateOpts = { notMerge: true, lazyUpdate: false } as const;

const days = ref<1 | 7 | 30>(1);
const webRows = ref<LoginLocationStatRow[]>([]);
const mobileRows = ref<LoginLocationStatRow[]>([]);
const adminRows = ref<LoginLocationStatRow[]>([]);

function abbrev(s: string, maxLen: number): string {
  const abbrText = (s || '').trim() || t('dashboard.unknown');
  return abbrText.length <= maxLen
    ? abbrText
    : `${abbrText.slice(0, maxLen)}…`;
}

function buildBarOption(
  rows: LoginLocationStatRow[],
  color: string,
  chartKey: string
): EChartsOption {
  const labels = rows.map((r) => abbrev(r.location, 11));
  const vals = rows.map((r) => Number(r.loginCount) || 0);
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter(params: unknown) {
        const arr = params as Array<{
          name: string;
          value: number;
          dataIndex: number;
        }>;
        if (!arr?.length) return '';
        const { dataIndex } = arr[0];
        const raw = rows[dataIndex]?.location ?? arr[0].name;
        const v = arr[0].value;
        return `${raw}<br/>${t('dashboard.loginTimes')}${v}`;
      },
    },
    grid: {
      left: 2,
      right: 32,
      top: 4,
      bottom: 2,
      containLabel: true,
    },
    xAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { fontSize: 10, color: '#888' },
      splitLine: {
        lineStyle: { type: 'dashed', color: 'var(--color-border-2)' },
      },
    },
    yAxis: {
      type: 'category',
      data: labels,
      inverse: true,
      axisLabel: { fontSize: 10, color: '#666' },
      axisLine: { show: false },
      axisTick: { show: false },
    },
    series: [
      {
        id: `login-loc-${chartKey}`,
        type: 'bar',
        data: vals,
        barMaxWidth: 14,
        itemStyle: {
          color,
          borderRadius: [0, 4, 4, 0],
        },
        label: {
          show: true,
          position: 'right',
          fontSize: 10,
          color: '#888',
        },
      },
    ],
  };
}

/** ECharts 画布内用色值；series id 带 days，切换区间时完整重绘 */
const webChartOption = computed(() =>
  buildBarOption(webRows.value, '#165DFF', `web-${days.value}`)
);
const mobileChartOption = computed(() =>
  buildBarOption(mobileRows.value, '#722ED1', `mobile-${days.value}`)
);
const adminChartOption = computed(() =>
  buildBarOption(adminRows.value, '#00B42A', `admin-${days.value}`)
);

async function load() {
  setLoading(true);
  try {
    const d = days.value;
    const webP = getWebLoginLocationStats({ days: d, limit: 8 });
    const mobileP = getMobileLoginLocationStats({ days: d, limit: 8 });
    const adminP = getAdminLoginLocationStats({ days: d, limit: 8 });
    const [w, m, a] = await Promise.allSettled([webP, mobileP, adminP]);

    if (w.status === 'fulfilled' && w.value.code === 200) {
      webRows.value = (w.value.data || []) as LoginLocationStatRow[];
    } else {
      webRows.value = [];
    }
    if (m.status === 'fulfilled' && m.value.code === 200) {
      mobileRows.value = (m.value.data || []) as LoginLocationStatRow[];
    } else {
      mobileRows.value = [];
    }
    if (a.status === 'fulfilled' && a.value.code === 200) {
      adminRows.value = (a.value.data || []) as LoginLocationStatRow[];
    } else {
      adminRows.value = [];
    }
  } finally {
    setLoading(false);
  }
}

onMounted(() => {
  load();
});

watch(days, () => {
  load();
});
</script>

<style scoped lang="less">
.login-loc-range {
  flex-shrink: 0;

  :deep(.arco-radio-button) {
    // padding: 0 1px;
    font-size: 12px;
  }
}

.section-label {
  font-size: 12px;
  color: rgb(var(--gray-7));
  margin-bottom: 6px;
  font-weight: 500;

  &--mobile {
    margin-top: 12px;
  }

  &--admin {
    margin-top: 12px;
  }
}

.mini-empty {
  padding: 12px 0;
  :deep(.arco-empty-image) {
    display: none;
  }
}
</style>
