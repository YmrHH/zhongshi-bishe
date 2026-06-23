package com.gzpprod.center.module.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_material")
public class EvaluationMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long evaluationId;
    private String fileUrl;
    private String fileName;
    private String materialType;
    private Integer version;
    private LocalDateTime createdAt;
}
