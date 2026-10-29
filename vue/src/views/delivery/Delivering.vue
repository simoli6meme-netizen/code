<template>
  <div class="delivery-page">
    <div class="delivery-page__head">
      <div>
        <div class="delivery-page__title">配送中</div>
        <div class="delivery-page__desc">共 {{ data.orders.length }} 笔进行中的订单，点击订单卡片可查看配送路线</div>
      </div>
      <el-button :loading="data.loading" @click="load">
        <el-icon><Refresh /></el-icon>
        <span>刷新</span>
      </el-button>
    </div>

    <!-- ===== 订单列表 ===== -->
    <div class="order-list">
      <div
          v-for="item in data.orders"
          :key="item.id"
          class="order-card"
          :class="{ 'is-active': data.activeId === item.id }"
          @click="data.activeId = item.id"
      >
        <div class="order-card__top">
          <div>
            <div class="order-card__no">{{ item.orderNo }}</div>
            <div class="order-card__user">{{ item.userName || '同学' }} · ￥{{ item.total }}</div>
          </div>
          <el-tag :type="statusTagType(item.status)" effect="dark">{{ item.status }}</el-tag>
        </div>

        <div class="order-card__addr">
          <el-icon><Location /></el-icon>
          <span>{{ item.address || '自提' }}</span>
        </div>

        <div class="order-card__goods">
          <span v-for="d in item.orderDetailList" :key="d.id" class="goods-chip">
            {{ d.goodsName }} × {{ d.num }}
          </span>
        </div>

        <div class="order-card__foot">
          <span class="order-card__time">{{ item.time }}</span>
          <div class="order-card__actions" @click.stop>
            <!-- 未取货：先在地图上选自己的位置，再点已取货 -->
            <el-button
                v-if="item.status === '配送中'"
                type="primary"
                size="small"
                :disabled="!myLocation"
                @click="pickUp(item)"
            >已取货</el-button>
            <!-- 已取货：送往用户地址 -->
            <el-button
                v-else-if="item.status === '待收货'"
                type="success"
                size="small"
                @click="delivered(item)"
            >已送达</el-button>
            <span v-else class="cell-muted">等待用户确认收货</span>
          </div>
        </div>
      </div>

      <div v-if="!data.orders.length && !data.loading" class="empty-state">
        <span class="empty-state__icon">🛵</span>
        <span>还没有进行中的订单，去「待接单」抢单吧</span>
      </div>
    </div>

    <!-- ===== 配送路线 ===== -->
    <div v-if="active" class="delivery-card map-card">
      <div class="map-card__head">
        <div>
          <div class="map-card__title">{{ stageTitle }}</div>
          <div class="map-card__desc">{{ stageDesc }}</div>
        </div>
        <el-tag v-if="active.status === '配送中' && !myLocation" type="warning" effect="plain">请先选择你的位置</el-tag>
        <el-tag v-else-if="stage !== 'toShop' && !userPoint" type="danger" effect="plain">无法定位收货地址</el-tag>
        <el-tag v-else-if="stage !== 'toShop' && !hasExactPoint" type="warning" effect="plain">按楼栋定位（订单无坐标）</el-tag>
      </div>

      <el-alert
          v-if="stage !== 'toShop' && !userPoint"
          type="warning"
          :closable="false"
          show-icon
          class="map-card__alert"
      >
        <template #title>该订单没有地图坐标，地址文字里也没有「靠近XX楼」信息，无法自动规划路线，请按收货地址文字配送</template>
      </el-alert>

      <el-alert
          v-else-if="stage !== 'toShop' && !hasExactPoint"
          type="info"
          :closable="false"
          show-icon
          class="map-card__alert"
      >
        <template #title>该订单是旧数据（未保存地图坐标），已按地址中的楼栋位置规划路线；用户重新下单后会使用精确的地图选点</template>
      </el-alert>

      <CampusMap
          :start="mapStart"
          :end="mapEnd"
          :start-label="startLabel"
          :end-label="endLabel"
          :chip-text="chipText"
          :hint="mapHint"
          @pick="onPick"
          @miss="onMiss"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import CampusMap from '@/components/CampusMap.vue'
import { SHOP_NODE, SHOP, orderTargetPoint } from '@/utils/campusMap'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  orders: [],
  activeId: null,
  myLocation: null,
  loading: false
})

// 骑手自己的位置：按骑手隔离存本地，刷新后仍在
const locationKey = () => `delivery-location-${data.user?.id || 'guest'}`
const loadLocation = () => {
  try {
    const raw = localStorage.getItem(locationKey())
    data.myLocation = raw ? JSON.parse(raw) : null
  } catch {
    data.myLocation = null
  }
}
loadLocation()

const myLocation = computed(() => data.myLocation)

const load = () => {
  data.loading = true
  request.get('/orders/selectAllWithDetail', {
    params: {
      deliveryId: data.user.id,
      statuses: '配送中,待收货,已送达'
    }
  }).then(res => {
    data.orders = res.data || []
    // 默认选中第一笔
    if (data.orders.length && !data.orders.some(o => o.id === data.activeId)) {
      data.activeId = data.orders[0].id
    }
    if (!data.orders.length) data.activeId = null
  }).finally(() => {
    data.loading = false
  })
}
load()

const active = computed(() => data.orders.find(o => o.id === data.activeId) || null)

// 用户收货地址（优先用下单时的地图坐标，老订单退回按「靠近XX楼」定位）
const userPoint = computed(() => orderTargetPoint(active.value))
// 是否精确坐标（不是按楼栋文字兜底的）
const hasExactPoint = computed(() => {
  const o = active.value
  return !!(o && o.addressX != null && o.addressY != null)
})

const stage = computed(() => {
  const o = active.value
  if (!o) return ''
  if (o.status === '配送中') return 'toShop'
  if (o.status === '待收货') return 'toUser'
  return 'arrived'
})

const mapStart = computed(() => stage.value === 'toShop' ? myLocation.value : SHOP_NODE)
const mapEnd = computed(() => {
  if (stage.value === 'toShop') return SHOP_NODE
  // 用户下单时保存的地图坐标，缺失时无法规划路线
  return userPoint.value
})
const startLabel = computed(() => stage.value === 'toShop' ? '我的位置' : '小卖部')
const endLabel = computed(() => stage.value === 'toShop' ? '小卖部' : '收货地址')
const chipText = computed(() => stage.value === 'toShop' ? '我的位置 → 小卖部' : '小卖部 → 收货地址')

const stageTitle = computed(() => {
  if (stage.value === 'toShop') return '前往小卖部取货'
  if (stage.value === 'toUser') return '正在送往收货地址'
  return '已送达，等待用户确认'
})

const stageDesc = computed(() => {
  if (stage.value === 'toShop') return '点击地图上的白色步道选择你当前的位置，系统会自动规划到小卖部的最短路线'
  if (stage.value === 'toUser') return '已取货，按橙色路线把商品送到用户的收货地址，到达后点击「已送达」'
  return '商品已送达，用户确认收货后订单将进入「已完成」'
})

const mapHint = computed(() => {
  if (stage.value !== 'toShop') return ''
  return myLocation.value
      ? `当前位置：靠近「${myLocation.value.landmark || '校园步道'}」，可重新点击地图修改`
      : '点击地图上的白色步道，选择你当前所在的位置'
})

const statusTagType = (status) => {
  if (status === '配送中') return 'primary'
  if (status === '待收货') return 'warning'
  return 'success'
}

const onPick = (point) => {
  if (stage.value !== 'toShop') return
  data.myLocation = point
  localStorage.setItem(locationKey(), JSON.stringify(point))
  ElMessage.success('已标记你的位置')
}

const onMiss = () => {
  ElMessage.warning('请点击白色步道上的位置')
}

// 已取货：路径消失，用户端变成待收货，页面切换为「小卖部 → 收货地址」路线
const pickUp = (row) => {
  ElMessageBox.confirm('确认已在小卖部取到商品？', '取货确认', { type: 'warning' }).then(() => {
    request.put('/orders/update', { id: row.id, status: '待收货' }).then(res => {
      if (res.code === '200') {
        ElMessage.success('已取货，请尽快送达')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

// 已送达：用户端点击「已收货」后完成订单
const delivered = (row) => {
  ElMessageBox.confirm('确认已送达用户手中？', '送达确认', { type: 'warning' }).then(() => {
    request.put('/orders/update', { id: row.id, status: '已送达' }).then(res => {
      if (res.code === '200') {
        ElMessage.success('已送达，等待用户确认收货')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}
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

/* ===== 订单卡片 ===== */
.order-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.order-card {
  background: #fff;
  border: 1px solid var(--line, #f0e6da);
  border-radius: var(--radius, 12px);
  padding: 14px 16px;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
}

.order-card:hover {
  border-color: #eed6bd;
  transform: translateY(-2px);
}

.order-card.is-active {
  border-color: #d9701a;
  box-shadow: 0 8px 22px rgba(217, 112, 26, 0.14);
}

.order-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.order-card__no {
  font-size: 14px;
  font-weight: 700;
  color: var(--text, #2b2118);
}

.order-card__user {
  margin-top: 3px;
  font-size: 12.5px;
  color: var(--text-3, #a2917e);
}

.order-card__addr {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-2, #7a6a5a);
}

.order-card__goods {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.goods-chip {
  padding: 2px 8px;
  border-radius: 999px;
  background: #fdf3e9;
  color: #b3651a;
  font-size: 11.5px;
}

.order-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed var(--line, #f0e6da);
}

.order-card__time {
  font-size: 12px;
  color: var(--text-3, #a2917e);
}

/* ===== 地图卡片 ===== */
.delivery-card {
  background: #fff;
  border: 1px solid var(--line, #f0e6da);
  border-radius: var(--radius, 12px);
  padding: 16px;
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
}

.map-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.map-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text, #2b2118);
}

.map-card__desc {
  margin-top: 3px;
  font-size: 12.5px;
  color: var(--text-3, #a2917e);
}

.map-card__alert {
  margin-bottom: 12px;
  border-radius: 10px;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: var(--text-3, #a2917e);
  font-size: 13px;
}

.empty-state__icon {
  font-size: 26px;
}

.cell-muted {
  font-size: 12.5px;
  color: var(--text-3, #a2917e);
}

@media (max-width: 992px) {
  .order-list {
    grid-template-columns: 1fr;
  }
}
</style>
