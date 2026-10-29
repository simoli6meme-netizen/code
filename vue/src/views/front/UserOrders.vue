<template>
  <div class="front-container orders-page">
    <div class="page-head">
      <div>
        <div class="page-title">我的订单</div>
        <div class="page-desc">共 {{ data.total }} 笔订单，点击行首箭头可查看商品明细</div>
      </div>
    </div>

    <div class="card toolbar">
      <el-input v-model="data.orderNo" clearable @clear="load" @keyup.enter="load" placeholder="请输入订单编号查询"></el-input>
      <el-input v-model="data.goodsName" clearable @clear="load" @keyup.enter="load" placeholder="请输入商品名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <div class="card table-card">
      <div class="table-card__head">
        <div class="table-card__title">订单列表</div>
      </div>
      <el-table :data="data.tableData" empty-text="还没有订单，去逛一逛吧">
        <el-table-column type="expand">
          <template #default="props">
            <div class="order-detail">
              <el-table :data="props.row.orderDetailList" border size="small">
                <el-table-column label="商品图片" prop="goodsImg" width="90">
                  <template #default="scope">
                    <img
                        :src="formatImg(scope.row.goodsImg)"
                        alt=""
                        class="table-cover table-cover--sm"
                        @error="(e) => (e.target.src = PLACEHOLDER)"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="商品名称" prop="goodsName" show-overflow-tooltip></el-table-column>
                <el-table-column label="商品单价" prop="goodsPrice" width="100"></el-table-column>
                <el-table-column label="数量" prop="num" width="90">
                  <template #default="scope">
                    X {{ scope.row.num }}
                  </template>
                </el-table-column>
                <el-table-column label="小计" width="120">
                  <template #default="scope">
                    <b class="money">￥{{ (Number(scope.row.goodsPrice) * Number(scope.row.num)).toFixed(2) }}</b>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="scope">
                    <el-button @click="handleAddComment(scope.row)" type="success" v-if="props.row.status === '已完成'">评价</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号" width="150">
          <template #default="scope">
            <b class="text-strong">{{ scope.row.orderNo }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="总价格" width="100">
          <template #default="scope">
            <b class="money">￥{{ scope.row.total }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="deliverType" label="配送类型" width="90">
          <template #default="scope">
            <el-tag effect="plain" :type="scope.row.deliverType === '外送' ? 'warning' : 'success'">{{ scope.row.deliverType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag type="danger" v-if="scope.row.status === '已取消'">已取消</el-tag>
            <el-tag type="warning" v-if="scope.row.status === '待接单'">待接单</el-tag>
            <el-tag type="primary" v-if="scope.row.status === '配送中'">骑手取货中</el-tag>
            <el-tag type="warning" v-if="scope.row.status === '待收货'">待收货</el-tag>
            <el-tag type="primary" v-if="scope.row.status === '已送达'">已送达</el-tag>
            <el-tag type="primary" v-if="scope.row.status === '已配送'">已配送</el-tag>
            <el-tag type="primary" v-if="scope.row.status === '已出货'">已出货</el-tag>
            <el-tag type="success" v-if="scope.row.status === '已完成'">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="下单时间" width="160"></el-table-column>
        <el-table-column label="地址 / 配送信息" min-width="200">
          <template #default="scope">
            <div class="cell-stack">
              <div>{{ scope.row.address || '自提' }}</div>
              <div class="text-muted" v-if="scope.row.deliver">{{ scope.row.deliver }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单操作" align="center" width="110">
          <template #default="scope">
            <el-button @click="cancel(scope.row)" type="danger" link v-if="scope.row.status === '待接单'">取消</el-button>
            <el-button type="success" link v-if="scope.row.status === '已完成'" @click="router.push('/front/userComment')">评价</el-button>
            <el-button
                @click="done(scope.row)"
                type="primary"
                link
                v-if="['已出货', '已配送', '待收货', '已送达'].includes(scope.row.status)"
            >已收货</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </div>

    <el-dialog title="评价信息" width="480px" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px">
        <el-form-item label="评分" prop="score">
          <el-rate show-score allow-half v-model="data.form.score"></el-rate>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" :rows="4" v-model="data.form.content" autocomplete="off" placeholder="说说你的使用感受吧"/>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
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

const formRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  pageNum: 1,
  pageSize: 4,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  orderNo: null,
  goodsName: null,
  rules: {
    score: [
      { required: true, message: '请输入评分', trigger: 'change' },
    ],
    content: [
      { required: true, message: '请输入内容', trigger: 'blur' },
    ],
  }
})

// 分页查询
const load = () => {
  request.get('/orders/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      orderNo: data.orderNo,
      goodsName: data.goodsName,
      userId: data.user.id
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

const cancel = (row) => {
  ElMessageBox.confirm('您确认取消订单吗?', '二次确认', { type: 'warning' }).then(res => {
    // 用副本提交，避免请求失败时表格行状态已被本地改掉
    data.form = { ...row, status: '已取消' }
    updateOrder()
  }).catch(err => {})
}

const done = (row) => {
  ElMessageBox.confirm('您确认订单货物已经收到了吗?', '二次确认', { type: 'warning' }).then(res => {
    data.form = { ...row, status: '已完成' }
    updateOrder()
  }).catch(err => {})
}

// 编辑保存
const updateOrder = () => {
  request.put('/orders/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 新增
const handleAddComment = (row) => {
  request.get('/comment/selectAll', {
    params: {
      orderId: row.orderId,
      goodsId: row.goodsId
    }
  }).then(res => {
    // 返回的是数组
    data.form = res.data?.length > 0 ? res.data[0] : { orderId: row.orderId, goodsId: row.goodsId, userId: data.user.id }
    data.formVisible = true
  })
}

// 新增保存
const addComment = () => {
  request.post('/comment/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 编辑保存
const updateComment = () => {
  request.put('/comment/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 弹窗保存
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      // data.form有id就是更新，没有就是新增
      data.form.id ? updateComment() : addComment()
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/orders/delete/' + id).then(res => {
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
  data.orderNo = null
  data.goodsName = null
  load()
}
</script>

<style scoped>
.orders-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 22px;
}

.order-detail {
  padding: 12px 16px;
  background: #fafbfd;
}

.table-card__actions :deep(.el-input) {
  width: 200px;
}
</style>