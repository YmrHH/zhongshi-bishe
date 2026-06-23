package com.gzpprod.center.module.evaluation.controller;

import com.gzpprod.center.common.Result;
import com.gzpprod.center.common.SecurityUtils;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.demand.dto.DemandTodoItem;
import com.gzpprod.center.module.evaluation.dto.EvaluationDetailResponse;
import com.gzpprod.center.module.evaluation.dto.EvaluationMaterialRequest;
import com.gzpprod.center.module.evaluation.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评估管理")
@RestController
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final SysUserMapper userMapper;

    @Operation(summary = "评估待办列表")
    @GetMapping("/api/evaluation/todos")
    public Result<List<DemandTodoItem>> todos(Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.listTodos(user));
    }

    @Operation(summary = "评估详情")
    @GetMapping("/api/projects/{id}/evaluation/detail")
    public Result<EvaluationDetailResponse> detail(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.getDetail(user, id));
    }

    @PostMapping("/api/projects/{id}/evaluation/precheck")
    public Result<EvaluationDetailResponse> precheck(@PathVariable Long id,
                                                     @RequestBody(required = false) EvaluationMaterialRequest request,
                                                     Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) request = new EvaluationMaterialRequest();
        return Result.ok(evaluationService.precheck(user, id, request));
    }

    @PostMapping("/api/projects/{id}/evaluation/condition")
    public Result<EvaluationDetailResponse> condition(@PathVariable Long id,
                                                      @RequestBody EvaluationMaterialRequest request,
                                                      Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.condition(user, id, request));
    }

    @PostMapping("/api/projects/{id}/evaluation/rectify-notice")
    public Result<EvaluationDetailResponse> rectifyNotice(@PathVariable Long id,
                                                          @RequestBody EvaluationMaterialRequest request,
                                                          Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.rectifyNotice(user, id, request));
    }

    @PostMapping("/api/projects/{id}/evaluation/condition-supplement")
    public Result<EvaluationDetailResponse> conditionSupplement(@PathVariable Long id,
                                                                @RequestBody EvaluationMaterialRequest request,
                                                                Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.conditionSupplement(user, id, request));
    }

    @PostMapping("/api/projects/{id}/evaluation/resource")
    public Result<EvaluationDetailResponse> resource(@PathVariable Long id,
                                                     @RequestBody EvaluationMaterialRequest request,
                                                     Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.resource(user, id, request));
    }

    @PostMapping("/api/projects/{id}/evaluation/feasibility")
    public Result<EvaluationDetailResponse> feasibility(@PathVariable Long id,
                                                        @RequestBody EvaluationMaterialRequest request,
                                                        Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.feasibility(user, id, request));
    }

    @PostMapping("/api/projects/{id}/evaluation/supplement")
    public Result<EvaluationDetailResponse> supplement(@PathVariable Long id,
                                                       @RequestBody EvaluationMaterialRequest request,
                                                       Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.supplement(user, id, request));
    }

    @PostMapping("/api/projects/{id}/evaluation/conclusion")
    public Result<EvaluationDetailResponse> conclusion(@PathVariable Long id,
                                                       @RequestBody EvaluationMaterialRequest request,
                                                       Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.conclusion(user, id, request));
    }

    @PostMapping("/api/projects/{id}/evaluation/receipt")
    public Result<EvaluationDetailResponse> receipt(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.receipt(user, id));
    }

    @PostMapping("/api/projects/{id}/evaluation/feedback")
    public Result<EvaluationDetailResponse> feedback(@PathVariable Long id,
                                                     @RequestBody EvaluationMaterialRequest request,
                                                     Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.feedback(user, id, request));
    }

    @PostMapping("/api/projects/{id}/evaluation/archive")
    public Result<EvaluationDetailResponse> archive(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(evaluationService.archive(user, id));
    }
}
