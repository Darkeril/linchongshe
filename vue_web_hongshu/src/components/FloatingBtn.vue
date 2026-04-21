<template>
  <div class="floating-btn-sets">
    <el-backtop :bottom="backTopBottom" :right="backTopRight">
      <div class="back-top">
        <Top style="width: 1.2em; height: 1.2em" color="rgba(51, 51, 51, 0.8)" />
      </div>
    </el-backtop>

    <!-- AI助手按钮 - 可拖拽 -->
    <div
      ref="aiBtn"
      class="ai-btn draggable"
      :class="{ dragging: isDragging }"
      :style="{
        left: position.x + 'px',
        top: position.y + 'px',
        transition: isDragging ? 'none' : isInitialized ? 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)' : 'none',
      }"
      @mousedown="handleMouseDown"
      @click="handleClick"
      :title="t('aiChat.floatingTitle')"
    >
      <div class="ai-icon">
        <svg
          t="1768137801162"
          class="icon"
          viewBox="0 0 1218 1024"
          version="1.1"
          xmlns="http://www.w3.org/2000/svg"
          p-id="12992"
          width="200"
          height="200"
        >
          <path
            d="M326.183722 206.500257c19.568414 0 36.788619 9.523295 47.529504 24.177863a41.528523 41.528523 0 0 1 17.394146 18.698707l1.348046 3.391858 249.910392 711.072687c8.088278 22.916787-6.348863 48.529667-32.17917 57.22674-24.525746 8.262219-50.529994-1.652444-60.009803-22.351478l-1.304561-3.348373-64.793194-184.247491H160.460996l-64.706223 184.247491c-8.088278 22.916787-35.527543 34.440409-61.314364 25.699851-24.569231-8.262219-38.788945-31.787802-33.222819-53.791396l1.000164-3.47883L252.08466 252.81217a43.267938 43.267938 0 0 1 30.439756-26.91744A58.574787 58.574787 0 0 1 326.140237 206.500257h0.043485z m459.553336 201.511181c25.960763 0 47.225106 12.958639 49.312404 29.396106l0.130456 2.609122v549.046217c0 17.698544-22.177536 32.005229-49.44286 32.005229-25.960763 0-47.225106-12.915153-49.268918-29.396107l-0.217427-2.609122V440.060152c0-17.698544 22.177536-32.005229 49.486345-32.005229zM322.226554 350.741212l-127.760002 363.494165h255.563489L322.226554 350.697727zM1038.430514 105.52724a14.958966 14.958966 0 0 1 9.697236 9.56678l38.658489 117.801853 120.889315 42.87657a14.958966 14.958966 0 0 1-0.869708 28.482914l-118.410648 33.266304-36.658163 116.018954a14.958966 14.958966 0 0 1-28.439428 0.130456l-38.701975-117.758368-117.323515-37.223473a14.958966 14.958966 0 0 1-0.217426-28.395943l115.975468-39.049858 36.658162-115.975468a14.958966 14.958966 0 0 1 18.742193-9.740721zM772.387051 0.553569a10.436488 10.436488 0 0 1 6.653261 6.827202l22.04708 70.489776 72.490103 24.569232a10.436488 10.436488 0 0 1-0.391368 19.959782l-72.185706 21.48177-23.569067 70.794174a10.436488 10.436488 0 0 1-19.872812-0.173941l-22.04708-70.489777-70.228865-21.220858a10.436488 10.436488 0 0 1-0.434853-19.872811l70.794174-24.873629 23.525582-70.881145a10.436488 10.436488 0 0 1 13.219551-6.609775z"
            p-id="12993"
            data-spm-anchor-id="a313x.search_index.0.i16.50643a81WDYy3v"
            class="selected"
            fill="#ffffff"
          ></path>
        </svg>
        <!-- <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="currentColor" opacity="0.9" />
          <path
            d="M2 17L12 22L22 17"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            opacity="0.8"
          />
          <path
            d="M2 12L12 17L22 12"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            opacity="0.8"
          />
        </svg> -->
      </div>
      <!-- <span class="ai-text">AI</span> -->
    </div>

    <div class="reload" @click="refresh">
      <Refresh style="width: 1.2em; height: 1.2em" color="rgba(51, 51, 51, 0.8)" />
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref, reactive, onMounted, onUnmounted, computed } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
import { Refresh, Top } from "@element-plus/icons-vue";
import { getWebsiteConfig } from "@/api/config";
import { useUserStore } from "@/store/userStore";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";

const emit = defineEmits(["clickRefresh"]);

const router = useRouter();

// 检测是否为移动端
const isMobile = computed(() => {
  return window.innerWidth <= 695;
});

// 动态计算bottom值
const backTopBottom = computed(() => {
  return isMobile.value ? 120 : 80;
});

// 动态计算right值
const backTopRight = computed(() => {
  return isMobile.value ? 16 : 24;
});

const aiBtn = ref<HTMLElement | null>(null);
const isDragging = ref(false);
const isClick = ref(true);
const aiUrl = ref<string>("");
const isInitialized = ref(false); // 标记是否已初始化位置，用于控制过渡动画

// 按钮位置
const position = reactive({
  x: 0,
  y: 0,
});

// 拖拽相关变量
const dragStart = reactive({
  x: 0,
  y: 0,
  elementX: 0,
  elementY: 0,
});

// 设置默认位置
const setDefaultPosition = () => {
  // 默认位置：与客服按钮相同的位置
  const rightOffset = isMobile.value ? 20 : 20;
  const bottomOffset = isMobile.value ? 150 : 150; // 与客服按钮相同的bottom值
  position.x = window.innerWidth - 56 - rightOffset; // 56px是按钮宽度，rightOffset是右边距
  position.y = window.innerHeight - 56 - bottomOffset; // 与客服按钮对齐
};

// 窗口大小变化处理
const handleResize = () => {
  // 如果按钮位置超出窗口范围，调整到窗口内
  const btnWidth = 56;
  const btnHeight = 56;
  const maxX = window.innerWidth - btnWidth;
  const maxY = window.innerHeight - btnHeight;

  if (position.x > maxX) {
    position.x = maxX;
  }
  if (position.y > maxY) {
    position.y = maxY;
  }

  // 🔧 修改：不再保存位置到localStorage
  // 因为切换页面时需要恢复到初始位置
};

// 初始化位置和获取配置
onMounted(async () => {
  // 🔧 修复：先禁用过渡动画，避免切换栏目时出现"飞"的效果
  isInitialized.value = false;

  // 🔧 修改：每次切换页面时，都恢复到初始位置（右侧）
  // 不再从localStorage读取位置，确保每次进入页面都是默认位置
  setDefaultPosition();

  // 🔧 修复：延迟启用过渡动画，确保位置已经设置完成
  // 使用 setTimeout 确保 DOM 已更新后再启用动画
  setTimeout(() => {
    isInitialized.value = true;
  }, 100);

  // 监听窗口大小变化，确保按钮位置在窗口内
  window.addEventListener("resize", handleResize);

  // 从后端获取AI路径配置
  try {
    const res: any = await getWebsiteConfig();
    if (res?.code === 200 && res.data?.aiUrl) {
      aiUrl.value = res.data.aiUrl;
    }
  } catch (error) {
    console.error("获取AI路径配置失败:", error);
  }
});

// 鼠标按下
const handleMouseDown = (e: MouseEvent) => {
  e.preventDefault();
  isClick.value = true;
  isDragging.value = true;

  // 记录鼠标起始位置和元素位置
  dragStart.x = e.clientX;
  dragStart.y = e.clientY;
  dragStart.elementX = position.x;
  dragStart.elementY = position.y;

  // 添加全局事件监听
  document.addEventListener("mousemove", handleMouseMove);
  document.addEventListener("mouseup", handleMouseUp);
};

// 鼠标移动
const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging.value) return;

  // 如果移动距离超过5px，则认为是拖拽而不是点击
  const moveX = Math.abs(e.clientX - dragStart.x);
  const moveY = Math.abs(e.clientY - dragStart.y);
  if (moveX > 5 || moveY > 5) {
    isClick.value = false;
  }

  // 计算新位置
  const deltaX = e.clientX - dragStart.x;
  const deltaY = e.clientY - dragStart.y;

  let newX = dragStart.elementX + deltaX;
  let newY = dragStart.elementY + deltaY;

  // 边界限制
  const btnWidth = 40;
  const btnHeight = 40;
  const maxX = window.innerWidth - btnWidth;
  const maxY = window.innerHeight - btnHeight;

  newX = Math.max(0, Math.min(newX, maxX));
  newY = Math.max(0, Math.min(newY, maxY));

  position.x = newX;
  position.y = newY;
};

// 鼠标松开
const handleMouseUp = () => {
  isDragging.value = false;

  // 移除全局事件监听
  document.removeEventListener("mousemove", handleMouseMove);
  document.removeEventListener("mouseup", handleMouseUp);

  // 🔧 修改：不再保存位置到localStorage
  // 因为切换页面时需要恢复到初始位置，所以不需要持久化保存
  // 用户可以在当前页面内拖动按钮，但切换页面后会重置为默认位置
};

// 点击事件
const handleClick = () => {
  // 只有在没有拖拽的情况下才触发点击
  if (isClick.value) {
    goToAI();
  }
};

// 刷新
const refresh = () => {
  emit("clickRefresh", true);
};

// AI助手跳转
const goToAI = () => {
  // 检查是否已登录
  const userStore = useUserStore();
  const isLogin = userStore.isLogin();

  if (isLogin) {
    // 已登录，跳转到AI聊天页面
    router.push("/ai-chat");
  } else {
    // 未登录，提示用户先登录
    ElMessage.warning(t("aiChat.pleaseLoginFirst"));
    router.push("/login");
  }
};

// 组件卸载时清理事件监听
onUnmounted(() => {
  document.removeEventListener("mousemove", handleMouseMove);
  document.removeEventListener("mouseup", handleMouseUp);
  window.removeEventListener("resize", handleResize);
});
</script>

<style lang="less" scoped>
// 主题变量
@primary-color: #3d8af5;
@text-primary: rgba(51, 51, 51, 0.8);
@text-secondary: rgba(51, 51, 51, 0.6);
@border-color: rgba(0, 0, 0, 0.08);
@white: #fff;

.floating-btn-sets {
  position: fixed;
  display: flex;
  flex-direction: column;
  width: 40px;
  grid-gap: 8px;
  gap: 8px;
  right: 24px;
  bottom: 24px;

  // 移动端优化 - 避免被底部导航栏遮挡
  @media screen and (max-width: 695px) {
    bottom: 140px;
    right: 16px;
  }

  .back-top {
    width: 40px;
    height: 40px;
    background: #fff;
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 100px;
    color: rgba(51, 51, 51, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s;
    cursor: pointer;
  }

  .ai-btn {
    position: fixed;
    width: 60px;
    height: 60px;
    background: @primary-color;
    border: none;
    box-shadow: 0 4px 15px 0 rgba(65, 147, 255, 0.4), 0 2px 8px 0 rgba(0, 0, 0, 0.15);
    border-radius: 50%;
    color: #fff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2px;
    cursor: move;
    z-index: 9999;
    overflow: visible;
    user-select: none;
    will-change: transform, box-shadow;

    // 移动端隐藏AI按钮
    @media screen and (max-width: 695px) {
      display: none;
    }

    &::before {
      content: "";
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 50%;
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0) 100%);
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:hover:not(.dragging) {
      transform: translateY(-2px) scale(1.05);
      box-shadow: 0 8px 25px 0 rgba(65, 211, 255, 0.5), 0 4px 12px 0 rgba(0, 0, 0, 0.2);

      &::before {
        opacity: 1;
      }

      .ai-icon {
        transform: scale(1.1);
      }

      .ai-text {
        transform: scale(1.05);
      }
    }

    &.dragging {
      cursor: grabbing;
      transform: scale(1.08);
      box-shadow: 0 12px 30px 0 rgba(65, 195, 255, 0.6), 0 6px 16px 0 rgba(0, 0, 0, 0.25);
      opacity: 0.95;
      transition: transform 0.1s, box-shadow 0.1s, opacity 0.1s !important;
    }

    &:active:not(.dragging) {
      transform: scale(0.98);
    }

    .ai-icon {
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      z-index: 1;
      transition: transform 0.3s;

      svg {
        width: 100%;
        height: 100%;
        filter: drop-shadow(0 1px 3px rgba(0, 0, 0, 0.2));
      }
    }

    .ai-text {
      font-size: 12px;
      font-weight: 700;
      letter-spacing: 0.5px;
      position: relative;
      z-index: 1;
      text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
      transition: transform 0.3s;
    }
  }

  .reload {
    position: fixed;
    width: 40px;
    height: 40px;
    background: #fff;
    border: 1px solid rgba(0, 0, 0, 0.08);
    box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.02);
    border-radius: 100px;
    color: rgba(51, 51, 51, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s;
    cursor: pointer;
    right: 24px;
    bottom: 24px;

    // 移动端优化
    @media screen and (max-width: 695px) {
      right: 16px;
      bottom: 72px;
    }

    &:hover {
      background: #f5f5f5;
    }

    &:active {
      transform: scale(0.95);
    }
  }
}
</style>
