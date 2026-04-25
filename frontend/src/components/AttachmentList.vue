<template>
  <div class="attachment-list">
    <div v-if="attachments && attachments.length > 0" class="attachments-container">
      <div v-for="(attachment, index) in attachments" :key="index" class="attachment-item">
        <div class="attachment-file">
          <el-icon class="file-icon"><Document /></el-icon>
          <span class="file-name">{{ attachment.name || '未知文件' }}</span>
        </div>
        <div class="attachment-actions">
          <el-button size="small" type="primary" @click="handlePreview(attachment)">
            预览
          </el-button>
          <el-button size="small" @click="handleDownload(attachment)">
            下载
          </el-button>
        </div>
      </div>
    </div>
    <div v-else class="no-data">暂无附件</div>
  </div>
</template>

<script setup>
import { Document } from '@element-plus/icons-vue'

const props = defineProps({
  attachments: {
    type: Array,
    default: () => []
  }
})

const handlePreview = (attachment) => {
  // 强制使用KKFileView预览URL
  if (attachment.previewUrl) {
    window.open(attachment.previewUrl, '_blank')
  } else {
    window.open(attachment.url, '_blank')
  }
}

const handleDownload = (attachment) => {
  // 将 preview 接口改为 download 接口
  let downloadUrl = attachment.url
  if (downloadUrl.includes('/file/preview')) {
    downloadUrl = downloadUrl.replace('/file/preview', '/file/download')
  }
  
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
  
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = attachment.name || '未知文件'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
</script>

<style scoped>
.attachment-list {
  width: 100%;
}

.attachments-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.attachment-file {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.file-icon {
  color: #409eff;
  font-size: 20px;
  flex-shrink: 0;
}

.file-name {
  color: #303133;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.no-data {
  color: #909399;
  text-align: center;
  padding: 20px 0;
}
</style>
