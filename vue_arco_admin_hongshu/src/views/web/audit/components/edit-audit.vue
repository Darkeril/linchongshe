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
        <a-col v-if="state.form.pid !== 0" :span="24">
          <a-form-item label="上级分类" field="pid">
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
            ></a-tree-select>
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
              :show-file-list="false"
              :auto-upload="false"
              :before-upload="beforeUpload"
              :on-change="handleChange"
            >
              <template v-if="form.normalCover">
                <a-image
                  :src="form.normalCover"
                  width="100%"
                  height="100%"
                  preview
                  style="max-height: 100px"
                />
              </template>
              <template v-else>
                <div>
                  <a-icon name="plus" style="font-size: 28px; color: #8c939d" />
                  <div>上传封面图</div>
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
        <a-col :span="12">
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
  import { Form, Message } from '@arco-design/web-vue';
  import {
    addNavbar,
    getNavbar,
    listNavbar,
    listNavbarExcludeChild,
    updateNavbar,
  } from '@/api/web/navbar';
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
    const { data } = await listNavbar({});
    state.navbarOptions = listToTree(data);
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
    };
  };

  const emits = defineEmits([
    'update:visible',
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
    formRef.value?.validate().then((error: any) => {
      if (!error) {
        if (state.form.id !== undefined) {
          updateNavbar(state.form).then(() => {
            Message.success('修改成功');
            emits('update:visible', false);
            emits('update:pid', 0);
            emits('refreshDataList');
          });
        } else {
          addNavbar(state.form).then(() => {
            Message.success('新增成功');
            emits('update:visible', false);
            emits('update:pid', 0);
            emits('refreshDataList');
          });
        }
      }
    });
  };

  const getNavbarForm = async (id: number) => {
    const { data, code, msg } = await getNavbar(id);
    const { data: navbarList } = await listNavbarExcludeChild(
      props.id as number
    );
    state.navbarOptions = listToTree(navbarList);
    if (code && code === 200) {
      state.form = data;
    } else {
      Message.error(msg);
    }
  };

  watch(
    () => props.visible,
    (value) => {
      if (value) {
        if (props.id) {
          state.title = '修改导航栏';
          getNavbarForm(props.id);
        } else {
          reset();
        }
      } else {
        reset();
        state.form.pid = props.pid;
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
</script>

<style scoped></style>
