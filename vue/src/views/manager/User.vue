<script setup>
import { reactive, ref } from "vue";
import { Search } from "@element-plus/icons-vue";
import request from "../../utils/request";
import { ElMessage, ElMessageBox } from "element-plus";

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

// ===== 文件上传地址：开发走 Vite proxy 的 /api =====
const uploadUrl = import.meta.env.DEV
    ? '/api/files/upload'
    : ((import.meta.env.VITE_BASE_URL || '') + '/files/upload')

const formRef = ref()
const data = reactive({
  name: null,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  form: {},
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' }
    ],
    name: [
      { required: true, message: '请输入姓名', trigger: 'blur' }
    ]
  }
})

// 查询
const load = () => {
  request.get('user/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 重置
const reset = () => {
  data.name = null
  data.pageNum = 1
  load()
}

// 删除
const del = (id) => {
  ElMessageBox.confirm('您确定删除吗？', '删除确认', { type: 'warning' }).then(() => {
    request.delete('user/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
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

// 分页切换
const handlePageChange = () => {
  load()
}

// 保存（新增/编辑）
const save = () => {
  formRef.value.validate((valid) => {
    if (!valid) return
    if (data.form.id) {
      // 编辑
      request.put("/user/update", data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success("修改成功")
          data.formVisible = false
          load()
        } else {
          ElMessage.error(res.msg || res.message || "修改失败")
        }
      }).catch(err => {
        ElMessage.error(err.response?.data?.msg || err.response?.data?.message || "修改失败")
      })
    } else {
      // 新增
      request.post("/user/add", data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success("操作成功")
          data.formVisible = false
          load()
        } else {
          ElMessage.error(res.msg || res.message || "新增失败")
        }
      }).catch(err => {
        ElMessage.error(err.response?.data?.msg || err.response?.data?.message || "新增失败")
      })
    }
  })
}

// 处理文件上传成功
const handleFileUpload = (res) => {
  if (res && res.code === '200') {
    // 存后端返回的原始路径，展示时由 formatImg 统一处理
    data.form.avatar = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res?.msg || '上传失败')
  }
}

// 处理文件上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败，请稍后重试')
}

// 初始加载
load()
</script>

<template>
  <div class="page">
    <!-- 搜索栏 -->
    <div class="card toolbar">
      <el-input
          v-model="data.name"
          placeholder="请输入用户名称查询"
          clearable
          @keyup.enter="load"
          :prefix-icon="Search"
      />
      <el-button @click="load" type="primary">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="toolbar__spacer"></div>
      <span class="toolbar__tip">共 {{ data.total }} 位普通用户</span>
    </div>

    <!-- 表格 -->
    <div class="card table-card">
      <div class="table-card__head">
        <div class="table-card__title">普通用户列表</div>
        <div class="table-card__actions">
          <el-button @click="handleAdd" type="primary">
            <el-icon><Plus /></el-icon>
            <span>新增用户</span>
          </el-button>
        </div>
      </div>
      <el-table :data="data.tableData" empty-text="暂无用户数据" style="width: 100%">
        <el-table-column prop="username" label="账号">
          <template #default="scope">
            <span class="text-strong">{{ scope.row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="avatar" label="头像">
          <template #default="scope">
            <el-image
                v-if="scope.row.avatar"
                class="table-avatar"
                :src="formatImg(scope.row.avatar)"
                :preview-src-list="[formatImg(scope.row.avatar)]"
                preview-teleported
                fit="cover"
            >
              <template #error>
                <img :src="PLACEHOLDER" style="width:100%;height:100%;object-fit:cover" />
              </template>
            </el-image>
            <span v-else class="table-avatar table-avatar--empty">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            <el-tag effect="light">{{ scope.row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="account" label="账户余额">
          <template #default="scope">
            <b class="money">￥{{ scope.row.account }}</b>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pager">
        <el-pagination
            background
            layout="total, prev, pager, next"
            v-model:current-page="data.pageNum"
            v-model:page-size="data.pageSize"
            :total="data.total"
            @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 弹窗 -->
    <el-dialog title="用户信息" v-model="data.formVisible" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px">
        <el-form-item prop="username" label="账号">
          <el-input
              :disabled="data.form.id !== undefined"
              v-model="data.form.username"
              placeholder="请输入账号"
              autocomplete="off"
          ></el-input>
        </el-form-item>
        <el-form-item prop="name" label="姓名">
          <el-input v-model="data.form.name" placeholder="请输入姓名" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="avatar" label="头像">
          <el-upload
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleFileUpload"
              :on-error="handleUploadError"
              accept="image/*"
          >
            <img
                v-if="data.form.avatar"
                :src="formatImg(data.form.avatar)"
                class="upload-preview"
                alt=""
                @error="(e) => (e.target.src = PLACEHOLDER)"
            />
            <el-button v-else type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.card {
  margin-bottom: 10px;
}

.upload-preview {
  width: 90px;
  height: 90px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #e8eaed;
}
</style>