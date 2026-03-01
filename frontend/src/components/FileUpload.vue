<template>
  <div class="file-upload-container">
    <el-upload
      class="file-upload"
      :action="action"
      :before-upload="beforeUpload"
      :on-change="handleFileChange"
      :file-list="fileList"
      :auto-upload="false"
      :http-request="handleCustomUpload"
    >
      <slot>
        <el-button type="primary">
          <el-icon><Upload /></el-icon>
          点击上传
        </el-button>
      </slot>
      <template #tip>
        <div class="el-upload__tip">
          支持上传JPG/PNG图片、Word、Excel、PDF、PPT和ZIP文件，单个文件不超过25MB
        </div>
      </template>
    </el-upload>
    
    <!-- 上传进度条 -->
    <div v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress-container">
      <el-progress 
        :percentage="uploadProgress" 
        :status="uploadStatus"
        :stroke-width="10"
        :show-text="true"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, defineExpose } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import request from '@/utils/request'
import SparkMD5 from 'spark-md5'

// 定义props
const props = defineProps({
  action: {
    type: String,
    default: '/file/upload'
  },
  fileList: {
    type: Array,
    default: () => []
  }
})

// 定义emits
const emit = defineEmits(['update:fileList', 'upload-success', 'upload-error', 'upload-progress'])

// 上传进度状态
const uploadProgress = ref(0)
const uploadStatus = ref('')
const currentFile = ref(null)

// 分片上传配置
const CHUNK_SIZE = 1024 * 1024 * 5 // 5MB 分片大小

// 生成文件MD5哈希
const generateFileHash = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    const spark = new SparkMD5.ArrayBuffer()
    const chunkSize = 1024 * 1024 * 2 // 2MB per chunk for hashing
    const chunks = Math.ceil(file.size / chunkSize)
    let currentChunk = 0

    reader.onload = (e) => {
      spark.append(e.target.result)
      currentChunk++

      if (currentChunk < chunks) {
        loadNext()
      } else {
        resolve(spark.end())
      }
    }

    reader.onerror = () => {
      reject(new Error('文件哈希计算失败'))
    }

    const loadNext = () => {
      const start = currentChunk * chunkSize
      const end = Math.min(start + chunkSize, file.size)
      reader.readAsArrayBuffer(file.slice(start, end))
    }

    loadNext()
  })
}

// 文件选择变化处理
const handleFileChange = (file, fileList) => {
  // 当选择文件后，自动开始上传
  if (file.status === 'ready') {
    currentFile.value = file
    uploadStatus.value = ''
    uploadProgress.value = 0
    // 触发自定义上传
    handleCustomUpload({
      file: file.raw
    })
  }
}

// 自定义上传方法
const handleCustomUpload = async (options) => {
  const { file } = options
  try {
    // 生成文件唯一标识
    const fileHash = await generateFileHash(file)
    const fileName = file.name
    const fileSize = file.size
    const chunkCount = Math.ceil(fileSize / CHUNK_SIZE)
    
    // 检查文件是否已存在
    const checkResponse = await request.post('/file/check', {
      fileHash,
      fileName,
      fileSize
    })
    
    if (checkResponse && checkResponse.exists) {
      // 文件已存在，直接返回结果
      const uploadedFile = {
        uid: file.uid || Date.now(),
        name: file.name,
        url: checkResponse.fileUrl,
        previewUrl: checkResponse.previewUrl,
        filePath: checkResponse.filePath,
        type: file.type,
        size: file.size
      }
      
      // 更新文件列表
      const newFileList = [...props.fileList, uploadedFile]
      emit('update:fileList', newFileList)
      emit('upload-success', uploadedFile)
      
      uploadProgress.value = 100
      uploadStatus.value = 'success'
      return
    }
    
    // 分片上传
    const uploadedChunks = []
    for (let i = 0; i < chunkCount; i++) {
      const start = i * CHUNK_SIZE
      const end = Math.min(start + CHUNK_SIZE, fileSize)
      const chunk = file.slice(start, end)
      
      const formData = new FormData()
      formData.append('file', chunk)
      formData.append('fileHash', fileHash)
      formData.append('fileName', fileName)
      formData.append('chunkIndex', i)
      formData.append('chunkCount', chunkCount)
      formData.append('chunkSize', CHUNK_SIZE)
      
      // 上传分片
      await request.post('/file/upload-chunk', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent) => {
          const percent = Math.round((i * CHUNK_SIZE + progressEvent.loaded) / fileSize * 100)
          uploadProgress.value = percent
          emit('upload-progress', percent)
        }
      })
      
      uploadedChunks.push(i)
    }
    
    // 合并分片
    const mergeResponse = await request.post('/file/merge', {
      fileHash,
      fileName,
      chunkCount
    })
    
    // 更新文件列表
    if (mergeResponse) {
      const uploadedFile = {
        uid: file.uid || Date.now(),
        name: file.name,
        url: mergeResponse.fileUrl,
        previewUrl: mergeResponse.previewUrl,
        filePath: mergeResponse.filePath,
        type: file.type,
        size: file.size
      }
      
      // 更新文件列表
      const newFileList = [...props.fileList, uploadedFile]
      emit('update:fileList', newFileList)
      emit('upload-success', uploadedFile)
      
      uploadProgress.value = 100
      uploadStatus.value = 'success'
    }
  } catch (error) {
    console.error('文件上传失败', error)
    uploadStatus.value = 'exception'
    emit('upload-error', error)
  } finally {
    // 3秒后重置进度条
    setTimeout(() => {
      uploadProgress.value = 0
      uploadStatus.value = ''
      currentFile.value = null
    }, 3000)
  }
}

// 上传前检查
const beforeUpload = (file) => {
  // 检查文件类型
  const allowedTypes = [
    'image/jpeg', 'image/png', // 图片
    'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', // Word
    'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // Excel
    'application/pdf', // PDF
    'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation', // PPT
    'application/zip', 'application/x-zip-compressed' // ZIP
  ]
  
  const isAllowedType = allowedTypes.includes(file.type)
  if (!isAllowedType) {
    ElMessage.error('只能上传JPG/PNG图片、Word、Excel、PDF、PPT和ZIP文件')
    return false
  }
  
  // 检查文件大小（限制为25MB）
  const isLt25M = file.size / 1024 / 1024 < 25
  if (!isLt25M) {
    ElMessage.error('文件大小不能超过25MB')
    return false
  }
  
  return true
}

// 暴露方法给父组件
defineExpose({
  handleCustomUpload,
  uploadProgress
})
</script>

<style scoped>
.file-upload {
  margin-bottom: 16px;
}

.upload-progress-container {
  margin-top: 12px;
  margin-bottom: 16px;
}

.file-upload-container {
  width: 100%;
}
</style>