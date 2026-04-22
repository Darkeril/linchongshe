/**
 * 笔记列表 mock。
 *
 * 约定（来自 phase-4-waterfall.md + 原则 5）：
 * - 3 个 feed（follow / discover / nearby）各 30 条，3 页 × 10 条
 * - 数据只在首次 build 时生成，之后保持稳定（保证刷新一致的视觉验证）
 * - 点赞 mock 持久到内存里，切 tab 回来状态保留
 */
import type {
  LikeNoteResult,
  NoteFeedId,
  NoteListItem,
  NoteListParams,
  NoteListResult,
  PetVariant,
} from '@/types/note';
import { delay } from '@/api/mock/user.mock';

/** 4 种宠物 variant 循环 */
const VARIANTS: PetVariant[] = ['dog', 'cat', 'mochi', 'puppy'];

/** 标签池（设计稿 screens-part2.jsx 行 7-12 + 扩展） */
const TAGS: Array<string | undefined> = [
  '萌宠日常',
  '美食分享',
  '洗护',
  '出行',
  '医疗',
  '训练',
  undefined,
  undefined, // 一部分卡片没标签，和设计稿一致
];

/** 标题模板池（10 条循环 + seed 微调） */
const TITLES: string[] = [
  '我家毛孩子又在拆家了，真是又气又好笑',
  '新买的猫窝它不睡，偏要挤进纸盒',
  '今天第一次带狗狗爬山，它兴奋得飞起',
  '分享一个自制宠物零食配方，超简单',
  '橘猫的一百种睡姿，你家的喜欢哪一种',
  '洗完澡的柴柴变成了一颗毛球，救命',
  '新手养狗三个月，来说说踩过的坑',
  '这家宠物店的美容师太会了，宝贝帅爆',
  '布偶猫的日常，治愈一整天的疲惫',
  '自制宠物冰淇淋，天热给毛孩子降降温',
];

/** 用户昵称池 */
const USERS: string[] = [
  '汤圆麻麻',
  '橘座大人',
  '小饼干食堂',
  '阿汪爸爸',
  'Lulu',
  'momo',
  '奶油小糕',
  'Tommy粑粑',
  '毛绒绒收藏家',
  '猫薄荷爱好者',
];

/** 简易 seed 随机（不用 Math.random 保证构建稳定） */
function seeded(seed: number): number {
  // 线性同余，稳定伪随机
  return ((seed * 9301 + 49297) % 233280) / 233280;
}

function pick<T>(arr: T[], seed: number): T {
  return arr[Math.floor(seeded(seed) * arr.length)] as T;
}

/**
 * 根据 feed + 全局序号生成一条笔记。
 * feed 作为 seed 前缀，保证 3 个 feed 数据不同。
 */
function makeNote(feed: NoteFeedId, globalIdx: number): NoteListItem {
  const feedSeedBase = feed === 'follow' ? 1000 : feed === 'discover' ? 2000 : 3000;
  const s = feedSeedBase + globalIdx;
  const variant = VARIANTS[globalIdx % VARIANTS.length] as PetVariant;
  // 高度 170-260px（严格对齐设计稿 NOTES_DATA：170/180/200/220/240/260）
  // 基础宽度 180 → 高宽比 0.94-1.44，接近正方形到偏长方形
  const h = 170 + Math.floor(seeded(s + 11) * 91); // 170..260
  // 点赞 0-5000
  const likes = Math.floor(seeded(s + 23) * 5000);
  // 30% 概率初始已点赞（和设计稿风格一致：部分卡片初始红心）
  const liked = seeded(s + 31) < 0.3;
  const tag = TAGS[Math.floor(seeded(s + 7) * TAGS.length)];
  const title = TITLES[globalIdx % TITLES.length] as string;
  const user = USERS[globalIdx % USERS.length] as string;
  const avatarVariant = VARIANTS[(globalIdx + 1) % VARIANTS.length] as PetVariant;

  return {
    id: `${feed}-${globalIdx + 1}`,
    title,
    variant,
    tag,
    h,
    user,
    avatarVariant,
    likes,
    liked,
  };
}

/** 每个 feed 预生成 30 条，内存持有（模拟后端数据库） */
const STORE: Record<NoteFeedId, NoteListItem[]> = {
  follow: Array.from({ length: 30 }, (_, i) => makeNote('follow', i)),
  discover: Array.from({ length: 30 }, (_, i) => makeNote('discover', i)),
  nearby: Array.from({ length: 30 }, (_, i) => makeNote('nearby', i)),
};

/** 列表分页 mock */
export function mockGetNoteList(params: NoteListParams): Promise<NoteListResult> {
  const all = STORE[params.feed] ?? [];
  const start = (params.page - 1) * params.size;
  const end = start + params.size;
  const slice = all.slice(start, end).map((n) => ({ ...n })); // 返回浅拷贝，防止外部修改 store
  const hasMore = end < all.length;
  return delay(
    {
      list: slice,
      hasMore,
      page: params.page,
    },
    300,
  );
}

/** 点赞 mock（在 store 里改状态，保证返回同一条后再切 tab 回来点赞状态保留） */
export function mockLikeNote(noteId: string): Promise<LikeNoteResult> {
  // 按 id 前缀定位到哪个 feed
  const feedId = (noteId.split('-')[0] ?? 'discover') as NoteFeedId;
  const list = STORE[feedId] ?? [];
  const note = list.find((n) => n.id === noteId);
  if (!note) {
    return Promise.reject(new Error(`mock: note ${noteId} not found`));
  }
  note.liked = !note.liked;
  note.likes = Math.max(0, note.likes + (note.liked ? 1 : -1));
  return delay({ liked: note.liked, likes: note.likes }, 200);
}
