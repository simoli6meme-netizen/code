<template>
  <div class="register-page">
    <!-- ========== 左侧：品牌展示区 ========== -->
    <aside class="brand-panel">
      <!-- 浮动零食装饰 -->
      <span class="float-snack snack-1">🍭</span>
      <span class="float-snack snack-2">🥤</span>
      <span class="float-snack snack-3">🍫</span>
      <span class="float-snack snack-4">🍬</span>
      <span class="float-snack snack-5">🧃</span>

      <div class="brand-content">
        <div class="brand-logo">{{ brandCopy.logo }}</div>
        <h1 class="brand-title">{{ brandCopy.title }}</h1>
        <p class="brand-sub">{{ brandCopy.sub }}</p>

        <ul class="feature-list">
          <li v-for="(f, i) in brandCopy.features" :key="i">
            <span class="feature-icon">{{ f.icon }}</span>
            <div>
              <strong>{{ f.title }}</strong>
              <p>{{ f.desc }}</p>
            </div>
          </li>
        </ul>
      </div>
      <div class="brand-footer">© 2026 校园小卖部 · 快乐每一天</div>
    </aside>

    <!-- ========== 右侧：表单区 ========== -->
    <main class="form-panel">
      <!-- 顶部：已有账号入口 -->
      <header class="top-bar">
        <span class="top-tip">已有账号？</span>
        <a href="/login" class="top-link">立即登录 →</a>
      </header>

      <div class="form-wrapper">
        <div class="form-header">
          <h2 class="form-title">创建新账号 🎒</h2>
          <p class="form-subtitle">填写信息，开启你的小卖部之旅</p>
        </div>

        <!-- ===== 角色切换 ===== -->
        <div class="role-switch">
          <button
              v-for="r in roles"
              :key="r.value"
              type="button"
              class="role-btn"
              :class="{ 'is-active': data.form.role === r.value }"
              @click="data.form.role = r.value"
          >
            <span class="role-btn__icon">{{ r.icon }}</span>
            <span class="role-btn__text">
              <strong>{{ r.label }}</strong>
              <small>{{ r.desc }}</small>
            </span>
          </button>
        </div>

        <el-form :model="data.form" ref="formRef" :rules="data.rules" size="large">
          <el-form-item prop="username">
            <el-input :prefix-icon="User" v-model="data.form.username" placeholder="请输入学号 / 用户名" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input :prefix-icon="Lock" v-model="data.form.password" placeholder="请设置密码" show-password />
          </el-form-item>
          <el-form-item prop="newPassword">
            <el-input :prefix-icon="Lock" v-model="data.form.newPassword" placeholder="请再次确认密码" show-password />
          </el-form-item>

          <!-- 验证码 -->
          <el-form-item prop="captcha">
            <div class="captcha-row">
              <el-input v-model="data.form.captcha" placeholder="请输入验证码" maxlength="6" />
              <img :src="data.captchaUrl" @click="refreshCaptcha" class="captcha-img" title="点击刷新验证码" />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button class="submit-btn" @click="register">
              {{ data.form.role === '外卖员' ? '加入骑手队伍' : '加入小卖部' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 底部协议 -->
        <footer class="agreement">
          注册即表示同意
          <a href="#" class="agreement-link">《用户协议》</a>
          与
          <a href="#" class="agreement-link">《隐私政策》</a>
        </footer>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from "vue";
import { User, Lock, Iphone } from "@element-plus/icons-vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

// 开发环境走 Vite proxy 的 /api，生产环境用 VITE_BASE_URL
const BASE = import.meta.env.DEV
    ? '/api'
    : (import.meta.env.VITE_BASE_URL || '/api')

const genKey = () => Date.now().toString(36) + Math.random().toString(36).slice(2, 10)

// ===== 角色配置：value 需与后端约定一致 =====
const roles = [
  { value: '普通用户', label: '普通用户', desc: '逛小卖部 · 下单购物', icon: '🎒' },
  { value: '外卖员',   label: '外卖员',   desc: '接单配送 · 赚取收益', icon: '🛵' }
]

// ===== 左侧品牌区文案：随角色联动 =====
const brandCopy = computed(() => {
  if (data.form.role === '外卖员') {
    return {
      logo: '🛵',
      title: '成为骑手',
      sub: '注册外卖员账号，开启校园配送之旅',
      features: [
        { icon: '⚡', title: '自由接单', desc: '随时上下线，接单更灵活' },
        { icon: '💰', title: '多劳多得', desc: '收益实时结算，提现便捷' },
        { icon: '🛡', title: '安全保障', desc: '配送保险，出行更安心' }
      ]
    }
  }
  return {
    logo: '🏪',
    title: '欢迎加入',
    sub: '注册账号，成为校园小卖部的一员',
    features: [
      { icon: '🎉', title: '极速注册', desc: '一分钟搞定，立刻开逛' },
      { icon: '🔒', title: '安全可靠', desc: '账号信息多重保护' },
      { icon: '🎁', title: '学生专享', desc: '专属优惠，惊喜不断' }
    ]
  }
})

const data = reactive({
  form: {
    role: '普通用户',
    captcha: '',
    captchaKey: '',
    phone: ''
  },
  captchaUrl: '',
  rules: {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    newPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }],
    captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
    ]
  }
})

const formRef = ref()

const refreshCaptcha = () => {
  data.form.captchaKey = genKey()
  data.form.captcha = ''
  data.captchaUrl = `${BASE}/captcha?key=${data.form.captchaKey}&t=${Date.now()}`
}

onMounted(refreshCaptcha)

const register = () => {
  formRef.value.validate((valid => {
    if (valid) {
      if (data.form.password !== data.form.newPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
      }

      // 如果后端分两个接口，可以这样区分：
      // const url = data.form.role === '外卖员' ? '/register/delivery' : '/register'
      // request.post(url, data.form)...
      request.post('/register', data.form).then(res => {
        if (res.code === '200') {
          if (data.form.role === '外卖员') {
            ElMessage.success('外卖员注册成功')
          } else {
            ElMessage.success('恭喜用户您注册成功')
          }
          router.push('/login')
        } else {
          ElMessage.error(res.msg)
          refreshCaptcha()
        }
      }).catch(() => refreshCaptcha())
    }
  })).catch(error => console.error(error))
}
</script>

<style scoped>
/* ========== 整体：左右双栏 ========== */
.register-page {
  display: flex;
  min-height: 100vh;
  background: #FFFBF3;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
  "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
}

/* ========== 左侧品牌区 ========== */
.brand-panel {
  position: relative;
  flex: 0 0 42%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 56px 60px;
  overflow: hidden;
  background: linear-gradient(155deg, #FFC14B 0%, #FF8C42 45%, #F4511E 100%);
  color: #ffffff;
}
.brand-panel::before,
.brand-panel::after {
  content: "";
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.brand-panel::before {
  width: 440px;
  height: 440px;
  background: rgba(255, 255, 255, 0.16);
  top: -170px;
  right: -130px;
}
.brand-panel::after {
  width: 320px;
  height: 320px;
  background: rgba(255, 255, 255, 0.10);
  bottom: -120px;
  left: -90px;
}

.float-snack {
  position: absolute;
  z-index: 0;
  font-size: 32px;
  opacity: 0.6;
  filter: drop-shadow(0 8px 12px rgba(150, 55, 0, 0.18));
  animation: floaty 6s ease-in-out infinite;
  pointer-events: none;
  user-select: none;
}
.snack-1 { top: 10%; left: 8%; animation-delay: 0s; }
.snack-2 { top: 18%; right: 12%; font-size: 40px; animation-delay: 1.1s; }
.snack-3 { bottom: 26%; left: 10%; animation-delay: 2.2s; }
.snack-4 { bottom: 13%; right: 16%; font-size: 28px; animation-delay: 0.7s; }
.snack-5 { top: 50%; right: 6%; animation-delay: 1.7s; }

@keyframes floaty {
  0%, 100% { transform: translateY(0) rotate(-6deg); }
  50% { transform: translateY(-14px) rotate(8deg); }
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 400px;
}
.brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 66px;
  height: 66px;
  border-radius: 20px;
  font-size: 34px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 12px 28px rgba(180, 70, 0, 0.30);
  margin-bottom: 30px;
  transition: transform 0.3s;
}
.brand-title {
  font-size: 36px;
  font-weight: 800;
  margin: 0 0 12px;
  letter-spacing: 3px;
  text-shadow: 0 2px 14px rgba(180, 70, 0, 0.28);
}
.brand-sub {
  font-size: 15px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.88);
  margin: 0 0 46px;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.feature-list li {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  align-items: flex-start;
}
.feature-icon {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 6px 16px rgba(180, 70, 0, 0.20);
}
.feature-list strong {
  display: block;
  font-size: 15px;
  color: #fff;
  margin-bottom: 3px;
  letter-spacing: 0.5px;
}
.feature-list p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.78);
  margin: 0;
}

.brand-footer {
  position: relative;
  z-index: 1;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
  letter-spacing: 1px;
}

/* ========== 右侧表单区 ========== */
.form-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #FFFBF3;
}

.top-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  padding: 24px 40px;
  font-size: 14px;
}
.top-tip {
  color: #A0785A;
}
.top-link {
  color: #F4511E;
  font-weight: 600;
  text-decoration: none;
  padding: 8px 18px;
  border-radius: 999px;
  border: 1.5px solid #FFD7B5;
  background: #ffffff;
  transition: all 0.2s;
}
.top-link:hover {
  background: #F4511E;
  color: #ffffff;
  border-color: #F4511E;
  box-shadow: 0 6px 16px rgba(244, 81, 30, 0.25);
}

.form-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  padding: 0 24px 60px;
}

.form-header {
  margin-bottom: 24px;
}
.form-title {
  font-size: 28px;
  font-weight: 800;
  color: #4A2E1B;
  margin: 0 0 8px;
  letter-spacing: 1px;
}
.form-subtitle {
  font-size: 14px;
  color: #A0785A;
  margin: 0;
}

/* ========== 角色切换 ========== */
.role-switch {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.role-btn {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1.5px solid #FFE1BF;
  background: #FFF3E0;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s;
  font-family: inherit;
}

.role-btn:hover {
  border-color: #FFC98F;
  background: #FFF7EC;
}

.role-btn.is-active {
  border-color: #F4511E;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(244, 81, 30, 0.12);
}

.role-btn__icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  background: #ffffff;
  box-shadow: 0 3px 8px rgba(180, 70, 0, 0.12);
  transition: background 0.2s;
}

.role-btn.is-active .role-btn__icon {
  background: #FFF3E0;
}

.role-btn__text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.role-btn__text strong {
  font-size: 14px;
  font-weight: 700;
  color: #4A2E1B;
  line-height: 1.3;
}

.role-btn__text small {
  font-size: 11.5px;
  color: #A0785A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role-btn.is-active .role-btn__text strong {
  color: #F4511E;
}

/* ========== 表单控件 ========== */
.form-wrapper :deep(.el-form-item) {
  margin-bottom: 22px;
}
.form-wrapper :deep(.el-input__wrapper) {
  height: 50px;
  border-radius: 14px;
  background: #FFF3E0;
  box-shadow: 0 0 0 1.5px #FFE1BF inset;
  transition: all 0.2s;
}
.form-wrapper :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1.5px #FFC98F inset;
}
.form-wrapper :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow:
      0 0 0 2px #FF8C42 inset,
      0 0 0 5px rgba(255, 140, 66, 0.15);
}

/* 验证码行 */
.captcha-row {
  display: flex;
  width: 100%;
  gap: 10px;
}
.captcha-row .el-input {
  flex: 1;
}
.captcha-img {
  flex-shrink: 0;
  width: 120px;
  height: 50px;
  border-radius: 14px;
  cursor: pointer;
  border: 1.5px solid #FFE1BF;
  background: #FFF3E0;
  transition: border-color 0.2s, transform 0.15s, box-shadow 0.2s;
}
.captcha-img:hover {
  border-color: #FF8C42;
  transform: scale(1.02);
  box-shadow: 0 6px 16px rgba(255, 140, 66, 0.20);
}

/* 注册按钮 */
.submit-btn {
  width: 100%;
  height: 52px;
  margin-top: 6px;
  border: none !important;
  border-radius: 14px !important;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 3px;
  color: #ffffff !important;
  background: linear-gradient(135deg, #FFA14A, #F4511E) !important;
  box-shadow: 0 10px 22px rgba(244, 81, 30, 0.35);
  transition: transform 0.2s, box-shadow 0.2s;
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(244, 81, 30, 0.45);
}
.submit-btn:active {
  transform: translateY(0);
}

/* 底部协议 */
.agreement {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: #B08968;
  line-height: 1.8;
}
.agreement-link {
  color: #F4511E;
  text-decoration: none;
  font-weight: 600;
}
.agreement-link:hover {
  text-decoration: underline;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .brand-panel {
    display: none;
  }
  .form-panel {
    min-height: 100vh;
  }
  .top-bar {
    padding: 20px 24px;
  }
  .form-wrapper {
    padding: 0 24px 40px;
  }
}
@media (max-width: 480px) {
  .form-title {
    font-size: 24px;
  }
  .captcha-img {
    width: 100px;
  }
  .top-link {
    padding: 6px 14px;
  }
  .role-switch {
    gap: 8px;
  }
  .role-btn {
    padding: 10px;
    gap: 8px;
  }
  .role-btn__icon {
    width: 30px;
    height: 30px;
    font-size: 16px;
  }
  .role-btn__text small {
    display: none;
  }
}
</style>