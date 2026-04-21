<template>
  <a-card class="sidebar-card" :bordered="true" title="个人信息">
    <div class="avatar-block">
      <ProfileAvatar
          :avatar-url="avatarDisplay"
          @avatar-updated="$emit('refresh')"
      />
      <div class="avatar-tip">点击上传头像</div>
    </div>
    <ul class="info-list">
      <li>
        <span class="label">
          <icon-user/>
          用户名称
        </span>
        <span class="value">{{ user.userName || '—' }}</span>
      </li>
      <li>
        <span class="label">
          <icon-phone/>
          手机号码
        </span>
        <span class="value">{{ user.phonenumber || '—' }}</span>
      </li>
      <li>
        <span class="label">
          <icon-email/>
          用户邮箱
        </span>
        <span class="value">{{ user.email || '—' }}</span>
      </li>
      <li>
        <span class="label">
          <icon-branch/>
          所属部门
        </span>
        <span class="value">{{ deptPostText }}</span>
      </li>
      <li>
        <span class="label">
          <icon-user-group/>
          所属角色
        </span>
        <span class="value">{{ roleGroup || '—' }}</span>
      </li>
      <li>
        <span class="label">
          <icon-calendar/>
          创建日期
        </span>
        <span class="value">{{ user.createTime || '—' }}</span>
      </li>
    </ul>
  </a-card>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import type { UserRecord } from '@/api/system/user';
import ProfileAvatar from './profile-avatar.vue';

const props = defineProps<{
  user: Partial<UserRecord>;
  roleGroup: string;
  postGroup: string;
}>();

defineEmits<{
  (e: 'refresh'): void;
}>();

const avatarDisplay = computed(() => props.user.avatar || '');

const deptPostText = computed(() => {
  const deptName = props.user.dept?.deptName;
  if (!deptName) return '—';
  return props.postGroup ? `${deptName} / ${props.postGroup}` : deptName;
});
</script>

<style scoped lang="less">
.sidebar-card {
  height: 100%;
}

.avatar-block {
  margin-bottom: 20px;
  text-align: center;
}

.avatar-tip {
  margin-top: 8px;
  color: rgb(var(--gray-6));
  font-size: 12px;
}

.info-list {
  margin: 0;
  padding: 0;
  list-style: none;

  li {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 12px 0;
    font-size: 13px;
    border-bottom: 1px solid var(--color-border-2);

    &:last-child {
      border-bottom: none;
    }
  }

  .label {
    display: inline-flex;
    gap: 8px;
    align-items: center;
    color: rgb(var(--gray-7));
  }

  .value {
    max-width: 58%;
    color: rgb(var(--gray-10));
    text-align: right;
    word-break: break-all;
  }
}
</style>
