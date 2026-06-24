package com.gym.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.CheckIn;

public interface CheckInService extends IService<CheckIn> {
    CheckIn doCheckIn(Long bookingId);
    Page<CheckIn> getMemberCheckIns(Long memberId, int page, int size);
    Page<CheckIn> getTrainerCheckIns(Long trainerId, int page, int size);
    Page<CheckIn> getAllCheckIns(int page, int size);
}
