<template>
  <div class="manager-layout" :class="{ 'is-collapsed': data.collapse }">
    <!-- ===== 左侧导航 ===== -->
    <aside class="manager-aside">
      <div class="manager-brand" @click="router.push('/manager/home')">
        <img src="@/assets/imgs/logo.png" alt="logo" />
        <div v-show="!data.collapse" class="manager-brand__text">
          <div class="manager-brand__name">校园小卖部</div>
          <div class="manager-brand__sub">管理端</div>
        </div>
      </div>

      <el-scrollbar class="manager-menu-wrap">
        <el-menu
            router
            :collapse="data.collapse"
            :collapse-transition="false"
            :default-active="router.currentRoute.value.path"
            :default-openeds="['user','info']"
            background-color="transparent"
            text-color="#c3c7d6"
            active-text-color="#ffffff"
            class="manager-menu"
        >
          <el-menu-item index="/manager/home">
            <el-icon><HomeFilled /></el-icon>
            <template #title>系统首页</template>
          </el-menu-item>
          <el-menu-item index="/manager/dataManager">
            <el-icon><TrendCharts /></el-icon>
            <template #title>数据统计</template>
          </el-menu-item>
          <el-sub-menu index="info">
            <template #title>
              <el-icon><Memo /></el-icon>
              <span>信息管理</span>
            </template>
            <el-menu-item index="/manager/orders">
              <el-icon><Document /></el-icon>
              <template #title>订单信息</template>
            </el-menu-item>
            <el-menu-item index="/manager/category">
              <el-icon><PriceTag /></el-icon>
              <template #title>商品分类</template>
            </el-menu-item>
            <el-menu-item index="/manager/goods">
              <el-icon><Goods /></el-icon>
              <template #title>商品信息</template>
            </el-menu-item>
            <el-menu-item index="/manager/carousel">
              <el-icon><PictureFilled /></el-icon>
              <template #title>轮播图信息</template>
            </el-menu-item>
            <el-menu-item index="/manager/comment">
              <el-icon><ChatLineSquare /></el-icon>
              <template #title>订单评价</template>
            </el-menu-item>
            <el-menu-item index="/manager/collect">
              <el-icon><Star /></el-icon>
              <template #title>用户收藏</template>
            </el-menu-item>
            <el-menu-item index="/manager/recharge">
              <el-icon><Wallet /></el-icon>
              <template #title>用户充值</template>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/manager/user">
              <el-icon><Avatar /></el-icon>
              <template #title>普通用户</template>
            </el-menu-item>
            <el-menu-item index="/manager/delivery">
              <el-icon><Avatar /></el-icon>
              <template #title>外卖员管理</template>
            </el-menu-item>
            <el-menu-item index="/manager/admin">
              <el-icon><UserFilled /></el-icon>
              <template #title>管理员信息</template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/manager/person">
            <el-icon><Postcard /></el-icon>
            <template #title>个人信息</template>
          </el-menu-item>
          <el-menu-item index="/manager/password">
            <el-icon><Lock /></el-icon>
            <template #title>修改密码</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>

      <div class="manager-aside__foot">
        <div class="manager-logout" @click="logout">
          <el-icon><SwitchButton /></el-icon>
          <span v-show="!data.collapse">退出系统</span>
        </div>
      </div>
    </aside>

    <!-- ===== 右侧主体 ===== -->
    <section class="manager-body">
      <header class="manager-topbar">
        <div class="manager-topbar__left">
          <button class="manager-collapse" type="button" @click="data.collapse = !data.collapse">
            <el-icon><Fold v-if="!data.collapse" /><Expand v-else /></el-icon>
          </button>
          <div class="manager-breadcrumb">
            <span class="manager-breadcrumb__root">管理后台</span>
            <el-icon class="manager-breadcrumb__sep"><ArrowRight /></el-icon>
            <span class="manager-breadcrumb__current">{{ pageTitle }}</span>
          </div>
        </div>

        <div class="manager-topbar__right">
          <el-dropdown trigger="click">
            <div class="manager-user">
              <img
                  class="manager-user__avatar"
                  :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                  alt=""
              />
              <span class="manager-user__name">{{ data.user.name || '代码小白' }}</span>
              <el-icon class="manager-user__arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click.native="router.push('/manager/person')">
                  <el-icon><Postcard /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item @click.native="router.push('/manager/password')">
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

      <main class="manager-content">
        <router-view @updateUser="updateUser" />
      </main>
    </section>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from "vue";
import router from "@/router";
import {ElMessage} from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  collapse: false
})

const pageTitles = {
  '/manager/home': '系统首页',
  '/manager/dataManager': '数据统计',
  '/manager/orders': '订单信息',
  '/manager/category': '商品分类',
  '/manager/goods': '商品信息',
  '/manager/carousel': '轮播图信息',
  '/manager/comment': '订单评价',
  '/manager/collect': '用户收藏',
  '/manager/recharge': '用户充值',
  '/manager/user': '普通用户',
  '/manager/delivery': '外卖员',
  '/manager/admin': '管理员信息',
  '/manager/person': '个人信息',
  '/manager/password': '修改密码'
}

const pageTitle = computed(() => pageTitles[router.currentRoute.value.path] || '管理后台')

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
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
.manager-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg, #f5f6fa);
  --aside-width: 224px;
}

/* ================= 侧边栏 ================= */
.manager-aside {
  width: var(--aside-width);
  flex-shrink: 0;
  background: linear-gradient(180deg, #2e3143 0%, #262938 100%);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  transition: width 0.24s cubic-bezier(0.22, 0.61, 0.36, 1);
  overflow: hidden;
  z-index: 20;
}

.is-collapsed .manager-aside {
  width: 64px;
}

.manager-brand {
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

.manager-brand img {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.manager-brand__text {
  min-width: 0;
}

.manager-brand__name {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.5px;
  line-height: 1.2;
}

.manager-brand__sub {
  margin-top: 2px;
  font-size: 11px;
  color: #9aa0b5;
  letter-spacing: 2px;
}

.manager-menu-wrap {
  flex: 1;
  min-height: 0;
  padding: 10px 8px;
}

.manager-menu {
  border-right: none;
}

.manager-menu:not(.el-menu--collapse) {
  width: 100%;
}

.manager-menu :deep(.el-menu-item),
.manager-menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 3px 0;
  border-radius: 9px;
  font-size: 14px;
}

.manager-menu :deep(.el-menu-item:hover),
.manager-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.07) !important;
  color: #fff !important;
}

.manager-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #5b6187 0%, #4a5072 100%) !important;
  color: #fff !important;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.18);
}

.manager-menu :deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

.manager-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: 0;
  padding-left: 44px !important;
  font-size: 13px;
}

.manager-aside__foot {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.manager-logout {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 40px;
  border-radius: 9px;
  color: #b9bdcb;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  white-space: nowrap;
}

.manager-logout:hover {
  background: rgba(246, 72, 72, 0.16);
  color: #ff8b8b;
}

/* ================= 顶部栏 ================= */
.manager-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.manager-topbar {
  height: 64px;
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid var(--line, #eceef3);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 22px;
  position: sticky;
  top: 0;
  z-index: 15;
}

.manager-topbar__left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.manager-collapse {
  width: 34px;
  height: 34px;
  border: 1px solid var(--line, #eceef3);
  background: #fff;
  border-radius: 9px;
  color: var(--text-2, #59607a);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 16px;
}

.manager-collapse:hover {
  color: var(--brand, #2e3143);
  border-color: #d8dbe7;
  background: #f7f8fc;
}

.manager-breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  min-width: 0;
}

.manager-breadcrumb__root {
  color: var(--text-3, #8c93a8);
}

.manager-breadcrumb__sep {
  font-size: 12px;
  color: #c6cbd8;
}

.manager-breadcrumb__current {
  color: var(--text, #1f2430);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.manager-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px 5px 5px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s;
}

.manager-user:hover {
  background: #f7f8fc;
}

.manager-user__avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--line, #eceef3);
}

.manager-user__name {
  font-size: 13.5px;
  color: var(--text, #1f2430);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.manager-user__arrow {
  font-size: 12px;
  color: var(--text-3, #8c93a8);
}

/* ================= 内容区 ================= */
.manager-content {
  flex: 1;
  padding: 20px 22px 28px;
  min-width: 0;
}

@media (max-width: 992px) {
  .manager-layout {
    --aside-width: 200px;
  }
  .manager-content {
    padding: 16px;
  }
  .manager-topbar {
    padding: 0 16px;
  }
}

@media (max-width: 640px) {
  .manager-aside {
    width: 64px;
  }
  .manager-brand {
    padding: 0 16px;
  }
  .manager-brand__text {
    display: none;
  }
  .manager-menu {
    --el-menu-base-level-padding: 14px;
  }
  .manager-breadcrumb__root,
  .manager-breadcrumb__sep {
    display: none;
  }
  .manager-user__name {
    display: none;
  }
  .manager-content {
    padding: 12px;
  }
}
</style>
