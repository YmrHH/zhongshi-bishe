package com.gzpprod.center.module.common.controller;

import com.gzpprod.center.common.Result;
import com.gzpprod.center.common.SecurityUtils;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.common.dto.ProjectProgressResponse;
import com.gzpprod.center.module.common.service.ProjectProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "项目")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectProgressService projectProgressService;
    private final SysUserMapper userMapper;

    @Operation(summary = "项目跨模块进度（按 stage 返回当前模块步骤条）")
    @GetMapping("/{id}/progress")
    public Result<ProjectProgressResponse> progress(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(projectProgressService.getProgress(user, id));
    }
}
