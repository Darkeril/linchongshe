<template>
  <a-modal
    :visible="open"
    :title="state.title"
    width="800px"
    :body-style="{ maxHeight: '70vh', overflowY: 'auto', padding: '20px' }"
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
    >
      <a-row :gutter="16">
        <!-- 基本信息 -->
        <a-col :span="24">
          <a-form-item :label="$t('biz.albumName')" field="title">
            <a-input
              v-model="state.form.title"
              :placeholder="$t('biz.albumNameInputPh')"
            />
          </a-form-item>
        </a-col>

        <a-col :span="24">
          <a-form-item :label="$t('biz.albumOwnerFieldLabel')" field="uid">
            <a-select
              v-model="state.form.uid"
              allow-search
              :filter-option="false"
              :loading="state.userSearching"
              :placeholder="$t('biz.albumUserSearchPh')"
              allow-clear
              @search="handleUserSearch"
              @change="handleUserChange"
            >
              <a-option
                v-for="opt in state.userOptions"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </a-option>
            </a-select>
          </a-form-item>
        </a-col>
        
        <a-col :span="24">
          <a-form-item :label="$t('biz.albumSortField')" field="sort">
            <a-input-number
              v-model="state.form.sort"
              :min="0"
              :max="999"
              :placeholder="$t('biz.albumSortInputPh')"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
        
        <!-- 专辑封面 -->
        <a-col :span="24">
          <a-form-item :label="$t('biz.albumCoverFieldLabel')" field="albumCover">
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
                <div v-if="!state.form.albumCover" class="upload-placeholder">
                  <icon-plus style="font-size: 24px; color: #8c939d" />
                  <div style="font-size: 12px; margin-top: 8px;">{{ $t('biz.uploadCover') }}</div>
                </div>
                <template v-else>
                  <a-image
                    :src="state.form.albumCover"
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
      </a-row>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, reactive, watch, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { Message } from '@arco-design/web-vue';
import type { FormInstance } from '@arco-design/web-vue';
import { IconPlus } from '@arco-design/web-vue/es/icon';
import { getAlbumById, addAlbum, editAlbum } from '@/api/web/album';
import {
  getUserByKeyword,
  getUserById,
  unwrapUserKeywordPage,
  unwrapUserEntity,
  type WebUserRecord,
} from '@/api/web/user';
import axios from '@/api/interceptor';

const { t } = useI18n();

const props = defineProps<{
  open: boolean;
  id: number | string;
}>();

const emits = defineEmits<{
  cancel: [];
  success: [];
}>();

const formRef = ref<FormInstance>();
const fileList = ref<any[]>([]);

type UserOption = {
  value: string;
  label: string;
  username?: string;
  id: string;
  avatar?: string;
};

const state = reactive({
  title: t('biz.addAlbumDialogTitle'),
  form: {
    id: undefined as string | number | undefined,
    title: '',
    uid: '' as string,
    sort: 0,
    albumCover: '',
    file: undefined as File | undefined,
  },
  userOptions: [] as UserOption[],
  userSearching: false,
  rules: {
    title: [
      { required: true, message: t('biz.validateAlbumNameRequired'), trigger: 'blur' },
    ],
    uid: [{ required: true, message: t('biz.validateAlbumUidRequired'), trigger: 'change' }],
    sort: [
      { required: true, message: t('biz.validateAlbumSortRequired'), trigger: 'blur' },
    ],
  },
});

let userSearchTimer: ReturnType<typeof setTimeout> | null = null;

async function ensureUserOptionForUid(uid: string) {
  if (!uid) return;
  try {
    const res = await getUserById(uid);
    const u = unwrapUserEntity(res);
    if (u?.id != null) {
      const idStr = String(u.id);
      state.userOptions = [
        {
          value: idStr,
          label: `${u.username || t('biz.userDefaultName')} (ID: ${idStr})`,
          username: u.username,
          id: idStr,
          avatar: u.avatar,
        },
      ];
    }
  } catch {
    state.userOptions = [
      {
        value: uid,
        label: t('biz.uidLabel', { id: uid }),
        id: uid,
      },
    ];
  }
}

async function fetchUserOptions(keyword: string) {
  if (!keyword || !keyword.trim()) {
    state.userOptions = [];
    return;
  }
  state.userSearching = true;
  try {
    const res = await getUserByKeyword(1, 20, keyword.trim());
    const userList = unwrapUserKeywordPage(res);
    state.userOptions = userList.map((user: WebUserRecord) => {
      const idStr = String(user.id);
      return {
        value: idStr,
        label: `${user.username || t('biz.albumUnnamedShort')} (ID: ${idStr})`,
        username: user.username,
        id: idStr,
        avatar: user.avatar,
      };
    });
  } catch {
    state.userOptions = [];
    Message.error(t('biz.searchUserFailed'));
  } finally {
    state.userSearching = false;
  }
}

const handleUserSearch = (value: string) => {
  if (userSearchTimer) clearTimeout(userSearchTimer);
  userSearchTimer = setTimeout(() => {
    fetchUserOptions(value);
  }, 300);
};

function handleUserChange(value: string | undefined) {
  if (!value) {
    state.userOptions = [];
  }
}

// 加载专辑详情
const loadAlbumData = async () => {
  if (!props.id || props.id === 0 || props.id === '0') {
    // 新建模式，重置表单
    state.form = {
      id: undefined,
      title: '',
      uid: '',
      sort: 0,
      albumCover: '',
      file: undefined,
    };
    state.userOptions = [];
    fileList.value = [];
    state.title = t('biz.addAlbumDialogTitle');
    return;
  }

  // 编辑模式，加载数据
  try {
    const res = await getAlbumById(props.id);
    const raw = res as Record<string, any>;
    const data = raw?.data ?? raw ?? {};

    const uidStr = data.uid != null && data.uid !== '' ? String(data.uid) : '';

    state.form = {
      id: data.id,
      title: data.title || '',
      uid: uidStr,
      sort: data.sort ?? 0,
      albumCover: data.albumCover || '',
      file: undefined,
    };

    if (uidStr) {
      await ensureUserOptionForUid(uidStr);
    } else {
      state.userOptions = [];
    }
    
    // 如果有封面，设置 fileList
    if (data.albumCover) {
      fileList.value = [{
        uid: Date.now().toString(),
        name: 'album-cover.jpg',
        url: data.albumCover,
        thumbUrl: data.albumCover,
        status: 'done' as const,
      }];
    } else {
      fileList.value = [];
    }
    
    state.title = t('biz.editAlbumDialogTitle');
  } catch (error: any) {
    console.error('获取专辑详情失败:', error);
    Message.error(
      error?.response?.data?.msg ||
        error?.message ||
        t('biz.loadAlbumDetailFail')
    );
  }
};

// 监听 props.id 变化
watch(
  () => [props.open, props.id],
  () => {
    if (props.open) {
      nextTick(() => {
        loadAlbumData();
      });
    }
  },
  { immediate: true }
);

// 阻止默认上传行为
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    Message.error(t('biz.imageOnly'));
    return false;
  }
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    Message.error(t('biz.imageSize5m'));
    return false;
  }
  
  // 保存文件对象
  state.form.file = file;
  
  // 立即读取文件预览
  const reader = new FileReader();
  reader.onload = (e) => {
    const previewUrl = e.target?.result as string;
    state.form.albumCover = previewUrl;
    
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
  
  return false; // 阻止自动上传，使用手动上传
};

// 处理文件变更
const handleChange = (fileListParam: any) => {
  if (!fileListParam || fileListParam.length === 0) {
    // 文件列表为空，清空封面
    state.form.albumCover = '';
    state.form.file = undefined;
    fileList.value = [];
    return;
  }
  
  const file = fileListParam[fileListParam.length - 1];
  if (file.originFile) {
    // 新上传的文件
    state.form.file = file.originFile;
    const reader = new FileReader();
    reader.onload = (e) => {
      const previewUrl = e.target?.result as string;
      state.form.albumCover = previewUrl;
    };
    reader.readAsDataURL(file.originFile);
  } else if (file.url && !file.originFile) {
    // 已有图片
    state.form.albumCover = file.url;
  }
};

// 处理文件删除
const handleRemove = () => {
  state.form.albumCover = '';
  state.form.file = undefined;
  fileList.value = [];
};

// 确认提交
const handleConfirm = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    // 如果有新上传的文件，先上传到OSS
    let albumCoverUrl = state.form.albumCover;
    if (state.form.file) {
      const formData = new FormData();
      formData.append('file', state.form.file);
      
      try {
        const uploadRes = await axios.post('/web/oss/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });
        
        if (uploadRes?.code === 200 || uploadRes?.status === 200) {
          albumCoverUrl = uploadRes.data || uploadRes.data?.url || albumCoverUrl;
        } else {
          Message.error(uploadRes?.msg || t('biz.albumCoverUploadFail'));
          return;
        }
      } catch (error: any) {
        console.error('封面上传失败:', error);
        Message.error(
          error?.response?.data?.msg ||
            error?.message ||
            t('biz.albumCoverUploadFail')
        );
        return;
      }
    }
    
    // 构建提交数据
    const submitData: any = {
      title: state.form.title,
      sort: state.form.sort || 0,
      albumCover: albumCoverUrl,
      uid: state.form.uid?.trim() || undefined,
    };

    if (!submitData.uid) {
      Message.warning(t('biz.selectAlbumOwnerWarn'));
      return;
    }
    
    if (state.form.id) {
      // 编辑
      submitData.id = state.form.id;
      const res = await editAlbum(submitData);
      if (res?.code === 200 || res?.status === 200) {
        Message.success(t('biz.editSuccess'));
        emits('success');
      } else {
        Message.error(res?.msg || t('biz.editFailed'));
      }
    } else {
      // 新增
      const res = await addAlbum(submitData);
      if (res?.code === 200 || res?.status === 200) {
        Message.success(t('biz.addSuccess'));
        emits('success');
      } else {
        Message.error(res?.msg || t('biz.addFailed'));
      }
    }
  } catch (error: any) {
    console.error('表单验证失败:', error);
    if (error?.fields) {
      Message.error(t('biz.formCompleteWarn'));
    }
  }
};

// 取消
const handleCancel = () => {
  emits('cancel');
};
</script>

<style scoped lang="less">
.cover-uploader {
  :deep(.arco-upload-list-picture-card) {
    .arco-upload-list-item {
      width: 120px;
      height: 120px;
    }
  }
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background-color: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    border-color: #1890ff;
  }
}
</style>



