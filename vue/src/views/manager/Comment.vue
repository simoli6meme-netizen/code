<template>
  <div class="page">

    <div class="card toolbar">
      <el-input v-model="data.content" clearable @keyup.enter="load" placeholder="请输入评价内容查询"></el-input>
      <el-input v-model="data.goodsName" clearable @keyup.enter="load" placeholder="请输入商品名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="toolbar__spacer"></div>
      <span class="toolbar__tip">共 {{ data.total }} 条评价</span>
    </div>

    <div class="card table-card">
      <div class="table-card__head">
        <div class="table-card__title">订单评价</div>
      </div>
      <el-table :data="data.tableData" empty-text="暂无评价数据">
        <el-table-column prop="goodsImg" label="商品图片">
          <template #default="scope">
            <el-image
                class="table-cover table-cover--sm"
                :src="formatImg(scope.row.goodsImg)"
                :preview-src-list="[formatImg(scope.row.goodsImg)]"
                preview-teleported
                fit="cover"
            >
              <template #error>
                <img :src="PLACEHOLDER" style="width:100%;height:100%;object-fit:cover" />
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="goodsName" label="商品名称" show-overflow-tooltip min-width="160"></el-table-column>
        <el-table-column prop="orderNo" label="订单编号"></el-table-column>
        <el-table-column prop="score" label="评分" width="200">
          <template #default="scope">
            <el-rate disabled allow-half show-score v-model="scope.row.score" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="userName" label="评价人"></el-table-column>
        <el-table-column prop="time" label="时间"></el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </div>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";

// ===== 图片地址处理 =====
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
  pageNum: 1,
  pageSize: 8,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  content: null,
  goodsName: null,
  rules: {
    name: [
      { required: true, message: '请输入名称', trigger: 'blur' }
    ]
  }
})

// 分页查询
const load = () => {
  request.get('/comment/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      content: data.content,
      goodsName: data.goodsName
      // 删除了原来多传的 rules 对象，避免作为查询参数发给后端
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
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
  request.post('/comment/add', data.form).then(res => {
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
  request.put('/comment/update', data.form).then(res => {
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
    request.delete('/comment/delete/' + id).then(res => {
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
  data.goodsName = null
  data.content = null
  load()
}
</script>