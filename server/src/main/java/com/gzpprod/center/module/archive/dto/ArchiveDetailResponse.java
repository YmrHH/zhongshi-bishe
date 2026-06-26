package com.gzpprod.center.module.archive.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ArchiveDetailResponse {

    private Long projectId;
    private String projectNo;
    private String title;
    private String stage;
    private String status;
    private String statusLabel;
    private String currentNode;
    private String enterpriseName;
    private String ledgerJson;
    private String collectRemark;
    private Long briefId;
    private String briefTitle;
    private String briefContent;
    private String briefAuditStatus;
    private String briefAuditRemark;
    private List<ProgressStep> steps;
    private List<LogItem> logs;

    @Data
    @Builder
    public static class ProgressStep {
        private String node;
        private String status;
    }

    @Data
    @Builder
    public static class LogItem {
        private String fromStatus;
        private String toStatus;
        private String remark;
        private LocalDateTime time;
    }
}
