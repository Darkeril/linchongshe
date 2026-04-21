<template>
  <div class="feeds-page">
    <div class="channel-container">
      <div class="scroll-container channel-scroll-container">
        <div class="content-container">
          <div :class="categoryClass == '0' ? 'channel active' : 'channel'" @click="getProductList">
            {{ $t("dashboard.recommend") }}
          </div>
          <div :class="categoryClass == 'video' ? 'channel active' : 'channel'" @click="getVideoProductList">
            {{ $t("dashboard.video") }}
          </div>
          <div
            :class="categoryClass == item.id ? 'channel active' : 'channel'"
            v-for="item in categoryList"
            :key="item.id"
            @click="getProductListByCategory(item.id)"
          >
            {{ $te(`category.${item.title}`) ? $t(`category.${item.title}`) : item.title }}
          </div>
        </div>
      </div>
    </div>
    <div class="loading-container"></div>
    <div class="feeds-container" v-infinite-scroll="loadMoreData" :infinite-scroll-distance="50">
      <div class="feeds-loading-top animate__animated animate__zoomIn animate__delay-0.5s" v-show="topLoading">
        <Loading style="width: 1.2em; height: 1.2em"></Loading>
      </div>
      <!-- 首屏骨架屏：模拟瀑布流卡片布局 -->
      <div v-if="listLoading && productList.length === 0" class="feeds-skeleton-grid">
        <div class="feeds-skeleton-card" v-for="n in 10" :key="'sk-' + n">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" :style="{ width: '100%', height: (140 + (n % 3) * 40) + 'px', borderRadius: '12px' }" />
              <div style="padding: 10px 4px">
                <el-skeleton-item variant="h3" style="width: 90%" />
                <div style="display: flex; align-items: center; justify-content: space-between; margin-top: 8px">
                  <el-skeleton-item variant="text" style="width: 40%; height: 18px" />
                  <el-skeleton-item variant="text" style="width: 30%; height: 14px" />
                </div>
                <div style="display: flex; align-items: center; margin-top: 8px">
                  <el-skeleton-item variant="circle" style="width: 20px; height: 20px; flex-shrink: 0" />
                  <el-skeleton-item variant="text" style="margin-left: 8px; width: 50%" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
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
                  height: item.coverHeight + 'px',
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
                <el-image :src="item.cover" fit="cover" class="product-cover-img" @click="toMain(item.id)"> </el-image>
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
                      <span v-if="item.likeCount <= 0" class="count">{{ $t("common.wantIt") }}</span>
                      <span v-else class="count">{{ $t("common.peopleWant", { n: item.likeCount }) }}</span>
                    </span>
                  </div>
                </div>
                <!-- 视频标识 -->
                <div class="top-tag-area" v-if="item.productType === 2">
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
                <div class="top-tag-area" v-if="item.productType === 3">
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
              </div>
            </template>
          </el-skeleton>
        </template>
      </Waterfall>

      <div class="feeds-loading" v-show="loadingMore && !isEnd">
        <Loading style="width: 1.2em; height: 1.2em"></Loading>
      </div>
      <div class="feeds-end" v-show="isEnd">{{ $t("common.noMore") }}</div>
    </div>
    <FloatingBtn @click-refresh="refresh"></FloatingBtn>
    <IdleMain
      v-show="mainShow"
      :pid="pid"
      :nowTime="new Date()"
      class="animate__animated animate__zoomIn animate__delay-0.5s"
      @click-main="close"
    >
    </IdleMain>
  </div>
</template>

<script lang="ts" setup>
import { Waterfall } from "vue-waterfall-plugin-next";
import "vue-waterfall-plugin-next/dist/style.css";
import { ElMessage } from "element-plus";
import { onMounted, ref, watch, computed } from "vue";
import { useI18n } from "vue-i18n";
import { getProductById } from "@/api/idle";
import { getShopCategoryTreeData } from "@/api/idleCategory";
import { getRecommendProduct, getProduct, getVideoProduct } from "@/api/idle";
import type { Category } from "@/type/category";
import type { EsProductDTO, ProductSearch, ProductInfo } from "@/type/product";
import type { CollectionDTO } from "@/type/collection";
import IdleMain from "@/views/idle-main/main.vue";
import FloatingBtn from "@/components/FloatingBtn.vue";
import { options } from "@/constant/constant";
import { useSearchStore } from "@/store/searchStore";
import Loading from "@/components/Loading.vue";
import { refreshTab } from "@/utils/util";
import { collectionByDTO } from "@/api/collection";
import { useUserStore } from "@/store/userStore";
import convert from "@/utils/convert";
import { useRoute } from "vue-router";

const userStore = useUserStore();
const { t } = useI18n();
const isLogin = ref(false);
const searchStore = useSearchStore();
const topLoading = ref(false);
const listLoading = ref(false);
const loadingMore = ref(false);
const productList = ref<Array<ProductSearch>>([]);
const categoryList = ref<Array<Category>>([]);
const currentPage = ref(1);
const pageSize = 20;
const productTotal = ref(0);
const categoryClass = ref("0");
const mainShow = ref(false);
const pid = ref("");
const isEnd = ref(false);

// 移动端检测
const isMobile = computed(() => window.innerWidth <= 695);
// 移动端关闭周围gutter，减少左右留白
const hasAroundGutter = computed(() => !isMobile.value);

const route = useRoute();

const productDTO = ref<EsProductDTO>({
  keyword: "",
  type: 0,
  cid: "",
  cpid: "",
});
const productInfo = ref<ProductInfo>({
  id: "",
  uid: "",
  title: "",
  description: "",
  type: 0,
  productType: 1,
  image: "",
  cover: "",
  urls: "",
  imgList: [],
  price: 0,
  originalPrice: 0,
  postType: 0,
  province: "",
  city: "",
  district: "",
  address: "",
  status: 0,
  username: "",
  avatar: "",
  time: "",
  likeCount: 0,
  collectionCount: 0,
  commentCount: 0,
  isCollection: false,
  isFollow: false,
  isLike: false,
  pinned: "",
  auditStatus: "",
});

const likeOrComment = ref({
  isCollection: false,
  isComment: false,
});

watch(
  () => [searchStore.seed],
  () => {
    productDTO.value.keyword = searchStore.keyWord;
    productDTO.value.cpid = "";
    categoryClass.value = "0";
    getProductListByKeyword();
  }
);

// 格式化省份显示（去掉省、市、自治区等后缀）
// const formatProvince = (province: string | null | undefined): string => {
//   if (!province) return "";
//   return province
//     .replace(/省$/, "")
//     .replace(/市$/, "")
//     .replace(/自治区$/, "")
//     .replace(/特别行政区$/, "");
// };

const noLoginNotice = () => {
  if (!isLogin.value) {
    ElMessage.warning(t("common.notLoggedIn"));
    return false;
  }
  return true;
};

const collection = (pid: string, type: number, val: number) => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }
  getProductById(pid).then((res: any) => {
    productInfo.value = res.data;
    const collectionDTO = {} as CollectionDTO;
    collectionDTO.collectionId = productInfo.value.id;
    collectionDTO.publishUid = productInfo.value.uid;
    collectionDTO.type = type == 1 ? 1 : 3;
    collectionByDTO(collectionDTO).then(() => {
      productInfo.value.isCollection = val == 1;
      productInfo.value.collectionCount += val;
      likeOrComment.value.isCollection = val == 1;

      // 同步更新 productList 中的对应项
      const productIndex = productList.value.findIndex((product) => product.id === pid);
      if (productIndex !== -1) {
        productList.value[productIndex].likeCount += val;
        productList.value[productIndex].isCollection = val == 1;
      }
    });
  });
};

const toMain = (productId: string) => {
  pid.value = productId;
  mainShow.value = true;
};

const close = () => {
  mainShow.value = false;
};

const handleLoad = (item: any) => {
  item.isLoading = true;
};

const refresh = () => {
  // 使用回调函数优化代码
  refreshTab(() => {
    topLoading.value = true;
    setTimeout(() => {
      currentPage.value = 1;
      productList.value = [];
      isEnd.value = false;
      if (categoryClass.value === "video") {
        getVideoProductList();
      } else {
        getProductList();
      }
      topLoading.value = false;
    }, 200);
  });
};

const loadMoreData = () => {
  if (productList.value.length >= productTotal.value || isEnd.value) {
    isEnd.value = true;
    return;
  }
  if (listLoading.value || loadingMore.value) {
    return;
  }
  currentPage.value += 1;
  loadingMore.value = true;
  const done = () => {
    loadingMore.value = false;
  };
  if (categoryClass.value === "video") {
    getVideoProduct(currentPage.value, pageSize)
      .then((res: any) => {
        setData(res);
      })
      .catch((error) => {
        console.error("load more video products failed:", error);
        currentPage.value -= 1;
      })
      .finally(done);
  } else if (productDTO.value.cpid === "" && productDTO.value.keyword == "") {
    getRecommendProduct(currentPage.value, pageSize)
      .then((res: any) => {
        setData(res);
      })
      .catch((error) => {
        console.error("load more recommended products failed:", error);
        currentPage.value -= 1;
      })
      .finally(done);
  } else {
    getProduct(currentPage.value, pageSize, productDTO.value)
      .then((res) => {
        setData(res);
      })
      .catch((error) => {
        console.error("load more products failed:", error);
        currentPage.value -= 1;
      })
      .finally(done);
  }
};

const setData = (res: any) => {
  const { records, total } = res.data;
  productTotal.value = total;
  if (records && records.length > 0) {
    productList.value.push(...records);
  } else {
    isEnd.value = true;
  }
  if (productList.value.length >= productTotal.value) {
    isEnd.value = true;
  }
};

const getProductList = async () => {
  categoryClass.value = "0";
  productDTO.value.cpid = "";
  productDTO.value.keyword = "";
  productList.value = [] as Array<any>;
  currentPage.value = 1;
  isEnd.value = false;
  listLoading.value = true;
  try {
    const res: any = await getRecommendProduct(currentPage.value, pageSize);
    setData(res);
  } catch (e) {
    console.error(e);
    ElMessage.error(t("common.loadFailed"));
  } finally {
    listLoading.value = false;
  }
};

const getProductListByCategory = async (id: string) => {
  categoryClass.value = id;
  productDTO.value.cpid = id;
  productList.value = [] as Array<any>;
  currentPage.value = 1;
  isEnd.value = false;
  listLoading.value = true;
  try {
    const res = await getProduct(currentPage.value, pageSize, productDTO.value);
    setData(res);
  } catch (e) {
    console.error(e);
    ElMessage.error(t("common.loadFailed"));
  } finally {
    listLoading.value = false;
  }
};

const getVideoProductList = async () => {
  categoryClass.value = "video";
  productDTO.value.cpid = "";
  productDTO.value.keyword = "";
  productList.value = [] as Array<any>;
  currentPage.value = 1;
  isEnd.value = false;
  listLoading.value = true;
  try {
    const res: any = await getVideoProduct(currentPage.value, pageSize);
    setData(res);
  } catch (e) {
    console.error(e);
    ElMessage.error(t("common.loadFailed"));
  } finally {
    listLoading.value = false;
  }
};

const getProductListByKeyword = async () => {
  productList.value = [] as Array<any>;
  currentPage.value = 1;
  isEnd.value = false;
  listLoading.value = true;
  try {
    const res = await getProduct(currentPage.value, pageSize, productDTO.value);
    setData(res);
  } catch (e) {
    console.error(e);
    ElMessage.error(t("common.loadFailed"));
  } finally {
    listLoading.value = false;
  }
};

const initData = async () => {
  isLogin.value = userStore.isLogin();
  categoryClass.value = "0";
  productDTO.value.cpid = "";
  productDTO.value.keyword = "";
  productList.value = [];
  currentPage.value = 1;
  isEnd.value = false;
  listLoading.value = true;
  try {
    const [catRes, prodRes] = await Promise.all([
      getShopCategoryTreeData(),
      getRecommendProduct(1, pageSize),
    ]);
    categoryList.value = (catRes as any).data;
    setData(prodRes as any);
  } catch (e) {
    console.error(e);
    ElMessage.error(t("common.loadFailed"));
  } finally {
    listLoading.value = false;
  }
};

watch(
  () => route.query,
  (query) => {
    if (query.id) {
      // 如果有 id 参数，打开 IdleMain 组件
      pid.value = query.id as string;
      mainShow.value = true;
    }
  },
  { immediate: true } // 立即执行一次
);

onMounted(() => {
  if (route.query.id) {
    pid.value = route.query.id as string;
    mainShow.value = true;
  }
});

initData();
</script>

<style lang="less" scoped>
.feeds-page {
  flex: 1;
  padding: 0 24px;
  padding-top: 72px;
  height: 100vh;

  // 移动端优化
  @media screen and (max-width: 695px) {
    padding: 0;
    padding-top: 64px;
  }

  .channel-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    user-select: none;
    -webkit-user-select: none;

    .channel-scroll-container {
      backdrop-filter: blur(20px);
      background-color: transparent;
      width: calc(100vw - 24px);

      position: relative;
      overflow: hidden;
      display: flex;
      user-select: none;
      -webkit-user-select: none;
      align-items: center;
      font-size: 16px;
      color: rgba(51, 51, 51, 0.8);
      height: 40px;
      white-space: nowrap;
      height: 72px;

      // 移动端优化
      @media screen and (max-width: 695px) {
        width: 100vw;
        height: 48px;
      }

      .content-container::-webkit-scrollbar {
        display: none;
      }

      .content-container {
        display: flex;
        overflow-x: scroll;
        overflow-y: hidden;
        white-space: nowrap;
        color: rgba(51, 51, 51, 0.8);

        .active {
          font-weight: 600;
          background: rgba(0, 0, 0, 0.03);
          border-radius: 999px;
          color: #333;
        }

        .channel {
          height: 40px;
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 0 16px;
          cursor: pointer;
          -webkit-user-select: none;
          user-select: none;
        }
        :hover {
          cursor: pointer; /* 显示小手指针 */
          transform: scale(1.2); /* 鼠标移入时按钮稍微放大 */
        }
      }
    }
  }

  .feeds-container {
    position: relative;
    transition: width 0.5s;
    margin: 0 auto;
    margin-top: -15px;
    margin-left: -15px;

    // 移动端优化
    @media screen and (max-width: 695px) {
      padding: 0;
      margin-top: 10px;
      margin-left: 0px;
    }

    .feeds-loading {
      margin: 3vh;
      text-align: center;
    }

    .feeds-skeleton-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 16px;
      padding: 16px 8px;

      @media screen and (max-width: 695px) {
        grid-template-columns: repeat(2, 1fr);
        gap: 8px;
        padding: 8px 4px;
      }
    }

    .feeds-skeleton-card {
      background: var(--card-bg, #fff);
      border-radius: 20px;
      overflow: hidden;
      padding: 8px;
    }

    .feeds-loading-top {
      text-align: center;
      line-height: 6vh;
      height: 6vh;
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
      border-radius: 20px;
      cursor: pointer;

      :deep(img) {
        width: 100%;
        height: auto;
        max-height: 300px;
        object-fit: cover;
        display: block;

        // 移动端优化
        @media screen and (max-width: 695px) {
          max-height: 260px;
          border-radius: 20px;
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
        max-width: 100% !important;
      }

      .top-tag-area {
        position: absolute;
        right: 12px;
        top: 12px;
        z-index: 4;
      }
    }

    .footer {
      padding: 12px;

      // 移动端优化
      @media screen and (max-width: 695px) {
        padding: 8px 10px;
      }

      .title {
        margin-bottom: 5px;
        word-break: break-all;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        overflow: hidden;
        font-weight: 500;
        font-size: 14px;
        line-height: 140%;
        color: var(--card-text);
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

      .author-wrapper {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 20px;
        color: rgba(51, 51, 51, 0.8);
        font-size: 12px;
        transition: color 1s;
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

        .count {
          margin-left: 2px;
        }
      }
    }
  }
}

/* 深色模式样式 */
::deep(.dark-mode) .feeds-container .card .footer .price-wrapper .current-price {
  color: #ff2442 !important;
}

:deep(.dark-mode) .feeds-container .card .footer .post-type-tag {
  /* 邮寄样式（绿色纯色背景 + 白色文字） */
  &.post-type-mail {
    color: #ffffff !important;
    background: #67c23a !important;
    border: none !important;
  }

  /* 自提样式（橙色纯色背景 + 白色文字） */
  &.post-type-pickup {
    color: #ffffff !important;
    background: #e6a23c !important;
    border: none !important;
  }
}

:deep(.dark-mode) .feeds-container .card .footer .author-wrapper {
  color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.dark-mode) .feeds-container .card .footer .author-wrapper .name {
  color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.dark-mode) .feeds-container .card .footer .author-wrapper span[style*="color: #9e9e9e"] {
  color: #3d8af5 !important;
}

.floating-btn-sets {
  position: fixed;
  display: flex;
  flex-direction: column;
  width: 40px;
  grid-gap: 8px;
  gap: 8px;
  right: 24px;
  bottom: 24px;

  .back-top {
    width: 40px;
    height: 40px;
    background: #fff;
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 100px;
    color: rgba(51, 51, 51, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    // transition: background 0.2s;
    cursor: pointer;
  }

  .reload {
    width: 40px;
    height: 40px;
    background: #fff;
    border: 1px solid rgba(0, 0, 0, 0.08);
    box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.02);
    border-radius: 100px;
    color: rgba(51, 51, 51, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    //transition: background 0.2s;
    cursor: pointer;
  }
}
.feeds-end {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 50px;
  color: #999;
  font-size: 14px;
  margin-top: 20px;
  border-radius: 10px;
}
</style>
