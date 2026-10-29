<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from "@/utils/request";
import router from "@/router";

const formRef = ref()

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  rules: {
    password: [
      { required: true, message: '请输入原密码', trigger: 'blur' }
    ],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请确认新密码', trigger: 'blur' }
    ]
  }
})

const updatePassword = () => {
  if(data.user.newPassword !== data.user.confirmPassword){
    ElMessage.warning('两次输入的新密码不同，请确认')
    return
  }
  request.put("/updatePassword", data.user).then(res => {
    if (res.code === '200') {
      ElMessage.success("更新成功")
      logout()
    }else{
      ElMessage.error(res.msg)
    }
  })
}

const logout = () => {
  router.push('/login')
  localStorage.removeItem('system-user')
}
</script>

<template>
    <div class="page">
      <div class="card form-card">
        <div class="form-card__head">
          <div>
            <div class="form-card__title">修改密码</div>
            <div class="form-card__desc">为了账号安全，修改成功后需要重新登录</div>
          </div>
          <el-icon class="form-card__icon"><Lock /></el-icon>
        </div>

        <el-divider />

        <el-form ref="formRef" :model="data.user" :rules="data.rules" label-width="100px" class="form-card__body">
          <el-form-item prop="password" label="原密码">
            <el-input
                :disabled="!!data.user.id"
                v-model="data.user.password"
                autocomplete="off"
                show-password>
            </el-input>
            <div class="field-tip">当前登录账号的原密码</div>
          </el-form-item>
          <el-form-item prop="newPassword" label="新密码">
            <el-input
                v-model="data.user.newPassword"
                autocomplete="off"
                show-password
                placeholder="请输入新密码">
            </el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword" label="确认新密码">
            <el-input
                v-model="data.user.confirmPassword"
                autocomplete="off"
                show-password
                placeholder="请再次输入新密码">
            </el-input>
          </el-form-item>
          <div class="form-card__actions">
            <el-button type="primary" size="large" @click="updatePassword">保存修改</el-button>
          </div>
        </el-form>
      </div>
    </div>
</template>

<style scoped>
.form-card {
  max-width: 620px;
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

.form-card__icon {
  font-size: 22px;
  color: #c3c8d6;
}

.form-card__body {
  max-width: 460px;
  padding-top: 6px;
}

.field-tip {
  font-size: 11.5px;
  color: var(--text-3, #8c93a8);
  line-height: 1.4;
  margin-top: 2px;
}

.form-card__actions {
  padding-left: 100px;
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
}
</style>
