<template>
  <div class="address-container">
    <div class="address-header">
      <h2>{{ $t("user.myAddress") }}</h2>
      <el-button type="primary" size="small" @click="toAdd" class="add-btn">
        <el-icon><Plus /></el-icon>
        {{ $t("user.addAddress") }}
      </el-button>
    </div>

    <div class="address-list">
      <div v-for="(item) in addressList" 
           :key="item.id"
           class="address-item"
           :class="{ active: item.id === selectedAddress.id }"
           @click="select(item)">
        <div class="address-content">
          <div class="address-info">
            <div class="user-info">
              <span class="name">{{ item.name }}</span>
              <span class="phone">{{ item.phone }}</span>
              <el-tag v-if="item.isDefault" size="small" type="success">{{ $t("user.defaultAddress") }}</el-tag>
            </div>
            <div class="address-detail">
              <el-icon><Location /></el-icon>
              <span>{{ formatFullAddress(item) }}</span>
            </div>
          </div>
          <div class="address-actions">
            <el-button 
              type="text" 
              @click.stop="edit(item)"
              class="action-btn">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button 
              type="text" 
              @click.stop="del(item)"
              class="action-btn delete">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="props.button" class="dialog-footer">
      <el-button @click="cancel">{{ $t("common.cancel") }}</el-button>
      <el-button type="primary" @click="confirm">{{ $t("common.confirm") }}</el-button>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="formTitle"
      width="500px"
      append-to-body
      :close-on-click-modal="false"
      class="address-dialog"
    >
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-position="top"
        class="address-form"
      >
        <el-form-item 
          :label="$t('user.receiverName')" 
          prop="name"
          class="form-item"
        >
          <el-input 
            v-model="formData.name" 
            maxlength="20"
            show-word-limit
            :placeholder="$t('user.addressNamePlaceholder')"
          />
        </el-form-item>
        
        <el-form-item 
          :label="$t('user.receiverPhone')" 
          prop="phone"
          class="form-item"
        >
          <el-input 
            v-model="formData.phone" 
            maxlength="11"
            :placeholder="$t('user.addressPhonePlaceholder')"
          />
        </el-form-item>
        
        <el-form-item 
          :label="$t('user.regionLabel')" 
          prop="region"
          class="form-item"
        >
          <el-cascader
            v-model="regionValue"
            :options="regionOptions"
            :props="cascaderProps"
            :placeholder="$t('user.selectRegionPlaceholder')"
            clearable
            style="width: 100%"
            @change="handleRegionChange"
          />
        </el-form-item>
        
        <el-form-item 
          :label="$t('user.addressDetail')" 
          prop="address"
          class="form-item"
        >
          <el-input 
            v-model="formData.address" 
            type="textarea"
            maxlength="100"
            show-word-limit
            :placeholder="$t('user.addressDetailInputPlaceholder')"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t("common.cancel") }}</el-button>
          <el-button type="primary" @click="add">{{ $t("common.confirm") }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElNotification } from 'element-plus'
import { Plus, Location, Edit, Delete } from '@element-plus/icons-vue'
import type { AddressItem, AddressForm } from '@/type/address'
import { getAddressList, saveAddress, deleteAddress } from '@/api/user'
import { useUserStore } from "@/store/userStore" // 添加用户状态管理
import { regionData, type RegionOption } from '@/utils/regionData'
import type { CascaderProps } from 'element-plus'

const { t } = useI18n()

const formTitle = computed(() => {
  return formData.id ? t('user.editAddressTitle') : t('user.newAddressTitle')
})

// Props定义
interface Props {
  button?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  button: false
})

// Emits定义
const emit = defineEmits<{
  (e: 'confirm', address: AddressItem): void
  (e: 'close-drawer'): void
}>()

const userStore = useUserStore()
const uid = computed(() => userStore.getUserInfo().id)

// 响应式数据
const dialogVisible = ref(false)
const addressList = ref<AddressItem[]>([])
const selectedAddress = ref<AddressItem>({
  id: '',
  uid: '',
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  address: ''
})

const formData = reactive<AddressForm>({
  id: '',
  uid: '',
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  address: ''
})

// 省市区选择器相关
const regionValue = ref<string[]>([])
const regionOptions = ref<RegionOption[]>(regionData)
const cascaderProps: CascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  expandTrigger: 'hover' as const
}

// 处理省市区选择变化
const handleRegionChange = (value: string[]) => {
  if (value && value.length >= 3) {
    formData.province = value[0] || ''
    formData.city = value[1] || ''
    formData.district = value[2] || ''
  } else {
    formData.province = ''
    formData.city = ''
    formData.district = ''
  }
}

// 根据省市区值设置级联选择器的值
const setRegionValue = () => {
  if (formData.province && formData.city && formData.district) {
    regionValue.value = [formData.province, formData.city, formData.district]
  } else {
    regionValue.value = []
  }
}

const edit = (item: AddressItem) => {
  // 复制数据到表单
  formData.id = item.id
  formData.uid = uid.value
  formData.name = item.name
  formData.phone = item.phone
  formData.province = item.province || ''
  formData.city = item.city || ''
  formData.district = item.district || ''
  formData.address = item.address
  
  // 设置级联选择器的值
  setRegionValue()
  
  // 打开对话框
  dialogVisible.value = true
}

// 添加表单验证规则
const rules = computed(() => ({
  name: [
    { required: true, message: t('user.validateAddressName'), trigger: 'blur' },
    { min: 2, max: 20, message: t('user.validateAddressNameLen'), trigger: 'blur' }
  ],
  phone: [
    { required: true, message: t('user.validateAddressPhone'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('user.validateAddressPhonePattern'), trigger: 'blur' }
  ],
  region: [
    { 
      required: true, 
      validator: (_rule: any, _value: any, callback: any) => {
        if (!regionValue.value || regionValue.value.length !== 3) {
          callback(new Error(t('user.validateAddressRegion')))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  address: [
    { required: true, message: t('user.validateAddressDetailRequired'), trigger: 'blur' },
    { min: 5, max: 100, message: t('user.validateAddressDetailLen'), trigger: 'blur' }
  ]
}))

const formRef = ref()


// 方法
const getList = async () => {
  try {
    const res = await getAddressList(uid.value)
    addressList.value = res.data
  } catch (error) {
    console.error('获取地址列表失败:', error)
  }
}

const add = async () => {
  if (!formRef.value) return
  try {
    // 表单验证
    await formRef.value.validate()
    // 发送请求
    await saveAddress({
      id: formData.id, // 添加 id，编辑时使用
      uid: uid.value,
      name: formData.name,
      phone: formData.phone,
      province: formData.province,
      city: formData.city,
      district: formData.district,
      address: formData.address
    })
    await getList()
    dialogVisible.value = false
    ElNotification({
      type: 'success',
      title: formData.id ? t('user.addressEditSuccess') : t('user.addressAddSuccess'),
      message: ''
    })
    resetForm()
  } catch (error) {
    if (error === 'validate') return // 表单验证失败
    console.error(formData.id ? '编辑地址失败:' : '添加地址失败:', error)
    ElNotification({
      type: 'error',
      title: formData.id ? t('user.addressEditFailed') : t('user.addressAddFailed'),
      message: ''
    })
  }
}

const del = async (item: AddressItem) => {
  try {
    await deleteAddress(item.id)
    await getList()
    ElNotification({
      type: 'success',
      title: t('user.addressDeleteSuccess'),
      message: ''
    })
  } catch (error) {
    console.error('删除地址失败:', error)
    ElNotification({
      type: 'error',
      title: t('user.addressDeleteFailed'),
      message: ''
    })
  }
}

const toAdd = () => {
  resetForm() // 清空表单
  dialogVisible.value = true
}

const select = (item: AddressItem) => {
  selectedAddress.value = item
}

const confirm = () => {
  if (!selectedAddress.value.id) {
    ElMessage.warning(t('user.selectAddress'))
    return
  }
  emit('confirm', selectedAddress.value)
}

const cancel = () => {
  emit('close-drawer')
}

const resetForm = () => {
  formData.id = ''
  formData.name = ''
  formData.phone = ''
  formData.province = ''
  formData.city = ''
  formData.district = ''
  formData.address = ''
  regionValue.value = []
}

// 格式化完整地址显示
const formatFullAddress = (item: AddressItem): string => {
  const parts: string[] = []
  if (item.province) parts.push(item.province)
  if (item.city) parts.push(item.city)
  if (item.district) parts.push(item.district)
  if (item.address) parts.push(item.address)
  return parts.length > 0 ? parts.join(' ') : item.address || ''
}

// 生命周期
onMounted(() => {
  if (uid.value) { // 添加判断
    getList()
  } else {
    ElMessage.error(t('user.getUserInfoFailed'))
  }
})
</script>

<style scoped>
.address-dialog {
  :deep(.el-dialog) {
    border-radius: 8px;
    
    .el-dialog__header {
      margin: 0;
      padding: 20px;
      border-bottom: 1px solid #eee;
      
      .el-dialog__title {
        font-size: 18px;
        font-weight: 500;
      }
    }
    
    .el-dialog__body {
      padding: 24px 20px;
    }
    
    .el-dialog__footer {
      padding: 16px 20px;
      border-top: 1px solid #eee;
    }
  }
}
.address-form {
  .form-item {
    margin-bottom: 20px;
    
    :deep(.el-form-item__label) {
      padding: 0 0 8px;
      line-height: 1;
      font-size: 14px;
      color: #333;
      
      &::before {
        margin-right: 4px;
        color: #ff4d4f;
      }
    }
    
    :deep(.el-input__wrapper),
    :deep(.el-textarea__inner) {
      box-shadow: none;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      
      &:hover,
      &:focus {
        border-color: var(--el-color-primary);
      }
    }
    
    :deep(.el-input__wrapper) {
      padding: 1px 11px;
    }
    
    :deep(.el-input__count) {
      background: transparent;
    }
  }
  
}

.dialog-footer {
  text-align: right;
  
  .el-button + .el-button {
    margin-left: 12px;
  }
}

.address-container {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

  .address-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 500;
      color: #333;
    }

    .add-btn {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .address-list {
    .address-item {
      margin-bottom: 16px;
      padding: 16px 20px;
      background: #f8f9fa;
      border-radius: 8px;
      border: 1px solid transparent;
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        background: #fff;
        border-color: var(--el-color-primary);
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      }

      &.active {
        background: #fff;
        border-color: var(--el-color-primary);
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      }

      .address-content {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
      }

      .address-info {
        flex: 1;

        .user-info {
          margin-bottom: 8px;
          display: flex;
          align-items: center;
          gap: 12px;

          .name {
            font-size: 16px;
            font-weight: 500;
            color: #333;
          }

          .phone {
            color: #666;
          }
        }

        .address-detail {
          display: flex;
          align-items: flex-start;
          gap: 8px;
          color: #666;
          font-size: 14px;
          line-height: 1.5;

          .el-icon {
            margin-top: 3px;
            color: #999;
          }
        }
      }

      .address-actions {
        display: flex;
        gap: 8px;

        .action-btn {
          padding: 4px;
          
          &:hover {
            color: var(--el-color-primary);
          }

          &.delete:hover {
            color: var(--el-color-danger);
          }
        }
      }
    }
  }

  .dialog-footer {
    margin-top: 24px;
    display: flex;
    justify-content: center;
    gap: 16px;
  }
}

:deep(.el-dialog) {
  border-radius: 8px;
  
  .el-dialog__header {
    margin: 0;
    padding: 20px 24px;
    border-bottom: 1px solid #eee;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #eee;
  }
}
.depot-item {
  border: 1px solid transparent;
  border-radius: 3px;
  transition: border-color 0.3s ease-in-out;
  cursor: pointer;
}

.depot-item:hover {
  border-color: rgb(9, 109, 217);
}

.depotFont {
  transition: border-color 0.3s ease-in-out;
  cursor: pointer;
}

.depotFont:hover {
  color: #2d6cd3;
}

.active {
  border-color: rgb(9, 109, 217);
  color: #2d6cd3;
}

.delete-icon {
  color: #727272;
  margin-left: 10px;
  cursor: pointer;
}

.delete-icon:hover {
  color: #f56c6c;
}

:deep(.el-dialog__wrapper) {
  z-index: 90000000;
}
</style>