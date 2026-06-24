package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_member")
public class Member {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String realName;
    private String phone;
    private String gender;
    private LocalDate birthday;
    private String memberLevel;   // 普通会员, 银卡会员, 金卡会员, 钻石会员
    private Integer remainingHours; // 剩余课时
    private LocalDate memberExpireDate; // 会员到期日
    private BigDecimal totalRecharge; // 累计充值
    private String avatar; // 头像URL，与tb_user.avatar同步
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
