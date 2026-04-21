<template>
  <div class="feeds-container" v-infinite-scroll="loadMoreData" :infinite-scroll-distance="50">
    <Waterfall
      :list="productList"
      :width="options.width"
      :gutter="options.gutter"
      :hasAroundGutter="hasAroundGutter"
      :animation-effect="options.animationEffect"
      :animation-duration="options.animationDuration"
      :animation-delay="options.animationDelay"
      :breakpoints="options.breakpoints"
    >
      <template #item="{ item }">
        <el-skeleton style="width: 240px" :loading="!item.isLoading" animated>
          <template #template>
            <el-image
              :src="item.cover"
              :style="{
                width: '240px',
                maxHeight: '300px',
                height: item.imageHeight ? item.imageHeight + 'px' : 'auto',
                borderRadius: '18px',
              }"
              @load="handleLoad(item)"
            >
            </el-image>

            <div style="padding: 14px">
              <el-skeleton-item variant="h3" style="width: 100%" />
              <div style="display: flex; align-items: center; margin-top: 2px; height: 16px">
                <el-skeleton style="--el-skeleton-circle-size: 20px">
                  <template #template>
                    <el-skeleton-item variant="circle" />
                  </template>
                </el-skeleton>
                <el-skeleton-item variant="text" style="margin-left: 10px" />
              </div>
            </div>
          </template>

          <template #default>
            <div class="card">
              <div class="image-container">
                <el-image :src="item.cover" fit="cover" class="product-cover-img" @click="toMain(item.id)"> </el-image>
                <div v-if="item.auditStatus === '0'" class="overlay passing">
                  <div class="audit-eye">
                    <svg t="1710000000000" class="icon" viewBox="0 0 1024 1024" width="48" height="48">
                      <path
                        d="M512 256c-256 0-416 256-416 256s160 256 416 256 416-256 416-256-160-256-416-256z m0 448c-106.039 0-192-85.961-192-192s85.961-192 192-192 192 85.961 192 192-85.961 192-192 192z m0-320c-70.692 0-128 57.308-128 128s57.308 128 128 128 128-57.308 128-128-57.308-128-128-128z"
                        fill="#fff"
                      />
                    </svg>
                    <div>{{ $t("product.pendingReview") }}...</div>
                  </div>
                </div>
                <div v-if="item.auditStatus === '2'" class="overlay not-passed">
                  {{ $t("product.rejected") }}⚠️
                  <el-button
                    v-if="item.auditStatus === '2' && item.uid === currentUid"
                    type="primary"
                    size="small"
                    class="edit-btn-on-img"
                    @click.stop="editProduct(item.id)"
                    style="margin-left: 8px"
                  >
                    {{ $t("product.reEdit") }}
                  </el-button>
                </div>
              </div>
              <div class="footer">
                <a class="title">
                  <span>{{ item.title }}</span>
                </a>
                <div class="price-wrapper">
                  <div class="price-row">
                    <div class="price-group">
                      <span class="current-price">￥{{ convert.to_price(item.price) }}</span>
                      <span class="original-price">￥{{ convert.to_price(item.originalPrice) }}</span>
                    </div>
                    <span :class="item.postType === 0 ? 'post-type-tag post-type-mail' : 'post-type-tag post-type-pickup'">
                      {{ item.postType === 0 ? $t("common.mail") : $t("common.selfPickup") }}
                    </span>
                  </div>
                  <div class="meta-row" v-if="props.status === 'publish'">
                    <el-tag :type="getType(item.type)" size="small" class="status-tag">
                      {{ getTypeText(item.type) }}
                    </el-tag>
                  </div>
                </div>

                <div class="action-buttons" v-if="props.status === 'sell' || props.status === 'buy'">
                  <el-tag :type="getDealStatus(item.status)" size="small">
                    {{ getDealStatusText(item.status) }}
                  </el-tag>

                  <!-- 买家视角 -->
                  <template v-if="props.status === 'buy'">
                    <!-- 未付款或待付款状态 -->
                    <el-button
                      v-if="item.status === 0 || item.status === 1"
                      type="primary"
                      size="small"
                      @click="toPay(item.paymentOrderId)"
                    >
                      {{ $t("product.toPay") }}
                    </el-button>
                    <!-- 已付款且待收货状态 -->
                    <template v-else-if="item.status === 3">
                      <template v-if="item.dealStatus === '5'">
                        <!-- 邮寄方式 -->
                        <el-button v-if="item.postType === 0" type="primary" size="small" @click="toReceipt(item.id)">
                          {{ $t("product.confirmReceipt") }}
                        </el-button>
                        <!-- 自提方式 -->
                        <el-button v-if="item.postType === 1" type="primary" size="small" @click="toReceipt(item.id)">
                          {{ $t("product.confirmPickupWithCode", { code: item.postSelfCode }) }}
                        </el-button>
                      </template>
                      <!-- 其他状态显示查看订单 -->
                      <template v-else>
                        <el-button type="primary" size="small" @click="viewOrder(item.productOrderNumber)">
                          {{ $t("product.pendingShip") }}
                        </el-button>
                      </template>
                    </template>
                    <!-- 其他状态显示查看订单 -->
                    <template v-else>
                      <el-button type="primary" size="small" @click="viewOrder(item.productOrderNumber)">
                        {{ $t("product.viewOrder") }}
                      </el-button>
                    </template>
                  </template>

                  <!-- 卖家视角 -->
                  <template v-else>
                    <!-- 已付款状态 -->
                    <template v-if="item.status === 3">
                      <template v-if="item.dealStatus === '5'">
                        <!-- 邮寄方式 -->
                        <el-button
                          v-if="item.postType === 0"
                          type="primary"
                          size="small"
                          @click="viewOrder(item.productOrderNumber)"
                        >
                          {{ $t("product.sellerShipped") }}
                        </el-button>
                        <!-- 自提方式 -->
                        <el-button
                          v-if="item.postType === 1"
                          type="primary"
                          size="small"
                          @click="viewOrder(item.productOrderNumber)"
                        >
                          {{ $t("product.pendingPickupWithCode", { code: item.postSelfCode }) }}
                        </el-button>
                      </template>
                      <template v-else>
                        <el-button type="primary" size="small" @click="toShip(item.productOrderNumber, item.postType)">
                          {{ $t("product.toShip") }}
                        </el-button>
                      </template>
                    </template>
                    <!-- 其他状态显示查看订单 -->
                    <template v-else>
                      <el-button type="primary" size="small" @click="viewOrder(item.productOrderNumber)">
                        {{ $t("product.viewOrder") }}
                      </el-button>
                    </template>
                  </template>
                </div>
                <div class="author-wrapper">
                  <a class="author">
                    <img class="author-avatar" :src="item.avatar" />
                    <span class="name">{{ item.username }}</span>
                  </a>
                  <span class="like-wrapper like-active">
                    <span class="like-lottie" v-if="item.isCollection" @click="collection(item.id, 1, -1)">
                      <svg
                        t="1739470033309"
                        class="icon"
                        viewBox="0 0 1024 1024"
                        version="1.1"
                        xmlns="http://www.w3.org/2000/svg"
                        p-id="19111"
                        width="25"
                        height="25"
                      >
                        <path
                          d="M428.6976 107.3152c-6.5024 72.192-36.352 207.2576-160.256 337.408 3.6864-48.0256-7.1168-83.7632-19.0464-107.6736-6.6048-13.1584-26.0608-10.5984-28.8768 3.84-5.7344 29.44-20.5824 75.0592-57.6 137.7792-71.6288 121.3952-62.5664 459.8784 340.736 459.8784s430.4384-352.8192 373.1456-496.0256c-37.376-93.44-93.952-152.5248-128.8192-182.3232-11.4176-9.7792-29.1328-1.9456-29.5936 13.056-0.9216 30.464-7.3216 73.3696-33.0752 102.144-0.6656-52.7872-38.144-208.384-202.4448-296.8576-23.296-12.544-51.7632 2.4576-54.1696 28.7744z"
                          fill="#FF5D50"
                          p-id="19112"
                        ></path>
                        <path
                          d="M702.2592 678.4c-4.1984-45.056-60.672-166.5536-212.6336-246.4256-10.5984-5.5808-23.0912 3.1232-21.504 15.0016 6.2464 46.848 12.9536 140.4928-24.064 184.7296 4.0448-40.3968-18.1248-73.8304-36.6592-94.3104-8.3968-9.216-23.552-4.6592-25.4976 7.68-3.5328 22.3232-12.8512 56.2688-36.5568 97.9456-42.0864 74.0352-86.9888 188.672 124.5696 294.656 10.9568 0.5632 22.1696 0.8704 33.7408 0.8704 11.2128 0 22.0672-0.3072 32.7168-0.8704 158.2592-59.4944 173.4656-177.9712 165.888-259.2768z"
                          fill="#FFDF99"
                          p-id="19113"
                        ></path>
                      </svg>
                    </span>
                    <span class="like-lottie" v-else @click="collection(item.id, 1, 1)">
                      <svg
                        t="1739864213490"
                        class="icon"
                        viewBox="0 0 1024 1024"
                        version="1.1"
                        xmlns="http://www.w3.org/2000/svg"
                        p-id="2122"
                        width="25"
                        height="25"
                      >
                        <path
                          d="M428.6976 107.3152c-6.5024 72.192-36.352 207.2576-160.256 337.408 3.6864-48.0256-7.1168-83.7632-19.0464-107.6736-6.6048-13.1584-26.0608-10.5984-28.8768 3.84-5.7344 29.44-20.5824 75.0592-57.6 137.7792-71.6288 121.3952-62.5664 459.8784 340.736 459.8784s430.4384-352.8192 373.1456-496.0256c-37.376-93.44-93.952-152.5248-128.8192-182.3232-11.4176-9.7792-29.1328-1.9456-29.5936 13.056-0.9216 30.464-7.3216 73.3696-33.0752 102.144-0.6656-52.7872-38.144-208.384-202.4448-296.8576-23.296-12.544-51.7632 2.4576-54.1696 28.7744z"
                          fill="#cdcdcd"
                          p-id="2123"
                          data-spm-anchor-id="a313x.search_index.0.i1.26253a81VGicUs"
                          class="selected"
                        ></path>
                        <path
                          d="M702.2592 678.4c-4.1984-45.056-60.672-166.5536-212.6336-246.4256-10.5984-5.5808-23.0912 3.1232-21.504 15.0016 6.2464 46.848 12.9536 140.4928-24.064 184.7296 4.0448-40.3968-18.1248-73.8304-36.6592-94.3104-8.3968-9.216-23.552-4.6592-25.4976 7.68-3.5328 22.3232-12.8512 56.2688-36.5568 97.9456-42.0864 74.0352-86.9888 188.672 124.5696 294.656 10.9568 0.5632 22.1696 0.8704 33.7408 0.8704 11.2128 0 22.0672-0.3072 32.7168-0.8704 158.2592-59.4944 173.4656-177.9712 165.888-259.2768z"
                          fill="#FFDF99"
                          p-id="2124"
                        ></path>
                      </svg>
                    </span>
                    <span class="count">{{ $t("product.wantCount", { n: item.likeCount }) }}</span>
                  </span>
                </div>
              </div>
              <!-- 视频标识 -->
              <div class="top-tag-sign" v-if="item.productType === 2">
                <svg
                  t="1734053667366"
                  class="icon"
                  viewBox="0 0 1024 1024"
                  version="1.1"
                  xmlns="http://www.w3.org/2000/svg"
                  p-id="5045"
                  width="25"
                  height="25"
                >
                  <path
                    d="M512 0C230.4 0 0 230.4 0 512s230.4 512 512 512 512-230.4 512-512S793.6 0 512 0z m0 981.333333C253.866667 981.333333 42.666667 770.133333 42.666667 512S253.866667 42.666667 512 42.666667s469.333333 211.2 469.333333 469.333333-211.2 469.333333-469.333333 469.333333z"
                    fill="#ffffff"
                    p-id="5046"
                  ></path>
                  <path
                    d="M672 441.6l-170.666667-113.066667c-57.6-38.4-106.666667-12.8-106.666666 57.6v256c0 70.4 46.933333 96 106.666666 57.6l170.666667-113.066666c57.6-42.666667 57.6-106.666667 0-145.066667z"
                    fill="#ffffff"
                    p-id="5047"
                  ></path>
                </svg>
              </div>
              <!-- live图标识 -->
              <div class="top-tag-sign" v-if="item.productType === 3">
                <svg
                  t="1734054678308"
                  class="icon"
                  viewBox="0 0 1024 1024"
                  version="1.1"
                  xmlns="http://www.w3.org/2000/svg"
                  p-id="7819"
                  width="25"
                  height="25"
                >
                  <path
                    d="M526.49882864 364.2931832c-74.8243121 0-135.66761086 60.84329876-135.66761086 135.66761088s60.84329876 135.66761086 135.66761086 135.66761086S662.1664395 574.78510617 662.1664395 499.96079408s-60.97275259-135.66761086-135.66761086-135.66761088z m0 211.78646125c-41.94304 0-76.11885037-34.17581037-76.11885037-76.11885037s34.17581037-76.11885037 76.11885037-76.11885038 76.11885037 34.17581037 76.11885038 76.11885038-34.17581037 76.11885037-76.11885038 76.11885037z"
                    fill="#ffffff"
                    p-id="7820"
                  ></path>
                  <path
                    d="M526.49882864 239.88805531c-143.4348405 0-260.07273876 116.63789827-260.07273877 260.07273877s116.63789827 260.07273876 260.07273877 260.07273875 260.07273876-116.63789827 260.07273877-260.07273875S669.93366914 239.88805531 526.49882864 239.88805531z m0 480.53260642c-121.5571437 0-220.45986765-98.90272395-220.45986766-220.45986765s98.90272395-220.45986765 220.45986766-220.45986766 220.45986765 98.90272395 220.45986766 220.45986766S648.05597235 720.42066173 526.49882864 720.42066173zM526.49882864 183.96400197c13.46319803 0 24.3373195-10.87412148 24.33731951-24.3373195s-10.87412148-24.3373195-24.33731951-24.3373195-24.3373195 10.87412148-24.3373195 24.3373195 10.87412148 24.3373195 24.3373195 24.3373195zM460.73628445 190.82505482c13.07483653-2.84798419 21.48933531-15.66391309 18.77080494-28.86820346-2.84798419-13.07483653-15.66391309-21.48933531-28.86820347-18.77080494-13.07483653 2.84798419-21.48933531 15.66391309-18.77080494 28.86820345s15.79336691 21.61878914 28.86820347 18.77080495zM397.95117827 211.2787595c12.29811358-5.43706075 17.73517431-19.80643555 12.29811358-32.10454913-5.43706075-12.29811358-19.80643555-17.73517431-32.10454913-12.29811357-12.29811358 5.43706075-17.73517431 19.80643555-12.29811358 32.10454912 5.43706075 12.16865975 19.80643555 17.73517431 32.10454913 12.29811358zM340.73258667 244.28948543c10.87412148-7.89668347 13.33374419-23.04278124 5.43706074-33.91690271-7.89668347-10.87412148-23.04278124-13.33374419-33.91690272-5.43706075-10.87412148 7.89668347-13.33374419 23.04278124-5.43706074 33.91690272 7.89668347 10.87412148 23.04278124 13.33374419 33.91690272 5.43706074zM291.66958617 288.4332405c8.93231408-9.96794469 8.15559111-25.37295013-1.81235358-34.3052642-9.96794469-8.93231408-25.37295013-8.15559111-34.3052642 1.81235357-8.93231408 9.96794469-8.15559111 25.37295013 1.81235358 34.30526421 9.96794469 9.0617679 25.37295013 8.28504494 34.3052642-1.81235358zM219.56380445 350.82998518c11.65084445 6.73159902 26.53803457 2.71853037 33.26963358-8.93231407 6.73159902-11.65084445 2.71853037-26.53803457-8.93231408-33.26963358-11.65084445-6.73159902-26.53803457-2.71853037-33.26963358 8.93231408-6.60214518 11.65084445-2.58907653 26.53803457 8.93231408 33.26963357zM195.35593876 417.88706765c12.81592889 4.14252247 26.53803457-2.84798419 30.68055705-15.66391308 4.14252247-12.81592889-2.84798419-26.53803457-15.66391309-30.68055704-12.81592889-4.14252247-26.53803457 2.84798419-30.68055703 15.66391308-4.14252247 12.81592889 2.84798419 26.53803457 15.66391307 30.68055704zM185.5174479 488.56885728c13.33374419 1.4239921 25.37295013-8.28504494 26.6674884-21.61878913 1.4239921-13.33374419-8.28504494-25.37295013-21.61878913-26.6674884-13.33374419-1.4239921-25.37295013 8.28504494-26.66748841 21.61878914-1.4239921 13.20429037 8.28504494 25.2434963 21.61878914 26.66748839zM212.1849363 532.97152c-1.4239921-13.33374419-13.33374419-23.04278124-26.6674884-21.61878914-13.33374419 1.4239921-23.04278124 13.33374419-21.61878914 26.66748839 1.4239921 13.33374419 13.33374419 23.04278124 26.66748841 21.61878914 13.33374419-1.4239921 23.04278124-13.33374419 21.61878913-26.66748839zM226.03649581 597.56897975c-4.14252247-12.81592889-17.86462815-19.80643555-30.68055705-15.66391308-12.81592889 4.14252247-19.80643555 17.86462815-15.66391307 30.68055703 4.14252247 12.81592889 17.86462815 19.80643555 30.68055703 15.6639131 12.81592889-4.14252247 19.80643555-17.86462815 15.66391309-30.68055705zM252.83343803 657.8944632c-6.73159902-11.65084445-21.61878914-15.66391309-33.26963358-8.93231406-11.65084445 6.73159902-15.66391309 21.61878914-8.93231408 33.26963358 6.73159902 11.65084445 21.61878914 15.66391309 33.26963358 8.93231408 11.65084445-6.73159902 15.66391309-21.61878914 8.93231408-33.2696336zM257.36432197 709.54654025c-9.96794469 8.93231408-10.74466765 24.3373195-1.81235358 34.3052642 8.93231408 9.96794469 24.3373195 10.74466765 34.3052642 1.81235358 9.96794469-8.93231408 10.74466765-24.3373195 1.81235358-34.3052642-8.93231408-9.96794469-24.3373195-10.74466765-34.3052642-1.81235358zM306.81568395 760.93970963c-7.89668347 10.87412148-5.43706075 26.02021925 5.43706074 33.91690272 10.87412148 7.89668347 26.02021925 5.43706075 33.91690272-5.43706074 7.89668347-10.87412148 5.43706075-26.02021925-5.43706074-33.91690272-10.87412148-7.89668347-26.02021925-5.43706075-33.91690272 5.43706074zM397.95117827 788.64282864c-12.29811358-5.43706075-26.66748839 0-32.10454913 12.29811358-5.43706075 12.29811358 0 26.66748839 12.29811358 32.10454914 12.29811358 5.43706075 26.66748839 0 32.10454913-12.29811358 5.56651457-12.29811358 0-26.66748839-12.29811358-32.10454914zM460.73628445 808.9670795c-13.07483653-2.84798419-26.02021925 5.56651457-28.86820347 18.77080495s5.56651457 26.02021925 18.77080494 28.86820345c13.07483653 2.84798419 26.02021925-5.56651457 28.86820347-18.77080493 2.84798419-13.07483653-5.56651457-26.02021925-18.77080494-28.86820347zM526.49882864 815.95758617c-13.46319803 0-24.3373195 10.87412148-24.3373195 24.33731952s10.87412148 24.3373195 24.3373195 24.33731949S550.83614815 853.7581037 550.83614815 840.29490569s-10.87412148-24.3373195-24.33731951-24.33731952zM592.13191902 808.9670795c-13.07483653 2.84798419-21.48933531 15.66391309-18.77080494 28.86820347s15.66391309 21.48933531 28.86820345 18.77080493c13.07483653-2.84798419 21.48933531-15.66391309 18.77080494-28.86820345-2.71853037-13.20429037-15.66391309-21.48933531-28.86820345-18.77080495zM655.04647902 788.64282864c-12.29811358 5.43706075-17.73517431 19.80643555-12.29811359 32.10454914 5.43706075 12.29811358 19.80643555 17.73517431 32.10454914 12.29811358 12.29811358-5.43706075 17.73517431-19.80643555 12.29811358-32.10454914-5.56651457-12.29811358-19.93588939-17.86462815-32.10454913-12.29811358zM712.26507061 755.50264889c-10.87412148 7.89668347-13.33374419 23.04278124-5.43706074 33.91690272s23.04278124 13.33374419 33.91690272 5.43706074c10.87412148-7.89668347 13.33374419-23.04278124 5.43706074-33.91690272s-23.04278124-13.33374419-33.91690272-5.43706074zM761.32807111 711.35889383c-8.93231408 9.96794469-8.15559111 25.37295013 1.81235358 34.3052642 9.96794469 8.93231408 25.37295013 8.15559111 34.3052642-1.81235358 8.93231408-9.96794469 8.15559111-25.37295013-1.81235358-34.3052642-9.96794469-8.93231408-25.37295013-8.15559111-34.3052642 1.81235358zM833.30439902 648.96214914c-11.65084445-6.73159902-26.53803457-2.71853037-33.26963359 8.93231406-6.73159902 11.65084445-2.71853037 26.53803457 8.93231407 33.2696336 11.65084445 6.73159902 26.53803457 2.71853037 33.26963359-8.93231408 6.73159902-11.65084445 2.71853037-26.53803457-8.93231407-33.26963358zM857.64171852 581.90506667c-12.81592889-4.14252247-26.53803457 2.84798419-30.68055704 15.66391308-4.14252247 12.81592889 2.84798419 26.53803457 15.66391309 30.68055705 12.81592889 4.14252247 26.53803457-2.84798419 30.68055704-15.6639131 4.14252247-12.81592889-2.84798419-26.53803457-15.66391309-30.68055703zM867.48020939 511.35273086c-13.33374419-1.4239921-25.37295013 8.28504494-26.66748841 21.61878914-1.4239921 13.33374419 8.28504494 25.37295013 21.61878915 26.66748839 13.33374419 1.4239921 25.37295013-8.28504494 26.66748839-21.61878914 1.4239921-13.33374419-8.28504494-25.2434963-21.61878913-26.66748839zM840.68326717 466.95006815c1.4239921 13.33374419 13.33374419 23.04278124 26.66748838 21.61878913s23.04278124-13.33374419 21.61878914-26.66748839c-1.4239921-13.33374419-13.33374419-23.04278124-26.66748839-21.61878914-13.33374419 1.29453827-22.91332741 13.20429037-21.61878913 26.6674884zM826.96116148 402.22315457c4.14252247 12.81592889 17.86462815 19.80643555 30.68055704 15.66391308 12.81592889-4.14252247 19.80643555-17.86462815 15.66391309-30.68055704-4.14252247-12.81592889-17.86462815-19.80643555-30.68055704-15.66391308-12.81592889 4.2719763-19.80643555 17.99408197-15.66391309 30.68055704zM800.16421925 341.89767111c6.73159902 11.65084445 21.61878914 15.66391309 33.26963358 8.93231407 11.65084445-6.73159902 15.66391309-21.61878914 8.93231408-33.26963357-6.73159902-11.65084445-21.61878914-15.66391309-33.26963358-8.93231408-11.65084445 6.86105283-15.66391309 21.61878914-8.93231408 33.26963358zM795.63333531 290.24559408c9.96794469-8.93231408 10.74466765-24.3373195 1.81235358-34.30526421-8.93231408-9.96794469-24.3373195-10.74466765-34.3052642-1.81235357-9.96794469 8.93231408-10.74466765 24.3373195-1.81235358 34.3052642 8.93231408 10.09739852 24.3373195 10.87412148 34.3052642 1.81235358zM746.18197333 238.85242469c7.89668347-10.87412148 5.43706075-26.02021925-5.43706074-33.91690272-10.87412148-7.89668347-26.02021925-5.43706075-33.91690272 5.43706075-7.89668347 10.87412148-5.43706075 26.02021925 5.43706074 33.91690271 10.87412148 7.89668347 26.02021925 5.43706075 33.91690272-5.43706074zM655.04647902 211.2787595c12.29811358 5.43706075 26.66748839 0 32.10454913-12.29811358 5.43706075-12.29811358 0-26.66748839-12.29811358-32.10454912-12.29811358-5.43706075-26.66748839 0-32.10454914 12.29811357-5.56651457 12.29811358 0 26.66748839 12.29811359 32.10454913zM592.13191902 190.82505482c13.07483653 2.84798419 26.02021925-5.56651457 28.86820345-18.77080495 2.84798419-13.07483653-5.56651457-26.02021925-18.77080494-28.86820345s-26.02021925 5.56651457-28.86820345 18.77080494c-2.71853037 13.20429037 5.69596839 26.14967309 18.77080494 28.86820346z"
                    fill="#ffffff"
                    p-id="7821"
                  ></path>
                </svg>
              </div>
              <div class="top-tag-area" v-show="item.pinned === '1'">
                <div class="top-wrapper">{{ $t("common.pinned") }}</div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </template>
    </Waterfall>
  </div>

  <ShopMain
    v-show="mainShow"
    :pid="pid"
    :nowTime="new Date()"
    :editMode="editMode"
    class="animate__animated animate__zoomIn animate__delay-0.5s"
    @click-main="close"
  >
  </ShopMain>

  <el-drawer
    v-model="drawerVisible"
    :title="$t('order.postInfo')"
    direction="rtl"
    size="400px"
    :before-close="handleDrawerClose"
    destroy-on-close
  >
    <PostEdit
      v-if="drawerVisible"
      :product-order-number="productOrderNumber"
      :type="shipmentType"
      @close-drawer="closeDrawer"
      @submit-success="handleSubmitSuccess"
    />
  </el-drawer>
</template>

<script lang="ts" setup>
import { Waterfall } from "vue-waterfall-plugin-next";
import "vue-waterfall-plugin-next/dist/style.css";
import { ref, onMounted, watch, PropType, computed } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
import { getProductByUser } from "@/api/user";
import ShopMain from "@/views/idle-main/main.vue";
import { options } from "@/constant/constant";
import { useRoute } from "vue-router";
import { useUserStore } from "@/store/userStore";
import type { CollectionDTO } from "@/type/collection";
import { getProductById, updateProductStatus } from "@/api/idle";
import { collectionByDTO } from "@/api/collection";
import { ElMessage, ElMessageBox } from "element-plus";
import type { ProductInfo } from "@/type/product";
import convert from "@/utils/convert";
import { useRouter } from "vue-router";
import PostEdit from "@/components/PostEdit.vue";

const router = useRouter();
const isLogin = ref(false);
const route = useRoute();

// 移动端检测
const isMobile = computed(() => window.innerWidth <= 695);
// 移动端关闭周围gutter，避免右侧被裁剪
const hasAroundGutter = computed(() => !isMobile.value);

const drawerVisible = ref(false);
const productOrderNumber = ref("");
const shipmentType = ref<"用户自提" | "物流发货">("物流发货");

type ProductStatus = "publish" | "sell" | "buy" | "collect";

const props = defineProps({
  type: {
    type: Number,
    default: 1,
  },
  isCollect: {
    // 新增 prop
    type: Boolean,
    default: false,
  },
  status: {
    // 新增状态属性
    type: String as PropType<ProductStatus>,
    default: "publish",
  },
});

// 添加计数器的响应式引用
const counts = ref({
  publish: 0,
  sell: 0,
  buy: 0,
  collect: 0,
});

watch(
  () => [props.type, props.isCollect, props.status],
  () => {
    currentPage.value = 1;
    productList.value = [];
    getProductList();
  }
);

const emit = defineEmits(["update-total"]);

const editMode = ref(false);

const productList = ref<Array<any>>([]);
const productTotal = ref(0);
const uid = route.query.uid as string;
const currentPage = ref(1);
const pageSize = 20;
const pid = ref("");
const mainShow = ref(false);
const userStore = useUserStore();
const currentUid = userStore.getUserInfo().id;
const loading = ref(false);

const productInfo = ref<ProductInfo>();

const toPay = (paymentOrderId: string) => {
  try {
    // 使用 router.push 跳转到支付页面，并传递必要的参数
    router.push({
      name: "paymentPay", // 确保这是支付页面在路由配置中的名称
      query: {
        paymentOrderId: paymentOrderId,
        from: "product", // 标记来源，用于支付取消后的返回
      },
    });
  } catch (error) {
    console.error("跳转支付页面失败:", error);
    ElMessage.error(t("product.payNavigateFailed"));
  }
};

// 更新商品信息
const toReceipt = async (productId: string) => {
  try {
    await updateProductStatus(productId);
    // 更新成功后刷新列表
    ElMessage.success(t("product.confirmReceiptSuccess"));
    // 重置当前页面并清空商品列表
    currentPage.value = 1;
    productList.value = [];
    // 重新获取最新的商品列表
    await getProductList();
  } catch (error) {
    console.error("更新商品状态失败:", error);
    ElMessage.error(t("product.confirmReceiptFailed"));
  }
};

const toShip = (orderNumber: string, postType: number) => {
  productOrderNumber.value = orderNumber;
  shipmentType.value = postType === 0 ? "物流发货" : "用户自提";
  drawerVisible.value = true;
};

const handleDrawerClose = (done: () => void) => {
  // 例如弹窗确认
  ElMessageBox.confirm(t("common.confirmClose"))
    .then(() => {
      done();
    })
    .catch(() => {
      // 用户取消，不关闭
    });
};

// 关闭抽屉
const closeDrawer = () => {
  drawerVisible.value = false;
};

const handleSubmitSuccess = async () => {
  closeDrawer();
  ElMessage.success(t("product.shipSuccess"));
  // 重置列表并重新加载
  currentPage.value = 1;
  productList.value = [];
  await getProductList();
};

// 查看订单方法
const viewOrder = async (orderNumber: string) => {
  try {
    // 使用 query 参数进行路由跳转
    router.push({
      name: "orderDetail",
      query: {
        orderNumber: orderNumber,
      },
    });
  } catch (error) {
    console.error("获取订单详情失败:", error);
    ElMessage.error(t("order.getOrderDetailFailed"));
  }
};

// 获取状态标签类型
const getType = (status: number) => {
  switch (status) {
    case 0:
      return "primary"; // 在售
    case 1:
      return "success"; // 已售
    case 2:
      return "success"; // 已售
    case 3:
      return "warning"; // 待上架
    case 4:
      return "info"; // 已上架
    case 5:
      return "info"; // 已下架
    default:
      return "info";
  }
};

// 获取状态文本
const getTypeText = (status: number) => {
  switch (status) {
    case 0:
      return t("idle.onSale");
    case 1:
      return t("idle.sold");
    case 2:
      return t("idle.sold");
    case 3:
      return t("product.pendingListing");
    case 4:
      return t("product.onShelf");
    case 5:
      return t("product.offShelf");
    default:
      return t("order.unknownStatus");
  }
};

// 获取交易状态标签类型
const getDealStatus = (status: number) => {
  switch (status) {
    case 0:
      return "info"; // 未支付
    case 1:
      return "danger"; // 待支付
    case 2:
      return "warning"; // 支付中
    case 3:
      return "success"; // 已支付
    case 4:
      return "success"; // 已支付
    default:
      return "info";
  }
};

// 获取交易状态文本
const getDealStatusText = (status: number) => {
  switch (status) {
    case 0:
      return t("order.unpaid");
    case 1:
      return t("order.pendingPay");
    case 2:
      return t("order.paying");
    case 3:
      return t("order.paidStatus");
    case 4:
      return t("order.transactionSuccess");
    default:
      return t("order.unknownStatus");
  }
};

const handleLoad = (item: any) => {
  item.isLoading = true;
};

const close = () => {
  mainShow.value = false;
  editMode.value = false;
};

const editProduct = (productId: string) => {
  pid.value = productId;
  // 通过 props 传递给 Main 组件，让其自动进入编辑状态
  editMode.value = true;
  mainShow.value = true;
};

const toMain = (productId: string) => {
  // router.push({ name: "main", state: { nid: nid } });
  pid.value = productId;
  editMode.value = false;
  mainShow.value = true;
};

const noLoginNotice = () => {
  if (!isLogin.value) {
    ElMessage.warning(t("common.notLoggedIn"));
    return false;
  }
  return true;
};

const collection = async (pid: string, type: number, val: number) => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }

  try {
    const res = await getProductById(pid);
    if (!res.data) {
      ElMessage.error(t("product.getProductFailed"));
      return;
    }

    productInfo.value = res.data;

    const collectionDTO: CollectionDTO = {
      collectionId: pid, // 直接使用传入的 pid
      publishUid: res.data.uid, // 使用响应数据中的 uid
      type: type === 1 ? 1 : 3,
    };

    await collectionByDTO(collectionDTO);

    // 更新商品列表中的对应项
    const productIndex = productList.value.findIndex((product) => product.id === pid);
    if (productIndex !== -1) {
      productList.value[productIndex].likeCount += val;
      productList.value[productIndex].isCollection = val === 1;
    }
  } catch (error) {
    console.error("收藏操作失败:", error);
    ElMessage.error(t("common.collectFailed"));
  }
};

const setData = (res: any) => {
  const { records, total } = res.data;
  productTotal.value = total;

  // 过滤并去重
  const filteredRecords = records
    .filter((item: any) => {
      return item.uid === currentUid || (item.auditStatus !== "0" && item.auditStatus !== "2");
    })
    .filter((newItem: { id: any }) => !productList.value.some((existingItem) => existingItem.id === newItem.id));

  productList.value.push(...filteredRecords);
  emit("update-total", total);
};

const getProductList = () => {
  // 根据不同状态获取商品列表
  const queryParams = {
    currentPage: currentPage.value,
    pageSize: pageSize,
    uid: uid,
    type: props.isCollect ? 3 : props.type,
    status: props.status, // 添加状态参数
  };

  getProductByUser(queryParams)
    .then((res: any) => {
      setData(res);
      // 根据不同状态更新对应的计数
      if (props.status) {
        counts.value[props.status] = res.data.total;
      }
    })
    .catch((error: any) => {
      console.error("获取商品列表失败:", error);
      ElMessage.error(t("product.listLoadFailed"));
    });
};

const loadMoreData = async () => {
  if (loading.value) return;
  try {
    loading.value = true;
    currentPage.value += 1;
    await getProductList();
  } finally {
    loading.value = false;
  }
};

const initData = () => {
  isLogin.value = userStore.isLogin();
  getProductList();
};

onMounted(() => {
  initData();
});
</script>

<style lang="less" scoped>
.image-container {
  position: relative;
  display: block;
  width: 100%;
}

.audit-eye {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.audit-eye .icon {
  margin-bottom: 8px;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5); /* 半透明背景 */
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  font-size: 20px;
}

.overlay.passing {
  color: white;
}

.overlay.not-passed {
  color: red; /* 设置未通过状态的字体颜色为红色 */
}

.edit-btn-on-img {
  position: absolute;
  top: 10px; // 距离图片顶部10px
  right: 10px; // 距离图片右侧10px
  z-index: 10; // 保证按钮在图片之上
  padding: 2px 10px;
  font-size: 12px;
  opacity: 0.92;
  // background-color: #ff2442 !important;
  // border-color: #ff2442 !important;
  // color: #fff !important;

  // 移动端隐藏
  @media screen and (max-width: 695px) {
    display: none;
  }
}

.feeds-container {
  position: relative;
  transition: width 0.5s;
  margin: 0 auto;

  // 移动端优化
  @media screen and (max-width: 695px) {
    width: 100%;
    padding: 0;
  }

  .noteImg {
    width: 240px;
    max-height: 300px;
    object-fit: cover;
    border-radius: 8px;
  }

  .product-cover-img {
    width: 100%;
    display: block;
    border-radius: 18px 18px 18px 18px;
    cursor: pointer;

    // 移动端优化
    @media screen and (max-width: 695px) {
      border-radius: 18px 18px 18px 18px;
    }

    :deep(img) {
      width: 100%;
      height: auto;
      max-height: 350px;
      object-fit: cover;
      display: block;

      // 移动端优化
      @media screen and (max-width: 695px) {
        max-height: 300px;
      }
    }
  }

  .card {
    position: relative;
    background-color: var(--card-bg);
    border-radius: 20px;
    // box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    width: 100%;

    // 移动端优化
    @media screen and (max-width: 695px) {
      border-radius: 20px;
      // box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
    }

    .top-tag-sign {
      position: absolute;
      right: 12px;
      top: 12px;
      z-index: 4;
    }

    .top-tag-area {
      position: absolute;
      left: 12px;
      top: 12px;
      z-index: 4;

      .top-wrapper {
        background: #ff2442;
        border-radius: 999px;
        font-weight: 500;
        color: #fff;
        line-height: 120%;
        font-size: 12px;
        padding: 5px 8px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .footer {
    padding: 12px;

    // 移动端优化
    @media screen and (max-width: 695px) {
      padding: 8px 10px;
    }

    .title {
      margin-bottom: 8px;
      word-break: break-all;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      overflow: hidden;
      font-weight: 500;
      font-size: 14px;
      line-height: 140%;
      color: #333;

      // 移动端优化
      @media screen and (max-width: 695px) {
        font-size: 13px;
        line-height: 1.4;
        margin-bottom: 6px;
        -webkit-line-clamp: 2;
        line-clamp: 2;
      }
    }

    .price-wrapper {
      margin-bottom: 8px;
      display: flex;
      flex-direction: column;
      gap: 6px;

      // 移动端优化
      @media screen and (max-width: 695px) {
        gap: 4px;
        margin-bottom: 6px;
      }

      .price-row {
        display: flex;
        align-items: baseline;
        justify-content: space-between;
        gap: 8px;

        .price-group {
          display: flex;
          align-items: baseline;
          gap: 8px;
          flex: 1;
          min-width: 0;
        }

        .current-price {
          font-weight: bold;
          font-size: 18px;
          color: #ff2442;
          line-height: 1;
          flex-shrink: 0;

          // 移动端优化
          @media screen and (max-width: 695px) {
            font-size: 16px;
          }
        }

        .original-price {
          font-size: 12px;
          color: #9e9e9e;
          font-weight: normal;
          text-decoration: line-through;
          line-height: 1;
          flex-shrink: 0;

          // 移动端优化
          @media screen and (max-width: 695px) {
            font-size: 11px;
          }
        }

        .post-type-tag {
          flex-shrink: 0;
        }
      }

      .meta-row {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        gap: 8px;
      }
    }

    .post-type-tag {
      display: inline-block;
      border-radius: 12px;
      padding: 2px 8px;
      font-size: 11px;
      line-height: 1.4;
      flex-shrink: 0;

      // 移动端优化
      @media screen and (max-width: 695px) {
        font-size: 10px;
        padding: 1px 6px;
      }

      // 邮寄样式（绿色纯色背景 + 白色文字）
      &.post-type-mail {
        border: none;
        color: #ffffff;
        background: #67c23a;
      }

      // 自提样式（橙色纯色背景 + 白色文字）
      &.post-type-pickup {
        border: none;
        color: #ffffff;
        background: #e6a23c;
      }
    }

    .status-tag {
      flex-shrink: 0;
    }

    .action-buttons {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 8px 0;
      padding: 0 5px;

      .el-button {
        font-size: 12px;
      }

      .el-tag {
        margin-left: 8px;
      }
    }

    .status-online {
      color: #67c23a; /* 绿色 */
    }
    .status-sold {
      color: #f2b65d; /* 灰色 */
    }

    .author-wrapper {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 20px;
      color: rgba(51, 51, 51, 0.8);
      font-size: 12px;
      transition: color 1s;

      // 移动端优化
      @media screen and (max-width: 695px) {
        font-size: 11px;
        height: 18px;
      }

      .author {
        display: flex;
        align-items: center;
        color: inherit;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-right: 12px;

        .author-avatar {
          margin-right: 6px;
          width: 20px;
          height: 20px;
          border-radius: 20px;
          border: 1px solid rgba(0, 0, 0, 0.08);
          flex-shrink: 0;
          object-fit: cover;

          // 移动端优化
          @media screen and (max-width: 695px) {
            width: 18px;
            height: 18px;
            margin-right: 4px;
          }
        }

        .name {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .like-wrapper {
        position: relative;
        cursor: pointer;
        display: flex;
        align-items: center;
        flex-shrink: 0;

        .count {
          margin-left: 2px;
        }
      }
    }
  }
}
</style>
