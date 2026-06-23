package com.gzpprod.center.module.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzpprod.center.common.BusinessException;
import com.gzpprod.center.common.EvaluationStatus;
import com.gzpprod.center.common.ProjectStage;
import com.gzpprod.center.common.SecurityUtils;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.demand.dto.DemandTodoItem;
import com.gzpprod.center.module.demand.entity.TrialProject;
import com.gzpprod.center.module.demand.entity.WorkflowLog;
import com.gzpprod.center.module.demand.mapper.TrialProjectMapper;
import com.gzpprod.center.module.demand.mapper.WorkflowLogMapper;
import com.gzpprod.center.module.evaluation.dto.EvaluationDetailResponse;
import com.gzpprod.center.module.evaluation.dto.EvaluationMaterialRequest;
import com.gzpprod.center.module.evaluation.entity.Evaluation;
import com.gzpprod.center.module.evaluation.entity.EvaluationMaterial;
import com.gzpprod.center.module.evaluation.mapper.EvaluationMapper;
import com.gzpprod.center.module.evaluation.mapper.EvaluationMaterialMapper;
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
public class EvaluationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TrialProjectMapper projectMapper;
    private final EvaluationMapper evaluationMapper;
    private final EvaluationMaterialMapper materialMapper;
    private final WorkflowLogMapper workflowLogMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;

    @Transactional
    public void initForProject(Long projectId) {
        Long count = evaluationMapper.selectCount(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getProjectId, projectId));
        if (count == 0) {
            Evaluation evaluation = new Evaluation();
            evaluation.setProjectId(projectId);
            evaluationMapper.insert(evaluation);
        }
    }

    @Transactional
    public EvaluationDetailResponse precheck(SysUser user, Long projectId, EvaluationMaterialRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireEvalProject(projectId);
        requireStatus(project, EvaluationStatus.PRECHECK);
        Evaluation evaluation = requireEvaluation(projectId);
        evaluation.setPrecheckRemark(text(request.getRemark(), "前置核查通过"));
        evaluationMapper.updateById(evaluation);
        transition(project, EvaluationStatus.CONDITION_EVAL, "中试条件评估", user.getId(), evaluation.getPrecheckRemark());
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public EvaluationDetailResponse condition(SysUser user, Long projectId, EvaluationMaterialRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireEvalProject(projectId);
        requireStatus(project, EvaluationStatus.CONDITION_EVAL);
        Evaluation evaluation = requireEvaluation(projectId);
        boolean passed = Boolean.TRUE.equals(request.getPassed());
        evaluation.setConditionResult(passed ? "PASS" : "FAIL");
        evaluation.setConditionRemark(request.getRemark());
        evaluationMapper.updateById(evaluation);
        if (passed) {
            transition(project, EvaluationStatus.RESOURCE, "资源需求核定", user.getId(),
                    text(request.getRemark(), "具备中试条件"));
        } else {
            transition(project, EvaluationStatus.CONDITION_RETURNED, "条件整改通知", user.getId(),
                    text(request.getRemark(), "暂不具备中试条件"));
            notifyEnterprise(project);
        }
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public EvaluationDetailResponse rectifyNotice(SysUser user, Long projectId, EvaluationMaterialRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireEvalProject(projectId);
        requireStatus(project, EvaluationStatus.CONDITION_RETURNED);
        Evaluation evaluation = requireEvaluation(projectId);
        evaluation.setRectifyNotice(text(request.getNotice(), "请补充条件材料"));
        evaluationMapper.updateById(evaluation);
        notifyEnterprise(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public EvaluationDetailResponse conditionSupplement(SysUser user, Long projectId, EvaluationMaterialRequest request) {
        TrialProject project = requireEnterpriseProject(user, projectId);
        requireStatus(project, EvaluationStatus.CONDITION_RETURNED);
        Evaluation evaluation = requireEvaluation(projectId);
        saveMaterials(evaluation.getId(), request.getMaterials(), nextVersion(evaluation.getId()));
        transition(project, EvaluationStatus.PRECHECK, "评估前置核查", user.getId(), "企业已补充条件材料");
        projectMapper.updateById(project);
        notificationService.notifyRole(UserRole.DISPATCHER, projectId, "EVALUATION",
                "条件材料已补充", project.getProjectNo());
        return getDetail(user, projectId);
    }

    @Transactional
    public EvaluationDetailResponse resource(SysUser user, Long projectId, EvaluationMaterialRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireEvalProject(projectId);
        requireStatus(project, EvaluationStatus.RESOURCE);
        Evaluation evaluation = requireEvaluation(projectId);
        evaluation.setResourceRequirement(request.getRequirement());
        evaluation.setResourceRemark(text(request.getRemark(), "资源核定完成"));
        evaluationMapper.updateById(evaluation);
        transition(project, EvaluationStatus.FEASIBILITY, "技术可行性审", user.getId(), evaluation.getResourceRemark());
        projectMapper.updateById(project);
        notificationService.notifyRole(UserRole.AUDITOR, projectId, "EVALUATION",
                "待可行性审查", project.getProjectNo());
        return getDetail(user, projectId);
    }

    @Transactional
    public EvaluationDetailResponse feasibility(SysUser user, Long projectId, EvaluationMaterialRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireEvalProject(projectId);
        requireStatus(project, EvaluationStatus.FEASIBILITY);
        Evaluation evaluation = requireEvaluation(projectId);
        boolean passed = Boolean.TRUE.equals(request.getPassed());
        evaluation.setFeasibilityResult(passed ? "PASS" : "FAIL");
        evaluation.setFeasibilityRemark(request.getRemark());
        evaluationMapper.updateById(evaluation);
        if (passed) {
            transition(project, EvaluationStatus.CONCLUSION, "评估结论出具", user.getId(),
                    text(request.getRemark(), "技术可行"));
        } else {
            transition(project, EvaluationStatus.FEASIBILITY_RETURNED, "评估材料补充", user.getId(),
                    text(request.getRemark(), "需补充评估材料"));
            notifyEnterprise(project);
        }
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public EvaluationDetailResponse supplement(SysUser user, Long projectId, EvaluationMaterialRequest request) {
        TrialProject project = requireEnterpriseProject(user, projectId);
        requireStatus(project, EvaluationStatus.FEASIBILITY_RETURNED);
        Evaluation evaluation = requireEvaluation(projectId);
        saveMaterials(evaluation.getId(), request.getMaterials(), nextVersion(evaluation.getId()));
        transition(project, EvaluationStatus.FEASIBILITY, "技术可行性审", user.getId(), "企业已补充评估材料");
        projectMapper.updateById(project);
        notificationService.notifyRole(UserRole.AUDITOR, projectId, "EVALUATION",
                "评估材料已补充", project.getProjectNo());
        return getDetail(user, projectId);
    }

    @Transactional
    public EvaluationDetailResponse conclusion(SysUser user, Long projectId, EvaluationMaterialRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireEvalProject(projectId);
        requireStatus(project, EvaluationStatus.CONCLUSION);
        Evaluation evaluation = requireEvaluation(projectId);
        evaluation.setConclusion(text(request.getConclusion(), "评估通过，建议进入调度执行阶段"));
        evaluation.setConclusionOpinion(request.getOpinion());
        evaluationMapper.updateById(evaluation);
        project.setCurrentNode("评估结果签收");
        project.setUpdatedAt(LocalDateTime.now());
        projectMapper.updateById(project);
        notifyEnterprise(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public EvaluationDetailResponse receipt(SysUser user, Long projectId) {
        TrialProject project = requireEnterpriseProject(user, projectId);
        requireStatus(project, EvaluationStatus.CONCLUSION);
        transition(project, EvaluationStatus.RECEIPTED, "评估结果签收", user.getId(), "企业签收评估结论");
        projectMapper.updateById(project);
        notificationService.notifyRole(UserRole.DISPATCHER, projectId, "EVALUATION",
                "待评估归档", project.getProjectNo());
        return getDetail(user, projectId);
    }

    @Transactional
    public EvaluationDetailResponse feedback(SysUser user, Long projectId, EvaluationMaterialRequest request) {
        TrialProject project = requireEnterpriseProject(user, projectId);
        requireStatus(project, EvaluationStatus.RECEIPTED);
        Evaluation evaluation = requireEvaluation(projectId);
        evaluation.setFeedbackContent(text(request.getContent(), "无异议"));
        evaluationMapper.updateById(evaluation);
        transition(project, EvaluationStatus.FEEDBACK, "评估意见反馈", user.getId(), evaluation.getFeedbackContent());
        projectMapper.updateById(project);
        notificationService.notifyRole(UserRole.DISPATCHER, projectId, "EVALUATION",
                "评估意见已反馈", project.getProjectNo());
        return getDetail(user, projectId);
    }

    @Transactional
    public EvaluationDetailResponse archive(SysUser user, Long projectId) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireEvalProject(projectId);
        EvaluationStatus current = EvaluationStatus.of(project.getStatus());
        if (current != EvaluationStatus.RECEIPTED && current != EvaluationStatus.FEEDBACK) {
            throw new BusinessException("当前状态不可归档");
        }
        transition(project, EvaluationStatus.ARCHIVED, "评估信息归档", user.getId(), "评估段归档完成");
        project.setStage(ProjectStage.DISPATCH.name());
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    public EvaluationDetailResponse getDetail(SysUser user, Long projectId) {
        TrialProject project = requireProject(projectId);
        assertReadable(user, project);
        Evaluation evaluation = requireEvaluation(projectId);
        SysUser enterprise = project.getEnterpriseId() != null ? userMapper.selectById(project.getEnterpriseId()) : null;
        List<EvaluationMaterial> materials = materialMapper.selectList(new LambdaQueryWrapper<EvaluationMaterial>()
                .eq(EvaluationMaterial::getEvaluationId, evaluation.getId())
                .orderByDesc(EvaluationMaterial::getVersion, EvaluationMaterial::getId));
        List<WorkflowLog> logs = workflowLogMapper.selectList(new LambdaQueryWrapper<WorkflowLog>()
                .eq(WorkflowLog::getProjectId, projectId)
                .orderByAsc(WorkflowLog::getCreatedAt));
        EvaluationStatus status = project.getStage().equals(ProjectStage.EVALUATION.name())
                ? EvaluationStatus.of(project.getStatus()) : EvaluationStatus.ARCHIVED;

        return EvaluationDetailResponse.builder()
                .projectId(project.getId())
                .projectNo(project.getProjectNo())
                .title(project.getTitle())
                .stage(project.getStage())
                .status(project.getStatus())
                .statusLabel(status.label())
                .currentNode(project.getCurrentNode())
                .enterpriseName(enterprise != null ? enterprise.getOrgName() : null)
                .precheckRemark(evaluation.getPrecheckRemark())
                .conditionResult(evaluation.getConditionResult())
                .conditionRemark(evaluation.getConditionRemark())
                .rectifyNotice(evaluation.getRectifyNotice())
                .resourceRemark(evaluation.getResourceRemark())
                .resourceRequirement(evaluation.getResourceRequirement())
                .feasibilityResult(evaluation.getFeasibilityResult())
                .feasibilityRemark(evaluation.getFeasibilityRemark())
                .conclusion(evaluation.getConclusion())
                .conclusionOpinion(evaluation.getConclusionOpinion())
                .feedbackContent(evaluation.getFeedbackContent())
                .materials(materials.stream().map(m -> EvaluationDetailResponse.MaterialVo.builder()
                        .id(m.getId()).fileUrl(m.getFileUrl()).fileName(m.getFileName())
                        .materialType(m.getMaterialType()).version(m.getVersion()).build()).toList())
                .steps(buildSteps(status))
                .logs(logs.stream().map(l -> EvaluationDetailResponse.LogItem.builder()
                        .fromStatus(l.getFromStatus()).toStatus(l.getToStatus())
                        .remark(l.getRemark()).time(l.getCreatedAt()).build()).toList())
                .build();
    }

    public List<DemandTodoItem> listTodos(SysUser user) {
        UserRole role = UserRole.valueOf(user.getRole());
        Set<EvaluationStatus> statuses = switch (role) {
            case DISPATCHER -> EvaluationStatus.dispatcherTodos();
            case AUDITOR -> EvaluationStatus.auditorTodos();
            case ENTERPRISE -> EvaluationStatus.enterpriseTodos();
            default -> Set.of();
        };
        if (statuses.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<TrialProject> qw = new LambdaQueryWrapper<TrialProject>()
                .eq(TrialProject::getStage, ProjectStage.EVALUATION.name())
                .in(TrialProject::getStatus, statuses.stream().map(Enum::name).toList())
                .orderByDesc(TrialProject::getUpdatedAt);
        if (role == UserRole.ENTERPRISE) {
            qw.eq(TrialProject::getEnterpriseId, user.getId());
        }
        return projectMapper.selectList(qw).stream().map(p -> toTodoItem(p, role)).toList();
    }

    private DemandTodoItem toTodoItem(TrialProject project, UserRole role) {
        EvaluationStatus status = EvaluationStatus.of(project.getStatus());
        String route = switch (role) {
            case DISPATCHER -> switch (status) {
                case PRECHECK -> "/center/dispatch/evaluation/precheck";
                case CONDITION_EVAL -> "/center/dispatch/evaluation/condition";
                case CONDITION_RETURNED -> "/center/dispatch/evaluation/rectify-notice";
                case RESOURCE -> "/center/dispatch/evaluation/resource";
                case RECEIPTED, FEEDBACK -> "/center/dispatch/evaluation/archive";
                default -> "/center/dispatch/evaluation/precheck";
            };
            case AUDITOR -> switch (status) {
                case FEASIBILITY, FEASIBILITY_RETURNED -> "/center/audit/evaluation/feasibility";
                case CONCLUSION -> "/center/audit/evaluation/conclusion";
                default -> "/center/audit/evaluation/feasibility";
            };
            case ENTERPRISE -> switch (status) {
                case CONDITION_RETURNED -> "/enterprise/evaluation/condition-supplement";
                case FEASIBILITY_RETURNED -> "/enterprise/evaluation/eval-supplement";
                case CONCLUSION -> "/enterprise/evaluation/conclusion-receipt";
                case RECEIPTED -> "/enterprise/evaluation/feedback";
                default -> "/enterprise/evaluation/conclusion-detail";
            };
            default -> "/";
        };
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

    private List<EvaluationDetailResponse.ProgressStep> buildSteps(EvaluationStatus current) {
        record Node(String name, EvaluationStatus threshold) {}
        List<Node> nodes = List.of(
                new Node("评估前置核查", EvaluationStatus.PRECHECK),
                new Node("中试条件评估", EvaluationStatus.CONDITION_EVAL),
                new Node("资源需求核定", EvaluationStatus.RESOURCE),
                new Node("技术可行性审", EvaluationStatus.FEASIBILITY),
                new Node("评估结论出具", EvaluationStatus.CONCLUSION),
                new Node("评估结果签收", EvaluationStatus.RECEIPTED),
                new Node("评估信息归档", EvaluationStatus.ARCHIVED)
        );
        int currentOrd = current.ordinal();
        List<EvaluationDetailResponse.ProgressStep> steps = new ArrayList<>();
        for (Node node : nodes) {
            String st = node.threshold.ordinal() < currentOrd ? "done"
                    : node.threshold == current ? "active" : "pending";
            steps.add(EvaluationDetailResponse.ProgressStep.builder().node(node.name()).status(st).build());
        }
        return steps;
    }

    private void transition(TrialProject project, EvaluationStatus to, String node, Long operatorId, String remark) {
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

    private void notifyEnterprise(TrialProject project) {
        if (project.getEnterpriseId() != null) {
            notificationService.notifyUser(project.getEnterpriseId(), project.getId(), "EVALUATION",
                    "评估状态更新", project.getProjectNo() + " " + project.getCurrentNode());
        }
    }

    private TrialProject requireProject(Long projectId) {
        TrialProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    private TrialProject requireEvalProject(Long projectId) {
        TrialProject project = requireProject(projectId);
        if (!ProjectStage.EVALUATION.name().equals(project.getStage())) {
            throw new BusinessException("项目不在评估阶段");
        }
        return project;
    }

    private TrialProject requireEnterpriseProject(SysUser user, Long projectId) {
        TrialProject project = requireEvalProject(projectId);
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
    }

    private Evaluation requireEvaluation(Long projectId) {
        Evaluation evaluation = evaluationMapper.selectOne(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getProjectId, projectId));
        if (evaluation == null) {
            throw new BusinessException("评估记录不存在");
        }
        return evaluation;
    }

    private void requireStatus(TrialProject project, EvaluationStatus expected) {
        if (!expected.name().equals(project.getStatus())) {
            throw new BusinessException("当前状态为「" + EvaluationStatus.of(project.getStatus()).label() + "」，不可执行此操作");
        }
    }

    private int nextVersion(Long evaluationId) {
        return materialMapper.selectList(new LambdaQueryWrapper<EvaluationMaterial>()
                        .eq(EvaluationMaterial::getEvaluationId, evaluationId))
                .stream().mapToInt(m -> m.getVersion() == null ? 1 : m.getVersion()).max().orElse(0) + 1;
    }

    private void saveMaterials(Long evaluationId, List<EvaluationMaterialRequest.MaterialItem> items, int version) {
        if (items == null) return;
        for (EvaluationMaterialRequest.MaterialItem item : items) {
            if (!StringUtils.hasText(item.getFileUrl())) continue;
            EvaluationMaterial m = new EvaluationMaterial();
            m.setEvaluationId(evaluationId);
            m.setFileUrl(item.getFileUrl());
            m.setFileName(item.getFileName());
            m.setMaterialType(item.getMaterialType());
            m.setVersion(version);
            materialMapper.insert(m);
        }
    }

    private String text(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
