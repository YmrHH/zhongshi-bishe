package com.gzpprod.center.module.dispatch.dto;

import lombok.Data;

@Data
public class DispatchActionRequest {

    private Long resourceId;
    private Long technicianId;
    private Boolean passed;
    private String remark;
    private String content;
    private Integer progressPct;
}
