import axios from 'axios';
import type { TableData } from '@arco-design/web-vue/es/table/interface';
import { successResponseWrap } from "@/utils/setup-mock";
import dayjs from "dayjs";
import qs from "query-string";

export interface ContentDataRecord {
  x: string;
  y: number;
}

export function queryContentData() {
  const count = 8;
  const array = new Array(count).fill(0).map((el, idx) => ({
    x: dayjs()
      .day(idx - 2)
      .format('YYYY-MM-DD'),
    y: [58, 81, 53, 90, 64, 88, 49, 79][idx],
  }));
  return successResponseWrap(array);
}

export interface PopularRecord {
  key: number;
  clickNumber: string;
  title: string;
  increases: number;
}

export function queryPopularList1(params: { type: string }) {
  const { type = 'text' } = params;
  if (type === 'image') {
    return successResponseWrap([...[
      {
        key: 1,
        clickNumber: '367.6w+',
        title: '这是今日10点的南京',
        increases: 5,
      },
      {
        key: 2,
        clickNumber: '352.2w+',
        title: '立陶宛不断挑衅致经济受损民众…',
        increases: 17,
      },
      {
        key: 3,
        clickNumber: '348.9w+',
        title: '韩国艺人刘在石确诊新冠',
        increases: 30,
      },
      {
        key: 4,
        clickNumber: '346.3w+',
        title: '关于北京冬奥会，文在寅表态',
        increases: 12,
      },
      {
        key: 5,
        clickNumber: '271.2w+',
        title: '95后现役军人荣立一等功',
        increases: 2,
      },
    ]]);
  }
  if (type === 'video') {
    return successResponseWrap([...[
      {
        key: 1,
        clickNumber: '15.3w+',
        title: '杨涛接替陆慷出任外交部美大司…',
        increases: 15,
      },
      {
        key: 2,
        clickNumber: '12.2w+',
        title: '图集：龙卷风袭击美国多州房屋…',
        increases: 26,
      },
      {
        key: 3,
        clickNumber: '18.9w+',
        title: '52岁大姐贴钱照顾自闭症儿童八…',
        increases: 9,
      },
      {
        key: 4,
        clickNumber: '7.9w+',
        title: '杭州一家三口公园宿营取暖中毒',
        increases: 0,
      },
      {
        key: 5,
        clickNumber: '5.2w+',
        title: '派出所副所长威胁市民？警方调…',
        increases: 4,
      },
    ]]);
  }
  return successResponseWrap([...[
    {
      key: 1,
      clickNumber: '346.3w+',
      title: '经济日报：财政政策要精准提升…',
      increases: 35,
    },
    {
      key: 2,
      clickNumber: '324.2w+',
      title: '双12遇冷，消费者厌倦了电商平…',
      increases: 22,
    },
    {
      key: 3,
      clickNumber: '318.9w+',
      title: '致敬坚守战“疫”一线的社区工作…',
      increases: 9,
    },
    {
      key: 4,
      clickNumber: '257.9w+',
      title: '普高还是职高？家长们陷入选择…',
      increases: 17,
    },
    {
      key: 5,
      clickNumber: '124.2w+',
      title: '人民快评：没想到“浓眉大眼”的…',
      increases: 37,
    },
  ]]);
}

export function queryPopularList(params: { type: string }) {
  return axios({
    url: `/web/index/getHotNote`,
    method: 'get',
    params: { noteType: params.type }

  })
}

export function init() {
  return axios({
    url: '/web/index/init',
    method: 'get'
  })
}

export function getVisitByWeek() {
  return axios({
    url: '/web/index/getVisitByWeek',
    method: 'get'
  })
}

// 兼容后端：第一个项目为 getNoteCountByType（笔记类型分布）
export function getBlogCountByTag() {
  return axios({
    url: '/web/index/getNoteCountByType',
    method: 'get'
  })
}

export function getBlogCountByBlogSort() {
  return axios({
    url: '/web/index/getBlogCountByBlogSort',
    method: 'get'
  })
}

export function getBlogContributeCount() {
  return axios({
    url: '/web/index/getBlogContributeCount',
    method: 'get'
  })
}

// 商品类型分布
export function getProductCountByType() {
  return axios({
    url: '/web/index/getProductCountByType',
    method: 'get'
  })
}

// 根据分类聚合的商品数量
export function getProductByBlogSort() {
  return axios({
    url: '/web/index/getProductByBlogSort',
    method: 'get'
  })
}

// 订单销售数据，days 仅 7 或 30
export function getOrderData(days: 7 | 30 = 7) {
  return axios({
    url: '/web/index/getOrderData',
    method: 'get',
    params: { days },
  })
}

// 每日新增趋势，days 仅 7 或 30
export function getDailyNewData(days: 7 | 30 = 7) {
  return axios({
    url: '/web/index/getDailyNewData',
    method: 'get',
    params: { days },
  });
}

/** 工作台-笔记互动效率，days 仅 7 或 30 */
export function getNoteInteractionEfficiency(days: 7 | 30 = 7) {
  return axios({
    url: '/web/index/getNoteInteractionEfficiency',
    method: 'get',
    params: { days },
  })
}

// 数据使用分析（迁移自项目5：/statistics/index/pie）
// 返回格式兼容：
// - 数组：[{ name, value }]
// - 对象：{ key: number, ... }
export function getUsagePieData(params?: Record<string, any>) {
  return axios({
    url: '/chat/statistics/index/pie',
    method: 'get',
    params,
  })
}

// 用户地理位置分布
export function getUserLocations() {
  return axios({
    url: '/web/index/getUserLocations',
    method: 'get'
  })
}

export interface LoginLocationStatRow {
  location: string;
  loginCount: number;
}

/** Web 端登录日志：近 days 天按地点 Top limit，days 为 1（当日）/7/30 */
export function getWebLoginLocationStats(params?: {
  days?: 1 | 7 | 30;
  limit?: number;
}) {
  return axios({
    url: '/web/index/getWebLoginLocationStats',
    method: 'get',
    params: { days: 1, limit: 8, ...params },
  });
}

/** 会员移动端（App/小程序）web_login_info 按地点 Top limit，days 为 1/7/30 */
export function getMobileLoginLocationStats(params?: {
  days?: 1 | 7 | 30;
  limit?: number;
}) {
  return axios({
    url: '/web/index/getMobileLoginLocationStats',
    method: 'get',
    params: { days: 1, limit: 8, ...params },
  });
}

/** 管理端 sys_logininfor：近 days 天按地点 Top limit（需 system:logininfor:list），days 为 1/7/30 */
export function getAdminLoginLocationStats(params?: {
  days?: 1 | 7 | 30;
  limit?: number;
}) {
  return axios({
    url: '/system/logininfor/locationStats',
    method: 'get',
    params: { days: 1, limit: 8, ...params },
  });
}

/** 工作台右侧：待审笔记/商品 + 今日注册/发帖/上新/订单 */
export interface WorkbenchSidebarSummary {
  pendingNotes: number;
  pendingProducts: number;
  todayRegistrations: number;
  todayNotes: number;
  todayNewProducts: number;
  todayOrders: number;
}

export function getWorkbenchSidebarSummary() {
  return axios({
    url: '/web/index/getWorkbenchSidebarSummary',
    method: 'get',
  });
}

/** 会员登录日志按 IP 聚合（Web/App/小程序） */
export interface IpLoginFrequencyRow {
  ipaddr: string;
  loginCount: number;
  location: string;
}

export function getIpLoginFrequencyStats(params?: {
  days?: 1 | 7 | 30;
  limit?: number;
}) {
  return axios({
    url: '/web/index/getIpLoginFrequencyStats',
    method: 'get',
    params: { days: 1, limit: 3, ...params },
  });
}

// AI聊天业务趋势，days 仅 7 或 30（与后端 sys_date 区间一致）
export function getChatBusinessTrend(days: 7 | 30 = 7) {
  return axios({
    url: '/chat/statistics/index/line',
    method: 'get',
    params: { days },
  })
}
