<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="{ padding: '20px' }"
    >
      <template #title>{{ $t('dashboard.orderSalesTrend') }}</template>
      <template #extra>
        <a-radio-group v-model="days" type="button" size="small">
          <a-radio :value="7">{{ $t('dashboard.days7') }}</a-radio>
          <a-radio :value="30">{{ $t('dashboard.days30') }}</a-radio>
        </a-radio-group>
      </template>
      <Chart
        height="350px"
        :options="chartOption"
        :update-options="chartUpdateOptions"
      />
    </a-card>
  </a-spin>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import useLoading from '@/hooks/loading';
import { getOrderData } from '@/api/dashboard';
import { countValueYAxisPartial } from '@/utils/chart-line-axis';
import { alignSeriesToAxis, buildRollingDayAxis } from '../rolling-axis';

const { t } = useI18n();
const { loading, setLoading } = useLoading(true);
const days = ref<7 | 30>(7);
const chartUpdateOptions = { notMerge: true, lazyUpdate: false } as const;

/** 横轴展示用 MM-dd，与「笔记互动效率」一致 */
const chartXLabels = ref<string[]>([]);

const orderData = ref<{
  date: string[];
  data: Record<string, number[]>;
}>({
  date: [],
  data: {},
});

function applyOrderPayload(data: any) {
  const axis = buildRollingDayAxis(days.value);
  chartXLabels.value = axis.map((a) => a.label);
  const apiDates = data.dates || [];
  const categoryData = data.categoryData || {};
  const aligned: Record<string, number[]> = {};
  Object.keys(categoryData).forEach((cat) => {
    aligned[cat] = alignSeriesToAxis(
      axis,
      apiDates,
      categoryData[cat] as number[],
      0
    ).map((n) => Number(n) || 0);
  });
  orderData.value = {
    date: axis.map((a) => a.raw),
    data: aligned,
  };
}

const chartOption = computed(() => {
  const rangeDays = days.value;
  const categories = Object.keys(orderData.value.data);
  const allValues = categories.flatMap(
    (c) => orderData.value.data[c] || []
  ) as number[];

  // 定义渐变色
  const colors = [
    ['#83bff6', '#188df0'],
    ['#9fe6b8', '#32c87d'],
    ['#FFB6C1', '#FF69B4'],
    ['#87CEFA', '#1E90FF'],
    ['#98FB98', '#32CD32'],
    ['#DDA0DD', '#9370DB'],
  ];

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.9)',
      borderColor: '#eee',
      borderWidth: 1,
      padding: [10, 15],
      textStyle: {
        color: '#666',
        fontSize: 12,
      },
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999',
          width: 1,
          type: 'dashed',
        },
        label: {
          backgroundColor: '#6a7985',
        },
      },
      formatter(params: any) {
        if (!params || !params.length) return '';
        let result = `${params[0].axisValue}<br/>`;
        params.forEach((item: any) => {
          result += `
            <div style="display: flex; justify-content: space-between; align-items: center; margin: 3px 0;">
              <span style="display:inline-block;margin-right:5px;border-radius:50%;width:10px;height:10px;background-color:${item.color};"></span>
              <span style="flex: 1;">${item.seriesName}:</span>
              <span style="font-weight:bold;margin-left:5px;">${item.value}</span>
            </div>
          `;
        });
        return result;
      },
    },
    legend: {
      data: categories,
      top: 0,
      type: 'scroll',
      padding: [0, 100],
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        color: '#666',
        fontSize: 12,
      },
      pageIconColor: '#666',
      pageTextStyle: {
        color: '#666',
      },
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
      data: chartXLabels.value.slice(),
      axisLine: { lineStyle: { color: '#ddd' } },
      axisTick: { show: false },
      axisLabel: {
        color: '#666',
        fontSize: 11,
        rotate: rangeDays === 30 ? 35 : 0,
        interval: rangeDays === 30 ? 2 : 0,
        margin: 12,
      },
      splitLine: {
        show: false,
      },
    },
    yAxis: {
      type: 'value',
      name: t('dashboard.salesQuantity'),
      ...countValueYAxisPartial(allValues, { emptyMax: 10 }),
      nameTextStyle: {
        color: '#666',
        fontSize: 12,
        padding: [0, 0, 10, 30],
      },
      axisLine: {
        show: false,
      },
      axisTick: {
        show: false,
      },
      axisLabel: {
        color: '#666',
        fontSize: 12,
      },
      splitLine: {
        lineStyle: {
          color: '#eee',
          type: 'dashed',
        },
      },
    },
    series: categories.map((category, index) => ({
      id: `order-${index}-${rangeDays}`,
      name: category,
      type: 'line',
      data: orderData.value.data[category] || [],
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      sampling: 'average',
      itemStyle: {
        color: colors[index % colors.length][1],
        borderWidth: 2,
        borderColor: '#fff',
      },
      lineStyle: {
        width: 3,
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0,
              color: colors[index % colors.length][0],
            },
            {
              offset: 1,
              color: colors[index % colors.length][1],
            },
          ],
        },
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0,
              color: colors[index % colors.length][0],
            },
            {
              offset: 1,
              color: 'rgba(255,255,255,0.1)',
            },
          ],
        },
      },
      emphasis: {
        focus: 'series',
        itemStyle: {
          borderWidth: 3,
        },
      },
      animationDuration: 1500,
      animationEasing: 'cubicInOut',
    })),
  };
});

async function fetchData() {
  const axis = buildRollingDayAxis(days.value);
  chartXLabels.value = axis.map((a) => a.label);
  const cats = Object.keys(orderData.value.data);
  if (cats.length > 0) {
    const next: Record<string, number[]> = {};
    cats.forEach((c) => {
      next[c] = axis.map(() => 0);
    });
    orderData.value = { date: axis.map((a) => a.raw), data: next };
  }
  try {
    setLoading(true);
    const response = await getOrderData(days.value);
    if (response.code === 200) {
      applyOrderPayload(response.data || {});
    }
  } catch (error) {
    console.error('获取订单数据失败:', error);
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
