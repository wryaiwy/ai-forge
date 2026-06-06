import request from '@/utils/request'

export const searchApi = {
  // 全站跨域搜索
  globalSearch: (keyword: string) => {
    return request.get('/search/global', { params: { keyword } })
  }
}
