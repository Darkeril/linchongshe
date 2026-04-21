package com.hongshu.idle.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Idle模块审核日志实体
 *
 * @author hongshu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("web_audit_log")
public class IdleAuditLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 内容ID
     */
    private String contentId;

    /**
     * 内容类型(product)
     */
    private String contentType;

    /**
     * 审核结果(PASS/MANUAL/REJECT)
     */
    private String auditResult;

    /**
     * 审核分数
     */
    private BigDecimal auditScore;

    /**
     * 审核原因
     */
    private String auditReason;

    /**
     * 审核服务商
     */
    private String auditProvider;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
