package com.gzpprod.center.module.dispatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_progress")
public class TaskProgress {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Integer progressPct;
    private String content;
    private LocalDateTime reportTime;
}
