<template>
  <div class="page home-page">
    <!-- ===== 欢迎横幅 ===== -->
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
            欢迎回来，{{ data.user.name || '骑手小哥' }}
            <span class="welcome-banner__wave">🛵</span>
          </div>
          <div class="welcome-banner__time">
            <el-icon><Clock /></el-icon>
            {{ data.now }}
          </div>
        </div>
      </div>

      <div class="welcome-banner__actions">
        <button class="welcome-banner__btn welcome-banner__btn--ghost" @click="go('/delivery/pending')">
          <el-icon><List /></el-icon>待接单
        </button>
        <button class="welcome-banner__btn welcome-banner__btn--ghost" @click="go('/delivery/person')">
          <el-icon><Postcard /></el-icon>个人信息
        </button>
      </div>
    </section>

    <!-- ===== 今日概览 ===== -->
    <section class="stat-row">
      <div class="stat-card">
        <div class="stat-card__icon" style="background:#fff1e0">📥</div>
        <div class="stat-card__text">
          <div class="stat-card__value">{{ data.stats.pending }}</div>
          <div class="stat-card__label">待接单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon" style="background:#e8f4ff">🛵</div>
        <div class="stat-card__text">
          <div class="stat-card__value">{{ data.stats.delivering }}</div>
          <div class="stat-card__label">配送中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon" style="background:#e9f8ec">✅</div>
        <div class="stat-card__text">
          <div class="stat-card__value">{{ data.stats.completed }}</div>
          <div class="stat-card__label">今日已完成</div>
        </div>
      </div>
    </section>

    <!-- ===== 待接单 / 我的配送 ===== -->
    <section class="home-panels">
      <div class="home-panel">
        <div class="home-panel__head">
          <span class="home-panel__title">最新待接单</span>
          <el-button link type="primary" @click="go('/delivery/pending')">去接单</el-button>
        </div>
        <div v-if="data.pendingList.length" class="home-panel__list">
          <div v-for="item in data.pendingList" :key="item.id" class="mini-order" @click="go('/delivery/pending')">
            <div class="mini-order__main">
              <div class="mini-order__no">{{ item.orderNo }}</div>
              <div class="mini-order__addr">{{ item.address || '自提' }}</div>
            </div>
            <div class="mini-order__right">
              <span class="mini-order__money">￥{{ item.total }}</span>
              <span class="mini-order__time">{{ item.time }}</span>
            </div>
          </div>
        </div>
        <div v-else class="home-panel__empty">暂无待接订单</div>
      </div>

      <div class="home-panel">
        <div class="home-panel__head">
          <span class="home-panel__title">我的配送中</span>
          <el-button link type="primary" @click="go('/delivery/delivering')">去配送</el-button>
        </div>
        <div v-if="data.deliveringList.length" class="home-panel__list">
          <div v-for="item in data.deliveringList" :key="item.id" class="mini-order" @click="go('/delivery/delivering')">
            <div class="mini-order__main">
              <div class="mini-order__no">
                {{ item.orderNo }}
                <el-tag size="small" :type="item.status === '配送中' ? 'primary' : (item.status === '待收货' ? 'warning' : 'success')">{{ item.status }}</el-tag>
              </div>
              <div class="mini-order__addr">{{ item.address || '自提' }}</div>
            </div>
            <div class="mini-order__right">
              <span class="mini-order__money">￥{{ item.total }}</span>
              <span class="mini-order__time">{{ item.time }}</span>
            </div>
          </div>
        </div>
        <div v-else class="home-panel__empty">暂无进行中的配送</div>
      </div>
    </section>

    <!-- ===== 功能入口分组 ===== -->
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
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// ===== 图片地址处理 =====
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
  now: '',
  // 接单状态：刷新后保留
  online: localStorage.getItem('delivery-online') !== '0',
  // 概览数据来自接口：待接单 / 配送中 / 已完成
  stats: {
    pending: 0,
    delivering: 0,
    completed: 0
  },
  // 待接单列表预览（用户端的订单数量与配送信息）
  pendingList: [],
  // 我名下正在配送的订单
  deliveringList: []
})

// ===== 工作台数据 =====
const loadStats = () => {
  request.get('/orders/deliveryStats', {
    params: { deliveryId: data.user.id }
  }).then(res => {
    if (res.data) {
      data.stats.pending = res.data.pending ?? 0
      data.stats.delivering = res.data.delivering ?? 0
      data.stats.completed = res.data.completed ?? 0
    }
  })
}

// 待接单：用户端所有「待接单」的外送订单
const loadPending = () => {
  request.get('/orders/selectAllWithDetail', {
    params: { status: '待接单', deliverType: '外送', unassigned: true }
  }).then(res => {
    data.pendingList = (res.data || []).slice(0, 5)
  })
}

// 配送中：我自己名下的订单
const loadDelivering = () => {
  request.get('/orders/selectAllWithDetail', {
    params: {
      deliveryId: data.user.id,
      statuses: '配送中,待收货,已送达'
    }
  }).then(res => {
    data.deliveringList = res.data || []
  })
}

const loadAll = () => {
  if (!data.user?.id) return
  loadStats()
  loadPending()
  loadDelivering()
}
loadAll()

const menuGroups = [
  {
    title: '配送任务',
    items: [
      { name: '待接单',   desc: '查看附近待接订单',   icon: '📥', path: '/delivery/pending',    bg: '#fff1e0' },
      { name: '配送中',   desc: '正在进行中的订单',   icon: '🛵', path: '/delivery/delivering', bg: '#e8f4ff' },
      { name: '已完成',   desc: '历史配送订单记录',   icon: '✅', path: '/delivery/completed',  bg: '#e9f8ec' }
    ]
  },
  {
    title: '消息',
    items: [
      { name: '消息通知', desc: '系统通知与派单提醒', icon: '🔔', path: '/delivery/message',  bg: '#e5fff5' }
    ]
  },
  {
    title: '个人中心',
    items: [
      { name: '个人信息', desc: '头像与资料修改',   icon: '👤', path: '/delivery/person',   bg: '#e5e5ff' },
      { name: '修改密码', desc: '更新登录密码',     icon: '🔑', path: '/delivery/password', bg: '#ffe5d5' },
      { name: '退出系统', desc: '安全退出外卖员端', icon: '🚪', path: '__logout__',         bg: '#ffd5d5' }
    ]
  }
]

const go = (path) => {
  if (path === '__logout__') {
    localStorage.removeItem('system-user')
    localStorage.removeItem('delivery-online')
    router.push('/login')
    return
  }
  router.push(path)
}

const toggleOnline = () => {
  data.online = !data.online
  localStorage.setItem('delivery-online', data.online ? '1' : '0')
  ElMessage.success(data.online ? '已开启接单' : '已暂停接单')
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
  data.now = formatNow()
  timer = setInterval(() => {
    data.now = formatNow()
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
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
  background: linear-gradient(120deg, #d9701a 0%, #e8862f 55%, #f0a04b 100%);
  box-shadow: 0 16px 40px rgba(217, 112, 26, 0.25);
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
  background: rgba(255, 255, 255, 0.1);
}

.welcome-banner__glow--2 {
  width: 140px;
  height: 140px;
  right: 140px;
  bottom: -80px;
  background: rgba(255, 235, 200, 0.16);
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
  border: 2px solid rgba(255, 255, 255, 0.55);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.2);
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
  color: rgba(255, 255, 255, 0.78);
}

.welcome-banner__actions {
  display: flex;
  gap: 10px;
  position: relative;
  z-index: 1;
  flex-wrap: wrap;
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
  color: #d9701a;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, background 0.2s;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.16);
}

.welcome-banner__btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.22);
}

.welcome-banner__btn--ghost {
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.28);
}

.welcome-banner__btn--ghost:hover {
  background: rgba(255, 255, 255, 0.26);
}

/* 接单状态按钮 */
.welcome-banner__btn--status {
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.28);
  box-shadow: none;
  user-select: none;
}

.welcome-banner__btn--status:hover {
  background: rgba(255, 255, 255, 0.26);
}

.welcome-banner__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ffd7a8;
  transition: background 0.2s, box-shadow 0.2s;
}

.welcome-banner__btn--status.is-online .welcome-banner__dot {
  background: #7ff09a;
  box-shadow: 0 0 0 3px rgba(127, 240, 154, 0.28);
}

/* ==================== 今日概览 ==================== */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  background: #fff;
  border: 1px solid var(--line, #f0e6da);
  border-radius: var(--radius, 12px);
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
  transition: transform 0.22s, box-shadow 0.22s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(217, 112, 26, 0.1);
}

.stat-card__icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 21px;
  flex-shrink: 0;
}

.stat-card__text {
  min-width: 0;
}

.stat-card__value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text, #2b2118);
  line-height: 1.2;
}

.stat-card__label {
  margin-top: 2px;
  font-size: 12px;
  color: var(--text-3, #a2917e);
}

/* ==================== 待接单 / 我的配送面板 ==================== */
.home-panels {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.home-panel {
  background: #fff;
  border: 1px solid var(--line, #f0e6da);
  border-radius: var(--radius, 12px);
  padding: 16px;
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
}

.home-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.home-panel__title {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--text, #2b2118);
}

.home-panel__empty {
  padding: 24px 0;
  text-align: center;
  font-size: 12.5px;
  color: var(--text-3, #a2917e);
}

.home-panel__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mini-order {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fdf8f2;
  cursor: pointer;
  transition: background 0.2s;
}

.mini-order:hover {
  background: #fbf1e6;
}

.mini-order__main {
  min-width: 0;
}

.mini-order__no {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text, #2b2118);
}

.mini-order__addr {
  margin-top: 3px;
  font-size: 12px;
  color: var(--text-3, #a2917e);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 260px;
}

.mini-order__right {
  text-align: right;
  flex-shrink: 0;
}

.mini-order__money {
  display: block;
  font-size: 13.5px;
  font-weight: 700;
  color: #d9701a;
}

.mini-order__time {
  display: block;
  margin-top: 2px;
  font-size: 11.5px;
  color: var(--text-3, #a2917e);
}

@media (max-width: 900px) {
  .home-panels {
    grid-template-columns: 1fr;
  }
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
  background: #d9701a;
}

.home-group__title {
  font-size: 15.5px;
  font-weight: 600;
  color: var(--text, #2b2118);
}

.home-group__count {
  font-size: 11px;
  color: var(--text-3, #a2917e);
  background: #f7f0e8;
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
  border: 1px solid var(--line, #f0e6da);
  border-radius: var(--radius, 12px);
  cursor: pointer;
  transition: transform 0.22s cubic-bezier(0.22, 0.61, 0.36, 1), box-shadow 0.22s, border-color 0.22s;
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
  position: relative;
  overflow: hidden;
}

.home-tile:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(217, 112, 26, 0.12);
  border-color: #eed6bd;
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
  color: var(--text, #2b2118);
  margin-bottom: 3px;
}

.home-tile__desc {
  font-size: 12px;
  color: var(--text-3, #a2917e);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-tile__arrow {
  color: #dccbb8;
  font-size: 14px;
  transition: transform 0.2s, color 0.2s;
  flex-shrink: 0;
}

.home-tile:hover .home-tile__arrow {
  transform: translateX(3px);
  color: #d9701a;
}

.home-tile.is-danger:hover .home-tile__arrow {
  color: #f64848;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1200px) {
  .home-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .stat-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
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
  .stat-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
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