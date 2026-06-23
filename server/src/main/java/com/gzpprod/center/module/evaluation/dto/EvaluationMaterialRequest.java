package com.gzpprod.center.module.evaluation.dto;

import lombok.Data;

import java.util.List;

@Data
public class EvaluationMaterialRequest {

    private String content;
    private String remark;
    private String requirement;
    private String notice;
    private String conclusion;
    private String opinion;
    private Boolean passed;
    private List<MaterialItem> materials;

    @Data
    public static class MaterialItem {
        private String fileUrl;
        private String fileName;
        private String materialType;
    }
}
