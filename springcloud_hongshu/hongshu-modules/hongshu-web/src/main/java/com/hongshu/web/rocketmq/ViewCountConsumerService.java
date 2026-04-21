package com.hongshu.web.rocketmq;

import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.message.ViewCountMessage;
import com.hongshu.web.mapper.web.WebNoteMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 浏览数消费者服务
 * 异步更新数据库中的浏览数
 *
 * @author: hongshu
 */
@Slf4j
@Service
// 添加条件注解，当配置为 false 时不加载
@ConditionalOnProperty(name = "rocketmq.consumer.listen", havingValue = "true")
@RocketMQMessageListener(
        topic = "view-count-topic",
        consumerGroup = "view-count-consumer-group"
)
public class ViewCountConsumerService implements RocketMQListener<ViewCountMessage> {

    @Autowired
    private WebNoteMapper noteMapper;

    // TODO: 需要注入 IdleProductMapper，暂时注释
    // @Autowired
    // private IdleProductMapper productMapper;

    @Override
    public void onMessage(ViewCountMessage message) {
        try {
            log.info("收到浏览数更新消息: type={}, id={}, count={}",
                    message.getType(), message.getId(), message.getCount());

            if ("note".equals(message.getType())) {
                updateNoteViewCount(message.getId(), message.getCount());
            } else if ("product".equals(message.getType())) {
                updateProductViewCount(message.getId(), message.getCount());
            } else {
                log.warn("未知的浏览数类型: {}", message.getType());
            }
        } catch (Exception e) {
            log.error("处理浏览数消息失败: {}", message, e);
            throw e; // 抛出异常触发重试
        }
    }

    /**
     * 更新笔记浏览数
     *
     * @param noteId 笔记ID
     * @param count  浏览数
     */
    private void updateNoteViewCount(String noteId, Long count) {
        try {
            WebNote note = new WebNote();
            note.setId(noteId);
            note.setViewCount(count);

            int rows = noteMapper.updateById(note);

            if (rows > 0) {
                log.info("更新笔记浏览数成功: noteId={}, count={}", noteId, count);
            } else {
                log.warn("更新笔记浏览数失败，笔记不存在: noteId={}", noteId);
            }
        } catch (Exception e) {
            log.error("更新笔记浏览数异常: noteId={}, count={}", noteId, count, e);
            throw e;
        }
    }

    /**
     * 更新商品浏览数
     *
     * @param productId 商品ID
     * @param count     浏览数
     */
    private void updateProductViewCount(String productId, Long count) {
        try {
            // TODO: 实现商品浏览数更新
            // IdleProduct product = new IdleProduct();
            // product.setId(productId);
            // product.setViewCount(count);
            // productMapper.updateById(product);

            log.info("更新商品浏览数成功: productId={}, count={}", productId, count);
        } catch (Exception e) {
            log.error("更新商品浏览数异常: productId={}, count={}", productId, count, e);
            throw e;
        }
    }
}

