package com.gym.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Member;

import java.math.BigDecimal;
import java.util.Map;

public interface MemberService extends IService<Member> {
    Page<Member> pageList(int page, int size, String keyword);
    Member getByUserId(Long userId);
    boolean deductHours(Long memberId, int hours);
    boolean addHours(Long memberId, int hours, BigDecimal amount);
    Map<String, Object> getMemberStats(Long memberId);
}
