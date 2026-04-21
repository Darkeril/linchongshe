<template>
  <div class="video-upload-container">
    <template v-if="videoUrl">
      <div class="header">
        <div class="header-left">
          <span class="header-icon"></span>
          <span class="header-title">{{ $t("note.publishVideo") }}</span>
        </div>
        <div class="header-right">
          <div class="action-buttons">
            <el-upload
              class="replace-upload"
              action=""
              :show-file-list="false"
              :headers="uploadHeader"
              :before-upload="beforeVideoUpload"
              :on-change="handleVideoChange"
              accept="video/*"
            >
              <el-button type="primary" link>
                <el-icon><RefreshRight /></el-icon>
                {{ $t("note.replaceVideo") }}
              </el-button>
            </el-upload>
            <el-button type="primary" link @click="handleClear">
              <el-icon><CloseBold /></el-icon>
              {{ $t("common.clear") }}
            </el-button>
          </div>
        </div>
      </div>
    </template>

    <div v-if="videoUrl" class="video-preview-container">
      <div class="cover-container">
        <!-- 视频预览区域 -->
        <div class="video-title">{{ $t("note.video") }}</div>
        <div class="video-content">
          <div class="video-wrapper">
            <video
              ref="videoRef"
              :src="videoUrl"
              class="uploaded-video"
              controls
              @loadeddata="handleVideoLoaded"
              preload="auto"
            ></video>
          </div>
        </div>

        <!-- 视频封面 -->
        <div class="cover-title">{{ $t("note.videoCover") }}</div>
        <div class="cover-content">
          <div class="cover-preview" v-if="coverUrl">
            <img :src="coverUrl" :alt="$t('note.videoCover')" />
            <div class="cover-actions">
              <div class="action-buttons">
                <el-button type="primary" link @click.stop="handleCapture" class="action-btn">
                  <el-icon><RefreshLeft /></el-icon>
                  {{ $t("note.recapture") }}
                </el-button>
                <el-upload
                  class="cover-upload"
                  action=""
                  :show-file-list="false"
                  :headers="uploadHeader"
                  :before-upload="beforeCoverUpload"
                  :on-change="handleCoverChange"
                >
                  <el-button type="primary" link class="action-btn">
                    <el-icon><Upload /></el-icon>
                    {{ $t("note.uploadCover") }}
                  </el-button>
                </el-upload>
              </div>
            </div>
          </div>
          <div v-else class="cover-empty">
            <div class="loading-cover">{{ $t("note.generatingCover") }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 视频上传区域 -->
    <div class="video-upload-area" v-if="!videoUrl">
      <el-upload
        ref="uploadRef"
        class="video-uploader"
        action=""
        :limit="1"
        :headers="uploadHeader"
        :before-upload="beforeVideoUpload"
        :on-change="handleVideoChange"
        :auto-upload="false"
        :show-file-list="false"
        v-model:file-list="fileList"
        accept="video/*"
        drag
      >
        <div class="upload-placeholder">
          <div class="upload-icon">
            <img
              src="//fe-static.xhscdn.com/formula-static/ugc/public/img/upload-large.e52d3e7.png"
              :alt="$t('note.uploadIcon')"
            />
          </div>
          <div class="upload-text">{{ $t("note.dragOrClickUploadVideo") }}</div>
          <div class="upload-tip">({{ $t("note.singleVideoOnly") }})</div>
          <el-button type="primary" class="upload-btn">
            <el-icon><Plus /></el-icon>
            {{ $t("note.uploadVideo") }}
          </el-button>
        </div>
      </el-upload>

      <!-- 上传说明 -->
      <div class="upload-info">
        <div class="info-item">
          <h4>{{ $t("note.videoSize") }}</h4>
          <p>{{ $t("note.videoSizeDesc") }}</p>
        </div>
        <div class="vertical-divider"></div>
        <div class="info-item">
          <h4>{{ $t("note.videoFormat") }}</h4>
          <p>{{ $t("note.videoFormatDesc") }}</p>
        </div>
        <div class="vertical-divider"></div>
        <div class="info-item">
          <h4>{{ $t("note.videoResolution") }}</h4>
          <p>{{ $t("note.videoResolutionDesc") }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, nextTick } from "vue";
import { useI18n } from "vue-i18n";
import { Plus, CloseBold, RefreshLeft, RefreshRight, Upload } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";

const { t } = useI18n();

const props = defineProps({
  videoUrl: {
    type: String,
    default: "",
  },
  coverUrl: {
    type: String,
    default: "",
  },
  uploadHeader: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["update:videoUrl", "update:coverUrl"]);

const videoRef = ref<HTMLVideoElement | null>(null);
const uploadRef = ref<any>(null);
const fileList = ref<any[]>([]);

// 视频上传前验证
const ALLOWED_VIDEO_FORMATS = ["video/mp4", "video/quicktime"]; // mov格式

const MAX_VIDEO_SIZE_MB = 20;

const beforeVideoUpload = (file: File) => {
  // 先进行同步验证（格式和大小）
  const isValidFormat = ALLOWED_VIDEO_FORMATS.includes(file.type);
  if (!isValidFormat) {
    ElMessage.error(t("note.mp4MovOnly"));
    // 使用 nextTick 确保在 DOM 更新后清理文件列表
    nextTick(() => {
      fileList.value = [];
      if (uploadRef.value) {
        uploadRef.value.clearFiles?.();
      }
    });
    return false;
  }

  const isVideo = file.type.startsWith("video/");
  if (!isVideo) {
    ElMessage.error(t("note.uploadVideoFilePlease"));
    // 使用 nextTick 确保在 DOM 更新后清理文件列表
    nextTick(() => {
      fileList.value = [];
      if (uploadRef.value) {
        uploadRef.value.clearFiles?.();
      }
    });
    return false;
  }

  // 检查文件大小（同步检查，立即返回）
  const maxSizeBytes = MAX_VIDEO_SIZE_MB * 1024 * 1024;
  if (file.size > maxSizeBytes) {
    ElMessage.error(t("note.videoMaxSizeMB", { n: MAX_VIDEO_SIZE_MB }));
    // 使用 nextTick 确保在 DOM 更新后清理文件列表
    nextTick(() => {
      fileList.value = [];
      if (uploadRef.value) {
        uploadRef.value.clearFiles?.();
      }
    });
    return false;
  }

  // 异步检查视频时长（需要加载元数据）
  const duration = 60 * 60; // 60分钟
  const video = document.createElement("video");
  video.preload = "metadata";
  return new Promise((resolve) => {
    video.onloadedmetadata = () => {
      if (video.duration > duration) {
        ElMessage.error(t("note.videoMaxDuration60"));
        // 使用 nextTick 确保在 DOM 更新后清理文件列表
        nextTick(() => {
          fileList.value = [];
          if (uploadRef.value) {
            uploadRef.value.clearFiles?.();
          }
        });
        resolve(false);
      } else {
        resolve(true);
      }
      // 清理临时创建的 URL
      URL.revokeObjectURL(video.src);
    };
    video.onerror = () => {
      // 如果无法加载视频，允许继续（可能是元数据问题）
      ElMessage.warning(t("note.videoDurationUnknownWarn"));
      resolve(true);
      if (video.src.startsWith("blob:")) {
        URL.revokeObjectURL(video.src);
      }
    };
    video.src = URL.createObjectURL(file);
  });
};

// 处理视频变更
const handleVideoChange = (file: any, _fileListParam: any[]) => {
  // 如果没有文件原始数据，不处理
  if (!file || !file.raw) {
    return;
  }

  // 先进行完整的文件验证
  const maxSizeBytes = MAX_VIDEO_SIZE_MB * 1024 * 1024;

  // 检查文件大小
  if (file.raw.size > maxSizeBytes) {
    ElMessage.error(t("note.videoMaxSizeMB", { n: MAX_VIDEO_SIZE_MB }));
    // 清理可能创建的 URL
    if (file.url && file.url.startsWith("blob:")) {
      URL.revokeObjectURL(file.url);
    }
    // 清理文件列表，防止状态污染
    fileList.value = [];
    // 尝试清空 upload 组件的内部状态
    if (uploadRef.value) {
      uploadRef.value.clearFiles?.();
    }
    return;
  }

  // 检查文件格式
  const isValidFormat = ALLOWED_VIDEO_FORMATS.includes(file.raw.type);
  if (!isValidFormat) {
    ElMessage.error(t("note.mp4MovOnly"));
    // 清理可能创建的 URL
    if (file.url && file.url.startsWith("blob:")) {
      URL.revokeObjectURL(file.url);
    }
    // 清理文件列表
    fileList.value = [];
    if (uploadRef.value) {
      uploadRef.value.clearFiles?.();
    }
    return;
  }

  // 如果文件状态是fail（被before-upload拒绝），不处理
  if (file.status === "fail") {
    fileList.value = [];
    return;
  }

  // 文件验证通过，处理视频
  // 清除旧的视频URL
  if (props.videoUrl && props.videoUrl.startsWith("blob:")) {
    URL.revokeObjectURL(props.videoUrl);
  }
  const videoUrl = URL.createObjectURL(file.raw);
  emit("update:videoUrl", videoUrl);
  // 重置封面，等待视频加载完成后自动截取
  emit("update:coverUrl", "");
};

// 视频加载完成后自动截取第一帧作为封面
const handleVideoLoaded = async () => {
  if (!videoRef.value) return;
  try {
    // 将视频跳转到第一帧
    videoRef.value.currentTime = 0;
    // 等待视频真正跳转完成
    await new Promise((resolve) => {
      videoRef.value!.onseeked = resolve;
    });
    // 截取第一帧作为封面
    handleCapture();
  } catch (error) {
    // console.error('Error capturing video frame:', error);
  }
};

// 截取视频当前帧作为封面
const handleCapture = () => {
  if (!videoRef.value) return;
  const video = videoRef.value;
  const canvas = document.createElement("canvas");
  canvas.width = video.videoWidth;
  canvas.height = video.videoHeight;
  const ctx = canvas.getContext("2d");
  if (!ctx) return;
  try {
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
    const coverUrl = canvas.toDataURL("image/jpeg", 0.8);
    emit("update:coverUrl", coverUrl);
  } catch (error) {
    // console.error('Error capturing video frame:', error);
    ElMessage.error(t("note.coverCaptureFail"));
  }
};

// 封面图片上传前验证
const beforeCoverUpload = (file: File) => {
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImage) {
    ElMessage.error(t("note.coverMustBeImage"));
    return false;
  }
  if (!isLt2M) {
    ElMessage.error(t("note.coverMaxSize2MB"));
    return false;
  }
  return true;
};

// 处理封面图片变更
const handleCoverChange = (file: any) => {
  if (file.raw) {
    const coverUrl = URL.createObjectURL(file.raw);
    emit("update:coverUrl", coverUrl);
  }
};

// 清空视频和封面
const handleClear = () => {
  ElMessageBox.confirm(t("note.clearVideoConfirm"), t("common.tip"), {
    confirmButtonText: t("common.reupload"),
    cancelButtonText: t("common.cancel"),
    type: "warning",
    confirmButtonClass: "el-button--danger",
  })
    .then(() => {
      if (props.videoUrl && props.videoUrl.startsWith("blob:")) {
        URL.revokeObjectURL(props.videoUrl);
      }
      emit("update:videoUrl", "");
      emit("update:coverUrl", "");
      if (videoRef.value) {
        videoRef.value.onseeked = null;
      }
    })
    .catch(() => {
      // 用户点击取消，不做任何操作
    });
};

const reset = () => {
  // 使用 emit 更新父组件的值
  emit("update:videoUrl", "");
  emit("update:coverUrl", "");
  if (videoRef.value) {
    videoRef.value.src = "";
  }
};

// 暴露方法
defineExpose({
  reset,
});

// 添加资源清理
onBeforeUnmount(() => {
  if (props.videoUrl && props.videoUrl.startsWith("blob:")) {
    URL.revokeObjectURL(props.videoUrl);
  }
  if (props.coverUrl && props.coverUrl.startsWith("blob:")) {
    URL.revokeObjectURL(props.coverUrl);
  }
});
</script>

<style lang="less" scoped>
// 主题变量
@primary-color: #3d8af5;
@text-primary: rgba(51, 51, 51, 0.8);
@text-secondary: rgba(51, 51, 51, 0.6);
@border-color: rgba(0, 0, 0, 0.08);
@white: #fff;

.video-upload-container {
  .video-upload-area {
    background: #fff;
    border-radius: 8px;
    padding: 20px 20px;
    margin-top: 12px;
    text-align: center;
    // box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;

    :deep(.el-upload) {
      width: 100%;

      .el-upload-dragger {
        width: 100%;
        height: auto;
        padding: 60px 0;
        transition: border-color 0.3s;

        &:hover {
          border-color: @primary-color;
        }
      }

      // 拖拽文件到上传区域时的样式
      .el-upload-dragger.is-dragover {
        border-color: @primary-color;
        background-color: rgba(255, 36, 66, 0.06);
      }
    }

    .upload-placeholder {
      padding: 40px 0;

      .upload-icon {
        margin-bottom: 16px;
        img {
          width: 100px;
          height: 64px;
        }
      }

      .upload-text {
        color: #909399;
        font-size: 14px;
        margin-bottom: 8px;
      }

      .upload-tip {
        color: #909399;
        font-size: 12px;
        margin-bottom: 16px;
      }

      .upload-btn {
        padding: 9px 23px;
        font-size: 14px;
        border-radius: 4px;
        background-color: @primary-color;
        border-color: @primary-color;

        &:hover {
          background-color: @primary-color;
          border-color: @primary-color;
        }
      }
    }

    .upload-info {
      display: flex;
      justify-content: space-around;
      margin-top: 40px;
      padding: 0 40px;
      align-items: stretch;
      gap: 20px;

      .vertical-divider {
        width: 0;
        border-left: 1px dashed #dcdfe6;
      }

      .info-item {
        text-align: left;
        flex: 1;

        h4 {
          font-size: 14px;
          color: #333;
          margin-bottom: 8px;
        }

        p {
          font-size: 12px;
          color: #909399;
          line-height: 1.5;
          max-width: 200px;
        }
      }
    }
  }

  .video-preview-container {
    padding: 10px;
    position: relative;

    .video-title {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 10px;
    }

    .video-content {
      width: 50%;
      display: flex;
      margin-left: 20px;

      .video-wrapper {
        flex: 1;

        .uploaded-video {
          max-width: 100%;
          max-height: 350px;
          object-fit: contain;
          border-radius: 8px;
        }
      }
    }
  }

  .header {
    display: flex;
    padding: 12px;
    line-height: 2px;
    font-size: 16px;
    font-weight: 400;
    margin-top: 20px;
    border-bottom: 1px solid #f0f0f0;

    .header-left {
      align-items: center;

      .header-icon {
        position: relative;
        top: 2px;
        display: inline-block;
        width: 6px;
        height: 16px;
        background: @primary-color;
        border-radius: 3px;
        margin-right: 3px;
      }
    }

    .header-right {
      margin-left: auto;
      align-items: center;

      .action-buttons {
        display: flex;
        align-items: center;
        gap: 8px;

        :deep(.el-upload) {
          display: block;
        }

        .el-button {
          display: flex;
          align-items: center;

          .el-icon {
            margin-right: 4px;
          }
        }
      }
    }
  }

  .cover-container {
    margin: 10px;
    padding: 16px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #eee;

    .cover-title {
      font-size: 16px;
      font-weight: 500;
      margin-top: 15px;
      margin-bottom: 15px;
    }

    .cover-content {
      margin-left: 1vw;

      .cover-preview {
        display: flex;
        align-items: flex-start;

        img {
          width: 250px;
          height: 150px;
          object-fit: cover;
          border-radius: 4px;
        }

        .cover-actions {
          display: flex;
          gap: 8px;
        }

        .action-buttons {
          display: flex;
          flex-direction: column;
          margin-top: 18px;
          margin-left: 18px;
          gap: 10px;

          .action-btn {
            width: 100px;
          }

          :deep(.el-upload) {
            display: block;

            .el-button {
              width: 100px;
            }
          }
        }
      }

      .cover-empty {
        display: flex;
        gap: 8px;

        .loading-cover {
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }
}

/* 按钮悬浮效果 */
.el-button.el-button--danger.is-link {
  transition: all 0.3s ease;

  &:hover {
    color: @primary-color;
    transform: scale(1.1);
  }
}

/* 上传按钮悬浮效果 */
.upload-btn {
  transition: all 0.3s ease !important;

  &:hover {
    transform: scale(1.05);
  }
}
</style>