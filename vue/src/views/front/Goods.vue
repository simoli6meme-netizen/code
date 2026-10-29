<template>
  <div class="front-container goods-page">
    <div class="page-head">
      <div>
        <div class="page-title">精选商品</div>
        <div class="page-desc">校园小卖部好物，支持到店自提与配送到宿舍</div>
      </div>
    </div>

    <!-- 分类 + 搜索 -->
    <div class="goods-filter panel">
      <div class="goods-cats">
        <button
            class="cat-chip"
            :class="{ 'is-active': data.categoryId === null }"
            @click="loadByCategory(null)"
        >
          全部
        </button>

        <button
            v-for="item in data.categoryList.slice(0,8)"
            :key="item.id"
            class="cat-chip"
            :class="{ 'is-active': item.id === data.categoryId }"
            @click="loadByCategory(item.id)"
        >
          {{ item.name }}
        </button>

        <el-popover width="640" trigger="hover" placement="bottom">
          <template #reference>
            <button class="cat-chip cat-chip--more">
              更多
              <el-icon><ArrowDown /></el-icon>
            </button>
          </template>
          <div class="category-grid">
            <div
                v-for="item in data.categoryList"
                :key="item.id"
                :class="{'active': item.id === data.categoryId}"
                @click="loadByCategory(item.id)"
                class="category-item">
              {{ item.name }}
            </div>
          </div>
        </el-popover>
      </div>

      <div class="goods-search">
        <el-input
            v-model="data.name"
            clearable
            @clear="load"
            @keyup.enter="load"
            placeholder="搜索商品名称"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="load">搜索</el-button>
      </div>
    </div>

    <!-- 结果统计 -->
    <div class="goods-result">
      <span>共 <b>{{ data.total }}</b> 件商品</span>
      <span v-if="data.categoryId" class="goods-result__filter">
        已按分类筛选
        <span class="goods-result__clear" @click="loadByCategory(null)">清除</span>
      </span>
      <span v-if="data.name" class="goods-result__filter">
        关键词：{{ data.name }}
        <span class="goods-result__clear" @click="data.name = null; load()">清除</span>
      </span>
    </div>

    <!-- 商品列表 -->
    <div class="goods-grid" v-if="data.total > 0">
      <div class="goods-card" v-for="item in data.tableData" :key="item.id" @click="router.push('/front/goodsDetail?id=' + item.id)">
        <div class="goods-card__media-wrap">
          <img
              :src="formatImg(item.img)"
              alt=""
              class="goods-card__media"
              loading="lazy"
              @error="(e) => (e.target.src = PLACEHOLDER)"
          />
          <span class="goods-card__tag" v-if="item.recommend === '是'">推荐</span>
        </div>
        <div class="goods-card__body">
          <div class="goods-card__name">{{ item.name }}</div>
          <div class="goods-card__meta">
            <span class="price"><span class="price__symbol">￥</span>{{ item.price }}</span>
            <span class="goods-card__sales">已售 {{ item.saleCount }} 件</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="panel empty-state">
      <span class="empty-state__icon">🔍</span>
      <span>没有找到相关商品</span>
      <el-button size="small" plain @click="data.name = null; loadByCategory(null)">查看全部商品</el-button>
    </div>

    <!-- 分页 -->
    <div class="pager" v-if="data.total > 0">
      <el-pagination
          background
          @current-change="load"
          layout="total, prev, pager, next"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"/>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import router from "@/router";
import request from "@/utils/request";

// ===== 图片地址处理：开发走 Vite proxy 的 /api，生产用 VITE_BASE_URL =====
const BASE = import.meta.env.DEV
    ? '/api'
    : (import.meta.env.VITE_BASE_URL || '/api')

// 兜底占位图（SVG 内联，无需外部文件）
const PLACEHOLDER =
    'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect fill="%23FFF3E0" width="300" height="300"/%3E%3Ctext x="50%25" y="50%25" font-size="72" text-anchor="middle" dy=".35em"%3E%F0%9F%8D%AC%3C/text%3E%3C/svg%3E'

const formatImg = (url) => {
  if (!url) return PLACEHOLDER
  // 完整 URL
  if (/^https?:\/\//i.test(url)) {
    // 把指向 localhost / 127.0.0.1 的地址替换成 BASE，走 proxy 转发，手机/别人访问才不会裂
    return url.replace(/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i, BASE)
  }
  // 相对路径：拼上 BASE
  return BASE + (url.startsWith('/') ? url : '/' + url)
}

const data = reactive({
  name: router.currentRoute.value.query.name,
  pageNum: 1,
  pageSize: 8,
  total: 0,
  tableData: [],
  categoryId: null,
  categoryList : []
})

const loadByCategory = (categoryId) => {
  data.categoryId = categoryId
  data.pageNum = 1
  load()
}

const load = () => {
  request.get('/goods/selectPage', {
    params: {
      pageNum: data.pageNum,
      status: '上架',
      pageSize: data.pageSize,
      name: data.name,
      categoryId: data.categoryId
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

const loadCategory = () => {
  request.get('/category/selectAll').then(res => {
    data.categoryList = res.data
  })
}
loadCategory()

const clearPathParam = () => {
  let url = location.href
  url = url.replace(/(\?|#)[^'"]*/, '');           //去除参数
  window.history.pushState({}, 0, url);
}
clearPathParam()
</script>

<style scoped>
.goods-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 22px;
}

/* ==================== 筛选栏 ==================== */
.goods-filter {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
  padding: 14px 18px;
  border-radius: var(--radius, 12px);
  background: #fff;
  border: 1px solid var(--line, #eceef3);
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
}

.goods-cats {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
  padding-bottom: 2px;
}

.goods-cats::-webkit-scrollbar {
  display: none;
}

.cat-chip {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  height: 32px;
  padding: 0 12px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: #f5f6fa;
  color: var(--text-2, #59607a);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
  white-space: nowrap;
  flex-shrink: 0;
}

.cat-chip:hover {
  background: #eceefa;
  color: var(--brand, #2e3143);
}

.cat-chip.is-active {
  background: var(--brand, #2e3143);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(46, 49, 67, 0.22);
}

.cat-chip--more {
  background: #fff;
  border-color: var(--line-strong, #e0e3ec);
}

.goods-search {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.goods-search :deep(.el-input) {
  width: 200px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 6px;
}

.category-item {
  font-size: 13px;
  padding: 8px 6px;
  cursor: pointer;
  text-align: center;
  border-radius: 8px;
  color: var(--text-2, #59607a);
  transition: background 0.2s, color 0.2s;
}

.category-item:hover {
  background: #f5f6fa;
  color: var(--brand, #2e3143);
}

.category-item.active {
  color: #fff;
  background: var(--brand, #2e3143);
  font-weight: 600;
}

/* ==================== 结果统计 ==================== */
.goods-result {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 13px;
  color: var(--text-3, #8c93a8);
  padding: 0 4px;
}

.goods-result b {
  color: var(--accent, #f64848);
  font-size: 16px;
  margin: 0 2px;
}

.goods-result__filter {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 3px 10px;
  border-radius: 999px;
  background: #eef0f8;
  color: var(--text-2, #59607a);
}

.goods-result__clear {
  color: var(--accent, #f64848);
  cursor: pointer;
  font-size: 12px;
}

.goods-result__clear:hover {
  text-decoration: underline;
}

@media (max-width: 900px) {
  .goods-filter {
    flex-direction: column;
    align-items: stretch;
  }
  .goods-search :deep(.el-input) {
    width: 100%;
  }
  .goods-search {
    width: 100%;
  }
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 640px) {
  .goods-page {
    padding-top: 14px;
  }
  .goods-cats {
    flex-wrap: wrap;
    overflow: visible;
    gap: 6px;
  }
  .cat-chip {
    height: 30px;
    padding: 0 12px;
    font-size: 12.5px;
  }
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>