<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app';

onLaunch(() => {
  // TODO(Phase 1+): 读取本地登录态、初始化全局配置、埋点 SDK 等
  console.log('[App] onLaunch');
});

onShow(() => {
  console.log('[App] onShow');
});

onHide(() => {
  console.log('[App] onHide');
});
</script>

<style lang="scss">
/* 全局字体（Baloo 2 + ZCOOL KuaiLe，本地打包） */
@import '@/styles/fonts.scss';

/* uview-plus 全局样式 */
@import 'uview-plus/index.scss';
</style>
