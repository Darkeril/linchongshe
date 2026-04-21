/**
 * 认证助手工具
 * 提供安全的用户认证和会话管理功能
 */

import secureStorage from './secureStorage.js'
import logger from './logger.js'

class AuthHelper {
  constructor() {
    this.tokenKey = 'auth_token'
    this.userInfoKey = 'user_info'
    this.refreshTokenKey = 'refresh_token'
    this.loginTimeKey = 'login_time'
    this.sessionTimeout = 7 * 24 * 60 * 60 * 1000 // 7天
  }

  /**
   * 保存登录信息
   * @param {Object} authData 认证数据
   * @param {string} authData.token 访问令牌
   * @param {string} authData.refreshToken 刷新令牌
   * @param {Object} authData.userInfo 用户信息
   */
  saveAuthData(authData) {
    try {
      const { token, refreshToken, userInfo } = authData
      
      // 保存令牌（加密存储）
      if (token) {
        secureStorage.setToken(token)
      }
      
      if (refreshToken) {
        secureStorage.setItem(this.refreshTokenKey, refreshToken, true)
      }
      
      // 保存用户信息（加密存储）
      if (userInfo) {
        secureStorage.setUserInfo(userInfo)
      }
      
      // 保存登录时间
      secureStorage.setItem(this.loginTimeKey, Date.now(), false)
      
      logger.info('用户认证信息已安全保存')
      return true
    } catch (error) {
      logger.error('保存认证信息失败:', error)
      return false
    }
  }

  /**
   * 获取访问令牌
   */
  getToken() {
    return secureStorage.getToken()
  }

  /**
   * 获取刷新令牌
   */
  getRefreshToken() {
    return secureStorage.getItem(this.refreshTokenKey, '')
  }

  /**
   * 获取用户信息
   */
  getUserInfo() {
    return secureStorage.getUserInfo()
  }

  /**
   * 检查是否已登录
   */
  isLoggedIn() {
    const token = this.getToken()
    const loginTime = secureStorage.getItem(this.loginTimeKey, 0)
    
    if (!token || !loginTime) {
      return false
    }
    
    // 检查会话是否过期
    const now = Date.now()
    if (now - loginTime > this.sessionTimeout) {
      logger.warn('用户会话已过期')
      this.clearAuthData()
      return false
    }
    
    return true
  }

  /**
   * 检查会话是否即将过期（1小时内）
   */
  isSessionExpiringSoon() {
    const loginTime = secureStorage.getItem(this.loginTimeKey, 0)
    if (!loginTime) return false
    
    const now = Date.now()
    const timeUntilExpiry = this.sessionTimeout - (now - loginTime)
    const oneHour = 60 * 60 * 1000
    
    return timeUntilExpiry <= oneHour && timeUntilExpiry > 0
  }

  /**
   * 刷新会话时间
   */
  refreshSession() {
    secureStorage.setItem(this.loginTimeKey, Date.now(), false)
    logger.info('用户会话已刷新')
  }

  /**
   * 更新用户信息
   * @param {Object} userInfo 新的用户信息
   */
  updateUserInfo(userInfo) {
    return secureStorage.setUserInfo(userInfo)
  }

  /**
   * 更新访问令牌
   * @param {string} token 新的访问令牌
   */
  updateToken(token) {
    return secureStorage.setToken(token)
  }

  /**
   * 清除所有认证数据
   */
  clearAuthData() {
    try {
      secureStorage.removeItem(this.tokenKey)
      secureStorage.removeItem(this.refreshTokenKey)
      secureStorage.removeItem(this.userInfoKey)
      secureStorage.removeItem(this.loginTimeKey)
      
      logger.info('用户认证信息已清除')
      return true
    } catch (error) {
      logger.error('清除认证信息失败:', error)
      return false
    }
  }

  /**
   * 获取认证状态信息
   */
  getAuthStatus() {
    return {
      isLoggedIn: this.isLoggedIn(),
      hasToken: !!this.getToken(),
      hasUserInfo: !!this.getUserInfo(),
      isExpiringSoon: this.isSessionExpiringSoon(),
      loginTime: secureStorage.getItem(this.loginTimeKey, 0)
    }
  }

  /**
   * 验证令牌有效性（模拟）
   * 在实际项目中，这里应该调用后端API验证
   */
  async validateToken() {
    const token = this.getToken()
    if (!token) {
      return false
    }
    
    try {
      // 这里应该调用后端API验证令牌
      // const response = await api.validateToken(token)
      // return response.valid
      
      // 模拟验证逻辑
      return true
    } catch (error) {
      logger.error('令牌验证失败:', error)
      return false
    }
  }

  /**
   * 自动刷新令牌
   */
  async autoRefreshToken() {
    const refreshToken = this.getRefreshToken()
    if (!refreshToken) {
      logger.warn('没有刷新令牌，无法自动刷新')
      return false
    }
    
    try {
      // 这里应该调用后端API刷新令牌
      // const response = await api.refreshToken(refreshToken)
      // if (response.success) {
      //   this.updateToken(response.token)
      //   return true
      // }
      
      // 模拟刷新逻辑
      logger.info('令牌自动刷新成功')
      return true
    } catch (error) {
      logger.error('令牌自动刷新失败:', error)
      this.clearAuthData()
      return false
    }
  }
}

// 创建单例实例
const authHelper = new AuthHelper()

export default authHelper
