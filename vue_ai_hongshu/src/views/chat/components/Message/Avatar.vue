<script lang="ts" setup>
import { computed } from 'vue'
import { NAvatar } from 'naive-ui'
import { useAuthStoreWithout } from '@/store/modules/auth'
import { SvgIcon } from '@/components/common'
import { resolveAvatarUrl } from '@/utils/functions'
import defaultAvatar from '@/assets/avatar.png'

interface Props {
  inversion?: boolean
  loading?: boolean
  model?: string
}
defineProps<Props>()

const authStore = useAuthStoreWithout()
const avatar = computed(() => authStore.session && authStore.session.avatar ? authStore.session.avatar : defaultAvatar)
const avatarSrc = computed(() => resolveAvatarUrl(avatar.value as unknown as string))
</script>

<template>
  <template v-if="inversion">
    <NAvatar round :src="avatarSrc" />
  </template>
  <span v-else class="text-[28px] dark:text-white" :class="[loading ? 'rotate-center' : '']">
    <SvgIcon :name="model" width="32" height="32" />
  </span>
</template>

<style scoped>
.rotate-center {
  -webkit-animation: rotate-center 1.5s linear infinite;
  animation: rotate-center 1.5s linear infinite;
}

/**
 * ----------------------------------------
 * animation rotate-center
 * ----------------------------------------
 */
@-webkit-keyframes rotate-center {
  0% {
    -webkit-transform: rotate(0);
    transform: rotate(0);
  }

  100% {
    -webkit-transform: rotate(360deg);
    transform: rotate(360deg);
  }
}

@keyframes rotate-center {
  0% {
    -webkit-transform: rotate(0);
    transform: rotate(0);
  }

  100% {
    -webkit-transform: rotate(360deg);
    transform: rotate(360deg);
  }
}
</style>
