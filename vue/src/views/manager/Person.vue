<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from "@/utils/request";

// ===== 图片地址处理 =====
const BASE = import.meta.env.DEV
    ? '/api'
    : (import.meta.env.VITE_BASE_URL || '/api')

const PLACEHOLDER =
    'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect fill="%23FFF3E0" width="300" height="300"/%3E%3Ctext x="50%25" y="50%25" font-size="72" text-anchor="middle" dy=".35em"%3E%F0%9F%8D%AC%3C/text%3E%3C/svg%3E'

const formatImg = (url) => {
  if (!url) return PLACEHOLDER
  if (/^https?:\/\//i.test(url)) {
    return url.replace(/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i, BASE)
  }
  return BASE + (url.startsWith('/') ? url : '/' + url)
}

// ===== 上传地址：开发走 Vite proxy 的 /api =====
const uploadUrl = import.meta.env.DEV
    ? '/api/files/upload'
    : ((import.meta.env.VITE_BASE_URL || '') + '/files/upload')

const emit = defineEmits(['updateUser'])

const formRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' }
    ],
    name: [
      { required: true, message: '请输入姓名', trigger: 'blur' }
    ]
  }
})

const loadUser = () => {
  request.get(`/admin/selectById/` + data.user.id).then(res => {
    Object.assign(data.user, res.data)
    // 存储最新用户信息
    localStorage.setItem('system-user', JSON.stringify(res.data))
    emit('updateUser')
  })
}
loadUser()

const handleFileUpload = (res) => {
  if (!res || res.code !== '200') {
    ElMessage.error(res?.msg || '上传失败')
    return
  }
  // 先把头像路径更新到当前用户对象
  data.user.avatar = res.data
  // 走统一的 request，baseURL 已处理，不会请求 localhost
  request.put('/admin/update', data.user).then(r => {
    if (r.code === '200') {
      ElMessage.success('头像更新成功')
      loadUser()
    } else {
      ElMessage.error(r.msg)
    }
  }).catch(() => {
    ElMessage.error('头像更新失败')
  })
}

const handleUploadError = () => {
  ElMessage.error('上传失败，请稍后重试')
}

const update = () => {
  formRef.value.validate(valid => {
    if (!valid) return
    request.put('/admin/update', data.user).then(res => {
      if (res.code === '200') {
        ElMessage.success('更新成功')
        loadUser()
      } else {
        ElMessage.error(res.msg)
      }
    })
  })
}
</script>

<template>
  <div class="page">
    <div class="card form-card">
      <div class="form-card__head">
        <div>
          <div class="form-card__title">个人信息</div>
          <div class="form-card__desc">修改头像与名称后记得点击「保存」</div>
        </div>
        <el-tag effect="plain">{{ data.user.role || '管理员' }}</el-tag>
      </div>

      <el-divider />

      <el-form
          ref="formRef"
          :model="data.user"
          :rules="data.rules"
          label-width="90px"
          class="form-card__body"
      >
        <el-form-item prop="avatar" label="头像">
          <div class="avatar-field">
            <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :show-file-list="false"
                :on-success="handleFileUpload"
                :on-error="handleUploadError"
                accept="image/*"
            >
              <img
                  v-if="data.user.avatar"
                  :src="formatImg(data.user.avatar)"
                  class="avatar-preview"
                  alt=""
                  @error="(e) => (e.target.src = PLACEHOLDER)"
              >
              <i v-else class="avatar-uploader-icon">
                <el-icon><Plus /></el-icon>
              </i>
            </el-upload>
            <div class="avatar-field__tip">
              <div>点击左侧头像上传新图片</div>
              <div class="avatar-field__sub">支持 jpg / png，建议 200×200</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item prop="username" label="账号">
          <el-input
              :disabled="!!data.user.id"
              v-model="data.user.username"
              placeholder="请输入账号" auto-complete="false"/>
        </el-form-item>

        <el-form-item prop="name" label="姓名">
          <el-input
              v-model="data.user.name"
              placeholder="请输入姓名" auto-complete="false"/>
        </el-form-item>
        <div class="form-card__actions">
          <el-button type="primary" size="large" @click="update">保存修改</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.form-card {
  max-width: 720px;
  padding: 24px 28px;
}

.form-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.form-card__title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text, #1f2430);
}

.form-card__desc {
  margin-top: 4px;
  font-size: 12.5px;
  color: var(--text-3, #8c93a8);
}

.form-card__body {
  max-width: 520px;
  padding-top: 6px;
}

.avatar-field {
  display: flex;
  align-items: center;
  gap: 18px;
}

.avatar-field__tip {
  font-size: 12.5px;
  color: var(--text-2, #59607a);
  line-height: 1.8;
}

.avatar-field__sub {
  font-size: 11.5px;
  color: var(--text-3, #8c93a8);
}

.form-card__actions {
  padding-left: 90px;
  margin-top: 8px;
}

@media (max-width: 640px) {
  .form-card {
    padding: 18px;
  }
  .form-card__actions {
    padding-left: 0;
  }
  .form-card__actions .el-button {
    width: 100%;
  }
  .avatar-field {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>