package com.gym.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.*;
import com.gym.mapper.*;
import com.gym.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private TrainerMapper trainerMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ConsumptionRecordMapper consumptionRecordMapper;

    @Override
    @Transactional
    public Booking createBooking(Long memberId, Long trainerId, Long courseId, Long scheduleId, String remark) {
        // 检查会员剩余课时
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        if (member.getRemainingHours() == null || member.getRemainingHours() <= 0) {
            throw new RuntimeException("剩余课时不足，请先充值");
        }

        // 检查排班是否可用
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }
        if (schedule.getStatus() != 1) {
            throw new RuntimeException("该时段不可预约");
        }
        if (schedule.getBookedCount() >= schedule.getMaxBookings()) {
            throw new RuntimeException("该时段已约满");
        }

        // 检查教练
        Trainer trainer = trainerMapper.selectById(trainerId);
        if (trainer == null || trainer.getStatus() != 1) {
            throw new RuntimeException("教练不可用");
        }

        // 检查该时段是否已有预约（防止重复预约）
        LambdaQueryWrapper<Booking> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Booking::getMemberId, memberId)
                    .eq(Booking::getScheduleId, scheduleId)
                    .in(Booking::getStatus, 0, 1);
        if (count(existWrapper) > 0) {
            throw new RuntimeException("您已预约过该时段");
        }

        // 占用排班位
        if (scheduleMapper.incrementBookedCount(scheduleId) <= 0) {
            throw new RuntimeException("时段已被他人抢先预约");
        }

        // 获取课程信息
        Course course = courseMapper.selectById(courseId);
        BigDecimal amount = BigDecimal.ZERO;
        int hours = 1;
        if (course != null) {
            amount = course.getPricePerSession() != null ? course.getPricePerSession() : BigDecimal.ZERO;
            hours = course.getDurationMinutes() != null ? course.getDurationMinutes() / 60 : 1;
            if (hours < 1) hours = 1;
        }

        // 创建订单
        Booking booking = new Booking();
        booking.setOrderNo("GYM" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + IdUtil.fastSimpleUUID().substring(0, 6));
        booking.setMemberId(memberId);
        booking.setTrainerId(trainerId);
        booking.setCourseId(courseId);
        booking.setScheduleId(scheduleId);
        booking.setBookingDate(schedule.getScheduleDate());
        booking.setStartTime(schedule.getStartTime());
        booking.setEndTime(schedule.getEndTime());
        booking.setHours(hours);
        booking.setAmount(amount);
        booking.setStatus(1); // 已确认
        booking.setRemark(remark);
        save(booking);

        // 生成消费记录
        ConsumptionRecord record = new ConsumptionRecord();
        record.setMemberId(memberId);
        record.setBookingId(booking.getId());
        record.setTrainerId(trainerId);
        record.setCourseId(courseId);
        record.setHoursConsumed(hours);
        record.setAmount(amount);
        record.setRecordType("BOOKING");
        record.setRemark("预约消费");
        consumptionRecordMapper.insert(record);

        return booking;
    }

    @Override
    @Transactional
    public boolean cancelBooking(Long bookingId, Long userId) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        if (booking.getStatus() == 3) {
            throw new RuntimeException("订单已取消");
        }
        if (booking.getStatus() == 2) {
            throw new RuntimeException("已完成订单不可取消");
        }

        booking.setStatus(3);
        updateById(booking);

        // 释放排班位
        scheduleMapper.decrementBookedCount(booking.getScheduleId());

        return true;
    }

    @Override
    public boolean confirmBooking(Long bookingId) {
        Booking booking = getById(bookingId);
        if (booking == null || booking.getStatus() != 0) {
            throw new RuntimeException("订单状态不正确");
        }
        booking.setStatus(1);
        return updateById(booking);
    }

    @Override
    public boolean completeBooking(Long bookingId) {
        Booking booking = getById(bookingId);
        if (booking == null || booking.getStatus() != 1) {
            throw new RuntimeException("订单状态不正确");
        }
        booking.setStatus(2);
        return updateById(booking);
    }

    @Override
    public Page<Booking> getMemberBookings(Long memberId, int page, int size, Integer status) {
        Page<Booking> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getMemberId, memberId);
        if (status != null) {
            wrapper.eq(Booking::getStatus, status);
        }
        wrapper.orderByDesc(Booking::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    public Page<Booking> getTrainerBookings(Long trainerId, int page, int size, Integer status) {
        Page<Booking> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getTrainerId, trainerId);
        if (status != null) {
            wrapper.eq(Booking::getStatus, status);
        }
        wrapper.orderByDesc(Booking::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    public Page<Booking> getAllBookings(int page, int size, Integer status) {
        Page<Booking> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Booking::getStatus, status);
        }
        wrapper.orderByDesc(Booking::getCreateTime);
        return page(pageParam, wrapper);
    }
}
