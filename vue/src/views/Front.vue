<script setup>
import { reactive, ref, nextTick, onMounted, onBeforeUnmount } from "vue";
import router from "@/router";
import { ElMessage } from "element-plus";

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

// ===== AI 服务地址：开发走 Vite proxy（/api/ai -> localhost:9090），生产用 VITE_AI_BASE_URL =====
const AI_BASE = import.meta.env.DEV
    ? '/api/ai'
    : (import.meta.env.VITE_AI_BASE_URL || '/api/ai')

//主页面
const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}
const search = () => {
  if (data.goodsName) {
    router.push('/front/goods?name=' + data.goodsName)
    data.goodsName = null
  }
}
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  goodsName: null
})

// 顶部导航（仅用于展示，路由跳转逻辑与原来一致）
const navList = [
  { path: '/front/home', name: '首页', icon: 'HomeFilled' },
  { path: '/front/goods', name: '精选商品', icon: 'Goods' },
  { path: '/front/cart', name: '购物车', icon: 'ShoppingCart' },
  { path: '/front/myAddress', name: '我的地址', icon: 'Location' },
  { path: '/front/userOrders', name: '我的订单', icon: 'Tickets' }
]
const isNavActive = (path) => {
  const current = router.currentRoute.value.path
  if (path === '/front/goods') {
    return current === '/front/goods' || current === '/front/goodsDetail'
  }
  return current === path
}

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}
const logout = () => {
  localStorage.removeItem('system-user')
  router.push('/login')
  ElMessage.success("退出成功")
}

//悬浮头像
const showLove = ref(true)
const floatRef = ref(null)
const isDragging = ref(false)

let dragging = false
let startX = 0
let startY = 0
let startLeft = 0
let startTop = 0

const startDrag = (e) => {
  e.preventDefault()
  const el = floatRef.value
  if (!el) return

  dragging = true
  isDragging.value = false // 先假设是点击

  const clientX = e.clientX || e.touches[0].clientX
  const clientY = e.clientY || e.touches[0].clientY

  startX = clientX
  startY = clientY

  const rect = el.getBoundingClientRect()
  el.style.right = 'auto'
  el.style.bottom = 'auto'
  el.style.left = rect.left + 'px'
  el.style.top = rect.top + 'px'

  startLeft = rect.left
  startTop = rect.top

  document.onmousemove = onDrag
  document.onmouseup = stopDrag
  document.ontouchmove = onDrag
  document.ontouchend = stopDrag
}

const onDrag = (e) => {
  if (!dragging) return

  // 移动超过 5px 就认为是拖拽，不是点击
  const clientX = e.clientX || (e.touches && e.touches[0].clientX)
  const clientY = e.clientY || (e.touches && e.touches[0].clientY)

  const dx = Math.abs(clientX - startX)
  const dy = Math.abs(clientY - startY)

  if (dx > 5 || dy > 5) {
    isDragging.value = true
  }

  const el = floatRef.value
  el.style.left = startLeft + (clientX - startX) + 'px'
  el.style.top = startTop + (clientY - startY) + 'px'
}

const stopDrag = () => {
  dragging = false
  document.onmousemove = null
  document.onmouseup = null
  document.ontouchmove = null
  document.ontouchend = null

  //  拖拽结束后短暂延迟恢复 popover
  setTimeout(() => {
    isDragging.value = false
  }, 100)
}

// ==================== AI 对话 ====================
const chatVisible = ref(false)
const chatInput = ref('')
const chatListRef = ref(null)
const popoverVisible = ref(false)

const chat = reactive({
  messages: [
    { role: 'ai', content: '你好，我是校园小卖部 AI 小助手，有什么可以帮你的吗？' }
  ],
  loading: false
})

const chatId = ref('')
let eventSource = null

// 历史记录相关
const historyVisible = ref(false)       // 历史抽屉是否显示
const historyList = ref([])             // 历史会话列表 [{id, title, time, messages}]

// 存储 key（按用户隔离）
const getHistoryKey = () => `ai-history-${data.user?.id || 'guest'}`
const getSessionKey = () => `ai-session-${data.user?.id || 'guest'}`

// ==================== 初始化 ====================
onMounted(() => {
  // 1. 登录标记（每次登录都会生成新的 loginToken）
  const loginToken = localStorage.getItem('login-token')
  const lastSession = localStorage.getItem(getSessionKey())

  // 2. 如果是新登录 → 清空 AI 历史 + 当前会话
  if (loginToken && loginToken !== lastSession) {
    localStorage.setItem(getSessionKey(), loginToken)
    localStorage.removeItem(getHistoryKey())
    localStorage.removeItem(`ai-current-${data.user?.id || 'guest'}`)
  }

  // 3. 加载历史列表
  loadHistory()
})

// 生成新 chatId
const genChatId = () => 'chat-' + Date.now() + '-' + Math.random().toString(36).slice(2, 8)

// 加载历史列表
const loadHistory = () => {
  try {
    const raw = localStorage.getItem(getHistoryKey())
    historyList.value = raw ? JSON.parse(raw) : []
  } catch {
    historyList.value = []
  }
}

// 保存历史列表
const saveHistory = () => {
  localStorage.setItem(getHistoryKey(), JSON.stringify(historyList.value))
}

// 保存当前会话到历史（关闭或切换会话时调用）
const persistCurrentChat = () => {
  if (!chatId.value) return
  // 过滤掉只有初始欢迎语的情况
  const realMessages = chat.messages.filter(m => !(m.role === 'ai' && m.content.includes('有什么可以帮你的吗')))
  if (realMessages.length === 0) return

  const firstUser = chat.messages.find(m => m.role === 'user')
  const title = firstUser ? firstUser.content.slice(0, 20) : '新会话'

  const idx = historyList.value.findIndex(h => h.id === chatId.value)
  const record = {
    id: chatId.value,
    title,
    time: Date.now(),
    messages: JSON.parse(JSON.stringify(chat.messages))
  }
  if (idx >= 0) {
    historyList.value[idx] = record
  } else {
    historyList.value.unshift(record)
  }
  // 最多保留 20 条
  if (historyList.value.length > 20) historyList.value = historyList.value.slice(0, 20)
  saveHistory()
}

// 打开对话
const openChat = () => {
  popoverVisible.value = false
  chatVisible.value = true

  // 如果没 chatId，生成一个
  if (!chatId.value) {
    chatId.value = genChatId()
  }
  scrollToBottom()
}

// 关闭对话
const closeChat = () => {
  // 关闭前先存一次历史
  persistCurrentChat()

  chatVisible.value = false
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  chat.loading = false
}

// ==================== 新会话 ====================
const newChat = () => {
  // 先存当前会话
  persistCurrentChat()

  // 关闭旧连接
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  // 重置对话
  chat.messages = [
    { role: 'ai', content: '你好，我是校园小卖部 AI 小助手,我叫小米，有什么可以帮你的吗？' }
  ]
  chat.loading = false
  chatId.value = genChatId()
  chatInput.value = ''
  historyVisible.value = false
  scrollToBottom()
  ElMessage.success('已开启新会话')
}

// ==================== 查看历史 ====================
const openHistory = () => {
  loadHistory()
  historyVisible.value = true
}

const closeHistory = () => {
  historyVisible.value = false
}

// 切换到某个历史会话（只读查看，或继续对话）
const switchToHistory = (item) => {
  // 先保存当前
  persistCurrentChat()

  // 关闭连接
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  // 载入历史
  chat.messages = JSON.parse(JSON.stringify(item.messages))
  chatId.value = item.id
  chat.loading = false
  historyVisible.value = false
  scrollToBottom()
}

// 删除某条历史
const deleteHistory = (id) => {
  historyList.value = historyList.value.filter(h => h.id !== id)
  saveHistory()
  ElMessage.success('已删除')
}

// ==================== 滚动 ====================
const scrollToBottom = () => {
  nextTick(() => {
    const el = chatListRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

// ==================== 发送消息（SSE） ====================
const sendMessage = async () => {
  const text = chatInput.value.trim()
  if (!text || chat.loading) return

  chat.messages.push({ role: 'user', content: text })
  chatInput.value = ''
  scrollToBottom()

  const aiMsg = reactive({ role: 'ai', content: '' })
  chat.messages.push(aiMsg)
  chat.loading = true
  scrollToBottom()

  // 走相对路径：开发环境经 Vite proxy 转发到 localhost:9090，手机/别人访问也能通
  const url = `${AI_BASE}/chat?prompt=${encodeURIComponent(text)}&chatId=${chatId.value}`
  eventSource = new EventSource(url)

  eventSource.onmessage = (event) => {
    const data = event.data
    if (!data || data === '[DONE]') return
    aiMsg.content += data
    scrollToBottom()
  }

  eventSource.onerror = () => {
    if (!aiMsg.content) {
      aiMsg.content = 'AI 服务连接失败，请稍后再试'
    }
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    chat.loading = false
  }

  eventSource.addEventListener('complete', () => {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    chat.loading = false
    scrollToBottom()
  })
}

// ==================== 卸载清理 ====================
onBeforeUnmount(() => {
  persistCurrentChat()
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
})

const handleChatClick = (e) => {
  const target = e.target
  if (target && target.classList.contains('goods-link')) {
    const id = target.getAttribute('data-id')
    router.push('/front/goodsDetail?id=' + id)
  }
}

// 把 AI 文本里的 [商品:名称|id:123] 渲染成可点击的 span
const renderMessage = (content) => {
  if (!content) return ''

  // 1. 先把标记换成占位符（不进入最终 HTML）
  let temp = content.replace(
      /\[商品:(.+?)\|id:(\d+)\]/g,
      (match, name, id) => `@@GOODS_${id}@@${name}@@END@@`
  )

  // 2. 转义防 XSS
  let safe = temp
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')

  // 3. 占位符还原成只含商品名的 span，id 放在 data-id 属性里（用户看不到）
  safe = safe.replace(
      /@@GOODS_(\d+)@@(.+?)@@END@@/g,
      (match, id, name) => `<span class="goods-link" data-id="${id}">${name}</span>`
  )

  // 4. 流式过程中未闭合的标记先隐藏
  safe = safe.replace(/\[商品:[^\]]*$/g, '')

  safe = safe.replace(/\n/g, '<br/>')
  return safe
}
</script>

<template>
  <div class="front-layout">
    <!-- ==================== 顶部导航 ==================== -->
    <header class="front-header">
      <div class="front-header__inner">
        <router-link class="front-brand" to="/front/home">
          <img src="@/assets/imgs/logo.png" alt="logo" />
          <span class="front-brand__name">校园小卖部</span>
        </router-link>

        <nav class="front-nav">
          <router-link
              v-for="item in navList"
              :key="item.path"
              :to="item.path"
              class="front-nav__item"
              :class="{ 'is-active': isNavActive(item.path) }"
          >
            <el-icon class="front-nav__icon"><component :is="item.icon" /></el-icon>
            <span>{{ item.name }}</span>
          </router-link>
        </nav>

        <div class="front-search" v-if="router.currentRoute.value.path !== '/front/goods'">
          <el-input
              v-model="data.goodsName"
              placeholder="搜索商品"
              clearable
              @keyup.enter="search"
              @clear="data.goodsName = null"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #suffix>
              <span class="front-search__btn" @click="search">搜索</span>
            </template>
          </el-input>
        </div>

        <el-dropdown trigger="click" class="front-user-dropdown">
          <div class="front-user">
            <img
                class="front-user__avatar"
                :src="data.user.avatar ? formatImg(data.user.avatar) : DEFAULT_AVATAR"
                alt=""
                @error="(e) => (e.target.src = DEFAULT_AVATAR)"
            />
            <div class="front-user__info">
              <span class="front-user__name">{{ data.user.name || '代码小白' }}</span>
              <span class="front-user__balance" v-if="data.user.account !== undefined">余额 ￥{{ data.user.account }}</span>
            </div>
            <el-icon class="front-user__arrow"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click.native="router.push('/front/userRecharge')">
                <el-icon><Wallet /></el-icon>我的充值
              </el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/userCollect')">
                <el-icon><Star /></el-icon>我的收藏
              </el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/userComment')">
                <el-icon><ChatLineSquare /></el-icon>我的评价
              </el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/person')">
                <el-icon><Postcard /></el-icon>个人信息
              </el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/password')">
                <el-icon><Lock /></el-icon>修改密码
              </el-dropdown-item>
              <el-dropdown-item divided @click.native="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- ==================== 页面内容 ==================== -->
    <main class="front-main">
      <router-view @updateUser="updateUser" />
    </main>

    <Footer />

    <!-- ==================== 悬浮 AI 助手 ==================== -->
    <el-popover
        v-model:visible="popoverVisible"
        :disabled="isDragging"
        placement="top-end"
        :width="250"
        trigger="click"
        popper-class="ai-float-popper"
    >
      <template #reference>
        <div v-if="showLove" class="ai-float" ref="floatRef" @mousedown="startDrag" @touchstart="startDrag">
          <img src="@/assets/imgs/AI.jpg" class="ai-float__img" alt="AI 小助手" />
          <span class="ai-float__dot"></span>
        </div>
      </template>

      <div class="ai-float-card">
        <div class="ai-float-card__title">
          <span class="ai-float-card__avatar">🤖</span>
          <div>
            <div class="ai-float-card__name">AI 小助手</div>
            <div class="ai-float-card__status"><i></i>在线</div>
          </div>
        </div>
        <p class="ai-float-card__desc">请问有什么需要帮助的吗？可以问我商品推荐、价格和库存哦～</p>
        <button class="ai-float-card__btn" @click="openChat">点击提问</button>
      </div>
    </el-popover>

    <!-- ==================== AI 对话框 ==================== -->
    <transition name="ai-panel">
      <div v-if="chatVisible" class="ai-chat-panel">
        <div class="ai-chat-header">
          <div class="ai-chat-header__title">
            <img src="@/assets/imgs/AI.jpg" alt="" />
            <div>
              <div class="ai-chat-header__name">AI 小助手</div>
              <div class="ai-chat-header__sub">校园小卖部 · 智能导购</div>
            </div>
          </div>
          <div class="ai-chat-header-actions">
            <span class="ai-chat-icon-btn" title="查看历史记录" @click="openHistory">🕘</span>
            <span class="ai-chat-icon-btn" title="开启新会话" @click="newChat">＋</span>
            <span class="ai-chat-close" @click="closeChat">×</span>
          </div>
        </div>

        <!-- 历史记录抽屉 -->
        <div v-if="historyVisible" class="ai-history-drawer">
          <div class="ai-history-title">
            <span>历史会话</span>
            <span class="ai-chat-close" @click="closeHistory">×</span>
          </div>
          <div class="ai-history-list">
            <div v-if="historyList.length === 0" class="ai-history-empty">
              <span>🗂</span>
              <p>暂无历史记录</p>
            </div>
            <div
                v-for="item in historyList"
                :key="item.id"
                class="ai-history-item"
                @click="switchToHistory(item)"
            >
              <div class="ai-history-item-title">{{ item.title }}</div>
              <div class="ai-history-item-time">
                {{ new Date(item.time).toLocaleString() }}
              </div>
              <span class="ai-history-item-del" @click.stop="deleteHistory(item.id)">删除</span>
            </div>
          </div>
        </div>

        <div class="ai-chat-body" ref="chatListRef">
          <div v-for="(msg, idx) in chat.messages"
               :key="idx"
               class="ai-chat-msg"
               :class="msg.role === 'user' ? 'is-user' : 'is-ai'">
            <template v-if="msg.role === 'ai'">
              <img class="ai-chat-avatar" src="@/assets/imgs/AI.jpg" alt="" />
            </template>
            <div
                v-if="msg.role === 'ai'"
                class="ai-chat-bubble"
                v-html="renderMessage(msg.content)"
                @click="handleChatClick"
            ></div>
            <div
                v-else
                class="ai-chat-bubble"
            >{{ msg.content }}</div>
            <template v-if="msg.role === 'user'">
              <img class="ai-chat-avatar"
                   :src="data.user.avatar ? formatImg(data.user.avatar) : DEFAULT_AVATAR"
                   alt=""
                   @error="(e) => (e.target.src = DEFAULT_AVATAR)" />
            </template>
          </div>
          <div v-if="chat.loading" class="ai-chat-msg is-ai">
            <img class="ai-chat-avatar" src="@/assets/imgs/AI.jpg" alt="" />
            <div class="ai-chat-bubble ai-chat-typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <div class="ai-chat-footer">
          <input
              v-model="chatInput"
              class="ai-chat-input"
              placeholder="请输入你的问题…"
              @keyup.enter="sendMessage" />
          <button class="ai-chat-send" @click="sendMessage" :disabled="chat.loading">发送</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
/* ==================== 整体骨架 ==================== */
.front-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg, #f5f6fa);
}

.front-main {
  flex: 1;
}

/* ==================== 顶部导航 ==================== */
.front-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(46, 49, 67, 0.97);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 16px rgba(20, 24, 40, 0.12);
}

.front-header__inner {
  max-width: 1240px;
  margin: 0 auto;
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 22px;
}

.front-brand {
  display: flex;
  align-items: center;
  gap: 9px;
  flex-shrink: 0;
  text-decoration: none;
}

.front-brand img {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  object-fit: cover;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.12);
}

.front-brand__name {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  white-space: nowrap;
}

.front-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
}

.front-nav::-webkit-scrollbar {
  display: none;
}

.front-nav__item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 14px;
  border-radius: 9px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
  text-decoration: none;
  white-space: nowrap;
  transition: background 0.2s, color 0.2s;
}

.front-nav__item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.front-nav__item.is-active {
  color: #fff;
  background: rgba(255, 255, 255, 0.14);
  font-weight: 600;
}

.front-nav__icon {
  font-size: 15px;
}

/* 搜索框 */
.front-search {
  margin-left: auto;
  width: 240px;
  flex-shrink: 0;
}

.front-search :deep(.el-input__wrapper) {
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  box-shadow: none !important;
  padding-left: 14px;
  transition: background 0.2s;
}

.front-search :deep(.el-input__wrapper:hover),
.front-search :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.16);
}

.front-search :deep(.el-input__inner) {
  color: #fff;
  font-size: 13px;
}

.front-search :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

.front-search :deep(.el-input__prefix),
.front-search :deep(.el-input__suffix) {
  color: rgba(255, 255, 255, 0.65);
}

.front-search__btn {
  color: #ffd166;
  font-size: 12.5px;
  cursor: pointer;
  padding-left: 8px;
  white-space: nowrap;
}

.front-search__btn:hover {
  color: #ffe6a6;
}

/* 用户 */
.front-user {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 5px 8px 5px 5px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s;
  outline: none;
}

.front-user:hover {
  background: rgba(255, 255, 255, 0.08);
}

.front-user__avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.2);
  flex-shrink: 0;
}

.front-user__info {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
  min-width: 0;
}

.front-user__name {
  color: #fff;
  font-size: 13px;
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.front-user__balance {
  color: #ffd166;
  font-size: 11px;
  white-space: nowrap;
}

.front-user__arrow {
  color: rgba(255, 255, 255, 0.55);
  font-size: 12px;
}

/* ==================== 悬浮球 ==================== */
.ai-float {
  position: fixed;
  right: 24px;
  bottom: 28px;
  width: 58px;
  height: 58px;
  border-radius: 50%;
  cursor: grab;
  z-index: 9999;
  user-select: none;
  -webkit-user-drag: none;
  box-shadow: 0 10px 26px rgba(20, 24, 40, 0.28);
  transition: transform 0.25s cubic-bezier(0.22, 0.61, 0.36, 1), box-shadow 0.25s;
  background: #fff;
  padding: 2px;
}

.ai-float:hover {
  transform: scale(1.06) translateY(-2px);
  box-shadow: 0 14px 32px rgba(20, 24, 40, 0.34);
}

.ai-float:active {
  cursor: grabbing;
}

.ai-float__img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  display: block;
  -webkit-user-drag: none;
  pointer-events: none;
}

.ai-float__dot {
  position: absolute;
  right: 3px;
  top: 4px;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #34d399;
  border: 2px solid #fff;
  animation: ai-pulse 2s infinite;
}

@keyframes ai-pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.55);
  }
  50% {
    box-shadow: 0 0 0 5px rgba(52, 211, 153, 0);
  }
}

/* 悬浮卡片（popover 内） */
.ai-float-card__title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.ai-float-card__avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f1f3f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
}

.ai-float-card__name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text, #1f2430);
}

.ai-float-card__status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #34d399;
  margin-top: 1px;
}

.ai-float-card__status i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #34d399;
  display: inline-block;
}

.ai-float-card__desc {
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-2, #59607a);
}

.ai-float-card__btn {
  width: 100%;
  height: 36px;
  border: none;
  border-radius: 9px;
  background: var(--brand, #2e3143);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}

.ai-float-card__btn:hover {
  background: #41465f;
  transform: translateY(-1px);
}

/* ==================== AI 对话面板 ==================== */
.ai-chat-panel {
  position: fixed;
  right: 24px;
  bottom: 100px;
  width: 400px;
  max-width: calc(100vw - 32px);
  height: 540px;
  max-height: calc(100vh - 140px);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 24px 60px rgba(20, 24, 40, 0.22);
  display: flex;
  flex-direction: column;
  z-index: 10000;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.ai-panel-enter-active,
.ai-panel-leave-active {
  transition: opacity 0.22s ease, transform 0.22s cubic-bezier(0.22, 0.61, 0.36, 1);
  transform-origin: bottom right;
}

.ai-panel-enter-from,
.ai-panel-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.97);
}

.ai-chat-header {
  height: 62px;
  background: linear-gradient(135deg, #2e3143 0%, #3d4260 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  flex-shrink: 0;
}

.ai-chat-header__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-chat-header__title img {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.35);
}

.ai-chat-header__name {
  font-size: 14px;
  font-weight: 600;
}

.ai-chat-header__sub {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 2px;
}

.ai-chat-header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-chat-icon-btn {
  cursor: pointer;
  font-size: 17px;
  line-height: 1;
  user-select: none;
  opacity: 0.85;
  transition: opacity 0.2s, transform 0.2s;
}

.ai-chat-icon-btn:hover {
  opacity: 1;
  transform: translateY(-1px);
}

.ai-chat-close {
  cursor: pointer;
  font-size: 22px;
  line-height: 1;
  color: rgba(255, 255, 255, 0.8);
  transition: color 0.2s;
}

.ai-chat-close:hover {
  color: #ff8b8b;
}

.ai-chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 14px 8px;
  background: #f7f8fa;
  scroll-behavior: smooth;
}

.ai-chat-body::-webkit-scrollbar {
  width: 6px;
}

.ai-chat-body::-webkit-scrollbar-thumb {
  background: #d4d7e1;
  border-radius: 3px;
}

.ai-chat-msg {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 14px;
  animation: msg-in 0.25s ease;
}

@keyframes msg-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

.ai-chat-msg.is-user {
  justify-content: flex-end;
}

.ai-chat-msg.is-ai {
  justify-content: flex-start;
}

.ai-chat-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  flex-shrink: 0;
  object-fit: cover;
  border: 1px solid #e8eaf1;
  margin-top: 2px;
}

.ai-chat-bubble {
  max-width: 74%;
  padding: 10px 13px;
  border-radius: 12px;
  font-size: 13.5px;
  line-height: 1.65;
  word-break: break-word;
  white-space: pre-wrap;
}

.is-user .ai-chat-bubble {
  background: linear-gradient(135deg, #2e3143, #41465f);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 4px 12px rgba(46, 49, 67, 0.18);
}

.is-ai .ai-chat-bubble {
  background: #fff;
  color: #333;
  border: 1px solid #eef0f5;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(20, 24, 40, 0.04);
}

.ai-chat-typing {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 13px !important;
}

.ai-chat-typing span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #b6bac9;
  animation: typing 1.2s infinite ease-in-out;
}

.ai-chat-typing span:nth-child(2) {
  animation-delay: 0.15s;
}

.ai-chat-typing span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

.ai-chat-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-top: 1px solid #eef0f5;
  background: #fff;
  flex-shrink: 0;
}

.ai-chat-input {
  flex: 1;
  height: 40px;
  padding: 0 14px;
  border: 1px solid #e3e6ee;
  border-radius: 999px;
  outline: none;
  font-size: 13.5px;
  box-sizing: border-box;
  background: #fafbfd;
  transition: border-color 0.2s, background 0.2s;
}

.ai-chat-input:focus {
  border-color: #2e3143;
  background: #fff;
}

.ai-chat-send {
  padding: 0 18px;
  height: 40px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #2e3143, #41465f);
  color: #fff;
  cursor: pointer;
  font-size: 13.5px;
  transition: opacity 0.2s, transform 0.2s;
}

.ai-chat-send:hover {
  transform: translateY(-1px);
}

.ai-chat-send:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 历史记录抽屉 */
.ai-history-drawer {
  position: absolute;
  top: 62px;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.ai-history-title {
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  font-weight: 600;
  font-size: 14px;
  color: var(--text, #1f2430);
  border-bottom: 1px solid #eef0f5;
  background: #fafbfd;
  flex-shrink: 0;
}

.ai-history-title .ai-chat-close {
  color: #9aa0b2;
}

.ai-history-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.ai-history-empty {
  text-align: center;
  color: #9aa0b2;
  font-size: 13px;
  padding: 60px 0;
}

.ai-history-empty span {
  font-size: 30px;
  display: block;
  margin-bottom: 10px;
  opacity: 0.6;
}

.ai-history-item {
  position: relative;
  padding: 11px 12px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid #eef0f5;
  margin-bottom: 9px;
  transition: background 0.2s, border-color 0.2s, transform 0.2s;
  background: #fff;
}

.ai-history-item:hover {
  background: #f7f8fc;
  border-color: #dfe3ef;
  transform: translateX(2px);
}

.ai-history-item-title {
  font-size: 13.5px;
  color: #333;
  font-weight: 500;
  margin-bottom: 4px;
  padding-right: 42px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ai-history-item-time {
  font-size: 11.5px;
  color: #9aa0b2;
}

.ai-history-item-del {
  position: absolute;
  right: 10px;
  top: 12px;
  font-size: 11.5px;
  color: #b9bdca;
  padding: 2px 4px;
}

.ai-history-item-del:hover {
  color: #f64848;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1200px) {
  .front-header__inner {
    gap: 14px;
    padding: 0 16px;
  }
  .front-search {
    width: 200px;
  }
}

@media (max-width: 992px) {
  .front-brand__name {
    display: none;
  }
  .front-search {
    display: none;
  }
  .front-header__inner {
    height: 58px;
  }
  .front-nav {
    flex: 1;
  }
}

@media (max-width: 640px) {
  .front-header__inner {
    gap: 8px;
    padding: 0 12px;
  }
  .front-nav__item {
    padding: 0 10px;
    height: 32px;
    font-size: 13px;
  }
  .front-nav__icon {
    display: none;
  }
  .front-user__info {
    display: none;
  }
  .front-user {
    padding: 3px;
  }
  .ai-float {
    right: 16px;
    bottom: 18px;
    width: 52px;
    height: 52px;
  }
  .ai-chat-panel {
    right: 8px;
    left: 8px;
    bottom: 84px;
    width: auto;
    height: calc(100vh - 120px);
    max-height: none;
  }
}
</style>