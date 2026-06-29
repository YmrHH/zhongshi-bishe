package com.gzpprod.center.module.demand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^$|^1\\d{10}$", message = "联系电话须为11位手机号")
    private String contactPhone;
    private List<MaterialItem> materials;

    @Data
    public static class MaterialItem {
        private String fileUrl;
        private String fileName;
        private String materialType;
    }
}
