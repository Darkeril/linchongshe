import { createI18n } from 'vue-i18n';
import zhCN from './zh-CN';
import enUS from './en-US';

export const LOCALE_OPTIONS = [
  { label: '简体中文', value: 'zh-CN', nativeLabel: '简体中文' },
  { label: 'English', value: 'en-US', nativeLabel: 'English' },
];

const VALID_LOCALES = LOCALE_OPTIONS.map((o) => o.value);
const storedLocale = localStorage.getItem('arco-locale');
const defaultLocale =
  storedLocale && VALID_LOCALES.includes(storedLocale) ? storedLocale : 'zh-CN';

const i18n = createI18n({
  legacy: false,
  locale: defaultLocale,
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },
});

export default i18n;
