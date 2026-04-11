# AiForge-UI 前端项目架构文档

## 1. 技术栈概述
本项目基于最新的前端工程化标准搭建，主要技术栈如下：
* **核心框架**：Vue 3 (Composition API / `<script setup>`)
* **开发语言**：TypeScript (强类型约束)
* **构建工具**：Vite (极速热更新与打包)
* **路由管理**：Vue Router 4
* **状态管理**：Pinia
* **UI 组件库**：Element Plus
* **网络请求**：Axios
* **代码规范**：ESLint 8.x + Prettier

## 2. 目录结构说明

项目的核心代码全部集中在 `src` 目录下，按照高内聚、低耦合的原则进行划分：

```text
aiforge-ui/
├── public/                 # 静态资源（不参与构建，如 favicon、外部全局 JS）
├── src/                    # 核心源码目录
│   ├── api/                # 后端接口对接层 (与后端 Controller 一一对应)
│   │   ├── auth.ts         # 例如：登录鉴权接口
│   │   └── user.ts         # 例如：用户管理接口
│   ├── assets/             # 静态资源层 (参与构建，如全局样式、图片、SVG)
│   ├── components/         # 业务公共组件层 (全局复用的基础组件，如自定义按钮、图表封装)
│   ├── layout/             # 页面布局层 (包含顶栏 Header、侧边栏 Sidebar、主区域 AppMain 等)
│   ├── router/             # 路由配置层 (静态路由与动态权限路由的注册)
│   ├── stores/             # 状态管理层 (Pinia store，如 UserStore、AppStore)
│   ├── types/              # TS 类型声明层 (与后端实体类 Entity / DTO 对应的 interface)
│   ├── utils/              # 工具函数层 (如 axios 拦截器 request.ts、日期格式化等)
│   ├── views/              # 视图页面层 (按照业务模块划分的页面)
│   │   ├── login/          # 登录模块
│   │   ├── dashboard/      # 首页看板
│   │   └── system/         # 系统管理等
│   ├── App.vue             # 应用入口组件
│   └── main.ts             # 应用全局入口配置文件
├── .eslintrc.cjs           # ESLint 配置文件
├── index.html              # Vite 宿主 HTML 文件
├── package.json            # 依赖及脚本配置
├── tsconfig.json           # TypeScript 配置文件
└── vite.config.ts          # Vite 构建与代理配置文件