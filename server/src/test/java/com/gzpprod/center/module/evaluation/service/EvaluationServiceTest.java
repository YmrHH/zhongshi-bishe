package com.gzpprod.center.module.evaluation.service;

import com.gzpprod.center.common.EvaluationStatus;
import com.gzpprod.center.common.ProjectStage;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.demand.entity.TrialProject;
import com.gzpprod.center.module.demand.mapper.TrialProjectMapper;
import com.gzpprod.center.module.demand.mapper.WorkflowLogMapper;
import com.gzpprod.center.module.evaluation.dto.EvaluationMaterialRequest;
import com.gzpprod.center.module.evaluation.entity.Evaluation;
import com.gzpprod.center.module.evaluation.mapper.EvaluationMapper;
import com.gzpprod.center.module.evaluation.mapper.EvaluationMaterialMapper;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.notification.service.NotificationService;
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
class EvaluationServiceTest {

    @Mock private TrialProjectMapper projectMapper;
    @Mock private EvaluationMapper evaluationMapper;
    @Mock private EvaluationMaterialMapper materialMapper;
    @Mock private WorkflowLogMapper workflowLogMapper;
    @Mock private SysUserMapper userMapper;
    @Mock private NotificationService notificationService;
    @InjectMocks private EvaluationService evaluationService;

    private SysUser dispatcher;
    private SysUser enterprise;
    private TrialProject project;
    private Evaluation evaluation;

    @BeforeEach
    void setUp() {
        dispatcher = new SysUser();
        dispatcher.setId(1L);
        dispatcher.setRole(UserRole.DISPATCHER.name());

        enterprise = new SysUser();
        enterprise.setId(10L);
        enterprise.setRole(UserRole.ENTERPRISE.name());

        project = new TrialProject();
        project.setId(1L);
        project.setProjectNo("ZS-2026-001");
        project.setTitle("测试");
        project.setEnterpriseId(10L);
        project.setStage(ProjectStage.EVALUATION.name());
        project.setStatus(EvaluationStatus.CONDITION_EVAL.name());

        evaluation = new Evaluation();
        evaluation.setId(100L);
        evaluation.setProjectId(1L);
    }

    @Test
    void conditionFail_movesToReturned() {
        when(projectMapper.selectById(1L)).thenReturn(project);
        when(evaluationMapper.selectOne(any())).thenReturn(evaluation);

        EvaluationMaterialRequest req = new EvaluationMaterialRequest();
        req.setPassed(false);
        req.setRemark("场地不满足");

        var result = evaluationService.condition(dispatcher, 1L, req);
        assertEquals(EvaluationStatus.CONDITION_RETURNED.name(), result.getStatus());
    }

    @Test
    void conditionSupplement_loopsBackToPrecheck() {
        project.setStatus(EvaluationStatus.CONDITION_RETURNED.name());
        when(projectMapper.selectById(1L)).thenReturn(project);
        when(evaluationMapper.selectOne(any())).thenReturn(evaluation);
        when(materialMapper.selectList(any())).thenReturn(new ArrayList<>());

        var result = evaluationService.conditionSupplement(enterprise, 1L, new EvaluationMaterialRequest());
        assertEquals(EvaluationStatus.PRECHECK.name(), result.getStatus());
    }
}
