<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="{ padding: '20px' }"
    >
      <template #title>{{ $t('dashboard.aiChatTrend') }}</template>
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
import { getChatBusinessTrend } from '@/api/dashboard';
import {
  amountValueYAxisPartial,
  countValueYAxisPartial,
} from '@/utils/chart-line-axis';
import {
  alignSeriesToAxis,
  buildRollingDayAxis,
} from '../rolling-axis';

const { t } = useI18n();
const { loading, setLoading } = useLoading(true);
const days = ref<7 | 30>(7);
const chartUpdateOptions = { notMerge: true, lazyUpdate: false } as const;

const chartXLabels = ref<string[]>([]);

const businessData = ref({
  dateString: [] as string[],
  registerData: [] as number[],
  chatData: [] as number[],
  orderData: [] as number[],
  amountData: [] as number[],
});

function applyChatPayload(rows: any[]) {
  const axis = buildRollingDayAxis(days.value);
  chartXLabels.value = axis.map((a) => a.label);
  const list = Array.isArray(rows) ? rows : [];
  if (list.length === 0) {
    businessData.value = {
      dateString: chartXLabels.value,
      registerData: axis.map(() => 0),
      chatData: axis.map(() => 0),
      orderData: axis.map(() => 0),
      amountData: axis.map(() => 0),
    };
    return;
  }
  const apiDates = list.map((item: any) => item.dateString);
  const reg = list.map((item: any) => Number(item.registerData) || 0);
  const chat = list.map((item: any) => Number(item.chatData) || 0);
  const ord = list.map((item: any) => Number(item.orderData) || 0);
  const amt = list.map((item: any) => Number(item.amountData) || 0);
  businessData.value = {
    dateString: chartXLabels.value,
    registerData: alignSeriesToAxis(axis, apiDates, reg, 0),
    chatData: alignSeriesToAxis(axis, apiDates, chat, 0),
    orderData: alignSeriesToAxis(axis, apiDates, ord, 0),
    amountData: alignSeriesToAxis(axis, apiDates, amt, 0),
  };
}

const chartOption = computed(() => {
  const rangeDays = days.value;
  const countVals = [
    ...businessData.value.registerData,
    ...businessData.value.chatData,
    ...businessData.value.orderData,
  ];
  const amountVals = [...businessData.value.amountData];
  // 定义渐变色
  const colors = {
    register: ['#a8d8ff', '#409EFF'],
    chat: ['#b7ed9d', '#67C23A'],
    order: ['#ffd591', '#E6A23C'],
    amount: ['#ffb3cc', '#F56C6C'],
  };

  return {
    title: {
      text: '',
    },
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
      },
      formatter(params: any) {
        if (!params || !params.length) return '';
        let result = `${params[0].axisValue}<br/>`;
        params.forEach((item: any) => {
          const val =
            item.seriesName === t('dashboard.orderAmount')
              ? Number(item.value).toLocaleString(undefined, {
                  minimumFractionDigits: 0,
                  maximumFractionDigits: 2,
                })
              : item.value;
          result += `
            <div style="display: flex; justify-content: space-between; align-items: center; margin: 3px 0;">
              <span style="display:inline-block;margin-right:5px;border-radius:50%;width:10px;height:10px;background-color:${item.color};"></span>
              <span style="flex: 1;">${item.seriesName}:</span>
              <span style="font-weight:bold;margin-left:5px;">${val}</span>
            </div>
          `;
        });
        return result;
      },
    },
    legend: {
      data: [
        t('dashboard.registerCount'),
        t('dashboard.dialogCount'),
        t('dashboard.orderCount'),
        t('dashboard.orderAmount'),
      ],
      top: 5,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        color: '#666',
        fontSize: 12,
      },
      itemGap: 30,
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true,
    },
    // toolbox: {
    //   feature: {
    //     saveAsImage: {
    //       title: '保存为图片',
    //     },
    //   },
    //   right: 20,
    //   top: 5,
    // },
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
    },
    yAxis: [
      {
        type: 'value',
        name: t('dashboard.quantity'),
        position: 'left',
        ...countValueYAxisPartial(countVals, { emptyMax: 8 }),
        nameTextStyle: {
          color: '#666',
          fontSize: 12,
          padding: [0, 0, 0, 30],
        },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#666', fontSize: 12 },
        splitLine: {
          lineStyle: { color: '#eee', type: 'dashed' },
        },
      },
      {
        type: 'value',
        name: t('dashboard.amountYuan'),
        position: 'right',
        ...amountValueYAxisPartial(amountVals),
        nameTextStyle: {
          color: '#666',
          fontSize: 12,
          padding: [0, 0, 0, 0],
        },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#666', fontSize: 12 },
        splitLine: { show: false },
      },
    ],
    series: [
      {
        id: `chat-reg-${rangeDays}`,
        name: t('dashboard.registerCount'),
        type: 'line',
        yAxisIndex: 0,
        data: businessData.value.registerData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: colors.register[1],
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
                color: colors.register[0],
              },
              {
                offset: 1,
                color: colors.register[1],
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
                color: colors.register[0],
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
      },
      {
        id: `chat-msg-${rangeDays}`,
        name: t('dashboard.dialogCount'),
        type: 'line',
        yAxisIndex: 0,
        data: businessData.value.chatData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: colors.chat[1],
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
                color: colors.chat[0],
              },
              {
                offset: 1,
                color: colors.chat[1],
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
                color: colors.chat[0],
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
      },
      {
        id: `chat-ord-${rangeDays}`,
        name: t('dashboard.orderCount'),
        type: 'line',
        yAxisIndex: 0,
        data: businessData.value.orderData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: colors.order[1],
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
                color: colors.order[0],
              },
              {
                offset: 1,
                color: colors.order[1],
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
                color: colors.order[0],
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
      },
      {
        id: `chat-amt-${rangeDays}`,
        name: t('dashboard.orderAmount'),
        type: 'line',
        yAxisIndex: 1,
        data: businessData.value.amountData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: colors.amount[1],
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
                color: colors.amount[0],
              },
              {
                offset: 1,
                color: colors.amount[1],
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
                color: colors.amount[0],
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
      },
    ],
  };
});

async function fetchData() {
  const axis = buildRollingDayAxis(days.value);
  chartXLabels.value = axis.map((a) => a.label);
  businessData.value = {
    dateString: chartXLabels.value,
    registerData: axis.map(() => 0),
    chatData: axis.map(() => 0),
    orderData: axis.map(() => 0),
    amountData: axis.map(() => 0),
  };
  try {
    setLoading(true);
    const response = await getChatBusinessTrend(days.value);
    if (response.code === 200) {
      applyChatPayload(response.data as any[]);
    }
  } catch (error) {
    console.error('获取AI聊天业务趋势数据失败:', error);
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

