package com.gzpprod.center.module.demand.dto;

import com.gzpprod.center.module.demand.dto.DemandFormRequest.MaterialItem;
import lombok.Data;

import java.util.List;

@Data
public class DemandSupplementRequest {

    private String content;
    private List<MaterialItem> materials;
}
