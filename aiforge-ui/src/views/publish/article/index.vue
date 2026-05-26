<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  MagicStick,
  Back,
  ArrowDown,
} from '@element-plus/icons-vue'
import { addArticleApi, getArticleDetailApi, updateArticleApi } from '@/api/article'
import { polishArticleApi, translateArticleApi } from '@/api/ai'
import type { AiPolishVO, AiTranslateVO } from '@/types/ai'
import { PolishMode, polishModeOptions, translateLangOptions } from '@/types/ai'
import GlobalHeader from '@/layout/components/GlobalHeader.vue'

import DraftSvg from '@/assets/images/svg/biz/article/draft.svg?component'
import ReleaseSvg from '@/assets/images/svg/biz/article/release.svg?component'
import PolishSvg from '@/assets/images/svg/biz/article/pencil.svg?component'
import TranslateSvg from '@/assets/images/svg/biz/article/translate.svg?component'
import MagicSvg from '@/assets/images/svg/biz/article/magic.svg?component'
import ClockSvg from '@/assets/images/svg/biz/article/clock.svg?component'
import PreviewSvg from '@/assets/images/svg/biz/article/preview.svg?component'


import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const router = useRouter()
const route = useRoute()

// 基础表单状态
const articleTitle = ref('')
const articleTags = ref('')
const content = ref('')
const submitting = ref(false)

const isEdit = ref(false)
const articleId = ref<number | null>(null)

onMounted(async () => {
  const id = route.query.id
  if (id) {
    isEdit.value = true
    articleId.value = Number(id)
    try {
      const res = await getArticleDetailApi(articleId.value)
      if (res.data) {
        articleTitle.value = res.data.articleTitle || ''
        articleTags.value = res.data.articleTags || ''
        content.value = res.data.content || ''
      }
    } catch (e) {
      ElMessage.error('获取文章详情失败')
    }
  }
})

// 建议标签数据
const suggestedTags = [
  'Java',
  'Spring Boot',
  '微服务',
  '后端开发',
  '数据库',
  'Docker',
  'Kubernetes',
]

// AI 润色相关状态
const polishDialogVisible = ref(false)
const polishMode = ref<PolishMode>(PolishMode.PROFESSIONAL_POLISH)
const targetStyle = ref('')
const polishing = ref(false)
const polishResult = ref<AiPolishVO | null>(null)

const translateDialogVisible = ref(false)
const targetLang = ref('en')
const translating = ref(false)
const translateResult = ref<AiTranslateVO | null>(null)

const selectedModeDesc = computed(() => {
  return polishModeOptions.find((m) => m.value === polishMode.value)?.description ?? ''
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
    // 异常提示已由全局 request.ts 处理
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

const handleOpenTranslate = () => {
  if (!content.value.trim()) {
    ElMessage.warning('请先输入文章内容')
    return
  }
  translateResult.value = null
  targetLang.value = 'en'
  translateDialogVisible.value = true
}

const handleTranslate = async () => {
  translating.value = true
  try {
    const res = await translateArticleApi({
      content: content.value,
      targetLang: targetLang.value,
    })
    translateResult.value = res.data
  } catch {
    // 异常提示已由全局 request.ts 处理
  } finally {
    translating.value = false
  }
}

const handleApplyTranslate = () => {
  if (translateResult.value) {
    content.value = translateResult.value.translatedContent
    translateDialogVisible.value = false
    ElMessage.success('已应用翻译结果')
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
    const payload = {
      articleId: articleId.value || undefined,
      articleTitle: articleTitle.value.trim(),
      articleTags: articleTags.value.trim(),
      content: content.value,
      publishStatus: 1,
    }
    
    if (isEdit.value) {
      await updateArticleApi(payload)
      ElMessage.success('文章修改成功')
    } else {
      await addArticleApi(payload)
      ElMessage.success('文章发布成功')
    }
    
    articleTitle.value = ''
    articleTags.value = ''
    content.value = ''
    router.push('/personalCenter')
  } catch {
    // 异常提示已由全局 request.ts 处理
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
    const payload = {
      articleId: articleId.value || undefined,
      articleTitle: articleTitle.value.trim(),
      articleTags: articleTags.value.trim(),
      content: content.value,
      publishStatus: 3,
    }

    if (isEdit.value) {
      await updateArticleApi(payload)
      ElMessage.success('草稿修改成功')
    } else {
      await addArticleApi(payload)
      ElMessage.success('草稿保存成功')
    }
    
    articleTitle.value = ''
    articleTags.value = ''
    content.value = ''
  } catch {
    // 异常提示已由全局 request.ts 处理
  } finally {
    submitting.value = false
  }
}

// 快捷添加推荐标签
const addSuggestedTag = (tag: string) => {
  const currentTags = articleTags.value
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean)
  if (!currentTags.includes(tag)) {
    currentTags.push(tag)
    articleTags.value = currentTags.join(', ')
  }
}
</script>

<template>
  <div class="publish-article-page">
    <GlobalHeader />
    <div class="page-header">
      <el-row align="middle" justify="space-between" class="header-inner">
        <div class="header-left">
          <el-button circle class="back-btn" :icon="Back" />
          <div class="title-wrap">
            <h1 class="page-title">{{ isEdit ? '编辑文章' : '撰写新文章' }}</h1>
            <span class="page-subtitle">分享你的技术见解与经验</span>
          </div>
        </div>
        <div class="header-actions">
          <el-button class="action-btn" :loading="submitting" @click="handleDraft">
            <div class="btn-icon"><DraftSvg /></div>
            <div class="btn-text">存为草稿</div>
          </el-button>
          <!--          <el-button class="action-btn" :icon="View"> 预览 </el-button>-->
          <el-button-group class="publish-btn-group">
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              <div class="btn-icon"><ReleaseSvg /></div>
              <div class="btn-text">{{ isEdit ? '保存发布' : '发布文章' }}</div>
            </el-button>
            <el-dropdown trigger="click">
              <el-button type="primary" :icon="ArrowDown" />
              <template #dropdown>
                <el-dropdown-menu>
                  <ClockSvg/><el-dropdown-item>定时发布</el-dropdown-item>
                  <PreviewSvg/><el-dropdown-item divided>仅自己可见</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-button-group>
        </div>
      </el-row>
    </div>

    <div class="main-container">
      <el-row justify="center">
        <el-col :span="24">
          <div class="form-section panel-card">
            <div class="form-item">
              <label class="form-label">文章标题</label>
              <el-input
                v-model="articleTitle"
                placeholder="请输入一个清晰且有吸引力的文章标题..."
                maxlength="100"
                show-word-limit
                class="custom-light-input"
              />
            </div>

            <div class="form-item">
              <label class="form-label">文章标签</label>
              <el-input
                v-model="articleTags"
                placeholder="添加 3-8 个标签（按回车确认）"
                maxlength="8"
                class="custom-light-input"
              />
              <div class="suggested-tags">
                <span class="suggest-label">建议标签：</span>
                <el-tag
                  v-for="tag in suggestedTags"
                  :key="tag"
                  round
                  effect="light"
                  class="tag-item"
                  @click="addSuggestedTag(tag)"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </div>

          <div class="editor-section panel-card">
            <div class="editor-header">
              <div class="editor-tabs">
                <div class="tab-item active">
                    <PolishSvg /> Markdown编辑器
                </div>
              </div>
              <div class="editor-tools">
                <div class="tool-group">
                  <MagicSvg />
                  <el-button
                    link
                    size="small"
                    class="ai-tool-btn polish-btn"
                    @click="handleOpenPolish"
                  >
                    AI 润色
                  </el-button>
                </div>
                <div class="tool-group">
                  <TranslateSvg />
                  <el-button
                    link
                    size="small"
                    class="ai-tool-btn translate-btn"
                    @click="handleOpenTranslate"
                  >
                    AI 翻译
                  </el-button>
                </div>
              </div>
            </div>

            <MdEditor
              v-model="content"
              class="custom-md-editor"
              placeholder="开始撰写你的技术文章..."
            />
          </div>

          <!-- 高级选项 -->
          <!--          <div class="advanced-section panel-card">-->
          <!--            <div class="advanced-header">-->
          <!--              <div class="advanced-title">-->
          <!--                <el-icon><Setting /></el-icon> 高级选项-->
          <!--              </div>-->
          <!--              <el-icon class="arrow-icon"><ArrowDown /></el-icon>-->
          <!--            </div>-->
          <!--          </div>-->
        </el-col>
      </el-row>
    </div>

    <el-dialog
      v-model="polishDialogVisible"
      title="AI 文章润色"
      width="80%"
      class="ai-dialog"
      :close-on-click-modal="false"
      destroy-on-close
    >
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
          class="ai-trigger-btn"
          @click="handlePolish"
        >
          {{ polishing ? '润色中...' : '开始润色' }}
        </el-button>
      </div>

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
        <el-button type="primary" :disabled="!polishResult" @click="handleApplyPolish"
          >应用润色结果</el-button
        >
      </template>
    </el-dialog>

    <el-dialog
      v-model="translateDialogVisible"
      title="AI 文章翻译"
      width="80%"
      class="ai-dialog"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="polish-config">
        <el-select v-model="targetLang" size="large" style="width: 200px">
          <el-option
            v-for="lang in translateLangOptions"
            :key="lang.value"
            :label="lang.label"
            :value="lang.value"
          />
        </el-select>
        <el-button
          type="primary"
          :loading="translating"
          :icon="MagicStick"
          size="large"
          class="ai-trigger-btn"
          @click="handleTranslate"
        >
          {{ translating ? '翻译中...' : '开始翻译' }}
        </el-button>
      </div>

      <el-row v-if="translateResult" :gutter="16" class="compare-area">
        <el-col :span="12">
          <div class="compare-panel original">
            <div class="compare-header">
              <span>原文内容</span>
              <el-tag type="info" size="small">原始</el-tag>
            </div>
            <MdEditor
              :model-value="translateResult.originalContent"
              class="compare-editor"
              preview-only
            />
          </div>
        </el-col>
        <el-col :span="12">
          <div class="compare-panel polished">
            <div class="compare-header">
              <span>翻译结果</span>
              <el-tag type="success" size="small">{{ translateResult.targetLangDesc }}</el-tag>
            </div>
            <MdEditor
              :model-value="translateResult.translatedContent"
              class="compare-editor"
              preview-only
            />
          </div>
        </el-col>
      </el-row>
      <template #footer>
        <el-button @click="translateDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!translateResult" @click="handleApplyTranslate"
          >应用翻译结果</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 全局页面样式 */
.publish-article-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 40px;
}

/* 顶部导航栏 */
.page-header {
  background-color: #ffffff;
  padding: 12px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1400px;
  margin: 0 auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  border: 1px solid #e4e7ed;
  color: #606266;
  font-size: 16px;
}

.title-wrap {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2329;
  margin: 0;
  line-height: 1.2;
}

.page-subtitle {
  font-size: 12px;
  color: #8f959e;
  margin-top: 4px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-btn {
  color: #000000;
  border-color: #e4e7ed;
  background-color: #fff;
}

.publish-btn-group .el-button {
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 19px;
  height: 19px;
  margin-right: 8px;
}

.btn-icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.btn-text {
  display: flex;
  align-items: center;
  font-size: 16px;
}

/* 主容器配置 */
.main-container {
  max-width: 1200px;
  margin: 24px auto 0;
  padding: 0 24px;
}

.panel-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

/* 表单区域 */
.form-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 18px;
  font-weight: 500;
  color: #1f2329;
}

.required {
  color: #f56c6c;
  margin-left: 2px;
}

/* 统一重写输入框样式 */
:deep(.custom-light-input .el-input__wrapper),
:deep(.custom-light-input .el-textarea__inner) {
  box-shadow: none !important;
  border: 1px solid #e4e7ed;
  background-color: #fff;
  transition: all 0.2s;
  border-radius: 6px;
}

:deep(.custom-light-input.el-input .el-input__wrapper) {
  padding: 8px 15px;
}

:deep(.custom-light-input .el-input__wrapper:hover),
:deep(.custom-light-input .el-textarea__inner:hover),
:deep(.custom-light-input .el-input__wrapper.is-focus),
:deep(.custom-light-input .el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 1px #409eff !important;
}

.suggested-tags {
  margin-top: 10px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.suggest-label {
  font-size: 13px;
  color: #8f959e;
}

.tag-item {
  cursor: pointer;
  background-color: #f0f7ff;
  border-color: #f0f7ff;
  color: #409eff;
}
.tag-item:hover {
  background-color: #d9ecff;
}

/* 编辑器区域 */
.editor-section {
  padding: 0;
  overflow: hidden;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #f0f2f5;
  background-color: #fafbfc;
}

.editor-tabs {
  display: flex;
  gap: 24px;
}

.tab-item {
  padding: 16px 0;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
}

.tab-item.active {
  color: #409eff;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #409eff;
  border-radius: 2px 2px 0 0;
}

.editor-tools {
  display: flex;
  align-items: center;
  gap: 16px;
}

.tool-group {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
}

.ai-tool-btn {
  font-size: 16px;
  padding: 0;
  height: auto;
}

.polish-btn {
  color: #67c23a;
}

.translate-btn {
  color: #409eff;
}

.custom-md-editor {
  height: 600px;
  border: none !important;
}

/* 高级选项区域 */
.advanced-section {
  padding: 16px 24px;
  cursor: pointer;
}

.advanced-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.advanced-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #1f2329;
}

.arrow-icon {
  color: #8f959e;
}

/* AI 对话框专属样式修正 */
.ai-dialog :deep(.el-dialog__body) {
  padding-top: 16px;
}

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

.ai-trigger-btn {
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
</style>
