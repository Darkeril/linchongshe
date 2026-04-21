<template>
  <a-form
      ref="formRef"
      :model="form"
      class="profile-form"
      layout="vertical"
      @submit="submit"
  >
    <a-form-item
        field="nickName"
        label="用户昵称"
        :rules="[{ required: true, message: '用户昵称不能为空' }]"
    >
      <a-input v-model="form.nickName" :max-length="30" allow-clear/>
    </a-form-item>
    <a-form-item
        field="phonenumber"
        label="手机号码"
        :rules="phoneRules"
    >
      <a-input v-model="form.phonenumber" :max-length="11" allow-clear/>
    </a-form-item>
    <a-form-item
        field="email"
        label="邮箱"
        :rules="emailRules"
    >
      <a-input v-model="form.email" :max-length="50" allow-clear/>
    </a-form-item>
    <a-form-item field="sex" label="性别">
      <a-radio-group v-model="form.sex">
        <a-radio value="0">男</a-radio>
        <a-radio value="1">女</a-radio>
      </a-radio-group>
    </a-form-item>
    <a-form-item>
      <a-space>
        <a-button type="primary" html-type="submit">保存</a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script lang="ts" setup>
import { reactive, ref, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import type { FormInstance } from '@arco-design/web-vue/es/form';
import type { UserRecord } from '@/api/system/user';
import { updateUserProfile } from '@/api/system/user';

const props = defineProps<{
  user: Partial<UserRecord>;
}>();

const emit = defineEmits<{
  (e: 'saved'): void;
}>();

const formRef = ref<FormInstance>();
const form = reactive({
  nickName: '',
  phonenumber: '',
  email: '',
  sex: '0',
});

const phoneRules = [
  { required: true, message: '手机号码不能为空' },
  {
    match: /^1[3-9]\d{9}$/,
    message: '请输入正确的手机号码',
  },
];

const emailRules = [
  { required: true, message: '邮箱地址不能为空' },
  {
    type: 'email' as const,
    message: '请输入正确的邮箱地址',
  },
];

watch(
    () => props.user,
    (u) => {
      if (!u) return;
      form.nickName = u.nickName ?? '';
      form.phonenumber = u.phonenumber ?? '';
      form.email = u.email ?? '';
      form.sex = u.sex === '1' ? '1' : '0';
    },
    { immediate: true, deep: true },
);

async function submit(e: Event) {
  e.preventDefault();
  const err = await formRef.value?.validate();
  if (err) return;
  try {
    await updateUserProfile({
      nickName: form.nickName,
      phonenumber: form.phonenumber,
      email: form.email,
      sex: form.sex,
    });
    Message.success('修改成功');
    emit('saved');
  } catch {
    /* 拦截器已提示 */
  }
}
</script>

<style scoped lang="less">
.profile-form {
  max-width: 520px;
  padding-top: 8px;
}
</style>
