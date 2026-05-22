<script setup lang="ts">
interface Props {
  total: number
  page?: number
  pageSize?: number
  pageSizes?: number[]
  layout?: string
  background?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  page: 1,
  pageSize: 10,
  pageSizes: () => [10, 20, 50],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true
})

const emit = defineEmits<{
  'update:page': [value: number]
  'update:pageSize': [value: number]
  'change': []
}>()

const handleCurrentChange = (val: number) => {
  emit('update:page', val)
  emit('change')
}

const handleSizeChange = (val: number) => {
  emit('update:pageSize', val)
  emit('update:page', 1)
  emit('change')
}
</script>

<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="props.page"
      v-model:page-size="props.pageSize"
      :page-sizes="props.pageSizes"
      :layout="props.layout"
      :total="props.total"
      :background="props.background"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 24px 0;
}

/* 按钮基础样式重置 */
:deep(.el-pagination.is-background .el-pager li) {
  margin: 0 4px;
  background-color: #f4f4f5;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  font-weight: 500;
  min-width: 36px;
  height: 36px;
  line-height: 36px;
}

/* 激活状态 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: var(--el-color-primary, #409eff);
  color: #fff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transform: translateY(-1px);
}

/* 悬浮状态 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: var(--el-color-primary, #409eff);
  background-color: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

/* 上一页/下一页按钮 */
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next) {
  border-radius: 8px;
  background-color: #f4f4f5;
  transition: all 0.3s ease;
  min-width: 36px;
  height: 36px;
  margin: 0 4px;
}

:deep(.el-pagination.is-background .btn-prev:not(:disabled):hover),
:deep(.el-pagination.is-background .btn-next:not(:disabled):hover) {
  background-color: #ecf5ff;
  color: var(--el-color-primary, #409eff);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

/* 下拉框和输入框圆角优化 */
:deep(.el-select .el-input__wrapper),
:deep(.el-pagination__editor.el-input .el-input__wrapper) {
  border-radius: 8px;
}
</style>
