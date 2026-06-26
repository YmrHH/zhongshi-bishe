package com.gzpprod.center.common;

import java.util.Set;

public enum FeedbackStatus {
    PENDING,
    SUBMITTED,
    VALIDATED,
    AUDIT_REJECTED,
    AUDIT_PASSED,
    REVIEWED,
    REPORT_ARCHIVED,
    REVIEW_NOTICED,
    REVIEW_FEEDBACK,
    FEEDBACK_AUDIT_REJECTED,
    FEEDBACK_AUDIT_PASSED,
    CASE_ARCHIVED;

    public String label() {
        return switch (this) {
            case PENDING -> "待提交试验结果";
            case SUBMITTED -> "待数据校验";
            case VALIDATED -> "待报告审核";
            case AUDIT_REJECTED -> "报告审核退回";
            case AUDIT_PASSED -> "待复核确认";
            case REVIEWED -> "待报告归档";
            case REPORT_ARCHIVED -> "待复核结果通知";
            case REVIEW_NOTICED -> "待企业复核反馈";
            case REVIEW_FEEDBACK -> "待反馈结果审核";
            case FEEDBACK_AUDIT_REJECTED -> "反馈审核退回";
            case FEEDBACK_AUDIT_PASSED -> "待结案归档";
            case CASE_ARCHIVED -> "反馈段结案归档";
        };
    }

    public static FeedbackStatus of(String value) {
        return FeedbackStatus.valueOf(value);
    }

    public static Set<FeedbackStatus> technicianTodos() {
        return Set.of(PENDING, SUBMITTED, AUDIT_REJECTED);
    }

    public static Set<FeedbackStatus> auditorTodos() {
        return Set.of(VALIDATED, AUDIT_PASSED, REVIEWED, REPORT_ARCHIVED, REVIEW_FEEDBACK, FEEDBACK_AUDIT_PASSED);
    }

    public static Set<FeedbackStatus> enterpriseTodos() {
        return Set.of(REVIEW_NOTICED, FEEDBACK_AUDIT_REJECTED);
    }
}
