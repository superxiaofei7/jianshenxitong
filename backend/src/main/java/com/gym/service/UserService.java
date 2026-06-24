package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.User;
import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(String username, String password);
    User register(User user);
    User getByUsername(String username);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    void updateAvatar(Long userId, String avatar);
    void forgotPassword(String phone, String newPassword);
    User updateProfile(Long userId, User user);
}
