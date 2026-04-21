<script lang='ts' setup>
import logom from '@/assets/logom.jpg'
</script>

<template>
  <div class="flex flex-col items-center justify-center w-full h-[12vh]">
    <div class="mb-2 flex items-center space-x-2">
      <img :src="logom" class="w-10 h-10 rounded-lg">
      <div class="text-2xl font-semibold text-[#1a2029] dark:text-white">
        今天有什么可以帮到你?
      </div>
    </div>
    <slot />
  </div>
</template>
