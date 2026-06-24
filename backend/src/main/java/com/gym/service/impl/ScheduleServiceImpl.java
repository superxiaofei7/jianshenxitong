package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Schedule;
import com.gym.mapper.ScheduleMapper;
import com.gym.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Override
    public List<Schedule> getTrainerSchedule(Long trainerId, LocalDate date) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getTrainerId, trainerId)
               .eq(Schedule::getScheduleDate, date)
               .orderByAsc(Schedule::getStartTime);
        return list(wrapper);
    }

    @Override
    public List<Schedule> getAvailableSlots(Long trainerId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getTrainerId, trainerId)
               .ge(Schedule::getScheduleDate, startDate)
               .le(Schedule::getScheduleDate, endDate)
               .eq(Schedule::getStatus, 1)
               .apply("booked_count < max_bookings")
               .orderByAsc(Schedule::getScheduleDate)
               .orderByAsc(Schedule::getStartTime);
        return list(wrapper);
    }

    @Override
    @Transactional
    public boolean setSchedule(Long trainerId, LocalDate date, List<Schedule> schedules) {
        // 先删除当天已有排班
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getTrainerId, trainerId).eq(Schedule::getScheduleDate, date);
        remove(wrapper);

        // 批量保存
        for (Schedule schedule : schedules) {
            schedule.setTrainerId(trainerId);
            schedule.setScheduleDate(date);
            schedule.setBookedCount(0);
            schedule.setMaxBookings(1);
            schedule.setStatus(1);
        }
        return saveBatch(schedules);
    }

    @Override
    public boolean checkConflict(Long trainerId, LocalDate date, String startTime, String endTime) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getTrainerId, trainerId)
               .eq(Schedule::getScheduleDate, date)
               .eq(Schedule::getStatus, 1)
               .lt(Schedule::getStartTime, LocalTime.parse(endTime))
               .gt(Schedule::getEndTime, LocalTime.parse(startTime));
        return count(wrapper) > 0;
    }
}
