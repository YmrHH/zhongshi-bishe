package com.gzpprod.center.module.feedback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzpprod.center.common.*;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.demand.dto.DemandTodoItem;
import com.gzpprod.center.module.demand.entity.TrialProject;
import com.gzpprod.center.module.demand.entity.WorkflowLog;
import com.gzpprod.center.module.demand.mapper.TrialProjectMapper;
import com.gzpprod.center.module.demand.mapper.WorkflowLogMapper;
import com.gzpprod.center.module.dispatch.entity.DispatchTask;
import com.gzpprod.center.module.dispatch.mapper.DispatchTaskMapper;
import com.gzpprod.center.module.feedback.dto.FeedbackActionRequest;
import com.gzpprod.center.module.feedback.dto.FeedbackDetailResponse;
import com.gzpprod.center.module.feedback.entity.FeedbackReport;
import com.gzpprod.center.module.feedback.entity.ReviewRecord;
import com.gzpprod.center.module.feedback.mapper.FeedbackReportMapper;
import com.gzpprod.center.module.feedback.mapper.ReviewRecordMapper;
import com.gzpprod.center.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TrialProjectMapper projectMapper;
    private final WorkflowLogMapper workflowLogMapper;
    private final FeedbackReportMapper reportMapper;
    private final ReviewRecordMapper reviewMapper;
    private final DispatchTaskMapper taskMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;

    @Transactional
    public FeedbackDetailResponse submit(SysUser user, Long projectId, FeedbackActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.TECHNICIAN);
        TrialProject project = requireFeedbackProject(projectId);
        requireStatus(project, FeedbackStatus.PENDING, FeedbackStatus.AUDIT_REJECTED);
        assertTechnician(user, projectId);
        FeedbackReport report = requireReport(projectId);
        report.setContent(text(request.getContent(), report.getContent()));
        report.setFileUrl(request.getFileUrl());
        report.setFileName(request.getFileName());
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(report);
        transition(project, FeedbackStatus.SUBMITTED, "试验数据校验", user.getId(),
                text(request.getRemark(), "试验结果已提交"));
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public FeedbackDetailResponse validate(SysUser user, Long projectId, FeedbackActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.TECHNICIAN);
        TrialProject project = requireFeedbackProject(projectId);
        requireStatus(project, FeedbackStatus.SUBMITTED);
        assertTechnician(user, projectId);
        FeedbackReport report = requireReport(projectId);
        if (Boolean.TRUE.equals(request.getPassed())) {
            report.setValidateRemark(text(request.getRemark(), "数据校验通过"));
            report.setUpdatedAt(LocalDateTime.now());
            reportMapper.updateById(report);
            transition(project, FeedbackStatus.VALIDATED, "中试报告审核", user.getId(), report.getValidateRemark());
            notifyAuditors(project);
        } else {
            report.setValidateRemark(text(request.getRemark(), "数据校验未通过，请修改后重新提交"));
            report.setUpdatedAt(LocalDateTime.now());
            reportMapper.updateById(report);
            transition(project, FeedbackStatus.PENDING, "试验结果提交", user.getId(), report.getValidateRemark());
        }
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public FeedbackDetailResponse audit(SysUser user, Long projectId, FeedbackActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireFeedbackProject(projectId);
        requireStatus(project, FeedbackStatus.VALIDATED);
        saveReview(projectId, "REPORT_AUDIT", request, user.getId());
        if (Boolean.TRUE.equals(request.getPassed())) {
            transition(project, FeedbackStatus.AUDIT_PASSED, "结果复核确认", user.getId(),
                    text(request.getRemark(), "报告审核通过"));
        } else {
            transition(project, FeedbackStatus.AUDIT_REJECTED, "试验结果修改", user.getId(),
                    text(request.getRemark(), "报告审核不通过，请修改"));
            notifyTechnician(project);
        }
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public FeedbackDetailResponse modify(SysUser user, Long projectId, FeedbackActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.TECHNICIAN);
        TrialProject project = requireFeedbackProject(projectId);
        requireStatus(project, FeedbackStatus.AUDIT_REJECTED);
        assertTechnician(user, projectId);
        FeedbackReport report = requireReport(projectId);
        report.setContent(text(request.getContent(), report.getContent()));
        report.setFileUrl(request.getFileUrl());
        report.setFileName(request.getFileName());
        report.setModifyRemark(text(request.getRemark(), "已修改试验结果"));
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(report);
        transition(project, FeedbackStatus.SUBMITTED, "试验数据校验", user.getId(), report.getModifyRemark());
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public FeedbackDetailResponse review(SysUser user, Long projectId, FeedbackActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireFeedbackProject(projectId);
        requireStatus(project, FeedbackStatus.AUDIT_PASSED);
        saveReview(projectId, "REVIEW", request, user.getId());
        if (Boolean.FALSE.equals(request.getPassed())) {
            transition(project, FeedbackStatus.AUDIT_REJECTED, "试验结果修改", user.getId(),
                    text(request.getRemark(), "复核不通过，请修改试验结果"));
            notifyTechnician(project);
        } else if (Boolean.TRUE.equals(request.getPassed())) {
            transition(project, FeedbackStatus.REVIEWED, "中试报告归档", user.getId(),
                    text(request.getRemark(), "复核确认通过"));
        } else {
            throw new BusinessException("请选择复核结果（通过或不通过）");
        }
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public FeedbackDetailResponse reportArchive(SysUser user, Long projectId, FeedbackActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireFeedbackProject(projectId);
        requireStatus(project, FeedbackStatus.REVIEWED);
        transition(project, FeedbackStatus.REPORT_ARCHIVED, "复核结果通知", user.getId(),
                text(request.getRemark(), "中试报告已归档"));
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public FeedbackDetailResponse reviewNotice(SysUser user, Long projectId, FeedbackActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireFeedbackProject(projectId);
        requireStatus(project, FeedbackStatus.REPORT_ARCHIVED);
        transition(project, FeedbackStatus.REVIEW_NOTICED, "复核意见反馈", user.getId(),
                text(request.getRemark(), "复核结果已通知企业"));
        projectMapper.updateById(project);
        notifyEnterprise(project, "复核结果待反馈");
        return getDetail(user, projectId);
    }

    @Transactional
    public FeedbackDetailResponse reviewFeedback(SysUser user, Long projectId, FeedbackActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.ENTERPRISE);
        TrialProject project = requireEnterpriseProject(user, projectId);
        requireStatus(project, FeedbackStatus.REVIEW_NOTICED, FeedbackStatus.FEEDBACK_AUDIT_REJECTED);
        FeedbackReport report = requireReport(projectId);
        saveReview(projectId, "ENTERPRISE_FEEDBACK", request, user.getId());
        transition(project, FeedbackStatus.REVIEW_FEEDBACK, "反馈结果审核", user.getId(),
                text(request.getRemark(), "企业已提交复核意见"));
        projectMapper.updateById(project);
        notifyAuditors(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public FeedbackDetailResponse feedbackAudit(SysUser user, Long projectId, FeedbackActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireFeedbackProject(projectId);
        requireStatus(project, FeedbackStatus.REVIEW_FEEDBACK);
        saveReview(projectId, "FEEDBACK_AUDIT", request, user.getId());
        if (Boolean.FALSE.equals(request.getPassed())) {
            transition(project, FeedbackStatus.FEEDBACK_AUDIT_REJECTED, "复核意见反馈", user.getId(),
                    text(request.getRemark(), "反馈审核不通过，请重新反馈"));
            notifyEnterprise(project, "反馈审核退回，请重新提交复核意见");
        } else {
            transition(project, FeedbackStatus.FEEDBACK_AUDIT_PASSED, "报告信息归档", user.getId(),
                    text(request.getRemark(), "反馈审核通过"));
        }
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public FeedbackDetailResponse caseArchive(SysUser user, Long projectId, FeedbackActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireFeedbackProject(projectId);
        requireStatus(project, FeedbackStatus.FEEDBACK_AUDIT_PASSED);
        transition(project, FeedbackStatus.CASE_ARCHIVED, "报告信息归档", user.getId(),
                text(request.getRemark(), "反馈段结案归档完成"));
        project.setStage(ProjectStage.ARCHIVE.name());
        project.setStatus("PENDING");
        project.setCurrentNode("项目台账维护");
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    public FeedbackDetailResponse getDetail(SysUser user, Long projectId) {
        TrialProject project = requireProject(projectId);
        assertReadable(user, project);
        FeedbackReport report = findReport(projectId);
        DispatchTask task = findTask(projectId);
        SysUser enterprise = project.getEnterpriseId() != null ? userMapper.selectById(project.getEnterpriseId()) : null;
        SysUser technician = task != null && task.getTechnicianId() != null
                ? userMapper.selectById(task.getTechnicianId()) : null;

        List<ReviewRecord> reviews = reviewMapper.selectList(new LambdaQueryWrapper<ReviewRecord>()
                .eq(ReviewRecord::getProjectId, projectId)
                .orderByAsc(ReviewRecord::getCreatedAt));

        List<WorkflowLog> logs = workflowLogMapper.selectList(new LambdaQueryWrapper<WorkflowLog>()
                .eq(WorkflowLog::getProjectId, projectId)
                .orderByAsc(WorkflowLog::getCreatedAt));

        FeedbackStatus status = ProjectStage.FEEDBACK.name().equals(project.getStage())
                ? FeedbackStatus.of(project.getStatus()) : FeedbackStatus.CASE_ARCHIVED;

        String reviewOpinion = reviews.stream()
                .filter(r -> "REVIEW".equals(r.getType()))
                .reduce((a, b) -> b)
                .map(ReviewRecord::getOpinion).orElse(null);

        String enterpriseFeedback = reviews.stream()
                .filter(r -> "ENTERPRISE_FEEDBACK".equals(r.getType()))
                .reduce((a, b) -> b)
                .map(ReviewRecord::getOpinion).orElse(null);

        return FeedbackDetailResponse.builder()
                .projectId(project.getId())
                .projectNo(project.getProjectNo())
                .title(project.getTitle())
                .stage(project.getStage())
                .status(project.getStatus())
                .statusLabel(ProjectStatusHelper.label(project))
                .currentNode(project.getCurrentNode())
                .enterpriseName(enterprise != null ? enterprise.getOrgName() : null)
                .technicianName(technician != null ? technician.getRealName() : null)
                .reportId(report != null ? report.getId() : null)
                .reportContent(report != null ? report.getContent() : null)
                .fileUrl(report != null ? report.getFileUrl() : null)
                .fileName(report != null ? report.getFileName() : null)
                .validateRemark(report != null ? report.getValidateRemark() : null)
                .modifyRemark(report != null ? report.getModifyRemark() : null)
                .reviewOpinion(reviewOpinion)
                .reviewFeedback(enterpriseFeedback)
                .reviews(reviews.stream().map(r -> {
                    SysUser reviewer = r.getReviewerId() != null ? userMapper.selectById(r.getReviewerId()) : null;
                    return FeedbackDetailResponse.ReviewVo.builder()
                            .type(r.getType())
                            .result(r.getResult())
                            .opinion(r.getOpinion())
                            .reviewerName(reviewer != null ? reviewer.getRealName() : null)
                            .time(r.getCreatedAt())
                            .build();
                }).toList())
                .steps(buildSteps(status))
                .logs(logs.stream().map(l -> FeedbackDetailResponse.LogItem.builder()
                        .fromStatus(l.getFromStatus())
                        .toStatus(l.getToStatus())
                        .remark(l.getRemark())
                        .time(l.getCreatedAt())
                        .build()).toList())
                .build();
    }

    public List<DemandTodoItem> listTodos(SysUser user) {
        UserRole role = UserRole.valueOf(user.getRole());
        if (role == UserRole.TECHNICIAN) {
            return listTechnicianTodos(user);
        }
        Set<FeedbackStatus> statuses = switch (role) {
            case AUDITOR -> FeedbackStatus.auditorTodos();
            case ENTERPRISE -> FeedbackStatus.enterpriseTodos();
            default -> Set.of();
        };
        if (statuses.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<TrialProject> qw = new LambdaQueryWrapper<TrialProject>()
                .eq(TrialProject::getStage, ProjectStage.FEEDBACK.name())
                .in(TrialProject::getStatus, statuses.stream().map(Enum::name).toList())
                .orderByDesc(TrialProject::getUpdatedAt);
        if (role == UserRole.ENTERPRISE) {
            qw.eq(TrialProject::getEnterpriseId, user.getId());
        }
        return projectMapper.selectList(qw).stream().map(p -> toTodoItem(p, role)).toList();
    }

    private List<DemandTodoItem> listTechnicianTodos(SysUser user) {
        List<DispatchTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<DispatchTask>()
                .eq(DispatchTask::getTechnicianId, user.getId())
                .orderByDesc(DispatchTask::getUpdatedAt));
        List<DemandTodoItem> items = new ArrayList<>();
        for (DispatchTask task : tasks) {
            TrialProject project = projectMapper.selectById(task.getProjectId());
            if (project == null || !ProjectStage.FEEDBACK.name().equals(project.getStage())) {
                continue;
            }
            FeedbackStatus status = FeedbackStatus.of(project.getStatus());
            if (!FeedbackStatus.technicianTodos().contains(status)) {
                continue;
            }
            items.add(toTodoItem(project, UserRole.TECHNICIAN));
        }
        return items;
    }

    private DemandTodoItem toTodoItem(TrialProject project, UserRole role) {
        FeedbackStatus status = FeedbackStatus.of(project.getStatus());
        String route = switch (role) {
            case TECHNICIAN -> switch (status) {
                case PENDING, AUDIT_REJECTED -> "/technician/feedback/submit";
                case SUBMITTED -> "/technician/feedback/validate";
                default -> "/technician/feedback/submit";
            };
            case AUDITOR -> switch (status) {
                case VALIDATED -> "/center/audit/feedback/audit";
                case AUDIT_PASSED -> "/center/audit/feedback/review";
                case REVIEWED -> "/center/audit/feedback/report-archive";
                case REPORT_ARCHIVED -> "/center/audit/feedback/review-notice";
                case REVIEW_FEEDBACK -> "/center/audit/feedback/feedback-audit";
                case FEEDBACK_AUDIT_PASSED -> "/center/audit/feedback/case-archive";
                default -> "/center/audit/feedback/audit";
            };
            case ENTERPRISE -> switch (status) {
                case REVIEW_NOTICED, FEEDBACK_AUDIT_REJECTED -> "/enterprise/feedback/review-feedback";
                default -> "/enterprise/feedback/review-detail";
            };
            default -> "/";
        };
        if (role == UserRole.TECHNICIAN && status == FeedbackStatus.AUDIT_REJECTED) {
            route = "/technician/feedback/modify";
        }
        return DemandTodoItem.builder()
                .projectId(project.getId())
                .projectNo(project.getProjectNo())
                .title(project.getTitle())
                .status(project.getStatus())
                .statusLabel(status.label())
                .currentNode(project.getCurrentNode())
                .action("办理")
                .route(route)
                .updatedAt(project.getUpdatedAt() != null ? project.getUpdatedAt().format(FMT) : "")
                .build();
    }

    private List<FeedbackDetailResponse.ProgressStep> buildSteps(FeedbackStatus current) {
        List<String> nodes = List.of(
                "试验结果提交",
                "试验数据校验",
                "中试报告审核",
                "结果复核确认",
                "中试报告归档",
                "复核结果通知",
                "复核意见反馈",
                "反馈结果审核",
                "报告信息归档"
        );
        int currentIdx = feedbackStepIndex(current);
        List<FeedbackDetailResponse.ProgressStep> steps = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            String st = i < currentIdx ? "done" : (i == currentIdx ? "active" : "pending");
            steps.add(FeedbackDetailResponse.ProgressStep.builder().node(nodes.get(i)).status(st).build());
        }
        return steps;
    }

    private static int feedbackStepIndex(FeedbackStatus status) {
        return switch (status) {
            case PENDING -> 0;
            case SUBMITTED -> 1;
            case VALIDATED, AUDIT_REJECTED -> 2;
            case AUDIT_PASSED -> 3;
            case REVIEWED -> 4;
            case REPORT_ARCHIVED -> 5;
            case REVIEW_NOTICED -> 6;
            case REVIEW_FEEDBACK, FEEDBACK_AUDIT_REJECTED -> 7;
            case FEEDBACK_AUDIT_PASSED, CASE_ARCHIVED -> 8;
        };
    }

    private void transition(TrialProject project, FeedbackStatus to, String node, Long operatorId, String remark) {
        String from = project.getStatus();
        project.setStatus(to.name());
        project.setCurrentNode(node);
        project.setUpdatedAt(LocalDateTime.now());
        WorkflowLog log = new WorkflowLog();
        log.setProjectId(project.getId());
        log.setFromStatus(from);
        log.setToStatus(to.name());
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        workflowLogMapper.insert(log);
    }

    private void saveReview(Long projectId, String type, FeedbackActionRequest request, Long reviewerId) {
        ReviewRecord record = new ReviewRecord();
        record.setProjectId(projectId);
        record.setType(type);
        if ("ENTERPRISE_FEEDBACK".equals(type)) {
            record.setResult("SUBMITTED");
            record.setOpinion(text(request.getRemark(), request.getContent()));
        } else {
            record.setResult(Boolean.FALSE.equals(request.getPassed()) ? "REJECTED" : "PASSED");
            record.setOpinion(request.getRemark());
        }
        record.setReviewerId(reviewerId);
        record.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(record);
    }

    private FeedbackReport requireReport(Long projectId) {
        FeedbackReport report = findReport(projectId);
        if (report == null) {
            report = new FeedbackReport();
            report.setProjectId(projectId);
            DispatchTask task = findTask(projectId);
            report.setTaskId(task != null ? task.getId() : null);
            report.setCreatedAt(LocalDateTime.now());
            report.setUpdatedAt(LocalDateTime.now());
            reportMapper.insert(report);
        }
        return report;
    }

    private FeedbackReport findReport(Long projectId) {
        return reportMapper.selectOne(new LambdaQueryWrapper<FeedbackReport>()
                .eq(FeedbackReport::getProjectId, projectId)
                .orderByDesc(FeedbackReport::getId)
                .last("LIMIT 1"));
    }

    private DispatchTask findTask(Long projectId) {
        return taskMapper.selectOne(new LambdaQueryWrapper<DispatchTask>()
                .eq(DispatchTask::getProjectId, projectId)
                .orderByDesc(DispatchTask::getId)
                .last("LIMIT 1"));
    }

    private void assertTechnician(SysUser user, Long projectId) {
        DispatchTask task = findTask(projectId);
        if (task == null || !user.getId().equals(task.getTechnicianId())) {
            throw new BusinessException(403, "无权操作该项目");
        }
    }

    private void notifyEnterprise(TrialProject project, String title) {
        if (project.getEnterpriseId() != null) {
            notificationService.notifyUser(project.getEnterpriseId(), project.getId(), "FEEDBACK",
                    title, project.getProjectNo() + " " + project.getTitle());
        }
    }

    private void notifyTechnician(TrialProject project) {
        DispatchTask task = findTask(project.getId());
        if (task != null && task.getTechnicianId() != null) {
            notificationService.notifyUser(task.getTechnicianId(), project.getId(), "FEEDBACK",
                    "报告审核退回", project.getProjectNo() + " 请修改试验结果");
        }
    }

    private void notifyAuditors(TrialProject project) {
        userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRole, UserRole.AUDITOR.name()))
                .forEach(u -> notificationService.notifyUser(u.getId(), project.getId(), "FEEDBACK",
                        "反馈待审核", project.getProjectNo() + " " + project.getCurrentNode()));
    }

    private TrialProject requireProject(Long projectId) {
        TrialProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    private TrialProject requireFeedbackProject(Long projectId) {
        TrialProject project = requireProject(projectId);
        if (!ProjectStage.FEEDBACK.name().equals(project.getStage())) {
            throw new BusinessException("项目不在反馈阶段");
        }
        return project;
    }

    private TrialProject requireEnterpriseProject(SysUser user, Long projectId) {
        TrialProject project = requireFeedbackProject(projectId);
        if (!UserRole.ENTERPRISE.name().equals(user.getRole()) || !user.getId().equals(project.getEnterpriseId())) {
            throw new BusinessException(403, "无权操作该项目");
        }
        return project;
    }

    private void assertReadable(SysUser user, TrialProject project) {
        UserRole role = UserRole.valueOf(user.getRole());
        if (role == UserRole.ENTERPRISE && !user.getId().equals(project.getEnterpriseId())) {
            throw new BusinessException(403, "无权查看该项目");
        }
        if (role == UserRole.TECHNICIAN) {
            DispatchTask task = findTask(project.getId());
            if (task == null || !user.getId().equals(task.getTechnicianId())) {
                throw new BusinessException(403, "无权查看该项目");
            }
        }
    }

    private void requireStatus(TrialProject project, FeedbackStatus... expected) {
        FeedbackStatus current = FeedbackStatus.of(project.getStatus());
        for (FeedbackStatus s : expected) {
            if (s == current) {
                return;
            }
        }
        throw new BusinessException("当前状态为「" + current.label() + "」，不可执行此操作");
    }

    private String text(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    /** §17.9 跨模块进度：反馈段环节步骤 */
    public List<FeedbackDetailResponse.ProgressStep> exportProgressSteps(TrialProject project) {
        FeedbackStatus status = ProjectStage.FEEDBACK.name().equals(project.getStage())
                ? FeedbackStatus.of(project.getStatus()) : FeedbackStatus.CASE_ARCHIVED;
        return buildSteps(status);
    }
}
