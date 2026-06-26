package com.gzpprod.center.common;

import java.util.Set;

public enum ArchiveStatus {
    PENDING,
    LEDGER_INCOMPLETE,
    LEDGER_OK,
    CONFIRMED,
    COLLECTED,
    STATS_UNAVAILABLE,
    STATS_OK,
    BRIEF_GENERATED,
    BRIEF_RETURNED,
    BRIEF_PUBLISHED,
    CLOSED;

    public String label() {
        return switch (this) {
            case PENDING -> "待台账维护";
            case LEDGER_INCOMPLETE -> "台账不完整";
            case LEDGER_OK -> "待档案确认";
            case CONFIRMED -> "待资料归集";
            case COLLECTED -> "待周期统计";
            case STATS_UNAVAILABLE -> "统计不可用";
            case STATS_OK -> "待简报生成";
            case BRIEF_GENERATED -> "简报待审核";
            case BRIEF_RETURNED -> "简报审核退回";
            case BRIEF_PUBLISHED -> "待档案归档";
            case CLOSED -> "档案段结案";
        };
    }

    public static ArchiveStatus of(String value) {
        return ArchiveStatus.valueOf(value);
    }

    public static Set<ArchiveStatus> auditorTodos() {
        return Set.of(PENDING, LEDGER_INCOMPLETE, LEDGER_OK, CONFIRMED, COLLECTED,
                BRIEF_GENERATED, BRIEF_PUBLISHED);
    }

    public static Set<ArchiveStatus> dispatcherTodos() {
        return Set.of(COLLECTED, STATS_UNAVAILABLE, STATS_OK, BRIEF_RETURNED);
    }

    public static Set<ArchiveStatus> enterpriseTodos() {
        return Set.of(BRIEF_PUBLISHED);
    }
}
