<script setup lang="ts">
import { ref } from 'vue'
import { Edit, Document, Opportunity, View, Star, Plus, ArrowRight} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import GlobalHeader from '@/layout/components/GlobalHeader.vue'

// 1. 获取 router 实例
const router = useRouter()

// 定义用户数据类型
interface UserProfile {
  avatar: string
  nickname: string
  bio: string
  following: number
  followers: number
  likes: number
  views: number
}

// 模拟用户数据
const userProfile = ref<UserProfile>({
  avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
  nickname: '小蜜蜂',
  bio: 'Java全栈工程师 ｜ 热爱探索新技术 ｜ 专注于 Spring Boot 与 Vue3',
  following: 128,
  followers: 1024,
  likes: 8848,
  views: 56700
})

// 控制激活的 Tab
const activeTab = ref('home')
const activeSubmissionTab = ref('articles')

// 格式化数字工具函数 (例如 10000 -> 1w)
const formatNumber = (num: number) => {
  return num >= 10000 ? (num / 10000).toFixed(1) + 'w' : num.toString()
}

// 投稿按钮点击事件
const handlePublish = () => {
  // 这里后续可以结合 vue-router 跳转到发布页，或者打开一个弹窗
  publishDialogVisible.value = true
}

// 控制弹窗的显示与隐藏
const publishDialogVisible = ref(false)

// 点击具体卡片时触发
const goToPublish = (type: string) => {
  publishDialogVisible.value = false // 关闭弹窗
  if (type === 'article') {
    // 使用 vue-router 跳转到刚才配置的文章发布页
    router.push({ name: 'PublishArticle' })
    // 或者用 path 也可以: router.push('/publish/article')

  } else if (type === 'dataset') {
    console.log('跳转到数据集发布路由')
    // router.push({ name: 'PublishDataset' })
  }
}
</script>

<template>
  <div class="personal-center-container">
    <GlobalHeader />
    <!-- 限制最大宽度的页面包裹层 -->
    <div class="page-wrapper">

      <!-- 1. 顶部个人信息卡片 -->
      <div class="profile-header-card">
        <!-- 封面图 -->
        <div class="banner"></div>

        <!-- 信息区：使用 24 分栏布局 -->
        <el-row class="profile-info-section" :gutter="20">
          <!-- 左侧：头像与基本信息 (占 14 栏) -->
          <el-col :span="14" class="main-info-col">
            <div class="avatar-wrapper">
              <el-avatar :size="100" :src="userProfile.avatar" class="user-avatar" />
            </div>
            <div class="user-details">
              <h2 class="nickname">{{ userProfile.nickname }}</h2>
              <p class="bio">{{ userProfile.bio }}</p>
            </div>
          </el-col>

          <!-- 右侧：数据统计与操作 (占 10 栏) -->
          <el-col :span="10" class="side-info-col">
            <div class="stats-container">
              <div class="stat-item">
                <span class="stat-value">{{ formatNumber(userProfile.following) }}</span>
                <span class="stat-label">关注</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ formatNumber(userProfile.followers) }}</span>
                <span class="stat-label">粉丝</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ formatNumber(userProfile.likes) }}</span>
                <span class="stat-label">获赞</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ formatNumber(userProfile.views) }}</span>
                <span class="stat-label">播放/阅读</span>
              </div>
            </div>

            <!-- 新增：我要投稿 按钮组合 -->
            <div class="action-buttons">
              <el-button type="primary" round :icon="Plus" @click="handlePublish">我要投稿</el-button>
              <el-button plain round :icon="Edit">编辑资料</el-button>
            </div>

          </el-col>
        </el-row>
      </div>

      <!-- 2. 下方内容区域：使用 24 分栏布局 -->
      <el-row :gutter="16" class="content-row">
        <!-- 左侧主内容区 (占 17 栏) -->
        <el-col :span="17">
          <el-card class="main-content-card" shadow="never">
            <el-tabs v-model="activeTab" class="custom-tabs">
              <!-- 主页 -->
              <el-tab-pane label="主页" name="home">
                <el-empty description="这是主页，可以展示个人置顶内容或成就墙" />
              </el-tab-pane>

              <!-- 动态 -->
              <el-tab-pane label="动态" name="dynamics">
                <el-empty description="暂无最新动态" />
              </el-tab-pane>

              <!-- 投稿 (嵌套 Tabs) -->
              <el-tab-pane label="投稿" name="submissions">
                <el-tabs v-model="activeSubmissionTab" type="border-card" class="submission-tabs">
                  <el-tab-pane name="articles">
                    <template #label>
                      <span class="custom-tab-label">
                        <el-icon><Document /></el-icon>
                        <span>文章</span>
                      </span>
                    </template>
                    <div class="mock-list">
                      <div class="mock-item" v-for="i in 3" :key="i">
                        <h4>深入理解 MyBatis-Plus 在企业级架构中的应用</h4>
                        <p class="meta">发布于 2026-04-12 · 1284 阅读 · 45 点赞</p>
                      </div>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane name="datasets">
                    <template #label>
                      <span class="custom-tab-label">
                        <el-icon><Opportunity /></el-icon>
                        <span>数据集</span>
                      </span>
                    </template>
                    <div class="mock-list">
                      <div class="mock-item">
                        <h4>电商业务用户行为分析样本数据集</h4>
                        <p class="meta">包含 10w+ 条脱敏用户交互记录，适用于推荐系统训练。</p>
                      </div>
                    </div>
                  </el-tab-pane>
                </el-tabs>
              </el-tab-pane>

              <!-- 收藏 -->
              <el-tab-pane label="收藏" name="collections">
                <template #label>
                  <span class="custom-tab-label">
                    <el-icon><Star /></el-icon>
                    <span>收藏夹</span>
                  </span>
                </template>
                <el-empty description="你的收藏夹空空如也" />
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-col>

        <!-- 右侧侧边栏辅助区 (占 7 栏) -->
        <el-col :span="7">
          <el-card class="sidebar-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>个人成就</span>
              </div>
            </template>
            <div class="achievement-list">
              <div class="achievement-item">
                <el-icon class="icon-blue"><View /></el-icon>
                <span>累计获得 {{ formatNumber(userProfile.views) }} 次阅读</span>
              </div>
              <div class="achievement-item">
                <el-icon class="icon-orange"><Star /></el-icon>
                <span>获得 {{ formatNumber(userProfile.likes) }} 次点赞</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

    </div>
  </div>

  <el-dialog
    v-model="publishDialogVisible"
    title="请选择你要创作的内容"
    width="600px"
    center
    destroy-on-close
    class="custom-publish-dialog"
  >
    <div class="publish-type-container">
      <!-- 文章卡片 -->
      <div class="publish-card" @click="goToPublish('article')">
        <div class="card-icon-wrapper blue">
          <el-icon><Document /></el-icon>
        </div>
        <div class="card-content">
          <h3>博客文章</h3>
          <p>分享你的技术见解、项目复盘或学习笔记，支持 Markdown 编辑。</p>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

<!--      &lt;!&ndash; 数据集卡片 &ndash;&gt;-->
<!--      <div class="publish-card" @click="goToPublish('dataset')">-->
<!--        <div class="card-icon-wrapper orange">-->
<!--          <el-icon><Opportunity /></el-icon>-->
<!--        </div>-->
<!--        <div class="card-content">-->
<!--          <h3>数据集</h3>-->
<!--          <p>上传结构化或非结构化数据，为 AI 模型训练与开源社区贡献力量。</p>-->
<!--        </div>-->
<!--        <div class="card-arrow">-->
<!--          <el-icon><ArrowRight /></el-icon>-->
<!--        </div>-->
<!--      </div>-->
    </div>
  </el-dialog>
</template>

<style scoped>
/* 整个页面的外层背景 */
.personal-center-container {
  min-height: calc(100vh - 60px);
  background-color: #f4f5f7;
  padding-bottom: 40px;
}

.page-wrapper {
  max-width: 1000px;
  margin: 0 auto;
}

/* ================= 1. 顶部卡片 ================= */
.profile-header-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(18, 18, 18, 0.1);
  overflow: hidden;
  margin-bottom: 16px;
}

.banner {
  height: 200px;
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
}

.profile-info-section {
  padding: 0 30px 24px;
}

.main-info-col {
  position: relative;
}

.avatar-wrapper {
  position: absolute;
  top: -50px;
  padding: 4px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  z-index: 10;
}

.user-details {
  margin-top: 60px;
}

.nickname {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #222;
}

.bio {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.side-info-col {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: flex-end;
}

.stats-container {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
  margin-top: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #222;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

/* 新增：按钮组的排列与间距 */
.action-buttons {
  margin-bottom: 4px;
  display: flex;
  gap: 12px; /* 两个按钮之间的间距 */
}

/* ================= 2. 下方内容区 ================= */
.content-row {
  align-items: flex-start;
}

.main-content-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 3px rgba(18, 18, 18, 0.1);
  min-height: 500px;
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #f0f0f0;
}

.custom-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 24px;
  height: 50px;
  line-height: 50px;
}

.custom-tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.submission-tabs {
  margin-top: 10px;
  border-radius: 4px;
  box-shadow: none;
}

.mock-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mock-item {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  transition: background-color 0.2s;
  cursor: pointer;
}

.mock-item:hover {
  background-color: #fafafa;
}

.mock-item h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.mock-item .meta {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

/* ================= 3. 侧边栏 ================= */
.sidebar-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 3px rgba(18, 18, 18, 0.1);
}

.card-header span {
  font-weight: 600;
  color: #222;
}

.achievement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.achievement-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.icon-blue {
  color: #409eff;
  font-size: 18px;
}

.icon-orange {
  color: #e6a23c;
  font-size: 18px;
}

/* ================= 投稿弹窗专属样式 ================= */
.publish-type-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 10px 20px 20px;
}

.publish-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background-color: #f8f9fa;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

/* 鼠标悬浮时的动态效果：背景变白、加阴影、边框高亮、整体上浮 */
.publish-card:hover {
  background-color: #ffffff;
  border-color: #c6e2ff;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.12);
  transform: translateY(-2px);
}

.publish-card:hover .card-arrow {
  color: #409eff;
  transform: translateX(4px); /* 箭头向右微动，引导点击 */
}

/* 左侧图标区域 */
.card-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  flex-shrink: 0;
}

.card-icon-wrapper.blue {
  background-color: #ecf5ff;
  color: #409eff;
}

.card-icon-wrapper.orange {
  background-color: #fdf6ec;
  color: #e6a23c;
}

/* 中间文本区域 */
.card-content {
  flex: 1;
  margin-left: 20px;
}

.card-content h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.card-content p {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

/* 右侧箭头 */
.card-arrow {
  font-size: 20px;
  color: #dcdfe6;
  transition: all 0.3s ease;
}

/* 微调 Element Plus 对话框的圆角以匹配整体风格 */
:deep(.custom-publish-dialog) {
  border-radius: 12px;
}
:deep(.el-dialog__title) {
  font-weight: bold;
}
</style>
