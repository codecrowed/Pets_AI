import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    chunkSizeWarningLimit: 600,
    rollupOptions: {
      output: {
        manualChunks(id: string) {
          if (!id.includes('node_modules')) return undefined
          if (id.includes('react-markdown') || id.includes('remark-gfm') || id.includes('remark-breaks')) {
            return 'markdown-vendor'
          }
          if (id.includes('animal-island-ui')) {
            return 'ui-vendor'
          }
          if (id.includes('/react/') || id.includes('/react-dom/') || id.includes('/scheduler/')) {
            return 'react-vendor'
          }
          return undefined
        },
      },
    },
  },
})
