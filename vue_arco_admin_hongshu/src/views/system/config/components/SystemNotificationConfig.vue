<template>
  <div style="margin-left: 20px">
    <div class="form-section">
      <h4>系统通知列表 ({{ notificationList.length }} 条)</h4>
    </div>
    <div style="margin-bottom: 16px">
      <a-button type="primary" @click="showAddDialog">
        <icon-plus />
        添加系统通知
      </a-button>
      <a-button style="margin-left: 8px" @click="refreshList">
        <icon-refresh />
        刷新列表
      </a-button>
    </div>

    <a-table
      :data="notificationList"
      :columns="tableColumns"
      :loading="loading"
      :pagination="false"
      row-key="id"
    >
      <template #idCol="{ record }">
        <span class="mono-id" :title="record.id">{{ shortId(record.id) }}</span>
      </template>
      <template #enabledCol="{ record }">
        <a-tag :color="record.enabled ? 'green' : 'red'">
          {{ record.enabled ? '启用' : '禁用' }}
        </a-tag>
      </template>
      <template #timeCol="{ record }">
        {{ formatTime(record.timestamp) }}
      </template>
      <template #imageCol="{ record }">
        <div v-if="record.imageUrl" class="thumb-wrap">
          <img
            class="thumb-img"
            :src="record.imageUrl"
            alt=""
            @error="onThumbError"
          />
        </div>
        <span v-else class="thumb-empty">-</span>
      </template>
      <template #operations="{ record }">
        <a-button size="small" type="primary" @click="handleEdit(record)">
          编辑
        </a-button>
        <a-button
          size="small"
          type="primary"
          status="success"
          :disabled="!record.enabled"
          style="margin-left: 8px"
          @click="handlePush(record)"
        >
          推送
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

    <!-- 添加/编辑系统通知（布局与「通知管理」新增公告一致：栅格 + 富文本 + 单选状态） -->
    <a-modal
      v-model:visible="dialogVisible"
      :title="isEdit ? '编辑系统通知' : '添加系统通知'"
      width="50vw"
      :mask-closable="false"
      unmount-on-close
      class="system-notification-config-modal"
      @ok="handleSave"
      @cancel="dialogVisible = false"
    >
      <a-form :model="form" auto-label-width>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="推送说明" field="description">
              <a-input
                v-model="form.description"
                placeholder="选填，列表/推送标题摘要"
                allow-clear
                :max-length="200"
                show-word-limit
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态" field="enabled">
              <a-radio-group v-model="form.enabled" type="button">
                <a-radio :value="true">启用</a-radio>
                <a-radio :value="false">禁用</a-radio>
              </a-radio-group>
              <div class="form-tip">启用后可推送给 C 端用户</div>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="通知内容" field="content">
              <editor
                v-if="dialogVisible"
                :key="editorMountKey"
                v-model:value="form.content"
                mode="simple"
                :min-height="360"
              />
              <div class="form-tip">
                支持富文本；与配图至少填一项。纯文本建议不超过 2000 字。
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="配图" field="imageUrl">
              <div class="notice-image-row">
                <div
                  class="notice-image-preview"
                  :class="{ 'notice-image-preview--empty': !form.imageUrl?.trim() }"
                >
                  <img
                    v-if="form.imageUrl?.trim()"
                    :key="form.imageUrl"
                    :src="form.imageUrl"
                    alt=""
                    class="notice-image-preview-img"
                    @error="onFormPreviewError"
                  />
                  <span v-else class="notice-image-placeholder">未设置配图</span>
                </div>
                <div class="notice-image-fields">
                  <a-input
                    v-model="form.imageUrl"
                    placeholder="图片 URL（https://）"
                    allow-clear
                  />
                  <div class="notice-image-actions">
                    <a-upload
                      :show-file-list="false"
                      accept="image/jpeg,image/png,image/jpg,image/gif,image/webp"
                      :custom-request="handleNoticeImageUpload"
                    >
                      <template #upload-button>
                        <a-button type="outline" size="small" :loading="imageUploading">
                          <icon-upload />
                          上传图片
                        </a-button>
                      </template>
                    </a-upload>
                  </div>
                </div>
              </div>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, toRaw } from 'vue';
import { Message } from '@arco-design/web-vue';
import { currentLocale } from '@/utils/common';
import { IconPlus, IconRefresh, IconUpload } from '@arco-design/web-vue/es/icon';
import Editor from '@/components/editor/index.vue';
import {
  getSystemNotificationList,
  addSystemNotification,
  updateSystemNotification,
  deleteSystemNotification,
  pushSystemNotification,
  uploadBatchFiles,
} from '@/api/system/config';

// Emits
const emit = defineEmits<{
  (e: 'update'): void;
}>();

// 状态
const notificationList = ref<any[]>([]);
const loading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
/** 每次打开弹窗递增，确保富文本编辑器重新挂载并同步 form.content */
const editorMountKey = ref(0);

function htmlToPlainText(html: string): string {
  if (!html || typeof html !== 'string') return '';
  if (typeof document === 'undefined') {
    return html.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim();
  }
  const d = document.createElement('div');
  d.innerHTML = html;
  return (d.textContent || d.innerText || '').replace(/\s+/g, ' ').trim();
}

watch(dialogVisible, (open) => {
  if (open) {
    editorMountKey.value += 1;
  }
});

const tableColumns = [
  {
    title: '编号',
    dataIndex: 'id',
    width: 150,
    align: 'center' as const,
    slotName: 'idCol',
  },
  {
    title: '通知内容',
    dataIndex: 'content',
    width: 200,
    align: 'center' as const,
    ellipsis: true,
    tooltip: true,
  },
  {
    title: '配图',
    width: 72,
    align: 'center' as const,
    slotName: 'imageCol',
  },
  {
    title: '状态',
    width: 100,
    align: 'center' as const,
    slotName: 'enabledCol',
  },
  {
    title: '创建时间',
    dataIndex: 'timestamp',
    width: 150,
    align: 'center' as const,
    slotName: 'timeCol',
  },
  {
    title: '说明',
    dataIndex: 'description',
    width: 150,
    align: 'center' as const,
    ellipsis: true,
    tooltip: true,
  },
  {
    title: '操作',
    width: 220,
    align: 'center' as const,
    slotName: 'operations',
  },
];

// 表单数据
const form = reactive({
  id: '',
  content: '',
  imageUrl: '',
  enabled: true,
  description: '',
});

/** UTF-8 转 Base64，供 contentBase64 传输富文本，避免 HTML 属性引号破坏 JSON */
function utf8ToBase64(text: string): string {
  const bytes = new TextEncoder().encode(text);
  let binary = '';
  for (let i = 0; i < bytes.length; i += 1) {
    binary += String.fromCharCode(bytes[i]!);
  }
  return btoa(binary);
}

/** 仅提交后端 VO 所需字段；正文走 contentBase64，content 固定空串以减小非法 JSON 风险 */
function buildSystemNotificationPayload(): {
  id: string;
  content: string;
  contentBase64: string;
  imageUrl: string;
  enabled: boolean;
  description: string;
} {
  const f = toRaw(form);
  let content = '';
  if (f.content != null) {
    content = typeof f.content === 'string' ? f.content : String(f.content);
  }
  return {
    id: f.id == null ? '' : String(f.id),
    content: '',
    contentBase64: utf8ToBase64(content),
    imageUrl: f.imageUrl == null ? '' : String(f.imageUrl).trim(),
    enabled: f.enabled === true || f.enabled === 'true' || f.enabled === 1,
    description: f.description == null ? '' : String(f.description),
  };
}

const imageUploading = ref(false);

function onThumbError(e: Event) {
  const el = e.target as HTMLImageElement;
  if (el) el.style.visibility = 'hidden';
}

function onFormPreviewError(e: Event) {
  const el = e.target as HTMLImageElement;
  if (el) el.classList.add('notice-image-preview-img--broken');
}

async function handleNoticeImageUpload(options: {
  fileItem: { file?: File };
  onSuccess?: () => void;
  onError?: (err: Error) => void;
}) {
  const file = options.fileItem?.file;
  if (!file || !(file instanceof File)) {
    options.onError?.(new Error('未选择文件'));
    return;
  }
  if (!file.type.startsWith('image/')) {
    Message.error('只能上传图片文件（JPG、PNG、GIF、WebP）');
    options.onError?.(new Error('非图片'));
    return;
  }
  if (file.size / 1024 / 1024 >= 2) {
    Message.error('图片大小不能超过 2MB');
    options.onError?.(new Error('过大'));
    return;
  }
  try {
    imageUploading.value = true;
    const res = await uploadBatchFiles([file]);
    const url = res?.data?.[0];
    let finalUrl = '';
    if (typeof url === 'string') {
      finalUrl = url;
    } else if (url && typeof url === 'object' && (url as { url?: string }).url) {
      finalUrl = (url as { url: string }).url;
    }
    if (finalUrl) {
      form.imageUrl = finalUrl;
      Message.success('图片上传成功');
      options.onSuccess?.();
    } else {
      Message.error('上传失败，未返回图片地址');
      options.onError?.(new Error('无 URL'));
    }
  } catch {
    Message.error('上传失败');
    options.onError?.(new Error('上传失败'));
  } finally {
    imageUploading.value = false;
  }
}

// 格式化时间
const formatTime = (time: string | number): string => {
  if (!time) return '-';
  return new Date(time).toLocaleString(currentLocale());
};

const shortId = (id: string): string => {
  if (!id || typeof id !== 'string') return '-';
  return id.length > 10 ? `${id.slice(0, 8)}…` : id;
};

// 加载系统通知列表
const loadList = async (): Promise<void> => {
  try {
    loading.value = true;
    const response = await getSystemNotificationList();
    if (response.code === 200 && response.data) {
      notificationList.value = response.data;
    }
  } catch (error) {
    console.error('加载系统通知列表失败:', error);
    Message.warning('加载系统通知列表接口暂未实现');
  } finally {
    loading.value = false;
  }
};

// 刷新系统通知列表
const refreshList = async (): Promise<void> => {
  await loadList();
};

// 显示添加系统通知对话框
const showAddDialog = (): void => {
  isEdit.value = false;
  Object.assign(form, {
    id: '',
    content: '',
    imageUrl: '',
    enabled: true,
    description: '',
  });
  dialogVisible.value = true;
};

// 编辑系统通知
const handleEdit = (record: any): void => {
  isEdit.value = true;
  let plain: Record<string, unknown> = {};
  try {
    plain = JSON.parse(JSON.stringify(record ?? {})) as Record<string, unknown>;
  } catch {
    plain = { ...(record ?? {}) } as Record<string, unknown>;
  }
  Object.assign(form, {
    id: plain.id != null ? String(plain.id) : '',
    content: plain.content != null ? String(plain.content) : '',
    imageUrl: plain.imageUrl != null ? String(plain.imageUrl) : '',
    enabled:
      plain.enabled === true ||
      plain.enabled === 'true' ||
      plain.enabled === 1,
    description: plain.description != null ? String(plain.description) : '',
  });
  dialogVisible.value = true;
};

// 保存系统通知
const handleSave = async (): Promise<void> => {
  try {
    const plain = htmlToPlainText(form.content || '');
    const imgOk = !!form.imageUrl?.trim();
    if (!plain && !imgOk) {
      Message.error('请填写通知内容或配图至少一项');
      return;
    }
    if (plain.length > 2000) {
      Message.error('通知内容纯文本过长，请控制在 2000 字以内');
      return;
    }

    const payload = buildSystemNotificationPayload();
    try {
      JSON.parse(JSON.stringify(payload));
    } catch {
      Message.error('通知数据无法序列化，请检查正文是否含异常字符');
      return;
    }

    let response;
    if (isEdit.value) {
      response = await updateSystemNotification(payload);
    } else {
      response = await addSystemNotification(payload);
    }

    if (response.code === 200) {
      Message.success(isEdit.value ? '系统通知更新成功' : '系统通知添加成功');
      dialogVisible.value = false;
      await loadList();
      emit('update');
    } else {
      Message.error((response as any).message || '操作失败');
    }
  } catch (error) {
    console.error('保存系统通知失败:', error);
    Message.warning('系统通知接口暂未实现');
  }
};

// 推送系统通知
const handlePush = async (record: any): Promise<void> => {
  try {
    if (!record.enabled) {
      Message.error('请先启用该通知');
      return;
    }

    const response = await pushSystemNotification(record.id);
    if (response.code === 200) {
      Message.success('系统通知推送成功');
      emit('update');
    } else {
      Message.error((response as any).message || '推送失败');
    }
  } catch (error) {
    console.error('推送系统通知失败:', error);
    Message.warning('推送系统通知接口暂未实现');
  }
};

// 删除系统通知
const handleDelete = async (record: any): Promise<void> => {
  try {
    const response = await deleteSystemNotification(record.id);
    if (response.code === 200) {
      Message.success('删除成功');
      await loadList();
      emit('update');
    } else {
      Message.error((response as any).message || '删除失败');
    }
  } catch (error) {
    console.error('删除系统通知失败:', error);
    Message.warning('删除系统通知接口暂未实现');
  }
};

// 生命周期
onMounted(() => {
  loadList();
});

// 暴露方法供父组件调用
defineExpose({
  refresh: refreshList,
  load: loadList,
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

.mono-id {
  font-family: monospace;
  font-size: 12px;
}

.form-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.thumb-wrap {
  display: flex;
  justify-content: center;
}
.thumb-img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e5e6eb;
}
.thumb-empty {
  color: #c9cdd4;
}

.notice-image-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  width: 100%;
}
.notice-image-preview {
  flex-shrink: 0;
  width: 96px;
  height: 96px;
  border: 1px dashed #e5e6eb;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #f7f8fa;
}
.notice-image-preview--empty {
  border-style: dashed;
}
.notice-image-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.notice-image-preview-img--broken {
  display: none;
}
.notice-image-placeholder {
  font-size: 12px;
  color: #86909c;
  padding: 8px;
  text-align: center;
}
.notice-image-fields {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.notice-image-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>

<style lang="less">
.system-notification-config-modal {
  :deep(.arco-modal-body) {
    max-height: calc(100vh - 200px);
    overflow-y: auto;
  }
}
</style>




