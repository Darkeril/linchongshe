<template>
  <div class="config-page">
    <a-card class="config-card">
      <a-tabs v-model:active-key="activeName" type="card">
        <!-- 系统配置（主标签，包含子标签） -->
        <a-tab-pane key="system-main">
          <template #title>
            <icon-settings />
            {{ $t('biz.systemConfigMainTab') }}
          </template>
          <a-tabs
            v-model:active-key="systemSubTab"
            type="line"
            style="margin-top: 20px"
          >
            <!-- 网站配置 -->
            <a-tab-pane key="website">
              <template #title> {{ $t('biz.websiteConfig') }} </template>
              <WebsiteConfig
                ref="websiteConfigRef"
                :initial-data="websiteForm"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 系统配置 -->
            <a-tab-pane key="system">
              <template #title> {{ $t('biz.systemConfigSubTab') }} </template>
              <SystemConfig
                ref="systemConfigRef"
                :initial-data="systemForm"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 黑名单管理 -->
            <a-tab-pane key="blacklist">
              <template #title> {{ $t('biz.blacklistConfigTab') }} </template>
              <BlacklistConfig
                ref="blacklistConfigRef"
                :initial-data="blacklistForm"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 演示配置 -->
            <a-tab-pane key="demo">
              <template #title>
                <icon-settings />
                {{ $t('biz.demoConfigTab') }}
              </template>
              <DemoConfig
                ref="demoConfigRef"
                :initial-account-data="demoAccountForm"
                :initial-site-data="demoSiteForm"
                :initial-provider="currentDemoProvider"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 系统通知配置 -->
            <a-tab-pane key="system-notification">
              <template #title>
                <icon-notification />
                {{ $t('biz.systemNotificationConfigTab') }}
              </template>
              <SystemNotificationConfig @update="handleConfigUpdate" />
            </a-tab-pane>

            <!-- 待实现功能管理 -->
            <a-tab-pane key="plannedFeatures">
              <template #title>
                <icon-list />
                {{ $t('biz.plannedFeaturesTab') }}
              </template>
              <PlannedFeatureConfig @update="handleConfigUpdate" />
            </a-tab-pane>

            <!-- 轮播图管理 -->
            <a-tab-pane key="carousel">
              <template #title>
                <icon-list />
                {{ $t('biz.mallCarouselTab') }}
              </template>
              <CarouselConfig
                ref="carouselConfigRef"
                :initial-data="carouselForm"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <a-tab-pane key="carousel-workbench">
              <template #title>
                <icon-image />
                {{ $t('biz.workbenchCarouselTab') }}
              </template>
              <CarouselConfig
                config-scope="workbench"
                :initial-data="workbenchCarouselForm"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 登录页展示区 -->
            <a-tab-pane key="admin-login-promo">
              <template #title>
                <icon-image />
                {{ $t('biz.loginPromoTab') }}
              </template>
              <AdminLoginPromoConfig
                :initial-data="adminLoginPromoForm"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>
          </a-tabs>
        </a-tab-pane>

        <!-- 第三方服务配置（主标签，包含子标签） -->
        <a-tab-pane key="third-party-main">
          <template #title>
            <icon-settings />
            {{ $t('biz.thirdPartyConfigTab') }}
          </template>
          <a-tabs
            v-model:active-key="thirdPartySubTab"
            type="line"
            style="margin-top: 20px"
          >
            <!-- OSS存储配置 -->
            <a-tab-pane key="oss">
              <template #title>
                <icon-folder />
                {{ $t('biz.ossStorageTab') }}
              </template>
              <OssConfig
                ref="ossConfigRef"
                :initial-data="ossForm"
                :initial-provider="currentOssProvider"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 短信配置 -->
            <a-tab-pane key="sms">
              <template #title>
                <icon-message />
                {{ $t('biz.smsConfig') }}
              </template>
              <SmsConfig
                ref="smsConfigRef"
                :initial-data="smsForm"
                :initial-provider="currentSmsProvider"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 支付配置 -->
            <a-tab-pane key="payment">
              <template #title> {{ $t('biz.payConfig') }} </template>
              <PaymentConfig
                ref="paymentConfigRef"
                :initial-alipay-data="alipayForm"
                :initial-wechat-pay-data="weChatPayForm"
                :initial-provider="currentPayProvider"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 微信登录配置（主站 + IM 小程序同一页） -->
            <a-tab-pane key="wechat-login">
              <template #title> {{ $t('biz.wechatLoginConfigTab') }} </template>
              <WeChatLoginConfig
                ref="weChatLoginConfigRef"
                :initial-data="wechatLoginForm"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 自动审核配置 -->
            <a-tab-pane key="auto-audit">
              <template #title> {{ $t('biz.autoAuditConfigTab') }} </template>
              <AutoAuditConfig
                ref="autoAuditConfigRef"
                :initial-data="autoAuditForm"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 高德地图配置 -->
            <a-tab-pane key="amap">
              <template #title>
                <icon-location />
                {{ $t('biz.amapConfigTab') }}
              </template>
              <AmapConfig
                ref="amapConfigRef"
                :initial-data="amapForm"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 验证码配置 -->
            <a-tab-pane key="captcha">
              <template #title>
                <icon-lock />
                {{ $t('biz.captchaAjTab') }}
              </template>
              <CaptchaConfig
                ref="captchaConfigRef"
                :initial-data="captchaForm"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>

            <!-- 爬虫管理（小红书等外部数据源） -->
            <a-tab-pane key="spider">
              <template #title>
                <icon-link />
                {{ $t('biz.xiaohongshuSpiderTab') }}
              </template>
              <SpiderConfig
                ref="spiderConfigRef"
                :initial-data="spiderForm"
                @update="handleConfigUpdate"
              />
            </a-tab-pane>
          </a-tabs>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Message } from '@arco-design/web-vue';
import {
  IconSettings,
  IconPlus,
  IconWechat,
  IconWechatpay,
  IconAlipayCircle,
  IconFolder,
  IconMessage,
  IconLock,
  IconLocation,
  IconUser,
  IconLink,
  IconExclamationCircle,
  IconNotification,
  IconRefresh,
  IconList,
  IconImage,
} from '@arco-design/web-vue/es/icon';
import {
  getAllConfig,
  editWebsiteConfig,
  editSystemConfig,
} from '@/api/system/config';
import WebsiteConfig from './components/WebsiteConfig.vue';
import SystemConfig from './components/SystemConfig.vue';
import BlacklistConfig from './components/BlacklistConfig.vue';
import WeChatLoginConfig from './components/WeChatLoginConfig.vue';
import OssConfig from './components/OssConfig.vue';
import SmsConfig from './components/SmsConfig.vue';
import PaymentConfig from './components/PaymentConfig.vue';
import AmapConfig from './components/AmapConfig.vue';
import CaptchaConfig from './components/CaptchaConfig.vue';
import DemoConfig from './components/DemoConfig.vue';
import SystemNotificationConfig from './components/SystemNotificationConfig.vue';
import PlannedFeatureConfig from './components/PlannedFeatureConfig.vue';
import AutoAuditConfig from './components/AutoAuditConfig.vue';
import SpiderConfig from './components/SpiderConfig.vue';
import CarouselConfig from './components/CarouselConfig.vue';
import AdminLoginPromoConfig from './components/AdminLoginPromoConfig.vue';

const { t } = useI18n();

// 表单数据
const systemForm = reactive({
  ossType: '',
  smsPrimary: '',
  mqType: '',
  queryPrimary: '',
  auditMode: 'manual', // 审核模式：manual(纯人工)、auto(纯自动)、hybrid(混合)
  contentAuditEnabled: false,
  productAuditEnabled: false,
  userAuditEnabled: false,
  commentAuditEnabled: false,
});

const websiteForm = reactive({
  logo: '',
  name: '',
  author: '',
  title: '',
  description: '',
  copyright: '',
  aiUrl: '',
  wechatQrCode: '',
  followUs: '',
  alipayRewardQrCode: '',
  wechatRewardQrCode: '',
});

// 文件存储相关的ref已移至WebsiteConfig组件中

// tab 激活项
const activeName = ref<string>('system-main');
// 系统配置子标签
const systemSubTab = ref<string>('website');
// 第三方服务配置子标签
const thirdPartySubTab = ref<string>('oss');

// 当前选中的服务商
const currentSmsProvider = ref<string>('aliyun');
const currentOssProvider = ref<string>('local');
const currentPayProvider = ref<string>('alipay');
const currentDemoProvider = ref<string>('account');

// OSS配置表单
const ossForm = reactive({
  local: {
    domain: '',
    url: '',
    isEnabled: false,
  },
  qiniuyun: {
    domain: '',
    accessKey: '',
    secretKey: '',
    bucketName: '',
    region: 'z1',
    isEnabled: false,
  },
  minio: {
    domain: '',
    accessKey: '',
    secretKey: '',
    bucketName: '',
    isEnabled: false,
  },
  tencent: {
    bucketName: '',
    endpoint: '',
    region: '',
    ossKeyId: '',
    ossKeySecret: '',
    isEnabled: false,
  },
  aliyun: {
    bucketName: '',
    endpoint: '',
    region: '',
    accessKeyId: '',
    accessKeySecret: '',
    isEnabled: false,
  },
});

// 短信配置表单
const smsForm = reactive({
  aliyun: {
    accessKey: '',
    secretKey: '',
    signName: '',
    templateCode: '',
    isPrimary: true,
  },
  tencent: {
    secretId: '',
    secretKey: '',
    sdkAppId: '',
    signName: '',
    templateId: '',
    region: '',
  },
  yunpian: {
    apiKey: '',
    signName: '',
  },
});

// 支付宝配置表单
const alipayForm = reactive({
  pid: '',
  appId: '',
  merchantPrivateKey: '',
  alipayPublicKey: '',
  notifyUrl: '',
  returnUrl: '',
  signType: 'RSA2',
  charset: 'utf-8',
  gatewayUrl: 'https://openapi.alipaydev.com/gateway.do',
  isEnabled: false,
});

// 微信支付配置表单
const weChatPayForm = reactive({
  mchId: '',
  pcAppId: '',
  appAppId: '',
  appId: '',
  apiV2Key: '',
  apiV3Key: '',
  certSerialNo: '',
  privateKeyPath: '',
  domain: '',
  notifyUrl: '',
  isEnabled: false,
});

// 验证码配置表单
const captchaForm = reactive({
  cacheType: 'redis',
  type: 'blockPuzzle',
  waterMark: '',
  slipOffset: 5,
  aesStatus: false,
  interferenceOptions: 0,
  jigsaw: '',
  picClick: '',
  isEnabled: false,
});

// 高德地图配置表单
const amapForm = reactive({
  key: '',
  securityKey: '',
  isEnabled: false,
});

// 演示账号配置表单
const demoAccountForm = reactive({
  username: '',
  password: '',
  description: '',
  permissions: [] as string[],
  expireTime: '',
  enabled: false,
});

// 演示站配置表单
const demoSiteForm = reactive({
  mobileUrl: '',
  webUrl: '',
  adminUrl: '',
  arcoAdminUrl: '',
  giteeUrl: '',
  githubUrl: '',
  docUrl: '',
  description: '',
  enabled: false,
});

// 黑名单管理表单
const blacklistForm = reactive({
  enabled: false,
  list: [] as any[],
});

// 微信登录配置表单
const wechatLoginForm = reactive({
  enabled: false,
  miniappAppId: '',
  miniappAppSecret: '',
  webAppId: '',
  webAppSecret: '',
  webCallbackUrl: '',
});

// 自动审核配置表单
const autoAuditForm = reactive({
  auditMode: 'manual', // 审核模式：manual(纯人工)、auto(纯自动)、hybrid(混合)
  autoAuditThreshold: 0.8, // 自动审核阈值
  baiduQianfanEnabled: false, // 是否启用百度千帆
  baiduQianfanAccessKey: '', // 百度千帆Access Key
  baiduQianfanSecretKey: '', // 百度千帆Secret Key
  baiduQianfanTextEndpoint:
    'https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined', // 文本审核接口
  baiduQianfanImageEndpoint:
    'https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined', // 图片审核接口
  baiduQianfanVideoEndpoint:
    'https://aip.baidubce.com/rest/2.0/solution/v1/video_censor/v2/user_defined', // 视频审核接口
});

// 爬虫配置表单
const spiderForm = reactive({
  enabled: false,
  interval: 5,
  maxConcurrent: 3,
  timeout: 30,
  userAgent:
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
  proxyEnabled: false,
  proxyHost: '',
});

// 轮播图配置（市集页）
const carouselForm = ref<any[]>([]);

// 管理端工作台右侧轮播图
const workbenchCarouselForm = ref<any[]>([]);

// 管理端登录页圆形展示区
const adminLoginPromoForm = ref<any[]>([]);

// 黑名单相关的表单和状态已移至BlacklistConfig组件中

// 系统通知相关状态
// 系统通知和待实现功能的状态已移至对应组件中

// 组件引用（只保留需要在父组件中操作的）
const websiteConfigRef = ref();
const systemConfigRef = ref();
const blacklistConfigRef = ref();
const weChatLoginConfigRef = ref();
const ossConfigRef = ref();
const smsConfigRef = ref();
const paymentConfigRef = ref();
const amapConfigRef = ref();
const captchaConfigRef = ref();
const demoConfigRef = ref();
const autoAuditConfigRef = ref();
const spiderConfigRef = ref();
const carouselConfigRef = ref();

// 初始化默认配置
const initDefaultConfig = (): void => {
  // 系统配置默认值
  Object.assign(systemForm, {
    ossType: '0',
    smsPrimary: 'aliyun',
    mqType: 'RabbitMQ',
    queryPrimary: 'mysql',
    blacklistEnabled: false,
    auditMode: 'manual', // 默认审核模式为人工审核
    contentAuditEnabled: false,
    productAuditEnabled: false,
    userAuditEnabled: false,
    commentAuditEnabled: false,
  });

  // 网站配置默认值
  Object.assign(websiteForm, {
    logo: '',
    name: '来因创作服务平台',
    author: '管理员',
    title: '来因创作服务平台',
    description: '基于Vue3 + Arco Design的管理系统',
    copyright: '© 2025 来因创作服务平台',
    aiUrl: '',
    wechatQrCode: '',
    followUs: '欢迎关注我们的官方账号',
    alipayRewardQrCode: '',
    wechatRewardQrCode: '',
  });

  // OSS配置默认值
  Object.assign(ossForm, {
    local: {
      domain: 'http://localhost:8080',
      url: '/upload/',
      isEnabled: true,
    },
    qiniuyun: {
      domain: '',
      accessKey: '',
      secretKey: '',
      bucketName: '',
      region: 'z1',
      isEnabled: false,
    },
    minio: {
      domain: '',
      accessKey: '',
      secretKey: '',
      bucketName: '',
      isEnabled: false,
    },
    tencent: {
      bucketName: '',
      endpoint: '',
      region: '',
      ossKeyId: '',
      ossKeySecret: '',
      isEnabled: false,
    },
    aliyun: {
      bucketName: '',
      endpoint: '',
      region: '',
      accessKeyId: '',
      accessKeySecret: '',
      isEnabled: false,
    },
  });

  // 短信配置默认值
  Object.assign(smsForm, {
    aliyun: {
      accessKey: '',
      secretKey: '',
      signName: '',
      templateCode: '',
      isPrimary: true,
    },
    tencent: {
      secretId: '',
      secretKey: '',
      sdkAppId: '',
      signName: '',
      templateId: '',
      region: 'ap-beijing',
    },
    yunpian: {
      apiKey: '',
      signName: '',
    },
  });

  // 支付宝配置默认值
  Object.assign(alipayForm, {
    pid: '',
    appId: '',
    merchantPrivateKey: '',
    alipayPublicKey: '',
    notifyUrl: '',
    returnUrl: '',
    signType: 'RSA2',
    charset: 'utf-8',
    gatewayUrl: 'https://openapi.alipaydev.com/gateway.do',
    isEnabled: false,
  });

  // 微信支付配置默认值
  Object.assign(weChatPayForm, {
    mchId: '',
    pcAppId: '',
    appAppId: '',
    appId: '',
    apiV2Key: '',
    apiV3Key: '',
    certSerialNo: '',
    privateKeyPath: '',
    domain: '',
    notifyUrl: '',
    isEnabled: false,
  });

  // 微信登录配置默认值
  Object.assign(wechatLoginForm, {
    enabled: false,
    miniappAppId: '',
    miniappAppSecret: '',
    webAppId: '',
    webAppSecret: '',
    webCallbackUrl: '',
  });

  // 自动审核配置默认值
  Object.assign(autoAuditForm, {
    auditMode: 'manual',
    autoAuditThreshold: 0.8,
    baiduQianfanEnabled: false,
    baiduQianfanAccessKey: '',
    baiduQianfanSecretKey: '',
    baiduQianfanTextEndpoint:
      'https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined',
    baiduQianfanImageEndpoint:
      'https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined',
    baiduQianfanVideoEndpoint:
      'https://aip.baidubce.com/rest/2.0/solution/v1/video_censor/v2/user_defined',
  });

  // 验证码配置默认值
  Object.assign(captchaForm, {
    cacheType: 'redis',
    type: 'blockPuzzle',
    waterMark: '',
    slipOffset: 5,
    aesStatus: false,
    interferenceOptions: 0,
    jigsaw: '',
    picClick: '',
    isEnabled: false,
  });

  // 高德地图配置默认值
  Object.assign(amapForm, {
    key: '',
    securityKey: '',
    isEnabled: false,
  });

  // 演示账号配置默认值
  Object.assign(demoAccountForm, {
    username: 'demo',
    password: '123456',
    description: '演示账号，用于系统功能展示',
    permissions: ['readonly'],
    expireTime: '',
    enabled: false,
  });

  // 演示站配置默认值
  Object.assign(demoSiteForm, {
    mobileUrl: '',
    webUrl: '',
    adminUrl: '',
    arcoAdminUrl: '',
    giteeUrl: '',
    githubUrl: '',
    docUrl: '',
    description: '',
    enabled: false,
  });

  // 黑名单配置默认值
  Object.assign(blacklistForm, {
    enabled: false,
    list: [],
  });

  // 爬虫配置默认值
  Object.assign(spiderForm, {
    enabled: false,
    interval: 5,
    maxConcurrent: 3,
    timeout: 30,
    userAgent:
      'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    proxyEnabled: false,
    proxyHost: '',
  });
};

// 黑名单相关的数据加载和处理函数已移至BlacklistConfig组件中

// 获取配置数据
const getConfigData = async (showSuccessMessage = true): Promise<void> => {
  try {
    const response = await getAllConfig();

    if (response.code === 200 && response.data) {
      const configData = response.data;

      // 回显网站配置
      if (configData.website) {
        Object.assign(websiteForm, configData.website);
        // 更新组件数据
        if (websiteConfigRef.value) {
          Object.assign(websiteConfigRef.value.form, configData.website);
        }
      }

      // 回显系统配置
      if (configData.system) {
        Object.assign(systemForm, configData.system);
        // 更新组件数据
        if (systemConfigRef.value) {
          Object.assign(systemConfigRef.value.form, configData.system);
        }
      }

      // 回显OSS配置
      if (configData.oss) {
        Object.assign(ossForm, configData.oss);
        if (
          ossForm.qiniuyun &&
          (ossForm.qiniuyun.region === '' ||
            ossForm.qiniuyun.region === undefined ||
            ossForm.qiniuyun.region === null)
        ) {
          ossForm.qiniuyun.region = 'z1';
        }
        // 根据ossType设置当前选中的服务商
        let provider = 'local';
        if (configData.system?.ossType) {
          const ossTypeMap: { [key: string]: string } = {
            '0': 'local',
            '1': 'qiniuyun',
            '2': 'minio',
            '3': 'aliyun',
            '4': 'tencent',
          };
          provider = ossTypeMap[configData.system.ossType] || 'local';
          currentOssProvider.value = provider;
        }
        // 更新组件数据
        if (ossConfigRef.value) {
          Object.assign(ossConfigRef.value.form, configData.oss);
          const q = ossConfigRef.value.form?.qiniuyun;
          if (
            q &&
            (q.region === '' || q.region === undefined || q.region === null)
          ) {
            q.region = 'z1';
          }
          ossConfigRef.value.currentProvider = provider;
        }
      }

      // 回显短信配置
      if (configData.sms) {
        Object.assign(smsForm, configData.sms);
        // 根据smsPrimary设置当前选中的服务商
        let provider = 'aliyun';
        if (configData.system?.smsPrimary) {
          provider = configData.system.smsPrimary;
          currentSmsProvider.value = provider;
        }
        // 更新组件数据
        if (smsConfigRef.value) {
          Object.assign(smsConfigRef.value.form, configData.sms);
          smsConfigRef.value.currentProvider = provider;
        }
      }

      // 回显支付宝配置
      if (configData.alipay) {
        Object.assign(alipayForm, configData.alipay);
        // 更新组件数据
        if (paymentConfigRef.value) {
          Object.assign(paymentConfigRef.value.alipayForm, configData.alipay);
        }
      }

      // 回显微信支付配置
      if (configData.wechatPay) {
        Object.assign(weChatPayForm, configData.wechatPay);
        // 更新组件数据
        if (paymentConfigRef.value) {
          Object.assign(
            paymentConfigRef.value.weChatPayForm,
            configData.wechatPay
          );
        }
      }

      // 回显微信登录配置
      if (configData.wechatLogin) {
        Object.assign(wechatLoginForm, configData.wechatLogin);
        // 更新组件数据
        if (weChatLoginConfigRef.value) {
          Object.assign(
            weChatLoginConfigRef.value.form,
            configData.wechatLogin
          );
        }
      }

      // 回显验证码配置
      if (configData.captcha) {
        Object.assign(captchaForm, configData.captcha);
        // 更新组件数据
        if (captchaConfigRef.value) {
          Object.assign(captchaConfigRef.value.form, configData.captcha);
        }
      }

      // 回显高德地图配置
      if (configData.amap) {
        Object.assign(amapForm, configData.amap);
        // 更新组件数据
        if (amapConfigRef.value) {
          Object.assign(amapConfigRef.value.form, configData.amap);
        }
      }

      // 回显演示账号配置
      if (configData.demoAccount) {
        Object.assign(demoAccountForm, configData.demoAccount);
        // 更新组件数据
        if (demoConfigRef.value) {
          Object.assign(
            demoConfigRef.value.demoAccountForm,
            configData.demoAccount
          );
        }
      }

      // 回显演示站配置
      if (configData.demoSite) {
        Object.assign(demoSiteForm, configData.demoSite);
        // 更新组件数据
        if (demoConfigRef.value) {
          Object.assign(demoConfigRef.value.demoSiteForm, configData.demoSite);
        }
      }

      // 黑名单配置由 BlacklistConfig 组件自行加载，无需父组件回显

      // 回显轮播图配置
      if (configData.carousel && Array.isArray(configData.carousel)) {
        carouselForm.value = configData.carousel;
      }

      if (
        configData.workbenchCarousel &&
        Array.isArray(configData.workbenchCarousel)
      ) {
        workbenchCarouselForm.value = configData.workbenchCarousel;
      }

      if (
        configData.adminLoginPromo &&
        Array.isArray(configData.adminLoginPromo)
      ) {
        adminLoginPromoForm.value = configData.adminLoginPromo;
      }

      // 回显自动审核配置
      if (configData.autoAudit) {
        Object.assign(autoAuditForm, {
          auditMode: configData.autoAudit.auditMode || 'manual',
          autoAuditThreshold: configData.autoAudit.autoAuditThreshold || 0.8,
          enabled: configData.autoAudit.enabled || false,
          accessKey: configData.autoAudit.accessKey || '',
          secretKey: configData.autoAudit.secretKey || '',
          textEndpoint:
            configData.autoAudit.textEndpoint ||
            'https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined',
          imageEndpoint:
            configData.autoAudit.imageEndpoint ||
            'https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined',
          videoEndpoint:
            configData.autoAudit.videoEndpoint ||
            'https://aip.baidubce.com/rest/2.0/solution/v1/video_censor/v2/user_defined',
        });
        // 更新组件数据
        if (autoAuditConfigRef.value) {
          Object.assign(autoAuditConfigRef.value.form, {
            auditMode: configData.autoAudit.auditMode || 'manual',
            autoAuditThreshold: configData.autoAudit.autoAuditThreshold || 0.8,
            enabled: configData.autoAudit.enabled || false,
            accessKey: configData.autoAudit.accessKey || '',
            secretKey: configData.autoAudit.secretKey || '',
            textEndpoint:
              configData.autoAudit.textEndpoint ||
              'https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined',
            imageEndpoint:
              configData.autoAudit.imageEndpoint ||
              'https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined',
            videoEndpoint:
              configData.autoAudit.videoEndpoint ||
              'https://aip.baidubce.com/rest/2.0/solution/v1/video_censor/v2/user_defined',
          });
        }
      }

      // 回显爬虫配置
      if (configData.spider) {
        Object.assign(spiderForm, configData.spider);
        // 更新组件数据
        if (spiderConfigRef.value) {
          Object.assign(spiderConfigRef.value.form, configData.spider);
        }
      }

      if (showSuccessMessage) {
        Message.success(t('biz.configLoadSuccess'));
      }
    } else {
      Message.warning(t('biz.configApiFallback'));
      // 使用默认配置
      initDefaultConfig();
    }
  } catch (error) {
    console.error('获取配置失败:', error);
    Message.warning(t('biz.configApiFallback'));
    // 使用默认配置
    initDefaultConfig();
  }
};

// 配置更新处理
const handleConfigUpdate = (): void => {
  // 配置更新后重新加载所有配置数据，不显示加载成功提示
  getConfigData(false);
};

// 文件上传处理
// 文件上传和表单提交相关的函数已移至各自组件中

// 时间格式化函数已移至各自组件中

// 黑名单相关的处理函数已移至BlacklistConfig组件中

// 系统通知和待实现功能的逻辑已移至对应组件中

// 生命周期
onMounted(() => {
  getConfigData();
});
</script>

<style scoped>
/* 页面整体样式 */
.config-page {
  min-height: 100vh;
  background-color: var(--color-bg-1, #f5f5f5);
  padding: 20px;
}

/* 主卡片样式 */
.config-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background-color: var(--color-bg-2, #ffffff);
}

.form-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border, #e5e6eb);
  background-color: transparent;
}

.form-section h4 {
  margin: 0 0 16px 0;
  color: var(--color-text-1, #1d2129);
  font-size: 16px;
  font-weight: 500;
}

.upload-tip {
  margin-top: 5px;
  color: var(--color-text-3, #999);
  font-size: 12px;
  margin-left: 10px;
}

.form-tip {
  margin-top: 5px;
  color: var(--color-text-3, #999);
  font-size: 12px;
  margin-left: 6px;
}

/* 暗黑模式下的页面样式 */
:global([arco-theme='dark']) .config-page {
  background-color: #1a1a1a !important;
}

/* 暗黑模式下的卡片样式 */
:global([arco-theme='dark']) .config-card {
  background-color: #2a2a2a !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3) !important;
}

:global([arco-theme='dark']) .config-card .arco-card-body {
  background-color: #2a2a2a !important;
}

/* 暗黑模式下的表单区域样式 */
:global([arco-theme='dark']) .form-section {
  border-bottom: 1px solid #404040 !important;
}

/* 暗黑模式下的标题样式 */
:global([arco-theme='dark']) .form-section h4 {
  color: #ffffff !important;
}

/* 暗黑模式下的提示文字样式 */
:global([arco-theme='dark']) .upload-tip {
  color: #8a8a8a !important;
}

/* 暗黑模式下的表单提示样式 */
:global([arco-theme='dark']) .form-tip {
  color: #8a8a8a !important;
}

.reward-container {
  display: flex;
  gap: 20px;
}

.reward-item {
  text-align: center;
}

.reward-title {
  margin-bottom: 2px;
  font-weight: bold;
}

.reward-title.wechat {
  color: #07c160;
}

.reward-title.alipay {
  color: #1677ff;
}

.reward-label {
  margin-top: 2px;
  color: #999;
  font-size: 12px;
}

/* LOGO上传样式 */
.logo-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-uploader:hover {
  border-color: #409eff;
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.logo-image {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

/* 二维码上传样式 */
.qr-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.qr-uploader:hover {
  border-color: #409eff;
}

.qr-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.qr-image {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

/* 打赏二维码上传样式 */
.reward-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.reward-uploader:hover {
  border-color: #409eff;
}

.reward-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.reward-image {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

/* 审核功能配置样式 */
.audit-config-container {
  width: 100%;
}

.audit-config-container .audit-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.audit-config-container .audit-row:last-child {
  margin-bottom: 0;
}

.audit-config-container .audit-item {
  flex: 0 0 300px;
  max-width: 300px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: #fafafa;
  min-width: 0;
}

.audit-config-container .audit-item .audit-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.audit-config-container .audit-item .audit-label .audit-title {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.audit-config-container .audit-item .form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}

/* 服务商选择器样式 */
.provider-selector {
  margin-left: 22px;
  margin-top: 10px;
  margin-bottom: 20px;
}

.provider-selector .arco-radio-group {
  display: flex;
  gap: 8px;
}

.provider-selector .arco-radio {
  margin-right: 0;
}

/* 表单提示样式 */
.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #86909c;
}

/* 演示说明样式 */
.demo-instructions {
  line-height: 1.6;
}

.demo-instructions p {
  margin: 8px 0;
  font-size: 13px;
}

/* 输入框样式优化 - 在白色卡片上使用灰色背景 */
.arco-input {
  background-color: var(--color-fill-2, #f7f8fa) !important;
  border: 1px solid var(--color-border, #e5e6eb) !important;
}

.arco-input:hover {
  border-color: var(--color-border-2, #4e5969) !important;
  background-color: var(--color-bg-2, #ffffff) !important;
}

.arco-input:focus {
  border-color: var(--color-primary-6, #165dff) !important;
  background-color: var(--color-bg-2, #ffffff) !important;
  box-shadow: 0 0 0 2px var(--color-primary-light-1, rgba(22, 93, 255, 0.1)) !important;
}

.arco-textarea {
  background-color: var(--color-fill-2, #f7f8fa) !important;
  border: 1px solid var(--color-border, #e5e6eb) !important;
}

.arco-textarea:hover {
  border-color: var(--color-border-2, #4e5969) !important;
  background-color: var(--color-bg-2, #ffffff) !important;
}

.arco-textarea:focus {
  border-color: var(--color-primary-6, #165dff) !important;
  background-color: var(--color-bg-2, #ffffff) !important;
  box-shadow: 0 0 0 2px var(--color-primary-light-1, rgba(22, 93, 255, 0.1)) !important;
}

.arco-select {
  background-color: #f7f8fa !important;
}

.arco-select .arco-select-view {
  background-color: #f7f8fa !important;
  border: 1px solid #e5e6eb !important;
}

.arco-select .arco-select-view:hover {
  border-color: #4e5969 !important;
  background-color: #ffffff !important;
}

.arco-select .arco-select-view:focus {
  border-color: #165dff !important;
  background-color: #ffffff !important;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1) !important;
}

.arco-input-number {
  background-color: #f7f8fa !important;
}

.arco-input-number .arco-input {
  background-color: #f7f8fa !important;
  border: 1px solid #e5e6eb !important;
}

.arco-input-number .arco-input:hover {
  border-color: #4e5969 !important;
  background-color: #ffffff !important;
}

.arco-input-number .arco-input:focus {
  border-color: #165dff !important;
  background-color: #ffffff !important;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1) !important;
}

.arco-date-picker {
  background-color: #f7f8fa !important;
}

.arco-date-picker .arco-picker {
  background-color: #f7f8fa !important;
  border: 1px solid #e5e6eb !important;
}

.arco-date-picker .arco-picker:hover {
  border-color: #4e5969 !important;
  background-color: #ffffff !important;
}

.arco-date-picker .arco-picker:focus {
  border-color: #165dff !important;
  background-color: #ffffff !important;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1) !important;
}

/* 暗黑模式下的输入框样式 */
:global([arco-theme='dark']) .arco-input {
  background-color: #3a3a3a !important;
  border: 1px solid #555555 !important;
  color: #ffffff !important;
}

:global([arco-theme='dark']) .arco-input:hover {
  border-color: #777777 !important;
  background-color: #4a4a4a !important;
}

:global([arco-theme='dark']) .arco-input:focus {
  border-color: #165dff !important;
  background-color: #4a4a4a !important;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.2) !important;
}

/* 暗黑模式下的文本域样式 */
:global([arco-theme='dark']) .arco-textarea {
  background-color: #3a3a3a !important;
  border: 1px solid #555555 !important;
  color: #ffffff !important;
}

:global([arco-theme='dark']) .arco-textarea:hover {
  border-color: #777777 !important;
  background-color: #4a4a4a !important;
}

:global([arco-theme='dark']) .arco-textarea:focus {
  border-color: #165dff !important;
  background-color: #4a4a4a !important;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.2) !important;
}

/* 暗黑模式下的选择器样式 */
:global([arco-theme='dark']) .arco-select .arco-select-view {
  background-color: #3a3a3a !important;
  border: 1px solid #555555 !important;
  color: #ffffff !important;
}

:global([arco-theme='dark']) .arco-select .arco-select-view:hover {
  border-color: #777777 !important;
  background-color: #4a4a4a !important;
}

:global([arco-theme='dark']) .arco-select .arco-select-view:focus {
  border-color: #165dff !important;
  background-color: #4a4a4a !important;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.2) !important;
}

/* 暗黑模式下的数字输入框样式 */
:global([arco-theme='dark']) .arco-input-number .arco-input {
  background-color: #3a3a3a !important;
  border: 1px solid #555555 !important;
  color: #ffffff !important;
}

:global([arco-theme='dark']) .arco-input-number .arco-input:hover {
  border-color: #777777 !important;
  background-color: #4a4a4a !important;
}

:global([arco-theme='dark']) .arco-input-number .arco-input:focus {
  border-color: #165dff !important;
  background-color: #4a4a4a !important;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.2) !important;
}

/* 暗黑模式下的日期选择器样式 */
:global([arco-theme='dark']) .arco-date-picker .arco-picker {
  background-color: #3a3a3a !important;
  border: 1px solid #555555 !important;
  color: #ffffff !important;
}

:global([arco-theme='dark']) .arco-date-picker .arco-picker:hover {
  border-color: #777777 !important;
  background-color: #4a4a4a !important;
}

:global([arco-theme='dark']) .arco-date-picker .arco-picker:focus {
  border-color: #165dff !important;
  background-color: #4a4a4a !important;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.2) !important;
}

/* 暗黑模式下的 Arco Design 组件样式 */
:global([arco-theme='dark']) .arco-card {
  background-color: #2a2a2a !important;
  border-color: #404040 !important;
}

:global([arco-theme='dark']) .arco-card-body {
  background-color: #2a2a2a !important;
}

:global([arco-theme='dark']) .arco-tabs-content {
  background-color: #2a2a2a !important;
}

:global([arco-theme='dark']) .arco-tabs-tab {
  background-color: #2a2a2a !important;
  color: #ffffff !important;
}

:global([arco-theme='dark']) .arco-tabs-tab-active {
  background-color: #2a2a2a !important;
  color: #165dff !important;
}

:global([arco-theme='dark']) .arco-form-item-label {
  color: #ffffff !important;
}

:global([arco-theme='dark']) .arco-form-item-label-content {
  color: #ffffff !important;
}

/* 系统通知内容样式 */
.notification-content {
  max-width: 300px;
  word-break: break-all;
  line-height: 1.4;
}
</style>