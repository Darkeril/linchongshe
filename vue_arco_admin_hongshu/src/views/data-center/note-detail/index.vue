<template>
  <div class="note-detail-page">
    <Breadcrumb :items="['数据中心', '内容分析', '笔记数据详情']" />
    <a-spin :loading="loading" style="width: 100%">
      <template v-if="detail">
        <div class="detail-header">
          <div class="note-info-card">
            <a-image
              v-if="detail.noteInfo?.cover"
              :src="detail.noteInfo.cover"
              width="120"
              height="90"
              fit="cover"
              class="note-cover"
            />
            <div v-else class="note-cover-placeholder" />
            <div class="note-meta">
              <div class="note-title">{{ detail.noteInfo?.title || '-' }}</div>
              <div v-if="detail.noteInfo?.tags" class="note-tags">
                <a-tag v-for="(t, i) in tagList" :key="i" size="small">{{
                  t
                }}</a-tag>
              </div>
              <div class="note-row"
                >发布时间：{{ detail.noteInfo?.publishTime || '-' }}</div
              >
            </div>
            <div class="note-info-actions">
              <a-button
                type="outline"
                size="small"
                class="btn-switch-note"
                @click="openSwitchModal"
              >
                切换笔记
              </a-button>
            </div>
          </div>
          <div class="header-toolbar">
            <a-radio-group
              v-model="timeRange"
              type="button"
              size="small"
              @change="handleTimeChange"
            >
              <a-radio value="7">近7天</a-radio>
              <a-radio value="14">近14天</a-radio>
              <a-radio value="30">近30天</a-radio>
            </a-radio-group>
          </div>
        </div>

        <a-tabs v-model:active-key="activeTab" type="text" class="detail-tabs">
          <a-tab-pane key="diagnosis" title="笔记诊断" />
          <a-tab-pane key="core" title="核心数据" />
          <a-tab-pane key="viewTrend" title="观看趋势" />
          <a-tab-pane key="viewSource" title="观看来源" />
          <a-tab-pane key="audience" title="观众画像" />
        </a-tabs>

        <div class="data-hint">最多更新至笔记发布后14天</div>

        <!-- 1. 笔记诊断 -->
        <div v-show="activeTab === 'diagnosis'" class="detail-content">
          <div class="section-title-main">笔记诊断 ①</div>
          <div class="diagnosis-layout">
            <div class="diagnosis-chart">
              <div class="diagnosis-chart-body">
                <Chart class="chart-inner" :options="radarOption" />
              </div>
              <div class="radar-legend">
                <span class="legend-item"
                  ><i class="dot dot-self" />本篇笔记数据</span
                >
              </div>
            </div>
            <div class="diagnosis-detail">
              <div
                v-for="(item, i) in diagnosisItems"
                :key="i"
                class="diagnosis-row"
              >
                <span class="diagnosis-text">{{ item.text }}</span>
                <a-tooltip :content="item.tooltip" position="top">
                  <a-link size="small" class="link-suggestion">提升建议</a-link>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>

        <!-- 2. 核心数据 -->
        <div v-show="activeTab === 'core'" class="detail-content">
          <div class="core-header">
            <span class="core-title">| 基础数据</span>
            <span class="core-update">数据更新至 {{ dataUpdateTime }}</span>
            <!-- <a-button type="primary" size="small">
              <template #icon><icon-download /></template>
              导出数据
            </a-button> -->
          </div>
          <div class="core-cards">
            <div class="core-card"
              ><div class="core-label">曝光数</div
              ><div class="core-value">{{ formatNum(totalExposure) }}</div></div
            >
            <div class="core-card"
              ><div class="core-label">观看数</div
              ><div class="core-value">{{ formatNum(totalView) }}</div></div
            >
            <div class="core-card"
              ><div class="core-label">封面点击率</div
              ><div class="core-value">{{ coverCtr }}%</div></div
            >
            <div class="core-card"
              ><div class="core-label">平均观看时长</div
              ><div class="core-value"
                >{{ detail.avgDurationSec ?? 0 }}秒</div
              ></div
            >
            <div class="core-card"
              ><div class="core-label">完播率</div
              ><div class="core-value"
                >{{ (detail.completionRate ?? 0).toFixed(1) }}%</div
              ></div
            >
            <div class="core-card"
              ><div class="core-label">2秒退出率</div
              ><div class="core-value"
                >{{ (detail.twoSecExitRate ?? 0).toFixed(1) }}%</div
              ></div
            >
          </div>
          <a-radio-group
            v-model="coreTimeDimension"
            type="button"
            size="small"
            class="dimension-switch"
          >
            <a-radio value="hour">按小时</a-radio>
            <a-radio value="day">按天</a-radio>
          </a-radio-group>
          <div class="chart-box">
            <div class="chart-body">
              <Chart class="chart-inner" :options="coreTrendOption" />
            </div>
          </div>
          <div class="chart-box">
            <div class="chart-title">观看时段分布</div>
            <div class="chart-subtitle"
              >统计周期内 0–24 点观看数（来自接口）</div
            >
            <div class="chart-body chart-body--sm">
              <Chart class="chart-inner" :options="viewTimeSlotOption" />
            </div>
          </div>

          <div class="core-header core-header--block">
            <span class="core-title">| 互动数据</span>
            <span class="core-update">数据实时更新,粉丝占比次日更新</span>
            <!-- <a-button type="primary" size="small">导出数据</a-button> -->
          </div>
          <div class="core-cards">
            <div class="core-card"
              ><div class="core-label">点赞数</div
              ><div class="core-value">{{ detail.likes ?? 0 }}</div></div
            >
            <div class="core-card"
              ><div class="core-label">评论数</div
              ><div class="core-value">{{ detail.comments ?? 0 }}</div></div
            >
            <div class="core-card"
              ><div class="core-label">收藏数</div
              ><div class="core-value">{{ detail.favorites ?? 0 }}</div></div
            >
            <div class="core-card"
              ><div class="core-label">分享数</div
              ><div class="core-value">{{ detail.shares ?? 0 }}</div></div
            >
          </div>
          <a-radio-group
            v-model="coreTimeDimension"
            type="button"
            size="small"
            class="dimension-switch"
          >
            <a-radio value="hour">按小时</a-radio>
            <a-radio value="day">按天</a-radio>
          </a-radio-group>
          <div class="chart-box">
            <div class="chart-body">
              <Chart class="chart-inner" :options="interactTrendOption" />
            </div>
          </div>
        </div>

        <!-- 3. 观看趋势 -->
        <div v-show="activeTab === 'viewTrend'" class="detail-content">
          <div class="section-title-main">观看趋势 ①</div>
          <div class="section-subtitle">数据更新至 {{ dataUpdateTime }}</div>
          <a-button type="outline" size="small" class="btn-trend-tab"
            >离开趋势</a-button
          >
          <p class="insight-text"
            >5秒内离开率可结合「2秒退出率」和「平均观看时长」综合判断内容前段吸引力，建议持续优化开头信息密度与节奏。</p
          >
          <div class="chart-box">
            <div class="chart-title">观看时长分布</div>
            <div class="chart-subtitle">基于实际观看时长与完播率统计</div>
            <div class="chart-body">
              <Chart class="chart-inner" :options="watchDurationDistOption" />
            </div>
          </div>
        </div>

        <!-- 4. 观看来源 -->
        <div v-show="activeTab === 'viewSource'" class="detail-content">
          <div class="section-title-main">观看来源</div>
          <div class="section-subtitle">最多更新至笔记发布后14天</div>
          <div class="chart-box chart-box--single">
            <div class="chart-body chart-body--pie">
              <Chart class="chart-inner" :options="viewSourceOption" />
            </div>
          </div>
        </div>

        <!-- 5. 观众画像 -->
        <div v-show="activeTab === 'audience'" class="detail-content">
          <div class="section-title-main">
            观众画像
            <a-tooltip content="统计该笔记观众的性别、年龄、城市及兴趣分布">
              <icon-question-circle
                class="section-title-icon"
                style="color: var(--color-text-3); margin-left: 4px"
              />
            </a-tooltip>
          </div>
          <div class="section-subtitle">最多更新至笔记发布后14天</div>
          <div class="audience-grid">
            <div class="audience-cell">
              <div class="audience-cell-title">性别分布</div>
              <div class="audience-chart-body">
                <Chart class="chart-inner" :options="audienceGenderOption" />
              </div>
            </div>
            <div class="audience-cell">
              <div class="audience-cell-title">年龄分布</div>
              <div class="audience-chart-body">
                <Chart class="chart-inner" :options="audienceAgeOption" />
              </div>
            </div>
            <div class="audience-cell">
              <div class="audience-cell-title">城市分布</div>
              <div class="audience-chart-body">
                <Chart class="chart-inner" :options="audienceRegionOption" />
              </div>
            </div>
            <div class="audience-cell">
              <div class="audience-cell-title">兴趣分布</div>
              <div class="audience-chart-body">
                <Chart class="chart-inner" :options="audienceInterestOption" />
              </div>
            </div>
          </div>
        </div>
      </template>
      <a-empty v-else-if="!loading" description="笔记不存在或加载失败" />
    </a-spin>

    <!-- 切换笔记弹窗 -->
    <a-modal
      v-model:visible="switchModalVisible"
      title="切换笔记"
      :footer="false"
      width="700px"
      unmount-on-close
      @open="loadSwitchNoteList"
    >
      <a-spin
        :loading="switchNoteLoading"
        style="min-height: 200px; display: block"
      >
        <div v-if="switchNoteList.length > 0" class="switch-note-list">
          <div
            v-for="item in switchNoteList"
            :key="item.id"
            class="switch-note-item"
          >
            <a-image
              v-if="item.cover"
              :src="item.cover"
              width="64"
              height="48"
              fit="cover"
              class="switch-note-cover"
            />
            <div v-else class="switch-note-cover-placeholder" />
            <div class="switch-note-meta">
              <div class="switch-note-title">{{
                item.title || '无标题笔记'
              }}</div>
              <div class="switch-note-time">{{ item.publishTime || '-' }}</div>
            </div>
            <a-button
              type="outline"
              size="small"
              class="switch-note-select"
              @click="selectNote(item)"
            >
              选择
            </a-button>
          </div>
        </div>
        <div v-else-if="!switchNoteLoading" class="switch-note-empty">
          <a-empty description="暂无笔记数据" />
        </div>
      </a-spin>
      <div class="switch-note-pagination">
        <a-pagination
          v-model:current="switchNotePage.current"
          v-model:page-size="switchNotePage.pageSize"
          :total="switchNotePage.total"
          :show-total="true"
          size="small"
          @change="loadSwitchNoteList"
        />
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
/**
 * 数据对接说明（接口 getNoteDataCenterDetail）：
 * 已对接：笔记信息、曝光/观看数及趋势、封面点击率、人均时长、完播率、2秒退出率、点赞/评论/收藏/分享、
 * 观看来源、观看时段分布、观众画像（性别/年龄/城市/兴趣）。
 */
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import Chart from '@/components/chart/index.vue';
import useChartOption from '@/hooks/chart-option';
import {
  getNoteDataCenterDetail,
  getNoteDataCenterList,
  type NoteDataCenterDetailVo,
  type NoteDataCenterRecord,
} from '@/api/web/note';

const route = useRoute();
const router = useRouter();
const noteId = computed(() => String(route.params.id || ''));
const timeRange = ref('14');
const loading = ref(false);
const detail = ref<NoteDataCenterDetailVo | null>(null);
const activeTab = ref('diagnosis');
const coreTimeDimension = ref('day');

const switchModalVisible = ref(false);
const switchNoteList = ref<NoteDataCenterRecord[]>([]);
const switchNoteLoading = ref(false);
const switchNotePage = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

const dataUpdateTime = computed(() => {
  const now = new Date();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  const h = String(now.getHours()).padStart(2, '0');
  const min = String(now.getMinutes()).padStart(2, '0');
  return `${m}-${d} ${h}:${min}`;
});

const tagList = computed(() => {
  const tags = detail.value?.noteInfo?.tags;
  if (!tags) return [];
  if (typeof tags === 'string' && tags.startsWith('[')) {
    try {
      const arr = JSON.parse(tags);
      return Array.isArray(arr) ? arr : [tags];
    } catch {
      return String(tags)
        .split(/[,，]/)
        .map((s: string) => s.trim())
        .filter(Boolean);
    }
  }
  return String(tags)
    .split(/[,，]/)
    .map((s: string) => s.trim())
    .filter(Boolean);
});

const totalExposure = computed(() => {
  const list = detail.value?.exposureTrend ?? [];
  return list.reduce((sum, p) => sum + (p.cnt ?? 0), 0);
});
const totalView = computed(() => {
  const list = detail.value?.viewTrend ?? [];
  return list.reduce((sum, p) => sum + (p.cnt ?? 0), 0);
});

const coverCtr = computed(() => {
  const e = totalExposure.value;
  const v = totalView.value;
  if (!e) return '0';
  return ((v / e) * 100).toFixed(1);
});

const diagnosisItems = computed(() => {
  const d = detail.value;
  const ctr = coverCtr.value;
  const completion = (d?.completionRate ?? 0).toFixed(1);
  const interact = (d?.likes ?? 0) + (d?.comments ?? 0) + (d?.favorites ?? 0);
  return [
    {
      text: `封面点击率: 当前封面点击率为${ctr}%`,
      tooltip:
        '封面点击率还不错，超过同类平均水平，可以总结一下这篇笔记在封面、标题及选题上有哪些可取之处',
    },
    {
      text: `2秒退出率: 当前2秒退出率为${(d?.twoSecExitRate ?? 0).toFixed(1)}%`,
      tooltip:
        '在个人主页设置清晰的头像及具备信息量的个人简介，选择具有吸引力的背景图片，采用统一风格的视频封面，利用置顶笔记展示精彩内容，均有助于涨粉哦',
    },
    {
      text: `分享数: 当前分享数为${d?.shares ?? 0}`,
      tooltip: '该视频画质一般，更换高清的视频源还能使作品质量进一步提升哦～',
    },
    {
      text: `完播率: 当前完播率为${completion}%`,
      tooltip:
        '记得将完播率与"人均观看时长"、"离开趋势"结合分析，剪辑节奏、内容趣味性、有用性、信息价值、亮点出现频次、字幕配音等因素都会影响观众持续观看的欲望',
    },
    {
      text: `互动数: 当前互动数为${interact}`,
      tooltip:
        '互动数据表现还不错，你的笔记帮到了不少人呢，快去总结一下大家点赞、评论、收藏这篇笔记的原因吧',
    },
  ];
});

function formatNum(n: number) {
  if (n >= 10000) return `${(n / 10000).toFixed(1)}万`;
  return String(n);
}

function openSwitchModal() {
  switchModalVisible.value = true;
}

async function loadSwitchNoteList() {
  if (!switchModalVisible.value) return;
  switchNoteLoading.value = true;
  try {
    const res: any = await getNoteDataCenterList({
      pageNum: switchNotePage.value.current,
      pageSize: switchNotePage.value.pageSize,
    });
    const data = res?.data ?? res;
    const rows = data?.rows ?? data?.list ?? [];
    switchNoteList.value = Array.isArray(rows) ? rows : [];
    switchNotePage.value.total = Number(data?.total ?? res?.total ?? 0);
  } catch (e) {
    switchNoteList.value = [];
    switchNotePage.value.total = 0;
    console.error('获取笔记列表失败', e);
  } finally {
    switchNoteLoading.value = false;
  }
}

function selectNote(item: NoteDataCenterRecord) {
  switchModalVisible.value = false;
  if (String(item.id) === noteId.value) return;
  router.push({
    name: 'DataCenterNoteDetail',
    params: { id: String(item.id) },
    query: { days: timeRange.value },
  });
}

async function fetchDetail() {
  if (!noteId.value) return;
  loading.value = true;
  try {
    const res: any = await getNoteDataCenterDetail(
      noteId.value,
      Number(timeRange.value)
    );
    const data = res?.data ?? res;
    detail.value = data ?? null;
  } catch (e) {
    detail.value = null;
    console.error('笔记数据详情请求失败', e);
  } finally {
    loading.value = false;
  }
}

function handleTimeChange() {
  fetchDetail();
}

const trendDates = computed(() => {
  const list =
    coreTimeDimension.value === 'day' ? detail.value?.viewTrend ?? [] : [];
  return list.map((p) => p.date);
});
const trendValues = computed(() => {
  const list =
    coreTimeDimension.value === 'day' ? detail.value?.viewTrend ?? [] : [];
  return list.map((p) => p.cnt ?? 0);
});

const radarValues = computed(() => {
  const d = detail.value;
  const e = totalExposure.value;
  const v = totalView.value;
  const ctr = e ? Math.min(100, (v / e) * 100) : 0;
  const completion = d?.completionRate ?? 0;
  const keep3s = Math.max(0, 100 - (d?.twoSecExitRate ?? 0));
  const interact = (d?.likes ?? 0) + (d?.comments ?? 0) + (d?.favorites ?? 0);
  const share = d?.shares ?? 0;
  return [
    { name: '封面点击率', value: ctr, max: 100 },
    { name: '前3秒留存', value: keep3s, max: 100 },
    { name: '分享数', value: Math.min(100, share * 5), max: 100 },
    { name: '完播率', value: completion, max: 100 },
    { name: '互动数', value: Math.min(100, interact * 2), max: 100 },
  ];
});

const { chartOption: radarOption } = useChartOption(() => {
  const indicators = radarValues.value.map((r) => ({
    name: r.name,
    max: r.max,
  }));
  const selfData = radarValues.value.map((r) => r.value);
  return {
    tooltip: {},
    radar: { indicator: indicators, shape: 'polygon', splitNumber: 4 },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: selfData,
            name: '本篇笔记数据',
            lineStyle: { color: '#165dff' },
            areaStyle: { opacity: 0.2 },
          },
        ],
      },
    ],
  };
});

const { chartOption: coreTrendOption } = useChartOption(() => ({
  tooltip: { trigger: 'axis' },
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
    data: trendDates.value.length ? trendDates.value : ['01/01', '01/15'],
    axisLine: { lineStyle: { color: '#999' } },
    axisTick: { show: false },
    axisLabel: { color: '#666', fontSize: 11 },
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    axisTick: { show: false },
    splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
  },
  series: [
    {
      name: '观看数',
      type: 'line',
      data: trendValues.value.length ? trendValues.value : [0, 0],
      smooth: true,
      symbol: 'circle',
      symbolSize: 5,
      areaStyle: { color: 'rgba(22, 93, 255, 0.15)' },
      itemStyle: { color: '#165dff' },
      lineStyle: { width: 2, color: '#165dff' },
    },
  ],
}));

const interactTrendValues = computed(() => {
  const list = detail.value?.viewTrend ?? [];
  const total =
    (detail.value?.likes ?? 0) +
    (detail.value?.comments ?? 0) +
    (detail.value?.favorites ?? 0);
  return list.length ? list.map(() => total) : [0, 0];
});

const { chartOption: interactTrendOption } = useChartOption(() => ({
  tooltip: { trigger: 'axis' },
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
    data: trendDates.value.length ? trendDates.value : ['01/01', '01/15'],
    axisLine: { lineStyle: { color: '#999' } },
    axisTick: { show: false },
    axisLabel: { color: '#666', fontSize: 11 },
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    axisTick: { show: false },
    splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
  },
  series: [
    {
      name: '互动数',
      type: 'line',
      data: interactTrendValues.value,
      smooth: true,
      symbol: 'circle',
      symbolSize: 5,
      areaStyle: { color: 'rgba(0, 180, 42, 0.15)' },
      itemStyle: { color: '#00b42a' },
      lineStyle: { width: 2, color: '#00b42a' },
    },
  ],
}));

const viewSourceMapped = computed(() => {
  const raw = detail.value?.viewSource ?? [];
  const total = raw.reduce((sum, s) => sum + s.value, 0);
  if (!total) return [{ name: '暂无数据', value: 1 }];
  return raw.map((s) => ({ name: s.name, value: s.value }));
});

const viewTimeSlotData = computed(() => {
  const arr = detail.value?.viewTimeSlot ?? [];
  if (arr.length >= 24) return arr.map((v) => Number(v));
  return Array.from({ length: 24 }, (_, i) => Number(arr[i] ?? 0));
});

const { chartOption: viewTimeSlotOption } = useChartOption(() => ({
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
    data: Array.from({ length: 24 }, (_, i) => `${i}-${i + 1}`),
    axisLabel: { fontSize: 10, interval: 1 },
  },
  yAxis: { type: 'value' },
  series: [
    {
      type: 'bar',
      data: viewTimeSlotData.value,
      itemStyle: { color: '#165dff' },
      barWidth: '60%',
    },
  ],
}));

const { chartOption: viewSourceOption } = useChartOption(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 10, left: 'center' },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b} {d}%' },
      data: viewSourceMapped.value.map((s) => ({
        name: s.name,
        value: s.value,
      })),
      color: ['#f53f3f', '#ff7d00', '#00b42a', '#165dff', '#722ed1', '#f769b0'],
    },
  ],
}));

// 观众画像数据（接口返回，无数据则为空，由图表自行展示“暂无数据”）
const audienceGender = computed(() => {
  const list = detail.value?.audienceProfile?.gender;
  const buckets = [
    { name: '男性', color: '#165dff' },
    { name: '女性', color: '#f53f3f' },
    { name: '未知', color: '#86909c' },
  ];

  if (!list?.length) {
    // 没有数据时也返回三个维度，数值为 0，只用于图例展示颜色
    return buckets.map((b) => ({
      name: b.name,
      value: 0,
      itemStyle: { color: b.color },
    }));
  }

  const map = list.reduce<Record<string, number>>((acc, cur) => {
    const key = cur.name;
    if (!key) return acc;
    const current = acc[key] || 0;
    acc[key] = current + Number(cur.value ?? 0);
    return acc;
  }, {});

  return buckets.map((b) => ({
    name: b.name,
    value: map[b.name] ?? 0,
    itemStyle: { color: b.color },
  }));
});
const audienceAge = computed(() => {
  const list = detail.value?.audienceProfile?.age;
  if (list?.length) {
    return list.map((x) => ({
      name: x.name,
      value: Number(x.value ?? 0),
    }));
  }
  return [];
});
const audienceRegion = computed(() => {
  const list = detail.value?.audienceProfile?.region;
  if (list?.length)
    return list.map((x) => ({ name: x.name, value: Number(x.value ?? 0) }));
  return [];
});
const audienceInterest = computed(() => {
  const list = detail.value?.audienceProfile?.interest;
  if (list?.length)
    return list.map((x) => ({ name: x.name, value: Number(x.value ?? 0) }));
  return [];
});

const { chartOption: audienceGenderOption } = useChartOption(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
  legend: {
    orient: 'vertical',
    left: 10,
    top: 'middle',
    data: audienceGender.value.length
      ? audienceGender.value.map((x) => x.name)
      : ['暂无数据'],
  },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['55%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}: {d}%' },
      labelLine: { length: 8, length2: 6 },
      data: audienceGender.value.length
        ? audienceGender.value
        : [{ name: '暂无数据', value: 1 }],
    },
  ],
}));

const audienceAgeNames = computed(() => audienceAge.value.map((x) => x.name));
const audienceAgeValues = computed(() => audienceAge.value.map((x) => x.value));
const { chartOption: audienceAgeOption } = useChartOption(() => ({
  tooltip: { trigger: 'axis' },
  grid: {
    left: '8%',
    right: '5%',
    top: '10%',
    bottom: '15%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: audienceAgeNames.value,
    axisLabel: { fontSize: 11 },
    axisLine: { lineStyle: { color: '#eee' } },
  },
  yAxis: {
    type: 'value',
    max: 60,
    axisLabel: { formatter: '{value}%', fontSize: 11 },
    splitLine: { lineStyle: { color: '#eee' } },
  },
  series: [
    {
      type: 'bar',
      data: audienceAgeValues.value,
      itemStyle: { color: '#165dff' },
      barWidth: 40,
      label: { show: true, position: 'top', formatter: '{c}%' },
    },
  ],
}));

const audienceRegionNames = computed(() =>
  audienceRegion.value.map((x) => x.name)
);
const audienceRegionValues = computed(() =>
  audienceRegion.value.map((x) => x.value)
);
const { chartOption: audienceRegionOption } = useChartOption(() => {
  const hasData = audienceRegion.value.length > 0;
  return {
    tooltip: { trigger: 'axis' },
    grid: {
      left: '8%',
      right: '5%',
      top: '10%',
      bottom: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: hasData ? audienceRegionNames.value : ['暂无数据'],
      axisLabel: { fontSize: 11, rotate: hasData ? 25 : 0 },
      axisLine: { lineStyle: { color: '#eee' } },
    },
    yAxis: {
      type: 'value',
      max: hasData ? 70 : 10,
      axisLabel: { formatter: '{value}%', fontSize: 11 },
      splitLine: { lineStyle: { color: '#eee' } },
    },
    series: [
      {
        type: 'bar',
        data: hasData ? audienceRegionValues.value : [0],
        itemStyle: { color: '#f53f3f' },
        barWidth: 40,
        label: { show: hasData, position: 'top', formatter: '{c}%' },
      },
    ],
  };
});

const audienceInterestNames = computed(() =>
  audienceInterest.value.map((x) => x.name)
);
const audienceInterestValues = computed(() =>
  audienceInterest.value.map((x) => x.value)
);
const { chartOption: audienceInterestOption } = useChartOption(() => {
  const hasData = audienceInterest.value.length > 0;
  return {
    tooltip: { trigger: 'axis' },
    grid: {
      left: '8%',
      right: '5%',
      top: '10%',
      bottom: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: hasData ? audienceInterestNames.value : ['暂无数据'],
      axisLabel: { fontSize: 11, rotate: hasData ? 25 : 0 },
      axisLine: { lineStyle: { color: '#eee' } },
    },
    yAxis: {
      type: 'value',
      max: hasData ? 15 : 10,
      axisLabel: { formatter: '{value}%', fontSize: 11 },
      splitLine: { lineStyle: { color: '#eee' } },
    },
    series: [
      {
        type: 'bar',
        data: hasData ? audienceInterestValues.value : [0],
        itemStyle: { color: '#f53f3f' },
        barWidth: '50%',
        label: { show: hasData, position: 'top', formatter: '{c}%' },
      },
    ],
  };
});

const watchDurationCurve = computed(() => {
  const completion = (detail.value?.completionRate ?? 0) / 100;
  const points: number[] = [100];
  for (let s = 1; s <= 50; s += 1) {
    const t = s * 5;
    const remain =
      100 * Math.exp(-t / 30) * (1 - completion * 0.5) + completion * 100 * 0.5;
    points.push(Math.max(0, Math.round(remain)));
  }
  return points;
});

const { chartOption: watchDurationDistOption } = useChartOption(() => ({
  tooltip: { trigger: 'axis' },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '15%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: Array.from({ length: 51 }, (_, i) => `${i * 5}s`),
    axisLabel: { interval: 4, fontSize: 10 },
  },
  yAxis: {
    type: 'value',
    name: '%',
    min: 0,
    max: 100,
    axisLabel: { formatter: '{value}%' },
    splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
  },
  series: [
    {
      name: '本篇',
      type: 'line',
      data: watchDurationCurve.value,
      smooth: true,
      lineStyle: { color: '#165dff' },
      itemStyle: { color: '#165dff' },
    },
    {
      name: '同类平均（参考）',
      type: 'line',
      data: Array.from({ length: 51 }, (_, i) => Math.max(0, 100 - i * 1.8)),
      smooth: true,
      lineStyle: { type: 'dashed', color: '#00b42a' },
      itemStyle: { color: '#00b42a' },
    },
  ],
}));

watch(
  noteId,
  (id) => {
    if (id) fetchDetail();
  },
  { immediate: false }
);

onMounted(() => {
  const q = route.query.days;
  if (q === '7' || q === '14' || q === '30') timeRange.value = String(q);
  fetchDetail();
});
</script>

<script lang="ts">
export default { name: 'DataCenterNoteDetail' };
</script>

<style lang="less" scoped>
.note-detail-page {
  padding: 0 20px 20px;
  background-color: var(--color-fill-2);
  min-height: 100%;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
  padding: 20px;
  background: var(--color-bg-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
}
.note-info-card {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}
.note-cover {
  border-radius: 8px;
  flex-shrink: 0;
}
.note-cover-placeholder {
  width: 120px;
  height: 90px;
  background: var(--color-fill-2);
  border-radius: 8px;
  flex-shrink: 0;
}
.note-meta {
  min-width: 0;
}
.note-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-1);
  margin-bottom: 6px;
}
.note-tags {
  margin-bottom: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.note-row {
  font-size: 12px;
  color: var(--color-text-3);
}
.note-info-actions {
  margin-left: 50px;
  flex-shrink: 0;
}
.btn-switch-note {
  flex-shrink: 0;
}
.header-toolbar {
  flex-shrink: 0;
}

.detail-tabs {
  margin-bottom: 8px;
}
.data-hint {
  font-size: 12px;
  color: var(--color-text-3);
  margin-bottom: 16px;
}

.detail-content {
  background: var(--color-bg-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
  padding: 20px;
}

.section-title-main {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 4px;
}
.section-subtitle {
  font-size: 12px;
  color: var(--color-text-3);
  margin-bottom: 12px;
}

.diagnosis-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  align-items: center;
}
.diagnosis-chart {
  min-width: 0;
}
.diagnosis-chart-body {
  width: 100%;
  height: 320px;
}
.radar-legend {
  margin-top: 12px;
  font-size: 12px;
  color: var(--color-text-2);
  display: flex;
  justify-content: center;
  gap: 24px;
}
.legend-item {
  margin-right: 0;
}
.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;
  vertical-align: middle;
}
.dot-self {
  background: #165dff;
}
.dot-peer {
  background: transparent;
  border: 1px dashed #f53f3f;
}
.diagnosis-detail {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.diagnosis-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--color-text-2);
}
.diagnosis-row:last-child {
  margin-bottom: 0;
}
.link-suggestion {
  flex-shrink: 0;
  margin-right: 148px;
}

.core-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.core-header--block {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border-2);
}
.core-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-1);
}
.core-update {
  font-size: 12px;
  color: var(--color-text-3);
  margin-left: auto;
}
.core-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}
.core-card {
  padding: 12px;
  background: var(--color-fill-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
}
.core-label {
  font-size: 12px;
  color: var(--color-text-3);
  margin-bottom: 4px;
}
.core-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-1);
}
.core-extra {
  font-size: 11px;
  color: var(--color-text-3);
  margin-top: 4px;
}
.dimension-switch {
  margin-bottom: 12px;
}

.chart-box {
  background: var(--color-fill-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
  padding: 16px;
  margin-bottom: 12px;
}
.chart-box--single {
  margin-bottom: 0;
}
.chart-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-1);
  margin-bottom: 8px;
}
.chart-body {
  height: 280px;
}
.chart-body--pie {
  height: 320px;
}
.chart-body--small {
  height: 220px;
}
.chart-body--sm {
  height: 200px;
}
.chart-inner {
  width: 100% !important;
  height: 100% !important;
}

.btn-trend-tab {
  margin-bottom: 12px;
}
.insight-text {
  font-size: 13px;
  color: var(--color-text-2);
  margin-bottom: 16px;
  line-height: 1.6;
}

.audience-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.audience-cell {
  background: var(--color-fill-2);
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
  padding: 16px;
}
.audience-cell-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-1);
  margin-bottom: 12px;
  text-align: center;
}
.audience-chart-body {
  width: 100%;
  height: 260px;
}

.switch-note-list {
  max-height: 520px;
  overflow-y: auto;
  padding-right: 8px;
}
.switch-note-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border-2);
}
.switch-note-item:last-child {
  border-bottom: none;
}
.switch-note-cover {
  border-radius: 6px;
  flex-shrink: 0;
}
.switch-note-cover-placeholder {
  width: 64px;
  height: 48px;
  background: var(--color-fill-2);
  border-radius: 6px;
  flex-shrink: 0;
}
.switch-note-meta {
  flex: 1;
  min-width: 0;
}
.switch-note-title {
  font-size: 14px;
  color: var(--color-text-1);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.switch-note-time {
  font-size: 12px;
  color: var(--color-text-3);
}
.switch-note-select {
  flex-shrink: 0;
  margin-right: 16px;
}
.switch-note-empty {
  padding: 40px 0;
  text-align: center;
}
.switch-note-pagination {
  margin-top: 36px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .diagnosis-layout {
    grid-template-columns: 1fr;
  }
  .audience-grid {
    grid-template-columns: 1fr;
  }
}
</style>
