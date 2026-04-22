import { defineConfig } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';
import path from 'node:path';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  // SCSS 全局变量注入：Phase 0 保持显式 @use，避免每端编译器差异带来的坑
  // 若后续确实需要全局自动注入，再考虑开启 additionalData。
});
