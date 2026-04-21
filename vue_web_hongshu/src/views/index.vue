<template>
  <div class="container" id="container">
    <div class="top">
      <header class="mask-paper">
        <a style="display: flex">
          <img v-if="logoUrl" class="logo" :src="logoUrl" alt="logo" @error="handleLogoError" />
          <img v-else class="logo" src="@/assets/logo.png" alt="logo" />
        </a>
        <div class="tool-box"></div>
        <div class="input-box" id="sujContainer">
          <input
            type="text"
            v-model="keyword"
            class="search-input"
            :placeholder="$t('nav.search')"
            @input="changeInput"
            @focus="focusInput"
            @keyup.enter="searchPage"
            ref="SearchInput"
          />
          <div class="input-button">
            <div class="close-icon" v-show="showClose" @click="clearInput">
              <Close style="width: 1.2em; height: 1.2em; margin-right: 20px; margin-top: 5px" />
            </div>
            <div class="search-icon" @click="searchPage">
              <a href="#">
                <Search style="width: 1.2em; height: 1.2em; margin-right: 20px; margin-top: 5px" />
              </a>
            </div>
          </div>
          <SearchContainer v-show="showSearch" :recordList="recordList"></SearchContainer>
          <SujContainer v-show="showHistory" :closeHistoryRecord="showHistory"></SujContainer>
        </div>
        <div class="right">
          <!-- 移动端搜索和菜单图标 -->
          <div class="mobile-icons">
            <Search class="mobile-icon" @click="showMobileSearch = true" />
            <div class="hamburger-menu" @click="showMobileMenu = true">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>

          <!-- 桌面端图标 -->
          <div class="desktop-icons">
            <template v-if="demoSiteConfig.enabled">
              <!-- 功能建议 -->
              <!-- <el-tooltip content="功能建议" effect="dark" placement="bottom">
                <Message class="right-icon" @click="router.push('/feedback')" />
              </el-tooltip> -->

              <!-- 移动端 -->
              <el-tooltip :content="$t('demoSite.mobile')" effect="dark" placement="bottom">
                <Iphone class="right-icon" @click="handleClick(demoSiteConfig.mobileUrl)" />
              </el-tooltip>

              <!-- Web端 -->
              <el-tooltip :content="$t('demoSite.web')" effect="dark" placement="bottom">
                <Monitor class="right-icon" @click="handleClick(demoSiteConfig.webUrl)" />
              </el-tooltip>

              <!-- 管理端 -->
              <!-- <el-tooltip :content="$t('demoSite.admin')" effect="dark" placement="bottom">
              <DataBoard class="right-icon" @click="handleClick(demoSiteConfig.adminUrl)" />
            </el-tooltip> -->

              <!-- Arco管理端 -->
              <el-tooltip :content="$t('demoSite.admin')" effect="dark" placement="bottom">
                <DataLine class="right-icon" @click="handleClick(demoSiteConfig.arcoAdminUrl)" />
              </el-tooltip>

              <!-- Gitee仓库 -->
              <el-tooltip :content="$t('demoSite.gitee')" effect="dark" placement="bottom">
                <span class="right-icon" @click="handleClick(demoSiteConfig.giteeUrl)">
                  <svg
                    t="1754937375007"
                    class="icon"
                    viewBox="0 0 1024 1024"
                    version="1.1"
                    xmlns="http://www.w3.org/2000/svg"
                    p-id="18372"
                    width="24"
                    height="24"
                  >
                    <path
                      d="M512 1024C229.222 1024 0 794.778 0 512S229.222 0 512 0s512 229.222 512 512-229.222 512-512 512z m259.149-568.883h-290.74a25.293 25.293 0 0 0-25.292 25.293l-0.026 63.206c0 13.952 11.315 25.293 25.267 25.293h177.024c13.978 0 25.293 11.315 25.293 25.267v12.646a75.853 75.853 0 0 1-75.853 75.853h-240.23a25.293 25.293 0 0 1-25.267-25.293V417.203a75.853 75.853 0 0 1 75.827-75.853h353.946a25.293 25.293 0 0 0 25.267-25.292l0.077-63.207a25.293 25.293 0 0 0-25.268-25.293H417.152a189.62 189.62 0 0 0-189.62 189.645V771.15c0 13.977 11.316 25.293 25.294 25.293h372.94a170.65 170.65 0 0 0 170.65-170.65V480.384a25.293 25.293 0 0 0-25.293-25.267z"
                      fill="#C71D23"
                      p-id="18373"
                    ></path>
                  </svg>
                </span>
              </el-tooltip>

              <!-- GitHub仓库 -->
              <el-tooltip :content="$t('demoSite.github')" effect="dark" placement="bottom">
                <span class="right-icon" @click="handleClick(demoSiteConfig.githubUrl)">
                  <svg
                    t="1746051827779"
                    class="icon"
                    viewBox="0 0 1024 1024"
                    version="1.1"
                    xmlns="http://www.w3.org/2000/svg"
                    p-id="9914"
                    width="25"
                    height="25"
                  >
                    <path
                      d="M960 512c0 97.76-28.704 185.216-85.664 263.264-56.96 78.016-130.496 131.84-220.64 161.856-10.304 1.824-18.368 0.448-22.848-4.032a22.4 22.4 0 0 1-7.2-17.504v-122.88c0-37.632-10.304-65.44-30.464-82.912a409.856 409.856 0 0 0 59.616-10.368 222.752 222.752 0 0 0 54.72-22.816c18.848-10.784 34.528-23.36 47.104-38.592 12.544-15.232 22.848-35.904 30.912-61.44 8.096-25.568 12.128-54.688 12.128-87.904 0-47.072-15.232-86.976-46.208-120.16 14.368-35.456 13.024-74.912-4.48-118.848-10.752-3.616-26.432-1.344-47.072 6.272s-38.56 16.16-53.824 25.568l-21.984 13.888c-36.32-10.304-73.536-15.232-112.096-15.232s-75.776 4.928-112.096 15.232a444.48 444.48 0 0 0-24.672-15.68c-10.336-6.272-26.464-13.888-48.896-22.432-21.952-8.96-39.008-11.232-50.24-8.064-17.024 43.936-18.368 83.424-4.032 118.848-30.496 33.632-46.176 73.536-46.176 120.608 0 33.216 4.032 62.336 12.128 87.456 8.032 25.12 18.368 45.76 30.496 61.44 12.544 15.68 28.224 28.704 47.072 39.04 18.848 10.304 37.216 17.92 54.72 22.816a409.6 409.6 0 0 0 59.648 10.368c-15.712 13.856-25.12 34.048-28.704 60.064a99.744 99.744 0 0 1-26.464 8.512 178.208 178.208 0 0 1-33.184 2.688c-13.024 0-25.568-4.032-38.144-12.544-12.544-8.512-23.296-20.64-32.256-36.32a97.472 97.472 0 0 0-28.256-30.496c-11.232-8.064-21.088-12.576-28.704-13.92l-11.648-1.792c-8.096 0-13.92 0.928-17.056 2.688-3.136 1.792-4.032 4.032-2.688 6.72s3.136 5.408 5.376 8.096 4.928 4.928 7.616 7.168l4.032 2.688c8.544 4.032 17.056 11.232 25.568 21.984 8.544 10.752 14.368 20.64 18.4 29.6l5.824 13.44c4.928 14.816 13.44 26.912 25.568 35.872 12.096 8.992 25.088 14.816 39.008 17.504 13.888 2.688 27.36 4.032 40.352 4.032s23.776-0.448 32.288-2.24l13.472-2.24c0 14.784 0 32.288 0.416 52.032 0 19.744 0.48 30.496 0.48 31.392a22.624 22.624 0 0 1-7.648 17.472c-4.928 4.48-12.992 5.824-23.296 4.032-90.144-30.048-163.68-83.84-220.64-161.888C92.256 697.216 64 609.312 64 512c0-81.152 20.192-156.064 60.096-224.672s94.176-122.88 163.232-163.232C355.936 84.192 430.816 64 512 64s156.064 20.192 224.672 60.096 122.88 94.176 163.232 163.232C939.808 355.488 960 430.848 960 512"
                      fill="#000000"
                      p-id="9915"
                    ></path>
                  </svg>
                </span>
              </el-tooltip>

              <!-- 微信 -->
              <el-tooltip effect="dark" placement="bottom" :open-delay="300">
                <template #content>
                  <div class="wechat-qr-container">
                    <div class="wechat-qr-text">
                      <p class="qr-title">{{ $t("demoSite.getSource") }}</p>
                    </div>
                    <img
                      :src="wechatQrCodeUrl"
                      :alt="$t('demoSite.wechatQrAlt')"
                      class="wechat-qr-code"
                      @error="handleQrCodeError"
                    />
                    <div class="wechat-qr-text">
                      <p class="qr-desc">{{ $t("demoSite.scanWechat") }}</p>
                    </div>
                  </div>
                </template>
                <span class="right-icon" @click="handleClick(demoSiteConfig.docUrl)">
                  <svg
                    t="1754937313086"
                    class="icon"
                    viewBox="0 0 1024 1024"
                    version="1.1"
                    xmlns="http://www.w3.org/2000/svg"
                    p-id="17413"
                    width="24"
                    height="24"
                  >
                    <path
                      d="M337.387283 341.82659c-17.757225 0-35.514451 11.83815-35.514451 29.595375s17.757225 29.595376 35.514451 29.595376 29.595376-11.83815 29.595376-29.595376c0-18.49711-11.83815-29.595376-29.595376-29.595375zM577.849711 513.479769c-11.83815 0-22.936416 12.578035-22.936416 23.6763 0 12.578035 11.83815 23.676301 22.936416 23.676301 17.757225 0 29.595376-11.83815 29.595376-23.676301s-11.83815-23.676301-29.595376-23.6763zM501.641618 401.017341c17.757225 0 29.595376-12.578035 29.595376-29.595376 0-17.757225-11.83815-29.595376-29.595376-29.595375s-35.514451 11.83815-35.51445 29.595375 17.757225 29.595376 35.51445 29.595376zM706.589595 513.479769c-11.83815 0-22.936416 12.578035-22.936416 23.6763 0 12.578035 11.83815 23.676301 22.936416 23.676301 17.757225 0 29.595376-11.83815 29.595376-23.676301s-11.83815-23.676301-29.595376-23.6763z"
                      fill="#28C445"
                      p-id="17414"
                    ></path>
                    <path
                      d="M510.520231 2.959538C228.624277 2.959538 0 231.583815 0 513.479769s228.624277 510.520231 510.520231 510.520231 510.520231-228.624277 510.520231-510.520231-228.624277-510.520231-510.520231-510.520231zM413.595376 644.439306c-29.595376 0-53.271676-5.919075-81.387284-12.578034l-81.387283 41.433526 22.936416-71.768786c-58.450867-41.433526-93.965318-95.445087-93.965317-159.815029 0-113.202312 105.803468-201.988439 233.803468-201.98844 114.682081 0 216.046243 71.028902 236.023121 166.473989-7.398844-0.739884-14.797688-1.479769-22.196532-1.479769-110.982659 1.479769-198.289017 85.086705-198.289017 188.67052 0 17.017341 2.959538 33.294798 7.398844 49.572255-7.398844 0.739884-15.537572 1.479769-22.936416 1.479768z m346.265896 82.867052l17.757225 59.190752-63.630058-35.514451c-22.936416 5.919075-46.612717 11.83815-70.289017 11.83815-111.722543 0-199.768786-76.947977-199.768786-172.393063-0.739884-94.705202 87.306358-171.653179 198.289017-171.65318 105.803468 0 199.028902 77.687861 199.028902 172.393064 0 53.271676-34.774566 100.624277-81.387283 136.138728z"
                      fill="#28C445"
                      p-id="17415"
                    ></path>
                  </svg>
                </span>
              </el-tooltip>
            </template>
          </div>
        </div>
      </header>
    </div>
    <div class="main">
      <div class="side-bar">
        <ul class="channel-list">
          <li :class="activeLink == 0 ? 'active-channel' : ''" @click="toLink(0)">
            <a class="link-wrapper">
              <House class="sidebar-nav-icon" /><span class="channel">{{ $t("nav.discover") }}</span>
            </a>
          </li>
          <li :class="activeLink == 6 ? 'active-channel' : ''" @click="toLink(6)">
            <ShoppingBag class="sidebar-nav-icon sidebar-nav-icon--lg" /><span class="channel">{{
              $t("nav.idle")
            }}</span>
          </li>
          <li :class="activeLink == 1 ? 'active-channel' : ''" @click="toLink(1)">
            <Star class="sidebar-nav-icon" /><span class="channel">{{ $t("nav.trends") }}</span>
          </li>
          <li :class="activeLink == 2 ? 'active-channel' : ''" @click="toLink(2)">
            <Bell class="sidebar-nav-icon" />
            <el-badge :value="messageCount" :max="99" :hidden="!userInfo || messageCount === 0" class="item">
              <span class="channel">{{ $t("nav.message") }}</span>
            </el-badge>
          </li>
          <li :class="activeLink == 3 ? 'active-channel' : ''" @click="toLink(3)">
            <CirclePlus class="sidebar-nav-icon" /><span class="channel">{{ $t("nav.publish") }}</span>
          </li>
          <div v-if="userInfo == null">
            <el-button round @click="login" class="custom-button">{{ $t("login.login") }}</el-button>
          </div>
          <li v-else :class="activeLink == 4 ? 'active-channel' : ''" @click="toLink(4)">
            <el-avatar :src="userInfo.avatar" :size="22" />
            <span class="channel">{{ $t("nav.profile") }}</span>
          </li>
        </ul>

        <div v-if="userInfo == null">
          <div data-v-6432121e="" data-v-7d49aed8="" class="visible floating-box">
            <div data-v-6432121e="" class="title">{{ $t("sidebar.loginNow") }}</div>
            <div data-v-6432121e="" class="line-container">
              <svg data-v-23d27ada="" data-v-6432121e="" class="reds-icon icon" width="16" height="16">
                <use data-v-23d27ada="" xlink:href="#thumbUp"></use>
              </svg>
              <span data-v-6432121e="" class="desc">{{ $t("sidebar.betterContent") }}</span>
            </div>
            <div data-v-6432121e="" class="line-container">
              <svg data-v-23d27ada="" data-v-6432121e="" class="reds-icon icon" width="16" height="16">
                <use data-v-23d27ada="" xlink:href="#convention_b"></use>
              </svg>
              <span data-v-6432121e="" class="desc">{{ $t("sidebar.searchInfo") }}</span>
            </div>
            <div data-v-6432121e="" class="line-container">
              <svg data-v-23d27ada="" data-v-6432121e="" class="reds-icon icon" width="16" height="16">
                <use data-v-23d27ada="" xlink:href="#collect"></use>
              </svg>
              <span data-v-6432121e="" class="desc">{{ $t("sidebar.viewCollections") }}</span>
            </div>
            <div data-v-6432121e="" class="line-container">
              <svg data-v-23d27ada="" data-v-6432121e="" class="reds-icon icon" width="16" height="16">
                <use data-v-23d27ada="" xlink:href="#chat"></use>
              </svg>
              <span data-v-6432121e="" class="desc">{{ $t("sidebar.interactMore") }}</span>
            </div>
          </div>
        </div>

        <!-- 嵌入 SVG 图标定义 -->
        <svg style="display: none">
          <symbol id="thumbUp" viewBox="0 0 24 24">
            <!-- SVG 路径数据 -->
            <path
              d="M1 21h4V9H1v12zM23 10h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L16.17 3 9.59 9.59C9.21 9.95 9 10.45 9 11v8c0 .55.45 1 1 1h6c.38 0 .72-.21.89-.55l3.66-7.33c.08-.14.13-.3.13-.46V11c0-.55-.45-1-1-1zm-2 7h-4v-6h4v6z"
            />
          </symbol>
          <symbol id="convention_b" viewBox="0 0 24 24">
            <!-- SVG 路径数据 -->
            <path
              d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"
            />
          </symbol>
          <symbol id="collect" viewBox="0 0 24 24">
            <!-- SVG 路径数据 -->
            <path
              d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
            />
          </symbol>
          <symbol id="chat" viewBox="0 0 24 24">
            <!-- SVG 路径数据 -->
            <path d="M20 2H4c-1.1 0-2 .9-2 2v14l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 9H6V9h12v2zm0-3H6V6h12v2z" />
          </symbol>
        </svg>

        <div class="information-container" id="informationContainer">
          <div class="information-pad" v-show="padShow">
            <div class="container">
              <div class="group-wrapper">
                <a
                  class="menu-item hover-effect links"
                  target="_blank"
                  href="https://agree.xiaohongshu.com/h5/terms/ZXXY20220331001/-1"
                >
                  <span>{{ $t("settings.about") }}</span>
                  <div class="icon">
                    <ArrowRight style="width: 1em; height: 1em; margin-right: 8px" />
                  </div>
                </a>
                <a
                  class="menu-item hover-effect links"
                  target="_blank"
                  href="https://agree.xiaohongshu.com/h5/terms/ZXXY20220509001/-1"
                >
                  <span>{{ $t("settings.privacy") }}</span>
                  <div class="icon">
                    <ArrowRight style="width: 1em; height: 1em; margin-right: 8px" />
                  </div>
                </a>
                <a
                  v-if="userInfo != null"
                  class="menu-item hover-effect links"
                  href="#"
                  @click.prevent="router.push('/feedback')"
                >
                  <span>{{ $t("settings.help") }}</span>
                  <div class="icon">
                    <ArrowRight style="width: 1em; height: 1em; margin-right: 8px" />
                  </div>
                </a>
                <div class="menu-item hover-effect" v-else>
                  <a href="#" @click="toUpshow = true">
                    <span>{{ $t("settings.help") }}</span>
                  </a>
                </div>
              </div>
              <div>
                <div class="group-wrapper">
                  <div class="group-header">{{ $t("settings.title") }}</div>
                  <!-- 语言切换 -->
                  <div class="menu-item hover-effect">
                    <span>{{ $t("settings.language") }}</span>
                    <div class="multistage-toggle component">
                      <button
                        class="toggle-item"
                        :class="{ active: currentLocale === 'zh-CN' }"
                        @click="switchLanguage('zh-CN')"
                      >
                        <div class="icon-wrapper">
                          <span class="lang-text">{{ $t("settings.langZh") }}</span>
                        </div>
                      </button>
                      <button
                        class="toggle-item"
                        :class="{ active: currentLocale === 'en-US' }"
                        @click="switchLanguage('en-US')"
                      >
                        <div class="icon-wrapper">
                          <span class="lang-text">{{ $t("settings.langEn") }}</span>
                        </div>
                      </button>
                    </div>
                  </div>
                  <div class="menu-item hover-effect">
                    <span>{{ $t("settings.theme") }}</span>
                    <div class="multistage-toggle component">
                      <button
                        class="toggle-item"
                        :class="{ active: themeStore.currentTheme === 'light' }"
                        @click="themeStore.setTheme('light')"
                      >
                        <div class="icon-wrapper">
                          <Sunny style="width: 1em; height: 1em" />
                        </div>
                      </button>
                      <button
                        class="toggle-item"
                        :class="{ active: themeStore.currentTheme === 'dark' }"
                        @click="themeStore.setTheme('dark')"
                      >
                        <div class="icon-wrapper">
                          <Moon style="width: 1em; height: 1em" />
                        </div>
                      </button>
                    </div>
                  </div>
                  <div v-if="userInfo != null">
                    <div class="menu-item hover-effect" @click="logout">
                      <a href="#"
                        ><span>{{ $t("login.logout") }}</span></a
                      >
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="information-wrapper" @click="loadPad">
            <!-- <More style="width: 1em; height: 1em; margin-right: 8px" /> -->
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24">
              <path
                fill="currentColor"
                fill-rule="evenodd"
                d="M19.75 12a.75.75 0 0 0-.75-.75H5a.75.75 0 0 0 0 1.5h14a.75.75 0 0 0 .75-.75m0-5a.75.75 0 0 0-.75-.75H5a.75.75 0 0 0 0 1.5h14a.75.75 0 0 0 .75-.75m0 10a.75.75 0 0 0-.75-.75H5a.75.75 0 0 0 0 1.5h14a.75.75 0 0 0 .75-.75"
                clip-rule="evenodd"
              />
            </svg>
            <span class="channel">{{ $t("common.more") }}</span>
          </div>

          <!-- 备案号显示（桌面端） -->
          <a
            v-if="recordNumber"
            class="record-number"
            href="https://beian.miit.gov.cn/#/Integrated/index"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ recordNumber }}
          </a>
        </div>
      </div>
      <div class="main-content with-side-bar">
        <router-view />
      </div>
    </div>

    <!-- 移动端搜索覆盖层 -->
    <div v-if="showMobileSearch" class="mobile-search-overlay">
      <div class="mobile-search-bar">
        <el-input
          v-model="keyword"
          :placeholder="$t('search.placeholder')"
          class="mobile-search-input"
          clearable
          autofocus
          @keyup.enter="toSearch"
        >
          <template #suffix>
            <Search @click="toSearch" style="cursor: pointer" />
          </template>
        </el-input>
        <span class="cancel-btn" @click="showMobileSearch = false">{{ $t("common.cancel") }}</span>
      </div>
    </div>

    <!-- 移动端下拉菜单 -->
    <transition name="menu-fade">
      <div v-if="showMobileMenu" class="mobile-menu-dropdown" @click.self="showMobileMenu = false">
        <div class="menu-panel">
          <!-- 关于来因 -->
          <div class="menu-item clickable arrow">
            <span class="menu-label">{{ $t("settings.about") }}</span>
            <ArrowRight style="width: 16px; height: 16px; color: #999" />
          </div>

          <!-- 隐私、协议 -->
          <div class="menu-item clickable arrow">
            <span class="menu-label">{{ $t("settings.privacy") }}</span>
            <ArrowRight style="width: 16px; height: 16px; color: #999" />
          </div>

          <!-- 帮助与客服 -->
          <div
            class="menu-item clickable arrow"
            v-if="userInfo"
            @click="
              router.push('/feedback');
              showMobileMenu = false;
            "
          >
            <span class="menu-label">{{ $t("settings.help") }}</span>
            <ArrowRight style="width: 16px; height: 16px; color: #999" />
          </div>
          <div v-else class="menu-section-title">{{ $t("settings.help") }}</div>

          <!-- 设置 -->
          <div class="menu-section-title">{{ $t("settings.title") }}</div>

          <!-- 语言 -->
          <div class="menu-item">
            <span class="menu-label">{{ $t("settings.language") }}</span>
            <div
              class="lang-toggle"
              style="
                display: inline-flex;
                background: #c8c8c8;
                border-radius: 16px;
                padding: 3px;
                border: 1px solid #b0b0b0;
              "
            >
              <button
                :class="['lang-btn', { active: currentLocale === 'zh-CN' }]"
                :style="
                  currentLocale === 'zh-CN'
                    ? { background: '#fff', color: '#111', fontWeight: '700', boxShadow: '0 2px 6px rgba(0,0,0,0.25)' }
                    : { background: 'transparent', color: '#777', fontWeight: '400' }
                "
                @click="switchLanguage('zh-CN')"
              >
                {{ $t("settings.langZh") }}
              </button>
              <button
                :class="['lang-btn', { active: currentLocale === 'en-US' }]"
                :style="
                  currentLocale === 'en-US'
                    ? { background: '#fff', color: '#111', fontWeight: '700', boxShadow: '0 2px 6px rgba(0,0,0,0.25)' }
                    : { background: 'transparent', color: '#777', fontWeight: '400' }
                "
                @click="switchLanguage('en-US')"
              >
                {{ $t("settings.langEn") }}
              </button>
            </div>
          </div>

          <!-- 深色模式 -->
          <div class="menu-item">
            <span class="menu-label">{{ $t("settings.theme") }}</span>
            <div
              class="theme-toggle"
              style="
                display: inline-flex;
                background: #c8c8c8;
                border-radius: 16px;
                padding: 3px;
                border: 1px solid #b0b0b0;
              "
            >
              <button
                :class="['theme-btn', { active: themeStore.currentTheme === 'light' }]"
                :style="
                  themeStore.currentTheme === 'light'
                    ? { background: '#fff', color: '#111', boxShadow: '0 2px 6px rgba(0,0,0,0.25)' }
                    : { background: 'transparent', color: '#777' }
                "
                @click="themeStore.setTheme('light')"
              >
                <Sunny style="width: 1em; height: 1em" />
              </button>
              <button
                :class="['theme-btn', { active: themeStore.currentTheme === 'dark' }]"
                :style="
                  themeStore.currentTheme === 'dark'
                    ? { background: '#fff', color: '#111', boxShadow: '0 2px 6px rgba(0,0,0,0.25)' }
                    : { background: 'transparent', color: '#777' }
                "
                @click="themeStore.setTheme('dark')"
              >
                <Moon style="width: 1em; height: 1em" />
              </button>
            </div>
          </div>

          <!-- 退出登录 -->
          <div class="menu-item clickable" v-if="userInfo" @click="logout">
            <span class="menu-label">{{ $t("user.logout") }}</span>
          </div>
        </div>
      </div>
    </transition>

    <Login v-show="loginShow" @click-child="close"></Login>
    <ToUP v-show="toUpshow" @click-to-up="toUpshow = false"></ToUP>
  </div>

  <!-- 移动端底部导航栏 -->
  <div class="mobile-bottom-nav">
    <div class="nav-items-container">
      <div class="nav-item" :class="{ active: activeLink === 0 }" @click="toLink(0)">
        <House class="nav-icon" />
        <span class="nav-label">{{ $t("nav.discover") }}</span>
      </div>
      <div class="nav-item" :class="{ active: activeLink === 6 }" @click="toLink(6)">
        <ShoppingBag class="nav-icon" />
        <span class="nav-label">{{ $t("nav.idle") }}</span>
      </div>
      <div class="nav-item" :class="{ active: activeLink === 3 }" @click="toLink(3)">
        <CirclePlus class="nav-icon publish-icon" />
        <span class="nav-label">{{ $t("nav.publish") }}</span>
      </div>
      <div class="nav-item" :class="{ active: activeLink === 2 }" @click="toLink(2)">
        <el-badge :value="messageCount" :max="99" :hidden="!userInfo || messageCount === 0" class="nav-badge">
          <Bell class="nav-icon" />
        </el-badge>
        <span class="nav-label">{{ $t("nav.message") }}</span>
      </div>
      <div class="nav-item" :class="{ active: activeLink === 4 }" @click="toLink(4)">
        <el-avatar v-if="userInfo" :src="userInfo.avatar" :size="24" />
        <House v-else class="nav-icon" />
        <span class="nav-label">{{ $t("nav.profile") }}</span>
      </div>
    </div>
    <!-- 备案号显示（移动端） -->
    <a
      v-if="recordNumber"
      class="mobile-record-number"
      href="https://beian.miit.gov.cn/#/Integrated/index"
      target="_blank"
      rel="noopener noreferrer"
    >
      {{ recordNumber }}
    </a>
  </div>
</template>

<script lang="ts" setup>
import {
  Search,
  Sunny,
  Moon,
  Close,
  House,
  Star,
  Bell,
  ArrowRight,
  CirclePlus,
  Iphone,
  Monitor,
  DataLine,
  ShoppingBag,
} from "@element-plus/icons-vue";
import { useRouter, useRoute } from "vue-router";
import Login from "@/views/login.vue";
import { ref, watch, onMounted, computed, watchEffect, onBeforeUnmount } from "vue";
import { useUserStore } from "@/store/userStore";
import { useSearchStore } from "@/store/searchStore";
import { useThemeStore } from "@/store/themeStore";
import SujContainer from "@/components/SujContainer.vue";
import SearchContainer from "@/components/SearchContainer.vue";
import { addRecord, getRecordByKeyWord } from "@/api/search";
import { getRandomString, htmlToPlainText } from "@/utils/util";
import { getChatUserList, getProductChatMessages, getCountMessage } from "@/api/im";
import { useImStore } from "@/store/imStore";
import { loginOut } from "@/api/user";
import { getWebsiteConfig, getDemoSiteConfig } from "@/api/config";
import { wsKey } from "@/constant/constant";
import ToUP from "@/views/to-up/index.vue";
import { MessageType } from "@/type/message";
import { setLocale, getLocale } from "@/locales";
import { ElMessage, ElNotification } from "element-plus";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const searchStore = useSearchStore();
const imStore = useImStore();
const themeStore = useThemeStore();
const loginShow = ref(true);

// /login 重定向到 /dashboard?showLogin=1：在布局内打开登录弹层，避免整页空白
watch(
  () => route.query.showLogin,
  (val) => {
    if (val !== "1" && val !== "true") return;
    const nextQuery = { ...route.query } as Record<string, string | string[] | undefined>;
    delete nextQuery.showLogin;
    router.replace({ path: route.path, query: nextQuery });
    if (!userStore.isLogin()) {
      loginShow.value = true;
    }
  },
  { immediate: true }
);
const userInfo = ref<any>({});
const showHistory = ref(false);
const showSearch = ref(false);
const keyword = ref("");
const showClose = ref(false);
const SearchInput = ref(); // 明确指定类型
const showMobileSearch = ref(false);
const showMobileMenu = ref(false);
const recordList = ref<Array<string>>([]);
const activeLink = ref(1);
const padShow = ref(false);
const ws = ref();
const toUpshow = ref(false);
const isShowDot = ref(false);
const logoUrl = ref<string>("");
const wechatQrCodeUrl = ref<string>("");
const recordNumber = ref<string>("");

const demoSiteConfig = ref({
  enabled: false,
  mobileUrl: "",
  webUrl: "",
  adminUrl: "",
  arcoAdminUrl: "",
  giteeUrl: "",
  githubUrl: "",
  docUrl: "",
});

// 语言相关
const currentLocale = ref(getLocale());

interface CountMessage {
  chatCount: number;
  productChatCount: number;
  likeOrCollectionCount: number;
  commentCount: number;
  followCount: number;
}

const routerList = ["/dashboard", "/noteTrend", "/notice", "/push", "/user", "/search", "/idle"];

const handleQrCodeError = () => {
  // 可用默认二维码或清空以隐藏图片
  wechatQrCodeUrl.value = "";
};

// 获取logo和演示站配置
const fetchConfig = async () => {
  try {
    const webSiteResponse = (await getWebsiteConfig()) as any;
    if (webSiteResponse.code === 200 && webSiteResponse.data) {
      // 获取logo
      if (webSiteResponse.data?.logo) {
        logoUrl.value = webSiteResponse.data.logo;
        wechatQrCodeUrl.value = webSiteResponse.data.wechatQrCode;
      }
      // 获取备案号
      if (webSiteResponse.data?.recordNumber) {
        recordNumber.value = webSiteResponse.data.recordNumber;
      }

      const demoSiteResponse = await getDemoSiteConfig();
      // 获取演示站配置
      if (demoSiteResponse.data) {
        demoSiteConfig.value = {
          enabled: demoSiteResponse.data.enabled || false,
          mobileUrl: demoSiteResponse.data.mobileUrl || "",
          webUrl: demoSiteResponse.data.webUrl || "",
          adminUrl: demoSiteResponse.data.adminUrl || "",
          arcoAdminUrl: demoSiteResponse.data.arcoAdminUrl || "",
          giteeUrl: demoSiteResponse.data.giteeUrl || "",
          githubUrl: demoSiteResponse.data.githubUrl || "",
          docUrl: demoSiteResponse.data.docUrl || "",
        };
      }
    }
  } catch (error) {
    console.error("获取配置失败:", error);
  }
};

// 处理logo加载错误
const handleLogoError = () => {
  logoUrl.value = ""; // 清空URL，使用默认logo
};

// 语言切换方法
const switchLanguage = (locale: string) => {
  if (locale === currentLocale.value) {
    return;
  }

  setLocale(locale);
  currentLocale.value = locale;
  // ElMessage.success(t("settings.langSwitched", { lang: SUPPORT_LOCALES.find((l) => l.value === locale)?.label || locale }));
};

const isInDiv = (dom: string, target: any) => {
  return new Promise((res) => {
    const data = document.getElementById(dom)!.contains(target);
    res(data);
  });
};

const loadPad = () => {
  padShow.value = !padShow.value;
};

const searchPage = async () => {
  try {
    if (keyword.value.length > 0) {
      // 添加搜索记录
      const recordDTO = {
        keyword: keyword.value,
        uid: userInfo.value?.id || "", // 添加可选链
      };

      await addRecord(recordDTO);
      searchStore.pushRecord(keyword.value);
      searchStore.setSeed(getRandomString(12));
      searchStore.setKeyword(keyword.value);

      // 重置显示状态
      showSearch.value = false;
      showHistory.value = false;

      // 路由跳转
      await router.push({
        name: "search",
        query: { keyword: keyword.value },
      });
    }
  } catch (error) {
    console.error("搜索失败:", error);
  }
};

// 移动端搜索
const toSearch = async () => {
  showMobileSearch.value = false;
  await searchPage();
};

// 清除搜索记录（预留功能，暂时注释）
// const clearRecord = () => {
//   recordList.value = [];
//   searchStore.clearAllRecord();
// };

// 点击历史记录搜索（预留功能，暂时注释）
// const searchByHistory = async (item: string) => {
//   keyword.value = item;
//   showMobileSearch.value = false;
//   await searchPage();
// };

watch(
  () => [searchStore.seed, route.query.date],
  (newVal, oldVal) => {
    if (newVal[0] != oldVal[0]) {
      keyword.value = searchStore.keyWord;
      showHistory.value = false;
      showSearch.value = false;
    }
    if (newVal[1] != oldVal[1]) {
      initData();
    }
  },
  {
    deep: true,
  }
);

watch(
  () => [imStore.countMessage],
  (val) => {
    const allCount =
      val[0].chatCount +
      val[0].productChatCount +
      val[0].likeOrCollectionCount +
      val[0].commentCount +
      val[0].followCount;
    if (allCount === 0) {
      isShowDot.value = false;
    }
  },
  {
    deep: true,
  }
);

// 总消息数
const messageCount = computed(() => {
  const chatCount = imStore.countMessage.chatCount || 0; // 笔记未读数
  const productCount = imStore.countMessage.productChatCount || 0; // 闲宝未读数
  const otherCount =
    (imStore.countMessage.likeOrCollectionCount || 0) +
    (imStore.countMessage.commentCount || 0) +
    (imStore.countMessage.followCount || 0);
  // 总未读数 = 笔记 + 闲宝 + 其他消息
  return chatCount + productCount + otherCount;
});

watchEffect(() => {
  const url = window.location.href;
  const _keyword = "keyword";
  if (url.indexOf("?") != -1 && url.indexOf(_keyword) != -1) {
    const val = url.substring(url.lastIndexOf(_keyword) + _keyword.length + 1, url.length);
    keyword.value = decodeURI(val);
  }
});

// 监听路由变化，检测移动端访问发布页面
watch(
  () => route.path,
  (newPath) => {
    const isMobile = window.innerWidth <= 695;
    if (isMobile && newPath === "/push") {
      ElMessage.warning(t("common.switchToPC"));
      router.push({ path: "/dashboard" });
    }
  },
  { immediate: true }
);

const handleClick = (url: string | URL | undefined) => {
  window.open(url, "_blank"); // 打开外链到新标签页
};

const changeInput = async (e: any) => {
  try {
    const { value } = e.target;
    keyword.value = value;

    // 简化显示逻辑
    showClose.value = value !== "";
    showSearch.value = value.length > 0;
    showHistory.value = value.length === 0;

    if (value.length > 0) {
      const res = await getRecordByKeyWord(value);
      if (res && res.data) {
        recordList.value = res.data;
      } else {
        recordList.value = [];
      }
    } else {
      recordList.value = [];
    }
  } catch (error) {
    console.error("搜索出错:", error);
    recordList.value = [];
  }
};

const focusInput = () => {
  try {
    showClose.value = keyword.value !== "";
    showSearch.value = keyword.value.length > 0;
    showHistory.value = keyword.value.length === 0;
    SearchInput.value?.focus();
  } catch (error) {
    console.error("聚焦输入框失败:", error);
  }
};

const clearInput = () => {
  keyword.value = "";
  showClose.value = false;
  showHistory.value = true;
  showSearch.value = false;
  SearchInput.value.focus();
};

const resetMessageCount = (type: number) => {
  const newCountMessage = { ...imStore.countMessage };
  // 确保只重置对应类型的消息计数
  switch (type) {
    case MessageType.CHAT:
      newCountMessage.chatCount = 0;
      break;
    case MessageType.PRODUCT:
      newCountMessage.productChatCount = 0;
      break;
    case MessageType.COMMENT:
      newCountMessage.commentCount = 0;
      break;
    case MessageType.LIKE:
      newCountMessage.likeOrCollectionCount = 0;
      break;
    case MessageType.FOLLOW:
      newCountMessage.followCount = 0;
      break;
  }
  console.log("重置消息计数:", type, newCountMessage);
  imStore.setCountMessage(newCountMessage);
};

const toLink = (num: number) => {
  // 移动端发布按钮拦截
  if (num === 3 && window.innerWidth <= 695) {
    ElMessage.warning(t("common.switchToPC"));
    return;
  }

  if (num === 2) {
    // 点击通知时
    resetMessageCount(num);
  }
  // 如果点击闲宝，不改变 activeLink
  if (num !== 6) {
    activeLink.value = num;
  }
  const url = routerList[num];
  if (url === "/user") {
    router.push({ name: "user", query: { uid: userInfo.value.id } });
    return;
  }
  router.push({ path: url });
};

const close = (val: boolean) => {
  loginShow.value = val;
  userInfo.value = userStore.getUserInfo();
};

const maxRetries = 5; // 最大重试次数
let retryCount = 0; // 当前重试次数
let heartbeatTimer: ReturnType<typeof setInterval> | null = null; // 心跳定时器
let reconnectTimer: ReturnType<typeof setTimeout> | null = null; // 重连定时器
const HEARTBEAT_INTERVAL = 30000; // 心跳间隔：30秒
const HEARTBEAT_TIMEOUT = 60000; // 心跳超时：60秒（如果60秒内没有收到任何消息，认为连接断开）
let lastMessageTime = Date.now(); // 最后一次收到消息的时间

// 启动心跳保活
const startHeartbeat = () => {
  // 清除旧的心跳定时器
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
  }

  heartbeatTimer = setInterval(() => {
    if (ws.value && ws.value.readyState === WebSocket.OPEN) {
      // 检查是否超时（60秒内没有收到任何消息）
      const now = Date.now();
      if (now - lastMessageTime > HEARTBEAT_TIMEOUT) {
        console.warn("⚠️ WebSocket 心跳超时，连接可能已断开，尝试重连");
        // 关闭当前连接并重连
        if (ws.value) {
          ws.value.close();
        }
        return;
      }

      // 发送心跳消息（使用 msgType: 0 作为心跳，与后端保持一致）
      try {
        ws.value.send(
          JSON.stringify({
            sendUid: userInfo.value?.id,
            msgType: 0, // 心跳消息类型
            content: "ping",
          })
        );
        console.log("💓 发送心跳消息");
      } catch (error) {
        console.error("发送心跳消息失败:", error);
      }
    } else {
      console.warn("⚠️ WebSocket 未连接，无法发送心跳");
    }
  }, HEARTBEAT_INTERVAL);
};

// 停止心跳保活
const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
};

const connectWs = (uid: string) => {
  // 清除重连定时器
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }

  // 如果已有连接，先关闭
  if (ws.value) {
    stopHeartbeat();
    ws.value.close();
  }

  ws.value = new WebSocket(wsKey + uid);

  ws.value.onopen = () => {
    console.log("✅ WebSocket 连接成功");
    retryCount = 0; // 重置重试计数
    lastMessageTime = Date.now(); // 重置最后消息时间
    startHeartbeat(); // 启动心跳保活
  };

  ws.value.onclose = (event: CloseEvent) => {
    console.log("❌ WebSocket 连接断开", {
      code: event.code,
      reason: event.reason,
      wasClean: event.wasClean,
    });
    stopHeartbeat(); // 停止心跳

    // 如果不是正常关闭，尝试重连
    if (userInfo.value != null && userInfo.value != undefined) {
      if (retryCount < maxRetries) {
        retryCount++;
        const retryDelay = Math.min(1000 * retryCount, 5000); // 延迟时间（1秒，2秒，3秒，最多5秒）
        console.log(`🔄 尝试重新连接，第 ${retryCount} 次重试，将在 ${retryDelay} 毫秒后重试`);
        reconnectTimer = setTimeout(() => {
          connectWs(userInfo.value.id);
        }, retryDelay);
      } else {
        console.log("❌ 已达到最大重试次数，停止重连");
      }
    }
  };

  ws.value.onerror = (error: Event) => {
    console.error("❌ WebSocket 错误:", error);
  };

  ws.value.onmessage = (e: any) => {
    try {
      const message = JSON.parse(e.data);
      lastMessageTime = Date.now(); // 更新最后消息时间
      console.log("📨 收到消息", message);

      if (message.msgType === 0) {
        // 心跳响应或消息计数更新
        if (message.content && typeof message.content === "object") {
          // 更新消息计数时保持各类型消息的独立性
          const currentCounts = { ...imStore.countMessage };
          const newCounts = message.content;

          // 确保每种类型的消息计数都是独立的
          const updatedCounts = {
            // chatCount: newCounts.chatCount ?? currentCounts.chatCount,
            // productChatCount: newCounts.productChatCount ?? currentCounts.productChatCount,
            likeOrCollectionCount: newCounts.likeOrCollectionCount ?? currentCounts.likeOrCollectionCount,
            commentCount: newCounts.commentCount ?? currentCounts.commentCount,
            followCount: newCounts.followCount ?? currentCounts.followCount,
          };
          imStore.setCountMessage(updatedCounts);
        } else {
          // 心跳响应，忽略
          console.log("💓 收到心跳响应");
        }
      } else if (message.msgType === 1) {
        imStore.setMessage(message);
        // 不再刷新整个列表，由 note-message.vue 的 watch 直接更新对应项
      } else if (message.msgType === 99 && message.sendUid === "system") {
        imStore.setMessage(message);
        const text =
          typeof message.content === "string"
            ? htmlToPlainText(message.content)
            : "";
        const img = message.imageUrl;
        const tip = text || (img ? t("common.image") : "");
        if (tip) {
          ElNotification({
            title: t("notice.title"),
            message: tip.length > 120 ? `${tip.slice(0, 120)}...` : tip,
            duration: 8000,
            type: "warning",
            customClass: "im-system-notification",
            offset: 64,
          });
        }
      } else if (message.msgType === 5) {
        imStore.setUserList(message.content);
        // 更新 noteChatList（消息列表页使用）
        imStore.setNoteChatList(message.content);
      }
    } catch (error) {
      console.error("解析 WebSocket 消息失败:", error);
    }
  };
};

const getChatUserListMethod = (): Promise<number> => {
  return new Promise((resolve) => {
    getChatUserList().then((res: any) => {
      const data = res.data;
      const countMessage = imStore.countMessage;
      let chatCount = 0;
      data.forEach((item: any) => {
        chatCount += item.count;
      });
      countMessage.chatCount = chatCount;
      imStore.setCountMessage(countMessage);
      imStore.setUserList(data);
      imStore.setNoteChatList(data); // 同时更新 noteChatList，这样 note-message.vue 的 watchEffect 会自动更新
      resolve(chatCount);
    });
  });
};

const getProductChatListMethod = (): Promise<number> => {
  return new Promise((resolve) => {
    getProductChatMessages().then((res: any) => {
      const data = res.data;
      const countMessage = imStore.countMessage;
      let chatCount = 0;
      data.forEach((item: any) => {
        chatCount += item.count;
      });
      countMessage.chatCount = chatCount;
      imStore.setCountMessage(countMessage);
      imStore.setUserList(data);
      resolve(chatCount);
    });
  });
};

// 在获取消息计数的方法中添加默认值
const getCountMessageMethod = (): Promise<CountMessage> => {
  return new Promise((resolve) => {
    getCountMessage().then((res: any) => {
      const data = res.data as CountMessage;
      // 确保所有计数字段都有默认值0
      const safeData = {
        chatCount: data.chatCount || 0,
        productChatCount: data.productChatCount || 0,
        likeOrCollectionCount: data.likeOrCollectionCount || 0,
        commentCount: data.commentCount || 0,
        followCount: data.followCount || 0,
      };
      imStore.setCountMessage(safeData);
      resolve(safeData);
    });
  });
};

const login = () => {
  loginShow.value = true;
};

const logout = () => {
  // 检查用户ID是否存在
  if (!userInfo.value || !userInfo.value.id) {
    console.warn("用户信息不存在，直接清除本地状态并跳转登录页");
    // 清除所有本地状态
    userStore.loginOut();
    userInfo.value = null;
    // 重置消息数
    imStore.setCountMessage({
      chatCount: 0,
      productChatCount: 0,
      likeOrCollectionCount: 0,
      commentCount: 0,
      followCount: 0,
    });
    // 显示登录框
    loginShow.value = true;
    return;
  }

  // 有用户ID时，调用后端退出接口
  loginOut(userInfo.value.id).then(() => {
    userStore.loginOut();
    userInfo.value = null;
    // 新增：重置消息数
    imStore.setCountMessage({
      chatCount: 0,
      productChatCount: 0,
      likeOrCollectionCount: 0,
      commentCount: 0,
      followCount: 0,
    });
    loginShow.value = true;
    activeLink.value = 0;
    // 清理定时器并关闭 WebSocket
    stopHeartbeat();
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
    if (ws.value) {
      ws.value.close();
    }
    router.push({ path: "/" });
  });
};

const getWsMessage = async () => {
  if (userInfo.value === null || userInfo.value === undefined) {
    return;
  }
  loginShow.value = false;
  connectWs(userInfo.value.id);

  try {
    const [chatUserList, chatProductList, countMessageData] = await Promise.all([
      getChatUserListMethod() as Promise<number>,
      getProductChatListMethod() as Promise<number>,
      getCountMessageMethod() as Promise<CountMessage>,
    ]);

    // 确保每个计数都是独立的
    const newCountMessage: CountMessage = {
      chatCount: chatUserList || 0,
      productChatCount: chatProductList || 0,
      likeOrCollectionCount: countMessageData.likeOrCollectionCount || 0,
      commentCount: countMessageData.commentCount || 0,
      followCount: countMessageData.followCount || 0,
    };

    console.log("更新消息计数:", newCountMessage);
    imStore.setCountMessage(newCountMessage);
  } catch (error) {
    console.error("获取消息数据失败:", error);
  }
};

const getPath = () => {
  const url = window.location.href;
  let path = "";
  if (url.indexOf("?") != -1) {
    const index = url.indexOf("?");
    path = url.substring(url.lastIndexOf("/"), index);
  } else {
    path = url.substring(url.lastIndexOf("/"), url.length);
  }
  return path;
};

const initData = () => {
  userInfo.value = userStore.getUserInfo();
  const path = getPath();
  // 如果当前路径是 /idleTrend，保持 activeLink 为1（动态）
  if (path !== "/idleTrend") {
    activeLink.value = path === "/search" ? 0 : routerList.findIndex((item) => item === path);
  }
  getWsMessage();
};

// 监听外部点击
onMounted(() => {
  initData();

  fetchConfig();

  const handleOutsideClick = (e: MouseEvent) => {
    const target = e.target as HTMLElement;

    Promise.all([isInDiv("sujContainer", target), isInDiv("informationContainer", target)]).then(
      ([isSujContainer, isInfoContainer]) => {
        if (!isSujContainer) {
          showHistory.value = false;
          showSearch.value = false;
        }
        if (!isInfoContainer) {
          padShow.value = false;
        }
      }
    );
  };

  // 监听窗口大小变化，检测发布页面在移动端的情况
  const handleResize = () => {
    const isMobile = window.innerWidth <= 695;
    const currentPath = route.path;

    if (isMobile && currentPath === "/push") {
      ElMessage.warning(t("common.switchToPC"));
      router.push({ path: "/dashboard" });
    }
  };

  document.getElementById("container")?.addEventListener("click", handleOutsideClick);
  window.addEventListener("resize", handleResize);

  // 清理事件监听
  onBeforeUnmount(() => {
    document.getElementById("container")?.removeEventListener("click", handleOutsideClick);
    window.removeEventListener("resize", handleResize);
    // 清理 WebSocket 相关定时器
    stopHeartbeat();
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
    // 关闭 WebSocket 连接
    if (ws.value) {
      ws.value.close();
    }
  });
});
</script>

<style lang="less" scoped>
// 主题变量
@primary-color: #3d8af5;
@text-primary: rgba(51, 51, 51, 0.8);
@text-secondary: rgba(51, 51, 51, 0.6);
@border-color: rgba(0, 0, 0, 0.08);
@white: #fff;

a {
  text-decoration: none;
  color: var(--text-secondary);
}

// 移动端图标
.mobile-icons {
  display: none;
  align-items: center;
  gap: 16px;

  @media screen and (max-width: 695px) {
    display: flex;
  }

  .mobile-icon {
    width: 22px;
    height: 22px;
    cursor: pointer;
    color: var(--text-color);

    &:hover {
      color: @primary-color;
    }
  }

  .hamburger-menu {
    width: 18px;
    height: 14px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    cursor: pointer;

    span {
      display: block;
      width: 100%;
      height: 2px;
      background-color: var(--text-color);
      border-radius: 1px;
      transition: all 0.3s;
    }

    &:active {
      span {
        background-color: @primary-color;
      }
    }
  }
}

// 桌面端图标
.desktop-icons {
  display: flex;
  align-items: center;

  @media screen and (max-width: 695px) {
    display: none;
  }
}

.right-icon {
  width: 1.5em;
  height: 1.5em;
  margin-top: 10px;
  cursor: pointer;
}

.right-icon:not(:last-child) {
  margin-right: 25px;
}
.right-icon:last-child {
  margin-right: 50px;
}

.right-icon:hover {
  transform: scale(1.2); /* 鼠标移入时放大 1.2 倍 */
}

/* 添加选中标签的效果 */
.channel-list li:hover {
  border-radius: 20px;
  background-color: var(--hover-bg);
  cursor: pointer;
  width: 90%;
}

/* 激活的选中标签样式 */
.active-channel {
  background-color: var(--hover-bg);
}

/* 备案号样式（桌面端） */
.record-number {
  text-align: left;
  font-size: 12px;
  color: var(--text-muted);
  padding: 0 0 18px;
  opacity: 0.7;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  // margin-left: 10px;
  width: 100%;
  padding-left: 24px; /* 与"更多"文本对齐：svg图标32px + channel margin-left 12px */
  box-sizing: border-box;
  margin-top: -15px;
  text-decoration: none;
  display: block;
  cursor: pointer;
  transition: opacity 0.2s;

  &:hover {
    opacity: 1;
    text-decoration: underline;
  }
}

.custom-button {
  margin-top: 2px;
  margin-bottom: 15px;
  margin-left: -2px;
  height: 48px;
  background: @primary-color;
  color: #fff;
  opacity: 1;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  width: 90%;
}

/* 浮动框容器 */
.floating-box {
  background-color: var(--sidebar-bg);
  border-radius: 10px;
  padding: 20px; /* 调整内边距 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
  max-width: 300px; /* 调整宽度 */
  margin: 0 auto; /* 使浮动框居中 */
  margin-left: -2px; /* 向左移动 2px */
  cursor: pointer;
  width: 90%;
}

/* 标题样式 */
.title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 15px;
  color: var(--text-color);
}

/* 行容器样式 */
.line-container {
  margin-bottom: 6px;
  display: flex;
  align-items: center;
}

/* 图标样式 */
.reds-icon {
  fill: @primary-color; /* 图标颜色 */
  margin-right: 10px;
}

/* 描述文本样式 */
.desc {
  font-size: 13px;
  color: var(--text-muted);
}

.container {
  max-width: 1728px;
  background-color: var(--bg-color);
  margin: 0 auto;

  .top {
    display: flex;
    flex-direction: column;
    justify-content: center;
    width: 100vw;
    height: 72px;
    position: fixed;
    left: 0;
    top: 0;
    z-index: 10;
    align-items: center;
    background: var(--header-bg);

    @media screen and (max-width: 695px) {
      height: 64px;
    }

    header {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      max-width: 1728px;
      height: 120px;
      padding: 0 16px 0 24px;
      z-index: 10;

      @media screen and (max-width: 695px) {
        height: 64px;
        padding: 0 12px;
        padding-top: 8px;
      }

      .logo {
        margin-top: 30px;
        margin-left: 20px;
        width: 90px;
        height: 50px;
        object-fit: contain;
        user-select: none;

        @media screen and (max-width: 695px) {
          width: 70px;
          height: 40px;
          margin-left: 0;
          margin-top: 0;
        }
      }

      .tool-box {
        width: 24px;
        height: 70px;
        position: absolute;
        left: 0;
        top: 0;
      }

      .input-box {
        height: 40px;
        position: fixed;
        left: 50%;
        transform: translate(-50%);

        @media screen and (max-width: 695px) {
          display: none;
        }

        @media screen and (min-width: 960px) and (max-width: 1191px) {
          width: calc(-36px + 50vw);
        }

        @media screen and (min-width: 1192px) and (max-width: 1423px) {
          width: calc(-33.6px + 40vw);
        }

        @media screen and (min-width: 1424px) and (max-width: 1727px) {
          width: calc(-42.66667px + 33.33333vw);
        }

        @media screen and (min-width: 1728px) {
          width: 533.33333px;
        }

        .search-input {
          padding: 0 84px 0 16px;
          width: 100%;
          height: 100%;
          font-size: 16px;
          line-height: 120%;
          color: var(--text-color);
          caret-color: @primary-color;
          background: var(--nav-hover);
          border-radius: 999px;
        }

        .input-button {
          position: absolute;
          right: 0;
          top: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          height: 100%;
          color: var(--text-secondary);

          .close-icon .search-icon {
            width: 40px;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            color: var(--text-secondary);
          }
          :hover {
            cursor: pointer; /* 显示小手指针 */
            transform: scale(1.15); /* 鼠标移入时按钮稍微放大 */
          }
        }
      }
    }
  }

  .main {
    display: flex;

    .side-bar {
      @media screen and (max-width: 695px) {
        display: none;
      }

      @media screen and (min-width: 696px) and (max-width: 959px) {
        display: none;
      }

      @media screen and (min-width: 960px) and (max-width: 1191px) {
        width: calc(-18px + 25vw);
        margin-left: 12px;
      }

      @media screen and (min-width: 1192px) and (max-width: 1423px) {
        width: calc(-16.8px + 20vw);
        margin-left: 12px;
      }

      @media screen and (min-width: 1424px) and (max-width: 1727px) {
        width: calc(-21.33333px + 16.66667vw);
        margin-left: 16px;
      }

      @media screen and (min-width: 1728px) {
        width: 266.66667px;
        margin-left: 16px;
      }

      height: calc(100vh - 72px);
      overflow-y: scroll;
      background-color: var(--bg-color);
      display: flex;
      flex-direction: column;
      flex-shrink: 0;
      padding-top: 16px;
      margin-top: 72px;
      position: fixed;
      overflow: visible;

      .channel-list {
        min-height: auto;
        -webkit-user-select: none;
        user-select: none;

        .active-channel {
          background-color: var(--nav-hover);
          border-radius: 999px;
          color: var(--nav-active);
          width: 90%;
        }

        li {
          padding-left: 16px;
          min-height: 48px;
          display: flex;
          align-items: center;
          cursor: pointer;
          margin-bottom: 8px;
          color: var(--text-muted);

          .link-wrapper {
            display: flex;
            width: 100%;
            height: 48px;
            align-items: center;
          }

          .message-count {
            margin-left: 7rem;
            background-color: @primary-color;
            width: 20px;
            height: 20px;
            font-size: 14px;
            line-height: 20px;
            text-align: center;
            border-radius: 50%;
            color: #fff;
          }
        }

        .channel {
          font-size: 16px;
          font-weight: 600;
          margin-left: 12px;
          color: var(--text-color);
        }

        /* Element Plus 图标为 fill 路径：叠同色描边 + 略放大，视觉上更接近「加粗」 */
        .sidebar-nav-icon {
          width: 1.2em;
          height: 1.2em;
          margin-right: 8px;
          flex-shrink: 0;
          vertical-align: middle;
          transform: scale(1.06);
          transform-origin: center center;

          :deep(svg) {
            display: block;
            overflow: visible;
          }

          :deep(path),
          :deep(circle),
          :deep(line),
          :deep(rect),
          :deep(ellipse),
          :deep(polyline) {
            paint-order: stroke fill;
            stroke: currentColor;
            stroke-width: 22px;
            stroke-linejoin: round;
            stroke-linecap: round;
          }
        }

        .sidebar-nav-icon--lg {
          width: 1.38em;
          height: 1.38em;
        }
      }

      .information-container {
        display: inline-block;
        width: 100%;
        color: var(--text-color);
        font-size: 16px;
        position: absolute;
        bottom: 0;
        margin-left: 10px;

        .information-pad {
          z-index: 16;
          margin-bottom: 4px;
          width: 90%;

          .container {
            width: 100%;
            background: var(--menu-bg);
            box-shadow: 0 4px 32px 0 var(--shadow), 0 1px 4px 0 var(--shadow);
            border-radius: 12px;

            .divider {
              margin: 0px 12px;
              list-style: none;
              height: 0;
              border: 1px solid var(--border-light);
              border-width: 1px 0 0;
            }

            .group-wrapper {
              padding: 4px;

              .group-header {
                display: flex;
                align-items: center;
                padding: 0 12px;
                font-weight: 400;
                height: 32px;
                color: var(--text-muted);
                font-size: 12px;
              }

              .menu-item {
                height: 40px;
                color: var(--text-secondary);
                font-size: 16px;
                border-radius: 8px;
                display: flex;
                align-items: center;
                padding: 0 12px;
                font-weight: 400;

                .icon {
                  color: var(--text-muted);
                  margin-left: auto;
                }

                .component {
                  margin-left: auto;
                }

                /* 分段开关：轨道与菜单底色需区分；选中态勿用 --card-bg（浅色下与白菜单同色会「看不见」） */
                .multistage-toggle {
                  position: relative;
                  display: flex;
                  align-items: center;
                  padding: 3px;
                  border-radius: 999px;
                  cursor: pointer;
                  background: #dbd9d9;
                  // border: 1px solid #a8a8a8;
                  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.08);

                  .toggle-item {
                    flex: 1;
                    min-width: 26px;
                    border: none;
                    margin: 0;
                    padding: 0;
                    border-radius: 999px;
                    background: transparent;
                    color: #666;
                    cursor: pointer;
                    transition: background 0.2s, color 0.2s, box-shadow 0.2s;

                    .icon-wrapper {
                      width: 24px;
                      height: 24px;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      cursor: pointer;
                    }

                    &.active {
                      background: #ffffff;
                      color: #111;
                      font-weight: 600;
                      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.22);
                    }
                  }
                }
              }
              /* 添加选中标签的效果 */
              .menu-item:hover {
                border-radius: 20px;
                background-color: var(--hover-bg);
                cursor: pointer;
                width: 100%;
              }

              // 语言切换按钮文字样式
              .lang-text {
                font-size: 14px;
                font-weight: 500;
              }

              html.dark-mode & {
                .multistage-toggle {
                  background: #3a3a3a;
                  border-color: #555;
                  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.35);

                  .toggle-item {
                    color: rgba(255, 255, 255, 0.45);

                    &.active {
                      background: #f0f0f0;
                      color: #111;
                      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
                    }
                  }
                }
              }
            }
          }
        }

        .information-wrapper {
          -webkit-user-select: none;
          user-select: none;
          cursor: pointer;
          position: relative;
          margin-bottom: 20px;
          height: 48px;
          width: 100%;
          display: flex;
          font-weight: 600;
          align-items: center;
          border-radius: 999px;
        }

        /* 添加选中标签的效果 */
        .information-wrapper:hover {
          border-radius: 20px;
          background-color: var(--hover-bg);
          cursor: pointer;
          width: 90%;
        }
      }
    }

    .main-content {
      width: 100%;

      @media screen and (max-width: 695px) {
        padding-bottom: 70px; // 为底部导航栏留出空间
      }
    }

    .main-content {
      @media screen and (min-width: 960px) and (max-width: 1191px) {
        padding-left: calc(-6px + 25vw);
      }

      @media screen and (min-width: 1192px) and (max-width: 1423px) {
        padding-left: calc(-4.8px + 20vw);
      }

      @media screen and (min-width: 1424px) and (max-width: 1727px) {
        padding-left: calc(-5.33333px + 16.66667vw);
      }

      @media screen and (min-width: 1728px) {
        padding-left: 282.66667px;
      }
    }
  }
}

// 移动端底部导航栏
.mobile-bottom-nav {
  display: none;

  @media screen and (max-width: 695px) {
    display: flex !important;
    flex-direction: column !important;
    position: fixed !important;
    left: 0 !important;
    right: 0 !important;
    bottom: 0 !important;
    width: 100% !important;
    background: var(--bg-color) !important;
    border-top: 1px solid var(--border-color) !important;
    z-index: 9999 !important;
    padding: 0 !important;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05) !important;
    box-sizing: border-box !important;
  }

  /* 导航项容器 */
  .nav-items-container {
    display: flex;
    flex-direction: row;
    align-items: center;
    width: 100%;
    height: 60px;

    @media screen and (max-width: 695px) {
      display: flex !important;
    }
  }

  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex: 1;
    cursor: pointer;
    position: relative;
    padding: 8px 0;
    min-width: 0;

    @media screen and (max-width: 695px) {
      flex: 1 1 20% !important;
    }
  }

  /* 移动端备案号样式 */
  .mobile-record-number {
    width: 100%;
    text-align: center;
    font-size: 11px;
    color: var(--text-muted);
    padding: 4px 12px 8px;
    opacity: 0.7;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    border-top: 1px solid var(--border-color);
    background: var(--bg-color);
    text-decoration: none;
    display: block;
    cursor: pointer;
    transition: opacity 0.2s;

    &:hover {
      opacity: 1;
      text-decoration: underline;
    }

    &:active {
      opacity: 0.9;
    }
  }

  .nav-icon {
    width: 24px;
    height: 24px;
    color: var(--text-secondary);
    transition: all 0.3s;
    flex-shrink: 0;
    transform: scale(1.06);
    transform-origin: center center;

    :deep(svg) {
      display: block;
      overflow: visible;
    }

    :deep(path),
    :deep(circle),
    :deep(line),
    :deep(rect),
    :deep(ellipse),
    :deep(polyline) {
      paint-order: stroke fill;
      stroke: currentColor;
      stroke-width: 22px;
      stroke-linejoin: round;
      stroke-linecap: round;
    }

    &.publish-icon {
      width: 28px;
      height: 28px;
    }
  }

  .nav-label {
    font-size: 11px;
    color: var(--text-secondary);
    margin-top: 4px;
    transition: all 0.3s;
    white-space: nowrap;
  }

  .nav-badge {
    :deep(.el-badge__content) {
      transform: translateY(-50%) translateX(50%);
    }
  }

  .nav-item.active {
    .nav-icon {
      color: @primary-color;
    }

    .nav-label {
      color: @primary-color;
      font-weight: 600;
    }
  }

  .nav-item:active {
    transform: scale(0.95);
  }
}

// 移动端下拉菜单
.mobile-menu-dropdown {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  background: rgba(0, 0, 0, 0.3);

  .menu-panel {
    position: absolute;
    top: 64px;
    right: 12px;
    width: 280px;
    max-height: calc(100vh - 80px);
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    overflow-y: auto;
    padding: 8px 0;

    .menu-section-title {
      padding: 12px 20px 8px;
      font-size: 13px;
      color: #999;
      font-weight: 400;
    }

    .menu-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 14px 20px;
      cursor: default;
      border-bottom: 0.5px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      &.clickable {
        cursor: pointer;

        &:active {
          background: #f8f8f8;
        }
      }

      &.arrow {
        cursor: pointer;

        &:active {
          background: #f8f8f8;
        }
      }

      .menu-label {
        font-size: 15px;
        color: #333;
      }

      .theme-toggle,
      .lang-toggle {
        display: inline-flex;
        background: #c8c8c8;
        border-radius: 16px;
        padding: 3px;
        border: 1px solid #b0b0b0;

        button {
          min-width: 25px;
          height: 25px;
          border: none;
          background: transparent;
          border-radius: 13px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          transition: background 0.2s, color 0.2s, box-shadow 0.2s;
          color: #666;
          font-size: 13px;
          font-weight: 500;

          &.active {
            background: #ffffff;
            color: #111111;
            font-weight: 700;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
          }
        }
      }
    }
  }
}

.menu-fade-enter-active,
.menu-fade-leave-active {
  transition: opacity 0.3s;

  .menu-panel {
    transition: transform 0.3s, opacity 0.3s;
  }
}

.menu-fade-enter-from,
.menu-fade-leave-to {
  opacity: 0;

  .menu-panel {
    transform: translateY(-10px);
    opacity: 0;
  }
}

// 移动端搜索覆盖层
.mobile-search-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;

  .mobile-search-bar {
    display: flex;
    align-items: center;
    padding: 16px 16px;
    gap: 12px;
    background: var(--bg-color);

    .mobile-search-input {
      flex: 1;

      :deep(.el-input__wrapper) {
        background-color: #f5f5f5;
        box-shadow: none;
        border-radius: 18px;
        padding: 4px 16px;
        height: 36px;
      }

      :deep(.el-input__inner) {
        color: var(--text-color);
        font-size: 14px;

        &::placeholder {
          color: #999;
        }
      }

      :deep(.el-input__suffix) {
        .el-icon {
          color: #666;
          font-size: 20px;
        }
      }
    }

    .cancel-btn {
      color: #333;
      font-size: 16px;
      cursor: pointer;
      white-space: nowrap;

      &:active {
        opacity: 0.6;
      }
    }
  }
}

.wechat-qr-container {
  padding: 12px;
  background: white;
  border-radius: 8px;
  text-align: center;

  .wechat-qr-code {
    width: 140px;
    height: 160px;
    display: block;
    border-radius: 4px;
    margin: 0 auto 8px;
  }

  .wechat-qr-text {
    .qr-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-color);
      margin: 0 0 4px 0;
    }

    .qr-desc {
      font-size: 12px;
      color: var(--text-muted);
      margin: 0;
      line-height: 1.4;
    }
   }
}
</style>

<style lang="less">
/* ElNotification 挂载在 body 上，须使用非 scoped 样式 */
.im-system-notification.el-notification {
  border-left: 4px solid #ffc107;
  background: linear-gradient(135deg, #fffdf5 0%, #ffffff 100%);
}
.im-system-notification.el-notification .el-notification__icon {
  color: #f59e0b;
}
</style>
