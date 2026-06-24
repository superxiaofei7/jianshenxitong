package com.gym.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.R;
import com.gym.entity.Booking;
import com.gym.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAuthority('MEMBER')")
    public R<Booking> create(@RequestBody Map<String, Object> params) {
        Long memberId = Long.valueOf(params.get("memberId").toString());
        Long trainerId = Long.valueOf(params.get("trainerId").toString());
        Long courseId = Long.valueOf(params.get("courseId").toString());
        Long scheduleId = Long.valueOf(params.get("scheduleId").toString());
        String remark = (String) params.getOrDefault("remark", "");
        return R.ok(bookingService.createBooking(memberId, trainerId, courseId, scheduleId, remark));
    }

    @PutMapping("/{id}/cancel")
    public R<?> cancel(@PathVariable Long id) {
        bookingService.cancelBooking(id, 1L);
        return R.ok("取消成功");
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<?> confirm(@PathVariable Long id) {
        bookingService.confirmBooking(id);
        return R.ok("确认成功");
    }

    @GetMapping("/member/{memberId}")
    public R<Page<Booking>> memberBookings(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return R.ok(bookingService.getMemberBookings(memberId, page, size, status));
    }

    @GetMapping("/trainer/{trainerId}")
    public R<Page<Booking>> trainerBookings(
            @PathVariable Long trainerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return R.ok(bookingService.getTrainerBookings(trainerId, page, size, status));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<Page<Booking>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return R.ok(bookingService.getAllBookings(page, size, status));
    }
}
