<template>
  <div class="page">

    <div class="card toolbar">
      <el-input v-model="data.goodsName" clearable @keyup.enter="load" placeholder="请输入商品名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="toolbar__spacer"></div>
      <span class="toolbar__tip">共 {{ data.total }} 张轮播图</span>
    </div>

    <div class="card table-card">
      <div class="table-card__head">
        <div class="table-card__title">轮播图列表</div>
        <div class="table-card__actions">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            <span>新增轮播图</span>
          </el-button>
        </div>
      </div>
      <el-table :data="data.tableData" empty-text="暂无轮播图数据">
        <el-table-column prop="goodsName" label="关联商品" min-width="180">
          <template #default="scope">
            <span class="text-strong">{{ scope.row.goodsName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="img" label="图片">
          <template #default="scope">
            <el-image
                v-if="scope.row.img"
                :src="formatImg(scope.row.img)"
                :preview-src-list="[formatImg(scope.row.img)]"
                preview-teleported
                fit="cover"
                class="table-cover"
            >
              <template #error>
                <img :src="PLACEHOLDER" style="width:100%;height:100%;object-fit:cover" />
              </template>
            </el-image>
            <span v-else class="text-muted">未上传</span>
          </template>
        </el-table-column>

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

    <el-dialog title="轮播图信息" width="520px" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px">
        <el-form-item label="商品" prop="goodsId">
          <el-select style="width: 100%" v-model="data.form.goodsId">
            <el-option v-for="item in data.goodsList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="图片" prop="img">
          <el-upload
              :action="uploadUrl"
              :on-success="handleFileUpload"
              :on-error="handleUploadError"
              list-type="picture"
              :show-file-list="false"
              accept="image/*"
          >
            <img
                v-if="data.form.img"
                style="width: 200px; height: 100px; border-radius: 5px; object-fit: cover"
                :src="formatImg(data.form.img)"
                alt=""
                @error="(e) => (e.target.src = PLACEHOLDER)"
            >
            <div v-else style="width: 200px; height: 150px; border-radius: 5px; border: 1px dashed #ccc; display: flex; align-items: center; justify-content: center">上传图片</div>
          </el-upload>
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
  pageSize: 4,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  goodsName: null,
  goodsList: [],
  rules: {
    goodsId: [
      { required: true, message: '请选择商品', trigger: 'change' }
    ],
    img: [
      { required: true, message: '请上传图片', trigger: 'blur' }
    ]
  }
})

const handleFileUpload = (res) => {
  if (res && res.code === '200') {
    // 存后端返回的原始路径，展示时由 formatImg 统一处理
    data.form.img = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res?.msg || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败，请稍后重试')
}

request.get('/goods/selectAll').then(res => {
  data.goodsList = res.data
})
// 分页查询
const load = () => {
  request.get('/carousel/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
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
  request.post('/carousel/add', data.form).then(res => {
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
  request.put('/carousel/update', data.form).then(res => {
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
    request.delete('/carousel/delete/' + id).then(res => {
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
  load()
}
</script>