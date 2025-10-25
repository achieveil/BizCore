package com.achieveil.bizcore.controller;

import com.achieveil.bizcore.common.Result;
import com.achieveil.bizcore.dto.LoginRequest;
import com.achieveil.bizcore.dto.LoginResponse;
import com.achieveil.bizcore.dto.RefreshTokenRequest;
import com.achieveil.bizcore.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                       HttpServletRequest request) {
        LoginResponse loginResponse = authService.login(loginRequest, request);
        return Result.success("登录成功", loginResponse);
    }

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("认证测试成功", "您已通过JWT认证");
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return Result.success("刷新成功", response);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return Result.success("退出成功", null);
    }
}
