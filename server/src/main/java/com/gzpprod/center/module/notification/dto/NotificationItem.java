package com.gzpprod.center.module.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationItem {

    private Long id;
    private Long projectId;
    private String type;
    private String title;
    private String content;
    private Boolean read;
    private String createdAt;
}
