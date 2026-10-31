import request from '@/utils/request'
import type { AiPolishDTO, AiPolishVO, AiTranslateDTO, AiTranslateVO, AiHomePageChatContextAddDTO, AiHomePageChatListVO, AiChatContextVO } from '@/types/ai'
import type { ApiResponse } from '@/types/auth'
import type { PageResult } from '@/types/common'

// 文章润色 (流式)
export const polishArticleApi = (data: AiPolishDTO) => {
  const token = localStorage.getItem('aiforge_token')
  const baseURL = import.meta.env.VITE_PROXY_TARGET || ''

  return fetch(`${baseURL}/ai/polish`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(data)
  })
}

// 文章翻译 (流式)
export const translateArticleApi = (data: AiTranslateDTO) => {
  const token = localStorage.getItem('aiforge_token')
  const baseURL = import.meta.env.VITE_PROXY_TARGET || ''

  return fetch(`${baseURL}/ai/translate`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(data)
  })
}

// 首页 AI 对话助手 (流式)
export const homePageChatGraphStreamApi = (data: AiHomePageChatContextAddDTO) => {
  const token = localStorage.getItem('aiforge_token')
  const baseURL = import.meta.env.VITE_PROXY_TARGET || ''

  return fetch(`${baseURL}/ai/chat-context/home-page-chat-graph`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(data)
  })
}

// AI 写作助手 (流式)
export const writingChatStreamApi = (data: { sessionId: string; content: string; agentType?: string }) => {
  const token = localStorage.getItem('aiforge_token')
  const baseURL = import.meta.env.VITE_PROXY_TARGET || ''

  return fetch(`${baseURL}/ai/writing/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(data)
  })
}

// 首页 AI 对话助手 消息列表
export const getHomePageChatListApi = (params: { pageNum?: number; pageSize?: number; agentType: string }) => {
  return request.get<any, ApiResponse<PageResult<AiHomePageChatListVO>>>('/ai/chat-context/home-page-chat/list', { params })
}

// 首页 AI 对话助手 消息详情
export const getChatContextDetailApi = (params: { chatContextId: number }) => {
  return request.get<any, ApiResponse<AiChatContextVO[]>>('/ai/chat-context/home-page-chat/detail', { params })
}
