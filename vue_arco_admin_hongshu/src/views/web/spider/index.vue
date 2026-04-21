<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['工具管理', '图片爬虫']"></Breadcrumb>
    <a-card
      class="general-card r-nav-card"
      :body-style="{ padding: '15px' }"
      :header-style="{ padding: '15px' }"
    >
      <a-row>
        <!-- 搜索区块 -->
        <a-col :flex="1">
          <a-form
            :model="state.queryParams"
            :label-col-props="{ span: 6 }"
            :wrapper-col-props="{ span: 18 }"
            label-align="left"
            auto-label-width
          >
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="keyword" :label="'关键字'">
                  <a-input
                    v-model="state.queryParams.keyword"
                    :placeholder="'输入爬取图片的英文关键字'"
                    @keyup.enter="handleSearch"
                  >
                    <template #prefix>
                      <icon-search />
                    </template>
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="4">
                <a-form-item>
                  <a-button type="primary" @click="handleSearch" :loading="state.searching">
                    <template #icon>
                      <icon-search />
                    </template>
                    开始爬取
                  </a-button>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 84px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space direction="vertical" :size="18">
            <a-button @click="handleUploadPicture" type="success">
              <template #icon>
                <icon-upload />
              </template>
              上传图片管理
            </a-button>
            <a-button @click="handleSelectAll" type="info">
              <template #icon>
                <icon-check />
              </template>
              {{ state.isCheckedAll ? '取消全选' : '全选' }}
            </a-button>
            <a-button 
              @click="handleDeleteBatch" 
              type="danger"
              :disabled="state.selectedPictures.length === 0"
            >
              <template #icon>
                <icon-delete />
              </template>
              删除选中
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <!-- 图片网格 -->
      <a-row :gutter="[16, 16]" style="margin-top: 20px">
        <a-col
          v-for="(url, index) in state.spiderPictureList"
          :key="index"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          :xl="4"
        >
          <a-card
            :body-style="{ padding: '8px' }"
            :bordered="false"
            class="picture-card"
            :class="{ 'selected': state.selectedPictures.includes(url) }"
          >
            <div class="picture-container">
              <input
                type="checkbox"
                :id="`picture-${index}`"
                :checked="state.selectedPictures.includes(url)"
                @change="handlePictureSelect(url, $event)"
                class="picture-checkbox"
              />
              <a-image
                :src="url"
                :preview="true"
                fit="cover"
                class="picture-image"
                @click="handlePreviewImage(url)"
              />
              <div class="picture-overlay">
                <a-space>
                  <a-tooltip content="上传至图片管理">
                    <a-button
                      type="text"
                      size="mini"
                      @click="handleUploadSingle(url)"
                    >
                      <template #icon>
                        <icon-upload />
                      </template>
                    </a-button>
                  </a-tooltip>
                  <a-tooltip content="复制图片地址">
                    <a-button
                      type="text"
                      size="mini"
                      @click="handleCopyUrl(url)"
                    >
                      <template #icon>
                        <icon-copy />
                      </template>
                    </a-button>
                  </a-tooltip>
                  <a-tooltip content="复制Markdown格式">
                    <a-button
                      type="text"
                      size="mini"
                      @click="handleCopyMarkdown(url, `图片${index + 1}`)"
                    >
                      <template #icon>
                        <icon-file />
                      </template>
                    </a-button>
                  </a-tooltip>
                  <a-tooltip content="删除图片">
                    <a-button
                      type="text"
                      size="mini"
                      status="danger"
                      @click="handleDeleteSingle(url)"
                    >
                      <template #icon>
                        <icon-delete />
                      </template>
                    </a-button>
                  </a-tooltip>
                </a-space>
              </div>
            </div>
            <div class="picture-title">
              图片 {{ index + 1 }}
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 分页 -->
      <div class="pagination-container" v-if="state.total > 0">
        <a-pagination
          v-model:current="state.currentPage"
          v-model:page-size="state.pageSize"
          :total="state.total"
          :show-total="true"
          :show-page-size="true"
          @change="handlePageChange"
        />
      </div>

      <!-- 上传图片管理对话框 -->
      <a-modal
        v-model:visible="state.uploadDialogVisible"
        title="上传至图片管理"
        @ok="handleSubmitUpload"
        @cancel="state.uploadDialogVisible = false"
      >
        <a-form :model="state.uploadForm" :label-col-props="{ span: 6 }">
          <a-form-item label="图片分类" field="pictureSortUid" required>
            <a-select
              v-model="state.uploadForm.pictureSortUid"
              placeholder="请选择图片分类"
            >
              <a-option
                v-for="item in state.pictureSortData"
                :key="item.uid"
                :value="item.uid"
                :label="item.name"
              />
            </a-select>
          </a-form-item>
        </a-form>
      </a-modal>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { reactive, onMounted } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { pictureSpider } from '@/api/web/spider';
  import { getPictureSortList } from '@/api/web/pictureSort';
  import { uploadPicsByUrl, addPicture } from '@/api/web/picture';
  import { getToken } from '@/utils/auth';

  const state = reactive({
    queryParams: {
      keyword: 'cat',
    },
    spiderPictureList: [] as string[],
    selectedPictures: [] as string[],
    isCheckedAll: false,
    currentPage: 1,
    pageSize: 20,
    total: 0,
    searching: false,
    uploadDialogVisible: false,
    uploadForm: {
      pictureSortUid: '',
    },
    pictureSortData: [] as Array<{
      uid: string;
      name: string;
    }>,
  });

  // 搜索图片
  const handleSearch = async () => {
    if (!state.queryParams.keyword.trim()) {
      Message.error('请输入需要爬取的关键字');
      return;
    }

    state.searching = true;
    try {
      const params = {
        currentPage: state.currentPage,
        keyword: state.queryParams.keyword,
      };
      
      const response = await pictureSpider(params);
      if (response.code === 'success' || response.code === 200) {
        state.spiderPictureList = response.data || [];
        state.total = state.spiderPictureList.length > 0 ? 100 : 0; // 暂时没有获取到返回的total
        Message.success(`成功爬取 ${state.spiderPictureList.length} 张图片`);
      } else {
        Message.error(response.message || '爬取失败');
      }
    } catch (error) {
      Message.error('爬取失败');
    } finally {
      state.searching = false;
    }
  };

  // 分页变化
  const handlePageChange = (page: number) => {
    state.currentPage = page;
    handleSearch();
  };

  // 全选/取消全选
  const handleSelectAll = () => {
    if (state.isCheckedAll) {
      state.selectedPictures = [];
      state.isCheckedAll = false;
    } else {
      state.selectedPictures = [...state.spiderPictureList];
      state.isCheckedAll = true;
    }
  };

  // 选择图片
  const handlePictureSelect = (url: string, event: Event) => {
    const target = event.target as HTMLInputElement;
    if (target.checked) {
      if (!state.selectedPictures.includes(url)) {
        state.selectedPictures.push(url);
      }
    } else {
      const index = state.selectedPictures.indexOf(url);
      if (index > -1) {
        state.selectedPictures.splice(index, 1);
      }
    }
    state.isCheckedAll = state.selectedPictures.length === state.spiderPictureList.length;
  };

  // 预览图片
  const handlePreviewImage = (url: string) => {
    // 图片预览由 a-image 组件的 preview 属性处理
  };

  // 复制图片地址
  const handleCopyUrl = async (url: string) => {
    try {
      await navigator.clipboard.writeText(url);
      Message.success('复制链接到剪切板成功');
    } catch (error) {
      Message.error('复制失败');
    }
  };

  // 复制Markdown格式
  const handleCopyMarkdown = async (url: string, name: string) => {
    const text = `![${name}](${url})`;
    try {
      await navigator.clipboard.writeText(text);
      Message.success('复制Markdown格式链接到剪切板成功');
    } catch (error) {
      Message.error('复制失败');
    }
  };

  // 上传单张图片
  const handleUploadSingle = (url: string) => {
    state.selectedPictures = [url];
    handleUploadPicture();
  };

  // 打开上传对话框
  const handleUploadPicture = () => {
    if (state.selectedPictures.length === 0) {
      Message.error('请先选择要上传的图片');
      return;
    }
    state.uploadDialogVisible = true;
  };

  // 提交上传
  const handleSubmitUpload = async () => {
    if (!state.uploadForm.pictureSortUid) {
      Message.error('请选择图片分类');
      return;
    }

    try {
      const params = {
        source: 'admin',
        userUid: 'uid00000000000000000000000000000000',
        adminUid: 'uid00000000000000000000000000000000',
        projectName: 'blog',
        sortName: 'admin',
        token: getToken(),
        urlList: state.selectedPictures,
      };

      const response = await uploadPicsByUrl(params);
      if (response.code === 'success' || response.code === 200) {
        // 将上传后返回的图片插入到图片管理中
        const data = response.data || [];
        const pictureUploadList = data.map((item: any) => ({
          fileUid: item.uid,
          pictureSortUid: state.uploadForm.pictureSortUid,
          picName: item.picName,
        }));

        const addResponse = await addPicture(pictureUploadList);
        if (addResponse.code === 'success' || addResponse.code === 200) {
          Message.success(addResponse.message || '上传成功');
          // 删除上传完的图片
          state.spiderPictureList = state.spiderPictureList.filter(
            url => !state.selectedPictures.includes(url)
          );
          state.selectedPictures = [];
          state.uploadDialogVisible = false;
        } else {
          Message.error(addResponse.message || '上传失败');
        }
      } else {
        Message.error(response.message || '上传失败');
      }
    } catch (error) {
      Message.error('上传失败');
    }
  };

  // 删除单张图片
  const handleDeleteSingle = (url: string) => {
    state.selectedPictures = [url];
    handleDeleteBatch();
  };

  // 批量删除
  const handleDeleteBatch = () => {
    if (state.selectedPictures.length === 0) {
      Message.error('请先选择要删除的图片');
      return;
    }

    state.spiderPictureList = state.spiderPictureList.filter(
      url => !state.selectedPictures.includes(url)
    );
    state.selectedPictures = [];
    state.isCheckedAll = false;
    Message.success('删除成功');
  };

  // 获取图片分类列表
  const getPictureSort = async () => {
    try {
      const params = {
        pageSize: 500,
        currentPage: 1,
        isShow: 1,
      };
      
      const response = await getPictureSortList(params);
      if (response.code === 'success' || response.code === 200) {
        state.pictureSortData = response.data?.records || [];
      }
    } catch (error) {
      console.error('获取图片分类失败:', error);
    }
  };

  onMounted(() => {
    handleSearch();
    getPictureSort();
  });
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px 20px;
  }

  .picture-card {
    position: relative;
    transition: all 0.3s ease;
    cursor: pointer;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &.selected {
      border-color: #1890ff;
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
    }
  }

  .picture-container {
    position: relative;
    width: 100%;
    height: 160px;
    border-radius: 6px;
    overflow: hidden;

    .picture-checkbox {
      position: absolute;
      top: 8px;
      left: 8px;
      z-index: 10;
      width: 16px;
      height: 16px;
    }

    .picture-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .picture-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s ease;

      .arco-btn {
        color: #fff;
        background: rgba(255, 255, 255, 0.2);
        border: none;

        &:hover {
          background: rgba(255, 255, 255, 0.3);
        }
      }
    }

    &:hover .picture-overlay {
      opacity: 1;
    }
  }

  .picture-title {
    text-align: center;
    margin-top: 8px;
    color: #666;
    font-size: 12px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
</style>








