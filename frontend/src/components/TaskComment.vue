<template>
  <div class="task-comment">
    <div class="comment-header">
      <h3>任务批注</h3>
      <el-button type="primary" @click="showAddCommentDialog = true">添加批注</el-button>
    </div>
    
    <div class="comment-list">
      <el-card v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-info">
          <span class="comment-author">{{ comment.employeeName }}</span>
          <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        <div v-if="comment.attachmentUrl" class="comment-attachments">
          <h4>附件：</h4>
          <el-link 
            :href="comment.attachmentUrl" 
            target="_blank"
            class="attachment-item"
          >
            {{ getFileNameFromUrl(comment.attachmentUrl) }}
          </el-link>
        </div>
      </el-card>
      <div v-if="comments.length === 0" class="no-comments">
        暂无批注
      </div>
    </div>

    <!-- 添加批注对话框 -->
    <el-dialog
      v-model="showAddCommentDialog"
      title="添加批注"
      width="500px"
    >
      <el-form ref="commentFormRef" :model="commentForm" label-width="80px">
        <el-form-item label="批注内容" prop="content">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入批注内容"
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            class="upload-demo"
            action="/api/rustfs/upload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :file-list="fileList"
            :auto-upload="true"
            multiple
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传多个文件，单个文件大小不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddCommentDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddComment">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTaskComments, addTaskComment } from '@/api/task'

const props = defineProps({
  taskId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['commentAdded'])

const showAddCommentDialog = ref(false)
const commentFormRef = ref(null)
const comments = ref([])
const fileList = ref([])

const commentForm = ref({
  content: '',
  attachmentUrl: '',
  taskId: props.taskId
})

// 获取批注列表
const loadComments = async () => {
  try {
    const data = await getTaskComments(props.taskId)
    comments.value = data || []
  } catch (error) {
    ElMessage.error('获取批注列表失败')
  }
}

const handleAddComment = async () => {
  try {
    commentForm.value.taskId = props.taskId
    await addTaskComment(commentForm.value)
    ElMessage.success('添加批注成功')
    showAddCommentDialog.value = false
    resetCommentForm()
    loadComments()
    emit('commentAdded')
  } catch (error) {
    ElMessage.error('添加批注失败')
  }
}

const handleUploadSuccess = (response, file, fileList) => {
  if (response && response.code === 200 && response.data) {
    commentForm.value.attachmentUrl = response.data
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error('文件上传失败')
  }
}

const handleUploadError = (error, file, fileList) => {
  ElMessage.error('文件上传失败')
}

const resetCommentForm = () => {
  commentForm.value = {
    content: '',
    attachmentUrl: '',
    taskId: props.taskId
  }
  fileList.value = []
  commentFormRef.value?.resetFields()
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString()
}

const getFileNameFromUrl = (url) => {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1]
}

// 组件挂载时加载批注列表
onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.task-comment {
  margin-top: 20px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.comment-header h3 {
  margin: 0;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  margin-bottom: 15px;
}

.comment-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.comment-author {
  font-weight: bold;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-content {
  margin-bottom: 10px;
  line-height: 1.5;
}

.comment-attachments {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e4e7ed;
}

.attachment-item {
  display: block;
  margin: 5px 0;
}

.no-comments {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>