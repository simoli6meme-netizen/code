<template>
  <div class="front-container address-page">
    <div class="page-head">
      <div>
        <div class="page-title">我的地址</div>
        <div class="page-desc">点击校园平面图上的白色步道选择你的位置，保存后外送下单会自动使用该地址</div>
      </div>
    </div>

    <div class="card address-card">
      <!-- 已保存地址提示 -->
      <el-alert
          v-if="data.saved"
          type="success"
          :closable="false"
          show-icon
          class="address-alert"
      >
        <template #title>
          当前已保存的位置：{{ myAddressText(data.saved) }}
        </template>
      </el-alert>
      <el-alert
          v-else
          type="warning"
          :closable="false"
          show-icon
          class="address-alert"
      >
        <template #title>还没有保存地址，请在下方地图上点击白色步道选择位置后点击「保存地址」</template>
      </el-alert>

      <!-- 校园平面图（起始点 = 所选位置，终点 = 小卖部） -->
      <CampusMap
          :start="data.picked"
          end="door.shop"
          start-label="我的位置"
          end-label="小卖部"
          chip-text="所选步道点 → 小卖部"
          @pick="onPick"
          @miss="onMiss"
      />

      <!-- 联系信息 -->
      <el-form label-width="100px" class="address-form">
        <el-form-item label="联系人">
          <el-input v-model="data.contact" placeholder="请输入联系人姓名（选填）" maxlength="20" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="data.phone" placeholder="请输入联系电话（选填）" maxlength="20" />
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="data.note" type="textarea" :rows="2" placeholder="如：知言楼 302 宿舍（选填）" maxlength="100" />
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="address-actions">
        <el-button @click="resetAddress">
          <el-icon><RefreshLeft /></el-icon>
          <span>重 置</span>
        </el-button>
        <el-button type="primary" :disabled="!data.picked" @click="saveAddress">
          <el-icon><Check /></el-icon>
          <span>保存地址</span>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import CampusMap from "@/components/CampusMap.vue";
import { myAddressText } from "@/utils/myAddress";
import { planRoute, nearestLandmark, SHOP_NODE } from "@/utils/campusMap";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  picked: null,     // { x, y, landmark }
  contact: '',
  phone: '',
  note: '',
  saved: null
})

const getAddressKey = () => `my-address-${data.user?.id || 'guest'}`

// 由坐标补充距离 / 时长 / 最近地标
const enrich = (point) => {
  if (!point) return null
  const route = planRoute(point, SHOP_NODE)
  return {
    x: point.x,
    y: point.y,
    landmark: point.landmark || nearestLandmark(point.x, point.y),
    distanceMeters: route ? route.distanceMeters : null,
    durationMinutes: route ? route.durationMinutes : null
  }
}

const onPick = (point) => {
  data.picked = point
}

const onMiss = () => {
  ElMessage.warning('请点击白色步道上的位置')
}

// 加载已保存地址
const loadAddress = () => {
  try {
    const raw = localStorage.getItem(getAddressKey())
    if (raw) {
      const addr = JSON.parse(raw)
      if (addr && typeof addr.x === 'number') {
        data.picked = { x: addr.x, y: addr.y, landmark: addr.landmark }
        data.contact = addr.contact || ''
        data.phone = addr.phone || ''
        data.note = addr.note || ''
        data.saved = addr
      }
    }
  } catch {
    data.saved = null
  }
}
loadAddress()

// 保存
const saveAddress = () => {
  if (!data.picked) {
    ElMessage.warning('请先在地图上点击步道选择你的位置')
    return
  }
  const addr = {
    ...enrich(data.picked),
    contact: data.contact.trim(),
    phone: data.phone.trim(),
    note: data.note.trim(),
    time: Date.now()
  }
  localStorage.setItem(getAddressKey(), JSON.stringify(addr))
  data.saved = addr
  ElMessage.success('地址保存成功，外送下单时将自动使用该位置')
}

// 重置
const resetAddress = () => {
  const doReset = () => {
    data.picked = null
    data.contact = ''
    data.phone = ''
    data.note = ''
    data.saved = null
    localStorage.removeItem(getAddressKey())
    ElMessage.success('已重置，请重新在地图上选择位置')
  }
  if (data.saved) {
    ElMessageBox.confirm('重置将清除已保存的地址，确定吗？', '重置确认', { type: 'warning' })
        .then(doReset).catch(() => {})
  } else {
    doReset()
  }
}

// 供下单页面复用：读取当前保存地址（含坐标）
defineExpose({ getAddressKey })
</script>

<style scoped>
.address-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 22px;
}

.address-card {
  padding: 20px;
}

.address-alert {
  margin-bottom: 16px;
  border-radius: 10px;
}

.address-form {
  max-width: 520px;
  margin-top: 12px;
}

.address-actions {
  display: flex;
  gap: 12px;
  padding-top: 6px;
}

.address-actions .el-button {
  min-width: 120px;
  border-radius: 10px;
}
</style>
