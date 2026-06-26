package com.gzpprod.center.module.feedback.service;

import com.gzpprod.center.common.FeedbackStatus;
import com.gzpprod.center.common.ProjectStage;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.demand.entity.TrialProject;
import com.gzpprod.center.module.demand.mapper.TrialProjectMapper;
import com.gzpprod.center.module.demand.mapper.WorkflowLogMapper;
import com.gzpprod.center.module.dispatch.entity.DispatchTask;
import com.gzpprod.center.module.dispatch.mapper.DispatchTaskMapper;
import com.gzpprod.center.module.feedback.dto.FeedbackActionRequest;
import com.gzpprod.center.module.feedback.entity.FeedbackReport;
import com.gzpprod.center.module.feedback.entity.ReviewRecord;
import com.gzpprod.center.module.feedback.mapper.FeedbackReportMapper;
import com.gzpprod.center.module.feedback.mapper.ReviewRecordMapper;
import com.gzpprod.center.module.notification.service.NotificationService;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
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
class FeedbackServiceTest {

    @Mock private TrialProjectMapper projectMapper;
    @Mock private WorkflowLogMapper workflowLogMapper;
    @Mock private FeedbackReportMapper reportMapper;
    @Mock private ReviewRecordMapper reviewMapper;
    @Mock private DispatchTaskMapper taskMapper;
    @Mock private SysUserMapper userMapper;
    @Mock private NotificationService notificationService;
    @InjectMocks private FeedbackService feedbackService;

    private SysUser auditor;
    private TrialProject project;

    @BeforeEach
    void setUp() {
        auditor = new SysUser();
        auditor.setId(3L);
        auditor.setRole(UserRole.AUDITOR.name());

        project = new TrialProject();
        project.setId(10L);
        project.setProjectNo("ZS-TEST");
        project.setTitle("测试项目");
        project.setStage(ProjectStage.FEEDBACK.name());
        project.setStatus(FeedbackStatus.VALIDATED.name());
    }

    @Test
    void auditReject_returnsToModify() {
        when(projectMapper.selectById(10L)).thenReturn(project);
        when(reportMapper.selectOne(any())).thenReturn(new FeedbackReport());
        when(taskMapper.selectOne(any())).thenReturn(new DispatchTask());
        when(workflowLogMapper.selectList(any())).thenReturn(java.util.List.of());
        when(reviewMapper.selectList(any())).thenReturn(java.util.List.of());
        when(userMapper.selectList(any())).thenReturn(java.util.List.of());

        FeedbackActionRequest request = new FeedbackActionRequest();
        request.setPassed(false);
        request.setRemark("数据不完整");

        var result = feedbackService.audit(auditor, 10L, request);

        assertEquals(FeedbackStatus.AUDIT_REJECTED.name(), result.getStatus());
    }

    @Test
    void caseArchive_movesToArchiveStage() {
        project.setStatus(FeedbackStatus.FEEDBACK_AUDIT_PASSED.name());
        when(projectMapper.selectById(10L)).thenReturn(project);
        when(reportMapper.selectOne(any())).thenReturn(new FeedbackReport());
        when(taskMapper.selectOne(any())).thenReturn(new DispatchTask());
        when(workflowLogMapper.selectList(any())).thenReturn(java.util.List.of());
        when(reviewMapper.selectList(any())).thenReturn(java.util.List.of());

        var result = feedbackService.caseArchive(auditor, 10L, new FeedbackActionRequest());

        assertEquals(ProjectStage.ARCHIVE.name(), result.getStage());
        assertEquals("PENDING", result.getStatus());
    }
}
