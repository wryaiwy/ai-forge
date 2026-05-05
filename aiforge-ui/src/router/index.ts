import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 首页
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    // 关于
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/home/AboutView.vue'),
    },
    // 登录
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/login/login.vue'),
    },
    // 个人中心
    {
      path: '/personalCenter',
      name: 'personalCenter',
      component: () => import('../views/personalCenter/index.vue'),
    },
    // 文章发布页
    {
      path: '/publish/article',
      name: 'PublishArticle',
      component: () => import('../views/publish/article/index.vue'),
    }
  ],
})
export default router
