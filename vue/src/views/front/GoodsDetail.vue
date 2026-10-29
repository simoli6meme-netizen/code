<template>
  <div class="front-container detail-page">
    <!-- 面包屑 -->
    <div class="detail-breadcrumb">
      <span @click="router.push('/front/home')">首页</span>
      <el-icon><ArrowRight /></el-icon>
      <span @click="router.push('/front/goods')">精选商品</span>
      <el-icon><ArrowRight /></el-icon>
      <span class="detail-breadcrumb__current">{{ data.goods.name || '商品详情' }}</span>
    </div>

    <!-- 商品信息 -->
    <div class="detail-main panel">
      <div class="detail-media">
        <el-image
            v-if="data.goods.img"
            :src="formatImg(data.goods.img)"
            :preview-src-list="[formatImg(data.goods.img)]"
            preview-teleported
            fit="cover"
            class="detail-media__img"
            @error="(e) => (e.target.src = PLACEHOLDER)"
        />
        <div v-else class="detail-media__placeholder">
          <el-icon><Picture /></el-icon>
        </div>
        <span class="detail-media__badge" v-if="data.goods.recommend === '是'">推荐</span>
      </div>

      <div class="detail-info">
        <h1 class="detail-title">
          <el-tag type="danger" effect="dark" class="detail-title__tag" v-if="data.goods.recommend === '是'">推荐</el-tag>
          {{ data.goods.name }}
        </h1>

        <div class="detail-price-box">
          <span class="detail-price-label">售价</span>
          <span class="price detail-price"><span class="price__symbol">￥</span>{{ data.goods.price }}</span>
          <div class="detail-price-meta">
            <span>累计销量 <b>{{ data.goods.saleCount }}</b></span>
            <span>剩余库存 <b>{{ data.goods.store }}</b></span>
          </div>
        </div>

        <p class="detail-desc">{{ data.goods.description }}</p>

        <div class="detail-buy">
          <div class="detail-num">
            <span class="detail-num__label">数量</span>
            <el-input-number :min="1" v-model="data.num" />
          </div>
          <div class="detail-actions">
            <el-button type="danger" size="large" @click="addCart">
              <el-icon><ShoppingCart /></el-icon>
              <span>加入购物车</span>
            </el-button>
            <el-button type="primary" size="large" @click="handleAddOrder">立即购买</el-button>
            <button
                class="detail-collect"
                :class="{ 'is-active': data.userCollect?.id }"
                @click="data.userCollect?.id ? removeCollect() : addCollect()"
            >
              <el-icon>
                <StarFilled v-if="data.userCollect?.id" />
                <Star v-else />
              </el-icon>
              <span>{{ data.userCollect?.id ? '已收藏' : '收藏' }}</span>
            </button>
          </div>
        </div>

        <div class="detail-service">
          <span><el-icon><CircleCheck /></el-icon>校园小卖部销售并发货</span>
          <span><el-icon><CircleCheck /></el-icon>提供发票和售后服务</span>
          <span><el-icon><CircleCheck /></el-icon>请您放心购买</span>
        </div>
      </div>
    </div>

    <!-- 详情 / 评价 -->
    <div class="panel detail-tabs">
      <div class="detail-tabs__nav">
        <button class="detail-tab" :class="{ 'is-active': data.current === '商品详情' }" @click="changeTab('商品详情')">
          商品详情
        </button>
        <button class="detail-tab" :class="{ 'is-active': data.current === '商品评价' }" @click="changeTab('商品评价')">
          商品评价
          <span class="detail-tab__count">{{ data.total }}</span>
        </button>
      </div>

      <div v-if="data.current ==='商品详情'" class="detail-content" v-html="data.goods.content"></div>

      <div v-if="data.current ==='商品评价'" class="detail-comments">
        <div v-if="data.commentList.length === 0" class="empty-state">
          <span class="empty-state__icon">💬</span>
          <span>暂无评论，买过的同学来说两句吧</span>
        </div>

        <div v-if="data.commentList.length > 0" class="comment-list">
          <div v-for="(item , index) in data.commentList" :key="item.id" class="comment-item">
            <img
                :src="formatImg(item.userAvatar)"
                alt=""
                class="comment-item__avatar"
                @error="(e) => (e.target.src = PLACEHOLDER)"
            >
            <div class="comment-item__body">
              <div class="comment-item__head">
                <span class="comment-item__name">{{ item.userName }}</span>
                <span class="comment-item__time">{{ item.time }}</span>
              </div>
              <el-rate v-model="item.score" show-score allow-half disabled class="comment-item__rate"></el-rate>
              <p class="comment-item__content">{{ item.content }}</p>
            </div>
          </div>

          <div class="pager">
            <el-pagination @current-change="loadComment" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
          </div>
        </div>
      </div>
    </div>

    <el-dialog title="下单信息" width="480px" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="forRef" :model="data.form" :rules="data.rules" label-width="100px" >
        <el-form-item label="配送类型" prop="deliverType">
          <el-radio-group v-model="data.form.deliverType" @change="handleDeliverChange">
            <el-radio-button value="自提" label="自提"></el-radio-button>
            <el-radio-button value="外送" label="外送"></el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="收货地址" prop="address" v-if="data.form.deliverType === '外送'">
          <!-- 已在「我的地址」选点：展示已保存位置 -->
          <div v-if="data.myAddress" class="address-saved">
            <div class="address-saved__text">
              <el-icon><CircleCheck /></el-icon>
              <span>{{ myAddressText(data.myAddress) }}</span>
            </div>
            <el-button link type="primary" size="small" @click="goMyAddress">重新选择</el-button>
          </div>
          <!-- 未选点：提示先去「我的地址」选择 -->
          <div v-else class="address-missing">
            <div class="address-missing__text">
              <el-icon><WarningFilled /></el-icon>
              <span>您还没有在「我的地址」中选择外送位置</span>
            </div>
            <el-button type="warning" size="small" @click="goMyAddress">去选择地址</el-button>
          </div>
          <el-input
              v-if="data.myAddress"
              v-model="data.form.address"
              type="textarea"
              :rows="2"
              placeholder="可补充说明（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="addOrder">确 认</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, ref, watch } from "vue";
import router from "@/router";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import { loadMyAddress, myAddressText } from "@/utils/myAddress";

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

const forRef = ref()

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  id: router.currentRoute.value.query.id,
  goods: {},
  num: 1,
  current: '商品详情',
  commentList: [],
  pageNum: 1,
  pageSize: 8,
  total: 0,
  userCollect: {},
  myAddress: null,
  form: {},
  formVisible: false,
  rules: {
    deliverType: [
      { required: true, message: '请选择配送类型', trigger: 'change' }
    ],
    address: [
      { required: true, message: '请输入配送地址', trigger: 'blur' }
    ]
  }
})

const loadComment = () => {
  request.get('/comment/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      goodsId: data.id,
    }
  }).then(res => {
    data.commentList = res.data?.list
    data.total = res.data?.total
  })
}
loadComment()

const handleAddOrder = () => {
  data.form = {}
  data.formVisible = true
}

// 选择「外送」时，识别用户是否已在「我的地址」页面保存了位置
const handleDeliverChange = (val) => {
  if (val === '外送') {
    data.myAddress = loadMyAddress(data.user.id)
    if (data.myAddress) {
      // 已选点：自动填入保存的地址（可补充说明）
      data.form.address = myAddressText(data.myAddress)
    } else {
      ElMessage.warning('请先到「我的地址」页面选择您的外送位置')
    }
  }
}

// 跳转到「我的地址」页面选点
const goMyAddress = () => {
  data.formVisible = false
  router.push('/front/myAddress')
}

const addOrder = () => {
  // 外送必须有「我的地址」选点：放在表单校验之前，避免未选点时被校验拦下却没有任何提示
  if (data.form.deliverType === '外送' && !data.myAddress) {
    ElMessage.warning('外送需要先在「我的地址」页面选择您的位置')
    goMyAddress()
    return
  }
  forRef.value.validate((valid, fields) => {
    if (!valid) {
      const firstError = fields ? Object.values(fields)[0]?.[0]?.message : ''
      ElMessage.warning(firstError || '请完善下单信息')
      return
    }
    // 外送：把「我的地址」的地图坐标一起存进订单，供外卖员规划配送路线
    if (data.form.deliverType === '外送' && data.myAddress) {
      data.form.addressX = data.myAddress.x
      data.form.addressY = data.myAddress.y
    }
    data.form.userId = data.user.id
    data.form.cartList = [{ goodsId: data.id, num: data.num }]
    request.post('/orders/add', data.form).then(res => {
      if (res.code === '200') {
        ElMessage.success('下单成功')
        load()
        data.formVisible = false
      } else {
        ElMessage.error(res.msg || '下单失败')
      }
    }).catch(err => {
      console.error('下单请求失败:', err)
      ElMessage.error('下单失败：' + (err?.message || '网络异常，请稍后重试'))
    })
  })
}

const addCart = () => {
  request.post('/cart/add', { goodsId: data.id, num: data.num, userId: data.user.id }).then(res => {
    if (res.code === '200') {
      ElMessage.success('加入购物车成功')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 当前的商品是否被当前登录的用户收藏过
const loadCollect = () => {
  request.get('/collect/selectAll', {
    params: {
      goodsId: data.id,
      userId: data.user.id
    }
  }).then(res => {
    if (res.data?.length > 0) {  // 查询到数据了 表示用户收藏过了
      data.userCollect = res.data[0]
    } else {
      data.userCollect = {}
    }
  })
}
loadCollect()

// 取消收藏
const removeCollect = () => {
  request.delete('/collect/delete/' + data.userCollect.id).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      loadCollect()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const addCollect = () => {
  request.post('/collect/add', { goodsId: data.id, userId: data.user.id }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      loadCollect()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const changeTab = (tabName) => {
  data.current = tabName
}

const load = () => {
  request.get('/goods/selectById/' + data.id).then(res => {
    data.goods = res.data
  })
}
const incrementViews = () => {
  if (data.id) {
    request.put('/goods/incrementViews/' + data.id)
  }
}

// 进入页面时执行
incrementViews()
load()

// 从 AI 商品链接等入口在同一详情页组件内切换不同商品时，
// Vue Router 会复用组件实例（仅 query.id 变化），需主动刷新，否则页面不会更新
watch(
  () => router.currentRoute.value.query.id,
  (newId) => {
    if (!newId || String(newId) === String(data.id)) return
    data.id = newId
    data.num = 1
    data.current = '商品详情'
    data.pageNum = 1
    load()
    loadComment()
    loadCollect()
    incrementViews()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
)
</script>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-top: 20px;
}

/* ==================== 面包屑 ==================== */
.detail-breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-3, #8c93a8);
  padding: 0 4px;
}

.detail-breadcrumb span {
  cursor: pointer;
  transition: color 0.2s;
}

.detail-breadcrumb span:hover {
  color: var(--brand, #2e3143);
}

.detail-breadcrumb .el-icon {
  font-size: 11px;
  color: #c6cbd8;
}

.detail-breadcrumb__current {
  color: var(--text, #1f2430) !important;
  cursor: default !important;
  max-width: 420px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ==================== 主信息 ==================== */
.detail-main {
  display: flex;
  gap: 30px;
  padding: 24px;
  border-radius: var(--radius-lg, 16px);
  background: #fff;
  border: 1px solid var(--line, #eceef3);
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
}

.detail-media {
  position: relative;
  width: 400px;
  height: 400px;
  flex-shrink: 0;
  border-radius: 14px;
  overflow: hidden;
  background: #f5f6fa;
}

.detail-media__img {
  width: 100%;
  height: 100%;
  display: block;
}

.detail-media__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c3c8d6;
  font-size: 44px;
}

.detail-media__badge {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 3px 12px;
  border-radius: 999px;
  background: rgba(246, 72, 72, 0.94);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.detail-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.detail-title {
  margin: 0 0 16px;
  font-size: 24px;
  line-height: 1.45;
  font-weight: 600;
  color: var(--text, #1f2430);
}

.detail-title__tag {
  vertical-align: 2px;
  margin-right: 8px;
}

.detail-price-box {
  display: flex;
  align-items: baseline;
  gap: 14px;
  flex-wrap: wrap;
  padding: 16px 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff6f6 0%, #fff 70%);
  border: 1px solid #ffe6e6;
}

.detail-price-label {
  font-size: 12.5px;
  color: var(--text-3, #8c93a8);
}

.detail-price {
  font-size: 34px;
  letter-spacing: -0.5px;
}

.detail-price-meta {
  display: flex;
  gap: 18px;
  margin-left: auto;
  font-size: 12.5px;
  color: var(--text-3, #8c93a8);
  flex-wrap: wrap;
}

.detail-price-meta b {
  color: var(--text-2, #59607a);
  margin-left: 2px;
}

.detail-desc {
  margin: 16px 0;
  padding: 14px 16px;
  border-radius: 10px;
  background: #f7f8fc;
  color: var(--text-2, #59607a);
  font-size: 13.5px;
  line-height: 1.8;
  text-align: justify;
}

.detail-buy {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  margin-top: auto;
  padding-top: 6px;
}

.detail-num {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-num__label {
  font-size: 13px;
  color: var(--text-3, #8c93a8);
}

.detail-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-actions .el-button {
  height: 44px;
  padding: 0 22px;
  border-radius: 10px;
  font-size: 14px;
}

.detail-collect {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 44px;
  padding: 0 16px;
  border-radius: 10px;
  border: 1px solid var(--line-strong, #e0e3ec);
  background: #fff;
  color: var(--text-2, #59607a);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.detail-collect:hover {
  border-color: #f6b26b;
  color: #e08b2d;
  background: #fffaf2;
}

.detail-collect.is-active {
  border-color: #f9b44c;
  color: #e08b2d;
  background: #fff8ec;
  font-weight: 600;
}

.detail-service {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px dashed var(--line, #eceef3);
  font-size: 12.5px;
  color: var(--text-3, #8c93a8);
}

.detail-service span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.detail-service .el-icon {
  color: #34c38f;
}

/* ==================== 详情 / 评价 ==================== */
.detail-tabs {
  border-radius: var(--radius-lg, 16px);
  background: #fff;
  border: 1px solid var(--line, #eceef3);
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
  overflow: hidden;
}

.detail-tabs__nav {
  display: flex;
  gap: 6px;
  padding: 0 18px;
  border-bottom: 1px solid var(--line, #eceef3);
  background: #fafbfd;
}

.detail-tab {
  position: relative;
  height: 52px;
  padding: 0 18px;
  border: none;
  background: transparent;
  color: var(--text-2, #59607a);
  font-size: 14.5px;
  cursor: pointer;
  transition: color 0.2s;
  font-family: inherit;
}

.detail-tab:hover {
  color: var(--brand, #2e3143);
}

.detail-tab.is-active {
  color: var(--text, #1f2430);
  font-weight: 600;
}

.detail-tab.is-active::after {
  content: '';
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: -1px;
  height: 2px;
  border-radius: 2px;
  background: var(--brand, #2e3143);
}

.detail-tab__count {
  display: inline-block;
  margin-left: 4px;
  padding: 0 6px;
  border-radius: 999px;
  background: #eef0f7;
  color: var(--text-3, #8c93a8);
  font-size: 11px;
  line-height: 17px;
  vertical-align: 1px;
}

.detail-content {
  padding: 24px;
  min-height: 320px;
  line-height: 1.9;
  color: var(--text-2, #59607a);
  overflow-x: auto;
}

/* 评论 */
.detail-comments {
  padding: 8px 24px 20px;
  min-height: 280px;
}

.comment-list {
  padding-top: 8px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 18px 0;
  border-bottom: 1px solid var(--line, #eceef3);
}

.comment-item:last-of-type {
  border-bottom: none;
}

.comment-item__avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid var(--line, #eceef3);
}

.comment-item__body {
  flex: 1;
  min-width: 0;
}

.comment-item__head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-item__name {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--text, #1f2430);
}

.comment-item__time {
  font-size: 12px;
  color: var(--text-3, #8c93a8);
}

.comment-item__rate {
  margin: 4px 0 2px;
}

.comment-item__content {
  margin: 4px 0 0;
  font-size: 13.5px;
  line-height: 1.7;
  color: var(--text-2, #59607a);
}

/* ==================== 响应式 ==================== */
/* 我的地址提示 */
.address-saved,
.address-missing {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  margin-bottom: 8px;
}

.address-saved {
  background: #f2fbf6;
  border: 1px solid #c9edda;
}

.address-saved__text {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12.5px;
  line-height: 1.6;
  color: #2b8a5e;
  min-width: 0;
}

.address-saved__text .el-icon {
  margin-top: 3px;
  color: #34c38f;
  flex-shrink: 0;
}

.address-missing {
  background: #fff8ec;
  border: 1px solid #f7dfb8;
}

.address-missing__text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  color: #c07a1d;
}

.address-missing__text .el-icon {
  color: #f9b44c;
  flex-shrink: 0;
}

@media (max-width: 1000px) {
  .detail-main {
    flex-direction: column;
  }
  .detail-media {
    width: 100%;
    height: 340px;
  }
  .detail-price-meta {
    margin-left: 0;
  }
}

@media (max-width: 640px) {
  .detail-main {
    padding: 14px;
  }
  .detail-media {
    height: 240px;
  }
  .detail-title {
    font-size: 18px;
  }
  .detail-price {
    font-size: 26px;
  }
  .detail-actions {
    width: 100%;
  }
  .detail-actions .el-button {
    flex: 1;
    padding: 0 10px;
  }
  .detail-collect {
    justify-content: center;
  }
  .detail-content {
    padding: 16px;
  }
  .detail-comments {
    padding: 6px 16px 16px;
  }
  .detail-tab {
    padding: 0 10px;
    font-size: 13.5px;
  }
}
</style>