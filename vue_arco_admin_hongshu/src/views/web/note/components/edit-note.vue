<template>
  <a-modal
    :visible="open"
    :title="state.title"
    width="1000px"
    :body-style="{ maxHeight: '70vh', overflowY: 'auto', padding: '20px' }"
    :loading="state.submitting"
    :ok-loading="state.submitting"
    :ok-button-props="{ disabled: state.submitting }"
    :cancel-button-props="{ disabled: state.submitting }"
    @cancel="handleCancel"
    @ok="handleConfirm"
  >
    <a-form
      ref="formRef"
      :model="state.form"
      :rules="state.rules"
      auto-label-width
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 20 }"
      :disabled="state.submitting"
    >
      <a-row :gutter="16">
        <!-- 基本信息 -->
        <a-col :span="12">
          <a-form-item :label="$t('biz.noteTitle')" field="title">
            <a-input
              v-model="state.form.title"
              :placeholder="$t('biz.pleaseInputNoteTitle')"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="$t('biz.userFieldLabel')" field="userId" required>
            <a-select
              v-model="state.form.userId"
              :loading="state.userSearching"
              :placeholder="$t('biz.userSearchNotePlaceholder')"
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
          <a-form-item :label="$t('biz.category')" field="cpid" required>
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
        
        <!-- 状态信息 -->
        <a-col :span="12">
          <a-form-item :label="$t('biz.noteType')" field="noteType">
            <a-radio-group v-model="state.form.noteType">
              <a-radio
                v-for="dict in state.noteTypeOptions"
                :key="dict.dictValue"
                :value="dict.dictValue"
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
          <a-form-item :label="$t('biz.columnNoteSource')" field="fromType">
            <a-radio-group v-model="state.form.fromType" disabled>
              <a-radio
                v-for="dict in state.fromTypeOptions"
                :key="dict.dictValue"
                :value="dict.dictValue"
              >
                {{ dict.dictLabel }}
              </a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        
        <!-- 标签 -->
        <a-col :span="24">
          <a-form-item :label="$t('biz.tag')" field="tags">
            <a-select
              v-model="state.form.tags"
              multiple
              :placeholder="$t('biz.tagSelectOrInput')"
              allow-search
              allow-clear
              allow-create
            >
              <a-option
                v-for="tag in state.tagOptions"
                :key="tag.id"
                :label="tag.title"
                :value="tag.title"
              >
                {{ tag.title }}
              </a-option>
            </a-select>
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
          <a-form-item :label="$t('biz.publishLocation')" field="address">
            <a-input
              v-model="state.form.address"
              :placeholder="$t('biz.pleaseInputDetailAddress')"
            />
          </a-form-item>
        </a-col>
        
        <!-- 笔记封面 -->
        <a-col :span="24">
          <a-form-item :label="$t('biz.columnNoteCover')" field="noteCover">
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
                <div v-if="!state.form.noteCover" class="upload-placeholder">
                  <icon-plus style="font-size: 24px; color: #8c939d" />
                  <div style="font-size: 12px; margin-top: 8px;">{{ $t('biz.uploadCover') }}</div>
                </div>
                <template v-else>
                  <a-image
                    :src="getNginxProxyImageUrl(state.form.noteCover)"
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
        
        <!-- 笔记图片/视频 -->
        <a-col :span="24">
          <a-form-item
            :label="state.form.noteType === '2' ? $t('biz.noteVideoField') : $t('biz.noteImagesField')"
            field="urls"
          >
            <!-- 视频类型 -->
            <div v-if="state.form.noteType === '2'">
              <!-- 视频预览 -->
              <div v-if="imageList.length > 0" style="display: flex; align-items: flex-end; gap: 16px; margin-bottom: 16px;">
                <video
                  v-for="item in imageList"
                  :key="item.uid"
                  :src="getNginxProxyVideoUrl(item.url)"
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
        
        <!-- 笔记内容 -->
        <a-col :span="24">
          <a-form-item :label="$t('biz.noteBodyHeading')" field="content">
            <a-textarea
              v-model="state.form.content"
              :auto-size="{ minRows: 6, maxRows: 12 }"
              :placeholder="$t('biz.pleaseInputNoteContent')"
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
  import { getNote, updateNote, addNote } from '@/api/web/note';
  import { listNavbar } from '@/api/web/navbar';
  import { getDicts } from '@/api/system/dict-data';
  import { tagList } from '@/api/web/tag';
  import {
    downloadXiaohongshuVideo,
    downloadXiaohongshuImage,
  } from '@/api/web/xiaohongshu';
  import {
    getUserByKeyword,
    unwrapUserKeywordPage,
    type WebUserRecord,
  } from '@/api/web/user';

  const { t } = useI18n();

  // Nginx代理函数：将小红书图片URL转换为Nginx代理路径
  const getNginxProxyImageUrl = (imageUrl: string | undefined): string => {
    if (!imageUrl || !imageUrl.trim()) {
      return '';
    }
    
    // 如果已经是OSS URL（包含 OSS 域名），直接返回
    if (imageUrl.includes('oss') || imageUrl.includes('aliyuncs.com') || imageUrl.includes('qcloud.com')) {
      return imageUrl;
    }
    
    // 如果是小红书图片URL，转换为Nginx代理路径
    if (imageUrl.includes('sns-webpic-qc.xhscdn.com') || 
        imageUrl.includes('xhscdn.com') ||
        imageUrl.includes('xiaohongshu.com')) {
      try {
        const url = new URL(imageUrl);
        // 提取路径部分，转换为 /xhspic/ 开头的路径
        return `/xhspic${url.pathname}${url.search}`;
      } catch (e) {
        // 如果URL解析失败，尝试简单替换
        const match = imageUrl.match(/https?:\/\/[^/]+(\/.*)/);
        if (match) {
          return `/xhspic${match[1]}`;
        }
        // 如果都失败了，返回原始URL
        return imageUrl;
      }
    }
    
    // 非小红书图片，直接返回原始URL
    return imageUrl;
  };

  // Nginx代理函数：将小红书视频URL转换为Nginx代理路径
  const getNginxProxyVideoUrl = (videoUrl: string | undefined): string => {
    if (!videoUrl || !videoUrl.trim()) {
      return '';
    }
    
    // 如果已经是OSS URL（包含 OSS 域名），直接返回
    if (videoUrl.includes('oss') || videoUrl.includes('aliyuncs.com') || videoUrl.includes('qcloud.com')) {
      return videoUrl;
    }
    
    // 如果是小红书视频URL，转换为Nginx代理路径
    if (videoUrl.includes('sns-video-bd.xhscdn.com') || 
        videoUrl.includes('xhscdn.com') ||
        videoUrl.includes('xiaohongshu.com')) {
      try {
        const url = new URL(videoUrl);
        // 提取路径部分，转换为 /xhsvideo/ 开头的路径
        return `/xhsvideo${url.pathname}${url.search}`;
      } catch (e) {
        // 如果URL解析失败，尝试简单替换
        const match = videoUrl.match(/https?:\/\/[^/]+(\/.*)/);
        if (match) {
          return `/xhsvideo${match[1]}`;
        }
        // 如果都失败了，返回原始URL
        return videoUrl;
      }
    }
    
    // 非小红书视频，直接返回原始URL
    return videoUrl;
  };

  // 判断是否是OSS URL
  const isOssUrl = (url: string | undefined): boolean => {
    if (!url) return false;
    // 检查是否是OSS URL（通常包含OSS域名或特定路径）
    return !url.includes('xhscdn.com') && !url.includes('xiaohongshu.com');
  };

  // 判断是否是小红书URL
  const isXiaohongshuUrl = (url: string | undefined): boolean => {
    if (!url) return false;
    return url.includes('xhscdn.com') || url.includes('xiaohongshu.com');
  };

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    id: {
      type: Number,
      default: undefined,
    },
    initialData: {
      type: Object,
      default: undefined,
    },
  });

  const open = computed(() => {
    return !!props.visible;
  });

  const fileList = ref<any[]>([]);
  // 多图片列表
  const imageList = ref<any[]>([]);
  // 省市区级联选择器的值
  const regionValue = ref<string[]>([]);

  const state = reactive({
    title: t('biz.addNoteDialogTitle'),
    submitting: false, // 提交中状态
    form: {
      id: undefined,
      title: undefined,
      cpid: undefined,
      noteType: undefined,
      auditStatus: '1', // 默认已通过
      pinned: undefined,
      fromType: '2', // 默认管理端
      noteCover: undefined,
      content: undefined,
      address: undefined,
      province: undefined,
      city: undefined,
      district: undefined,
      tags: [] as string[],
      urls: [] as string[],
      file: undefined as File | undefined, // 用户选择的文件
      userId: undefined as string | undefined, // 用户ID
      author: undefined as string | undefined, // 作者名称
    },
    // 用户搜索相关
    userOptions: [] as Array<{ value: string; label: string; username: string; id: string; avatar?: string }>, // 用户选项列表
    userSearching: false, // 用户搜索中状态
    selectedUser: null as WebUserRecord | null, // 选中的用户
    rules: {
      title: [{ required: true, message: t('biz.pleaseInputNoteTitle'), trigger: 'blur' }],
      userId: [{ required: true, message: t('biz.pleaseSelectNoteUser'), trigger: 'change' }],
      cpid: [{ required: true, message: t('biz.pleaseSelectNoteCategory'), trigger: 'change' }],
    },
    navbarOptions: [] as Array<any>,
    noteTypeOptions: [] as Array<any>,
    auditStatusOptions: [] as Array<any>,
    pinnedOptions: [] as Array<any>,
    fromTypeOptions: [] as Array<any>,
    tagOptions: [] as Array<any>, // 标签选项列表
    regionOptions: [] as Array<any>, // 省市区选项列表
  });

  // 初始化字典数据
  const initDictData = async () => {
    function parseToArray(input: any) {
      if (Array.isArray(input)) return input;
      if (typeof input === 'string') {
        const str = input.trim();
        try {
          const parsed = JSON.parse(str);
          if (Array.isArray(parsed)) return parsed;
        } catch (_) {
          const parts = str.split(/[,\n]\s*/).filter(Boolean);
          if (parts.length) {
            return parts.map((p) => {
              const seg = p.split(/[:|，：]/);
              const value = (seg[0] ?? '').trim();
              const label = (seg[1] ?? value).trim();
              return { dictValue: value, dictLabel: label } as any;
            });
          }
        }
      }
      return [];
    }

    async function safeGetDict(key: string) {
      try {
        const res: any = await getDicts(key);
        const data = res && (res.data !== undefined ? res.data : res);
        return parseToArray(data);
      } catch (e) {
        console.warn(`[dict] load failed: ${key}`, e);
        return [];
      }
    }

    try {
      const [noteTypeRes, auditStatusRes, pinnedRes] = await Promise.all([
        safeGetDict('note_type'),
        safeGetDict('audit_status'),
        safeGetDict('sys_normal_disable'),
      ]);

      // 定义笔记来源字典（0代表web端，1代表app端，2代表管理端）
      const fromType = [
        { dictValue: '0', dictLabel: t('biz.fromWeb') },
        { dictValue: '1', dictLabel: t('biz.fromApp') },
        { dictValue: '2', dictLabel: t('biz.fromAdmin') },
      ];

      // 定义置顶字典（0代表否，1代表是）
      const pinned = [
        { dictValue: '0', dictLabel: t('biz.no') },
        { dictValue: '1', dictLabel: t('biz.yes') },
      ];

      state.noteTypeOptions = noteTypeRes;
      state.auditStatusOptions = auditStatusRes;
      state.pinnedOptions = pinned;
      state.fromTypeOptions = fromType;
    } catch (e) {
      console.warn('字典数据加载失败', e);
    }
  };

  // 数组转树结构
  const listToTree: any = (list: Array<any>) => {
    const map: any = {};
    const treeData: Array<any> = [];
    list = list.map((item) => {
      return {
        id: item.id,
        pid: item.pid,
        title: item.title,
      };
    });
    for (let i = 0; i < list.length; i += 1) {
      map[list[i].id] = i;
      list[i].children = [];
    }
    for (let i = 0; i < list.length; i += 1) {
      const node = list[i];
      if (node.pid && list[map[node.pid]]) {
        list[map[node.pid]].children.push(node);
      } else {
        treeData.push(node);
      }
    }
    return treeData;
  };

  // 查询导航栏下拉树结构
  const fetchTreeSelectOption = async () => {
    try {
      const { data } = await listNavbar({});
      state.navbarOptions = listToTree(data);
    } catch (e) {
      console.warn('导航栏数据加载失败', e);
    }
  };

  // 加载标签列表
  const loadTagOptions = async () => {
    try {
      const { data } = await tagList({});
      state.tagOptions = data || [];
    } catch (e) {
      console.warn('标签列表加载失败', e);
      state.tagOptions = [];
    }
  };

  // 初始化省市区数据（简化版本，包含主要省市）
  const initRegionOptions = () => {
    state.regionOptions = [
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
              { name: '长清区' },
              { name: '章丘区' },
              { name: '济阳区' },
              { name: '莱芜区' },
              { name: '钢城区' },
              { name: '平阴县' },
              { name: '商河县' },
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
              { name: '胶州市' },
              { name: '平度市' },
              { name: '莱西市' },
            ],
          },
          {
            name: '淄博市',
            children: [
              { name: '淄川区' },
              { name: '张店区' },
              { name: '博山区' },
              { name: '临淄区' },
              { name: '周村区' },
              { name: '桓台县' },
              { name: '高青县' },
              { name: '沂源县' },
            ],
          },
          { name: '枣庄市' },
          { name: '东营市' },
          { name: '烟台市' },
          { name: '潍坊市' },
          { name: '济宁市' },
          { name: '泰安市' },
          { name: '威海市' },
          { name: '日照市' },
          { name: '临沂市' },
          { name: '德州市' },
          { name: '聊城市' },
          { name: '滨州市' },
          { name: '菏泽市' },
        ],
      },
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
          { name: '闵行区' },
          { name: '宝山区' },
          { name: '嘉定区' },
          { name: '浦东新区' },
          { name: '金山区' },
          { name: '松江区' },
          { name: '青浦区' },
          { name: '奉贤区' },
          { name: '崇明区' },
        ],
      },
      {
        name: '广东省',
        children: [
          { name: '广州市' },
          { name: '深圳市' },
          { name: '珠海市' },
          { name: '汕头市' },
          { name: '佛山市' },
          { name: '韶关市' },
          { name: '湛江市' },
          { name: '肇庆市' },
          { name: '江门市' },
          { name: '茂名市' },
          { name: '惠州市' },
          { name: '梅州市' },
          { name: '汕尾市' },
          { name: '河源市' },
          { name: '阳江市' },
          { name: '清远市' },
          { name: '东莞市' },
          { name: '中山市' },
          { name: '潮州市' },
          { name: '揭阳市' },
          { name: '云浮市' },
        ],
      },
      {
        name: '浙江省',
        children: [
          { name: '杭州市' },
          { name: '宁波市' },
          { name: '温州市' },
          { name: '嘉兴市' },
          { name: '湖州市' },
          { name: '绍兴市' },
          { name: '金华市' },
          { name: '衢州市' },
          { name: '舟山市' },
          { name: '台州市' },
          { name: '丽水市' },
        ],
      },
      {
        name: '江苏省',
        children: [
          { name: '南京市' },
          { name: '无锡市' },
          { name: '徐州市' },
          { name: '常州市' },
          { name: '苏州市' },
          { name: '南通市' },
          { name: '连云港市' },
          { name: '淮安市' },
          { name: '盐城市' },
          { name: '扬州市' },
          { name: '镇江市' },
          { name: '泰州市' },
          { name: '宿迁市' },
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

  // 自定义级联选择器的显示文本（显示完整路径）
  // Arco Design 的 cascader 使用 formatLabel，参数是选中的选项数组
  const formatRegionLabel = (options: any[]) => {
    if (options && Array.isArray(options) && options.length > 0) {
      // 从选项中提取 label（name）字段
      return options.map((option: any) => option.name || option.label || option).join(' / ');
    }
    return '';
  };

  // 使用初始数据填充表单
  const fillFormWithInitialData = (data: any) => {
    console.log('fillFormWithInitialData 被调用，数据:', data);
    if (data.title) {
      state.form.title = data.title as string;
      console.log('设置标题:', data.title);
    }
    if (data.content) {
      state.form.content = data.content as string;
      console.log('设置内容:', data.content);
    }
    if (data.noteCover) {
      state.form.noteCover = data.noteCover as string;
      console.log('设置封面:', data.noteCover);
    }
    if (data.noteType !== undefined) {
      state.form.noteType = data.noteType as string;
      console.log('设置笔记类型:', data.noteType);
    }
    if (data.urls && Array.isArray(data.urls)) {
      state.form.urls = data.urls as string[];
      console.log('设置URLs:', data.urls);
      // 设置图片列表（使用nginx代理URL显示）
      imageList.value = data.urls.map((url: string, index: number) => {
        // 判断是视频还是图片
        const isVideo = state.form.noteType === '2';
        // 如果是base64或OSS URL，直接使用；否则使用nginx代理
        let displayUrl = url;
        if (!url.startsWith('data:') && !url.includes('oss') && !url.includes('aliyuncs.com') && !url.includes('qcloud.com')) {
          displayUrl = isVideo ? getNginxProxyVideoUrl(url) : getNginxProxyImageUrl(url);
        }
        return {
          uid: `-${index}`,
          name: isVideo
            ? t('biz.videoItemDisplayName', { index: index + 1 })
            : t('biz.imageItemDisplayName', { index: index + 1 }),
          status: 'done',
          url: displayUrl, // 显示时使用代理URL
          originalUrl: url, // 保存原始URL用于提交
        };
      });
      console.log('设置图片列表:', imageList.value);
    }
    if (data.tags && Array.isArray(data.tags)) {
      state.form.tags = data.tags as string[];
    }
    if (data.cpid !== undefined) state.form.cpid = data.cpid as number;
    // 如果未指定 fromType，默认设置为管理端（'2'）
    if (data.fromType !== undefined) {
      state.form.fromType = String(data.fromType);
      console.log('设置来源类型:', data.fromType);
    } else {
      state.form.fromType = '2'; // 默认管理端
    }
    // 如果未指定 auditStatus，默认设置为已通过（'1'）
    if (data.auditStatus !== undefined) {
      state.form.auditStatus = String(data.auditStatus);
      console.log('设置审核状态:', data.auditStatus);
    } else {
      state.form.auditStatus = '1'; // 默认已通过
    }
    // 设置封面（使用nginx代理URL显示）
    if (data.noteCover) {
      const coverDisplayUrl = data.noteCover.startsWith('data:') || data.noteCover.includes('oss') || data.noteCover.includes('aliyuncs.com') || data.noteCover.includes('qcloud.com')
        ? data.noteCover
        : getNginxProxyImageUrl(data.noteCover);
      fileList.value = [{
        uid: '-1',
        name: t('biz.coverFileDisplayName'),
        status: 'done',
        url: coverDisplayUrl, // 显示时使用代理URL
        originalUrl: data.noteCover, // 保存原始URL用于提交
      }];
      console.log('设置封面列表:', fileList.value);
    }
    console.log('表单数据填充完成，当前表单:', state.form);
  };

  const reset = () => {
    state.title = t('biz.addNoteDialogTitle');
    state.submitting = false; // 重置提交状态
    state.form = {
      id: undefined,
      title: undefined,
      cpid: undefined,
      noteType: undefined,
      auditStatus: '1', // 默认已通过
      pinned: undefined,
      fromType: '2', // 默认管理端
      noteCover: undefined,
      content: undefined,
      address: undefined,
      province: undefined,
      city: undefined,
      district: undefined,
      tags: [],
      urls: [],
      file: undefined,
      userId: undefined,
      author: undefined,
    };
    // 重置用户相关状态
    state.userOptions = [];
    state.selectedUser = null;
    fileList.value = [];
    imageList.value = [];
    regionValue.value = [];
  };

  const emits = defineEmits([
    'update:visible',
    'update:id',
    'refreshDataList',
  ]);

  // 用户搜索
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
      console.error('搜索用户失败:', error);
      state.userOptions = [];
      Message.error(t('biz.searchUserFailed'));
    } finally {
      state.userSearching = false;
    }
  };

  // 用户选择变化
  const handleUserChange = (value: string) => {
    if (!value) {
      state.selectedUser = null;
      state.form.userId = undefined;
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
      state.form.userId = selectedOption.id;
      state.form.author = selectedOption.username;
    }
  };

  const handleCancel = () => {
    emits('update:visible', false);
    emits('update:id', undefined);
    reset();
  };

  const formRef = ref<typeof Form>();
  const handleConfirm = async () => {
    const validationResult = await formRef.value?.validate();
    if (validationResult) {
      return;
    }

    // 设置提交中状态，禁用表单和按钮
    state.submitting = true;

    try {
      // 检查并下载小红书资源
      let finalUrls = state.form.urls || [];
      let finalNoteCover = state.form.noteCover;

      // 检查是否是视频类型
      if (state.form.noteType === '2' && finalUrls.length > 0) {
        const videoUrl = finalUrls[0];
        if (isXiaohongshuUrl(videoUrl) && !isOssUrl(videoUrl)) {
          // 需要下载视频
          const loadingMessage = Message.loading({
            content: t('biz.downloadingVideoToOss'),
            duration: 0, // 不自动关闭
          });
          try {
            const response = await downloadXiaohongshuVideo(videoUrl);
            if (response.code === 'success' || response.code === 200) {
              const responseData = response.data;
              let ossUrl = '';
              if (typeof responseData === 'string') {
                ossUrl = responseData;
              } else if (
                responseData &&
                typeof responseData === 'object' &&
                'url' in responseData
              ) {
                ossUrl = (responseData as any).url as string;
              } else if (
                responseData &&
                typeof responseData === 'object' &&
                'data' in responseData
              ) {
                ossUrl = (responseData as any).data as string;
              } else {
                throw new Error(t('biz.downloadResponseBad'));
              }
              finalUrls = [ossUrl];
              // 注意：视频类型的封面是单独的图片，不是视频URL，所以不修改 finalNoteCover
              // 封面图片会在后面的逻辑中单独下载
              loadingMessage.close();
              Message.success(t('biz.videoDownloadToOssOk'));
            } else {
              loadingMessage.close();
              const errorMsg = (response as any).message || t('biz.downloadFailedShort');
              throw new Error(errorMsg);
            }
          } catch (error: any) {
            loadingMessage.close();
            console.error('下载视频失败:', error);
            Message.error(
              t('biz.videoDownloadFailSave', {
                msg: error.message || t('biz.unknownError'),
              })
            );
            state.submitting = false;
            return;
          }
        }
      } else if (finalUrls.length > 0) {
        // 图片类型：检查并下载所有小红书图片
        const urlsToDownload = finalUrls.filter((url: string) => 
          isXiaohongshuUrl(url) && !isOssUrl(url)
        );

        if (urlsToDownload.length > 0) {
          const loadingMessage = Message.loading({
            content: t('biz.downloadingNImagesToOss', {
              count: urlsToDownload.length,
            }),
            duration: 0, // 不自动关闭
          });
          
          try {
            const downloadPromises = finalUrls.map(async (url: string, index: number) => {
              // 如果已经是OSS URL，直接返回
              if (isOssUrl(url)) {
                return { success: true, url, index };
              }
              // 如果是小红书URL，需要下载
              if (isXiaohongshuUrl(url)) {
                try {
                  const response = await downloadXiaohongshuImage(url);
                  if (response.code === 'success' || response.code === 200) {
                    const responseData = response.data;
                    let ossUrl = '';
                    if (typeof responseData === 'string') {
                      ossUrl = responseData;
                    } else if (
                      responseData &&
                      typeof responseData === 'object' &&
                      'url' in responseData
                    ) {
                      ossUrl = (responseData as any).url as string;
                    } else if (
                      responseData &&
                      typeof responseData === 'object' &&
                      'data' in responseData
                    ) {
                      ossUrl = (responseData as any).data as string;
                    } else {
                      throw new Error(t('biz.downloadResponseBad'));
                    }
                    return { success: true, url: ossUrl, index };
                  }
                  const errorMsg = (response as any).message || t('biz.downloadFailedShort');
                  throw new Error(errorMsg);
                } catch (error: any) {
                  console.error(`图片 ${index + 1} 下载失败:`, error);
                  return {
                    success: false,
                    url: null,
                    index,
                    error: error.message || t('biz.downloadFailedShort'),
                  };
                }
              }
              // 既不是OSS也不是小红书URL，直接返回
              return { success: true, url, index };
            });

            const results = await Promise.all(downloadPromises);
            const successful = results.filter((r) => r.success && r.url);
            const failed = results.filter((r) => !r.success);

            if (successful.length === 0) {
              throw new Error(t('biz.allImagesDownloadFailSave'));
            }

            if (failed.length > 0) {
              Message.warning(
                t('biz.partialImagesDownloadWarn', {
                  failed: failed.length,
                  total: finalUrls.length,
                })
              );
            }

            finalUrls = successful
              .sort((a, b) => a.index - b.index)
              .map((r) => r.url as string);

            // 更新封面为第一张下载后的图片
            if (finalUrls.length > 0 && isXiaohongshuUrl(finalNoteCover || '')) {
              const [firstImageUrl] = finalUrls;
              if (firstImageUrl) {
                finalNoteCover = firstImageUrl;
              }
            }

            loadingMessage.close();
            Message.success(
              t('biz.imagesDownloadToOssOk', { count: successful.length })
            );
          } catch (error: any) {
            loadingMessage.close();
            console.error('下载图片失败:', error);
            Message.error(
              t('biz.imagesDownloadFailSave', {
                msg: error.message || t('biz.unknownError'),
              })
            );
            state.submitting = false;
            return;
          }
        }
      }

      // 检查封面是否需要下载
      if (finalNoteCover && isXiaohongshuUrl(finalNoteCover) && !isOssUrl(finalNoteCover)) {
        const loadingMessage = Message.loading({
          content: t('biz.downloadingCoverToOss'),
          duration: 0, // 不自动关闭
        });
        try {
          const response = await downloadXiaohongshuImage(finalNoteCover);
          if (response.code === 'success' || response.code === 200) {
            const responseData = response.data;
            if (typeof responseData === 'string') {
              finalNoteCover = responseData;
            } else if (
              responseData &&
              typeof responseData === 'object' &&
              'url' in responseData
            ) {
              finalNoteCover = (responseData as any).url as string;
            } else if (
              responseData &&
              typeof responseData === 'object' &&
              'data' in responseData
            ) {
              finalNoteCover = (responseData as any).data as string;
            }
            loadingMessage.close();
            Message.success(t('biz.coverDownloadToOssOk'));
          } else {
            loadingMessage.close();
            const errorMsg = (response as any).message || t('biz.downloadFailedShort');
            throw new Error(errorMsg);
          }
        } catch (error: any) {
          loadingMessage.close();
          console.error('下载封面失败:', error);
          Message.warning(
            t('biz.coverDownloadWarnUseOriginal', {
              msg: error.message || t('biz.unknownError'),
            })
          );
        }
      }

      // 构建 FormData，后端需要 note 和 file 参数
      const params = new FormData();
      
      // 构建笔记数据对象（使用下载后的URL）
      const noteData: any = {
        title: state.form.title,
        cpid: state.form.cpid,
        noteType: state.form.noteType,
        auditStatus: state.form.auditStatus,
        pinned: state.form.pinned,
        fromType: state.form.fromType,
        noteCover: finalNoteCover,
        content: state.form.content,
        address: state.form.address,
        province: state.form.province,
        city: state.form.city,
        district: state.form.district,
        // 使用选择的用户信息，如果没有选择则使用默认值
        uid: state.form.userId || state.selectedUser?.id || '29', // 用户ID
        author: state.form.author || state.selectedUser?.username || t('biz.avatar'), // 作者名称
        // 将 tags 数组转换为 JSON 字符串
        tags: state.form.tags && state.form.tags.length > 0 
          ? JSON.stringify(state.form.tags) 
          : undefined,
        // 将 urls 数组转换为 JSON 字符串（使用下载后的URL）
        urls: finalUrls.length > 0 
          ? JSON.stringify(finalUrls) 
          : undefined,
        // 计算图片数量
        count: finalUrls.length > 0 
          ? finalUrls.length 
          : 0,
      };
      
      // 如果是修改，添加 id
      if (state.form.id !== undefined) {
        noteData.id = state.form.id;
      }
      
      // 将笔记数据转为 JSON 字符串添加到 FormData
      params.append('note', JSON.stringify(noteData));
      
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
      
      if (state.form.id !== undefined) {
        updateNote(params as any).then(() => {
          // 判断是否需要重新审核
          const needReaudit = state.form.auditStatus === '0' || state.form.auditStatus === '2';
          if (needReaudit) {
            Message.success(t('biz.editSuccessNoteReaudit'));
          } else {
            Message.success(t('biz.editSuccess'));
          }
          state.submitting = false;
          emits('update:visible', false);
          emits('update:id', undefined);
          emits('refreshDataList');
          reset();
        }).catch((err) => {
          state.submitting = false;
          Message.error(err.message || t('biz.updateNoteFail'));
        });
      } else {
        addNote(params as any).then(() => {
          Message.success(t('biz.addSuccess'));
          state.submitting = false;
          emits('update:visible', false);
          emits('update:id', undefined);
          emits('refreshDataList');
          reset();
        }).catch((err) => {
          state.submitting = false;
          Message.error(err.message || t('biz.createNoteFail'));
        });
      }
    } catch (error: any) {
      console.error('保存失败:', error);
      state.submitting = false;
      Message.error(error.message || t('biz.saveNoteFail'));
    }
  };

  const getNoteForm = async (id: number) => {
    try {
      const { data, code, msg } = await getNote(id);
      if (code && code === 200) {
        // 确保字典值类型匹配（转换为字符串，因为字典选项的 dictValue 可能是字符串）
        // 封面字段优先使用 noteCover，如果没有则尝试其他字段
        let coverUrl = data.noteCover;
        if (!coverUrl) {
          // 尝试从其他字段获取封面
          coverUrl = data.cover || data.thumbnail;
          if (!coverUrl && data.urls) {
            try {
              const urls = typeof data.urls === 'string' ? JSON.parse(data.urls) : data.urls;
              if (Array.isArray(urls) && urls.length > 0) {
                const [firstUrl] = urls;
                coverUrl = firstUrl;
              }
            } catch (e) {
              // 解析失败，忽略
            }
          }
        }
        
        // 保存当前的文件对象（如果有新上传的文件）
        const currentFile = state.form.file;
        
        // 直接赋值所有字段，确保响应式更新
        state.form.id = data.id;
        state.form.title = data.title;
        state.form.cpid = data.cpid;
        state.form.noteType = data.noteType !== undefined && data.noteType !== null ? String(data.noteType) : undefined;
        state.form.auditStatus = data.auditStatus !== undefined && data.auditStatus !== null ? String(data.auditStatus) : undefined;
        state.form.pinned = data.pinned !== undefined && data.pinned !== null ? String(data.pinned) : undefined;
        state.form.fromType = data.fromType !== undefined && data.fromType !== null ? String(data.fromType) : undefined;
        state.form.noteCover = coverUrl || undefined;
        state.form.content = data.content;
        state.form.address = data.address;
        state.form.province = data.province || undefined;
        state.form.city = data.city || undefined;
        state.form.district = data.district || undefined;
        
        // 加载用户信息（如果存在）
        if (data.uid) {
          state.form.userId = String(data.uid);
          state.form.author = data.author || '';
          // 设置选中的用户信息
          if (data.author) {
            state.selectedUser = {
              id: String(data.uid),
              username: data.author,
            } as WebUserRecord;
            // 将当前用户添加到选项列表中，以便下拉框能正确显示
            state.userOptions = [{
              value: String(data.uid),
              label: `${data.author} (ID: ${data.uid})`,
              username: data.author,
              id: String(data.uid),
            }];
          }
        }
        
        // 设置省市区级联选择器的值
        // 使用 nextTick 确保级联选择器数据已加载
        if (data.province) {
          const regionArr: string[] = [data.province];
          if (data.city) {
            regionArr.push(data.city);
            if (data.district) {
              regionArr.push(data.district);
            }
          }
          // 使用 nextTick 确保级联选择器组件已渲染
          // 需要延迟更长时间，确保级联选择器数据已完全加载
          nextTick(() => {
            setTimeout(() => {
              regionValue.value = regionArr;
            }, 100);
          });
        } else {
          regionValue.value = [];
        }
        
        // 解析标签（tags是JSON字符串数组）
        if (data.tags) {
          try {
            const tagsArray = typeof data.tags === 'string' ? JSON.parse(data.tags) : data.tags;
            state.form.tags = Array.isArray(tagsArray) ? tagsArray : [];
          } catch (e) {
            state.form.tags = [];
          }
        } else {
          state.form.tags = [];
        }
        
        // 解析图片URLs（urls是JSON字符串数组）
        if (data.urls) {
          try {
            const urlsArray = typeof data.urls === 'string' ? JSON.parse(data.urls) : data.urls;
            state.form.urls = Array.isArray(urlsArray) ? urlsArray : [];
            
            // 更新图片列表（使用nginx代理URL显示）
            if (state.form.urls.length > 0) {
              imageList.value = state.form.urls.map((url: string, index: number) => {
                // 判断是视频还是图片
                const isVideo = state.form.noteType === '2';
                // 如果是base64或OSS URL，直接使用；否则使用nginx代理
                let displayUrl = url;
                if (!url.startsWith('data:') && !url.includes('oss') && !url.includes('aliyuncs.com') && !url.includes('qcloud.com')) {
                  displayUrl = isVideo ? getNginxProxyVideoUrl(url) : getNginxProxyImageUrl(url);
                }
                return {
                  uid: `-${index + 1}`,
                  name: isVideo
                    ? t('biz.videoItemDisplayName', { index: index + 1 })
                    : t('biz.imageItemDisplayName', { index: index + 1 }),
                  status: 'done',
                  url: displayUrl, // 显示时使用代理URL
                  originalUrl: url, // 保存原始URL用于提交
                };
              });
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
        
        state.form.file = currentFile; // 保留新上传的文件对象
        
        // 如果有封面图，更新 fileList（使用nginx代理URL显示）
        if (coverUrl && !currentFile) {
          const coverDisplayUrl = coverUrl.startsWith('data:') || coverUrl.includes('oss') || coverUrl.includes('aliyuncs.com') || coverUrl.includes('qcloud.com')
            ? coverUrl
            : getNginxProxyImageUrl(coverUrl);
          fileList.value = [
            {
              uid: '-1',
              name: t('biz.coverFileDisplayName'),
              status: 'done',
              url: coverDisplayUrl, // 显示时使用代理URL
              originalUrl: coverUrl, // 保存原始URL用于提交
            },
          ];
        } else {
          fileList.value = [];
        }
      } else {
        Message.error(msg || t('biz.loadNoteDataFail'));
      }
    } catch (error: any) {
      Message.error(error.message || t('biz.loadNoteDataFail'));
    }
  };

  // 文件上传处理
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
    
    // 立即保存文件对象
    state.form.file = file;
    
    // 立即读取文件预览
    const reader = new FileReader();
    reader.onload = (e) => {
      const previewUrl = e.target?.result as string;
      state.form.noteCover = previewUrl;
      
      // 更新 fileList
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
    
    return false; // 阻止自动上传
  };

  const handleChange = (fileListParam: any, fileItem: any) => {
    // 更新 fileList
    if (Array.isArray(fileListParam)) {
      fileList.value = fileListParam;
      
      // 如果 fileList 为空，说明删除了文件，清空所有相关数据
      if (fileListParam.length === 0) {
        state.form.noteCover = undefined;
        state.form.file = undefined;
        return;
      }
    } else if (fileListParam) {
      fileList.value = [fileListParam];
    } else {
      fileList.value = [];
      // 如果 fileList 为空，说明删除了文件，清空所有相关数据
      state.form.noteCover = undefined;
      state.form.file = undefined;
      return;
    }
    
    // 如果 beforeUpload 已经保存了文件对象，确保 fileList 中也包含
    if (state.form.file && state.form.file instanceof File) {
      if (fileList.value.length > 0) {
        const lastFile = fileList.value[fileList.value.length - 1];
        if (!lastFile.originFile) {
          lastFile.originFile = state.form.file;
        }
      }
      return;
    }
    
    // 获取文件对象
    let file: any = null;
    if (Array.isArray(fileListParam) && fileListParam.length > 0) {
      file = fileListParam[fileListParam.length - 1];
    } else if (fileListParam && !Array.isArray(fileListParam)) {
      file = fileListParam;
    } else if (fileItem) {
      file = fileItem;
    }
    
    if (file) {
      // 尝试从多个可能的属性中获取文件对象
      let fileObj: File | null = null;
      
      if (fileItem?.originFile && fileItem.originFile instanceof File) {
        fileObj = fileItem.originFile;
      } else if (file.originFile && file.originFile instanceof File) {
        fileObj = file.originFile;
      } else if (file instanceof File) {
        fileObj = file;
      }
      
      if (fileObj) {
        state.form.file = fileObj;
        
        // 如果有文件对象，读取预览
        if (!state.form.noteCover || !state.form.noteCover.startsWith('data:')) {
          const reader = new FileReader();
          reader.onload = (e) => {
            const previewUrl = e.target?.result as string;
            state.form.noteCover = previewUrl;
            
            // 更新 fileList
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
      } else if (file.url && !file.originFile) {
        // 只有 URL 没有文件对象，说明是已有的图片（从服务器加载的）
        state.form.noteCover = file.url;
      }
    } else if (!state.form.file) {
      // 没有文件，且之前也没有文件对象，清空
      state.form.noteCover = undefined;
      state.form.file = undefined;
    }
  };

  // 处理文件删除
  const handleRemove = () => {
    state.form.noteCover = undefined;
    state.form.file = undefined;
    fileList.value = [];
    return true; // 返回 true 表示允许删除
  };

  // 多图片/视频上传处理
  const beforeImageUpload = (file: File) => {
    // 判断是视频还是图片类型
    const isVideoType = state.form.noteType === '2';
    
    if (isVideoType) {
      // 视频类型验证
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
      
      // 清空之前的视频（视频类型只能有一个）
      imageList.value = [];
      
      // 读取视频预览
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
        imageList.value = [fileItem]; // 视频只有一个
        
        // 更新 urls 数组（视频预览使用base64，提交时会替换为实际URL）
        state.form.urls = [previewUrl];
      };
      reader.readAsDataURL(file);
    } else {
      // 图片类型验证
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
      
      // 读取图片预览
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
        
        // 更新 urls 数组（优先使用 originalUrl，如果没有则使用 url）
        state.form.urls = imageList.value
          .filter(item => item.url && !item.url.startsWith('data:'))
          .map(item => item.originalUrl || item.url)
          .concat(imageList.value
            .filter(item => item.url && item.url.startsWith('data:'))
            .map(item => item.url));
      };
      reader.readAsDataURL(file);
    }
    
    return false; // 阻止自动上传
  };

  const handleImageChange = (fileListParam: any) => {
    if (Array.isArray(fileListParam)) {
      imageList.value = fileListParam;
      
      // 更新 urls 数组（只包含URL，不包括base64）
      // 优先使用 originalUrl（原始URL），如果没有则使用 url
      state.form.urls = imageList.value
        .filter(item => item.url && !item.url.startsWith('data:'))
        .map(item => item.originalUrl || item.url);
    }
  };

  const handleImageRemove = (fileItem: any) => {
    // 从列表中移除
    imageList.value = imageList.value.filter(item => item.uid !== fileItem.uid);
    
    // 更新 urls 数组
    // 优先使用 originalUrl（原始URL），如果没有则使用 url
    state.form.urls = imageList.value
      .filter(item => item.url && !item.url.startsWith('data:'))
      .map(item => item.originalUrl || item.url);
    
    return true; // 返回 true 表示允许删除
  };

  watch(
    () => props.visible,
    (value) => {
      if (value) {
        if (props.id) {
          state.title = t('biz.editNoteDialogTitle');
          getNoteForm(props.id);
        } else {
          reset();
          // 使用 nextTick 确保 reset 完成后再填充数据
          nextTick(() => {
            if (props.initialData) {
              fillFormWithInitialData(props.initialData);
            }
          });
        }
      } else {
        reset();
      }
    }
  );

  // 监听 initialData 变化，当弹窗已打开且 initialData 更新时，填充表单
  watch(
    () => props.initialData,
    (newData) => {
      if (props.visible && !props.id && newData) {
        fillFormWithInitialData(newData);
      }
    },
    { deep: true }
  );

  watch(
    () => props.id,
    (value) => {
      if (value && props.visible) {
        state.title = t('biz.editNoteDialogTitle');
        getNoteForm(value);
      }
    }
  );

  // 监听封面变化，自动更新 fileList（使用nginx代理URL显示）
  watch(
    () => state.form.noteCover,
    (value: string | undefined) => {
      if (value && typeof value === 'string' && !state.form.file) {
        // 只有在没有新上传文件时才显示已有图片
        const coverDisplayUrl = (value.startsWith('data:') || value.includes('oss') || value.includes('aliyuncs.com') || value.includes('qcloud.com'))
          ? value
          : getNginxProxyImageUrl(value);
        fileList.value = [
          {
            uid: '-1',
            name: t('biz.coverFileDisplayName'),
            status: 'done',
            url: coverDisplayUrl, // 显示时使用代理URL
            originalUrl: value, // 保存原始URL用于提交
          },
        ];
      } else if (!value && !state.form.file) {
        fileList.value = [];
      }
    },
    { immediate: true }
  );

  // 初始化
  initDictData();
  fetchTreeSelectOption();
  loadTagOptions();
  initRegionOptions();
</script>

<style scoped>
  .image-uploader {
    width: 150px;
    height: 150px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s;
  }

  .image-uploader:hover {
    border-color: #409eff;
  }

  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #8c939d;
  }

  /* 封面上传组件样式优化 - 缩小容器，增大图片显示 */
  .cover-uploader :deep(.arco-upload-list-picture-card) {
    width: 120px;
    height: 120px;
  }

  .cover-uploader :deep(.arco-upload-list-picture-card-item) {
    width: 120px;
    height: 120px;
  }

  .cover-uploader :deep(.arco-upload-list-picture-card-item-image) {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .cover-uploader :deep(.arco-upload-list-picture-card-item-upload) {
    width: 120px;
    height: 120px;
  }

  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
  }

  /* 笔记图片上传组件样式调整 - 对齐商品编辑页面 */
  :deep(.arco-upload-list-picture-card) {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  :deep(.arco-upload-list-picture-card-item) {
    margin: 0;
  }

  :deep(.arco-upload-list-picture-card-item-upload) {
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
