import request from '@/utils/request'
import type { HomeArticleVO, BizArticleDTO } from '@/types/article'
import type { ApiResponse } from '@/types/auth'

/**
 * 获取最新文章列表
 * @param limit 返回的文章数量
 * @returns 最新文章列表
 */
export const getLatestArticlesApi = (limit: number = 6) => {
  return request.get<any, ApiResponse<HomeArticleVO[]>>('/biz/article/latest', {
    params: { limit }
  })
}

/**
 * 添加文章
 * @param data 文章数据
 * @returns 添加结果
 */
export const addArticleApi = (data: BizArticleDTO) => {
  return request.post<any, ApiResponse<void>>('/biz/article/add', data)
}
