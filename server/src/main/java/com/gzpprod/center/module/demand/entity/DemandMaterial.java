package com.gzpprod.center.module.demand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("demand_material")
public class DemandMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long demandId;
    private String fileUrl;
    private String fileName;
    private String materialType;
    private Integer version;
    private LocalDateTime createdAt;
}
