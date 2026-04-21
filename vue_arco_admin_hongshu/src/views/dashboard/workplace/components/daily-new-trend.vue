<template>
  <div class="daily-new-trend-root">
    <a-card
      class="general-card daily-new-trend-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="dailyCardBodyStyle"
    >
      <template #title>{{ $t('dashboard.dailyNewTrend') }}</template>
      <template #extra>
        <a-space :size="8" wrap>
          <a-button type="primary" size="small" @click="showUserMap">{{
            $t('dashboard.viewMapDist')
          }}</a-button>
          <a-radio-group v-model="days" type="button" size="small">
            <a-radio :value="7">{{ $t('dashboard.days7') }}</a-radio>
            <a-radio :value="30">{{ $t('dashboard.days30') }}</a-radio>
          </a-radio-group>
        </a-space>
      </template>
      <div ref="chartRef" class="daily-new-trend-chart"></div>
    </a-card>

    <a-modal
      v-model:visible="mapVisible"
      :title="$t('dashboard.userGeoDist')"
      :footer="false"
      :unmount-on-close="true"
      :width="800"
      @close="destroyMap"
    >
      <div style="position: relative; height: 600px; width: 100%">
        <div
          id="userMapContainer"
          style="height: 100%; width: 100%; position: relative; z-index: 1"
        ></div>
        <a-spin
          v-if="mapLoading"
          :loading="mapLoading"
          :tip="$t('dashboard.loadingGeoData')"
          style="
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            z-index: 10;
            background-color: rgba(255, 255, 255, 0.9);
          "
        >
          <div style="height: 100%; width: 100%"></div>
        </a-spin>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import {
  onMounted,
  onBeforeUnmount,
  ref,
  nextTick,
  computed,
  watch,
} from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts';
import { getDailyNewData, getUserLocations } from '@/api/dashboard';
import { countValueYAxisPartial } from '@/utils/chart-line-axis';
import trendChartHeight from '../dashboard-chart-sizes';
import { alignSeriesToAxis, buildRollingDayAxis } from '../rolling-axis';
// 声明高德地图全局对象，避免 no-undef
declare const AMap: any;

const { t } = useI18n();

const dailyCardBodyStyle = computed(() => ({
  padding: '16px 20px 16px',
  display: 'flex',
  flexDirection: 'column' as const,
  flex: 1,
  minHeight: 0,
}));

const chartRef = ref<HTMLDivElement | null>(null);
let chart: echarts.ECharts | null = null;
let resizeHandler: any = null;
let chartResizeObserver: ResizeObserver | null = null;
const mapVisible = ref(false);
const mapLoading = ref(false);
let map: any = null;

const days = ref<7 | 30>(7);

function buildOption(
  xLabels: string[],
  seriesData: { users: number[]; notes: number[]; products: number[] },
  rangeDays: 7 | 30
) {
  const colors: Record<'member' | 'note' | 'idle', string[]> = {
    member: ['#a8d8ff', '#409EFF'],
    note: ['#b7ed9d', '#67C23A'],
    idle: ['#ffd591', '#E6A23C'],
  };
  const categories: Array<{
    key: 'users' | 'notes' | 'products';
    name: string;
    colorKey: 'member' | 'note' | 'idle';
  }> = [
    { key: 'users', name: t('dashboard.member'), colorKey: 'member' },
    { key: 'notes', name: t('dashboard.note'), colorKey: 'note' },
    { key: 'products', name: t('dashboard.idleProduct'), colorKey: 'idle' },
  ];
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.9)',
      borderColor: '#eee',
      borderWidth: 1,
      padding: [10, 15],
      textStyle: { color: '#666', fontSize: 12 },
      axisPointer: {
        type: 'cross',
        crossStyle: { color: '#999', width: 1, type: 'dashed' },
      },
    },
    legend: {
      data: categories.map((c) => c.name),
      top: 5,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#666', fontSize: 12 },
      itemGap: 30,
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
      data: xLabels,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#ddd' } },
      axisTick: { show: false },
      axisLabel: {
        rotate: rangeDays === 30 ? 35 : 0,
        interval: rangeDays === 30 ? 2 : 0,
        color: '#666',
        fontSize: 11,
        margin: 12,
      },
    },
    yAxis: {
      type: 'value',
      name: t('dashboard.newCount'),
      ...countValueYAxisPartial(
        [...seriesData.users, ...seriesData.notes, ...seriesData.products],
        { emptyMax: 10 }
      ),
      nameTextStyle: { color: '#666', fontSize: 12, padding: [0, 0, 0, 30] },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
      splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
    },
    series: categories.map((c, idx) => ({
      id: `daily-new-${c.key}-${rangeDays}-${idx}`,
      name: c.name,
      type: 'line',
      data: seriesData[c.key],
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: {
        color: colors[c.colorKey][1],
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
            { offset: 0, color: colors[c.colorKey][0] },
            { offset: 1, color: colors[c.colorKey][1] },
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
            { offset: 0, color: colors[c.colorKey][0] },
            { offset: 1, color: 'rgba(255,255,255,0.1)' },
          ],
        },
      },
      emphasis: { focus: 'series', itemStyle: { borderWidth: 3 } },
      animationDuration: 1500,
      animationEasing: 'cubicInOut',
    })),
  } as echarts.EChartsOption;
}

async function loadChart() {
  const res = await getDailyNewData(days.value);
  if (res.code !== 200) return;
  const payload = res.data || {
    dates: [],
    users: [],
    notes: [],
    products: [],
  };
  const axis = buildRollingDayAxis(days.value);
  const xLabels = axis.map((a) => a.label);
  const toNum = (n: unknown) => Number(n) || 0;
  const seriesData = {
    users: alignSeriesToAxis(axis, payload.dates, payload.users, 0).map(toNum),
    notes: alignSeriesToAxis(axis, payload.dates, payload.notes, 0).map(toNum),
    products: alignSeriesToAxis(axis, payload.dates, payload.products, 0).map(
      toNum
    ),
  };
  if (!chart && chartRef.value) chart = echarts.init(chartRef.value);
  if (!chart) return;
  chart.setOption(buildOption(xLabels, seriesData, days.value), {
    notMerge: true,
  });
}

function setupResize() {
  resizeHandler = () => chart && chart.resize();
  window.addEventListener('resize', resizeHandler);
  chartResizeObserver?.disconnect();
  if (chartRef.value && typeof ResizeObserver !== 'undefined') {
    chartResizeObserver = new ResizeObserver(() => chart?.resize());
    chartResizeObserver.observe(chartRef.value);
  }
}

function initUserMapChart(data: any[]) {
  const container = document.getElementById(
    'userMapContainer'
  ) as HTMLDivElement | null;
  if (!container) return;
  map = new AMap.Map('userMapContainer', {
    zoom: 4,
    center: [104.114129, 37.550339],
    viewMode: '2D',
  });
  AMap.plugin(
    ['AMap.Scale', 'AMap.ToolBar', 'AMap.HeatMap'],
    function setupPlugins() {
      map.addControl(new AMap.Scale());
      map.addControl(new AMap.ToolBar({ position: 'RB' }));
      // 样式对齐项目1
      const heatmap = new AMap.HeatMap(map, {
        radius: 25,
        opacity: [0, 0.8],
        gradient: {
          0.4: 'rgb(0, 255, 255)',
          0.65: 'rgb(0, 255, 0)',
          0.85: 'yellow',
          1.0: 'rgb(255, 0, 0)',
        },
      });
      const points = (data || [])
        .map((item: any) => {
          const lng = Number(item.longitude ?? item.lng ?? item.lon ?? item.x);
          const lat = Number(item.latitude ?? item.lat ?? item.y);
          const count = Number(
            item.count ?? item.value ?? item.num ?? item.cnt ?? 0
          );
          return { lng, lat, count };
        })
        .filter((p: any) => !Number.isNaN(p.lng) && !Number.isNaN(p.lat));
      // 输出调试
      // @ts-ignore
      (window as any).heatPointsDebug = points;
      const maxVal = points.length
        ? Math.max.apply(
            null,
            points.map((p: any) => p.count || 0)
          )
        : 1;
      heatmap.setDataSet({ data: points, max: maxVal });
      if (typeof heatmap.show === 'function') heatmap.show();
    }
  );
  const markers = (data || [])
    .map((item: any) => {
      console.log('处理标记数据:', item);
      const lng = Number(item.longitude ?? item.lng ?? item.lon ?? item.x);
      const lat = Number(item.latitude ?? item.lat ?? item.y);
      const count = Number(
        item.count ?? item.value ?? item.num ?? item.cnt ?? 0
      );
      const city = item.city ?? item.name ?? item.location ?? '未知城市';

      console.log(`城市: ${city}, 经度: ${lng}, 纬度: ${lat}, 数量: ${count}`);

      if (Number.isNaN(lng) || Number.isNaN(lat)) {
        console.warn('跳过无效坐标的数据:', item);
        return null;
      }

      const marker = new AMap.Marker({
        position: [lng, lat],
        title: city,
      });

      const infoWindow = new AMap.InfoWindow({
        content: `<div style="padding: 10px; min-width: 120px;">
          <h4 style="margin: 0 0 8px 0; color: #333; font-size: 14px;">${city}</h4>
          <p style="margin: 0; color: #666; font-size: 12px;">${t(
            'dashboard.userCount'
          )}<strong style="color: #1890ff;">${count}</strong></p>
        </div>`,
        offset: new AMap.Pixel(0, -30),
      });

      marker.on('click', () => {
        console.log(`点击了城市: ${city}, 数量: ${count}`);
        infoWindow.open(map, marker.getPosition());
      });

      return marker;
    })
    .filter(Boolean);
  map.add(markers);
  map.setFitView();
}

onMounted(async () => {
  await nextTick();
  await loadChart();
  setupResize();
});

watch(days, async () => {
  await loadChart();
});
onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler);
  chartResizeObserver?.disconnect();
  chartResizeObserver = null;
  if (chart) {
    chart.dispose();
    chart = null;
  }
});

async function showUserMap() {
  mapVisible.value = true;
  mapLoading.value = true;
  // 等待 DOM 渲染完成
  await nextTick();
  try {
    if (typeof AMap === 'undefined') {
      mapLoading.value = false;
      return;
    }
    const res = await getUserLocations();
    console.log('用户地理位置数据:', res);
    if (res.code !== 200) {
      mapLoading.value = false;
      return;
    }
    console.log('地图数据:', res.data);
    // 初始化地图
    initUserMapChart(res.data || []);
    await nextTick();
    mapLoading.value = false;
  } catch (e) {
    console.error('获取用户地理位置失败:', e);
    mapLoading.value = false;
  }
}

function destroyMap() {
  if (map) {
    map.destroy();
    map = null;
  }
}
</script>

<style scoped lang="less">
/* 与并排列等高：根节点铺满格子里一行高度 */
.daily-new-trend-root {
  display: flex;
  flex-direction: column;
  width: 100%;
  min-width: 0;
  height: 100%;
  min-height: 0;
}

.daily-new-trend-card {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  width: 100%;
  height: 100%;

  :deep(.arco-card-body) {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
  }
}

/* 占满卡片内剩余高度，至少 trendChartHeight；同列另一侧较矮时由 flex 拉高图表 */
.daily-new-trend-chart {
  flex: 1;
  width: 100%;
  min-width: 0;
  min-height: v-bind(trendChartHeight);
}
</style>


