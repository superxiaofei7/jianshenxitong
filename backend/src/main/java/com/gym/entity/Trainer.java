package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_trainer")
public class Trainer {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String realName;
    private String phone;
    private String gender;
    private Integer age;
    private String avatar;
    private String specialties;    // 擅长项目，逗号分隔
    private BigDecimal pricePerHour; // 每小时授课价格
    private String description;    // 个人简介
    private String certifications; // 证书资质
    private Integer experienceYears; // 从业年限
    private Integer status;        // 1-在职 0-离职

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
