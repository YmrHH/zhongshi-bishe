package com.gzpprod.center.module.feedback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review_record")
public class ReviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String type;
    private String result;
    private String opinion;
    private Long reviewerId;
    private LocalDateTime createdAt;
}
