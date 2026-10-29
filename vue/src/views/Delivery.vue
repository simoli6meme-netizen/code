<template>
  <div class="delivery-layout" :class="{ 'is-collapsed': data.collapse }">
    <!-- ===== 左侧导航 ===== -->
    <aside class="delivery-aside">
      <div class="delivery-brand" @click="router.push('/delivery/home')">
        <img src="@/assets/imgs/logo.png" alt="logo" />
        <div v-show="!data.collapse" class="delivery-brand__text">
          <div class="delivery-brand__name">校园小卖部</div>
          <div class="delivery-brand__sub">外卖员端</div>
        </div>
      </div>

      <el-scrollbar class="delivery-menu-wrap">
        <el-menu
            router
            :collapse="data.collapse"
            :collapse-transition="false"
            :default-active="router.currentRoute.value.path"
            :default-openeds="['order']"
            background-color="transparent"
            text-color="#e8d8c8"
            active-text-color="#ffffff"
            class="delivery-menu"
        >
          <el-menu-item index="/delivery/home">
            <el-icon><HomeFilled /></el-icon>
            <template #title>配送工作台</template>
          </el-menu-item>

          <el-sub-menu index="order">
            <template #title>
              <el-icon><List /></el-icon>
              <span>我的订单</span>
            </template>
            <el-menu-item index="/delivery/pending">
              <el-icon><Clock /></el-icon>
              <template #title>待接单</template>
            </el-menu-item>
            <el-menu-item index="/delivery/delivering">
              <el-icon><Van /></el-icon>
              <template #title>配送中</template>
            </el-menu-item>
            <el-menu-item index="/delivery/completed">
              <el-icon><CircleCheck /></el-icon>
              <template #title>已完成</template>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/delivery/person">
            <el-icon><Postcard /></el-icon>
            <template #title>个人信息</template>
          </el-menu-item>

          <el-menu-item index="/delivery/password">
            <el-icon><Lock /></el-icon>
            <template #title>修改密码</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>

      <div class="delivery-aside__foot">
        <div class="delivery-logout" @click="logout">
          <el-icon><SwitchButton /></el-icon>
          <span v-show="!data.collapse">退出系统</span>
        </div>
      </div>
    </aside>

    <!-- ===== 右侧主体 ===== -->
    <section class="delivery-body">
      <header class="delivery-topbar">
        <div class="delivery-topbar__left">
          <button class="delivery-collapse" type="button" @click="data.collapse = !data.collapse">
            <el-icon><Fold v-if="!data.collapse" /><Expand v-else /></el-icon>
          </button>
          <div class="delivery-breadcrumb">
            <span class="delivery-breadcrumb__root">外卖员工作台</span>
            <el-icon class="delivery-breadcrumb__sep"><ArrowRight /></el-icon>
            <span class="delivery-breadcrumb__current">{{ pageTitle }}</span>
          </div>
        </div>

        <div class="delivery-topbar__right">
          <!-- 接单状态开关，外卖员端常见 -->
          <div class="delivery-status" :class="{ 'is-online': data.online }" @click="toggleOnline">
            <span class="delivery-status__dot"></span>
            <span class="delivery-status__text">{{ data.online ? '接单中' : '休息中' }}</span>
          </div>

          <el-dropdown trigger="click">
            <div class="delivery-user">
              <img
                  class="delivery-user__avatar"
                  :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                  alt=""
              />
              <span class="delivery-user__name">{{ data.user.name || '骑手小哥' }}</span>
              <el-icon class="delivery-user__arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click.native="router.push('/delivery/person')">
                  <el-icon><Postcard /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item @click.native="router.push('/delivery/password')">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided @click.native="logout">
                  <el-icon><SwitchButton /></el-icon>退出系统
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="delivery-content">
        <router-view @updateUser="updateUser" />
      </main>
    </section>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from "vue";
import router from "@/router";
import { ElMessage } from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  collapse: false,
  online: true
})

const pageTitles = {
  '/delivery/home': '配送工作台',
  '/delivery/pending': '待接单',
  '/delivery/delivering': '配送中',
  '/delivery/completed': '已完成',
  '/delivery/person': '个人信息',
  '/delivery/password': '修改密码'
}

const pageTitle = computed(() => pageTitles[router.currentRoute.value.path] || '外卖员工作台')

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}

const toggleOnline = () => {
  data.online = !data.online
  ElMessage.success(data.online ? '已开启接单' : '已暂停接单')
}

const logout = () => {
  router.push('/login')
  ElMessage.success('退出成功')
  localStorage.removeItem('system-user')
}

// 小屏默认收起侧边栏，避免导航挤压内容
onMounted(() => {
  if (window.innerWidth < 768) {
    data.collapse = true
  }
})
</script>

<style scoped>
.delivery-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg, #fdf7f0);
  --aside-width: 224px;
}

/* ================= 侧边栏 ================= */
.delivery-aside {
  width: var(--aside-width);
  flex-shrink: 0;
  background: linear-gradient(180deg, #3a2b1e 0%, #2a1f16 100%);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  transition: width 0.24s cubic-bezier(0.22, 0.61, 0.36, 1);
  overflow: hidden;
  z-index: 20;
}

.is-collapsed .delivery-aside {
  width: 64px;
}

.delivery-brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  cursor: pointer;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;
  white-space: nowrap;
}

.delivery-brand img {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.delivery-brand__text {
  min-width: 0;
}

.delivery-brand__name {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.5px;
  line-height: 1.2;
}

.delivery-brand__sub {
  margin-top: 2px;
  font-size: 11px;
  color: #f0b67f;
  letter-spacing: 2px;
}

.delivery-menu-wrap {
  flex: 1;
  min-height: 0;
  padding: 10px 8px;
}

.delivery-menu {
  border-right: none;
}

.delivery-menu:not(.el-menu--collapse) {
  width: 100%;
}

.delivery-menu :deep(.el-menu-item),
.delivery-menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 3px 0;
  border-radius: 9px;
  font-size: 14px;
}

.delivery-menu :deep(.el-menu-item:hover),
.delivery-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.07) !important;
  color: #fff !important;
}

.delivery-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #f08a24 0%, #d9701a 100%) !important;
  color: #fff !important;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(217, 112, 26, 0.35);
}

.delivery-menu :deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

.delivery-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: 0;
  padding-left: 44px !important;
  font-size: 13px;
}

.delivery-aside__foot {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.delivery-logout {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 40px;
  border-radius: 9px;
  color: #d8c7b6;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  white-space: nowrap;
}

.delivery-logout:hover {
  background: rgba(246, 72, 72, 0.16);
  color: #ff8b8b;
}

/* ================= 顶部栏 ================= */
.delivery-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.delivery-topbar {
  height: 64px;
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid var(--line, #f0e6da);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 22px;
  position: sticky;
  top: 0;
  z-index: 15;
}

.delivery-topbar__left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.delivery-topbar__right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.delivery-collapse {
  width: 34px;
  height: 34px;
  border: 1px solid var(--line, #f0e6da);
  background: #fff;
  border-radius: 9px;
  color: var(--text-2, #7a6a5a);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 16px;
}

.delivery-collapse:hover {
  color: var(--brand, #d9701a);
  border-color: #eed6bd;
  background: #fdf3e9;
}

.delivery-breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  min-width: 0;
}

.delivery-breadcrumb__root {
  color: var(--text-3, #a2917e);
}

.delivery-breadcrumb__sep {
  font-size: 12px;
  color: #dccbb8;
}

.delivery-breadcrumb__current {
  color: var(--text, #2b2118);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 接单状态 ===== */
.delivery-status {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12.5px;
  cursor: pointer;
  user-select: none;
  background: #f2f2f2;
  color: #999;
  transition: all 0.2s;
}

.delivery-status__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #bbb;
  transition: background 0.2s;
}

.delivery-status.is-online {
  background: #e8f8ec;
  color: #2e9e4f;
}

.delivery-status.is-online .delivery-status__dot {
  background: #2e9e4f;
  box-shadow: 0 0 0 3px rgba(46, 158, 79, 0.18);
}

.delivery-status:hover {
  filter: brightness(0.97);
}

.delivery-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px 5px 5px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s;
}

.delivery-user:hover {
  background: #fdf3e9;
}

.delivery-user__avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--line, #f0e6da);
}

.delivery-user__name {
  font-size: 13.5px;
  color: var(--text, #2b2118);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delivery-user__arrow {
  font-size: 12px;
  color: var(--text-3, #a2917e);
}

/* ================= 内容区 ================= */
.delivery-content {
  flex: 1;
  padding: 20px 22px 28px;
  min-width: 0;
}

@media (max-width: 992px) {
  .delivery-layout {
    --aside-width: 200px;
  }
  .delivery-content {
    padding: 16px;
  }
  .delivery-topbar {
    padding: 0 16px;
  }
}

@media (max-width: 640px) {
  .delivery-aside {
    width: 64px;
  }
  .delivery-brand {
    padding: 0 16px;
  }
  .delivery-brand__text {
    display: none;
  }
  .delivery-menu {
    --el-menu-base-level-padding: 14px;
  }
  .delivery-breadcrumb__root,
  .delivery-breadcrumb__sep {
    display: none;
  }
  .delivery-user__name,
  .delivery-status__text {
    display: none;
  }
  .delivery-status {
    padding: 0 9px;
  }
  .delivery-content {
    padding: 12px;
  }
}
</style>