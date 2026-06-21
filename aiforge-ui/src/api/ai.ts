import request from '@/utils/request'
import type { AiPolishDTO, AiPolishVO, AiTranslateDTO, AiTranslateVO, AiChatContextAddDTO } from '@/types/ai'
import type { ApiResponse } from '@/types/auth'

// 文章润色
export const polishArticleApi = (data: AiPolishDTO) => {
  return request.post<any, ApiResponse<AiPolishVO>>('/ai/polish', data)
}

// 文章翻译
export const translateArticleApi = (data: AiTranslateDTO) => {
  return request.post<any, ApiResponse<AiTranslateVO>>('/ai/translate', data)
}

// 首页 AI 对话 (流式)
export const homePageChatStreamApi = (data: AiChatContextAddDTO) => {
  const token = localStorage.getItem('aiforge_token')
  const baseURL = import.meta.env.VITE_PROXY_TARGET || ''
  
  return fetch(`${baseURL}/ai/chat-context/home-page-chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(data)
  })
}
