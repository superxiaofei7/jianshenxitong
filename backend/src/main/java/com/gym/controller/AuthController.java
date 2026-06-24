package com.gym.controller;

import com.gym.common.R;
import com.gym.entity.User;
import com.gym.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody User loginUser) {
        Map<String, Object> result = userService.login(loginUser.getUsername(), loginUser.getPassword());
        return R.ok(result);
    }

    @PostMapping("/register")
    public R<User> register(@RequestBody User user) {
        return R.ok(userService.register(user));
    }

    @PutMapping("/password")
    public R<?> updatePassword(@RequestParam String oldPassword,
                                @RequestParam String newPassword,
                                Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        userService.updatePassword(userId, oldPassword, newPassword);
        return R.ok("密码修改成功");
    }

    @PutMapping("/avatar")
    public R<?> updateAvatar(@RequestParam String avatar,
                              Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        userService.updateAvatar(userId, avatar);
        return R.ok("头像更新成功");
    }

    @PostMapping("/forgotPassword")
    public R<?> forgotPassword(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String newPassword = params.get("newPassword");
        if (phone == null || phone.isEmpty()) {
            return R.fail("手机号不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return R.fail("新密码不能为空");
        }
        userService.forgotPassword(phone, newPassword);
        return R.ok("密码重置成功");
    }

    @PutMapping("/profile")
    public R<User> updateProfile(@RequestBody User user, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        return R.ok(userService.updateProfile(userId, user));
    }

    @GetMapping("/profile")
    public R<User> getProfile(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        User user = userService.getById(userId);
        user.setPassword(null);
        return R.ok(user);
    }
}
