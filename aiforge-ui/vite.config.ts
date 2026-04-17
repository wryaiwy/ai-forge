import { fileURLToPath, URL } from 'node:url'
import { loadEnv } from 'vite'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// 把 defineConfig 改成接受一个函数的写法，这样才能拿到当前的环境 mode
export default defineConfig(({ mode }) => {
  // 加载对应的环境变量文件（如 .env.development）
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    server: {
      port: 80,
      proxy: {
        // 使用环境变量替换写死的 http://localhost:8080
        '/auth': {
          target: env.VITE_PROXY_TARGET,
          changeOrigin: true
        }
      }
    }
  }
})