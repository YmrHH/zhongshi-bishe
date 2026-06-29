package com.gzpprod.center.module.demand.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DemandEnterpriseProjectItem {

    private Long projectId;
    private String projectNo;
    private String title;
    private String stage;
    private String status;
    private String statusLabel;
    private String currentNode;
    private String submittedAt;
    private boolean canViewProgress;
}
