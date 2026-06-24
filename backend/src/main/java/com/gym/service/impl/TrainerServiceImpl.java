package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Trainer;
import com.gym.entity.User;
import com.gym.mapper.TrainerMapper;
import com.gym.mapper.UserMapper;
import com.gym.service.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrainerServiceImpl extends ServiceImpl<TrainerMapper, Trainer> implements TrainerService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<Trainer> pageList(int page, int size, String keyword) {
        Page<Trainer> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Trainer> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Trainer::getRealName, keyword)
                   .or().like(Trainer::getSpecialties, keyword);
        }
        wrapper.orderByDesc(Trainer::getCreateTime);
        Page<Trainer> result = page(pageParam, wrapper);
        // 从User表填充头像
        result.getRecords().forEach(this::fillAvatar);
        return result;
    }

    @Override
    public Page<Trainer> availableList(int page, int size) {
        Page<Trainer> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Trainer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trainer::getStatus, 1);
        wrapper.orderByDesc(Trainer::getExperienceYears);
        Page<Trainer> result = page(pageParam, wrapper);
        // 从User表填充头像
        result.getRecords().forEach(this::fillAvatar);
        return result;
    }

    @Override
    public Trainer getByUserId(Long userId) {
        LambdaQueryWrapper<Trainer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trainer::getUserId, userId);
        Trainer trainer = getOne(wrapper);
        if (trainer != null) {
            fillAvatar(trainer);
        }
        return trainer;
    }

    /**
     * 从User表填充头像URL
     */
    private void fillAvatar(Trainer trainer) {
        if (trainer.getAvatar() == null || trainer.getAvatar().isEmpty()) {
            User user = userMapper.selectById(trainer.getUserId());
            if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                trainer.setAvatar(user.getAvatar());
            }
        }
    }
}
