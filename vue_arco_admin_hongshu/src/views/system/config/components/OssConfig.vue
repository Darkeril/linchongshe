<template>
  <div>
    <!-- OSS服务商选择 -->
    <div class="provider-selector">
      <a-radio-group
        v-model="currentProvider"
        type="button"
        @change="handleProviderChange"
      >
        <a-radio value="local">本地存储</a-radio>
        <a-radio value="minio">Minio</a-radio>
        <a-radio value="qiniuyun">七牛云</a-radio>
        <a-radio value="aliyun">阿里云</a-radio>
        <a-radio value="tencent">腾讯云</a-radio>
      </a-radio-group>
    </div>

    <!-- 本地存储配置 -->
    <a-form
      v-if="currentProvider === 'local'"
      ref="localFormRef"
      style="margin-left: 20px"
      :model="ossForm.local"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>使用IO流将文件存储本地磁盘中</h4>
      </div>
      <a-form-item label="本地文件域名" field="domain">
        <a-input
          v-model="ossForm.local.domain"
          style="width: 400px"
          placeholder="请输入本地文件访问域名，如：http://localhost:8080"
        />
      </a-form-item>
      <a-form-item label="文件上传路径" field="url">
        <a-input
          v-model="ossForm.local.url"
          style="width: 400px"
          placeholder="请输入文件存储路径，如：/data/docker/nginx/html/data"
        />
        <div class="form-tip">注意：请确保该路径存在且有写入权限</div>
      </a-form-item>
      <a-form-item label="是否启用">
        <a-switch v-model="ossForm.local.isEnabled" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmit">保 存</a-button>
      </a-form-item>
    </a-form>

    <!-- Minio配置 -->
    <a-form
      v-if="currentProvider === 'minio'"
      ref="minioFormRef"
      style="margin-left: 20px"
      :model="ossForm.minio"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>Minio对象存储配置</h4>
      </div>
      <a-form-item label="访问地址" field="domain">
        <a-input
          v-model="ossForm.minio.domain"
          style="width: 400px"
          placeholder="请输入Minio访问地址"
        />
      </a-form-item>
      <a-form-item label="Access Key" field="accessKey">
        <a-input
          v-model="ossForm.minio.accessKey"
          style="width: 400px"
          placeholder="请输入Access Key"
        />
      </a-form-item>
      <a-form-item label="Secret Key" field="secretKey">
        <a-input-password
          v-model="ossForm.minio.secretKey"
          allow-clear
          style="width: 400px"
          placeholder="请输入Secret Key"
        />
      </a-form-item>
      <a-form-item label="存储桶名称" field="bucketName">
        <a-input
          v-model="ossForm.minio.bucketName"
          style="width: 400px"
          placeholder="请输入存储桶名称"
        />
      </a-form-item>
      <a-form-item label="是否启用">
        <a-switch v-model="ossForm.minio.isEnabled" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmit">保 存</a-button>
      </a-form-item>
    </a-form>

    <!-- 七牛云配置 -->
    <a-form
      v-if="currentProvider === 'qiniuyun'"
      ref="qiniuFormRef"
      style="margin-left: 20px"
      :model="ossForm.qiniuyun"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>七牛云对象存储配置</h4>
      </div>
      <a-form-item label="域名" field="domain">
        <a-input
          v-model="ossForm.qiniuyun.domain"
          style="width: 400px"
          placeholder="请输入七牛云域名"
        />
      </a-form-item>
      <a-form-item label="Access Key" field="accessKey">
        <a-input
          v-model="ossForm.qiniuyun.accessKey"
          style="width: 400px"
          placeholder="请输入Access Key"
        />
      </a-form-item>
      <a-form-item label="Secret Key" field="secretKey">
        <a-input-password
          v-model="ossForm.qiniuyun.secretKey"
          allow-clear
          style="width: 400px"
          placeholder="请输入Secret Key"
        />
      </a-form-item>
      <a-form-item label="存储桶名称" field="bucketName">
        <a-input
          v-model="ossForm.qiniuyun.bucketName"
          style="width: 400px"
          placeholder="请输入存储桶名称"
        />
      </a-form-item>
      <a-form-item label="存储区域" field="region">
        <a-select
          v-model="ossForm.qiniuyun.region"
          style="width: 400px"
          placeholder="请选择与七牛空间一致的机房"
          allow-clear
        >
          <a-option value="z1">华北（z1）</a-option>
          <a-option value="z0">华东（z0）</a-option>
          <a-option value="z2">华南（z2）</a-option>
          <a-option value="na0">北美（na0）</a-option>
          <a-option value="as0">东南亚-新加坡（as0）</a-option>
          <a-option value="fog-cn-east-1">雾存储华东-1</a-option>
          <a-option value="auto">自动查询（按 Bucket）</a-option>
        </a-select>
        <div class="form-tip">
          须与七牛控制台该存储空间所在区域一致；留空或未选时后端默认按华北（z1）处理，与历史行为一致。
        </div>
      </a-form-item>
      <a-form-item label="是否启用">
        <a-switch v-model="ossForm.qiniuyun.isEnabled" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmit">保 存</a-button>
      </a-form-item>
    </a-form>

    <!-- 阿里云配置 -->
    <a-form
      v-if="currentProvider === 'aliyun'"
      ref="aliyunFormRef"
      style="margin-left: 20px"
      :model="ossForm.aliyun"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>阿里云对象存储配置</h4>
      </div>
      <a-form-item label="存储桶名称" field="bucketName">
        <a-input
          v-model="ossForm.aliyun.bucketName"
          style="width: 400px"
          placeholder="请输入存储桶名称"
        />
      </a-form-item>
      <a-form-item label="端点地址" field="endpoint">
        <a-input
          v-model="ossForm.aliyun.endpoint"
          style="width: 400px"
          placeholder="请输入端点地址"
        />
      </a-form-item>
      <a-form-item label="地理区域" field="region">
        <a-input
          v-model="ossForm.aliyun.region"
          style="width: 400px"
          placeholder="请输入地理区域"
        />
      </a-form-item>
      <a-form-item label="Access Key ID" field="accessKeyId">
        <a-input
          v-model="ossForm.aliyun.accessKeyId"
          style="width: 400px"
          placeholder="请输入Access Key ID"
        />
      </a-form-item>
      <a-form-item label="Access Key Secret" field="accessKeySecret">
        <a-input-password
          v-model="ossForm.aliyun.accessKeySecret"
          allow-clear
          style="width: 400px"
          placeholder="请输入Access Key Secret"
        />
      </a-form-item>
      <a-form-item label="是否启用">
        <a-switch v-model="ossForm.aliyun.isEnabled" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmit">保 存</a-button>
      </a-form-item>
    </a-form>

    <!-- 腾讯云配置 -->
    <a-form
      v-if="currentProvider === 'tencent'"
      ref="tencentFormRef"
      style="margin-left: 20px"
      :model="ossForm.tencent"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>腾讯云对象存储配置</h4>
      </div>
      <a-form-item label="存储桶名称" field="bucketName">
        <a-input
          v-model="ossForm.tencent.bucketName"
          style="width: 400px"
          placeholder="请输入存储桶名称"
        />
      </a-form-item>
      <a-form-item label="端点地址" field="endpoint">
        <a-input
          v-model="ossForm.tencent.endpoint"
          style="width: 400px"
          placeholder="请输入端点地址"
        />
      </a-form-item>
      <a-form-item label="地理区域" field="region">
        <a-input
          v-model="ossForm.tencent.region"
          style="width: 400px"
          placeholder="请输入地理区域"
        />
      </a-form-item>
      <a-form-item label="Secret ID" field="ossKeyId">
        <a-input
          v-model="ossForm.tencent.ossKeyId"
          style="width: 400px"
          placeholder="请输入Secret ID"
        />
      </a-form-item>
      <a-form-item label="Secret Key" field="ossKeySecret">
        <a-input-password
          v-model="ossForm.tencent.ossKeySecret"
          allow-clear
          style="width: 400px"
          placeholder="请输入Secret Key"
        />
      </a-form-item>
      <a-form-item label="是否启用">
        <a-switch v-model="ossForm.tencent.isEnabled" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmit">保 存</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { editOssConfig } from '@/api/system/config';

// Props
interface Props {
  initialData?: any;
  initialProvider?: string;
}

const props = withDefaults(defineProps<Props>(), {
  initialData: () => ({}),
  initialProvider: 'local',
});

// Emits
const emit = defineEmits<{
  (e: 'update'): void;
}>();

// 表单引用
const localFormRef = ref();
const minioFormRef = ref();
const qiniuFormRef = ref();
const aliyunFormRef = ref();
const tencentFormRef = ref();

// 当前选中的服务商
const currentProvider = ref<string>(props.initialProvider);

// OSS配置表单
const ossForm = reactive({
  local: {
    domain: '',
    url: '',
    isEnabled: false,
  },
  qiniuyun: {
    domain: '',
    accessKey: '',
    secretKey: '',
    bucketName: '',
    region: 'z1',
    isEnabled: false,
  },
  minio: {
    domain: '',
    accessKey: '',
    secretKey: '',
    bucketName: '',
    isEnabled: false,
  },
  tencent: {
    bucketName: '',
    endpoint: '',
    region: '',
    ossKeyId: '',
    ossKeySecret: '',
    isEnabled: false,
  },
  aliyun: {
    bucketName: '',
    endpoint: '',
    region: '',
    accessKeyId: '',
    accessKeySecret: '',
    isEnabled: false,
  },
});

// 初始化数据
if (props.initialData && Object.keys(props.initialData).length > 0) {
  Object.assign(ossForm, props.initialData);
}

// 监听初始服务商变化
watch(
  () => props.initialProvider,
  (newVal) => {
    if (newVal) {
      currentProvider.value = newVal;
    }
  },
  { immediate: true }
);

// 服务商切换处理
const handleProviderChange = (value: string): void => {
  currentProvider.value = value;
};

// 提交表单
const handleSubmit = async (): Promise<void> => {
  try {
    const currentConfig =
      ossForm[currentProvider.value as keyof typeof ossForm];
    const response = await editOssConfig({
      provider: currentProvider.value,
      config: currentConfig,
    });

    if (response.code === 200) {
      Message.success('OSS配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || 'OSS配置保存失败');
    }
  } catch (error) {
    console.error('保存OSS配置失败:', error);
    Message.warning('配置保存接口暂未实现，配置已更新到本地');
  }
};

// 暴露方法供父组件调用
defineExpose({
  form: ossForm,
  currentProvider,
  submit: handleSubmit,
});
</script>

<style scoped lang="less">
.provider-selector {
  margin: 20px 0 20px 20px;
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

.form-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}
</style>



