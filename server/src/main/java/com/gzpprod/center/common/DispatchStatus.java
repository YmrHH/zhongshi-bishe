package com.gzpprod.center.common;

import java.util.Set;

public enum DispatchStatus {
    MATCH,
    MATCH_FAILED,
    ASSIGNED,
    NOTICED,
    PENDING_RECEIVE,
    RECEIVED,
    CONFIRMED,
    EXECUTING,
    PROGRESS_ACKED,
    EXEC_DONE,
    ARCHIVED;

    public String label() {
        return switch (this) {
            case MATCH -> "资源匹配中";
            case MATCH_FAILED -> "匹配失败待重试";
            case ASSIGNED -> "任务已派发";
            case NOTICED -> "派单已通知";
            case PENDING_RECEIVE -> "待技术人员接收";
            case RECEIVED -> "任务已接收";
            case CONFIRMED -> "任务已签收";
            case EXECUTING -> "执行中";
            case PROGRESS_ACKED -> "进度已知悉";
            case EXEC_DONE -> "执行完成待确认";
            case ARCHIVED -> "调度段归档";
        };
    }

    public static DispatchStatus of(String value) {
        return DispatchStatus.valueOf(value);
    }

    public static Set<DispatchStatus> dispatcherTodos() {
        return Set.of(MATCH, MATCH_FAILED, ASSIGNED, PENDING_RECEIVE, RECEIVED,
                CONFIRMED, EXECUTING, EXEC_DONE);
    }

    public static Set<DispatchStatus> technicianTodos() {
        return Set.of(PENDING_RECEIVE, RECEIVED, CONFIRMED, EXECUTING);
    }

    public static Set<DispatchStatus> enterpriseTodos() {
        return Set.of(EXECUTING, PROGRESS_ACKED, EXEC_DONE, CONFIRMED);
    }
}
