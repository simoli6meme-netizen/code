<template>
  <div class="page">

    <div class="card toolbar">
      <el-input v-model="data.name" clearable @keyup.enter="load" placeholder="请输入商品名称查询"></el-input>
      <el-select v-model="data.categoryId" placeholder="请选择商品分类" clearable>
        <el-option v-for="item in data.categoryList" :key="item.id" :value="item.id" :label="item.name"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="toolbar__spacer"></div>
      <span class="toolbar__tip">共 {{ data.total }} 件商品</span>
    </div>

    <div class="card table-card">
      <div class="table-card__head">
        <div class="table-card__title">商品列表</div>
        <div class="table-card__actions">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            <span>新增商品</span>
          </el-button>
        </div>
      </div>
      <el-table :data="data.tableData" empty-text="暂无商品数据">
        <el-table-column prop="name" label="商品" min-width="210">
          <template #default="scope">
            <div class="cell-stack">
              <div class="text-strong">{{ scope.row.name }}</div>
              <div class="text-muted">{{ scope.row.description }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="img" label="图片" width="84">
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
            <span v-else class="text-muted">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            <b class="money">￥{{ scope.row.price }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="详情" width="80">
          <template #default="scope">
            <el-button type="primary" link @click="view(scope.row.content)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="store" label="库存" width="80"></el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="上架状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === '上架' ? 'success' : 'info'" effect="light">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="90">
          <template #default="scope">
            <el-switch
                v-model="scope.row.recommendActive"
                inline-prompt
                active-text="是"
                inactive-text="否"
                style="--el-switch-on-color: #25bc8f; --el-switch-off-color: #b9bdca"
                @change="updateRecommend(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览量" width="80"></el-table-column>
        <el-table-column prop="saleCount" label="销量" width="70"></el-table-column>
        <el-table-column label="操作" align="center" width="120">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </div>

    <el-dialog title="商品信息" width="760px" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item prop="name" label="名称">
          <el-input v-model="data.form.name" placeholder="请输入名称"></el-input>
        </el-form-item>
        <el-form-item prop="img" label="图片">
          <el-upload
              :action="uploadUrl"
              :on-success="handleImgSuccess"
              :on-error="handleImgError"
              :show-file-list="false"
              list-type="picture"
              accept="image/*"
          >
            <img
                v-if="data.form.img"
                :src="formatImg(data.form.img)"
                class="upload-preview"
                alt=""
                @error="(e) => (e.target.src = PLACEHOLDER)"
            />
            <el-button v-else type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="price" label="价格">
          <el-input-number :min="0" v-model="data.form.price" placeholder="请输入价格"></el-input-number>
        </el-form-item>
        <el-form-item prop="description" label="简介">
          <el-input type="textarea" :rows="3" v-model="data.form.description" placeholder="请输入简介"></el-input>
        </el-form-item>

        <el-form-item prop="store" label="库存">
          <el-input v-model="data.form.store" placeholder="请输入库存"></el-input>
        </el-form-item>
        <el-form-item prop="categoryId" label="分类">
          <el-select style="width: 100%" v-model="data.form.categoryId">
            <el-option v-for="item in data.categoryList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="status" label="上架状态">
          <el-radio-group v-model="data.form.status">
            <el-radio-button label="上架" value="上架"></el-radio-button>
            <el-radio-button label="下架" value="下架"></el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="content" label="详情">
          <div style="border: 1px solid #ccc; width: 100%">
            <Toolbar
                style="border-bottom: 1px solid #ccc"
                :editor="editorRef"
                :mode="mode"
            />
            <Editor
                style="height: 360px; overflow-y: hidden;"
                v-model="data.form.content"
                :mode="mode"
                :defaultConfig="editorConfig"
                @onCreated="handleCreated"
            />
          </div>
        </el-form-item>

      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </span>
      </template>
    </el-dialog>

    <el-dialog title="商品详情" v-model="data.viewVisible" width="760px" :close-on-click-modal="false" destroy-on-close>
      <div class="editor-content-view goods-content" v-html="data.content"></div>
      <template #footer>
    <span class="dialog-footer">
      <el-button @click="data.viewVisible = false">关 闭</el-button>
    </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import '@wangeditor/editor/dist/css/style.css'
import { onBeforeUnmount, shallowRef } from "vue";
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

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

// ===== wangEditor 富文本图片上传地址，同样区分环境 =====
const editorUploadUrl = import.meta.env.DEV
    ? '/api/files/wang/upload'
    : ((import.meta.env.VITE_BASE_URL || '') + '/files/wang/upload')

const formRef = ref()
const data = reactive({
  pageNum: 1,
  pageSize: 6,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  categoryList: [],
  name: null,
  categoryId: null,
  viewVisible: false,
  content: null,
  rules: {
    name: [
      { required: true, message: '请输入名称', trigger: 'blur' }
    ],
    img: [
      { required: true, message: '请输入图片', trigger: 'blur' }
    ],
    price: [
      { required: true, message: '请输入价格', trigger: 'blur' }
    ],
    description: [
      { required: true, message: '请输入简介', trigger: 'blur' }
    ],
    content: [
      { required: true, message: '请输入详情', trigger: 'blur' }
    ],
    store: [
      { required: true, message: '请输入库存', trigger: 'blur' }
    ],
    categoryId: [
      { required: true, message: '请输入分类ID', trigger: 'change' }
    ]
  }
})

/* wangEditor5 初始化开始 */
const editorRef = shallowRef()  // 编辑器实例，必须用 shallowRef
const mode = 'default'
const editorConfig = { MENU_CONF: {} }
// 图片上传配置：使用相对路径，走 proxy 转发到 localhost:9090
editorConfig.MENU_CONF['uploadImage'] = {
  server: editorUploadUrl,
  fieldName: 'file'
}
// 组件销毁时，也及时销毁编辑器，否则可能会造成内存泄漏
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
// 记录 editor 实例，重要！
const handleCreated = (editor) => {
  editorRef.value = editor
}
/* wangEditor5 初始化结束 */

const view = (content) => {
  data.content = content
  data.viewVisible = true
}

// 处理文件上传成功
const handleImgSuccess = (res) => {
  if (res && res.code === '200') {
    data.form.img = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res?.msg || '上传失败')
  }
}

// 处理文件上传失败
const handleImgError = () => {
  ElMessage.error('上传失败，请稍后重试')
}

request.get('/category/selectAll').then(res => {
  data.categoryList = res.data
})


// 分页查询
const load = () => {
  request.get('/goods/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      categoryId: data.categoryId
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.tableData.forEach(v => {
      v.recommendActive = v.recommend === '是'
    })
    data.total = res.data?.total
  })
}
load()

const updateRecommend = (row) => {
  // 1. 仅构造需要提交的纯净对象，不操作 data.form
  const form = {
    id: row.id,
    recommend: row.recommendActive ? '是' : '否'
  }

  // 2. 直接调用请求，不使用原 update() 方法避免依赖 data.form
  request.put('/goods/update', form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
    } else {
      // 失败回滚状态，避免前端开关已变但后端未变
      row.recommendActive = !row.recommendActive
      ElMessage.error(res.msg)
    }
  }).catch(() => {
    row.recommendActive = !row.recommendActive
  })
}

// 新增
const handleAdd = () => {
  data.form = { status: '上架', price: 0, recommend: '否' }
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增保存
const add = () => {
  request.post('/goods/add', data.form).then(res => {
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
  request.put('/goods/update', data.form).then(res => {
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
    request.delete('/goods/delete/' + id).then(res => {
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
  data.categoryId = null
  load()
}
</script>

<style scoped>
.upload-preview {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #e8eaed;
}
</style>