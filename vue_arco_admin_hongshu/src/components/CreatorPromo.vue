<template>
  <div class="creator-promo-container">
    <div class="video-box-container">
      <div class="mask"></div>
      <div class="title-box">
        <span class="title-line">
          加入我们<br />
          解锁创作者专属功能
        </span>
        <span class="des">让创作发布、数据分析、商业变现更高效。</span>
      </div>

      <template v-for="(item, index) in videoItems" :key="index">
        <div
          v-if="item.enabled !== false"
          class="video-item-container"
          :style="getItemStyle(item)"
        >
          <div class="circle" :style="getCircleStyle(item)">
            <video
              v-if="item.video && !item.loadError"
              :key="`${index}-${item.video}`"
              playsinline
              disableremoteplayback
              class="video"
              autoplay
              loop
              muted
              preload="auto"
              @error="handleVideoError($event, index)"
              @loadstart="handleVideoLoadStart($event, index)"
            >
              <source :src="item.video" type="video/mp4" />
            </video>
            <div
              v-else-if="item.loadError"
              class="video-placeholder"
              :style="{ background: item.background || 'transparent' }"
            >
              <span class="placeholder-text">视频加载失败</span>
            </div>
            <img
              v-else-if="item.image"
              :key="`${index}-${item.image}`"
              :src="item.image"
              alt=""
              class="video"
              @error="handleImageError($event, index)"
            />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getAdminLoginPromoConfig } from '@/api/system/config';
import {
  ADMIN_LOGIN_PROMO_VIDEO_FILES,
  ADMIN_LOGIN_PROMO_LAYOUT,
  getBuiltinVideoUrl,
} from '@/constants/admin-login-promo-defaults';

interface VideoItem {
  enabled?: boolean;
  width: number;
  height: number;
  marginTop: number;
  marginLeft: number;
  background?: string;
  transform?: string;
  video?: string;
  image?: string;
  loadError?: boolean;
}

function buildDefaultVideoItems(): VideoItem[] {
  return ADMIN_LOGIN_PROMO_LAYOUT.map((layout, i) => ({
    enabled: true,
    ...layout,
    video: getBuiltinVideoUrl(ADMIN_LOGIN_PROMO_VIDEO_FILES[i]),
    loadError: false,
  }));
}

function toPositiveNumber(value: unknown, fallback: number): number {
  if (typeof value === 'number' && !Number.isNaN(value)) {
    return value;
  }
  if (typeof value === 'string' && value.trim() !== '') {
    const n = Number(value);
    if (!Number.isNaN(n)) {
      return n;
    }
  }
  return fallback;
}

function mergeRemote(remote: Record<string, unknown>[]): VideoItem[] {
  const defaults = buildDefaultVideoItems();
  if (!remote || !remote.length) {
    return defaults;
  }
  return defaults.map((def, i) => {
    const r = remote[i] as Record<string, unknown> | undefined;
    if (!r) {
      return def;
    }
    const vUrl = String(r.videoUrl ?? '').trim();
    const imgUrl = String(r.imageUrl ?? '').trim();
    const enabled = r.enabled !== false;
    let video: string | undefined;
    if (vUrl) {
      video = vUrl;
    } else if (imgUrl) {
      video = undefined;
    } else {
      video = def.video;
    }
    return {
      width: def.width,
      height: def.height,
      marginTop: def.marginTop,
      marginLeft: def.marginLeft,
      background: def.background,
      transform: def.transform,
      enabled,
      video,
      image: imgUrl || undefined,
      loadError: false,
    };
  });
}

const videoItems = ref<VideoItem[]>(buildDefaultVideoItems());

function isHttpOk(code: unknown): boolean {
  return code === 200 || code === '200' || Number(code) === 200;
}

/** 兼容拦截器返回 { code, data } 或直接返回数组等情况 */
function extractPromoList(res: unknown): Record<string, unknown>[] | null {
  if (!res) return null;
  if (Array.isArray(res)) {
    return res as Record<string, unknown>[];
  }
  const body = res as { code?: unknown; data?: unknown };
  if (!isHttpOk(body.code)) {
    return null;
  }
  const d = body.data;
  if (Array.isArray(d)) {
    return d as Record<string, unknown>[];
  }
  return null;
}

onMounted(async () => {
  try {
    const res = await getAdminLoginPromoConfig();
    const rows = extractPromoList(res);
    if (rows && rows.length > 0) {
      videoItems.value = mergeRemote(rows);
    } else if (res && typeof res === 'object' && !isHttpOk((res as any).code)) {
      console.warn(
        '登录页展示区配置接口非成功状态',
        (res as any).code,
        (res as any).message || (res as any).msg
      );
    }
  } catch (e) {
    console.warn(
      '加载登录页展示区配置失败，使用内置默认（请检查网关是否放行 /system/config/adminLoginPromo）',
      e
    );
  }
});

const getItemStyle = (item: VideoItem) => {
  return {
    width: `${item.width}px`,
    height: `${item.height}px`,
    marginTop: `${item.marginTop}px`,
    marginLeft: `${item.marginLeft}px`,
    borderRadius: `${item.width}px`,
  };
};

const getCircleStyle = (item: VideoItem) => {
  return {
    background: item.background || 'transparent',
    transform: item.transform || 'none',
  };
};

const handleVideoError = (event: Event, index: number) => {
  console.error(`视频 ${index} 加载失败:`, event);
  if (videoItems.value[index]) {
    videoItems.value[index].loadError = true;
  }
};

const handleVideoLoadStart = (event: Event, index: number) => {
  console.log(`视频 ${index} 开始加载`, event);
};

const handleImageError = (event: Event, index: number) => {
  console.error(`图片 ${index} 加载失败:`, event);
};
</script>

<style scoped lang="less">
.creator-promo-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: transparent;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 40px 20px;
  box-sizing: border-box;
}

.video-box-container {
  position: relative;
  width: 820px;
  height: 520px;
  margin: 0 auto;
}

.mask {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.title-box {
  position: relative;
  z-index: 10;
  margin-top: -50px;

  .title-line {
    display: block;
    font-size: 46px;
    font-weight: 700;
    line-height: 1.3;
    color: #ffffff;
    margin-bottom: 18px;
    letter-spacing: -0.5px;
  }

  .des {
    display: block;
    font-size: 17px;
    color: rgba(255, 255, 255, 0.9);
    line-height: 1.6;
    font-weight: 400;
  }
}

.video-item-container {
  position: absolute;
  overflow: hidden;
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.3s ease;

  &:hover {
    transform: scale(1.05);
  }

  .circle {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    overflow: hidden;
    position: relative;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

    .video {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    .video-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;

      .placeholder-text {
        color: rgba(255, 255, 255, 0.6);
        font-size: 12px;
        text-align: center;
      }
    }
  }
}
</style>
