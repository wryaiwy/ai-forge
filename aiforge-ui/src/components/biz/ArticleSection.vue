<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getLatestArticlesApi } from '@/api/article'
import type { HomeArticleVO } from '@/types/article'

const articles = ref<HomeArticleVO[]>([])
const loading = ref(false)

const formatTime = (time: string) => {
  if (!time) return ''
  return time.slice(0, 10)
}

const parseTags = (tags: string) => {
  if (!tags) return []
  return tags.split(',').map(t => t.trim()).filter(Boolean)
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getLatestArticlesApi(6)
    articles.value = res.data || []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="article-section">
    <el-empty v-if="!loading && articles.length === 0" description="暂无文章" />
    <el-row :gutter="24" v-else>
      <el-col :xs="24" :sm="12" :md="8" v-for="item in articles" :key="item.articleId">
        <el-card shadow="hover" class="article-card hover-shadow" body-style="padding: 20px;">
          <h3 class="article-title">{{ item.articleTitle }}</h3>
          <div class="article-tags">
            <el-tag
              v-for="tag in parseTags(item.articleTags)"
              :key="tag"
              size="small"
              type="info"
              effect="plain"
              class="tag-item"
            >{{ tag }}</el-tag>
          </div>
          <div class="article-meta">
            <span class="meta-author">{{ item.authorName || '佚名' }}</span>
            <span class="meta-time">{{ formatTime(item.publishTime) }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.article-section {
  min-height: 200px;
}

.article-card {
  margin-bottom: 24px;
  cursor: pointer;
  border-radius: 8px;
  border: 1px solid var(--color-border);
}

.article-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-tags {
  margin-bottom: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-item {
  border-radius: 4px;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.6;
}
</style>
