<template>
  <div class="edit-note-form">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" @submit.prevent="submitForm">
      <!-- 封面图 -->
      <el-form-item :label="$t('note.coverImage')" prop="cover">
        <div class="cover-upload">
          <el-upload
            class="upload-container"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="uploadCover"
          >
            <div class="cover-upload-trigger" v-if="!form.cover">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <span class="upload-text">{{ $t("note.uploadCoverText") }}</span>
            </div>
            <el-image v-else :src="form.cover" fit="cover" class="cover-preview">
              <template #error>
                <div class="image-error">
                  <el-icon>
                    <Picture />
                  </el-icon>
                </div>
              </template>
            </el-image>
          </el-upload>
          <el-button v-if="form.cover" type="primary" link class="remove-cover" @click="removeCover">
            {{ $t("note.removeCoverText") }}
          </el-button>
        </div>
      </el-form-item>

      <!-- 媒体内容 -->
      <el-form-item
        :label="form.productType === 1 ? $t('note.labelImage') : $t('note.labelVideo')"
        prop="imgList"
        class="media-form-item"
      >
        <div class="media-upload">
          <!-- 图片上传 -->
          <el-upload
            v-if="form.productType === 1"
            class="img-upload-container"
            :file-list="fileList"
            list-type="picture-card"
            :before-upload="beforeUpload"
            :http-request="uploadMedia"
            :on-remove="handleRemove"
            :on-preview="handlePreview"
            :limit="9"
            :on-exceed="handleExceed"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>

          <!-- 视频上传 -->
          <div v-else class="video-upload">
            <el-upload
              class="video-upload-container"
              :show-file-list="false"
              :before-upload="beforeVideoUpload"
              :http-request="uploadVideo"
              :class="{ 'has-video': form.imgList?.length }"
            >
              <div v-if="!form.imgList?.length" class="video-upload-trigger">
                <el-icon><VideoCamera /></el-icon>
                <span>{{ $t("note.uploadVideoText") }}</span>
              </div>
              <div v-else class="video-container">
                <video :src="form.imgList[0]" class="video-preview" controls preload="metadata"></video>
              </div>
              <el-button v-if="form.imgList?.length" type="primary" link class="remove-video-btn" @click="removeVideo">
                {{ $t("note.removeVideoText") }}
              </el-button>
            </el-upload>
          </div>
        </div>
      </el-form-item>

      <!-- 标题 -->
      <el-form-item :label="$t('note.labelTitleShort')" prop="title">
        <el-input
          class="input-title"
          v-model="form.title"
          :placeholder="$t('note.titlePlaceholder')"
          maxlength="20"
          show-word-limit
          @change="handleFormChange"
        />
      </el-form-item>

      <!-- 内容 -->
      <el-form-item :label="$t('note.labelContentShort')" prop="content">
        <el-input
          class="input-content"
          v-model="form.content"
          type="textarea"
          :rows="5"
          :placeholder="$t('common.inputContent')"
          maxlength="250"
          show-word-limit
          @change="handleFormChange"
        />
      </el-form-item>

      <!-- 分类选择 -->
      <el-form-item :label="$t('note.labelCategoryShort')" prop="category">
        <el-cascader
          ref="cascaderRef"
          v-model="form.cpid"
          :options="categoryOptions"
          :props="cascaderProps"
          :placeholder="$t('note.selectCategoryPh')"
          clearable
          @change="handleCategoryChange"
        />
      </el-form-item>

      <!-- 发货方式  -->
      <el-form-item :label="$t('note.deliveryMethod')" prop="postType">
        <el-radio-group v-model="form.postType" required>
          <el-radio :label="0">{{ $t("common.mail") }}</el-radio>
          <el-radio :label="1">{{ $t("common.selfPickup") }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 价格 -->
      <el-form-item :label="$t('note.price')">
        <el-input-number
          required
          controls-position="right"
          v-model="form.price"
          :precision="2"
          :step="1"
          :min="0"
          :placeholder="$t('note.priceInputPh')"
        ></el-input-number>
      </el-form-item>

      <!-- 原价 -->
      <el-form-item :label="$t('note.originalPrice')">
        <el-input-number
          required
          controls-position="right"
          v-model="form.originalPrice"
          :precision="2"
          :step="1"
          :min="0"
          :placeholder="$t('note.originalPriceInputPh')"
        >
        </el-input-number>
      </el-form-item>

      <!-- 位置 -->
      <el-form-item :label="$t('note.labelLocationShort')">
        <div class="location-container" @click="handleGetLocation">
          <el-icon><Location /></el-icon>
          <span class="location-text">{{ form.location || $t("note.clickGetLocation") }}</span>
        </div>
      </el-form-item>

      <el-form-item>
        <div class="dialog-footer">
          <el-button @click="cancel" :disabled="loading">{{ $t("common.cancel") }}</el-button>
          <el-button type="primary" @click="submitForm" :loading="loading" :disabled="loading">
            {{ loading ? $t("common.saving") : $t("common.save") }}
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <!-- 图片预览框 -->
    <el-dialog v-model="dialogVisible" :title="$t('note.imagePreview')" width="30%" :style="{ marginTop: '15vh' }" center>
      <img :src="dialogImageUrl" alt="Preview Image" class="preview-image" />
    </el-dialog>

    <!-- 地址选择抽屉 -->
    <el-drawer
      v-model="drawer"
      direction="rtl"
      size="35%"
      :title="$t('note.selectLocation')"
      :show-close="false"
      @close-drawer="closeDrawer"
    >
      <AddressEdit @close-drawer="closeDrawer" @select-location="handleLocationSelect" />
    </el-drawer>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted, computed } from "vue";
import { useI18n } from "vue-i18n";
import { Location, Plus, Picture, VideoCamera } from "@element-plus/icons-vue";
import type { FormInstance, FormRules, ElInput, UploadProps, UploadUserFile } from "element-plus";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/userStore";
import { getShopCategoryTreeData } from "@/api/idleCategory";
import { uploadFileMethod } from "@/utils/util";
import AddressEdit from "@/views/push/address-edit.vue";

const { t } = useI18n();

const props = defineProps({
  productData: {
    type: Object,
    required: true,
  },
});

const loading = ref(false);

const cascaderProps = {
  value: "id",
  label: "title",
  children: "children",
  checkStrictly: true, // 允许选择任意一级
};

const emit = defineEmits(["save", "cancel", "change", "save-complete"]);

const userInfo = ref<any>({});
const userStore = useUserStore();

const drawer = ref(false);
const cascaderRef = ref();
const locationInfo = ref("");

const formRef = ref<FormInstance>();
const categoryOptions = ref<any[]>([]);
const fileList = ref<{ name: string; url: string; status: string }[]>([]);

// 预览相关
const dialogImageUrl = ref("");
const dialogVisible = ref(false);

// 表单校验规则
const rules = computed<FormRules>(() => ({
  title: [
    { required: true, message: t("note.titleRuleRequired"), trigger: "blur" },
    { min: 2, max: 50, message: t("note.titleRuleLength"), trigger: "blur" },
  ],
  cpid: [{ required: true, message: t("note.categoryRuleRequired"), trigger: "change" }],
  imgList: [
    {
      type: "array",
      required: true,
      message: t("note.mediaRuleRequired"),
      trigger: "change",
    },
    {
      type: "array",
      max: 9,
      message: t("note.ruleMaxNineImages"),
      trigger: "change",
      validator: (_rule, value) => {
        if (form.value.productType === 1 && value.length > 9) {
          return false;
        }
        return true;
      },
    },
  ],
}));

// 表单数据
const form = ref({
  id: "",
  title: "",
  content: "",
  location: "",
  cover: "", // 保存临时预览URL
  productType: 1,
  imgList: [] as string[], // 保存临时预览URL
  cid: "",
  cpid: "",
  postType: 0,
  price: "",
  originalPrice: "",
  longitude: undefined as number | undefined,
  latitude: undefined as number | undefined,
  address: "",
  locationName: "",
  province: "",
  city: "",
});

const handleLocationSelect = (location: any) => {
  if (location) {
    const locationStr = `${location.province || ""} ${location.city || ""} ${location.name || ""}`.trim();
    locationInfo.value = locationStr;
    form.value = {
      ...form.value,
      longitude: location.location.lng,
      latitude: location.location.lat,
      address: location.address,
      locationName: location.name,
      province: location.province,
      city: location.city,
      location: locationStr,
    };
  }
  drawer.value = false;
};

const closeDrawer = () => {
  drawer.value = false;
};

// 初始化分类数据
const initCategories = async () => {
  loading.value = true;
  try {
    const res = await getShopCategoryTreeData();
    if (Array.isArray(res.data)) {
      categoryOptions.value = res.data;
    } else {
      ElMessage.error(t("note.getCategoryFail"));
    }
  } catch (error) {
    console.error("获取分类失败:", error);
    ElMessage.error(t("note.getCategoryFail"));
  } finally {
    loading.value = false;
  }
};

// 添加分类变化处理函数
// 添加分类变化处理函数
const handleCategoryChange = (value: string[]) => {
  // 添加空值判断
  if (!value || value.length === 0) {
    form.value.cpid = ""; // 清空分类
  } else {
    form.value.cpid = value[value.length - 1]; // 保存最后一级分类ID
  }
  handleFormChange();
  // 选中后关闭下拉框
  cascaderRef.value?.togglePopperVisible();
};

// 处理超出限制
const handleExceed = () => {
  ElMessage.warning(t("note.ruleMaxNineImages"));
};

// 修改图片上传前检查
const beforeUpload = (file: File) => {
  // 检查文件类型
  const isImage = file.type.startsWith("image/");
  if (!isImage) {
    ElMessage.error(t("note.onlyImageFile"));
    return false;
  }

  // 检查文件大小（限制为20MB）
  const maxSize = 20 * 1024 * 1024; // 20MB
  if (file.size > maxSize) {
    ElMessage.error(t("note.imageMaxSize20Mb"));
    return false;
  }

  // 检查数量限制
  if (form.value.productType === 1 && form.value.imgList.length >= 9) {
    ElMessage.warning(t("note.ruleMaxNineImages"));
    return false;
  }

  return true;
};

// 视频上传前检查
const beforeVideoUpload: UploadProps["beforeUpload"] = (file) => {
  const isVideo = /^video\//.test(file.type);
  const maxSize = 20 * 1024 * 1024; // 20MB

  if (!isVideo) {
    ElMessage.error(t("note.onlyVideoFile"));
    return false;
  }
  if (file.size > maxSize) {
    ElMessage.error(t("note.videoSizeExceeded20"));
    return false;
  }

  // 显示上传中状态
  ElMessage.info(t("note.videoUploadingInfo"));
  return true;
};

// 监听props更新表单
watch(
  () => props.productData,
  (newVal) => {
    if (newVal) {
      // 统一使用后端返回的 urls 作为图片/视频的唯一数据源（与笔记编辑保持一致）
      const parsedUrls =
        newVal.urls && typeof newVal.urls === "string"
          ? (() => {
              try {
                return JSON.parse(newVal.urls);
              } catch {
                return [];
              }
            })()
          : Array.isArray(newVal.urls)
          ? newVal.urls
          : [];

      form.value = {
        id: newVal.id,
        title: newVal.title,
        content: newVal.description,
        // 与商品详情展示规则一致：使用 省 市 区 + 详细地址 作为展示文案
        location: `${newVal.province || ""} ${newVal.city || ""} ${newVal.district || ""} ${newVal.address || ""}`.trim(),
        cover: newVal.cover,
        productType: newVal.productType,
        imgList:
          newVal.productType === 1
            ? parsedUrls // 多图商品：使用 urls 数组
            : parsedUrls.length
            ? [parsedUrls[0]] // 视频商品：只取第一个作为视频地址
            : [],
        cid: newVal.cid || "",
        cpid: newVal.cpid || "",
        postType: typeof newVal.postType === "number" ? newVal.postType : Number(newVal.postType),
        price: newVal.price,
        originalPrice: newVal.originalPrice,
        // 回填位置信息，便于不修改位置时直接保存
        longitude: undefined as number | undefined,
        latitude: undefined as number | undefined,
        address: newVal.address || "",
        locationName: "",
        province: newVal.province || "",
        city: newVal.city || "",
      };
      // 更新文件列表(仅图片模式)
      if (newVal.productType === 1) {
        fileList.value = form.value.imgList.map((url, index) => ({
          name: t("note.imageOrdinal", { n: index + 1 }),
          url,
          status: "success",
        }));
      }
    }
  },
  { immediate: true }
);

watch(
  () => form.value.productType,
  (newType, oldType) => {
    if (newType !== oldType) {
      // 清理之前的媒体内容
      if (form.value.imgList.length) {
        form.value.imgList = [];
        fileList.value = [];
      }
      handleFormChange();
    }
  }
);

onMounted(async () => {
  userInfo.value = userStore.getUserInfo();
  if (userInfo.value?.id) {
    // 并行加载数据
    await Promise.all([initCategories()]);
  } else {
    ElMessage.error(t("note.getUserFail"));
  }
});

// 表单变化
const handleFormChange = () => {
  emit("change", true);
};

// 获取位置
const handleGetLocation = () => {
  drawer.value = true;
};

// 修改封面上传处理：前端直接上传到文件服务，拿到 URL 后回填
const uploadCover = async ({ file }: any) => {
  try {
    const res: any = await uploadFileMethod(file);
    if (res.code === 200 && res.data) {
      form.value.cover = res.data;
      handleFormChange();
    } else {
      throw new Error(res.msg || t("note.coverUploadFail"));
    }
  } catch (error) {
    console.error("封面上传失败:", error);
    ElMessage.error(t("note.coverUploadFail"));
  }
};

// 修改图片上传处理：前端直接上传到文件服务，拿到 URL 后 push 到 imgList
const uploadMedia = async ({ file }: any) => {
  // 检查数量限制
  if (form.value.imgList.length >= 9) {
    ElMessage.warning(t("note.ruleMaxNineImages"));
    return;
  }
  try {
    const res: any = await uploadFileMethod(file);
    if (res.code === 200 && res.data) {
      const url = res.data;
      form.value.imgList.push(url);
      fileList.value.push({
        name: file.name,
        url,
        status: "success",
      });
      handleFormChange();
    } else {
      throw new Error(res.msg || t("note.imageUploadFail"));
    }
  } catch (error) {
    console.error("图片上传失败:", error);
    ElMessage.error(t("note.imageUploadFail"));
  }
};

// 修改视频上传处理：前端直接上传视频到文件服务
const uploadVideo = async ({ file }: any) => {
  try {
    // 检查文件类型
    if (!file.type.startsWith("video/")) {
      ElMessage.error(t("note.uploadVideoFilePlease"));
      return;
    }
    // 检查文件大小
    const maxSize = 20 * 1024 * 1024; // 20MB
    if (file.size > maxSize) {
      ElMessage.error(t("note.videoSizeExceeded20"));
      return;
    }

    const res: any = await uploadFileMethod(file);
    if (res.code === 200 && res.data) {
      const url = res.data;
      form.value.imgList = [url];
      handleFormChange();
      ElMessage.success(t("note.videoUploadOk"));
    } else {
      throw new Error(res.msg || t("note.uploadVideoGenericFail"));
    }
  } catch (error) {
    console.error("视频上传失败:", error);
    ElMessage.error(t("note.uploadVideoGenericFail"));
  }
};

// 修改移除处理：与笔记表单一致，只维护 URL 列表与文件列表
const handleRemove = (file: any) => {
  const index = fileList.value.findIndex((f) => f.url === file.url);
  if (index !== -1) {
    fileList.value.splice(index, 1);
    form.value.imgList.splice(index, 1);
    handleFormChange();
  }
};

const removeCover = () => {
  form.value.cover = "";
  handleFormChange();
};

const removeVideo = () => {
  form.value.imgList = [];
  handleFormChange();
  ElMessage.success(t("note.videoRemovedOk"));
};

// 处理图片预览
const handlePreview = (file: UploadUserFile) => {
  dialogImageUrl.value = file.url!;
  dialogVisible.value = true;
};

// 提交表单：与笔记编辑一致，仅携带包含 URL 的 productData
const submitForm = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    loading.value = true;
    const payload: any = { ...form.value };
    // 将图片/视频 URL 统一写入 urls 字段，便于后端直接使用
    // 图片模式：以当前 fileList 为准，确保删除/新增后的顺序和数量与界面一致（与笔记编辑保持一致）
    if (payload.productType === 1) {
      payload.urls = fileList.value.map((f) => f.url);
    } else if (payload.productType === 2) {
      payload.urls = form.value.imgList && form.value.imgList.length ? [form.value.imgList[0]] : [];
    }
    const formData = new FormData();
    formData.append("productData", JSON.stringify(payload));
    emit("save", formData);
  } catch (error) {
    loading.value = false;
  } finally {
    loading.value = false;
  }
};

// 取消编辑
const cancel = () => {
  emit("cancel");
};
</script>

<style lang="less" scoped>
.edit-note-form {
  margin-top: -10px;

  // 封面图样式
  .cover-upload {
    display: flex;
    align-items: flex-start; // 改为顶部对齐
    gap: 12px;

    .upload-container {
      width: 150px;
      height: 150px;
      border: 1px dashed #d9d9d9;
      border-radius: 8px;
      overflow: hidden; // 添加溢出隐藏
      position: relative;

      &:hover {
        border-color: #409eff;
      }

      .cover-upload-trigger {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 100%;
        color: #8c939d;

        .upload-icon {
          font-size: 28px;
          margin-bottom: 8px;
        }

        .upload-text {
          font-size: 14px;
        }
      }

      .cover-preview {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .image-error {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #999;
        font-size: 24px;
      }
    }

    .remove-cover {
      align-self: flex-start; // 按钮靠上对齐
      margin-top: 60px;
    }
  }

  // 媒体上传区域样式
  .media-form-item {
    margin-top: 24px;

    .media-upload {
      .img-upload-container {
        width: 100%;
        border-radius: 8px;
        cursor: pointer;
        transition: border-color 0.3s;

        // 一行显示 3 张图，固定宽高（与笔记编辑保持一致）
        :deep(.el-upload-list--picture-card) {
          display: flex;
          flex-wrap: wrap;
          gap: 12px;
        }

        :deep(.el-upload-list--picture-card .el-upload-list__item) {
          width: 120px;
          height: 120px;
          margin: 0;
        }

        :deep(.el-upload--picture-card) {
          width: 120px;
          height: 120px;
          margin: 0;
          line-height: 120px;
        }

        &:hover {
          border-color: #409eff;
        }
      }

      .video-upload {
        .video-upload-container {
          width: 100%;
          border: 1px dashed #d9d9d9;
          border-radius: 8px;
          cursor: pointer;
          transition: border-color 0.3s;
          padding: 16px; /* 添加内边距 */
          box-sizing: border-box; /* 确保内边距不影响总宽度 */

          &:hover {
            border-color: #409eff;
          }

          &.has-video {
            border: none;
            padding: 0; /* 预览时不需要内边距 */
          }

          .video-upload-trigger {
            width: 100%;
            height: 180px; /* 固定高度 */
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #8c939d;

            .el-icon {
              font-size: 32px;
              margin-bottom: 8px;
            }

            span {
              font-size: 14px;
            }
          }

          .video-container {
            position: relative;
            width: 100%;
            // border: 1px dashed #d9d9d9;
            max-height: 400px; /* 限制最大高度 */
            border-radius: 8px;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            gap: 12px;
            margin-right: 12px; // 添加右侧间距

            .video-preview {
              width: 100%;
              max-height: 360px; /* 视频预览区域高度 */
              object-fit: contain; /* 保持视频原始比例 */
              background: #000; /* 黑色背景更适合视频 */
            }

            .remove-video-btn {
              align-self: flex-start; // 按钮靠上对齐
              margin-top: 120px;
            }
          }
        }
      }
    }
  }

  .input-title {
    width: 350px;
    font-size: 15px;
  }

  .input-content {
    font-size: 14px;
    width: 500px;
  }

  .related-products {
    margin-bottom: 16px;

    label {
      font-size: 14px;
      color: #666;
      margin-right: 8px;
    }

    .products-container {
      margin-top: 12px;
      display: flex;
      gap: 12px;
      align-items: flex-start;
    }

    .selected-products {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }

    .product-card {
      position: relative;
      width: 160px;
      background: #fff;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);

        .remove-btn {
          opacity: 1;
        }
      }

      .product-img {
        width: 100%;
        height: 100px;
        object-fit: cover;
        display: block;
      }

      .product-info {
        padding: 8px;

        .info-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 8px;

          .product-title {
            font-size: 13px;
            color: #333;
            margin-bottom: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .product-price {
            font-size: 15px;
            color: #ff2442;
            font-weight: 500;
          }
        }
      }

      .remove-btn {
        position: absolute;
        top: 4px;
        right: 4px;
        width: 20px;
        height: 20px;
        border-radius: 50%;
        background: rgba(0, 0, 0, 0.5);
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        opacity: 0;
        transition: all 0.3s;

        &:hover {
          background: rgba(0, 0, 0, 0.7);
        }
      }
    }

    .add-product-btn {
      width: 160px;
      height: 144px; // 与商品卡片等高
      border: 1px dashed #dcdfe6;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s;
      color: #909399;

      &:hover {
        border-color: #ff2442;
        color: #ff2442;
      }

      .el-icon {
        font-size: 24px;
        margin-bottom: 8px;
      }

      span {
        font-size: 13px;
      }
    }
  }

  .product-selector {
    .search-bar {
      margin-bottom: 16px;
    }

    .product-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 16px;
      margin-bottom: 16px;

      .product-item {
        position: relative;
        border: 1px solid #eee;
        border-radius: 8px;
        overflow: hidden;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        &.active {
          border-color: #ff2442;
        }

        .el-image {
          width: 100%;
          height: 180px;
          background-color: #f5f5f5;
        }

        .product-info {
          padding: 8px;

          .info-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 8px;

            .product-title {
              font-size: 14px;
              margin-bottom: 4px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            .product-price {
              color: #ff2442;
              font-size: 16px;
              font-weight: 500;
            }
          }
        }

        .product-mask {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(255, 36, 66, 0.1);
          display: flex;
          align-items: center;
          justify-content: center;
          color: #ff2442;
          font-size: 24px;
        }
      }
    }

    .pagination {
      display: flex;
      justify-content: center;
    }

    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      padding: 0 24px 12px 24px;
    }
  }

  .tags-container {
    .tags-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .el-tag {
        margin-right: 8px;
        margin-bottom: 8px;
        background-color: rgba(0, 0, 0, 0.03);
        border: none;
        color: #333;

        &:hover {
          background-color: rgba(0, 0, 0, 0.06);
        }
      }

      .tag-input {
        width: 100px;
        margin-right: 8px;
        vertical-align: bottom;
      }

      .button-new-tag {
        height: 32px;
        padding: 0 8px;
        border-radius: 16px;
        border: 1px dashed #d9d9d9;

        &:hover {
          border-color: #409eff;
          color: #409eff;
        }

        &:disabled {
          cursor: not-allowed;
          border-color: #d9d9d9;
          color: #999;
        }
      }
    }

    .tags-tip {
      font-size: 12px;
      color: #999;
      margin-bottom: -10px;
    }
  }

  .location-container {
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    transition: all 0.3s;
    color: #1989fa;

    &:hover {
      color: #ff2442;
    }

    .location-text {
      font-size: 14px;
    }
  }

  .dialog-footer {
    display: flex;
    margin-left: auto;
    justify-content: flex-end;
    gap: 12px;
    padding: 0 24px 12px 24px;
  }

  .preview-image {
    max-width: 100%;
    max-height: 60vh;
    object-fit: contain;
  }
}

// 自定义 drawer 内容区域
:deep(.el-drawer__body) {
  padding: 0; // 移除默认内边距
  height: 100%; // 确保内容区域占满高度
  overflow: hidden; // 防止内容溢出
}

// 自定义 drawer 本身
:deep(.el-drawer) {
  background: #f5f5f5;
}

:deep(.el-drawer__header) {
  display: none;
}
</style>