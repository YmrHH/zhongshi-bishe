package com.gzpprod.center.module.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.notification.entity.Notification;
import com.gzpprod.center.module.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final SysUserMapper userMapper;

    public void notifyRole(UserRole role, Long projectId, String type, String title, String content) {
        userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, role.name()))
                .forEach(user -> notifyUser(user.getId(), projectId, type, title, content));
    }

    public void notifyUser(Long userId, Long projectId, String type, String title, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setProjectId(projectId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setReadFlag(0);
        notificationMapper.insert(n);
    }
}
