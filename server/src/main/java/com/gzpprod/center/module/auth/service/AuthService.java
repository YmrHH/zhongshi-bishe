package com.gzpprod.center.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzpprod.center.common.BusinessException;
import com.gzpprod.center.common.JwtTokenProvider;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.auth.dto.LoginRequest;
import com.gzpprod.center.module.auth.dto.LoginResponse;
import com.gzpprod.center.module.auth.dto.ProfileUpdateRequest;
import com.gzpprod.center.module.auth.dto.UserProfileResponse;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || user.getEnabled() == null || user.getEnabled() != 1) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        UserRole role = UserRole.valueOf(user.getRole());
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole());
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .roleLabel(role.label())
                .realName(user.getRealName())
                .orgName(user.getOrgName())
                .homePath(role.homePath())
                .build();
    }

    public UserProfileResponse profile(String username) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        UserRole role = UserRole.valueOf(user.getRole());
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .roleLabel(role.label())
                .realName(user.getRealName())
                .orgName(user.getOrgName())
                .phone(user.getPhone())
                .homePath(role.homePath())
                .build();
    }

    public UserProfileResponse updateProfile(String username, ProfileUpdateRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getOldPassword() == null
                    || !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new BusinessException("原密码不正确");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        userMapper.updateById(user);
        return profile(username);
    }
}
