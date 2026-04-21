import loading from "@/assets/loading.png";
import error from "@/assets/error.png";
import { reactive } from "vue";
// websocket地址
// export const wsKey = "ws://www.hongshu.website/ws/";
// // 项目url地址
// export const baseURL = "http://www.hongshu.website/";

// 本地项目websocket地址
export const wsKey = "ws://localhost:8080/web/ws/";
// 本地项目url地址
export const baseURL = "http://localhost:8080/";

// 图片必须保持在336和186之间

export const options = reactive({
  // 唯一key值
  rowKey: "id",
  // 卡片之间的间隙
  gutter: 16,
  // 是否有周围的gutter
  hasAroundGutter: true,
  // 卡片在PC上的宽度
  width: 240,
  // 自定义行显示个数，主要用于对移动端的适配
  breakpoints: {
    2000: {
      // 当屏幕宽度小于等于2000
      rowPerView: 5,
    },
    1200: {
      // 当屏幕宽度小于等于1200
      rowPerView: 4,
    },
    1000: {
      // 当屏幕宽度小于等于1000
      rowPerView: 3,
    },
    695: {
      // 当屏幕宽度小于等于695（移动端）
      rowPerView: 2,
    },
  },
  // 动画效果（瀑布流卡片入场；过长会导致切换分类/首屏「像卡住」）
  animationEffect: "animate__zoomIn",
  animationDuration: 450,
  animationDelay: 60,
  // 背景色
  backgroundColor: "#2C2E3A",
  // imgSelector
  imgSelector: "src.original",
  // 加载配置
  loadProps: {
    loading,
    error,
  },
  // 是否懒加载
  lazyload: true,
});
