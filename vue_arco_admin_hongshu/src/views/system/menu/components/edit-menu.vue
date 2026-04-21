<template>
  <a-modal :visible="open" :title="modalTitle" width="50vw" @cancel="handleCancel" @ok="handleConfirm">
    <a-form ref="formRef" :model="state.form" :rules="state.rules" auto-label-width>
      <a-row :gutter="16">
        <a-col :span="24">
          <a-form-item :label="$t('biz.parentMenu')" field="parentId">
            <a-tree-select
                v-model="state.form.parentId"
                :field-names="{ key: 'menuId', title: 'menuName', children: 'children' }"
                :data="state.menuOptions"
                :show-count="false"
                :placeholder="$t('biz.selectParentMenu')"
            ></a-tree-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="$t('biz.menuType')" field="menuType">
            <a-radio-group v-model="state.form.menuType" default-value="M">
              <a-radio key="M" value="M">{{ $t('biz.menuTypeDirectory') }}</a-radio>
              <a-radio key="C" value="C">{{ $t('biz.menuTypeMenu') }}</a-radio>
              <a-radio key="F" value="F">{{ $t('biz.menuTypeButton') }}</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col v-if="state.form.menuType !== 'F'" :span="24">
          <a-form-item :label="$t('biz.menuIconLabel')" field="icon">
            <arco-menu-icon-picker v-model="state.form.icon"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="$t('biz.menuTitle')" field="menuName">
            <a-input v-model="state.form.menuName" :placeholder="$t('biz.menuTitlePlaceholder')"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="$t('biz.displaySort')" field="orderNum">
            <a-input-number v-model="state.form.orderNum" :step="1" :min="0"/>
          </a-form-item>
        </a-col>
        <a-col v-if="state.form.menuType !== 'F'" :span="12">
          <a-form-item :label="$t('biz.externalLink')" field="isFrame" :tooltip="$t('biz.externalLinkTooltip')">
            <a-radio-group v-model="state.form.isFrame" default-value="0">
              <a-radio key="0" value="0">{{ $t('biz.yes') }}</a-radio>
              <a-radio key="1" value="1">{{ $t('biz.no') }}</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col v-if="state.form.menuType !== 'F'" :span="12">
          <a-form-item :label="$t('biz.path')" field="path" :tooltip="$t('biz.routePathTooltip')">
            <a-input v-model="state.form.path" :placeholder="$t('biz.routePathPlaceholder')"/>
          </a-form-item>
        </a-col>
        <a-col v-if="state.form.menuType === 'C'" :span="12">
          <a-form-item :label="$t('biz.component')" field="component" :tooltip="$t('biz.componentTooltip')">
            <a-input v-model="state.form.component" :placeholder="$t('biz.componentPlaceholder')"/>
          </a-form-item>
        </a-col>
        <a-col v-if="state.form.menuType !== 'M'" :span="12">
          <a-form-item
              :label="$t('biz.permissionCharLabel')" field="perms" :tooltip="$t('biz.permissionCharTooltip')">
            <a-input v-model="state.form.perms" :placeholder="$t('biz.permsPlaceholder')" maxlength="100"/>
          </a-form-item>
        </a-col>
        <a-col v-if="state.form.menuType === 'C'" :span="12">
          <a-form-item :label="$t('biz.routeQueryLabel')" field="query" :tooltip="$t('biz.routeQueryTooltip')">
            <a-input v-model="state.form.query" :placeholder="$t('biz.routeQueryPlaceholder')" maxlength="255"/>
          </a-form-item>
        </a-col>
        <a-col v-if="state.form.menuType === 'C'" :span="12">
          <a-form-item :label="$t('biz.isCacheLabel')" field="isCache" :tooltip="$t('biz.isCacheTooltip')">
            <a-radio-group v-model="state.form.isCache" default-value="1">
              <a-radio key="0" value="0">{{ $t('biz.cacheKeep') }}</a-radio>
              <a-radio key="1" value="1">{{ $t('biz.cacheNoKeep') }}</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col v-if="state.form.menuType !== 'F'" :span="12">
          <a-form-item :label="$t('biz.displayStatusLabel')" :tooltip="$t('biz.displayStatusTooltip')">
            <a-radio-group v-model="state.form.visible">
              <a-radio
                  v-for="dict in state.visibleOptions"
                  :key="dict.dictValue"
                  :value="dict.dictValue"
                  :label="dict.dictValue"
              >{{ dict.dictLabel }}
              </a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col v-if="state.form.menuType !== 'F'" :span="12">
          <a-form-item :label="$t('biz.menuStateLabel')" :tooltip="$t('biz.menuStateTooltip')">
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
import {computed, reactive, ref, watch} from "vue";
import {useI18n} from 'vue-i18n';
import {Form, Message} from "@arco-design/web-vue";
import {addMenu, getMenu, listMenu, updateMenu} from "@/api/system/menu";
import {getDicts} from "@/api/system/dict-data";
import ArcoMenuIconPicker from '@/components/arco-menu-icon-picker/index.vue';
import {normalizeMenuIconForSave} from '@/utils/arco-menu-icon';

const {t} = useI18n();

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  id: {
    type: [Number, String],
    default: undefined,
  },
  parentId: {
    type: Number,
    default: 0,
  },
});

const open = computed(() => {
  return props.visible;
})

const modalTitle = computed(() =>
    props.id ? t('biz.editMenuModalTitle') : t('biz.addMenuTitle'),
);

const state = reactive({
  form: {
    menuId: undefined,
    parentId: 0,
    menuName: undefined,
    icon: undefined,
    menuType: 'M',
    orderNum: undefined,
    isFrame: '1',
    isCache: '0',
    visible: '0',
    status: '0',
    path: undefined,
  },
  menuOptions: [] as Array<any>,
  visibleOptions: [],
  statusOptions: [],
  rules: {},
})

// 初始化字典
const initData = async () => {
  const {data: visibleData} = await getDicts('sys_show_hide');
  state.visibleOptions = visibleData;
  const {data: statusData} = await getDicts('sys_normal_disable');
  state.statusOptions = statusData;
};
initData();

// 数组转树结构集合
const listToTree: any = (list: Array<any>) => {
  const map: any = {};
  const treeData: Array<any> = [];
  list = list.map(item => {
    return {menuId: item.menuId, parentId: item.parentId, menuName: item.menuName,}
  })
  for (let i = 0; i < list.length; i += 1) {
    map[list[i].menuId] = i;
    list[i].children = [];
  }
  for (let i = 0; i < list.length; i += 1) {
    const node = list[i];
    if (node.parentId && list[map[node.parentId]]) {
      list[map[node.parentId]].children.push(node);
    } else {
      treeData.push(node);
    }
  }
  return treeData;
};

// 查询菜单下拉树结构
const fetchTreeSelectOption = async () => {
  const {data} = await listMenu({});
  const menu = {menuId: 0, menuName: t('biz.mainCategory'), children: [...listToTree(data)]};
  state.menuOptions.push(menu);
};
fetchTreeSelectOption();

const reset = () => {
  state.form = {
    menuId: undefined,
    parentId: 0,
    menuName: undefined,
    icon: undefined,
    menuType: 'M',
    orderNum: undefined,
    isFrame: '1',
    isCache: '0',
    visible: '0',
    status: '0',
    path: undefined,
  };
};

const emits = defineEmits(['update:visible', 'update:parentId', 'refreshDataList']);
const handleCancel = () => {
  emits('update:visible', false);
  emits('update:parentId', 0);
  reset();
};

const formRef = ref<typeof Form>();
const handleConfirm = () => {
  formRef.value?.validate().then((error: any) => {
    if (!error) {
      if (state.form.menuType !== 'F') {
        (state.form as { icon?: string }).icon = normalizeMenuIconForSave(
          state.form.icon as string | undefined,
        );
      }
      if (state.form.menuId !== undefined) {
        updateMenu(state.form).then(() => {
          Message.success(t('biz.editSuccess'));
          emits('update:visible', false);
          emits('update:parentId', 0);
          emits('refreshDataList');
          reset();
        });
      } else {
        addMenu(state.form).then(() => {
          Message.success(t('biz.addSuccess'));
          emits('update:visible', false);
          emits('update:parentId', 0);
          emits('refreshDataList');
          reset();
        });
      }
    }
  });
};

const getMenuForm = async (id: number | string) => {
  const {data, code, msg} = await getMenu(id);
  if (code && code === 200) {
    state.form = data;
  } else {
    Message.error(msg)
  }
};

watch(
    () => props.id,
    (value) => {
      if (value) {
        getMenuForm(value);
      } else {
        reset();
        state.form.parentId = props.parentId;
      }
    },
    {immediate: true}
)
watch(
    () => props.parentId,
    (value) => {
      state.form.parentId = value;
    }, {immediate: true},
)
</script>

<style scoped>

</style>
