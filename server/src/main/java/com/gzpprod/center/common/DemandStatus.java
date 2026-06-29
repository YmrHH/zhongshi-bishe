package com.gzpprod.center.common;

import java.util.Set;

public enum DemandStatus {
    DRAFT,
    SUBMITTED,
    ACCEPTING,
    VERIFYING,
    RETURNED,
    ACCEPTED,
    RECEIPTED,
    ARCHIVED;

    public String label() {
        return switch (this) {
            case DRAFT -> "草稿";
            case SUBMITTED -> "已提交待受理";
            case ACCEPTING -> "受理登记中";
            case VERIFYING -> "材料核验中";
            case RETURNED -> "已退回待补充";
            case ACCEPTED -> "同意受理";
            case RECEIPTED -> "已签收";
            case ARCHIVED -> "需求段归档";
        };
    }

    public static DemandStatus of(String value) {
        return DemandStatus.valueOf(value);
    }

    public static Set<DemandStatus> dispatcherTodos() {
        return Set.of(SUBMITTED, ACCEPTING, RECEIPTED);
    }

    public static Set<DemandStatus> auditorTodos() {
        return Set.of(ACCEPTING);
    }

    public static Set<DemandStatus> enterpriseTodos() {
        return Set.of(DRAFT, RETURNED, ACCEPTED);
    }
}
