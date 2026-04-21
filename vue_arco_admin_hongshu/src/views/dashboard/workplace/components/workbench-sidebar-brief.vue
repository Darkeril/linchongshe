<template>
  <div class="workbench-brief-wrap">
    <!-- 单卡片：避免两个 a-card 叠放时中间露出父级 .panel 的灰底形成「横线」（测试/正式构建或间距差异会更明显） -->
    <a-card
      class="general-card workbench-brief-card"
      :body-style="{ padding: '15px 20px 12px' }"
    >
      <a-spin :loading="loading">
        <section class="workbench-brief-section">
          <div class="card-title">
            <span class="card-title-mark" />
            {{ $t('dashboard.coreToday') }}
          </div>
          <a-grid :cols="2" :col-gap="12" :row-gap="10">
            <a-grid-item v-for="m in todayMetrics" :key="m.key">
              <div
                class="metric-tile"
                role="button"
                tabindex="0"
                @click="goPaths(m.paths, { today: '1' })"
                @keydown.enter.prevent="goPaths(m.paths, { today: '1' })"
              >
                <div class="metric-tile-icon" :style="{ background: m.tint }">
                  <component :is="m.icon" />
                </div>
                <div class="metric-tile-body">
                  <a-statistic
                    :value="m.value"
                    :show-group-separator="false"
                    :value-style="statisticValueStyle"
                  />
                  <div class="metric-tile-label">{{ m.label }}</div>
                </div>
              </div>
            </a-grid-item>
          </a-grid>
        </section>

        <section
          class="workbench-brief-section workbench-brief-section--pending"
        >
          <div class="card-title">
            <span class="card-title-mark card-title-mark--orange" />
            {{ $t('dashboard.pending') }}
          </div>
          <a-list :bordered="false" class="pending-arco-list">
            <a-list-item
              class="pending-arco-item"
              @click="pushNoteAudit(router)"
            >
              <div class="pending-arco-row">
                <a-space :size="10">
                  <span class="pending-icon-wrap pending-icon-wrap--note">
                    <IconFile :size="16" />
                  </span>
                  <span class="pending-text">{{
                    $t('dashboard.notePendingAudit')
                  }}</span>
                </a-space>
                <a-space :size="4">
                  <a-tag
                    v-if="summary.pendingNotes > 0"
                    color="orangered"
                    size="small"
                  >
                    {{ summary.pendingNotes }}
                  </a-tag>
                  <span v-else class="pending-zero">0</span>
                  <IconRight class="pending-chevron" />
                </a-space>
              </div>
            </a-list-item>
            <a-list-item
              class="pending-arco-item"
              @click="pushIdleAudit(router)"
            >
              <div class="pending-arco-row">
                <a-space :size="10">
                  <span class="pending-icon-wrap pending-icon-wrap--idle">
                    <IconApps :size="16" />
                  </span>
                  <span class="pending-text">{{
                    $t('dashboard.productPendingAudit')
                  }}</span>
                </a-space>
                <a-space :size="4">
                  <a-tag
                    v-if="summary.pendingProducts > 0"
                    color="orangered"
                    size="small"
                  >
                    {{ summary.pendingProducts }}
                  </a-tag>
                  <span v-else class="pending-zero">0</span>
                  <IconRight class="pending-chevron" />
                </a-space>
              </div>
            </a-list-item>
          </a-list>

          <div class="ip-freq-block">
            <div class="ip-freq-head">
              <div class="card-title">
                <span class="card-title-mark card-title-mark--orange" />
                {{ $t('dashboard.highFreqLoginIP') }}
              </div>
              <a-radio-group
                v-model="ipDays"
                type="button"
                size="small"
                class="ip-freq-range"
              >
                <a-radio :value="1">{{ $t('dashboard.today') }}</a-radio>
                <a-radio :value="7">{{ $t('dashboard.last7Days') }}</a-radio>
                <!-- <a-radio :value="30">30日</a-radio> -->
              </a-radio-group>
            </div>
            <a-spin :loading="ipLoading">
              <div v-if="!ipRows.length" class="ip-freq-empty">
                {{ $t('dashboard.noRecords') }}
              </div>
              <ul v-else class="ip-freq-list">
                <li
                  v-for="row in ipRows"
                  :key="row.ipaddr + row.location"
                  class="ip-freq-item"
                  :class="{ 'ip-freq-item--warn': isAbnormal(row) }"
                >
                  <div class="ip-freq-row-top">
                    <span
                      class="ip-freq-ip"
                      role="button"
                      tabindex="0"
                      @click="goWebLoginLog"
                      @keydown.enter.prevent="goWebLoginLog"
                      >{{ row.ipaddr }}</span
                    >
                    <a-space :size="4">
                      <a-tag v-if="isAbnormal(row)" color="red" size="small">{{
                        $t('dashboard.abnormal')
                      }}</a-tag>
                      <a-tag color="arcoblue" size="small"
                        >{{ row.loginCount }}{{ $t('common.times') }}</a-tag
                      >
                    </a-space>
                  </div>
                  <div class="ip-freq-row-bottom">
                    <div class="ip-freq-loc" :title="row.location">{{
                      row.location
                    }}</div>
                    <a-button
                      v-if="!row.banned"
                      type="text"
                      size="mini"
                      status="danger"
                      @click.stop="handleBanIp(row)"
                    >
                      <icon-stop />
                      {{ $t('dashboard.banIp') }}
                    </a-button>
                    <a-tag v-else color="red" size="small">{{
                      $t('dashboard.banned')
                    }}</a-tag>
                  </div>
                </li>
              </ul>
            </a-spin>
            <div class="ip-freq-hint">
              {{ $t('dashboard.dataSource') }}
            </div>
          </div>
        </section>
      </a-spin>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import type { Component } from 'vue';
import useLoading from '@/hooks/loading';
import { Message } from '@arco-design/web-vue';
import {
  getIpLoginFrequencyStats,
  getWorkbenchSidebarSummary,
  type IpLoginFrequencyRow,
  type WorkbenchSidebarSummary,
} from '@/api/dashboard';
import {
  addBlacklistItem,
  getAutobanConfig,
  getBlacklistConfig,
} from '@/api/system/config';
import {
  pushIdleAudit,
  pushIfResolvable,
  pushNoteAudit,
} from '@/utils/workbench-nav';
import {
  IconApps,
  IconBook,
  IconFile,
  IconOrderedList,
  IconRight,
  IconStop,
  IconUser,
} from '@arco-design/web-vue/es/icon';

const router = useRouter();
const { t } = useI18n();
const { loading, setLoading } = useLoading(true);
const { loading: ipLoading, setLoading: setIpLoading } = useLoading(false);

const ipDays = ref<1 | 7 | 30>(1);

interface IpRowExtended extends IpLoginFrequencyRow {
  banned?: boolean;
}
const ipRows = ref<IpRowExtended[]>([]);
const autobanThreshold = ref(30);

const summary = reactive<WorkbenchSidebarSummary>({
  pendingNotes: 0,
  pendingProducts: 0,
  todayRegistrations: 0,
  todayNotes: 0,
  todayNewProducts: 0,
  todayOrders: 0,
});

const statisticValueStyle = {
  fontSize: '20px',
  fontWeight: 600,
  color: 'var(--color-text-1)',
  lineHeight: '1.2',
} as const;

const todayMetrics = computed(() => [
  {
    key: 'reg',
    label: t('dashboard.register'),
    value: summary.todayRegistrations,
    paths: ['/member/member'],
    icon: IconUser as Component,
    tint: 'rgb(var(--arcoblue-1))',
  },
  {
    key: 'note',
    label: t('dashboard.post'),
    value: summary.todayNotes,
    paths: ['/note/note'],
    icon: IconBook as Component,
    tint: 'rgb(var(--green-1))',
  },
  {
    key: 'product',
    label: t('dashboard.newProduct'),
    value: summary.todayNewProducts,
    paths: ['/idle/product'],
    icon: IconApps as Component,
    tint: 'rgb(var(--cyan-1))',
  },
  {
    key: 'order',
    label: t('dashboard.order'),
    value: summary.todayOrders,
    paths: ['/idle/order'],
    icon: IconOrderedList as Component,
    tint: 'rgb(var(--orange-1))',
  },
]);

function goPaths(paths: string[], query?: Record<string, string>) {
  pushIfResolvable(router, paths, undefined, query);
}

function goWebLoginLog() {
  pushIfResolvable(router, ['/system/log/loginLog']);
}

function isAbnormal(row: IpRowExtended): boolean {
  return row.loginCount >= autobanThreshold.value;
}

async function handleBanIp(row: IpRowExtended) {
  try {
    const reason = `[手动] 工作台一键封禁，${row.loginCount}次登录记录`;
    const res = await addBlacklistItem({
      ipAddress: row.ipaddr,
      reason,
    });
    if (res.code === 200) {
      row.banned = true;
      Message.success(`IP ${row.ipaddr} 已封禁`);
    } else {
      Message.error((res as any).message || '封禁失败');
    }
  } catch (e: any) {
    Message.error(e?.message || '封禁请求失败');
  }
}

async function loadAutobanThreshold() {
  try {
    const res = await getAutobanConfig();
    if (res.code === 200 && res.data) {
      autobanThreshold.value = Math.min(
        Number((res.data as any).failThreshold) || 10,
        Number((res.data as any).successThreshold) || 30
      );
    }
  } catch {
    // use default
  }
}

async function loadIpFreq() {
  setIpLoading(true);
  try {
    const [freqRes, blacklistRes] = await Promise.all([
      getIpLoginFrequencyStats({ days: ipDays.value }),
      getBlacklistConfig(),
    ]);

    // 构建已封禁IP集合（只包含 status="1" 的）
    const bannedIps = new Set<string>();
    const blData = (blacklistRes as any).data ?? blacklistRes;
    if (blData && Array.isArray(blData.list)) {
      blData.list.forEach((item: any) => {
        if (item.status === '1') {
          bannedIps.add(item.ipAddress);
        }
      });
    }

    if (freqRes.code === 200 && Array.isArray(freqRes.data)) {
      ipRows.value = (freqRes.data as IpLoginFrequencyRow[])
        .slice(0, 3)
        .map((r) => ({
          ipaddr: String(r.ipaddr ?? ''),
          loginCount: Number(r.loginCount) || 0,
          location: String(r.location ?? t('dashboard.unresolved')),
          banned: bannedIps.has(String(r.ipaddr ?? '')),
        }));
    } else {
      ipRows.value = [];
    }
  } catch {
    ipRows.value = [];
  } finally {
    setIpLoading(false);
  }
}

watch(ipDays, () => {
  loadIpFreq();
});

async function load() {
  setLoading(true);
  try {
    const res = await getWorkbenchSidebarSummary();
    if (res.code === 200 && res.data) {
      const d = res.data as WorkbenchSidebarSummary;
      summary.pendingNotes = Number(d.pendingNotes) || 0;
      summary.pendingProducts = Number(d.pendingProducts) || 0;
      summary.todayRegistrations = Number(d.todayRegistrations) || 0;
      summary.todayNotes = Number(d.todayNotes) || 0;
      summary.todayNewProducts = Number(d.todayNewProducts) || 0;
      summary.todayOrders = Number(d.todayOrders) || 0;
    }
  } finally {
    setLoading(false);
  }
}

onMounted(() => {
  load();
  loadIpFreq();
  loadAutobanThreshold();
});
</script>

<style scoped lang="less">
.workbench-brief-wrap {
  display: flex;
  flex-direction: column;
}

.workbench-brief-section {
  & > .card-title {
    margin-bottom: 8px;
  }

  &--pending {
    margin-top: 12px;
  }
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-1);
}

.card-title-mark {
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background: rgb(var(--arcoblue-6));
  flex-shrink: 0;

  &--orange {
    background: rgb(var(--orange-6));
  }
}

.metric-tile {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 15px;
  padding: 10px 10px;
  border-radius: var(--border-radius-medium);
  border: 1px solid var(--color-border-2);
  background: var(--color-bg-1);
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease,
    background-color 0.15s ease;

  &:hover {
    border-color: rgb(var(--arcoblue-3));
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    background: var(--color-fill-1);
  }

  &:focus-visible {
    outline: 2px solid rgb(var(--arcoblue-6));
    outline-offset: 1px;
  }
}

.metric-tile-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgb(var(--arcoblue-6));

  :deep(svg) {
    color: rgb(var(--arcoblue-6));
  }
}

.metric-tile-body {
  min-width: 0;
  flex: 1;

  :deep(.arco-statistic-title) {
    display: none;
  }

  :deep(.arco-statistic-content) {
    margin-bottom: 0;
  }
}

.metric-tile-label {
  margin-top: 2px;
  font-size: 12px;
  color: var(--color-text-3);
  line-height: 1.2;
}

.pending-arco-list {
  padding: 0;

  :deep(.arco-list-content) {
    padding: 0;
  }

  :deep(.arco-list-item) {
    padding: 0 !important;
  }
}

.pending-arco-item {
  border-radius: var(--border-radius-medium);
  cursor: pointer;
  transition: background-color 0.15s ease;

  & + & {
    margin-top: 6px;
  }

  &:hover {
    background: var(--color-fill-1);

    .pending-chevron {
      color: rgb(var(--arcoblue-6));
    }
  }
}

.pending-arco-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 15px 15px;
  border: 1px solid var(--color-border-2);
  border-radius: var(--border-radius-medium);
  background: var(--color-bg-1);
}

.pending-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  &--note {
    background: rgb(var(--arcoblue-1));
    color: rgb(var(--arcoblue-6));
  }

  &--idle {
    background: rgb(var(--green-1));
    color: rgb(var(--green-6));
  }
}

.pending-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-1);
}

.pending-zero {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-3);
  min-width: 22px;
  text-align: right;
}

.pending-chevron {
  color: var(--color-text-4);
  transition: color 0.15s ease;
}

.ip-freq-block {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed var(--color-border-2);
  width: 100%;
  box-sizing: border-box;

  :deep(.arco-spin) {
    display: block;
    width: 100%;
  }
}

.ip-freq-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.ip-freq-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-2);
}

.ip-freq-range {
  flex-shrink: 0;

  :deep(.arco-radio-button) {
    // padding: 0 1px;
    font-size: 12px;
  }
}

.ip-freq-empty {
  padding: 12px 0;
  text-align: center;
  font-size: 12px;
  color: var(--color-text-3);
}

.ip-freq-list {
  list-style: none;
  margin: 0;
  padding: 8px 0 0;
  width: 100%;
  box-sizing: border-box;
}

.ip-freq-item {
  width: 100%;
  max-width: 100%;
  margin-left: auto;
  margin-right: auto;
  box-sizing: border-box;
  padding: 12px 14px;
  border: 1px solid var(--color-border-2);
  border-radius: var(--border-radius-medium);
  background: var(--color-bg-1);
  transition: background-color 0.15s ease, border-color 0.15s ease;

  & + & {
    margin-top: 6px;
  }

  &:hover {
    background: var(--color-fill-1);
    border-color: rgb(var(--arcoblue-3));
  }

  &--warn {
    border-color: rgb(var(--red-3));
    background: rgb(var(--red-1));

    &:hover {
      border-color: rgb(var(--red-4));
      background: rgb(var(--red-1));
    }
  }
}

.ip-freq-row-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.ip-freq-ip {
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-family-mono, monospace);
  color: var(--color-text-1);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;

  &:hover {
    color: rgb(var(--arcoblue-6));
  }
}

.ip-freq-row-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 5px;
  gap: 4px;
}

.ip-freq-loc {
  font-size: 12px;
  color: var(--color-text-3);
  line-height: 1.35;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ip-freq-hint {
  margin-top: 8px;
  font-size: 11px;
  color: var(--color-text-4);
  line-height: 1.4;
  text-align: center;
}
</style>
