package com.gzpprod.center.module.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_brief")
public class ServiceBrief {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String title;
    private String content;
    private String statsJson;
    private String auditStatus;
    private String auditRemark;
    private Long generatorId;
    private Long auditorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
