<template>
  <div>
    <ul class="agree-container" v-infinite-scroll="loadMore">
      <li class="agree-item" v-for="(item, index) in dataList" :key="index">
        <a class="user-avatar">
          <img class="avatar-item" :src="item.avatar" @click="toUser(item.uid)" />
        </a>
        <div class="main">
          <div class="info">
            <div class="user-info">
              <a class>{{ item.username }}</a>
            </div>
            <div class="interaction-hint">
              <span v-if="item.type == 1">
                {{ $t("message.likedYour") }}
                <span
                  class="type-tag"
                  :class="item.itemType === 2 ? 'product' : 'note'"
                >
                  {{ item.itemType === 2 ? $t("message.productType") : $t("message.noteType") }}
                </span>
              </span>
              <span v-if="item.type == 2">{{ $t("message.likedYourComment") }}</span>
              <span v-if="item.type == 3">
                {{ $t("message.collectedYour") }}
                <span class="type-tag" 
                :class="item.itemType === 2 ? 'product' : 'note'"
                >
                  {{ item.itemType === 2 ? $t("message.productType") : $t("message.noteType") }}
                </span>
              </span>
              <span v-if="item.type == 4">{{ $t("message.likedYourAlbum", { content: item.content }) }}</span>
              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              <span>{{ item.time }}</span>
            </div>
            <div class="quote-info" v-if="item.type == 2">{{ item.content }}</div>
          </div>
          <div class="extra" @click="toPage(item)">
            <img class="extra-image" :src="item.itemCover" />
          </div>
        </div>
      </li>
    </ul>
  </div>

  <div v-if="dataList.length === 0" class="empty-state">
    <el-empty :description="$t('common.noMessage')">
    </el-empty>
  </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { getNoticeLikeOrCollection } from "@/api/likeOrCollection";
import { formateTime } from "@/utils/util";
import { useRouter } from "vue-router";

const { t } = useI18n();
void t("common.confirm");
const router = useRouter();
const currentPage = ref(1);
const pageSize = 10;
const dataList = ref<Array<any>>([]);
const dataTotal = ref(0);

const emit = defineEmits(["clickMain"]);

const toPage = (item: any) => {
  // 根据 itemType 判断是笔记还是商品
  const type = item.itemType === 2 ? 'product' : 'note';
  emit("clickMain", { type, id: item.itemId });
};

const toUser = (uid: string) => {
  router.push({ name: "user", query: { uid: uid } });
};

const getPageData = () => {
  getNoticeLikeOrCollection(currentPage.value, pageSize).then((res) => {
    const { records, total } = res.data;
    dataTotal.value = total;
    records.forEach((item: any) => {
      item.time = formateTime(item.time);
      dataList.value.push(item);
    });
  });
};

const loadMore = () => {
  currentPage.value += 1;
  getPageData();
};

const initData = () => {
  getPageData();
};
initData();
</script>

<style lang="less" scoped>
textarea {
  overflow: auto;
}

.agree-container {
  width: 100%;
  max-width: 40rem;
  height: 90vh;
  padding: 0;
  margin: 0;
  
  // 移动端适配
  @media screen and (max-width: 695px) {
    max-width: 100%;
    padding: 0;
  }

  .agree-item {
    display: flex;
    flex-direction: row;
    padding-top: 24px;
    padding-left: 0;
    padding-right: 0;
    
    // 移动端优化
    @media screen and (max-width: 695px) {
      padding-top: 16px;
      padding-left: 0;
      padding-right: 0;
    }

    .user-avatar {
      margin-right: 24px;
      flex-shrink: 0;
      
      // 移动端优化
      @media screen and (max-width: 695px) {
        margin-right: 12px;
      }

      .avatar-item {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        border-radius: 100%;
        border: 1px solid rgba(0, 0, 0, 0.08);
        object-fit: cover;
        
        // 移动端优化
        @media screen and (max-width: 695px) {
          width: 40px;
          height: 40px;
        }
      }
    }

    .main {
      flex-grow: 1;
      flex-shrink: 1;
      display: flex;
      flex-direction: row;
      padding-bottom: 12px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.08);

      .info {
        flex-grow: 1;
        flex-shrink: 1;

        .user-info {
          display: flex;
          flex-direction: row;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 4px;

          a {
            color: #333;
            font-size: 16px;
            font-weight: 600;
            
            // 移动端优化
            @media screen and (max-width: 695px) {
              font-size: 14px;
            }
          }
        }

        .interaction-hint {
          font-size: 14px;
          color: rgba(51, 51, 51, 0.6);
          margin-bottom: 8px;
          flex-wrap: wrap;
          display: flex;
          align-items: center;
          gap: 5px;
          
          // 移动端优化
          @media screen and (max-width: 695px) {
            font-size: 13px;
            gap: 4px;
          }
          
          .type-tag {
            font-size: 12px;
            padding: 0 5px;
            height: 18px;
            line-height: 18px;
            border-radius: 10px;
            display: inline-flex;
            align-items: center;
            
            &.note {
              color: #3d8af5;
              background: rgba(61, 138, 245, 0.1);
            }
            
            &.product {
              color: #67c23a;
              background: rgba(103, 194, 58, 0.1);
            }
          }

          .ml-2 {
            margin-left: 8px;
          }
          
          .el-tag {
            height: 20px;
            line-height: 20px;
            padding: 0 6px;
            font-size: 12px;
          }
        }

        .interaction-content {
          display: flex;
          font-size: 14px;
          color: #333;
          line-height: 140%;
          cursor: pointer;
          margin-bottom: 12px;

          .msg-count {
            width: 20px;
            height: 20px;
            line-height: 20px;
            font-size: 13px;
            color: #fff;
            background-color: red;
            text-align: center;
            border-radius: 100%;
          }
        }

        .quote-info {
          font-size: 12px;
          display: flex;
          align-items: center;
          color: rgba(51, 51, 51, 0.6);
          margin-bottom: 12px;
          cursor: pointer;
          word-break: break-word;
          
          // 移动端优化
          @media screen and (max-width: 695px) {
            font-size: 12px;
            margin-bottom: 8px;
          }
        }

        .quote-info::before {
          content: "";
          display: inline-block;
          border-radius: 8px;
          margin-right: 6px;
          width: 4px;
          height: 17px;
          background: rgba(0, 0, 0, 0.08);
        }
      }

      .extra {
        min-width: 48px;
        flex-shrink: 0;
        margin-left: 24px;
        
        // 移动端优化
        @media screen and (max-width: 695px) {
          min-width: 60px;
          margin-left: 12px;
        }

        .extra-image {
          cursor: pointer;
          width: 48px;
          height: 48px;
          border: 1px solid rgba(0, 0, 0, 0.02);
          border-radius: 6px;
          object-fit: cover;
          
          // 移动端优化
          @media screen and (max-width: 695px) {
            width: 60px;
            height: 60px;
            border-radius: 4px;
          }
        }
      }
    }
  }
}

.empty-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  text-align: center;

  :deep(.el-empty) {
    padding: 40px 0;
  }
}
</style>
