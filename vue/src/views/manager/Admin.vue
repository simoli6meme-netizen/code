<template>
  <div class="page">

    <div class="card toolbar">
      <el-input v-model="data.name" clearable @keyup.enter="load" placeholder="请输入名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="toolbar__spacer"></div>
      <span class="toolbar__tip">共 {{ data.total }} 位管理员</span>
    </div>

    <div class="card table-card">
      <div class="table-card__head">
        <div class="table-card__title">管理员列表</div>
        <div class="table-card__actions">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            <span>新增管理员</span>
          </el-button>
        </div>
      </div>
      <el-table :data="data.tableData" empty-text="暂无管理员数据">
        <el-table-column label="用户名" prop="username"></el-table-column>
        <el-table-column label="名称" prop="name"></el-table-column>
        <el-table-column label="头像">
          <template #default="scope">
            <el-image
                v-if="scope.row.avatar"
                :src="formatImg(scope.row.avatar)"
                :preview-src-list="[formatImg(scope.row.avatar)]"
                preview-teleported
                fit="cover"
                class="table-avatar"
            >
              <template #error>
                <img :src="PLACEHOLDER" style="width:100%;height:100%;object-fit:cover" />
              </template>
            </el-image>
            <span v-else class="table-avatar table-avatar--empty">无</span>
          </template>
        </el-table-column>
        <el-table-column label="角色" prop="role"></el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </div>

    <el-dialog title="管理员信息" width="520px" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px">
        <el-form-item label="头像" prop="avatar">
          <el-upload
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleImgSuccess"
              :on-error="handleImgError"
              accept="image/*"
          >
            <img
                v-if="data.form.avatar"
                :src="formatImg(data.form.avatar)"
                class="upload-preview"
                @error="(e) => (e.target.src = PLACEHOLDER)"
            />
            <el-button v-else type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input :disabled="data.form.id" v-model="data.form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </div>
      </template>
    </el-dialog>

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

// ===== 文件上传地址：开发走 Vite proxy 的 /api，手机/别人访问也能通 =====
const uploadUrl = import.meta.env.DEV
    ? '/api/files/upload'
    : ((import.meta.env.VITE_BASE_URL || '') + '/files/upload')

const formRef = ref()

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null,
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' }
    ],
    name: [
      { required: true, message: '请输入姓名', trigger: 'blur' }
    ]
  }
})

// 分页查询
const load = () => {
  request.get('/admin/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}

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
  request.post('/admin/add', data.form).then(res => {
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
  request.put('/admin/update', data.form).then(res => {
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
      // data.form有id就是更新，没有就是新增
      data.form.id ? update() : add()
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/admin/delete/' + id).then(res => {
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

// 处理文件上传成功
const handleImgSuccess = (res) => {
  if (res && res.code === '200') {
    // 存后端返回的原始路径，展示时由 formatImg 统一处理
    data.form.avatar = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res?.msg || '上传失败')
  }
}

// 处理文件上传失败
const handleImgError = () => {
  ElMessage.error('上传失败，请稍后重试')
}

load()
</script>

<style scoped>
.upload-preview {
  width: 90px;
  height: 90px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #e8eaed;
}
</style>