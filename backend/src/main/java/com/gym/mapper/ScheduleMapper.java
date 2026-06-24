package com.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    @Update("UPDATE tb_schedule SET booked_count = booked_count + 1 WHERE id = #{id} AND booked_count < max_bookings")
    int incrementBookedCount(@Param("id") Long id);

    @Update("UPDATE tb_schedule SET booked_count = booked_count - 1 WHERE id = #{id} AND booked_count > 0")
    int decrementBookedCount(@Param("id") Long id);
}
