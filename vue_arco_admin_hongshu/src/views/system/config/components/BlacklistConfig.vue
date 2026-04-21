<template>
  <div>
    <!-- 黑名单开关 -->
    <a-form
      :model="blacklistForm"
      style="margin-left: 20px; margin-bottom: 20px"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>黑名单功能配置</h4>
      </div>
      <a-form-item label="黑名单功能">
        <a-switch
          :model-value="blacklistForm.enabled"
          @update:model-value="handleToggle"
        />
        <div class="form-tip">启用后可以管理IP黑名单</div>
      </a-form-item>
    </a-form>

    <!-- 自动封禁配置 -->
    <a-form
      v-if="blacklistForm.enabled"
      :model="autobanForm"
      style="margin-left: 20px; margin-bottom: 20px"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>自动封禁配置</h4>
      </div>
      <a-form-item label="自动封禁">
        <a-switch v-model="autobanForm.enabled" />
        <div class="form-tip">启用后，系统将自动检测高频登录IP并封禁</div>
      </a-form-item>
      <template v-if="autobanForm.enabled">
        <a-form-item label="失败阈值">
          <a-input-number
            v-model="autobanForm.failThreshold"
            :min="1"
            :max="1000"
            style="width: 200px"
          />
          <div class="form-tip"
            >同一IP在检测窗口内登录失败达到此次数时自动封禁</div
          >
        </a-form-item>
        <a-form-item label="成功阈值">
          <a-input-number
            v-model="autobanForm.successThreshold"
            :min="1"
            :max="1000"
            style="width: 200px"
          />
          <div class="form-tip"
            >同一IP在检测窗口内登录成功达到此次数时自动封禁（防频繁登录退出）</div
          >
        </a-form-item>
        <a-form-item label="检测窗口">
          <a-input-number
            v-model="autobanForm.timeWindowMinutes"
            :min="1"
            :max="1440"
            style="width: 200px"
          />
          <div class="form-tip">检测时间窗口（分钟）</div>
        </a-form-item>
        <a-form-item label="封禁时长">
          <a-input-number
            v-model="autobanForm.banHours"
            :min="0"
            :max="8760"
            style="width: 200px"
          />
          <div class="form-tip">自动封禁时长（小时），设为 0 表示永久封禁</div>
        </a-form-item>
      </template>
      <a-form-item>
        <a-button
          type="primary"
          :loading="autobanSaving"
          @click="handleSaveAutoban"
          >保存自动封禁配置</a-button
        >
      </a-form-item>
    </a-form>

    <!-- 黑名单列表 -->
    <div v-if="blacklistForm.enabled" style="margin-left: 20px">
      <div class="form-section">
        <h4>IP黑名单列表 ({{ blacklistForm.list.length }} 条记录)</h4>
      </div>
      <div style="margin-bottom: 16px">
        <a-button type="primary" @click="showAddDialog">
          <icon-plus />
          添加IP黑名单
        </a-button>
        <a-button style="margin-left: 8px" @click="loadData">
          刷新列表
        </a-button>
      </div>

      <!-- 黑名单表格 -->
      <a-table
        :data="blacklistForm.list"
        :columns="tableColumns"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #status="{ record }">
          <a-tag :color="record.status === '1' ? 'red' : 'green'">
            {{ record.status === '1' ? '封禁' : '已解封' }}
          </a-tag>
        </template>
        <template #banTime="{ record }">
          {{ formatTime(record.banTime) }}
        </template>
        <template #expireTime="{ record }">
          {{ formatTime(record.expireTime) }}
        </template>
        <template #operations="{ record }">
          <a-button
            size="small"
            type="primary"
            status="danger"
            @click="handleDelete(record)"
          >
            删除
          </a-button>
          <a-button
            size="small"
            type="primary"
            status="warning"
            :disabled="record.status !== '1'"
            style="margin-left: 8px"
            @click="handleUnban(record)"
          >
            解封
          </a-button>
        </template>
      </a-table>
    </div>

    <!-- 添加黑名单对话框 -->
    <a-modal
      v-model:visible="dialogVisible"
      title="添加IP黑名单"
      @ok="handleAdd"
      @cancel="handleCancel"
    >
      <a-form
        :model="form"
        :label-col-props="{ span: 6 }"
        :wrapper-col-props="{ span: 18 }"
      >
        <a-form-item label="IP地址" field="ipAddress">
          <a-input v-model="form.ipAddress" placeholder="请输入IP地址" />
        </a-form-item>
        <a-form-item label="封禁原因" field="reason">
          <a-textarea
            v-model="form.reason"
            :rows="3"
            placeholder="请输入封禁原因"
          />
        </a-form-item>
        <a-form-item label="解封时间" field="expireTime">
          <a-date-picker
            v-model="form.expireTime"
            show-time
            placeholder="选择解封时间（可选）"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus } from '@arco-design/web-vue/es/icon';
import { currentLocale } from '@/utils/common';
import {
  getBlacklistConfig,
  addBlacklistItem,
  updateBlacklistEnabled,
  unbanBlacklistItem,
  deleteBlacklistItem,
  getAutobanConfig,
  updateAutobanConfig,
} from '@/api/system/config';

// Props
interface Props {
  initialData?: any;
}

const props = withDefaults(defineProps<Props>(), {
  initialData: () => ({}),
});

// Emits
const emit = defineEmits<{
  (e: 'update'): void;
}>();

// 表单数据
const blacklistForm = reactive({
  enabled: false,
  list: [] as any[],
});

const form = reactive({
  ipAddress: '',
  reason: '',
  expireTime: '',
});

// 自动封禁表单
const autobanForm = reactive({
  enabled: false,
  failThreshold: 10,
  successThreshold: 30,
  timeWindowMinutes: 60,
  banHours: 24,
});
const autobanSaving = ref(false);

// 状态
const loading = ref(false);
const dialogVisible = ref(false);

// 表格列定义
const tableColumns = [
  { title: '编号', dataIndex: 'id', width: 80, align: 'center' as const },
  { title: 'IP地址', dataIndex: 'ipAddress', width: 150, align: 'center' as const },
  { title: '封禁原因', dataIndex: 'reason', align: 'center' as const },
  { title: '状态', dataIndex: 'status', width: 100, align: 'center' as const, slotName: 'status' },
  { title: '封禁时间', dataIndex: 'banTime', width: 200, align: 'center' as const, slotName: 'banTime' },
  { title: '解封时间', dataIndex: 'expireTime', width: 200, align: 'center' as const, slotName: 'expireTime' },
  { title: '操作', width: 180, align: 'center' as const, slotName: 'operations' },
];

// 格式化时间
const formatTime = (time: string | number | Date): string => {
  if (!time) return '-';
  const date = new Date(time);
  return date.toLocaleString(currentLocale());
};

// 加载黑名单数据
const loadData = async (): Promise<void> => {
  try {
    loading.value = true;
    const response: any = await getBlacklistConfig();
    const data = response.data ?? response;
    if (data && Array.isArray(data.list)) {
      blacklistForm.list = data.list;
    }
  } catch (error) {
    console.error('加载黑名单数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 加载自动封禁配置
const loadAutobanConfig = async (): Promise<void> => {
  try {
    const response: any = await getAutobanConfig();
    const data = response.data ?? response;
    if (data) {
      Object.assign(autobanForm, data);
    }
  } catch (error) {
    console.error('加载自动封禁配置失败:', error);
  }
};

// 切换黑名单功能
const handleToggle = async (enabled: boolean): Promise<void> => {
  try {
    blacklistForm.enabled = enabled;
    const response = await updateBlacklistEnabled(enabled);
    if (response.code === 200) {
      Message.success(enabled ? '黑名单功能已启用' : '黑名单功能已禁用');
      if (enabled) {
        await loadData();
        await loadAutobanConfig();
      }
    } else {
      Message.error((response as any).message || '操作失败');
    }
    emit('update');
  } catch (error) {
    console.error('切换黑名单功能失败:', error);
    Message.warning('操作接口暂未实现，状态已更新到本地');
  }
};

// 显示添加对话框
const showAddDialog = (): void => {
  form.ipAddress = '';
  form.reason = '';
  form.expireTime = '';
  dialogVisible.value = true;
};

// 添加黑名单
const handleAdd = async (): Promise<void> => {
  try {
    if (!form.ipAddress || !form.reason) {
      Message.error('请填写IP地址和封禁原因');
      return;
    }

    const response = await addBlacklistItem({
      ipAddress: form.ipAddress,
      reason: form.reason,
      expireTime: form.expireTime,
    });

    if (response.code === 200) {
      Message.success('添加黑名单成功');
      dialogVisible.value = false;
      await loadData();
      emit('update');
    } else {
      Message.error((response as any).message || '添加黑名单失败');
    }
  } catch (error) {
    console.error('添加黑名单失败:', error);
    Message.warning('添加黑名单接口暂未实现');
  }
};

// 解封黑名单
const handleUnban = async (record: any): Promise<void> => {
  try {
    const response = await unbanBlacklistItem(record.id);
    if (response.code === 200) {
      Message.success('解封成功');
      await loadData();
      emit('update');
    } else {
      Message.error((response as any).message || '解封失败');
    }
  } catch (error) {
    console.error('解封黑名单失败:', error);
    Message.warning('解封接口暂未实现');
  }
};

// 删除黑名单
const handleDelete = async (record: any): Promise<void> => {
  try {
    const response = await deleteBlacklistItem(record.id);
    if (response.code === 200) {
      Message.success('删除成功');
      await loadData();
      emit('update');
    } else {
      Message.error((response as any).message || '删除失败');
    }
  } catch (error) {
    console.error('删除黑名单失败:', error);
    Message.warning('删除接口暂未实现');
  }
};

// 取消
const handleCancel = (): void => {
  dialogVisible.value = false;
};

// 保存自动封禁配置
const handleSaveAutoban = async (): Promise<void> => {
  try {
    autobanSaving.value = true;
    const response = await updateAutobanConfig({ ...autobanForm });
    if (response.code === 200) {
      Message.success('自动封禁配置保存成功');
    } else {
      Message.error('保存失败');
    }
  } catch (error) {
    console.error('保存自动封禁配置失败:', error);
    Message.error('保存自动封禁配置失败');
  } finally {
    autobanSaving.value = false;
  }
};

// 组件挂载时直接加载数据，不依赖父组件传递
onMounted(async () => {
  try {
    loading.value = true;
    const response: any = await getBlacklistConfig();
    const data = response.data ?? response;
    if (data) {
      blacklistForm.enabled = !!data.enabled;
      if (Array.isArray(data.list)) {
        blacklistForm.list = data.list;
      }
    }
    if (blacklistForm.enabled) {
      await loadAutobanConfig();
    }
  } catch (error) {
    console.error('初始化黑名单配置失败:', error);
  } finally {
    loading.value = false;
  }
});

// 暴露方法供父组件调用
defineExpose({
  form: blacklistForm,
  loadData,
});
</script>

<style scoped lang="less">
.form-section {
  margin-bottom: 20px;

  h4 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 500;
    color: #1d2129;
  }
}

.form-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
  margin-left: 4px;
}
</style>



