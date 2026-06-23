package com.gzpprod.center.common;

import com.gzpprod.center.module.demand.entity.TrialProject;

public final class ProjectStatusHelper {

    private ProjectStatusHelper() {
    }

    public static String label(TrialProject project) {
        ProjectStage stage = ProjectStage.valueOf(project.getStage());
        return switch (stage) {
            case DEMAND -> DemandStatus.of(project.getStatus()).label();
            case EVALUATION -> EvaluationStatus.of(project.getStatus()).label();
            default -> project.getCurrentNode() != null ? project.getCurrentNode() : stage.label();
        };
    }

    /** 需求模块详情页：已进入后续阶段时，需求步骤条视为全部完成 */
    public static DemandStatus demandStepStatus(TrialProject project) {
        if (ProjectStage.DEMAND.name().equals(project.getStage())) {
            return DemandStatus.of(project.getStatus());
        }
        return DemandStatus.ARCHIVED;
    }

    public static String demandOperationHint(TrialProject project) {
        if (ProjectStage.DEMAND.name().equals(project.getStage())) {
            return DemandStatus.of(project.getStatus()).label();
        }
        return ProjectStage.valueOf(project.getStage()).label()
                + "（" + (project.getCurrentNode() != null ? project.getCurrentNode() : project.getStatus()) + "）";
    }
}
