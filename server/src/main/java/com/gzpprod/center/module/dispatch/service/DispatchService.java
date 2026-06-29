package com.gzpprod.center.module.dispatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzpprod.center.common.*;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.demand.dto.DemandTodoItem;
import com.gzpprod.center.module.demand.entity.TrialProject;
import com.gzpprod.center.module.demand.entity.WorkflowLog;
import com.gzpprod.center.module.demand.mapper.TrialProjectMapper;
import com.gzpprod.center.module.demand.mapper.WorkflowLogMapper;
import com.gzpprod.center.module.dispatch.dto.DispatchActionRequest;
import com.gzpprod.center.module.dispatch.dto.DispatchDetailResponse;
import com.gzpprod.center.module.dispatch.entity.DispatchTask;
import com.gzpprod.center.module.dispatch.entity.Resource;
import com.gzpprod.center.module.dispatch.entity.TaskProgress;
import com.gzpprod.center.module.dispatch.mapper.DispatchTaskMapper;
import com.gzpprod.center.module.dispatch.mapper.ResourceMapper;
import com.gzpprod.center.module.dispatch.mapper.TaskProgressMapper;
import com.gzpprod.center.module.evaluation.entity.Evaluation;
import com.gzpprod.center.module.evaluation.mapper.EvaluationMapper;
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
public class DispatchService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TrialProjectMapper projectMapper;
    private final ResourceMapper resourceMapper;
    private final DispatchTaskMapper taskMapper;
    private final TaskProgressMapper progressMapper;
    private final WorkflowLogMapper workflowLogMapper;
    private final SysUserMapper userMapper;
    private final EvaluationMapper evaluationMapper;
    private final NotificationService notificationService;

    public void seedResourcesIfEmpty() {
        if (resourceMapper.selectCount(null) > 0) {
            return;
        }
        seedResource("中试放大线A", "工艺放大", "500L/批次");
        seedResource("中试验证平台B", "产品验证", "200kg/批次");
        seedResource("稳定性测试舱C", "稳定性测试", "3个并行工位");
    }

    @Transactional
    public DispatchDetailResponse match(SysUser user, Long projectId, DispatchActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireDispatchProject(projectId);
        requireStatus(project, DispatchStatus.MATCH, DispatchStatus.MATCH_FAILED);
        if (Boolean.TRUE.equals(request.getPassed())) {
            transition(project, DispatchStatus.ASSIGNED, "中试任务派发", user.getId(),
                    text(request.getRemark(), "资源匹配成功"));
        } else {
            transition(project, DispatchStatus.MATCH_FAILED, "中试资源匹配", user.getId(),
                    text(request.getRemark(), "暂无合适资源，请重新匹配"));
        }
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public DispatchDetailResponse assign(SysUser user, Long projectId, DispatchActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireDispatchProject(projectId);
        requireStatus(project, DispatchStatus.ASSIGNED);
        if (request.getResourceId() == null || request.getTechnicianId() == null) {
            throw new BusinessException("请选择资源与技术人员");
        }
        DispatchTask task = requireTask(projectId);
        if (task == null) {
            task = new DispatchTask();
            task.setProjectId(projectId);
        }
        task.setResourceId(request.getResourceId());
        task.setTechnicianId(request.getTechnicianId());
        task.setStatus("PENDING");
        task.setProgressPct(0);
        task.setRemark(request.getRemark());
        if (task.getId() == null) {
            taskMapper.insert(task);
        } else {
            taskMapper.updateById(task);
        }
        transition(project, DispatchStatus.ASSIGNED, "中试任务派发", user.getId(), "任务已派发给技术人员");
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    @Transactional
    public DispatchDetailResponse assignNotice(SysUser user, Long projectId, DispatchActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireDispatchProject(projectId);
        requireStatus(project, DispatchStatus.ASSIGNED);
        DispatchTask task = requireTaskOrThrow(projectId);
        task.setStatus("PENDING_RECEIVE");
        taskMapper.updateById(task);
        transition(project, DispatchStatus.PENDING_RECEIVE, "派单结果通知", user.getId(), "派单通知已发送");
        projectMapper.updateById(project);
        notificationService.notifyUser(task.getTechnicianId(), projectId, "DISPATCH",
                "新任务待接收", project.getProjectNo() + " " + project.getTitle());
        return getDetail(user, projectId);
    }

    @Transactional
    public DispatchDetailResponse receiveTask(SysUser user, Long taskId) {
        SecurityUtils.requireRole(user, UserRole.TECHNICIAN);
        DispatchTask task = requireTaskById(taskId);
        assertTaskOwner(user, task);
        if (!"PENDING_RECEIVE".equals(task.getStatus())) {
            throw new BusinessException("当前任务状态不可接收");
        }
        TrialProject project = requireDispatchProject(task.getProjectId());
        requireStatus(project, DispatchStatus.PENDING_RECEIVE);
        task.setStatus("RECEIVED");
        taskMapper.updateById(task);
        transition(project, DispatchStatus.RECEIVED, "接收任务执行", user.getId(), "技术人员已接收任务");
        projectMapper.updateById(project);
        notificationService.notifyRole(UserRole.DISPATCHER, project.getId(), "DISPATCH",
                "任务已接收", project.getProjectNo());
        return getDetail(user, project.getId());
    }

    @Transactional
    public DispatchDetailResponse confirmTask(SysUser user, Long taskId) {
        SecurityUtils.requireRole(user, UserRole.TECHNICIAN);
        DispatchTask task = requireTaskById(taskId);
        assertTaskOwner(user, task);
        if (!"RECEIVED".equals(task.getStatus())) {
            throw new BusinessException("请先接收任务后再确认签收");
        }
        TrialProject project = requireDispatchProject(task.getProjectId());
        requireStatus(project, DispatchStatus.RECEIVED);
        task.setStatus("CONFIRMED");
        taskMapper.updateById(task);
        transition(project, DispatchStatus.CONFIRMED, "任务接收确认", user.getId(), "技术人员确认签收任务");
        projectMapper.updateById(project);
        return getDetail(user, project.getId());
    }

    @Transactional
    public DispatchDetailResponse reportProgress(SysUser user, Long taskId, DispatchActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.TECHNICIAN);
        DispatchTask task = requireTaskById(taskId);
        assertTaskOwner(user, task);
        TrialProject project = requireDispatchProject(task.getProjectId());
        requireStatus(project, DispatchStatus.CONFIRMED, DispatchStatus.EXECUTING, DispatchStatus.PROGRESS_ACKED);
        int pct = request.getProgressPct() != null ? request.getProgressPct() : task.getProgressPct();
        task.setProgressPct(pct);
        task.setStatus("EXECUTING");
        taskMapper.updateById(task);

        TaskProgress record = new TaskProgress();
        record.setTaskId(taskId);
        record.setProgressPct(pct);
        record.setContent(text(request.getContent(), "进度更新"));
        record.setReportTime(LocalDateTime.now());
        progressMapper.insert(record);

        transition(project, DispatchStatus.EXECUTING, "填报执行进度", user.getId(), record.getContent());
        projectMapper.updateById(project);
        notificationService.notifyRole(UserRole.DISPATCHER, project.getId(), "DISPATCH",
                "执行进度更新", project.getProjectNo() + " " + pct + "%");
        if (project.getEnterpriseId() != null) {
            notificationService.notifyUser(project.getEnterpriseId(), project.getId(), "DISPATCH",
                    "执行进度更新", project.getProjectNo() + " " + pct + "%");
        }
        return getDetail(user, project.getId());
    }

    @Transactional
    public DispatchDetailResponse supervise(SysUser user, Long projectId, DispatchActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireDispatchProject(projectId);
        if (DispatchStatus.PROGRESS_ACKED.name().equals(project.getStatus()) && project.getEnterpriseId() != null) {
            transition(project, DispatchStatus.EXECUTING, "查看执行进度", user.getId(),
                    "督办通知：请企业再次查看执行进度");
            projectMapper.updateById(project);
        } else {
            WorkflowLog log = new WorkflowLog();
            log.setProjectId(projectId);
            log.setFromStatus(project.getStatus());
            log.setToStatus(project.getStatus());
            log.setOperatorId(user.getId());
            log.setRemark("进度通报督办：" + text(request.getRemark(), "请关注执行进度"));
            workflowLogMapper.insert(log);
        }
        if (project.getEnterpriseId() != null) {
            notificationService.notifyUser(project.getEnterpriseId(), projectId, "DISPATCH",
                    "进度通报", project.getProjectNo());
        }
        return getDetail(user, projectId);
    }

    @Transactional
    public DispatchDetailResponse acknowledgeProgress(SysUser user, Long projectId, DispatchActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.ENTERPRISE);
        TrialProject project = requireProject(projectId);
        assertEnterpriseOwner(user, project);
        if (!ProjectStage.DISPATCH.name().equals(project.getStage())) {
            throw new BusinessException("项目已进入后续阶段，请返回首页查看待办");
        }
        requireStatus(project, DispatchStatus.EXECUTING, DispatchStatus.CONFIRMED, DispatchStatus.EXEC_DONE);
        transition(project, DispatchStatus.PROGRESS_ACKED, "查看执行进度", user.getId(),
                "企业确认执行进度知悉：" + text(request != null ? request.getRemark() : null, "已查看当前执行进度"));
        projectMapper.updateById(project);
        notificationService.notifyRole(UserRole.DISPATCHER, projectId, "DISPATCH",
                "企业已确认进度知悉", project.getProjectNo());
        return getDetail(user, projectId);
    }

    @Transactional
    public DispatchDetailResponse reassign(SysUser user, Long projectId, DispatchActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireDispatchProject(projectId);
        requireStatus(project, DispatchStatus.RECEIVED, DispatchStatus.CONFIRMED,
                DispatchStatus.EXECUTING, DispatchStatus.PROGRESS_ACKED);
        DispatchTask task = requireTaskOrThrow(projectId);
        if (request.getTechnicianId() != null) {
            task.setTechnicianId(request.getTechnicianId());
        }
        task.setStatus("PENDING_RECEIVE");
        task.setProgressPct(0);
        taskMapper.updateById(task);
        transition(project, DispatchStatus.PENDING_RECEIVE, "异常任务重派", user.getId(),
                text(request.getRemark(), "任务已重新派发"));
        projectMapper.updateById(project);
        notificationService.notifyUser(task.getTechnicianId(), projectId, "DISPATCH",
                "任务重派", project.getProjectNo());
        return getDetail(user, projectId);
    }

    @Transactional
    public DispatchDetailResponse execConfirm(SysUser user, Long projectId, DispatchActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireDispatchProject(projectId);
        requireStatus(project, DispatchStatus.EXECUTING, DispatchStatus.PROGRESS_ACKED);
        DispatchTask task = requireTaskOrThrow(projectId);
        int progressPct = task.getProgressPct() == null ? 0 : task.getProgressPct();
        if (progressPct < 100) {
            throw new BusinessException("请先完成100%进度填报后再确认执行结果");
        }
        transition(project, DispatchStatus.EXEC_DONE, "调度信息归档", user.getId(),
                text(request.getRemark(), "执行结果确认通过"));
        closeDispatchTask(projectId);
        projectMapper.updateById(project);
        if (project.getEnterpriseId() != null) {
            notificationService.notifyUser(project.getEnterpriseId(), projectId, "DISPATCH",
                    "执行结果待查看", project.getProjectNo());
        }
        return getDetail(user, projectId);
    }

    @Transactional
    public DispatchDetailResponse archive(SysUser user, Long projectId) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireDispatchProject(projectId);
        requireStatus(project, DispatchStatus.EXEC_DONE);
        transition(project, DispatchStatus.ARCHIVED, "调度信息归档", user.getId(), "调度段归档完成");
        closeDispatchTask(projectId);
        project.setStage(ProjectStage.FEEDBACK.name());
        project.setStatus("PENDING");
        project.setCurrentNode("试验结果提交");
        projectMapper.updateById(project);
        return getDetail(user, projectId);
    }

    public DispatchDetailResponse getDetail(SysUser user, Long projectId) {
        TrialProject project = requireProject(projectId);
        assertReadable(user, project);
        DispatchTask task = requireTask(projectId);
        Resource resource = task != null && task.getResourceId() != null
                ? resourceMapper.selectById(task.getResourceId()) : null;
        SysUser enterprise = project.getEnterpriseId() != null ? userMapper.selectById(project.getEnterpriseId()) : null;
        SysUser technician = task != null && task.getTechnicianId() != null
                ? userMapper.selectById(task.getTechnicianId()) : null;

        List<TaskProgress> records = task != null
                ? progressMapper.selectList(new LambdaQueryWrapper<TaskProgress>()
                .eq(TaskProgress::getTaskId, task.getId())
                .orderByAsc(TaskProgress::getReportTime))
                : List.of();

        List<WorkflowLog> logs = workflowLogMapper.selectList(new LambdaQueryWrapper<WorkflowLog>()
                .eq(WorkflowLog::getProjectId, projectId)
                .orderByAsc(WorkflowLog::getCreatedAt));

        DispatchStatus status = ProjectStage.DISPATCH.name().equals(project.getStage())
                ? DispatchStatus.of(project.getStatus()) : DispatchStatus.ARCHIVED;

        Evaluation evaluation = evaluationMapper.selectOne(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getProjectId, projectId));

        return DispatchDetailResponse.builder()
                .projectId(project.getId())
                .projectNo(project.getProjectNo())
                .title(project.getTitle())
                .stage(project.getStage())
                .status(project.getStatus())
                .statusLabel(ProjectStatusHelper.label(project))
                .currentNode(project.getCurrentNode())
                .enterpriseName(enterprise != null ? enterprise.getOrgName() : null)
                .taskId(task != null ? task.getId() : null)
                .resourceId(task != null ? task.getResourceId() : null)
                .resourceName(resource != null ? resource.getName() : null)
                .technicianId(task != null ? task.getTechnicianId() : null)
                .technicianName(technician != null ? technician.getRealName() : null)
                .progressPct(task != null ? (task.getProgressPct() == null ? 0 : task.getProgressPct()) : 0)
                .taskRemark(task != null ? task.getRemark() : null)
                .evaluationSummary(buildEvaluationSummary(evaluation))
                .resources(listResources())
                .technicians(listTechnicians())
                .progressRecords(records.stream().map(r -> DispatchDetailResponse.ProgressVo.builder()
                        .progressPct(r.getProgressPct() == null ? 0 : r.getProgressPct())
                        .content(r.getContent())
                        .reportTime(r.getReportTime())
                        .build()).toList())
                .steps(buildSteps(status))
                .logs(logs.stream().map(l -> DispatchDetailResponse.LogVo.builder()
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
            return taskMapper.selectList(new LambdaQueryWrapper<DispatchTask>()
                            .eq(DispatchTask::getTechnicianId, user.getId())
                            .in(DispatchTask::getStatus, List.of("PENDING_RECEIVE", "RECEIVED", "CONFIRMED", "EXECUTING"))
                            .orderByDesc(DispatchTask::getUpdatedAt))
                    .stream()
                    .filter(this::isActiveDispatchTask)
                    .map(t -> toTaskTodo(t, user))
                    .toList();
        }
        Set<DispatchStatus> statuses = switch (role) {
            case DISPATCHER -> DispatchStatus.dispatcherTodos();
            case ENTERPRISE -> DispatchStatus.enterpriseTodos();
            default -> Set.of();
        };
        if (statuses.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<TrialProject> qw = new LambdaQueryWrapper<TrialProject>()
                .eq(TrialProject::getStage, ProjectStage.DISPATCH.name())
                .in(TrialProject::getStatus, statuses.stream().map(Enum::name).toList())
                .orderByDesc(TrialProject::getUpdatedAt);
        if (role == UserRole.ENTERPRISE) {
            qw.eq(TrialProject::getEnterpriseId, user.getId());
        }
        return projectMapper.selectList(qw).stream().map(p -> toProjectTodo(p, role)).toList();
    }

    private DemandTodoItem toProjectTodo(TrialProject project, UserRole role) {
        DispatchStatus status = DispatchStatus.of(project.getStatus());
        DispatchTask task = requireTask(project.getId());
        String route = switch (role) {
            case DISPATCHER -> switch (status) {
                case MATCH, MATCH_FAILED -> "/center/dispatch/dispatch/match";
                case ASSIGNED -> "/center/dispatch/dispatch/assign";
                case PENDING_RECEIVE -> "/center/dispatch/dispatch/assign-notice";
                case RECEIVED, EXECUTING, CONFIRMED, PROGRESS_ACKED -> "/center/dispatch/dispatch/supervise";
                case EXEC_DONE -> "/center/dispatch/dispatch/archive";
                default -> "/center/dispatch/dispatch/match";
            };
            case ENTERPRISE -> "/enterprise/dispatch/progress-view";
            default -> "/";
        };
        return DemandTodoItem.builder()
                .projectId(project.getId())
                .taskId(task != null ? task.getId() : null)
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

    private DemandTodoItem toTaskTodo(DispatchTask task, SysUser user) {
        TrialProject project = requireProject(task.getProjectId());
        String route = switch (task.getStatus()) {
            case "PENDING_RECEIVE" -> "/technician/dispatch/receive";
            case "RECEIVED" -> "/technician/dispatch/confirm";
            default -> "/technician/dispatch/progress-report";
        };
        return DemandTodoItem.builder()
                .projectId(project.getId())
                .taskId(task.getId())
                .projectNo(project.getProjectNo())
                .title(project.getTitle())
                .status(task.getStatus())
                .statusLabel(ProjectStatusHelper.label(project))
                .currentNode(project.getCurrentNode())
                .action("办理")
                .route(route)
                .updatedAt(task.getUpdatedAt() != null ? task.getUpdatedAt().format(FMT) : "")
                .build();
    }

    private DispatchDetailResponse.EvaluationSummaryVo buildEvaluationSummary(Evaluation evaluation) {
        if (evaluation == null) {
            return null;
        }
        return DispatchDetailResponse.EvaluationSummaryVo.builder()
                .conclusion(evaluation.getConclusion())
                .conclusionOpinion(evaluation.getConclusionOpinion())
                .resourceRequirement(evaluation.getResourceRequirement())
                .resourceRemark(evaluation.getResourceRemark())
                .conditionResult(evaluation.getConditionResult())
                .feasibilityResult(evaluation.getFeasibilityResult())
                .feedbackContent(evaluation.getFeedbackContent())
                .build();
    }

    private List<DispatchDetailResponse.TechnicianVo> listTechnicians() {
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRole, UserRole.TECHNICIAN.name()))
                .stream()
                .map(u -> DispatchDetailResponse.TechnicianVo.builder()
                        .id(u.getId()).realName(u.getRealName()).orgName(u.getOrgName()).build())
                .toList();
    }

    private List<DispatchDetailResponse.ResourceVo> listResources() {
        return resourceMapper.selectList(new LambdaQueryWrapper<Resource>()
                        .eq(Resource::getStatus, "AVAILABLE"))
                .stream()
                .map(r -> DispatchDetailResponse.ResourceVo.builder()
                        .id(r.getId()).name(r.getName()).type(r.getType())
                        .capacity(r.getCapacity()).status(r.getStatus()).build())
                .toList();
    }

    private List<DispatchDetailResponse.StepVo> buildSteps(DispatchStatus current) {
        record Node(String name) {}
        List<Node> nodes = List.of(
                new Node("中试资源匹配"),
                new Node("中试任务派发"),
                new Node("派单结果通知"),
                new Node("接收任务执行"),
                new Node("任务接收确认"),
                new Node("填报执行进度"),
                new Node("执行结果确认")
        );
        int currentIdx = dispatchStepIndex(current);
        List<DispatchDetailResponse.StepVo> steps = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            String st = i < currentIdx ? "done" : (i == currentIdx ? "active" : "pending");
            steps.add(DispatchDetailResponse.StepVo.builder().node(nodes.get(i).name()).status(st).build());
        }
        return steps;
    }

    private static int dispatchStepIndex(DispatchStatus status) {
        return switch (status) {
            case MATCH, MATCH_FAILED -> 0;
            case ASSIGNED, NOTICED -> 1;
            case PENDING_RECEIVE -> 2;
            case RECEIVED -> 3;
            case CONFIRMED -> 4;
            case EXECUTING, PROGRESS_ACKED -> 5;
            case EXEC_DONE, ARCHIVED -> 6;
        };
    }

    private void closeDispatchTask(Long projectId) {
        DispatchTask task = requireTask(projectId);
        if (task != null) {
            task.setStatus("CLOSED");
            task.setUpdatedAt(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    private boolean isActiveDispatchTask(DispatchTask task) {
        TrialProject project = projectMapper.selectById(task.getProjectId());
        return project != null && ProjectStage.DISPATCH.name().equals(project.getStage());
    }

    private void transition(TrialProject project, DispatchStatus to, String node, Long operatorId, String remark) {
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

    private void seedResource(String name, String type, String capacity) {
        Resource r = new Resource();
        r.setName(name);
        r.setType(type);
        r.setCapacity(capacity);
        r.setStatus("AVAILABLE");
        resourceMapper.insert(r);
    }

    private TrialProject requireProject(Long projectId) {
        TrialProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    private TrialProject requireDispatchProject(Long projectId) {
        TrialProject project = requireProject(projectId);
        if (!ProjectStage.DISPATCH.name().equals(project.getStage())) {
            throw new BusinessException("项目不在调度阶段");
        }
        return project;
    }

    private void assertReadable(SysUser user, TrialProject project) {
        UserRole role = UserRole.valueOf(user.getRole());
        if (role == UserRole.ENTERPRISE && !user.getId().equals(project.getEnterpriseId())) {
            throw new BusinessException(403, "无权查看该项目");
        }
    }

    private void assertEnterpriseOwner(SysUser user, TrialProject project) {
        if (!UserRole.ENTERPRISE.name().equals(user.getRole()) || !user.getId().equals(project.getEnterpriseId())) {
            throw new BusinessException(403, "无权操作该项目");
        }
    }

    private void assertTaskOwner(SysUser user, DispatchTask task) {
        if (!user.getId().equals(task.getTechnicianId())) {
            throw new BusinessException(403, "无权操作该任务");
        }
    }

    private DispatchTask requireTask(Long projectId) {
        return taskMapper.selectOne(new LambdaQueryWrapper<DispatchTask>()
                .eq(DispatchTask::getProjectId, projectId)
                .orderByDesc(DispatchTask::getId)
                .last("LIMIT 1"));
    }

    private DispatchTask requireTaskOrThrow(Long projectId) {
        DispatchTask task = requireTask(projectId);
        if (task == null) {
            throw new BusinessException("调度任务不存在，请先派发任务");
        }
        return task;
    }

    private DispatchTask requireTaskById(Long taskId) {
        DispatchTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        return task;
    }

    private void requireStatus(TrialProject project, DispatchStatus... expected) {
        for (DispatchStatus s : expected) {
            if (s.name().equals(project.getStatus())) {
                return;
            }
        }
        throw new BusinessException("当前状态为「" + ProjectStatusHelper.label(project) + "」，不可执行此操作");
    }

    private String text(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    /** §17.9 跨模块进度：调度段环节步骤 */
    public List<DispatchDetailResponse.StepVo> exportProgressSteps(TrialProject project) {
        DispatchStatus status = ProjectStage.DISPATCH.name().equals(project.getStage())
                ? DispatchStatus.of(project.getStatus()) : DispatchStatus.ARCHIVED;
        return buildSteps(status);
    }
}
