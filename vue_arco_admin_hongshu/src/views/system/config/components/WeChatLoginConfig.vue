<template>
  <div class="wechat-login-page">
    <a-form
      ref="wechatLoginFormRef"
      style="margin-left: 20px"
      :model="wechatLoginForm"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>主站微信登录（uniapp_hongshu / H5 / 网页）</h4>
        <p class="section-desc">
          用于社区主站小程序、H5 与网页端微信登录；与下方 IM
          小程序凭证相互独立。
        </p>
      </div>
      <a-form-item label="微信登录功能">
        <a-switch v-model="wechatLoginForm.enabled" />
        <div class="form-tip">启用后前端登录页面将显示微信登录按钮</div>
      </a-form-item>

      <div class="form-section" style="margin-top: 24px">
        <h4>主站微信小程序</h4>
      </div>

      <a-form-item label="小程序AppID" field="miniappAppId">
        <a-input
          v-model="wechatLoginForm.miniappAppId"
          style="width: 400px"
          placeholder="请输入微信小程序AppID"
        />
        <div class="form-tip">用于 uniapp_hongshu 等主站小程序登录</div>
      </a-form-item>

      <a-form-item label="小程序AppSecret" field="miniappAppSecret">
        <a-input-password
          v-model="wechatLoginForm.miniappAppSecret"
          style="width: 400px"
          placeholder="请输入微信小程序AppSecret"
          allow-clear
        />
        <div class="form-tip">用于主站小程序登录，请妥善保管</div>
      </a-form-item>

      <div class="form-section" style="margin-top: 24px">
        <h4>微信网页应用</h4>
      </div>

      <a-form-item label="网页应用AppID" field="webAppId">
        <a-input
          v-model="wechatLoginForm.webAppId"
          style="width: 400px"
          placeholder="请输入微信网页应用AppID"
        />
        <div class="form-tip">用于网页微信登录功能</div>
      </a-form-item>

      <a-form-item label="网页应用AppSecret" field="webAppSecret">
        <a-input-password
          v-model="wechatLoginForm.webAppSecret"
          style="width: 400px"
          placeholder="请输入微信网页应用AppSecret"
          allow-clear
        />
        <div class="form-tip">用于网页微信登录功能，请妥善保管</div>
      </a-form-item>

      <a-form-item label="网页应用回调地址" field="webCallbackUrl">
        <a-input
          v-model="wechatLoginForm.webCallbackUrl"
          style="width: 500px"
          placeholder="请输入网页应用回调地址，例如：https://hongshu.website/web/auth/wechat/callback"
        />
        <div class="form-tip">
          回调地址必须与微信开放平台配置的授权回调域名一致，且包含完整路径
        </div>
      </a-form-item>

      <a-form-item>
        <a-button type="primary" @click="handleSubmit"> 保存主站配置 </a-button>
      </a-form-item>
    </a-form>

    <!-- <a-divider style="margin: 8px 20px 24px" />

    <a-form
      ref="imWechatLoginFormRef"
      style="margin-left: 20px"
      :model="imWechatForm"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>IM 小程序微信登录（uniapp_im）</h4>
        <p class="section-desc">
          独立 AppID / Secret，须与 uniapp_im 项目 manifest 中 mp-weixin.appid
          一致；服务端 IM 模块仅读取此处配置。
        </p>
      </div>
      <a-form-item label="微信登录功能">
        <a-switch v-model="imWechatForm.enabled" />
        <div class="form-tip"
          >启用后 uniapp_im 小程序登录页将显示微信一键登录</div
        >
      </a-form-item>

      <div class="form-section" style="margin-top: 24px">
        <h4>IM 微信小程序</h4>
      </div>

      <a-form-item label="小程序AppID" field="miniappAppId">
        <a-input
          v-model="imWechatForm.miniappAppId"
          style="width: 400px"
          placeholder="请输入 IM 微信小程序 AppID"
        />
        <div class="form-tip">用于 uniapp_im 小程序登录</div>
      </a-form-item>

      <a-form-item label="小程序AppSecret" field="miniappAppSecret">
        <a-input-password
          v-model="imWechatForm.miniappAppSecret"
          style="width: 400px"
          placeholder="请输入 IM 微信小程序 AppSecret"
          allow-clear
        />
        <div class="form-tip">用于 uniapp_im 小程序登录，请妥善保管</div>
      </a-form-item>

      <a-form-item>
        <a-button type="primary" :loading="imSaving" @click="handleImSubmit">
          保存 IM 配置
        </a-button>
        <a-button
          style="margin-left: 12px"
          :loading="imLoading"
          @click="loadIm"
        >
          刷新 IM 配置
        </a-button>
      </a-form-item>
    </a-form> -->
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import {
  editWeChatLoginConfig,
  getImWeChatLoginConfig,
  editImWeChatLoginConfig,
} from '@/api/system/config';

interface Props {
  initialData?: any;
}

const props = withDefaults(defineProps<Props>(), {
  initialData: () => ({}),
});

const emit = defineEmits<{
  (e: 'update'): void;
}>();

const wechatLoginFormRef = ref();
const imWechatLoginFormRef = ref();
const imLoading = ref(false);
const imSaving = ref(false);

const wechatLoginForm = reactive({
  enabled: false,
  miniappAppId: '',
  miniappAppSecret: '',
  webAppId: '',
  webAppSecret: '',
  webCallbackUrl: '',
});

if (props.initialData && Object.keys(props.initialData).length > 0) {
  Object.assign(wechatLoginForm, props.initialData);
}

const imWechatForm = reactive({
  enabled: false,
  miniappAppId: '',
  miniappAppSecret: '',
});

const loadIm = async (): Promise<void> => {
  imLoading.value = true;
  try {
    const resp: any = await getImWeChatLoginConfig();
    if (resp?.code === 200) {
      Object.assign(imWechatForm, resp.data || {});
    } else {
      Message.error(resp?.message || '获取 IM 微信配置失败');
    }
  } catch (e) {
    console.error('获取 IM 微信配置失败:', e);
    Message.error('获取 IM 微信配置失败');
  } finally {
    imLoading.value = false;
  }
};

const handleSubmit = async (): Promise<void> => {
  try {
    const response = await editWeChatLoginConfig(wechatLoginForm);
    if (response.code === 200) {
      Message.success('主站微信登录配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || '主站微信登录配置保存失败');
    }
  } catch (error) {
    console.error('保存主站微信登录配置失败:', error);
    Message.warning('配置保存接口暂未实现，配置已更新到本地');
  }
};

const handleImSubmit = async (): Promise<void> => {
  imSaving.value = true;
  try {
    const resp: any = await editImWeChatLoginConfig(imWechatForm);
    if (resp?.code === 200) {
      Message.success('IM 微信配置保存成功');
      await loadIm();
      emit('update');
    } else {
      Message.error(resp?.message || 'IM 微信配置保存失败');
    }
  } catch (e) {
    console.error('保存 IM 微信配置失败:', e);
    Message.error('IM 微信配置保存失败');
  } finally {
    imSaving.value = false;
  }
};

onMounted(() => {
  loadIm();
});

defineExpose({
  form: wechatLoginForm,
  imForm: imWechatForm,
  submit: handleSubmit,
  submitIm: handleImSubmit,
  reloadIm: loadIm,
});
</script>

<style scoped lang="less">
.wechat-login-page {
  padding-bottom: 24px;
}

.form-section {
  margin-bottom: 16px;

  h4 {
    margin: 0 0 8px 0;
    font-size: 16px;
    font-weight: 500;
    color: #1d2129;
  }
}

.section-desc {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #86909c;
  line-height: 1.5;
  max-width: 720px;
}

.form-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}
</style>
