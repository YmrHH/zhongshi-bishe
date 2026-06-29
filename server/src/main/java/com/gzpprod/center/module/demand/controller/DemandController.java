package com.gzpprod.center.module.demand.controller;

import com.gzpprod.center.common.PageResult;
import com.gzpprod.center.common.Result;
import com.gzpprod.center.common.SecurityUtils;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.demand.dto.*;
import com.gzpprod.center.module.demand.service.DemandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "需求管理")
@RestController
@RequiredArgsConstructor
public class DemandController {

    private final DemandService demandService;
    private final SysUserMapper userMapper;

    @Operation(summary = "需求待办列表（分页）")
    @GetMapping("/api/demand/todos")
    public Result<PageResult<DemandTodoItem>> todos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.listTodosPage(user, page, pageSize));
    }

    @Operation(summary = "企业我的需求项目（分页）")
    @GetMapping("/api/demand/enterprise/projects")
    public Result<PageResult<DemandEnterpriseProjectItem>> enterpriseProjects(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "ALL") String stageFilter,
            @RequestParam(required = false) String keyword,
            Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.listEnterpriseProjectsPage(user, page, pageSize, stageFilter, keyword));
    }

    @Operation(summary = "创建需求项目（草稿）")
    @PostMapping("/api/projects/demand/create")
    public Result<ProjectCreateResponse> create(@Valid @RequestBody DemandFormRequest request,
                                                Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.createProject(user, request));
    }

    @Operation(summary = "需求详情/预览")
    @GetMapping("/api/projects/{id}/demand/preview")
    public Result<DemandDetailResponse> preview(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.getDetail(user, id));
    }

    @Operation(summary = "保存需求草稿")
    @PostMapping("/api/projects/{id}/demand/draft")
    public Result<DemandDetailResponse> draft(@PathVariable Long id,
                                              @RequestBody DemandFormRequest request,
                                              Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.saveDraft(user, id, request));
    }

    @Operation(summary = "提交需求")
    @PostMapping("/api/projects/{id}/demand/submit")
    public Result<DemandDetailResponse> submit(@PathVariable Long id,
                                               @Valid @RequestBody DemandFormRequest request,
                                               Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.submit(user, id, request));
    }

    @Operation(summary = "需求受理登记")
    @PostMapping({"/api/projects/{id}/demand/accept-register", "/api/projects/{id}/demand/accept"})
    public Result<DemandDetailResponse> acceptRegister(@PathVariable Long id,
                                                         @RequestBody(required = false) DemandAcceptRegisterRequest request,
                                                         Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) {
            request = new DemandAcceptRegisterRequest();
        }
        return Result.ok(demandService.acceptRegister(user, id, request));
    }

    @Operation(summary = "材料核验与受理决定（一步完成）")
    @PostMapping("/api/projects/{id}/demand/verify")
    public Result<DemandDetailResponse> verify(@PathVariable Long id,
                                               @RequestBody DemandVerifyRequest request,
                                               Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.verify(user, id, request));
    }

    @Operation(summary = "需求退回")
    @PostMapping("/api/projects/{id}/demand/reject")
    public Result<DemandDetailResponse> reject(@PathVariable Long id,
                                               @Valid @RequestBody DemandRejectRequest request,
                                               Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.reject(user, id, request));
    }

    @Operation(summary = "材料补充")
    @PostMapping("/api/projects/{id}/demand/supplement")
    public Result<DemandDetailResponse> supplement(@PathVariable Long id,
                                                   @RequestBody DemandSupplementRequest request,
                                                   Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.supplement(user, id, request));
    }

    @Operation(summary = "受理结果录入")
    @PostMapping("/api/projects/{id}/demand/accept-result")
    public Result<DemandDetailResponse> acceptResult(@PathVariable Long id,
                                                     @RequestBody DemandAcceptResultRequest request,
                                                     Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.acceptResult(user, id, request));
    }

    @Operation(summary = "受理回执签收")
    @PostMapping("/api/projects/{id}/demand/receipt")
    public Result<DemandDetailResponse> receipt(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.receipt(user, id));
    }

    @Operation(summary = "需求受理归档")
    @PostMapping("/api/projects/{id}/demand/archive")
    public Result<DemandDetailResponse> archive(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.archive(user, id));
    }

    @Operation(summary = "受理进度详情")
    @GetMapping("/api/projects/{id}/demand/progress")
    public Result<DemandDetailResponse> progress(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(demandService.getDetail(user, id));
    }
}
