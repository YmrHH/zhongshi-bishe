package com.gzpprod.center.module.dispatch.controller;

import com.gzpprod.center.common.Result;
import com.gzpprod.center.common.SecurityUtils;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.demand.dto.DemandTodoItem;
import com.gzpprod.center.module.dispatch.dto.DispatchActionRequest;
import com.gzpprod.center.module.dispatch.dto.DispatchDetailResponse;
import com.gzpprod.center.module.dispatch.service.DispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "调度管理")
@RestController
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;
    private final SysUserMapper userMapper;

    @GetMapping("/api/dispatch/todos")
    public Result<List<DemandTodoItem>> todos(Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(dispatchService.listTodos(user));
    }

    @GetMapping("/api/projects/{id}/dispatch/progress")
    public Result<DispatchDetailResponse> progress(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(dispatchService.getDetail(user, id));
    }

    @PostMapping("/api/projects/{id}/dispatch/match")
    public Result<DispatchDetailResponse> match(@PathVariable Long id,
                                              @RequestBody DispatchActionRequest request,
                                              Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(dispatchService.match(user, id, request));
    }

    @PostMapping("/api/projects/{id}/dispatch/assign")
    public Result<DispatchDetailResponse> assign(@PathVariable Long id,
                                               @RequestBody DispatchActionRequest request,
                                               Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(dispatchService.assign(user, id, request));
    }

    @PostMapping("/api/projects/{id}/dispatch/assign-notice")
    public Result<DispatchDetailResponse> assignNotice(@PathVariable Long id,
                                                       @RequestBody(required = false) DispatchActionRequest request,
                                                       Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) {
            request = new DispatchActionRequest();
        }
        return Result.ok(dispatchService.assignNotice(user, id, request));
    }

    @PostMapping("/api/projects/{id}/dispatch/supervise")
    public Result<DispatchDetailResponse> supervise(@PathVariable Long id,
                                                    @RequestBody(required = false) DispatchActionRequest request,
                                                    Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) {
            request = new DispatchActionRequest();
        }
        return Result.ok(dispatchService.supervise(user, id, request));
    }

    @PostMapping("/api/projects/{id}/dispatch/reassign")
    public Result<DispatchDetailResponse> reassign(@PathVariable Long id,
                                                   @RequestBody DispatchActionRequest request,
                                                   Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(dispatchService.reassign(user, id, request));
    }

    @PostMapping("/api/projects/{id}/dispatch/exec-confirm")
    public Result<DispatchDetailResponse> execConfirm(@PathVariable Long id,
                                                      @RequestBody(required = false) DispatchActionRequest request,
                                                      Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) {
            request = new DispatchActionRequest();
        }
        return Result.ok(dispatchService.execConfirm(user, id, request));
    }

    @PostMapping("/api/projects/{id}/dispatch/archive")
    public Result<DispatchDetailResponse> archive(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(dispatchService.archive(user, id));
    }

    @Operation(summary = "企业确认执行进度知悉")
    @PostMapping("/api/projects/{id}/dispatch/acknowledge")
    public Result<DispatchDetailResponse> acknowledge(@PathVariable Long id,
                                                      @RequestBody(required = false) DispatchActionRequest request,
                                                      Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) {
            request = new DispatchActionRequest();
        }
        return Result.ok(dispatchService.acknowledgeProgress(user, id, request));
    }

    @Operation(summary = "接收任务")
    @PostMapping("/api/tasks/{id}/receive")
    public Result<DispatchDetailResponse> receive(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(dispatchService.receiveTask(user, id));
    }

    @Operation(summary = "确认签收任务")
    @PostMapping("/api/tasks/{id}/confirm")
    public Result<DispatchDetailResponse> confirm(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(dispatchService.confirmTask(user, id));
    }

    @Operation(summary = "填报进度")
    @PostMapping("/api/tasks/{id}/progress")
    public Result<DispatchDetailResponse> reportProgress(@PathVariable Long id,
                                                         @RequestBody DispatchActionRequest request,
                                                         Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(dispatchService.reportProgress(user, id, request));
    }
}
