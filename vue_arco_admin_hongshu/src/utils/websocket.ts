import { ref, onUnmounted } from 'vue';
import { getToken } from '@/utils/auth';

/**
 * WebSocket 工具类
 */
export class WebSocketUtil {
  private ws: WebSocket | null = null;

  private url: string;

  private reconnectTimer: NodeJS.Timeout | null = null;

  private heartbeatTimer: NodeJS.Timeout | null = null;

  private maxRetries = 5;

  private retryCount = 0;

  private reconnectDelay = 1000;

  private isManualClose = false;

  private uid: string; // 保存用户ID，用于发送心跳

  private lastMessageTime = Date.now(); // 最后一次收到消息的时间

  private readonly HEARTBEAT_INTERVAL = 30000; // 心跳间隔：30秒

  private readonly HEARTBEAT_TIMEOUT = 60000; // 心跳超时：60秒

  // 消息处理器
  private messageHandlers: Map<number, (message: any) => void> = new Map();

  constructor(uid: string) {
    this.uid = uid;
    // 构建 WebSocket URL
    // 参考 web 端的配置：ws://localhost:8080/web/ws/
    const { protocol } = window.location;
    const wsProtocol = protocol === 'https:' ? 'wss:' : 'ws:';
    const baseUrl = import.meta.env.VITE_APP_BASE_API || '/dev-api';
    
    // 从 baseUrl 中提取基础路径
    let wsHost: string;
    // web 端配置是 /web/ws/，所以使用相同的路径
    let wsPath = '/web/ws';
    
    if (baseUrl.startsWith('http://') || baseUrl.startsWith('https://')) {
      // 完整 URL，例如：http://localhost:9002
      wsHost = baseUrl.replace(/^https?:\/\//, '').replace(/\/$/, '');
    } else if (baseUrl.startsWith('/')) {
      // 相对路径，例如：/dev-api
      // /dev-api 是前端开发代理路径，WebSocket 应该直接连接到后端
      // 根据 vite.config.dev.ts，代理目标是 http://localhost:8080
      // 所以 WebSocket 应该连接到 localhost:8080，而不是当前前端端口
      const { host, hostname } = window.location;
      // 如果当前是开发环境（8991端口），则连接到后端端口 8080
      if (host.includes('8991') || host.includes('localhost')) {
        // 提取 hostname，使用后端端口 8080
        wsHost = `${hostname}:8080`;
      } else {
        // 生产环境，使用当前 host
        wsHost = host;
      }
      wsPath = '/web/ws';
    } else {
      // 直接使用 baseUrl 作为 host
      wsHost = baseUrl.replace(/\/$/, '');
    }
    
    // 后端 WebSocket 路径是 /web/ws/{uid}（根据 web 端配置）
    this.url = `${wsProtocol}//${wsHost}${wsPath}/${uid}`;
    console.log('🔗 WebSocket URL:', this.url);
    console.log('🔗 WebSocket Host:', wsHost);
    console.log('🔗 WebSocket Path:', wsPath);
    console.log('🔗 Base URL:', baseUrl);
    console.log('🔗 Current Location:', window.location.href);
  }

  /**
   * 连接 WebSocket
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        console.log('🔌 正在连接 WebSocket...', this.url);
        this.ws = new WebSocket(this.url);

        this.ws.onopen = () => {
          console.log('✅ WebSocket 连接成功，URL:', this.url);
          this.retryCount = 0;
          this.isManualClose = false;
          this.lastMessageTime = Date.now(); // 重置最后消息时间
          this.startHeartbeat();
          resolve();
        };

        this.ws.onmessage = (event) => {
          try {
            const message = JSON.parse(event.data);
            this.lastMessageTime = Date.now(); // 更新最后消息时间
            console.log('📨 收到 WebSocket 消息:', message);
            console.log('📨 消息类型 (msgType):', message.msgType);
            this.handleMessage(message);
          } catch (error) {
            console.error('❌ 解析 WebSocket 消息失败:', error, '原始数据:', event.data);
          }
        };

        this.ws.onerror = (error) => {
          console.error('❌ WebSocket 错误:', error);
          console.error('❌ WebSocket URL:', this.url);
          console.error('❌ WebSocket readyState:', this.ws?.readyState);
          // 不立即 reject，等待 onclose 事件
        };

        this.ws.onclose = (event) => {
          console.log('🔌 WebSocket 连接关闭', {
            code: event.code,
            reason: event.reason,
            wasClean: event.wasClean,
            url: this.url,
          });
          this.stopHeartbeat();
          
          // 如果连接失败（非正常关闭），且不是手动关闭，则重连
          if (!event.wasClean && !this.isManualClose) {
            console.log('🔄 连接异常关闭，准备重连...');
            // 如果连接失败，可能是路径不对，尝试使用 /web/ws 路径
            if (this.url.includes('/ws/') && !this.url.includes('/web/ws') && this.retryCount === 0) {
              console.log('🔄 尝试使用 /web/ws 路径重新连接...');
              this.url = this.url.replace('/ws/', '/web/ws/');
              console.log('🔄 新的 WebSocket URL:', this.url);
            }
            this.reconnect();
          } else if (this.isManualClose) {
            console.log('✅ 手动关闭连接');
          }
          
          // 只有在非正常关闭时才 reject
          if (!event.wasClean && !this.isManualClose) {
            reject(new Error(`WebSocket 连接失败: code=${event.code}, reason=${event.reason}`));
          }
        };
      } catch (error) {
        console.error('❌ WebSocket 连接失败:', error);
        reject(error);
      }
    });
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.isManualClose = true;
    this.stopHeartbeat();
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  /**
   * 重连
   */
  private reconnect() {
    if (this.retryCount >= this.maxRetries) {
      console.log('已达到最大重试次数，停止重连');
      return;
    }

    this.retryCount += 1;
    const delay = Math.min(this.reconnectDelay * this.retryCount, 5000);
    console.log(`尝试重新连接，第 ${this.retryCount} 次重试，将在 ${delay} 毫秒后重试`);

    this.reconnectTimer = setTimeout(() => {
      this.connect().catch((error) => {
        console.error('重连失败:', error);
      });
    }, delay);
  }

  /**
   * 心跳检测
   */
  private startHeartbeat() {
    // 清除旧的心跳定时器
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
    }
    
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        // 检查是否超时（60秒内没有收到任何消息）
        const now = Date.now();
        if (now - this.lastMessageTime > this.HEARTBEAT_TIMEOUT) {
          console.warn('⚠️ WebSocket 心跳超时，连接可能已断开，尝试重连');
          // 关闭当前连接并重连
          if (this.ws) {
            this.ws.close();
          }
          return;
        }
        
        // 发送心跳消息（使用 msgType: 0 作为心跳，与后端保持一致）
        try {
          this.send({
            sendUid: this.uid,
            msgType: 0, // 心跳消息类型
            content: 'ping'
          });
          console.log('💓 发送心跳消息');
        } catch (error) {
          console.error('发送心跳消息失败:', error);
        }
      } else {
        console.warn('⚠️ WebSocket 未连接，无法发送心跳');
      }
    }, this.HEARTBEAT_INTERVAL); // 30秒发送一次心跳
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 发送消息
   */
  send(data: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
    } else {
      console.warn('WebSocket 未连接，无法发送消息');
    }
  }

  /**
   * 注册消息处理器
   */
  onMessage(msgType: number, handler: (message: any) => void) {
    this.messageHandlers.set(msgType, handler);
  }

  /**
   * 移除消息处理器
   */
  offMessage(msgType: number) {
    this.messageHandlers.delete(msgType);
  }

  /**
   * 处理消息
   */
  private handleMessage(message: any) {
    const handler = this.messageHandlers.get(message.msgType);
    if (handler) {
      handler(message);
    }
  }

  /**
   * 获取连接状态
   */
  get readyState(): number {
    return this.ws?.readyState ?? WebSocket.CLOSED;
  }

  /**
   * 是否已连接
   */
  get isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }
}

/**
 * 使用 WebSocket 的 composable
 */
export function useWebSocket(uid: string) {
  const ws = ref<WebSocketUtil | null>(null);
  const isConnected = ref(false);

  const connect = async () => {
    try {
      const websocket = new WebSocketUtil(uid);
      await websocket.connect();
      ws.value = websocket;
      isConnected.value = true;
    } catch (error) {
      console.error('WebSocket 连接失败:', error);
      isConnected.value = false;
    }
  };

  const disconnect = () => {
    if (ws.value) {
      ws.value.disconnect();
      ws.value = null;
      isConnected.value = false;
    }
  };

  const send = (data: any) => {
    if (ws.value) {
      ws.value.send(data);
    }
  };

  const onMessage = (msgType: number, handler: (message: any) => void) => {
    if (ws.value) {
      ws.value.onMessage(msgType, handler);
    }
  };

  const offMessage = (msgType: number) => {
    if (ws.value) {
      ws.value.offMessage(msgType);
    }
  };

  onUnmounted(() => {
    disconnect();
  });

  return {
    ws,
    isConnected,
    connect,
    disconnect,
    send,
    onMessage,
    offMessage,
  };
}

