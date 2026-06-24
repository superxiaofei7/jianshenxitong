package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.*;
import com.gym.mapper.*;
import com.gym.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckInService {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private ConsumptionRecordMapper consumptionRecordMapper;

    @Override
    @Transactional
    public CheckIn doCheckIn(Long bookingId) {
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        if (booking.getStatus() == 2) {
            throw new RuntimeException("该订单已完成签到");
        }
        if (booking.getStatus() == 3) {
            throw new RuntimeException("已取消的订单无法签到");
        }

        // 检查是否已签到
        LambdaQueryWrapper<CheckIn> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(CheckIn::getBookingId, bookingId);
        if (count(existWrapper) > 0) {
            throw new RuntimeException("已签到");
        }

        // 扣减会员课时
        int hours = booking.getHours() != null ? booking.getHours() : 1;
        if (memberMapper.deductHours(booking.getMemberId(), hours) <= 0) {
            throw new RuntimeException("课时不足，扣减失败");
        }

        // 创建签到记录
        CheckIn checkIn = new CheckIn();
        checkIn.setBookingId(bookingId);
        checkIn.setMemberId(booking.getMemberId());
        checkIn.setTrainerId(booking.getTrainerId());
        checkIn.setCourseId(booking.getCourseId());
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setHoursConsumed(hours);
        checkIn.setStatus(1);
        save(checkIn);

        // 更新订单状态
        booking.setStatus(2);
        bookingMapper.updateById(booking);

        // 生成消费记录
        ConsumptionRecord record = new ConsumptionRecord();
        record.setMemberId(booking.getMemberId());
        record.setBookingId(bookingId);
        record.setTrainerId(booking.getTrainerId());
        record.setCourseId(booking.getCourseId());
        record.setHoursConsumed(hours);
        record.setAmount(booking.getAmount());
        record.setRecordType("SIGN_IN");
        record.setRemark("签到扣减课时");
        consumptionRecordMapper.insert(record);

        return checkIn;
    }

    @Override
    public Page<CheckIn> getMemberCheckIns(Long memberId, int page, int size) {
        Page<CheckIn> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getMemberId, memberId);
        wrapper.orderByDesc(CheckIn::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    public Page<CheckIn> getTrainerCheckIns(Long trainerId, int page, int size) {
        Page<CheckIn> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getTrainerId, trainerId);
        wrapper.orderByDesc(CheckIn::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    public Page<CheckIn> getAllCheckIns(int page, int size) {
        Page<CheckIn> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CheckIn::getCreateTime);
        return page(pageParam, wrapper);
    }
}
