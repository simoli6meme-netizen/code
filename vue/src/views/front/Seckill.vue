<template>
  <div class="front-container seckill-page">
    <div class="page-head">
      <div>
        <div class="page-title">限时秒杀</div>
        <div class="page-desc">Redis 原子扣库存，订单异步生成，每人限抢一件</div>
      </div>
      <el-button @click="load">
        <el-icon><Refresh /></el-icon>
        <span>刷新</span>
      </el-button>
    </div>

    <div v-if="data.activities.length" class="seckill-grid">
      <div class="seckill-card" v-for="item in data.activities" :key="item.id">
        <img class="seckill-card__img" :src="formatImg(item.goodsImg)" alt=""
             @error="(e) => (e.target.src = PLACEHOLDER)"/>
        <div class="seckill-card__body">
          <div class="seckill-card__name">{{ item.goodsName }}</div>
          <div class="seckill-card__desc">{{ item.goodsDescription || '限时特惠，售完即止' }}</div>

          <div class="seckill-card__price">
            <span class="price"><span class="price__symbol">￥</span>{{ item.seckillPrice }}</span>
            <span class="origin">原价 ￥{{ item.originalPrice }}</span>
          </div>

          <div class="seckill-card__meta">
            <span>剩余 {{ item.remain }} / {{ item.stock + (item.sold || 0) }} 件（已抢 {{ item.sold || 0 }}）</span>
            <span class="state" :class="'state--' + item.state">{{ item.state }}</span>
          </div>
          <div class="seckill-card__time">{{ item.startTime }} 至 {{ item.endTime }}</div>

          <el-button type="danger" class="seckill-card__btn"
                     :disabled="isDisabled(item)" :loading="item.pending"
                     @click="buy(item)">
            {{ buttonText(item) }}
          </el-button>

          <div v-if="data.results[item.id]" class="seckill-card__result">
            {{ data.results[item.id] }}
            <router-link v-if="data.successIds[item.id]" to="/front/userOrders">查看订单</router-link>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="panel empty-state">
      <span>暂无秒杀活动</span>
    </div>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { onMounted, reactive } from "vue";
import router from "@/router";
import { ElMessage } from "element-plus";

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
  activities: [],
  results: {},
  successIds: {}
})

const load = () => {
  request.get('/seckill/list', { params: { userId: data.user.id } }).then(res => {
    if (res.code === '200') {
      data.activities = (res.data || []).map(a => ({ ...a, pending: false }))
    }
  })
}

const buttonText = (item) => {
  if (item.bought) return '已抢到'
  if (item.state === '未开始') return '即将开始'
  if (item.state === '已结束') return '已结束'
  if (item.remain <= 0) return '已抢光'
  return '立即抢购'
}

const isDisabled = (item) => {
  return item.pending || item.bought || item.state !== '进行中' || item.remain <= 0
}

/** 抢购：接口只返回「排队中」，真正的订单由后台线程池生成，所以这里要轮询 */
const buy = (item) => {
  if (!data.user.id) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  item.pending = true
  data.results[item.id] = '排队中，正在生成订单...'

  request.post('/seckill/' + item.id, null, { params: { userId: data.user.id } })
      .then(res => {
        if (res.code !== '200') {
          ElMessage.error(res.msg || '抢购失败')
          data.results[item.id] = res.msg || '抢购失败'
          item.pending = false
          return
        }
        pollResult(item, 0)
      })
      .catch(() => {
        item.pending = false
        data.results[item.id] = '请求失败，请稍后重试'
      })
}

const pollResult = (item, times) => {
  request.get('/seckill/result', {
    params: { activityId: item.id, userId: data.user.id }
  }).then(res => {
    const result = res.data || {}
    if (result.status === 'PENDING') {
      if (times >= 20) {
        item.pending = false
        data.results[item.id] = '订单还在生成，请到「我的订单」查看'
        return
      }
      setTimeout(() => pollResult(item, times + 1), 400)
      return
    }
    item.pending = false
    if (result.status === 'SUCCESS') {
      data.results[item.id] = '抢购成功，订单号 ' + result.orderNo
      data.successIds[item.id] = true
      ElMessage.success('抢购成功')
      load()
    } else if (result.status === 'FAIL') {
      data.results[item.id] = result.msg
      ElMessage.error(result.msg)
      load()
    }
  }).catch(() => {
    item.pending = false
    data.results[item.id] = '查询结果失败，请到「我的订单」确认'
  })
}

onMounted(load)
</script>

<style scoped>
.seckill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 18px;
}

.seckill-card {
  background: #fff;
  border: 1px solid #f0e6d8;
  border-radius: 14px;
  overflow: hidden;
  transition: transform .15s ease;
}

.seckill-card:hover {
  transform: translateY(-2px);
}

.seckill-card__img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.seckill-card__body {
  padding: 14px 16px 18px;
}

.seckill-card__name {
  font-size: 16px;
  font-weight: 500;
  color: #2c2c2a;
}

.seckill-card__desc {
  margin-top: 4px;
  font-size: 12px;
  color: #888;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.seckill-card__price {
  margin-top: 12px;
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.price {
  color: #d85a30;
  font-size: 24px;
  font-weight: 500;
}

.price__symbol {
  font-size: 14px;
}

.origin {
  color: #aaa;
  font-size: 12px;
  text-decoration: line-through;
}

.seckill-card__meta {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.state {
  padding: 1px 8px;
  border-radius: 10px;
  background: #f1efe8;
}

.state--进行中 {
  background: #fcebeb;
  color: #a32d2d;
}

.state--已结束, .state--未开始 {
  background: #f1efe8;
  color: #888;
}

.seckill-card__time {
  margin-top: 8px;
  font-size: 11px;
  color: #aaa;
}

.seckill-card__btn {
  margin-top: 14px;
  width: 100%;
}

.seckill-card__result {
  margin-top: 10px;
  font-size: 12px;
  color: #0f6e56;
}

.seckill-card__result a {
  margin-left: 8px;
  color: #185fa5;
}
</style>
