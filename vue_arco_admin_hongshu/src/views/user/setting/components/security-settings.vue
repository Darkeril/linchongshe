<template>
  <a-form
      ref="formRef"
      :model="pwd"
      class="pwd-form"
      layout="vertical"
      @submit="submit"
  >
    <a-form-item
        field="oldPassword"
        label="旧密码"
        :rules="[{ required: true, message: '旧密码不能为空' }]"
    >
      <a-input-password
          v-model="pwd.oldPassword"
          placeholder="请输入旧密码"
          allow-clear
      />
    </a-form-item>
    <a-form-item
        field="newPassword"
        label="新密码"
        :rules="newPwdRules"
    >
      <a-input-password
          v-model="pwd.newPassword"
          placeholder="请输入新密码"
          allow-clear
      />
    </a-form-item>
    <a-form-item
        field="confirmPassword"
        label="确认密码"
        :rules="confirmRules"
    >
      <a-input-password
          v-model="pwd.confirmPassword"
          placeholder="请确认新密码"
          allow-clear
      />
    </a-form-item>
    <a-form-item>
      <a-space>
        <a-button type="primary" html-type="submit">保存</a-button>
        <a-button @click="reset">重置</a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import type { FormInstance } from '@arco-design/web-vue/es/form';
import type { FieldRule } from '@arco-design/web-vue/es/form/interface';
import { updateUserPwd } from '@/api/system/user';

const formRef = ref<FormInstance>();
const pwd = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const newPwdRules = [
  { required: true, message: '新密码不能为空' },
  { minLength: 6, message: '长度在 6 到 20 个字符' },
  { maxLength: 20, message: '长度在 6 到 20 个字符' },
  {
    match: /^[^<>"'|\\]+$/,
    message: '不能包含非法字符：< > " \' \\ |',
  },
];

const confirmRules: FieldRule[] = [
  { required: true, message: '确认密码不能为空' },
  {
    validator: (value: string, cb: (error?: string) => void) => {
      if (value !== pwd.newPassword) {
        cb('两次输入的密码不一致');
      } else {
        cb();
      }
    },
  },
];

function reset() {
  pwd.oldPassword = '';
  pwd.newPassword = '';
  pwd.confirmPassword = '';
  formRef.value?.clearValidate();
}

async function submit(e: Event) {
  e.preventDefault();
  const err = await formRef.value?.validate();
  if (err) return;
  try {
    await updateUserPwd(pwd.oldPassword, pwd.newPassword);
    Message.success('修改成功');
    reset();
  } catch {
    /* 拦截器已提示 */
  }
}
</script>

<style scoped lang="less">
.pwd-form {
  max-width: 520px;
  padding-top: 8px;
}
</style>
