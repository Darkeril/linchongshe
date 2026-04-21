<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['来因管理', '登录日志']"></Breadcrumb>
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
                <a-form-item field="ipaddr" :label="'登录地址'">
                  <a-input
                    v-model="state.queryParams.ipaddr"
                    :placeholder="'请输入登录地址'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="userName" :label="'用户名称'">
                  <a-input
                    v-model="state.queryParams.userName"
                    :placeholder="'请输入用户名称'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="status" :label="'状态'">
                  <a-select
                    v-model="state.queryParams.status"
                    placeholder="请选择登录状态"
                  >
                    <a-option
                      v-for="dict in state.statusOptions"
                      :key="dict.dictValue"
                      :value="dict.dictValue"
                      :label="dict.dictLabel"
                      >{{ dict.dictLabel }}
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="dateRange" :label="'登录时间'">
                  <a-range-picker
                    v-model="state.dateRange"
                    style="width: 100%"
                  ></a-range-picker>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 84px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space direction="vertical" :size="18">
            <a-button type="primary" @click="fetchListData">
              <template #icon>
                <icon-search />
              </template>
              查询
            </a-button>
            <a-button @click="resetQueryForm">
              <template #icon>
                <icon-refresh />
              </template>
              重置
            </a-button>
          </a-space>
        </a-col>

        <a-divider style="margin-top: 0" />
        <!-- 工具栏 -->
        <a-row style="margin-bottom: 16px">
          <!-- 前置工具栏部分 -->
          <a-col :span="12">
            <a-space>
              <a-space>
                <a-button
                  v-permission="['monitor:logininfor:remove']"
                  status="danger"
                  :disabled="state.selectedKeys.length <= 0"
                  @click="handleDelete"
                >
                  <template #icon>
                    <icon-delete />
                  </template>
                  删除
                </a-button>
                <a-button
                  v-permission="['monitor:logininfor:remove']"
                  status="danger"
                  @click="handleClean"
                >
                  <template #icon>
                    <icon-delete />
                  </template>
                  清空
                </a-button>
                <a-button
                  v-permission="['monitor:logininfor:export']"
                  status="warning"
                  @click="handleExport"
                >
                  <template #icon>
                    <icon-download />
                  </template>
                  导出
                </a-button>
              </a-space>
            </a-space>
          </a-col>
        </a-row>
      </a-row>

      <!-- 表格数据-->
      <a-table
        ref="tableRef"
        v-model:selected-keys="state.selectedKeys"
        row-key="id"
        :loading="loading"
        :pagination="pagination"
        :columns="state.columns"
        :data="state.list"
        :row-selection="state.rowSelection"
        :bordered="false"
        :size="'medium'"
        @select="handleSelected"
        @sorter-change="handleSorterChange"
        @page-change="onPageChange"
      >
        <template #status="{ record }">
          <dict-tag :options="state.statusOptions" :value="record.status" />
        </template>
        <template #loginLocation="{ record }">
          <a-tag
            :color="record.loginLocation ? 'orange' : 'gray'"
            size="medium"
            style="
              border-radius: 4px;
              font-weight: 500;
              padding: 6px 12px;
              font-size: 13px;
            "
          >
            {{ record.loginLocation || '未知位置' }}
          </a-tag>
        </template>
        <template #fromType="{ record }">
          <a-tag
            :color="getFromTypeColor(record.fromType)"
            size="medium"
            style="
              border-radius: 4px;
              font-weight: 500;
              padding: 6px 12px;
              font-size: 13px;
            "
          >
            {{ getFromTypeLabel(record.fromType) }}
          </a-tag>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
// 定义页面加载中效果变量
import useLoading from '@/hooks/loading';
import { computed, reactive } from 'vue';
import { DictDataRecord, getDicts } from '@/api/system/dict-data';
import { Message, Modal } from '@arco-design/web-vue';
import { Pagination } from '@/types/global';
import addDateRange from '@/utils/common';
import {
  cleanLogininfor,
  delLogininfor,
  listLogininfor,
  unlockLogininfor,
} from '@/api/web/logininfor';
import { download } from '@/api/download';
import { useRoute } from 'vue-router';
import { getTodayRange } from '@/utils/workbench-nav';
import {
  TableColumnData,
  TableData,
  TableRowSelection,
} from '@arco-design/web-vue/es/table/interface';
import DictTag from '@/components/dict-tag/index.vue';

const route = useRoute();
const { loading, setLoading } = useLoading(true);

const basePagination: Pagination = {
  current: 1,
  pageSize: 10,
};
const pagination = reactive({
  ...basePagination,
});

const state = reactive({
  queryParams: {
    orderByColumn: '',
    isAsc: '',
    ipaddr: undefined,
    userName: undefined,
    status: undefined,
  },
  rowSelection: {
    type: 'checkbox',
    showCheckedAll: true,
    onlyCurrent: false,
  } as TableRowSelection,
  // 表格选中值
  selectedKeys: [] as (string | number)[],
  selectName: '',
  // 时间选择
  dateRange: [],
  // 列表数据
  list: [],
  // 字典
  statusOptions: [] as DictDataRecord[],
  // 选中行ID
  currentId: 0,
  // 列表显示列
  columns: computed<TableColumnData[]>(() => [
    {
      title: '编号',
      dataIndex: 'id',
      slotName: 'id',
      width: 60,
      align: 'center',
    },
    {
      title: '用户名称',
      dataIndex: 'username',
      slotName: 'username',
      ellipsis: true,
      tooltip: { position: 'right' },
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 130,
      align: 'center',
    },
    {
      title: '登录来源',
      dataIndex: 'fromType',
      slotName: 'fromType',
      width: 100,
      align: 'center',
    },
    {
      title: '登录地址',
      dataIndex: 'ipaddr',
      slotName: 'ipaddr',
      width: 130,
      align: 'center',
    },
    {
      title: '登录地点',
      dataIndex: 'loginLocation',
      slotName: 'loginLocation',
      width: 110,
      align: 'center',
    },
    {
      title: '浏览器',
      dataIndex: 'browser',
      slotName: 'browser',
      width: 130,
      align: 'center',
    },
    // {
    //   title: '操作地点',
    //   dataIndex: 'operLocation',
    //   slotName: 'operLocation',
    //   width: 200,
    //   align: 'center',
    // },
    {
      title: '操作系统',
      dataIndex: 'os',
      slotName: 'os',
      width: 150,
      align: 'center',
    },
    // {
    //   title: '操作状态',
    //   dataIndex: 'status',
    //   slotName: 'status',
    //   width: 100,
    //   align: 'center',
    // },
    {
      title: '操作信息',
      dataIndex: 'msg',
      slotName: 'msg',
      width: 120,
      align: 'center',
    },
    {
      title: '登录日期',
      dataIndex: 'loginTime',
      slotName: 'loginTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 200,
      align: 'center',
      render: ({ record }) => {
        const date = new Date(record.loginTime);
        return date.toLocaleDateString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false, // 24小时制
        });
      },
    },
  ]),
});

// 初始化字典
const initData = async () => {
  const { data: status } = await getDicts('sys_common_status');
  state.statusOptions = status;
};
initData();

// 重置查询表单
const resetQueryForm = () => {
  state.queryParams = {
    orderByColumn: '',
    isAsc: '',
    ipaddr: undefined,
    userName: undefined,
    status: undefined,
  };
  state.selectName = '';
  state.dateRange = [];
};

// 获取列表数据
const fetchListData = async () => {
  setLoading(true);
  try {
    const { rows, total } = await listLogininfor({
      ...pagination,
      ...addDateRange(state.queryParams, state.dateRange),
    });
    state.list = rows;
    pagination.total = total;
  } catch (error: any) {
    // 401 错误已经在拦截器中处理（显示错误并触发 logout），这里静默处理
    // 检查是否是 401 错误（多种方式判断）
    const errorMessage = error?.message || error?.msg || '';
    const is401Error = 
      error?.response?.status === 401 || 
      error?.response?.data?.code === 401 ||
      error?.isHandled === true || // 拦截器标记的错误
      errorMessage.includes('认证失败') ||
      errorMessage.includes('Unauthorized') ||
      errorMessage.includes('无法访问系统资源');
    
    // 如果是 401 错误，完全静默处理（不打印日志）
    // 其他错误也静默处理，避免未捕获的 Promise 错误
    if (!is401Error) {
      console.error('获取登录日志列表失败:', error);
    }
    state.list = [];
    pagination.total = 0;
  } finally {
    setLoading(false);
  }
};
if (route.query.today === '1') {
  state.dateRange = getTodayRange() as unknown as never[];
}
fetchListData();

// 分页
const onPageChange = (current: number) => {
  pagination.current = current;
  fetchListData();
};

// 多选框选中数据
const handleSelected = (
  rowKeys: (string | number)[],
  rowKey: string | number,
  record: TableData
) => {
  state.selectName = record.userName;
};

// 排序触发事件
const handleSorterChange = (dataIndex: string, direction: string) => {
  state.queryParams.orderByColumn = dataIndex;
  state.queryParams.isAsc = direction;
  fetchListData();
};

// 删除
const handleDelete = () => {
  const ids = state.selectedKeys.join(',');
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: `是否确认删除访问编号为"${ids}"的数据项？`,
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      delLogininfor(ids).then(() => {
        Message.success('删除成功');
        fetchListData();
      });
    },
  });
};

// 清空按钮操作
const handleClean = () => {
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: '是否确认清空所有登录日志数据项？',
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      cleanLogininfor().then(() => {
        Message.success('清空成功');
        fetchListData();
      });
    },
  });
};

// 解锁按钮操作
const handleUnlock = () => {
  const username = state.selectName;
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: `是否确认解锁用户${username}数据项?`,
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      unlockLogininfor(username).then(() => {
        Message.success(`用户${username}解锁成功`);
        fetchListData();
      });
    },
  });
};

// 导出按钮操作
const handleExport = () => {
  download(
    'monitor/logininfor/export',
    state.queryParams,
    `logininfor_${new Date().getTime()}.xlsx`
  );
};

// 获取登录来源标签
const getFromTypeLabel = (fromType: string | number) => {
  if (fromType === undefined || fromType === null) return '未知';
  if (fromType === 0 || fromType === '0') {
    return 'Web端';
  }
  if (fromType === 1 || fromType === '1') {
    return 'App端';
  }
  return '未知';
};

// 获取登录来源颜色
const getFromTypeColor = (fromType: string | number) => {
  if (fromType === undefined || fromType === null) return 'gray';
  if (fromType === 0 || fromType === '0') {
    return 'blue'; // web端用蓝色
  }
  if (fromType === 1 || fromType === '1') {
    return 'green'; // app端用绿色
  }
  return 'gray'; // 其他情况用灰色
};
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}
</style>
