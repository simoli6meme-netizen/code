<template>
  <div class="front-container cart-page">
    <div class="page-head">
      <div>
        <div class="page-title">我的购物车</div>
        <div class="page-desc">
          共 {{ data.tableData.length }} 件商品，已选 {{ data.selectedRows.length }} 件
        </div>
      </div>
      <el-button @click="$router.push('/front/goods')">
        <el-icon><Goods /></el-icon>
        <span>继续购物</span>
      </el-button>
    </div>

    <div class="card cart-card">
      <el-table
          ref="tableRef"
          :data="data.tableData"
          empty-text="购物车还是空的，去挑点好吃的吧"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="商品图片">
          <template #default="scope">
            <img
                class="table-cover table-cover--sm"
                :src="formatImg(scope.row.goodsImg)"
                alt=""
                @error="(e) => (e.target.src = PLACEHOLDER)"
            />
          </template>
        </el-table-column>
        <el-table-column label="商品名称" prop="goodsName" show-overflow-tooltip min-width="180">
          <template #default="scope">
            <span class="text-strong">{{ scope.row.goodsName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商品单价" width="130">
          <template #default="scope">
            <b class="money">￥{{ scope.row.goodsPrice }}</b>
          </template>
        </el-table-column>
        <el-table-column label="商品数量" width="170">
          <template #default="scope">
            <el-input-number @change="changeNum(scope.row)" v-model="scope.row.num" :min="1" size="small" style="width: 130px"></el-input-number>
          </template>
        </el-table-column>
        <el-table-column label="小计" width="130">
          <template #default="scope">
            <b class="money">￥{{ (Number(scope.row.goodsPrice) * Number(scope.row.num)).toFixed(2) }}</b>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template #default="scope">
            <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-footer">
        <div class="cart-footer__tip">
          <el-icon><InfoFilled /></el-icon>
          <span>勾选商品后点击「立即下单」</span>
        </div>
        <div class="cart-footer__right">
          <div class="cart-footer__total">
            <span class="cart-footer__total-label">合计</span>
            <span class="price"><span class="price__symbol">￥</span>{{ data.total.toFixed ? data.total.toFixed(2) : data.total }}</span>
          </div>
          <el-button :disabled="data.total === 0" @click="handleAddOrder" type="danger" size="large">立即下单</el-button>
        </div>
      </div>
    </div>


    <el-dialog title="下单信息" width="480px" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px">
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
import request from "@/utils/request";
import { reactive, ref, nextTick } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import router from "@/router";
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

const formRef = ref()
const tableRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null,
  selectedRows: [],
  myAddress: null,
  rules: {
    deliverType: [
      { required: true, message: '请选择配送类型', trigger: 'change' }
    ],
    address: [
      { required: true, message: '请输入配送地址', trigger: 'blur' }
    ]
  }
})

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
  if (!data.selectedRows?.length) {
    ElMessage.warning('请选择商品')
    return
  }
  // 外送必须有「我的地址」选点
  if (data.form.deliverType === '外送' && !data.myAddress) {
    ElMessage.warning('外送需要先在「我的地址」页面选择您的位置')
    goMyAddress()
    return
  }
  // 外送：把「我的地址」的地图坐标一起存进订单，供外卖员规划配送路线
  if (data.form.deliverType === '外送' && data.myAddress) {
    data.form.addressX = data.myAddress.x
    data.form.addressY = data.myAddress.y
  }
  data.form.userId = data.user.id
  data.form.cartList = data.selectedRows
  request.post('/orders/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('下单成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg || '下单失败')
    }
  }).catch(err => {
    console.error('下单请求失败:', err)
    ElMessage.error('下单失败：' + (err?.message || '网络异常，请稍后重试'))
  })
}

const changeNum = (row) => {
  calTotal()
  data.form = row
  update()
}

const calTotal = () => {
  data.total = 0
  data.selectedRows.forEach(item => {
    data.total += Number(item.goodsPrice) * Number(item.num)
  })
  data.total = Math.round(data.total * 100) / 100
}

const handleSelectionChange = (rows) => {
  data.selectedRows = rows
  calTotal()
}

// 分页查询
const load = () => {
  request.get('/cart/selectAll', {
    params: {
      userId: data.user.id
    }
  }).then(res => {
    data.tableData = res.data
    // 默认全选，方便直接结算
    nextTick(() => {
      if (tableRef.value && data.tableData?.length) {
        tableRef.value.toggleAllSelection()
      }
    })
  })
}
load()

// 新增
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增保存
const add = () => {
  request.post('/cart/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 编辑保存
const update = () => {
  request.put('/cart/update', data.form).then(res => {
    if (res.code === '200') {

    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 弹窗保存
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? update() : add()
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/cart/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// 重置
const reset = () => {
  data.name = null
  load()
}
</script>

<style scoped>
.cart-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 22px;
}

.cart-card {
  padding: 18px 18px 6px;
}

.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 16px;
  padding: 16px 4px 12px;
  border-top: 1px dashed var(--line, #eceef3);
}

  .cart-footer__tip {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12.5px;
    color: var(--text-3, #8c93a8);
  }

.cart-footer__right {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-left: auto;
}

.cart-footer__total {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.cart-footer__total-label {
  font-size: 13px;
  color: var(--text-2, #59607a);
}

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

@media (max-width: 768px) {
  .cart-footer__right {
    width: 100%;
    justify-content: space-between;
  }
  .cart-footer__tip {
    display: none;
  }
}
</style>