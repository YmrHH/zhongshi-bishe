package com.gzpprod.center.module.demand.dto;

import lombok.Data;

@Data
public class DemandVerifyRequest {

    /** 材料是否齐全合规 */
    private boolean complete;
    /** 材料齐全时是否同意受理（complete=true 时必填） */
    private Boolean accepted;
    private String opinion;
}
