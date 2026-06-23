package com.gzpprod.center.module.demand.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DemandRejectRequest {

    @NotBlank(message = "退回意见不能为空")
    private String reason;
}
