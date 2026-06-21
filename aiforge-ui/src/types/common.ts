/**
 * MyBatis-Plus 分页响应
 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
