package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String courseName;
    private String courseType;     // PRIVATE-私教课, GROUP-团体课
    private Long trainerId;
    private Integer maxStudents;   // 最大人数（团体课）
    private Integer durationMinutes; // 课程时长(分钟)
    private BigDecimal pricePerSession; // 每节课价格
    private String description;
    private Integer status;        // 1-上架 0-下架

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
