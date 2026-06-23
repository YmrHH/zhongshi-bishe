package com.gzpprod.center.module.demand.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DemandTodoItem {

    private Long projectId;
    private String projectNo;
    private String title;
    private String status;
    private String statusLabel;
    private String currentNode;
    private String action;
    private String route;
    private String updatedAt;
}
