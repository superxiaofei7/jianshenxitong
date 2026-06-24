package com.gym.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.entity.*;
import com.gym.mapper.*;
import com.gym.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private TrainerMapper trainerMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private ConsumptionRecordMapper consumptionRecordMapper;

    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 会员总数
        stats.put("totalMembers", memberMapper.selectCount(null));

        // 活跃教练数
        LambdaQueryWrapper<Trainer> trainerWrapper = new LambdaQueryWrapper<>();
        trainerWrapper.eq(Trainer::getStatus, 1);
        stats.put("activeTrainers", trainerMapper.selectCount(trainerWrapper));

        // 今日预约数
        LambdaQueryWrapper<Booking> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.eq(Booking::getBookingDate, LocalDate.now());
        stats.put("todayBookings", bookingMapper.selectCount(bookingWrapper));

        // 本月营收
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        LambdaQueryWrapper<ConsumptionRecord> revenueWrapper = new LambdaQueryWrapper<>();
        revenueWrapper.ge(ConsumptionRecord::getCreateTime, monthStart);
        List<ConsumptionRecord> records = consumptionRecordMapper.selectList(revenueWrapper);
        BigDecimal monthlyRevenue = records.stream()
                .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("monthlyRevenue", monthlyRevenue);

        // 今日签到数
        LambdaQueryWrapper<CheckIn> checkInWrapper = new LambdaQueryWrapper<>();
        checkInWrapper.ge(CheckIn::getCheckInTime, LocalDate.now().atStartOfDay());
        stats.put("todayCheckIns", checkInMapper.selectCount(checkInWrapper));

        return stats;
    }

    @Override
    public Map<String, Object> getMonthlyRevenue(int year, int month) {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime end = YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59);

        // 月度营收
        LambdaQueryWrapper<ConsumptionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(ConsumptionRecord::getCreateTime, start, end);
        List<ConsumptionRecord> records = consumptionRecordMapper.selectList(wrapper);
        BigDecimal total = records.stream()
                .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalRevenue", total);
        result.put("recordCount", records.size());

        // 月度预约数
        LambdaQueryWrapper<Booking> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.between(Booking::getCreateTime, start, end);
        result.put("totalBookings", bookingMapper.selectCount(bookingWrapper));

        // 月度充值
        LambdaQueryWrapper<RechargeRecord> rechargeWrapper = new LambdaQueryWrapper<>();
        rechargeWrapper.between(RechargeRecord::getCreateTime, start, end);
        List<RechargeRecord> rechargeRecords = rechargeRecordMapper.selectList(rechargeWrapper);
        BigDecimal totalRecharge = rechargeRecords.stream()
                .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalRecharge", totalRecharge);

        return result;
    }

    @Override
    public Map<String, Object> getTrainerStats(Long trainerId) {
        Map<String, Object> stats = new HashMap<>();

        // 总授课数
        LambdaQueryWrapper<CheckIn> checkInWrapper = new LambdaQueryWrapper<>();
        checkInWrapper.eq(CheckIn::getTrainerId, trainerId);
        long totalClasses = checkInMapper.selectCount(checkInWrapper);
        stats.put("totalClasses", totalClasses);

        // 本月授课数
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        checkInWrapper.ge(CheckIn::getCheckInTime, monthStart);
        long monthClasses = checkInMapper.selectCount(checkInWrapper);
        stats.put("monthClasses", monthClasses);

        // 预约数
        LambdaQueryWrapper<Booking> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.eq(Booking::getTrainerId, trainerId);
        stats.put("totalBookings", bookingMapper.selectCount(bookingWrapper));

        return stats;
    }

    @Override
    public Map<String, Object> getFinanceStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总营收
        List<ConsumptionRecord> allConsumptions = consumptionRecordMapper.selectList(null);
        BigDecimal totalRevenue = allConsumptions.stream()
                .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);

        // 账户总余额
        List<Member> allMembers = memberMapper.selectList(null);
        BigDecimal totalBalance = allMembers.stream()
                .map(m -> m.getTotalRecharge() != null ? m.getTotalRecharge() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalBalance", totalBalance);

        // 会员数量
        stats.put("memberCount", allMembers.size());

        // 教练数量
        LambdaQueryWrapper<Trainer> trainerWrapper = new LambdaQueryWrapper<>();
        trainerWrapper.eq(Trainer::getStatus, 1);
        stats.put("trainerCount", trainerMapper.selectCount(trainerWrapper));

        return stats;
    }

    @Override
    public Map<String, Object> getMemberActivity() {
        Map<String, Object> result = new HashMap<>();

        // 活跃会员（本月有签到的会员）
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        LambdaQueryWrapper<CheckIn> checkInWrapper = new LambdaQueryWrapper<>();
        checkInWrapper.ge(CheckIn::getCheckInTime, monthStart);
        checkInWrapper.select(CheckIn::getMemberId);
        checkInWrapper.groupBy(CheckIn::getMemberId);
        List<CheckIn> activeMembers = checkInMapper.selectList(checkInWrapper);
        result.put("activeMemberCount", activeMembers.size());

        // 各教练本月授课排行
        LambdaQueryWrapper<CheckIn> monthCheckInWrapper = new LambdaQueryWrapper<>();
        monthCheckInWrapper.ge(CheckIn::getCheckInTime, monthStart)
                           .select(CheckIn::getTrainerId)
                           .groupBy(CheckIn::getTrainerId);
        result.put("trainerRanking", checkInMapper.selectMaps(monthCheckInWrapper));

        return result;
    }
}
