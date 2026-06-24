package com.gzpprod.center.module.dispatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dispatch_task")
public class DispatchTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long resourceId;
    private Long technicianId;
    private String status;
    private Integer progressPct;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
