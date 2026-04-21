<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="{ padding: '17px 20px 21px 20px' }"
    >
      <template #title>
        {{ $t('dashboard.contentAnalysis') }}
      </template>
      <a-space direction="vertical" :size="10" fill>
        <a-radio-group
          v-model:model-value="type"
          type="button"
          @change="typeChange"
        >
          <a-radio value="1">
            {{ $t('dashboard.imageText') }}
          </a-radio>
          <a-radio value="2">
            {{ $t('dashboard.video') }}
          </a-radio>
          <a-radio value="3">
            {{ $t('dashboard.livePhoto') }}
          </a-radio>
        </a-radio-group>
        <a-table
          :data="renderList"
          :pagination="false"
          :bordered="false"
          :scroll="{ x: '100%', y: '264px' }"
        >
          <template #columns>
            <a-table-column
              :title="$t('dashboard.rank')"
              data-index="key"
            ></a-table-column>
            <a-table-column
              :title="$t('dashboard.contentTitle')"
              data-index="title"
            >
              <template #cell="{ record }">
                <a-typography-paragraph
                  :ellipsis="{
                    rows: 1,
                  }"
                >
                  {{ record.title }}
                </a-typography-paragraph>
              </template>
            </a-table-column>
            <a-table-column
              :title="$t('dashboard.clickCount')"
              data-index="viewCount"
              :sortable="{
                sortDirections: ['ascend', 'descend'],
              }"
            >
            </a-table-column>
            <a-table-column
              :title="$t('dashboard.dailyGrowth')"
              data-index="increases"
              :sortable="{
                sortDirections: ['ascend', 'descend'],
              }"
            >
              <template #cell="{ record }">
                <div class="increases-cell">
                  <span>{{ record.increases }}%</span>
                  <icon-caret-up
                    v-if="record.increases !== 0"
                    style="color: #f53f3f; font-size: 8px"
                  />
                </div>
              </template>
            </a-table-column>
          </template>
        </a-table>
      </a-space>
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import useLoading from '@/hooks/loading';
import { queryPopularList } from '@/api/dashboard';
import type { TableData } from '@arco-design/web-vue/es/table/interface';

const type = ref('1');
const { loading, setLoading } = useLoading();
const renderList = ref<TableData[]>([]);
const fetchData = async (contentType: string) => {
  try {
    setLoading(true);
    const { data } = await queryPopularList({ type: contentType });
    renderList.value = data;

    // 为每个项目添加排名（这里使用数组索引作为排名）
    renderList.value = data.map((item: any, index: number) => ({
      ...item,
      key: index + 1, // 设置排名值
    }));
  } catch (err) {
    // you can report use errorHandler or other
  } finally {
    setLoading(false);
  }
};
const typeChange = (contentType: string) => {
  fetchData(contentType);
};
// 使用 watch 监听 type 的变化
watch(type, (newType) => {
  fetchData(newType); // 当 type 改变时调用 fetchData
});

// 初始加载时只请求一次数据
fetchData('1');
</script>

<style scoped lang="less">
.general-card {
  min-height: 395px;
}

:deep(.arco-table-tr) {
  height: 44px;

  .arco-typography {
    margin-bottom: 0;
  }
}

.increases-cell {
  display: flex;
  align-items: center;

  span {
    margin-right: 4px;
  }
}
</style>
