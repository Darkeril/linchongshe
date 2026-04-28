/**
 * 笔记列表 mock。
 *
 * 约定（来自 phase-4-waterfall.md + 原则 5）：
 * - 3 个 feed（follow / discover / nearby）各 30 条，3 页 × 10 条
 * - 数据只在首次 build 时生成，之后保持稳定（保证刷新一致的视觉验证）
 * - 点赞 mock 持久到内存里，切 tab 回来状态保留
 */
import type {
  FollowResult,
  LikeNoteResult,
  NoteAuthor,
  NoteDetail,
  NoteFeedId,
  NoteImage,
  NoteListItem,
  NoteListParams,
  NoteListResult,
  PetVariant,
  PublishNotePayload,
  PublishNoteResult,
  SaveNoteResult,
} from '@/types/note';
import { delay, mockUserProfile } from '@/api/mock/user.mock';

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

// ─────────────────────────────────────────────────────────────
// 笔记详情 mock（Phase 5a 新增）
// ─────────────────────────────────────────────────────────────

/** 正文模板池（3 段组合渲染） */
const CONTENT_TEMPLATES: string[] = [
  '记录一下汤圆每周的洗澡日常～这次用了新的沐浴露，洗完之后毛特别柔软好摸。吹干之后整个变成了一个大毛球，傻乎乎地坐在那里，忍不住多拍了几张。\n\n有推荐的狗狗沐浴露吗？最好是香味淡一点的，汤圆对味道比较敏感。',
  '今天第一次带小家伙去宠物友好咖啡馆，意外地乖，趴在我脚边一动不动。\n\n分享一下装备清单：牵引绳（防挣脱款）+ 吸水垫 + 零食条。坐标上海，想交流的姐妹评论区留言。',
  '养了三年柴柴总结的冷知识：\n1. 柴柴的耳朵每天要用棉签轻轻擦一圈；\n2. 夏天不能剃光毛，会影响散热；\n3. 零食比玩具更能建立信任。\n\n希望对新手有帮助～',
  '自制南瓜鸡肉饼教程来啦！食材只要鸡胸肉 + 南瓜泥 + 一点点鸡蛋清，烤箱 160° 25 分钟搞定。\n\n家里两只毛孩子闻到就冲过来，完全不掉渣，绝对值得收藏。',
];

/** 话题标签池（不带 # 前缀） */
const TOPIC_POOL: string[] = [
  '金毛日记',
  '洗澡日常',
  '毛绒绒',
  '喵星人',
  '宠物食谱',
  '出行攻略',
  '新手指南',
  '萌宠互动',
  '日常vlog',
];

/** 城市池 */
const CITY_POOL: string[] = ['上海', '北京', '深圳', '杭州', '成都', '广州'];

/** 笔记详情缓存（id → detail）：多次进入同一条笔记保持状态一致 */
const DETAIL_CACHE: Map<string, NoteDetail> = new Map();

/** 作者缓存（authorId → NoteAuthor）：跨笔记共享关注状态 */
const AUTHOR_CACHE: Map<string, NoteAuthor> = new Map();

/** 根据字符串生成稳定 hash（用于派生 author.id 等） */
function hashStr(str: string): number {
  let h = 0;
  for (let i = 0; i < str.length; i += 1) {
    h = (h * 31 + str.charCodeAt(i)) & 0x7fffffff;
  }
  return h;
}

/** 根据 feed + id 在 STORE 里查找 NoteListItem */
function findInStore(noteId: string): NoteListItem | undefined {
  const feedId = (noteId.split('-')[0] ?? 'discover') as NoteFeedId;
  const list = STORE[feedId] ?? [];
  return list.find((n) => n.id === noteId);
}

/** 从缓存或构造一个 NoteAuthor（同作者跨笔记共享关注状态） */
function resolveAuthor(base: NoteListItem): NoteAuthor {
  const authorId = `u-${hashStr(base.user)}`;
  const cached = AUTHOR_CACHE.get(authorId);
  if (cached) return cached;
  const author: NoteAuthor = {
    id: authorId,
    name: base.user,
    avatarVariant: base.avatarVariant,
    followed: false,
  };
  AUTHOR_CACHE.set(authorId, author);
  return author;
}

/**
 * 根据 id 构造笔记详情（首次构造后缓存，保证点赞/收藏/关注在进入-返回-再进入时状态一致）。
 */
export function buildNoteDetail(id: string): NoteDetail | null {
  const cached = DETAIL_CACHE.get(id);
  if (cached) return cached;

  const base = findInStore(id);
  if (!base) return null;

  const hashBase = hashStr(id);
  // 3-5 张图片
  const imageCount = 3 + (hashBase % 3); // 3 / 4 / 5
  const images: NoteImage[] = Array.from({ length: imageCount }, (_, i) => ({
    url: '',
    variant: VARIANTS[(hashBase + i) % VARIANTS.length] as PetVariant,
    seed: `${id}-${i}`,
  }));

  // 正文：按 hash 选一条模板
  const content = CONTENT_TEMPLATES[hashBase % CONTENT_TEMPLATES.length] as string;

  // 话题：2-4 个，从 TOPIC_POOL 取
  const topicCount = 2 + (hashBase % 3); // 2 / 3 / 4
  const topics: string[] = [];
  for (let i = 0; i < topicCount; i += 1) {
    const idx = (hashBase + i * 7) % TOPIC_POOL.length;
    const t = TOPIC_POOL[idx] as string;
    if (!topics.includes(t)) topics.push(t);
  }

  const saves = Math.floor(seeded(hashBase + 41) * 2000);
  const comments = 5 + Math.floor(seeded(hashBase + 59) * 295); // 5..300
  const hoursAgo = 1 + Math.floor(seeded(hashBase + 67) * 23); // 1..23
  const editedAt = `${hoursAgo} 小时前`;
  const fromCity = CITY_POOL[hashBase % CITY_POOL.length] as string;

  const detail: NoteDetail = {
    ...base,
    images,
    content,
    topics,
    author: resolveAuthor(base),
    saves,
    saved: false,
    comments,
    editedAt,
    fromCity,
  };
  DETAIL_CACHE.set(id, detail);
  return detail;
}

/** 详情查询 mock（返回 store 里同一个 note 引用的字段副本，点赞状态与列表同步） */
export function mockGetNoteDetail(id: string): Promise<NoteDetail> {
  const detail = buildNoteDetail(id);
  if (!detail) {
    return Promise.reject(new Error(`mock: note ${id} not found`));
  }
  // 用 store 里的最新 liked / likes 覆盖（列表点赞后再进详情，状态保持一致）
  const base = findInStore(id);
  if (base) {
    detail.liked = base.liked;
    detail.likes = base.likes;
  }
  return delay(detail, 260);
}

/** 收藏 mock（in-place 切换缓存里的 saved/saves） */
export function mockToggleSave(noteId: string): Promise<SaveNoteResult> {
  const detail = DETAIL_CACHE.get(noteId) ?? buildNoteDetail(noteId);
  if (!detail) {
    return Promise.reject(new Error(`mock: note ${noteId} not found`));
  }
  detail.saved = !detail.saved;
  detail.saves = Math.max(0, detail.saves + (detail.saved ? 1 : -1));
  return delay({ saved: detail.saved, saves: detail.saves }, 200);
}

/** 关注 mock（in-place 切换 AUTHOR_CACHE 里的 followed） */
export function mockToggleFollow(authorId: string): Promise<FollowResult> {
  const author = AUTHOR_CACHE.get(authorId);
  if (!author) {
    return Promise.reject(new Error(`mock: author ${authorId} not found`));
  }
  author.followed = !author.followed;
  return delay({ followed: author.followed }, 200);
}

// ─────────────────────────────────────────────────────────────
// 发布笔记 mock（Phase 7 新增）
// ─────────────────────────────────────────────────────────────

let publishSeq = 0;

function normalizeTopics(topics: string[]): string[] {
  const cleaned: string[] = [];
  for (const raw of topics) {
    const t = raw.replace(/^#/, '').trim();
    if (t && !cleaned.includes(t)) cleaned.push(t);
  }
  return cleaned;
}

function makePublishedImages(payload: PublishNotePayload, id: string): NoteImage[] {
  if (payload.images.length === 0) {
    return [{
      url: '',
      variant: 'dog',
      seed: `${id}-empty`,
    }];
  }
  return payload.images.slice(0, 9).map((img, i) => ({
    url: img.url,
    variant: img.variant,
    seed: img.seed || `${id}-${i}`,
  }));
}

/** 发布笔记：插入 discover feed，并写入 detail 缓存，保证发布后可直接进详情。 */
export function mockPublishNote(payload: PublishNotePayload): Promise<PublishNoteResult> {
  publishSeq += 1;
  const id = `discover-published-${Date.now()}-${publishSeq}`;
  const topics = normalizeTopics(payload.topics);
  const images = makePublishedImages(payload, id);
  const cover = images[0]!;
  const author: NoteAuthor = {
    id: mockUserProfile.id,
    name: mockUserProfile.nickname,
    avatarVariant: 'mochi',
    followed: true,
  };
  AUTHOR_CACHE.set(author.id, author);

  const base: NoteListItem = {
    id,
    title: payload.title,
    coverUrl: cover.url,
    variant: cover.variant,
    tag: topics[0],
    h: 220,
    user: mockUserProfile.nickname,
    avatarVariant: author.avatarVariant,
    likes: 0,
    liked: false,
  };

  STORE.discover.unshift(base);
  const detail: NoteDetail = {
    ...base,
    images,
    content: payload.content,
    topics,
    author,
    saves: 0,
    saved: false,
    comments: 0,
    editedAt: '刚刚',
    fromCity: payload.location?.split('·')[0]?.trim() || '上海',
  };
  DETAIL_CACHE.set(id, detail);

  return delay({ id, status: 'published' }, 520);
}
