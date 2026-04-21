<template>
  <a-form
    ref="websiteFormRef"
    style="margin-left: 20px"
    :model="websiteForm"
    :label-col-props="{ span: 4 }"
    :wrapper-col-props="{ span: 16 }"
  >
    <div class="form-section">
      <h4>网站基本信息配置</h4>
    </div>

    <!-- 备案号 -->
    <a-form-item label="备案号" field="recordNumber">
      <a-input
        v-model="websiteForm.recordNumber"
        style="width: 400px"
        placeholder="请输入备案号，例如：京ICP备12345678号"
      />
    </a-form-item>

    <!-- LOGO上传 -->
    <a-form-item label="LOGO" field="logo">
      <a-upload
        :auto-upload="false"
        :show-file-list="false"
        :before-upload="beforeLogoUpload"
        accept="image/jpeg,image/png"
        @change="handleLogoChange"
      >
        <template #upload-button>
          <div class="logo-uploader">
            <img
              v-if="websiteForm.logo"
              :key="websiteForm.logo.substring(0, 100)"
              :src="websiteForm.logo"
              class="logo-image"
            />
            <div v-else class="logo-uploader-icon">
              <icon-plus />
            </div>
          </div>
        </template>
      </a-upload>
      <div class="upload-tip">
        建议尺寸：200x200px，支持 JPG、PNG 格式
      </div>
    </a-form-item>

    <!-- 网站名称 -->
    <a-form-item label="网站名称" field="name">
      <a-input
        v-model="websiteForm.name"
        style="width: 400px"
        placeholder="请输入网站名称"
      />
    </a-form-item>

    <!-- 作者 -->
    <a-form-item label="作者" field="author">
      <a-input
        v-model="websiteForm.author"
        style="width: 400px"
        placeholder="请输入网站作者"
      />
    </a-form-item>

    <!-- 标题 -->
    <a-form-item label="标题" field="title">
      <a-input
        v-model="websiteForm.title"
        style="width: 400px"
        placeholder="请输入网站标题"
      />
    </a-form-item>

    <!-- 描述 -->
    <a-form-item label="描述" field="description">
      <a-textarea
        v-model="websiteForm.description"
        :rows="4"
        style="width: 400px"
        placeholder="请输入网站描述"
      />
      <div class="upload-tip">建议长度：100-200字</div>
    </a-form-item>

    <!-- 版权申明 -->
    <a-form-item label="版权申明" field="copyright">
      <a-textarea
        v-model="websiteForm.copyright"
        :rows="3"
        style="width: 400px"
        placeholder="请输入版权申明"
      />
    </a-form-item>

    <!-- AI路径 -->
    <a-form-item label="AI路径" field="aiUrl">
      <a-input
        v-model="websiteForm.aiUrl"
        style="width: 400px"
        placeholder="请输入AI路径"
      />
    </a-form-item>

    <!-- 微信二维码 -->
    <a-form-item label="微信二维码" field="wechatQrCode">
      <a-upload
        :auto-upload="false"
        :show-file-list="false"
        :before-upload="beforeWechatQrUpload"
        accept="image/jpeg,image/png"
        @change="handleWechatQrChange"
      >
        <template #upload-button>
          <div class="qr-uploader">
            <img
              v-if="websiteForm.wechatQrCode"
              :key="websiteForm.wechatQrCode.substring(0, 100)"
              :src="websiteForm.wechatQrCode"
              class="qr-image"
            />
            <div v-else class="qr-uploader-icon">
              <icon-plus />
            </div>
          </div>
        </template>
      </a-upload>
      <div class="upload-tip">
        建议尺寸：200x200px，支持 JPG、PNG 格式
      </div>
    </a-form-item>

    <!-- 收款二维码 -->
    <a-form-item label="收款二维码">
      <div class="reward-container">
        <!-- 微信二维码 -->
        <div class="reward-item">
          <div class="reward-title wechat">
            <icon-wechat />
            微信
          </div>
          <a-upload
            :auto-upload="false"
            :show-file-list="false"
            :before-upload="beforeWechatRewardQrUpload"
            accept="image/jpeg,image/png"
            @change="handleWechatRewardQrChange"
          >
            <template #upload-button>
              <div class="reward-uploader">
                <img
                  v-if="websiteForm.wechatRewardQrCode"
                  :key="websiteForm.wechatRewardQrCode.substring(0, 100)"
                  :src="websiteForm.wechatRewardQrCode"
                  class="reward-image"
                />
                <div v-else class="reward-uploader-icon">
                  <icon-plus />
                </div>
              </div>
            </template>
          </a-upload>
          <div class="reward-label">微信收款码</div>
        </div>

        <!-- 支付宝二维码 -->
        <div class="reward-item">
          <div class="reward-title alipay">
            <icon-alipay-circle />
            支付宝
          </div>
          <a-upload
            :auto-upload="false"
            :show-file-list="false"
            :before-upload="beforeAlipayRewardQrUpload"
            accept="image/jpeg,image/png"
            @change="handleAlipayRewardQrChange"
          >
            <template #upload-button>
              <div class="reward-uploader">
                <img
                  v-if="websiteForm.alipayRewardQrCode"
                  :key="websiteForm.alipayRewardQrCode.substring(0, 100)"
                  :src="websiteForm.alipayRewardQrCode"
                  class="reward-image"
                />
                <div v-else class="reward-uploader-icon">
                  <icon-plus />
                </div>
              </div>
            </template>
          </a-upload>
          <div class="reward-label">支付宝收款码</div>
        </div>
      </div>
      <div class="upload-tip">
        建议尺寸：200x200px，支持 JPG、PNG 格式
      </div>
    </a-form-item>

    <!-- 关注我们 -->
    <a-form-item label="关注我们" field="followUs">
      <a-textarea
        v-model="websiteForm.followUs"
        :rows="3"
        style="width: 400px"
        placeholder="请输入关注我们的内容"
      />
    </a-form-item>

    <a-form-item>
      <a-button type="primary" @click="handleSubmit">
        保 存
      </a-button>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus, IconWechat, IconAlipayCircle } from '@arco-design/web-vue/es/icon';
import { editWebsiteConfig, uploadBatchFiles } from '@/api/system/config';

// Props
interface Props {
  initialData?: any;
}

const props = withDefaults(defineProps<Props>(), {
  initialData: () => ({}),
});

// Emits
const emit = defineEmits<{
  (e: 'update'): void;
}>();

// 表单引用
const websiteFormRef = ref();

// 表单数据
const websiteForm = reactive({
  recordNumber: '',
  logo: '',
  name: '',
  author: '',
  title: '',
  description: '',
  copyright: '',
  aiUrl: '',
  wechatQrCode: '',
  followUs: '',
  alipayRewardQrCode: '',
  wechatRewardQrCode: '',
});

// 文件引用
const logoFile = ref<File | null>(null);
const wechatQrFile = ref<File | null>(null);
const alipayRewardQrFile = ref<File | null>(null);
const wechatRewardQrFile = ref<File | null>(null);

// 初始化数据
if (props.initialData && Object.keys(props.initialData).length > 0) {
  Object.assign(websiteForm, props.initialData);
}

// 文件上传处理
const handleLogoChange = (fileList: any[]): void => {
  console.log('handleLogoChange 被调用, fileList:', fileList);
  if (fileList && fileList.length > 0) {
    const fileItem = fileList[fileList.length - 1];
    console.log('handleLogoChange fileItem:', fileItem);
    
    // 获取文件对象（可能是 file.file 或 file.originFile）
    const file = fileItem?.file || fileItem?.originFile;
    
    if (file && file instanceof File) {
      console.log('handleLogoChange: 找到文件对象, 开始读取预览');
      logoFile.value = file;
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = async (e) => {
        const result = e.target?.result as string;
        if (result) {
          websiteForm.logo = result;
          console.log('handleLogoChange: 预览已更新, logo length:', websiteForm.logo.length);
          await nextTick();
        }
      };
      reader.onerror = (error) => {
        console.error('handleLogoChange: FileReader 错误:', error);
        Message.error('LOGO图片读取失败');
      };
    } else if (fileItem?.url && fileItem.url.startsWith('blob:')) {
      // 如果已经有 blob URL，直接使用
      console.log('handleLogoChange: 使用 blob URL');
      websiteForm.logo = fileItem.url;
    } else {
      console.warn('handleLogoChange: 无法获取文件对象', fileItem);
    }
  } else {
    // 文件被删除，清空预览
    logoFile.value = null;
    // 注意：这里不清空 websiteForm.logo，保留原有的URL
  }
};

const handleWechatQrChange = (fileList: any[]): void => {
  if (fileList && fileList.length > 0) {
    const fileItem = fileList[fileList.length - 1];
    const file = fileItem?.file || fileItem?.originFile;
    if (file && file instanceof File) {
      wechatQrFile.value = file;
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = async (e) => {
        const result = e.target?.result as string;
        if (result) {
          websiteForm.wechatQrCode = result;
          await nextTick();
        }
      };
      reader.onerror = () => {
        Message.error('微信二维码图片读取失败');
      };
    } else if (fileItem?.url && fileItem.url.startsWith('blob:')) {
      websiteForm.wechatQrCode = fileItem.url;
    }
  } else {
    wechatQrFile.value = null;
  }
};

const handleWechatRewardQrChange = (fileList: any[]): void => {
  if (fileList && fileList.length > 0) {
    const fileItem = fileList[fileList.length - 1];
    const file = fileItem?.file || fileItem?.originFile;
    if (file && file instanceof File) {
      wechatRewardQrFile.value = file;
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = async (e) => {
        const result = e.target?.result as string;
        if (result) {
          websiteForm.wechatRewardQrCode = result;
          await nextTick();
        }
      };
      reader.onerror = () => {
        Message.error('微信收款码图片读取失败');
      };
    } else if (fileItem?.url && fileItem.url.startsWith('blob:')) {
      websiteForm.wechatRewardQrCode = fileItem.url;
    }
  } else {
    wechatRewardQrFile.value = null;
  }
};

const handleAlipayRewardQrChange = (fileList: any[]): void => {
  if (fileList && fileList.length > 0) {
    const fileItem = fileList[fileList.length - 1];
    const file = fileItem?.file || fileItem?.originFile;
    if (file && file instanceof File) {
      alipayRewardQrFile.value = file;
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = async (e) => {
        const result = e.target?.result as string;
        if (result) {
          websiteForm.alipayRewardQrCode = result;
          await nextTick();
        }
      };
      reader.onerror = () => {
        Message.error('支付宝收款码图片读取失败');
      };
    } else if (fileItem?.url && fileItem.url.startsWith('blob:')) {
      websiteForm.alipayRewardQrCode = fileItem.url;
    }
  } else {
    alipayRewardQrFile.value = null;
  }
};

// 文件上传前验证和处理
const beforeLogoUpload = (file: File): boolean => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    Message.error('只能上传图片文件');
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    Message.error('图片大小不能超过 2MB');
    return false;
  }
  
  console.log('beforeLogoUpload 被调用, 文件:', file.name, file.size, file.type);
  
  // 保存文件对象
  logoFile.value = file;
  
  // 立即读取文件预览
  const reader = new FileReader();
  reader.onload = async (e) => {
    const result = e.target?.result as string;
    console.log('FileReader onload, result length:', result?.length);
    if (result) {
      websiteForm.logo = result;
      console.log('websiteForm.logo 已设置, 长度:', websiteForm.logo.length);
      await nextTick();
      console.log('LOGO预览已更新, 当前值:', websiteForm.logo?.substring(0, 50));
    }
  };
  reader.onerror = (error) => {
    console.error('FileReader onerror:', error);
    Message.error('LOGO图片读取失败');
  };
  reader.readAsDataURL(file);
  console.log('FileReader.readAsDataURL 已调用');
  
  return false; // 阻止自动上传，使用手动上传
};

const beforeQrUpload = (file: File): boolean => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    Message.error('只能上传图片文件');
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    Message.error('图片大小不能超过 2MB');
    return false;
  }
  return true;
};

const beforeWechatQrUpload = (file: File): boolean => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    Message.error('只能上传图片文件');
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    Message.error('图片大小不能超过 2MB');
    return false;
  }
  
  // 保存文件对象
  wechatQrFile.value = file;
  
  // 立即读取文件预览
  const reader = new FileReader();
  reader.onload = async (e) => {
    const result = e.target?.result as string;
    if (result) {
      websiteForm.wechatQrCode = result;
      await nextTick();
      console.log('微信二维码预览已更新');
    }
  };
  reader.onerror = () => {
    Message.error('微信二维码图片读取失败');
  };
  reader.readAsDataURL(file);
  
  return false; // 阻止自动上传，使用手动上传
};

const beforeWechatRewardQrUpload = (file: File): boolean => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    Message.error('只能上传图片文件');
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    Message.error('图片大小不能超过 2MB');
    return false;
  }
  
  // 保存文件对象
  wechatRewardQrFile.value = file;
  
  // 立即读取文件预览
  const reader = new FileReader();
  reader.onload = async (e) => {
    const result = e.target?.result as string;
    if (result) {
      websiteForm.wechatRewardQrCode = result;
      await nextTick();
      console.log('微信收款码预览已更新');
    }
  };
  reader.onerror = () => {
    Message.error('微信收款码图片读取失败');
  };
  reader.readAsDataURL(file);
  
  return false; // 阻止自动上传，使用手动上传
};

const beforeAlipayRewardQrUpload = (file: File): boolean => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    Message.error('只能上传图片文件');
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    Message.error('图片大小不能超过 2MB');
    return false;
  }
  
  // 保存文件对象
  alipayRewardQrFile.value = file;
  
  // 立即读取文件预览
  const reader = new FileReader();
  reader.onload = async (e) => {
    const result = e.target?.result as string;
    if (result) {
      websiteForm.alipayRewardQrCode = result;
      await nextTick();
      console.log('支付宝收款码预览已更新');
    }
  };
  reader.onerror = () => {
    Message.error('支付宝收款码图片读取失败');
  };
  reader.readAsDataURL(file);
  
  return false; // 阻止自动上传，使用手动上传
};

// 提交表单
const handleSubmit = async (): Promise<void> => {
  try {
    // 收集需要上传的文件
    const uploadTasks = [];
    if (logoFile.value)
      uploadTasks.push({ file: logoFile.value, type: 'logo' });
    if (wechatQrFile.value)
      uploadTasks.push({ file: wechatQrFile.value, type: 'wechatQr' });
    if (alipayRewardQrFile.value)
      uploadTasks.push({
        file: alipayRewardQrFile.value,
        type: 'alipayRewardQr',
      });
    if (wechatRewardQrFile.value)
      uploadTasks.push({
        file: wechatRewardQrFile.value,
        type: 'wechatRewardQr',
      });

    // 批量上传文件
    if (uploadTasks.length > 0) {
      try {
        const files = uploadTasks.map((task) => task.file);
        const response = await uploadBatchFiles(files);

        if (response.code === 200 && response.data && Array.isArray(response.data)) {
          const uploadedUrls = response.data;

          // 检查上传结果数量是否匹配
          if (uploadedUrls.length !== uploadTasks.length) {
            Message.error(`文件上传失败：期望上传 ${uploadTasks.length} 个文件，实际返回 ${uploadedUrls.length} 个URL`);
            return;
          }

          // 将上传的URL分配给对应的字段
          uploadTasks.forEach((task, index) => {
            const url = uploadedUrls[index];
            if (!url) {
              Message.error(`文件上传失败：${task.type} 上传后未返回URL`);
              return;
            }
            if (task.type === 'logo') {
              websiteForm.logo = url;
            } else if (task.type === 'wechatQr') {
              websiteForm.wechatQrCode = url;
            } else if (task.type === 'alipayRewardQr') {
              websiteForm.alipayRewardQrCode = url;
            } else if (task.type === 'wechatRewardQr') {
              websiteForm.wechatRewardQrCode = url;
            }
          });
          
          Message.success(`成功上传 ${uploadedUrls.length} 个文件`);
        } else {
          Message.error('文件上传失败：服务器返回数据格式错误');
          return;
        }
      } catch (error: any) {
        console.error('文件上传失败:', error);
        Message.error(`文件上传失败：${error.message || '未知错误'}`);
        return;
      }
    }

    // 提交配置
    const response = await editWebsiteConfig(websiteForm);
    if (response.code === 200) {
      Message.success('网站配置保存成功');
      // 清空文件引用，避免重复上传
      logoFile.value = null;
      wechatQrFile.value = null;
      alipayRewardQrFile.value = null;
      wechatRewardQrFile.value = null;
      emit('update');
    } else {
      Message.error((response as any).message || '网站配置保存失败');
    }
  } catch (error: any) {
    console.error('保存网站配置失败:', error);
    Message.error(`保存网站配置失败：${error.message || '未知错误'}`);
  }
};

// 暴露方法供父组件调用
defineExpose({
  form: websiteForm,
  submit: handleSubmit,
});
</script>

<style scoped lang="less">
.logo-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;

  .logo-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .logo-uploader-icon {
    font-size: 24px;
    color: #8c8c8c;
  }
}

.qr-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;

  .qr-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .qr-uploader-icon {
    font-size: 24px;
    color: #8c8c8c;
  }
}

.reward-container {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.reward-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.reward-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;

  &.wechat {
    color: #07c160;
  }

  &.alipay {
    color: #1677ff;
  }
}

.reward-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;

  .reward-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .reward-uploader-icon {
    font-size: 24px;
    color: #8c8c8c;
  }
}

.reward-label {
  font-size: 12px;
  color: #8c8c8c;
}

.upload-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.form-section {
  margin-bottom: 20px;

  h4 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 500;
    color: #1d2129;
  }
}
</style>

