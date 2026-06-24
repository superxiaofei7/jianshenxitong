package com.gym.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.R;
import com.gym.entity.CheckIn;
import com.gym.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @PostMapping("/{bookingId}")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','MEMBER')")
    public R<CheckIn> checkIn(@PathVariable Long bookingId) {
        return R.ok(checkInService.doCheckIn(bookingId));
    }

    @GetMapping("/member/{memberId}")
    public R<Page<CheckIn>> memberCheckIns(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(checkInService.getMemberCheckIns(memberId, page, size));
    }

    @GetMapping("/trainer/{trainerId}")
    public R<Page<CheckIn>> trainerCheckIns(
            @PathVariable Long trainerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(checkInService.getTrainerCheckIns(trainerId, page, size));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<Page<CheckIn>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(checkInService.getAllCheckIns(page, size));
    }
}
