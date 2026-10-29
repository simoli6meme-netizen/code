import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

import ElementPlus from 'unplugin-element-plus/vite'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver({ importStyle: 'sass' })],
    }),
    Components({
      resolvers: [ElementPlusResolver({ importStyle: 'sass' })],
    }),
    ElementPlus({
      useSource: true,
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    host: true,
    allowedHosts: true,
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 文件接口也转发到后端，保证图片/上传在公网隧道下同源可用
      '/files': {
        target: 'http://localhost:9090',
        changeOrigin: true
      }
    },
    watch: {
      ignored: ['**/*.tmp', '**/*.tmpdir/**', '**/.*.tmpdir/**', '**/.~*']
    }
  },
  // 生产预览：托管打包后的前端，并把 /api 反代到本机后端(9090)
  // 这样 cpolar 只需穿透 4173 一个端口，前端与接口同源，免重建、免跨域
  preview: {
    host: true,
    port: 4173,
    allowedHosts: true,
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 文件接口也转发到后端，保证图片/上传在公网隧道下同源可用
      '/files': {
        target: 'http://localhost:9090',
        changeOrigin: true
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `
          @use "@/assets/css/index.scss" as *;
        `,
        silenceDeprecations: ['legacy-js-api']
      }
    }
  }
})