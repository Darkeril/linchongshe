<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['闲宝管理', '分类管理']"></Breadcrumb>
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
                <a-form-item field="title" :label="'导航栏名称'">
                  <a-input
                    v-model="state.queryParams.title"
                    :placeholder="'请输入导航栏名称'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="status" :label="'状态'">
                  <a-select
                    v-model="state.queryParams.status"
                    placeholder="请选择"
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
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 42px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space :size="18">
            <a-button type="primary" @click="fetchNavbarData">
              <template #icon>
                <IconSearch />
              </template>
              查询
            </a-button>
            <a-button @click="resetQueryForm">
              <template #icon>
                <IconRefresh />
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
              <a-button type="primary" @click="handleAdd(0)">
                <template #icon>
                  <IconPlus />
                </template>
                新建
              </a-button>
              <a-button @click="toggleExpandAll">
                <template #icon>
                  <icon-shrink v-if="state.isExpandAll" />
                  <icon-expand v-if="!state.isExpandAll" />
                </template>
                <span v-if="state.isExpandAll">折叠</span>
                <span v-if="!state.isExpandAll">展开</span>
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-row>
      <!-- 表格数据-->
      <a-table
        ref="tableRef"
        row-key="id"
        :loading="loading"
        :columns="state.columns"
        :data="state.navbarList"
        :bordered="false"
        :size="'medium'"
      >
        <template #status="{ record }">
          <a-tag v-if="record.status === '1'" color="red">停用</a-tag>
          <a-tag v-else-if="record.status === '0'" color="blue">正常</a-tag>
          <a-tag v-else color="gray">未知</a-tag>
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button-group>
              <a-button
                type="text"
                size="mini"
                @click="handleUpdate(record.id)"
              >
                <template #icon>
                  <IconEdit />
                </template>
                修改
              </a-button>
              <a-button type="text" size="mini" @click="handleDelete(record)">
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
  <edit-navbar
    v-model:id="state.currentId"
    v-model:pid="state.currentParentId"
    v-model:visible="state.editVisible"
    @refresh-data-list="fetchNavbarData"
  >
  </edit-navbar>
</template>

<script lang="ts" setup>
import { computed, reactive, ref, h } from 'vue';
import AImage from '@arco-design/web-vue/es/image';
import { DictDataRecord, getDicts } from '@/api/system/dict-data';
import useLoading from '@/hooks/loading';
import {
  getIdleNavbarList,
  deleteIdleNavbar,
  updateIdleNavbarStatus,
} from '@/api/idle/navbar';
import { useRouter } from 'vue-router';
import ASwitch from '@arco-design/web-vue/es/switch';
import { Message, Modal } from '@arco-design/web-vue';
import { TableColumnData } from '@arco-design/web-vue/es/table/interface';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import EditNavbar from './components/edit-navbar.vue';

// 定义页面加载中效果变量
const { loading, setLoading } = useLoading(true);

const router = useRouter();

// 提前定义，避免 no-use-before-define
const toProductList = (row: any) => {
  router.push({ path: '/idle/product', query: { cid: row.id } });
};

const handleStatusChange = (record: any, nextStatus: string) => {
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: `确认要"${nextStatus === '0' ? '启用' : '禁用'}"【${
      record.title
    }】导航栏吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: () =>
      updateIdleNavbarStatus(record.id, Number(nextStatus)).then(() => {
        record.status = nextStatus;
        Message.success('操作成功');
      }),
  });
};
const state = reactive({
  queryParams: {
    title: undefined,
    status: undefined,
    params: undefined,
  },
  // 菜单列表数据
  navbarList: [],
  // 字典
  statusOptions: [] as DictDataRecord[],
  // 选中行ID
  currentId: 0,
  currentParentId: 0,
  // 显示编辑页面
  editVisible: false,
  // 是否全部展开
  isExpandAll: false,
  // 列表显示列
  columns: computed<TableColumnData[]>(() => [
    {
      title: '导航栏名称',
      dataIndex: 'title',
      slotName: 'title',
      width: 150,
      align: 'center',
    },
    {
      title: '导航栏封面',
      dataIndex: 'normalCover',
      slotName: 'normalCover',
      width: 120,
      align: 'center',
      render: ({ record }) =>
        h(AImage, {
          src: record.normalCover,
          width: '60px',
          height: '60px',
          preview: true, // 支持图片预览
          alt: '导航栏封面',
        }),
    },
    {
      title: '导航栏描述',
      dataIndex: 'description',
      slotName: 'description',
      width: 150,
      align: 'center',
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
      title: '排序',
      dataIndex: 'sort',
      slotName: 'sort',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 100,
      align: 'center',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      slotName: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 200,
      align: 'center',
      render: ({ record }) => {
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
      title: '更新时间',
      dataIndex: 'updateTime',
      slotName: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 200,
      align: 'center',
      render: ({ record }) => {
        const date = new Date(record.updateTime);
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
  const { data: statusData } = await getDicts('sys_normal_disable');
  state.statusOptions = statusData;
};
initData();

// 重置查询表单
const resetQueryForm = () => {
  state.queryParams = {
    title: undefined,
    status: undefined,
    params: undefined,
  };
  state.currentId = 0;
  state.currentParentId = 0;
  // eslint-disable-next-line no-use-before-define
  fetchNavbarData();
};

// 数组转树结构集合
const listToTree: any = (list: Array<any>) => {
  const map: any = {};
  const treeData: Array<any> = [];
  for (let i = 0; i < list.length; i += 1) {
    map[list[i].id] = i;
    list[i].children = [];
  }
  for (let i = 0; i < list.length; i += 1) {
    const node = list[i];
    if (node.pid && list[map[node.pid]]) {
      list[map[node.pid]].children.push(node);
    } else {
      treeData.push(node);
    }
  }
  return treeData;
};

// 获取菜单数据
const fetchNavbarData = async () => {
  setLoading(true);
  try {
    const { data } = await getIdleNavbarList({
      ...state.queryParams,
    });
    const list = Array.isArray(data)
      ? data
      : data?.list || data?.rows || data?.records || [];
    state.navbarList = listToTree(list);
  } catch (err) {
    console.error('获取导航栏数据失败:', err);
  } finally {
    setLoading(false);
  }
};
fetchNavbarData();

// 打开新增
const handleAdd = (id: number) => {
  state.currentId = 0;
  state.currentParentId = id;
  state.editVisible = true;
};

// 打开编辑
const handleUpdate = (id: number) => {
  state.currentId = id;
  state.currentParentId = id;
  state.editVisible = true;
};

// 删除菜单
const handleDelete = (row: any) => {
  Modal.confirm({
    titleAlign: 'start',
    title: '系统提示',
    content: `是否确认删除导航栏名称为"${row.title}"的数据项？`,
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      deleteIdleNavbar(row.id as number).then(() => {
        Message.success('删除成功');
        fetchNavbarData();
      });
    },
  });
};

// 页面表格元素选择
const tableRef = ref();
// 展开/折叠操作
const toggleExpandAll = () => {
  state.isExpandAll = !state.isExpandAll;
  if (tableRef.value) {
    tableRef.value.expandAll(state.isExpandAll);
  }
};
</script>

<style scoped lang="less">
.container {
  padding: 0 20px 20px 20px;
}
</style>
