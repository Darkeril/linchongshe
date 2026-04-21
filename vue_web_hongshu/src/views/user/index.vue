<template>
  <div class="user-page">
    <div class="user">
      <div class="user-info">
        <div class="avatar">
          <div class="avatar-wrapper">
            <img :src="userInfo.avatar" class="user-image" style="border: 0.0625rem solid rgba(0, 0, 0, 0.08)" />
          </div>
        </div>
        <div class="info-part">
          <div class="info">
            <div class="basic-info">
              <div class="user-basic">
                <div class="user-nickname">
                  <div class="user-name">
                    <span> {{ userInfo.username }}</span>
                  </div>
                </div>
                <div class="user-content">
                  <span class="user-redId">{{ $t("user.userId") }}：{{ userInfo.hsId }}</span>
                  <span class="user-ip">{{ $t("user.ipLocation") }}：{{ formatProvince(userInfo.province) }}</span>
                </div>
              </div>
            </div>
            <div class="user-desc">
              <div>
                <span v-if="userInfo.description === null">{{ $t("user.noDesc") }}</span>
                <span v-else>{{ userInfo.description }}</span>
              </div>
            </div>
            <div class="user-tags">
              <el-tag v-for="tag in tagList" :key="tag" effect="light" type="info" round style="margin-left: 0.3125rem">
                {{ tag }}
              </el-tag>
              <!-- 当没有标签时显示提示文本 -->
              <span v-if="!tagList.length" class="no-tags">{{ $t("user.noTags") }}</span>
            </div>
            <div class="data-info">
              <div class="user-interactions">
                <div>
                  <span class="count">{{ userInfo.noteCount }}</span>
                  <span class="shows">{{ $t("user.notes") }}</span>
                </div>
                <div>
                  <span class="count">{{ userInfo.productCount }}</span>
                  <span class="shows">{{ $t("user.works") }}</span>
                </div>
                <div>
                  <span class="count">{{ userInfo.followerCount }}</span>
                  <span class="shows">{{ $t("user.following") }}</span>
                </div>
                <div>
                  <span class="count">{{ userInfo.fanCount }}</span>
                  <span class="shows">{{ $t("user.fans") }}</span>
                </div>
                <div>
                  <span class="count">{{ likeNoteCount }}</span>
                  <span class="shows">{{ $t("user.likes") }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="follow"></div>
          <div class="action-buttons" v-if="uid === currentUid">
            <el-tooltip :content="$t('user.edit')" effect="dark" placement="bottom">
              <el-button class="action-btn" @click="editUser">
                <svg
                  t="1743846527433"
                  class="icon"
                  viewBox="0 0 1024 1024"
                  version="1.1"
                  xmlns="http://www.w3.org/2000/svg"
                  p-id="9869"
                  width="20"
                  height="20"
                >
                  <path
                    d="M518.72 85.333333a203.050667 203.050667 0 0 1 203.050667 203.050667v96.469333c0 56.298667-22.613333 110.229333-62.805334 149.674667l-42.794666 42.026667a3.562667 3.562667 0 0 0-1.066667 2.538666v6.016c0 0.405333 0.234667 0.768 0.597333 0.938667l295.808 139.413333a103.104 103.104 0 0 1 59.157334 93.269334v10.752A87.850667 87.850667 0 0 1 882.816 917.333333H141.184c-46.442667 0-84.8-36.16-87.722667-83.328l-0.128-4.522666v-10.752c0-37.717333 20.586667-72.362667 54.421334-90.88l4.736-2.389334 296.192-139.562666c0.085333 0.042667 0.128 0.256 0.085333 0.746666l0.128-1.536v-6.016c0-0.853333-0.298667-1.642667 0.213333-1.066666l-1.28-1.493334-42.794666-42.005333a210.112 210.112 0 0 1-62.698667-142.613333l-0.106667-7.061334v-96.469333c0-109.781333 87.253333-199.594667 197.226667-202.965333L505.28 85.333333z m0 64h-12.949333l-4.842667 0.064a139.050667 139.050667 0 0 0-134.698667 138.986667v95.872l0.085334 5.909333a145.770667 145.770667 0 0 0 43.562666 98.709334l43.904 43.157333 3.456 3.84c10.112 12.117333 15.658667 27.392 15.658667 43.221333l-0.042667 7.552-0.256 4.693334a65.024 65.024 0 0 1-37.013333 52.608L140.650667 782.912l-2.986667 1.514667a39.104 39.104 0 0 0-20.330667 34.304v9.749333l0.042667 2.538667A23.850667 23.850667 0 0 0 141.184 853.333333h741.632c13.162667 0 23.850667-10.666667 23.850667-23.850666v-10.752c0-15.146667-8.746667-28.928-22.442667-35.392l-295.808-139.392a65.024 65.024 0 0 1-37.312-58.837334v-6.016c0-18.133333 7.296-35.498667 20.224-48.213333l42.794667-42.026667a145.770667 145.770667 0 0 0 43.648-104v-96.469333c0-76.8-62.250667-139.050667-139.050667-139.050667zM951.466667 533.333333c4.693333 0 8.533333 3.84 8.533333 8.533334v46.933333a8.533333 8.533333 0 0 1-8.533333 8.533333h-89.6a8.533333 8.533333 0 0 1-8.533334-8.533333v-46.933333c0-4.693333 3.84-8.533333 8.533334-8.533334h89.6z m0-128c4.693333 0 8.533333 3.84 8.533333 8.533334v46.933333a8.533333 8.533333 0 0 1-8.533333 8.533333h-153.6a8.533333 8.533333 0 0 1-8.533334-8.533333v-46.933333c0-4.693333 3.84-8.533333 8.533334-8.533334h153.6z m0-128c4.693333 0 8.533333 3.84 8.533333 8.533334v46.933333a8.533333 8.533333 0 0 1-8.533333 8.533333h-153.6a8.533333 8.533333 0 0 1-8.533334-8.533333v-46.933333c0-4.693333 3.84-8.533333 8.533334-8.533334h153.6z"
                    fill="#333333"
                    p-id="9870"
                  ></path>
                </svg>
              </el-button>
            </el-tooltip>
            <el-tooltip :content="$t('user.editAddress')" effect="dark" placement="bottom">
              <el-button class="action-btn" @click="editAddress">
                <svg
                  t="1745172773495"
                  class="icon"
                  viewBox="0 0 1024 1024"
                  version="1.1"
                  xmlns="http://www.w3.org/2000/svg"
                  p-id="9061"
                  width="20"
                  height="20"
                >
                  <path
                    d="M811.084 578.703L531.642 996.204a29.483 29.483 0 0 1-24.557 13.148h-0.002c-9.818 0-19.041-4.927-24.557-13.127L203.219 579.804c-34.029-54.436-51.751-116.094-51.751-179.049 0-189.898 159.406-344.416 355.359-344.416 195.927 0 355.359 154.508 355.359 344.416 0 62.864-17.669 124.398-51.088 177.949zM506.829 115.917c-163.293 0-296.128 127.782-296.128 284.842 0 51.751 14.606 102.412 42.17 146.556l254.208 378.977 254.4-380.022c26.976-43.239 41.484-93.845 41.484-145.498 0-157.055-132.853-284.842-296.117-284.842z m0 447.346c-92.846 0-168.368-73.486-168.368-163.827 0-90.292 75.523-163.791 168.368-163.791 92.846 0 168.359 73.474 168.359 163.791 0 90.315-75.506 163.827-168.359 163.827z m0-268.044c-60.212 0-109.156 46.774-109.156 104.239 0 57.471 48.953 104.239 109.156 104.239 60.195 0 109.143-46.766 109.143-104.239 0-57.461-48.953-104.239-109.143-104.239z"
                    fill=""
                    p-id="9062"
                  ></path>
                </svg>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
      <div class="tool-btn" v-show="uid !== currentUid">
        <el-button :icon="ChatLineRound" circle @click="chatShow = true" />
        <el-button plain type="primary" round v-if="_isFollow" @click="follow(uid, 1)">{{
          $t("user.followed")
        }}</el-button>
        <el-button type="primary" round v-else @click="follow(uid, 0)">{{ $t("user.follow") }}</el-button>
      </div>
    </div>
    <div class="reds-sticky-box user-page-sticky" style="--1ee3a37c: all 0.4s cubic-bezier(0.2, 0, 0.25, 1) 0s">
      <div class="reds-sticky">
        <div class="tertiary center reds-tabs-list">
          <div :class="type == 1 ? 'reds-tab-item active' : 'reds-tab-item'">
            <span @click="toPage(1)">{{ $t("user.myNotes") }}</span>
          </div>
          <div :class="type == 4 ? 'reds-tab-item active' : 'reds-tab-item'">
            <span @click="toPage(4)">{{ $t("user.myItems") }}</span>
          </div>
          <div :class="type == 2 ? 'reds-tab-item active' : 'reds-tab-item'">
            <span @click="toPage(2)">{{ $t("user.myLikes") }}</span>
          </div>
          <div :class="type == 3 ? 'reds-tab-item active' : 'reds-tab-item'">
            <span @click="toPage(3)">{{ $t("user.myCollections") }}</span>
          </div>
          <div class="active-tag" style="width: 4rem; left: 39.1875rem"></div>
        </div>
      </div>
    </div>
    <!-- 笔记 -->
    <div class="divider" v-show="type === 1"></div>
    <div class="count-info" v-show="type === 1">
      <!-- 其他用户只显示全部 -->
      <template v-if="uid !== currentUid">
        <!-- <span class="count-item active">
          全部 · {{ allCount }}
        </span> -->
      </template>
      <!-- 当前用户显示所有状态 -->
      <template v-else>
        <span :class="['count-item', { active: selectedNoteStatus === 'all' }]" @click="switchNoteStatus('all')">
          {{ $t("user.all") }} · {{ allCount }}
        </span>
        <span
          :class="['count-item', { active: selectedNoteStatus === 'pending' }]"
          @click="switchNoteStatus('pending')"
        >
          {{ $t("user.pending") }} · {{ pendingCount }}
        </span>
        <span
          :class="['count-item', { active: selectedNoteStatus === 'approved' }]"
          @click="switchNoteStatus('approved')"
        >
          {{ $t("user.approved") }} · {{ approvedCount }}
        </span>
        <span
          :class="['count-item', { active: selectedNoteStatus === 'rejected' }]"
          @click="switchNoteStatus('rejected')"
        >
          {{ $t("user.rejected") }} · {{ rejectedCount }}
        </span>
      </template>
    </div>
    <div class="divider" v-show="type === 2"></div>
    <!-- 收藏 -->
    <div class="divider" v-show="type === 3"></div>
    <div class="count-info" v-show="type === 3">
      <span :class="['count-item', { active: selectedCount === 'note' }]" @click="switchCountType('note')">
        {{ $t("note.title") }} · {{ noteCount }}
      </span>
      <span :class="['count-item', { active: selectedCount === 'product' }]" @click="switchCountType('product')">
        {{ $t("idle.title") }} · {{ productCount }}
      </span>
    </div>
    <!-- 闲宝 -->
    <div class="divider" v-show="type === 4"></div>
    <div class="count-info" v-show="type === 4">
      <!-- 其他用户只显示全部 -->
      <template v-if="uid !== currentUid">
        <!-- <span class="count-item active">
          全部 · {{ publishCount }}
        </span> -->
      </template>
      <!-- 当前用户显示所有状态 -->
      <template v-else>
        <span :class="['count-item', { active: selectedCount === 'publish' }]" @click="switchCountType('publish')">
          {{ $t("user.all") }} · {{ publishCount }}
        </span>
        <span :class="['count-item', { active: selectedCount === 'sell' }]" @click="switchCountType('sell')">
          {{ $t("user.sold") }} · {{ sellCount }}
        </span>
        <span :class="['count-item', { active: selectedCount === 'buy' }]" @click="switchCountType('buy')">
          {{ $t("user.bought") }} · {{ buyCount }}
        </span>
        <!-- <span  :class="['count-item', { 'active': selectedCount === 'collect' }]" @click="switchCountType('collect')">
          收藏的 · {{ collectCount }}
        </span> -->
      </template>
    </div>
    <el-drawer
      v-model="chatShow"
      direction="rtl"
      size="30%"
      :with-header="false"
      class="chat-drawer"
      @closed="handleDrawerClose"
    >
      <!-- 聊天组件 -->
      <NoteChat
        v-if="chatShow"
        :key="`chat-${uid}`"
        v-model="chatShow"
        :acceptUid="uid"
        class="chat-component animate__animated animate__fadeIn"
        @refresh-list="fetchMessages"
      />
    </el-drawer>
    <div class="feeds-tab-container" style="--1ee3a37c: all 0.4s cubic-bezier(0.2, 0, 0.25, 1) 0s">
      <Note
        v-if="type !== 4 && (type !== 3 || (type === 3 && selectedCount === 'note'))"
        :type="type"
        :isCollect="type === 3"
        :status="selectedNoteStatus"
      />
      <Product
        v-if="type === 4 || (type === 3 && selectedCount === 'product')"
        :type="type"
        :isCollect="type === 3 || selectedCount === 'collect'"
        :status="getProductStatus()"
      />
    </div>
    <!--抽屉 用户编辑-->
    <div>
      <el-drawer
        v-model="drawer"
        :title="editType === 'user' ? $t('user.editProfile') : $t('user.myAddress')"
        :size="450"
        destroy-on-close
      >
        <UserEdit
          v-if="editType === 'user'"
          :userInfo="userInfo"
          @update-success="handleUpdateSuccess"
          @close-drawer="closeDrawer"
        />
        <UserAddress v-if="editType === 'address'" @update-success="handleUpdateSuccess" @close-drawer="closeDrawer" />
      </el-drawer>
    </div>

    <div v-if="showEmptyState" class="empty-state">
      <el-empty :description="$t('common.noData')"> </el-empty>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ChatLineRound } from "@element-plus/icons-vue";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { getUserById, getCollectCount } from "@/api/user";
import { getIdleCount } from "@/api/idle";
import { getNoteCount } from "@/api/note";
import Note from "@/components/Note.vue";
import { useUserStore } from "@/store/userStore";
import NoteChat from "@/components/NoteChat.vue";
import { followById, isFollow } from "@/api/follower";
import { useRoute } from "vue-router";
import { ElMessage, ElDrawer } from "element-plus";
import Product from "@/components/Product.vue";
import UserEdit from "./user-edit.vue";
import UserAddress from "./user-address.vue";
import { getChatUserList } from "@/api/im";
import { useImStore } from "@/store/imStore";

const { t } = useI18n();
const route = useRoute();
const userStore = useUserStore();
const currentUid = userStore.getUserInfo().id;
const userInfo = ref<any>({});
const uid = route.query.uid as string;
const type = ref(1);
const chatShow = ref(false);
const _isFollow = ref(false);
const tagList = ref<string[]>([]);
const noteCount = ref(0);
const productCount = ref(0);
const likeNoteCount = ref(0);

const publishCount = ref(0);
const sellCount = ref(0);
const buyCount = ref(0);
const collectCount = ref(0);

const allCount = ref(0);
const pendingCount = ref(0);
const approvedCount = ref(0);
const rejectedCount = ref(0);
const loading = ref(false);
const imStore = useImStore();

type CountType = "note" | "product" | "publish" | "sell" | "buy" | "collect";
type NoteStatus = "all" | "pending" | "approved" | "rejected";

const selectedCount = ref<CountType>("note");
const selectedNoteStatus = ref<NoteStatus>("all");

const drawer = ref(false);
const editType = ref<"user" | "address">("user");

// 用户资料编辑抽屉
const editUser = () => {
  drawer.value = true;
  editType.value = "user";
};

// 地址编辑抽屉
const editAddress = () => {
  editType.value = "address";
  drawer.value = true;
};

// 关闭抽屉
const closeDrawer = () => {
  drawer.value = false;
};

const getProductStatus = () => {
  // 当在收藏页面且选择了闲宝时，status应该为collect
  if (type.value === 3 && selectedCount.value === "product") {
    return "collect";
  }
  // 其他情况保持原有逻辑
  return selectedCount.value === "note" || selectedCount.value === "product" ? undefined : selectedCount.value;
};

// 处理更新成功
const handleUpdateSuccess = async () => {
  // 重新获取用户信息
  await initData();
  // ElMessage.success('更新成功')
  closeDrawer();
};

const switchCountType = (type: CountType) => {
  selectedCount.value = type;
};

const switchNoteStatus = (status: NoteStatus) => {
  selectedNoteStatus.value = status;
};

/** 切换主 tab：立即更新 type，计数接口后台拉取，避免阻塞列表首屏请求 */
const toPage = (val: number) => {
  type.value = val;
  if (val === 1) {
    selectedNoteStatus.value = "all";
    void loadNoteCounts();
  } else if (val === 2) {
    selectedCount.value = "note";
    void loadNoteCounts();
  } else if (val === 3) {
    selectedCount.value = "note";
    void loadCollectCounts();
  } else if (val === 4) {
    selectedCount.value = "publish";
    void loadIdleCounts();
  }
};

const handleDrawerClose = async () => {
  await fetchMessages();
};

const fetchMessages = async () => {
  try {
    loading.value = true;
    const res = await getChatUserList();
    imStore.setNoteChatList(res.data);
  } catch (error) {
    console.error("获取聊天消息失败:", error);
  } finally {
    loading.value = false;
  }
};

const loadNoteCounts = async () => {
  try {
    const res = await getNoteCount(uid);
    allCount.value = res.data.allCount;
    pendingCount.value = res.data.pendingCount;
    approvedCount.value = res.data.approvedCount;
    rejectedCount.value = res.data.rejectedCount;
    likeNoteCount.value = res.data.likeCount;
  } catch (error) {
    console.error("Failed to load collect counts:", error);
    // ElMessage.error('获取笔记数量失败');
  }
};

const loadIdleCounts = async () => {
  try {
    const res = await getIdleCount(uid);
    publishCount.value = res.data.publishCount;
    sellCount.value = res.data.sellCount;
    buyCount.value = res.data.buyCount;
    collectCount.value = res.data.collectCount;
  } catch (error) {
    console.error("Failed to load collect counts:", error);
    ElMessage.error(t("user.getIdleCountFailed"));
  }
};

const showEmptyState = computed(() => {
  if (type.value === 1 && selectedNoteStatus.value === "all") {
    return allCount.value === 0;
  }
  if (type.value === 1 && selectedNoteStatus.value === "pending") {
    return pendingCount.value === 0;
  }
  if (type.value === 1 && selectedNoteStatus.value === "approved") {
    return approvedCount.value === 0;
  }
  if (type.value === 1 && selectedNoteStatus.value === "rejected") {
    return rejectedCount.value === 0;
  }
  if (type.value === 2) {
    return likeNoteCount.value === 0;
  }
  if (type.value === 3 && selectedCount.value === "note") {
    return noteCount.value === 0;
  }
  if (type.value === 3 && selectedCount.value === "product") {
    return productCount.value === 0;
  }
  if (type.value === 4 && selectedCount.value === "publish") {
    return publishCount.value === 0;
  }
  if (type.value === 4 && selectedCount.value === "sell") {
    return sellCount.value === 0;
  }
  if (type.value === 4 && selectedCount.value === "buy") {
    return buyCount.value === 0;
  }
  return false;
});

const loadCollectCounts = async () => {
  try {
    const res = await getCollectCount(uid);
    noteCount.value = res.data.noteCount;
    productCount.value = res.data.productCount;
    publishCount.value = res.data.publishCount;
    sellCount.value = res.data.sellCount;
    buyCount.value = res.data.buyCount;
    collectCount.value = res.data.collectCount;
  } catch (error) {
    console.error("Failed to load collect counts:", error);
    ElMessage.error(t("user.getCollectCountFailed"));
  }
};

const follow = (fid: string, type: number) => {
  followById(fid).then(() => {
    _isFollow.value = type == 0;
  });
};

// 格式化省份显示（去掉省、市、自治区等后缀）
const formatProvince = (province: string | null | undefined): string => {
  if (!province) return t("user.unknown");
  // 去掉后缀：省、市、自治区、特别行政区
  return province
    .replace(/省$/, "")
    .replace(/市$/, "")
    .replace(/自治区$/, "")
    .replace(/特别行政区$/, "");
};

const initData = () => {
  getUserById(uid).then((res) => {
    userInfo.value = res.data;
    if (res.data.tags != null) {
      tagList.value = JSON.parse(res.data.tags);
    }
  });
  isFollow(uid).then((res) => {
    _isFollow.value = res.data;
  });
  loadNoteCounts();
};

initData();
</script>

<style lang="less" scoped>
:deep(.el-button:hover) {
  background-color: #fff;
  color: black;
  border-color: #f4f4f4;
}

:deep(.el-tag) {
  border: 0;
}

.user-page {
  background: #fff;
  min-height: 100vh;

  // 移动端优化
  @media screen and (max-width: 695px) {
    width: 100vw;
    max-width: 100%;
    min-height: auto;
    padding-bottom: 80px;
    padding-top: 64px;
    overflow-x: hidden;
  }

  .user {
    position: relative;
    padding-top: 3rem;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;

    // 移动端优化
    @media screen and (max-width: 695px) {
      padding-top: 0;
    }

    .user-info {
      display: flex;
      justify-content: center;
      padding: 3rem 0;

      // 移动端优化
      @media screen and (max-width: 695px) {
        padding: 1.5rem 0;
        width: 100%;
        flex-wrap: wrap;
        align-items: flex-start;
      }

      .avatar {
        // 移动端优化
        @media screen and (max-width: 695px) {
          flex-shrink: 0;
          padding-left: 1rem;
        }

        .avatar-wrapper {
          text-align: center;
          width: 15.6667rem;
          height: 10.9667rem;

          // 移动端优化
          @media screen and (max-width: 695px) {
            width: 80px !important;
            height: 80px !important;
            text-align: left;
          }

          .user-image {
            border-radius: 50%;
            margin: 0 auto;
            width: 70%;
            height: 100%;
            object-fit: cover;

            // 移动端优化
            @media screen and (max-width: 695px) {
              width: 80px !important;
              height: 80px !important;
              margin: 0;
            }
          }

          .btn-avatr {
            border: 0.0625rem solid #f4f4f4;
            width: 2.875rem;
            font-size: 0.75rem;
            height: 1.75rem;
            color: #1f1e1e;
            border-radius: 0.5rem;
          }
          .btn-avatr:hover {
            background-color: #f4f4f4;
            color: #000;
          }
        }
      }

      .info-part {
        position: relative;

        // 移动端优化
        @media screen and (max-width: 695px) {
          margin-left: 0.5rem;
          flex: 1;
          min-width: 0;
          width: 100%;
        }

        .info {
          width: 33.3333rem;
          margin-left: 2rem;

          // 移动端优化
          @media screen and (max-width: 695px) {
            margin-left: 0;
            width: 100%;
          }

          .basic-info {
            display: flex;
            align-items: center;

            .user-basic {
              width: 100%;

              .user-nickname {
                width: 100%;
                display: flex;
                align-items: center;
                max-width: calc(100% - 6rem);

                .user-name {
                  font-weight: 600;
                  font-size: 1.5rem;
                  line-height: 120%;
                  color: #333;

                  // 移动端优化
                  @media screen and (max-width: 695px) {
                    font-size: 1.125rem;
                  }
                }
              }

              .user-content {
                width: 100%;
                font-size: 0.75rem;
                line-height: 120%;
                color: rgba(51, 51, 51, 0.6);
                display: flex;
                margin-top: 0.5rem;

                // 移动端优化
                @media screen and (max-width: 695px) {
                  flex-direction: column;
                  gap: 0.25rem;
                }

                .user-redId {
                  padding-right: 0.75rem;
                }

                .user-ip {
                  padding-right: 0.75rem;
                }
              }
            }
          }

          .user-desc {
            width: 100%;
            font-size: 0.875rem;
            line-height: 140%;
            color: #333;
            margin-top: 0.75rem;
            white-space: pre-line;

            // 移动端优化
            @media screen and (max-width: 695px) {
              font-size: 0.8125rem;
              margin-top: 2rem;
              margin-left: calc(-80px - 1.5rem);
              width: calc(100% + 80px + 1.5rem);
              padding-left: 1rem;
            }
          }

          .user-tags {
            height: 1.5rem;
            margin-top: 1rem;
            display: flex;

            // 移动端优化
            @media screen and (max-width: 695px) {
              margin-top: 0.75rem;
              flex-wrap: wrap;
              height: auto;
              margin-left: calc(-80px - 1.5rem);
              width: calc(100% + 80px + 1.5rem);
              padding-left: 0.5rem;
            }
            align-items: center;
            font-size: 0.75rem;
            color: #333;
            text-align: center;
            font-weight: 400;
            line-height: 120%;

            .tag-item :first-child {
              padding: 0.1875rem 0.375rem;
            }

            .tag-item {
              display: flex;
              align-items: center;
              justify-content: center;
              padding: 0.25rem 0.5rem;
              grid-gap: 0.25rem;
              gap: 0.25rem;
              height: 1.125rem;
              border-radius: 2.5625rem;
              background: rgba(0, 0, 0, 0.03);
              height: 1.5rem;
              line-height: 1.5rem;
              margin-right: 0.375rem;
              color: rgba(51, 51, 51, 0.6);
            }
            :hover {
              cursor: pointer; /* 显示小手指针 */
              transform: scale(1.15); /* 鼠标移入时按钮稍微放大 */
            }
          }

          .data-info {
            display: flex;
            align-items: center;
            justify-content: center;
            margin-top: 1.65rem;

            // 移动端优化
            @media screen and (max-width: 695px) {
              margin-top: 1.5rem;
              justify-content: flex-start;
              margin-left: calc(-80px - 1.5rem);
              width: calc(100% + 80px + 1.5rem);
              padding-left: 1rem;
            }

            .user-interactions {
              width: 100%;
              display: flex;
              align-items: center;

              // 移动端优化
              @media screen and (max-width: 695px) {
                justify-content: flex-start;
                gap: 0;
              }

              > div {
                // 移动端优化
                @media screen and (max-width: 695px) {
                  display: flex;
                  flex-direction: column;
                  align-items: flex-start;
                  gap: 0.125rem;
                  margin-right: 2rem;

                  &:last-child {
                    margin-right: 0;
                  }
                }
              }

              .count {
                font-weight: 500;
                font-size: 0.875rem;
                margin-right: 0.25rem;

                // 移动端优化
                @media screen and (max-width: 695px) {
                  margin-right: 0;
                  font-size: 1rem;
                  font-weight: 600;
                  color: #333;
                  line-height: 1.2;
                }
              }

              .shows {
                color: rgba(51, 51, 51, 0.6);
                font-size: 0.875rem;

                // 移动端优化
                @media screen and (max-width: 695px) {
                  font-size: 0.75rem;
                  color: rgba(51, 51, 51, 0.6);
                  line-height: 1.2;
                }
                line-height: 120%;
              }
            }

            .user-interactions > div {
              height: 100%;
              display: flex;
              align-items: center;
              justify-content: center;
              text-align: center;
              margin-right: 1rem;
            }
          }
        }

        .follow {
          position: absolute;
          margin-left: auto;
          display: block;
          right: 0;
          top: 0;
        }
      }

      .action-buttons {
        position: absolute;
        left: 100%;
        top: 0;
        display: flex;
        gap: 0px; // 按钮之间的间距

        .action-btn {
          height: 32px;
          padding: 0 16px;
          font-size: 14px;
          border-radius: 16px;

          // 可选：添加hover效果
          &:hover {
            opacity: 0.9;
            transform: translateY(-1px);
            transition: all 0.3s ease;
          }
        }
      }
    }

    .tool-btn {
      position: absolute;
      right: 5%; // 调整右侧距离
      top: 50%; // 垂直居中
      transform: translateY(-150%); // 确保完全居中
      display: flex;
      align-items: center;
      gap: 10px; // 按钮之间的间距
      z-index: 10; // 确保在其他元素之上

      // 响应式样式 - 移动端隐藏
      @media screen and (max-width: 695px) {
        display: none;
      }

      // 桌面端显示
      @media screen and (min-width: 696px) {
        display: flex;
      }
    }
  }

  .reds-sticky {
    padding: 1rem 0;
    z-index: 5 !important;
    background: hsla(0, 0%, 100%, 0.98);

    // 移动端优化
    @media screen and (max-width: 695px) {
      padding: 0.75rem 0;
      display: flex;
      justify-content: center;
      align-items: center;
      width: 100%;
      text-align: center;
    }

    .reds-tabs-list {
      @media screen and (min-width: 108rem) {
        width: 90.3333rem;
      }

      display: flex;
      flex-wrap: nowrap;
      position: relative;
      font-size: 1rem;
      justify-content: center;

      // 移动端优化
      @media screen and (max-width: 695px) {
        justify-content: center !important;
        width: 100% !important;
        max-width: 100% !important;
        padding: 0 !important;
        margin: 0 !important;
        box-sizing: border-box !important;
        gap: 0 !important;
      }

      .reds-tab-item {
        padding: 0rem 1rem;
        margin-right: 0rem;
        font-size: 1rem;
        display: flex;
        align-items: center;
        box-sizing: border-box;
        height: 2.5rem;
        cursor: pointer;
        color: rgba(51, 51, 51, 0.8);
        white-space: nowrap;
        transition: transform 0.3s cubic-bezier(0.2, 0, 0.25, 1);
        z-index: 1;

        // 移动端优化
        @media screen and (max-width: 695px) {
          flex: 0 0 auto;
          padding: 0 0.5rem;
        }
      }
      :hover {
        cursor: pointer; /* 显示小手指针 */
        transform: scale(1.15); /* 鼠标移入时按钮稍微放大 */
      }

      .reds-tab-item.active {
        background-color: rgba(0, 0, 0, 0.03);
        border-radius: 1.25rem;
        font-weight: 600;
        color: #333;
      }

      .active-tag {
        // 移动端隐藏下划线标签
        @media screen and (max-width: 695px) {
          display: none !important;
        }
      }
    }
  }

  .divider {
    border-bottom: 1px solid rgba(15, 14, 14, 0.1);
    margin: 10px 0;
    width: 100%;
    margin-top: 0;
  }

  .count-info {
    margin-bottom: 0.5rem;
    // margin-left: 2rem;
    font-size: 1rem;
    color: #333;
    display: flex;
    align-items: center;

    // 移动端隐藏筛选标签
    @media screen and (max-width: 695px) {
      display: none;
    }

    .count-item {
      margin-right: 0.5rem;
      font-weight: 500;
      cursor: pointer;
      padding: 0.5rem 1rem;
      border-radius: 1.25rem;
      transition: all 0.3s ease;

      &:last-child {
        margin-right: 0;
      }

      &:hover {
        background-color: rgba(0, 0, 0, 0.03);
      }

      &.active {
        background-color: rgba(0, 0, 0, 0.03);
        font-weight: 600;
        color: rgba(51, 51, 51, 0.8);
      }
    }
  }

  .feeds-tab-container {
    padding-left: 0rem;

    // 移动端优化
    @media screen and (max-width: 695px) {
      padding: 0;
      width: 100%;
      margin: 0;
      overflow-x: hidden;
    }
  }
}

.empty-state {
  position: absolute;
  bottom: -5%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  text-align: center;

  :deep(.el-empty) {
    padding: 40px 0;
  }
}
</style>
