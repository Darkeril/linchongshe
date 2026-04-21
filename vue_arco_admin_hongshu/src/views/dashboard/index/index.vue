<template>
  <div class="page-wrap">
    <div class="app-container home">
      <!-- 项目介绍（与系统内其它页一致：顶左对齐 + 统一内边距，避免整块像居中浮窗） -->
      <a-row :gutter="20">
        <a-col :sm="24" :lg="12">
          <h2><b>来因创作服务平台</b></h2>
          <p>
            此项目是基于 springboot + vue 架构的前后端分离系统。
            <br />
            Web 端使用 vue3+ts+elementUI，后端使用 springboot + mybatis-plus
            进行开发。
            <br />
            使用 elasticSearch 作为全文检索服务。
            <br />
            使用 webSocket 做聊天和消息推送。
          </p>
          <p>
            <b>当前版本:</b> <span>v{{ version }}</span>
          </p>
          <p>
            <a-tag color="red">免费开源</a-tag>
          </p>
          <p>
            <a-button
              type="primary"
              icon="cloud"
              plain
              @click="goTarget('https://gitee.com/Maverick_Ma/hongshu')"
            >
              访问码云
            </a-button>
            <a-button
              icon="home"
              plain
              @click="goTarget('https://hongshu.website')"
            >
              访问主页
            </a-button>
          </p>
        </a-col>

        <!-- 技术介绍 -->
        <a-col :sm="24" :lg="12">
          <a-row>
            <a-col :span="12">
              <h2><b>技术选型</b></h2>
            </a-col>
          </a-row>
          <a-row>
            <a-col :span="6">
              <h4>后端技术</h4>
              <ul>
                <li>SpringBoot</li>
                <li>Spring Security</li>
                <li>JWT</li>
                <li>MyBatis-Plus</li>
                <li>Druid</li>
                <li>Fastjson</li>
                <li>...</li>
              </ul>
            </a-col>
            <a-col :span="6">
              <h4>前端技术</h4>
              <ul>
                <li>Vue</li>
                <li>Vuex</li>
                <li>Element-ui</li>
                <li>Axios</li>
                <li>Sass</li>
                <li>Quill</li>
                <li>...</li>
              </ul>
            </a-col>
          </a-row>
        </a-col>
      </a-row>

      <!-- 其他内容，例如部署文档、捐赠支持等 -->
      <a-divider />
      <a-row :gutter="20">
        <a-col :xs="24" :sm="24" :md="12" :lg="8">
          <a-card class="document" :title="'项目部署'">
            <div class="card-block-title">
              <h3 class="text-danger">
                部署该项目选择 阿里云/腾讯云 “2核2G” 配置即可
              </h3>
            </div>
            <blockquote class="text-warning" style="font-size: 16px">
              领取阿里云通用云产品1888优惠券
              <br />
              <a-link
                href="https://www.aliyun.com/minisite/goods?source=5176.11533457&userCode=ojvsntx1"
                type="primary"
                target="_blank"
                >☛☛ 点我进入 ☚☚</a-link
              >
              <br />
              领取腾讯云通用云产品2860优惠券
              <br />
              <a-link
                href="https://curl.qcloud.com/efTJbNyi"
                type="primary"
                target="_blank"
                >☛☛ 点我进入 ☚☚</a-link
              >
              <br />
              阿里云服务器折扣区
              <br />
              <a-link
                href="https://www.aliyun.com/minisite/goods?source=5176.11533457&userCode=ojvsntx1"
                type="primary"
                target="_blank"
                >☛☛ 点我进入 ☚☚</a-link
              >
              <br />
              腾讯云服务器秒杀区
              <br />
              <a-link
                href="https://curl.qcloud.com/efTJbNyi"
                type="primary"
                target="_blank"
                >☛☛ 点我进入 ☚☚</a-link
              >
            </blockquote>
            <div style="margin: 20px 20px">
              <h4 class="text-danger">
                云产品通用红包，可叠加官网常规优惠使用。 (仅限新用户)
              </h4>
            </div>
          </a-card>
        </a-col>

        <!-- 捐赠支持 -->
        <a-col :xs="24" :sm="24" :md="12" :lg="8">
          <a-card class="donate" :title="'捐赠支持'">
            <div class="body">
              <img
                src="@/assets/images/pay.png"
                alt="donate"
                style="width: 92%"
              />
              <span
                style="
                  display: inline-block;
                  height: 30px;
                  line-height: 30px;
                  text-align: center;
                  width: 90%;
                "
              >
                你可以请作者喝杯咖啡☕️表示鼓励❤️
              </span>
            </div>
          </a-card>
        </a-col>

        <!-- 更新日志 -->
        <a-col :xs="24" :sm="24" :md="12" :lg="8">
          <a-card class="update-log" :title="'更新日志'">
            <div v-if="loadingNotice" style="padding: 20px; text-align: center">
              <a-spin />
            </div>
            <a-steps
              v-else
              current="0"
              direction="vertical"
              style="padding-left: 20px"
            >
              <!-- 显示通知数据作为更新日志 -->
              <a-step
                v-for="(notice, index) in displayNotices"
                :key="notice.noticeId || index"
                :title="formatNoticeTitle(notice)"
                :description="formatNoticeDescription(notice)"
              />
            </a-steps>
            <!-- 按钮控制显示额外更新日志 -->
            <div
              v-if="!loadingNotice && noticeList.length > 3"
              style="display: flex; justify-content: flex-end; margin-top: 16px"
            >
              <a-button @click="showAdditionalSteps = !showAdditionalSteps">
                {{ showAdditionalSteps ? '隐藏更多' : '显示更多' }}
              </a-button>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { listNotice, NoticeRecord } from '@/api/system/notice';

const showAdditionalSteps = ref(false); // 控制是否显示额外步骤
const loadingNotice = ref(false);
const noticeList = ref<NoticeRecord[]>([]);

const version = ref('1.0.0');

// 计算显示的更新日志列表
const displayNotices = computed(() => {
  if (showAdditionalSteps.value) {
    return noticeList.value;
  }
  return noticeList.value.slice(0, 3);
});

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 格式化通知标题（版本号 - 日期）
const formatNoticeTitle = (notice: NoticeRecord) => {
  const title = notice.noticeTitle || '';
  const date = formatDate(notice.createTime);
  return `${title} - ${date}`;
};

// 格式化通知描述（从内容中提取或使用标题）
const formatNoticeDescription = (notice: NoticeRecord) => {
  const content = notice.noticeContent || '';
  // 如果是HTML内容，提取纯文本
  if (content.includes('<')) {
    const text = content.replace(/<[^>]*>/g, '').trim();
    // 提取前100个字符
    return text.length > 100 ? `${text.substring(0, 100)}...` : text;
  }
  // 如果是纯文本，直接返回前100个字符
  return content.length > 100 ? `${content.substring(0, 100)}...` : content;
};

// 获取通知列表
const fetchNoticeList = async () => {
  try {
    loadingNotice.value = true;
    const res = await listNotice({
      pageNum: 1,
      pageSize: 10,
      status: '0', // 只获取已发布的通知
    });
    if (res.code === 200 && res.rows) {
      // 按创建时间倒序排列
      noticeList.value = res.rows.sort((a: NoticeRecord, b: NoticeRecord) => {
        const timeA = new Date(a.createTime || 0).getTime();
        const timeB = new Date(b.createTime || 0).getTime();
        return timeB - timeA;
      });
    }
  } catch (error) {
    console.error('获取通知列表失败:', error);
  } finally {
    loadingNotice.value = false;
  }
};

function goTarget(url) {
  window.open(url, '__blank');
}

// 组件挂载时获取通知列表
onMounted(() => {
  fetchNoticeList();
});
</script>

<style lang="less" scoped>
.page-wrap {
  box-sizing: border-box;
  width: 100%;
  min-height: 100%;
  background-color: var(--color-fill-2);
  padding: 16px 20px 20px;
}

.card-block-title {
  margin: 10px 0;
  text-align: left;
}

.home {
  .document {
    padding: 20px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  }

  .donate {
    padding: 20px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  }

  .update-log {
    padding: 20px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  }

  .text-danger {
    color: red;
  }

  .text-warning {
    color: rgb(255, 119, 0);
  }

  ul {
    padding: 0;
    margin: 0;
  }

  h4 {
    margin-top: 0;
  }

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 100;
  }

  p {
    margin-top: 10px;

    b {
      font-weight: 700;
    }
  }
}
</style>

<style lang="less">
// 暗黑模式下的文字颜色 - 只在暗黑模式下生效
html[arco-theme='dark'] .app-container.home,
html[arco-theme='dark'] .home,
html[data-theme='dark'] .app-container.home,
html[data-theme='dark'] .home,
body[arco-theme='dark'] .app-container.home,
body[arco-theme='dark'] .home,
body[data-theme='dark'] .app-container.home,
body[data-theme='dark'] .home,
[arco-theme='dark'] .app-container.home,
[arco-theme='dark'] .home,
[data-theme='dark'] .app-container.home,
[data-theme='dark'] .home {
  h1,
  h2,
  h3,
  h4,
  h5,
  h6 {
    color: #ffffff !important;

    b,
    strong {
      color: #ffffff !important;
    }
  }

  p {
    color: #ffffff !important;

    b,
    strong,
    span {
      color: #ffffff !important;
    }
  }

  ul li,
  ol li {
    color: #ffffff !important;
  }

  blockquote {
    color: #ffffff !important;
  }

  .text-danger {
    color: #ff7875 !important;
  }

  .text-warning {
    color: #ffb84d !important;
  }

  .arco-card {
    .arco-card-body {
      h1,
      h2,
      h3,
      h4,
      h5,
      h6,
      p,
      span,
      div,
      li,
      blockquote {
        color: #ffffff !important;
      }
    }
  }

  .arco-row,
  .arco-col {
    h1,
    h2,
    h3,
    h4,
    h5,
    h6,
    p,
    span,
    div,
    li {
      color: #ffffff !important;
    }
  }

  .arco-link {
    color: #4080ff !important;
  }

  .arco-steps-item-title,
  .arco-steps-item-description {
    color: #ffffff !important;
  }
}
</style>
