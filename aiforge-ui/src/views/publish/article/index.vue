<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, CollectionTag, Document, Position, Box } from '@element-plus/icons-vue'
import { addArticleApi } from '@/api/article'
import GlobalHeader from '@/layout/components/GlobalHeader.vue'

// 1. 引入 Markdown 编辑器组件和样式
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const articleTitle = ref('')
const articleTags = ref('')
const content = ref('')
const submitting = ref(false)

const handleSubmit = async () => {
  if (!articleTitle.value.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!content.value.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }

  submitting.value = true
  try {
    await addArticleApi({
      articleTitle: articleTitle.value.trim(),
      articleTags: articleTags.value.trim(),
      content: content.value,
      publishStatus: 1,
    })
    ElMessage.success('文章发布成功')
    articleTitle.value = ''
    articleTags.value = ''
    content.value = ''
  } catch {
    ElMessage.error('文章发布失败')
  } finally {
    submitting.value = false
  }
}

const handleDraft = async () => {
  if (!articleTitle.value.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  submitting.value = true
  try {
    await addArticleApi({
      articleTitle: articleTitle.value.trim(),
      articleTags: articleTags.value.trim(),
      content: content.value,
      publishStatus: 3,
    })
    ElMessage.success('草稿保存成功')
    articleTitle.value = ''
    articleTags.value = ''
    content.value = ''
  } catch {
    ElMessage.error('草稿保存失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="publish-article-wrapper">
    <GlobalHeader />
    <el-row justify="center">
      <el-col :xs="24" :sm="24" :md="22" :lg="20" :xl="18">
        <el-card class="editor-card" shadow="hover">
          <!-- 卡片头部 -->
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon"><EditPen /></el-icon>
                <span>发布技术文章</span>
              </div>
              <span class="header-subtitle">分享你的技术见解</span>
            </div>
          </template>

          <!-- 表单区域使用 24 分栏进行精细排版 -->
          <el-row :gutter="20">
            <el-col :span="24">
              <el-input
                v-model="articleTitle"
                placeholder="请输入一个响亮且清晰的文章标题..."
                size="large"
                class="custom-input title-input"
                maxlength="100"
                show-word-limit
              >
                <template #prefix>
                  <el-icon><Document /></el-icon>
                </template>
              </el-input>
            </el-col>

            <el-col :span="24">
              <el-input
                v-model="articleTags"
                placeholder="添加标签 (多个标签请用逗号分隔，例如：Java, 微服务, Spring Boot)"
                size="large"
                class="custom-input tags-input"
              >
                <template #prefix>
                  <el-icon><CollectionTag /></el-icon>
                </template>
              </el-input>
            </el-col>

            <!-- 2. 替换为 Markdown 编辑器 -->
            <el-col :span="24">
              <MdEditor
                v-model="content"
                class="custom-md-editor"
                placeholder="开始撰写你的技术文章，支持 Markdown 语法排版..."
              />
            </el-col>
          </el-row>

          <!-- 底部操作栏 -->
          <div class="action-bar">
            <el-button
              size="large"
              class="draft-btn"
              :loading="submitting"
              :icon="Box"
              @click="handleDraft"
            >
              存为草稿
            </el-button>
            <el-button
              type="primary"
              size="large"
              class="publish-btn"
              :loading="submitting"
              :icon="Position"
              @click="handleSubmit"
            >
              发布文章
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.publish-article-wrapper {
  min-height: 100vh;
  padding-bottom: 20px;
  background-color: #f5f7fa;
}

.editor-card {
  border-radius: 12px;
  border: none;
}

/* 头部样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 8px;
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-icon {
  margin-right: 8px;
  color: #409eff;
  font-size: 24px;
}

.header-subtitle {
  font-size: 14px;
  color: #909399;
}

/* 输入框通用间距 */
.custom-input {
  margin-bottom: 24px;
}

:deep(.title-input .el-input__inner) {
  font-size: 16px;
  font-weight: 500;
}

/* Markdown 编辑器自定义样式 */
.custom-md-editor {
  margin-bottom: 30px;
  height: 550px; /* 设置一个合适的默认高度 */
  border-radius: 8px;
  overflow: hidden; /* 确保圆角生效 */
  box-shadow: 0 0 0 1px #dcdfe6 inset; /* 增加一层淡边框与 Element Plus 统一 */
  transition: box-shadow 0.3s;
}

.custom-md-editor:focus-within {
  box-shadow: 0 0 0 1px #409eff inset; /* 获取焦点时的主题色边框 */
}

/* 底部操作栏 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.draft-btn {
  min-width: 120px;
  border-radius: 8px;
}

.publish-btn {
  min-width: 140px;
  border-radius: 8px;
  font-weight: 600;
}
</style>
