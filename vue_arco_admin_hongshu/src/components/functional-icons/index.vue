<script lang="ts">
import {defineComponent, h, resolveDynamicComponent} from 'vue';

/** 解析侧栏 / 菜单管理表格中的 Arco 图标组件名（如 IconHome） */
export default defineComponent({
  name: 'FunctionalIcons',
  props: {
    icon: {
      type: String,
      default: '',
    },
  },
  setup(props) {
    return () => {
      const name = props.icon?.trim();
      if (!name || !name.includes('Icon')) {
        return null;
      }
      const Comp = resolveDynamicComponent(name);
      if (Comp === name || Comp === null) {
        return null;
      }
      return h(Comp as object, {size: 18});
    };
  },
});
</script>
