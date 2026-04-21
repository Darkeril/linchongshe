package com.hongshu.web.websocket;

import com.hongshu.common.core.domain.Message;
import com.hongshu.web.config.ServerEncoder;
import com.hongshu.web.im.ChatUtils;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: hongshu
 */
@ServerEndpoint(value = "/ws/{uid}", encoders = {ServerEncoder.class})
@Component
@Slf4j
public class WebSocketServer implements ApplicationContextAware {

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static final AtomicInteger ONLINE_NUM = new AtomicInteger();

    //concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketServer对象。
    private static final ConcurrentHashMap<String, Session> SESSION_POOLS = new ConcurrentHashMap<>();

    // 记录每个连接的最后活动时间（用于保活检测）
    private static final ConcurrentHashMap<String, Long> LAST_ACTIVE_TIME = new ConcurrentHashMap<>();

    private final Object lockObj = new Object();

    private SystemWebSocketNotifyPublisher systemWebSocketNotifyPublisher;

    // 静态 ApplicationContext，用于获取 Bean
    private static ApplicationContext applicationContext;

    @Autowired
    public void setSystemWebSocketNotifyPublisher(SystemWebSocketNotifyPublisher systemWebSocketNotifyPublisher) {
        this.systemWebSocketNotifyPublisher = systemWebSocketNotifyPublisher;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    //发送消息
    public void sendMessage(Session session, Message message) {
        if (session != null) {
            synchronized (lockObj) {
                log.info("发送数据={}", message);
                try {
                    session.getBasicRemote().sendObject(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //给指定用户发送信息
    public void sendInfo(Message message) {
        String acceptUid = message.getAcceptUid();
        
        // 🔧 客服场景特殊处理：如果消息是发送给客服的（acceptUid = "0"），推送给所有在线的管理员
        if ("0".equals(acceptUid)) {
            // 推送给所有在线的管理员（所有连接的 WebSocket 会话）
            // 注意：这里假设所有管理员都连接了 WebSocket，实际使用时可能需要过滤出管理员
            for (Session session : SESSION_POOLS.values()) {
                try {
                    sendMessage(session, message);
                } catch (Exception e) {
                    log.error("发送客服消息给管理员失败", e);
                }
            }
        } else {
            // 普通消息，只发送给指定的接收方
            Session session = SESSION_POOLS.get(acceptUid);
            try {
                sendMessage(session, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 群发消息
    public void broadcast(Message message) {
        int n = SESSION_POOLS.size();
        log.info("WebSocket 本地广播: 会话数={}, msgType={}, sendUid={}", n, message.getMsgType(), message.getSendUid());
        for (Session session : SESSION_POOLS.values()) {
            try {
                sendMessage(session, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送群聊消息给指定用户列表
     *
     * @param message 消息
     * @param userIds 用户ID列表
     */
    public void sendToGroupMembers(Message message, List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (String userId : userIds) {
            Session session = SESSION_POOLS.get(userId);
            if (session != null) {
                try {
                    sendMessage(session, message);
                } catch (Exception e) {
                    log.error("发送群聊消息给用户{}失败", userId, e);
                }
            }
        }
    }

    // 发送系统通知
    public void sendSystemNotification(String content) {
        sendSystemNotification(content, null);
    }

    /**
     * @param imageUrl 可选配图，C 端 msgType=99 时与 content 一并下发
     */
    public void sendSystemNotification(String content, String imageUrl) {
        Message message = new Message();
        message.setSendUid("system");
        message.setAcceptUid("all");
        message.setContent(content);
        message.setImageUrl(imageUrl);
        message.setMsgType(99); // 系统通知类型
        message.setChatType(0);

        // 经 Redis 发布，各节点订阅后向本地会话广播（网关多实例时与 HTTP 入口不在同一 JVM 也能送达）
        if (systemWebSocketNotifyPublisher != null) {
            try {
                systemWebSocketNotifyPublisher.publish(message);
                return;
            } catch (Exception e) {
                log.warn("Redis 发布系统通知失败，降级为仅本节点广播", e);
            }
        }
        broadcast(message);
    }

    //建立连接成功调用
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "uid") String uid) {
        SESSION_POOLS.put(uid, session);
        LAST_ACTIVE_TIME.put(uid, System.currentTimeMillis()); // 记录连接时间
        addOnlineCount();
        log.info("{}加入webSocket！当前人数为={}", uid, ONLINE_NUM);

        // ✅ 用户上线时主动推送离线期间的计数消息
        try {
            if (applicationContext != null) {
                ChatUtils chatUtils = applicationContext.getBean(ChatUtils.class);
                if (chatUtils != null) {
                    // 延迟100ms推送，确保连接完全建立
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                            chatUtils.pushCountMessageOnConnect(uid);
                        } catch (Exception e) {
                            log.error("推送上线消息异常: uid={}", uid, e);
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            log.error("获取ChatUtils失败: uid={}", uid, e);
        }
    }

    //关闭连接时调用
    @OnClose
    public void onClose(@PathParam(value = "uid") String uid) {
        SESSION_POOLS.remove(uid);
        LAST_ACTIVE_TIME.remove(uid); // 清理活动时间记录
        subOnlineCount();
        log.info("{}断开webSocket连接！当前人数为={}", uid, ONLINE_NUM);
    }

    //收到客户端信息后，根据接收人的username把消息推下去或者群发
    // to=-1群发消息
    @OnMessage
    public void onMessage(String message, Session session, @PathParam(value = "uid") String uid) {
        // 更新最后活动时间
        LAST_ACTIVE_TIME.put(uid, System.currentTimeMillis());
        
        try {
            // 解析消息（可能是 JSON 格式）
            if (message != null && message.trim().startsWith("{")) {
                // 尝试解析为 JSON（简单判断，实际应该用 JSON 库）
                // 如果是心跳消息（msgType: 0, content: 'ping'），只记录日志，不回复
                if (message.contains("\"msgType\":0") || message.contains("\"messageType\":0") || 
                    message.contains("\"content\":\"ping\"")) {
                    log.debug("收到心跳消息 from uid={}", uid);
                    // 心跳消息已通过更新 LAST_ACTIVE_TIME 处理，不需要额外回复
                    return;
                }
            }
            log.info("收到客户端消息 from uid={}, message={}", uid, message);
        } catch (Exception e) {
            log.warn("处理客户端消息异常 from uid={}, message={}", uid, message, e);
        }
    }

    //错误时调用
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("发生错误", throwable);
        throwable.printStackTrace();
    }

    public static void addOnlineCount() {
        ONLINE_NUM.incrementAndGet();
    }

    public static void subOnlineCount() {
        ONLINE_NUM.decrementAndGet();
    }

    public static AtomicInteger getOnlineNumber() {
        return ONLINE_NUM;
    }

    public static ConcurrentMap<String, Session> getSessionPools() {
        return SESSION_POOLS;
    }

    /**
     * 获取连接的最后活动时间
     * @param uid 用户ID
     * @return 最后活动时间（毫秒时间戳），如果不存在返回 null
     */
    public static Long getLastActiveTime(String uid) {
        return LAST_ACTIVE_TIME.get(uid);
    }

    /**
     * 检查并清理超时的连接（可选，由定时任务调用）
     * 注意：由于客户端已有自动重连机制，这个方法主要用于资源清理，不是必须的
     * @param timeoutMillis 超时时间（毫秒），默认 120 秒（2分钟）
     */
    public static void cleanupIdleConnections(long timeoutMillis) {
        long now = System.currentTimeMillis();
        LAST_ACTIVE_TIME.entrySet().removeIf(entry -> {
            String uid = entry.getKey();
            long lastActive = entry.getValue();
            if (now - lastActive > timeoutMillis) {
                Session session = SESSION_POOLS.get(uid);
                if (session != null && session.isOpen()) {
                    try {
                        log.warn("检测到超时连接，关闭连接: uid={}, 超时时间={}ms", uid, now - lastActive);
                        session.close();
                    } catch (Exception e) {
                        log.error("关闭超时连接失败: uid={}", uid, e);
                    }
                }
                SESSION_POOLS.remove(uid);
                subOnlineCount();
                return true;
            }
            return false;
        });
    }
}
