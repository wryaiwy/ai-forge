import request from '@/utils/request'
import type { AiPolishDTO, AiPolishVO, AiTranslateDTO, AiTranslateVO } from '@/types/ai'
import type { ApiResponse } from '@/types/auth'

export const polishArticleApi = (data: AiPolishDTO) => {
  return request.post<any, ApiResponse<AiPolishVO>>('/ai/polish', data)
}

export const translateArticleApi = (data: AiTranslateDTO) => {
  return request.post<any, ApiResponse<AiTranslateVO>>('/ai/translate', data)
}
