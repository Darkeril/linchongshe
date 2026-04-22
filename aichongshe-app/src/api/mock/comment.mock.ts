/**
 * 笔记评论 mock（Phase 5b）。
 *
 * 约定：
 * - 每个 noteId 首次访问时，伪随机生成 10-20 条一级评论
 * - 每条一级 0-5 条 reply，30% 回复带 @提及
 * - STORE 持久化在内存：点赞 / 新增都 in-place mutation（gotcha #18）
 * - seeded() 随机与 note.mock.ts 一致，保证构建稳定
 */
import type { PetVariant } from '@/types/note';
import type {
  CommentItem,
  CommentListParams,
  CommentListResult,
  CommentThread,
  CommentUser,
  LikeCommentResult,
  PostCommentParams,
} from '@/types/comment';
import { delay } from '@/api/mock/user.mock';

/** 4 种宠物 variant 循环（与 note.mock.ts 保持一致） */
const VARIANTS: PetVariant[] = ['dog', 'cat', 'mochi', 'puppy'];

/** 评论用户池 */
const USER_POOL: CommentUser[] = [
  { id: 'cu-1', name: '汤圆麻麻', avatarVariant: 'dog' },
  { id: 'cu-2', name: '橘座大人', avatarVariant: 'cat' },
  { id: 'cu-3', name: '小饼干食堂', avatarVariant: 'mochi' },
  { id: 'cu-4', name: '阿汪爸爸', avatarVariant: 'puppy' },
  { id: 'cu-5', name: 'Lulu', avatarVariant: 'cat' },
  { id: 'cu-6', name: 'momo', avatarVariant: 'dog' },
  { id: 'cu-7', name: '奶油小糕', avatarVariant: 'mochi' },
  { id: 'cu-8', name: 'Tommy粑粑', avatarVariant: 'puppy' },
  { id: 'cu-9', name: '毛绒绒收藏家', avatarVariant: 'dog' },
  { id: 'cu-10', name: '猫薄荷爱好者', avatarVariant: 'cat' },
];

/** 一级评论文本池 */
const ROOT_TEXTS: string[] = [
  '太可爱了吧！看一次笑一次，我家也是这样，天天被气笑',
  '这个角度拍得真好，毛色看着特别顺滑，用的啥梳子求推荐',
  '楼主家宝贝好乖！我家一洗澡就跟杀猪似的嚎，真羡慕',
  '收藏了，准备照着做，感谢分享！',
  '请问你家多大啦？看着好小只，戳手手',
  '同款橘猫，感觉你家的明显比我家的瘦，是不是有啥运动秘籍',
  '这配方看起来不错，家里毛孩子挑食，打算试一下',
  '我家柴柴也爱扒拉纸箱，给它买了窝不睡非睡箱子里，哭笑不得',
  '上海哪家宠物店？求坐标，想带我家狗子去',
  '楼主拍照技术绝了，像杂志封面',
  '看这眼神就知道肯定又干坏事了 哈哈哈',
  '我家这两天也拉稀，怎么办啊急',
  '一下子被治愈了，谢谢楼主',
  '多少钱做一次？感觉性价比挺高的',
  '话说这个牌子的零食真的吗，会不会上火',
];

/** 回复文本池 */
const REPLY_TEXTS: string[] = [
  '哈哈哈，我家也是',
  '同感，天天气死',
  '用的是小佩的直排梳，超好用',
  '3 个月大，刚到家一周',
  '减肥靠少喂 + 多逗猫棒',
  '我做了 2 次了，孩子爱吃',
  '坐标人广附近，私聊',
  '谢谢支持！',
  '我家的也一样，主子都爱箱子',
  '先去医院查个便便，别耽误',
  '这家是我邻居开的，放心',
  '没上火，吃了半年了',
  '直接给毛孩子试试，吃得香',
  '改天带它去体检一下',
];

/** 简易 seed 随机（与 note.mock.ts 同一实现，保证稳定） */
function seeded(seed: number): number {
  return ((seed * 9301 + 49297) % 233280) / 233280;
}

/** 生成稳定 hash（字符串 → 数字） */
function hashStr(str: string): number {
  let h = 0;
  for (let i = 0; i < str.length; i += 1) {
    h = (h * 31 + str.charCodeAt(i)) & 0x7fffffff;
  }
  return h;
}

/** STORE：noteId → 其下的 thread 列表（按时间倒序，即最新在前） */
const STORE: Map<string, CommentThread[]> = new Map();

/** 时间描述池（假） */
const TIME_DESCS: string[] = [
  '5 分钟前',
  '18 分钟前',
  '1 小时前',
  '2 小时前',
  '5 小时前',
  '昨天',
  '2 天前',
  '3 天前',
];

/** 根据 noteId 生成 threads */
function buildThreads(noteId: string): CommentThread[] {
  const base = hashStr(noteId);
  // 10-20 条一级
  const rootCount = 10 + (base % 11);
  const threads: CommentThread[] = [];

  for (let i = 0; i < rootCount; i += 1) {
    const seed = base + i * 13;
    const author = USER_POOL[Math.floor(seeded(seed + 1) * USER_POOL.length)] as CommentUser;
    const text = ROOT_TEXTS[Math.floor(seeded(seed + 3) * ROOT_TEXTS.length)] as string;
    const likes = Math.floor(seeded(seed + 5) * 320);
    const liked = seeded(seed + 7) < 0.2;
    const createdAt = TIME_DESCS[Math.floor(seeded(seed + 9) * TIME_DESCS.length)] as string;

    const root: CommentItem = {
      id: `c-${noteId}-r-${i}`,
      noteId,
      author,
      text,
      likes,
      liked,
      createdAt,
    };

    // 0-5 条 reply
    const replyCount = Math.floor(seeded(seed + 11) * 6);
    const replies: CommentItem[] = [];
    for (let j = 0; j < replyCount; j += 1) {
      const rSeed = seed + j * 17 + 101;
      const rAuthor = USER_POOL[Math.floor(seeded(rSeed + 1) * USER_POOL.length)] as CommentUser;
      const rText = REPLY_TEXTS[Math.floor(seeded(rSeed + 3) * REPLY_TEXTS.length)] as string;
      const rLikes = Math.floor(seeded(rSeed + 5) * 80);
      const rLiked = seeded(rSeed + 7) < 0.15;
      const rTime = TIME_DESCS[Math.floor(seeded(rSeed + 9) * TIME_DESCS.length)] as string;

      // 30% 概率带 @提及（指向池里其他用户）
      let replyTo: CommentUser | undefined;
      if (seeded(rSeed + 13) < 0.3) {
        const target = USER_POOL[Math.floor(seeded(rSeed + 15) * USER_POOL.length)] as CommentUser;
        if (target.id !== rAuthor.id) {
          replyTo = target;
        }
      }

      replies.push({
        id: `c-${noteId}-r-${i}-re-${j}`,
        noteId,
        author: rAuthor,
        text: rText,
        likes: rLikes,
        liked: rLiked,
        createdAt: rTime,
        ...(replyTo ? { replyTo } : {}),
      });
    }

    threads.push({ root, replies });
  }

  return threads;
}

/** 获取或构建某 noteId 的 thread 列表（持久化） */
function ensureThreads(noteId: string): CommentThread[] {
  let threads = STORE.get(noteId);
  if (!threads) {
    threads = buildThreads(noteId);
    STORE.set(noteId, threads);
  }
  return threads;
}

/** 统计 thread 总条数（root + 所有 replies） */
function countAll(threads: CommentThread[]): number {
  let n = 0;
  for (const t of threads) {
    n += 1 + t.replies.length;
  }
  return n;
}

// ─────────────────────────────────────────────────────────────
// mock 接口
// ─────────────────────────────────────────────────────────────

export function mockGetComments(params: CommentListParams): Promise<CommentListResult> {
  const threads = ensureThreads(params.noteId);
  const start = (params.page - 1) * params.size;
  const end = start + params.size;
  const slice = threads.slice(start, end);
  const hasMore = end < threads.length;
  return delay(
    {
      threads: slice,
      hasMore,
      page: params.page,
      total: countAll(threads),
    },
    260,
  );
}

export function mockPostComment(params: PostCommentParams): Promise<CommentItem> {
  const threads = ensureThreads(params.noteId);
  // 当前用户（假）：用 USER_POOL[0] 作为"我"
  const me: CommentUser = { id: 'me', name: '我', avatarVariant: 'mochi' };
  const now = Date.now();

  if (!params.rootId) {
    // 新一级评论，插到顶部
    const root: CommentItem = {
      id: `c-${params.noteId}-r-new-${now}`,
      noteId: params.noteId,
      author: me,
      text: params.text,
      likes: 0,
      liked: false,
      createdAt: '刚刚',
    };
    threads.unshift({ root, replies: [] });
    return delay(root, 200);
  }

  // 回复：找到对应 thread
  const target = threads.find((t) => t.root.id === params.rootId);
  if (!target) {
    return Promise.reject(new Error(`mock: thread ${params.rootId} not found`));
  }
  const reply: CommentItem = {
    id: `c-${params.noteId}-re-new-${now}`,
    noteId: params.noteId,
    author: me,
    text: params.text,
    likes: 0,
    liked: false,
    createdAt: '刚刚',
    ...(params.replyTo ? { replyTo: params.replyTo } : {}),
  };
  target.replies.push(reply);
  return delay(reply, 200);
}

export function mockLikeComment(commentId: string): Promise<LikeCommentResult> {
  // commentId 形如 c-<noteId>-r-<i> 或 c-<noteId>-r-<i>-re-<j>
  // 反解 noteId
  const parts = commentId.split('-');
  // 协议：c - <feed> - <idx> - r - <i>  ... 取前两段 feed-idx 作为 noteId
  // 例：c-follow-5-r-2 → noteId = follow-5
  if (parts.length < 5) {
    return Promise.reject(new Error(`mock: bad commentId ${commentId}`));
  }
  const noteId = `${parts[1]}-${parts[2]}`;
  const threads = STORE.get(noteId);
  if (!threads) {
    return Promise.reject(new Error(`mock: threads for ${noteId} not loaded`));
  }

  // 在 root 或 reply 里 in-place 切换（gotcha #18）
  for (const t of threads) {
    if (t.root.id === commentId) {
      t.root.liked = !t.root.liked;
      t.root.likes = Math.max(0, t.root.likes + (t.root.liked ? 1 : -1));
      return delay({ liked: t.root.liked, likes: t.root.likes }, 180);
    }
    const r = t.replies.find((x) => x.id === commentId);
    if (r) {
      r.liked = !r.liked;
      r.likes = Math.max(0, r.likes + (r.liked ? 1 : -1));
      return delay({ liked: r.liked, likes: r.likes }, 180);
    }
  }
  return Promise.reject(new Error(`mock: comment ${commentId} not found`));
}
