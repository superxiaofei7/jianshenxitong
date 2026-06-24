package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Schedule;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleService extends IService<Schedule> {
    List<Schedule> getTrainerSchedule(Long trainerId, LocalDate date);
    List<Schedule> getAvailableSlots(Long trainerId, LocalDate startDate, LocalDate endDate);
    boolean setSchedule(Long trainerId, LocalDate date, List<Schedule> schedules);
    boolean checkConflict(Long trainerId, LocalDate date, String startTime, String endTime);
}
