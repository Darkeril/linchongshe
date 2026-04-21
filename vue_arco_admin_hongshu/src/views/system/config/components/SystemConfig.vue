<template>
  <a-form
    ref="systemFormRef"
    style="margin-left: 20px"
    :model="systemForm"
    :label-col-props="{ span: 4 }"
    :wrapper-col-props="{ span: 16 }"
  >
    <div class="form-section">
      <h4>通过开关选择相关系统配置的优先级</h4>
    </div>

    <!-- 文件存储优先级 -->
    <a-form-item label="文件存储优先级">
      <a-radio-group v-model="systemForm.ossType">
        <a-radio value="0">本地存储</a-radio>
        <a-radio value="1">七牛云</a-radio>
        <a-radio value="2">Minio</a-radio>
        <a-radio value="3">阿里云</a-radio>
        <a-radio value="4">腾讯云</a-radio>
      </a-radio-group>
    </a-form-item>

    <!-- 短信优先级 -->
    <a-form-item label="短信优先级">
      <a-radio-group v-model="systemForm.smsPrimary">
        <a-radio value="aliyun">阿里云</a-radio>
        <a-radio value="tencent">腾讯云</a-radio>
        <a-radio value="yunpian">云片</a-radio>
      </a-radio-group>
    </a-form-item>

    <!-- 中间件优先级 -->
    <a-form-item label="中间件优先级">
      <a-radio-group v-model="systemForm.mqType">
        <a-radio value="Kafka">Kafka</a-radio>
        <a-radio value="RabbitMQ">RabbitMQ</a-radio>
        <a-radio value="RocketMQ">RocketMQ</a-radio>
      </a-radio-group>
    </a-form-item>

    <!-- 搜索优先级 -->
    <a-form-item label="推荐优先级">
      <a-radio-group v-model="systemForm.queryPrimary">
        <a-radio value="es">ES搜索</a-radio>
        <a-radio value="mysql">SQL搜索</a-radio>
        <a-radio value="lightweight">轻量级推荐</a-radio>
      </a-radio-group>
      <div class="form-tip">
        <div>• <strong>ES搜索</strong>：使用Elasticsearch，性能好但需要额外组件</div>
        <div>• <strong>SQL搜索</strong>：使用MySQL直接查询，简单但性能一般</div>
        <div>• <strong>轻量级推荐</strong>：基于标签/分类的智能推荐，性能优秀且效果好（推荐）</div>
      </div>
    </a-form-item>

    <!-- 审核功能配置 -->
    <a-form-item label="审核功能配置">
      <div class="audit-config-container">
        <div class="audit-row">
          <!-- 内容审核 -->
          <div class="audit-item">
            <div class="audit-label">
              <span class="audit-title">内容审核</span>
              <a-switch v-model="systemForm.contentAuditEnabled" />
            </div>
            <div class="form-tip">
              启用后用户发布的内容需要审核通过才能显示
            </div>
          </div>

          <!-- 商品审核 -->
          <div class="audit-item">
            <div class="audit-label">
              <span class="audit-title">商品审核</span>
              <a-switch v-model="systemForm.productAuditEnabled" />
            </div>
            <div class="form-tip">
              启用后用户发布的商品需要审核通过才能上架
            </div>
          </div>
        </div>

        <div class="audit-row">
          <!-- 用户审核 -->
          <div class="audit-item">
            <div class="audit-label">
              <span class="audit-title">用户审核</span>
              <a-switch v-model="systemForm.userAuditEnabled" />
            </div>
            <div class="form-tip">
              启用后新用户注册需要审核通过才能正常使用
            </div>
          </div>

          <!-- 评论审核 -->
          <div class="audit-item">
            <div class="audit-label">
              <span class="audit-title">评论审核</span>
              <a-switch v-model="systemForm.commentAuditEnabled" />
            </div>
            <div class="form-tip">
              启用后用户发布的评论需要审核通过才能显示
            </div>
          </div>
        </div>
      </div>
    </a-form-item>

    <a-form-item>
      <a-button type="primary" @click="handleSubmit">
        保 存
      </a-button>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { Message } from '@arco-design/web-vue';
import { editSystemConfig } from '@/api/system/config';

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

// 表单引用
const systemFormRef = ref();

// 表单数据
const systemForm = reactive({
  ossType: '',
  smsPrimary: '',
  mqType: '',
  queryPrimary: '',
  contentAuditEnabled: false,
  productAuditEnabled: false,
  userAuditEnabled: false,
  commentAuditEnabled: false,
});

// 初始化数据
if (props.initialData && Object.keys(props.initialData).length > 0) {
  Object.assign(systemForm, props.initialData);
}

// 提交表单
const handleSubmit = async (): Promise<void> => {
  try {
    const response = await editSystemConfig(systemForm);
    if (response.code === 200) {
      Message.success('系统配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || '系统配置保存失败');
    }
  } catch (error) {
    console.error('保存系统配置失败:', error);
    Message.warning('配置保存接口暂未实现，配置已更新到本地');
  }
};

// 暴露方法供父组件调用
defineExpose({
  form: systemForm,
  submit: handleSubmit,
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
  line-height: 1.6;
}

.audit-config-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.audit-row {
  display: flex;
  gap: 24px;
}

.audit-item {
  flex: 1;
  padding: 16px;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
  background: #fafafa;
}

.audit-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.audit-title {
  font-weight: 500;
  color: #1d2129;
}
</style>

