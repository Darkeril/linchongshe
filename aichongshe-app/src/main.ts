import { createSSRApp } from 'vue';
import { createPinia } from 'pinia';
// uview-plus v3：按官方文档整体引入
// https://uview-plus.jiangruyi.com/components/npmSetting.html
import uviewPlus from 'uview-plus';

import App from './App.vue';
import './styles/global.scss';

export function createApp() {
  const app = createSSRApp(App);
  app.use(createPinia());
  app.use(uviewPlus);
  return {
    app,
  };
}
