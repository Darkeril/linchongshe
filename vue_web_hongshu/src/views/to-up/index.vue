<template>
  <div
    class="note-detail-mask"
    style="transition: background-color 0.4s ease 0s;
    hsla(0,0%,100%,0.98)"
    @click="close"
  >
    <div class="note-container" @click.stop>
      <div class="bug-info">
        <el-input v-model="emailVal" :placeholder="$t('toUp.emailPlaceholder')" style="margin-bottom: 0.625rem" />
        <el-input
          v-model="content"
          type="textarea"
          :placeholder="$t('toUp.contentPlaceholder')"
          :autosize="{ minRows: 5, maxRows: 8 }"
          style="margin-bottom: 1.25rem"
        />
        <el-button type="primary" @click="submit" style="width: 6.25rem">{{ $t("toUp.send") }}</el-button>
      </div>
    </div>

    <div class="close-cricle" @click="close">
      <div class="close close-mask-white">
        <Close style="width: 1.2em; height: 1.2em; color: rgba(51, 51, 51, 0.8)" />
      </div>
    </div>

    <div class="back-desk"></div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { Close } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { toUp } from "@/api/util";

const { t } = useI18n();

const emailVal = ref("");
const content = ref("");

const emit = defineEmits(["clickToUp"]);

const close = () => {
  emit("clickToUp");
};

const submit = () => {
  const reg = /^([a-zA-Z]|[0-9])(\w|-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
  if (!reg.test(emailVal.value)) {
    ElMessage.warning(t("toUp.invalidEmail"));
    return;
  }

  if (content.value === "") {
    ElMessage.warning(t("toUp.emptyContent"));
    return;
  }

  const dto = {} as any;
  dto.email = emailVal.value;
  dto.content = content.value;
  toUp(dto).then(() => {
    content.value = "";
    ElMessage.success(t("toUp.sendSuccess"));
  });
};
</script>

<style lang="less" scoped>
.note-detail-mask {
  position: fixed;
  left: 0;
  top: 0;
  display: flex;
  width: 100vw;
  height: 100vh;
  z-index: 20;
  overflow: auto;

  .back-desk {
    position: fixed;
    background-color: #f4f4f4;
    opacity: 0.5;
    width: 100vw;
    height: 100vh;
    z-index: 30;
  }

  .close-cricle {
    left: 40vw;
    top: 1.3vw;
    position: fixed;
    display: flex;
    z-index: 100;
    cursor: pointer;

    .close-mask-white {
      box-shadow:
        0 0.125rem 0.5rem 0 rgba(0, 0, 0, 0.04),
        0 0.0625rem 0.125rem 0 rgba(0, 0, 0, 0.02);
      border: 0.0625rem solid rgba(0, 0, 0, 0.08);
    }

    .close {
      display: flex;
      justify-content: center;
      align-items: center;
      border-radius: 100%;
      width: 2.5rem;
      height: 2.5rem;
      border-radius: 2.5rem;
      cursor: pointer;
      transition: all 0.3s;
      background-color: #fff;
    }
  }

  .note-container {
    margin-left: 38vw;
    width: 20%;
    height: 50%;
    transition:
      transform 0.4s ease 0s,
      width 0.4s ease 0s;
    transform: translate(6.5rem, 2rem) scale(1);
    overflow: visible;

    display: flex;
    box-shadow:
      0 0.5rem 4rem 0 rgba(0, 0, 0, 0.04),
      0 0.0625rem 0.25rem 0 rgba(0, 0, 0, 0.02);
    border-radius: 1.25rem;
    background: #f8f8f8;
    transform-origin: left top;
    z-index: 100;

    .bug-info {
      width: 80%;
      height: 80%;
      margin: auto;
      display: flex;
      flex-direction: column;
    }
  }
}
</style>
