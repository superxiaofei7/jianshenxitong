package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_consumption_record")
public class ConsumptionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;
    private Long bookingId;
    private Long trainerId;
    private Long courseId;
    private Integer hoursConsumed;
    private BigDecimal amount;
    private String recordType;     // BOOKING-预约消费, SIGN_IN-签到扣减
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
