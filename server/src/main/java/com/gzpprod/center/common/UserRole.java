package com.gzpprod.center.common;

public enum UserRole {
    DISPATCHER,
    AUDITOR,
    ENTERPRISE,
    TECHNICIAN;

    public String homePath() {
        return switch (this) {
            case DISPATCHER, AUDITOR -> "/center/home";
            case ENTERPRISE -> "/enterprise/home";
            case TECHNICIAN -> "/technician/home";
        };
    }

    public String label() {
        return switch (this) {
            case DISPATCHER -> "中试调度员";
            case AUDITOR -> "中试审核员";
            case ENTERPRISE -> "企业项目负责人";
            case TECHNICIAN -> "中试技术人员";
        };
    }
}
