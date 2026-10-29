<template>
  <div class="front-container recharge-page">
    <div class="page-head">
      <div>
        <div class="page-title">我的充值</div>
        <div class="page-desc">共 {{ data.total }} 条充值记录</div>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span>发起充值</span>
      </el-button>
    </div>

    <!-- 余额 -->
    <div class="balance-banner">
      <div class="balance-banner__glow"></div>
      <div>
        <div class="balance-banner__label">当前账户余额</div>
        <div class="balance-banner__value"><span>￥</span>{{ formatMoney(data.user.account) }}</div>
      </div>
      <div class="balance-banner__icon">💰</div>
    </div>

    <div class="card table-card">
      <div class="table-card__head">
        <div class="table-card__title">充值记录</div>
        <div class="table-card__actions">
          <el-date-picker
              v-model="data.time"
              type="date"
              placeholder="选择日期查询"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              clearable
              style="width: 180px"
          />
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </div>

      <el-table :data="data.tableData" empty-text="暂无充值记录">
        <el-table-column prop="money" label="充值金额">
          <template #default="scope">
            <b class="money">+￥{{ scope.row.money }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="支付方式">
          <template #default="scope">
            <el-tag :type="scope.row.type === '微信支付' ? 'success' : 'primary'" effect="light">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="充值时间" width="220"></el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
            background
            @current-change="load"
            layout="total, prev, pager, next"
            v-model:page-size="data.pageSize"
            v-model:current-page="data.pageNum"
            :total="data.total"/>
      </div>
    </div>

    <!--弹窗-->
    <el-dialog title="用户充值" width="480px" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px">
        <el-form-item label="充值金额" prop="money">
          <el-input-number style="width: 200px" :min="1" v-model="data.form.money" autocomplete="off" />
          <span class="money-tip">元</span>
        </el-form-item>
        <el-form-item label="支付方式" prop="type">
          <el-radio-group v-model="data.form.type" class="pay-group">
            <el-radio value="微信支付" border>
              <img style="width: 84px;height: 32px;object-fit: contain" src="@/assets/imgs/WeChat.png" alt="微信支付" />
            </el-radio>
            <el-radio value="支付宝支付" border>
              <img style="width: 84px;height: 32px;object-fit: contain" src="@/assets/imgs/AliPay.png" alt="支付宝支付" />
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">充 值</el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";

const formRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  time: null,
  rules: {
    money: [
      { required: true, message: '请输入金额', trigger: 'blur' }
    ],
    type: [
      { required: true, message: '请选择支付方式', trigger: 'change' }
    ]
  }
})

// 余额格式化：0 / 字符串 / undefined 都显示成 0.00
const formatMoney = (val) => {
  const n = Number(val)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

const loadAccount = () => {
  request.get(`/user/selectById/` + data.user.id).then(res => {
    data.user.account = res.data.account
    // 同步回 localStorage，避免其他页面读到的还是旧余额
    localStorage.setItem('system-user', JSON.stringify(data.user))
  }).catch(err => {
    console.error('加载余额失败:', err)
  })
}
loadAccount()

// 分页查询
const load = () => {
  request.get('/recharge/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.user.id,
      time: data.time
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

// 新增
const handleAdd = () => {
  data.form = { money: 1, type: '微信支付' }
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增保存
const add = () => {
  data.form.userId = data.user.id
  request.post('/recharge/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      loadAccount()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 编辑保存
const update = () => {
  request.put('/recharge/update', data.form).then(res => {
    if (res.code === '200') {
      load()
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
      data.form.id ? update() : add()
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/recharge/delete/' + id).then(res => {
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
  data.time = null
  load()
}
</script>

<style scoped>
.recharge-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-top: 22px;
}

.balance-banner {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  border-radius: var(--radius-lg, 16px);
  background: linear-gradient(120deg, #2e3143 0%, #3f4460 60%, #565d80 100%);
  color: #fff;
  box-shadow: 0 14px 34px rgba(46, 49, 67, 0.2);
}

.balance-banner__glow {
  position: absolute;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  right: -50px;
  top: -100px;
  background: rgba(255, 255, 255, 0.08);
}

.balance-banner__label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.65);
}

.balance-banner__value {
  margin-top: 6px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.balance-banner__value span {
  font-size: 18px;
  margin-right: 2px;
}

.balance-banner__icon {
  font-size: 42px;
  opacity: 0.85;
  position: relative;
  z-index: 1;
}

.money-tip {
  margin-left: 8px;
  color: var(--text-3, #8c93a8);
  font-size: 13px;
}

.pay-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pay-group :deep(.el-radio) {
  height: 52px;
  margin-right: 0;
  padding: 0 14px;
  border-radius: 10px;
}

.pay-group :deep(.el-radio__label) {
  display: flex;
  align-items: center;
}

@media (max-width: 640px) {
  .balance-banner {
    padding: 18px 20px;
  }
  .balance-banner__value {
    font-size: 26px;
  }
  .table-card__actions {
    flex-wrap: wrap;
    justify-content: flex-end;
  }
}
</style>