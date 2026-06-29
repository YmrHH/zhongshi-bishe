package com.gzpprod.center.module.demand.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DemandDetailResponse {

    private Long projectId;
    private String projectNo;
    private String title;
    private String stage;
    private String status;
    private String statusLabel;
    private String currentNode;
    private String content;
    private String pilotType;
    private Integer expectedDays;
    private String contactName;
    private String contactPhone;
    private String rejectReason;
    private String acceptOpinion;
    private String acceptResult;
    private String enterpriseName;
    private LocalDateTime submittedAt;
    private Integer materialCount;
    private List<MaterialVo> materials;
    private List<ProgressStep> steps;
    /** 需求模块五阶段进度（填报→登记→审核→签收→归档查进度） */
    private List<ProgressStep> phaseSteps;
    private List<LogItem> logs;

    @Data
    @Builder
    public static class MaterialVo {
        private Long id;
        private String fileUrl;
        private String fileName;
        private String materialType;
        private Integer version;
    }

    @Data
    @Builder
    public static class ProgressStep {
        private String node;
        private String status;
        private LocalDateTime time;
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
