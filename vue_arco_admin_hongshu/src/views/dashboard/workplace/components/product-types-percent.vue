<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="{ padding: '10px' }"
    >
      <template #title>
        {{ $t('dashboard.productTypesPercent') }}
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
import { init, getProductByBlogSort } from '@/api/dashboard';
import {
  foldPieOthers,
  pieItemTooltipFormatter,
  pieOutsidePercentFormatter,
} from '@/utils/chart-pie';

const { t } = useI18n();
const { loading } = useLoading();
const productTotal = ref(0);
const productTypeChartData = ref<{ name: string; value: number }[]>([]);

function handleInitResponse(data: { productCount: number }) {
  productTotal.value = data.productCount;
}

function handleTypeResponse(data: { name: string; count: number }[]) {
  productTypeChartData.value = data.map((it: any) => ({
    name: it.name,
    value: it.count ?? it.value ?? 0,
  }));
}

const displayPieData = computed(() =>
  foldPieOthers(productTypeChartData.value, {
    maxItems: 8,
    othersLabel: t('dashboard.other'),
  })
);

async function fetchData() {
  try {
    const [initResponse, typeResponse] = await Promise.all([
      init(),
      getProductByBlogSort(),
    ]);
    if (initResponse.code === 200) handleInitResponse(initResponse.data);
    if (typeResponse.code === 200) handleTypeResponse(typeResponse.data);
  } catch (error) {
    console.error('An error occurred while fetching data:', error);
  }
}

const { chartOption } = useChartOption((isDark) => {
  return {
    legend: {
      type: 'scroll',
      orient: 'horizontal',
      left: 'center',
      data: displayPieData.value.map((item) => item.name),
      bottom: 10,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      pageButtonPosition: 'end',
      textStyle: {
        color: isDark ? 'rgba(255, 255, 255, 0.7)' : '#4E5969',
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
            text: t('dashboard.productVolume'),
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
            text: productTotal.value,
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


