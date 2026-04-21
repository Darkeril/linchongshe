<template>
  <div class="container">
    <div class="push-container">
      <el-form
        label-position="left"
        class="check-form"
        :model="formData"
        :rules="rules"
        ref="formRef"
        label-width="80px"
        :show-message="false"
        status-icon
        :hide-required-asterisk="true"
        size="small"
      >
        <div class="header">
          <span class="header-icon"></span>
          <span class="header-title">{{ $t("order.confirmBuy") }}</span>
        </div>

        <!-- 商品信息部分 - 放在最前面 -->
        <div class="section-container">
          <div class="section-header">
            <span class="section-title">{{ $t("order.productInfo") }}</span>
          </div>
          <div class="product-card">
            <div class="product-image-wrapper">
              <el-image class="product-image" :src="productInfo.image" fit="cover" :lazy="true" />
            </div>
            <div class="product-info-wrapper">
              <div class="product-title">{{ productInfo.title }}</div>
              <div class="product-description">{{ productInfo.description }}</div>
              <div class="product-meta">
                <span class="product-tag">{{ productInfo.postType === 0 ? $t("common.mail") : $t("common.selfPickup") }}</span>
                <span class="product-location">{{ productInfo.city }}</span>
              </div>
              <div class="product-price-wrapper">
                <span class="product-price">￥{{ convert.to_price(productInfo.price) }}</span>
                <span class="product-original-price">￥{{ convert.to_price(productInfo.originalPrice) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 收货地址部分 -->
        <div class="section-container" v-if="productInfo.postType !== 1">
          <div class="section-header">
            <span class="section-title">{{ $t("order.shippingAddress") }}</span>
          </div>

          <!-- 地址卡片展示 -->
          <div v-if="formData.idname && formData.phone && formData.address" class="address-card">
            <div class="address-card-content" @click="open">
              <div class="address-user-info">
                <span class="address-name">{{ formData.idname }}</span>
                <span class="address-phone">{{ formData.phone }}</span>
              </div>
              <div class="address-detail">
                <el-icon class="address-icon"><Location /></el-icon>
                <span class="address-text">{{ formData.address }}</span>
              </div>
            </div>
            <el-button type="text" class="address-edit-btn" @click.stop="open" size="small">
              <el-icon><Edit /></el-icon>
              {{ $t("order.changeAddress") }}
            </el-button>
          </div>

          <!-- 未选择地址时的提示 -->
          <div v-else class="address-empty" @click="open">
            <el-empty :image-size="80" :description="$t('order.selectAddress')" :show-image="false" />
          </div>

          <!-- 隐藏的表单字段，用于表单验证 -->
          <div style="display: none">
            <el-form-item prop="idname" class="address-form-item">
              <el-input v-model="formData.idname" />
            </el-form-item>
            <el-form-item prop="phone" class="address-form-item">
              <el-input v-model="formData.phone" />
            </el-form-item>
            <el-form-item prop="address" class="address-form-item">
              <el-input v-model="formData.address" />
            </el-form-item>
          </div>
        </div>

        <!-- 支付方式选择 -->
        <div class="section-container">
          <div class="section-header">
            <span class="section-title">{{ $t("order.payMethodLabel") }}</span>
          </div>
          <div class="payment-options">
            <el-radio-group v-model="payType" class="payment-group">
              <el-radio label="2" class="payment-radio" :disabled="isAlipayDisabled">
                <div class="payment-card" :class="{ 'payment-card-disabled': isAlipayDisabled }">
                  <img
                    src="@/assets/zfb.png"
                    :alt="$t('order.alipay')"
                    class="payment-logo-img"
                    :class="{ 'payment-logo-disabled': isAlipayDisabled }"
                  />
                  <span class="payment-label">{{ $t("order.alipay") }}</span>
                </div>
              </el-radio>
              <el-radio label="1" class="payment-radio" :disabled="isWechatDisabled">
                <div class="payment-card" :class="{ 'payment-card-disabled': isWechatDisabled }">
                  <img
                    src="@/assets/wx.png"
                    :alt="$t('order.wechatPay')"
                    class="payment-logo-img"
                    :class="{ 'payment-logo-disabled': isWechatDisabled }"
                  />
                  <span class="payment-label">{{ $t("order.wechat") }}</span>
                </div>
              </el-radio>
            </el-radio-group>
          </div>
        </div>

        <!-- 按钮部分 -->
        <div class="submit-section">
          <div class="total-price">
            <span class="total-label">{{ $t("order.actualPay") }}：</span>
            <span class="total-amount">￥{{ convert.to_price(productInfo.price) }}</span>
          </div>
          <div class="submit-buttons">
            <el-button class="cancel-btn" @click="router.push('/idle')">{{ $t("common.cancel") }}</el-button>
            <el-button type="primary" class="confirm-btn" @click.prevent="confirmBuy">{{ $t("order.confirmBuy") }}</el-button>
          </div>
        </div>
      </el-form>
    </div>

    <!-- 地址选择抽屉 -->
    <el-drawer v-model="drawer" destroy-on-close :show-close="false" size="400px" direction="rtl">
      <UserAddress :button="true" @close-drawer="closeDrawer" @confirm="confirm" />
    </el-drawer>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/store/userStore";
import { ElForm, ElMessage } from "element-plus";
import type { FormRules, FormInstance } from "element-plus";
import { Location, Edit } from "@element-plus/icons-vue";
import convert from "@/utils/convert";
import { getProductById } from "@/api/idle";
import { createProductOrder } from "@/api/idle";
import { createPaymentOrder } from "@/api/idle";
import { getPaymentGlobalConfig } from "@/api/idle";
import UserAddress from "../user/user-address.vue";
import { useI18n } from "vue-i18n";

// 路由
const router = useRouter();
const route = useRoute();
const { t } = useI18n();

// store
const userStore = useUserStore();

// refs
const formRef = ref<FormInstance>();
const drawer = ref(false);

// 表单数据
interface FormData {
  productId: string;
  info: string;
  address: string;
  idname: string;
  phone: string;
  province?: string;
  city?: string;
  district?: string;
}

const formData = reactive<FormData>({
  productId: "",
  info: "",
  address: "",
  idname: "",
  phone: "",
  province: "",
  city: "",
  district: "",
});

// 支付方式（1-微信支付，2-支付宝支付，默认支付宝）
const payType = ref("2");

// 支付配置
const paymentConfig = ref<any>(null);
const isWechatDisabled = ref(false);
const isAlipayDisabled = ref(false);

// 商品信息
interface ProductInfo {
  postType?: number;
  image: string;
  title: string;
  description: string;
  address: string;
  city: string;
  price: number;
  originalPrice: number;
}

const productInfo = ref<ProductInfo>({
  image: "",
  title: "",
  description: "",
  address: "",
  city: "",
  price: 0,
  originalPrice: 0,
});

// 表单校验规则
const rules = computed<FormRules>(() => ({
  idname: [{ required: true, message: t("order.pleaseInputName"), trigger: "blur" }],
  phone: [{ required: true, message: t("order.pleaseInputPhone"), trigger: "blur" }],
  address: [{ required: true, message: t("order.pleaseInputAddressDetail"), trigger: "blur" }],
}));

// 初始化数据
onMounted(async () => {
  formData.productId = route.query.productId as string;

  // 获取用户信息（仅用于姓名和电话，地址需要用户自己选择）
  const userInfo = userStore.getUserInfo();
  formData.phone = userInfo.phone || "";
  formData.idname = userInfo.username || "";

  // 地址留空，提示用户点击"我的收货地址"按钮选择地址
  formData.address = "";

  // 加载支付配置
  await loadPaymentConfig();

  await getProductInfo();
});

// 加载支付配置
const loadPaymentConfig = async () => {
  try {
    const res = (await getPaymentGlobalConfig()) as any;
    console.log("支付全局配置:", res);

    if (res.code === 200 && res.data) {
      paymentConfig.value = res.data;

      // 检查全局支付开关
      if (!paymentConfig.value.paymentEnabled) {
        ElMessage.warning(t("order.paymentClosedCannotBuy"));
        isWechatDisabled.value = true;
        isAlipayDisabled.value = true;
        return;
      }

      // 根据配置设置支付方式的可用状态
      isWechatDisabled.value = !paymentConfig.value.wechatQrcodeEnabled;
      // 支付宝支付：沙箱或扫码，只要有一个可用就可以选择
      const alipayAvailable = paymentConfig.value.alipaySandboxEnabled || paymentConfig.value.alipayQrcodeEnabled;
      isAlipayDisabled.value = !alipayAvailable;

      // 如果所有支付方式都被禁用
      if (isWechatDisabled.value && isAlipayDisabled.value) {
        ElMessage.warning(t("order.noPaymentMethodForBuy"));
      } else {
        // 如果当前选中的支付方式被禁用，自动选择第一个可用的支付方式
        if (payType.value === "1" && isWechatDisabled.value) {
          payType.value = "2"; // 切换到支付宝
        } else if (payType.value === "2" && isAlipayDisabled.value) {
          payType.value = "1"; // 切换到微信
        }
      }
    }
  } catch (error) {
    console.error("加载支付配置失败:", error);
    // 加载失败时不影响页面使用，使用默认配置
  }
};

// 获取商品信息
const getProductInfo = async () => {
  const res = await getProductById(formData.productId);
  productInfo.value = res.data;
  productInfo.value.image = res.data.cover;
};

// 格式化完整地址（包含省市区）
const formatFullAddress = (item: any) => {
  const parts = [];
  if (item.province) parts.push(item.province);
  if (item.city) parts.push(item.city);
  if (item.district) parts.push(item.district);
  if (item.address) parts.push(item.address);
  return parts.join(" ");
};

// 确认地址选择
const confirm = (item: any) => {
  formData.phone = item.phone;
  formData.idname = item.name;
  formData.province = item.province || "";
  formData.city = item.city || "";
  formData.district = item.district || "";
  // 组合省市区和详细地址
  formData.address = formatFullAddress(item);
  drawer.value = false;
};

// 确认购买
const confirmBuy = async () => {
  // 再次检查支付配置（双重保险）
  if (!paymentConfig.value || !paymentConfig.value.paymentEnabled) {
    ElMessage.warning(t("order.paymentOffCannotSubmit"));
    return;
  }

  // 检查是否所有支付方式都被禁用
  if (isWechatDisabled.value && isAlipayDisabled.value) {
    ElMessage.warning(t("order.noPaymentCannotSubmit"));
    return;
  }

  // 如果是邮寄方式，需要验证地址
  if (productInfo.value.postType !== 1) {
    if (!formData.idname || !formData.phone || !formData.address) {
      ElMessage.warning(t("order.selectAddress"));
      return;
    }
  }

  // 表单验证
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch (error) {
    ElMessage.warning(t("order.completeOrderInfo"));
    return;
  }

  console.log("确认购买，选择的支付类型:", payType.value);
  const orderRes = await createProductOrder(formData);
  console.log("创建商品订单成功:", orderRes.data);
  console.log("准备创建支付订单，支付类型:", payType.value);
  const payRes = await createPaymentOrder(orderRes.data, payType.value);
  console.log("创建支付订单成功:", payRes.data);
  router.push(`/paymentPay?paymentOrderId=${payRes.data}`);
};

// 打开抽屉
const open = () => {
  drawer.value = true;
};

// 关闭抽屉
const closeDrawer = () => {
  drawer.value = false;
};
</script>

<style lang="less" scoped>
.container {
  flex: 1;
  padding-top: 90px;
  margin-left: 2vw;

  .push-container {
    margin-left: 12vw;
    width: 660px;
    border-radius: 8px;
    box-sizing: border-box;
    box-shadow: var(--el-box-shadow-lighter);

    .header {
      padding: 10px 2px;
      line-height: 16px;
      font-size: 16px;
      font-weight: 400;

      .header-icon {
        position: relative;
        top: 2px;
        display: inline-block;
        width: 6px;
        height: 19px;
        background: #3d8af5;
        border-radius: 3px;
        margin-right: 5px;
      }

      .header-title {
        line-height: 20px;
        font-size: 20px;
      }
    }

    /* 区块容器样式 */
    .section-container {
      margin: 20px;
      padding: 10px;
      background: #fff;
      border-radius: 8px;
      border: 1px solid #f0f0f0;

      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #f5f5f5;

        .section-title {
          font-size: 16px;
          font-weight: 600;
          color: #333;
        }
      }
    }

    /* 商品信息卡片 */
    .product-card {
      display: flex;
      gap: 16px;
      padding: 12px;
      background: #fafafa;
      border-radius: 8px;

      .product-image-wrapper {
        flex-shrink: 0;
        width: 120px;
        height: 120px;
        border-radius: 8px;
        overflow: hidden;
        background: #f5f5f5;

        .product-image {
          width: 100%;
          height: 100%;
        }
      }

      .product-info-wrapper {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .product-title {
          font-size: 16px;
          font-weight: 600;
          color: #333;
          margin-bottom: 8px;
          line-height: 1.4;
        }

        .product-description {
          font-size: 13px;
          color: #666;
          line-height: 1.5;
          margin-bottom: 8px;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .product-meta {
          display: flex;
          gap: 12px;
          margin-bottom: 8px;

          .product-tag,
          .product-location {
            font-size: 12px;
            color: #999;
            padding: 2px 8px;
            background: #f0f0f0;
            border-radius: 4px;
          }
        }

        .product-price-wrapper {
          display: flex;
          align-items: baseline;
          gap: 8px;

          .product-price {
            font-size: 20px;
            font-weight: 700;
            color: #ff2442;
          }

          .product-original-price {
            font-size: 13px;
            color: #999;
            text-decoration: line-through;
          }
        }
      }
    }

    /* 地址卡片样式 */
    .address-card {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      padding: 16px;
      background: linear-gradient(135deg, #f5f7fa 0%, #fafbfc 100%);
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
        box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
      }

      .address-card-content {
        flex: 1;
        min-width: 0;
        cursor: pointer;

        .address-user-info {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 12px;

          .address-name {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
          }

          .address-phone {
            font-size: 14px;
            color: #606266;
          }
        }

        .address-detail {
          display: flex;
          align-items: flex-start;
          gap: 8px;
          line-height: 1.6;

          .address-icon {
            color: #409eff;
            font-size: 18px;
            margin-top: 2px;
            flex-shrink: 0;
          }

          .address-text {
            font-size: 14px;
            color: #606266;
            word-break: break-all;
            flex: 1;
          }
        }
      }

      .address-edit-btn {
        flex-shrink: 0;
        margin-left: 16px;
        color: #409eff;
        padding: 8px 12px;

        &:hover {
          background: rgba(64, 158, 255, 0.1);
        }
      }
    }

    /* 地址为空时的样式 */
    .address-empty {
      padding: 14px 20px;
      text-align: center;
      background: #fafafa;
      border: 1px dashed #d9d9d9;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
        background: #f0f7ff;
      }

      :deep(.el-empty) {
        padding: 0;
      }

      :deep(.el-empty__description) {
        color: #909399;
        margin-bottom: 0;
        font-size: 14px;
      }
    }

    /* 隐藏的表单字段样式 */
    .address-form {
      .address-form-item {
        margin-bottom: 0;

        :deep(.el-form-item__content) {
          margin-left: 0 !important;
        }
      }

      .address-input {
        width: 100%;
      }
    }

    /* 支付方式样式 */
    .payment-options {
      .payment-group {
        display: flex;
        gap: 16px;
        width: 100%;
      }

      .payment-radio {
        flex: 1;
        margin: 0;
        height: auto;

        :deep(.el-radio__input) {
          display: none;
        }

        :deep(.el-radio__label) {
          padding: 0;
          width: 100%;
        }

        .payment-card {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 1px;
          border: 2px solid #e0e0e0;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.3s;
          background: #fff;

          &:hover {
            border-color: #409eff;
            background: #f0f7ff;
          }

          .payment-logo-img {
            width: 36px;
            height: 36px;
            object-fit: contain;
            margin-top: 8px;
            margin-bottom: 8px;
          }

          .payment-logo-svg {
            width: 48px;
            height: 48px;
            margin-bottom: 8px;
          }

          .payment-label {
            font-size: 14px;
            color: #333;
            font-weight: 500;
          }
        }

        /* 禁用状态样式 */
        .payment-card-disabled {
          opacity: 0.5;
          cursor: not-allowed !important;
          background: #f5f5f5 !important;
          border-color: #e0e0e0 !important;

          &:hover {
            border-color: #e0e0e0 !important;
            background: #f5f5f5 !important;
          }

          .payment-logo-disabled {
            opacity: 0.5;
            filter: grayscale(100%);
          }

          .payment-label {
            color: #999 !important;
          }
        }

        &.is-checked .payment-card {
          border-color: #409eff;
          background: #ecf5ff;

          .payment-label {
            color: #409eff;
            font-weight: 600;
          }
        }
      }
    }

    /* 提交区域 */
    .submit-section {
      margin: 20px;
      padding: 20px;
      background: #fff;
      border-top: 1px solid #f0f0f0;

      .total-price {
        display: flex;
        justify-content: flex-end;
        align-items: baseline;
        margin-bottom: 16px;
        padding-bottom: 16px;
        border-bottom: 1px solid #f5f5f5;

        .total-label {
          font-size: 14px;
          color: #666;
          margin-right: 8px;
        }

        .total-amount {
          font-size: 24px;
          font-weight: 700;
          color: #ff2442;
        }
      }

      .submit-buttons {
        display: flex;
        justify-content: flex-end;
        gap: 12px;

        .cancel-btn {
          width: 100px;
          height: 40px;
        }

        .confirm-btn {
          width: 140px;
          height: 40px;
          font-size: 16px;
          font-weight: 600;
          background: #3d8af5;
          border-color: #3d8af5;

          &:hover {
            background: #3d8af5;
            border-color: #3d8af5;
          }
        }
      }
    }

    .push-content {
      position: relative;

      .scroll-tag-container {
        position: absolute;
        width: 98%;
        background-color: #fff;
        z-index: 99999;
        border: 1px solid #f4f4f4;
        height: 300px;
        overflow: auto;

        .scrollbar-tag-item {
          display: flex;
          align-items: center;
          height: 30px;
          margin: 10px;
          text-align: center;
          border-radius: 4px;
          padding-left: 2px;
          color: #484848;
          font-size: 14px;
        }

        .scrollbar-tag-item:hover {
          background-color: #f8f8f8;
        }
      }

      .input-title {
        margin-bottom: 5px;
        font-size: 12px;
      }

      .input-content {
        font-size: 12px;
      }
    }

    .btns {
      padding: 0 12px 0px 20px;

      button {
        min-width: 62px;
        width: 62px;
        margin: 0 6px 0 0;
        height: 18px;
      }

      .css-fm44j {
        -webkit-font-smoothing: antialiased;
        appearance: none;
        font-family: RedNum, RedZh, RedEn, -apple-system;
        vertical-align: middle;
        text-decoration: none;
        border: 1px solid rgb(217, 217, 217);
        outline: none;
        user-select: none;
        cursor: pointer;
        display: inline-flex;
        -webkit-box-pack: center;
        justify-content: center;
        -webkit-box-align: center;
        align-items: center;
        margin-right: 16px;
        border-radius: 4px;
        background-color: white;
        color: rgb(38, 38, 38);
        height: 24px;
        font-size: 12px;
      }
    }
  }
}
.push-content {
  width: 100%;
  padding: 0 20px;
  box-sizing: border-box;

  :deep(.el-form-item) {
    margin-bottom: 15px;

    .custom-input {
      width: 80%; // 或者使用具体像素值，如 400px
      max-width: 500px; // 设置最大宽度
      margin: 0 auto; // 居中显示
      margin-left: 20px; // 左边距
    }
  }
}
</style>
