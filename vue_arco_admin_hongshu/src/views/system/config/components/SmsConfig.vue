<template>
  <div>
    <!-- 短信服务商选择 -->
    <div class="provider-selector">
      <a-radio-group
        v-model="currentProvider"
        type="button"
        @change="handleProviderChange"
      >
        <a-radio value="aliyun">阿里云</a-radio>
        <a-radio value="tencent">腾讯云</a-radio>
        <a-radio value="yunpian">云片</a-radio>
      </a-radio-group>
    </div>

    <!-- 阿里云短信配置 -->
    <a-form
      v-if="currentProvider === 'aliyun'"
      ref="aliyunFormRef"
      style="margin-left: 20px"
      :model="smsForm.aliyun"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>阿里云短信服务配置</h4>
      </div>
      <a-form-item label="Access Key" field="accessKey">
        <a-input
          v-model="smsForm.aliyun.accessKey"
          style="width: 400px"
          placeholder="请输入阿里云Access Key"
        />
      </a-form-item>
      <a-form-item label="Secret Key" field="secretKey">
        <a-input-password
          v-model="smsForm.aliyun.secretKey"
          allow-clear
          style="width: 400px"
          placeholder="请输入阿里云Secret Key"
        />
      </a-form-item>
      <a-form-item label="签名名称" field="signName">
        <a-input
          v-model="smsForm.aliyun.signName"
          style="width: 400px"
          placeholder="请输入短信签名"
        />
      </a-form-item>
      <a-form-item label="模板代码" field="templateCode">
        <a-input
          v-model="smsForm.aliyun.templateCode"
          style="width: 400px"
          placeholder="请输入短信模板代码"
        />
      </a-form-item>
      <a-form-item label="是否主配置">
        <a-switch v-model="smsForm.aliyun.isPrimary" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmit">保 存</a-button>
      </a-form-item>
    </a-form>

    <!-- 腾讯云短信配置 -->
    <a-form
      v-if="currentProvider === 'tencent'"
      ref="tencentFormRef"
      style="margin-left: 20px"
      :model="smsForm.tencent"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>腾讯云短信服务配置</h4>
      </div>
      <a-form-item label="Secret ID" field="secretId">
        <a-input
          v-model="smsForm.tencent.secretId"
          style="width: 400px"
          placeholder="请输入腾讯云Secret ID"
        />
      </a-form-item>
      <a-form-item label="Secret Key" field="secretKey">
        <a-input-password
          v-model="smsForm.tencent.secretKey"
          allow-clear
          style="width: 400px"
          placeholder="请输入腾讯云Secret Key"
        />
      </a-form-item>
      <a-form-item label="SDK App ID" field="sdkAppId">
        <a-input
          v-model="smsForm.tencent.sdkAppId"
          style="width: 400px"
          placeholder="请输入SDK App ID"
        />
      </a-form-item>
      <a-form-item label="签名名称" field="signName">
        <a-input
          v-model="smsForm.tencent.signName"
          style="width: 400px"
          placeholder="请输入短信签名"
        />
      </a-form-item>
      <a-form-item label="模板ID" field="templateId">
        <a-input
          v-model="smsForm.tencent.templateId"
          style="width: 400px"
          placeholder="请输入短信模板ID"
        />
      </a-form-item>
      <a-form-item label="区域" field="region">
        <a-input
          v-model="smsForm.tencent.region"
          style="width: 400px"
          placeholder="请输入区域"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmit">保 存</a-button>
      </a-form-item>
    </a-form>

    <!-- 云片短信配置 -->
    <a-form
      v-if="currentProvider === 'yunpian'"
      ref="yunpianFormRef"
      style="margin-left: 20px"
      :model="smsForm.yunpian"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>云片短信服务配置</h4>
      </div>
      <a-form-item label="API Key" field="apiKey">
        <a-input
          v-model="smsForm.yunpian.apiKey"
          style="width: 400px"
          placeholder="请输入云片API Key"
        />
      </a-form-item>
      <a-form-item label="签名名称" field="signName">
        <a-input
          v-model="smsForm.yunpian.signName"
          style="width: 400px"
          placeholder="请输入短信签名"
        />
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
import { editSmsConfig } from '@/api/system/config';

// Props
interface Props {
  initialData?: any;
  initialProvider?: string;
}

const props = withDefaults(defineProps<Props>(), {
  initialData: () => ({}),
  initialProvider: 'aliyun',
});

// Emits
const emit = defineEmits<{
  (e: 'update'): void;
}>();

// 表单引用
const aliyunFormRef = ref();
const tencentFormRef = ref();
const yunpianFormRef = ref();

// 当前选中的服务商
const currentProvider = ref<string>(props.initialProvider);

// 短信配置表单
const smsForm = reactive({
  aliyun: {
    accessKey: '',
    secretKey: '',
    signName: '',
    templateCode: '',
    isPrimary: true,
  },
  tencent: {
    secretId: '',
    secretKey: '',
    sdkAppId: '',
    signName: '',
    templateId: '',
    region: 'ap-beijing',
  },
  yunpian: {
    apiKey: '',
    signName: '',
  },
});

// 初始化数据
if (props.initialData && Object.keys(props.initialData).length > 0) {
  Object.assign(smsForm, props.initialData);
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
      smsForm[currentProvider.value as keyof typeof smsForm];
    const response = await editSmsConfig({
      provider: currentProvider.value,
      config: currentConfig,
    });

    if (response.code === 200) {
      Message.success('短信配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || '短信配置保存失败');
    }
  } catch (error) {
    console.error('保存短信配置失败:', error);
    Message.warning('配置保存接口暂未实现，配置已更新到本地');
  }
};

// 暴露方法供父组件调用
defineExpose({
  form: smsForm,
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



