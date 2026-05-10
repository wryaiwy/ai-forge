<script setup lang="ts">
import { ref } from 'vue'
import banner1 from '@/assets/images/banners/banners1.png'
import banner2 from '@/assets/images/banners/banners2.png'
import banner3 from '@/assets/images/banners/banners3.png'

const banners = [
  { id: 1, src: banner1 },
  { id: 2, src: banner2 },
  { id: 3, src: banner3 }
]
</script>

<template>
  <div class="banner-section">
    <el-row :gutter="24" type="flex" align="middle">
      <!-- 左侧文字和按钮 -->
      <el-col :span="12" class="banner-text">
        <h1 class="title">探索知识的边界</h1>
        <p class="subtitle">记录思考，分享知识，构建更好的自己</p>
        <el-button type="primary" size="large">开始阅读</el-button>
      </el-col>

      <!-- 右侧轮播图 -->
      <el-col :span="12">
        <el-carousel :interval="4000" type="card" height="300px">
          <el-carousel-item v-for="item in banners" :key="item.id">
            <img class="banner-img" :src="item.src" alt="banner" />
          </el-carousel-item>
        </el-carousel>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.banner-section {
  padding: 40px 30px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 12px;
  max-width: 1400px;
  margin: 24px auto 0;
}

.banner-text .title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #1f2d3d;
}

.banner-text .subtitle {
  font-size: 16px;
  margin-bottom: 24px;
  color: #4b5c6b;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}

:deep(.el-carousel__arrow) {
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid #e0e6ed;
}

:deep(.el-carousel__arrow .el-icon) {
  color: #409eff;
}

:deep(.el-carousel__arrow:hover) {
  background-color: #ffffff;
}
</style>