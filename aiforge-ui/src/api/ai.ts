import request from '@/utils/request'
import type { AiPolishDTO, AiPolishVO } from '@/types/ai'
import type { ApiResponse } from '@/types/auth'

export const polishArticleApi = (data: AiPolishDTO) => {
  return request.post<any, ApiResponse<AiPolishVO>>('/ai/polish', data)
}
