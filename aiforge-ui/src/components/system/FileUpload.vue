<template>
  <el-upload
    ref="uploadRef"
    :action="uploadAction"
    :headers="uploadHeaders"
    :accept="accept"
    :limit="limit"
    :multiple="multiple"
    :drag="drag"
    :show-file-list="showFileList"
    :file-list="fileList"
    :list-type="listType"
    :before-upload="handleBeforeUpload"
    :on-success="handleSuccess"
    :on-error="handleError"
    :on-exceed="handleExceed"
    :on-remove="handleRemove"
    :on-preview="handlePreview"
  >
    <!-- 拖拽模式 -->
    <template v-if="drag">
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">
        将文件拖到此处，或 <em>点击上传</em>
      </div>
    </template>

    <!-- 图片卡片模式 -->
    <template v-else-if="listType === 'picture-card'">
      <el-icon><Plus /></el-icon>
    </template>

    <!-- 默认按钮模式 -->
    <template v-else>
      <el-button type="primary">
        <el-icon class="el-icon--left"><UploadFilled /></el-icon>
        {{ buttonText }}
      </el-button>
    </template>

    <!-- 提示文字 -->
    <template #tip>
      <div v-if="tip" class="el-upload__tip">{{ tip }}</div>
      <div v-else-if="maxSizeMB" class="el-upload__tip">
        单个文件不超过 {{ maxSizeMB }}MB
        <template v-if="accept">，仅支持 {{ accept }} 格式</template>
      </div>
    </template>
  </el-upload>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, type UploadProps, type UploadInstance, type UploadRawFile, type UploadFile, type UploadFiles } from 'element-plus'
import { UploadFilled, Plus } from '@element-plus/icons-vue'
import type { SysFile } from '@/types/file'

export interface FileUploadProps {
  /** 接受的文件类型，如 '.jpg,.png,.pdf' */
  accept?: string
  /** 最大文件数 */
  limit?: number
  /** 是否支持多选 */
  multiple?: boolean
  /** 是否拖拽上传 */
  drag?: boolean
  /** 列表展示类型 */
  listType?: 'text' | 'picture' | 'picture-card'
  /** 按钮文本 */
  buttonText?: string
  /** 单文件最大大小(MB) */
  maxSizeMB?: number
  /** 自定义提示文字 */
  tip?: string
  /** 是否显示文件列表 */
  showFileList?: boolean
}

const props = withDefaults(defineProps<FileUploadProps>(), {
  accept: '',
  limit: 0,
  multiple: false,
  drag: false,
  listType: 'text',
  buttonText: '点击上传',
  maxSizeMB: 50,
  tip: '',
  showFileList: true,
})

const emit = defineEmits<{
  /** 上传成功，返回后端 SysFile 对象 */
  (e: 'success', file: SysFile): void
  /** 文件被移除 */
  (e: 'remove', file: UploadFile): void
}>()

const uploadRef = ref<UploadInstance>()
const fileList = ref<UploadFile[]>([])

/** 上传地址 */
const uploadAction = computed(() => {
  return `${import.meta.env.VITE_PROXY_TARGET}/system/file/upload`
})

/** 请求头带 token */
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('aiforge_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

/** 上传前校验 */
const handleBeforeUpload: UploadProps['beforeUpload'] = (rawFile: UploadRawFile) => {
  // 校验文件大小
  if (props.maxSizeMB && rawFile.size / 1024 / 1024 > props.maxSizeMB) {
    ElMessage.error(`文件大小不能超过 ${props.maxSizeMB}MB`)
    return false
  }
  return true
}

/** 上传成功回调 */
const handleSuccess: UploadProps['onSuccess'] = (response: any, _uploadFile: UploadFile, _uploadFiles: UploadFiles) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    emit('success', response.data as SysFile)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

/** 上传失败回调 */
const handleError: UploadProps['onError'] = () => {
  ElMessage.error('上传失败，请重试')
}

/** 超出文件数限制 */
const handleExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`)
}

/** 移除文件 */
const handleRemove: UploadProps['onRemove'] = (file: UploadFile) => {
  emit('remove', file)
}

/** 预览文件（图片模式下点击放大） */
const handlePreview: UploadProps['onPreview'] = (file: UploadFile) => {
  if (file.url) {
    window.open(file.url)
  }
}

/** 暴露清空方法给父组件 */
const clearFiles = () => {
  uploadRef.value?.clearFiles()
}

defineExpose({ clearFiles })
</script>
