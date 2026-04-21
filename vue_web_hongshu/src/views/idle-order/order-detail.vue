<template>
  <div class="container">
    <!-- 添加加载状态显示 -->
    <el-loading :full-screen="true" v-if="loading" />
    <div class="push-container" v-else>
      <template v-if="productOrder && productInfo">
        <div class="header"><span class="header-icon"></span><span class="header-title">{{ $t("order.detail") }}</span></div>
        <div class="img-list">
          <div style="color: white; font-size: 20px; font-weight: bold">{{ $t("order.orderStatusWithColon") }}{{ getPaymentStatusText }}</div>
        </div>
        <div style="margin-left: 20px">
          <el-divider style="width: 576px" />
        </div>
        <!-- 物流 -->
        <div class="header-title" style="margin-top: 10px; margin-bottom: 15px; font-size: 18px; padding-left: 20px">
          {{ $t("order.logisticsInfo") }}
        </div>
        <div style="margin-left: 20px">
          <el-descriptions class="margin-top" :column="2" border>
            <el-descriptions-item>
              <template v-slot:label>
                <i class="el-icon-s-order"></i>
                {{ $t("order.logisticsMethod") }}
              </template>
              {{ displayPostType }}
            </el-descriptions-item>
            <el-descriptions-item v-if="productOrder.productPostStatus === 0">
              <template v-slot:label>
                <i class="el-icon-s-release"></i>
                {{ $t("order.logistics") }}
              </template>
              {{ productOrder.shipCompany }} {{ productOrder.shipNum }}
            </el-descriptions-item>
            <el-descriptions-item v-if="productOrder.productPostStatus === 1">
              <template v-slot:label>
                <i class="el-icon-s-release"></i>
                {{ $t("order.pickupCode") }}
              </template>
              {{ productOrder.postSelfCode }}
            </el-descriptions-item>
            <el-descriptions-item>
              <template v-slot:label>
                <i class="el-icon-s-claim"></i>
                {{ $t("order.addressLabel") }}
              </template>
              <span v-if="productOrder.postType === postTypeShip">
                {{ productOrder.postUsername }} {{ productOrder.postPhone }} {{ productOrder.postAddress }}</span
              >
              <span v-if="productOrder.postType === postTypePickup"> {{ $t("order.selfPickupCode") }} {{ productOrder.postSelfCode }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div style="margin-left: 20px">
          <el-divider style="width: 576px" />
        </div>

        <!-- 订单 -->
        <div class="header-title" style="margin-top: 10px; margin-bottom: 15px; font-size: 18px; padding-left: 20px">
          {{ $t("order.orderInfo") }}
        </div>
        <div style="margin-left: 20px">
          <el-descriptions class="margin-top" :column="1" border>
            <el-descriptions-item>
              <template v-slot:label>
                <i class="el-icon-s-order"></i>
                {{ $t("order.orderNumber") }}
              </template>
              {{ productOrder.orderNumber }}
            </el-descriptions-item>
            <el-descriptions-item>
              <template v-slot:label>
                <i class="el-icon-s-release"></i>
                {{ $t("order.productTitle") }}
              </template>
              {{ productInfo.title }}
            </el-descriptions-item>
            <el-descriptions-item>
              <template v-slot:label>
                <i class="el-icon-s-claim"></i>
                {{ $t("order.productDesc") }}
              </template>
              {{ productInfo.description }}
            </el-descriptions-item>
            <el-descriptions-item>
              <template v-slot:label>
                <i class="el-icon-s-claim"></i>
                {{ $t("order.productAmount") }}
              </template>
              {{ convert.to_price(productInfo.price) }} {{ $t("order.yuan") }}
            </el-descriptions-item>
            <el-descriptions-item v-if="paymentOrder">
              <template v-slot:label>
                <i class="el-icon-s-claim"></i>
                {{ $t("order.payAmount") }}
              </template>
              {{ convert.to_price(paymentOrder.payPrice) }} {{ $t("order.yuan") }}
            </el-descriptions-item>
            <el-descriptions-item v-else>
              <template v-slot:label>
                <i class="el-icon-s-claim"></i>
                {{ $t("order.amountDue") }}
              </template>
              {{ convert.to_price((Number(productOrder.answerBuyMoney) || 0) / 100) }} {{ $t("order.yuan") }}
            </el-descriptions-item>
            <el-descriptions-item v-if="paymentOrder">
              <template v-slot:label>
                <i class="el-icon-s-claim"></i>
                {{ $t("order.payMethodLabel") }}
              </template>
              <span :class="getPayTypeClass" class="pay-type-badge">
                <img
                  v-if="paymentOrder.payType === '2'"
                  src="https://t.alipayobjects.com/images/T1HHFgXXVeXXXXXXXX.png"
                  :alt="$t('order.alipay')"
                  class="pay-type-icon"
                />
                <img
                  v-else-if="paymentOrder.payType === '1'"
                  src="https://res.wx.qq.com/a/wx_fed/assets/res/OTE0YTAw.png"
                  :alt="$t('order.wechatPay')"
                  class="pay-type-icon wechat-icon"
                />
                <span class="pay-type-text">{{ getPayTypeText }}</span>
              </span>
            </el-descriptions-item>
            <el-descriptions-item v-if="paymentPay && paymentPay.finishTime">
              <template v-slot:label>
                <i class="el-icon-s-claim"></i>
                {{ $t("order.payTime") }}
              </template>
              {{ convert.parseTime(paymentPay.finishTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div style="margin-left: 20px">
          <el-divider style="width: 576px" />
        </div>
        <div class="header-title" style="margin-top: 10px; margin-bottom: 15px; font-size: 18px; padding-left: 20px">
          {{ $t("order.evaluateInfo") }}
        </div>
        <div style="margin-left: 20px">
          <div class="info">
            <div>
              <div class="user-info">
                <el-rate
                  :disabled="true"
                  v-model="productOrder.evaScore"
                  :texts="rateTexts"
                  show-text
                ></el-rate>
              </div>
              <div class="interaction-content" style="margin-top: 25px">
                <el-input
                  :disabled="true"
                  type="textarea"
                  :placeholder="$t('order.evaluateHint')"
                  maxlength="150"
                  :autosize="{ minRows: 5 }"
                  show-word-limit
                  v-model="productOrder.evaContent"
                ></el-input>
              </div>
            </div>
          </div>
        </div>
      </template>
      <div class="submit">
        <button class="publishBtn" type="submit" @click="back">
          <span class="btn-content">{{ $t("common.back") }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import convert from "@/utils/convert";
import type { ProductInfo } from "@/type/product";
import type { ProductOrder, PaymentOrder, PaymentPay } from "@/type/order";
import { getProductOrderDetail } from "@/api/idle";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/userStore";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

/** 与后端返回的 postType 文案一致，用于条件判断 */
const postTypeShip = "物流发货";
const postTypePickup = "用户自提";

// 使用ref替代data
const productOrder = ref<ProductOrder>({} as ProductOrder);
const productInfo = ref<ProductInfo>({} as ProductInfo);
const paymentOrder = ref<PaymentOrder | null>(null);
const paymentPay = ref<PaymentPay | null>(null);
const loading = ref(false); // 添加加载状态

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const userInfo = ref<any>({});

const displayPostType = computed(() => {
  const pt = productOrder.value.postType;
  if (pt === postTypeShip) return t("order.logisticsShip");
  if (pt === postTypePickup) return t("order.userPickup");
  return pt ?? "";
});

const rateTexts = computed(() => [
  t("order.rateVeryBad"),
  t("order.rateBad"),
  t("order.rateNormal"),
  t("order.rateGood"),
  t("order.rateVeryGood"),
]);

// 添加订单状态文字显示的计算属性
const getPaymentStatusText = computed(() => {
  const s = productOrder.value.orderStatus != null ? String(productOrder.value.orderStatus) : "";
  switch (s) {
    case "0":
      return t("order.unpaid");
    case "1":
      return t("order.pendingPay");
    case "2":
      return t("order.paying");
    case "3":
      return t("order.paidStatus");
    case "4":
      return t("order.transactionComplete");
    case "5":
      return t("order.cancelled");
    default:
      return t("order.unknownStatus");
  }
});

// 支付类型文字
const getPayTypeText = computed(() => {
  const p = paymentOrder.value;
  if (!p?.payType) return "—";
  switch (p.payType) {
    case "1":
      return t("order.wechatPay");
    case "2":
      return t("order.alipay");
    default:
      return p.payTypeName || t("order.unknownPayMethod");
  }
});

// 支付类型样式类
const getPayTypeClass = computed(() => {
  const p = paymentOrder.value;
  if (!p?.payType) return "";
  switch (p.payType) {
    case "1":
      return "pay-type-wechat";
    case "2":
      return "pay-type-alipay";
    default:
      return "pay-type-alipay";
  }
});

// 获取订单详情
const getDetail = async () => {
  const orderNumber = route.query.orderNumber;

  // 添加参数验证
  if (!orderNumber) {
    ElMessage.error(t("order.orderIdRequired"));
    router.push({ name: "user", query: { uid: userInfo.value.id } }); // 如果没有orderId，返回用户页面
    return;
  }

  loading.value = true;
  try {
    const res = await getProductOrderDetail(orderNumber as string);
    if (!res.data) {
      throw new Error(t("order.getOrderDetailFailed"));
    }

    // 解构赋值并添加空值检查
    const { productOrder: po, productInfo: pi, paymentOrder: pao, paymentPay: pp } = res.data;
    if (!po || !pi) {
      throw new Error(t("order.orderDataIncomplete"));
    }

    productOrder.value = po;
    productInfo.value = pi;
    paymentOrder.value = pao ?? null;
    paymentPay.value = pp ?? null;
  } catch (error) {
    console.error("获取订单详情失败:", error);
    ElMessage.error(error instanceof Error ? error.message : t("order.getOrderDetailFailed"));
    router.push({ name: "user", query: { uid: userInfo.value.id } }); // 发生错误时返回用户页面
  } finally {
    loading.value = false;
  }
};

// 返回按钮
const back = () => {
  router.push({ name: "user", query: { uid: userInfo.value.id } });
};

// 生命周期
onMounted(() => {
  userInfo.value = userStore.getUserInfo();
  getDetail();
});
</script>

<style lang="less" scoped>
.container {
  flex: 1;
  padding-top: 72px;

  .push-container {
    margin-left: 12vw;
    width: 660px;
    border-radius: 8px;
    box-sizing: border-box;
    box-shadow: var(--el-box-shadow-lighter);

    .header {
      padding: 15px 5px;
      line-height: 16px;
      font-size: 16px;
      font-weight: 400;

      .header-icon {
        position: relative;
        top: 2px;
        display: inline-block;
        width: 6px;
        height: 19px;
        background: #3a64ff;
        border-radius: 3px;
        margin-right: 5px;
      }

      .header-title {
        line-height: 20px;
        font-size: 20px;
      }
    }

    .img-list {
      width: 576px;
      margin-left: 20px;
      padding: 0;
      height: 200px;
      background-color: #3a64ff;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-direction: column;
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

    .categorys {
      padding: 0 12px 10px 12px;
    }

    .trend-container {
      padding-left: 15px;

      .trend-item {
        display: flex;
        flex-direction: row;
        padding-top: 10px;
        max-width: 100vw;

        .main {
          flex-grow: 1;
          flex-shrink: 1;
          display: flex;
          flex-direction: row;
          padding-bottom: 12px;
          border-bottom: 1px solid rgba(0, 0, 0, 0.08);

          .details-box {
            width: 25%;
          }

          .info {
            flex-grow: 1;
            flex-shrink: 1;
            margin-left: 10px;

            .user-info {
              display: flex;
              flex-direction: row;
              align-items: center;
              font-size: 16px;
              font-weight: 600;
              margin-bottom: 4px;

              a {
                color: #333;
              }
            }

            .interaction-hint {
              font-size: 14px;
              color: rgba(51, 51, 51, 0.6);
              margin-bottom: 8px;
            }

            .interaction-content {
              display: flex;
              font-size: 14px;
              color: #333;
              margin-bottom: 12px;
              line-height: 140%;

              display: -webkit-box;
              -webkit-box-orient: vertical;
              -webkit-line-clamp: 3;
              word-break: break-all;
              overflow: hidden;
            }

            .interaction-price {
              font-weight: bolder;
              font-size: 16px;
              color: red;
            }

            .interaction-footer {
              margin: 8px 12px 0 0;
              padding: 0 12px;
              display: flex;
              justify-content: space-between;
              align-items: center;

              .icon-item {
                display: flex;
                justify-content: left;
                align-items: center;
                color: rgba(51, 51, 51, 0.929);

                .count {
                  margin-left: 3px;
                }
              }
            }
          }
        }
      }
    }

    .submit {
      padding: 0 12px 10px 20px;
      margin-top: 30px;

      button {
        width: 80px;
        height: 32px;
        font-size: 14px;
      }

      .publishBtn {
        background-color: #ff2442;
        color: #fff;
        border-radius: 4px;
        cursor: pointer;
      }

      .clearBtn {
        cursor: pointer;
        border-radius: 4px;
        margin-left: 10px;
        border: 1px solid rgb(217, 217, 217);
      }
    }
  }
}

/* 支付类型样式 */
.pay-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;

  .pay-type-icon {
    width: 50px;
    height: 20px;
    flex-shrink: 0;
    object-fit: contain;

    &.wechat-icon {
      width: 20px;
      height: 20px;
    }
  }

  .pay-type-text {
    font-weight: 600;
  }
}

.pay-type-wechat {
  // background-color: #f0f9ff;
  color: #07c160;
  border: 1px solid #07c160;

  .pay-type-text {
    color: #07c160;
  }
}

.pay-type-alipay {
  // background-color: #fff7e6;
  color: #1677ff;
  border: 1px solid #1677ff;

  .pay-type-text {
    color: #1677ff;
  }
}
</style>
