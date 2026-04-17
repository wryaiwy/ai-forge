<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 定义项目名称
const projectName = ref('ai-forger')

// 示例操作函数
const startForging = () => {
  router.push('/login')
}
</script>

<template>
  // TODO 需要完善
  <div class="home-container">
    <header class="page-header">
      <h1 class="app-title">{{ projectName }}</h1>
      <p class="app-subtitle">你的 AI 智能锻造工坊</p>
    </header>

    <main class="page-main">
      <div class="action-bar">
        <button class="el-button el-button--primary" @click="startForging">
          登录
        </button>
      </div>
    </main>
  </div>
</template>

