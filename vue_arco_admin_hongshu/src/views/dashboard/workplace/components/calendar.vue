<template>
  <a-card title="" class="chart-card">
    <div id="container" style="width: 100%; height: 300px"></div>
  </a-card>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts';
import useChartOption from '@/hooks/chart-option';
import { getBlogContributeCount } from '@/api/dashboard';

const { t } = useI18n();
const contributeDate = ref([]);
const blogContributeCount = ref([]);
const goodsContributeCount = ref([]);
let chart = null;
let resizeTimer = null;

const { chartOption } = useChartOption((isDark) => {
  return {
    title: {
      top: 10,
      text: t('dashboard.noteProductContrib'),
      subtext: t('dashboard.contribSubtitle'),
      left: 'center',
      textStyle: {
        color: isDark ? 'rgba(255, 255, 255, 0.7)' : '#000',
      },
    },
    legend: {
      top: '20',
      left: '100',
      data: [
        { name: t('dashboard.noteCountLabel') },
        { name: t('dashboard.productCountLabel') },
      ],
      textStyle: {
        color: isDark ? 'rgba(255, 255, 255, 0.7)' : '#4E5969',
      },
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const date = Array.isArray(params.data) ? params.data[0] : params.name;
        const noteItem = blogContributeCount.value.find((it) => it[0] === date);
        const goodsItem = goodsContributeCount.value.find(
          (it) => it[0] === date
        );
        const note = noteItem ? noteItem[1] : 0;
        const goods = goodsItem ? goodsItem[1] : 0;
        return `${date}<br>${t('dashboard.noteCountLabel')}：${note}<br>${t(
          'dashboard.productCountLabel'
        )}：${goods}`;
      },
    },
    calendar: [
      {
        top: 100,
        left: '5%',
        right: '3%',
        range: contributeDate.value,
        height: '60%',
        splitLine: {
          show: true,
          lineStyle: {
            color: isDark ? 'rgba(255, 255, 255, 0.7)' : '#D3D3D3',
            width: 4,
          },
        },
        yearLabel: { show: false },
        dayLabel: {
          nameMap: [
            t('dashboard.sun'),
            t('dashboard.mon'),
            t('dashboard.tue'),
            t('dashboard.wed'),
            t('dashboard.thu'),
            t('dashboard.fri'),
            t('dashboard.sat'),
          ],
          textStyle: {
            color: isDark ? 'rgba(255, 255, 255, 0.7)' : '#4E5969',
          },
          firstDay: 1,
        },
        monthLabel: {
          nameMap: [
            t('dashboard.jan'),
            t('dashboard.feb'),
            t('dashboard.mar'),
            t('dashboard.apr'),
            t('dashboard.may'),
            t('dashboard.jun'),
            t('dashboard.jul'),
            t('dashboard.aug'),
            t('dashboard.sep'),
            t('dashboard.oct'),
            t('dashboard.nov'),
            t('dashboard.dec'),
          ],
          textStyle: {
            color: isDark ? 'rgba(255, 255, 255, 0.7)' : '#4E5969',
          },
        },
        itemStyle: {
          color: isDark ? '#4E5969' : 'rgba(255, 255, 255, 0.7)',
          borderWidth: 1,
          borderColor: '#D3D3D3',
        },
      },
    ],
    series: [
      {
        name: t('dashboard.noteCountLabel'),
        type: 'scatter',
        coordinateSystem: 'calendar',
        data: blogContributeCount.value.filter((val) => val[1] !== 0),
        symbolSize: (val) => Math.min(18, 8 + val[1] * 2),
        itemStyle: {
          color: '#2ec7c9',
        },
      },
      {
        name: t('dashboard.productCountLabel'),
        type: 'scatter',
        coordinateSystem: 'calendar',
        data: goodsContributeCount.value.filter((val) => val[1] !== 0),
        symbolSize: (val) => Math.min(18, 8 + val[1] * 2),
        itemStyle: {
          color: '#ff9800',
        },
      },
    ],
  };
});

function renderChart() {
  const container = document.getElementById('container');
  if (!container) {
    console.error('容器元素未找到');
    return;
  }

  if (!chart) {
    chart = echarts.init(container);
  }

  chart.setOption(chartOption.value);
}

function splitNoteAndGoods(list) {
  if (!Array.isArray(list)) return [];
  const notes = [];
  const goods = [];
  list.forEach((item) => {
    const tuple = Array.isArray(item)
      ? item
      : [item.date, item.count, item.type || item.category || item.kind];
    const type = (tuple[2] || '').toString().toLowerCase();
    const normalized = [tuple[0], tuple[1]];
    if (['product', 'idle', 'goods'].includes(type)) {
      goods.push(normalized);
    } else {
      notes.push(normalized);
    }
  });
  return { notes, goods };
}

async function initDate() {
  try {
    const response = await getBlogContributeCount();
    if (response.code === 200) {
      contributeDate.value = (response.data.contributeDate || []).map((d) =>
        (d || '').toString().substring(0, 10)
      );
      const hasSeparatedGoods = Array.isArray(
        response.data.goodsContributeCount
      );
      if (hasSeparatedGoods) {
        blogContributeCount.value = (
          response.data.blogContributeCount || []
        ).map((it) =>
          Array.isArray(it) ? [it[0], it[1]] : [it.date, it.count]
        );
        goodsContributeCount.value = (
          response.data.goodsContributeCount || []
        ).map((it) =>
          Array.isArray(it) ? [it[0], it[1]] : [it.date, it.count]
        );
      } else {
        const list =
          response.data.blogContributeCount || response.data.data || [];
        const { notes, goods } = splitNoteAndGoods(list);
        blogContributeCount.value = notes;
        goodsContributeCount.value = goods;
      }
      await nextTick(); // 确保数据更新完毕再渲染
      renderChart();
    }
  } catch (error) {
    console.error('获取数据失败:', error);
  }
}

// 监听 chartOption 或数据的变化并更新图表
watch([chartOption, contributeDate, blogContributeCount], () => {
  if (chart) {
    chart.setOption(chartOption.value);
  }
});

// 防抖机制
function resizeChart() {
  if (resizeTimer) clearTimeout(resizeTimer);
  resizeTimer = setTimeout(() => {
    chart.resize();
  }, 200);
}

onMounted(() => {
  initDate();
  window.addEventListener('resize', resizeChart);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart);
  if (chart) {
    chart.dispose();
    chart = null;
  }
});
</script>

<style scoped>
.chart-card {
  width: 100%;
}
</style>
