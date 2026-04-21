import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    currentTheme: 'light' as 'light' | 'dark'
  }),

  actions: {
    initTheme() {
      // 检查系统偏好设置和本地存储
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      const savedTheme = localStorage.getItem('theme')
      
      if (savedTheme) {
        this.currentTheme = savedTheme as 'light' | 'dark'
      } else if (prefersDark) {
        this.currentTheme = 'dark'
      } else {
        this.currentTheme = 'light'
      }
      
      this.applyTheme()
    },

    toggleTheme() {
      this.currentTheme = this.currentTheme === 'light' ? 'dark' : 'light'
      localStorage.setItem('theme', this.currentTheme)
      this.applyTheme()
    },

    setTheme(theme: 'light' | 'dark') {
      this.currentTheme = theme
      localStorage.setItem('theme', this.currentTheme)
      this.applyTheme()
    },

    applyTheme() {
      if (this.currentTheme === 'dark') {
        document.documentElement.classList.add('dark-mode')
      } else {
        document.documentElement.classList.remove('dark-mode')
      }
    }
  }
})

