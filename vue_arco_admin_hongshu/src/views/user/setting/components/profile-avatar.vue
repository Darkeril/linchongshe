<template>
  <div class="user-info-head" @click="open = true">
    <a-avatar :size="120" shape="circle">
      <img :src="displayImg" alt="avatar"/>
    </a-avatar>
    <div class="hover-mask">+</div>
  </div>

  <a-modal
      v-model:visible="open"
      title="修改头像"
      :width="800"
      :footer="false"
      :body-style="{ padding: '16px 20px 20px' }"
      unmount-on-close
      @cancel="closeDialog"
  >
    <div v-if="open" class="cropper-wrap">
      <div class="cropper-main">
        <div class="cropper-box">
          <vue-cropper
              v-if="cropperReady"
              ref="cropperRef"
              :img="options.img"
              :info="true"
              :auto-crop="options.autoCrop"
              :auto-crop-width="options.autoCropWidth"
              :auto-crop-height="options.autoCropHeight"
              :fixed-box="options.fixedBox"
              :output-type="options.outputType"
              @real-time="realTime"
          />
        </div>
        <div class="avatar-upload-preview">
          <div
              v-if="previewUrl && previewOuterStyle.width"
              class="preview-circle"
              :style="previewOuterStyle"
          >
            <div :style="previewDivStyle">
              <img :src="previewUrl" :style="previewImgStyle" alt="preview"/>
            </div>
          </div>
          <span v-else class="preview-empty">实时预览</span>
        </div>
      </div>

      <div class="cropper-toolbar">
        <a-space :size="8" wrap>
          <a-upload
              :show-file-list="false"
              :custom-request="noopRequest"
              accept="image/*"
              @before-upload="beforeUpload"
          >
            <a-button type="outline">
              <template #icon>
                <icon-upload/>
              </template>
              选择
            </a-button>
          </a-upload>
          <a-button title="放大" @click="changeScale(1)">
            <template #icon>
              <icon-plus/>
            </template>
          </a-button>
          <a-button title="缩小" @click="changeScale(-1)">
            <template #icon>
              <icon-minus/>
            </template>
          </a-button>
          <a-button title="向左旋转" @click="rotateLeft">
            <template #icon>
              <icon-rotate-left/>
            </template>
          </a-button>
          <a-button title="向右旋转" @click="rotateRight">
            <template #icon>
              <icon-rotate-right/>
            </template>
          </a-button>
        </a-space>
        <a-button type="primary" @click="uploadImg">提 交</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { computed, nextTick, reactive, ref, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { VueCropper } from 'vue-cropper';
import 'vue-cropper/dist/index.css';
import { uploadAvatar } from '@/api/system/user';
import { useUserStore } from '@/store';

interface CropperPreviewPayload {
  url?: string;
  w?: number;
  h?: number;
  div?: Record<string, string>;
  img?: Record<string, string>;
}

const props = defineProps<{
  avatarUrl: string;
}>();

const emit = defineEmits<{
  (e: 'avatarUpdated'): void;
}>();

const userStore = useUserStore();
const open = ref(false);
const cropperReady = ref(false);
const cropperRef = ref<{
  getCropBlob: (cb: (data: Blob) => void) => void;
  rotateLeft: () => void;
  rotateRight: () => void;
  changeScale: (n: number) => void;
}>();

const options = reactive({
  img: '',
  autoCrop: true,
  autoCropWidth: 200,
  autoCropHeight: 200,
  fixedBox: true,
  outputType: 'png' as 'png' | 'jpeg' | 'webp',
  filename: 'avatar.png',
  previews: {} as Record<string, unknown>,
});

const displayImg = computed(() => props.avatarUrl || userStore.user.avatar);

const previewPayload = computed(
    () => options.previews as CropperPreviewPayload,
);
const previewUrl = computed(() => previewPayload.value.url || '');
const previewOuterStyle = computed((): Record<string, string> => {
  const p = previewPayload.value;
  if (!p.w || !p.h) return {};
  return {
    width: `${p.w}px`,
    height: `${p.h}px`,
    overflow: 'hidden',
    borderRadius: '50%',
  };
});
const previewDivStyle = computed(() => previewPayload.value.div || {});
const previewImgStyle = computed(() => previewPayload.value.img || {});

function syncImgFromProp() {
  options.img = displayImg.value;
}

watch(
    () => props.avatarUrl,
    () => {
      if (!open.value) syncImgFromProp();
    },
    { immediate: true },
);

watch(
    () => userStore.user,
    () => {
      if (!open.value) syncImgFromProp();
    },
    { deep: true },
);

watch(open, async (v) => {
  if (v) {
    syncImgFromProp();
    cropperReady.value = false;
    await nextTick();
    cropperReady.value = true;
  } else {
    cropperReady.value = false;
  }
});

function noopRequest() {}

function beforeUpload(file: File) {
  if (!file.type.startsWith('image/')) {
    Message.error('文件格式错误，请上传 JPG、PNG 等图片文件。');
    return false;
  }
  const reader = new FileReader();
  reader.readAsDataURL(file);
  reader.onload = () => {
    options.img = reader.result as string;
    options.filename = file.name;
  };
  return false;
}

function rotateLeft() {
  cropperRef.value?.rotateLeft();
}

function rotateRight() {
  cropperRef.value?.rotateRight();
}

function changeScale(num: number) {
  cropperRef.value?.changeScale(num || 1);
}

function realTime(data: Record<string, unknown>) {
  options.previews = data;
}

function uploadImg() {
  cropperRef.value?.getCropBlob((data: Blob) => {
    const formData = new FormData();
    formData.append('avatarfile', data, options.filename);
    uploadAvatar(formData)
        .then((res: { imgUrl?: string }) => {
          const imgUrl = res?.imgUrl;
          if (imgUrl) {
            userStore.setInfo({
              user: {
                ...userStore.user,
                avatar: imgUrl,
              },
            });
          }
          Message.success('修改成功');
          open.value = false;
          emit('avatarUpdated');
        })
        .catch(() => {});
  });
}

function closeDialog() {
  syncImgFromProp();
  cropperReady.value = false;
  options.previews = {};
}
</script>

<style scoped lang="less">
.user-info-head {
  position: relative;
  display: inline-block;
  cursor: pointer;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .hover-mask {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #eee;
    font-size: 28px;
    background: rgba(0, 0, 0, 0.45);
    border-radius: 50%;
    opacity: 0;
    transition: opacity 0.2s;
    pointer-events: none;
  }

  &:hover .hover-mask {
    opacity: 1;
  }
}

.cropper-wrap {
  min-height: 0;
}

.cropper-main {
  display: flex;
  gap: 16px;
  align-items: stretch;
}

@media (max-width: 768px) {
  .cropper-main {
    flex-direction: column;
  }
}

.cropper-box {
  flex: 1;
  min-width: 0;
  height: 350px;
  background: var(--color-fill-2);
  border-radius: 4px;
  overflow: hidden;
}

.avatar-upload-preview {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  min-width: 0;
  height: 350px;
  padding: 12px;
  background: var(--color-fill-2);
  border-radius: 4px;
  box-sizing: border-box;
}

.preview-circle {
  position: relative;
  flex-shrink: 0;
  box-shadow: 0 0 0 1px var(--color-border-2);
  background: repeating-conic-gradient(
      var(--color-fill-3) 0% 25%,
      var(--color-bg-2) 0% 50%
  )
  50% / 16px 16px;

  img {
    display: block;
    max-width: none;
    vertical-align: top;
  }
}

.preview-empty {
  color: var(--color-text-3);
  font-size: 13px;
}

.cropper-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 4px;
  border-top: 1px solid var(--color-border-2);
}
</style>
