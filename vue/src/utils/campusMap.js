/**
 * 校园平面图共享数据与路线算法
 *
 * 数据与后端 CampusMap.java 保持一致：
 *   坐标系 1200 × 800，前端比例尺 1 米 = 2.5 个地图单位，步行速度 1.25 m/s
 */

/* ==================== 图标 ==================== */
export const ICONS = {
  flag: '<path d="M5 21V4"/><path d="M5 5h10.5l-2.2 3 2.2 3H5"/>',
  book: '<path d="M4.5 5.5A2.5 2.5 0 0 1 7 3h13v15H7a2.5 2.5 0 0 0-2.5 2.5v-15Z"/><path d="M4.5 20.5A2.5 2.5 0 0 1 7 18h13"/><path d="M8 7h8M8 10.5h5"/>',
  library: '<path d="M12 6.5C10.2 4.9 7.4 4.4 4 5v14c3.4-.6 6.2.2 8 2 1.8-1.8 4.6-2.6 8-2V5c-3.4-.6-6.2 0-8 1.5Z"/><path d="M12 6.5v14"/>',
  flask: '<path d="M9 3h6M10 3v5.4L5.4 17a3 3 0 0 0 2.7 4.3h7.8a3 3 0 0 0 2.7-4.3L14 8.4V3"/><path d="M7.2 15h9.6"/>',
  ball: '<circle cx="12" cy="12" r="9"/><path d="m12 7.6 4.2 3-1.6 5H9.4l-1.6-5 4.2-3Z"/><path d="M12 7.6V3.2M16.2 10.6l4.2-1.3M14.6 15.6l2.6 3.6M9.4 15.6l-2.6 3.6M7.8 10.6 3.6 9.3"/>',
  track: '<ellipse cx="12" cy="12" rx="9" ry="5.3"/><path d="M7 12h10"/><path d="M4.7 9.2c1.8-.4 4.4-.6 7.3-.6s5.5.2 7.3.6"/>',
  bowl: '<path d="M4 11h16a8 8 0 0 1-16 0Z"/><path d="M8 7.5c0-1.5 1-2 1-3.5M12 7.5c0-1.5 1-2 1-3.5M16 7.5c0-1.5 1-2 1-3.5"/>',
  bed: '<path d="M3 19v-7a2 2 0 0 1 2-2h12a3 3 0 0 1 3 3v6M3 15h18M7 10V8a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"/>',
  blocks: '<rect x="4" y="4" width="7" height="7" rx="1.6"/><rect x="13" y="4" width="7" height="7" rx="1.6"/><rect x="4" y="13" width="7" height="7" rx="1.6"/><rect x="13" y="13" width="7" height="7" rx="1.6"/>',
  gate: '<path d="M5 21V9l7-5 7 5v12M3 21h18M9 21v-8h6v8"/><path d="M8 9h.01M16 9h.01"/>',
  shop: '<path d="M5.5 8.5h13l1.2 11.1a1.5 1.5 0 0 1-1.5 1.7H5.8a1.5 1.5 0 0 1-1.5-1.7L5.5 8.5Z"/><path d="M8.5 9V7a3.5 3.5 0 0 1 7 0v2"/><path d="M9.2 13h.01M14.8 13h.01"/>',
  arrowRight: '<path d="M5 12h14M14 7l5 5-5 5"/>'
}

export const HEART_PATH = 'M12 21s-7.6-4.8-9.5-9.4C.6 8 2.9 4.7 6.3 4.3c2-.2 3.7.8 4.7 2.4 1-1.6 2.8-2.6 4.8-2.3 3.4.4 5.6 3.7 3.8 7.3C19.6 16.2 12 21 12 21Z'

/* ==================== 树木 ==================== */
export const TREES = [
  [92, 108, 0.96], [406, 110, 0.78], [1117, 152, 0.62],
  [374, 610, 0.68], [606, 620, 0.58], [772, 618, 0.55],
  [88, 621, 0.72], [330, 714, 0.8], [620, 722, 0.78],
  [956, 716, 0.75], [1100, 730, 0.65], [1128, 320, 0.58]
]

/* ==================== 辅助建筑（装饰性小楼） ==================== */
export const AUX_BUILDINGS = [
  { name: '内五', x: 480, y: 240, width: 70, height: 76, color: '#8098A8' },
  { name: '内六', x: 720, y: 240, width: 70, height: 76, color: '#8098A8' },
  { name: '知美楼', subtitle: '教学楼', x: 640, y: 525, width: 140, height: 80, color: '#A77ACF' },
  { name: '内七', x: 470, y: 678, width: 80, height: 56, color: '#8098A8' }
]

/* ==================== 楼栋 / 场地 ==================== */
export const LOCATIONS = [
  { id: 'admin', name: '知言楼', subtitle: '行政楼 · 北区', kind: 'building', x: 150, y: 58, width: 240, height: 112, color: '#5D7CF6', icon: 'flag', door: { x: 270, y: 170 } },
  { id: 'sanjiao', name: '知真楼', subtitle: '三教 · 北区', kind: 'building', x: 430, y: 55, width: 250, height: 115, color: '#E99A43', icon: 'book', door: { x: 555, y: 170 } },
  { id: 'wujiao', name: '知化楼', subtitle: '五教 · 东区', kind: 'building', x: 835, y: 235, width: 145, height: 80, color: '#149C8E', icon: 'book', door: { x: 907, y: 315 } },
  { id: 'sijiao', name: '知新楼', subtitle: '四教 · 东区', kind: 'building', x: 1000, y: 235, width: 110, height: 80, color: '#4E8EDB', icon: 'book', door: { x: 1055, y: 315 } },
  { id: 'library', name: '图书馆', subtitle: '阅读自习 · 东区', kind: 'building', x: 835, y: 370, width: 145, height: 85, color: '#896FE8', icon: 'library', door: { x: 907, y: 455 } },
  { id: 'zhiyuan', name: '知远楼', subtitle: '招生 / 财务缴费', kind: 'building', x: 835, y: 520, width: 145, height: 85, color: '#9674D8', icon: 'blocks', door: { x: 907, y: 605 } },
  { id: 'gym', name: '体育馆', subtitle: '室内运动 · 东区', kind: 'building', x: 1000, y: 510, width: 110, height: 95, color: '#E96865', icon: 'ball', door: { x: 1055, y: 605 } },
  { id: 'field', name: '田径场', subtitle: '跑步球场 · 西区', kind: 'field', x: 210, y: 365, width: 210, height: 250, color: '#52A36F', icon: 'track', door: { x: 315, y: 615 } },
  { id: 'xinshitang', name: '天兴园', subtitle: '新食堂 · 西区', kind: 'building', x: 190, y: 245, width: 160, height: 75, color: '#DE9A29', icon: 'bowl', door: { x: 270, y: 320 } },
  { id: 'jiushitang', name: '坤生园', subtitle: '旧食堂 · 中区', kind: 'building', x: 580, y: 235, width: 110, height: 80, color: '#B97A3D', icon: 'bowl', door: { x: 635, y: 315 } },
  { id: 'dorm1', name: '内一', subtitle: '学生公寓 · 西区', kind: 'dorm', x: 55, y: 250, width: 100, height: 92, color: '#4E8EDB', icon: 'bed', door: { x: 155, y: 342 } },
  { id: 'dorm2', name: '内二', subtitle: '学生公寓 · 西区', kind: 'dorm', x: 55, y: 350, width: 100, height: 85, color: '#5A97DE', icon: 'bed', door: { x: 155, y: 435 } },
  { id: 'dorm3', name: '内三', subtitle: '学生公寓 · 西区', kind: 'dorm', x: 55, y: 445, width: 100, height: 85, color: '#66A0E4', icon: 'bed', door: { x: 155, y: 530 } },
  { id: 'dorm4', name: '内四', subtitle: '学生公寓 · 西区', kind: 'dorm', x: 55, y: 540, width: 100, height: 85, color: '#66ABE9', icon: 'bed', door: { x: 155, y: 625 } },
  { id: 'zhiyin', name: '知音楼', subtitle: '教学楼 · 中区', kind: 'building', x: 480, y: 520, width: 120, height: 85, color: '#A77ACF', icon: 'book', door: { x: 540, y: 605 } }
]

/* ==================== 小卖部（固定目的地） ==================== */
export const SHOP = { name: '小卖部', x: 56, y: 666, width: 114, height: 86, color: '#E7A13B', icon: 'shop', door: { x: 170, y: 708 } }
export const SHOP_NODE = 'door.shop'

/* ==================== 球场（2 × 2） ==================== */
export const pitches = []
for (let row = 0; row < 2; row++) {
  for (let column = 0; column < 2; column++) {
    pitches.push({ x: 900 + column * 86, y: 56 + row * 64, row })
  }
}

// 辅助楼的窗户排布
export const auxWindows = (aux) => {
  const list = []
  const columns = aux.width >= 120 ? 5 : 3
  const windowWidth = aux.width >= 120 ? 13 : 11
  const startX = aux.x + (aux.width - (columns * windowWidth + (columns - 1) * 4)) / 2
  const startY = aux.y + aux.height - 30
  for (let row = 0; row < 2; row++) {
    for (let column = 0; column < columns; column++) {
      list.push({
        key: `${aux.name}-${row}-${column}`,
        x: startX + column * (windowWidth + 4),
        y: startY + row * 11,
        width: windowWidth
      })
    }
  }
  return list
}

/* ==================== 步道网络 ==================== */
export const NODES = {
  t170: [170, 212], t270: [270, 212], t450: [450, 212], t555: [555, 212], t800: [800, 212], t1120: [1120, 212],
  m170: [170, 342], m270: [270, 342], m450: [450, 342], m635: [635, 342], m800: [800, 342], m907: [907, 342], m1055: [1055, 342], m1120: [1120, 342],
  s450: [450, 475], s800: [800, 475], s907: [907, 475], s1120: [1120, 475],
  b105: [105, 650], b170: [170, 650], b315: [315, 650], b450: [450, 650], b540: [540, 650], b800: [800, 650], b907: [907, 650], b1055: [1055, 650], b1120: [1120, 650],
  d2: [170, 435], d3: [170, 530], d4: [170, 625],
  'door.admin': [270, 170], 'door.sanjiao': [555, 170], 'door.wujiao': [907, 315], 'door.sijiao': [1055, 315],
  'door.library': [907, 455], 'door.zhiyuan': [907, 605], 'door.gym': [1055, 605], 'door.field': [315, 615],
  'door.xinshitang': [270, 320], 'door.jiushitang': [635, 315],
  'door.dorm1': [155, 342], 'door.dorm2': [155, 435], 'door.dorm3': [155, 530], 'door.dorm4': [155, 625],
  'door.zhiyin': [540, 605], 'door.shop': [170, 708]
}

export const MAIN_EDGES = [
  ['t170', 't270'], ['t270', 't450'], ['t450', 't555'], ['t555', 't800'], ['t800', 't1120'],
  ['m170', 'm270'], ['m270', 'm450'], ['m450', 'm635'], ['m635', 'm800'], ['m800', 'm907'], ['m907', 'm1055'], ['m1055', 'm1120'],
  ['s450', 's800'], ['s800', 's907'], ['s907', 's1120'],
  ['b105', 'b170'], ['b170', 'b315'], ['b315', 'b450'], ['b450', 'b540'], ['b540', 'b800'], ['b800', 'b907'], ['b907', 'b1055'], ['b1055', 'b1120'],
  ['t170', 'm170'], ['m170', 'd2'], ['d2', 'd3'], ['d3', 'd4'], ['d4', 'b170'],
  ['t450', 'm450'], ['m450', 's450'], ['s450', 'b450'],
  ['t800', 'm800'], ['m800', 's800'], ['s800', 'b800'],
  ['t1120', 'm1120'], ['m1120', 's1120'], ['s1120', 'b1120']
]

export const DOOR_EDGES = [
  ['door.admin', 't270'], ['door.sanjiao', 't555'], ['door.wujiao', 'm907'], ['door.sijiao', 'm1055'],
  ['door.library', 's907'], ['door.zhiyuan', 'b907'], ['door.gym', 'b1055'], ['door.field', 'b315'],
  ['door.xinshitang', 'm270'], ['door.jiushitang', 'm635'],
  ['door.dorm1', 'm170'], ['door.dorm2', 'd2'], ['door.dorm3', 'd3'], ['door.dorm4', 'd4'],
  ['door.zhiyin', 'b540'], ['door.shop', 'b170']
]

export const toPath = (edges) => edges.map(([a, b]) =>
    `M${NODES[a][0]} ${NODES[a][1]}L${NODES[b][0]} ${NODES[b][1]}`).join('')

export const mainPath = toPath(MAIN_EDGES)
export const spurPath = toPath(DOOR_EDGES)

const ALL_SEGS = [...MAIN_EDGES, ...DOOR_EDGES].map(([a, b]) => ({
  a, b, x1: NODES[a][0], y1: NODES[a][1], x2: NODES[b][0], y2: NODES[b][1]
}))

/* ==================== 常量 ==================== */
export const PIXELS_PER_METER = 2.5          // 1 米 = 2.5 个地图单位
export const WALK_SPEED_M_PER_MIN = 1.25 * 60 // 步行 1.25 m/s
export const MAX_SNAP_DISTANCE = 36           // 点击位置到步道的最大吸附距离（约 14 米）

/* ==================== 图结构 ==================== */
const ADJ = {}
for (const id of Object.keys(NODES)) ADJ[id] = []
for (const [a, b] of [...MAIN_EDGES, ...DOOR_EDGES]) {
  const len = Math.hypot(NODES[a][0] - NODES[b][0], NODES[a][1] - NODES[b][1])
  ADJ[a].push({ to: b, len })
  ADJ[b].push({ to: a, len })
}

const nearestOnSegment = (px, py, s) => {
  const dx = s.x2 - s.x1
  const dy = s.y2 - s.y1
  const len2 = dx * dx + dy * dy
  let t = len2 === 0 ? 0 : ((px - s.x1) * dx + (py - s.y1) * dy) / len2
  t = Math.max(0, Math.min(1, t))
  return { x: s.x1 + t * dx, y: s.y1 + t * dy }
}

/** 把任意点吸附到最近的步道中心线上 */
export const snapToWalkway = (px, py) => {
  let best = null
  let bestD = Infinity
  for (const s of ALL_SEGS) {
    const np = nearestOnSegment(px, py, s)
    const d = Math.hypot(np.x - px, np.y - py)
    if (d < bestD) { bestD = d; best = np; best.seg = s; best.distance = d }
  }
  if (!best || bestD > MAX_SNAP_DISTANCE) return null
  const { seg } = best
  return {
    x: best.x,
    y: best.y,
    seg,
    offA: Math.hypot(best.x - NODES[seg.a][0], best.y - NODES[seg.a][1]),
    offB: Math.hypot(best.x - NODES[seg.b][0], best.y - NODES[seg.b][1])
  }
}

const distToRect = (px, py, b) => {
  const cx = Math.max(b.x, Math.min(px, b.x + b.width))
  const cy = Math.max(b.y, Math.min(py, b.y + b.height))
  return Math.hypot(px - cx, py - cy)
}

/** 最近的地标名称 */
export const nearestLandmark = (px, py) => {
  let name = ''
  let bestD = Infinity
  for (const b of [...LOCATIONS, ...AUX_BUILDINGS]) {
    const d = distToRect(px, py, b)
    if (d < bestD) { bestD = d; name = b.name }
  }
  return name
}

// 去掉共线的中间点，让折线更干净
const simplify = (pts) => {
  const nearly = (a, b) => Math.abs(a - b) < 1e-6
  if (pts.length <= 2) return pts.slice()
  const out = [pts[0]]
  for (let i = 1; i < pts.length - 1; i++) {
    const p = out[out.length - 1]
    const c = pts[i]
    const n = pts[i + 1]
    const vertical = nearly(p[0], c[0]) && nearly(c[0], n[0])
    const horizontal = nearly(p[1], c[1]) && nearly(c[1], n[1])
    if (!vertical && !horizontal) out.push(c)
  }
  out.push(pts[pts.length - 1])
  return out
}

/* ==================== 订单配送终点 ==================== */

const ALL_PLACES = [...LOCATIONS, ...AUX_BUILDINGS]

const placePoint = (place) => {
  if (!place) return null
  if (place.door) return { x: place.door.x, y: place.door.y }
  return { x: place.x + place.width / 2, y: place.y + place.height / 2 }
}

/** 按楼栋名称查找地点 */
export const locationByName = (name) => {
  if (!name) return null
  return ALL_PLACES.find(p => p.name === name) || null
}

/** 从「校园地图位置（靠近内五）…」这类地址文字里解析出楼栋门口坐标 */
export const pointFromAddressText = (text) => {
  if (!text) return null
  const matched = String(text).match(/靠近([^）)，,、\s]+)/)
  if (!matched) return null
  return placePoint(locationByName(matched[1]))
}

/**
 * 订单的配送终点：优先用下单时保存的地图坐标，
 * 老订单没有坐标时退回按地址文字里的「靠近XX楼」定位。
 */
export const orderTargetPoint = (order) => {
  if (!order) return null
  if (order.addressX != null && order.addressY != null) {
    return { x: order.addressX, y: order.addressY }
  }
  return pointFromAddressText(order.address)
}

const VIRT = '__start__'

// 以「节点」或「步道上的点」为起点跑一次 Dijkstra
const dijkstraFrom = (start) => {
  const dist = {}
  const prev = {}
  const visited = new Set()
  if (start.type === 'node') {
    dist[start.id] = 0
  } else {
    const { seg, offA, offB } = start.snap
    dist[seg.a] = offA
    dist[seg.b] = offB
    prev[seg.a] = VIRT
    prev[seg.b] = VIRT
  }
  while (true) {
    let u = null
    let du = Infinity
    for (const id in dist) {
      if (!visited.has(id) && dist[id] < du) { du = dist[id]; u = id }
    }
    if (u === null) break
    visited.add(u)
    for (const e of ADJ[u]) {
      const nd = du + e.len
      if (dist[e.to] === undefined || nd < dist[e.to]) {
        dist[e.to] = nd
        prev[e.to] = u
      }
    }
  }
  return { dist, prev }
}

/**
 * 规划最短路线
 * @param from 起点：'节点id' 或 { x, y }
 * @param to   终点：'节点id' 或 { x, y }
 * @returns { points, distanceMeters, durationMinutes } 或 null
 */
export const planRoute = (from, to) => {
  const start = typeof from === 'string'
      ? { type: 'node', id: from }
      : { type: 'point', snap: snapToWalkway(from.x, from.y) }
  if (start.type === 'point' && !start.snap) return null

  const { dist, prev } = dijkstraFrom(start)

  // 终点
  let endNode = null
  let endOffset = 0
  let endPoint = null
  if (typeof to === 'string') {
    if (dist[to] === undefined) return null
    endNode = to
  } else {
    const t = snapToWalkway(to.x, to.y)
    if (!t) return null
    const da = dist[t.seg.a]
    const db = dist[t.seg.b]
    if (da !== undefined && (db === undefined || da + t.offA <= db + t.offB)) {
      endNode = t.seg.a
      endOffset = t.offA
    } else if (db !== undefined) {
      endNode = t.seg.b
      endOffset = t.offB
    } else {
      return null
    }
    endPoint = { x: t.x, y: t.y }
  }

  const totalUnits = dist[endNode] + endOffset

  // 回溯节点链
  const chain = []
  let cur = endNode
  let guard = 0
  while (cur !== undefined && cur !== VIRT && guard++ < 500) {
    chain.push(cur)
    cur = prev[cur]
  }
  chain.reverse()

  const points = []
  if (start.type === 'point') points.push([Math.round(start.snap.x), Math.round(start.snap.y)])
  for (const n of chain) points.push([...NODES[n]])
  if (endPoint) points.push([Math.round(endPoint.x), Math.round(endPoint.y)])

  const distanceMeters = Math.round(totalUnits / PIXELS_PER_METER)
  const durationMinutes = Math.max(1, Math.ceil(distanceMeters / WALK_SPEED_M_PER_MIN))

  return { points: simplify(points), distanceMeters, durationMinutes }
}
