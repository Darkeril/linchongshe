<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="breadcrumbItems"></Breadcrumb>
    <a-card
      class="general-card r-nav-card"
      :body-style="{ padding: '15px' }"
      :header-style="{ padding: '15px' }"
    >
      <a-row>
        <!-- 搜索区块 -->
        <a-col :flex="1">
          <a-form
            :model="state.queryParams"
            :label-col-props="{ span: 6 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="left"
            auto-label-width
          >
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="noticeTitle" :label="$t('biz.noticeTitle')">
                  <a-input
                    v-model="state.queryParams.noticeTitle"
                    :placeholder="$t('biz.noticeTitlePlaceholder')"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="createBy" :label="$t('biz.operator')">
                  <a-input
                    v-model="state.queryParams.createBy"
                    :placeholder="$t('biz.operatorPlaceholder')"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="noticeType" :label="$t('biz.noticeType')">
                  <a-select
                    v-model="state.queryParams.noticeType"
                    :placeholder="$t('biz.selectPlaceholder')"
                  >
                    <a-option
                      v-for="dict in state.noticeTypeOptions"
                      :key="dict.dictValue"
                      :value="dict.dictValue"
                      :label="dict.dictLabel"
                      >{{ dict.dictLabel }}
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 42px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space :size="18">
            <a-button type="primary" @click="fetchNoticeData">
              <template #icon>
                <icon-search />
              </template>
              {{ $t('biz.query') }}
            </a-button>
            <a-button @click="resetQueryForm">
              <template #icon>
                <icon-refresh />
              </template>
              {{ $t('common.reset') }}
            </a-button>
          </a-space>
        </a-col>

        <a-divider style="margin-top: 0" />
        <!-- 工具栏 -->
        <a-row style="margin-bottom: 16px">
          <!-- 前置工具栏部分 -->
          <a-col :span="12">
            <a-space>
              <a-button
                v-permission="['system:notice:add']"
                type="primary"
                @click="handleAdd"
              >
                <template #icon>
                  <icon-plus />
                </template>
                {{ $t('common.add') }}
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-row>

      <!-- 表格数据-->
      <a-table
        row-key="noticeId"
        :loading="loading"
        :pagination="pagination"
        :columns="state.columns"
        :data="state.noticeList"
        :bordered="false"
        :size="'medium'"
        @page-change="onPageChange"
      >
        <template #noticeType="{ record }">
          <dict-tag
            :options="state.noticeTypeOptions"
            :value="record.noticeType"
          />
        </template>
        <template #status="{ record }">
          <dict-tag
            :options="state.noticeStatusOptions"
            :value="record.status"
          />
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button-group>
              <a-button
                v-permission="['system:notice:edit']"
                type="text"
                size="mini"
                @click="handleUpdate(record.noticeId)"
              >
                <template #icon>
                  <icon-edit />
                </template>
                {{ $t('biz.modify') }}
              </a-button>
              <a-button
                v-permission="['system:notice:remove']"
                type="text"
                size="mini"
                @click="handleDelete(record)"
              >
                <template #icon>
                  <icon-delete />
                </template>
                {{ $t('common.delete') }}
              </a-button>
            </a-button-group>
          </a-space>
        </template>
      </a-table>
    </a-card>
    <edit-notice
      v-model:id="state.currentId"
      v-model:visible="state.editVisible"
      @refresh-data-list="fetchNoticeData"
    >
    </edit-notice>
  </div>
</template>

<script lang="ts" setup>
// 定义页面加载中效果变量
import useLoading from '@/hooks/loading';
import { computed, reactive } from 'vue';
import { useI18n } from 'vue-i18n';
import { DictDataRecord, getDicts } from '@/api/system/dict-data';
import { delNotice, listNotice, NoticeRecord } from '@/api/system/notice';
import { Message, Modal } from '@arco-design/web-vue';
import { Pagination } from '@/types/global';
import { TableColumnData } from '@arco-design/web-vue/es/table/interface';
import DictTag from '@/components/dict-tag/index.vue';
import EditNotice from './components/edit-notice.vue';

const { t } = useI18n();

const { loading, setLoading } = useLoading(true);

const breadcrumbItems = computed(() => [
  t('menu.system'),
  t('biz.pageNoticeManage'),
]);

const basePagination: Pagination = {
  current: 1,
  pageSize: 10,
};
const pagination = reactive({
  ...basePagination,
});

const state = reactive({
  queryParams: {
    noticeTitle: undefined,
    noticeType: undefined,
    createBy: undefined,
    status: undefined,
  },
  // 列表数据
  noticeList: [],
  // 字典
  noticeStatusOptions: [] as DictDataRecord[],
  noticeTypeOptions: [] as DictDataRecord[],
  // 选中行ID
  currentId: 0,
  // 显示编辑页面
  editVisible: false,
  // 列表显示列
  columns: computed<TableColumnData[]>(() => [
    {
      title: t('biz.serialNo'),
      dataIndex: 'noticeId',
      slotName: 'noticeId',
      width: 50,
      align: 'center',
    },
    {
      title: t('biz.noticeTitle'),
      dataIndex: 'noticeTitle',
      slotName: 'noticeTitle',
      ellipsis: true,
      tooltip: { position: 'right' },
      width: 200,
      align: 'center',
    },
    {
      title: t('biz.noticeType'),
      dataIndex: 'noticeType',
      slotName: 'noticeType',
      width: 100,
      align: 'center',
    },
    {
      title: t('biz.status'),
      dataIndex: 'status',
      slotName: 'status',
      width: 100,
    },
    {
      title: t('biz.creator'),
      dataIndex: 'createBy',
      slotName: 'createBy',
      width: 100,
      align: 'center',
    },
    {
      title: t('biz.createTime'),
      dataIndex: 'createTime',
      slotName: 'createTime',
      width: 200,
      align: 'center',
    },
    {
      title: t('biz.operation'),
      dataIndex: 'optional',
      slotName: 'optional',
      fixed: 'right',
      width: 200,
      align: 'center',
    },
  ]),
});

// 初始化字典
const initData = async () => {
  const { data: noticeStatus } = await getDicts('sys_notice_status');
  state.noticeStatusOptions = noticeStatus;
  const { data: noticeType } = await getDicts('sys_notice_type');
  state.noticeTypeOptions = noticeType;
};
initData();

// 重置查询表单
const resetQueryForm = () => {
  state.queryParams = {
    noticeTitle: undefined,
    noticeType: undefined,
    createBy: undefined,
    status: undefined,
  };
  state.currentId = 0;
};

// 获取列表数据
const fetchNoticeData = async () => {
  setLoading(true);
  const { rows, total } = await listNotice({
    ...pagination,
    ...state.queryParams,
  });
  state.noticeList = rows;
  pagination.total = total;
  setLoading(false);
};
fetchNoticeData();

// 分页
const onPageChange = (current: number) => {
  pagination.current = current;
  fetchNoticeData();
};

// 打开新增
const handleAdd = () => {
  state.editVisible = true;
  state.currentId = 0;
};

// 打开编辑
const handleUpdate = (noticeId: number) => {
  state.editVisible = true;
  state.currentId = noticeId;
};

// 删除菜单
const handleDelete = (row: NoticeRecord) => {
  Modal.confirm({
    titleAlign: 'start',
    title: t('biz.deleteConfirmTitle'),
    content: t('biz.deleteNoticeItemContent', { id: row.noticeId }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: () => {
      delNotice(row.noticeId as number).then(() => {
        Message.success(t('biz.deleteSuccess'));
        fetchNoticeData();
      });
    },
  });
};
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}
</style>
