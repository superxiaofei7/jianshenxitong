package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_check_in")
public class CheckIn {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bookingId;
    private Long memberId;
    private Long trainerId;
    private Long courseId;
    private LocalDateTime checkInTime;
    private Integer hoursConsumed; // 本次消耗课时
    private Integer status;        // 1-已签到 2-已取消

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
