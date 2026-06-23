package com.gzpprod.center.module.demand.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class DemandFormRequest {

    @NotBlank(message = "项目名称不能为空")
    private String title;

    private String content;
    private String pilotType;
    private Integer expectedDays;
    private String contactName;
    private String contactPhone;
    private List<MaterialItem> materials;

    @Data
    public static class MaterialItem {
        private String fileUrl;
        private String fileName;
        private String materialType;
    }
}
