package com.gzpprod.center.module.common.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {

    private Map<String, Integer> stats;
    private List<TodoItem> todos;

    @Data
    @Builder
    public static class TodoItem {
        private Long projectId;
        private Long taskId;
        private String projectNo;
        private String title;
        private String module;
        private String status;
        private String time;
        private String route;
    }
}
