<template>
  <a-modal
    :visible="open"
    :title="state.title"
    width="1000px"
    :body-style="{ maxHeight: '70vh', overflowY: 'auto', padding: '20px' }"
    @cancel="handleCancel"
    @ok="handleConfirm"
  >
    <a-form
      ref="formRef"
      :model="state.form"
      :rules="formRules"
      auto-label-width
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 20 }"
    >
      <a-row :gutter="16">
        <!-- 基本信息 -->
        <a-col :span="12">
          <a-form-item :label="$t('biz.productTitle')" field="title">
            <a-input
              v-model="state.form.title"
              :placeholder="$t('biz.pleaseInputProductTitle')"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="$t('biz.userField')" field="uid" required>
            <a-select
              v-model="state.form.uid"
              :loading="state.userSearching"
              :placeholder="$t('biz.searchUserPlaceholder')"
              allow-search
              allow-clear
              :filter-option="false"
              @search="handleUserSearch"
              @change="handleUserChange"
            >
              <template #empty>
                <div v-if="state.userSearching" style="text-align: center; padding: 8px">
                  {{ $t('biz.searching') }}
                </div>
                <div v-else style="text-align: center; padding: 8px; color: #86909c">
                  {{ $t('biz.searchUserKeyword') }}
                </div>
              </template>
              <a-option
                v-for="option in state.userOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              >
                <div style="display: flex; align-items: center; gap: 8px">
                  <a-avatar v-if="option.avatar" :size="24" :image-url="option.avatar" />
                  <span>{{ option.username }}</span>
                  <span style="color: #86909c; font-size: 12px">(ID: {{ option.id }})</span>
                </div>
              </a-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="$t('biz.category')" field="cpid">
            <a-tree-select
              v-model="state.form.cpid"
              :field-names="{
                key: 'id',
                title: 'title',
                children: 'children',
              }"
              :data="state.navbarOptions"
              :show-count="false"
              :placeholder="$t('biz.selectCategory')"
              allow-clear
            />
          </a-form-item>
        </a-col>
        
        <!-- 价格信息 -->
        <a-col :span="12">
          <a-form-item :label="$t('biz.productPrice')" field="price">
            <a-input-number
              v-model="state.form.price"
              :min="0"
              :precision="2"
              :placeholder="$t('biz.pleaseInputProductPrice')"
            >
              <template #prefix>¥</template>
            </a-input-number>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="$t('biz.originalPrice')" field="originalPrice">
            <a-input-number
              v-model="state.form.originalPrice"
              :min="0"
              :precision="2"
              :placeholder="$t('biz.pleaseInputOriginalPrice')"
            >
              <template #prefix>¥</template>
            </a-input-number>
          </a-form-item>
        </a-col>
        
        <!-- 状态信息 -->
        <a-col :span="12">
          <a-form-item :label="$t('biz.productTypeField')" field="productType">
            <a-radio-group v-model="state.form.productType">
              <a-radio
                v-for="dict in state.productTypeOptions"
                :key="dict.dictValue"
                :value="Number(dict.dictValue)"
              >
                {{ dict.dictLabel }}
              </a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="$t('biz.auditStatus')" field="auditStatus">
            <a-radio-group v-model="state.form.auditStatus" disabled>
              <a-radio
                v-for="dict in state.auditStatusOptions"
                :key="dict.dictValue"
                :value="dict.dictValue"
              >
                {{ dict.dictLabel }}
              </a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        
        <a-col :span="12">
          <a-form-item :label="$t('biz.pinned')" field="pinned">
            <a-radio-group v-model="state.form.pinned">
              <a-radio
                v-for="dict in state.pinnedOptions"
                :key="dict.dictValue"
                :value="dict.dictValue"
              >
                {{ dict.dictLabel }}
              </a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="$t('biz.deliveryMethod')" field="postType">
            <a-radio-group v-model="state.form.postType">
              <a-radio :value="0">{{ $t('biz.postMail') }}</a-radio>
              <a-radio :value="1">{{ $t('biz.pickup') }}</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        
        <!-- 封面和商品图片/视频 -->
        <a-col :span="12">
          <a-form-item :label="$t('biz.productCover')" field="cover">
            <a-upload
              class="cover-uploader"
              list-type="picture-card"
              :file-list="fileList"
              :auto-upload="false"
              :limit="1"
              :image-preview="true"
              @before-upload="beforeUpload"
              @change="handleChange"
              @remove="handleRemove"
            >
              <template #upload-button>
                <div v-if="!state.form.cover" class="upload-placeholder">
                  <icon-plus style="font-size: 24px; color: #8c939d" />
                  <div style="font-size: 12px; margin-top: 8px;">{{ $t('biz.uploadCover') }}</div>
                </div>
                <template v-else>
                  <a-image
                    :src="state.form.cover"
                    width="120px"
                    height="120px"
                    fit="cover"
                    :preview="false"
                  />
                </template>
              </template>
            </a-upload>
          </a-form-item>
        </a-col>
        
        <!-- 商品图片/视频 -->
        <a-col :span="24">
          <a-form-item
            :label="state.form.productType === 2 ? $t('biz.productVideo') : $t('biz.productImages')"
            field="urls"
          >
            <!-- 视频类型 -->
            <div v-if="state.form.productType === 2">
              <!-- 视频预览 -->
              <div v-if="imageList.length > 0" style="display: flex; align-items: flex-end; gap: 16px; margin-bottom: 16px;">
                <video
                  v-for="item in imageList"
                  :key="item.uid"
                  :src="item.url"
                  controls
                  style="max-width: 400px; max-height: 250px; border-radius: 4px;"
                />
                <a-upload
                  :auto-upload="false"
                  :show-file-list="false"
                  accept="video/*"
                  @before-upload="beforeImageUpload"
                >
                  <template #upload-button>
                    <a-button type="primary">
                      <template #icon><icon-upload /></template>
                      {{ $t('biz.replaceVideo') }}
                    </a-button>
                  </template>
                </a-upload>
              </div>
              <!-- 视频上传 -->
              <a-upload
                v-if="imageList.length === 0"
                :auto-upload="false"
                :show-file-list="false"
                accept="video/*"
                @before-upload="beforeImageUpload"
              >
                <template #upload-button>
                  <a-button type="primary">
                    <template #icon><icon-upload /></template>
                    {{ $t('biz.uploadVideo') }}
                  </a-button>
                  <span style="margin-left: 8px; color: #86909c; font-size: 12px;">{{ $t('biz.videoFormatHint') }}</span>
                </template>
              </a-upload>
            </div>
            <!-- 图片类型 -->
            <a-upload
              v-else
              list-type="picture-card"
              :file-list="imageList"
              :auto-upload="false"
              :limit="9"
              :image-preview="true"
              @before-upload="beforeImageUpload"
              @change="handleImageChange"
              @remove="handleImageRemove"
            >
              <template #upload-button>
                <div class="upload-placeholder">
                  <icon-plus style="font-size: 24px; color: #8c939d" />
                  <div style="font-size: 12px; margin-top: 8px;">{{ $t('biz.uploadImage') }}</div>
                </div>
              </template>
            </a-upload>
          </a-form-item>
        </a-col>
        
        <!-- 位置信息 -->
        <a-col :span="12">
          <a-form-item :label="$t('biz.region')" field="region">
            <a-cascader
              v-model="regionValue"
              :options="state.regionOptions"
              :field-names="{
                value: 'name',
                label: 'name',
                children: 'children',
              }"
              :placeholder="$t('biz.selectRegion')"
              allow-clear
              path-mode
              @change="handleRegionChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="$t('biz.detailAddress')" field="address">
            <a-input
              v-model="state.form.address"
              :placeholder="$t('biz.pleaseInputDetailAddress')"
            />
          </a-form-item>
        </a-col>
        
        <!-- 商品内容 -->
        <a-col :span="24">
          <a-form-item :label="$t('biz.productDescField')" field="description">
            <a-textarea
              v-model="state.form.description"
              :auto-size="{ minRows: 6, maxRows: 12 }"
              :placeholder="$t('biz.pleaseInputProductDesc')"
            />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref, watch, nextTick } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { Form, Message } from '@arco-design/web-vue';
  import { IconPlus, IconUpload } from '@arco-design/web-vue/es/icon';
  import { getProduct, updateProduct, createProduct } from '@/api/idle/product';
  import { getIdleNavbarList } from '@/api/idle/navbar';
  import { getDicts } from '@/api/system/dict-data';
  import { uploadFile } from '@/api/web/xiaohongshu';
  import {
    getUserByKeyword,
    unwrapUserKeywordPage,
    type WebUserRecord,
  } from '@/api/web/user';

  const { t } = useI18n();

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    id: {
      type: String,
      default: undefined,
    },
  });

  const open = computed(() => props.visible);

  const fileList = ref<any[]>([]);
  // 多图片列表
  const imageList = ref<any[]>([]);
  // 省市区级联选择器的值
  const regionValue = ref<string[]>([]);

  const formRules = computed(() => ({
    title: [{ required: true, message: t('biz.pleaseInputProductTitle'), trigger: 'blur' }],
    price: [{ required: true, message: t('biz.pleaseInputProductPrice'), trigger: 'blur' }],
    uid: [{ required: true, message: t('biz.pleaseSelectUser'), trigger: 'change' }],
  }));

  const state = reactive({
    title: '',
    form: {
      id: undefined,
      title: undefined,
      cpid: undefined,
      price: undefined,
      originalPrice: undefined,
      productType: 1, // 默认图片类型
      auditStatus: undefined,
      pinned: undefined,
      postType: 0, // 默认邮寄
      cover: undefined,
      description: undefined,
      address: undefined,
      province: undefined,
      city: undefined,
      district: undefined,
      urls: [] as string[],
      file: undefined as File | undefined,
      uid: undefined as string | undefined,
      author: undefined as string | undefined,
    },
    userOptions: [] as Array<{ value: string; label: string; username: string; id: string; avatar?: string }>,
    userSearching: false,
    selectedUser: null as WebUserRecord | null,
    navbarOptions: [] as Array<any>,
    productTypeOptions: [] as Array<any>,
    auditStatusOptions: [] as Array<any>,
    pinnedOptions: [] as Array<any>,
    regionOptions: [] as Array<any>,
  });

  const emits = defineEmits(['update:visible', 'update:id', 'refreshDataList']);

  // 初始化字典数据
  const initDictData = async () => {
    try {
      const [productTypeRes, auditStatusRes, pinnedRes] = await Promise.all([
        getDicts('product_type'),
        getDicts('audit_status'),
        getDicts('sys_normal_disable'),
      ]);
      
      state.productTypeOptions = Array.isArray(productTypeRes.data) ? productTypeRes.data : [];
      state.auditStatusOptions = Array.isArray(auditStatusRes.data) ? auditStatusRes.data : [];
      state.pinnedOptions = Array.isArray(pinnedRes.data) ? pinnedRes.data : [];
    } catch (e) {
      console.warn('字典数据加载失败', e);
    }
  };

  // 将列表数据转换为树结构
  const listToTree = (list: any[]) => {
    const treeData: any[] = [];
    const map: Record<number, number> = {};
    
    // 初始化映射和children
    for (let i = 0; i < list.length; i += 1) {
      map[list[i].id] = i;
      list[i].children = [];
    }
    
    // 构建树形结构
    for (let i = 0; i < list.length; i += 1) {
      const node = list[i];
      if (node.pid && list[map[node.pid]]) {
        // 有父节点，添加到父节点的children中
        list[map[node.pid]].children.push(node);
      } else {
        // 没有父节点，作为根节点
        treeData.push(node);
      }
    }
    
    return treeData;
  };

  // 获取分类树
  const fetchTreeSelectOption = async () => {
    try {
      const response = await getIdleNavbarList({});
      const data = response.data || response;
      const list = Array.isArray(data)
        ? data
        : data?.list || data?.rows || data?.records || [];
      
      if (list && list.length > 0) {
        // 转换为树形结构
        state.navbarOptions = listToTree(list);
      } else {
        state.navbarOptions = [];
      }
    } catch (e) {
      console.warn('分类树加载失败', e);
      state.navbarOptions = [];
    }
  };

  // 初始化省市区数据
  const initRegionOptions = () => {
    state.regionOptions = [
      {
        name: '北京市',
        children: [
          {
            name: '北京市',
            children: [
              { name: '东城区' },
              { name: '西城区' },
              { name: '朝阳区' },
              { name: '丰台区' },
              { name: '石景山区' },
              { name: '海淀区' },
              { name: '门头沟区' },
              { name: '房山区' },
              { name: '通州区' },
              { name: '顺义区' },
              { name: '昌平区' },
              { name: '大兴区' },
              { name: '怀柔区' },
              { name: '平谷区' },
              { name: '密云区' },
              { name: '延庆区' },
            ],
          },
        ],
      },
      {
        name: '山东省',
        children: [
          {
            name: '济南市',
            children: [
              { name: '历下区' },
              { name: '市中区' },
              { name: '槐荫区' },
              { name: '天桥区' },
              { name: '历城区' },
            ],
          },
          {
            name: '青岛市',
            children: [
              { name: '市南区' },
              { name: '市北区' },
              { name: '黄岛区' },
              { name: '崂山区' },
              { name: '李沧区' },
              { name: '城阳区' },
              { name: '即墨区' },
            ],
          },
        ],
      },
      {
        name: '上海市',
        children: [
          {
            name: '上海市',
            children: [
              { name: '黄浦区' },
              { name: '徐汇区' },
              { name: '长宁区' },
              { name: '静安区' },
              { name: '普陀区' },
              { name: '虹口区' },
              { name: '杨浦区' },
            ],
          },
        ],
      },
      {
        name: '广东省',
        children: [
          {
            name: '广州市',
            children: [
              { name: '越秀区' },
              { name: '海珠区' },
              { name: '荔湾区' },
              { name: '天河区' },
              { name: '白云区' },
            ],
          },
          {
            name: '深圳市',
            children: [
              { name: '罗湖区' },
              { name: '福田区' },
              { name: '南山区' },
              { name: '宝安区' },
              { name: '龙岗区' },
            ],
          },
        ],
      },
    ];
  };

  // 处理省市区选择变化
  const handleRegionChange = (value: string[]) => {
    if (value && value.length >= 1) {
      state.form.province = value[0] || '';
      state.form.city = value[1] || '';
      state.form.district = value[2] || '';
    } else {
      state.form.province = '';
      state.form.city = '';
      state.form.district = '';
    }
  };

  const reset = () => {
    state.title = t('biz.addProduct');
    state.form = {
      id: undefined,
      title: undefined,
      cpid: undefined,
      price: undefined,
      originalPrice: undefined,
      productType: 1,
      auditStatus: undefined,
      pinned: undefined,
      postType: 0,
      cover: undefined,
      description: undefined,
      address: undefined,
      province: undefined,
      city: undefined,
      district: undefined,
      urls: [],
      file: undefined,
      uid: undefined,
      author: undefined,
    };
    state.userOptions = [];
    state.selectedUser = null;
    fileList.value = [];
    imageList.value = [];
    regionValue.value = [];
  };

  // 用户搜索（参考新增笔记）
  const handleUserSearch = async (value: string) => {
    if (!value || value.trim().length === 0) {
      state.userOptions = [];
      return;
    }
    state.userSearching = true;
    try {
      const response = await getUserByKeyword(1, 10, value.trim());
      const userList: WebUserRecord[] = unwrapUserKeywordPage(response);
      state.userOptions = userList.map((user: WebUserRecord) => ({
        value: String(user.id),
        label: `${user.username} (ID: ${user.id})`,
        username: user.username,
        id: String(user.id),
        avatar: user.avatar,
      }));
    } catch (error) {
      state.userOptions = [];
      Message.error(t('biz.searchUserFailed'));
    } finally {
      state.userSearching = false;
    }
  };

  const handleUserChange = (value: string) => {
    if (!value) {
      state.selectedUser = null;
      state.form.uid = undefined;
      state.form.author = undefined;
      return;
    }
    const selectedOption = state.userOptions.find((opt) => opt.value === value);
    if (selectedOption) {
      state.selectedUser = {
        id: selectedOption.id,
        username: selectedOption.username,
        avatar: selectedOption.avatar,
      } as WebUserRecord;
      state.form.uid = selectedOption.id;
      state.form.author = selectedOption.username;
    }
  };

  const handleCancel = () => {
    emits('update:visible', false);
    emits('update:id', undefined);
    reset();
  };

  const formRef = ref<typeof Form>();
  
  // 提交商品的公共方法
  const submitProduct = (params: FormData) => {
    if (state.form.id !== undefined) {
      updateProduct(state.form.id, params as any).then(() => {
        // 判断是否需要重新审核
        const needReaudit = state.form.auditStatus === '0' || state.form.auditStatus === '2';
        if (needReaudit) {
          Message.success(t('biz.editSuccessReaudit'));
        } else {
          Message.success(t('biz.editSuccess'));
        }
        emits('update:visible', false);
        emits('update:id', undefined);
        emits('refreshDataList');
        reset();
      }).catch((err) => {
        Message.error(err.message || t('biz.editFailed'));
      });
    } else {
      createProduct(params as any).then(() => {
        Message.success(t('biz.addSuccess'));
        emits('update:visible', false);
        emits('update:id', undefined);
        emits('refreshDataList');
        reset();
      }).catch((err) => {
        Message.error(err.message || t('biz.addFailed'));
      });
    }
  };
  
  const handleConfirm = () => {
    formRef.value?.validate().then((error: any) => {
      if (!error) {
        // 构建 FormData，后端需要 product 和 file 参数
        const params = new FormData();
        
        // 构建商品数据对象（urls 和 count 会在后面根据新上传文件和已有图片重新计算）
        const productData: any = {
          ...state.form,
          uid: state.form.uid || state.selectedUser?.id,
          author: state.form.author || state.selectedUser?.username,
          urls: undefined, // 先清空，后面会根据实际情况设置
          count: undefined, // 先清空，后面会根据实际情况设置
        };
        
        // 如果是修改，添加 id
        if (state.form.id !== undefined) {
          productData.id = state.form.id;
        }
        
        // 将商品数据转为 JSON 字符串添加到 FormData
        params.append('product', JSON.stringify(productData));
        
        // 如果有新上传的封面文件，添加到 FormData
        let fileToUpload: File | null = null;
        
        // 优先使用 state.form.file
        if (state.form.file && state.form.file instanceof File) {
          fileToUpload = state.form.file;
        } else {
          // 尝试从 fileList 中获取文件对象
          for (let i = 0; i < fileList.value.length; i += 1) {
            const fileItem = fileList.value[i];
            // 如果 uid 不是 '-1'，说明是新上传的文件
            if (fileItem.uid !== '-1' && fileItem.originFile && fileItem.originFile instanceof File) {
              fileToUpload = fileItem.originFile;
              break;
            }
          }
        }
        
        if (fileToUpload) {
          params.append('file', fileToUpload);
        }
        
        // 管理后台的商品更新逻辑：参考笔记更新
        // 先上传新图片（base64）获取 URL，然后提交所有 URL
        const existingUrls = imageList.value
          .filter(item => item.url && !item.url.startsWith('data:'))
          .map(item => item.url);
        
        const newImageFiles = imageList.value
          .filter(item => item.url && item.url.startsWith('data:') && item.originFile && item.originFile instanceof File)
          .map(item => item.originFile);
        
        // ✅ 优化：如果有新图片需要上传，直接调用file模块上传获取URL（与笔记更新保持一致）
        if (newImageFiles.length > 0) {
          // 批量上传新图片（直接调用file模块）
          const uploadPromises = newImageFiles.map((file: File) => uploadFile(file));
          
          Promise.all(uploadPromises).then((responses) => {
            const newUrls = responses
              .map((res: any) => {
                // 处理file模块返回的数据结构：{ code, data: { url, name }, msg }
                if (res.code === 'success' || res.code === 200) {
                  if (typeof res.data === 'string') {
                    return res.data;
                  }
                  if (res.data && typeof res.data === 'object' && 'url' in res.data) {
                    return res.data.url;
                  }
                }
                return '';
              })
              .filter((url: string) => url);
            
            // 合并已有 URL 和新上传的 URL
            const allUrls = [...existingUrls, ...newUrls];
            
            // 更新 productData
            if (allUrls.length > 0) {
              productData.urls = JSON.stringify(allUrls);
              productData.count = allUrls.length;
            } else {
              productData.urls = undefined;
              productData.count = 0;
            }
            
            // 更新 params 中的 product 数据
            params.delete('product');
            params.append('product', JSON.stringify(productData));
            
            // 提交
            submitProduct(params);
          }).catch((err: any) => {
            Message.error(
              `${t('biz.imageUploadFailed')}: ${err?.message || t('biz.unknownError')}`
            );
          });
        } else {
          // 没有新图片，直接使用已有 URL
          if (existingUrls.length > 0) {
            productData.urls = JSON.stringify(existingUrls);
            productData.count = existingUrls.length;
          } else {
            productData.urls = undefined;
            productData.count = 0;
          }
          
          // 更新 params 中的 product 数据
          params.delete('product');
          params.append('product', JSON.stringify(productData));
          
          // 提交
          submitProduct(params);
        }
      }
    });
  };

  const getProductForm = async (id: string) => {
    try {
      const response = await getProduct(id);
      const data = (response as any).data || response;
      if (data) {
        // 显式赋值每个字段
        state.form.id = data.id;
        state.form.title = data.title;
        state.form.cpid = data.cpid !== undefined && data.cpid !== null ? String(data.cpid) : undefined;
        // 价格字段：后端返回的是字符串，需要转换为数字
        state.form.price = data.price !== undefined && data.price !== null && data.price !== '' 
          ? parseFloat(String(data.price)) 
          : undefined;
        state.form.originalPrice = data.originalPrice !== undefined && data.originalPrice !== null && data.originalPrice !== '' 
          ? parseFloat(String(data.originalPrice)) 
          : undefined;
        state.form.productType = data.productType !== undefined && data.productType !== null ? Number(data.productType) : 1;
        state.form.auditStatus = data.auditStatus !== undefined && data.auditStatus !== null ? String(data.auditStatus) : undefined;
        state.form.pinned = data.pinned !== undefined && data.pinned !== null ? String(data.pinned) : undefined;
        state.form.postType = data.postType !== undefined && data.postType !== null ? Number(data.postType) : 0;
        state.form.cover = data.cover;
        state.form.description = data.description;
        state.form.address = data.address;
        state.form.province = data.province || undefined;
        state.form.city = data.city || undefined;
        state.form.district = data.district || undefined;
        state.form.uid = data.uid ? String(data.uid) : undefined;
        state.form.author = data.author || undefined;

        if (data.uid && data.author) {
          state.selectedUser = {
            id: String(data.uid),
            username: data.author,
          } as WebUserRecord;
          state.userOptions = [
            {
              value: String(data.uid),
              label: `${data.author} (ID: ${data.uid})`,
              username: data.author,
              id: String(data.uid),
            },
          ];
        }
        
        // 设置封面
        if (data.cover) {
          fileList.value = [
            {
              uid: '-1',
              name: 'cover',
              url: data.cover,
              status: 'done',
            },
          ];
        }
        
        // 设置省市区级联选择器的值
        if (data.province) {
          const regionArr: string[] = [data.province];
          if (data.city) {
            regionArr.push(data.city);
            if (data.district) {
              regionArr.push(data.district);
            }
          }
          nextTick(() => {
            setTimeout(() => {
              regionValue.value = regionArr;
            }, 100);
          });
        } else {
          regionValue.value = [];
        }
        
        // 解析图片URLs
        if (data.urls) {
          try {
            const urlsArray = typeof data.urls === 'string' ? JSON.parse(data.urls) : data.urls;
            state.form.urls = Array.isArray(urlsArray) ? urlsArray : [];
            
            // 更新图片列表
            if (state.form.urls.length > 0) {
              imageList.value = state.form.urls.map((url: string, index: number) => ({
                uid: `-${index + 1}`,
                name: t('biz.imageNamed', { index: index + 1 }),
                status: 'done',
                url,
              }));
            } else {
              imageList.value = [];
            }
          } catch (e) {
            state.form.urls = [];
            imageList.value = [];
          }
        } else {
          state.form.urls = [];
          imageList.value = [];
        }
      }
    } catch (e: any) {
      Message.error(e.message || t('biz.getProductFailed'));
    }
  };

  // 封面上传处理
  const beforeUpload = (file: File) => {
    const isImage = file.type.startsWith('image/');
    if (!isImage) {
      Message.error(t('biz.imageOnly'));
      return false;
    }
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isLt2M) {
      Message.error(t('biz.imageSize2m'));
      return false;
    }
    
    state.form.file = file;
    
    const reader = new FileReader();
    reader.onload = (e) => {
      const previewUrl = e.target?.result as string;
      state.form.cover = previewUrl;
      
      const fileItem = {
        uid: Date.now().toString(),
        name: file.name,
        url: previewUrl,
        thumbUrl: previewUrl,
        status: 'done' as const,
        originFile: file,
      };
      fileList.value = [fileItem];
    };
    reader.readAsDataURL(file);
    
    return false;
  };

  const handleChange = (fileListParam: any, fileItem: any) => {
    if (Array.isArray(fileListParam)) {
      fileList.value = fileListParam;
      
      // 如果 fileList 为空，说明删除了文件，清空所有相关数据
      if (fileListParam.length === 0) {
        state.form.cover = undefined;
        state.form.file = undefined;
        return;
      }
    }
    
    if (state.form.file && state.form.file instanceof File) {
      if (fileList.value.length > 0 && !fileList.value[0].originFile) {
        fileList.value[0].originFile = state.form.file;
      }
      return;
    }
    
    let file: any = null;
    if (Array.isArray(fileListParam) && fileListParam.length > 0) {
      file = fileListParam[fileListParam.length - 1];
    } else if (fileListParam && !Array.isArray(fileListParam)) {
      file = fileListParam;
    } else if (fileItem) {
      file = fileItem;
    }
    
    if (file && file.originFile && file.originFile instanceof File) {
      state.form.file = file.originFile;
      
      if (!state.form.cover || state.form.cover.startsWith('data:')) {
        const reader = new FileReader();
        reader.onload = (e) => {
          const previewUrl = e.target?.result as string;
          state.form.cover = previewUrl;
          
          if (fileList.value.length > 0) {
            const updatedFileList = fileList.value.map((item: any, index: number) => {
              if (index === fileList.value.length - 1) {
                return {
                  ...item,
                  url: previewUrl,
                  thumbUrl: previewUrl,
                  status: 'done',
                  originFile: state.form.file,
                };
              }
              return item;
            });
            fileList.value = updatedFileList;
          }
        };
        reader.readAsDataURL(state.form.file);
      }
    } else if (file && file.url && !file.originFile) {
      state.form.cover = file.url;
    }
  };

  const handleRemove = () => {
    // 清空所有封面相关的数据
    state.form.cover = undefined;
    state.form.file = undefined;
    fileList.value = [];
    return true;
  };

  // 多图片/视频上传处理
  const beforeImageUpload = (file: File) => {
    const isVideoType = state.form.productType === 2;
    
    if (isVideoType) {
      const isVideo = file.type.startsWith('video/');
      if (!isVideo) {
        Message.error(t('biz.videoOnly'));
        return false;
      }
      const isLt50M = file.size / 1024 / 1024 < 50;
      if (!isLt50M) {
        Message.error(t('biz.videoSize50m'));
        return false;
      }
      
      imageList.value = [];
      
      const reader = new FileReader();
      reader.onload = (e) => {
        const previewUrl = e.target?.result as string;
        const fileItem = {
          uid: Date.now().toString() + Math.random(),
          name: file.name,
          url: previewUrl,
          thumbUrl: previewUrl,
          status: 'done' as const,
          originFile: file,
        };
        imageList.value = [fileItem];
        state.form.urls = [previewUrl];
      };
      reader.readAsDataURL(file);
    } else {
      const isImage = file.type.startsWith('image/');
      if (!isImage) {
        Message.error(t('biz.imageOnlyMulti'));
        return false;
      }
      const isLt5M = file.size / 1024 / 1024 < 5;
      if (!isLt5M) {
        Message.error(t('biz.imageSize5m'));
        return false;
      }
      
      const reader = new FileReader();
      reader.onload = (e) => {
        const previewUrl = e.target?.result as string;
        const fileItem = {
          uid: Date.now().toString() + Math.random(),
          name: file.name,
          url: previewUrl,
          thumbUrl: previewUrl,
          status: 'done' as const,
          originFile: file,
        };
        imageList.value.push(fileItem);
        
        // 只保留非 base64 的 URL（已上传的图片），不包含 base64 预览数据
        state.form.urls = imageList.value
          .filter(item => item.url && !item.url.startsWith('data:'))
          .map(item => item.url);
      };
      reader.readAsDataURL(file);
    }
    
    return false;
  };

  const handleImageChange = (fileListParam: any) => {
    if (Array.isArray(fileListParam)) {
      imageList.value = fileListParam;
      
      state.form.urls = imageList.value
        .filter(item => item.url && !item.url.startsWith('data:'))
        .map(item => item.url);
    }
  };

  const handleImageRemove = (fileItem: any) => {
    imageList.value = imageList.value.filter(item => item.uid !== fileItem.uid);
    
    state.form.urls = imageList.value
      .filter(item => item.url && !item.url.startsWith('data:'))
      .map(item => item.url);
    
    return true;
  };

  watch(
    () => props.visible,
    (value) => {
      if (value) {
        if (props.id) {
          state.title = t('biz.editProduct');
          getProductForm(props.id);
        } else {
          reset();
        }
      } else {
        reset();
      }
    }
  );

  watch(
    () => props.id,
    (value) => {
      if (value && props.visible) {
        state.title = t('biz.editProduct');
        getProductForm(value);
      }
    }
  );

  // 初始化
  initDictData();
  fetchTreeSelectOption();
  initRegionOptions();
</script>

<style scoped lang="less">
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.cover-uploader {
  :deep(.arco-upload-list-item) {
    width: 120px;
    height: 120px;
  }
  
  :deep(.arco-upload-list-picture-card .arco-upload-list-item-picture img) {
    object-fit: cover;
  }
}
</style>

