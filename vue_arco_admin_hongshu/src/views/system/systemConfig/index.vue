<template>
  <div class="container">
    <a-card class="general-card" title="系统配置">
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
                <a-form-item field="configName" label="配置名称">
                  <a-input
                    v-model="formModel.configName"
                    placeholder="请输入配置名称"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="configKey" label="配置键">
                  <a-input
                    v-model="formModel.configKey"
                    placeholder="请输入配置键"
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
                      搜索
                    </a-button>
                    <a-button @click="reset">
                      <template #icon>
                        <icon-refresh />
                      </template>
                      重置
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
            <a-button type="primary" @click="handleAdd">
              <template #icon>
                <icon-plus />
              </template>
              新增
            </a-button>
            <a-button @click="handleRefreshCache">
              <template #icon>
                <icon-refresh />
              </template>
              刷新缓存
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
        <template #configType="{ record }">
          <a-tag v-if="record.configType === 'Y'" color="green">
            是
          </a-tag>
          <a-tag v-else color="red">
            否
          </a-tag>
        </template>
        <template #operations="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="handleEdit(record)">
              编辑
            </a-button>
            <a-button type="text" size="small" @click="handleDelete(record)">
              删除
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, reactive, onMounted } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { Pagination } from '@/types/global';
  import { IconSearch, IconRefresh, IconPlus } from '@arco-design/web-vue/es/icon';
  import { getSystemConfigList, deleteSystemConfig, refreshCache } from '@/api/system/systemConfig';

  const generateFormModel = () => {
    return {
      configName: '',
      configKey: '',
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
      title: 'ID',
      dataIndex: 'id',
    },
    {
      title: '配置名称',
      dataIndex: 'configName',
    },
    {
      title: '配置键',
      dataIndex: 'configKey',
    },
    {
      title: '配置值',
      dataIndex: 'configValue',
    },
    {
      title: '配置类型',
      dataIndex: 'configType',
      slotName: 'configType',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
    },
    {
      title: '操作',
      dataIndex: 'operations',
      slotName: 'operations',
    },
  ]);
  const renderData = ref([]);
  const fetchData = async (
    params: any = { current: 1, pageSize: 20 }
  ) => {
    loading.value = true;
    try {
      const { data } = await getSystemConfigList(params);
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
  const handleAdd = () => {
    // TODO: 实现添加系统配置逻辑
    Message.info('添加系统配置功能待实现');
  };
  const handleEdit = (record: any) => {
    // TODO: 实现编辑系统配置逻辑
    Message.info('编辑系统配置功能待实现');
  };
  const handleDelete = async (record: any) => {
    try {
      await deleteSystemConfig(record.id);
      Message.success('删除成功');
      fetchData();
    } catch (error) {
      Message.error('删除失败');
    }
  };
  const handleRefreshCache = async () => {
    try {
      await refreshCache();
      Message.success('缓存刷新成功');
    } catch (error) {
      Message.error('缓存刷新失败');
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


