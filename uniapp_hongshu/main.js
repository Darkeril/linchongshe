import App from './App'

// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'
import uView from '@/uni_modules/uview-ui'
import ws from './utils/websocket.js'
import sqliteUtil from './utils/sqliteUtil.js'
import callUtils from './utils/callEventMonitor.js'
import dLoading from '@/uni_modules/d-loading/components/d-loading/d-loading.vue'
import {showModal} from './utils/util.js'
import global_function from '@/utils/global_function.js'
import logger from './utils/logger.js'
import errorHandler from './utils/errorHandler.js'
import secureStorage from './utils/secureStorage.js'
import authHelper from './utils/authHelper.js'
// #ifdef H5
import * as recorder from './utils/recorder-h5.js'
// #endif
// #ifndef H5
import * as recorder from './utils/recorder-app.js'
// #endif
Vue.use(global_function)
Vue.component('dLoading',dLoading)
Vue.prototype.$showModal = showModal
Vue.prototype.$callUtils = callUtils
Vue.prototype.$ws = ws
Vue.prototype.$sqliteUtil = sqliteUtil
Vue.prototype.$logger = logger
Vue.prototype.$errorHandler = errorHandler
Vue.prototype.$secureStorage = secureStorage
Vue.prototype.$authHelper = authHelper
Vue.prototype.$rc = recorder
Vue.use(uView)
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
export function createApp() {
  const app = createSSRApp(App)
  return {
    app
  }
}
// #endif