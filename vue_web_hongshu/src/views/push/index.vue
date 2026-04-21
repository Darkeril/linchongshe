<template>
  <div class="container push-page" id="container">
    <div v-if="isLogin" :class="['push-container', { 'with-preview': fileList.length > 0 || videoUrl }]">
      <!-- 标签切换 -->
      <div class="tab-container" v-if="!fileList.length && !videoUrl">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          :class="['tab-item', { active: currentTab === tab.key }]"
          @click="handleTabChange(tab.key)"
        >
          {{ tab.label }}
        </div>
      </div>

      <!-- 内容类型选择（与动态页笔记/商品胶囊一致：轨道 + 滑块） -->
      <div class="content-type-container" v-if="!fileList.length && !videoUrl">
        <div class="content-type-track">
          <div
            class="content-type-slider"
            :style="{ transform: contentType === 'video' ? 'translateX(100%)' : 'translateX(0)' }"
          ></div>
          <div
            class="content-type-tab"
            :class="{ active: contentType === 'image' }"
            @click="handleContentTypeChange('image')"
          >
            {{ $t("note.imageText") }}
          </div>
          <div
            class="content-type-tab"
            :class="{ active: contentType === 'video' }"
            @click="handleContentTypeChange('video')"
          >
            {{ $t("dashboard.video") }}
          </div>
        </div>
      </div>

      <!-- 笔记上传组件 -->
      <template v-if="currentTab === 'note'">
        <ImageUpload
          ref="imageUploadRef"
          v-if="contentType === 'image'"
          v-model:fileList="fileList"
          :uploadHeader="uploadHeader"
          @update:fileList="handleFileListUpdate"
        />
        <VideoUpload
          ref="videoUploadRef"
          v-if="contentType === 'video'"
          v-model:videoUrl="videoUrl"
          v-model:coverUrl="coverUrl"
          :uploadHeader="uploadHeader"
        />
      </template>

      <!-- 闲置上传组件 -->
      <template v-if="currentTab === 'idle'">
        <ImageUpload
          ref="imageUploadRef"
          v-if="contentType === 'image'"
          v-model:fileList="fileList"
          :uploadHeader="uploadHeader"
          @update:fileList="handleFileListUpdate"
        />
        <VideoUpload
          ref="videoUploadRef"
          v-if="contentType === 'video'"
          v-model:videoUrl="videoUrl"
          v-model:coverUrl="coverUrl"
          :uploadHeader="uploadHeader"
        />
      </template>

      <!-- 内容编辑区域 -->
      <ContentEditor
        v-if="showContentEditor"
        v-model:note="note"
        v-model:categoryList="categoryList"
        v-model:shopCategoryList="shopCategoryList"
        v-model:album="album"
        v-model:locationInfo="locationInfo"
        v-model:dynamicTags="dynamicTags"
        :currentTab="currentTab"
        :contentType="contentType"
        :loading="pushLoading"
        :disabled="pushLoading"
        :publishing="pushLoading"
        :fileList="fileList"
        :videoUrl="videoUrl"
        :coverUrl="coverUrl"
        @publish="publish"
        @reset="resetData"
        @get-location="getLocation"
      />

      <!-- 移动端预览 -->
      <PreviewMobile
        v-if="fileList.length > 0 || videoUrl"
        :key="previewKey"
        :fileList="fileList"
        :videoUrl="videoUrl"
        :note="note"
        :userInfo="userInfo"
        :currentTab="currentTab"
        :contentType="contentType"
      />
    </div>
    <div v-else>
      <el-empty :description="$t('common.notLoggedIn')" />
    </div>

    <!-- 地址选择抽屉 -->
    <el-drawer
      v-model="drawer"
      direction="rtl"
      size="40%"
      :title="$t('note.selectLocation')"
      :show-close="false"
      @close-drawer="closeDrawer"
    >
      <AddressEdit @close-drawer="closeDrawer" @select-location="handleLocationSelect" />
    </el-drawer>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from "vue";
import { useRoute } from "vue-router";
import { useUserStore } from "@/store/userStore";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import type { UploadUserFile, MessageHandler } from "element-plus";
import { getNoteById, updateNoteByDTO, saveNote } from "@/api/note";
import { updateIdleByDTO, saveProduct } from "@/api/idle";
import { getFileFromUrl, uploadFileToFileService, uploadBatchFilesToFileService } from "@/utils/util";
import ImageUpload from "./image-upload.vue";
import VideoUpload from "./video-upload.vue";
import ContentEditor from "./content-editor.vue";
import PreviewMobile from "./preview-mobile.vue";
import AddressEdit from "./address-edit.vue";
import { v4 as uuidv4 } from "uuid";

const { t } = useI18n();

// 状态定义
const fileList = ref<UploadUserFile[]>([]);
const videoUrl = ref("");
const coverUrl = ref("");
const note = ref<any>({});
const categoryList = ref<Array<any>>([]);
const shopCategoryList = ref<Array<any>>([]);
const album = ref("");
const locationInfo = ref("");
const dynamicTags = ref<Array<string>>([]);
const drawer = ref(false);
const isLogin = ref(false);
const userInfo = ref({});
const pushLoading = ref(false);
const publishingMessage = ref<MessageHandler | null>(null);
const userStore = useUserStore();
const route = useRoute();
const uploadHeader = ref({
  accessToken: userStore.getToken(),
});
const imageUploadRef = ref();
const videoUploadRef = ref();

type PublishType = "note" | "idle";
type TabKey = "note" | "idle";
type ContentType = "image" | "video";

interface Tab {
  key: TabKey;
  label: string;
}

const tabs = computed<Tab[]>(() => [
  { key: "note", label: t("note.publish") },
  { key: "idle", label: t("idle.publish") },
]);

// 计算 PreviewMobile 的 key，基于内容类型和文件列表/视频 URL
const previewKey = computed(() => {
  if (contentType.value === "image") {
    // 基于所有图片的 uid、url 和 name 生成唯一键，确保图片变化时 key 也变化
    // 使用 JSON.stringify 确保对象变化能被检测到
    const filesKey = fileList.value
      .map((f, index) => `${index}-${f.uid || f.name || ""}-${f.url || ""}-${f.name || ""}`)
      .join("|");
    return `image-${fileList.value.length}-${filesKey}`;
  } else {
    // 视频模式基于视频 URL 和封面 URL
    return `video-${videoUrl.value}-${coverUrl.value}`;
  }
});

// 定义闲置商品相关的类型（已注释，未使用）
// interface IdleNote {
//   productType: number;
//   province: string;
//   city: string;
//   district: string;
//   address: string;
//   cpid: number | string;
//   cid: number | string;
//   price: number; // 售价
//   originalPrice: number; // 原价
//   postType: number; // 发货方式：1-包邮，2-自费，3-自提
//   // ... 其他已有字段
// }

// 新增内容类型选择
const contentType = ref<ContentType>("image");
const currentTab = ref<TabKey>("note");

// 标签切换方法
const handleTabChange = (tab: TabKey) => {
  currentTab.value = tab;
  contentType.value = "image";
  resetData();
};

const handleContentTypeChange = (type: ContentType) => {
  if (contentType.value !== type) {
    contentType.value = type;
    resetData();
  }
};

const handleFileListUpdate = (newFiles: UploadUserFile[]) => {
  fileList.value = newFiles;
};

// 获取位置信息的方法
const getLocation = async () => {
  drawer.value = true;
};

const handleLocationSelect = (location: any) => {
  if (location) {
    // 确定显示的地址：优先使用地点名称，如果没有地点名称或地点名称是详细地址字符串，则使用详细地址
    // 判断 name 是否为详细地址字符串（包含区、街道等详细信息）
    const isDetailedAddress =
      location.name &&
      (location.name.includes(location.district || "") ||
        location.name.includes(location.address || "") ||
        /区.*路|街.*号|小区|大厦|广场|商场/.test(location.name));

    // 显示的地址名称：如果有真正的POI名称就使用，否则使用详细地址
    const displayName =
      !isDetailedAddress && location.name && location.name.trim()
        ? location.name.trim()
        : location.address && location.address.trim()
        ? location.address.trim()
        : location.name || "";

    // 更新显示的位置信息（只显示市+地点名称或详细地址，不显示完整省市区）
    locationInfo.value = location.city ? `${location.city} ${displayName}`.trim() : displayName;

    // 更新笔记对象中的位置信息
    // 优先使用地点名称（name），如果name是详细地址字符串，则使用address
    note.value = {
      ...note.value,
      longitude: location.location.lng,
      latitude: location.location.lat,
      province: location.province,
      city: location.city,
      district: location.district || "",
      address:
        !isDetailedAddress && location.name && location.name.trim() ? location.name.trim() : location.address || "",
    };
  }
  drawer.value = false;
};

const closeDrawer = () => {
  drawer.value = false;
};

// 处理图片发布（上传文件到file模块获取URL）
const handleImagePublish = async (uploadedUrls?: {
  uploadedFileUrls?: string[];
  uploadedCoverUrl?: string;
  uploadedVideoUrl?: string;
}) => {
  // 如果已经有上传后的URL（从AI配文获取），直接使用，避免重复上传
  if (uploadedUrls?.uploadedFileUrls && uploadedUrls.uploadedFileUrls.length > 0) {
    const imageUrls = uploadedUrls.uploadedFileUrls;
    const coverUrl = uploadedUrls.uploadedCoverUrl || imageUrls[0];

    // 计算封面高度（使用第一张图片）
    if (coverUrl) {
      const coverImage = new Image();
      coverImage.src = coverUrl;
      await new Promise<void>((resolve) => {
        coverImage.onload = () => {
          const size = coverImage.width / coverImage.height;
          note.value.noteCoverHeight = size >= 1.3 ? 200 : 300;
          resolve();
        };
        coverImage.onerror = () => resolve(); // 如果加载失败，继续执行
      });
    }

    // 设置笔记类型和数量
    note.value.noteType = 1;
    note.value.count = imageUrls.length;
    note.value.noteCover = coverUrl;
    note.value.urls = imageUrls;

    return { coverUrl, imageUrls };
  }

  // 如果没有已上传的URL，执行正常的上传流程
  // 获取第一张图作为封面
  const firstFile = fileList.value[0];
  if (!firstFile?.url) {
    throw new Error(t("note.coverUrlMissing"));
  }

  // 计算封面高度
  const coverImage = new Image();
  coverImage.src = firstFile.url;
  await new Promise<void>((resolve) => {
    coverImage.onload = () => {
      const size = coverImage.width / coverImage.height;
      note.value.noteCoverHeight = size >= 1.3 ? 200 : 300;
      resolve();
    };
  });

  // 上传封面图到file模块
  let coverUrl = "";
  if (firstFile.raw) {
    try {
      const coverResp = await uploadFileToFileService(firstFile.raw);
      if (coverResp.code === 200 && coverResp.data?.url) {
        coverUrl = coverResp.data.url;
      } else {
        throw new Error(coverResp.msg || t("note.coverUploadFail"));
      }
    } catch (error: any) {
      throw new Error(`${t("note.coverUploadFail")}: ${error.message || error}`);
    }
  } else if (firstFile.url) {
    // 如果没有raw文件，从URL获取
    const coverBlob = await fetch(firstFile.url).then((res) => res.blob());
    const coverFile = new File([coverBlob], "cover.jpg", { type: "image/jpeg" });
    const coverResp = await uploadFileToFileService(coverFile);
    if (coverResp.code === 200 && coverResp.data?.url) {
      coverUrl = coverResp.data.url;
    } else {
      throw new Error(coverResp.msg || t("note.coverUploadFail"));
    }
  }

  // 上传所有图片到file模块（使用批量上传优化性能）
  const imageUrls: string[] = [];
  const filesToUpload = fileList.value.filter((file) => file.raw).map((file) => file.raw!);

  if (filesToUpload.length > 0) {
    try {
      const resp = await uploadBatchFilesToFileService(filesToUpload);
      if (resp.code === 200 && resp.data) {
        // 处理返回的数据格式：可能是数组，每个元素可能是对象（有url属性）或直接是URL字符串
        const urls = resp.data
          .map((file: any) => {
            if (typeof file === "string") {
              return file;
            } else if (file && file.url) {
              return file.url;
            }
            return file;
          })
          .filter((url: string) => url && (url.startsWith("http://") || url.startsWith("https://")));

        imageUrls.push(...urls);
      } else {
        throw new Error(resp.msg || t("note.batchUploadFail"));
      }
    } catch (error: any) {
      // 如果批量上传失败，回退到逐个上传
      console.warn("批量上传失败，回退到逐个上传:", error);
      for (const file of filesToUpload) {
        try {
          const resp = await uploadFileToFileService(file);
          if (resp.code === 200 && resp.data?.url) {
            imageUrls.push(resp.data.url);
          } else {
            throw new Error(resp.msg || t("note.imageUploadFail"));
          }
        } catch (err: any) {
          throw new Error(`${t("note.imageUploadFail")}: ${err.message || err}`);
        }
      }
    }
  }

  // 设置笔记类型和数量
  note.value.noteType = 1;
  note.value.count = imageUrls.length;
  note.value.noteCover = coverUrl;
  note.value.urls = imageUrls;

  return { coverUrl, imageUrls };
};

// 处理视频发布（上传文件到file模块获取URL）
const handleVideoPublish = async (uploadedUrls?: {
  uploadedFileUrls?: string[];
  uploadedCoverUrl?: string;
  uploadedVideoUrl?: string;
}) => {
  // 如果已经有上传后的URL（从AI配文获取），直接使用，避免重复上传
  if (uploadedUrls?.uploadedVideoUrl) {
    const videoUrlResult = uploadedUrls.uploadedVideoUrl;
    const coverUrlResult = uploadedUrls.uploadedCoverUrl || "";

    // 设置笔记类型和数量
    note.value.noteType = 2;
    note.value.count = 1;
    note.value.noteCover = coverUrlResult;
    note.value.urls = [videoUrlResult];

    return { coverUrl: coverUrlResult, videoUrl: videoUrlResult };
  }

  // 如果没有已上传的URL，执行正常的上传流程
  if (!videoUrl.value) {
    throw new Error(t("note.uploadVideoFirstPlease"));
  }
  if (!coverUrl.value) {
    throw new Error(t("note.setNoteVideoCoverPlease"));
  }

  try {
    // 上传封面图到file模块
    let coverUrlResult = "";
    if (coverUrl.value.startsWith("data:")) {
      const coverFile = dataURLtoFile(coverUrl.value, "cover.jpg");
      const coverResp = await uploadFileToFileService(coverFile);
      if (coverResp.code === 200 && coverResp.data?.url) {
        coverUrlResult = coverResp.data.url;
      } else {
        throw new Error(coverResp.msg || t("note.coverUploadFail"));
      }
    } else {
      // 如果已经是URL，直接使用
      coverUrlResult = coverUrl.value;
    }

    // 上传视频到file模块
    let videoUrlResult = "";
    if (videoUrl.value.startsWith("blob:")) {
      // 如果是本地blob URL
      const response = await fetch(videoUrl.value);
      const blob = await response.blob();
      const videoFile = new File([blob], "video.mp4", { type: "video/mp4" });
      const videoResp = await uploadFileToFileService(videoFile);
      if (videoResp.code === 200 && videoResp.data?.url) {
        videoUrlResult = videoResp.data.url;
      } else {
        throw new Error(videoResp.msg || t("note.uploadVideoGenericFail"));
      }
    } else {
      // 如果已经是远程URL，直接使用
      videoUrlResult = videoUrl.value;
    }

    // 设置笔记类型和数量
    note.value.noteType = 2;
    note.value.count = 1;
    note.value.noteCover = coverUrlResult;
    note.value.urls = [videoUrlResult];

    return { coverUrl: coverUrlResult, videoUrl: videoUrlResult };
  } catch (error: any) {
    throw new Error(`${t("note.videoProcessFailedPrefix")}: ${error.message || error}`);
  }
};

// 抽取提交笔记的公共方法（使用文件URL）
const submitNote = async () => {
  // 地址字段已在 handleLocationSelect 中设置，这里确保使用 address
  note.value.cpid = categoryList.value[0];
  note.value.cid = categoryList.value[1];
  note.value.albumId = album.value;
  note.value.tags = dynamicTags.value;

  const requestId = uuidv4();

  try {
    if (note.value.id !== null && note.value.id !== undefined) {
      // 编辑场景，暂时还是使用旧接口（因为updateNoteByDTO接口逻辑更复杂）
      const params = new FormData();
      const noteData = JSON.stringify(note.value);
      params.append("noteData", noteData);
      await updateNoteByDTO(params);
      resetData();
      ElMessage.success(t("note.updateSuccess"));
    } else {
      // 新建场景，使用新的saveNote接口
      await saveNote(requestId, note.value);
      resetData();
      ElMessage.success(t("note.publishPendingReviewFull"));
    }
  } catch (error: any) {
    console.error("保存笔记错误:", error);
    const message = (error && error.message) || error?.response?.data?.msg || t("note.publishFailed");
    throw new Error(message);
  }
};

const dataURLtoFile = (dataurl: string, filename: string): File => {
  const arr = dataurl.split(",");
  const mime = arr[0].match(/:(.*?);/)![1];
  const bstr = atob(arr[1]);
  let n = bstr.length;
  const u8arr = new Uint8Array(n);
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  return new File([u8arr], filename, { type: mime });
};

// 抽取公共的验证逻辑
const validatePublish = () => {
  // 基础验证
  if (!note.value.title) {
    ElMessage.error(t("note.fillTitlePlease"));
    return false;
  }
  if (currentTab.value === "idle") {
    if (shopCategoryList.value.length <= 0) {
      ElMessage.error(t("note.chooseCategoryPlease"));
      return false;
    }
    if (!validateIdlePublish()) {
      return false;
    }
    return validateIdleFields();
  } else {
    if (categoryList.value.length <= 0) {
      ElMessage.error(t("note.chooseCategoryPlease"));
      return false;
    }
    if (contentType.value === "image") {
      return validateImagePublish();
    } else if (contentType.value === "video") {
      return validateVideoPublish();
    }
  }
  return false;
};

// 闲置发布验证
const validateIdlePublish = () => {
  if (contentType.value === "image") {
    if (fileList.value.length <= 0) {
      ElMessage.error(t("idle.uploadProductImagesPlease"));
      return false;
    }
  } else if (contentType.value === "video") {
    if (!videoUrl.value) {
      ElMessage.error(t("idle.uploadProductVideoPlease"));
      return false;
    }
    if (!coverUrl.value) {
      ElMessage.error(t("idle.setProductVideoCoverPlease"));
      return false;
    }
  }
  // 可以添加更多闲置商品相关的验证
  return true;
};

// 图片发布验证
const validateImagePublish = () => {
  if (fileList.value.length <= 0) {
    ElMessage.error(t("note.chooseImagesPlease"));
    return false;
  }
  return true;
};

// 视频发布验证
const validateVideoPublish = () => {
  if (!videoUrl.value) {
    ElMessage.error(t("note.uploadVideoFirstPlease"));
    return false;
  }
  if (!coverUrl.value) {
    ElMessage.error(t("note.setNoteVideoCoverPlease"));
    return false;
  }
  return true;
};

// 闲置商品提交方法（使用文件URL）
const submitIdle = async () => {
  // 验证必填字段
  if (!validateIdleFields()) {
    return;
  }
  try {
    // 处理闲置商品特有的数据
    const idleData: any = {
      ...note.value,
      productType: note.value.productType, // 闲置商品类型
      province: note.value.province,
      city: note.value.city,
      district: note.value.district,
      address: note.value.address || locationInfo.value,
      cpid: shopCategoryList.value[0],
      cid: shopCategoryList.value[1],
      price: Number(note.value.price || 0).toFixed(2),
      originalPrice: Number(note.value.originalPrice || 0).toFixed(2),
      postType: note.value.postType || 1,
      cover: note.value.cover,
      urls: note.value.urls,
      count: note.value.count,
    };

    const requestId = uuidv4();

    if (note.value.id) {
      // 编辑场景，暂时还是使用旧接口
      const params = new FormData();
      const noteData = JSON.stringify(idleData);
      params.append("productData", noteData);
      await updateIdleByDTO(params);
      ElMessage.success(t("idle.updateSuccessFull"));
    } else {
      // 新建场景，使用新的saveProduct接口
      await saveProduct(requestId, idleData);
      ElMessage.success(t("idle.publishPendingReviewFull"));
    }

    // 发布成功后重置表单
    resetData();
  } catch (error: any) {
    console.error("闲置商品发布失败:", error);
    const message = (error && error.message) || error?.response?.data?.msg || t("note.publishFailed");
    throw new Error(message);
  }
};

// 闲置商品字段验证
const validateIdleFields = () => {
  if (!note.value.price) {
    ElMessage.error(t("idle.enterSellingPrice"));
    return false;
  }

  if (note.value.price <= 0) {
    ElMessage.error(t("idle.priceMustPositive"));
    return false;
  }

  if (!note.value.originalPrice) {
    ElMessage.error(t("idle.enterOriginalPriceItem"));
    return false;
  }

  if (note.value.originalPrice < note.value.price) {
    ElMessage.error(t("idle.originalNotLessThanPrice"));
    return false;
  }

  if (!note.value.postType) {
    ElMessage.error(t("idle.chooseDeliveryMethod"));
    return false;
  }

  if (!locationInfo.value) {
    ElMessage.error(t("idle.chooseTreasureAddress"));
    return false;
  }

  return true;
};

// 优化后的发布方法
const publish = async (publishData?: any) => {
  if (pushLoading.value) return;
  if (!validatePublish()) return;
  try {
    pushLoading.value = true;
    publishingMessage.value?.close();
    publishingMessage.value = ElMessage({
      message: t("common.publishingWait"),
      type: "warning",
      showClose: true,
      duration: 0,
    });

    // 如果接收到了publishData，合并到note.value中（特别是relatedProducts）
    if (publishData?.note) {
      note.value = {
        ...note.value,
        ...publishData.note,
      };
      // 同步更新其他数据
      if (publishData.categories) {
        categoryList.value = publishData.categories;
      }
      if (publishData.tags) {
        dynamicTags.value = publishData.tags;
      }
      if (publishData.address) {
        locationInfo.value = publishData.address;
      }
    }

    // 根据不同类型执行不同的发布流程
    const publishType: PublishType = currentTab.value === "idle" ? "idle" : "note";
    // 传递已上传的URL，避免重复上传
    await handlePublish(publishType, {
      uploadedFileUrls: publishData?.uploadedFileUrls,
      uploadedCoverUrl: publishData?.uploadedCoverUrl,
      uploadedVideoUrl: publishData?.uploadedVideoUrl,
    });
  } catch (error) {
    const message = error instanceof Error && error.message ? error.message : t("note.publishFailed");
    ElMessage.error(message);
  } finally {
    pushLoading.value = false;
    if (publishingMessage.value) {
      publishingMessage.value.close();
      publishingMessage.value = null;
    }
  }
};

// 处理不同类型的发布
const handlePublish = async (
  type: PublishType,
  uploadedUrls?: {
    uploadedFileUrls?: string[];
    uploadedCoverUrl?: string;
    uploadedVideoUrl?: string;
  }
) => {
  switch (type) {
    case "idle":
      await handleIdlePublish(uploadedUrls);
      await submitIdle();
      break;
    case "note":
      await handleContentPublish(uploadedUrls);
      await submitNote();
      break;
    default:
      throw new Error(t("common.unknownType"));
  }
};

const handleIdlePublish = async (uploadedUrls?: {
  uploadedFileUrls?: string[];
  uploadedCoverUrl?: string;
  uploadedVideoUrl?: string;
}) => {
  if (contentType.value === "image") {
    // 图片处理逻辑 - 上传文件到file模块获取URL
    const firstFile = fileList.value[0];
    if (!firstFile?.url) {
      throw new Error(t("note.coverUrlMissing"));
    }

    // 计算封面高度
    const coverImage = new Image();
    coverImage.src = firstFile.url;
    await new Promise<void>((resolve) => {
      coverImage.onload = () => {
        const size = coverImage.width / coverImage.height;
        note.value.noteCoverHeight = size >= 1.3 ? 200 : 300;
        resolve();
      };
    });

    // 上传封面图到file模块
    let coverUrl = "";
    if (firstFile.raw) {
      const coverResp = await uploadFileToFileService(firstFile.raw);
      if (coverResp.code === 200 && coverResp.data?.url) {
        coverUrl = coverResp.data.url;
      } else {
        throw new Error(coverResp.msg || t("note.coverUploadFail"));
      }
    } else if (firstFile.url) {
      const coverBlob = await fetch(firstFile.url).then((res) => res.blob());
      const coverFile = new File([coverBlob], "cover.jpg", { type: "image/jpeg" });
      const coverResp = await uploadFileToFileService(coverFile);
      if (coverResp.code === 200 && coverResp.data?.url) {
        coverUrl = coverResp.data.url;
      } else {
        throw new Error(coverResp.msg || t("note.coverUploadFail"));
      }
    }

    // 上传所有图片到file模块（使用批量上传优化性能）
    const imageUrls: string[] = [];
    const filesToUpload = fileList.value.filter((file) => file.raw).map((file) => file.raw!);

    if (filesToUpload.length > 0) {
      try {
        const resp = await uploadBatchFilesToFileService(filesToUpload);
        if (resp.code === 200 && resp.data) {
          // 处理返回的数据格式：可能是数组，每个元素可能是对象（有url属性）或直接是URL字符串
          const urls = resp.data
            .map((file: any) => {
              if (typeof file === "string") {
                return file;
              } else if (file && file.url) {
                return file.url;
              }
              return file;
            })
            .filter((url: string) => url && (url.startsWith("http://") || url.startsWith("https://")));

          imageUrls.push(...urls);
        } else {
          throw new Error(resp.msg || t("note.batchUploadFail"));
        }
      } catch (error: any) {
        // 如果批量上传失败，回退到逐个上传
        console.warn("批量上传失败，回退到逐个上传:", error);
        for (const file of filesToUpload) {
          const resp = await uploadFileToFileService(file);
          if (resp.code === 200 && resp.data?.url) {
            imageUrls.push(resp.data.url);
          } else {
            throw new Error(resp.msg || t("note.imageUploadFail"));
          }
        }
      }
    }

    note.value.productType = 1;
    note.value.count = imageUrls.length;
    note.value.cover = coverUrl;
    note.value.urls = imageUrls;
  } else if (contentType.value === "video") {
    // 如果已经有上传后的URL（从AI配文获取），直接使用，避免重复上传
    if (uploadedUrls?.uploadedVideoUrl) {
      const videoUrlResult = uploadedUrls.uploadedVideoUrl;
      const coverUrlResult = uploadedUrls.uploadedCoverUrl || "";

      note.value.productType = 2;
      note.value.count = 1;
      note.value.cover = coverUrlResult;
      note.value.urls = [videoUrlResult];
      return;
    }

    // 如果没有已上传的URL，执行正常的上传流程
    // 视频处理逻辑 - 上传文件到file模块获取URL
    if (!videoUrl.value) {
      throw new Error(t("idle.uploadProductVideoPlease"));
    }
    if (!coverUrl.value) {
      throw new Error(t("idle.setProductVideoCoverPlease"));
    }

    // 上传封面图到file模块
    let coverUrlResult = "";
    if (coverUrl.value.startsWith("data:")) {
      const coverFile = dataURLtoFile(coverUrl.value, "cover.jpg");
      const coverResp = await uploadFileToFileService(coverFile);
      if (coverResp.code === 200 && coverResp.data?.url) {
        coverUrlResult = coverResp.data.url;
      } else {
        throw new Error(coverResp.msg || t("note.coverUploadFail"));
      }
    } else {
      coverUrlResult = coverUrl.value;
    }

    // 上传视频到file模块
    let videoUrlResult = "";
    if (videoUrl.value.startsWith("blob:")) {
      const response = await fetch(videoUrl.value);
      const blob = await response.blob();
      const videoFile = new File([blob], "video.mp4", { type: "video/mp4" });
      const videoResp = await uploadFileToFileService(videoFile);
      if (videoResp.code === 200 && videoResp.data?.url) {
        videoUrlResult = videoResp.data.url;
      } else {
        throw new Error(videoResp.msg || t("note.uploadVideoGenericFail"));
      }
    } else {
      videoUrlResult = videoUrl.value;
    }

    note.value.productType = 2;
    note.value.count = 1;
    note.value.cover = coverUrlResult;
    note.value.urls = [videoUrlResult];
  }
};

// 处理内容发布（图片或视频）
const handleContentPublish = async (uploadedUrls?: {
  uploadedFileUrls?: string[];
  uploadedCoverUrl?: string;
  uploadedVideoUrl?: string;
}) => {
  if (contentType.value === "image") {
    await handleImagePublish(uploadedUrls);
  } else if (contentType.value === "video") {
    await handleVideoPublish(uploadedUrls);
  } else {
    throw new Error(t("note.unsupportedContentType"));
  }
};

const resetData = () => {
  note.value = {};
  categoryList.value = [];
  shopCategoryList.value = [];
  album.value = "";
  locationInfo.value = "";
  fileList.value = [];
  coverUrl.value = "";
  videoUrl.value = "";
  imageUploadRef.value?.reset?.();
  videoUploadRef.value?.reset?.();
  pushLoading.value = false;
  dynamicTags.value = [];
  // currentTab.value = 'note'; // 是否需要重置tab视业务而定
  // contentType.value = 'image'; // 是否需要重置内容类型视业务而定
};

// 初始化数据
const initData = () => {
  userInfo.value = userStore.getUserInfo();
  isLogin.value = userStore.isLogin();
  if (isLogin.value) {
    const noteId = route.query.noteId as string;
    if (noteId) {
      getNoteByIdMethod(noteId);
    }
  }
};

const getNoteByIdMethod = (noteId: string) => {
  getNoteById(noteId).then((res) => {
    const { data } = res;
    note.value = data;
    fileList.value = [];
    if (data.urls) {
      const urls = JSON.parse(data.urls);
      urls.forEach((item: string) => {
        const fileName = item.substring(item.lastIndexOf("/") + 1);
        getFileFromUrl(item, fileName).then((res: any) => {
          fileList.value.push({ name: fileName, url: item, raw: res });
        });
      });
    }
    categoryList.value = [data.cpid, data.cid];
    dynamicTags.value = data.tags?.map((item: any) => item.title) || [];
  });
};

const showContentEditor = computed(() => {
  return fileList.value.length > 0 || !!videoUrl.value;
});

onMounted(() => {
  const tab = route.query.tab as TabKey;
  if (tab === "note" || tab === "idle") {
    currentTab.value = tab;
  }
  initData();
});
</script>

<style lang="less" scoped>
// 主题变量
@primary-color: #3d8af5;
@text-primary: rgba(51, 51, 51, 0.8);
@text-secondary: rgba(51, 51, 51, 0.6);
@border-color: rgba(0, 0, 0, 0.08);
@white: #fff;

.content-type-container {
  display: flex;
  justify-content: center;
  padding: 0 20px 15px;
  margin-bottom: -38px;
}

.content-type-track {
  position: relative;
  display: inline-flex;
  align-items: stretch;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 999px;
  padding: 0;
  overflow: hidden;
}

.content-type-slider {
  position: absolute;
  top: 0;
  left: 0;
  width: 50%;
  height: 100%;
  background: @primary-color;
  border-radius: 999px;
  box-shadow: 0 2px 6px rgba(61, 138, 245, 0.32);
  transition: transform 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.content-type-tab {
  position: relative;
  z-index: 1;
  flex: 1 1 0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  padding: 12px 15px;
  cursor: pointer;
  color: #86909c;
  font-size: 13px;
  font-weight: 600;
  line-height: 1;
  border-radius: 999px;
  transition: color 0.3s ease;
  user-select: none;
  text-align: center;
  white-space: nowrap;

  &:hover:not(.active) {
    color: #4e5969;
  }

  &.active {
    color: #fff;
  }
}

.tab-container {
  display: flex;
  border-bottom: 2px solid #eee;
  padding: 0 20px;
  margin-top: 30px;
  margin-bottom: 10px;
}

.tab-item {
  padding: 15px 15px;
  cursor: pointer;
  color: gray;
  font-size: 16px;
  position: relative;

  &.active {
    color: @primary-color;
    font-weight: 500;

    &::after {
      content: "";
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 2px;
      background-color: @primary-color;
    }
  }
}

.container {
  flex: 1;
  padding-top: 60px;
  position: relative;

  .push-container {
    margin-left: 8vw;
    width: 780px;
    border-radius: 8px;
    box-sizing: border-box;
    box-shadow: var(--el-box-shadow-lighter);
    transition: margin-left 0.3s ease; // 添加过渡动画

    // 当显示预览容器时，增加左边距
    &.with-preview {
      margin-left: 6.5vw;
    }
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

<style lang="less">
/* 发布页图文/视频胶囊：暗色模式 */
html.dark-mode .push-page .content-type-container {
  .content-type-track {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.14);
  }

  .content-type-slider {
    background: #3d8af5;
    box-shadow: 0 2px 12px rgba(61, 138, 245, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.12);
  }

  .content-type-tab {
    color: rgba(255, 255, 255, 0.5);

    &:hover:not(.active) {
      color: rgba(255, 255, 255, 0.88);
    }

    &.active {
      color: #fff !important;
      -webkit-text-fill-color: #fff;
    }
  }
}
</style>