package com.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {

    @Update("UPDATE tb_member SET remaining_hours = remaining_hours - #{hours} WHERE id = #{memberId} AND remaining_hours >= #{hours}")
    int deductHours(@Param("memberId") Long memberId, @Param("hours") int hours);

    @Update("UPDATE tb_member SET remaining_hours = COALESCE(remaining_hours, 0) + #{hours} WHERE id = #{memberId}")
    int addHours(@Param("memberId") Long memberId, @Param("hours") int hours);

    @Update("UPDATE tb_member SET total_recharge = COALESCE(total_recharge, 0) + #{amount} WHERE id = #{memberId}")
    int addRechargeAmount(@Param("memberId") Long memberId, @Param("amount") BigDecimal amount);
}
