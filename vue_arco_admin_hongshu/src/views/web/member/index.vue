<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['会员中心', '会员管理']"></Breadcrumb>
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
              <a-col :span="6">
                <a-form-item field="hsId" :label="'来因号'">
                  <a-input
                    v-model="state.queryParams.hsId"
                    :placeholder="'请输入来因号'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="username" :label="'会员名称'">
                  <a-input
                    v-model="state.queryParams.username"
                    :placeholder="'请输入会员名称'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="phone" :label="'手机号'">
                  <a-input
                    v-model="state.queryParams.phone"
                    :placeholder="'请输入手机号'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="status" :label="'状态'">
                  <a-select
                    v-model="state.queryParams.status"
                    placeholder="请选择会员状态"
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
              <a-col :span="6">
                <a-form-item field="fromType" :label="'会员来源'">
                  <a-select
                    v-model="state.queryParams.fromType"
                    placeholder="请选择会员来源"
                    allow-clear
                  >
                    <a-option
                      v-for="dict in state.fromTypeOptions"
                      :key="dict.dictValue"
                      :value="dict.dictValue"
                      :label="dict.dictLabel"
                      >{{ dict.dictLabel }}
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="createTime" :label="'注册时间'">
                  <a-range-picker v-model="state.dateRange" style="width: 100%">
                  </a-range-picker>
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
                  v-permission="['web:member:add']"
                  type="primary"
                  @click="handleAdd()"
                >
                  <template #icon>
                    <IconPlus />
                  </template>
                  新建
                </a-button>
                <!-- <a-button
                  v-permission="['web:member:edit']"
                  type="outline"
                  :disabled="state.selectedKeys.length !== 1"
                  @click="handleBatchEdit"
                >
                  <template #icon>
                    <IconEdit />
                  </template>
                  编辑
                </a-button> -->
                <a-button
                  v-permission="['web:member:remove']"
                  status="danger"
                  :disabled="state.selectedKeys.length <= 0"
                  @click="handleDelete"
                >
                  <template #icon>
                    <IconDelete />
                  </template>
                  删除
                </a-button>
                <a-button
                  v-permission="['monitor:logininfor:remove']"
                  status="danger"
                  @click="handleClean"
                >
                  <template #icon>
                    <IconDelete />
                  </template>
                  清空
                </a-button>
                <!-- <a-button
                  v-permission="['monitor:logininfor:export']"
                  status="warning"
                  @click="handleExport"
                >
                  <template #icon>
                    <IconDownload />
                  </template>
                  导出
                </a-button> -->
                <a-button
                  v-permission="['web:member:edit']"
                  status="danger"
                  :disabled="state.selectedKeys.length <= 0"
                  @click="handleBatchDisable"
                >
                  <template #icon>
                    <IconStop />
                  </template>
                  禁用
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
        <template #gender="{ record }">
          <a-tag
            :color="getGenderColor(record.gender)"
            size="medium"
            style="
              border-radius: 4px;
              font-weight: 500;
              padding: 6px 12px;
              font-size: 13px;
            "
          >
            {{ getGenderLabel(record.gender) || '未知' }}
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
            {{ getFromTypeLabel(record.fromType) || '未知来源' }}
          </a-tag>
        </template>
        <template #address="{ record }">
          <a-tag
            :color="record.address ? 'orange' : 'gray'"
            size="medium"
            style="
              border-radius: 4px;
              font-weight: 500;
              padding: 6px 12px;
              font-size: 13px;
            "
          >
            {{ record.address || '未知位置' }}
          </a-tag>
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button-group>
              <a-button
                v-permission="['web:member:edit']"
                type="text"
                size="mini"
                @click="handleUpdate(record)"
              >
                <template #icon>
                  <IconEdit />
                </template>
                修改
              </a-button>
              <a-button
                v-permission="['web:member:remove']"
                type="text"
                size="mini"
                @click="handleDelete(record)"
              >
                <template #icon>
                  <IconDelete />
                </template>
                删除
              </a-button>
            </a-button-group>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
  <edit-member
    v-model:id="state.currentId"
    v-model:visible="state.editVisible"
    @refresh-data-list="fetchListData"
  >
  </edit-member>
</template>

<script lang="ts" setup>
import useLoading from '@/hooks/loading';
import { computed, reactive, h } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AImage from '@arco-design/web-vue/es/image';
import ASwitch from '@arco-design/web-vue/es/switch';
import { DictDataRecord, getDicts } from '@/api/system/dict-data';
import { Message, Modal } from '@arco-design/web-vue';
import { Pagination } from '@/types/global';
import addDateRange from '@/utils/common';
import { getTodayRange } from '@/utils/workbench-nav';
import {
  listMember,
  addMember,
  delMember,
  disableMember,
  updateMemberStatus,
  delAllMember,
  getMember,
  updateMember,
} from '@/api/web/member';
import { download } from '@/api/download';
import {
  TableColumnData,
  TableData,
} from '@arco-design/web-vue/es/table/interface';
import DictTag from '@/components/dict-tag/index.vue';
import EditMember from './components/edit-member.vue';

const { loading, setLoading } = useLoading(true);
const route = useRoute();
const router = useRouter();

const basePagination: Pagination = {
  current: 1,
  pageSize: 10,
};
const pagination = reactive({
  ...basePagination,
});

// 详情跳转（与项目1一致）
const toNoteList = (record: any) => {
  router.push({ path: '/note/note', query: { uid: record.id } });
};
const toProductList = (record: any) => {
  router.push({ path: '/idle/product', query: { uid: record.id } });
};

// 状态切换（放在 columns 之前，避免 no-use-before-define）
const handleStatusChange = (record: any, nextStatus: string) => {
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: `确认要"${nextStatus === '0' ? '启用' : '禁用'}"【${
      record.username || record.nickName || record.userName
    }】用户吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: () =>
      updateMemberStatus(record.id, nextStatus).then(() => {
        record.status = nextStatus;
        Message.success('操作成功');
      }),
    onCancel: () => {},
  });
};

const state = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    hsId: '',
    username: undefined,
    phone: undefined,
    status: undefined,
    fromType: undefined,
    orderByColumn: undefined as string | undefined,
    isAsc: undefined as string | undefined,
  },
  rowSelection: {
    type: 'checkbox',
    showCheckedAll: true,
    onlyCurrent: false,
  } as unknown,
  // 表格选中值
  selectedKeys: [] as (string | number)[],
  selectName: '',
  // 时间选择
  dateRange: [],
  // 显示编辑页面
  editVisible: false,
  // 列表数据
  list: [],
  // 字典
  statusOptions: [] as DictDataRecord[],
  sexOptions: [] as DictDataRecord[],
  fromTypeOptions: [] as DictDataRecord[],
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
      title: '来因号',
      dataIndex: 'hsId',
      slotName: 'hsId',
      width: 120,
      align: 'center',
    },
    {
      title: '头像',
      dataIndex: 'avatar',
      slotName: 'avatar',
      width: 100,
      align: 'center',
      render: ({ record }) =>
        h(AImage, {
          src: record.avatar,
          width: '60px',
          height: '60px',
          preview: true, // 支持图片预览
          alt: '头像',
          style: {
            borderRadius: '50%', // 圆形样式
            objectFit: 'cover', // 保持图片缩放比例
            border: '1px solid #d9d9d9', // 可选边框
          },
        }),
    },
    {
      title: '会员名称',
      dataIndex: 'username',
      slotName: 'username',
      width: 130,
      align: 'center',
    },
    {
      title: '性别',
      dataIndex: 'gender',
      slotName: 'gender',
      width: 100,
      align: 'center',
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      slotName: 'phone',
      width: 120,
      align: 'center',
    },
    {
      title: '会员来源',
      dataIndex: 'fromType',
      slotName: 'fromType',
      width: 110,
      align: 'center',
    },
    {
      title: '笔记数量',
      dataIndex: 'noteCount',
      slotName: 'noteCount',
      width: 120,
      align: 'center',
      render: ({ record }) =>
        h(
          'a',
          {
            style: 'color:#165DFF;cursor:pointer',
            onClick: () => toNoteList(record),
          },
          record.noteCount ?? 0
        ),
    },
    {
      title: '商品数量',
      dataIndex: 'productCount',
      slotName: 'productCount',
      width: 120,
      align: 'center',
      render: ({ record }) =>
        h(
          'a',
          {
            style: 'color:#165DFF;cursor:pointer',
            onClick: () => toProductList(record),
          },
          record.productCount ?? 0
        ),
    },
    {
      title: '登录IP',
      dataIndex: 'loginIp',
      slotName: 'loginIp',
      width: 130,
      align: 'center',
    },
    {
      title: '登录地点',
      dataIndex: 'address',
      slotName: 'address',
      width: 120,
      align: 'center',
    },
    {
      title: '状态',
      dataIndex: 'status',
      slotName: 'status',
      width: 120,
      align: 'center',
      render: ({ record }) =>
        h(
          ASwitch as any,
          {
            checkedValue: '0',
            uncheckedValue: '1',
            modelValue: String(record.status),
            onChange: (val: string) => handleStatusChange(record, val),
          },
          {
            checked: () => '启用',
            unchecked: () => '禁用',
          }
        ),
    },
    {
      title: '注册时间',
      dataIndex: 'createTime',
      slotName: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 200,
      align: 'center',
      render: ({ record }) => {
        if (!record.createTime) return '-';
        const date = new Date(record.createTime);
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
    {
      title: '最后登录时间',
      dataIndex: 'loginDate',
      slotName: 'loginDate',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 200,
      align: 'center',
      render: ({ record }) => {
        if (!record.loginDate) return '-';
        const date = new Date(record.loginDate);
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
    {
      title: '操作',
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
  const [statusResponse, sexResponse, fromTypeResp] = await Promise.all([
    getDicts('web_member_status').catch(() => getDicts('sys_normal_disable')),
    getDicts('sys_user_sex'),
    getDicts('from_type'),
  ]);
  state.statusOptions = statusResponse.data;
  state.sexOptions = sexResponse.data;
  state.fromTypeOptions = fromTypeResp.data;
};
initData();

// 获取会员来源标签文本
const getFromTypeLabel = (fromType: string | number) => {
  const option = state.fromTypeOptions.find(
    (item) => item.dictValue === String(fromType)
  );
  return option ? option.dictLabel : '';
};

// 获取性别标签文本
const getGenderLabel = (gender: string | number) => {
  const option = state.sexOptions.find(
    (item) => item.dictValue === String(gender)
  );
  return option ? option.dictLabel : '';
};

// 获取性别颜色
const getGenderColor = (gender: string | number) => {
  if (gender === undefined || gender === null) return 'gray';

  // 根据性别值判断颜色
  if (gender === 0 || gender === '0') {
    return 'blue'; // 男用蓝色
  }
  if (gender === 1 || gender === '1') {
    return 'pink'; // 女用粉色
  }
  return 'gray'; // 其他情况用灰色
};

// 获取会员来源颜色
const getFromTypeColor = (fromType: string | number) => {
  if (fromType === undefined || fromType === null) return 'gray';

  // 根据fromType值直接判断颜色
  if (fromType === 0 || fromType === '0') {
    return 'blue'; // web端用蓝色
  }
  if (fromType === 1 || fromType === '1') {
    return 'green'; // app端用绿色
  }
  if (fromType === 2 || fromType === '2') {
    return 'orange'; // 小程序端用橙色
  }
  return 'gray'; // 其他情况用灰色
};

// 重置查询表单
const resetQueryForm = () => {
  state.queryParams = {
    pageNum: 1,
    pageSize: 10,
    hsId: '',
    username: undefined,
    phone: undefined,
    status: undefined,
    fromType: undefined,
    orderByColumn: undefined as string | undefined,
    isAsc: undefined as string | undefined,
  };
  state.selectName = '';
  state.dateRange = [];
  // eslint-disable-next-line no-use-before-define
  fetchListData();
};

// 获取列表数据
const fetchListData = async () => {
  setLoading(true);
  const queryParamsCopy = { ...state.queryParams };

  const requestParams: any = {
    ...addDateRange(queryParamsCopy, state.dateRange),
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
  };

  if (state.queryParams.orderByColumn) {
    requestParams.orderByColumn = state.queryParams.orderByColumn;
  }
  if (state.queryParams.isAsc) {
    requestParams.isAsc = state.queryParams.isAsc;
  }

  const { rows, total } = await listMember(requestParams);
  state.list = rows;
  pagination.total = total;
  setLoading(false);
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
  state.selectName = (record as any).userName;
};

// 排序触发事件（后端排序）
const handleSorterChange = (
  dataIndex: string,
  direction: string | undefined
) => {
  if (!direction) {
    // 如果没有排序方向，清除排序参数
    state.queryParams.orderByColumn = undefined;
    state.queryParams.isAsc = undefined;
  } else {
    // 设置排序字段和排序方向（参考项目4的实现方式）
    if (dataIndex === 'createTime') {
      // 注册时间排序
      state.queryParams.orderByColumn = 'createTime';
    } else if (dataIndex === 'loginDate') {
      // 最后登录时间排序
      state.queryParams.orderByColumn = 'loginDate';
    }

    // 设置排序方向：将 Arco Design 的排序方向转换为后端需要的 asc/desc
    // 确保传给后端的是 'asc' 或 'desc'
    const directionStr = String(direction || '')
      .toLowerCase()
      .trim();

    // 使用 startsWith 判断，可以处理 ascend, ascending, asc, ascing 等所有以 asc 开头的值
    if (directionStr.startsWith('asc')) {
      state.queryParams.isAsc = 'asc';
    }
    // 使用 startsWith 判断，可以处理 descend, descending, desc 等所有以 desc 开头的值
    else if (directionStr.startsWith('desc')) {
      state.queryParams.isAsc = 'desc';
    }
    // 默认值，防止传错值
    else {
      state.queryParams.isAsc = 'asc';
    }
  }

  // 重置到第一页并重新获取数据
  pagination.current = 1;
  fetchListData();
};

// 打开新增
const handleAdd = () => {
  state.currentId = 0;
  state.editVisible = true;
};

// 打开编辑
const handleUpdate = (record: any) => {
  state.currentId = record.id;
  state.editVisible = true;
};

// 批量编辑（从工具栏编辑按钮触发）
const handleBatchEdit = () => {
  if (state.selectedKeys.length === 1) {
    state.currentId = state.selectedKeys[0] as number;
    state.editVisible = true;
  } else {
    Message.warning('请选择一条记录进行编辑');
  }
};

// 批量禁用
const handleBatchDisable = () => {
  if (state.selectedKeys.length <= 0) {
    Message.warning('请选择要禁用的用户');
    return;
  }

  // 获取选中用户的信息
  const selectedUsers = state.list.filter((item: any) =>
    state.selectedKeys.includes(item.id as string | number)
  );

  // 过滤出未禁用的用户（status !== '1'）
  const enabledUsers = selectedUsers.filter((user: any) => user.status !== '1');
  const disabledUsers = selectedUsers.filter(
    (user: any) => user.status === '1'
  );

  // 如果所有选中的用户都已禁用，提示用户
  if (enabledUsers.length === 0) {
    const disabledUserNames = disabledUsers
      .map(
        (user: any) =>
          user.username || user.nickName || user.userName || `ID:${user.id}`
      )
      .join('、');
    Message.warning(
      `所选用户【${disabledUserNames}】已经处于禁用状态，无需重复禁用`
    );
    return;
  }

  // 如果部分用户已禁用，提示用户
  let confirmMessage = '';
  if (disabledUsers.length > 0) {
    const disabledUserNames = disabledUsers
      .map(
        (user: any) =>
          user.username || user.nickName || user.userName || `ID:${user.id}`
      )
      .join('、');
    confirmMessage = `注意：以下用户已处于禁用状态，将跳过：\n${disabledUserNames}\n\n`;
  }

  const enabledUserNames = enabledUsers
    .map(
      (user: any) =>
        user.username || user.nickName || user.userName || `ID:${user.id}`
    )
    .join('、');
  confirmMessage += `确认要禁用以下用户吗？\n${enabledUserNames}`;

  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: confirmMessage,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        // 只禁用未禁用的用户
        const enabledUserIds = enabledUsers.map((user: any) => user.id);
        const promises = enabledUserIds.map(
          (userId: string | number) => updateMemberStatus(userId, '1') // '1' 表示禁用状态
        );

        await Promise.all(promises);

        // 更新本地状态
        state.list.forEach((item: any) => {
          if (enabledUserIds.includes(item.id as string | number)) {
            item.status = '1';
          }
        });

        // 清空选中状态
        state.selectedKeys = [];

        Message.success(`成功禁用 ${enabledUsers.length} 个用户`);
      } catch (error) {
        Message.error('批量禁用失败，请重试');
        console.error('批量禁用错误:', error);
      }
    },
  });
};

// 删除
const handleDelete = (record: any) => {
  const ids = record && record.id ? record.id : state.selectedKeys.join(',');
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: `是否确认删除访问编号为"${ids}"的数据项？`,
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      delMember(ids).then(() => {
        Message.success('删除成功');
        fetchListData();
      });
    },
  });
};

// 已上移，避免使用前定义

// 清空按钮操作
const handleClean = () => {
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: '是否确认清空所有会员数据项？',
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      delAllMember().then(() => {
        Message.success('清空成功');
        fetchListData();
      });
    },
  });
};

// 导出按钮操作
const handleExport = () => {
  download(
    'web/member/export',
    state.queryParams,
    `logininfor_${new Date().getTime()}.xlsx`
  );
};
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}
</style>
