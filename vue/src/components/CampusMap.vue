<template>
  <div class="campus-map-frame">
    <svg
        ref="svgRef"
        class="campus-svg"
        :class="{ 'has-route': !!route, 'has-path-preview': !!hover && interactive }"
        viewBox="0 0 1200 800"
        preserveAspectRatio="xMidYMid meet"
        @click="onMapClick"
        @pointermove="interactive && onMapHover($event)"
        @pointerleave="hover = null"
    >
      <title>青禾中学校园平面图</title>

      <!-- ========== defs ========== -->
      <defs>
        <linearGradient id="campusWash" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#f1f5e8"/>
          <stop offset="0.52" stop-color="#eaf1df"/>
          <stop offset="1" stop-color="#e2ebd7"/>
        </linearGradient>
        <linearGradient id="trackTone" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#dda580"/>
          <stop offset="1" stop-color="#cf906e"/>
        </linearGradient>
        <pattern id="grassDots" width="28" height="28" patternUnits="userSpaceOnUse">
          <circle cx="3" cy="4" r="1" fill="#a9c099" opacity="0.32"/>
          <circle cx="17" cy="18" r="0.8" fill="#a9c099" opacity="0.24"/>
        </pattern>
        <filter id="shopShadow" x="-30%" y="-30%" width="160%" height="180%">
          <feDropShadow dx="0" dy="7" stdDeviation="9" flood-color="#694a22" flood-opacity="0.14"/>
        </filter>
      </defs>

      <!-- ========== 背景 ========== -->
      <g aria-hidden="true">
        <rect x="0" y="0" width="1200" height="800" fill="#e8efdd"/>
        <rect x="40" y="26" width="1120" height="748" rx="38" fill="url(#campusWash)"/>
        <rect x="40" y="26" width="1120" height="748" rx="38" fill="url(#grassDots)" opacity="0.8"/>
        <rect x="40" y="26" width="1120" height="748" rx="38" fill="none" stroke="#cedcc3" stroke-width="2"/>
        <rect x="54" y="40" width="1092" height="720" rx="28" fill="none"
              stroke="#ffffff" stroke-width="1.2" opacity="0.55" stroke-dasharray="2 9"/>
        <ellipse cx="285" cy="440" rx="190" ry="205" fill="#e5eeda" opacity="0.58"/>
        <ellipse cx="246" cy="150" rx="250" ry="125" fill="#e3edd8" opacity="0.45"/>
        <rect x="810" y="222" width="330" height="405" rx="24" fill="#edf2e7" opacity="0.55"/>
        <rect x="462" y="222" width="350" height="410" rx="24" fill="#eff3e8" opacity="0.38"/>
        <rect x="54" y="238" width="360" height="380" rx="24" fill="#e5eddb" opacity="0.36"/>
      </g>

      <!-- ========== 景观 ========== -->
      <g aria-hidden="true">
        <template v-for="(pitch, index) in pitches" :key="'pitch' + index">
          <rect :x="pitch.x" :y="pitch.y" width="78" height="57" rx="3"
                :fill="pitch.row === 0 ? '#9bc78f' : '#a8ce9b'" stroke="#c9786e" stroke-width="1.4"/>
          <line :x1="pitch.x + 6" :y1="pitch.y + 28.5" :x2="pitch.x + 72" :y2="pitch.y + 28.5"
                stroke="#f2f7ec" stroke-width="1" opacity="0.8"/>
          <circle :cx="pitch.x + 39" :cy="pitch.y + 28.5" r="9"
                  fill="none" stroke="#f2f7ec" stroke-width="1" opacity="0.72"/>
          <rect :x="pitch.x + 6" :y="pitch.y + 9" width="13" height="39"
                fill="none" stroke="#f2f7ec" stroke-width="1" opacity="0.55"/>
          <rect :x="pitch.x + 59" :y="pitch.y + 9" width="13" height="39"
                fill="none" stroke="#f2f7ec" stroke-width="1" opacity="0.55"/>
        </template>
        <text x="985" y="201" class="map-feature-label" text-anchor="middle">球 场</text>

        <template v-for="index in 3" :key="'court' + index">
          <rect :x="480 + (index - 1) * 106" y="357" width="96" height="78" rx="3"
                fill="#5b8cc4" stroke="#aac8e4" stroke-width="1.4"/>
          <rect :x="487 + (index - 1) * 106" y="364" width="82" height="64"
                fill="none" stroke="#eef5ef" stroke-width="1" opacity="0.8"/>
          <line :x1="487 + (index - 1) * 106" :y1="396" :x2="569 + (index - 1) * 106" :y2="396"
                stroke="#eef5ef" stroke-width="1" opacity="0.7"/>
          <circle :cx="528 + (index - 1) * 106" cy="396" r="9"
                  fill="none" stroke="#eef5ef" stroke-width="1" opacity="0.7"/>
          <rect :x="520 + (index - 1) * 106" y="364" width="16" height="20" fill="#c96b62" opacity="0.88"/>
          <rect :x="520 + (index - 1) * 106" y="408" width="16" height="20" fill="#c96b62" opacity="0.88"/>
        </template>
        <text x="635" y="452" class="map-feature-label" text-anchor="middle">篮 球 场</text>

        <path d="M690 200 Q716 148 758 124 Q804 104 846 128 Q880 146 890 200 Z"
              fill="#b7d29f" opacity="0.86"/>
        <path d="M706 188 Q748 148 792 138 Q840 130 872 188"
              fill="none" stroke="#a2c38c" stroke-width="5" opacity="0.48" stroke-linecap="round"/>
        <ellipse cx="798" cy="198" rx="92" ry="9" fill="#9eba87" opacity="0.18"/>
        <g transform="translate(784 114)">
          <path d="M-30 2 Q-16 -4 0 -20 Q16 -4 30 2 L20 8 H-20 Z" fill="#c77a58" stroke="#a86144" stroke-width="1"/>
          <rect x="-14" y="8" width="5" height="22" fill="#f1e5ce"/>
          <rect x="9" y="8" width="5" height="22" fill="#f1e5ce"/>
          <rect x="-25" y="30" width="50" height="4" rx="2" fill="#e3d4b7"/>
        </g>
        <text x="825" y="158" class="map-feature-label" text-anchor="middle">勤 仁 坡</text>
      </g>

      <!-- ========== 树木 ========== -->
      <g aria-hidden="true">
        <g v-for="(tree, index) in TREES" :key="'tree' + index"
           class="tree" :transform="`translate(${tree[0]} ${tree[1]}) scale(${tree[2]})`">
          <rect x="-1.5" y="0" width="3" height="13" rx="1.5" class="tree-trunk"/>
          <circle cx="-7" cy="0" r="6.6" class="tree-leaf-light"/>
          <circle cx="7" cy="1" r="5.8" class="tree-leaf"/>
          <circle cx="0" cy="-7" r="9.2" class="tree-leaf"/>
          <circle cx="-2.5" cy="-11" r="4.2" class="tree-leaf-light" opacity="0.55"/>
        </g>
      </g>

      <!-- ========== 步道 ========== -->
      <g aria-hidden="true">
        <path :d="mainPath" class="walkway-base" stroke-width="22"/>
        <path :d="mainPath" class="walkway-fill" stroke-width="18"/>
        <path :d="spurPath" class="walkway-base" stroke-width="15"/>
        <path :d="spurPath" class="walkway-fill" stroke-width="11"/>
        <path :d="mainPath" class="walkway-center" stroke-width="1"/>
      </g>

      <!-- ========== 辅助建筑 ========== -->
      <g aria-hidden="true">
        <g v-for="aux in AUX_BUILDINGS" :key="'aux' + aux.name" class="aux-building">
          <rect :x="aux.x + 2" :y="aux.y + 4" :width="aux.width" :height="aux.height" rx="10"
                fill="#314c3e" opacity="0.055"/>
          <rect :x="aux.x" :y="aux.y" :width="aux.width" :height="aux.height" rx="10" class="aux-body"/>
          <line :x1="aux.x + 10" :y1="aux.y + 6.5" :x2="aux.x + aux.width - 10" :y2="aux.y + 6.5"
                :stroke="aux.color" stroke-width="1.6" stroke-linecap="round" opacity="0.38"/>
          <text :x="aux.x + aux.width / 2" :y="aux.y + (aux.subtitle ? 26 : 30)"
                class="aux-label" text-anchor="middle">{{ aux.name }}</text>
          <text v-if="aux.subtitle" :x="aux.x + aux.width / 2" :y="aux.y + 40"
                class="aux-sub" text-anchor="middle">{{ aux.subtitle }}</text>
          <template v-if="aux.height >= 66">
            <rect v-for="w in auxWindows(aux)" :key="'aw' + w.key"
                  :x="w.x" :y="w.y" :width="w.width" height="6" rx="1.4"
                  :fill="aux.color" opacity="0.11"/>
          </template>
          <template v-else>
            <rect v-for="i in 3" :key="'as' + i"
                  :x="aux.x + 12 + (i - 1) * 20" :y="aux.y + aux.height - 14"
                  width="13" height="6" rx="1.4" :fill="aux.color" opacity="0.11"/>
          </template>
        </g>
      </g>

      <!-- ========== 楼栋 ========== -->
      <g v-for="loc in LOCATIONS" :key="loc.id" class="map-location">
        <title>{{ loc.name }} · {{ loc.subtitle }}</title>

        <template v-if="loc.kind === 'field'">
          <ellipse :cx="loc.x + loc.width / 2" :cy="loc.y + loc.height / 2"
                   :rx="loc.width / 2" :ry="loc.height / 2"
                   fill="url(#trackTone)" stroke="#c38563" stroke-width="1.2" class="field-outline"/>
          <ellipse :cx="loc.x + loc.width / 2" :cy="loc.y + loc.height / 2"
                   :rx="loc.width / 2 - 17" :ry="loc.height / 2 - 10"
                   fill="none" stroke="#f1ded0" stroke-width="1" opacity="0.65" stroke-dasharray="3 7"/>
          <ellipse :cx="loc.x + loc.width / 2" :cy="loc.y + loc.height / 2"
                   :rx="loc.width / 2 - 32" :ry="loc.height / 2 - 18"
                   fill="none" stroke="#f1ded0" stroke-width="0.9" opacity="0.5"/>
          <ellipse :cx="loc.x + loc.width / 2" :cy="loc.y + loc.height / 2"
                   :rx="loc.width / 2 - 48" :ry="loc.height / 2 - 26"
                   fill="#a9cf91" stroke="#8db77b" stroke-width="1"/>
          <line :x1="loc.x + loc.width / 2" :y1="loc.y + loc.height / 2 - (loc.height / 2 - 28)"
                :x2="loc.x + loc.width / 2" :y2="loc.y + loc.height / 2 + (loc.height / 2 - 28)"
                stroke="#ffffff" stroke-width="1" opacity="0.34"/>
          <circle :cx="loc.x + loc.width / 2" :cy="loc.y + loc.height / 2" r="18"
                  fill="none" stroke="#ffffff" stroke-width="1" opacity="0.32"/>
          <rect :x="loc.x + loc.width / 2 - (loc.width / 2 - 54)" :y="loc.y + loc.height / 2 - 22"
                width="44" height="44" fill="none" stroke="#ffffff" stroke-width="1" opacity="0.24"/>
          <rect :x="loc.x + loc.width / 2 + (loc.width / 2 - 54) - 44" :y="loc.y + loc.height / 2 - 22"
                width="44" height="44" fill="none" stroke="#ffffff" stroke-width="1" opacity="0.24"/>
          <rect :x="loc.x + loc.width / 2 - 48" :y="loc.y + loc.height / 2 - 16"
                width="96" height="32" rx="16" class="field-label-bg"
                stroke="#ffffff" stroke-width="1.2" stroke-opacity="0.7"/>
          <g :transform="`translate(${loc.x + loc.width / 2 - 36} ${loc.y + loc.height / 2 - 8.5})`">
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="#4c875f"
                 stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" v-html="ICONS.track"></svg>
          </g>
          <text :x="loc.x + loc.width / 2 - 12" :y="loc.y + loc.height / 2 + 4.5"
                class="field-label">{{ loc.name }}</text>
          <ellipse :cx="loc.door.x" :cy="loc.door.y" rx="11" ry="4" :fill="loc.color" opacity="0.55"/>
        </template>

        <template v-else-if="loc.kind === 'dorm'">
          <rect :x="loc.x + 2" :y="loc.y + 5" :width="loc.width" :height="loc.height" rx="13" class="building-shadow"/>
          <rect :x="loc.x" :y="loc.y" :width="loc.width" :height="loc.height" rx="13" class="building-body"/>
          <line :x1="loc.x + 12" :y1="loc.y + 7.5" :x2="loc.x + loc.width - 12" :y2="loc.y + 7.5"
                :stroke="loc.color" stroke-width="2" stroke-linecap="round" opacity="0.35"/>
          <rect :x="loc.x + 9" :y="loc.y + 13" width="22" height="22" rx="7" :fill="loc.color" opacity="0.13"/>
          <g :transform="`translate(${loc.x + 14.5} ${loc.y + 18.5})`">
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" :stroke="loc.color"
                 stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" v-html="ICONS.bed"></svg>
          </g>
          <text :x="loc.x + 38" :y="loc.y + 28" class="dorm-title">{{ loc.name }}</text>
          <line :x1="loc.x + 13" :y1="loc.y + loc.height - 37"
                :x2="loc.x + loc.width - 13" :y2="loc.y + loc.height - 37"
                :stroke="loc.color" stroke-width="0.8" opacity="0.12"/>
          <template v-for="row in 2" :key="'dwr' + row">
            <rect v-for="col in 3" :key="'dwc' + col"
                  :x="loc.x + 15 + (col - 1) * 24" :y="loc.y + loc.height - 28 + (row - 1) * 11"
                  width="16" height="7" rx="1.8" :fill="loc.color"
                  fill-opacity="0.07" :stroke="loc.color" stroke-opacity="0.13" stroke-width="0.7"/>
          </template>
          <line :x1="loc.door.x - 10" :y1="loc.door.y" :x2="loc.door.x + 10" :y2="loc.door.y"
                :stroke="loc.color" class="door-mark"/>
        </template>

        <template v-else>
          <rect :x="loc.x + 2" :y="loc.y + 5" :width="loc.width" :height="loc.height" rx="13" class="building-shadow"/>
          <rect :x="loc.x" :y="loc.y" :width="loc.width" :height="loc.height" rx="13" class="building-body"/>
          <line :x1="loc.x + 14" :y1="loc.y + 8.5" :x2="loc.x + loc.width - 14" :y2="loc.y + 8.5"
                :stroke="loc.color" stroke-width="2" stroke-linecap="round" opacity="0.35"/>
          <rect :x="loc.x + 14" :y="loc.y + 15" width="30" height="30" rx="9" :fill="loc.color" opacity="0.12"/>
          <g :transform="`translate(${loc.x + 21.5} ${loc.y + 22.5})`">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" :stroke="loc.color"
                 stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" v-html="ICONS[loc.icon]"></svg>
          </g>
          <text :x="loc.x + 52" :y="loc.y + 32" class="location-label">{{ loc.name }}</text>
          <text :x="loc.x + 52" :y="loc.y + 47" class="location-zone">{{ loc.subtitle.split(' · ')[0] }}</text>
          <template v-if="loc.width >= 150">
            <circle :cx="loc.x + loc.width - 21" :cy="loc.y + 22" r="9.5" :fill="loc.color" opacity="0.09"/>
            <g :transform="`translate(${loc.x + loc.width - 27} ${loc.y + 16})`" class="building-arrow">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" :stroke="loc.color"
                   stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" v-html="ICONS.arrowRight"></svg>
            </g>
          </template>
          <template v-if="loc.height >= 95">
            <line :x1="loc.x + 16" :y1="loc.y + loc.height - 33"
                  :x2="loc.x + loc.width - 16" :y2="loc.y + loc.height - 33"
                  :stroke="loc.color" stroke-width="0.8" opacity="0.12"/>
            <rect v-for="i in 3" :key="'bw' + i"
                  :x="loc.x + 18 + (i - 1) * 22" :y="loc.y + loc.height - 24"
                  width="15" height="8" rx="2" :fill="loc.color" fill-opacity="0.055"
                  :stroke="loc.color" stroke-opacity="0.13" stroke-width="0.7"/>
          </template>
          <line :x1="loc.door.x - 10" :y1="loc.door.y" :x2="loc.door.x + 10" :y2="loc.door.y"
                :stroke="loc.color" class="door-mark"/>
        </template>
      </g>

      <!-- ========== 小卖部 ========== -->
      <g class="map-shop" aria-hidden="true" filter="url(#shopShadow)">
        <title>小卖部 · 固定目的地</title>
        <rect :x="SHOP.x + 2" :y="SHOP.y + 5" :width="SHOP.width" :height="SHOP.height" rx="15"
              fill="#6a4a20" opacity="0.14"/>
        <rect :x="SHOP.x" :y="SHOP.y" :width="SHOP.width" :height="SHOP.height" rx="15" class="shop-body"/>
        <path class="shop-top"
              :d="`M${SHOP.x + 15} ${SHOP.y} H${SHOP.x + SHOP.width - 15} Q${SHOP.x + SHOP.width} ${SHOP.y} ${SHOP.x + SHOP.width} ${SHOP.y + 15} V${SHOP.y + 19} H${SHOP.x} V${SHOP.y + 15} Q${SHOP.x} ${SHOP.y} ${SHOP.x + 15} ${SHOP.y} Z`"/>
        <text :x="SHOP.x + 12" :y="SHOP.y + 13.2" class="shop-fixed-label">固定目的地</text>
        <circle :cx="SHOP.x + SHOP.width - 18" :cy="SHOP.y + 9.5" r="8.5" fill="#fffaf0" opacity="0.95"/>
        <path :d="HEART_PATH" :transform="`translate(${SHOP.x + SHOP.width - 23.4} ${SHOP.y + 4}) scale(0.45)`"
              fill="#df5c3f"/>
        <rect :x="SHOP.x + 11" :y="SHOP.y + 26" width="25" height="25" rx="8" class="shop-icon-bg"/>
        <g :transform="`translate(${SHOP.x + 16} ${SHOP.y + 31})`">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#b57822"
               stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" v-html="ICONS.shop"></svg>
        </g>
        <text :x="SHOP.x + 43" :y="SHOP.y + 39" class="shop-label">{{ SHOP.name }}</text>
        <text :x="SHOP.x + 43" :y="SHOP.y + 52" class="shop-subtitle">零食 · 饮料</text>
        <rect :x="SHOP.x + SHOP.width - 14" :y="SHOP.door.y - 16" width="14" height="32" rx="2" class="shop-door"/>
        <circle :cx="SHOP.x + SHOP.width - 4" :cy="SHOP.door.y" r="1.2" fill="#e8c578"/>
        <rect :x="SHOP.x + SHOP.width - 3" :y="SHOP.door.y - 18" width="3" height="36" rx="1.5"
              :fill="SHOP.color" opacity="0.55"/>
        <rect :x="SHOP.x + 13" :y="SHOP.y + 61" width="41" height="16" rx="3" class="shop-window"/>
        <line :x1="SHOP.x + 33.5" :y1="SHOP.y + 62" :x2="SHOP.x + 33.5" :y2="SHOP.y + 76" class="shop-window-line"/>
        <line :x1="SHOP.x + 14" :y1="SHOP.y + 69" :x2="SHOP.x + 53" :y2="SHOP.y + 69" class="shop-window-line"/>
      </g>

      <!-- ========== 校门 ========== -->
      <g aria-hidden="true">
        <path d="M789 650 L789 686" fill="none" stroke="#fcfaf3" stroke-width="9"
              stroke-linecap="round" opacity="0.8"/>
        <g transform="translate(772 684)">
          <rect x="0" y="8" width="38" height="23" rx="3" fill="#f3ead9" stroke="#cbbfa9" stroke-width="1"/>
          <path d="M-3 9 L19 -4 L41 9 Z" fill="#c87554"/>
          <rect x="13" y="17" width="11" height="14" rx="2" fill="#4a6b59"/>
        </g>
        <rect x="815" y="690" width="51" height="18" rx="9" fill="#fffdf7" opacity="0.84"/>
        <text x="840.5" y="703" class="map-gate-label" text-anchor="middle">东北门</text>

        <rect x="1088" y="359" width="58" height="18" rx="9" fill="#fffdf7" opacity="0.84"/>
        <text x="1117" y="372" class="map-gate-label" text-anchor="middle">西南门</text>
        <g transform="translate(1125 382)">
          <rect x="0" y="8" width="26" height="22" rx="3" fill="#f3ead9" stroke="#cbbfa9" stroke-width="1"/>
          <path d="M-3 9 L13 -3 L29 9 Z" fill="#c87554"/>
          <rect x="9" y="17" width="8" height="13" rx="2" fill="#4a6b59"/>
        </g>

        <rect x="1127" y="424" width="19" height="21" rx="4" fill="#537cad" stroke="#fffdf7" stroke-width="1.5"/>
        <text x="1136.5" y="439" class="parking-label" text-anchor="middle">P</text>
      </g>

      <!-- ========== 指北针 & 比例尺 ========== -->
      <g aria-hidden="true">
        <g transform="translate(1118 104)">
          <circle cx="0" cy="0" r="20" fill="#fffdf7" fill-opacity="0.7" stroke="#d8e2d3" stroke-width="1"/>
          <path d="M0 -12 L4.5 6 L0 2.5 L-4.5 6 Z" class="compass-needle" opacity="0.82"/>
          <text x="0" y="-25" class="compass-label" text-anchor="middle">N</text>
        </g>
        <g transform="translate(400 783)">
          <line x1="0" y1="0" x2="300" y2="0" class="scale-line"/>
          <line x1="0" y1="-6" x2="0" y2="6" class="scale-line"/>
          <line x1="300" y1="-6" x2="300" y2="6" class="scale-line"/>
          <line x1="150" y1="-4" x2="150" y2="4" class="scale-line" opacity="0.55"/>
          <text x="150" y="-11" class="scale-label" text-anchor="middle">120 m</text>
        </g>
      </g>

      <!-- ========== 步道点击热区 ========== -->
      <g v-if="interactive" class="path-hit-layer" aria-hidden="true">
        <path :d="mainPath" class="path-hit" stroke-width="40"/>
        <path :d="spurPath" class="path-hit" stroke-width="32"/>
      </g>

      <!-- ========== 悬停吸附预览 ========== -->
      <g v-if="interactive" class="path-preview-layer" aria-hidden="true">
        <circle class="path-preview-halo" :cx="hover ? hover.x : -100" :cy="hover ? hover.y : -100" r="13"/>
        <circle class="path-preview-dot" :cx="hover ? hover.x : -100" :cy="hover ? hover.y : -100" r="4.5"/>
      </g>

      <!-- ========== 最短路线 ========== -->
      <g v-if="route" :key="routeKey" class="route-layer" aria-hidden="true">
        <path class="route-casing" :d="routePath"/>
        <path class="route-line" :d="routePath" :pathLength="'1'"/>
        <path class="route-flow" :d="routePath"/>
        <circle class="route-start-halo" :cx="startPoint.x" :cy="startPoint.y" r="13"/>
        <circle class="route-start-dot" :cx="startPoint.x" :cy="startPoint.y" r="4.8"/>
        <circle class="route-arrival-ring" :cx="endPoint.x" :cy="endPoint.y" r="10"/>
        <circle class="route-arrival-dot" :cx="endPoint.x" :cy="endPoint.y" r="3.8"/>
        <rect v-if="startLabel" :x="startPoint.x - 34" :y="startPoint.y - 52" width="68" height="19" rx="9.5" class="map-chip-bg"/>
        <text v-if="startLabel" :x="startPoint.x" :y="startPoint.y - 38.5" class="map-chip-text" text-anchor="middle">{{ startLabel }}</text>
        <rect v-if="endLabel" :x="endPoint.x - 34" :y="endPoint.y - 52" width="68" height="19" rx="9.5" class="map-chip-bg map-chip-bg--end"/>
        <text v-if="endLabel" :x="endPoint.x" :y="endPoint.y - 38.5" class="map-chip-text" text-anchor="middle">{{ endLabel }}</text>
      </g>
    </svg>

    <!-- 图例 -->
    <div class="map-legend">
      <span class="legend-item"><i class="dot dot-shop"></i>小卖部</span>
      <span class="legend-item"><i class="dot dot-road"></i>可点击步道</span>
      <span class="legend-item"><i class="dot dot-route"></i>最短路线</span>
    </div>

    <!-- 状态条 -->
    <div v-if="route" class="map-chip">
      {{ chipText }}&nbsp;&nbsp;|&nbsp;&nbsp;{{ route.distanceMeters }} 米 · 约 {{ route.durationMinutes }} 分钟
    </div>

    <div v-if="interactive" class="map-hint">
      <el-icon><Location /></el-icon>
      <span>{{ hint || '把鼠标移到白色步道上会出现落点预览，点击即可选择位置' }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import {
  ICONS, HEART_PATH, TREES, AUX_BUILDINGS, LOCATIONS, SHOP,
  pitches, auxWindows, mainPath, spurPath,
  snapToWalkway, nearestLandmark, planRoute, NODES
} from "@/utils/campusMap";

const props = defineProps({
  // 起点：{ x, y } 或节点 id（如 'door.shop'）
  start: { type: [Object, String], default: null },
  // 终点：{ x, y } 或节点 id
  end: { type: [Object, String], default: null },
  // 是否允许点击选点
  interactive: { type: Boolean, default: true },
  startLabel: { type: String, default: '' },
  endLabel: { type: String, default: '' },
  chipText: { type: String, default: '最短路线' },
  hint: { type: String, default: '' }
})

const emit = defineEmits(['pick', 'miss'])

const svgRef = ref(null)
const hover = ref(null)
const routeKey = ref(0)

const route = computed(() => {
  if (!props.start || !props.end) return null
  return planRoute(props.start, props.end)
})

// 路线变化时重播绘制动画
watch(route, () => { routeKey.value += 1 })

const routePath = computed(() => {
  if (!route.value) return ''
  return route.value.points.map((p, i) => `${i === 0 ? 'M' : 'L'}${p[0]} ${p[1]}`).join(' ')
})

const toPoint = (v) => {
  if (!v) return null
  if (typeof v === 'string') {
    const n = NODES[v]
    return n ? { x: n[0], y: n[1] } : null
  }
  return { x: v.x, y: v.y }
}
const startPoint = computed(() => toPoint(props.start) || { x: -100, y: -100 })
const endPoint = computed(() => toPoint(props.end) || { x: -100, y: -100 })

const toSvgPoint = (e) => {
  const svg = svgRef.value
  if (!svg) return null
  const matrix = svg.getScreenCTM()
  if (!matrix) return null
  const pt = svg.createSVGPoint()
  pt.x = e.clientX
  pt.y = e.clientY
  return pt.matrixTransform(matrix.inverse())
}

const onMapClick = (e) => {
  if (!props.interactive) return
  const p = toSvgPoint(e)
  if (!p) return
  const snapped = snapToWalkway(p.x, p.y)
  if (!snapped) {
    emit('miss')
    return
  }
  emit('pick', {
    x: Math.round(snapped.x),
    y: Math.round(snapped.y),
    landmark: nearestLandmark(snapped.x, snapped.y)
  })
}

const onMapHover = (e) => {
  const p = toSvgPoint(e)
  if (!p) return
  const snapped = snapToWalkway(p.x, p.y)
  hover.value = snapped ? { x: snapped.x, y: snapped.y } : null
}
</script>

<style scoped>
.campus-map-frame {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid var(--line, #eceef3);
  box-shadow: var(--shadow-sm, 0 1px 2px rgba(20, 24, 40, 0.04));
}

.campus-svg {
  display: block;
  width: 100%;
  background: #e8efdd;
  font-family: "Microsoft YaHei UI", "Microsoft YaHei", "PingFang SC", "Hiragino Sans GB", "Noto Sans SC", sans-serif;
}

.campus-svg .tree { pointer-events: none; }
.campus-svg .tree-trunk { fill: #a18461; opacity: 0.75; }
.campus-svg .tree-leaf { fill: #8cb383; opacity: 0.82; }
.campus-svg .tree-leaf-light { fill: #a7c897; opacity: 0.64; }

.campus-svg .walkway-base,
.campus-svg .walkway-fill { fill: none; stroke-linecap: round; stroke-linejoin: round; }
.campus-svg .walkway-base { stroke: #d6d0c3; opacity: 0.8; }
.campus-svg .walkway-fill { stroke: #fcfaf3; }
.campus-svg .walkway-center {
  fill: none; stroke: #e9e3d6; stroke-width: 1;
  stroke-dasharray: 2 8; stroke-linecap: round; opacity: 0.75;
}

.campus-svg .door-mark { stroke-width: 2.4; stroke-linecap: round; opacity: 0.72; }

.campus-svg .building-body { fill: #fffdf8; stroke: #d9d1c2; stroke-width: 1.1; }
.campus-svg .building-shadow { fill: #163e32; opacity: 0.09; }
.campus-svg .location-label { fill: #223a2e; font-size: 15px; font-weight: 700; pointer-events: none; }
.campus-svg .location-zone { fill: #8e978c; font-size: 9px; font-weight: 500; pointer-events: none; }
.campus-svg .dorm-title { fill: #3d6350; font-size: 11.5px; font-weight: 700; letter-spacing: 0.08em; pointer-events: none; }

.campus-svg .map-feature-label { fill: #74866d; font-size: 9px; font-weight: 600; letter-spacing: 0.1em; pointer-events: none; }

.campus-svg .aux-building { pointer-events: none; }
.campus-svg .aux-body { fill: #f7f4eb; stroke: #dcd5c8; stroke-width: 1; }
.campus-svg .aux-label { fill: #63736a; font-size: 11px; font-weight: 700; }
.campus-svg .aux-sub { fill: #9ba39a; font-size: 7.5px; font-weight: 500; }

.campus-svg .map-gate-label { fill: #5d7164; font-size: 8.5px; font-weight: 700; letter-spacing: 0.08em; }
.campus-svg .parking-label { fill: #ffffff; font-size: 11px; font-weight: 700; font-family: Consolas, monospace; }

.campus-svg .map-location { cursor: default; outline: none; pointer-events: none; }
.campus-svg .building-arrow { opacity: 0.34; }

.campus-svg .field-label-bg { fill: rgba(255, 253, 247, 0.9); }
.campus-svg .field-label { fill: #2c5140; font-size: 13px; font-weight: 700; letter-spacing: 0.08em; }

.campus-svg .map-shop { pointer-events: none; }
.campus-svg .shop-body { fill: #fffaf0; stroke: #e4b974; stroke-width: 1.4; }
.campus-svg .shop-top { fill: #e7a13b; }
.campus-svg .shop-fixed-label { fill: rgba(255, 250, 238, 0.9); font-size: 7.2px; font-weight: 700; letter-spacing: 0.08em; }
.campus-svg .shop-label { fill: #3e2e18; font-size: 16px; font-weight: 800; }
.campus-svg .shop-subtitle { fill: #9b7b4e; font-size: 7.6px; font-weight: 500; }
.campus-svg .shop-icon-bg { fill: #f7e5c5; }
.campus-svg .shop-window { fill: #f8ebd2; stroke: #dab77c; stroke-width: 1; }
.campus-svg .shop-window-line { stroke: #dab77c; stroke-width: 0.8; }
.campus-svg .shop-door { fill: #315747; }

.campus-svg .compass-needle { fill: #355c49; }
.campus-svg .compass-label { fill: #70826f; font-size: 8px; font-weight: 700; letter-spacing: 0.1em; }
.campus-svg .scale-line { stroke: #7d8d7d; stroke-width: 1.7; stroke-linecap: round; }
.campus-svg .scale-label { fill: #849284; font-size: 9.5px; font-weight: 600; font-family: Consolas, monospace; }

.campus-svg .path-hit {
  fill: none;
  stroke: rgba(255, 255, 255, 0.001);
  stroke-linecap: round;
  stroke-linejoin: round;
  pointer-events: stroke;
  cursor: crosshair;
}

.campus-svg .path-preview-layer { pointer-events: none; }
.campus-svg .path-preview-halo { fill: rgba(228, 102, 52, 0.13); stroke: none; opacity: 0; }
.campus-svg .path-preview-dot { fill: #e46634; stroke: #fffaf0; stroke-width: 2.4; opacity: 0; }
.campus-svg.has-path-preview .path-preview-halo,
.campus-svg.has-path-preview .path-preview-dot { opacity: 1; }

.campus-svg .route-layer { pointer-events: none; }
.campus-svg .route-casing,
.campus-svg .route-line,
.campus-svg .route-flow { fill: none; stroke-linecap: round; stroke-linejoin: round; }
.campus-svg .route-casing { stroke: #fffaf0; stroke-width: 15; opacity: 0.97; }
.campus-svg .route-line {
  stroke: #e56634; stroke-width: 8;
  stroke-dasharray: 1; stroke-dashoffset: 1;
  animation: routeDraw 0.65s ease-out forwards;
}
.campus-svg .route-flow {
  stroke: rgba(255, 239, 221, 0.85); stroke-width: 2.2;
  stroke-dasharray: 0.1 13;
  animation: routeDashes 1.8s linear infinite;
}
.campus-svg .route-start-halo {
  fill: rgba(229, 102, 52, 0.16);
  transform-box: fill-box; transform-origin: center;
  animation: routePulse 2s ease-out infinite;
}
.campus-svg .route-start-dot { fill: #e56634; stroke: #fffaf0; stroke-width: 2.6; }
.campus-svg .route-arrival-ring { fill: rgba(231, 161, 59, 0.12); stroke: #e7a13b; stroke-width: 2; }
.campus-svg .route-arrival-dot { fill: #e7a13b; stroke: #fffaf0; stroke-width: 2; }

.campus-svg .map-chip-bg { fill: #e56634; }
.campus-svg .map-chip-bg--end { fill: #3d6350; }
.campus-svg .map-chip-text { fill: #fffaf1; font-size: 9px; font-weight: 700; letter-spacing: 0.06em; }

.campus-svg.has-route .map-location,
.campus-svg.has-route .aux-building { opacity: 0.7; transition: opacity 0.25s ease; }

@keyframes routeDraw { to { stroke-dashoffset: 0; } }
@keyframes routeDashes { to { stroke-dashoffset: -13; } }
@keyframes routePulse {
  0% { transform: scale(0.6); opacity: 0.9; }
  70% { transform: scale(1.5); opacity: 0; }
  100% { transform: scale(1.5); opacity: 0; }
}

/* 浮层 */
.map-legend {
  position: absolute;
  top: 14px; right: 16px;
  display: flex; align-items: center; gap: 14px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #e3e9d8;
  font-size: 12px; color: #5a6a5e;
}
.legend-item { display: inline-flex; align-items: center; gap: 5px; }
.legend-item .dot { display: inline-block; border-radius: 50%; }
.dot-shop { width: 10px; height: 10px; background: #E7A13B; }
.dot-road { width: 12px; height: 6px; border-radius: 3px; background: #fff; border: 1px solid #d7e3c8; }
.dot-route { width: 14px; height: 3px; border-radius: 2px; background: #E46634; }

.map-chip {
  position: absolute;
  right: 16px; bottom: 14px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(42, 52, 44, 0.92);
  color: #fff; font-size: 12.5px;
  box-shadow: 0 6px 16px rgba(20, 30, 20, 0.25);
}

.map-hint {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px;
  font-size: 12.5px;
  color: var(--text-3, #8c93a8);
  background: #fff;
  border-top: 1px solid var(--line, #eceef3);
}
</style>
