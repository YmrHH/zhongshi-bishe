package com.gzpprod.center.module.evaluation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EvaluationDetailResponse {

    private Long projectId;
    private String projectNo;
    private String title;
    private String stage;
    private String status;
    private String statusLabel;
    private String currentNode;
    private String enterpriseName;
    private String precheckRemark;
    private String conditionResult;
    private String conditionRemark;
    private String rectifyNotice;
    private String resourceRemark;
    private String resourceRequirement;
    private String feasibilityResult;
    private String feasibilityRemark;
    private String conclusion;
    private String conclusionOpinion;
    private String feedbackContent;
    private List<MaterialVo> materials;
    private List<ProgressStep> steps;
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
