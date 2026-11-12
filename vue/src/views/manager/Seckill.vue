<template>
  <div class="page">

    <div class="card toolbar">
      <span class="toolbar__tip">
        在这里把商品放进「限时秒杀」：选商品 → 填秒杀价和活动件数 → 保存后前台页面立刻可见
      </span>
      <div class="toolbar__spacer"></div>
      <el-button @click="load">
        <el-icon><Refresh /></el-icon>
        <span>刷新</span>
      </el-button>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span>新增秒杀活动</span>
      </el-button>
    </div>

    <div class="card table-card">
      <div class="table-card__head">
        <div class="table-card__title">秒杀活动列表</div>
        <span class="table-card__hint">
          秒杀卖的是「活动池」库存：创建活动时从商品库存划拨，卖出时只扣活动库存，不再动商品库存
        </span>
      </div>

      <el-table :data="data.tableData" empty-text="暂无秒杀活动，点右上角「新增秒杀活动」">
        <el-table-column label="参与商品" min-width="210">
          <template #default="scope">
            <div class="cell-stack">
              <div class="text-strong">{{ scope.row.goodsName }}</div>
              <div class="text-muted">原价 ￥{{ scope.row.originalPrice }} · 商品ID {{ scope.row.goodsId }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="秒杀价" width="100">
          <template #default="scope">
            <b class="money">￥{{ scope.row.seckillPrice }}</b>
          </template>
        </el-table-column>
        <el-table-column label="配额 / 剩余 / 已抢" width="160">
          <template #default="scope">
            {{ scope.row.stock + (scope.row.sold || 0) }} /
            <b>{{ scope.row.remain }}</b> /
            {{ scope.row.sold || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="起止时间" min-width="180">
          <template #default="scope">
            <div class="text-muted">{{ scope.row.startTime }}</div>
            <div class="text-muted">{{ scope.row.endTime }}</div>
          </template>
        </el-table-column>
        <el-table-column label="活动状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.state === '进行中' ? 'danger' : 'info'" effect="light">
              {{ scope.row.state }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="90">
          <template #default="scope">
            <el-switch
                v-model="scope.row.enabled"
                inline-prompt
                active-text="是"
                inactive-text="否"
                style="--el-switch-on-color: #25bc8f; --el-switch-off-color: #b9bdca"
                @change="toggle(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="90">
          <template #default="scope">
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog title="新增秒杀活动" width="620px" v-model="data.formVisible"
               :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px"
               style="padding-right: 30px; padding-top: 20px">
        <el-form-item prop="goodsId" label="参与商品">
          <el-select v-model="data.form.goodsId" filterable placeholder="输入名称搜索并选择商品"
                     style="width: 100%" @change="onGoodsChange">
            <el-option
                v-for="g in data.goodsList"
                :key="g.id"
                :value="g.id"
                :label="g.name + '（原价 ￥' + g.price + '，库存 ' + g.store + '）'"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="商品信息" v-if="selectedGoods">
          <span class="text-muted">
            原价 ￥{{ selectedGoods.price }}，当前可售 {{ selectedGoods.store }} 件；
            划拨后普通下单不会卖到这部分货
          </span>
        </el-form-item>

        <el-form-item prop="seckillPrice" label="秒杀价">
          <el-input-number v-model="data.form.seckillPrice" :min="0.01" :precision="2" :step="0.5"/>
          <span class="form-tip">必须低于商品原价（选中商品后自动填半价，可修改）</span>
        </el-form-item>

        <el-form-item prop="stock" label="活动件数">
          <el-input-number v-model="data.form.stock" :min="1" :max="selectedGoods ? selectedGoods.store : 9999"/>
          <span class="form-tip">从商品库存划拨，不能超过当前可售库存</span>
        </el-form-item>

        <el-form-item prop="startTime" label="开始时间">
          <el-date-picker v-model="data.form.startTime" type="datetime"
                          value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间"/>
        </el-form-item>

        <el-form-item prop="endTime" label="结束时间">
          <el-date-picker v-model="data.form.endTime" type="datetime"
                          value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择结束时间"/>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" :loading="data.saving" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import {computed, onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";

const formRef = ref()
const data = reactive({
  tableData: [],
  goodsList: [],
  formVisible: false,
  saving: false,
  form: {},
  rules: {
    goodsId: [{required: true, message: '请选择参与秒杀的商品', trigger: 'change'}],
    seckillPrice: [{required: true, message: '请输入秒杀价', trigger: 'blur'}],
    stock: [{required: true, message: '请输入活动件数', trigger: 'blur'}],
    startTime: [{required: true, message: '请选择开始时间', trigger: 'change'}],
    endTime: [{required: true, message: '请选择结束时间', trigger: 'change'}]
  }
})

const selectedGoods = computed(() =>
    data.goodsList.find(g => g.id === data.form.goodsId) || null)

const formatTime = (d) => {
  const p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} `
      + `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/** 活动列表：复用前台接口，不带 userId 就不会计算「我是否已抢」 */
const load = () => {
  request.get('/seckill/list').then(res => {
    if (res.code === '200') {
      data.tableData = (res.data || []).map(a => ({...a, enabled: a.status === '启用'}))
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  })
}

/** 可选商品：只列出上架且还有库存的 */
const loadGoods = () => {
  request.get('/goods/selectAll').then(res => {
    data.goodsList = (res.data || []).filter(g => g.status === '上架' && g.store > 0)
  })
}

const handleAdd = () => {
  const now = new Date()
  const end = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
  data.form = {
    goodsId: null,
    seckillPrice: null,
    stock: 10,
    startTime: formatTime(now),
    endTime: formatTime(end)
  }
  data.formVisible = true
}

const onGoodsChange = () => {
  const g = selectedGoods.value
  if (g && g.price != null) {
    data.form.seckillPrice = Math.max(0.01, Number((Number(g.price) * 0.5).toFixed(2)))
    if (data.form.stock > g.store) data.form.stock = g.store
  }
}

const save = () => {
  formRef.value.validate(valid => {
    if (!valid) return
    if (selectedGoods.value && Number(data.form.seckillPrice) >= Number(selectedGoods.value.price)) {
      ElMessage.error('秒杀价必须低于商品原价')
      return
    }
    if (data.form.endTime <= data.form.startTime) {
      ElMessage.error('结束时间必须晚于开始时间')
      return
    }
    data.saving = true
    request.post('/seckill/activity', {
      goodsId: data.form.goodsId,
      seckillPrice: data.form.seckillPrice,
      stock: data.form.stock,
      startTime: data.form.startTime,
      endTime: data.form.endTime
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('活动已创建，前台「限时秒杀」立即可见')
        data.formVisible = false
        load()
        loadGoods()   // 划拨后商品库存变了，刷新下拉里的库存数
      } else {
        ElMessage.error(res.msg || '创建失败')
      }
    }).finally(() => {
      data.saving = false
    })
  })
}

const toggle = (row) => {
  const status = row.enabled ? '启用' : '停用'
  request.put(`/seckill/activity/${row.id}/status`, null, {params: {status}}).then(res => {
    if (res.code === '200') {
      ElMessage.success(`已${status}`)
      load()
    } else {
      row.enabled = !row.enabled
      ElMessage.error(res.msg || '操作失败')
    }
  }).catch(() => {
    row.enabled = !row.enabled
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
      `确定删除「${row.goodsName}」的秒杀活动吗？剩余 ${row.remain} 件会退回商品库存。已有人抢购的活动不能删除，只能停用。`,
      '提示', {type: 'warning'}
  ).then(() => request.delete(`/seckill/activity/${row.id}`))
      .then(res => {
        if (res.code === '200') {
          ElMessage.success('已删除并归还库存')
          load()
          loadGoods()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      }).catch(() => {
  })
}

onMounted(() => {
  load()
  loadGoods()
})
</script>

<style scoped>
.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}

.table-card__hint {
  font-size: 12px;
  color: #999;
}
</style>
