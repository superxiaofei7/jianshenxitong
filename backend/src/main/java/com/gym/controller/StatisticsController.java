package com.gym.controller;

import com.gym.common.R;
import com.gym.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<Map<String, Object>> dashboard() {
        return R.ok(statisticsService.getDashboardStats());
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyAuthority('FINANCE')")
    public R<Map<String, Object>> monthlyRevenue(@RequestParam int year, @RequestParam int month) {
        return R.ok(statisticsService.getMonthlyRevenue(year, month));
    }

    @GetMapping("/trainer/{trainerId}")
    public R<Map<String, Object>> trainerStats(@PathVariable Long trainerId) {
        return R.ok(statisticsService.getTrainerStats(trainerId));
    }

    @GetMapping("/memberActivity")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<Map<String, Object>> memberActivity() {
        return R.ok(statisticsService.getMemberActivity());
    }

    @GetMapping("/finance")
    @PreAuthorize("hasAnyAuthority('FINANCE')")
    public R<Map<String, Object>> financeStats() {
        return R.ok(statisticsService.getFinanceStats());
    }
}
