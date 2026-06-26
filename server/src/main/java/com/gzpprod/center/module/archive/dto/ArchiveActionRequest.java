package com.gzpprod.center.module.archive.dto;

import lombok.Data;

@Data
public class ArchiveActionRequest {

    private Long projectId;
    private Boolean complete;
    private Boolean passed;
    private String remark;
    private String ledgerJson;
    private String title;
    private String content;
    private String collectRemark;
}
