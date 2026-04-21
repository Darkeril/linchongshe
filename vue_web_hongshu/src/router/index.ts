import { createRouter, createWebHistory, type RouteLocationNormalized } from "vue-router";
import Dashboard from "@/views/dashboard/dashboard.vue";

export const routes = [
  {
    path: "/",
    redirect: "/index",
  },
  /**
   * 登录 UI 实际由 index 布局内的 <Login> 弹层承载（与 /dashboard 一致）。
   * 独立 /login 路由仅渲染弹窗组件会导致整页灰底无侧栏/信息流，故统一重定向到首页并拉起登录层。
   */
  {
    name: "login",
    path: "/login",
    redirect: (to: RouteLocationNormalized) => ({
      path: "/dashboard",
      query: { ...to.query, showLogin: "1" },
    }),
  },
  {
    name: "index",
    path: "/index",
    component: () => import("@/views/index.vue"),
    redirect: "/dashboard",
    children: [
      {
        path: "/dashboard",
        component: Dashboard,
        name: "dashboard",
      },
      {
        path: "/noteTrend",
        component: () => import("@/views/note-trend/note-trend.vue"),
        name: "noteTrend",
      },
      {
        path: "/notice",
        component: () => import("@/views/message/index.vue"),
        name: "notice",
      },
      {
        path: "/user",
        component: () => import("@/views/user/index.vue"),
        name: "user",
      },
      {
        path: "/push",
        component: () => import("@/views/push/index.vue"),
        name: "push",
      },
      {
        path: "/search",
        component: () => import("@/views/search/index.vue"),
        name: "search",
      },
      {
        path: "/idle",
        component: () => import("@/views/idle/index.vue"),
        name: "idle",
      },
      {
        path: "/ai-chat",
        component: () => import("@/views/ai-chat/index.vue"),
        name: "ai-chat",
        meta: {
          requiresAuth: true, // 需要登录
          title: "AI智能助手"
        }
      },
      {
        path: "/idleTrend",
        component: () => import("@/views/idle-trend/trend.vue"),
        name: "idleTrend",
      },
      {
        path: "/orderCreate",
        component: () => import("@/views/idle-order/order-create.vue"),
        name: "orderCreate",
      },
      {
        path: "/orderDetail",
        component: () => import("@/views/idle-order/order-detail.vue"),
        name: "orderDetail",
      },
      {
        path: "/orderEvaluate",
        component: () => import("@/views/idle-order/order-evaluate.vue"),
        name: "orderEvaluate",
      },
      {
        path: "/paymentPay",
        component: () => import("@/views/idle-order/payment-pay.vue"),
        name: "paymentPay",
      },
      {
        path: "/feedback",
        component: () => import("@/views/feedback/index.vue"),
        name: "feedback",
        meta: {
          requiresAuth: true, // 需要登录
          title: "功能建议"
        }
      }
    ],
  },
];
const router = createRouter({
  scrollBehavior: () => ({ left: 0, top: 0 }),
  history: createWebHistory(),
  routes,
});
router.beforeEach((_to, _from, next) => {
  next();
});
export default router;
