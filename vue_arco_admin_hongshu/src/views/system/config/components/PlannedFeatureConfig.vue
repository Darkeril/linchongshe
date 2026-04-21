<template>
  <div>
    <!-- 待实现功能列表 -->
    <div style="margin-left: 20px">
      <div style="margin-bottom: 20px">
        <a-button type="primary" @click="showAddDialog">
          <icon-plus />
          添加待实现功能
        </a-button>
        <a-button type="outline" style="margin-left: 8px" @click="loadList">
          <icon-refresh />
          刷新
        </a-button>
      </div>

      <!-- 待实现功能表格 -->
      <a-table
        :columns="columns"
        :data="featureList"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1000 }"
      >
        <template #empty>
          <div style="text-align: center; padding: 40px; color: #999">
            暂无待实现功能数据
          </div>
        </template>
        <template #status="{ record }">
          <a-tag
            :color="
              record.status === '已完成'
                ? 'green'
                : record.status === '开发中'
                ? 'blue'
                : 'orange'
            "
          >
            {{ record.status }}
          </a-tag>
        </template>
        <template #isShow="{ record }">
          <a-tag :color="record.isShow === 1 ? 'green' : 'red'">
            {{ record.isShow === 1 ? '显示' : '隐藏' }}
          </a-tag>
        </template>
        <template #createTime="{ record }">
          <span>{{ formatTime(record.createTime) }}</span>
        </template>
        <template #operations="{ record }">
          <a-button size="small" type="primary" @click="handleEdit(record)">
            编辑
          </a-button>
          <a-button
            size="small"
            type="primary"
            status="danger"
            style="margin-left: 8px"
            @click="handleDelete(record)"
          >
            删除
          </a-button>
        </template>
      </a-table>
    </div>

    <!-- 添加/编辑待实现功能对话框 -->
    <a-modal
      v-model:visible="dialogVisible"
      :title="isEdit ? '编辑待实现功能' : '添加待实现功能'"
      width="600px"
      @ok="handleSave"
      @cancel="dialogVisible = false"
    >
      <a-form
        :model="form"
        :label-col-props="{ span: 6 }"
        :wrapper-col-props="{ span: 18 }"
      >
        <a-form-item label="功能标题" field="title" required>
          <a-input
            v-model="form.title"
            placeholder="请输入功能标题"
            :max-length="200"
            show-word-limit
          />
        </a-form-item>
        <a-form-item label="功能描述" field="description" required>
          <a-textarea
            v-model="form.description"
            :rows="4"
            placeholder="请输入功能描述"
            :max-length="500"
            show-word-limit
          />
        </a-form-item>
        <a-form-item label="状态" field="status" required>
          <a-select v-model="form.status" placeholder="请选择状态">
            <a-option value="规划中">规划中</a-option>
            <a-option value="开发中">开发中</a-option>
            <a-option value="已完成">已完成</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="排序" field="sort">
          <a-input-number
            v-model="form.sort"
            :min="0"
            placeholder="数字越小越靠前"
            style="width: 100%"
          />
          <div class="form-tip">数字越小越靠前，默认为0</div>
        </a-form-item>
        <a-form-item label="显示状态" field="isShow">
          <a-switch
            v-model="form.isShow"
            :checked-value="1"
            :unchecked-value="0"
          />
          <div class="form-tip">启用后在前端显示该功能</div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus, IconRefresh } from '@arco-design/web-vue/es/icon';
import { currentLocale } from '@/utils/common';
import type { TableColumnData } from '@arco-design/web-vue';
import {
  getPlannedFeatureList,
  addPlannedFeature,
  updatePlannedFeature,
  deletePlannedFeature,
} from '@/api/system/config';

// Emits
const emit = defineEmits<{
  (e: 'update'): void;
}>();

// 状态
const featureList = ref<any[]>([]);
const loading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);

// 表格列配置
const columns: TableColumnData[] = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
    align: 'center',
  },
  {
    title: '功能标题',
    dataIndex: 'title',
    width: 200,
    align: 'center',
  },
  {
    title: '功能描述',
    dataIndex: 'description',
    align: 'center',
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 120,
    align: 'center',
    slotName: 'status',
  },
  {
    title: '排序',
    dataIndex: 'sort',
    width: 100,
    align: 'center',
  },
  {
    title: '显示状态',
    dataIndex: 'isShow',
    width: 120,
    align: 'center',
    slotName: 'isShow',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
    align: 'center',
    slotName: 'createTime',
  },
  {
    title: '操作',
    width: 200,
    align: 'center',
    slotName: 'operations',
  },
];

// 表单数据
const form = reactive({
  id: null as number | null,
  title: '',
  description: '',
  status: '规划中',
  sort: 0,
  isShow: 1,
});

// 格式化时间
const formatTime = (time: string | number): string => {
  if (!time) return '-';
  return new Date(time).toLocaleString(currentLocale());
};

// 加载待实现功能列表
const loadList = async (): Promise<void> => {
  try {
    loading.value = true;
    const response = await getPlannedFeatureList();
    if (response.code === 200 && response.data) {
      featureList.value = response.data;
    } else {
      Message.warning((response as any).message || '获取数据失败');
    }
  } catch (error) {
    console.error('加载待实现功能列表失败:', error);
    Message.error('加载待实现功能列表失败');
  } finally {
    loading.value = false;
  }
};

// 显示添加待实现功能对话框
const showAddDialog = (): void => {
  isEdit.value = false;
  Object.assign(form, {
    id: null,
    title: '',
    description: '',
    status: '规划中',
    sort: 0,
    isShow: 1,
  });
  dialogVisible.value = true;
};

// 编辑待实现功能
const handleEdit = (record: any): void => {
  isEdit.value = true;
  Object.assign(form, {
    id: record.id,
    title: record.title,
    description: record.description,
    status: record.status,
    sort: record.sort,
    isShow: record.isShow,
  });
  dialogVisible.value = true;
};

// 保存待实现功能
const handleSave = async (): Promise<void> => {
  try {
    if (!form.title.trim()) {
      Message.error('请输入功能标题');
      return;
    }
    if (!form.description.trim()) {
      Message.error('请输入功能描述');
      return;
    }

    let response;
    if (isEdit.value) {
      response = await updatePlannedFeature(form);
    } else {
      response = await addPlannedFeature(form);
    }

    if (response.code === 200) {
      Message.success(
        isEdit.value ? '待实现功能更新成功' : '待实现功能添加成功'
      );
      dialogVisible.value = false;
      await loadList();
      emit('update');
    } else {
      Message.error((response as any).message || '操作失败');
    }
  } catch (error) {
    console.error('保存待实现功能失败:', error);
    Message.warning('待实现功能接口暂未实现');
  }
};

// 删除待实现功能
const handleDelete = async (record: any): Promise<void> => {
  try {
    const response = await deletePlannedFeature(record.id);
    if (response.code === 200) {
      Message.success('删除成功');
      await loadList();
      emit('update');
    } else {
      Message.error((response as any).message || '删除失败');
    }
  } catch (error) {
    console.error('删除待实现功能失败:', error);
    Message.warning('删除接口暂未实现');
  }
};

// 生命周期
onMounted(() => {
  loadList();
});

// 暴露方法供父组件调用
defineExpose({
  refresh: loadList,
  load: loadList,
});
</script>

<style scoped lang="less">
.form-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}
</style>




