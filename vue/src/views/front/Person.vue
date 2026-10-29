<script setup>
import { reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import request from "@/utils/request";

const baseUrl = import.meta.env.VITE_BASE_URL


const emit = defineEmits(['updateUser'])

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
  request.get(`/user/selectById/` + data.user.id).then(res => {
    Object.assign(data.user, res.data)
    //存储最新用户信息
    localStorage.setItem('system-user',JSON.stringify(res.data))
    emit('updateUser')
  })
}
loadUser()

const handleFileUpload = (res) => {
  data.user.avatar = res.data
  axios.put('/user/update', data.user).then(() => {
    ElMessage.success('头像更新成功')
  })
}
const update = () => {
  request.put("/user/update", data.user).then(res => {
    if (res.code === '200') {
      ElMessage.success("更新成功")
      loadUser()
    }else{
      ElMessage.error(res.msg)
    }
  })
}
</script>

<template>
  <div class="front-container">
    <div class="person-layout">
      <!-- 左侧资料卡 -->
      <aside class="panel profile-card">
        <div class="profile-card__cover"></div>
        <div class="profile-card__body">
          <el-upload
              class="avatar-uploader profile-card__uploader"
              :action="baseUrl + '/files/upload'"
              :show-file-list="false"
              :on-success="handleFileUpload"
          >
            <img v-if="data.user.avatar" :src="data.user.avatar" class="profile-card__avatar">
            <div v-else class="profile-card__avatar profile-card__avatar--empty">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="profile-card__avatar-mask">更换头像</div>
          </el-upload>

          <div class="profile-card__name">{{ data.user.name || '未设置姓名' }}</div>
          <div class="profile-card__account">@{{ data.user.username }}</div>

          <el-tag class="profile-card__role" effect="light">{{ data.user.role || '普通用户' }}</el-tag>

          <div class="profile-card__balance">
            <div class="profile-card__balance-label">账户余额</div>
            <div class="profile-card__balance-value">
              <span>￥</span>{{ data.user.account ?? '0.00' }}
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧编辑表单 -->
      <div class="panel form-card">
        <div class="form-card__head">
          <div>
            <div class="form-card__title">个人信息</div>
            <div class="form-card__desc">完善头像和昵称，让小店更好地认识你</div>
          </div>
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
                  :action="baseUrl + '/files/upload'"
                  :show-file-list="false"
                  :on-success="handleFileUpload"
              >
                <img v-if="data.user.avatar" :src="data.user.avatar" class="avatar-preview">
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
  </div>
</template>

<style scoped>
.person-layout {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

/* ==================== 左侧资料卡 ==================== */
.profile-card {
  overflow: hidden;
  border-radius: var(--radius-lg, 16px);
  background: #fff;
  border: 1px solid var(--line, #eceef3);
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
}

.profile-card__cover {
  height: 92px;
  background: linear-gradient(135deg, #2e3143 0%, #4b5178 100%);
  position: relative;
}

.profile-card__cover::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.18), transparent 55%);
}

.profile-card__body {
  padding: 0 22px 24px;
  text-align: center;
  margin-top: -44px;
  position: relative;
}

.profile-card__uploader {
  display: inline-block;
}

.profile-card__uploader :deep(.el-upload) {
  border-radius: 50%;
  overflow: hidden;
  width: 88px;
  height: 88px;
  border: 4px solid #fff;
  box-shadow: 0 6px 18px rgba(20, 24, 40, 0.16);
  position: relative;
  background: #f2f3f7;
}

.profile-card__avatar {
  width: 80px;
  height: 80px;
  object-fit: cover;
  display: block;
}

.profile-card__avatar--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #b9bdca;
  font-size: 24px;
}

.profile-card__avatar-mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 26px;
  line-height: 26px;
  font-size: 11px;
  color: #fff;
  background: rgba(20, 24, 40, 0.55);
  opacity: 0;
  transition: opacity 0.2s;
}

.profile-card__uploader:hover .profile-card__avatar-mask {
  opacity: 1;
}

.profile-card__name {
  margin-top: 12px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text, #1f2430);
}

.profile-card__account {
  margin-top: 3px;
  font-size: 12.5px;
  color: var(--text-3, #8c93a8);
}

.profile-card__role {
  margin-top: 10px;
  border-radius: 999px;
}

.profile-card__balance {
  margin-top: 20px;
  padding: 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff6f6, #fff);
  border: 1px solid #ffe3e3;
}

.profile-card__balance-label {
  font-size: 12px;
  color: var(--text-3, #8c93a8);
}

.profile-card__balance-value {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 700;
  color: var(--accent, #f64848);
  letter-spacing: -0.5px;
}

.profile-card__balance-value span {
  font-size: 14px;
  margin-right: 2px;
}

/* ==================== 右侧表单 ==================== */
.form-card {
  padding: 24px 28px;
  border-radius: var(--radius-lg, 16px);
  background: #fff;
  border: 1px solid var(--line, #eceef3);
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
  min-height: 420px;
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
  max-width: 560px;
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

.balance-text {
  color: var(--accent, #f64848);
  font-weight: 700;
  font-size: 18px;
}

.form-card__actions {
  padding-left: 90px;
  margin-top: 8px;
}

@media (max-width: 960px) {
  .person-layout {
    grid-template-columns: 1fr;
  }
  .form-card {
    min-height: auto;
  }
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
