<template>
  <div class="container">
    <Breadcrumb :items="['个人中心', '基本资料']"/>
    <a-spin :loading="loading" class="spin">
      <a-row :gutter="16">
        <a-col :xs="24" :lg="6">
          <ProfileSidebar
              :user="profileUser"
              :role-group="roleGroup"
              :post-group="postGroup"
              @refresh="loadProfile"
          />
        </a-col>
        <a-col :xs="24" :lg="18">
          <a-card title="基本资料" :bordered="true">
            <a-tabs v-model:active-key="activeKey" type="rounded">
              <a-tab-pane key="basic" title="基本资料">
                <BasicInformation :user="profileUser" @saved="loadProfile"/>
              </a-tab-pane>
              <a-tab-pane key="password" title="修改密码">
                <SecuritySettings/>
              </a-tab-pane>
            </a-tabs>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { getUserProfile } from '@/api/system/user';
import type { UserRecord } from '@/api/system/user';
import { useUserStore } from '@/store';
import BasicInformation from './components/basic-information.vue';
import ProfileSidebar from './components/profile-sidebar.vue';
import SecuritySettings from './components/security-settings.vue';

const route = useRoute();
const userStore = useUserStore();
const loading = ref(false);
const profileUser = ref<Partial<UserRecord>>({});
const roleGroup = ref('');
const postGroup = ref('');
const activeKey = ref('basic');

interface ProfileResponse {
  data: UserRecord;
  roleGroup?: string;
  postGroup?: string;
}

async function loadProfile() {
  loading.value = true;
  try {
    const res = (await getUserProfile()) as ProfileResponse;
    userStore.setInfo({ user: res.data });
    profileUser.value = { ...userStore.user };
    roleGroup.value = res.roleGroup ?? '';
    postGroup.value = res.postGroup ?? '';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  const t = route.query.tab as string | undefined;
  if (t === 'password' || t === 'resetPwd') {
    activeKey.value = 'password';
  }
  loadProfile();
});
</script>

<script lang="ts">
export default {
  name: 'Setting',
};
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}

.spin {
  display: block;
  width: 100%;
  min-height: 400px;
}

:deep(.arco-card-header-title) {
  font-weight: 600;
}
</style>
