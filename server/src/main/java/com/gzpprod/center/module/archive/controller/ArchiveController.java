package com.gzpprod.center.module.archive.controller;

import com.gzpprod.center.common.Result;
import com.gzpprod.center.common.SecurityUtils;
import com.gzpprod.center.module.archive.dto.*;
import com.gzpprod.center.module.archive.service.ArchiveService;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.demand.dto.DemandTodoItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "档案管理")
@RestController
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;
    private final SysUserMapper userMapper;

    @GetMapping("/api/archive/todos")
    public Result<List<DemandTodoItem>> todos(Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(archiveService.listTodos(user));
    }

    @Operation(summary = "项目台账查询")
    @GetMapping("/api/archive/ledger")
    public Result<ArchiveDetailResponse> ledger(@RequestParam Long projectId, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(archiveService.getLedger(user, projectId));
    }

    @Operation(summary = "项目台账维护")
    @PutMapping("/api/archive/ledger")
    public Result<ArchiveDetailResponse> updateLedger(@RequestBody ArchiveActionRequest request,
                                                     Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(archiveService.updateLedger(user, request));
    }

    @GetMapping("/api/projects/{id}/archive/detail")
    public Result<ArchiveDetailResponse> detail(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(archiveService.getDetail(user, id));
    }

    @Operation(summary = "档案信息确认")
    @PostMapping("/api/archive/projects/{id}/confirm")
    public Result<ArchiveDetailResponse> confirm(@PathVariable Long id,
                                                 @RequestBody(required = false) ArchiveActionRequest request,
                                                 Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) {
            request = new ArchiveActionRequest();
        }
        return Result.ok(archiveService.confirm(user, id, request));
    }

    @Operation(summary = "结案资料归集")
    @PostMapping("/api/archive/projects/{id}/collect")
    public Result<ArchiveDetailResponse> collect(@PathVariable Long id,
                                                 @RequestBody ArchiveActionRequest request,
                                                 Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(archiveService.collect(user, id, request));
    }

    @Operation(summary = "中试周期统计")
    @GetMapping("/api/archive/cycle-stats")
    public Result<ArchiveStatsResponse> cycleStats(@RequestParam Long projectId, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(archiveService.cycleStats(user, projectId));
    }

    @Operation(summary = "成功率分析")
    @GetMapping("/api/archive/success-rate")
    public Result<ArchiveStatsResponse> successRate(@RequestParam Long projectId, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(archiveService.successRate(user, projectId));
    }

    @Operation(summary = "周期统计确认")
    @PostMapping("/api/archive/cycle-stats/confirm")
    public Result<ArchiveDetailResponse> confirmStats(@RequestBody ArchiveActionRequest request,
                                                      Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(archiveService.confirmStats(user, request.getProjectId(), request));
    }

    @Operation(summary = "服务简报生成")
    @PostMapping("/api/archive/brief/generate")
    public Result<ArchiveDetailResponse> generateBrief(@RequestBody ArchiveActionRequest request,
                                                       Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(archiveService.generateBrief(user, request));
    }

    @Operation(summary = "简报内容审核")
    @PostMapping("/api/archive/brief/audit")
    public Result<ArchiveDetailResponse> auditBrief(@RequestBody ArchiveActionRequest request,
                                                    Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(archiveService.auditBrief(user, request));
    }

    @Operation(summary = "查看服务简报")
    @GetMapping("/api/archive/brief/{id}")
    public Result<BriefDetailResponse> getBrief(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(archiveService.getBrief(user, id));
    }

    @Operation(summary = "档案信息归档")
    @PostMapping("/api/archive/projects/{id}/archive")
    public Result<ArchiveDetailResponse> archive(@PathVariable Long id,
                                               @RequestBody(required = false) ArchiveActionRequest request,
                                               Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) {
            request = new ArchiveActionRequest();
        }
        return Result.ok(archiveService.archive(user, id, request));
    }
}
