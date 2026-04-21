<template>
  <div class="system-notify-drawer">
    <div class="drawer-header">
      <div class="header-info">
        <div class="header-avatar header-avatar--system" role="img" :aria-label="$t('message.systemNotice')">
          <el-icon :size="28"><BellFilled /></el-icon>
        </div>
        <div class="header-text">
          <div class="header-title">{{ $t("message.systemNotice") }}</div>
          <div class="header-subtitle">{{ $t("systemNotify.configSubtitle") }}</div>
        </div>
      </div>
    </div>

    <div class="list-container">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="items.length > 0" class="item-list">
        <div v-for="item in items" :key="item.id" class="notify-card" @click="openDetail(item)">
          <div class="notify-time">{{ formatTs(item.timestamp) }}</div>
          <div class="notify-card-inner">
            <div class="notify-tag">{{ $t("systemNotify.tag") }}</div>
            <div class="notify-title">{{ item.description || $t("message.systemNotice") }}</div>
            <div v-if="item.imageUrl" class="notify-thumb-wrap">
              <img class="notify-thumb" :src="item.imageUrl" alt="" @error="onImgErr" />
            </div>
            <div class="notify-preview">{{ previewItem(item) }}</div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty :description="$t('systemNotify.empty')" />
      </div>
    </div>

    <el-dialog
      v-model="detailVisible"
      :title="current?.description || $t('message.systemNotice')"
      width="520px"
      :close-on-click-modal="true"
    >
      <div v-if="current" class="detail-body">
        <div class="detail-time">{{ formatTs(current.timestamp) }}</div>
        <div v-if="current.imageUrl" class="detail-image-wrap">
          <img class="detail-image" :src="current.imageUrl" alt="" @error="onImgErr" />
        </div>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <div
          v-if="detailText(current)"
          class="detail-content detail-content--html"
          v-html="detailText(current)"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from "vue";
import { useI18n } from "vue-i18n";
import { getSystemNotificationConfigList } from "@/api/im";
import { ElMessage } from "element-plus";
import { BellFilled } from "@element-plus/icons-vue";
import { htmlToPlainText } from "@/utils/util";

const { t } = useI18n();

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
}>();

const items = ref<any[]>([]);
const loading = ref(false);
const detailVisible = ref(false);
const current = ref<any | null>(null);

const previewItem = (item: any) => {
  const c = item?.content;
  if (c && typeof c === "string") {
    const plain = htmlToPlainText(c);
    if (plain) return plain.length > 120 ? plain.slice(0, 120) + "…" : plain;
  }
  return item?.imageUrl ? t("common.image") : "";
};

const detailText = (item: any) => {
  const c = item?.content;
  if (c && typeof c === "string" && htmlToPlainText(c)) return c.trim();
  return "";
};

const onImgErr = (e: Event) => {
  const el = e.target as HTMLImageElement;
  if (el) el.style.display = "none";
};

const formatTs = (ts: number) => {
  if (ts == null || !Number.isFinite(Number(ts))) return "";
  const d = new Date(Number(ts));
  if (Number.isNaN(d.getTime())) return "";
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const h = String(d.getHours()).padStart(2, "0");
  const min = String(d.getMinutes()).padStart(2, "0");
  return `${y}-${m}-${day} ${h}:${min}`;
};

const loadList = async () => {
  loading.value = true;
  try {
    const res = (await getSystemNotificationConfigList()) as any;
    if (res.code === 200 && Array.isArray(res.data)) {
      items.value = res.data;
    } else {
      items.value = [];
      if (res.msg) ElMessage.warning(res.msg);
    }
  } catch (e) {
    console.error(e);
    items.value = [];
    ElMessage.error(t("systemNotify.loadFailed"));
  } finally {
    loading.value = false;
  }
};

const openDetail = (item: any) => {
  current.value = item;
  detailVisible.value = true;
};

watch(
  () => props.modelValue,
  (open) => {
    if (open) loadList();
  }
);

onMounted(() => {
  if (props.modelValue) loadList();
});
</script>

<style lang="less" scoped>
.system-notify-drawer {
  min-height: 200px;
}

.drawer-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  .header-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .header-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .header-avatar--system {
    color: #ffb400;
    background: #fff9e6;
    // border: 1px solid rgba(255, 180, 0, 0.35);
  }
  .header-title {
    font-size: 16px;
    font-weight: 600;
  }
  .header-subtitle {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 4px;
  }
}

.list-container {
  padding: 12px 16px 24px;
}

.loading-container {
  padding: 16px;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notify-card {
  cursor: pointer;
}

.notify-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
  width: 100%;
  text-align: center;
}

.notify-card-inner {
  background: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  padding: 12px 14px;
  transition: box-shadow 0.2s;
  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.notify-tag {
  display: inline-block;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #fff8e6;
  color: #d48806;
  margin-bottom: 8px;
}

.notify-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 8px;
  line-height: 1.4;
}

.notify-thumb-wrap {
  margin-bottom: 8px;
}
.notify-thumb {
  max-width: 100%;
  max-height: 120px;
  border-radius: 8px;
  object-fit: cover;
  display: block;
}
.notify-preview {
  font-size: 13px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.empty-state {
  padding: 40px 0;
}

.detail-body {
  .detail-time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-bottom: 12px;
  }
  .detail-image-wrap {
    margin-bottom: 12px;
  }
  .detail-image {
    max-width: 100%;
    border-radius: 8px;
    display: block;
  }
  .detail-content {
    white-space: pre-wrap;
    word-break: break-word;
    line-height: 1.6;
    font-size: 14px;
  }
}
</style>
