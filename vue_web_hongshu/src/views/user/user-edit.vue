<template>
  <div>
    <div class="user-card">
      <el-form ref="formRef" :model="form">
        <el-upload
          :key="uploadKey"
          style="display: flex; align-items: center; justify-content: center; padding: 10px"
          class="avatar-uploader"
          :auto-upload="false"
          :show-file-list="false"
          :limit="1"
          accept="image/*"
          @change="handleAvatarChange"
        >
          <img v-if="avatarPreview" :src="avatarPreview" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>

        <div class="description">
          <el-descriptions :column="1" :colon="false" direction="vertical">
            <el-descriptions-item :label="$t('user.userId')">
              <span class="info-text">{{ userInfo.hsId }}</span>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('user.phone')">
              <span class="info-text">{{ userInfo.phone }}</span>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('user.ipLocation')">
              <span class="info-text">{{ userInfo.address }}</span>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('user.nickname')">
              <input class="input" type="text" :placeholder="form.username" v-model="form.username" />
            </el-descriptions-item>
            <el-descriptions-item :label="$t('user.myPassword')">
              <el-link type="primary" @click="dialog = true">{{ $t("user.changePassword") }}</el-link>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('user.personalTags')">
              <div class="tags-container">
                <div class="tags-list">
                  <el-tag
                    v-for="tag in form.tags"
                    :key="tag"
                    closable
                    :disable-transitions="false"
                    @close="handleClose(tag)"
                  >
                    {{ tag }}
                  </el-tag>
                  <el-input
                    v-if="inputVisible"
                    ref="InputRef"
                    v-model="inputValue"
                    class="tag-input"
                    size="small"
                    @keyup.enter="handleInputConfirm"
                    @blur="handleInputConfirm"
                  />
                  <el-button
                    v-else
                    class="button-new-tag"
                    size="small"
                    @click="showInput"
                    :disabled="form.tags.length >= 5"
                  >
                    {{ $t("user.addTag") }}
                  </el-button>
                </div>
                <div class="tags-tip">{{ $t("user.maxTags") }}</div>
              </div>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('user.personalBio')">
              <textarea class="textarea" :placeholder="form.description" v-model="form.description"></textarea>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="button-container">
          <el-form-item class="form-buttons">
            <el-button size="small" @click="cancel">{{ $t("common.cancel") }}</el-button>
            <el-button type="primary" @click="onSubmit" size="small">{{ $t("user.confirmEdit") }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>

    <el-dialog v-model="dialog" :title="$t('user.setPassword')" append-to-body>
      <el-form :model="form">
        <el-form-item :label="$t('user.setPassword')" label-width="100px">
          <el-input type="password" v-model="form.password" />
        </el-form-item>
        <el-form-item :label="$t('user.reenterPassword')" label-width="100px">
          <el-input type="password" v-model="form.checkPassword" />
        </el-form-item>
      </el-form>
      <div class="dialog-footer">
        <el-button size="small" @click="dialog = false">{{ $t("common.cancel") }}</el-button>
        <el-button type="primary" @click="postPass" size="small">{{ $t("user.confirmEdit") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from "vue";
import { Plus } from "@element-plus/icons-vue";
import { ElMessage, ElNotification } from "element-plus";
// import type { UploadProps } from 'element-plus'
import { updateUserPassword, updateUser } from "@/api/user";
import type { ElInput } from "element-plus";
import { getUserById } from "@/api/user";
import { useUserStore } from "@/store/userStore";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const userStore = useUserStore();

// 新增头像相关的响应式变量
const avatarPreview = ref("");
const avatarFile = ref<File | null>(null);
const uploadKey = ref(0);

const props = defineProps<{
  userInfo: {
    id: string;
    username: string;
    description: string;
    avatar: string;
    tags?: string;
    hsId?: string;
    phone?: string;
    address?: string;
  };
}>();

interface FormState {
  id: string;
  username: string;
  description: string;
  avatar: string;
  password: string;
  checkPassword: string;
  tags: string[];
}

// 定义响应式数据
const dialog = ref(false);

const form = ref<FormState>({
  id: "",
  username: "",
  description: "",
  avatar: "",
  password: "",
  checkPassword: "",
  tags: [],
});

const emit = defineEmits(["close-drawer", "update-success"]);

const inputValue = ref("");
const inputVisible = ref(false);
const InputRef = ref<InstanceType<typeof ElInput>>();

// 初始化表单数据
const initForm = () => {
  form.value.username = props.userInfo.username;
  form.value.description = props.userInfo.description;
  form.value.avatar = props.userInfo.avatar;
  form.value.id = props.userInfo.id;
  form.value.tags = props.userInfo.tags ? JSON.parse(props.userInfo.tags) : [];

  // 设置初始头像预览
  avatarPreview.value = props.userInfo.avatar;
};

const handleClose = (tag: string) => {
  form.value.tags = form.value.tags.filter((t) => t !== tag);
};

const showInput = () => {
  inputVisible.value = true;
  nextTick(() => {
    InputRef.value?.input?.focus();
  });
};

const handleInputConfirm = () => {
  if (inputValue.value) {
    if (form.value.tags.length >= 5) {
      ElMessage.warning(t("user.maxTagsReached"));
      return;
    }
    if (!form.value.tags.includes(inputValue.value)) {
      form.value.tags.push(inputValue.value);
    }
  }
  inputVisible.value = false;
  inputValue.value = "";
};

// 处理头像选择
const handleAvatarChange = (file: any) => {
  const isImage = file.raw.type.startsWith("image/");
  const isLt2M = file.raw.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error(t("user.imageUploadImageOnly"));
    return;
  }
  if (!isLt2M) {
    ElMessage.error(t("user.imageUploadMax2m"));
    return;
  }

  // 如果之前有预览URL，先清除
  if (avatarPreview.value && avatarPreview.value.startsWith("blob:")) {
    URL.revokeObjectURL(avatarPreview.value);
  }

  // 保存文件对象
  avatarFile.value = file.raw;
  // 创建新的本地预览URL
  avatarPreview.value = URL.createObjectURL(file.raw);

  // 更新key以强制重新渲染上传组件
  uploadKey.value++;
};

// 方法定义
const onSubmit = async () => {
  try {
    const formData = new FormData();
    const userData = {
      id: form.value.id,
      username: form.value.username,
      description: form.value.description,
      tags: JSON.stringify(form.value.tags),
      avatar: form.value.avatar,
    };
    formData.append(
      "user",
      new Blob([JSON.stringify(userData)], {
        type: "application/json",
      })
    );
    if (avatarFile.value) {
      formData.append("avatarFile", avatarFile.value);
    }
    const res = await updateUser(formData);
    if (res.data) {
      // 如果有本地预览URL，清除它
      if (avatarPreview.value && avatarPreview.value.startsWith("blob:")) {
        URL.revokeObjectURL(avatarPreview.value);
      }
      // 更新头像URL和预览
      form.value.avatar = res.data.avatar || form.value.avatar;
      avatarPreview.value = form.value.avatar;
      avatarFile.value = null;
      // 重新获取用户信息
      const userRes = await getUserById(res.data.id);
      if (userRes.data) {
        // 更新全局 userStore
        userStore.setUserInfo(userRes.data);
        // 更新 localStorage
        localStorage.setItem("userInfo", JSON.stringify(userRes.data));
      }
      ElNotification({
        type: "success",
        title: t("user.profileUpdateSuccessTitle"),
        message: t("user.profileUpdateSuccessMsg"),
      });
      emit("update-success");
    }
  } catch (error) {
    console.error("更新用户信息失败:", error);
    ElMessage.error(t("user.profileUpdateFailed"));
  }
};

const postPass = async () => {
  if (!form.value.password) {
    ElNotification({
      type: "warning",
      title: t("user.abnormalTitle"),
      message: t("user.passwordRequiredMsg"),
    });
    return;
  }
  if (form.value.password !== form.value.checkPassword) {
    ElNotification({
      type: "warning",
      title: t("user.abnormalTitle"),
      message: t("user.passwordMismatchMsg"),
    });
    return;
  }
  try {
    await updateUserPassword(form.value);
    dialog.value = false;
    ElNotification({
      type: "success",
      title: t("user.passwordChangeSuccessTitle"),
      message: t("user.passwordChangeSuccessMsg"),
    });
    emit("update-success");
  } catch (error) {
    console.error("更新密码失败:", error);
  }
};

const cancel = () => {
  ElMessage({
    message: t("user.editCancelledMsg"),
    type: "warning",
    duration: 1500,
    grouping: true,
  });
  // 如果有本地预览URL，清除它
  if (avatarPreview.value && avatarPreview.value.startsWith("blob:")) {
    URL.revokeObjectURL(avatarPreview.value);
  }
  // 重置头像预览为当前保存的头像
  avatarPreview.value = form.value.avatar;
  avatarFile.value = null;
  emit("close-drawer");
};

// const handleUploadSuccess: UploadProps['onSuccess'] = (res) => {
//   form.value.avatar = res.result.url
// }

// 组件销毁时清理本地预览URL
onBeforeUnmount(() => {
  if (avatarPreview.value && avatarPreview.value.startsWith("blob:")) {
    URL.revokeObjectURL(avatarPreview.value);
  }
});

// 生命周期钩子
onMounted(() => {
  initForm();
});
</script>

<style scoped lang="less">
.user-card {
  height: 100%;
  border-radius: 10px;
  background-color: white;
  margin-top: -30px;

  .description {
    :deep(.el-descriptions__cell) {
      padding-bottom: 10px;
    }

    :deep(.el-descriptions__label) {
      width: 80px; // 固定标签宽度
      color: #666;
      font-weight: normal;
    }

    :deep(.el-descriptions__content) {
      padding-left: 10px; // 内容区域左侧padding
    }

    .info-text {
      color: #333;
      font-size: 14px;
    }

    .input {
      padding: 12px 16px;
      width: 60%;
      height: 25px;
      line-height: 25px;
      background: rgba(0, 0, 0, 0.03);
      caret-color: rgba(51, 51, 51, 0.3);
      border-radius: 10px;
      border: none;
      outline: none;
      resize: none;
      color: #333;
    }

    .textarea {
      padding: 12px 16px;
      width: 100%;
      height: 70px;
      line-height: 16px;
      background: rgba(0, 0, 0, 0.03);
      caret-color: rgba(51, 51, 51, 0.3);
      border-radius: 10px;
      border: none;
      outline: none;
      resize: none;
      color: #333;
    }
  }

  .button-container {
    position: sticky; // 改为sticky定位
    bottom: 0;
    left: 0;
    right: 0;
    padding: 16px 30px;
    background-color: white;
    border-top: 1px solid #f0f0f0;
    margin-top: auto; // 自动占据剩余空间
    z-index: 1; // 确保按钮在内容之上

    .form-buttons {
      display: flex;
      justify-content: flex-end;
      margin: 0;
      gap: 12px;

      :deep(.el-form-item__content) {
        margin-left: 0 !important;
        justify-content: flex-end;
      }
    }
  }
}

:deep(.avatar-uploader .el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 100%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;

  &:hover {
    border-color: #409eff;

    .avatar-uploader-icon {
      color: #409eff;
    }
  }
}

:deep(.avatar) {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
}

.tags-container {
  .tags-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;

    .el-tag {
      margin-right: 8px;
      margin-bottom: 8px;
      background-color: rgba(0, 0, 0, 0.03);
      border: none;
      color: #333;

      &:hover {
        background-color: rgba(0, 0, 0, 0.06);
      }
    }

    .tag-input {
      width: 100px;
      margin-right: 8px;
      vertical-align: bottom;
    }

    .button-new-tag {
      height: 32px;
      padding: 0 8px;
      border-radius: 16px;
      border: 1px dashed #d9d9d9;

      &:hover {
        border-color: #409eff;
        color: #409eff;
      }

      &:disabled {
        cursor: not-allowed;
        border-color: #d9d9d9;
        color: #999;
      }
    }
  }

  .tags-tip {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 24px 12px 24px;
}
</style>