<template>
  <a-card
    class="general-card"
    :title="$t('dashboard.announcement')"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ padding: '15px 20px 13px 20px' }"
  >
    <template #extra>
      <a-link @click="handleViewMore">{{ $t('dashboard.viewMore') }}</a-link>
    </template>
    <div v-if="loading" style="text-align: center; padding: 20px 0">
      <a-spin />
    </div>
    <div v-else-if="list.length === 0" style="text-align: center; padding: 20px 0; color: var(--color-text-3)">
      {{ $t('dashboard.noAnnouncement') }}
    </div>
    <div v-else>
      <div v-for="(item, idx) in list" :key="item.noticeId || idx" class="item" @click="handleItemClick(item)">
        <a-tag :color="getTagColor(item.noticeType)" size="small">{{ getTagLabel(item.noticeType) }}</a-tag>
        <span class="item-content">
          {{ getDisplayContent(item) }}
        </span>
      </div>
    </div>
  </a-card>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { listNotice, NoticeRecord } from '@/api/system/notice';
import { getDicts, DictDataRecord } from '@/api/system/dict-data';

const { t } = useI18n();
const router = useRouter();
const loading = ref(false);
const noticeList = ref<NoticeRecord[]>([]);
const noticeTypeOptions = ref<DictDataRecord[]>([]);

// 显示前5条
const list = computed(() => {
  return noticeList.value.slice(0, 5);
});

// 获取标签颜色
const getTagColor = (noticeType?: string) => {
  if (!noticeType) return 'blue';
  // 根据通知类型返回不同颜色
  // 1-通知(blue), 2-公告(orangered), 3-消息(cyan)
  const typeMap: Record<string, string> = {
    '1': 'blue',
    '2': 'orangered',
    '3': 'cyan',
  };
  return typeMap[noticeType] || 'blue';
};

// 获取标签文字
const getTagLabel = (noticeType?: string) => {
  if (!noticeType) return t('dashboard.notice');
  const option = noticeTypeOptions.value.find((item) => item.dictValue === noticeType);
  return option?.dictLabel || t('dashboard.notice');
};

// 获取显示内容
const getDisplayContent = (item: NoticeRecord) => {
  // 优先显示标题，如果没有标题则显示内容的前50个字符
  if (item.noticeTitle) {
    return item.noticeTitle;
  }
  if (item.noticeContent) {
    // 如果是HTML内容，提取纯文本
    const text = item.noticeContent.replace(/<[^>]*>/g, '').trim();
    return text.length > 50 ? `${text.substring(0, 50)}...` : text;
  }
  return '';
};

// 点击查看更多
const handleViewMore = () => {
  router.push({ name: 'Notice' });
};

// 点击公告项
const handleItemClick = (item: NoticeRecord) => {
  // 可以跳转到详情页或通知管理页
  router.push({ name: 'Notice' });
};

// 初始化字典
const initDict = async () => {
  try {
    const { data: noticeType } = await getDicts('sys_notice_type');
    noticeTypeOptions.value = noticeType || [];
  } catch (error) {
    console.error('获取通知类型字典失败:', error);
  }
};

// 获取通知列表
const fetchNoticeList = async () => {
  try {
    loading.value = true;
    const res = await listNotice({
      pageNum: 1,
      pageSize: 5,
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
    loading.value = false;
  }
};

onMounted(() => {
  initDict();
  fetchNoticeList();
});
</script>

<style scoped lang="less">
.item {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 24px;
  margin-bottom: 4px;
  padding: 4px 0;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: var(--color-fill-2);
  }

  .item-content {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-left: 4px;
    color: var(--color-text-2);
    text-decoration: none;
    font-size: 13px;
  }
}
</style>
