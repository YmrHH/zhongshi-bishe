package com.gzpprod.center.module.demand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("demand")
public class Demand {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String content;
    private String pilotType;
    private Integer expectedDays;
    private String contactName;
    private String contactPhone;
    private String rejectReason;
    private String acceptOpinion;
    private String acceptResult;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
