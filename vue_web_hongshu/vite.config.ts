import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vitejs.dev/config/
import { resolve } from "path";
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": resolve(__dirname, "./src"),
    },
    //extensions: [".ts", ".js", ".vue", ".json", ".mjs"],
    extensions: [".mjs", ".js", ".ts", ".jsx", ".tsx", ".json", ".vue"],
  },
  // server: {
  //   port: 3000,
  //   open: true,
  //   // 配置代理
  //   proxy: {
  //     // 请求的路径前缀只要是 /testaxios 就会被拦截走这个代理
  //     '/oss': {
  //     /**
  //       *  请求的目标资源再经过替换成 /httphwm/getList 后，
  //       *  会加上 http://127.0.0.1:9693 这个前缀，
  //       *  最后请求的URL为: http://127.0.0.1:9693/httphwm/getList
  //       */
  //       target: 'http://localhost:8080/',
  //       ws: true,
  //       changeOrigin: true,
  //       // 拦截到的请求路径 testaxios/httphwm/getList，/testaxios会被替换成空
  //       rewrite: (path) => path.replace(/^\/oss/, 'oss'),
  //     },
  //   },
  // }

  server: {
    port: 80,
    host: true,
    open: true,
    proxy: {
      // https://cn.vitejs.dev/config/#server-proxy
      '/dev-api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/dev-api/, '')
      },
      '/profile': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 小红书图片代理 - 绕过防盗链
      '/xhspic': {
        target: 'https://sns-webpic-qc.xhscdn.com',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/xhspic/, ''),
        configure: (proxy, _options) => {
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            // 设置正确的请求头，绕过防盗链
            proxyReq.setHeader('Referer', 'https://www.xiaohongshu.com');
            proxyReq.setHeader('Host', 'sns-webpic-qc.xhscdn.com');
            proxyReq.setHeader('Origin', '');
          });
        }
      },
      // 小红书视频代理 - 绕过防盗链
      '/xhsvideo': {
        target: 'https://sns-video-bd.xhscdn.com',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/xhsvideo/, ''),
        configure: (proxy, _options) => {
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            // 设置正确的请求头，绕过防盗链
            proxyReq.setHeader('Referer', 'https://www.xiaohongshu.com');
            proxyReq.setHeader('Host', 'sns-video-bd.xhscdn.com');
            proxyReq.setHeader('Origin', '');
            // 转发 Range 请求头（视频分段加载必需）
            if (req.headers.range) {
              proxyReq.setHeader('Range', req.headers.range);
            }
            if (req.headers['if-range']) {
              proxyReq.setHeader('If-Range', req.headers['if-range']);
            }
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            // 设置 CORS 头
            proxyRes.headers['access-control-allow-origin'] = '*';
            proxyRes.headers['access-control-allow-methods'] = 'GET, HEAD, OPTIONS';
            proxyRes.headers['access-control-allow-headers'] = 'DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range';
            proxyRes.headers['access-control-expose-headers'] = 'Content-Length,Content-Range,Accept-Ranges';
            // 支持 Range 请求
            if (!proxyRes.headers['accept-ranges']) {
              proxyRes.headers['accept-ranges'] = 'bytes';
            }
          });
        }
      }
    }
  },
});
