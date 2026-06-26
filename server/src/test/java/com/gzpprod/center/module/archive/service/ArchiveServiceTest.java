package com.gzpprod.center.module.archive.service;

import com.gzpprod.center.common.ArchiveStatus;
import com.gzpprod.center.common.ProjectStage;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.archive.dto.ArchiveActionRequest;
import com.gzpprod.center.module.archive.entity.ProjectArchive;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.demand.entity.TrialProject;
import com.gzpprod.center.module.demand.mapper.TrialProjectMapper;
import com.gzpprod.center.module.demand.mapper.WorkflowLogMapper;
import com.gzpprod.center.module.archive.mapper.ProjectArchiveMapper;
import com.gzpprod.center.module.archive.mapper.ServiceBriefMapper;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
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
class ArchiveServiceTest {

    @Mock private TrialProjectMapper projectMapper;
    @Mock private WorkflowLogMapper workflowLogMapper;
    @Mock private ProjectArchiveMapper archiveMapper;
    @Mock private ServiceBriefMapper briefMapper;
    @Mock private SysUserMapper userMapper;
    @Mock private NotificationService notificationService;
    @InjectMocks private ArchiveService archiveService;

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
        project.setStage(ProjectStage.ARCHIVE.name());
        project.setStatus(ArchiveStatus.PENDING.name());
    }

    @Test
    void updateLedger_complete_movesToConfirm() {
        when(projectMapper.selectById(10L)).thenReturn(project);
        when(archiveMapper.selectOne(any())).thenReturn(new ProjectArchive());
        when(workflowLogMapper.selectList(any())).thenReturn(java.util.List.of());

        ArchiveActionRequest request = new ArchiveActionRequest();
        request.setProjectId(10L);
        request.setComplete(true);
        request.setLedgerJson("{\"items\":[]}");

        var result = archiveService.updateLedger(auditor, request);

        assertEquals(ArchiveStatus.LEDGER_OK.name(), result.getStatus());
    }

    @Test
    void archive_movesToClosedStage() {
        project.setStatus(ArchiveStatus.BRIEF_PUBLISHED.name());
        when(projectMapper.selectById(10L)).thenReturn(project);
        when(archiveMapper.selectOne(any())).thenReturn(new ProjectArchive());
        when(workflowLogMapper.selectList(any())).thenReturn(java.util.List.of());

        var result = archiveService.archive(auditor, 10L, new ArchiveActionRequest());

        assertEquals(ProjectStage.CLOSED.name(), result.getStage());
        assertEquals("CLOSED", result.getStatus());
    }
}
