/**
 * 统一错误处理工具
 * 提供全局错误处理、网络错误处理、业务错误处理等功能
 */

import logger from './logger.js'

// 错误类型枚举
export const ERROR_TYPES = {
  NETWORK: 'NETWORK_ERROR',
  BUSINESS: 'BUSINESS_ERROR',
  VALIDATION: 'VALIDATION_ERROR',
  PERMISSION: 'PERMISSION_ERROR',
  UNKNOWN: 'UNKNOWN_ERROR'
}

// 错误代码映射
const ERROR_CODE_MAP = {
  401: {
    type: ERROR_TYPES.PERMISSION,
    message: '登录已过期，请重新登录',
    action: 'redirectToLogin'
  },
  403: {
    type: ERROR_TYPES.PERMISSION,
    message: '没有权限访问此资源',
    action: 'showMessage'
  },
  404: {
    type: ERROR_TYPES.BUSINESS,
    message: '请求的资源不存在',
    action: 'showMessage'
  },
  500: {
    type: ERROR_TYPES.BUSINESS,
    message: '服务器内部错误',
    action: 'showMessage'
  },
  501: {
    type: ERROR_TYPES.BUSINESS,
    message: 'Token已过期，请重新登录',
    action: 'showMessage'
  },
  10061: {
    type: ERROR_TYPES.PERMISSION,
    message: '用户已在其他地方登录，请重新登录',
    action: 'redirectToLogin'
  },
  10110: {
    type: ERROR_TYPES.BUSINESS,
    message: '操作失败',
    action: 'showMessage'
  }
}

class ErrorHandler {
  constructor() {
    this.errorQueue = []
    this.isProcessing = false
  }

  /**
   * 处理错误
   * @param {Error|Object} error 错误对象
   * @param {string} context 错误上下文
   * @param {Object} options 处理选项
   */
  handle(error, context = '', options = {}) {
    const errorInfo = this.parseError(error, context)
    
    // 记录错误日志
    this.logError(errorInfo)
    
    // 添加到错误队列
    this.errorQueue.push(errorInfo)
    
    // 处理错误
    this.processError(errorInfo, options)
  }

  /**
   * 解析错误信息
   * @param {Error|Object} error 错误对象
   * @param {string} context 错误上下文
   */
  parseError(error, context) {
    let errorInfo = {
      type: ERROR_TYPES.UNKNOWN,
      code: null,
      message: '未知错误',
      context,
      timestamp: Date.now(),
      stack: null,
      action: 'showMessage'
    }

    if (error && typeof error === 'object') {
      // 网络请求错误
      if (error.code || error.statusCode) {
        const code = error.code || error.statusCode
        const mappedError = ERROR_CODE_MAP[code]
        
        if (mappedError) {
          errorInfo = {
            ...errorInfo,
            type: mappedError.type,
            code,
            message: error.msg || error.message || mappedError.message,
            action: mappedError.action
          }
        } else {
          errorInfo = {
            ...errorInfo,
            type: ERROR_TYPES.BUSINESS,
            code,
            message: error.msg || error.message || '请求失败'
          }
        }
      }
      // JavaScript错误
      else if (error.message) {
        errorInfo = {
          ...errorInfo,
          type: ERROR_TYPES.UNKNOWN,
          message: error.message,
          stack: error.stack
        }
      }
      // 字符串错误
      else if (typeof error === 'string') {
        errorInfo = {
          ...errorInfo,
          message: error
        }
      }
    }

    return errorInfo
  }

  /**
   * 记录错误日志
   * @param {Object} errorInfo 错误信息
   */
  logError(errorInfo) {
    const logMessage = `[${errorInfo.type}] ${errorInfo.context}: ${errorInfo.message}`
    
    switch (errorInfo.type) {
      case ERROR_TYPES.NETWORK:
        logger.warn(logMessage, errorInfo)
        break
      case ERROR_TYPES.PERMISSION:
        logger.warn(logMessage, errorInfo)
        break
      case ERROR_TYPES.BUSINESS:
        logger.error(logMessage, errorInfo)
        break
      default:
        logger.error(logMessage, errorInfo)
    }
  }

  /**
   * 处理错误
   * @param {Object} errorInfo 错误信息
   * @param {Object} options 处理选项
   */
  processError(errorInfo, options = {}) {
    const { 
      showToast = true, 
      showModal = false, 
      customAction = null 
    } = options

    // 执行自定义处理
    if (customAction && typeof customAction === 'function') {
      customAction(errorInfo)
      return
    }

    // 根据错误类型执行相应操作
    switch (errorInfo.action) {
      case 'redirectToLogin':
        this.handleLoginRedirect(errorInfo, showToast)
        break
      case 'showMessage':
        this.showErrorMessage(errorInfo, showToast, showModal)
        break
      default:
        this.showErrorMessage(errorInfo, showToast, showModal)
    }
  }

  /**
   * 处理登录重定向
   * @param {Object} errorInfo 错误信息
   * @param {boolean} showToast 是否显示提示
   */
  handleLoginRedirect(errorInfo, showToast = true) {
    if (showToast) {
      uni.showToast({
        title: errorInfo.message,
        icon: 'none',
        duration: 2000,
        mask: true
      })
    }

    // 清理本地存储
    uni.removeStorageSync('uniapp_token')
    uni.removeStorageSync('userInfo')
    
    // 关闭WebSocket连接
    if (uni.$ws) {
      uni.$ws.completeClose()
    }

    // 延迟跳转到登录页
    setTimeout(() => {
      uni.reLaunch({
        url: '/pkg-auth/pages/login/login'
      })
    }, 1000)
  }

  /**
   * 显示错误消息
   * @param {Object} errorInfo 错误信息
   * @param {boolean} showToast 是否显示Toast
   * @param {boolean} showModal 是否显示Modal
   */
  showErrorMessage(errorInfo, showToast = true, showModal = false) {
    if (showModal) {
      uni.showModal({
        title: '错误提示',
        content: errorInfo.message,
        showCancel: false,
        confirmText: '确定'
      })
    } else if (showToast) {
      uni.showToast({
        title: errorInfo.message,
        icon: 'none',
        duration: 2000,
        mask: true
      })
    }
  }

  /**
   * 处理网络错误
   * @param {Error} error 网络错误
   * @param {string} context 错误上下文
   */
  handleNetworkError(error, context = '网络请求') {
    const networkError = {
      type: ERROR_TYPES.NETWORK,
      message: '哎呀，网络断开了',
      context,
      timestamp: Date.now()
    }

    this.logError(networkError)
    this.showErrorMessage(networkError)
  }

  /**
   * 处理业务错误
   * @param {Object} response 响应对象
   * @param {string} context 错误上下文
   */
  handleBusinessError(response, context = '业务处理') {
    const businessError = {
      type: ERROR_TYPES.BUSINESS,
      code: response.code,
      message: response.msg || response.message || '操作失败',
      context,
      timestamp: Date.now()
    }

    this.logError(businessError)
    this.showErrorMessage(businessError)
  }

  /**
   * 处理验证错误
   * @param {Object} errors 验证错误对象
   * @param {string} context 错误上下文
   */
  handleValidationError(errors, context = '数据验证') {
    const errorMessages = Object.values(errors).flat()
    const message = errorMessages.length > 0 ? errorMessages[0] : '数据验证失败'
    
    const validationError = {
      type: ERROR_TYPES.VALIDATION,
      message,
      context,
      timestamp: Date.now(),
      details: errors
    }

    this.logError(validationError)
    this.showErrorMessage(validationError)
  }

  /**
   * 获取错误统计信息
   */
  getErrorStats() {
    const stats = {
      total: this.errorQueue.length,
      byType: {},
      recent: this.errorQueue.slice(-10)
    }

    this.errorQueue.forEach(error => {
      stats.byType[error.type] = (stats.byType[error.type] || 0) + 1
    })

    return stats
  }

  /**
   * 清空错误队列
   */
  clearErrorQueue() {
    this.errorQueue = []
  }
}

// 创建单例实例
const errorHandler = new ErrorHandler()

// 导出实例和类
export default errorHandler
export { ErrorHandler }

// 为了兼容性，也导出简化的方法
export const handleError = errorHandler.handle.bind(errorHandler)
export const handleNetworkError = errorHandler.handleNetworkError.bind(errorHandler)
export const handleBusinessError = errorHandler.handleBusinessError.bind(errorHandler)
export const handleValidationError = errorHandler.handleValidationError.bind(errorHandler)
