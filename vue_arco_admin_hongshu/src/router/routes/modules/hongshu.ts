import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const SYSTEM: AppRouteRecordRaw = {
    path: '/member',
    name: 'Member',
    component: DEFAULT_LAYOUT,
    meta: {
        locale: 'menu.web.member',
        title: '会员中心',
        requiresAuth: true,
        icon: 'IconStorage',
        order: 1,
    },
    children: [
        {
            path: 'member',
            name: 'MemberManagement',
            component: () => import('@/views/web/member/index.vue'),
            meta: {
                locale: 'menu.web.member',
                title: '会员管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },
        {
            path: 'memberAudit',
            alias: 'userAudit',
            name: 'MemberUserAudit',
            component: () => import('@/views/web/member/audit.vue'),
            meta: {
                locale: 'menu.web.memberAudit',
                title: '用户审核',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },
    ],
};

const NOTE: AppRouteRecordRaw = {
    path: '/note',
    name: 'Note',
    component: DEFAULT_LAYOUT,
    meta: {
        locale: 'menu.web.note',
        title: '笔记管理',
        requiresAuth: true,
        icon: 'IconStorage',
        order: 1,
    },
    children: [
        {
            path: 'navbar',
            name: 'NoteNavbar',
            component: () => import('@/views/web/navbar/index.vue'),
            meta: {
                locale: 'menu.web.navbar',
                title: '导航栏管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },
        {
            path: 'note',
            name: 'NoteManagement',
            component: () => import('@/views/web/note/index.vue'),
            meta: {
                locale: 'menu.web.note',
                title: '笔记管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },

        {
            path: 'audit',
            /** 与 sys_menu 中 path=noteAudit 对齐，避免仅动态菜单能打开 /note/noteAudit */
            alias: 'noteAudit',
            name: 'NoteAudit',
            component: () => import('@/views/web/audit/index.vue'),
            meta: {
                locale: 'menu.web.audit',
                title: '审核管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },
        {
            path: 'tag',
            name: 'Tag',
            component: () => import('@/views/web/tag/index.vue'),
            meta: {
                locale: 'menu.web.tag',
                title: '标签管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },
        {
            path: 'album',
            name: 'Album',
            component: () => import('@/views/web/album/index.vue'),
            meta: {
                locale: 'menu.web.album',
                title: '专辑管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },
        {
            path: 'comment',
            name: 'Comment',
            component: () => import('@/views/web/comment/index.vue'),
            meta: {
                locale: 'menu.web.comment',
                title: '评论管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },
        {
            path: 'groupChat',
            name: 'GroupChat',
            component: () => import('@/views/web/groupChat/index.vue'),
            meta: {
                locale: 'menu.web.groupChat',
                title: '群聊管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconMessage',
            },
        },
        {
            path: 'customerService',
            name: 'CustomerService',
            component: () => import('@/views/web/customerService/index.vue'),
            meta: {
                locale: 'menu.web.customerService',
                title: '客服管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconCustomerService',
            },
        },
        {
            path: 'comment-data/:id',
            name: 'CommentData',
            component: () => import('@/views/web/comment/data/index.vue'),
            meta: {
                title: '子评论管理',
                requiresAuth: true,
                roles: ['*'],
                hideInMenu: true,
            },
        },
        {
            path: 'xiaohongshu',
            name: 'XiaohongshuSpider',
            component: () => import('@/views/web/xiaohongshu/index.vue'),
            meta: {
                locale: 'menu.web.xiaohongshu',
                title: '小红书爬虫',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconImage',
                hideInMenu: true, // 隐藏菜单，但不影响路由访问
            },
        },
    ],
};

const IDLE: AppRouteRecordRaw = {
    path: '/idle',
    name: 'Idle',
    component: DEFAULT_LAYOUT,
    meta: {
        locale: 'menu.idle',
        title: '闲宝管理',
        requiresAuth: true,
        icon: 'IconStorage',
        order: 3,
    },
    children: [
        {
            path: 'navbar',
            name: 'IdleNavbar',
            component: () => import('@/views/idle/navbar/index.vue'),
            meta: {
                locale: 'menu.idle.navbar',
                title: '分类管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },
        {
            path: 'product',
            name: 'Product',
            component: () => import('@/views/idle/product/index.vue'),
            meta: {
                locale: 'menu.idle.product',
                title: '商品管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },
        {
            path: 'audit',
            name: 'IdleAudit',
            component: () => import('@/views/idle/audit/index.vue'),
            meta: {
                locale: 'menu.idle.audit',
                title: '审核管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },
        {
            path: 'order',
            name: 'Order',
            component: () => import('@/views/idle/order/index.vue'),
            meta: {
                locale: 'menu.idle.order',
                title: '订单管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
        },
    ],
};

const CHAT: AppRouteRecordRaw = {
    path: '/chat',
    name: 'Chat',
    component: DEFAULT_LAYOUT,
    meta: {
        locale: 'menu.chat',
        title: 'AI管理',
        requiresAuth: true,
        icon: 'IconSettings',
        order: 4,
    },
    children: [
        {
            path: 'conversation',
            name: 'ChatConversation',
            component: () => import('@/views/chat/conversation/index.vue'),
            redirect: '/chat/conversation/chat',
            meta: {
                locale: 'menu.chat.conversation',
                title: '对话管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconMessage',
            },
            children: [
                {
                    path: 'chat',
                    name: 'ChatConversationChat',
                    component: () => import('@/views/chat/conversation/chat/index.vue'),
                    meta: {
                        locale: 'menu.chat.conversation.chat',
                        title: '聊天管理',
                        requiresAuth: true,
                        roles: ['*'],
                        icon: 'IconMessage',
                    },
                },
                {
                    path: 'message',
                    name: 'ChatConversationMessage',
                    component: () => import('@/views/chat/conversation/message/index.vue'),
                    meta: {
                        locale: 'menu.chat.conversation.message',
                        title: '消息管理',
                        requiresAuth: true,
                        roles: ['*'],
                        icon: 'IconNotification',
                    },
                },
            ],
        },
        {
            path: 'order',
            name: 'ChatOrder',
            component: () => import('@/views/chat/order/index.vue'),
            meta: {
                locale: 'menu.chat.order',
                title: '订单管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconList',
            },
            children: [
                {
                    path: 'order',
                    name: 'ChatOrderList',
                    component: () => import('@/views/chat/order/order/index.vue'),
                    meta: {
                        locale: 'menu.chat.order.list',
                        title: '订单管理',
                        requiresAuth: true,
                        roles: ['*'],
                        icon: 'IconList',
                    },
                },
                {
                    path: 'redemption',
                    name: 'ChatOrderRedemption',
                    component: () => import('@/views/chat/order/redemption/index.vue'),
                    meta: {
                        locale: 'menu.chat.order.redemption',
                        title: '兑换码管理',
                        requiresAuth: true,
                        roles: ['*'],
                        icon: 'IconGift',
                    },
                },
            ],
        },
        {
            path: 'member',
            name: 'ChatMember',
            component: () => import('@/views/chat/member/index.vue'),
            redirect: '/chat/member/user',
            meta: {
                locale: 'menu.chat.member',
                title: '会员管理',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconUser',
            },
            children: [
                {
                    path: 'user',
                    name: 'ChatMemberUser',
                    component: () => import('@/views/chat/member/user/index.vue'),
                    meta: {
                        locale: 'menu.chat.member.user',
                        title: '会员列表',
                        requiresAuth: true,
                        roles: ['*'],
                        icon: 'IconUser',
                    },
                },
                {
                    path: 'comb',
                    name: 'ChatMemberComb',
                    component: () => import('@/views/chat/member/comb/index.vue'),
                    meta: {
                        locale: 'menu.chat.member.comb',
                        title: '套餐配置',
                        requiresAuth: true,
                        roles: ['*'],
                        icon: 'IconStorage',
                    },
                },
            ],
        },
        {
            path: 'assistant',
            name: 'ChatAssistant',
            component: () => import('@/views/chat/assistant/index.vue'),
            redirect: '/chat/assistant/type',
            meta: {
                locale: 'menu.chat.assistant',
                title: '助手中心',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconBulb',
            },
            children: [
                {
                    path: 'type',
                    name: 'ChatAssistantType',
                    component: () => import('@/views/chat/assistant/type/index.vue'),
                    meta: {
                        locale: 'menu.chat.assistant.type',
                        title: '助手分类',
                        requiresAuth: true,
                        roles: ['*'],
                        icon: 'IconBulb',
                    },
                },
                {
                    path: 'list',
                    name: 'ChatAssistantList',
                    component: () => import('@/views/chat/assistant/list/index.vue'),
                    meta: {
                        locale: 'menu.chat.assistant.list',
                        title: '助手管理',
                        requiresAuth: true,
                        roles: ['*'],
                        icon: 'IconSettings',
                    },
                },
            ],
        },
        {
            path: 'config',
            name: 'ChatConfig',
            component: () => import('@/views/chat/config/index.vue'),
            redirect: '/chat/config/model',
            meta: {
                locale: 'menu.chat.config',
                title: '配置中心',
                requiresAuth: true,
                roles: ['*'],
                icon: 'IconSettings',
            },
            children: [
                {
                    path: 'model',
                    name: 'ChatConfigModel',
                    component: () => import('@/views/chat/config/model/index.vue'),
                    meta: {
                        locale: 'menu.chat.config.model',
                        title: '大模型信息',
                        requiresAuth: true,
                        roles: ['*'],
                        icon: 'IconSettings',
                    },
                },
                {
                    path: 'token',
                    name: 'ChatConfigToken',
                    component: () => import('@/views/chat/config/token/index.vue'),
                    meta: {
                        locale: 'menu.chat.config.token',
                        title: 'token管理',
                        requiresAuth: true,
                        roles: ['*'],
                        icon: 'IconLock',
                    },
                },
            ],
        },
    ],
};


export default [SYSTEM, NOTE, IDLE, CHAT];
