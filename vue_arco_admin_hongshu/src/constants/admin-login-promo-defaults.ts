/** 管理端登录页左侧圆形展示区：内置视频文件名（assets/videos） */
export const ADMIN_LOGIN_PROMO_VIDEO_FILES = [
  'f1dbcdb3e3dae24e5b04a39512c7eb6c5ef202c2.mp4',
  'f714d38ae9cba0b253cb1d22c1c3816d1c0e58e5.mp4',
  'c2eb6f3a166720cb47d75d82ce9baf252d4e966b.mp4',
  '1bc37eeafdc71585d23f2550f7eba8cf90497edf.mp4',
  'e127c2e0619048e7bfa67ce15162a54ff1d5bcd4.mp4',
  '61f51a6c21075bcfa52d4751404d72b37f4fc445.mp4',
  'a00472903313b9a87fedd3a69a854e37c6037a8a.mp4',
];

export type AdminLoginPromoLayout = {
  width: number;
  height: number;
  marginTop: number;
  marginLeft: number;
  background: string;
  transform: string;
};

/** 与 CreatorPromo 原始布局一致，共 7 槽位 */
export const ADMIN_LOGIN_PROMO_LAYOUT: AdminLoginPromoLayout[] = [
  {
    width: 200,
    height: 200,
    marginTop: 110,
    marginLeft: 0,
    background: 'rgb(246, 214, 231)',
    transform: 'translate(14px, -8px)',
  },
  {
    width: 145,
    height: 145,
    marginTop: 350,
    marginLeft: 100,
    background: 'rgb(246, 243, 214)',
    transform: 'translate(7px, -9px)',
  },
  {
    width: 260,
    height: 260,
    marginTop: 120,
    marginLeft: 250,
    background: 'rgb(215, 237, 250)',
    transform: 'translate(10px, -16px)',
  },
  {
    width: 155,
    height: 155,
    marginTop: -10,
    marginLeft: 470,
    background: 'rgb(246, 214, 214)',
    transform: 'translate(2px, -7px)',
  },
  {
    width: 130,
    height: 130,
    marginTop: 100,
    marginLeft: 610,
    background: 'rgb(201, 217, 215)',
    transform: 'translate(-5px, -9px)',
  },
  {
    width: 90,
    height: 90,
    marginTop: 210,
    marginLeft: 740,
    background: 'rgb(246, 225, 214)',
    transform: 'translate(-4px, -5px)',
  },
  {
    width: 210,
    height: 210,
    marginTop: 275,
    marginLeft: 530,
    background: 'rgb(204, 224, 240)',
    transform: 'translate(-4px, -16px)',
  },
];

export const ADMIN_LOGIN_PROMO_SLOT_COUNT = ADMIN_LOGIN_PROMO_LAYOUT.length;

export function getBuiltinVideoUrl(filename: string): string {
  return new URL(`../assets/videos/${filename}`, import.meta.url).href;
}

/** 供管理端表单初始化的 DTO 形状 */
export function buildDefaultAdminLoginPromoDtoList() {
  return ADMIN_LOGIN_PROMO_LAYOUT.map((layout) => ({
    enabled: true,
    videoUrl: '',
    imageUrl: '',
    width: layout.width,
    height: layout.height,
    marginTop: layout.marginTop,
    marginLeft: layout.marginLeft,
    background: layout.background,
    transform: layout.transform,
  }));
}
