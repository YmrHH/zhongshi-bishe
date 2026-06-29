package com.gzpprod.center.module.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzpprod.center.common.BusinessException;
import com.gzpprod.center.common.ProjectStage;
import com.gzpprod.center.common.ProjectStatusHelper;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.archive.service.ArchiveService;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.common.dto.ProgressStepVo;
import com.gzpprod.center.module.common.dto.ProjectProgressResponse;
import com.gzpprod.center.module.demand.entity.Demand;
import com.gzpprod.center.module.demand.entity.TrialProject;
import com.gzpprod.center.module.demand.entity.WorkflowLog;
import com.gzpprod.center.module.demand.mapper.DemandMapper;
import com.gzpprod.center.module.demand.mapper.TrialProjectMapper;
import com.gzpprod.center.module.demand.mapper.WorkflowLogMapper;
import com.gzpprod.center.module.demand.service.DemandService;
import com.gzpprod.center.module.dispatch.service.DispatchService;
import com.gzpprod.center.module.evaluation.service.EvaluationService;
import com.gzpprod.center.module.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectProgressService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TrialProjectMapper projectMapper;
    private final DemandMapper demandMapper;
    private final WorkflowLogMapper workflowLogMapper;
    private final DemandService demandService;
    private final EvaluationService evaluationService;
    private final DispatchService dispatchService;
    private final FeedbackService feedbackService;
    private final ArchiveService archiveService;

    public ProjectProgressResponse getProgress(SysUser user, Long projectId) {
        TrialProject project = requireProject(projectId);
        assertReadable(user, project);

        ProjectStage stage = ProjectStage.valueOf(project.getStage());
        String moduleName = moduleName(stage);
        List<ProgressStepVo> moduleSteps = switch (stage) {
            case DEMAND -> mapDemandSteps(demandService.exportProgressSteps(project));
            case EVALUATION -> mapEvalSteps(evaluationService.exportProgressSteps(project));
            case DISPATCH -> mapDispatchSteps(dispatchService.exportProgressSteps(project));
            case FEEDBACK -> mapFeedbackSteps(feedbackService.exportProgressSteps(project));
            case ARCHIVE -> mapArchiveSteps(archiveService.exportProgressSteps(project));
            case CLOSED -> List.of(ProgressStepVo.builder().node("项目已结案").status("done").build());
        };

        Demand demand = demandMapper.selectOne(new LambdaQueryWrapper<Demand>()
                .eq(Demand::getProjectId, projectId));
        List<WorkflowLog> logs = workflowLogMapper.selectList(new LambdaQueryWrapper<WorkflowLog>()
                .eq(WorkflowLog::getProjectId, projectId)
                .orderByAsc(WorkflowLog::getCreatedAt));

        return ProjectProgressResponse.builder()
                .projectId(project.getId())
                .projectNo(project.getProjectNo())
                .title(project.getTitle())
                .stage(project.getStage())
                .status(project.getStatus())
                .statusLabel(ProjectStatusHelper.label(project))
                .currentNode(project.getCurrentNode())
                .moduleName(moduleName)
                .moduleSteps(moduleSteps)
                .content(demand != null ? demand.getContent() : null)
                .acceptOpinion(demand != null ? demand.getAcceptOpinion() : null)
                .rejectReason(demand != null ? demand.getRejectReason() : null)
                .submittedAt(findSubmittedTime(logs))
                .logs(logs.stream().map(l -> ProjectProgressResponse.LogItem.builder()
                        .fromStatus(l.getFromStatus())
                        .toStatus(l.getToStatus())
                        .remark(l.getRemark())
                        .time(l.getCreatedAt())
                        .build()).toList())
                .build();
    }

    private static String moduleName(ProjectStage stage) {
        return switch (stage) {
            case DEMAND -> "中试需求管理";
            case EVALUATION -> "中试评估管理";
            case DISPATCH -> "中试调度管理";
            case FEEDBACK -> "中试反馈管理";
            case ARCHIVE, CLOSED -> "中试档案管理";
        };
    }

    private List<ProgressStepVo> mapDemandSteps(
            List<com.gzpprod.center.module.demand.dto.DemandDetailResponse.ProgressStep> steps) {
        return steps.stream()
                .map(s -> ProgressStepVo.builder().node(s.getNode()).status(s.getStatus()).build())
                .toList();
    }

    private List<ProgressStepVo> mapEvalSteps(
            List<com.gzpprod.center.module.evaluation.dto.EvaluationDetailResponse.ProgressStep> steps) {
        return steps.stream()
                .map(s -> ProgressStepVo.builder().node(s.getNode()).status(s.getStatus()).build())
                .toList();
    }

    private List<ProgressStepVo> mapDispatchSteps(
            List<com.gzpprod.center.module.dispatch.dto.DispatchDetailResponse.StepVo> steps) {
        return steps.stream()
                .map(s -> ProgressStepVo.builder().node(s.getNode()).status(s.getStatus()).build())
                .toList();
    }

    private List<ProgressStepVo> mapFeedbackSteps(
            List<com.gzpprod.center.module.feedback.dto.FeedbackDetailResponse.ProgressStep> steps) {
        return steps.stream()
                .map(s -> ProgressStepVo.builder().node(s.getNode()).status(s.getStatus()).build())
                .toList();
    }

    private List<ProgressStepVo> mapArchiveSteps(
            List<com.gzpprod.center.module.archive.dto.ArchiveDetailResponse.ProgressStep> steps) {
        return steps.stream()
                .map(s -> ProgressStepVo.builder().node(s.getNode()).status(s.getStatus()).build())
                .toList();
    }

    private String findSubmittedTime(List<WorkflowLog> logs) {
        return logs.stream()
                .filter(l -> "SUBMITTED".equals(l.getToStatus()))
                .map(WorkflowLog::getCreatedAt)
                .findFirst()
                .map(t -> t.format(FMT))
                .orElse(null);
    }

    private TrialProject requireProject(Long projectId) {
        TrialProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    private void assertReadable(SysUser user, TrialProject project) {
        UserRole role = UserRole.valueOf(user.getRole());
        if (role == UserRole.ENTERPRISE && !user.getId().equals(project.getEnterpriseId())) {
            throw new BusinessException(403, "无权查看该项目");
        }
    }
}
