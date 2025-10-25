package com.achieveil.bizcore.controller;

import com.achieveil.bizcore.common.Result;
import com.achieveil.bizcore.dto.LoginRequest;
import com.achieveil.bizcore.dto.RegisterRequest;
import com.achieveil.bizcore.dto.UserVO;
import com.achieveil.bizcore.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UserVO user = userService.register(request);
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginRequest request) {
        try {
            UserVO user = userService.login(request);
            return Result.success("登录成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        try {
            UserVO user = userService.getUserById(id);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    public Result<List<UserVO>> getAllUsers() {
        try {
            List<UserVO> users = userService.getAllUsers();
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(@PathVariable Long id,
                                       @RequestBody Map<String, String> request) {
        try {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            userService.updatePassword(id, oldPassword, newPassword);
            return Result.success("密码修改成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/security-question/{username}")
    public Result<Map<String, String>> getSecurityQuestion(@PathVariable String username) {
        try {
            String question = userService.getSecurityQuestion(username);
            String maskedPhone = userService.getMaskedPhone(username);
            Map<String, String> result = Map.of(
                "securityQuestion", question,
                "maskedPhone", maskedPhone
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/verify-security-answer")
    public Result<Void> verifySecurityAnswer(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String securityAnswer = request.get("securityAnswer");
            userService.verifySecurityAnswer(username, securityAnswer);
            return Result.success("密保答案验证成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/verify-phone")
    public Result<Void> verifyPhone(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String phoneMiddle = request.get("phoneMiddle");
            userService.verifyPhone(username, phoneMiddle);
            return Result.success("手机号验证成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String securityAnswer = request.get("securityAnswer");
            String newPassword = request.get("newPassword");
            String phoneMiddle = request.get("phoneMiddle");
            userService.resetPassword(username, securityAnswer, newPassword, phoneMiddle);
            return Result.success("密码重置成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员重置用户密码
     */
    @PostMapping("/admin-reset-password")
    public Result<Void> adminResetPassword(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String newPassword = request.get("newPassword").toString();
            Long adminId = Long.valueOf(request.get("adminId").toString());
            userService.adminResetPassword(userId, newPassword, adminId);
            return Result.success("密码重置成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
