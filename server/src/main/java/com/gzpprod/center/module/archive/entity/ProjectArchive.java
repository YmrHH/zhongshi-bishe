package com.gzpprod.center.module.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_archive")
public class ProjectArchive {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String ledgerJson;
    private String collectRemark;
    private Long briefId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
