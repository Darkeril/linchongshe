<template>
  <div class="feedback-container">
    <div v-if="isLogin" class="feedback-wrapper">
      <!-- 左侧内容区 -->
      <div class="feedback-main">
        <!-- 页面标题 -->
        <div class="page-header">
          <h2 class="page-title">
            <el-icon><ChatLineRound /></el-icon>
            <span>{{ $t("feedback.pageTitle") }}</span>
          </h2>
          <p class="page-desc">{{ $t("feedback.pageDesc") }}</p>
        </div>

        <!-- 留言表单 -->
        <div class="feedback-form-card">
          <el-form :model="formData" :rules="formRules" ref="formRef">
            <el-form-item prop="content">
              <el-input
                v-model="formData.content"
                type="textarea"
                :rows="5"
                :placeholder="$t('feedback.contentPlaceholder')"
                maxlength="500"
                show-word-limit
                class="feedback-textarea"
              />
            </el-form-item>
            <el-form-item>
              <div class="form-actions">
                <el-select v-model="formData.type" :placeholder="$t('feedback.selectType')" style="width: 150px" clearable>
                  <el-option :label="$t('feedback.typeFeature')" value="feature" />
                  <el-option :label="$t('feedback.typeBug')" value="bug" />
                  <el-option :label="$t('feedback.typeOther')" value="other" />
                </el-select>
                <el-button
                  type="primary"
                  :loading="submitting"
                  @click="handleSubmit"
                  :disabled="!formData.content.trim()"
                >
                  <el-icon><Promotion /></el-icon>
                  {{ $t("feedback.submitSuggestion") }}
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </div>

        <!-- 留言列表 -->
        <div class="feedback-list">
          <div class="list-header">
            <h3>{{ $t("feedback.allSuggestions", { n: total }) }}</h3>
            <el-radio-group v-model="filterType" size="small" @change="handleFilterChange">
              <el-radio-button label="">{{ $t("feedback.filterAll") }}</el-radio-button>
              <el-radio-button label="feature">{{ $t("feedback.typeFeature") }}</el-radio-button>
              <el-radio-button label="bug">{{ $t("feedback.typeBug") }}</el-radio-button>
              <el-radio-button label="other">{{ $t("feedback.typeOther") }}</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 留言项 -->
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="feedbackList.length === 0" class="empty-container">
            <el-empty :description="$t('feedback.emptyList')" />
          </div>
          <div v-else class="feedback-items">
            <div v-for="item in feedbackList" :key="item.id" class="feedback-item">
              <div class="item-header">
                <div class="user-info">
                  <el-avatar :size="40" :src="item.avatar" class="user-avatar">
                    {{ item.nickname?.charAt(0) || "U" }}
                  </el-avatar>
                  <div class="user-details">
                    <div class="nickname">{{ item.nickname || $t("feedback.anonymous") }}</div>
                    <div class="time">{{ formatTime(item.createTime) }}</div>
                  </div>
                </div>
                <el-tag :type="getTypeTagType(item.type)" size="small" class="type-tag">
                  {{ getTypeLabel(item.type) }}
                </el-tag>
              </div>
              <div class="item-content">{{ item.content }}</div>
              <div class="item-footer">
                <el-button text @click="handleLike(item)" class="like-btn">
                  <i
                    v-if="item.isLiked"
                    class="iconfont icon-follow-fill"
                    style="width: 1em; height: 1em; color: #ff2442"
                  ></i>
                  <i v-else class="iconfont icon-follow" style="width: 1em; height: 1em; color: #999"></i>
                  <span>{{ item.likeCount || 0 }}</span>
                </el-button>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="total > 0" class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="handlePageChange"
              :hide-on-single-page="false"
            />
          </div>
        </div>
      </div>

      <!-- 右侧待实现功能栏 -->
      <div class="planned-features-sidebar">
        <div class="sidebar-header">
          <h3>
            <el-icon><List /></el-icon>
            {{ $t("feedback.plannedFeatures") }}
          </h3>
        </div>
        <div v-if="plannedFeaturesLoading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="plannedFeatures.length === 0" class="empty-sidebar">
          <el-empty :description="$t('feedback.emptyPlanned')" :image-size="80" />
        </div>
        <div v-else class="features-list">
          <div v-for="(feature, index) in plannedFeatures" :key="index" class="feature-item">
            <div class="feature-index">{{ index + 1 }}</div>
            <div class="feature-content">
              <div class="feature-title">{{ feature.title }}</div>
              <div v-if="feature.description" class="feature-desc">
                {{ feature.description }}
              </div>
              <div v-if="feature.status" class="feature-status">
                <el-tag :type="getStatusTagType(feature.status)" size="small">
                  {{ feature.status }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 未登录提示 -->
    <div v-else class="not-login-container">
      <el-empty :description="$t('feedback.loginToSuggest')">
        <el-button type="primary" @click="goToLogin">{{ $t("feedback.goLogin") }}</el-button>
      </el-empty>
    </div>

    <!-- 返回顶部 -->
    <el-backtop :bottom="80" :right="24">
      <div class="back-top">
        <el-icon><Top /></el-icon>
      </div>
    </el-backtop>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/userStore";
import { submitFeedback, getFeedbackList, likeFeedback, getPlannedFeatures } from "@/api/feedback";
import { ElMessage } from "element-plus";
import { ChatLineRound, Promotion, List, Top } from "@element-plus/icons-vue";
import { formatDate } from "@/utils/util";

const { t } = useI18n();
const router = useRouter();
const userStore = useUserStore();

// 登录状态
const isLogin = computed(() => userStore.isLogin());

// 表单数据
const formRef = ref();
const formData = ref({
  content: "",
  type: "feature",
});
const submitting = ref(false);

// 表单验证规则
const formRules = computed(() => ({
  content: [
    { required: true, message: t("feedback.contentRequired"), trigger: "blur" },
    { min: 5, message: t("feedback.contentMinLength"), trigger: "blur" },
  ],
}));

// 留言列表
const feedbackList = ref<any[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const filterType = ref("");

// 待实现功能
const plannedFeatures = ref<any[]>([]);
const plannedFeaturesLoading = ref(false);

// 获取留言列表
const fetchFeedbackList = async () => {
  loading.value = true;
  try {
    const res: any = await getFeedbackList({
      currentPage: currentPage.value,
      pageSize: pageSize.value,
      type: filterType.value || undefined,
    });
    if (res?.code === 200) {
      feedbackList.value = res.data?.records || [];
      total.value = res.data?.total || 0;
      console.log("分页数据:", { total: total.value, currentPage: currentPage.value, pageSize: pageSize.value });
    }
  } catch (error) {
    console.error("获取留言列表失败:", error);
    ElMessage.error(t("feedback.listLoadFailed"));
  } finally {
    loading.value = false;
  }
};

// 获取待实现功能
const fetchPlannedFeatures = async () => {
  plannedFeaturesLoading.value = true;
  try {
    const res: any = await getPlannedFeatures();
    if (res?.code === 200) {
      plannedFeatures.value = res.data || [];
    }
  } catch (error) {
    console.error("获取待实现功能失败:", error);
    plannedFeatures.value = [];
  } finally {
    plannedFeaturesLoading.value = false;
  }
};

// 提交建议
const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return;

    submitting.value = true;
    try {
      const res: any = await submitFeedback({
        content: formData.value.content.trim(),
        type: formData.value.type,
      });
      if (res?.code === 200) {
        ElMessage.success(t("feedback.submitSuccess"));
        formData.value.content = "";
        formData.value.type = "feature";
        // 重新获取列表
        currentPage.value = 1;
        await fetchFeedbackList();
      } else {
        ElMessage.error(res?.message || res?.msg || t("feedback.submitFailed"));
      }
    } catch (error) {
      console.error("提交建议失败:", error);
      ElMessage.error(t("feedback.submitFailedRetry"));
    } finally {
      submitting.value = false;
    }
  });
};

// 点赞
const handleLike = async (item: any) => {
  try {
    const res: any = await likeFeedback(item.id);
    if (res?.code === 200) {
      const isLiked = res.data?.isLiked;
      if (isLiked !== undefined) {
        item.isLiked = isLiked;
        item.likeCount = (item.likeCount || 0) + (isLiked ? 1 : -1);
      }
    }
  } catch (error) {
    console.error("点赞失败:", error);
  }
};

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchFeedbackList();
};

// 筛选变化
const handleFilterChange = () => {
  currentPage.value = 1;
  fetchFeedbackList();
};

// 格式化时间
const formatTime = (time: string | number) => {
  if (!time) return "";
  // 如果是字符串，转换为时间戳
  const timestamp = typeof time === "string" ? new Date(time).getTime() : time;
  if (isNaN(timestamp)) return "";
  return formatDate(timestamp);
};

// 获取类型标签
const getTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    feature: t("feedback.typeFeature"),
    bug: t("feedback.typeBug"),
    other: t("feedback.typeOther"),
  };
  return typeMap[type] || t("feedback.typeOther");
};

const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    feature: "primary",
    bug: "danger",
    other: "info",
  };
  return typeMap[type] || "info";
};

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    [t("feedback.statusPlanning")]: "info",
    [t("feedback.statusDeveloping")]: "warning",
    [t("feedback.statusDone")]: "success",
    规划中: "info",
    开发中: "warning",
    已完成: "success",
  };
  return statusMap[status] || "info";
};

// 去登录
const goToLogin = () => {
  router.push("/login");
};

// 初始化
onMounted(() => {
  if (isLogin.value) {
    fetchFeedbackList();
  }
  fetchPlannedFeatures();
});
</script>

<style lang="less" scoped>
.feedback-container {
  flex: 1;
  padding: 0 24px;
  padding-top: 72px;
  width: 77%;
  margin: 0 auto;
  min-height: 100vh;
  box-sizing: border-box;
  position: relative;

  @media screen and (max-width: 1200px) {
    width: 100%;
    padding: 0 16px;
    padding-top: 64px;
  }
}

.feedback-wrapper {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;

  @media screen and (max-width: 1200px) {
    flex-direction: column;
    gap: 16px;
  }
}

.feedback-main {
  flex: 1;
  min-width: 0;
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;

    .el-icon {
      font-size: 28px;
      color: #409eff;
    }
  }

  .page-desc {
    color: #666;
    font-size: 14px;
    margin: 0;
  }
}

.feedback-form-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .feedback-textarea {
    :deep(.el-textarea__inner) {
      border-radius: 8px;
      font-size: 14px;
      line-height: 1.6;
    }
  }

  .form-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;

    @media screen and (max-width: 695px) {
      flex-direction: column;
      align-items: stretch;

      .el-select {
        width: 100% !important;
      }

      .el-button {
        width: 100%;
      }
    }
  }
}

.feedback-list {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }

    @media screen and (max-width: 695px) {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .el-radio-group {
        width: 100%;
        display: flex;
        flex-wrap: wrap;
      }
    }
  }

  .loading-container,
  .empty-container {
    padding: 40px 0;
  }

  .feedback-items {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .feedback-item {
    padding: 20px;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
    }

    .item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .user-info {
        display: flex;
        align-items: center;
        gap: 12px;

        .user-avatar {
          flex-shrink: 0;
        }

        .user-details {
          .nickname {
            font-size: 15px;
            font-weight: 500;
            color: #333;
            margin-bottom: 4px;
          }

          .time {
            font-size: 12px;
            color: #999;
          }
        }
      }

      .type-tag {
        flex-shrink: 0;
      }
    }

    .item-content {
      font-size: 14px;
      line-height: 1.6;
      color: #666;
      margin-bottom: 12px;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .item-footer {
      display: flex;
      justify-content: flex-end;

      .like-btn {
        padding: 4px 12px;
      }
    }
  }

  .pagination-container {
    margin-top: 32px;
    padding: 20px 0;
    display: flex;
    justify-content: center;
    width: 100%;

    :deep(.el-pagination) {
      .btn-prev,
      .btn-next {
        background: #fff !important;
        border: 1px solid #e0e0e0 !important;
        border-radius: 6px !important;
        color: #333 !important;
        padding: 8px 12px !important;
        transition: all 0.3s;
        min-width: 36px;
        height: 36px;

        &:hover:not(.disabled) {
          background: #f5f5f5 !important;
          border-color: #409eff !important;
          color: #409eff !important;
        }

        &.disabled {
          color: #c0c4cc !important;
          cursor: not-allowed;
        }
      }

      .el-pager {
        li {
          background: #fff !important;
          border: 1px solid #e0e0e0 !important;
          border-radius: 6px !important;
          color: #333 !important;
          margin: 0 4px !important;
          min-width: 36px !important;
          height: 36px !important;
          line-height: 36px !important;
          transition: all 0.3s;

          &:hover {
            background: #f5f5f5 !important;
            border-color: #409eff !important;
            color: #409eff !important;
          }

          &.is-active {
            background: #409eff !important;
            border-color: #409eff !important;
            color: #fff !important;
            font-weight: 500;
          }
        }
      }
    }
  }
}

.planned-features-sidebar {
  width: 320px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  height: fit-content;
  position: sticky;
  top: 88px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;

  @media screen and (max-width: 1200px) {
    width: 100%;
    position: relative;
    top: 0;
    max-height: none;
  }

  .sidebar-header {
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;

    h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;

      .el-icon {
        color: #409eff;
      }
    }
  }

  .loading-container,
  .empty-sidebar {
    padding: 20px 0;
  }

  .features-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .feature-item {
    display: flex;
    gap: 12px;
    padding: 12px;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      background: #f5f7fa;
    }

    .feature-index {
      flex-shrink: 0;
      width: 24px;
      height: 24px;
      background: #409eff;
      color: #fff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 600;
    }

    .feature-content {
      flex: 1;
      min-width: 0;

      .feature-title {
        font-size: 14px;
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
      }

      .feature-desc {
        font-size: 12px;
        color: #666;
        line-height: 1.5;
        margin-bottom: 8px;
      }

      .feature-status {
        display: flex;
        justify-content: flex-end;
      }
    }
  }
}

.not-login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}

.back-top {
  width: 40px;
  height: 40px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 100px;
  color: rgba(51, 51, 51, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  cursor: pointer;

  &:hover {
    background: #f5f7fa;
  }
}
</style>

