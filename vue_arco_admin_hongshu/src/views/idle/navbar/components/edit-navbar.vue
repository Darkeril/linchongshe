<template>
  <a-modal
    :visible="open"
    :title="state.title"
    width="50vw"
    @cancel="handleCancel"
    @ok="handleConfirm"
  >
    <a-form
      ref="formRef"
      :model="state.form"
      :rules="state.rules"
      auto-label-width
    >
      <a-row :gutter="16">
        <a-col :span="13">
          <a-form-item
            :label="state.form.pid === state.form.id ? '当前分类' : '上级分类'"
            field="pid"
          >
            <a-tree-select
              v-model="state.form.pid"
              :field-names="{
                key: 'id',
                title: 'title',
                children: 'children',
              }"
              :data="state.navbarOptions"
              :show-count="false"
              placeholder="选择上级分类"
              allow-clear
            >
            </a-tree-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="导航栏名称" field="title">
            <a-input
              v-model="state.form.title"
              placeholder="请输入导航栏名称"
            />
          </a-form-item>
        </a-col>
        <a-col :span="13">
          <a-form-item label="封面图" field="normalCover">
            <a-upload
              class="image-uploader"
              list-type="picture-card"
              :show-file-list="true"
              :auto-upload="false"
              :image-preview="true"
              :limit="1"
              :file-list="fileList"
              @before-upload="beforeUpload"
              @change="handleChange"
              @remove="handleRemove"
            >
              <template #upload-button>
                <div class="upload-placeholder">
                  <icon-plus />
                  <div class="upload-text">上传封面</div>
                </div>
              </template>
            </a-upload>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="显示排序" field="sort">
            <a-input-number v-model="state.form.sort" :step="1" :min="0" />
          </a-form-item>
        </a-col>
        <a-col :span="15">
          <a-form-item label="导航栏状态">
            <a-radio-group v-model="state.form.status">
              <a-radio
                v-for="dict in state.statusOptions"
                :key="dict.dictValue"
                :value="dict.dictValue"
                :label="dict.dictValue"
                >{{ dict.dictLabel }}
              </a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref, watch } from 'vue';
  import { UploadFile, Form, Message } from '@arco-design/web-vue';
  import { IconPlus } from '@arco-design/web-vue/es/icon';
  import {
    addIdleNavbar,
    getIdleNavbar,
    getIdleNavbarList,
    updateIdleNavbar,
  } from '@/api/idle/navbar';
  import { DictDataRecord, getDicts } from '@/api/system/dict-data';

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    id: {
      type: Number,
      default: undefined,
    },
    pid: {
      type: Number,
      default: 0,
    },
  });

  const open = computed(() => {
    return !!props.visible;
  });

  const state = reactive({
    title: '新增导航栏',
    form: {
      id: undefined,
      pid: 0,
      title: undefined,
      sort: undefined,
      normalCover: undefined,
      status: '0',
      file: undefined as File | undefined, // 用户选择的文件
    },
    // 表单校验
    rules: {
      pid: [{ required: true, message: '上级分类不能为空', trigger: 'blur' }],
      title: [
        { required: true, message: '导航栏名称不能为空', trigger: 'blur' },
      ],
      sort: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }],
    } as unknown,
    navbarOptions: [] as Array<any>,
    statusOptions: [] as DictDataRecord[],
  });

  // 初始化字典
  const initData = async () => {
    const { data: statusData } = await getDicts('sys_normal_disable');
    state.statusOptions = statusData;
  };
  initData();

  const fileList = ref<any[]>([]);

  // 阻止默认上传行为
  const beforeUpload = (file: File) => {
    const isImage = file.type.startsWith('image/');
    if (!isImage) {
      Message.error('只能上传图片文件');
      return false;
    }
    const isLt5M = file.size / 1024 / 1024 < 5;
    if (!isLt5M) {
      Message.error('图片大小不能超过 5MB');
      return false;
    }
    
    // 立即保存文件对象到 state.form.file
    state.form.file = file;
    
    // 立即读取文件预览
    const reader = new FileReader();
    reader.onload = (e) => {
      const previewUrl = e.target?.result as string;
      state.form.normalCover = previewUrl;
      
      // 更新 fileList，确保包含 originFile
      const fileItem = {
        uid: Date.now().toString(),
        name: file.name,
        url: previewUrl,
        thumbUrl: previewUrl,
        status: 'done' as const,
        originFile: file,
      };
      fileList.value = [fileItem];
    };
    reader.onerror = (error) => {
      console.error('FileReader 读取失败:', error);
    };
    reader.readAsDataURL(file);
    
    return false; // 阻止自动上传，使用手动上传
  };

  // 处理文件变更
  const handleChange = (fileListParam: any, fileItem: any) => {
    // 更新 fileList
    if (Array.isArray(fileListParam)) {
      fileList.value = fileListParam;
    } else if (fileListParam) {
      fileList.value = [fileListParam];
    } else {
      fileList.value = [];
    }
    
    // 如果 beforeUpload 已经保存了文件对象，确保 fileList 中也包含
    if (state.form.file && state.form.file instanceof File) {
      if (fileList.value.length > 0) {
        const lastFile = fileList.value[fileList.value.length - 1];
        if (!lastFile.originFile) {
          lastFile.originFile = state.form.file;
        }
      }
      return;
    }
    
    // 获取文件对象
    let file: any = null;
    if (Array.isArray(fileListParam) && fileListParam.length > 0) {
      file = fileListParam[fileListParam.length - 1];
    } else if (fileListParam && !Array.isArray(fileListParam)) {
      file = fileListParam;
    } else if (fileItem) {
      file = fileItem;
    }
    
    if (file) {
      // 尝试从多个可能的属性中获取文件对象
      let fileObj: File | null = null;
      
      if (fileItem?.originFile && fileItem.originFile instanceof File) {
        fileObj = fileItem.originFile;
      } else if (fileItem?.file && fileItem.file instanceof File) {
        fileObj = fileItem.file;
      } else if (fileItem?.rawFile && fileItem.rawFile instanceof File) {
        fileObj = fileItem.rawFile;
      } else if (file.originFile && file.originFile instanceof File) {
        fileObj = file.originFile;
      } else if (file.file && file.file instanceof File) {
        fileObj = file.file;
      } else if (file.rawFile && file.rawFile instanceof File) {
        fileObj = file.rawFile;
      } else if (file instanceof File) {
        fileObj = file;
      }
      
      if (fileObj) {
        // 保存文件对象
        state.form.file = fileObj;
        
        // 确保 fileList 中也包含文件对象
        if (fileList.value.length > 0) {
          const lastFile = fileList.value[fileList.value.length - 1];
          lastFile.originFile = fileObj;
        }
        
        // 如果有文件对象，读取预览（如果 beforeUpload 还没有读取）
        if (!state.form.normalCover || !state.form.normalCover.startsWith('data:')) {
          const reader = new FileReader();
          reader.onload = (e) => {
            const previewUrl = e.target?.result as string;
            state.form.normalCover = previewUrl;
            
            // 更新 fileList，添加预览 URL
            if (fileList.value.length > 0) {
              const updatedFileList = fileList.value.map((item: any, index: number) => {
                if (index === fileList.value.length - 1) {
                  return {
                    ...item,
                    url: previewUrl,
                    thumbUrl: previewUrl,
                    status: 'done',
                    originFile: state.form.file,
                  };
                }
                return item;
              });
              fileList.value = updatedFileList;
            }
          };
          reader.onerror = (error) => {
            console.error('FileReader 读取失败:', error);
          };
          reader.readAsDataURL(state.form.file);
        }
      } else if (file.url && !file.originFile && !file.file && !file.rawFile) {
        // 只有 URL 没有文件对象，说明是已有的图片（从服务器加载的）
        state.form.normalCover = file.url;
        if (!state.form.file) {
          state.form.file = undefined;
        }
      }
    } else if (!state.form.file) {
      // 没有文件，且之前也没有文件对象，清空
      state.form.normalCover = undefined;
      state.form.file = undefined;
    }
  };

  // 处理文件删除
  const handleRemove = () => {
    // 删除文件时，清空封面图和文件
    state.form.normalCover = undefined;
    state.form.file = undefined;
    fileList.value = [];
    return true; // 返回 true 表示允许删除
  };

  // 数组转树结构集合
  const listToTree: any = (list: Array<any>) => {
    const map: any = {};
    const treeData: Array<any> = [];
    list = list.map((item) => {
      return {
        id: item.id,
        pid: item.pid,
        title: item.title,
      };
    });
    for (let i = 0; i < list.length; i += 1) {
      map[list[i].id] = i;
      list[i].children = [];
    }
    for (let i = 0; i < list.length; i += 1) {
      const node = list[i];
      if (node.pid && list[map[node.pid]]) {
        list[map[node.pid]].children.push(node);
      } else {
        treeData.push(node);
      }
    }
    return treeData;
  };

  // 查询导航栏下拉树结构
  const fetchTreeSelectOption = async () => {
    const { data } = await getIdleNavbarList({});
    const list = Array.isArray(data)
      ? data
      : data?.list || data?.rows || data?.records || [];
    const treeData = listToTree(list);
    // 添加"主类目"作为父级节点
    state.navbarOptions = [
      {
        id: 0,
        title: '主类目',
        children: treeData,
      },
    ];
  };

  fetchTreeSelectOption();

  const reset = () => {
    state.title = '新增导航栏';
    state.form = {
      id: undefined,
      pid: 0,
      title: undefined,
      sort: undefined,
      normalCover: undefined,
      status: '0',
      file: undefined,
    };
    fileList.value = [];
  };

  const emits = defineEmits([
    'update:visible',
    'update:id',
    'update:pid',
    'refreshDataList',
  ]);
  const handleCancel = () => {
    emits('update:visible', false);
    emits('update:pid', 0);
    reset();
  };

  const formRef = ref<typeof Form>();
  const handleConfirm = () => {
    formRef.value?.validate().then((validateError: any) => {
      if (!validateError) {
        // 构建 FormData，后端需要 category 和 file 参数
        const params = new FormData();
        
        // 构建分类数据对象
        const categoryData: any = {
          pid: state.form.pid === 0 ? 0 : state.form.pid,
          title: state.form.title,
          sort: state.form.sort,
          status: state.form.status,
          normalCover: state.form.normalCover,
        };
        
        // 如果是修改，添加 id
        if (state.form.id !== undefined) {
          categoryData.id = state.form.id;
        }
        
        // 将分类数据转为 JSON 字符串添加到 FormData
        params.append('category', JSON.stringify(categoryData));
        
        // 如果有新上传的文件，添加到 FormData
        let fileToUpload: File | null = null;
        
        // 优先使用 state.form.file
        if (state.form.file && state.form.file instanceof File) {
          fileToUpload = state.form.file;
        } else {
          // 尝试从 fileList 中获取文件对象
          // 如果 uid 不是 '-1'，说明是新上传的文件
          for (let i = 0; i < fileList.value.length; i += 1) {
            const fileItem = fileList.value[i];
            // 如果 uid 不是 '-1'，说明是新上传的文件
            if (fileItem.uid !== '-1' && fileItem.originFile && fileItem.originFile instanceof File) {
              fileToUpload = fileItem.originFile;
              break;
            }
          }
        }
        
        if (fileToUpload) {
          params.append('file', fileToUpload);
        }
        // 注意：新增时文件不是必需的，如果没有上传文件，封面图可以为空
        
        if (state.form.id !== undefined) {
          // 修改导航栏
          updateIdleNavbar(params as any).then(() => {
            Message.success('修改成功');
            emits('update:visible', false);
            emits('update:id', 0);
            emits('update:pid', 0);
            emits('refreshDataList');
            reset();
          }).catch((err) => {
            Message.error('修改失败');
            console.error('修改失败:', err);
          });
        } else {
          // 新增导航栏
          addIdleNavbar(params as any).then(() => {
            Message.success('新增成功');
            emits('update:visible', false);
            emits('update:id', 0);
            emits('update:pid', 0);
            emits('refreshDataList');
            reset();
          }).catch((err) => {
            Message.error('新增失败');
            console.error('新增失败:', err);
          });
        }
      }
    });
  };

  const getNavbarForm = async (id: number) => {
    try {
      const { data, code, msg } = await getIdleNavbar(id);
      // 重新获取导航栏列表用于树选择（排除当前编辑的节点）
      const { data: navbarListData } = await getIdleNavbarList({});
      const navbarList = Array.isArray(navbarListData)
        ? navbarListData
        : navbarListData?.list || navbarListData?.rows || navbarListData?.records || [];
      // 将列表数据转换为树结构并添加"主类目"
      const treeData = listToTree(navbarList);
      state.navbarOptions = [
        {
          id: 0,
          title: '主类目',
          children: treeData,
        },
      ];
      if (code && code === 200) {
        // 保存当前的文件对象（如果有新上传的文件）
        const currentFile = state.form.file;
        // 确保 pid 是数字类型，如果是 null 或 undefined，则设置为 0（主类目）
        const pidValue = data.pid !== null && data.pid !== undefined ? Number(data.pid) : 0;
        state.form = {
          ...data,
          pid: pidValue,
          normalCover: data.normalCover,
          file: currentFile, // 保留新上传的文件对象
        };
        // 如果有封面图，更新 fileList
        if (data.normalCover) {
          fileList.value = [
            {
              uid: '-1',
              name: '封面图',
              status: 'done',
              url: data.normalCover,
            },
          ];
        }
      } else {
        Message.error(msg || '获取导航栏信息失败');
      }
    } catch (error) {
      Message.error('获取导航栏信息失败');
      console.error('获取导航栏信息失败:', error);
    }
  };

  watch(
    () => props.visible,
    (value) => {
      if (value) {
        if (props.id && props.id !== 0) {
          state.title = '修改导航栏';
          getNavbarForm(props.id);
        } else {
          reset();
          // 设置父级ID
          if (props.pid) {
            state.form.pid = props.pid;
          }
        }
      } else {
        reset();
      }
    }
  );
  watch(
    () => props.pid,
    (value) => {
      state.form.pid = value;
    },
    { immediate: true }
  );
  watch(
    () => state.form.normalCover,
    (value) => {
      if (value && !state.form.file) {
        // 只有在没有新上传文件时才显示已有图片
        fileList.value = [
          {
            uid: '-1',
            name: '封面图',
            status: 'done',
            url: value,
          },
        ];
      } else if (!value && !state.form.file) {
        fileList.value = [];
      }
    },
    { immediate: true }
  );
</script>

<style scoped>
  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
  }

  .upload-text {
    margin-top: 8px;
    color: #8c939d;
    font-size: 14px;
  }
</style>
