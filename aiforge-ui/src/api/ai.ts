import request from '@/utils/request'
import type { AiPolishDTO, AiPolishVO, AiTranslateDTO, AiTranslateVO, AiHomePageChatContextAddDTO, AiHomePageChatListVO, AiChatContextVO } from '@/types/ai'
import type { ApiResponse } from '@/types/auth'
import type { PageResult } from '@/types/common'

// 文章润色
export const polishArticleApi = (data: AiPolishDTO) => {
  return request.post<any, ApiResponse<AiPolishVO>>('/ai/polish', data)
}

// 文章翻译
export const translateArticleApi = (data: AiTranslateDTO) => {
  return request.post<any, ApiResponse<AiTranslateVO>>('/ai/translate', data)
}

// 首页 AI 对话助手 (流式)
export const homePageChatStreamApi = (data: AiHomePageChatContextAddDTO) => {
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

// 首页 AI 对话助手 消息列表
export const getHomePageChatListApi = (params: { pageNum?: number; pageSize?: number }) => {
  return request.get<any, ApiResponse<PageResult<AiHomePageChatListVO>>>('/ai/chat-context/home-page-chat/list', { params })
}

// 首页 AI 对话助手 消息详情
export const getChatContextDetailApi = (params: { chatContextId: number }) => {
  return request.get<any, ApiResponse<AiChatContextVO[]>>('/ai/chat-context/home-page-chat/detail', { params })
}
