package com.gzpprod.center.common;

import java.util.Set;

public enum EvaluationStatus {
    PRECHECK,
    CONDITION_EVAL,
    CONDITION_RETURNED,
    RESOURCE,
    FEASIBILITY,
    FEASIBILITY_RETURNED,
    CONCLUSION,
    RECEIPTED,
    FEEDBACK,
    ARCHIVED;

    public String label() {
        return switch (this) {
            case PRECHECK -> "前置核查中";
            case CONDITION_EVAL -> "条件评估中";
            case CONDITION_RETURNED -> "条件整改待补充";
            case RESOURCE -> "资源核定中";
            case FEASIBILITY -> "可行性审查中";
            case FEASIBILITY_RETURNED -> "评估材料待补充";
            case CONCLUSION -> "结论待签收";
            case RECEIPTED -> "结论已签收";
            case FEEDBACK -> "意见已反馈";
            case ARCHIVED -> "评估段归档";
        };
    }

    public static EvaluationStatus of(String value) {
        return EvaluationStatus.valueOf(value);
    }

    public static Set<EvaluationStatus> dispatcherTodos() {
        return Set.of(PRECHECK, CONDITION_EVAL, CONDITION_RETURNED, RESOURCE, RECEIPTED, FEEDBACK);
    }

    public static Set<EvaluationStatus> auditorTodos() {
        return Set.of(FEASIBILITY, CONCLUSION);
    }

    public static Set<EvaluationStatus> enterpriseTodos() {
        return Set.of(CONDITION_RETURNED, FEASIBILITY_RETURNED, CONCLUSION, RECEIPTED);
    }
}
