<template>
  <div class="image-upload-container">
    <!-- 未上传图片时显示的界面 -->
    <template v-if="fileList.length === 0">
      <div class="image-upload-area">
        <el-upload
          v-model:file-list="fileList"
          class="image-uploader"
          action=""
          :limit="maxImageCount"
          :headers="uploadHeader"
          :auto-upload="false"
          :on-change="handleImageChange"
          :on-exceed="handleExceed"
          multiple
          drag
        >
          <div class="upload-placeholder">
            <div class="upload-icon">
              <img src="//fe-static.xhscdn.com/formula-static/ugc/public/img/upload-large.e52d3e7.png" :alt="$t('note.uploadIcon')" />
            </div>
            <div class="upload-text">{{ $t("note.dragOrClickUpload") }}</div>
            <div class="upload-tip">({{ $t("note.maxImages", { count: maxImageCount }) }})</div>
            <el-button type="primary" class="upload-btn">{{ $t("note.uploadImage") }}</el-button>
          </div>
        </el-upload>

        <!-- 上传说明 -->
        <div class="upload-info">
          <div class="info-item">
            <h4>{{ $t("note.imageSize") }}</h4>
            <p>{{ $t("note.imageSizeDesc") }}</p>
          </div>
          <div class="vertical-divider"></div>
          <div class="info-item">
            <h4>{{ $t("note.imageFormat") }}</h4>
            <p>{{ $t("note.imageFormatDesc") }}</p>
          </div>
          <div class="vertical-divider"></div>
          <div class="info-item">
            <h4>{{ $t("note.imageResolution") }}</h4>
            <p>{{ $t("note.imageResolutionDesc") }}</p>
          </div>
        </div>
      </div>
    </template>

    <!-- 上传后显示的界面 -->
    <template v-else>
      <div class="header">
        <div class="header-left">
          <span class="header-icon"></span>
          <span class="header-title">{{ $t("note.publishIdle") }}</span>
          <!-- <span class="image-count">({{ fileList.length }}/{{ maxImageCount }})</span> -->
        </div>
        <div class="header-right">
          <el-button type="primary" link @click="handleClearAndReupload">
            <el-icon><RefreshRight /></el-icon>
            {{ $t("note.clearAndReupload") }}
          </el-button>
        </div>
      </div>

      <!-- 图片列表 -->
      <div class="img-list">
        <div class="header-left">
          <span class="header-title">{{ $t("note.imageEdit") }}</span>
          <span class="image-count">({{ fileList.length }}/{{ maxImageCount }})</span>
        </div>
        <el-upload
          v-model:file-list="fileList"
          class="image-uploader"
          action=""
          list-type="picture-card"
          :limit="maxImageCount"
          :headers="uploadHeader"
          :auto-upload="false"
          :on-change="handleImageChange"
          :on-preview="handlePreview"
          :on-exceed="handleExceed"
          multiple
          drag
        >
          <el-icon><Plus /></el-icon>
        </el-upload>

        <!-- 图片预览对话框 -->
        <el-dialog v-model="dialogVisible" :title="$t('note.imagePreview')" width="30%" :style="{ marginTop: '15vh' }" center>
          <img :src="dialogImageUrl" alt="Preview Image" class="preview-image" />
        </el-dialog>
      </div>

      <!-- 封面图显示区域 -->
      <div class="cover-container">
        <div class="cover-title">{{ $t("note.coverImage") }}</div>
        <div class="cover-content">
          <div class="cover-preview">
            <img :src="fileList[0]?.url" :alt="$t('note.coverImage')" class="cover-image" />
            <div class="cover-actions">
              <el-button type="primary" link @click="handleChangeCover" v-if="fileList.length > 1">
                <el-icon><RefreshLeft /></el-icon>
                {{ $t("note.changeCover") }}
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 封面选择对话框 -->
      <el-dialog v-model="coverDialogVisible" :title="$t('note.selectCover')" width="50%" :close-on-click-modal="false">
        <div class="cover-selector">
          <div
            v-for="(item, index) in fileList"
            :key="index"
            class="cover-option"
            :class="{ active: index === selectedImageIndex }"
            @click="setCoverImage(index)"
          >
            <img :src="item.url" :alt="$t('note.imageOrdinal', { n: index + 1 })" />
            <div class="cover-mask">
              <el-icon v-if="index === selectedImageIndex"><Check /></el-icon>
            </div>
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="cancelCoverChange">{{ $t("common.cancel") }}</el-button>
            <el-button type="primary" @click="confirmCoverChange">{{ $t("common.confirm") }}</el-button>
          </span>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage, ElMessageBox } from "element-plus";

const { t } = useI18n();
import type { UploadUserFile } from "element-plus";
import { Plus, RefreshRight, RefreshLeft, Check } from "@element-plus/icons-vue";

const props = defineProps({
  uploadHeader: {
    type: Object,
    required: true,
  },
  maxImageCount: {
    type: Number,
    default: 9,
  },
});

const emit = defineEmits(["update:fileList"]);

// 文件列表
const fileList = ref<UploadUserFile[]>([]);
const originalFileList = ref<UploadUserFile[]>([]);

// 预览相关
const dialogImageUrl = ref("");
const dialogVisible = ref(false);

// 封面相关
const coverDialogVisible = ref(false);
const coverIndex = ref(0);
const tempCoverIndex = ref(0);
const selectedImageIndex = ref(0);

// 暴露重置方法给父组件
defineExpose({
  reset: () => {
    fileList.value = [];
  },
});

// 处理图片变化
const handleImageChange = (file: UploadUserFile) => {
  // 验证图片
  if (!validateImage(file)) {
    return false;
  }
  // 检查图片数量限制
  if (fileList.value.length >= props.maxImageCount) {
    ElMessage.warning(t("note.maxImagesCount", { n: props.maxImageCount }));
    return false;
  }
  // 创建预览URL
  if (!file.url && file.raw) {
    const url = URL.createObjectURL(file.raw);
    fileList.value.push({
      name: file.name,
      url: url,
      raw: file.raw,
    });
  }
  emit("update:fileList", fileList.value);
};

// 验证图片
const validateImage = (file: UploadUserFile) => {
  // 验证文件类型
  const acceptTypes = ["image/jpeg", "image/png", "image/webp"];
  if (!acceptTypes.includes(file.raw?.type || "")) {
    ElMessage.error(t("note.imageFormatsJpgPngWebp"));
    return false;
  }
  // 验证文件大小
  const maxSize = 20 * 1024 * 1024; // 20MB
  if (file.raw?.size && file.raw.size > maxSize) {
    ElMessage.error(t("note.imageMaxSize20Mb"));
    return false;
  }
  return true;
};

// 处理图片预览
const handlePreview = (file: UploadUserFile) => {
  dialogImageUrl.value = file.url!;
  dialogVisible.value = true;
};

// 处理超出限制
const handleExceed = () => {
  ElMessage.warning(t("note.maxImagesCountSpace", { n: props.maxImageCount }));
};

// 清空并重新上传
const handleClearAndReupload = () => {
  ElMessageBox.confirm(t("note.reuploadClearImagesConfirm"), t("common.tip"), {
    confirmButtonText: t("common.reupload"),
    cancelButtonText: t("common.cancel"),
    type: "warning",
    confirmButtonClass: "el-button--danger",
  })
    .then(() => {
      fileList.value = [];
      emit("update:fileList", []);
    })
    .catch(() => {
      // 用户点击取消，不做任何操作
    });
};

// 处理更换封面
const handleChangeCover = () => {
  selectedImageIndex.value = 0; // 默认选中第一张图片
  tempCoverIndex.value = coverIndex.value;
  originalFileList.value = [...fileList.value];
  coverDialogVisible.value = true;
};

// 设置封面图片
const setCoverImage = (index: number) => {
  selectedImageIndex.value = 0;
  tempCoverIndex.value = index;
  // 立即更新封面图位置
  const newFileList = [...fileList.value];
  const [selected] = newFileList.splice(index, 1);
  newFileList.unshift(selected);
  fileList.value = newFileList;
  emit("update:fileList", fileList.value);
};

// 确认更换封面
const confirmCoverChange = () => {
  coverIndex.value = tempCoverIndex.value;
  coverDialogVisible.value = false;
  originalFileList.value = [];
};

// 取消更换封面时恢复原始状态
const cancelCoverChange = () => {
  // 恢复到打开对话框时的状态
  fileList.value = [...originalFileList.value];
  emit("update:fileList", fileList.value);
  coverDialogVisible.value = false;
  tempCoverIndex.value = coverIndex.value;
};

// 组件销毁时清理资源
watch(
  () => fileList.value,
  (newFiles, oldFiles) => {
    // 清理不再使用的blob URL
    oldFiles?.forEach((file) => {
      if (file.url?.startsWith("blob:") && !newFiles.some((newFile) => newFile.url === file.url)) {
        URL.revokeObjectURL(file.url);
      }
    });
  },
  { deep: true }
);
</script>

<style lang="less" scoped>
// 主题变量
@primary-color: #3d8af5;
@text-primary: rgba(51, 51, 51, 0.8);
@text-secondary: rgba(51, 51, 51, 0.6);
@border-color: rgba(0, 0, 0, 0.08);
@white: #fff;

.image-upload-container {
  .image-upload-area {
    background: #fff;
    border-radius: 8px;
    padding: 20px 20px;
    text-align: center;

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
        }
      }
    }
  }

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    margin-top: 10px;
    border-bottom: 1px solid #f0f0f0;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;

      .header-icon {
        width: 4px;
        height: 16px;
        background: @primary-color;
        border-radius: 2px;
      }

      .header-title {
        font-size: 16px;
        font-weight: 500;
      }

      .image-count {
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .img-list {
    padding: 16px;
    margin: 20px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #eee;

    :deep(.el-upload--picture-card) {
      width: 100px;
      height: 100px;
    }

    :deep(.el-upload-list--picture-card .el-upload-list__item) {
      width: 100px;
      height: 100px;
    }

    .header-title {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 16px;
    }

    .image-count {
      font-size: 14px; /* 可以调整字体大小 */
      color: #909399; /* 使用较浅的颜色 */
      margin-left: 4px; /* 和"发布图文"之间的间距 */
    }
  }

  .image-uploader {
    margin-top: 12px;
  }

  .preview-image {
    max-width: 100%;
    max-height: 60vh;
    object-fit: contain;
  }

  .cover-container {
    margin: 20px;
    padding: 16px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #eee;

    .cover-title {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 10px;
    }

    .cover-content {
      .cover-preview {
        display: flex;
        align-items: flex-start;
        gap: 16px;

        .cover-image {
          width: 150px;
          height: 150px;
          object-fit: cover;
          border-radius: 4px;
        }

        .cover-actions {
          padding-top: 8px;
        }
      }
    }
  }

  .cover-selector {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 16px;
    padding: 16px;

    .cover-option {
      position: relative;
      cursor: pointer;
      border-radius: 8px;
      overflow: hidden;
      aspect-ratio: 1;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .cover-mask {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s;

        .el-icon {
          font-size: 24px;
          color: #fff;
        }
      }

      &:hover .cover-mask {
        opacity: 1;
      }

      &.active {
        border: 2px solid @primary-color;

        .cover-mask {
          opacity: 1;
          background: rgba(255, 36, 66, 0.3);
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
</style>