package com.gzpprod.center.module.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.notification.dto.NotificationItem;
import com.gzpprod.center.module.notification.entity.Notification;
import com.gzpprod.center.module.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

    public List<NotificationItem> listForUser(Long userId) {
        return notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreatedAt))
                .stream()
                .map(this::toItem)
                .toList();
    }

    public long countUnread(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadFlag, 0));
    }

    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n == null || !userId.equals(n.getUserId())) {
            throw new com.gzpprod.center.common.BusinessException("消息不存在");
        }
        n.setReadFlag(1);
        notificationMapper.updateById(n);
    }

    private NotificationItem toItem(Notification n) {
        return NotificationItem.builder()
                .id(n.getId())
                .projectId(n.getProjectId())
                .type(n.getType())
                .title(n.getTitle())
                .content(n.getContent())
                .read(n.getReadFlag() != null && n.getReadFlag() == 1)
                .createdAt(n.getCreatedAt() != null ? n.getCreatedAt().format(FMT) : "")
                .build();
    }
}
