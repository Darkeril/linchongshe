<script setup lang='ts'>
import { onMounted, ref } from "vue";
import { fetchAgreement } from "@/api";

const content = ref<string>("");

onMounted(() => {
    fetchAgreement<any>(1).then(res => {
    if (res.code === 200) {
        content.value = res.data.content;
    }
  });
});
</script>

<template>
  <div class="flex justify-center p-8">
    <div class="w-[50%]" v-html="content"/>
 </div>

</template>