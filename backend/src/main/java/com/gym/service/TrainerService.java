package com.gym.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Trainer;

public interface TrainerService extends IService<Trainer> {
    Page<Trainer> pageList(int page, int size, String keyword);
    Page<Trainer> availableList(int page, int size);
    Trainer getByUserId(Long userId);
}
