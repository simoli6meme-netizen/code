<template>
  <div class="delivery-page">
    <div class="delivery-page__head">
      <div>
        <div class="delivery-page__title">修改密码</div>
        <div class="delivery-page__desc">
          当前账号：{{ data.user.username || '-' }}（{{ data.user.name || '骑手' }}），修改后请使用新密码登录
        </div>
      </div>
    </div>

    <div class="delivery-card">
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px" class="pwd-form">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="data.form.oldPassword" type="password" show-password placeholder="请输入当前登录密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="data.form.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="data.form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="updatePassword">保存修改</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const formRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  form: { oldPassword: '', newPassword: '', confirmPassword: '' },
  rules: {
    oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
    newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
    confirmPassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }]
  }
})

// 走系统统一的 /updatePassword，由后端校验原密码
const updatePassword = () => {
  formRef.value.validate(valid => {
    if (!valid) return
    const oldPassword = (data.form.oldPassword || '').trim()
    const newPassword = (data.form.newPassword || '').trim()
    const confirmPassword = (data.form.confirmPassword || '').trim()

    if (!oldPassword || !newPassword || !confirmPassword) {
      ElMessage.warning('密码不能为空格')
      return
    }
    if (newPassword !== confirmPassword) {
      ElMessage.error('两次输入的新密码不一致')
      return
    }
    if (newPassword === oldPassword) {
      ElMessage.warning('新密码与原密码相同，无需修改')
      return
    }
    if (!data.user?.id) {
      ElMessage.error('登录信息已失效，请重新登录')
      return
    }

    request.put('/updatePassword', {
      id: data.user.id,
      username: data.user.username,
      role: data.user.role || '外卖员',
      password: oldPassword,          // 原密码
      newPassword: newPassword       // 新密码
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('密码修改成功')
        // 同步本地缓存里的密码，避免状态不一致
        data.user.password = newPassword
        localStorage.setItem('system-user', JSON.stringify(data.user))
        reset()
      } else {
        ElMessage.error(res.msg || '修改失败')
      }
    })
  })
}

const reset = () => {
  data.form = { oldPassword: '', newPassword: '', confirmPassword: '' }
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
.pwd-form { max-width: 460px; }
</style>
