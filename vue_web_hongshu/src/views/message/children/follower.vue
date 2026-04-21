<template>
  <div>
    <ul class="agree-container" v-infinite-scroll="loadMore">
      <li class="agree-item" v-for="(item, index) in dataList" :key="index">
        <a class="user-avatar">
          <!-- https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png -->
          <img class="avatar-item" :src="item.avatar" @click="toUser(item.uid)" />
        </a>
        <div class="main">
          <div class="info">
            <div class="user-info">
              <a class>{{ item.username }}</a>
            </div>
            <div class="interaction-hint">
              <span>{{ $t("message.startedFollowing") }}</span>
              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              <span>{{ item.time }}</span>
            </div>
          </div>
          <div class="extra">
            <el-button plain type="primary" round size="large" v-if="item.isFollow" @click="follow(item.uid, index, 1)">
              {{ $t("common.mutualFollow") }}
            </el-button>
            <el-button type="primary" round size="large" v-else @click="follow(item.uid, index, -1)">
              {{ $t("common.followBack") }}
            </el-button>
          </div>
        </div>
      </li>
    </ul>
  </div>

  <div v-if="dataList.length === 0" class="empty-state">
    <el-empty :description="$t('common.noMessage')"> </el-empty>
  </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { getNoticeFollower } from "@/api/follower";
import { formateTime } from "@/utils/util";
import { followById } from "@/api/follower";
import { useRouter } from "vue-router";

const { t } = useI18n();
void t("common.confirm");
const router = useRouter();
const currentPage = ref(1);
const pageSize = 10;
const dataList = ref<Array<any>>([]);
const dataTotal = ref(0);

const getPageData = () => {
  getNoticeFollower(currentPage.value, pageSize).then((res) => {
    const { records, total } = res.data;
    dataTotal.value = total;
    records.forEach((item: any) => {
      item.time = formateTime(item.time);
      dataList.value.push(item);
    });
  });
};

const follow = (fid: string, index: number, type: number) => {
  followById(fid).then(() => {
    dataList.value[index].isFollow = type == -1;
  });
};

const loadMore = () => {
  currentPage.value += 1;
  getPageData();
};

const toUser = (uid: string) => {
  router.push({ name: "user", query: { uid: uid } });
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
    padding-top: 16px;
    padding-left: 0;
    padding-right: 0;
    
    // 移动端优化
    @media screen and (max-width: 695px) {
      padding-top: 12px;
      padding-left: 0;
      padding-right: 0;
    }

    .user-avatar {
      margin-right: 12px;
      flex-shrink: 0;
      
      // 移动端优化
      @media screen and (max-width: 695px) {
        margin-right: 10px;
      }

      .avatar-item {
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        border-radius: 100%;
        border: 1px solid rgba(0, 0, 0, 0.08);
        object-fit: cover;
        
        // 移动端优化
        @media screen and (max-width: 695px) {
          width: 36px;
          height: 36px;
        }
      }
    }

    .main {
      flex-grow: 1;
      flex-shrink: 1;
      display: flex;
      flex-direction: row;
      padding-bottom: 10px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.08);
      
      // 移动端优化
      @media screen and (max-width: 695px) {
        padding-bottom: 8px;
      }

      .info {
        flex-grow: 1;
        flex-shrink: 1;

        .user-info {
          display: flex;
          flex-direction: row;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 2px;

          a {
            color: #333;
            font-size: 14px;
            font-weight: 600;
            
            // 移动端优化
            @media screen and (max-width: 695px) {
              font-size: 14px;
            }
          }
        }

        .interaction-hint {
          font-size: 13px;
          color: rgba(51, 51, 51, 0.6);
          margin-bottom: 4px;
          flex-wrap: wrap;
          display: flex;
          align-items: center;
          gap: 4px;
          
          // 移动端优化
          @media screen and (max-width: 695px) {
            font-size: 12px;
            gap: 3px;
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
        flex-shrink: 0;
        margin-left: 12px;
        display: flex;
        align-items: flex-start;
        
        // 移动端优化
        @media screen and (max-width: 695px) {
          margin-left: 8px;
        }

        :deep(.el-button) {
          padding: 6px 16px;
          font-size: 13px;
          height: 32px;
          
          // 移动端优化按钮大小
          @media screen and (max-width: 695px) {
            padding: 5px 14px;
            font-size: 12px;
            height: 28px;
          }
        }

        .follow-button {
          width: auto;
          min-width: 88px;
          
          @media screen and (max-width: 695px) {
            width: auto;
            min-width: 72px;
          }
        }

        .reds-button-new.large {
          font-size: 13px;
          font-weight: 600;
          line-height: 16px;
          padding: 0 16px;
          height: 32px;
          
          @media screen and (max-width: 695px) {
            font-size: 12px;
            padding: 0 14px;
            height: 28px;
          }
        }

        .reds-button-new.primary {
          background-color: #ff2e4d;
          color: #fff;
        }

        .reds-button-new {
          position: relative;
          cursor: pointer;
          -webkit-user-select: none;
          user-select: none;
          white-space: nowrap;
          outline: none;
          background: none;
          border: none;
          vertical-align: middle;
          text-align: center;
          display: inline-block;
          padding: 0;
          border-radius: 100px;
          font-weight: 500;
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
