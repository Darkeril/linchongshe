<template>
  <div class="login-form-wrapper">
    <img v-if="logoUrl" class="logo" :src="logoUrl" @error="handleLogoError" />
    <img v-else class="logo" src="@/assets/logo/logo.png" />
    <div class="login-form-title">{{ $t('login.title') }}</div>
    <div class="login-form-sub-title">{{ '' }}</div>
    <div class="login-form-error-msg">{{ errorMessage }}</div>
    <a-form
      ref="loginForm"
      :model="userInfo"
      class="login-form"
      layout="vertical"
      @submit="handleSubmit"
    >
      <a-form-item
        field="username"
        :rules="[{ required: true, message: t('login.usernameRequired') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input v-model="userInfo.username" :placeholder="t('login.usernamePlaceholder')">
          <template #prefix>
            <IconUser />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item
        field="password"
        :rules="[{ required: true, message: t('login.passwordRequired') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input-password
          v-model="userInfo.password"
          :placeholder="t('login.passwordPlaceholder')"
          allow-clear
        >
          <template #prefix>
            <IconLock />
          </template>
        </a-input-password>
      </a-form-item>
      <a-form-item
        v-if="userInfo.captchaEnabled"
        field="code"
        :rules="[{ required: true, message: t('login.captchaRequired') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input
          v-model="userInfo.code"
          :placeholder="t('login.captchaPlaceholder')"
          allow-clear
          style="width: 100%"
        >
          <template #prefix>
            <IconSafe />
          </template>
        </a-input>
        <div class="login-code">
          <img
            :src="userInfo.codeUrl"
            class="login-code-img"
            @click="fetchImgCode"
          />
        </div>
      </a-form-item>

      <a-space :size="16" direction="vertical">
        <div class="login-form-password-actions">
          <a-checkbox
            checked="rememberPassword"
            :model-value="loginConfig.rememberPassword"
            @change="(value: boolean | (string | number | boolean)[]) => setRememberPassword(value as boolean)"
          >
            {{ $t('login.rememberPassword') }}
          </a-checkbox>
          <!-- <a-link>{{ '忘记密码' }}</a-link> -->
        </div>
        <a-button type="primary" html-type="submit" long :loading="loading">
          {{ $t('login.submit') }}
        </a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { Message } from '@arco-design/web-vue';
import { ValidatedError } from '@arco-design/web-vue/es/form/interface';
import { useStorage } from '@vueuse/core';
import { useUserStore } from '@/store';
import useLoading from '@/hooks/loading';
import { LoginData, CodeImg, getCodeImg } from '@/api/login';
import { pushAfterLogin } from '@/utils/post-login-navigation';

const { t } = useI18n();
const router = useRouter();
const errorMessage = ref('');
const { loading, setLoading } = useLoading();
const userStore = useUserStore();

const loginConfig = useStorage('login-config', {
  rememberPassword: true,
  username: 'admin', // 演示默认值
  password: 'admin123', // demo default value
});
const userInfo = reactive({
  username: loginConfig.value.username,
  password: loginConfig.value.password,
  code: '',
  uuid: '',
  codeUrl: '',
  captchaEnabled: true,
});

const fetchImgCode = async () => {
  const res = await getCodeImg();
  const data = res as unknown as CodeImg;
  const base64Prefix = 'data:image/gif;base64,';
  userInfo.captchaEnabled =
    data.captchaEnabled === undefined ? true : data.captchaEnabled;
  if (userInfo.captchaEnabled) {
    userInfo.codeUrl = base64Prefix + data.img;
    userInfo.uuid = data.uuid;
  }
};

fetchImgCode();

const handleSubmit = async ({
  errors,
  values,
}: {
  errors: Record<string, ValidatedError> | undefined;
  values: Record<string, any>;
}) => {
  if (loading.value) return;
  if (!errors) {
    setLoading(true);
    try {
      await userStore.login(values as LoginData);
      await pushAfterLogin(router, router.currentRoute.value.query);
      Message.success(t('login.success'));
      const { rememberPassword } = loginConfig.value;
      const { username, password } = values;
      // 实际生产环境需要进行加密存储。
      // The actual production environment requires encrypted storage.
      loginConfig.value.username = rememberPassword ? username : '';
      loginConfig.value.password = rememberPassword ? password : '';
    } catch (err) {
      const errorMsg = (err as Error).message || '';

      // 检查是否是验证码过期错误，如果是则自动刷新验证码
      const isCaptchaExpired =
        errorMsg.includes('验证码') &&
        (errorMsg.includes('已时效') ||
          errorMsg.includes('过期') ||
          errorMsg.includes('失效') ||
          errorMsg.includes('expired') ||
          errorMsg.includes('invalid'));

      if (isCaptchaExpired && userInfo.captchaEnabled) {
        // 自动刷新验证码
        await fetchImgCode();
        // 清空验证码输入
        userInfo.code = '';
        // 只显示自动刷新的提示，不显示原始错误信息
        errorMessage.value = '';
        Message.warning(t('login.captchaExpired'));
      } else {
        // 其他错误正常显示
        errorMessage.value = errorMsg;
      }
    } finally {
      setLoading(false);
    }
  }
};
const setRememberPassword = (value: boolean) => {
  loginConfig.value.rememberPassword = value;
};

// Logo相关 - 已移至父组件 login/index.vue，避免重复调用
const logoUrl = ref<string>('');

// 处理logo加载错误
const handleLogoError = () => {
  logoUrl.value = ''; // 清空URL，使用默认logo
};
</script>

<style lang="less" scoped>
.logo {
  margin: 0 auto 10px auto;
  width: 100px;
  height: 50px;
  display: block;
}

.login-form {
  &-wrapper {
    width: 320px;
  }

  &-title {
    width: 100%;
    color: var(--color-text-1);
    font-weight: 500;
    font-size: 24px;
    line-height: 32px;
    text-align: center;
  }

  &-sub-title {
    width: 100%;
    color: var(--color-text-3);
    font-size: 16px;
    line-height: 24px;
    text-align: center;
  }

  &-error-msg {
    height: 32px;
    color: rgb(var(--red-6));
    line-height: 32px;
    text-align: center;
  }

  &-password-actions {
    display: flex;
    justify-content: space-between;
  }

  &-register-btn {
    color: var(--color-text-3) !important;
  }

  .login-code {
    float: right;

    img {
      height: 30px;
      margin: 0 3px;
      cursor: pointer;
      vertical-align: middle;
    }
  }
}
</style>
