<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import GlobalHeader from '@/layout/components/GlobalHeader.vue'
import { getArticleDetailApi } from '@/api/article'
import type { BizArticleVO } from '@/types/article'
import HomeAvatarSvg from '@/assets/images/svg/home/home_avatar.svg'
import HomeCalendarSvg from '@/assets/images/svg/home/home_calendar.svg'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const article = ref<BizArticleVO | null>(null)

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

onMounted(fetchArticle)
watch(articleId, fetchArticle)
</script>

<template>
  <div class="article-detail-page">
    <GlobalHeader />

    <div v-loading="loading" class="detail-container">
      <div class="detail-toolbar">
        <el-button circle class="back-btn" :icon="Back" @click="handleBack" />
        <span class="toolbar-label">文章详情</span>
      </div>

      <template v-if="!loading && article">
        <article class="article-panel">
          <h1 class="article-title">{{ article.articleTitle }}</h1>

          <div class="article-meta">
            <span class="meta-item">
              <HomeAvatarSvg />
              {{ article.authorName || '佚名' }}
            </span>
            <span class="meta-divider">·</span>
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

          <div class="article-content">
            <MdPreview
              editor-id="article-detail-preview"
              :model-value="article.content || ''"
            />
          </div>
        </article>
      </template>

      <el-empty v-if="!loading && !article" description="文章不存在或已删除" />
    </div>
  </div>
</template>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 20px 60px;
  min-height: 400px;
}

.detail-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.back-btn {
  border: 1px solid #e4e7ed;
  background: #fff;
}

.toolbar-label {
  font-size: 14px;
  color: #909399;
}

.article-panel {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  padding: 40px 48px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
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

.meta-divider {
  color: #dcdfe6;
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

@media (max-width: 768px) {
  .article-panel {
    padding: 24px 20px;
  }

  .article-title {
    font-size: 24px;
  }
}
</style>
