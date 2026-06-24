package com.gzpprod.center.common;

import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.core.Authentication;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static SysUser requireUser(Authentication authentication, SysUserMapper userMapper) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, authentication.getName()));
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return user;
    }

    public static UserRole currentRole(SysUser user) {
        if (user == null || user.getRole() == null || user.getRole().isBlank()) {
            throw new BusinessException(401, "用户角色无效，请重新登录");
        }
        return UserRole.valueOf(user.getRole().trim());
    }

    public static UserRole requireRole(SysUser user, UserRole... allowed) {
        UserRole role = currentRole(user);
        for (UserRole r : allowed) {
            if (r == role) {
                return role;
            }
        }
        String required = java.util.Arrays.stream(allowed)
                .map(UserRole::label)
                .reduce((a, b) -> a + "、" + b)
                .orElse("");
        throw new BusinessException(403, "无权操作（需要" + required + "，当前为" + role.label() + "）");
    }
}
