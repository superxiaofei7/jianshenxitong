package com.gym.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.common.JwtUtils;
import com.gym.common.RoleConstants;
import com.gym.entity.Member;
import com.gym.entity.Trainer;
import com.gym.entity.User;
import com.gym.mapper.MemberMapper;
import com.gym.mapper.TrainerMapper;
import com.gym.mapper.UserMapper;
import com.gym.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TrainerMapper trainerMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public Map<String, Object> login(String username, String password) {
        User user = getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        user.setPassword(null);
        result.put("user", user);
        return result;
    }

    @Override
    public User register(User user) {
        if (getByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (StrUtil.isBlank(user.getRole())) {
            user.setRole(RoleConstants.ROLE_MEMBER);
        }
        user.setStatus(1);
        save(user);
        // 自动创建对应的教练/会员记录
        createRoleRecord(user);
        user.setPassword(null);
        return user;
    }

    /**
     * 注册时自动创建教练表或会员表中的记录
     */
    private void createRoleRecord(User user) {
        if (RoleConstants.ROLE_TRAINER.equals(user.getRole())) {
            LambdaQueryWrapper<Trainer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Trainer::getUserId, user.getId());
            if (trainerMapper.selectCount(wrapper) == 0) {
                Trainer trainer = new Trainer();
                trainer.setUserId(user.getId());
                trainer.setRealName(user.getRealName());
                trainer.setPhone(user.getPhone());
                trainer.setStatus(1);
                trainerMapper.insert(trainer);
            }
        } else if (RoleConstants.ROLE_MEMBER.equals(user.getRole())) {
            LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Member::getUserId, user.getId());
            if (memberMapper.selectCount(wrapper) == 0) {
                Member member = new Member();
                member.setUserId(user.getId());
                member.setRealName(user.getRealName());
                member.setPhone(user.getPhone());
                memberMapper.insert(member);
            }
        }
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatar) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setAvatar(avatar);
        updateById(user);
        // 同步头像到教练/会员表
        syncAvatar(userId, avatar, user.getRole());
    }

    @Override
    public void forgotPassword(String phone, String newPassword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("未找到该手机号对应的用户");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    public User updateProfile(Long userId, User updateUser) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // Only allow updating realName, phone, avatar
        if (StrUtil.isNotBlank(updateUser.getRealName())) {
            user.setRealName(updateUser.getRealName());
        }
        if (StrUtil.isNotBlank(updateUser.getPhone())) {
            user.setPhone(updateUser.getPhone());
        }
        if (updateUser.getAvatar() != null) {
            user.setAvatar(updateUser.getAvatar());
            // 同步头像到教练/会员表，确保其他人也能看到头像
            syncAvatar(userId, updateUser.getAvatar(), user.getRole());
        }
        updateById(user);
        user.setPassword(null);
        return user;
    }

    /**
     * 将头像同步到对应的教练表或会员表
     */
    private void syncAvatar(Long userId, String avatar, String role) {
        if (RoleConstants.ROLE_TRAINER.equals(role)) {
            LambdaQueryWrapper<Trainer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Trainer::getUserId, userId);
            Trainer trainer = trainerMapper.selectOne(wrapper);
            if (trainer != null) {
                trainer.setAvatar(avatar);
                trainerMapper.updateById(trainer);
            } else {
                // 如果教练记录不存在，自动创建
                Trainer newTrainer = new Trainer();
                newTrainer.setUserId(userId);
                newTrainer.setAvatar(avatar);
                newTrainer.setStatus(1);
                trainerMapper.insert(newTrainer);
            }
        } else if (RoleConstants.ROLE_MEMBER.equals(role)) {
            LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Member::getUserId, userId);
            Member member = memberMapper.selectOne(wrapper);
            if (member != null) {
                member.setAvatar(avatar);
                memberMapper.updateById(member);
            } else {
                // 如果会员记录不存在，自动创建
                Member newMember = new Member();
                newMember.setUserId(userId);
                newMember.setAvatar(avatar);
                memberMapper.insert(newMember);
            }
        } else if (RoleConstants.ROLE_FRONT_DESK.equals(role) || RoleConstants.ROLE_FINANCE.equals(role)) {
            // 管理员/财务没有对应的业务表，无需同步
        }
    }
}
