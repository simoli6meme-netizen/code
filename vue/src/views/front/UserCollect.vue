<template>
  <div class="front-container collect-page">
    <div class="page-head">
      <div>
        <div class="page-title">我的收藏</div>
        <div class="page-desc">共收藏 {{ data.total }} 件商品，喜欢的都在这里</div>
      </div>
      <el-button @click="router.push('/front/goods')">
        <el-icon><Goods /></el-icon>
        <span>去逛逛</span>
      </el-button>
    </div>

    <div class="goods-grid" v-if="data.tableData?.length">
      <div class="goods-card" v-for="item in data.tableData" :key="item.id">
        <div class="goods-card__media-wrap" @click="router.push('/front/goodsDetail?id=' + item.goodsId)">
          <img
              :src="formatImg(item.goodsImg)"
              alt=""
              class="goods-card__media"
              loading="lazy"
              @error="(e) => (e.target.src = PLACEHOLDER)"
          />
        </div>
        <div class="goods-card__body">
          <div class="goods-card__name" @click="router.push('/front/goodsDetail?id=' + item.goodsId)">
            {{ item.goodsName }}
          </div>
          <div class="goods-card__meta">
            <span class="price"><span class="price__symbol">￥</span>{{ item.goodsPrice }}</span>
            <el-button type="danger" text size="small" @click.stop="cancel(item.id)">取消收藏</el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="panel empty-state">
      <span class="empty-state__icon">💔</span>
      <span>还没有收藏任何商品</span>
      <el-button type="primary" plain size="small" @click="router.push('/front/goods')">去挑选喜欢的商品</el-button>
    </div>

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
import request from "@/utils/request";
import { reactive } from "vue";
import router from "@/router";
import { ElMessage } from "element-plus";

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
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  pageNum: 1,
  pageSize: 4,
  total: 0,
  tableData: [],
})

const load = () => {
  request.get('/collect/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.user.id,
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

const cancel = (collectId) => {
  request.delete('/collect/delete/' + collectId).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}
</script>

<style scoped>
.collect-page {
  padding-top: 22px;
}

.goods-card__body {
  padding-bottom: 12px;
}

.goods-card .el-button {
  padding: 2px 6px;
  height: auto;
}

@media (max-width: 640px) {
  .page-head {
    align-items: center;
  }
}
</style>