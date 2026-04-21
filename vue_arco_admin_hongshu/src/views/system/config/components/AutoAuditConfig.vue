<template>
  <a-form
    ref="autoAuditFormRef"
    style="margin-left: 20px"
    :model="autoAuditForm"
    :label-col-props="{ span: 4 }"
    :wrapper-col-props="{ span: 16 }"
  >
    <!-- 自动审核基础配置 -->
    <div class="form-section">
      <h4>自动审核基础配置</h4>
    </div>

    <!-- 审核模式 -->
    <a-form-item label="审核模式">
      <a-radio-group v-model="autoAuditForm.auditMode" class="audit-mode-radio">
        <a-radio value="manual">纯人工审核</a-radio>
        <a-radio value="auto">纯自动审核</a-radio>
        <a-radio value="hybrid">自动+人工混合</a-radio>
      </a-radio-group>
      <div class="form-tip">
        <div>• <strong>纯人工</strong>：所有内容进入人工审核队列</div>
        <div>• <strong>纯自动</strong>：完全依赖AI审核结果</div>
        <div>• <strong>混合模式</strong>：AI先审，疑似内容转人工复审</div>
      </div>
    </a-form-item>

    <!-- 自动审核阈值 -->
    <a-form-item label="自动审核阈值">
      <a-input-number
        v-model="autoAuditForm.autoAuditThreshold"
        :min="0"
        :max="1"
        :step="0.1"
        :precision="1"
        style="width: 200px"
      />
      <div class="form-tip">
        推荐范围:0.5-0.9,数值越大越严格,默认0.8
      </div>
    </a-form-item>

    <!-- 百度千帆审核配置 -->
    <div class="form-section" style="margin-top: 40px">
      <h4>百度千帆审核配置</h4>
    </div>

    <!-- 启用百度千帆 -->
    <a-form-item label="启用百度千帆">
      <a-switch v-model="autoAuditForm.enabled" />
      <div class="form-tip">
        启用后,将使用百度千帆内容审核API进行自动审核
      </div>
    </a-form-item>

    <!-- Access Key -->
    <a-form-item label="Access Key">
      <a-input
        v-model="autoAuditForm.accessKey"
        placeholder="请输入百度千帆Access Key"
        :show-password="false"
      />
    </a-form-item>

    <!-- Secret Key -->
    <a-form-item label="Secret Key">
      <a-input-password
        v-model="autoAuditForm.secretKey"
        placeholder="请输入百度千帆Secret Key"
      />
    </a-form-item>

    <!-- 文本审核接口 -->
    <a-form-item label="文本审核接口">
      <a-input
        v-model="autoAuditForm.textEndpoint"
        placeholder="请输入文本审核接口地址"
      />
    </a-form-item>

    <!-- 图片审核接口 -->
    <a-form-item label="图片审核接口">
      <a-input
        v-model="autoAuditForm.imageEndpoint"
        placeholder="请输入图片审核接口地址"
      />
    </a-form-item>

    <!-- 视频审核接口 -->
    <a-form-item label="视频审核接口">
      <a-input
        v-model="autoAuditForm.videoEndpoint"
        placeholder="请输入视频审核接口地址"
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
import { ref, reactive, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { editAutoAuditConfig } from '@/api/system/config';

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
const autoAuditFormRef = ref();

// 表单数据
const autoAuditForm = reactive({
  auditMode: 'manual', // 审核模式：manual(纯人工)、auto(纯自动)、hybrid(混合)
  autoAuditThreshold: 0.8, // 自动审核阈值
  enabled: false, // 是否启用百度千帆
  accessKey: '', // 百度千帆Access Key
  secretKey: '', // 百度千帆Secret Key
  textEndpoint: 'https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined', // 文本审核接口
  imageEndpoint: 'https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined', // 图片审核接口
  videoEndpoint: 'https://aip.baidubce.com/rest/2.0/solution/v1/video_censor/v2/user_defined', // 视频审核接口
});

// 不再需要单独加载配置，通过 props.initialData 接收数据

// 监听初始数据变化（从 allConfig 接口获取）
watch(
  () => props.initialData,
  (newData) => {
    if (newData && Object.keys(newData).length > 0) {
      Object.assign(autoAuditForm, {
        auditMode: newData.auditMode || 'manual',
        autoAuditThreshold: newData.autoAuditThreshold || 0.8,
        enabled: newData.enabled !== undefined ? newData.enabled : (newData.baiduQianfanEnabled || false),
        accessKey: newData.accessKey || newData.baiduQianfanAccessKey || '',
        secretKey: newData.secretKey || newData.baiduQianfanSecretKey || '',
        textEndpoint: newData.textEndpoint || newData.baiduQianfanTextEndpoint || 'https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined',
        imageEndpoint: newData.imageEndpoint || newData.baiduQianfanImageEndpoint || 'https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined',
        videoEndpoint: newData.videoEndpoint || newData.baiduQianfanVideoEndpoint || 'https://aip.baidubce.com/rest/2.0/solution/v1/video_censor/v2/user_defined',
      });
    }
  },
  { immediate: true, deep: true }
);

// 提交表单
const handleSubmit = async (): Promise<void> => {
  try {
    // 确保审核模式和阈值始终有值
    const submitData = {
      auditMode: autoAuditForm.auditMode || 'manual',
      autoAuditThreshold: autoAuditForm.autoAuditThreshold ?? 0.8,
      enabled: autoAuditForm.enabled ?? false,
      accessKey: autoAuditForm.accessKey || '',
      secretKey: autoAuditForm.secretKey || '',
      textEndpoint: autoAuditForm.textEndpoint || '',
      imageEndpoint: autoAuditForm.imageEndpoint || '',
      videoEndpoint: autoAuditForm.videoEndpoint || '',
    };
    console.log('提交自动审核配置数据:', submitData);
    const response = await editAutoAuditConfig(submitData);
    if (response.code === 200) {
      Message.success('自动审核配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || '自动审核配置保存失败');
    }
  } catch (error) {
    console.error('保存自动审核配置失败:', error);
    Message.error('自动审核配置保存失败');
  }
};

// 暴露方法供父组件调用
defineExpose({
  form: autoAuditForm,
  submit: handleSubmit,
});
</script>

<style scoped lang="less">
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
  line-height: 1.6;
}

.audit-mode-radio {
  margin-bottom: 8px;
}

.audit-mode-radio :deep(.arco-radio) {
  margin-right: 24px;
}
</style>

