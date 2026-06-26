package com.gzpprod.center.module.feedback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feedback_report")
public class FeedbackReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long taskId;
    private String content;
    private String fileUrl;
    private String fileName;
    private String validateRemark;
    private String modifyRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
