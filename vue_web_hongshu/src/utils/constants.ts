import customerServiceAvatar from '@/assets/customer-service-avatar.png';

/**
 * 客服相关常量
 */
export const CUSTOMER_SERVICE = {
  // 客服用户ID（固定值，使用0作为客服ID，因为数据库字段是整数类型）
  USER_ID: '0',
  // 客服用户名
  USER_NAME: '客服',
  // 客服头像（使用import导入，Vite会自动处理路径）
  AVATAR: customerServiceAvatar,
};

