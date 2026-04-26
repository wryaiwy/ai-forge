<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth' // 引入我们之前写的 Pinia
import { Search, User } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const searchQuery = ref('')
const activeMenu = ref('/')

// 判断是否登录（有 token 视为已登录）
const isLoggedIn = computed(() => !!authStore.token)

const handleLogin = () => {
  router.push('/login')
}

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    authStore.clearToken()
    router.push('/')
  }
}
</script>

<template>
  <header class="global-header">
    <el-row class="container flex-between" type="flex" align="middle" :gutter="20">

      <el-col :span="4" class="logo-area">
        <router-link to="/" class="logo-link">
          <img src="@/assets/images/logo.png" alt="Logo" class="logo-img">
        </router-link>
      </el-col>

      <el-col :span="12">
        <el-menu :default-active="activeMenu" mode="horizontal" router :ellipsis="false" class="nav-menu">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/articles">博客文章</el-menu-item>
          <el-menu-item index="/baike">知识百科</el-menu-item>
          <el-menu-item index="/datasets">数据集</el-menu-item>
          <el-menu-item index="/news">热点新闻</el-menu-item>
        </el-menu>
      </el-col>

      <el-col :span="6" class="action-area">
        <div class="action-container">
          <el-input
            v-model="searchQuery"
            placeholder="搜索全站资源..."
            :prefix-icon="Search"
            class="search-input"
          />

          <el-button v-if="!isLoggedIn" type="primary" @click="handleLogin" class="login-btn">
            登录
          </el-button>

          <el-dropdown v-else @command="handleCommand">
            <div class="my-profile-trigger">
              <el-icon><User/></el-icon>
            </div>

            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-col>

    </el-row>
  </header>
</template>

<style scoped>
.global-header {
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

/* ==================================
   局部布局样式
   ================================== */

/* --- 1. 左侧 Logo 区 --- */
.logo-area {
  margin-left: 50px;
}

.logo-link {
  display: flex;
  align-items: center; /* 确保 a 标签内部也能垂直居中 */
}

.logo-img {
  width: 50px;
  height: 50px;
  object-fit: contain;
  border-radius: 50%;
  display: block;
}

/* --- 2. 中间导航区 --- */
.nav-menu {
  border-bottom: none; /* 去除 Element 默认下边框 */
}

/* --- 3. 右侧操作区 --- */
.action-container {
  display: flex;
  justify-content: flex-end; /* 整体靠右 */
  align-items: center;       /* 内部所有元素垂直居中 */
  gap: 20px;                 /* 统一设定元素之间的间距，代替 margin-left/right */
  width: 100%;
}

.search-input {
  width: 250px;
}

.login-btn {
  border-radius: 999px;
}

.my-profile-trigger {
  width: 36px;       /* 控制圆形的宽度 */
  height: 36px;      /* 控制圆形的高度，保持宽高一致 */
  border-radius: 50%; /* 1. 实现完美的圆形 */
  border: 1px solid #dcdfe6; /* 保持与默认按钮类似的边框 */
  background-color: #ffffff;
  color: #606266;    /* 图标默认颜色 */

  /* 使用 Flex 让内部图标居中 */
  display: flex;
  justify-content: center;
  align-items: center;

  cursor: pointer;   /* 鼠标放上去显示小手 */
  outline: none;     /* 去除点击时可能出现的焦点外框 */
}
</style>
