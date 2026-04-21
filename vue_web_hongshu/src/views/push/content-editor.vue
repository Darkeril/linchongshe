<template>
  <div class="content-editor">
    <div class="cover-title-header">
      <span class="cover-title">{{ $t("note.contentBody") }}</span>
      <el-button
        type="primary"
        size=""
        :icon="MagicStick"
        :loading="aiGenerating"
        :disabled="props.publishing || !canGenerateContent"
        @click="handleAIGenerate"
        class="ai-generate-btn"
      >
        {{ $t("note.aiGenerate") }}
      </el-button>
    </div>
    <!-- 标题和正文输入 -->
    <div class="push-content">
      <el-input
        v-model="noteData.title"
        maxlength="20"
        show-word-limit
        type="text"
        :placeholder="$t('note.titlePlaceholder')"
        class="input-title"
        :disabled="props.publishing"
      />
      <el-input
        v-model="noteData.content"
        maxlength="250"
        show-word-limit
        :autosize="{ minRows: 4, maxRows: 5 }"
        type="textarea"
        :placeholder="$t('note.contentDescriptionPlaceholder')"
        class="input-content"
        :disabled="props.publishing"
      />

      <!-- 非闲置模式下的标签和按钮 -->
      <template v-if="props.currentTab !== 'idle'">
        <div class="btns">
          <button class="css-fm44j css-osq2ks dyn" @click="addTag('tag')" :disabled="props.publishing">
            <span class="btn-content">{{ $t("note.topic") }}</span>
          </button>
          <button class="css-fm44j css-osq2ks dyn" @click="addTag('user')" :disabled="props.publishing">
            <span class="btn-content">{{ $t("note.atUser") }}</span>
          </button>
          <!-- 表情选择器 -->
          <el-popover
            v-model:visible="emojiPickerVisible"
            placement="bottom"
            trigger="click"
            width="300px"
            :disabled="props.publishing"
          >
            <template #reference>
              <button class="css-fm44j css-osq2ks dyn" :disabled="props.publishing">
                <span class="btn-content">
                  <div class="smile"></div>
                  {{ $t("note.emoji") }}
                </span>
              </button>
            </template>
            <div class="emoji-picker">
              <span v-for="emoji in emojis" :key="emoji" @click="insertEmoji(emoji)" class="emoji-item">
                {{ emoji }}
              </span>
            </div>
          </el-popover>
        </div>

        <div class="related-products">
          <label>{{ $t("note.relatedProductsColon") }}</label>
          <!-- 选择商品按钮 -->
          <el-button
            type="primary"
            plain
            size="small"
            @click="showProductSelector"
            :disabled="(noteData.relatedProducts?.length ?? 0) >= 3"
          >
            <el-icon><Plus /></el-icon>
            {{ $t("note.selectProductBtn") }}
          </el-button>

          <!-- 已选商品展示 -->
          <div class="products-container" v-if="noteData.relatedProducts?.length">
            <div class="selected-products">
              <div v-for="product in noteData.relatedProducts" :key="product.id" class="product-card">
                <img :src="product.cover" :alt="product.title" class="product-img" />
                <div class="product-info">
                  <div class="info-row">
                    <div class="product-title">{{ product.title }}</div>
                    <div class="price-group">
                      <span class="current-price">￥{{ convert.to_price(product.price) }}</span>
                      <span
                        class="original-price"
                        v-if="product.originalPrice && product.originalPrice > product.price"
                      >
                        ￥{{ convert.to_price(product.originalPrice) }}
                      </span>
                    </div>
                  </div>
                </div>
                <div class="remove-btn" @click.stop="removeRelatedProduct(product)">
                  <el-icon><Close /></el-icon>
                </div>
              </div>
            </div>

            <!-- 添加商品按钮 -->
            <div class="add-product-btn" v-if="noteData.relatedProducts?.length < 3" @click="showProductSelector">
              <el-icon><Plus /></el-icon>
              <span>{{ $t("note.selectProductBtn") }}</span>
            </div>
          </div>

          <!-- 商品选择对话框 -->
          <el-dialog
            v-model="productDialogVisible"
            :title="$t('note.selectRelatedProductDialog')"
            width="60%"
            :close-on-click-modal="false"
          >
            <div class="product-selector">
              <!-- 搜索栏 -->
              <div class="search-bar">
                <el-input
                  v-model="productSearchKeyword"
                  :placeholder="$t('note.searchProductByTitle')"
                  clearable
                  @input="handleProductSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>

              <!-- 商品列表 -->
              <div class="product-list">
                <div
                  v-for="product in userProducts.slice(0, 10)"
                  :key="product.id"
                  class="product-item"
                  :class="{ active: isProductSelected(product) }"
                  @click="toggleProductSelection(product)"
                >
                  <el-image :src="product.cover" fit="cover" :preview-teleported="false">
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div class="product-info">
                    <div class="product-title">{{ product.title }}</div>
                    <div class="price-group">
                      <span class="current-price">￥{{ convert.to_price(product.price) }}</span>
                      <span
                        class="original-price"
                        v-if="product.originalPrice && product.originalPrice > product.price"
                      >
                        ￥{{ convert.to_price(product.originalPrice) }}
                      </span>
                    </div>
                  </div>
                  <div class="product-mask" v-if="isProductSelected(product)">
                    <el-icon><Check /></el-icon>
                  </div>
                </div>
              </div>
              <!-- 分页 -->
              <div class="pagination">
                <el-pagination
                  v-model:current-page="productPage"
                  v-model:page-size="productPageSize"
                  :total="totalProducts"
                  @current-change="handleProductPageChange"
                  layout="prev, pager, next"
                />
              </div>
            </div>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="cancelProductSelection">{{ $t("common.cancel") }}</el-button>
                <el-button type="primary" @click="confirmProductSelection">{{ $t("common.confirm") }}</el-button>
              </span>
            </template>
          </el-dialog>
        </div>

        <!-- 标签列表 -->
        <div class="tag-list">
          <el-tag
            v-for="tag in dynamicTags"
            :key="tag"
            closable
            :disable-transitions="false"
            @close="handleClose(tag)"
            class="tag-item"
            type="primary"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="inputVisible"
            ref="InputRef"
            v-model="inputValue"
            style="width: 3.125rem"
            size="small"
            @keyup.enter="handleInputConfirm"
            @blur="handleInputBlur"
          />
          <el-button
            v-else
            type="primary"
            size="small"
            @click="showInput"
            plain
            id="tagContainer"
            :disabled="dynamicTags.length >= 5"
          >
            <el-icon><Plus /></el-icon>
            {{ $t("note.tag") }}
          </el-button>
        </div>

        <!-- 热门标签 -->
        <div class="hot-tag">
          <span class="tag-title-text">{{ $t("note.recommendTags") }}</span>
          <el-tag
            v-for="tag in hotTags"
            :key="tag"
            class="hot-tag-item"
            type="primary"
            @click="selectHotTag(tag)"
            :disabled="dynamicTags.length >= 5"
          >
            {{ tag }}
          </el-tag>
        </div>

        <!-- 分类选择 -->
        <div class="categorys">
          <label for="category">{{ $t("note.selectCategoryWithColon") }}</label>
          <el-cascader
            ref="CategoryCascaderRef"
            :model-value="props.categoryList"
            :options="categoryOptions"
            @change="handleCategoryChange"
            :props="cascaderProps"
            :placeholder="$t('note.selectCategoryPh')"
            clearable
            :popper-options="{
              placement: 'right-start',
              modifiers: [
                {
                  name: 'flip',
                  enabled: false,
                },
                {
                  name: 'preventOverflow',
                  enabled: true,
                },
              ],
            }"
            @visible-change="setupCascaderNodeClick"
          />
        </div>

        <!-- 专辑选择 -->
        <div class="albums">
          <label>{{ $t("note.selectAlbumWithColon") }}</label>
          <el-select
            v-model="noteData.albumId"
            :placeholder="$t('note.selectAlbumPh')"
            style="width: 190px"
            clearable
            filterable
            allow-create
            default-first-option
            :popper-options="{
              placement: 'right-start',
              modifiers: [
                {
                  name: 'flip',
                  enabled: false,
                },
                {
                  name: 'preventOverflow',
                  enabled: true,
                },
              ],
            }"
            @change="handleAlbumChange"
          >
            <el-option v-for="item in albumOptions" :key="item.id" :label="item.title" :value="item.id">
              <div class="album-option">
                <span class="album-title">{{ item.title }}</span>
                <span class="album-count" v-if="item.noteCount"> - {{ $t("note.albumNoteCount", { n: item.noteCount }) }}</span>
              </div>
            </el-option>
          </el-select>
        </div>
      </template>
    </div>

    <!-- 闲置商品信息 -->
    <template v-if="props.currentTab === 'idle'">
      <!-- 分类选择 -->
      <div class="categorys">
        <label for="category">{{ $t("note.selectCategoryWithColon") }}</label>
        <el-cascader
          ref="ShopCategoryCascaderRef"
          :model-value="props.shopCategoryList"
          :options="shopCategoryOptions"
          @change="handleCategoryChange"
          :props="cascaderProps"
          :placeholder="$t('note.selectCategoryPh')"
          clearable
          :popper-options="{
            placement: 'right-start',
            modifiers: [
              {
                name: 'flip',
                enabled: false,
              },
              {
                name: 'preventOverflow',
                enabled: true,
              },
            ],
          }"
          @visible-change="setupCascaderNodeClick"
        />
      </div>

      <!-- 发货方式  -->
      <div class="product">
        <el-form-item :label="$t('note.deliveryMethod')" prop="postType">
          <el-radio-group v-model="noteData.postType" required>
            <el-radio label="0">{{ $t("common.mail") }}</el-radio>
            <el-radio label="1">{{ $t("common.selfPickup") }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('note.price')">
          <el-input-number
            required
            controls-position="right"
            v-model="noteData.price"
            :precision="2"
            :step="1"
            :min="0"
            :placeholder="$t('note.priceInputPh')"
          ></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('note.originalPrice')">
          <el-input-number
            required
            controls-position="right"
            v-model="noteData.originalPrice"
            :precision="2"
            :step="1"
            :min="0"
            :placeholder="$t('note.originalPriceInputPh')"
          >
          </el-input-number>
        </el-form-item>
      </div>
    </template>

    <!-- 位置信息 -->
    <div class="location">
      <span>{{ props.currentTab === "idle" ? $t("note.treasureLocation") : $t("note.addLocation") }}</span>
      <div class="location-container" @click="handleGetLocation">
        <el-icon><Location /></el-icon>
        <span class="location-text">{{ props.locationInfo || $t("note.clickGetLocation") }}</span>
      </div>
    </div>
  </div>

  <!-- 发布按钮 - 移到内容框外部 -->
  <div class="submit">
    <button class="clearBtn" @click="handleReset" :disabled="props.publishing">
      <span class="btn-content">{{ $t("common.cancel") }}</span>
    </button>
    <el-button type="primary" loading :disabled="true" v-if="props.publishing">{{ $t("nav.publish") }}</el-button>
    <button class="publishBtn" @click="handlePublish" :disabled="props.publishing" v-else>
      <span class="btn-content">{{ $t("nav.publish") }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch, computed } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage, ElMessageBox } from "element-plus";

const { t } = useI18n();
import { Location } from "@element-plus/icons-vue";
import { getAlbumData, createAlbum } from "@/api/album";
import { getCategoryTreeData } from "@/api/category";
import { getShopCategoryTreeData } from "@/api/idleCategory";
import { getTagByKeyword } from "@/api/tag";
import { useUserStore } from "@/store/userStore";
import type { CascaderProps, ElInput } from "element-plus";
import { debounce } from "lodash-es";
import { Plus, Close, Search, Picture, Check, MagicStick } from "@element-plus/icons-vue";
import { getUserProducts } from "@/api/user";
import convert from "@/utils/convert";
import { generateContentFromUrls } from "@/api/tongyi";
import { uploadFileToFileService, uploadBatchFilesToFileService } from "@/utils/util";
import type { UploadUserFile } from "element-plus";

interface RelatedProduct {
  id: string;
  cover: string;
  title: string;
  price: number;
  [key: string]: any; // 如果有其他字段
}

interface AlbumOption {
  id: string | number;
  title: string;
  noteCount?: number;
}

const props = defineProps({
  note: {
    type: Object,
    required: true,
  },
  categoryList: {
    type: Array,
    default: () => [],
  },
  shopCategoryList: {
    type: Array,
    default: () => [],
  },
  album: {
    type: String,
    default: "",
  },
  locationInfo: {
    type: String,
    default: "",
  },
  dynamicTags: {
    type: Array,
    default: () => [],
  },
  currentTab: {
    type: String,
    default: "image",
  },
  contentType: {
    type: String,
    default: "image",
  },
  publishing: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  fileList: {
    type: Array,
    default: () => [],
  },
  videoUrl: {
    type: String,
    default: "",
  },
  coverUrl: {
    type: String,
    default: "",
  },
});

const emit = defineEmits([
  "update:note",
  "update:categoryList",
  "update:shopCategoryList",
  "update:album",
  "update:locationInfo",
  "update:dynamicTags",
  "publish",
  "reset",
  "get-location",
]);

const loadingState = ref({
  categories: false,
  shopCategories: false,
  albums: false,
  tags: false,
});

// 数据相关
const noteData = computed({
  get: () => props.note as { relatedProducts?: RelatedProduct[] } & Record<string, any>,
  set: (newVal) => {
    if (JSON.stringify(newVal) !== JSON.stringify(props.note)) {
      emit("update:note", { ...newVal });
    }
  },
});

const pageSize = 10;
const inputValue = ref("");
const inputVisible = ref(false);
const InputRef = ref<InstanceType<typeof ElInput>>();
const hotTags = ref<string[]>([]);
const userInfo = ref<any>({});
const userStore = useUserStore();
const isLogin = ref(false);
// const route = useRoute();
const categoryOptions = ref([]);
const shopCategoryOptions = ref([]);
const albumOptions = ref<AlbumOption[]>([]);
const CategoryCascaderRef = ref<any>(null);
const ShopCategoryCascaderRef = ref<any>(null);

const productDialogVisible = ref(false);
const productSearchKeyword = ref("");
const productPage = ref(1);
const productPageSize = ref(10);
const totalProducts = ref(0);
const userProducts = ref<RelatedProduct[]>([]);
const tempSelectedProducts = ref<RelatedProduct[]>([]);

const emojiPickerVisible = ref(false);
const aiGenerating = ref(false);
// 保存已上传的文件URL（用于避免重复上传）
const uploadedFileUrls = ref<string[]>([]);
const uploadedCoverUrl = ref<string>("");
const uploadedVideoUrl = ref<string>("");

// 判断是否可以生成内容（需要有图片或视频）
const canGenerateContent = computed(() => {
  if (props.contentType === "video") {
    return !!props.videoUrl;
  } else {
    return props.fileList && props.fileList.length > 0;
  }
});

// AI生成内容
const handleAIGenerate = async () => {
  if (!canGenerateContent.value) {
    ElMessage.warning(t("note.uploadMediaFirst"));
    return;
  }

  aiGenerating.value = true;

  // 显示AI生成中的轻量提示（不遮盖整个页面）
  const loadingMessage = ElMessage({
    message: t("note.aiGenerating"),
    type: "warning",
    duration: 0, // 不自动关闭
    showClose: false,
    customClass: "ai-generating-message",
  });

  try {
    let response;

    if (props.contentType === "video") {
      // 视频模式：优先使用已有真实URL，避免重复上传
      if (!props.videoUrl) {
        ElMessage.warning(t("note.videoUrlMissingShort"));
        return;
      }

      let videoUrlForAI = props.videoUrl;
      
      // 如果已经是真实URL（http/https开头），直接使用，不需要上传
      if (props.videoUrl.startsWith("http://") || props.videoUrl.startsWith("https://")) {
        videoUrlForAI = props.videoUrl;
        uploadedVideoUrl.value = videoUrlForAI;
      } 
      // 如果视频URL是blob URL，需要上传获取真实URL
      else if (props.videoUrl.startsWith("blob:")) {
        console.log("📤 视频是blob URL，需要上传:", props.videoUrl.substring(0, 50) + "...");
        try {
          const response = await fetch(props.videoUrl);
          const blob = await response.blob();
          const videoFile = new File([blob], "video.mp4", { type: "video/mp4" });
          const resp = await uploadFileToFileService(videoFile);
          if (resp.code === 200 && resp.data?.url) {
            videoUrlForAI = resp.data.url;
            // 保存上传后的URL，避免发布时重复上传
            uploadedVideoUrl.value = videoUrlForAI;
            console.log("✅ 视频上传成功:", videoUrlForAI);
          } else {
            throw new Error(resp.msg || t("note.uploadVideoGenericFail"));
          }
        } catch (error: any) {
          throw new Error(`${t("note.uploadVideoGenericFail")}: ${error.message || error}`);
        }
      } else {
        // 其他情况（如data URL），也尝试上传
        try {
          let videoFile: File;
          if (props.videoUrl.startsWith("data:")) {
            // data URL转File
            const arr = props.videoUrl.split(",");
            const mime = arr[0].match(/:(.*?);/)![1];
            const bstr = atob(arr[1]);
            let n = bstr.length;
            const u8arr = new Uint8Array(n);
            while (n--) {
              u8arr[n] = bstr.charCodeAt(n);
            }
            videoFile = new File([u8arr], "video.mp4", { type: mime });
          } else {
            // 其他情况，尝试fetch
            const blob = await fetch(props.videoUrl).then((res) => res.blob());
            videoFile = new File([blob], "video.mp4", { type: "video/mp4" });
          }
          const resp = await uploadFileToFileService(videoFile);
          if (resp.code === 200 && resp.data?.url) {
            videoUrlForAI = resp.data.url;
            uploadedVideoUrl.value = videoUrlForAI;
          } else {
            throw new Error(resp.msg || t("note.uploadVideoGenericFail"));
          }
        } catch (error: any) {
          throw new Error(`${t("note.uploadVideoGenericFail")}: ${error.message || error}`);
        }
      }

      // 如果有封面，优先使用已有真实URL，避免重复上传
      if (props.coverUrl) {
        // 如果已经是真实URL（http/https开头），直接使用
        if (props.coverUrl.startsWith("http://") || props.coverUrl.startsWith("https://")) {
          uploadedCoverUrl.value = props.coverUrl;
        } 
        // 如果是blob或data URL，需要上传
        else if (props.coverUrl.startsWith("data:") || props.coverUrl.startsWith("blob:")) {
          try {
            let coverFile: File;
            if (props.coverUrl.startsWith("data:")) {
              // data URL转File
              const arr = props.coverUrl.split(",");
              const mime = arr[0].match(/:(.*?);/)![1];
              const bstr = atob(arr[1]);
              let n = bstr.length;
              const u8arr = new Uint8Array(n);
              while (n--) {
                u8arr[n] = bstr.charCodeAt(n);
              }
              coverFile = new File([u8arr], "cover.jpg", { type: mime });
            } else {
              // blob URL转File
              const blob = await fetch(props.coverUrl).then((res) => res.blob());
              coverFile = new File([blob], "cover.jpg", { type: "image/jpeg" });
            }
            const resp = await uploadFileToFileService(coverFile);
            if (resp.code === 200 && resp.data?.url) {
              uploadedCoverUrl.value = resp.data.url;
            }
          } catch (error: any) {
            console.warn("封面上传失败，将在发布时上传:", error);
          }
        }
      }

      // 使用上传后的URL调用AI生成
      response = await generateContentFromUrls({
        videoUrl: videoUrlForAI,
        style: "小红书笔记",
        contentType: props.currentTab === "idle" ? "idle" : "note",
      });
    } else {
      // 图片模式：参考发布时的文件上传逻辑
      const imageUrls: string[] = [];

      // 优先检查是否已有上传过的URL（避免重复上传）
      if (uploadedFileUrls.value.length > 0 && uploadedFileUrls.value.length === props.fileList.length) {
        // 如果之前已经上传过，且数量匹配，直接使用已上传的URL
        imageUrls.push(...uploadedFileUrls.value);
        console.log("✅ 使用之前已上传的URL，跳过上传:", uploadedFileUrls.value);
      } else {
        // 收集需要上传的文件
        const filesToUpload: File[] = [];
        const filesFromUrl: { file: UploadUserFile; url: string }[] = [];

        for (let index = 0; index < props.fileList.length; index++) {
          const file = props.fileList[index] as UploadUserFile;
          let hasRealUrl = false;
          
          // 优先检查是否已有真实URL（已上传过的图片）
          // 1. 检查 uploadedFileUrls（之前AI配文时上传的URL）
          if (uploadedFileUrls.value[index]) {
            const url = uploadedFileUrls.value[index];
            if (url && (url.startsWith("http://") || url.startsWith("https://"))) {
              imageUrls.push(url);
              hasRealUrl = true;
              console.log(`✅ 图片[${index}]已有真实URL (uploadedFileUrls):`, url);
            }
          }
          
          // 2. 如果第1步没有找到，检查 response.data.url（上传组件返回的URL）
          if (!hasRealUrl && (file as any).response?.data?.url) {
            const url = (file as any).response.data.url;
            // 确保是真实URL（http/https开头）
            if (url && (url.startsWith("http://") || url.startsWith("https://"))) {
              imageUrls.push(url);
              hasRealUrl = true;
              console.log(`✅ 图片[${index}]已有真实URL (response.data.url):`, url);
            }
          }
          
          // 3. 如果第2步没有找到，检查 file.url 是否是真实URL（非blob/data URL）
          if (!hasRealUrl && file.url && (file.url.startsWith("http://") || file.url.startsWith("https://"))) {
            imageUrls.push(file.url);
            hasRealUrl = true;
            console.log(`✅ 图片[${index}]已有真实URL (file.url):`, file.url);
          }
          
          // 4. 如果没有真实URL，才需要上传
          if (!hasRealUrl) {
            // 优先使用 raw 文件
            if ((file as any).raw) {
              filesToUpload.push((file as any).raw);
              console.log(`📤 图片[${index}]需要上传 (raw文件):`, (file as any).raw?.name || "unknown", "file.url:", file.url);
            }
            // 如果没有 raw 文件，从 blob/data URL 获取
            else if (file.url && (file.url.startsWith("blob:") || file.url.startsWith("data:"))) {
              filesFromUrl.push({ file, url: file.url });
              console.log(`📤 图片[${index}]需要上传 (blob/data URL):`, file.url.substring(0, 50) + "...");
            } else {
              console.warn(`⚠️ 图片[${index}]文件没有可用的URL或raw文件:`, {
                name: file.name,
                url: file.url,
                hasResponse: !!(file as any).response,
                hasRaw: !!(file as any).raw
              });
            }
          }
        }
        
        console.log("📊 图片处理统计 - 已有URL:", imageUrls.length, "需要上传raw文件:", filesToUpload.length, "需要上传blob/data URL:", filesFromUrl.length);

        // 批量上传 raw 文件
        if (filesToUpload.length > 0) {
          try {
            const resp = await uploadBatchFilesToFileService(filesToUpload);
            if (resp.code === 200 && resp.data) {
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

        // 处理从 URL 获取的文件（逐个上传，因为需要先 fetch）
        for (const { url } of filesFromUrl) {
          try {
            console.log("📤 正在上传blob/data URL图片:", url.substring(0, 50) + "...");
            const blob = await fetch(url).then((res) => res.blob());
            const imageFile = new File([blob], "image.jpg", { type: blob.type || "image/jpeg" });
            const resp = await uploadFileToFileService(imageFile);
            if (resp.code === 200 && resp.data?.url) {
              imageUrls.push(resp.data.url);
              console.log("✅ blob/data URL图片上传成功:", resp.data.url);
            } else {
              throw new Error(resp.msg || t("note.imageUploadFail"));
            }
          } catch (error: any) {
            throw new Error(`${t("note.imageUploadFail")}: ${error.message || error}`);
          }
        }
      }

      if (imageUrls.length === 0) {
        ElMessage.warning(t("note.noImageForAi"));
        return;
      }

      // 保存上传后的URL，避免发布时重复上传
      uploadedFileUrls.value = imageUrls;

      // 如果有封面，优先使用已有真实URL，避免重复上传
      if (props.coverUrl) {
        // 如果已经是真实URL（http/https开头），直接使用
        if (props.coverUrl.startsWith("http://") || props.coverUrl.startsWith("https://")) {
          uploadedCoverUrl.value = props.coverUrl;
        } 
        // 如果是blob或data URL，需要上传
        else if (props.coverUrl.startsWith("data:") || props.coverUrl.startsWith("blob:")) {
          try {
            let coverFile: File;
            if (props.coverUrl.startsWith("data:")) {
              // data URL转File
              const arr = props.coverUrl.split(",");
              const mime = arr[0].match(/:(.*?);/)![1];
              const bstr = atob(arr[1]);
              let n = bstr.length;
              const u8arr = new Uint8Array(n);
              while (n--) {
                u8arr[n] = bstr.charCodeAt(n);
              }
              coverFile = new File([u8arr], "cover.jpg", { type: mime });
            } else {
              // blob URL转File
              const blob = await fetch(props.coverUrl).then((res) => res.blob());
              coverFile = new File([blob], "cover.jpg", { type: "image/jpeg" });
            }
            const resp = await uploadFileToFileService(coverFile);
            if (resp.code === 200 && resp.data?.url) {
              uploadedCoverUrl.value = resp.data.url;
            }
          } catch (error: any) {
            console.warn("封面上传失败，将在发布时上传:", error);
          }
        }
      } else if (imageUrls.length > 0) {
        // 没有封面但有图片，使用第一张作为封面
        uploadedCoverUrl.value = imageUrls[0];
      }

      // 使用上传后的URL调用AI生成
      response = await generateContentFromUrls({
        imageUrls,
        style: "小红书笔记",
        contentType: props.currentTab === "idle" ? "idle" : "note",
      });
    }

    if (response && (response as any).code === 200 && (response as any).data) {
      const data = (response as any).data;

      // 填充标题（取第一个标题，如果标题列表存在）
      if (data.titles && data.titles.length > 0) {
        // 如果标题超过20字符，截取
        const title = data.titles[0];
        noteData.value.title = title.length > 20 ? title.substring(0, 20) : title;
      }

      // 填充正文内容（如果超过250字符，截取）
      if (data.content) {
        const content = data.content;
        noteData.value.content = content.length > 250 ? content.substring(0, 250) : content;
      }

      // 关闭loading提示
      loadingMessage.close();
      ElMessage.success(t("note.aiCaptionGenerated"));
    } else {
      // 关闭loading提示
      loadingMessage.close();
      ElMessage.error((response as any)?.msg || t("note.generateFailedRetry"));
    }
  } catch (error: any) {
    console.error("AI生成失败:", error);
    // 关闭loading提示
    loadingMessage.close();
    ElMessage.error(error?.response?.data?.msg || error?.message || t("note.generateFailedRetry"));
  } finally {
    aiGenerating.value = false;
  }
};

const emojis = [
  "😀",
  "😃",
  "😄",
  "😁",
  "😆",
  "😅",
  "😂",
  "🤣",
  "😊",
  "😇",
  "🙂",
  "🙃",
  "😉",
  "😌",
  "😍",
  "🥰",
  "😘",
  "😗",
  "😙",
  "😚",
  "😋",
  "😛",
  "😝",
  "😜",
  "🤪",
  "🤨",
  "🧐",
  "🤓",
  "😎",
  "🤩",
  "🥳",
  "😏",
  "😒",
  "😞",
  "😔",
  "😟",
  "😕",
  "🙁",
  "☹️",
  "😣",
  "😖",
  "😫",
  "😩",
  "🥺",
  "😢",
  "😭",
  "😤",
  "😠",
  "😡",
  "🤬",
  "🤯",
  "😳",
  "🥵",
  "🥶",
  "😱",
  "😨",
  "😰",
  "😥",
  "😓",
  "🤗",
  "🤔",
  "🤭",
  "🤫",
  "🤥",
  "😶",
  "😐",
  "😑",
  "😬",
  "🙄",
  "😯",
  "😦",
  "😧",
  "😮",
  "😲",
  "🥱",
  "😴",
  "🤤",
  "😪",
  "😵",
  "🤐",
  "🥴",
  "🤢",
  "🤮",
  "🤧",
  "😷",
  "🤒",
  "🤕",
  "🤑",
  "🤠",
  "😈",
  "👿",
  "👹",
  "👺",
  "🤡",
  "💩",
  "👻",
  "💀",
  "☠️",
  "👽",
  "👾",
  "🤖",
  "🎃",
  "😺",
  "😸",
  "😹",
  "😻",
  "😼",
  "😽",
  "🙀",
  "😿",
  "😾",
];

const insertEmoji = (emoji: string) => {
  const content = noteData.value.content || "";
  noteData.value.content = `${content}${emoji}`;
  emojiPickerVisible.value = false;

  // 聚焦到输入框
  nextTick(() => {
    const textarea = document.querySelector(".input-content textarea") as HTMLTextAreaElement;
    if (textarea) {
      textarea.focus();
      textarea.selectionStart = textarea.selectionEnd = textarea.value.length;
    }
  });
};

// 显示商品选择器
const showProductSelector = () => {
  productDialogVisible.value = true;
  tempSelectedProducts.value = [...(noteData.value.relatedProducts || [])];
  loadUserProducts();
};

// 加载用户商品
const loadUserProducts = async () => {
  try {
    const res = await getUserProducts({
      page: productPage.value,
      pageSize: productPageSize.value,
      keyword: productSearchKeyword.value,
      userId: userInfo.value.id,
    });
    userProducts.value = res.data.records;
    totalProducts.value = res.data.total;
  } catch (error) {
    ElMessage.error(t("note.getProductListFail"));
  }
};

// 搜索商品
const handleProductSearch = debounce(() => {
  productPage.value = 1;
  loadUserProducts();
}, 300);

// 页码变化
const handleProductPageChange = (page: number) => {
  productPage.value = page;
  loadUserProducts();
};

// 检查商品是否被选中
const isProductSelected = (product: any) => {
  return tempSelectedProducts.value.some((p) => p.id === product.id);
};

// 切换商品选择状态
const toggleProductSelection = (product: any) => {
  const index = tempSelectedProducts.value.findIndex((p) => p.id === product.id);
  if (index > -1) {
    tempSelectedProducts.value.splice(index, 1);
  } else {
    if (tempSelectedProducts.value.length >= 3) {
      ElMessage.warning(t("note.maxRelatedProducts"));
      return;
    }
    tempSelectedProducts.value.push(product);
  }
};

// 移除已关联的商品
const removeRelatedProduct = (product: any) => {
  const newProducts = noteData.value.relatedProducts?.filter((p) => p.id !== product.id) || [];
  noteData.value = {
    ...noteData.value,
    relatedProducts: newProducts,
  };
};

// 确认选择
const confirmProductSelection = () => {
  noteData.value = {
    ...noteData.value,
    relatedProducts: [...tempSelectedProducts.value],
  };
  productDialogVisible.value = false;
};

// 取消选择
const cancelProductSelection = () => {
  productDialogVisible.value = false;
  tempSelectedProducts.value = [];
};

// Cascader配置
const cascaderProps: CascaderProps = {
  label: "title",
  value: "id",
  checkStrictly: true, // 允许选择父级节点
};

// 让整个分类节点都可以点击
const setupCascaderNodeClick = (visible?: boolean) => {
  if (!visible) return; // 只在打开时设置

  nextTick(() => {
    // 强制设置下拉框向下展开
    const dropdown = document.querySelector(".el-cascader__dropdown") as HTMLElement;
    if (dropdown) {
      dropdown.style.top = "auto";
      dropdown.style.bottom = "auto";
      // 移除可能存在的向上展开的样式
      dropdown.classList.remove("is-reverse");
    }

    // 使用事件委托，监听整个文档的点击事件
    const handleDocumentClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      // 如果点击的是 cascader 节点，但不是 radio 按钮本身
      const node = target.closest(".el-cascader-node");
      if (node) {
        const radioInput = target.closest(".el-radio__input");
        // 如果点击的不是 radio 按钮
        if (!radioInput) {
          // 检查节点是否有子分类（通过检查是否有展开图标）
          const postfix = node.querySelector(".el-cascader-node__postfix");
          const isLeaf = node.classList.contains("is-leaf");

          // 如果有子分类（有展开图标且不是叶子节点），点击应该展开而不是选中
          if (postfix && !isLeaf) {
            // 触发展开：点击展开图标区域
            const expandIcon = postfix.querySelector(".el-icon") as HTMLElement;
            if (expandIcon) {
              e.preventDefault();
              e.stopPropagation();
              expandIcon.click();
            }
          } else {
            // 如果没有子分类（叶子节点），点击选中
            const radio = node.querySelector(".el-radio__input") as HTMLElement;
            if (radio) {
              e.preventDefault();
              e.stopPropagation();
              radio.click();
            }
          }
        }
      }
    };

    // 添加事件监听（使用 capture 阶段确保能捕获到）
    document.addEventListener("click", handleDocumentClick, true);

    // 当 cascader 关闭时移除事件监听
    setTimeout(() => {
      if (!document.querySelector(".el-cascader__dropdown")) {
        document.removeEventListener("click", handleDocumentClick, true);
      }
    }, 100);
  });
};

const getCategoryList = async () => {
  loadingState.value.categories = true;
  try {
    const res = await getCategoryTreeData();
    categoryOptions.value = res.data;
  } catch (error) {
    console.error("获取分类失败:", error);
    ElMessage.error(t("note.getCategoryFail"));
  } finally {
    loadingState.value.categories = false;
  }
};

const addTag = (type: string) => {
  const content = noteData.value.content || "";

  switch (type) {
    case "tag":
      noteData.value.content = `${content} #`;
      break;
    case "user":
      noteData.value.content = `${content} @`;
      break;
  }

  // 聚焦到输入框
  nextTick(() => {
    const textarea = document.querySelector(".input-content textarea") as HTMLTextAreaElement;
    if (textarea) {
      textarea.focus();
      // 将光标移动到末尾
      textarea.selectionStart = textarea.selectionEnd = textarea.value.length;
    }
  });
};

const getShopCategoryList = async () => {
  try {
    const res = await getShopCategoryTreeData();
    shopCategoryOptions.value = res.data;
  } catch (error) {
    // console.error('获取分类失败:', error);
  }
};

const getAlbumList = async (userId: string) => {
  try {
    const res = await getAlbumData(userId);
    albumOptions.value = res.data.map((album: any) => ({
      id: album.id,
      title: album.title,
      noteCount: album.imgCount,
    }));
  } catch (error) {
    console.error("获取专辑失败:", error);
  }
};

// 处理专辑选择变化
const handleAlbumChange = async (value: string | number) => {
  // 如果 value 是字符串且不在现有列表中（通过ID查找），说明是用户输入的新专辑名称
  if (typeof value === "string" && value.trim()) {
    // 先检查是否是通过标题匹配的（allow-create 会创建临时选项，value 可能是字符串标题）
    const existingAlbumById = albumOptions.value.find((album) => String(album.id) === String(value));
    const existingAlbumByTitle = albumOptions.value.find((album) => album.title === value.trim());

    // 如果不是已存在的专辑（既不是ID匹配，也不是标题匹配），创建新专辑
    if (!existingAlbumById && !existingAlbumByTitle) {
      const userId = userInfo.value?.id;
      if (!userId) {
        ElMessage.error(t("note.userNotFoundAlbum"));
        noteData.value.albumId = "";
        return;
      }

      try {
        const res: any = await createAlbum({ title: value.trim(), sort: 0, uid: userId });
        if (res?.code === 200 || res?.data?.code === 200) {
          // 创建成功，刷新专辑列表
          await getAlbumList(userId);
          // 选中新创建的专辑
          const newAlbum = albumOptions.value.find((album) => album.title === value.trim());
          if (newAlbum) {
            noteData.value.albumId = newAlbum.id;
            ElMessage.success(t("note.albumCreateSuccess"));
          }
        }
      } catch (error: any) {
        console.error("创建专辑失败:", error);
        ElMessage.error(error?.response?.data?.msg || error?.message || t("note.createAlbumFail"));
        // 创建失败，清空选择
        noteData.value.albumId = "";
      }
    } else if (existingAlbumByTitle && !existingAlbumById) {
      // 如果通过标题找到了，但ID不匹配，说明是临时选项，需要设置为正确的ID
      noteData.value.albumId = existingAlbumByTitle.id;
    }
  }
};

const getHotTag = async () => {
  getTagByKeyword(1, pageSize, "").then((res) => {
    const { records } = res.data;
    records.forEach((item: any) => {
      hotTags.value.push(item.title);
    });
  });
};

// 标签相关方法
const handleClose = (tag: any) => {
  const newTags = props.dynamicTags.filter((t) => t !== tag);
  emit("update:dynamicTags", newTags);
};

const showInput = () => {
  inputVisible.value = true;
  nextTick(() => {
    InputRef.value?.input?.focus();
  });
};

const validateInput = (value: string) => {
  return value.trim().length > 0;
};

const handleInputConfirm = () => {
  const value = inputValue.value.trim();
  if (value && validateInput(value)) {
    if (props.dynamicTags.length >= 5) {
      ElMessage.warning(t("user.maxTags"));
      return;
    }
    if (!props.dynamicTags.includes(value)) {
      emit("update:dynamicTags", [...props.dynamicTags, value]);
    }
  }
  inputVisible.value = false;
  inputValue.value = "";
};

const handleInputBlur = () => {
  handleInputConfirm();
};

const selectHotTag = (tag: string) => {
  if (props.dynamicTags.length >= 5) {
    ElMessage.warning(t("user.maxTags"));
    return;
  }
  if (!props.dynamicTags.includes(tag)) {
    emit("update:dynamicTags", [...props.dynamicTags, tag]);
  }
};

// 分类变更
const handleCategoryChange = (ids: Array<any>) => {
  if (props.currentTab === "idle") {
    emit("update:shopCategoryList", ids);
  } else {
    emit("update:categoryList", ids);
  }
  // 选中后关闭下拉框
  if (props.currentTab === "idle") {
    ShopCategoryCascaderRef.value?.togglePopperVisible();
  } else {
    CategoryCascaderRef.value?.togglePopperVisible();
  }
};

// 获取位置
const handleGetLocation = () => {
  emit("get-location");
};

// 发布
const handlePublish = async () => {
  // console.log('发布数据:', {
  //   note: noteData.value,
  //   categories: props.currentTab === 'idle' ? props.shopCategoryList : props.categoryList,
  //   tags: props.dynamicTags,
  //   location: props.locationInfo
  // })

  if (!noteData.value.title?.trim()) {
    ElMessage.warning(t("note.titleRuleRequired"));
    return;
  }
  if (!noteData.value.content?.trim()) {
    ElMessage.warning(t("common.inputContent"));
    return;
  }

  const categories = props.currentTab === "idle" ? props.shopCategoryList : props.categoryList;

  if (!categories.length) {
    ElMessage.warning(t("note.chooseCategoryPlease"));
    return;
  }

  // 闲置商品特殊验证
  if (props.currentTab === "idle") {
    if (!noteData.value.postType) {
      ElMessage.warning(t("idle.chooseDeliveryMethod"));
      return;
    }
    if (!noteData.value.price) {
      ElMessage.warning(t("note.priceInputPh"));
      return;
    }
    if (!noteData.value.originalPrice) {
      ElMessage.warning(t("note.originalPriceInputPh"));
      return;
    }
  }

  // 构建发布数据
  const publishData = {
    note: {
      ...noteData.value,
      // 如果是闲置模式，清空关联商品
      relatedProducts: props.currentTab === "idle" ? [] : noteData.value.relatedProducts,
    },
    categories,
    tags: props.dynamicTags,
    address: props.locationInfo,
  };
  // 传递已上传的URL，避免重复上传
  emit("publish", {
    ...publishData,
    uploadedFileUrls: uploadedFileUrls.value,
    uploadedCoverUrl: uploadedCoverUrl.value,
    uploadedVideoUrl: uploadedVideoUrl.value,
  });
};

// 重置
const handleReset = () => {
  // 检查是否有已上传的文件或内容
  const hasFiles = props.fileList && props.fileList.length > 0;
  const hasVideo = props.videoUrl && props.videoUrl.length > 0;
  const hasContent = props.note?.title || props.note?.content;

  if (hasFiles || hasVideo || hasContent) {
    // 有内容时显示确认弹窗
    ElMessageBox.confirm(t("note.cancelPublishWarn"), t("common.tip"), {
      confirmButtonText: t("common.confirmDiscardCancel"),
      cancelButtonText: t("common.continueEditing"),
      type: "warning",
      distinguishCancelAndClose: true,
    })
      .then(() => {
        // 用户确认取消
        emit("update:note", {
          title: "",
          content: "",
          albumId: "",
          relatedProducts: [], // 清空关联商品
        });
        emit("update:dynamicTags", []);
        emit("update:categoryList", []);
        emit("update:shopCategoryList", []);
        emit("update:album", "");
        emit("update:locationInfo", "");
        // 清空临时选择的商品
        tempSelectedProducts.value = [];
        // 通知父组件进行完整重置
        emit("reset");
      })
      .catch(() => {
        // 用户取消操作，不做任何处理
      });
  } else {
    // 没有内容时直接重置
    emit("update:note", {
      title: "",
      content: "",
      albumId: "",
      relatedProducts: [], // 清空关联商品
    });
    emit("update:dynamicTags", []);
    emit("update:categoryList", []);
    emit("update:shopCategoryList", []);
    emit("update:album", "");
    emit("update:locationInfo", "");
    // 清空临时选择的商品
    tempSelectedProducts.value = [];
    // 通知父组件进行完整重置
    emit("reset");
  }
};

// 添加防抖的数据更新处理
const emitNoteUpdate = debounce((newVal: any) => {
  emit("update:note", newVal);
}, 300);

// 监听数据变化
watch(
  () => noteData.value,
  (newVal) => {
    // console.log('noteData changed:', newVal)
    emitNoteUpdate(newVal);
  },
  { deep: true }
);

// 监听专辑选择变化
watch(
  () => noteData.value.albumId,
  (newVal) => {
    emit("update:album", newVal);
  }
);

// 监听props变化，同步更新本地数据
watch(
  () => props.note,
  (newVal) => {
    if (!newVal || Object.keys(newVal).length === 0) {
      // 如果note被清空，也清空本地数据
      inputValue.value = "";
      inputVisible.value = false;
    }
  }
);

const initData = () => {
  userInfo.value = userStore.getUserInfo();
  isLogin.value = userStore.isLogin();
  if (isLogin.value) {
    // const noteId = route.query.noteId as string;
    // if (noteId !== "" && noteId !== undefined) {
    //   getNoteByIdMethod(noteId);
    // }
    getCategoryList();
    getShopCategoryList();
    getAlbumList(userInfo.value.id);
    getHotTag();
  }
};

initData();
</script>

<style lang="less" scoped>
// 主题变量
@primary-color: #3d8af5;
@text-primary: rgba(51, 51, 51, 0.8);
@text-secondary: rgba(51, 51, 51, 0.6);
@border-color: rgba(0, 0, 0, 0.08);
@white: #fff;

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

        .price-group {
          display: flex;
          align-items: baseline;
          gap: 6px;
          flex-shrink: 0;
        }

        .current-price {
          font-weight: bold;
          font-size: 15px;
          color: #ff2442;
          line-height: 1;
          flex-shrink: 0;
        }

        .original-price {
          font-size: 11px;
          color: #9e9e9e;
          font-weight: normal;
          text-decoration: line-through;
          line-height: 1;
          flex-shrink: 0;
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
      border-color: @primary-color;
      color: @primary-color;
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
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
    margin-bottom: 16px;
    max-height: calc(2 * (180px + 80px) + 16px); // 两行高度：图片高度 + 信息高度 + 间距
    overflow-y: auto;

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
        border-color: @primary-color;
      }

      .el-image {
        width: 100%;
        height: 180px;
        background-color: #f5f5f5;
      }

      .product-info {
        padding: 8px;
        display: flex;
        flex-direction: column;
        gap: 6px;

        .product-title {
          font-size: 14px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          line-height: 1.4;
        }

        .price-group {
          display: flex;
          align-items: baseline;
          gap: 8px;
          flex-shrink: 0;
        }

        .current-price {
          font-weight: bold;
          font-size: 16px;
          color: #ff2442;
          line-height: 1;
          flex-shrink: 0;
        }

        .original-price {
          font-size: 12px;
          color: #9e9e9e;
          font-weight: normal;
          text-decoration: line-through;
          line-height: 1;
          flex-shrink: 0;
        }
      }

      .product-mask {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(64, 158, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10;

        .el-icon {
          background: #409eff;
          color: #fff;
          border-radius: 50%;
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
        }
      }
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
  }
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

.emoji-picker {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;

  .emoji-item {
    font-size: 20px;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: scale(1.2);
    }
  }
}

.content-editor {
  margin: 20px;
  margin-top: 10px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;

  .cover-title-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .cover-title {
    font-size: 16px;
    font-weight: 500;
  }

  .ai-generate-btn {
    :deep(.el-icon) {
      margin-right: 4px;
    }
  }

  .push-content {
    margin-bottom: 20px;

    .input-title {
      width: 350px;
      margin-bottom: 12px;
      font-size: 15px;
    }

    .input-content {
      font-size: 14px;
      width: 600px;
    }
  }

  .btns {
    padding: 10px 0;
    display: flex;
    gap: 6px;

    .css-fm44j {
      min-width: 62px;
      height: 24px;
      font-size: 12px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: @primary-color;
        color: @primary-color;
      }
    }
  }

  .tag-list {
    margin: 16px 0;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .tag-item {
      transition: all 0.3s;
      background-color: #409eff !important;
      border-color: #409eff !important;
      color: #fff !important;
      font-weight: 500;

      &:hover {
        transform: translateY(-2px);
        background-color: #66b1ff !important;
        border-color: #66b1ff !important;
      }

      :deep(.el-tag__close) {
        color: #fff !important;

        &:hover {
          color: #fff !important;
          background-color: rgba(255, 255, 255, 0.2) !important;
        }
      }
    }
  }

  .hot-tag {
    margin-bottom: 16px;

    .tag-title-text {
      font-size: 14px;
      color: #666;
      margin-right: 8px;
    }

    .hot-tag-item {
      margin: 0 8px 8px 0;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
      }
    }
  }

  .categorys,
  .albums,
  .location {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 8px;

    label {
      font-size: 14px;
      color: #666;
    }
  }

  // 强制下拉框向下展开
  :deep(.el-cascader__dropdown) {
    top: auto !important;
    bottom: auto !important;
    transform-origin: top center !important;

    &.is-reverse {
      transform-origin: top center !important;
    }
  }

  // 让整个分类项都可以点击
  :deep(.el-cascader-menu) {
    .el-cascader-node {
      cursor: pointer;
      padding: 8px 20px;

      // 确保整个节点都可以点击，包括文字部分
      &:hover {
        background-color: #f5f7fa;
      }

      // 让节点文字部分也可以点击
      .el-cascader-node__label {
        cursor: pointer;
        flex: 1;
        user-select: none;
      }
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
      color: @primary-color;
    }

    .location-text {
      font-size: 14px;
    }
  }
}

// 发布按钮样式 - 独立于内容框
.submit {
  margin: 24px 20px 20px 20px;
  display: flex;
  justify-content: center;
  gap: 16px;

  button {
    width: 100px;
    height: 36px;
    margin: 2px 2px 20px 2px;
    border-radius: 18px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s;

    &.publishBtn {
      background: @primary-color;
      color: #fff;
      border: none;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(36, 149, 255, 0.3);
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
        background: #d9d9d9;
      }
    }

    &.clearBtn {
      background: #fff;
      border: 1px solid #d9d9d9;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
        color: #999;
      }
    }
  }
}

// AI生成中的消息样式（全局样式，不遮盖页面）
:deep(.ai-generating-message) {
  z-index: 3000;
  background-color: #409eff;
  border-color: #409eff;

  .el-message__content {
    color: #fff;
    font-weight: 500;
  }
}
</style>