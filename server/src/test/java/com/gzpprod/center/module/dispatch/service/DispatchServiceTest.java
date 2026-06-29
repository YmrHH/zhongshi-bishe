package com.gzpprod.center.module.dispatch.service;

import com.gzpprod.center.common.DispatchStatus;
import com.gzpprod.center.common.ProjectStage;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.demand.entity.TrialProject;
import com.gzpprod.center.module.demand.mapper.TrialProjectMapper;
import com.gzpprod.center.module.demand.mapper.WorkflowLogMapper;
import com.gzpprod.center.module.dispatch.dto.DispatchActionRequest;
import com.gzpprod.center.module.dispatch.entity.DispatchTask;
import com.gzpprod.center.module.dispatch.mapper.DispatchTaskMapper;
import com.gzpprod.center.module.dispatch.mapper.ResourceMapper;
import com.gzpprod.center.module.dispatch.mapper.TaskProgressMapper;
import com.gzpprod.center.module.evaluation.mapper.EvaluationMapper;
import com.gzpprod.center.module.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @Mock private TrialProjectMapper projectMapper;
    @Mock private ResourceMapper resourceMapper;
    @Mock private DispatchTaskMapper taskMapper;
    @Mock private TaskProgressMapper progressMapper;
    @Mock private WorkflowLogMapper workflowLogMapper;
    @Mock private SysUserMapper userMapper;
    @Mock private EvaluationMapper evaluationMapper;
    @Mock private NotificationService notificationService;
    @InjectMocks private DispatchService dispatchService;

    private SysUser dispatcher;
    private TrialProject project;

    @BeforeEach
    void setUp() {
        dispatcher = new SysUser();
        dispatcher.setId(1L);
        dispatcher.setRole(UserRole.DISPATCHER.name());

        project = new TrialProject();
        project.setId(10L);
        project.setProjectNo("ZS-TEST");
        project.setTitle("测试项目");
        project.setStage(ProjectStage.DISPATCH.name());
        project.setStatus(DispatchStatus.EXECUTING.name());
    }

    @Test
    void execConfirm_keepsDispatchStage() {
        when(projectMapper.selectById(10L)).thenReturn(project);
        DispatchTask task = new DispatchTask();
        task.setProgressPct(100);
        when(taskMapper.selectOne(any())).thenReturn(task);
        when(progressMapper.selectList(any())).thenReturn(java.util.List.of());
        when(workflowLogMapper.selectList(any())).thenReturn(java.util.List.of());
        when(evaluationMapper.selectOne(any())).thenReturn(null);
        when(resourceMapper.selectList(any())).thenReturn(java.util.List.of());
        when(userMapper.selectList(any())).thenReturn(java.util.List.of());

        var result = dispatchService.execConfirm(dispatcher, 10L, new DispatchActionRequest());

        assertEquals(DispatchStatus.EXEC_DONE.name(), result.getStatus());
        assertEquals(ProjectStage.DISPATCH.name(), result.getStage());
    }

    @Test
    void archive_movesToFeedbackStage() {
        project.setStatus(DispatchStatus.EXEC_DONE.name());
        when(projectMapper.selectById(10L)).thenReturn(project);
        when(taskMapper.selectOne(any())).thenReturn(new DispatchTask());
        when(progressMapper.selectList(any())).thenReturn(java.util.List.of());
        when(workflowLogMapper.selectList(any())).thenReturn(java.util.List.of());
        when(evaluationMapper.selectOne(any())).thenReturn(null);
        when(resourceMapper.selectList(any())).thenReturn(java.util.List.of());
        when(userMapper.selectList(any())).thenReturn(java.util.List.of());

        var result = dispatchService.archive(dispatcher, 10L);

        assertEquals(ProjectStage.FEEDBACK.name(), result.getStage());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void execConfirm_closesTechnicianTask() {
        when(projectMapper.selectById(10L)).thenReturn(project);
        DispatchTask task = new DispatchTask();
        task.setId(5L);
        task.setProjectId(10L);
        task.setStatus("EXECUTING");
        task.setProgressPct(100);
        when(taskMapper.selectOne(any())).thenReturn(task);
        when(progressMapper.selectList(any())).thenReturn(java.util.List.of());
        when(workflowLogMapper.selectList(any())).thenReturn(java.util.List.of());
        when(evaluationMapper.selectOne(any())).thenReturn(null);
        when(resourceMapper.selectList(any())).thenReturn(java.util.List.of());
        when(userMapper.selectList(any())).thenReturn(java.util.List.of());

        dispatchService.execConfirm(dispatcher, 10L, new DispatchActionRequest());

        assertEquals("CLOSED", task.getStatus());
    }

    @Test
    void technicianTodos_excludeNonDispatchStage() {
        SysUser technician = new SysUser();
        technician.setId(2L);
        technician.setRole(UserRole.TECHNICIAN.name());

        DispatchTask task = new DispatchTask();
        task.setId(5L);
        task.setProjectId(10L);
        task.setTechnicianId(2L);
        task.setStatus("EXECUTING");
        task.setUpdatedAt(java.time.LocalDateTime.now());

        TrialProject feedbackProject = new TrialProject();
        feedbackProject.setId(10L);
        feedbackProject.setProjectNo("ZS-TEST");
        feedbackProject.setTitle("测试");
        feedbackProject.setStage(ProjectStage.FEEDBACK.name());
        feedbackProject.setStatus("PENDING");

        when(taskMapper.selectList(any())).thenReturn(java.util.List.of(task));
        when(projectMapper.selectById(10L)).thenReturn(feedbackProject);

        assertEquals(0, dispatchService.listTodos(technician).size());
    }
}
