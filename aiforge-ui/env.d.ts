/// <reference types="vite/client" />

// 定义 SVG 组件的类型
declare module '*.svg?component' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>
  export default component
}
