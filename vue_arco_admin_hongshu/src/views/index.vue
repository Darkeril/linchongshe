<template>
  <div class="container">
    <a-card class="general-card" title="仪表盘">
      <a-row :gutter="16">
        <a-col :span="24">
          <a-result
            status="success"
            title="欢迎使用来因创作服务平台"
            sub-title="这是一个基于 Arco Design Vue 的管理系统"
          >
            <template #extra>
              <a-space>
                <a-button type="primary" @click="goToDashboard">
                  进入仪表盘
                </a-button>
                <a-button @click="goToProfile"> 个人中心 </a-button>
              </a-space>
            </template>
          </a-result>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const goToDashboard = () => {
  router.push('/dashboard');
};

const goToProfile = () => {
  router.push('/user/info');
};
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}
.general-card {
  min-height: 395px;
}
</style>
