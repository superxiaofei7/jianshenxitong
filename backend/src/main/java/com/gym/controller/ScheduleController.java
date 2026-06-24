package com.gym.controller;

import com.gym.common.R;
import com.gym.entity.Schedule;
import com.gym.entity.Trainer;
import com.gym.service.ScheduleService;
import com.gym.service.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private TrainerService trainerService;

    @GetMapping("/trainer/{trainerId}")
    public R<List<Schedule>> getTrainerSchedule(
            @PathVariable Long trainerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return R.ok(scheduleService.getTrainerSchedule(trainerId, date));
    }

    @GetMapping("/available/{trainerId}")
    public R<List<Schedule>> getAvailableSlots(
            @PathVariable Long trainerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return R.ok(scheduleService.getAvailableSlots(trainerId, startDate, endDate));
    }

    @PostMapping("/set/{trainerId}")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<?> setSchedule(
            @PathVariable Long trainerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestBody List<Schedule> schedules) {
        scheduleService.setSchedule(trainerId, date, schedules);
        return R.ok("排班设置成功");
    }

    // ======== 教练自主排班接口 ========

    /**
     * 教练获取自己的排班
     */
    @GetMapping("/mySchedule")
    @PreAuthorize("hasAnyAuthority('TRAINER')")
    public R<List<Schedule>> getMySchedule(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Trainer trainer = trainerService.getByUserId(userId);
        if (trainer == null) {
            return R.fail("未找到教练信息");
        }
        return R.ok(scheduleService.getTrainerSchedule(trainer.getId(), date));
    }

    /**
     * 教练设置自己的排班
     */
    @PostMapping("/mySchedule")
    @PreAuthorize("hasAnyAuthority('TRAINER')")
    public R<?> setMySchedule(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestBody List<Schedule> schedules,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Trainer trainer = trainerService.getByUserId(userId);
        if (trainer == null) {
            return R.fail("未找到教练信息");
        }
        scheduleService.setSchedule(trainer.getId(), date, schedules);
        return R.ok("排班设置成功");
    }

    // ======== 冲突检查 ========

    @GetMapping("/checkConflict")
    public R<Boolean> checkConflict(
            @RequestParam Long trainerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        return R.ok(scheduleService.checkConflict(trainerId, date, startTime, endTime));
    }
}
