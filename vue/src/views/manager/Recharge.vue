<template>
  <div class="page">

    <div class="card toolbar">
      <el-input v-model="data.userName" clearable @keyup.enter="load" placeholder="请输入用户名称查询"></el-input>
      <el-date-picker v-model="data.time" type="date" placeholder="请选择充值日期"
                      format="YYYY-MM-DD" value-format="YYYY-MM-DD" clearable />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="toolbar__spacer"></div>
      <span class="toolbar__tip">共 {{ data.total }} 条充值记录</span>
    </div>

    <div class="card table-card">
      <div class="table-card__head">
        <div class="table-card__title">用户充值记录</div>
      </div>
      <el-table :data="data.tableData" empty-text="暂无充值记录">
        <el-table-column prop="money" label="充值金额">
          <template #default="scope">
            <b class="money">￥{{ scope.row.money }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="充值人"></el-table-column>
        <el-table-column prop="type" label="支付方式">
          <template #default="scope">
            <el-tag :type="scope.row.type === '微信支付' ? 'success' : 'primary'" effect="light">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="充值时间" width="200"></el-table-column>
        <el-table-column label="操作" align="center" width="100">
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
import {reactive,ref} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

const formRef=ref()
const data = reactive({
  pageNum: 1,
  pageSize: 8,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  userName: null,
  time: null
})

// 分页查询
const load = () => {
  request.get('/recharge/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userName: data.userName,
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
  request.post('/recharge/add', data.form).then(res => {
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
  formRef.value.validate(valid =>{
    if(valid){
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
  data.userName = null
  data.time = null
  load()
}


</script>