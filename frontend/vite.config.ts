import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 9080,//端口号
    host: true,//ip地址 或 '0.0.0.0' 或 "loaclhost"
    open: false, //启动后是否自动打开浏览器
    https: false, // 是否开启 https
    proxy: {
      '/api': {
        target: 'http://36.150.237.57/api', //服务器
        changeOrigin: true,
        ws: true,
        hotOnly: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
    }
  },
});

