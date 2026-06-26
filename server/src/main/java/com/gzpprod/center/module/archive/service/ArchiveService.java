package com.gzpprod.center.module.archive.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzpprod.center.common.*;
import com.gzpprod.center.module.archive.dto.*;
import com.gzpprod.center.module.archive.entity.ProjectArchive;
import com.gzpprod.center.module.archive.entity.ServiceBrief;
import com.gzpprod.center.module.archive.mapper.ProjectArchiveMapper;
import com.gzpprod.center.module.archive.mapper.ServiceBriefMapper;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.demand.dto.DemandTodoItem;
import com.gzpprod.center.module.demand.entity.TrialProject;
import com.gzpprod.center.module.demand.entity.WorkflowLog;
import com.gzpprod.center.module.demand.mapper.TrialProjectMapper;
import com.gzpprod.center.module.demand.mapper.WorkflowLogMapper;
import com.gzpprod.center.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TrialProjectMapper projectMapper;
    private final WorkflowLogMapper workflowLogMapper;
    private final ProjectArchiveMapper archiveMapper;
    private final ServiceBriefMapper briefMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;

    public ArchiveDetailResponse getLedger(SysUser user, Long projectId) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireArchiveProject(projectId);
        return buildDetail(user, project);
    }

    @Transactional
    public ArchiveDetailResponse updateLedger(SysUser user, ArchiveActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        if (request.getProjectId() == null) {
            throw new BusinessException("请指定项目");
        }
        TrialProject project = requireArchiveProject(request.getProjectId());
        requireStatus(project, ArchiveStatus.PENDING, ArchiveStatus.LEDGER_INCOMPLETE);
        ProjectArchive archive = requireArchiveRecord(project.getId());
        archive.setLedgerJson(text(request.getLedgerJson(), archive.getLedgerJson()));
        archive.setUpdatedAt(LocalDateTime.now());
        archiveMapper.updateById(archive);
        if (Boolean.TRUE.equals(request.getComplete())) {
            transition(project, ArchiveStatus.LEDGER_OK, "档案信息确认", user.getId(),
                    text(request.getRemark(), "项目台账维护完成"));
        } else {
            transition(project, ArchiveStatus.LEDGER_INCOMPLETE, "项目台账维护", user.getId(),
                    text(request.getRemark(), "台账信息不完整，请补充"));
        }
        projectMapper.updateById(project);
        return buildDetail(user, project);
    }

    @Transactional
    public ArchiveDetailResponse confirm(SysUser user, Long projectId, ArchiveActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireArchiveProject(projectId);
        requireStatus(project, ArchiveStatus.LEDGER_OK);
        transition(project, ArchiveStatus.CONFIRMED, "结案资料归集", user.getId(),
                text(request != null ? request.getRemark() : null, "档案信息确认通过"));
        projectMapper.updateById(project);
        return buildDetail(user, project);
    }

    @Transactional
    public ArchiveDetailResponse collect(SysUser user, Long projectId, ArchiveActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireArchiveProject(projectId);
        requireStatus(project, ArchiveStatus.CONFIRMED);
        ProjectArchive archive = requireArchiveRecord(project.getId());
        archive.setCollectRemark(text(request.getCollectRemark(), "结案资料已归集"));
        archive.setUpdatedAt(LocalDateTime.now());
        archiveMapper.updateById(archive);
        transition(project, ArchiveStatus.COLLECTED, "中试周期统计", user.getId(), archive.getCollectRemark());
        projectMapper.updateById(project);
        notifyDispatchers(project);
        return buildDetail(user, project);
    }

    public ArchiveStatsResponse cycleStats(SysUser user, Long projectId) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        requireArchiveProject(projectId);
        return buildStats(true);
    }

    public ArchiveStatsResponse successRate(SysUser user, Long projectId) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        requireArchiveProject(projectId);
        return buildStats(false);
    }

    @Transactional
    public ArchiveDetailResponse confirmStats(SysUser user, Long projectId, ArchiveActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        TrialProject project = requireArchiveProject(projectId);
        requireStatus(project, ArchiveStatus.COLLECTED, ArchiveStatus.STATS_UNAVAILABLE);
        ArchiveStatsResponse stats = buildStats(true);
        if (Boolean.FALSE.equals(request.getPassed()) || !stats.isAvailable()) {
            transition(project, ArchiveStatus.STATS_UNAVAILABLE, "中试周期统计", user.getId(),
                    text(request.getRemark(), stats.getMessage()));
        } else {
            transition(project, ArchiveStatus.STATS_OK, "服务简报生成", user.getId(),
                    text(request.getRemark(), "周期统计数据确认"));
        }
        projectMapper.updateById(project);
        return buildDetail(user, project);
    }

    @Transactional
    public ArchiveDetailResponse generateBrief(SysUser user, ArchiveActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.DISPATCHER);
        if (request.getProjectId() == null) {
            throw new BusinessException("请指定项目");
        }
        TrialProject project = requireArchiveProject(request.getProjectId());
        requireStatus(project, ArchiveStatus.STATS_OK, ArchiveStatus.BRIEF_RETURNED);
        ArchiveStatsResponse stats = buildStats(false);
        ProjectArchive archive = requireArchiveRecord(project.getId());
        ServiceBrief brief = archive.getBriefId() != null ? briefMapper.selectById(archive.getBriefId()) : null;
        if (brief == null) {
            brief = new ServiceBrief();
            brief.setProjectId(project.getId());
            brief.setCreatedAt(LocalDateTime.now());
        }
        brief.setTitle(text(request.getTitle(), project.getProjectNo() + " 中试服务简报"));
        brief.setContent(text(request.getContent(), buildDefaultBriefContent(project, stats)));
        brief.setStatsJson(statsJson(stats));
        brief.setAuditStatus("PENDING");
        brief.setAuditRemark(null);
        brief.setGeneratorId(user.getId());
        brief.setUpdatedAt(LocalDateTime.now());
        if (brief.getId() == null) {
            briefMapper.insert(brief);
        } else {
            briefMapper.updateById(brief);
        }
        archive.setBriefId(brief.getId());
        archive.setUpdatedAt(LocalDateTime.now());
        archiveMapper.updateById(archive);
        transition(project, ArchiveStatus.BRIEF_GENERATED, "简报内容审核", user.getId(), "服务简报已生成");
        projectMapper.updateById(project);
        notifyAuditors(project);
        return buildDetail(user, project);
    }

    @Transactional
    public ArchiveDetailResponse auditBrief(SysUser user, ArchiveActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        if (request.getProjectId() == null) {
            throw new BusinessException("请指定项目");
        }
        TrialProject project = requireArchiveProject(request.getProjectId());
        requireStatus(project, ArchiveStatus.BRIEF_GENERATED);
        ProjectArchive archive = requireArchiveRecord(project.getId());
        ServiceBrief brief = briefMapper.selectById(archive.getBriefId());
        if (brief == null) {
            throw new BusinessException("简报不存在");
        }
        brief.setAuditorId(user.getId());
        brief.setUpdatedAt(LocalDateTime.now());
        if (Boolean.FALSE.equals(request.getPassed())) {
            brief.setAuditStatus("REJECTED");
            brief.setAuditRemark(text(request.getRemark(), "简报内容需修改"));
            briefMapper.updateById(brief);
            transition(project, ArchiveStatus.BRIEF_RETURNED, "服务简报生成", user.getId(), brief.getAuditRemark());
            notifyDispatchers(project);
        } else {
            brief.setAuditStatus("PASSED");
            brief.setAuditRemark(text(request.getRemark(), "简报审核通过"));
            briefMapper.updateById(brief);
            transition(project, ArchiveStatus.BRIEF_PUBLISHED, "档案信息归档", user.getId(), brief.getAuditRemark());
            if (project.getEnterpriseId() != null) {
                notificationService.notifyUser(project.getEnterpriseId(), project.getId(), "ARCHIVE",
                        "服务简报已发布", project.getProjectNo() + " 可查看服务简报");
            }
        }
        projectMapper.updateById(project);
        return buildDetail(user, project);
    }

    public BriefDetailResponse getBrief(SysUser user, Long briefId) {
        ServiceBrief brief = briefMapper.selectById(briefId);
        if (brief == null) {
            throw new BusinessException("简报不存在");
        }
        TrialProject project = projectMapper.selectById(brief.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        assertBriefReadable(user, project, brief);
        return BriefDetailResponse.builder()
                .id(brief.getId())
                .projectId(project.getId())
                .projectNo(project.getProjectNo())
                .title(project.getTitle())
                .briefTitle(brief.getTitle())
                .content(brief.getContent())
                .auditStatus(brief.getAuditStatus())
                .statsJson(brief.getStatsJson())
                .createdAt(brief.getCreatedAt() != null ? brief.getCreatedAt().format(FMT) : "")
                .build();
    }

    @Transactional
    public ArchiveDetailResponse archive(SysUser user, Long projectId, ArchiveActionRequest request) {
        SecurityUtils.requireRole(user, UserRole.AUDITOR);
        TrialProject project = requireArchiveProject(projectId);
        requireStatus(project, ArchiveStatus.BRIEF_PUBLISHED);
        transition(project, ArchiveStatus.CLOSED, "档案信息归档", user.getId(),
                text(request != null ? request.getRemark() : null, "档案段结案归档完成"));
        project.setStage(ProjectStage.CLOSED.name());
        project.setStatus("CLOSED");
        projectMapper.updateById(project);
        return buildDetail(user, project);
    }

    public ArchiveDetailResponse getDetail(SysUser user, Long projectId) {
        TrialProject project = requireProject(projectId);
        assertReadable(user, project);
        return buildDetail(user, project);
    }

    public List<DemandTodoItem> listTodos(SysUser user) {
        UserRole role = UserRole.valueOf(user.getRole());
        Set<ArchiveStatus> statuses = switch (role) {
            case AUDITOR -> ArchiveStatus.auditorTodos();
            case DISPATCHER -> ArchiveStatus.dispatcherTodos();
            case ENTERPRISE -> ArchiveStatus.enterpriseTodos();
            default -> Set.of();
        };
        if (statuses.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<TrialProject> qw = new LambdaQueryWrapper<TrialProject>()
                .eq(TrialProject::getStage, ProjectStage.ARCHIVE.name())
                .in(TrialProject::getStatus, statuses.stream().map(Enum::name).toList())
                .orderByDesc(TrialProject::getUpdatedAt);
        if (role == UserRole.ENTERPRISE) {
            qw.eq(TrialProject::getEnterpriseId, user.getId());
        }
        return projectMapper.selectList(qw).stream().map(p -> toTodoItem(p, role)).toList();
    }

    private DemandTodoItem toTodoItem(TrialProject project, UserRole role) {
        ArchiveStatus status = ArchiveStatus.of(project.getStatus());
        String route = switch (role) {
            case AUDITOR -> switch (status) {
                case PENDING, LEDGER_INCOMPLETE -> "/center/audit/archive/ledger";
                case LEDGER_OK -> "/center/audit/archive/confirm";
                case CONFIRMED -> "/center/audit/archive/collect";
                case BRIEF_GENERATED -> "/center/audit/archive/brief-audit";
                case BRIEF_PUBLISHED -> "/center/audit/archive/archive";
                default -> "/center/audit/archive/ledger";
            };
            case DISPATCHER -> switch (status) {
                case COLLECTED, STATS_UNAVAILABLE -> "/center/dispatch/archive/cycle-stats";
                case STATS_OK, BRIEF_RETURNED -> "/center/dispatch/archive/brief-generate";
                default -> "/center/dispatch/archive/cycle-stats";
            };
            case ENTERPRISE -> "/enterprise/archive/brief-view";
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

    private ArchiveDetailResponse buildDetail(SysUser user, TrialProject project) {
        ProjectArchive archive = findArchiveRecord(project.getId());
        ServiceBrief brief = archive != null && archive.getBriefId() != null
                ? briefMapper.selectById(archive.getBriefId()) : null;
        SysUser enterprise = project.getEnterpriseId() != null ? userMapper.selectById(project.getEnterpriseId()) : null;
        ArchiveStatus status = ProjectStage.ARCHIVE.name().equals(project.getStage())
                ? ArchiveStatus.of(project.getStatus())
                : ArchiveStatus.CLOSED;

        List<WorkflowLog> logs = workflowLogMapper.selectList(new LambdaQueryWrapper<WorkflowLog>()
                .eq(WorkflowLog::getProjectId, project.getId())
                .orderByAsc(WorkflowLog::getCreatedAt));

        return ArchiveDetailResponse.builder()
                .projectId(project.getId())
                .projectNo(project.getProjectNo())
                .title(project.getTitle())
                .stage(project.getStage())
                .status(project.getStatus())
                .statusLabel(ProjectStatusHelper.label(project))
                .currentNode(project.getCurrentNode())
                .enterpriseName(enterprise != null ? enterprise.getOrgName() : null)
                .ledgerJson(archive != null ? archive.getLedgerJson() : null)
                .collectRemark(archive != null ? archive.getCollectRemark() : null)
                .briefId(brief != null ? brief.getId() : null)
                .briefTitle(brief != null ? brief.getTitle() : null)
                .briefContent(brief != null ? brief.getContent() : null)
                .briefAuditStatus(brief != null ? brief.getAuditStatus() : null)
                .briefAuditRemark(brief != null ? brief.getAuditRemark() : null)
                .steps(buildSteps(status))
                .logs(logs.stream().map(l -> ArchiveDetailResponse.LogItem.builder()
                        .fromStatus(l.getFromStatus())
                        .toStatus(l.getToStatus())
                        .remark(l.getRemark())
                        .time(l.getCreatedAt())
                        .build()).toList())
                .build();
    }

    private List<ArchiveDetailResponse.ProgressStep> buildSteps(ArchiveStatus current) {
        record Node(String name, ArchiveStatus threshold) {}
        List<Node> nodes = List.of(
                new Node("项目台账维护", ArchiveStatus.PENDING),
                new Node("档案信息确认", ArchiveStatus.LEDGER_OK),
                new Node("结案资料归集", ArchiveStatus.CONFIRMED),
                new Node("中试周期统计", ArchiveStatus.COLLECTED),
                new Node("服务简报生成", ArchiveStatus.STATS_OK),
                new Node("简报内容审核", ArchiveStatus.BRIEF_GENERATED),
                new Node("档案信息归档", ArchiveStatus.BRIEF_PUBLISHED)
        );
        int currentOrd = current.ordinal();
        List<ArchiveDetailResponse.ProgressStep> steps = new ArrayList<>();
        for (Node node : nodes) {
            String st = node.threshold.ordinal() < currentOrd ? "done"
                    : node.threshold == current ? "active" : "pending";
            steps.add(ArchiveDetailResponse.ProgressStep.builder().node(node.name()).status(st).build());
        }
        return steps;
    }

    private ArchiveStatsResponse buildStats(boolean requireAvailability) {
        List<TrialProject> all = projectMapper.selectList(new LambdaQueryWrapper<TrialProject>()
                .ne(TrialProject::getStatus, "DRAFT")
                .orderByAsc(TrialProject::getCreatedAt));
        int total = all.size();
        long closed = all.stream()
                .filter(p -> ProjectStage.CLOSED.name().equals(p.getStage())
                        || ArchiveStatus.CLOSED.name().equals(p.getStatus()))
                .count();
        boolean available = total >= 1;
        if (requireAvailability && !available) {
            return ArchiveStatsResponse.builder()
                    .available(false)
                    .message("暂无足够项目数据，统计不可用")
                    .totalProjects(total)
                    .closedProjects((int) closed)
                    .successRate(total == 0 ? 0 : roundRate(closed, total))
                    .months(List.of())
                    .cycleDays(List.of())
                    .stageDistribution(List.of())
                    .build();
        }

        Map<String, List<TrialProject>> byMonth = all.stream()
                .collect(Collectors.groupingBy(p -> {
                    LocalDateTime t = p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.now();
                    return t.getYear() + "-" + String.format("%02d", t.getMonthValue());
                }, LinkedHashMap::new, Collectors.toList()));

        List<String> months = new ArrayList<>(byMonth.keySet());
        List<Integer> cycleDays = months.stream().map(m -> {
            List<TrialProject> group = byMonth.get(m);
            int sum = 0;
            int count = 0;
            for (TrialProject p : group) {
                if (p.getCreatedAt() != null && p.getUpdatedAt() != null) {
                    sum += (int) ChronoUnit.DAYS.between(p.getCreatedAt(), p.getUpdatedAt());
                    count++;
                }
            }
            return count == 0 ? 0 : Math.max(1, sum / count);
        }).toList();

        Map<String, Long> stageCount = all.stream()
                .collect(Collectors.groupingBy(TrialProject::getStage, Collectors.counting()));
        List<Map<String, Object>> stageDistribution = stageCount.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", ProjectStage.valueOf(e.getKey()).label());
                    item.put("value", e.getValue());
                    return item;
                }).toList();

        return ArchiveStatsResponse.builder()
                .available(true)
                .message("统计数据就绪")
                .totalProjects(total)
                .closedProjects((int) closed)
                .successRate(roundRate(closed, total))
                .months(months)
                .cycleDays(cycleDays)
                .stageDistribution(stageDistribution)
                .build();
    }

    private double roundRate(long closed, int total) {
        if (total == 0) {
            return 0;
        }
        return Math.round(closed * 1000.0 / total) / 10.0;
    }

    private String buildDefaultBriefContent(TrialProject project, ArchiveStatsResponse stats) {
        return "项目 " + project.getProjectNo() + "（" + project.getTitle() + "）已完成中试全流程。"
                + "累计项目 " + stats.getTotalProjects() + " 个，结案 "
                + stats.getClosedProjects() + " 个，成功率 "
                + stats.getSuccessRate() + "%。";
    }

    private String statsJson(ArchiveStatsResponse stats) {
        return "{\"total\":" + stats.getTotalProjects()
                + ",\"closed\":" + stats.getClosedProjects()
                + ",\"successRate\":" + stats.getSuccessRate() + "}";
    }

    private ProjectArchive requireArchiveRecord(Long projectId) {
        ProjectArchive archive = findArchiveRecord(projectId);
        if (archive == null) {
            archive = new ProjectArchive();
            archive.setProjectId(projectId);
            archive.setCreatedAt(LocalDateTime.now());
            archive.setUpdatedAt(LocalDateTime.now());
            archiveMapper.insert(archive);
        }
        return archive;
    }

    private ProjectArchive findArchiveRecord(Long projectId) {
        return archiveMapper.selectOne(new LambdaQueryWrapper<ProjectArchive>()
                .eq(ProjectArchive::getProjectId, projectId)
                .orderByDesc(ProjectArchive::getId)
                .last("LIMIT 1"));
    }

    private void transition(TrialProject project, ArchiveStatus to, String node, Long operatorId, String remark) {
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

    private void notifyDispatchers(TrialProject project) {
        userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRole, UserRole.DISPATCHER.name()))
                .forEach(u -> notificationService.notifyUser(u.getId(), project.getId(), "ARCHIVE",
                        "档案待统计", project.getProjectNo() + " " + project.getCurrentNode()));
    }

    private void notifyAuditors(TrialProject project) {
        userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRole, UserRole.AUDITOR.name()))
                .forEach(u -> notificationService.notifyUser(u.getId(), project.getId(), "ARCHIVE",
                        "简报待审核", project.getProjectNo() + " " + project.getCurrentNode()));
    }

    private TrialProject requireProject(Long projectId) {
        TrialProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    private TrialProject requireArchiveProject(Long projectId) {
        TrialProject project = requireProject(projectId);
        if (!ProjectStage.ARCHIVE.name().equals(project.getStage())) {
            throw new BusinessException("项目不在档案阶段");
        }
        return project;
    }

    private void assertReadable(SysUser user, TrialProject project) {
        UserRole role = UserRole.valueOf(user.getRole());
        if (role == UserRole.ENTERPRISE && !user.getId().equals(project.getEnterpriseId())) {
            throw new BusinessException(403, "无权查看该项目");
        }
    }

    private void assertBriefReadable(SysUser user, TrialProject project, ServiceBrief brief) {
        UserRole role = UserRole.valueOf(user.getRole());
        if (role == UserRole.ENTERPRISE) {
            if (!user.getId().equals(project.getEnterpriseId())) {
                throw new BusinessException(403, "无权查看该简报");
            }
            if (!"PASSED".equals(brief.getAuditStatus())) {
                throw new BusinessException("简报尚未发布");
            }
            return;
        }
        if (role == UserRole.DISPATCHER || role == UserRole.AUDITOR) {
            return;
        }
        throw new BusinessException(403, "无权查看该简报");
    }

    private void requireStatus(TrialProject project, ArchiveStatus... expected) {
        ArchiveStatus current = ArchiveStatus.of(project.getStatus());
        for (ArchiveStatus s : expected) {
            if (s == current) {
                return;
            }
        }
        throw new BusinessException("当前状态为「" + current.label() + "」，不可执行此操作");
    }

    private String text(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}
