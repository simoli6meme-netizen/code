// 「我的地址」共享工具：读取用户在 /front/myAddress 页面保存的校园地图位置
export const getAddressKey = (userId) => `my-address-${userId || 'guest'}`

// 读取已保存的地址对象 { x, y, landmark, distanceMeters, durationMinutes, contact, phone, note, time }，没有则返回 null
export const loadMyAddress = (userId) => {
  try {
    const raw = localStorage.getItem(getAddressKey(userId))
    if (!raw) return null
    const addr = JSON.parse(raw)
    if (addr && typeof addr.x === 'number' && typeof addr.y === 'number') {
      return addr
    }
    return null
  } catch {
    return null
  }
}

// 地址描述文本（写入订单收货地址用）
export const myAddressText = (addr) => {
  if (!addr) return ''
  let text = addr.landmark
      ? `校园地图位置（靠近${addr.landmark}）`
      : `校园地图位置（坐标 ${Math.round(addr.x)},${Math.round(addr.y)}）`
  const extra = []
  if (addr.distanceMeters != null) {
    let seg = `距小卖部约 ${addr.distanceMeters} 米`
    if (addr.durationMinutes != null) seg += `（步行约 ${addr.durationMinutes} 分钟）`
    extra.push(seg)
  }
  if (addr.contact) extra.push('联系人：' + addr.contact)
  if (addr.phone) extra.push('电话：' + addr.phone)
  if (addr.note) extra.push(addr.note)
  return extra.length ? text + '，' + extra.join('，') : text
}
