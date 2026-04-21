<template>
  <div style="border: 1px solid #ccc">
    <Toolbar
        :editor="editorRef"
        :default-config="toolbarConfig"
        :mode="mode"
        style="border-bottom: 1px solid #ccc"
    />
    <Editor
        v-model="valueHtml"
        :default-config="editorConfig"
        :mode="mode"
        :style="`height: ${minHeight}px; overflow-y: hidden;`"
        @on-created="handleCreated"
        @on-change="onChange"
    />
  </div>
</template>

<script lang="ts" setup>
import '@wangeditor/editor/dist/css/style.css' // 引入 css

import {onBeforeUnmount, ref, shallowRef, watch} from 'vue'
import {IEditorConfig} from "@wangeditor/editor";
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import axios from '@/api/interceptor';
import {getToken} from '@/utils/auth';

const props = defineProps({
  value: {
    type: String,
    default: '',
  },
  mode: {
    type: String,
    default: 'simple',
  },
  minHeight: {
    type: Number,
    default: 192,
  },
});

const emits = defineEmits(["update:value"]);

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef();

// 内容 HTML
const valueHtml = ref('');

// 使用监听加载内容
watch(
    () => props.value,
    (value) => {
      valueHtml.value = value;
    }, {
      immediate: true,
    }
);

const toolbarConfig = {};
const editorConfig: Partial<IEditorConfig> = {
  placeholder: '请输入内容',
  MENU_CONF: {
    uploadImage: {
      // 自定义上传
      async customUpload(file: File, insertFn: (url: string, alt: string, href: string) => void) {
        const formData = new FormData();
        formData.append('file', file);
        
        try {
          const response = await axios.post('/web/oss/upload', formData, {
            headers: {
              'Content-Type': 'multipart/form-data',
            },
          });
          
          if (response.code === 200 && response.data) {
            // response.data 就是文件URL字符串
            const url = typeof response.data === 'string' ? response.data : response.data.url || response.data;
            insertFn(url, file.name, url);
          } else {
            throw new Error(response.msg || '上传失败');
          }
        } catch (error: any) {
          console.error('图片上传失败:', error);
          const errorMsg = error.response?.data?.msg || error.message || '图片上传失败';
          throw new Error(errorMsg);
        }
      },
    },
  },
};

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
});

const handleCreated = (editor: any) => {
  editorRef.value = editor // 记录 editor 实例，重要！
};

const onChange = (e: any) => {
  emits("update:value", e.getHtml())
};
</script>

<style scoped>

</style>
