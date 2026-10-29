<template>
  <div class="delivery-page">
    <div class="delivery-page__head">
      <div>
        <div class="delivery-page__title">个人信息</div>
        <div class="delivery-page__desc">维护你的骑手资料与头像</div>
      </div>
    </div>

    <div class="delivery-card person-card">
      <div class="person-avatar">
        <img :src="data.user.avatar ? formatImg(data.user.avatar) : DEFAULT_AVATAR" alt="" @error="(e) => (e.target.src = DEFAULT_AVATAR)" />
        <el-upload
            class="person-avatar__upload"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
        >
          <el-button size="small" type="primary">更换头像</el-button>
        </el-upload>
      </div>

      <el-form ref="formRef" :model="data.user" :rules="data.rules" label-width="90px" class="person-form">
        <el-form-item label="账号">
          <el-input v-model="data.user.username" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="data.user.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="data.user.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const BASE = import.meta.env.DEV
    ? '/api'
    : (import.meta.env.VITE_BASE_URL || '/api')

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const formatImg = (url) => {
  if (!url) return DEFAULT_AVATAR
  if (/^https?:\/\//i.test(url)) {
    return url.replace(/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i, BASE)
  }
  return BASE + (url.startsWith('/') ? url : '/' + url)
}

const uploadUrl = import.meta.env.DEV
    ? '/api/files/upload'
    : ((import.meta.env.VITE_BASE_URL || '') + '/files/upload')

const emit = defineEmits(['updateUser'])

const formRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  rules: {
    name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
  }
})

const loadUser = () => {
  if (!data.user?.id) return
  request.get('/delivery/selectById/' + data.user.id).then(res => {
    Object.assign(data.user, res.data)
    localStorage.setItem('system-user', JSON.stringify(data.user))
  })
}
loadUser()

const handleAvatarSuccess = (res) => {
  if (!res || res.code !== '200') {
    ElMessage.error(res?.msg || '上传失败')
    return
  }
  data.user.avatar = res.data
  request.put('/delivery/update', data.user).then(r => {
    if (r.code === '200') {
      localStorage.setItem('system-user', JSON.stringify(data.user))
      emit('updateUser')
      ElMessage.success('头像已更新')
    } else {
      ElMessage.error(r.msg)
    }
  })
}

const save = () => {
  formRef.value.validate(valid => {
    if (!valid) return
    request.put('/delivery/update', data.user).then(res => {
      if (res.code === '200') {
        localStorage.setItem('system-user', JSON.stringify(data.user))
        emit('updateUser')
        ElMessage.success('保存成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  })
}
</script>

<style scoped>
.delivery-page { display: flex; flex-direction: column; gap: 16px; }
.delivery-page__title { font-size: 18px; font-weight: 600; color: var(--text, #2b2118); }
.delivery-page__desc { margin-top: 4px; font-size: 12.5px; color: var(--text-3, #a2917e); }
.delivery-card {
  background: #fff;
  border: 1px solid var(--line, #f0e6da);
  border-radius: var(--radius, 12px);
  padding: 20px;
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
}
.person-card { display: flex; gap: 24px; flex-wrap: wrap; }
.person-avatar { text-align: center; }
.person-avatar img {
  width: 96px; height: 96px; border-radius: 50%;
  object-fit: cover; border: 2px solid var(--line, #f0e6da);
}
.person-avatar__upload { margin-top: 10px; }
.person-form { flex: 1; min-width: 280px; max-width: 460px; }
</style>
