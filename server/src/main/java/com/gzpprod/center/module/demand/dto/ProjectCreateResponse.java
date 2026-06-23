package com.gzpprod.center.module.demand.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectCreateResponse {

    private Long projectId;
    private String projectNo;
}
