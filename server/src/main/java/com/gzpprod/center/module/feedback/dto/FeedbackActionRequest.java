package com.gzpprod.center.module.feedback.dto;

import lombok.Data;

@Data
public class FeedbackActionRequest {

    private Boolean passed;
    private String remark;
    private String content;
    private String fileUrl;
    private String fileName;
}
