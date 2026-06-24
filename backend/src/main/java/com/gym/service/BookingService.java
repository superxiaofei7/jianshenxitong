package com.gym.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Booking;

public interface BookingService extends IService<Booking> {
    Booking createBooking(Long memberId, Long trainerId, Long courseId, Long scheduleId, String remark);
    boolean cancelBooking(Long bookingId, Long userId);
    boolean confirmBooking(Long bookingId);
    boolean completeBooking(Long bookingId);
    Page<Booking> getMemberBookings(Long memberId, int page, int size, Integer status);
    Page<Booking> getTrainerBookings(Long trainerId, int page, int size, Integer status);
    Page<Booking> getAllBookings(int page, int size, Integer status);
}
