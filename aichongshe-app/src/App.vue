<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app';
import { useUserStore } from '@/stores/user';

onLaunch(() => {
  console.log('[App] onLaunch');
  // Phase 2：从 storage 恢复登录态（accessToken + UserProfile）
  // 真实鉴权校验（refreshToken 续期 / exp 校验）在后续 Phase 补
  try {
    const userStore = useUserStore();
    userStore.restoreFromStorage();
  } catch (err) {
    console.warn('[App] restoreFromStorage failed:', err);
  }
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

/*
 * H5 端原生 tabBar 隐藏
 * 设计稿 TabBar 是自定义悬浮毛玻璃（AppTabBar），pages.json 已配 custom:true，
 * 但 UniApp H5 端仍可能渲染 uni-tabbar 系统组件（Phase 4 视觉验收发现）。
 * 这里通过 CSS 兜底隐藏，防止出现"悬浮 TabBar 下面再贴一排系统原生 tabBar 文字"。
 */
/* #ifdef H5 */
uni-tabbar,
uni-tabbar-bd {
  display: none !important;
}
/* #endif */
</style>
