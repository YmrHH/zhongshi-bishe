package com.gzpprod.center.module.dispatch.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DispatchDetailResponse {

    private Long projectId;
    private String projectNo;
    private String title;
    private String stage;
    private String status;
    private String statusLabel;
    private String currentNode;
    private String enterpriseName;
    private Long taskId;
    private Long resourceId;
    private String resourceName;
    private Long technicianId;
    private String technicianName;
    private Integer progressPct;
    private String taskRemark;
    private EvaluationSummaryVo evaluationSummary;
    private List<ResourceVo> resources;
    private List<TechnicianVo> technicians;
    private List<ProgressVo> progressRecords;
    private List<StepVo> steps;
    private List<LogVo> logs;

    @Data
    @Builder
    public static class EvaluationSummaryVo {
        private String conclusion;
        private String conclusionOpinion;
        private String resourceRequirement;
        private String resourceRemark;
        private String conditionResult;
        private String feasibilityResult;
        private String feedbackContent;
    }

    @Data
    @Builder
    public static class ResourceVo {
        private Long id;
        private String name;
        private String type;
        private String capacity;
        private String status;
    }

    @Data
    @Builder
    public static class TechnicianVo {
        private Long id;
        private String realName;
        private String orgName;
    }

    @Data
    @Builder
    public static class ProgressVo {
        private Integer progressPct;
        private String content;
        private LocalDateTime reportTime;
    }

    @Data
    @Builder
    public static class StepVo {
        private String node;
        private String status;
    }

    @Data
    @Builder
    public static class LogVo {
        private String fromStatus;
        private String toStatus;
        private String remark;
        private LocalDateTime time;
    }
}
