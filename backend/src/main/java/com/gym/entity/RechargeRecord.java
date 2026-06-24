package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_recharge_record")
public class RechargeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;
    private BigDecimal amount;
    private Integer hours;         // 充值课时数
    private String payMethod;      // 支付方式
    private Long operatorId;       // 操作员ID(前台/财务)
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
