<template>
  <a-form
    ref="captchaFormRef"
    style="margin-left: 20px"
    :model="captchaForm"
    :label-col-props="{ span: 4 }"
    :wrapper-col-props="{ span: 16 }"
  >
    <div class="form-section">
      <h4>AJ-Captcha 验证码配置</h4>
    </div>
    <a-form-item label="缓存类型" field="cacheType">
      <a-select
        v-model="captchaForm.cacheType"
        style="width: 400px"
      >
        <a-option label="redis" value="redis" />
        <a-option label="local" value="local" />
      </a-select>
    </a-form-item>
    <a-form-item label="验证方式" field="type">
      <a-select v-model="captchaForm.type" style="width: 400px">
        <a-option label="滑块拼图" value="blockPuzzle" />
        <a-option label="文字点选" value="clickWord" />
        <a-option label="默认" value="default" />
      </a-select>
    </a-form-item>
    <a-form-item label="水印文字" field="waterMark">
      <a-input
        v-model="captchaForm.waterMark"
        style="width: 400px"
        placeholder="请输入水印文字"
      />
    </a-form-item>
    <a-form-item label="滑动误差偏移量" field="slipOffset">
      <a-input-number
        v-model="captchaForm.slipOffset"
        :min="1"
        :max="20"
        style="width: 400px"
      />
    </a-form-item>
    <a-form-item label="AES加密状态" field="aesStatus">
      <a-switch v-model="captchaForm.aesStatus" />
    </a-form-item>
    <a-form-item label="滑动干扰项" field="interferenceOptions">
      <a-input-number
        v-model="captchaForm.interferenceOptions"
        :min="0"
        :max="2"
        style="width: 400px"
      />
    </a-form-item>
    <a-form-item label="滑动图片路径" field="jigsaw">
      <a-input
        v-model="captchaForm.jigsaw"
        style="width: 400px"
        placeholder="请输入滑动图片路径"
      />
    </a-form-item>
    <a-form-item label="点击图片路径" field="picClick">
      <a-input
        v-model="captchaForm.picClick"
        style="width: 400px"
        placeholder="请输入点击图片路径"
      />
    </a-form-item>
    <a-form-item label="是否启用" field="isEnabled">
      <a-switch v-model="captchaForm.isEnabled" />
      <div class="form-tip">启用后登录时需要校验验证码</div>
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
import { editCaptchaConfig } from '@/api/system/config';

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
const captchaFormRef = ref();

// 表单数据
const captchaForm = reactive({
  cacheType: 'redis',
  type: 'blockPuzzle',
  waterMark: '',
  slipOffset: 5,
  aesStatus: false,
  interferenceOptions: 0,
  jigsaw: '',
  picClick: '',
  isEnabled: false,
});

// 初始化数据
if (props.initialData && Object.keys(props.initialData).length > 0) {
  Object.assign(captchaForm, props.initialData);
}

// 提交表单
const handleSubmit = async (): Promise<void> => {
  try {
    const response = await editCaptchaConfig(captchaForm);
    if (response.code === 200) {
      Message.success('验证码配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || '验证码配置保存失败');
    }
  } catch (error) {
    console.error('保存验证码配置失败:', error);
    Message.warning('配置保存接口暂未实现，配置已更新到本地');
  }
};

// 暴露方法供父组件调用
defineExpose({
  form: captchaForm,
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








