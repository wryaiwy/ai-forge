/** 后端 SysFile 实体对应的类型 */
export interface SysFile {
  fileId: number
  originalName: string
  storageName: string
  storagePath: string
  url: string
  fileSize: number
  mimeType: string
  suffix: string
  uploadUserId: number | null
  createdAt: string
}
