package com.gzpprod.center.module.auth.controller;

import com.gzpprod.center.common.Result;
import com.gzpprod.center.module.auth.dto.LoginRequest;
import com.gzpprod.center.module.auth.dto.LoginResponse;
import com.gzpprod.center.module.auth.dto.UserProfileResponse;
import com.gzpprod.center.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @Operation(summary = "当前用户")
    @GetMapping("/me")
    public Result<UserProfileResponse> me(Authentication authentication) {
        return Result.ok(authService.profile(authentication.getName()));
    }
}
