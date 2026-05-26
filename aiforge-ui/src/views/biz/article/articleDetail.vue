<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Document, ChatDotRound, Promotion, Close, CloseBold } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import GlobalHeader from '@/layout/components/GlobalHeader.vue'
import { getArticleDetailApi, generateArticleSummaryStreamApi, generateArticleQAStreamApi } from '@/api/article'
import type { BizArticleVO } from '@/types/article'
import HomeAvatarSvg from '@/assets/images/svg/home/home_avatar.svg'
import HomeCalendarSvg from '@/assets/images/svg/home/home_calendar.svg'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const article = ref<BizArticleVO | null>(null)

const summaryText = ref('')
const summaryLoading = ref(false)
let summaryController: AbortController | null = null

const articleId = computed(() => Number(route.params.articleId))

const tagList = computed(() => {
  if (!article.value?.articleTags) return []
  return article.value.articleTags
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean)
})

const formatTime = (time?: string) => {
  if (!time) return '未发布'
  return time.slice(0, 16)
}

const fetchArticle = async () => {
  if (!articleId.value || Number.isNaN(articleId.value)) {
    ElMessage.error('无效的文章 ID')
    router.replace('/')
    return
  }

  loading.value = true
  article.value = null
  try {
    const res = await getArticleDetailApi(articleId.value)
    if (!res.data) {
      ElMessage.error('文章不存在')
      router.replace('/')
      return
    }
    article.value = res.data
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

const handleGenerateSummary = () => {
  if (summaryLoading.value) {
    summaryController?.abort()
    summaryLoading.value = false
    return
  }

  summaryText.value = ''
  summaryLoading.value = true

  summaryController = generateArticleSummaryStreamApi(
    articleId.value,
    (text) => {
      summaryText.value += text
    },
    () => {
      summaryLoading.value = false
    },
    (err) => {
      ElMessage.error('生成摘要失败: ' + err.message)
      summaryLoading.value = false
    }
  )
}

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

const qaVisible = ref(false)
const qaInput = ref('')
const qaMessages = ref<ChatMessage[]>([])
const qaLoading = ref(false)
let qaController: AbortController | null = null
const qaBodyRef = ref<HTMLDivElement | null>(null)

const scrollQaToBottom = () => {
  nextTick(() => {
    if (qaBodyRef.value) {
      qaBodyRef.value.scrollTop = qaBodyRef.value.scrollHeight
    }
  })
}

const handleSendQuestion = () => {
  const question = qaInput.value.trim()
  if (!question || qaLoading.value) return

  qaMessages.value.push({ role: 'user', content: question })
  qaInput.value = ''
  qaMessages.value.push({ role: 'assistant', content: '' })
  qaLoading.value = true
  scrollQaToBottom()

  qaController = generateArticleQAStreamApi(
    articleId.value,
    question,
    (text) => {
      const last = qaMessages.value[qaMessages.value.length - 1]
      if (last?.role === 'assistant') {
        last.content += text
        scrollQaToBottom()
      }
    },
    () => {
      qaLoading.value = false
    },
    (err) => {
      ElMessage.error('问答失败: ' + err.message)
      qaLoading.value = false
    }
  )
}

const handleStopQA = () => {
  qaController?.abort()
  qaLoading.value = false
}

onMounted(fetchArticle)
watch(articleId, fetchArticle)
</script>

<template>
  <el-container class="article-detail-page">
    <el-header class="page-header" height="auto">
      <GlobalHeader />
    </el-header>

    <el-main class="page-main">
      <div v-loading="loading" class="detail-container">
        <!-- 顶部导航重构为 el-page-header -->
        <el-page-header @back="handleBack" class="detail-toolbar">
          <template #content>
            <span class="toolbar-label">文章详情</span>
          </template>
        </el-page-header>

        <template v-if="!loading && article">
          <el-card class="article-panel" shadow="never">
            <h1 class="article-title">{{ article.articleTitle }}</h1>

            <div class="article-meta">
              <span class="meta-item">
                <HomeAvatarSvg />
                {{ article.authorName || '佚名' }}
              </span>
              <el-divider direction="vertical" />
              <span class="meta-item">
                <HomeCalendarSvg />
                {{ formatTime(article.publishTime) }}
              </span>
            </div>

            <div v-if="tagList.length" class="article-tags">
              <el-tag
                v-for="tag in tagList"
                :key="tag"
                size="small"
                type="info"
                effect="plain"
                round
              >
                {{ tag }}
              </el-tag>
            </div>

            <!--  流式输出  -->
            <div class="summary-section">
              <el-button
                type="primary"
                :icon="Document"
                :loading="summaryLoading"
                @click="handleGenerateSummary"
                round
              >
                {{ summaryLoading ? '停止生成' : '一键摘要' }}
              </el-button>
              
              <Transition name="el-fade-in-linear">
                <div v-if="summaryText" class="summary-content">
                  <div class="summary-label">
                    <el-icon><Document /></el-icon> AI 摘要
                  </div>
                  <div class="summary-text">{{ summaryText }}<span v-if="summaryLoading" class="typing-cursor">|</span></div>
                </div>
              </Transition>
            </div>

            <div class="article-content">
              <MdPreview
                editor-id="article-detail-preview"
                :model-value="article.content || ''"
              />
            </div>
          </el-card>
        </template>

        <el-empty v-if="!loading && !article" description="文章不存在或已删除" />
      </div>
    </el-main>

    <!-- 问答弹窗模块（固定右下角） -->
    <div class="qa-widget">
      <div class="qa-fab" @click="qaVisible = !qaVisible">
        <el-icon :size="24"><ChatDotRound /></el-icon>
      </div>

      <Transition name="qa-slide">
        <div v-if="qaVisible" class="qa-popup">
          <div class="qa-header">
            <span class="qa-header-title">知识问答</span>
            <el-icon class="qa-close" @click="qaVisible = false"><Close /></el-icon>
          </div>
          <div ref="qaBodyRef" class="qa-body">
            <div v-if="!qaMessages.length" class="qa-empty-hint">
              基于文章内容提问，AI 将为你解答
            </div>
            <div
              v-for="(msg, idx) in qaMessages"
              :key="idx"
              class="qa-msg"
              :class="msg.role === 'user' ? 'qa-msg-user' : 'qa-msg-ai'"
            >
              <div class="qa-msg-bubble">
                {{ msg.content }}<span v-if="msg.role === 'assistant' && qaLoading && idx === qaMessages.length - 1" class="typing-cursor">|</span>
              </div>
            </div>
          </div>
          <div class="qa-footer">
            <el-input
              v-model="qaInput"
              placeholder="输入你的问题..."
              :disabled="qaLoading"
              @keyup.enter="handleSendQuestion"
            >
              <template #append>
                <el-button
                  v-if="qaLoading"
                  :icon="CloseBold"
                  @click="handleStopQA"
                />
                <el-button
                  v-else
                  :icon="Promotion"
                  :disabled="!qaInput.trim()"
                  @click="handleSendQuestion"
                />
              </template>
            </el-input>
          </div>
        </div>
      </Transition>
    </div>
  </el-container>
</template>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
  padding: 0;
}

.page-main {
  padding: 0;
}

.detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 20px 60px;
  min-height: 400px;
}

.detail-toolbar {
  margin-bottom: 24px;
}

.toolbar-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.article-panel {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04) !important;
}

.article-panel :deep(.el-card__body) {
  padding: 40px 48px;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1.4;
  margin: 0 0 20px;
  word-break: break-word;
}

.article-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 14px;
  color: #909399;
  margin-bottom: 20px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.meta-item :deep(svg) {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
}

.article-content :deep(.md-editor-preview-wrapper) {
  padding: 0;
}

.summary-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.summary-content {
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 8px;
}

.summary-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 600;
}

.summary-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
  white-space: pre-wrap;
}

.typing-cursor {
  animation: blink 1s step-end infinite;
  color: #409eff;
  font-weight: bold;
}

@keyframes blink {
  50% { opacity: 0; }
}

/* --- 问答弹窗样式 --- */
.qa-widget {
  position: fixed;
  z-index: 2000;
}

.qa-fab {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  transition: transform 0.2s, box-shadow 0.2s;
  z-index: 2000;
}

.qa-fab:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 24px rgba(64, 158, 255, 0.5);
}

.qa-popup {
  position: fixed;
  bottom: 96px;
  right: 32px;
  width: 320px;
  max-height: 520px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 2000;
}

.qa-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  color: #fff;
  flex-shrink: 0;
}

.qa-header-title {
  font-size: 15px;
  font-weight: 600;
}

.qa-close {
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.qa-close:hover {
  opacity: 1;
}

.qa-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 200px;
  max-height: 360px;
  background: #f9fafb;
}

.qa-empty-hint {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 40px 0;
}

.qa-msg {
  margin-bottom: 12px;
  display: flex;
}

.qa-msg-user {
  justify-content: flex-end;
}

.qa-msg-ai {
  justify-content: flex-start;
}

.qa-msg-bubble {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.qa-msg-user .qa-msg-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.qa-msg-ai .qa-msg-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #ebeef5;
  border-bottom-left-radius: 4px;
}

.qa-footer {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  flex-shrink: 0;
}

.qa-footer :deep(.el-input-group__append) {
  padding: 0;
}

.qa-footer :deep(.el-input-group__append .el-button) {
  margin: 0;
  border: none;
  border-radius: 0;
  height: 32px;
}

.qa-slide-enter-active,
.qa-slide-leave-active {
  transition: all 0.25s ease;
}

.qa-slide-enter-from,
.qa-slide-leave-to {
  opacity: 0;
  transform: translateY(16px);
}

@media (max-width: 768px) {
  .article-panel :deep(.el-card__body) {
    padding: 24px 20px;
  }

  .article-title {
    font-size: 24px;
  }

  .qa-popup {
    width: 100%;
    right: 0;
    bottom: 0;
    border-radius: 12px 12px 0 0;
    max-height: 70vh;
  }

  .qa-fab {
    bottom: 20px;
    right: 20px;
  }
}
</style>
