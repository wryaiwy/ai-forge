<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLatestArticlesApi } from '@/api/article'
import type { HomeArticleVO } from '@/types/article'
import OtherSvg from '@/assets/images/svg/other.svg'
import HomeAvatarSvg from '@/assets/images/svg/home/home_avatar.svg'
import HomeCalendarSvg from '@/assets/images/svg/home/home_calendar.svg'
import HomeViewDetailsSvg from '@/assets/images/svg/home/home_view_details.svg'

const articles = ref<HomeArticleVO[]>([])
const loading = ref(false)

const formatTime = (time: string) => {
  if (!time) return ''
  return time.slice(0, 10)
}

const parseTags = (tags: string) => {
  if (!tags) return []
  return tags
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean)
}

const handleViewDetails = () => {
  ElMessage.info('敬请期待')
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
        <el-card shadow="hover" class="article-card hover-shadow">
          <div class="article-card-inner">
            <div class="article-icon">
              <OtherSvg />
            </div>

            <div class="article-content">
              <el-tooltip :content="item.articleTitle" placement="top" :show-after="300">
                <h3 class="article-title">{{ item.articleTitle }}</h3>
              </el-tooltip>
              <div class="article-tags">
                <el-tag
                  v-for="tag in parseTags(item.articleTags)"
                  :key="tag"
                  size="small"
                  type="info"
                  effect="plain"
                  class="tag-item"
                  >{{ tag }}</el-tag
                >
              </div>
            </div>
          </div>

          <div class="article-meta">
            <span class="meta-author"><HomeAvatarSvg />{{ item.authorName || '佚名' }}</span>
            <span class="meta-time"><HomeCalendarSvg />{{ formatTime(item.publishTime) }}</span>
            <span class="meta-view-details" @click.stop="handleViewDetails">
              <HomeViewDetailsSvg />
            </span>
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
  border-radius: 12px;
  border: 1px solid var(--color-border);
  transition: box-shadow 0.3s ease;
  overflow: hidden;
}

.article-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.article-card-inner {
  display: flex;
  gap: 16px;
  padding: 20px;
}

.article-icon :deep(svg) {
  width: 32px;
  height: 32px;
}

.article-icon {
  width: 35px; /* 图标容器宽度，与高度保持一致为正方形 */
  height: 35px; /* 图标容器高度，与宽度保持一致为正方形 */
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 28px;
}

.article-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.article-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.tag-item {
  border-radius: 4px;
}

.article-meta {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.7;
}

.meta-author {
  flex: 1;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.meta-time {
  flex: 1;
  text-align: center;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.meta-author :deep(svg),
.meta-time :deep(svg) {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.meta-view-details {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  cursor: pointer;
  transition: opacity 0.3s ease;
}

.meta-view-details:hover {
  opacity: 0.6;
}
</style>
