<template>
  <div class="progress-history">
    <h4 style="margin-bottom: 15px;">进展历史</h4>
    <el-timeline>
      <el-timeline-item
        v-for="(update, index) in progressUpdates"
        :key="index"
        :timestamp="formatDateTime(update.createTime)"
        type="primary"
        placement="top"
      >
        <el-card>
          <div class="update-content">
            <div class="update-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
              <span style="font-weight: bold;">{{ update.employeeName }}</span>
              <span style="color: #606266;">进度更新至 {{ update.progress }}%</span>
            </div>
            <div class="update-description" v-if="update.description" style="margin-bottom: 10px;" v-html="update.description"></div>
            <div class="update-attachments" v-if="update.attachmentList && update.attachmentList.length > 0">
              <el-divider content-position="left">附件</el-divider>
              <div class="attachments-container">
                <div v-for="(attachment, idx) in update.attachmentList" :key="idx" class="attachment-item">
                  <div class="attachment-file">
                    <el-icon class="file-icon"><Document /></el-icon>
                    <span class="file-name">{{ attachment.name || '未知文件' }}</span>
                  </div>
                  <div class="attachment-info">
                    <span class="attachment-name">{{ attachment.name || '未知文件' }}</span>
                    <div class="attachment-actions">
                      <el-button size="small" type="primary" @click="handleAttachmentPreview(attachment)">
                        预览
                      </el-button>
                      <el-button size="small" @click="handleAttachmentDownload(attachment)">
                        下载
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
import { Document } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/taskUtils'

const props = defineProps({
  progressUpdates: {
    type: Array,
    default: () => []
  }
})

const handleAttachmentPreview = (attachment) => {
  if (!attachment.url) return
  window.open(attachment.url, '_blank')
}

const handleAttachmentDownload = (attachment) => {
  console.log('下载附件', attachment)
  console.log('原始 URL:', attachment.url)
  console.log('文件名:', attachment.name)
  
  // 将 preview 接口改为 download 接口
  let downloadUrl = attachment.url
  if (downloadUrl.includes('/file/preview')) {
    downloadUrl = downloadUrl.replace('/file/preview', '/file/download')
  }
  
  console.log('替换后 URL:', downloadUrl)
  
  // 如果 URL 中已经有 fileName 参数，先移除它
  if (downloadUrl.includes('fileName=')) {
    const urlObj = new URL(downloadUrl, window.location.origin)
    urlObj.searchParams.delete('fileName')
    downloadUrl = urlObj.toString()
  }
  
  // 添加 fileName 参数
  if (attachment.name) {
    const separator = downloadUrl.includes('?') ? '&' : '?'
    downloadUrl += `${separator}fileName=${encodeURIComponent(attachment.name)}`
  }
  
  console.log('最终下载 URL:', downloadUrl)
  
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = attachment.name || '未知文件'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
</script>

<style scoped>
/* 对话框内容样式，防止横向滚动 */
:deep(.el-dialog__body) {
  overflow-x: hidden !important;
  padding: 20px;
}

/* 更新内容区域样式 */
.update-content {
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 描述内容自适应 */
.update-description {
  width: 100%;
  word-wrap: break-word;
  overflow-wrap: break-word;
  box-sizing: border-box;
}

/* 富文本编辑器内容自适应 */
.update-description :deep(img) {
  max-width: 100%;
  height: auto;
}

.update-description :deep(table) {
  max-width: 100%;
  overflow-x: auto;
  display: block;
}

/* 附件区域自适应 */
.update-attachments {
  width: 100%;
  box-sizing: border-box;
}

/* 附件容器，使用block布局支持垂直排列 */
.attachments-container {
  display: block;
  width: 100%;
  box-sizing: border-box;
}

/* 附件项目样式 */
.attachment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
  margin-bottom: 10px;
}

.attachment-file {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.file-icon {
  color: #409EFF;
}

.file-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.attachment-name {
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.attachment-actions {
  display: flex;
  gap: 8px;
}
</style>