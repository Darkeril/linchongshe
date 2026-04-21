<template>
  <view>
    <u-toast ref="uToast"></u-toast>
    <u-popup
      :show="showName"
      mode="right"
      customStyle="width:750rpx;background-color: #f5f5f5;z-index: 9999;"
    >
      <view v-if="updateOption === 1">
        <u-navbar title="编辑名字" :autoBack="false" :placeholder="false" :safeAreaInsetTop="false" @leftClick="showName = false">
          <text slot="left">取消</text>
          <view
            @click="saveName"
            slot="right"
            :style="{ color: nickname == '' ? '#3d8af5' : '#3d8af5' }"
          >
            保存
          </view>
        </u-navbar>
        <view style="height: 30rpx"></view>
        <text style="margin-left: 60rpx; font-size: 32rpx">修改一个心仪的名字吧</text>
        <view style="padding: 30rpx">
          <u--textarea
            v-model="nickname"
            style="border-radius: 30rpx; padding: 30rpx"
            placeholder="添加一个名字"
            count
            border="none"
            maxlength="12"
            height="1.5em"
          ></u--textarea>
        </view>
        <text style="margin-left: 60rpx; font-size: 28rpx; color: #9ea1a3">
          请设置2-12个字符哦，尽量不要包含特殊字符
        </text>
      </view>
      <view v-else-if="updateOption === 3">
        <u-navbar title="编辑简介" :autoBack="false" :placeholder="false" :safeAreaInsetTop="false" @leftClick="showName = false">
          <text slot="left">取消</text>
          <view
            @click="saveIntroduction"
            slot="right"
            :style="{ color: introduction == '' ? '#3d8af5' : '#3d8af5' }"
          >
            保存
          </view>
        </u-navbar>
        <view style="height: 30rpx"></view>
        <text style="margin-left: 60rpx; font-size: 32rpx">写一个自己喜欢的简介吧</text>
        <view style="padding: 30rpx">
          <u--textarea
            v-model="introduction"
            style="border-radius: 30rpx; padding: 30rpx"
            placeholder="可以说明自己的喜好,特点"
            count
            border="none"
            :maxlength="100"
            height="120"
          ></u--textarea>
        </view>
      </view>
      <view v-else-if="updateOption === 7">
        <u-navbar title="编辑标签" :autoBack="false" :placeholder="false" :safeAreaInsetTop="false" @leftClick="showName = false">
          <text slot="left">取消</text>
          <view
            @click="saveTags"
            slot="right"
            :style="{ color: tags == '' ? '#3d8af5' : '#3d8af5' }"
          >
            保存
          </view>
        </u-navbar>
        <view style="height: 30rpx"></view>
        <text style="margin-left: 60rpx; font-size: 32rpx">添加个性标签吧</text>
        <view style="padding: 30rpx">
          <u--textarea
            v-model="tags"
            style="border-radius: 30rpx; padding: 30rpx"
            placeholder="用逗号分隔多个标签，如：美食,旅行,摄影"
            count
            border="none"
            :maxlength="50"
            height="120"
          ></u--textarea>
        </view>
        <text style="margin-left: 60rpx; font-size: 28rpx; color: #9ea1a3">
          多个标签用逗号分隔，最多50个字符
        </text>
      </view>
      <view v-else-if="updateOption === 4">
        <u-navbar title="编辑性别" :autoBack="false" :placeholder="false" :safeAreaInsetTop="false" @leftClick="showName = false">
          <text slot="left">取消</text>
        </u-navbar>
        <view style="height: 30rpx"></view>
        <text style="margin-left: 60rpx; font-size: 30rpx; color: #9ea1a3">请选择你的性别</text>
        <view
          style="margin: 30rpx; background-color: #ffffff; padding: 30rpx; border-radius: 35rpx"
        >
          <u-radio-group
            v-model="sex"
            placement="column"
            iconPlacement="right"
            @change="groupChange"
          >
            <u-radio activeColor="red" label="男" :name="1"></u-radio>
            <u-divider :hairline="true"></u-divider>
            <u-radio activeColor="red" label="女" :name="0"></u-radio>
          </u-radio-group>
        </view>
      </view>
      <view v-else-if="updateOption === 5">
        <u-navbar title="编辑生日" :autoBack="false" :placeholder="false" :safeAreaInsetTop="false" @leftClick="showName = false">
          <text slot="left">取消</text>
        </u-navbar>
        <view style="height: 30rpx"></view>
        <text style="margin-left: 60rpx; font-size: 30rpx; color: #9ea1a3">请选择你的生日</text>
        <u-cell
          style="background-color: #ffffff; margin: 30rpx; border-radius: 30rpx"
          :border="false"
          title="生日信息"
          :isLink="true"
          :value="userInfo.birthday"
          @click="showDatePicker = true"
          size="large"
        ></u-cell>
        <u-datetime-picker
          :show="showDatePicker"
          v-model="currentBirthdayTs"
          mode="date"
          :minDate="new Date('1900-01-01').getTime()"
          :maxDate="new Date().getTime()"
          @confirm="confirmDate"
          @cancel="showDatePicker = false"
          @close="showDatePicker = false"
        ></u-datetime-picker>
      </view>
      <view v-else-if="updateOption === 6">
        <u-navbar title="编辑地区" :autoBack="false" :placeholder="false" :safeAreaInsetTop="false" @leftClick="showName = false">
          <text slot="left">取消</text>
        </u-navbar>
        <view style="height: 30rpx"></view>
        <text style="margin-left: 60rpx; font-size: 30rpx; color: #9ea1a3">请选择你的地区</text>
        <u-cell
          style="background-color: #ffffff; margin: 30rpx; border-radius: 30rpx"
          :border="false"
          title="地区"
          :isLink="true"
          :value="userInfo.area"
          @click="addressShow = true"
          size="large"
        ></u-cell>
        <address-picker
          :show="addressShow"
          closeOnClickOverlay
          @confirm="confirmAddress"
          @cancel="addressShow = false"
          @close="addressShow = false"
          :address-data="addressData"
          :indexs="indexs"
          :areaId="areaId"
          :type="type"
        ></address-picker>
      </view>
    </u-popup>
    <!-- 微信端使用系统栏标题「编辑资料」，不重复展示导航栏 -->
    <!-- #ifndef MP-WEIXIN -->
    <u-navbar title="编辑资料" :autoBack="true" placeholder></u-navbar>
    <!-- #endif -->
    <view style="padding: 30rpx; background-color: #ffffff" class="up">
      <view style="justify-content: center; margin-top: 30rpx; margin-bottom: 30rpx; display: flex">
        <view style="width: 200rpx; height: 200rpx; position: relative" @click="chooseAvatar">
          <image
            style="width: 200rpx; height: 200rpx; border-radius: 50%"
            :src="userInfo.avatarUrl"
            mode="aspectFill"
          ></image>
          <view
            style="
              text-align: center;
              background-color: #000000;
              border-radius: 50%;
              width: 50rpx;
              height: 50rpx;
              padding: 5rpx;
              position: absolute;
              bottom: 5rpx;
              right: 5rpx;
            "
          >
            <image style="width: 40rpx" src="@/static/image/camera.png" mode="widthFix"></image>
          </view>
        </view>
      </view>
      <u-cell
        title="名字"
        :isLink="true"
        :value="userInfo.nickname"
        :name="1"
        @click="openPopup"
      ></u-cell>
      <u-cell
        title="爱宠社号"
        :isLink="true"
        :value="userInfo.hsId"
        :name="2"
        @click="openPopup"
      ></u-cell>
      <u-cell
        title="简介"
        :isLink="true"
        :value="userInfo.selfIntroduction || '有趣的简介可以吸引粉丝'"
        :name="3"
        @click="openPopup"
      ></u-cell>
      <u-cell
        title="标签"
        :isLink="true"
        :value="formatTags(userInfo.tags) || '添加标签'"
        :name="7"
        @click="openPopup"
      ></u-cell>
      <u-cell
        title="性别"
        :isLink="true"
        :value="userInfo.sex === 0 ? '女' : userInfo.sex === 1 ? '男' : ''"
        :name="4"
        @click="openPopup"
      ></u-cell>
      <u-cell
        title="生日"
        :isLink="true"
        :value="userInfo.birthday"
        :name="5"
        @click="openPopup"
      ></u-cell>
      <u-cell
        title="职业"
        :isLink="true"
        :value="userInfo.occupation"
        @click="chooseOccupation"
      ></u-cell>
      <u-cell
        title="地区"
        :isLink="true"
        :value="userInfo.area"
        :name="6"
        @click="openPopup"
      ></u-cell>
      <u-cell title="背景图" :isLink="true" @click="chooseBackgroundImage">
        <view slot="value">
          <image style="height: 22px" :src="userInfo.homePageBackground" mode="heightFix"></image>
        </view>
      </u-cell>
    </view>
    <view
      style="padding: 30rpx; background-color: #ffffff; margin-top: 20rpx; padding-bottom: 10rpx"
      class="up"
    >
      <view style="display: flex">
        更多信息
        <view style="color: #9ea1a3; font-size: 23rpx; align-self: flex-end">（仅自己可见）</view>
      </view>
      <u-cell title="二维码" :isLink="true" :border="false">
        <u-icon slot="value" name="grid" size="28"></u-icon>
      </u-cell>
    </view>
  </view>
</template>

<script>
import { baseUrl } from '@/.env.js';
import { getUserInfo } from '@/apis/user_service.js';
import {
  updateBackgroundImage,
  updateAvatarUrl,
  updateNickname,
  updateIntroduction,
  updateTags,
  updateSex,
  updateArea,
  updateBirthday
} from '@/apis/user_service.js';

// 字段映射函数
function mapUserInfo(data) {
  return {
    avatarUrl: data.avatar,
    nickname: data.username,
    id: parseInt(data.id, 10) || 0,
    uid: data.uid || '',
    hsId: data.hsId,
    ipAddr: data.address,
    selfIntroduction: data.description,
    tags: data.tags || '',
    age: data.age || null,
    sex: data.gender !== undefined ? parseInt(data.gender, 10) : null,
    attentionNum: parseInt(data.followerCount) || 0,
    fansNum: parseInt(data.fanCount) || 0,
    homePageBackground: data.userCover,
    area: data.address,
    birthday: data.birthday || '',
    occupation: data.occupation || ''
  };
}
export default {
  data() {
    return {
      userInfo: {},
      updateOption: 0,
      showName: false,
      nickname: '',
      introduction: '',
      tags: '',
      sex: 2,
      showDatePicker: false,
      addressShow: false,
      indexs: [0, 0, 0],
      areaId: [1, 110000, 110101],
      addressData: ['北京市', '北京市', '东城区'],
      type: 3, //1-省，2-省市，3-省市区
      date: new Date().toISOString().split('T')[0], // 格式化为 YYYY-MM-DD
      currentBirthdayTs: new Date().getTime() // 生日选择器使用的时间戳
    };
  },
  onLoad() {
    const cache = uni.getStorageSync('userInfo') || {};
    this.userInfo = cache;
    this.sex = cache.sex || 2;
    this.introduction = cache.selfIntroduction || '';
    this.nickname = cache.nickname || '';
    // 标签需要从 JSON 格式转换为逗号分隔的字符串格式
    this.tags = this.formatTagsForEdit(cache.tags) || '';
    // 初始化生日选择器时间戳
    if (this.userInfo.birthday) {
      this.currentBirthdayTs = new Date(this.userInfo.birthday).getTime();
    } else {
      this.currentBirthdayTs = new Date().getTime();
    }
  },
  onShow() {
    // 再请求接口刷新
    const cache = uni.getStorageSync('userInfo') || {};
    const userId = cache.id || cache.uid || '';
    if (!userId) return;
    getUserInfo({
      userId
    }).then(res => {
      if (res.code === 200) {
        const userInfo = mapUserInfo(res.data);
        this.userInfo = userInfo;
        this.sex = userInfo.sex;
        this.introduction = userInfo.selfIntroduction || '';
        this.nickname = userInfo.nickname || '';
        // 标签需要从 JSON 格式转换为逗号分隔的字符串格式
        this.tags = this.formatTagsForEdit(userInfo.tags) || '';
        if (this.userInfo.birthday) {
          this.currentBirthdayTs = new Date(this.userInfo.birthday).getTime();
        }
        uni.setStorageSync('userInfo', userInfo);
      }
    });
  },
  onBackPress() {
    if (this.showName) {
      this.showName = false;
      return true;
    }
  },
  methods: {
    chooseOccupation() {
      // this.$refs.uToast.show({
      //   type: 'warning',
      //   message: '暂未开放',
      //   duration: 2000
      // });
      return;
    },
    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        sourceType: ['album'],
        success: res => {
          const filePath = res.tempFilePaths[0];
          // 前端先上传到 OSS 获取 URL，再调后端只更新 URL
          uni.uploadFile({
            url: baseUrl + '/web/oss/upload',
            filePath,
            name: 'file',
            header: {
              accessToken: uni.getStorageSync('uniapp_token') || ''
            },
            success: uploadRes => {
              try {
                const data = JSON.parse(uploadRes.data);
                const isOk = data.code === 200;
                const avatarUrl = (data.data != null ? data.data : data.url) || '';
                if (!isOk || !avatarUrl) {
                  this.$refs.uToast.show({
                    type: 'error',
                    message: data.msg || '上传失败',
                    duration: 2000
                  });
                  return;
                }
                updateAvatarUrl({
                  userVO: {
                    id: this.userInfo.id,
                    avatarUrl
                  }
                })
                  .then(apiRes => {
                    if (apiRes.code === 200) {
                      this.$refs.uToast.show({
                        type: 'success',
                        message: '更换成功',
                        duration: 1500
                      });
                      if (apiRes.data) {
                        this.userInfo = mapUserInfo(apiRes.data);
                      } else {
                        this.userInfo.avatarUrl = avatarUrl;
                      }
                      uni.setStorageSync('userInfo', this.userInfo);
                      this.$forceUpdate();
                    } else {
                      this.$refs.uToast.show({
                        type: 'error',
                        message: apiRes.msg || '更换失败',
                        duration: 2000
                      });
                    }
                  })
                  .catch(err => {
                    console.error('更新头像失败:', err);
                    this.$refs.uToast.show({
                      type: 'error',
                      message: err.msg || '更换失败，请稍后重试',
                      duration: 2000
                    });
                  });
              } catch (e) {
                console.error('解析上传结果失败:', e);
                this.$refs.uToast.show({
                  type: 'error',
                  message: '上传失败，请稍后重试',
                  duration: 2000
                });
              }
            },
            fail: err => {
              console.error('上传头像失败:', err);
              this.$refs.uToast.show({
                type: 'error',
                message: '上传失败，请稍后重试',
                duration: 2000
              });
            }
          });
        },
        fail: err => {
          console.log(err.errMsg);
          this.$refs.uToast.show({
            type: 'error',
            message: '选择图片失败',
            duration: 2000
          });
        }
      });
    },
    chooseBackgroundImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        sourceType: ['album'],
        success: res => {
          const filePath = res.tempFilePaths[0];
          // 前端先上传到 OSS 获取 URL，再调后端只更新 URL
          uni.uploadFile({
            url: baseUrl + '/web/oss/upload',
            filePath,
            name: 'file',
            header: {
              accessToken: uni.getStorageSync('uniapp_token') || ''
            },
            success: uploadRes => {
              try {
                const data = JSON.parse(uploadRes.data);
                const isOk = data.code === 200;
                const userCover = (data.data != null ? data.data : data.url) || '';
                if (!isOk || !userCover) {
                  this.$refs.uToast.show({
                    type: 'error',
                    message: data.msg || '上传失败',
                    duration: 2000
                  });
                  return;
                }
                updateBackgroundImage({
                  userVO: {
                    id: this.userInfo.id,
                    userCover
                  }
                })
                  .then(apiRes => {
                    if (apiRes.code === 200) {
                      this.$refs.uToast.show({
                        type: 'success',
                        message: '更换成功',
                        duration: 1500
                      });
                      if (apiRes.data) {
                        this.userInfo = mapUserInfo(apiRes.data);
                      } else {
                        this.userInfo.homePageBackground = userCover;
                      }
                      uni.setStorageSync('userInfo', this.userInfo);
                      this.$forceUpdate();
                    } else {
                      this.$refs.uToast.show({
                        type: 'error',
                        message: apiRes.msg || '更换失败',
                        duration: 2000
                      });
                    }
                  })
                  .catch(err => {
                    console.error('更新背景图失败:', err);
                    this.$refs.uToast.show({
                      type: 'error',
                      message: err.msg || '更换失败，请稍后重试',
                      duration: 2000
                    });
                  });
              } catch (e) {
                console.error('解析上传结果失败:', e);
                this.$refs.uToast.show({
                  type: 'error',
                  message: '上传失败，请稍后重试',
                  duration: 2000
                });
              }
            },
            fail: err => {
              console.error('上传背景图失败:', err);
              this.$refs.uToast.show({
                type: 'error',
                message: '上传失败，请稍后重试',
                duration: 2000
              });
            }
          });
        },
        fail: err => {
          console.log(err.errMsg);
          this.$refs.uToast.show({
            type: 'error',
            message: '选择图片失败',
            duration: 2000
          });
        }
      });
    },
    confirmAddress(e) {
      updateArea({
        userVO: {
          id: this.userInfo.id,
          address: e.value.join(' ')
        }
      })
        .then(res => {
          if (res.code === 200) {
            // 如果第一个与第二个相同，说明是直辖市，只需要显示第一个
            if (e.value[0] === e.value[1]) {
              this.userInfo.area = e.value[0] + ' ' + e.value[2];
            } else {
              this.userInfo.area = e.value.join(' ');
            }
            this.addressShow = false;
            uni.setStorageSync('userInfo', this.userInfo);
            this.$forceUpdate();
          } else {
            this.$refs.uToast.show({
              type: 'error',
              message: res.msg || '更改失败',
              duration: 2000
            });
          }
        })
        .catch(err => {
          console.error('更新地区失败:', err);
          this.$refs.uToast.show({
            type: 'error',
            message: err.msg || '更改失败，请稍后重试',
            duration: 2000
          });
        });
    },
    confirmDate(e) {
      // 使用本地时区格式化日期，避免 toISOString 导致日期偏移一天
      const d = new Date(e.value);
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      const selectedDate = `${y}-${m}-${day}`;
      updateBirthday({
        userVO: {
          id: this.userInfo.id,
          birthday: selectedDate
        }
      })
        .then(res => {
          if (res.code === 200) {
            // 如果后端返回了更新后的用户数据，使用后端数据
            if (res.data) {
              const updatedUserInfo = mapUserInfo(res.data);
              this.userInfo = updatedUserInfo;
            } else {
              this.userInfo.birthday = selectedDate;
            }
            uni.setStorageSync('userInfo', this.userInfo);
            this.showDatePicker = false;
            this.$forceUpdate();
          } else {
            this.$refs.uToast.show({
              type: 'error',
              message: res.msg || '更改失败',
              duration: 2000
            });
          }
        })
        .catch(err => {
          console.error('更新生日失败:', err);
          this.$refs.uToast.show({
            type: 'error',
            message: err.msg || '更改失败，请稍后重试',
            duration: 2000
          });
        });
    },
    bindDateChange(e) {
      updateBirthday({
        userVO: {
          id: this.userInfo.id,
          birthday: e.detail.value
        }
      })
        .then(res => {
          if (res.code === 200) {
            // 如果后端返回了更新后的用户数据，使用后端数据
            if (res.data) {
              const updatedUserInfo = mapUserInfo(res.data);
              this.userInfo = updatedUserInfo;
            } else {
              this.userInfo.birthday = e.detail.value;
            }
            uni.setStorageSync('userInfo', this.userInfo);
            this.$forceUpdate();
          } else {
            this.$refs.uToast.show({
              type: 'error',
              message: res.msg || '更改失败',
              duration: 2000
            });
          }
        })
        .catch(err => {
          console.error('更新生日失败:', err);
          this.$refs.uToast.show({
            type: 'error',
            message: err.msg || '更改失败，请稍后重试',
            duration: 2000
          });
        });
    },
    groupChange(e) {
      updateSex({
        userVO: {
          id: this.userInfo.id,
          gender: this.sex
        }
      })
        .then(res => {
          if (res.code === 200) {
            this.userInfo.sex = this.sex;
            uni.setStorageSync('userInfo', this.userInfo);
            this.$forceUpdate();
          } else {
            this.$refs.uToast.show({
              type: 'error',
              message: res.msg || '更改失败',
              duration: 2000
            });
          }
        })
        .catch(err => {
          console.error('更新性别失败:', err);
          this.$refs.uToast.show({
            type: 'error',
            message: err.msg || '更改失败，请稍后重试',
            duration: 2000
          });
        });
    },
    saveIntroduction() {
      if (this.introduction == '') {
        return;
      }
      if (this.introduction.length > 100) {
        this.$refs.uToast.show({
          type: 'warning',
          message: '个人简介不能超过100个字符哦',
          duration: 2000
        });
        return;
      }
      updateIntroduction({
        userVO: {
          id: this.userInfo.id,
          description: this.introduction
        }
      })
        .then(res => {
          if (res.code === 200) {
            this.$refs.uToast.show({
              type: 'success',
              message: '更改成功',
              duration: 1500
            });
            // 更新本地用户信息
            this.userInfo.selfIntroduction = this.introduction;
            // 如果后端返回了更新后的用户数据，使用后端数据
            if (res.data) {
              const updatedUserInfo = mapUserInfo(res.data);
              this.userInfo = updatedUserInfo;
              this.introduction = updatedUserInfo.selfIntroduction || '';
            }
            uni.setStorageSync('userInfo', this.userInfo);
            // 强制更新视图
            this.$forceUpdate();
            this.showName = false;
          } else {
            this.$refs.uToast.show({
              type: 'error',
              message: res.msg || '更改失败',
              duration: 2000
            });
          }
        })
        .catch(err => {
          console.error('更新简介失败:', err);
          this.$refs.uToast.show({
            type: 'error',
            message: err.msg || '更改失败，请稍后重试',
            duration: 2000
          });
        });
    },
    saveTags() {
      const tagsStr = this.tags.trim();
      if (tagsStr.length > 50) {
        this.$refs.uToast.show({
          type: 'warning',
          message: '标签不能超过50个字符哦',
          duration: 2000
        });
        return;
      }
      // 将逗号分隔的字符串转换为 JSON 数组格式（同时兼容英文逗号和中文逗号）
      let tagsJson = '';
      if (tagsStr) {
        // 先把中文逗号统一替换为英文逗号，方便后续 split
        const normalized = tagsStr.replace(/，/g, ',');
        const tagsArray = normalized
          .split(',')
          .map(tag => tag.trim())
          .filter(tag => tag);
        tagsJson = JSON.stringify(tagsArray);
      }
      updateTags({
        userVO: {
          id: this.userInfo.id,
          tags: tagsJson
        }
      })
        .then(res => {
          if (res.code === 200) {
            this.$refs.uToast.show({
              type: 'success',
              message: '更改成功',
              duration: 1500
            });
            // 如果后端返回了更新后的用户数据，使用后端数据
            if (res.data) {
              const updatedUserInfo = mapUserInfo(res.data);
              this.userInfo = updatedUserInfo;
              // 将 JSON 数组转换为逗号分隔的字符串用于编辑
              this.tags = this.formatTagsForEdit(updatedUserInfo.tags);
              // 将 tags 保存为数组格式到本地存储（mine 页面需要数组格式用于显示）
              if (updatedUserInfo.tags) {
                try {
                  const parsed = JSON.parse(updatedUserInfo.tags);
                  if (Array.isArray(parsed)) {
                    updatedUserInfo.tags = parsed;
                  }
                } catch (e) {
                  // 如果不是 JSON，保持原样
                }
              }
            } else {
              // 如果没有返回数据，使用当前输入转换后的格式
              if (tagsJson) {
                try {
                  const parsed = JSON.parse(tagsJson);
                  if (Array.isArray(parsed)) {
                    this.userInfo.tags = parsed;
                  } else {
                    this.userInfo.tags = tagsJson;
                  }
                } catch (e) {
                  this.userInfo.tags = tagsJson;
                }
              } else {
                this.userInfo.tags = [];
              }
            }
            uni.setStorageSync('userInfo', this.userInfo);
            // 强制更新视图
            this.$forceUpdate();
            this.showName = false;
          } else {
            this.$refs.uToast.show({
              type: 'error',
              message: res.msg || '更改失败',
              duration: 2000
            });
          }
        })
        .catch(err => {
          console.error('更新标签失败:', err);
          this.$refs.uToast.show({
            type: 'error',
            message: err.msg || '更改失败，请稍后重试',
            duration: 2000
          });
        });
    },
    // 格式化标签用于显示（将 JSON 数组转换为逗号分隔的字符串）
    formatTags(tags) {
      if (!tags) {
        return '';
      }
      // 确保 tags 是字符串类型
      const tagsStr = typeof tags === 'string' ? tags : String(tags);
      if (tagsStr.trim() === '') {
        return '';
      }
      // 如果标签是JSON格式，解析并转换为逗号分隔的字符串
      try {
        const tagsArray = JSON.parse(tagsStr);
        if (Array.isArray(tagsArray)) {
          return tagsArray.join(', ');
        }
      } catch (e) {
        // 如果不是JSON，可能是已经逗号分隔的字符串，直接返回
        return tagsStr;
      }
      return tagsStr;
    },
    // 格式化标签用于编辑（将 JSON 数组转换为逗号分隔的字符串，去掉空格）
    formatTagsForEdit(tags) {
      if (!tags) {
        return '';
      }
      // 确保 tags 是字符串类型
      const tagsStr = typeof tags === 'string' ? tags : String(tags);
      if (tagsStr.trim() === '') {
        return '';
      }
      // 如果标签是JSON格式，解析并转换为逗号分隔的字符串（不带空格）
      try {
        const tagsArray = JSON.parse(tagsStr);
        if (Array.isArray(tagsArray)) {
          return tagsArray.join(',');
        }
      } catch (e) {
        // 如果不是JSON，可能是已经逗号分隔的字符串，直接返回
        return tagsStr;
      }
      return tagsStr;
    },
    saveName() {
      if (this.nickname == '') {
        return;
      }
      if (this.nickname.length === 1) {
        this.$refs.uToast.show({
          type: 'warning',
          message: '名字不能少于两个字符哦',
          duration: 2000
        });
        return;
      }
      console.log('更改名字');
      updateNickname({
        userVO: {
          id: this.userInfo.id,
          username: this.nickname // 后端期望的是 username，不是 nickname
        }
      })
        .then(res => {
          if (res.code === 200) {
            this.$refs.uToast.show({
              type: 'success',
              message: '更改成功',
              duration: 1500
            });
            // 更新本地用户信息
            this.userInfo.nickname = this.nickname;
            // 如果后端返回了更新后的用户数据，使用后端数据
            if (res.data) {
              const updatedUserInfo = mapUserInfo(res.data);
              this.userInfo = updatedUserInfo;
              this.nickname = updatedUserInfo.nickname || '';
            }
            uni.setStorageSync('userInfo', this.userInfo);
            // 强制更新视图
            this.$forceUpdate();
            this.showName = false;
          } else {
            this.$refs.uToast.show({
              type: 'error',
              message: res.msg || '更改失败',
              duration: 2000
            });
          }
        })
        .catch(err => {
          console.error('更新用户名失败:', err);
          this.$refs.uToast.show({
            type: 'error',
            message: err.msg || '更改失败，请稍后重试',
            duration: 2000
          });
        });
    },
    openPopup(e) {
      console.log(e);
      this.updateOption = e.name;
      if (e.name === 2) {
        this.updateOption = 2;
        // this.$refs.uToast.show({
        //   type: 'warning',
        //   message: '功能还未开放',
        //   duration: 2000
        // });
        return;
      }
      // 每次弹窗打开都同步一次
      this.nickname = this.userInfo.nickname || '';
      this.introduction = this.userInfo.selfIntroduction || '';
      // 标签需要从 JSON 格式转换为逗号分隔的字符串格式
      this.tags = this.formatTagsForEdit(this.userInfo.tags) || '';
      this.sex = this.userInfo.sex;
      this.showName = true;
      return;
    }
  }
};
</script>

<style lang="scss">
page {
  background-color: #f5f5f5;
}

.up ::v-deep .u-cell {
  &__body {
    padding: 30rpx 0;
  }

  &__title-text {
    color: #7b7c7d;
  }

  &__value {
    color: #000000;
  }
}
</style>



