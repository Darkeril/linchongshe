<template>
  <div class="form-container">
    <div class="user-card">
      <el-form ref="formRef" :model="form">
        <div class="description"> 
          <el-descriptions :column="1" :colon="false" direction="vertical">
            <el-descriptions-item :label="$t('order.receiverNameLabel')" v-if="type === '用户自提'">
              <input 
                class="input" 
                type="text" 
                :placeholder="$t('order.contactNamePlaceholder')" 
                v-model="form.username"
              />
            </el-descriptions-item>
            <el-descriptions-item :label="$t('order.receiverPhoneLabel')" v-if="type === '用户自提'">
              <input 
                class="input" 
                type="text" 
                :placeholder="$t('order.contactPhonePlaceholder')" 
                v-model="form.phone"
              />
            </el-descriptions-item>
            <el-descriptions-item :label="$t('order.addressLabel')" v-if="type === '用户自提'">
              <textarea 
                class="textarea" 
                :placeholder="$t('order.pickupAddressPlaceholder')" 
                v-model="form.address"
              ></textarea>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('order.logisticsCompany')" v-if="type === '物流发货'">
              <input 
                class="input" 
                type="text" 
                :placeholder="$t('order.logisticsCompanyPlaceholder')" 
                v-model="form.postCompany"
              />
            </el-descriptions-item>
            <el-descriptions-item :label="$t('order.trackingNumber')" v-if="type === '物流发货'">
              <input 
                class="input" 
                type="text" 
                :placeholder="$t('order.trackingPlaceholder')" 
                v-model="form.postNum"
              />
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <el-form-item>
          <el-button type="primary" @click="onSubmit" size="mini">{{ $t("common.confirm") }}</el-button>
          <el-button size="mini" @click="cancel">{{ $t("common.cancel") }}</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import type { ElForm } from 'element-plus'
import { createPost, createPostSelf } from "@/api/idle";


// 定义表单数据类型
interface FormData {
  orderNumber: string
  username: string
  phone: string
  address: string
  postCompany: string
  postNum: string
}

// 定义props
const props = defineProps({
  productOrderNumber: {
    type: String,
    default: ''
  },
  type: {
    type: String as () => '用户自提' | '物流发货',
    default: '用户自提'
  }
})

// 定义emits
const emits = defineEmits(['close-drawer', 'submit-success']);

// 表单ref
const formRef = ref<InstanceType<typeof ElForm>>()

// 表单数据
const form = ref<FormData>({
  orderNumber: '',
  username: '',
  phone: '',
  address: '',
  postCompany: '',
  postNum: ''
})

// 初始化表单数据
onMounted(() => {
  form.value.orderNumber = props.productOrderNumber
})

// 提交表单
const onSubmit = () => {
  if (props.type === '物流发货') {
    createPostData()
  } else if (props.type === '用户自提') {
    createPostSelfData()
  }
}

// 创建自提订单
const createPostSelfData = async () => {
  try {
    await createPostSelf(form.value)
    emits('close-drawer');
    emits('submit-success'); // 可以添加提交成功事件
  } catch (error) {
    console.error('创建自提订单失败:', error)
  }
}

// 创建物流订单
const createPostData = async () => {
  try {
    await createPost(form.value); 
    emits('close-drawer');
    emits('submit-success'); // 可以添加提交成功事件
  } catch (error) {
    console.error('创建物流订单失败:', error)
  }
}

// 取消操作
const cancel = () => {
  emits('close-drawer')
}
</script>

<style scoped lang="less">
.form-container {
  // background-color: #f7f7f7;
  height: 100%;
  width: 100%; 
  padding: 15px;

  .user-card {
    height: 100%;
    border-radius: 10px;
    background-color: white;
    padding: 10px;

    .description {
      .input {
        padding: 12px 16px;
        width: 80%;
        height: 25px;
        line-height: 25px;
        background: rgba(0, 0, 0, 0.03);
        caret-color: rgba(51, 51, 51, 0.3);
        border-radius: 10px;
        border: none;
        outline: none;
        resize: none;
        color: #333;
      }

      .textarea {
        padding: 12px 16px;
        width: 100%;
        height: 70px;
        line-height: 16px;
        background: rgba(0, 0, 0, 0.03);
        caret-color: rgba(51, 51, 51, 0.3);
        border-radius: 10px;
        border: none;
        outline: none;
        resize: none;
        color: #333;
      }
    }
  }
}

:deep(.avatar-uploader .el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 100%;
  cursor: pointer;
  position: relative;
  overflow: hidden;

  &:hover {
    border-color: #409EFF;
  }
}

:deep(.avatar-uploader-icon) {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

:deep(.avatar) {
  width: 100px;
  height: 100px;
  display: block;
}
</style>