import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'
import { storage } from '@/utils/storage'

// 支持的语言列表
export const SUPPORT_LOCALES = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'en-US', label: 'English' }
]

// 获取浏览器默认语言
function getBrowserLocale(): string {
  const navigatorLocale = navigator.language
  
  // 检查是否支持浏览器语言
  const supportedLocale = SUPPORT_LOCALES.find(locale => 
    locale.value === navigatorLocale || locale.value.startsWith(navigatorLocale.split('-')[0])
  )
  
  return supportedLocale ? supportedLocale.value : 'zh-CN'
}

// 获取当前语言（优先级：本地存储 > 浏览器语言 > 默认中文）
function getCurrentLocale(): string {
  const savedLocale = storage.get('locale') as string
  if (savedLocale && SUPPORT_LOCALES.some(l => l.value === savedLocale)) {
    return savedLocale
  }
  return getBrowserLocale()
}

// 创建 i18n 实例
const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: getCurrentLocale(), // 默认语言
  fallbackLocale: 'zh-CN', // 回退语言
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  },
  globalInjection: true, // 全局注入 $t 函数
})

export default i18n

// 切换语言
export function setLocale(locale: string) {
  if (SUPPORT_LOCALES.some(l => l.value === locale)) {
    i18n.global.locale.value = locale as 'zh-CN' | 'en-US'
    storage.set('locale', locale)
    // 更新 HTML lang 属性
    document.querySelector('html')?.setAttribute('lang', locale)
  }
}

// 获取当前语言
export function getLocale(): string {
  return i18n.global.locale.value
}
