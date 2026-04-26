<script setup lang="ts">
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
  <div class="banner-section mb-20">
    <el-carousel :interval="4000" type="card" height="300px">
      <el-carousel-item v-for="item in banners" :key="item.id">
        <img class="banner-img" :src="item.src" alt="banner" />
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<style scoped>
.banner-section {
  padding-top: 20px;
  padding-bottom: 20px;
}
.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}
</style>
