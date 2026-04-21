<template>
  <div>
    <!-- 演示配置类型选择 -->
    <div class="provider-selector">
      <a-radio-group v-model="currentProvider" type="button">
        <a-radio value="account">
          <icon-user />
          演示账号配置
        </a-radio>
        <a-radio value="site">
          <icon-link />
          演示站配置
        </a-radio>
      </a-radio-group>
    </div>

    <!-- 演示账号配置 -->
    <a-form
      v-if="currentProvider === 'account'"
      ref="demoAccountFormRef"
      style="margin-left: 20px"
      :model="demoAccountForm"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>演示账号功能配置</h4>
      </div>
      <a-form-item label="演示账号" field="username">
        <a-input
          v-model="demoAccountForm.username"
          style="width: 400px"
          placeholder="请输入演示账号用户名"
        />
        <div class="form-tip">建议使用简单的用户名，如：demo</div>
      </a-form-item>
      <a-form-item label="演示密码" field="password">
        <a-input-password
          v-model="demoAccountForm.password"
          allow-clear
          style="width: 400px"
          placeholder="请输入演示账号密码"
        />
        <div class="form-tip">建议使用简单的密码，如：123456</div>
      </a-form-item>
      <a-form-item label="账号描述" field="description">
        <a-textarea
          v-model="demoAccountForm.description"
          :rows="3"
          style="width: 400px"
          placeholder="请输入演示账号的描述信息"
        />
        <div class="form-tip">用于说明演示账号的用途和注意事项</div>
      </a-form-item>
      <a-form-item label="权限范围" field="permissions">
        <a-select
          v-model="demoAccountForm.permissions"
          multiple
          style="width: 400px"
          placeholder="请选择演示账号的权限范围"
        >
          <a-option label="只读权限" value="readonly" />
          <a-option label="基础操作权限" value="basic" />
          <a-option label="完整功能权限" value="full" />
        </a-select>
        <div class="form-tip">建议选择只读权限，确保系统安全</div>
      </a-form-item>
      <a-form-item label="有效期" field="expireTime">
        <a-date-picker
          v-model="demoAccountForm.expireTime"
          show-time
          style="width: 400px"
          placeholder="选择演示账号有效期（可选）"
          format="YYYY-MM-DD HH:mm:ss"
        />
        <div class="form-tip">不设置则永久有效</div>
      </a-form-item>
      <a-form-item label="是否启用" field="enabled">
        <a-switch v-model="demoAccountForm.enabled" />
        <div class="form-tip">启用后可以使用演示账号登录系统</div>
      </a-form-item>
      <a-form-item label="使用说明">
        <a-alert
          message="演示账号使用说明"
          type="info"
          :closable="false"
          show-icon
        >
          <template #content>
            <div class="demo-instructions">
              <p>1. 演示账号仅用于系统功能演示，请勿用于生产环境</p>
              <p>2. 演示账号具有受限的权限，某些敏感操作将被禁用</p>
              <p>3. 建议定期更换演示账号密码，确保系统安全</p>
              <p> 4. 演示账号登录后，系统会在页面顶部显示"演示模式"标识 </p>
              <p> 5. 演示账号的所有操作都会被记录，但不会影响真实数据 </p>
            </div>
          </template>
        </a-alert>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmitAccount"> 保 存 </a-button>
      </a-form-item>
    </a-form>

    <!-- 演示站配置 -->
    <a-form
      v-if="currentProvider === 'site'"
      ref="demoSiteFormRef"
      style="margin-left: 20px"
      :model="demoSiteForm"
      :label-col-props="{ span: 4 }"
      :wrapper-col-props="{ span: 16 }"
    >
      <div class="form-section">
        <h4>演示站相关配置</h4>
      </div>
      <a-form-item label="移动端地址" field="mobileUrl">
        <a-input
          v-model="demoSiteForm.mobileUrl"
          style="width: 400px"
          placeholder="请输入移动端演示站地址"
        />
        <div class="form-tip">例如：https://m.demo.example.com</div>
      </a-form-item>
      <a-form-item label="Web端地址" field="webUrl">
        <a-input
          v-model="demoSiteForm.webUrl"
          style="width: 400px"
          placeholder="请输入Web端演示站地址"
        />
        <div class="form-tip">例如：https://demo.example.com</div>
      </a-form-item>
      <a-form-item label="管理端地址" field="adminUrl">
        <a-input
          v-model="demoSiteForm.adminUrl"
          style="width: 400px"
          placeholder="请输入管理端演示站地址"
        />
        <div class="form-tip">例如：https://admin.demo.example.com</div>
      </a-form-item>
      <a-form-item label="Arco管理端地址" field="arcoAdminUrl">
        <a-input
          v-model="demoSiteForm.arcoAdminUrl"
          style="width: 400px"
          placeholder="请输入Arco管理端演示站地址"
        />
        <div class="form-tip">例如：https://admin.demo.example.com</div>
      </a-form-item>
      <a-form-item label="Gitee地址" field="giteeUrl">
        <a-input
          v-model="demoSiteForm.giteeUrl"
          style="width: 400px"
          placeholder="请输入Gitee项目地址"
        />
        <div class="form-tip">例如：https://gitee.com/username/project</div>
      </a-form-item>
      <a-form-item label="GitHub地址" field="githubUrl">
        <a-input
          v-model="demoSiteForm.githubUrl"
          style="width: 400px"
          placeholder="请输入GitHub项目地址"
        />
        <div class="form-tip">例如：https://github.com/username/project</div>
      </a-form-item>
      <a-form-item label="文档地址" field="docUrl">
        <a-input
          v-model="demoSiteForm.docUrl"
          style="width: 400px"
          placeholder="请输入文档地址"
        />
        <div class="form-tip">例如：https://doc.com/doc</div>
      </a-form-item>
      <a-form-item label="演示站描述" field="description">
        <a-textarea
          v-model="demoSiteForm.description"
          :rows="3"
          style="width: 400px"
          placeholder="请输入演示站描述信息"
        />
        <div class="form-tip">用于说明演示站的功能和特点</div>
      </a-form-item>
      <a-form-item label="是否启用" field="enabled">
        <a-switch v-model="demoSiteForm.enabled" />
        <div class="form-tip">启用后可以访问演示站相关功能</div>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmitSite"> 保 存 </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconUser, IconLink } from '@arco-design/web-vue/es/icon';
import { editDemoAccountConfig, editDemoSiteConfig } from '@/api/system/config';

// Props
interface Props {
  initialAccountData?: any;
  initialSiteData?: any;
  initialProvider?: string;
}

const props = withDefaults(defineProps<Props>(), {
  initialAccountData: () => ({}),
  initialSiteData: () => ({}),
  initialProvider: 'account',
});

// Emits
const emit = defineEmits<{
  (e: 'update'): void;
}>();

// 表单引用
const demoAccountFormRef = ref();
const demoSiteFormRef = ref();

// 当前选中的配置类型
const currentProvider = ref<string>(props.initialProvider);

// 演示账号配置表单
const demoAccountForm = reactive({
  username: 'demo',
  password: '123456',
  description: '演示账号，用于系统功能展示',
  permissions: ['readonly'],
  expireTime: '',
  enabled: false,
});

// 演示站配置表单
const demoSiteForm = reactive({
  mobileUrl: '',
  webUrl: '',
  adminUrl: '',
  arcoAdminUrl: '',
  giteeUrl: '',
  githubUrl: '',
  docUrl: '',
  description: '',
  enabled: false,
});

// 初始化数据
if (
  props.initialAccountData &&
  Object.keys(props.initialAccountData).length > 0
) {
  Object.assign(demoAccountForm, props.initialAccountData);
}

if (props.initialSiteData && Object.keys(props.initialSiteData).length > 0) {
  Object.assign(demoSiteForm, props.initialSiteData);
}

// 监听初始服务商变化
watch(
  () => props.initialProvider,
  (newVal) => {
    if (newVal) {
      currentProvider.value = newVal;
    }
  },
  { immediate: true }
);

// 提交演示账号配置
const handleSubmitAccount = async (): Promise<void> => {
  try {
    const response = await editDemoAccountConfig(demoAccountForm);
    if (response.code === 200) {
      Message.success('演示账号配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || '演示账号配置保存失败');
    }
  } catch (error) {
    console.error('保存演示账号配置失败:', error);
    Message.warning('配置保存接口暂未实现，配置已更新到本地');
  }
};

// 提交演示站配置
const handleSubmitSite = async (): Promise<void> => {
  try {
    const response = await editDemoSiteConfig(demoSiteForm);
    if (response.code === 200) {
      Message.success('演示站配置保存成功');
      emit('update');
    } else {
      Message.error((response as any).message || '演示站配置保存失败');
    }
  } catch (error) {
    console.error('保存演示站配置失败:', error);
    Message.warning('配置保存接口暂未实现，配置已更新到本地');
  }
};

// 暴露方法供父组件调用
defineExpose({
  demoAccountForm,
  demoSiteForm,
  currentProvider,
  submitAccount: handleSubmitAccount,
  submitSite: handleSubmitSite,
});
</script>

<style scoped lang="less">
.provider-selector {
  margin: 20px 0 20px 20px;
}

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
}

.demo-instructions {
  p {
    margin: 4px 0;
    line-height: 1.6;
  }
}
</style>



