package com.gym.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.*;
import com.gym.mapper.*;
import com.gym.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<Member> pageList(int page, int size, String keyword) {
        Page<Member> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Member::getRealName, keyword).or().like(Member::getPhone, keyword);
        }
        wrapper.orderByDesc(Member::getCreateTime);
        Page<Member> result = page(pageParam, wrapper);
        // 从User表填充头像
        result.getRecords().forEach(this::fillAvatar);
        return result;
    }

    @Override
    public Member getByUserId(Long userId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUserId, userId);
        Member member = getOne(wrapper);
        if (member != null) {
            fillAvatar(member);
        }
        return member;
    }

    /**
     * 从User表填充头像URL
     */
    private void fillAvatar(Member member) {
        if (member.getAvatar() == null || member.getAvatar().isEmpty()) {
            User user = userMapper.selectById(member.getUserId());
            if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                member.setAvatar(user.getAvatar());
            }
        }
    }

    @Override
    @Transactional
    public boolean deductHours(Long memberId, int hours) {
        return baseMapper.deductHours(memberId, hours) > 0;
    }

    @Override
    @Transactional
    public boolean addHours(Long memberId, int hours, BigDecimal amount) {
        Member member = getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        baseMapper.addHours(memberId, hours);
        // 更新累计充值金额
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            baseMapper.addRechargeAmount(memberId, amount);
        }
        return true;
    }

    @Override
    public Map<String, Object> getMemberStats(Long memberId) {
        Map<String, Object> stats = new HashMap<>();

        Member member = getById(memberId);
        stats.put("member", member);

        // 预约统计
        LambdaQueryWrapper<Booking> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.eq(Booking::getMemberId, memberId);
        long totalBookings = bookingMapper.selectCount(bookingWrapper);
        stats.put("totalBookings", totalBookings);

        // 签到统计
        LambdaQueryWrapper<CheckIn> checkInWrapper = new LambdaQueryWrapper<>();
        checkInWrapper.eq(CheckIn::getMemberId, memberId);
        long totalCheckIns = checkInMapper.selectCount(checkInWrapper);
        stats.put("totalCheckIns", totalCheckIns);

        // 充值记录
        LambdaQueryWrapper<RechargeRecord> rechargeWrapper = new LambdaQueryWrapper<>();
        rechargeWrapper.eq(RechargeRecord::getMemberId, memberId);
        rechargeWrapper.orderByDesc(RechargeRecord::getCreateTime);
        rechargeWrapper.last("limit 10");
        List<RechargeRecord> rechargeRecords = rechargeRecordMapper.selectList(rechargeWrapper);
        stats.put("rechargeRecords", rechargeRecords);

        return stats;
    }
}
