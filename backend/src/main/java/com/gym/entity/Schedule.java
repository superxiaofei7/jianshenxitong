package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@TableName("tb_schedule")
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long trainerId;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxBookings;   // 该时段最多预约数(通常为1)
    private Integer bookedCount;   // 已预约数
    private Integer status;        // 1-可预约 0-已满 2-已取消

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
