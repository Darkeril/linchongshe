<template>
  <a-modal
    :visible="open"
    :title="state.title"
    width="600px"
    @cancel="handleCancel"
    @ok="handleConfirm"
  >
    <a-form
      ref="formRef"
      :model="state.form"
      :rules="state.rules"
      auto-label-width
    >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item
            label="会员名称"
            field="username"
            :rules="[{ required: true, message: '会员名称不能为空' }]"
            :validate-trigger="['change', 'input']"
          >
            <a-input
              v-model="state.form.username"
              placeholder="请输入会员名称"
              maxlength="30"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="头像" field="avatar">
            <a-upload
              class="image-uploader"
              list-type="picture-card"
              :show-file-list="true"
              :auto-upload="false"
              :image-preview="true"
              :limit="1"
              :file-list="fileList"
              @before-upload="beforeUpload"
              @change="handleChange"
              @remove="handleRemove"
            >
              <template #upload-button>
                <div class="upload-placeholder">
                  <icon-plus />
                  <div class="upload-text">上传头像</div>
                </div>
              </template>
            </a-upload>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item
            label="手机号码"
            field="phone"
            :rules="state.form.id ? [] : [
              { required: true, message: '手机号码不能为空' },
              { match: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码' },
            ]"
            :validate-trigger="['change', 'input']"
          >
            <a-input
              v-model="state.form.phone"
              placeholder="请输入手机号码"
              :disabled="!!state.form.id"
              maxlength="11"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item
            label="密码"
            field="password"
            :rules="state.form.id ? [
              { min: 5, max: 20, message: '用户密码长度必须介于 5 和 20 之间' },
            ] : [
              { required: true, message: '用户密码不能为空' },
              { min: 5, max: 20, message: '用户密码长度必须介于 5 和 20 之间' },
            ]"
            :validate-trigger="['change', 'input']"
          >
            <a-input-password
              v-model="state.form.password"
              :placeholder="state.form.id ? '留空则不修改密码' : '请输入密码'"
              maxlength="20"
            />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="用户性别">
            <a-select v-model="state.form.gender" placeholder="请选择性别">
              <a-option
                v-for="dict in state.sexOptions"
                :key="dict.dictValue"
                :label="dict.dictLabel"
                :value="dict.dictValue"
              ></a-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="状态">
            <a-radio-group v-model="state.form.status">
              <a-radio
                v-for="dict in state.statusOptions"
                :key="dict.dictValue"
                :value="dict.dictValue"
                :label="dict.dictValue"
                >{{ dict.dictLabel }}
              </a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="24">
          <a-form-item label="备注" label-col-flex="70px">
            <a-textarea
              v-model="state.form.remark"
              placeholder="请输入内容"
            ></a-textarea>
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
import { computed, reactive, ref, watch } from 'vue';
import { getMember, updateMember, addMember } from '@/api/web/member';
import { Form, Message } from '@arco-design/web-vue';
import { DictDataRecord, getDicts } from '@/api/system/dict-data';
import { IconPlus } from '@arco-design/web-vue/es/icon';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  id: {
    type: [Number, String],
    default: undefined,
  },
});

const open = computed(() => {
  return !!props.visible;
});

const state = reactive({
  title: '新增会员',
  sexOptions: [] as DictDataRecord[],
  statusOptions: [] as DictDataRecord[],
  form: {
    id: undefined as number | undefined,
    username: undefined as string | undefined,
    phone: undefined as string | undefined,
    password: undefined as string | undefined,
    gender: undefined as string | undefined,
    status: '0' as string,
    avatar: undefined as string | undefined,
    remark: undefined as string | undefined,
    file: undefined as File | undefined,
  },
  rules: {},
});

const fileList = ref<any[]>([]);

const reset = () => {
  state.title = '新增会员';
  state.form = {
    id: undefined,
    username: undefined,
    phone: undefined,
    password: undefined,
    gender: undefined,
    status: '0',
    avatar: undefined,
    remark: undefined,
    file: undefined,
  };
  fileList.value = [];
};

// 初始化字典
const initData = async () => {
  const [statusResponse, sexResponse] = await Promise.all([
    getDicts('web_member_status').catch(() => getDicts('sys_normal_disable')),
    getDicts('sys_user_sex'),
  ]);
  state.statusOptions = statusResponse.data;
  state.sexOptions = sexResponse.data;
};

initData();

const getMemberForm = async () => {
  state.title = '修改会员';
  try {
    const response = await getMember(props.id as number);
    if (response.data) {
      state.form = {
        id: response.data.id,
        username: response.data.username,
        phone: response.data.phone,
        password: undefined, // 修改时不显示密码
        gender: String(response.data.gender || ''),
        status: String(response.data.status || '0'),
        avatar: response.data.avatar,
        remark: response.data.remark,
        file: undefined,
      };
      if (response.data.avatar) {
        fileList.value = [
          {
            uid: '-1',
            name: 'avatar',
            url: response.data.avatar,
            status: 'done',
          },
        ] as any;
      }
    }
  } catch (error) {
    Message.error('获取会员信息失败');
    console.error('获取会员信息失败:', error);
  }
};

const emits = defineEmits(['update:visible', 'update:id', 'refreshDataList']);
const handleCancel = () => {
  emits('update:visible', false);
  emits('update:id', 0);
  reset();
};

const formRef = ref<typeof Form>();

function beforeUpload(file: File) {
  // 检查文件类型
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    Message.error('只能上传图片文件');
    return false;
  }
  // 检查文件大小（5MB）
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    Message.error('图片大小不能超过 5MB');
    return false;
  }
  // 验证通过，保存文件对象
  state.form.file = file;
  console.log('beforeUpload 保存的文件对象:', file, '文件名:', file.name, '文件大小:', file.size);
  
  // 立即读取文件预览，并更新 fileList
  const reader = new FileReader();
  reader.onload = (e) => {
    const previewUrl = e.target?.result as string;
    state.form.avatar = previewUrl;
    
    // 创建文件对象并添加到 fileList
    const fileItem = {
      uid: Date.now().toString(),
      name: file.name,
      url: previewUrl,
      thumbUrl: previewUrl,
      status: 'done' as const,
      originFile: file,
    };
    
    // 更新 fileList，确保只保留一个文件
    fileList.value = [fileItem];
    console.log('beforeUpload 中更新 fileList:', fileList.value);
  };
  reader.readAsDataURL(file);
  
  return false; // 阻止自动上传，使用手动上传
}

function handleChange(fileListParam: any, fileItem: any) {
  console.log('handleChange 被调用');
  console.log('参数1 (fileListParam):', fileListParam);
  console.log('参数2 (fileItem):', fileItem);
  
  // 如果 beforeUpload 中已经设置了 fileList（有预览 URL），则不再覆盖
  if (fileList.value.length > 0 && fileList.value[0]?.url && fileList.value[0]?.thumbUrl) {
    console.log('fileList 已有预览，跳过 handleChange 的 fileList 更新');
  } else if (Array.isArray(fileListParam)) {
    // 更新 fileList
    // 如果第一个参数是数组，说明是文件列表
    fileList.value = fileListParam;
  } else if (fileListParam) {
    // 如果第一个参数是单个文件对象，转换为数组
    fileList.value = [fileListParam];
  } else {
    fileList.value = [];
  }
  
  // 获取文件对象（可能是单个文件或文件列表中的最后一个）
  let file: any = null;
  if (Array.isArray(fileListParam) && fileListParam.length > 0) {
    file = fileListParam[fileListParam.length - 1];
  } else if (fileListParam && !Array.isArray(fileListParam)) {
    file = fileListParam;
  } else if (fileItem) {
    file = fileItem;
  }
  
  console.log('提取的 file 对象:', file);
  console.log('当前 state.form.file:', state.form.file);
  
  if (file) {
    console.log('file 对象 keys:', Object.keys(file));
    console.log('file.originFile:', file.originFile);
    console.log('file.file:', file.file);
    console.log('file.rawFile:', file.rawFile);
    
    // 检查是否有原始文件对象（新上传的文件）
    // 参考 system/config/index.vue 的实现，使用 file.originFile
    let fileObj: File | null = null;
    
    // 优先使用 before-upload 中保存的文件对象（最可靠）
    if (state.form.file && state.form.file instanceof File) {
      fileObj = state.form.file;
      console.log('使用 before-upload 中保存的文件对象');
    } else if (file.originFile && file.originFile instanceof File) {
      // 参考 system/config/index.vue，使用 originFile
      fileObj = file.originFile;
      state.form.file = fileObj || undefined;
      console.log('从 file.originFile 获取文件对象');
    } else if (file.file && file.file instanceof File) {
      fileObj = file.file;
      state.form.file = fileObj || undefined;
      console.log('从 file.file 获取文件对象');
    } else if (file.rawFile && file.rawFile instanceof File) {
      fileObj = file.rawFile;
      state.form.file = fileObj || undefined;
      console.log('从 file.rawFile 获取文件对象');
    } else if (file instanceof File) {
      // 如果 file 本身就是 File 对象
      fileObj = file;
      state.form.file = fileObj || undefined;
      console.log('file 本身就是 File 对象');
    }
    
    if (fileObj) {
      // 新上传的文件
      console.log('保存的文件对象:', fileObj, '文件名:', fileObj.name, '文件大小:', fileObj.size);
      // 读取文件预览
      const reader = new FileReader();
      reader.onload = (e) => {
        const previewUrl = e.target?.result as string;
        state.form.avatar = previewUrl;
        
        // 更新 fileList，添加预览 URL，确保上传组件显示预览
        // 需要确保文件对象有正确的结构
        if (fileList.value.length > 0) {
          const updatedFileList = fileList.value.map((item: any, index: number) => {
            if (index === fileList.value.length - 1) {
              // 更新最后一个文件对象（新上传的文件）
              return {
                ...item,
                url: previewUrl,
                thumbUrl: previewUrl,
                status: 'done',
              };
            }
            return item;
          });
          fileList.value = updatedFileList;
          console.log('更新后的 fileList:', fileList.value);
        } else {
          // 如果 fileList 为空，创建一个新的文件对象
          fileList.value = [{
            uid: Date.now().toString(),
            name: fileObj.name,
            url: previewUrl,
            thumbUrl: previewUrl,
            status: 'done',
            originFile: fileObj,
          }];
          console.log('创建新的 fileList:', fileList.value);
        }
      };
      reader.readAsDataURL(fileObj);
    } else if (file.url && !file.originFile && !file.file && !file.rawFile) {
      // 只有 URL 没有文件对象，说明是已有的图片（从服务器加载的），没有新文件
      state.form.avatar = file.url;
      // 注意：不要清空 state.form.file，因为可能之前已经保存了文件对象
      if (!state.form.file) {
        state.form.file = undefined;
      }
      console.log('使用已有图片 URL:', file.url);
    } else {
      console.warn('无法获取文件对象，file 结构:', file);
      if (!state.form.file) {
        console.warn('没有找到文件对象，且之前也没有保存的文件对象');
      }
    }
  } else {
    // 没有文件，清空
    state.form.avatar = undefined;
    state.form.file = undefined;
  }
  
  console.log('handleChange 结束, state.form.file:', state.form.file);
  console.log('handleChange 结束, fileList.value:', fileList.value);
}

function handleRemove() {
  // 删除文件时，清空头像和文件
  // Arco Design 会自动从 fileList 中移除文件，我们只需要清空表单数据
  state.form.avatar = undefined;
  state.form.file = undefined;
  // 确保 fileList 为空，这样上传按钮会显示
  fileList.value = [];
  return true; // 返回 true 表示允许删除
}

function handleConfirm() {
  formRef.value?.validate().then((error: any) => {
    if (!error) {
      const params = new FormData();
      
      // 如果有新上传的文件，添加到 FormData
      // 注意：修改时如果没有上传新文件，则不传递 file 参数
      console.log('提交时的文件对象:', state.form.file);
      if (state.form.file && state.form.file instanceof File) {
        params.append('file', state.form.file);
        console.log('文件已添加到 FormData, 文件名:', state.form.file.name, '文件大小:', state.form.file.size);
      } else {
        console.log('没有文件对象，跳过文件上传');
      }
      
      // 构建用户数据对象
      const userData: any = {
        username: state.form.username,
        phone: state.form.phone,
        gender: state.form.gender,
        status: state.form.status,
        avatar: state.form.avatar,
        remark: state.form.remark,
      };
      
      // 如果是新增，必须添加密码
      if (!state.form.id) {
        userData.password = state.form.password;
      } else {
        // 如果是修改，添加 id
        userData.id = state.form.id;
        // 如果密码不为空，才添加密码字段（留空则不修改密码）
        if (state.form.password && String(state.form.password).trim() !== '') {
          userData.password = state.form.password;
        }
      }
      
      // 将用户数据转为 JSON 字符串添加到 FormData
      params.append('user', JSON.stringify(userData));
      
      if (state.form.id) {
        // 修改会员
        updateMember(params as any)
          .then(() => {
            Message.success('修改成功');
            emits('update:visible', false);
            emits('update:id', 0);
            emits('refreshDataList');
            reset();
          })
          .catch((error) => {
            console.error('修改失败:', error);
            // 如果错误有 response.data，说明是业务错误，拦截器已经显示了错误消息
            // 拦截器会在 code !== 200 时显示错误，所以这里不需要再显示
            if (error?.response?.data) {
              // 拦截器已经显示了错误，不需要重复显示
              return;
            }
            // 提取错误信息（仅用于网络错误等拦截器未处理的情况）
            let errorMessage = '修改失败';
            if (error?.message) {
              errorMessage = error.message;
            }
            Message.error(errorMessage);
          });
      } else {
        // 新增会员
        addMember(params as any)
          .then(() => {
            Message.success('新增成功');
            emits('update:visible', false);
            emits('update:id', 0);
            emits('refreshDataList');
            reset();
          })
          .catch((error) => {
            console.error('新增失败:', error);
            // 如果错误有 response.data，说明是业务错误，拦截器已经显示了错误消息
            // 拦截器会在 code !== 200 时显示错误，所以这里不需要再显示
            if (error?.response?.data) {
              // 拦截器已经显示了错误，不需要重复显示
              return;
            }
            // 提取错误信息（仅用于网络错误等拦截器未处理的情况）
            let errorMessage = '新增失败';
            if (error?.message) {
              errorMessage = error.message;
            }
            Message.error(errorMessage);
          });
      }
    }
  });
}

watch(
  () => props.id,
  (value) => {
    if (value && value !== 0) {
      getMemberForm();
    } else {
      reset();
    }
  },
  { immediate: true }
);

watch(
  () => props.visible,
  (value) => {
    if (value) {
      // 对话框打开时
      if (props.id && props.id !== 0) {
        // 有 id，加载编辑数据
        getMemberForm();
      } else {
        // 没有 id 或 id 为 0，显示新增表单
        reset();
      }
    } else {
      // 对话框关闭时，重置表单
      reset();
    }
  }
);
</script>

<script lang="ts">
export default {
  name: 'EditMember',
};
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

.image-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #86909c;
}

.upload-text {
  margin-top: 8px;
  font-size: 12px;
}
</style>
