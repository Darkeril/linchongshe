<template>
  <div class="container">
    <a-card class="general-card" :title="$t('biz.loginLogCardTitle')">
      <a-row>
        <a-col :flex="1">
          <a-form
            :model="formModel"
            :label-col-props="{ span: 6 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="left"
            :label-col-style="{ paddingRight: '12px' }"
          >
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="userName" :label="$t('biz.username')">
                  <a-input
                    v-model="formModel.userName"
                    :placeholder="$t('biz.enterUsername')"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="ipaddr" :label="$t('biz.ipAddress')">
                  <a-input
                    v-model="formModel.ipaddr"
                    :placeholder="$t('biz.enterIp')"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item>
                  <a-space>
                    <a-button type="primary" @click="search">
                      <template #icon>
                        <icon-search />
                      </template>
                      {{ $t('common.search') }}
                    </a-button>
                    <a-button @click="reset">
                      <template #icon>
                        <icon-refresh />
                      </template>
                      {{ $t('common.reset') }}
                    </a-button>
                  </a-space>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 32px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space>
            <a-button type="primary" @click="handleExport">
              <template #icon>
                <icon-download />
              </template>
              {{ $t('common.export') }}
            </a-button>
            <a-button status="danger" @click="handleClean">
              <template #icon>
                <icon-delete />
              </template>
              {{ $t('biz.clean') }}
            </a-button>
          </a-space>
        </a-col>
      </a-row>
      <a-divider style="margin-top: 0" />
      <a-table
        row-key="id"
        :loading="loading"
        :columns="columns"
        :data="renderData"
        :pagination="pagination"
        :bordered="false"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
      >
        <template #status="{ record }">
          <span v-if="record.status === 0" class="status-text status-success">
            {{ $t('biz.loginStatusSuccess') }}
          </span>
          <span v-else class="status-text status-danger">
            {{ $t('biz.loginStatusFail') }}
          </span>
        </template>
        <template #operations="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="handleView(record)">
              {{ $t('biz.view') }}
            </a-button>
            <a-button type="text" size="small" @click="handleDelete(record)">
              {{ $t('common.delete') }}
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, reactive, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Message } from '@arco-design/web-vue';
import { Pagination } from '@/types/global';
import {
  IconSearch,
  IconRefresh,
  IconDownload,
  IconDelete,
} from '@arco-design/web-vue/es/icon';
import {
  getLogininfoList,
  deleteLogininfo,
  cleanLogininfo,
  exportLogininfo,
} from '@/api/web/logininfo';

const { t } = useI18n();

const generateFormModel = () => {
  return {
    userName: '',
    ipaddr: '',
  };
};
const formModel = ref(generateFormModel());
const loading = ref(false);
const basePagination: Pagination = {
  current: 1,
  pageSize: 20,
};
const pagination = reactive({
  ...basePagination,
});
const columns = computed(() => [
  {
    title: t('biz.idColumn'),
    dataIndex: 'id',
  },
  {
    title: t('biz.username'),
    dataIndex: 'userName',
  },
  {
    title: t('biz.ipAddress'),
    dataIndex: 'ipaddr',
  },
  {
    title: t('biz.loginLocation'),
    dataIndex: 'loginLocation',
  },
  {
    title: t('biz.browser'),
    dataIndex: 'browser',
  },
  {
    title: t('biz.os'),
    dataIndex: 'os',
  },
  {
    title: t('biz.status'),
    dataIndex: 'status',
    slotName: 'status',
  },
  {
    title: t('biz.msgColumn'),
    dataIndex: 'msg',
  },
  {
    title: t('biz.loginTime'),
    dataIndex: 'loginTime',
  },
  {
    title: t('biz.operation'),
    dataIndex: 'operations',
    slotName: 'operations',
  },
]);
const renderData = ref([]);
const fetchData = async (params: any = { current: 1, pageSize: 20 }) => {
  loading.value = true;
  try {
    const { data } = await getLogininfoList(params);
    renderData.value = data.list;
    pagination.current = params.current;
    pagination.total = data.total;
  } catch (err) {
    // you can report use errorHandler or other
  } finally {
    loading.value = false;
  }
};

const search = () => {
  fetchData({
    ...basePagination,
    ...formModel.value,
  } as any);
};
const onPageChange = (current: number) => {
  fetchData({ ...basePagination, current });
};
const onPageSizeChange = (pageSize: number) => {
  fetchData({ ...basePagination, pageSize });
};
const reset = () => {
  formModel.value = generateFormModel();
};
const handleView = (record: any) => {
  // TODO: 实现查看详情逻辑
  Message.info(t('biz.viewDetailTodo'));
};
const handleDelete = async (record: any) => {
  try {
    await deleteLogininfo(record.id);
    Message.success(t('biz.deleteSuccess'));
    fetchData();
  } catch (error) {
    Message.error(t('biz.deleteFailed'));
  }
};
const handleExport = async () => {
  try {
    await exportLogininfo();
    Message.success(t('biz.exportSuccess'));
  } catch (error) {
    Message.error(t('biz.exportFailed'));
  }
};
const handleClean = async () => {
  try {
    await cleanLogininfo();
    Message.success(t('biz.cleanSuccess'));
    fetchData();
  } catch (error) {
    Message.error(t('biz.cleanFailed'));
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}
.general-card {
  min-height: 395px;
}
:deep(.arco-table-th) {
  &:last-child {
    .arco-table-th-item-title {
      margin-left: 16px;
    }
  }
}
</style>


