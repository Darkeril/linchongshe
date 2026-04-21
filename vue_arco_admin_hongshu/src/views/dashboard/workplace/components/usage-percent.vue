<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="{ padding: '10px' }"
    >
      <template #title>
        {{ $t('dashboard.aiUsageAnalysis') }}
      </template>
      <Chart height="320px" :option="chartOption" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import useLoading from '@/hooks/loading';
import useChartOption from '@/hooks/chart-option';
import { getUsagePieData } from '@/api/dashboard';
import {
  foldPieOthers,
  pieItemTooltipFormatter,
  pieOutsidePercentFormatter,
} from '@/utils/chart-pie';

const { t } = useI18n();
const { loading } = useLoading();
const total = ref(0);
const pieData = ref<{ name: string; value: number }[]>([]);

// 与项目 5 对齐：后端返回 a-f，需要映射为中文名称
const KEY_TO_NAME: Record<string, string> = {
  a: 'ChatGPT',
  b: '文心一言',
  c: '智谱清言',
  d: '通义千问',
  e: '讯飞星火',
  f: '月之暗面',
};
// 固定顺序，保证展示一致
const KEY_ORDER = ['a', 'b', 'c', 'd', 'e', 'f'];

function normalizeToArray(data: any): { name: string; value: number }[] {
  if (Array.isArray(data)) return data as any;
  if (data && typeof data === 'object') {
    const keys = Object.keys(data);
    const ordered = KEY_ORDER.filter((k) => keys.includes(k));
    const rest = keys.filter((k) => !ordered.includes(k));
    const finalKeys = [...ordered, ...rest];
    return finalKeys.map((k) => ({
      name: KEY_TO_NAME[k] || k,
      value: Number((data as any)[k]) || 0,
    }));
  }
  return [];
}

async function fetchData() {
  try {
    const res = await getUsagePieData();
    const raw = res?.data ?? res;
    const list = normalizeToArray(raw);
    pieData.value = list;
    total.value = list.reduce((sum, it) => sum + (Number(it.value) || 0), 0);
  } catch (e) {
    console.error('Failed to load usage pie data:', e);
  }
}

/** 模型种类不多时保持原样；增多时同样合并长尾 */
const displayPieData = computed(() =>
  foldPieOthers(pieData.value, { maxItems: 8, othersLabel: t('dashboard.other') })
);

const { chartOption } = useChartOption((isDark) => {
  return {
    legend: {
      type: 'scroll',
      orient: 'horizontal',
      left: 'center',
      data: displayPieData.value.map((i) => i.name),
      bottom: 10,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      pageButtonPosition: 'end',
      textStyle: {
        color: isDark ? 'rgba(255,255,255,0.7)' : '#4E5969',
        fontSize: 12,
      },
      itemStyle: { borderWidth: 0 },
    },
    tooltip: {
      show: true,
      trigger: 'item',
      confine: true,
      formatter: pieItemTooltipFormatter,
    },
    graphic: {
      elements: [
        {
          type: 'text',
          left: 'center',
          top: '37%',
          style: {
            text: t('dashboard.total'),
            textAlign: 'center',
            fill: isDark ? '#ffffffb3' : '#4E5969',
            fontSize: 14,
          },
        },
        {
          type: 'text',
          left: 'center',
          top: '43%',
          style: {
            text: total.value,
            textAlign: 'center',
            fill: isDark ? '#ffffffb3' : '#1D2129',
            fontSize: 16,
            fontWeight: 500,
          },
        },
      ],
    },
    series: [
      {
        type: 'pie',
        radius: ['30%', '60%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: true,
        labelLayout: { hideOverlap: true },
        label: {
          show: true,
          position: 'outside',
          formatter: pieOutsidePercentFormatter,
          fontSize: 12,
          color: isDark ? 'rgba(255, 255, 255, 0.75)' : '#4E5969',
        },
        labelLine: {
          length: 10,
          length2: 6,
          smooth: 0.2,
        },
        emphasis: {
          label: { show: true, fontSize: 12 },
        },
        itemStyle: {
          borderColor: isDark ? '#232324' : '#fff',
          borderWidth: 1,
        },
        data: displayPieData.value,
      },
    ],
  };
});

onMounted(fetchData);
</script>

<style scoped lang="less"></style>




