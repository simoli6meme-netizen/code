<script setup>
import { reactive } from 'vue'
import request from "@/utils/request";
import router from "@/router";

// ===== 图片地址处理：开发走 Vite proxy 的 /api，生产用 VITE_BASE_URL =====
const BASE = import.meta.env.DEV
    ? '/api'
    : (import.meta.env.VITE_BASE_URL || '/api')

const PLACEHOLDER =
    'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect fill="%23FFF3E0" width="300" height="300"/%3E%3Ctext x="50%25" y="50%25" font-size="72" text-anchor="middle" dy=".35em"%3E%F0%9F%8D%AC%3C/text%3E%3C/svg%3E'

const formatImg = (url) => {
  if (!url) return PLACEHOLDER
  if (/^https?:\/\//i.test(url)) {
    return url.replace(/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i, BASE)
  }
  return BASE + (url.startsWith('/') ? url : '/' + url)
}

const data = reactive({
  carouselList: [],
  hotGoods: [],
  NewGoods: [],
  recommendGoods: [],
})

request.get('carousel/selectAll').then(res => {
  data.carouselList = res.data || []
})

// 一次请求取出上架商品，再分别派生「热销 / 最新 / 推荐」三个列表
// （与原来三次相同请求的结果一致，只是减少重复请求）
request.get('goods/selectAll', {
  params: {
    status: '上架'
  }
}).then(res => {
  const list = res.data || []
  data.hotGoods = [...list].sort((v1, v2) => v2.saleCount - v1.saleCount).slice(0, 4)
  data.NewGoods = list.slice(0, 4)
  data.recommendGoods = list.filter(v => v.recommend === '是').slice(0, 4)
})
</script>

<template>
  <div class="front-container home-page">
    <!-- 轮播图 + 推荐位 -->
    <section class="home-hero">
      <div class="home-hero__carousel">
        <el-carousel v-if="data.carouselList.length" height="460px" :interval="4500" arrow="hover">
          <el-carousel-item v-for="item in data.carouselList" :key="item.id">
            <img
                @click="router.push('/front/goodsDetail?id=' + item.goodsId)"
                :src="formatImg(item.img)"
                alt=""
                class="home-hero__img"
                @error="(e) => (e.target.src = PLACEHOLDER)"
            />
          </el-carousel-item>
        </el-carousel>
        <div v-else class="home-hero__placeholder">
          <el-icon><Picture /></el-icon>
          <span>暂无轮播图</span>
        </div>
      </div>

      <aside class="home-recommend panel">
        <div class="home-recommend__head">
          <img src="@/assets/imgs/love.png" alt="" />
          <span>为您推荐</span>
          <span class="home-recommend__badge">HOT</span>
        </div>

        <div v-if="data.recommendGoods.length" class="home-recommend__list">
          <div
              class="home-recommend__item"
              v-for="item in data.recommendGoods"
              :key="item.id"
              @click="router.push('/front/goodsDetail?id=' + item.id)"
          >
            <img
                :src="formatImg(item.img)"
                alt=""
                @error="(e) => (e.target.src = PLACEHOLDER)"
            />
            <div class="home-recommend__info">
              <div class="home-recommend__name line2">{{ item.name }}</div>
              <div class="home-recommend__bottom">
                <span class="price"><span class="price__symbol">￥</span>{{ item.price }}</span>
                <span class="home-recommend__sale">已售 {{ item.saleCount }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="home-recommend__empty">暂无推荐商品</div>

        <div class="home-recommend__more" @click="router.push('/front/goods')">
          查看全部商品
          <el-icon><ArrowRight /></el-icon>
        </div>
      </aside>
    </section>

    <!-- 热销商品 -->
    <section class="home-section panel">
      <div class="section-head">
        <span class="section-head__bar"></span>
        <img src="@/assets/imgs/hot.png" alt="" class="section-head__icon" />
        <span class="section-head__title">热销商品</span>
        <span class="section-head__more" @click="router.push('/front/goods')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </span>
      </div>

      <div class="goods-grid" v-if="data.hotGoods.length">
        <div class="goods-card" v-for="item in data.hotGoods" :key="item.id" @click="router.push('/front/goodsDetail?id=' + item.id)">
          <div class="goods-card__media-wrap">
            <img
                :src="formatImg(item.img)"
                alt=""
                class="goods-card__media"
                loading="lazy"
                @error="(e) => (e.target.src = PLACEHOLDER)"
            />
            <span class="goods-card__tag" v-if="item.saleCount > 0">热销</span>
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
      <div v-else class="empty-state">
        <span class="empty-state__icon">📦</span>
        <span>暂无热销商品</span>
      </div>
    </section>

    <!-- 最新商品 -->
    <section class="home-section panel">
      <div class="section-head">
        <span class="section-head__bar section-head__bar--green"></span>
        <img src="@/assets/imgs/new.png" alt="" class="section-head__icon" />
        <span class="section-head__title">最新商品</span>
        <span class="section-head__more" @click="router.push('/front/goods')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </span>
      </div>

      <div class="goods-grid" v-if="data.NewGoods.length">
        <div class="goods-card" v-for="item in data.NewGoods" :key="item.id" @click="router.push('/front/goodsDetail?id=' + item.id)">
          <div class="goods-card__media-wrap">
            <img
                :src="formatImg(item.img)"
                alt=""
                class="goods-card__media"
                loading="lazy"
                @error="(e) => (e.target.src = PLACEHOLDER)"
            />
            <span class="goods-card__tag goods-card__tag--new">新品</span>
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
      <div v-else class="empty-state">
        <span class="empty-state__icon">🆕</span>
        <span>暂无最新商品</span>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

/* ==================== 顶部区域 ==================== */
.home-hero {
  display: flex;
  gap: 20px;
  align-items: stretch;
}

.home-hero__carousel {
  flex: 1;
  min-width: 0;
  border-radius: var(--radius-lg, 16px);
  overflow: hidden;
  box-shadow: 0 11px 30px rgba(20, 24, 40, 0.1);
  background: #eef0f6;
}

.home-hero__carousel :deep(.el-carousel) {
  border-radius: var(--radius-lg, 16px);
}

.home-hero__carousel :deep(.el-carousel__container) {
  height: 460px;
}

.home-hero__carousel :deep(.el-carousel__item) {
  border-radius: var(--radius-lg, 16px);
  overflow: hidden;
}

.home-hero__carousel :deep(.el-carousel__indicator .el-carousel__button) {
  width: 22px;
  height: 4px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.65);
}

.home-hero__carousel :deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background: #fff;
  width: 30px;
}

.home-hero__img {
  width: 100%;
  height: 100%;
  cursor: pointer;
  object-fit: cover;
  display: block;
  transition: transform 0.6s cubic-bezier(0.22, 0.61, 0.36, 1);
}

.home-hero__img:hover {
  transform: scale(1.02);
}

.home-hero__placeholder {
  height: 460px;
  border-radius: var(--radius-lg, 16px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--text-3, #8c93a8);
  font-size: 14px;
  background: linear-gradient(135deg, #e9ecf5, #f6f7fb);
}

.home-hero__placeholder .el-icon {
  font-size: 40px;
  opacity: 0.5;
}

/* ==================== 推荐位 ==================== */
.home-recommend {
  width: 340px;
  flex-shrink: 0;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.home-recommend__head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line, #eceef3);
}

.home-recommend__head img {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.home-recommend__head span {
  font-size: 16px;
  font-weight: 600;
  color: var(--text, #1f2430);
}

.home-recommend__badge {
  margin-left: auto;
  font-size: 10px !important;
  font-weight: 700 !important;
  color: var(--accent) !important;
  background: var(--accent-soft);
  border-radius: 999px;
  padding: 2px 8px;
  letter-spacing: 1px;
}

.home-recommend__list {
  flex: 1;
  padding-top: 8px;
  overflow: hidden;
}

.home-recommend__item {
  display: flex;
  gap: 12px;
  padding: 10px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}

.home-recommend__item:hover {
  background: #f6f7fb;
  transform: translateX(2px);
}

.home-recommend__item + .home-recommend__item {
  margin-top: 2px;
}

.home-recommend__item img {
  width: 72px;
  height: 72px;
  border-radius: 9px;
  object-fit: cover;
  flex-shrink: 0;
  background: #f2f3f7;
}

.home-recommend__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 2px 0;
}

.home-recommend__name {
  font-size: 13.5px;
  color: var(--text, #1f2430);
  line-height: 1.5;
}

.home-recommend__bottom {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.home-recommend__sale {
  font-size: 11.5px;
  color: var(--text-3, #8c93a8);
  white-space: nowrap;
}

.home-recommend__empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3, #8c93a8);
  font-size: 13px;
}

.home-recommend__more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 38px;
  margin-top: 10px;
  border-radius: 9px;
  background: #f5f6fa;
  color: var(--text-2, #59607a);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.home-recommend__more:hover {
  background: var(--brand, #2e3143);
  color: #fff;
}

/* ==================== 商品区块 ==================== */
.home-section {
  padding: 20px 20px 4px;
  border-radius: var(--radius-lg, 16px);
  background: #fff;
  border: 1px solid var(--line, #eceef3);
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
}

.section-head__icon {
  width: 22px;
  height: 22px;
  object-fit: contain;
}

.section-head__bar--green {
  background: #34c38f;
}

.section-head__more {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.goods-card__tag--new {
  background: rgba(52, 195, 143, 0.92);
}

/* ==================== 响应式 ==================== */
@media (max-width: 1100px) {
  .home-recommend {
    width: 300px;
  }
  .home-hero__carousel :deep(.el-carousel__container) {
    height: 400px !important;
  }
}

@media (max-width: 900px) {
  .home-hero {
    flex-direction: column;
  }
  .home-recommend {
    width: 100%;
  }
  .home-recommend__list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 4px;
  }
}

@media (max-width: 640px) {
  .home-hero__carousel :deep(.el-carousel__container) {
    height: 220px !important;
  }
  .home-hero__placeholder {
    height: 220px;
  }
  .home-section {
    padding: 16px 14px 0;
  }
  .home-recommend__list {
    grid-template-columns: 1fr;
  }
  .goods-card__media {
    height: 150px;
  }
}
</style>