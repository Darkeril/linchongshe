<template>
  <div>
    <!-- 支付全局配置 -->
    <div class="payment-global-config">
      <h3 style="margin: 20px 0 20px 20px">支付全局配置</h3>
      <a-form
        style="margin-left: 20px"
        :label-col-props="{ span: 4 }"
        :wrapper-col-props="{ span: 16 }"
      >
        <a-form-item label="全局支付开关">
          <a-switch v-model="globalConfig.paymentEnabled">
            <template #checked>开启</template>
            <template #unchecked>关闭</template>
          </a-switch>
          <div style="color: #86909c; font-size: 12px; margin-top: 8px">
            关闭后，整个系统（Web端和UniApp端）将无法进行支付操作
          </div>
        </a-form-item>

        <a-form-item label="支付方式选择">
          <a-space direction="vertical" fill>
            <a-checkbox v-model="globalConfig.wechatQrcodeEnabled">
              微信扫码支付
            </a-checkbox>
            <a-checkbox v-model="globalConfig.wechatMiniprogramEnabled">
              微信小程序支付
            </a-checkbox>
            <a-checkbox v-model="globalConfig.alipaySandboxEnabled">
              支付宝沙箱支付（开发测试用）
            </a-checkbox>
            <a-checkbox v-model="globalConfig.alipayQrcodeEnabled">
              支付宝扫码支付（生产用）
            </a-checkbox>
          </a-space>
          <div style="color: #86909c; font-size: 12px; margin-top: 8px">
            勾选后用户可以使用对应的支付方式，未勾选的支付方式将无法使用
          </div>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" @click="handleSubmitGlobalConfig"
            >保存配置</a-button
          >
        </a-form-item>
      </a-form>
      <a-divider />
    </div>

    <!-- 支付方式详细配置 -->
    <h3 style="margin: 20px 0 20px 20px">🔧 支付方式详细配置</h3>
    <div class="provider-selector">
      <a-radio-group v-model="currentProvider" type="button">
        <a-radio value="alipay">
          <icon-alipay-circle />
          支付宝支付配置
        </a-radio>
        <a-radio value="wechatpay">
          <icon-wechatpay />
          微信支付配置
        </a-radio>
      </a-radio-group>
    </div>

    <!-- 支付宝支付配置 -->
    <a-form
      v-if="currentProvider === 'alipay'"
      ref="alipayFormRef"
      style="margin-left: 20px"
      :model="alipayForm"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>支付宝沙箱支付配置</h4>
      </div>
      <a-form-item label="商户PID" field="pid">
        <a-input
          v-model="alipayForm.pid"
          style="width: 400px"
          placeholder="请输入商户PID"
        />
      </a-form-item>
      <a-form-item label="应用ID" field="appId">
        <a-input
          v-model="alipayForm.appId"
          style="width: 400px"
          placeholder="请输入应用ID"
        />
      </a-form-item>
      <a-form-item label="商户私钥" field="merchantPrivateKey">
        <a-textarea
          v-model="alipayForm.merchantPrivateKey"
          :rows="4"
          style="width: 400px"
          placeholder="请输入商户私钥"
        />
      </a-form-item>
      <a-form-item label="支付宝公钥" field="alipayPublicKey">
        <a-textarea
          v-model="alipayForm.alipayPublicKey"
          :rows="4"
          style="width: 400px"
          placeholder="请输入支付宝公钥"
        />
      </a-form-item>
      <a-form-item label="异步通知地址" field="notifyUrl">
        <a-input
          v-model="alipayForm.notifyUrl"
          style="width: 400px"
          placeholder="请输入异步通知地址"
        />
      </a-form-item>
      <a-form-item label="同步返回地址" field="returnUrl">
        <a-input
          v-model="alipayForm.returnUrl"
          style="width: 400px"
          placeholder="请输入同步返回地址"
        />
      </a-form-item>
      <a-form-item label="签名类型" field="signType">
        <a-input
          v-model="alipayForm.signType"
          style="width: 400px"
          placeholder="请输入签名类型"
        />
      </a-form-item>
      <a-form-item label="字符集" field="charset">
        <a-input
          v-model="alipayForm.charset"
          style="width: 400px"
          placeholder="请输入字符集"
        />
      </a-form-item>
      <a-form-item label="网关地址" field="gatewayUrl">
        <a-input
          v-model="alipayForm.gatewayUrl"
          style="width: 400px"
          placeholder="请输入网关地址"
        />
      </a-form-item>
      <a-form-item label="是否启用">
        <a-switch v-model="alipayForm.isEnabled" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmitAlipay">保 存</a-button>
      </a-form-item>
    </a-form>

    <!-- 微信支付配置 -->
    <a-form
      v-if="currentProvider === 'wechatpay'"
      ref="weChatPayFormRef"
      style="margin-left: 20px"
      :model="weChatPayForm"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>微信支付配置</h4>
      </div>
      <a-form-item label="商户号" field="mchId">
        <a-input
          v-model="weChatPayForm.mchId"
          style="width: 400px"
          placeholder="请输入商户号"
        />
      </a-form-item>
      <a-form-item label="PC端应用ID" field="pcAppId">
        <a-input
          v-model="weChatPayForm.pcAppId"
          style="width: 400px"
          placeholder="请输入PC端应用ID"
        />
      </a-form-item>
      <a-form-item label="APP应用ID" field="appAppId">
        <a-input
          v-model="weChatPayForm.appAppId"
          style="width: 400px"
          placeholder="请输入APP应用ID"
        />
      </a-form-item>
      <a-form-item label="小程序应用ID" field="appId">
        <a-input
          v-model="weChatPayForm.appId"
          style="width: 400px"
          placeholder="请输入小程序应用ID"
        />
      </a-form-item>
      <a-form-item label="APIv2密钥" field="apiV2Key">
        <a-input
          v-model="weChatPayForm.apiV2Key"
          style="width: 400px"
          placeholder="请输入APIv2密钥"
        />
      </a-form-item>
      <a-form-item label="APIv3密钥" field="apiV3Key">
        <a-input
          v-model="weChatPayForm.apiV3Key"
          style="width: 400px"
          placeholder="请输入APIv3密钥"
        />
      </a-form-item>
      <a-form-item label="商户证书序列号" field="certSerialNo">
        <a-input
          v-model="weChatPayForm.certSerialNo"
          style="width: 400px"
          placeholder="请输入商户证书序列号"
        />
      </a-form-item>
      <a-form-item label="商户私钥路径" field="privateKeyPath">
        <a-input
          v-model="weChatPayForm.privateKeyPath"
          style="width: 400px"
          placeholder="请输入商户私钥路径"
        />
      </a-form-item>
      <a-form-item label="微信服务器地址" field="domain">
        <a-input
          v-model="weChatPayForm.domain"
          style="width: 400px"
          placeholder="请输入微信服务器地址"
        />
      </a-form-item>
      <a-form-item label="异步通知地址" field="notifyUrl">
        <a-input
          v-model="weChatPayForm.notifyUrl"
          style="width: 400px"
          placeholder="请输入异步通知地址"
        />
      </a-form-item>
      <a-form-item label="是否启用">
        <a-switch v-model="weChatPayForm.isEnabled" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmitWeChatPay">保 存</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconAlipayCircle, IconWechatpay } from '@arco-design/web-vue/es/icon';
import {
  editAlipayConfig,
  editWeChatPayConfig,
  getPaymentGlobalConfig,
  editPaymentGlobalConfig,
} from '@/api/system/config';

// Props
interface Props {
  initialAlipayData?: any;
  initialWeChatPayData?: any;
  initialProvider?: string;
}

const props = withDefaults(defineProps<Props>(), {
  initialAlipayData: () => ({}),
  initialWeChatPayData: () => ({}),
  initialProvider: 'alipay',
});

// Emits
const emit = defineEmits<{
  (e: 'update'): void;
}>();

// 表单引用
const alipayFormRef = ref();
const weChatPayFormRef = ref();

// 当前选中的支付方式
const currentProvider = ref<string>(props.initialProvider);

// 支付全局配置
const globalConfig = reactive({
  paymentEnabled: false,
  wechatQrcodeEnabled: false,
  wechatMiniprogramEnabled: false,
  alipaySandboxEnabled: false,
  alipayQrcodeEnabled: false,
});

// 支付宝配置表单
const alipayForm = reactive({
  pid: '',
  appId: '',
  merchantPrivateKey: '',
  alipayPublicKey: '',
  notifyUrl: '',
  returnUrl: '',
  signType: 'RSA2',
  charset: 'utf-8',
  gatewayUrl: 'https://openapi.alipaydev.com/gateway.do',
  isEnabled: false,
});

// 微信支付配置表单
const weChatPayForm = reactive({
  mchId: '',
  pcAppId: '',
  appAppId: '',
  appId: '',
  apiV2Key: '',
  apiV3Key: '',
  certSerialNo: '',
  privateKeyPath: '',
  domain: '',
  notifyUrl: '',
  isEnabled: false,
});

// 初始化数据
if (
  props.initialAlipayData &&
  Object.keys(props.initialAlipayData).length > 0
) {
  Object.assign(alipayForm, props.initialAlipayData);
}

if (
  props.initialWeChatPayData &&
  Object.keys(props.initialWeChatPayData).length > 0
) {
  Object.assign(weChatPayForm, props.initialWeChatPayData);
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

// 提交支付宝配置
const handleSubmitAlipay = async (): Promise<void> => {
  try {
    const response = await editAlipayConfig(alipayForm);
    if (response.code === 200) {
      Message.success('支付宝配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || '支付宝配置保存失败');
    }
  } catch (error) {
    console.error('保存支付宝配置失败:', error);
    Message.warning('配置保存接口暂未实现，配置已更新到本地');
  }
};

// 提交微信支付配置
const handleSubmitWeChatPay = async (): Promise<void> => {
  try {
    const response = await editWeChatPayConfig(weChatPayForm);
    if (response.code === 200) {
      Message.success('微信支付配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || '微信支付配置保存失败');
    }
  } catch (error) {
    console.error('保存微信支付配置失败:', error);
    Message.warning('配置保存接口暂未实现，配置已更新到本地');
  }
};

// 加载支付全局配置
const loadGlobalConfig = async (): Promise<void> => {
  try {
    const response = await getPaymentGlobalConfig();
    if (response.code === 200 && response.data) {
      Object.assign(globalConfig, response.data);
    }
  } catch (error) {
    console.error('加载支付全局配置失败:', error);
  }
};

// 提交支付全局配置
const handleSubmitGlobalConfig = async (): Promise<void> => {
  try {
    const response = await editPaymentGlobalConfig(globalConfig);
    if (response.code === 200) {
      Message.success('支付全局配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || '支付全局配置保存失败');
    }
  } catch (error) {
    console.error('保存支付全局配置失败:', error);
    Message.error('保存支付全局配置失败');
  }
};

// 组件挂载时加载全局配置
onMounted(() => {
  loadGlobalConfig();
});

// 暴露方法供父组件调用
defineExpose({
  alipayForm,
  weChatPayForm,
  currentProvider,
  submitAlipay: handleSubmitAlipay,
  submitWeChatPay: handleSubmitWeChatPay,
  loadGlobalConfig,
  submitGlobalConfig: handleSubmitGlobalConfig,
});
</script>

<style scoped lang="less">
.payment-global-config {
  background-color: #f7f8fa;
  padding: 10px 0;
  border-radius: 4px;
  margin: 20px;
}

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






