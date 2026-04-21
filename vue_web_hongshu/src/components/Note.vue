<template>
  <div class="feeds-container" v-infinite-scroll="loadMoreData" :infinite-scroll-distance="50">
    <Waterfall
      :list="noteList"
      :width="options.width"
      :gutter="options.gutter"
      :hasAroundGutter="hasAroundGutter"
      :animation-effect="options.animationEffect"
      :animation-duration="options.animationDuration"
      :animation-delay="options.animationDelay"
      :breakpoints="options.breakpoints"
    >
      <template #item="{ item }">
        <el-skeleton style="width: 240px" :loading="!item.isLoading" animated>
          <template #template>
            <el-image
              :src="item.noteCover"
              :style="{
                width: '240px',
                maxHeight: '300px',
                height: item.noteCoverHeight + 'px',
                borderRadius: '8px',
              }"
              @load="handleLoad(item)"
            >
            </el-image>

            <div style="padding: 14px">
              <el-skeleton-item variant="h3" style="width: 100%" />
              <div style="display: flex; align-items: center; margin-top: 2px; height: 16px">
                <el-skeleton style="--el-skeleton-circle-size: 20px">
                  <template #template>
                    <el-skeleton-item variant="circle" />
                  </template>
                </el-skeleton>
                <el-skeleton-item variant="text" style="margin-left: 10px" />
              </div>
            </div>
          </template>

          <template #default>
            <div class="card">
              <div class="image-container">
                <el-image :src="item.noteCover" fit="cover" class="note-cover-img" @click="toMain(item.id)"> </el-image>
                <div v-if="item.auditStatus === '0'" class="overlay passing">
                  <div class="audit-eye">
                    <svg t="1710000000000" class="icon" viewBox="0 0 1024 1024" width="48" height="48">
                      <path
                        d="M512 256c-256 0-416 256-416 256s160 256 416 256 416-256 416-256-160-256-416-256z m0 448c-106.039 0-192-85.961-192-192s85.961-192 192-192 192 85.961 192 192-85.961 192-192 192z m0-320c-70.692 0-128 57.308-128 128s57.308 128 128 128 128-57.308 128-128-57.308-128-128-128z"
                        fill="#fff"
                      />
                    </svg>
                    <div>{{ $t("note.pendingReview") }}...</div>
                  </div>
                </div>
                <div v-if="item.auditStatus === '2'" class="overlay not-passed">
                  {{ $t("note.rejected") }}⚠️
                  <el-button
                    v-if="item.auditStatus === '2' && item.uid === currentUid"
                    type="primary"
                    size="small"
                    class="edit-btn-on-img"
                    @click.stop="editNote(item.id)"
                    style="margin-left: 8px"
                  >
                    {{ $t("note.reEdit") }}
                  </el-button>
                </div>
              </div>
              <div class="footer">
                <a class="title">
                  <span>{{ item.title }}</span>
                </a>
                <div class="author-wrapper">
                  <a class="author">
                    <img class="author-avatar" :src="item.avatar" />
                    <span class="name">{{ item.username }}</span>
                  </a>
                  <span class="like-wrapper like-active">
                    <span
                      class="like-lottie"
                      :class="{ 'like-disabled': item.auditStatus === '0' || item.auditStatus === '2' }"
                      v-if="item.isLike"
                      @click="handleLike(item)"
                    >
                      <i class="iconfont icon-follow-fill" style="width: 1em; height: 1em; color: #ff2442"></i>
                    </span>
                    <span
                      class="like-lottie"
                      :class="{ 'like-disabled': item.auditStatus === '0' || item.auditStatus === '2' }"
                      v-else
                      @click="handleLike(item)"
                    >
                      <i class="iconfont icon-follow" style="width: 1em; height: 1em; color: #333"></i>
                    </span>
                    <span class="count">{{ item.likeCount }}</span>
                  </span>
                </div>
              </div>
              <!-- 视频标识 -->
              <div class="top-tag-sign" v-if="item.noteType === '2'">
                <svg
                  t="1734053667366"
                  class="icon"
                  viewBox="0 0 1024 1024"
                  version="1.1"
                  xmlns="http://www.w3.org/2000/svg"
                  p-id="5045"
                  width="25"
                  height="25"
                >
                  <path
                    d="M512 0C230.4 0 0 230.4 0 512s230.4 512 512 512 512-230.4 512-512S793.6 0 512 0z m0 981.333333C253.866667 981.333333 42.666667 770.133333 42.666667 512S253.866667 42.666667 512 42.666667s469.333333 211.2 469.333333 469.333333-211.2 469.333333-469.333333 469.333333z"
                    fill="#ffffff"
                    p-id="5046"
                  ></path>
                  <path
                    d="M672 441.6l-170.666667-113.066667c-57.6-38.4-106.666667-12.8-106.666666 57.6v256c0 70.4 46.933333 96 106.666666 57.6l170.666667-113.066666c57.6-42.666667 57.6-106.666667 0-145.066667z"
                    fill="#ffffff"
                    p-id="5047"
                  ></path>
                </svg>
              </div>
              <!-- live图标识 -->
              <div class="top-tag-sign" v-if="item.noteType === '3'">
                <svg
                  t="1734054678308"
                  class="icon"
                  viewBox="0 0 1024 1024"
                  version="1.1"
                  xmlns="http://www.w3.org/2000/svg"
                  p-id="7819"
                  width="25"
                  height="25"
                >
                  <path
                    d="M526.49882864 364.2931832c-74.8243121 0-135.66761086 60.84329876-135.66761086 135.66761088s60.84329876 135.66761086 135.66761086 135.66761086S662.1664395 574.78510617 662.1664395 499.96079408s-60.97275259-135.66761086-135.66761086-135.66761088z m0 211.78646125c-41.94304 0-76.11885037-34.17581037-76.11885037-76.11885037s34.17581037-76.11885037 76.11885037-76.11885038 76.11885037 34.17581037 76.11885038 76.11885038-34.17581037 76.11885037-76.11885038 76.11885037z"
                    fill="#ffffff"
                    p-id="7820"
                  ></path>
                  <path
                    d="M526.49882864 239.88805531c-143.4348405 0-260.07273876 116.63789827-260.07273877 260.07273877s116.63789827 260.07273876 260.07273877 260.07273875 260.07273876-116.63789827 260.07273877-260.07273875S669.93366914 239.88805531 526.49882864 239.88805531z m0 480.53260642c-121.5571437 0-220.45986765-98.90272395-220.45986766-220.45986765s98.90272395-220.45986765 220.45986766-220.45986766 220.45986765 98.90272395 220.45986766 220.45986766S648.05597235 720.42066173 526.49882864 720.42066173zM526.49882864 183.96400197c13.46319803 0 24.3373195-10.87412148 24.33731951-24.3373195s-10.87412148-24.3373195-24.33731951-24.3373195-24.3373195 10.87412148-24.3373195 24.3373195 10.87412148 24.3373195 24.3373195 24.3373195zM460.73628445 190.82505482c13.07483653-2.84798419 21.48933531-15.66391309 18.77080494-28.86820346-2.84798419-13.07483653-15.66391309-21.48933531-28.86820347-18.77080494-13.07483653 2.84798419-21.48933531 15.66391309-18.77080494 28.86820345s15.79336691 21.61878914 28.86820347 18.77080495zM397.95117827 211.2787595c12.29811358-5.43706075 17.73517431-19.80643555 12.29811358-32.10454913-5.43706075-12.29811358-19.80643555-17.73517431-32.10454913-12.29811357-12.29811358 5.43706075-17.73517431 19.80643555-12.29811358 32.10454912 5.43706075 12.16865975 19.80643555 17.73517431 32.10454913 12.29811358zM340.73258667 244.28948543c10.87412148-7.89668347 13.33374419-23.04278124 5.43706074-33.91690271-7.89668347-10.87412148-23.04278124-13.33374419-33.91690272-5.43706075-10.87412148 7.89668347-13.33374419 23.04278124-5.43706074 33.91690272 7.89668347 10.87412148 23.04278124 13.33374419 33.91690272 5.43706074zM291.66958617 288.4332405c8.93231408-9.96794469 8.15559111-25.37295013-1.81235358-34.3052642-9.96794469-8.93231408-25.37295013-8.15559111-34.3052642 1.81235357-8.93231408 9.96794469-8.15559111 25.37295013 1.81235358 34.30526421 9.96794469 9.0617679 25.37295013 8.28504494 34.3052642-1.81235358zM219.56380445 350.82998518c11.65084445 6.73159902 26.53803457 2.71853037 33.26963358-8.93231407 6.73159902-11.65084445 2.71853037-26.53803457-8.93231408-33.26963358-11.65084445-6.73159902-26.53803457-2.71853037-33.26963358 8.93231408-6.60214518 11.65084445-2.58907653 26.53803457 8.93231408 33.26963357zM195.35593876 417.88706765c12.81592889 4.14252247 26.53803457-2.84798419 30.68055705-15.66391308 4.14252247-12.81592889-2.84798419-26.53803457-15.66391309-30.68055704-12.81592889-4.14252247-26.53803457 2.84798419-30.68055703 15.66391308-4.14252247 12.81592889 2.84798419 26.53803457 15.66391307 30.68055704zM185.5174479 488.56885728c13.33374419 1.4239921 25.37295013-8.28504494 26.6674884-21.61878913 1.4239921-13.33374419-8.28504494-25.37295013-21.61878913-26.6674884-13.33374419-1.4239921-25.37295013 8.28504494-26.66748841 21.61878914-1.4239921 13.20429037 8.28504494 25.2434963 21.61878914 26.66748839zM212.1849363 532.97152c-1.4239921-13.33374419-13.33374419-23.04278124-26.6674884-21.61878914-13.33374419 1.4239921-23.04278124 13.33374419-21.61878914 26.66748839 1.4239921 13.33374419 13.33374419 23.04278124 26.66748841 21.61878914 13.33374419-1.4239921 23.04278124-13.33374419 21.61878913-26.66748839zM226.03649581 597.56897975c-4.14252247-12.81592889-17.86462815-19.80643555-30.68055705-15.66391308-12.81592889 4.14252247-19.80643555 17.86462815-15.66391307 30.68055703 4.14252247 12.81592889 17.86462815 19.80643555 30.68055703 15.6639131 12.81592889-4.14252247 19.80643555-17.86462815 15.66391309-30.68055705zM252.83343803 657.8944632c-6.73159902-11.65084445-21.61878914-15.66391309-33.26963358-8.93231406-11.65084445 6.73159902-15.66391309 21.61878914-8.93231408 33.26963358 6.73159902 11.65084445 21.61878914 15.66391309 33.26963358 8.93231408 11.65084445-6.73159902 15.66391309-21.61878914 8.93231408-33.2696336zM257.36432197 709.54654025c-9.96794469 8.93231408-10.74466765 24.3373195-1.81235358 34.3052642 8.93231408 9.96794469 24.3373195 10.74466765 34.3052642 1.81235358 9.96794469-8.93231408 10.74466765-24.3373195 1.81235358-34.3052642-8.93231408-9.96794469-24.3373195-10.74466765-34.3052642-1.81235358zM306.81568395 760.93970963c-7.89668347 10.87412148-5.43706075 26.02021925 5.43706074 33.91690272 10.87412148 7.89668347 26.02021925 5.43706075 33.91690272-5.43706074 7.89668347-10.87412148 5.43706075-26.02021925-5.43706074-33.91690272-10.87412148-7.89668347-26.02021925-5.43706075-33.91690272 5.43706074zM397.95117827 788.64282864c-12.29811358-5.43706075-26.66748839 0-32.10454913 12.29811358-5.43706075 12.29811358 0 26.66748839 12.29811358 32.10454914 12.29811358 5.43706075 26.66748839 0 32.10454913-12.29811358 5.56651457-12.29811358 0-26.66748839-12.29811358-32.10454914zM460.73628445 808.9670795c-13.07483653-2.84798419-26.02021925 5.56651457-28.86820347 18.77080495s5.56651457 26.02021925 18.77080494 28.86820345c13.07483653 2.84798419 26.02021925-5.56651457 28.86820347-18.77080493 2.84798419-13.07483653-5.56651457-26.02021925-18.77080494-28.86820347zM526.49882864 815.95758617c-13.46319803 0-24.3373195 10.87412148-24.3373195 24.33731952s10.87412148 24.3373195 24.3373195 24.33731949S550.83614815 853.7581037 550.83614815 840.29490569s-10.87412148-24.3373195-24.33731951-24.33731952zM592.13191902 808.9670795c-13.07483653 2.84798419-21.48933531 15.66391309-18.77080494 28.86820347s15.66391309 21.48933531 28.86820345 18.77080493c13.07483653-2.84798419 21.48933531-15.66391309 18.77080494-28.86820345-2.71853037-13.20429037-15.66391309-21.48933531-28.86820345-18.77080495zM655.04647902 788.64282864c-12.29811358 5.43706075-17.73517431 19.80643555-12.29811359 32.10454914 5.43706075 12.29811358 19.80643555 17.73517431 32.10454914 12.29811358 12.29811358-5.43706075 17.73517431-19.80643555 12.29811358-32.10454914-5.56651457-12.29811358-19.93588939-17.86462815-32.10454913-12.29811358zM712.26507061 755.50264889c-10.87412148 7.89668347-13.33374419 23.04278124-5.43706074 33.91690272s23.04278124 13.33374419 33.91690272 5.43706074c10.87412148-7.89668347 13.33374419-23.04278124 5.43706074-33.91690272s-23.04278124-13.33374419-33.91690272-5.43706074zM761.32807111 711.35889383c-8.93231408 9.96794469-8.15559111 25.37295013 1.81235358 34.3052642 9.96794469 8.93231408 25.37295013 8.15559111 34.3052642-1.81235358 8.93231408-9.96794469 8.15559111-25.37295013-1.81235358-34.3052642-9.96794469-8.93231408-25.37295013-8.15559111-34.3052642 1.81235358zM833.30439902 648.96214914c-11.65084445-6.73159902-26.53803457-2.71853037-33.26963359 8.93231406-6.73159902 11.65084445-2.71853037 26.53803457 8.93231407 33.2696336 11.65084445 6.73159902 26.53803457 2.71853037 33.26963359-8.93231408 6.73159902-11.65084445 2.71853037-26.53803457-8.93231407-33.26963358zM857.64171852 581.90506667c-12.81592889-4.14252247-26.53803457 2.84798419-30.68055704 15.66391308-4.14252247 12.81592889 2.84798419 26.53803457 15.66391309 30.68055705 12.81592889 4.14252247 26.53803457-2.84798419 30.68055704-15.6639131 4.14252247-12.81592889-2.84798419-26.53803457-15.66391309-30.68055703zM867.48020939 511.35273086c-13.33374419-1.4239921-25.37295013 8.28504494-26.66748841 21.61878914-1.4239921 13.33374419 8.28504494 25.37295013 21.61878915 26.66748839 13.33374419 1.4239921 25.37295013-8.28504494 26.66748839-21.61878914 1.4239921-13.33374419-8.28504494-25.2434963-21.61878913-26.66748839zM840.68326717 466.95006815c1.4239921 13.33374419 13.33374419 23.04278124 26.66748838 21.61878913s23.04278124-13.33374419 21.61878914-26.66748839c-1.4239921-13.33374419-13.33374419-23.04278124-26.66748839-21.61878914-13.33374419 1.29453827-22.91332741 13.20429037-21.61878913 26.6674884zM826.96116148 402.22315457c4.14252247 12.81592889 17.86462815 19.80643555 30.68055704 15.66391308 12.81592889-4.14252247 19.80643555-17.86462815 15.66391309-30.68055704-4.14252247-12.81592889-17.86462815-19.80643555-30.68055704-15.66391308-12.81592889 4.2719763-19.80643555 17.99408197-15.66391309 30.68055704zM800.16421925 341.89767111c6.73159902 11.65084445 21.61878914 15.66391309 33.26963358 8.93231407 11.65084445-6.73159902 15.66391309-21.61878914 8.93231408-33.26963357-6.73159902-11.65084445-21.61878914-15.66391309-33.26963358-8.93231408-11.65084445 6.86105283-15.66391309 21.61878914-8.93231408 33.26963358zM795.63333531 290.24559408c9.96794469-8.93231408 10.74466765-24.3373195 1.81235358-34.30526421-8.93231408-9.96794469-24.3373195-10.74466765-34.3052642-1.81235357-9.96794469 8.93231408-10.74466765 24.3373195-1.81235358 34.3052642 8.93231408 10.09739852 24.3373195 10.87412148 34.3052642 1.81235358zM746.18197333 238.85242469c7.89668347-10.87412148 5.43706075-26.02021925-5.43706074-33.91690272-10.87412148-7.89668347-26.02021925-5.43706075-33.91690272 5.43706075-7.89668347 10.87412148-5.43706075 26.02021925 5.43706074 33.91690271 10.87412148 7.89668347 26.02021925 5.43706075 33.91690272-5.43706074zM655.04647902 211.2787595c12.29811358 5.43706075 26.66748839 0 32.10454913-12.29811358 5.43706075-12.29811358 0-26.66748839-12.29811358-32.10454912-12.29811358-5.43706075-26.66748839 0-32.10454914 12.29811357-5.56651457 12.29811358 0 26.66748839 12.29811359 32.10454913zM592.13191902 190.82505482c13.07483653 2.84798419 26.02021925-5.56651457 28.86820345-18.77080495 2.84798419-13.07483653-5.56651457-26.02021925-18.77080494-28.86820345s-26.02021925 5.56651457-28.86820345 18.77080494c-2.71853037 13.20429037 5.69596839 26.14967309 18.77080494 28.86820346z"
                    fill="#ffffff"
                    p-id="7821"
                  ></path>
                </svg>
              </div>
              <div class="top-tag-area" v-show="type === 1 && item.pinned === '1' && props.status !== 'approved'">
                <div class="top-wrapper">{{ $t("common.pinned") }}</div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </template>
    </Waterfall>
  </div>

  <Main
    v-show="mainShow"
    :nid="nid"
    :scene="scene"
    :nowTime="new Date()"
    :editMode="editMode"
    class="animate__animated animate__zoomIn animate__delay-0.5s"
    @click-main="close"
  ></Main>
</template>

<script lang="ts" setup>
import { Waterfall } from "vue-waterfall-plugin-next";
import "vue-waterfall-plugin-next/dist/style.css";
import { ElMessage } from "element-plus";
import { ref, onMounted, watch, computed } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
import { getTrendByUser } from "@/api/user";
import Main from "@/views/main/main.vue";
import { options } from "@/constant/constant";
import { useRoute } from "vue-router";
import { useUserStore } from "@/store/userStore";
import { getNoteById } from "@/api/note";
import { reportNoteBehavior } from "@/api/noteBehavior";
import { likeOrCollectionByDTO } from "@/api/likeOrCollection";
import type { NoteInfo } from "@/type/note";
import type { LikeOrCollectionDTO } from "@/type/likeOrCollection";

const isLogin = ref(false);
const route = useRoute();

// 移动端检测
const isMobile = computed(() => window.innerWidth <= 695);
// 移动端关闭周围gutter，避免右侧被裁剪
const hasAroundGutter = computed(() => !isMobile.value);

const editMode = ref(false);
const props = defineProps({
  type: {
    type: Number,
    default: 1,
  },
  isCollect: {
    type: Boolean,
    default: false,
  },
  status: {
    type: String,
    default: "all", // 'all' | 'pending' | 'approved' | 'rejected'
  },
  /** 来源场景：用于曝光/观看统计 recommend-推荐 follow-关注 profile-个人页 other */
  scene: {
    type: String,
    default: "profile",
  },
});

const STATUS_MAP = {
  all: "-1", // 全部
  pending: "0", // 审核中
  approved: "1", // 已通过
  rejected: "2", // 未通过
} as const;

const emit = defineEmits(["update-total"]);

watch(
  () => [props.type, props.isCollect, props.status],
  () => {
    currentPage.value = 1;
    noteList.value = [] as Array<any>;
    getNoteList();
  }
);

const noteList = ref<Array<any>>([]);
const noteTotal = ref(0);
const uid = route.query.uid as string;
const currentPage = ref(1);
const pageSize = 20;
const nid = ref("");
const mainShow = ref(false);
const userStore = useUserStore();
const currentUid = userStore.getUserInfo().id;
const noteInfo = ref<NoteInfo>({
  id: "",
  title: "",
  content: "",
  noteCover: "",
  noteType: "",
  uid: "",
  username: "",
  avatar: "",
  imgList: [],
  type: -1,
  likeCount: 0,
  collectionCount: 0,
  commentCount: 0,
  tagList: [],
  time: "",
  isFollow: false,
  isLike: false,
  isCollection: false,
  pinned: "0",
  province: "",
  city: "",
  district: "",
  address: "",
  relatedProducts: [],
  auditStatus: "",
});
const likeOrComment = ref({
  isLike: false,
  isComment: false,
});

const noLoginNotice = () => {
  if (!isLogin.value) {
    ElMessage.warning(t("common.notLoggedIn"));
    return false;
  }
  return true;
};

const handleLike = (item: any) => {
  // 审核中或未通过不能点赞
  if (item.auditStatus === "0" || item.auditStatus === "2") {
    ElMessage.warning(t("note.cannotLikeRejected"));
    return;
  }
  // 原有逻辑
  likeOrCollection(item.id, 1, item.isLike ? -1 : 1);
};

const likeOrCollection = (nid: string, type: number, val: number) => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }
  getNoteById(nid).then((res: any) => {
    noteInfo.value = res.data;

    const likeOrCollectionDTO = {} as LikeOrCollectionDTO;
    likeOrCollectionDTO.likeOrCollectionId = noteInfo.value.id;
    likeOrCollectionDTO.publishUid = noteInfo.value.uid;
    likeOrCollectionDTO.type = type == 1 ? 1 : 3;
    likeOrCollectionByDTO(likeOrCollectionDTO).then(() => {
      noteInfo.value.isLike = val == 1;
      noteInfo.value.likeCount += val;
      likeOrComment.value.isLike = val == 1;

      // 同步更新 noteList 中的对应项
      const noteIndex = noteList.value.findIndex((note) => note.id === nid);
      if (noteIndex !== -1) {
        noteList.value[noteIndex].likeCount += val;
        noteList.value[noteIndex].isLike = val == 1;
      }
    });
  });
};

const editNote = (noteId: string) => {
  nid.value = noteId;
  // 通过 props 传递给 Main 组件，让其自动进入编辑状态
  editMode.value = true;
  mainShow.value = true;
};

const handleLoad = (item: any) => {
  item.isLoading = true;
  if (userStore.isLogin()) {
    reportNoteBehavior({ eventType: "exposure", nid: String(item.id), scene: props.scene }).catch(() => {});
  }
};

const close = () => {
  mainShow.value = false;
  editMode.value = false; // 关键：每次关闭都重置
};

const toMain = (noteId: string) => {
  // router.push({ name: "main", state: { nid: nid } });
  nid.value = noteId;
  editMode.value = false;
  mainShow.value = true;
};

const setData = (res: any) => {
  const { records, total } = res.data;
  noteTotal.value = total;

  // 根据不同状态筛选数据
  const filteredRecords = records.filter((item: any) => {
    if (props.status === "all") {
      // 全部状态时，非当前用户只显示已通过的
      return item.uid === currentUid || item.auditStatus === "1";
    }
    // 其他状态下直接使用后端过滤后的数据
    return true;
  });

  noteList.value.push(...filteredRecords);
  emit("update-total", total);
};

const getNoteList = () => {
  const actualType = props.type === 3 ? 3 : props.type;
  const statusCode = STATUS_MAP[props.status as keyof typeof STATUS_MAP] || "-1";
  getTrendByUser(currentPage.value, pageSize, uid, actualType, statusCode).then((res) => {
    setData(res);
  });
};

const loadMoreData = () => {
  currentPage.value += 1;
  getNoteList();
};

const initData = () => {
  isLogin.value = userStore.isLogin();
  getNoteList();
};

onMounted(() => {
  initData();
});
</script>

<style lang="less" scoped>
// 主题变量
@primary-color: #3d8af5;
@text-primary: rgba(51, 51, 51, 0.8);
@text-secondary: rgba(51, 51, 51, 0.6);
@border-color: rgba(0, 0, 0, 0.08);
@white: #fff;

.image-container {
  position: relative;
  display: block;
  width: 100%;
}

.audit-eye {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.audit-eye .icon {
  margin-bottom: 8px;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5); /* 半透明背景 */
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  font-size: 20px;
}

.overlay.passing {
  color: white;
}

.overlay.not-passed {
  color: red; /* 设置未通过状态的字体颜色为红色 */
}

.edit-btn-on-img {
  position: absolute;
  top: 10px; // 距离图片顶部10px
  right: 10px; // 距离图片右侧10px
  z-index: 10; // 保证按钮在图片之上
  padding: 2px 10px;
  font-size: 12px;
  opacity: 0.92;
  // background-color: #ff2442 !important;
  // border-color: #ff2442 !important;
  // color: #fff !important;

  // 移动端隐藏
  @media screen and (max-width: 695px) {
    display: none;
  }
}

.feeds-container {
  position: relative;
  transition: width 0.5s;
  margin: 0 auto;

  // 移动端优化
  @media screen and (max-width: 695px) {
    width: 100%;
    padding: 0;
  }

  .noteImg {
    width: 240px;
    max-height: 300px;
    object-fit: cover;
    border-radius: 8px;
  }

  .note-cover-img {
    width: 100%;
    display: block;
    border-radius: 20px;
    cursor: pointer;

    // 移动端优化
    @media screen and (max-width: 695px) {
      border-radius: 20px;
    }

    :deep(img) {
      width: 100%;
      height: auto;
      max-height: 350px;
      object-fit: cover;
      display: block;

      // 移动端优化
      @media screen and (max-width: 695px) {
        max-height: 300px;
      }
    }
  }

  .card {
    position: relative;
    background-color: var(--card-bg);
    border-radius: 20px;
    // box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    width: 100%;

    // 移动端优化
    @media screen and (max-width: 695px) {
      border-radius: 20px;
      // box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
    }

    .top-tag-sign {
      position: absolute;
      right: 12px;
      top: 12px;
      z-index: 4;
    }

    .top-tag-area {
      position: absolute;
      left: 12px;
      top: 12px;
      z-index: 4;

      .top-wrapper {
        background: @primary-color;
        border-radius: 999px;
        font-weight: 500;
        color: #fff;
        line-height: 120%;
        font-size: 12px;
        padding: 5px 8px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .footer {
    padding: 12px;

    // 移动端优化
    @media screen and (max-width: 695px) {
      padding: 8px 10px;
    }

    .title {
      margin-bottom: 8px;
      word-break: break-all;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      overflow: hidden;
      font-weight: 500;
      font-size: 14px;
      line-height: 140%;
      color: var(--card-text);

      // 移动端优化
      @media screen and (max-width: 695px) {
        font-size: 13px;
        line-height: 1.4;
        margin-bottom: 6px;
        -webkit-line-clamp: 2;
        line-clamp: 2;
      }
    }

    .author-wrapper {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 20px;
      color: var(--card-text-secondary);
      font-size: 12px;
      transition: color 1s;

      // 移动端优化
      @media screen and (max-width: 695px) {
        font-size: 11px;
        height: 18px;
      }

      .author {
        display: flex;
        align-items: center;
        color: inherit;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-right: 12px;

        .author-avatar {
          margin-right: 6px;
          width: 20px;
          height: 20px;
          border-radius: 20px;
          border: 1px solid rgba(0, 0, 0, 0.08);
          flex-shrink: 0;
          object-fit: cover;

          // 移动端优化
          @media screen and (max-width: 695px) {
            width: 18px;
            height: 18px;
            margin-right: 4px;
          }
        }

        .name {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .like-wrapper {
        position: relative;
        cursor: pointer;
        display: flex;
        align-items: center;
        flex-shrink: 0;

        .count {
          margin-left: 2px;
        }
      }

      .like-disabled {
        pointer-events: none; /* 禁止点击 */
        opacity: 0.5; /* 变灰 */
        cursor: not-allowed;
      }
    }
  }
}
</style>
