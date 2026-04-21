<template>
  <div class="notice-list-drawer">
    <!-- 头部 -->
    <div class="drawer-header">
      <div class="header-info">
        <div class="header-avatar header-avatar--system" role="img" :aria-label="$t('notice.title')">
          <el-icon :size="28"><BellFilled /></el-icon>
        </div>
        <div class="header-text">
          <div class="header-title">{{ $t("notice.title") }}</div>
          <div class="header-subtitle">{{ $t("notice.announcement") }}</div>
        </div>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="notice-list-container">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="noticeList.length > 0" class="notice-list">
        <div
          v-for="(item, index) in noticeList"
          :key="item.noticeId || index"
          class="notice-item"
          @click="viewNoticeDetail(item)"
        >
          <!-- 时间显示在卡片上方 -->
          <div class="notice-time">{{ formatTime(item.createTime) }}</div>

          <!-- 公告卡片 -->
          <div class="notice-card">
            <!-- 标题行 -->
            <div class="notice-card-header">
              <div class="notice-tag" :class="getTagClass(item.noticeType)">
                {{ getTagText(item.noticeType) }}
              </div>
              <div class="notice-title-row">
                <span class="notice-emoji" v-if="getNoticeEmoji(item)">{{ getNoticeEmoji(item) }}</span>
                <span class="notice-card-title">{{ item.noticeTitle }}</span>
              </div>
            </div>

            <!-- 内容 -->
            <div class="notice-card-content" v-html="formatContentForList(item.noticeContent)"></div>

            <!-- 底部信息 -->
            <div class="notice-card-footer" v-if="item.createBy">
              <img class="admin-avatar" src="https://image.mayongjian.cn/profile.jpg" :alt="$t('notice.admin')" />
              <span class="admin-name">{{ $t("notice.admin") }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty :description="$t('notice.noNotice')" />
      </div>

      <!-- 加载更多 -->
      <div v-if="noticeList.length > 0 && hasMore" class="load-more">
        <el-button :loading="loadingMore" @click="loadMore" text type="primary">
          {{ loadingMore ? $t("common.loading") : $t("notice.loadMore") }}
        </el-button>
      </div>
    </div>

    <!-- 通知详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="$t('notice.detailDialogTitle')"
      width="600px"
      :close-on-click-modal="true"
    >
      <div v-if="currentNotice" class="notice-detail">
        <div class="notice-detail-header">
          <div class="notice-tag" :class="getTagClass(currentNotice.noticeType)">
            {{ getTagText(currentNotice.noticeType) }}
          </div>
          <div class="notice-title">{{ currentNotice.noticeTitle }}</div>
          <div class="notice-time">{{ formatNoticeTime(currentNotice.createTime) }}</div>
        </div>
        <div class="notice-content-wrapper">
          <div class="notice-content" v-html="formatNoticeContent(currentNotice.noticeContent)"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from "vue";
import { useI18n } from "vue-i18n";
import { getNoticeList, getNoticeDetail } from "@/api/notice";
import { ElMessage } from "element-plus";
import { BellFilled } from "@element-plus/icons-vue";

const { t } = useI18n();

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
}>();

const noticeList = ref<any[]>([]);
const loading = ref(false);
const loadingMore = ref(false);
const page = ref(1);
const pageSize = ref(10);
const hasMore = ref(true);
const detailDialogVisible = ref(false);
const currentNotice = ref<any>(null);

// 获取通知公告列表
const fetchNoticeList = async (reset = false) => {
  if (reset) {
    page.value = 1;
    hasMore.value = true;
    noticeList.value = [];
  }

  if (!hasMore.value && !reset) {
    return;
  }

  try {
    if (reset) {
      loading.value = true;
    } else {
      loadingMore.value = true;
    }

    const res = (await getNoticeList(page.value, pageSize.value)) as any;

    if (res.code === 200 && res.data) {
      const records = res.data.records || res.data.list || [];

      // 确保按创建时间倒序排序（最新的在前）
      records.sort((a: any, b: any) => {
        const timeA = new Date(a.createTime || 0).getTime();
        const timeB = new Date(b.createTime || 0).getTime();
        return timeB - timeA; // 倒序：时间大的在前
      });

      if (reset) {
        noticeList.value = records;
      } else {
        // 合并时也要保持倒序
        noticeList.value = [...noticeList.value, ...records];
        // 合并后再次排序，确保整体倒序
        noticeList.value.sort((a: any, b: any) => {
          const timeA = new Date(a.createTime || 0).getTime();
          const timeB = new Date(b.createTime || 0).getTime();
          return timeB - timeA; // 倒序：时间大的在前
        });
      }

      // 判断是否还有更多
      if (records.length < pageSize.value) {
        hasMore.value = false;
      }
    } else {
      ElMessage.error(t("notice.loadFailed"));
    }
  } catch (error) {
    console.error("加载通知公告失败:", error);
    ElMessage.error(t("notice.loadFailed"));
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
};

// 加载更多
const loadMore = () => {
  if (hasMore.value && !loadingMore.value) {
    page.value += 1;
    fetchNoticeList();
  }
};

// 查看通知详情
const viewNoticeDetail = async (notice: any) => {
  if (!notice.noticeId) {
    ElMessage.warning(t("notice.idMissing"));
    return;
  }

  detailDialogVisible.value = true;
  currentNotice.value = null;

  try {
    const res = (await getNoticeDetail(notice.noticeId)) as any;
    if (res.code === 200 && res.data) {
      currentNotice.value = res.data;
    } else {
      ElMessage.error(t("notice.detailLoadFailed"));
    }
  } catch (error) {
    console.error("获取通知公告详情失败:", error);
    ElMessage.error(t("notice.detailLoadFailed"));
  }
};

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return "";

  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  // 今天
  if (days === 0) {
    const hours = date.getHours();
    const minutes = date.getMinutes();
    return `${t("notice.today")} ${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
  }

  // 今年
  if (date.getFullYear() === now.getFullYear()) {
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const hours = date.getHours();
    const minutes = date.getMinutes();
    return `${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")} ${String(hours).padStart(
      2,
      "0"
    )}:${String(minutes).padStart(2, "0")}`;
  }

  // 往年
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
};

// 格式化详情时间
const formatNoticeTime = (timeStr: string) => {
  if (!timeStr) return "";
  const date = new Date(timeStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};

// 格式化内容（列表显示，保留HTML格式）
const formatContentForList = (content: string) => {
  if (!content) return "";
  // 如果是HTML内容，直接返回（但限制显示行数）
  if (content.includes("<")) {
    return content;
  }
  // 否则转换为HTML格式，保留换行
  return content.replace(/\n/g, "<br/>");
};

// 格式化详情内容
const formatNoticeContent = (content: string) => {
  if (!content) return "";
  // 如果是HTML内容，直接返回
  if (content.includes("<")) {
    return content;
  }
  // 否则转换为HTML格式，保留换行
  return content.replace(/\n/g, "<br/>");
};

// 获取标签样式类
const getTagClass = (noticeType: string) => {
  return noticeType === "1" ? "tag-notice" : "tag-activity";
};

// 获取标签文本
const getTagText = (noticeType: string) => {
  return noticeType === "1" ? t("notice.noticeType") : t("notice.activityType");
};

// 获取公告表情（根据标题内容判断）
const getNoticeEmoji = (item: any) => {
  const title = item.noticeTitle || "";
  if (title.includes("汇报") || title.includes("进度")) {
    return "🪴";
  }
  if (title.includes("圈子") || title.includes("变化")) {
    return "😊";
  }
  return "";
};

// 监听抽屉打开，加载数据
const handleDrawerOpen = () => {
  if (props.modelValue && noticeList.value.length === 0) {
    fetchNoticeList(true);
  }
};

// 监听 modelValue 变化
watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal) {
      handleDrawerOpen();
    }
  }
);

onMounted(() => {
  if (props.modelValue) {
    handleDrawerOpen();
  }
});
</script>

<style lang="less" scoped>
.notice-list-drawer {
  display: flex;
  flex-direction: column;
  height: 100%;

  .drawer-header {
    padding: 20px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.08);
    background-color: #fff;

    .header-info {
      display: flex;
      align-items: center;

      .header-avatar {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        margin-right: 12px;
        flex-shrink: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
      }

      .header-avatar--system {
        color: #ffb400;
        background: #fff9e6;
        border: 1px solid rgba(255, 180, 0, 0.35);
      }

      .header-text {
        flex: 1;

        .header-title {
          font-size: 18px;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
        }

        .header-subtitle {
          font-size: 14px;
          color: #999;
        }
      }
    }
  }

  .notice-list-container {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    background-color: #f5f5f5;

    .loading-container {
      padding: 20px;
    }

    .notice-list {
      .notice-item {
        margin-bottom: 10px;
        cursor: pointer;

        .notice-time {
          text-align: center;
          font-size: 12px;
          color: #999;
          margin-bottom: 12px;
        }

        .notice-card {
          background-color: #fff;
          border-radius: 8px;
          padding: 16px;
          box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
          transition: all 0.3s;

          &:hover {
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            transform: translateY(-2px);
          }

          .notice-card-header {
            display: flex;
            align-items: center;
            margin-bottom: 12px;

            .notice-tag {
              padding: 2px 8px;
              border-radius: 4px;
              font-size: 12px;
              font-weight: 500;
              margin-right: 12px;
            }

            .tag-notice {
              background-color: #fff7e6;
              color: #fa8c16;
            }

            .tag-activity {
              background-color: #f6ffed;
              color: #52c41a;
            }

            .notice-title-row {
              flex: 1;
              display: flex;
              align-items: center;

              .notice-emoji {
                font-size: 16px;
                margin-right: 6px;
              }

              .notice-card-title {
                font-size: 16px;
                font-weight: 500;
                color: #333;
                flex: 1;
              }
            }
          }

          .notice-card-content {
            font-size: 14px;
            color: #666;
            line-height: 1.8;
            margin-bottom: 12px;
            word-break: break-all;

            // HTML内容样式
            :deep(p) {
              margin: 0 0 8px 0;
              line-height: 1.8;
              color: #666;
            }

            :deep(ul),
            :deep(ol) {
              margin: 8px 0;
              padding-left: 20px;
              line-height: 1.8;
            }

            :deep(li) {
              margin: 4px 0;
              color: #666;
              list-style-type: none;
              position: relative;
              padding-left: 20px;

              &::before {
                content: "✓";
                position: absolute;
                left: 0;
                color: #52c41a;
                font-weight: bold;
              }
            }

            :deep(ul li) {
              &::before {
                content: "✓";
              }
            }

            :deep(ol li) {
              counter-increment: item;
              padding-left: 24px;

              &::before {
                content: counter(item) ".";
                color: #666;
                font-weight: normal;
              }
            }

            :deep(ol) {
              counter-reset: item;
            }

            :deep(strong),
            :deep(b) {
              font-weight: 600;
              color: #333;
            }
          }

          .notice-card-footer {
            display: flex;
            align-items: center;
            padding-top: 12px;
            border-top: 1px solid #f0f0f0;

            .admin-avatar {
              width: 20px;
              height: 20px;
              border-radius: 50%;
              margin-right: 8px;
              object-fit: cover;
            }

            .admin-name {
              font-size: 12px;
              color: #999;
            }
          }
        }
      }
    }

    .empty-state {
      padding: 60px 20px;
      text-align: center;
    }

    .load-more {
      text-align: center;
      padding: 20px 0;
    }
  }

  .notice-detail {
    .notice-detail-header {
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #f0f0f0;

      .notice-tag {
        display: inline-block;
        padding: 4px 12px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 500;
        margin-bottom: 12px;
      }

      .tag-notice {
        background-color: #fff7e6;
        color: #fa8c16;
      }

      .tag-activity {
        background-color: #f6ffed;
        color: #52c41a;
      }

      .notice-title {
        font-size: 20px;
        font-weight: 600;
        color: #333;
        margin-bottom: 8px;
        line-height: 1.5;
      }

      .notice-time {
        font-size: 14px;
        color: #999;
      }
    }

    .notice-content-wrapper {
      .notice-content {
        font-size: 14px;
        color: #666;
        line-height: 1.8;
        word-break: break-all;

        // HTML内容样式
        :deep(p) {
          margin: 0 0 12px 0;
          line-height: 1.8;
          color: #666;
        }

        :deep(ul),
        :deep(ol) {
          margin: 12px 0;
          padding-left: 24px;
          line-height: 1.8;
        }

        :deep(li) {
          margin: 6px 0;
          color: #666;
        }

        :deep(h1),
        :deep(h2),
        :deep(h3),
        :deep(h4),
        :deep(h5),
        :deep(h6) {
          margin: 16px 0 12px 0;
          font-weight: 600;
          color: #333;
          line-height: 1.5;
        }

        :deep(h1) {
          font-size: 24px;
        }

        :deep(h2) {
          font-size: 20px;
        }

        :deep(h3) {
          font-size: 18px;
        }

        :deep(h4) {
          font-size: 16px;
        }

        :deep(strong),
        :deep(b) {
          font-weight: 600;
          color: #333;
        }

        :deep(em),
        :deep(i) {
          font-style: italic;
        }

        :deep(a) {
          color: #409eff;
          text-decoration: none;

          &:hover {
            text-decoration: underline;
          }
        }

        :deep(blockquote) {
          margin: 12px 0;
          padding: 12px 16px;
          border-left: 4px solid #409eff;
          background-color: #f5f7fa;
          color: #666;
        }

        :deep(code) {
          padding: 2px 6px;
          background-color: #f5f7fa;
          border-radius: 3px;
          font-family: "Courier New", monospace;
          font-size: 13px;
          color: #e6a23c;
        }

        :deep(pre) {
          margin: 12px 0;
          padding: 12px;
          background-color: #f5f7fa;
          border-radius: 4px;
          overflow-x: auto;

          code {
            background-color: transparent;
            padding: 0;
            color: #666;
          }
        }

        :deep(img) {
          max-width: 100%;
          height: auto;
          border-radius: 4px;
          margin: 12px 0;
        }

        :deep(table) {
          width: 100%;
          border-collapse: collapse;
          margin: 12px 0;

          th,
          td {
            padding: 8px 12px;
            border: 1px solid #e4e7ed;
            text-align: left;
          }

          th {
            background-color: #f5f7fa;
            font-weight: 600;
            color: #333;
          }
        }

        :deep(hr) {
          margin: 16px 0;
          border: none;
          border-top: 1px solid #e4e7ed;
        }
      }
    }
  }
}
</style>

