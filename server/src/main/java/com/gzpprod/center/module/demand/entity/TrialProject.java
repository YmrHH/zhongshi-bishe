package com.gzpprod.center.module.demand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("trial_project")
public class TrialProject {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String projectNo;
    private String title;
    private Long enterpriseId;
    private String stage;
    private String status;
    private String currentNode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
