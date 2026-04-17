<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
/* 这里放置全局基础样式，重置默认的内外边距，确保充满屏幕 */
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa; /* Element 风格的浅灰色背景，让卡片更立体 */
}

#app {
  width: 100%;
  height: 100%;
}
</style>