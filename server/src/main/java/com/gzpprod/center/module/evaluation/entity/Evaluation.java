package com.gzpprod.center.module.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation")
public class Evaluation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String precheckRemark;
    private String conditionResult;
    private String conditionRemark;
    private String rectifyNotice;
    private String resourceRemark;
    private String resourceRequirement;
    private String feasibilityResult;
    private String feasibilityRemark;
    private String conclusion;
    private String conclusionOpinion;
    private String feedbackContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
