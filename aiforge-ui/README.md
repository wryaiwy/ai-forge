# AiForge-UI 前端项目架构文档

## 1. 技术栈概述
本项目基于企业级前端标准搭建，采用前后端分离架构，核心技术栈如下：
* **核心框架**：Vue 3 (完全使用 `<script setup>` 组合式 API)
* **开发语言**：TypeScript (开启严格模式)
* **构建工具**：Vite (提供极速热更新与打包)
* **路由管理**：Vue Router 4
* **状态管理**：Pinia (取代传统的 Vuex)
* **UI 组件库**：Element Plus (严格基于 24 分栏的栅格系统布局)
* **网络请求**：Axios (配合 Vite Proxy 解决跨域)
* **代码规范**：ESLint 8.x + Prettier

## 2. 核心目录结构
项目遵循高内聚、低耦合的原则，全栈代码和 AI 配置文件组织如下：

```text
aiforge-ui/
├── .agents/                # AI 专属工作流与规则目录 (Antigravity 专用)
│   ├── rules/              # 全局被动规则 (如强制代码风格)
│   └── workflows/          # 主动生成工作流 (如 /new-page)
├── public/                 # 静态资源（不参与 Vite 构建，如 favicon.ico）
├── src/                    # 核心源码目录
│   ├── api/                # 后端接口对接层 (与后端 Controller 严格对应)
│   ├── assets/             # 静态资源层 (参与构建，如局部样式、图片)
│   ├── components/         # 业务公共组件层 (全局复用的 UI 封装)
│   ├── layout/             # 页面布局层 (包含 Header、Sidebar、AppMain 等)
│   ├── router/             # 路由配置层 (动态与静态路由注册)
│   ├── stores/             # 状态管理层 (Pinia 全局共享状态，如 UserStore)
│   ├── types/              # TS 类型声明层 (与后端 Entity / DTO 对应的 interface)
│   ├── utils/              # 工具函数层 (如 axios 拦截器 request.ts、时间处理等)
│   ├── views/              # 视图页面层 (按业务模块划分)
│   │   ├── auth/           # 认证模块 (登录/注册)
│   │   └── dashboard/      # 首页看板模块
│   ├── App.vue             # 应用根组件入口
│   └── main.ts             # 应用全局配置文件 (挂载插件、路由、状态)
├── .env.development        # 本地开发环境变量配置 (如代理 Target 配置)
├── .env.production         # 生产环境变量配置
├── .eslintrc.cjs           # ESLint 8.x 配置文件
├── index.html              # Vite 宿主 HTML 文件
├── package.json            # 依赖清单与项目脚本
├── tsconfig.json           # TypeScript 编译与路径别名配置
└── vite.config.ts          # Vite 构建配置 (含跨域 Proxy 与动态环境变量加载)