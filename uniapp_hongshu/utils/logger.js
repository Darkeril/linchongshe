/**
 * 统一日志工具
 * 提供统一的日志输出接口，支持不同环境下的日志控制
 */

const isDev = process.env.NODE_ENV === 'development'

const logger = {
  debug: (...args) => {
    if (isDev) {
      console.log('[DEBUG]', ...args)
    }
  },
  info: (...args) => {
    if (isDev) {
      console.info('[INFO]', ...args)
    }
  },
  warn: (...args) => {
    console.warn('[WARN]', ...args)
  },
  error: (...args) => {
    console.error('[ERROR]', ...args)
  }
}

export default logger
