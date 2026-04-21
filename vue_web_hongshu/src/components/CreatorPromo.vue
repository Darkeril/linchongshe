<template>
  <div class="creator-promo-container">
    <div class="video-box-container">
      <div class="mask"></div>
      <div class="title-box">
        <span class="title-line">
          {{ $t("creator.joinUs") }}<br />
          {{ $t("creator.unlockFeatures") }}
        </span>
        <span class="des">{{ $t("creator.description") }}</span>
      </div>

      <!-- 圆形视频容器 -->
      <div v-for="(item, index) in videoItems" :key="index" class="video-item-container" :style="getItemStyle(item)">
        <div class="circle" :style="getCircleStyle(item)">
          <video v-if="item.video" playsinline disableremoteplayback class="video" autoplay loop muted>
            <source :src="item.video" type="video/mp4" />
          </video>
          <img v-else-if="item.image" :src="item.image" alt="" class="video" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";

interface VideoItem {
  width: number;
  height: number;
  marginTop: number;
  marginLeft: number;
  background?: string;
  transform?: string;
  video?: string;
  image?: string;
}

const videoItems = ref<VideoItem[]>([
  {
    width: 213,
    height: 213,
    marginTop: 92,
    marginLeft: 0,
    background: "rgb(246, 214, 231)",
    transform: "translate(14px, -8px)",
    video:
      "https://fe-video-qc.xhscdn.com/picasso-editor/f1dbcdb3e3dae24e5b04a39512c7eb6c5ef202c2.mp4_video_original_mp4",
  },
  {
    width: 156,
    height: 156,
    marginTop: 297,
    marginLeft: 101,
    background: "rgb(246, 243, 214)",
    transform: "translate(7px, -9px)",
    video:
      "https://fe-video-qc.xhscdn.com/picasso-editor/f714d38ae9cba0b253cb1d22c1c3816d1c0e58e5.mp4_video_original_mp4",
  },
  {
    width: 265,
    height: 265,
    marginTop: 103,
    marginLeft: 216,
    background: "rgb(215, 237, 250)",
    transform: "translate(10px, -16px)",
    video:
      "https://fe-video-qc.xhscdn.com/picasso-editor/c2eb6f3a166720cb47d75d82ce9baf252d4e966b.mp4_video_original_mp4",
  },
  {
    width: 151,
    height: 151,
    marginTop: -12,
    marginLeft: 399,
    background: "rgb(246, 214, 214)",
    transform: "translate(2px, -7px)",
    video:
      "https://fe-video-qc.xhscdn.com/picasso-editor/1bc37eeafdc71585d23f2550f7eba8cf90497edf.mp4_video_original_mp4",
  },
  {
    width: 144,
    height: 144,
    marginTop: 88,
    marginLeft: 518,
    background: "rgb(201, 217, 215)",
    transform: "translate(-5px, -9px)",
    video:
      "https://fe-video-qc.xhscdn.com/picasso-editor/e127c2e0619048e7bfa67ce15162a54ff1d5bcd4.mp4_video_original_mp4",
  },
  {
    width: 93,
    height: 93,
    marginTop: 187,
    marginLeft: 643,
    background: "rgb(246, 225, 214)",
    transform: "translate(-4px, -5px)",
    video:
      "https://fe-video-qc.xhscdn.com/picasso-editor/61f51a6c21075bcfa52d4751404d72b37f4fc445.mp4_video_original_mp4",
  },
  {
    width: 222,
    height: 222,
    marginTop: 233,
    marginLeft: 459,
    background: "rgb(204, 224, 240)",
    transform: "translate(-4px, -16px)",
    video:
      "https://fe-video-qc.xhscdn.com/picasso-editor/a00472903313b9a87fedd3a69a854e37c6037a8a.mp4_video_original_mp4",
  },
]);

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
    background: item.background || "transparent",
    transform: item.transform || "none",
  };
};
</script>

<style scoped lang="less">
.creator-promo-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #fafbfc 50%, #fef0f5 100%);
  min-height: 441px;
}

.video-box-container {
  position: relative;
  width: 735px;
  height: 441px;
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
  margin-top: -60px;
  padding-left: 20px;

  .title-line {
    display: block;
    font-size: 48px;
    font-weight: 700;
    line-height: 1.3;
    color: #1a1a1a;
    margin-bottom: 16px;
    letter-spacing: -0.5px;
  }

  .des {
    display: block;
    font-size: 16px;
    color: #666;
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
  }
}
</style>

