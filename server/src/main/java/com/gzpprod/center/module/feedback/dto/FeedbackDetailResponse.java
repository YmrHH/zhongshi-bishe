package com.gzpprod.center.module.feedback.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FeedbackDetailResponse {

    private Long projectId;
    private String projectNo;
    private String title;
    private String stage;
    private String status;
    private String statusLabel;
    private String currentNode;
    private String enterpriseName;
    private String technicianName;
    private Long reportId;
    private String reportContent;
    private String fileUrl;
    private String fileName;
    private String validateRemark;
    private String modifyRemark;
    private String reviewOpinion;
    private String reviewFeedback;
    private List<ReviewVo> reviews;
    private List<ProgressStep> steps;
    private List<LogItem> logs;

    @Data
    @Builder
    public static class ReviewVo {
        private String type;
        private String result;
        private String opinion;
        private String reviewerName;
        private LocalDateTime time;
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
