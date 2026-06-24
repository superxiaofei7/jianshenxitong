package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@TableName("tb_booking")
public class Booking {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;        // 订单号
    private Long memberId;
    private Long trainerId;
    private Long courseId;
    private Long scheduleId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer hours;         // 消耗课时数
    private BigDecimal amount;     // 金额
    private Integer status;        // 0-待确认 1-已确认 2-已完成 3-已取消
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
