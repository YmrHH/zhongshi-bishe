package com.gzpprod.center.module.common.controller;

import com.gzpprod.center.common.Result;
import com.gzpprod.center.common.SecurityUtils;
import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.common.dto.DashboardResponse;
import com.gzpprod.center.module.demand.dto.DemandTodoItem;
import com.gzpprod.center.module.demand.service.DemandService;
import com.gzpprod.center.module.evaluation.service.EvaluationService;
import com.gzpprod.center.module.dispatch.service.DispatchService;
import com.gzpprod.center.module.feedback.service.FeedbackService;
import com.gzpprod.center.module.archive.service.ArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "公共")
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final DemandService demandService;
    private final EvaluationService evaluationService;
    private final DispatchService dispatchService;
    private final FeedbackService feedbackService;
    private final ArchiveService archiveService;
    private final SysUserMapper userMapper;

    @Operation(summary = "工作台首页统计")
    @GetMapping("/dashboard")
    public Result<DashboardResponse> dashboard(Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        List<DemandTodoItem> allTodos = new ArrayList<>();
        allTodos.addAll(demandService.listTodos(user));
        allTodos.addAll(evaluationService.listTodos(user));
        allTodos.addAll(dispatchService.listTodos(user));
        allTodos.addAll(feedbackService.listTodos(user));
        allTodos.addAll(archiveService.listTodos(user));

        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("pending", (int) allTodos.stream()
                .filter(t -> t.getStatus().contains("SUBMITTED") || t.getStatus().contains("DRAFT")
                        || t.getStatus().contains("RETURNED") || t.getStatus().contains("PRECHECK"))
                .count());
        stats.put("processing", (int) allTodos.stream()
                .filter(t -> !t.getStatus().contains("ARCHIVED") && !t.getStatus().equals("CLOSED"))
                .count());
        stats.put("returned", (int) allTodos.stream()
                .filter(t -> t.getStatus().contains("RETURNED"))
                .count());
        stats.put("completed", (int) allTodos.stream()
                .filter(t -> t.getStatus().contains("ARCHIVED"))
                .count());

        List<DashboardResponse.TodoItem> todos = allTodos.stream()
                .map(t -> DashboardResponse.TodoItem.builder()
                        .projectId(t.getProjectId())
                        .taskId(t.getTaskId())
                        .projectNo(t.getProjectNo())
                        .title(t.getTitle())
                        .module(resolveModule(t))
                        .status(t.getStatusLabel())
                        .time(t.getUpdatedAt())
                        .route(t.getRoute())
                        .build())
                .toList();

        return Result.ok(DashboardResponse.builder().stats(stats).todos(todos).build());
    }

    private String resolveModule(DemandTodoItem t) {
        if (t.getRoute() == null) {
            return "中试需求管理";
        }
        if (t.getRoute().contains("/evaluation/")) {
            return "中试评估管理";
        }
        if (t.getRoute().contains("/dispatch/")) {
            return "中试调度管理";
        }
        if (t.getRoute().contains("/feedback/")) {
            return "中试反馈管理";
        }
        if (t.getRoute().contains("/archive/")) {
            return "中试档案管理";
        }
        return "中试需求管理";
    }

    private String moduleLabel(UserRole role) {
        return switch (role) {
            case DISPATCHER, AUDITOR, ENTERPRISE -> "中试需求管理";
            case TECHNICIAN -> "中试调度管理";
        };
    }
}
