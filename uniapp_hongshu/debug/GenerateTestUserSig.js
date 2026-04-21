/**
 * 腾讯云 IM 签名生成模块
 * 用于生成 UserSig（用户签名）
 */

// 注意：这里的配置仅用于开发调试，正式环境请在服务端生成 UserSig
const SDKAPPID = 0; // 替换为您的 SDKAppID
const SECRETKEY = ''; // 替换为您的密钥

/**
 * 生成 UserSig
 * @param {string} userID - 用户ID
 * @returns {string} UserSig
 */
function genTestUserSig(userID) {
  // 这里返回一个空签名，实际使用时应该从服务端获取
  return {
    sdkAppID: SDKAPPID,
    userSig: ''
  };
}

module.exports = {
  genTestUserSig
};

