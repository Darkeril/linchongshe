<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['消息管理', '评论管理']"></Breadcrumb>
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
                <a-form-item field="uid" :label="'评论人'">
                  <a-input
                    v-model="state.queryParams.uid"
                    :placeholder="'请输入评论人'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="noteUid" :label="'被评论人'">
                  <a-input
                    v-model="state.queryParams.noteUid"
                    :placeholder="'请输入被评论人'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="nid" :label="'被评论笔记'">
                  <a-input
                    v-model="state.queryParams.nid"
                    :placeholder="'请输入被评论笔记'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="content" :label="'评论内容'">
                  <a-input
                    v-model="state.queryParams.content"
                    :placeholder="'请输入评论内容'"
                  >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="dateRange" :label="'评论时间'">
                  <a-range-picker v-model="state.dateRange" style="width: 100%">
                  </a-range-picker>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 42px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space :size="18">
            <a-button type="primary" @click="fetchListData">
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
              <a-space>
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
              </a-space>
            </a-space>
          </a-col>
        </a-row>
      </a-row>

      <!-- 调试信息 -->
      <div style="margin-bottom: 10px; padding: 10px; background: #f0f0f0; border-radius: 4px;">
        <strong>调试信息：</strong>
        数据条数: {{ state.list.length }} | 
        总数: {{ pagination.total }} | 
        当前页: {{ pagination.current }} | 
        页大小: {{ pagination.pageSize }}
      </div>

      <!-- 表格数据-->
      <a-table
        :key="`${state.list.length}-${pagination.current}-${pagination.pageSize}`"
        ref="tableRef"
        v-model:selectedKeys="state.selectedKeys"
        row-key="id"
        :loading="loading"
        :pagination="pagination"
        :columns="state.columns"
        :data="state.list || []"
        :row-selection="state.rowSelection"
        :bordered="false"
        :size="'medium'"
        @select="handleSelected"
        @sorter-change="handleSorterChange"
        @page-change="onPageChange"
      >
        <template #optional="{ record }">
          <a-space>
            <a-button-group>
              <a-button
                v-permission="['web:category:remove']"
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
</template>

<script lang="ts" setup>
  import useLoading from '@/hooks/loading';
  import { computed, reactive, h } from 'vue';
  import { Message, Modal } from '@arco-design/web-vue';
  import { Pagination } from '@/types/global';
  import addDateRange from '@/utils/common';
  import AImage from '@arco-design/web-vue/es/image';
  import { listComment, delComment } from '@/api/web/comment';
  import { download } from '@/api/download';
  import {
    TableColumnData,
    TableData,
  } from '@arco-design/web-vue/es/table/interface';

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
      uid: undefined,
      nid: undefined,
      noteUid: undefined,
      content: undefined,
    },
    rowSelection: {
      type: 'checkbox' as const,
      showCheckedAll: true,
      onlyCurrent: false,
    },
    // 表格选中值
    selectedKeys: [],
    selectName: '',
    // 时间选择
    dateRange: [],
    // 显示编辑页面
    editVisible: false,
    // 列表数据
    list: [] as any[],
    // 字典
    dictOptions: {},
    // 选中行ID
    currentId: 0,
    // 列表显示列
    columns: computed<TableColumnData[]>(() => [
      {
        title: '编号',
        dataIndex: 'id',
        slotName: 'id',
        width: 80,
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
        title: '评论人',
        dataIndex: 'username',
        slotName: 'username',
        width: 100,
        align: 'center',
      },
      {
        title: '被评论人',
        dataIndex: 'pushUsername',
        slotName: 'pushUsername',
        width: 100,
        align: 'center',
      },
      {
        title: '被评论笔记',
        dataIndex: 'title',
        slotName: 'title',
        width: 100,
        align: 'center',
      },
      {
        title: '评论内容',
        dataIndex: 'content',
        slotName: 'content',
        width: 100,
        align: 'center',
      },
      {
        title: '评论等级',
        dataIndex: 'level',
        slotName: 'level',
        width: 100,
        align: 'center',
      },
      {
        title: '评论时间',
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
        title: '操作',
        dataIndex: 'optional',
        slotName: 'optional',
        fixed: 'right',
        width: 200,
        align: 'center',
      },
    ]),
  });

  // 重置查询表单
  const resetQueryForm = () => {
    state.queryParams = {
      uid: undefined,
      nid: undefined,
      noteUid: undefined,
      content: undefined,
    };
    state.selectName = '';
    state.dateRange = [];
  };

  // 获取列表数据
  const fetchListData = async () => {
    setLoading(true);
    try {
      const params = {
        current: pagination.current,
        pageSize: pagination.pageSize,
        ...state.queryParams,
        ...addDateRange(state.queryParams, state.dateRange),
      };
      
      console.log('Comment API params:', params);
      
      const res = await listComment(params);
      
      console.log('Comment API response:', res);
      
      // 处理不同的数据结构
      let rows, total;
      if (res?.data?.rows) {
        // 格式: { data: { rows: [...], total: 20 } }
        rows = res.data.rows;
        total = res.data.total;
      } else if (res?.rows) {
        // 格式: { rows: [...], total: 20 }
        rows = res.rows;
        total = (res as any).total;
      } else if (Array.isArray(res?.data)) {
        // 格式: { data: [...] }
        rows = res.data;
        total = res.data.length;
      } else if (res?.data?.records) {
        // 格式: { data: { records: [...], total: 20 } }
        rows = res.data.records;
        total = res.data.total;
      } else {
        // 其他格式
        rows = [];
        total = 0;
      }
      
      state.list = Array.isArray(rows) ? rows : [];
      pagination.total = typeof total === 'number' ? total : state.list.length;
      
      console.log('Parsed comment data:', { rows: state.list.length, total: pagination.total });
    } catch (error) {
      console.error('Failed to load comment data:', error);
      state.list = [];
      pagination.total = 0;
    } finally {
      setLoading(false);
    }
  };
  fetchListData();

  // 分页
  const onPageChange = (current: number) => {
    pagination.current = current;
    fetchListData();
  };

  // 多选框选中数据
  const handleSelected = (
    rowKeys: Array<string | number>,
    rowKey: string | number,
    record: TableData
  ) => {
    state.selectName = (record as any).userName;
  };

  // 排序触发事件
  const handleSorterChange = (dataIndex: string, direction: string) => {
    // state.queryParams.orderByColumn = dataIndex;
    // state.queryParams.isAsc = direction;
    fetchListData();
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
        delComment(ids).then(() => {
          Message.success('删除成功');
          fetchListData();
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
