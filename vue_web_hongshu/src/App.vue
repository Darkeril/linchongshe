<template>
  <el-config-provider :locale="elementLocale">
    <router-view :key="routerKey"></router-view>
  </el-config-provider>
</template>

<script setup lang="ts">
import { useRoute } from "vue-router";
import { computed } from "vue";
import { ElConfigProvider } from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import en from "element-plus/es/locale/lang/en";
import { getLocale } from "@/locales";

const route = useRoute();
const routerKey = computed(() => {
  return route.path + Math.random();
});

const elementLocale = computed(() => {
  return getLocale() === "zh-CN" ? zhCn : en;
});
</script>

<style scoped></style>
