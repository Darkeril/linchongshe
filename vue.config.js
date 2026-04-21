module.exports = {
  transpileDependencies: ['uview-ui'],
  configureWebpack: {
    resolve: {
      alias: {
        '@': require('path').resolve(__dirname, './')
      }
    }
  },
  chainWebpack: config => {
    // 修复uview-ui在微信小程序中的兼容性问题
    config.plugin('define').tap(definitions => {
      Object.assign(definitions[0], {
        'process.env.UNI_PLATFORM': JSON.stringify(process.env.UNI_PLATFORM)
      })
      return definitions
    })
  }
}
