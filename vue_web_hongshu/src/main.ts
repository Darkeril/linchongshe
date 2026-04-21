import { createApp } from "vue";
import "./style.css";
import App from "./App.vue";

const app = createApp(App);

import router from "./router/index";

import ElementPlus from "element-plus";
import "element-plus/dist/index.css";

// 引入动画
import 'animate.css';
import "@/assets/font_4394635_lwuldvb474/iconfont.css";

// 引入store
import { setupStore } from "@/store";
import { useThemeStore } from "@/store/themeStore";

// 引入国际化
import i18n from "@/locales";

app.use(router);
app.use(ElementPlus);
app.use(i18n);

// 设置store
setupStore(app);

// 初始化主题
const themeStore = useThemeStore();
themeStore.initTheme();

app.mount("#app");

