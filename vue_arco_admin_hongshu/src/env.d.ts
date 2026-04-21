/// <reference types="vite/client" />

declare module '*.vue' {
    import {DefineComponent} from 'vue';
    // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/ban-types
    const component: DefineComponent<{}, {}, any>;
    export default component;
}

declare module '*.mp4' {
    const src: string;
    export default src;
}

interface ImportMetaEnv {
    readonly VITE_API_BASE_URL: string;
    /** 可选：网关 Swagger UI 在浏览器中的路径前缀；不填时生产环境用 BASE_URL（如 /arco-admin）、开发环境用 VITE_APP_BASE_API */
    readonly VITE_APP_SWAGGER_BASE?: string;
    /** Nacos 控制台完整地址，如 http://localhost:8848/nacos 或 http://服务器IP:8848/nacos；生产环境建议必配 */
    readonly VITE_APP_NACOS_URL?: string;
}
