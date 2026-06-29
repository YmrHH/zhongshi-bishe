package com.gzpprod.center.module.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectProgressResponse {
    private Long projectId;
    private String projectNo;
    private String title;
    private String stage;
    private String status;
    private String statusLabel;
    private String currentNode;
    private String moduleName;
    private List<ProgressStepVo> moduleSteps;
    private String content;
    private String acceptOpinion;
    private String rejectReason;
    private String submittedAt;
    private List<LogItem> logs;

    @Data
    @Builder
    public static class LogItem {
        private String fromStatus;
        private String toStatus;
        private String remark;
        private LocalDateTime time;
    }
}
