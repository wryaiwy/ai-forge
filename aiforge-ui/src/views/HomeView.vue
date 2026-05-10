<script setup lang="ts">
// 引入全局 Header (如果是配在 router 的 Layout 里也可以，这里作为示例直接引入)
import GlobalHeader from '@/layout/components/GlobalHeader.vue'

// 引入首页切片组件
import HomeBanner from './home/components/HomeBanner.vue'
// 以下三个组件你可以仿照 Banner 自己建立空的 .vue 文件进行占位
import ArticleSection from "@/components/biz/ArticleSection.vue";
import DatasetSection from '@/components/biz/DatasetSection.vue'
import NewsSection from '@/components/biz/NewsSection.vue'
import HomeArticleIcon from '@/assets/images/svg/home/home_article.svg?component'
</script>

<template>
  <div class="page-wrapper">
    <GlobalHeader />

    <main class="container">
      <HomeBanner />

      <section class="blog-section">
        <h2 class="blog-title">
          <span style="color: #409eff">
            <HomeArticleIcon />
          </span>
          博客文章
        </h2>
        <ArticleSection />
      </section>

<!--      <section class="section-padding">-->
<!--        <h2 class="section-title">数据集</h2>-->
<!--        <DatasetSection />-->
<!--      </section>-->

<!--      <section class="section-padding">-->
<!--        <h2 class="section-title">热点新闻</h2>-->
<!--        <NewsSection />-->
<!--      </section>-->
    </main>

<!--    <footer class="flex-center section-padding" style="background: #f5f7fa; margin-top: 40px;">-->
<!--      <p style="color: #909399;">© 2026 AiForge Platform. All rights reserved.</p>-->
<!--    </footer>-->
  </div>
</template>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background-color: #fcfcfc;
}

.blog-section {
  max-width: 1400px;
  margin: 25px auto 40px;
  border: 1px solid transparent;
  border-radius: 12px;
  background-color: transparent;
  box-shadow: none;
}

.blog-title {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  margin-bottom: 20px;

  span {
    display: inline-flex;
    align-items: center;

    svg {
      height: 1em;
      width: auto;
    }
  }
}

</style>
