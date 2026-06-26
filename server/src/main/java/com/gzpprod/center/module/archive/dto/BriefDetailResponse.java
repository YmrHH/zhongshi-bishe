package com.gzpprod.center.module.archive.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BriefDetailResponse {

    private Long id;
    private Long projectId;
    private String projectNo;
    private String title;
    private String briefTitle;
    private String content;
    private String auditStatus;
    private String statsJson;
    private String createdAt;
}
