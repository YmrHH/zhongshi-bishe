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

    public static UserRole requireRole(SysUser user, UserRole... allowed) {
        UserRole role = UserRole.valueOf(user.getRole());
        for (UserRole r : allowed) {
            if (r == role) {
                return role;
            }
        }
        throw new BusinessException(403, "无权操作");
    }
}
