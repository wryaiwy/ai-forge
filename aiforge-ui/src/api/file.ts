import request from '@/utils/request'
import type { SysFile } from '@/types/file'
import type { ApiResponse } from '@/types/auth'

/**
 * 文件上传
 * @param file 文件对象
 */
export const uploadFileApi = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, ApiResponse<SysFile>>('/system/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/**
 * 获取文件下载URL（拼接完整路径）
 * @param fileId 文件ID
 */
export const getFileDownloadUrl = (fileId: number) => {
  return `${import.meta.env.VITE_PROXY_TARGET}/system/file/download/${fileId}`
}
