package com.gzpprod.center.module.notification.controller;

import com.gzpprod.center.common.Result;
import com.gzpprod.center.common.SecurityUtils;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.notification.dto.NotificationItem;
import com.gzpprod.center.module.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "消息通知")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SysUserMapper userMapper;

    @Operation(summary = "消息列表")
    @GetMapping
    public Result<List<NotificationItem>> list(Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(notificationService.listForUser(user.getId()));
    }

    @Operation(summary = "未读消息数")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount(Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(Map.of("count", notificationService.countUnread(user.getId())));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        notificationService.markRead(user.getId(), id);
        return Result.ok(null);
    }
}
