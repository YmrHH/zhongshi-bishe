package com.gzpprod.center.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long page;
    private long pageSize;

    public static <T> PageResult<T> of(IPage<?> page, List<T> records) {
        return PageResult.<T>builder()
                .records(records)
                .total(page.getTotal())
                .page(page.getCurrent())
                .pageSize(page.getSize())
                .build();
    }
}
