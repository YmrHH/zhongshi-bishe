package com.gzpprod.center.module.demand.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzpprod.center.common.BusinessException;
import com.gzpprod.center.common.DemandStatus;
import com.gzpprod.center.common.EvaluationStatus;
import com.gzpprod.center.common.ProjectStage;
import com.gzpprod.center.common.ProjectStatusHelper;
import com.gzpprod.center.common.SecurityUtils;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.demand.dto.*;
import com.gzpprod.center.module.demand.entity.Demand;
import com.gzpprod.center.module.demand.entity.DemandMaterial;
import com.gzpprod.center.module.demand.entity.TrialProject;
import com.gzpprod.center.module.demand.entity.WorkflowLog;
import com.gzpprod.center.module.demand.mapper.DemandMapper;
import com.gzpprod.center.module.demand.mapper.DemandMaterialMapper;
import com.gzpprod.center.module.demand.mapper.TrialProjectMapper;
import com.gzpprod.center.module.demand.mapper.WorkflowLogMapper;
import com.gzpprod.center.module.notification.service.NotificationService;
import com.gzpprod.center.module.evaluation.service.EvaluationService;
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
public class DemandService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TrialProjectMapper projectMapper;
    private final DemandMapper demandMapper;
    private final DemandMaterialMapper materialMapper;
    private final WorkflowLogMapper workflowLogMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;
    private final EvaluationService evaluationService;

    @Transactional
    public ProjectCreateResponse createProject(SysUser user, DemandFormRequest request) {
        SecurityUtils.requireRole(user, UserRole.ENTERPRISE);
        TrialProject project = new TrialProject();
        project.setProjectNo(nextProjectNo());
        project.setTitle(request.getTitle());
        project.setEnterpriseId(user.getId());
        project.setStage(ProjectStage.DEMAND.name());
        project.setStatus(DemandStatus.DRAFT.name());
        project.setCurrentNode("需求信息确认");
        projectMapper.insert(project);

        Demand demand = new Demand();
        demand.setProjectId(project.getId());
        fillDemand(demand, request);
        demandMapper.insert(demand);
        saveMaterials(demand.getId(), request.getMaterials(), 1);

        return ProjectCreateResponse.builder()
                .projectId(project.getId())
                .projectNo(project.getProjectNo())
                .build();
    }

    @Transactional
    public DemandDetailResponse saveDraft(SysUser user, Long projectId, DemandFormRequest request) {
        TrialProject project = requireEnterpriseProject(user, projectId);
        requireStatus(project, DemandStatus.DRAFT);
        project.setTitle(request.getTitle());
        projectMapper.updateById(project);

        Demand demand = requireDemand(projectId);
        fillDemand(demand, request);
        demandMapper.updateById(demand);
        replaceMaterials(demand.getId(), request.getMaterials(), 1);
        return getDetail(user, projectId);
    }

    @Transactional
    public DemandDetailResponse submit(SysUser user, Long projectId, DemandFormRequest request) {
        TrialProject project = requireEnterpriseProject(user, projectId);
        requireStatus(project, DemandStatus.DRAFT);
        if (!StringUtils.hasText(request.getContent())) {
            throw new BusinessException("需求内容不能为空");
        }
        project.setTitle(request.getTitle());
        transition(project, DemandStatus.SUBMITTED, "中试需求提交", user.getId(), "企业提交需求");
        projectMapper.updateById(project);

        Demand demand = requireDemand(projectId);
        fillDemand(demand, request);
        demandMapper.updateById(demand);
        replaceMaterials(demand.getId(), request.getMaterials(), 1);

        notificationService.notifyRole(UserRole.DISPATCHER, projectId, "DEMAND",
                "新需求待受理", project.getProjectNo() + " " + project.getTitle());
        return getDetail(user, projectId);
    }

    @Transactional
    public DemandDetailResponse acceptRegister(SysUser user, Long projectId, DemandAcceptRegisterRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireProject(projectId);
        requireStatus(project, DemandStatus.SUBMITTED);
        transition(project, DemandStatus.ACCEPTING, "需求受理登记", user.getId(),
                request.getRemark() != null ? request.getRemark() : "调度员受理登记");
        projectMapper.updateById(project);
        notificationService.notifyRole(UserRole.AUDITOR, projectId, "DEMAND",
                "需求材料待核验", project.getProjectNo());
        return getDetail(user, projectId);
    }

    @Transactional
    public DemandDetailResponse verify(SysUser user, Long projectId, DemandVerifyRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireProject(projectId);
        requireStatus(project, DemandStatus.ACCEPTING);
        if (request.isComplete()) {
            transition(project, DemandStatus.VERIFYING, "受理材料核验", user.getId(),
                    StringUtils.hasText(request.getOpinion()) ? request.getOpinion() : "材料齐全");
        } else {
            Demand demand = requireDemand(projectId);
            demand.setRejectReason(StringUtils.hasText(request.getOpinion()) ? request.getOpinion() : "材料不齐");
            demandMapper.updateById(demand);
            transition(project, DemandStatus.RETURNED, "需求退回通知", user.getId(), demand.getRejectReason());
            notifyEnterprise(project);
        }
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public DemandDetailResponse reject(SysUser user, Long projectId, DemandRejectRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireProject(projectId);
        DemandStatus current = DemandStatus.of(project.getStatus());
        if (current != DemandStatus.ACCEPTING && current != DemandStatus.VERIFYING) {
            throw new BusinessException("当前状态不可退回");
        }
        Demand demand = requireDemand(projectId);
        demand.setRejectReason(request.getReason());
        demandMapper.updateById(demand);
        transition(project, DemandStatus.RETURNED, "需求退回通知", user.getId(), request.getReason());
        projectMapper.updateById(project);
        notifyEnterprise(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public DemandDetailResponse supplement(SysUser user, Long projectId, DemandSupplementRequest request) {
        TrialProject project = requireEnterpriseProject(user, projectId);
        requireStatus(project, DemandStatus.RETURNED);
        Demand demand = requireDemand(projectId);
        if (StringUtils.hasText(request.getContent())) {
            demand.setContent(request.getContent());
        }
        demand.setRejectReason(null);
        demandMapper.updateById(demand);

        int nextVersion = materialMapper.selectList(new LambdaQueryWrapper<DemandMaterial>()
                        .eq(DemandMaterial::getDemandId, demand.getId()))
                .stream().mapToInt(m -> m.getVersion() == null ? 1 : m.getVersion()).max().orElse(0) + 1;
        if (request.getMaterials() != null && !request.getMaterials().isEmpty()) {
            saveMaterials(demand.getId(), request.getMaterials(), nextVersion);
        }

        transition(project, DemandStatus.SUBMITTED, "需求材料补充", user.getId(), "企业补充材料后重新提交");
        projectMapper.updateById(project);
        notificationService.notifyRole(UserRole.DISPATCHER, projectId, "DEMAND",
                "需求材料已补充", project.getProjectNo());
        return getDetail(user, projectId);
    }

    @Transactional
    public DemandDetailResponse acceptResult(SysUser user, Long projectId, DemandAcceptResultRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireProject(projectId);
        requireStatus(project, DemandStatus.VERIFYING);
        Demand demand = requireDemand(projectId);
        demand.setAcceptOpinion(request.getOpinion());
        if (request.isAccepted()) {
            demand.setAcceptResult("ACCEPTED");
            demandMapper.updateById(demand);
            transition(project, DemandStatus.ACCEPTED, "受理结果通知", user.getId(),
                    StringUtils.hasText(request.getOpinion()) ? request.getOpinion() : "同意受理");
            notifyEnterprise(project);
        } else {
            demand.setAcceptResult("REJECTED");
            demandMapper.updateById(demand);
            String from = project.getStatus();
            project.setStage(ProjectStage.CLOSED.name());
            project.setStatus("CLOSED");
            project.setCurrentNode("不予受理");
            project.setUpdatedAt(LocalDateTime.now());
            WorkflowLog log = new WorkflowLog();
            log.setProjectId(project.getId());
            log.setFromStatus(from);
            log.setToStatus("CLOSED");
            log.setOperatorId(user.getId());
            log.setRemark(StringUtils.hasText(request.getOpinion()) ? request.getOpinion() : "不同意受理");
            workflowLogMapper.insert(log);
            notifyEnterprise(project);
        }
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public DemandDetailResponse receipt(SysUser user, Long projectId) {
        TrialProject project = requireEnterpriseProject(user, projectId);
        requireStatus(project, DemandStatus.ACCEPTED);
        transition(project, DemandStatus.RECEIPTED, "受理回执签收", user.getId(), "企业签收受理回执");
        projectMapper.updateById(project);
        notificationService.notifyRole(UserRole.DISPATCHER, projectId, "DEMAND",
                "待需求归档", project.getProjectNo());
        return getDetail(user, projectId);
    }

    @Transactional
    public DemandDetailResponse archive(SysUser user, Long projectId) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireProject(projectId);
        requireStatus(project, DemandStatus.RECEIPTED);
        transition(project, DemandStatus.ARCHIVED, "受理信息归档", user.getId(), "需求段归档完成");
        project.setStage(ProjectStage.EVALUATION.name());
        project.setStatus(EvaluationStatus.PRECHECK.name());
        project.setCurrentNode("评估前置核查");
        projectMapper.updateById(project);
        evaluationService.initForProject(project.getId());
        return getDetail(user, projectId);
    }

    public DemandDetailResponse getDetail(SysUser user, Long projectId) {
        TrialProject project = requireProject(projectId);
        assertReadable(user, project);
        Demand demand = requireDemand(projectId);
        SysUser enterprise = project.getEnterpriseId() != null
                ? userMapper.selectById(project.getEnterpriseId()) : null;

        List<DemandMaterial> materials = materialMapper.selectList(new LambdaQueryWrapper<DemandMaterial>()
                .eq(DemandMaterial::getDemandId, demand.getId())
                .orderByDesc(DemandMaterial::getVersion, DemandMaterial::getId));

        List<WorkflowLog> logs = workflowLogMapper.selectList(new LambdaQueryWrapper<WorkflowLog>()
                .eq(WorkflowLog::getProjectId, projectId)
                .orderByAsc(WorkflowLog::getCreatedAt));

        DemandStatus stepStatus = ProjectStatusHelper.demandStepStatus(project);
        return DemandDetailResponse.builder()
                .projectId(project.getId())
                .projectNo(project.getProjectNo())
                .title(project.getTitle())
                .stage(project.getStage())
                .status(project.getStatus())
                .statusLabel(ProjectStatusHelper.label(project))
                .currentNode(project.getCurrentNode())
                .content(demand.getContent())
                .pilotType(demand.getPilotType())
                .expectedDays(demand.getExpectedDays())
                .contactName(demand.getContactName())
                .contactPhone(demand.getContactPhone())
                .rejectReason(demand.getRejectReason())
                .acceptOpinion(demand.getAcceptOpinion())
                .acceptResult(demand.getAcceptResult())
                .enterpriseName(enterprise != null ? enterprise.getOrgName() : null)
                .materials(materials.stream().map(m -> DemandDetailResponse.MaterialVo.builder()
                        .id(m.getId())
                        .fileUrl(m.getFileUrl())
                        .fileName(m.getFileName())
                        .materialType(m.getMaterialType())
                        .version(m.getVersion())
                        .build()).toList())
                .steps(buildSteps(stepStatus))
                .logs(logs.stream().map(l -> DemandDetailResponse.LogItem.builder()
                        .fromStatus(l.getFromStatus())
                        .toStatus(l.getToStatus())
                        .remark(l.getRemark())
                        .time(l.getCreatedAt())
                        .build()).toList())
                .build();
    }

    public List<DemandTodoItem> listTodos(SysUser user) {
        UserRole role = UserRole.valueOf(user.getRole());
        Set<DemandStatus> statuses = switch (role) {
            case DISPATCHER -> DemandStatus.dispatcherTodos();
            case AUDITOR -> DemandStatus.auditorTodos();
            case ENTERPRISE -> DemandStatus.enterpriseTodos();
            default -> Set.of();
        };
        if (statuses.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<TrialProject> qw = new LambdaQueryWrapper<TrialProject>()
                .eq(TrialProject::getStage, ProjectStage.DEMAND.name())
                .in(TrialProject::getStatus, statuses.stream().map(Enum::name).toList())
                .orderByDesc(TrialProject::getUpdatedAt);
        if (role == UserRole.ENTERPRISE) {
            qw.eq(TrialProject::getEnterpriseId, user.getId());
        }

        return projectMapper.selectList(qw).stream()
                .map(p -> toTodoItem(p, role))
                .toList();
    }

    private DemandTodoItem toTodoItem(TrialProject project, UserRole role) {
        DemandStatus status = DemandStatus.of(project.getStatus());
        String action = "办理";
        String route = switch (role) {
            case DISPATCHER -> switch (status) {
                case SUBMITTED, ACCEPTING -> "/center/dispatch/demand/workbench";
                case RECEIPTED -> "/center/dispatch/demand/archive";
                default -> "/center/dispatch/demand/workbench";
            };
            case AUDITOR -> switch (status) {
                case ACCEPTING -> "/center/audit/demand/verify";
                case VERIFYING -> "/center/audit/demand/accept-result";
                default -> "/center/audit/demand/verify";
            };
            case ENTERPRISE -> switch (status) {
                case DRAFT -> "/enterprise/demand/preview";
                case RETURNED -> "/enterprise/demand/supplement";
                case ACCEPTED -> "/enterprise/demand/receipt";
                default -> "/enterprise/demand/progress";
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
                .action(action)
                .route(route)
                .updatedAt(project.getUpdatedAt() != null ? project.getUpdatedAt().format(FMT) : "")
                .build();
    }

    private List<DemandDetailResponse.ProgressStep> buildSteps(DemandStatus current) {
        record Node(String name, DemandStatus threshold) {}
        List<Node> nodes = List.of(
                new Node("需求信息确认", DemandStatus.DRAFT),
                new Node("中试需求提交", DemandStatus.SUBMITTED),
                new Node("需求受理登记", DemandStatus.ACCEPTING),
                new Node("受理材料核验", DemandStatus.VERIFYING),
                new Node("受理结果通知", DemandStatus.ACCEPTED),
                new Node("受理回执签收", DemandStatus.RECEIPTED),
                new Node("受理信息归档", DemandStatus.ARCHIVED)
        );
        int currentOrd = current.ordinal();
        List<DemandDetailResponse.ProgressStep> steps = new ArrayList<>();
        for (Node node : nodes) {
            String st = node.threshold.ordinal() < currentOrd ? "done"
                    : node.threshold == current ? "active" : "pending";
            steps.add(DemandDetailResponse.ProgressStep.builder()
                    .node(node.name())
                    .status(st)
                    .build());
        }
        return steps;
    }

    private void transition(TrialProject project, DemandStatus to, String node, Long operatorId, String remark) {
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
            notificationService.notifyUser(project.getEnterpriseId(), project.getId(), "DEMAND",
                    "需求状态更新", project.getProjectNo() + " " + project.getCurrentNode());
        }
    }

    private TrialProject requireProject(Long projectId) {
        TrialProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    private TrialProject requireEnterpriseProject(SysUser user, Long projectId) {
        TrialProject project = requireProject(projectId);
        if (!UserRole.ENTERPRISE.name().equals(user.getRole())
                || !user.getId().equals(project.getEnterpriseId())) {
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

    private Demand requireDemand(Long projectId) {
        Demand demand = demandMapper.selectOne(new LambdaQueryWrapper<Demand>()
                .eq(Demand::getProjectId, projectId));
        if (demand == null) {
            throw new BusinessException("需求记录不存在");
        }
        return demand;
    }

    private void requireStatus(TrialProject project, DemandStatus expected) {
        if (!ProjectStage.DEMAND.name().equals(project.getStage())) {
            throw new BusinessException("项目已离开需求阶段");
        }
        if (!expected.name().equals(project.getStatus())) {
            throw new BusinessException("当前状态为「" + ProjectStatusHelper.label(project) + "」，不可执行此操作");
        }
    }

    private void fillDemand(Demand demand, DemandFormRequest request) {
        demand.setContent(request.getContent());
        demand.setPilotType(request.getPilotType());
        demand.setExpectedDays(request.getExpectedDays());
        demand.setContactName(request.getContactName());
        demand.setContactPhone(request.getContactPhone());
    }

    private void replaceMaterials(Long demandId, List<DemandFormRequest.MaterialItem> items, int version) {
        materialMapper.delete(new LambdaQueryWrapper<DemandMaterial>().eq(DemandMaterial::getDemandId, demandId));
        saveMaterials(demandId, items, version);
    }

    private void saveMaterials(Long demandId, List<DemandFormRequest.MaterialItem> items, int version) {
        if (items == null) {
            return;
        }
        for (DemandFormRequest.MaterialItem item : items) {
            if (!StringUtils.hasText(item.getFileUrl())) {
                continue;
            }
            DemandMaterial m = new DemandMaterial();
            m.setDemandId(demandId);
            m.setFileUrl(item.getFileUrl());
            m.setFileName(item.getFileName());
            m.setMaterialType(item.getMaterialType());
            m.setVersion(version);
            materialMapper.insert(m);
        }
    }

    private String nextProjectNo() {
        int year = LocalDateTime.now().getYear();
        long count = projectMapper.selectCount(null);
        return String.format("ZS-%d-%03d", year, count + 1);
    }
}
