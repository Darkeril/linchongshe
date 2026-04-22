<!--
  CommentInputSheet — 底部半屏发评论面板
  TODO(design): 输入面板暂无设计稿，按原则 1 V1 渲染（latte 主题，480rpx 高）

  gotcha 提醒：
    - #5 fixed 祖先不能 transform（父 note-detail 页面根已确认）
    - #12 acs- 前缀
    - #21 emit 名：sheet-close / sheet-submit（避 DOM 原生）
    - 键盘适配：uni.onKeyboardHeightChange（条件编译）
-->
<template>
  <!-- 遮罩 -->
  <view
    v-if="open"
    class="acs-sheet-mask"
    @tap="onMaskTap"
  >
    <!-- Sheet 主体（点击内部不冒泡到遮罩） -->
    <view
      class="acs-sheet"
      :style="{ paddingBottom: keyboardPad + 'px' }"
      @tap.stop
    >
      <!-- 拖拽条 -->
      <view class="acs-sheet__handle" />

      <!-- 输入框 -->
      <textarea
        v-model="text"
        class="acs-sheet__input"
        :placeholder="placeholderText"
        placeholder-class="acs-sheet__input-ph"
        :maxlength="200"
        :focus="open"
        :auto-height="false"
        :show-confirm-bar="false"
      />

      <!-- 底部栏 -->
      <view class="acs-sheet__bottom">
        <text class="acs-sheet__counter">{{ text.length }} / 200</text>
        <view
          class="acs-sheet__submit"
          :class="{ 'acs-sheet__submit--disabled': !canSubmit }"
          @tap="onSubmit"
        >
          <text class="acs-sheet__submit-text">发送</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted } from 'vue';
import type { CommentUser } from '@/types/comment';

interface Props {
  open: boolean;
  replyTarget?: CommentUser | null;
}

const props = withDefaults(defineProps<Props>(), {
  replyTarget: null,
});

// gotcha #21：sheet-close / sheet-submit（close/submit 不在 DOM 黑名单）
const emit = defineEmits<{
  (e: 'sheet-close'): void;
  (e: 'sheet-submit', text: string): void;
}>();

const text = ref<string>('');
const keyboardPad = ref<number>(0);

const placeholderText = computed<string>(() => {
  return props.replyTarget ? `回复 @${props.replyTarget.name}` : '说点什么吧...';
});

const canSubmit = computed<boolean>(() => text.value.trim().length > 0);

// open 切换时清空 text
watch(
  () => props.open,
  (v) => {
    if (!v) {
      text.value = '';
    }
  },
);

// 键盘高度监听（小程序 / App 有原生 api；H5 需要 visualViewport 兜底）
let kbListenerBound = false;
function onKbChange(res: { height: number }): void {
  keyboardPad.value = res.height ?? 0;
}

onMounted(() => {
  // #ifdef MP-WEIXIN || MP-TOUTIAO || APP-PLUS
  uni.onKeyboardHeightChange(onKbChange);
  kbListenerBound = true;
  // #endif
});

onUnmounted(() => {
  // #ifdef MP-WEIXIN || MP-TOUTIAO || APP-PLUS
  if (kbListenerBound) {
    uni.offKeyboardHeightChange(onKbChange);
  }
  // #endif
});

function onMaskTap(): void {
  emit('sheet-close');
}

function onSubmit(): void {
  const trimmed = text.value.trim();
  if (!trimmed) return;
  emit('sheet-submit', trimmed);
  text.value = '';
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

// gotcha #5：本组件 fixed，要求父页面根节点无 transform（note-detail 已确认）
.acs-sheet-mask {
  position: fixed;
  inset: 0;
  left: 0; right: 0; top: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 900;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.acs-sheet {
  width: 100%;
  background: $color-surface;
  border-top-left-radius: $radius-xl; // 28
  border-top-right-radius: $radius-xl;
  box-shadow: 0 -10rpx 40rpx rgba(80, 50, 30, 0.16);
  padding: 16rpx 32rpx 24rpx;
  box-sizing: border-box;
  // 滑入动画
  animation: acs-sheet-slide-up 260ms ease-out;
}

@keyframes acs-sheet-slide-up {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.acs-sheet__handle {
  width: 72rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: $color-ink-faint;
  margin: 0 auto 20rpx;
}

.acs-sheet__input {
  width: 100%;
  height: 240rpx;
  box-sizing: border-box;
  padding: 20rpx 24rpx;
  background: $color-bg;
  border-radius: $radius-md; // 16
  font-size: $font-size-md; // 13
  color: $color-ink;
  line-height: 1.5;
}

.acs-sheet__input-ph {
  color: $color-ink-muted;
  font-size: $font-size-md;
}

.acs-sheet__bottom {
  margin-top: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.acs-sheet__counter {
  font-size: $font-size-sm; // 11
  color: $color-ink-muted;
  font-family: $font-family-num;
  line-height: 1;
}

.acs-sheet__submit {
  min-width: 140rpx;
  height: 64rpx;
  padding: 0 32rpx;
  border-radius: $radius-pill;
  background: $color-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active { opacity: 0.85; }

  &--disabled {
    background: $color-ink-faint;
  }
}

.acs-sheet__submit-text {
  font-size: $font-size-base; // 12
  color: #ffffff;
  font-weight: $font-weight-semibold;
  line-height: 1;
}
</style>
