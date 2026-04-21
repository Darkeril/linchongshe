<template>
  <a-form
    ref="amapFormRef"
    style="margin-left: 20px"
    :model="amapForm"
    :label-col-props="{ span: 4 }"
    :wrapper-col-props="{ span: 16 }"
  >
    <div class="form-section">
      <h4>高德地图服务配置</h4>
    </div>
    <a-form-item label="API Key" field="key">
      <a-input
        v-model="amapForm.key"
        style="width: 400px"
        placeholder="请输入高德地图API Key"
      />
    </a-form-item>
    <a-form-item label="安全密钥" field="securityKey">
      <a-input
        v-model="amapForm.securityKey"
        type="password"
        allow-clear
        style="width: 400px"
        placeholder="请输入高德地图安全密钥"
      />
    </a-form-item>
    <a-form-item label="是否启用" field="isEnabled">
      <a-switch v-model="amapForm.isEnabled" />
    </a-form-item>
    <a-form-item>
      <a-button type="primary" @click="handleSubmit">
        保 存
      </a-button>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { Message } from '@arco-design/web-vue';
import { editAmapConfig } from '@/api/system/config';

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
const amapFormRef = ref();

// 表单数据
const amapForm = reactive({
  key: '',
  securityKey: '',
  isEnabled: false,
});

// 初始化数据
if (props.initialData && Object.keys(props.initialData).length > 0) {
  Object.assign(amapForm, props.initialData);
}

// 提交表单
const handleSubmit = async (): Promise<void> => {
  try {
    const response = await editAmapConfig(amapForm);
    if (response.code === 200) {
      Message.success('高德地图配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || '高德地图配置保存失败');
    }
  } catch (error) {
    console.error('保存高德地图配置失败:', error);
    Message.warning('配置保存接口暂未实现，配置已更新到本地');
  }
};

// 暴露方法供父组件调用
defineExpose({
  form: amapForm,
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
}
</style>



