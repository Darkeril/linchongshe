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
</style>
