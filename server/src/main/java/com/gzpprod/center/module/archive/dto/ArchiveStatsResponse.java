package com.gzpprod.center.module.archive.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ArchiveStatsResponse {

    private boolean available;
    private String message;
    private List<String> months;
    private List<Integer> cycleDays;
    private List<Map<String, Object>> stageDistribution;
    private int totalProjects;
    private int closedProjects;
    private double successRate;
}
