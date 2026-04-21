<template>
  <div class="note-detail-mask" @click="close">
    <div class="note-container" @click.stop>
      <div class="media-container">
        <!-- 根据 type 判断是图片轮播还是视频播放器 -->
        <!-- 0 图文 / 1 多图 / 3 live 等走图片轮播；2 为视频（与后端 note_type 字典一致） -->
        <el-carousel
          v-if="String(noteInfo.noteType) !== '2'"
          height="90vh"
          :autoplay="false"
          :arrow="noteInfo.imgList && noteInfo.imgList.length > 1 ? 'hover' : 'never'"
          @change="handleCarouselChange"
        >
          <el-carousel-item v-for="(item, index) in noteInfo.imgList" :key="index">
            <el-image
              :style="{
                width: '100%',
                height: '100%',
              }"
              :src="getNginxProxyImageUrl(item)"
              fit="contain"
              class="animate__animated animate__zoomIn animate__delay-0.5s"
            />
          </el-carousel-item>
        </el-carousel>
        <!-- 仅多图时显示当前图片序号 -->
        <div v-if="noteInfo.imgList && noteInfo.imgList.length > 1" class="img-counter-badge">
          {{ carouselIndex + 1 }}/{{ noteInfo.imgList.length }}
        </div>
        <!-- 视频播放部分 -->
        <div v-if="String(noteInfo.noteType) === '2'" class="video-player">
          <video
            v-if="noteInfo.imgList.length > 0"
            ref="video"
            class="video"
            controls
            :src="getNginxProxyVideoUrl(noteInfo.imgList[0])"
            @pause="handlePause"
            @play="handlePlay"
            @ended="handleVideoEnded"
            style="width: 100%; height: 100%"
          >
            Your browser does not support the video tag.
          </video>
          <!-- 暂停符号 -->
          <div v-if="isPaused" class="pause-overlay" @click="handlePlayClick">
            <svg
              t="1734061569758"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="9973"
              width="100"
              height="100"
            >
              <path
                d="M780.8 475.733333L285.866667 168.533333c-27.733333-17.066667-64 4.266667-64 36.266667v614.4c0 32 36.266667 53.333333 64 36.266667l492.8-307.2c29.866667-14.933333 29.866667-57.6 2.133333-72.533334z"
                fill="#ffffff"
                p-id="9974"
              ></path>
            </svg>
          </div>
        </div>
      </div>

      <div class="interaction-container">
        <div class="author-container">
          <div class="author-me">
            <div class="info" @click="toUser(noteInfo.uid)">
              <img class="avatar-item" style="width: 40px; height: 40px" :src="noteInfo.avatar" />
              <span class="name">{{ noteInfo.username }}</span>
            </div>
            <div class="follow-btn" v-if="currentUid !== noteInfo.uid">
              <el-button
                plain
                type="primary"
                size="large"
                round
                v-if="noteInfo.isFollow"
                @click="follow(noteInfo.uid, 1)"
              >
                {{ $t('common.followed') }}
              </el-button>
              <el-button type="primary" size="large" round v-else @click="follow(noteInfo.uid, 0)">{{
                $t('common.follow')
              }}</el-button>
            </div>
            <div class="follow-btn" v-else>
              <el-dropdown>
                <el-button plain type="primary" size="large" round>
                  {{ $t('common.edit') }}
                  <el-icon class="el-icon--right">
                    <arrow-down />
                  </el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="noteInfo.pinned === '0'" @click="pinned(noteInfo.id, '1')">
                      <el-icon><Top /></el-icon>
                      {{ $t('common.pinned') }}
                    </el-dropdown-item>
                    <el-dropdown-item v-else @click="pinned(noteInfo.id, '0')">
                      <el-icon><Bottom /></el-icon>
                      {{ $t('common.unpin') }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="toEdit()" v-if="noteInfo.id">
                      <el-icon><Edit /></el-icon>
                      {{ $t('common.edit') }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="deleteNote()">
                      <el-icon><Delete /></el-icon>
                      {{ $t('common.delete') }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="note-scroller" ref="noteScroller" @scroll="loadMoreData">
            <div class="note-content">
              <div class="title">{{ noteInfo.title }}</div>
              <div class="desc">
                <span v-html="formatContentWithHashtags(noteInfo.content)"></span>
                <br />
                <a class="tag tag-search" v-for="(item, index) in noteInfo.tagList" :key="index">#{{ item.title }}</a>
              </div>

              <!-- 关联商品展示 -->
              <div class="related-products" v-if="noteInfo.relatedProducts?.length">
                <div class="products-list">
                  <div
                    v-for="product in noteInfo.relatedProducts"
                    :key="product.id"
                    class="product-item"
                    @click="toProduct(product.id)"
                  >
                    <div class="product-card">
                      <img :src="product.cover" :alt="product.title" class="product-img" />
                      <div class="product-info">
                        <div class="product-title">{{ product.title }}</div>
                        <div class="product-price">
                          <span class="current-price">¥{{ product.price }}</span>
                          <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
                        </div>
                      </div>
                      <!-- 添加箭头图标 -->
                      <div class="arrow-icon">
                        <el-icon><ArrowRight /></el-icon>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="bottom-container">
                <span class="date">{{ noteInfo.time }}</span>
                <span class="location" v-if="formatAddress(noteInfo)">
                  <el-icon><Location /></el-icon>
                  <span class="location-text">{{ formatAddress(noteInfo) }}</span>
                </span>
              </div>
            </div>
            <div class="divider interaction-divider"></div>

            <!-- 评论 -->
            <div class="comments-el">
              <Comment
                :nid="props.nid"
                :currentPage="currentPage"
                :replyComment="replyComment"
                :seed="seed"
                @click-comment="clickComment"
              >
              </Comment>
            </div>
          </div>

          <div class="interactions-footer">
            <div class="buttons">
              <div class="left">
                <span class="like-wrapper">
                  <span class="like-lottie" v-if="noteInfo.isCollection" @click="likeOrCollection(3, -1)">
                    <StarFilled style="width: 1em; height: 1em; color: #ffcc00" />
                  </span>
                  <span class="like-lottie" v-else @click="likeOrCollection(3, 1)">
                    <Star style="width: 1em; height: 1em; color: #333" />
                  </span>
                  <span class="count">{{ noteInfo.collectionCount }}</span>
                </span>

                <span class="collect-wrapper">
                  <span class="like-lottie" v-if="noteInfo.isLike" @click="likeOrCollection(1, -1)">
                    <i class="iconfont icon-follow-fill" style="width: 0.8em; height: 0.8em; color: #ff2442"></i>
                  </span>
                  <span class="like-lottie" v-else @click="likeOrCollection(1, 1)">
                    <i class="iconfont icon-follow" style="width: 0.8em; height: 0.8em; color: #333"></i>
                  </span>
                  <span class="count">{{ noteInfo.likeCount }}</span>
                </span>

                <span class="chat-wrapper">
                  <span class="like-lottie">
                    <ChatRound style="width: 0.8em; height: 0.8em; color: #333" />
                  </span>
                  <span class="count">
                    {{ noteInfo.commentCount }}
                  </span>
                </span>
              </div>
              <div class="share-wrapper" @click="shareCurrentNote">
                <Share style="width: 0.8em; height: 0.8em; color: #333" />
              </div>
            </div>
            <div :class="showSaveBtn ? 'comment-wrapper active comment-comp ' : 'comment-wrapper comment-comp '">
              <div class="input-wrapper">
                <input
                  class="comment-input"
                  v-model="commentValue"
                  type="text"
                  :placeholder="commentPlaceVal"
                  @input="commenInput"
                  @keyup.enter="saveComment"
                />
                <div class="input-buttons" @click="clearCommeent" v-show="showSaveBtn">
                  <Close style="width: 1.2em; height: 1.2em" />
                </div>
              </div>
              <button class="submit" @click="saveComment">{{ $t('common.send') }}</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-drawer
      v-model="editDrawerVisible"
      :title="$t('note.edit')"
      size="40%"
      :before-close="handleEditClose"
      destroy-on-close
      @close="onDrawerClose"
    >
      <EditNoteForm
        v-if="editDrawerVisible"
        :note-data="noteInfo"
        :loading="saveLoading"
        @save="handleSaveNote"
        @cancel="handleCancel"
        @change="(val) => (hasUnsavedChanges = val)"
      />
    </el-drawer>

    <div class="close-cricle" @click="close">
      <div class="close close-mask-white">
        <Close style="width: 1.2em; height: 1.2em; color: rgba(51, 51, 51, 0.8)" />
      </div>
    </div>
    <div class="back-desk"></div>
  </div>
</template>

<script lang="ts" setup>
import {
  Top,
  Bottom,
  Edit,
  Delete,
  Close,
  Star,
  ChatRound,
  Share,
  StarFilled,
  ArrowRight,
  ArrowDown,
  Location,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox, ElLoading } from "element-plus";
import { ref, watch, onMounted, onUnmounted } from "vue";
import { getNoteById, pinnedNote } from "@/api/note";
import { addBrowseRecord } from "@/api/browseRecord";
import { reportNoteBehavior } from "@/api/noteBehavior";
import { likeOrCollectionByDTO } from "@/api/likeOrCollection";
import type { NoteInfo } from "@/type/note";
import type { LikeOrCollectionDTO } from "@/type/likeOrCollection";
import { formateTime, getRandomString, getNginxProxyImageUrl, getNginxProxyVideoUrl } from "@/utils/util";
import { followById } from "@/api/follower";
import Comment from "@/components/Comment.vue";
import type { CommentDTO } from "@/type/comment";
import { saveCommentByDTO, syncCommentByIds } from "@/api/comment";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/userStore";
import EditNoteForm from "@/components/EditNoteForm.vue";
import { updateNoteByDTO } from "@/api/note";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

// 修改编辑相关的代码
const editDrawerVisible = ref(false);
const isEditing = ref(false);
const hasUnsavedChanges = ref(false);
const saveLoading = ref(false);

const userStore = useUserStore();
const router = useRouter();

// 这是路由传参
// nid.value = history.state.nid;

const emit = defineEmits(["clickMain"]);

const props = defineProps({
  nid: {
    type: String,
    default: "",
  },
  /** 来源场景：search recommend follow profile other，用于观看来源统计 */
  scene: {
    type: String,
    default: "other",
  },
  nowTime: {
    type: Date,
    default: null,
  },
  editMode: {
    type: Boolean,
    default: false,
  },
});

const currentUid = ref("");
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
const commentValue = ref("");
const commentPlaceVal = ref(t("common.inputContent"));
const commentObject = ref<any>({});
const replyComment = ref<any>({});
const showSaveBtn = ref(false);
const currentPage = ref(1);
const seed = ref("");
const commentIds = ref<Array<string>>([]);
const noteScroller = ref(null);
const isLogin = ref(false);
const likeOrComment = ref({
  isLike: false,
  isComment: false,
});
const isPaused = ref(false);
const video = ref(); // 获取 video 元素的引用
const carouselIndex = ref(0); // 新增：图片轮播的当前索引
const detailOpenTime = ref(0); // 详情打开时间，用于上报观看时长
const videoCompleted = ref(false); // 视频是否已播放完成

/** 解析笔记 urls（字符串 JSON / 数组），空时用 noteCover 兜底 */
const parseNoteUrlsToImgList = (data: any): string[] => {
  const urls = data?.urls;
  const cover = data?.noteCover && String(data.noteCover).trim() ? String(data.noteCover).trim() : "";
  if (urls == null || urls === "") {
    return cover ? [cover] : [];
  }
  if (Array.isArray(urls)) {
    const list = urls.filter(Boolean);
    return list.length > 0 ? list : cover ? [cover] : [];
  }
  if (typeof urls === "string") {
    try {
      const parsed = JSON.parse(urls);
      if (Array.isArray(parsed)) {
        const list = parsed.filter(Boolean);
        return list.length > 0 ? list : cover ? [cover] : [];
      }
    } catch {
      const list = urls
        .split(",")
        .map((s: string) => s.trim())
        .filter(Boolean);
      return list.length > 0 ? list : cover ? [cover] : [];
    }
  }
  return cover ? [cover] : [];
};

watch(
  () => [props.nowTime, props.editMode],
  () => {
    currentPage.value = 1;
    if (props.nid !== null && props.nid !== "") {
      getNoteById(props.nid).then((res: any) => {
        noteInfo.value = res.data;
        noteInfo.value.imgList = parseNoteUrlsToImgList(res.data);
        noteInfo.value.relatedProducts = Array.isArray(res.data.relatedProducts) ? res.data.relatedProducts : [];
        noteInfo.value.time = formateTime(res.data.time);
        likeOrComment.value.isLike = noteInfo.value.isLike;
        // 添加浏览记录与行为上报（仅登录后触发）
        const uid = userStore.getUserInfo()?.id;
        if (uid) {
          addBrowseRecord({ nid: props.nid, uid }).catch(() => {});
          reportNoteBehavior({ eventType: "view", nid: props.nid, scene: props.scene }).catch(() => {});
        }
        detailOpenTime.value = Date.now();
        videoCompleted.value = false;
        // 只有数据加载完再判断 editMode
        if (props.editMode) {
          toEdit();
        }
      });
    }
  },
  { immediate: true }
);

// 只保留键盘事件监听
onMounted(() => {
  document.addEventListener("keydown", handleKeyDown);
});

// 视频暂停
const handlePause = () => {
  isPaused.value = true;
};

// 视频播放
const handlePlay = () => {
  isPaused.value = false;
};

// 视频播放结束（完播）
const handleVideoEnded = () => {
  videoCompleted.value = true;
};

// 点击暂停符号时继续播放
const handlePlayClick = () => {
  video.value.play(); // 播放视频
  isPaused.value = false; // 更新暂停状态
};

// 格式化地址显示（只显示市+详细地址）
const formatAddress = (note: any): string => {
  if (!note) return "";
  const parts = [];
  if (note.city) parts.push(note.city);
  if (note.address) parts.push(note.address);
  return parts.length > 0 ? parts.join(" ") : "";
};

const noLoginNotice = () => {
  if (!isLogin.value) {
    ElMessage.warning(t("common.notLoggedIn"));
    return false;
  }
  return true;
};

const toUser = (uid: string) => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }
  router.push({ name: "user", query: { uid: uid } });
};

// 格式化内容，处理#标签高亮
const formatContentWithHashtags = (content: string) => {
  if (!content) return content;

  // 处理#标签高亮
  const hashtagRegex = /#([^\s#]+)/g;
  const processedContent = content.replace(hashtagRegex, (match, tagName) => {
    return `<a href="/search?q=${encodeURIComponent(
      tagName
    )}" style="color: #1890ff; text-decoration: underline;">${match}</a>`;
  });

  return processedContent;
};

const close = () => {
  const openTime = detailOpenTime.value;
  if (openTime > 0 && props.nid && isLogin.value) {
    const durationSec = Math.floor((Date.now() - openTime) / 1000);
    const isVideo = String(noteInfo.value.noteType) === "2";
    reportNoteBehavior({
      eventType: "watch",
      nid: props.nid,
      durationSec,
      completed: isVideo ? (videoCompleted.value ? 1 : 0) : undefined,
    }).catch(() => {});
  }
  if (isLogin.value) {
    syncCommentByIds(commentIds.value).then(() => {
      commentIds.value = [];
      emit("clickMain", props.nid, likeOrComment.value);
    });
    if (String(noteInfo.value.noteType) === "2" && video.value) {
      video.value.pause();
    }
  } else {
    emit("clickMain");
  }
};

const shareCurrentNote = async () => {
  if (!props.nid || typeof window === "undefined") return;
  const shareUrl = `${window.location.origin}/?nid=${encodeURIComponent(props.nid)}`;
  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(shareUrl);
    } else {
      const input = document.createElement("input");
      input.value = shareUrl;
      document.body.appendChild(input);
      input.select();
      document.execCommand("copy");
      document.body.removeChild(input);
    }
    ElMessage.success(t("common.copySuccess"));
    reportNoteBehavior({ eventType: "share", nid: props.nid, scene: props.scene }).catch(() => {});
  } catch {
    ElMessage.warning(t("common.copyFailed"));
  }
};

const follow = (fid: string, type: number) => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }
  followById(fid).then(() => {
    noteInfo.value.isFollow = type == 0;
  });
};

const likeOrCollection = (type: number, val: number) => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }
  const likeOrCollectionDTO = {} as LikeOrCollectionDTO;
  likeOrCollectionDTO.likeOrCollectionId = noteInfo.value.id;
  likeOrCollectionDTO.publishUid = noteInfo.value.uid;
  likeOrCollectionDTO.type = type == 1 ? 1 : 3;
  likeOrCollectionByDTO(likeOrCollectionDTO).then(() => {
    if (type == 1) {
      noteInfo.value.isLike = val == 1;
      noteInfo.value.likeCount += val;
      likeOrComment.value.isLike = val == 1;
    } else {
      noteInfo.value.isCollection = val == 1;
      noteInfo.value.collectionCount += val;
    }
  });
};

const pinned = (noteId: string, type: string) => {
  pinnedNote(noteId)
    .then((res: any) => {
      if (res.data) {
        noteInfo.value.pinned = type;
        ElMessage.success(type === "1" ? t("common.pinSuccess") : t("common.unpinSuccess"));
      } else {
        ElMessage.error(type === "1" ? t("common.pinFailed") : t("common.unpinFailed"));
      }
    })
    .catch((error) => {
      if (error.response?.data?.message) {
        ElMessage.warning(error.response.data.message);
      } else if (type === "1") {
        ElMessage.warning(t("common.maxPinNotes"));
      } else {
        ElMessage.error(type === "1" ? t("common.pinFailed") : t("common.unpinFailed"));
      }
    });
};

const deleteNote = () => {
  ElMessage.warning(t("common.noPermission"));
  // ElMessageBox.confirm('确定要删除这篇笔记吗?', '提示', {
  //   confirmButtonText: '确定',
  //   cancelButtonText: '取消',
  //   type: 'warning'
  // }).then(() => {
  //   const data = [noteId];
  //   deleteNoteByIds(data).then(() => {
  //     ElMessage.success("删除成功");
  //     emit("clickMain");
  //     // 可以添加路由跳转回列表页
  //     router.push({ name: 'note-list' });
  //   });
  // }).catch(() => {
  //   ElMessage.info('已取消删除');
  // });
};

// 打开编辑抽屉
const toEdit = () => {
  if (!checkEditPermission()) return;
  editDrawerVisible.value = true;
  isEditing.value = true;
};

// 编辑权限检查
const checkEditPermission = () => {
  if (!isLogin.value) {
    ElMessage.warning(t("common.pleaseLoginFirst"));
    return false;
  }
  if (noteInfo.value.uid !== currentUid.value) {
    ElMessage.warning(t("common.noEditPermission"));
    return false;
  }
  return true;
};

// 处理保存
const handleSaveNote = async (formData: FormData) => {
  const loading = ElLoading.service({
    lock: true,
    text: t("common.saving"),
    background: "rgba(0, 0, 0, 0.7)",
  });
  try {
    // 调用API上传数据和文件
    const res = (await updateNoteByDTO(formData)) as any;
    if (res.code === 200) {
      // 更新本地数据
      const updatedNote = res.data;
      Object.assign(noteInfo.value, updatedNote);
      editDrawerVisible.value = false;
      isEditing.value = false;
      hasUnsavedChanges.value = false;
      ElMessage.success(t("note.updateSuccess"));
      // 更新：保存成功后关闭详情页弹窗
      emit("clickMain");
      // 刷新当前页面
      setTimeout(() => {
        router.go(0);
      }, 300); // 延迟一点点，确保弹窗关闭动画能看到
    } else {
      throw new Error(res.data?.message || t("note.updateFailed"));
    }
  } catch (error: any) {
    ElMessage.error(error.message || t("note.updateFailed"));
  } finally {
    loading.close();
  }
};

const handleCancel = () => {
  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm(t("common.unsavedChanges"), {
      confirmButtonText: t("common.confirm"),
      cancelButtonText: t("common.cancel"),
      type: "warning",
    })
      .then(() => {
        editDrawerVisible.value = false;
        hasUnsavedChanges.value = false;
        // 只有未通过状态时才关闭 Main 弹窗
        if (noteInfo.value.auditStatus === "2") {
          setTimeout(() => {
            emit("clickMain");
          }, 200);
        }
      })
      .catch(() => {});
  } else {
    editDrawerVisible.value = false;
    // 只有未通过状态时才关闭 Main 弹窗
    if (noteInfo.value.auditStatus === "2") {
      setTimeout(() => {
        emit("clickMain");
      }, 200);
    }
  }
};

const onDrawerClose = () => {
  // 只有未通过状态时才关闭 Main 弹窗
  if (noteInfo.value.auditStatus === "2") {
    setTimeout(() => {
      emit("clickMain");
    }, 200);
  }
};

// 处理抽屉关闭
const handleEditClose = async (done: () => void) => {
  if (hasUnsavedChanges.value) {
    try {
      await ElMessageBox.confirm(t("common.unsavedClose"), {
        confirmButtonText: t("common.confirm"),
        cancelButtonText: t("common.cancel"),
        type: "warning",
      });
      isEditing.value = false;
      hasUnsavedChanges.value = false;
      done();
    } catch {
      // 用户取消关闭
    }
  } else {
    done();
  }
};

// 键盘快捷键
const handleKeyDown = (e: KeyboardEvent) => {
  if (isEditing.value) {
    // ESC关闭
    if (e.key === "Escape") {
      editDrawerVisible.value = false;
    }
    // Ctrl/Cmd + S 保存
    if ((e.ctrlKey || e.metaKey) && e.key === "s") {
      e.preventDefault();
      const formEl = document.querySelector(".edit-note-form form");
      if (formEl) {
        formEl.dispatchEvent(new Event("submit"));
      }
    }
  }
};

// 商品跳转方法
const toProduct = (productId: string) => {
  router.push({ path: "/idle", query: { id: productId } });
};

const clickComment = (comment: any) => {
  commentObject.value = comment;
  commentPlaceVal.value = t("common.replyTo", { name: comment.username });
};

const commenInput = (e: any) => {
  const { value } = e.target;
  commentValue.value = value;
  showSaveBtn.value = commentValue.value.length > 0 || commentObject.value.pid !== undefined;
};

const saveComment = () => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }
  const comment = {} as CommentDTO;
  comment.nid = props.nid;
  comment.noteUid = noteInfo.value.uid;
  if (commentObject.value.pid === undefined) {
    comment.pid = "0";
    comment.replyId = "0";
    comment.replyUid = noteInfo.value.uid;
    comment.level = 1;
  } else if (commentObject.value.pid == "0") {
    comment.pid = commentObject.value.id;
    comment.replyId = commentObject.value.id;
    comment.replyUid = commentObject.value.uid;
    comment.level = 2;
  } else {
    comment.pid = commentObject.value.pid;
    comment.replyId = commentObject.value.id;
    comment.replyUid = commentObject.value.uid;
    comment.level = 2;
  }

  comment.content = commentValue.value;
  saveCommentByDTO(comment).then((res: any) => {
    replyComment.value = res.data;
    replyComment.value.replyUsername = commentObject.value.username;
    commentValue.value = "";
    commentObject.value = {};
    commentPlaceVal.value = t("common.inputContent");
    showSaveBtn.value = false;
    seed.value = getRandomString(12);
    commentIds.value.push(res.data.id);
    likeOrComment.value.isComment = true;
  });
};

const clearCommeent = () => {
  commentValue.value = "";
  commentObject.value = {};
  commentPlaceVal.value = t("common.inputContent");
  showSaveBtn.value = false;
};

const loadMoreData = () => {
  const container = noteScroller.value as any;
  if (container.scrollTop + container.clientHeight >= container.scrollHeight) {
    currentPage.value += 1;
    console.log("到底了");
  }
};

const initData = () => {
  isLogin.value = userStore.isLogin();
  if (isLogin.value) {
    currentUid.value = userStore.getUserInfo().id;
  }
};

initData();

onMounted(() => {
  document.addEventListener("keydown", handleKeyDown);
});

onUnmounted(() => {
  document.removeEventListener("keydown", handleKeyDown);
});

const handleCarouselChange = (newIndex: number) => {
  carouselIndex.value = newIndex;
};
</script>

<style lang="less" scoped>
.related-products {
  margin: 12px 0;

  .products-list {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .product-item {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateX(4px);

        .product-card {
          background-color: #f8f8f8;
        }
      }

      .product-card {
        display: flex;
        padding: 5px;
        border-radius: 8px;
        background-color: #f5f5f5;
        transition: all 0.3s;
        align-items: center; // 确保垂直居中

        .product-img {
          width: 30px;
          height: 35px;
          border-radius: 4px;
          object-fit: cover;
        }

        .product-info {
          flex: 1;
          margin-left: 12px;
          margin-right: 8px; // 为箭头留出空间
          display: flex;
          flex-direction: column;
          justify-content: space-between;

          .product-title {
            font-size: 14px;
            color: #333;
            line-height: 1.4;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
          }

          .product-price {
            display: flex;
            align-items: baseline;
            gap: 8px;

            .current-price {
              font-size: 16px;
              font-weight: 500;
              color: #ff2442;
            }

            .original-price {
              font-size: 12px;
              color: #999;
              text-decoration: line-through;
            }
          }
        }
        // 添加箭头图标样式
        .arrow-icon {
          color: #999;
          display: flex;
          align-items: center;
          font-size: 16px;
          padding-right: 4px;
          transition: transform 0.3s;
        }

        &:hover {
          .arrow-icon {
            color: #666;
            transform: translateX(4px);
          }
        }
      }
    }
  }
}

// 调整底部容器样式
.bottom-container {
  margin-top: 12px;
}

:deep(.el-dropdown-menu__item:not(.is-disabled):focus) {
  background-color: #f8f8f8;
  color: black;
}

.note-detail-mask {
  position: fixed;
  left: 0;
  top: 0;
  display: flex;
  width: 100vw;
  height: 100vh;
  z-index: 20;
  overflow: auto;

  .back-desk {
    position: fixed;
    background-color: #f4f4f4;
    opacity: 0.5;
    width: 100vw;
    height: 100vh;
    z-index: 30;
  }

  .close-cricle {
    left: 1.3vw;
    top: 1.3vw;
    position: fixed;
    display: flex;
    z-index: 100;
    cursor: pointer;

    .close-mask-white {
      box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.04), 0 1px 2px 0 rgba(0, 0, 0, 0.02);
      border: 1px solid rgba(0, 0, 0, 0.08);
    }

    .close {
      display: flex;
      justify-content: center;
      align-items: center;
      border-radius: 100%;
      width: 40px;
      height: 40px;
      border-radius: 40px;
      cursor: pointer;
      transition: all 0.3s;
      background-color: #fff;
    }
    :hover {
      cursor: pointer; /* 显示小手指针 */
      transform: scale(1.2); /* 鼠标移入时按钮稍微放大 */
    }
  }

  .note-container {
    width: 65%;
    height: 86%;
    transition: transform 0.4s ease 0s, width 0.4s ease 0s;
    transform: translate(350px, 60px) scale(1);
    overflow: visible;

    display: flex;
    box-shadow: 0 8px 64px 0 rgba(0, 0, 0, 0.04), 0 1px 4px 0 rgba(0, 0, 0, 0.02);
    border-radius: 20px;
    background: #f8f8f8;
    transform-origin: left top;
    z-index: 100;

    .media-container {
      width: 65%;
      height: auto;

      position: relative;
      background: rgba(0, 0, 0, 0.03);
      flex-shrink: 0;
      flex-grow: 0;
      -webkit-user-select: none;
      user-select: none;
      overflow: hidden;
      border-radius: 20px 0 0 20px;
      transform: translateZ(0);
      height: 100%;
      object-fit: contain;
      min-width: 440px;

      .video-player {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%; /* 让容器宽度为 100% */
        height: 100%; /* 让容器高度占满整个视口 */
      }
      .pause-overlay {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        color: white;
        padding: 10px;
      }
    }

    .interaction-container {
      width: 35%;
      flex-shrink: 0;
      border-radius: 0 20px 20px 0;
      position: relative;
      display: flex;
      flex-direction: column;
      flex-grow: 1;
      height: 100%;
      background-color: #fff;
      overflow: hidden;
      border-left: 1px solid rgba(0, 0, 0, 0.08);

      .author-me {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        padding: 24px;
        border-bottom: 1px solid transparent;

        .info {
          display: flex;
          align-items: center;

          .avatar-item {
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            border-radius: 100%;
            border: 1px solid rgba(0, 0, 0, 0.08);
            object-fit: cover;
          }

          .name {
            padding-left: 12px;
            height: 40px;
            display: flex;
            align-items: center;
            font-size: 16px;
            color: rgba(51, 51, 51, 0.8);
          }
        }
      }

      .note-scroller::-webkit-scrollbar {
        display: none;
      }

      .note-scroller {
        transition: scroll 0.4s;
        overflow-y: scroll;
        flex-grow: 1;
        height: 80vh;

        .note-content {
          padding: 0 24px 24px;
          color: var(--color-primary-label);

          .title {
            margin-bottom: 8px;
            font-weight: 600;
            font-size: 18px;
            line-height: 140%;
          }

          .desc {
            margin: 0;
            font-weight: 400;
            font-size: 14px;
            line-height: 150%;
            color: #333;
            white-space: pre-wrap;
            overflow-wrap: break-word;

            .tag-search {
              cursor: pointer;
            }

            .tag {
              margin-right: 2px;
              color: #1890ff;
              text-decoration: underline;
            }
          }

          .bottom-container {
            display: flex;
            gap: 10px;
            align-items: center;
            margin-top: 12px;

            .date {
              font-size: 14px;
              line-height: 120%;
              color: rgba(51, 51, 51, 0.6);
            }

            .location {
              display: flex;
              align-items: center;
              gap: 4px;
              font-size: 14px;
              line-height: 120%;
              color: #1890ff;
              background: none;
              padding: 0;
              border-radius: 0;
              border: none;
              font-weight: 500;

              .el-icon {
                font-size: 14px;
                color: #1890ff;
              }

              .location-text {
                color: #1890ff;
                font-weight: 500;
              }
            }
          }
        }

        .interaction-divider {
          margin: 0 24px;
        }

        .divider {
          margin: 4px 8px;
          list-style: none;
          height: 0;
          border: solid rgba(0, 0, 0, 0.08);
          border-width: 1px 0 0;
        }

        .comments-el {
          position: relative;
        }
      }

      .interactions-footer {
        position: absolute;
        bottom: 0px;
        background: #fff;
        flex-shrink: 0;
        padding: 12px 24px 24px;
        height: 130px;
        border-top: 1px solid rgba(0, 0, 0, 0.08);
        flex-basis: 130px;
        z-index: 1;

        .buttons {
          display: flex;
          justify-content: space-between;

          .count {
            margin-left: 6px;
            margin-right: 12px;
            font-weight: 500;
            font-size: 12px;
          }

          .left {
            display: flex;

            .like-wrapper {
              position: relative;
              cursor: pointer;
              display: flex;
              justify-content: left;
              color: rgba(51, 51, 51, 0.6);
              margin-right: 5px;
              align-items: center;

              .like-lottie {
                transform: scale(1.7);
              }
            }

            .collect-wrapper {
              position: relative;
              cursor: pointer;
              display: flex;
              color: rgba(51, 51, 51, 0.6);
              margin-right: 5px;
              align-items: center;

              .like-lottie {
                transform: scale(1.7);
              }
            }
            :hover {
              cursor: pointer; /* 显示小手指针 */
              transform: scale(1.15); /* 鼠标移入时按钮稍微放大 */
            }

            .chat-wrapper {
              cursor: pointer;
              color: rgba(51, 51, 51, 0.6);
              display: flex;
              align-items: center;

              .like-lottie {
                transform: scale(1.7);
              }
            }
          }
        }

        .comment-wrapper {
          &.active {
            .input-wrapper {
              flex-shrink: 1;
            }
          }
        }

        .comment-wrapper {
          display: flex;
          font-size: 16px;
          overflow: hidden;

          .input-wrapper {
            display: flex;
            position: relative;
            width: 100%;
            flex-shrink: 0;
            transition: flex 0.3s;

            .comment-input:placeholder-shown {
              background-image: none;
              padding: 12px 92px 12px 36px;
              background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAMAAABg3Am1AAAANlBMVEUAAAA0NDQyMjIzMzM2NjY2NjYyMjI0NDQ1NTU1NTUzMzM1NTU1NTUzMzM1NTUzMzM1NTU1NTVl84gVAAAAEnRSTlMAmUyGEzlgc2AmfRx9aToKQzCSoXt+AAAAhElEQVRIx+3Uuw6DMAyF4XOcBOdCafv+L9vQkQFyJBak/JOHT7K8GLM7epuHusRhHwP/mejJ77i32CpZh33aD+lDFDzgZFE8+tgUv5BB9NxEb9NPL3i46JvoUUhXPBKZFQ/rTPHI3ZXt8xr12KX055LoAVtXz9kKHprxNMMxXqRvmAn9ACQ7A/tTXYAxAAAAAElFTkSuQmCC);
              background-repeat: no-repeat;
              background-size: 16px 16px;
              background-position: 16px 12px;
              color: rgba(51, 51, 51, 0.3);
            }

            .comment-input {
              padding: 12px 92px 12px 16px;
              width: 100%;
              height: 40px;
              line-height: 16px;
              background: rgba(0, 0, 0, 0.03);
              caret-color: rgba(51, 51, 51, 0.3);
              border-radius: 22px;
              border: none;
              outline: none;
              resize: none;
              color: #333;
            }

            .input-buttons {
              position: absolute;
              right: 0;
              top: 0;
              height: 40px;
              display: flex;
              align-items: center;
              justify-content: center;
              width: 92px;
              color: rgba(51, 51, 51, 0.3);
            }
          }

          .submit {
            margin-left: 8px;
            width: 60px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
            font-weight: 600;
            cursor: pointer;
            flex-shrink: 0;
            background: #3d8af5;
            border-radius: 44px;
            font-size: 16px;
          }
        }

        .comment-comp {
          margin-top: 20px;
        }
      }
    }
  }
}

// 暗色模式下的地点样式
@media (prefers-color-scheme: dark) {
  .note-detail-mask {
    .note-container {
      .note-scroller {
        .note-content {
          .bottom-container {
            .location {
              background: none;
              border: none;
              color: #64b5f6;

              .el-icon {
                color: #64b5f6;
              }

              .location-text {
                color: #64b5f6;
              }
            }
          }
        }
      }
    }
  }
}

// 手动暗色模式类
.dark {
  .note-detail-mask {
    .note-container {
      .note-scroller {
        .note-content {
          .bottom-container {
            .location {
              background: none;
              border: none;
              color: #64b5f6;

              .el-icon {
                color: #64b5f6;
              }

              .location-text {
                color: #64b5f6;
              }
            }
          }
        }
      }
    }
  }
}

.img-counter-badge {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 10;
  background: rgba(51, 51, 51, 0.75);
  color: #fff;
  border-radius: 999px;
  font-size: 16px;
  min-width: 40px;
  text-align: center;
  padding: 7px 15px;
  user-select: none;
}

// 移动端响应式样式
@media screen and (max-width: 695px) {
  .note-detail-mask {
    background-color: #fff; // 去掉 !important，让暗色模式样式可以覆盖
    z-index: 9999 !important;
    position: fixed !important;
    left: 0 !important;
    right: 0 !important;
    top: 0 !important;
    bottom: 0 !important;

    .note-container {
      width: 100vw !important;
      max-width: 100vw !important;
      height: 100vh !important;
      max-height: 100vh !important;
      transform: translate(0, 0) scale(1) !important;
      border-radius: 0 !important;
      flex-direction: column !important;
      background: #fff; // 去掉 !important
      box-shadow: none !important;
      display: flex !important;
      overflow-y: auto !important;
      overflow-x: hidden !important;
      padding: 0 !important;
      margin: 0 !important;
      position: fixed !important;
      left: 0 !important;
      right: 0 !important;
      top: 0 !important;
      bottom: 0 !important;
      -webkit-overflow-scrolling: touch !important;

      // 1. 图片/视频区域（在顶部栏下方）
      .media-container {
        width: 100% !important;
        min-width: 0 !important;
        max-width: 100% !important;
        height: 40vh !important;
        max-height: 400px !important;
        flex-shrink: 0 !important;
        background: #f5f5f5; // 去掉 !important
        margin: 0 !important;
        margin-top: 60px !important;
        padding: 0 !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        overflow: visible !important;
        position: relative !important;

        :deep(.el-carousel) {
          height: 100% !important;
          width: 100% !important;
          margin: 0 !important;
          padding: 0 !important;
        }

        :deep(.el-carousel__container) {
          height: 100% !important;
          width: 100% !important;
          left: 0 !important;
          right: 0 !important;
          transform: none !important;
          margin: 0 !important;
          padding: 0 !important;
        }

        :deep(.el-carousel__item) {
          height: 100% !important;
          width: 100% !important;
          background: #f5f5f5; // 去掉 !important
          padding: 0 !important;
          margin: 0 !important;
          display: flex !important;
          align-items: center !important;
          justify-content: center !important;
        }

        :deep(.el-carousel__arrow) {
          display: flex !important;
          background: rgba(31, 45, 61, 0.5) !important;
          width: 36px !important;
          height: 36px !important;
        }

        :deep(.el-image) {
          width: 100% !important;
          height: 100% !important;
          display: flex !important;
          align-items: center !important;
          justify-content: center !important;
        }

        :deep(img) {
          max-width: 100% !important;
          max-height: 100% !important;
          width: auto !important;
          height: auto !important;
          object-fit: contain !important;
          margin-left: 0 !important;
          margin-right: 0 !important;
        }

        .img-counter-badge {
          display: block !important;
          position: absolute !important;
          top: 14px !important;
          right: 14px !important;
          z-index: 10 !important;
          background: rgba(51, 51, 51, 0.75) !important;
          color: #fff !important;
          border-radius: 999px !important;
          font-size: 14px !important;
          padding: 4px 12px !important;
        }
      }

      // 2. 顶部栏：关闭、头像、用户名、关注
      .interaction-container {
        width: 100% !important;
        height: 100% !important;
        display: flex !important;
        flex-direction: column !important;
        border: none !important;
        overflow: hidden !important;
        padding: 0 !important;
        margin: 0 !important;

        .author-container {
          width: 100% !important;
          flex: 1 !important;
          display: flex !important;
          flex-direction: column !important;
          overflow: visible !important;
          padding: 0 !important;
          margin: 0 !important;
        }

        .author-me {
          width: 100% !important;
          height: 60px !important;
          flex-shrink: 0 !important;
          padding: 0 8px 0 16px !important;
          border: none !important;
          border-bottom: 1px solid rgba(0, 0, 0, 0.06) !important;
          display: flex !important;
          align-items: center !important;
          justify-content: space-between !important;
          overflow: visible !important;
          position: fixed !important;
          top: 0 !important;
          left: 0 !important;
          right: 0 !important;
          z-index: 100 !important;
          background: #fff; // 去掉 !important
          border-bottom: 1px solid rgba(0, 0, 0, 0.06) !important;

          .info {
            display: flex !important;
            align-items: center !important;
            flex-shrink: 1 !important;
            min-width: 0 !important;
            margin-left: 48px !important;

            .avatar-item {
              width: 36px !important;
              height: 36px !important;
              flex-shrink: 0 !important;
            }

            .name {
              font-size: 15px !important;
              font-weight: 600 !important;
              color: #333; // 去掉 !important
              margin-left: 10px !important;
              padding: 0 !important;
              height: auto !important;
              white-space: nowrap !important;
              overflow: hidden !important;
              text-overflow: ellipsis !important;
            }
          }

          .follow-btn {
            flex-shrink: 0 !important;

            :deep(.el-button) {
              padding: 6px 16px !important;
              height: 32px !important;
              font-size: 13px !important;
            }

            :deep(.el-dropdown) {
              display: none !important;
            }
          }
        }

        // 3. 内容区域（笔记内容、评论）
        .note-scroller {
          flex: 1 !important;
          display: flex !important;
          flex-direction: column !important;
          overflow: visible !important;
          background: #fff; // 去掉 !important
          padding: 0 !important;
          margin: 0 !important;

          // 笔记内容
          .note-content {
            padding: 8px 16px 16px 16px !important;
            background: #fff; // 去掉 !important
            display: block !important;
            width: 100% !important;

            .title {
              font-size: 18px !important;
              font-weight: 600 !important;
              line-height: 1.5 !important;
              margin-bottom: 12px !important;
              color: #333; // 去掉 !important
            }

            .desc {
              font-size: 15px !important;
              line-height: 1.7 !important;
              color: #333; // 去掉 !important
              margin-bottom: 12px !important;
              word-break: break-word !important;
            }

            .tag-search {
              display: inline-block !important;
              font-size: 14px !important;
              color: #1890ff !important;
              margin-right: 8px !important;
              margin-bottom: 8px !important;
            }

            .bottom-container {
              display: flex !important;
              align-items: flex-start !important;
              gap: 12px !important;
              font-size: 13px !important;
              color: #999 !important;
              margin-top: 12px !important;
              padding-top: 0 !important;
              border-top: none !important;
              flex-wrap: wrap !important;

              .date {
                flex-shrink: 0 !important;
                white-space: nowrap !important;
              }

              .location {
                display: flex !important;
                align-items: center !important;
                gap: 4px !important;
                flex: 1 !important;
                min-width: 0 !important;
                background: none !important;
                border: none !important;
                padding: 0 !important;
                border-radius: 0 !important;

                .el-icon {
                  flex-shrink: 0 !important;
                  font-size: 13px !important;
                  color: #1890ff !important;
                }

                .location-text {
                  overflow: hidden !important;
                  text-overflow: ellipsis !important;
                  white-space: nowrap !important;
                  color: #1890ff !important;
                  font-weight: normal !important;
                }
              }
            }
          }

          .divider {
            height: 1px !important;
            margin: 0 16px !important;
            margin-top: 16px !important;
            margin-bottom: 16px !important;
            border-top: 1px solid #f0f0f0 !important;
            display: block !important;
          }

          .comments-el {
            padding: 0 16px 20px 0 !important;
            background: #fff; // 去掉 !important
            display: block !important;
            visibility: visible !important;
            min-height: 100px !important;
          }
        }

        // 4. 底部栏
        .interactions-footer {
          position: fixed !important;
          bottom: 60px !important;
          left: 0 !important;
          right: 0 !important;
          width: 100% !important;
          background: #fff; // 去掉 !important
          border-top: 1px solid rgba(0, 0, 0, 0.08) !important;
          padding: 10px 16px !important;
          min-height: 48px !important;
          max-height: 48px !important;
          height: 48px !important;
          z-index: 100 !important;
          box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08) !important;
          display: flex !important;
          align-items: center !important;
          gap: 10px !important;

          // 当有输入内容时，隐藏右侧按钮
          &:has(.comment-wrapper.active) .buttons {
            display: none !important;
          }

          .comment-wrapper {
            order: 1 !important;
            flex: 1 !important;
            display: flex !important;
            align-items: center !important;
            gap: 10px !important;
            margin: 0 !important;
            margin-right: 12px !important;

            .input-wrapper {
              flex: 1 !important;
              display: flex !important;
              align-items: center !important;
              gap: 6px !important;
              background: #f5f5f5; // 去掉 !important
              border-radius: 16px !important;
              padding: 5px 12px !important;
              height: 28px !important;
              min-height: 28px !important;
              max-height: 28px !important;
              position: relative !important;

              .comment-input {
                flex: 1 !important;
                height: 20px !important;
                line-height: 20px !important;
                font-size: 13px !important;
                padding: 0 !important;
                padding-right: 24px !important;
                border: none !important;
                background: transparent !important;
                outline: none !important;
                color: #333; // 去掉 !important
              }

              .input-buttons {
                position: absolute !important;
                right: 8px !important;
                top: 50% !important;
                transform: translateY(-50%) !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                color: #999 !important;
                cursor: pointer !important;
                font-size: 16px !important;
                width: 20px !important;
                height: 20px !important;

                &[style*="display: none"] {
                  display: none !important;
                }
              }
            }

            .submit {
              display: none;
              height: 28px !important;
              padding: 0 16px !important;
              font-size: 13px !important;
              border-radius: 14px !important;
              background: #007aff !important;
              color: #fff !important;
              border: none !important;
              cursor: pointer !important;
              white-space: nowrap !important;
              flex-shrink: 0 !important;
            }
          }

          // 有输入内容时显示发送按钮
          .comment-wrapper.active {
            .submit {
              display: block !important;
            }

            .input-wrapper {
              .comment-input {
                padding-right: 8px !important;
              }

              .input-buttons {
                display: none !important;
              }
            }
          }

          .buttons {
            order: 2 !important;
            display: flex !important;
            align-items: center !important;
            gap: 20px !important;
            margin-left: 8px !important;

            .left {
              display: flex !important;
              align-items: center !important;
              gap: 20px !important;
            }

            .like-wrapper,
            .collect-wrapper,
            .chat-wrapper {
              display: flex !important;
              align-items: center !important;
              gap: 4px !important;
              cursor: pointer !important;

              .like-lottie {
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                font-size: 20px !important;
                line-height: 1 !important;
              }

              .count {
                font-size: 14px !important;
                color: #333 !important;
                line-height: 1 !important;
                padding-top: 1px !important;
              }
            }

            .share-wrapper {
              display: flex !important;
              align-items: center !important;
              cursor: pointer !important;
            }
          }
        }
      }
    }

    // 关闭按钮（左上角）
    .close-cricle {
      position: fixed !important;
      top: 12px !important;
      left: 8px !important;
      z-index: 200 !important;

      .close {
        width: 32px !important;
        height: 32px !important;
        background: rgba(255, 255, 255, 0.9) !important;
        border-radius: 50% !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;

        :deep(.el-icon) {
          color: #333 !important;
          font-size: 20px !important;
        }
      }
    }
  }

  // 图片计数
  .img-counter-badge {
    position: absolute !important;
    top: 14px !important;
    right: 14px !important;
    z-index: 10 !important;
  }

  .back-desk {
    display: none !important;
  }
}
</style>

<style>
/* 移动端暗色模式 - 使用超高优先级选择器 */
@media screen and (max-width: 695px) {
  html.dark-mode.dark-mode .note-detail-mask {
    background-color: #1a1a1a !important;
  }

  html.dark-mode.dark-mode .note-container {
    background: #1a1a1a !important;
  }

  html.dark-mode.dark-mode .media-container {
    background: #000 !important;
  }

  html.dark-mode.dark-mode .el-carousel__item {
    background: #000 !important;
  }

  html.dark-mode.dark-mode .author-me {
    background: #1a1a1a !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
  }

  html.dark-mode.dark-mode .author-me .info .name {
    color: #e0e0e0 !important;
  }

  html.dark-mode.dark-mode .note-scroller {
    background: #1a1a1a !important;
  }

  html.dark-mode.dark-mode .note-content {
    background: #1a1a1a !important;
  }

  html.dark-mode.dark-mode .note-content .title {
    color: #e0e0e0 !important;
  }

  html.dark-mode.dark-mode .note-content .desc {
    color: #b0b0b0 !important;
  }

  html.dark-mode.dark-mode .comments-el {
    background: #1a1a1a !important;
  }

  html.dark-mode.dark-mode .interactions-footer {
    background: #1a1a1a !important;
    border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
  }

  html.dark-mode.dark-mode .comment-wrapper .input-wrapper {
    background: #2a2a2a !important;
  }

  html.dark-mode.dark-mode .comment-input {
    color: #e0e0e0 !important;
  }
}
</style>
