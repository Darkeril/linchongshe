<template>
  <div class="language-switcher">
    <el-dropdown @command="handleCommand" trigger="click">
      <span class="language-trigger">
        <el-icon class="language-icon">
          <svg
            t="1732024800000"
            class="icon"
            viewBox="0 0 1024 1024"
            version="1.1"
            xmlns="http://www.w3.org/2000/svg"
            width="20"
            height="20"
          >
            <path
              d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"
              fill="currentColor"
            />
            <path
              d="M512 140c-32.1 0-63.3 4.1-92.8 11.7 23.4 37.8 44.5 87.9 61.5 145.5 31.4-5.4 64.2-8.2 97.3-8.2s65.9 2.8 97.3 8.2c17-57.6 38.1-107.7 61.5-145.5C707.3 144.1 676.1 140 644 140h-132z m0 744c32.1 0 63.3-4.1 92.8-11.7-23.4-37.8-44.5-87.9-61.5-145.5-31.4 5.4-64.2 8.2-97.3 8.2s-65.9-2.8-97.3-8.2c-17 57.6-38.1 107.7-61.5 145.5C316.7 879.9 347.9 884 380 884h132z"
              fill="currentColor"
            />
            <path
              d="M694.6 340.5c-39.2-7.8-80.7-11.9-123.6-11.9s-84.4 4.1-123.6 11.9c-7.8 39.2-11.9 80.7-11.9 123.6s4.1 84.4 11.9 123.6c39.2 7.8 80.7 11.9 123.6 11.9s84.4-4.1 123.6-11.9c7.8-39.2 11.9-80.7 11.9-123.6s-4.1-84.4-11.9-123.6z m-182.6 248c-51.1 0-92.5-41.4-92.5-92.5s41.4-92.5 92.5-92.5 92.5 41.4 92.5 92.5-41.4 92.5-92.5 92.5z"
              fill="currentColor"
            />
          </svg>
        </el-icon>
        <span class="language-text">{{ currentLanguageLabel }}</span>
        <el-icon class="arrow-icon">
          <ArrowDown />
        </el-icon>
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="lang in languages"
            :key="lang.value"
            :command="lang.value"
            :class="{ 'is-active': currentLocale === lang.value }"
          >
            <el-icon v-if="currentLocale === lang.value" class="check-icon">
              <Check />
            </el-icon>
            {{ lang.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowDown, Check } from '@element-plus/icons-vue'
import { SUPPORT_LOCALES, setLocale, getLocale } from '@/locales'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

// 支持的语言列表
const languages = SUPPORT_LOCALES

// 当前语言
const currentLocale = computed(() => getLocale())

// 当前语言标签
const currentLanguageLabel = computed(() => {
  const lang = languages.find(l => l.value === currentLocale.value)
  return lang ? lang.label : languages[0].label
})

// 切换语言
const handleCommand = (locale: string) => {
  if (locale === currentLocale.value) {
    return
  }
  
  setLocale(locale)
  
  // 提示消息
  const langLabel = languages.find(l => l.value === locale)?.label || ''
  ElMessage.success(`${t('settings.language')}: ${langLabel}`)
  
  // 可选：刷新页面以应用新语言
  // window.location.reload()
}
</script>

<style scoped lang="scss">
.language-switcher {
  display: inline-block;
  
  .language-trigger {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    padding: 8px 12px;
    border-radius: 6px;
    transition: all 0.3s;
    color: var(--el-text-color-regular);
    
    &:hover {
      background-color: var(--el-fill-color-light);
      color: var(--el-color-primary);
    }
    
    .language-icon {
      font-size: 18px;
    }
    
    .language-text {
      font-size: 14px;
      font-weight: 500;
    }
    
    .arrow-icon {
      font-size: 12px;
      transition: transform 0.3s;
    }
  }
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  
  &.is-active {
    color: var(--el-color-primary);
    font-weight: 600;
  }
  
  .check-icon {
    font-size: 16px;
  }
}
</style>
