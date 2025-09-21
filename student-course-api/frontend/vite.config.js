import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
    server: {
      port: 5173,
      proxy: {
          // redireciona tudo que começar com /api para o backend em http://localhost:8080
          '/api': {
              target: 'http://localhost:8080',
              changeOrigin: true,
              secure: false,
              // remove o /api apenas se necessário: rewrite: (path) => path.replace(/^\/api/, '')
          }
      }
    }
})
