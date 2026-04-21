import {mergeConfig} from 'vite';
import eslint from 'vite-plugin-eslint';
import baseConfig from './vite.config.base';

/** 本地网关；Swagger UI 会请求根路径 /v3/api-docs、/{serviceId}/v3/api-docs，需直连网关（不能只走 /dev-api 前缀） */
const GATEWAY_DEV_TARGET = 'http://localhost:8080';

const HONGSHU_DISCOVERY_SERVICE_PREFIXES = [
    'hongshu-auth',
    'hongshu-job',
    'hongshu-system',
    'hongshu-file',
    'hongshu-web',
    'hongshu-idle',
    'hongshu-ai',
    'hongshu-im',
] as const;

function buildGatewayDiscoveryProxies(): Record<
    string,
    { target: string; changeOrigin: boolean }
> {
    const map: Record<string, { target: string; changeOrigin: boolean }> = {};
    for (const p of HONGSHU_DISCOVERY_SERVICE_PREFIXES) {
        map[`/${p}`] = { target: GATEWAY_DEV_TARGET, changeOrigin: true };
    }
    return map;
}

export default mergeConfig(
    {
        mode: 'development',
        server: {
            // 是否自动在浏览器打开
            open: true,
            // 端口
            port: 8991,
            // 是否开启 https
            https: false,
            fs: {
                strict: true,
            },
            // 设置反向代理，跨域
            proxy: {
                '/dev-api': {
                    target: GATEWAY_DEV_TARGET,
                    changeOrigin: true,
                    secure: false, // 如果是https接口，需要配置这个参数
                    rewrite: (path: string) => path.replace(/^\/dev-api/, '')
                },
                '/v3/api-docs': {
                    target: GATEWAY_DEV_TARGET,
                    changeOrigin: true,
                    secure: false,
                },
                '/swagger-ui': {
                    target: GATEWAY_DEV_TARGET,
                    changeOrigin: true,
                    secure: false,
                },
                ...buildGatewayDiscoveryProxies(),
                // 小红书图片代理（本地开发环境）
                // 注意：使用 /xhspic/ 确保匹配所有以 /xhspic/ 开头的路径
                '/xhspic/': {
                    target: 'https://sns-webpic-qc.xhscdn.com',
                    changeOrigin: true,
                    secure: false,
                    rewrite: (path: string) => path.replace(/^\/xhspic\//, '/'),
                    configure: (proxy, _options) => {
                        proxy.on('proxyReq', (proxyReq, req, _res) => {
                            // 设置正确的请求头，绕过防盗链（模拟真实浏览器请求）
                            proxyReq.setHeader('Referer', 'https://www.xiaohongshu.com/');
                            proxyReq.setHeader('Origin', 'https://www.xiaohongshu.com');
                            proxyReq.setHeader('Host', 'sns-webpic-qc.xhscdn.com');
                            proxyReq.setHeader('User-Agent', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36');
                            proxyReq.setHeader('Accept', 'image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8');
                            proxyReq.setHeader('Accept-Encoding', 'gzip, deflate, br');
                            proxyReq.setHeader('Accept-Language', 'zh-CN,zh;q=0.9,en;q=0.8');
                            // 添加更多浏览器请求头，模拟真实请求
                            proxyReq.setHeader('Sec-Fetch-Site', 'cross-site');
                            proxyReq.setHeader('Sec-Fetch-Mode', 'no-cors');
                            proxyReq.setHeader('Sec-Fetch-Dest', 'image');
                            proxyReq.setHeader('Cache-Control', 'no-cache');
                            // 移除可能暴露代理的头
                            proxyReq.removeHeader('X-Forwarded-For');
                            proxyReq.removeHeader('X-Real-IP');
                            proxyReq.removeHeader('Via');
                        });
                        proxy.on('proxyRes', (proxyRes, req, res) => {
                            // 设置 CORS 头
                            proxyRes.headers['access-control-allow-origin'] = '*';
                            proxyRes.headers['access-control-allow-methods'] = 'GET, HEAD, OPTIONS';
                        });
                    }
                },
                // 小红书视频代理（本地开发环境）
                // 注意：使用 /xhsvideo/ 确保匹配所有以 /xhsvideo/ 开头的路径
                '/xhsvideo/': {
                    target: 'https://sns-video-bd.xhscdn.com',
                    changeOrigin: true,
                    secure: false,
                    rewrite: (path: string) => path.replace(/^\/xhsvideo\//, '/'),
                    configure: (proxy, _options) => {
                        proxy.on('proxyReq', (proxyReq, req, _res) => {
                            // 设置正确的请求头，绕过防盗链（与Nginx配置保持一致）
                            proxyReq.setHeader('Referer', 'https://www.xiaohongshu.com');
                            proxyReq.setHeader('Origin', ''); // 设置为空字符串，与Nginx配置一致
                            proxyReq.setHeader('Host', 'sns-video-bd.xhscdn.com');
                            proxyReq.setHeader('User-Agent', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36');
                            proxyReq.setHeader('Accept', '*/*');
                            proxyReq.setHeader('Accept-Encoding', 'gzip, deflate, br');
                            proxyReq.setHeader('Accept-Language', 'zh-CN,zh;q=0.9,en;q=0.8');
                            // 移除可能暴露代理的头
                            proxyReq.removeHeader('X-Forwarded-For');
                            proxyReq.removeHeader('X-Real-IP');
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
            },
        },
        plugins: [
            eslint({
                cache: false,
                include: ['src/**/*.ts', 'src/**/*.tsx', 'src/**/*.vue'],
                exclude: ['node_modules'],
            }),
        ],
    },
    baseConfig
);
