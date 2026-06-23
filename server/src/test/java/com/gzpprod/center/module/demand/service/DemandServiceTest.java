package com.gzpprod.center.module.demand.service;

import com.gzpprod.center.common.DemandStatus;
import com.gzpprod.center.common.ProjectStage;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.demand.dto.DemandFormRequest;
import com.gzpprod.center.module.demand.dto.DemandSupplementRequest;
import com.gzpprod.center.module.demand.entity.Demand;
import com.gzpprod.center.module.demand.entity.TrialProject;
import com.gzpprod.center.module.demand.mapper.DemandMapper;
import com.gzpprod.center.module.demand.mapper.DemandMaterialMapper;
import com.gzpprod.center.module.demand.mapper.TrialProjectMapper;
import com.gzpprod.center.module.demand.mapper.WorkflowLogMapper;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.notification.service.NotificationService;
import com.gzpprod.center.module.evaluation.service.EvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandServiceTest {

    @Mock
    private TrialProjectMapper projectMapper;
    @Mock
    private DemandMapper demandMapper;
    @Mock
    private DemandMaterialMapper materialMapper;
    @Mock
    private WorkflowLogMapper workflowLogMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EvaluationService evaluationService;

    @InjectMocks
    private DemandService demandService;

    private SysUser enterprise;
    private TrialProject project;
    private Demand demand;

    @BeforeEach
    void setUp() {
        enterprise = new SysUser();
        enterprise.setId(10L);
        enterprise.setRole(UserRole.ENTERPRISE.name());
        enterprise.setOrgName("测试企业");

        project = new TrialProject();
        project.setId(1L);
        project.setProjectNo("ZS-2026-001");
        project.setTitle("测试项目");
        project.setEnterpriseId(10L);
        project.setStage(ProjectStage.DEMAND.name());
        project.setStatus(DemandStatus.DRAFT.name());

        demand = new Demand();
        demand.setId(100L);
        demand.setProjectId(1L);
    }

    @Test
    void submit_movesDraftToSubmitted() {
        when(projectMapper.selectById(1L)).thenReturn(project);
        when(demandMapper.selectOne(any())).thenReturn(demand);

        DemandFormRequest form = new DemandFormRequest();
        form.setTitle("测试项目");
        form.setContent("中试需求内容");
        form.setMaterials(new ArrayList<>());

        var result = demandService.submit(enterprise, 1L, form);

        assertEquals(DemandStatus.SUBMITTED.name(), result.getStatus());
        assertEquals("已提交待受理", result.getStatusLabel());
    }

    @Test
    void supplement_movesReturnedBackToSubmitted() {
        project.setStatus(DemandStatus.RETURNED.name());
        demand.setRejectReason("材料不齐");

        when(projectMapper.selectById(1L)).thenReturn(project);
        when(demandMapper.selectOne(any())).thenReturn(demand);
        when(materialMapper.selectList(any())).thenReturn(new ArrayList<>());

        DemandSupplementRequest request = new DemandSupplementRequest();
        request.setContent("补充后的需求说明");

        var result = demandService.supplement(enterprise, 1L, request);

        assertEquals(DemandStatus.SUBMITTED.name(), result.getStatus());
        assertEquals("已提交待受理", result.getStatusLabel());
    }
}
