<template>
  <div class="login-page">
    <!-- ========== 左侧：品牌展示区 ========== -->
    <aside class="brand-panel">
      <!-- 浮动零食装饰 -->
      <span class="float-snack snack-1">🍭</span>
      <span class="float-snack snack-2">🥤</span>
      <span class="float-snack snack-3">🍫</span>
      <span class="float-snack snack-4">🍬</span>
      <span class="float-snack snack-5">🧃</span>

      <div class="brand-content">
        <div class="brand-logo">🏪</div>
        <h1 class="brand-title">欢迎光临</h1>
        <p class="brand-sub">校园小卖部 · 下课来点好吃的</p>

        <ul class="feature-list">
          <li>
            <span class="feature-icon">🍭</span>
            <div>
              <strong>零食满满</strong>
              <p>辣条薯片糖果，一应俱全</p>
            </div>
          </li>
          <li>
            <span class="feature-icon">🥤</span>
            <div>
              <strong>冰镇饮料</strong>
              <p>可乐雪碧奶茶，清凉解暑</p>
            </div>
          </li>
          <li>
            <span class="feature-icon">⚡</span>
            <div>
              <strong>课间速达</strong>
              <p>十分钟课间，快速选购</p>
            </div>
          </li>
          <li>
            <span class="feature-icon">🎁</span>
            <div>
              <strong>学生专享</strong>
              <p>积分兑换，惊喜好礼</p>
            </div>
          </li>
        </ul>
      </div>
      <div class="brand-footer">© 2026 校园小卖部 · 快乐每一天</div>
    </aside>

    <!-- ========== 右侧：表单区 ========== -->
    <main class="form-panel">
      <!-- 顶部：注册入口 -->
      <header class="top-bar">
        <span class="top-tip">还没有账号？</span>
        <a href="/register" class="top-link">立即注册 →</a>
      </header>

      <div class="form-wrapper">
        <div class="form-header">
          <h2 class="form-title">欢迎回来 👋</h2>
          <p class="form-subtitle">登录你的账号，开始选购吧</p>
        </div>

        <el-form :model="data.form" ref="formRef" :rules="data.rules" size="large">
          <el-form-item prop="username">
            <el-input :prefix-icon="User" v-model="data.form.username" placeholder="请输入学号 / 用户名" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input :prefix-icon="Lock" v-model="data.form.password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item prop="role">
            <el-select v-model="data.form.role" placeholder="请选择身份" style="width: 100%">
              <!-- 显示「学生 / 店主」，但 value 保持原值以兼容后端与路由逻辑 -->
              <el-option value="普通用户" label="🎒 学生"></el-option>
              <el-option value="外卖员" label="🎒 外卖员"></el-option>
              <el-option value="管理员" label="🏪 店主"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button class="submit-btn" :loading="loading" @click="login">开始逛店</el-button>
          </el-form-item>
        </el-form>

        <footer class="agreement">
          登录即表示同意
          <a href="#" class="agreement-link">《用户协议》</a>
          与
          <a href="#" class="agreement-link">《隐私政策》</a>
        </footer>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

const data = reactive({
  // 默认改为「学生」，避免误以店主身份登录
  form: { role: '普通用户' },
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
    ],
  }
})

const formRef = ref()
const loading = ref(false)

const login = () => {
  if (loading.value) return

  formRef.value.validate((valid) => {
    if (!valid) return

    loading.value = true

    request.post('/login', data.form)
        .then(res => {
          if (res.code === '200') {
            ElMessage.success("登录成功")
            localStorage.setItem('system-user', JSON.stringify(res.data))
            if (res.data.role === "管理员") {
              router.push('/manager/home')
            } else if(res.data.role === "外卖员"){
              router.push('/delivery/home')
            }else {
              router.push('/front/home')
            }
          } else {
            ElMessage.error(res.msg || '登录失败')
          }
        })
        .catch(err => {
          console.error('登录请求失败:', err)
          // 关键：请求失败时给用户提示，不再是"点了没反应"
          ElMessage.error('网络异常，请稍后重试')
        })
        .finally(() => {
          loading.value = false
        })
  }).catch(error => {
    console.error(error)
    loading.value = false
  })
}
</script>

<style scoped>
/* ========== 整体：左右双栏 ========== */
.login-page {
  display: flex;
  min-height: 100vh;
  background: #FFFBF3;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
  "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
}

/* ========== 左侧品牌区：明亮的暖橙色 ========== */
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

/* 浮动零食装饰 */
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
  margin-bottom: 32px;
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

/* ========== 输入框：暖奶咖底 + 橙色聚焦 ========== */
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
.form-wrapper :deep(.el-input__wrapper):hover {
  box-shadow: 0 0 0 1.5px #FFC98F inset;
}
.form-wrapper :deep(.el-input__wrapper).is-focus {
  background: #ffffff;
  box-shadow:
      0 0 0 2px #FF8C42 inset,
      0 0 0 5px rgba(255, 140, 66, 0.15);
}

/* ========== Select 下拉框（与输入框保持一致） ========== */
.form-wrapper :deep(.el-select__wrapper) {
  height: 50px;
  border-radius: 14px;
  background: #FFF3E0;
  box-shadow: 0 0 0 1.5px #FFE1BF inset;
  transition: all 0.2s;
  padding-left: 15px;
}
.form-wrapper :deep(.el-select__wrapper):hover {
  box-shadow: 0 0 0 1.5px #FFC98F inset;
}
.form-wrapper :deep(.el-select__wrapper).is-focused {
  background: #ffffff;
  box-shadow:
      0 0 0 2px #FF8C42 inset,
      0 0 0 5px rgba(255, 140, 66, 0.15);
}

/* ========== 登录按钮：橙色渐变，圆润厚实 ========== */
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

/* ========== 底部协议 ========== */
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
  .top-link {
    padding: 6px 14px;
  }
}
</style>