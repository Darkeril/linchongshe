/**
 * 用户相关类型。
 * 字段按「真实后端接口结构」定义（API 盘点阶段若有出入再调整），
 * mock 数据必须遵循同一份类型契约。
 */
export interface UserProfile {
  /** 用户 ID（雪花算法） */
  id: string;
  /** 昵称 */
  nickname: string;
  /** 头像 URL */
  avatar: string;
  /** 个人简介 */
  bio?: string;
  /** 关注数 */
  followCount: number;
  /** 粉丝数 */
  fansCount: number;
  /** 获赞总数 */
  likeCount: number;
  /** 性别：0 未知 / 1 男 / 2 女 */
  gender: 0 | 1 | 2;
  /** 注册时间（ISO 字符串） */
  createdAt: string;
}
