<template>
  <div class="delivery-page">
    <div class="delivery-page__head">
      <div>
        <div class="delivery-page__title">已完成</div>
        <div class="delivery-page__desc">共 {{ data.tableData.length }} 笔已完成订单，累计配送金额 ￥{{ totalAmount }}</div>
      </div>
      <el-button :loading="data.loading" @click="load">
        <el-icon><Refresh /></el-icon>
        <span>刷新</span>
      </el-button>
    </div>

    <div class="delivery-card">
      <el-table :data="data.tableData" v-loading="data.loading" empty-text="还没有已完成的订单">
        <el-table-column type="expand">
          <template #default="props">
            <div class="order-goods">
              <el-table :data="props.row.orderDetailList" border size="small">
                <el-table-column label="商品图片" width="90">
                  <template #default="scope">
                    <img :src="formatImg(scope.row.goodsImg)" alt="" class="goods-cover" @error="(e) => (e.target.src = PLACEHOLDER)" />
                  </template>
                </el-table-column>
                <el-table-column label="商品名称" prop="goodsName" show-overflow-tooltip />
                <el-table-column label="单价" prop="goodsPrice" width="100" />
                <el-table-column label="数量" width="90">
                  <template #default="scope">X {{ scope.row.num }}</template>
                </el-table-column>
                <el-table-column label="小计" width="120">
                  <template #default="scope">
                    <b class="money">￥{{ (Number(scope.row.goodsPrice) * Number(scope.row.num)).toFixed(2) }}</b>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="orderNo" label="订单编号" width="150">
          <template #default="scope"><b>{{ scope.row.orderNo }}</b></template>
        </el-table-column>
        <el-table-column prop="userName" label="下单用户" width="110" />
        <el-table-column prop="total" label="总价格" width="100">
          <template #default="scope"><b class="money">￥{{ scope.row.total }}</b></template>
        </el-table-column>
        <el-table-column prop="deliverType" label="配送" width="90">
          <template #default="scope">
            <el-tag effect="plain" :type="scope.row.deliverType === '外送' ? 'warning' : 'info'">{{ scope.row.deliverType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag type="success" effect="dark">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="下单时间" width="160" />
        <el-table-column label="收货地址" min-width="220">
          <template #default="scope">{{ scope.row.address || '自提' }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive } from 'vue'
import request from '@/utils/request'

const BASE = import.meta.env.DEV
    ? '/api'
    : (import.meta.env.VITE_BASE_URL || '/api')

const PLACEHOLDER =
    'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect fill="%23FFF3E0" width="300" height="300"/%3E%3Ctext x="50%25" y="50%25" font-size="72" text-anchor="middle" dy=".35em"%3E%F0%9F%8D%AC%3C/text%3E'

const formatImg = (url) => {
  if (!url) return PLACEHOLDER
  if (/^https?:\/\//i.test(url)) {
    return url.replace(/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i, BASE)
  }
  return BASE + (url.startsWith('/') ? url : '/' + url)
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  tableData: [],
  loading: false
})

const totalAmount = computed(() => {
  const sum = data.tableData.reduce((acc, item) => acc + Number(item.total || 0), 0)
  return sum.toFixed(2)
})

const load = () => {
  data.loading = true
  request.get('/orders/selectAllWithDetail', {
    params: { deliveryId: data.user.id, status: '已完成' }
  }).then(res => {
    data.tableData = res.data || []
  }).finally(() => {
    data.loading = false
  })
}
load()
</script>

<style scoped>
.delivery-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.delivery-page__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.delivery-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text, #2b2118);
}

.delivery-page__desc {
  margin-top: 4px;
  font-size: 12.5px;
  color: var(--text-3, #a2917e);
}

.delivery-card {
  background: #fff;
  border: 1px solid var(--line, #f0e6da);
  border-radius: var(--radius, 12px);
  padding: 16px;
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
}

.order-goods {
  padding: 12px 16px;
  background: #faf7f2;
}

.goods-cover {
  width: 46px;
  height: 46px;
  border-radius: 8px;
  object-fit: cover;
}
</style>
