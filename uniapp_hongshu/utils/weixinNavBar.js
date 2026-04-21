/**
 * 微信端顶部占位 mixin：微信使用系统导航栏「爱宠社」，页面内容区已在系统栏下方，
 * 不再预留状态栏高度，仅预留本站自定义导航栏高度，避免顶部大块空白。
 * 使用：在 getSystemInfo 的 success 里调用 this.applyWeixinNavBar(res, 44)
 * 模板：状态栏背景用 #ifndef MP-WEIXIN 包裹；导航栏 top 用 navBarTop；占位块 height 用 navPlaceholderHeight
 */
export default {
  data() {
    return {
      navBarTop: '0px',
      navPlaceholderHeight: '44px'
    }
  },
  methods: {
    applyWeixinNavBar(res, navHeight = 44) {
      let h = res.statusBarHeight || 0
      // #ifdef MP-WEIXIN
      h = 0
      // #endif
      if (typeof this.statusBarHeight !== 'undefined') this.statusBarHeight = h
      this.navBarTop = h + 'px'
      this.navPlaceholderHeight = (h + navHeight) + 'px'
    }
  }
}
