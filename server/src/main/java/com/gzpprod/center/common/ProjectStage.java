package com.gzpprod.center.common;

public enum ProjectStage {
    DEMAND,
    EVALUATION,
    DISPATCH,
    FEEDBACK,
    ARCHIVE,
    CLOSED;

    public String label() {
        return switch (this) {
            case DEMAND -> "中试需求管理";
            case EVALUATION -> "中试评估管理";
            case DISPATCH -> "中试调度管理";
            case FEEDBACK -> "中试反馈管理";
            case ARCHIVE -> "中试档案管理";
            case CLOSED -> "已结案";
        };
    }
}
