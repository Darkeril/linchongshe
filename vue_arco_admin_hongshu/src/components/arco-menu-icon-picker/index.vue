<template>
  <div class="arco-menu-icon-picker">
    <a-input-group>
      <a-input
        :model-value="modelValue"
        allow-clear
        placeholder="点击此处或右侧按钮选择图标；也可手填 IconHome"
        @click="openPicker"
        @update:model-value="onInput"
      >
        <template #prefix>
          <span v-if="previewComponent" class="arco-menu-icon-picker__prefix">
            <component :is="previewComponent" :size="18" />
          </span>
        </template>
      </a-input>
      <a-popover
        v-model:popup-visible="popoverVisible"
        trigger="click"
        position="bl"
        :content-style="{
          padding: '12px',
          width: '440px',
          maxHeight: '380px',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
        }"
        @popup-visible-change="onPopupVisible"
      >
        <a-button type="primary">选择图标</a-button>
        <template #content>
          <a-input
            v-model="keyword"
            allow-clear
            placeholder="搜索，如 Home、Settings、User…"
            style="margin-bottom: 10px"
          />
          <div class="arco-menu-icon-picker__grid">
            <a-tooltip v-for="name in filteredNames" :key="name" :content="name">
              <a-button
                type="text"
                class="arco-menu-icon-picker__cell"
                :class="{ 'arco-menu-icon-picker__cell--active': name === modelValue }"
                @click="select(name)"
              >
                <component :is="(ArcoIcons as Record<string, unknown>)[name]" :size="20" />
              </a-button>
            </a-tooltip>
          </div>
        </template>
      </a-popover>
    </a-input-group>
    <div class="arco-menu-icon-picker__hint">
      存入数据库的为 Arco 图标组件名（含 <code>Icon</code> 前缀），与侧栏渲染一致。
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, shallowRef, watch } from 'vue';
import { ArcoIcons, normalizeMenuIconForSave, ARCO_MENU_ICON_NAMES } from '@/utils/arco-menu-icon';

const props = defineProps<{
  modelValue?: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', v: string): void;
}>();

const keyword = ref('');
const popoverVisible = ref(false);
const previewComponent = shallowRef<unknown>(null);

function syncPreview(icon?: string) {
  const normalized = icon ? normalizeMenuIconForSave(icon) : '';
  const comp =
    normalized && (ArcoIcons as Record<string, unknown>)[normalized]
      ? (ArcoIcons as Record<string, unknown>)[normalized]
      : null;
  previewComponent.value = comp as typeof previewComponent.value;
}

watch(
  () => props.modelValue,
  (v) => syncPreview(v),
  { immediate: true },
);

const filteredNames = computed(() => {
  const k = keyword.value.trim().toLowerCase();
  if (!k) return ARCO_MENU_ICON_NAMES;
  return ARCO_MENU_ICON_NAMES.filter((n) => n.toLowerCase().includes(k));
});

function onInput(v: string) {
  emit('update:modelValue', v ?? '');
  syncPreview(v);
}

function openPicker() {
  popoverVisible.value = true;
}

function select(name: string) {
  emit('update:modelValue', name);
  syncPreview(name);
  keyword.value = '';
  popoverVisible.value = false;
}

function onPopupVisible(visible: boolean) {
  if (!visible) keyword.value = '';
}
</script>

<style scoped lang="less">
.arco-menu-icon-picker {
  width: 100%;

  :deep(.arco-input-group) {
    display: flex;
    width: 100%;
  }

  :deep(.arco-input-wrapper) {
    flex: 1;
    min-width: 0;
  }

  &__prefix {
    display: inline-flex;
    align-items: center;
    color: var(--color-text-2);
  }

  &__hint {
    margin-top: 6px;
    font-size: 12px;
    color: var(--color-text-3);
    line-height: 1.5;

    code {
      padding: 0 4px;
      font-size: 11px;
      background: var(--color-fill-2);
      border-radius: 2px;
    }
  }

  &__grid {
    flex: 1;
    overflow-y: auto;
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 4px;
    max-height: 280px;
    padding-right: 4px;
  }

  &__cell {
    width: 100%;
    height: 36px;
    padding: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: var(--border-radius-small);
    color: var(--color-text-2);

    &:hover {
      color: rgb(var(--primary-6));
      background: var(--color-fill-2);
    }

    &--active {
      color: rgb(var(--primary-6));
      background: var(--color-primary-light-1);
    }
  }
}
</style>
