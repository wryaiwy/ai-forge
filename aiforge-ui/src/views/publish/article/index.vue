<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, CollectionTag, Document, Position, Box, MagicStick } from '@element-plus/icons-vue'
import { addArticleApi } from '@/api/article'
import { polishArticleApi } from '@/api/ai'
import type { AiPolishVO } from '@/types/ai'
import { PolishMode, polishModeOptions } from '@/types/ai'
import GlobalHeader from '@/layout/components/GlobalHeader.vue'

import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const articleTitle = ref('')
const articleTags = ref('')
const content = ref('')
const submitting = ref(false)

// AI 润色相关状态
const polishDialogVisible = ref(false)
const polishMode = ref<PolishMode>(PolishMode.PROFESSIONAL_POLISH)
const targetStyle = ref('')
const polishing = ref(false)
const polishResult = ref<AiPolishVO | null>(null)

const selectedModeDesc = computed(() => {
  return polishModeOptions.find(m => m.value === polishMode.value)?.description ?? ''
})

const handleOpenPolish = () => {
  if (!content.value.trim()) {
    ElMessage.warning('请先输入文章内容')
    return
  }
  polishResult.value = null
  targetStyle.value = ''
  polishDialogVisible.value = true
}

const handlePolish = async () => {
  if (polishMode.value === PolishMode.STYLE_TRANSFER && !targetStyle.value.trim()) {
    ElMessage.warning('请输入目标风格描述')
    return
  }
  polishing.value = true
  try {
    const res = await polishArticleApi({
      content: content.value,
      polishMode: polishMode.value,
      targetStyle: targetStyle.value.trim() || undefined,
    })
    polishResult.value = res.data
  } catch {
    ElMessage.error('AI 润色失败，请稍后重试')
  } finally {
    polishing.value = false
  }
}

const handleApplyPolish = () => {
  if (polishResult.value) {
    content.value = polishResult.value.polishedContent
    polishDialogVisible.value = false
    ElMessage.success('已应用润色结果')
  }
}

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
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon"><EditPen /></el-icon>
                <span>发布技术文章</span>
              </div>
              <span class="header-subtitle">分享你的技术见解</span>
            </div>
          </template>

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

            <el-col :span="24">
              <div class="editor-toolbar">
                <el-button
                  :icon="MagicStick"
                  type="success"
                  plain
                  size="small"
                  @click="handleOpenPolish"
                >
                  AI 润色
                </el-button>
              </div>
              <MdEditor
                v-model="content"
                class="custom-md-editor"
                placeholder="开始撰写你的技术文章，支持 Markdown 语法排版..."
              />
            </el-col>
          </el-row>

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

    <!-- AI 润色弹窗 -->
    <el-dialog
      v-model="polishDialogVisible"
      title="AI 文章润色"
      width="80%"
      class="polish-dialog"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <!-- 模式选择区域 -->
      <div class="polish-config">
        <el-radio-group v-model="polishMode" size="large">
          <el-radio-button
            v-for="option in polishModeOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </el-radio-button>
        </el-radio-group>
        <span class="mode-desc">{{ selectedModeDesc }}</span>

        <el-input
          v-if="polishMode === PolishMode.STYLE_TRANSFER"
          v-model="targetStyle"
          placeholder="请描述目标风格，例如：学术论文风格、轻松幽默风格..."
          class="style-input"
          size="default"
        />

        <el-button
          type="primary"
          :loading="polishing"
          :icon="MagicStick"
          size="large"
          class="polish-trigger-btn"
          @click="handlePolish"
        >
          {{ polishing ? '润色中...' : '开始润色' }}
        </el-button>
      </div>

      <!-- 对比展示区域 -->
      <el-row v-if="polishResult" :gutter="16" class="compare-area">
        <el-col :span="12">
          <div class="compare-panel original">
            <div class="compare-header">
              <span>原文内容</span>
              <el-tag type="info" size="small">原始</el-tag>
            </div>
            <MdEditor
              :model-value="polishResult.originalContent"
              class="compare-editor"
              preview-only
            />
          </div>
        </el-col>
        <el-col :span="12">
          <div class="compare-panel polished">
            <div class="compare-header">
              <span>润色结果</span>
              <el-tag type="success" size="small">{{ polishResult.polishModeDesc }}</el-tag>
            </div>
            <MdEditor
              :model-value="polishResult.polishedContent"
              class="compare-editor"
              preview-only
            />
          </div>
        </el-col>
      </el-row>

      <template #footer>
        <el-button @click="polishDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="!polishResult"
          @click="handleApplyPolish"
        >
          应用润色结果
        </el-button>
      </template>
    </el-dialog>
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

.custom-input {
  margin-bottom: 24px;
}

:deep(.title-input .el-input__inner) {
  font-size: 16px;
  font-weight: 500;
}

.editor-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.custom-md-editor {
  margin-bottom: 30px;
  height: 550px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: box-shadow 0.3s;
}

.custom-md-editor:focus-within {
  box-shadow: 0 0 0 1px #409eff inset;
}

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

/* AI 润色弹窗样式 */
.polish-config {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.mode-desc {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.style-input {
  max-width: 480px;
  margin-top: 8px;
}

.polish-trigger-btn {
  align-self: flex-start;
  margin-top: 4px;
}

.compare-area {
  margin-top: 16px;
}

.compare-panel {
  border-radius: 8px;
  overflow: hidden;
  height: 450px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.compare-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background-color: #f5f7fa;
  font-weight: 600;
  font-size: 14px;
  border-bottom: 1px solid #ebeef5;
}

.compare-editor {
  flex: 1;
}

.polish-dialog :deep(.el-dialog__body) {
  padding-top: 16px;
}
</style>
