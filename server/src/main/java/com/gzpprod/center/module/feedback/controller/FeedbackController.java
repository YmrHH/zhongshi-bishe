package com.gzpprod.center.module.feedback.controller;

import com.gzpprod.center.common.Result;
import com.gzpprod.center.common.SecurityUtils;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.demand.dto.DemandTodoItem;
import com.gzpprod.center.module.feedback.dto.FeedbackActionRequest;
import com.gzpprod.center.module.feedback.dto.FeedbackDetailResponse;
import com.gzpprod.center.module.feedback.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "反馈管理")
@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final SysUserMapper userMapper;

    @GetMapping("/api/feedback/todos")
    public Result<List<DemandTodoItem>> todos(Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(feedbackService.listTodos(user));
    }

    @GetMapping("/api/projects/{id}/feedback/detail")
    public Result<FeedbackDetailResponse> detail(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(feedbackService.getDetail(user, id));
    }

    @GetMapping("/api/projects/{id}/feedback/review-detail")
    public Result<FeedbackDetailResponse> reviewDetail(@PathVariable Long id, Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(feedbackService.getDetail(user, id));
    }

    @Operation(summary = "试验结果提交")
    @PostMapping("/api/projects/{id}/feedback/submit")
    public Result<FeedbackDetailResponse> submit(@PathVariable Long id,
                                                 @RequestBody FeedbackActionRequest request,
                                                 Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(feedbackService.submit(user, id, request));
    }

    @Operation(summary = "试验数据校验")
    @PostMapping("/api/projects/{id}/feedback/validate")
    public Result<FeedbackDetailResponse> validate(@PathVariable Long id,
                                                 @RequestBody FeedbackActionRequest request,
                                                 Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(feedbackService.validate(user, id, request));
    }

    @Operation(summary = "中试报告审核")
    @PostMapping("/api/projects/{id}/feedback/audit")
    public Result<FeedbackDetailResponse> audit(@PathVariable Long id,
                                                @RequestBody FeedbackActionRequest request,
                                                Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(feedbackService.audit(user, id, request));
    }

    @Operation(summary = "试验结果修改")
    @PostMapping("/api/projects/{id}/feedback/modify")
    public Result<FeedbackDetailResponse> modify(@PathVariable Long id,
                                                 @RequestBody FeedbackActionRequest request,
                                                 Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(feedbackService.modify(user, id, request));
    }

    @Operation(summary = "结果复核确认")
    @PostMapping("/api/projects/{id}/feedback/review")
    public Result<FeedbackDetailResponse> review(@PathVariable Long id,
                                                 @RequestBody(required = false) FeedbackActionRequest request,
                                                 Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) {
            request = new FeedbackActionRequest();
        }
        return Result.ok(feedbackService.review(user, id, request));
    }

    @Operation(summary = "中试报告归档")
    @PostMapping("/api/projects/{id}/feedback/report-archive")
    public Result<FeedbackDetailResponse> reportArchive(@PathVariable Long id,
                                                        @RequestBody(required = false) FeedbackActionRequest request,
                                                        Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) {
            request = new FeedbackActionRequest();
        }
        return Result.ok(feedbackService.reportArchive(user, id, request));
    }

    @Operation(summary = "复核结果通知")
    @PostMapping("/api/projects/{id}/feedback/review-notice")
    public Result<FeedbackDetailResponse> reviewNotice(@PathVariable Long id,
                                                       @RequestBody(required = false) FeedbackActionRequest request,
                                                       Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) {
            request = new FeedbackActionRequest();
        }
        return Result.ok(feedbackService.reviewNotice(user, id, request));
    }

    @Operation(summary = "复核意见反馈")
    @PostMapping("/api/projects/{id}/feedback/review-feedback")
    public Result<FeedbackDetailResponse> reviewFeedback(@PathVariable Long id,
                                                         @RequestBody FeedbackActionRequest request,
                                                         Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(feedbackService.reviewFeedback(user, id, request));
    }

    @Operation(summary = "反馈结果审核")
    @PostMapping("/api/projects/{id}/feedback/feedback-audit")
    public Result<FeedbackDetailResponse> feedbackAudit(@PathVariable Long id,
                                                        @RequestBody FeedbackActionRequest request,
                                                        Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        return Result.ok(feedbackService.feedbackAudit(user, id, request));
    }

    @Operation(summary = "报告结案归档")
    @PostMapping("/api/projects/{id}/feedback/case-archive")
    public Result<FeedbackDetailResponse> caseArchive(@PathVariable Long id,
                                                      @RequestBody(required = false) FeedbackActionRequest request,
                                                      Authentication authentication) {
        var user = SecurityUtils.requireUser(authentication, userMapper);
        if (request == null) {
            request = new FeedbackActionRequest();
        }
        return Result.ok(feedbackService.caseArchive(user, id, request));
    }
}
