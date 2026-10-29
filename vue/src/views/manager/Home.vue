<template>
  <div class="page home-page">
    <!-- 欢迎横幅 -->
    <section class="welcome-banner">
      <div class="welcome-banner__glow welcome-banner__glow--1"></div>
      <div class="welcome-banner__glow welcome-banner__glow--2"></div>
      <div class="welcome-banner__main">
        <img
            :src="data.user.avatar ? formatImg(data.user.avatar) : DEFAULT_AVATAR"
            class="welcome-banner__avatar"
            alt=""
            @error="(e) => (e.target.src = DEFAULT_AVATAR)"
        />
        <div>
          <div class="welcome-banner__title">
            欢迎回来，{{ data.user.name || '管理员' }}
            <span class="welcome-banner__wave">👋</span>
          </div>
          <div class="welcome-banner__time">
            <el-icon><Clock /></el-icon>
            {{ data.now }}
          </div>
        </div>
      </div>

      <div class="welcome-banner__actions">
        <button class="welcome-banner__btn" @click="go('/manager/dataManager')">
          <el-icon><TrendCharts /></el-icon>数据统计
        </button>
        <button class="welcome-banner__btn welcome-banner__btn--ghost" @click="go('/manager/person')">
          <el-icon><Postcard /></el-icon>个人信息
        </button>
      </div>
    </section>

    <!-- 功能入口分组 -->
    <section v-for="(group, gi) in menuGroups" :key="gi" class="home-group">
      <div class="home-group__head">
        <span class="home-group__bar"></span>
        <span class="home-group__title">{{ group.title }}</span>
        <span class="home-group__count">{{ group.items.length }}</span>
      </div>

      <div class="home-grid">
        <div
            v-for="item in group.items"
            :key="item.path"
            class="home-tile"
            :class="{ 'is-danger': item.path === '__logout__' }"
            @click="go(item.path)"
        >
          <div class="home-tile__icon" :style="{ background: item.bg }">{{ item.icon }}</div>
          <div class="home-tile__text">
            <div class="home-tile__name">{{ item.name }}</div>
            <div class="home-tile__desc">{{ item.desc }}</div>
          </div>
          <el-icon class="home-tile__arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

// ===== 图片地址处理（用户头像用）=====
const BASE = import.meta.env.DEV
    ? '/api'
    : (import.meta.env.VITE_BASE_URL || '/api')

const PLACEHOLDER =
    'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect fill="%23FFF3E0" width="300" height="300"/%3E%3Ctext x="50%25" y="50%25" font-size="72" text-anchor="middle" dy=".35em"%3E%F0%9F%8D%AC%3C/text%3E%3C/svg%3E'

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const formatImg = (url) => {
  if (!url) return PLACEHOLDER
  if (/^https?:\/\//i.test(url)) {
    return url.replace(/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i, BASE)
  }
  return BASE + (url.startsWith('/') ? url : '/' + url)
}

const router = useRouter()

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  now: ''
})

const menuGroups = [
  {
    title: '数据管理',
    items: [
      { name: '数据统计', desc: '销售趋势与分类占比', icon: '📊', path: '/manager/dataManager', bg: '#e5f0ff' },
      { name: '订单信息', desc: '查看与处理用户订单', icon: '📋', path: '/manager/orders',      bg: '#fff0e5' },
      { name: '商品分类', desc: '维护商品分类', icon: '🏷',  path: '/manager/category',    bg: '#f0ffe5' },
      { name: '商品信息', desc: '上架、编辑与推荐', icon: '📦', path: '/manager/goods',       bg: '#ffe5f0' }
    ]
  },
  {
    title: '内容管理',
    items: [
      { name: '轮播图信息', desc: '首页轮播图配置', icon: '🖼', path: '/manager/carousel', bg: '#f0e5ff' },
      { name: '订单评价', desc: '用户评价内容管理',   icon: '💬', path: '/manager/comment',  bg: '#e5fff5' }
    ]
  },
  {
    title: '用户管理',
    items: [
      { name: '用户收藏',   desc: '查看用户收藏记录', icon: '⭐', path: '/manager/collect',  bg: '#fffbe5' },
      { name: '用户充值',   desc: '充值流水查询', icon: '💰', path: '/manager/recharge', bg: '#e5fff0' },
      { name: '用户管理',   desc: '普通用户账号维护', icon: '👥', path: '/manager/user',     bg: '#e5f8ff' },
      { name: '外卖员管理',   desc: '外卖员账号维护', icon: '👥', path: '/manager/delivery',     bg: '#b9cdd8' },
      { name: '管理员信息', desc: '管理员账号维护', icon: '👤', path: '/manager/admin',    bg: '#ffe5e5' }
    ]
  },
  {
    title: '个人中心',
    items: [
      { name: '个人信息', desc: '头像与资料修改', icon: '👤', path: '/manager/person',   bg: '#e5e5ff' },
      { name: '修改密码', desc: '更新登录密码', icon: '🔑', path: '/manager/password', bg: '#ffe5d5' },
      { name: '退出系统', desc: '安全退出管理端', icon: '🚪', path: '__logout__',        bg: '#ffd5d5' }
    ]
  }
]

const go = (path) => {
  if (path === '__logout__') {
    // 退出登录
    localStorage.removeItem('system-user')
    router.push('/login')
    return
  }
  router.push(path)
}


// 格式化当前时间
function formatNow() {
  const d = new Date()
  const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  const pad = n => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} 星期${week} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

let timer = null

onMounted(() => {
  data.now = formatNow()                    // 先给初值
  timer = setInterval(() => {
    data.now = formatNow()                  // 每秒刷新
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)           // 离开页面时清掉定时器
})
</script>

<style scoped>
.home-page {
  gap: 20px;
}

/* ==================== 欢迎横幅 ==================== */
.welcome-banner {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  padding: 28px 30px;
  border-radius: var(--radius-lg, 16px);
  background: linear-gradient(120deg, #2e3143 0%, #3b4060 55%, #4b5178 100%);
  box-shadow: 0 16px 40px rgba(46, 49, 67, 0.22);
  color: #fff;
}

.welcome-banner__glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
  pointer-events: none;
}

.welcome-banner__glow--1 {
  width: 220px;
  height: 220px;
  right: -60px;
  top: -90px;
  background: rgba(255, 255, 255, 0.08);
}

.welcome-banner__glow--2 {
  width: 140px;
  height: 140px;
  right: 140px;
  bottom: -80px;
  background: rgba(255, 209, 102, 0.12);
}

.welcome-banner__main {
  display: flex;
  align-items: center;
  gap: 18px;
  position: relative;
  z-index: 1;
}

.welcome-banner__avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.22);
}

.welcome-banner__title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.welcome-banner__wave {
  font-size: 20px;
  margin-left: 2px;
}

.welcome-banner__time {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.68);
}

.welcome-banner__actions {
  display: flex;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.welcome-banner__btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 38px;
  padding: 0 16px;
  border: none;
  border-radius: 10px;
  background: #fff;
  color: #2e3143;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, background 0.2s;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.18);
}

.welcome-banner__btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.24);
}

.welcome-banner__btn--ghost {
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.22);
}

.welcome-banner__btn--ghost:hover {
  background: rgba(255, 255, 255, 0.22);
}

/* ==================== 分组 ==================== */
.home-group__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.home-group__bar {
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: var(--brand, #2e3143);
}

.home-group__title {
  font-size: 15.5px;
  font-weight: 600;
  color: var(--text, #1f2430);
}

.home-group__count {
  font-size: 11px;
  color: var(--text-3, #8c93a8);
  background: #eff1f7;
  border-radius: 999px;
  padding: 1px 8px;
  line-height: 18px;
}

.home-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.home-tile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid var(--line, #eceef3);
  border-radius: var(--radius, 12px);
  cursor: pointer;
  transition: transform 0.22s cubic-bezier(0.22, 0.61, 0.36, 1), box-shadow 0.22s, border-color 0.22s;
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
  position: relative;
  overflow: hidden;
}

.home-tile:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(20, 24, 40, 0.1);
  border-color: #dfe3ef;
}

.home-tile__icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 21px;
  flex-shrink: 0;
}

.home-tile__text {
  flex: 1;
  min-width: 0;
}

.home-tile__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text, #1f2430);
  margin-bottom: 3px;
}

.home-tile__desc {
  font-size: 12px;
  color: var(--text-3, #8c93a8);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-tile__arrow {
  color: #c6cbd8;
  font-size: 14px;
  transition: transform 0.2s, color 0.2s;
  flex-shrink: 0;
}

.home-tile:hover .home-tile__arrow {
  transform: translateX(3px);
  color: var(--brand, #2e3143);
}

.home-tile.is-danger:hover .home-tile__arrow {
  color: #f64848;
}

@media (max-width: 1200px) {
  .home-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .home-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .home-grid {
    grid-template-columns: 1fr;
  }
  .welcome-banner {
    padding: 20px;
  }
  .welcome-banner__title {
    font-size: 18px;
  }
  .welcome-banner__actions {
    width: 100%;
  }
  .welcome-banner__btn {
    flex: 1;
    justify-content: center;
  }
}
</style>