<template>
  <div class="edit-note-form">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" @submit.prevent="submitForm">
      <!-- 封面图 -->
      <el-form-item :label="$t('note.coverImage')" prop="noteCover">
        <div class="cover-upload">
          <el-upload
            class="upload-container"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="uploadCover"
          >
            <div class="cover-upload-trigger" v-if="!form.noteCover">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <span class="upload-text">{{ $t("note.uploadCoverText") }}</span>
            </div>
            <el-image v-else :src="form.noteCover" fit="cover" class="cover-preview">
              <template #error>
                <div class="image-error">
                  <el-icon>
                    <Picture />
                  </el-icon>
                </div>
              </template>
            </el-image>
          </el-upload>
          <el-button v-if="form.noteCover" type="primary" link class="remove-cover" @click="removeCover">
            {{ $t("note.removeCoverText") }}
          </el-button>
        </div>
      </el-form-item>

      <!-- 媒体内容 -->
      <el-form-item
        :label="form.noteType === '1' ? $t('note.labelImage') : $t('note.labelVideo')"
        prop="imgList"
        class="media-form-item"
      >
        <div class="media-upload">
          <!-- 图片上传 -->
          <el-upload
            v-if="form.noteType === '1'"
            class="img-upload-container"
            :file-list="fileList"
            list-type="picture-card"
            :before-upload="beforeUpload"
            :http-request="uploadMedia"
            :on-remove="handleRemove"
            :on-preview="handlePreview"
            :limit="9"
            :on-exceed="handleExceed"
            accept="image/*"
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
              accept="video/*"
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

      <!-- 关联商品 -->
      <el-form-item :label="$t('note.relatedProducts')" prop="tags">
        <div class="related-products">
          <!-- 选择商品按钮 -->
          <el-button
            type="primary"
            plain
            size="small"
            @click="showProductSelector"
            :disabled="form.relatedProducts?.length >= 3"
          >
            <el-icon><Plus /></el-icon>
            {{ $t("note.selectProductBtn") }}
          </el-button>

          <!-- 已选商品展示 -->
          <div class="products-container" v-if="form.relatedProducts?.length">
            <div class="selected-products">
              <div v-for="product in form.relatedProducts" :key="product.id" class="product-card">
                <img :src="product.cover" :alt="product.title" class="product-img" />
                <div class="product-info">
                  <div class="product-title">{{ product.title }}</div>
                  <div class="price-group">
                    <span class="current-price">￥{{ convert.to_price(product.price) }}</span>
                    <span class="original-price" v-if="product.originalPrice && product.originalPrice > product.price">
                      ￥{{ convert.to_price(product.originalPrice) }}
                    </span>
                  </div>
                </div>
                <div class="remove-btn" @click.stop="removeRelatedProduct(product)">
                  <el-icon><Close /></el-icon>
                </div>
              </div>
            </div>

            <!-- 添加商品按钮 -->
            <div class="add-product-btn" v-if="form.relatedProducts?.length < 3" @click="showProductSelector">
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
                    <div class="info-row">
                      <div class="product-title">{{ product.title }}</div>
                      <div class="product-price">¥{{ product.price }}</div>
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
          @change="handleCategoryChange"
          @visible-change="setupCascaderNodeClick"
        />
      </el-form-item>

      <!-- 专辑选择 -->
      <el-form-item :label="$t('note.selectAlbum')">
        <el-select
          v-model="form.albumId"
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
              <span class="album-count" v-if="item.noteCount">
                - {{ $t("note.albumNoteCount", { n: item.noteCount }) }}
              </span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 标签 -->
      <el-form-item :label="$t('note.tag')" prop="tags">
        <div class="tags-container">
          <div class="tags-list">
            <el-tag
              v-for="tag in form.tags"
              :key="tag"
              closable
              :disable-transitions="false"
              @close="handleClose(tag)"
              type="primary"
            >
              {{ tag }}
            </el-tag>
            <el-input
              v-if="inputVisible"
              ref="InputRef"
              v-model="inputValue"
              class="tag-input"
              size="small"
              @keyup.enter="handleInputConfirm"
              @blur="handleInputConfirm"
            />
            <el-button v-else class="button-new-tag" size="small" @click="showInput" :disabled="form.tags.length >= 5">
              {{ $t("user.addTag") }}
            </el-button>
          </div>
          <div class="tags-tip">{{ $t("note.maxFiveTagsTip") }}</div>
        </div>
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
import { ref, watch, onMounted, onBeforeUnmount, nextTick, computed } from "vue";
import { useI18n } from "vue-i18n";
import { Location, Plus, Picture, VideoCamera, Search, Check, Close } from "@element-plus/icons-vue";
import { debounce } from "lodash-es";
import type { FormInstance, FormRules, ElInput, UploadProps, UploadUserFile } from "element-plus";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/userStore";
import { getCategoryTreeData } from "@/api/category";
import { getUserProducts } from "@/api/user";
import { getAlbumData, createAlbum } from "@/api/album";
import { uploadFileMethod } from "@/utils/util";
import AddressEdit from "@/views/push/address-edit.vue";
import convert from "@/utils/convert";

const { t } = useI18n();

const props = defineProps({
  noteData: {
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

const emit = defineEmits(["save", "cancel", "change", "save-complete"]);

interface Product {
  id: string | number;
  cover: string;
  title: string;
  price: string | number;
  // 你还可以加其它字段
}

interface AlbumOption {
  id: string | number;
  title: string;
  noteCount?: number;
}

// 新增文件暂存（保留字段，方便后续扩展；当前模式下不再依赖它来提交）
const tempFiles = ref({
  cover: null as File | null,
  media: [] as File[],
});

const userInfo = ref<any>({});
const userStore = useUserStore();

const drawer = ref(false);
const cascaderRef = ref();
const locationInfo = ref("");

const formRef = ref<FormInstance>();
const albumOptions = ref<AlbumOption[]>([]);
const categoryOptions = ref<any[]>([]);
const fileList = ref<{ name: string; url: string; status: string }[]>([]);

const inputValue = ref("");
const inputVisible = ref(false);

const InputRef = ref<InstanceType<typeof ElInput>>();

const productDialogVisible = ref(false);
const productSearchKeyword = ref("");
const productPage = ref(1);
const productPageSize = ref(10);
const totalProducts = ref(0);
const userProducts = ref<Product[]>([]);
const tempSelectedProducts = ref<Product[]>([]);

// 预览相关
const dialogImageUrl = ref("");
const dialogVisible = ref(false);

// 表单校验规则
const rules = computed<FormRules>(() => ({
  title: [
    { required: true, message: t("note.titleRuleRequired"), trigger: "blur" },
    { min: 2, max: 50, message: t("note.titleRuleLength"), trigger: "blur" },
  ],
  tags: [{ type: "array", max: 5, message: t("note.tagsRuleMax"), trigger: "change" }],
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
        if (form.value.noteType === "1" && value.length > 9) {
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
  tags: [] as string[],
  location: "",
  noteCover: "", // 保存临时预览URL
  noteType: "1",
  imgList: [] as string[], // 保存临时预览URL
  cid: "",
  cpid: "",
  relatedProducts: [] as any[],
  albumId: "",
  longitude: undefined as number | undefined,
  latitude: undefined as number | undefined,
  address: "",
  locationName: "",
  province: "",
  city: "",
});

const handleClose = (tag: string) => {
  form.value.tags = form.value.tags.filter((t) => t !== tag);
};

const showInput = () => {
  inputVisible.value = true;
  nextTick(() => {
    InputRef.value?.input?.focus();
  });
};

const handleInputConfirm = () => {
  if (inputValue.value) {
    if (form.value.tags.length >= 5) {
      ElMessage.warning(t("note.tagsMaxFiveWarn"));
      return;
    }
    if (!form.value.tags.includes(inputValue.value)) {
      form.value.tags.push(inputValue.value);
    }
  }
  inputVisible.value = false;
  inputValue.value = "";
};

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

const getAlbumList = async (userId: string) => {
  loading.value = true;
  try {
    const res = (await getAlbumData(userId)) as any;
    if (res.code === 200 && Array.isArray(res.data)) {
      albumOptions.value = res.data.map((album: any) => ({
        id: album.id,
        title: album.title,
        noteCount: album.imgCount,
      }));
    } else {
      ElMessage.error(t("note.getAlbumListFail"));
    }
  } catch (error) {
    console.error("获取专辑失败:", error);
    ElMessage.error(t("note.getAlbumListFail"));
  } finally {
    loading.value = false;
  }
};

// 处理专辑选择变化
const handleAlbumChange = async (value: string | number) => {
  // 如果 value 是字符串且不在现有列表中，说明是用户输入的新专辑名称
  if (typeof value === "string" && value.trim()) {
    // 先检查是否是通过标题匹配的（allow-create 会创建临时选项，value 可能是字符串标题）
    const existingAlbumById = albumOptions.value.find((album) => String(album.id) === String(value));
    const existingAlbumByTitle = albumOptions.value.find((album) => album.title === value.trim());

    // 如果不是已存在的专辑（既不是ID匹配，也不是标题匹配），创建新专辑
    if (!existingAlbumById && !existingAlbumByTitle) {
      const userId = userInfo.value?.id;
      if (!userId) {
        ElMessage.error(t("note.userNotFoundAlbum"));
        form.value.albumId = "";
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
            form.value.albumId = String(newAlbum.id);
            ElMessage.success(t("note.albumCreateSuccess"));
            handleFormChange();
          }
        }
      } catch (error: any) {
        console.error("创建专辑失败:", error);
        ElMessage.error(error?.response?.data?.msg || error?.message || t("note.createAlbumFail"));
        // 创建失败，清空选择
        form.value.albumId = "";
      }
    } else if (existingAlbumByTitle && !existingAlbumById) {
      // 如果通过标题找到了，但ID不匹配，说明是临时选项，需要设置为正确的ID
      form.value.albumId = String(existingAlbumByTitle.id);
      handleFormChange();
    }
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

// 确认选择
const confirmProductSelection = () => {
  form.value.relatedProducts = [...tempSelectedProducts.value];
  productDialogVisible.value = false;
  handleFormChange();
};

// 取消选择
const cancelProductSelection = () => {
  productDialogVisible.value = false;
  tempSelectedProducts.value = [];
};

// 显示商品选择器
const showProductSelector = () => {
  productDialogVisible.value = true;
  tempSelectedProducts.value = [...(form.value.relatedProducts || [])];
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

// 移除已关联的商品
const removeRelatedProduct = (product: any) => {
  form.value.relatedProducts = form.value.relatedProducts.filter((p) => p.id !== product.id);
  handleFormChange();
};

// 初始化分类数据
const initCategories = async () => {
  loading.value = true;
  try {
    const res = (await getCategoryTreeData()) as any;
    if (res.code === 200 && Array.isArray(res.data)) {
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
  if (form.value.noteType === "1" && form.value.imgList.length >= 9) {
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
  () => props.noteData,
  (newVal) => {
    if (newVal) {
      form.value = {
        id: newVal.id,
        title: newVal.title,
        content: newVal.content || "",
        tags: newVal.tags ? JSON.parse(newVal.tags) : [],
        // 位置显示优先用后端的 locationName，其次是 address
        location: newVal.locationName || newVal.address || "",
        noteCover: newVal.noteCover,
        noteType: newVal.noteType,
        imgList:
          newVal.noteType === "1"
            ? newVal.urls
              ? JSON.parse(newVal.urls)
              : []
            : newVal.urls
            ? [JSON.parse(newVal.urls)[0]]
            : [],
        cid: newVal.cid || "",
        cpid: newVal.cpid || "",
        albumId: newVal.albumId || "",
        relatedProducts: Array.isArray(newVal.relatedProducts) ? newVal.relatedProducts : [],
        // 位置信息：从后端原始数据恢复，避免未修改时被清空
        longitude: newVal.longitude ?? undefined,
        latitude: newVal.latitude ?? undefined,
        address: newVal.address || "",
        locationName: newVal.locationName || newVal.address || "",
        province: newVal.province || "",
        city: newVal.city || "",
      };
      // 更新文件列表(仅图片模式)
      if (newVal.noteType === "1") {
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
  () => form.value.noteType,
  (newType, oldType) => {
    if (newType !== oldType) {
      // 清理之前的媒体内容
      if (form.value.imgList.length) {
        form.value.imgList.forEach((url) => {
          if (url.startsWith("blob:")) {
            revokeObjectURL(url);
          }
        });
        form.value.imgList = [];
        tempFiles.value.media = [];
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
    await Promise.all([initCategories(), getAlbumList(userInfo.value.id)]);
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

// 清理临时预览URL
const revokeObjectURL = (url: string) => {
  if (url.startsWith("blob:")) {
    URL.revokeObjectURL(url);
  }
};

// 修改封面上传处理：前端直接上传到 OSS，拿到 URL 后回填
const uploadCover = async ({ file }: any) => {
  try {
    const res: any = await uploadFileMethod(file);
    if (res.code === 200 && res.data) {
      form.value.noteCover = res.data; // 后端返回的封面 URL
      handleFormChange();
    } else {
      throw new Error(res.msg || t("note.coverUploadFail"));
    }
  } catch (error) {
    console.error("封面上传失败:", error);
    ElMessage.error(t("note.coverUploadFail"));
  }
};

// 修改图片上传处理：前端直接上传到 OSS，拿到 URL 后 push 到 imgList
const uploadMedia = async ({ file }: any) => {
  // 检查数量限制
  if (form.value.imgList.length >= 9) {
    ElMessage.warning(t("note.ruleMaxNineImages"));
    return;
  }
  try {
    const res: any = await uploadFileMethod(file);
    if (res.code === 200 && res.data) {
      const url = res.data; // 单文件上传返回的 URL
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

// 修改视频上传处理：前端直接上传视频到 OSS
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
      // 覆盖之前的视频 URL
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

// 修改移除处理
const handleRemove = (file: any) => {
  const index = fileList.value.findIndex((f) => f.url === file.url);
  if (index !== -1) {
    // 清理预览URL
    revokeObjectURL(file.url);
    // 移除文件和预览
    tempFiles.value.media.splice(index, 1);
    fileList.value.splice(index, 1);
    form.value.imgList.splice(index, 1);
    handleFormChange();
  }
};

const removeCover = () => {
  if (form.value.noteCover) {
    revokeObjectURL(form.value.noteCover);
  }
  tempFiles.value.cover = null;
  form.value.noteCover = "";
  handleFormChange();
};

const removeVideo = () => {
  try {
    if (form.value.imgList[0]?.startsWith("blob:")) {
      revokeObjectURL(form.value.imgList[0]);
    }
    tempFiles.value.media = [];
    form.value.imgList = [];
    handleFormChange();
    ElMessage.success(t("note.videoRemovedOk"));
  } catch (error) {
    console.error("视频移除失败:", error);
    ElMessage.error(t("note.videoRemoveFail"));
  }
};

// 处理图片预览
const handlePreview = (file: UploadUserFile) => {
  dialogImageUrl.value = file.url!;
  dialogVisible.value = true;
};

// 组件卸载时清理预览URL
onBeforeUnmount(() => {
  if (form.value.noteCover?.startsWith("blob:")) {
    revokeObjectURL(form.value.noteCover);
  }
  form.value.imgList.forEach((url) => {
    if (url.startsWith("blob:")) {
      revokeObjectURL(url);
    }
  });
});

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    loading.value = true;
    // 构造 noteData：直接使用已上传到 OSS 的 URL
    const payload: any = { ...form.value };
    // 统一以当前上传组件的文件列表为准，确保删除/新增后的顺序和数量准确
    if (payload.noteType === "1") {
      // 图文：urls 为当前所有图片的 URL 列表
      payload.urls = fileList.value.map((f) => f.url);
    } else if (payload.noteType === "2") {
      // 视频：urls 只保留一个视频地址
      payload.urls = form.value.imgList && form.value.imgList.length ? [form.value.imgList[0]] : [];
    }
    // 构造FormData，仅携带 noteData，后端不再接收文件
    const formData = new FormData();
    formData.append("noteData", JSON.stringify(payload));
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
      margin-top: 80px;
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

        // 一行显示 3 张图，固定宽高
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
      // 三列布局：每行展示 3 个
      width: 120px;
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
        height: 80px;
        object-fit: cover;
        display: block;
      }

      .product-info {
        padding: 8px;

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
          font-size: 14px;
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
      width: 120px;
      height: 120px; // 与商品卡片等高
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
        border-color: #409eff;
        color: #409eff;
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
          border-color: #409eff;
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
        background-color: #409eff !important;
        border-color: #409eff !important;
        color: #fff !important;
        font-weight: 500;

        &:hover {
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

      .tag-input {
        width: 100px;
        margin-right: 8px;
        vertical-align: bottom;
      }

      .button-new-tag {
        height: 32px;
        padding: 0 8px;
        border-radius: 16px;
        border: 1px dashed #409eff;
        color: #409eff;
        background-color: transparent;

        &:hover {
          border-color: #66b1ff;
          color: #66b1ff;
          background-color: #ecf5ff;
        }

        &:disabled {
          cursor: not-allowed;
          border-color: #d9d9d9;
          color: #999;
          background-color: transparent;
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